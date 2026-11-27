#!/usr/bin/env python3
"""3.9.4 (the owner: "Z - Transforms (DIsturbing progressive transformation)"): the human's part of the Itsumade's change, played by
server/ItsuServer as itsumade/ItsuForm's timeline runs (the creature tearing out of the body from tick 4, the human gone at
ItsuForm.HUMAN_GONE 30; going back, the human back at HUMAN_BACK 16):

  itsu_form    the body seized: a jerk forward clutching the chest, wrenched back arched with the arms flung stiff behind, doubled over
               with the arms wrapped round, twisted one way then the other with an arm torn back, then thrown open (the arms wide, the
               back arched as if hauled up by the chest) and shuddering there in small fast jolts until it is gone
  itsu_unform  the human drawn back out of the creature doubled over, clutching itself, shuddering, then slowly straightening

Only the body, the torso and the arms are keyed (POINT ONE: no clip keys a leg or the head). Writes src/main/resources/assets/hexhaki/
player_animation/itsu_form.json and itsu_unform.json.

Usage: python3 tools/itsu_anims.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gura_anims import REST  # noqa: E402
import pika_anims  # noqa: E402
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


if __name__ == "__main__":
    form()
    unform()
