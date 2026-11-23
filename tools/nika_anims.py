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
from style_anims import OUT as ANIM_DIR, PARTS, WRITTEN, arm, leg, mirror, p  # noqa: E402

OWNED = {"gear5_awaken", "gear5_pistol", "gear5_gatling", "gear5_stance", "gear5_bajrang_charge", "gear5_bajrang_slam", "gear5_dawn_whip",
         "gear5_bounce", "gear5_jump", "gear5_land", "gear5_dawn_pistol", "gear5_dawn_pistol_left"}

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


# ------------------------------------------------------------------------------------------------------ 3.2.6: the Bajrang Gun

# gear/NikaSlam's very numbers (HudTests.nika checks the clip against them): ticks into the slam, the fist's way in the world (0 up,
# 90 ahead, 180 down) and the body's pitch (forward positive); the arm's own pitch is theta - 180 - pitch, so the fist the client draws
# (client/gear/NikaFist) sits where the arm points at every moment
SLAM_T = (0, 4, 10, 13, 16, 116, 142)
SLAM_THETA = (-8, -30, 165, 180, 180, 180, 180)
SLAM_PITCH = (-12, -22, 55, 72, 70, 70, 10)
SLAM_LENGTH = 142
CHARGE_PITCH = -12
BAJRANG_PARTS = {"body": ("pitch", "bend"), "rightArm": ("pitch", "yaw", "roll", "bend"), "leftArm": ("pitch", "yaw", "roll", "bend"),
                 "rightLeg": ("pitch", "yaw", "roll", "bend"), "leftLeg": ("pitch", "yaw", "roll", "bend")}


