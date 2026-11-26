#!/usr/bin/env python3
"""2.4.7: the Conqueror's Haki charge and releases (the owner: "I want a badass animation for the Conqueror Haki charge / release. the
current one is dog shit."). 2.5.1 (the owner: "only animate the top half of the body above the legs btw, evil gets one animation
(EVIL/Disturbing) and good gets another (Peaceful/in pain) like a hand on the face and then a stare? idk"): the upper body only (the
legs, and the body's height, lean and turn, are left to the game), and a set for each alignment.

The server plays, by the releaser's alignment (server/ConquerorRelease.animate):
  neutral  conqueror_gear5_charge, supreme_conqueror_release, conqueror_gear5_release (over the originals'; anim_polish leaves them)
  good     conqueror_good_charge, conqueror_good_release, conqueror_good_release_partial
  evil     conqueror_evil_charge, conqueror_evil_release, conqueror_evil_release_partial
The charge plays while G is held (the server roots the player; the charge fills in 160 ticks): a build of FULL ticks, then a loop at full
charge (isLoop, returnTick FULL, endTick FULL + LOOP; the poses at both ends the same, every wave whole cycles in the loop). The full
release plays for 56 ticks, a partial one for 48 (the originals' lengths).
  neutral  the power stance: fists pulled in at the waist, then up under the chest, elbows flared, the body hunched over them, the head
           hanging then lifted to glare, the breath quickening, a tremor growing, shudders. Its release: a 2-tick crunch (forearms
           crossed before the chest), then the burst: arms flung wide and back, chest thrown open, head back roaring, held shaking.
  good     peaceful, in pain: a hand pressed over the face, the other arm clutching the stomach, the head bowed into the hand, the
           breath heavy, the hand trembling more as it fills, pained jolts; as it fills the hand slides off the face and the head
           comes up into a level, calm stare, held almost still. Its release: an inhale, then the pressure let out with barely a
           movement: the arms eased out from the sides, the chest open, the stare unmoved; the exhale.
  evil     disturbing: the head drops forward limp, the arms hang like a puppet's; as it fills the head creeps up and over to an
           unnatural tilt, staring sideways, the shoulders hitch, the head jerks, the fingers twitch, faster and faster, the body
           swaying. Its release: the head snaps up and back as the arms are flung wide and high, a shaking laugh (the head bobbing),
           then the head drops limp and the arms fall.

Every beat keys every channel of the upper body (the body's bend only of the body; nothing drifts between beats), one move a beat; the
leans are the body bend (anim_hand's spine rule). Conventions: tools/style_anims.py (arm pitch - raises forward, right arm roll + out,
elbow bend - flexes, head pitch + looks down, body bend + hunches forward).

Usage: python3 tools/conqueror_anims.py      (then judge: tools/anim_preview.py, anim.lint; build.py lints every animation)
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402
from anim_hand import spine  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
CHARGE = "conqueror_gear5_charge"
SUPREME = "supreme_conqueror_release"
PARTIAL = "conqueror_gear5_release"
GOOD = ("conqueror_good_charge", "conqueror_good_release", "conqueror_good_release_partial")
EVIL = ("conqueror_evil_charge", "conqueror_evil_release", "conqueror_evil_release_partial")
# 3.8.2: the Conqueror's Haki clash (server/ConquerorClash): both fighters braced against the other's Haki, looping until it ends
CLASH = "conqueror_clash"
NAMES = (CHARGE, SUPREME, PARTIAL) + GOOD + EVIL + (CLASH,)
# every part a pose may name (the legs and the body's other channels are only ever zero here and never written)
PARTS = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "torso": ("pitch", "yaw", "roll", "bend"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "rightLeg": ("pitch", "yaw", "roll", "bend"),
    "leftLeg": ("pitch", "yaw", "roll", "bend"),
}
# 2.5.1: what the files key: the upper body (the body's bend carries the head and arms; the legs walk or stand as the game has them)
WRITTEN = {
    "body": ("bend",),
    "torso": ("pitch", "yaw", "roll", "bend"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
}
# the charge fills in this many ticks; its loop at full charge lasts LOOP
FULL = 160
LOOP = 40


# ------------------------------------------------------------------------------------------------------------- poses

def arm(pitch=0.0, yaw=0.0, roll=0.0, bend=0.0):
    return {"pitch": pitch, "yaw": yaw, "roll": roll, "bend": bend}


def leg(pitch=0.0, roll=0.0, bend=0.0, yaw=0.0):
    return {"pitch": pitch, "roll": roll, "bend": bend, "yaw": yaw}


def mirror(a):
    return {"pitch": a.get("pitch", 0.0), "yaw": -a.get("yaw", 0.0), "roll": -a.get("roll", 0.0), "bend": a.get("bend", 0.0)}


def pose(right_arm, right_leg=None, body=None, head=None, left_arm=None, left_leg=None, torso=None):
    """A pose from the right side (the left arm mirrored unless given)."""
    right_leg = right_leg or leg()
    return {
        "body": dict(body or {}),
        "torso": dict(torso or {}),
        "head": dict(head or {}),
        "rightArm": dict(right_arm),
        "leftArm": dict(left_arm) if left_arm is not None else mirror(right_arm),
        "rightLeg": dict(right_leg),
        "leftLeg": dict(left_leg) if left_leg is not None else mirror(right_leg),
    }


def channels(q):
    """Every channel of every part, 0 where not given."""
    return {part: {ch: float(q.get(part, {}).get(ch, 0.0)) for ch in chs} for part, chs in PARTS.items()}


def mix(a, b, t):
    """a to b by t (channel by channel)."""
    a, b = channels(a), channels(b)
    return {part: {ch: a[part][ch] + (b[part][ch] - a[part][ch]) * t for ch in chs} for part, chs in PARTS.items()}


def nudge(q, deltas):
    """q with deltas added: {(part, channel): degrees}; "arms" (or "legs") both, the left's roll and yaw mirrored."""
    out = channels(q)
    for (part, ch), value in deltas.items():
        if part == "arms":
            out["rightArm"][ch] += value
            out["leftArm"][ch] += -value if ch in ("roll", "yaw") else value
        elif part == "legs":
            out["rightLeg"][ch] += value
            out["leftLeg"][ch] += -value if ch in ("roll", "yaw") else value
        else:
            out[part][ch] += value
    return out


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def wave(t, freqs):
    """A tremor: a sum of sines (cycles a tick, phase) normalised to about -1..1."""
    return sum(math.sin(2 * math.pi * (f * t + ph)) for f, ph in freqs) / math.sqrt(len(freqs) * 0.5) / 2.0


