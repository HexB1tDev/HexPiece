#!/usr/bin/env python3
"""3.6.9: the Tori Tori no Mi, Model: Phoenix's inventory icon (32 x 32, the house's fruit icons' style as tools/mochi_icon.py draws them:
a shaded fruit, a dark outline): a round fruit of blue fire, cyan where lit and violet in its shade, covered in rows of rounded
feather-like scales, a crown of golden flame licking up off its top. Pillow.

Usage: python3 tools/tori_icon.py src/main/resources/assets/hexhaki/textures/item/tori_tori_no_mi.png [preview.png]
"""
import math
import sys

from PIL import Image

N = 32
CYAN = (96, 222, 246)
BLUE = (46, 120, 230)
VIOLET = (110, 74, 196)
LIGHT = (214, 250, 255)
OUTLINE = (20, 22, 48)
SCALE_EDGE = (34, 74, 170)
GOLD = (255, 196, 40)
GOLD_LIGHT = (255, 240, 150)
GOLD_DARK = (232, 120, 20)

BODY = (16.0, 20.0, 10.2, 8.6)  # middle x, y, half width, half height


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def inside_body(x, y):
    cx, cy, rx, ry = BODY
    return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 < 1.0


def icon():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    inside = {}
    cx, cy, rx, ry = BODY
    for y in range(N):
        for x in range(N):
            fx, fy = x + 0.5, y + 0.5
            if inside_body(fx, fy):
                ux, uy = (fx - cx) / rx, (fy - cy) / ry
                z = math.sqrt(max(0.0, 1.0 - min(1.0, ux * ux + uy * uy)))
                inside[(x, y)] = max(0.0, -0.45 * ux - 0.6 * uy + 0.6 * z)
    # the scales: rows of rounded scallops, each lit on its upper side and edged dark at its bottom
    for (x, y), lit in inside.items():
        # each scale a rounded scallop: the nearest scale middle (rows 3 apart, every other row offset), the pixel's distance from it
        best = (9.0, 0.0, 0.0)
        for row in range(-1, 8):
            my = cy - ry + 1.0 + row * 3.0
            off = 2.0 if row % 2 else 0.0
            for col_i in range(-1, 9):
                mx = cx - rx + off + col_i * 4.0
                d = math.hypot((x + 0.5 - mx) / 2.1, (y + 0.5 - my) / 2.0)
                if y + 0.5 >= my - 0.5 and d < best[0]:
                    best = (d, x + 0.5 - mx, y + 0.5 - my)
        d, dx, dy = best
        bump = max(0.0, 1.0 - d) * 0.9 - dy * 0.05
        tone = lit * 0.85 + bump * 0.35
        col = mix(VIOLET, BLUE, tone * 1.6) if tone < 0.62 else mix(BLUE, CYAN, (tone - 0.62) * 2.6)
        if 0.78 < d < 1.15 and dy > 0.2:
            col = mix(col, SCALE_EDGE, 0.75)
        if lit > 0.8 and d < 0.5:
            col = mix(col, LIGHT, 0.6)
        px[x, y] = col + (255,)
    for y in range(N):
        for x in range(N):
            if (x, y) in inside:
                continue
            if any((x + ox, y + oy) in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE + (255,)
    # the crown of golden flame: three tongues licking up and out of the top, the middle one tallest, curling to their tips
    top = min(y for (x, y) in inside if x == 16)
    flame = {}
    for tongue, (base_x, lean, height, width) in enumerate(((16.0, 0.15, 10.0, 2.4), (13.0, -0.55, 7.0, 1.8), (19.2, 0.6, 7.5, 1.8))):
        for k in range(int(height * 4)):
            t = k / (height * 4)
            y = top - t * height
            x = base_x + lean * t * height + 1.4 * math.sin(t * 3.4 + tongue) * t
            w = width * (1.0 - t) ** 0.8 + 0.3
            for dx in range(-3, 4):
                px_x = int(round(x + dx * 0.5))
                if abs(px_x + 0.5 - x) <= w:
                    p = (px_x, int(round(y)))
                    if 0 <= p[0] < N and 0 <= p[1] < N:
                        edge = abs(px_x + 0.5 - x) / max(0.5, w)
                        heat = (1.0 - t) * 0.6 + (1.0 - edge) * 0.5
                        flame[p] = max(flame.get(p, 0.0), heat)
    for (x, y), heat in flame.items():
        if (x, y) in inside:
            continue
        col = mix(GOLD_DARK, GOLD, heat * 1.4) if heat < 0.7 else mix(GOLD, GOLD_LIGHT, (heat - 0.7) * 3.0)
        px[x, y] = col + (255,)
    for (x, y) in flame:
        for ox, oy in ((1, 0), (-1, 0), (0, -1), (0, 1)):
            q = (x + ox, y + oy)
            if 0 <= q[0] < N and 0 <= q[1] < N and q not in flame and q not in inside and px[q[0], q[1]][3] == 0:
                px[q[0], q[1]] = (120, 50, 10, 255)
    return img


if __name__ == "__main__":
    image = icon()
    image.save(sys.argv[1])
    if len(sys.argv) > 2:
        image.resize((256, 256), Image.NEAREST).save(sys.argv[2])
