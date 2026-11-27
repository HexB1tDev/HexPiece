#!/usr/bin/env python3
"""3.8.7: the owner's mace (input/newmodels/NewModels.zip: First_mace.blend) baked for the game, as the blades are: an OBJ in the
katanas' frame (model px: the grip's top at y 0, the head up +y, the handle down to -y) and its atlas, committed under art/mace/ (CI
has no Blender: tools/katana_mesh.py packs these into the jar as it packs the legendary blades). The owner's category for it: Mace
("Right handed, rests on the back like Yoru, the pull out is slow, when idle the player rests it on it's right shoulder, has range, hits
ragdoll but are slow when cocking them, fast on the swing").

What it does: opens the .blend (unpacked from the zip into a scratch folder), takes its three parts (the tapered iron body, its 36
spikes, the suspension ring), applies their transforms, decimates them for the phone (about 3,000 faces in all: the body to DECIMATE
of its faces, each spike to SPIKE_FACES, the ring to RING), turns the long axis (Blender z) to the game's +y with the narrow handle down,
puts y 0 at GRIP_Z (the top of the handle, where the right hand holds it), scales it to LENGTH model px end to end (2.5 blocks: it has
range), maps the body round its axis onto the atlas's left half, the spikes base to tip onto the next quarter, the ring onto the last,
and paints the atlas: the owner's "Blued black iron" (0.031, 0.031, 0.043; metallic, rough 0.5, a noise over it) as dark blue-black
iron with a faint grain and a cold sheen down the length, the spikes' points worn to bright steel.

Usage: python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy        (bpy 5.3, Python 3.13)
       /tmp/bpy/bin/python tools/mace_bake.py                         (writes art/mace/iron_mace.obj and art/mace/iron_mace.png)
"""
import math
import os
import random
import tempfile
import zipfile

import bpy
import bmesh
import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP = os.path.join(ROOT, "input", "newmodels", "NewModels.zip")
OUT = os.path.join(ROOT, "art", "mace")
LENGTH = 40.0
GRIP_Z = 1.35
DECIMATE = 0.32
SPIKE_FACES = 40
RING_FACES = 160
ATLAS = 128


def load():
    folder = tempfile.mkdtemp()
    with zipfile.ZipFile(ZIP) as z:
        z.extract("First_mace.blend", folder)
    bpy.ops.wm.open_mainfile(filepath=os.path.join(folder, "First_mace.blend"))


def decimated(obj, faces):
    """A copy of obj's mesh in the world, decimated to about faces faces, triangulated."""
    me = obj.data.copy()
    me.transform(obj.matrix_world)
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces[:])
    ratio = min(1.0, faces / max(1, len(bm.faces)))
    bm.to_mesh(me)
    bm.free()
    tmp = bpy.data.objects.new("tmp_" + obj.name, me)
    bpy.context.scene.collection.objects.link(tmp)
    if ratio < 1.0:
        mod = tmp.modifiers.new("dec", "DECIMATE")
        mod.ratio = ratio
        depsgraph = bpy.context.evaluated_depsgraph_get()
        evaluated = tmp.evaluated_get(depsgraph)
        me2 = bpy.data.meshes.new_from_object(evaluated)
    else:
        me2 = me
    bm = bmesh.new()
    bm.from_mesh(me2)
    bmesh.ops.triangulate(bm, faces=bm.faces[:])
    bm.normal_update()
    tris = []
    for f in bm.faces:
        tris.append([(tuple(v.co), tuple(f.normal if not f.smooth else v.normal)) for v in f.verts])
    bm.free()
    return tris


def to_game(p):
    """Blender (x, y, z), z along the mace, the handle low -> model px (right, up, out), y 0 at the grip."""
    k = LENGTH / SPAN
    return ((p[0]) * k, (p[2] - GRIP_Z) * k, (p[1]) * k)


