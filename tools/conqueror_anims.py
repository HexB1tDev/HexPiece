#!/usr/bin/env python3
"""2.4.7: the Conqueror's Haki charge and releases (the owner: "I want a badass animation for the Conqueror Haki charge / release. the
current one is dog shit.").

Writes three animations over the originals' (tools/anim_polish.py leaves these names to this file):
  - conqueror_gear5_charge: played while G is held (the server's beginConquerorCharge, 5-tick fade in). The server roots the player
    (velocity zeroed, no sprint) and the charge fills in 160 ticks (conquerorChargeTicks x 0.625 reaches 100). So the animation is a
    build of 160 ticks, then a loop at full charge (isLoop, returnTick 160, endTick 200; the poses at 160 and 200 are the same):
      0-6     the intake: the weight drops into a wide planted stance, the fists clench and pull down to the hips;
      6-160   the build: the stance widens and sinks, the body hunches over the fists, the elbows flare, the head hangs then lifts to
              glare; the breathing heaves faster, a tremor grows through the arms, the head and the body, shudders jolt it now and then;
      160-200 full charge, looping: coiled as deep as it goes, shaking hard, heaving (whole cycles of every wave in the 40 ticks).
  - supreme_conqueror_release: a full charge let go (every full-charge radial release is the supreme one, HexPiece's JoyBoy patch).
    From the coil, a 2-tick crunch (the forearms crossed before the chest, the body curled, the right knee drawn up), then the burst:
    the foot stamped down wide, arms flung wide, down and back, chest thrown open, head back in a roar, the stance spread and risen; an overshoot, a roar held shaking (dying down), then the
    weight comes back up to standing by 56 ticks (the original's length).
  - conqueror_gear5_release: a partial charge let go: the same crunch and burst at a little over half the reach, settling by 48.

Every beat keys every channel of every part (nothing drifts between beats), in one move a beat; the leans are the body bend (anim_hand's spine rule: the
torso is never pitched or bent alone); the feet are kept on the ground (each beat's body height solved from its legs, `grounded`); the
tremors are sums of sines at whole cycles over the loop (seamless). Conventions: tools/style_anims.py.

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
NAMES = (CHARGE, SUPREME, PARTIAL)
PARTS = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "torso": ("pitch", "yaw", "roll", "bend"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "rightLeg": ("pitch", "yaw", "roll", "bend"),
    "leftLeg": ("pitch", "yaw", "roll", "bend"),
}
# the charge fills in this many ticks; its loop at full charge lasts LOOP
FULL = 160
LOOP = 40
# the ground in model units (the soles of standing feet, y down)
GROUND = 24.0


# ------------------------------------------------------------------------------------------------------------- poses

def arm(pitch=0.0, yaw=0.0, roll=0.0, bend=0.0):
    return {"pitch": pitch, "yaw": yaw, "roll": roll, "bend": bend}


def leg(pitch=0.0, roll=0.0, bend=0.0, yaw=0.0):
    return {"pitch": pitch, "roll": roll, "bend": bend, "yaw": yaw}


def mirror(a):
    return {"pitch": a.get("pitch", 0.0), "yaw": -a.get("yaw", 0.0), "roll": -a.get("roll", 0.0), "bend": a.get("bend", 0.0)}


def pose(right_arm, right_leg, body=None, head=None, left_arm=None, left_leg=None):
    """A pose from the right side (the left mirrored unless given)."""
    return {
        "body": dict(body or {}),
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
    """q with deltas added: {(part, channel): degrees}; an arm's or leg's roll and yaw mirrored on the left."""
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


def feet(q):
    """The lowest point of the legs (model units, y down) with the body at height 0."""
    probe = channels(q)
    probe["body"]["y"] = 0.0
    full = {part: {ch: probe.get(part, {}).get(ch, 0.0) for ch in anim.CHANNELS[part]} for part in anim.PARTS}
    sk = anim.skeleton(full)
    return max(c[1] for part, corners in sk["boxes"] if part.endswith("Leg") for c in corners)


def grounded(q):
    """q with the body's height set so the lowest point of the legs is on the ground."""
    out = channels(q)
    out["body"]["y"] = round((feet(out) - GROUND) / 16.0, 4)
    return out


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


