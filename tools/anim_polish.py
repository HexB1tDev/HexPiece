#!/usr/bin/env python3
"""2.1.1, the owner: "Better every single animation." The rework pass over every original animation.

Reads the original jar's animations, writes the reworked ones to src/main/resources/assets/hexhaki/player_animation/ (build.py packs them
over the originals). Hand-made animations in tools/anim_hand.py replace an original outright; every other one goes through these passes,
which never move or remove an original keyframe (so every hit, effect and sound the server times against a tick still lands on it) and
never change beginTick/endTick/stopTick/isLoop:

  0. spine    - every torso lean goes to the body bend (head and arms stay attached; see p_spine).
  1. easing   - straight "linear" moves of more than 8 degrees become in-out sine (limbs accelerate and decelerate instead of sliding).
  2. snap     - a fast swing into an extreme (35+ degrees at 14+ degrees a tick, then a hold or a turn back) eases out with a little
                overshoot ("outback"): the limb snaps past the pose and settles into it. Impacts read as impacts.
  3. wind-up  - the same fast swing, when it starts from a hold of 3+ ticks, first draws back against itself (14% of the swing) over
                that hold: anticipation, so the strike has somewhere to come from.
  Every pass only touches parts the original animates (55 originals leave legs, body or head to vanilla on purpose: walking under a
  charge must keep working), and the thragg_ attacker/victim pairs only get the easing pass, so they stay in step.

  4. weight   - big strikes drop the body a little at the moment of the extreme and lift it back (only when the animation never moves the
                body up or down itself), and legs that never move in a big upper-body animation take an athletic stance (soft knees,
                feet apart) for its length.
  5. head     - when the whole body tilts 15+ degrees and the head is never animated, the head counters a third of the tilt (eyes stay
                nearer level, as a fighter's do).
  6. off arm  - in a strike where one arm swings hard and the other never moves, the other arm pulls back as a counter-swing.
  7. breath   - loops (charges, idles, stances, flight) get a slow breath on every channel they leave still: torso, arms and body bob,
                whole cycles over the loop so it stays seamless.

Usage: python3 tools/anim_polish.py   (reads the originals straight from input/hexhaki-2.1.0.jar)
"""
import copy
import json
import math
import os
import sys
import zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
LIMB_ANGLES = ("pitch", "yaw", "roll", "bend")
LOOP_WORDS = ("charge", "idle", "stance", "float", "fly", "run", "walk", "ready", "loop", "hold")
# synchronised pairs (attacker and victim are played together and must stay in step): only the safe passes (easing, head)
PAIRED = ("thragg_",)
# arms seen in first person (HakiAnimations.OWN_FIRST_PERSON_ARM): no counter-swing of the off arm (it would cross the view)
FIRST_PERSON = {"gear5_pistol", "gear5_gatling", "gear5_whip", "gear5_rocket", "gear5_coil_release", "gear5_giant_grab", "gear5_giant_throw"}


def tracks_of(moves):
    """{(part, channel): [(tick, value, easing, move index)]} from a moves list."""
    tracks = {}
    for index, move in enumerate(moves):
        for part in anim.PARTS:
            if part in move:
                for channel, value in move[part].items():
                    tracks.setdefault((part, channel), []).append((move.get("tick", 0), float(value), move.get("easing", "linear"), index))
    for key in tracks:
        tracks[key].sort(key=lambda f: (f[0], f[3]))
    return tracks


def still(track, tolerance=0.01):
    return track is None or all(abs(f[1]) <= tolerance for f in track)


