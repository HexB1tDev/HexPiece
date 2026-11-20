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


# 2.1.3 (the owner: "Fix the Fishman and Iron Fist stances so the player's arms remain naturally lowered when idle, walking or
# sprinting. Selecting a combat style must not permanently activate an attacking pose. Raise the arms only during an attack's
# anticipation, strike and recovery, then blend smoothly back into normal movement. Preserve natural leg movement during attacks that
# allow movement."):
# EASE_IN: no keyframe on tick 0, so every animated part eases out of whatever the player was doing (arms swinging as they walk, a
#   stance below, the blow before) into the wind-up, and back to it after the recovery (stopTick 5 ticks on).
# FREE_LEGS: the legs are left out of the file, so the game keeps walking, sprinting or standing them; the body is never raised or
#   lowered (it would lift or sink the walking feet), its turn, lean and the arms carry the blow. Kicks, the knee, the ground punch
#   and the shoulder charge keep their legs.
PUNCHES = ("style_fishman_karate_palm", "style_fishman_karate_backfist", "style_fishman_karate_brick", "style_fishman_karate_bullet",
           "style_fishman_karate_prison", "style_brawler_jab", "style_brawler_cross",
           "style_brawler_hook", "style_brawler_upper", "style_brawler_haymaker", "style_brawler_gatling",
           "style_six_powers_finger_r", "style_six_powers_finger_l", "style_six_powers_palm")
# 2.4.4: the Brick Fist eases in like a punch but plants its legs (it is stepped into: the owner, "it doesn't look very strong")
EASE_IN = set(PUNCHES) | {"style_fishman_karate_knee", "style_brawler_charge", "style_fishman_karate_brick_fist",
                          # 2.8.4: the extras of the two styles that hold no stance
                          "style_fishman_karate_spear", "style_fishman_karate_drill", "style_fishman_karate_five", "style_brawler_love",
                          "style_brawler_meteor", "style_brawler_honesty"}
FREE_LEGS = set(PUNCHES)


