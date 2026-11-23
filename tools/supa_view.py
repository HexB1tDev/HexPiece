#!/usr/bin/env python3
"""3.3.5: the Supa Supa no Mi's blades off-game, made of the owner's katana steel (the owner chose "Katana steel" after the first,
flat blades: "it honestly all looks really poor and poorly made"). A katana's steel alone (its 01_Blade part, the owner's own mesh and
texture, lit as the game lights it: tools/sword_view.py) reshaped for Daz Bones by the same rule client/sword/KatanaMesh.shaped uses:

  every 0.5 px band of the blade along its length: its spine (the most +x of it) and its edge (the most -x); a vertex d = spine - x in
  from the spine, u = d / (spine - edge) of the way to the edge;
  x' = spine * curve - d * widen - wave * sin(2 pi y / period) * u^2      (wider about its own spine, its curve eased, the edge rolling)
  y' = y * length,  z' = z * thick

then set on a limb (sword_rig's chain, the game's own) and drawn beside the figure. Armament's coat multiplies the steel toward the
katanas' very dark purple (62, 34, 88), its glint dimmed, as client/sword/KatanaRenderer.coated does.

Usage: python3 tools/supa_view.py out.png [--key azure_tide] [--coat 0..1] [--views front,three,side,back]
"""
import argparse
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_rig as rig  # noqa: E402
import sword_view as sv  # noqa: E402

COAT = np.array([62, 34, 88], float) / 255.0

# the shapes (client/supa/SupaBlades keeps the same numbers): widen, thick, length, wave, period, curve; then where on the limb
ARM = dict(widen=4.2, thick=2.6, length=0.82, wave=0.45, period=7.5, curve=0.4)
ARM_AT = (-1.6, 3.2, 0.0)   # the guard end on the arm (px, the arm's frame: x out, y down the arm)
ARM_TURN = 30.0             # the blade's section turned round the arm (degrees): never edge-on from the side


def shaped(key, s):
    """The katana's blade part reshaped: (verts with x y z u v nx ny nz, the blade's triangles)."""
    v, t, blade = sv.mesh(key)
    tris = t[blade]
    idx = np.unique(tris)
    x, y = v[:, 0], v[:, 1]
    band = np.floor(y / 0.5).astype(int)
    spine = {}
    edge = {}
    for i in idx:
        k = band[i]
        spine[k] = max(spine.get(k, -1e9), x[i])
        edge[k] = min(edge.get(k, 1e9), x[i])
    out = v.copy()
    for i in idx:
        k = band[i]
        sp = spine[k]
        w = max(0.05, sp - edge[k])
        d = sp - x[i]
        u = min(1.0, max(0.0, d / w))
        out[i, 0] = sp * s["curve"] - d * s["widen"] - s["wave"] * math.sin(2 * math.pi * y[i] / s["period"]) * u * u
        out[i, 1] = y[i] * s["length"]
        out[i, 2] = v[i, 2] * s["thick"]
    return out, tris


def draw_steel(canvas, cam, key, verts, tris, matrix, shine=0.7, coat=0.0):
    """sword_view.draw_katana's drawing for these verts and triangles (all steel), the coat multiplied in."""
    tex = sv.texture(key).copy()
    if coat > 0:
        tex[..., :3] *= (1.0 - coat) + coat * COAT
        shine *= 1.0 - 0.75 * coat
    p = np.c_[verts[:, :3], np.ones(len(verts))] @ matrix.T
    n = verts[:, 5:8] @ matrix[:3, :3].T
    n /= np.maximum(1e-9, np.linalg.norm(n, axis=1, keepdims=True))
    s = cam.project(p[:, :3])
    uv = verts[:, 3:5]
    tri_s = s[tris]
    tn = n[tris].mean(axis=1)
    tn /= np.maximum(1e-9, np.linalg.norm(tn, axis=1, keepdims=True))
    light = 0.45 + 0.55 * np.abs(tn @ sv.LIGHT)
    refl = tn @ (cam.rot.T @ np.array([0, 1.0, 0]))
    gleam = np.clip(np.abs(refl) - 0.35, 0, 1) ** 2 * shine * 1.6
    for t in range(len(tris)):
        canvas.tri(tri_s[t], uv=uv[tris[t]], tex=tex, shade=light[t], extra=gleam[t])


