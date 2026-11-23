#!/usr/bin/env python3
"""3.4.5 (the owner, with their GIF: "whenever the Gura Gura No Mi hits the ultimate move N, this gif I sent fills the screen (in a way
that looks good) and plays till the end only once"; asked, "X-ray overlay"): the owner's art/broken-bones.gif (12 frames of 240 ms,
356 x 358: a pixel X-ray of a player's skeleton cracking, then shattering) laid out as one sheet the game draws a frame of at a time
(assets/hexhaki/textures/gui/broken_bones.png, 4 frames across, 3 down). Its black made see-through (an X-ray over the game: the black
background clear, the grey body half seen, the bones solid), its colours kept. The GIF itself is never changed.

Run: python3 tools/bones_texture.py"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "art/broken-bones.gif")
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/gui/broken_bones.png")
COLUMNS = 4
ROWS = 3
# brightness (the brightest channel) at which a pixel starts to show and at which it is solid
CLEAR = 10
SOLID = 72


def main():
    gif = Image.open(SRC)
    w, h = gif.size
    assert gif.n_frames == COLUMNS * ROWS, gif.n_frames
    sheet = Image.new("RGBA", (w * COLUMNS, h * ROWS), (0, 0, 0, 0))
    for i in range(gif.n_frames):
        gif.seek(i)
        frame = gif.convert("RGB")
        out = Image.new("RGBA", (w, h))
        src = frame.load()
        dst = out.load()
        for y in range(h):
            for x in range(w):
                r, g, b = src[x, y]
                m = max(r, g, b)
                a = 0 if m <= CLEAR else min(255, int(255 * (m - CLEAR) / (SOLID - CLEAR)))
                dst[x, y] = (r, g, b, a)
        sheet.paste(out, ((i % COLUMNS) * w, (i // COLUMNS) * h))
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    sheet.save(OUT, optimize=True)
    print(OUT, sheet.size)


if __name__ == "__main__":
    main()
