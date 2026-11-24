#!/usr/bin/env python3
"""3.5.5: the Mochi Mochi no Mi's own animations (Charlotte Katakuri's way: a still, upright fighter whose arms do the work, the mochi
stretching out of them: a straight punch from the shoulder, one arm then the other; Mogura's thrust from a deep draw; the arms raised
high and slammed down for Kaku Mochi; the forearm cocked back swelling, then punched out for Yaki Mochi; the overhead swing of the club;
the arms opened wide for the Anemone's rings; the palms pressed down to the ground for the Awakening, swept forward for the wave,
clapped together for the walls, raised for the rain and the dome, pointed at the target for the donuts' fists).

Writes src/main/resources/assets/hexhaki/player_animation/mochi_<move>.json and client/npc/MochiPoses (StylePoser samples them). Every
strike lands on the tick server/MochiMoves lands it (a punch on 3, Kaku Mochi's slam on 8, the club's on 7, the walls shut on 14). No
clip keys a leg (but Yanagi Mochi's own right leg, 3.6.3), the head, nor the body's place or pitch (tools/gura_anims.write_parts: the arms
and the upper body only); no stance.

Usage: python3 tools/mochi_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gura_anims import ARMS, REST, tremble, write_parts  # noqa: E402
from style_anims import WRITTEN, arm, java_poses, leg, mirror, p  # noqa: E402

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


# ------------------------------------------------------------------------------------------------------ RMB: Mochi Tsuki

# Mogura held low in both hands, its head forward: the right hand back by the hip on its shaft, the left forward guiding it
DRAWN = p(rightArm=arm(-46, -24, 10, -70), leftArm=arm(-72, 34, -8, -22), body={"yaw": -26, "bend": 6})


def tsuki():
    # held: drawn back deep behind Mogura, the arm twisting itself up round the shaft (it trembles as the coils tighten)
    beats = [(0, "linear", REST), (5, "outsine", DRAWN)]
    beats += tremble(DRAWN, 7, 70, 4, lambda b, s: p(b, rightArm={"pitch": -46 + 1.4 * s, "roll": 10 + 1.2 * s}, body={"yaw": -26 - s}))
    write_parts("mochi_tsuki_charge", 70, beats,
                "Mochi Mochi: Mochi Tsuki held: drawn back behind the trident Mogura, the arm coiling up round its shaft.", ARMS)
    # let go: the whole body behind the thrust, both arms driving the trident straight out (the drill of mochi on tick 2)
    thrust = p(rightArm=arm(-88, 8, 4, -6), leftArm=arm(-84, -22, -4, -14), body={"yaw": 20, "bend": 10})
    write_parts("mochi_tsuki_thrust", 14, [
        (0, "constant", DRAWN),
        (2, "outexpo", thrust),
        (7, "outsine", p(thrust, rightArm={"pitch": -84}, body={"bend": 8})),
        (14, "inoutsine", REST),
    ], "Mochi Mochi: Mochi Tsuki let go: the trident thrust straight out, a drill of mochi piercing the line ahead.", ARMS)


# ------------------------------------------------------------------------------------------------------ Z: Yanagi Mochi

def write_leg_parts(name, duration, beats, description, parts):
    """3.6.3: as gura_anims.write_parts, but the right leg may be keyed (Yanagi Mochi's: the move is that leg's); never the head."""
    from gura_anims import BODY, spine  # noqa: E402
    from style_anims import OUT as ANIM_DIR, PARTS  # noqa: E402
    import json
    assert "head" not in parts and "leftLeg" not in parts, name + ": the head and the other leg are the player's own"
    moves = []
    for tick, easing, q in beats:
        q = spine(q)
        for part in parts:
            values = q.get(part, {})
            for channel in PARTS[part]:
                if part == "body" and channel not in BODY:
                    continue
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.6.3", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + 3, "isLoop": False, "returnTick": 0,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    with open(os.path.join(ANIM_DIR, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def yanagi():
    # 3.6.3 (the owner: "Z Move should lift the leg of the player and turn it into the many legs"): the right leg lifted high in front
    # (tick 3), then it is the tendrils (client/mochi/MochiFx hides the leg while it is: from tick 3 to 19, drawn from the hip), the
    # upper body leaning back over it, the arms out low for balance, shifting with each stamp (every 4 ticks); the leg comes back raised
    # and is set down by tick 22
    lifted = leg(-78, 4, 34)
    lean = p(rightArm=arm(10, 0, 34, -10), leftArm=mirror(arm(10, 0, 34, -10)), rightLeg=lifted, body={"bend": -8})
    beats = [(0, "linear", REST), (3, "outsine", lean)]
    beats += tremble(lean, 4, 20, 4, lambda b, s: p(b, rightArm={"roll": 34 + 4 * s}, leftArm={"roll": -34 + 4 * s},
                                                     body={"roll": 2.5 * s, "bend": -8 - s}))
    beats += [(22, "inoutsine", REST)]
    write_leg_parts("mochi_yanagi", 22, beats,
                    "Mochi Mochi: Yanagi Mochi: the right leg lifted and turned into tendrils of mochi that stamp the ground ahead.",
                    ("body", "torso", "rightArm", "leftArm", "rightLeg"))


# ------------------------------------------------------------------------------------------------------ X: Kaku Mochi

def kaku():
    # both arms swung up high over the head (the square blocks rising), then slammed straight down in front on tick 8
    # (raised by the sides, never up through the face)
    raised = p(rightArm=arm(-12, 0, 158, -16), leftArm=mirror(arm(-12, 0, 158, -16)), body={"bend": -10})
    slammed = p(rightArm=arm(-52, 6, 8, -6), leftArm=mirror(arm(-52, 6, 8, -6)), body={"bend": 22})
    write_parts("mochi_kaku", 20, [
        (0, "linear", REST),
        (4, "outback", raised),
        (6, "linear", p(raised, rightArm={"roll": 164}, leftArm={"roll": -164})),
        (8, "outexpo", slammed),
        (13, "outsine", p(slammed, body={"bend": 20})),
        (20, "inoutsine", REST),
    ], "Mochi Mochi: Kaku Mochi: both arms raised high and slammed down, square blocks of mochi crushing what is ahead.", ARMS)


# ------------------------------------------------------------------------------------------------------ C: Yaki Mochi

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


# ------------------------------------------------------------------------------------------------------ V: Zan Giri Mochi's club

def club():
    # the wheel unrolled: the right arm swung up behind and over the head (the spiked club rising), then down on tick 7
    up = p(rightArm=arm(10, 0, 160, -30), leftArm=arm(-50, 0, -20, -30), body={"yaw": -18, "bend": -12})
    down = p(rightArm=arm(-46, -18, 4, -4), leftArm=arm(10, 0, -24, -24), body={"yaw": 16, "bend": 24})
    write_parts("mochi_club", 18, [
        (0, "linear", REST),
        (4, "outback", up),
        (5, "linear", p(up, rightArm={"roll": 166})),
        (7, "outexpo", down),
        (12, "outsine", p(down, body={"bend": 22})),
        (18, "inoutsine", REST),
    ], "Mochi Mochi: Zan Giri Mochi: a giant spiked club of mochi swung over the head and slammed down.", ARMS)


# ------------------------------------------------------------------------------------------------------ B: Mochi Anemone

OPEN = p(rightArm=arm(-30, 0, 70, -14), leftArm=mirror(arm(-30, 0, 70, -14)), body={"bend": -6})


def anemone():
    # held: the arms opened wide and low at the sides, palms out, the rings floating round and arms of mochi punching out of them;
    # the arms flex with the punches (every 4 ticks)
    beats = [(0, "linear", REST), (5, "outback", OPEN)]
    beats += tremble(OPEN, 8, 100, 4, lambda b, s: p(b, rightArm={"roll": 70 + 5 * s, "bend": -14 - 6 * s},
                                                      leftArm={"roll": -70 + 5 * s, "bend": -14 + 6 * s}, body={"yaw": 3 * s}))
    write_parts("mochi_anemone", 100, beats,
                "Mochi Mochi: Mochi Anemone held: the arms opened wide, rings of mochi floating round, arms punching out of them.", ARMS)


# ------------------------------------------------------------------------------------------------------ N: Sticky Mochi

def throw():
    # an overhand throw of the sticky glob: the hand back by the ear, then flung out forward on tick 2
    back = p(rightArm=arm(-20, 0, 110, -90), leftArm=arm(-50, 0, -16, -26), body={"yaw": -20, "bend": -6})
    flung = p(rightArm=arm(-70, -10, 8, -6), leftArm=arm(14, 0, -18, -24), body={"yaw": 16, "bend": 12})
    write_parts("mochi_throw", 10, [
        (0, "linear", REST),
        (1, "insine", back),
        (2, "outexpo", flung),
        (5, "outsine", p(flung, rightArm={"pitch": -56})),
        (10, "inoutsine", REST),
    ], "Mochi Mochi: Sticky Mochi: an overhand throw, a glob of sticky mochi flung out.", ARMS)


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


# awakened Z: Nagare Mochi
def wave():
    # both hands swept up from low to high in front (the wave rising), then pushed out forward as it rolls away
    low = p(rightArm=arm(-10, 10, 18, -40), leftArm=mirror(arm(-10, 10, 18, -40)), body={"bend": 14})
    pushed = p(rightArm=arm(-100, 14, 6, -4), leftArm=mirror(arm(-100, 14, 6, -4)), body={"bend": -4})
    write_parts("mochi_wave", 16, [
        (0, "linear", REST),
        (3, "outsine", low),
        (7, "outexpo", pushed),
        (11, "outsine", p(pushed, rightArm={"pitch": -94}, leftArm={"pitch": -94})),
        (16, "inoutsine", REST),
    ], "Mochi Mochi: Nagare Mochi: the hands swept up and pushed out, a great wave of mochi rolling forward.", ARMS)


# awakened X: Kagami Mochi
def walls():
    # the arms opened wide to either side (the walls rising), then clapped together in front as they shut on tick 14
    side = p(rightArm=arm(-20, 0, 80, -4), leftArm=mirror(arm(-20, 0, 80, -4)), body={"bend": -4})
    wide = p(rightArm=arm(-80, 0, 72, -4), leftArm=mirror(arm(-80, 0, 72, -4)), body={"bend": -4})
    clap = p(rightArm=arm(-88, 30, 8, -8), leftArm=mirror(arm(-88, 30, 8, -8)), body={"bend": 8})
    write_parts("mochi_walls", 20, [
        (0, "linear", REST),
        (4, "outback", side),
        (10, "inoutsine", wide),
        (12, "linear", p(wide, rightArm={"roll": 78}, leftArm={"roll": -78})),
        (14, "outexpo", clap),
        (17, "outsine", clap),
        (20, "inoutsine", REST),
    ], "Mochi Mochi: Kagami Mochi: the arms opened wide, then clapped together, two walls of mochi slamming shut.", ARMS)


# awakened C: Amadare Mochi
def rain():
    # the right hand raised to the sky (the mass of mochi gathering overhead), then brought down pointing at the place it rains on
    sky = p(rightArm=arm(-8, 0, 158, -6), leftArm=arm(-12, 0, -18, -20), body={"bend": -8})
    point = p(rightArm=arm(-78, -10, 6, -2), leftArm=arm(10, 0, -18, -20), body={"yaw": 12, "bend": 6})
    write_parts("mochi_rain", 18, [
        (0, "linear", REST),
        (3, "outback", sky),
        (7, "linear", p(sky, rightArm={"roll": 164})),
        (9, "outexpo", point),
        (14, "outsine", point),
        (18, "inoutsine", REST),
    ], "Mochi Mochi: Amadare Mochi: the hand raised to the sky, then brought down, tendrils of mochi raining on the place.", ARMS)


# awakened V: Muso Donuts and Chikara Mochi
def donuts():
    # the right arm pointed at the target, the left drawn back; a jab of the pointed fist with each giant fist (every 4 ticks from 6)
    aim = p(rightArm=arm(-90, -8, 4, -4), leftArm=arm(18, 0, -20, -36), body={"yaw": 18, "bend": 4})
    beats = [(0, "linear", REST), (4, "outsine", aim)]
    for k in range(5):
        t = 6 + 4 * k
        beats += [(t, "outexpo", p(aim, rightArm={"pitch": -94, "bend": 0}, body={"bend": 7})), (t + 2, "outsine", aim)]
    beats += [(32, "inoutsine", REST)]
    write_parts("mochi_donuts", 32, beats,
                "Mochi Mochi: Muso Donuts: pointing at the target as giant fists punch out of the rings of mochi, one after another.", ARMS)


# awakened B: Mochi Ginkaku
def dome():
    # both arms raised out wide and brought together overhead (the dome rising over the place), held a moment
    out = p(rightArm=arm(-30, 0, 90, -10), leftArm=mirror(arm(-30, 0, 90, -10)), body={"bend": -6})
    over = p(rightArm=arm(-10, 0, 150, -34), leftArm=mirror(arm(-10, 0, 150, -34)), body={"bend": -10})
    write_parts("mochi_dome", 18, [
        (0, "linear", REST),
        (4, "outsine", out),
        (8, "outback", over),
        (13, "outsine", over),
        (18, "inoutsine", REST),
    ], "Mochi Mochi: Mochi Ginkaku: the arms raised and brought together overhead, a dome of mochi rising over the place.", ARMS)


def main():
    punch()
    lotus()
    muddle()
    chestnut()
    tsuki()
    yanagi()
    kaku()
    yaki()
    club()
    anemone()
    throw()
    awaken()
    wave()
    walls()
    rain()
    donuts()
    dome()
    names = sorted(n for n in WRITTEN if n.startswith("mochi_"))
    java_poses(names, "MochiPoses", "tools/mochi_anims.py", "the Mochi fruit's animations' keyframes")
    print(f"wrote {len(names)} Mochi animations and their poses")


if __name__ == "__main__":
    main()
