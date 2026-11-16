#!/usr/bin/env python3
"""2.1.1: the fighting styles' animations (the owner: "custom movesets, extremely well choreographed").

Writes src/main/resources/assets/hexhaki/player_animation/style_<style>_<move>.json for every StyleMove (and the Shark Skin Counter's
answering palm). Every move's strike lands on the very tick the server lands its blow (StyleMove's hits; StyleTests checks the
animation has a keyframe there and lasts as long as the move), anticipated by a wind-up, followed through with an overshoot, recovered
into the style's stance. The spine rule of tools/anim_hand.py holds: leans are the body bend (head and arms carried), never a torso
pitch. Whole turns snap back to 0 on a "constant" keyframe once the turn is done (the same facing), so no fade ever unwinds a spin.

Conventions (PlayerAnimator + bendy-lib, degrees): arm pitch - raises forward (-90 level, -180 straight up), + swings back; right arm
roll + out to the side (left -); a raised right arm's yaw + swings it out (left -); elbow bend - flexes; leg pitch - forward; knee bend +
flexes; head pitch + looks down; body yaw + turns left (the right shoulder comes forward); body z - moves forward; body y + up (blocks);
body bend + hunches forward.

Usage: python3 tools/style_anims.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from anim_hand import arms, legs, pose, spine  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
PARTS = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "torso": ("pitch", "yaw", "roll", "bend"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "rightLeg": ("pitch", "yaw", "roll", "bend"),
    "leftLeg": ("pitch", "yaw", "roll", "bend"),
}
WRITTEN = {}


def arm(pitch=0.0, yaw=0.0, roll=0.0, bend=0.0):
    return {"pitch": pitch, "yaw": yaw, "roll": roll, "bend": bend}


def leg(pitch=0.0, roll=0.0, bend=0.0, yaw=0.0):
    return {"pitch": pitch, "roll": roll, "bend": bend, "yaw": yaw}


def mirror(a):
    """A right arm or leg pose for the left side."""
    return {"pitch": a.get("pitch", 0.0), "yaw": -a.get("yaw", 0.0), "roll": -a.get("roll", 0.0), "bend": a.get("bend", 0.0)}


def p(*pieces, **parts):
    """A pose: the pieces merged in order, then the parts given (each replaces only the channels it names)."""
    out = {}
    for piece in pieces:
        for part, values in (piece or {}).items():
            out.setdefault(part, {}).update(values)
    for part, values in parts.items():
        out.setdefault(part, {}).update(values)
    return out


def write(name, duration, beats, description, stop_extra=3):
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part, channels in PARTS.items():
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.1.1", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + stop_extra, "isLoop": False, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def write_stance(name, duration, beats, description):
    """2.1.2: a combat-mode stance (R): a loop of the ARMS only. Every other part is left out of the file, so the game keeps animating it
    (walking, sneaking, looking round); the stance plays on its own layer under every move (client/StanceLayer), which eases in from it
    and back into it. The first and last beats are the same pose, so the loop has no seam."""
    assert beats[0][2] == beats[-1][2] and beats[-1][0] == duration, name + ": a loop ends where it starts"
    moves = []
    for tick, easing, q in beats:
        for part in ("rightArm", "leftArm"):
            values = q[part]
            for channel in PARTS[part]:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.1.2", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration, "isLoop": True, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def breath(arms_pose, lift=1.0):
    """The stance breathing in: the shoulders rise a little, the elbows ease (degrees scaled by lift)."""
    out = {}
    for part, values in arms_pose.items():
        v = dict(values)
        side = 1.0 if part == "rightArm" else -1.0
        v["pitch"] = v.get("pitch", 0.0) - 1.6 * lift
        v["roll"] = v.get("roll", 0.0) + 1.2 * lift * side
        v["bend"] = v.get("bend", 0.0) + 3.0 * lift
        out[part] = v
    return out


def stances():
    """2.1.2: the four combat stances, held while combat mode is on (R), a slow breath (40 ticks, 2 s) each."""
    def loop(name, arms_pose, description, lift=1.0):
        held = {k: dict(v) for k, v in arms_pose.items()}
        write_stance(name, 40, [(0, "linear", held), (20, "inoutsine", breath(held, lift)), (40, "inoutsine", held)], description)

    loop("style_black_leg_stance", BL_STANCE,
         "Black Leg combat stance: the hands clasped behind the back, as a cook keeps them out of the fight.", lift=0.5)
    loop("style_six_powers_stance", SP_STANCE,
         "Six Powers combat stance: upright and correct, the left hand behind the back, the right loose at the side.", lift=0.7)
    loop("style_fishman_karate_stance", FK_STANCE,
         "Fishman Karate combat stance: the right fist chambered at the hip, the left palm open forward.")
    loop("style_brawler_stance", IF_STANCE,
         "Iron Fist combat stance: a boxer's guard, both fists up at the chin, elbows in, bouncing on the breath.", lift=1.6)


def shake(base, ticks, amp, parts=(("body", "roll"),)):
    beats = []
    for i, t in enumerate(ticks):
        q = p(base)
        for part, channel in parts:
            q.setdefault(part, {})
            q[part][channel] = q[part].get(channel, 0.0) + (amp if i % 2 == 0 else -amp)
        beats.append((t, "inoutsine", q))
    return beats


# ------------------------------------------------------------------------------------------------------------- Black Leg
# Hands clasped behind the back the whole time; the weight on the back leg, the kicking leg light.
# 2.1.2: the hands truly clasped at the small of the back (solved on the model: the hands meet behind the spine, the elbows out behind,
# no arm through the torso), where 2.1.1 only hung them at the back of each hip.
BACK_HAND = arm(94, -48, -75, -81)
BL_HANDS = {"rightArm": BACK_HAND, "leftArm": mirror(BACK_HAND)}
BL = p(BL_HANDS, rightLeg=leg(4, 2, 8), leftLeg=leg(-6, -2, 12), body={"y": -0.03, "yaw": -8}, head={"pitch": -2})
BL_STANCE = p(BL_HANDS)


def black_leg():
    s = "style_black_leg_"
    write(s + "snap", 9, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, rightLeg=leg(-76, 4, 104), leftLeg=leg(2, -2, 18), body={"y": -0.05, "yaw": -4, "pitch": 6})),
        (4, "outexpo", p(BL, rightLeg=leg(-92, 2, 2), leftLeg=leg(4, -2, 14), body={"y": -0.02, "z": -0.06, "pitch": 12, "yaw": -2})),
        (5, "outback", p(BL, rightLeg=leg(-96, 2, 0), leftLeg=leg(4, -2, 14), body={"y": -0.02, "z": -0.07, "pitch": 13, "yaw": -2})),
        (7, "inoutsine", p(BL, rightLeg=leg(-58, 4, 84), body={"y": -0.04, "pitch": 6, "yaw": -6})),
        (9, "inoutsine", BL),
    ], "Black Leg: a front kick snapped out from the knee, the hands behind the back.")
    # the spinning hook: a full turn on the standing foot, the heel hooking through at three quarters of it
    write(s + "hook", 11, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"yaw": -55, "y": -0.06}, leftLeg=leg(-4, -3, 22), rightLeg=leg(10, 6, 30))),
        (4, "insine", p(BL, body={"yaw": 110, "y": -0.04, "roll": -6}, rightLeg=leg(-30, 40, 70), leftLeg=leg(0, -3, 18))),
        (6, "outexpo", p(BL, body={"yaw": 250, "y": -0.02, "roll": -14}, rightLeg=leg(-42, 74, 6), leftLeg=leg(2, -4, 12))),
        (7, "outsine", p(BL, body={"yaw": 330, "y": -0.03, "roll": -8}, rightLeg=leg(-26, 46, 50), leftLeg=leg(0, -3, 16))),
        (8, "constant", p(BL, body={"yaw": 0, "y": -0.03, "roll": -4}, rightLeg=leg(-10, 18, 40), leftLeg=leg(-4, -3, 16))),
        (11, "inoutsine", BL),
    ], "Black Leg: a full turn on the standing foot into a hooking heel.")
    write(s + "sweep", 11, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"y": -0.42, "yaw": 70, "bend": 18}, leftLeg=leg(-48, -6, 110), rightLeg=leg(-6, 58, 8),
                           head={"pitch": -10})),
        (5, "outexpo", p(BL, body={"y": -0.46, "yaw": -12, "bend": 20}, leftLeg=leg(-50, -6, 116), rightLeg=leg(-10, 66, 4),
                         head={"pitch": -12})),
        (6, "outback", p(BL, body={"y": -0.46, "yaw": -40, "bend": 20}, leftLeg=leg(-50, -6, 116), rightLeg=leg(-8, 60, 6),
                         head={"pitch": -12})),
        (8, "inoutsine", p(BL, body={"y": -0.2, "yaw": -20, "bend": 10}, leftLeg=leg(-24, -4, 60), rightLeg=leg(-2, 20, 30))),
        (11, "inoutsine", BL),
    ], "Black Leg: dropped low, the leg scythes their feet away.")
    write(s + "mutton", 16, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"y": -0.12}, rightLeg=leg(-10, 2, 40), leftLeg=leg(-8, -2, 40))),
        (5, "outsine", p(BL, body={"y": 0.18, "pitch": 16}, rightLeg=leg(-150, 6, 10), leftLeg=leg(8, -2, 20), head={"pitch": -14})),
        (8, "inoutsine", p(BL, body={"y": 0.22, "pitch": 20}, rightLeg=leg(-168, 4, 0), leftLeg=leg(10, -2, 24), head={"pitch": -16})),
        (10, "outexpo", p(BL, body={"y": -0.04, "pitch": -6, "bend": 10}, rightLeg=leg(-38, 4, 0), leftLeg=leg(6, -2, 30),
                          head={"pitch": 8})),
        (11, "outback", p(BL, body={"y": -0.12, "pitch": -8, "bend": 14}, rightLeg=leg(-30, 4, 4), leftLeg=leg(6, -2, 44),
                          head={"pitch": 10})),
        (13, "inoutsine", p(BL, body={"y": -0.08, "bend": 6}, rightLeg=leg(-14, 2, 30), leftLeg=leg(0, -2, 30))),
        (16, "inoutsine", BL),
    ], "Black Leg: a hop and an axe kick brought down from overhead.")
    write(s + "neck", 16, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"z": -0.12, "y": -0.06}, leftLeg=leg(-22, -3, 24), rightLeg=leg(12, 3, 20))),
        (4, "insine", p(BL, body={"z": -0.2, "y": -0.04, "yaw": 38, "roll": -10}, rightLeg=leg(-66, 40, 108), leftLeg=leg(-4, -3, 20))),
        (7, "outexpo", p(BL, body={"z": -0.24, "y": 0.0, "yaw": 78, "roll": -24}, rightLeg=leg(-104, 62, 8), leftLeg=leg(2, -4, 10),
                         head={"yaw": -30})),
        (8, "outback", p(BL, body={"z": -0.24, "y": 0.0, "yaw": 84, "roll": -26}, rightLeg=leg(-110, 64, 4), leftLeg=leg(2, -4, 10),
                         head={"yaw": -32})),
        (11, "inoutsine", p(BL, body={"z": -0.1, "y": -0.04, "yaw": 30, "roll": -8}, rightLeg=leg(-40, 24, 70), leftLeg=leg(0, -3, 18))),
        (16, "inoutsine", BL),
    ], "Black Leg: a lunging roundhouse to the neck that launches them skyward.")
    out = p(BL, rightLeg=leg(-8, 84, 6), leftLeg=leg(-4, -4, 26), body={"y": -0.08})
    write(s + "flame", 22, [
        (0, "linear", BL),
        (4, "inoutsine", p(BL, body={"y": -0.14, "yaw": -45}, rightLeg=leg(-10, 30, 50), leftLeg=leg(-6, -3, 40))),
        (6, "insine", p(out, body={"y": -0.08, "yaw": 0})),
        (8, "linear", p(out, body={"y": -0.08, "yaw": 180})),
        (10, "linear", p(out, body={"y": -0.08, "yaw": 360})),
        (12, "linear", p(out, body={"y": -0.08, "yaw": 540})),
        (14, "linear", p(out, body={"y": -0.08, "yaw": 720})),
        (16, "linear", p(out, body={"y": -0.06, "yaw": 900})),
        (18, "outsine", p(out, body={"y": -0.06, "yaw": 1080})),
        (19, "constant", p(BL, body={"y": -0.06, "yaw": 0}, rightLeg=leg(-20, 40, 40))),
        (22, "inoutsine", BL),
    ], "Black Leg: the Flame Leg, a spin on one leg until it burns, three blazing sweeps all round.")
    write(s + "sky", 26, [
        (0, "linear", BL),
        (1, "inoutsine", p(BL, body={"y": -0.16, "bend": 10}, rightLeg=leg(-20, 2, 60), leftLeg=leg(-20, -2, 60))),
        (3, "outexpo", p(BL, body={"y": 0.05, "pitch": -6}, rightLeg=leg(14, 2, 6), leftLeg=leg(18, -2, 10))),
        (6, "outsine", p(BL, body={"pitch": -4}, rightLeg=leg(-46, 4, 70), leftLeg=leg(22, -2, 20))),
        (9, "outsine", p(BL, body={"pitch": -4}, rightLeg=leg(20, 2, 20), leftLeg=leg(-46, -4, 70))),
        (12, "inoutsine", p(BL, body={"pitch": 10, "bend": 10}, rightLeg=leg(-70, 4, 100), leftLeg=leg(-66, -4, 104), head={"pitch": 8})),
        (15, "inoutsine", p(BL, body={"pitch": 18}, rightLeg=leg(-172, 4, 0), leftLeg=leg(8, -2, 30), head={"pitch": -12})),
        (19, "outexpo", p(BL, body={"pitch": -10, "bend": 16, "y": -0.12}, rightLeg=leg(-34, 4, 0), leftLeg=leg(6, -2, 60),
                          head={"pitch": 12})),
        (20, "outback", p(BL, body={"pitch": -12, "bend": 20, "y": -0.2}, rightLeg=leg(-26, 4, 6), leftLeg=leg(4, -2, 80),
                          head={"pitch": 12})),
        (23, "inoutsine", p(BL, body={"y": -0.08, "bend": 6}, rightLeg=leg(-10, 2, 30), leftLeg=leg(0, -2, 30))),
        (26, "inoutsine", BL),
    ], "Black Leg: kicking off the air itself, high, then a heel dropped into the ground.")


# ------------------------------------------------------------------------------------------------------------- Six Powers
# Upright and correct: the left hand behind the back, the right relaxed at the side, ready.
SP = p(arms(right=arm(-8, 0, 6, -14), left=mirror(BACK_HAND)), legs(6, 2.5), body={"yaw": -6})
SP_STANCE = p(arms(right=arm(-8, 0, 6, -14), left=mirror(BACK_HAND)))


def six_powers():
    s = "style_six_powers_"
    for side, name in (("right", "finger_r"), ("left", "finger_l")):
        right = side == "right"
        hand = "rightArm" if right else "leftArm"
        other = "leftArm" if right else "rightArm"
        turn = 1 if right else -1
        pulled = arm(22, 0, 8 * turn, -92)
        thrust = arm(-90, -4 * turn, 2 * turn, -2)
        rest_other = SP[other] if right else arm(-8, 0, -6, -14)
        write(s + name, 8, [
            (0, "linear", SP),
            (1, "outsine", p(SP, **{hand: pulled if right else mirror(pulled), other: rest_other}, body={"yaw": -14 * turn, "y": -0.03},
                             rightLeg=leg(-2, 2, 14), leftLeg=leg(-10, -2, 18))),
            (3, "outexpo", p(SP, **{hand: thrust, other: rest_other}, body={"yaw": 20 * turn, "z": -0.08, "y": -0.04, "bend": 4},
                             rightLeg=leg(6, 2, 12), leftLeg=leg(-16, -2, 20), head={"yaw": -10 * turn})),
            (4, "outback", p(SP, **{hand: arm(-93, -4 * turn, 2 * turn, 0), other: rest_other}, body={"yaw": 23 * turn, "z": -0.1, "y": -0.04, "bend": 5},
                             rightLeg=leg(6, 2, 12), leftLeg=leg(-16, -2, 20), head={"yaw": -11 * turn})),
            (6, "inoutsine", p(SP, **{hand: arm(-40, 0, 6 * turn, -60), other: rest_other}, body={"yaw": 6 * turn, "y": -0.02})),
            (8, "inoutsine", SP),
        ], "Six Powers: a finger driven in like a bullet.")
    write(s + "storm", 12, [
        (0, "linear", SP),
        (2, "inoutsine", p(SP, body={"yaw": 34, "y": -0.04}, rightLeg=leg(-42, 22, 92), leftLeg=leg(0, -2, 20))),
        (6, "outexpo", p(SP, body={"yaw": -28, "roll": -10, "y": 0.0}, rightLeg=leg(-112, 40, 2), leftLeg=leg(4, -3, 10), head={"yaw": 18})),
        (7, "outback", p(SP, body={"yaw": -36, "roll": -12, "y": 0.0}, rightLeg=leg(-116, 44, 0), leftLeg=leg(4, -3, 10), head={"yaw": 20})),
        (9, "inoutsine", p(SP, body={"yaw": -10, "y": -0.03}, rightLeg=leg(-40, 14, 70), leftLeg=leg(0, -2, 16))),
        (12, "inoutsine", SP),
    ], "Six Powers: a kick fast enough to cut the air in front of it.")
    write(s + "palm", 14, [
        (0, "linear", SP),
        (2, "inoutsine", p(SP, arms(right=arm(26, 0, 10, -100), left=arm(26, 0, -10, -100)), body={"y": -0.08},
                           rightLeg=leg(8, 3, 24), leftLeg=leg(-20, -3, 28))),
        (5, "insine", p(SP, arms(right=arm(18, 0, 10, -104), left=arm(18, 0, -10, -104)), body={"y": -0.08, "z": -0.14},
                        rightLeg=leg(10, 3, 22), leftLeg=leg(-28, -3, 30))),
        (7, "outexpo", p(SP, arms(right=arm(-88, -8, 2, -2), left=arm(-88, 8, -2, -2)), body={"y": -0.06, "z": -0.26, "bend": 8},
                         rightLeg=leg(14, 3, 16), leftLeg=leg(-30, -3, 26), head={"pitch": -6})),
        (8, "outback", p(SP, arms(right=arm(-92, -8, 2, 0), left=arm(-92, 8, -2, 0)), body={"y": -0.06, "z": -0.28, "bend": 10},
                         rightLeg=leg(14, 3, 16), leftLeg=leg(-30, -3, 26), head={"pitch": -7})),
        (11, "inoutsine", p(SP, arms(right=arm(-40, 0, 8, -50), left=arm(-40, 0, -8, -50)), body={"y": -0.03, "z": -0.1})),
        (14, "inoutsine", SP),
    ], "Six Powers: both palms driven into the chest with the whole body behind them.")
    run_a = p(SP, arms(right=arm(48, 0, 12, -40), left=arm(48, 0, -12, -40)), body={"bend": 26, "y": -0.1},
              rightLeg=leg(-52, 2, 60), leftLeg=leg(32, -2, 44), head={"pitch": -18})
    write(s + "shave", 10, [
        (0, "linear", SP),
        (1, "outexpo", p(run_a, body={"bend": 22, "y": -0.16}, rightLeg=leg(-20, 2, 70), leftLeg=leg(-10, -2, 70))),
        (3, "linear", run_a),
        (4, "linear", p(run_a, rightLeg=leg(30, 2, 44), leftLeg=leg(-52, -2, 60))),
        (6, "outexpo", p(SP, arms(right=arm(-90, -6, 4, -4), left=arm(26, 0, -10, -90)), body={"bend": 10, "y": -0.06, "z": -0.1, "yaw": 22},
                         rightLeg=leg(10, 3, 18), leftLeg=leg(-26, -3, 24))),
        (7, "outback", p(SP, arms(right=arm(-93, -6, 4, -2), left=arm(26, 0, -10, -90)), body={"bend": 11, "y": -0.06, "z": -0.12, "yaw": 24},
                         rightLeg=leg(10, 3, 18), leftLeg=leg(-26, -3, 24))),
        (10, "inoutsine", SP),
    ], "Six Powers: Shave, the ground kicked ten times in a blink: gone, and there, with a palm.")
    write(s + "blade", 16, [
        (0, "linear", SP),
        (3, "inoutsine", p(SP, body={"yaw": -58, "y": -0.06}, rightLeg=leg(-30, 20, 96), leftLeg=leg(-4, -3, 26))),
        (7, "outexpo", p(SP, body={"yaw": 40, "roll": -16, "y": 0.0}, rightLeg=leg(-124, 22, 0), leftLeg=leg(4, -3, 8), head={"yaw": -14})),
        (8, "outback", p(SP, body={"yaw": 46, "roll": -18}, rightLeg=leg(-128, 24, 0), leftLeg=leg(4, -3, 8), head={"yaw": -15})),
        (11, "inoutsine", p(SP, body={"yaw": 12, "y": -0.04}, rightLeg=leg(-40, 10, 70), leftLeg=leg(0, -2, 18))),
        (16, "inoutsine", SP),
    ], "Six Powers: a high kick that sends a crescent of cut air flying.")
    brace = p(arms(right=arm(-78, -46, 10, -72), left=arm(-74, 46, -10, -72)), legs(36, 17), body={"y": -0.16, "bend": 6},
              head={"pitch": 9})
    write(s + "iron", 40, [(0, "linear", SP), (3, "outexpo", p(brace, body={"y": -0.2, "bend": 8})), (5, "outback", brace)]
          + shake(brace, list(range(7, 36, 2)), 0.9, (("body", "roll"), ("head", "roll")))
          + [(37, "inoutsine", brace), (40, "inoutsine", SP)],
          "Six Powers: Iron Body, every muscle locked, arms crossed and legs planted, trembling with the strain.")


# ------------------------------------------------------------------------------------------------------------- Fishman Karate
# A wide, low stance: the left palm forward, the right chambered at the hip.
FK = p(arms(right=arm(20, 0, 12, -100), left=arm(-70, 10, -6, -24)), legs(30, 14), body={"y": -0.12, "yaw": -14})
FK_STANCE = p(arms(right=arm(20, 0, 12, -100), left=arm(-70, 10, -6, -24)))


def fishman_karate():
    s = "style_fishman_karate_"
    write(s + "palm", 9, [
        (0, "linear", FK),
        (2, "inoutsine", p(FK, rightArm=arm(28, 0, 12, -108), leftArm=arm(-60, 10, -6, -30), body={"y": -0.13, "yaw": -24})),
        (4, "outexpo", p(FK, rightArm=arm(-88, -4, 4, -6), leftArm=arm(24, 0, -12, -100), body={"y": -0.12, "yaw": 22, "z": -0.1})),
        (5, "outback", p(FK, rightArm=arm(-91, -4, 4, -4), leftArm=arm(26, 0, -12, -102), body={"y": -0.12, "yaw": 25, "z": -0.11})),
        (7, "inoutsine", p(FK, rightArm=arm(-40, 0, 8, -60), body={"y": -0.12, "yaw": 0})),
        (9, "inoutsine", FK),
    ], "Fishman Karate: an open palm thrust from the hip.")
    write(s + "backfist", 10, [
        (0, "linear", FK),
        (2, "inoutsine", p(FK, rightArm=arm(-62, -54, 16, -108), body={"y": -0.12, "yaw": 22})),
        (5, "outexpo", p(FK, rightArm=arm(-86, 34, 14, -12), body={"y": -0.12, "yaw": -22}, head={"yaw": 8})),
        (6, "outback", p(FK, rightArm=arm(-88, 40, 16, -8), body={"y": -0.12, "yaw": -26}, head={"yaw": 9})),
        (8, "inoutsine", p(FK, rightArm=arm(-30, 10, 10, -70), body={"y": -0.12, "yaw": -10})),
        (10, "inoutsine", FK),
    ], "Fishman Karate: the same arm snaps back across.")
    write(s + "knee", 10, [
        (0, "linear", FK),
        (2, "inoutsine", p(FK, arms(right=arm(-82, -10, 4, -50), left=arm(-82, 10, -4, -50)), body={"y": -0.06, "yaw": 0},
                           rightLeg=leg(4, 6, 20), leftLeg=leg(-6, -6, 20))),
        (5, "outexpo", p(FK, arms(right=arm(-44, -10, 6, -84), left=arm(-44, 10, -6, -84)), body={"y": 0.02, "pitch": -8, "bend": 6},
                         rightLeg=leg(-104, 4, 124), leftLeg=leg(4, -3, 8), head={"pitch": 6})),
        (6, "outback", p(FK, arms(right=arm(-40, -10, 6, -88), left=arm(-40, 10, -6, -88)), body={"y": 0.03, "pitch": -9, "bend": 7},
                         rightLeg=leg(-110, 4, 126), leftLeg=leg(4, -3, 8), head={"pitch": 7})),
        (8, "inoutsine", p(FK, body={"y": -0.06}, rightLeg=leg(-30, 8, 50), leftLeg=leg(0, -10, 24))),
        (10, "inoutsine", FK),
    ], "Fishman Karate: a knee driven up through the guard.")
    write(s + "brick", 16, [
        (0, "linear", FK),
        (3, "inoutsine", p(FK, rightArm=arm(42, 10, 16, -112), leftArm=arm(-74, 14, -6, -16), body={"y": -0.16, "yaw": -40},
                           head={"yaw": 30})),
        (6, "insine", p(FK, rightArm=arm(38, 10, 16, -116), leftArm=arm(-78, 14, -6, -12), body={"y": -0.16, "yaw": -42, "z": -0.1},
                        head={"yaw": 32})),
        (9, "outexpo", p(FK, rightArm=arm(-90, -4, 2, -2), leftArm=arm(30, 0, -12, -104), body={"y": -0.12, "yaw": 30, "z": -0.26, "bend": 8},
                         head={"yaw": -12})),
        (10, "outback", p(FK, rightArm=arm(-93, -4, 2, 0), leftArm=arm(32, 0, -12, -106), body={"y": -0.12, "yaw": 33, "z": -0.28, "bend": 9},
                          head={"yaw": -13})),
        (13, "inoutsine", p(FK, rightArm=arm(-50, 0, 8, -50), body={"y": -0.12, "yaw": 6, "z": -0.1})),
        (16, "inoutsine", FK),
    ], "Fishman Karate: the Thousand-Brick Fist, its shock passing through them like a wave.")
    up = p(FK, rightArm=arm(-82, -10, 4, -62), body={"y": -0.1, "yaw": 6})
    flick = p(FK, rightArm=arm(-96, -6, 4, -4), body={"y": -0.1, "yaw": 10})
    write(s + "bullets", 16, [
        (0, "linear", FK), (3, "inoutsine", up), (5, "outexpo", flick), (6, "inoutsine", up), (8, "outexpo", flick), (9, "inoutsine", up),
        (11, "outexpo", p(flick, rightArm=arm(-100, -6, 4, 0), body={"y": -0.1, "yaw": 14, "z": -0.05})),
        (12, "outback", p(flick, rightArm=arm(-102, -6, 4, 0), body={"y": -0.1, "yaw": 15, "z": -0.05})),
        (16, "inoutsine", FK),
    ], "Fishman Karate: three drops of water flicked from the fingers, hard as shot.")
    ready = p(arms(right=arm(-58, -26, 10, -112), left=arm(-72, 12, -6, -30)), legs(34, 15), body={"y": -0.14, "yaw": -18}, head={"pitch": 3})
    write(s + "counter", 25, [(0, "linear", FK), (3, "outexpo", ready)] + shake(ready, [6, 10, 14, 18], 1.2, (("body", "yaw"),))
          + [(21, "inoutsine", ready), (25, "inoutsine", FK)],
          "Fishman Karate: the Shark Skin Counter's ready stance.")
    write(s + "counter_strike", 10, [
        (0, "linear", ready),
        (2, "outexpo", p(ready, rightArm=arm(-90, -4, 4, -2), leftArm=arm(28, 0, -12, -100), body={"y": -0.12, "yaw": 26, "z": -0.14})),
        (3, "outback", p(ready, rightArm=arm(-93, -4, 4, 0), leftArm=arm(30, 0, -12, -102), body={"y": -0.12, "yaw": 28, "z": -0.16})),
        (10, "inoutsine", FK),
    ], "Fishman Karate: the answering palm after a blow is turned aside.")
    write(s + "wave", 22, [
        (0, "linear", FK),
        (4, "outsine", p(FK, legs(8, 8), rightArm=arm(-168, -6, 10, -40), leftArm=arm(-40, 10, -20, -30), body={"y": 0.06, "pitch": 10, "yaw": 0},
                         head={"pitch": -16})),
        (9, "insine", p(FK, legs(40, 14), rightArm=arm(-150, -6, 10, -20), leftArm=arm(-30, 10, -24, -30), body={"y": -0.14, "bend": 14, "yaw": 0},
                        head={"pitch": -4})),
        (12, "outexpo", p(FK, legs(78, 18), rightArm=arm(-34, -4, 6, 0), leftArm=arm(20, 10, -30, -40), body={"y": -0.42, "bend": 40, "yaw": 0},
                          head={"pitch": -24})),
        (13, "outback", p(FK, legs(84, 18), rightArm=arm(-30, -4, 6, 0), leftArm=arm(22, 10, -32, -40), body={"y": -0.46, "bend": 42, "yaw": 0},
                          head={"pitch": -25})),
        (17, "inoutsine", p(FK, legs(40, 14), body={"y": -0.2, "bend": 12, "yaw": -6})),
        (22, "inoutsine", FK),
    ], "Fishman Karate: a fist into the ground, the shock running out ahead.")


# ------------------------------------------------------------------------------------------------------------- Iron Fist
# A boxer's guard: fists at the chin, elbows in, the left foot and shoulder forward.
IF_STANCE = p(arms(right=arm(-66, -24, 12, -116), left=arm(-76, 20, -10, -104)))
IF = p(arms(right=arm(-66, -24, 12, -116), left=arm(-76, 20, -10, -104)), rightLeg=leg(12, 3, 18), leftLeg=leg(-14, -3, 16),
       body={"y": -0.05, "yaw": -16}, head={"pitch": 6})


def iron_fist():
    s = "style_brawler_"
    write(s + "jab", 7, [
        (0, "linear", IF),
        (3, "outexpo", p(IF, leftArm=arm(-90, 6, -2, -4), body={"y": -0.05, "yaw": -24, "z": -0.05}, leftLeg=leg(-18, -3, 18))),
        (4, "outback", p(IF, leftArm=arm(-93, 6, -2, -2), body={"y": -0.05, "yaw": -26, "z": -0.06}, leftLeg=leg(-18, -3, 18))),
        (7, "inoutsine", IF),
    ], "Iron Fist: a quick lead-hand jab.")
    write(s + "cross", 9, [
        (0, "linear", IF),
        (1, "inoutsine", p(IF, body={"y": -0.06, "yaw": -22})),
        (4, "outexpo", p(IF, rightArm=arm(-90, -6, 2, -2), body={"y": -0.05, "yaw": 26, "z": -0.1}, rightLeg=leg(14, 3, 14, yaw=20),
                         head={"yaw": -14})),
        (5, "outback", p(IF, rightArm=arm(-93, -6, 2, 0), body={"y": -0.05, "yaw": 29, "z": -0.11}, rightLeg=leg(14, 3, 14, yaw=22),
                         head={"yaw": -15})),
        (9, "inoutsine", IF),
    ], "Iron Fist: the rear hand, the hips turned through it.")
    write(s + "hook", 10, [
        (0, "linear", IF),
        (2, "inoutsine", p(IF, body={"y": -0.07, "yaw": -32}, leftArm=arm(-70, -10, -40, -96))),
        (5, "outexpo", p(IF, leftArm=arm(-88, 58, -64, -92), body={"y": -0.06, "yaw": 22}, leftLeg=leg(-14, -3, 18, yaw=-18),
                         head={"yaw": -10})),
        (6, "outback", p(IF, leftArm=arm(-88, 64, -66, -90), body={"y": -0.06, "yaw": 27}, leftLeg=leg(-14, -3, 18, yaw=-20),
                         head={"yaw": -12})),
        (10, "inoutsine", IF),
    ], "Iron Fist: a tight hook round the guard.")
    write(s + "upper", 14, [
        (0, "linear", IF),
        (2, "inoutsine", p(IF, rightArm=arm(14, -6, 12, -104), body={"y": -0.2, "bend": 12, "yaw": -24}, rightLeg=leg(12, 3, 40),
                           leftLeg=leg(-16, -3, 40))),
        (6, "outexpo", p(IF, rightArm=arm(-150, -14, 6, -72), body={"y": 0.06, "pitch": 10, "yaw": 22}, rightLeg=leg(10, 3, 4),
                         leftLeg=leg(-14, -3, 6), head={"pitch": -10})),
        (7, "outback", p(IF, rightArm=arm(-156, -14, 6, -70), body={"y": 0.07, "pitch": 11, "yaw": 25}, rightLeg=leg(10, 3, 4),
                         leftLeg=leg(-14, -3, 6), head={"pitch": -11})),
        (10, "inoutsine", p(IF, rightArm=arm(-90, -20, 10, -100), body={"y": -0.02, "pitch": 4, "yaw": 6})),
        (14, "inoutsine", IF),
    ], "Iron Fist: dropped low and driven up through the chin.")
    wound = p(IF, rightArm=arm(46, 6, 30, -64), body={"y": -0.08, "yaw": -58, "bend": -4}, rightLeg=leg(16, 3, 30), leftLeg=leg(-18, -3, 14),
              head={"yaw": 40})
    write(s + "haymaker", 20, [(0, "linear", IF), (4, "inoutsine", wound)] + shake(wound, [6, 8, 10], 1.0, (("rightArm", "roll"),))
          + [(13, "outexpo", p(IF, rightArm=arm(-94, -8, 4, 0), leftArm=arm(26, 0, -12, -100), body={"y": -0.06, "yaw": 46, "z": -0.3, "bend": 10},
                               rightLeg=leg(16, 3, 10, yaw=24), leftLeg=leg(-26, -3, 24), head={"yaw": -18})),
             (14, "outback", p(IF, rightArm=arm(-97, -8, 4, 0), leftArm=arm(28, 0, -12, -102), body={"y": -0.06, "yaw": 50, "z": -0.32, "bend": 11},
                               rightLeg=leg(16, 3, 10, yaw=26), leftLeg=leg(-26, -3, 24), head={"yaw": -19})),
             (17, "inoutsine", p(IF, body={"y": -0.05, "yaw": 10, "z": -0.1})),
             (20, "inoutsine", IF)],
          "Iron Fist: the arm wound all the way back, then everything behind one punch.")
    beats = [(0, "linear", IF), (2, "inoutsine", p(IF, body={"y": -0.07, "yaw": -10, "bend": 8}))]
    right_out = arm(-90, -8, 2, -4)
    left_out = arm(-90, 8, -2, -4)
    right_in = arm(-70, -24, 12, -112)
    left_in = arm(-76, 20, -10, -104)
    for t in range(3, 24):
        right = t % 2 == 0
        beats.append((t, "outsine", p(IF, rightArm=right_out if right else right_in, leftArm=left_in if right else left_out,
                                      body={"y": -0.07, "yaw": 10 if right else -12, "bend": 8, "z": -0.04})))
    beats += [(24, "outexpo", p(IF, rightArm=arm(-93, -8, 2, 0), leftArm=arm(26, 0, -12, -100), body={"y": -0.06, "yaw": 28, "z": -0.14, "bend": 6})),
              (25, "outback", p(IF, rightArm=arm(-96, -8, 2, 0), leftArm=arm(28, 0, -12, -102), body={"y": -0.06, "yaw": 30, "z": -0.15, "bend": 6})),
              (26, "inoutsine", IF)]
    write(s + "gatling", 26, beats, "Iron Fist: a storm of jabs too fast to count, and a straight to end it.")
    charge = p(arms(right=arm(24, 0, 10, -112), left=arm(-40, 20, -24, -118)), body={"bend": 30, "yaw": -36, "y": -0.08}, head={"pitch": -16})
    beats = [(0, "linear", IF), (2, "outexpo", p(charge, rightLeg=leg(-10, 3, 40), leftLeg=leg(-10, -3, 40)))]
    for i, t in enumerate(range(4, 13, 2)):
        forward = i % 2 == 0
        beats.append((t, "inoutsine", p(charge, rightLeg=leg(-48 if forward else 28, 2, 60 if forward else 40),
                                        leftLeg=leg(28 if forward else -48, -2, 40 if forward else 60))))
    beats += [(14, "inoutsine", p(IF, body={"y": -0.06, "yaw": -10, "bend": 6})), (18, "inoutsine", IF)]
    write(s + "charge", 18, beats, "Iron Fist: head down, shoulder first, through the crowd.")


EASINGS = ["linear", "constant", "insine", "outsine", "inoutsine", "inquad", "outquad", "inoutquad", "incubic", "outcubic", "inoutcubic",
           "inexpo", "outexpo", "inoutexpo", "inback", "outback", "inoutback"]
POSE_PARTS = ["body", "head", "rightArm", "leftArm", "rightLeg", "leftLeg"]
POSE_CHANNELS = ["x", "y", "z", "pitch", "yaw", "roll", "bend"]


def java_poses():
    """The same animations for NPCs (PlayerAnimator only animates players): client/npc/StylePoses.java, one packed array each."""
    out = ["package com.hexhaki.piece.client.npc;", "", "/**",
           " * Generated by tools/style_anims.py (do not edit): every fighting-style animation's keyframes, for NPCs. Each array packs, per",
           " * animated channel: part (" + ", ".join(POSE_PARTS) + "), channel (" + ", ".join(POSE_CHANNELS) + "), the keyframe count, then",
           " * tick, value (degrees, or blocks for body x/y/z), easing (" + ", ".join(EASINGS) + ") for each keyframe.",
           " */", "final class StylePoses {", "    private StylePoses() {", "    }", ""]
    names = sorted(WRITTEN)
    out.append("    static float[] of(String name) {")
    out.append("        return switch (name) {")
    for i, name in enumerate(names):
        out.append(f'            case "{name}" -> a{i}();')
    out.append("            default -> null;")
    out.append("        };")
    out.append("    }")
    out.append("")
    for i, name in enumerate(names):
        tracks = {}
        for move in WRITTEN[name]["emote"]["moves"]:
            for part in POSE_PARTS:
                if part in move:
                    for channel, value in move[part].items():
                        if channel in POSE_CHANNELS:
                            tracks.setdefault((part, channel), {})[move["tick"]] = (value, move["easing"])
        values = []
        for (part, channel), frames in sorted(tracks.items()):
            if all(abs(v) < 1e-6 for v, _ in frames.values()):
                continue
            values += [POSE_PARTS.index(part), POSE_CHANNELS.index(channel), len(frames)]
            for tick in sorted(frames):
                value, easing = frames[tick]
                values += [tick, round(value, 3), EASINGS.index(easing) if easing in EASINGS else 0]
        body = ", ".join(f"{v}F" if isinstance(v, float) else str(v) for v in values)
        out.append(f"    /** {name} ({WRITTEN[name]['emote']['endTick']} ticks) */")
        out.append(f"    private static float[] a{i}() {{")
        out.append(f"        return new float[] {{{body}}};")
        out.append("    }")
        out.append("")
    out.append("}")
    path = os.path.join(ROOT, "src/main/java/com/hexhaki/piece/client/npc/StylePoses.java")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(out) + "\n")


def main():
    black_leg()
    six_powers()
    fishman_karate()
    iron_fist()
    stances()
    java_poses()
    print(f"wrote {len(WRITTEN)} style animations and their NPC poses")


if __name__ == "__main__":
    main()
