#!/usr/bin/env python3
"""3.8.9: the owner's clothes replacements (input/newmodels/NewModels.zip: Clothes_Replacements.blend; the owner: "I also will be sending
clothes replacements for the ones currently in game") baked for the game, as the mace and the gun are (CI has no Blender): an OBJ per
armor piece under art/clothes/ and one atlas, which tools/katana_mesh.py packs into the jar and client/armor/ClothesModel draws on the
wearer.

The file holds four outfits, each on a mannequin of its own (a rig of 10 bones over a Minecraft player, in Minecraft pixels: the hips at
12.7, the head from 23.75, the arms' pivots at 4.96 out and 22.6 up), every mesh bound wholly to one bone. The pieces in game and what of
the file each is (the mannequin's skin, face, hair and jersey are not clothes and stay out):
  marine_cap        the Marine uniform's cap (its striped crown and peak, shaped bill, embroidered badge)
  marine_shirt      its tunic (the MARINE print on the back), sleeves, rolled cuffs, sailor collar and neckerchief
  marine_trousers   its trousers (indigo denim, folded hems)
  marine_boots      its calfskin boots and their welts
  admiral_coat      the Admiral coat (worn over the shoulders, sleeves hanging empty, JUSTICE on the back, epaulettes, buttons)
  straw_hat         the Straw hat
  blackbeard_coat   the Blackbeard coat
The owner's modifiers are applied but the bevels (a fraction of a pixel at the game's size, six times the faces): the cloth keeps its
thickness (Solidify). Each piece's faces are grouped by the body part that carries them (the frame each is drawn in, model px from that part's pivot, the game's
model space: x the player's left, y down, z back): head; body_upper and body_lower (split at y 6 where the body's bend folds it); each
arm's upper and lower half (split 4 px down the arm, its elbow); each leg's upper and lower (the thigh's and the shin's bones: its knee).

The look: every material is the owner's: a speckle of two close colours (its colour ramp) by the white noise of the surface's own place
floored 1.07 cells a pixel (Minecraft-sized pixels of cloth), and three of them a print over it (JUSTICE, MARINE, the cap's lettering:
their masks, packed in the file, mixed in by their alpha). Baked into one atlas: a face bigger than SMALL px gets its own patch of texels
(DENSITY a pixel, PRINT_DENSITY where it carries a print, so the lettering stays legible), each texel the colour of its place on the
face; a small face (the hat's weave, the cap's stripes, the epaulettes' bullion) one texel of its material's palette row (SHADES
shades of its ramp) by the noise at its middle. The cap and the hat are decimated for the phone (DECIMATE).

Usage: /tmp/bpy/bin/python tools/clothes_bake.py     (writes art/clothes/<piece>.obj and art/clothes/clothes.png)
"""
import math
import os
import re
import sys
import tempfile
import zipfile

import bpy
import bmesh
import mathutils
import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP = os.path.join(ROOT, "input", "newmodels", "NewModels.zip")
OUT = os.path.join(ROOT, "art", "clothes")
CELLS = 1.07
SMALL = 1.6
DENSITY = 2.0
PRINT_DENSITY = 8.0
SHADES = 16
ATLAS_W = 1024
# the many small faces of the cap, the hat (each decimated to about this many faces: their whole look kept) and the epaulettes' bullion
# (each pendant to this share of its faces): the Marines in the seas wear the cap, a player a coat
DECIMATE = {"Marine cap": 900, "Straw hat": 1300, "Epaulette L": 0.45, "Epaulette R": 0.45}

COATS = ["Blind turned hem", "Button", "Coat", "Collar face binding", "Collar top", "Continuous hem braid", "Cuff stud", "Cuff top braid",
         "Empty hanging sleeve", "Epaulette L", "Epaulette R", "Facing seam", "Front facing", "Shoulder yoke", "Standing collar",
         "Turned cuff", "Turned front edge"]
