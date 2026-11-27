#!/usr/bin/env python3
"""3.9.3 (the owner: "New Fruit: Name: ???? Rarity: Mythical Type: Zoan ... Z - Transforms (DIsturbing progressive transformation)",
with input/itsumade/Saint_Marcus_Mars_Itsumade.blend): the owner's Itsumade baked for the game in the dragon's model format
(dragon/DragonModel, version 2: a colour per vertex), read by itsumade/ItsuForm and client/itsumade/ItsuFx.

Run it with Blender's own Python module (CI does not need it: its outputs are committed):
    python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy     (bpy 5.3, Python 3.13)
    /tmp/bpy/bin/python tools/itsumade_bake.py [--ratio 0.03]
It writes src/main/resources/assets/hexhaki/itsumade/itsumade.sry and src/main/resources/assets/hexhaki/textures/piece/itsumade/itsumade.png.

What it keeps of the owner's file, exactly: every one of its 704 parts (meshes and the bevelled ink lines) in its own place, with its
own colours (the file colours its parts per vertex: the "Pigment" attribute, or the material's colour; the face, the beak and the eyes
mix in the painted image "mirrored facial pigments", baked here into the texture). What it changes for the phone: each part decimated
(collapse) from about 421k faces to about 14k. What it adds: the file has no rig (its parts hang off four empties: the model root, the
head, the jaw's hinge and each wing's shoulder), so the bake builds one, keeping the file's own pivots:
- the body (one continuous mesh: the neck, the breast and the long S-curved tail): its centreline found by the distance along the surface
  from the top of the neck; the chest, two neck bones, the pelvis and TAIL bones down the tail to its fiery tip; every vertex blended
  between the two joints it lies between (by that distance), and every part lying on the body (the flame-edged dorsal fin and its
  flicks, the flame collar, the ruffs, the cloud collar, the tail's fire, the rump plumes) by the nearest point of the centreline;
- the head and the lower jaw at the file's own pivots (everything the file hangs off them, rigid);
- the black cloud ring (the "black ribbon") on its own bone, the halo;
- each wing at the file's shoulder pivot, with an elbow, a wrist and a finger along its leading edge: its membranes blended per vertex
  along the span; each feather rigid where its root sits on the arm (so a folded wing overlaps its feathers, never bends them);
- each leg: the hip (the violet femoral plumage and the tibial cuff), the ankle (the scaled tarsometatarsus) and the toes.
The parts' kinds (Itsu kinds, itsumade/ItsuForm): 0 body, 2 fire (vermilion flame: alive, rippling like the phoenix's), 3 the black
ribbon (alive too), 4 the mane's hair (swaying).

The model's space: Blender's (x, y, z) to the game's (-x, z, y): +Y up, the face toward -Z. Units stay the file's (ItsuForm.SCALE makes
them blocks).
"""
import gzip
import heapq
import io
import math
import re
import struct
import sys
from pathlib import Path

import bpy  # noqa: I001 (bpy first: it makes bmesh importable)
import bmesh
import numpy as np
from mathutils import Matrix, Vector

ROOT = Path(__file__).resolve().parent.parent
BLEND = ROOT / "input" / "itsumade" / "Saint_Marcus_Mars_Itsumade.blend"
OUT_MODEL = ROOT / "src/main/resources/assets/hexhaki/itsumade/itsumade.sry"
OUT_TEXTURE = ROOT / "src/main/resources/assets/hexhaki/textures/piece/itsumade/itsumade.png"
EXTENT = 128.0
C = Matrix(((-1, 0, 0, 0), (0, 0, 1, 0), (0, 1, 0, 0), (0, 0, 0, 1)))
BODY = "Continuous neck, breast and S-curved tail"
FACE_IMAGE = "Itsumade  /  mirrored facial pigments"
# the atlas: a white square for every part coloured per vertex, then the face's painted parts baked over their own colours
ATLAS_W, ATLAS_H = 1024, 1024
WHITE = (0, 0, 8, 8)
# the face's three materials (each a mix of its colour and the painted image by the image's alpha): their regions
FACE_REGIONS = {}
# the materials that mix their colour with the painted image (the skull, the beak, the eyes): all fused into the one head now
FACE_MATS = ("20b", "30", "35b")
# 3.9.4: the fused head's own texture: its region of the atlas, and how far its cylindrical u runs (past 1 where a face crosses the seam)
HEAD_REGION = (0, 16, 1024, 1008)
HEAD_U_SPAN = 1.25
HEAD_TEX = []
TAIL = 14
# 3.9.4: an ink line (a bevelled curve) thinner than this (its bevel's radius, units) is left out: under a quarter of a game pixel at the
# creature's size (ItsuForm.SCALE), seen from anywhere; the faces it cost (about 140k triangles) buy nothing on a phone
INK = 0.016
# the faces the main parts keep (triangles): the body's tube, the lower jaw
BODY_TRIS = 3200
JAW_TRIS = 900
# triangles a part keeps at least (by what it is), and the ratio of the rest
FLOORS = (("tooth", 4), ("remex", 12), ("covert", 10), ("plume", 10), ("cuff feather", 8), ("Dorsal flick", 8), ("Cloud mantle", 10),
          ("Flame collar", 8), ("mane lock", 12), ("crown ridge", 10), ("Beard", 16), ("talon", 12), ("digit", 16), ("hallux", 14),
          ("Temple blaze", 14), ("Terminal fire", 24), ("whorl", 16), ("Inked eye", 60), ("rictus", 10),
          # the ink lines kept (thick enough to be seen): the beak's lips and gums, the flank's piping, the scutes, the cloud's commas
          ("lip edge", 60), ("dental ridge", 60), ("tomial", 40), ("scutella", 90), ("boundary of the indigo", 90), ("incised comma", 24),
          ("ember curl", 20), ("filigree", 30), ("light", 12))
FLOOR = 12


def args():
    ratio = 0.031
    if "--ratio" in sys.argv:
        ratio = float(sys.argv[sys.argv.index("--ratio") + 1])
    return ratio


def floor_of(name):
    for key, n in FLOORS:
        if key in name:
            return n
    return FLOOR


