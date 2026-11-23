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


# 3.5.0 (the owner: "I don't want the player to just be attached to two strings in the air at all times. I want it to like do break
# dances in the fucking sky ... jump from rope to rope ... backflip to another string, just skip to another one, jump from one to the
# other, and like swing, just like grab a string from the sky and swing to the next line ... If the player is just, like, hanging in
# midair, then we just make the player, like, grabbing onto a string with one hand ... animate it a little bit more"): the hang and the
# tricks client/ito/SkyRoad plays between strides. The tricks turn the whole body (its pitch: the flip, the swing), so they key it.
TRICK_PARTS = dict(SKY_PARTS, body=("pitch", "yaw", "roll", "bend"))
# the right hand straight up, gripping a string over the head: raised out by the side (from the run's arms out it never crosses the face)
UP_RIGHT = arm(-6, 0, 152, -4)


def write_trick(name, duration, beats, description, loop=False):
    moves = []
    for tick, easing, q in beats:
        for part, channels in TRICK_PARTS.items():
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.5.0", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + 3, "isLoop": loop, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(ANIM_DIR, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def tricks():
    run = p(rightLeg=leg(-40, 0, 30), leftLeg=mirror(leg(30, 0, 60)), rightArm=arm(0, 0, 50, -12), leftArm=mirror(arm(0, 0, 50, -12)),
            body={"bend": 8})
    # 3.5.2: the backflip and the leap play as a swing lets go of its string or a leap leaves the hang: they start from the grip (the right
    # hand still up on the string, the left reaching ahead, the knees bent)
    letgo = p(rightArm=UP_RIGHT, leftArm=mirror(arm(-64, 0, 28, -16)), rightLeg=leg(-24, 0, 42), leftLeg=mirror(leg(-12, 0, 30)),
              body={"bend": 2})
    # the hang: one hand up on a string, the other arm loose and swinging, the legs dangling and pedalling slowly, the body turning a
    # little on the string and swaying
    def hang(k):
        return p(rightArm=UP_RIGHT, leftArm=mirror(arm(10 * k, 0, 14 + 4 * k, -16 + 6 * k)),
                 rightLeg=leg(-14 * k, 0, 24 + 8 * k), leftLeg=mirror(leg(10 * k, 0, 10 - 6 * k)),
                 body={"yaw": 7 * k, "roll": 2.5 * k, "bend": 2})
    write_trick("ito_sky_hang", 48, [(0, "inoutsine", hang(1)), (12, "inoutsine", hang(0)), (24, "inoutsine", hang(-1)),
                                     (36, "inoutsine", hang(0)), (48, "inoutsine", hang(1))],
                "Ito Ito: Sky Road at rest: hanging by one hand from a string, the other arm and the legs swinging easy.", loop=True)
    # the backflip to another string: tucked, a whole turn back, opening with the right hand thrown up to catch the next string; the
    # turn stepped back to 0 on the last tick (360 is 0 seen: the fade out never unwinds it)
    tuck = dict(rightLeg=leg(-112, 0, 118), leftLeg=mirror(leg(-108, 0, 122)), rightArm=arm(-46, 0, 14, -64), leftArm=mirror(arm(-46, 0, 14, -64)))
    write_trick("ito_sky_backflip", 14, [
        (0, "linear", letgo),
        (2, "outsine", p(tuck, body={"pitch": 50, "bend": 14})),
        (5, "linear", p(tuck, body={"pitch": 190, "bend": 16})),
        (8, "linear", p(tuck, body={"pitch": 300, "bend": 10})),
        (11, "outsine", p(rightLeg=leg(-8, 0, 14), leftLeg=mirror(leg(4, 0, 8)), rightArm=UP_RIGHT, leftArm=mirror(arm(-10, 0, 46, -10)),
                          body={"pitch": 360, "bend": 0})),
        (13, "linear", p(rightLeg=leg(-8, 0, 14), leftLeg=mirror(leg(4, 0, 8)), rightArm=UP_RIGHT, leftArm=mirror(arm(-10, 0, 46, -10)),
                         body={"pitch": 360})),
        (14, "constant", p(rightLeg=leg(-8, 0, 14), leftLeg=mirror(leg(4, 0, 8)), rightArm=UP_RIGHT, leftArm=mirror(arm(-10, 0, 46, -10)),
                           body={"pitch": 0})),
    ], "Ito Ito: Sky Road: a tucked backflip from one string to the next, the hand thrown up to catch it.")
    # the leap: a flying split from string to string, the arms flung wide, the body arched; then gathered, the hand up for the next
    split = p(rightLeg=leg(-96, 0, 0), leftLeg=mirror(leg(64, 0, 6)), rightArm=arm(-20, 0, 112, -6), leftArm=mirror(arm(-20, 0, 112, -6)),
              body={"bend": -10})
    write_trick("ito_sky_leap", 12, [
        (0, "linear", letgo), (3, "outback", split), (8, "linear", p(split, body={"bend": -12})),
        (12, "inoutsine", p(rightLeg=leg(-20, 0, 30), leftLeg=mirror(leg(10, 0, 20)), rightArm=UP_RIGHT, leftArm=mirror(arm(-20, 0, 40, -10)),
                            body={"bend": 4})),
    ], "Ito Ito: Sky Road: a flying split leap from one string to the next.")
    # the skip: a knee driven high, the other foot kicked up behind, the opposite arm pumped up; then the other way round
    def skip(k):
        right = k > 0
        high = leg(-102, 0, 96)
        back = leg(28, 0, 104)
        pump = arm(-18, 0, 138, -12)
        low = arm(36, 0, 18, -20)
        return p(rightLeg=high if right else back, leftLeg=mirror(back if right else high), rightArm=low if right else pump,
                 leftArm=mirror(pump if right else low), body={"roll": 5 * k, "yaw": -8 * k, "bend": -4})
    write_trick("ito_sky_skip", 10, [(0, "linear", run), (3, "outback", skip(1)), (6, "inoutsine", skip(-1)), (10, "inoutsine", run)],
                "Ito Ito: Sky Road: a skip from string to string, a knee high, the other foot kicked up behind.")
    # the swing: the right hand catches a string overhead and the body swings under it like a pendulum (back, then far forward, the feet
    # leading), lets go at the top of the swing and flies on to the next
    grip = dict(rightArm=UP_RIGHT, leftArm=mirror(arm(-10, 0, 70, -10)))
    write_trick("ito_sky_swing", 20, [
        (0, "linear", run),
        (3, "outsine", p(grip, rightLeg=leg(10, 0, 30), leftLeg=mirror(leg(16, 0, 40)), body={"pitch": -28, "bend": -6})),
        (10, "inoutsine", p(grip, rightLeg=leg(-40, 0, 8), leftLeg=mirror(leg(-34, 0, 12)), body={"pitch": 36, "bend": 6})),
        (14, "outsine", p(grip, rightLeg=leg(-58, 0, 10), leftLeg=mirror(leg(-50, 0, 16)), body={"pitch": 44, "bend": 8})),
        (17, "inoutsine", p(rightArm=arm(-60, 0, 40, -10), leftArm=mirror(arm(-40, 0, 56, -10)), rightLeg=leg(-30, 0, 30),
                            leftLeg=mirror(leg(-10, 0, 40)), body={"pitch": 12, "bend": 4})),
        (20, "inoutsine", run),
    ], "Ito Ito: Sky Road: a string caught overhead, a swing under it like a pendulum, let go at the top to fly on.")


