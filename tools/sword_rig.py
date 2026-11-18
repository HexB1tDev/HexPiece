#!/usr/bin/env python3
"""2.1.4: the Swordsman's rig, off-game: where the katana is for any pose of a playerAnimator animation, exactly as the game draws it.

The chain is the game's (client/sword/SwordLayer.java does the same steps in the same order):
  entity space E (blocks, y up, the feet at 0, the player facing -z, its right +x)
    <- playerAnimator's body transform (PlayerRendererMixin.applyBodyTransforms: translate(body x, y + 0.7, z), rotZ(roll),
       rotY(yaw), rotX(pitch), translate(0, -0.7, 0); the JSON's degrees)
    <- LivingEntityRenderer: scale(-1, -1, 1), PlayerRenderer.scale 0.9375, translate(0, -1.501, 0)      -> model space (y down)
    <- the body bend every render layer gets (LivingEntityRenderRedirect_bendOnly + bendy-lib IBendHelper.rotateMatrixStack:
       translate(0, 0.375, 0), rotate by bend about (cos(-axis), 0, sin(-axis)), translate back)          -> the layer's frame L
  blade:  L <- rightArm.translateAndRotate (pivot (-5, 2, 0) px + the animation's x y z, rotationZYX(roll, yaw, pitch))
            <- the forearm's bend (HeldItemMixin: translate(0, 4 px), rotate by bend about (cos(-a), 0, sin(-a)), translate back)
            <- translate to the fist's centre (FIST)  <- the wrist: the animation's rightItem rotation, rotZ, rotY, rotX
            <- the grip (GRIP_ROT: the blade, model +y, along the fist's -z; its edge, model -x, along +y), the handle's grip point
               (KatanaRenderer.GRIP) at the fist's centre, the mesh at KatanaRenderer.SCALE
  saya:   model space (the body bend undone: the hip does not hunch) <- body.translateAndRotate <- HIP (the koiguchi at the left hip)

Everything a cut needs (the blade's base and tip per tick, for the server's swept hit volumes) and every preview comes from here.
"""
import math

import numpy as np

PX = 1.0 / 16.0
# KatanaRenderer: model pixels -> blocks, the grip on the handle (model pixels; the guard at 0)
SCALE = 1.0 / 16.0
GRIP = -2.4
# the blade's base (habaki) and tip (model pixels): what a cut sweeps
BLADE_BASE = 1.0
BLADE_TIP = 21.0
# the right fist's centre in the arm's own frame (px): the wide arm's box is x -3..1, y -2..10, z -2..2
FIST = {"wide": (-1.0, 8.5, 0.0), "slim": (-0.5, 8.5, 0.0)}
LEFT_FIST = {"wide": (1.0, 8.5, 0.0), "slim": (0.5, 8.5, 0.0)}
ARM_PIVOT = {"rightArm": (-5.0, 2.0, 0.0), "leftArm": (5.0, 2.0, 0.0)}
LEG_PIVOT = {"rightLeg": (-1.9, 12.0, 0.0), "leftLeg": (1.9, 12.0, 0.0)}


def rx(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]], float)


def ry(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]], float)


def rz(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]], float)


def raxis(angle, axis):
    x, y, z = axis
    c, s, t = math.cos(angle), math.sin(angle), 1 - math.cos(angle)
    return np.array([[c + x * x * t, x * y * t - z * s, x * z * t + y * s, 0],
                     [y * x * t + z * s, c + y * y * t, y * z * t - x * s, 0],
                     [z * x * t - y * s, z * y * t + x * s, c + z * z * t, 0], [0, 0, 0, 1]], float)


def tr(x, y, z):
    m = np.eye(4)
    m[:3, 3] = (x, y, z)
    return m


def sc(x, y=None, z=None):
    return np.diag([x, x if y is None else y, x if z is None else z, 1.0])


def zyx(pitch, yaw, roll):
    """ModelPart.translateAndRotate's rotation: Quaternionf.rotationZYX(zRot, yRot, xRot) = Rz * Ry * Rx (radians)."""
    return rz(roll) @ ry(yaw) @ rx(pitch)


def rad(v):
    return math.radians(v)


def ch(pose, part, channel):
    return pose.get(part, {}).get(channel, 0.0)


# the grip: katana model axes into the fist's frame: model x -> -y, model y -> -z, model z -> x (Rz(-90) Rx(-90))
GRIP_ROT = rz(-math.pi / 2) @ rx(-math.pi / 2)


def body_to_entity(pose):
    """Model space (px, y down, before the body bend) -> E (blocks)."""
    b = pose.get("body", {})
    m = tr(b.get("x", 0.0), b.get("y", 0.0) + 0.7, b.get("z", 0.0)) @ rz(rad(b.get("roll", 0.0))) @ ry(rad(b.get("yaw", 0.0))) \
        @ rx(rad(b.get("pitch", 0.0))) @ tr(0, -0.7, 0)
    return m @ sc(-1, -1, 1) @ sc(0.9375) @ tr(0, -1.501, 0) @ sc(PX)


def body_bend(pose):
    """The layer's frame inside model space (px): the body bend, as bendy-lib rotates every render layer's matrix stack."""
    b = pose.get("body", {})
    axis = -rad(b.get("axis", 0.0))
    return tr(0, 6, 0) @ raxis(rad(b.get("bend", 0.0)), (math.cos(axis), 0.0, math.sin(axis))) @ tr(0, -6, 0)


def layer_to_entity(pose):
    return body_to_entity(pose) @ body_bend(pose)


def part_matrix(pose, part, pivot):
    p = pose.get(part, {})
    return tr(pivot[0] + p.get("x", 0.0), pivot[1] + p.get("y", 0.0), pivot[2] + p.get("z", 0.0)) @ zyx(
        rad(p.get("pitch", 0.0)), rad(p.get("yaw", 0.0)), rad(p.get("roll", 0.0)))


def forearm_bend(pose, part):
    """HeldItemMixin: the held item follows the forearm (rotate by the arm's bend about its middle, 4 px down)."""
    p = pose.get(part, {})
    axis = -rad(p.get("axis", 0.0))
    return tr(0, 4, 0) @ raxis(rad(p.get("bend", 0.0)), (math.cos(axis), 0.0, math.sin(axis))) @ tr(0, -4, 0)


def fist_frame(pose, side="right", arm="wide"):
    """The fist's centre frame in the layer's frame (px): arm, forearm bend, the fist's centre."""
    part = side + "Arm"
    fist = (FIST if side == "right" else LEFT_FIST)[arm]
    return part_matrix(pose, part, ARM_PIVOT[part]) @ forearm_bend(pose, part) @ tr(*fist)


def wrist(pose, item="rightItem"):
    return rz(rad(ch(pose, item, "roll"))) @ ry(rad(ch(pose, item, "yaw"))) @ rx(rad(ch(pose, item, "pitch")))


def grip(slide=0.0):
    """Katana model px -> the fist's frame (px): the grip point on the fist's centre; slide (model px, the animation's rightItem y)
    holds the handle further down, towards the pommel (a draw takes the handle low, where the hand can reach it)."""
    return GRIP_ROT @ sc(SCALE / PX) @ tr(0, -(GRIP - slide), 0)


def slide_of(pose):
    return ch(pose, "rightItem", "y")


def blade_matrix(pose, arm="wide"):
    """Katana model px -> E (blocks), the katana drawn in the right hand."""
    return layer_to_entity(pose) @ fist_frame(pose, "right", arm) @ wrist(pose) @ grip(slide_of(pose))