PIECES = {
    "marine_cap": ("Marine uniform", ["Marine cap"]),
    "marine_shirt": ("Marine uniform", ["Tunic", "Tunic sleeve", "Rolled shirt cuff", "Sailor collar", "Neckerchief"]),
    "marine_trousers": ("Marine uniform", ["Trouser"]),
    "marine_boots": ("Marine uniform", ["Boot"]),
    "admiral_coat": ("Admiral coat", COATS),
    "straw_hat": ("Straw hat", ["Straw hat"]),
    "blackbeard_coat": ("Blackbeard coat", COATS),
}
# the game's part pivots (model px) and which bone each group's faces come from
PIVOT = {"head": (0, 0, 0), "body": (0, 0, 0), "right_arm": (-5, 2, 0), "left_arm": (5, 2, 0), "right_leg": (-1.9, 12, 0),
         "left_leg": (1.9, 12, 0)}
BONE = {"Head": "head", "Spine": "body", "Hips": "body", "Arm.R": "right_arm", "Arm.L": "left_arm", "Thigh.R": "right_leg",
        "Shin.R": "right_leg", "Thigh.L": "left_leg", "Shin.L": "left_leg"}
# 3.8.9: the owner's mannequin is not quite the game's player (measured off its own skin): its arms 2.9 px deep (the game's 4) and 0.3 px
# in from the game's arm boxes, its head 7.85 px across and 0.68 px higher, its legs 0.2 px out: each part's cloth is carried from the
# mannequin's part onto the game's box as it sat on the mannequin's (model px)
ARM_X = 0.3
ARM_Z = (-0.49, 4.0 / 2.9)
HEAD = ((0.0, -4.68, 0.0), (8.0 / 7.85, 8.0 / 7.88, 8.0 / 7.44), (0.0, -4.0, 0.0))
LEG_X = 0.2


# 3.8.9 (the owner: "head is clipping through the coat"): a coat's standing collar was shaped round the mannequin's neck and smaller,
# higher head; the game's head is a box 8 px across (x and z within 4 of its axis) from the top of the body up. Above the shoulders (fully
# above COLLAR_TOP, fading out by COLLAR_BOTTOM) a point is carried out from the head's axis to clear that box by HEAD_CLEAR: its
# box-shaped distance from the axis (the larger of |x| and |z| over 4) remapped from COLLAR_IN..COLLAR_OUT onto 1 + HEAD_CLEAR / 4 ..
# COLLAR_OUT, in order, so the collar's inner face stays inside its outer and nothing past COLLAR_OUT moves
HEAD_CLEAR = 0.35
COLLAR_IN = 0.5
COLLAR_OUT = 1.2
COLLAR_TOP = 0.0
COLLAR_BOTTOM = 0.6


def collar(p):
    x, y, z = p
    if y >= COLLAR_BOTTOM:
        return p
    t = min(1.0, max(0.0, (COLLAR_BOTTOM - y) / (COLLAR_BOTTOM - COLLAR_TOP)))
    w = t * t * (3 - 2 * t)
    rho = max(abs(x), abs(z)) / 4.0
    if rho >= COLLAR_OUT or rho < 1e-6:
        return p
    low = 1.0 + HEAD_CLEAR / 4.0
    target = low + (max(rho, COLLAR_IN) - COLLAR_IN) / (COLLAR_OUT - COLLAR_IN) * (COLLAR_OUT - low)
    k = 1.0 + (max(target, rho) / rho - 1.0) * w
    return (x * k, y, z * k)


def fit(part, p):
    """A point of the mannequin's part (model px) onto the game's player's part."""
    x, y, z = p
    if part == "body":
        return collar(p)
    if part.endswith("_arm"):
        side = 1.0 if part.startswith("left") else -1.0
        return (x + side * ARM_X, y, (z - ARM_Z[0]) * ARM_Z[1])
    if part == "head":
        (cx, cy, cz), (kx, ky, kz), (tx, ty, tz) = HEAD
        return ((x - cx) * kx + tx, (y - cy) * ky + ty, (z - cz) * kz + tz)
    if part.endswith("_leg"):
        side = 1.0 if part.startswith("left") else -1.0
        return (x - side * LEG_X, y, z)
    return p


