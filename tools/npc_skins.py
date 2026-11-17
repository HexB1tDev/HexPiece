#!/usr/bin/env python3
"""The pirates' and marines' skins (2.3.2: remade; the owner: "the Marines and Pirate skins are shit and need to be recreated and bettered,
you CAN take skins from HexGhoul"; "the NPCS are all alex").

Each skin is a 128 px Alex (slim-armed) player skin: the head and hair are a HexGhoul villager's (art/npc/human_NN.png, see
art/npc/SOURCES.txt), the clothes are painted here, shaded cloth with folds, seams, belts and boots; the bare forearms and hands take the
face's own skin tone. Beside each skin, <side>_<look>_closed.png: the same face's shut eyes (HexGhoul's hand-boxed overlay of it), laid
over the skin when it blinks.

Pirates: bandanas and headscarves, striped shirts, open vests, sashes, baggy trousers, boots; their officers long coats; their captains a
red or black captain's coat with gold, a tricorn with the skull. Marines: they wear the owner's Marine outfit (armor) over the skin, so
theirs is the uniform under it (white shirt, navy trousers, black boots) with nothing in the skin's outer layer where the outfit goes:
vanilla's outer layer stands 0.25 px off the body, outside the outfit's tight shirt, and would show through it.

Writes src/main/resources/assets/hexhaki/textures/piece/npc/<side>_<n>.png and <side>_<n>_closed.png, the looks in SeaRules.LOOKS order
(rank, female): two men and two women grunts, an officer man and woman, a captain man and woman.
Usage: python3 tools/npc_skins.py
"""
import colorsys
import os
import random

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/piece/npc")
ART = os.path.join(ROOT, "art/npc")
K = 2  # pixels per skin unit (a 128 px skin)

# Regions of a 64-unit skin, slim (Alex) arms: part -> {face: (x0, y0, x1, y1)}; outer layers listed separately.
HEAD = {"top": (8, 0, 16, 8), "bottom": (16, 0, 24, 8), "right": (0, 8, 8, 16), "front": (8, 8, 16, 16), "left": (16, 8, 24, 16),
        "back": (24, 8, 32, 16)}
HAT = {k: (a + 32, b, c + 32, d) for k, (a, b, c, d) in HEAD.items()}
BODY = {"top": (20, 16, 28, 20), "bottom": (28, 16, 36, 20), "right": (16, 20, 20, 32), "front": (20, 20, 28, 32), "left": (28, 20, 32, 32),
        "back": (32, 20, 40, 32)}
JACKET = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in BODY.items()}
R_ARM = {"top": (44, 16, 47, 20), "bottom": (47, 16, 50, 20), "right": (40, 20, 44, 32), "front": (44, 20, 47, 32), "left": (47, 20, 51, 32),
         "back": (51, 20, 54, 32)}
L_ARM = {"top": (36, 48, 39, 52), "bottom": (39, 48, 42, 52), "right": (32, 52, 36, 64), "front": (36, 52, 39, 64), "left": (39, 52, 43, 64),
         "back": (43, 52, 46, 64)}
R_SLEEVE = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in R_ARM.items()}
L_SLEEVE = {k: (a + 16, b, c + 16, d) for k, (a, b, c, d) in L_ARM.items()}
R_LEG = {"top": (4, 16, 8, 20), "bottom": (8, 16, 12, 20), "right": (0, 20, 4, 32), "front": (4, 20, 8, 32), "left": (8, 20, 12, 32),
         "back": (12, 20, 16, 32)}
L_LEG = {"top": (20, 48, 24, 52), "bottom": (24, 48, 28, 52), "right": (16, 52, 20, 64), "front": (20, 52, 24, 64), "left": (24, 52, 28, 64),
         "back": (28, 52, 32, 64)}
R_PANTS = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in R_LEG.items()}
L_PANTS = {k: (a - 16, b, c - 16, d) for k, (a, b, c, d) in L_LEG.items()}
SIDES = ("right", "front", "left", "back")

