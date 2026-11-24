#!/usr/bin/env python3
"""3.5.5, rebuilt in 3.6.3 on the reference's moveset (the old moves and their clips are gone): the Mochi Mochi no Mi's own animations
(Charlotte Katakuri's way: a still, upright fighter whose arms do the work, the mochi stretching out of them): the straight punch from
the shoulder, one arm then the other; Grilled Mochi's pointed arm swelling, then kicking as the fist flies; Lotus Mochi Combo's fling,
hold and thrust; Mochi Muddle's arms out and the dive; Chestnut Mochi's fling up; the Anemone's point; Buzz Cut Mochi from the wheel
to the slam; the Awakening's arms pressed down.

Writes src/main/resources/assets/hexhaki/player_animation/mochi_<move>.json and client/npc/MochiPoses (StylePoser samples them). Every
strike lands on the tick server/MochiMoves lands it. No clip keys a leg, the head, nor the body's place or pitch
(tools/gura_anims.write_parts: the arms and the upper body only); no stance.

Usage: python3 tools/mochi_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gura_anims import ARMS, REST, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, mirror, p  # noqa: E402

# the right arm straight out at the crosshair, the left drawn back by the hip (and the other way round)
RIGHT_OUT = p(rightArm=arm(-90, -6, 4, -2), leftArm=arm(22, 0, -16, -34), body={"yaw": 18, "bend": 4})
LEFT_OUT = p(leftArm=mirror(arm(-90, -6, 4, -2)), rightArm=mirror(arm(22, 0, -16, -34)), body={"yaw": -18, "bend": 4})


# ------------------------------------------------------------------------------------------------------ LMB: Mochi Punch

def punch():
    # the fist chambered by the ribs, then driven straight out from the shoulder (the mochi arm stretches on out of it on tick 3);
    # held a beat while the arm snaps back, then let down
    chamber_r = p(rightArm=arm(-30, 0, 14, -96), leftArm=arm(-40, 0, -12, -60), body={"yaw": -14, "bend": 2})
    write_parts("mochi_punch_1", 10, [
        (0, "linear", REST),
        (1, "insine", chamber_r),
        (3, "outexpo", RIGHT_OUT),
        (6, "outsine", p(RIGHT_OUT, rightArm={"pitch": -86})),
        (10, "inoutsine", REST),
    ], "Mochi Mochi: Mochi Punch: the right fist driven straight out from the shoulder, the arm stretching on as mochi.", ARMS)
    chamber_l = p(leftArm=mirror(arm(-30, 0, 14, -96)), rightArm=mirror(arm(-40, 0, -12, -60)), body={"yaw": 14, "bend": 2})
    write_parts("mochi_punch_2", 10, [
        (0, "linear", REST),
        (1, "insine", chamber_l),
        (3, "outexpo", LEFT_OUT),
        (6, "outsine", p(LEFT_OUT, leftArm={"pitch": -86})),
        (10, "inoutsine", REST),
    ], "Mochi Mochi: Mochi Punch: the left fist driven straight out, the arm stretching on as mochi.", ARMS)


# ------------------------------------------------------------------------------------------------------ C: Lotus Mochi Combo

def lotus():
    # 3.6.3, on the reference's beats (mochi/MochiDraw.LOTUS_*): the left hand flings the ribbon up and over to the left (tick 4), held
    # out at the donut while it forms, pulled back with the flash (31), thrust out with its punch (34), both arms swept up with the
    # launch (39), then let down
    fling = p(leftArm=mirror(arm(-125, 0, 80, -8)), body={"yaw": -12, "bend": -4})
    held = p(leftArm=mirror(arm(-80, 0, 70, -6)), body={"yaw": -16, "bend": 0})
    write_parts("mochi_lotus", 46, [
        (0, "linear", REST),
        (2, "insine", p(leftArm=mirror(arm(-15, 0, 20, -30)), body={"yaw": 8, "bend": 2})),
        (4, "outexpo", fling),
        (8, "outsine", held),
        (28, "linear", p(held, leftArm={"pitch": -84})),
        (31, "insine", p(leftArm=mirror(arm(-40, 0, 40, -60)), body={"yaw": -6, "bend": 2})),
        (34, "outexpo", p(leftArm=mirror(arm(-95, 10, 10, -2)), body={"yaw": -20, "bend": 4})),
        (39, "outexpo", p(leftArm=mirror(arm(-20, 0, 150, -10)), rightArm=arm(-20, 0, 150, -10), body={"yaw": 0, "bend": -6})),
        (46, "inoutsine", REST),
    ], "Mochi Mochi: Lotus Mochi Combo: the left hand flings the dough out to the side, holds the donut, thrusts its punch, sweeps the "
       "launch up.", ARMS)


# ------------------------------------------------------------------------------------------------------ X: Mochi Muddle

def muddle():
    # 3.6.3, on the reference's beats (mochi/MochiDraw.MUDDLE_*, the splash on 28): both arms out to the sides gathering (to 22), swung
    # up and forward (24), thrown down and ahead with the body leaning into the dive (27), held low in the pool, then back up
    out = p(rightArm=arm(-10, 0, 80, -10), leftArm=mirror(arm(-10, 0, 80, -10)), body={"yaw": 0, "bend": -2})
    beats = [(0, "linear", REST), (4, "outsine", out)]
    beats += tremble(out, 8, 21, 3, lambda b, s: p(b, rightArm={"roll": 80 + 2.0 * s}, leftArm={"roll": -80 - 2.0 * s}))
    beats += [
        (24, "insine", p(rightArm=arm(-115, 0, 35, -10), leftArm=mirror(arm(-115, 0, 35, -10)), body={"yaw": 0, "bend": -6})),
        (27, "outexpo", p(rightArm=arm(-55, 0, 14, -4), leftArm=mirror(arm(-55, 0, 14, -4)), body={"yaw": 0, "bend": 24})),
        (40, "linear", p(rightArm=arm(-50, 0, 16, -6), leftArm=mirror(arm(-50, 0, 16, -6)), body={"yaw": 0, "bend": 20})),
        (50, "inoutsine", REST),
    ]
    write_parts("mochi_muddle", 50, beats, "Mochi Mochi: Mochi Muddle: the arms out gathering, then the dive down into the splash of dough.",
                ARMS)


# ------------------------------------------------------------------------------------------------------ V: Chestnut Mochi

def chestnut():
    # 3.6.3, on the reference's beats (mochi/MochiDraw.CHESTNUT_*): the right arm drawn down and back, flung straight up throwing the
    # comet of dough (4), held up while the disc forms over the head (to 12), let down
    up = p(rightArm=arm(-130, 0, 50, -6), body={"yaw": 6, "bend": -6})
    write_parts("mochi_chestnut", 22, [
        (0, "linear", REST),
        (2, "insine", p(rightArm=arm(30, 0, 10, -40), body={"yaw": 10, "bend": 4})),
        (4, "outexpo", up),
        (12, "linear", up),
        (22, "inoutsine", REST),
    ], "Mochi Mochi: Chestnut Mochi: the right arm flings a comet of dough straight up, held while the spiked disc forms overhead.",
        ("body", "torso", "rightArm"))


# ------------------------------------------------------------------------------------------------------ N: Buzz Cut Mochi

def buzz():
    # 3.6.3, from the catch on (mochi/MochiDraw.BUZZ_*; the right arm itself is the mace, hidden and drawn by MochiDraw.buzzMace): the
    # body re-formed out of the wheel twisted back (3), the heavy swing round into the one caught (7), leaning back against the whirl
    # with the left arm out (to 52; the whole body turns with the arm: the yaw is the user's own), the arm raised overhead (60), a beat
    # leaning back (63), slammed down leaning in (66), held, let down
    whirl = p(rightArm=arm(-86, 0, 6, -2), leftArm=mirror(arm(-25, 0, 65, -12)), body={"yaw": 6, "bend": -8})
    over = p(rightArm=arm(-130, 0, 60, -4), leftArm=mirror(arm(-10, 0, 45, -20)), body={"yaw": 0, "bend": -12})
    beats = [
        (0, "linear", REST),
        (3, "outsine", p(rightArm=arm(-80, 0, 30, -10), leftArm=mirror(arm(-30, 0, 40, -30)), body={"yaw": -40, "bend": 2})),
        (7, "inexpo", p(rightArm=arm(-88, 0, 8, -2), leftArm=mirror(arm(-20, 0, 55, -20)), body={"yaw": 28, "bend": 8})),
        (11, "outsine", whirl),
    ]
    beats += tremble(whirl, 14, 50, 3, lambda b, s: p(b, body={"yaw": 6 + 3.0 * s, "bend": -8}))
    beats += [
        (52, "linear", whirl),
        (60, "outsine", over),
        (63, "linear", p(over, body={"bend": -15})),
        (66, "inexpo", p(rightArm=arm(-55, 0, 10, -2), leftArm=mirror(arm(-30, 0, 40, -20)), body={"yaw": 4, "bend": 30})),
        (76, "linear", p(rightArm=arm(-52, 0, 10, -2), leftArm=mirror(arm(-28, 0, 40, -20)), body={"yaw": 4, "bend": 26})),
        (86, "inoutsine", REST),
    ]
    write_parts("mochi_buzz", 86, beats, "Mochi Mochi: Buzz Cut Mochi: out of the wheel, the heavy swing, leaning back in the whirl, the arm "
                "raised overhead and slammed down.", ARMS)


# ------------------------------------------------------------------------------------------------------ Z: Grilled Mochi

# 3.6.3 (the owner: "C Move, should be held, the longer its held the bigger the explosion, only 1 arm should be pointed, no other part of
# the body should be animated"): the right arm alone, pointed straight out at the crosshair while the mochi swells round it
POINTED = p(rightArm=arm(-90, 0, 4, -2))


def yaki():
    # held (to MochiMoves.YAKI_MOST, 100 ticks): the arm raised to point in 4 ticks, then a fine tremble as the pressure builds; the arm
    # alone is keyed (no body, no other limb)
    beats = [(0, "linear", REST), (4, "outsine", POINTED)]
    beats += tremble(POINTED, 8, 100, 3, lambda b, s: p(b, rightArm={"pitch": -90 + 1.1 * s, "yaw": 0.7 * s}))
    write_parts("mochi_yaki_charge", 100, beats,
                "Mochi Mochi: Yaki Mochi held: the right arm pointed straight out while the mochi swells round it.", ("rightArm",))
    # let go: the arm kicks up with the fist's burst, then down
    write_parts("mochi_yaki_fire", 10, [
        (0, "constant", POINTED),
        (2, "outexpo", p(POINTED, rightArm={"pitch": -104})),
        (5, "outsine", p(POINTED, rightArm={"pitch": -94})),
        (10, "inoutsine", REST),
    ], "Mochi Mochi: Yaki Mochi let go: the pointed arm kicks as the swollen fist bursts away on its jet of fire.", ("rightArm",))


# ------------------------------------------------------------------------------------------------------ M: the Awakening

def awaken():
    # both arms flung out and down, palms to the ground as it turns to mochi round the feet, the chest up
    spread = p(rightArm=arm(-20, 0, 52, -6), leftArm=mirror(arm(-20, 0, 52, -6)), body={"bend": -10})
    pressed = p(rightArm=arm(-30, 0, 34, -4), leftArm=mirror(arm(-30, 0, 34, -4)), body={"bend": 14})
    write_parts("mochi_awaken", 18, [
        (0, "linear", REST),
        (3, "outback", spread),
        (6, "outexpo", pressed),
        (12, "outsine", p(pressed, body={"bend": 12})),
        (18, "inoutsine", REST),
    ], "Mochi Mochi: Awakening: the arms flung out and pressed down, the ground round the feet turning to mochi.", ARMS)


# ------------------------------------------------------------------------------------------------------ B: Mochi Anemone

def donuts():
    # the right arm pointed at the target, the left drawn back; a jab of the pointed fist with each giant fist (every 4 ticks from 6)
    aim = p(rightArm=arm(-90, -8, 4, -4), leftArm=arm(18, 0, -20, -36), body={"yaw": 18, "bend": 4})
    beats = [(0, "linear", REST), (4, "outsine", aim)]
    for k in range(5):
        t = 6 + 4 * k
        beats += [(t, "outexpo", p(aim, rightArm={"pitch": -94, "bend": 0}, body={"bend": 7})), (t + 2, "outsine", aim)]
    beats += [(32, "inoutsine", REST)]
    write_parts("mochi_donuts", 32, beats,
                "Mochi Mochi: Mochi Anemone: pointing at the place as Haki fists punch down out of the donuts over it, one after another.",
                ARMS)


# ------------------------------------------------------------------------------------------------------ awakened LMB: Trident Lacerations

# 3.6.4, on the reference's beats (docs/handoff/next-ai-3.6.3.md P; mochi/MochiV2.TRIDENT_*: each swing strikes on tick 3): Mogura held
# out of the right fist's front as the game holds a tool (MochiV2.mogura), so the arm's yaw sweeps it level round and its pitch lifts
# it: a thrust, a level slash from right to left, a full spin, a rising diagonal slash, a low sweep up that launches. The left arm out
# for balance; no head, no leg; the arm's roll kept near 0 (a raised arm's roll swings it into the head).
BALANCE = mirror(arm(-14, 0, 28, -18))


def trident():
    swings = [
        # 1: the thrust: chambered back and turned right, driven out and in
        [(0, "linear", REST),
         (2, "insine", p(rightArm=arm(25, 10, 2, 0), leftArm=BALANCE, body={"yaw": -25, "bend": 2})),
         (3, "outexpo", p(rightArm=arm(-12, -5, 2, 0), leftArm=BALANCE, body={"yaw": 15, "bend": 10})),
         (6, "outsine", p(rightArm=arm(-8, -5, 2, 0), leftArm=BALANCE, body={"yaw": 12, "bend": 8})),
         (10, "inoutsine", REST)],
        # 2: the level slash, right to left through the one ahead
        [(0, "linear", REST),
         (1, "insine", p(rightArm=arm(-12, 85, 2, 0), leftArm=BALANCE, body={"yaw": -30, "bend": 2})),
         (3, "outsine", p(rightArm=arm(-15, 0, 2, 0), leftArm=BALANCE, body={"yaw": 5, "bend": 6})),
         (5, "outexpo", p(rightArm=arm(-12, -75, 2, 0), leftArm=BALANCE, body={"yaw": 40, "bend": 6})),
         (10, "inoutsine", REST)],
        # 3: the spin: the whole body turned once round, Mogura out to the right sweeping level all round (the turn's last part a
        # jump from 300 to its equal -60 under a constant key, so the clip ends facing the way it began without turning back)
        [(0, "linear", REST),
         (1, "insine", p(rightArm=arm(-10, 60, 2, 0), leftArm=BALANCE, body={"yaw": 0, "bend": 4})),
         (3, "linear", p(rightArm=arm(-10, 60, 2, 0), leftArm=BALANCE, body={"yaw": 150, "bend": 4})),
         (5, "linear", p(rightArm=arm(-10, 60, 2, 0), leftArm=BALANCE, body={"yaw": 300, "bend": 4})),
         (6, "constant", p(rightArm=arm(-10, 60, 2, 0), leftArm=BALANCE, body={"yaw": -60, "bend": 4})),
         (7, "outsine", p(rightArm=arm(-10, 40, 2, 0), leftArm=BALANCE, body={"yaw": 0, "bend": 4})),
         (10, "inoutsine", REST)],
        # 4: the rising diagonal, low on the right up high to the left
        [(0, "linear", REST),
         (1, "insine", p(rightArm=arm(35, 60, 2, 0), leftArm=BALANCE, body={"yaw": -25, "bend": 6})),
         (3, "outsine", p(rightArm=arm(-40, 0, 2, 0), leftArm=BALANCE, body={"yaw": 10, "bend": 2})),
         (5, "outexpo", p(rightArm=arm(-100, -20, 2, 0), leftArm=BALANCE, body={"yaw": 30, "bend": -6})),
         (10, "inoutsine", REST)],
        # 5: the low sweep up that throws them into the sky
        [(0, "linear", REST),
         (1, "insine", p(rightArm=arm(45, 15, 2, 0), leftArm=BALANCE, body={"yaw": -15, "bend": 8})),
         (3, "outsine", p(rightArm=arm(-45, 5, 2, 0), leftArm=BALANCE, body={"yaw": 5, "bend": 0})),
         (5, "outexpo", p(rightArm=arm(-110, 0, 2, 0), leftArm=BALANCE, body={"yaw": 8, "bend": -8})),
         (10, "inoutsine", REST)],
    ]
    names = ["the thrust", "the level slash", "the spin", "the rising diagonal", "the low sweep up that launches"]
    for k, beats in enumerate(swings):
        write_parts("mochi_trident_%d" % (k + 1), 10, beats, "Mochi Mochi: awakened, Trident Lacerations: Mogura's " + names[k] + ".", ARMS)


def main():
    punch()
    lotus()
    muddle()
    chestnut()
    buzz()
    yaki()
    awaken()
    donuts()
    trident()
    names = sorted(n for n in WRITTEN if n.startswith("mochi_"))
    java_poses(names, "MochiPoses", "tools/mochi_anims.py", "the Mochi fruit's animations' keyframes")
    print(f"wrote {len(names)} Mochi animations and their poses")


if __name__ == "__main__":
    main()
