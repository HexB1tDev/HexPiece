#!/usr/bin/env python3
"""3.7.9 (the owner: "New Fruit: Legendary Leopard Fruit, Name: Neko Neko No Mi, Type: Zoan, Model in the Zip, Size: 5 blocks tall"):
the owner's Lucci leopard (input/neko/lucci/pl_lucci_eggh02 (merge).fbx, unpacked unchanged from the owner's zip) baked for the game in
the dragon's own model format (dragon/DragonModel, .sry), read by neko/LeopardForm and client/neko/LeopardFx.

Run it with Blender's own Python module (CI does not need it: its outputs are committed):
    python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy     (bpy 5.3, Python 3.13)
    /tmp/bpy/bin/python tools/leopard_bake.py
It writes src/main/resources/assets/hexhaki/neko/lucci.sry and src/main/resources/assets/hexhaki/textures/piece/neko/lucci.png (the
file's own diffuse texture, copied unchanged).

Blender 5.3's FBX importer stops on this file ("KeyError: pl_lucci_eggh02" in link_hierarchy: the meshes are bound under the helper
node that holds the rig, not under the rig itself). This script patches that one line of the venv's own copy of the importer (the
mesh's bind matrices taken from whichever rig it was bound to) before importing; the owner's file is never changed.

What it keeps of the owner's file, exactly: every one of its 22 meshes (the body; the mane, beard, elbow hair, shoulder hair and tail hair
of the "trans_hair" material; the ribbon "hagoromo"; the four faces and the four hand poses each side, shown one at a time by the
game), each vertex skinned to up to four of the 69 bones with its own weights, the file's UVs on its one texture, and its takes (25 a
second): idle_a, run, jump, jump_lp, jump_end and every other take of the game's (combo, skill, dodge, damage...). Nothing is decimated
(about 11.7k triangles are seen at once: one face and one hand pose each side).

The model's space: Blender's (x, y, z) to the game's (-x, z, y): +Y up, the face toward -Z (as the dragon's and the phoenix's), the left
arm toward -X. Units: the file's centimetres as hundredths (the model about 2.3 units tall); neko/LeopardForm.SCALE makes it 5 blocks.
Part kinds: 0 the skin and clothes (lit, textured), 2 the black flames (the mane and body hair: "trans_hair"), 3 the ribbon
("trans_hagoromo": "the black ribbon around the leopard is black flames as well").
"""
import gzip
import io
import shutil
import struct
import sys
from pathlib import Path

import bpy
from mathutils import Matrix

ROOT = Path(__file__).resolve().parent.parent
FBX = ROOT / "input" / "neko" / "lucci" / "pl_lucci_eggh02 (merge).fbx"
TEXTURE = ROOT / "input" / "neko" / "lucci" / "pl_lucci_eggh02_diff.png"
OUT_MODEL = ROOT / "src/main/resources/assets/hexhaki/neko/lucci.sry"
OUT_TEXTURE = ROOT / "src/main/resources/assets/hexhaki/textures/piece/neko/lucci.png"
EXTENT = 128.0
UNITS = 100.0
PREFIX = "pl_lucci_eggh02_"
C = Matrix(((-1, 0, 0, 0), (0, 0, 1, 0), (0, 1, 0, 0), (0, 0, 0, 1)))
C_INV = C.inverted()


def patch_importer():
    import io_scene_fbx.import_fbx as fbx
    path = Path(fbx.__file__)
    text = path.read_text()
    old = "(mmat, amat) = mesh.armature_setup[self]"
    new = "(mmat, amat) = mesh.armature_setup.get(self) or next(iter(mesh.armature_setup.values()))"
    if old in text:
        path.write_text(text.replace(old, new))
        raise SystemExit("patched Blender's FBX importer (" + str(path) + "): run again")


def kind_of(material):
    if material is None:
        return 0
    if material.endswith("trans_hagoromo"):
        return 3
    if material.endswith("trans_hair"):
        return 2
    return 0