def srgb(c):
    c = np.clip(np.asarray(c, float), 0.0, 1.0)
    return np.where(c <= 0.0031308, c * 12.92, 1.055 * np.power(c, 1 / 2.4) - 0.055)


def world_verts(o):
    co = np.array([v.co[:] for v in o.data.vertices])
    m = np.array(o.matrix_world)
    return co @ m[:3, :3].T + m[:3, 3]


# ---------------------------------------------------------------- the body's centreline
def centreline(o):
    """(the joints' points along the body from the top of the neck to the tail's tip (n x 3), their distance along it, each body
    vertex's distance along the surface from the top)."""
    co = world_verts(o)
    ed = np.array([e.vertices[:] for e in o.data.edges])
    n = len(co)
    adj = [[] for _ in range(n)]
    lens = np.linalg.norm(co[ed[:, 0]] - co[ed[:, 1]], axis=1)
    for (a, b), ln in zip(ed, lens):
        adj[a].append((b, ln))
        adj[b].append((a, ln))
    top = int(np.argmax(co[:, 2]))
    dist = np.full(n, np.inf)
    dist[top] = 0.0
    heap = [(0.0, top)]
    while heap:
        d, u = heapq.heappop(heap)
        if d > dist[u]:
            continue
        for v, ln in adj[u]:
            nd = d + ln
            if nd < dist[v]:
                dist[v] = nd
                heapq.heappush(heap, (nd, v))
    length = float(dist.max())
    bins = 96
    pts = []
    ds = []
    for i in range(bins):
        m = (dist >= length * i / bins) & (dist < length * (i + 1) / bins + 1e-6)
        if m.sum() < 3:
            continue
        pts.append(co[m].mean(0))
        ds.append(float(dist[m].mean()))
    pts = np.array(pts)
    # smoothed a little (the bins' centroids wobble where the cross-section is uneven)
    sm = pts.copy()
    for _ in range(3):
        sm[1:-1] = (sm[:-2] + 2 * sm[1:-1] + sm[2:]) / 4
    return sm, np.array(ds), dist, length


def nearest_on_line(line, ds, p):
    """The distance along the centreline of the point of it nearest p (Blender coordinates)."""
    best = (1e9, 0.0)
    for i in range(len(line) - 1):
        a, b = line[i], line[i + 1]
        ab = b - a
        t = float(np.clip(np.dot(p - a, ab) / max(1e-9, np.dot(ab, ab)), 0.0, 1.0))
        q = a + ab * t
        d2 = float(np.dot(p - q, p - q))
        if d2 < best[0]:
            best = (d2, ds[i] + (ds[i + 1] - ds[i]) * t)
    return best[1]


def point_at(line, ds, d):
    d = min(max(d, ds[0]), ds[-1])
    i = int(np.searchsorted(ds, d)) - 1
    i = max(0, min(len(ds) - 2, i))
    t = (d - ds[i]) / max(1e-9, ds[i + 1] - ds[i])
    return line[i] + (line[i + 1] - line[i]) * t


# ---------------------------------------------------------------- the rig
class Rig:
    def __init__(self):
        self.names = []
        self.parent = []
        self.at = []  # Blender coordinates

    def add(self, name, parent, at):
        self.names.append(name)
        self.parent.append(-1 if parent is None else self.names.index(parent))
        self.at.append(np.array(at, float))
        return len(self.names) - 1

    def index(self, name):
        return self.names.index(name)