class Builder:
    """Collects the reworked animation as single-channel keyframes (the format playerAnimator reads move by move)."""

    def __init__(self, data):
        self.data = copy.deepcopy(data)
        self.emote = self.data["emote"]
        self.channels = {}
        for (part, channel), frames in tracks_of(self.emote["moves"]).items():
            merged = {}
            for tick, value, easing, _ in frames:
                merged[tick] = [tick, value, easing]
            self.channels[(part, channel)] = [merged[t] for t in sorted(merged)]
        self.notes = []
        # Parts the original animates at all. A part it never names is left to vanilla on purpose (walking legs under a charge, the
        # body the game turns): no pass may add a keyframe to it, or it would freeze.
        self.pinned = {part for move in self.emote["moves"] for part in move if part in anim.PARTS}

    def track(self, part, channel):
        return self.channels.get((part, channel))

    def set(self, part, channel, tick, value, easing, replace=False):
        if part not in self.pinned:
            return
        frames = self.channels.setdefault((part, channel), [])
        for frame in frames:
            if frame[0] == tick:
                if replace:
                    frame[1], frame[2] = value, easing
                return
        frames.append([tick, value, easing])
        frames.sort(key=lambda f: f[0])

    def value_at(self, part, channel, tick):
        frames = self.channels.get((part, channel))
        if not frames:
            return 0.0
        if tick <= frames[0][0]:
            return frames[0][1]
        for a, b in zip(frames, frames[1:]):
            if a[0] <= tick <= b[0]:
                t = 0 if b[0] == a[0] else (tick - a[0]) / (b[0] - a[0])
                return a[1] + (b[1] - a[1]) * anim.ease(b[2], t)
        return frames[-1][1]

    def write(self):
        moves = []
        for (part, channel), frames in sorted(self.channels.items()):
            for tick, value, easing in frames:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(value, 3)}})
        moves.sort(key=lambda m: m["tick"])
        self.emote["moves"] = moves
        self.emote["easeBeforeKeyframe"] = True
        return self.data


def length_of(emote):
    return max(emote.get("stopTick", 0), emote.get("endTick", 0))


def p_spine(b):
    """The owner flagged the originals' broken frames: a torso pitch or bend moves the torso mesh alone (PlayerAnimator + bendy-lib), so
    the torso swings off the hips and the head and arms float. Every torso lean (pitch + bend) becomes the body bend, which carries the
    head and arms; the head and arms are turned back by it so they keep the direction the author gave them, now attached. Twists and side
    tilts are kept small (the shoulders do not follow a torso yaw)."""
    pitch, bend = b.track("torso", "pitch"), b.track("torso", "bend")
    if not pitch and not bend:
        return
    ticks = sorted({f[0] for f in (pitch or []) + (bend or [])} | {f[0] for part in ("head", "rightArm", "leftArm")
                                                                    for f in (b.track(part, "pitch") or [])})
    lean = {t: b.value_at("torso", "pitch", t) + b.value_at("torso", "bend", t) for t in ticks}
    easing = {f[0]: f[2] for f in (pitch or bend)}
    head = {t: b.value_at("head", "pitch", t) for t in ticks}
    arms = {arm: {t: b.value_at(arm, "pitch", t) for t in ticks} for arm in ("rightArm", "leftArm")}
    b.pinned.add("body")
    for t in ticks:
        b.set("body", "bend", t, lean[t], easing.get(t, "inoutsine"), replace=True)
        b.set("head", "pitch", t, head[t] - lean[t] * 0.8, easing.get(t, "inoutsine"), replace=True)
        for arm in ("rightArm", "leftArm"):
            if b.track(arm, "pitch"):
                b.set(arm, "pitch", t, arms[arm][t] - lean[t], easing.get(t, "inoutsine"), replace=True)
    for channel in ("pitch", "bend"):
        for frame in b.track("torso", channel) or []:
            frame[1] = 0.0
    for channel, most in (("yaw", 10.0), ("roll", 5.0)):
        for frame in b.track("torso", channel) or []:
            frame[1] = max(-most, min(most, frame[1]))


def p_clearance(b, rounds=14):
    """No forearm through the head: where the lint finds one, the arm's roll keyframes either side of that moment turn 4 degrees
    further out, round after round until the arm is clear (at most 56 degrees)."""
    for _ in range(rounds):
        clipping = {}
        for tick, what in anim.lint(anim.Anim(b.write())):
            if "through the head" in what:
                clipping.setdefault(what.split(" ")[0], set()).add(tick)
        if not clipping:
            return
        for arm, ticks in clipping.items():
            if not b.track(arm, "roll"):
                b.set(arm, "roll", 0, 0.0, "inoutsine")
            frames = b.track(arm, "roll") or []
            moving = {}
            for tick in ticks:
                before = [f for f in frames if f[0] <= tick][-1:]
                after = [f for f in frames if f[0] >= tick][:1]
                for f in before + after:
                    moving[id(f)] = f
            for f in moving.values():
                # Outward is +roll for a hanging right arm, but with the arm raised past level the same turn swings it in: the sign
                # follows where the arm points (its pitch at that keyframe).
                up = math.cos(math.radians(b.value_at(arm, "pitch", f[0]))) < 0
                outward = (4.0 if arm == "rightArm" else -4.0) * (-1.0 if up else 1.0)
                f[1] += outward


