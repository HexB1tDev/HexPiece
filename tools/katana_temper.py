#!/usr/bin/env python3
"""2.5.6 (the owner: "improve the temper on all swords but yoru, the blade is too bland not good looking at all, simply improve the
texture thats it, same for Gryphon"): every katana's and Gryphon's blade texture repainted with a real temper (hamon), derived from
the owner's own atlas, which is never changed: the tempered atlases are written to textures/tempered/ (with manifest.json: the
SHA-256 of each source atlas), and build.py packs them in place of the originals while those hashes match (a changed source atlas
fails the build until this is run again). Yoru is left as it is.

How: each blade triangle of the owner's mesh is rasterised into its texture, so every blade texel knows where it lies on the blade:
how far along (y, model px), how far across from the edge to the spine (0..1), and which face (the flat, the spine, the edge bevel).
From the owner's own texture it reads, row by row along the blade, where its hardened edge ends: the owner draws it as a thin bright
outline, found as the first thin bright stroke from the edge (each blade keeps its own pattern: zigzag, waves, square teeth, straight).
Gryphon has none painted: it gets irregular rounded waves (gunome). Then it paints, per texel:
  - the body (ji): the owner's painting, polished lighter toward the temper line, with the folded steel's grain (hada) flowing
    along the blade like wood grain;
  - the hardened edge (hasaki): frosted, misty white over the tint, cloud-like;
  - the temper line (nioiguchi): the owner's line, smoothed and drawn as a soft bright glow, with sparkling crystals (nie) along it
    and thin streaks (ashi) running from it toward the edge;
  - the edge bevel brightened as a honed edge; the spine as the owner painted it.

Usage: python3 tools/katana_temper.py [--preview OUT_DIR]      (numpy and PIL; run locally, the results are committed)
"""
import hashlib
import io
import json
import math
import os
import sys
import zipfile

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import katana_mesh as km  # noqa: E402
import sword_view as sv  # noqa: E402
from katana_names import KATANAS  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "textures", "tempered")
KATANA_ZIP = os.path.join(ROOT, "input", "Hex_Katana_Atelier_1.20.1.zip")
LEGENDS_ZIP = os.path.join(ROOT, "input", "Yoru_Gryphon_Models_Textures_1.20.1.zip")


def source(key):
    """The owner's atlas for key (png bytes)."""
    if key == "gryphon":
        return km.legend_textures(LEGENDS_ZIP)["gryphon"]
    with zipfile.ZipFile(KATANA_ZIP) as z:
        return z.read(km.PREFIX + "textures/" + key + ".png")


def raster(key, size):
    """Per texel of the blade: model position (x, y, z), normal; NaN off the blade."""
    verts, tris, blade = sv.mesh(key)
    h, w = size
    pos = np.full((h, w, 3), np.nan)
    nrm = np.full((h, w, 3), np.nan)
    uv = verts[:, 3:5] * np.array([w - 1, h - 1])
    for t in tris[blade]:
        p = uv[t]
        x0, y0 = np.floor(p.min(axis=0)).astype(int)
        x1, y1 = np.ceil(p.max(axis=0)).astype(int)
        x0, y0 = max(x0, 0), max(y0, 0)
        x1, y1 = min(x1, w - 1), min(y1, h - 1)
        if x1 < x0 or y1 < y0:
            continue
        xs, ys = np.meshgrid(np.arange(x0, x1 + 1), np.arange(y0, y1 + 1))
        (ax, ay), (bx, by), (cx, cy) = p
        area = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay)
        if abs(area) < 1e-9:
            continue
        w0 = ((bx - xs) * (cy - ys) - (cx - xs) * (by - ys)) / area
        w1 = ((cx - xs) * (ay - ys) - (ax - xs) * (cy - ys)) / area
        w2 = 1 - w0 - w1
        inside = (w0 >= -0.02) & (w1 >= -0.02) & (w2 >= -0.02)
        if not inside.any():
            continue
        v = verts[t]
        for out, cols in ((pos, slice(0, 3)), (nrm, slice(5, 8))):
            val = w0[..., None] * v[0, cols] + w1[..., None] * v[1, cols] + w2[..., None] * v[2, cols]
            region = out[y0:y1 + 1, x0:x1 + 1]
            region[inside] = val[inside]
    return pos, nrm


