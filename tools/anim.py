#!/usr/bin/env python3
"""playerAnimator (emotecraft v3) animations: load, sample and render them on a Minecraft player figure.

Used by tools/anim_preview.py (contact sheets to judge an animation) and tools/anim_polish.py (the 2.1.1 rework). The sampling follows
playerAnimator's KeyframeAnimationPlayer: per part and channel, the keyframes are interpolated with the easing written on the keyframe
being moved towards ("easeBeforeKeyframe": true); before the first keyframe a channel eases from its default (0); after the last one it
holds until endTick, then returns to the default by stopTick. Angles are degrees in the JSON ("degrees": true).
"""
import json
import math

PARTS = ("body", "torso", "head", "rightArm", "leftArm", "rightLeg", "leftLeg", "rightItem", "leftItem")
CHANNELS = {
    "body": ("x", "y", "z", "pitch", "yaw", "roll", "bend", "axis"),
    "torso": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "head": ("x", "y", "z", "pitch", "yaw", "roll"),
    "rightArm": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "leftArm": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "rightLeg": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "leftLeg": ("x", "y", "z", "pitch", "yaw", "roll", "bend"),
    "rightItem": ("x", "y", "z", "pitch", "yaw", "roll"),
    "leftItem": ("x", "y", "z", "pitch", "yaw", "roll"),
}
ANGLES = ("pitch", "yaw", "roll", "bend")


def _back(t, s=1.70158):
    return t * t * ((s + 1) * t - s)


def _bounce_out(t):
    n, d = 7.5625, 2.75
    if t < 1 / d:
        return n * t * t
    if t < 2 / d:
        t -= 1.5 / d
        return n * t * t + 0.75
    if t < 2.5 / d:
        t -= 2.25 / d
        return n * t * t + 0.9375
    t -= 2.625 / d
    return n * t * t + 0.984375


def _elastic_out(t):
    if t in (0, 1):
        return t
    return 2 ** (-10 * t) * math.sin((t * 10 - 0.75) * (2 * math.pi / 3)) + 1


BASE = {
    "linear": lambda t: t,
    "sine": lambda t: 1 - math.cos(t * math.pi / 2),
    "quad": lambda t: t * t,
    "cubic": lambda t: t ** 3,
    "quart": lambda t: t ** 4,
    "quint": lambda t: t ** 5,
    "expo": lambda t: 0 if t == 0 else 2 ** (10 * t - 10),
    "circ": lambda t: 1 - math.sqrt(max(0.0, 1 - t * t)),
    "back": _back,
    "elastic": lambda t: 1 - _elastic_out(1 - t),
    "bounce": lambda t: 1 - _bounce_out(1 - t),
}


def ease(name, t):
    name = (name or "linear").lower()
    if name == "constant":
        return 0.0 if t < 1 else 1.0
    if name == "linear":
        return t
    for prefix in ("inout", "in", "out"):
        if name.startswith(prefix) and name[len(prefix):] in BASE:
            f = BASE[name[len(prefix):]]
            if prefix == "in":
                return f(t)
            if prefix == "out":
                return 1 - f(1 - t)
            return f(2 * t) / 2 if t < 0.5 else 1 - f(2 - 2 * t) / 2
    return t