# ------------------------------------------------------------------------------------------------------------- the charge
# The power stance, light (just dropped into it) and deep (full charge): the fists pulled in to the waist (light) and up under the chest
# (deep), the upper arms back, the elbows flared out to the sides; the feet wide, the knees bent over upright shins; the body hunched
# over the fists, the head held up to look ahead (it glares from under the hunch).
COIL_LIGHT = pose(arm(pitch=14, yaw=10, roll=14, bend=-70), leg(pitch=-14, roll=10, bend=18), body={"bend": 8}, head={"pitch": 4})
COIL_DEEP = pose(arm(pitch=38, yaw=16, roll=20, bend=-108), leg(pitch=-28, roll=19, bend=40), body={"bend": 22}, head={"pitch": -16})
# the intake overshoot: a sharp drop before it settles into the light coil
INTAKE = nudge(COIL_LIGHT, {("legs", "bend"): 10, ("legs", "pitch"): -5, ("body", "bend"): 4, ("arms", "bend"): -8, ("head", "pitch"): 6})


def wave(t, freqs):
    """A tremor: a sum of sines (cycles a tick, phase) normalised to about -1..1."""
    return sum(math.sin(2 * math.pi * (f * t + ph)) for f, ph in freqs) / math.sqrt(len(freqs) * 0.5) / 2.0


# whole cycles over the loop (LOOP ticks): k / LOOP cycles a tick
# (all under half a cycle a tick, so one keyframe a tick draws them; 7 to 17 cycles in 40 ticks: 3.5 to 8.5 Hz)
SHAKE_A = ((13 / LOOP, 0.11), (17 / LOOP, 0.53), (9 / LOOP, 0.29))
SHAKE_B = ((11 / LOOP, 0.71), (16 / LOOP, 0.37), (7 / LOOP, 0.05))
SHAKE_C = ((15 / LOOP, 0.42), (12 / LOOP, 0.83), (8 / LOOP, 0.61))
BREATH_FULL = 2 / LOOP  # two breaths in the loop (20 ticks each) at full charge
# the shudders in the build (tick, strength): a jolt through the whole body, the head snapping down
SHUDDERS = ((54, 0.55), (92, 0.75), (128, 0.9))
# and at full charge, one each loop, ending inside it
LOOP_SHUDDER = (FULL + 22, 1.0)


def breath_phase(t):
    """The breath's phase (cycles): slow (32 ticks) at first, quickening to BREATH_FULL by FULL, integrated so it never jumps; at full
    charge whole breaths every LOOP ticks."""
    slow = 1 / 32.0
    if t <= FULL:
        # f(t) = slow + (full - slow) * smooth(t / FULL): integrate in steps
        steps = max(1, int(t * 4))
        total = 0.0
        for i in range(steps):
            u = (i + 0.5) / steps * t
            total += (slow + (BREATH_FULL - slow) * smooth(u / FULL)) * (t / steps)
        return total
    return breath_phase(FULL) + BREATH_FULL * (t - FULL)


def shudder(t, at, strength):
    """A jolt: sharp in (1 tick), held, eased out over 7 ticks."""
    d = t - at
    if d < 0 or d > 8:
        return 0.0
    if d < 1:
        return strength * d
    return strength * (1 - smooth((d - 1) / 7))


