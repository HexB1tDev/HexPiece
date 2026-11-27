#!/usr/bin/env python3
"""3.8.9: renders the owner's clothes (art/clothes, tools/clothes_bake.py) on the player as client/armor/ClothesModel draws them: each
group of faces on its body part (tools/sword_rig.py's chain), the lower body and the legs out of the body's bend, the forearms and shins
bent with their joints. Usage: python3 tools/clothes_view.py <out.png> [anim.json tick] (a pose from an animation; else standing)."""
import io
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import katana_mesh as km  # noqa: E402
import sword_rig as rig  # noqa: E402
import sword_view as sv  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DIR = os.path.join(ROOT, "art", "clothes")
PIECES = ["marine_cap", "marine_shirt", "marine_trousers", "marine_boots", "admiral_coat", "straw_hat", "blackbeard_coat"]
PIVOT = {"head": (0, 0, 0), "body": (0, 0, 0), "right_arm": (-5, 2, 0), "left_arm": (5, 2, 0), "right_leg": (-1.9, 12, 0),
         "left_leg": (1.9, 12, 0)}
PART = {"head": "head", "body": "torso", "right_arm": "rightArm", "left_arm": "leftArm", "right_leg": "rightLeg", "left_leg": "leftLeg"}


def load(piece):
    with open(os.path.join(DIR, piece + ".obj"), encoding="utf-8") as h:
        return km.parse(h.read())


# client/armor/ClothesModel's skirt: below HIP (body px) a coat's cloth turns about the hip line with the legs' swing (SWING of it), fully
# FULL px below the hips, each point by the leg under it (blended across SIDE px either side of the middle)
HIP = 12.0
SWING = 0.6
FULL = 8.0
SIDE = 2.0


def skirt(p, right, left):
    """Body px points (n x 3) of a body_lower group bent by the legs' pitches (radians)."""
    out = p.copy()
    t = np.clip((p[:, 1] - HIP) / FULL, 0.0, 1.0)
    u = np.clip((p[:, 0] + SIDE) / (2 * SIDE), 0.0, 1.0)
    s = u * u * (3 - 2 * u)
    a = (right * (1 - s) + left * s) * SWING * t
    dy, z = p[:, 1] - HIP, p[:, 2]
    out[:, 1] = HIP + dy * np.cos(a) - z * np.sin(a)
    out[:, 2] = dy * np.sin(a) + z * np.cos(a)
    return out


def group_matrix(pose, group):
    """Model px of a group's frame -> E (blocks)."""
    if group.endswith("_in"):
        group = group[:-3]
    if group.endswith("_sleeve"):
        group = "body_upper"
    part = group.rsplit("_", 1)[0] if group.endswith(("_upper", "_lower")) else group
    lower = group.endswith("_lower")
    name = PART[part]
    if part.endswith("_leg"):
        p = pose.get(name, {})
        m = rig.body_to_entity(pose) @ rig.part_matrix(pose, name, rig.LEG_PIVOT[name])
        if lower:
            m = m @ rig.tr(0, 6, 0) @ rig.rx(rig.rad(p.get("bend", 0.0))) @ rig.tr(0, -6, 0)
        return m
    if group == "body_lower":
        return rig.body_to_entity(pose) @ rig.part_matrix(pose, "torso", (0, 0, 0))
    if part.endswith("_arm"):
        m = rig.layer_to_entity(pose) @ rig.part_matrix(pose, name, rig.ARM_PIVOT[name])
        return m @ rig.forearm_bend(pose, name) if lower else m
    return rig.layer_to_entity(pose) @ rig.part_matrix(pose, "torso" if part == "body" else "head", (0, 0, 0))


COATS = ("admiral_coat", "blackbeard_coat")


def apply(m, p):
    return (np.c_[p, np.ones(len(p))] @ m.T)[:, :3]


