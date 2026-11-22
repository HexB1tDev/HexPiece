#!/usr/bin/env python3
"""3.1.9: the Hie Hie no Mi's inventory icon (32 x 32, the original fruits' style as tools/uo_icon.py draws the Uo Uo no Mi: a shaded
round fruit swirled all over, a dark outline, a curled stem and a leaf): pale ice blue going white where the light falls, its swirls
white, faceted glints of frost in the shade, a frosted grey-blue stem and a pale leaf. Pillow.

Usage: python3 tools/hie_icon.py src/main/resources/assets/hexhaki/textures/item/hie_hie_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (112, 196, 236)
DARK = (34, 92, 150)
LIGHT = (214, 244, 255)
SWIRL = (244, 252, 255)
FROST = (178, 232, 252)
OUTLINE = (12, 34, 62)
STEM = (74, 96, 112)
LEAF = (126, 206, 196)
LEAF_DARK = (62, 138, 140)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    cx, cy, r = 15.5, 17.5, 12.2
    inside = set()
    for y in range(N):
        for x in range(N):
            dx, dy = (x + 0.5 - cx) / r, (y + 0.5 - cy) / (r * 0.96)
            d = dx * dx + dy * dy
            if d > 1.0:
                continue
            inside.add((x, y))
            z = math.sqrt(max(0.0, 1.0 - d))
            # light from the upper left, in hard cel steps (his ice is cut, never soft)
            lit = max(0.0, -0.55 * dx - 0.6 * dy + 0.58 * z)
            step = math.floor(min(1.0, 0.25 + lit * 1.1) * 4.0) / 4.0
            col = mix(DARK, BASE, step)
            if lit > 0.74:
                col = mix(col, LIGHT, 0.85)
            # swirls: two spirals wound about the fruit's middle, white as frost
            a = math.atan2(dy, dx)
            rr = math.sqrt(d)
            for k in (0.0, math.pi):
                wave = math.sin(a + k - rr * 7.2)
                if wave > 0.82 and 0.12 < rr < 0.93:
                    col = mix(col, SWIRL, 0.6 + 0.35 * lit)
            # glints of frost in the shade: little diagonal facets
            if lit < 0.4 and (x * 3 + y * 5) % 11 == 0:
                col = mix(col, FROST, 0.55)
            px[x, y] = col + (255,)
    # the outline: every empty pixel touching the fruit
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the stem, curled, and a leaf to the right (the same weight as the original fruits')
    stem = [(15, 5), (16, 5), (15, 4), (16, 4), (16, 3), (17, 3), (17, 2), (18, 2)]
    leaf = [(19, 3), (20, 3), (21, 3), (22, 3), (23, 3), (20, 2), (21, 2), (22, 2), (23, 2), (24, 2), (21, 1), (22, 1), (23, 1),
            (20, 4), (21, 4), (22, 4), (23, 4), (21, 5), (22, 5)]
    vein = [(20, 3), (21, 3), (22, 3), (23, 2)]
    for x, y in stem:
        px[x, y] = STEM + (255,)
    for x, y in leaf:
        px[x, y] = (LEAF if y <= 2 or x < 22 and y == 3 else LEAF_DARK) + (255,)
    for x, y in vein:
        px[x, y] = LEAF_DARK + (255,)
    for x, y in stem + leaf:
        for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + ox, y + oy)
            if 0 <= q[0] < N and 0 <= q[1] < N and px[q][3] == 0:
                px[q] = OUTLINE + (255,)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
