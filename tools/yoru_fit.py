#!/usr/bin/env python3
"""2.2.7: where Yoru can hang on the back (sword/BackCarry, tools/sword_rig.py BACK_*): every tilt, lean and position tried for the
blade at a given length; kept: none of its vertices in the head, body, arms or legs (Minecraft boxes, a hair inflated), the lowest point
above the ground (y 24 from the neck), the grip (model y -2.4) within 8 px of the right shoulder, the pommel above the head; sorted by
the least tilt, then the least gap between the blade and the back. 2.2.7 took tilt 33, lean 8 at (-4.0, -1.0, 5.6) for 2.75 blocks.

Usage: python3 tools/yoru_fit.py   (a few minutes)
"""
import numpy as np
sys.path.insert(0, 'tools')
import katana_mesh as km
v, q, g = km.legend_meshes('input/Yoru_Gryphon_Models_Textures_1.20.1.zip')['yoru']
P = np.array([p[:3] for p in v])[::2] / (1.95 * 16 / 3.053)
BOX = [((-4.6, -8.6, -4.6), (4.6, 0.3, 4.6)), ((-4.35, 0.0, -2.35), (4.35, 12.0, 2.35)), ((-8.35, -0.35, -2.35), (-3.9, 12.0, 2.35)),
       ((3.9, -0.35, -2.35), (8.35, 12.0, 2.35)), ((-4.35, 12.0, -2.35), (4.35, 24.0, 2.35))]
SHOULDER = np.array((-5.0, 2.0, 0.0))
def frame(a, b):
    y = np.array((math.sin(a) * math.cos(b), math.cos(a) * math.cos(b), -math.sin(b)))
    e = np.array((-math.cos(a), math.sin(a), 0.0)); e = e - y * (e @ y); e /= np.linalg.norm(e)
    x = -e; return np.stack([x, y, np.cross(x, y)], axis=1)
res = []
k = 2.75 * 16 / 3.053
pts = P * k
for a_deg in range(18, 46, 3):
    for b_deg in range(4, 18, 2):
        R = frame(math.radians(a_deg), math.radians(b_deg)); w = pts @ R.T
        for x0 in np.arange(-6, 2.01, 0.5):
            for y0 in np.arange(-6, 3.01, 0.5):
                if (w[:, 1] + y0).max() > 23.8: continue
                if (w[:, 1] + y0).min() > -9.5: continue   # the pommel above the head
                for z0 in np.arange(4.0, 7.01, 0.2):
                    W = w + (x0, y0, z0)
                    if any(np.any(np.all((W > lo) & (W < hi), axis=1)) for lo, hi in BOX): continue
                    grip = R @ np.array((0, -2.4, 0)) + (x0, y0, z0)
                    reach = np.linalg.norm(grip - SHOULDER)
                    if reach > 8.0: break
                    blade = W[(W[:, 1] > 1) & (W[:, 1] < 20)]
                    gap = float(np.mean(blade[:, 2])) - 2.35
                    res.append((a_deg, gap, reach, b_deg, x0, y0, round(z0, 2), round(W[:, 1].min(), 1), round(W[:, 0].max(), 1)))
                    break
res.sort(key=lambda r: (r[0], r[1]))
print(len(res))
for r in res[:12]: print('tilt %d gap %.2f reach %.1f lean %d at (%.1f, %.1f, %.2f) top %s xmax %s' % r)
