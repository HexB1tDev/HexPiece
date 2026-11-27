#!/usr/bin/env python3
"""2.1.4: renders the Swordsman off-game: the player as Minecraft's boxes (bent as bendy-lib bends them) and the owner's katana and
saya from their own meshes and textures (tools/katana_mesh.py, the jar's packed meshes), in one depth buffer, from any side. The rig
(where each piece is) is tools/sword_rig.py's, the game's own chain. Used by tools/sword_anims.py's previews."""
import io
import math
import os
import sys
import zipfile

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import katana_mesh as km  # noqa: E402
import sword_rig as rig  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP = os.path.join(ROOT, "input", "Hex_Katana_Atelier_1.20.1.zip")
COLORS = {
    "torso": (64, 120, 196), "head": (226, 186, 150), "rightArm": (205, 92, 70), "leftArm": (240, 160, 90),
    "rightLeg": (60, 70, 140), "leftLeg": (95, 110, 190),
}
_MESHES = {}
_TEXTURES = {}


LEGENDS_ZIP = os.path.join(ROOT, "input", "Yoru_Gryphon_Models_Textures_1.20.1.zip")


def mesh(name):
    if name not in _MESHES:
        if name in km.MACES:
            # 3.8.7: the mace (art/mace, baked from the owner's First_mace.blend)
            v, q, g = km.mace_meshes()[name]
        elif name in km.LEGENDS:
            # 2.2.7: Gryphon and Yoru, from their own zip, in the katanas' frame
            v, q, g = km.legend_meshes(LEGENDS_ZIP)[name]
        else:
            with zipfile.ZipFile(ZIP) as z:
                v, q, g = km.parse(z.read(km.PREFIX + "models/obj/" + name + ".obj").decode("utf-8"))
        tris = []
        blade = []
        for gname, first, count in g:
            for quad in q[first:first + count]:
                for tri in ((quad[0], quad[1], quad[2]), (quad[0], quad[2], quad[3])):
                    if tri[1] != tri[2] and tri[0] != tri[2]:
                        tris.append(tri)
                        blade.append(gname.startswith("01_Blade"))
        verts = np.array(v, float)
        _MESHES[name] = (verts, np.array(tris, int), np.array(blade, bool))
    return _MESHES[name]


def texture(key):
    if key not in _TEXTURES and key in km.MACES:
        _TEXTURES[key] = np.asarray(Image.open(io.BytesIO(km.mace_textures()[key])).convert("RGBA"), float) / 255.0
    if key not in _TEXTURES and key in ("gryphon", "yoru"):
        _TEXTURES[key] = np.asarray(Image.open(io.BytesIO(km.legend_textures(LEGENDS_ZIP)[key])).convert("RGBA"), float) / 255.0
    if key not in _TEXTURES:
        with zipfile.ZipFile(ZIP) as z:
            _TEXTURES[key] = np.asarray(Image.open(io.BytesIO(z.read(km.PREFIX + "textures/" + key + ".png"))).convert("RGBA"),
                                        float) / 255.0
    return _TEXTURES[key]


class Camera:
    """Orthographic: turned yaw degrees round the player (0: we face them), tilted pitch degrees down, scale px per block, the
    player's feet at (cx, cy) on the image."""

    def __init__(self, yaw, pitch, scale, cx, cy):
        a, b = math.radians(yaw), math.radians(pitch)
        # E: the player faces -z; at yaw 0 we look at their front, from -z towards +z
        self.rot = rig.rx(b)[:3, :3] @ rig.ry(math.pi + a)[:3, :3]
        self.scale, self.cx, self.cy = scale, cx, cy

    def project(self, pts):
        p = np.asarray(pts, float) @ self.rot.T
        return np.stack([self.cx + p[:, 0] * self.scale, self.cy - p[:, 1] * self.scale, -p[:, 2]], axis=1)  # depth: nearer is less


# 2.3.5: the depth given to a point behind the eye (EyeCamera); orthographic depths are small, of either sign
BEHIND = -1.0e9


class EyeCamera:
    """2.3.5: first person, as playerAnimator draws a swing seen from the eyes: a perspective camera at the eye (1.62 blocks over the
    feet), looking ahead (-z) pitch degrees down, fov degrees from the top of the view to its bottom, the image w x h. Points behind
    the near plane come out at depth BEHIND (the canvas drops their triangles)."""

    NEAR = 0.05

    def __init__(self, w, h, pitch=12.0, fov=70.0, eye=1.62):
        b = math.radians(pitch)
        self.eye = np.array((0.0, eye, 0.0))
        self.right = np.array((1.0, 0.0, 0.0))
        self.up = np.array((0.0, math.cos(b), -math.sin(b)))
        self.ahead = np.array((0.0, -math.sin(b), -math.cos(b)))
        self.f = (h / 2.0) / math.tan(math.radians(fov) / 2.0)
        self.cx, self.cy = w / 2.0, h / 2.0
        # as Camera.rot: E to the view's axes (x right, y up, z towards the eye), for the blade's shine
        self.rot = np.array([self.right, self.up, -self.ahead])

    def project(self, pts):
        p = np.asarray(pts, float) - self.eye
        x, y, z = p @ self.right, p @ self.up, p @ self.ahead
        zs = np.maximum(z, self.NEAR)
        return np.stack([self.cx + self.f * x / zs, self.cy - self.f * y / zs, np.where(z < self.NEAR, BEHIND, z)], axis=1)