def write(name, duration, beats, description, stop_extra=3):
    moves = []
    if name in EASE_IN:
        beats = [beat for beat in beats if beat[0] > 0]
        stop_extra = max(stop_extra, 5)
    for tick, easing, q in beats:
        q = spine(q)
        if name in FREE_LEGS:
            # the legs walk on their own, so nothing may tip the whole body over them: its height, pitch and roll would sink, lift or
            # skate the feet; a lean back or forward goes into the bend (which carries the head and arms and leaves the legs)
            q = {part: dict(values) for part, values in q.items() if part not in ("rightLeg", "leftLeg")}
            body = q.setdefault("body", {})
            body["bend"] = body.get("bend", 0.0) - body.get("pitch", 0.0)
            body["y"] = body["pitch"] = body["roll"] = 0.0
        for part, channels in PARTS.items():
            if name in FREE_LEGS and part in ("rightLeg", "leftLeg"):
                continue
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.1.3" if name in EASE_IN else "HexPiece 2.1.1", "description": description,
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
    """2.1.2: the combat stances, held while combat mode is on (R), a slow breath (40 ticks, 2 s) each: Black Leg's hands and Six
    Powers' one hand behind the back (relaxed poses, not attacking ones)."""
    def loop(name, arms_pose, description, lift=1.0):
        held = {k: dict(v) for k, v in arms_pose.items()}
        write_stance(name, 40, [(0, "linear", held), (20, "inoutsine", breath(held, lift)), (40, "inoutsine", held)], description)

    loop("style_black_leg_stance", BL_STANCE,
         "Black Leg combat stance: the hands clasped behind the back, as a cook keeps them out of the fight.", lift=0.5)
    loop("style_six_powers_stance", SP_STANCE,
         "Six Powers combat stance: upright and correct, the left hand behind the back, the right loose at the side.", lift=0.7)
    # 2.1.3: Fishman Karate and Iron Fist hold no stance: their arms hang and swing as the game's do until a blow raises them.


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
        # 2.8.2: the leg overhead stays a good way in front of the face (it went up through the head: in first person the whole view)
        (5, "outsine", p(BL, body={"y": 0.18, "pitch": 8}, rightLeg=leg(-124, 6, 10), leftLeg=leg(8, -2, 20), head={"pitch": -14})),
        (8, "inoutsine", p(BL, body={"y": 0.22, "pitch": 10}, rightLeg=leg(-136, 4, 0), leftLeg=leg(10, -2, 24), head={"pitch": -16})),
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
        (15, "inoutsine", p(BL, body={"pitch": 10}, rightLeg=leg(-138, 4, 0), leftLeg=leg(8, -2, 30), head={"pitch": -12})),
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
    # 2.3.8 (the owner: "Water Bullet: The user pulls the right hand up into the sky and 5 dropplets of water form on top of the first
    # in a horizontal arch line over the fist, then the user pushes the hand forward (pointing forward) and the bullets auto fire"): the
    # right hand straight up to the sky by tick 4 (water/WaterBullet.RAISED), held, trembling a little as the drops gather over it
    # (4-12), the eyes on them; pushed forward, pointing, at 14-15; held pointing as the five fire one a tick (16-20), each kicking it
    # back a hair; then down
    # 2.4.4 (the owner: the drops "way too ... to the right side of the player, not in the center, like above the head"): the arch is
    # centred over the head now (WaterBullet.CROWN), its right end over the raised fist; the arm stays straight up (any pose that brings
    # a Minecraft fist nearer the middle puts it in front of the forehead, through the head as it looks up, not up in the sky)
    sky = p(FK, rightArm=arm(-176, 0, 6, -4), leftArm=arm(-40, 22, -12, -84), body={"bend": -5, "yaw": 4}, head={"pitch": -16})
    point = p(FK, rightArm=arm(-92, -6, 4, 0), leftArm=arm(-30, 18, -14, -70), body={"bend": 4, "yaw": 12, "z": -0.06}, head={"pitch": 0})
    kick = p(point, rightArm=arm(-97, -6, 4, -6), body={"bend": 3, "yaw": 11, "z": -0.04})
    beats = [(2, "insine", p(FK, rightArm=arm(-120, 0, 8, -60), leftArm=arm(-50, 18, -10, -70), body={"bend": -2}, head={"pitch": -8})),
             (4, "outexpo", sky)]
    beats += shake(sky, [7, 10, 12], 0.8, (("rightArm", "roll"),))
    beats += [(14, "insine", p(FK, rightArm=arm(-140, -4, 6, -20), leftArm=arm(-36, 20, -12, -76), body={"bend": -2, "yaw": 6})),
              (15, "outexpo", point)]
    for t in range(16, 21):
        beats.append((t, "outsine", kick if t % 2 == 0 else point))
    beats += [(23, "inoutsine", p(point, rightArm=arm(-60, 0, 8, -40))), (28, "inoutsine", FK)]
    write(s + "bullet", 28, beats, "Fishman Karate: the hand raised to the sky, five drops of water gathered over it out of the air, then "
          "pushed forward and fired, each locked onto a foe in front.")
    # 2.3.9 (the owner: "Brick Fist: The user punches where he's looking creating a water shockwave"): the right fist drawn back to the
    # hip and the body wound away from the look, the left hand out ahead sighting (2-6); driven straight out along the look on tick 8
    # (water/BrickFist.STRIKE), the whole body turned through behind it, held out as the shockwave rushes off it, then back.
    # 2.4.4 (the owner: "it doesn't look very strong"): planted and stepped into: the feet set wide (the left forward, knee bent), the
    # body sunk and wound further away (yaw -50), held a beat; then the rear leg drives straight, the hips and shoulders turn through
    # (yaw 40), the body lunges 0.36 forward behind the fist and stays sunk while the wave rushes off it
    stance = {"rightLeg": leg(16, 4, 14), "leftLeg": leg(-20, -4, 22)}
    loaded = {"rightLeg": leg(20, 5, 20), "leftLeg": leg(-24, -4, 30)}
    driven = {"rightLeg": leg(30, 4, 2), "leftLeg": leg(-32, -4, 34)}
    write(s + "brick_fist", 18, [
        (2, "inoutsine", p(FK, stance, rightArm=arm(36, 8, 14, -114), leftArm=arm(-82, 12, -6, -10), body={"yaw": -38, "bend": 3, "y": -0.05},
                           head={"yaw": 28})),
        (6, "insine", p(FK, loaded, rightArm=arm(44, 12, 18, -124), leftArm=arm(-88, 16, -6, -4), body={"yaw": -50, "bend": 6, "z": 0.08,
                        "y": -0.08}, head={"yaw": 34})),
        (7, "linear", p(FK, loaded, rightArm=arm(45, 12, 18, -125), leftArm=arm(-88, 16, -6, -4), body={"yaw": -51, "bend": 6, "z": 0.09,
                        "y": -0.08}, head={"yaw": 35})),
        (8, "outexpo", p(FK, driven, rightArm=arm(-90, -4, 2, 0), leftArm=arm(34, 0, -14, -108), body={"yaw": 36, "z": -0.32, "bend": 10,
                         "y": -0.08}, head={"yaw": -16})),
        (9, "outback", p(FK, driven, rightArm=arm(-94, -4, 2, 0), leftArm=arm(36, 0, -14, -110), body={"yaw": 40, "z": -0.36, "bend": 11,
                         "y": -0.08}, head={"yaw": -18})),
        (12, "inoutsine", p(FK, driven, rightArm=arm(-89, -2, 3, -4), leftArm=arm(28, 0, -12, -102), body={"yaw": 31, "z": -0.3, "bend": 8,
                            "y": -0.07}, head={"yaw": -12})),
        (15, "inoutsine", p(FK, rightArm=arm(-46, 0, 8, -50), body={"yaw": 6, "z": -0.08})),
        (18, "inoutsine", FK),
    ], "Fishman Karate: the Brick Fist, a punch where he looks, stepped into, a shockwave of water rushing out of it.")
    # 2.4.0 (the owner: "Water Prison: The user pulls both hands up into the sky (Needs an entity to target) the entity selected gets
    # trapped"): both hands up to the sky by tick 4 (water/WaterPrison.GATHER), spread, the head up, held, trembling as the water
    # gathers round the target (4-12); on the seal (14) both brought down and forward together, the hands closing on it; then down
    sky = p(FK, rightArm=arm(-176, 0, 8, -6), leftArm=arm(-176, 0, -8, -6), body={"bend": -6}, head={"pitch": -14})
    write(s + "prison", 22, [
        (2, "insine", p(FK, rightArm=arm(-120, 0, 12, -50), leftArm=arm(-120, 0, -12, -50), body={"bend": -3}, head={"pitch": -8})),
        (4, "outexpo", sky)] + shake(sky, [7, 10], 0.9, (("rightArm", "roll"), ("leftArm", "roll"))) + [
        (12, "inoutsine", p(sky, rightArm=arm(-179, 0, 5, -4), leftArm=arm(-179, 0, -5, -4))),
        (14, "outexpo", p(FK, rightArm=arm(-92, -18, 6, -10), leftArm=arm(-92, 18, -6, -10), body={"bend": 6, "z": -0.06}, head={"pitch": 2})),
        (15, "outback", p(FK, rightArm=arm(-90, -20, 6, -6), leftArm=arm(-90, 20, -6, -6), body={"bend": 7, "z": -0.07}, head={"pitch": 2})),
        (18, "inoutsine", p(FK, rightArm=arm(-60, -10, 8, -40), leftArm=arm(-60, 10, -8, -40), body={"bend": 3})),
        (22, "inoutsine", FK),
    ], "Fishman Karate: both hands to the sky, the one in your sights sealed in a ball of water.")


