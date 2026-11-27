#!/usr/bin/env python3
"""3.8.8: every gun's animations (the owner's category Gun: "Pistols, usually held with one hand (right) in a badass pose, all weapons
only shoot 1 bullet at a time, and auto reloads after ... simple reload animation, put 1 bullet in the hole of the barrel", "Make sure guns
are lowered in a really relaxed way, pointing down at the floor sometimes playing with them by spinning them on the hand, running
animation, etc (these animations will serve for every gun from now on)"). Written as playerAnimator clips under player_animation/,
played by client/gun/GunClient as gun/GunAnims names them:

  gun_idle     standing still: the gun hangs in the right hand at the thigh, muzzle down at the floor and a little ahead, the weight
               easy, the breath slow (a loop)
  gun_walk     walking: the right arm's swing is the game's own (its pitch is not keyed), the elbow eased and the gun along it, down
  gun_run      sprinting: the same, the elbow bent harder (the arm pumping), the gun still pointing at the ground
  gun_spin     standing still now and then: the forearm comes up and the gun spins twice round the trigger finger, settling down again
  gun_draw     taken in hand: one spin round the finger into the relaxed hold
  gun_fire_N   M1 (N the look's pitch, gun/GunAnims.bucket: -60 to 60 degrees, 20 apart): in AIM ticks the arm snaps out straight,
               the barrel on the line of the look, the body turned side on, the left hand on the hip; the shot; the recoil kicks the
               muzzle up and back; it settles, held to the reload
  gun_reload   the gun brought up before the chest, the muzzle tipped up; the left hand takes a ball from the belt, carries it up and
               presses it into the muzzle with the thumb; the gun goes back down into the hold

None keys a leg or the head (the head follows the look, the legs walk as the game has them). Every clip is solved on the game's own
rig (tools/sword_rig.py: the arm, its forearm bend, the fist), the gun held as client/gun/GunLayer holds it: its trigger on the finger
(FINGER on the fist), the right hand's item channel turning it about that finger (so a spin is a spin round the finger), the grip in
the palm (GRIP: the barrel along the fist, its top to the back of the hand). The aims are solved so the barrel lies along the look; the
reload's left hand is solved onto the muzzle (sword_rig.solve_left_at, the natural-arm limits). Previews: --preview <dir> renders each
clip as a contact sheet with the owner's gun in the hand (tools/sword_view.py).

Usage: python3 tools/gun_anims.py [--preview <dir>]
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

# --- the gun in the hand (client/gun/GunLayer: the same chain) ---------------------------------------------------------------------
# blocks per model px (gun/Gun.SCALE * 16): the owner's flintlock, 16.7 px long, about 0.7 of a block
SCALE = 0.7
# gun model px: the trigger (the finger's place: the spin's pivot), the muzzle's centre, its mouth's radius
TRIGGER = np.array([0.0, -0.3, -2.8])
MUZZLE = np.array([0.0, 2.2, -15.1])
BARREL = np.array([0.0, 0.0, -1.0])
UP = np.array([0.0, 1.0, 0.0])
# the gun's model axes into the fist's frame: its barrel (-z) along the fist (+y: from the wrist out past the knuckles), its top (+y) to
# the back of the hand (-z), its right (+x) to the fist's -x: Ry(180) Rx(90)
GRIP = rig.ry(math.pi) @ rig.rx(math.pi / 2)
# the finger on the trigger (fist px): where GRIP puts the trigger with the grip's pivot (the gun's origin) at the fist's centre
FINGER = (GRIP[:3, :3] @ (SCALE * TRIGGER))
# the left hand's fingertips (fist px from its centre): where it holds the ball
FINGERTIP = np.array([0.0, 2.2, -0.6])
BALL_SCALE = 0.7

# --- gun/GunAnims and gun/GunRules (the same numbers) ------------------------------------------------------------------------------
AIM = 3
RELOAD_FROM = 6
RELOAD = 26
FIRE_TICKS = AIM + RELOAD_FROM
BUCKETS = 7
STEP = 20
HIGHEST = -60
DRAW_TICKS = 16
SPIN_TICKS = 30
BALL_FROM = 4
BALL_AT = 12
BALL_HOME = 15

PARTS = {
    "body": ("bend",),
    "torso": ("pitch", "yaw", "roll"),
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "rightItem": ("x", "y", "z", "pitch", "yaw", "roll"),
}


def item_shift(pose):
    """The right hand's item channel's move (fist px): the finger's place shifted (a spin beside the hand, not through the forearm)."""
    it = pose.get("rightItem", {})
    return np.array([it.get("x", 0.0), it.get("y", 0.0), it.get("z", 0.0)])


