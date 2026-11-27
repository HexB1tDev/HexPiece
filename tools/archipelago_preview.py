#!/usr/bin/env python3
"""3.8.3: the archipelago world generation, previewed top down (the owner: "the world will now generate in Arquipelagos (big islands
but most of it will be sea) key is that every single river IS CONNECTED TO THE SEA no matter what ... the SEA is deeper than normal ...
every island MUST have a beach, long").

Evaluates the overworld's density functions as Minecraft 1.20.1 does (ImprovedNoise, PerlinNoise octaves, NormalNoise, shift_a/b,
shifted_noise, cubic splines, add/mul/min/max/abs/clamp, the caches as pass-throughs), from the JSON files under a data root: ours
(src/main/resources/data) over vanilla's (a folder of 1.20.1's own worldgen files: misode/mcmeta 1.20.1-data, never committed). The
river lines (hexhaki:river_lines) are the mod's own RiverField, run through Java (the same class the game runs). The surface height
is estimated from the terrain's offset where depth = 0: y = 128 + 128 x offset (the 3D noise and the factor's wobble left out).

Usage: python3 tools/archipelago_preview.py VANILLA_DATA_DIR OUT.png [--seed 1] [--size 12288] [--step 16] [--x0 0 --z0 0]
  VANILLA_DATA_DIR holds minecraft/worldgen/... (1.20.1). Needs numpy and build/classes (for RiverField: python3 build.py first).
"""
import json
import math
import os
import struct
import subprocess
import sys

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OURS = os.path.join(ROOT, "src", "main", "resources", "data")

GRAD = np.array([[1, 1, 0], [-1, 1, 0], [1, -1, 0], [-1, -1, 0], [1, 0, 1], [-1, 0, 1], [1, 0, -1], [-1, 0, -1],
                 [0, 1, 1], [0, -1, 1], [0, 1, -1], [0, -1, -1], [1, 1, 0], [0, -1, 1], [-1, 1, 0], [0, -1, -1]], dtype=np.float64)


class Improved:
    """Minecraft's ImprovedNoise (Perlin with its own permutation and gradient table, a random origin)."""

    def __init__(self, rng):
        self.xo, self.yo, self.zo = rng.random() * 256, rng.random() * 256, rng.random() * 256
        p = np.arange(256)
        rng.shuffle(p)
        self.p = np.concatenate([p, p]).astype(np.int64)

    def noise(self, x, y, z):
        x = x + self.xo
        y = y + self.yo
        z = z + self.zo
        xi, yi, zi = np.floor(x), np.floor(y), np.floor(z)
        xf, yf, zf = x - xi, y - yi, z - zi
        xi, yi, zi = xi.astype(np.int64) & 255, yi.astype(np.int64) & 255, zi.astype(np.int64) & 255
        p = self.p

        def g(h, dx, dy, dz):
            v = GRAD[h & 15]
            return v[..., 0] * dx + v[..., 1] * dy + v[..., 2] * dz

        a = p[xi] + yi
        b = p[xi + 1] + yi
        aa, ab, ba, bb = p[a] + zi, p[a + 1] + zi, p[b] + zi, p[b + 1] + zi
        u = xf * xf * xf * (xf * (xf * 6 - 15) + 10)
        v = yf * yf * yf * (yf * (yf * 6 - 15) + 10)
        w = zf * zf * zf * (zf * (zf * 6 - 15) + 10)
        l = lambda t, a_, b_: a_ + t * (b_ - a_)
        x1 = l(u, g(p[aa], xf, yf, zf), g(p[ba], xf - 1, yf, zf))
        x2 = l(u, g(p[ab], xf, yf - 1, zf), g(p[bb], xf - 1, yf - 1, zf))
        y1 = l(v, x1, x2)
        x3 = l(u, g(p[aa + 1], xf, yf, zf - 1), g(p[ba + 1], xf - 1, yf, zf - 1))
        x4 = l(u, g(p[ab + 1], xf, yf - 1, zf - 1), g(p[bb + 1], xf - 1, yf - 1, zf - 1))
        y2 = l(v, x3, x4)
        return l(w, y1, y2)


