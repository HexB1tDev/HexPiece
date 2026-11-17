#!/usr/bin/env python3
"""2.1.4: the Swordsman's animations (the owner: "Make Swordsman M1 attacks the most elaborate and polished melee animations in the
mod ... Ground the handling in iaido's draw-and-cut, follow-up cut and resheathing sequence").

Every beat is written as what the SWORD does: where the right fist is, where the blade points and where its edge faces; the right arm
and the wrist (the animation's rightItem rotation, which the game turns the held katana by) are solved for it on the game's own rig
(tools/sword_rig.py), so the blade goes exactly where the choreography says and the hand never lets go of it. A two-handed beat puts
the left fist on the handle under the right (solved the same way, on the drawn katana's own axis); a draw puts the right fist on the
handle of the sheathed katana (its frame as the saya holds it), and the left hand works the saya (the animation's leftItem moves the
saya at the hip, as the left hand pulls it back in a draw: saya-biki).

Writes src/main/resources/assets/hexhaki/player_animation/sword_<move>.json and the blade paths the server sweeps for hits
(src/main/java/com/hexhaki/piece/sword/BladePaths.java: the blade's base and tip every half tick, in the player's frame).

Coordinates for writing beats (the layer frame: the neck at 0, model pixels): H(right, down, forward) a point, D(right, up, forward) a
direction. Body: yaw + turns left (the right shoulder comes forward), bend + hunches forward (carries the head and the arms), x/z
blocks (z - forward), y blocks (+ up). Legs: pitch - forward, bend + flexes the knee.

Usage: python3 tools/sword_anims.py [--preview <dir>]
"""
import json
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_rig as rig  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
JAVA = os.path.join(ROOT, "src/main/java/com/hexhaki/piece/sword/BladePaths.java")


def H(right, down, forward):
    return (-right, down, -forward)


def D(right, up, forward):
    v = np.array((-right, -up, -forward), float)
    return tuple(v / np.linalg.norm(v))


def leg(pitch=0.0, roll=0.0, bend=0.0, yaw=0.0):
    return {"pitch": pitch, "roll": roll, "bend": bend, "yaw": yaw}


STANCE = {"rightLeg": leg(-4, 3, 10), "leftLeg": leg(6, -3, 12)}


class Beat:
    """One keyframe: the body, legs and head as given; the right arm by the blade (or given); the left arm by its job."""

    def __init__(self, tick, easing="inoutsine", body=None, legs=None, head=None, torso=None, blade=None, sheathed=False,
                 rarm=None, wrist=None, left=None, saya=None, elbow=None):
        self.tick, self.easing = tick, easing
        self.body = body or {}
        self.legs = legs if legs is not None else STANCE
        self.head = head or {}
        self.torso = torso or {}
        self.blade, self.sheathed = blade, sheathed
        self.rarm, self.wrist = rarm, wrist
        self.left, self.saya, self.elbow = left, saya or {}, elbow
        self.solved = None
        self.errors = (0.0, 0.0, 0.0)


def base_pose(beat):
    q = {"body": dict(beat.body), "torso": dict(beat.torso), "head": dict(beat.head), "leftItem": dict(beat.saya)}
    for part, values in beat.legs.items():
        q[part] = dict(values)
    return q


RELAXED_R = {"pitch": 2.0, "yaw": 0.0, "roll": 4.0, "bend": -6.0}
RELAXED_L = {"pitch": 2.0, "yaw": 0.0, "roll": -4.0, "bend": -6.0}


def solve(beats):
    """Arms and wrists for every beat, each seeded from the beat before (so the solution moves smoothly)."""
    seed_r = None
    seed_l = None
    for beat in beats:
        q = base_pose(beat)
        err_p = err_d = err_l = 0.0
        if beat.rarm is not None:
            q["rightArm"] = dict(beat.rarm)
            q["rightItem"] = dict(beat.wrist or {})
        elif getattr(beat, "noto", None) is not None:
            # the noto line: the saya turned about its mouth (never moved) to lie on the line from the koiguchi up across the front of
            # the body to just in front of the right shoulder; the blade slides along it, out of or into the saya's mouth
            m_body = rig.body_to_entity(q) @ rig.part_matrix(q, "torso", (0, 0, 0))
            pull = np.array(NOTO_PULL) * min(1.0, 0.35 + beat.noto / NOTO_OUT)
            mouth = rig.layer_from_entity(q, rig.point(m_body, np.array(rig.HIP_MOUTH) + pull))
            u_layer = unit(np.array(NOTO_P) - mouth)
            to_body = np.linalg.inv(m_body) @ rig.layer_to_entity(q)
            u_body = rig.direction(to_body, u_layer)
            q["leftItem"] = dict(rig.saya_turn(u_body, (0.0, -1.0, 0.0)), x=float(pull[0]), y=float(pull[1]), z=float(pull[2]))
            out = beat.noto
            slide = DRAW_SLIDE * max(0.0, min(1.0, 1.0 - out / NOTO_OUT))
            target, bdir, edir = rig.sheathed_grip(q, slide, out)
            # on the saya's line the blade must be exactly coaxial: its direction counts far more than the wrist's comfort
            arm, wr, err_p, err_d = rig.solve_blade(q, target, bdir, edir, seed=seed_r, slide=slide, dir_weight=20000.0,
                                                    wrist_limit=80.0)
            q["rightArm"], q["rightItem"] = arm, wr
            seed_r = [arm["pitch"], arm["yaw"], arm["roll"], arm["bend"], wr["pitch"], wr["yaw"], wr["roll"]]
        elif getattr(beat, "hilt", None) is not None:
            # the saya turned about its mouth (never moved) until the handle is where the right hand reaches it
            best = None
            for k in beat.hilt:
                hd = slerp(rig.rest_hilt(), rig.toward_right_hand(q), k)
                edge = np.array(getattr(beat, "edge_out", (0.0, -1.0, 0.18)), float)
                q["leftItem"] = rig.saya_turn(hd, edge)
                target, bdir, edir = rig.sheathed_grip(q, DRAW_SLIDE, beat.saya_out)
                arm, wr, ep, ed = rig.solve_blade(q, target, bdir, edir, seed=seed_r, slide=DRAW_SLIDE)
                score = ep + ed / 20.0
                if best is None or score < best[0] - 1e-6:
                    best = (score, dict(q["leftItem"]), arm, wr, ep, ed)
                if ep < 0.35 and ed < 6:
                    break
            _, q["leftItem"], arm, wr, err_p, err_d = best
            q["rightArm"], q["rightItem"] = arm, wr
            seed_r = [arm["pitch"], arm["yaw"], arm["roll"], arm["bend"], wr["pitch"], wr["yaw"], wr["roll"]]
        else:
            if beat.sheathed:
                q["rightArm"] = dict(RELAXED_R)
                target, bdir, edir = rig.sheathed_grip(q)
            else:
                target, bdir, edir = beat.blade
            arm, wr, err_p, err_d = rig.solve_blade(q, target, bdir, edir, seed=seed_r, elbow=beat.elbow,
                                                    slide=getattr(beat, "slide", 0.0))
            q["rightArm"], q["rightItem"] = arm, wr
            seed_r = [arm["pitch"], arm["yaw"], arm["roll"], arm["bend"], wr["pitch"], wr["yaw"], wr["roll"]]
        left = beat.left
        if left == "two":
            q["leftArm"], err_l = rig.solve_left_on_handle(q, seed=seed_l)
            seed_l = [q["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")]
        elif left == "saya":
            q["leftArm"], err_l = rig.solve_left_at(q, rig.saya_point(q, 2.5), seed=seed_l)
            seed_l = [q["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")]
        elif isinstance(left, dict):
            q["leftArm"] = dict(left)
        elif isinstance(left, tuple):
            q["leftArm"], err_l = rig.solve_left_at(q, left, seed=seed_l)
            seed_l = [q["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")]
        else:
            q["leftArm"] = dict(RELAXED_L)
        beat.solved = q
        beat.errors = (err_p, err_d, err_l)
    return beats


def report(name, beats):
    worst = max((b.errors for b in beats), key=lambda e: e[0] + e[1] / 10 + e[2])
    print(f"{name}: worst fist {worst[0]:.2f}px, blade {worst[1]:.1f} deg, left hand {worst[2]:.2f}px")
    for b in beats:
        if b.errors[0] > 0.6 or b.errors[1] > 6 or b.errors[2] > 0.8:
            print(f"   tick {b.tick}: fist {b.errors[0]:.2f}px blade {b.errors[1]:.1f}deg left {b.errors[2]:.2f}px")


# --- cut geometry -------------------------------------------------------------------------------------------------------------------

def unit(v):
    v = np.array(v, float)
    return v / np.linalg.norm(v)


def plane(p, q):
    """A cut's plane: the blade at angle a points cos(a) p + sin(a) q (p, q: D() directions; q made perpendicular to p)."""
    p = unit(p)
    q = np.array(q, float)
    q = unit(q - p * (q @ p))
    return p, q


