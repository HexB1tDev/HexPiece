#!/usr/bin/env python3
"""2.5.6 (the owner: "improve the temper on all swords but yoru, the blade is too bland not good looking at all, simply improve the
texture thats it, same for Gryphon"): every katana's and Gryphon's blade texture repainted with a real temper (hamon), derived from
the owner's own atlas, which is never changed: the tempered atlases are written to textures/tempered/ (with manifest.json: the
SHA-256 of each source atlas), and build.py packs them in place of the originals while those hashes match (a changed source atlas
fails the build until this is run again). Yoru is left as it is.

How: each blade triangle of the owner's mesh is rasterised into its texture, so every blade texel knows where it lies on the blade:
how far along (y, model px), how far across from the edge to the spine (0..1), and which face (the flat, the spine, the edge bevel).
From the owner's own texture it reads, row by row along the blade, where its hardened edge ends (the hamon line: each blade keeps its
own pattern, zigzag, waves or straight) and its colours (the body's tint, the edge's). Then it paints, per texel:
  - the body (ji): the blade's own tint, polished (lighter toward the temper line, darker past the ridge), with a fine forging grain
    (hada) running along the blade;
  - the hardened edge (hasaki): frosted, misty white over the tint, cloud-like;
  - the temper line (nioiguchi): the owner's line, smoothed and drawn as a soft bright glow, with sparkling crystals (nie) along it
    and thin streaks (ashi) running from it toward the edge;
  - the ridge (shinogi) a crisp highlight, the spine (mune) dark and polished, the edge bevel bright.

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
    width = np.maximum(spine[bi] - edge[bi], 1e-3)
    across = np.clip(np.where(on, (pos[..., 0] - edge[bi]) / width, 0.0), 0.0, 1.0)
    ridge_a = np.clip((ridge[bi] - edge[bi]) / width, 0.3, 0.9)
    side = on & (np.abs(np.nan_to_num(nrm[..., 2])) > 0.55)
    spine_face = on & ~side & (np.nan_to_num(nrm[..., 0]) > 0.3)
    bevel = on & ~side & ~spine_face

    # the owner's hamon line, row by row along the blade: where the edge's colour gives way to the body's
    rgb = img[..., :3]
    length = y[on].max()
    ybins = np.arange(0.5, length + 0.01, 0.25)
    abins = np.linspace(0, 1, 41)
    edge_col = np.median(rgb[side & (across < 0.06)], axis=0)
    body_col = np.median(rgb[side & (across > 0.45) & (across < 0.7)], axis=0)
    line = np.full(len(ybins), np.nan)
    diff = body_col - edge_col
    span = float(diff @ diff) + 1e-6
    for i, yb in enumerate(ybins):
        row = side & (np.abs(y - yb) < 0.2)
        if row.sum() < 8:
            continue
        prof = []
        for j in range(len(abins) - 1):
            cell = row & (across >= abins[j]) & (across < abins[j + 1])
            prof.append(np.median(rgb[cell], axis=0) if cell.any() else None)
        likeness = [None if c is None else float((c - edge_col) @ diff) / span for c in prof]
        for j, l in enumerate(likeness):
            if l is not None and l > 0.5:
                line[i] = abins[j]
                break
    good = ~np.isnan(line)
    if good.sum() < 4:
        line = np.full(len(ybins), 0.28)
    else:
        line = np.interp(np.arange(len(line)), np.flatnonzero(good), line[good])
    line = np.clip(line, 0.08, 0.6)
    # smoothed a little (the owner's pattern kept, its stair-steps gone)
    k = np.array([1, 2, 3, 2, 1], float)
    line = np.convolve(np.pad(line, 2, mode="edge"), k / k.sum(), mode="valid")
    hamon = np.interp(y, ybins, line)

    seed = int(hashlib.sha256(key.encode()).hexdigest()[:8], 16)
    n_grain = noise((h, w), 3.0, seed, stretch=(4.0, 0.6))      # long grain along the blade (rows run along it in these atlases)
    n_mist = noise((h, w), 10.0, seed + 1)
    n_fine = noise((h, w), 1.2, seed + 2)
    rng = np.random.default_rng(seed + 3)
    speck = rng.random((h, w))

    tint = body_col / max(1e-3, body_col.max())
    lum = float(body_col @ np.array([0.3, 0.59, 0.11]))
    base = np.clip(0.42 + 0.55 * lum, 0.45, 0.82)
    steel = tint * base
    hot = np.clip(edge_col * 0.35 + 0.72, 0, 1)                   # the hardened edge: frosted white over its own tint

    d = across - hamon                                             # < 0 inside the hardened edge
    out = rgb.copy()
    # the body: polished, lighter toward the temper line, darker past the ridge, a fine grain along the blade
    polish = 1.0 + 0.10 * np.exp(-np.maximum(d, 0) / 0.18) - 0.16 * np.clip((across - ridge_a) / 0.2, 0, 1)
    grain = 0.94 + 0.12 * n_grain + 0.03 * (n_fine - 0.5)
    body = steel[None, None, :] * (polish * grain)[..., None]
    # the hardened edge: misty and frosted
    mist = 0.88 + 0.16 * n_mist + 0.05 * (n_fine - 0.5)
    edge_paint = hot[None, None, :] * mist[..., None]
    # the line: a soft bright glow (nioi) over the boundary
    t = 1.0 / (1.0 + np.exp(d / 0.012))                           # 1 in the edge, 0 in the body, a soft step
    paint = body * (1 - t[..., None]) + edge_paint * t[..., None]
    glow = np.exp(-(d / 0.022) ** 2)
    paint = paint + (np.array([1.0, 1.0, 1.0]) - paint) * (0.55 * glow)[..., None]
    # nie: bright crystals scattered along the line
    nie = (np.abs(d) < 0.05) & (speck > 0.93 - 0.05 * n_mist)
    paint[nie] = np.minimum(1.0, paint[nie] * 0.4 + 0.75)
    # ashi: thin streaks from the line toward the edge, every so often along the blade
    phase = (y * 1.7 + 3.0 * n_mist) % 4.0
    ashi = (d < 0) & (d > -0.16) & (np.abs(phase - 2.0) < 0.18)
    paint[ashi] = np.minimum(1.0, paint[ashi] * 0.75 + 0.3)
    # the ridge: a crisp highlight; the spine dark and polished; the edge bevel bright
    ridge_line = np.exp(-((across - ridge_a) / 0.018) ** 2)
    paint = paint + (1.0 - paint) * (0.35 * ridge_line)[..., None]
    out[side] = paint[side]
    out[spine_face] = (steel * 0.55)[None, :] * (0.9 + 0.2 * n_grain[spine_face])[:, None]
    out[bevel] = np.clip(hot * 0.92 + 0.06 * (n_fine[bevel][:, None] - 0.5), 0, 1)
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