def p_easing(b):
    for frames in b.channels.values():
        for i in range(1, len(frames)):
            if frames[i][2] == "linear" and abs(frames[i][1] - frames[i - 1][1]) > 8:
                frames[i][2] = "inoutsine"


def strikes(b):
    """Fast swings into an extreme: [(part, channel, index of the extreme)]."""
    found = []
    for (part, channel), frames in b.channels.items():
        if part not in ("rightArm", "leftArm", "rightLeg", "leftLeg", "torso", "body") or channel not in LIMB_ANGLES:
            continue
        for i in range(1, len(frames)):
            dt = frames[i][0] - frames[i - 1][0]
            dv = frames[i][1] - frames[i - 1][1]
            if dt <= 0 or abs(dv) < 35 or abs(dv) / dt < 14:
                continue
            after = frames[i + 1] if i + 1 < len(frames) else None
            turns = after is None or (after[1] - frames[i][1]) * dv <= 0 or abs(after[1] - frames[i][1]) < abs(dv) * 0.2
            if turns:
                found.append((part, channel, i))
    return found


def p_snap(b, hits):
    for part, channel, i in hits:
        frame = b.channels[(part, channel)][i]
        if frame[2] in ("linear", "inoutsine", "outsine", "outquad", "outcubic", "insine", "inquad", "inoutquad"):
            frame[2] = "outback"


def p_windup(b, hits, emote):
    done = set()
    for part, channel, i in hits:
        frames = b.channels[(part, channel)]
        if i < 2 or (part, channel, i) in done:
            continue
        start, held = frames[i - 1], frames[i - 2]
        if start[0] - held[0] < 3 or abs(start[1] - held[1]) > 2.0:
            continue
        swing = frames[i][1] - start[1]
        start[1] = start[1] - swing * 0.14
        start[2] = "inoutsine"
        done.add((part, channel, i))


def p_weight(b, hits, emote, name):
    length = length_of(emote)
    big = [h for h in hits if h[0] in ("rightArm", "leftArm", "rightLeg", "leftLeg")]
    if big and still(b.track("body", "y")) and not any(w in name for w in ("dodge", "flip", "jump", "leap", "bounce", "fly", "float", "dive")):
        for part, channel, i in big[:4]:
            tick = b.channels[(part, channel)][i][0]
            b.set("body", "y", max(0, tick - 2), 0.0, "inoutsine")
            b.set("body", "y", tick, -0.045, "outquad", replace=True)
            if tick + 3 <= length:
                b.set("body", "y", tick + 3, 0.0, "inoutsine")
    legs_still = all(still(b.track(leg, ch)) for leg in ("rightLeg", "leftLeg") for ch in LIMB_ANGLES)
    upper_moves = any(not still(b.track(arm, ch)) for arm in ("rightArm", "leftArm") for ch in LIMB_ANGLES)
    if legs_still and upper_moves and length >= 8 and not emote.get("isLoop", False):
        first = min(f[0] for frames in b.channels.values() for f in frames)
        last = max(f[0] for frames in b.channels.values() for f in frames)
        settle = min(last, first + 4)
        release = max(settle, last - 3)
        for leg, side in (("rightLeg", 1), ("leftLeg", -1)):
            for tick, bend, roll, pitch in ((first, 0, 0, 0), (settle, 12, 3.5 * side, -5 if side > 0 else 4),
                                            (release, 12, 3.5 * side, -5 if side > 0 else 4), (last, 0, 0, 0)):
                b.set(leg, "bend", tick, bend, "inoutsine")
                b.set(leg, "roll", tick, roll, "inoutsine")
                b.set(leg, "pitch", tick, pitch, "inoutsine")
        if still(b.track("body", "y")):
            for tick, y in ((first, 0.0), (settle, -0.03), (release, -0.03), (last, 0.0)):
                b.set("body", "y", tick, y, "inoutsine")
    elif legs_still and upper_moves and emote.get("isLoop", False):
        loop_len = max(1, emote.get("endTick", length))
        for leg, side in (("rightLeg", 1), ("leftLeg", -1)):
            for tick in (0, loop_len):
                b.set(leg, "bend", tick, 10, "inoutsine")
                b.set(leg, "roll", tick, 3 * side, "inoutsine")


