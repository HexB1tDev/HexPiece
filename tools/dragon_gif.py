#!/usr/bin/env python3
"""2.7.0: the Uo Uo no Mi's dragon as an animated GIF, rendered by tools/preview/DragonPreview (the game's own DragonForm and model):
the transformation, the hover, the flight blending in and out, the way back, from a fixed camera beside the player. To judge it.

Usage (after python3 build.py):
  javac -cp build/classes -d build/preview tools/preview/DragonPreview.java
  python3 tools/dragon_gif.py out.gif [yaw pitch distance]
"""
import glob
import subprocess
import sys
import tempfile
from pathlib import Path

from PIL import Image

FPS = 15


def shots(yaw, pitch, dist):
    out = []
    target = "0:9:-6"
    def add(phase, t, fly, look=0.0, bank=0.0):
        out.append(f"g:{phase}:{t:.3f}:{fly:.3f}:{look:.1f}:{bank:.1f}:{yaw}:{pitch}:{dist}:{target}")
    for i in range(int(2.2 * FPS) + 1):                  # the transformation
        add(1, i / FPS, 0.0)
    for i in range(int(1.0 * FPS)):                       # the hover
        add(2, i / FPS, 0.0)
    for i in range(int(1.5 * FPS)):                       # taking off: the flight eased in (as the client eases it)
        f = 1.0 - 0.93 ** (i * 20 / FPS)
        add(2, 1.0 + i / FPS, f)
    for i in range(int(1.5 * FPS)):                       # flying, banking a little
        add(2, 2.5 + i / FPS, 1.0, 0.0, 15.0 * (i / (1.5 * FPS)))
    for i in range(int(1.5 * FPS)):                       # landing: the flight eased out
        f = 0.93 ** (i * 20 / FPS)
        add(2, 4.0 + i / FPS, f)
    for i in range(int(1.5 * FPS) + 1):                   # the way back
        add(3, i / FPS, 0.0)
    for i in range(int(0.6 * FPS)):                       # the human again
        add(0, 0.0, 0.0)
    return out


def main():
    out = sys.argv[1]
    yaw, pitch, dist = sys.argv[2:5] if len(sys.argv) >= 5 else ("55", "12", "52")
    cache = sorted(glob.glob("build/dragon-cache/*/seiryu.sry"))[-1]
    texture = cache.replace("seiryu.sry", "seiryu.png")
    with tempfile.TemporaryDirectory() as tmp:
        subprocess.run(["java", "-cp", "build/preview:build/classes", "DragonPreview", cache, texture, tmp + "/", "--size=480x300",
                        *shots(yaw, pitch, dist)], check=True)
        frames = [Image.open(p).convert("P", palette=Image.ADAPTIVE, colors=128) for p in sorted(Path(tmp).glob("*.png"))]
    frames[0].save(out, save_all=True, append_images=frames[1:], duration=int(1000 / FPS), loop=0, optimize=True)
    print(out, len(frames), "frames")


if __name__ == "__main__":
    main()
