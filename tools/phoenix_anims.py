#!/usr/bin/env python3
"""3.7.0 (the owner: "improve it so the players body slowly becomes the phoenix, arms become the wings and etc."): the Tori Tori no
Mi, Model: Phoenix's change: the arms rising out to the sides as wings while the fire runs up them (phoenix_form: out level by tick 10,
up a little past level by 15, held while the human is fire and then the phoenix); going, held out while the phoenix closes back into
the human, let down by 26 (phoenix_unform). phoenix/PhoenixForm.armRaise is the same beats (the figure of fire's arms follow it).

Writes src/main/resources/assets/hexhaki/player_animation/phoenix_form.json and phoenix_unform.json. No clip keys a leg or the head
(tools/gura_anims.write_parts).

Usage: python3 tools/phoenix_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gura_anims import REST, write_parts  # noqa: E402
from style_anims import arm, mirror, p  # noqa: E402

LEVEL = p(rightArm=arm(-6, 0, 88, -6), leftArm=mirror(arm(-6, 0, 88, -6)), body={"yaw": 0, "bend": -3})
WINGS = p(rightArm=arm(-8, 0, 102, -8), leftArm=mirror(arm(-8, 0, 102, -8)), body={"yaw": 0, "bend": -7})
PARTS = ("body", "torso", "rightArm", "leftArm")


def main():
    write_parts("phoenix_form", 40, [
        (0, "linear", REST),
        (10, "outsine", LEVEL),
        (15, "inoutsine", WINGS),
        (40, "linear", WINGS),
    ], "Tori Tori, Model: Phoenix: the arms rise out to the sides as wings while the blue fire runs up them.", PARTS)
    write_parts("phoenix_unform", 26, [
        (0, "linear", WINGS),
        (16, "inoutsine", LEVEL),
        (26, "inoutsine", REST),
    ], "Tori Tori, Model: Phoenix: the arms held out as the phoenix closes back into the human, then let down.", PARTS)
    print("wrote phoenix_form and phoenix_unform")


if __name__ == "__main__":
    main()
