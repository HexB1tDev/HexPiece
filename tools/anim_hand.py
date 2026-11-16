#!/usr/bin/env python3
"""2.1.1: hand-made reworks of the Haki animations (the owner: "Improve Haki's lightning, usage, animation, and everything else").

Each replaces the original of the same name in tools/anim_polish.py. Each keeps the original's beginTick, endTick, stopTick, isLoop and
returnTick (the server, the sounds and the effects are timed against them) and animates exactly the parts the original did (a part the
original left to vanilla, such as walking legs under a charge, stays vanilla's). Poses are written as {part: {channel: degrees}}; arms
are given for the right side and mirrored for the left (yaw and roll flip). Every channel of every animated part is keyed at every beat, so
nothing drifts between beats.

The spine rule (PlayerAnimator + bendy-lib, verified in HexGhoul's RigPose; the owner flagged the originals' broken frames): a torso pitch
or torso bend moves the torso mesh alone and leaves the head and arms floating (and the torso swings off the hips). So no beat here pitches
or bends the torso: a hunch or an arch is the whole-body "body" bend (+ forward), which carries the head and both arms with it, and a twist
is at most a few degrees of torso yaw. tools/anim.py's lint fails a beat whose head, arms or hips come apart.
"""
import anim

CH = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "torso": ("pitch", "yaw", "roll", "bend"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "rightLeg": ("pitch", "yaw", "roll", "bend"),
    "leftLeg": ("pitch", "yaw", "roll", "bend"),
}


def arms(pitch=0.0, yaw=0.0, roll=0.0, bend=0.0, right=None, left=None):
    """Both arms, the left mirrored (or each given separately as a dict)."""
    r = right if right is not None else {"pitch": pitch, "yaw": yaw, "roll": roll, "bend": bend}
    l = left if left is not None else {"pitch": r["pitch"], "yaw": -r.get("yaw", 0.0), "roll": -r.get("roll", 0.0), "bend": r.get("bend", 0.0)}
    return {"rightArm": r, "leftArm": l}


def legs(bend=0.0, spread=0.0, right=None, left=None):
    """Both legs: soft knees and feet apart, or each given."""
    r = right if right is not None else {"pitch": -2.0, "roll": spread, "bend": bend}
    l = left if left is not None else {"pitch": 2.0, "roll": -spread, "bend": bend}
    return {"rightLeg": r, "leftLeg": l}


def pose(*pieces, **parts):
    out = {}
    for piece in pieces:
        for part, values in piece.items():
            out.setdefault(part, {}).update(values)
    for part, values in parts.items():
        out.setdefault(part, {}).update(values)
    return out


def shake(base, ticks, start, amp, channels=(("rightArm", "roll"), ("leftArm", "roll")), easing="inoutsine"):
    """A tremble over a held pose: alternating +-amp on the given channels, one beat each tick count."""
    beats = []
    for i, tick in enumerate(ticks):
        sign = 1 if i % 2 == 0 else -1
        p = {part: dict(values) for part, values in base.items()}
        for part, channel in channels:
            mirror = -1 if part.startswith("left") else 1
            p.setdefault(part, {})
            p[part][channel] = p[part].get(channel, 0.0) + sign * amp * mirror * (1 - 0.5 * i / max(1, len(ticks)))
        beats.append((tick, easing, p))
    return beats


def spine(p):
    """A beat's torso lean (pitch + bend) moved to the body bend, which carries the head and arms; the head keeps most of its gaze;
    the twist and side tilt stay small (the arms do not follow a torso yaw, the shoulders would come off)."""
    q = {k: dict(v) for k, v in p.items()}
    torso = q.get("torso", {})
    lean = torso.pop("pitch", 0.0) + torso.pop("bend", 0.0)
    if lean:
        q.setdefault("body", {})["bend"] = q.get("body", {}).get("bend", 0.0) + lean
        head = q.setdefault("head", {})
        head["pitch"] = head.get("pitch", 0.0) - lean * 0.8
    if "yaw" in torso:
        torso["yaw"] = max(-8.0, min(8.0, torso["yaw"]))
    if "roll" in torso:
        torso["roll"] = max(-4.0, min(4.0, torso["roll"]))
    q["torso"] = torso
    return q


