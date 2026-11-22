#!/usr/bin/env python3
"""3.1.9: the Hie Hie no Mi's animations (the owner: "make sure this one is also equally as smooth"; every fruit "the same style of animation
that you've been doing for the light, quake, and magma"; never the legs, never the head).

Writes src/main/resources/assets/hexhaki/player_animation/hie_<move>.json and their keyframes for client/npc/HiePoses (StylePoser samples
them: the orb and the ice fist sit where the pose puts the fist). Kuzan's way, as docs/handoff/hie-ref shows it: tall and easy, both arms
thrown wide as the spears form round him (Ice Block: Partisan, ref 62), one arm thrown like a javelin, both swept forward to let them
fly; the hand reaching and closing on the one he freezes (Ice Time, refs 47, 51); the hands cupped round the orb and pushed out (Ice
Ball, ref 76); the arm swept round from the side to loose the pheasant; bent over, the palm to the ground (Ice Age, refs 40, 45); leaning
into the slide, the fist cocked at the hip and driven through (the ice road and the ice fist, refs 15, 38); the saber drawn and cut
level and down. Every strike lands on the tick server/HieMoves lands it (the tap's spear on 2, the launch on 1, the grab on arrival, the
orb thrown on 10 of the gather, the pheasant on 3, the palm on 5, the punch on arrival, a cut on 2). No clip keys a leg, nor the body's
place or pitch (tools/gura_anims.write_parts: the arms and the upper body only); no stance.

Usage: python3 tools/hie_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gura_anims import ARMS, REST, RIGHT, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, mirror, p  # noqa: E402

SPREAD = p(rightArm=arm(-38, 0, 72, -10), leftArm=mirror(arm(-38, 0, 72, -10)), body={"bend": -4})
COCKED = p(rightArm=arm(30, 0, 12, -100), leftArm=arm(-58, 10, -10, -20), body={"yaw": -18, "bend": 16})
CUP = p(rightArm=arm(-58, -36, 4, -68), leftArm=mirror(arm(-58, -36, 4, -68)), body={"bend": 4})


def partisan():
    beats = [(0, "linear", REST), (5, "outback", SPREAD)]
    beats += tremble(SPREAD, 8, 140, 6, lambda b, s: p(b, rightArm={"roll": 72 + 1.5 * s}, leftArm={"roll": -72 - 1.5 * s}))
    write_parts("hie_partisan_raise", 140, beats, "Hie Hie: Ice Block: Partisan: both arms thrown wide as the spears of ice form round him.",
                ARMS)
    wind = p(rightArm=arm(-120, 12, 24, -40), body={"yaw": -20})
    thrown = p(rightArm=arm(-86, -10, 6, -4), body={"yaw": 22, "bend": 6})
    write_parts("hie_partisan_throw", 10, [
        (0, "linear", REST),
        (1, "insine", wind),
        (2, "outexpo", thrown),
        (5, "outsine", p(thrown, rightArm={"pitch": -80})),
        (10, "inoutsine", REST),
    ], "Hie Hie: Ice Block: Partisan tapped: a spear of ice thrown like a javelin.", RIGHT)
    point = p(rightArm=arm(-88, -14, 8, -4), leftArm=mirror(arm(-88, -14, 8, -4)), body={"bend": 8})
    write_parts("hie_partisan_launch", 14, [
        (0, "constant", SPREAD),
        (2, "outexpo", point),
        (7, "outsine", p(point, rightArm={"pitch": -84}, leftArm={"pitch": -84})),
        (14, "inoutsine", REST),
    ], "Hie Hie: Ice Block: Partisan let go: both arms swept forward, every spear flying.", ARMS)


def ice_time():
    reach = p(rightArm=arm(-80, -6, 10, -10), leftArm=arm(20, 0, -14, -10), body={"bend": 14, "yaw": 8})
    write_parts("hie_grab_reach", 24, [(0, "linear", REST), (3, "outexpo", reach), (24, "constant", reach)],
                "Hie Hie: Ice Time: lunging, the hand reaching out.", ARMS)
    grab = p(reach, rightArm=arm(-90, -4, 4, -30), body={"bend": 10, "yaw": 12})
    write_parts("hie_grab", 14, [
        (0, "constant", reach),
        (1, "outexpo", grab),
        (7, "outsine", p(grab, rightArm={"bend": -34})),
        (14, "inoutsine", REST),
    ], "Hie Hie: Ice Time: the hand closing on them, freezing them solid.", ARMS)


def ice_ball():
    beats = [(0, "linear", REST), (4, "inoutsine", CUP)]
    beats += tremble(CUP, 5, 10, 2, lambda b, s: p(b, rightArm={"yaw": -36 + s}, leftArm={"yaw": 36 - s}))
    beats.append((10, "linear", CUP))
    write_parts("hie_ball_gather", 10, beats, "Hie Hie: Ice Ball: the hands cupped round the orb of ice gathering between them.", ARMS)
    push = p(rightArm=arm(-90, -12, 6, -4), leftArm=mirror(arm(-90, -12, 6, -4)), body={"bend": 10})
    write_parts("hie_ball_throw", 12, [
        (0, "constant", CUP),
        (1, "outexpo", push),
        (5, "outsine", p(push, rightArm={"pitch": -94}, leftArm={"pitch": -94})),
        (12, "inoutsine", REST),
    ], "Hie Hie: Ice Ball: both hands pushed out, the orb thrown.", ARMS)


def pheasant():
    side = p(rightArm=arm(-20, 0, 80, -10), body={"yaw": -22})
    loose = p(rightArm=arm(-88, -22, 6, -2), body={"yaw": 22, "bend": 8})
    write_parts("hie_pheasant", 14, [
        (0, "linear", REST),
        (2, "insine", side),
        (3, "outexpo", loose),
        (7, "outsine", p(loose, rightArm={"yaw": -28}, body={"yaw": 25})),
        (14, "inoutsine", REST),
    ], "Hie Hie: Ice Block: Pheasant Beak: the arm swept round from the side, the pheasant of ice let loose.", RIGHT)


def ice_age():
    lift = p(rightArm=arm(-120, 0, 20, -30), body={"bend": 18})
    palm = p(rightArm=arm(-52, -6, 8, -4), leftArm=arm(12, 0, -18, -20), body={"bend": 52})
    write_parts("hie_ice_age", 18, [
        (0, "linear", REST),
        (3, "insine", lift),
        (5, "outexpo", palm),
        (12, "constant", palm),
        (18, "inoutsine", REST),
    ], "Hie Hie: Ice Age: bent over, the palm to the ground, the freeze running out ahead.", ARMS)


def road():
    write_parts("hie_road_slide", 30, [(0, "linear", REST), (3, "outexpo", COCKED), (30, "constant", COCKED)],
                "Hie Hie: the ice road: leaning into the slide, the iced fist cocked at the hip.", ARMS)
    punch = p(rightArm=arm(-92, 0, 2, 0), leftArm=arm(24, 0, -16, -40), body={"yaw": 26, "bend": 16})
    write_parts("hie_road_punch", 12, [
        (0, "constant", COCKED),
        (1, "outexpo", punch),
        (5, "outsine", p(punch, rightArm={"pitch": -96}, body={"yaw": 29})),
        (12, "inoutsine", REST),
    ], "Hie Hie: the ice fist driven into them.", ARMS)


def saber():
    held = p(rightArm=arm(-40, -8, 12, -30))
    write_parts("hie_saber_draw", 12, [(0, "linear", REST), (4, "outback", held), (12, "inoutsine", REST)],
                "Hie Hie: Ice Saber: the sword of ice forming in the hand.", RIGHT)
    wind = p(rightArm=arm(-80, 48, 30, -20), body={"yaw": -28})
    cut = p(rightArm=arm(-84, -58, 8, -6), body={"yaw": 28})
    write_parts("hie_saber_cut_1", 10, [
        (0, "linear", REST),
        (1, "insine", wind),
        (2, "outexpo", cut),
        (5, "outsine", p(cut, rightArm={"yaw": -62})),
        (10, "inoutsine", REST),
    ], "Hie Hie: Ice Saber: a level cut.", RIGHT)
    high = p(rightArm=arm(-140, 16, 24, -20), body={"yaw": -16})
    low = p(rightArm=arm(-42, -36, 6, -4), body={"yaw": 24, "bend": 10})
    write_parts("hie_saber_cut_2", 10, [
        (0, "linear", REST),
        (1, "insine", high),
        (2, "outexpo", low),
        (5, "outsine", p(low, rightArm={"pitch": -38})),
        (10, "inoutsine", REST),
    ], "Hie Hie: Ice Saber: a cut down across.", RIGHT)


def main():
    partisan()
    ice_time()
    ice_ball()
    pheasant()
    ice_age()
    road()
    saber()
    names = sorted(n for n in WRITTEN if n.startswith("hie_"))
    java_poses(names, "HiePoses", "tools/hie_anims.py", "the Ice fruit's animations' keyframes, for the orb and the ice fist on the fist")
    print(f"wrote {len(names)} Ice animations and their poses")


if __name__ == "__main__":
    main()