def swing(pl, angle, centre, reach, lead=1, roll=0.0, offset=None):
    """The blade at angle (degrees) in plane pl, the fist reach px out along it from centre (H()), the edge leading the way the angle
    grows (lead +1) or shrinks (lead -1), turned roll degrees about the blade (a flat or an angled edge)."""
    p, q = pl
    a = math.radians(angle)
    b = math.cos(a) * p + math.sin(a) * q
    motion = -math.sin(a) * p + math.cos(a) * q
    e = motion * lead
    if roll:
        n = np.cross(b, e)
        r = math.radians(roll)
        e = math.cos(r) * e + math.sin(r) * n
    target = np.array(centre, float) + reach * b + (np.array(offset, float) if offset is not None else 0.0)
    return (tuple(target), tuple(b), tuple(e))


FORWARD = D(0, 0, 1)
UP = D(0, 1, 0)
RIGHT = D(1, 0, 0)
HORIZONTAL = plane(FORWARD, RIGHT)                # + toward the right
KESA = plane(FORWARD, D(0.75, 0.75, -0.1))        # + up over the right shoulder, - down to the left hip
GYAKU_KESA = plane(FORWARD, D(-0.75, 0.75, -0.1)) # + up over the left shoulder, - down to the right hip
RISING = plane(FORWARD, D(0.55, -0.85, 0))        # + down to the right (low), - up to the left (high)
VERTICAL = plane(FORWARD, UP)                     # + up and over the head, - down in front

# the drawn guard (chudan): both hands in front of the stomach, the point at an opponent's throat, the edge down
GUARD_BLADE = (H(0.5, 7.0, 6.0), D(0, 0.5, 1), D(0, -1, 0.5))
GUARD_BODY = {"bend": 6, "y": -0.04}
GUARD_LEGS = {"rightLeg": leg(-16, 3, 18), "leftLeg": leg(10, -3, 16)}


def guard(tick, easing="inoutsine", **extra):
    kw = dict(blade=GUARD_BLADE, left="two", body=GUARD_BODY, legs=GUARD_LEGS, head={"pitch": -4})
    kw.update(extra)
    return Beat(tick, easing, **kw)


# the right fist at the sheathed katana's handle: low on it (a Minecraft arm is short), the saya brought forward by the left hand
DRAW_GRIP = (H(-0.5, 8.6, 4.6), D(-0.62, -0.42, -1.0), D(0.05, 1, -0.25))
DRAW_SLIDE = 3.6


def sheathed_beat(tick, easing="inoutsine", grip=DRAW_GRIP, out=0.0, **kw):
    """The right fist on the sheathed katana's handle (or drawing it, out model px out of the saya); the saya held where the blade is."""
    b = Beat(tick, easing, blade=grip, **kw)
    b.slide = DRAW_SLIDE
    b.saya_out = out
    b.left = kw.get("left", "saya")
    return b


# --- writing ------------------------------------------------------------------------------------------------------------------------
ARM_CH = ("pitch", "yaw", "roll", "bend")
WRITE_PARTS = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "torso": ("pitch", "yaw", "roll"),
    "head": ("pitch", "yaw", "roll"),
    "rightArm": ARM_CH,
    "leftArm": ARM_CH,
    "rightLeg": ARM_CH,
    "leftLeg": ARM_CH,
    "rightItem": ("pitch", "yaw", "roll", "y"),
    "leftItem": ("x", "y", "z", "pitch", "yaw", "roll"),
}
MOVES = {}


class Move:
    """A written sword animation: its beats, the tick its blade leaves the saya (draw) or goes back in (sheath), its cut windows."""

    def __init__(self, name, duration, beats, description, draw=-1, sheath=-1, cuts=(), free_legs=False, loop=None, stop_extra=4):
        self.name, self.duration, self.beats, self.description = name, duration, beats, description
        self.draw, self.sheath, self.cuts, self.free_legs, self.loop, self.stop_extra = draw, sheath, cuts, free_legs, loop, stop_extra

    def drawn_at(self, tick):
        """Is the blade in the hand at tick (else in its saya)?"""
        if self.draw >= 0 and tick < self.draw:
            return False
        if self.sheath >= 0 and tick >= self.sheath:
            return False
        return True


SOLVE = True
ONLY = None


def _mix(a, b, u):
    keys = set(a) | set(b)
    return {k: a.get(k, 0.0) + (b.get(k, 0.0) - a.get(k, 0.0)) * u for k in keys}


def densify(beats):
    """Every tick between two blade beats keyed on the designed arc (the fist along its line, the blade and the edge turned along a
    great circle, the body, legs, head and left arm in step, the later beat's easing baked in), so the game only ever interpolates
    the solved angles across one tick and the arm can never swing through the face between distant keys."""
    import anim
    out = [beats[0]]
    for a, b in zip(beats, beats[1:]):
        gap = b.tick - a.tick
        dense = (gap > 1 and a.blade is not None and b.blade is not None and b.easing != "constant"
                 and a.rarm is None and b.rarm is None and getattr(a, "hilt", None) is None and getattr(b, "hilt", None) is None)
        if dense:
            for k in range(a.tick + 1, b.tick):
                u = anim.ease(b.easing, (k - a.tick) / gap)
                fist = tuple(np.array(a.blade[0]) * (1 - u) + np.array(b.blade[0]) * u)
                bd = slerp(a.blade[1], b.blade[1], u)
                ed = slerp(a.blade[2], b.blade[2], u)
                ed = ed - bd * (ed @ bd)
                left = a.left
                if isinstance(a.left, dict) and isinstance(b.left, dict):
                    left = _mix(a.left, b.left, u)
                elif u > 0.5:
                    left = b.left
                legs = {part: _mix(a.legs.get(part, {}), b.legs.get(part, {}), u) for part in set(a.legs) | set(b.legs)}
                mid = Beat(k, "linear", blade=(fist, tuple(bd), tuple(unit(ed))), left=left, body=_mix(a.body, b.body, u),
                           legs=legs, head=_mix(a.head, b.head, u), torso=_mix(a.torso, b.torso, u), saya=_mix(a.saya, b.saya, u))
                out.append(mid)
            b.easing = "linear"
        out.append(b)
    return out


def game_blade(move, q, t):
    """The katana's matrix (model px -> E) as the game draws it at tick t: in the hand, except while it slides along the saya's line
    (between its noto beats: drawn out or slid home), where it is laid exactly along the saya's curve, out the interpolated amount, so
    no frame between two ticks can put it through the saya's side (client/sword/SwordLayer does the same from BladePaths.notoOut)."""
    pairs = move.noto
    if pairs and move.drawn_at(t) and pairs[0][0] <= t <= pairs[-1][0]:
        for (t0, o0), (t1, o1) in zip(pairs, pairs[1:]):
            if t0 <= t <= t1:
                out = o0 + (o1 - o0) * ((t - t0) / max(1e-9, t1 - t0))
                return rig.saya_matrix(q) @ rig.arc(out)
    return rig.blade_matrix(q)


