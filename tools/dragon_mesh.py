#!/usr/bin/env python3
"""2.7.0: the owner's dragon (input/kaido_dragon_form.zip: one binary FBX, 6 skinned meshes on a 92-bone rig with its own takes, and
one 2048 x 2048 colour map) packed for the game as the Uo Uo no Mi's dragon form. numpy and Pillow (CI installs both).

The FBX is read as it is (tools/fbx_reader.py): the bones' rest transforms (Lcl Translation / Rotation (XYZ, degrees) / Scaling),
every mesh's corners (position, uv, normal, material), each control point's four strongest skin weights, every cluster's bind
(TransformLink), and the takes sampled at 30 frames a second. Format (gzip, big endian, read by sea/DragonModel.java):
  b"SRY1", int version (1)
  int bones:   for each: UTF name, short parent (-1: none), float t[3], q[4] (x, y, z, w), s[3] (rest local),
               float inverse bind 3 x 4 (rows of the bone's bind matrix inverted, blocks of the model's bind space)
  int parts:   for each: UTF name, byte kind (0 opaque, 1 cutout (the mane's and beard's cards), 2 glow (the fire)),
               int vertices, int quads
               vertices: short x, y, z (position = q / 32767 * EXTENT), char u, v (0..65535), byte nx, ny, nz,
                         byte bone[4] (unsigned), byte weight[4] (unsigned, summing to 255)
               quads:    char a, b, c, d (a triangle repeats its last corner)
  int clips:   for each: UTF name, float fps, int frames, int channels; channels: short bone, byte kind (0 rotation, 1 translation,
               2 scale), then frames x (rotation: short x, y, z, w / 32767; translation and scale: float x, y, z)
The model's space: blocks, +Y up, the dragon's head toward -Z as the FBX has it (sea/DragonModel turns it to face the look).

Usage: python3 tools/dragon_mesh.py <zip> <out dir> [--preview]   (writes seiryu.sry and seiryu.png)
"""
import gzip
import io
import math
import struct
import sys
import zipfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import fbx_reader  # noqa: E402

FBX_TIME = 46186158000
# The takes kept (the others: the same takes again (_1, _2, _3), the two long skills, the camera tracks)
TAKES = {
    "idle": "pl_kaido_orig02_bs01_idle_a",
    "form": "pl_kaido_orig02_bs01_metamorphosis_a",
    "unform": "pl_kaido_orig02_bs01_metamorphosis_b",
    "stun": "pl_kaido_orig02_bs01_stun",
    "down": "pl_kaido_orig02_bs01_down",
}
FPS = 30.0
# the file keeps the FBX's units (dragon/DragonForm.SCALE makes them blocks)
UNIT = 1.0
EXTENT = 128.0  # positions within +-EXTENT units
TEXTURE_SIZE = 1024
GLOW_PARTS = {"l_fire", "r_fire"}


def strip(name):
    return name.split("\x00")[0] if isinstance(name, str) else name


def props70(node):
    out = {}
    p = node.find("Properties70")
    if p is not None:
        for c in p.children:
            out[c.props[0]] = c.props[4:]
    return out


def euler_xyz(deg):
    """FBX eEulerXYZ: x first, then y, then z (column vectors: Rz Ry Rx)."""
    x, y, z = (math.radians(a) for a in deg)
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return rz @ ry @ rx


def trs(t, r, s):
    m = np.eye(4)
    m[:3, :3] = euler_xyz(r) @ np.diag(s)
    m[:3, 3] = t
    return m


