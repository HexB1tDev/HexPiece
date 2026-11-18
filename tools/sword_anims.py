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


class Blade:
    """2.3.2: a blade the moves are written for: its mesh (key), the suffix of its animations' names, its point (model px: up the blade
    and out to its back, the curve), how far down the handle the hand slides to draw it, and its home pose (where every cut starts and
    ends: the fist, the blade, the edge; set below). The owner: "yoru clips through the floor": every pose of a blade on the ground
    keeps its point off the floor ({@link lift_clear})."""

    def __init__(self, name, key, suffix, tip, tip_x, draw_slide):
        self.name, self.key, self.suffix, self.tip, self.tip_x, self.draw_slide = name, key, suffix, tip, tip_x, draw_slide
        self.home = None


KATANA = Blade("katana", "crimson_dawn", "", 20.9, 1.23, 3.6)
GRYPHON = Blade("gryphon", "gryphon", "_gryphon", 22.44, 3.2, 3.0)
YORU = Blade("yoru", "yoru", "_yoru", 32.2, 3.07, 0.0)
PROFILE = KATANA
# the point is kept this far (blocks) over the floor the feet stand on
FLOOR = 0.12


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


def _seed(arm, wr):
    return [arm["pitch"], arm["yaw"], arm["roll"], arm["bend"], wr["pitch"], wr["yaw"], wr["roll"]]


def solve_right(beat, q, seed_r, ground, between=None, starts=None):
    """The right arm and wrist for one beat (q: its base pose; the saya's leftItem set in it where the beat turns the saya), seeded
    from the beat before; between and starts as rig.solve_blade's. Returns (rightArm, rightItem, error px, error degrees)."""
    kw = {"between": between, "starts": starts}
    if beat.rarm is not None:
        return dict(beat.rarm), dict(beat.wrist or {}), 0.0, 0.0
    if getattr(beat, "noto", None) is not None:
        # the noto line: the saya turned about its mouth (never moved) to lie on the line from the koiguchi up across the front of
        # the body to just in front of the right shoulder; the blade slides along it, out of or into the saya's mouth
        (target, bdir, edir), saya, slide = noto_frame(beat)
        q["leftItem"] = saya
        # on the saya's line the blade must be exactly coaxial: its direction counts far more than the wrist's comfort
        return rig.solve_blade(q, target, bdir, edir, seed=seed_r, slide=slide, dir_weight=20000.0, wrist_limit=80.0, **kw)
    if getattr(beat, "fist", None) is not None:
        # 2.3.2: the empty right hand on its way somewhere (to the hilt, or away from it): the fist alone, the wrist easy
        arm, wr, err_p, _ = rig.solve_blade(q, beat.fist, (0.0, 1.0, 0.0), (1.0, 0.0, 0.0), seed=seed_r, dir_weight=0.0,
                                            edge_weight=0.0, **kw)
        return arm, wr, err_p, 0.0
    if getattr(beat, "back_lift", None) is not None:
        # 2.2.7: the right fist on Yoru's handle on the back (lifted back_lift px up along itself out of its ropes), or reaching
        # for it (reach_off: the fist that many px away from the handle, the sword not yet in it)
        target, bdir, edir = rig.back_grip(q, beat.back_lift)
        target = tuple(np.array(target) + np.array(getattr(beat, "reach_off", (0.0, 0.0, 0.0))))
        # 2.3.2: reached from above: the elbow up beside the head, the forearm folded back behind it, the hand down onto the hilt
        return rig.solve_blade(q, target, bdir, edir, seed=seed_r, dir_weight=4000.0, edge_weight=1500.0, wrist_limit=80.0,
                               upper=OVERHEAD, **kw)
    if getattr(beat, "hilt", None) is not None:
        # the saya turned about its mouth (never moved) until the handle is where the right hand reaches it
        best = None
        for k in beat.hilt:
            hd = slerp(rig.rest_hilt(), rig.toward_right_hand(q), k)
            edge = np.array(getattr(beat, "edge_out", (0.0, -1.0, 0.18)), float)
            q["leftItem"] = rig.saya_turn(hd, edge)
            target, bdir, edir = rig.sheathed_grip(q, PROFILE.draw_slide, beat.saya_out, PROFILE.key)
            arm, wr, ep, ed = rig.solve_blade(q, target, bdir, edir, seed=seed_r, slide=PROFILE.draw_slide, **kw)
            score = ep + ed / 20.0
            if best is None or score < best[0] - 1e-6:
                best = (score, dict(q["leftItem"]), arm, wr, ep, ed)
            if ep < 0.35 and ed < 6:
                break
        _, q["leftItem"], arm, wr, err_p, err_d = best
        return arm, wr, err_p, err_d
    if beat.sheathed:
        q["rightArm"] = dict(RELAXED_R)
        target, bdir, edir = rig.sheathed_grip(q, 0.0, 0.0, PROFILE.key)
    else:
        target, bdir, edir = beat.blade
    sl, up = getattr(beat, "slide", 0.0), getattr(beat, "upper", None)
    arm, wr, err_p, err_d = rig.solve_blade(q, target, bdir, edir, seed=seed_r, elbow=beat.elbow, slide=sl, upper=up, **kw)
    for _ in range(4):
        if not ground or beat.sheathed:
            break
        h = rig.point(rig.blade_matrix(dict(q, rightArm=arm, rightItem=wr)), tip_point(PROFILE))[1]
        if h >= FLOOR * 0.6:
            break
        lift_clear(beat, PROFILE, FLOOR + (FLOOR - h) + 0.04)
        target, bdir, edir = beat.blade
        arm, wr, err_p, err_d = rig.solve_blade(q, target, bdir, edir, seed=seed_r, elbow=beat.elbow, slide=sl, upper=up, **kw)
    return arm, wr, err_p, err_d


def solve_left(beat, q, seed_l, prev=None, mid=None):
    """mid: (the pose half a tick after the beat before, how far (eased) the game is from it to this beat there)"""
    """The left arm for one beat (q: its pose with the right arm solved) by its job; prev: the left arm the beat before. Returns
    (leftArm, error px)."""
    left = beat.left
    if left == "two":
        return rig.solve_left_on_handle(q, seed=seed_l)
    if left == "saya":
        return rig.solve_left_at(q, rig.saya_point(q, 2.5), seed=seed_l)
    if isinstance(left, dict):
        if beat.blade is not None or getattr(beat, "noto", None) is not None:
            return rig.clear_left(q, dict(left), prev=prev, mid=mid[0] if mid else None, mid_u=mid[1] if mid else 0.5), 0.0
        return dict(left), 0.0
    if isinstance(left, tuple):
        return rig.solve_left_at(q, left, seed=seed_l)
    return dict(RELAXED_L), 0.0


def solve(beats, ground=False, check=None):
    """Arms and wrists for every beat, each seeded from the beat before (so the solution moves smoothly). ground: a blade's point is
    kept off the floor as solved (2.3.2: a natural arm cannot always turn the blade as far as asked; then it is turned up more).
    check: (tick -> is the sword in the hand there, to be kept out of the body), for refine."""
    seed_r = None
    seed_l = None
    prev_left = None
    prev_beat = None
    for beat in beats:
        q = base_pose(beat)
        arm, wr, err_p, err_d = solve_right(beat, q, seed_r, ground)
        q["rightArm"], q["rightItem"] = arm, wr
        if beat.rarm is None or getattr(beat, "mirrored", False):
            seed_r = _seed(arm, wr)
        mid = None
        if prev_beat is not None and prev_left is not None:
            import anim
            u = anim.ease(beat.easing, 0.5 / max(0.5, beat.tick - prev_beat.tick))
            mid = (_blend_pose(prev_beat.solved, q, u), u)
        q["leftArm"], err_l = solve_left(beat, q, seed_l, prev_left, mid)
        prev_left = q["leftArm"]
        prev_beat = beat
        if beat.left in ("two", "saya") or isinstance(beat.left, tuple):
            seed_l = [q["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")]
        beat.solved = q
        beat.errors = (err_p, err_d, err_l)
    refine(beats, ground, check)
    return beats


def _blend_pose(qa, qb, u):
    """The pose the game shows a fraction u (eased) of the way from solved pose qa to qb (qb's angles written nearest qa's)."""
    out = {}
    for part in set(qa) | set(qb):
        a, b_ = qa.get(part, {}), qb.get(part, {})
        out[part] = {k: a.get(k, 0.0) + (b_.get(k, 0.0) - a.get(k, 0.0)) * u for k in set(a) | set(b_)}
    return out


# what refine looks for in the frames between two solved beats (tighter than clearance's limits: headroom)
REFINE_STRAIN, REFINE_BODY, REFINE_SWORD, REFINE_HANDLE = 5.0, 0.25, 0.2, 0.55


def gap_problems(a, b, check=None):
    """[(tick, what, by)] in the game's frames between solved beats a and b (every half tick, as clearance samples them)."""
    import anim
    out = []
    gap = b.tick - a.tick
    # every half tick between, and the beat b itself (a beat left a little through something is solved again, harder)
    for k in range(1, int(round(2 * gap)) + 1):
        t = a.tick + 0.5 * k
        q = _blend_pose(a.solved, b.solved, anim.ease(b.easing, (t - a.tick) / gap))
        for what, by in rig.arm_strain(q, "right"):
            if by > (REFINE_BODY if what == "arm through the body" else REFINE_STRAIN):
                out.append((t, what, by))
        if check is not None and check(t):
            for what, by in rig.sword_hits(q):
                if by > (REFINE_HANDLE if what.endswith("(handle)") else REFINE_SWORD):
                    out.append((t, "sword through the " + what, by))
        model = rig._Arm(q, "right")
        v = [q["rightArm"].get(c, 0.0) for c in ("pitch", "yaw", "roll", "bend")]
        m, fore, _ = model.frames(v)
        h = model.head_depth(m, fore)
        if h > 0.3:
            out.append((t, "arm through the head", h))
        hb = head_box(q)
        fist = rig.point(rig.layer_to_entity(q) @ rig.fist_frame(q, "right"), (0, 0, 0))
        # 2.3.5: with headroom (the check flags it at 0.0; refine used to wait until it was 0.3 inside)
        if inside(hb, fist, (-4.5, -8.5, -9.5), (4.5, 1.0, -4.0), 0.3):
            out.append((t, "fist in front of the face", 0.0))
    return out


def _samples(a, b, after):
    """refine's between-terms for the frames from beat a to beat b: (the neighbour's angles, w, after) for every half tick between;
    after: the beat being solved is a (b its neighbour after it), else b."""
    import anim
    out = []
    gap = b.tick - a.tick
    other = b if after else a
    o = _seed(other.solved["rightArm"], other.solved["rightItem"])
    for k in range(1, min(int(round(2 * gap)), 5)):
        u = anim.ease(b.easing, 0.5 * k / gap)
        out.append((o, u if after else 1.0 - u, after))
    return out


def refine(beats, ground, check=None, passes=4):
    """2.3.2: the frames between the solved beats as the game shows them (it interpolates the written angles): where one is twisted,
    or the arm or the sword goes through the body or the head, the beat at its end is solved again with those frames in its price
    (rig.solve_blade's between), its neighbours as they are. A beat whose arm is set (rarm) is left alone: the one beside it moves."""
    for _ in range(passes):
        unwrap(beats, ends=False)
        bad = set()
        for i in range(1, len(beats)):
            if gap_problems(beats[i - 1], beats[i], check):
                j = i if beats[i].rarm is None else i - 1
                if beats[j].rarm is None and j > 0:
                    bad.add(j)
        if not bad:
            return
        for j in sorted(bad):
            beat, prev = beats[j], beats[j - 1]
            between = _samples(prev, beat, False)
            if j + 1 < len(beats):
                between += _samples(beat, beats[j + 1], True)
            q = base_pose(beat)
            for part in ("leftItem",):
                q[part] = dict(beat.solved.get(part, {}))
            own = _seed(beat.solved["rightArm"], beat.solved["rightItem"])
            seed = None if prev.rarm is not None else _seed(prev.solved["rightArm"], prev.solved["rightItem"])
            mean = list((np.array(own) + np.array(seed if seed else own)) / 2)
            # solved again harder: the body and the limits weigh more against following the blade to the degree
            weights = rig.COLLIDE_WEIGHT, rig.STRAIN_WEIGHT
            rig.COLLIDE_WEIGHT, rig.STRAIN_WEIGHT = weights[0] * 2.5, weights[1] * 2.5
            try:
                arm, wr, err_p, err_d = solve_right(beat, q, seed, ground, between=between, starts=[own, mean])
            finally:
                rig.COLLIDE_WEIGHT, rig.STRAIN_WEIGHT = weights
            q["rightArm"], q["rightItem"] = arm, wr
            import anim
            u = anim.ease(beat.easing, 0.5 / max(0.5, beat.tick - prev.tick))
            mid = (_blend_pose(prev.solved, q, u), u)
            q["leftArm"], err_l = solve_left(beat, q, None if not isinstance(beat.solved.get("leftArm"), dict) else
                                             [beat.solved["leftArm"].get(k, 0.0) for k in ("pitch", "yaw", "roll", "bend")],
                                             prev.solved.get("leftArm"), mid)
            beat.solved = q
            beat.errors = (err_p, err_d, err_l)


def noto_frame(beat):
    """A noto beat's blade (the right fist's target, the blade, the edge: layer frame), its saya's leftItem and the hand's slide down
    the handle: the saya turned about its mouth onto the noto line, the blade {beat.noto} px out along its own curve."""
    q = base_pose(beat)
    m_body = rig.body_to_entity(q) @ rig.part_matrix(q, "torso", (0, 0, 0))
    pull = np.array(PROFILE.noto_pull) * min(1.0, 0.35 + beat.noto / PROFILE.noto_out)
    mouth = rig.layer_from_entity(q, rig.point(m_body, np.array(rig.HIP_MOUTH) + pull))
    u_layer = unit(np.array(PROFILE.noto_p) - mouth)
    to_body = np.linalg.inv(m_body) @ rig.layer_to_entity(q)
    u_body = rig.direction(to_body, u_layer)
    saya = dict(rig.saya_turn(u_body, (0.0, -1.0, 0.0)), x=float(pull[0]), y=float(pull[1]), z=float(pull[2]))
    q["leftItem"] = saya
    slide = PROFILE.draw_slide * max(0.0, min(1.0, 1.0 - beat.noto / PROFILE.noto_out))
    return rig.sheathed_grip(q, slide, beat.noto, PROFILE.key), saya, slide


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
    """A written sword animation: its beats, the tick its blade leaves the saya (draw) or goes back in (sheath), its cut windows.
    2.3.2: the blade it is written for (its name gets the blade's suffix), the parts it keys (None: all of them; the guard keys the
    right arm alone and the idles leave the head free, so the player still looks round and walks: the owner, "using sword makes the
    player's body too stiff, the head doesnt move"), and whether its point is kept off the floor (not in the air)."""

    def __init__(self, name, duration, beats, description, draw=-1, sheath=-1, cuts=(), free_legs=False, loop=None, stop_extra=4,
                 parts=None, ground=True, back=False):
        self.name, self.duration, self.beats, self.description = name, duration, beats, description
        self.draw, self.sheath, self.cuts, self.free_legs, self.loop, self.stop_extra = draw, sheath, cuts, free_legs, loop, stop_extra
        self.parts, self.ground, self.back = parts, ground, back
        self.profile = PROFILE
        self.noto = []

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


def route(a, b):
    """2.3.2: where a swing of the blade from a to b (directions) passes halfway, if not along the shortest arc: a swing of over 110
    degrees, or over 60 whose shortest arc does not pass well in front, is taken out in front of the body (the shortest arc of a
    recovery from a cut across the body, or of a wind-up from the low guard to the left side, ran the blade through the body and the
    free arm). None: the shortest arc."""
    a, b = unit(a), unit(b)
    turn = math.degrees(math.acos(max(-1.0, min(1.0, float(a @ b)))))
    forward = unit(D(0.0, 0.15, 1.0))
    mid = a + b
    mid = unit(mid) if np.linalg.norm(mid) > 0.25 else forward
    if turn <= 60.0 or (turn <= 110.0 and float(mid @ forward) >= 0.35):
        return None
    if float(mid @ forward) < 0.5:
        mid = unit(0.35 * mid + forward)
    return mid


def _gripping(beat):
    """A beat with the fist on Yoru's hilt on the back (not reaching for it)."""
    return getattr(beat, "back_lift", None) is not None and not hasattr(beat, "reach_off")


