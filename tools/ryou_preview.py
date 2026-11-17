#!/usr/bin/env python3
"""2.1.3: renders Ryou's hand rim off-game, to judge it (the house rule: judge every render yourself).

The same geometry as client/haki/RyouHands.java: the arm box bent as bendy-lib bends it (BodyFrame.bend, HexGhoul's verified port), a
hull grown RIM pixels (and three halo hulls) round the hand (the lowest 3.5 pixels of the arm and the fist end), wound inward and drawn with back faces
culled, depth tested against the arm (which writes depth), additive. 2.1.9: only the hand is coated black (fading in over the wrist),
the arm is its own sleeve and skin. A z-buffer rasteriser (numpy), orthographic.

Usage: python3 tools/ryou_preview.py out.png
"""
import math
import sys

import numpy as np
from PIL import Image, ImageDraw

PX = 1.0 / 16.0
WRIST, LIT, FIST = 6.5, 7.7, 10.0          # pixels below the shoulder pivot
COAT_TOP = 5.4                             # 2.1.9: where the hand's black coat begins to fade in up the wrist
RIM = 0.55                                 # pixels
HALOS = ((1.0, 0.13), (1.45, 0.08), (1.9, 0.05))  # pixels, strength (they add up)
DARK = 0.85


def bent(bend_axis, amount, min_x, max_x, q):
    """bendy-lib's applyBend on an arm point (pixels), as RyouHands.bent."""
    if abs(amount) < 1e-4:
        return q
    qx, qy, qz = q
    ax, az = math.cos(bend_axis), math.sin(bend_axis)
    cx, cy = (min_x + max_x) / 2.0, 4.0
    dist_a, dist_b, half = qy - 10.0, qy + 2.0, 6.0
    from_bend = -(az * (qx - cx) - ax * qz)
    shear = math.tan(amount / 2.0) * from_bend
    if abs(dist_a) < abs(dist_b):
        qy += -dist_a / half * shear
        dx, dy, dz = qx - cx, qy - cy, qz
        c, s = math.cos(amount), math.sin(amount)
        kd = ax * dx + az * dz
        rx = dx * c + (-az * dy) * s + ax * kd * (1 - c)
        ry = dy * c + (az * dx - ax * dz) * s
        rz = dz * c + (ax * dy) * s + az * kd * (1 - c)
        return (cx + rx, cy + ry, rz)
    return (qx, qy - dist_b / half * shear, qz)


def box_faces(x0, x1, y0, y1, z0, z1):
    """The six faces of a box, each [4 corners] wound counter-clockwise seen from outside, with its outward normal."""
    c = lambda x, y, z: (x, y, z)
    return [
        ([c(x0, y0, z0), c(x0, y1, z0), c(x1, y1, z0), c(x1, y0, z0)], (0, 0, -1)),
        ([c(x1, y0, z1), c(x1, y1, z1), c(x0, y1, z1), c(x0, y0, z1)], (0, 0, 1)),
        ([c(x0, y0, z1), c(x0, y1, z1), c(x0, y1, z0), c(x0, y0, z0)], (-1, 0, 0)),
        ([c(x1, y0, z0), c(x1, y1, z0), c(x1, y1, z1), c(x1, y0, z1)], (1, 0, 0)),
        ([c(x0, y0, z1), c(x0, y0, z0), c(x1, y0, z0), c(x1, y0, z1)], (0, -1, 0)),
        ([c(x0, y1, z0), c(x0, y1, z1), c(x1, y1, z1), c(x1, y1, z0)], (0, 1, 0)),
    ]


def oriented(face):
    """Make a face's corners counter-clockwise about its outward normal."""
    pts, n = face
    a, b, c = (np.array(p, float) for p in pts[:3])
    if np.dot(np.cross(b - a, c - a), n) < 0:
        pts = pts[::-1]
    return pts, n


def shell(min_x, max_x, grow):
    """The hull's faces as RyouHands.shell builds them: the four sides in a wrist band (fading) and a hand band, then the fist end."""
    x0, x1, z0, z1, y1 = min_x - grow, max_x + grow, -2 - grow, 2 + grow, FIST + grow
    out = []
    for top, bottom, lit_top in ((WRIST, LIT, 0.0), (LIT, y1, 1.0)):
        for pts, n in box_faces(x0, x1, top, bottom, z0, z1)[:4]:
            pts, n = oriented((pts, n))
            out.append((pts, n, [lit_top if p[1] == top else 1.0 for p in pts]))
    pts, n = oriented(box_faces(x0, x1, WRIST, y1, z0, z1)[5])
    out.append((pts, n, [1.0] * 4))
    return out