class Anim:
    """An animation as tracks: tracks[(part, channel)] = sorted [(tick, value, easing)]."""

    def __init__(self, data):
        self.data = data
        emote = data["emote"]
        self.begin = emote.get("beginTick", 0)
        self.end = emote.get("endTick", 0)
        self.stop = emote.get("stopTick", self.end)
        self.loop = emote.get("isLoop", False)
        self.ret = emote.get("returnTick", 0)
        self.degrees = emote.get("degrees", True)
        self.tracks = {}
        for move in emote.get("moves", []):
            tick = move.get("tick", 0)
            easing = move.get("easing", "linear")
            for part in PARTS:
                if part in move:
                    for channel, value in move[part].items():
                        if channel in CHANNELS[part]:
                            self.tracks.setdefault((part, channel), []).append((tick, float(value), easing))
        for key in self.tracks:
            # later moves at the same tick replace earlier ones, as playerAnimator does
            merged = {}
            for tick, value, easing in self.tracks[key]:
                merged[tick] = (tick, value, easing)
            self.tracks[key] = sorted(merged.values())

    def value(self, part, channel, tick):
        frames = self.tracks.get((part, channel))
        if not frames:
            return 0.0
        if tick <= frames[0][0]:
            first = frames[0]
            if first[0] <= self.begin:
                return first[1]
            t = (tick - self.begin) / max(1e-9, first[0] - self.begin)
            return first[1] * ease(first[2], max(0.0, t))
        for a, b in zip(frames, frames[1:]):
            if a[0] <= tick <= b[0]:
                span = b[0] - a[0]
                t = 0.0 if span == 0 else (tick - a[0]) / span
                return a[1] + (b[1] - a[1]) * ease(b[2], t)
        last = frames[-1]
        if self.loop or tick <= max(self.end, last[0]):
            return last[1]
        hold = max(self.end, last[0])
        if self.stop <= hold:
            return last[1]
        t = min(1.0, (tick - hold) / (self.stop - hold))
        return last[1] * (1 - ease("inoutsine", t))

    def pose(self, tick):
        return {part: {ch: self.value(part, ch, tick) for ch in CHANNELS[part]} for part in PARTS}

    def length(self):
        return max(self.stop, self.end, max((f[-1][0] for f in self.tracks.values()), default=0))


def load(path):
    with open(path, encoding="utf-8") as handle:
        return Anim(json.load(handle))


# ---------------------------------------------------------------- the figure (Minecraft player model, model units, y down)

def rot_x(a):
    c, s = math.cos(a), math.sin(a)
    return ((1, 0, 0), (0, c, -s), (0, s, c))


def rot_y(a):
    c, s = math.cos(a), math.sin(a)
    return ((c, 0, s), (0, 1, 0), (-s, 0, c))


def rot_z(a):
    c, s = math.cos(a), math.sin(a)
    return ((c, -s, 0), (s, c, 0), (0, 0, 1))


def mul(a, b):
    return tuple(tuple(sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)) for i in range(3))


def apply(m, v):
    return tuple(sum(m[i][k] * v[k] for k in range(3)) for i in range(3))


def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


def rot_axis(angle, axis):
    """Rodrigues: a turn by angle about a unit axis."""
    x, y, z = axis
    c, s, t = math.cos(angle), math.sin(angle), 1 - math.cos(angle)
    return ((c + x * x * t, x * y * t - z * s, x * z * t + y * s),
            (y * x * t + z * s, c + y * y * t, y * z * t - x * s),
            (z * x * t - y * s, z * y * t + x * s, c + z * z * t))


def zyx(pitch, yaw, roll):
    return mul(mul(rot_z(math.radians(roll)), rot_y(math.radians(yaw))), rot_x(math.radians(pitch)))


# part -> (pivot, limb start, limb end, thickness) in model units; the limb runs from start to end in the part's own frame
LIMBS = {
    "rightArm": ((-5, 2, 0), (-1, -1, 0), (-1, 10, 0), 4),
    "leftArm": ((5, 2, 0), (1, -1, 0), (1, 10, 0), 4),
    "rightLeg": ((-1.9, 12, 0), (0, 0, 0), (0, 12, 0), 4),
    "leftLeg": ((1.9, 12, 0), (0, 0, 0), (0, 12, 0), 4),
}


