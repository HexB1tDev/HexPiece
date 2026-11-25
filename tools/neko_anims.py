#!/usr/bin/env python3
"""3.7.9 (the owner: "Neko Neko No Mi ... We need Idle, running, jumping, transforming and de-transforming animations"; "first of all, Z
(transformation, get the base animations, and m1s.)"): the human's clips of the Leopard fruit (the leopard's own are the owner's model's
takes: neko/LeopardForm).

- neko_form: the reference's Transform (Leov2Transform): the human slouches down, hunched forward, the arms hanging, the knees bent, as
  the black flames take them; the roar: thrown upright, the chest out, the arms flung out and back (the leopard burns into being over
  it: neko/LeopardForm.FORM_*).
- neko_unform: the human standing back up out of the flames (from a crouch).
- neko_m1_1 to neko_m1_5: Six Styles (the reference's SixStylesBase, read frame by frame: docs/handoff/next-ai-3.7.6.md): a snapping
  front kick of the right leg; a claw swipe of the right hand across, low to high; a roundhouse kick of the left leg sweeping across; a
  double punch (left then right); Rokuogan: both fists drawn to the hip and thrust out together, knuckles forward, from a deep stance.
  The blows land at server/NekoMoves.M1_HIT ticks (3).

Writes src/main/resources/assets/hexhaki/player_animation/neko_*.json. No clip keys the head; only the limbs a clip uses
(tools/pika_anims.write_parts).

Usage: python3 tools/neko_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import pika_anims  # noqa: E402
from gura_anims import REST  # noqa: E402
from style_anims import arm, leg, mirror, p  # noqa: E402

# (tools/anim.skeleton: the body's pitch + leans it back, its bend + forward; an arm's pitch + back (- forward, up), a leg's - forward)
STAND = p(REST, rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))
ALL = ("body", "torso", "rightArm", "leftArm", "rightLeg", "leftLeg")
ARMS = ("body", "torso", "rightArm", "leftArm")


def form():
    """Z: the slouch (0 to 10), held hunched and trembling while the flames take the body (to 22), the roar (22 to 30), held, eased."""
    slouch = p(rightArm=arm(8, 0, 12, -10), leftArm=mirror(arm(8, 0, 12, -10)), rightLeg=leg(-26, 3, 42), leftLeg=leg(-18, -3, 36),
               body={"yaw": 0, "bend": 34, "pitch": -6})
    deeper = p(slouch, rightArm=arm(14, 0, 16, -14), leftArm=mirror(arm(14, 0, 16, -14)), body={"yaw": 0, "bend": 40, "pitch": -8})
    roar = p(rightArm=arm(24, -18, 70, -20), leftArm=mirror(arm(24, -18, 70, -20)), rightLeg=leg(-10, 6, 14), leftLeg=leg(6, -6, 10),
             body={"yaw": 0, "bend": -14, "pitch": 6})
    pika_anims.write_parts("neko_form", 40, [
        (0, "linear", STAND),
        (10, "outsine", slouch),
        (16, "inoutsine", deeper),
        (20, "inoutsine", slouch),
        (22, "linear", deeper),
        (25, "outexpo", roar),
        (32, "linear", roar),
        (40, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: the transformation: slouched down, hunched, as the black flames take the body; the roar.", ALL)


def unform():
    """Going back: crouched in the flames, standing up out of them."""
    crouch = p(rightArm=arm(10, 0, 14, -12), leftArm=mirror(arm(10, 0, 14, -12)), rightLeg=leg(-30, 3, 50), leftLeg=leg(-22, -3, 44),
               body={"yaw": 0, "bend": 30, "pitch": -6})
    pika_anims.write_parts("neko_unform", 26, [
        (0, "linear", crouch),
        (12, "linear", crouch),
        (24, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: changing back: crouched in the black flames, standing up out of them.", ALL)


def m1():
    """Six Styles, the human's five blows (each 10 ticks; the blow lands at tick 3)."""
    guard = p(rightArm=arm(-30, 0, 18, -70), leftArm=mirror(arm(-30, 0, 18, -70)), body={"yaw": 0, "bend": 6})
    # 1: a snapping front kick of the right leg
    chamber = p(guard, rightLeg=leg(-70, 0, 80), leftLeg=leg(4, 0, 10), body={"yaw": 0, "bend": -4, "pitch": 4})
    kick = p(guard, rightLeg=leg(-95, 0, 0), leftLeg=leg(6, 0, 12), body={"yaw": 0, "bend": -10, "pitch": 10})
    pika_anims.write_parts("neko_m1_1", 10, [
        (0, "linear", p(guard, rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))),
        (2, "outsine", chamber),
        (3, "outexpo", kick),
        (5, "linear", kick),
        (10, "inoutsine", p(STAND, rightArm=arm(0, 0, 4, -4))),
    ], "Neko Neko, Model: Leopard: Six Styles 1: a snapping front kick.", ALL)
    # 2: a claw swipe of the right hand, low on the left to high on the right
    wind = p(rightArm=arm(-40, 60, -10, -30), leftArm=mirror(arm(-30, 0, 18, -70)), body={"yaw": 30, "bend": 10})
    swipe = p(rightArm=arm(-110, -50, 40, -6), leftArm=mirror(arm(-20, 0, 24, -60)), body={"yaw": -28, "bend": 4})
    pika_anims.write_parts("neko_m1_2", 10, [
        (0, "linear", guard),
        (2, "outsine", wind),
        (3, "outexpo", swipe),
        (5, "linear", swipe),
        (10, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: Six Styles 2: a claw swipe across, low to high.", ARMS)
    # 3: a roundhouse kick of the left leg sweeping across in front
    lift = p(guard, leftLeg=leg(-50, -40, 60), rightLeg=leg(4, 4, 12), body={"yaw": -20, "bend": 0, "pitch": 6})
    sweep = p(guard, leftLeg=leg(-80, 20, 6), rightLeg=leg(6, 6, 14), body={"yaw": 30, "bend": -6, "pitch": 12})
    pika_anims.write_parts("neko_m1_3", 10, [
        (0, "linear", p(guard, rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))),
        (2, "outsine", lift),
        (3, "outexpo", sweep),
        (5, "linear", sweep),
        (10, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: Six Styles 3: a roundhouse kick sweeping across.", ALL)
    # 4: a double punch: the left, then the right
    left = p(rightArm=arm(-30, 0, 18, -70), leftArm=mirror(arm(-90, 0, 6, -4)), body={"yaw": 22, "bend": 6})
    right = p(rightArm=arm(-90, 0, 6, -4), leftArm=mirror(arm(-30, 0, 18, -70)), body={"yaw": -22, "bend": 8})
    pika_anims.write_parts("neko_m1_4", 10, [
        (0, "linear", guard),
        (1, "outexpo", left),
        (3, "outexpo", right),
        (5, "linear", right),
        (10, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: Six Styles 4: a double punch, left then right.", ARMS)
    # 5: Rokuogan: both fists drawn to the right hip, thrust out together from a deep stance
    draw = p(rightArm=arm(30, 0, 20, -90), leftArm=mirror(arm(10, -30, 30, -90)), rightLeg=leg(10, 4, 30), leftLeg=leg(-40, -4, 40),
             body={"yaw": -30, "bend": 10})
    thrust = p(rightArm=arm(-88, 8, 0, -2), leftArm=mirror(arm(-88, 8, 0, -2)), rightLeg=leg(18, 4, 20), leftLeg=leg(-46, -4, 36),
               body={"yaw": 0, "bend": 16, "pitch": -4})
    pika_anims.write_parts("neko_m1_5", 16, [
        (0, "linear", p(guard, rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))),
        (3, "outsine", draw),
        (5, "outexpo", thrust),
        (9, "linear", thrust),
        (16, "inoutsine", STAND),
    ], "Neko Neko, Model: Leopard: Six Styles 5: Rokuogan: both fists thrust out together from a deep stance.", ALL)


def main():
    form()
    unform()
    m1()
    print("neko clips:", ", ".join(sorted(k for k in pika_anims.WRITTEN if k.startswith("neko_"))))


if __name__ == "__main__":
    main()
