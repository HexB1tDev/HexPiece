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
# 2.9.7: the arms held out from the sides, a little forward, the hands open (Kizaru in the air at Egghead)
OPEN_R = arm(-14, 0, 36, -8)
OPEN_ARMS = arms(right=OPEN_R, left=mirror(OPEN_R))
SLOUCH = {"bend": 5}
LAZY = p(POCKETS, legs(0, 0), body=SLOUCH, head={"pitch": -4})


def write_loop(name, duration, beats, description, parts=None):
    """A loop (first and last beats the same pose) of the given parts only (None: every part)."""
    assert beats[0][2] == beats[-1][2] and beats[-1][0] == duration, name + ": a loop ends where it starts"
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part, channels in PARTS.items():
            if parts is not None and part not in parts or part == "head":
                continue
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.9.0", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration, "isLoop": True, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


# 2.9.0 (the owner: "The M1s are locking your head into place ... why is it like controlling your fucking arms if you're not using them?
# Why is it like not just using the legs? ... Do you not learn from every single fucking problem we've had thus far?"): PlayerAnimator
# freezes every part a file keys, so every Light animation keys only the parts its move uses, and never the head (the look is always
# the player's own). Kicks: the legs and the body; the laser: the right arm; Yata: the arms; the stances: the arms (and the legs and the
# lean in flight).
KICK = ("body", "torso", "rightLeg", "leftLeg")
RIGHT = ("body", "torso", "rightArm")
ARMS = ("body", "torso", "rightArm", "leftArm")
ALL_BUT_HEAD = ("body", "torso", "rightArm", "leftArm", "rightLeg", "leftLeg")


def write_parts(name, duration, beats, description, parts, stop_extra=3):
    """As style_anims.write, keying only {parts} (never the head)."""
    assert "head" not in parts, name + ": the head is the player's own"
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part in parts:
            values = q.get(part, {})
            for channel in PARTS[part]:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.9.0", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + stop_extra, "isLoop": False, "returnTick": 0,
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
               "Pika Pika: Borsalino standing, slouched, hands in his pockets.", parts=ARMS)
    write_loop("pika_walk", 20, [(0, "linear", POCKETS), (10, "linear", p(POCKETS, rightArm={"pitch": 12}, leftArm={"pitch": 8})),
                                 (20, "linear", POCKETS)],
               "Pika Pika: walking with his hands in his pockets (the legs the game's own).", parts=("rightArm", "leftArm"))
    # 2.9.0 (the owner: "Flying and jumping gives you a fuck ass animation"): only real flight has a pose (a jump is the game's own, the
    # hands in the pockets): upright as Kizaru hangs in the air, the legs easy, one knee a little up; going fast, leaning a little into
    # the way he goes (never laid flat: the look stays the player's), the legs together trailing
    # 2.9.7 (the owner: "adapt the other abilities to your reference gifs"): as Kizaru hangs and flies over Egghead, the arms held out
    # from his sides and a little forward, the hands open, the legs together and straight under him
    hang = p(OPEN_ARMS, rightLeg=leg(-4, 2, 8), leftLeg=leg(4, -2, 4), body={"bend": 3})
    write_loop("pika_float", 40, [(0, "inoutsine", hang), (20, "inoutsine", p(hang, body={"y": 0.05}, rightLeg={"pitch": -7, "bend": 12},
                                                                               rightArm={"roll": 40}, leftArm={"roll": -40})),
                                  (40, "inoutsine", hang)],
               "Pika Pika: hanging in the air as light, upright, the arms held out from his sides, the legs together.", parts=ALL_BUT_HEAD)
    fly = p(OPEN_ARMS, rightArm={"pitch": 8, "roll": 30}, leftArm={"pitch": 8, "roll": -30}, rightLeg=leg(10, 1, 4), leftLeg=leg(14, -1, 6),
            body={"pitch": -16, "bend": -4})
    write_loop("pika_fly", 30, [(0, "inoutsine", fly), (15, "inoutsine", p(fly, leftLeg={"pitch": 13, "bend": 8}, rightLeg={"pitch": 13},
                                                                             body={"pitch": -18})),
                                (30, "inoutsine", fly)],
               "Pika Pika: flying as light, leaning a little into the way he goes, the arms out from his sides, the legs together.",
               parts=ALL_BUT_HEAD)


# ------------------------------------------------------------------------------------------------------ the laser and the beam (RMB)

POINT = arm(-90, 4, 4, 0)
LEFT_POCKET = dict(leftArm=POCKET_L)


