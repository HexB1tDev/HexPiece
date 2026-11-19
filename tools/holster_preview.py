#!/usr/bin/env python3
"""2.5.4: renders the first-person view of a sheathed katana with the left hand resting on it (sword/HolsterView, drawn in game by
client/sword/HolsterHand) to judge it: the game's own steps (dumped by tools/preview/HolsterDump.java from build/classes), the owner's
katana and saya meshes (tools/sword_view.py), the left arm as the game's renderLeftHand draws it (its box from the pivot, the idle
sway), seen by the first-person hand camera (70 degrees from the top of the view to its bottom), phone-wide and 16:9.

Usage: python3 tools/holster_preview.py [out.png] [katana_key]     (run python3 build.py first)
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


def steps(age=0.0):
    classes = os.path.join(ROOT, "build", "classes")
    preview = os.path.join(ROOT, "build", "preview")
    os.makedirs(preview, exist_ok=True)
    subprocess.run(["javac", "-cp", classes, "-d", preview, os.path.join(ROOT, "tools", "preview", "HolsterDump.java")], check=True,
                   stderr=subprocess.DEVNULL)
    out = subprocess.run(["java", "-cp", preview + os.pathsep + classes, "HolsterDump", str(age)], check=True, capture_output=True,
                         text=True).stdout
    return json.loads(out)


def matrix(ops):
    m = np.eye(4)
    for s in ops:
        t = np.eye(4)
        if s[0] == 0:
            t[:3, 3] = s[1:4]
        else:
            a = math.radians(s[1])
            c, n = math.cos(a), math.sin(a)
            if s[0] == 1:
                t[1:3, 1:3] = [[c, -n], [n, c]]
            elif s[0] == 2:
                t[0, 0], t[0, 2], t[2, 0], t[2, 2] = c, n, -n, c
            else:
                t[0:2, 0:2] = [[c, -n], [n, c]]
        m = m @ t
    return m


def arm_box(canvas, cam, m, slim, black=True):
    """The left arm as renderLeftHand draws it: the part at its pivot, swayed by the idle roll, its box (model px)."""
    pivot = (5.0, 2.5 if slim else 2.0, 0.0)
    part = m.copy()
    t = np.eye(4)
    t[:3, 3] = np.array(pivot) / 16.0
    part = part @ t
    r = np.eye(4)
    a = -0.1
    r[0:2, 0:2] = [[math.cos(a), -math.sin(a)], [math.sin(a), math.cos(a)]]
    part = part @ r
    x0, x1 = -1.0, 2.0 if slim else 3.0
    skin, coat = (204, 153, 122), (14, 14, 16)
    rows = [(-2.0, 6.0, skin), (6.0, 7.0, tuple(c * 0.35 + 5 for c in skin)), (7.0, 10.0, coat if black else skin)]
    for y0, y1, colour in rows:
        corners = [(x, y, z) for x in (x0, x1) for y in (y0, y1) for z in (-2.0, 2.0)]
        p = np.array([(c[0] / 16, c[1] / 16, c[2] / 16, 1.0) for c in corners]) @ part.T
        faces = [(0, 1, 3, 2), (4, 5, 7, 6), (0, 1, 5, 4), (2, 3, 7, 6), (0, 2, 6, 4), (1, 3, 7, 5)]
        centre = p[:, :3].mean(axis=0)
        for f in faces:
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


def view(w, h, data, key, slim=False, black=True, arm=True):
    canvas = sv.Canvas(w, h, bg=(150, 190, 235))
    # the ground's horizon band below, for a sense of the view
    canvas.rgb[int(h * 0.52):, :] = np.array((96, 140, 72)) / 255.0
    cam = sv.EyeCamera(w, h, pitch=0.0, fov=70.0, eye=0.0)
    k = matrix(data["katana"]) @ np.diag([1.25 / 16, 1.25 / 16, 1.25 / 16, 1])  # 2.6.2: Katana.SIZE
    sv.draw_katana(canvas, cam, key, k, saya=True, blade=True, shine=0.0)
    if arm:
        arm_box(canvas, cam, matrix(data["slim" if slim else "wide"]), slim, black)
    return Image.fromarray((np.clip(canvas.rgb, 0, 1) * 255).astype(np.uint8))


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "build", "holster.png")
    key = sys.argv[2] if len(sys.argv) > 2 else "crimson_dawn"
    data = steps()
    shots = [(view(660, 300, data, key), "phone-wide, Haki on"), (view(533, 300, data, key, black=False), "16:9, no Haki"),
             (view(660, 300, data, key, slim=True), "slim arm"), (view(660, 300, data, key, arm=False), "the hilt alone")]
    sheet = Image.new("RGB", (660 + 533, 640), (20, 20, 20))
    d = ImageDraw.Draw(sheet)
    for i, (img, label) in enumerate(shots):
        x, y = (i % 2) * 660, (i // 2) * 320
        sheet.paste(img, (x, y + 18))
        d.text((x + 6, y + 3), label, fill=(230, 230, 230))
    sheet.save(out)
    print("wrote", out)


if __name__ == "__main__":
    main()