def p_head(b):
    if not all(still(b.track("head", ch)) for ch in ("pitch", "roll")):
        return
    for channel, share in (("roll", -0.33), ("pitch", -0.25)):
        body = b.track("body", channel) or []
        torso = b.track("torso", channel) or []
        ticks = sorted(set(f[0] for f in body) | set(f[0] for f in torso))
        values = [(t, b.value_at("body", channel, t) + b.value_at("torso", channel, t)) for t in ticks]
        if values and max(abs(v) for _, v in values) >= 15:
            for t, v in values:
                b.set("head", channel, t, v * share, "inoutsine")


def p_off_arm(b, hits, name):
    if name in FIRST_PERSON:
        return
    for arm, other in (("rightArm", "leftArm"), ("leftArm", "rightArm")):
        swings = [h for h in hits if h[0] == arm and h[1] == "pitch"]
        if not swings or not all(still(b.track(other, ch)) for ch in LIMB_ANGLES):
            continue
        for part, channel, i in swings[:3]:
            frames = b.channels[(part, channel)]
            tick = frames[i][0]
            before = frames[i - 1][0]
            forward = frames[i][1] < frames[i - 1][1]
            b.set(other, "pitch", before, 0.0, "inoutsine")
            b.set(other, "pitch", tick, 28.0 if forward else -22.0, "outquad")
            b.set(other, "bend", tick, -35.0, "outquad")
            end = frames[i + 1][0] if i + 1 < len(frames) else tick + 4
            b.set(other, "pitch", end, 0.0, "inoutsine")
            b.set(other, "bend", end, 0.0, "inoutsine")


def p_breath(b, emote):
    if not emote.get("isLoop", False):
        return
    loop_len = emote.get("endTick", 0) - emote.get("returnTick", 0)
    start = emote.get("returnTick", 0)
    if loop_len < 16:
        return
    cycles = max(1, round(loop_len / 40))
    period = loop_len / cycles
    breath = (("torso", "pitch", 1.6), ("rightArm", "roll", 2.2), ("leftArm", "roll", -2.2), ("body", "y", 0.012), ("head", "pitch", -1.0))
    for part, channel, amp in breath:
        if not still(b.track(part, channel), 1e9 if False else 0.01) and part != "body":
            # an animated channel keeps its own motion; the breath rides only on still ones
            continue
        if part == "body" and not still(b.track(part, channel)):
            continue
        for k in range(cycles * 2 + 1):
            tick = round(start + k * period / 2)
            value = 0.0 if k % 2 == 0 else amp
            if part.endswith("Arm") and not still(b.track(part, "pitch")):
                value *= 0.5
            b.set(part, channel, min(tick, emote.get("endTick", tick)), value, "inoutsine", replace=True)


def polish(data, name):
    b = Builder(data)
    emote = b.emote
    paired = name.startswith(PAIRED)
    p_spine(b)
    p_easing(b)
    hits = strikes(b)
    if not paired:
        p_windup(b, hits, emote)
        p_snap(b, hits)
        p_weight(b, hits, emote, name)
        p_off_arm(b, hits, name)
        p_breath(b, emote)
    p_head(b)
    p_clearance(b)
    out = b.write()
    out["author"] = "HexHaki, reworked for HexPiece 2.1.1"
    return out


def main():
    jar = zipfile.ZipFile(os.path.join(ROOT, "input/hexhaki-2.1.0.jar"))
    prefix = "assets/hexhaki/player_animation/"
    hand = {}
    try:
        import anim_hand  # noqa: E402
        hand = anim_hand.ANIMATIONS
    except ImportError:
        pass
    os.makedirs(OUT, exist_ok=True)
    count = 0
    for entry in sorted(jar.namelist()):
        if not entry.startswith(prefix) or not entry.endswith(".json"):
            continue
        file = entry[len(prefix):]
        name = file[:-5]
        data = json.loads(jar.read(entry).decode("utf-8"))
        if name in hand:
            out = hand[name](data)
            b = Builder(out)
            p_clearance(b)
            out = b.write()
        else:
            out = polish(data, name)
        with open(os.path.join(OUT, file), "w", encoding="utf-8") as handle:
            json.dump(out, handle, indent=1)
            handle.write("\n")
        count += 1
    print(f"reworked {count} animations into {OUT}")


if __name__ == "__main__":
    main()