# ------------------------------------------------------------------------------------------------------------- Iron Fist
# A boxer's guard: fists at the chin, elbows in, the left foot and shoulder forward.
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


# ------------------------------------------------------------------------------------------------------------- clash recoils
# 2.3.6 (the owner: "every move is clashable ... I want a impact frame whenever a clash happens"): a blow met by another's (server/
# Clashes) stops dead and the fighter is knocked back from it: the arms (or Black Leg's kicking leg) flung up and back by the shock,
# the head snapped back, the body leaning away and the feet stepping back to catch it, then the stance found again. ClashRules.RECOIL
# ticks (the stagger); the first beat is the instant after the blows met, the peak two ticks on.
RECOIL = 12


def clash_recoils():
    def bare(base, name, description, left_behind=False):
        def flung(a, b):
            """The right arm (the lead) and the left flung by the shock (each pitch, yaw, roll out, bend; the left mirrored); Six
            Powers' left hand stays behind the back."""
            return {"rightArm": arm(*a), "leftArm": base["leftArm"] if left_behind else mirror(arm(*b))}
        # the striking fist bounced back to the shoulder, its elbow thrown out wide; the other arm out for balance
        write(name, RECOIL, [
            (0, "linear", p(base, flung((-74, 0, 46, -92), (-46, 0, 40, -30)), body={"z": 0.08, "bend": -8, "yaw": 5},
                            head={"pitch": -12}, rightLeg=leg(10, 4, 12), leftLeg=leg(-12, -3, 12))),
            (2, "outexpo", p(base, flung((-92, 4, 64, -108), (-52, 0, 58, -22)), body={"z": 0.22, "y": -0.03, "bend": -16, "yaw": 12},
                             head={"pitch": -20}, rightLeg=leg(28, 5, 24), leftLeg=leg(-22, -3, 20))),
            (5, "inoutsine", p(base, flung((-58, 0, 30, -78), (-40, 0, 30, -48)), body={"z": 0.16, "y": -0.13, "bend": 8, "yaw": 4},
                               head={"pitch": 6}, rightLeg=leg(24, 4, 48), leftLeg=leg(-26, -3, 44))),
            (8, "inoutsine", p(base, body={"z": 0.06, "y": -0.07, "bend": 4}, head={"pitch": 2}, rightLeg=leg(12, 3, 26),
                               leftLeg=leg(-12, -3, 24))),
            (RECOIL, "inoutsine", base),
        ], description)

    bare(SP, "style_six_powers_clash", "Six Powers: the blow met and stopped dead, knocked back, the left hand still behind the back.",
         left_behind=True)
    bare(FK, "style_fishman_karate_clash", "Fishman Karate: the blow met and stopped dead, knocked back, the stance found again.")
    bare(IF, "style_brawler_clash", "Iron Fist: the punch met and stopped dead, knocked back, the guard found again.")
    # Black Leg: the hands stay behind the back; the kicking leg is thrown back down and the body hops back off it
    write("style_black_leg_clash", RECOIL, [
        (0, "linear", p(BL, rightLeg=leg(-42, 6, 18), leftLeg=leg(-6, -2, 16), body={"z": 0.08, "bend": -10, "yaw": -4},
                        head={"pitch": -14})),
        (2, "outexpo", p(BL, rightLeg=leg(14, 8, 36), leftLeg=leg(-18, -2, 30), body={"z": 0.24, "y": 0.04, "bend": -18, "yaw": -12,
                                                                                       "roll": 5}, head={"pitch": -22})),
        (5, "inoutsine", p(BL, rightLeg=leg(30, 4, 56), leftLeg=leg(-26, -3, 50), body={"z": 0.18, "y": -0.16, "bend": 10, "yaw": -6},
                           head={"pitch": 8})),
        (8, "inoutsine", p(BL, rightLeg=leg(12, 3, 28), leftLeg=leg(-14, -3, 26), body={"z": 0.06, "y": -0.08, "bend": 4, "yaw": -8})),
        (RECOIL, "inoutsine", BL),
    ], "Black Leg: the kick met and stopped dead, the leg thrown back down, a hop back, the hands still behind the back.")


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


