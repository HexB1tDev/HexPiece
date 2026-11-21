#!/usr/bin/env python3
"""2.7.8: the Pika Pika no Mi rebuilt after Kizaru (the owner: "better the Lightfruit in every way, make it cooler, more dramatic,
better animations ... copy his moves and how he uses them exactly").

Writes src/main/resources/assets/hexhaki/player_animation/pika_<move>.json in tools/style_anims.py's conventions. Borsalino's way of
fighting, as the anime shows it: lazy and unhurried, hands in his trouser pockets even while he kicks, one finger pointed for a laser,
both hands brought together for Yata no Kagami, arms crossed up in the air for Yasakani no Magatama, the sword of light drawn out of
his joined hands. Every blow lands on the tick server/PikaMoves lands it (kicks and cuts on tick 2, the barrage on 1 to 4 and 6, the
laser on 2, Yata's light on 4, the light kick on 1, the clones on 4).

Usage: python3 tools/pika_anims.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from anim_hand import arms, legs, spine  # noqa: E402
from style_anims import OUT, PARTS, WRITTEN, arm, java_poses, leg, mirror, p, write  # noqa: E402

# his hands in his trouser pockets: the upper arms a little back and out, the elbows bent, the hands at the hips
POCKET_R = arm(10, 0, 9, -34)
POCKET_L = mirror(POCKET_R)
POCKETS = arms(right=POCKET_R, left=POCKET_L)
REST = p(arms(right=arm(0, 0, 4, -4), left=arm(0, 0, -4, -4)), legs(0, 0))
SLOUCH = {"bend": 5}
LAZY = p(POCKETS, legs(0, 0), body=SLOUCH, head={"pitch": -4})


def write_loop(name, duration, beats, description, parts=None):
    """A loop (first and last beats the same pose) of the given parts only (None: every part)."""
    assert beats[0][2] == beats[-1][2] and beats[-1][0] == duration, name + ": a loop ends where it starts"
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part, channels in PARTS.items():
            if parts is not None and part not in parts:
                continue
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.7.8", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration, "isLoop": True, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


# ------------------------------------------------------------------------------------------------------ stances

def stances():
    idle = LAZY
    breathe = p(LAZY, body={"bend": 6, "y": -0.01}, head={"pitch": -2})
    write_loop("pika_idle", 60, [(0, "inoutsine", idle), (30, "inoutsine", breathe), (60, "inoutsine", idle)],
               "Pika Pika: Borsalino standing, slouched, hands in his pockets.")
    write_loop("pika_walk", 20, [(0, "linear", POCKETS), (10, "linear", p(POCKETS, rightArm={"pitch": 12}, leftArm={"pitch": 8})),
                                 (20, "linear", POCKETS)],
               "Pika Pika: walking with his hands in his pockets (the legs the game's own).", parts=("rightArm", "leftArm"))
    hang = p(POCKETS, rightLeg=leg(-8, 3, 14), leftLeg=leg(4, -3, 22), body={"bend": 4})
    write_loop("pika_float", 40, [(0, "inoutsine", hang), (20, "inoutsine", p(hang, body={"y": 0.06}, rightLeg={"bend": 18})),
                                  (40, "inoutsine", hang)],
               "Pika Pika: floating as light, hands in his pockets, the legs hanging easy.")
    fly = p(POCKETS, rightLeg=leg(14, 2, 6), leftLeg=leg(22, -2, 14), body={"pitch": 46}, head={"pitch": -38})
    write_loop("pika_fly", 20, [(0, "inoutsine", fly), (10, "inoutsine", p(fly, leftLeg={"pitch": 18}, rightLeg={"pitch": 18})),
                                (20, "inoutsine", fly)],
               "Pika Pika: flying as light, laid along the way he goes, hands in his pockets, the legs trailing.")
    hold = arms(right=arm(-28, 0, 8, -30), left=POCKET_L)
    write_loop("pika_sword_idle", 40, [(0, "inoutsine", hold), (20, "inoutsine", p(hold, rightArm={"pitch": -31})),
                                       (40, "inoutsine", hold)],
               "Pika Pika: Ama no Murakumo held low and ahead in the right hand, the left in its pocket.", parts=("rightArm", "leftArm"))
    write_loop("pika_sword_walk", 20, [(0, "linear", hold), (10, "linear", p(hold, rightArm={"pitch": -24})), (20, "linear", hold)],
               "Pika Pika: walking with Ama no Murakumo out.", parts=("rightArm", "leftArm"))


# ------------------------------------------------------------------------------------------------------ the kicks (LMB)

PLANT = dict(leftLeg=leg(-4, -4, 14))


def kicks():
    # 1: a front kick straight out, his hands never leaving his pockets
    write("pika_kick_1", 8, [
        (0, "linear", LAZY),
        (1, "insine", p(LAZY, rightLeg=leg(-62, 4, 92), body={"bend": -4, "z": 0.02}, **PLANT)),
        (2, "outexpo", p(LAZY, rightLeg=leg(-96, 2, 0), body={"bend": -12, "z": -0.06}, head={"pitch": 4}, **PLANT)),
        (3, "outback", p(LAZY, rightLeg=leg(-88, 2, 6), body={"bend": -10, "z": -0.05}, **PLANT)),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a front kick at the speed of light, hands in his pockets.")
    # 2: a roundhouse from the left leg, the body turning into it
    write("pika_kick_2", 8, [
        (0, "linear", LAZY),
        (1, "insine", p(LAZY, leftLeg=leg(-30, -46, 80), rightLeg=leg(4, 4, 16), body={"yaw": 30, "roll": 8})),
        (2, "outexpo", p(LAZY, leftLeg=leg(-62, -82, 4), rightLeg=leg(6, 6, 20), body={"yaw": -48, "roll": 20, "bend": -6}, head={"yaw": 30})),
        (3, "outback", p(LAZY, leftLeg=leg(-58, -76, 10), rightLeg=leg(6, 6, 20), body={"yaw": -54, "roll": 18, "bend": -5}, head={"yaw": 34})),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a roundhouse at the speed of light, the left leg swept across.")
    # 3: a spinning back kick: turned round, the right leg driven back into them
    write("pika_kick_3", 8, [
        (0, "linear", LAZY),
        (1, "insine", p(LAZY, rightLeg=leg(-20, 6, 70), body={"yaw": -110, "bend": 6}, head={"yaw": 70}, **PLANT)),
        (2, "outexpo", p(LAZY, rightLeg=leg(82, 4, 4), body={"yaw": -180, "bend": 22, "z": -0.05}, head={"yaw": 150, "pitch": -10}, **PLANT)),
        (4, "outback", p(LAZY, rightLeg=leg(74, 4, 10), body={"yaw": -182, "bend": 20, "z": -0.05}, head={"yaw": 150, "pitch": -8}, **PLANT)),
        (5, "constant", p(LAZY, rightLeg=leg(30, 4, 30), body={"yaw": -180, "bend": 10}, head={"yaw": 150})),
        (6, "constant", p(LAZY, rightLeg=leg(30, 4, 30), body={"yaw": 0, "bend": 10}, head={"yaw": -30})),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a spinning back kick at the speed of light.")
    # 4: a rising kick straight up that throws them into the air
    write("pika_kick_4", 8, [
        (0, "linear", LAZY),
        (1, "insine", p(LAZY, rightLeg=leg(-30, 4, 60), body={"bend": 8, "y": -0.06}, **PLANT)),
        (2, "outexpo", p(LAZY, rightLeg=leg(-160, 2, 0), body={"bend": -22, "y": 0.04}, head={"pitch": -14}, **PLANT)),
        (3, "outback", p(LAZY, rightLeg=leg(-150, 2, 4), body={"bend": -20, "y": 0.03}, head={"pitch": -12}, **PLANT)),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a rising kick at the speed of light that throws them up.")
    # 5: the barrage: four kicks too fast to see (alternating legs), then the last that throws them (ticks 1, 2, 3, 4, then 6)
    beats = [(0, "linear", LAZY)]
    for i, t in enumerate((1, 2, 3, 4)):
        right = i % 2 == 0
        out = leg(-92 + 8 * (i % 2), 2, 4)
        back = leg(-4, 4, 14)
        beats.append((t, "outexpo", p(LAZY, rightLeg=out if right else back, leftLeg=back if right else mirror(out),
                                      body={"bend": -8, "z": -0.04, "yaw": 10 if right else -10})))
    beats += [(5, "insine", p(LAZY, rightLeg=leg(-40, 4, 90), body={"bend": 4}, **PLANT)),
              (6, "outexpo", p(LAZY, rightLeg=leg(-100, 2, 0), body={"bend": -16, "z": -0.1}, head={"pitch": 6}, **PLANT)),
              (7, "outback", p(LAZY, rightLeg=leg(-94, 2, 6), body={"bend": -14, "z": -0.09}, **PLANT)),
              (12, "inoutsine", LAZY)]
    write("pika_kick_5", 12, beats, "Pika Pika: a barrage of kicks too fast to see, and the last that throws them.")
    # sprinting: arrived as light, a flying kick
    write("pika_dash_kick", 10, [
        (0, "constant", p(LAZY, rightLeg=leg(-60, 2, 80), leftLeg=leg(30, -2, 60), body={"bend": -10, "y": 0.2})),
        (2, "outexpo", p(LAZY, rightLeg=leg(-98, 2, 0), leftLeg=leg(24, -2, 70), body={"bend": -16, "y": 0.18, "z": -0.1})),
        (4, "outback", p(LAZY, rightLeg=leg(-92, 2, 6), leftLeg=leg(18, -2, 60), body={"bend": -14, "y": 0.1, "z": -0.08})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: across the distance as light and a flying kick on arrival.")
    # in the air, looking down: fallen on them as light, the heel driven into the ground
    write("pika_dive", 10, [
        (0, "constant", p(LAZY, rightLeg=leg(-120, 2, 10), leftLeg=leg(10, -2, 40), body={"bend": -10})),
        (1, "outexpo", p(LAZY, rightLeg=leg(-6, 4, 30), leftLeg=leg(14, -4, 70), body={"bend": 20, "y": -0.32}, head={"pitch": 16})),
        (4, "outback", p(LAZY, rightLeg=leg(-10, 4, 34), leftLeg=leg(14, -4, 74), body={"bend": 22, "y": -0.34}, head={"pitch": 18})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: fallen on them as light, the heel driven into the ground.")


# ------------------------------------------------------------------------------------------------------ the laser and the beam (RMB)

POINT = arm(-90, 4, 4, 0)
LEFT_POCKET = dict(leftArm=POCKET_L)


def lasers():
    aim = p(LAZY, rightArm=POINT, body={"yaw": 12, "bend": 2}, head={"yaw": -6}, **LEFT_POCKET)
    write("pika_laser", 8, [
        (0, "linear", LAZY),
        (2, "outexpo", aim),
        (3, "outback", p(aim, rightArm={"pitch": -98})),
        (5, "inoutsine", aim),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a finger pointed and a laser out of its tip, the other hand left in its pocket.")
    gather = p(aim, rightArm={"pitch": -88, "bend": -4}, body={"yaw": 14, "bend": 0}, rightLeg=leg(6, 4, 10), leftLeg=leg(-10, -4, 14))
    beats = [(0, "linear", LAZY), (4, "outexpo", gather)]
    for t in range(8, 121, 4):
        beats.append((t, "inoutsine", p(gather, rightArm={"pitch": -88 + (1.2 if t % 8 else -1.2)})))
    write("pika_laser_charge", 120, beats, "Pika Pika: the light gathered at the fingertip, the arm held out, trembling with it.")
    write("pika_laser_fire", 10, [
        (0, "constant", gather),
        (1, "outexpo", p(gather, rightArm={"pitch": -112, "bend": -10}, body={"yaw": 8, "bend": -10, "z": 0.06}, head={"pitch": -6})),
        (3, "outback", p(gather, rightArm={"pitch": -104, "bend": -6}, body={"yaw": 10, "bend": -6, "z": 0.04})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: the beam let go, the arm thrown up by it.")
    write("pika_laser_end", 6, [(0, "constant", aim), (6, "inoutsine", LAZY)], "Pika Pika: the pointed hand back into its pocket.")


# ------------------------------------------------------------------------------------------------------ Yata no Kagami, the stream

JOIN_R = arm(-62, -34, -14, -64)
JOINED = arms(right=JOIN_R, left=mirror(JOIN_R))


def yata():
    joined = p(JOINED, legs(0, 0), body={"bend": 6}, head={"pitch": 6})
    write("pika_yata", 10, [
        (0, "linear", LAZY),
        (2, "outexpo", joined),
        (3, "inoutsine", p(joined, rightArm={"pitch": -66}, leftArm={"pitch": -66})),
        (4, "constant", p(joined, rightArm={"pitch": -70}, leftArm={"pitch": -70})),
        (5, "constant", p(LAZY, rightArm=arm(-30, 0, 20, -20), leftArm=arm(-30, 0, -20, -20), body={"bend": 2})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: Yata no Kagami, light formed between the joined hands, and he is gone along it.")
    streak = p(POCKETS, rightLeg=leg(10, 2, 4), leftLeg=leg(16, -2, 8), body={"pitch": 70}, head={"pitch": -56})
    beats = [(0, "linear", LAZY), (3, "outexpo", streak)]
    for t in range(6, 61, 6):
        beats.append((t, "inoutsine", p(streak, body={"roll": 4 if t % 12 else -4})))
    write("pika_stream", 60, beats, "Pika Pika: streaming as light, laid out along the way he goes.")
    write("pika_stream_end", 6, [(0, "constant", streak), (6, "inoutsine", LAZY)], "Pika Pika: out of the stream of light, upright again.")


# ------------------------------------------------------------------------------------------------------ Yasakani no Magatama

CROSS_R = arm(-100, -30, -4, -18)
CROSSED = arms(right=CROSS_R, left=mirror(CROSS_R))


def yasakani():
    rising = p(CROSSED, rightLeg=leg(-30, 4, 60), leftLeg=leg(-10, -4, 40), body={"bend": 10}, head={"pitch": 10})
    crossed = p(CROSSED, rightLeg=leg(-12, 4, 30), leftLeg=leg(6, -4, 44), body={"bend": 8}, head={"pitch": 14})
    beats = [(0, "linear", LAZY), (3, "outexpo", rising), (6, "outback", crossed)]
    for t in range(10, 81, 4):
        jolt = 3 if t % 8 else -3
        beats.append((t, "inoutsine", p(crossed, rightArm={"pitch": -100 + jolt}, leftArm={"pitch": -100 - jolt})))
    write("pika_yasakani", 80, beats,
          "Pika Pika: Yasakani no Magatama, up in the air, arms crossed before the face, a torrent of light out of both hands.")
    open_ = p(arms(right=arm(-60, 0, 70, -10), left=arm(-60, 0, -70, -10)), rightLeg=leg(-8, 4, 20), leftLeg=leg(6, -4, 30),
              body={"bend": -4}, head={"pitch": 4})
    write("pika_yasakani_end", 12, [(0, "constant", crossed), (3, "outexpo", open_), (12, "inoutsine", LAZY)],
          "Pika Pika: the torrent spent, the arms thrown open, coming down.")


# ------------------------------------------------------------------------------------------------------ Ama no Murakumo

HOLD_R = arm(-28, 0, 8, -30)


def murakumo():
    joined = p(JOINED, legs(0, 0), body={"bend": 4})
    drawn = p(LAZY, rightArm=arm(-70, 40, 50, -6), leftArm=arm(-40, -10, -30, -40), body={"yaw": 20}, head={"yaw": -10})
    write("pika_murakumo", 10, [
        (0, "linear", LAZY),
        (3, "outexpo", joined),
        (5, "outexpo", drawn),
        (6, "outback", p(drawn, rightArm={"yaw": 46})),
        (10, "inoutsine", p(LAZY, rightArm=HOLD_R)),
    ], "Pika Pika: Ama no Murakumo, the joined hands drawn apart and a sword of solid light between them.")
    ready = p(LAZY, rightArm=HOLD_R)
    cuts = [
        # (wind-up arm, cut-through arm, body yaw from, to)
        (arm(-100, 70, 60, -20), arm(-80, -70, -20, -6), 34, -40),
        (arm(-90, -70, -20, -10), arm(-90, 70, 60, -6), -40, 36),
        (arm(-64, 84, 84, -10), arm(-72, -64, -12, -6), 26, -30),
        (arm(-10, -20, 10, -10), arm(-160, 10, 10, -4), -10, 10),
        (arm(-180, 0, 10, -60), arm(-30, 0, 0, 0), 0, 0),
    ]
    for i, (up, through, y0, y1) in enumerate(cuts, start=1):
        lunge = -0.18 if i == 5 else -0.06
        write("pika_sword_" + str(i), 8 if i < 5 else 10, [
            (0, "linear", ready),
            (1, "insine", p(LAZY, rightArm=up, body={"yaw": y0, "bend": -6 if i == 5 else 0}, rightLeg=leg(4, 4, 12), leftLeg=leg(-10, -4, 16))),
            (2, "outexpo", p(LAZY, rightArm=through, body={"yaw": y1, "bend": 14 if i == 5 else 4, "z": lunge}, rightLeg=leg(10, 4, 18),
                             leftLeg=leg(-24, -4, 22))),
            (3, "outback", p(LAZY, rightArm=p({"a": through}, a={"bend": -12})["a"], body={"yaw": y1 * 1.08, "bend": 12 if i == 5 else 3,
                                                                                         "z": lunge}, rightLeg=leg(10, 4, 18),
                             leftLeg=leg(-24, -4, 22))),
            (8 if i < 5 else 10, "inoutsine", ready),
        ], "Pika Pika: Ama no Murakumo, cut " + str(i) + (" (the wave of light)" if i == 5 else "") + ".")
    guard = p(LAZY, rightArm=arm(-80, -30, -10, -50), leftArm=arm(-60, -20, -10, -70), body={"bend": 4})
    beats = [(0, "linear", ready), (2, "outexpo", guard)]
    for t in range(6, 101, 6):
        beats.append((t, "inoutsine", p(guard, rightArm={"pitch": -80 + (1.5 if t % 12 else -1.5)})))
    write("pika_sword_guard", 100, beats, "Pika Pika: Ama no Murakumo held across before him, guarding.")
    write("pika_sword_away", 6, [(0, "constant", ready), (6, "inoutsine", LAZY)], "Pika Pika: the sword of light let go, the hand back in its pocket.")


# ------------------------------------------------------------------------------------------------------ the speed-of-light kick, the clones

def light_kick():
    gather = p(LAZY, rightLeg=leg(-44, 6, 96), leftLeg=leg(-4, -4, 26), body={"bend": -8, "y": -0.08}, head={"pitch": 6})
    beats = [(0, "linear", LAZY), (6, "outexpo", gather)]
    for t in range(10, 81, 4):
        beats.append((t, "inoutsine", p(gather, rightLeg={"pitch": -44 + (2 if t % 8 else -2)})))
    write("pika_light_kick_charge", 80, beats,
          "Pika Pika: the light gathered in the leg, the knee drawn up, hands in his pockets. \"Have you ever been kicked at the speed of light?\"")
    write("pika_light_kick", 14, [
        (0, "constant", p(gather, rightLeg=leg(-70, 6, 80))),
        (1, "outexpo", p(LAZY, rightLeg=leg(-112, 2, 0), leftLeg=leg(4, -4, 22), body={"bend": -20, "z": -0.12}, head={"pitch": 10})),
        (4, "outback", p(LAZY, rightLeg=leg(-106, 2, 4), leftLeg=leg(4, -4, 22), body={"bend": -18, "z": -0.1}, head={"pitch": 8})),
        (14, "inoutsine", LAZY),
    ], "Pika Pika: the kick at the speed of light, landed.")


def clones():
    spread = p(arms(right=arm(-80, 0, 90, -4), left=arm(-80, 0, -90, -4)), rightLeg=leg(-6, 6, 10), leftLeg=leg(4, -6, 14),
               body={"bend": -12}, head={"pitch": -14})
    write("pika_clones", 12, [
        (0, "linear", LAZY),
        (2, "insine", p(JOINED, legs(0, 0), body={"bend": 8})),
        (4, "outexpo", spread),
        (6, "outback", p(spread, rightArm={"roll": 96}, leftArm={"roll": -96})),
        (12, "inoutsine", LAZY),
    ], "Pika Pika: the light thrown out of the body, every bead of it a Borsalino of light.")


def main():
    stances()
    kicks()
    lasers()
    yata()
    yasakani()
    murakumo()
    light_kick()
    clones()
    # 2.8.8: the same keyframes for the clones of light (client/npc/PikaPoses, sampled by StylePoser)
    java_poses(sorted(n for n in WRITTEN if n.startswith("pika_")), "PikaPoses", "tools/pika_anims.py",
               "the Light fruit's animations' keyframes, for its clones of light")


if __name__ == "__main__":
    main()