def rot(pitch, yaw, roll):
    """ModelPart rotation Rz Ry Rx (radians)."""
    cx, sx, cy, sy, cz, sz = math.cos(pitch), math.sin(pitch), math.cos(yaw), math.sin(yaw), math.cos(roll), math.sin(roll)
    rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return rz @ ry @ rx


class Canvas:
    def __init__(self, w, h, bg):
        self.w, self.h = w, h
        self.color = np.zeros((h, w, 3), float)
        self.color[:, :] = np.array(bg, float) / 255.0
        self.depth = np.full((h, w), np.inf)

    def tri(self, p, rgb, additive=False, intensity=None, cull_back=True, write_depth=True, blend=None):
        """p: 3 screen points (x, y, depth). Counter-clockwise on screen = front (screen y down, so the sign flips)."""
        (x0, y0, d0), (x1, y1, d1), (x2, y2, d2) = p
        area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
        if abs(area) < 1e-9 or (cull_back and area > 0):
            return
        minx, maxx = max(int(min(x0, x1, x2)), 0), min(int(max(x0, x1, x2)) + 1, self.w - 1)
        miny, maxy = max(int(min(y0, y1, y2)), 0), min(int(max(y0, y1, y2)) + 1, self.h - 1)
        if minx > maxx or miny > maxy:
            return
        xs, ys = np.meshgrid(np.arange(minx, maxx + 1) + 0.5, np.arange(miny, maxy + 1) + 0.5)
        w0 = ((x1 - xs) * (y2 - ys) - (x2 - xs) * (y1 - ys)) / area
        w1 = ((x2 - xs) * (y0 - ys) - (x0 - xs) * (y2 - ys)) / area
        w2 = 1 - w0 - w1
        inside = (w0 >= 0) & (w1 >= 0) & (w2 >= 0)
        d = w0 * d0 + w1 * d1 + w2 * d2
        region = (slice(miny, maxy + 1), slice(minx, maxx + 1))
        visible = inside & (d <= self.depth[region] + 1e-6)
        if blend is not None:
            a = (blend[0] * w0 + blend[1] * w1 + blend[2] * w2)[..., None]
            mixed = self.color[region] * (1 - a) + np.array(rgb, float)[None, None, :] * a
            self.color[region] = np.where(visible[..., None], mixed, self.color[region])
            self.depth[region] = np.where(visible, d, self.depth[region])
        elif additive:
            k = intensity[0] * w0 + intensity[1] * w1 + intensity[2] * w2
            add = np.array(rgb, float)[None, None, :] * k[..., None]
            self.color[region] = np.where(visible[..., None], np.minimum(1.0, self.color[region] + add), self.color[region])
        else:
            self.color[region] = np.where(visible[..., None], np.array(rgb, float)[None, None, :], self.color[region])
            if write_depth:
                self.depth[region] = np.where(visible, d, self.depth[region])

    def image(self):
        return Image.fromarray((self.color * 255).clip(0, 255).astype(np.uint8))


