#!/usr/bin/env python3
"""Contact sheet of one or more animations: each row is an animation, each column a moment (front view above, side view below).

Usage: python3 tools/anim_preview.py out.png anim.json [anim.json ...] [--frames 10] [--views front,side]
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402
from PIL import Image, ImageDraw  # noqa: E402


def sheet(paths, out, frames=10, views=("front", "side")):
    cell_w, cell_h, scale = 120, 150, 3.4
    rows = []
    for path in paths:
        a = anim.load(path)
        length = a.length()
        ticks = [round(length * i / (frames - 1), 2) for i in range(frames)]
        rows.append((os.path.basename(path), a, ticks))
    img = Image.new("RGB", (cell_w * frames + 10, (cell_h * len(views) + 22) * len(rows)), (238, 236, 230))
    d = ImageDraw.Draw(img)
    for r, (name, a, ticks) in enumerate(rows):
        top = r * (cell_h * len(views) + 22)
        d.text((4, top + 4), f"{name}  ({a.length()} ticks{', loop' if a.loop else ''})", fill=(20, 20, 20))
        for c, tick in enumerate(ticks):
            pose = a.pose(tick)
            for v, view in enumerate(views):
                x0 = c * cell_w + 5
                y0 = top + 20 + v * cell_h
                d.rectangle([x0, y0, x0 + cell_w - 4, y0 + cell_h - 4], fill=(250, 250, 248), outline=(200, 200, 200))
                d.line([(x0, y0 + 32 + 24 * scale), (x0 + cell_w - 4, y0 + 32 + 24 * scale)], fill=(170, 170, 170))
                anim.draw(d, pose, (x0 + cell_w / 2, y0 + 32), scale, view)
                if v == 0:
                    d.text((x0 + 3, y0 + 2), f"t{tick:g}", fill=(90, 90, 90))
    img.save(out)


if __name__ == "__main__":
    args = sys.argv[1:]
    frames = 10
    views = ("front", "side")
    if "--frames" in args:
        i = args.index("--frames")
        frames = int(args[i + 1])
        del args[i:i + 2]
    if "--views" in args:
        i = args.index("--views")
        views = tuple(args[i + 1].split(","))
        del args[i:i + 2]
    sheet(args[1:], args[0], frames, views)
