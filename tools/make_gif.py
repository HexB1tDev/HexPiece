#!/usr/bin/env python3
"""3.2.9 (the owner: "I want gifs when it's all done"): a looping GIF from a folder of frames, as FxPreview writes them when its output
ends in "/" (tools/preview/FxPreview: one PNG a shot, in order). Its label strip is covered by the caption.
Usage: python3 tools/make_gif.py frames_dir/ out.gif [--ms 25] [--last 600] [--caption "text"]
(--ms each frame's time: 25 for half-tick frames, 50 for whole ticks; --last how long the last frame stays before the loop.)
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
    a = p.parse_args()
    names = sorted(f for f in os.listdir(a.frames) if f.endswith(".png"))
    frames = []
    for n in names:
        im = Image.open(os.path.join(a.frames, n)).convert("RGB")
        d = ImageDraw.Draw(im)
        d.rectangle([0, 0, im.width, 20], fill=(30, 30, 34))
        if a.caption:
            d.text((6, 4), a.caption, fill=(240, 240, 232))
        frames.append(im.quantize(colors=128, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE))
    durations = [a.ms] * len(frames)
    if a.last:
        durations[-1] = a.last
    frames[0].save(a.out, save_all=True, append_images=frames[1:], duration=durations, loop=0, optimize=True, disposal=1)
    print(a.out, len(frames), "frames,", os.path.getsize(a.out), "bytes")


if __name__ == "__main__":
    main()
