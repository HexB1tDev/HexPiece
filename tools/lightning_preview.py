#!/usr/bin/env python3
"""Renders the Conqueror's bolts (top-down and side) and a crackled segment: java tools/preview/LightningDump | this > png."""
import json
import sys
from PIL import Image, ImageDraw, ImageFilter

data = json.load(sys.stdin)
out = sys.argv[1]
W, H = 420, 420
sheet = Image.new("RGB", (W * len(data["cases"]), H * 2 + 220), (236, 232, 222))
for c, case in enumerate(data["cases"]):
    r = case["radius"] * 1.08
    for view in range(2):
        layer = Image.new("RGBA", (W, H), (40, 44, 52, 255))
        glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        d = ImageDraw.Draw(layer)
        g = ImageDraw.Draw(glow)
        def p(x, y, z):
            if view == 0:
                return (W / 2 + x / r * W / 2, H / 2 + z / r * H / 2)
            return (W / 2 + x / r * W / 2, H * 0.8 - y / r * W / 2 * 4)  # height x4
        if view == 1:
            d.line([(0, H * 0.8), (W, H * 0.8)], fill=(90, 90, 90), width=1)
        for i, bolt in enumerate(case["bolts"]):
            pts = [p(bolt[k], bolt[k + 1], bolt[k + 2]) for k in range(0, len(bolt), 3)]
            main = i < case["mains"]
            n = len(pts) - 1
            for k in range(n):
                t = k / max(1, n - 1)
                w = (5 if main else 3) * (1 - 0.45 * t)
                g.line([pts[k], pts[k + 1]], fill=(200, 20, 30, 200), width=int(w * 3))
                d.line([pts[k], pts[k + 1]], fill=(8, 0, 10), width=max(1, int(w)))
        glow = glow.filter(ImageFilter.GaussianBlur(4))
        img = Image.alpha_composite(layer, glow)
        d2 = ImageDraw.Draw(img)
        for i, bolt in enumerate(case["bolts"]):
            pts = [p(bolt[k], bolt[k + 1], bolt[k + 2]) for k in range(0, len(bolt), 3)]
            for k in range(len(pts) - 1):
                d2.line([pts[k], pts[k + 1]], fill=(255, 40, 30), width=1)
        d2.ellipse([W / 2 - 4, (H / 2 if view == 0 else H * 0.8 - 20) - 4, W / 2 + 4, (H / 2 if view == 0 else H * 0.8 - 20) + 4], fill=(240, 220, 120))
        d2.text((6, 6), case["label"] + (" top" if view == 0 else " side, height x4"), fill=(230, 230, 230))
        sheet.paste(img.convert("RGB"), (c * W, view * H))
seg = Image.new("RGB", (W * 2, 200), (40, 44, 52))
sd = ImageDraw.Draw(seg)
for s, path in enumerate(data["segment"]):
    pts = [(30 + path[k] * 90 + s * 270, 150 - path[k + 1] * 60) for k in range(0, len(path), 3)]
    sd.line(pts, fill=(255, 40, 30), width=2)
sd.text((6, 6), "one segment A->B crackled (3 seeds)", fill=(230, 230, 230))
sheet.paste(seg, (0, H * 2 + 10))
sheet.save(out)
