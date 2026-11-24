#!/usr/bin/env python3
"""3.5.8 (the owner, with their own art/impact-frame.gif: "new impact frames template, this will replace every impact frame in the game
(but the Quake N ultimate move) ... feel free to edit it to your heart's content, change color, speed or whatever is needed for it to look
good"): the GIF (107 frames, 1008 x 1008 at 50 a second, its pixel art drawn 4 times over: 252 x 252 underneath) laid out as one sheet
the game draws a frame of at a time (assets/hexhaki/textures/piece/impact/impact_frame.png, 8 across and 8 down, each at the art's own
252 x 252). Edited for the game: its slow gathering cut down to the last few frames before the flash (an impact frame lands with the
blow), the flash and burst at their own speed, the sparks and smoke after it twice as fast: 58 frames, 1.16 s. Its black made
see-through by brightness (the burst over the game; the dark smoke half seen), its colours kept, its edge faded to a round rim (the
white flash filled the GIF's square: drawn over the game it showed the square's edge). The GIF itself is never changed.

Run: python3 tools/impact_texture.py"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "art/impact-frame.gif")
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/piece/impact/impact_frame.png")
CELL = 252
COLUMNS = 8
ROWS = 8
# the GIF's frames played: four just before the flash, the flash and burst (30 to 60) each, the rest every other one
FRAMES = list(range(26, 30)) + list(range(30, 61)) + list(range(61, 107, 2))
# brightness (the brightest channel) at which a pixel starts to show and at which it is solid
CLEAR = 8
SOLID = 70
# the burst fades out towards a round rim (share of the half cell) so the flash never shows the frame's square edge
RIM_FROM = 0.7
RIM_TO = 0.98


def rim(x, y):
    """How much of a pixel shows by its distance from the cell's middle: 1 inside, falling smoothly to 0 at the round rim."""
    half = CELL / 2.0
    d = ((x + 0.5 - half) ** 2 + (y + 0.5 - half) ** 2) ** 0.5 / half
    if d <= RIM_FROM:
        return 1.0
    if d >= RIM_TO:
        return 0.0
    t = (d - RIM_FROM) / (RIM_TO - RIM_FROM)
    return 1.0 - t * t * (3.0 - 2.0 * t)


def main():
    gif = Image.open(SRC)
    assert gif.size == (CELL * 4, CELL * 4), gif.size
    assert len(FRAMES) <= COLUMNS * ROWS, len(FRAMES)
    sheet = Image.new("RGBA", (CELL * COLUMNS, CELL * ROWS), (0, 0, 0, 0))
    for i, index in enumerate(FRAMES):
        gif.seek(index)
        # the art's own pixels: the middle of each 4 x 4 block
        frame = gif.convert("RGB").resize((CELL, CELL), Image.NEAREST)
        out = Image.new("RGBA", (CELL, CELL))
        src = frame.load()
        dst = out.load()
        for y in range(CELL):
            for x in range(CELL):
                r, g, b = src[x, y]
                m = max(r, g, b)
                a = 0 if m <= CLEAR else min(255, int(255 * (m - CLEAR) / (SOLID - CLEAR)))
                dst[x, y] = (r, g, b, int(round(a * rim(x, y))))
        sheet.paste(out, ((i % COLUMNS) * CELL, (i // COLUMNS) * CELL))
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    sheet.save(OUT, optimize=True)
    print(f"wrote {OUT}: {len(FRAMES)} frames, {os.path.getsize(OUT)} bytes")


if __name__ == "__main__":
    main()