def gun_matrix(pose, arm="wide"):
    """Gun model px -> E (blocks): the gun in the right hand (GunLayer's chain)."""
    return rig.layer_to_entity(pose) @ rig.fist_frame(pose, "right", arm) @ rig.tr(*(FINGER + item_shift(pose))) @ rig.wrist(pose) @ GRIP \
        @ rig.sc(SCALE) @ rig.tr(*(-TRIGGER))


def ball_matrix(pose, arm="wide"):
    """Ball model px -> E: the lead ball at the left hand's fingertips."""
    return rig.layer_to_entity(pose) @ rig.fist_frame(pose, "left", arm) @ rig.tr(*FINGERTIP) @ rig.sc(BALL_SCALE)


def muzzle(pose):
    """The muzzle's centre (E) and the barrel's direction (E, unit)."""
    m = gun_matrix(pose)
    d = rig.direction(m, BARREL)
    return rig.point(m, MUZZLE), d / np.linalg.norm(d)


def gun_up(pose):
    m = gun_matrix(pose)
    u = rig.direction(m, UP)
    return u / np.linalg.norm(u)


def look(pitch):
    """The look's direction (E) at a game pitch (degrees, negative up), straight ahead (-z)."""
    p = math.radians(pitch)
    return np.array([0.0, -math.sin(p), -math.cos(p)])


# --- poses ---------------------------------------------------------------------------------------------------------------------------

def channels(q):
    return {part: {ch: float(q.get(part, {}).get(ch, 0.0)) for ch in chs} for part, chs in PARTS.items()}


def mix(a, b, t):
    a, b = channels(a), channels(b)
    return {part: {ch: a[part][ch] + (b[part][ch] - a[part][ch]) * t for ch in chs} for part, chs in PARTS.items()}


def nudge(q, deltas):
    out = channels(q)
    for (part, ch), v in deltas.items():
        out[part][ch] += v
    return out


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def ease_out(t):
    t = max(0.0, min(1.0, t))
    return 1 - (1 - t) ** 3


def ease_in_out(t):
    t = max(0.0, min(1.0, t))
    return 4 * t ** 3 if t < 0.5 else 1 - (-2 * t + 2) ** 3 / 2


# the relaxed hold, standing (right arm roll + out, pitch - forward, bend - flexes; the item's pitch - tips the muzzle ahead)
IDLE = {
    "body": {"bend": 1.5},
    "torso": {"yaw": -3.0, "roll": 1.0},
    "rightArm": {"pitch": -7.0, "yaw": 4.0, "roll": 9.0, "bend": -12.0},
    "leftArm": {"pitch": 4.0, "yaw": 0.0, "roll": -5.0, "bend": -9.0},
    # 3.9.3 (the owner: "The pistol isnt being held right, it's resting in the hand instead of being held properly"; "Gun handle goes
    # where the green circle is": the middle of the fist): the grip in the fist (its pivot at the fist's centre, held()), the wrist
    # cocked so the barrel comes out of the front of the hand pointing ahead and down at the floor (was: the barrel straight down out
    # of the bottom of the fist like a pipe)
    "rightItem": {"pitch": -25.0},
}
# 3.8.8: a spin turns the gun beside the hand (fist px: out to the right of the forearm), never through it; 3.9.6 (the owner: "The spin
# of the ambient animation (spin) of the GUN is offcenter"): just beside the fingers, the gun's own half thickness off the fist's middle
SPIN_SIDE = -1.2
# the left hand on the hip (the aim's badass stance), solved below
HIP = None