def quat(m):
    """A rotation matrix (3 x 3, orthonormal) as (x, y, z, w)."""
    t = m[0, 0] + m[1, 1] + m[2, 2]
    if t > 0:
        s = math.sqrt(t + 1.0) * 2
        q = ((m[2, 1] - m[1, 2]) / s, (m[0, 2] - m[2, 0]) / s, (m[1, 0] - m[0, 1]) / s, 0.25 * s)
    elif m[0, 0] > m[1, 1] and m[0, 0] > m[2, 2]:
        s = math.sqrt(1.0 + m[0, 0] - m[1, 1] - m[2, 2]) * 2
        q = (0.25 * s, (m[0, 1] + m[1, 0]) / s, (m[0, 2] + m[2, 0]) / s, (m[2, 1] - m[1, 2]) / s)
    elif m[1, 1] > m[2, 2]:
        s = math.sqrt(1.0 + m[1, 1] - m[0, 0] - m[2, 2]) * 2
        q = ((m[0, 1] + m[1, 0]) / s, 0.25 * s, (m[1, 2] + m[2, 1]) / s, (m[0, 2] - m[2, 0]) / s)
    else:
        s = math.sqrt(1.0 + m[2, 2] - m[0, 0] - m[1, 1]) * 2
        q = ((m[0, 2] + m[2, 0]) / s, (m[1, 2] + m[2, 1]) / s, 0.25 * s, (m[1, 0] - m[0, 1]) / s)
    q = np.array(q)
    q /= np.linalg.norm(q)
    return q if q[3] >= 0 else -q