# 3.8.9 (the owner: "the arms should never interact with the coat's arms", with pictures of Garp's coat worn over the shoulders and
# blown back): a coat's empty hanging sleeves and their cuffs are the coat's, each in a group of its own (right_sleeve, left_sleeve, the
# body's frame) so it can be hinged at its shoulder and blown back; the coat's lining (its inner face: these materials) is grouped apart
# (_in) so the coat is kept out of an arm's way with its lining just inside its cloth
SLEEVES = ("Empty hanging sleeve", "Turned cuff", "Cuff top braid", "Cuff stud")
LININGS = ("Violet silk", "Oxblood silk")
GROUPS = ["head", "body_upper", "body_upper_in", "body_lower", "body_lower_in", "right_sleeve", "left_sleeve", "right_arm_upper", "right_arm_lower", "left_arm_upper", "left_arm_lower",
          "right_leg_upper", "right_leg_lower", "left_leg_upper", "left_leg_lower"]


def load():
    folder = tempfile.mkdtemp()
    with zipfile.ZipFile(ZIP) as z:
        z.extract("Clothes_Replacements.blend", folder)
    bpy.ops.wm.open_mainfile(filepath=os.path.join(folder, "Clothes_Replacements.blend"))


def hash01(cells):
    """A white noise of integer cells (n x 3) in [0, 1): the same cell always the same value (Blender's own is another hash: the same
    kind of speckle)."""
    c = cells.astype(np.int64) & 0xFFFFFFFF
    h = (c[:, 0] * 73856093) ^ (c[:, 1] * 19349663) ^ (c[:, 2] * 83492791)
    h = h & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5BD1E995) & 0xFFFFFFFF
    h ^= h >> 15
    return (h & 0xFFFFFF) / float(0x1000000)


class Material:
    def __init__(self, m):
        nt = m.node_tree
        ramp = next(n for n in nt.nodes if n.type == "VALTORGB")
        self.stops = [(e.position, np.array(e.color[:3])) for e in ramp.color_ramp.elements]
        self.print = None
        image = next((n for n in nt.nodes if n.type == "TEX_IMAGE"), None)
        mix = next((n for n in nt.nodes if n.type in ("MIX_RGB", "MIX")), None)
        if image is not None and image.image is not None and mix is not None:
            img = image.image
            px = np.array(img.pixels[:], float).reshape(img.size[1], img.size[0], 4)
            self.print = (px, np.array(mix.inputs["Color2"].default_value[:3]))
            uvnode = next((n for n in nt.nodes if n.type == "UVMAP"), None)
            self.uv_name = uvnode.uv_map if uvnode is not None and uvnode.uv_map else None
        self.name = m.name

    def colour(self, value, uv=None):
        """Linear colours (n x 3) for noise values (n); the print mixed in by its mask's alpha at uv (n x 2)."""
        (p0, c0), (p1, c1) = self.stops[0], self.stops[-1]
        t = np.clip((value - p0) / max(1e-6, p1 - p0), 0.0, 1.0)[:, None]
        col = c0 + (c1 - c0) * t
        if self.print is not None and uv is not None:
            px, ink = self.print
            h, w = px.shape[:2]
            x = np.clip(uv[:, 0] * w - 0.5, 0, w - 1)
            y = np.clip(uv[:, 1] * h - 0.5, 0, h - 1)
            inside = (uv[:, 0] >= 0) & (uv[:, 0] <= 1) & (uv[:, 1] >= 0) & (uv[:, 1] <= 1)
            x0, y0 = np.floor(x).astype(int), np.floor(y).astype(int)
            x1, y1 = np.minimum(x0 + 1, w - 1), np.minimum(y0 + 1, h - 1)
            fx, fy = x - x0, y - y0
            a = (px[y0, x0, 3] * (1 - fx) * (1 - fy) + px[y0, x1, 3] * fx * (1 - fy) + px[y1, x0, 3] * (1 - fx) * fy
                 + px[y1, x1, 3] * fx * fy) * inside
            col = col * (1 - a[:, None]) + ink * a[:, None]
        return col


def srgb(col):
    return np.clip(np.round(255 * np.clip(col, 0, 1) ** (1 / 2.2)), 0, 255).astype(np.uint8)


def wanted(o, outfit, names):
    arm = next((m.object for m in o.modifiers if m.type == "ARMATURE" and m.object), None)
    if arm is None or not arm.name.startswith(outfit):
        return False
    base = re.sub(r"\.\d+$", "", o.name)
    head = base.split(" / ")[0]
    return head in names or base in names