def solve_right(base, target_dir, up_hint=None, keep=None, seed=None, muzzle_at=None, pos_weight=0.0):
    """The right arm (pitch, yaw, roll, bend) and the item's pitch for which the barrel lies along target_dir (E), the gun's top as near
    up_hint as it may, the arm natural (sword_rig's strain), and (pos_weight) the muzzle at muzzle_at (E)."""
    base = channels(base)
    seed = seed or [-80.0, 0.0, 0.0, -5.0, 0.0]
    keep = keep or {}

    def pose_of(v):
        q = channels(base)
        q["rightArm"] = {"pitch": v[0], "yaw": v[1], "roll": v[2], "bend": v[3]}
        q["rightItem"] = {"pitch": v[4], "yaw": 0.0, "roll": 0.0}
        return q

    def cost(v):
        q = pose_of(v)
        at, d = muzzle(q)
        c = 900.0 * float(np.sum((d - target_dir) ** 2))
        if up_hint is not None:
            c += 60.0 * float(np.sum((gun_up(q) - up_hint) ** 2))
        if pos_weight:
            c += pos_weight * float(np.sum((at - muzzle_at) ** 2))
        for i, (value, weight) in keep.items():
            c += weight * (v[i] - value) ** 2
        c += rig.strain_cost(q, "right")
        return c

    best_v, best = None, None
    seeds = [seed] + [list(np.array(seed) + np.array(dv)) for dv in ((20, 0, 0, 0, 0), (-20, 0, 0, 0, 0), (0, 20, 0, -20, 0),
                                                                      (0, -20, 20, 20, 0), (0, 0, -25, 0, 20))]
    for s0 in seeds:
        v, c = rig._solve(cost, s0, 16.0, 5000)
        if best is None or c < best:
            best_v, best = v, c
    return pose_of(best_v), best


def fire_pose(bucket):
    """The aim of a fire bucket: the arm straight out along the look, the gun upright (a touch canted in), side on."""
    pitch = HIGHEST + bucket * STEP
    base = channels(IDLE)
    base["torso"] = {"pitch": 0.0, "yaw": 8.0, "roll": 0.0}
    base["body"] = {"bend": -1.0 + 0.04 * pitch}
    base["leftArm"] = dict(HIP)
    d = look(pitch)
    up = np.array([0.18, 1.0, 0.0])
    up = up - d * (up @ d)
    up /= np.linalg.norm(up)
    q, _ = solve_right(base, d, up, keep={3: (-6.0, 2.0), 4: (6.0, 0.4)}, seed=[-90.0 + pitch, 0.0, 0.0, -6.0, 6.0])
    return q


def recoil(q, k, pitch=0.0):
    """The recoil k of the way (1 at its height): the muzzle kicked up and back, the elbow giving, the body rocked; aimed steeply up
    the arm kicks less (it would go past the vertical)."""
    arm = 16.0 * (1.0 - 0.6 * max(0.0, -pitch) / 60.0)
    return nudge(q, {("rightArm", "pitch"): -arm * k, ("rightArm", "bend"): -14.0 * k, ("rightItem", "pitch"): 22.0 * k,
                     ("body", "bend"): -2.5 * k, ("torso", "yaw"): 3.0 * k})


def reload_hold():
    """The reload's hold: the gun before the chest, the muzzle tipped up (a little ahead and in), the forearm up."""
    base = channels(IDLE)
    base["torso"] = {"pitch": 0.0, "yaw": -4.0, "roll": 0.0}
    base["body"] = {"bend": 4.0}
    d = np.array([-0.45, 0.62, -0.62])
    d /= np.linalg.norm(d)
    at = np.array([-0.05, 1.45, -0.42])
    q, _ = solve_right(base, d, np.array([0.0, 0.0, 1.0]), seed=[-45.0, 10.0, 10.0, -95.0, 40.0], muzzle_at=at, pos_weight=20000.0)
    return q


def fingertip(q):
    return rig.point(ball_matrix(q), (0.0, 0.0, 0.0))


def left_to(q, target_e, seed=None):
    """The left arm with its fingertips (FINGERTIP) at target_e (E): the fist's centre solved, the fingertips' offset from it taken from
    the answer's own turn, three times over."""
    target = rig.layer_from_entity(q, target_e)
    q2 = channels(q)
    off = np.array([0.0, 2.6, 0.0])
    err = 1e9
    for _ in range(4):
        arm, _ = rig.solve_left_at(q, target - off, seed=seed)
        q2["leftArm"] = arm
        seed = [arm["pitch"], arm["yaw"], arm["roll"], arm["bend"]]
        frame = rig.fist_frame(q2, "left")
        off = frame[:3, :3] @ FINGERTIP
        err = float(np.linalg.norm(fingertip(q2) - target_e))
    return q2, err


