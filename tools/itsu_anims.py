#!/usr/bin/env python3
"""3.9.4 (the owner: "Z - Transforms (DIsturbing progressive transformation)"): the human's part of the Itsumade's change, played by
server/ItsuServer as itsumade/ItsuForm's timeline runs (the creature tearing out of the body from tick 4, the human gone at
ItsuForm.HUMAN_GONE 30; going back, the human back at HUMAN_BACK 16):

  itsu_form    the body seized: a jerk forward clutching the chest, wrenched back arched with the arms flung stiff behind, doubled over
               with the arms wrapped round, twisted one way then the other with an arm torn back, then thrown open (the arms wide, the
               back arched as if hauled up by the chest) and shuddering there in small fast jolts until it is gone
  itsu_unform  the human drawn back out of the creature doubled over, clutching itself, shuddering, then slowly straightening

3.9.8, its moves as a human (server/ItsuSaint):

  itsu_stare         Gorosei Stare: the right fist clenched and drawn up before the chest, shaking as the glare fixes on the target,
                     then flicked out at it as it is thrown (STARE_AT 8)
  itsu_presence      Enormous Presence: the chest thrown out, the arms flung down and out from the sides, fists clenched, shuddering
                     with the Haki let out, then easing back
  itsu_blast_charge  Yokai Blast held: the right arm pointed straight ahead, the left hand bracing its forearm, shaking harder
  itsu_blast_fire    let go: the arm kicked up by the beam leaving it, then held out pointing against its push for as long as it pours
  itsu_blast_end     the arm let down
  itsu_death         Thy Death: the right hand raised high to the sky, the body leaning back under it, held while the clouds gather,
                     then let down
  itsu_gate          3.9.9, Hellish Teleport: both hands thrust down before the body, palms to the ground as the pentagram is called
                     up under them, spreading slowly apart while it forms, then let down

Only the body, the torso and the arms are keyed (POINT ONE: no clip keys a leg or the head). Writes src/main/resources/assets/hexhaki/
player_animation/itsu_*.json.

Usage: python3 tools/itsu_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gura_anims import REST  # noqa: E402
import pika_anims  # noqa: E402
from gura_anims import tremble  # noqa: E402
from style_anims import arm, mirror, p  # noqa: E402

PARTS = ("body", "torso", "rightArm", "leftArm")


def body(yaw=0.0, bend=0.0, pitch=0.0):
    return {"yaw": yaw, "bend": bend, "pitch": pitch}


STAND = p(REST, torso={"pitch": 0, "yaw": 0, "roll": 0})
CLUTCH = p(rightArm=arm(-35, 30, 10, -95), leftArm=mirror(arm(-35, 30, 10, -95)), body=body(0, 16, 0), torso={"pitch": 0, "yaw": 0, "roll": 0})
ARCH = p(rightArm=arm(55, 0, 42, 8), leftArm=mirror(arm(55, 0, 42, 8)), body=body(0, 0, 0), torso={"pitch": -20, "yaw": 0, "roll": 0})
DOUBLED = p(rightArm=arm(-48, 48, 6, -110), leftArm=mirror(arm(-48, 48, 6, -110)), body=body(0, 34, 0), torso={"pitch": 0, "yaw": 0, "roll": 0})
TWIST_R = p(rightArm=arm(82, -10, 30, 6), leftArm=mirror(arm(-40, 35, 10, -95)), body=body(28, 14, 0), torso={"pitch": 0, "yaw": 0, "roll": 8})
TWIST_L = p(rightArm=arm(-40, 35, 10, -95), leftArm=mirror(arm(82, -10, 30, 6)), body=body(-30, 12, 0), torso={"pitch": 0, "yaw": 0, "roll": -8})
THROWN = p(rightArm=arm(22, 0, 100, 10), leftArm=mirror(arm(22, 0, 100, 10)), body=body(0, 0, 0), torso={"pitch": -18, "yaw": 0, "roll": 0})


def shudder(q, k, amp):
    """A small fast jolt of the pose (k alternates the side)."""
    s = 1 if k % 2 == 0 else -1
    out = p(q)
    out["body"] = dict(out["body"], yaw=out["body"]["yaw"] + s * amp, bend=out["body"]["bend"] + s * amp * 0.6)
    for side in ("rightArm", "leftArm"):
        a = dict(out[side])
        a["roll"] = a["roll"] + s * amp * 1.4
        a["pitch"] = a["pitch"] - s * amp
        out[side] = a
    return out


def form():
    beats = [(0, "linear", STAND),
             (2, "outexpo", CLUTCH),
             (4, "outexpo", ARCH),
             (7, "outexpo", DOUBLED),
             (10, "outexpo", TWIST_R),
             (13, "outexpo", TWIST_L),
             (16, "outexpo", THROWN)]
    for k, t in enumerate(range(18, 32)):
        beats.append((t, "linear", shudder(THROWN, k, 3.0 + 2.0 * (k % 3))))
    beats.append((34, "linear", THROWN))
    beats.append((40, "inoutsine", STAND))
    pika_anims.write_parts("itsu_form", 40, beats, "Itsumade: the change: the body seized, jerked, arched, doubled over, twisted, thrown"
                           " open and shuddering as the creature tears out of it.", PARTS)


def unform():
    beats = [(0, "linear", DOUBLED),
             (16, "linear", DOUBLED),
             (18, "outexpo", CLUTCH)]
    for k, t in enumerate(range(19, 25)):
        beats.append((t, "linear", shudder(CLUTCH, k, 2.5)))
    beats.append((34, "inoutsine", STAND))
    pika_anims.write_parts("itsu_unform", 34, beats, "Itsumade: back: the human drawn out of the creature doubled over, clutching itself,"
                           " shuddering, slowly straightening.", PARTS)


# ------------------------------------------------------------------------------------------------------ 3.9.8: as a human

TORSO0 = {"pitch": 0, "yaw": 0, "roll": 0}
REST0 = p(STAND, torso=TORSO0)


def stare():
    focus = p(REST0, rightArm=arm(-30, -14, 10, -62), leftArm=arm(10, 0, -12, -14), body=body(10, 5, 0), torso=TORSO0)
    jolt = p(REST0, rightArm=arm(-72, -8, 10, -16), leftArm=arm(18, 0, -16, -10), body=body(6, 10, 0), torso=TORSO0)
    beats = [(0, "linear", REST0), (3, "outsine", focus)]
    beats += tremble(focus, 4, 7, 1, lambda b, sg: p(b, rightArm={"pitch": -30 + 2.0 * sg, "roll": 10 - 1.5 * sg}))
    beats += [(9, "outexpo", jolt), (13, "outsine", p(jolt, rightArm={"pitch": -64, "bend": -24})), (24, "inoutsine", REST0)]
    pika_anims.write_parts("itsu_stare", 24, beats, "Itsumade: Gorosei Stare: the fist clenched before the chest, shaking as the glare fixes"
                           " on its target, flicked out as the target is thrown.", PARTS)


def presence():
    out = p(REST0, rightArm=arm(10, 0, 40, -14), leftArm=mirror(arm(10, 0, 40, -14)), body=body(0, -4, 0),
            torso={"pitch": -12, "yaw": 0, "roll": 0})
    beats = [(0, "linear", REST0), (2, "inexpo", p(REST0, rightArm=arm(-12, 0, 14, -40), leftArm=mirror(arm(-12, 0, 14, -40)),
                                                    body=body(0, 10, 0))),
             (5, "outexpo", out)]
    beats += [(t, "linear", shudder(out, k, 2.0)) for k, t in enumerate(range(6, 18))]
    beats += [(20, "linear", out), (32, "inoutsine", REST0)]
    pika_anims.write_parts("itsu_presence", 32, beats, "Itsumade: Enormous Presence: the chest thrown out, the arms flung down and out,"
                           " shuddering as the Conqueror's Haki pours off the body.", PARTS)


POINTED = p(REST0, rightArm=arm(-90, -10, 6, -2), leftArm=arm(-62, 38, 0, -52), body=body(14, 4, 0), torso=TORSO0)


def blast():
    beats = [(0, "linear", REST0), (4, "outsine", POINTED)]
    beats += tremble(POINTED, 6, 200, 3, lambda b, sg: p(b, rightArm={"pitch": -90 + 1.4 * sg, "yaw": -10 - 0.9 * sg},
                                                          leftArm={"pitch": -62 + 1.4 * sg}))
    pika_anims.write_parts("itsu_blast_charge", 200, beats, "Itsumade: Yokai Blast held: the right arm pointed straight ahead, the left hand"
                           " bracing it, fire gathering in the hand.", PARTS)
    kick = p(POINTED, rightArm=arm(-80, 12, 16, -26), leftArm=arm(-56, 36, 0, -60), body=body(6, -4, 0))
    beats = [(0, "constant", POINTED), (1, "outexpo", kick), (4, "outsine", POINTED)]
    beats += tremble(POINTED, 6, 104, 2, lambda b, sg: p(b, rightArm={"pitch": -88 + 1.0 * sg}, body={"bend": 3 + 0.8 * sg}))
    pika_anims.write_parts("itsu_blast_fire", 104, beats, "Itsumade: Yokai Blast let go: the arm kicked up by the beam, then held out"
                           " against its push while it pours.", PARTS)
    pika_anims.write_parts("itsu_blast_end", 10, [(0, "constant", POINTED), (10, "inoutsine", REST0)],
                           "Itsumade: Yokai Blast done: the arm let down.", PARTS)


def death():
    sky = p(REST0, rightArm=arm(-170, 6, -10, -2), leftArm=arm(-8, 0, -22, -20), body=body(-6, -8, 0),
            torso={"pitch": -8, "yaw": 0, "roll": 0})
    beats = [(0, "linear", REST0), (3, "outback", sky)]
    beats += tremble(sky, 5, 25, 4, lambda b, sg: p(b, rightArm={"pitch": -170 + 1.0 * sg, "roll": -10 - 1.0 * sg}))
    beats += [(28, "insine", p(sky, rightArm={"pitch": -150})), (38, "inoutsine", REST0)]
    pika_anims.write_parts("itsu_death", 38, beats, "Itsumade: Thy Death: the hand raised to the sky, the body leaning back under it while"
                           " the black clouds gather, then let down.", PARTS)


def gate():
    down = p(REST0, rightArm=arm(-52, -16, 10, -10), leftArm=mirror(arm(-52, -16, 10, -10)), body=body(0, 14, 0), torso=TORSO0)
    spread = p(down, rightArm=arm(-46, 8, 26, -8), leftArm=mirror(arm(-46, 8, 26, -8)), body=body(0, 10, 0))
    beats = [(0, "linear", REST0), (4, "outexpo", down)]
    beats += tremble(down, 6, 18, 3, lambda b, sg: p(b, rightArm={"pitch": -52 + 1.2 * sg}, leftArm={"pitch": -52 - 1.2 * sg}))
    beats += [(34, "inoutsine", spread), (40, "linear", spread), (52, "inoutsine", REST0)]
    pika_anims.write_parts("itsu_gate", 52, beats, "Itsumade: Hellish Teleport: both hands thrust down, palms to the ground, the pentagram"
                           " called up under them, spreading slowly apart while it forms.", PARTS)


if __name__ == "__main__":
    form()
    unform()
    stare()
    presence()
    blast()
    death()
    gate()
