#!/usr/bin/env python3
"""3.1.3: the Magu Magu no Mi's animations (the owner: "make his abilities. Make sure not to animate the feet, like the legs, unless it's
absolutely needed").

Writes src/main/resources/assets/hexhaki/player_animation/magu_<move>.json and their keyframes for client/npc/MaguPoses (StylePoser
samples them: the swelling fist of magma sits where the pose puts the fist). Akainu's way, as docs/handoff/magu-ref shows it: upright and
heavy, the fist drawn back low and driven out (Dai Funka), the fist cocked at the hip and thrust straight through (Meigo), the fist
punched up at the sky (Ryusei Kazan), both arms thrust out to let the hound loose (Inugami Guren), both fists into the ground (Bakuretsu
Kazan), slumping as he melts and rising out of the magma (Magma Flow). Every strike lands on the tick server/MaguMoves lands it (the
tap's on 2, the giant fist's on 1, Meigo's on 1, the sky's on 3, the hound's on 3, the ground's on 4). No clip keys a leg, nor the
body's place or pitch (tools/gura_anims.write_parts: the arms and the upper body only); never the head; no stance.

Usage: python3 tools/magu_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gura_anims import ARMS, REST, RIGHT, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, mirror, p  # noqa: E402

DRAWN = p(rightArm=arm(28, -8, 18, -96), leftArm=arm(-62, -10, -12, -14), body={"yaw": -26, "bend": 6})
LOW = p(rightArm=arm(18, 0, 14, -70), body={"yaw": -20, "bend": 8})


def dai_funka():
    strike = p(rightArm=arm(-92, 6, 4, -4), body={"yaw": 24, "bend": 8})
    write_parts("magu_punch", 8, [
        (0, "linear", REST),
        (1, "insine", LOW),
        (2, "outexpo", strike),
        (4, "outback", p(strike, rightArm={"pitch": -97}, body={"yaw": 27})),
        (8, "inoutsine", REST),
    ], "Magu Magu: Dai Funka tapped: a fist of magma shot out of the fist.", RIGHT)
    beats = [(0, "linear", REST), (6, "outexpo", DRAWN)]
    beats += tremble(DRAWN, 8, 120, 3, lambda b, s: p(b, rightArm={"roll": 18 + 2 * s}, body={"roll": s}))
    write_parts("magu_charge", 120, beats, "Magu Magu: the fist drawn back, swelling into a giant fist of magma.", ARMS)
    punch = p(rightArm=arm(-95, 4, 2, 0), leftArm=arm(14, 0, -14, -44), body={"yaw": 30, "bend": 10})
    write_parts("magu_release", 12, [
        (0, "constant", DRAWN),
        (1, "outexpo", punch),
        (4, "outback", p(punch, rightArm={"pitch": -100}, body={"yaw": 33})),
        (12, "inoutsine", REST),
    ], "Magu Magu: Dai Funka let go: the giant fist of magma driven out.", ARMS)


def meigo():
    cocked = p(rightArm=arm(30, 0, 12, -100), leftArm=arm(-30, 0, -20, -20), body={"yaw": -22, "bend": 16})
    write_parts("magu_meigo_wind", 24, [(0, "linear", REST), (3, "outexpo", cocked), (24, "constant", cocked)],
                "Magu Magu: Meigo: lunging, the fist cocked at the hip.", ARMS)
    thrust = p(rightArm=arm(-90, 0, 2, 0), leftArm=arm(24, 0, -16, -40), body={"yaw": 26, "bend": 18})
    write_parts("magu_meigo", 12, [
        (0, "constant", cocked),
        (1, "outexpo", thrust),
        (5, "outsine", p(thrust, rightArm={"pitch": -93})),
        (12, "inoutsine", REST),
    ], "Magu Magu: Meigo: the fist of magma thrust straight through them.", ARMS)


def ryusei():
    up = p(rightArm=arm(-168, 0, 16, -10), leftArm=arm(-20, 0, -24, -20), body={"bend": -8, "yaw": 6})
    # 3.1.5 (the owner: "the more you hold X, the more fireballs you throw at the sky"): a punch up at tick 3 and every 6 ticks after,
    # 15 in all (MaguMoves.THROW_FIRST, THROW_EVERY, THROWS_MOST), the fist drawn back to the shoulder between them; held up after
    pulled = p(up, rightArm=arm(-110, 0, 24, -8), body={"bend": -2, "yaw": 6})
    beats = [(0, "linear", REST), (2, "insine", p(rightArm=arm(10, 0, 14, -90), body={"bend": 6})), (3, "outexpo", up)]
    for k in range(1, 15):
        t = 3 + 6 * k
        beats.append((t - 3, "inoutsine", pulled))
        beats.append((t, "outexpo", up))
    beats.append((96, "linear", up))
    write_parts("magu_sky", 96, beats, "Magu Magu: Ryusei Kazan: fist after fist of magma punched up at the sky while X is held.", ARMS)
    write_parts("magu_sky_end", 10, [(0, "constant", up), (10, "inoutsine", REST)], "Magu Magu: the fist brought down.", ARMS)


def hound():
    gather = p(rightArm=arm(20, 0, 16, -80), leftArm=mirror(arm(20, 0, 16, -80)), body={"bend": 4})
    loose = p(rightArm=arm(-88, -14, 8, -6), leftArm=mirror(arm(-88, -14, 8, -6)), body={"bend": 14})
    write_parts("magu_hound", 14, [
        (0, "linear", REST),
        (2, "insine", gather),
        (3, "outexpo", loose),
        (7, "outsine", p(loose, rightArm={"yaw": -30, "roll": 30}, leftArm={"yaw": 30, "roll": -30})),
        (14, "inoutsine", REST),
    ], "Magu Magu: Inugami Guren: both arms thrust out, the hound of magma let loose.", ARMS)


def ground():
    up = p(rightArm=arm(-170, 0, 16, -30), leftArm=mirror(arm(-170, 0, 16, -30)), body={"bend": -10})
    down = p(rightArm=arm(-82, 0, 6, -6), leftArm=mirror(arm(-82, 0, 6, -6)), body={"bend": 52})
    write_parts("magu_erupt", 16, [
        (0, "linear", REST),
        (2, "insine", up),
        (4, "outexpo", down),
        (8, "outsine", p(down, body={"bend": 50})),
        (16, "inoutsine", REST),
    ], "Magu Magu: Bakuretsu Kazan: both fists into the ground, the ground boiling.", ARMS)
    slump = p(rightArm=arm(-10, 0, 20, -20), leftArm=mirror(arm(-10, 0, 20, -20)), body={"bend": 30})
    write_parts("magu_melt", 8, [(0, "linear", REST), (4, "insine", slump), (8, "constant", slump)],
                "Magu Magu: melting into magma.", ARMS)
    risen = p(rightArm=arm(-130, 0, 50, -10), leftArm=mirror(arm(-130, 0, 50, -10)), body={"bend": -6})
    write_parts("magu_rise", 14, [(0, "constant", slump), (3, "outexpo", risen), (14, "inoutsine", REST)],
                "Magu Magu: rising out of the magma, the arms thrown open.", ARMS)


def main():
    dai_funka()
    meigo()
    ryusei()
    hound()
    ground()
    names = sorted(n for n in WRITTEN if n.startswith("magu_"))
    java_poses(names, "MaguPoses", "tools/magu_anims.py", "the Magma fruit's animations' keyframes, for the swelling fist of magma")
    print(f"wrote {len(names)} Magma animations and their poses")


if __name__ == "__main__":
    main()