def main():
    patch_importer()
    bpy.ops.wm.read_factory_settings(use_empty=True)
    work = Path("/tmp/leopard_bake")
    work.mkdir(exist_ok=True)
    src = work / "lucci.fbx"
    shutil.copyfile(FBX, src)
    bpy.ops.import_scene.fbx(filepath=str(src))
    rig = next(o for o in bpy.data.objects if o.type == "ARMATURE")
    rig.data.pose_position = "REST"
    bpy.context.view_layer.update()
    bones = list(rig.data.bones)
    if len(bones) > 127:
        raise SystemExit("too many bones for a byte")
    index = {b.name: i for i, b in enumerate(bones)}
    W = rig.matrix_world

    def game(m):
        """A bone's armature-space matrix as the game's rigid one: its rotation, its place in hundredths."""
        g = C @ W @ m @ C_INV
        t, q, s = g.decompose()
        return Matrix.Translation(t * UNITS) @ q.to_matrix().to_4x4()

    rest_global = [game(b.matrix_local) for b in bones]
    # ---- the parts (the bind pose: the rig at rest)
    depsgraph = bpy.context.evaluated_depsgraph_get()
    meshes = sorted((o for o in bpy.data.objects if o.type == "MESH"), key=lambda o: o.name)
    parts = []
    total = 0
    lo_y, hi_y = 1e9, -1e9
    for o in meshes:
        ev = o.evaluated_get(depsgraph)
        mesh = ev.to_mesh()
        mesh.calc_loop_triangles()
        uv = mesh.uv_layers.active.data if mesh.uv_layers.active else None
        normals = mesh.corner_normals
        to_world = ev.matrix_world
        nrm_world = to_world.to_3x3().inverted().transposed()
        groups = {g.index: index.get(g.name, -1) for g in o.vertex_groups}
        material = o.material_slots[0].material.name if o.material_slots and o.material_slots[0].material else None
        kind = kind_of(material)
        verts = []
        vmap = {}
        quads = []
        orig = o.data.vertices
        for tri in mesh.loop_triangles:
            corner = []
            for li in tri.loops:
                vi = mesh.loops[li].vertex_index
                p = C @ (to_world @ mesh.vertices[vi].co) * UNITS
                n = (C.to_3x3() @ (nrm_world @ normals[li].vector)).normalized()
                u, v = (uv[li].uv if uv else (0.5, 0.5))
                ws = sorted(((g.weight, groups.get(g.group, -1)) for g in orig[vi].groups if groups.get(g.group, -1) >= 0), reverse=True)[:4]
                total_w = sum(w for w, _ in ws)
                if total_w <= 0:
                    ws = [(1.0, 0)]
                    total_w = 1.0
                ws = [(w / total_w, b) for w, b in ws]
                key = (round(p.x, 4), round(p.y, 4), round(p.z, 4), round(u, 5), round(v, 5), round(n.x, 2), round(n.y, 2), round(n.z, 2))
                if key not in vmap:
                    vmap[key] = len(verts)
                    verts.append((p, (u, 1.0 - v), n, ws))
                    lo_y = min(lo_y, p.y)
                    hi_y = max(hi_y, p.y)
                corner.append(vmap[key])
            quads.append((corner[0], corner[1], corner[2], corner[2]))
        ev.to_mesh_clear()
        if len(verts) > 65535:
            raise SystemExit(o.name + ": too many vertices")
        total += len(quads)
        parts.append((o.name, kind, verts, quads))
    # ---- the takes: every bone's local transform (the game's space), frame by frame at the file's rate
    rig.data.pose_position = "POSE"
    scene = bpy.context.scene
    fps = scene.render.fps / scene.render.fps_base
    takes = []
    rig.animation_data_create()
    for action in sorted(bpy.data.actions, key=lambda a: a.name):
        if not action.name.startswith(rig.name + "|"):
            continue
        name = action.name.split("|")[1]
        if name.startswith(PREFIX):
            name = name[len(PREFIX):]
        rig.animation_data.action = action
        if hasattr(rig.animation_data, "action_slot") and action.slots:
            rig.animation_data.action_slot = action.slots[0]
        first, last = action.frame_range
        frames = []
        f = first
        while f <= last + 1e-4:
            scene.frame_set(int(f), subframe=f - int(f))
            globals_ = [game(pb.matrix) for pb in rig.pose.bones]
            locals_ = []
            for i, b in enumerate(bones):
                pi = index[b.parent.name] if b.parent else -1
                local = globals_[i] if pi < 0 else globals_[pi].inverted() @ globals_[i]
                t, q, s = local.decompose()
                locals_.append((t, q))
            frames.append(locals_)
            f += 1.0
        takes.append((name, frames))
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
        t, q, s = local.decompose()
        name = b.name.encode("utf-8")
        w(struct.pack(">H", len(name)) + name)
        w(struct.pack(">h", pi))
        w(struct.pack(">3f", t.x, t.y, t.z))
        w(struct.pack(">4f", q.x, q.y, q.z, q.w))
        w(struct.pack(">3f", 1.0, 1.0, 1.0))
        inv = rest_global[i].inverted()
        for r in range(3):
            w(struct.pack(">4f", inv[r][0], inv[r][1], inv[r][2], inv[r][3]))
    w(struct.pack(">i", len(parts)))
    for name, kind, verts, quads in parts:
        nb = name.encode("utf-8")
        w(struct.pack(">H", len(nb)) + nb)
        w(struct.pack(">b", kind))
        w(struct.pack(">ii", len(verts), len(quads)))
        for p, (u, v), n, ws in verts:
            for c in (p.x, p.y, p.z):
                w(struct.pack(">h", int(round(max(-1.0, min(1.0, c / EXTENT)) * 32767))))
            w(struct.pack(">HH", int(round(min(max(u, 0.0), 1.0) * 65535)), int(round(min(max(v, 0.0), 1.0) * 65535))))
            w(struct.pack(">bbb", *(int(round(max(-1.0, min(1.0, c)) * 127)) for c in (n.x, n.y, n.z))))
            bw = (ws + [(0.0, 0)] * 4)[:4]
            # the weights in 255ths summing to 255 exactly (the largest takes the rounding)
            q = [int(round(x * 255)) for x, _ in bw]
            q[0] += 255 - sum(q)
            w(struct.pack(">BBBB", *(b for _, b in bw)))
            w(struct.pack(">BBBB", *q))
        for q in quads:
            w(struct.pack(">4H", *q))
    w(struct.pack(">i", len(takes)))
    for name, frames in takes:
        cn = name.encode("utf-8")
        w(struct.pack(">H", len(cn)) + cn)
        w(struct.pack(">f", float(fps)))
        w(struct.pack(">i", len(frames)))
        w(struct.pack(">i", len(bones) * 2))
        for i in range(len(bones)):
            w(struct.pack(">hb", i, 0))
            prev = None
            for fr in frames:
                q = fr[i][1]
                if prev is not None and prev.dot(q) < 0:
                    q = -q
                prev = q
                for c in (q.x, q.y, q.z, q.w):
                    w(struct.pack(">h", int(round(max(-1.0, min(1.0, c)) * 32767))))
            w(struct.pack(">hb", i, 1))
            for fr in frames:
                t = fr[i][0]
                w(struct.pack(">3f", t.x, t.y, t.z))
    OUT_MODEL.parent.mkdir(parents=True, exist_ok=True)
    OUT_MODEL.write_bytes(gzip.compress(out.getvalue(), 9, mtime=0))
    OUT_TEXTURE.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(TEXTURE, OUT_TEXTURE)
    print(f"leopard: {len(parts)} parts, {total} faces, {len(bones)} bones, height {hi_y - lo_y:.3f} units (y {lo_y:.3f} to {hi_y:.3f}), "
          f"{len(takes)} takes at {fps} a second")
    for name, kind, verts, quads in parts:
        print(f"  {name}: kind {kind}, {len(verts)} vertices, {len(quads)} faces")
    for name, frames in takes:
        print(f"  take {name}: {len(frames)} frames")


if __name__ == "__main__":
    main()
