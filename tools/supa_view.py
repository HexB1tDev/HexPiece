#!/usr/bin/env python3
"""3.3.5: the Supa Supa no Mi's blades off-game, made of the owner's katana steel (the owner chose "Katana steel" after the first,
flat blades: "it honestly all looks really poor and poorly made"). A katana's steel alone (its 01_Blade part, the owner's own mesh and
texture, lit as the game lights it: tools/sword_view.py) reshaped for Daz Bones by the same rule client/sword/KatanaMesh.shaped uses:

  every 0.5 px band of the blade along its length: its spine (the most +x of it) and its edge (the most -x); a vertex d = spine - x in
  from the spine, u = d / (spine - edge) of the way to the edge;
  x' = spine * curve - d * widen - wave * sin(2 pi y / period) * u^2      (wider about its own spine, its curve eased, the edge rolling)
  y' = y * length,  z' = z * thick

then set on a limb (sword_rig's chain, the game's own) and drawn beside the figure. Armament's coat multiplies the steel toward the
katanas' very dark purple (62, 34, 88), its glint dimmed, as client/sword/KatanaRenderer.coated does.

Usage: python3 tools/supa_view.py out.png [--key azure_tide] [--coat 0..1] [--views front,three,side,back]
"""
import argparse
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_rig as rig  # noqa: E402
import sword_view as sv  # noqa: E402

COAT = np.array([62, 34, 88], float) / 255.0

# the shapes (client/supa/SupaBlades keeps the same numbers): widen, thick, length, wave, period, curve; then where on the limb
ARM = dict(widen=4.2, thick=2.6, length=0.82, wave=0.45, period=7.5, curve=0.4)
ARM_AT = (-1.6, 3.2, 0.0)   # the guard end on the arm (px, the arm's frame: x out, y down the arm)
ARM_TURN = 30.0             # the blade's section turned round the arm (degrees): never edge-on from the side


def shaped(key, s):
    """The katana's blade part reshaped: (verts with x y z u v nx ny nz, the blade's triangles)."""
    v, t, blade = sv.mesh(key)
    tris = t[blade]
    idx = np.unique(tris)
    x, y = v[:, 0], v[:, 1]
    band = np.floor(y / 0.5).astype(int)
    spine = {}
    edge = {}
    for i in idx:
        k = band[i]
        spine[k] = max(spine.get(k, -1e9), x[i])
        edge[k] = min(edge.get(k, 1e9), x[i])
    out = v.copy()
    for i in idx:
        k = band[i]
        sp = spine[k]
        w = max(0.05, sp - edge[k])
        d = sp - x[i]
        u = min(1.0, max(0.0, d / w))
        out[i, 0] = sp * s["curve"] - d * s["widen"] - s["wave"] * math.sin(2 * math.pi * y[i] / s["period"]) * u * u
        out[i, 1] = y[i] * s["length"]
        out[i, 2] = v[i, 2] * s["thick"]
    return out, tris


def draw_steel(canvas, cam, key, verts, tris, matrix, shine=0.7, coat=0.0):
    """sword_view.draw_katana's drawing for these verts and triangles (all steel), the coat multiplied in."""
    tex = sv.texture(key).copy()
    if coat > 0:
        tex[..., :3] *= (1.0 - coat) + coat * COAT
        shine *= 1.0 - 0.75 * coat
    p = np.c_[verts[:, :3], np.ones(len(verts))] @ matrix.T
    n = verts[:, 5:8] @ matrix[:3, :3].T
    n /= np.maximum(1e-9, np.linalg.norm(n, axis=1, keepdims=True))
    s = cam.project(p[:, :3])
    uv = verts[:, 3:5]
    tri_s = s[tris]
    tn = n[tris].mean(axis=1)
    tn /= np.maximum(1e-9, np.linalg.norm(tn, axis=1, keepdims=True))
    light = 0.45 + 0.55 * np.abs(tn @ sv.LIGHT)
    refl = tn @ (cam.rot.T @ np.array([0, 1.0, 0]))
    gleam = np.clip(np.abs(refl) - 0.35, 0, 1) ** 2 * shine * 1.6
    for t in range(len(tris)):
        canvas.tri(tri_s[t], uv=uv[tris[t]], tex=tex, shade=light[t], extra=gleam[t])


def arm_matrix(pose, side, at=ARM_AT):
    """Steel px -> E: on the {side} arm, the guard end at {at} (the left arm mirrored)."""
    part = side + "Arm"
    m = rig.layer_to_entity(pose) @ rig.part_matrix(pose, part, rig.ARM_PIVOT[part])
    turn = rig.ry(math.radians(ARM_TURN))
    if side == "right":
        return m @ rig.tr(*at) @ turn
    return m @ rig.tr(-at[0], at[1], at[2]) @ rig.sc(-1, 1, 1) @ turn


POSES = {
    "rest": {"rightArm": {"pitch": -8, "roll": 6}, "leftArm": {"pitch": 6, "roll": -6}},
    "forward": {"rightArm": {"pitch": -90}, "leftArm": {"pitch": -20, "roll": -12}},
    "swipe": {"rightArm": {"pitch": -85, "yaw": 40}, "leftArm": {"pitch": 10, "roll": -8}, "body": {"yaw": -20}},
}


def render(out, key, coat, views):
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180}
    w, h = 300, 300
    verts, tris = shaped(key, ARM)
    rows = []
    for name, pose in POSES.items():
        row = Image.new("RGB", (w * (len(views) + 1), h), (246, 244, 238))
        for i, view in enumerate(views):
            cam = sv.Camera(yaw_of[view], 12.0, 92.0, w / 2, h * 0.88)
            cv = sv.Canvas(w, h)
            sv.draw_figure(cv, cam, pose)
            draw_steel(cv, cam, key, verts, tris, arm_matrix(pose, "right"), coat=coat)
            draw_steel(cv, cam, key, verts, tris, arm_matrix(pose, "left"), coat=coat)
            tile = cv.image()
            ImageDraw.Draw(tile).text((4, 3), f"{name} {view}", fill=(60, 60, 60))
            row.paste(tile, (i * w, 0))
        # beside it: the same katana in a Swordsman's hand, for its steel
        sword = {"rightArm": {"pitch": -60}}
        cam = sv.Camera(35, 12.0, 92.0, w / 2, h * 0.88)
        cv = sv.Canvas(w, h)
        sv.draw_figure(cv, cam, sword)
        sv.draw_katana(cv, cam, key, rig.blade_matrix(sword))
        tile = cv.image()
        ImageDraw.Draw(tile).text((4, 3), f"the katana ({key})", fill=(60, 60, 60))
        row.paste(tile, (len(views) * w, 0))
        rows.append(row)
    img = Image.new("RGB", (rows[0].width, h * len(rows)), (246, 244, 238))
    for i, r in enumerate(rows):
        img.paste(r, (0, i * h))
    img.save(out)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("out")
    p.add_argument("--key", default="azure_tide")
    p.add_argument("--coat", type=float, default=0.0)
    p.add_argument("--views", default="front,three,side,back")
    a = p.parse_args()
    render(a.out, a.key, a.coat, a.views.split(","))


if __name__ == "__main__":
    main()