# whole cycles over the loop (LOOP ticks), all under half a cycle a tick (one keyframe a tick draws them): 3.5 to 8.5 Hz
SHAKE_A = ((13 / LOOP, 0.11), (17 / LOOP, 0.53), (9 / LOOP, 0.29))
SHAKE_B = ((11 / LOOP, 0.71), (16 / LOOP, 0.37), (7 / LOOP, 0.05))
SHAKE_C = ((15 / LOOP, 0.42), (12 / LOOP, 0.83), (8 / LOOP, 0.61))


def phase(t, slow, fast):
    """A wave's phase (cycles) whose rate (cycles a tick) quickens from slow to fast over the build, integrated so it never jumps;
    at full charge fast (whole cycles in the loop when fast x LOOP is whole)."""
    if t <= FULL:
        steps = max(1, int(t * 4))
        return sum((slow + (fast - slow) * smooth((i + 0.5) / steps * t / FULL)) * (t / steps) for i in range(steps))
    return phase(FULL, slow, fast) + fast * (t - FULL)


def shudder(t, at, strength):
    """A jolt: sharp in (1 tick), eased out over 7 ticks."""
    d = t - at
    if d < 0 or d > 8:
        return 0.0
    if d < 1:
        return strength * d
    return strength * (1 - smooth((d - 1) / 7))


def jerk(t, at):
    """A twitch: snapped in within a tick, out over three."""
    d = t - at
    if d < 0 or d > 4:
        return 0.0
    if d < 1:
        return d
    return 1 - smooth((d - 1) / 3)


def charge_beats(pose_at, ease_in=6):
    beats = [(0, "linear", channels({}))]
    for t in range(1, FULL + LOOP + 1):
        beats.append((t, "outquad" if t <= ease_in else "linear", pose_at(t)))
    return beats