def skeleton(pose):
    """World-ish points (model units, y down, z forward = -z in MC) of every part for one pose."""
    body = pose["body"]
    # playerAnimator turns the whole body in the renderer's space (y up, before the model's scale(-1, -1, 1)) about a point 0.7 blocks
    # above the feet; in model space (x and y flipped) that is pitch and yaw negated, roll kept, x and y offsets negated.
    whole = zyx(-body["pitch"], -body["yaw"], body["roll"])
    pivot_y = 24 - 0.7 * 16

    def world(p):
        rel = (p[0], p[1] - pivot_y, p[2])
        r = apply(whole, rel)
        return (r[0] - body["x"] * 16, r[1] + pivot_y - body["y"] * 16, r[2] + body["z"] * 16)

    # PlayerAnimator 1.0.2 + bendy-lib 4.0.0 (verified in HexGhoul's RigPose): a torso rotation or torso bend moves the torso mesh
    # alone (head and arms stay where they are and detach); the "body" bend bends the torso mesh and turns the head and both arms with
    # it about the mid-torso (6 model pixels below the neck), about the axis (cos(-axis), 0, sin(-axis)); + leans forward.
    upper = rot_axis(math.radians(body.get("bend", 0.0)), (math.cos(-math.radians(body.get("axis", 0.0))), 0.0,
                                                          math.sin(-math.radians(body.get("axis", 0.0)))))
    mid = (0.0, 6.0, 0.0)

    def carried(p):
        return add(apply(upper, (p[0] - mid[0], p[1] - mid[1], p[2] - mid[2])), mid)

    torso = pose["torso"]
    tm = zyx(torso["pitch"], torso["yaw"], torso["roll"])
    tpivot = (torso["x"], torso["y"], torso["z"])
    tb = rot_x(math.radians(torso["bend"]))
    out = {}
    corners = [(x, y, z) for y in (0, 6, 12) for z in (-2, 2) for x in (-4, 4)]
    torso_pts = []
    for c in corners:
        p = c
        if c[1] < 6:
            p = add(apply(tb, (c[0], c[1] - 6, c[2])), mid)
            p = carried(p)
        torso_pts.append(world(add(apply(tm, p), tpivot)))
    out["torso"] = torso_pts
    out["torso_top"] = world(add(apply(tm, carried(add(apply(tb, (0, -6, 0)), mid))), tpivot))
    out["torso_bottom"] = world(add(apply(tm, (0, 12, 0)), tpivot))
    out["shoulders"] = [world(add(apply(tm, carried(add(apply(tb, (sx, -4, 0)), mid))), tpivot)) for sx in (-5, 5)]
    head = pose["head"]
    hm = zyx(head["pitch"], head["yaw"], head["roll"])
    hp = (head["x"], head["y"], head["z"])
    hc = [(x, y, z) for x in (-4, 4) for y in (-8, 0) for z in (-4, 4)]
    out["head"] = [world(carried(add(apply(hm, c), hp))) for c in hc]
    out["head_frame"] = (hm, hp)
    out["face"] = world(carried(add(apply(hm, (0, -4, -5)), hp)))
    out["neck"] = world(carried(hp))
    out["hips"] = [world(add(LIMBS[leg][0], (pose[leg]["x"], pose[leg]["y"], pose[leg]["z"]))) for leg in ("rightLeg", "leftLeg")]
    out["upper"] = (upper, mid)
    # the cuboids as Minecraft draws them, for draw(): corners in (x, y, z) nested order (index 4 ix + 2 iy + iz)
    boxes = [("head", out["head"])]
    for level in (0, 1):
        boxes.append(("torso", [torso_pts[(level + iy) * 4 + iz * 2 + ix] for ix in (0, 1) for iy in (0, 1) for iz in (0, 1)]))
    for part, (pivot, start, end, _) in LIMBS.items():
        p = pose[part]
        m = zyx(p["pitch"], p["yaw"], p["roll"])
        piv = add(pivot, (p["x"], p["y"], p["z"]))
        midp = ((start[0] + end[0]) / 2, (start[1] + end[1]) / 2, (start[2] + end[2]) / 2)
        bend = rot_x(math.radians(p.get("bend", 0.0)))
        lower = add(apply(bend, (end[0] - midp[0], end[1] - midp[1], end[2] - midp[2])), midp)
        pts = [add(apply(m, q), piv) for q in (start, midp, lower)]

        def placed(q, bent):
            if bent:
                q = add(apply(bend, (q[0] - midp[0], q[1] - midp[1], q[2] - midp[2])), midp)
            q = add(apply(m, q), piv)
            return world(carried(q) if part.endswith("Arm") else q)

        top = start[1] - 1 if part.endswith("Arm") else start[1]  # an arm's box starts 2 px above its pivot, a leg's at it
        for y0, y1, bent in ((top, midp[1], False), (midp[1], end[1], True)):
            boxes.append((part, [placed((start[0] + dx, y, dz), bent) for dx in (-2, 2) for y in (y0, y1) for dz in (-2, 2)]))
        if part.endswith("Arm"):
            pts = [carried(q) for q in pts]
            out[part + "_pivot"] = world(carried(piv))
        out[part] = [world(q) for q in pts]
    out["boxes"] = boxes
    return out


