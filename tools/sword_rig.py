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


def solve_blade(pose, target, blade_dir, edge_dir, seed=None, arm="wide", elbow=None, wrist_limit=55.0, slide=0.0, dir_weight=900.0):
    """The right arm and wrist that put the fist's centre at target (layer frame px) with the blade along blade_dir and the edge along
    edge_dir (layer frame). Returns (rightArm dict, rightItem dict, error px, error degrees)."""
    target = np.array(target, float)
    bdir = np.array(blade_dir, float)
    bdir /= np.linalg.norm(bdir)
    edir = np.array(edge_dir, float)
    edir = edir - bdir * (edir @ bdir)
    edir /= np.linalg.norm(edir)
    anchor = None if seed is None else np.array(seed, float)
    seed = seed or [-40.0, 0.0, 0.0, -40.0, 0.0, 0.0, 0.0]

    def frames(v):
        q = with_arm(pose, "rightArm", v[:4])
        q["rightItem"] = {"pitch": v[4], "yaw": v[5], "roll": v[6], "y": slide}
        f = fist_frame(q, "right", arm)
        w = f @ wrist(q) @ GRIP_ROT
        return f, w

    def cost(v):
        f, w = frames(v)
        pos = f[:3, 3]
        b = w[:3, 1]
        e = -w[:3, 0]
        c = np.sum((pos - target) ** 2) * 40.0
        c += (1 - b @ bdir) * dir_weight + (1 - e @ edir) * 250.0
        c += 0.002 * (v[4] ** 2 + v[5] ** 2 + v[6] ** 2)
        c += 0.0025 * v[2] ** 2 + 0.0008 * v[1] ** 2  # a natural arm: pitch and the elbow do the work, little twist
        if v[3] > 0:
            c += v[3] ** 2  # an elbow never bends backwards
        if v[3] < -140:
            c += (v[3] + 140) ** 2
        for k in (4, 5, 6):
            over = abs(v[k]) - wrist_limit
            if over > 0:
                c += over * over * 2.0
        if elbow is not None:
            c += 0.02 * (v[3] - elbow) ** 2
        if anchor is not None:
            # stay near the beat before: the game interpolates the angles, and a far-away solution swings the arm through the face
            c += 0.0012 * float(np.sum((v[:4] - anchor[:4]) ** 2)) + 0.0006 * float(np.sum((v[4:] - anchor[4:]) ** 2))
        return c

    best = None
    for start in [seed] + [[p0, 0.0, r0, -40.0, 0.0, 0.0, 0.0] for p0 in (-20.0, -70.0, -120.0, -165.0) for r0 in (-25.0, 25.0)]:
        v, c = _solve(cost, start, 16.0, 5000)
        if best is None or c < best[1] - 1e-6:
            best = (v, c)
    v = best[0]
    f, w = frames(v)
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

    def cost(v):
        q = with_arm(pose, "leftArm", v)
        f = fist_frame(q, "left", arm)
        pos = f[:3, 3]
        gz = -f[:3, 2]
        c = np.sum((pos - target) ** 2) * 40.0
        c += (1 - abs(gz @ axis)) * 120.0
        c += 0.0025 * v[2] ** 2 + 0.0008 * v[1] ** 2
        if v[3] > 0:
            c += v[3] ** 2
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


def solve_left_at(pose, target, seed=None, arm="wide"):
    """The left arm whose fist centre is at target (layer frame px): a hand on the saya at the hip, or out for balance."""
    target = np.array(target, float)
    seed = seed or [0.0, 0.0, 0.0, -30.0]

    def cost(v):
        q = with_arm(pose, "leftArm", v)
        c = np.sum((fist_frame(q, "left", arm)[:3, 3] - target) ** 2) * 40.0
        c += 0.0005 * (v[1] ** 2 + v[2] ** 2)
        if v[3] > 0:
            c += v[3] ** 2
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


# the blades' sori: each is a gentle arc bending towards its back (+x), radius about 170 to 290 model px (measured on the owner's
# meshes; the saya follows the same curve). Drawn out of the saya, a blade travels along that arc, not in a straight line, or its point
# would leave the saya through its side.
SORI = 180.0


def arc(out):
    """The katana drawn out px along its own curve (model px -> model px): a turn about the curve's centre (SORI, 0, 0)."""
    return tr(SORI, 0, 0) @ rz(out / SORI) @ tr(-SORI, 0, 0)


def sheathed_grip(pose, slide=0.0, out=0.0):
    """Where the right fist must be (layer frame px) and how the katana points, to hold the handle of the sheathed katana (slide px
    down the handle; the blade drawn out px along the saya): the blade's frame as the saya holds it. Returns (target, blade_dir,
    edge_dir) in the layer frame."""
    m = saya_matrix(pose) @ arc(out)
    g = point(m, (0, GRIP - slide, 0))
    b = direction(m, (0, 1, 0))
    e = direction(m, (-1, 0, 0))
    return layer_from_entity(pose, g), layer_dir_from_entity(pose, b), layer_dir_from_entity(pose, e)


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
