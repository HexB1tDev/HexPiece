#!/usr/bin/env python3
"""3.3.1 (the owner: "I want gifs when it's all done"): a looping GIF of a player clip, the player drawn as Minecraft draws it
(tools/anim.py: every part a box, painted far to near), half a tick a frame. With --toon the Nika fruit's Toon Force squash
(gear/ToonForce, played tick by tick by tools/preview/ToonDump exactly as client/gear/NikaToon plays it) scales the body round its feet.
Usage: python3 tools/clip_gif.py out.gif clip.json [clip.json ...] [--toon] [--ticks N] [--view three] [--caption text] [--cols 3]
(several clips: one cell each, side by side, all playing at once; each cell captioned with its clip)
(needs a build first: build/classes, and tools/preview compiled to build/preview for --toon)
"""
import argparse
import math
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402
from PIL import Image, ImageDraw  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def toon_curve(name, ticks):
    cp = os.pathsep.join([os.path.join(ROOT, "build", "classes"), os.path.join(ROOT, "build", "preview")])
    out = subprocess.run(["java", "-cp", cp, "ToonDump", name, str(ticks + 1)], capture_output=True, text=True, check=True).stdout
    return [float(line.split()[1]) for line in out.splitlines() if line.strip()]


def clip_tick(a, tick):
    end = a.length()
    if a.loop and tick > end:
        ret = a.ret
        period = max(1e-6, end - ret)
        return ret + (tick - end) % period
    return min(tick, end)


def frame(a, tick, squash, view, size, caption):
    w, h = size
    img = Image.new("RGB", (w, h), (236, 240, 244))
    d = ImageDraw.Draw(img)
    scale = h / 50.0
    origin = (w / 2, h * 0.34)
    ground = origin[1] + 24 * scale
    d.rectangle([0, ground, w, h], fill=(126, 168, 98))
    # a soft shadow under the feet, widening with a squash
    sw = 7 * scale * (1 + 0.26 * squash)
    d.ellipse([origin[0] - sw, ground - 1.2 * scale, origin[0] + sw, ground + 1.2 * scale], fill=(104, 140, 82))
    pose = a.pose(clip_tick(a, tick))
    sk = anim.skeleton(pose)
    tall = 1 - 0.3 * squash
    wide = 1 + 0.26 * squash

    def toon(p):
        return (p[0] * wide, 24 - (24 - p[1]) * tall, p[2] * wide)

    sk["boxes"] = [(part, [toon(c) for c in corners]) for part, corners in sk["boxes"]]
    sk["face"] = toon(sk["face"])
    sk["head"] = [toon(c) for c in sk["head"]]
    ang = math.radians(anim.VIEWS.get(view, 35))

    def project(p):
        x, y, z = p
        return (origin[0] + (x * math.cos(ang) - z * math.sin(ang)) * scale, origin[1] + y * scale), x * math.sin(ang) + z * math.cos(ang)

    faces = []
    for part, corners in sk["boxes"]:
        projected = [project(c) for c in corners]
        centre = sum(dd for _, dd in projected) / 8
        colour = anim.COLORS[part]
        for indices, axis, side in anim.FACES:
            depth = sum(projected[i][1] for i in indices) / 4
            if depth > centre - 1e-6:
                continue
            shade = {1: 0.82 if side > 0 else 1.12, 0: 0.86, 2: 0.94}[axis]
            faces.append((depth, [projected[i][0] for i in indices], tuple(min(255, int(c * shade)) for c in colour)))
    for depth, polygon, colour in sorted(faces, key=lambda f: -f[0]):
        d.polygon(polygon, fill=colour, outline=(20, 20, 20))
    d.rectangle([0, 0, w, 16], fill=(30, 30, 34))
    d.text((5, 2), caption, fill=(240, 240, 232))
    d.text((w - 52, h - 14), f"t {tick:5.1f}", fill=(40, 60, 30))
    return img


def main():
    p = argparse.ArgumentParser()
    p.add_argument("out")
    p.add_argument("clips", nargs="+")
    p.add_argument("--toon", action="store_true")
    p.add_argument("--ticks", type=float, default=None)
    p.add_argument("--view", default="three")
    p.add_argument("--caption", default=None)
    p.add_argument("--size", default="300x260")
    p.add_argument("--cols", type=int, default=3)
    a = p.parse_args()
    clips = []
    for path in a.clips:
        clip = anim.load(path)
        name = os.path.splitext(os.path.basename(path))[0]
        clips.append((name, clip))
    ticks = a.ticks if a.ticks is not None else max(c.length() for _, c in clips) + 12
    curves = {name: (toon_curve(name, int(math.ceil(ticks)) + 2) if a.toon else None) for name, _ in clips}
    size = tuple(int(v) for v in a.size.split("x"))
    cols = min(a.cols, len(clips))
    rows = (len(clips) + cols - 1) // cols
    top = 20 if a.caption else 0
    frames = []
    t = 0.0
    while t <= ticks + 1e-9:
        sheet = Image.new("RGB", (size[0] * cols, size[1] * rows + top), (30, 30, 34))
        if a.caption:
            ImageDraw.Draw(sheet).text((6, 4), a.caption, fill=(240, 240, 232))
        for k, (name, clip) in enumerate(clips):
            curve = curves[name]
            squash = 0.0
            if curve:
                i = int(math.floor(t))
                f = t - i
                squash = curve[i] + (curve[min(i + 1, len(curve) - 1)] - curve[i]) * f
            cell = frame(clip, t, squash, a.view, size, name)
            sheet.paste(cell, ((k % cols) * size[0], top + (k // cols) * size[1]))
        frames.append(sheet.quantize(colors=96, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE))
        t += 0.5
    durations = [25] * len(frames)
    durations[-1] = 500
    frames[0].save(a.out, save_all=True, append_images=frames[1:], duration=durations, loop=0, optimize=True, disposal=1)
    print(a.out, len(frames), "frames,", os.path.getsize(a.out), "bytes")


if __name__ == "__main__":
    main()
