#!/usr/bin/env python3
"""3.6.7 (the owner: "Mythical Fruit: Tori Tori no Mi, Model: Phoenix ... I made the model and sent it in the zip, first of all animate it
so it looks good"): the owner's phoenix (input/marco_phoenix.zip: Marco_Phoenix.blend, Blender 4.3) baked for the game in the dragon's own
model format (sea/DragonModel: tools/dragon_mesh.py writes the dragon's), read by phoenix/PhoenixForm and client/phoenix/PhoenixRenderer.

Run it with Blender's own Python module (CI does not need it: its outputs are committed):
    python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy     (bpy 5.3, Python 3.13)
    /tmp/bpy/bin/python tools/phoenix_bake.py [--ratio 0.065]
It writes src/main/resources/assets/hexhaki/phoenix/marco.sry and src/main/resources/assets/hexhaki/textures/piece/phoenix/marco.png.

What it keeps of the owner's file, exactly: every one of the 213 parts in its own place and colour, each skinned to its bone of the
21-bone rig, and the take "Marco | soaring wingbeat" (73 frames at 30 a second). What it changes for the phone: each part decimated
(collapse; at least FLOOR triangles a part) from about 175k triangles to about 11.5k (the dragon is about 10k). Its look: every material of the file is unlit, an
emission whose colour is its base colour (or its fire texture) times a colour ramp of how much the surface faces one fixed light
(dot(normal, LIGHT) * 0.5 + 0.5); the two ramps of the file (cool: plumage, bill, eyes; warm: tail, mane, wings, fire) are written into
the model's part kinds (0 the cool ramp, 2 the warm one, which also ripples like flame in the game) and PhoenixForm keeps their stops.

The model's space: Blender's (x, y, z) to the game's (-x, z, y): +Y up, the head toward -Z (as the dragon's), the left wing toward -X.
Units stay the file's (PhoenixForm.SCALE makes them blocks). The texture: one 1024 x 1024 atlas: the wings' fire (Tidal_fire), the
breast's (Body_revival_fire), the flame tongues' (Flying_tongues) and a palette of every flat colour, each part's UVs moved into its
region.
"""
import gzip
import io
import math
import struct
import sys
import zipfile
from pathlib import Path

import bpy
import bmesh
from mathutils import Matrix, Vector

ROOT = Path(__file__).resolve().parent.parent
ZIP = ROOT / "input" / "marco_phoenix.zip"
OUT_MODEL = ROOT / "src/main/resources/assets/hexhaki/phoenix/marco.sry"
OUT_TEXTURE = ROOT / "src/main/resources/assets/hexhaki/textures/piece/phoenix/marco.png"
EXTENT = 128.0
FLOOR = 16
TAKE = "Marco | soaring wingbeat"
ATLAS = 1024
# the atlas regions (x, y, w, h in pixels) of the file's three images; the flat colours go in PALETTE's cells
REGIONS = {"Tidal_fire.png": (0, 0, 1024, 512), "Body_revival_fire.png": (0, 512, 768, 512), "Flying_tongues.png": (768, 512, 256, 88)}
PALETTE = (768, 600, 256, 424)
CELL = 16
# Blender to the game: (x, y, z) -> (-x, z, y)
C = Matrix(((-1, 0, 0, 0), (0, 0, 1, 0), (0, 1, 0, 0), (0, 0, 0, 1)))
C_INV = C.inverted()


def args():
    ratio = 0.065
    if "--ratio" in sys.argv:
        ratio = float(sys.argv[sys.argv.index("--ratio") + 1])
    return ratio


def open_blend():
    work = Path("/tmp/phoenix_bake")
    work.mkdir(exist_ok=True)
    with zipfile.ZipFile(ZIP) as z:
        name = next(n for n in z.namelist() if n.endswith(".blend"))
        target = work / Path(name).name
        target.write_bytes(z.read(name))
    bpy.ops.wm.open_mainfile(filepath=str(target))


def material_look(mat):
    """(base rgb (linear) or None if textured, the texture's name or None, the ramp's stops [(pos, rgb)])."""
    nodes = mat.node_tree.nodes
    image = None
    base = None
    ramp = None
    for n in nodes:
        if n.type == "TEX_IMAGE" and n.image:
            image = n.image.name
        if n.type == "MIX_RGB":
            c1 = n.inputs[1]
            if not c1.is_linked:
                base = tuple(c1.default_value[:3])
        if n.type == "VALTORGB":
            ramp = [(e.position, tuple(e.color[:3])) for e in n.color_ramp.elements]
    return base, image, ramp


def srgb(c):
    c = max(0.0, min(1.0, c))
    return c * 12.92 if c <= 0.0031308 else 1.055 * c ** (1 / 2.4) - 0.055


