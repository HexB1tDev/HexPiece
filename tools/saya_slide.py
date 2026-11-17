#!/usr/bin/env python3
"""2.3.2: how each blade slides out of its own saya (the owner, of Gryphon's draw: "you can clearly see in the sheets that it's fucking
broken"). Before 2.3.2 every blade was drawn along one circle (radius 180 model px); Gryphon's saber curves three times as hard and its
hidden part came out through the scabbard's side mid-draw.

A blade's curve is not a circle (a katana's sori gathers towards the point, a saber's more so), so for every px {out} it is drawn, the
rigid turn and shift (in the blade's own plane, model px, about the koiguchi) that lays the part of the blade still in the saya onto
the line it lay along at rest: the part at u px up the blade goes where the part at u - out was. Fitted from the blade's own mesh
(its centre line, Kabsch in 2D); every blade's worst miss is under a quarter of a model px. Out past the table the blade goes on
straight along its last step. client/sword/SwordLayer lays the blade by sword/SayaSlide.java, generated from this table."""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

STEP = 1.0
_TABLES = {}


def centre_line(key):
    """The blade's centre line (model px, (x, y) from the guard up), smoothed: the middle between its back and its edge."""
    import sword_view as sv
    v, t, b = sv.mesh(key)
    pts = v[np.unique(t[b])] if b.any() else v[np.unique(t)]
    out = []
    y = pts[:, 1].min() + 0.3
    while y < pts[:, 1].max() - 0.3:
        s = pts[np.abs(pts[:, 1] - y) < 0.5]
        if len(s) >= 2:
            out.append(((s[:, 0].min() + s[:, 0].max()) / 2.0, y))
        y += 0.5
    c = np.array(out)
    k = np.ones(5) / 5.0
    xs = np.convolve(np.r_[[c[0, 0]] * 2, c[:, 0], [c[-1, 0]] * 2], k, "valid")
    return np.c_[xs, c[:, 1]]


def _along(c):
    return np.r_[0.0, np.cumsum(np.linalg.norm(np.diff(c, axis=0), axis=1))]


def _at(c, d, s):
    return np.c_[np.interp(s, d, c[:, 0]), np.interp(s, d, c[:, 1])]


def table(key):
    """[(angle degrees about z, tx, ty)] for out = 0, STEP, 2 STEP ... : model px -> model px, p' = R(angle) p + t."""
    if key in _TABLES:
        return _TABLES[key]
    c = centre_line(key)
    d = _along(c)
    rows = []
    s = 0.0
    while True:
        u = np.linspace(max(s, d[0]), d[-1], 40)
        if u[-1] - u[0] < 1.5:
            break
        a = _at(c, d, u)
        b = _at(c, d, u - s)
        ca, cb = a.mean(0), b.mean(0)
        h = (a - ca).T @ (b - cb)
        uu, _, vt = np.linalg.svd(h)
        r = vt.T @ uu.T
        if np.linalg.det(r) < 0:
            vt[1] *= -1
            r = vt.T @ uu.T
        t = cb - r @ ca
        rows.append((math.degrees(math.atan2(r[1, 0], r[0, 0])), float(t[0]), float(t[1])))
        s += STEP
    _TABLES[key] = rows
    return rows


def at(key, out):
    """(angle degrees, tx, ty) for the blade drawn out px (past the table: straight on along its last step)."""
    rows = table(key)
    x = max(0.0, out) / STEP
    i = int(math.floor(x))
    if i + 1 < len(rows):
        u = x - i
        return tuple(rows[i][k] + (rows[i + 1][k] - rows[i][k]) * u for k in range(3))
    a, b = rows[-2], rows[-1]
    u = x - (len(rows) - 1)
    return tuple(b[k] + (b[k] - a[k]) * u for k in range(3))


def residual(key):
    """The worst miss (model px) of the fit over the table."""
    c = centre_line(key)
    d = _along(c)
    worst = 0.0
    for i, (ang, tx, ty) in enumerate(table(key)):
        s = i * STEP
        u = np.linspace(max(s, d[0]), d[-1], 40)
        a = _at(c, d, u)
        b = _at(c, d, u - s)
        r = math.radians(ang)
        rot = np.array([[math.cos(r), -math.sin(r)], [math.sin(r), math.cos(r)]])
        worst = max(worst, float(np.abs(a @ rot.T + (tx, ty) - b).max()))
    return worst


if __name__ == "__main__":
    for name in sys.argv[1:] or ["crimson_dawn", "gryphon"]:
        print(name, f"worst {residual(name):.3f}px", [tuple(round(v, 2) for v in r) for r in table(name)[::4]])