def charge_pose(t):
    """The charge at tick t (0..FULL + LOOP)."""
    if t <= 3:
        return grounded(mix({}, INTAKE, smooth(t / 3)))
    if t <= 6:
        return grounded(mix(INTAKE, COIL_LIGHT, smooth((t - 3) / 3)))
    s = smooth((min(t, FULL) - 6) / (FULL - 6))
    q = mix(COIL_LIGHT, COIL_DEEP, s)
    # the head: hangs while it gathers, lifts to glare as it fills
    hang = math.sin(math.pi * min(1.0, (t - 6) / 90.0)) * (1 - s)
    q["head"]["pitch"] += 16 * hang
    # the breath: the chest heaves (the body bend), the shoulders with it (arm pitch); deeper as it fills
    b = math.sin(2 * math.pi * breath_phase(t))
    depth = 0.6 + 0.4 * s
    q = nudge(q, {("body", "bend"): 3.2 * b * depth, ("head", "pitch"): -2.0 * b * depth, ("arms", "pitch"): -2.5 * b * depth,
                  ("arms", "roll"): 1.5 * b * depth})
    # the tremor, growing: arms (roll and bend), the body (roll), the head (roll and yaw)
    amp = 0.25 + 0.75 * s if t <= FULL else 1.0
    a, c, e = wave(t, SHAKE_A), wave(t, SHAKE_B), wave(t, SHAKE_C)
    q["rightArm"]["roll"] += 4.5 * amp * a
    q["leftArm"]["roll"] -= 4.5 * amp * c
    q["rightArm"]["bend"] += 4.0 * amp * c
    q["leftArm"]["bend"] += 4.0 * amp * e
    q["body"]["roll"] += 1.8 * amp * e
    q["head"]["roll"] += 2.6 * amp * a
    q["head"]["yaw"] += 1.8 * amp * c
    q["head"]["pitch"] += 1.6 * amp * e
    # the shudders
    jolt = sum(shudder(t, at, k) for at, k in SHUDDERS)
    if t >= FULL:
        jolt += shudder(t, *LOOP_SHUDDER)
    q = nudge(q, {("body", "bend"): 6 * jolt, ("head", "pitch"): 9 * jolt, ("arms", "bend"): -8 * jolt, ("arms", "roll"): 4 * jolt,
                  ("legs", "bend"): 6 * jolt, ("legs", "pitch"): -3 * jolt})
    return grounded(q)


def charge():
    beats = [(0, "linear", channels({}))]
    for t in range(1, FULL + LOOP + 1):
        easing = "outquad" if t <= 6 else "linear"
        beats.append((t, easing, charge_pose(t)))
    write(CHARGE, beats, end=FULL + LOOP, stop=FULL + LOOP, loop=True, ret=FULL,
          description="Conqueror's Haki charge: the weight drops into a wide planted stance and the fists clench down at the hips; as "
                      "it fills the stance sinks and widens, the body hunches over the fists, the elbows flare, the head hangs then "
                      "lifts to glare, the breath heaves faster and a tremor grows through the whole body, shudders jolting it; at full "
                      "charge it holds coiled as deep as it goes, shaking hard.")


# ------------------------------------------------------------------------------------------------------------- the releases
# the crunch: the forearms crossed before the chest, the body curled over them, the left knee deeper and the right knee drawn up for the
# stomp the burst brings down (the ground it splits)
CRUNCH = pose(arm(pitch=-40, yaw=-40, roll=-20, bend=-70), leg(pitch=-58, roll=10, bend=78), body={"bend": 30}, head={"pitch": 10},
              left_leg=mirror(leg(pitch=-30, roll=18, bend=50)))
# the burst: the arms flung wide, down and back, the fists clenched, the chest thrown open (the bend back), the head back roaring, the
# stance spread and risen
BURST = pose(arm(pitch=30, yaw=0, roll=62, bend=-8), leg(pitch=-10, roll=24, bend=14), body={"bend": -16}, head={"pitch": -36})
OVER = nudge(BURST, {("arms", "roll"): 8, ("arms", "pitch"): 6, ("body", "bend"): -4, ("head", "pitch"): -6})
# the roar held: a little less than the burst's reach, the elbows tightening
ROAR = nudge(BURST, {("arms", "roll"): -5, ("arms", "pitch"): -3, ("arms", "bend"): -8, ("body", "bend"): 3, ("head", "pitch"): 5})
# the arms coming down, the head forward, still in the stance
EXHALE = pose(arm(pitch=4, yaw=0, roll=12, bend=-24), leg(pitch=-8, roll=10, bend=14), body={"bend": 8}, head={"pitch": 8})