# ------------------------------------------------------------------------------------------------------------- neutral
# The power stance, light (just dropped into it) and deep (full charge): the fists pulled in to the waist, then up under the chest, the
# upper arms back, the elbows flared out; the body hunched over the fists, the head held up to look ahead (it glares from under the hunch).
COIL_LIGHT = pose(arm(pitch=14, yaw=10, roll=14, bend=-70), body={"bend": 8}, head={"pitch": 4})
COIL_DEEP = pose(arm(pitch=38, yaw=16, roll=20, bend=-108), body={"bend": 22}, head={"pitch": -16})
INTAKE = nudge(COIL_LIGHT, {("body", "bend"): 4, ("arms", "bend"): -8, ("head", "pitch"): 6})
BREATH_FULL = 2 / LOOP  # two breaths in the loop at full charge
SHUDDERS = ((54, 0.55), (92, 0.75), (128, 0.9))
LOOP_SHUDDER = (FULL + 22, 1.0)


def neutral_charge(t):
    if t <= 3:
        return mix({}, INTAKE, smooth(t / 3))
    if t <= 6:
        return mix(INTAKE, COIL_LIGHT, smooth((t - 3) / 3))
    s = smooth((min(t, FULL) - 6) / (FULL - 6))
    q = mix(COIL_LIGHT, COIL_DEEP, s)
    hang = math.sin(math.pi * min(1.0, (t - 6) / 90.0)) * (1 - s)
    q["head"]["pitch"] += 16 * hang
    b = math.sin(2 * math.pi * phase(t, 1 / 32.0, BREATH_FULL))
    depth = 0.6 + 0.4 * s
    q = nudge(q, {("body", "bend"): 3.2 * b * depth, ("head", "pitch"): -2.0 * b * depth, ("arms", "pitch"): -2.5 * b * depth,
                  ("arms", "roll"): 1.5 * b * depth})
    amp = 0.25 + 0.75 * s if t <= FULL else 1.0
    a, c, e = wave(t, SHAKE_A), wave(t, SHAKE_B), wave(t, SHAKE_C)
    q["rightArm"]["roll"] += 4.5 * amp * a
    q["leftArm"]["roll"] -= 4.5 * amp * c
    q["rightArm"]["bend"] += 4.0 * amp * c
    q["leftArm"]["bend"] += 4.0 * amp * e
    q["head"]["roll"] += 2.6 * amp * a
    q["head"]["yaw"] += 1.8 * amp * c
    q["head"]["pitch"] += 1.6 * amp * e
    jolt = sum(shudder(t, at, k) for at, k in SHUDDERS)
    if t >= FULL:
        jolt += shudder(t, *LOOP_SHUDDER)
    return nudge(q, {("body", "bend"): 6 * jolt, ("head", "pitch"): 9 * jolt, ("arms", "bend"): -8 * jolt, ("arms", "roll"): 4 * jolt})


# its release: the crunch (the forearms crossed before the chest, the body curled over them), the burst (the arms flung wide, down and
# back, the chest thrown open, the head back roaring), the overshoot, the roar held, the exhale
CRUNCH = pose(arm(pitch=-40, yaw=-40, roll=-20, bend=-70), body={"bend": 30}, head={"pitch": 10})
BURST = pose(arm(pitch=30, yaw=0, roll=62, bend=-8), body={"bend": -16}, head={"pitch": -36})
OVER = nudge(BURST, {("arms", "roll"): 8, ("arms", "pitch"): 6, ("body", "bend"): -4, ("head", "pitch"): -6})
ROAR = nudge(BURST, {("arms", "roll"): -5, ("arms", "pitch"): -3, ("arms", "bend"): -8, ("body", "bend"): 3, ("head", "pitch"): 5})
EXHALE = pose(arm(pitch=4, yaw=0, roll=12, bend=-24), body={"bend": 8}, head={"pitch": 8})


