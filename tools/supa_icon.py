#!/usr/bin/env python3
"""3.3.5: the Supa Supa no Mi's inventory icon (32 x 32, the original fruits' style as tools/uo_icon.py, tools/hie_icon.py and
tools/noro_icon.py draw them: a shaded fruit patterned all over, a dark outline): after the owner's reference picture (blue and white
crescent blades), a round fruit, pale steel blue going white where the light falls, covered in crescent blades (white, edged deep blue,
set round it like a pinwheel), a short curled stem and a leaf on top. Pillow.

Usage: python3 tools/supa_icon.py src/main/resources/assets/hexhaki/textures/item/supa_supa_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (112, 150, 204)
DARK = (52, 82, 142)
LIGHT = (196, 220, 244)
BLADE = (240, 246, 252)
EDGE = (30, 52, 108)
OUTLINE = (14, 22, 48)
STEM = (70, 52, 40)
LEAF = (74, 160, 92)
LEAF_DARK = (36, 102, 56)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    cx, cy, r = 15.5, 18.0, 12.2
    inside = set()
    for y in range(N):
        for x in range(N):
            ux, uy = (x + 0.5 - cx) / r, (y + 0.5 - cy) / (r * 0.94)
            d = ux * ux + uy * uy
            if d > 1.0:
                continue
            inside.add((x, y))
            z = math.sqrt(max(0.0, 1.0 - d))
            lit = max(0.0, -0.45 * ux - 0.5 * uy + 0.55 * z)
            step = math.floor(min(1.0, 0.25 + lit * 0.95) * 4.0) / 4.0
            col = mix(DARK, BASE, step)
            if lit > 0.75:
                col = mix(col, LIGHT, 0.65)
            # crescent blades: a pinwheel of five round the middle and one in it, each a disc less a disc set off from it (its points
            # curling the same way round), white with a deep blue edge
            for k in range(6):
                if k < 5:
                    th = k * 2.0 * math.pi / 5.0 + 0.5
                    bx, by, br = 0.56 * math.cos(th), 0.56 * math.sin(th), 0.36
                    off = th + math.pi * 0.62
                else:
                    bx, by, br, off = 0.0, 0.02, 0.3, -0.9
                ox, oy = bx + 0.26 * math.cos(off), by + 0.26 * math.sin(off)
                da = math.hypot(ux - bx, uy - by)
                db = math.hypot(ux - ox, uy - oy)
                if da < br and db > br:
                    edge = da > br - 0.065 or db < br + 0.05
                    col = EDGE if edge else (BLADE if lit > 0.2 else mix(BLADE, BASE, 0.3))
            px[x, y] = col + (255,)
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # a short stem curling out of the top, a leaf to its right
    top = []
    for t in range(12):
        s = t / 11.0
        top.append((int(round(15.6 + 2.2 * math.sin(s * 2.6))), int(round(6.2 - 4.6 * s))))
    for x, y in top:
        if (x, y) not in inside and 0 <= y:
            px[x, y] = STEM + (255,)
    for y in range(N):
        for x in range(N):
            lx, ly = x + 0.5 - 22.0, y + 0.5 - 4.2
            u, v = lx * 0.83 + ly * 0.55, -lx * 0.55 + ly * 0.83
            if (u / 4.6) ** 2 + (v / 2.0) ** 2 <= 1.0 and (x, y) not in inside and px[x, y][3] == 0:
                px[x, y] = (LEAF if v < 0.2 else LEAF_DARK) + (255,)
    for y in range(N):
        for x in range(N):
            if px[x, y][3] != 0 or (x, y) in inside:
                continue
            if any(0 <= x + ox < N and 0 <= y + oy < N and px[x + ox, y + oy][3] != 0 and px[x + ox, y + oy][:3] in (STEM, LEAF, LEAF_DARK)
                   for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
