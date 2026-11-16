#!/usr/bin/env python3
"""Animated GIFs of each fighting style's whole moveset, from the real animation files (tools/anim.py's PlayerAnimator model).

The M1 string is chained as the game chains it: each blow cut into at its cancel tick with PlayerAnimator's 2-tick crossfade; then the
right-click signature and both specials, each easing back to the stance. 2.1.2: it opens on the combat stance (R) and holds it between
moves, as the game does (the stance is an arms-only loop on a layer under the moves: a move fades in from it, and after its endTick
returns to it by its stopTick; the legs are the game's, shown standing). Two views (3/4 front, side); the move's name and key on top; a
red flash where a blow lands, on its hit tick. Real time: one frame per game tick (50 ms).
Usage: java -cp build/preview:build/classes StyleDump > build/preview/styles.json; python3 tools/style_gif.py <out dir>
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402
from PIL import Image, ImageDraw, ImageFont  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANIMS = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
KEYS = {"STANCE": "R", "M1_1": "M1 (1)", "M1_2": "M1 (2)", "M1_3": "M1 (3)", "M1_4": "M1 (4)", "HEAVY": "Right click", "SPECIAL_A": "Y", "SPECIAL_B": "U"}
W, H = 300, 330
SCALE = 6.2


def blend(a, b, t):
    return {p: {c: a[p][c] + (b[p][c] - a[p][c]) * t for c in a[p]} for p in a}


def neutral():
    return {p: {c: 0.0 for c in anim.CHANNELS[p]} for p in anim.PARTS}


STANCE = {"slot": "STANCE", "name": "combat mode"}


def stance_pose(stance, tick):
    """The stance loop at tick (arms only; every other part standing); a style without one (2.1.3: Fishman Karate, Iron Fist) rests
    with its arms hanging, the game's own."""
    return neutral() if stance is None else stance.pose(tick % max(1, stance.end))


def move_pose(a, t, rest):
    """A move at local tick t over the stance: after its endTick it returns to the stance (not to 0) by its stopTick."""
    if t <= a.end or a.stop <= a.end:
        return a.pose(t)
    w = min(1.0, (t - a.end) / (a.stop - a.end))
    return blend(a.pose(a.end), rest, anim.ease("inoutsine", w))


def timeline(moves, stance):
    """[(pose, move, hit)] one per tick."""
    frames = []
    clock = [0]

    def rest():
        clock[0] += 1
        return stance_pose(stance, clock[0])

    for _ in range(30):
        frames.append((rest(), STANCE, False))
    string = [m for m in moves if m["slot"].startswith("M1")]
    others = [m for m in moves if not m["slot"].startswith("M1")]
    previous = None  # (anim, move, local tick) of what was playing
    # the string, chained at cancel ticks with a 2-tick crossfade
    for i, move in enumerate(string):
        a = anim.load(os.path.join(ANIMS, move["animation"] + ".json"))
        last = i == len(string) - 1
        span = a.length() if last else move["cancel"]
        for t in range(0, span + 1):
            here = stance_pose(stance, clock[0] + t)
            pose = move_pose(a, t, here)
            if t < 2:
                before = here if previous is None else previous[0].pose(previous[1] + t)
                pose = blend(before, pose, (t + 1) / 3.0)
            frames.append((pose, move, t in move["hits"]))
        clock[0] += span
        previous = (a, span)
    for _ in range(8):
        frames.append((rest(), STANCE, False))
    for move in others:
        a = anim.load(os.path.join(ANIMS, move["animation"] + ".json"))
        for t in range(0, a.length() + 1):
            here = stance_pose(stance, clock[0] + t)
            pose = move_pose(a, t, here)
            if t < 2:
                pose = blend(here, pose, (t + 1) / 3.0)
            frames.append((pose, move, t in move["hits"]))
        clock[0] += a.length()
        for _ in range(8):
            frames.append((rest(), STANCE, False))
    return frames


