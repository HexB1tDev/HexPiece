#!/usr/bin/env python3
"""2.1.4: renders the owner's katanas off-game from the packed meshes (tools/katana_mesh.py) and their own textures: a z-buffered,
textured rasteriser (numpy) with the lighting the game will use (two directional lights and ambient, as Minecraft lights entities) and
the blade's moving highlight (client/sword/KatanaRenderer: a band of light reflected off the steel, by the view-space normal).

  python3 tools/katana_render.py icons <out dir>          the 25 inventory icons (32x32, blade up to the right as Minecraft draws swords; 2.2.6: they belong in src/main/resources/assets/hexhaki/textures/item/katana, where the item atlas stitches them)
  python3 tools/katana_render.py sheet <out.png>          all 25 blades and their saya side by side, to judge them
Both read input/Hex_Katana_Atelier_1.20.1.zip.
"""
import io
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import katana_mesh as km  # noqa: E402
from katana_names import KATANAS  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP = os.path.join(ROOT, "input", "Hex_Katana_Atelier_1.20.1.zip")


def rotation(pitch, yaw, roll):
    cx, sx, cy, sy, cz, sz = math.cos(pitch), math.sin(pitch), math.cos(yaw), math.sin(yaw), math.cos(roll), math.sin(roll)
    rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return rz @ ry @ rx


def shine(normal_view, view_dir=np.array([0.0, 0.0, 1.0])):
    """The blade's highlight (as KatanaRenderer.shine): a soft band where the steel reflects the sky above the viewer."""
    n = normal_view / np.maximum(1e-6, np.linalg.norm(normal_view, axis=-1, keepdims=True))
    r = view_dir - 2 * (n @ view_dir)[..., None] * n
    up = r[..., 1]
    return np.clip((up - 0.15) / 0.55, 0, 1) ** 2


def render(mesh, texture, rot, size, scale, center, blade_groups=("01_Blade",), shade=True, background=None):
    vertices, quads, groups = mesh
    v = np.array(vertices, float)
    pos = v[:, :3] @ rot.T
    nrm = v[:, 5:8] @ rot.T
    uv = v[:, 3:5]
    # screen: x right, y down (the model's y is up)
    sx = size / 2 + (pos[:, 0] - center[0]) * scale
    sy = size / 2 - (pos[:, 1] - center[1]) * scale
    depth = -pos[:, 2]
    tex = np.asarray(texture.convert("RGBA"), float) / 255.0
    th, tw = tex.shape[:2]
    color = np.zeros((size, size, 4))
    if background is not None:
        color[:, :, :3] = np.array(background, float) / 255.0
        color[:, :, 3] = 1.0
    zbuf = np.full((size, size), np.inf)
    blade = np.zeros(len(quads), bool)
    for name, first, count in groups:
        if any(name.startswith(b) for b in blade_groups):
            blade[first:first + count] = True
    light0 = np.array([0.2, 1.0, -0.7]) / np.linalg.norm([0.2, 1.0, -0.7])
    light1 = np.array([-0.2, 1.0, 0.7]) / np.linalg.norm([-0.2, 1.0, 0.7])
    for qi, quad in enumerate(quads):
        for tri in ((quad[0], quad[1], quad[2]), (quad[0], quad[2], quad[3])):
            if tri[1] == tri[2]:
                continue
            a, b, c = tri
            x0, y0, x1, y1, x2, y2 = sx[a], sy[a], sx[b], sy[b], sx[c], sy[c]
            area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
            if abs(area) < 1e-9:
                continue
            minx, maxx = max(int(min(x0, x1, x2)), 0), min(int(max(x0, x1, x2)) + 1, size - 1)
            miny, maxy = max(int(min(y0, y1, y2)), 0), min(int(max(y0, y1, y2)) + 1, size - 1)
            if minx > maxx or miny > maxy:
                continue
            xs, ys = np.meshgrid(np.arange(minx, maxx + 1) + 0.5, np.arange(miny, maxy + 1) + 0.5)
            w0 = ((x1 - xs) * (y2 - ys) - (x2 - xs) * (y1 - ys)) / area
            w1 = ((x2 - xs) * (y0 - ys) - (x0 - xs) * (y2 - ys)) / area
            w2 = 1 - w0 - w1
            inside = (w0 >= -1e-4) & (w1 >= -1e-4) & (w2 >= -1e-4)
            d = w0 * depth[a] + w1 * depth[b] + w2 * depth[c]
            region = (slice(miny, maxy + 1), slice(minx, maxx + 1))
            visible = inside & (d < zbuf[region])
            if not visible.any():
                continue
            u = (w0 * uv[a, 0] + w1 * uv[b, 0] + w2 * uv[c, 0]) * (tw - 1)
            vv = (w0 * uv[a, 1] + w1 * uv[b, 1] + w2 * uv[c, 1]) * (th - 1)
            texel = tex[np.clip(vv.astype(int), 0, th - 1), np.clip(u.astype(int), 0, tw - 1)]
            if texel[..., 3].max() < 0.1:
                continue
            visible &= texel[..., 3] > 0.1
            n = w0[..., None] * nrm[a] + w1[..., None] * nrm[b] + w2[..., None] * nrm[c]
            n /= np.maximum(1e-6, np.linalg.norm(n, axis=-1, keepdims=True))
            # facing away? the mesh is two-sided where it needs to be: light the side we see
            n = np.where((n[..., 2:3] < 0), -n, n) if False else n
            light = 0.4 + 0.6 * (np.clip(n @ light0, 0, 1) + np.clip(n @ light1, 0, 1)) if shade else np.ones(n.shape[:2])
            light = np.minimum(1.0, light)
            rgb = texel[..., :3] * light[..., None]
            if blade[qi]:
                rgb = np.minimum(1.0, rgb + shine(n)[..., None] * 0.75)
            out = np.concatenate([rgb, np.ones(rgb.shape[:2] + (1,))], axis=-1)
            color[region] = np.where(visible[..., None], out, color[region])
            zbuf[region] = np.where(visible, d, zbuf[region])
    return Image.fromarray((color * 255).clip(0, 255).astype(np.uint8), "RGBA")