# the other blades (supa/SupaBlades: the same numbers)
CLAW = dict(widen=1.3, thick=1.8, length=0.26, wave=0.0, period=1.0, curve=1.0)
DRILL = dict(widen=1.3, thick=2.2, length=0.5, wave=0.0, period=1.0, curve=0.2)
SKATE = dict(widen=2.6, thick=2.2, length=0.46, wave=0.0, period=1.0, curve=1.0)
CLAW_Y, CLAW_X, CLAW_FAN = 9.4, (-2.5, -1.5, -0.5, 0.5), (14.0, 5.0, -5.0, -14.0)
DRILL_BLADES, DRILL_AXIS, DRILL_OUT, DRILL_LEAN, DRILL_TWIST = 6, (-1.0, 4.6, 0.0), 2.5, 11.0, 115.0
SKATE_AT = (0.0, 12.4, 3.2)
SPIN_BLADES, SPIN_Y, SPIN_OUT = 8, 7.0, 3.0
SKIN = 0.45
PARTS = [("head", (-4, -8, -4, 4, 0, 4)), ("torso", (-4, 0, -2, 4, 12, 2)), ("rightArm", (-3, -2, -2, 1, 10, 2)),
         ("leftArm", (-1, -2, -2, 3, 10, 2)), ("rightLeg", (-2, 0, -2, 2, 12, 2)), ("leftLeg", (-2, 0, -2, 2, 12, 2))]
D = math.radians


def limb(pose, part):
    """A limb's frame (px) -> E, as client/supa/SupaClient takes it (ModelPart.translateAndRotate)."""
    if part in ("rightArm", "leftArm"):
        return rig.layer_to_entity(pose) @ rig.part_matrix(pose, part, rig.ARM_PIVOT[part])
    if part in ("rightLeg", "leftLeg"):
        return rig.body_to_entity(pose) @ rig.part_matrix(pose, part, rig.LEG_PIVOT[part])
    return rig.layer_to_entity(pose) @ rig.part_matrix(pose, part, (0, 0, 0))


def arm_frame(pose, side):
    m = limb(pose, side + "Arm")
    return m if side == "right" else m @ rig.sc(-1, 1, 1)


def box_steel(key, box):
    """KatanaMesh.box: a box (px) skinned with the steel's texture: a window in the middle of the blade's uvs on every face."""
    v, t, blade = sv.mesh(key)
    uv = v[np.unique(t[blade]), 3:5]
    u0, v0 = uv.min(axis=0)
    u1, v1 = uv.max(axis=0)
    along_v = (v1 - v0) >= (u1 - u0)
    a0, a1 = (v0 + (v1 - v0) * 0.45, v0 + (v1 - v0) * 0.5) if along_v else (u0 + (u1 - u0) * 0.45, u0 + (u1 - u0) * 0.5)
    b0, b1 = (u0 + (u1 - u0) * 0.3, u0 + (u1 - u0) * 0.6) if along_v else (v0 + (v1 - v0) * 0.3, v0 + (v1 - v0) * 0.6)
    x0, y0, z0, x1, y1, z1 = box
    c = [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0), (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)]
    faces = [(0, 1, 2, 3), (5, 4, 7, 6), (4, 0, 3, 7), (1, 5, 6, 2), (4, 5, 1, 0), (3, 2, 6, 7)]
    normals = [(0, 0, -1), (0, 0, 1), (-1, 0, 0), (1, 0, 0), (0, -1, 0), (0, 1, 0)]
    corner = [(0, 0), (1, 0), (1, 1), (0, 1)]
    verts, tris = [], []
    for f, face in enumerate(faces):
        base = len(verts)
        for k, i in enumerate(face):
            s_, t_ = corner[k]
            uu = b0 + (b1 - b0) * s_ if along_v else a0 + (a1 - a0) * t_
            vv = a0 + (a1 - a0) * t_ if along_v else b0 + (b1 - b0) * s_
            verts.append(list(c[i]) + [uu, vv] + list(normals[f]))
        tris += [(base, base + 1, base + 2), (base, base + 2, base + 3)]
    return np.array(verts, float), np.array(tris, int)


