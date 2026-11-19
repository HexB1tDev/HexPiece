#!/usr/bin/env python3
"""2.5.6: renders the first-person draw and sheathing (2.6.1: and the right click's charge) (sword/DrawView, played in game by client/sword/HolsterHand) to judge them: the
game's own frames (dumped by tools/preview/DrawDump.java from build/classes), the owner's katana and saya meshes (tools/sword_view.py),
the arms as the game's renderLeftHand/renderRightHand draw them, seen by the first-person hand camera (70 degrees, phone-wide).

Usage: python3 tools/draw_preview.py OUT_DIR [katana_key] [--step 1.0] [--slim]     (run python3 build.py first)
"""
import json
import math
import os
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_view as sv  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKIN, COAT = (204, 153, 122), (16, 16, 18)


def dump(key, step, slim):
    classes = os.path.join(ROOT, "build", "classes")
    preview = os.path.join(ROOT, "build", "preview")
    os.makedirs(preview, exist_ok=True)
    subprocess.run(["javac", "-cp", classes, "-d", preview, os.path.join(ROOT, "tools", "preview", "DrawDump.java")], check=True,
                   stderr=subprocess.DEVNULL)
    args = ["java", "-cp", preview + os.pathsep + classes, "DrawDump", key, str(step)] + (["slim"] if slim else [])
    return json.loads(subprocess.run(args, check=True, capture_output=True, text=True).stdout)


def matrix(ops):
    m = np.eye(4)
    for s in ops:
        t = np.eye(4)
        kind = int(s[0])
        if kind == 0:
            t[:3, 3] = s[1:4]
        elif kind == 4:
            a = math.radians(s[1])
            ax = np.array(s[2:5], float)
            ax /= np.linalg.norm(ax)
            x, y, z = ax
            c, n, k = math.cos(a), math.sin(a), 1 - math.cos(a)
            t[:3, :3] = [[k * x * x + c, k * x * y - n * z, k * x * z + n * y], [k * x * y + n * z, k * y * y + c, k * y * z - n * x],
                         [k * x * z - n * y, k * y * z + n * x, k * z * z + c]]
        else:
            a = math.radians(s[1])
            c, n = math.cos(a), math.sin(a)
            if kind == 1:
                t[1:3, 1:3] = [[c, -n], [n, c]]
            elif kind == 2:
                t[0, 0], t[0, 2], t[2, 0], t[2, 2] = c, n, -n, c
            else:
                t[0:2, 0:2] = [[c, -n], [n, c]]
        m = m @ t
    return m


def arm(canvas, cam, ops, right, slim):
    """The arm as renderHand draws it: the part at its pivot, swayed, its box (model px); the hand black (Haki)."""
    m = matrix(ops)
    pivot = ((-5.0 if right else 5.0), 2.5 if slim else 2.0, 0.0)
    sway = 0.1 if right else -0.1
    t = np.eye(4)
    t[:3, 3] = np.array(pivot) / 16.0
    r = np.eye(4)
    r[0:2, 0:2] = [[math.cos(sway), -math.sin(sway)], [math.sin(sway), math.cos(sway)]]
    part = m @ t @ r
    w = 3.0 if slim else 4.0
    x0, x1 = (-w + 1.0, 1.0) if right else (-1.0, w - 1.0)
    for y0, y1, colour in ((-2.0, 6.0, SKIN), (6.0, 7.0, tuple(c * 0.35 + 5 for c in SKIN)), (7.0, 10.0, COAT)):
        corners = [(x, y, z) for x in (x0, x1) for y in (y0, y1) for z in (-2.0, 2.0)]
        p = np.array([(c[0] / 16, c[1] / 16, c[2] / 16, 1.0) for c in corners]) @ part.T
        centre = p[:, :3].mean(axis=0)
        for f in ((0, 1, 3, 2), (4, 5, 7, 6), (0, 1, 5, 4), (2, 3, 7, 6), (0, 2, 6, 4), (1, 3, 7, 5)):
            q = p[list(f), :3]
            n = np.cross(q[1] - q[0], q[3] - q[0])
            n /= np.linalg.norm(n)
            if np.dot(n, q.mean(axis=0) - centre) < 0:
                n = -n
            shade = 0.5 + 0.5 * max(0.0, float(np.dot(n, sv.LIGHT)))
            s = cam.project(q)
            col = tuple(c * shade for c in colour)
            canvas.tri([s[0], s[1], s[2]], col)
            canvas.tri([s[0], s[2], s[3]], col)


def frame(pose, key, w, h, slim):
    canvas = sv.Canvas(w, h, bg=(150, 190, 235))
    canvas.rgb[int(h * 0.52):, :] = np.array((96, 140, 72)) / 255.0
    cam = sv.EyeCamera(w, h, pitch=0.0, fov=70.0, eye=0.0)
    # 2.6.2: every blade but Yoru drawn sword_rig.SIZE times its mesh (Katana.size(), KatanaRenderer.px)
    import sword_rig
    size = 1.0 if key == "yoru" else sword_rig.SIZE
    px = np.diag([size / 16, size / 16, size / 16, 1])
    if pose["saya"] is not None:
        sv.draw_katana(canvas, cam, key, matrix(pose["saya"]) @ px, saya=True, blade=False, shine=0.0)
    if pose["blade"] is not None:
        sv.draw_katana(canvas, cam, key, matrix(pose["blade"]) @ px, saya=False, blade=True, shine=pose["shine"])
    if pose["left"] is not None:
        arm(canvas, cam, pose["left"], False, slim)
    if pose["right"] is not None:
        arm(canvas, cam, pose["right"], True, slim)
    img = Image.fromarray((np.clip(canvas.rgb, 0, 1) * 255).astype(np.uint8))
    ImageDraw.Draw(img).text((4, 2), f"t{pose['t']:g}", fill=(20, 20, 20))
    return img


def sheet(poses, key, slim, cols, w=264, h=120):
    rows = (len(poses) + cols - 1) // cols
    out = Image.new("RGB", (cols * w, rows * h), (20, 20, 20))
    for i, pose in enumerate(poses):
        out.paste(frame(pose, key, w, h, slim), ((i % cols) * w, (i // cols) * h))
    return out


def main():
    out = sys.argv[1]
    key = sys.argv[2] if len(sys.argv) > 2 and not sys.argv[2].startswith("--") else "crimson_dawn"
    step = float(sys.argv[sys.argv.index("--step") + 1]) if "--step" in sys.argv else 1.0
    slim = "--slim" in sys.argv
    os.makedirs(out, exist_ok=True)
    data = dump(key, step, slim)
    sheet(data["draw"], key, slim, 6).save(os.path.join(out, "draw.png"))
    sheet(data["sheathe"], key, slim, 6).save(os.path.join(out, "sheathe.png"))
    # 2.6.1: the right click's charge (from the held blade, and drawn from the saya), every other tick of its first 40
    sheet(data["charge"][::2], key, slim, 6).save(os.path.join(out, "charge.png"))
    sheet(data["charge_draw"][::2], key, slim, 6).save(os.path.join(out, "charge_draw.png"))
    frame(dict(data["held"], t=0), key, 660, 300, slim).save(os.path.join(out, "held.png"))
    frame(dict(data["holster"], t=0), key, 660, 300, slim).save(os.path.join(out, "holster.png"))
    print("wrote", out)


if __name__ == "__main__":
    main()