# --- the saya at the left hip (body-part space, px: x the player's left, y down, z back) --------------------------------------
# The koiguchi (the guard of a sheathed blade) sits at the front of the left hip, the saya sloping back and down past the hip, the
# edge up (worn edge-up, as a katana is worn through the obi), turned a little out so it clears the thigh.
HIP_MOUTH = (3.4, 10.0, -4.2)     # the owner: "the sheath is too far back": the koiguchi at the front of the left hip
HIP_AXIS = (0.46, 0.46, 1.0)       # model +y of the saya: back, down and out past the hip
HIP_EDGE = (0.0, -1.0, 0.18)       # model -x of the blade: up (a little forward)


def frame_from(axis_y, edge_minus_x):
    """A rotation whose +y is axis_y and whose -x is as near edge_minus_x as an orthonormal frame allows."""
    y = np.array(axis_y, float)
    y /= np.linalg.norm(y)
    e = np.array(edge_minus_x, float)
    e = e - y * (e @ y)
    e /= np.linalg.norm(e)
    x = -e
    z = np.cross(x, y)
    m = np.eye(4)
    m[:3, 0], m[:3, 1], m[:3, 2] = x, y, z
    return m


def hip(pose=None):
    """Katana model px -> body-part space (px): the koiguchi at the hip, moved and turned by the animation's leftItem (the left hand
    working the saya: pulled forward to bring the handle to the right hand, back as the blade leaves it; px and degrees)."""
    it = (pose or {}).get("leftItem", {})
    move = tr(it.get("x", 0.0), it.get("y", 0.0), it.get("z", 0.0)) @ zyx(rad(it.get("pitch", 0.0)), rad(it.get("yaw", 0.0)),
                                                                          rad(it.get("roll", 0.0)))
    return tr(*HIP_MOUTH) @ move @ frame_from(HIP_AXIS, HIP_EDGE) @ sc(SCALE / PX)


def saya_matrix(pose):
    """Katana model px -> E (blocks): the saya (and a sheathed blade) at the hip; the body bend left out, the torso part's own turn in."""
    return body_to_entity(pose) @ part_matrix(pose, "torso", (0.0, 0.0, 0.0)) @ hip(pose)


def point(m, p):
    v = m @ np.array([p[0], p[1], p[2], 1.0])
    return v[:3]


def direction(m, d):
    v = m[:3, :3] @ np.array(d, float)
    return v / np.linalg.norm(v)


def blade_segment(pose, arm="wide"):
    """The blade's base and tip in E (blocks) when drawn."""
    m = blade_matrix(pose, arm)
    return point(m, (0, BLADE_BASE, 0)), point(m, (0, BLADE_TIP, 0))


# --- inverse kinematics (numeric: the arms are 4 angles, the wrist 3) ---------------------------------------------------------------

def _solve(cost, x0, step0=12.0, iters=400):
    """Pattern search (deterministic, no dependencies): good enough for 4-7 angles in degrees."""
    x = np.array(x0, float)
    best = cost(x)
    step = step0
    n = len(x)
    while step > 0.02 and iters > 0:
        improved = False
        for i in range(n):
            for sgn in (1, -1):
                y = x.copy()
                y[i] += sgn * step
                c = cost(y)
                iters -= 1
                if c < best:
                    best, x, improved = c, y, True
                    break
        if not improved:
            step *= 0.5
    return x, best


def with_arm(pose, part, vals):
    q = {k: dict(v) for k, v in pose.items()}
    q[part] = {"pitch": vals[0], "yaw": vals[1], "roll": vals[2], "bend": vals[3]}
    return q


# --- 2.3.2: a natural arm (the owner: "the arm is twisted in a bunch of these, how does this pass") ------------------------------
# The IK used to weigh only where the fist and the blade go, so it twisted the arms freely: upper arms turned past what a shoulder turns,
# wrists bent to their stops, elbows folding the forearm out to the side or crumpled shut, arms through the chest. A human arm:
# the upper arm turns about itself (humeral rotation) about 70 degrees in and 45 out; the elbow folds the forearm towards the arm's
# front, up to about 125 degrees; a hand holding a sword in a hammer grip (the blade out of the thumb side) tilts it towards the
# fingers about 40 degrees (ulnar) and towards the elbow 25 (radial), turns it round the forearm about 80 each way (pronation), and
# bends it at the wrist about 65 (which spins the blade about itself); and no arm goes through the body. The solvers keep to these
# (arm_strain) and tools/sword_anims.py's clearance check fails an animation that does not.
TWIST_IN, TWIST_OUT = 70.0, 45.0
# 2.3.2: the elbow's fold, which is how a turned arm shows on a box arm (a straight box arm looks the same however it is turned): with
# the elbow bent past FOLD_FROM degrees, the forearm may not fold out away from the body further than FOLD_OUT degrees from the body's
# side plane, nor, the arm hanging, back behind it further than FOLD_BACK
FOLD_FROM = 25.0
FOLD_OUT = 50.0
FOLD_BACK = 35.0
# what the solvers pay (per degree past a limit, squared; per px of arm or sword through a body, squared): a natural arm and nothing
# through anything outweigh following the choreography's blade to the degree (the blade gives way, never the body)
STRAIN_WEIGHT = 3.0
COLLIDE_WEIGHT = 400.0
# 2.3.2: the solvers keep this far (px) inside what the check allows (its margins), so what they leave never reaches its limits
SOLVER_SPARE = 0.45
ELBOW_MAX = 125.0
# 2.3.2: the shoulder's swing front to back (the upper arm's direction seen from the side): back behind the body no more than
# SHOULDER_BACK degrees, up past straight overhead no more than SHOULDER_OVER (the solver once raised a sword overhead by swinging the
# arm backwards over the shoulder, 130 degrees behind the body, and wound the track a full turn)
SHOULDER_BACK, SHOULDER_OVER = 60.0, 190.0
# 2.4.5 (the owner: "the arms should be fully stretched and the blade pointed forward far from the player's chest"): the hand may tip
# the blade 60 degrees towards the fingers (was 40). The rig's handle crosses the fist square; a real hand holds it slanted across the
# palm (about 20 degrees), so a real wrist's 40 reads as 60 here: the blade can run on along the arm's line, as in the owner's lunge
ULNAR, RADIAL = 60.0, 25.0
PRONATION = 80.0
WRIST_BEND = 65.0


def _cross(a, b):
    """a x b for 3-vectors (numpy's cross is slow for one pair, and the solvers make thousands)."""
    return np.array((a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]))


def _rot_between(a, b):
    """The smallest rotation (3x3) taking unit vector a to unit vector b."""
    a = np.asarray(a, float)
    b = np.asarray(b, float)
    v = _cross(a, b)
    c = float(a @ b)
    if c < -0.9999:
        axis = _cross(a, (1.0, 0.0, 0.0))
        if np.linalg.norm(axis) < 1e-6:
            axis = _cross(a, (0.0, 0.0, 1.0))
        axis /= np.linalg.norm(axis)
        return 2.0 * np.outer(axis, axis) - np.eye(3)
    k = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
    return np.eye(3) + k + k @ k / (1.0 + c)


