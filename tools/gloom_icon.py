#!/usr/bin/env python3
"""3.3.6: the Gloom effect's icon (assets/hexhaki/textures/mob_effect/gloom.png, 18 x 18, as the game's own effect icons): a small dark
raincloud of gloom, the anime's blue gloom lines hanging under it, outlined dark as the game's icons are. Drawn pixel by pixel here
(nothing of the owner's is changed). Run: python3 tools/gloom_icon.py [out.png]"""
import os
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/mob_effect/gloom.png")

OUTLINE = (20, 21, 34, 255)
SHADOW = (52, 56, 78, 255)
MID = (84, 90, 116, 255)
LIGHT = (126, 132, 156, 255)
TOP = (160, 166, 188, 255)
LINE_DARK = (40, 46, 110, 255)
LINE = (82, 96, 190, 255)
LINE_LIGHT = (128, 144, 226, 255)

# the cloud: three puffs over a flat bottom
PUFFS = [(4.6, 7.4, 2.5), (8.8, 5.2, 3.3), (13.2, 6.6, 2.9)]
BOTTOM = 9.0


def cloud(x, y):
    if y > BOTTOM:
        return False
    if y > 7.4 and 2.6 <= x <= 15.6:
        return True
    return any((x - cx) ** 2 + (y - cy) ** 2 <= r * r for cx, cy, r in PUFFS)


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else OUT
    img = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    px = img.load()
    inside = [[cloud(x + 0.5, y + 0.5) for x in range(18)] for y in range(18)]
    for y in range(18):
        for x in range(18):
            if inside[y][x]:
                # lit from above: the tops of the puffs light, its underside in shadow
                top = not inside[y - 1][x] if y > 0 else True
                if top:
                    px[x, y] = TOP
                elif y >= 8:
                    px[x, y] = SHADOW
                elif y <= 5 or (y <= 7 and x >= 12 and not inside[y - 2][x]):
                    px[x, y] = LIGHT
                else:
                    px[x, y] = MID
    # the gloom lines hanging under it: three wavering strokes of blue, the middle one longest
    for x, top, length in ((4, 12, 4), (8, 12, 5), (12, 12, 4)):
        for i in range(length):
            # slanting a little as they fall, a gap between each and the cloud
            xx = x + i // 2
            px[xx, top + i] = LINE_LIGHT if i == 0 else LINE if i < length - 1 else LINE_DARK
    # the dark outline round everything drawn
    drawn = [[px[x, y][3] > 0 for x in range(18)] for y in range(18)]
    for y in range(18):
        for x in range(18):
            if drawn[y][x]:
                continue
            near = any(0 <= x + dx < 18 and 0 <= y + dy < 18 and drawn[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if near:
                px[x, y] = OUTLINE
    img.save(out)
    print(out)


if __name__ == "__main__":
    main()
