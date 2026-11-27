#!/usr/bin/env python3
"""3.8.9: how a coat moves on its wearer (the owner: "The arms cannot clip through the coats, when walking the coat should be pushed back
like the wind is pushing it"): the reference of armor/CoatCloth.java (the same rule, the same numbers), used by tools/clothes_view.py's
renders and checked by its penetration test. The coat's points at rest are as baked (tools/clothes_bake.py: the body's frame, model px:
x the wearer's left, y down from the top of the body, z back); its hanging sleeves and cuffs are baked onto the arms (they go with them).

  1. The wind of walking (the body's frame): the faster the wearer goes forward, the further back the coat is blown (BACK radians at full
     speed, the caller's back 1), each point turned back about the shoulders' line by more the lower it hangs (its depth below SHOULDER
     over LENGTH px, to the power CURVE), fluttering (FLUTTER); going sideways blows it across (SIDE); going backwards, forward.
  2. Kept off the body (the body's frame): a point of the coat's front over the torso is turned back no further than the torso's front
     (TORSO_FRONT, less MARGIN); below the hips each leg is a wedge about the hip line (its stride, its half-depth LEG_HALF over the
     distance down): the cloth ahead of it stays ahead, the cloth behind stays behind. Both are worked out from the coat's surfaces at rest
     (FRONT_PANEL, BACK_PANEL), so the cloth's two faces (outside and lining) move together and it keeps its thickness.
  3. Kept off the arms (model space, after the body's own turn): each arm is two boxes (the upper arm and the forearm past the elbow, as
     the game bends it), grown by MARGIN; a point of the coat inside one is pushed out of it along the way it lay from that arm at rest
     (in the arm's own frame: smooth across the cloth, so it tents round the arm, never tears), to the box's side, its lining (the _in
     groups) LINING px less far than its cloth, so neither shows through the other. The coat's sleeves are never touched by the arms.
  4. The sleeves (the owner: "the arms should never interact with the coat's arms", with Garp's coat over his shoulders, blown back):
     each empty sleeve is hinged at its shoulder (SLEEVE_HINGE), hangs flared FLARE out from it (clear of the arm under it, which never
     moves it), and walking blows it back from that hinge further than the coat (SLEEVE_BACK at full speed), fluttering more.
"""
import math

import numpy as np

SHOULDER = 1.0
LENGTH = 20.0
CURVE = 0.75
BACK = 1.0
SIDE = 0.35
FLUTTER = 0.09
TORSO_FRONT = -2.0
TORSO_HALF = 4.0
MARGIN = 0.35
LINING = 0.25
HIP = 12.0
LEG_HALF = 2.0
LEG_BAND = 2.3
FRONT_PANEL = -3.0
BACK_PANEL = 3.6
LEG_X = {"right": -1.9, "left": 1.9}
# the arm's boxes in its own frame (px from its pivot): x across (wide and slim), y down the arm (the upper arm, then the forearm in the
# forearm's frame), z
ARM_X = {("right", False): (-3.0, 1.0), ("left", False): (-1.0, 3.0), ("right", True): (-2.0, 1.0), ("left", True): (-1.0, 2.0)}
UPPER_Y = (-2.0, 4.0)
FORE_Y = (4.0, 10.0)
ARM_Z = (-2.0, 2.0)
ARM_PIVOT = {"right": (-5.0, 2.0, 0.0), "left": (5.0, 2.0, 0.0)}
OPEN = 1.1
OPEN_AT = (8.6, 0.5)
SLEEVE_HINGE = {"right": (-7.6, 0.6, 0.2), "left": (7.6, 0.6, 0.2)}
SLEEVE_LENGTH = 12.0
FLARE = 0.17
SLEEVE_BACK = 1.25
SLEEVE_FLUTTER = 0.16


def smooth(a, b, x):
    t = np.clip((x - a) / (b - a), 0.0, 1.0)
    return t * t * (3 - 2 * t)


def rot_x(points, angle, y0, z0):
    """points (n x 3) turned by angle (n, radians; + takes the bottom back) about the x axis through (y0, z0)."""
    dy, dz = points[:, 1] - y0, points[:, 2] - z0
    c, s = np.cos(angle), np.sin(angle)
    out = points.copy()
    out[:, 1] = y0 + dy * c - dz * s
    out[:, 2] = z0 + dy * s + dz * c
    return out


def rot_z(points, angle, x0, y0):
    dx, dy = points[:, 0] - x0, points[:, 1] - y0
    c, s = np.cos(angle), np.sin(angle)
    out = points.copy()
    out[:, 0] = x0 + dx * c - dy * s
    out[:, 1] = y0 + dx * s + dy * c
    return out