def triangles(o, rig_inv):
    """[(group, material, [(model px, normal, cut, uv)] x 3)] of a mesh (as the owner's modifiers make it: the cloth's thickness and its
    bevelled edges), in the game's model space (the rig's own frame: the rigs stand turned 15 degrees for the file's cameras), each
    face's group by its bone and where its middle is."""
    # the bevels (a fraction of a pixel at the game's size) left off: they multiply the faces six times over
    for m in o.modifiers:
        if m.type in ("BEVEL", "WEIGHTED_NORMAL"):
            m.show_viewport = False
    depsgraph = bpy.context.evaluated_depsgraph_get()
    me = bpy.data.meshes.new_from_object(o.evaluated_get(depsgraph))
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces[:])
    base = re.sub(r"\.\d+$", "", o.name).split(" / ")[0]
    budget = DECIMATE.get(base)
    if budget is not None and budget < 1.0:
        budget = len(bm.faces) * budget
    if budget is not None and len(bm.faces) > budget:
        bm.to_mesh(me)
        bm.free()
        tmp = bpy.data.objects.new("tmp_" + o.name, me)
        bpy.context.scene.collection.objects.link(tmp)
        mod = tmp.modifiers.new("dec", "DECIMATE")
        mod.ratio = budget / len(me.polygons)
        me = bpy.data.meshes.new_from_object(tmp.evaluated_get(bpy.context.evaluated_depsgraph_get()))
        bm = bmesh.new()
        bm.from_mesh(me)
        bmesh.ops.triangulate(bm, faces=bm.faces[:])
    bm.normal_update()
    uv_layers = {l.name: l for l in bm.loops.layers.uv.values()} if bm.loops.layers.uv else {}
    bone = o.vertex_groups[0].name if o.vertex_groups else "Spine"
    sleeve = None
    if base in SLEEVES:
        centre = sum((mathutils.Vector(c) for c in o.bound_box), mathutils.Vector()) / 8.0
        sleeve = "left_sleeve" if (rig_inv @ o.matrix_world @ centre).x > 0 else "right_sleeve"
    part = BONE.get(bone, "body")
    world = rig_inv @ o.matrix_world
    rot = world.to_3x3()
    mats = [m for m in o.data.materials]
    out = []
    for f in bm.faces:
        mat = mats[f.material_index] if f.material_index < len(mats) else mats[0]
        corners = []
        for loop in f.loops:
            v = loop.vert
            p = world @ v.co
            n = (rot @ (v.normal if f.smooth else f.normal)).normalized()
            uv = None
            layer = uv_layers.get(getattr(MATS[mat.name], "uv_name", None) or "") or (next(iter(uv_layers.values())) if uv_layers else None)
            if layer is not None and MATS[mat.name].print is not None:
                uv = tuple(loop[layer].uv)
            # Blender (x, y, z) -> model space (x, 24 - z, y): x the player's left, y down from the top of the body, z back
            corners.append((fit(part, (p.x, 24.0 - p.z, p.y)), (n.x, -n.z, n.y), tuple(v.co), uv))
        mid_y = sum(c[0][1] for c in corners) / 3.0
        if sleeve is not None:
            group = sleeve
        elif part == "body":
            group = ("body_upper" if mid_y < 6.0 else "body_lower") + ("_in" if mat.name in LININGS else "")
        elif part.endswith("_arm"):
            group = part + ("_upper" if mid_y - 2.0 < 4.0 else "_lower")
        elif part.endswith("_leg"):
            group = part + ("_lower" if bone.startswith("Shin") else "_upper")
        else:
            group = "head"
        out.append((group, mat.name, corners))
    bm.free()
    return out


MATS = {}


class Packer:
    """Shelves of patches in an atlas ATLAS_W wide, as tall as it needs."""

    def __init__(self, width):
        self.w = width
        self.x = 0
        self.y = 0
        self.row = 0

    def place(self, w, h):
        if self.x + w > self.w:
            self.x = 0
            self.y += self.row
            self.row = 0
        at = (self.x, self.y)
        self.x += w
        self.row = max(self.row, h)
        return at

    def height(self):
        return self.y + self.row