def build_atlas(colours):
    """The atlas image (bytes, PNG) and each flat colour's cell centre (u, v)."""
    from PIL import Image
    atlas = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 255))
    for name, (x, y, w, h) in REGIONS.items():
        img = bpy.data.images[name]
        px = list(img.pixels)
        iw, ih = img.size
        src = Image.new("RGBA", (iw, ih))
        data = []
        for j in range(ih):
            row = (ih - 1 - j) * iw * 4  # Blender's rows start at the bottom
            for i in range(iw):
                k = row + i * 4
                data.append(tuple(int(round(255 * (px[k + c] if img.colorspace_settings.name == "Non-Color" else px[k + c]))) for c in range(3)) + (255,))
        src.putdata(data)
        atlas.paste(src.resize((w, h), Image.LANCZOS), (x, y))
    cells = {}
    px0, py0, pw, ph = PALETTE
    per_row = pw // CELL
    for i, (key, rgb) in enumerate(colours.items()):
        cx = px0 + (i % per_row) * CELL
        cy = py0 + (i // per_row) * CELL
        c = tuple(int(round(255 * srgb(v))) for v in rgb)
        for j in range(CELL):
            for k in range(CELL):
                atlas.putpixel((cx + k, cy + j), c + (255,))
        cells[key] = ((cx + CELL / 2) / ATLAS, (cy + CELL / 2) / ATLAS)
    out = io.BytesIO()
    atlas.save(out, "PNG", optimize=True)
    return out.getvalue(), cells


def decompose(m):
    t, q, s = m.decompose()
    return t, q, s


def main():
    ratio = args()
    open_blend()
    rig = next(o for o in bpy.data.objects if o.type == "ARMATURE")
    rig.data.pose_position = "REST"
    bpy.context.view_layer.update()
    bones = list(rig.data.bones)
    index = {b.name: i for i, b in enumerate(bones)}
    W = rig.matrix_world

    def game(m):
        return C @ W @ m @ C_INV

    rest_global = [game(b.matrix_local) for b in bones]
    # ---- the parts
    meshes = [o for o in bpy.data.objects if o.type == "MESH"]
    looks = {}
    ramps = []
    for o in meshes:
        for s in o.material_slots:
            if s.material and s.material.name not in looks:
                looks[s.material.name] = material_look(s.material)
    for name, (_, _, ramp) in looks.items():
        if ramp and ramp not in ramps:
            ramps.append(ramp)
    flats = {name: look[0] for name, look in looks.items() if look[1] is None and look[0] is not None}
    atlas_png, cells = build_atlas(flats)
    depsgraph = bpy.context.evaluated_depsgraph_get()
    parts = []
    total = 0
    for o in meshes:
        tris = sum(len(p.vertices) - 2 for p in o.data.polygons)
        keep = min(1.0, max(ratio, FLOOR / max(1, tris)))
        if keep < 1.0:
            dec = o.modifiers.new("bake_decimate", "DECIMATE")
            dec.decimate_type = "COLLAPSE"
            dec.ratio = keep
            dec.use_collapse_triangulate = True
        depsgraph = bpy.context.evaluated_depsgraph_get()
        ev = o.evaluated_get(depsgraph)
        mesh = ev.to_mesh()
        bm = bmesh.new()
        bm.from_mesh(mesh)
        bmesh.ops.triangulate(bm, faces=bm.faces[:])
        uv_layer = bm.loops.layers.uv.active
        # the bone of each vertex group (the file's parts are each weighted to one bone)
        group_bone = {}
        for g in o.vertex_groups:
            if g.name in index:
                group_bone[g.index] = index[g.name]
        deform = bm.verts.layers.deform.active
        to_world = o.matrix_world
        normal_world = to_world.to_3x3().inverted().transposed()
        mat_names = [s.material.name if s.material else None for s in o.material_slots]
        kind = 0
        warm = 0
        for mn in mat_names:
            if mn and looks[mn][2] and ramps.index(looks[mn][2]) == 1:
                warm += 1
        kind = 2 if warm * 2 >= len([m for m in mat_names if m]) and warm > 0 else 0
        verts = []
        vmap = {}
        quads = []
        for f in bm.faces:
            mn = mat_names[f.material_index] if f.material_index < len(mat_names) else None
            base, image, _ = looks.get(mn, (None, None, None)) if mn else ((0.5, 0.5, 0.5), None, None)
            corner = []
            for loop in f.loops:
                v = loop.vert
                if image:
                    u, vv = loop[uv_layer].uv if uv_layer else (0.5, 0.5)
                    x, y, w, h = REGIONS[image]
                    uu = (x + min(max(u, 0.0), 1.0) * (w - 1) + 0.5) / ATLAS
                    vt = (y + (1.0 - min(max(vv, 0.0), 1.0)) * (h - 1) + 0.5) / ATLAS
                else:
                    uu, vt = cells.get(mn, (0.5, 0.5))
                p = C @ (to_world @ v.co)
                n = (C.to_3x3() @ (normal_world @ (v.normal if not f.smooth else v.normal))).normalized()
                if not f.smooth:
                    n = (C.to_3x3() @ (normal_world @ f.normal)).normalized()
                bone = 0
                if deform is not None and v[deform]:
                    best = max(v[deform].items(), key=lambda kv: kv[1])
                    bone = group_bone.get(best[0], 0)
                key = (round(p.x, 4), round(p.y, 4), round(p.z, 4), round(uu, 5), round(vt, 5), round(n.x, 2), round(n.y, 2), round(n.z, 2), bone)
                if key not in vmap:
                    vmap[key] = len(verts)
                    verts.append((p, (uu, vt), n, bone))
                corner.append(vmap[key])
            quads.append((corner[0], corner[1], corner[2], corner[2]))
        bm.free()
        ev.to_mesh_clear()
        if o.modifiers.get("bake_decimate"):
            o.modifiers.remove(o.modifiers["bake_decimate"])
        if len(verts) > 65535:
            raise SystemExit(o.name + ": too many vertices")
        total += len(quads)
        parts.append((o.name, kind, verts, quads))
    # ---- the take: every bone's local transform (the game's space), frame by frame
    rig.data.pose_position = "POSE"
    action = bpy.data.actions[TAKE]
    rig.animation_data.action = action
    scene = bpy.context.scene
    first, last = int(action.frame_range[0]), int(action.frame_range[1])
    frames = []
    for f in range(first, last + 1):
        scene.frame_set(f)
        globals_ = [game(pb.matrix) for pb in rig.pose.bones]
        locals_ = []
        for i, b in enumerate(bones):
            pi = index[b.parent.name] if b.parent else -1
            local = globals_[i] if pi < 0 else globals_[pi].inverted() @ globals_[i]
            locals_.append(decompose(local))
        frames.append(locals_)
    rig.data.pose_position = "REST"
    # ---- write
    out = io.BytesIO()
    w = out.write
    w(b"SRY1")
    w(struct.pack(">i", 1))
    w(struct.pack(">i", len(bones)))
    for i, b in enumerate(bones):
        pi = index[b.parent.name] if b.parent else -1
        local = rest_global[i] if pi < 0 else rest_global[pi].inverted() @ rest_global[i]
        t, q, s = decompose(local)
        name = b.name.encode("utf-8")
        w(struct.pack(">H", len(name)) + name)
        w(struct.pack(">h", pi))
        w(struct.pack(">3f", t.x, t.y, t.z))
        w(struct.pack(">4f", q.x, q.y, q.z, q.w))
        w(struct.pack(">3f", s.x, s.y, s.z))
        inv = rest_global[i].inverted()
        for r in range(3):
            w(struct.pack(">4f", inv[r][0], inv[r][1], inv[r][2], inv[r][3]))
    w(struct.pack(">i", len(parts)))
    for name, kind, verts, quads in parts:
        nb = name.encode("utf-8")
        w(struct.pack(">H", len(nb)) + nb)
        w(struct.pack(">b", kind))
        w(struct.pack(">ii", len(verts), len(quads)))
        for p, (u, v), n, bone in verts:
            for c in (p.x, p.y, p.z):
                w(struct.pack(">h", int(round(max(-1.0, min(1.0, c / EXTENT)) * 32767))))
            w(struct.pack(">HH", int(round(min(max(u, 0.0), 1.0) * 65535)), int(round(min(max(v, 0.0), 1.0) * 65535))))
            w(struct.pack(">bbb", *(int(round(max(-1.0, min(1.0, c)) * 127)) for c in (n.x, n.y, n.z))))
            w(struct.pack(">BBBB", bone, 0, 0, 0))
            w(struct.pack(">BBBB", 255, 0, 0, 0))
        for q in quads:
            w(struct.pack(">4H", *q))
    w(struct.pack(">i", 1))
    cn = "soar".encode("utf-8")
    w(struct.pack(">H", len(cn)) + cn)
    w(struct.pack(">f", float(scene.render.fps)))
    w(struct.pack(">i", len(frames)))
    w(struct.pack(">i", len(bones) * 2))
    for i in range(len(bones)):
        w(struct.pack(">hb", i, 0))
        for fr in frames:
            q = fr[i][1]
            for c in (q.x, q.y, q.z, q.w):
                w(struct.pack(">h", int(round(max(-1.0, min(1.0, c)) * 32767))))
        w(struct.pack(">hb", i, 1))
        for fr in frames:
            t = fr[i][0]
            w(struct.pack(">3f", t.x, t.y, t.z))
    OUT_MODEL.parent.mkdir(parents=True, exist_ok=True)
    OUT_MODEL.write_bytes(gzip.compress(out.getvalue(), 9, mtime=0))
    OUT_TEXTURE.parent.mkdir(parents=True, exist_ok=True)
    OUT_TEXTURE.write_bytes(atlas_png)
    print(f"phoenix: {len(parts)} parts, {total} faces, {len(bones)} bones, take {len(frames)} frames; ramps {len(ramps)}")
    for k, ramp in enumerate(ramps):
        print("  ramp", k, [(round(p, 3), tuple(round(c, 3) for c in rgb)) for p, rgb in ramp])
    print("  light (game space)", tuple(round(c, 4) for c in (C.to_3x3() @ Vector((-0.4294421076774597, -0.5592734217643738, 0.7090787887573242)))))


if __name__ == "__main__":
    main()
