#!/usr/bin/env python3
"""3.8.8: the owner's first gun (input/newmodels/NewModels.zip: First_gun.blend) baked for the game, as the mace is (tools/mace_bake.py):
an OBJ and its atlas under art/gun/, which tools/katana_mesh.py packs into the jar (CI has no Blender). The owner's category: Gun
("Pistols, usually held with one hand (right) in a badass pose, all weapons only shoot 1 bullet at a time, and auto reloads after,
bullets are also the same model from now one ... when haki is on, bullets come out black (pure black), those bullets are much much
faster and go through blocks (10 blocks max), simple reload animation, put 1 bullet in the hole of the barrel").

Two models:
  flintlock   the pistol: every part under "Flintlock • grip pivot" (the brass barrel and its stepped crown, the chestnut stock, the
              gilt plates and guard, the lock, pins and medallions, the rod), and its ink lines (curves bevelled thick enough to be
              seen at the game's scale: INK_LEAST; the hairline hatching is left out), each decimated for the phone (a big part to
              MESH_FACES, one under SMALL to SMALL_FACES, one under TINY, a pin's marking, left out)
  lead_ball   the bullet: "Cast lead ball" (the owner: "bullets are also the same model from now one")
Frame (model px): the grip's pivot at the origin, the barrel forward along -z (Minecraft's own "forward" for a held item), up +y,
SCALE px a Blender unit (the pistol about one block long). The look: each material's own toon ramp (its colour stops) baked into the
atlas as a strip; each corner takes the stop its normal would light under KEY (the cel steps of the owner's render), ink black-brown.

Usage: /tmp/bpy/bin/python tools/gun_bake.py        (writes art/gun/flintlock.obj, art/gun/lead_ball.obj, art/gun/gun.png)
"""
import math
import os
import tempfile
import zipfile

import bpy
import bmesh
import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP = os.path.join(ROOT, "input", "newmodels", "NewModels.zip")
OUT = os.path.join(ROOT, "art", "gun")
SCALE = 4.2
INK_LEAST = 0.021
MESH_FACES = 420
SMALL = 0.2
SMALL_FACES = 24
TINY = 0.03
CURVE_FACES = 36
BALL_FACES = 96
KEY = np.array([-0.35, 0.55, 0.75])
KEY = KEY / np.linalg.norm(KEY)
ATLAS = 64
# the materials' rows in the atlas (each a strip: u across the toon ramp, dark to light)
ROWS = ["Cel • chestnut", "Cel • lemon brass", "Cel • round shot", "Front rim • reflected sky", "Ink • sepia", "Muzzle • inner shadow",
        "Wood grain ink"]


def load():
    folder = tempfile.mkdtemp()
    with zipfile.ZipFile(ZIP) as z:
        z.extract("First_gun.blend", folder)
    bpy.ops.wm.open_mainfile(filepath=os.path.join(folder, "First_gun.blend"))


def ramps():
    out = {}
    for m in bpy.data.materials:
        for n in m.node_tree.nodes:
            if n.type == "VALTORGB":
                out[m.name] = [(e.position, tuple(e.color[:3])) for e in n.color_ramp.elements]
    return out


def triangles(obj, faces):
    """obj's (evaluated: a curve's bevel) mesh in the world, decimated to about faces faces: [(material, [(pos, normal) x3])]."""
    depsgraph = bpy.context.evaluated_depsgraph_get()
    evaluated = obj.evaluated_get(depsgraph)
    me = bpy.data.meshes.new_from_object(evaluated)
    me.transform(obj.matrix_world)
    mats = [m.name if m else "Ink • sepia" for m in (obj.data.materials if obj.type == "MESH" else obj.data.materials)]
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces[:])
    n = len(bm.faces)
    bm.to_mesh(me)
    bm.free()
    if n > faces:
        tmp = bpy.data.objects.new("tmp_" + obj.name, me)
        bpy.context.scene.collection.objects.link(tmp)
        mod = tmp.modifiers.new("dec", "DECIMATE")
        mod.ratio = faces / n
        depsgraph = bpy.context.evaluated_depsgraph_get()
        me = bpy.data.meshes.new_from_object(tmp.evaluated_get(depsgraph))
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces[:])
    bm.normal_update()
    out = []
    for f in bm.faces:
        mat = mats[f.material_index] if f.material_index < len(mats) else (mats[0] if mats else "Ink • sepia")
        out.append((mat, [(tuple(v.co), tuple(v.normal if f.smooth else f.normal)) for v in f.verts]))
    bm.free()
    return out