def build_rig(line, ds, length):
    rig = Rig()
    head = bpy.data.objects["Head  /  turn"]
    jaw = bpy.data.objects["Lower jaw  /  hinge"]
    port = bpy.data.objects["Port wing  /  shoulder pivot"]
    star = bpy.data.objects["Starboard wing  /  shoulder pivot"]
    rig.add("root", None, (0.0, 0.0, 0.0))
    chain = []  # (bone, distance along the body)
    d_chest, d_neck1, d_neck2, d_pelvis = 3.7, 2.8, 1.6, 5.6
    rig.add("chest", "root", point_at(line, ds, d_chest))
    rig.add("neck1", "chest", point_at(line, ds, d_neck1))
    rig.add("neck2", "neck1", point_at(line, ds, d_neck2))
    rig.add("head", "neck2", head.matrix_world.translation[:])
    rig.add("jaw", "head", jaw.matrix_world.translation[:])
    rig.add("halo", "chest", point_at(line, ds, d_neck1))
    rig.add("pelvis", "chest", point_at(line, ds, d_pelvis))
    chain += [("head", 0.0), ("neck2", d_neck2), ("neck1", d_neck1), ("chest", d_chest), ("pelvis", d_pelvis)]
    first = 6.6
    step = (length - 0.25 - first) / (TAIL - 1)
    prev = "pelvis"
    for k in range(TAIL):
        d = first + step * k
        name = f"tail{k + 1}"
        rig.add(name, prev, point_at(line, ds, d))
        chain.append((name, d))
        prev = name
    wings = {}
    for side, piv in (("L", port), ("R", star)):
        sgn = -1.0 if side == "L" else 1.0
        edge = bpy.data.objects[f"{side}  /  ink-blue leading edge and violet mantle"]
        ev = world_verts(edge)
        joints = []
        for x in (abs(piv.matrix_world.translation.x), 2.7, 4.7, 6.7):
            m = np.abs(np.abs(ev[:, 0]) - x) < 0.35
            top = ev[m][np.argsort(-ev[m][:, 2])[:max(3, m.sum() // 3)]]
            p = top.mean(0)
            joints.append((sgn * x, p[1], p[2] - 0.12))
        joints[0] = tuple(piv.matrix_world.translation[:])
        names = [f"shoulder{side}", f"elbow{side}", f"wrist{side}", f"finger{side}"]
        par = "chest"
        for nm, at in zip(names, joints):
            rig.add(nm, par, at)
            par = nm
        wings[side] = (names, [abs(j[0]) for j in joints], np.array(joints))
    legs = {}
    for side in ("L", "R"):
        fem = world_verts(bpy.data.objects[f"{side}  /  violet femoral plumage"])
        tar = world_verts(bpy.data.objects[f"{side}  /  scale-clad tarsometatarsus"])
        hip = fem[fem[:, 2] > fem[:, 2].max() - 0.35].mean(0)
        ankle = tar[tar[:, 2] > tar[:, 2].max() - 0.25].mean(0)
        toes = tar[tar[:, 2] < tar[:, 2].min() + 0.25].mean(0)
        rig.add(f"hip{side}", "pelvis", hip)
        rig.add(f"ankle{side}", f"hip{side}", ankle)
        rig.add(f"toes{side}", f"ankle{side}", toes)
        legs[side] = (hip, ankle, toes)
    return rig, chain, wings, legs


def chain_weights(chain, d):
    """Two bones and their weights for a point d along the body (the chain from the head to the tail's tip)."""
    if d <= chain[0][1]:
        return [(chain[0][0], 1.0)]
    for (a, da), (b, db) in zip(chain, chain[1:]):
        if d <= db:
            t = (d - da) / max(1e-9, db - da)
            return [(a, 1.0 - t), (b, t)]
    return [(chain[-1][0], 1.0)]


def span_weights(names, xs, x):
    x = abs(x)
    if x <= xs[0]:
        return [(names[0], 1.0)]
    for i in range(len(xs) - 1):
        if x <= xs[i + 1]:
            t = (x - xs[i]) / (xs[i + 1] - xs[i])
            # held rigid between the joints, blended near each (a joint's crease)
            t = min(1.0, max(0.0, (t - 0.72) / 0.28)) if i > 0 else min(1.0, max(0.0, (t - 0.55) / 0.45))
            return [(names[i], 1.0 - t), (names[i + 1], t)]
    return [(names[-1], 1.0)]


# ---------------------------------------------------------------- the look
def material_base(mat):
    if mat is None or not mat.use_nodes:
        return np.array([0.8, 0.8, 0.8])
    bsdf = next((n for n in mat.node_tree.nodes if n.type == "BSDF_PRINCIPLED"), None)
    if bsdf is None:
        return np.array([0.8, 0.8, 0.8])
    return np.array(bsdf.inputs["Base Color"].default_value[:3])


def face_key(mat):
    if mat is None:
        return None
    for k in FACE_MATS:
        if mat.name.startswith(k + " "):
            return k
    return None


def kind_of(name, mats):
    mn = " ".join(m.name for m in mats if m)
    if "vermilion flame" in mn or "orange flame interior" in mn:
        return 2
    if "awakening cloud" in mn or "smoke calligraphy" in mn or "curl of cloud" in mn:
        return 3
    if "umber and bronze mane" in mn or "chestnut lock grooves" in mn or "copper filament" in mn:
        return 4
    return 0


# ---------------------------------------------------------------- 3.9.4: the owner's notes on the model
# "the fiery mane of my model Is wrong ... it should follow the green line" (the line down the middle of the tail's back): the flame-edged
# dorsal fin and its flicks ran down the tail's side; each cross-section of them is turned about the tail's centreline onto its back
# (the creature faces -y: its back is +y), by the angle the fin stood off the back there (smoothed along the tail)
DORSAL = ("Continuous flame-edged dorsal fin", "Dorsal flick")
BACK = np.array([0.0, 1.0, 0.0])


def tangent_at(line, ds, d):
    a = point_at(line, ds, d - 0.15)
    b = point_at(line, ds, d + 0.15)
    t = b - a
    return t / max(1e-9, np.linalg.norm(t))


def turn_about(axis, angle):
    """The 3 x 3 rotation by angle (radians) about the unit axis (Rodrigues)."""
    x, y, z = axis
    c, s_ = math.cos(angle), math.sin(angle)
    k = np.array([[0, -z, y], [z, 0, -x], [-y, x, 0]])
    return np.eye(3) + s_ * k + (1 - c) * (k @ k)


def off_back(line, ds, p):
    """(distance along the body of p's nearest centreline point, the signed angle of p round the centreline from the back)."""
    d = nearest_on_line(line, ds, p)
    c = point_at(line, ds, d)
    t = tangent_at(line, ds, d)
    u = BACK - t * float(BACK @ t)
    u /= max(1e-9, np.linalg.norm(u))
    r = p - c
    r = r - t * float(r @ t)
    return d, math.atan2(float(np.cross(u, r) @ t), float(u @ r))


def dorsal_angles(line, ds):
    """The fin's angle off the back along the body: [(distance, angle)], each bin's circular mean, smoothed."""
    fin = world_verts(bpy.data.objects[DORSAL[0]])
    pts = [off_back(line, ds, p) for p in fin[:: max(1, len(fin) // 1500)]]
    d = np.array([q[0] for q in pts])
    a = np.array([q[1] for q in pts])
    lo, hi = d.min(), d.max()
    bins = 30
    out = []
    for i in range(bins):
        m = (d >= lo + (hi - lo) * i / bins) & (d <= lo + (hi - lo) * (i + 1) / bins)
        if m.sum() < 3:
            continue
        out.append(((lo + (hi - lo) * (i + 0.5) / bins), math.atan2(np.sin(a[m]).mean(), np.cos(a[m]).mean())))
    ang = np.unwrap(np.array([q[1] for q in out]))
    for _ in range(4):
        ang[1:-1] = (ang[:-2] + 2 * ang[1:-1] + ang[2:]) / 4
    return np.array([q[0] for q in out]), ang


def dorsal_turn(line, ds, table, p, root=None):
    """The world point p of the fin (or of a flick rooted at distance root) turned onto the back: (new point, its 3 x 3 turn)."""
    dd, ang = table
    d = nearest_on_line(line, ds, p) if root is None else root
    angle = float(np.interp(d, dd, ang))
    r = turn_about(tangent_at(line, ds, d), -angle)
    c = point_at(line, ds, d)
    return c + r @ (p - c), r


# "IF you could fix the head of my "????" fruit so it's 1 singular thing instead of a broken looking head" (with the manga's head from
# above): the skull with its tapered brow and cheeks, the upper beak and the fleshy corners of the mouth fused into ONE skin (a voxel
# remesh of their union: no seams, no beak stuck into a mask), coloured from the owner's own surfaces (their colours and the painted
# face under the image's alpha, read at the nearest point of each); the eyes, the teeth, the jaw and the mane stay the owner's parts
HEAD_FUSED = ("Cranium with tapered brow and cheeks", "Upper maxilla", "Fleshy rictus", "Inked eye socket")
HEAD_VOXEL = 0.026
HEAD_TRIS = 3000


def fused_head(face_rgb, face_alpha):
    """A new object: the fused skull, beak and eyes (world space), decimated to HEAD_TRIS, with its own texture: cylindrical uvs about
    the beak's axis (a "head" uv layer, the seam under the head) and the texture itself (HEAD_REGION's pixels), baked texel by texel
    from the owner's surfaces (each texel the colour of the nearest point of the original skull, beak, mouth corners and eyes: their
    colours with the image over them by its alpha), so the eyes and the head's red keep their exact shapes."""
    from mathutils.bvhtree import BVHTree
    from mathutils.geometry import barycentric_transform
    sources = [o for o in bpy.data.objects if o.type == "MESH" and o.name.startswith(HEAD_FUSED)]
    depsgraph = bpy.context.evaluated_depsgraph_get()
    verts, tris, cols, uvs, painted = [], [], [], [], []
    union = bmesh.new()
    for o in sources:
        ev = o.evaluated_get(depsgraph)
        me = ev.to_mesh()
        me.calc_loop_triangles()
        m = o.matrix_world
        base = len(verts)
        verts += [tuple(m @ v.co) for v in me.vertices]
        pig = me.color_attributes.get("Pigment")
        uv = me.uv_layers.active
        for lt in me.loop_triangles:
            tris.append(tuple(base + i for i in lt.vertices))
            mat = me.materials[lt.material_index] if lt.material_index < len(me.materials) else None
            fk = face_key(mat)
            corner_cols = []
            corner_uv = []
            for li, vi in zip(lt.loops, lt.vertices):
                if fk == "35b":
                    c = np.array([0.017, 0.014, 0.025])
                elif pig is not None and pig.domain == "POINT":
                    c = np.array(pig.data[vi].color[:3])
                else:
                    c = material_base(mat)
                corner_cols.append(srgb(c))
                corner_uv.append(tuple(uv.data[li].uv) if (uv is not None and fk) else (0.0, 0.0))
            cols.append(corner_cols)
            uvs.append(corner_uv)
            painted.append(fk is not None and uv is not None)
        tmp = bmesh.new()
        tmp.from_mesh(me)
        tmp.transform(m)
        scratch = bpy.data.meshes.new("fuse_scratch")
        tmp.to_mesh(scratch)
        union.from_mesh(scratch)
        tmp.free()
        ev.to_mesh_clear()
    mesh = bpy.data.meshes.new("Head  /  one skull")
    union.to_mesh(mesh)
    union.free()
    obj = bpy.data.objects.new("Head  /  one skull", mesh)
    bpy.context.scene.collection.objects.link(obj)
    rem = obj.modifiers.new("fuse", "REMESH")
    rem.mode = "VOXEL"
    rem.voxel_size = HEAD_VOXEL
    rem.adaptivity = 0.0
    depsgraph = bpy.context.evaluated_depsgraph_get()
    depsgraph.update()
    fused = bpy.data.meshes.new_from_object(obj.evaluated_get(depsgraph))
    obj.modifiers.clear()
    obj.data = fused
    dec = obj.modifiers.new("fit", "DECIMATE")
    dec.decimate_type = "COLLAPSE"
    dec.ratio = min(1.0, HEAD_TRIS / max(1, sum(len(p.vertices) - 2 for p in fused.polygons)))
    dec.use_collapse_triangulate = True
    depsgraph = bpy.context.evaluated_depsgraph_get()
    depsgraph.update()
    final = bpy.data.meshes.new_from_object(obj.evaluated_get(depsgraph))
    obj.modifiers.clear()
    obj.data = final
    # the cylinder's frame: the head's own (the file's "Head / turn" empty): along the beak, across, up; the seam under the head
    head = bpy.data.objects["Head  /  turn"].matrix_world
    origin = np.array(head.translation)
    along = np.array(head.to_3x3() @ Vector((1, 0, 0)))
    across = np.array(head.to_3x3() @ Vector((0, 1, 0)))
    up = np.cross(along, across)
    up /= np.linalg.norm(up)
    if up[2] < 0:
        up = -up
    co = np.array([v.co[:] for v in final.vertices])
    rel = co - origin
    a = rel @ along
    theta = np.arctan2(rel @ across, rel @ up)
    u_of = theta / (2 * math.pi) + 0.5
    lo, hi = a.min(), a.max()
    v_of = (a - lo) / (hi - lo)
    uvl = final.uv_layers.new(name="head")
    corner_uv = {}
    for poly in final.polygons:
        us = [u_of[final.loops[li].vertex_index] for li in poly.loop_indices]
        wrap = max(us) - min(us) > 0.5
        for li in poly.loop_indices:
            vi = final.loops[li].vertex_index
            u = u_of[vi] + (1.0 if wrap and u_of[vi] < 0.5 else 0.0)
            uvl.data[li].uv = (u / HEAD_U_SPAN, v_of[vi])
            corner_uv[li] = (u / HEAD_U_SPAN, v_of[vi])
    # the texture: each final triangle rasterised in its uvs; each texel the owner's colour at the nearest point to its place
    tree = BVHTree.FromPolygons(verts, tris)
    ih, iw = face_alpha.shape
    x0, y0, w, h = HEAD_REGION
    tex = np.zeros((h, w, 3), np.float32)
    done = np.zeros((h, w), bool)
    final.calc_loop_triangles()
    e1, e2, e3 = Vector((1, 0, 0)), Vector((0, 1, 0)), Vector((0, 0, 1))
    for lt in final.loop_triangles:
        pts = np.array([corner_uv[li] for li in lt.loops]) * [w - 1, h - 1]
        ps = [final.vertices[i].co for i in lt.vertices]
        # each texel's colour is looked up from a hair outside the skin (the remesh's normals face out): the thin beak's tip would
        # otherwise read the mouth's inside through itself
        out = (ps[1] - ps[0]).cross(ps[2] - ps[0])
        out = out.normalized() * 0.012 if out.length > 1e-12 else Vector((0, 0, 0))
        gx0, gy0 = np.floor(pts.min(0)).astype(int)
        gx1, gy1 = np.ceil(pts.max(0)).astype(int)
        gx0, gy0, gx1, gy1 = max(0, gx0), max(0, gy0), min(w - 1, gx1), min(h - 1, gy1)
        pa, pb, pc = pts
        den = (pb[1] - pc[1]) * (pa[0] - pc[0]) + (pc[0] - pb[0]) * (pa[1] - pc[1])
        if abs(den) < 1e-12:
            continue
        for ty in range(gy0, gy1 + 1):
            for tx in range(gx0, gx1 + 1):
                px, py = tx + 0.5, ty + 0.5
                l1 = ((pb[1] - pc[1]) * (px - pc[0]) + (pc[0] - pb[0]) * (py - pc[1])) / den
                l2 = ((pc[1] - pa[1]) * (px - pc[0]) + (pa[0] - pc[0]) * (py - pc[1])) / den
                l3 = 1 - l1 - l2
                if l1 < -0.02 or l2 < -0.02 or l3 < -0.02:
                    continue
                at = ps[0] * l1 + ps[1] * l2 + ps[2] * l3 + out
                loc, _, idx, _ = tree.find_nearest(at)
                if idx is None:
                    continue
                va, vb, vc = (Vector(verts[i]) for i in tris[idx])
                bw = barycentric_transform(loc, va, vb, vc, e1, e2, e3)
                bw = np.clip(np.array(bw), 0, None)
                bw /= max(1e-9, bw.sum())
                col = bw[0] * cols[idx][0] + bw[1] * cols[idx][1] + bw[2] * cols[idx][2]
                if painted[idx]:
                    uu = bw[0] * uvs[idx][0][0] + bw[1] * uvs[idx][1][0] + bw[2] * uvs[idx][2][0]
                    vv = bw[0] * uvs[idx][0][1] + bw[1] * uvs[idx][1][1] + bw[2] * uvs[idx][2][1]
                    ix = int(min(iw - 1, max(0, round(uu * (iw - 1)))))
                    iy = int(min(ih - 1, max(0, round((1.0 - vv) * (ih - 1)))))
                    al = float(face_alpha[iy, ix])
                    col = col * (1 - al) + face_rgb[iy, ix] * al
                tex[ty, tx] = col
                done[ty, tx] = True
    # grown a few texels past every triangle's edge (no seam shows where the game samples between texels)
    for _ in range(4):
        grow = ~done & (np.roll(done, 1, 0) | np.roll(done, -1, 0) | np.roll(done, 1, 1) | np.roll(done, -1, 1))
        if not grow.any():
            break
        acc = np.zeros_like(tex)
        cnt = np.zeros((h, w), np.float32)
        for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            m = np.roll(done, (dy, dx), (0, 1))
            acc += np.roll(tex, (dy, dx), (0, 1)) * m[..., None]
            cnt += m
        tex[grow] = acc[grow] / np.maximum(1, cnt[grow])[:, None]
        done |= grow
    obj["fused_parent"] = "Head  /  turn"
    obj["head_uv"] = True
    HEAD_TEX.append(tex)
    return obj


MANTLE = "Black cloud  /  continuous open mantle"
# a placeholder u for a flowing vertex until its part's reach is known (inside the white square)
REACH_UV = (WHITE[0] + 0.5) / ATLAS_W


def tree_of(objs):
    """A BVH of the objects' surfaces (world space)."""
    from mathutils.bvhtree import BVHTree
    depsgraph = bpy.context.evaluated_depsgraph_get()
    verts, tris = [], []
    for o in objs:
        ev = o.evaluated_get(depsgraph)
        me = ev.to_mesh()
        me.calc_loop_triangles()
        base = len(verts)
        verts += [tuple(o.matrix_world @ v.co) for v in me.vertices]
        tris += [tuple(base + i for i in lt.vertices) for lt in me.loop_triangles]
        ev.to_mesh_clear()
    return BVHTree.FromPolygons(verts, tris)


def reach_of(name, kind, p, core, mantle):
    """How far a flowing part's point stands off what carries it (units): the black ribbon's wisps off its mantle, everything else off
    the body and the head."""
    tree = mantle if kind == 3 and name != MANTLE else core
    hit = tree.find_nearest(Vector(tuple(p)))
    return 0.0 if hit[0] is None else float(hit[3])


def halo_phase(p, centre):
    """Where round the black ribbon's ring a point is (0 to 1), seen from the front."""
    return (math.atan2(p[2] - centre[2], p[0] - centre[0]) / (2 * math.pi)) % 1.0


def hash_name(name):
    h = 0
    for ch in name.encode("utf-8"):
        h = (h * 31 + ch) & 0x7FFFFFFF
    return h


def curve_as_mesh(o):
    """A bevelled curve as a mesh object (its own name, place, materials and parent) so it is decimated as the meshes are."""
    depsgraph = bpy.context.evaluated_depsgraph_get()
    depsgraph.update()
    mesh = bpy.data.meshes.new_from_object(o.evaluated_get(depsgraph))
    name = o.name
    parent = o.parent.name if o.parent else ""
    world = o.matrix_world.copy()
    o.name = name + "  (curve)"
    m = bpy.data.objects.new(name, mesh)
    bpy.context.scene.collection.objects.link(m)
    m.matrix_world = world
    m["fused_parent"] = parent
    m["was_curve"] = True
    return m


def main():
    ratio = args()
    bpy.ops.wm.open_mainfile(filepath=str(BLEND))
    body = bpy.data.objects[BODY]
    line, ds, body_dist, length = centreline(body)
    rig, chain, wings, legs = build_rig(line, ds, length)
    print(f"centreline {length:.2f} units, {len(rig.names)} bones")
    # the face's image
    img = bpy.data.images[FACE_IMAGE]
    iw, ih = img.size
    face_px = np.array(img.pixels[:], np.float32).reshape(ih, iw, 4)[::-1]  # top row first
    face_rgb = face_px[..., :3]
    if img.colorspace_settings.name != "sRGB":
        face_rgb = srgb(face_rgb)
    face_alpha = face_px[..., 3]
    atlas = np.ones((ATLAS_H, ATLAS_W, 3), np.float32)
    face_uv_tris = {k: [] for k in FACE_REGIONS}  # (uv x3, colour x3) to paint each region's own colours under the image
    head = fused_head(face_rgb, face_alpha)
    table = dorsal_angles(line, ds)
    core = tree_of([bpy.data.objects[BODY], head])
    mantle = tree_of([bpy.data.objects[MANTLE]])
    halo_at = np.array(rig.at[rig.index("halo")])
    print("the fiery mane turned onto the back by", [round(math.degrees(a)) for a in table[1][::5]], "degrees along the tail")
    objects = [o for o in bpy.data.objects if o.type in ("MESH", "CURVE") and not o.name.startswith(HEAD_FUSED)]
    dropped = 0
    keep_objects = []
    for o in objects:
        if o.type == "CURVE":
            # an ink line thinner than INK (units across: under a quarter of a game pixel at the creature's size) is left out
            if o.data.bevel_depth * max(o.matrix_world.to_scale()) < INK:
                dropped += 1
                continue
            o.data.resolution_u = min(o.data.resolution_u, 2)
            o.data.bevel_resolution = 0
            o = curve_as_mesh(o)
        keep_objects.append(o)
    objects = keep_objects
    print(f"{dropped} ink lines thinner than {INK} left out")
    depsgraph = bpy.context.evaluated_depsgraph_get()
    parts = []
    total = 0
    head_names = {"Head  /  turn": "head", "Lower jaw  /  hinge": "jaw"}
    for o in objects:
        tris = sum(len(p.vertices) - 2 for p in o.data.polygons) if o.type == "MESH" else 0
        keep = 1.0
        if o.type == "MESH":
            keep = min(1.0, max(ratio, floor_of(o.name) / max(1, tris)))
            if o.name == BODY:
                keep = max(keep, BODY_TRIS / max(1, tris))
            elif o.name == "Head  /  one skull":
                keep = 1.0  # decimated already, its uvs and texture made on its final faces
            elif "mandible" in o.name:
                keep = max(keep, JAW_TRIS / max(1, tris))
            keep = min(1.0, keep)
            if keep < 1.0:
                dec = o.modifiers.new("bake_decimate", "DECIMATE")
                dec.decimate_type = "COLLAPSE"
                dec.ratio = keep
                dec.use_collapse_triangulate = True
        depsgraph = bpy.context.evaluated_depsgraph_get()
        depsgraph.update()
        ev = o.evaluated_get(depsgraph)
        mesh = ev.to_mesh()
        bm = bmesh.new()
        bm.from_mesh(mesh)
        bmesh.ops.triangulate(bm, faces=bm.faces[:])
        uv_layer = bm.loops.layers.uv.active
        colour_layer = bm.verts.layers.float_color.get("Pigment")
        mats = [s.material for s in o.material_slots] if o.material_slots else list(getattr(o.data, "materials", []))
        kind = kind_of(o.name, mats)
        to_world = o.matrix_world
        normal_world = to_world.to_3x3().inverted().transposed()
        parent = o.get("fused_parent") or (o.parent.name if o.parent else "")
        # ---- each vertex's bones
        wv = np.array([to_world @ v.co for v in bm.verts]) if len(bm.verts) else np.zeros((0, 3))
        weights = [None] * len(bm.verts)
        side = "L" if (wv[:, 0].mean() if len(wv) else 0.0) < 0 else "R"
        if parent in head_names:
            for i in range(len(weights)):
                weights[i] = [(head_names[parent], 1.0)]
        elif parent.startswith("Port wing") or parent.startswith("Starboard wing"):
            names, xs, joints = wings["L" if parent.startswith("Port") else "R"]
            membrane = "patagium" in o.name or "leading edge" in o.name or bool(o.get("was_curve"))
            if membrane:
                for i, p in enumerate(wv):
                    weights[i] = span_weights(names, xs, p[0])
            else:
                # the feather's root: its point nearest the arm (the line of the joints)
                best = None
                for p in wv:
                    for a, b in zip(joints[:-1], joints[1:]):
                        ab = b - a
                        t = float(np.clip(np.dot(p - a, ab) / np.dot(ab, ab), 0, 1))
                        q = a + ab * t
                        dd = float(np.dot(p - q, p - q))
                        if best is None or dd < best[0]:
                            best = (dd, q[0])
                w = span_weights(names, xs, best[1])
                for i in range(len(weights)):
                    weights[i] = w
        elif "halo" == parent or kind == 3:
            for i in range(len(weights)):
                weights[i] = [("halo", 1.0)]
        elif o.name.startswith(("L  /", "R  /")) and ("plumage" in o.name or "cuff" in o.name):
            for i in range(len(weights)):
                weights[i] = [(f"hip{o.name[0]}", 1.0)]
        elif o.name.startswith(("L  /", "R  /")) and "tarsometatarsus" in o.name:
            for i in range(len(weights)):
                weights[i] = [(f"ankle{o.name[0]}", 1.0)]
        elif o.name.startswith(("L  /", "R  /")) and any(k in o.name for k in ("digit", "hallux", "talon", "toe pad")):
            for i in range(len(weights)):
                weights[i] = [(f"toes{o.name[0]}", 1.0)]
        elif any(m and "tarsal scutes" in m.name for m in mats):
            for i in range(len(weights)):
                weights[i] = [(f"ankle{side}", 1.0)]
        elif o.name == BODY:
            # each vertex by its own distance along the surface (the decimated vertex: its nearest original's)
            src = world_verts(body)
            for i, p in enumerate(wv):
                j = int(np.argmin(np.einsum("ij,ij->i", src - p, src - p)))
                weights[i] = chain_weights(chain, float(body_dist[j]))
        else:
            rigid = "plume" in o.name or "Dorsal flick" in o.name or "lappet" in o.name or "rising lick" in o.name or "inset" in o.name
            if rigid and len(wv):
                # rigid where it sits on the body: its point nearest the centreline
                dmin = None
                for p in wv[:: max(1, len(wv) // 40)]:
                    d = nearest_on_line(line, ds, p)
                    q = point_at(line, ds, d)
                    e = float(np.dot(p - q, p - q))
                    if dmin is None or e < dmin[0]:
                        dmin = (e, d)
                w = chain_weights(chain, dmin[1])
                for i in range(len(weights)):
                    weights[i] = w
            else:
                for i, p in enumerate(wv):
                    weights[i] = chain_weights(chain, nearest_on_line(line, ds, p))
        # ---- the vertices
        verts = []
        vmap = {}
        raw = {}
        flowing = []
        part_phase = (hash_name(o.name) % 997) / 997.0
        # the fiery mane turned onto the tail's back: the fin per vertex, each flick whole about its root
        turned = {} if o.name.startswith(DORSAL) else None
        root = None
        if turned is not None and o.name != DORSAL[0] and len(wv):
            root = min((nearest_on_line(line, ds, q) for q in wv[:: max(1, len(wv) // 12)]),
                       key=lambda d: float(np.linalg.norm(point_at(line, ds, d) - wv.mean(0))))
        quads = []
        bm.verts.ensure_lookup_table()
        for f in bm.faces:
            mat = mats[f.material_index] if f.material_index < len(mats) else None
            fk = face_key(mat)
            if fk not in FACE_REGIONS:
                fk = None
            base = material_base(mat)
            corner = []
            tri_uv = []
            tri_col = []
            for loop in f.loops:
                v = loop.vert
                col = np.array(v[colour_layer][:3]) if colour_layer is not None else base
                if fk == "35b":
                    col = np.array([0.017, 0.014, 0.025])
                if o.get("head_uv") and uv_layer is not None:
                    u, vv = loop[uv_layer].uv
                    x, y, w, h = HEAD_REGION
                    uu = (x + u * (w - 1) + 0.5) / ATLAS_W
                    vt = (y + vv * (h - 1) + 0.5) / ATLAS_H
                    rgb = (1.0, 1.0, 1.0)
                elif fk is not None and uv_layer is not None:
                    u, vv = loop[uv_layer].uv
                    x, y, w, h = FACE_REGIONS[fk]
                    uu = (x + min(max(u, 0.0), 1.0) * (w - 1) + 0.5) / ATLAS_W
                    vt = (y + (1.0 - min(max(vv, 0.0), 1.0)) * (h - 1) + 0.5) / ATLAS_H
                    tri_uv.append((min(max(u, 0.0), 1.0), 1.0 - min(max(vv, 0.0), 1.0)))
                    tri_col.append(srgb(col))
                    rgb = (1.0, 1.0, 1.0)
                else:
                    uu, vt = (WHITE[0] + WHITE[2] / 2) / ATLAS_W, (WHITE[1] + WHITE[3] / 2) / ATLAS_H
                    rgb = tuple(float(c) for c in srgb(col))
                wn = normal_world @ (v.normal if f.smooth else f.normal)
                if turned is not None:
                    if v.index not in turned:
                        w0 = np.array(to_world @ v.co)
                        turned[v.index] = dorsal_turn(line, ds, table, w0, root)
                    wp, rot = turned[v.index]
                    p = C @ Vector(tuple(wp))
                    n = (C.to_3x3() @ Vector(tuple(rot @ np.array(wn)))).normalized()
                    world_p = wp
                else:
                    p = C @ (to_world @ v.co)
                    n = (C.to_3x3() @ wn).normalized()
                    world_p = np.array(to_world @ v.co)
                if kind in (2, 3, 4) and fk is None:
                    # 3.9.4: a flowing part's vertex carries, inside the white square, how far out it reaches (u: 0 at its root on
                    # the body, 1 at its tips: itsumade/ItsuForm.flame moves the tips most) and its phase (v: round the black ribbon's
                    # ring for the ribbon, the part's own for the rest), read back by ItsuForm
                    raw[v.index] = reach_of(o.name, kind, world_p, core, mantle)
                    ph = halo_phase(world_p, halo_at) if kind == 3 else part_phase
                    uu, vt = REACH_UV, (WHITE[1] + 0.5 + 7.0 * ph) / ATLAS_H
                bw = weights[v.index]
                key = (round(p.x, 4), round(p.y, 4), round(p.z, 4), round(uu, 5), round(vt, 5), round(n.x, 2), round(n.y, 2),
                       round(n.z, 2), tuple(int(round(c * 255)) for c in rgb), tuple((b, round(w, 3)) for b, w in bw))
                if key not in vmap:
                    vmap[key] = len(verts)
                    verts.append((p, (uu, vt), n, rgb, bw))
                corner.append(vmap[key])
                if v.index in raw:
                    flowing.append((v.index, vmap[key]))
            if fk is not None and len(tri_uv) == 3:
                face_uv_tris[fk].append((tri_uv, tri_col))
            quads.append((corner[0], corner[1], corner[2], corner[2]))
        bm.free()
        ev.to_mesh_clear()
        if o.type == "MESH" and o.modifiers.get("bake_decimate"):
            o.modifiers.remove(o.modifiers["bake_decimate"])
        if raw:
            lo = min(raw.values())
            hi = max(raw.values())
            for vi, k in flowing:
                r = 0.0 if hi - lo < 1e-6 else (raw[vi] - lo) / (hi - lo)
                pv = verts[k]
                verts[k] = (pv[0], ((WHITE[0] + 0.5 + 7.0 * r) / ATLAS_W, pv[1][1]), pv[2], pv[3], pv[4])
        if not quads:
            continue
        if len(verts) > 65535:
            raise SystemExit(o.name + ": too many vertices")
        total += len(quads)
        parts.append((o.name, kind, verts, quads))
    # ---- the face's regions: the parts' own colours (painted in the uv layout), the image over them by its alpha
    from PIL import Image
    for fk, (x, y, w, h) in FACE_REGIONS.items():
        under = np.zeros((h, w, 3), np.float32)
        filled = np.zeros((h, w), bool)
        for uvs, cols in face_uv_tris[fk]:
            pts = np.array(uvs) * [w - 1, h - 1]
            x0, y0 = np.floor(pts.min(0)).astype(int)
            x1, y1 = np.ceil(pts.max(0)).astype(int)
            x0, y0 = max(0, x0), max(0, y0)
            x1, y1 = min(w - 1, x1), min(h - 1, y1)
            if x1 < x0 or y1 < y0:
                continue
            gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
            a, b, c = pts
            den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
            if abs(den) < 1e-9:
                continue
            l1 = ((b[1] - c[1]) * (gx - c[0]) + (c[0] - b[0]) * (gy - c[1])) / den
            l2 = ((c[1] - a[1]) * (gx - c[0]) + (a[0] - c[0]) * (gy - c[1])) / den
            l3 = 1 - l1 - l2
            inside = (l1 >= -0.02) & (l2 >= -0.02) & (l3 >= -0.02)
            colour = l1[..., None] * cols[0] + l2[..., None] * cols[1] + l3[..., None] * cols[2]
            sub = under[y0:y1 + 1, x0:x1 + 1]
            sub[inside] = colour[inside]
            filled[y0:y1 + 1, x0:x1 + 1] |= inside
        if filled.any():
            mean = under[filled].mean(0)
            under[~filled] = mean
        pic = np.asarray(Image.fromarray((face_rgb * 255).astype(np.uint8)).resize((w, h), Image.LANCZOS), np.float32) / 255
        alpha = np.asarray(Image.fromarray((face_alpha * 255).astype(np.uint8)).resize((w, h), Image.LANCZOS), np.float32) / 255
        atlas[y:y + h, x:x + w] = under * (1 - alpha[..., None]) + pic * alpha[..., None]
    if HEAD_TEX:
        x, y, w, h = HEAD_REGION
        atlas[y:y + h, x:x + w] = HEAD_TEX[0]
    out_png = io.BytesIO()
    Image.fromarray((np.clip(atlas, 0, 1) * 255).astype(np.uint8)).save(out_png, "PNG", optimize=True)
    # ---- write (version 2: a colour a vertex)
    index = {nm: i for i, nm in enumerate(rig.names)}
    out = io.BytesIO()
    w = out.write
    w(b"SRY1")
    w(struct.pack(">i", 2))
    w(struct.pack(">i", len(rig.names)))
    game_at = [C @ Vector(tuple(a)) for a in rig.at]
    for i, nm in enumerate(rig.names):
        pi = rig.parent[i]
        t = game_at[i] - (game_at[pi] if pi >= 0 else Vector((0, 0, 0)))
        nb = nm.encode("utf-8")
        w(struct.pack(">H", len(nb)) + nb)
        w(struct.pack(">h", pi))
        w(struct.pack(">3f", t.x, t.y, t.z))
        w(struct.pack(">4f", 0.0, 0.0, 0.0, 1.0))
        w(struct.pack(">3f", 1.0, 1.0, 1.0))
        g = game_at[i]
        for r, row in enumerate(((1, 0, 0, -g.x), (0, 1, 0, -g.y), (0, 0, 1, -g.z))):
            w(struct.pack(">4f", *row))
    w(struct.pack(">i", len(parts)))
    for name, kind, verts, quads in parts:
        nb = name.encode("utf-8")
        w(struct.pack(">H", len(nb)) + nb)
        w(struct.pack(">b", kind))
        w(struct.pack(">ii", len(verts), len(quads)))
        for p, (u, v), n, rgb, bw in verts:
            for c in (p.x, p.y, p.z):
                w(struct.pack(">h", int(round(max(-1.0, min(1.0, c / EXTENT)) * 32767))))
            w(struct.pack(">HH", int(round(min(max(u, 0.0), 1.0) * 65535)), int(round(min(max(v, 0.0), 1.0) * 65535))))
            w(struct.pack(">bbb", *(int(round(max(-1.0, min(1.0, c)) * 127)) for c in (n.x, n.y, n.z))))
            bw = sorted(bw, key=lambda kv: -kv[1])[:4]
            tot = sum(x[1] for x in bw)
            ids = [index[b] for b, _ in bw] + [0] * (4 - len(bw))
            ws = [int(round(255 * x[1] / tot)) for x in bw] + [0] * (4 - len(bw))
            ws[0] += 255 - sum(ws)
            w(struct.pack(">BBBB", *ids))
            w(struct.pack(">BBBB", *ws))
            w(struct.pack(">BBB", *(int(round(min(max(c, 0.0), 1.0) * 255)) for c in rgb)))
        for q in quads:
            w(struct.pack(">4H", *q))
    w(struct.pack(">i", 0))
    OUT_MODEL.parent.mkdir(parents=True, exist_ok=True)
    OUT_MODEL.write_bytes(gzip.compress(out.getvalue(), 9, mtime=0))
    OUT_TEXTURE.parent.mkdir(parents=True, exist_ok=True)
    OUT_TEXTURE.write_bytes(out_png.getvalue())
    for name, kind, verts, quads in sorted(parts, key=lambda pt: -len(pt[3]))[:12]:
        print(f"  {len(quads):6d} {name}")
    groups = {}
    for name, kind, verts, quads in parts:
        key = re.sub(r"[0-9]+", "#", name.split("  •  ")[0])
        groups[key] = groups.get(key, 0) + len(quads)
    for key, n in sorted(groups.items(), key=lambda kv: -kv[1])[:30]:
        print(f"  group {n:6d} {key}")
    kinds = {}
    for name, kind, verts, quads in parts:
        kinds[kind] = kinds.get(kind, 0) + len(quads)
    print(f"itsumade: {len(parts)} parts, {total} faces (by kind {kinds}), {len(rig.names)} bones")
    for i, nm in enumerate(rig.names):
        g = game_at[i]
        print(f"  bone {i:2d} {nm:10s} parent {rig.parent[i]:3d} at ({g.x:.3f}, {g.y:.3f}, {g.z:.3f})")


if __name__ == "__main__":
    main()
