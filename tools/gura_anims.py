#!/usr/bin/env python3
"""3.1.1: the Gura Gura no Mi's animations (the owner: "Now do Gura Gura, get reference gifs from white beard and black beard both").

Writes src/main/resources/assets/hexhaki/player_animation/gura_<move>.json in tools/style_anims.py's conventions, and their keyframes
for client/npc/GuraPoses (StylePoser samples them: the bubble on the fist sits where the pose puts the fist). Whitebeard's way, as the
references in docs/handoff/gura-ref show it: heavy, wide, every blow thrown from the whole body; the fist drawn back with the bubble on
it and driven out into the air (Kaishin), both arms thrown out to the sides and the air gripped (Shima Yurashi), the leap with the fist
cocked over the head and brought down (Kabutowari, Blackbeard onto Law), both fists driven out (the tsunami), both fists into the ground
(the Ground Splitter). Every strike lands on the tick server/GuraMoves lands it (the tap's punch on 2, the charged punch on 1, the
shake's cracks on 4, the slam on 3, the tsunami on 4, Gekishin on 1, the split on 4). PlayerAnimator freezes every part a clip keys, so
each keys only the parts its move uses, never the head (the look is always the player's own); no stance (the owner removed the Light
fruit's: between moves the body is the game's own).

Usage: python3 tools/gura_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import json  # noqa: E402

from anim_hand import spine  # noqa: E402
from style_anims import OUT as ANIM_DIR, PARTS, WRITTEN, arm, java_poses, leg, mirror, p  # noqa: E402

# 3.1.2 (the owner: "the entire body is being animated, including the legs, which I tell you not to do"; "when you're on the floor
# sneaking and you right click, the entire body just breaks ... Your torso moves forward"): no clip keys a leg, and the body only by its
# turn, its side tilt and its bend at the waist (bendy-lib: the upper body only), never its place or its pitch (those move the whole
# model, legs and all). The arms and the upper body do the work; the legs are always the game's own (walking, standing, sneaking).
BODY = ("yaw", "roll", "bend")


def write_parts(name, duration, beats, description, parts, stop_extra=3):
    """As tools/pika_anims.write_parts, keying only {parts} (never the head, never a leg) and of the body only its {BODY} channels."""
    assert "head" not in parts and "rightLeg" not in parts and "leftLeg" not in parts, name + ": the head and the legs are the player's own"
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part in parts:
            values = q.get(part, {})
            for channel in PARTS[part]:
                if part == "body" and channel not in BODY:
                    continue
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.1.2", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + stop_extra, "isLoop": False, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(ANIM_DIR, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")

REST = p(rightArm=arm(0, 0, 4, -4), leftArm=arm(0, 0, -4, -4), rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0),
         body={"yaw": 0, "bend": 0, "pitch": 0, "roll": 0, "x": 0, "y": 0, "z": 0})
RIGHT = ("body", "torso", "rightArm")
ARMS = ("body", "torso", "rightArm", "leftArm")
ALL = ARMS  # 3.1.2: never the legs


def tremble(base, start, end, every, wobble):
    """Keyframes from {start} to {end} every {every} ticks, {base} shaken by {wobble}(sign) (the quake held in the fist)."""
    beats = []
    sign = 1
    for t in range(start, end + 1, every):
        beats.append((t, "inoutsine", wobble(base, sign)))
        sign = -sign
    return beats


# ------------------------------------------------------------------------------------------------------ RMB: Kaishin

DRAWN = p(rightArm=arm(32, -10, 22, -92), leftArm=arm(-68, -12, -10, -12), body={"yaw": -24, "bend": 6})


def kaishin():
    wind = p(rightArm=arm(20, 0, 12, -74), body={"yaw": -18, "bend": 4})
    strike = p(rightArm=arm(-92, 6, 4, -4), body={"yaw": 22, "bend": 6, "z": -0.06})
    write_parts("gura_punch", 8, [
        (0, "linear", REST),
        (1, "insine", wind),
        (2, "outexpo", strike),
        (4, "outback", p(strike, rightArm={"pitch": -97, "bend": -2}, body={"yaw": 25})),
        (8, "inoutsine", REST),
    ], "Gura Gura: Kaishin tapped: a punch into the air, the bubble on the fist, the air cracking where it strikes.", RIGHT)
    beats = [(0, "linear", REST), (5, "outexpo", DRAWN)]
    beats += tremble(DRAWN, 7, 120, 2, lambda b, s: p(b, rightArm={"roll": 22 + 3 * s, "bend": -92 - 2 * s}, body={"roll": 1.2 * s}))
    write_parts("gura_charge", 120, beats,
                "Gura Gura: the quake gathered on the drawn-back fist, the other arm out ahead, trembling with it.", ARMS)
    punch = p(rightArm=arm(-95, 4, 2, 0), leftArm=arm(12, 0, -14, -42), body={"yaw": 28, "bend": 8, "z": -0.1})
    write_parts("gura_release", 12, [
        (0, "constant", DRAWN),
        (1, "outexpo", punch),
        (4, "outback", p(punch, rightArm={"pitch": -100}, body={"yaw": 31})),
        (12, "inoutsine", REST),
    ], "Gura Gura: Kaishin let go: the fist driven out into the air, the air shattering.", ARMS)


# ------------------------------------------------------------------------------------------------------ Z: Shima Yurashi

OUT_R = arm(-20, 0, 78, -8)
OUT = p(rightArm=OUT_R, leftArm=mirror(OUT_R), body={"pitch": 4, "bend": 2})


def shima():
    gather = p(rightArm=arm(-80, -40, 0, -60), leftArm=mirror(arm(-80, -40, 0, -60)), body={"bend": 8})
    beats = [(0, "linear", REST), (2, "insine", gather), (4, "outexpo", OUT)]
    beats += tremble(OUT, 6, 100, 3, lambda b, s: p(b, rightArm={"roll": 78 + 4 * s, "bend": -8 - 6 * s},
                                                    leftArm={"roll": -78 - 4 * s, "bend": -8 + 6 * s}, body={"roll": 1.5 * s}))
    write_parts("gura_shake", 100, beats,
                "Gura Gura: Shima Yurashi: both arms thrown out to the sides, the air cracking at both fists, the air gripped and shaken.", ARMS)
    yank = p(rightArm=arm(-40, 20, 30, -50), leftArm=mirror(arm(-40, 20, 30, -50)), body={"bend": 18, "pitch": 0})
    write_parts("gura_tilt", 14, [
        (0, "constant", OUT),
        (2, "outexpo", yank),
        (6, "outsine", p(yank, rightArm={"pitch": -32}, leftArm={"pitch": -32}, body={"bend": 14})),
        (14, "inoutsine", REST),
    ], "Gura Gura: the air pulled down, the island tilted.", ARMS)


# ------------------------------------------------------------------------------------------------------ X: Kabutowari

def kabutowari():
    launch = p(rightLeg=leg(-12, 2, 44), leftLeg=leg(-8, -2, 40), rightArm=arm(-150, 0, 16, -10), leftArm=arm(-60, 0, -20, -10),
               body={"y": -0.15, "bend": 10})
    air = p(rightLeg=leg(-60, 4, 90), leftLeg=leg(-30, -4, 70), rightArm=arm(-168, 0, 16, -10), leftArm=arm(-70, 0, -30, -20),
            body={"y": 0.0, "bend": -6, "pitch": 6})
    beats = [(0, "linear", REST), (2, "outexpo", launch), (5, "outexpo", air)]
    for t in range(8, 21, 4):
        beats.append((t, "inoutsine", p(air, rightArm={"pitch": -168 + (3 if t % 8 else -3)})))
    write_parts("gura_leap", 20, beats, "Gura Gura: Kabutowari's leap, the quake fist cocked over the head.", ALL)
    over = p(air, rightArm=arm(-170, 0, 16, -10), body={"bend": -8})
    wind = p(over, rightArm=arm(-175, 0, 16, -10), body={"bend": -12})
    smash = p(rightArm=arm(-38, 0, 6, 0), leftArm=arm(-50, 0, -40, -20), rightLeg=leg(-72, 6, 104), leftLeg=leg(18, -4, 110),
              body={"bend": 34, "y": -0.28, "pitch": 0})
    write_parts("gura_slam", 12, [
        (0, "outsine", over),
        (2, "insine", wind),
        (3, "outexpo", smash),
        (6, "outsine", p(smash, rightArm={"pitch": -34}, body={"bend": 31})),
        (12, "inoutsine", REST),
    ], "Gura Gura: Kabutowari: the quake fist brought down on their head, landing kneeling.", ALL)


# ------------------------------------------------------------------------------------------------------ C: the tsunami

def tsunami():
    # 3.1.2 (the owner: "He smashes both his arms to the side, each one to the left, one to the right, and then two waves come from
    # both left and right"): the arms gathered crossed before the chest, then smashed out to either side (on tick 4: the air cracks at
    # both fists and the two walls of water start rising), held a moment, back
    gather = p(rightArm=arm(-100, -40, 0, -50), leftArm=mirror(arm(-100, -40, 0, -50)), body={"bend": 6})
    out = p(rightArm=arm(-12, 0, 72, -4), leftArm=mirror(arm(-12, 0, 72, -4)), body={"bend": 10})
    write_parts("gura_tsunami", 16, [
        (0, "linear", REST),
        (2, "insine", gather),
        (4, "outexpo", out),
        (8, "outback", p(out, rightArm={"roll": 77}, leftArm={"roll": -77})),
        (16, "inoutsine", REST),
    ], "Gura Gura: both arms smashed out to the sides, a wave rising on either side.", ARMS)


# ------------------------------------------------------------------------------------------------------ V: Gekishin

DEEP = p(rightArm=arm(40, -12, 24, -100), leftArm=arm(-60, -14, -14, -20), body={"yaw": -32, "bend": 10, "y": -0.06})


def gekishin():
    beats = [(0, "linear", REST), (6, "outexpo", DEEP)]
    beats += tremble(DEEP, 8, 100, 2, lambda b, s: p(b, rightArm={"roll": 24 + 3 * s}, body={"roll": 1.5 * s, "y": -0.06 - 0.01 * s}))
    write_parts("gura_fist_charge", 100, beats, "Gura Gura: Gekishin's bubble gathered on the fist drawn far back, the body coiled.", ARMS)
    coiled = p(DEEP, rightLeg=leg(18, 0, 14), leftLeg=leg(-26, 0, 34))
    hit = p(rightArm=arm(-94, 4, 2, 0), leftArm=arm(16, 0, -16, -50), rightLeg=leg(22, 0, 10), leftLeg=leg(-38, 0, 40),
            body={"yaw": 30, "bend": 10, "z": -0.15, "y": -0.04})
    write_parts("gura_gekishin", 10, [
        (0, "constant", coiled),
        (1, "outexpo", hit),
        (4, "outback", p(hit, rightArm={"pitch": -99}, body={"yaw": 33})),
        (10, "inoutsine", REST),
    ], "Gura Gura: Gekishin: the quake driven point-blank into them, lunging.", ALL)


# ------------------------------------------------------------------------------------------------------ B: the Ground Splitter

def split():
    up = p(rightArm=arm(-170, 0, 16, -30), leftArm=mirror(arm(-170, 0, 16, -30)), rightLeg=leg(-6, 2, 10), leftLeg=leg(-6, -2, 10),
           body={"bend": -10})
    down = p(rightArm=arm(-82, 0, 6, -6), leftArm=mirror(arm(-82, 0, 6, -6)), rightLeg=leg(-42, 4, 72), leftLeg=leg(-36, -4, 66),
             body={"bend": 52, "y": -0.32})
    write_parts("gura_split", 16, [
        (0, "linear", REST),
        (2, "insine", up),
        (4, "outexpo", down),
        (8, "outsine", p(down, body={"bend": 50})),
        (16, "inoutsine", REST),
    ], "Gura Gura: both fists driven down into the ground, the ground splitting ahead.", ALL)


# ------------------------------------------------------------------------------------------------------ End: the ultimate (3.1.6)

# the owner: "the user pulls the fist, like the right fist, like back ... like a little further away from the face, but like around that
# area" (docs/handoff/gura-ref/ultimate-whitebeard-punch.gif: the fist cocked high beside his head, the other arm out ahead): the upper
# arm out to the side and back, the forearm up, the fist beside the head; the body wound up away from the punch, leaning back a little
COCKED = p(rightArm=arm(-88, 64, 10, -100), leftArm=arm(-60, -14, -18, -20), body={"yaw": -24, "bend": -4})


def ultimate():
    beats = [(0, "linear", REST), (8, "outexpo", COCKED)]
    # trembling harder as the bubble grows (server/GuraUltimate.FULL 80), and held so up to its HOLD_MOST 220
    sign = 1
    for t in range(10, 221, 2):
        g = min(1.0, t / 80.0)
        w = 0.6 + 2.4 * g
        beats.append((t, "inoutsine", p(COCKED, rightArm={"roll": 10 + 2.5 * w * sign, "bend": -100 - 1.5 * w * sign},
                                         body={"roll": 0.5 * w * sign, "yaw": -24 - 0.6 * w * sign})))
        sign = -sign
    write_parts("gura_ult_charge", 220, beats,
                "Gura Gura: the ultimate: the right fist cocked back beside the head, the quake bubble swelling on it, trembling harder.", ARMS)
    punch = p(rightArm=arm(-92, 6, 4, -2), leftArm=arm(16, 0, -16, -40), body={"yaw": 32, "bend": 16})
    write_parts("gura_ult_release", 24, [
        (0, "constant", COCKED),
        (2, "outexpo", punch),
        (6, "outback", p(punch, rightArm={"pitch": -80}, body={"yaw": 34, "bend": 20})),
        (14, "outsine", p(punch, rightArm={"pitch": -78}, body={"yaw": 30, "bend": 18})),
        (24, "inoutsine", REST),
    ], "Gura Gura: the ultimate let go: the fist driven out and down, the ground ahead breaking.", ARMS)
    write_parts("gura_ult_drop", 8, [(0, "constant", COCKED), (8, "inoutsine", REST)],
                "Gura Gura: the ultimate given up before it was full: the fist lowered.", ARMS)


def main():
    kaishin()
    shima()
    kabutowari()
    tsunami()
    gekishin()
    split()
    ultimate()
    names = sorted(n for n in WRITTEN if n.startswith("gura_"))
    java_poses(names, "GuraPoses", "tools/gura_anims.py", "the Gura fruit's animations' keyframes, for the quake bubble on the fist")
    print(f"wrote {len(names)} Gura animations and their poses")


if __name__ == "__main__":
    main()
