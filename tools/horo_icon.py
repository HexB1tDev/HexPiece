#!/usr/bin/env python3
"""3.3.6: the Horo Horo no Mi's inventory icon (32 x 32, the original fruits' style as tools/uo_icon.py, tools/hie_icon.py,
tools/noro_icon.py and tools/supa_icon.py draw them: a shaded fruit, a dark outline): after the owner's reference picture, a cluster of
cream-yellow lobes, each a little ghost (round, its tip curling up and over to one side), two of them with tiny faces (dot eyes, a small
open mouth), a short stem on top. Pillow.

Usage: python3 tools/horo_icon.py src/main/resources/assets/hexhaki/textures/item/horo_horo_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (243, 227, 166)
DARK = (196, 170, 96)
LIGHT = (255, 248, 214)
OUTLINE = (58, 42, 20)
FACE = (52, 36, 34)
STEM = (96, 132, 60)

# lobes: (centre x, centre y, radius, tip direction (radians, 0 right, -pi/2 up), face)
LOBES = [
    (11.0, 21.0, 7.2, -2.2, True),
    (20.5, 21.5, 7.0, -1.0, False),
    (15.8, 14.0, 6.6, -1.7, True),
    (16.0, 24.5, 6.0, 0.4, False),
]


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    owner = {}
    for idx, (cx, cy, r, tip, _) in enumerate(LOBES):
        tx, ty = math.cos(tip), math.sin(tip)
        for y in range(N):
            for x in range(N):
                ux, uy = x + 0.5 - cx, y + 0.5 - cy
                d = math.hypot(ux, uy)
                along = ux * tx + uy * ty
                # a round lobe drawn out to a pointed tip, the point curling round (a ghost's tail)
                stretch = 1.0 + 0.7 * max(0.0, along / r) ** 2
                side = -ux * ty + uy * tx
                curl = side + 0.35 * max(0.0, along) ** 2 / r
                inside = math.hypot(along / stretch, curl) < r and along < r * 1.45
                if not inside:
                    continue
                z = math.sqrt(max(0.0, 1.0 - (d / (r * 1.2)) ** 2))
                lit = max(0.0, -0.5 * ux / r - 0.6 * uy / r + 0.6 * z)
                owner[(x, y)] = (idx, lit)
    for (x, y), (idx, lit) in owner.items():
        step = math.floor(min(1.0, 0.3 + lit * 0.9) * 4.0) / 4.0
        col = mix(DARK, BASE, step)
        if lit > 0.75:
            col = mix(col, LIGHT, 0.7)
        px[x, y] = col + (255,)
    # where one lobe meets another: a thin darker crease
    for (x, y), (idx, _) in owner.items():
        for ox, oy in ((1, 0), (0, 1)):
            q = owner.get((x + ox, y + oy))
            if q is not None and q[0] != idx:
                px[x, y] = mix(DARK, OUTLINE, 0.35) + (255,)
    # faces on two lobes: dot eyes and a small open mouth
    for cx, cy, r, _, face in LOBES:
        if not face:
            continue
        for ex in (-2, 2):
            px[int(cx + ex), int(cy - 1)] = FACE + (255,)
        px[int(cx), int(cy + 1)] = FACE + (255,)
        px[int(cx), int(cy + 2)] = (214, 110, 120, 255)
    for y in range(N):
        for x in range(N):
            if (x, y) in owner:
                continue
            if any((x + ox, y + oy) in owner for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # a short stem growing out of the top of the cluster, leaning right
    top = min(y for (x, y) in owner if x == 17)
    for k in range(4):
        x, y = 17 + k // 2, top - 1 - k
        if 0 <= y:
            px[x, y] = STEM + (255,)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
