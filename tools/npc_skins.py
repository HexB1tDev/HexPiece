#!/usr/bin/env python3
"""2.1.1: the pirates' and marines' skins (64 x 64 player skins), painted from code.

Writes src/main/resources/assets/hexhaki/textures/piece/npc/<side>_<n>.png and prints the looks table SeaRules.LOOKS mirrors
(side, rank, female). Grunts: two men and two women per side; officers: a man and a woman; captains: a man and a woman.
Pirates: bandanas, striped shirts, open vests, sashes, boots, beards; their captain a long red coat, a black hat with a skull.
Marines: white uniform, blue collar and scarf, the white cap with a blue band; officers a white coat over the shoulders; their captain
the coat with gold epaulettes and a peaked cap.
Usage: python3 tools/npc_skins.py
"""
import os
import random
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/textures/piece/npc")

# Regions of a 64 x 64 skin: part -> {face: (x0, y0, x1, y1)}; outer layers listed separately.
HEAD = {"top": (8, 0, 16, 8), "bottom": (16, 0, 24, 8), "right": (0, 8, 8, 16), "front": (8, 8, 16, 16), "left": (16, 8, 24, 16),
        "back": (24, 8, 32, 16)}
HAT = {k: (a + 32, b, c + 32, d) for k, (a, b, c, d) in HEAD.items()}
BODY = {"top": (20, 16, 28, 20), "bottom": (28, 16, 36, 20), "right": (16, 20, 20, 32), "front": (20, 20, 28, 32), "left": (28, 20, 32, 32),
        "back": (32, 20, 40, 32)}
JACKET = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in BODY.items()}
R_ARM = {"top": (44, 16, 48, 20), "bottom": (48, 16, 52, 20), "right": (40, 20, 44, 32), "front": (44, 20, 48, 32), "left": (48, 20, 52, 32),
         "back": (52, 20, 56, 32)}
L_ARM = {"top": (36, 48, 40, 52), "bottom": (40, 48, 44, 52), "right": (32, 52, 36, 64), "front": (36, 52, 40, 64), "left": (40, 52, 44, 64),
         "back": (44, 52, 48, 64)}
R_SLEEVE = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in R_ARM.items()}
L_SLEEVE = {k: (a + 16, b, c + 16, d) for k, (a, b, c, d) in L_ARM.items()}
R_LEG = {"top": (4, 16, 8, 20), "bottom": (8, 16, 12, 20), "right": (0, 20, 4, 32), "front": (4, 20, 8, 32), "left": (8, 20, 12, 32),
         "back": (12, 20, 16, 32)}
L_LEG = {"top": (20, 48, 24, 52), "bottom": (24, 48, 28, 52), "right": (16, 52, 20, 64), "front": (20, 52, 24, 64), "left": (24, 52, 28, 64),
         "back": (28, 52, 32, 64)}
R_PANTS = {k: (a, b + 16, c, d + 16) for k, (a, b, c, d) in R_LEG.items()}
L_PANTS = {k: (a - 16, b, c - 16, d) for k, (a, b, c, d) in L_LEG.items()}


def shade(colour, amount):
    return tuple(max(0, min(255, int(c * amount))) for c in colour[:3]) + (255,)


class Skin:
    def __init__(self, seed):
        self.img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        self.px = self.img.load()
        self.rand = random.Random(seed)

    def rect(self, box, colour, noise=0.06):
        x0, y0, x1, y1 = box
        for y in range(y0, y1):
            for x in range(x0, x1):
                n = 1.0 + (self.rand.random() - 0.5) * noise
                self.px[x, y] = shade(colour, n)

    def part(self, faces, colour, noise=0.06):
        for face, box in faces.items():
            self.rect(box, colour if face != "bottom" else shade(colour, 0.8), noise)

    def rows(self, faces, y_from, y_to, colour, noise=0.05):
        """Colour rows y_from..y_to (counted from each side face's top) on the four sides of a part."""
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = faces[face]
            self.rect((x0, y0 + y_from, x1, min(y1, y0 + y_to)), colour, noise)

    def stripes(self, faces, colour, every=2, start=0, until=12):
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = faces[face]
            for y in range(y0 + start, min(y1, y0 + until), every):
                for x in range(x0, x1):
                    self.px[x, y] = shade(colour, 1.0 + (self.rand.random() - 0.5) * 0.05)

    def dot(self, x, y, colour):
        self.px[x, y] = colour if len(colour) == 4 else colour + (255,)


SKINS = [(232, 190, 150), (196, 140, 100), (150, 100, 70), (110, 72, 50)]
HAIRS = [(40, 28, 20), (90, 55, 30), (160, 110, 60), (20, 20, 24), (180, 60, 30)]


