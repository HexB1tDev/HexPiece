#!/usr/bin/env python3
"""3.2.4: the Nika fruit's (Gear 5's) animations, reworked (the owner: "The punching animation makes the body wobble too much in the
third person ... The transformation animation is completely broken. It's just busted. It looks bad. It doesn't look badass at all. The
pounding on the chest is cool, but the rest of it is horrible"; asked, the transformation "Shorten to about 4 s"; "make sure that this
fruit feels like a cartoon, like Bugs Bunny"; "The first person is fine ... we don't have to be touching that").

Writes src/main/resources/assets/hexhaki/player_animation/gear5_<move>.json over tools/anim_polish.py's (which leaves OWNED alone):
- gear5_awaken: the transformation in 85 ticks (patch/PiecePatch.gearFast runs its clock twice a tick: every beat of the original's
  170-tick clock lands at half its tick). The drums of liberation pounded on the chest on the five beats (20, 27, 34, 41, 48: the
  server's drums), right, left, right, left, then both; the body going loose like rubber; a deep crouch; the launch at 64 (the server
  throws the body up) with the arms flung up in a star; the float leaning back with the arms wide; the squash landing at 82; Gear 5's
  stance at 85. Cartoon timing: big anticipation, a one-tick hit, an overshooting rebound.
- gear5_pistol, gear5_gatling, gear5_stance: their arms exactly as they were (first person shows them: client HakiAnimations'
  OWN_FIRST_PERSON_ARM), the head never keyed (the look is the player's own: the stance froze it), and the wobble gone (the punch swung
  the body 13 degrees forward then 17 back, twisted the torso 10 each way and turned the head 15; the Gatling twisted both every three
  ticks): the punch leans into the hit once and settles, the Gatling leans in steady with a small twist, the stance keeps its rubbery
  bob without the sideways sway.
No clip keys the head; the transformation keys no leg (the body is held where it stands by the server).

Usage: python3 tools/nika_anims.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gura_anims import write_parts  # noqa: E402
from style_anims import OUT as ANIM_DIR, arm, mirror, p  # noqa: E402

OWNED = {"gear5_awaken", "gear5_pistol", "gear5_gatling", "gear5_stance"}

# the transformation's beats on the real clock (the original's 170-tick clock at double speed: patch/PiecePatch.gearFast)
LENGTH = 85
BURST = 17
BEATS = (20, 27, 34, 41, 48)
LOOSE_END = 53
JUMP = 64
FLOAT_END = 73
LAND = 82

BODY_ARMS = ("body", "torso", "rightArm", "leftArm")


def both(right, **extra):
    """A pose with the right arm {right} and the left its mirror."""
    return p(rightArm=right, leftArm=mirror(right), **extra)


def awaken():
    rest = both(arm(0, 0, 6, -6), body={"bend": 0})
    stagger = both(arm(12, 0, 10, -14), body={"bend": 22, "roll": 3})  # the first heartbeat: the body hunches
    coil = both(arm(34, -10, 44, -70), body={"bend": 34})  # power drawn in: fists clenched low and back
    burst = both(arm(-30, 10, 82, -12), body={"bend": 8})  # it bursts out of him: arms flung wide
    wide = arm(-6, -24, 86, -64)  # a pounding arm wound far out to the side and back (the chest puffed out)
    pound = arm(-30, 32, -40, -110)  # the fist slammed onto the chest, at the sternum
    rebound = arm(-28, 26, -24, -98)
    guard = arm(-22, 10, 38, -72)  # the arm not pounding this beat: low, ready for its own
    beats = [(0, "linear", rest), (5, "outback", stagger), (13, "inoutsine", coil), (BURST, "outexpo", burst)]
    sides = ("right", "left", "right", "left", "both")
    for k, (t, side) in enumerate(zip(BEATS, sides)):
        lean = 14 + 3 * k  # every beat harder
        if side == "both":
            hit = p(rightArm=pound, leftArm=mirror(pound), body={"bend": lean + 8})
            up = p(rightArm=wide, leftArm=mirror(wide), body={"bend": -10})
            back = p(rightArm=rebound, leftArm=mirror(rebound), body={"bend": lean + 2})
        elif side == "right":
            hit = p(rightArm=pound, leftArm=mirror(guard), body={"bend": lean, "roll": -5, "yaw": -6})
            up = p(rightArm=wide, leftArm=mirror(guard), body={"bend": -6, "roll": 4, "yaw": 10})
            back = p(hit, rightArm=rebound)
        else:
            hit = p(rightArm=guard, leftArm=mirror(pound), body={"bend": lean, "roll": 5, "yaw": 6})
            up = p(rightArm=guard, leftArm=mirror(wide), body={"bend": -6, "roll": -4, "yaw": -10})
            back = p(hit, leftArm=mirror(rebound))
        beats.append((t - 3, "outsine", up))
        beats.append((t, "outexpo", hit))
        beats.append((t + 2, "outback", back))
    # loose as rubber: the arms flop, the body wobbles like jelly
    flop = both(arm(18, 0, 14, -4), body={"bend": 14, "roll": 12})
    flop2 = both(arm(10, 0, 22, -10), body={"bend": 10, "roll": -12})
    beats += [(50, "inoutsine", flop), (52, "inoutsine", flop2), (LOOSE_END, "inoutsine", p(flop, body={"bend": 12, "roll": 4}))]
    # the crouch: everything wound down and back (anticipation)
    crouch = both(arm(52, 0, 36, -24), body={"bend": 38})
    beats += [(58, "outsine", crouch), (JUMP - 1, "linear", p(crouch, body={"bend": 42}))]
    # the launch: a star, arched back
    star = both(arm(-160, 0, -30, 0), body={"bend": -18})  # (arm up: a negative roll spreads it out)
    beats.append((JUMP, "outexpo", star))
    # the float: leaning back, arms wide, a lazy rock
    spread = both(arm(-24, 10, 88, -18), body={"bend": -22, "roll": 5})
    spread2 = both(arm(-18, 10, 92, -24), body={"bend": -20, "roll": -5})
    beats += [(JUMP + 4, "outsine", spread), (FLOAT_END - 2, "inoutsine", spread2)]
    # the fall: arms up a little, then the squash landing, then Gear 5's stance
    fall = both(arm(-70, 8, 96, -10), body={"bend": -8})
    squash = both(arm(18, 10, 62, -30), body={"bend": 30})
    stand = both(arm(-12, 8, 26, -24), body={"bend": 2})
    beats += [(LAND - 2, "insine", fall), (LAND, "outexpo", squash), (LENGTH, "outback", stand)]
    write_parts("gear5_awaken", LENGTH, beats, "Nika: Gear 5's awakening in four seconds: the drums of liberation pounded on the chest, "
                "the body loose as rubber, the leap and the landing.", BODY_ARMS)


# ------------------------------------------------------------------------------------------------------ the calmed clips

def load(name):
    with open(os.path.join(ANIM_DIR, name + ".json"), encoding="utf-8") as handle:
        return json.load(handle)


def save(name, data):
    with open(os.path.join(ANIM_DIR, name + ".json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")


def calm(name, tracks, description, keep=None):
    """{name} with every arm track as it is, no head, and its torso and body tracks replaced by {tracks}: {(part, channel): [(tick,
    easing, value)]}; {keep}: (part, channel) tracks of its own kept as they are."""
    data = load(name)
    emote = data["emote"]
    kept = []
    for move in emote["moves"]:
        parts = [k for k in move if isinstance(move[k], dict)]
        if any(part in ("head", "torso", "body") for part in parts):
            part = parts[0]
            channel = next(iter(move[part]))
            if keep and (part, channel) in keep:
                kept.append(move)
            continue
        kept.append(move)
    for (part, channel), frames in tracks.items():
        for tick, easing, value in frames:
            kept.append({"tick": tick, "easing": easing, part: {channel: round(float(value), 3)}})
    kept.sort(key=lambda m: m["tick"])
    emote["moves"] = kept
    data["description"] = description
    data["author"] = "HexPiece 3.2.4"
    save(name, data)


def calmed():
    calm("gear5_pistol", {("body", "bend"): [(0, "linear", 0), (5, "outsine", 7), (8, "outback", 9), (14, "inoutsine", 3),
                                             (22, "inoutsine", 0)]},
         "Nika: the stretching punch (the body leans into it once and settles; the arms as they were).")
    calm("gear5_gatling", {("body", "bend"): [(0, "linear", 7), (6, "linear", 7), (12, "linear", 7)],
                           ("torso", "yaw"): [(0, "inoutsine", 0), (3, "inoutsine", 3), (6, "inoutsine", 0), (9, "inoutsine", -3),
                                              (12, "inoutsine", 0)]},
         "Nika: the Gatling (leaning in, a small twist with each fist; the arms as they were).")
    stance = load("gear5_stance")
    torso_roll = [(m["tick"], m.get("easing", "linear"), m["torso"]["roll"] * 0.5) for m in stance["emote"]["moves"]
                  if "torso" in m and "roll" in m["torso"]]
    calm("gear5_stance", {("torso", "roll"): torso_roll},
         "Nika: Gear 5's bouncy stance (the rubbery bob; the head the player's own).", keep={("body", "bend"), ("body", "y"), ("torso", "yaw")})


def main():
    awaken()
    calmed()
    print("wrote the Nika fruit's animations: " + ", ".join(sorted(OWNED)))


if __name__ == "__main__":
    main()