def write(name, tris, origin, rows):
    """An OBJ of tris in the gun's frame (origin at the pivot), each corner's uv on its material's strip by how KEY lights it."""
    lines = [f"# HexPiece 3.8.8: the owner's {name} (First_gun.blend) baked by tools/gun_bake.py: model px, the grip's pivot at 0"]
    vs, vts, vns, faces = [], [], [], []
    for mat, tri in tris:
        idx = []
        row = rows.index(mat) if mat in rows else rows.index("Ink • sepia")
        for p, n in tri:
            # Blender: x along the barrel (the muzzle +x), z up -> game: forward -z, up +y, right +x
            x, y, z = p[0] - origin[0], p[1] - origin[1], p[2] - origin[2]
            vs.append((y * SCALE, z * SCALE, -x * SCALE))
            nn = np.array([n[1], n[2], -n[0]])
            nn = nn / (np.linalg.norm(nn) or 1.0)
            vns.append(tuple(nn))
            lit = float(np.clip(0.5 + 0.5 * np.dot(nn, KEY), 0.0, 1.0))
            vts.append(((0.5 + lit * (ATLAS - 1.0)) / ATLAS * 0.98 + 0.01, 1.0 - (row + 0.5) / len(rows)))
            idx.append(len(vs))
        faces.append((mat, idx))
    for v in vs:
        lines.append(f"v {v[0]:.4f} {v[1]:.4f} {v[2]:.4f}")
    for t in vts:
        lines.append(f"vt {t[0]:.5f} {t[1]:.5f}")
    for n in vns:
        lines.append(f"vn {n[0]:.4f} {n[1]:.4f} {n[2]:.4f}")
    current = None
    for mat, idx in faces:
        group = "01_" + mat.replace(" • ", "_").replace(" ", "_").lower()
        if group != current:
            lines.append(f"g {group}")
            current = group
        lines.append("f " + " ".join(f"{i}/{i}/{i}" for i in idx))
    with open(os.path.join(OUT, name + ".obj"), "w", encoding="utf-8") as h:
        h.write("\n".join(lines) + "\n")
    ys = [v[1] for v in vs]
    zs = [v[2] for v in vs]
    print(f"{name}: {len(faces)} triangles, y {min(ys):.2f}..{max(ys):.2f}, z {min(zs):.2f}..{max(zs):.2f} px")


def paint(rows, ramp):
    """The atlas: each material a row, its toon ramp's stops left (unlit) to right (lit), stepped as the owner's cel shader steps."""
    from PIL import Image
    img = np.zeros((len(rows) * 4, ATLAS, 4), np.uint8)
    for r, mat in enumerate(rows):
        stops = ramp.get(mat, [(0.0, (0.045, 0.025, 0.026))])
        for x in range(ATLAS):
            t = x / (ATLAS - 1.0)
            colour = stops[0][1]
            for pos, col in stops:
                if t >= pos:
                    colour = col
            srgb = [min(255, int(round(255 * (c ** (1 / 2.2))))) for c in colour]
            img[r * 4:(r + 1) * 4, x] = srgb + [255]
    Image.fromarray(img, "RGBA").resize((ATLAS, ATLAS), Image.NEAREST).save(os.path.join(OUT, "gun.png"))


def main():
    load()
    ramp = ramps()
    pivot = bpy.data.objects["Flintlock • grip pivot"].matrix_world.translation
    gun = []
    for o in bpy.data.objects:
        if o.name == "Cast lead ball":
            continue
        if o.type == "MESH" and o.parent is not None and o.parent.name.startswith("Flintlock"):
            size = max(o.dimensions)
            if size < TINY:
                continue  # a pin's marking, a recess: smaller than a pixel in the hand
            gun += triangles(o, SMALL_FACES if size < SMALL else MESH_FACES)
        elif o.type == "CURVE" and o.data.bevel_depth >= INK_LEAST:
            gun += triangles(o, CURVE_FACES)
    os.makedirs(OUT, exist_ok=True)
    write("flintlock", gun, tuple(pivot), ROWS)
    ball = bpy.data.objects["Cast lead ball"]
    centre = ball.matrix_world.translation
    write("lead_ball", triangles(ball, BALL_FACES), tuple(centre), ROWS)
    paint(ROWS, ramp)


if __name__ == "__main__":
    main()
