#!/usr/bin/env python3
"""2.7.2: the Uo Uo no Mi's human-form moves (the owner: "x - Air Pressure: The user points right hand forward and beautiful air
blasts ... shoots from it"; "C - Blast breath: The user shoots a fire blast from his mouth"; "V - Wind Force: Swipe your arm forward
hard, forms a tornado"; "B - Dragon Embers: Shoot multiple fireballs from your mouth that scatter up into the sky").

Writes src/main/resources/assets/hexhaki/player_animation/uo_<move>.json in tools/style_anims.py's conventions (its helpers, its
spine rule): every blow on the tick the server makes it (server/UoMoves: AIR_BEATS, BREATH_AT, SWIPE_AT, EMBERS_AT), a wind-up before,
an overshoot after, the recovery back to rest. The legs are planted (each of these is thrown standing).

Usage: python3 tools/uo_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from anim_hand import arms, legs  # noqa: E402
from style_anims import arm, leg, p, write  # noqa: E402

REST = p(arms(right=arm(0, 0, 4, -4), left=arm(0, 0, -4, -4)), legs(0, 0))
# the server's beats (ticks), kept equal to server/UoMoves
AIR_BEATS = (4, 7, 10)
BREATH_AT = 7
SWIPE_AT = 6
EMBERS_AT = 8


def air_pressure():
    """The right hand driven out level three times, a blast of air from it each time, the body behind it, the left hand back."""
    left = arm(24, 0, -10, -40)
    stance = dict(rightLeg=leg(6, 3, 12), leftLeg=leg(-14, -3, 18))
    out = arm(-90, -4, 2, -2)
    beats = [(0, "linear", REST),
             (2, "inoutsine", p(REST, arms(right=arm(24, 0, 8, -96), left=left), body={"yaw": -18, "y": -0.04}, **stance))]
    for i, t in enumerate(AIR_BEATS):
        push = 0.06 + 0.02 * i
        beats.append((t, "outexpo", p(REST, arms(right=out, left=left), body={"yaw": 16, "z": -push, "y": -0.05, "bend": 4},
                                      head={"yaw": -8}, **stance)))
        beats.append((t + 1, "outback", p(REST, arms(right=arm(-82, -4, 4, -14), left=left),
                                          body={"yaw": 13, "z": -push + 0.04, "y": -0.05, "bend": 2}, head={"yaw": -7}, **stance)))
    beats += [(14, "inoutsine", p(REST, arms(right=arm(-40, 0, 6, -50), left=arm(10, 0, -6, -20)), body={"yaw": 4, "y": -0.02})),
              (18, "inoutsine", REST)]
    write("uo_air_pressure", 18, beats, "Uo Uo no Mi: Air Pressure, the right hand driven out three times, a blast of air from each.")


def blast_breath():
    """A breath drawn in (head back, chest up, arms opening), then thrown out: the head snapping forward, the body hunched behind it."""
    stance = dict(rightLeg=leg(4, 4, 10), leftLeg=leg(-12, -4, 16))
    inhale = p(REST, arms(right=arm(16, 0, 18, -22), left=arm(16, 0, -18, -22)), body={"bend": -12, "y": 0.02},
               head={"pitch": -32}, **stance)
    exhale = p(REST, arms(right=arm(34, 0, 26, -34), left=arm(34, 0, -26, -34)), body={"bend": 16, "z": -0.12, "y": -0.06},
               head={"pitch": 14}, **stance)
    write("uo_blast_breath", 16, [
        (0, "linear", REST),
        (4, "inoutsine", inhale),
        (6, "insine", p(inhale, head={"pitch": -36}, body={"bend": -14, "y": 0.03})),
        (BREATH_AT, "outexpo", exhale),
        (8, "outback", p(exhale, head={"pitch": 17}, body={"bend": 18, "z": -0.14, "y": -0.06})),
        (12, "inoutsine", p(exhale, head={"pitch": 8}, body={"bend": 8, "z": -0.06, "y": -0.03})),
        (16, "inoutsine", REST),
    ], "Uo Uo no Mi: Blast Breath, a breath drawn in and thrown out as fire.")


def wind_force():
    """The right arm raised high and out behind, swept forward and across hard (the tornado leaves it), followed through to the left."""
    stance = dict(rightLeg=leg(10, 4, 16), leftLeg=leg(-18, -4, 22))
    write("uo_wind_force", 16, [
        (0, "linear", REST),
        (3, "inoutsine", p(REST, arms(right=arm(-120, 20, 75, -20), left=arm(-20, 0, -30, -30)), body={"yaw": -34, "bend": -6, "y": -0.04},
                           head={"yaw": 14}, **stance)),
        (5, "insine", p(REST, arms(right=arm(-126, 24, 80, -16), left=arm(-24, 0, -32, -28)), body={"yaw": -38, "bend": -8, "y": -0.04},
                        head={"yaw": 16}, **stance)),
        (SWIPE_AT, "outexpo", p(REST, arms(right=arm(-74, -42, -6, -4), left=arm(30, 0, -20, -40)),
                                body={"yaw": 36, "bend": 10, "z": -0.12, "y": -0.07}, head={"yaw": -12}, **stance)),
        (8, "outback", p(REST, arms(right=arm(-46, -56, -12, -10), left=arm(34, 0, -18, -44)),
                         body={"yaw": 44, "bend": 12, "z": -0.14, "y": -0.07}, head={"yaw": -14}, **stance)),
        (12, "inoutsine", p(REST, arms(right=arm(-24, -20, 0, -30), left=arm(12, 0, -10, -24)), body={"yaw": 14, "bend": 4, "y": -0.03})),
        (16, "inoutsine", REST),
    ], "Uo Uo no Mi: Wind Force, the arm swept forward hard enough to raise a tornado.")


def embers():
    """Gathered low (the head down, the arms in), then thrown up: back arched, head up to the sky as the embers burst out of the mouth."""
    stance = dict(rightLeg=leg(-4, 4, 26), leftLeg=leg(-4, -4, 26))
    gather = p(REST, arms(right=arm(26, 0, 10, -70), left=arm(26, 0, -10, -70)), body={"bend": 16, "y": -0.12}, head={"pitch": 22},
               **stance)
    burst = p(REST, arms(right=arm(40, 0, 42, -12), left=arm(40, 0, -42, -12)), body={"bend": -16, "y": 0.02}, head={"pitch": -56},
              rightLeg=leg(4, 4, 6), leftLeg=leg(4, -4, 6))
    write("uo_embers", 22, [
        (0, "linear", REST),
        (5, "inoutsine", gather),
        (7, "insine", p(gather, body={"bend": 18, "y": -0.14})),
        (EMBERS_AT, "outexpo", burst),
        (9, "outback", p(burst, head={"pitch": -60}, body={"bend": -18, "y": 0.03})),
        (15, "inoutsine", p(burst, head={"pitch": -48})),
        (22, "inoutsine", REST),
    ], "Uo Uo no Mi: Dragon Embers, fire thrown up out of the mouth to scatter over the sky.")


def main():
    air_pressure()
    blast_breath()
    wind_force()
    embers()


if __name__ == "__main__":
    main()