def left_path(q, points, seed):
    """The left arm along points (E), each solved from the last: a path, not a swing between two far answers."""
    out = []
    for p in points:
        q2, _ = left_to(q, p, seed)
        seed = [q2["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")]
        out.append(q2)
    return out


# --- the clips -------------------------------------------------------------------------------------------------------------------------

def idle_beats():
    """80 ticks: the breath (body bend, arms easing), the weight shifting (torso), the gun swaying a little at the thigh."""
    beats = []
    for t in range(0, 81, 4):
        a = 2 * math.pi * t / 80.0
        b = math.sin(a)
        c = math.sin(2 * a)
        q = nudge(IDLE, {("body", "bend"): 1.2 * c, ("torso", "yaw"): 2.0 * b, ("torso", "roll"): 1.0 * b,
                         ("rightArm", "roll"): 1.5 * c, ("rightArm", "pitch"): -1.5 * b, ("rightArm", "bend"): -2.0 * c,
                         ("leftArm", "roll"): -1.2 * c, ("leftArm", "pitch"): 1.5 * b, ("rightItem", "pitch"): -3.0 * b})
        beats.append((t, "inoutsine", q))
    return beats


WALK = {"rightArm": {"yaw": 3.0, "roll": 8.0, "bend": -16.0}, "rightItem": {"pitch": -31.0}}
RUN = {"rightArm": {"yaw": 6.0, "roll": 9.0, "bend": -42.0}, "rightItem": {"pitch": 9.0}}


def held(q):
    """3.9.3: the relaxed hold's item move: the gun turned about the trigger finger (FINGER) by the item's pitch and roll is moved back
    so the grip's pivot (the gun's origin, in its handle) stays at the fist's centre, then HANG_FORWARD px ahead: the handle in the
    hand, hooking out of its front, the barrel hanging in front of the fist (the owner's drawing)."""
    out = channels(q)
    it = out["rightItem"]
    r = (rig.rz(math.radians(it["roll"])) @ rig.ry(math.radians(it["yaw"])) @ rig.rx(math.radians(it["pitch"])))[:3, :3]
    pivot = FINGER - r @ FINGER
    it["y"] = -float(pivot[1])
    it["z"] = -float(pivot[2]) - HANG_FORWARD
    return out


# 3.9.3 (the owner: "Make the gun point fully down man", with a drawing: the handle out of the fist's front, the barrel down; then
# "thats too much now up a bit"): how far ahead of the fist's centre the grip sits (px), and the wrist solved so the barrel points at the
# floor HANG_AHEAD degrees ahead of straight down
HANG_FORWARD = 1.6
HANG_AHEAD = 22.0
HANG_DIR = np.array([0.0, -math.cos(math.radians(HANG_AHEAD)), -math.sin(math.radians(HANG_AHEAD))])


def hang_down(q):
    """The pose with the item's pitch and roll solved so the barrel points along HANG_DIR (E: down, a little ahead), the grip held as
    held() holds it."""
    best = None
    for pitch in np.arange(-40.0, 60.0, 1.0):
        for roll in np.arange(-30.0, 30.5, 1.0):
            t = channels(q)
            t["rightItem"]["pitch"] = float(pitch)
            t["rightItem"]["roll"] = float(roll)
            t = held(t)
            d = muzzle(t)[1]
            err = 1.0 - float(d @ HANG_DIR)
            if best is None or err < best[0]:
                best = (err, float(pitch), float(roll))
    for _ in range(3):
        _, p0, r0 = best
        for pitch in np.arange(p0 - 1.0, p0 + 1.01, 0.1):
            for roll in np.arange(r0 - 1.0, r0 + 1.01, 0.1):
                t = channels(q)
                t["rightItem"]["pitch"] = float(pitch)
                t["rightItem"]["roll"] = float(roll)
                t = held(t)
                err = 1.0 - float(muzzle(t)[1] @ HANG_DIR)
                if err < best[0]:
                    best = (err, float(pitch), float(roll))
    out = channels(q)
    out["rightItem"]["pitch"] = round(best[1], 1)
    out["rightItem"]["roll"] = round(best[2], 1)
    return held(out)