LIGHT = np.array([0.35, 0.85, -0.4]) / np.linalg.norm([0.35, 0.85, -0.4])


class Canvas:
    def __init__(self, w, h, bg=(246, 244, 238)):
        self.w, self.h = w, h
        self.rgb = np.zeros((h, w, 3))
        self.rgb[:, :] = np.array(bg, float) / 255.0
        self.z = np.full((h, w), np.inf)

    def tri(self, s, color=None, uv=None, tex=None, shade=1.0, extra=None):
        """One triangle (3 screen points with depth). A flat colour, or a texture sampled by uv."""
        (x0, y0, d0), (x1, y1, d1), (x2, y2, d2) = s
        area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
        if abs(area) < 1e-9 or min(d0, d1, d2) <= BEHIND:
            return
        minx, maxx = max(int(min(x0, x1, x2)), 0), min(int(max(x0, x1, x2)) + 1, self.w - 1)
        miny, maxy = max(int(min(y0, y1, y2)), 0), min(int(max(y0, y1, y2)) + 1, self.h - 1)
        if minx > maxx or miny > maxy:
            return
        xs, ys = np.meshgrid(np.arange(minx, maxx + 1) + 0.5, np.arange(miny, maxy + 1) + 0.5)
        w0 = ((x1 - xs) * (y2 - ys) - (x2 - xs) * (y1 - ys)) / area
        w1 = ((x2 - xs) * (y0 - ys) - (x0 - xs) * (y2 - ys)) / area
        w2 = 1 - w0 - w1
        inside = (w0 >= -1e-3) & (w1 >= -1e-3) & (w2 >= -1e-3)
        d = w0 * d0 + w1 * d1 + w2 * d2
        region = (slice(miny, maxy + 1), slice(minx, maxx + 1))
        vis = inside & (d < self.z[region])
        if not vis.any():
            return
        if tex is not None:
            th, tw = tex.shape[:2]
            u = (w0 * uv[0][0] + w1 * uv[1][0] + w2 * uv[2][0]) * (tw - 1)
            v = (w0 * uv[0][1] + w1 * uv[1][1] + w2 * uv[2][1]) * (th - 1)
            texel = tex[np.clip(v.astype(int), 0, th - 1), np.clip(u.astype(int), 0, tw - 1)]
            vis &= texel[..., 3] > 0.1
            out = texel[..., :3] * shade
            if extra is not None:
                out = np.minimum(1.0, out + extra)
        else:
            out = np.broadcast_to(np.array(color, float) / 255.0 * shade, xs.shape + (3,))
        self.rgb[region] = np.where(vis[..., None], out, self.rgb[region])
        self.z[region] = np.where(vis, d, self.z[region])

    def image(self):
        return Image.fromarray((self.rgb * 255).clip(0, 255).astype(np.uint8), "RGB")


FACES = ((0, 1, 3, 2), (4, 5, 7, 6), (0, 1, 5, 4), (2, 3, 7, 6), (0, 2, 6, 4), (1, 3, 7, 5))


def draw_figure(canvas, cam, pose, arm="wide", skip=()):
    for part, corners in rig.boxes(pose, arm):
        if part in skip:
            continue
        c = np.array(corners)
        s = cam.project(c)
        centre = c.mean(axis=0)
        for face in FACES:
            q = c[list(face)]
            n = np.cross(q[1] - q[0], q[2] - q[0])
            ln = np.linalg.norm(n)
            if ln < 1e-9:
                continue
            n /= ln
            if n @ (q.mean(axis=0) - centre) < 0:
                n = -n
            shade = 0.55 + 0.45 * max(0.0, n @ LIGHT)
            sq = s[list(face)]
            canvas.tri(sq[[0, 1, 2]], COLORS[part], shade=shade)
            canvas.tri(sq[[0, 2, 3]], COLORS[part], shade=shade)
        # dark edges
    return canvas