class Scene:
    def __init__(self, data):
        _, root = fbx_reader.load_bytes(data)
        objs = root.find("Objects")
        self.byid = {c.props[0]: c for c in objs.children}
        self.children = {}
        self.parents = {}
        for c in root.find("Connections").children:
            src, dst = c.props[1], c.props[2]
            prop = c.props[3] if len(c.props) > 3 else None
            self.children.setdefault(dst, []).append((src, prop))
            self.parents.setdefault(src, []).append((dst, prop))
        self.models = {i: n for i, n in self.byid.items() if n.name == "Model"}
        # the bone hierarchy: every Model (LimbNode, Null) but the meshes, parents first
        def model_parent(i):
            for d, _ in self.parents.get(i, []):
                if d in self.models:
                    return d
            return None
        bones = [i for i, n in self.models.items() if n.props[2] != "Mesh"]
        order, seen = [], set()

        def visit(i):
            if i in seen:
                return
            p = model_parent(i)
            if p is not None and p in bones:
                visit(p)
            seen.add(i)
            order.append(i)
        for i in bones:
            visit(i)
        self.bones = order
        self.index = {i: k for k, i in enumerate(order)}
        self.parent = [self.index.get(model_parent(i), -1) for i in order]
        self.names = [strip(self.models[i].props[1]) for i in order]
        self.rest = []
        for i in order:
            p = props70(self.models[i])
            self.rest.append((tuple(p.get("Lcl Translation", (0, 0, 0))), tuple(p.get("Lcl Rotation", (0, 0, 0))),
                              tuple(p.get("Lcl Scaling", (1, 1, 1)))))

    # -- meshes -------------------------------------------------------------------------------------------------------------------
    def meshes(self):
        out = []
        for gid, geo in self.byid.items():
            if geo.name != "Geometry":
                continue
            name = strip(geo.props[1])
            points = np.asarray(geo.find("Vertices").props[0], dtype=np.float64).reshape(-1, 3)
            pvi = np.asarray(geo.find("PolygonVertexIndex").props[0], dtype=np.int64)
            uv_layer = geo.find("LayerElementUV")
            uvs = np.asarray(uv_layer.find("UV").props[0], dtype=np.float64).reshape(-1, 2)
            corners = np.where(pvi < 0, -pvi - 1, pvi)
            uv_index = uv_layer.find("UVIndex")
            if strip(uv_layer.find("ReferenceInformationType").props[0]) == "IndexToDirect":
                uvs = uvs[np.asarray(uv_index.props[0])]
            if strip(uv_layer.find("MappingInformationType").props[0]) in ("ByVertice", "ByControlPoint"):
                uvs = uvs[corners]
            nl = geo.find("LayerElementNormal")
            normals = np.asarray(nl.find("Normals").props[0], dtype=np.float64).reshape(-1, 3)
            nmap = strip(nl.find("MappingInformationType").props[0])
            nref = strip(nl.find("ReferenceInformationType").props[0])
            if nref == "IndexToDirect":
                normals = normals[np.asarray(nl.find("NormalsIndex").props[0])]
            if nmap == "ByControlPoint" or nmap == "ByVertice":
                normals = normals[corners]
            polys = []
            start = 0
            for k, v in enumerate(pvi):
                if v < 0:
                    polys.append(list(range(start, k + 1)))
                    start = k + 1
            # the mesh's place: its model's (identity here) times the skin's bind
            model_id = next(d for d, _ in self.parents[gid] if d in self.models)
            skin = [s for s, _ in self.children.get(gid, []) if s in self.byid and self.byid[s].name == "Deformer"]
            weights = [[] for _ in range(len(points))]
            mesh_bind = None
            for sk in skin:
                for cl, _ in self.children.get(sk, []):
                    cluster = self.byid.get(cl)
                    if cluster is None or cluster.name != "Deformer":
                        continue
                    bone = next((s for s, _ in self.children.get(cl, []) if s in self.models), None)
                    if bone is None or cluster.find("Indexes") is None:
                        continue
                    idx = np.asarray(cluster.find("Indexes").props[0])
                    w = np.asarray(cluster.find("Weights").props[0])
                    transform = np.asarray(cluster.find("Transform").props[0]).reshape(4, 4).T
                    link = np.asarray(cluster.find("TransformLink").props[0]).reshape(4, 4).T
                    if mesh_bind is None:
                        mesh_bind = transform
                    self.binds.setdefault(self.index[bone], link)
                    for a, b in zip(idx, w):
                        if b > 0:
                            weights[a].append((self.index[bone], float(b)))
            out.append(dict(name=name, points=points, corners=corners, uvs=uvs, normals=normals, polys=polys,
                            # 2.7.0 (the owner: "the face is literally not on the head"): the clusters' Transform is NOT the
                            # meshes' bind here: every mesh's points already lie on its bones' binds (the face at z 8.7..11.7 on
                            # Head_Face's 8.54), and each Transform is the inverse of the bone the mesh hung from in the game it
                            # came from (the face's -8.538 = -Head_Face): applied, the face slid 8.5 units down the neck. Identity.
                            weights=weights, bind=np.eye(4)))
        return out

    # -- takes --------------------------------------------------------------------------------------------------------------------
    def take(self, name):
        """{bone index: {"T"|"R"|"S": [(times, values) per axis]}} of the take named."""
        stack = next(i for i, n in self.byid.items() if n.name == "AnimationStack" and strip(n.props[1]) == name)
        curves = {}
        end = 0
        for layer, _ in self.children.get(stack, []):
            if self.byid.get(layer) is None or self.byid[layer].name != "AnimationLayer":
                continue
            for cn, _ in self.children.get(layer, []):
                node = self.byid.get(cn)
                if node is None or node.name != "AnimationCurveNode":
                    continue
                target = next(((d, p) for d, p in self.parents.get(cn, []) if d in self.models), None)
                if target is None or target[0] not in self.index:
                    continue
                kind = {"Lcl Translation": "T", "Lcl Rotation": "R", "Lcl Scaling": "S"}.get(strip(target[1]))
                if kind is None:
                    continue
                defaults = props70(node)
                axes = []
                for ax, key in enumerate(("d|X", "d|Y", "d|Z")):
                    curve = next((self.byid[s] for s, p in self.children.get(cn, []) if strip(p) == key and s in self.byid), None)
                    if curve is None:
                        axes.append((np.array([0.0]), np.array([defaults.get(key, (0.0,))[0]])))
                        continue
                    t = np.asarray(curve.find("KeyTime").props[0], dtype=np.float64) / FBX_TIME
                    v = np.asarray(curve.find("KeyValueFloat").props[0], dtype=np.float64)
                    end = max(end, t.max())
                    axes.append((t, v))
                curves.setdefault(self.index[target[0]], {})[kind] = axes
        return curves, end