def draw_parts(cv, cam, key, pose, what, coat, spin=0.0):
    """The blades client/supa/SupaClient draws for {what} (a set of: arms, claws, drills, skates, steel, spin), by its own steps."""
    cache = {}

    def steel(shape):
        name = id(shape)
        if name not in cache:
            cache[name] = shaped(key, shape)
        return cache[name]

    if "steel" in what:
        for part, b in PARTS:
            g = (b[0] - SKIN, b[1] - SKIN, b[2] - SKIN, b[3] + SKIN, b[4] + SKIN, b[5] + SKIN)
            verts, tris = box_steel(key, g)
            draw_steel(cv, cam, key, verts, tris, limb(pose, part), coat=coat)
    for side in ("right", "left"):
        m = arm_frame(pose, side)
        if "arms" in what:
            verts, tris = steel(ARM)
            draw_steel(cv, cam, key, verts, tris, m @ rig.tr(*ARM_AT) @ rig.ry(D(ARM_TURN)), coat=coat)
        if "claws" in what:
            verts, tris = steel(CLAW)
            for x, fan in zip(CLAW_X, CLAW_FAN):
                draw_steel(cv, cam, key, verts, tris, m @ rig.tr(x, CLAW_Y, 0) @ rig.rz(D(fan)), coat=coat)
        if "drills" in what:
            verts, tris = steel(DRILL)
            sp = spin if side == "right" else -spin
            for k in range(DRILL_BLADES):
                mm = m @ rig.tr(*DRILL_AXIS) @ rig.ry(D(sp + k * 360.0 / DRILL_BLADES)) @ rig.tr(-DRILL_OUT, 0, 0) \
                    @ rig.rz(D(-DRILL_LEAN)) @ rig.ry(D(DRILL_TWIST))
                draw_steel(cv, cam, key, verts, tris, mm, coat=coat)
        if "skates" in what:
            verts, tris = steel(SKATE)
            leg = limb(pose, side + "Leg")
            draw_steel(cv, cam, key, verts, tris, leg @ rig.tr(*SKATE_AT) @ rig.rx(D(-90)) @ rig.ry(D(90)), coat=coat)
    if "spin" in what:
        verts, tris = steel(ARM)
        body = limb(pose, "torso") @ rig.tr(0, SPIN_Y, 0) @ rig.ry(D(spin))
        for k in range(SPIN_BLADES):
            mm = body @ rig.ry(D(k * 360.0 / SPIN_BLADES)) @ rig.tr(-SPIN_OUT, -1.5 if k % 2 == 0 else 1.5, 0) @ rig.rz(D(-90))
            draw_steel(cv, cam, key, verts, tris, mm, coat=coat)


CLIPS = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src/main/resources/assets/hexhaki/player_animation")


def clip_pose(name, tick):
    import anim
    return anim.load(os.path.join(CLIPS, name + ".json")).pose(tick)


MOVES = [("Spar Claw (pika_sword_1)", "pika_sword_1", 3.0, {"claws"}), ("Flying Slash charging (hie_partisan_raise)", "hie_partisan_raise", 30.0,
         {"arms"}), ("Spiral Hollow (hie_grab_reach)", "hie_grab_reach", 10.0, {"drills"}),
         ("Atomic Spurt (hie_road_slide)", "hie_road_slide", 12.0, {"skates"}), ("Spider (hie_partisan_launch end)", "hie_partisan_launch", 16.0,
         {"steel"}), ("Spar Break (gura_tilt)", "gura_tilt", 3.0, {"spin", "arms"})]