def wind_angles(rest, back, side, phase):
    """Each point's turn back (and across) by the wind: back and side in -1..1, phase the flutter's (radians)."""
    depth = np.clip((rest[:, 1] - SHOULDER) / LENGTH, 0.0, 1.0) ** CURVE
    strength = min(1.0, abs(back) + abs(side))
    flutter = FLUTTER * strength * np.sin(phase + 0.35 * rest[:, 1] + 0.25 * rest[:, 0])
    return (back * BACK + flutter) * depth, -side * SIDE * depth


def torso_limit(rest, theta):
    """The wind's turn of the coat's front over the torso held to the torso's front (worked from the front panel at rest)."""
    r = np.maximum(rest[:, 1] - SHOULDER, 0.05)
    z = FRONT_PANEL
    rho = np.sqrt(r * r + z * z)
    psi = np.arctan2(z, r)
    allow = np.arcsin(np.clip((TORSO_FRONT - MARGIN) / rho, -1.0, 1.0)) - psi
    front = rest[:, 2] < -0.5
    over = smooth(TORSO_HALF + 0.9, TORSO_HALF - 0.3, np.abs(rest[:, 0])) * (rest[:, 1] < HIP + 1.0) * front
    clamped = np.minimum(theta, allow)
    return theta + (clamped - theta) * over


def legs_push(rest, theta, legs):
    """Below the hips: each leg's wedge about the hip line keeps the cloth ahead of it ahead and behind behind; returns the extra turn."""
    extra = np.zeros(len(rest))
    front = rest[:, 2] < 0.0
    ref = rest.copy()
    ref[:, 2] = np.where(front, FRONT_PANEL, BACK_PANEL)
    ref = rot_x(ref, theta, SHOULDER, 0.0)
    below = np.maximum(ref[:, 1] - HIP, 0.05)
    psi = np.arctan2(ref[:, 2], below)
    half = np.arctan2(LEG_HALF + MARGIN, below)
    for side, alpha in legs.items():
        band = smooth(LEG_BAND, LEG_BAND - 1.2, np.abs(rest[:, 0] - LEG_X[side])) * smooth(HIP - 1.0, HIP + 0.5, ref[:, 1])
        need = np.where(front, np.minimum(0.0, (alpha - half) - psi), np.maximum(0.0, (alpha + half) - psi))
        extra += need * band
    return extra


def rot_y(points, angle, x0, z0):
    """points turned by angle (n, radians) about the vertical through (x0, z0) (+ takes +x towards -z)."""
    dx, dz = points[:, 0] - x0, points[:, 2] - z0
    c, s = np.cos(angle), np.sin(angle)
    out = points.copy()
    out[:, 0] = x0 + dx * c + dz * s
    out[:, 2] = z0 - dx * s + dz * c
    return out


def opened(rest, back):
    """Rule 1's opening: blown back, the coat's open front panels swing out round the sides of the body (about the vertical at each
    shoulder's outer edge, OPEN_AT), the lower the further (OPEN at full speed), so they go back round the arms, not through them."""
    out = rest.copy()
    w = max(0.0, back)
    if w <= 0.0:
        return out
    depth = np.clip((rest[:, 1] - SHOULDER) / LENGTH, 0.0, 1.0) ** 0.6
    front = smooth(0.5, -2.0, rest[:, 2])
    for sign in (1.0, -1.0):
        on = (np.sign(rest[:, 0]) == sign) & (np.abs(rest[:, 0]) > 0.0)
        angle = -sign * OPEN * w * depth * front * smooth(0.5, 2.5, np.abs(rest[:, 0]))
        turned = rot_y(rest, angle, sign * OPEN_AT[0], OPEN_AT[1])
        out = np.where(on[:, None], turned, out)
    return out


def body_moved(rest, legs, back=0.0, side=0.0, phase=0.0):
    """Rules 1 and 2: the coat's points in the body's frame. legs: {side: the leg's pitch, radians (+ back)}."""
    theta, across = wind_angles(rest, back, side, phase)
    theta = torso_limit(rest, theta)
    moved = rot_x(opened(rest, back), theta, SHOULDER, 0.0)
    moved = rot_x(moved, legs_push(rest, theta, legs), HIP, 0.0)
    return rot_z(moved, across, 0.0, SHOULDER)


def rest_ways(rest, slim=False):
    """{(side, segment): the unit way (n x 2, x and z in the arm's frame) each point lay from that arm's box's middle at rest}."""
    out = {}
    for side in ("right", "left"):
        x0, x1 = ARM_X[(side, slim)]
        q = rest - np.array(ARM_PIVOT[side])
        d = np.stack([q[:, 0] - (x0 + x1) / 2.0, q[:, 2] - (ARM_Z[0] + ARM_Z[1]) / 2.0], axis=1)
        n = np.linalg.norm(d, axis=1, keepdims=True)
        d = np.where(n > 1e-6, d / np.maximum(n, 1e-6), np.array([0.0, -1.0]))
        out[side] = d
    return out