def cross_section(key):
    """Per model y (0.25 px bins): the edge's x, the spine's x, the ridge's x (where the blade is thickest)."""
    verts, tris, blade = sv.mesh(key)
    p = verts[np.unique(tris[blade].ravel()), :3]
    bins = np.arange(0.0, p[:, 1].max() + 0.5, 0.25)
    edge, spine, ridge = [], [], []
    for b in bins:
        near = p[np.abs(p[:, 1] - b) < 0.6]
        if len(near) < 3:
            edge.append(np.nan), spine.append(np.nan), ridge.append(np.nan)
            continue
        edge.append(near[:, 0].min())
        spine.append(near[:, 0].max())
        ridge.append(near[np.argmax(np.abs(near[:, 2])), 0])
    fill = lambda a: np.interp(np.arange(len(a)), np.flatnonzero(~np.isnan(a)), np.array(a)[~np.isnan(a)])
    return bins, fill(np.array(edge)), fill(np.array(spine)), fill(np.array(ridge))


def noise(shape, scale, seed, stretch=(1.0, 1.0)):
    """Smooth value noise 0..1 (bilinear over a random lattice), stretched (rows, cols)."""
    rng = np.random.default_rng(seed)
    h, w = shape
    gh, gw = int(h / (scale * stretch[0])) + 3, int(w / (scale * stretch[1])) + 3
    g = rng.random((gh, gw))
    ys = np.arange(h) / (scale * stretch[0])
    xs = np.arange(w) / (scale * stretch[1])
    y0, x0 = np.floor(ys).astype(int), np.floor(xs).astype(int)
    fy, fx = ys - y0, xs - x0
    fy, fx = fy * fy * (3 - 2 * fy), fx * fx * (3 - 2 * fx)
    a = g[y0][:, x0]
    b = g[y0][:, x0 + 1]
    c = g[y0 + 1][:, x0]
    d = g[y0 + 1][:, x0 + 1]
    top = a + (b - a) * fx[None, :]
    bot = c + (d - c) * fx[None, :]
    return top + (bot - top) * fy[:, None]


def hamon_line(lum, side, across, rows):
    """Per texture row, how far across the face (0 the edge) the owner's hamon outline first crosses it (NaN where none is seen)."""
    h, w = lum.shape
    pad = np.pad(lum, 3, mode="constant", constant_values=np.nan)
    ridge = np.full((h, w), -1.0)
    for dy, dx in ((0, 1), (1, 0), (1, 1), (1, -1)):
        for k in (2, 3):
            a = pad[3 + dy * k:3 + dy * k + h, 3 + dx * k:3 + dx * k + w]
            b = pad[3 - dy * k:3 - dy * k + h, 3 - dx * k:3 - dx * k + w]
            r = lum - np.fmax(a, b)
            ridge = np.fmax(ridge, np.nan_to_num(r, nan=-1.0))
    ridge = np.where(side, ridge, -1.0)
    contrast = np.percentile(ridge[side], 99.0)
    line = np.full(h, np.nan)
    for r in rows:
        xs = np.flatnonzero(side[r])
        order = xs[np.argsort(across[r, xs])]
        a = across[r, order]
        v = ridge[r, order]
        hit = np.flatnonzero((a >= 0.035) & (a <= 0.66) & (v > 0.35 * contrast))
        if len(hit):
            line[r] = a[hit[0]]
    return line