def main():
    global SPAN
    load()
    body = bpy.data.objects["Continuous tapered iron body"]
    spikes = [o for o in bpy.data.objects if o.type == "MESH" and o.name.startswith("Spike")]
    ring = bpy.data.objects.get("Suspension ring")
    parts = [("01_Blade_iron", decimated(body, int(len(body.data.polygons) * DECIMATE)))]
    spike_tris = []
    for s in spikes:
        spike_tris += [(s, t) for t in decimated(s, SPIKE_FACES)]
    if ring is not None:
        parts.append(("03_Ring_iron", decimated(ring, RING_FACES)))
    zs = [c[0][2] for _, tris in parts for t in tris for c in t] + [c[0][2] for _, t in spike_tris for c in t]
    lo, hi = min(zs), max(zs)
    SPAN = hi - lo
    # the atlas: the body on [0, .5] round it, the spikes [.5, .75] base to tip, the ring [.75, 1]
    lines = ["# HexPiece 3.8.7: the owner's mace (First_mace.blend) baked by tools/mace_bake.py: model px, the grip's top at y 0",
             "mtllib iron_mace.mtl"]
    vs, vts, vns, faces = [], [], [], []

    def add(group, tri, uv_of):
        idx = []
        for (p, n) in tri:
            g = to_game(p)
            vs.append(g)
            ng = (n[0], n[2], n[1])
            l = math.sqrt(sum(c * c for c in ng)) or 1.0
            vns.append(tuple(c / l for c in ng))
            vts.append(uv_of(p))
            idx.append(len(vs))
        faces.append((group, idx))

    def body_uv(p):
        a = (math.atan2(p[1], p[0]) / (2 * math.pi)) % 1.0
        return (0.02 + 0.46 * a, (p[2] - lo) / SPAN)

    def ring_uv(p):
        a = (math.atan2(p[1], p[0]) / (2 * math.pi)) % 1.0
        return (0.77 + 0.21 * a, 0.5)

    for tri in parts[0][1]:
        add("01_Blade_iron", tri, body_uv)
    by_spike = {}
    for s, tri in spike_tris:
        by_spike.setdefault(s.name, []).append(tri)
    for name, tris in by_spike.items():
        pts = np.array([c[0] for t in tris for c in t])
        centre = pts.mean(axis=0)
        axis_out = np.array([centre[0], centre[1], 0.0])
        axis_out /= np.linalg.norm(axis_out) or 1.0
        reach = pts @ axis_out
        r0, r1 = reach.min(), reach.max()

        def spike_uv(p, axis_out=axis_out, r0=r0, r1=r1):
            t = (np.dot(p, axis_out) - r0) / max(1e-6, r1 - r0)
            return (0.52 + 0.21 * float(np.clip(t, 0, 1)), 0.5)

        for tri in tris:
            add("02_Spikes_iron", tri, spike_uv)
    if ring is not None:
        for tri in parts[1][1]:
            add("03_Ring_iron", tri, ring_uv)
    for v in vs:
        lines.append(f"v {v[0]:.4f} {v[1]:.4f} {v[2]:.4f}")
    for t in vts:
        lines.append(f"vt {t[0]:.5f} {t[1]:.5f}")
    for n in vns:
        lines.append(f"vn {n[0]:.4f} {n[1]:.4f} {n[2]:.4f}")
    current = None
    for group, idx in faces:
        if group != current:
            lines.append(f"g {group}")
            current = group
        lines.append("f " + " ".join(f"{i}/{i}/{i}" for i in idx))
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, "iron_mace.obj"), "w", encoding="utf-8") as h:
        h.write("\n".join(lines) + "\n")
    paint()
    ys = [v[1] for v in vs]
    print(f"iron_mace: {len(faces)} triangles, y {min(ys):.2f} to {max(ys):.2f} px, width {max(abs(v[0]) for v in vs) * 2:.2f} px")


def paint():
    """The atlas: blued black iron with a faint grain and a cold sheen along it; the spikes' points worn to steel."""
    from PIL import Image
    rnd = random.Random(7)
    img = np.zeros((ATLAS, ATLAS, 4), float)
    base = np.array([0.055, 0.058, 0.078])
    for y in range(ATLAS):
        for x in range(ATLAS):
            u = x / (ATLAS - 1)
            v = 1.0 - y / (ATLAS - 1)
            grain = 0.012 * math.sin(v * 90.0 + 3.0 * math.sin(u * 40.0)) + 0.01 * (rnd.random() - 0.5)
            if u < 0.5:
                a = u / 0.5 * 2 * math.pi
                sheen = 0.05 * max(0.0, math.cos(a - 0.6)) ** 6 + 0.02 * max(0.0, math.cos(a + 2.4)) ** 4
                c = base + grain + np.array([0.6, 0.7, 1.0]) * sheen
            elif u < 0.75:
                t = (u - 0.5) / 0.25
                c = base + grain + np.array([0.34, 0.36, 0.42]) * t ** 3
            else:
                c = base * 1.15 + grain
            img[y, x, :3] = np.clip(c, 0, 1)
            img[y, x, 3] = 1.0
    Image.fromarray((img * 255).astype(np.uint8), "RGBA").save(os.path.join(OUT, "iron_mace.png"))


if __name__ == "__main__":
    main()