def lasers():
    aim = p(LAZY, rightArm=POINT, body={"yaw": 12, "bend": 2}, head={"yaw": -6}, **LEFT_POCKET)
    write_parts("pika_laser", 8, [
        (0, "linear", LAZY),
        (2, "outexpo", aim),
        (3, "outback", p(aim, rightArm={"pitch": -98})),
        (5, "inoutsine", aim),
        (8, "inoutsine", LAZY),
    ], "Pika Pika: a finger pointed and a laser out of its tip, the other hand left in its pocket.", RIGHT)
    gather = p(aim, rightArm={"pitch": -88, "bend": -4}, body={"yaw": 14, "bend": 0}, rightLeg=leg(6, 4, 10), leftLeg=leg(-10, -4, 14))
    beats = [(0, "linear", LAZY), (4, "outexpo", gather)]
    for t in range(8, 121, 4):
        beats.append((t, "inoutsine", p(gather, rightArm={"pitch": -88 + (1.2 if t % 8 else -1.2)})))
    write_parts("pika_laser_charge", 120, beats, "Pika Pika: the light gathered at the fingertip, the arm held out, trembling with it.", RIGHT)
    write_parts("pika_laser_fire", 10, [
        (0, "constant", gather),
        (1, "outexpo", p(gather, rightArm={"pitch": -112, "bend": -10}, body={"yaw": 8, "bend": -10, "z": 0.06}, head={"pitch": -6})),
        (3, "outback", p(gather, rightArm={"pitch": -104, "bend": -6}, body={"yaw": 10, "bend": -6, "z": 0.04})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: the beam let go, the arm thrown up by it.", RIGHT)
    write_parts("pika_laser_end", 6, [(0, "constant", aim), (6, "inoutsine", LAZY)], "Pika Pika: the pointed hand back into its pocket.", RIGHT)


# ------------------------------------------------------------------------------------------------------ Yata no Kagami, the stream

JOIN_R = arm(-62, -34, -14, -64)
JOINED = arms(right=JOIN_R, left=mirror(JOIN_R))


def yata():
    joined = p(JOINED, legs(0, 0), body={"bend": 6}, head={"pitch": 6})
    write_parts("pika_yata", 10, [
        (0, "linear", LAZY),
        (2, "outexpo", joined),
        (3, "inoutsine", p(joined, rightArm={"pitch": -66}, leftArm={"pitch": -66})),
        (4, "constant", p(joined, rightArm={"pitch": -70}, leftArm={"pitch": -70})),
        (5, "constant", p(LAZY, rightArm=arm(-30, 0, 20, -20), leftArm=arm(-30, 0, -20, -20), body={"bend": 2})),
        (10, "inoutsine", LAZY),
    ], "Pika Pika: Yata no Kagami, light formed between the joined hands, and he is gone along it.", ARMS)
    streak = p(POCKETS, rightLeg=leg(10, 2, 4), leftLeg=leg(16, -2, 8), body={"pitch": 70}, head={"pitch": -56})
    beats = [(0, "linear", LAZY), (3, "outexpo", streak)]
    for t in range(6, 61, 6):
        beats.append((t, "inoutsine", p(streak, body={"roll": 4 if t % 12 else -4})))
    write_parts("pika_stream", 60, beats, "Pika Pika: streaming as light, laid out along the way he goes.", ALL_BUT_HEAD)
    write_parts("pika_stream_end", 6, [(0, "constant", streak), (6, "inoutsine", LAZY)], "Pika Pika: out of the stream of light, upright again.",
                ALL_BUT_HEAD)


# ------------------------------------------------------------------------------------------------------ Yasakani no Magatama

CROSS_R = arm(-100, -30, -4, -18)
CROSSED = arms(right=CROSS_R, left=mirror(CROSS_R))


# 2.9.7 (the owner: "adapt the other abilities to your reference gifs"): as at Egghead, up in the air over them, both arms thrust out
# toward the ground ahead, the hands open (the light pours from both palms), the legs together under him
POUR_R = arm(-62, -14, 8, -6)
POURING = arms(right=POUR_R, left=mirror(POUR_R))


def yasakani():
    rising = p(OPEN_ARMS, rightLeg=leg(-30, 4, 50), leftLeg=leg(-10, -4, 34), body={"bend": 6})
    pour = p(POURING, rightLeg=leg(-6, 3, 10), leftLeg=leg(8, -3, 16), body={"bend": 14})
    beats = [(0, "linear", LAZY), (3, "outexpo", rising), (6, "outback", pour)]
    for t in range(8, 81, 2):
        jolt = 5 if t % 4 else -5
        beats.append((t, "inoutsine", p(pour, rightArm={"pitch": -62 - jolt}, leftArm={"pitch": -62 + jolt})))
    write_parts("pika_yasakani", 80, beats,
                "Pika Pika: Yasakani no Magatama, up in the air over them, both arms thrust out, a torrent of light out of both palms.",
                ALL_BUT_HEAD)
    open_ = p(arms(right=arm(-60, 0, 70, -10), left=arm(-60, 0, -70, -10)), rightLeg=leg(-8, 4, 20), leftLeg=leg(6, -4, 30),
              body={"bend": -4}, head={"pitch": 4})
    write_parts("pika_yasakani_end", 12, [(0, "constant", pour), (3, "outexpo", open_), (12, "inoutsine", LAZY)],
                "Pika Pika: the torrent spent, the arms thrown open, coming down.", ALL_BUT_HEAD)


# ------------------------------------------------------------------------------------------------------ Ama no Murakumo

# 2.9.0 (the owner: "The sword grabbing is still broken in third person because you're grabbing it way too up, and the animations of
# the sword are fucking awful. Not to mention that they don't even work in first person"): Ama no Murakumo held at the bottom of its
# grip, the blade leaning on from the fist toward the line of the arm (pika/LightArt.LightArtGrip), the right elbow always straight (the
# game hangs the sword on the arm's own pivot: a bent elbow would leave the sword where the arm was), and every cut a real cut, its blade
# swept through a plane by the arm and the body's turn: arm(pitch, yaw, roll) with roll 90 sweeps the arm level across the front as the
# pitch goes (the blade flat at the shoulder), roll -45 down a diagonal, roll 0 up and down the middle. Only the right arm and the body
# (and the legs where he lunges) are keyed: the left arm and the head are the player's own.
HOLD_R = arm(-40, 10, 8, 0)
# 2.9.7: the lunge's arm: thrust straight out ahead along the laid-out body (the blade, leaning off the fist toward the arm's line,
# points on along the way he flies)
LUNGE_R = arm(-122, 4, 0, 0)
SWORD = ("body", "torso", "rightArm")
SWORD_LUNGE = ("body", "torso", "rightArm", "rightLeg", "leftLeg")


def murakumo():
    joined = p(JOINED, legs(0, 0), body={"bend": 4})
    drawn = p(rightArm=arm(-40, 30, 30, 0), leftArm=arm(-40, -10, -30, -40), body={"yaw": 16})
    write_parts("pika_murakumo", 10, [
        (0, "linear", LAZY),
        (3, "outexpo", joined),
        (5, "outexpo", drawn),
        (6, "outback", p(drawn, rightArm={"yaw": 36})),
        (10, "inoutsine", p(LAZY, rightArm=HOLD_R)),
    ], "Pika Pika: Ama no Murakumo, the joined hands drawn apart and a sword of solid light between them.", ARMS)
    ready = p(rightArm=HOLD_R)
    write_loop("pika_sword_idle", 40, [(0, "inoutsine", p(ready, body={"bend": 3})),
                                       (20, "inoutsine", p(ready, rightArm={"pitch": -43}, body={"bend": 4})),
                                       (40, "inoutsine", p(ready, body={"bend": 3}))],
               "Pika Pika: Ama no Murakumo held low at his side, pointed ahead, the right hand easy on the end of its grip.",
               parts=SWORD)
    write_loop("pika_sword_walk", 20, [(0, "linear", ready), (10, "linear", p(ready, rightArm={"pitch": -46})), (20, "linear", ready)],
               "Pika Pika: walking with Ama no Murakumo held low.", parts=("rightArm",))
    # each cut: (wind-up arm, through arm, body yaw from, to, bend from, to). 2.9.7: the two the clones and the lunge's last cut use
    # (the cuts on the attack button are gone with every M1)
    cuts = [
        # 1: level, right to left across the shoulders
        (arm(-10, 0, 90, 0), arm(-160, 0, 90, 0), 34, -38, 2, 6),
        # 2: back, left to right, level
        (arm(-10, 0, -90, 0), arm(-160, 0, -90, 0), -36, 34, 4, 2),
    ]
    for i, (up, through, y0, y1, b0, b1) in enumerate(cuts, start=1):
        write_parts("pika_sword_" + str(i), 8, [
            (0, "linear", ready),
            (1, "insine", p(rightArm=up, body={"yaw": y0, "bend": b0})),
            (3, "outexpo", p(rightArm=through, body={"yaw": y1, "bend": b1, "z": -0.04})),
            (4, "outback", p(rightArm=p({"a": through}, a={"pitch": through["pitch"] + (6 if through["pitch"] < -90 else -6)})["a"],
                             body={"yaw": y1 * 1.08, "bend": b1, "z": -0.04})),
            (8, "inoutsine", ready),
        ], "Pika Pika: Ama no Murakumo, cut " + str(i) + ".", SWORD)
    # 2.9.7 (the owner: "adapt the other abilities to your reference gifs ... scrap moves if you can make better moves"): as at Egghead,
    # Ama no Murakumo drawn out of the joined hands and Kizaru flying at them with it, laid out along the way he goes, the sword thrust
    # out ahead at the end of the straight right arm, the left arm swept back, the legs together trailing; held until he arrives (the
    # server ends it with the level cut, pika_sword_1)
    thrust = p(rightArm=LUNGE_R, leftArm=arm(30, 0, -24, -10), rightLeg=leg(8, 2, 4), leftLeg=leg(14, -2, 10), body={"pitch": -72, "bend": -6})
    beats = [(0, "linear", LAZY), (2, "outexpo", joined), (3, "outexpo", drawn), (5, "outexpo", thrust)]
    for t in range(9, 41, 4):
        beats.append((t, "inoutsine", p(thrust, body={"pitch": -72 + (2 if t % 8 else -2), "roll": 3 if t % 8 else -3})))
    write_parts("pika_lunge", 40, beats, "Pika Pika: Ama no Murakumo drawn, and flying at them with it thrust out ahead.", ALL_BUT_HEAD)


# ------------------------------------------------------------------------------------------------------ the speed-of-light kick, the clones

def light_kick():
    gather = p(LAZY, rightLeg=leg(-44, 6, 96), leftLeg=leg(-4, -4, 26), body={"bend": -8, "y": -0.08})
    beats = [(0, "linear", LAZY), (6, "outexpo", gather)]
    for t in range(10, 81, 4):
        beats.append((t, "inoutsine", p(gather, rightLeg={"pitch": -44 + (2 if t % 8 else -2)})))
    write_parts("pika_light_kick_charge", 80, beats,
                "Pika Pika: the light gathered in the leg, the knee drawn up, hands in his pockets. \"Have you ever been kicked at the speed "
                "of light?\"", KICK)
    # 2.9.0 (the owner: "It is meant to be a really dramatic one, like kick from the side, like comes from the back and then rotates and
    # it hits on the left side of the mob or whatever was hit, like on the around the head area"): out of the light behind them, turned
    # away, he spins round (most of a turn in three ticks), the right leg swung out wide and level and driven into the left side of their
    # head on tick 3 (server/PikaMoves lands it then), follows through, and drops to the ground
    tuck = p(rightLeg=leg(-40, 6, 80), leftLeg=leg(-30, -6, 70), body={"yaw": 300, "y": 0.12, "bend": 6})
    chamber = p(rightLeg=leg(-70, 34, 100), leftLeg=leg(-12, -4, 40), body={"yaw": 150, "y": 0.1, "roll": -6})
    strike = p(rightLeg=leg(-96, 26, 0), leftLeg=leg(10, -6, 30), body={"yaw": 0, "y": 0.08, "roll": -20, "bend": -10})
    follow = p(rightLeg=leg(-92, 36, 4), leftLeg=leg(10, -6, 30), body={"yaw": -36, "y": 0.06, "roll": -18, "bend": -8})
    land = p(rightLeg=leg(-14, 6, 34), leftLeg=leg(6, -6, 40), body={"yaw": -30, "y": -0.12, "bend": 12})
    write_parts("pika_light_kick", 16, [
        (0, "constant", tuck),
        (2, "insine", chamber),
        (3, "outexpo", strike),
        (6, "outsine", follow),
        (10, "inoutsine", land),
        (16, "inoutsine", p(rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0), body={"yaw": 0})),
    ], "Pika Pika: the speed-of-light kick: out of the light behind them, a spinning kick into the left side of the head.", KICK)


def clones():
    spread = p(arms(right=arm(-80, 0, 90, -4), left=arm(-80, 0, -90, -4)), rightLeg=leg(-6, 6, 10), leftLeg=leg(4, -6, 14),
               body={"bend": -12}, head={"pitch": -14})
    write_parts("pika_clones", 12, [
        (0, "linear", LAZY),
        (2, "insine", p(JOINED, legs(0, 0), body={"bend": 8})),
        (4, "outexpo", spread),
        (6, "outback", p(spread, rightArm={"roll": 96}, leftArm={"roll": -96})),
        (12, "inoutsine", LAZY),
    ], "Pika Pika: the light thrown out of the body, every bead of it a Borsalino of light.", ARMS)


def main():
    stances()
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