def fold_strain(rot, bend, side):
    """2.3.2: (degrees past FOLD_OUT that the elbow folds the forearm out away from the body, degrees past FOLD_BACK that it folds back
    with the arm hanging), 0 for an elbow bent less than FOLD_FROM. Measured on the fold's direction itself (the upper arm's front
    face), so it has no blind spot (a turn of the upper arm about itself has one wherever its zero is put)."""
    if abs(bend) < FOLD_FROM:
        return 0.0, 0.0
    f = rot @ np.array((0.0, 0.0, -1.0))
    u = rot @ np.array((0.0, 1.0, 0.0))
    out = np.array((-1.0, 0.0, 0.0)) if side == "right" else np.array((1.0, 0.0, 0.0))
    over_out = max(0.0, math.degrees(math.asin(max(-1.0, min(1.0, float(f @ out))))) - FOLD_OUT)
    over_back = 0.0
    if u[1] > 0.3:
        over_back = max(0.0, math.degrees(math.asin(max(-1.0, min(1.0, float(f[2]))))) - FOLD_BACK)
    return over_out, over_back


def shoulder_back(rot):
    """2.3.2: degrees the upper arm (rotation rot, 3x3) points further back than a shoulder swings it (SHOULDER_BACK behind the body,
    SHOULDER_OVER past hanging over the top), measured from the side, 0 for an arm out to the side (where the swing is the side's)."""
    u = rot @ np.array((0.0, 1.0, 0.0))
    proj = math.hypot(float(u[1]), float(u[2]))
    if proj < 0.2:
        return 0.0
    theta = math.degrees(math.atan2(-float(u[2]), float(u[1])))
    lo, hi = -SHOULDER_BACK, SHOULDER_OVER - 360.0
    if theta >= lo or theta <= hi:
        return 0.0
    return min(lo - theta, theta - hi) * min(1.0, (proj - 0.2) / 0.5)


def arm_twist(rot, side):
    """The upper arm's turn about itself (degrees; + inwards, the forearm folding across the body; - outwards), from its rotation
    (3x3, the part's): against the smallest swing that takes the hanging arm to where it points."""
    u = rot @ np.array((0.0, 1.0, 0.0))
    if u[1] < -0.8:
        # an arm raised near straight up: the swing from hanging is undefined there (a half turn about any level axis), so from the
        # arm raised straight forward instead (its front face up), which agrees with it for an arm raised in front
        v = _rot_between((0.0, 0.0, -1.0), u) @ np.array((0.0, -1.0, 0.0))
    else:
        v = _rot_between((0.0, 1.0, 0.0), u) @ np.array((0.0, 0.0, -1.0))
    w = rot @ np.array((0.0, 0.0, -1.0))
    ang = math.degrees(math.atan2(float(_cross(v, w) @ u), float(v @ w)))
    return -ang if side == "right" else ang


def wrist_angles(wr):
    """A hand's hold on the blade (degrees) from the wrist (rightItem rotation, 3x3 or 4x4): ulnar deviation (+, the blade tipped
    towards the fingers; - radial), pronation (the blade turned round the forearm) and the wrist's bend (the blade spun about itself)."""
    r = np.asarray(wr, float)[:3, :3]
    b = r @ np.array((0.0, 0.0, -1.0))
    e = r @ np.array((0.0, 1.0, 0.0))
    dev = math.degrees(math.asin(max(-1.0, min(1.0, float(b[1])))))
    pro = math.degrees(math.atan2(float(b[0]), float(-b[2])))
    flat = np.array((0.0, 1.0, 0.0)) - b * float(b[1])
    n = np.linalg.norm(flat)
    if n < 1e-6:
        return dev, pro, 0.0
    flat /= n
    spin = math.degrees(math.atan2(float(_cross(flat, e) @ b), float(flat @ e)))
    return dev, pro, spin


# what a degree of turn of the upper arm or the hand from one beat to the next costs (squared)
TURN_WEIGHT = 0.012


def _turn(a, b):
    """The angle (degrees) of the turn from rotation a to rotation b (3x3)."""
    c = (float(np.sum(a * b)) - 1.0) * 0.5
    return math.degrees(math.acos(max(-1.0, min(1.0, c))))


def _over(x, lo, hi):
    return lo - x if x < lo else (x - hi if x > hi else 0.0)


def arm_points(pose, side, arm="wide"):
    """The arm's centre line (layer frame px) where it may not touch the body: the upper arm's middle, the elbow, the forearm's middle
    (a hand may rest on the body: a hand on the hip, on the saya, on the hilt)."""
    part = side + "Arm"
    m = part_matrix(pose, part, ARM_PIVOT[part])
    cx = (FIST if side == "right" else LEFT_FIST)[arm][0]
    fore = m @ forearm_bend(pose, part)
    return [point(m, (cx, 2.0, 0.0)), point(m, (cx, 4.0, 0.0)), point(fore, (cx, 6.0, 0.0))]


# the arm's half-thickness, less a little (an arm brushing the body is no arm through it)
ARM_MARGIN = 1.4
# 2.3.2: the arm's centre line is kept this far (px) out of the head (a hand may reach past it to Yoru's hilt, never through it)
HEAD_MARGIN = 0.6


def through_body(pose, side, arm="wide", margin=ARM_MARGIN):
    """How deep (px) the arm's centre line goes into the torso (grown by margin: the arm's own half-thickness)."""
    inv = np.linalg.inv(part_matrix(pose, "torso", (0.0, 0.0, 0.0)))
    worst = 0.0
    for p in arm_points(pose, side, arm):
        q = point(inv, p)
        d = min(q[0] - (-4 - margin), (4 + margin) - q[0], q[1] - 0.5, 12 - q[1], q[2] - (-2 - margin), (2 + margin) - q[2])
        worst = max(worst, d)
    return worst


def arm_strain(pose, side, arm="wide"):
    """[(what, by how many degrees or px it is past what a body does)] for one arm of a pose (and the right hand's hold)."""
    part = side + "Arm"
    p = pose.get(part, {})
    rot = zyx(rad(p.get("pitch", 0.0)), rad(p.get("yaw", 0.0)), rad(p.get("roll", 0.0)))[:3, :3]
    out = []
    fo, fb = fold_strain(rot, p.get("bend", 0.0), side)
    sb = shoulder_back(rot)
    if sb > 0:
        out.append(("shoulder bent back", sb))
    if fo > 0:
        out.append(("elbow folding out", fo))
    if fb > 0:
        out.append(("elbow folding back", fb))
    bend = -p.get("bend", 0.0)
    if _over(bend, -2.0, ELBOW_MAX) > 0:
        out.append(("elbow " + ("backwards" if bend < 0 else "crumpled"), _over(bend, -2.0, ELBOW_MAX)))
    if side == "right":
        dev, pro, spin = wrist_angles(wrist(pose))
        for name, v, lo, hi in (("wrist tipped", dev, -RADIAL, ULNAR), ("hand turned", pro, -PRONATION, PRONATION),
                                ("wrist bent", spin, -WRIST_BEND, WRIST_BEND)):
            if _over(v, lo, hi) > 0:
                out.append((name, _over(v, lo, hi)))
    depth = through_body(pose, side, arm)
    if depth > 0:
        out.append(("arm through the body", depth))
    return out


def strain_cost(pose, side, arm="wide"):
    """The solvers' price for an unnatural arm (squared, past the limits)."""
    c = 0.0
    for what, by in arm_strain(pose, side, arm):
        c += (40.0 * by * by) if what == "arm through the body" else by * by
    return c


# the sword being posed (model px along it: its pommel and its point): its handle and blade are kept out of the arms, the body and
# the head (2.3.2, the owner of Yoru carried on the shoulder: "the sword handle clipping through the shoulder?"); set per blade
SWORD = (-6.61, 20.9)
# the part of the handle the fist holds (px either side of the grip point): inside the hand, where it belongs
HELD = 2.1
# the forearm ends and the hand begins this far down the arm (px): the guard against the fist is the hand holding the sword
HAND = 6.6