def load(zip_path=ZIP):
    meshes = {name: km.parse(text) for name, text in km.models(zip_path)}
    textures = {name: Image.open(io.BytesIO(data)) for name, data in km.textures(zip_path).items()}
    return meshes, textures


def icon(mesh, texture, size=32):
    """Blade up to the right, the handle down to the left, a little turned so the guard reads: the way Minecraft draws a sword. Item
    art rather than a photo: the blade fills the slot's diagonal, its width drawn 1.7x so it reads at 16 to 64 pixels, a dark outline."""
    vertices, quads, groups = mesh
    widened = ([(x * 1.7, y, z * 1.7, u, v, nx, ny, nz) for x, y, z, u, v, nx, ny, nz in vertices], quads, groups)
    rot = rotation(0.0, 0.35, -math.pi / 4)
    big_size = size * 8
    big = render(widened, texture, rot, big_size, big_size * 1.1 / 28.0, (0.0, 7.1, 0.0))
    small = big.resize((size, size), Image.LANCZOS)
    a = np.asarray(small, float)
    alpha = a[..., 3] > 40
    padded = np.pad(alpha, 1)
    grown = alpha | padded[:-2, 1:-1] | padded[2:, 1:-1] | padded[1:-1, :-2] | padded[1:-1, 2:]
    edge = grown & ~alpha
    a[edge] = (24, 20, 30, 235)
    a[alpha, 3] = 255
    return Image.fromarray(a.clip(0, 255).astype(np.uint8), "RGBA")


def main():
    mode, out = sys.argv[1], sys.argv[2]
    meshes, textures = load()
    if mode == "icons":
        os.makedirs(out, exist_ok=True)
        for key, *_ in KATANAS:
            icon(meshes[key], textures[key]).save(os.path.join(out, key + "_icon.png"))
            print(key)
    else:
        tile_w, tile_h = 150, 300
        sheet = Image.new("RGB", (tile_w * 5, (tile_h + 34) * 5), (18, 22, 30))
        d = ImageDraw.Draw(sheet)
        for i, (key, name, hamon, *_rest) in enumerate(KATANAS):
            x0, y0 = (i % 5) * tile_w, (i // 5) * (tile_h + 34)
            rot = rotation(0.0, 0.25, 0.0)
            blade = render(meshes[key], textures[key], rot, tile_h, tile_h / 30.0, (0.0, 7.2, 0.0))
            saya = render(meshes[key + "_sheath"], textures[key], rot, tile_h, tile_h / 30.0, (0.0, 7.2, 0.0))
            sheet.paste(blade, (x0 + 40 - tile_h // 2, y0), blade)
            sheet.paste(saya, (x0 + 105 - tile_h // 2, y0), saya)
            d.text((x0 + 8, y0 + tile_h + 2), name, fill=(235, 235, 235))
            d.text((x0 + 8, y0 + tile_h + 16), hamon, fill=(150, 160, 175))
        sheet.save(out)


if __name__ == "__main__":
    main()