IDLE = hang_down(IDLE)
WALK = hang_down(WALK)
RUN = hang_down(RUN)


def spin_beats(turns, length, raise_to):
    """From the hold the forearm comes up, the gun spins turns times round the finger (fast, easing out), and settles down again."""
    up = nudge(IDLE, {("rightArm", "bend"): raise_to, ("rightArm", "pitch"): -14.0, ("rightItem", "x"): SPIN_SIDE})
    beats = []
    lift = max(3, length // 6)
    settle = length - max(5, length // 4)
    for t in range(0, length + 1):
        if t <= lift:
            q = mix(IDLE, up, smooth(t / lift))
        elif t >= settle:
            q = mix(up, IDLE, smooth((t - settle) / (length - settle)))
        else:
            q = channels(up)
        # the spin: the item's pitch round 360 * turns, from the lift to the settle, quick and easing out; the item's rest pitch (IDLE's)
        # is reached exactly at the end
        u = ease_out((t - lift * 0.5) / (settle + 2 - lift * 0.5))
        rest = IDLE["rightItem"]["pitch"]
        hold = q["rightItem"]["pitch"]
        q["rightItem"]["pitch"] = hold + 360.0 * turns * u if t < length else rest + 360.0 * turns
        beats.append((t, "linear", q))
    # the spin ends where it began, a whole number of turns round; 3.9.6 (the owner: "whenever the gun does the spin and it ends, it goes
    # backwards when the animation finishes it should not undo itself"): the last tick keys the same angle as the hold's own (turns x 360
    # less), at once (constant): the clip ends on the hold's angle itself, so nothing after it winds the turns back
    beats[-2] = (length - 1, "linear", nudge(IDLE, {("rightItem", "pitch"): 360.0 * turns}))
    beats[-1] = (length, "constant", channels(IDLE))
    return beats


def fire_beats(bucket):
    aim = fire_pose(bucket)
    pitch = HIGHEST + bucket * STEP
    beats = [(0, "linear", channels(IDLE))]
    beats.append((1, "outquad", mix(IDLE, aim, 0.72)))
    beats.append((AIM, "outquad", aim))
    beats.append((AIM + 1, "outexpo", recoil(aim, 1.0, pitch)))
    beats.append((AIM + 2, "inoutsine", recoil(aim, 0.75, pitch)))
    beats.append((AIM + 4, "inoutsine", recoil(aim, 0.12, pitch)))
    beats.append((FIRE_TICKS, "inoutsine", aim))
    return beats


def reload_beats():
    hold = reload_hold()
    m_at, m_dir = muzzle(hold)
    beats = [(0, "linear", channels(IDLE))]
    # 0..4: the gun comes up; the left hand drops to the belt at the left hip (the ball's pouch); 4..12 it carries the ball up before
    # the body (out ahead of the chest, never through it) to just over the muzzle; 12..15 the thumb presses it in
    belt = np.array([-0.28, 0.80, -0.16])
    above = m_at + m_dir * 0.16
    path = [belt, np.array([-0.26, 0.92, -0.42]), np.array([-0.16, 1.08, -0.56]), np.array([-0.06, 1.24, -0.58]), above]
    solved = left_path(hold, path, [10.0, 0.0, -10.0, -40.0])
    for (t, easing), q in zip(((BALL_FROM, "inoutsine"), (6, "linear"), (8, "linear"), (10, "linear"), (BALL_AT, "outquad")), solved):
        beats.append((t, easing, q))
    press = left_to(hold, m_at + m_dir * 0.02, [solved[-1]["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")])[0]
    dip = nudge(press, {("rightArm", "pitch"): 3.0, ("rightItem", "pitch"): 3.0})
    beats.append((BALL_AT + 2, "inquad", dip))
    beats.append((BALL_HOME, "outquad", press))
    # the hand comes back down the way it went up (before the body), to the belt, then hangs as in the hold
    back = left_path(hold, [np.array([-0.17, 1.12, -0.50]), np.array([-0.27, 0.86, -0.22])],
                     [press["leftArm"][k] for k in ("pitch", "yaw", "roll", "bend")])
    beats.append((BALL_HOME + 2, "linear", back[0]))
    beats.append((BALL_HOME + 4, "inoutsine", back[1]))
    low = channels(hold)
    low["leftArm"] = back[1]["leftArm"]
    beats.append((BALL_HOME + 6, "inoutsine", nudge(low, {("rightItem", "pitch"): -4.0})))
    beats.append((RELOAD, "inoutsine", channels(IDLE)))
    return beats


def write(name, beats, end, loop, description, written=None):
    """The clip: each beat keys every channel of the parts written (PARTS, or written's), one move a beat."""
    written = written or PARTS
    moves = []
    for tick, easing, q in beats:
        q = channels(q)
        move = {"tick": tick, "easing": easing}
        for part, chs in written.items():
            move[part] = {ch: round(q[part][ch], 2) for ch in chs}
        moves.append(move)
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.9.3", "description": description,
            "emote": {"beginTick": 0, "endTick": end, "stopTick": end, "isLoop": loop, "returnTick": 0, "degrees": True,
                      "easeBeforeKeyframe": True, "moves": []}}
    text = json.dumps(data, indent=1)
    body = ",\n".join("   " + json.dumps(m, separators=(",", ":")) for m in moves)
    text = text.replace('"moves": []', '"moves": [\n' + body + "\n  ]")
    json.loads(text)
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as handle:
        handle.write(text + "\n")


def clips():
    """{name: (beats, end, loop, description, written)}"""
    global HIP
    base = channels(IDLE)
    hip, _ = left_to(base, np.array([-0.33, 0.83, -0.02]), seed=[8.0, 10.0, -40.0, -70.0])
    HIP = hip["leftArm"]
    out = {}
    out["gun_idle"] = (idle_beats(), 80, True, "Gun: standing still, the gun hanging in the right hand at the thigh, the muzzle down at the "
                       "floor a little ahead, the weight easy, the breath slow.", None)
    walk = {"rightArm": ("yaw", "roll", "bend"), "rightItem": ("x", "y", "z", "pitch", "yaw", "roll")}
    out["gun_walk"] = ([(0, "linear", WALK), (20, "linear", WALK)], 20, True, "Gun: walking: the right arm swings as the game swings "
                       "it, the elbow eased, the gun along it, muzzle down.", walk)
    out["gun_run"] = ([(0, "linear", RUN), (20, "linear", RUN)], 20, True, "Gun: sprinting: the arm pumps as the game swings it, the "
                      "elbow bent hard, the gun pointing at the ground.", walk)
    out["gun_spin"] = (spin_beats(2, SPIN_TICKS, -38.0), SPIN_TICKS, False, "Gun: the forearm comes up and the gun spins twice round "
                       "the trigger finger, then settles back down at the thigh.", None)
    out["gun_draw"] = (spin_beats(1, DRAW_TICKS, -30.0), DRAW_TICKS, False, "Gun: taken in hand, spun once round the finger into the "
                       "relaxed hold.", None)
    for b in range(BUCKETS):
        # 3.9.6 (the owner: "the left arm is not meant to have animation when firing (its putting the left arm on the hip"): the fire
        # clips key no left arm: it moves as the game moves it
        fire_parts = {k: v for k, v in PARTS.items() if k != "leftArm"}
        out[f"gun_fire_{b}"] = (fire_beats(b), FIRE_TICKS, False, f"Gun: aimed and fired at a look {HIGHEST + b * STEP} degrees "
                                f"({'up' if HIGHEST + b * STEP < 0 else 'down' if HIGHEST + b * STEP > 0 else 'level'}): the arm "
                                "snapped out straight along the look, side on; the shot; the recoil kicks the muzzle up; it settles "
                                "(the left arm left as the game moves it).", fire_parts)
    out["gun_reload"] = (reload_beats(), RELOAD, False, "Gun: the reload: the gun brought up before the chest, the muzzle tipped up; the "
                         "left hand takes a ball from the belt and presses it into the muzzle; the gun back down into the hold.", None)
    return out


def pose_at(beats, t):
    """The pose at tick t (linear between beats: for the checks and previews)."""
    beats = sorted(beats, key=lambda b: b[0])
    if t <= beats[0][0]:
        return channels(beats[0][2])
    for (t0, _, a), (t1, _, b) in zip(beats, beats[1:]):
        if t0 <= t <= t1:
            return mix(a, b, (t - t0) / max(1e-9, t1 - t0))
    return channels(beats[-1][2])


def check(all_clips):
    """Fails on an aim whose barrel is off the look, a reload whose ball misses the muzzle, an arm past what a body does."""
    problems = []
    for name, (beats, end, *_rest) in all_clips.items():
        if name.startswith("gun_fire_"):
            b = int(name.rsplit("_", 1)[1])
            q = pose_at(beats, AIM)
            _, d = muzzle(q)
            off = math.degrees(math.acos(max(-1.0, min(1.0, float(d @ look(HIGHEST + b * STEP))))))
            if off > 4.0:
                problems.append(f"{name}: the barrel is {off:.1f} degrees off the look at the shot")
        for t in range(0, end + 1):
            q = pose_at(beats, t)
            for side in ("right", "left"):
                for what, by in rig.arm_strain(q, side):
                    # a spin turns the gun on the finger, not the wrist: the hand's own limits are not asked of it there
                    spun = side == "right" and what in ("wrist tipped", "hand turned", "wrist bent") and name in ("gun_spin", "gun_draw")
                    if by > 12.0 and not spun:
                        problems.append(f"{name} t{t}: {side} {what} by {by:.1f}")
    beats = all_clips["gun_reload"][0]
    q = pose_at(beats, BALL_AT)
    at, d = muzzle(q)
    gap = float(np.linalg.norm(fingertip(q) - (at + d * 0.16)))
    if gap > 0.06:
        problems.append(f"gun_reload: the ball is {gap * 16:.1f} px from the muzzle's mouth when it gets there")
    return problems


def preview(all_clips, folder):
    import sword_view as sv
    from PIL import Image, ImageDraw
    os.makedirs(folder, exist_ok=True)
    for name, (beats, end, *_rest) in all_clips.items():
        ticks = sorted(set([0] + [b[0] for b in beats] + [end]))
        if len(ticks) > 9:
            ticks = [round(end * i / 8) for i in range(9)]
        w, h = 170, 230
        views = ("three", "side")
        img = Image.new("RGB", (w * len(ticks), h * len(views) + 16), (236, 234, 228))
        d = ImageDraw.Draw(img)
        d.text((4, 2), name, fill=(20, 20, 20))
        for c, t in enumerate(ticks):
            q = pose_at(beats, t)
            for r, view in enumerate(views):
                yaw = {"three": 35, "side": 90, "front": 0}[view]
                cam = sv.Camera(yaw, 10.0, 105.0, w / 2, h * 0.93)
                cv = sv.Canvas(w, h)
                sv.draw_figure(cv, cam, q)
                sv.draw_katana(cv, cam, "flintlock", gun_matrix(q))
                if name == "gun_reload" and BALL_FROM <= t < BALL_HOME:
                    if t < BALL_AT:
                        m = ball_matrix(q)
                    else:
                        at, dd = muzzle(q)
                        m = rig.tr(*(at + dd * 0.03 * (1 - (t - BALL_AT) / (BALL_HOME - BALL_AT)))) @ rig.sc(BALL_SCALE * rig.PX)
                    sv.draw_katana(cv, cam, "lead_ball", m)
                tile = cv.image()
                ImageDraw.Draw(tile).text((3, 3), f"t{t}", fill=(80, 80, 80))
                img.paste(tile, (c * w, 16 + r * h))
        img.save(os.path.join(folder, name + ".png"))


def main():
    all_clips = clips()
    problems = check(all_clips)
    for p in problems:
        print("PROBLEM", p)
    for name, (beats, end, loop, description, written) in all_clips.items():
        write(name, beats, end, loop, description, written)
    if "--preview" in sys.argv:
        preview(all_clips, sys.argv[sys.argv.index("--preview") + 1])
    if problems:
        sys.exit(1)


if __name__ == "__main__":
    main()