def _box_depth(pts, inv, lo, hi, margin):
    """How deep (px) the deepest of pts (n x 4, homogeneous) goes into the box lo..hi of the frame whose inverse is inv (grown by
    margin), 0 if none does."""
    q = pts @ inv.T
    d = np.minimum.reduce([q[:, 0] - (lo[0] - margin), (hi[0] + margin) - q[:, 0], q[:, 1] - (lo[1] - margin),
                           (hi[1] + margin) - q[:, 1], q[:, 2] - (lo[2] - margin), (hi[2] + margin) - q[:, 2]])
    return float(max(0.0, d.max()))


_SWORD_MESH = {}
SWORD_KEY = "crimson_dawn"


def set_sword(key):
    """The sword being posed (its mesh: blade, guard, handle)."""
    global SWORD_KEY, SWORD
    SWORD_KEY = key
    pts = _sword_mesh(key)
    SWORD = (float(pts[:, 1].min()), float(pts[:, 1].max()))


def _sword_mesh(key):
    """About 160 points of the sword's mesh (model px): its outline, the blade's width, the guard, the handle."""
    if key not in _SWORD_MESH:
        import sword_view as sv
        v = sv.mesh(key)[0][:, :3]
        # a grid's worth: one point per 1.2 px cell, so a long blade is not under-sampled
        cells = {}
        for p in v:
            cells.setdefault(tuple(np.floor(p / 1.2).astype(int)), p)
        _SWORD_MESH[key] = np.array(list(cells.values()), float)
    return _SWORD_MESH[key]


_THIN = {}


