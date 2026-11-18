#!/usr/bin/env python3
"""2.3.3: contact sheet of the NPCs' gestures (tools/preview/GestureDump.java: the game's own npc/GesturePose), each a row of moments,
drawn on the NPC's model as its renderer poses it (straight arms: an NPC has no elbows), from the front and three-quarters.

Usage: python3 tools/gesture_preview.py dump.txt out.png [--keys] [--only NAME,NAME] [--lint]
(2.4.3: --only draws just those gestures; --lint lists every moment a straight arm runs through the head or the chest.)"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_view as sv  # noqa: E402
import sword_rig as rig  # noqa: E402
import numpy as np  # noqa: E402
from PIL import Image, ImageDraw  # noqa: E402

PARTS = ("body", "head", "rightArm", "leftArm", "rightLeg", "leftLeg")


def pose_of(values):
    q = {}
    for i, part in enumerate(PARTS):
        v = values[i * 8:(i + 1) * 8]
        w = v[7]
        if w <= 0:
            continue
        d = {"pitch": v[3] * w, "yaw": v[4] * w, "roll": v[5] * w}
        if part == "body":
            d.update({"x": v[0] * w, "y": v[1] * w, "z": v[2] * w})
        q[part] = d
    return q


UNIT = 0.9375 / 16.0  # one model pixel in the rig's units (blocks, the player's scale)


def inside(point, corners, grow):
    """Is a point inside the box of these 8 corners (bendy order: x, y, z nested), grown by grow model px on every side?"""
    grow *= UNIT
    c = np.array(corners)
    centre = c.mean(axis=0)
    rel = np.array(point) - centre
    for a, b in ((0, 1), (0, 2), (0, 4)):
        e = c[b] - c[a]
        length = np.linalg.norm(e)
        if length < 1e-9:
            continue
        if abs(rel @ (e / length)) >= length / 2 + grow:
            return False
    return True


def lint(rows):
    """[(name, tick, what)]: a straight arm's middle line (past the shoulder) inside the head, or inside the chest by more than half the
    arm's own thickness (an NPC's arm cannot bend round its own body)."""
    out = []
    for name, frames in rows.items():
        for t, values in frames:
            q = pose_of(values)
            boxes = rig.boxes(q)
            head = [c for p, c in boxes if p == "head"][0]
            chest = [c for p, c in boxes if p == "torso"]
            for side in ("rightArm", "leftArm"):
                upper, lower = [np.array(c) for p, c in boxes if p == side]
                top, bottom = upper[[0, 1, 4, 5]].mean(axis=0), lower[[2, 3, 6, 7]].mean(axis=0)
                hit = None
                for k in range(5, 11):  # past the shoulder joint, which every arm raised forward overlaps
                    point = top + (bottom - top) * k / 10.0
                    if inside(point, head, -0.4):
                        hit = "through the head"
                    elif any(inside(point, box, 0.6) for box in chest):
                        hit = "into the chest"
                    if hit:
                        break
                if hit:
                    out.append((name, t, f"{side} {hit}"))
    return out


def keys(rows, out):
    """One moment of each (its middle), large, from the front, three-quarters and the side."""
    views = (("front", 0), ("three", 35), ("side", 90))
    tw, th, scale = 210, 250, 80.0
    img = Image.new("RGB", (tw * len(views), th * len(rows)), (236, 234, 228))
    for r, (name, frames) in enumerate(rows.items()):
        t, values = frames[len(frames) // 2]
        q = pose_of(values)
        for c, (_, yaw) in enumerate(views):
            cam = sv.Camera(yaw, 10.0, scale, tw / 2, th * 0.92)
            cv = sv.Canvas(tw, th)
            sv.draw_figure(cv, cam, q)
            im = cv.image()
            ImageDraw.Draw(im).text((3, 2), f"{name} t{t:g}", fill=(60, 60, 60))
            img.paste(im, (c * tw, r * th))
    img.save(out)


def main():
    rows = {}
    for line in open(sys.argv[1]):
        bits = line.split()
        if len(bits) < 3:
            continue
        rows.setdefault(bits[0], []).append((float(bits[1]), [float(x) for x in bits[2:]]))
    if "--only" in sys.argv:
        wanted = sys.argv[sys.argv.index("--only") + 1].split(",")
        rows = {name: frames for name, frames in rows.items() if name in wanted}
    if "--lint" in sys.argv:
        problems = lint(rows)
        for name, t, what in problems:
            print(f"{name} t{t:g}: {what}")
        print(f"{len(problems)} problems")
        return
    if "--keys" in sys.argv:
        keys(rows, sys.argv[2])
        return
    views = (("front", 0), ("three", 35))
    tw, th, scale = 120, 150, 42.0
    cols = max(len(v) for v in rows.values())
    img = Image.new("RGB", (tw * cols, (th * len(views) + 16) * len(rows)), (236, 234, 228))
    y = 0
    for name, frames in rows.items():
        d = ImageDraw.Draw(img)
        d.text((4, y + 2), name, fill=(20, 20, 20))
        for c, (t, values) in enumerate(frames):
            q = pose_of(values)
            for r, (_, yaw) in enumerate(views):
                cam = sv.Camera(yaw, 10.0, scale, tw / 2, th * 0.9)
                cv = sv.Canvas(tw, th)
                sv.draw_figure(cv, cam, q)
                im = cv.image()
                ImageDraw.Draw(im).text((3, 2), f"t{t:g}", fill=(90, 90, 90))
                img.paste(im, (c * tw, y + 16 + r * th))
        y += th * len(views) + 16
    img.save(sys.argv[2])


if __name__ == "__main__":
    main()