def draw_arm(canvas, view, place, bend_axis, bend, slim, right, color, pulse=1.0, scale=26.0, origin=(150, 70)):
    """One arm: posed by `place` (3x3 rotation), bent, seen through `view` (3x3 rotation); then its Ryou rim."""
    centre = (0.5 if slim else 1.0) * (-1 if right else 1)
    half = 1.5 if slim else 2.0
    min_x, max_x = centre - half, centre + half

    def project(q):
        b = bent(bend_axis, bend, min_x, max_x, q)
        w = view @ (place @ np.array(b, float))
        return (origin[0] + w[0] * scale, origin[1] + w[1] * scale, w[2])

    def light(n):
        w = view @ (place @ np.array(n, float))
        return 0.55 + 0.45 * max(0.0, -w[2])

    # 2.1.9: the arm in its own sleeve and skin; the coat is black over the hand only (the owner: "The whole arm is black instead of just
    # the hands"), fading in over the wrist from COAT_TOP
    sleeve, skin, coat = (0.86, 0.84, 0.78), (0.82, 0.62, 0.5), (0.07, 0.07, 0.09)
    fade = tuple(0.5 * (a + b) for a, b in zip(skin, coat))
    for y0, y1, base in ((-2, 4, sleeve), (4, COAT_TOP, skin), (COAT_TOP, WRIST, fade), (WRIST, 10, coat)):
        for pts, n in box_faces(min_x, max_x, y0, y1, -2, 2):
            pts, n = oriented((pts, n))
            sp = [project(p) for p in pts]
            shade = light(n)
            rgb = tuple(c * shade for c in base)
            canvas.tri([sp[0], sp[1], sp[2]], rgb)
            canvas.tri([sp[0], sp[2], sp[3]], rgb)
    # the rim's dark band: inward-wound (the corners reversed), back faces culled, alpha blended black, depth tested and written
    for pts, n, lit in shell(min_x, max_x, RIM):
        order = [3, 2, 1, 0]
        sp = [project(pts[i]) for i in order]
        k = [min(1.0, DARK * pulse * lit[i]) for i in order]
        canvas.tri([sp[0], sp[1], sp[2]], (0, 0, 0), blend=[k[0], k[1], k[2]])
        canvas.tri([sp[0], sp[2], sp[3]], (0, 0, 0), blend=[k[0], k[2], k[3]])
    # the rim's light and the halo: the same hulls, additive, depth tested, no depth written
    rgb = [((color >> 16) & 255) / 255.0, ((color >> 8) & 255) / 255.0, (color & 255) / 255.0]
    for grow, strength in ((RIM, pulse),) + tuple((g, pulse * k) for g, k in HALOS):
        for pts, n, lit in shell(min_x, max_x, grow):
            order = [3, 2, 1, 0]
            sp = [project(pts[i]) for i in order]
            k = [min(1.0, strength * lit[i]) for i in order]
            canvas.tri([sp[0], sp[1], sp[2]], rgb, additive=True, intensity=[k[0], k[1], k[2]])
            canvas.tri([sp[0], sp[2], sp[3]], rgb, additive=True, intensity=[k[0], k[2], k[3]])


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "ryou.png"
    cases = [
        ("hanging, front", rot(0, 0, 0), 0.0, 0.0, False, rot(0, 0, 0)),
        ("hanging, 3/4", rot(0, 0, 0), 0.0, 0.0, False, rot(0.25, 0.7, 0)),
        ("fist raised, elbow bent 90", rot(-1.3, 0.15, 0.1), 0.0, -1.57, False, rot(0.2, 1.2, 0)),
        ("punch out, side", rot(-1.55, 0, 0), 0.0, -0.15, False, rot(0, 1.57, 0)),
        ("slim arm, 3/4", rot(-0.6, 0.2, 0.15), 0.4, -0.9, True, rot(0.2, 0.6, 0)),
        ("first person (close)", rot(-1.4, -0.2, 0.0), 0.0, -0.3, False, rot(-0.35, 0.35, 0)),
    ]
    colors = [0xFF2B3D, 0x2AE6FF, 0x9B4BFF, 0x34FF7C, 0xFFD93A, 0x2E9BFF]
    tile_w, tile_h = 300, 300
    img = Image.new("RGB", (tile_w * len(cases), tile_h * 2 + 20), (20, 20, 20))
    d = ImageDraw.Draw(img)
    for col, (label, place, axis, amount, slim, view) in enumerate(cases):
        for row, bg in enumerate(((22, 26, 36), (178, 196, 214))):
            canvas = Canvas(tile_w, tile_h, bg)
            close = label.startswith("first")
            scale = 30.0 if close else 17.0
            hand = view @ (place @ np.array(bent(axis, amount, -2.0, 2.0, (0.0, 8.5, 0.0)), float))
            draw_arm(canvas, view, place, axis, amount, slim, True, colors[col], scale=scale,
                     origin=(tile_w / 2 - hand[0] * scale, tile_h * 0.62 - hand[1] * scale))
            img.paste(canvas.image(), (col * tile_w, 20 + row * tile_h))
        d.text((col * tile_w + 6, 4), label, fill=(230, 230, 230))
    img.save(out)


if __name__ == "__main__":
    main()
