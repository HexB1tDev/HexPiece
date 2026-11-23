#!/usr/bin/env python3
"""3.2.9 (the owner: "I want gifs when it's all done"): a looping GIF from a folder of frames, as FxPreview writes them when its output
ends in "/" (tools/preview/FxPreview: one PNG a shot, in order). Its label strip is covered by the caption.
Usage: python3 tools/make_gif.py frames_dir/ out.gif [--ms 25] [--last 600] [--caption "text"] [--captions file]
(--ms each frame's time: 25 for half-tick frames, 50 for whole ticks; --last how long the last frame stays before the loop;
--captions a text file, one caption a frame in order, after --caption's text: 3.5.4, the Sky Road run's moves and keys.)
"""
import argparse
import os
from PIL import Image, ImageDraw


def main():
    p = argparse.ArgumentParser()
    p.add_argument("frames")
    p.add_argument("out")
    p.add_argument("--ms", type=int, default=25)
    p.add_argument("--last", type=int, default=0)
    p.add_argument("--caption", default=None)
    p.add_argument("--captions", default=None)
    a = p.parse_args()
    names = sorted(f for f in os.listdir(a.frames) if f.endswith(".png"))
    each = []
    if a.captions:
        with open(a.captions, encoding="utf-8") as handle:
            each = [line.rstrip("\n") for line in handle]
    frames = []
    for i, n in enumerate(names):
        im = Image.open(os.path.join(a.frames, n)).convert("RGB")
        d = ImageDraw.Draw(im)
        d.rectangle([0, 0, im.width, 20], fill=(30, 30, 34))
        text = " - ".join(t for t in (a.caption, each[i] if i < len(each) else None) if t)
        if text:
            d.text((6, 4), text, fill=(240, 240, 232))
        frames.append(im.quantize(colors=128, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE))
    durations = [a.ms] * len(frames)
    if a.last:
        durations[-1] = a.last
    frames[0].save(a.out, save_all=True, append_images=frames[1:], duration=durations, loop=0, optimize=True, disposal=1)
    print(a.out, len(frames), "frames,", os.path.getsize(a.out), "bytes")


if __name__ == "__main__":
    main()