# the looks (SeaRules.LOOKS): rank, female; and each one's head
LOOKS = [(0, 0), (0, 0), (0, 1), (0, 1), (1, 0), (1, 1), (2, 0), (2, 1)]
HEADS = {
    "pirate": ["human_19", "human_22", "human_15", "human_03", "human_23", "human_14", "human_08", "human_02"],
    "marine": ["human_05", "human_25", "human_16", "human_13", "human_06", "human_10", "human_20", "human_12"],
}


def rgb(hexcode):
    hexcode = hexcode.lstrip("#")
    return tuple(int(hexcode[i:i + 2], 16) for i in (0, 2, 4))


def mul(c, k):
    return tuple(max(0, min(255, int(round(v * k)))) for v in c[:3])


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


class Skin:
    def __init__(self, seed):
        self.img = Image.new("RGBA", (64 * K, 64 * K), (0, 0, 0, 0))
        self.px = self.img.load()
        self.rng = random.Random(seed)

    # --- painting in skin units (64 across); every unit is K pixels ---
    def put(self, x, y, c, a=255):
        if 0 <= x < 64 * K and 0 <= y < 64 * K:
            self.px[x, y] = tuple(c[:3]) + (a,)

    def get(self, x, y):
        return self.px[x, y]

    def clear(self, rect):
        x0, y0, x1, y1 = rect
        for y in range(y0 * K, y1 * K):
            for x in range(x0 * K, x1 * K):
                self.px[x, y] = (0, 0, 0, 0)

    def cloth(self, rect, c, rows=None, top=1.06, bottom=0.9, noise=0.035, a=255):
        """Fill {rect} (or its rows r0..r1 in units) with cloth: lit from above, darker down it, a fine weave of noise."""
        x0, y0, x1, y1 = rect
        r0, r1 = (y0, y1) if rows is None else (y0 + rows[0], y0 + rows[1])
        h = max(1, (y1 - y0) * K - 1)
        for y in range(int(r0 * K), int(r1 * K)):
            t = (y - y0 * K) / h
            k = top + (bottom - top) * t
            for x in range(x0 * K, x1 * K):
                n = 1.0 + (self.rng.random() - 0.5) * 2 * noise
                self.put(x, y, mul(c, k * n), a)

    def hline(self, rect, row, c, k=1.0, thick=1):
        """A line across {rect} at unit row {row} (a seam, a hem, a belt edge), {thick} pixels."""
        x0, y0, x1, y1 = rect
        for y in range(int((y0 + row) * K), int((y0 + row) * K) + thick):
            for x in range(x0 * K, x1 * K):
                self.put(x, y, mul(c, k))

    def vline(self, rect, col, c, rows=None, k=1.0, thick=1):
        x0, y0, x1, y1 = rect
        r0, r1 = (y0, y1) if rows is None else (y0 + rows[0], y0 + rows[1])
        for x in range(int((x0 + col) * K), int((x0 + col) * K) + thick):
            for y in range(int(r0 * K), int(r1 * K)):
                self.put(x, y, mul(c, k))

    def folds(self, rect, c, count, rows=None, length=(3, 6)):
        """Creases: short darker strokes running down and a little across, a lit pixel beside each."""
        x0, y0, x1, y1 = rect
        r0, r1 = (y0, y1) if rows is None else (y0 + rows[0], y0 + rows[1])
        for _ in range(count):
            x = self.rng.uniform(x0 * K + 1, x1 * K - 2)
            y = self.rng.uniform(r0 * K, r1 * K - 2)
            dx = self.rng.choice((-0.5, -0.35, 0.35, 0.5, 0.0))
            for i in range(self.rng.randint(*length)):
                xi, yi = int(x + dx * i), int(y + i)
                if x0 * K <= xi < x1 * K - 1 and r0 * K <= yi < r1 * K:
                    self.put(xi, yi, mul(c, 0.8))
                    self.put(xi + 1, yi, mul(c, 1.1))

    def dot(self, x, y, c, size=1):
        for j in range(size):
            for i in range(size):
                self.put(int(x * K) + i, int(y * K) + j, c)