def bake(pieces):
    """{piece: [(group, [(pos, normal, (u px, v px))] x 3)]} and the atlas (h x w x 3 uint8)."""
    names = sorted({m for tris in pieces.values() for _, m, _ in tris})
    palette_rows = {m: i for i, m in enumerate(names)}
    packer = Packer(ATLAS_W)
    # the palette: a row of SHADES texels per material, up top
    for _ in names:
        packer.place(ATLAS_W, 1)
    patches = []
    out = {k: [] for k in pieces}
    for piece, tris in pieces.items():
        for group, mat, corners in tris:
            p = [np.array(c[0]) for c in corners]
            e1 = p[1] - p[0]
            u = np.linalg.norm(e1)
            if u < 1e-6:
                continue
            a1 = e1 / u
            d2 = p[2] - p[0]
            cu = float(d2 @ a1)
            e2 = d2 - a1 * cu
            v = np.linalg.norm(e2)
            if v < 1e-6:
                continue
            a2 = e2 / v
            size = max(u, v, np.linalg.norm(p[2] - p[1]))
            material = MATS[mat]
            if size < SMALL:
                cut = np.mean([np.array(c[2]) for c in corners], axis=0)
                value = hash01(np.floor(cut[None, :] * CELLS))[0]
                shade = int(min(SHADES - 1, value * SHADES))
                row = palette_rows[mat]
                uvpx = [(shade + 0.5, row + 0.5)] * 3
                out[piece].append((group, [(corners[k][0], corners[k][1], uvpx[k]) for k in range(3)]))
                continue
            density = PRINT_DENSITY if material.print is not None else DENSITY
            umin, umax = min(0.0, cu), max(u, cu)
            w = int(math.ceil((umax - umin) * density)) + 2
            h = int(math.ceil(v * density)) + 2
            patches.append((piece, group, mat, corners, (u, cu, v, umin, density, w, h)))
    patches.sort(key=lambda t: -t[4][6])
    placed = []
    for patch in patches:
        w, h = patch[4][5], patch[4][6]
        placed.append((patch, packer.place(w, h)))
    height = packer.height()
    height = 1 << max(4, int(math.ceil(math.log2(height))))
    img = np.zeros((height, ATLAS_W, 3), np.uint8)
    for m, row in palette_rows.items():
        values = (np.arange(SHADES) + 0.5) / SHADES
        img[row, :SHADES] = srgb(MATS[m].colour(values))
    for (piece, group, mat, corners, (u, cu, v, umin, density, w, h)), (x0, y0) in placed:
        material = MATS[mat]
        jj, ii = np.mgrid[0:h, 0:w]
        s = (ii + 0.5 - 1.0) / density + umin
        t = (jj + 0.5 - 1.0) / density
        # barycentric of (s, t) in the face's own 2D frame: p0 (0, 0), p1 (u, 0), p2 (cu, v) (outside it: extrapolated, the padding)
        l2 = t / v
        l1 = (s - cu * l2) / u
        l0 = 1.0 - l1 - l2
        cuts = np.array([c[2] for c in corners])
        cut = l0[..., None] * cuts[0] + l1[..., None] * cuts[1] + l2[..., None] * cuts[2]
        value = hash01(np.floor(cut.reshape(-1, 3) * CELLS))
        uv = None
        if material.print is not None and all(c[3] is not None for c in corners):
            uvs = np.array([c[3] for c in corners])
            uv = (l0[..., None] * uvs[0] + l1[..., None] * uvs[1] + l2[..., None] * uvs[2]).reshape(-1, 2)
        img[y0:y0 + h, x0:x0 + w] = srgb(material.colour(value, uv)).reshape(h, w, 3)
        uvpx = [(x0 + 1.0 + (0.0 - umin) * density, y0 + 1.0), (x0 + 1.0 + (u - umin) * density, y0 + 1.0),
                (x0 + 1.0 + (cu - umin) * density, y0 + 1.0 + v * density)]
        out[piece].append((group, [(corners[k][0], corners[k][1], uvpx[k]) for k in range(3)]))
    return out, img


# 3.8.9 (the owner: "The arms cannot clip through the coats"): the coats were shaped round the mannequin's thinner arms and lower shoulders;
# a coat's body point inside the game's arm (its box: 4 px across and deep, from the top of the body down 12 px, grown by ARM_FIT at the
# sides and the bottom) is moved to that box's nearest side or onto the shoulder's top, its cloth ARM_LINING px further out than its
# lining, so the coat at rest sits on the game's arms (client/armor's push then only works when they move)
ARM_FIT = 0.35
ARM_LINING = 0.25
ARM_BOXES = (((-8.0, 0.0, -2.0), (-4.0, 12.0, 2.0)), ((4.0, 0.0, -2.0), (8.0, 12.0, 2.0)))
COAT_PIECES = ("admiral_coat", "blackbeard_coat")