class Perlin:
    def __init__(self, rng, first, amps):
        self.amps = amps
        self.first = first
        self.octaves = [Improved(rng) if a != 0 else None for a in amps]
        n = len(amps)
        self.lowest_value = 2.0 ** (n - 1) / (2.0 ** n - 1)

    def value(self, x, y, z):
        total = 0.0
        freq = 2.0 ** self.first
        vf = self.lowest_value
        for a, o in zip(self.amps, self.octaves):
            if o is not None:
                total = total + a * o.noise(x * freq, y * freq, z * freq) * vf
            freq *= 2.0
            vf /= 2.0
        return total


class Normal:
    INPUT = 1.0181268882175227

    def __init__(self, rng, params):
        amps = [float(a) for a in params["amplitudes"]]
        first = int(params["firstOctave"])
        self.a = Perlin(rng, first, amps)
        self.b = Perlin(rng, first, amps)
        nz = [i for i, a in enumerate(amps) if a != 0]
        span = nz[-1] - nz[0] if nz else 0
        self.factor = (1.0 / 6.0) / (0.1 * (1.0 + 1.0 / (span + 1)))

    def value(self, x, y, z):
        return (self.a.value(x, y, z) + self.b.value(x * self.INPUT, y * self.INPUT, z * self.INPUT)) * self.factor


class World:
    def __init__(self, vanilla, seed, xs, zs):
        self.vanilla = vanilla
        self.seed = seed
        self.x = xs
        self.z = zs
        self.noises = {}
        self.cache = {}

    def load(self, kind, ident):
        ns, path = ident.split(":") if ":" in ident else ("minecraft", ident)
        for root in (OURS, self.vanilla):
            f = os.path.join(root, ns, "worldgen", kind, path + ".json")
            if os.path.exists(f):
                with open(f) as h:
                    return json.load(h)
        raise FileNotFoundError(ident)

    def noise(self, ident):
        if ident not in self.noises:
            h = int.from_bytes(ident.encode(), "little") % (2 ** 31)
            rng = np.random.default_rng((self.seed * 1000003 + h) % (2 ** 63))
            self.noises[ident] = Normal(rng, self.load("noise", ident))
        return self.noises[ident]

    def df(self, node):
        if isinstance(node, (int, float)):
            return np.full(self.x.shape, float(node))
        if isinstance(node, str):
            if node not in self.cache:
                self.cache[node] = self.df(self.load("density_function", node))
            return self.cache[node]
        t = node["type"].split(":")[-1]
        if t in ("flat_cache", "cache_2d", "cache_once", "interpolated", "cache_all_in_cell"):
            return self.df(node["argument"])
        if t == "add":
            return self.df(node["argument1"]) + self.df(node["argument2"])
        if t == "mul":
            return self.df(node["argument1"]) * self.df(node["argument2"])
        if t == "min":
            return np.minimum(self.df(node["argument1"]), self.df(node["argument2"]))
        if t == "max":
            return np.maximum(self.df(node["argument1"]), self.df(node["argument2"]))
        if t == "abs":
            return np.abs(self.df(node["argument"]))
        if t == "clamp":
            return np.clip(self.df(node["input"]), node["min"], node["max"])
        if t == "constant":
            return np.full(self.x.shape, float(node["argument"]))
        if t == "blend_offset":
            return np.zeros(self.x.shape)
        if t == "blend_alpha":
            return np.ones(self.x.shape)
        if t == "shift_a":
            n = self.noise(node["argument"])
            return n.value(self.x * 0.25, 0.0, self.z * 0.25) * 4.0
        if t == "shift_b":
            n = self.noise(node["argument"])
            return n.value(self.z * 0.25, self.x * 0.25, 0.0) * 4.0
        if t == "shifted_noise":
            n = self.noise(node["noise"])
            sx = self.df(node["shift_x"])
            sz = self.df(node["shift_z"])
            s = node["xz_scale"]
            return n.value(self.x * s + sx, 0.0, self.z * s + sz)
        if t == "noise":
            n = self.noise(node["noise"])
            s = node["xz_scale"]
            return n.value(self.x * s, 0.0, self.z * s)
        if t == "spline":
            return self.spline(node["spline"])
        if t == "river_lines":
            return river_lines(self.x, self.z, self.seed)
        if t == "island_field":
            return river_lines(self.x, self.z, self.seed, "islands")
        raise ValueError("unsupported density function " + t)

    def spline(self, s):
        if isinstance(s, (int, float)):
            return np.full(self.x.shape, float(s))
        c = self.df(s["coordinate"])
        pts = s["points"]
        loc = [p["location"] for p in pts]
        vals = [self.spline(p["value"]) for p in pts]
        der = [p["derivative"] for p in pts]
        out = np.zeros(self.x.shape)
        n = len(pts)
        below = c < loc[0]
        out = np.where(below, vals[0] + der[0] * (c - loc[0]), out)
        above = c >= loc[-1]
        out = np.where(above, vals[-1] + der[-1] * (c - loc[-1]), out)
        for i in range(n - 1):
            inside = (c >= loc[i]) & (c < loc[i + 1])
            if not inside.any():
                continue
            d = loc[i + 1] - loc[i]
            t = (c - loc[i]) / d
            y0, y1 = vals[i], vals[i + 1]
            d0, d1 = der[i] * d, der[i + 1] * d
            p = d0 - (y1 - y0)
            q = -d1 + (y1 - y0)
            v = y0 + t * (y1 - y0) + t * (1 - t) * (p + t * (q - p))
            out = np.where(inside, v, out)
        return out


