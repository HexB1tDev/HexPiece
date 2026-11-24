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
# 3.5.7: Fishman Karate's clips are gone with it (the owner: "Remove FIshman karate from the game entirely"), Iron Fist's with its
# moves (the owner: "Remove all abilities from Iron Fist, we're making this shit anew")
PUNCHES = ("style_six_powers_finger_r", "style_six_powers_finger_l", "style_six_powers_palm",
           # 3.5.8: Garp's Iron Fist made anew: the plain punches and the Fist of Love leave the legs to the game
           "style_brawler_punch_l", "style_brawler_punch_r", "style_brawler_love")
# 2.4.4: the Brick Fist eases in like a punch but plants its legs (it is stepped into: the owner, "it doesn't look very strong")
EASE_IN = set(PUNCHES)
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


# ------------------------------------------------------------------------------------------------------------- Iron Fist (3.5.8)
# The owner's remake (docs/handoff 8zzs): Garp's boxing. A guard with both fists up; every big blow thrown with the whole body (the
# legs planted and driven, the hips and shoulders turned through it). Beats as ironfist/IronMove has them.
IF = p(arms(right=arm(-62, -22, 12, -112), left=mirror(arm(-70, -18, 12, -104))), legs(12, 6), body={"y": -0.04, "yaw": -12})