def temper(key):
    """The tempered atlas for key (an RGBA uint8 array) and its source's SHA-256."""
    raw = source(key)
    img = np.asarray(Image.open(io.BytesIO(raw)).convert("RGBA")).astype(float) / 255.0
    h, w = img.shape[:2]
    pos, nrm = raster(key, (h, w))
    on = ~np.isnan(pos[..., 0])
    bins, edge, spine, ridge = cross_section(key)
    y = np.where(on, pos[..., 1], 0.0)
    bi = np.clip(np.round(y / 0.25).astype(int), 0, len(bins) - 1)
    geo = np.clip(np.where(on, (pos[..., 0] - edge[bi]) / np.maximum(spine[bi] - edge[bi], 1e-3), 0.0), 0.0, 1.0)
    side = on & (np.abs(np.nan_to_num(nrm[..., 2])) > 0.55)
    spine_face = on & ~side & (np.nan_to_num(nrm[..., 0]) > 0.3)
    bevel = on & ~side & ~spine_face
    rgb = img[..., :3]

    # across each flat face in the texture itself: per texture row, from the face's first texel to its last, turned so 0 is the edge
    # side (the mesh says which way the edge lies); the owner's painting is laid out that way
    across = np.zeros((h, w))
    rows = np.flatnonzero(side.any(axis=1))
    for r in rows:
        xs = np.flatnonzero(side[r])
        lo, hi = xs.min(), xs.max()
        across[r, xs] = (xs - lo) / max(1, hi - lo)
    if np.corrcoef(across[side], geo[side])[0, 1] < 0:
        across = np.where(side, 1.0 - across, across)

    # the owner's hamon line, row by row. The owner paints the hardened edge as a paler zone from the edge out to a thin bright outline
    # (a zigzag, waves, square teeth, a straight line), the body past it: the line is the first thin bright stroke crossing the face
    # from the edge. Thin in any direction (where the outline runs along a row, as at a tooth's side, it is thin down the column), so
    # never the broad polished band; a pale stripe on the very edge is too near the edge to count, the body's dots lie past the line
    lum = rgb @ np.array([0.299, 0.587, 0.114])
    lum = np.where(side, lum, np.nan)
    line = hamon_line(lum, side, across, rows)
    edge_col = np.median(rgb[side & (across > 0.02) & (across < 0.08)], axis=0)
    good = ~np.isnan(line)
    steps = np.abs(np.diff(line[rows][good[rows]]))
    painted = good[rows].mean() > 0.8 and len(steps) > 40 and np.percentile(steps, 90) < 0.06
    if not painted:
        # no hardened edge painted (Gryphon's plain steel): a temper of irregular rounded waves (gunome) a quarter of the way across,
        # each wave its own height and width
        seed = int(hashlib.sha256(key.encode()).hexdigest()[:8], 16)
        rng = np.random.default_rng(seed + 7)
        rr = np.arange(h, dtype=float)
        line = np.full(h, 0.22)
        at = 0.0
        while at < h:
            width = rng.uniform(0.04, 0.07) * len(rows)
            height = rng.uniform(0.05, 0.10)
            u = (rr - at) / width
            hump = np.where((u >= 0) & (u <= 1), np.sin(np.pi * np.clip(u, 0, 1)) ** 0.7, 0.0)
            line = np.maximum(line, 0.20 + height * hump)
            at += width * rng.uniform(0.85, 1.0)
        line = line + 0.015 * np.sin(rr / 37.0)
    else:
        # a stray row (a dot, a mark touching the line) never makes a spike: each row against its neighbours'
        idx = np.flatnonzero(good)
        vals = line[good]
        med = np.array([np.median(vals[max(0, i - 3):i + 4]) for i in range(len(vals))])
        keep = np.abs(vals - med) < 0.06
        line = np.interp(np.arange(h), idx[keep], vals[keep])
    line = np.clip(line, 0.05, 0.66)
    k = np.array([1, 2, 1], float)
    line = np.convolve(np.pad(line, 1, mode="edge"), k / k.sum(), mode="valid")
    d = across - line[:, None]                                     # < 0 in the hardened edge

    seed = int(hashlib.sha256(key.encode()).hexdigest()[:8], 16)
    n_grain = noise((h, w), 2.5, seed, stretch=(5.0, 0.5))       # the forging grain, long along the blade (texture rows)
    n_mist = noise((h, w), 9.0, seed + 1)
    n_fine = noise((h, w), 1.0, seed + 2)
    rng = np.random.default_rng(seed + 3)
    speck = rng.random((h, w))

    # the body: the owner's own painting kept, polished a touch (lighter toward the line), its grain brought out
    polish = 1.0 + 0.08 * np.exp(-np.maximum(d, 0) / 0.15)
    grain = 0.965 + 0.07 * n_grain + 0.02 * (n_fine - 0.5)
    # the forging grain (hada): the folded steel's layers, flowing along the blade and wandering like wood grain
    n_warp = noise((h, w), 6.0, seed + 4, stretch=(6.0, 0.6))
    hada = np.sin(2 * np.pi * (across * 20.0 + 1.6 * n_warp))
    grain = grain * (1.0 + 0.035 * hada)
    body = rgb * (polish * grain)[..., None]
    # the hardened edge: a pale, misty frost over the edge's own tint (never flat white), clouded
    frost = edge_col + (np.array([0.93, 0.95, 0.98]) - edge_col) * 0.55
    mist = 0.90 + 0.12 * n_mist + 0.04 * (n_fine - 0.5)
    edge_paint = frost[None, None, :] * mist[..., None]
    edge_paint = edge_paint * 0.85 + rgb * 0.15                   # a trace of the owner's painting under the frost
    t = 1.0 / (1.0 + np.exp(d / 0.010))
    paint = body * (1 - t[..., None]) + edge_paint * t[..., None]
    # the temper line itself: a soft, bright, breathing glow (nioi)
    glow = np.exp(-(d / 0.018) ** 2) * (0.75 + 0.25 * n_mist)
    paint = paint + (np.array([1.0, 1.0, 1.0]) - paint) * (0.45 * glow)[..., None]
    # nie: fine crystals along the line, sparse
    nie = (np.abs(d) < 0.035) & (speck > 0.965 - 0.02 * n_mist)
    paint[nie] = np.minimum(1.0, paint[nie] * 0.5 + 0.6)
    # ashi: faint streaks from the line toward the edge, now and then
    phase = (np.arange(h)[:, None] / 7.0 + 2.5 * n_mist) % 3.0
    ashi = (d < -0.01) & (d > -0.12) & (np.abs(phase - 1.5) < 0.12)
    paint[ashi] = np.minimum(1.0, paint[ashi] * 0.85 + 0.16)
    out = rgb.copy()
    out[side] = paint[side]
    # the spine and the edge bevel: the owner's, the bevel brightened as a honed edge catches light
    out[bevel] = np.minimum(1.0, rgb[bevel] * 1.1 + 0.06)
    result = img.copy()
    result[..., :3] = np.where(on[..., None], np.clip(out, 0, 1), img[..., :3])
    return (result * 255 + 0.5).astype(np.uint8), hashlib.sha256(raw).hexdigest()