def arm_fit(pos, lining):
    """A coat body point (model px) moved out of the game's arms at rest."""
    x, y, z = pos
    for lo, hi in ARM_BOXES:
        l = (lo[0] - ARM_FIT, lo[1], lo[2] - ARM_FIT)
        u = (hi[0] + ARM_FIT, hi[1] + ARM_FIT, hi[2] + ARM_FIT)
        if not (l[0] < x < u[0] and l[1] < y < u[1] and l[2] < z < u[2]):
            continue
        extra = 0.0 if lining else ARM_LINING
        ways = [(x - l[0], (l[0] - extra, y, z)), (u[0] - x, (u[0] + extra, y, z)), (z - l[2], (x, y, l[2] - extra)),
                (u[2] - z, (x, y, u[2] + extra)), (y - l[1], (x, l[1] - extra, z))]
        x, y, z = min(ways, key=lambda t: t[0])[1]
    return (x, y, z)


def write(piece, tris, w, h):
    """The piece's OBJ: its faces group by group (GROUPS' order), each in its part's frame (model px from the part's pivot)."""
    lines = [f"# HexPiece 3.8.9: the owner's {piece} (Clothes_Replacements.blend) baked by tools/clothes_bake.py: model px from each "
             "part's pivot (x left, y down, z back)"]
    vs, vts, vns, faces = [], [], [], []
    for group in GROUPS:
        name = group[:-3] if group.endswith("_in") else group
        part = "body" if name.endswith("_sleeve") else name.rsplit("_", 1)[0] if name.endswith(("_upper", "_lower")) else name
        pivot = np.array(PIVOT[part])
        for g, corners in tris:
            if g != group:
                continue
            idx = []
            for pos, nrm, (u, v) in corners:
                if piece in COAT_PIECES and group.startswith("body_"):
                    pos = arm_fit(pos, group.endswith("_in"))
                q = np.array(pos) - pivot
                vs.append(q)
                vns.append(nrm)
                vts.append((u / w, 1.0 - v / h))
                idx.append(len(vs))
            faces.append((group, idx))
    for v in vs:
        lines.append(f"v {v[0]:.4f} {v[1]:.4f} {v[2]:.4f}")
    for t in vts:
        lines.append(f"vt {t[0]:.6f} {t[1]:.6f}")
    for n in vns:
        lines.append(f"vn {n[0]:.4f} {n[1]:.4f} {n[2]:.4f}")
    current = None
    for group, idx in faces:
        if group != current:
            lines.append(f"g {group}")
            current = group
        lines.append("f " + " ".join(f"{i}/{i}/{i}" for i in idx))
    with open(os.path.join(OUT, piece + ".obj"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")
    counts = {}
    for group, _ in faces:
        counts[group] = counts.get(group, 0) + 1
    print(f"{piece}: {len(faces)} triangles {counts}")


def main():
    load()
    for m in bpy.data.materials:
        if m.node_tree and any(n.type == "VALTORGB" for n in m.node_tree.nodes):
            MATS[m.name] = Material(m)
    pieces = {}
    for piece, (outfit, names) in PIECES.items():
        rig = next(o for o in bpy.data.objects if o.type == "ARMATURE" and o.name.startswith(outfit))
        rig_inv = rig.matrix_world.inverted()
        tris = []
        for o in bpy.data.objects:
            if o.type == "MESH" and wanted(o, outfit, names):
                tris += triangles(o, rig_inv)
        pieces[piece] = tris
    out, img = bake(pieces)
    os.makedirs(OUT, exist_ok=True)
    from PIL import Image
    Image.fromarray(img, "RGB").convert("RGBA").save(os.path.join(OUT, "clothes.png"), optimize=True)
    h, w = img.shape[:2]
    for piece, tris in out.items():
        write(piece, tris, w, h)
    print(f"atlas {w} x {h}")


if __name__ == "__main__":
    main()