def coat_points(pose, verts, groups, quads, wind, slim=False):
    """3.8.9: a coat's body points in E as client/armor/ClothesModel places them (tools/coat_cloth.py's rule): the wind's bend and the
    torso and the legs kept out in the body's frame, then pushed out of the arms; {vertex: E point} for the body groups."""
    import coat_cloth as cc
    rest = verts[:, :3]
    upper = np.zeros(len(rest), bool)
    body = np.zeros(len(rest), bool)
    lining = np.zeros(len(rest), bool)
    sleeves = {}
    for gname, first, count in groups:
        idx = sorted({i for quad in quads[first:first + count] for i in quad})
        if gname.startswith("body_"):
            body[idx] = True
            upper[idx] = gname.startswith("body_upper")
            lining[idx] = gname.endswith("_in")
        elif gname.endswith("_sleeve"):
            sleeves[gname.split("_")[0]] = idx
    legs = {s: rig.rad(pose.get(s + "Leg", {}).get("pitch", 0.0)) for s in ("right", "left")}
    back, across, phase = wind
    moved = cc.body_moved(rest, legs, back, across, phase)
    for side, idx in sleeves.items():
        moved[idx] = cc.sleeve(rest[idx], side, back, across, phase)
        upper[idx] = True
    torso = rig.part_matrix(pose, "torso", (0, 0, 0))
    model = apply(torso, moved)
    frames = {}
    for side, name in (("right", "rightArm"), ("left", "leftArm")):
        arm = rig.part_matrix(pose, name, rig.ARM_PIVOT[name])
        frames[(side, "upper")] = arm
        frames[(side, "fore")] = arm @ rig.forearm_bend(pose, name)
    pushed = cc.keep_out(model, frames, rig.part_matrix(pose, "head", (0, 0, 0)), rest, cc.rest_ways(rest, slim), lining, slim)
    model = np.where(body[:, None], pushed, model)  # the sleeves are never touched by the arms
    out = np.where(upper[:, None], apply(rig.layer_to_entity(pose), model), apply(rig.body_to_entity(pose), model))
    on = body.copy()
    for idx in sleeves.values():
        on[idx] = True
    return out, on


def draw_piece(canvas, cam, pose, piece, tex, wind=(0.0, 0.0, 0.0)):
    verts, quads, groups = load(piece)
    v = np.array(verts, float)
    pos = v[:, :3].copy()
    world = None
    if piece in COATS:
        world, on_body = coat_points(pose, v, groups, quads, wind)
    for gname, first, count in groups:
        m = group_matrix(pose, gname)
        for quad in quads[first:first + count]:
            tri = [quad[0], quad[1], quad[2]]
            moving = world is not None and (gname.startswith("body_") or gname.endswith("_sleeve"))
            p = world[tri] if moving else np.c_[pos[tri], np.ones(3)] @ m.T
            s = cam.project(p[:, :3])
            if moving:
                n = np.cross(p[1, :3] - p[0, :3], p[2, :3] - p[0, :3])
            else:
                n = v[tri, 5:8].mean(axis=0) @ m[:3, :3].T
            n /= max(1e-9, np.linalg.norm(n))
            shade = 0.5 + 0.5 * abs(float(n @ sv.LIGHT))
            canvas.tri(s, uv=v[tri, 3:5], tex=tex, shade=shade)


def render(out, pose=None, pieces=None, views=("front", "three", "side", "back"), wind=(0.0, 0.0, 0.0)):
    pose = pose or {}
    tex = np.asarray(Image.open(os.path.join(DIR, "clothes.png")).convert("RGBA"), float) / 255.0
    sets = pieces or [["marine_cap", "marine_shirt", "marine_trousers", "marine_boots"], ["admiral_coat"], ["straw_hat"], ["blackbeard_coat"]]
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180}
    w, h = 230, 300
    img = Image.new("RGB", (w * len(views), h * len(sets)), (246, 244, 238))
    for r, group in enumerate(sets):
        for c, view in enumerate(views):
            cam = sv.Camera(yaw_of[view], 10.0, 135.0, w / 2, h * 0.93)
            cv = sv.Canvas(w, h)
            sv.draw_figure(cv, cam, pose)
            for piece in group:
                draw_piece(cv, cam, pose, piece, tex, wind)
            tile = cv.image()
            ImageDraw.Draw(tile).text((4, 3), f"{'+'.join(p.split('_')[0] for p in group)} {view}", fill=(60, 60, 60))
            img.paste(tile, (c * w, r * h))
    img.save(out)


if __name__ == "__main__":
    pose = None
    if len(sys.argv) > 3:
        import anim
        pose = anim.load(sys.argv[2]).pose(float(sys.argv[3]))
    render(sys.argv[1], pose)
