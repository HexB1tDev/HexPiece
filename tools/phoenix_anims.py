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
import pika_anims  # noqa: E402
from style_anims import arm, leg, mirror, p  # noqa: E402

LEVEL = p(rightArm=arm(-6, 0, 88, -6), leftArm=mirror(arm(-6, 0, 88, -6)), body={"yaw": 0, "bend": -3})
WINGS = p(rightArm=arm(-8, 0, 102, -8), leftArm=mirror(arm(-8, 0, 102, -8)), body={"yaw": 0, "bend": -7})
PARTS = ("body", "torso", "rightArm", "leftArm")


STAND = p(REST, rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))
ALL = ("body", "torso", "rightArm", "leftArm", "rightLeg", "leftLeg")


# (tools/anim.skeleton: the body's pitch + leans it back, its bend + forward; an arm's pitch + back, a leg's - forward)


def fuji():
    """3.7.2: X Fujiazami (phoenix/PhoenixMoveArt.FUJI_*: the charge 8 ticks, the dash 6, the throw at 14): crouched into a lunge, leaning
    forward, the arms swept back like wings while the fire gathers at the feet; driven forward low, the arms swept further back, the legs
    trailing; the arms flung forward with the throw; let back up."""
    charge = p(rightArm=arm(55, 0, 25, -8), leftArm=mirror(arm(55, 0, 25, -8)), rightLeg=leg(-25, 2, 40), leftLeg=leg(20, -2, 25),
               body={"yaw": 0, "bend": 12, "pitch": -4})
    dash = p(rightArm=arm(70, 0, 30, -4), leftArm=mirror(arm(70, 0, 30, -4)), rightLeg=leg(12, 0, 30), leftLeg=leg(34, 0, 18),
             body={"yaw": 0, "bend": 18, "pitch": -12})
    throw = p(rightArm=arm(-82, 0, 12, -4), leftArm=mirror(arm(-82, 0, 12, -4)), rightLeg=leg(-12, 2, 14), leftLeg=leg(14, -2, 10),
              body={"yaw": 0, "bend": 6, "pitch": -4})
    pika_anims.write_parts("phoenix_fuji", 32, [
        (0, "linear", STAND),
        (6, "outsine", charge),
        (8, "linear", charge),
        (10, "outexpo", dash),
        (14, "linear", dash),
        (16, "outexpo", throw),
        (22, "linear", throw),
        (32, "inoutsine", STAND),
    ], "Tori Tori, Model: Phoenix: Fujiazami: crouched into a lunge, the arms swept back like wings, the dash, the arms flung out with"
       " the throw.", ALL)


def talon():
    """3.7.2: C Talon Slam (phoenix/PhoenixMoveArt.TALON_*: crouch 4, rise 12, hang 8, the slam about 4 later): the crouch; the leap, the
    arms thrown up; at the top the right leg raised high in front (PhoenixMoveArt.talonLeg: -20 by tick 10, -120 by 22, +20 by 28),
    the arms out for balance; the leg driven down with the slam; the landing crouch; up."""
    crouch = p(rightArm=arm(30, 0, 15, -20), leftArm=mirror(arm(30, 0, 15, -20)), rightLeg=leg(-30, 3, 60), leftLeg=leg(-30, -3, 60),
               body={"yaw": 0, "bend": 14, "pitch": -6})
    leap = p(rightArm=arm(-135, 0, 42, -6), leftArm=mirror(arm(-135, 0, 42, -6)), rightLeg=leg(-20, 2, 20), leftLeg=leg(10, -2, 30),
             body={"yaw": 0, "bend": -4, "pitch": 0})
    raised = p(rightArm=arm(-20, 0, 80, -6), leftArm=mirror(arm(-20, 0, 80, -6)), rightLeg=leg(-120, 4, 0), leftLeg=leg(12, -2, 40),
               body={"yaw": 0, "bend": -6, "pitch": 4})
    slam = p(rightArm=arm(40, 0, 30, -10), leftArm=mirror(arm(40, 0, 30, -10)), rightLeg=leg(20, 2, 6), leftLeg=leg(-10, -2, 50),
             body={"yaw": 0, "bend": 16, "pitch": -8})
    land = p(rightArm=arm(35, 0, 20, -24), leftArm=mirror(arm(35, 0, 20, -24)), rightLeg=leg(-28, 3, 66), leftLeg=leg(-28, -3, 66),
             body={"yaw": 0, "bend": 18, "pitch": -8})
    pika_anims.write_parts("phoenix_talon", 44, [
        (0, "linear", STAND),
        (4, "outsine", crouch),
        (8, "outexpo", leap),
        (10, "linear", p(leap, rightLeg={"pitch": -20})),
        (22, "inoutsine", raised),
        (24, "linear", raised),
        (28, "inexpo", slam),
        (31, "outsine", land),
        (34, "linear", land),
        (44, "inoutsine", STAND),
    ], "Tori Tori, Model: Phoenix: Talon Slam: the crouch, the leap, the right leg raised high and lit, driven down in the slam, the"
       " landing.", ALL)


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
    fuji()
    talon()
    print("wrote phoenix_form, phoenix_unform, phoenix_fuji and phoenix_talon")


if __name__ == "__main__":
    main()
