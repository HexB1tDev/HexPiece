#!/usr/bin/env python3
"""3.5.5: the Mochi Mochi no Mi's inventory icon (32 x 32, the original fruits' style as tools/ito_icon.py and tools/horo_icon.py draw
them: a shaded fruit, a dark outline), after the owner's picture: a cream-white gourd (a big round body over a narrow waist and a smaller
round bottom) covered in grey swirls, a green T-shaped stem curling into a spiral at its left end, its right end bent down to an open
tip. Pillow.

Usage: python3 tools/mochi_icon.py src/main/resources/assets/hexhaki/textures/item/mochi_mochi_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (248, 244, 234)
DARK = (198, 190, 196)
LIGHT = (255, 255, 255)
OUTLINE = (34, 30, 36)
SWIRL = (168, 162, 170)
STEM = (150, 206, 60)
STEM_DARK = (86, 140, 34)
STEM_LIGHT = (200, 238, 120)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


TOP = (16.0, 13.4, 9.0)
BOTTOM = (16.0, 25.2, 5.8)


def gourd(x, y):
    """Inside the gourd: the big round body, the small round bottom, and the waist between them (pinched in)."""
    if math.hypot(x - TOP[0], y - TOP[1]) < TOP[2] or math.hypot(x - BOTTOM[0], y - BOTTOM[1]) < BOTTOM[2]:
        return True
    if TOP[1] < y < BOTTOM[1]:
        t = (y - TOP[1]) / (BOTTOM[1] - TOP[1])
        # the waist: a smooth neck, narrowest a little above the bottom bulb (never pinched into two balls)
        half = 4.7 + 5.5 * (t - 0.62) ** 2
        return abs(x - 16.0) < min(half, 8.6)
    return False


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    inside = {}
    for y in range(N):
        for x in range(N):
            if gourd(x + 0.5, y + 0.5):
                # lit from the upper left: each bulb shaded as a ball
                cx, cy, r = TOP if y + 0.5 < 21.0 else BOTTOM
                ux, uy = (x + 0.5 - cx) / r, (y + 0.5 - cy) / r
                z = math.sqrt(max(0.0, 1.0 - min(1.0, ux * ux + uy * uy)))
                inside[(x, y)] = max(0.0, -0.5 * ux - 0.55 * uy + 0.65 * z)
    for (x, y), lit in inside.items():
        step = math.floor(min(1.0, 0.3 + lit * 0.9) * 4.0) / 4.0
        col = mix(DARK, BASE, step)
        if lit > 0.82:
            col = mix(col, LIGHT, 0.75)
        px[x, y] = col + (255,)
    # the swirls: grey spirals over the skin (the Devil Fruit pattern), each turning about its own middle
    for cx, cy, turns, size in ((11.0, 10.5, 1.05, 2.2), (17.5, 8.0, 1.0, 2.0), (21.0, 12.5, 1.05, 2.2), (15.0, 14.0, 1.0, 2.0),
                                (10.5, 17.0, 1.0, 2.0), (19.5, 18.5, 1.0, 2.0), (15.0, 21.5, 0.9, 1.6), (13.5, 26.0, 1.0, 1.9),
                                (18.5, 27.5, 0.9, 1.6)):
        steps = 48
        # an open curl: its middle left clear (a filled spiral reads as a smudge at this size)
        for k in range(int(steps * 0.4), steps):
            a = turns * 2 * math.pi * k / steps
            r = size * k / steps
            x = int(round(cx + math.cos(a) * r - 0.5))
            y = int(round(cy + math.sin(a) * r * 0.9 - 0.5))
            if (x, y) in inside and inside[(x, y)] < 0.95:
                px[x, y] = SWIRL + (255,)
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the green T-shaped stem: up out of the top, a bar across, its left end curling into a spiral, its right end bent down to an
    # open tip
    top = min(y for (x, y) in inside if x == 16)
    stem = [(16, top - 1), (16, top - 2), (15, top - 2)]
    bar_y = top - 3
    stem += [(x, bar_y) for x in range(9, 25)]
    stem += [(8, bar_y + 1), (7, bar_y + 1), (6, bar_y), (6, bar_y - 1), (7, bar_y - 2), (8, bar_y - 2), (9, bar_y - 1)]
    stem += [(25, bar_y + 1), (25, bar_y + 2), (26, bar_y + 2)]
    shade = {p: STEM for p in stem}
    for x in range(10, 24):
        shade[(x, bar_y)] = STEM_LIGHT if x % 4 == 1 else STEM
    shade[(16, top - 1)] = STEM_DARK
    shade[(26, bar_y + 2)] = STEM_DARK
    for (x, y) in stem:
        if 0 <= y < N and 0 <= x < N:
            px[x, y] = shade[(x, y)] + (255,)
    for (x, y) in stem:
        for ox, oy in ((1, 0), (-1, 0), (0, -1), (0, 1)):
            q = (x + ox, y + oy)
            if 0 <= q[0] < N and 0 <= q[1] < N and q not in shade and q not in inside and px[q[0], q[1]][3] == 0:
                px[q[0], q[1]] = OUTLINE + (255,)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