def hull(points):
    """Convex hull (monotone chain) of 2D points, for drawing a projected box."""
    pts = sorted(set((round(x, 3), round(y, 3)) for x, y in points))
    if len(pts) <= 2:
        return pts

    def cross(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])

    lower, upper = [], []
    for p in pts:
        while len(lower) >= 2 and cross(lower[-2], lower[-1], p) <= 0:
            lower.pop()
        lower.append(p)
    for p in reversed(pts):
        while len(upper) >= 2 and cross(upper[-2], upper[-1], p) <= 0:
            upper.pop()
        upper.append(p)
    return lower[:-1] + upper[:-1]


COLORS = {
    "torso": (64, 120, 196),
    "head": (226, 186, 150),
    "rightArm": (205, 92, 70),
    "leftArm": (240, 160, 90),
    "rightLeg": (60, 70, 140),
    "leftLeg": (95, 110, 190),
}


# the camera's turn round the player (degrees): 0 we face them, 90 they face right, 180 we see their back
VIEWS = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180}
# a box's faces as corner indices (4 ix + 2 iy + iz), with the axis and side each faces
FACES = (((0, 1, 3, 2), 0, -1), ((4, 5, 7, 6), 0, 1), ((0, 1, 5, 4), 1, -1), ((2, 3, 7, 6), 1, 1), ((0, 2, 6, 4), 2, -1), ((1, 3, 7, 5), 2, 1))


def draw(draw_ctx, pose, origin, scale, view):
    """The player as Minecraft draws it: every part a box (the torso and each limb in two halves, as bendy-lib bends them), the faces
    turned away culled and the rest painted far to near, so a forearm folded behind the back is hidden behind the torso. view: 'front'
    (we face the player), 'three' (3/4), 'side' (the player faces right), 'threeback' (3/4 from behind), 'back'."""
    sk = skeleton(pose)
    a = math.radians(VIEWS.get(view, 35))

    def project(p):
        x, y, z = p
        return (origin[0] + (x * math.cos(a) - z * math.sin(a)) * scale, origin[1] + y * scale), x * math.sin(a) + z * math.cos(a)

    faces = []
    for part, corners in sk["boxes"]:
        projected = [project(c) for c in corners]
        centre = sum(d for _, d in projected) / 8
        colour = COLORS[part]
        for indices, axis, side in FACES:
            depth = sum(projected[i][1] for i in indices) / 4
            if depth > centre - 1e-6:
                continue  # turned away from us
            # a little light from above and the front: tops brightest, sides darker
            shade = {1: 0.82 if side > 0 else 1.12, 0: 0.86, 2: 0.94}[axis]
            faces.append((depth, [projected[i][0] for i in indices], tuple(min(255, int(c * shade)) for c in colour)))
    for depth, polygon, colour in sorted(faces, key=lambda f: -f[0]):
        draw_ctx.polygon(polygon, fill=colour, outline=(20, 20, 20))
    head = sk["head"]
    head_centre = sum(project(c)[1] for c in head) / 8
    eye, eye_depth = project(sk["face"])
    if eye_depth < head_centre:
        r = 1.2 * scale
        draw_ctx.ellipse([eye[0] - r, eye[1] - r, eye[0] + r, eye[1] + r], fill=(40, 30, 30))


def dist(a, b):
    return math.sqrt(sum((a[i] - b[i]) ** 2 for i in range(3)))


def inside_head(point, sk, pose, margin=0.6):
    """Is a world point inside the head box (shrunk by margin)?"""
    hm, hp = sk["head_frame"]
    upper, mid = sk["upper"]
    # undo the world transform approximately: the head box in world space is the hull of its 8 corners; test against its axes
    corners = sk["head"]
    centre = tuple(sum(c[i] for c in corners) / 8 for i in range(3))
    axes = []
    for a, b in ((0, 4), (0, 2), (0, 1)):  # x, y, z edges of the corner order (x, y, z nested)
        e = tuple(corners[b][i] - corners[a][i] for i in range(3))
        length = math.sqrt(sum(v * v for v in e))
        axes.append((tuple(v / length for v in e), length / 2 - margin))
    rel = tuple(point[i] - centre[i] for i in range(3))
    return all(abs(sum(rel[i] * ax[i] for i in range(3))) < half for ax, half in axes)