def render_parts(out, key, coat, views):
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180}
    w, h = 300, 300
    rows = []
    for label, clip, tick, what in MOVES:
        pose = clip_pose(clip, tick)
        row = Image.new("RGB", (w * len(views), h), (246, 244, 238))
        for i, view in enumerate(views):
            cam = sv.Camera(yaw_of[view], 12.0, 92.0, w / 2, h * 0.88)
            cv = sv.Canvas(w, h)
            sv.draw_figure(cv, cam, pose, skip=("head", "torso", "rightArm", "leftArm", "rightLeg", "leftLeg") if "steel" in what else ())
            draw_parts(cv, cam, key, pose, what, coat, spin=25.0)
            tile = cv.image()
            ImageDraw.Draw(tile).text((4, 3), f"{label} {view}", fill=(60, 60, 60))
            row.paste(tile, (i * w, 0))
        rows.append(row)
    img = Image.new("RGB", (rows[0].width, h * len(rows)), (246, 244, 238))
    for i, r in enumerate(rows):
        img.paste(r, (0, i * h))
    img.save(out)


def arm_matrix(pose, side, at=ARM_AT):
    """Steel px -> E: on the {side} arm, the guard end at {at} (the left arm mirrored)."""
    part = side + "Arm"
    m = rig.layer_to_entity(pose) @ rig.part_matrix(pose, part, rig.ARM_PIVOT[part])
    turn = rig.ry(math.radians(ARM_TURN))
    if side == "right":
        return m @ rig.tr(*at) @ turn
    return m @ rig.tr(-at[0], at[1], at[2]) @ rig.sc(-1, 1, 1) @ turn


POSES = {
    "rest": {"rightArm": {"pitch": -8, "roll": 6}, "leftArm": {"pitch": 6, "roll": -6}},
    "forward": {"rightArm": {"pitch": -90}, "leftArm": {"pitch": -20, "roll": -12}},
    "swipe": {"rightArm": {"pitch": -85, "yaw": 40}, "leftArm": {"pitch": 10, "roll": -8}, "body": {"yaw": -20}},
}


def render(out, key, coat, views):
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180}
    w, h = 300, 300
    verts, tris = shaped(key, ARM)
    rows = []
    for name, pose in POSES.items():
        row = Image.new("RGB", (w * (len(views) + 1), h), (246, 244, 238))
        for i, view in enumerate(views):
            cam = sv.Camera(yaw_of[view], 12.0, 92.0, w / 2, h * 0.88)
            cv = sv.Canvas(w, h)
            sv.draw_figure(cv, cam, pose)
            draw_steel(cv, cam, key, verts, tris, arm_matrix(pose, "right"), coat=coat)
            draw_steel(cv, cam, key, verts, tris, arm_matrix(pose, "left"), coat=coat)
            tile = cv.image()
            ImageDraw.Draw(tile).text((4, 3), f"{name} {view}", fill=(60, 60, 60))
            row.paste(tile, (i * w, 0))
        # beside it: the same katana in a Swordsman's hand, for its steel
        sword = {"rightArm": {"pitch": -60}}
        cam = sv.Camera(35, 12.0, 92.0, w / 2, h * 0.88)
        cv = sv.Canvas(w, h)
        sv.draw_figure(cv, cam, sword)
        sv.draw_katana(cv, cam, key, rig.blade_matrix(sword))
        tile = cv.image()
        ImageDraw.Draw(tile).text((4, 3), f"the katana ({key})", fill=(60, 60, 60))
        row.paste(tile, (len(views) * w, 0))
        rows.append(row)
    img = Image.new("RGB", (rows[0].width, h * len(rows)), (246, 244, 238))
    for i, r in enumerate(rows):
        img.paste(r, (0, i * h))
    img.save(out)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("out")
    p.add_argument("--key", default="azure_tide")
    p.add_argument("--coat", type=float, default=0.0)
    p.add_argument("--views", default="front,three,side,back")
    p.add_argument("--parts", action="store_true", help="every move's blades on its own clip's pose")
    a = p.parse_args()
    if a.parts:
        render_parts(a.out, a.key, a.coat, a.views.split(","))
    else:
        render(a.out, a.key, a.coat, a.views.split(","))