def main():
    keys = [k for k, _, _ in KATANAS] + ["gryphon"]
    os.makedirs(OUT, exist_ok=True)
    manifest = {}
    for key in keys:
        arr, digest = temper(key)
        Image.fromarray(arr, "RGBA").save(os.path.join(OUT, key + ".png"), optimize=True)
        manifest[key] = digest
        print(key)
    with open(os.path.join(OUT, "manifest.json"), "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=1, sort_keys=True)
        f.write("\n")
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1], keys)


def preview(out, keys):
    """Before and after: each blade's strip, then the blade drawn close in light."""
    os.makedirs(out, exist_ok=True)
    tiles = []
    for key in keys:
        verts, tris, blade = sv.mesh(key)
        u = verts[:, 3:5][tris[blade]].reshape(-1, 2)
        before = np.asarray(Image.open(io.BytesIO(source(key))).convert("RGBA"))
        after = np.asarray(Image.open(os.path.join(OUT, key + ".png")))
        h, w = before.shape[:2]
        x0, x1 = int(u[:, 0].min() * w), int(u[:, 0].max() * w) + 1
        y0, y1 = int(u[:, 1].min() * h), int(u[:, 1].max() * h) + 1
        tiles.append((Image.fromarray(before[y0:y1, x0:x1]).resize((90, 300)), Image.fromarray(after[y0:y1, x0:x1]).resize((90, 300))))
    sheet = Image.new("RGB", (len(tiles) * 190, 300), (25, 25, 25))
    for i, (b, a) in enumerate(tiles):
        sheet.paste(b, (i * 190, 0))
        sheet.paste(a, (i * 190 + 95, 0))
    sheet.save(os.path.join(out, "temper-strips.png"))


if __name__ == "__main__":
    main()
