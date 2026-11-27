#!/usr/bin/env python3
"""3.9.4: the Itsumade's item icon (the owner sent the fruit's picture: input/itsumade/fruit.webp, kept as sent): the fruit's carved
husk (its two orange-brown arms), its green leaves and the black-purple flames round it cut out of the parchment behind them (by colour:
the parchment is pale and grey-brown, the husk and the leaves saturated, the flames dark or purple; only the one connected shape kept,
so no stain of the parchment comes with it: the owner: "the 2d fruit is not meant to have a background"), shrunk to the game's 32 x 32,
outlined dark as the other fruits' icons are; then (the owner, of that icon: "Just use this with a red berry in the center") a red berry
filling the hollow between the husk's arms, shaded round with a glint.

Writes src/main/resources/assets/hexhaki/textures/item/itsu_itsu_no_mi.png.
Usage: python3 tools/itsu_icon.py [preview.png]
"""
import os
import sys

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "input", "itsumade", "fruit.webp")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "textures", "item", "itsu_itsu_no_mi.png")
SIZE = 32
OUTLINE = (0.09, 0.04, 0.10)
BERRY = np.array([0.80, 0.07, 0.12])
BERRY_DARK = np.array([0.42, 0.02, 0.07])
BERRY_GLINT = np.array([1.0, 0.75, 0.72])
BERRY_RIM = np.array([0.30, 0.02, 0.06])
BERRY_RADIUS = 4.4
HUSK_INSIDE = np.array([0.30, 0.10, 0.05])


def subject_mask(rgb):
    """True where the picture shows the fruit, the leaves or the flames (not the parchment, nor its painted clouds and ring)."""
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    mx = rgb.max(-1)
    mn = rgb.min(-1)
    sat = (mx - mn) / np.maximum(1e-6, mx)
    green = (g > r * 1.05) & (g > b * 1.2) & (sat > 0.3)
    fruit = (r > g * 1.3) & (r > b * 1.8) & (sat > 0.6) & (mx > 0.3)
    flame = ((b > r * 0.95) & (b > g * 1.1) & (sat > 0.25)) | ((mx < 0.16) & (b >= r * 0.9))
    return green | fruit | flame


def largest_blob(mask):
    """Only the largest 4-connected region of the mask (the fruit, its leaves and its flames are one; the parchment's stains are not)."""
    h, w = mask.shape
    seen = np.zeros_like(mask)
    best = None
    for y0 in range(h):
        for x0 in range(w):
            if not mask[y0, x0] or seen[y0, x0]:
                continue
            stack = [(y0, x0)]
            seen[y0, x0] = True
            cells = []
            while stack:
                y, x = stack.pop()
                cells.append((y, x))
                for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    ny, nx = y + dy, x + dx
                    if 0 <= ny < h and 0 <= nx < w and mask[ny, nx] and not seen[ny, nx]:
                        seen[ny, nx] = True
                        stack.append((ny, nx))
            if best is None or len(cells) > len(best):
                best = cells
    out = np.zeros_like(mask)
    for y, x in best or []:
        out[y, x] = True
    return out


def husk(px):
    """The husk's pixels in the icon (its orange-brown)."""
    r, g, b = px[..., 0], px[..., 1], px[..., 2]
    return (px[..., 3] > 0) & (r > g * 1.4) & (r > b * 2.0) & (r > 0.45)