if __name__ == "__main__" and "--first" not in sys.argv:
    main()


# --- first person (python3 tools/supa_view.py out.png --first): the game's own first-person right arm (client/supa/SupaClient.firstPerson:
# LightArt.FIRST_PERSON_GRIP's first eight steps, from the view to the arm's pivot), the blades on it, from the eye
FIRST_PERSON = [(0, 0.64, -0.6, -0.72), (2, 45.0), (0, -1.0, 3.6, 3.5), (3, 120.0), (1, 200.0), (2, -135.0), (0, 5.6, 0.0, 0.0),
                (0, -5.0 / 16.0, 2.0 / 16.0, 0.0)]


def first_person_frame():
    """View space (blocks: x right, y up, z back toward the eye) <- the arm's frame (px)."""
    m = np.eye(4)
    for st in FIRST_PERSON:
        if st[0] == 0:
            m = m @ rig.tr(st[1], st[2], st[3])
        elif st[0] == 1:
            m = m @ rig.rx(D(st[1]))
        elif st[0] == 2:
            m = m @ rig.ry(D(st[1]))
        else:
            m = m @ rig.rz(D(st[1]))
    return rig.tr(0, 1.62, 0) @ m @ rig.sc(1.0 / 16.0)


def render_first(out, key, views=(("arms",), ("claws",), ("drills",)), coats=(0.0, 1.0)):
    w, h = 420, 260
    img = Image.new("RGB", (w * len(views), h * len(coats)), (246, 244, 238))
    arm = first_person_frame()
    for r, coat in enumerate(coats):
        for c, what in enumerate(views):
            cam = sv.EyeCamera(w, h, pitch=0.0, fov=70.0)
            cv = sv.Canvas(w, h, bg=(150, 190, 225))
            # the arm itself
            box = np.array(rig._box(arm, -3, -2, -2, 1, 10, 2))
            s = cam.project(box)
            for face in sv.FACES:
                sq = s[list(face)]
                cv.tri(sq[[0, 1, 2]], (196, 150, 110), shade=0.9)
                cv.tri(sq[[0, 2, 3]], (196, 150, 110), shade=0.9)
            m = arm
            if "arms" in what:
                verts, tris = shaped(key, ARM)
                draw_steel(cv, cam, key, verts, tris, m @ rig.tr(*ARM_AT) @ rig.ry(D(ARM_TURN)), coat=coat)
            if "claws" in what:
                verts, tris = shaped(key, CLAW)
                for x, fan in zip(CLAW_X, CLAW_FAN):
                    draw_steel(cv, cam, key, verts, tris, m @ rig.tr(x, CLAW_Y, 0) @ rig.rz(D(fan)), coat=coat)
            if "drills" in what:
                verts, tris = shaped(key, DRILL)
                for k in range(DRILL_BLADES):
                    mm = m @ rig.tr(*DRILL_AXIS) @ rig.ry(D(20 + k * 360.0 / DRILL_BLADES)) @ rig.tr(-DRILL_OUT, 0, 0) \
                        @ rig.rz(D(-DRILL_LEAN)) @ rig.ry(D(DRILL_TWIST))
                    draw_steel(cv, cam, key, verts, tris, mm, coat=coat)
            tile = cv.image()
            d = ImageDraw.Draw(tile)
            d.line([(w / 2 - 6, h / 2), (w / 2 + 6, h / 2)], fill=(255, 255, 255))
            d.line([(w / 2, h / 2 - 6), (w / 2, h / 2 + 6)], fill=(255, 255, 255))
            d.text((4, 3), f"first person: {what[0]}{' (Armament)' if coat else ''}", fill=(20, 20, 20))
            img.paste(tile, (c * w, r * h))
    img.save(out)


if __name__ == "__main__" and "--first" in sys.argv:
    render_first(sys.argv[1], "silver_crane")