def river_lines(x, z, seed, what="rivers"):
    """hexhaki:river_lines (or 3.9.5 hexhaki:island_field) through the game's own RiverField (IslandField) (Java), for every point."""
    pts = np.stack([x.ravel(), z.ravel()], axis=1).astype(">f8")
    data = struct.pack(">qi", seed, len(pts)) + pts.tobytes()
    cp = os.pathsep.join([os.path.join(ROOT, "build", "classes"), os.path.join(ROOT, "build", "preview")])
    out = subprocess.run(["java", "-cp", cp, "RiverDump"] + (["islands"] if what == "islands" else []), input=data, capture_output=True,
                         check=True).stdout
    return np.frombuffer(out, dtype=">f8").astype(np.float64).reshape(x.shape)


def main():
    args = sys.argv[1:]
    vanilla, out = args[0], args[1]
    opts = dict(zip(args[2::2], args[3::2]))
    seed = int(opts.get("--seed", 1))
    size = int(opts.get("--size", 12288))
    step = int(opts.get("--step", 16))
    x0 = int(opts.get("--x0", 0))
    z0 = int(opts.get("--z0", 0))
    n = size // step
    zs, xs = np.mgrid[0:n, 0:n]
    x = (x0 - size / 2 + xs * step).astype(np.float64)
    z = (z0 - size / 2 + zs * step).astype(np.float64)
    w = World(vanilla, seed, x, z)
    c = w.df("minecraft:overworld/continents")
    off = w.df("minecraft:overworld/offset")
    ridges = w.df("minecraft:overworld/ridges")
    erosion = w.df("minecraft:overworld/erosion")
    y = 128.0 + 128.0 * off
    land = y >= 63.0
    print(f"seed {seed}: land {land.mean() * 100:.1f}%  continentalness mean {c.mean():.3f} sd {c.std():.3f}  "
          f"sea floor mean {y[~land].mean():.1f} (deep under c<-0.455: {y[c < -0.455].mean() if (c < -0.455).any() else float('nan'):.1f})")
    beach = land & (c < -0.11) & (y < 69)
    print(f"  beach (coast band, land) {beach.mean() * 100:.1f}% of the map, {beach.sum() / max(1, land.sum()) * 100:.1f}% of the land; "
          f"river water on land {((~land) & (c > -0.11)).mean() * 100:.2f}%")
    img = np.zeros((n, n, 3), dtype=np.uint8)
    depth = np.clip((63.0 - y) / 63.0, 0, 1)
    sea = np.stack([18 + 30 * (1 - depth), 60 + 110 * (1 - depth), 110 + 110 * (1 - depth)], axis=-1)
    hgt = np.clip((y - 63.0) / 90.0, 0, 1)
    ground = np.stack([70 + 120 * hgt, 140 - 30 * hgt, 60 + 100 * hgt], axis=-1)
    img[:] = np.where(land[..., None], ground, sea).astype(np.uint8)
    img[beach] = (226, 210, 150)
    river = (~land) & (c > -0.19)
    img[river] = (40, 120, 230)
    Image.fromarray(img).save(out)
    print("wrote", out, f"({n}x{n}, {step} blocks a pixel, {size} blocks across)")


if __name__ == "__main__":
    main()
