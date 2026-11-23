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
import json  # noqa: E402

from gura_anims import ANIM_DIR, ARMS, REST, RIGHT, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, leg, mirror, p  # noqa: E402


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


# ------------------------------------------------------------------------------------------------------ Sky Road (the air jump)

# 3.4.3 (the owner: "Sky Road movement ability ... The running animation should be unique. The player's upper body leans slightly
# forward, their arms spread loosely outward with their fingers extended, and their legs perform exaggerated, elegant running strides
# through empty air ... At higher speeds ... their legs cycle faster, and individual steps become elongated, almost like they're skating
# or leaping between invisible footholds ... a relaxed suspended stance with one leg slightly bent and their arms loosely positioned at
# their sides"): a run in the air, so the legs are keyed here (it is running); the lean and the bank into turns are the body's momentum,
# turned by client/ito/SkyRoad round the hips (never keyed: they follow the speed and the turn). Loops; never the head.
SKY_PARTS = {"body": ("yaw", "roll", "bend"), "rightArm": ("pitch", "yaw", "roll", "bend"), "leftArm": ("pitch", "yaw", "roll", "bend"),
             "rightLeg": ("pitch", "yaw", "roll", "bend"), "leftLeg": ("pitch", "yaw", "roll", "bend")}


def write_loop(name, duration, beats, description):
    moves = []
    for tick, easing, q in beats:
        for part, channels in SKY_PARTS.items():
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.4.3", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + 3, "isLoop": True, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(ANIM_DIR, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def sky():
    # a long bound: the front leg reaching far forward from a high knee, the back leg trailing with its shin kicked up behind; the arms
    # spread loosely out to the sides, swaying against the stride; the chest a little forward
    def stride(front_right, reach, trail, arms_out, sway):
        f = leg(-reach, 0, 24)
        b = leg(trail, 0, 82)
        ra = arm(-8 * sway, 0, arms_out, -12)
        la = mirror(arm(8 * sway, 0, arms_out, -12))
        return p(rightLeg=f if front_right else b, leftLeg=mirror(b) if front_right else mirror(f), rightArm=ra, leftArm=la,
                 body={"yaw": 7 * sway, "bend": 8})
    a = stride(True, 78, 42, 52, 1)
    b = stride(False, 78, 42, 52, -1)
    write_loop("ito_sky_run", 16, [(0, "inoutsine", a), (8, "inoutsine", b), (16, "inoutsine", a)],
               "Ito Ito: Sky Road: running through the air in long bounds, the arms spread loosely, carried by strings.")
    # faster and longer: the steps drawn out like a skater's, the arms swept back and out
    def skate(front_right, sway):
        f = leg(-96, 0, 12)
        b = leg(58, 0, 64)
        ra = arm(26 - 6 * sway, 0, 40, -6)
        la = mirror(arm(26 + 6 * sway, 0, 40, -6))
        return p(rightLeg=f if front_right else b, leftLeg=mirror(b) if front_right else mirror(f), rightArm=ra, leftArm=la,
                 body={"yaw": 5 * sway, "bend": 12})
    a = skate(True, 1)
    b = skate(False, -1)
    write_loop("ito_sky_sprint", 10, [(0, "inoutsine", a), (5, "inoutsine", b), (10, "inoutsine", a)],
               "Ito Ito: Sky Road at speed: an aggressive sprint through the sky, the steps long as a skater's.")
    # hanging at rest on the strings: upright, one leg slightly bent, the arms loose at the sides, swaying a little
    h0 = p(rightLeg=leg(-12, 0, 30), leftLeg=mirror(leg(4, 0, 6)), rightArm=arm(4, 0, 14, -8), leftArm=mirror(arm(4, 0, 14, -8)),
           body={"roll": 1.5, "bend": 0})
    h1 = p(rightLeg=leg(-16, 0, 36), leftLeg=mirror(leg(2, 0, 4)), rightArm=arm(2, 0, 18, -10), leftArm=mirror(arm(6, 0, 12, -6)),
           body={"roll": -1.5, "bend": 2})
    write_loop("ito_sky_hover", 40, [(0, "inoutsine", h0), (20, "inoutsine", h1), (40, "inoutsine", h0)],
               "Ito Ito: Sky Road at rest: hanging easy on the strings, one leg slightly bent, the arms loose at the sides.")


def main():
    slash()
    goshiki()
    overheat()
    birdcage()
    knight()
    sky()
    names = sorted(n for n in WRITTEN if n.startswith("ito_"))
    java_poses(names, "ItoPoses", "tools/ito_anims.py", "the String fruit's animations' keyframes, for the strings on the fist")
    print(f"wrote {len(names)} Ito animations and their poses")


if __name__ == "__main__":
    main()