def neutral_release(length, reach, hold):
    start = neutral_charge(FULL)
    burst = mix(EXHALE, BURST, reach)
    over = mix(EXHALE, OVER, reach)
    roar = mix(EXHALE, ROAR, reach)
    crunch = mix(start, CRUNCH, 0.55 + 0.45 * reach)
    beats = [(0, "linear", start), (2, "inquad", crunch), (5, "outexpo", burst), (7, "outback", over)]
    for t in range(8, hold + 1):
        k = reach * (1 - (t - 8) / max(1, hold - 8) * 0.6)
        a, c, e = wave(t, SHAKE_A), wave(t, SHAKE_B), wave(t, SHAKE_C)
        beats.append((t, "linear", nudge(roar, {("rightArm", "roll"): 3.0 * k * a, ("leftArm", "roll"): -3.0 * k * c,
                                                ("rightArm", "pitch"): 2.0 * k * c, ("leftArm", "pitch"): 2.0 * k * e,
                                                ("head", "roll"): 2.4 * k * e, ("head", "pitch"): 1.6 * k * a})))
    beats.append((round(hold + (length - hold) * 0.45), "inoutsine", EXHALE))
    beats.append((length, "inoutsine", channels({})))
    return beats


# ------------------------------------------------------------------------------------------------------------- good
# In pain, holding it back: the right hand pressed over the face (solved on the model: the hand before the face, no part in the head),
# the left arm across the stomach, the head bowed into the hand; deeper: doubled over a little more, the hand pressed harder.
FACE_HAND = arm(pitch=-80, yaw=-45, roll=0, bend=-70)
STOMACH = mirror(arm(pitch=-30, yaw=-30, roll=-8, bend=-70))
PAIN_LIGHT = pose(FACE_HAND, body={"bend": 8}, head={"pitch": 16}, left_arm=STOMACH)
PAIN_DEEP = pose(arm(pitch=-84, yaw=-44, roll=4, bend=-74), body={"bend": 18}, head={"pitch": 22},
                 left_arm=mirror(arm(pitch=-38, yaw=-34, roll=-6, bend=-92)))
# the stare: upright, the arms loose at the sides, the chin level, still
STARE = pose(arm(pitch=2, yaw=0, roll=5, bend=-8), body={"bend": 2}, head={"pitch": -3})
GOOD_JOLTS = ((58, 0.5), (98, 0.7), (124, 0.85))
REVEAL = 140  # the hand starts down off the face; the stare by FULL


def good_charge(t):
    if t <= 8:
        return mix({}, PAIN_LIGHT, smooth(t / 8))
    if t <= REVEAL:
        s = smooth((t - 8) / (REVEAL - 8))
        q = mix(PAIN_LIGHT, PAIN_DEEP, s)
        # heavy breathing, slower and deeper than the neutral's
        b = math.sin(2 * math.pi * phase(t, 1 / 36.0, 1 / 22.0))
        q = nudge(q, {("body", "bend"): 3.5 * b, ("head", "pitch"): 1.5 * b, ("leftArm", "bend"): -3 * b})
        # the hand on the face and the head tremble more and more as it fills
        amp = 0.3 + 0.9 * s
        a, c, e = wave(t, SHAKE_A), wave(t, SHAKE_B), wave(t, SHAKE_C)
        q["rightArm"]["roll"] += 2.4 * amp * a
        q["rightArm"]["pitch"] += 1.8 * amp * c
        q["rightArm"]["bend"] += 2.0 * amp * e
        q["head"]["roll"] += 1.6 * amp * c
        q["head"]["yaw"] += 1.2 * amp * e
        jolt = sum(shudder(t, at, k) for at, k in GOOD_JOLTS)
        # a pained jolt: doubled over (the bend carries the head and the hand on it together), the left arm clutching tighter
        return nudge(q, {("body", "bend"): 8 * jolt, ("head", "pitch"): 2 * jolt, ("leftArm", "bend"): -10 * jolt,
                         ("rightArm", "pitch"): 2 * jolt})
    if t <= FULL:
        # the reveal: the hand slides down off the face, the head comes up into the stare
        return mix(good_charge(REVEAL), stare(t), smooth((t - REVEAL) / (FULL - REVEAL)))
    return stare(t)


def stare(t):
    """The stare at full charge: one slow breath a loop, the fingers' faint tremor, the head unmoving."""
    b = math.sin(2 * math.pi * (t - FULL) / LOOP)
    a, c = wave(t, SHAKE_A), wave(t, SHAKE_B)
    return nudge(STARE, {("body", "bend"): 1.2 * b, ("arms", "pitch"): -0.8 * b, ("rightArm", "roll"): 0.6 * a,
                         ("leftArm", "roll"): -0.6 * c})


