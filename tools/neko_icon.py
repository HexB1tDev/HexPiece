#!/usr/bin/env python3
"""3.7.9: the Neko Neko no Mi, Model: Leopard's inventory icon (32 x 32, the house's fruit icons' style: a shaded fruit, a dark outline),
drawn after the owner's 2D picture of it (described in docs/handoff/next-ai-3.7.6.md): a golden-yellow fruit shaped like a heart with a
cat's face, two pointed cat ears on top with a pale inner ear and a dark swirl, leopard rosettes all over (dark rings round brown
centres), a white gloss on the upper left, and a green stem rising from the top into a T: a curl on the left arm, a small yellow bud
at the end of the right one. Pillow.

Usage: python3 tools/neko_icon.py src/main/resources/assets/hexhaki/textures/item/neko_neko_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
GOLD = (246, 190, 36)
GOLD_LIGHT = (255, 226, 110)
GOLD_DARK = (196, 120, 18)
EAR = (255, 244, 214)
OUTLINE = (32, 20, 12)
SPOT = (48, 30, 18)
SPOT_MID = (150, 98, 44)
GLOSS = (255, 252, 236)
STEM = (112, 178, 64)
STEM_DARK = (56, 104, 34)
BUD = (250, 226, 90)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def inside_fruit(x, y):
    """The cat-faced heart: two lobes on top (the cheeks under the ears), narrowing to a rounded chin."""
    # the upper body: a wide ellipse; the lower: a tapering rounded point
    cx = 16.0
    if y < 20.5:
        return ((x - cx) / 12.2) ** 2 + ((y - 19.5) / 9.0) ** 2 < 1.0
    half = 12.2 * (1.0 - ((y - 20.5) / 9.6) ** 1.6)
    return abs(x - cx) < max(0.0, half) and y < 30.3


def inside_ear(x, y, side):
    """A pointed ear: a triangle from the top corner of each lobe."""
    tip = (5.2 if side < 0 else 26.8, 6.2)
    base_a = (3.9 if side < 0 else 28.1, 17.5)
    base_b = (13.5 if side < 0 else 18.5, 12.2)
    return point_in_tri((x, y), tip, base_a, base_b)


def inner_ear(x, y, side):
    tip = (6.2 if side < 0 else 25.8, 8.6)
    base_a = (5.6 if side < 0 else 26.4, 15.2)
    base_b = (11.4 if side < 0 else 20.6, 12.8)
    return point_in_tri((x, y), tip, base_a, base_b)


def point_in_tri(p, a, b, c):
    def s(p1, p2, p3):
        return (p1[0] - p3[0]) * (p2[1] - p3[1]) - (p2[0] - p3[0]) * (p1[1] - p3[1])
    d1 = s(p, a, b)
    d2 = s(p, b, c)
    d3 = s(p, c, a)
    neg = d1 < 0 or d2 < 0 or d3 < 0
    pos = d1 > 0 or d2 > 0 or d3 > 0
    return not (neg and pos)


# the rosettes: (x, y, size)
SPOTS = [(10.0, 18.5, 2.0), (16.0, 16.0, 1.9), (22.0, 18.5, 2.0), (6.5, 23.5, 1.7), (13.0, 22.5, 2.1), (19.5, 22.5, 2.1), (25.5, 23.5, 1.7),
         (10.0, 27.4, 1.7), (16.0, 27.6, 1.9), (22.0, 27.4, 1.7)]


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    body = {}
    for y in range(N):
        for x in range(N):
            fx, fy = x + 0.5, y + 0.5
            ear = inside_ear(fx, fy, -1) or inside_ear(fx, fy, 1)
            if inside_fruit(fx, fy) or ear:
                body[(x, y)] = ear and not inside_fruit(fx, fy)
    for (x, y), is_ear in body.items():
        fx, fy = x + 0.5, y + 0.5
        # lit from the upper left, shaded to the lower right
        light = 1.0 - ((fx - 10.0) ** 2 + (fy - 15.0) ** 2) ** 0.5 / 19.0
        col = mix(GOLD_DARK, GOLD, 0.35 + light * 0.9)
        col = mix(col, GOLD_LIGHT, max(0.0, light - 0.55) * 1.6)
        if inner_ear(fx, fy, -1) or inner_ear(fx, fy, 1):
            col = EAR
            # the swirl in each ear: a small dark curl
            for side in (-1, 1):
                ex = 7.5 if side < 0 else 24.5
                for cx, cy in ((ex, 11.5), (ex + 1, 11.5), (ex + 1, 12.5), (ex, 13.5), (ex - 1, 12.5)):
                    if abs(fx - cx - 0.0) < 0.5 and abs(fy - cy) < 0.5:
                        col = SPOT
        else:
            # the rosettes: a broken dark ring round a brown middle
            for k, (sx, sy, sr) in enumerate(SPOTS):
                d = math.hypot((fx - sx) * 1.0, (fy - sy) * 1.2)
                a = math.atan2(fy - sy, fx - sx)
                if d < sr * 0.62:
                    col = mix(SPOT_MID, col, 0.15)
                elif d < sr * 1.15 and math.cos(a * 1.0 + k * 1.7) < 0.55:
                    col = SPOT
        px[x, y] = col + (255,)
    # the gloss: a soft white streak on the upper left
    for (x, y) in [(8, 17), (9, 16), (8, 18), (7, 19), (10, 15), (9, 17)]:
        if (x, y) in body:
            px[x, y] = mix(px[x, y][:3], GLOSS, 0.75) + (255,)
    # the outline: every empty pixel next to the fruit
    for y in range(N):
        for x in range(N):
            if (x, y) in body:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if (x + dx, y + dy) in body:
                    px[x, y] = OUTLINE + (255,)
                    break
    # the stem: up from the top middle into a T, a curl on the left, a bud on the right
    stem = [(16, 10), (16, 9), (16, 8), (16, 7), (16, 6), (15, 5), (16, 5), (17, 5), (14, 5), (13, 5), (12, 5), (18, 4), (19, 4), (20, 4),
            (21, 4), (22, 4), (11, 4), (10, 4), (9, 3), (9, 2), (10, 1), (11, 1), (12, 2), (11, 3)]
    for x, y in stem:
        px[x, y] = STEM + (255,)
    for x, y in [(15, 10), (15, 9), (15, 8), (15, 7), (15, 6), (13, 6), (12, 6), (14, 6), (17, 6), (18, 5), (19, 5), (20, 5), (21, 5),
                 (8, 3), (8, 2), (10, 5), (9, 4)]:
        if px[x, y][3] == 0:
            px[x, y] = STEM_DARK + (255,)
    for x, y in [(23, 3), (24, 3), (23, 4), (24, 4)]:
        px[x, y] = BUD + (255,)
    for x, y in [(25, 3), (25, 4), (23, 5), (24, 5), (23, 2), (24, 2)]:
        if px[x, y][3] == 0:
            px[x, y] = OUTLINE + (255,)
    return img


def main():
    img = icon()
    img.save(sys.argv[1])
    if len(sys.argv) > 2:
        img.resize((N * 12, N * 12), Image.NEAREST).save(sys.argv[2])


if __name__ == "__main__":
    main()