def write(move):
    MOVES[move.name] = move
    move.noto = sorted((beat.tick, beat.noto) for beat in move.beats if getattr(beat, "noto", None) is not None)
    if not SOLVE or (ONLY is not None and move.name not in ONLY):
        return move
    move.beats = densify(move.beats)
    solve(move.beats)
    report(move.name, move.beats)
    moves = []
    for beat in move.beats:
        q = beat.solved
        for part, channels in WRITE_PARTS.items():
            if move.free_legs and part in ("rightLeg", "leftLeg"):
                continue
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": beat.tick, "easing": beat.easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    emote = {"beginTick": 0, "endTick": move.duration, "stopTick": move.duration + move.stop_extra, "isLoop": move.loop is not None,
             "returnTick": move.loop or 0, "degrees": True, "easeBeforeKeyframe": True, "moves": moves}
    if move.loop is not None:
        emote["stopTick"] = move.duration
    data = {"version": 3, "name": move.name, "author": "HexPiece 2.1.4", "description": move.description, "emote": emote}
    with open(os.path.join(OUT, move.name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")
    MOVES[move.name] = move
    return move


def stance_write(name, beats, description):
    """An arms-only loop (the combat stance; client/StanceLayer plays it under every move): only the arms are in the file."""
    if not SOLVE:
        return
    solve(beats)
    report(name, beats)
    moves = []
    for beat in beats:
        for part in ("rightArm", "leftArm"):
            for channel in ARM_CH:
                moves.append({"tick": beat.tick, "easing": beat.easing, part: {channel: round(float(beat.solved[part].get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    duration = beats[-1].tick
    data = {"version": 3, "name": name, "author": "HexPiece 2.1.4", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration, "isLoop": True, "returnTick": 0, "degrees": True,
                      "easeBeforeKeyframe": True, "moves": moves}}
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


# --- reach and clearance ---------------------------------------------------------------------------------------------------------
SHOULDER_R = np.array((-5.0, 2.0, 0.0))
SHOULDER_L = np.array((5.0, 2.0, 0.0))
REACH = 8.1


def reach(target, shoulder=SHOULDER_R, limit=REACH):
    """A fist target pulled in to what a Minecraft arm reaches from its shoulder (the arm is short: 8.5 px to the fist's centre)."""
    t = np.array(target, float)
    v = t - shoulder
    n = np.linalg.norm(v)
    return tuple(shoulder + v * (limit / n)) if n > limit else tuple(t)


def slerp(a, b, t):
    a, b = unit(a), unit(b)
    d = max(-1.0, min(1.0, float(a @ b)))
    w = math.acos(d)
    if w < 1e-6:
        return a
    return (math.sin((1 - t) * w) * a + math.sin(t * w) * b) / math.sin(w)


def path(dirs, t):
    """A direction along a polyline of directions (each leg a great-circle arc), t 0..1 over the whole."""
    n = len(dirs) - 1
    k = min(n - 1, int(t * n))
    return slerp(dirs[k], dirs[k + 1], t * n - k)


def cut(dirs, fists, t, roll=0.0, lead=1):
    """A beat of a cut at t (0..1) along blade directions dirs and fist points fists (H(), polylines): the edge leads (lead -1: the
    blade moves back edge-last, a wind-up)."""
    b = path(dirs, t)
    eps = 0.02
    t0, t1 = max(0.0, t - eps), min(1.0, t + eps)
    motion = path(dirs, t1) - path(dirs, t0)
    motion = motion - b * (motion @ b)
    if np.linalg.norm(motion) < 1e-6:
        motion = np.cross(b, (0, 1.0, 0))
    e = unit(motion) * lead
    if roll:
        n = np.cross(b, e)
        r = math.radians(roll)
        e = math.cos(r) * e + math.sin(r) * n
    n = len(fists) - 1
    k = min(n - 1, int(t * n))
    u = t * n - k
    f = np.array(fists[k], float) * (1 - u) + np.array(fists[k + 1], float) * u
    return (reach(f), tuple(b), tuple(e))


def held(target, bdir, edir):
    return (reach(target), bdir, edir)


def head_box(pose):
    m = rig.layer_to_entity(pose) @ rig.part_matrix(pose, "head", (0, 0, 0))
    return m


def inside(m, p, lo, hi, margin):
    """Is E point p inside the box lo..hi (px) of frame m, shrunk by margin?"""
    q = rig.point(np.linalg.inv(m), p)
    return all(lo[i] + margin < q[i] < hi[i] - margin for i in range(3))


def clearance(move):
    """Every half tick of the written animation: no fist and no part of the blade inside the head, no blade through the torso."""
    import anim
    with open(os.path.join(OUT, move.name + ".json"), encoding="utf-8") as handle:
        a = anim.Anim(json.load(handle))
    problems = []
    t = 0.0
    while t <= move.duration + 1e-9:
        q = a.pose(t)
        hb = head_box(q)
        torso = rig.layer_to_entity(q) @ rig.part_matrix(q, "torso", (0, 0, 0))
        fist = rig.point(rig.layer_to_entity(q) @ rig.fist_frame(q, "right"), (0, 0, 0))
        if inside(hb, fist, (-4, -8, -4), (4, 0, 4), 0.3):
            problems.append((t, "right fist in the head"))
        if inside(hb, fist, (-4.5, -8.5, -9.5), (4.5, 1.0, -4.0), 0.0):
            problems.append((t, "right fist in front of the face"))
        if move.drawn_at(t):
            m = game_blade(move, q, t)
            for k in range(13):
                p = rig.point(m, (0, rig.BLADE_BASE + (rig.BLADE_TIP - rig.BLADE_BASE) * k / 12, 0))
                if inside(hb, p, (-4, -8, -4), (4, 0, 4), 0.2):
                    problems.append((t, "blade through the head"))
                    break
                if k >= 2 and inside(torso, p, (-4, 0, -2), (4, 12, 2), 0.4):
                    problems.append((t, "blade through the body"))
                    break
        t += 0.5
    for problem in anim.lint(a):
        problems.append(problem)
    return problems


# --- the poses the moves flow through (2.1.4, the owner: "the entire moveset is now right handed only, main stance looking like my
# image": one hand on the sword, always the right; the left hand balances, aims, or works the saya) -------------------------------
# The main stance (the image: the blade held low in the right hand, the arm hanging easy, the point down at the ground ahead and
# to the right): every cut starts from it and comes back to it.
STANCE_F, STANCE_D, STANCE_E = H(6.8, 10.0, 2.6), D(0.32, -0.62, 0.72), D(0.15, -0.75, -0.6)
LOW_STANCE = (STANCE_F, STANCE_D, STANCE_E)
LEGS_EASY = {"rightLeg": leg(-6, 3, 8), "leftLeg": leg(4, -3, 8)}
LEGS_BACK = {"rightLeg": leg(-10, 4, 22), "leftLeg": leg(20, -4, 26)}    # weight settling back into the wind-up
LEGS_LUNGE = {"rightLeg": leg(-34, 4, 44), "leftLeg": leg(22, -4, 30)}
LEGS_DEEP = {"rightLeg": leg(-44, 5, 60), "leftLeg": leg(28, -5, 44)}
LEGS_TUCK = {"rightLeg": leg(-46, 4, 80), "leftLeg": leg(-20, -4, 96)}
LEGS_REACH = {"rightLeg": leg(-20, 6, 30), "leftLeg": leg(14, -6, 48)}
# the left arm: relaxed, swung back behind a cut, out front aiming, out to the side for balance
L_REST = {"pitch": 6.0, "yaw": 0.0, "roll": -8.0, "bend": -10.0}
L_BACK = {"pitch": 42.0, "yaw": 0.0, "roll": -16.0, "bend": -18.0}
L_AIM = {"pitch": -70.0, "yaw": 8.0, "roll": -6.0, "bend": -22.0}
L_OUT = {"pitch": -30.0, "yaw": 0.0, "roll": -58.0, "bend": -18.0}
L_TUCK = {"pitch": -20.0, "yaw": 20.0, "roll": 10.0, "bend": -90.0}


def b(tick, easing, blade, left=None, body=None, legs=None, head=None, **kw):
    left = left or L_REST
    return Beat(tick, easing, blade=blade, left=dict(left) if isinstance(left, dict) else left, body=body or {}, legs=legs if legs is not None else LEGS_EASY,
                head=head or {}, **kw)


def stance_beat(tick, easing="inoutsine", body=None, legs=None, head=None):
    return b(tick, easing, held(*LOW_STANCE), L_REST, body or {"yaw": -6, "bend": 2}, legs if legs is not None else LEGS_EASY,
             head or {"yaw": 4})


def draw_grip(tick, easing, out=0.0, body=None, legs=None, head=None, left="saya", edge_out=False):
    """The right hand on the sheathed katana's handle (out: drawing it, model px out along the saya); the saya turned about its mouth
    (never moved) just enough for the hand to reach the handle; the left hand at the koiguchi."""
    beat = Beat(tick, easing, blade=None, left=left, body=body or {}, legs=legs if legs is not None else LEGS_EASY, head=head or {})
    beat.hilt = (0.45, 0.6, 0.75, 0.9, 1.0)
    beat.saya_out = out
    beat.slide = DRAW_SLIDE
    if edge_out:
        beat.edge_out = (0.55, -0.8, 0.0)   # the saya turned edge-out as the blade leaves it (koiguchi no kirikata)
    return beat


# the noto line passes this point (layer frame px: in front of the right shoulder, at the upper chest) and the koiguchi; NOTO_OUT is how
# far out of the saya the blade is when the right arm is stretched along it (the tip then just inside the mouth)
NOTO_P = (-5.0, 4.0, -4.5)
NOTO_OUT = 15.5
# the owner: "why not just make the player pull the sheath forward a bit and make it fit": the left hand draws the saya a little
# forward and in along the obi as the blade comes out of it or goes back in (body-part px: in, up, forward)
NOTO_PULL = (-1.2, -0.5, -2.2)


def noto(tick, easing, out, body=None, legs=None, head=None, left="saya"):
    """The katana on the noto line, out model px out of its saya (0: home)."""
    beat = Beat(tick, easing, blade=None, left=left, body=body or {}, legs=legs if legs is not None else LEGS_EASY, head=head or {})
    beat.noto = out
    return beat


DRAW_BODY = {"bend": 8, "yaw": 16, "y": -0.11}
DRAW_LEGS = {"rightLeg": leg(-32, 4, 46), "leftLeg": leg(18, -4, 36)}
EXIT = 7.0   # model px the blade slides out along the saya before it swings free (the arm's reach allows no more)


def strike(name, duration, wind, mid, end, cut_ticks, description, wind_body, end_body, wind_left=L_AIM, end_left=L_BACK,
           wind_legs=LEGS_BACK, end_legs=LEGS_DEEP, wind_tick=3, hold=2, head_wind=None, head_end=None, overshoot=None,
           cuts=None, start=None):
    """A heavy one-handed cut: from the low stance, a big wind-up (the hips coiled the other way), a held coil, then the strike in two
    ticks (fast out of the coil, the edge leading), an overshoot, a hold at the end of the cut (the weight of it), and an easy
    recovery into the stance. wind/mid/end: (fist, blade, edge) of the coil, the middle of the cut and its end."""
    lo, hi = cut_ticks
    beats = [start or stance_beat(0, "linear")]
    coil = dict(wind_body)
    beats.append(b(wind_tick, "inoutsine", held(*wind), wind_left, coil, wind_legs, head_wind or {}))
    deeper = dict(coil)
    deeper["yaw"] = coil.get("yaw", 0.0) * 1.12
    deeper["bend"] = coil.get("bend", 0.0) - 2.0
    beats.append(b(lo - 1, "outsine", held(*wind), wind_left, deeper, wind_legs, head_wind or {}))
    mid_body = {k: (coil.get(k, 0.0) + end_body.get(k, 0.0)) / 2 for k in set(coil) | set(end_body)}
    beats.append(b(lo, "insine", held(*mid), end_left, mid_body, end_legs, {}))
    beats.append(b(hi, "outexpo", held(*end), end_left, end_body, end_legs, head_end or {}))
    over = dict(end_body)
    over["yaw"] = end_body.get("yaw", 0.0) * 1.1
    over["bend"] = end_body.get("bend", 0.0) + 2.0
    beats.append(b(hi + 1, "outback", held(*(overshoot or end)), end_left, over, end_legs, head_end or {}))
    beats.append(b(hi + 1 + hold, "linear", held(*(overshoot or end)), end_left, over, end_legs, head_end or {}))
    beats.append(stance_beat(duration))
    return write(Move(name, duration, beats, description, cuts=cuts or ((lo, hi),)))


def moves():
    # ------------------------------------------------------------------------------------------------ the draws (from the saya)
    # Every draw: the hand on the hilt, the saya turned about its mouth onto the noto line (up across the front of the body to the
    # right shoulder), the blade drawn out of the koiguchi along it as the right arm stretches up and out (the left hand at the mouth,
    # saya-biki), and only when the point is at the mouth does it leave the saya, whipping into the cut.
    out_dir = D(-0.8, -0.56, 0.1)
    draw_rise = [out_dir, D(-0.3, -0.4, 1), D(0.4, 0.12, 0.92), D(0.95, 0.3, 0.1)]
    draw_rise_f = [H(10.0, 0.8, 5.2), H(6.6, 4.4, 7.6), H(4.8, 4.0, 7.8), H(8.6, 3.4, 3.0)]
    still = {"bend": 8, "yaw": 14, "y": -0.11}
    write(Move("sword_draw_cut", 13, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8, "y": -0.05}, legs=LEGS_EASY, head={"pitch": -6}),
        noto(1, "outsine", 0.0, body=still, legs=DRAW_LEGS, head={"pitch": -10}),
        noto(2, "insine", 7.0, body=still, legs=DRAW_LEGS, head={"pitch": -10}),
        noto(3, "linear", NOTO_OUT, body=dict(still, yaw=10), legs=DRAW_LEGS, head={"pitch": -8}),
        b(4, "linear", cut(draw_rise, draw_rise_f, 0.45), L_BACK, {"bend": 10, "yaw": 0, "y": -0.13, "z": -0.14}, LEGS_DEEP,
          {"pitch": -8}),
        b(5, "outexpo", cut(draw_rise, draw_rise_f, 1.0), L_BACK, {"bend": 6, "yaw": -40, "y": -0.12, "z": -0.18}, LEGS_DEEP,
          {"pitch": -6, "yaw": 22}),
        b(6, "outback", held(H(8.6, 3.0, 2.2), D(1, 0.32, -0.05), D(0.1, 0.1, -1)), L_BACK, {"bend": 6, "yaw": -46, "y": -0.12,
          "z": -0.18}, LEGS_DEEP, {"pitch": -6, "yaw": 24}),
        b(8, "linear", held(H(8.6, 3.0, 2.2), D(1, 0.32, -0.05), D(0.1, 0.1, -1)), L_BACK, {"bend": 6, "yaw": -46, "y": -0.12,
          "z": -0.18}, LEGS_DEEP, {"pitch": -6, "yaw": 24}),
        stance_beat(13, body={"yaw": -6, "bend": 2, "z": -0.1}),
    ], "Swordsman: the draw cut. Stillness, the hand on the hilt, then the blade drawn out of the koiguchi and up, straight into a "
       "rising cut across.", draw=2, cuts=((4, 5),)))

    # the dashing draw: low and far forward on the run, drawn into a rising cut from the left knee up past the right shoulder
    rise = [out_dir, D(-0.05, -0.1, 1), D(0.6, 0.5, 0.65), D(0.9, 0.55, -0.05)]
    rise_f = [H(10.0, 0.8, 5.2), H(4.6, 5.2, 7.8), H(5.0, 2.8, 6.8), H(6.8, 0.2, 3.6)]
    dash = {"bend": 14, "yaw": 16, "y": -0.16, "z": -0.1}
    write(Move("sword_advancing_draw", 14, [
        noto(0, "linear", 0.0, body={"bend": 14, "yaw": 10, "y": -0.08}, legs=LEGS_LUNGE, head={"pitch": -14}),
        noto(1, "outsine", 0.0, body=dash, legs=LEGS_DEEP, head={"pitch": -18}),
        noto(2, "insine", 7.0, body=dict(dash, z=-0.22), legs=LEGS_DEEP, head={"pitch": -18}),
        noto(3, "linear", NOTO_OUT, body=dict(dash, z=-0.3), legs=LEGS_DEEP, head={"pitch": -16}),
        b(4, "linear", cut(rise, rise_f, 0.3), L_BACK, {"bend": 18, "yaw": 8, "y": -0.16, "z": -0.38}, LEGS_DEEP, {"pitch": -14}),
        b(6, "outexpo", cut(rise, rise_f, 1.0), L_OUT, {"bend": -6, "yaw": -34, "y": -0.04, "z": -0.42}, LEGS_LUNGE,
          {"pitch": 4, "yaw": 18}),
        b(7, "outback", cut(rise, rise_f, 1.0), L_OUT, {"bend": -8, "yaw": -38, "y": -0.03, "z": -0.42}, LEGS_LUNGE,
          {"pitch": 4, "yaw": 20}),
        b(9, "linear", cut(rise, rise_f, 1.0), L_OUT, {"bend": -8, "yaw": -38, "y": -0.03, "z": -0.42}, LEGS_LUNGE,
          {"pitch": 4, "yaw": 20}),
        stance_beat(14, body={"yaw": -6, "bend": 2, "z": -0.3}),
    ], "Swordsman: the dashing draw. Low and far forward, the blade drawn on the run into a rising cut.", draw=2, cuts=((4, 6),)))

    write(Move("sword_aerial_draw", 12, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8}, legs=LEGS_TUCK, head={"pitch": -10}),
        noto(1, "insine", 7.0, body={"bend": 8, "yaw": 16}, legs=LEGS_TUCK, head={"pitch": -12}),
        noto(2, "linear", NOTO_OUT, body={"bend": 8, "yaw": 18}, legs=LEGS_TUCK, head={"pitch": -12}),
        b(3, "linear", cut(rise, rise_f, 0.3), L_BACK, {"bend": 10, "yaw": 6, "pitch": -6}, LEGS_TUCK, {"pitch": -8}),
        b(5, "outexpo", cut(rise, rise_f, 1.0), L_OUT, {"bend": -6, "yaw": -40, "pitch": -10}, LEGS_REACH, {"yaw": 20}),
        b(6, "outback", cut(rise, rise_f, 1.0), L_OUT, {"bend": -8, "yaw": -44, "pitch": -10}, LEGS_REACH, {"yaw": 22}),
        b(8, "linear", cut(rise, rise_f, 1.0), L_OUT, {"bend": -8, "yaw": -44, "pitch": -8}, LEGS_REACH, {"yaw": 22}),
        stance_beat(12, legs=LEGS_TUCK),
    ], "Swordsman: the falling draw, in the air: drawn up and across as the body turns.", draw=1, cuts=((3, 5),)))

    # ------------------------------------------------------------------------------------------------ the quick draw (Ctrl + M1)
    # The owner: "Control + left click does a quick draw katana slash forward (5 blocks)": crouched, the hand on the hilt, then the
    # body is gone down the line (the server carries it five blocks in four ticks) with the blade drawn and flung flat across behind
    # it; it ends far down the line, low, the blade held out behind to the right, still. From a drawn sword the same dash without the
    # draw (sword_quick_cut).
    dash_body = {"bend": 26, "yaw": 6, "y": -0.2, "z": -0.1}
    sweep = [out_dir, D(-0.6, -0.2, 0.8), D(0.3, -0.05, 1), D(0.95, -0.05, -0.2)]
    sweep_f = [H(10.0, 0.8, 5.2), H(4.0, 6.4, 7.6), H(5.0, 6.4, 7.6), H(8.4, 6.6, 1.0)]
    after = held(H(8.6, 7.2, -0.6), D(0.86, -0.18, -0.48), D(0.48, 0, 0.86))
    after_body = {"bend": 22, "yaw": -30, "y": -0.22, "z": 0.0}
    tail = [
        b(4, "linear", cut(sweep, sweep_f, 0.4), L_BACK, dict(dash_body, yaw=0), LEGS_DEEP, {"pitch": -14}),
        b(5, "linear", cut(sweep, sweep_f, 0.8), L_BACK, dict(dash_body, yaw=-14), LEGS_DEEP, {"pitch": -14}),
        b(6, "outexpo", cut(sweep, sweep_f, 1.0), L_OUT, after_body, LEGS_DEEP, {"pitch": -10, "yaw": 16}),
        b(7, "outback", after, L_OUT, after_body, LEGS_DEEP, {"pitch": -10, "yaw": 18}),
        b(11, "linear", after, L_OUT, after_body, LEGS_DEEP, {"pitch": -10, "yaw": 18}),
        stance_beat(16, body={"yaw": -6, "bend": 2}),
    ]
    write(Move("sword_quick_draw", 16, [
        noto(0, "linear", 0.0, body={"bend": 14, "yaw": 10, "y": -0.12}, legs=LEGS_DEEP, head={"pitch": -14}),
        noto(1, "outsine", 0.0, body={"bend": 18, "yaw": 12, "y": -0.16}, legs=LEGS_DEEP, head={"pitch": -16}),
        noto(2, "insine", 7.0, body={"bend": 18, "yaw": 12, "y": -0.18}, legs=LEGS_DEEP, head={"pitch": -16}),
        noto(3, "linear", NOTO_OUT, body={"bend": 18, "yaw": 10, "y": -0.2}, legs=LEGS_DEEP, head={"pitch": -14}),
    ] + [Beat(x.tick, x.easing, blade=x.blade, left=x.left, body=x.body, legs=x.legs, head=x.head) for x in tail],
        "Swordsman: the quick draw, down the line in a blink, the blade drawn and flung across behind.", draw=2, cuts=((3, 6),)))
    write(Move("sword_quick_cut", 16, [
        stance_beat(0, "linear"),
        b(2, "inoutsine", held(H(1.6, 7.4, 5.4), D(-0.85, -0.35, 0.4), D(0.4, 0, 0.9)), L_TUCK, dict(dash_body, yaw=26), LEGS_DEEP,
          {"pitch": -14}),
        b(3, "linear", held(H(1.6, 7.4, 5.4), D(-0.85, -0.35, 0.4), D(0.4, 0, 0.9)), L_TUCK, dict(dash_body, yaw=28), LEGS_DEEP,
          {"pitch": -14}),
    ] + [Beat(x.tick, x.easing, blade=x.blade, left=x.left, body=x.body, legs=x.legs, head=x.head) for x in tail],
        "Swordsman: the quick draw with the blade already out: down the line in a blink, flung across behind.", cuts=((3, 6),)))

    # ------------------------------------------------------------------------------------------------ the ground string
    strike("sword_kesa", 14,
           wind=(H(5.6, -4.6, 1.2), D(0.3, 0.45, -0.85), D(0.2, 0.85, 0.4)),
           mid=(H(4.0, 1.6, 7.6), D(0.15, 0.4, 1), D(0, -1, 0.4)),
           end=(H(1.6, 8.4, 5.6), D(-0.6, -0.68, 0.42), D(-0.55, 0.65, -0.5)),
           overshoot=(H(1.4, 8.8, 5.0), D(-0.66, -0.7, 0.28), D(-0.55, 0.65, -0.5)),
           cut_ticks=(5, 6), wind_body={"yaw": -42, "bend": -10, "y": 0.0}, end_body={"yaw": 34, "bend": 24, "y": -0.12, "z": -0.22},
           head_wind={"yaw": 18, "pitch": -4}, head_end={"yaw": -12, "pitch": 8},
           description="Swordsman: the kesa cut, from high over the right shoulder down through the left hip, the whole body behind it.")
    strike("sword_gyaku_kesa", 14,
           wind=(H(-0.6, 2.6, 5.4), D(-0.88, 0.42, -0.22), D(-0.2, 0.6, 0.78)),
           mid=(H(2.8, 2.6, 7.8), D(-0.1, 0.35, 1), D(0, -1, 0.35)),
           end=(H(7.4, 8.4, 3.8), D(0.66, -0.64, 0.38), D(0.6, 0.65, -0.45)),
           overshoot=(H(7.6, 8.8, 3.0), D(0.72, -0.64, 0.25), D(0.6, 0.65, -0.45)),
           cut_ticks=(5, 6), wind_body={"yaw": 46, "bend": -6}, end_body={"yaw": -36, "bend": 20, "y": -0.11, "z": -0.2},
           wind_left=L_TUCK, end_left=L_OUT, head_wind={"yaw": -16}, head_end={"yaw": 14, "pitch": 6},
           description="Swordsman: the reverse kesa, from high over the left shoulder down through the right hip.")
    strike("sword_yoko", 13,
           wind=(H(8.2, 5.4, -2.0), D(0.72, 0.12, -0.68), D(0.68, 0, 0.72)),
           mid=(H(4.6, 5.0, 8.0), D(0.2, 0.02, 1), D(-1, 0, 0.2)),
           end=(H(0.4, 5.6, 6.4), D(-0.92, 0.04, 0.38), D(-0.38, 0, -0.92)),
           overshoot=(H(0.0, 5.8, 5.8), D(-0.96, 0.04, 0.2), D(-0.2, 0, -0.98)),
           cut_ticks=(5, 6), wind_body={"yaw": -52, "bend": 4, "y": -0.05}, end_body={"yaw": 46, "bend": 10, "y": -0.08, "z": -0.18},
           head_wind={"yaw": 24}, head_end={"yaw": -20},
           description="Swordsman: the horizontal cut, right to left at the waist, the hips wound back and unwound through it.")
    strike("sword_gyaku_yoko", 13,
           wind=(H(0.6, 5.2, 4.2), D(-0.9, 0.1, -0.42), D(-0.42, 0, 0.9)),
           mid=(H(3.4, 5.0, 8.0), D(-0.15, 0.02, 1), D(1, 0, 0.15)),
           end=(H(8.4, 5.6, 3.2), D(0.94, 0.06, 0.32), D(0.32, 0, -0.94)),
           overshoot=(H(8.4, 5.6, 2.2), D(0.98, 0.06, 0.12), D(0.12, 0, -0.99)),
           cut_ticks=(5, 6), wind_body={"yaw": 50, "bend": 4, "y": -0.05}, end_body={"yaw": -46, "bend": 10, "y": -0.08, "z": -0.18},
           wind_left=L_TUCK, end_left=L_OUT, head_wind={"yaw": -22}, head_end={"yaw": 20},
           description="Swordsman: the return cut, a backhand from the left hip across to the right.")
    strike("sword_kiriage", 14,
           wind=(H(1.6, 9.0, 4.2), D(-0.55, -0.74, 0.38), D(0.45, -0.25, 0.86)),
           mid=(H(4.0, 4.8, 8.0), D(0.02, 0.0, 1), D(0, 1, 0)),
           end=(H(8.0, -1.0, 3.6), D(0.62, 0.76, 0.18), D(0.2, 0.2, -0.95)),
           overshoot=(H(8.2, -1.6, 2.8), D(0.66, 0.75, 0.0), D(0.2, 0.2, -0.95)),
           cut_ticks=(5, 6), wind_body={"yaw": 40, "bend": 22, "y": -0.14}, end_body={"yaw": -30, "bend": -12, "y": 0.02, "z": -0.16},
           wind_legs=LEGS_DEEP, end_legs=LEGS_LUNGE, wind_left=L_BACK, end_left=L_BACK,
           head_wind={"yaw": -14, "pitch": -6}, head_end={"yaw": 14, "pitch": -8},
           description="Swordsman: the rising cut, from the left knee up past the right shoulder, rising with it.")
    strike("sword_tsuki", 14,
           wind=(H(6.6, 7.6, -2.0), D(0.04, 0.08, 1), D(0, -1, 0.08)),
           mid=(H(4.0, 5.6, 6.0), D(0.0, 0.05, 1), D(0, -1, 0.05)),
           end=(H(2.2, 4.4, 8.2), D(-0.04, 0.03, 1), D(0, -1, 0.03)),
           overshoot=(H(2.0, 4.4, 8.4), D(-0.05, 0.02, 1), D(0, -1, 0.02)),
           cut_ticks=(5, 7), wind_body={"yaw": -38, "bend": 4, "y": -0.07, "z": 0.06},
           end_body={"yaw": 22, "bend": 14, "y": -0.14, "z": -0.46}, wind_left=L_AIM, end_left=L_BACK,
           head_wind={"yaw": 16}, head_end={"yaw": -8},
           description="Swordsman: the thrust, drawn back to the hip and driven in with a long lunge, the left hand sighting along it.")
    strike("sword_makko", 17,
           wind=(H(5.8, -6.2, 0.8), D(0.22, -0.32, -0.92), D(0.1, 0.94, -0.3)),
           mid=(H(5.4, -0.8, 6.6), D(0.12, 0.62, 0.78), D(0, -0.78, 0.62)),
           end=(H(3.0, 6.6, 7.2), D(0.04, -0.55, 0.84), D(0, -0.84, -0.55)),
           overshoot=(H(3.0, 7.8, 6.4), D(0.04, -0.78, 0.62), D(0, -0.62, -0.78)),
           cut_ticks=(7, 8), wind_tick=4, hold=3, wind_body={"yaw": -20, "bend": -16, "y": 0.02},
           end_body={"yaw": 6, "bend": 30, "y": -0.18, "z": -0.32}, wind_left=L_AIM, end_left=L_BACK,
           head_wind={"pitch": -10, "yaw": 8}, head_end={"pitch": 10},
           description="Swordsman: the heaven splitter, the finisher: raised high behind the shoulder, then straight down through the "
                       "centre with a step in.")

    # ------------------------------------------------------------------------------------------------ the moving string
    strike("sword_advancing_cut", 13,
           wind=(H(8.0, 7.6, -1.2), D(0.7, -0.2, -0.68), D(0.68, 0, 0.72)),
           mid=(H(4.6, 7.2, 7.6), D(0.2, -0.12, 1), D(-1, 0, 0.2)),
           end=(H(0.6, 7.6, 6.2), D(-0.9, -0.1, 0.4), D(-0.4, 0, -0.9)),
           cut_ticks=(5, 6), wind_body={"yaw": -44, "bend": 22, "y": -0.16, "z": -0.1},
           end_body={"yaw": 40, "bend": 30, "y": -0.2, "z": -0.46}, wind_legs=LEGS_DEEP, end_legs=LEGS_DEEP,
           head_wind={"yaw": 18, "pitch": -16}, head_end={"yaw": -16, "pitch": -20},
           description="Swordsman: the advancing cut, a long low step and a cut across at knee height at the end of it.")

    # the pivot cut: a full turn to the right on the front foot, the arm flung out flat, the blade sweeping everything round
    spin = held(H(9.0, 5.8, 1.6), D(0.98, 0.03, 0.18), D(0.18, 0, -0.98))
    write(Move("sword_pivot_cut", 15, [
        stance_beat(0, "linear"),
        b(3, "inoutsine", held(H(0.8, 5.6, 4.0), D(-0.9, 0.08, -0.42), D(-0.42, 0, 0.9)), L_TUCK, {"yaw": 52, "bend": 8, "y": -0.1},
          LEGS_DEEP, {"yaw": -20}),
        b(4, "outsine", held(H(0.8, 5.6, 4.0), D(-0.9, 0.08, -0.42), D(-0.42, 0, 0.9)), L_TUCK, {"yaw": 58, "bend": 6, "y": -0.11},
          LEGS_DEEP, {"yaw": -22}),
        b(5, "insine", held(H(5.0, 5.6, 7.6), D(0.25, 0.04, 1), D(1, 0, -0.25)), L_OUT, {"yaw": 10, "bend": 8, "y": -0.1}, LEGS_DEEP,
          {"yaw": -10}),
        b(7, "linear", spin, L_OUT, {"yaw": -110, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -8}),
        b(9, "linear", spin, L_OUT, {"yaw": -230, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -8}),
        b(10, "outsine", spin, L_OUT, {"yaw": -330, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -6}),
        b(11, "constant", held(H(8.6, 5.8, 2.6), D(0.96, 0.04, 0.28), D(0.28, 0, -0.96)), L_OUT, {"yaw": 22, "bend": 6, "y": -0.07},
          LEGS_LUNGE, {"yaw": -4}),
        b(12, "linear", held(H(8.6, 5.8, 2.6), D(0.96, 0.04, 0.28), D(0.28, 0, -0.96)), L_OUT, {"yaw": 18, "bend": 6, "y": -0.07},
          LEGS_LUNGE, {"yaw": -4}),
        stance_beat(15),
    ], "Swordsman: the pivot cut, a full turn on the front foot with the arm flung out flat, all round.", cuts=((5, 10),)))

    # ------------------------------------------------------------------------------------------------ the aerial string
    write(Move("sword_aerial_cross", 14, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(2, "inoutsine", held(H(5.6, -4.4, 1.2), D(0.3, 0.45, -0.85), D(0.2, 0.85, 0.4)), L_AIM, {"yaw": -36, "bend": -8}, LEGS_TUCK,
          {"yaw": 14}),
        b(3, "insine", held(H(4.0, 1.6, 7.6), D(0.15, 0.4, 1), D(0, -1, 0.4)), L_BACK, {"yaw": -6, "bend": 8}, LEGS_TUCK),
        b(4, "outexpo", held(H(1.6, 8.0, 5.6), D(-0.6, -0.68, 0.42), D(-0.55, 0.65, -0.5)), L_BACK, {"yaw": 28, "bend": 18},
          LEGS_TUCK, {"yaw": -10}),
        b(6, "inoutsine", held(H(-0.6, 2.6, 5.4), D(-0.88, 0.42, -0.22), D(-0.2, 0.6, 0.78)), L_TUCK, {"yaw": 40, "bend": -4},
          LEGS_TUCK, {"yaw": -14}),
        b(8, "insine", held(H(2.8, 2.6, 7.8), D(-0.1, 0.35, 1), D(0, -1, 0.35)), L_OUT, {"yaw": 6, "bend": 8}, LEGS_TUCK),
        b(9, "outexpo", held(H(7.4, 8.2, 3.8), D(0.66, -0.64, 0.38), D(0.6, 0.65, -0.45)), L_OUT, {"yaw": -34, "bend": 18},
          LEGS_REACH, {"yaw": 12}),
        b(10, "outback", held(H(7.6, 8.6, 3.0), D(0.72, -0.64, 0.25), D(0.6, 0.65, -0.45)), L_OUT, {"yaw": -38, "bend": 20},
          LEGS_REACH, {"yaw": 14}),
        stance_beat(14, legs=LEGS_TUCK),
    ], "Swordsman: the aerial cross, two diagonals crossing in the air, each its own cut.", cuts=((3, 4), (8, 9))))

    wheel = held(H(5.6, -1.0, 6.4), D(0.14, 0.66, 0.74), D(0, 0.74, -0.66))
    write(Move("sword_aerial_wheel", 15, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(2, "inoutsine", held(H(5.8, -6.0, 0.8), D(0.22, -0.32, -0.92), D(0.1, 0.94, -0.3)), L_AIM, {"pitch": 12, "bend": -12},
          LEGS_TUCK, {"pitch": -8}),
        b(4, "linear", wheel, L_BACK, {"pitch": -70, "bend": 12}, LEGS_TUCK),
        b(6, "linear", wheel, L_BACK, {"pitch": -180, "bend": 14}, LEGS_TUCK),
        b(8, "linear", wheel, L_BACK, {"pitch": -290, "bend": 12}, LEGS_TUCK),
        b(9, "outsine", held(H(3.0, 7.0, 7.0), D(0.04, -0.55, 0.84), D(0, -0.84, -0.55)), L_BACK, {"pitch": -352, "bend": 22},
          LEGS_REACH),
        b(10, "constant", held(H(3.0, 7.0, 7.0), D(0.04, -0.55, 0.84), D(0, -0.84, -0.55)), L_BACK, {"pitch": 8, "bend": 22},
          LEGS_REACH),
        b(11, "linear", held(H(3.0, 7.6, 6.6), D(0.04, -0.7, 0.7), D(0, -0.7, -0.7)), L_BACK, {"bend": 22}, LEGS_REACH),
        stance_beat(15, legs=LEGS_TUCK),
    ], "Swordsman: the wheel, a forward somersault with the blade carried round a full vertical circle.", cuts=((4, 9),)))

    write(Move("sword_plunge", 17, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(3, "inoutsine", held(H(7.6, -5.2, 1.6), D(0.25, 0.93, 0.25), D(0, -0.25, 1)), L_AIM, {"bend": -14, "pitch": 8}, LEGS_TUCK,
          {"pitch": -12}),
        b(5, "outsine", held(H(7.7, -5.6, 1.2), D(0.25, 0.95, 0.18), D(0, -0.2, 1)), L_AIM, {"bend": -16, "pitch": 8}, LEGS_TUCK,
          {"pitch": -12}),
        b(6, "insine", held(H(4.8, 1.0, 6.8), D(0.1, -0.2, 1), D(0, -1, -0.2)), L_BACK, {"bend": 8}, LEGS_TUCK, {"pitch": 4}),
        b(7, "outexpo", held(H(3.4, 6.8, 6.6), D(0.03, -0.96, 0.28), D(0, 0.28, 0.96)), L_OUT, {"bend": 28, "y": -0.22}, LEGS_DEEP,
          {"pitch": 16}),
        b(9, "outback", held(H(3.2, 8.0, 6.0), D(0.02, -1, 0.1), D(0, 0.1, 1)), L_OUT, {"bend": 32, "y": -0.32}, LEGS_DEEP,
          {"pitch": 18}),
        b(12, "linear", held(H(3.2, 8.0, 6.0), D(0.02, -1, 0.1), D(0, 0.1, 1)), L_OUT, {"bend": 30, "y": -0.3}, LEGS_DEEP,
          {"pitch": 16}),
        stance_beat(17),
    ], "Swordsman: the plunge, the aerial finisher: raised high, then driven point-down as the body drops onto it.",
        cuts=((6, 9),)))

    # ------------------------------------------------------------------------------------------------ stance, charge, release, sheathe
    write(Move("sword_guard", 24, [
        stance_beat(0, "linear"),
        b(12, "inoutsine", held(H(6.8, 9.6, 2.6), D(0.32, -0.6, 0.73), STANCE_E), L_REST, {"yaw": -6, "bend": 1}, LEGS_EASY,
          {"yaw": 4}),
        stance_beat(24),
    ], "Swordsman: the main stance, the blade held low in the right hand, the point down at the ground ahead (walking keeps its "
       "legs).", free_legs=True, loop=0))

    charge_pose = held(H(7.6, -5.8, 0.6), D(0.3, -0.28, -0.91), D(0.1, 0.95, -0.28))
    charge_body = {"yaw": -26, "bend": -16, "y": -0.04}
    charge_legs = {"rightLeg": leg(8, 6, 26), "leftLeg": leg(-24, -5, 34)}
    shake = held(H(7.7, -6.0, 0.4), D(0.32, -0.26, -0.91), D(0.1, 0.95, -0.28))
    write(Move("sword_charge", 26, [
        stance_beat(0, "linear"),
        b(2, "inoutsine", held(H(9.6, 3.0, 2.4), D(0.75, 0.3, -0.6), D(0.2, 0.9, 0.3)), L_AIM, dict(charge_body, bend=-6), charge_legs,
          {"pitch": -6, "yaw": 8}),
        b(5, "outsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(6, "linear", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(16, "inoutsine", shake, L_AIM, dict(charge_body, bend=-18, y=-0.06), charge_legs, {"pitch": -10, "yaw": 10}),
        b(26, "inoutsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
    ], "Swordsman: charging the slash, the blade raised high behind the right shoulder, the body drawn back like a bow (held until "
       "release).", loop=6))

    write(Move("sword_charge_draw", 26, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8, "y": -0.05}, head={"pitch": -8}),
        noto(1, "insine", 7.0, body={"bend": 8, "yaw": 12, "y": -0.06}, legs=DRAW_LEGS, head={"pitch": -8}),
        noto(2, "linear", NOTO_OUT, body={"bend": 8, "yaw": 12, "y": -0.06}, legs=DRAW_LEGS, head={"pitch": -6}),
        b(3, "outexpo", held(H(8.0, -1.0, 4.4), D(0.35, 0.92, 0.15), D(0, -0.15, 1)), L_BACK, {"yaw": 0, "bend": -4}, LEGS_EASY),
        b(5, "outsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(6, "linear", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(16, "inoutsine", shake, L_AIM, dict(charge_body, bend=-18, y=-0.06), charge_legs, {"pitch": -10, "yaw": 10}),
        b(26, "inoutsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
    ], "Swordsman: drawing straight up into the charge.", draw=1, loop=6))

    write(Move("sword_release", 15, [
        b(0, "linear", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(1, "insine", held(H(5.4, -0.8, 6.6), D(0.12, 0.62, 0.78), D(0, -0.78, 0.62)), L_BACK, {"yaw": -6, "bend": 2, "z": -0.1},
          LEGS_LUNGE, {"pitch": -2}),
        b(2, "outexpo", held(H(3.0, 6.6, 7.2), D(0.04, -0.55, 0.84), D(0, -0.84, -0.55)), L_BACK, {"yaw": 6, "bend": 30, "y": -0.18,
          "z": -0.34}, LEGS_DEEP, {"pitch": 10}),
        b(3, "outback", held(H(3.0, 7.8, 6.4), D(0.04, -0.78, 0.62), D(0, -0.62, -0.78)), L_BACK, {"yaw": 6, "bend": 33, "y": -0.2,
          "z": -0.36}, LEGS_DEEP, {"pitch": 12}),
        b(7, "linear", held(H(3.0, 7.8, 6.4), D(0.04, -0.78, 0.62), D(0, -0.62, -0.78)), L_BACK, {"yaw": 6, "bend": 32, "y": -0.2,
          "z": -0.36}, LEGS_DEEP, {"pitch": 12}),
        stance_beat(15, body={"yaw": -6, "bend": 2, "z": -0.3}),
    ], "Swordsman: the charged slash released: straight down from high behind the shoulder, the step in behind it.",
        cuts=((1, 3),)))

    # 2.2.1 (the owner: "the sheath and unsheath is too fast. Slow those animations down. Don't make the blade usable until it's fully
    # unsheathed ... It should be the only drawback from having the swordsman ... Unless it's a quick draw"): the draw, slow and
    # deliberate: the right hand settles on the hilt, the left thumb pushes the guard out (koiguchi wo kiru), the blade slides out of
    # the saya along the noto line a little at a time, and only when its point is at the mouth does it swing free, out to the right and
    # down into the low guard. Nothing is cut and nothing can be done with it until it is there.
    calm = {"bend": 5, "yaw": 8, "y": -0.04}
    write(Move("sword_draw", 22, [
        stance_beat(0, "linear"),
        noto(3, "inoutsine", 0.0, body=calm, legs=LEGS_EASY, head={"pitch": -6}),
        noto(6, "outsine", 1.2, body=calm, legs=LEGS_EASY, head={"pitch": -8}),
        noto(8, "linear", 1.2, body=calm, legs=LEGS_EASY, head={"pitch": -8}),
        noto(13, "inoutsine", 8.0, body=dict(calm, yaw=10), legs=DRAW_LEGS, head={"pitch": -8}),
        noto(16, "outsine", NOTO_OUT, body=dict(calm, yaw=8), legs=DRAW_LEGS, head={"pitch": -6}),
        b(18, "outsine", held(H(8.6, 3.0, 2.2), D(1, 0.32, -0.05), D(0.1, 0.1, -1)), L_REST, {"yaw": -10, "bend": 4}, LEGS_EASY,
          {"pitch": -4, "yaw": 8}),
        stance_beat(22, "inoutsine"),
    ], "Swordsman: the draw, slow and deliberate: the thumb pushes the guard out, the blade slides out of the saya, swings free and "
       "settles into the low guard.", draw=16))

    # sheathing (noto), as slow: the blade flicked clean (chiburi), held a beat, its back laid to the koiguchi, slid home along the saya
    # a little at a time, the guard clicking home at the end
    write(Move("sword_sheathe", 20, [
        stance_beat(0, "linear"),
        b(3, "outexpo", held(H(5.6, 8.8, 5.4), D(0.12, -0.78, 0.6), D(0.05, -0.6, -0.8)), L_REST, {"yaw": -4, "bend": 6},
          LEGS_EASY),
        b(5, "linear", held(H(5.6, 8.8, 5.4), D(0.12, -0.78, 0.6), D(0.05, -0.6, -0.8)), L_REST, {"yaw": -4, "bend": 6},
          LEGS_EASY),
        noto(8, "inoutsine", NOTO_OUT, body={"bend": 6, "yaw": 6}),
        noto(10, "linear", NOTO_OUT, body={"bend": 6, "yaw": 6}),
        noto(15, "inoutsine", 3.0, body={"bend": 6, "yaw": 6}),
        noto(17, "outsine", 0.0, body={"bend": 5, "yaw": 6}),
        Beat(20, "inoutsine", rarm=dict(RELAXED_R), wrist={}, left="saya", body={"bend": 3}, legs=LEGS_EASY),
    ], "Swordsman: the blade flicked clean, laid to the mouth of the saya and slid home, slowly.", sheath=17, stop_extra=6))


def stance():
    """The owner: "whenever combat mode is activated the left hand should rest on the sword handle on a relaxed pose (when not
    moving)": the left hand on the sheathed katana's hilt at the hip, the right arm loose; a slow breath. Arms only (the stance layer)."""
    q = {"leftItem": {}}
    hilt = rig.saya_point(q, -1.4)
    hilt = tuple(np.array(hilt) + np.array((0.2, -0.9, -0.2)))
    beats = [
        Beat(0, "linear", rarm=dict(RELAXED_R), wrist={}, left=hilt, legs={}),
        Beat(20, "inoutsine", rarm={"pitch": 0.5, "yaw": 0.0, "roll": 5.0, "bend": -8.0}, wrist={},
             left=tuple(np.array(hilt) + np.array((0, -0.3, 0))), legs={}),
        Beat(40, "inoutsine", rarm=dict(RELAXED_R), wrist={}, left=hilt, legs={}),
    ]
    stance_write("style_swordsman_stance", beats, "Swordsman combat stance: the left hand resting on the hilt at the hip, the "
                 "right arm loose.")


def java_paths():
    """src/main/java/com/hexhaki/piece/sword/BladePaths.java: for every sword animation, where the blade is every half tick (its base
    and its tip, blocks, in the player's frame: right, up, forward from the feet, before the player's own turn), the tick the blade
    leaves the saya and the tick it goes back in. The server sweeps cuts along these (the very keyframes every client plays)."""
    import anim
    names = sorted(MOVES)
    lines = []
    cases, draws, sheaths, notos = [], [], [], []
    for i, name in enumerate(names):
        move = MOVES[name]
        with open(os.path.join(OUT, name + ".json"), encoding="utf-8") as handle:
            a = anim.Anim(json.load(handle))
        values = []
        k = 0
        while k <= move.duration * 2:
            q = a.pose(k / 2.0)
            gm = game_blade(move, q, k / 2.0)
            base, tip = rig.point(gm, (0, rig.BLADE_BASE, 0)), rig.point(gm, (0, rig.BLADE_TIP, 0))
            for p in (base, tip):
                values += [p[0], p[1], -p[2]]
            k += 1
        cases.append(f'            case "{name}" -> p{i}();')
        if move.noto:
            notos.append(f'            case "{name}" -> new float[] {{' + ", ".join(f"{t}F, {o:.2f}F" for t, o in move.noto) + "};")
        draws.append(f'            case "{name}" -> {move.draw};')
        sheaths.append(f'            case "{name}" -> {move.sheath};')
        body = ", ".join(f"{v:.3f}F" for v in values)
        lines.append(f"    /** {name} ({move.duration} ticks) */\n    private static float[] p{i}() {{\n        return new float[] {{{body}}};\n    }}\n")
    src = ("package com.hexhaki.piece.sword;\n\n/**\n * Generated by tools/sword_anims.py (do not edit): every sword animation's blade, every half tick, from the very keyframes the\n"
           " * clients play: its base and its tip (blocks: right, up, forward of the feet, the player's own turn left out), 6 floats a half\n"
           " * tick; the tick the blade leaves the saya (draw) and the tick it is back in (sheath), -1 when it does not.\n */\n"
           "public final class BladePaths {\n    private BladePaths() {\n    }\n\n    public static float[] of(String animation) {\n"
           "        return switch (animation) {\n" + "\n".join(cases) + "\n            default -> null;\n        };\n    }\n\n"
           "    public static int drawTick(String animation) {\n        return switch (animation) {\n" + "\n".join(draws)
           + "\n            default -> -1;\n        };\n    }\n\n    public static int sheathTick(String animation) {\n"
           "        return switch (animation) {\n" + "\n".join(sheaths) + "\n            default -> -1;\n        };\n    }\n\n"
           "    /** The noto beats (tick, model px out of the saya): between the first and the last, the blade is laid along the saya. */\n"
           "    public static float[] notoOut(String animation) {\n        return switch (animation) {\n" + "\n".join(notos)
           + "\n            default -> null;\n        };\n    }\n\n"
           + "\n".join(lines) + "}\n")
    with open(JAVA, "w", encoding="utf-8") as handle:
        handle.write(src)
    print(f"{JAVA}: {len(names)} blade paths")


NPC_PARTS = ("body", "head", "rightArm", "leftArm", "rightLeg", "leftLeg", "rightItem", "leftItem")
NPC_CHANNELS = ("x", "y", "z", "pitch", "yaw", "roll", "bend")
NPC_EASINGS = ("linear", "constant", "insine", "outsine", "inoutsine", "inquad", "outquad", "inoutquad", "incubic", "outcubic", "inoutcubic",
               "inexpo", "outexpo", "inoutexpo", "inback", "outback", "inoutback")


def java_npc_poses():
    """src/main/java/com/hexhaki/piece/client/npc/SwordPoses.java: the sword animations for NPCs (playerAnimator animates players only),
    packed as client/npc/StylePoses packs the styles', with the wrist (rightItem, part 6) and the saya (leftItem, part 7) too."""
    out = ["package com.hexhaki.piece.client.npc;", "", "/**",
           " * Generated by tools/sword_anims.py (do not edit): every sword animation's keyframes, for NPCs, packed as StylePoses packs the",
           " * styles' (part, channel, keyframe count, then tick, value, easing each); parts " + ", ".join(NPC_PARTS) + ".",
           " */", "final class SwordPoses {", "    private SwordPoses() {", "    }", "", "    static float[] of(String name) {",
           "        return switch (name) {"]
    names = sorted(MOVES) + ["style_swordsman_stance"]
    for i, name in enumerate(names):
        out.append(f'            case "{name}" -> s{i}();')
    out += ["            default -> null;", "        };", "    }", ""]
    for i, name in enumerate(names):
        with open(os.path.join(OUT, name + ".json"), encoding="utf-8") as handle:
            data = json.load(handle)
        tracks = {}
        for move in data["emote"]["moves"]:
            for part in NPC_PARTS:
                if part in move:
                    for channel, value in move[part].items():
                        if channel in NPC_CHANNELS:
                            tracks.setdefault((part, channel), {})[move["tick"]] = (value, move["easing"])
        values = []
        for (part, channel), frames in sorted(tracks.items()):
            if all(abs(v) < 1e-6 for v, _ in frames.values()):
                continue
            values += [NPC_PARTS.index(part), NPC_CHANNELS.index(channel), len(frames)]
            for tick in sorted(frames):
                value, easing = frames[tick]
                values += [tick, round(value, 2), NPC_EASINGS.index(easing) if easing in NPC_EASINGS else 0]
        body = ", ".join(f"{v}F" if isinstance(v, float) else str(v) for v in values)
        out += [f"    /** {name} ({data['emote']['endTick']} ticks) */", f"    private static float[] s{i}() {{",
                f"        return new float[] {{{body}}};", "    }", ""]
    out.append("}")
    path = os.path.join(ROOT, "src/main/java/com/hexhaki/piece/client/npc/SwordPoses.java")
    with open(path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(out) + "\n")


def main():
    global ONLY
    if "--only" in sys.argv:
        ONLY = set(sys.argv[sys.argv.index("--only") + 1].split(","))
    moves()
    stance()
    java_paths()
    java_npc_poses()
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        os.makedirs(out, exist_ok=True)
        names = sorted(MOVES)
        for i in range(0, len(names), 3):
            preview(names[i:i + 3], os.path.join(out, f"sword-{i // 3 + 1}.png"))
    bad = 0
    for move in MOVES.values():
        if ONLY is not None and move.name not in ONLY:
            continue
        problems = clearance(move)
        if problems:
            bad += 1
            print(f"  {move.name}: {len(problems)} problems, first {problems[:3]}")
    print(f"{len(MOVES)} sword animations, {bad} with problems")


def preview(names, out, frames=9, views=("front", "three", "side"), key="crimson_dawn", tile=(170, 190), scale=58.0):
    """A contact sheet: each move a band of rows (one per view), each column a moment; the blade drawn in the hand or the saya as
    the game shows it at that moment."""
    import anim
    import sword_view as sv
    from PIL import Image, ImageDraw
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180, "left": -90}
    tw, th = tile
    bands = []
    for name in names:
        with open(os.path.join(OUT, name + ".json"), encoding="utf-8") as handle:
            a = anim.Anim(json.load(handle))
        move = MOVES.get(name)
        length = a.end if (move is None or move.loop is None) else a.end
        ticks = [round(length * i / (frames - 1) * 2) / 2 for i in range(frames)]
        band = Image.new("RGB", (tw * frames, th * len(views) + 16), (236, 234, 228))
        d = ImageDraw.Draw(band)
        d.text((4, 2), f"{name}  ({length} ticks)" + (f"  cuts {move.cuts}" if move and move.cuts else ""), fill=(20, 20, 20))
        for c, t in enumerate(ticks):
            q = a.pose(t)
            for r, view in enumerate(views):
                cam = sv.Camera(yaw_of[view], 10.0, scale, tw / 2, th * 0.9)
                cv = sv.Canvas(tw, th)
                sv.draw_figure(cv, cam, q)
                drawn = move.drawn_at(t) if move else False
                if drawn:
                    sv.draw_katana(cv, cam, key, game_blade(move, q, t))
                    sv.draw_katana(cv, cam, key, rig.saya_matrix(q), saya=True, blade=False)
                else:
                    sv.draw_katana(cv, cam, key, rig.saya_matrix(q), saya=True, blade=True, shine=0.3)
                img = cv.image()
                dd = ImageDraw.Draw(img)
                dd.line([(0, th * 0.9), (tw, th * 0.9)], fill=(190, 190, 190))
                in_cut = move and any(lo <= t <= hi for lo, hi in move.cuts)
                dd.text((3, 2), f"t{t:g}" + (" CUT" if in_cut else ""), fill=(200, 40, 40) if in_cut else (80, 80, 80))
                band.paste(img, (c * tw, 16 + r * th))
        bands.append(band)
    sheet = Image.new("RGB", (max(b.width for b in bands), sum(b.height for b in bands)), (236, 234, 228))
    y = 0
    for band in bands:
        sheet.paste(band, (0, y))
        y += band.height
    sheet.save(out)



if __name__ == "__main__":
    main()