def exit_along(c, u, lo, hi):
    """How far (n) each point c (n x 3, a box's frame) must go along u (n x 3) to leave the box lo..hi (the slabs' nearest far side)."""
    t = np.full(len(c), np.inf)
    for k in range(3):
        uk = u[:, k]
        far = np.where(uk > 0, hi[k], lo[k])
        with np.errstate(divide="ignore", invalid="ignore"):
            tk = np.where(np.abs(uk) > 1e-6, (far - c[:, k]) / uk, np.inf)
        t = np.minimum(t, tk)
    return np.maximum(t, 0.0)


def arms_push(points, frames, ways, lining, slim=False):
    """Rule 3: points (model px) pushed out of the arms' boxes, each along the way it lay from that arm at rest (in the arm's frame)
    until it is out (its lining LINING px less far). frames: {(side, "upper" | "fore"): the segment's frame -> model px, 4 x 4}."""
    out = points.copy()
    for (side, segment), frame in frames.items():
        x0, x1 = ARM_X[(side, slim)]
        y0, y1 = UPPER_Y if segment == "upper" else FORE_Y
        # the top of the upper arm is the shoulder the coat rests on: no margin above it
        lo = np.array([x0 - MARGIN, y0 - (0.0 if segment == "upper" else MARGIN), ARM_Z[0] - MARGIN])
        hi = np.array([x1 + MARGIN, y1 + MARGIN, ARM_Z[1] + MARGIN])
        inv = np.linalg.inv(frame)
        c = (np.c_[out, np.ones(len(out))] @ inv.T)[:, :3]
        inside = np.all((c > lo) & (c < hi), axis=1)
        if not inside.any():
            continue
        u = np.stack([ways[side][:, 0], np.zeros(len(c)), ways[side][:, 1]], axis=1)
        push = np.where(inside, exit_along(c, u, lo, hi) + np.where(lining, 0.0, LINING), 0.0)
        c = c + u * push[:, None]
        out = np.where(inside[:, None], (np.c_[c, np.ones(len(c))] @ frame.T)[:, :3], out)
    return out


def sleeve(rest, side, back=0.0, across=0.0, phase=0.0):
    """Rule 4: an empty sleeve's points (the body's frame): flared out from its shoulder hinge, then blown back (and across) from it, the
    more the lower, fluttering."""
    hx, hy, hz = SLEEVE_HINGE[side]
    flare = -FLARE if side == "left" else FLARE
    out = rot_z(rest, np.full(len(rest), flare), hx, hy)
    depth = np.clip((rest[:, 1] - hy) / SLEEVE_LENGTH, 0.0, 1.0)
    strength = min(1.0, abs(back) + abs(across))
    flutter = SLEEVE_FLUTTER * strength * np.sin(phase * 1.3 + 0.5 * rest[:, 1] + (1.7 if side == "left" else 0.0))
    out = rot_x(out, (back * SLEEVE_BACK + flutter) * depth ** 1.2, hy, hz)
    return rot_z(out, -across * SIDE * depth, hx, hy)


# 3.8.9 (the owner: "head is clipping through the coat"): the head's box in its own frame (x and z within 4 of its axis, from the top of
# the body up 8 px), grown by MARGIN: a turned or raised head pushes the collar out of its way as an arm pushes the coat (rule 3)
HEAD_HALF = 4.0
HEAD_Y = (-8.0, 0.0)
HEAD_REACH = 2.0


def head_push(points, frame, rest, lining):
    """The collar (the coat's points from HEAD_REACH px below the shoulders up) pushed straight out from the neck (model space, level)
    until it is out of the head's box (frame: the head's frame -> model px), its lining LINING px less far."""
    out = points.copy()
    h = HEAD_HALF + MARGIN
    lo = np.array([-h, HEAD_Y[0] - MARGIN, -h])
    hi = np.array([h, HEAD_Y[1], h])  # the head's bottom is the neck the coat sits round: no margin below it
    inv = np.linalg.inv(frame)
    c = (np.c_[out, np.ones(len(out))] @ inv.T)[:, :3]
    inside = np.all((c > lo) & (c < hi), axis=1) & (rest[:, 1] < HEAD_REACH)
    if not inside.any():
        return out
    r = np.stack([out[:, 0], np.zeros(len(out)), out[:, 2]], axis=1)
    ln = np.linalg.norm(r, axis=1, keepdims=True)
    r = np.where(ln > 1e-6, r / np.maximum(ln, 1e-6), np.array([0.0, 0.0, 1.0]))
    u = r @ inv[:3, :3].T
    push = np.where(inside, exit_along(c, u, lo, hi) + np.where(lining, 0.0, LINING), 0.0)
    return out + r * push[:, None]