def sample(axes, t):
    return [float(np.interp(t, ts, vs)) for ts, vs in axes]


# each take's own height: aligned on the hover (idle's first frame) at the frame where it meets it
ANCHOR = {"form": -1, "unform": 0, "stun": 0, "down": 0}


def pack(zip_path):
    """{file name: bytes}: seiryu.sry (the mesh, rig and takes) and seiryu.png (the colour map at 1024)."""
    with zipfile.ZipFile(zip_path) as z:
        fbx_name = next(n for n in z.namelist() if n.lower().endswith(".fbx"))
        jpg_name = next(n for n in z.namelist() if n.lower().endswith((".jpg", ".png")))
        scene = Scene.__new__(Scene)
        scene.binds = {}
        Scene.__init__(scene, z.read(fbx_name))
        texture = z.read(jpg_name)
    meshes = scene.meshes()
    nb = len(scene.bones)
    # every bone's bind: from the clusters; bones no cluster names are bound where their rest pose puts them
    rest_global = [None] * nb
    for k in range(nb):
        t, r, s = scene.rest[k]
        local = trs(t, r, s)
        rest_global[k] = local if scene.parent[k] < 0 else rest_global[scene.parent[k]] @ local
    binds = [np.array(scene.binds.get(k, rest_global[k]), dtype=np.float64) for k in range(nb)]
    out = io.BytesIO()
    w = out.write
    w(b"SRY1")
    w(struct.pack(">i", 1))
    w(struct.pack(">i", nb))
    for k in range(nb):
        t, r, s = scene.rest[k]
        name = scene.names[k].encode("utf-8")
        w(struct.pack(">H", len(name)) + name)
        w(struct.pack(">h", scene.parent[k]))
        q = quat(euler_xyz(r))
        w(struct.pack(">10f", *(np.array(t) * UNIT), *q, *s))
        bind = binds[k].copy()
        bind[:3, 3] *= UNIT
        inv = np.linalg.inv(bind)
        w(struct.pack(">12f", *inv[:3, :].reshape(-1)))
    parts = []
    for m in meshes:
        kind = 2 if m["name"] in GLOW_PARTS else 0
        # the control points in the model's bind space, as the owner's model has them
        pts = (m["bind"] @ np.c_[m["points"], np.ones(len(m["points"]))].T).T[:, :3] * UNIT
        nrm_m = m["bind"][:3, :3]
        verts, vindex, quads = [], {}, []
        for poly in m["polys"]:
            ids = []
            for c in poly:
                p = int(m["corners"][c])
                u, v = m["uvs"][c]
                n = nrm_m @ m["normals"][c]
                n = n / (np.linalg.norm(n) or 1.0)
                key = (p, round(u * 65535), round((1.0 - v) * 65535), *(int(round(a * 127)) for a in n))
                if key not in vindex:
                    vindex[key] = len(verts)
                    verts.append(key)
                ids.append(vindex[key])
            if len(ids) == 3:
                quads.append((ids[0], ids[1], ids[2], ids[2]))
            elif len(ids) == 4:
                quads.append(tuple(ids))
            else:
                for i in range(1, len(ids) - 1):
                    quads.append((ids[0], ids[i], ids[i + 1], ids[i + 1]))
        parts.append((m["name"], kind, pts, m["weights"], verts, quads))
    w(struct.pack(">i", len(parts)))
    for name, kind, pts, weights, verts, quads in parts:
        assert len(verts) < 65536, name
        assert np.abs(pts).max() < EXTENT, (name, np.abs(pts).max())
        b = name.encode("utf-8")
        w(struct.pack(">H", len(b)) + b + struct.pack(">b", kind))
        w(struct.pack(">ii", len(verts), len(quads)))
        for key in verts:
            p = pts[key[0]]
            q = np.clip(np.round(p / EXTENT * 32767), -32767, 32767).astype(int)
            wl = sorted(weights[key[0]], key=lambda x: -x[1])[:4] or [(0, 1.0)]
            total = sum(x[1] for x in wl)
            ws = [int(round(x[1] / total * 255)) for x in wl]
            ws[0] += 255 - sum(ws)
            bones = [x[0] for x in wl] + [0] * (4 - len(wl))
            ws += [0] * (4 - len(ws))
            w(struct.pack(">3h2H3b4B4B", *q, min(65535, max(0, key[1])), min(65535, max(0, key[2])), *key[3:6], *bones, *ws))
        for quad in quads:
            w(struct.pack(">4H", *quad))
    pelvis = scene.names.index("Body_Pelvis")
    takes = {}
    for short, full in TAKES.items():
        curves, end = scene.take(full)
        frames = int(round(end * FPS)) + 1
        times = np.arange(frames) / FPS
        channels = []
        for bone, kinds in sorted(curves.items()):
            t0, r0, s0 = scene.rest[bone]
            rot = [quat(euler_xyz(sample(kinds["R"], t))) if "R" in kinds else quat(euler_xyz(r0)) for t in times]
            # keep a channel only when it moves or differs from the rest pose
            if "R" in kinds:
                rot = np.array(rot)
                for i in range(1, len(rot)):
                    if np.dot(rot[i], rot[i - 1]) < 0:
                        rot[i] = -rot[i]
                rest_q = quat(euler_xyz(r0))
                if np.max(np.abs(np.abs(rot @ rest_q) - 1.0)) > 1e-6:
                    channels.append((bone, 0, rot))
            for kind, code, base in (("T", 1, t0), ("S", 2, s0)):
                if kind in kinds:
                    vals = np.array([sample(kinds[kind], t) for t in times])
                    if code == 1:
                        vals = vals * UNIT
                        base = np.array(base) * UNIT
                    if np.max(np.abs(vals - np.array(base))) > 1e-5 or (code == 1 and bone == pelvis):
                        channels.append((bone, code, vals))
        takes[short] = (frames, channels)
    # each take on the hover's height: its pelvis where idle's is, at the frame it meets the hover
    def pelvis_at(channels, frame):
        return next(v[frame] for b, c, v in channels if b == pelvis and c == 1)
    hover = pelvis_at(takes["idle"][1], 0)
    for short, anchor in ANCHOR.items():
        frames, channels = takes[short]
        delta = hover - pelvis_at(channels, anchor)
        takes[short] = (frames, [(b, c, v + delta if b == pelvis and c == 1 else v) for b, c, v in channels])
    w(struct.pack(">i", len(takes)))
    for short, (frames, channels) in takes.items():
        b = short.encode("utf-8")
        w(struct.pack(">H", len(b)) + b)
        w(struct.pack(">fii", FPS, frames, len(channels)))
        for bone, code, vals in channels:
            w(struct.pack(">hb", bone, code))
            if code == 0:
                w(np.clip(np.round(vals * 32767), -32767, 32767).astype(">i2").tobytes())
            else:
                w(np.asarray(vals, dtype=">f4").tobytes())
    from PIL import Image
    image = Image.open(io.BytesIO(texture)).convert("RGB").resize((TEXTURE_SIZE, TEXTURE_SIZE), Image.LANCZOS)
    png = io.BytesIO()
    image.save(png, "PNG", optimize=True)
    return {"seiryu.sry": gzip.compress(out.getvalue(), 9, mtime=0), "seiryu.png": png.getvalue()}


if __name__ == "__main__":
    files = pack(sys.argv[1])
    dest = Path(sys.argv[2])
    dest.mkdir(parents=True, exist_ok=True)
    for name, data in files.items():
        (dest / name).write_bytes(data)
        print(name, len(data))
