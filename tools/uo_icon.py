#!/usr/bin/env python3
"""2.7.0: the Uo Uo no Mi's inventory icon (32 x 32, the original fruits' style: a shaded round fruit swirled all over, a dark outline,
a curled stem and a leaf): azure, its swirls pale, a scale pattern faintly in the shade. Pillow.

Usage: python3 tools/uo_icon.py src/main/resources/assets/hexhaki/textures/item/uo_uo_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (42, 128, 204)
DARK = (14, 46, 104)
LIGHT = (142, 214, 250)
SWIRL = (198, 238, 255)
OUTLINE = (10, 22, 48)
STEM = (70, 52, 30)
LEAF = (64, 160, 72)
LEAF_DARK = (34, 102, 46)


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
            # light from the upper left
            lit = max(0.0, -0.55 * dx - 0.6 * dy + 0.58 * z)
            col = mix(DARK, BASE, min(1.0, 0.25 + lit * 1.1))
            if lit > 0.78:
                col = mix(col, LIGHT, min(1.0, (lit - 0.78) * 3.2))
            # swirls: two spirals wound about the fruit's middle
            a = math.atan2(dy, dx)
            rr = math.sqrt(d)
            for k in (0.0, math.pi):
                wave = math.sin(a + k - rr * 7.2)
                if wave > 0.82 and 0.12 < rr < 0.93:
                    col = mix(col, SWIRL, 0.55 + 0.4 * lit)
            # scales, faint, in the shade
            if lit < 0.35 and (x + (y // 2) % 2) % 3 == 0 and y % 2 == 0:
                col = mix(col, DARK, 0.35)
            px[x, y] = col + (255,)
    # the outline: every empty pixel touching the fruit
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the stem, curled, and a leaf to the right (the original fruits' weight: a two-pixel stem, a leaf of about seven by five)
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
