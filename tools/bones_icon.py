#!/usr/bin/env python3
"""3.4.5: the Broken Bones effect's icon (assets/hexhaki/textures/mob_effect/broken_bones.png, 18 x 18, as the game's own effect icons):
a bone lying slantwise, snapped in two in the middle, its halves knocked apart with a dark crack between them, bone white shaded to
ivory, outlined dark as the game's icons are. Drawn pixel by pixel here. Run: python3 tools/bones_icon.py [out.png]"""
import math
import os
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/mob_effect/broken_bones.png")

OUTLINE = (34, 26, 22, 255)
SHADOW = (176, 160, 132, 255)
MID = (222, 210, 186, 255)
LIGHT = (246, 240, 226, 255)
CRACK = (120, 40, 34, 255)


def bone(x, y):
    """Inside the bone? A slanted shaft with two knobs at each end, broken in the middle (the halves shifted apart)."""
    # the bone's own axis: from the lower left to the upper right
    a = math.radians(-45)
    u = (x - 9) * math.cos(a) - (y - 9) * math.sin(a)
    v = (x - 9) * math.sin(a) + (y - 9) * math.cos(a)
    # the break: the upper half pushed along and aside
    if u > 0.9:
        u -= 1.0
        v += 1.6
    elif u > -0.9:
        return False
    if abs(u) <= 5.0 and abs(v) <= 1.2:
        return True
    for end in (-5.6, 5.6):
        for side in (-1.25, 1.25):
            if (u - end) ** 2 + (v - side) ** 2 <= 1.7 ** 2:
                return True
    return False


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else OUT
    img = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    px = img.load()
    inside = [[bone(x + 0.5, y + 0.5) for x in range(18)] for y in range(18)]
    for y in range(18):
        for x in range(18):
            if inside[y][x]:
                up = y > 0 and inside[y - 1][x]
                down = y < 17 and inside[y + 1][x]
                px[x, y] = LIGHT if not up else SHADOW if not down else MID
    drawn = [[px[x, y][3] > 0 for x in range(18)] for y in range(18)]
    for y in range(18):
        for x in range(18):
            if drawn[y][x]:
                continue
            near = any(0 <= x + dx < 18 and 0 <= y + dy < 18 and drawn[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if near:
                px[x, y] = OUTLINE
    # two red-dark chips flying out of the break
    for x, y in ((7, 7), (11, 11)):
        if px[x, y][3] == 0:
            px[x, y] = CRACK
    os.makedirs(os.path.dirname(out), exist_ok=True)
    img.save(out)
    print(out)


if __name__ == "__main__":
    main()