def write_clip(name, duration, beats, description, parts, loop=False, return_tick=0):
    """{parts}: {part: channels} keyed at every beat (never the head; the body by its pitch: the whole body turning in the air)."""
    assert "head" not in parts, name + ": the head is the player's own"
    moves = []
    for tick, easing, q in beats:
        for part, channels in parts.items():
            values = q.get(part, {})
            for channel in channels:
                moves.append({"tick": tick, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    moves.sort(key=lambda m: m["tick"])
    data = {"version": 3, "name": name, "author": "HexPiece 3.2.6", "description": description,
            "emote": {"beginTick": 0, "endTick": duration, "stopTick": duration + 3, "isLoop": loop, "returnTick": return_tick,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    WRITTEN[name] = data
    save(name, data)


def bajrang():
    """The Bajrang Gun (the owner: "it should be coming first from above, and then the player slowly rotates it downwards and slams it
    down ... rotate it mid-air to like, actually aim down instead of just standing still in the arm, just doing all the work")."""
    balance = mirror(arm(-40, 10, 70, -20))
    # PlayerAnimator's body pitch leans BACK when positive (the handoff's 2.9.7 note): NikaSlam's forward-positive pitch goes in negated
    held = p(body={"pitch": -CHARGE_PITCH}, rightArm=arm(-176, 0, -6, -4), leftArm=balance, rightLeg=leg(-36, 0, 52), leftLeg=leg(8, 0, 12))
    heave = p(held, body={"pitch": -(CHARGE_PITCH - 3)}, rightArm=arm(-170, 0, -8, -10), rightLeg=leg(-40, 0, 58))
    write_clip("gear5_bajrang_charge", 26, [(0, "inoutsine", held), (13, "inoutsine", heave), (26, "inoutsine", held)],
               "Nika: the Bajrang Gun charging: high in the air, leaning back, the giant fist held straight up.", BAJRANG_PARTS,
               loop=True, return_tick=0)
    others = [
        (balance, leg(-36, 0, 52), leg(8, 0, 12)),            # still as it charged
        (mirror(arm(-70, 0, 84, -30)), leg(24, 0, 46), leg(12, 0, 30)),  # wound back: arched, legs kicked back
        (mirror(arm(30, 0, 60, -10)), leg(14, 0, 12), leg(-6, 0, 24)),   # the swing: the free arm flung back, legs trailing
        (mirror(arm(42, 0, 30, -8)), leg(10, 0, 10), leg(-8, 0, 20)),    # head-first, straight down
        (mirror(arm(42, 0, 30, -8)), leg(10, 0, 10), leg(-8, 0, 20)),
        (mirror(arm(42, 0, 30, -8)), leg(10, 0, 10), leg(-8, 0, 20)),    # held through the impact
        (mirror(arm(0, 0, 8, -6)), leg(0, 0, 0), leg(0, 0, 0)),          # the arm pulled in, upright again
    ]
    beats = []
    for k, t in enumerate(SLAM_T):
        free, right_leg, left_leg = others[k]
        right = arm(SLAM_THETA[k] - 180.0 - SLAM_PITCH[k], 0, 0, 0)
        beats.append((t, "inoutsine", p(body={"pitch": -SLAM_PITCH[k]}, rightArm=right, leftArm=free, rightLeg=right_leg, leftLeg=left_leg)))
    write_clip("gear5_bajrang_slam", SLAM_LENGTH, beats, "Nika: the Bajrang Gun: wound back overhead, swung over the front as the body turns "
               "head-first after it, slammed straight down and held there.", BAJRANG_PARTS)


# ------------------------------------------------------------------------------------------------------ 3.3.2: the target look

def target(fist="right", lean=10, knee=-100, back=110, pump=0):
    """Gear 5 Luffy's skip, the owner's target look. 3.3.1's and 3.3.2's first tries were wrong (the owner: "THE POSE IS STILL WRONG,
    how are you messing this shit up", "Even a fucking stick figure is easy to make dammit", "the right arm is meant to be pointing
    forward NOT THE SIDE"); then the owner described the picture limb by limb (docs/handoff 8zu has it verbatim):
    - the RIGHT arm raised high, bent sharply at the elbow: the upper arm diagonally up and FORWARD from the shoulder, the forearm up,
      the clenched fist up by the top of the head (a celebratory pump); turned 8 degrees out from straight ahead so the fist
      stays beside the head from the front through Dawn Whip's last turn;
    - the LEFT arm out and back from the shoulder, the elbow jutting back, the forearm hanging almost straight down, the fist at the waist;
    - the chest pushed out, the back arched, leaning back a little;
    - the RIGHT leg (the raised fist's side) thrown forward and up, the thigh about level (the knee above the hip), the shin angled down
      and further forward;
    - the LEFT leg trailing under and behind, the thigh down and back, the knee bent deep, the shin curled up behind, the sole near
      vertical.
    PlayerAnimator, as tools/anim draws it: an arm's pitch negative raises it forward (-90 level, -180 straight up), its bend folds the
    forearm on forward (the forearm's angle from straight down is the upper arm's plus the bend); a leg's pitch negative swings it forward,
    its bend the knee (the shin back). The body's pitch positive leans back, its bend negative arches the chest out. {lean}, {knee} (the
    front thigh), {back} (the back knee) and {pump} (the fist's upper arm raised further) bob the pose in the clips. From the front the
    raised fist is beside the head (build.py checks it there); from the side and three-quarters it is up in front of the head by design."""
    up = arm(-135 - pump, 8, 0, -50)
    down = arm(60, 0, 18, -60)
    front_leg = leg(knee, 0, 60)
    back_leg = leg(30, 0, back)
    if fist == "right":
        return p(body={"pitch": lean, "bend": -12, "yaw": 0}, rightArm=up, leftArm=mirror(down), rightLeg=front_leg, leftLeg=mirror(back_leg))
    return p(body={"pitch": lean, "bend": -12, "yaw": 0}, leftArm=mirror(up), rightArm=down, leftLeg=mirror(front_leg), rightLeg=back_leg)


# the right arm coming down out of the skip: still forward, the forearm folding down (never swung across the body)
LOWERED = arm(-40, 0, 0, -30)


TARGET_PARTS = {"body": ("pitch", "yaw", "bend"), "rightArm": ("pitch", "yaw", "roll", "bend"), "leftArm": ("pitch", "yaw", "roll", "bend"),
                "rightLeg": ("pitch", "yaw", "roll", "bend"), "leftLeg": ("pitch", "yaw", "roll", "bend")}
REST = p(body={"pitch": 0, "bend": 0, "yaw": 0}, rightArm=arm(0, 0, 0, -4), leftArm=arm(0, 0, 0, -4), rightLeg=leg(0, 0, 0), leftLeg=leg(0, 0, 0))


def dawn_whip():
    """Dawn Whip, redone again (3.3.2; the owner on 3.2.7's: "not a fan"; the target: Gear 5 Luffy's skip). Cartoon timing, open limbs
    (nothing crossed over the chest): wound up low and turned back, then the body spins round twice like a top on one foot, the arms flung
    straight out like a propeller and the other knee tucked up (fastest as the stretched leg lands: StretchStyle WHIP hits on tick 10),
    it overshoots, wobbles back and pops into Luffy's skip (fist up, laughing), then lets go. server/NikaRules.whipClip plays it with
    Haki on in Gear 5. No head keyed."""
    wind = p(body={"yaw": -55, "pitch": -8, "bend": 16}, rightArm=arm(38, 0, 26, -48), leftArm=mirror(arm(38, 0, 26, -48)),
             rightLeg=leg(-18, 0, 34), leftLeg=leg(-46, 0, 74))
    # the arms flung out and a little down, like a top's: as the body spins they sweep under the head, never across the face
    spin = {"rightArm": arm(-6, 0, 62, -6), "leftArm": mirror(arm(-6, 0, 62, -6)), "rightLeg": leg(0, 0, 4), "leftLeg": leg(-60, 0, 96)}
    beats = [(0, "linear", REST), (4, "outsine", wind)]
    for t, yaw, pitch in ((6, 70, 6), (8, 290, 8), (10, 520, 8), (12, 700, 6)):
        beats.append((t, "linear", p(spin, body={"yaw": yaw, "pitch": pitch, "bend": -4})))
    # past the second turn, wobbling back, then the skip pose: a pop (outback), held a beat, released
    beats.append((14, "outsine", p(spin, body={"yaw": 742, "pitch": 4, "bend": -4})))
    # the right arm swings round to the front, level and a little out, then comes up into the skip
    beats.append((15, "inoutsine", p(target("right"), rightArm=arm(-90, 20, 0, -20), body={"yaw": 722})))
    beats.append((16, "outback", p(target("right"), body={"yaw": 716})))
    beats.append((18, "inoutsine", p(target("right", lean=8, knee=-94), body={"yaw": 720})))
    # and comes down in front before the clip lets go (its fade then only lowers it)
    beats.append((19, "inoutsine", p(target("right", lean=4, knee=-60, back=80), rightArm=LOWERED, body={"yaw": 720})))
    # 720 is 0 seen: a step there at once (the clip's fade-out never unwinds the two turns)
    beats.append((20, "constant", p(target("right", lean=4, knee=-60, back=80), rightArm=LOWERED, body={"yaw": 0})))
    write_clip("gear5_dawn_whip", 20, beats, "Nika: Dawn Whip: wound up, spun round twice like a top with the arms flung out, popping "
               "into Gear 5's laughing skip.", TARGET_PARTS)


# 3.3.9: Dawn Pistol's wind-up: (tick, easing, body yaw for the right arm thrown, body bend). The whole body (PlayerAnimator turns
# the torso mesh alone on a torso yaw: the arms would come off it) twists the throwing shoulder far back as it leans back (negative yaw:
# tools/anim's rig puts the right shoulder 3.2 px behind at -40), then snaps through and is thrown forward into the punch. The original
# draws the stretched arm in the turned body's frame, so the body is square again (within 6 degrees) while the arm is out: StretchStyle
# PISTOL's wind-up 6, reach 4 (it lands on tick 10), hold 3, retract 7.
DAWN_PISTOL = ((0, "linear", 0, 0), (3, "outsine", -36, -10), (5, "insine", -42, -12), (7, "outexpo", 6, 18), (9, "outsine", 0, 16),
               (13, "linear", 0, 12), (17, "inoutsine", 0, 4), (22, "inoutsine", 0, 0))


def dawn_pistol():
    """3.3.9 (the owner: "We need to better the hotkey [Haki] versions"; asked, "Build as proposed": "a quick cartoon wind-up in third
    person (arm yanked far back, body twisting, then the whole body thrown into the punch; first person left alone)"): Dawn Pistol,
    gear5_dawn_pistol (the right arm thrown) and gear5_dawn_pistol_left (the left: server/NikaPistol picks the arm the original chose).
    Every arm track is gear5_pistol's exactly (its first person is the original's own: client/PieceHooks.ownFirstPerson); the torso and
    the body carry the wind-up (DAWN_PISTOL), the left clip's twist mirrored. No head, no leg (the punch is thrown on the run)."""
    pistol = load("gear5_pistol")
    arms = [m for m in pistol["emote"]["moves"] if any(part in m for part in ("rightArm", "leftArm"))]
    for name, sign in (("gear5_dawn_pistol", 1), ("gear5_dawn_pistol_left", -1)):
        moves = [json.loads(json.dumps(m)) for m in arms]
        for tick, easing, yaw, bend in DAWN_PISTOL:
            moves.append({"tick": tick, "easing": easing, "body": {"yaw": round(float(sign * yaw), 3)}})
            moves.append({"tick": tick, "easing": easing, "body": {"bend": round(float(bend), 3)}})
        moves.sort(key=lambda m: m["tick"])
        data = {"version": 3, "name": name, "author": "HexPiece 3.3.9",
                "description": "Nika: Dawn Pistol: the shoulder wound far back, then the whole body thrown into the stretching punch "
                               "(the Pistol's own arms).",
                "emote": dict(pistol["emote"], moves=moves)}
        WRITTEN[name] = data
        save(name, data)


def sky_bounce():
    """Sky Bounce (Shift+Space in Gear 5; the owner: "not a fan" of the old one, which hunched over with the arms folded across the chest;
    the target: Gear 5 Luffy's skip): a squash crouch with the arms swung back, the launch stretched straight with both arms flung up,
    then the skip in the air (fist pumped, knee up, the other foot kicked back), bobbing as it floats, easing out for the landing.
    14 ticks, as the original's. No arm crosses the face (tools/anim.face_covered, checked by build.py)."""
    crouch = p(body={"pitch": -6, "bend": 22}, rightArm=arm(44, 0, 18, -26), leftArm=mirror(arm(44, 0, 18, -26)),
               rightLeg=leg(-34, 0, 62), leftLeg=leg(-34, 0, 62))
    # flung up by the sides like a star jump (up the front they would sweep across the face)
    # the right arm shot up, a little out (it comes down in front into the skip: up by the side it swung across the face),
    # the left flung up by its side
    launch = p(body={"pitch": 4, "bend": -8}, rightArm=arm(-160, 0, -20, -8), leftArm=mirror(arm(24, 0, 150, -8)),
               rightLeg=leg(10, 0, 6), leftLeg=leg(6, 0, 10))
    beats = [(0, "linear", REST), (2, "outsine", crouch), (4, "outexpo", launch), (7, "outback", target("right")),
             (10, "inoutsine", target("right", lean=12, knee=-106, pump=6)),
             (12, "inoutsine", p(target("right", lean=4, knee=-60, back=80), rightArm=LOWERED)),
             (14, "inoutsine", p(REST, body={"pitch": 2, "bend": 0}))]
    write_clip("gear5_bounce", 14, beats, "Nika: Sky Bounce: a squash crouch, launched straight up with the arms flung high, then "
               "Gear 5's laughing skip in the air.", TARGET_PARTS)


def jump():
    """3.3.2's jump (the owner: "shift + space ability is removed, default jumping is much higher and always does the jump animation
    we're doing"; asked: Gear 5 only, about 5 blocks): every jump in Gear 5 (server/NikaJump). The body is already leaving the ground on
    tick 0 (the client launched it), so no crouch before it: pushed off squashed, stretched straight up with both arms flung high by the
    sides (gear5_bounce's launch), then Gear 5's laughing skip (the owner's target look) popping open, held through the air with a
    rubbery bob (a loop from tick 5) until the landing replaces it. No arm crosses the face (tools/anim.face_covered, build.py)."""
    push = p(body={"pitch": -4, "bend": 14}, rightArm=arm(40, 0, 22, -24), leftArm=mirror(arm(40, 0, 22, -24)),
             rightLeg=leg(-20, 0, 34), leftLeg=leg(-20, 0, 34))
    # the right arm shot up, a little out (it comes down in front into the skip: up by the side it swung across the face),
    # the left flung up by its side
    launch = p(body={"pitch": 4, "bend": -8}, rightArm=arm(-160, 0, -20, -8), leftArm=mirror(arm(24, 0, 150, -8)),
               rightLeg=leg(10, 0, 6), leftLeg=leg(6, 0, 10))
    skip = target("right")
    # the bob: leaning back a little more, the front knee higher, the back shin curled further up, the fist pumped higher, then back
    bob = target("right", lean=14, knee=-108, back=120, pump=8)
    beats = [(0, "outsine", push), (2, "outexpo", launch), (5, "outback", skip), (9, "inoutsine", bob), (13, "inoutsine", skip)]
    write_clip("gear5_jump", 13, beats, "Nika: Gear 5's jump: pushed off the rubber ground, stretched up with the arms flung high, then "
               "the laughing skip held through the air.", TARGET_PARTS, loop=True, return_tick=5)


def land():
    """3.3.2: the landing after a Gear 5 jump (server/NikaJump plays it as the feet touch down, unless the body is bounding on: then
    its run): squashed flat like a cartoon, the arms flung out low to the sides, knees buckled; a rubbery spring back up past standing;
    settled by tick 8 (then Gear 5's stance)."""
    squash = p(body={"pitch": -6, "bend": 26}, rightArm=arm(10, 0, 70, -24), leftArm=mirror(arm(10, 0, 70, -24)),
               rightLeg=leg(-44, 0, 78), leftLeg=leg(-40, 0, 72))
    spring = p(body={"pitch": 3, "bend": -6}, rightArm=arm(6, 0, 44, -10), leftArm=mirror(arm(6, 0, 44, -10)),
               rightLeg=leg(-4, 0, 6), leftLeg=leg(-4, 0, 6))
    settle = p(body={"pitch": 0, "bend": 4}, rightArm=arm(2, 0, 14, -8), leftArm=mirror(arm(2, 0, 14, -8)),
               rightLeg=leg(-8, 0, 14), leftLeg=leg(-8, 0, 14))
    beats = [(0, "outexpo", squash), (2, "outsine", squash), (5, "outback", spring), (8, "inoutsine", settle)]
    write_clip("gear5_land", 8, beats, "Nika: Gear 5's landing: squashed flat with the arms flung out, springing back up.", TARGET_PARTS)


def main():
    awaken()
    calmed()
    bajrang()
    dawn_whip()
    dawn_pistol()
    sky_bounce()
    jump()
    land()
    print("wrote the Nika fruit's animations: " + ", ".join(sorted(OWNED)))


if __name__ == "__main__":
    main()
