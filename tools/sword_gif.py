#!/usr/bin/env python3
"""2.3.2: animated GIFs of the written sword animations, real time (a frame every half tick, 25 ms), each move from three sides, the
blade drawn as the game draws it at every moment (tools/sword_anims.game_blade: in the hand, sliding along the saya, Yoru lifted out
of its ropes and gliding into the hand). The owner sees the renders: judge every one before showing it.

Usage: python3 tools/sword_gif.py OUT_DIR [regex,regex] [--views three,side,front] [--scale 62]
"""
import json
import multiprocessing
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402
import sword_anims as sa  # noqa: E402
import sword_rig as rig  # noqa: E402
import sword_view as sv  # noqa: E402
from PIL import Image, ImageDraw  # noqa: E402

YAW = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180, "left": -90, "threeleft": -35}
TILE = (240, 300)
STEP = 0.5


def tile(move, q, t, view, scale):
    prof = move.profile
    w, h = TILE
    cam = sv.Camera(YAW[view], 10.0, scale, w / 2, h * 0.92)
    cv = sv.Canvas(w, h)
    sv.draw_figure(cv, cam, q)
    if prof is sa.YORU:
        sv.draw_ropes(cv, cam, q)
        sv.draw_katana(cv, cam, "yoru", sa.game_blade(move, q, t), shine=0.3)
    elif move.drawn_at(t) or sa.sliding(move, t):
        sv.draw_katana(cv, cam, prof.key, sa.game_blade(move, q, t))
        sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=False)
    else:
        sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=True, shine=0.3)
    img = cv.image()
    d = ImageDraw.Draw(img)
    d.line([(0, h * 0.92), (w, h * 0.92)], fill=(150, 120, 90))
    return img


def one(job):
    name, out, views, scale = job
    move = sa.MOVES[name]
    rig.set_sword(move.profile.key)
    with open(os.path.join(sa.OUT, name + ".json"), encoding="utf-8") as handle:
        a = anim.Anim(json.load(handle))
    frames = []
    t = 0.0
    while t <= a.end + 1e-9:
        q = a.pose(t)
        frame = Image.new("RGB", (TILE[0] * len(views), TILE[1] + 18), (236, 234, 228))
        for c, view in enumerate(views):
            frame.paste(tile(move, q, t, view, scale), (c * TILE[0], 18))
        d = ImageDraw.Draw(frame)
        cut = any(lo <= t <= hi for lo, hi in move.cuts)
        d.text((4, 3), f"{name}  t{t:g}/{a.end}" + ("  CUT" if cut else ""), fill=(200, 40, 40) if cut else (30, 30, 30))
        frames.append(frame.convert("P", palette=Image.ADAPTIVE, colors=128))
        t += STEP
    # hold the last pose a moment so the loop reads where the move ends
    frames += [frames[-1]] * 8
    path = os.path.join(out, name + ".gif")
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=int(STEP * 50), loop=0, optimize=True)
    return path


def main():
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    pats = [a for a in sys.argv[2:] if not a.startswith("--") and sys.argv[sys.argv.index(a) - 1] not in ("--views", "--scale")]
    views = sys.argv[sys.argv.index("--views") + 1].split(",") if "--views" in sys.argv else ["three", "side", "front"]
    scale = float(sys.argv[sys.argv.index("--scale") + 1]) if "--scale" in sys.argv else 62.0
    sa.SOLVE = False
    sa.build()
    names = sorted(sa.MOVES)
    if pats:
        names = [n for n in names if any(re.fullmatch(p, n) for p in pats[0].split(","))]
    with multiprocessing.get_context("fork").Pool(max(1, os.cpu_count() or 1)) as pool:
        for path in pool.imap(one, [(n, out, views, scale) for n in names]):
            print(path)


if __name__ == "__main__":
    main()