def resample(img, size):
    """A HexGhoul head (64 to 1024 px skins) at our size: box-filtered down, or kept crisp up."""
    if img.width == size:
        return img
    if img.width < size:
        return img.resize((size, size), Image.NEAREST)
    return img.resize((size, size), Image.BOX)


def skin_tone(head):
    """The face's own skin: the commonest colour of its cheeks and chin (the lower face, under the eyes, away from the hair)."""
    k = head.width // 64
    counts = {}
    for y in range(int(14.5 * k), 16 * k):
        for x in range(9 * k, 15 * k):
            r, g, b, a = head.getpixel((x, y))
            if a < 250:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if not (0.02 <= h <= 0.12 and 0.35 <= l <= 0.92 and s > 0.12):
                continue  # not skin (a mouth line, a shadow, hair)
            key = (r // 6 * 6, g // 6 * 6, b // 6 * 6)
            counts[key] = counts.get(key, 0) + 1
    if not counts:
        return (230, 190, 160)
    return max(counts, key=counts.get)


# --- the clothes -------------------------------------------------------------------------------------------------------------------

def bare_arm(s, arm, tone, rows):
    """Skin on an arm's rows (a forearm under a short or rolled sleeve), the hand a touch darker at its end."""
    for side in SIDES:
        s.cloth(arm[side], tone, rows=rows, top=1.04, bottom=0.94, noise=0.015)
    s.cloth(arm["bottom"], mul(tone, 0.9), noise=0.015)


def sleeve(s, arm, c, rows, cuff=None, rolled=False, stripes=None):
    for side in SIDES:
        s.cloth(arm[side], c, rows=rows)
        if stripes:
            stripe_rows(s, arm[side], stripes, rows)
        s.folds(arm[side], c, 1, rows=rows, length=(2, 4))
        if cuff is not None:
            s.hline(arm[side], rows[1] - 1, cuff, thick=K)
        if rolled:
            s.hline(arm[side], rows[1] - 1, mul(c, 1.08), thick=K)
            s.hline(arm[side], rows[1] - 1, mul(c, 0.75), thick=1)
    s.cloth(arm["top"], mul(c, 1.05))


def stripe_rows(s, rect, stripes, rows):
    """Horizontal stripes (a sailor's): every other band in the second colour."""
    c2, every = stripes
    x0, y0, x1, y1 = rect
    for y in range(int((y0 + rows[0]) * K), int((y0 + rows[1]) * K)):
        if (y // every) % 2 == 1:
            for x in range(x0 * K, x1 * K):
                r, g, b, a = s.get(x, y)
                lum = (r + g + b) / (3 * 255)
                s.put(x, y, mul(c2, 0.7 + 0.5 * lum))


def trousers(s, leg, c, boots, boot_from, cuffs=False):
    for side in SIDES:
        s.cloth(leg[side], c, rows=(0, boot_from), top=1.0, bottom=0.88)
        s.folds(leg[side], c, 2, rows=(0, boot_from), length=(3, 6))
    s.cloth(leg["top"], mul(c, 0.95))
    boot(s, leg, boots, boot_from, cuffs)


def boot(s, leg, c, frm, cuff=False):
    for side in SIDES:
        s.cloth(leg[side], c, rows=(frm, 12), top=1.1, bottom=0.82, noise=0.025)
        s.hline(leg[side], 11.5, mul(c, 0.45), thick=K // 2 + 1)  # the sole
        if cuff:
            s.hline(leg[side], frm, mul(c, 1.25), thick=K)
            s.hline(leg[side], frm + 1, mul(c, 0.7), thick=1)
        if side == "front":
            s.hline(leg[side], frm + (12 - frm) * 0.55, mul(c, 1.35), thick=1)  # a lit toe crease
    s.cloth(leg["bottom"], mul(c, 0.45))


def torso_shirt(s, c, stripes=None, collar=None, open_neck=None, tone=None):
    for side in SIDES:
        s.cloth(BODY[side], c, top=1.05, bottom=0.92)
        if stripes:
            stripe_rows(s, BODY[side], stripes, (0, 12))
        s.folds(BODY[side], c, 2 if side in ("front", "back") else 1, rows=(4, 12))
    s.cloth(BODY["top"], mul(c, 1.05))
    s.cloth(BODY["bottom"], mul(c, 0.85))
    if open_neck and tone:
        # a V of bare chest at the collar
        x0, y0, _, _ = BODY["front"]
        for i in range(open_neck * K):
            for x in range(int((x0 + 4) * K - (open_neck * K - i) // 2), int((x0 + 4) * K + (open_neck * K - i) // 2)):
                s.put(x, y0 * K + i, mul(tone, 0.96))
    if collar:
        s.hline(BODY["front"], 0, collar, thick=K)
        s.hline(BODY["back"], 0, collar, thick=K)


def vest(s, c, gap=2, trim=None):
    """An open vest: down both sides of the front, the whole back and sides."""
    x0, y0, x1, y1 = BODY["front"]
    for side in ("right", "left", "back"):
        s.cloth(BODY[side], c, rows=(0, 10), top=1.08, bottom=0.86)
        s.folds(BODY[side], c, 1, rows=(0, 10))
    left = (x0, y0, x0 + 4 - gap // 2, y1)
    right = (x0 + 4 + (gap + 1) // 2, y0, x1, y1)
    for half in (left, right):
        s.cloth(half, c, rows=(0, 10), top=1.08, bottom=0.86)
    if trim:
        s.vline(BODY["front"], 4 - gap // 2 - 0.5, trim, rows=(0, 10), thick=1)
        s.vline(BODY["front"], 4 + (gap + 1) // 2, trim, rows=(0, 10), thick=1)


def sash(s, c, knot=True):
    for side in SIDES:
        s.cloth(BODY[side], c, rows=(9, 11.5), top=1.15, bottom=0.8, noise=0.02)
        s.hline(BODY[side], 10, mul(c, 0.75), thick=1)
    if knot:
        x0, y0, _, _ = BODY["front"]
        for j in range(3 * K):
            for i in range(K + (j // 2)):
                s.put((x0 + 6) * K + i, (y0 + 10) * K + j, mul(c, 0.9 - 0.05 * (j % 2)))


def belt(s, c, buckle):
    for side in SIDES:
        s.cloth(BODY[side], c, rows=(10, 11.5), top=1.1, bottom=0.85, noise=0.015)
    x0, y0, _, _ = BODY["front"]
    for j in range(int(1.5 * K)):
        for i in range(2 * K):
            edge = i in (0, 2 * K - 1) or j in (0, int(1.5 * K) - 1)
            s.put((x0 + 3) * K + i, (y0 + 10) * K + j, buckle if edge else mul(c, 0.7))


def coat(s, c, trim, tails=6, gold=None, buttons=None):
    """A long coat open down the front: on the torso's outer layer, the sleeves' outer layer, and on the legs' outer layer as tails."""
    x0, y0, x1, y1 = JACKET["front"]
    for side in ("right", "left", "back"):
        s.cloth(JACKET[side], c, top=1.06, bottom=0.88)
        s.folds(JACKET[side], c, 2)
    for half in ((x0, y0, x0 + 3, y1), (x0 + 5, y0, x1, y1)):
        s.cloth(half, c, top=1.06, bottom=0.88)
    s.vline(JACKET["front"], 3, trim, thick=K // 2 + 1)
    s.vline(JACKET["front"], 5 - 0.5, trim, thick=K // 2 + 1)
    s.cloth(JACKET["top"], mul(c, 1.05))
    if buttons:
        for row in (2, 5, 8):
            s.dot(x0 + 2.5, y0 + row, buttons)
            s.dot(x0 + 5.5, y0 + row, buttons)
    if gold:
        # epaulettes: the coat's shoulders on the sleeves' tops, a fringe down their outsides
        for arm, side in ((R_SLEEVE, "right"), (L_SLEEVE, "left")):
            s.cloth(arm["top"], gold, top=1.2, bottom=0.9)
            s.cloth(arm[side], gold, rows=(0, 1.5), top=1.25, bottom=0.8)
            x0a, y0a, x1a, _ = arm[side]
            for x in range(x0a * K, x1a * K, 2):
                s.put(x, int((y0a + 1.5) * K), mul(gold, 0.7))
    # the tails on the legs' outer layer: the outsides and back of each leg, open between them in front
    for pants, outside in ((R_PANTS, "right"), (L_PANTS, "left")):
        for side in (outside, "back"):
            s.cloth(pants[side], c, rows=(0, tails), top=0.95, bottom=0.82)
            s.folds(pants[side], c, 1, rows=(0, tails))
            s.hline(pants[side], tails - 0.5, trim, thick=K // 2 + 1)
        fx0, fy0, fx1, fy1 = pants["front"]
        third = (fx0, fy0, fx0 + 2, fy1) if outside == "right" else (fx1 - 2, fy0, fx1, fy1)
        s.cloth(third, c, rows=(0, tails), top=0.95, bottom=0.82)
        s.hline(third, tails - 0.5, trim, thick=K // 2 + 1)


def coat_sleeves(s, c, cuff):
    for arm in (R_SLEEVE, L_SLEEVE):
        for side in SIDES:
            s.cloth(arm[side], c, rows=(0, 10))
            s.folds(arm[side], c, 1, rows=(1, 9))
            s.hline(arm[side], 8.5, cuff, thick=int(1.5 * K))
        s.cloth(arm["top"], mul(c, 1.05))


def bandana(s, c, dots=None, knot_tails=True):
    """A scarf tied over the top of the head: on the hat layer's top and the upper rows of its sides, knotted at the back."""
    s.cloth(HAT["top"], c, top=1.12, bottom=0.92)
    for side in SIDES:
        s.clear((HAT[side][0], HAT[side][1], HAT[side][2], HAT[side][1] + 3))
        s.cloth(HAT[side], c, rows=(0, 2.5), top=1.1, bottom=0.82)
        s.hline(HAT[side], 2.5, mul(c, 0.6), thick=1)
    if dots:
        x0, y0, x1, y1 = HAT["top"]
        for j in range(y0 * K + 1, y1 * K - 1, 4):
            for i in range(x0 * K + 1 + (j // 4 % 2) * 2, x1 * K - 1, 4):
                s.put(i, j, dots)
        for side in SIDES:
            x0, y0, x1, _ = HAT[side]
            for i in range(x0 * K + 2, x1 * K - 1, 4):
                s.put(i, y0 * K + 2, dots)
    if knot_tails:
        x0, y0, _, _ = HAT["back"]
        for j in range(5 * K):
            for i in range(-(j // 3), K + (j // 3)):
                s.put((x0 + 4) * K + i - 1, (y0 + 2) * K + j, mul(c, 0.95 - 0.05 * (j % 3 == 0)))


def tricorn(s, c, trim, emblem=None, feather=None):
    """A captain's tricorn on the hat layer: the crown over the top, the brim turned up round the sides with a gold edge."""
    s.cloth(HAT["top"], c, top=1.15, bottom=0.9)
    for side in SIDES:
        s.clear((HAT[side][0], HAT[side][1], HAT[side][2], HAT[side][1] + 4))
        s.cloth(HAT[side], c, rows=(0, 3.5), top=1.15, bottom=0.8)
        s.hline(HAT[side], 3.5, trim, thick=K // 2 + 1)
        s.hline(HAT[side], 0, mul(c, 1.3), thick=1)
    if emblem:
        # the skull and crossbones on the front
        x0, y0, _, _ = HAT["front"]
        cx, cy = (x0 + 4) * K, (y0 + 1.6) * K
        skull = ["  ###  ", " ##### ", " #.#.# ", "  ###  ", "#  #  #", " #   # "]
        for j, row in enumerate(skull):
            for i, ch in enumerate(row):
                if ch == "#":
                    s.put(int(cx - 3 + i), int(cy - 3 + j), emblem)
                elif ch == ".":
                    s.put(int(cx - 3 + i), int(cy - 3 + j), mul(c, 0.6))
    if feather:
        x0, y0, _, _ = HAT["left"]
        for j in range(6):
            s.put(x0 * K + 10 - j, y0 * K + j, mul(feather, 1.1 - j * 0.06))
            s.put(x0 * K + 11 - j, y0 * K + j, mul(feather, 0.85))


# --- the outfits ----------------------------------------------------------------------------------------------------------------------

def pirate(s, look, tone):
    rank, female = LOOKS[look]
    if look == 0:
        # a sailor's stripes, sleeves rolled, black trousers, a red sash, a red spotted bandana
        torso_shirt(s, rgb("f2ece0"), stripes=(rgb("1f3f8a"), 3))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("f2ece0"), (0, 6), rolled=True, stripes=(rgb("1f3f8a"), 3))
            bare_arm(s, arm, tone, (6, 12))
        sash(s, rgb("b8262b"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("2b2a30"), rgb("5a3a22"), 8)
        bandana(s, rgb("c0282f"), dots=rgb("f0e6dc"))
    elif look == 1:
        # an open leather vest over a loose shirt, brown belt, sand trousers, a green headscarf
        torso_shirt(s, rgb("e6dcc4"), open_neck=4, tone=tone)
        vest(s, rgb("6b4426"), gap=3, trim=rgb("3c2615"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("e6dcc4"), (0, 7), rolled=True)
            bare_arm(s, arm, tone, (7, 12))
        belt(s, rgb("3a2416"), rgb("c9a646"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("a08a62"), rgb("2e2018"), 7, cuffs=True)
        bandana(s, rgb("3f7a3a"), knot_tails=True)
    elif look == 2:
        # a white blouse, a laced black bodice, deep red trousers, tall boots, a red headscarf
        torso_shirt(s, rgb("f4efe6"), open_neck=3, tone=tone)
        vest(s, rgb("1e1b22"), gap=1, trim=rgb("8a6a3a"))
        x0, y0, _, _ = BODY["front"]
        for row in (3, 5, 7):
            s.dot(x0 + 3.5, y0 + row, rgb("d6c08a"))
            s.dot(x0 + 4.5, y0 + row, rgb("d6c08a"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("f4efe6"), (0, 9), cuff=rgb("d8d0c4"))
            bare_arm(s, arm, tone, (9, 12))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("7a1f26"), rgb("231c1c"), 5, cuffs=True)
        bandana(s, rgb("a3252c"), knot_tails=True)
    elif look == 3:
        # blue and white stripes, a short brown vest, rolled sleeves, grey trousers, a blue bandana
        torso_shirt(s, rgb("eef0f4"), stripes=(rgb("2a5aa8"), 2))
        vest(s, rgb("7a5232"), gap=3)
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("eef0f4"), (0, 5), rolled=True, stripes=(rgb("2a5aa8"), 2))
            bare_arm(s, arm, tone, (5, 12))
        sash(s, rgb("d18f2a"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("4a4d55"), rgb("4a3020"), 7)
        bandana(s, rgb("2b5fb0"), dots=rgb("eef0f4"))
    elif look == 4:
        # the officer: a long dark green coat open over a white shirt, a brown sword belt, black trousers, tall boots
        torso_shirt(s, rgb("f1ece2"))
        belt(s, rgb("4a2c18"), rgb("caa548"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("f1ece2"), (0, 10))
            bare_arm(s, arm, tone, (10, 12))
        coat(s, rgb("24453a"), rgb("b8963e"), tails=7, buttons=rgb("d2b25a"))
        coat_sleeves(s, rgb("24453a"), rgb("b8963e"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("22232a"), rgb("1c1714"), 5, cuffs=True)
    elif look == 5:
        # the officer: a plum coat over a white shirt, a wide black belt, cream trousers, tall boots
        torso_shirt(s, rgb("f4efe6"), open_neck=3, tone=tone)
        belt(s, rgb("1c1a1e"), rgb("c9a646"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("f4efe6"), (0, 10))
            bare_arm(s, arm, tone, (10, 12))
        coat(s, rgb("4b2546"), rgb("c9a646"), tails=6, buttons=rgb("d6b55a"))
        coat_sleeves(s, rgb("4b2546"), rgb("c9a646"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("d9cfb8"), rgb("221a18"), 5, cuffs=True)
    elif look == 6:
        # the captain: the red captain's coat with gold, epaulettes, black trousers, black boots, a tricorn with the skull
        torso_shirt(s, rgb("efe8da"))
        sash(s, rgb("1f1b1c"), knot=False)
        belt(s, rgb("1f1b1c"), rgb("e0bf55"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("efe8da"), (0, 10))
            bare_arm(s, arm, tone, (10, 12))
        coat(s, rgb("9c1b22"), rgb("e0bf55"), tails=9, gold=rgb("e7c55a"), buttons=rgb("efd170"))
        coat_sleeves(s, rgb("9c1b22"), rgb("e0bf55"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("1d1c22"), rgb("141214"), 5, cuffs=True)
        tricorn(s, rgb("1b1a1e"), rgb("d6b452"), emblem=rgb("f2efe8"))
    else:
        # the captain: a black captain's coat with gold, a red shirt, white trousers, black boots, a tricorn with a red plume
        torso_shirt(s, rgb("b8242b"), open_neck=3, tone=tone)
        belt(s, rgb("1c1a1e"), rgb("e0bf55"))
        for arm in (R_ARM, L_ARM):
            sleeve(s, arm, rgb("b8242b"), (0, 10))
            bare_arm(s, arm, tone, (10, 12))
        coat(s, rgb("1c1b21"), rgb("e0bf55"), tails=8, gold=rgb("e7c55a"), buttons=rgb("efd170"))
        coat_sleeves(s, rgb("1c1b21"), rgb("e0bf55"))
        for leg in (R_LEG, L_LEG):
            trousers(s, leg, rgb("ece6dc"), rgb("141214"), 5, cuffs=True)
        tricorn(s, rgb("1b1a1e"), rgb("d6b452"), emblem=rgb("f2efe8"), feather=rgb("c4252d"))


def marine(s, look, tone):
    """The uniform under the Marine outfit (cap, shirt, trousers, boots are the armor): a white shirt with short sleeves, bare forearms,
    navy trousers, black boots. Nothing in the outer layer but the head's (the outfit is drawn just outside the body)."""
    torso_shirt(s, rgb("f3f4f6"), collar=rgb("2a4a8c"))
    for arm in (R_ARM, L_ARM):
        sleeve(s, arm, rgb("f3f4f6"), (0, 5), cuff=rgb("2a4a8c"))
        bare_arm(s, arm, tone, (5, 12))
    belt(s, rgb("1f1f24"), rgb("c9cdd6"))
    for leg in (R_LEG, L_LEG):
        trousers(s, leg, rgb("27386b"), rgb("17171b"), 9)


def hair_colour(head):
    """The hair's colour: the commonest of the head's top (both layers), and a darker and a lighter shade of it."""
    counts = {}
    for region in (HEAD["top"], HAT["top"]):
        x0, y0, x1, y1 = region
        for y in range(y0 * K, y1 * K):
            for x in range(x0 * K, x1 * K):
                r, g, b, a = head.getpixel((x, y))
                if a > 200:
                    key = (r // 8 * 8, g // 8 * 8, b // 8 * 8)
                    counts[key] = counts.get(key, 0) + 1
    base = max(counts, key=counts.get) if counts else (60, 40, 30)
    return base


def hair_back(s, hair, long_hair, rng):
    """The back of the head in the face's own hair (2.3.2: the villagers' heads wear flower crowns and bows, and from behind they read as a
    face): strands down the base layer's back, and on the outer layer the same, down to the nape (a man's) or the whole way (a woman's)."""
    for region, rows in ((HEAD["back"], 8), (HAT["back"], 8 if long_hair else 6)):
        x0, y0, x1, y1 = region
        s.clear(region)
        for x in range(x0 * K, x1 * K):
            strand = 0.82 + 0.3 * rng.random()
            end = rows * K - (0 if long_hair or region is HEAD["back"] else rng.randint(0, 2))
            for y in range(y0 * K, y0 * K + end):
                t = (y - y0 * K) / (8 * K)
                k = strand * (1.12 - 0.3 * t) * (1.0 + (rng.random() - 0.5) * 0.06)
                s.put(x, y, mul(hair, k))


def clean_sides(s, hair):
    """Off-colour things in the hair's outer layer (flowers, bows, horns): turned into hair of their own brightness."""
    for side in ("right", "left", "top"):
        x0, y0, x1, y1 = HAT[side]
        for y in range(y0 * K, y1 * K):
            for x in range(x0 * K, x1 * K):
                r, g, b, a = s.get(x, y)
                if a < 20:
                    continue
                far = sum((v - h) ** 2 for v, h in zip((r, g, b), hair)) ** 0.5
                if far > 110:
                    lum = (r + g + b) / (3 * max(1, sum(hair) / 3))
                    s.put(x, y, mul(hair, max(0.7, min(1.25, lum))), a)


def head_of(name):
    """A HexGhoul villager's head and hair (the skin's head and hat regions), at our size; its shut eyes likewise."""
    src = Image.open(os.path.join(ART, name + ".png")).convert("RGBA")
    closed = Image.open(os.path.join(ART, name + "_closed.png")).convert("RGBA")
    return src, resample(src, 64 * K), resample(closed, 64 * K)


def build(side, look):
    name = HEADS[side][look]
    src, head, closed = head_of(name)
    tone = skin_tone(src)
    s = Skin((0 if side == "pirate" else 100) + look)
    # the head and its hair (both layers) as they are
    for region in (HEAD, HAT):
        for rect in region.values():
            x0, y0, x1, y1 = rect
            s.img.paste(head.crop((x0 * K, y0 * K, x1 * K, y1 * K)), (x0 * K, y0 * K))
    s.px = s.img.load()
    hair = hair_colour(head)
    hair_back(s, hair, LOOKS[look][1] == 1, random.Random(look * 7 + (0 if side == "pirate" else 50)))
    clean_sides(s, hair)
    (pirate if side == "pirate" else marine)(s, look, tone)
    # the shut eyes: only the head's front, from the same face's overlay
    shut = Image.new("RGBA", s.img.size, (0, 0, 0, 0))
    x0, y0, x1, y1 = HEAD["front"]
    shut.paste(closed.crop((x0 * K, y0 * K, x1 * K, y1 * K)), (x0 * K, y0 * K))
    return s.img, shut


def main():
    os.makedirs(OUT, exist_ok=True)
    for side in ("pirate", "marine"):
        for look in range(len(LOOKS)):
            skin, shut = build(side, look)
            skin.save(os.path.join(OUT, f"{side}_{look}.png"))
            shut.save(os.path.join(OUT, f"{side}_{look}_closed.png"))
            print(f"{side}_{look}: {HEADS[side][look]}")


if __name__ == "__main__":
    main()