def release(name, length, reach, hold, description):
    """A release: from the charge's coil, the crunch, the burst at reach (1: the full one), the roar held to hold, the settle."""
    start = charge_pose(FULL)
    burst = mix(EXHALE, BURST, reach)
    over = mix(EXHALE, OVER, reach)
    roar = mix(EXHALE, ROAR, reach)
    crunch = mix(start, CRUNCH, 0.55 + 0.45 * reach)
    beats = [(0, "linear", start), (2, "inquad", grounded(crunch)), (5, "outexpo", grounded(burst)), (7, "outback", grounded(over))]
    # the roar, shaking, dying down
    for t in range(8, hold + 1):
        fade = 1 - (t - 8) / max(1, hold - 8) * 0.6
        a, c, e = wave(t, SHAKE_A), wave(t, SHAKE_B), wave(t, SHAKE_C)
        k = reach * fade
        q = nudge(roar, {("rightArm", "roll"): 3.0 * k * a, ("leftArm", "roll"): -3.0 * k * c, ("rightArm", "pitch"): 2.0 * k * c,
                         ("leftArm", "pitch"): 2.0 * k * e, ("head", "roll"): 2.4 * k * e, ("head", "pitch"): 1.6 * k * a,
                         ("body", "roll"): 1.0 * k * c})
        beats.append((t, "linear", grounded(q)))
    settle = hold + (length - hold) * 0.45
    beats.append((round(settle), "inoutsine", grounded(mix(roar, EXHALE, 1.0))))
    beats.append((length, "inoutsine", channels({})))
    write(name, beats, end=length, stop=length, loop=False, ret=0, description=description)


# ------------------------------------------------------------------------------------------------------------- the files

def write(name, beats, end, stop, loop, ret, description):
    """The file: one move a beat holding every channel of every part (as the original's own animations write theirs), one per line."""
    moves = []
    for tick, easing, q in beats:
        q = channels(spine(channels(q)))
        move = {"tick": tick, "easing": easing}
        for part, chs in PARTS.items():
            move[part] = {ch: round(q[part][ch], 2) for ch in chs}
        moves.append(move)
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 2.4.7", "description": description,
            "emote": {"beginTick": 0, "endTick": end, "stopTick": stop, "isLoop": loop, "returnTick": ret, "degrees": True,
                      "easeBeforeKeyframe": True, "moves": []}}
    text = json.dumps(data, indent=1)
    body = ",\n".join("   " + json.dumps(m, separators=(",", ":")) for m in moves)
    text = text.replace('"moves": []', '"moves": [\n' + body + "\n  ]")
    json.loads(text)
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        handle.write(text + "\n")


def main():
    charge()
    release(SUPREME, 56, 1.0, 26,
            "Conqueror's Haki, the full charge let go: from the coil a 2-tick crunch (the fists driven together, the knees deeper), "
            "then the burst: arms flung wide and back, the chest thrown open, the head back in a roar, the stance spread; the roar "
            "held shaking, then the weight comes back up to standing.")
    release(PARTIAL, 48, 0.6, 18,
            "Conqueror's Haki, a partial charge let go: the crunch and the burst at a little over half the reach, a short roar, then "
            "standing again.")
    for name in NAMES:
        problems = anim.lint(anim.load(os.path.join(OUT, name + ".json")))
        print(f"{name}: {'clean' if not problems else problems[:6]}")


if __name__ == "__main__":
    main()