# its release: an inhale, then the pressure let out with barely a movement: the open arms eased out from the sides, the chest open,
# the stare unmoved; held with a faint tremor; the exhale
GOOD_INHALE = nudge(STARE, {("body", "bend"): -4, ("arms", "roll"): 4, ("head", "pitch"): -2})
GOOD_PUSH = pose(arm(pitch=14, yaw=0, roll=26, bend=-4), body={"bend": -6}, head={"pitch": -6})
GOOD_SETTLE = pose(arm(pitch=2, yaw=0, roll=6, bend=-10), body={"bend": 4}, head={"pitch": 2})


def good_release(length, reach, hold, start):
    push = mix(STARE, GOOD_PUSH, reach)
    beats = [(0, "linear", start)]
    if start is STARE:
        beats.append((3, "insine", GOOD_INHALE))
    else:
        # a partial one: the hand snaps down off the face into the stare first
        beats.append((3, "outexpo", mix(GOOD_INHALE, STARE, 0.3)))
    beats.append((6, "outexpo", push))
    for t in range(7, hold + 1, 2):
        a, c = wave(t, SHAKE_A), wave(t, SHAKE_B)
        k = reach * (1 - (t - 7) / max(1, hold - 7) * 0.5)
        beats.append((t, "linear", nudge(push, {("rightArm", "roll"): 1.0 * k * a, ("leftArm", "roll"): -1.0 * k * c})))
    beats.append((round(hold + (length - hold) * 0.5), "inoutsine", GOOD_SETTLE))
    beats.append((length, "inoutsine", channels({})))
    return beats


# ------------------------------------------------------------------------------------------------------------- evil
# Limp, like a puppet whose strings were cut: the head hanging forward, the arms dangling a little forward; at full charge the head
# risen and over at an unnatural tilt, staring sideways, the arms a little out, the hands open.
LIMP = pose(arm(pitch=-12, yaw=0, roll=3, bend=-14), body={"bend": 22}, head={"pitch": 42})
TILT = pose(arm(pitch=-6, yaw=4, roll=14, bend=-24), body={"bend": 14}, head={"pitch": 6, "roll": 40, "yaw": 8})
# the twitches as it fills, more and more often (tick, kind, side), and those of the loop at full (ending inside it)
EVIL_TWITCHES = ((30, "head", 1), (52, "shoulder", -1), (70, "head", -1), (84, "shoulder", 1), (97, "head", 1), (108, "fingers", 1),
                 (118, "head", -1), (127, "shoulder", -1), (135, "head", 1), (142, "shoulder", 1), (149, "head", -1),
                 (155, "fingers", -1), (FULL + 9, "head", 1), (FULL + 18, "shoulder", -1), (FULL + 26, "fingers", 1),
                 (FULL + 31, "head", -1))


def evil_charge(t):
    if t <= 8:
        return mix({}, LIMP, smooth(t / 8))
    s = smooth((min(t, FULL) - 8) / (FULL - 8)) ** 1.3
    q = mix(LIMP, TILT, s)
    # a slow sway, side to side (the torso turning, the head with it a little)
    w = math.sin(2 * math.pi * phase(t, 1 / 60.0, 1 / LOOP))
    q = nudge(q, {("torso", "yaw"): 5 * w, ("head", "yaw"): 3 * w, ("arms", "pitch"): 2 * w})
    # a faint shiver in the dangling arms
    a, c = wave(t, SHAKE_A), wave(t, SHAKE_B)
    q["rightArm"]["roll"] += 1.2 * a
    q["leftArm"]["roll"] -= 1.2 * c
    for at, kind, side in EVIL_TWITCHES:
        j = jerk(t, at)
        if j <= 0:
            continue
        if kind == "head":
            q = nudge(q, {("head", "roll"): 13 * side * j, ("head", "pitch"): -7 * j, ("head", "yaw"): 5 * side * j})
        elif kind == "shoulder":
            part = "rightArm" if side > 0 else "leftArm"
            q = nudge(q, {(part, "pitch"): -16 * j, (part, "roll"): (6 if side > 0 else -6) * j, ("torso", "roll"): 3 * side * j})
        else:
            q = nudge(q, {("arms", "roll"): 6 * j, ("arms", "bend"): -12 * j})
    return q