def keep_out(points, frames, head, rest, ways, lining, slim=False):
    """Rule 3 for the arms and the head together: each pushes the coat out of its way, three times over (one can push into the other)."""
    for _ in range(3):
        points = head_push(points, head, rest, lining)
        points = arms_push(points, frames, ways, lining, slim)
    return points


# 3.8.9 (the owner, of the collar crumpled behind a turned head: "the fuck is this mess behind the head"): the head no longer pushes the
# collar point by point (that folded it into spikes); the collar is a ring round the neck: for each of SECTORS directions round it, how
# far out the head reaches (its box grown by MARGIN, at the collar's heights, COLLAR_BAND) is set against how far out the collar's
# inside lies at rest in that direction, and the collar's whole column there (cloth and lining together) moves out by the difference,
# eased round the ring (a max over NEIGHBOURS sectors, then a smoothing): it widens where the head turns into it, never folds
SECTORS = 72
NEIGHBOURS = 2
COLLAR_BAND = (-3.5, 0.0)
HEAD_SAMPLES = 25


def sector_of(x, z):
    a = np.arctan2(z, x) % (2 * np.pi)
    return np.minimum((a / (2 * np.pi) * SECTORS).astype(int), SECTORS - 1)


def ring_rest(rest):
    """Per sector: how far out the collar's inside lies at rest (the least radius of the collar's points there; inf if none)."""
    collar = rest[:, 1] < COLLAR_BAND[1]
    r = np.hypot(rest[:, 0], rest[:, 2])
    s = sector_of(rest[:, 0], rest[:, 2])
    inner = np.full(SECTORS, np.inf)
    np.minimum.at(inner, s[collar], r[collar])
    return inner


def head_reach(frame):
    """Per sector: how far out the head (its box grown by MARGIN; frame: its frame -> model px) reaches at the collar's heights."""
    h = HEAD_HALF + MARGIN
    g = np.linspace(-1.0, 1.0, HEAD_SAMPLES)
    ys = np.linspace(HEAD_Y[0], HEAD_Y[1], HEAD_SAMPLES)
    pts = []
    for y in ys:
        for t in g:
            pts += [(t * h, y, -h), (t * h, y, h), (-h, y, t * h), (h, y, t * h)]
    for a in g:
        for b in g:
            pts += [(a * h, HEAD_Y[1], b * h)]
    p = (np.c_[np.array(pts), np.ones(len(pts))] @ frame.T)[:, :3]
    p = p[(p[:, 1] > COLLAR_BAND[0]) & (p[:, 1] < COLLAR_BAND[1] + MARGIN)]
    reach = np.zeros(SECTORS)
    if len(p):
        np.maximum.at(reach, sector_of(p[:, 0], p[:, 2]), np.hypot(p[:, 0], p[:, 2]))
    return reach


RING_TOLERANCE = 0.25


def ring_push(points, frame, rest, inner=None):
    """The collar (the coat's points from HEAD_REACH px below the shoulders up, fading in from there to the top of the body) set out level
    from the neck, direction by direction, at least as far as its place at rest plus how much the head reaches past the collar's inside
    there, eased round the ring (idempotent: it may alternate with the arms' push)."""
    inner = ring_rest(rest) if inner is None else inner
    need = np.where(np.isfinite(inner), np.maximum(0.0, head_reach(frame) - inner - RING_TOLERANCE), 0.0)
    wide = need.copy()
    for k in range(1, NEIGHBOURS + 1):
        wide = np.maximum(wide, np.maximum(np.roll(need, k), np.roll(need, -k)))
    eased = (np.roll(wide, 1) + 2 * wide + np.roll(wide, -1)) / 4.0
    eased = np.maximum(eased, need)
    s = sector_of(rest[:, 0], rest[:, 2])
    w = smooth(HEAD_REACH, 0.0, rest[:, 1])
    target = np.hypot(rest[:, 0], rest[:, 2]) + eased[s] * w
    now = np.hypot(points[:, 0], points[:, 2])
    grow = np.where((now > 1e-6) & (now < target), target / np.maximum(now, 1e-6), 1.0)
    out = points.copy()
    out[:, 0] *= grow
    out[:, 2] *= grow
    return out


def keep_out(points, frames, head, rest, ways, lining, slim=False):
    """Rule 3: the collar widened round the head as a ring and the arms pushing the coat out of their way, alternately, three times."""
    inner = ring_rest(rest)
    for _ in range(3):
        points = ring_push(points, head, rest, inner)
        points = arms_push(points, frames, ways, lining, slim)
    return points