def sword_points(slide=0.0, n=None):
    """Points of the sword's mesh (model px, homogeneous), leaving out the part of the handle in the hand; n: about that many of them
    (the solvers' quick look; the checks take them all)."""
    pts = _sword_mesh(SWORD_KEY)
    if n is not None:
        key = (SWORD_KEY, n)
        if key not in _THIN:
            step = max(1, len(pts) // n)
            order = np.lexsort((pts[:, 0], pts[:, 2], pts[:, 1]))
            chosen = set(int(i) for i in order[::step])
            # 2.3.2: each 1.2 px band along the sword (the mesh grid's) keeps its outermost points (either way across and through
            # it): a sword touches with its outline, and Yoru's cross guard's tips (7.4 px out, curling 3.7 px up the blade) fell
            # between the stride's picks: 0.5 px through the arm and the head
            band = np.floor(pts[:, 1] / 1.2)
            for value in np.unique(band):
                idx = np.where(band == value)[0]
                for col in (0, 2):
                    chosen.add(int(idx[np.argmax(pts[idx, col])]))
                    chosen.add(int(idx[np.argmin(pts[idx, col])]))
            _THIN[key] = pts[sorted(chosen)]
        pts = _THIN[key]
    keep = ~((np.abs(pts[:, 1] - (GRIP - slide)) < HELD) & (np.hypot(pts[:, 0], pts[:, 2]) < 1.8))
    pts = pts[keep]
    return np.c_[pts, np.ones(len(pts))]


def rigid_inv(m):
    """The inverse of a rotation-and-shift matrix (4x4), without a general inverse."""
    r = m[:3, :3].T
    out = np.eye(4)
    out[:3, :3] = r
    out[:3, 3] = -r @ m[:3, 3]
    return out


def sword_hits(pose, arm="wide", slide=None, blade=None):
    """[(what, depth px)]: the sword in the right hand (or blade: its matrix in the layer frame) through the right arm, the left arm,
    the body or the head."""
    slide = slide_of(pose) if slide is None else slide
    m = blade if blade is not None else fist_frame(pose, "right", arm) @ wrist(pose) @ grip(slide)
    model_pts = sword_points(slide)
    out = []
    # 2.3.2: the handle (and its guard) and the blade apart: a handle's end resting against the wrist or the belly is how a sword is
    # held at rest; a blade is never through anything
    for label, keep in (("", model_pts[:, 1] >= 0.5), (" (handle)", model_pts[:, 1] < 0.5)):
        if keep.any():
            out += [(what + label, d) for what, d in _hits(pose, model_pts[keep] @ m.T, arm)]
    return out


def _hits(pose, pts, arm):
    w = 3 if arm == "slim" else 4
    out = []
    for part, lo, hi in (("rightArm", (-w + 1, -2, -2), (1, 4, 2)), ("leftArm", (-1, -2, -2), (w - 1, 4, 2))):
        pm = part_matrix(pose, part, ARM_PIVOT[part])
        fore = pm @ forearm_bend(pose, part)
        for frame, lo2, hi2, name in ((pm, lo, hi, "upper arm"), (fore, (lo[0], 4, lo[2]), (hi[0], HAND, hi[2]), "forearm")):
            d = _box_depth(pts, np.linalg.inv(frame), lo2, hi2, 0.3)
            if d > 0:
                out.append((("right " if part == "rightArm" else "left ") + name, d))
    d = _box_depth(pts, np.linalg.inv(part_matrix(pose, "torso", (0, 0, 0))), (-4, 0, -2), (4, 12, 2), 0.3)
    if d > 0:
        out.append(("body", d))
    d = _box_depth(pts, np.linalg.inv(part_matrix(pose, "head", (0, 0, 0))), (-4, -8, -4), (4, 0, 4), 0.3)
    if d > 0:
        out.append(("head", d))
    return out


_T4, _TM4 = tr(0, 4, 0), tr(0, -4, 0)
_EYE4 = np.eye(4)


class _Arm:
    """2.3.2: one arm of one pose, fast (the solvers evaluate thousands of candidate angles a beat): its pivot, the forearm's bend
    axis, the torso's frame; the frames and the strain for any angles, with the matrices built directly (no pose copied)."""

    def __init__(self, pose, side, arm="wide"):
        self.side, self.arm = side, arm
        part = side + "Arm"
        p = pose.get(part, {})
        piv = ARM_PIVOT[part]
        self.base = tr(piv[0] + p.get("x", 0.0), piv[1] + p.get("y", 0.0), piv[2] + p.get("z", 0.0))
        a = -rad(p.get("axis", 0.0))
        self.axis = (math.cos(a), 0.0, math.sin(a))
        self.fist = (FIST if side == "right" else LEFT_FIST)[arm]
        self.fist_t = tr(*self.fist)
        self.torso_inv = np.linalg.inv(part_matrix(pose, "torso", (0.0, 0.0, 0.0)))
        self.head_inv = np.linalg.inv(part_matrix(pose, "head", (0.0, 0.0, 0.0)))
        self.w = 3 if arm == "slim" else 4
        self._pts = {}

    def pts(self, slide):
        if slide not in self._pts:
            # a long blade gets more points (Yoru's mesh is two and a half times a katana's)
            self._pts[slide] = sword_points(slide, max(48, int(len(_sword_mesh(SWORD_KEY)) * 0.4)))
        return self._pts[slide]

    def head_depth(self, m, fore):
        """2.3.2: how deep (px) the arm's centre line (the upper arm, the elbow, the forearm, the hand) goes into the head grown by
        HEAD_MARGIN (anim.lint's "through the head" tests the forearm's line against the head shrunk by 0.6: never near it)."""
        cx = self.fist[0]
        worst = 0.0
        g = HEAD_MARGIN
        for mm, y in ((m, 0.0), (m, 2.0), (m, 4.0), (fore, 6.0), (fore, 8.0), (fore, 10.0)):
            q = self.head_inv @ (mm @ np.array((cx, y, 0.0, 1.0)))
            d = min(q[0] + 4 + g, 4 + g - q[0], q[1] + 8 + g, g - q[1], q[2] + 4 + g, 4 + g - q[2])
            worst = max(worst, d)
        return worst

    def sword_cost(self, m, fore, blade, slide):
        """2.3.2: the sword (blade: model px -> layer) kept out of this (right) arm, the body and the head."""
        pts = self.pts(slide) @ blade.T
        w = self.w
        g = 0.3 + SOLVER_SPARE
        d = _box_depth(pts, rigid_inv(m), (-w + 1, -2, -2), (1, 4, 2), g)
        d = max(d, _box_depth(pts, rigid_inv(fore), (-w + 1, 4, -2), (1, HAND, 2), g))
        d2 = _box_depth(pts, self.torso_inv, (-4, 0, -2), (4, 12, 2), g)
        d3 = _box_depth(pts, self.head_inv, (-4, -8, -4), (4, 0, 4), g)
        # 2.3.2: the top of the left upper arm (its shoulder, layer frame: there whichever way the arm is swung): the left arm is
        # placed after the sword, and Gryphon's curved saber, coiled back past the left side, ran through the shoulder joint itself,
        # where no left arm can get out of its way
        d4 = _box_depth(pts, _EYE4, (4.0, 0.0, -2.0), (8.0, 3.0, 2.0), g)
        return COLLIDE_WEIGHT * (d * d + d2 * d2 + d3 * d3 + d4 * d4)

    def frames(self, v):
        """(upper arm matrix, forearm matrix, fist matrix) for arm angles v (pitch, yaw, roll, bend degrees)."""
        m = self.base @ zyx(rad(v[0]), rad(v[1]), rad(v[2]))
        fore = m @ _T4 @ raxis(rad(v[3]), self.axis) @ _TM4
        return m, fore, fore @ self.fist_t

    def strain(self, v, m, fore, wr=None):
        """arm_strain's price for these frames (and the wrist wr, 3x3, for the right hand)."""
        c = 0.0
        fo, fb = fold_strain(m[:3, :3], v[3], self.side)
        sb = shoulder_back(m[:3, :3])
        c += fo * fo + fb * fb + sb * sb
        c += _over(-v[3], -2.0, ELBOW_MAX) ** 2
        if wr is not None:
            dev, pro, spin = wrist_angles(wr)
            c += _over(dev, -RADIAL, ULNAR) ** 2 + _over(pro, -PRONATION, PRONATION) ** 2 + _over(spin, -WRIST_BEND, WRIST_BEND) ** 2
        c *= STRAIN_WEIGHT
        cx = self.fist[0]
        worst = 0.0
        g = ARM_MARGIN + SOLVER_SPARE
        for mm, y in ((m, 2.0), (m, 4.0), (fore, 6.0)):
            q = self.torso_inv @ (mm @ np.array((cx, y, 0.0, 1.0)))
            d = min(q[0] + 4 + g, 4 + g - q[0], q[1] - 0.5, 12 - q[1], q[2] + 2 + g, 2 + g - q[2])
            worst = max(worst, d)
        h = self.head_depth(m, fore)
        return c + COLLIDE_WEIGHT * (worst * worst + h * h)


def nearest_euler(v, prev):
    """(pitch, yaw, roll) as the game will interpolate it from prev: of the two ways to write the same turn in this order (pitch +
    180, 180 - yaw, roll + 180), each with whole turns added, the one nearest prev (tools/sword_anims.py's unwrap writes them so)."""
    p, y, r = float(v[0]), float(v[1]), float(v[2])
    best = None
    for cand in ((p, y, r), (p + 180.0, 180.0 - y, r + 180.0)):
        c = list(cand)
        for k in range(3):
            c[k] -= 360.0 * round((c[k] - prev[k]) / 360.0)
        d = (c[0] - prev[0]) ** 2 + (c[1] - prev[1]) ** 2 + (c[2] - prev[2]) ** 2
        if best is None or d < best[0]:
            best = (d, c)
    return np.array(best[1])


def blend(v, other, w, other_after):
    """The arm and wrist angles (7) the game shows a fraction w of the way from v to other (other_after: other is the keyframe
    after v, written nearest it; else before, v written nearest it)."""
    v = np.asarray(v, float)
    o = np.asarray(other, float)
    if other_after:
        oa, ow, va, vw = nearest_euler(o[:3], v[:3]), nearest_euler(o[4:7], v[4:7]), v[:3], v[4:7]
    else:
        va, vw, oa, ow = nearest_euler(v[:3], o[:3]), nearest_euler(v[4:7], o[4:7]), o[:3], o[4:7]
    return np.r_[va * (1 - w) + oa * w, v[3] * (1 - w) + o[3] * w, vw * (1 - w) + ow * w]


def solve_blade(pose, target, blade_dir, edge_dir, seed=None, arm="wide", elbow=None, wrist_limit=55.0, slide=0.0, dir_weight=900.0,
                edge_weight=250.0, upper=None, upper_weight=120.0, between=None, starts=None):
    """The right arm and wrist that put the fist's centre at target (layer frame px) with the blade along blade_dir and the edge along
    edge_dir (layer frame), the arm kept natural (2.3.2: arm_strain). upper: the way the upper arm should point (layer frame), where
    the hand could reach the same place two ways (2.3.2, the owner of Yoru's hilt over the shoulder: "the shoulder is grabbing the
    sword in the opposite way it's meant to": the elbow up beside the head, the hand down onto the hilt from above, not from below).
    between: [(the neighbouring keyframe's 7 angles, w, after)]: the game's in-between frames to it (blend), each kept natural and
    clear too (2.3.2: every solved tick was natural, yet the half tick between two of them could turn the hand past its stop or fold
    the elbow out, the game interpolating the numbers). starts: more places to start the search from.
    Returns (rightArm dict, rightItem dict, error px, error degrees)."""
    target = np.array(target, float)
    bdir = np.array(blade_dir, float)
    bdir /= np.linalg.norm(bdir)
    edir = np.array(edge_dir, float)
    edir = edir - bdir * (edir @ bdir)
    edir /= np.linalg.norm(edir)
    anchor = None if seed is None else np.array(seed, float)
    seed = seed or [-40.0, 0.0, 0.0, -40.0, 0.0, 0.0, 0.0]
    model = _Arm(pose, "right", arm)
    held_t = tr(0, -(GRIP - slide), 0)
    up_dir = None if upper is None else np.array(upper, float) / np.linalg.norm(upper)
    # 2.3.2: the beat before's upper arm and hand, as turns (a flip of the hand written with nearby numbers is still a flip)
    prev_arm = None if anchor is None else zyx(rad(anchor[0]), rad(anchor[1]), rad(anchor[2]))[:3, :3]
    prev_wr = None if anchor is None else (rz(rad(anchor[6])) @ ry(rad(anchor[5])) @ rx(rad(anchor[4])))[:3, :3]

    def frames(v):
        m, fore, f = model.frames(v)
        wr = rz(rad(v[6])) @ ry(rad(v[5])) @ rx(rad(v[4]))
        w = f @ wr @ GRIP_ROT
        return m, fore, f, wr, w

    def cost(v):
        m, fore, f, wr, w = frames(v)
        pos = f[:3, 3]
        # 2.3.2: the fist kept out of the head and the space just in front of the face (the check's boxes, grown)
        hq = model.head_inv @ np.array((pos[0], pos[1], pos[2], 1.0))
        face = min(hq[0] + 4.5 + 0.6, 4.5 + 0.6 - hq[0], hq[1] + 8.5 + 0.6, 1.0 + 0.6 - hq[1], hq[2] + 9.5 + 0.6, 0.6 - hq[2])
        b = w[:3, 1]
        e = -w[:3, 0]
        c = np.sum((pos - target) ** 2) * 40.0
        if face > 0:
            c += COLLIDE_WEIGHT * face * face
        c += (1 - b @ bdir) * dir_weight + (1 - e @ edir) * edge_weight
        c += 0.0008 * (v[4] ** 2 + v[5] ** 2 + v[6] ** 2)
        c += 0.0006 * v[2] ** 2 + 0.0003 * v[1] ** 2
        # 2.3.2: a natural arm (arm_strain): the shoulder's turn, the elbow, the hand's hold, never through the body; and the sword
        # (its handle too) out of the arm that holds it, the body and the head
        c += model.strain(v, m, fore, wr[:3, :3])
        c += model.sword_cost(m, fore, w @ held_t, slide)
        for k in (4, 5, 6):
            over = abs(v[k]) - 120.0
            if over > 0:
                c += over * over
        if elbow is not None:
            c += 0.02 * (v[3] - elbow) ** 2
        if up_dir is not None:
            c += upper_weight * (1.0 - float(m[:3, 1] @ up_dir))
        for other, w, after in between or ():
            mv = blend(v, other, w, after)
            m2, fore2, _, wr2, w2 = frames(mv)
            c += model.strain(mv, m2, fore2, wr2[:3, :3]) + model.sword_cost(m2, fore2, w2 @ held_t, slide)
        if anchor is not None:
            # stay near the beat before: the game interpolates the angles, and a far-away solution swings the arm through the face. By
            # how far the upper arm and the hand really turn (2.3.2: the hand once flipped over in a tick, the arm turning against it)
            c += 0.0004 * float(np.sum((v[:4] - anchor[:4]) ** 2)) + 0.0002 * float(np.sum((v[4:] - anchor[4:]) ** 2))
            c += TURN_WEIGHT * (_turn(prev_arm, m[:3, :3]) ** 2 + _turn(prev_wr, wr[:3, :3]) ** 2)
            c += 0.004 * (v[3] - anchor[3]) ** 2
        return c

    best = None
    extra_starts = starts
    starts = [seed] + [[p0, 0.0, r0, -40.0, 0.0, 0.0, 0.0] for p0 in (-20.0, -70.0, -120.0, -165.0) for r0 in (-25.0, 25.0)]
    if up_dir is not None:
        starts.insert(1, [-165.0, 0.0, 20.0, -100.0, 0.0, 0.0, 0.0])
    for k, extra in enumerate(extra_starts or ()):
        starts.insert(1 + k, list(extra))
    # the beat before's arm (and refine's own starts) searched in full; the general starts given a short look each, the best of them
    # searched in full only if it beats what is found (they rarely do: a full search from each made the solve five times slower)
    full = 1 + len(extra_starts or ())
    quick = None
    for i, start in enumerate(starts):
        v, c = _solve(cost, start, 16.0, 5000 if i < full else 1200)
        if i < full:
            if best is None or c < best[1] - 1e-6:
                best = (v, c)
            if i == 0 and anchor is not None and c < 12.0:
                break  # the beat before's arm, moved a little, already does it well: no need to look further
        elif quick is None or c < quick[1] - 1e-6:
            quick = (v, c)
    if quick is not None and quick[1] < best[1] + 30.0:
        v, c = _solve(cost, quick[0], 4.0, 5000)
        if c < best[1] - 1e-6:
            best = (v, c)
    v = best[0]
    _, _, f, _, w = frames(v)
    err_p = float(np.linalg.norm(f[:3, 3] - target))
    err_d = math.degrees(math.acos(max(-1.0, min(1.0, w[:3, 1] @ bdir))))
    return ({"pitch": round(float(v[0]), 2), "yaw": round(float(v[1]), 2), "roll": round(float(v[2]), 2), "bend": round(float(v[3]), 2)},
            {"pitch": round(float(v[4]), 2), "yaw": round(float(v[5]), 2), "roll": round(float(v[6]), 2), "y": slide}, err_p, err_d)


def handle_point(pose, along, arm="wide"):
    """A point on the drawn katana's axis (model px along it, e.g. the second hand's place) in the layer frame (px)."""
    m = fist_frame(pose, "right", arm) @ wrist(pose) @ grip(slide_of(pose))
    return point(m, (0, along, 0)), direction(m, (0, 1, 0))


def solve_left_on_handle(pose, along=-7.4, arm="wide", seed=None):
    """The left arm whose fist closes round the handle {along} model px from the guard (the second hand of a two-handed grip): its
    centre on the handle's axis, its own grip axis (-z) along the blade, its knuckles out. Returns (leftArm dict, error px)."""
    target, axis = handle_point(pose, along, arm)
    seed = seed or [-50.0, 0.0, 0.0, -50.0]

    model = _Arm(pose, "left", arm)

    def cost(v):
        m, fore, f = model.frames(v)
        pos = f[:3, 3]
        gz = -f[:3, 2]
        c = np.sum((pos - target) ** 2) * 40.0
        c += (1 - abs(gz @ axis)) * 120.0
        c += 0.0006 * v[2] ** 2 + 0.0003 * v[1] ** 2
        c += model.strain(v, m, fore)
        return c

    best = None
    for start in [seed] + [[p0, 0.0, r0, -50.0] for p0 in (-20.0, -70.0, -120.0, -165.0) for r0 in (-25.0, 25.0)]:
        v, c = _solve(cost, start, 16.0, 3000)
        if best is None or c < best[1] - 1e-6:
            best = (v, c)
    v = best[0]
    q = with_arm(pose, "leftArm", v)
    err = float(np.linalg.norm(fist_frame(q, "left", arm)[:3, 3] - target))
    return {"pitch": round(float(v[0]), 2), "yaw": round(float(v[1]), 2), "roll": round(float(v[2]), 2), "bend": round(float(v[3]), 2)}, err


# 2.3.2: other places a free left arm may go when the hand-set one is in the sword's way (out to the side for balance, raised in
# front, swung back, up across the chest): clear_left starts from each
LEFT_STARTS = ((-30.0, 0.0, -58.0, -18.0), (-70.0, 8.0, -6.0, -22.0), (42.0, 0.0, -16.0, -18.0), (-95.0, 20.0, -40.0, -60.0),
               (10.0, 0.0, -35.0, -10.0))


def clear_left(pose, base, arm="wide", prev=None, mid=None, mid_u=0.5):
    """2.3.2: a left arm set by hand (out for balance, swung back, tucked), moved as little as it must to keep out of the sword in the
    right hand and the body, and natural (the hand-set arms were never checked against the blade, and a cut across the body went
    through them). prev: the left arm a tick before (it may not jump: the game interpolates it); where the hand-set arm cannot
    clear the blade by small moves, it looks from LEFT_STARTS too. mid: the pose half way from the beat before (its right arm and
    sword as the game shows them there, mid_u of the way: the beat's easing): the left arm as far from prev must be clear of that
    sword too."""
    blade = fist_frame(pose, "right", arm) @ wrist(pose) @ grip(slide_of(pose))
    pts = sword_points(slide_of(pose), max(48, int(len(_sword_mesh(SWORD_KEY)) * 0.4))) @ blade.T
    w = 3 if arm == "slim" else 4
    model = _Arm(pose, "left", arm)
    b0 = np.array([base.get(k, 0.0) for k in ("pitch", "yaw", "roll", "bend")], float)
    p0 = None if prev is None else np.array([prev.get(k, 0.0) for k in ("pitch", "yaw", "roll", "bend")], float)
    g = 0.3 + SOLVER_SPARE

    mid_model = mid_pts = None
    if mid is not None and p0 is not None:
        mid_model = _Arm(mid, "left", arm)
        mb = fist_frame(mid, "right", arm) @ wrist(mid) @ grip(slide_of(mid))
        mid_pts = sword_points(slide_of(mid), max(48, int(len(_sword_mesh(SWORD_KEY)) * 0.4))) @ mb.T

    def hit(v):
        m, fore, _ = model.frames(v)
        d = _box_depth(pts, rigid_inv(m), (-1, -2, -2), (w - 1, 4, 2), g)
        d = max(d, _box_depth(pts, rigid_inv(fore), (-1, 4, -2), (w - 1, 10, 2), g))
        if mid_model is not None:
            mm, mf, _ = mid_model.frames(p0 + (v - p0) * mid_u)
            d = max(d, _box_depth(mid_pts, rigid_inv(mm), (-1, -2, -2), (w - 1, 4, 2), g),
                    _box_depth(mid_pts, rigid_inv(mf), (-1, 4, -2), (w - 1, 10, 2), g))
        return d, m, fore

    d0, m0, f0 = hit(b0)
    if d0 <= 0.0 and model.strain(b0, m0, f0) <= 0.0 and (p0 is None or float(np.max(np.abs(b0 - p0))) < 25.0):
        return dict(base)

    def cost(v):
        d, m, fore = hit(v)
        c = 0.01 * float(np.sum((v - b0) ** 2)) + COLLIDE_WEIGHT * d * d + model.strain(v, m, fore)
        if p0 is not None:
            c += 0.03 * float(np.sum((v - p0) ** 2))
        return c

    best = None
    for start in [b0] + ([p0] if p0 is not None else []) + [np.array(x, float) for x in LEFT_STARTS]:
        v, c = _solve(cost, start, 12.0, 3000)
        if best is None or c < best[1] - 1e-6:
            best = (v, c)
        if c < 1.0:
            break
    v = best[0]
    return {"pitch": round(float(v[0]), 2), "yaw": round(float(v[1]), 2), "roll": round(float(v[2]), 2), "bend": round(float(v[3]), 2)}


def solve_left_at(pose, target, seed=None, arm="wide"):
    """The left arm whose fist centre is at target (layer frame px): a hand on the saya at the hip, or out for balance."""
    target = np.array(target, float)
    seed = seed or [0.0, 0.0, 0.0, -30.0]

    model = _Arm(pose, "left", arm)

    def cost(v):
        m, fore, f = model.frames(v)
        c = np.sum((f[:3, 3] - target) ** 2) * 40.0
        c += 0.0005 * (v[1] ** 2 + v[2] ** 2)
        c += model.strain(v, m, fore)
        return c

    v, _ = _solve(cost, seed, 16.0, 3000)
    q = with_arm(pose, "leftArm", v)
    err = float(np.linalg.norm(fist_frame(q, "left", arm)[:3, 3] - target))
    return {"pitch": round(float(v[0]), 2), "yaw": round(float(v[1]), 2), "roll": round(float(v[2]), 2), "bend": round(float(v[3]), 2)}, err


def layer_from_entity(pose, p):
    """A point of E (blocks) in the layer frame (px)."""
    m = np.linalg.inv(layer_to_entity(pose))
    return point(m, p)


def layer_dir_from_entity(pose, d):
    m = np.linalg.inv(layer_to_entity(pose))
    return direction(m, d)


# the blades' sori: each curves towards its back (+x), more towards its point, and the saya follows the same line. Drawn out of the saya
# a blade goes along that line, not straight, or its point would leave the saya through its side. 2.3.2: each blade by its own curve
# (tools/saya_slide.py: the rigid turn that keeps the part still inside on the saya's line, fitted from the blade's mesh; before, one
# circle of radius 180 px for all, and Gryphon's saber, curved three times as hard, came out through its scabbard's side).


def arc(out, key="crimson_dawn"):
    """The blade {key} drawn out px along its own curve (model px -> model px)."""
    import saya_slide
    ang, x, y = saya_slide.at(key, out)
    return tr(x, y, 0) @ rz(math.radians(ang))


def sheathed_grip(pose, slide=0.0, out=0.0, key="crimson_dawn"):
    """Where the right fist must be (layer frame px) and how the katana points, to hold the handle of the sheathed katana (slide px
    down the handle; the blade drawn out px along the saya): the blade's frame as the saya holds it. Returns (target, blade_dir,
    edge_dir) in the layer frame."""
    m = saya_matrix(pose) @ arc(out, key)
    g = point(m, (0, GRIP - slide, 0))
    b = direction(m, (0, 1, 0))
    e = direction(m, (-1, 0, 0))
    return layer_from_entity(pose, g), layer_dir_from_entity(pose, b), layer_dir_from_entity(pose, e)


# --- 2.2.7: Yoru on the back (body-part space px: x the player's left, y down, z back) --------------------------------------------
# The owner: "Yoru is big so it's on the back of the player ... (make it tied with a rope to the player's back", then "Yoru IS way
# bigger than that": 2.75 blocks, half as tall again as the player. Upright it would stand in the ground or the head, its grip out of an
# arm's reach: it is slung across the back (the placement searched for: the pommel over the head, the tip clear of the ground and the
# body, the grip in reach), hilt up over the right shoulder, the cross guard behind it, the blade down across the back past the left
# leg, standing off the back behind the head (sword/BackCarry.java has the same numbers, SwordTests checks they agree).
BACK_GUARD = (-7.5, 0.0, 5.4)
BACK_TILT = 45.0                  # degrees off the vertical, the point toward the left
BACK_LEAN = 4.0                   # degrees: the point in toward the legs, the hilt out behind the head
BACK_AXIS = (math.sin(math.radians(BACK_TILT)) * math.cos(math.radians(BACK_LEAN)),
             math.cos(math.radians(BACK_TILT)) * math.cos(math.radians(BACK_LEAN)), -math.sin(math.radians(BACK_LEAN)))


def back(pose=None):
    """Katana model px -> the layer's frame (px): Yoru on the back (the upper body's own turn and bend carry it)."""
    return (part_matrix(pose or {}, "torso", (0.0, 0.0, 0.0)) @ tr(*BACK_GUARD) @ rz(-math.radians(BACK_TILT))
            @ rx(-math.radians(BACK_LEAN)) @ sc(SCALE / PX))


def back_matrix(pose):
    """Katana model px -> E (blocks): Yoru on the back."""
    return layer_to_entity(pose) @ back(pose)


def back_grip(pose, lift=0.0):
    """Where the right fist must be (layer frame px) to hold Yoru's handle on the back, the sword lifted lift px up along itself out of
    its ropes, and how it points: (target, blade_dir, edge_dir) in the layer frame."""
    m = back(pose)
    return point(m, (0, GRIP - lift, 0)), direction(m, (0, 1, 0)), direction(m, (-1, 0, 0))


def _rope_loop(corners, step=0.45, times=2):
    pts = []
    for i, a in enumerate(corners):
        b = corners[(i + 1) % len(corners)]
        n = max(1, math.ceil(math.dist(a, b) / step))
        pts += [tuple(a[k] + (b[k] - a[k]) * j / n for k in range(3)) for j in range(n)]
    for _ in range(times):
        nxt = []
        for i, a in enumerate(pts):
            b = pts[(i + 1) % len(pts)]
            nxt.append(tuple(a[k] * 0.75 + b[k] * 0.25 for k in range(3)))
            nxt.append(tuple(a[k] * 0.25 + b[k] * 0.75 for k in range(3)))
        pts = nxt
    out = []
    for x, y, z in pts:
        if z > 0:
            d, depth = _blade_at(x, y)
            t = max(0.0, min(1.0, (d - 1.5) / 3.5))
            k = 1.0 - t * t * (3 - 2 * t)
            over = depth + 0.4 + 0.42 + 0.05
            z = max(z, z + (over - z) * k)
        out.append((x, y, z))
    return out


def _blade_at(x, y):
    """How far (x, y) on the back is from Yoru's line (px, seen from behind), and the blade's depth (z) there."""
    ax = BACK_AXIS
    flat = math.hypot(ax[0], ax[1])
    ux, uy = ax[0] / flat, ax[1] / flat
    dx, dy = x - BACK_GUARD[0], y - BACK_GUARD[1]
    along = dx * ux + dy * uy
    d = abs(dx * uy - dy * ux)
    t = along / flat
    return d, BACK_GUARD[2] + t * ax[2]


def back_ropes():
    """2.2.7: Yoru's ropes as (quad corners in body px, rgb) for the previews: sword/BackCarry's paths (the game's), as plain tubes."""
    x, z = 4.25 + 0.5, 2.25 + 0.5
    loops = [_rope_loop([(2.6, -0.5, -z), (-x, 10.6, -z), (-x, 10.8, 0.0), (-x, 10.6, z), (2.6, -0.5, z), (2.9, -0.75, 0.0)]),
             _rope_loop([(x, 10.0, -z), (-x, 10.0, -z), (-x, 10.0, z), (x, 10.0, z)])]
    out = []
    for loop in loops:
        n = len(loop)
        for i in range(n):
            a, b = np.array(loop[i]), np.array(loop[(i + 1) % n])
            t = b - a
            if np.linalg.norm(t) < 1e-6:
                continue
            t /= np.linalg.norm(t)
            u = np.cross(t, (0, 1, 0) if abs(t[1]) < 0.9 else (1, 0, 0))
            u /= np.linalg.norm(u)
            v = np.cross(t, u)
            for k in range(6):
                c0, s0 = math.cos(2 * math.pi * k / 6), math.sin(2 * math.pi * k / 6)
                c1, s1 = math.cos(2 * math.pi * (k + 1) / 6), math.sin(2 * math.pi * (k + 1) / 6)
                r = 0.42
                quad = [a + (u * c0 + v * s0) * r, b + (u * c0 + v * s0) * r, b + (u * c1 + v * s1) * r, a + (u * c1 + v * s1) * r]
                out.append(([tuple(q) for q in quad], (201, 167, 116) if (i // 2 + k) % 3 else (126, 100, 63)))
    return out


def saya_point(pose, along):
    """A point on the saya's axis (model px from the koiguchi), in the layer frame (px)."""
    return layer_from_entity(pose, point(saya_matrix(pose), (0, along, 0)))


# --- the figure's boxes, for the previews (E, blocks) -------------------------------------------------------------------------------

def _box(m, x0, y0, z0, x1, y1, z1):
    return [point(m, (x, y, z)) for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]


def boxes(pose, arm="wide"):
    """[(part, 8 corners in E)] as bendy-lib draws them: each limb in two halves, the lower half turned by the bend about the middle."""
    out = []
    to_e = body_to_entity(pose)
    layer = layer_to_entity(pose)
    torso = part_matrix(pose, "torso", (0, 0, 0))
    out.append(("torso", _box(layer @ torso, -4, 0, -2, 4, 6, 2)))
    out.append(("torso", _box(to_e @ torso, -4, 6, -2, 4, 12, 2)))
    head = part_matrix(pose, "head", (0, 0, 0))
    out.append(("head", _box(layer @ head, -4, -8, -4, 4, 0, 4)))
    for part in ("rightArm", "leftArm"):
        m = layer @ part_matrix(pose, part, ARM_PIVOT[part])
        w = 3 if arm == "slim" else 4
        x0 = (-w + 1) if part == "rightArm" else -1
        x1 = x0 + w
        out.append((part, _box(m, x0, -2, -2, x1, 4, 2)))
        out.append((part, _box(m @ forearm_bend(pose, part), x0, 4, -2, x1, 10, 2)))
    for part in ("rightLeg", "leftLeg"):
        m = to_e @ part_matrix(pose, part, LEG_PIVOT[part])
        p = pose.get(part, {})
        knee = tr(0, 6, 0) @ rx(rad(p.get("bend", 0.0))) @ tr(0, -6, 0)
        out.append((part, _box(m, -2, 0, -2, 2, 6, 2)))
        out.append((part, _box(m @ knee, -2, 6, -2, 2, 12, 2)))
    return out


def shoulders(pose):
    layer = layer_to_entity(pose)
    return point(layer, ARM_PIVOT["rightArm"]), point(layer, ARM_PIVOT["leftArm"])


def euler_zyx(m):
    """(pitch, yaw, roll) degrees of a rotation R = Rz(roll) Ry(yaw) Rx(pitch)."""
    b = -math.asin(max(-1.0, min(1.0, m[2, 0])))
    a = math.atan2(m[2, 1], m[2, 2])
    c = math.atan2(m[1, 0], m[0, 0])
    return math.degrees(a), math.degrees(b), math.degrees(c)


def saya_to_hold(pose, blade_e, out=0.0):
    """The leftItem (x, y, z px; pitch, yaw, roll degrees) that puts the saya where the katana blade_e (model px -> E) is, the blade
    drawn out of it by {out} model px: the saya follows the hand's sword, exactly."""
    base = body_to_entity(pose) @ part_matrix(pose, "torso", (0.0, 0.0, 0.0)) @ tr(*HIP_MOUTH)
    want = blade_e @ tr(0, out, 0)
    move = np.linalg.inv(base) @ want @ np.linalg.inv(sc(SCALE / PX)) @ np.linalg.inv(frame_from(HIP_AXIS, HIP_EDGE))
    pitch, yaw, roll = euler_zyx(move)
    x, y, z = move[:3, 3]
    return {"x": round(float(x), 3), "y": round(float(y), 3), "z": round(float(z), 3), "pitch": round(pitch, 2), "yaw": round(yaw, 2),
            "roll": round(roll, 2)}


REST_FRAME = frame_from(HIP_AXIS, HIP_EDGE)


def saya_turn(hilt_dir, edge_dir):
    """The leftItem rotation (pitch, yaw, roll degrees; never a move: the owner, "the sheath never moves out of place, it can
    rotate, but never move") that turns the saya about its koiguchi so its handle points along hilt_dir and its edge along edge_dir
    (body-part space)."""
    want = frame_from(-np.array(hilt_dir, float), edge_dir)
    turn = want @ np.linalg.inv(REST_FRAME)
    pitch, yaw, roll = euler_zyx(turn)
    return {"pitch": round(pitch, 2), "yaw": round(yaw, 2), "roll": round(roll, 2)}


def rest_hilt():
    """The handle's direction at rest (body-part space)."""
    return -REST_FRAME[:3, 1]


def toward_right_hand(pose):
    """From the koiguchi towards the right shoulder (body-part space): where a handle turned for the right hand points."""
    m = body_to_entity(pose) @ part_matrix(pose, "torso", (0.0, 0.0, 0.0))
    shoulder_e = point(layer_to_entity(pose), (-5.0, 2.0, -3.5))
    s_body = point(np.linalg.inv(m), shoulder_e)
    v = s_body - np.array(HIP_MOUTH)
    return v / np.linalg.norm(v)