# ------------------------------------------------------------------------------------------------------------- 2.8.4: the extras
# The owner: "add more custom perfect moves (with first person) ... give each their show accurate moves if they lack it". Three more
# per style on C, V and B in combat mode, each a move of the show's.


def extras():
    s = "style_black_leg_"
    # Party Table Kick Course: down onto the hands (the body flipped over the hips, lifted so the hands meet the ground), the legs split
    # wide and spun like a propeller, then back up onto the feet
    hands_down = arms(right=arm(-176, 0, 12, -6), left=arm(-176, 0, -12, -6))
    split = {"rightLeg": leg(-4, 78, 0), "leftLeg": leg(-4, -78, 0)}
    stand = {"y": 0.66, "pitch": -180}
    write(s + "party", 24, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"y": -0.2, "bend": 30}, rightLeg=leg(-30, 4, 70), leftLeg=leg(-30, -4, 70), head={"pitch": 20})),
        (4, "outsine", p(hands_down, split, body=dict(stand, yaw=-8))),
        (7, "linear", p(hands_down, split, body=dict(stand, yaw=200))),
        (10, "linear", p(hands_down, split, body=dict(stand, yaw=420))),
        (13, "linear", p(hands_down, split, body=dict(stand, yaw=640))),
        (16, "linear", p(hands_down, split, body=dict(stand, yaw=860))),
        (18, "outsine", p(hands_down, {"rightLeg": leg(-4, 70, 4), "leftLeg": leg(-4, -70, 4)}, body=dict(stand, yaw=1072))),
        (21, "inoutsine", p(BL, body={"y": -0.16, "bend": 24, "yaw": 1072 + 8}, rightLeg=leg(-26, 4, 60), leftLeg=leg(-26, -4, 60),
                            head={"pitch": 14})),
        (24, "inoutsine", p(BL, body={"yaw": 1080 - 8, "y": -0.03})),
    ], "Black Leg: Party Table Kick Course, down onto the hands, the legs split and spun like a propeller through everything round.")
    # Anti-Manner Kick Course: dropped onto the hands under their guard, the knees tucked, then both legs driven straight up into them
    write(s + "anti", 18, [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"y": -0.24, "bend": 34}, rightLeg=leg(-34, 4, 80), leftLeg=leg(-34, -4, 80), head={"pitch": 22})),
        (5, "outsine", p(arms(right=arm(-176, 0, 10, -14), left=arm(-176, 0, -10, -14)), body={"y": 0.5, "pitch": -168, "z": -0.1},
                         rightLeg=leg(-118, 6, 128), leftLeg=leg(-118, -6, 128))),
        (8, "inoutsine", p(arms(right=arm(-176, 0, 10, -20), left=arm(-176, 0, -10, -20)), body={"y": 0.46, "pitch": -160, "z": -0.14},
                           rightLeg=leg(-128, 6, 136), leftLeg=leg(-128, -6, 136))),
        (9, "outexpo", p(arms(right=arm(-176, 0, 10, -4), left=arm(-176, 0, -10, -4)), body={"y": 0.68, "pitch": -196, "z": -0.2},
                         rightLeg=leg(-14, 5, 0), leftLeg=leg(-14, -5, 0))),
        (11, "outback", p(arms(right=arm(-178, 0, 10, -2), left=arm(-178, 0, -10, -2)), body={"y": 0.68, "pitch": -198, "z": -0.2},
                          rightLeg=leg(-10, 5, 0), leftLeg=leg(-10, -5, 0))),
        (15, "inoutsine", p(BL, body={"y": -0.2, "bend": 28}, rightLeg=leg(-30, 4, 64), leftLeg=leg(-30, -4, 64), head={"pitch": 16})),
        (18, "inoutsine", BL),
    ], "Black Leg: Anti-Manner Kick Course, dropped onto the hands under their guard, both legs driven straight up into them.")
    # Diable Jambe: Premier Hachis: the leg spun until it burns, a storm of blazing kicks into them, the last a roundhouse
    hot = p(BL, rightLeg=leg(-8, 84, 6), leftLeg=leg(-4, -4, 26), body={"y": -0.08})
    beats = [
        (0, "linear", BL),
        (2, "inoutsine", p(BL, body={"y": -0.12, "yaw": -40}, rightLeg=leg(-10, 30, 50), leftLeg=leg(-6, -3, 40))),
        (4, "insine", p(hot, body={"y": -0.08, "yaw": 0})),
        (6, "linear", p(hot, body={"y": -0.08, "yaw": 180})),
        (8, "outsine", p(hot, body={"y": -0.08, "yaw": 352})),
    ]
    high = p(BL, rightLeg=leg(-98, 3, 0), leftLeg=leg(4, -2, 16), body={"pitch": 10, "y": -0.02, "z": -0.04, "yaw": 352})
    low = p(BL, rightLeg=leg(-70, 6, 4), leftLeg=leg(4, -2, 16), body={"pitch": 8, "y": -0.03, "z": -0.04, "yaw": 352 - 10})
    back = p(BL, rightLeg=leg(-46, 4, 88), leftLeg=leg(2, -2, 18), body={"pitch": 6, "y": -0.03, "yaw": 352 - 4})
    t = 9
    for i in range(8):
        beats.append((t, "outexpo", high if i % 2 == 0 else low))
        beats.append((t + 1, "inoutsine", back))
        t += 2
    beats += [
        (25, "inoutsine", p(BL, body={"y": -0.06, "yaw": 352 - 40}, rightLeg=leg(-40, 30, 90), leftLeg=leg(-4, -3, 22))),
        (28, "outexpo", p(BL, body={"yaw": 352 + 40, "roll": -18, "y": 0.0}, rightLeg=leg(-104, 66, 4), leftLeg=leg(2, -4, 10),
                          head={"yaw": -30})),
        (29, "outback", p(BL, body={"yaw": 352 + 46, "roll": -20}, rightLeg=leg(-108, 68, 2), leftLeg=leg(2, -4, 10), head={"yaw": -32})),
        (32, "inoutsine", p(BL, body={"yaw": 352})),
    ]
    write(s + "hachis", 32, beats, "Black Leg: Diable Jambe Premier Hachis, the leg spun until it burns, then a storm of blazing kicks.")

    s = "style_six_powers_"
    # Moon Walk: the air itself kicked down: one leg stamps down and back, the other knee drawn up, up into the sky
    write(s + "moon", 8, [
        (0, "linear", SP),
        (1, "outexpo", p(SP, body={"y": 0.04, "pitch": -4}, rightLeg=leg(34, 3, 2), leftLeg=leg(-74, -3, 96), head={"pitch": -6})),
        (3, "outsine", p(SP, body={"y": 0.06, "pitch": -2}, rightLeg=leg(22, 3, 30), leftLeg=leg(-58, -3, 84))),
        (6, "inoutsine", p(SP, rightLeg=leg(-20, 2, 50), leftLeg=leg(-26, -2, 56))),
        (8, "inoutsine", SP),
    ], "Six Powers: Moon Walk, the air itself kicked down to climb the sky.")
    # Paper Art: limp as a sheet of paper, the body drifting and folding away from every blow, the arms loose
    limp = p(arms(right=arm(-6, 0, 14, -22), left=arm(-6, 0, -14, -22)), legs(18, 6), body={"y": -0.05})
    sway = [(0, "linear", SP), (3, "outsine", limp)]
    side = 1
    for k, tick in enumerate(range(7, 37, 5)):
        side = -side
        sway.append((tick, "inoutsine", p(limp, arms(right=arm(-14 - 6 * side, 0, 18 + 16 * side, -30), left=arm(-14 + 6 * side, 0, -18 + 16 * side, -30)),
                                          body={"x": 0.18 * side, "roll": 16 * side, "yaw": 14 * side, "bend": 8 + 4 * (k % 2), "y": -0.06},
                                          head={"roll": -8 * side, "yaw": -8 * side},
                                          rightLeg=leg(-6, 4 + 6 * side, 22), leftLeg=leg(4, -4 + 6 * side, 22))))
    sway += [(38, "inoutsine", limp), (40, "inoutsine", SP)]
    write(s + "paper", 40, sway, "Six Powers: Paper Art, the body as limp as paper, drifting and folding away from every blow.")
    # Six King Gun: both fists drawn to the hips, the stance sunk, then pressed together into them and the whole body's force let go
    drawn = p(SP, arms(right=arm(24, 0, 10, -104), left=arm(24, 0, -10, -104)), legs(34, 16), body={"y": -0.14, "yaw": 0})
    pressed = p(SP, arms(right=arm(-88, -16, 2, -10), left=arm(-88, 16, -2, -10)), legs(30, 14), body={"y": -0.12, "z": -0.26, "bend": 8, "yaw": 0},
                head={"pitch": -4})
    write(s + "king", 24, [(0, "linear", SP), (4, "inoutsine", drawn), (7, "insine", p(drawn, body={"y": -0.16, "bend": 4})),
                           (10, "outsine", p(pressed, body={"z": -0.2})), (13, "insine", pressed),
                           (14, "outexpo", p(pressed, arms(right=arm(-90, -16, 2, -2), left=arm(-90, 16, -2, -2)), body={"z": -0.3, "bend": 10}))]
          + shake(p(pressed, arms(right=arm(-90, -16, 2, -2), left=arm(-90, 16, -2, -2)), body={"z": -0.3, "bend": 10}), [15, 16, 17, 18], 1.2,
                  (("body", "roll"), ("head", "roll")))
          + [(21, "inoutsine", p(SP, body={"y": -0.05})), (24, "inoutsine", SP)],
          "Six Powers: Six King Gun, both fists pressed against them and the whole body's force let go at once.")

    s = "style_fishman_karate_"
    # Spear Wave: the water in the air gathered in the raised hand and thrown like a javelin
    write(s + "spear", 20, [
        (0, "linear", FK),
        (3, "inoutsine", p(FK, rightArm=arm(-150, 6, 20, 40), leftArm=arm(-80, 14, -6, -10), body={"y": -0.1, "yaw": -34, "pitch": 4},
                           rightLeg=leg(14, 4, 20), leftLeg=leg(-26, -4, 24))),
        (8, "inoutsine", p(FK, rightArm=arm(-160, 10, 24, 44), leftArm=arm(-86, 18, -6, -6), body={"y": -0.1, "yaw": -44, "pitch": 6},
                           rightLeg=leg(18, 4, 22), leftLeg=leg(-30, -4, 26))),
        (11, "outexpo", p(FK, rightArm=arm(-84, -6, 6, -4), leftArm=arm(22, 0, -12, -96), body={"y": -0.12, "yaw": 26, "z": -0.14, "bend": 10},
                          rightLeg=leg(18, 4, 14, yaw=20), leftLeg=leg(-32, -4, 24), head={"yaw": -14})),
        (13, "outback", p(FK, rightArm=arm(-62, -30, 8, -8), leftArm=arm(24, 0, -12, -98), body={"y": -0.12, "yaw": 32, "z": -0.16, "bend": 14},
                          rightLeg=leg(18, 4, 14, yaw=22), leftLeg=leg(-32, -4, 24), head={"yaw": -16})),
        (17, "inoutsine", p(FK, rightArm=arm(-30, 0, 10, -60), body={"y": -0.12, "yaw": 0})),
        (20, "inoutsine", FK),
    ], "Fishman Karate: Spear Wave, the water in the air gathered in the hand and thrown as a spear.")
    # Vagabond Drill: a whirlpool spun up between the hands at the hip, then both palms thrust out together
    cup_a = p(FK, rightArm=arm(-34, -20, 14, -70), leftArm=arm(-52, -46, -4, -80), body={"y": -0.14, "yaw": -32})
    cup_b = p(FK, rightArm=arm(-50, -40, 10, -84), leftArm=arm(-30, -22, -14, -64), body={"y": -0.15, "yaw": -38})
    write(s + "drill", 22, [
        (0, "linear", FK), (3, "inoutsine", cup_a), (5, "inoutsine", cup_b), (7, "inoutsine", cup_a), (9, "inoutsine", cup_b),
        (12, "outexpo", p(FK, arms(right=arm(-90, -10, 2, -2), left=arm(-90, 10, -2, -2)), body={"y": -0.12, "yaw": 0, "z": -0.18, "bend": 8},
                          head={"pitch": -4})),
        (14, "outback", p(FK, arms(right=arm(-92, -10, 2, 0), left=arm(-92, 10, -2, 0)), body={"y": -0.12, "yaw": 0, "z": -0.2, "bend": 9},
                          head={"pitch": -4})),
        (18, "inoutsine", p(FK, arms(right=arm(-40, 0, 10, -60), left=arm(-50, 0, -10, -40)), body={"y": -0.12, "yaw": -6})),
        (22, "inoutsine", FK),
    ], "Fishman Karate: Vagabond Drill, a whirlpool spun up between the hands and thrust out as a drill of water.")
    # Five-Thousand Brick Fist: a hop, the fist raised high, then driven down into the ground
    write(s + "five", 26, [
        (0, "linear", FK),
        (3, "inoutsine", p(FK, rightArm=arm(-40, 0, 14, -90), body={"y": -0.2, "bend": 10}, rightLeg=leg(-10, 4, 60), leftLeg=leg(-30, -4, 60))),
        (7, "outsine", p(FK, rightArm=arm(-172, 0, 14, -46), leftArm=arm(-60, 30, -30, -20), body={"y": 0.14, "pitch": 6},
                         rightLeg=leg(10, 4, 30), leftLeg=leg(-40, -4, 50), head={"pitch": -8})),
        (11, "insine", p(FK, rightArm=arm(-150, 0, 10, -20), leftArm=arm(-40, 30, -30, -20), body={"y": -0.2, "bend": 26},
                         rightLeg=leg(20, 4, 50), leftLeg=leg(-50, -4, 80), head={"pitch": 16})),
        (14, "outexpo", p(FK, rightArm=arm(8, 0, 8, -2), leftArm=arm(-20, 30, -34, -24), body={"y": -0.5, "bend": 40},
                          rightLeg=leg(34, 4, 70), leftLeg=leg(-60, -4, 110), head={"pitch": 22})),
        (17, "outback", p(FK, rightArm=arm(10, 0, 8, 0), leftArm=arm(-20, 30, -34, -24), body={"y": -0.52, "bend": 42},
                          rightLeg=leg(34, 4, 72), leftLeg=leg(-60, -4, 112), head={"pitch": 22})),
        (22, "inoutsine", p(FK, body={"y": -0.16, "bend": 10})),
        (26, "inoutsine", FK),
    ], "Fishman Karate: Five-Thousand Brick Fist, a fist driven into the ground, its shock running out through everything round.")

    s = "style_brawler_"
    # Fist of Love: the fist raised overhead and brought down on their head like a scolding
    write(s + "love", 16, [
        (0, "linear", IF),
        (4, "inoutsine", p(IF, rightArm=arm(-170, 4, 16, -96), body={"y": 0.02, "pitch": 6, "yaw": -6}, head={"pitch": -4})),
        (6, "insine", p(IF, rightArm=arm(-176, 4, 16, -104), body={"y": 0.03, "pitch": 8, "yaw": -6}, head={"pitch": -6})),
        (9, "outexpo", p(IF, rightArm=arm(-50, -4, 6, -8), body={"y": -0.1, "bend": 24, "z": -0.12, "yaw": 6}, head={"pitch": 14},
                         rightLeg=leg(16, 3, 22), leftLeg=leg(-24, -3, 26))),
        (11, "outback", p(IF, rightArm=arm(-46, -4, 6, -6), body={"y": -0.12, "bend": 26, "z": -0.13, "yaw": 6}, head={"pitch": 15},
                          rightLeg=leg(16, 3, 22), leftLeg=leg(-24, -3, 26))),
        (16, "inoutsine", IF),
    ], "Iron Fist: Fist of Love, a fist brought down on their head like a scolding, driving them into the ground.")
    # Fist Meteor: a cannonball pitched overhand: the front knee lifted, the stride, the arm whipped through
    write(s + "meteor", 20, [
        (0, "linear", IF),
        (3, "inoutsine", p(IF, rightArm=arm(26, 0, 18, -40), leftArm=arm(-60, 30, -16, -50), body={"yaw": -40, "y": 0.0},
                           leftLeg=leg(-74, -3, 86), rightLeg=leg(2, 3, 14))),
        (6, "inoutsine", p(IF, rightArm=arm(-160, 14, 24, 40), leftArm=arm(-84, 30, -10, -12), body={"yaw": -34, "y": -0.06},
                           leftLeg=leg(-34, -3, 26), rightLeg=leg(24, 3, 30))),
        (8, "insine", p(IF, rightArm=arm(-176, 10, 20, 48), leftArm=arm(-70, 20, -10, -30), body={"yaw": -20, "y": -0.08, "pitch": 6},
                        leftLeg=leg(-36, -3, 26), rightLeg=leg(26, 3, 30))),
        (10, "outexpo", p(IF, rightArm=arm(-80, -10, 6, -6), leftArm=arm(20, 0, -14, -80), body={"yaw": 28, "y": -0.1, "bend": 16, "z": -0.1},
                          leftLeg=leg(-38, -3, 26), rightLeg=leg(28, 3, 24, yaw=20), head={"yaw": -12})),
        (13, "outback", p(IF, rightArm=arm(-36, -44, 10, -20), leftArm=arm(22, 0, -14, -80), body={"yaw": 36, "y": -0.12, "bend": 20, "z": -0.12},
                          leftLeg=leg(-38, -3, 28), rightLeg=leg(-6, 3, 40, yaw=24), head={"yaw": -14})),
        (20, "inoutsine", IF),
    ], "Iron Fist: Fist Meteor, a cannonball pitched overhand hard enough to hole a ship.")
    # Honesty Impact: the fist pulled far back and held, trembling with all of it, then one straight punch
    cocked = p(IF, rightArm=arm(30, 0, 20, -110), leftArm=arm(-80, 16, -8, -30), body={"yaw": -42, "y": -0.12, "bend": 4},
               rightLeg=leg(22, 3, 30), leftLeg=leg(-28, -3, 32), head={"yaw": 14})
    struck = p(IF, rightArm=arm(-92, -6, 2, 0), leftArm=arm(26, 0, -12, -96), body={"yaw": 30, "y": -0.1, "z": -0.26, "bend": 10},
               rightLeg=leg(24, 3, 16, yaw=24), leftLeg=leg(-34, -3, 26), head={"yaw": -14})
    write(s + "honesty", 26, [(0, "linear", IF), (4, "inoutsine", cocked)]
          + shake(cocked, [6, 7, 8, 9, 10, 11], 1.0, (("body", "roll"), ("rightArm", "roll")))
          + [(12, "insine", p(cocked, body={"yaw": -46})), (14, "outexpo", struck), (16, "outback", p(struck, body={"z": -0.28})),
             (22, "inoutsine", p(IF, body={"yaw": 4})), (26, "inoutsine", IF)],
          "Iron Fist: Honesty Impact, everything in one straight punch, its shock blowing a tunnel through the air.")


def main():
    black_leg()
    six_powers()
    fishman_karate()
    iron_fist()
    extras()
    clash_recoils()
    stances()
    java_poses()
    print(f"wrote {len(WRITTEN)} style animations and their NPC poses")


if __name__ == "__main__":
    main()