# its release: the head snaps up and back as the arms are flung wide and high (the back arched), a shaking laugh (the head bobbing, the
# shoulders with it), then the head drops limp and the arms fall
EVIL_SNAP = pose(arm(pitch=-20, yaw=0, roll=100, bend=-6), body={"bend": -18}, head={"pitch": -30})
EVIL_WIDE = pose(arm(pitch=-24, yaw=0, roll=110, bend=-4), body={"bend": -20}, head={"pitch": -40})
EVIL_LAUGH_PERIOD = 7.0


def evil_release(length, reach, laugh_end, drop):
    snap = mix(LIMP, EVIL_SNAP, reach)
    wide = mix(LIMP, EVIL_WIDE, reach)
    beats = [(0, "linear", evil_charge(FULL)), (2, "outexpo", snap), (4, "outback", wide)]
    for t in range(5, laugh_end + 1):
        k = reach * (1 - 0.4 * (t - 5) / max(1, laugh_end - 5))
        bob = math.sin(2 * math.pi * (t - 5) / EVIL_LAUGH_PERIOD)
        beats.append((t, "linear", nudge(wide, {("head", "pitch"): 7 * k * bob, ("arms", "pitch"): -4 * k * bob,
                                                ("body", "bend"): 2 * k * bob, ("arms", "roll"): -1.5 * k * bob})))
    # the head drops limp, the arms fall
    beats.append((drop, "inquad", LIMP))
    beats.append((drop + 5, "outsine", nudge(LIMP, {("head", "pitch"): 4, ("body", "bend"): 2})))
    beats.append((length, "inoutsine", channels({})))
    return beats


# ------------------------------------------------------------------------------------------------------------- the files

# ------------------------------------------------------------------------------------------------------------- the clash
# 3.8.2: braced against the other's Haki: the body leaned into it, the fists clenched low and a little back, the elbows bent and flared,
# the chest pushed forward, the head held level glaring ahead; straining: a tremor through the arms and head, the breath heaving, a surge
# of effort leaning in harder once a loop (every wave whole cycles in the loop; the ends the same).
CLASH_IN = 6
BRACE = pose(arm(pitch=4, yaw=4, roll=20, bend=-30), body={"bend": 18}, head={"pitch": -18})
SURGE = nudge(BRACE, {("body", "bend"): 6, ("arms", "roll"): 7, ("arms", "bend"): -10, ("arms", "pitch"): 6, ("head", "pitch"): -5})


def clash_pose(t):
    if t <= CLASH_IN:
        return mix({}, BRACE, smooth(t / CLASH_IN))
    u = t - CLASH_IN
    surge = 0.5 - 0.5 * math.cos(2 * math.pi * u / LOOP)
    q = mix(BRACE, SURGE, surge)
    b = math.sin(2 * math.pi * 2 * u / LOOP)
    q = nudge(q, {("body", "bend"): 2.0 * b, ("head", "pitch"): -1.5 * b, ("arms", "pitch"): -1.5 * b})
    a, c, e = wave(u, SHAKE_A), wave(u, SHAKE_B), wave(u, SHAKE_C)
    amp = 0.6 + 0.4 * surge
    q["rightArm"]["roll"] += 4.0 * amp * a
    q["leftArm"]["roll"] -= 4.0 * amp * c
    q["rightArm"]["bend"] += 3.5 * amp * c
    q["leftArm"]["bend"] += 3.5 * amp * e
    q["head"]["roll"] += 2.0 * amp * a
    q["head"]["yaw"] += 1.4 * amp * c
    q["head"]["pitch"] += 1.2 * amp * e
    return q


def clash_beats():
    beats = [(0, "linear", channels({}))]
    for t in range(1, CLASH_IN + LOOP + 1):
        beats.append((t, "outquad" if t <= CLASH_IN else "linear", clash_pose(t)))
    return beats