def build(original, timeline, description):
    """The animation: the original's timing and animated parts, these beats (plus the body bend, which no vanilla motion uses)."""
    emote = original["emote"]
    parts = [p for p in anim.PARTS if any(p in m for m in emote["moves"])]
    body_pinned = "body" in parts
    if not body_pinned:
        parts.append("body")
    moves = []
    for tick, easing, p in timeline:
        p = spine(p)
        for part in parts:
            if part not in CH:
                continue
            values = p.get(part, {})
            channels = CH[part] if (part != "body" or body_pinned) else ("bend",)
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    out = {
        "version": original.get("version", 3),
        "name": original.get("name"),
        "author": "HexPiece 2.1.1 (hand-made rework)",
        "description": description,
        "emote": {k: emote[k] for k in ("beginTick", "endTick", "stopTick", "isLoop", "returnTick", "degrees") if k in emote},
    }
    out["emote"]["easeBeforeKeyframe"] = True
    out["emote"]["moves"] = moves
    return out


NEUTRAL = {}


def armament_activate(original):
    """30 ticks, every part. Armament hardening: breathe in, fists drawn to the chest, the squeeze (haki crawling up the arms), the
    snap down and out as the coating sets, chest out, then settle."""
    inhale = pose(arms(-18, 8, 6, -30), legs(4, 1), torso={"pitch": -4}, head={"pitch": -3}, body={"y": 0.015})
    fists = pose(arms(-52, -16, 24, -104), legs(14, 3.5), torso={"pitch": 6, "bend": 4}, head={"pitch": 7}, body={"y": -0.045})
    squeeze = pose(arms(-44, -12, 30, -116), legs(16, 3.5), torso={"pitch": 9, "bend": 7}, head={"pitch": 9}, body={"y": -0.055})
    snap = pose(arms(10, -6, 24, -22), legs(10, 4.5), torso={"pitch": -7, "bend": -3}, head={"pitch": -5}, body={"y": -0.02})
    over = pose(arms(16, -8, 28, -16), legs(9, 4.5), torso={"pitch": -8, "bend": -4}, head={"pitch": -6}, body={"y": -0.025})
    hold = pose(arms(6, -4, 14, -26), legs(6, 3), torso={"pitch": -3}, head={"pitch": -2}, body={"y": -0.01})
    timeline = [(0, "linear", NEUTRAL), (3, "outsine", inhale), (7, "outexpo", fists), (10, "inoutsine", squeeze)]
    timeline += shake(squeeze, [11, 12, 13, 14, 15], 11, 2.2, (("rightArm", "roll"), ("leftArm", "roll"), ("torso", "yaw")))
    timeline += [(16, "inoutsine", squeeze), (18, "outexpo", snap), (20, "outback", over), (24, "inoutsine", hold), (30, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Armament Haki: breathe in, fists drawn to the chest, the squeeze as the haki crawls up the arms, "
                                     "then the snap down and out as the coating sets, chest out.")


def observation_activate(original):
    """32 ticks, every part. Observation: the eyes close, two fingers to the temple, a stillness, then the head snaps up and reads
    the field left and right before the hand comes down."""
    close = pose(arms(right={"pitch": -128, "yaw": -34, "roll": 18, "bend": -118}, left={"pitch": 6, "yaw": 0, "roll": -10, "bend": -14}),
                 legs(6, 2.5), torso={"pitch": 3}, head={"pitch": 14, "yaw": -4}, body={"y": -0.015})
    breathe = pose(close, torso={"pitch": -2}, head={"pitch": 10, "yaw": -4}, body={"y": 0.0})
    sense = pose(close, torso={"pitch": -4, "yaw": 6}, head={"pitch": -10, "yaw": 22}, body={"y": 0.005})
    scan = pose(close, torso={"pitch": -3, "yaw": -6}, head={"pitch": -7, "yaw": -20})
    lower = pose(arms(right={"pitch": -24, "yaw": -8, "roll": 8, "bend": -40}, left={"pitch": 4, "yaw": 0, "roll": -8, "bend": -12}),
                 legs(4, 2), torso={"pitch": -1}, head={"pitch": -2, "yaw": 0})
    timeline = [(0, "linear", NEUTRAL), (5, "inoutsine", close), (11, "inoutsine", breathe), (14, "outexpo", sense),
                (19, "inoutback", scan), (24, "inoutsine", lower), (32, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Observation Haki: eyes closed, two fingers to the temple, a held breath, then the head snaps up "
                                     "and reads the field to one side and the other before the hand comes down.")


def acoc_activate(original):
    """44 ticks, torso, head and arms (legs stay vanilla's). Advanced coating: the right fist raised before the face, the left hand
    gripping its forearm, the arm shaking as the haki pushes out of it, a flexed extension, then the fist chambered at the hip."""
    raise_ = pose(arms(right={"pitch": -104, "yaw": -22, "roll": 6, "bend": -104}, left={"pitch": -78, "yaw": 38, "roll": -12, "bend": -64}),
                  torso={"pitch": 4, "yaw": -8}, head={"pitch": 6, "yaw": -6})
    strain = pose(arms(right={"pitch": -96, "yaw": -20, "roll": 8, "bend": -114}, left={"pitch": -74, "yaw": 40, "roll": -12, "bend": -70}),
                  torso={"pitch": 7, "yaw": -10, "bend": 4}, head={"pitch": 9, "yaw": -6})
    extend = pose(arms(right={"pitch": -86, "yaw": -8, "roll": 4, "bend": -18}, left={"pitch": -34, "yaw": 24, "roll": -8, "bend": -82}),
                  torso={"pitch": 1, "yaw": 12}, head={"pitch": 2, "yaw": 4})
    chamber = pose(arms(right={"pitch": 14, "yaw": 6, "roll": 10, "bend": -84}, left={"pitch": -42, "yaw": 26, "roll": -10, "bend": -60}),
                   torso={"pitch": 2, "yaw": -14}, head={"pitch": 0, "yaw": 6})
    timeline = [(0, "linear", NEUTRAL), (6, "outexpo", raise_), (10, "inoutsine", strain)]
    timeline += shake(strain, [12, 14, 16, 18, 20, 22], 12, 2.6, (("rightArm", "roll"), ("rightArm", "pitch"), ("torso", "yaw")))
    timeline += [(24, "inoutsine", strain), (27, "outexpo", extend), (29, "outback", pose(extend, rightArm={"pitch": -90, "yaw": -6, "roll": 4, "bend": -12})),
                 (34, "inoutsine", chamber), (38, "inoutsine", chamber), (44, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Advanced coating: the fist raised before the face, the other hand gripping its forearm, the arm "
                                     "shaking as the haki pushes out of it, a flexed extension, the fist chambered at the hip.")


def conqueror_release(original):
    """42 ticks, torso, head and arms (legs stay vanilla's). The coil (hunched, arms crossed low), deeper, then the eruption: chest
    thrown open, head back, arms flung wide and up, an overshoot, the held roar shaking, and the exhale as the arms come down."""
    coil = pose(arms(22, 30, -12, -64), torso={"pitch": 15, "bend": 10}, head={"pitch": 16})
    deeper = pose(arms(28, 36, -16, -72), torso={"pitch": 19, "bend": 13}, head={"pitch": 19})
    erupt = pose(arms(-26, -48, 62, -8), torso={"pitch": -11, "bend": -7}, head={"pitch": -26})
    over = pose(arms(-30, -54, 70, -4), torso={"pitch": -13, "bend": -8}, head={"pitch": -29})
    roar = pose(arms(-22, -46, 60, -10), torso={"pitch": -10, "bend": -6}, head={"pitch": -24})
    exhale = pose(arms(6, -8, 18, -32), torso={"pitch": 5, "bend": 2}, head={"pitch": 7})
    timeline = [(0, "outquad", coil), (3, "inquad", deeper), (5, "outexpo", erupt), (7, "outback", over)]
    timeline += shake(roar, [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20], 8, 1.8,
                      (("rightArm", "roll"), ("leftArm", "roll"), ("head", "roll"), ("torso", "roll")), "linear")
    timeline += [(22, "inoutsine", roar), (30, "inoutsine", exhale), (42, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Conqueror's Haki release: the coil, deeper, then the eruption (chest thrown open, head back, arms "
                                     "flung wide and up), the held roar shaking, and the exhale as the arms come down.")


def haki_leap_charge(original):
    """36-tick loop, arms only (vanilla keeps the legs and body: the crouch is the game's). A sprinter's pre-jump: both arms swung back
    behind the hips and pumping, wider as they go."""
    back = arms(44, 10, 16, -26)
    further = arms(56, 12, 22, -18)
    timeline = [(0, "inoutsine", back), (9, "inoutsine", further), (18, "inoutsine", back), (27, "inoutsine", further), (36, "inoutsine", back)]
    return build(original, timeline, "Haki Leap charge: both arms swung back behind the hips, pumping, loading the jump (the crouch and "
                                     "the legs stay the game's own).")


def haki_leap_release(original):
    """24 ticks, arms only. The throw: arms whipped forward and up as the legs fire, an overshoot over the head, then spread for
    balance through the flight and lowered."""
    throw = arms(-150, -10, 14, -8)
    over = arms(-162, -14, 18, -4)
    balance = arms(-44, -20, 66, -16)
    timeline = [(0, "linear", arms(52, 12, 20, -20)), (2, "outexpo", throw), (4, "outback", over), (12, "inoutsine", balance),
                (24, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Haki Leap release: arms whipped forward and up as the legs fire, an overshoot over the head, then "
                                     "spread wide for balance through the flight.")


def dominion_activate(original):
    """30 ticks, every part. Haki Grip: the lunge with the open hand thrust out, the grip closing, the haul back to the hip with the
    whole body turning and leaning away, and the settle."""
    thrust = pose(arms(right={"pitch": -96, "yaw": -4, "roll": 0, "bend": -6}, left={"pitch": 24, "yaw": 0, "roll": -10, "bend": -30}),
                  legs(right={"pitch": -26, "roll": 2, "bend": 30}, left={"pitch": 22, "roll": -3, "bend": 10}),
                  torso={"pitch": 6, "yaw": -16}, head={"pitch": 2, "yaw": 12}, body={"y": -0.06, "z": -0.05})
    grip = pose(thrust, rightArm={"pitch": -98, "yaw": -4, "roll": -8, "bend": -30})
    haul = pose(arms(right={"pitch": 12, "yaw": 12, "roll": 10, "bend": -104}, left={"pitch": -30, "yaw": 20, "roll": -8, "bend": -40}),
                legs(right={"pitch": -8, "roll": 3, "bend": 16}, left={"pitch": 12, "roll": -3, "bend": 22}),
                torso={"pitch": -4, "yaw": 24}, head={"pitch": 0, "yaw": -14}, body={"y": -0.04, "z": 0.06, "pitch": 6})
    over = pose(haul, torso={"pitch": -5, "yaw": 28}, body={"y": -0.045, "z": 0.07, "pitch": 7})
    settle = pose(arms(right={"pitch": 6, "yaw": 6, "roll": 8, "bend": -60}, left={"pitch": -8, "yaw": 10, "roll": -6, "bend": -24}),
                  legs(8, 3), torso={"pitch": -1, "yaw": 8}, head={"yaw": -4}, body={"y": -0.02})
    timeline = [(0, "linear", NEUTRAL), (4, "outexpo", thrust), (8, "outback", grip), (13, "inexpo", grip), (16, "outexpo", haul),
                (18, "outback", over), (23, "inoutsine", settle), (30, "inoutsine", NEUTRAL)]
    return build(original, timeline, "Haki Grip: the lunge with the open hand thrust out, the grip closing, the haul back to the hip with "
                                     "the whole body turning and leaning away from the pull.")


ANIMATIONS = {
    "armament_activate": armament_activate,
    "observation_activate": observation_activate,
    "acoc_activate": acoc_activate,
    "conqueror_release": conqueror_release,
    "haki_leap_charge": haki_leap_charge,
    "haki_leap_release": haki_leap_release,
    "dominion_activate": dominion_activate,
}