def iron_fist():
    s = "style_brawler_"
    # LMB: a left, then a right (they land on tick 2)
    jab_l = p(IF, leftArm=mirror(arm(-90, -6, 4, -2)), rightArm=arm(-58, -26, 12, -116), body={"yaw": -20, "bend": 4})
    write(s + "punch_l", 6, [(1, "insine", p(IF, leftArm=mirror(arm(-60, -14, 12, -90)))), (2, "outexpo", jab_l),
                             (4, "outsine", p(jab_l, leftArm={"pitch": -84})), (6, "inoutsine", IF)],
          "Iron Fist: a plain left punch.")
    jab_r = p(IF, rightArm=arm(-90, -6, 4, -2), leftArm=mirror(arm(-58, -26, 12, -116)), body={"yaw": 22, "bend": 4})
    write(s + "punch_r", 6, [(1, "insine", p(IF, rightArm=arm(-58, -14, 14, -92), body={"yaw": -18})), (2, "outexpo", jab_r),
                             (4, "outsine", p(jab_r, rightArm={"pitch": -84})), (6, "inoutsine", IF)],
          "Iron Fist: a plain right punch.")
    # RMB: Fist of Love: the fist up overhead (by the side, never through the face) and straight down on the head, the body bent into it
    up = p(IF, rightArm=arm(-24, 0, 150, -70), body={"yaw": -6, "bend": -4})
    down = p(IF, rightArm=arm(-62, -12, 8, -8), leftArm=mirror(arm(-40, -10, 12, -70)), body={"yaw": 10, "bend": 18})
    write(s + "love", 8, [(1, "insine", up), (2, "outexpo", down), (4, "outsine", p(down, body={"bend": 20})), (8, "inoutsine", IF)],
          "Iron Fist: Fist of Love, the fist raised overhead and brought straight down on their head.")
    # Z held: Meteor Fist's wind-up, a pitcher's: the left leg forward, the upper body turned back, the cannonball behind the head
    windup = p(rightArm=arm(18, 0, 118, -104), leftArm=mirror(arm(-84, -28, 12, -8)), rightLeg=leg(18, 4, 24), leftLeg=leg(-38, -4, 30),
               body={"yaw": -42, "bend": -6, "y": -0.06}, head={"yaw": 30})
    beats = [(0, "linear", IF), (6, "outsine", windup)]
    sign = 1
    for t in range(10, 81, 6):
        beats.append((t, "inoutsine", p(windup, rightArm={"pitch": 18 + 2 * sign}, body={"yaw": -42 - sign})))
        sign = -sign
    write(s + "meteor_hold", 80, beats, "Iron Fist: Meteor Fist held, a pitcher's wind-up, the iron cannonball behind the head.")
    thrown = p(rightArm=arm(-72, -14, 10, -4), leftArm=mirror(arm(22, 0, 20, -40)), rightLeg=leg(46, 6, 30), leftLeg=leg(-30, -4, 40),
               body={"yaw": 40, "bend": 24, "y": -0.12, "z": -0.18}, head={"yaw": -16})
    write(s + "meteor_throw", 12, [(0, "constant", windup), (2, "insine", p(windup, rightArm=arm(-30, 0, 140, -60), body={"yaw": -10})),
                                   (3, "outexpo", thrown), (6, "outsine", p(thrown, rightArm={"pitch": -56}, body={"bend": 28})),
                                   (12, "inoutsine", IF)],
          "Iron Fist: Meteor Fist, the cannonball hurled like a fastball, the whole body following through.")
    # X: Mountain Crusher: low and wide, the fist clenched at the waist; one huge step, the hips and shoulders turned into an uppercut
    coil = p(legs(70, 18), rightArm=arm(24, 0, 16, -104), leftArm=mirror(arm(-70, -20, 10, -60)), body={"y": -0.42, "yaw": -30, "bend": 14})
    upper = p(rightArm=arm(-108, -4, 42, -58), leftArm=mirror(arm(16, 0, 18, -50)), rightLeg=leg(24, 6, 20), leftLeg=leg(-46, -6, 36),
              body={"y": -0.08, "yaw": 34, "bend": -6, "z": -0.32})
    beats = [(0, "linear", IF), (4, "outsine", coil)]
    for t, sgn in ((6, 1), (8, -1), (10, 1)):
        beats.append((t, "inoutsine", p(coil, rightArm={"pitch": 24 + 2 * sgn}, body={"yaw": -30 - sgn})))
    beats += [(12, "insine", p(coil, rightArm=arm(-10, 0, 40, -100), body={"z": -0.2, "y": -0.36}, leftLeg=leg(-40, -6, 60))),
              (13, "outexpo", upper),
              (17, "outsine", p(upper, rightArm={"pitch": -112}, body={"bend": -10})), (24, "inoutsine", IF)]
    write(s + "crusher", 24, beats, "Iron Fist: Mountain Crusher, a devastating uppercut stepped into from low and wide.")
    # C: Blue Hole (3.5.9, the owner: "Uppercut / Did it hit someone? No? Ends here. If Yes: the entity goes flying then the user catches
    # up (jumps to the entity) grabs them and throws them down to the floor, hard"): a quick low coil, the uppercut on tick 6 (IronMove
    # HOLE_HIT), the leap after them from tick 8 (legs tucked, the right arm reaching up), the face grabbed at the top on 14, wound back
    # over the head by the side (never through the face) by 17, thrown straight down on 18 with the whole body bent into it, the fall,
    # landing low on 29 at the hole's edge, then leaning over it looking down. A miss clears the clip right after the uppercut.
    crouch = p(legs(54, 14), rightArm=arm(22, 0, 16, -102), leftArm=mirror(arm(-70, -20, 10, -60)), body={"y": -0.32, "yaw": -26, "bend": 12})
    jump = p(rightArm=arm(-160, 16, 0, -18), leftArm=mirror(arm(-28, 0, 44, -34)), rightLeg=leg(-74, 6, 104), leftLeg=leg(-34, -6, 78),
             body={"yaw": 8, "bend": -6})
    grab = p(jump, rightArm=arm(-146, 14, 0, -36), leftArm=mirror(arm(-46, -10, 40, -20)), rightLeg=leg(-60, 6, 90), leftLeg=leg(-10, -6, 50))
    wound = p(grab, rightArm=arm(-22, 0, 150, -72), leftArm=mirror(arm(-70, -22, 18, -8)), body={"yaw": -16, "bend": -16})
    throw = p(rightArm=arm(-34, -12, 10, -6), leftArm=mirror(arm(32, 0, 32, -30)), rightLeg=leg(-46, 6, 64), leftLeg=leg(24, -6, 34),
              body={"yaw": 22, "bend": 40})
    drop = p(rightArm=arm(-40, -4, 36, -24), leftArm=mirror(arm(-34, -4, 40, -28)), rightLeg=leg(-12, 6, 30), leftLeg=leg(6, -6, 24),
             body={"bend": 10})
    landed = p(legs(66, 14), rightArm=arm(8, 0, 30, -34), leftArm=mirror(arm(-44, -10, 30, -46)), body={"y": -0.42, "yaw": 6, "bend": 28})
    look = p(IF, legs(22, 10), body={"bend": 24, "y": -0.08}, head={"pitch": 30})
    write(s + "blue_hole", 35, [(0, "linear", IF), (3, "outsine", crouch),
                                (5, "insine", p(crouch, rightArm=arm(-12, 0, 40, -100), body={"z": -0.16, "y": -0.3}, leftLeg=leg(-40, -6, 58))),
                                (6, "outexpo", upper), (9, "outsine", p(upper, rightArm={"pitch": -124, "roll": 8}, body={"bend": -12, "y": 0.0})),
                                (11, "outsine", jump), (13, "inoutsine", p(jump, rightArm={"pitch": -158})), (14, "outback", grab),
                                (17, "inoutsine", wound), (18, "outexpo", throw), (21, "outsine", p(throw, body={"bend": 44})),
                                (26, "inoutsine", drop), (29, "outexpo", landed), (31, "outsine", p(landed, body={"bend": 30, "y": -0.44})),
                                (34, "inoutsine", look), (35, "linear", look)],
          "Iron Fist: Blue Hole, an uppercut; if it lands, a leap after them, a grab in the air and a throw straight down into the ground.")
    # V: Galaxy Divide: knees bent, launched up at them, the fist pulled back behind the shoulder in the air; the punch into the air
    low = p(IF, legs(80, 12), body={"y": -0.5, "bend": 22})
    air = p(rightArm=arm(40, 0, 64, -112), leftArm=mirror(arm(-88, -22, 10, -12)), rightLeg=leg(-62, 6, 86), leftLeg=leg(-24, -6, 96),
            body={"yaw": -36, "bend": -6})
    punch = p(rightArm=arm(-96, -8, 4, -2), leftArm=mirror(arm(30, 0, 22, -40)), rightLeg=leg(-30, 6, 50), leftLeg=leg(10, -6, 40),
              body={"yaw": 36, "bend": 10, "z": -0.2})
    beats = [(0, "linear", IF), (5, "outsine", low), (8, "outexpo", air)]
    for t, sgn in ((11, 1), (14, -1), (17, 1)):
        beats.append((t, "inoutsine", p(air, rightArm={"pitch": 40 + 2 * sgn}, body={"yaw": -36 - 1.5 * sgn})))
    beats += [(19, "insine", p(air, body={"yaw": -40})), (20, "outexpo", punch), (24, "outsine", p(punch, rightArm={"pitch": -92})),
              (30, "inoutsine", IF)]
    write(s + "divide", 30, beats, "Iron Fist: Galaxy Divide, a leap with the fist loaded behind the shoulder, then a punch into the air.")
    # B: Infinitum Explosion: a wide stance, both fists up; left and right faster and faster on the barrage's own beats; the right
    # pulled all the way back and one devastating punch to finish
    wide = p(legs(40, 16), arms(right=arm(-70, -20, 14, -100), left=mirror(arm(-70, -20, 14, -100))), body={"y": -0.24, "yaw": 0, "bend": 8})
    beats = [(0, "linear", IF), (8, "outsine", wide)]
    tick = 10
    k = 0
    while tick < 70:
        right = k % 2 == 0
        hit = p(wide, rightArm=arm(-92, -6, 4, -2) if right else arm(-60, -22, 14, -100),
                leftArm=mirror(arm(-92, -6, 4, -2)) if not right else mirror(arm(-60, -22, 14, -100)),
                body={"yaw": 18 if right else -18, "bend": 10})
        beats.append((tick, "outexpo", hit))
        k += 1
        nxt = max(1.0, 4.0 - (k - 1) * 0.12)
        tick = int(round(10 + sum(max(1.0, 4.0 - i * 0.12) for i in range(k))))
    cocked = p(wide, rightArm=arm(40, 0, 30, -112), leftArm=mirror(arm(-84, -24, 10, -10)), body={"yaw": -46, "bend": 2, "y": -0.3})
    finish = p(rightArm=arm(-94, -6, 4, 0), leftArm=mirror(arm(30, 0, 24, -40)), rightLeg=leg(30, 6, 24), leftLeg=leg(-44, -6, 40),
               body={"yaw": 42, "bend": 14, "z": -0.34, "y": -0.2})
    beats += [(72, "inoutsine", wide), (78, "outsine", cocked), (79, "inoutsine", p(cocked, body={"yaw": -48})), (80, "outexpo", finish),
              (86, "outsine", p(finish, rightArm={"pitch": -90})), (94, "inoutsine", IF)]
    write(s + "infinitum", 94, beats, "Iron Fist: Infinitum Explosion, a barrage too fast to follow, ended by one devastating punch.")
    # N: Galaxy Impact: crouched deep, the fist drawn to the waist; the leap; in the sky the pose: diagonal to the ground, the chest
    # pushed out, the upper body wrenched back, the fist cocked beside and behind the head, the left arm out and down, a knee up and the
    # other leg back; then everything turned into one punch at the world below, held, and down to land
    gather = p(legs(96, 16), rightArm=arm(28, 0, 14, -104), leftArm=mirror(arm(-36, 0, 44, -10)), body={"y": -0.56, "bend": 30})
    leap = p(legs(4, 6), rightArm=arm(30, 0, 30, -60), leftArm=mirror(arm(20, 0, 30, -20)), body={"bend": -6})
    pose = p(rightArm=arm(-20, 0, 132, -128), leftArm=mirror(arm(-58, -26, 16, -6)), rightLeg=leg(-92, 6, 104), leftLeg=leg(44, -4, 22),
             body={"pitch": 38, "yaw": -44, "bend": -14}, head={"pitch": 10, "yaw": 34})
    blow = p(rightArm=arm(-74, -10, 6, 0), leftArm=mirror(arm(40, 0, 34, -30)), rightLeg=leg(-36, 6, 14), leftLeg=leg(34, -4, 6),
             body={"pitch": 52, "yaw": 34, "bend": 14}, head={"pitch": 24})
    beats = [(0, "linear", IF), (12, "inoutsine", gather), (28, "inoutsine", p(gather, body={"y": -0.6, "bend": 32})), (30, "outexpo", leap),
             (46, "inoutsine", p(leap, legs(30, 8))), (58, "inoutsine", pose)]
    sign = 1
    for t in range(62, 101, 4):
        beats.append((t, "inoutsine", p(pose, rightArm={"pitch": -20 + 1.5 * sign}, body={"yaw": -44 - 1.5 * sign, "pitch": 38 + sign})))
        sign = -sign
    beats += [(103, "insine", p(pose, body={"yaw": -48, "pitch": 36})), (104, "outexpo", blow), (108, "outsine", p(blow, body={"pitch": 50})),
              (150, "inoutsine", p(blow, body={"pitch": 44})),
              (172, "inoutsine", p(IF, legs(30, 10), rightArm=arm(-30, -10, 10, -20), body={"bend": 4})),
              (176, "outsine", p(IF, legs(60, 12), body={"y": -0.3, "bend": 14})), (180, "inoutsine", IF)]
    write(s + "impact", 180, beats, "Iron Fist: Galaxy Impact, a leap into the sky, the pose, and one punch at the world below.")


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