# 3.5.2 (the owner: "Sky walk needs a rework, I dont want 2 constant lines holding me up I wanna climb up like stairs when I hold Space,
# I wanna jump from 1 string to another further away, I want to swing like spiderman"): the swing on one string (client/ito/SkyRoad turns
# the whole body along the string round the hand, so the clip keeps the body upright: the hand straight up on the string, the left hand
# reaching ahead for the next one, the legs pumping), the climb up steps of string like stairs (a step every 5 ticks: the knee driven up
# onto the next step, the other leg pushing off, the arms swinging loose), and the drop (Shift: arms flung up, legs dangling).
def swing_climb_drop():
    def swinging(k):
        return p(rightArm=UP_RIGHT, leftArm=mirror(arm(-70 - 8 * k, 0, 26 + 4 * k, -14)),
                 rightLeg=leg(-26 * k if k > 0 else 14, 0, 44 if k > 0 else 22), leftLeg=mirror(leg(-16 if k > 0 else 20, 0, 30 if k > 0 else 36)),
                 body={"bend": 4 * k, "roll": 2 * k})
    write_loop("ito_sky_swinging", 24, [(0, "inoutsine", swinging(1)), (12, "inoutsine", swinging(-1)), (24, "inoutsine", swinging(1))],
               "Ito Ito: Sky Road: swinging on one string like a pendulum, the hand straight up on it, the other reaching for the next.")

    def stepping(right):
        up = leg(-78, 0, 92)
        push = leg(14, 0, 8)
        back = arm(30, 0, 18, -8)
        front = arm(-46, 0, 20, -26)
        k = 1 if right else -1
        return p(rightLeg=up if right else push, leftLeg=mirror(push if right else up), rightArm=back if right else front,
                 leftArm=mirror(front if right else back), body={"yaw": 6 * k, "bend": 10})

    def planting(right):
        down = leg(-26, 0, 30)
        lift = leg(26, 0, 50)
        k = 1 if right else -1
        return p(rightLeg=down if right else lift, leftLeg=mirror(lift if right else down), rightArm=arm(14, 0, 18, -10),
                 leftArm=mirror(arm(-14, 0, 18, -16)) if right else mirror(arm(14, 0, 18, -10)), body={"yaw": 3 * k, "bend": 12})
    write_loop("ito_sky_climb", 10, [(0, "outsine", stepping(True)), (3, "inoutsine", planting(True)), (5, "outsine", stepping(False)),
                                     (8, "inoutsine", planting(False)), (10, "outsine", stepping(True))],
               "Ito Ito: Sky Road: climbing up steps of string like stairs, a knee driven up onto each step.")

    def dropping(k):
        return p(rightArm=arm(-12 - 6 * k, 0, 128 + 8 * k, -10), leftArm=mirror(arm(-12 + 6 * k, 0, 128 - 8 * k, -10)),
                 rightLeg=leg(-10 - 6 * k, 0, 18 + 8 * k), leftLeg=mirror(leg(8 + 6 * k, 0, 8)), body={"bend": -5})
    write_loop("ito_sky_drop", 16, [(0, "inoutsine", dropping(1)), (8, "inoutsine", dropping(-1)), (16, "inoutsine", dropping(1))],
               "Ito Ito: Sky Road: dropping through the air, the arms flung up, the legs dangling.")


def main():
    slash()
    goshiki()
    overheat()
    birdcage()
    knight()
    sky()
    tricks()
    swing_climb_drop()
    names = sorted(n for n in WRITTEN if n.startswith("ito_"))
    java_poses(names, "ItoPoses", "tools/ito_anims.py", "the String fruit's animations' keyframes, for the strings on the fist")
    print(f"wrote {len(names)} Ito animations and their poses")


if __name__ == "__main__":
    main()