def face(s, skin, hair, female, eyes=(40, 40, 60)):
    s.part(HEAD, skin)
    s.rect(HEAD["top"], hair)
    s.rows(HEAD, 0, 2 if not female else 3, hair)
    s.rect((24, 8, 32, 16), hair)  # back of the head: hair
    if female:
        s.rect((0, 8, 2, 16), hair)
        s.rect((22, 8, 24, 16), hair)
        s.rect((24, 8, 32, 16), hair)
    fx, fy = 8, 8
    for ex in (fx + 1, fx + 5):
        s.dot(ex, fy + 4, (250, 250, 250))
        s.dot(ex + 1, fy + 4, eyes)
    s.dot(fx + 3, fy + 6, shade(skin, 0.8))
    s.dot(fx + 4, fy + 6, shade(skin, 0.8))
    if female:
        for x in (fx + 3, fx + 4):
            s.dot(x, fy + 6, (170, 70, 80))
        s.dot(fx + 1, fy + 3, shade(hair, 1.0))
        s.dot(fx + 6, fy + 3, shade(hair, 1.0))


def pirate(index, rank, female, seed):
    s = Skin(seed)
    skin = SKINS[(index + seed) % len(SKINS)]
    hair = HAIRS[(index * 3 + seed) % len(HAIRS)]
    face(s, skin, hair, female)
    shirt_colours = [(220, 220, 210), (200, 60, 60), (60, 90, 160), (230, 200, 120)]
    shirt = shirt_colours[(index + seed) % 4]
    s.part(BODY, shirt)
    s.part(R_ARM, skin)
    s.part(L_ARM, skin)
    s.rows(R_ARM, 0, 4, shirt)
    s.rows(L_ARM, 0, 4, shirt)
    if index % 2 == 0:
        s.stripes(BODY, (40, 50, 90), 2, 1)
        s.stripes(R_ARM, (40, 50, 90), 2, 1, 4)
        s.stripes(L_ARM, (40, 50, 90), 2, 1, 4)
    trousers = [(80, 60, 40), (50, 50, 60), (110, 90, 60)][(index + seed) % 3]
    s.part(R_LEG, trousers)
    s.part(L_LEG, trousers)
    s.rows(R_LEG, 8, 12, (50, 32, 20))
    s.rows(L_LEG, 8, 12, (50, 32, 20))
    sash = (170, 30, 40) if index % 3 != 1 else (200, 160, 40)
    s.rows(BODY, 9, 11, sash)
    if rank >= 1 or index % 2 == 1:
        vest = (70, 45, 30) if rank == 0 else (40, 30, 30)
        x0, y0, x1, y1 = BODY["front"]
        s.rect((x0, y0, x0 + 2, y0 + 9), vest)
        s.rect((x1 - 2, y0, x1, y0 + 9), vest)
        s.rect(BODY["back"][:2] + (BODY["back"][2], BODY["back"][1] + 9), vest)
        s.rect((BODY["right"][0], BODY["right"][1], BODY["right"][2], BODY["right"][1] + 9), vest)
        s.rect((BODY["left"][0], BODY["left"][1], BODY["left"][2], BODY["left"][1] + 9), vest)
    if rank == 2:
        coat = (150, 25, 30)
        for faces in (JACKET,):
            s.part(faces, coat)
            fx0, fy0, fx1, fy1 = faces["front"]
            for y in range(fy0, fy1):
                for x in range(fx0 + 2, fx1 - 2):
                    s.px[x, y] = (0, 0, 0, 0)
        s.part(R_SLEEVE, coat)
        s.part(L_SLEEVE, coat)
        s.rows(R_SLEEVE, 9, 12, (230, 200, 90))
        s.rows(L_SLEEVE, 9, 12, (230, 200, 90))
        # the captain's hat: black, gold trim, a white skull at the front
        s.part(HAT, (25, 25, 28))
        for k, (x0, y0, x1, y1) in HAT.items():
            if k not in ("top", "bottom"):
                for y in range(y0 + 3, y1):
                    for x in range(x0, x1):
                        s.px[x, y] = (0, 0, 0, 0)
                for x in range(x0, x1):
                    s.px[x, y0 + 2] = (220, 180, 70, 255)
        hx = HAT["front"][0]
        for x, y in ((hx + 3, 9), (hx + 4, 9), (hx + 3, 10), (hx + 4, 10)):
            s.px[x, y] = (240, 240, 235, 255)
    elif index % 2 == 0 or female:
        band = [(200, 40, 40), (40, 70, 160), (30, 30, 30)][(index + seed) % 3]
        for k, (x0, y0, x1, y1) in HAT.items():
            if k == "bottom":
                continue
            for y in range(y0, y1 if k == "top" else y0 + 3):
                for x in range(x0, x1):
                    s.px[x, y] = shade(band, 1.0 + (s.rand.random() - 0.5) * 0.08)
    if not female and rank >= 1 or (not female and index == 1):
        beard = shade(hair, 0.9)
        x0, y0, _, _ = HEAD["front"]
        for x in range(x0, x0 + 8):
            s.px[x, y0 + 7] = beard
        for x in (x0, x0 + 1, x0 + 6, x0 + 7):
            s.px[x, y0 + 6] = beard
    if female:
        s.rows(BODY, 0, 1, skin)
    return s.img