def java_poses(names=None, cls="StylePoses", tool="tools/style_anims.py", what="every fighting-style animation's keyframes, for NPCs"):
    """The same animations for NPCs (PlayerAnimator only animates players): client/npc/StylePoses.java, one packed array each. 2.8.8:
    also the Light fruit's (tools/pika_anims.py: client/npc/PikaPoses.java, for its clones of light)."""
    out = ["package com.hexhaki.piece.client.npc;", "", "/**",
           f" * Generated by {tool} (do not edit): {what}. Each array packs, per",
           " * animated channel: part (" + ", ".join(POSE_PARTS) + "), channel (" + ", ".join(POSE_CHANNELS) + "), the keyframe count, then",
           " * tick, value (degrees, or blocks for body x/y/z), easing (" + ", ".join(EASINGS) + ") for each keyframe.",
           " */", f"final class {cls} {{", f"    private {cls}() {{", "    }", ""]
    names = sorted(WRITTEN if names is None else names)
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
    path = os.path.join(ROOT, f"src/main/java/com/hexhaki/piece/client/npc/{cls}.java")
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


def main():
    black_leg()
    six_powers()
    iron_fist()
    extras()
    clash_recoils()
    stances()
    java_poses()
    print(f"wrote {len(WRITTEN)} style animations and their NPC poses")


if __name__ == "__main__":
    main()