def lint(a, step=0.5):
    """Problems a viewer would see, over the whole animation: [(tick, what)]. Model units (1/16 block)."""
    problems = []
    tick = 0.0
    while tick <= a.length() + 1e-9:
        pose = a.pose(tick)
        sk = skeleton(pose)
        neck = dist(sk["torso_top"], sk["neck"])
        if neck > 2.2:
            problems.append((tick, f"head off the neck by {neck:.1f}px"))
        hips = (tuple((sk["hips"][0][i] + sk["hips"][1][i]) / 2 for i in range(3)))
        hip_gap = dist(sk["torso_bottom"], hips)
        if hip_gap > 2.6:
            problems.append((tick, f"torso off the hips by {hip_gap:.1f}px"))
        for side, shoulder in (("rightArm", sk["shoulders"][0]), ("leftArm", sk["shoulders"][1])):
            gap = dist(sk[side + "_pivot"], shoulder)
            if gap > 2.6:
                problems.append((tick, f"{side} off the shoulder by {gap:.1f}px"))
            elbow, hand = sk[side][1], sk[side][2]
            for k in range(5):
                q = tuple(elbow[i] + (hand[i] - elbow[i]) * k / 4 for i in range(3))
                if inside_head(q, sk, pose):
                    problems.append((tick, f"{side} through the head"))
                    break
        tick += step
    return problems


# 3.3.1 (the owner, circling a fist drawn on the face from the 3/4 view that lint() passed: "how the FUCK is that a pass"): lint() only
# asks whether a forearm goes through the head in 3D; a fist hovering a pixel beside the head still reads as a fist in the face. This
# asks what the picture shows: in each view, does a forearm's box cover the head's outline (from the side or behind, its face half)
# while not hidden behind the head?
FACE_VIEWS = ("front", "three", "side", "threeback", "back")
# the views a face must be clear in: from the front, both three-quarters and both sides (the other side's through VIEWS' negatives)
FACE_CLEAR = ("front", "three", "threeleft", "side", "sideleft")
VIEWS.update({"threeleft": -35, "sideleft": -90})


def _overlap(p, q):
    """Do two convex 2D polygons overlap (separating axis test, a hair's tolerance)?"""
    for poly in (p, q):
        for i in range(len(poly)):
            a, b = poly[i], poly[(i + 1) % len(poly)]
            nx, ny = b[1] - a[1], a[0] - b[0]
            pa = [nx * x + ny * y for x, y in p]
            qa = [nx * x + ny * y for x, y in q]
            if max(pa) <= min(qa) + 1e-6 or max(qa) <= min(pa) + 1e-6:
                return False
    return True


def face_covered(pose, views=FACE_CLEAR, margin=-0.5):
    """[(view, side)] where a hand (the last two thirds of the forearm's box) is drawn over the face (the head's front half, its
    outline grown by {margin} px: shrunk a little, so only real cover counts) and in front of the head. An arm beside or behind the
    head is fine, as anyone's raised arm is; a fist on the face is not."""
    sk = skeleton(pose)
    out = []
    hands = {}
    for part, corners in sk["boxes"]:
        if part.endswith("Arm"):
            # the forearm's corners are dx, y, dz nested: y index 0 at the elbow, 1 at the hand's end
            hand = []
            for i, c in enumerate(corners):
                if (i // 2) % 2 == 1:
                    elbow = corners[i - 2]
                    hand.append(c)
                    hand.append(tuple(c[k] + (elbow[k] - c[k]) * 0.66 for k in range(3)))
            hands[part] = hand
    head = sk["head"]
    # the face half: the front corners (z index 0) and the head's mid-depth
    face = []
    for i in range(8):
        face.append(head[i] if i % 2 == 0 else tuple((head[i][k] + head[i - 1][k]) / 2 for k in range(3)))
    for view in views:
        a = math.radians(VIEWS[view])

        def project(p):
            return (p[0] * math.cos(a) - p[2] * math.sin(a), p[1]), p[0] * math.sin(a) + p[2] * math.cos(a)

        pf = [project(c) for c in face]
        fx = sum(q[0][0] for q in pf) / 8
        fy = sum(q[0][1] for q in pf) / 8
        grow = 1 + margin / 4
        face_hull = hull([(fx + (q[0][0] - fx) * grow, fy + (q[0][1] - fy) * grow) for q in pf])
        head_depth = sum(project(c)[1] for c in head) / 8
        for side, hand in hands.items():
            ph = [project(c) for c in hand]
            if sum(q[1] for q in ph) / len(ph) > head_depth:
                continue  # behind the head: drawn under it
            if _overlap(hull([q[0] for q in ph]), face_hull):
                out.append((view, side))
    return out