def marine(index, rank, female, seed):
    s = Skin(seed)
    skin = SKINS[(index * 2 + seed) % len(SKINS)]
    hair = HAIRS[(index + seed) % len(HAIRS)]
    face(s, skin, hair, female, (30, 50, 90))
    white = (236, 240, 245)
    blue = (40, 80, 160)
    s.part(BODY, white)
    s.part(R_ARM, white)
    s.part(L_ARM, white)
    s.part(R_LEG, (60, 80, 140) if rank == 0 else (230, 232, 236))
    s.part(L_LEG, (60, 80, 140) if rank == 0 else (230, 232, 236))
    s.rows(R_LEG, 10, 12, (30, 30, 34))
    s.rows(L_LEG, 10, 12, (30, 30, 34))
    s.rows(R_ARM, 10, 12, skin)
    s.rows(L_ARM, 10, 12, skin)
    # the sailor collar and scarf
    x0, y0, x1, y1 = BODY["front"]
    for x in range(x0, x1):
        s.px[x, y0] = shade(blue, 1.0)
    for i in range(3):
        s.px[x0 + 3 - i + 0, y0 + 1 + i] = shade(blue, 1.0)
        s.px[x0 + 4 + i, y0 + 1 + i] = shade(blue, 1.0)
    s.px[x0 + 3, y0 + 4] = (200, 40, 40, 255)
    s.px[x0 + 4, y0 + 4] = (200, 40, 40, 255)
    s.rect(BODY["back"][:2] + (BODY["back"][2], BODY["back"][1] + 3), blue)
    s.rows(BODY, 9, 10, (40, 40, 50))
    # the cap: white with a blue band
    for k, (hx0, hy0, hx1, hy1) in HAT.items():
        if k == "bottom":
            continue
        rows = range(hy0, hy1) if k == "top" else range(hy0, hy0 + 3)
        for y in rows:
            for x in range(hx0, hx1):
                band = k != "top" and y == hy0 + 2
                s.px[x, y] = shade(blue if band else white, 1.0 + (s.rand.random() - 0.5) * 0.04)
    if rank >= 1:
        coat = (245, 246, 248)
        s.part(JACKET, coat, 0.03)
        fx0, fy0, fx1, fy1 = JACKET["front"]
        for y in range(fy0, fy1):
            for x in range(fx0 + 2, fx1 - 2):
                s.px[x, y] = (0, 0, 0, 0)
        bx0, by0, bx1, by1 = JACKET["back"]
        # "JUSTICE" as a dark band of letters across the coat's back
        for x in range(bx0 + 1, bx1 - 1):
            if (x - bx0) % 2 == 1:
                s.px[x, by0 + 3] = (30, 30, 40, 255)
                s.px[x, by0 + 4] = (30, 30, 40, 255)
        s.part(R_SLEEVE, coat, 0.03)
        s.part(L_SLEEVE, coat, 0.03)
        if rank == 2:
            for sleeve in (R_SLEEVE, L_SLEEVE):
                sx0, sy0, sx1, _ = sleeve["top"]
                for x in range(sx0, sx1):
                    for y in range(sy0, sy0 + 4):
                        s.px[x, y] = (225, 185, 70, 255)
                s.rows(sleeve, 0, 1, (225, 185, 70))
            # the peaked cap: the band gold
            for k, (hx0, hy0, hx1, hy1) in HAT.items():
                if k not in ("top", "bottom"):
                    for x in range(hx0, hx1):
                        s.px[x, hy0 + 2] = (225, 185, 70, 255)
    if not female and rank == 2:
        x0, y0, _, _ = HEAD["front"]
        for x in range(x0 + 2, x0 + 6):
            s.px[x, y0 + 5] = shade(hair, 0.9)
    return s.img


LOOKS = []


def main():
    os.makedirs(OUT, exist_ok=True)
    plan = [(0, False), (0, False), (0, True), (0, True), (1, False), (1, True), (2, False), (2, True)]
    for side, painter in (("pirate", pirate), ("marine", marine)):
        for n, (rank, female) in enumerate(plan):
            img = painter(n, rank, female, 7 + n * 13 + (0 if side == "pirate" else 101))
            img.save(os.path.join(OUT, f"{side}_{n}.png"))
            LOOKS.append((side, n, rank, female))
    print("looks (index, rank, female):", [(n, r, f) for s, n, r, f in LOOKS if s == "pirate"])


if __name__ == "__main__":
    main()