def font(size):
    for path in ("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"):
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def striking_point(pose, previous, view, origin):
    """The hand or foot moving fastest this tick, projected."""
    now = anim.skeleton(pose)
    before = anim.skeleton(previous) if previous else now
    best, speed = None, -1
    for part in ("rightArm", "leftArm", "rightLeg", "leftLeg"):
        a, b = now[part][2], before[part][2]
        v = anim.dist(a, b)
        if v > speed:
            best, speed = a, v
    x, y, z = best
    if view == "side":
        sx = -z
    else:
        ang = math.radians(35)
        sx = x * math.cos(ang) - z * math.sin(ang)
    return origin[0] + sx * SCALE, origin[1] + y * SCALE


def render(style, moves, out):
    stance_file = os.path.join(ANIMS, f"style_{style}_stance.json")
    frames = timeline(moves, anim.load(stance_file) if os.path.exists(stance_file) else None)
    title_font, label_font, small = font(17), font(15), font(12)
    accent = {"black_leg": (232, 116, 58), "six_powers": (90, 160, 230), "fishman_karate": (40, 170, 150), "brawler": (210, 70, 70)}[style]
    images = []
    previous = None
    for index, (pose, move, hit) in enumerate(frames):
        img = Image.new("RGB", (W * 2, H + 54), (24, 30, 44))
        d = ImageDraw.Draw(img)
        for y in range(H + 54):
            shade = int(24 + 26 * y / (H + 54))
            d.line([(0, y), (W * 2, y)], fill=(shade, shade + 8, shade + 22))
        d.rectangle([0, 0, W * 2, 30], fill=(14, 18, 28))
        d.text((10, 6), moves[0]["styleName"], font=title_font, fill=accent)
        if move:
            label = f'{KEYS[move["slot"]]}:  {move["name"]}'
            d.text((W * 2 - 10 - d.textlength(label, font=label_font), 8), label, font=label_font, fill=(240, 240, 240))
        for v, view in enumerate(("three", "side")):
            ox = v * W + W / 2
            oy = 54 + 8 * SCALE + 6
            ground = oy + 24 * SCALE
            d.ellipse([ox - 34, ground - 7, ox + 34, ground + 7], fill=(14, 18, 26))
            d.line([(v * W + 14, ground), (v * W + W - 14, ground)], fill=(70, 80, 100), width=1)
            anim.draw(d, pose, (ox, oy), SCALE, view)
            if hit:
                px, py = striking_point(pose, previous, view, (ox, oy))
                for r, col in ((22, (255, 70, 40)), (14, (255, 170, 60)), (7, (255, 245, 210))):
                    d.ellipse([px - r, py - r, px + r, py + r], outline=col, width=3)
            d.text((v * W + 10, H + 32), "3/4 front" if view == "three" else "side", font=small, fill=(150, 160, 180))
        if hit:
            d.rectangle([W - 26, 36, W + 26, 54], fill=(200, 40, 30))
            d.text((W - 14, 38), "HIT", font=small, fill=(255, 255, 255))
        d.text((W * 2 - 70, H + 32), f"tick {index}", font=small, fill=(110, 120, 140))
        images.append(img.convert("P", palette=Image.ADAPTIVE, colors=96))
        previous = pose
    images[0].save(out, save_all=True, append_images=images[1:], duration=50, loop=0, optimize=True, disposal=1)
    return len(images)


def main():
    out_dir = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "docs/images")
    moves = json.load(open(os.path.join(ROOT, "build/preview/styles.json"), encoding="utf-8"))
    os.makedirs(out_dir, exist_ok=True)
    for style in ("black_leg", "six_powers", "fishman_karate", "brawler"):
        mine = [m for m in moves if m["style"] == style]
        order = ["M1_1", "M1_2", "M1_3", "M1_4", "HEAVY", "SPECIAL_A", "SPECIAL_B"]
        mine.sort(key=lambda m: order.index(m["slot"]))
        path = os.path.join(out_dir, f"style-{style.replace('_', '-')}.gif")
        frames = render(style, mine, path)
        print(f"{path}: {frames} frames, {os.path.getsize(path) // 1024} KB")


if __name__ == "__main__":
    main()