def berry(out):
    """The red berry (the owner: "Just use this with a red berry in the center", then "the berry needs more details"): a round berry
    drawn over the husk's middle (never the husk's slit filled: that read wrong), raspberry-like: its drupelets in rows (each a lit bump
    with a shadow under it), a dark rim, a glint up and to the left; whatever is left of the gap between the husk's arms its shadowed
    inside."""
    hk = husk(out)
    rows = np.nonzero(hk.any(axis=1))[0]
    if len(rows) == 0:
        return
    top, bottom = rows.min(), rows.max()
    hollow = np.zeros(hk.shape, bool)
    for y in range(top + 1, bottom + 1):
        xs = np.nonzero(hk[y])[0]
        if len(xs) < 2:
            continue
        for x in range(xs.min() + 1, xs.max()):
            if not hk[y, x]:
                hollow[y, x] = True
    ys, xs = np.nonzero(hollow)
    if len(ys) == 0:
        return
    # the gap: the husk's dark inside
    out[hollow, :3] = HUSK_INSIDE
    out[hollow, 3] = 1.0
    # the berry: a ball centred a little below the gap's middle, wider than the gap (it sits in the husk's cup)
    cx = float(xs.mean())
    cy = float(ys.mean())
    r = BERRY_RADIUS
    for y in range(SIZE):
        for x in range(SIZE):
            dx, dy = x + 0.5 - (cx + 0.5), y + 0.5 - (cy + 0.5)
            d = (dx * dx + dy * dy) ** 0.5
            if d > r + 0.35:
                continue
            if d > r - 0.6:
                out[y, x, :3] = BERRY_RIM
                out[y, x, 3] = 1.0
                continue
            # the drupelets: a lattice of bumps; each pixel lit by its place on its bump and on the ball (the light up and left)
            u = (x - cx) * 0.95 + (y - cy) * 0.3
            v = (y - cy) * 0.95 - (x - cx) * 0.3
            bump = 0.5 + 0.5 * np.cos(u * 2.3) * np.cos(v * 2.3)
            ball = 1.0 - 0.55 * (d / r) - 0.18 * (dx + dy) / r
            col = BERRY * (0.55 + 0.45 * bump) * ball + BERRY_DARK * (1.0 - ball) * 0.6
            out[y, x, :3] = np.clip(col, 0, 1)
            out[y, x, 3] = 1.0
    gy, gx = int(round(cy - r * 0.45)), int(round(cx - r * 0.45))
    for yy, xx in ((gy, gx), (gy, gx + 1), (gy + 1, gx)):
        if 0 <= yy < SIZE and 0 <= xx < SIZE:
            out[yy, xx, :3] = BERRY_GLINT if (yy, xx) == (gy, gx) else (BERRY_GLINT + BERRY) / 2


def main():
    img = np.asarray(Image.open(SRC).convert("RGB"), np.float32) / 255.0
    h, w, _ = img.shape
    mask = subject_mask(img)
    # the parchment's corners and edges (its torn border, the painted clouds) are never the subject
    border = int(0.05 * min(w, h))
    mask[:border] = False
    mask[-border:] = False
    mask[:, :border] = False
    mask[:, -border:] = False
    # the largest connected shape, found on a quarter-size copy (closed a little so the flames' wisps join the fruit)
    small = mask[::4, ::4]
    grown = small.copy()
    for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        grown |= np.roll(small, (dy, dx), (0, 1))
    blob = largest_blob(grown) & small
    mask = np.repeat(np.repeat(blob, 4, 0), 4, 1)[:h, :w] & mask
    ys, xs = np.nonzero(mask)
    y0, y1 = np.percentile(ys, 0.5), np.percentile(ys, 99.5)
    x0, x1 = np.percentile(xs, 0.5), np.percentile(xs, 99.5)
    side = max(y1 - y0, x1 - x0) * 1.04
    cy, cx = (y0 + y1) / 2, (x0 + x1) / 2
    top, left = int(cy - side / 2), int(cx - side / 2)
    cell = side / (SIZE - 2)
    out = np.zeros((SIZE, SIZE, 4), np.float32)
    for j in range(SIZE - 2):
        for i in range(SIZE - 2):
            ya, yb = int(top + j * cell), int(top + (j + 1) * cell)
            xa, xb = int(left + i * cell), int(left + (i + 1) * cell)
            ya, yb, xa, xb = max(0, ya), min(h, yb), max(0, xa), min(w, xb)
            if yb <= ya or xb <= xa:
                continue
            m = mask[ya:yb, xa:xb]
            if m.mean() < 0.42:
                continue
            col = img[ya:yb, xa:xb][m].mean(0)
            out[j + 1, i + 1, :3] = col
            out[j + 1, i + 1, 3] = 1.0
    # a little more contrast and saturation (it is read 32 pixels wide)
    rgb = out[..., :3]
    lum = rgb.mean(-1, keepdims=True)
    rgb = np.clip(lum + (rgb - lum) * 1.25, 0, 1)
    rgb = np.clip((rgb - 0.5) * 1.12 + 0.52, 0, 1)
    out[..., :3] = rgb
    berry(out)
    solid = out[..., 3] > 0
    edge = np.zeros_like(solid)
    for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        edge |= np.roll(solid, (dy, dx), (0, 1))
    edge &= ~solid
    out[edge] = list(OUTLINE) + [1.0]
    Image.fromarray((out * 255).astype(np.uint8), "RGBA").save(OUT)
    if len(sys.argv) > 1:
        big = Image.fromarray((out * 255).astype(np.uint8), "RGBA").resize((SIZE * 10, SIZE * 10), Image.NEAREST)
        bg = Image.new("RGBA", big.size, (60, 60, 60, 255))
        bg.alpha_composite(big)
        bg.save(sys.argv[1])
    print("wrote", OUT)


if __name__ == "__main__":
    main()
