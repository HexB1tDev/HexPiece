#!/usr/bin/env python3
"""3.4.2: the Ito Ito no Mi's own animations (3.3.7 reused other fruits' clips; the owner after playing it: "I dont like how M1 looks
animationwise, its shitty", Overheat "The charging should be one arm pointed forward and the strings gathering infront of the fist",
the Birdcage "the user points at the sky and shoots a stedy stream of strings"). Doflamingo's way: the hand does the work, never a
sword's swing: the strings are whipped out of the fingers by a cocked hand flicked out straight (the elbow snapping open), forehand
then backhand; the hand raised high and raked down for Goshikito's five strings; one arm pointed straight ahead while Overheat's rope
winds itself in front of the fist; the arm straight up at the sky while the Birdcage's strings pour up, then the hand yanked down to
close it.

Writes src/main/resources/assets/hexhaki/player_animation/ito_<move>.json and client/npc/ItoPoses (StylePoser samples them: the
gathering rope sits on the posed fist). Every strike lands on the tick server/ItoMoves lands it (a lash on 2, Goshikito's rake on 5).
No clip keys a leg, the head, nor the body's place or pitch (tools/gura_anims.write_parts: the arms and the upper body only); no stance.

Usage: python3 tools/ito_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gura_anims import ARMS, REST, RIGHT, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, mirror, p  # noqa: E402


# ------------------------------------------------------------------------------------------------------ LMB: String Slash

def slash():
    # forehand: the hand cocked back by the right shoulder (the elbow bent and up), then flicked out across to the left, the elbow
    # snapping straight: a whip cracked, not a blade swung (server/ItoMoves lands it on tick 2)
    cocked = p(rightArm=arm(-70, 62, 30, -70), leftArm=arm(-18, 0, -14, -26), body={"yaw": -20, "bend": -2})
    flick = p(rightArm=arm(-92, -32, 6, -2), leftArm=arm(10, 0, -16, -20), body={"yaw": 18, "bend": 6})
    write_parts("ito_slash_1", 10, [
        (0, "linear", REST),
        (1, "insine", cocked),
        (2, "outexpo", flick),
        (4, "outsine", p(flick, rightArm={"yaw": -44, "pitch": -84})),
        (10, "inoutsine", REST),
    ], "Ito Ito: String Slash: the hand cocked by the shoulder and flicked out across, strings lashing from the fingers.", ARMS)
    # backhand: the forearm folded across the chest, the hand by the left shoulder, then flung open out to the right
    folded = p(rightArm=arm(-42, -32, 4, -84), leftArm=arm(-10, 0, -14, -24), body={"yaw": 20, "bend": 2})
    flung = p(rightArm=arm(-90, 52, 10, -2), leftArm=arm(14, 0, -16, -20), body={"yaw": -18, "bend": 4})
    write_parts("ito_slash_2", 10, [
        (0, "linear", REST),
        (1, "insine", folded),
        (2, "outexpo", flung),
        (4, "outsine", p(flung, rightArm={"yaw": 62, "roll": 14})),
        (10, "inoutsine", REST),
    ], "Ito Ito: String Slash: a backhand, the folded arm flung open, strings lashing out to the side.", ARMS)


# ------------------------------------------------------------------------------------------------------ Z: Goshikito

def goshiki():
    # the hand raised high, fingers to the sky (five strings hanging from them), then raked down and across in front: the five strings
    # come down onto everything ahead on tick 5
    raised = p(rightArm=arm(-166, 10, -14, -12), leftArm=arm(-14, 0, -16, -20), body={"yaw": -10, "bend": -6})
    raked = p(rightArm=arm(-38, -28, 6, -6), leftArm=arm(16, 0, -18, -16), body={"yaw": 14, "bend": 18})
    write_parts("ito_goshiki", 16, [
        (0, "linear", REST),
        (3, "outback", raised),
        (4, "linear", p(raised, rightArm={"pitch": -170})),
        (5, "outexpo", raked),
        (8, "outsine", p(raked, rightArm={"pitch": -30}, body={"bend": 20})),
        (16, "inoutsine", REST),
    ], "Ito Ito: Goshikito: the hand raised high, then raked down, five strings coming down onto everything ahead.", ARMS)


# ------------------------------------------------------------------------------------------------------ X: Overheat

POINTED = p(rightArm=arm(-90, -10, 6, -2), leftArm=arm(14, 0, -14, -22), body={"yaw": 14, "bend": 4})


def overheat():
    # held: one arm pointed straight ahead at the crosshair, the rope winding itself in front of the fist (it trembles harder as it
    # heats); let go: the arm jolts with the beam's kick, then comes down
    beats = [(0, "linear", REST), (4, "outsine", POINTED)]
    beats += tremble(POINTED, 6, 96, 4, lambda b, s: p(b, rightArm={"pitch": -90 + 1.2 * s, "yaw": -10 - 0.8 * s}))
    write_parts("ito_heat_charge", 96, beats,
                "Ito Ito: Overheat held: one arm pointed straight ahead, the red-hot rope winding itself in front of the fist.", ARMS)
    kick = p(POINTED, rightArm=arm(-84, 14, 18, -30), body={"yaw": 4, "bend": -6})
    write_parts("ito_heat_fire", 12, [
        (0, "constant", POINTED),
        (1, "outexpo", kick),
        (4, "outsine", p(kick, rightArm={"pitch": -70, "bend": -20})),
        (12, "inoutsine", REST),
    ], "Ito Ito: Overheat let go: the arm jolted up by the beam's kick, then lowered.", ARMS)


# ------------------------------------------------------------------------------------------------------ N: Birdcage

SKY = p(rightArm=arm(-172, 4, -12, -2), leftArm=arm(-10, 0, -18, -24), body={"bend": -8})


def birdcage():
    # held: the arm straight up at the sky, the stream of strings pouring out of the hand
    beats = [(0, "linear", REST), (4, "outback", SKY)]
    beats += tremble(SKY, 6, 96, 4, lambda b, s: p(b, rightArm={"pitch": -172 + 1.0 * s, "roll": -12 - 1.2 * s}))
    write_parts("ito_cage_charge", 96, beats, "Ito Ito: Birdcage held: the arm straight up at the sky, strings pouring up out of the hand.",
                ARMS)
    flung = p(SKY, rightArm=arm(-160, 4, -30, -4), body={"bend": -10})
    write_parts("ito_cage_cast", 14, [
        (0, "constant", SKY),
        (2, "outexpo", flung),
        (6, "outsine", SKY),
        (14, "inoutsine", REST),
    ], "Ito Ito: Birdcage let go: the hand flung open, the strings spreading into the cage.", ARMS)
    # recast: the raised hand clenched and yanked down to the chest, pulling the cage shut
    pulled = p(rightArm=arm(-26, -8, 14, -38), leftArm=arm(-6, 0, -16, -24), body={"yaw": 8, "bend": 16})
    write_parts("ito_cage_close", 16, [
        (0, "linear", REST),
        (3, "outsine", p(SKY, rightArm={"roll": -30, "yaw": 14})),
        (5, "outexpo", pulled),
        (12, "constant", pulled),
        (16, "inoutsine", REST),
    ], "Ito Ito: Birdcage recast: the raised hand clenched and yanked down, the cage closing.", ARMS)


# ------------------------------------------------------------------------------------------------------ B: Black Knight

def knight():
    # the hand held out, then the fingers drawn back as if pulling the knight's strings: weaving it, or (pressed again) bursting it
    out = p(rightArm=arm(-80, 6, 14, -6), body={"yaw": 10})
    pulled = p(rightArm=arm(-36, -4, 18, -58), body={"yaw": 4, "bend": 6})
    write_parts("ito_knight", 12, [
        (0, "linear", REST),
        (2, "outback", out),
        (5, "outexpo", pulled),
        (8, "outsine", pulled),
        (12, "inoutsine", REST),
    ], "Ito Ito: Black Knight: the hand held out, then drawn back as if pulling strings.", RIGHT)


def main():
    slash()
    goshiki()
    overheat()
    birdcage()
    knight()
    names = sorted(n for n in WRITTEN if n.startswith("ito_"))
    java_poses(names, "ItoPoses", "tools/ito_anims.py", "the String fruit's animations' keyframes, for the strings on the fist")
    print(f"wrote {len(names)} Ito animations and their poses")


if __name__ == "__main__":
    main()