def draw_katana(canvas, cam, key, matrix, saya=False, blade=True, shine=0.7):
    """The katana (or its saya) whose model px map to E by matrix (4x4)."""
    names = ([key] if blade else []) + ([key + "_sheath"] if saya else [])
    tex = texture(key)
    for name in names:
        verts, tris, is_blade = mesh(name)
        p = np.c_[verts[:, :3], np.ones(len(verts))] @ matrix.T
        n = verts[:, 5:8] @ matrix[:3, :3].T
        n /= np.maximum(1e-9, np.linalg.norm(n, axis=1, keepdims=True))
        s = cam.project(p[:, :3])
        uv = verts[:, 3:5]
        tri_s = s[tris]
        # big triangles one by one, tiny ones as one textured splat each (fast and exact enough at preview sizes)
        ext = tri_s[:, :, :2].max(axis=1) - tri_s[:, :, :2].min(axis=1)
        small = (ext[:, 0] < 1.2) & (ext[:, 1] < 1.2)
        tn = n[tris].mean(axis=1)
        tn /= np.maximum(1e-9, np.linalg.norm(tn, axis=1, keepdims=True))
        view = cam.rot.T @ np.array([0.0, 0.0, -1.0])
        light = 0.45 + 0.55 * np.abs(tn @ LIGHT)
        refl = tn @ (cam.rot.T @ np.array([0, 1.0, 0]))
        gleam = np.where(is_blade, np.clip(np.abs(refl) - 0.35, 0, 1) ** 2 * shine * 1.6, 0.0)
        idx = np.nonzero(small)[0]
        if len(idx):
            cen = tri_s[idx].mean(axis=1)
            xs = cen[:, 0].astype(int)
            ys = cen[:, 1].astype(int)
            ok = (xs >= 0) & (xs < canvas.w) & (ys >= 0) & (ys < canvas.h) & (tri_s[idx][:, :, 2].min(axis=1) > BEHIND)
            idx, xs, ys, dep = idx[ok], xs[ok], ys[ok], cen[ok, 2]
            th, tw = tex.shape[:2]
            tuv = uv[tris[idx]].mean(axis=1)
            texel = tex[np.clip((tuv[:, 1] * (th - 1)).astype(int), 0, th - 1), np.clip((tuv[:, 0] * (tw - 1)).astype(int), 0, tw - 1)]
            keep = texel[:, 3] > 0.1
            idx, xs, ys, dep, texel = idx[keep], xs[keep], ys[keep], dep[keep], texel[keep]
        if len(idx):
            col = np.minimum(1.0, texel[:, :3] * light[idx, None] + gleam[idx, None])
            order = np.lexsort((dep, ys * canvas.w + xs))
            pix = (ys * canvas.w + xs)[order]
            first = np.r_[True, pix[1:] != pix[:-1]]
            sel = order[first]
            front = dep[sel] < canvas.z[ys[sel], xs[sel]]
            sel = sel[front]
            canvas.rgb[ys[sel], xs[sel]] = col[sel]
            canvas.z[ys[sel], xs[sel]] = dep[sel]
        for t in np.nonzero(~small)[0]:
            canvas.tri(tri_s[t], uv=uv[tris[t]], tex=tex, shade=light[t], extra=gleam[t])
    return canvas


def draw_ropes(canvas, cam, pose):
    """2.2.7: Yoru's two ropes (sword/BackCarry's, rebuilt by sword_rig.back_ropes), on the upper body, in hemp."""
    m = rig.layer_to_entity(pose) @ rig.part_matrix(pose, "torso", (0.0, 0.0, 0.0))
    for quad, colour in rig.back_ropes():
        p = np.c_[np.array(quad), np.ones(4)] @ m.T
        s = cam.project(p[:, :3])
        n = np.cross(p[1, :3] - p[0, :3], p[2, :3] - p[0, :3])
        ln = np.linalg.norm(n)
        shade = 0.6 + 0.4 * (abs(n @ LIGHT) / ln if ln > 1e-12 else 0.5)
        canvas.tri(s[[0, 1, 2]], colour, shade=shade)
        canvas.tri(s[[0, 2, 3]], colour, shade=shade)
    return canvas


def render(pose, key="crimson_dawn", drawn=True, views=("front", "three", "side", "back"), size=(300, 280), scale=82.0,
           pitch=12.0, arm="wide", label=None):
    """One pose from several sides: a row of tiles."""
    yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180, "left": -90, "threeleft": -35, "top": 0}
    w, h = size
    img = Image.new("RGB", (w * len(views), h), (246, 244, 238))
    for i, view in enumerate(views):
        cam = Camera(yaw_of[view], 80.0 if view == "top" else pitch, scale, w / 2, h * 0.86)
        cv = Canvas(w, h)
        draw_figure(cv, cam, pose, arm)
        if drawn:
            draw_katana(cv, cam, key, rig.blade_matrix(pose, arm))
            draw_katana(cv, cam, key, rig.saya_matrix(pose), saya=True, blade=False)
        else:
            draw_katana(cv, cam, key, rig.saya_matrix(pose), saya=True, blade=True, shine=0.3)
        tile = cv.image()
        d = ImageDraw.Draw(tile)
        d.line([(0, h * 0.86), (w, h * 0.86)], fill=(190, 190, 190))
        d.text((4, 3), view if label is None or i else f"{label}  {view}", fill=(60, 60, 60))
        img.paste(tile, (i * w, 0))
    return img
