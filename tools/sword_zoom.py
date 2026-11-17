"""zoom.py MOVE TICKS OUT [views]: big tiles of a written move at the given ticks (comma list), every view a column."""
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_anims as sa
import sword_rig as rig
import sword_view as sv
import anim
from PIL import Image, ImageDraw
sa.SOLVE = False
sa.build()
name, ticks, out = sys.argv[1], [float(t) for t in sys.argv[2].split(",")], sys.argv[3]
views = sys.argv[4].split(",") if len(sys.argv) > 4 else ["front", "three", "side", "back"]
pitch = float(sys.argv[5]) if len(sys.argv) > 5 else 12.0
yaw_of = {"front": 0, "three": 35, "side": 90, "threeback": 145, "back": 180, "left": -90, "threeleft": -35, "top": 0}
move = sa.MOVES[name]
prof = move.profile
rig.set_sword(prof.key)
a = anim.Anim(json.load(open(os.path.join(sa.OUT, name + ".json"))))
W, Ht, S = 300, 300, 90.0
sheet = Image.new("RGB", (W * len(views), Ht * len(ticks)), (246, 244, 238))
for r, t in enumerate(ticks):
    q = a.pose(t)
    for c, view in enumerate(views):
        cam = sv.Camera(yaw_of[view], 80.0 if view == "top" else pitch, S, W / 2, Ht * 0.92)
        cv = sv.Canvas(W, Ht)
        sv.draw_figure(cv, cam, q)
        drawn = move.drawn_at(t) or sa.sliding(move, t)
        if prof is sa.YORU:
            sv.draw_ropes(cv, cam, q)
            sv.draw_katana(cv, cam, "yoru", sa.game_blade(move, q, t), shine=0.3)
        elif drawn:
            sv.draw_katana(cv, cam, prof.key, sa.game_blade(move, q, t))
            sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=False)
        else:
            sv.draw_katana(cv, cam, prof.key, rig.saya_matrix(q), saya=True, blade=True, shine=0.3)
        img = cv.image()
        d = ImageDraw.Draw(img)
        d.line([(0, Ht * 0.92), (W, Ht * 0.92)], fill=(150, 120, 90))
        d.text((4, 4), f"{name} t{t:g} {view}", fill=(40, 40, 40))
        sheet.paste(img, (c * W, r * Ht))
sheet.save(out)
print(out)