def densify(beats):
    """Every tick between two blade beats keyed on the designed arc (the fist along its line, the blade and the edge turned along a
    great circle, the body, legs, head and left arm in step, the later beat's easing baked in), so the game only ever interpolates
    the solved angles across one tick and the arm can never swing through the face between distant keys."""
    import anim
    out = [beats[0]]
    for a, b in zip(beats, beats[1:]):
        gap = b.tick - a.tick
        # 2.3.2: a beat on the noto line counts by its blade there, so the swing from the saya's line to a cut (or back) is designed
        # and solved every tick too (before, the game turned the arm's angles across it, and the blade swept through the head)
        noto_a = getattr(a, "noto", None) is not None and b.blade is not None and getattr(b, "noto", None) is None
        noto_b = getattr(b, "noto", None) is not None and a.blade is not None and getattr(a, "noto", None) is None
        blade_a, saya_a = (noto_frame(a)[:2]) if noto_a else (a.blade, a.saya)
        blade_b, saya_b = (noto_frame(b)[:2]) if noto_b else (b.blade, b.saya)
        # 2.3.2: Yoru's hilt on the back counts by where the hand holds it there, so the swing between the ropes and the arc is designed
        # and solved every tick too (left to the game, the arm's angles sagged the point into the floor between them)
        back_a = _gripping(a) and b.blade is not None
        back_b = _gripping(b) and a.blade is not None
        if back_a:
            blade_a = rig.back_grip(base_pose(a), a.back_lift)
        if back_b:
            blade_b = rig.back_grip(base_pose(b), b.back_lift)
        dense = (gap > 1 and blade_a is not None and blade_b is not None and b.easing != "constant"
                 and a.rarm is None and b.rarm is None and getattr(a, "hilt", None) is None and getattr(b, "hilt", None) is None
                 and (getattr(a, "noto", None) is None or noto_a) and (getattr(b, "noto", None) is None or noto_b)
                 and (getattr(a, "back_lift", None) is None or back_a) and (getattr(b, "back_lift", None) is None or back_b))
        if dense:
            # a beat marked direct is reached along the shortest arc (2.3.2: a raise over the top, which route would take out in front)
            via = None if getattr(b, "direct", False) else route(blade_a[1], blade_b[1])
            # 2.4.5: next to a lunge (the reach grip) the hand slides along the handle with the swing, and the fist swings round the
            # shoulder (its distance eased between the two), so the arm stays out straight through the cut, never pulled in to a
            # chord between the two fists
            reach_ab = getattr(a, "lunge", False) or getattr(b, "lunge", False)
            for k in range(a.tick + 1, b.tick):
                u = anim.ease(b.easing, (k - a.tick) / gap)
                fist = tuple(np.array(blade_a[0]) * (1 - u) + np.array(blade_b[0]) * u)
                if reach_ab:
                    da = np.array(blade_a[0], float) - SHOULDER_R
                    db = np.array(blade_b[0], float) - SHOULDER_R
                    fist = tuple(SHOULDER_R + slerp(da, db, u) * (np.linalg.norm(da) * (1 - u) + np.linalg.norm(db) * u))
                if via is None:
                    bd = slerp(blade_a[1], blade_b[1], u)
                else:
                    bd = slerp(blade_a[1], via, 2 * u) if u < 0.5 else slerp(via, blade_b[1], 2 * u - 1)
                ed = slerp(blade_a[2], blade_b[2], u)
                ed = ed - bd * (ed @ bd)
                left = a.left
                if isinstance(a.left, dict) and isinstance(b.left, dict):
                    left = _mix(a.left, b.left, u)
                elif u > 0.5:
                    left = b.left
                legs = {part: _mix(a.legs.get(part, {}), b.legs.get(part, {}), u) for part in set(a.legs) | set(b.legs)}
                mid = Beat(k, "linear", blade=(fist, tuple(bd), tuple(unit(ed))), left=left, body=_mix(a.body, b.body, u),
                           legs=legs, head=_mix(a.head, b.head, u), torso=_mix(a.torso, b.torso, u), saya=_mix(saya_a, saya_b, u))
                if reach_ab:
                    mid.slide = getattr(a, "slide", 0.0) * (1 - u) + getattr(b, "slide", 0.0) * u
                if getattr(a, "upper", None) is not None and getattr(b, "upper", None) is not None:
                    mid.upper = a.upper
                elif back_a or back_b:
                    mid.upper = OVERHEAD
                out.append(mid)
            b.easing = "linear"
        out.append(b)
    return out


def noto_out(move, t):
    """Between the move's noto beats, how far out of the saya the blade is at tick t (model px); None outside them."""
    pairs = getattr(move, "noto", None)
    if not pairs or not pairs[0][0] <= t <= pairs[-1][0]:
        return None
    for (t0, o0), (t1, o1) in zip(pairs, pairs[1:]):
        if t0 <= t <= t1:
            return o0 + (o1 - o0) * ((t - t0) / max(1e-9, t1 - t0))
    return pairs[-1][1]


def sliding(move, t):
    """2.3.2: the blade on the saya's line with the hand (a draw before its draw tick too: it slides out with the arm), as SwordLayer."""
    out = noto_out(move, t)
    return out is not None and (move.drawn_at(t) or out > 0.0)


def back_phase(move):
    """2.3.2: for a move that takes Yoru out of its ropes or puts it back: (the lift beats [(tick, px lifted along itself)], the glide
    (tick at the ropes, tick in the hand)); (None, None) otherwise. No natural arm reaches Yoru's hilt behind the head exactly (it misses
    by about 3 px and 19 degrees, and a great sword hung upright enough for it would stand half a block into the floor): while the hand
    lifts it, the game draws it on its own line out of the ropes (client/sword/SwordLayer.back, BladePaths.backLift), and over the two
    ticks it swings free it glides from there into the hand (BladePaths.backGlide); the sheathing the same backwards."""
    backs = sorted((beat.tick, beat.back_lift) for beat in move.beats
                   if getattr(beat, "back_lift", None) is not None and not hasattr(beat, "reach_off"))
    if not backs:
        return None, None
    ticks = move.design_ticks
    if move.draw >= 0:
        tb = backs[-1][0]
        later = [t for t in ticks if t > tb]
        return backs, ((tb, later[0]) if later else None)
    tb = backs[0][0]
    earlier = [t for t in ticks if t < tb]
    return backs, ((tb, earlier[-1]) if earlier else None)


def _lift_at(backs, t):
    for (t0, l0), (t1, l1) in zip(backs, backs[1:]):
        if t0 <= t <= t1:
            return l0 + (l1 - l0) * ((t - t0) / max(1e-9, t1 - t0))
    return backs[0][1] if len(backs) == 1 and abs(t - backs[0][0]) < 1e-9 else None


def _euler_zyx(r):
    """(z, y, x) degrees with r = Rz Ry Rx (the order SwordLayer turns the pose in)."""
    y = math.asin(max(-1.0, min(1.0, -r[2, 0])))
    z = math.atan2(r[1, 0], r[0, 0])
    x = math.atan2(r[2, 1], r[2, 2])
    return math.degrees(z), math.degrees(y), math.degrees(x)


def _hand_layer(q):
    return rig.fist_frame(q, "right") @ rig.wrist(q) @ rig.grip(rig.slide_of(q))


GLIDES = {}


def _frame(m):
    """A layer-frame matrix (katana model px scaled to layer px) as (shift px, (z, y, x) degrees)."""
    r = m[:3, :3] / np.linalg.norm(m[:3, 0])
    return tuple(float(v) for v in m[:3, 3]), _euler_zyx(r)


GLIDE_STEP = 0.25


def back_glide(move):
    """(tick at the ropes, tick in the hand, [(shift px, (z, y, x) degrees)] every GLIDE_STEP ticks from the first to the second): the
    sword's frame during the glide seen from the hand's sword frame (hand^-1 sword), which client/sword/SwordLayer sets after the
    hand's (BladePaths.backGlide), the samples' shifts mixed and their turns slerped between. The sword's turn eases straight from how
    it hangs on its line to how the hand holds it at the second tick (the hand's own, just off the ropes, still points it at the floor:
    no wrist turns a great sword up from there), its grip riding with the hand, the hand's 3 px miss of the hilt taken off as it goes."""
    if move.name in GLIDES:
        return GLIDES[move.name]
    backs, glide = back_phase(move)
    out = None
    if glide is not None:
        import anim
        with open(os.path.join(OUT, move.name + ".json"), encoding="utf-8") as handle:
            a = anim.Anim(json.load(handle))
        tb, th = glide
        qb, qh = a.pose(tb), a.pose(th)
        mb = rig.back(qb) @ rig.tr(0, -_lift_at(backs, tb), 0)
        mh = _hand_layer(qh)
        _, eb = _frame(mb)
        _, eh = _frame(mh)
        miss = mb[:3, 3] - _hand_layer(qb)[:3, 3]
        samples = []
        n = int(round(abs(th - tb) / GLIDE_STEP))
        for k in range(n + 1):
            t = tb + (th - tb) * k / n
            u = k / n
            w = u * u * (3 - 2 * u)
            ang = slerp_zyx(eb, eh, w)
            hand = _hand_layer(a.pose(t))
            want = (rig.tr(*(hand[:3, 3] + (1.0 - w) * miss)) @ rig.rz(math.radians(ang[0])) @ rig.ry(math.radians(ang[1]))
                    @ rig.rx(math.radians(ang[2])) @ rig.sc(rig.SCALE / rig.PX))
            samples.append(_frame(np.linalg.inv(hand) @ want))
        out = (tb, th, samples)
    GLIDES[move.name] = out
    return out


def glide_matrix(g, q, t):
    """The glide's frame at tick t (layer px, katana model scale), as client/sword/SwordLayer sets it: the hand's, then the sample
    correction (shifts mixed, turns slerped) between the two samples about t."""
    tb, th, samples = g
    x = (t - tb) / (th - tb) * (len(samples) - 1)
    i = max(0, min(len(samples) - 2, int(math.floor(x))))
    f = x - i
    (s0, e0), (s1, e1) = samples[i], samples[i + 1]
    shift = np.array(s0) + (np.array(s1) - np.array(s0)) * f
    e = slerp_zyx(e0, e1, f)
    return (_hand_layer(q) @ rig.tr(*shift) @ rig.rz(math.radians(e[0])) @ rig.ry(math.radians(e[1])) @ rig.rx(math.radians(e[2])))


def yoru_layer(move, q, t):
    """The layer-frame matrix (katana model px -> layer px) of Yoru as the game draws it at tick t, or None where it is simply on the
    back (sheathed) or simply in the hand (drawn): on its line out of the ropes while lifted, gliding between there and the hand."""
    backs, glide = back_phase(move)
    if backs is None:
        return None
    if backs[0][0] <= t <= backs[-1][0]:
        return rig.back(q) @ rig.tr(0, -_lift_at(backs, t), 0)
    g = back_glide(move)
    if g is not None and min(g[0], g[1]) < t < max(g[0], g[1]):
        return glide_matrix(g, q, t)
    return None


def _q_zyx(e):
    """The quaternion (w, x, y, z) of Rz(e0) Ry(e1) Rx(e2), degrees."""
    out = np.array((1.0, 0.0, 0.0, 0.0))
    for ang, axis in ((e[0], 2), (e[1], 1), (e[2], 0)):
        h = math.radians(ang) / 2.0
        q = np.zeros(4)
        q[0] = math.cos(h)
        q[1 + axis] = math.sin(h)
        out = _qmul(out, q)
    return out


def _qmul(a, b):
    return np.array((a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3], a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2],
                     a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1], a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0]))


def slerp_zyx(ea, eb, w):
    """The turn w of the way along the shortest turn from Rz Ry Rx(ea) to Rz Ry Rx(eb), as (z, y, x) degrees (client/sword/SwordLayer
    does the same sums: BladePaths.backGlide)."""
    qa, qb = _q_zyx(ea), _q_zyx(eb)
    d = float(qa @ qb)
    if d < 0:
        qb, d = -qb, -d
    if d > 0.9995:
        q = qa + (qb - qa) * w
    else:
        th = math.acos(d)
        q = (math.sin((1 - w) * th) * qa + math.sin(w * th) * qb) / math.sin(th)
    q /= np.linalg.norm(q)
    qw, qx, qy, qz = q
    r = np.array([[1 - 2 * (qy * qy + qz * qz), 2 * (qx * qy - qz * qw), 2 * (qx * qz + qy * qw)],
                  [2 * (qx * qy + qz * qw), 1 - 2 * (qx * qx + qz * qz), 2 * (qy * qz - qx * qw)],
                  [2 * (qx * qz - qy * qw), 2 * (qy * qz + qx * qw), 1 - 2 * (qx * qx + qy * qy)]])
    return _euler_zyx(r)


def game_blade(move, q, t):
    """The katana's matrix (model px -> E) as the game draws it at tick t: in the hand, except while it slides along the saya's line
    (between its noto beats: drawn out or slid home), where it is laid exactly along the saya's curve (its own, 2.3.2), out the
    interpolated amount, so no frame between two ticks can put it through the saya's side (client/sword/SwordLayer does the same from
    BladePaths.notoOut and SayaSlide)."""
    pairs = move.noto
    if move.profile is YORU:
        m = yoru_layer(move, q, t)
        if m is not None:
            return rig.layer_to_entity(q) @ m
        if not move.drawn_at(t):
            return rig.back_matrix(q)
    if sliding(move, t):
        for (t0, o0), (t1, o1) in zip(pairs, pairs[1:]):
            if t0 <= t <= t1:
                out = o0 + (o1 - o0) * ((t - t0) / max(1e-9, t1 - t0))
                return rig.saya_matrix(q) @ rig.arc(out, move.profile.key)
    return rig.blade_matrix(q)


PENDING = []


def write(move):
    """Registers a move for the blade being written (PROFILE): its name gets the blade's suffix; it is solved and written by
    run_pending (in parallel: the arms are searched numerically, beat by beat)."""
    move.profile = PROFILE
    move.name = move.name + PROFILE.suffix
    move.noto = sorted((beat.tick, beat.noto) for beat in move.beats if getattr(beat, "noto", None) is not None)
    move.design_ticks = sorted(beat.tick for beat in move.beats)
    MOVES[move.name] = move
    if SOLVE and (ONLY is None or move.name in ONLY):
        PENDING.append(move)
    return move