def write(name, beats, end, stop, loop, ret, description):
    """The file: one move a beat holding every channel of the upper body (WRITTEN), one per line."""
    moves = []
    for tick, easing, q in beats:
        q = channels(spine(channels(q)))
        move = {"tick": tick, "easing": easing}
        for part, chs in WRITTEN.items():
            move[part] = {ch: round(q[part][ch], 2) for ch in chs}
        moves.append(move)
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.5.1", "description": description,
            "emote": {"beginTick": 0, "endTick": end, "stopTick": stop, "isLoop": loop, "returnTick": ret, "degrees": True,
                      "easeBeforeKeyframe": True, "moves": []}}
    text = json.dumps(data, indent=1)
    body = ",\n".join("   " + json.dumps(m, separators=(",", ":")) for m in moves)
    text = text.replace('"moves": []', '"moves": [\n' + body + "\n  ]")
    json.loads(text)
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        handle.write(text + "\n")


def charge_file(name, pose_at, description, ease_in=6):
    write(name, charge_beats(pose_at, ease_in), end=FULL + LOOP, stop=FULL + LOOP, loop=True, ret=FULL, description=description)


def main():
    charge_file(CHARGE, neutral_charge,
                "Conqueror's Haki charge (neutral): the fists clench and pull in at the waist, the elbows flared; as it fills the "
                "body hunches over the fists, the fists rise under the chest, the head hangs then lifts to glare, the breath heaves "
                "faster and a tremor grows, shudders jolting it; at full charge it holds coiled, shaking hard. The upper body only.")
    write(SUPREME, neutral_release(56, 1.0, 26), 56, 56, False, 0,
          "Conqueror's Haki, a full charge let go (neutral): a 2-tick crunch (the forearms crossed before the chest), then the burst: "
          "the arms flung wide and back, the chest thrown open, the head back in a roar, held shaking, then the exhale.")
    write(PARTIAL, neutral_release(48, 0.6, 18), 48, 48, False, 0,
          "Conqueror's Haki, a partial charge let go (neutral): the crunch and the burst at a little over half the reach, a short roar.")
    charge_file(GOOD[0], good_charge,
                "Conqueror's Haki charge (good: peaceful, in pain): a hand pressed over the face, the other arm clutching the stomach, the "
                "head bowed into the hand, the breath heavy, the hand trembling more as it fills, pained jolts; then the hand slides off "
                "the face and the head comes up into a level, calm stare, held almost still at full charge.", ease_in=8)
    write(GOOD[1], good_release(56, 1.0, 30, STARE), 56, 56, False, 0,
          "Conqueror's Haki, a full charge let go (good): an inhale, then the pressure let out with barely a movement: the open arms "
          "eased out from the sides, the chest open, the stare unmoved; held with a faint tremor; the exhale.")
    write(GOOD[2], good_release(48, 0.6, 18, good_charge(80)), 48, 48, False, 0,
          "Conqueror's Haki, a partial charge let go (good): the hand snaps down off the face into the stare and the arms ease out a "
          "little; the exhale.")
    charge_file(EVIL[0], evil_charge,
                "Conqueror's Haki charge (evil: disturbing): the head drops forward limp, the arms hang like a puppet's; as it fills the "
                "head creeps up and over to an unnatural tilt, staring sideways, the shoulders hitch, the head jerks, the fingers "
                "twitch, more and more often, the body swaying slowly.", ease_in=8)
    write(EVIL[1], evil_release(56, 1.0, 36, 42), 56, 56, False, 0,
          "Conqueror's Haki, a full charge let go (evil): the head snaps up and back as the arms are flung wide and high, the back "
          "arched, a shaking laugh, then the head drops limp and the arms fall.")
    write(EVIL[2], evil_release(48, 0.6, 20, 26), 48, 48, False, 0,
          "Conqueror's Haki, a partial charge let go (evil): the head snaps up, the arms half spread, a short laugh, then limp.")
    write(CLASH, clash_beats(), CLASH_IN + LOOP, CLASH_IN + LOOP, True, CLASH_IN,
          "Conqueror's Haki clash (3.8.2): braced against the other's Haki, the body leaned into it, the fists clenched low and a little "
          "back, the elbows flared, the head level glaring ahead; straining, a tremor through the arms and head, the breath heaving, a "
          "surge of effort leaning in harder once a loop. The upper body only; loops until the clash ends.")
    for name in NAMES:
        problems = anim.lint(anim.load(os.path.join(OUT, name + ".json")))
        print(f"{name}: {'clean' if not problems else problems[:6]}")


if __name__ == "__main__":
    main()
