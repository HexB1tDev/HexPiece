#!/usr/bin/env python3
"""3.3.7: the Ito Ito no Mi's inventory icon (32 x 32, the original fruits' style as tools/uo_icon.py and tools/horo_icon.py draw
them: a shaded fruit, a dark outline): after the owner's reference picture, a white pear-shaped fruit covered in swirls outlined in
black, a curly pink stem on top. Pillow.

Usage: python3 tools/ito_icon.py src/main/resources/assets/hexhaki/textures/item/ito_ito_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (236, 234, 240)
DARK = (176, 170, 188)
LIGHT = (255, 255, 255)
OUTLINE = (30, 26, 36)
SWIRL = (40, 36, 48)
STEM = (232, 112, 170)
STEM_DARK = (168, 62, 118)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def pear(x, y):
    """Inside the pear: a round bottom (its middle at 16, 20, radius 9.5) narrowing to a smaller top (16, 11, radius 6)."""
    t = min(1.0, max(0.0, (21.0 - y) / 12.0))
    cy = 21.0 - 11.0 * t
    r = 9.4 - 5.0 * t ** 1.4
    return math.hypot(x - 16.0, (y - cy) * 1.05) < r and 5.5 < y < 30.6


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    inside = {}
    for y in range(N):
        for x in range(N):
            if pear(x + 0.5, y + 0.5):
                ux, uy = (x + 0.5 - 16.0) / 9.5, (y + 0.5 - 19.0) / 11.0
                z = math.sqrt(max(0.0, 1.0 - ux * ux - uy * uy))
                lit = max(0.0, -0.55 * ux - 0.55 * uy + 0.6 * z)
                inside[(x, y)] = lit
    for (x, y), lit in inside.items():
        step = math.floor(min(1.0, 0.35 + lit * 0.85) * 4.0) / 4.0
        col = mix(DARK, BASE, step)
        if lit > 0.8:
            col = mix(col, LIGHT, 0.7)
        px[x, y] = col + (255,)
    # the swirls: spirals of dark line over the skin (the Devil Fruit pattern), each turning about its own middle
    for cx, cy, turns, size in ((11.5, 21.0, 1.25, 3.6), (19.5, 25.0, 1.25, 3.4), (17.0, 13.5, 1.15, 2.8), (21.5, 17.5, 0.9, 2.0)):
        steps = 60
        for k in range(steps):
            a = turns * 2 * math.pi * k / steps
            r = size * k / steps
            x = int(round(cx + math.cos(a) * r - 0.5))
            y = int(round(cy + math.sin(a) * r * 0.9 - 0.5))
            if (x, y) in inside:
                px[x, y] = SWIRL + (255,)
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the curly pink stem: up out of the top, curling over to the right
    top = min(y for (x, y) in inside if x == 16)
    curl = [(16, top - 1), (16, top - 2), (16, top - 3), (17, top - 4), (18, top - 4), (19, top - 3), (19, top - 2), (18, top - 2)]
    for i, (x, y) in enumerate(curl):
        if 0 <= y < N:
            px[x, y] = (STEM if i % 3 != 2 else STEM_DARK) + (255,)
    for x, y in curl:
        for ox, oy in ((1, 0), (-1, 0), (0, -1)):
            q = (x + ox, y + oy)
            if 0 <= q[1] < N and q not in curl and q not in inside and px[q[0], q[1]][3] == 0:
                px[q[0], q[1]] = OUTLINE + (255,)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
