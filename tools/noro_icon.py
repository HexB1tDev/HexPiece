#!/usr/bin/env python3
"""3.3.4: the Noro Noro no Mi's inventory icon (32 x 32, the original fruits' style as tools/uo_icon.py and tools/hie_icon.py draw them:
a shaded fruit swirled all over, a dark outline): after the owner's reference picture, an oval fruit lying tilted, deep purple going
lilac where the light falls, darker purple swirls packed all over it, and for a stem two green rings linked like an 8 standing on
its top right. Pillow.

Usage: python3 tools/noro_icon.py src/main/resources/assets/hexhaki/textures/item/noro_noro_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
BASE = (118, 62, 138)
DARK = (58, 26, 78)
LIGHT = (170, 116, 190)
SWIRL = (48, 18, 64)
OUTLINE = (24, 8, 34)
RING = (64, 178, 72)
RING_DARK = (28, 112, 44)
RING_LIGHT = (150, 222, 128)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    # an oval lying tilted (its long axis from lower left to upper right), as in the reference
    cx, cy, a, b, tilt = 13.2, 19.2, 12.0, 8.0, math.radians(-44)
    ca, sa = math.cos(tilt), math.sin(tilt)
    inside = set()
    for y in range(N):
        for x in range(N):
            ux, uy = x + 0.5 - cx, y + 0.5 - cy
            lx, ly = (ux * ca + uy * sa) / a, (-ux * sa + uy * ca) / b
            d = lx * lx + ly * ly
            if d > 1.0:
                continue
            inside.add((x, y))
            z = math.sqrt(max(0.0, 1.0 - d))
            lit = max(0.0, -0.4 * lx - 0.55 * ly + 0.5 * z)
            step = math.floor(min(1.0, 0.3 + lit * 0.9) * 4.0) / 4.0
            col = mix(DARK, BASE, step)
            if lit > 0.72:
                col = mix(col, LIGHT, 0.6)
            # small swirls packed all over (the fruit's pattern): little spirals on a grid in the fruit's own frame
            gx, gy = lx * 3.6 + 0.3, ly * 2.6 + 0.2
            fx, fy = gx - math.floor(gx) - 0.5, gy - math.floor(gy) - 0.5
            rr = math.hypot(fx, fy)
            ang = math.atan2(fy, fx)
            if rr < 0.5 and math.sin(ang - rr * 13.0) > 0.2:
                col = mix(col, SWIRL, 0.85 - 0.3 * lit)
            px[x, y] = col + (255,)
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the stem: two green rings linked like an 8 standing on the top right, their holes showing through
    ring = []
    for y in range(N):
        for x in range(N):
            for rx, ry, ro, ri in ((23.4, 7.6, 3.6, 1.6), (28.2, 3.2, 2.8, 1.1)):
                d = math.hypot(x + 0.5 - rx, y + 0.5 - ry)
                if ri < d <= ro and (x, y) not in inside and (x, y) not in ring:
                    ring.append((x, y))
                    lit = -(x + 0.5 - rx) - (y + 0.5 - ry)
                    px[x, y] = (RING_LIGHT if lit > 2.0 else RING if lit > -1.5 else RING_DARK) + (255,)
    for x, y in ring:
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