def solve_and_write(move):
    """In a worker: the beats densified, kept off the floor, solved; the animation written; what its clearance check finds."""
    global PROFILE
    PROFILE = move.profile
    rig.set_sword(PROFILE.key)
    import io
    import contextlib
    text = io.StringIO()
    with contextlib.redirect_stdout(text):
        move.beats = densify(move.beats)
        mirror_arm(move)
        if move.ground:
            for beat in move.beats:
                lift_clear(beat, move.profile)
        solve(move.beats, move.ground, lambda t: move.drawn_at(t) and not sliding(move, t))
        report(move.name, move.beats)
    # 2.5.4 (the owner: "Fix the head on sword animations, it should not be animated and should be free"): no sword animation keys the
    # head; it looks where the player looks (the solver still poses it to keep the blade clear of it)
    parts = move.parts or {k: v for k, v in WRITE_PARTS.items()
                           if k != "head" and not (move.free_legs and k in ("rightLeg", "leftLeg"))}
    parts = {k: v for k, v in parts.items() if k != "head"}
    if move.loop is not None:
        # a loop ends where it returns to: the same pose, solved once (two solves of one target can differ)
        ret = [beat for beat in move.beats if beat.tick == move.loop]
        if ret:
            import copy
            move.beats[-1].solved = copy.deepcopy(ret[0].solved)
    unwrap(move.beats, move.loop)
    moves = []
    for beat in move.beats:
        q = beat.solved
        for part, channels in parts.items():
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": beat.tick, "easing": beat.easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    emote = {"beginTick": 0, "endTick": move.duration, "stopTick": move.duration + move.stop_extra, "isLoop": move.loop is not None,
             "returnTick": move.loop or 0, "degrees": True, "easeBeforeKeyframe": True, "moves": moves}
    if move.loop is not None:
        emote["stopTick"] = move.duration
    data = {"version": 3, "name": move.name, "author": "HexPiece 2.3.2", "description": move.description, "emote": emote}
    with open(os.path.join(OUT, move.name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")
    return move.name, text.getvalue(), clearance(move)


def mirror_arm(move):
    """2.3.2: a move with mirror = (other move, sum, (first tick, last tick)) holds, at each of its beats from the first tick to the
    last, the right arm and wrist the other move's written animation has at tick sum - tick: Yoru's sheathing lays the great sword
    back on its back exactly as its draw takes it off (solved on its own, the hand came at it another way, and the cross guard
    swung through the head as it glided onto the back)."""
    spec = getattr(move, "mirror", None)
    if not spec:
        return
    import anim
    other, total, (lo, hi) = spec
    with open(os.path.join(OUT, other + ".json"), encoding="utf-8") as handle:
        a = anim.Anim(json.load(handle))
    for beat in move.beats:
        if lo <= beat.tick <= hi:
            q = a.pose(total - beat.tick)
            beat.rarm = {k: float(q["rightArm"].get(k, 0.0)) for k in ARM_CH}
            beat.wrist = {k: float(q["rightItem"].get(k, 0.0)) for k in ("pitch", "yaw", "roll", "y")}
            beat.mirrored = True


def _nearest_euler(v, prev):
    """(pitch, yaw, roll) as written nearest prev: the same turn has two ways to be written in this order (pitch + 180, 180 - yaw,
    roll + 180), each with whole turns added; the game interpolates the numbers, so the nearest one turns the least between beats."""
    p, y, r = v
    best = None
    for cand in ((p, y, r), (p + 180.0, 180.0 - y, r + 180.0)):
        c = list(cand)
        for k in range(3):
            while c[k] - prev[k] > 180.0:
                c[k] -= 360.0
            while c[k] - prev[k] < -180.0:
                c[k] += 360.0
        d = sum((c[k] - prev[k]) ** 2 for k in range(3))
        if best is None or d < best[0]:
            best = (d, c)
    return best[1]


def _rot(v):
    return rig.zyx(math.radians(v[0]), math.radians(v[1]), math.radians(v[2]))[:3, :3]


def _canonical(v):
    """The plain way to write a turn: the yaw within 90, pitch and roll within 180."""
    p, y, r = (float(x) for x in v)
    if abs(((y + 180.0) % 360.0) - 180.0) > 90.0:
        p, y, r = p + 180.0, 180.0 - y, r + 180.0
    return np.array([((x + 180.0) % 360.0) - 180.0 for x in (p, y, r)])


def _spellings(v, prev, plain=False):
    """Every way to write turn v near prev: both branches (plain: only the plain one, the yaw within a quarter turn of a whole one),
    each channel the nearest whole-turn shift of prev and one turn either side."""
    out = []
    p, y, r = (float(x) for x in _canonical(v))
    for cand in ((p, y, r),) if plain else ((p, y, r), (p + 180.0, 180.0 - y, r + 180.0)):
        base = [c - 360.0 * round((c - prev[k]) / 360.0) for k, c in enumerate(cand)]
        for dp in (-360.0, 0.0, 360.0):
            for dy in (-360.0, 0.0, 360.0):
                for dr in (-360.0, 0.0, 360.0):
                    out.append(np.array((base[0] + dp, base[1] + dy, base[2] + dr)))
    return out


def _between_strain(part, a, b, bend_a=0.0, bend_b=0.0):
    """What the game's frames between two written turns of part cost (the natural-arm limits, squared), at a quarter, half and three
    quarters of the way."""
    c = 0.0
    for u in (0.25, 0.5, 0.75):
        rot = _rot(a + (b - a) * u)
        if part == "rightItem":
            dev, pro, spin = rig.wrist_angles(rot)
            c += (rig._over(dev, -rig.RADIAL, rig.ULNAR) ** 2 + rig._over(pro, -rig.PRONATION, rig.PRONATION) ** 2
                  + rig._over(spin, -rig.WRIST_BEND, rig.WRIST_BEND) ** 2)
        else:
            fo, fb = rig.fold_strain(rot, bend_a + (bend_b - bend_a) * u, "right" if part == "rightArm" else "left")
            c += fo * fo + fb * fb + rig.shoulder_back(rot) ** 2
    return c


def _quat(m):
    t = m[0, 0] + m[1, 1] + m[2, 2]
    w = math.sqrt(max(0.0, 1.0 + t)) / 2.0
    x = math.copysign(math.sqrt(max(0.0, 1.0 + m[0, 0] - m[1, 1] - m[2, 2])) / 2.0, m[2, 1] - m[1, 2])
    y = math.copysign(math.sqrt(max(0.0, 1.0 - m[0, 0] + m[1, 1] - m[2, 2])) / 2.0, m[0, 2] - m[2, 0])
    z = math.copysign(math.sqrt(max(0.0, 1.0 - m[0, 0] - m[1, 1] + m[2, 2])) / 2.0, m[1, 0] - m[0, 1])
    return np.array((w, x, y, z))


def _turn_deg(a, b):
    """The real turn (degrees) between two written turns."""
    r = _rot(a).T @ _rot(b)
    return math.degrees(math.acos(max(-1.0, min(1.0, (float(np.trace(r)) - 1.0) / 2.0))))


def _detour(a, b):
    """How far (degrees) the game's frames between written turns a and b stray from the shortest turn between them."""
    qa, qb = _quat(_rot(a)), _quat(_rot(b))
    if qa @ qb < 0:
        qb = -qb
    worst = 0.0
    for u in (0.25, 0.5, 0.75):
        q = qa * (1 - u) + qb * u
        q /= np.linalg.norm(q)
        qn = _quat(_rot(a + (b - a) * u))
        worst = max(worst, math.degrees(2.0 * math.acos(min(1.0, abs(float(q @ qn))))))
    return worst


def _chain(part, values, bends, start, plain=False):
    """The track written from start: each turn spelled where the frames to it from the one before are natural, and near (plain: on
    the plain branch only)."""
    out = [np.array(start, float)]
    for k in range(1, len(values)):
        prev = out[-1]
        best = None
        for cand in _spellings(values[k], prev, plain):
            d = float(np.sum((cand - prev) ** 2))
            if d > 400.0 ** 2:
                continue
            cost = 0.002 * d + 5.0 * _between_strain(part, prev, cand, bends[k - 1], bends[k])
            if best is None or cost < best[0]:
                best = (cost, cand)
        out.append(best[1])
    return out


def _spun(a, b):
    """The game's frames between written turns a and b spin the part (wound()'s rule: the numbers jump far further than it turns, and
    the frames stray off the turn); a raise over the top written the long way round (190 degrees of pitch for a 170-degree turn) is
    not a spin: that is how an arm goes up."""
    jump = float(np.max(np.abs(np.asarray(b) - np.asarray(a))))
    return jump > 60.0 and _turn_deg(a, b) < 0.5 * jump and _detour(a, b) > 25.0


def _track_cost(part, track, bends):
    """How unnatural (the natural-arm limits, squared) the game's frames between a track's keyframes are, a spin counting heavily."""
    c = 0.0
    for k in range(1, len(track)):
        c += _between_strain(part, track[k - 1], track[k], bends[k - 1], bends[k]) + (1.0e4 if _spun(track[k - 1], track[k]) else 0.0)
    return c


def _best_track(part, values, bends, start):
    """2.3.5: the whole track written at once: every keyframe in every spelling (both branches, each channel the plain value or a
    whole turn either side), the cheapest way through them (the in-between frames' strain, a spin heavily, and how far the numbers
    jump), starting at start and ending on the plain spelling. A greedy pass, choosing tick by tick, can wind the numbers a full turn
    on two channels over a move and then has to spin back at its end (sword_yoko's recovery, sword_plunge's); this finds the place
    where taking the turn back does not show. None if no track ends plain."""
    states = []
    for k, v in enumerate(values):
        if k == 0:
            states.append([np.array(start, float)])
            continue
        c = _canonical(v)
        out = []
        for cand in ((c[0], c[1], c[2]), (c[0] + 180.0, 180.0 - c[1], c[2] + 180.0)):
            for dp in (-360.0, 0.0, 360.0):
                for dy in (-360.0, 0.0, 360.0):
                    for dr in (-360.0, 0.0, 360.0):
                        out.append(np.array((cand[0] + dp, cand[1] + dy, cand[2] + dr)))
        if k == len(values) - 1:
            out = [x for x in out if float(np.max(np.abs(x - _canonical(x)))) <= 0.5]
        states.append(out)
    cost = [np.zeros(1)]
    back = [None]
    for k in range(1, len(values)):
        prev, cur = states[k - 1], states[k]
        best = np.full(len(cur), np.inf)
        arg = np.zeros(len(cur), int)
        for j, b in enumerate(cur):
            for i, a in enumerate(prev):
                if not np.isfinite(cost[-1][i]):
                    continue
                d = float(np.max(np.abs(b - a)))
                if d > 400.0:
                    continue
                c = (cost[-1][i] + 0.002 * float(np.sum((b - a) ** 2)) + 5.0 * _between_strain(part, a, b, bends[k - 1], bends[k])
                     + (1.0e5 if _spun(a, b) else 0.0))
                if c < best[j]:
                    best[j], arg[j] = c, i
        cost.append(best)
        back.append(arg)
    if not np.isfinite(cost[-1]).any():
        return None
    j = int(np.argmin(cost[-1]))
    track = [None] * len(values)
    for k in range(len(values) - 1, -1, -1):
        track[k] = states[k][j]
        if k > 0:
            j = int(back[k][j])
    return track


def unwrap(beats, loop=None, ends=True):
    """2.3.2: every solved arm and wrist written so the game's frames between beats are natural (the game interpolates the numbers:
    two ways of writing the same turn, or -197 then 143 degrees, spin the arm between two beats; and the nearest way is not always the
    natural one: an arm let down from overhead to its side is written forward and down, never on round the back), each track starting
    on the plain spelling and ending on it (the game fades between animations, and out of one, by blending the numbers: a track left
    at 341 instead of -19 spins the arm a full turn in the fade): where it would end on the other branch, it changes over where the
    turn passes nearest the yaw's quarter turn (there the two spellings are the same turn, and the change does not show). loop: the
    tick a loop returns to: the last beat is written as that one, exactly. ends: False skips the ends (refine's passes)."""
    for part in ("rightArm", "leftArm", "rightItem"):
        idx = [i for i, beat in enumerate(beats) if part in beat.solved]
        if not idx:
            continue
        values = [np.array([beats[i].solved[part].get(c, 0.0) for c in ("pitch", "yaw", "roll")]) for i in idx]
        bends = [beats[i].solved[part].get("bend", 0.0) for i in idx]
        track = _chain(part, values, bends, _canonical(values[0]))
        end = _canonical(track[-1])
        if ends and (float(np.max(np.abs(track[-1] - end))) > 0.5
                     or any(_spun(track[k - 1], track[k]) for k in range(1, len(track)))):
            # 2.3.5: the whole track at once, ending plain, with no spin anywhere if one can be had
            best = _best_track(part, values, bends, _canonical(values[0]))
            if best is not None:
                track = best
                end = _canonical(track[-1])
        if ends and float(np.max(np.abs(track[-1] - end))) > 0.5:
            # 2.3.2: first the whole track held to the plain branch (an arm raised forward over the top written past -180, not
            # changed over to the other branch and back): it ends plain with nothing to change back from, if it is as natural
            plain = _chain(part, values, bends, _canonical(values[0]), plain=True)
            if (float(np.max(np.abs(plain[-1] - _canonical(plain[-1])))) <= 0.5
                    and _track_cost(part, plain, bends) <= _track_cost(part, track, bends) + 2.0):
                track = plain
                end = plain[-1]
        if ends and float(np.max(np.abs(track[-1] - end))) > 0.5:
            best = None
            near_gimbal = any(abs(_canonical(v)[1]) >= 55.0 for v in track[1:])
            for j in range(1, len(track)):
                alt = _canonical(track[j])
                # only where the yaw is near its quarter turn (elsewhere the change shows), unless the track never passes one: then
                # wherever it shows least
                if float(np.max(np.abs(alt - track[j]))) < 0.5 or (near_gimbal and abs(alt[1]) < 55.0):
                    continue
                tail = _chain(part, values[j:], bends[j:], alt)
                if float(np.max(np.abs(tail[-1] - _canonical(tail[-1])))) > 0.5:
                    continue
                cost = _detour(track[j - 1], tail[0]) + 0.2 * math.sqrt(_between_strain(part, track[j - 1], tail[0], bends[j - 1], bends[j]))
                if best is None or cost < best[0]:
                    best = (cost, j, tail)
            if best is not None:
                track = track[:best[1]] + best[2]
        if ends and loop is not None:
            ret = [k for k, i in enumerate(idx) if beats[i].tick == loop]
            if ret and float(np.max(np.abs(_canonical(track[-1]) - _canonical(track[ret[0]])))) < 3.0:
                track[-1] = track[ret[0]].copy()
        for k, i in enumerate(idx):
            for c, ch in enumerate(("pitch", "yaw", "roll")):
                beats[i].solved[part][ch] = round(float(track[k][c]), 3)


def smooth_json(name):
    """unwrap() on a written animation (its keyframes, tick by tick), in place."""
    path = os.path.join(OUT, name + ".json")
    with open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    moves = data["emote"]["moves"]
    for part in ("rightArm", "leftArm", "rightItem"):
        frames = {}
        for m in moves:
            if part in m:
                for ch, val in m[part].items():
                    if ch in ("pitch", "yaw", "roll"):
                        frames.setdefault(m["tick"], {})[ch] = (m, val)
        prev = None
        for tick in sorted(frames):
            f = frames[tick]
            if len(f) < 3:
                continue
            v = (f["pitch"][1], f["yaw"][1], f["roll"][1])
            if prev is not None:
                v = _nearest_euler(v, prev)
                for k, ch in enumerate(("pitch", "yaw", "roll")):
                    f[ch][0][part][ch] = round(float(v[k]), 3)
            prev = v
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def run_pending():
    import multiprocessing
    jobs = list(PENDING)
    PENDING.clear()
    jobs.sort(key=lambda m: -len(m.beats) * m.duration)
    # 2.3.2: a move that copies part of another's solved arm (Yoru's sheathing, its draw's) is solved once that one is written
    names = {m.name for m in jobs}
    later = [m for m in jobs if getattr(m, "mirror", None) and m.mirror[0] in names]
    first = [m for m in jobs if m not in later]
    with multiprocessing.get_context("fork").Pool(max(1, (os.cpu_count() or 2))) as pool:
        results = pool.map(solve_and_write, first, chunksize=1)
        if later:
            results += pool.map(solve_and_write, later, chunksize=1)
    bad = 0
    for name, text, problems in sorted(results):
        print(text, end="")
        if problems:
            bad += 1
            print(f"  {name}: {len(problems)} problems, first {problems[:3]}")
    print(f"{len(results)} sword animations written, {bad} with problems")
    return results


ARMS = {"rightArm": ARM_CH, "leftArm": ARM_CH}
RIGHT = {"rightArm": ARM_CH, "rightItem": ("pitch", "yaw", "roll", "y")}
NO_HEAD = {k: v for k, v in WRITE_PARTS.items() if k != "head"}


def stance_write(name, beats, description):
    """An arms-only loop (the combat stance; client/StanceLayer plays it under every move): only the arms are in the file."""
    write(Move(name, beats[-1].tick, beats, description, parts=ARMS, loop=0, ground=False))


# --- reach and clearance ---------------------------------------------------------------------------------------------------------
SHOULDER_R = np.array((-5.0, 2.0, 0.0))
SHOULDER_L = np.array((5.0, 2.0, 0.0))
# 2.3.5 (the owner: "swings on the blades need more freedom, arm more stretched, longer swings", "more range"): a fist reaches 8.4 px
# from its shoulder (the arm straight is 8.5 to the fist's centre); it was held to 8.1, the elbow always a little bent
REACH = 8.4


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
    """A beat of a cut at t (0..1) along blade directions dirs and fist points fists (H(), polylines): the edge faces the way the path
    runs (t growing; lead -1: the other way). A move that runs a path backwards (a sheathing down a draw's arc) keeps lead 1 to hold
    the sword as the forward move does."""
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


def tip_point(prof):
    """The blade's point (model px: out to its back by its curve, up the blade)."""
    return (prof.tip_x, prof.tip, 0.0)


def lift_clear(beat, prof, floor=None):
    """2.3.2 (the owner: "yoru clips through the floor"): a beat whose point would be under FLOOR blocks over the feet's floor has its
    blade turned up (about the axis across it and the vertical, the fist where it is, the edge turned with it) just enough to clear
    it: a great sword cannot point at the ground from the hip as a katana does."""
    if beat.blade is None or beat.rarm is not None or any(getattr(beat, k, None) is not None for k in ("noto", "hilt", "back_lift")):
        return False
    q = base_pose(beat)
    m = rig.layer_to_entity(q)
    up = rig.layer_dir_from_entity(q, (0.0, 1.0, 0.0))
    target, bdir, edir = beat.blade
    target = np.array(target, float)
    bd = unit(bdir)
    ed = np.array(edir, float)
    ed = unit(ed - bd * (ed @ bd))

    def height(b_, e_):
        p = target + b_ * (prof.tip - rig.GRIP) - e_ * prof.tip_x
        return rig.point(m, p)[1]

    floor = FLOOR if floor is None else floor
    if height(bd, ed) >= floor:
        return False
    axis = np.cross(bd, up)
    if np.linalg.norm(axis) < 1e-6:
        axis = np.cross(ed, up)
    axis = unit(axis)

    def turned(v, ang):
        c, sn = math.cos(ang), math.sin(ang)
        return v * c + np.cross(axis, v) * sn + axis * (axis @ v) * (1 - c)

    lo, hi = 0.0, math.radians(150.0)
    for _ in range(40):
        mid = (lo + hi) / 2
        if height(turned(bd, mid), turned(ed, mid)) >= floor:
            hi = mid
        else:
            lo = mid
    beat.blade = (tuple(target), tuple(turned(bd, hi)), tuple(turned(ed, hi)))
    return True


def clearance(move):
    """Every half tick of the written animation: no fist and no part of the blade inside the head, no blade through the torso, and
    (2.3.2) on the ground no point in the floor."""
    import anim
    with open(os.path.join(OUT, move.name + ".json"), encoding="utf-8") as handle:
        a = anim.Anim(json.load(handle))
    prof = move.profile
    # each move against its own blade's mesh (a check run in another process starts with the katana's)
    rig.set_sword(prof.key)
    problems = []
    # Yoru held where it hangs on the back (taken from its ropes, or put back): it hangs to the floor there by design (sword/BackCarry)
    on_back = [beat.tick for beat in move.beats if getattr(beat, "back_lift", None) is not None]
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
        for side in ("right", "left"):
            if move.parts is RIGHT and side == "left":
                continue
            for what, by in rig.arm_strain(q, side):
                if by > (0.4 if what == "arm through the body" else 8.0):
                    problems.append((t, f"{side} {what} ({by:.1f})"))
        yl = yoru_layer(move, q, t) if prof is YORU else None
        if (move.drawn_at(t) and not sliding(move, t)) or yl is not None:
            at_hilt = on_back and min(on_back) <= t <= max(on_back) + 1.0
            for what, by in rig.sword_hits(q, blade=yl):
                # Yoru's cross guard sits just behind the head on the back: the hand on its hilt there may graze it (half a pixel)
                limit = 0.6 if at_hilt and what.startswith("head") else 0.8 if what.endswith("(handle)") else 0.35
                if by > limit:
                    problems.append((t, f"sword through the {what} ({by:.1f})"))
        if move.drawn_at(t) or sliding(move, t) or yl is not None:
            m = game_blade(move, q, t)
            for k in range(13):
                p = rig.point(m, (0, rig.BLADE_BASE + (prof.tip - rig.BLADE_BASE) * k / 12, 0))
                if inside(hb, p, (-4, -8, -4), (4, 0, 4), 0.2):
                    problems.append((t, "blade through the head"))
                    break
                if k >= 2 and inside(torso, p, (-4, 0, -2), (4, 12, 2), 0.4):
                    problems.append((t, "blade through the body"))
                    break
            # on (or lifted along) its line on the back its point may sit where it hangs at rest, on the floor, never lower
            floor = -0.02 if yl is not None else 0.0
            if move.ground and rig.point(m, tip_point(prof))[1] < floor:
                problems.append((t, f"point in the floor ({rig.point(m, tip_point(prof))[1]:.2f})"))
        t += 0.5
    for problem in anim.lint(a):
        problems.append(problem)
    problems += wound(move.name)
    return problems


def wound(name):
    """2.3.2: [(tick, what)] for a rotation (an arm's or the wrist's) that starts or ends written past a half turn, or the other way
    round (pitch and roll past 90 with the yaw past 90): the game fades between animations, and out of one, by blending the written
    numbers, so a track ending at 341 instead of -19 spins the arm a full turn in the fade. A loop must end where it starts."""
    with open(os.path.join(OUT, name + ".json"), encoding="utf-8") as handle:
        emote = json.load(handle)["emote"]
    frames = {}
    for m in emote["moves"]:
        for part in ("rightArm", "leftArm", "rightItem"):
            if part in m:
                for ch, v in m[part].items():
                    if ch in ("pitch", "yaw", "roll"):
                        frames.setdefault(part, {}).setdefault(m["tick"], {})[ch] = v
    out = []
    for part, by_tick in frames.items():
        ticks = sorted(by_tick)
        for tick in (ticks[0], ticks[-1]):
            v = by_tick[tick]
            p, y, r = v.get("pitch", 0.0), v.get("yaw", 0.0), v.get("roll", 0.0)
            if abs(p) > 180.5 or abs(r) > 180.5 or abs(y) > 100.0:
                out.append((tick, f"{part} written wound ({p:.0f}, {y:.0f}, {r:.0f})"))
        for t0, t1 in zip(ticks, ticks[1:]):
            a0, a1 = by_tick[t0], by_tick[t1]
            v0 = np.array([a0.get(c, 0.0) for c in ("pitch", "yaw", "roll")])
            v1 = np.array([a1.get(c, 0.0) for c in ("pitch", "yaw", "roll")])
            jump = float(np.max(np.abs(v1 - v0)))
            turn = _turn_deg(v0, v1)
            if jump > 60.0 and turn < 0.5 * jump and _detour(v0, v1) > 25.0:
                out.append((t0, f"{part} spun between ticks {t0} and {t1} (numbers {jump:.0f}, turn {turn:.0f})"))
        if emote.get("isLoop"):
            a, b_ = by_tick.get(emote.get("returnTick", 0), by_tick[ticks[0]]), by_tick[ticks[-1]]
            if any(abs(a.get(c, 0.0) - b_.get(c, 0.0)) > 2.0 for c in ("pitch", "yaw", "roll")):
                out.append((ticks[-1], f"{part} loop ends elsewhere than it starts"))
    return out


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
# 2.3.2: solved for a fist in front of the left chest, the elbow out (it was set by hand and folded through the chest)
L_TUCK = {"pitch": -42.88, "yaw": 41.72, "roll": -26.88, "bend": -80.75}


def b(tick, easing, blade, left=None, body=None, legs=None, head=None, **kw):
    left = left or L_REST
    return Beat(tick, easing, blade=blade, left=dict(left) if isinstance(left, dict) else left, body=body or {}, legs=legs if legs is not None else LEGS_EASY,
                head=head or {}, **kw)


def direct(beat):
    """The beat reached along the shortest arc of the blade (densify), not routed out in front."""
    beat.direct = True
    return beat


def stance_beat(tick, easing="inoutsine", body=None, legs=None, head=None):
    """The blade's home pose (2.3.2: each blade its own: PROFILE.home), where every cut starts and ends."""
    return b(tick, easing, held(*(PROFILE.home or LOW_STANCE)), L_REST, body or {"yaw": -6, "bend": 2}, legs if legs is not None else LEGS_EASY,
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


# 2.3.2: per blade (Blade.noto_p, noto_out, noto_pull, set below): the left hand lifts the saya higher (its tail swung down onto the
# line used to graze the floor) and Gryphon's line runs closer to the shoulder (its hard curve turns the handle off the line).
# the noto line passes this point (layer frame px: in front of the right shoulder, at the upper chest) and the koiguchi; NOTO_OUT is how
# far out of the saya the blade is when the right arm is stretched along it (the tip then just inside the mouth)
NOTO_P = (-5.0, 4.0, -4.5)
NOTO_OUT = 14.5
# the owner: "why not just make the player pull the sheath forward a bit and make it fit": the left hand draws the saya a little
# forward and in along the obi as the blade comes out of it or goes back in (body-part px: in, up, forward)
NOTO_PULL = (-1.2, -1.8, -2.2)
KATANA.noto_p, KATANA.noto_out, KATANA.noto_pull = NOTO_P, NOTO_OUT, NOTO_PULL
GRYPHON.noto_p, GRYPHON.noto_out, GRYPHON.noto_pull = NOTO_P, 12.0, NOTO_PULL
YORU.noto_p, YORU.noto_out, YORU.noto_pull = NOTO_P, NOTO_OUT, NOTO_PULL


def noto(tick, easing, out, body=None, legs=None, head=None, left="saya"):
    """The katana on the noto line, out model px out of its saya (0: home)."""
    beat = Beat(tick, easing, blade=None, left=left, body=body or {}, legs=legs if legs is not None else LEGS_EASY, head=head or {})
    beat.noto = out
    return beat


DRAW_BODY = {"bend": 8, "yaw": 16, "y": -0.11}
DRAW_LEGS = {"rightLeg": leg(-32, 4, 46), "leftLeg": leg(18, -4, 36)}
EXIT = 7.0   # model px the blade slides out along the saya before it swings free (the arm's reach allows no more)


# 2.3.2: a one-handed backhand's coil: the fist at the left hip, the blade pointing back past it, the edge out to the left (the way it
# will cut, the edge up as on the iai grip: it turns out as it swings). It used to point the blade straight out to the left from the belly, which takes a wrist bent 90 degrees: the natural
# arm stood the blade upright instead, a salute, not a coil. The free hand goes out in front (L_AIM), above the blade's way round.
# 2.3.2: the blade raised upright in front, the fist beside the right of the face (never before it), the edge to the front: the way up
# to an overhead coil (furikaburi)
RAISED = (H(6.2, -1.2, 4.0), D(0.12, 1.0, 0.12), D(0.0, 0.1, 1.0))
BACKHAND_COIL = (H(-1.8, 7.2, 5.0), D(-0.42, 0.04, -0.9), D(0.0, 1.0, 0.0))
# the pivot cut's, the body bowed deeper over its bent legs: the fist lower and further out, the blade clear of the left shoulder
PIVOT_COIL = (H(-1.4, 8.2, 5.8), D(-0.4, -0.06, -0.91), D(0.0, 1.0, 0.0))


ARC_WIND, ARC_END, ARC_OVER = 1.2, 1.12, 1.18


def widen(pose, ref, k):
    """2.3.5: a held pose (fist, blade, edge) taken k times as far from ref along the way from ref to it (k > 1: further on)."""
    f, bd, ed = pose
    rf, rb, re_ = ref
    fist = tuple(np.array(rf, float) + (np.array(f, float) - np.array(rf, float)) * k)
    blade = unit(slerp(rb, bd, k))
    edge = np.array(slerp(re_, ed, k), float)
    edge = unit(edge - blade * float(edge @ blade))
    return (fist, tuple(blade), tuple(edge))


# --- 2.4.5: the owner's reach ---------------------------------------------------------------------------------------------------
# The owner, after 2.4.4: "The swings are not fixed, the arm barely extends forward during m1 swings. (the arms should be fully
# stretched and the blade pointed forward far from the player's chest, to be the max range it can be", with a picture of a swordsman's
# lunge: the feet far apart, the front knee deep, the rear leg nearly straight, the torso side-on, the sword arm thrown straight out from
# the shoulder with the blade running on along its line, the free arm straight back. Before, every cut was straight for half a tick
# (the blade steeply up) and then folded (the elbow 45-70 degrees), the follow-through held with the blade wrapped round behind the
# left hip. Now every cut strikes with the arm straight (the fist LUNGE_REACH px from the shoulder: the elbow about 5 degrees; 8.4 was
# 22) and ends in that lunge, the blade pointed at the foe; the hand slides down the handle to its end as the arm goes out (a reach
# grip, LUNGE_SLIDE px: the handle's end in the fist, so the hand can tip the blade onto the arm's line, LUNGE_KINK degrees off it
# towards the edge, without the pommel going through the forearm). The tip reaches about 2.5 blocks ahead of the feet (it was 1.6).
LUNGE_REACH = 8.55
LUNGE_KINK = 30.0
LUNGE_SLIDE = 3.0
# a rising cut's end (the draws on the run and in the air): forward and a little up, the edge rolled over down and out to the right
# as it stops (so the arm runs under and to the right of the blade's line; edge up, the arm went up out of the first-person view)
RISE_BLADE, RISE_EDGE = (0.06, 0.18, 1.0), (0.55, -0.7, -0.2)
# the lunge's legs: the right (front) leg far forward, the knee deep; the left (rear) leg far back and nearly straight
LEGS_LUNGE_WIDE = {"rightLeg": leg(-52, 8, 74), "leftLeg": leg(40, -8, 8)}
# the free arm stretched straight back and out for balance
L_LUNGE = {"pitch": 50.0, "yaw": 0.0, "roll": -34.0, "bend": -4.0}


def world(body, d):
    """A direction given as the player faces in the world (right, up, forward), in the layer frame of a beat whose body is body."""
    return unit(rig.layer_dir_from_entity({"body": dict(body)}, (d[0], d[1], -d[2])))


def extend(pose, kink=LUNGE_KINK, reach_px=LUNGE_REACH):
    """2.4.5: (fist, blade, edge) with the arm thrown straight out: the fist reach_px from the shoulder along the blade's line tipped
    kink degrees towards the edge (the wrist's bend between them), the blade and the edge as given (layer frame)."""
    _, bd, ed = pose
    bd = unit(bd)
    ed = np.array(ed, float)
    ed = unit(ed - bd * (ed @ bd))
    k = math.radians(kink)
    arm = math.cos(k) * bd + math.sin(k) * ed
    return (tuple(SHOULDER_R + reach_px * arm), tuple(bd), tuple(ed))


def lunge(body, blade, edge, turn=0.0):
    """2.4.5: the lunge's (fist, blade, edge) for a beat whose body is body, the blade and its edge given in the world (right, up,
    forward); turn: the blade swung that many degrees further on towards its edge (the overshoot)."""
    bd = world(body, blade)
    ed = world(body, edge)
    ed = unit(ed - bd * (ed @ bd))
    if turn:
        r = math.radians(turn)
        bd, ed = unit(math.cos(r) * bd + math.sin(r) * ed), unit(math.cos(r) * ed - math.sin(r) * bd)
    return extend((None, tuple(bd), tuple(ed)))


def reaching(beat):
    """2.4.5: a beat in the reach grip (the hand at the handle's end, the arm out)."""
    beat.slide = LUNGE_SLIDE
    beat.lunge = True
    # the elbow kept straight (the solver's price on the bend; a fist a twentieth of a pixel short of the reach left it 12 degrees)
    beat.elbow = -2.0
    return beat


def strike(name, duration, wind, mid, cut_ticks, description, wind_body, end_body, end_blade, end_edge, wind_left=L_AIM,
           end_left=L_LUNGE, wind_legs=LEGS_BACK, end_legs=LEGS_LUNGE_WIDE, wind_tick=3, hold=2, head_wind=None, head_end=None,
           cuts=None, start=None, raise_=None, mid_extended=True):
    """A heavy one-handed cut: from the low stance, a big wind-up (the hips coiled the other way), a held coil, then the strike in two
    ticks (fast out of the coil, the edge leading, the arm straight), an overshoot, a hold in the lunge at the end of the cut (the
    weight of it), and an easy recovery into the stance. wind/mid: (fist, blade, edge) of the coil and the middle of the cut (layer
    frame); 2.4.5: end_blade/end_edge: where the blade points at the end and its edge faces, in the world as the player faces (right,
    up, forward): the lunge (see LUNGE_REACH)."""
    lo, hi = cut_ticks
    # 2.3.5 (the owner: "longer swings", "more range"): the coil taken further back round the cut's arc from its middle (the blade's
    # direction along its great circle, the fist along its line, then held to the arm's reach)
    wind = widen(wind, mid, ARC_WIND)
    if mid_extended:
        mid = extend(mid)
    else:
        mid = held(*mid)
    if head_end is None:
        # the head turned back to the foe from the side-on body
        head_end = {"yaw": -0.8 * end_body.get("yaw", 0.0), "pitch": -4}
    over = dict(end_body)
    over["z"] = end_body.get("z", 0.0) - 0.04
    over["y"] = end_body.get("y", 0.0) - 0.02
    end = lunge(end_body, end_blade, end_edge)
    overshoot = lunge(over, end_blade, end_edge, turn=6.0)
    beats = [start or stance_beat(0, "linear")]
    coil = dict(wind_body)
    if raise_ is not None:
        # 2.3.2: an overhead coil is reached over the top: the blade raised upright in front first, then laid back over the shoulder
        rt, pose = raise_
        half = {k: v * 0.5 for k, v in coil.items()}
        beats.append(b(rt, "insine", held(*pose), wind_left, half, wind_legs, head_wind or {}))
    beats.append(b(wind_tick, "inoutsine", held(*wind), wind_left, coil, wind_legs, head_wind or {}))
    if raise_ is not None:
        beats[-1].direct = True
    deeper = dict(coil)
    deeper["yaw"] = coil.get("yaw", 0.0) * 1.12
    deeper["bend"] = coil.get("bend", 0.0) - 2.0
    beats.append(b(lo - 1, "outsine", held(*wind), wind_left, deeper, wind_legs, head_wind or {}))
    mid_body = {k: (coil.get(k, 0.0) + end_body.get(k, 0.0)) / 2 for k in set(coil) | set(end_body)}
    mid_legs = {part: _mix(wind_legs.get(part, {}), end_legs.get(part, {}), 0.6) for part in set(wind_legs) | set(end_legs)}
    beats.append(reaching(b(lo, "insine", mid, end_left, mid_body, mid_legs, {})) if mid_extended else
                 b(lo, "insine", mid, end_left, mid_body, mid_legs, {}))
    beats.append(reaching(b(hi, "outexpo", end, end_left, end_body, end_legs, head_end)))
    beats.append(reaching(b(hi + 1, "outback", overshoot, end_left, over, end_legs, head_end)))
    beats.append(reaching(b(hi + 1 + hold, "linear", overshoot, end_left, over, end_legs, head_end)))
    beats.append(stance_beat(duration))
    return write(Move(name, duration, beats, description, cuts=cuts or ((lo, hi),)))


def moves(with_draws=True):
    """The Swordsman's moves for the blade being written (PROFILE). with_draws: the draws from the saya at the hip too (the katanas';
    2.3.2: a legendary blade is drawn the moment combat begins and never drawn again mid-fight, so it has none)."""
    write_draw = write if with_draws else (lambda m: m)
    # ------------------------------------------------------------------------------------------------ the draws (from the saya)
    # Every draw: the hand on the hilt, the saya turned about its mouth onto the noto line (up across the front of the body to the
    # right shoulder), the blade drawn out of the koiguchi along it as the right arm stretches up and out (the left hand at the mouth,
    # saya-biki), and only when the point is at the mouth does it leave the saya, whipping into the cut.
    out_dir = D(-0.8, -0.56, 0.1)
    draw_rise = [out_dir, D(-0.3, -0.4, 1), D(0.4, 0.12, 0.92), D(0.95, 0.3, 0.1)]
    draw_rise_f = [H(10.0, 0.8, 5.2), H(6.6, 4.4, 7.6), H(4.8, 4.0, 7.8), H(8.6, 3.4, 3.0)]
    still = {"bend": 8, "yaw": 14, "y": -0.11}
    draw_end = {"bend": 4, "yaw": 12, "y": -0.16, "z": -0.36}
    draw_over = dict(draw_end, z=-0.4, y=-0.18)
    write_draw(Move("sword_draw_cut", 13, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8, "y": -0.05}, legs=LEGS_EASY, head={"pitch": -6}),
        noto(1, "outsine", 0.0, body=still, legs=DRAW_LEGS, head={"pitch": -10}),
        noto(2, "insine", 7.0, body=still, legs=DRAW_LEGS, head={"pitch": -10}),
        noto(3, "linear", NOTO_OUT, body=dict(still, yaw=10), legs=DRAW_LEGS, head={"pitch": -8}),
        b(4, "linear", cut(draw_rise, draw_rise_f, 0.45), L_BACK, {"bend": 10, "yaw": 0, "y": -0.13, "z": -0.14}, LEGS_DEEP,
          {"pitch": -8}),
        # 2.4.5: out of the saya straight into the owner's lunge (see LUNGE_REACH), the blade thrown out forward and a little right
        reaching(b(5, "outexpo", lunge(draw_end, (0.1, 0.12, 1.0), (0.55, -0.75, -0.2)), L_LUNGE, draw_end, LEGS_LUNGE_WIDE,
                   {"pitch": -4, "yaw": -10})),
        reaching(b(6, "outback", lunge(draw_over, (0.1, 0.12, 1.0), (0.55, -0.75, -0.2), 6.0), L_LUNGE, draw_over, LEGS_LUNGE_WIDE,
                   {"pitch": -4, "yaw": -10})),
        reaching(b(8, "linear", lunge(draw_over, (0.1, 0.12, 1.0), (0.55, -0.75, -0.2), 6.0), L_LUNGE, draw_over, LEGS_LUNGE_WIDE,
                   {"pitch": -4, "yaw": -10})),
        stance_beat(13, body={"yaw": -6, "bend": 2, "z": -0.1}),
    ], "Swordsman: the draw cut. Stillness, the hand on the hilt, then the blade drawn out of the koiguchi and up, straight into a "
       "rising cut across that ends in a long lunge.", draw=2, cuts=((4, 5),)))

    # the dashing draw: low and far forward on the run, drawn into a rising cut from the left knee up past the right shoulder (2.4.5:
    # ending thrown out forward in the lunge, the blade up and a little right)
    rise = [out_dir, D(-0.05, -0.1, 1), D(0.6, 0.5, 0.65), D(0.9, 0.55, -0.05)]
    rise_f = [H(10.0, 0.8, 5.2), H(4.6, 5.2, 7.8), H(5.0, 2.8, 6.8), H(6.8, 0.2, 3.6)]
    dash = {"bend": 14, "yaw": 16, "y": -0.16, "z": -0.1}
    dash_end = {"bend": -2, "yaw": 18, "y": -0.14, "z": -0.46}
    dash_over = dict(dash_end, z=-0.5, y=-0.16)
    write_draw(Move("sword_advancing_draw", 14, [
        noto(0, "linear", 0.0, body={"bend": 14, "yaw": 10, "y": -0.08}, legs=LEGS_LUNGE, head={"pitch": -14}),
        noto(1, "outsine", 0.0, body=dash, legs=LEGS_DEEP, head={"pitch": -18}),
        noto(2, "insine", 7.0, body=dict(dash, z=-0.22), legs=LEGS_DEEP, head={"pitch": -18}),
        noto(3, "linear", NOTO_OUT, body=dict(dash, z=-0.3), legs=LEGS_DEEP, head={"pitch": -16}),
        b(4, "linear", cut(rise, rise_f, 0.3), L_BACK, {"bend": 18, "yaw": 8, "y": -0.16, "z": -0.38}, LEGS_DEEP, {"pitch": -14}),
        reaching(b(6, "outexpo", lunge(dash_end, RISE_BLADE, RISE_EDGE), L_LUNGE, dash_end, LEGS_LUNGE_WIDE, {"pitch": -6, "yaw": -14})),
        reaching(b(7, "outback", lunge(dash_over, RISE_BLADE, RISE_EDGE, 6.0), L_LUNGE, dash_over, LEGS_LUNGE_WIDE,
                   {"pitch": -6, "yaw": -14})),
        reaching(b(9, "linear", lunge(dash_over, RISE_BLADE, RISE_EDGE, 6.0), L_LUNGE, dash_over, LEGS_LUNGE_WIDE,
                   {"pitch": -6, "yaw": -14})),
        stance_beat(14, body={"yaw": -6, "bend": 2, "z": -0.3}),
    ], "Swordsman: the dashing draw. Low and far forward, the blade drawn on the run into a rising cut.", draw=2, cuts=((4, 6),)))

    air_end = {"bend": -4, "yaw": 18, "pitch": -8}
    write_draw(Move("sword_aerial_draw", 12, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8}, legs=LEGS_TUCK, head={"pitch": -10}),
        noto(1, "insine", 7.0, body={"bend": 8, "yaw": 16}, legs=LEGS_TUCK, head={"pitch": -12}),
        noto(2, "linear", NOTO_OUT, body={"bend": 8, "yaw": 18}, legs=LEGS_TUCK, head={"pitch": -12}),
        b(3, "linear", cut(rise, rise_f, 0.3), L_BACK, {"bend": 10, "yaw": 6, "pitch": -6}, LEGS_TUCK, {"pitch": -8}),
        reaching(b(5, "outexpo", lunge(air_end, RISE_BLADE, RISE_EDGE), L_LUNGE, air_end, LEGS_REACH, {"yaw": -14})),
        reaching(b(6, "outback", lunge(air_end, RISE_BLADE, RISE_EDGE, 6.0), L_LUNGE, air_end, LEGS_REACH, {"yaw": -14})),
        reaching(b(8, "linear", lunge(air_end, RISE_BLADE, RISE_EDGE, 6.0), L_LUNGE, air_end, LEGS_REACH, {"yaw": -14})),
        stance_beat(12, legs=LEGS_TUCK),
    ], "Swordsman: the falling draw, in the air: drawn up and across as the body turns.", draw=1, cuts=((3, 5),), ground=False))

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
    write_draw(Move("sword_quick_draw", 16, [
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
    # 2.4.5: every cut of the strings ends in the owner's lunge (see LUNGE_REACH): end_blade / end_edge in the world (right, up, forward),
    # the body side-on (yaw +: the right shoulder forward), low and far forward over the front knee. The cut stops with the edge turned
    # down (the hand tips the blade towards its edge: the arm runs under the blade's line, so in first person it comes up from the
    # lower right and the blade goes on from the fist to the crosshair; an edge to the side swung the arm across the middle of the view)
    strike("sword_kesa", 14,
           wind=(H(5.6, -4.6, 1.2), D(0.3, 0.45, -0.85), D(0.2, 0.85, 0.4)),
           mid=(H(4.0, 1.6, 7.6), D(0.15, 0.4, 1), D(0, -1, 0.4)),
           end_blade=(-0.14, -0.06, 1.0), end_edge=(-0.55, -0.83, -0.05),
           cut_ticks=(5, 6), wind_body={"yaw": -42, "bend": -10, "y": 0.0}, end_body={"yaw": 40, "bend": 4, "y": -0.17, "z": -0.42},
           head_wind={"yaw": 18, "pitch": -4},
           description="Swordsman: the kesa cut, from high over the right shoulder down across to the left, ending in a long lunge with "
                       "the arm and the blade thrown out at the foe.")
    strike("sword_gyaku_kesa", 14,
           # (2.3.5: the coil and the middle of the strike a pixel lower: the longer swing passed the fist in front of the chin)
           wind=(H(-0.6, 3.8, 5.4), D(-0.88, 0.42, -0.22), D(-0.2, 0.6, 0.78)),
           mid=(H(2.8, 3.6, 7.8), D(-0.1, 0.35, 1), D(0, -1, 0.35)),
           end_blade=(0.06, -0.06, 1.0), end_edge=(0.55, -0.83, -0.05),
           cut_ticks=(5, 6), wind_body={"yaw": 46, "bend": -6}, end_body={"yaw": 22, "bend": 4, "y": -0.17, "z": -0.38},
           wind_left=L_TUCK, head_wind={"yaw": -16},
           description="Swordsman: the reverse kesa, from high over the left shoulder down across to the right, ending in a long lunge.")
    strike("sword_yoko", 13,
           wind=(H(8.2, 5.4, -2.0), D(0.72, 0.12, -0.68), D(0.68, 0, 0.72)),
           mid=(H(4.6, 5.0, 8.0), D(0.2, 0.02, 1), D(-1, 0, 0.2)),
           end_blade=(-0.16, 0.06, 1.0), end_edge=(-0.35, -0.94, 0.06),
           cut_ticks=(5, 6), wind_body={"yaw": -52, "bend": 4, "y": -0.05}, end_body={"yaw": 42, "bend": 6, "y": -0.16, "z": -0.4},
           head_wind={"yaw": 24},
           description="Swordsman: the horizontal cut, right to left, the hips wound back and unwound through it into a long lunge.")
    strike("sword_gyaku_yoko", 13,
           wind=BACKHAND_COIL,
           mid=(H(3.4, 5.0, 8.0), D(-0.15, 0.02, 1), D(1, 0, 0.15)),
           end_blade=(0.04, 0.06, 1.0), end_edge=(0.35, -0.94, 0.06),
           cut_ticks=(5, 6), wind_body={"yaw": 50, "bend": 4, "y": -0.05}, end_body={"yaw": 20, "bend": 6, "y": -0.16, "z": -0.38},
           wind_left=L_AIM, head_wind={"yaw": -22},
           description="Swordsman: the return cut, a backhand from the left hip across to the right, the arm thrown out straight.")
    strike("sword_kiriage", 14,
           wind=(H(1.6, 9.0, 4.2), D(-0.55, -0.74, 0.38), D(0.45, -0.25, 0.86)),
           mid=(H(4.0, 4.8, 8.0), D(0.02, 0.0, 1), D(0, 1, 0)),
           end_blade=(0.06, 0.18, 1.0), end_edge=(0.55, -0.7, -0.2),
           cut_ticks=(5, 6), wind_body={"yaw": 40, "bend": 22, "y": -0.14}, end_body={"yaw": 24, "bend": -4, "y": -0.1, "z": -0.36},
           wind_legs=LEGS_DEEP, wind_left=L_BACK, head_wind={"yaw": -14, "pitch": -6}, mid_extended=False,
           description="Swordsman: the rising cut, from the left knee up past the right shoulder, rising with it into the lunge.")
    strike("sword_tsuki", 14,
           wind=(H(6.6, 7.6, -2.0), D(0.04, 0.08, 1), D(0, -1, 0.08)),
           mid=(H(4.0, 5.6, 6.0), D(0.0, 0.05, 1), D(0, -1, 0.05)),
           end_blade=(0.0, 0.22, 1.0), end_edge=(0.0, -1.0, 0.22),
           cut_ticks=(5, 7), wind_body={"yaw": -38, "bend": 4, "y": -0.07, "z": 0.06},
           end_body={"yaw": 44, "bend": 6, "y": -0.2, "z": -0.52}, wind_left=L_AIM, head_wind={"yaw": 16}, mid_extended=False,
           description="Swordsman: the thrust, drawn back to the hip and driven in with a long lunge, the arm and the blade one line.")
    strike("sword_makko", 17,
           wind=(H(5.8, -6.2, 0.8), D(0.22, -0.32, -0.92), D(0.1, 0.94, -0.3)),
           mid=(H(5.4, -0.8, 6.6), D(0.12, 0.62, 0.78), D(0, -0.78, 0.62)),
           end_blade=(0.02, -0.1, 1.0), end_edge=(0.0, -1.0, -0.1),
           cut_ticks=(7, 8), wind_tick=4, hold=3, raise_=(2, RAISED), wind_body={"yaw": -20, "bend": -16, "y": 0.02},
           end_body={"yaw": 30, "bend": 10, "y": -0.22, "z": -0.46}, wind_left=L_AIM,
           head_wind={"pitch": -10, "yaw": 8},
           description="Swordsman: the heaven splitter, the finisher: raised high behind the shoulder, then straight down through the "
                       "centre with a long step in, the arm and the blade thrown out at the foe.")

    # ------------------------------------------------------------------------------------------------ the moving string
    strike("sword_advancing_cut", 13,
           wind=(H(8.0, 7.6, -1.2), D(0.7, -0.2, -0.68), D(0.68, 0, 0.72)),
           mid=(H(4.6, 7.2, 7.6), D(0.2, -0.12, 1), D(-1, 0, 0.2)),
           end_blade=(-0.16, -0.2, 1.0), end_edge=(-0.4, -0.88, -0.2),
           cut_ticks=(5, 6), wind_body={"yaw": -44, "bend": 22, "y": -0.16, "z": -0.1},
           end_body={"yaw": 40, "bend": 14, "y": -0.24, "z": -0.52}, wind_legs=LEGS_DEEP,
           head_wind={"yaw": 18, "pitch": -16},
           description="Swordsman: the advancing cut, a long low step and a cut across at knee height at the end of it, thrown out far.")

    # the pivot cut: a full turn to the right on the front foot, the arm flung out flat, the blade sweeping everything round
    # 2.4.5: the arm out straight through the spin and at its end (the owner's reach)
    spin = extend((None, D(0.98, 0.03, 0.18), D(0.18, 0, -0.98)))
    spin_end = extend((None, D(0.96, 0.04, 0.28), D(0.28, 0, -0.96)))
    write(Move("sword_pivot_cut", 15, [
        stance_beat(0, "linear"),
        b(3, "inoutsine", held(*PIVOT_COIL), L_AIM, {"yaw": 52, "bend": 6, "y": -0.1}, LEGS_DEEP, {"yaw": -20}),
        b(4, "outsine", held(*PIVOT_COIL), L_AIM, {"yaw": 58, "bend": 4, "y": -0.11}, LEGS_DEEP, {"yaw": -22}),
        b(5, "insine", held(H(5.0, 5.6, 7.6), D(0.25, 0.04, 1), D(1, 0, -0.25)), L_AIM, {"yaw": 10, "bend": 8, "y": -0.1}, LEGS_DEEP,
          {"yaw": -10}),
        reaching(b(7, "linear", spin, L_OUT, {"yaw": -110, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -8})),
        reaching(b(9, "linear", spin, L_OUT, {"yaw": -230, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -8})),
        reaching(b(10, "outsine", spin, L_OUT, {"yaw": -330, "bend": 6, "y": -0.08}, LEGS_DEEP, {"yaw": -6})),
        reaching(b(11, "constant", spin_end, L_OUT, {"yaw": 22, "bend": 6, "y": -0.07}, LEGS_LUNGE, {"yaw": -4})),
        reaching(b(12, "linear", spin_end, L_OUT, {"yaw": 18, "bend": 6, "y": -0.07}, LEGS_LUNGE, {"yaw": -4})),
        stance_beat(15),
    ], "Swordsman: the pivot cut, a full turn on the front foot with the arm flung out flat, all round.", cuts=((5, 10),)))

    # ------------------------------------------------------------------------------------------------ the aerial string
    write(Move("sword_aerial_cross", 14, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(2, "inoutsine", held(H(5.6, -4.4, 1.2), D(0.3, 0.45, -0.85), D(0.2, 0.85, 0.4)), L_AIM, {"yaw": -36, "bend": -8}, LEGS_TUCK,
          {"yaw": 14}),
        b(3, "insine", held(H(4.0, 1.6, 7.6), D(0.15, 0.4, 1), D(0, -1, 0.4)), L_BACK, {"yaw": -6, "bend": 8}, LEGS_TUCK),
        # 2.4.5: each diagonal ends with the arm and the blade thrown out at the foe (the owner's reach)
        reaching(b(4, "outexpo", lunge({"yaw": 30, "bend": 8}, (-0.16, -0.12, 1.0), (-0.55, -0.83, -0.05)), L_LUNGE,
                   {"yaw": 30, "bend": 8}, LEGS_TUCK, {"yaw": -18})),
        b(6, "inoutsine", held(H(-0.6, 2.6, 5.4), D(-0.88, 0.42, -0.22), D(-0.2, 0.6, 0.78)), L_TUCK, {"yaw": 40, "bend": -4},
          LEGS_TUCK, {"yaw": -14}),
        b(8, "insine", held(H(2.8, 2.6, 7.8), D(-0.1, 0.35, 1), D(0, -1, 0.35)), L_OUT, {"yaw": 6, "bend": 8}, LEGS_TUCK),
        reaching(b(9, "outexpo", lunge({"yaw": 16, "bend": 8}, (0.18, -0.12, 1.0), (0.55, -0.83, -0.05)), L_LUNGE,
                   {"yaw": 16, "bend": 8}, LEGS_REACH, {"yaw": -10})),
        reaching(b(10, "outback", lunge({"yaw": 16, "bend": 9}, (0.18, -0.12, 1.0), (0.55, -0.83, -0.05), 6.0), L_LUNGE,
                   {"yaw": 16, "bend": 9}, LEGS_REACH, {"yaw": -10})),
        stance_beat(14, legs=LEGS_TUCK),
    ], "Swordsman: the aerial cross, two diagonals crossing in the air, each its own cut.", cuts=((3, 4), (8, 9)), ground=False))

    # 2.3.2: the fist carried out to the right of the face on its way from the wind-up (at 6.6 the forearm crossed the brow)
    # (2.4.5: 0.5 px further right: the wider wrist let the solver bring the fist past the face between ticks 2 and 3)
    wheel = held(H(7.9, -1.0, 6.0), D(0.14, 0.66, 0.74), D(0, 0.74, -0.66))
    # 2.4.5: the wheel's end thrown out at the foe (the owner's reach), the arm straight
    wheel_end = extend((None, D(0.04, -0.4, 0.92), D(0, -0.92, -0.4)))
    write(Move("sword_aerial_wheel", 15, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(1, "insine", held(*RAISED), L_AIM, {"pitch": 6, "bend": -6}, LEGS_TUCK, {"pitch": -4}),
        direct(b(2, "outsine", held(H(7.0, -6.0, 1.0), D(0.22, -0.32, -0.92), D(0.1, 0.94, -0.3)), L_AIM, {"pitch": 12, "bend": -12},
                 LEGS_TUCK, {"pitch": -8})),
        b(4, "linear", wheel, L_BACK, {"pitch": -70, "bend": 12}, LEGS_TUCK),
        b(6, "linear", wheel, L_BACK, {"pitch": -180, "bend": 14}, LEGS_TUCK),
        b(8, "linear", wheel, L_BACK, {"pitch": -290, "bend": 12}, LEGS_TUCK),
        reaching(b(9, "outsine", wheel_end, L_LUNGE, {"pitch": -352, "bend": 14}, LEGS_REACH)),
        reaching(b(10, "constant", wheel_end, L_LUNGE, {"pitch": 8, "bend": 14}, LEGS_REACH)),
        reaching(b(11, "linear", extend((None, D(0.04, -0.5, 0.86), D(0, -0.86, -0.5))), L_LUNGE, {"bend": 14}, LEGS_REACH)),
        stance_beat(15, legs=LEGS_TUCK),
    ], "Swordsman: the wheel, a forward somersault with the blade carried round a full vertical circle.", cuts=((4, 9),), ground=False))

    write(Move("sword_plunge", 17, [
        stance_beat(0, "linear", legs=LEGS_TUCK),
        b(3, "inoutsine", held(H(7.6, -5.2, 1.6), D(0.25, 0.93, 0.25), D(0, -0.25, 1)), L_AIM, {"bend": -14, "pitch": 8}, LEGS_TUCK,
          {"pitch": -12}),
        b(5, "outsine", held(H(7.7, -5.6, 1.2), D(0.25, 0.95, 0.18), D(0, -0.2, 1)), L_AIM, {"bend": -16, "pitch": 8}, LEGS_TUCK,
          {"pitch": -12}),
        b(6, "insine", held(H(4.8, 1.0, 6.8), D(0.1, -0.2, 1), D(0, -1, -0.2)), L_BACK, {"bend": 8}, LEGS_TUCK, {"pitch": 4}),
        # 2.4.5: driven down with the arm straight out in front (the owner's reach)
        reaching(b(7, "outexpo", extend((None, D(0.03, -0.96, 0.28), D(0, 0.28, 0.96))), L_OUT, {"bend": 28, "y": -0.22}, LEGS_DEEP,
                   {"pitch": 16})),
        reaching(b(9, "outback", extend((None, D(0.02, -1, 0.1), D(0, 0.1, 1))), L_OUT, {"bend": 32, "y": -0.32}, LEGS_DEEP,
                   {"pitch": 18})),
        reaching(b(12, "linear", extend((None, D(0.02, -1, 0.1), D(0, 0.1, 1))), L_OUT, {"bend": 30, "y": -0.3}, LEGS_DEEP,
                   {"pitch": 16})),
        stance_beat(17),
    ], "Swordsman: the plunge, the aerial finisher: raised high, then driven point-down as the body drops onto it.",
        cuts=((6, 9),), ground=False))

    # ------------------------------------------------------------------------------------------------ stance, charge, release, sheathe

    charge_pose = held(H(7.6, -5.8, 0.6), D(0.3, -0.28, -0.91), D(0.1, 0.95, -0.28))
    charge_body = {"yaw": -26, "bend": -16, "y": -0.04}
    charge_legs = {"rightLeg": leg(8, 6, 26), "leftLeg": leg(-24, -5, 34)}
    shake = held(H(7.7, -6.0, 0.4), D(0.32, -0.26, -0.91), D(0.1, 0.95, -0.28))
    write(Move("sword_charge", 26, [
        stance_beat(0, "linear"),
        b(2, "inoutsine", held(H(9.6, 3.0, 2.4), D(0.75, 0.3, -0.6), D(0.2, 0.9, 0.3)), L_AIM, dict(charge_body, bend=-6), charge_legs,
          {"pitch": -6, "yaw": 8}),
        b(5, "outsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(16, "inoutsine", shake, L_AIM, dict(charge_body, bend=-18, y=-0.06), charge_legs, {"pitch": -10, "yaw": 10}),
        b(26, "inoutsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
    ], "Swordsman: charging the slash, the blade raised high behind the right shoulder, the body drawn back like a bow (held until "
       "release).", loop=6))

    write_draw(Move("sword_charge_draw", 26, [
        noto(0, "linear", 0.0, body={"bend": 8, "yaw": 8, "y": -0.05}, head={"pitch": -8}),
        noto(1, "insine", 7.0, body={"bend": 8, "yaw": 12, "y": -0.06}, legs=DRAW_LEGS, head={"pitch": -8}),
        noto(2, "linear", NOTO_OUT, body={"bend": 8, "yaw": 12, "y": -0.06}, legs=DRAW_LEGS, head={"pitch": -6}),
        b(3, "outexpo", held(H(8.0, -1.0, 4.4), D(0.35, 0.92, 0.15), D(0, -0.15, 1)), L_BACK, {"yaw": 0, "bend": -4}, LEGS_EASY),
        b(5, "outsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(16, "inoutsine", shake, L_AIM, dict(charge_body, bend=-18, y=-0.06), charge_legs, {"pitch": -10, "yaw": 10}),
        b(26, "inoutsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
    ], "Swordsman: drawing straight up into the charge.", draw=1, loop=6))

    release_end = {"yaw": 30, "bend": 10, "y": -0.22, "z": -0.46}
    release_over = dict(release_end, z=-0.5, y=-0.24)
    write(Move("sword_release", 15, [
        b(0, "linear", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(1, "insine", held(H(5.4, -0.8, 6.6), D(0.12, 0.62, 0.78), D(0, -0.78, 0.62)), L_BACK, {"yaw": -6, "bend": 2, "z": -0.1},
          LEGS_LUNGE, {"pitch": -2}),
        # 2.4.5: into the owner's lunge, the arm and the blade thrown out at the foe
        reaching(b(2, "outexpo", lunge(release_end, (0.02, -0.1, 1.0), (0.0, -1.0, -0.1)), L_LUNGE, release_end, LEGS_LUNGE_WIDE,
                   {"pitch": -4, "yaw": -24})),
        reaching(b(3, "outback", lunge(release_over, (0.02, -0.1, 1.0), (0.0, -1.0, -0.1), 6.0), L_LUNGE, release_over,
                   LEGS_LUNGE_WIDE, {"pitch": -4, "yaw": -24})),
        reaching(b(7, "linear", lunge(release_over, (0.02, -0.1, 1.0), (0.0, -1.0, -0.1), 6.0), L_LUNGE, release_over,
                   LEGS_LUNGE_WIDE, {"pitch": -4, "yaw": -24})),
        stance_beat(15, body={"yaw": -6, "bend": 2, "z": -0.3}),
    ], "Swordsman: the charged slash released: straight down from high behind the shoulder into a long lunge.",
        cuts=((1, 3),)))

    # 2.3.6 (the owner: "every move is clashable ... I want a impact frame whenever a clash happens"): a cut met by another blow
    # (server/Clashes) stops dead and the blade is knocked back: up and over the right shoulder by the shock, the body leaning away and
    # a step back to catch it, the free hand out for balance; then the blade comes down to the home pose. SwordAnims.CLASH_TICKS long.
    write(Move("sword_clash", 12, [
        b(0, "linear", held(H(3.2, 3.6, 6.6), D(0.18, 0.78, 0.6), D(0.0, -0.6, 0.78)), L_OUT, {"yaw": 4, "bend": -4, "z": 0.08},
          LEGS_EASY, {"pitch": -10}),
        b(2, "outexpo", held(H(5.6, 0.2, 4.6), D(0.22, 0.95, -0.2), D(0.0, 0.2, 0.98)), L_OUT, {"yaw": 10, "bend": -12, "y": -0.03,
          "z": 0.2}, LEGS_BACK, {"pitch": -18}),
        b(5, "inoutsine", held(H(2.6, 4.8, 5.8), D(0.08, 0.72, 0.69), D(0.0, -0.69, 0.72)), L_REST, {"yaw": 4, "bend": 6, "y": -0.1,
          "z": 0.14}, LEGS_BACK, {"pitch": 2}),
        stance_beat(12, body={"yaw": -6, "bend": 2, "z": 0.04}),
    ], "Swordsman: the cut met by another blow and stopped dead: the blade knocked up over the shoulder, a step back, the guard found "
       "again."))

    # 2.2.1 (the owner: "the sheath and unsheath is too fast. Slow those animations down. Don't make the blade usable until it's fully
    # unsheathed ... It should be the only drawback from having the swordsman ... Unless it's a quick draw"): the draw, slow and
    # deliberate: the right hand settles on the hilt, the left thumb pushes the guard out (koiguchi wo kiru), the blade slides out of
    # the saya along the noto line a little at a time, and only when its point is at the mouth does it swing free, out to the right and
    # down into the low guard. Nothing is cut and nothing can be done with it until it is there.


def reach_beat(tick, easing, fist, left="saya", body=None, legs=None, head=None):
    """2.3.2: the empty right hand at fist (H) on its way to the hilt or back from it (the arm solved for the fist alone)."""
    beat = Beat(tick, easing, blade=None, left=left, body=body or {}, legs=legs if legs is not None else LEGS_EASY, head=head or {})
    beat.fist = H(*fist)
    return beat


def hip_draws():
    """The draw from the saya at the hip and the sheathing (noto), for the blade being written (a katana, or Gryphon's saber along its
    own hard curve). 2.2.1 (the owner: "the sheath and unsheath is too fast. Slow those animations down"); 2.3.2 (the owner: "The pull
    out animation is way too fast on some swords, yoru should be slower than all"): a katana's draw 28 ticks, its sheathing 24;
    Gryphon's 32 and 28. Slow and deliberate: the right hand settles on the hilt, the left thumb pushes the guard out (koiguchi wo
    kiru), the blade slides out of the saya along the noto line a little at a time, and only when its point is at the mouth does it
    swing free: a katana out to the right and down into the low guard; Gryphon's saber whirled up and out to the right, its bell guard
    flashing, and let fall loose to the side. Nothing is cut and nothing can be done with it until it is there."""
    # the right shoulder turned in to the hilt and the body bowed a little over it (Gryphon's grip sits further from the right hand)
    calm = {"bend": 8, "yaw": 16, "y": -0.04} if PROFILE is GRYPHON else {"bend": 5, "yaw": 8, "y": -0.04}
    rest = Beat(0, "linear", rarm=dict(RELAXED_R), wrist={}, left="saya", body={"bend": 2}, legs=LEGS_EASY)
    # the blade, free of the saya, whips out forward first (the draw's own cut, nukitsuke), then swings out to the side
    whip = held(H(5.6, 4.6, 7.4), D(0.42, -0.12, 0.9), D(0.05, -0.99, 0.0))
    if PROFILE is GRYPHON:
        t = {"grip": 6, "push": 10, "hold": 13, "half": 20, "out": 24, "free": 27, "settle": 29, "end": 32}
        free = held(H(9.0, -1.2, 3.4), D(0.78, 0.6, 0.2), D(0.3, -0.1, -0.95))
        settle = held(H(8.4, 6.6, 2.6), D(0.6, -0.25, 0.76), D(0.4, -0.85, -0.3))
        free_body, free_head = {"yaw": -14, "bend": 2}, {"pitch": -6, "yaw": 12}
    else:
        t = {"grip": 5, "push": 9, "hold": 11, "half": 17, "out": 20, "free": 23, "settle": None, "end": 28}
        free = held(H(8.6, 3.0, 2.2), D(1, 0.32, -0.05), D(0.1, 0.1, -1))
        settle = None
        free_body, free_head = {"yaw": -10, "bend": 4}, {"pitch": -4, "yaw": 8}
    beats = [
        rest,
        reach_beat(t["grip"] - 3, "insine", (3.0, 8.4, 4.4), body={"bend": 3, "yaw": 4}, head={"pitch": -4}),
        noto(t["grip"], "outsine", 0.0, body=calm, legs=LEGS_EASY, head={"pitch": -6}),
        noto(t["push"], "outsine", 1.2, body=calm, legs=LEGS_EASY, head={"pitch": -8}),
        noto(t["hold"], "linear", 1.2, body=calm, legs=LEGS_EASY, head={"pitch": -8}),
        noto(t["half"], "inoutsine", 8.0, body=dict(calm, yaw=10), legs=DRAW_LEGS, head={"pitch": -8}),
        noto(t["out"], "outsine", PROFILE.noto_out, body=dict(calm, yaw=8), legs=DRAW_LEGS, head={"pitch": -6}),
        b(t["out"] + 1, "outsine", whip, L_REST, dict(calm, yaw=2), DRAW_LEGS, {"pitch": -6}),
        b(t["free"], "inoutsine", free, L_REST, free_body, LEGS_EASY, free_head),
    ]
    if settle is not None:
        beats.append(b(t["settle"], "outback", settle, L_REST, {"yaw": -8, "bend": 3}, LEGS_EASY, {"pitch": -4, "yaw": 6}))
    beats.append(stance_beat(t["end"], "inoutsine"))
    what = "Gryphon's saber" if PROFILE is GRYPHON else "the blade"
    write(Move("sword_draw", t["end"], beats, f"Swordsman: the draw, slow and deliberate: the thumb pushes the guard out, {what} slides "
               "out of the saya along its own curve, swings free and settles into the low guard.", draw=t["out"]))

    # sheathing (noto), as slow: the blade flicked clean (chiburi), held a beat, its back laid to the koiguchi, slid home along the saya
    # a little at a time, the guard clicking home at the end
    if PROFILE is GRYPHON:
        t = {"flick": 4, "hold": 6, "lay": 11, "rest": 13, "deep": 21, "home": 24, "end": 28}
        flick = held(H(8.8, 7.0, 3.6), D(0.86, -0.3, 0.4), D(0.35, -0.6, -0.72))
    else:
        t = {"flick": 4, "hold": 6, "lay": 10, "rest": 12, "deep": 18, "home": 21, "end": 24}
        flick = held(H(5.6, 8.8, 5.4), D(0.12, -0.78, 0.6), D(0.05, -0.6, -0.8))
    write(Move("sword_sheathe", t["end"], [
        stance_beat(0, "linear"),
        b(t["flick"], "outexpo", flick, L_REST, {"yaw": -4, "bend": 6}, LEGS_EASY),
        b(t["hold"], "linear", flick, L_REST, {"yaw": -4, "bend": 6}, LEGS_EASY),
        b(t["lay"] - 2, "inoutsine", whip, L_REST, {"bend": 6, "yaw": 2}, LEGS_EASY),
        noto(t["lay"], "outsine", PROFILE.noto_out, body={"bend": 6, "yaw": 6}),
        noto(t["rest"], "linear", PROFILE.noto_out, body={"bend": 6, "yaw": 6}),
        noto(t["deep"], "inoutsine", 3.0, body={"bend": 6, "yaw": 6}),
        noto(t["home"], "outsine", 0.0, body={"bend": 5, "yaw": 6}),
        reach_beat(t["home"] + 2, "inoutsine", (3.0, 8.4, 4.4), body={"bend": 4, "yaw": 4}),
        Beat(t["end"], "inoutsine", rarm=dict(RELAXED_R), wrist={}, left="saya", body={"bend": 3}, legs=LEGS_EASY),
    ], "Swordsman: the blade flicked clean, laid to the mouth of the saya and slid home, slowly.", sheath=t["home"], stop_extra=6))


# the upper arm raised, a little out to the right and forward (layer frame): reaching up behind the head
OVERHEAD = (-0.3, -0.94, -0.12)


def back_beat(tick, easing, lift=0.0, reach_off=None, body=None, legs=None, head=None, left=None):
    """2.2.7: the right fist on Yoru's handle on the back (lift px drawn up along itself), or reaching for it (reach_off px away)."""
    beat = Beat(tick, easing, blade=None, left=dict(left or L_REST), body=body or {}, legs=legs if legs is not None else LEGS_EASY,
                head=head or {})
    beat.back_lift = lift
    if reach_off is not None:
        beat.reach_off = reach_off
    return beat


# 2.3.2: each blade's home pose (where its cuts start and end): a katana low in the right hand, its point at the ground ahead; Gryphon's
# saber hanging looser and further out to the side; Yoru, a great sword, held low and forward, its point well off the floor
KATANA.home = LOW_STANCE
GRYPHON.home = (H(7.2, 10.2, 2.0), D(0.45, -0.5, 0.74), D(0.2, -0.78, -0.55))
YORU.home = (H(6.8, 9.0, 3.6), D(0.3, -0.38, 0.88), D(0.1, -0.9, -0.36))
# carried on the right shoulder: the back of the blade over it, the edge up (the owner: "the blade edge should be facing up"), the
# point high behind; the hand up in front of the shoulder, the elbow down, so the blade passes over the top of the arm, not along it
# (the owner: "the sword handle clipping through the shoulder?")
SHOULDER = {"gryphon": (H(6.2, 0.6, 4.6), D(0.0, 0.5, -0.87), D(0.0, 0.87, 0.5)),
            "yoru": (H(6.2, 0.2, 4.8), D(0.0, 0.56, -0.83), D(0.0, 0.83, 0.56))}
# Yoru presented: low at the right hip, its long blade angled up across the front (the owner's unsheath reference ends so)
PRESENT = (H(3.4, 8.6, 3.6), D(-0.48, 0.52, 0.72), D(0.0, 0.84, -0.55))


def halfway(a, b_, u):
    """A held pose u of the way from a to b_ (fist mixed, blade and edge slerped)."""
    return (tuple(np.array(a[0]) * (1 - u) + np.array(b_[0]) * u), tuple(slerp(a[1], b_[1], u)), tuple(slerp(a[2], b_[2], u)))


def shifted(pose, dfist=(0.0, 0.0, 0.0)):
    """A held pose with its fist moved (H px: right, down, forward)."""
    f, bd, ed = pose
    return (tuple(np.array(f) + np.array(H(*dfist))), bd, ed)


def carry():
    """2.3.2 (the owner: "using sword makes the player's body too stiff, the head doesnt move ... adapt it so the body is more free,
    more real, more ambient animation with all swords ... Yoru and Gryphon have custom ambient animations", "Also the left arm is not
    even moving... why is it frozen"): with the blade out between cuts,
    - on the move, sword_guard keys the right arm and the wrist alone: the head looks round, the body and the left arm swing with the
      walk as the game moves them. A katana held low; Gryphon and Yoru carried on the right shoulder.
    - standing still, sword_idle: the whole body breathes and shifts its weight, the free hand hangs easy and moves with it, the blade
      lives in the hand (a katana turned to the light and flicked; Gryphon's saber turned over lazily and rested on the shoulder; Yoru
      raised and presented across the front, then shouldered), the head always left to look where the player looks."""
    home = PROFILE.home
    if PROFILE is KATANA:
        walk = [b(0, "linear", held(*home), L_REST, {}, {}),
                b(20, "inoutsine", held(*shifted(home, (0.1, -0.4, 0.3))), L_REST, {}, {}),
                b(40, "inoutsine", held(*home), L_REST, {}, {})]
    else:
        sh = SHOULDER[PROFILE.name]
        walk = [b(0, "linear", held(*sh), L_REST, {}, {}),
                b(20, "inoutsine", held(*shifted(sh, (0.0, -0.4, 0.2))), L_REST, {}, {}),
                b(40, "inoutsine", held(*sh), L_REST, {}, {})]
    write(Move("sword_guard", 40, walk, "Swordsman: the blade between cuts on the move (the right arm alone: walking, looking round "
               "and the free arm stay the game's).", parts=RIGHT, loop=0))

    easy = {"yaw": -6, "bend": 2}
    breath = {"yaw": -7, "bend": 3.4}
    shift_legs = {"rightLeg": leg(-3, 3, 4), "leftLeg": leg(7, -3, 15)}
    l_breath = {"pitch": 3.0, "yaw": 0.0, "roll": -10.0, "bend": -13.0}
    l_sway = {"pitch": 9.0, "yaw": 0.0, "roll": -6.0, "bend": -8.0}
    if PROFILE is KATANA:
        turned = (H(7.0, 9.4, 3.0), D(0.4, -0.45, 0.8), D(0.9, 0.1, -0.4))
        flick = (H(7.4, 9.0, 3.4), D(0.55, -0.22, 0.8), D(0.75, 0.2, -0.6))
        beats = [
            b(0, "linear", held(*home), L_REST, easy, LEGS_EASY),
            b(22, "inoutsine", held(*shifted(home, (0.0, -0.4, 0.1))), l_breath, breath, LEGS_EASY),
            b(44, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
            b(56, "inoutsine", held(*turned), l_sway, {"yaw": -10, "bend": 2.5}, LEGS_EASY),
            b(66, "linear", held(*shifted(turned, (0.0, -0.2, 0.0))), l_sway, {"yaw": -10, "bend": 3}, LEGS_EASY),
            b(78, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
            b(92, "inoutsine", held(*shifted(home, (0.3, 0.1, 0.0))), l_breath, {"yaw": -4, "bend": 3, "roll": 1.5}, shift_legs),
            b(100, "outexpo", held(*flick), l_breath, {"yaw": -5, "bend": 2.5, "roll": 1.5}, shift_legs),
            b(103, "outback", held(*shifted(home, (0.3, 0.0, 0.0))), L_REST, {"yaw": -5, "bend": 2.5, "roll": 1.0}, shift_legs),
            b(120, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
        ]
        duration = 120
    elif PROFILE is GRYPHON:
        sh = SHOULDER["gryphon"]
        out = (H(7.8, 9.2, 2.4), D(0.75, -0.3, 0.6), D(0.45, -0.3, -0.84))
        over = (H(7.6, 9.0, 2.8), D(0.5, -0.32, 0.8), D(0.1, 0.92, 0.3))
        up = (H(8.0, 4.0, 3.0), D(0.7, 0.55, 0.45), D(0.4, -0.8, -0.3))
        beats = [
            b(0, "linear", held(*home), L_REST, easy, LEGS_EASY),
            b(24, "inoutsine", held(*shifted(home, (0.0, -0.4, 0.1))), l_breath, breath, LEGS_EASY),
            b(46, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
            b(56, "inoutsine", held(*out), l_sway, {"yaw": -9, "bend": 2}, LEGS_EASY),
            b(62, "inoutsine", held(*over), l_sway, {"yaw": -8, "bend": 2}, LEGS_EASY),
            b(72, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
            b(82, "inoutsine", held(*up), L_REST, {"yaw": -8, "bend": 0}, LEGS_EASY),
            b(88, "inoutsine", held(*sh), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(92, "outback", held(*shifted(sh, (0.0, 0.3, 0.0))), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(112, "inoutsine", held(*shifted(sh, (0.0, -0.4, 0.0))), l_breath, {"yaw": -3, "bend": 0.5, "roll": 1.5}, shift_legs),
            b(134, "inoutsine", held(*sh), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(142, "inoutsine", held(*up), L_REST, {"yaw": -8, "bend": 0}, LEGS_EASY),
            b(150, "outsine", held(*home), L_REST, easy, LEGS_EASY),
            b(160, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
        ]
        duration = 160
    else:
        sh = SHOULDER["yoru"]
        upright = (H(7.8, 3.6, 5.4), D(0.28, 1.0, 0.05), D(0.95, -0.25, -0.2))
        out_front = (H(6.4, 5.4, 6.6), D(0.35, 0.45, 0.85), D(0.9, -0.05, -0.35))
        beats = [
            b(0, "linear", held(*home), L_REST, easy, LEGS_EASY),
            b(18, "inoutsine", held(*PRESENT), L_REST, {"yaw": 6, "bend": 1}, LEGS_EASY),
            b(24, "outsine", held(*shifted(PRESENT, (0.0, 0.2, 0.0))), L_REST, {"yaw": 7, "bend": 1.5}, LEGS_EASY),
            b(44, "inoutsine", held(*shifted(PRESENT, (0.0, -0.3, 0.0))), l_breath, {"yaw": 6, "bend": 2.5}, LEGS_EASY),
            b(64, "inoutsine", held(*PRESENT), L_REST, {"yaw": 6, "bend": 1}, LEGS_EASY),
            b(68, "inoutsine", held(*out_front), L_REST, {"yaw": 2, "bend": 0.5}, LEGS_EASY),
            b(72, "inoutsine", held(*upright), L_REST, {"yaw": -2, "bend": 0}, LEGS_EASY),
            b(78, "inoutsine", held(*sh), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(84, "outback", held(*shifted(sh, (0.0, 0.3, 0.0))), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(104, "inoutsine", held(*shifted(sh, (0.0, -0.4, 0.0))), l_breath, {"yaw": -3, "bend": 0.5, "roll": 1.5}, shift_legs),
            b(124, "inoutsine", held(*sh), l_sway, {"yaw": -4, "bend": -1}, shift_legs),
            b(132, "inoutsine", held(*upright), L_REST, {"yaw": -2, "bend": 0}, LEGS_EASY),
            b(144, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
            b(160, "inoutsine", held(*home), L_REST, easy, LEGS_EASY),
        ]
        duration = 160
    write(Move("sword_idle", duration, beats, "Swordsman: standing with the blade out: breathing, the weight shifting, the blade alive in "
               "the hand (the head left free to look).", parts=NO_HEAD, loop=0))


def yoru():
    """2.2.7: Yoru, worn on the back (the owner: "it also needs a pull out animation"; his unsheath reference: the right arm reaches up
    behind the head to the hilt over the right shoulder, one sweep, and the great sword held out in front). 2.3.2 (the owner: "Gyphon
    and Yoru's pull outs are broken", "yoru should be slower than all"): rebuilt, 44 ticks, slow and heavy: the hand finds the hilt
    behind the head, the great sword is pulled up out of its ropes, the point swings out behind, up over the head in one great arc
    (all of it on the right side, behind, above and in front: never across the body) and down in front into Yoru's guard, its point
    off the floor. Its sheathing the same arc backwards, 36 ticks. Drawn, it is held and swung with the Swordsman's own moves, each
    turned so its point never cuts into the floor."""
    reach = (-2.0, 3.4, -1.2)       # the fist below the handle and out to the right, on its way up to it
    on_back = rig.back_grip({}, 0.0)
    back_dir = tuple(-v for v in on_back[1])
    # (2.3.2: the first leg off the back less steep: the point swung out behind cleared the floor by 0.01 blocks at best)
    dirs = [D(*back_dir), D(-0.25, -0.32, -0.91), D(0.05, 0.05, -1), D(0.08, 0.75, -0.66), D(0.08, 1.0, 0.1), D(0.1, 0.55, 0.83),
            D(*[v for v in (0.3, -0.38, 0.88)])]
    # the arc starts where the hand ends its pull out of the ropes (1.6 px: as far up its line as an arm reaches)
    # its first leg takes the hilt up and out to the right of the head before it comes forward: Yoru's cross guard, behind the head
    # at the hilt, would otherwise turn into it
    fists = [tuple(rig.back_grip({}, 1.6)[0]), H(8.0, -6.4, -3.2), H(7.4, -8.0, -1.2), H(6.6, -9.4, 0.6), H(6.0, -10.0, 2.2),
             H(7.4, -4.6, 6.4), YORU.home[0]]   # (2.3.2: out and forward of the face: Yoru's cross guard reaches 7.4 px)
    n = len(dirs) - 1
    rev_dirs, rev_fists = dirs[::-1], fists[::-1]
    look = {"pitch": -8, "yaw": -6}
    beats = [
        Beat(0, "linear", rarm=dict(RELAXED_R), wrist={}, left=dict(L_REST), body={"bend": 2}, legs=LEGS_EASY),
        back_beat(6, "insine", 0.0, reach, body={"bend": 3, "yaw": -6}, head={"pitch": -4, "yaw": -4}),
        back_beat(10, "outsine", 0.0, body={"bend": 5, "yaw": -8}, head=look),
        back_beat(13, "linear", 0.3, body={"bend": 5, "yaw": -8}, head=look),
        back_beat(16, "insine", 0.9, body={"bend": 4, "yaw": -9}, head=look),
        back_beat(18, "inoutsine", 1.6, body={"bend": 2, "yaw": -11}, head={"pitch": -8, "yaw": -4}),
    ]
    # 2.3.2: the first leg off the back (67 degrees) eased over three beats: the blade turns out from the back a little at a time
    # (taken in one, Yoru's pommel swung through the head and its point through the floor)
    arc = [(20, 0.25 / n), (22, 0.6 / n), (24, 1.0 / n), (26, 2.0 / n), (29, 3.0 / n), (31, 4.0 / n), (33, 5.0 / n), (36, 1.0)]
    bodies = [{"bend": 1, "yaw": -12}, {"bend": 0, "yaw": -12}, {"bend": -3, "yaw": -12}, {"bend": -5, "yaw": -10},
              {"bend": -4, "yaw": -6}, {"bend": 2, "yaw": -4}, {"bend": 5, "yaw": -6, "y": -0.03}]
    heads = [{"pitch": -8, "yaw": -3}, {"pitch": -8, "yaw": -2}, {"pitch": -8, "yaw": 2}, {"pitch": -10, "yaw": 4}, {"pitch": -8},
             {"pitch": -4}, {"pitch": -2}]

    def leg_of(u):
        return min(len(bodies) - 1, int(round(u * n)))

    # 2.3.2: the edge as the sword hangs on the back (down and out to the right), eased over the arc's first two legs into the edge
    # of the swing (cut(): the way the path runs): the hand takes the sword as it hangs and turns it over as it comes off the back.
    # Taken from the swing alone, the edge stood 81 degrees off the carry's at the first beat: the draw and the sheathing each
    # settled the turn differently, and the sheathing's glide onto the back spun the sword through the head.
    carry_edge = np.array(on_back[2], float)

    def arc_pose(u):
        f, bd, ed = cut(dirs, fists, u)
        bd = np.array(bd, float)
        ce = carry_edge - bd * float(carry_edge @ bd)
        w = min(1.0, u * n / 2.0)
        w = w * w * (3.0 - 2.0 * w)
        return f, tuple(bd), tuple(unit(slerp(unit(ce), ed, w)))

    for i, (tick, u) in enumerate(arc):
        ease = "outback" if i == len(arc) - 1 else ("insine" if i == 0 else "linear")
        k = leg_of(u)
        beat = b(tick, ease, arc_pose(u), L_REST if u < 0.7 else L_BACK, bodies[k], DRAW_LEGS if u >= 0.7 else LEGS_EASY,
                 heads[k])
        if u <= 1.0 / n + 1e-9:
            beat.upper = OVERHEAD   # still reaching up behind the head, the elbow up
        beats.append(beat)
    beats.append(stance_beat(44, "inoutsine"))
    write(Move("sword_draw", 44, beats, "Swordsman (Yoru): the right hand up behind the head to the hilt over the shoulder, the great "
               "sword pulled up out of its ropes, swung out behind, up over the head and down in front into its guard.", draw=17,
               back=True))

    # its sheathing, the draw's arc backwards and slower at its end, then let down into the ropes
    beats = [stance_beat(0, "linear")]
    rarc = [(5, 5.0 / n), (8, 4.0 / n), (11, 3.0 / n), (14, 2.0 / n), (16, 1.0 / n), (18, 0.6 / n), (20, 0.25 / n)]
    for i, (tick, u) in enumerate(rarc):
        k = leg_of(u)
        # 2.3.2: the hand holds the sword as the draw left it (the edge the draw's at every point of the arc: lead 1, the edge along
        # the draw's way round); with lead -1 the hand held it edge-reversed, and the glide onto the back spun it half a turn
        # about itself through the head
        beat = b(tick, "inoutsine" if i == 0 else "linear", arc_pose(u), L_REST, bodies[k], LEGS_EASY, heads[k])
        if u <= 1.0 / n + 1e-9:
            beat.upper = OVERHEAD
        beats.append(beat)
    beats += [
        back_beat(22, "inoutsine", 1.6, body={"bend": 2, "yaw": -11}, head={"pitch": -8, "yaw": -4}),
        back_beat(24, "inoutsine", 1.0, body={"bend": 3, "yaw": -10}, head=look),
        back_beat(26, "inoutsine", 0.4, body={"bend": 4, "yaw": -9}, head=look),
        back_beat(28, "outsine", 0.0, body={"bend": 5, "yaw": -8}, head=look),
        back_beat(31, "inoutsine", 0.0, reach, body={"bend": 3, "yaw": -4}, head={"pitch": -4, "yaw": -4}),
        Beat(36, "inoutsine", rarm=dict(RELAXED_R), wrist={}, left=dict(L_REST), body={"bend": 2}, legs=LEGS_EASY),
    ]
    sheathe = write(Move("sword_sheathe", 36, beats, "Swordsman (Yoru): swung up in front, over the head and back over the shoulder, "
                         "lowered into its ropes on the back.", sheath=28, stop_extra=6, back=True))
    # the stretch nearest the back held exactly as the draw holds it there, backwards (its ticks 20 to 24, the sheathing's 20 to 16)
    sheathe.mirror = ("sword_draw_yoru", 40, (16, 20))

    charge_pose = held(H(7.6, -5.8, 0.6), D(0.3, -0.28, -0.91), D(0.1, 0.95, -0.28))
    charge_body = {"yaw": -26, "bend": -16, "y": -0.04}
    charge_legs = {"rightLeg": leg(8, 6, 26), "leftLeg": leg(-24, -5, 34)}
    shake = held(H(7.7, -6.0, 0.4), D(0.32, -0.26, -0.91), D(0.1, 0.95, -0.28))
    write(Move("sword_charge_draw", 26, [
        Beat(0, "linear", rarm=dict(RELAXED_R), wrist={}, left=dict(L_REST), body={"bend": 2}, legs=LEGS_EASY),
        back_beat(2, "outsine", 0.0, body={"bend": 4, "yaw": -10}, head={"pitch": 6, "yaw": 12}),
        back_beat(3, "linear", 1.6, body={"bend": 2, "yaw": -14}, head={"pitch": 2, "yaw": 10}),
        # half way off the back (the turn into the charge eased over two beats: in one, the pommel crossed the head); 2.3.2: lifted
        # as far up its line as the draw lifts it before it turns (at 1.0 the cross guard turned through the back of the head)
        b(5, "insine", held(*halfway(rig.back_grip({}, 1.6), charge_pose, 0.45)), L_AIM, dict(charge_body, bend=-8), charge_legs,
          {"pitch": -4, "yaw": 10}),
        b(6, "outsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
        b(16, "inoutsine", shake, L_AIM, dict(charge_body, bend=-18, y=-0.06), charge_legs, {"pitch": -10, "yaw": 10}),
        b(26, "inoutsine", charge_pose, L_AIM, charge_body, charge_legs, {"pitch": -10, "yaw": 10}),
    ], "Swordsman (Yoru): ripped off the back over the shoulder straight into the charge.", draw=3, loop=6, back=True))


def yoru_stance():
    """2.2.7: the combat stance with Yoru on the back: no hilt at the hip to rest on: both arms loose, a slow breath."""
    beats = [
        Beat(0, "linear", rarm=dict(RELAXED_R), wrist={}, left=dict(RELAXED_L), legs={}),
        Beat(20, "inoutsine", rarm={"pitch": 0.5, "yaw": 0.0, "roll": 5.0, "bend": -8.0}, wrist={},
             left={"pitch": 0.5, "yaw": 0.0, "roll": -5.0, "bend": -8.0}, legs={}),
        Beat(40, "inoutsine", rarm=dict(RELAXED_R), wrist={}, left=dict(RELAXED_L), legs={}),
    ]
    stance_write("style_swordsman_back_stance", beats, "Swordsman combat stance with Yoru on the back: both arms loose.")


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
    cases, draws, sheaths, notos, lifts, glides = [], [], [], [], [], []
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
        backs, _ = back_phase(move)
        if backs:
            lifts.append(f'            case "{name}" -> new float[] {{' + ", ".join(f"{t}F, {l:.2f}F" for t, l in backs) + "};")
            g = back_glide(move)
            if g is not None:
                vals = [g[0], g[1]]
                for shift, ang in g[2]:
                    vals += list(shift) + list(ang)
                glides.append(f'            case "{name}" -> new float[] {{' + ", ".join(f"{v:.3f}F" for v in vals) + "};")
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
           "    /** 2.3.2: Yoru lifted out of (or let down into) its ropes (tick, model px up its line on the back): drawn there. */\n"
           "    public static float[] backLift(String animation) {\n        return switch (animation) {\n" + "\n".join(lifts)
           + "\n            default -> null;\n        };\n    }\n\n"
           "    /** 2.3.2: Yoru's glide between its line on the back and the hand: {tick at the ropes, tick in the hand, then every quarter\n"
           "     * tick the sword's frame from the hand's sword frame: shift x, y, z (model px), turn z, y, x (degrees)}. */\n"
           "    public static float[] backGlide(String animation) {\n        return switch (animation) {\n" + "\n".join(glides)
           + "\n            default -> null;\n        };\n    }\n\n"
           + "\n".join(lines) + "}\n")
    with open(JAVA, "w", encoding="utf-8") as handle:
        handle.write(src)
    print(f"{JAVA}: {len(names)} blade paths")


def java_slides():
    """src/main/java/com/hexhaki/piece/sword/SayaSlide.java (2.3.2): every hip blade's slide out of its own saya (tools/saya_slide.py),
    for client/sword/SwordLayer."""
    import re
    import saya_slide
    src = open(os.path.join(ROOT, "src/main/java/com/hexhaki/piece/sword/Katana.java"), encoding="utf-8").read()
    keys = [k for k in re.findall(r'^\s+[A-Z_]+\("([a-z_]+)"', src, re.M) if k != "yoru"]
    cases, bodies = [], []
    for i, key in enumerate(keys):
        rows = saya_slide.table(key)
        cases.append(f'            case "{key}" -> t{i}();')
        body = ", ".join(f"{v:.3f}F" for row in rows for v in row)
        bodies.append(f"    /** {key}: {len(rows)} px, worst miss {saya_slide.residual(key):.2f} px */\n    private static float[] t{i}() {{\n"
                      f"        return new float[] {{{body}}};\n    }}\n")
    out = ("package com.hexhaki.piece.sword;\n\n/**\n * Generated by tools/sword_anims.py from tools/saya_slide.py (do not edit). 2.3.2: how each blade slides out of its own saya:\n"
           " * for every model px it is drawn out, the turn (degrees about the saya's z) and the shift (model px) that keep the part of the\n"
           " * blade still inside on the saya's line, fitted from the blade's own mesh (a katana's sori gathers towards its point, Gryphon's\n"
           " * saber curves three times as hard: before 2.3.2 one circle for all, and Gryphon's blade came out through its scabbard's side).\n"
           " * Past the table the blade goes on straight along its last step.\n */\n"
           "public final class SayaSlide {\n    private SayaSlide() {\n    }\n\n"
           "    /** (degrees, x px, y px) for the blade {@code katana} drawn {@code out} model px out of its saya. */\n"
           "    public static float[] at(Katana katana, float out) {\n"
           "        float[] rows = katana == null ? null : of(katana.key());\n"
           "        if (rows == null || rows.length < 6) {\n            return new float[] {0.0F, 0.0F, -out};\n        }\n"
           "        int n = rows.length / 3;\n        float x = Math.max(0.0F, out);\n        int i = (int) Math.floor(x);\n"
           "        if (i + 1 >= n) {\n            i = n - 2;\n        }\n        float u = x - i;\n        float[] r = new float[3];\n"
           "        for (int k = 0; k < 3; k++) {\n            r[k] = rows[i * 3 + k] + (rows[(i + 1) * 3 + k] - rows[i * 3 + k]) * u;\n        }\n"
           "        return r;\n    }\n\n"
           "    static float[] of(String key) {\n        return switch (key) {\n" + "\n".join(cases)
           + "\n            default -> null;\n        };\n    }\n\n" + "\n".join(bodies) + "}\n")
    with open(os.path.join(ROOT, "src/main/java/com/hexhaki/piece/sword/SayaSlide.java"), "w", encoding="utf-8") as handle:
        handle.write(out)


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
    # NPCs carry katanas only (never a legendary blade): the katanas' animations
    names = sorted(n for n in MOVES if not n.endswith(GRYPHON.suffix) and not n.endswith(YORU.suffix)
                   and n != "style_swordsman_back_stance")
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


def build():
    """The moves: the katanas' one set, which every blade plays (2.3.2, the owner: "Just apply the same fix to every single one.
    Just copy. They're all the same."), and Yoru's own draw, sheathing and draw into the charge (off its back and onto it: a draw
    from the hip cannot do that)."""
    global PROFILE
    PROFILE = KATANA
    moves(True)
    hip_draws()
    carry()
    stance()
    yoru_stance()
    PROFILE = YORU
    yoru()
    PROFILE = KATANA


def main():
    global ONLY
    if "--only" in sys.argv:
        ONLY = set(sys.argv[sys.argv.index("--only") + 1].split(","))
    global SOLVE
    if "--no-solve" in sys.argv:
        SOLVE = False
    build()
    if SOLVE:
        run_pending()
    if ONLY is None and SOLVE:
        java_paths()
        java_slides()
        java_npc_poses()
    if "--preview" in sys.argv:
        sheets(sys.argv[sys.argv.index("--preview") + 1])


def preview(names, out, frames=None, views=("front", "three", "side"), tile=(150, 176), scale=50.0, every=None):
    """A contact sheet: each move a band of rows (one per view), each column a moment; the blade drawn in the hand or the saya as
    the game shows it at that moment, with its own mesh (2.3.2: Gryphon's saber in its blue scabbard, Yoru on the back in its ropes
    until the hand takes it). every: a column every that many ticks (else frames columns, 9 by default)."""
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
        prof = move.profile if move else KATANA
        length = a.end
        if every:
            ticks = [round(k * every, 2) for k in range(int(length / every) + 1)]
            if ticks[-1] < length:
                ticks.append(length)
        else:
            n = frames or 9
            ticks = [round(length * i / (n - 1) * 2) / 2 for i in range(n)]
        band = Image.new("RGB", (tw * len(ticks), th * len(views) + 16), (236, 234, 228))
        d = ImageDraw.Draw(band)
        d.text((4, 2), f"{name}  ({length} ticks)" + (f"  cuts {move.cuts}" if move and move.cuts else "")
               + (f"  draw t{move.draw}" if move and move.draw >= 0 else "") + (f"  sheath t{move.sheath}" if move and move.sheath >= 0 else ""),
               fill=(20, 20, 20))
        for c, t in enumerate(ticks):
            q = a.pose(t)
            for r, view in enumerate(views):
                cam = sv.Camera(yaw_of[view], 10.0, scale, tw / 2, th * 0.9)
                cv = sv.Canvas(tw, th)
                sv.draw_figure(cv, cam, q)
                drawn = (move.drawn_at(t) or sliding(move, t)) if move else True
                if prof is YORU:
                    sv.draw_ropes(cv, cam, q)
                    sv.draw_katana(cv, cam, "yoru", game_blade(move, q, t) if move else rig.back_matrix(q), shine=0.3)
                elif drawn:
                    sv.draw_katana(cv, cam, prof.key, game_blade(move, q, t))
                    sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=False)
                else:
                    sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=True, shine=0.3)
                img = cv.image()
                dd = ImageDraw.Draw(img)
                dd.line([(0, th * 0.9), (tw, th * 0.9)], fill=(150, 120, 90))
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


def sheets(out, every=2.0):
    """Every move's full sheet, a column every {every} ticks, one file a move (the owner: "THE FULL SHEET, thats not all")."""
    os.makedirs(out, exist_ok=True)
    for name in sorted(n for n in MOVES if ONLY is None or n in ONLY):
        if os.path.exists(os.path.join(OUT, name + ".json")):
            preview([name], os.path.join(out, name + ".png"), every=every if MOVES[name].duration <= 48 else 8.0)


if __name__ == "__main__":
    main()
