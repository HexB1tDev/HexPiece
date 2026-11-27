package com.hexhaki.piece.itsumade;

import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;

/**
 * 3.9.4 (the owner: "New Fruit ... Rarity: Mythical Type: Zoan ... Animations needed: Open/Close Mouth, Ambient Animation, Landing
 * Animation, Walk animation (scary/disturbing), Flying slow, Flying really fast, Glide, Bite ... All fire should feel alive, not
 * particles, the actual model should act like Tori Tori No Mi (only on the firey mane, black ribbon, tail and cheeks"; then "Fruit
 * "????" Renamed to "Itsumade"" and "Size: 10 blocks tall, big wing span"): the owner's Itsumade (tools/itsumade_bake.py: its 36-bone
 * rig) posed, its flames, its black ribbon and its mane alive all the time. Pure (no Minecraft API): the game's renderer
 * (client/itsumade/ItsuFx), the previews (tools/preview/ItsuPreview) and the tests share it.
 *
 * <p>The model's space: +Y up, the face toward -Z, its left wing toward +X; units the file's ({@link #SCALE} blocks a unit). Every bone's
 * rest frame is the model's own axes (the bake builds the rig so), so a bone turned about an axis of its parent's frame is turned about
 * the model's axes before its parents turn it.
 */
public final class ItsuForm {
    private ItsuForm() {
    }

    /** The form's phases (server/ItsuServer), how long it takes to form and to go back (ticks). */
    public static final int HUMAN = 0;
    public static final int FORMING = 1;
    public static final int ITSU = 2;
    public static final int UNFORMING = 3;
    public static final int FORM_TICKS = 64;
    public static final int UNFORM_TICKS = 34;

    /**
     * Blocks a model unit: the owner's "10 blocks tall", then "the monster is way too small": standing on its talons, mantling, its
     * crown about 10 blocks up; the file's whole height (its tail's lowest curl to the ring's top) 15.5 blocks.
     */
    public static final float SCALE = 1.2F;

    /** The parts' kinds (the bake's): the body, fire (alive), the black ribbon (alive), the mane's hair (swaying). */
    public static final int BODY = 0;
    public static final int FIRE = 2;
    public static final int RIBBON = 3;
    public static final int HAIR = 4;

    /**
     * The neck's and the head's turn (degrees about the up axis) that bring the face from where the owner's file turns it (toward its
     * own left, 57.7 degrees off ahead) to straight ahead: the neck swings the head before the chest, the head turns the rest.
     */
    static final float NECK_FRONT = 44.4F;
    static final float HEAD_FRONT = 13.3F;
    /** The jaw's hinge (the head's frame: the file's head empty's own across axis) and how far from the file's (open 31 degrees). */
    static final float[] JAW_AXIS = {-0.535F, 0.0F, -0.845F};
    static final float JAW_SHUT = -28.0F;
    static final float JAW_WIDE = 16.0F;
    /** The black ribbon held this far ahead of where the file has it (units): clear in front of the wings' roots (the owner's rule). */
    static final float RIBBON_AHEAD = 0.45F;
    /** The body's pitch on the ground (degrees, forward): leaning forward on its legs as a great bird stands. */
    static float GROUND_PITCH = -52.0F;
    /** The most a tail joint bends from the one before it (degrees). */
    static final float TAIL_BEND = 24.0F;
    /** The most the neck and head turn back against the body's forward pitch to hold the head level (degrees). */
    static final float LEVEL_MOST = 58.0F;

    // ------------------------------------------------------------------------------------------------------------------ the change

    /**
     * The change (the owner: "Z - Transforms (DIsturbing progressive transformation)"; then, of the creature growing out of the human:
     * "it's just growing out of me. Instead of like the player progressively transforming"): the human's own body transforms. First the
     * human convulses (the clip itsu_form: jerks, the back arching, the arms wrenched round, thrown wide, shuddering) and swells in jolts
     * ({@link #humanScale}: to {@link #HUMAN_BIG} its height); at {@link #SWAP} that swollen body IS the Itsumade, the same height
     * ({@link #SWAP_SIZE}), standing upright as the human stood, its arms thrown wide now its wings spread wide ({@link Motion#spread});
     * from there it keeps swelling in jolts to its own size while its body bends over into the bird's, the neck stretching the head up, the
     * tail unrolling joint by joint, the talons coming, the black ribbon snapping alight, the fire catching last; the jaw thrown open in a
     * scream, the wings folded at the end. Going back: the fire out, the ribbon gone, the whole shrinking in lurches, its body straightening
     * upright, until at {@link #UNSWAP} it is the swollen human again, doubled over, shrinking back to itself.
     */
    public static final float SWAP = 24.0F;
    public static final float UNSWAP = 20.0F;
    /** How far the human swells (its height) before it is the creature; the creature's size then (the same height, upright). */
    public static final float HUMAN_BIG = 1.7F;
    public static final float SWAP_SIZE = 0.6F;
    /** When it is full grown (FORMING ticks). */
    static final float FULL = 54.0F;
    /** Kept for the server's timings: the human hidden from the swap (FORMING), back at the unswap (UNFORMING). */
    public static final float HUMAN_GONE = SWAP;
    public static final float HUMAN_BACK = UNSWAP;

    /** Its size (of its own: 0 not drawn, SWAP_SIZE to 1), {@code ticks} into {@code phase}: swelling in jolts. */
    public static float size(int phase, float ticks) {
        return switch (phase) {
            case FORMING -> ticks < SWAP ? 0.0F : SWAP_SIZE + (1.0F - SWAP_SIZE) * lurch((ticks - SWAP) / (FULL - SWAP), 4);
            case UNFORMING -> ticks >= UNSWAP ? 0.0F : 1.0F - (1.0F - SWAP_SIZE) * lurch(ticks / UNSWAP, 4);
            case ITSU -> 1.0F;
            default -> 0.0F;
        };
    }

    /** The human's own size while it is still the human (1 itself): swelling in jolts toward the swap, shrinking back after the unswap. */
    public static float humanScale(int phase, float ticks) {
        return switch (phase) {
            case FORMING -> ticks >= SWAP ? 1.0F : 1.0F + (HUMAN_BIG - 1.0F) * lurch((ticks - 2.0F) / (SWAP - 3.0F), 4);
            case UNFORMING -> ticks < UNSWAP ? 1.0F : HUMAN_BIG - (HUMAN_BIG - 1.0F) * lurch((ticks - UNSWAP) / 11.0F, 3);
            default -> 1.0F;
        };
    }

    /** Whether the human is hidden (it is the creature now). */
    public static boolean humanHidden(int phase, float ticks) {
        return phase == ITSU || phase == FORMING && ticks >= SWAP || phase == UNFORMING && ticks < UNSWAP;
    }

    /** The change's motion {@code ticks} into {@code phase}: each group's growth, its own shape, its wings, the scream. */
    public static void change(int phase, float ticks, Motion mo) {
        if (phase == FORMING) {
            mo.grown[G_RIBBON] = lurch((ticks - 34.0F) / 10.0F, 2);
            mo.grown[G_WINGS] = lurch((ticks - SWAP) / 24.0F, 3);
            mo.grown[G_TAIL] = lurch((ticks - 26.0F) / 24.0F, 3);
            mo.grown[G_NECK] = lurch((ticks - 28.0F) / 16.0F, 3);
            mo.grown[G_LEGS] = lurch((ticks - SWAP) / 16.0F, 2);
            mo.grown[G_FIRE] = smooth((ticks - 42.0F) / 12.0F);
            mo.form = smooth((ticks - 28.0F) / 24.0F);
            // the arms thrown wide are its wings spread wide, held through the growth and the scream, folded at the end
            mo.spread = smooth((ticks - SWAP + 4.0F) / 4.0F) * (1.0F - smooth((ticks - 57.0F) / 7.0F));
            float scream = smooth((ticks - 48.0F) / 4.0F) * (1.0F - smooth((ticks - 58.0F) / 6.0F));
            mo.mouth = Math.max(0.12F, scream);
        } else if (phase == UNFORMING) {
            mo.grown[G_FIRE] = 1.0F - smooth(ticks / 6.0F);
            mo.grown[G_RIBBON] = 1.0F - lurch(ticks / 10.0F, 2);
            mo.grown[G_TAIL] = 1.0F - lurch((ticks - 2.0F) / 15.0F, 3);
            mo.grown[G_NECK] = 1.0F - lurch((ticks - 4.0F) / 13.0F, 3);
            mo.grown[G_WINGS] = 1.0F - lurch(ticks / UNSWAP, 3);
            mo.grown[G_LEGS] = 1.0F - lurch((ticks - 4.0F) / 14.0F, 2);
            mo.form = 1.0F - smooth((ticks - 2.0F) / 15.0F);
            mo.spread = 0.0F;
            mo.mouth = 0.5F * (1.0F - smooth(ticks / 8.0F));
        } else {
            for (int g = 0; g < 6; g++) {
                mo.grown[g] = 1.0F;
            }
            mo.form = 1.0F;
            mo.spread = 0.0F;
        }
    }

    /**
     * 0 to 1 over t in jolts: {@code steps} quick surges (each a hard ease out, stopping dead) with a stall between: the disturbing
     * growth that never runs smooth (and never shrinks back).
     */
    public static float lurch(float t, int steps) {
        t = clamp(t);
        if (t >= 1.0F) {
            return 1.0F;
        }
        float x = t * steps;
        int k = (int) Math.floor(x);
        float f = x - k;
        // each surge in the first 45% of its step, stopping dead (never settling back: it never shrinks), held the rest
        float u = clamp(f / 0.45F);
        float surge = 1.0F - (float) Math.pow(1.0F - u, 4);
        return Math.min(1.0F, (k + surge) / steps);
    }

    // ------------------------------------------------------------------------------------------------------------------ the shape

    /** The rig's bones found once, their rest places, each part's kind and each flowing vertex's reach and phase. */
    public static final class Shape {
        public final int root;
        public final int chest;
        public final int neck1;
        public final int neck2;
        public final int head;
        public final int jaw;
        public final int halo;
        public final int pelvis;
        public final int[] tail;
        /** [side][shoulder, elbow, wrist, finger]: side 0 its left (+X), 1 its right. */
        public final int[][] wing;
        /** [side][hip, ankle, toes]. */
        public final int[][] leg;
        /** Each bone's rest place (model space), 3 a bone. */
        public final float[] rest;
        public final int[] parent;
        /** The tail's end (past its last bone, along the last segment). */
        final float[] tailEnd;
        /** Each part's kind; for a flowing part each vertex's reach (0 at its root, 1 at its tips) and phase (0 to 1). */
        public final int[] kind;
        public final float[][] reach;
        public final float[][] phase;
        /** The lowest point of the feet at rest (model y), and the tail's thickness at its root and its tip (units). */
        public final float feet;
        public Shape(DragonModel m) {
            this.root = m.bone("root");
            this.chest = m.bone("chest");
            this.neck1 = m.bone("neck1");
            this.neck2 = m.bone("neck2");
            this.head = m.bone("head");
            this.jaw = m.bone("jaw");
            this.halo = m.bone("halo");
            this.pelvis = m.bone("pelvis");
            int n = 0;
            while (m.bone("tail" + (n + 1)) >= 0) {
                n++;
            }
            this.tail = new int[n];
            for (int i = 0; i < n; i++) {
                this.tail[i] = m.bone("tail" + (i + 1));
            }
            String[] sides = {"L", "R"};
            this.wing = new int[2][4];
            this.leg = new int[2][3];
            for (int s = 0; s < 2; s++) {
                this.wing[s] = new int[] {m.bone("shoulder" + sides[s]), m.bone("elbow" + sides[s]), m.bone("wrist" + sides[s]),
                    m.bone("finger" + sides[s])};
                this.leg[s] = new int[] {m.bone("hip" + sides[s]), m.bone("ankle" + sides[s]), m.bone("toes" + sides[s])};
            }
            this.parent = m.parent;
            this.rest = new float[m.bones() * 3];
            for (int b = 0; b < m.bones(); b++) {
                // the inverse bind of a bone whose rest frame is the model's axes: a translation by minus its rest place
                this.rest[b * 3] = -m.inverseBind[b * 12 + 3];
                this.rest[b * 3 + 1] = -m.inverseBind[b * 12 + 7];
                this.rest[b * 3 + 2] = -m.inverseBind[b * 12 + 11];
            }
            int last = this.tail[n - 1];
            int prev = this.tail[n - 2];
            this.tailEnd = new float[3];
            for (int k = 0; k < 3; k++) {
                this.tailEnd[k] = this.rest[last * 3 + k] + (this.rest[last * 3 + k] - this.rest[prev * 3 + k]) * 0.9F;
            }
            this.kind = new int[m.parts.length];
            this.reach = new float[m.parts.length][];
            this.phase = new float[m.parts.length][];
            float low = Float.MAX_VALUE;
            for (int i = 0; i < m.parts.length; i++) {
                DragonModel.Part part = m.parts[i];
                this.kind[i] = part.kind;
                if (part.kind == FIRE || part.kind == RIBBON || part.kind == HAIR) {
                    int nv = part.vertices();
                    this.reach[i] = new float[nv];
                    this.phase[i] = new float[nv];
                    for (int v = 0; v < nv; v++) {
                        // the bake's code: inside the atlas's white square (8 texels), u = (0.5 + 7 reach) / 1024, v the same for the phase
                        this.reach[i][v] = clamp((part.uv[v * 2] * 1024.0F - 0.5F) / 7.0F);
                        this.phase[i][v] = clamp((part.uv[v * 2 + 1] * 1024.0F - 0.5F) / 7.0F);
                    }
                }
                if (part.name.contains("digit") || part.name.contains("talon") || part.name.contains("hallux") || part.name.contains("toe pad")) {
                    for (int v = 0; v < part.vertices(); v++) {
                        low = Math.min(low, part.pos[v * 3 + 1]);
                    }
                }
            }
            this.feet = low == Float.MAX_VALUE ? 2.24F : low;
        }

        public float rx(int bone) {
            return this.rest[bone * 3];
        }

        public float ry(int bone) {
            return this.rest[bone * 3 + 1];
        }

        public float rz(int bone) {
            return this.rest[bone * 3 + 2];
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the motion

    /** How it moves now (each 0 to 1 unless said, eased by the caller). */
    public static final class Motion {
        /** Flying speed (0 hovering, 1 its fastest), gliding, diving. */
        public float speed;
        public float glide;
        public float dive;
        /** On the ground (0 in the air), walking (0 standing, 1 its pace), its stride's phase (radians, run on by the distance walked). */
        public float perch;
        public float walk;
        public float stride;
        /** Braking to land (wings flared, legs reaching), the touchdown's dip (0 to 1 and back). */
        public float flare;
        public float land;
        /** The mouth: 0 shut, 1 wide (the ambient's breath opens it now and then); the bite: 0 none, its run from 0 to 1. */
        public float mouth = -1.0F;
        public float bite;
        /** The look's pitch (degrees, negative up) the head follows; how far the look is turned from where the body faces (degrees). */
        public float look;
        public float turn;
        /** 3.9.4 the change: how far it has its own shape (0: the human's, 1: its own); each group's growth (FORMING / UNFORMING). */
        public float form = 1.0F;
        public final float[] grown = {1, 1, 1, 1, 1, 1};
        /** 3.9.5: its wings spread wide (the change's arms thrown wide; 0 folded as a bird's on the ground). */
        public float spread;
    }

    // ------------------------------------------------------------------------------------------------------------------ the pose

    /** Groups that grow out in the change (Motion.grown). */
    public static final int G_RIBBON = 0;
    public static final int G_WINGS = 1;
    public static final int G_TAIL = 2;
    public static final int G_NECK = 3;
    public static final int G_LEGS = 4;
    public static final int G_FIRE = 5;

    /**
     * 3.9.5 (the owner: "the wings are way too fast. He looks like a fucking mosquito"): the beat's rate (beats a second), hovering and
     * at its fastest: a bird this big beats slowly and heavily (a condor's or a great eagle's pace, slower still); the part of a beat the
     * downstroke takes (the power stroke, slower than the recovery).
     */
    static final float BEAT_SLOW = 0.42F;
    static final float BEAT_FAST = 0.6F;
    static final float DOWN_PART = 0.56F;
    /** How high the body heaves on each downstroke (units). */
    static final float HEAVE = 0.22F;
    /** Its body upright as the human's (the change, before it takes its own shape): the chest's pitch (degrees). */
    static final float UPRIGHT = 2.0F;

    /** The body's rise (units) on the beat now: up through the downstroke, settling through the recovery. */
    public static float heave(Motion mo, float seconds) {
        float air = 1.0F - mo.perch;
        float rate = BEAT_SLOW + (BEAT_FAST - BEAT_SLOW) * mo.speed;
        float beat = seconds * rate;
        float flap = air * (1.0F - 0.9F * mo.glide) * (1.0F - 0.5F * mo.flare);
        return flap * HEAVE * (1.0F - 0.5F * mo.speed) * (float) Math.sin((beat - 0.12F) * Math.PI * 2.0);
    }

    /**
     * Poses the rig for {@code mo} at {@code seconds} (the creature's own time: its breath, its beats, its twitches): the chest pitched
     * into the flight (upright hovering, levelled flying, flat out gliding and fast) or leaning forward on its legs on the ground; the
     * neck alive (an S that sways and breathes, turning after the look, the head snapping between looks as a bird's, thrust forward
     * with each step) and the head held level; the wings beating as a bird's (the downstroke with the wing spread, the recovery with it
     * folded at the elbow and the wrist), gliding spread, flared to land, folded against the body on the ground; the legs tucked in
     * flight, reaching to land, stalking on the ground; the tail laid along a line by {@link #tail}; the jaw; the black ribbon swaying.
     */
    public static void pose(DragonModel m, Shape sh, DragonPose pose, Motion mo, float seconds) {
        pose.rest();
        float air = 1.0F - mo.perch;
        float t = seconds;
        float rate = BEAT_SLOW + (BEAT_FAST - BEAT_SLOW) * mo.speed;
        float beat = t * rate;
        float cyc = beat - (float) Math.floor(beat);
        float flap = air * (1.0F - 0.9F * mo.glide) * (1.0F - 0.5F * mo.flare);
        // ---- the chest: its pitch into the flight
        float pitchAir = lerp(-30.0F, -70.0F, smooth(mo.speed * 1.6F));
        pitchAir = lerp(pitchAir, -84.0F, smooth((mo.speed - 0.6F) / 0.4F));
        pitchAir = lerp(pitchAir, -80.0F, mo.glide);
        pitchAir = lerp(pitchAir, -105.0F, mo.dive);
        pitchAir = lerp(pitchAir, -14.0F, mo.flare);
        // on the ground (the owner: "ITS A BIRD LIKE MONSTER"): leaning forward on its legs as a great bird stands, more as it walks;
        // the change: upright as the human was until it takes its own shape
        float groundPitch = lerp(UPRIGHT, GROUND_PITCH - 6.0F * mo.walk, mo.form);
        float chestPitch = lerp(groundPitch, pitchAir, air);
        // the downstroke rocks the body a little nose up, the recovery nose down; the body rises with it (heave)
        chestPitch += flap * 3.0F * (float) Math.sin((beat - 0.05F) * Math.PI * 2.0);
        pose.t[sh.root * 3 + 1] += heave(mo, t);
        // on the ground: the breath, each step's lurch and roll, the body dipping as each foot takes the weight
        float breath = (float) Math.sin(t * 1.3);
        chestPitch += mo.perch * (1.2F * breath + mo.walk * 3.0F * (float) Math.sin(mo.stride * 2.0F));
        float roll = mo.perch * mo.walk * 4.0F * (float) Math.sin(mo.stride);
        pose.t[sh.root * 3 + 1] -= mo.perch * mo.walk * 0.12F * (0.5F + 0.5F * (float) Math.cos(mo.stride * 2.0F));
        turn(pose, sh.chest, chestPitch, 1, 0, 0);
        turn(pose, sh.chest, roll, 0, 0, 1);
        float dip = mo.land;
        // ---- the neck and the head (the owner: "His neck is too stiff"): held level whatever the chest's pitch, turned after the look,
        // an S that sways slowly and breathes, the head snapping between still looks as a bird's (on the ground), thrust forward with
        // each step (a pigeon's bob, on a monster), undulating with the beats in the air
        float level = Math.min(LEVEL_MOST, -chestPitch) * mo.form;
        float look = clampf(mo.look, -50.0F, 50.0F);
        float follow = clampf(mo.turn, -70.0F, 70.0F);
        float twitch = mo.perch * twitch(t);
        float jerkYaw = mo.perch * (jerk(t, 0.9F) * (22.0F - 10.0F * mo.walk));
        float jerkPitch = mo.perch * jerk(t + 37.0F, 1.3F) * 8.0F;
        float swayYaw = 10.0F * (float) Math.sin(t * 0.37) + 4.0F * (float) Math.sin(t * 0.91 + 1.3);
        // (diving, the neck held straight down the dive: bent more there, its ruff tore)
        float swayPitch = 7.0F * (float) Math.sin(t * 0.6) * (0.6F + 0.4F * mo.perch) * (1.0F - 0.85F * mo.dive);
        float wave = flap * 6.0F * (float) Math.sin((beat - 0.25F) * Math.PI * 2.0) * (1.0F - mo.dive);
        float thrust = mo.perch * mo.walk * thrust(mo.stride * 2.0F);
        float bob = thrust * 14.0F + jerkPitch;
        float lunge = biteLunge(mo.bite);
        quat(pose, sh.neck1, NECK_FRONT + follow * 0.35F + swayYaw * 0.6F + jerkYaw * 0.3F,
            level * 0.5F - look * 0.25F + bob * 0.5F + swayPitch + wave - lunge * 22.0F - 6.0F * dip);
        quat(pose, sh.neck2, follow * 0.3F - swayYaw * 0.25F,
            level * 0.3F - look * 0.25F + bob * 0.3F - swayPitch * 0.8F - wave * 0.7F - lunge * 18.0F);
        quat(pose, sh.head, HEAD_FRONT + follow * 0.3F + jerkYaw * 0.7F + swayYaw * 0.15F,
            level * 0.2F - look * 0.5F + lunge * 14.0F - swayPitch * 0.3F);
        turn(pose, sh.head, twitch, 0, 0, 1);
        // ---- the jaw: the mouth as asked, else the breath opening it now and then (on the ground a slow, hungry gape)
        float mouth = mo.mouth >= 0.0F ? mo.mouth : ambientMouth(t, mo.perch);
        mouth = Math.max(mouth, biteMouth(mo.bite));
        turn(pose, sh.jaw, lerp(JAW_SHUT, JAW_WIDE, mouth), JAW_AXIS[0], JAW_AXIS[1], JAW_AXIS[2]);
        // ---- the black ribbon: swaying with the breath and the wind
        turn(pose, sh.halo, 3.0F * (float) Math.sin(t * 0.9) + 6.0F * mo.speed * air, 1, 0, 0);
        turn(pose, sh.halo, 2.5F * (float) Math.sin(t * 0.7 + 1.0), 0, 0, 1);
        pose.t[sh.halo * 3 + 2] -= RIBBON_AHEAD;
        // ---- the wings
        for (int s = 0; s < 2; s++) {
            wing(pose, sh, s, mo, cyc, flap, air, t);
        }
        // ---- the legs (the body's lean on the ground undone at the hips: they stand upright under it)
        for (int s = 0; s < 2; s++) {
            leg(pose, sh, s, mo, air, t, -groundPitch);
        }
        // ---- the change: each group grown (Motion.grown: 0 none, 1 full)
        if (mo.form < 1.0F || mo.grown[G_WINGS] < 1.0F || mo.grown[G_TAIL] < 1.0F) {
            grow(pose, sh, mo);
        }
        pose.solve();
        // ---- the tail along its line (the chest and the pelvis placed first)
        tail(sh, pose, mo, air, t);
        pose.solve();
    }

    /**
     * 3.9.5 (the owner: "when you're landing, you go into the ground"): how high its chest stands over its feet on the ground (units). In
     * the air the player's feet are held that far under its chest (client/itsumade/ItsuFx), so it comes down onto its feet, never into
     * the ground, and its feet stay where the player's are when it lands.
     */
    public static float chestHeight(DragonModel m, Shape sh) {
        DragonPose pose = new DragonPose(m);
        Motion mo = new Motion();
        mo.perch = 1.0F;
        pose(m, sh, pose, mo, 0.0F);
        float[] g = pose.global;
        float foot = Float.MAX_VALUE;
        for (int s = 0; s < 2; s++) {
            int toe = sh.leg[s][2];
            foot = Math.min(foot, g[toe * 12 + 7] - (sh.ry(toe) - sh.feet));
        }
        return g[sh.chest * 12 + 7] - foot;
    }

    /**
     * 3.9.5 (the owner: "This is not how wings pose of a bird. Please research how a fucking bird uses his wings"): on the ground a
     * bird's wings are FOLDED against its body: the upper arm laid back and down along the flank ({@link #FOLD_DOWN}, onto the back by
     * {@link #FOLD_ONTO}), the forearm folded forward beside it at the elbow ({@link #FOLD_ELBOW}), the hand folded back at the wrist
     * ({@link #FOLD_WRIST}) so the long flight feathers lie along the back and their tips reach past the hips. Degrees, the left wing's
     * (the right mirrored): about the fore and aft axis (+ up), about the across axis (+ onto the back... - off it), about the up axis
     * within the wing's own plane (+ the tip forward).
     */
    static float FOLD_DOWN = -78.0F;
    static float FOLD_ONTO = -16.0F;
    static float FOLD_ELBOW = 78.0F;
    static float FOLD_WRIST = -118.0F;
    static float FOLD_FINGER = -18.0F;

    /**
     * A wing, as a bird's (the owner, 3.9.5). In the air: the DOWNSTROKE (the first {@link #DOWN_PART} of a beat, the power stroke) with
     * the wing fully spread, swept from high over the back to low under the body, a little forward, its tip bending up under the load; the
     * RECOVERY (the rest) with the wing folded at the elbow and the wrist (the hand swept back, the feathers turned to slip through the
     * air) as it is drawn up close and back, opening again at the top. Fast: shallower, swept back; gliding: spread and still with a
     * dihedral; diving: half folded; flaring to land: thrown up and spread, the beats braking. On the ground folded ({@link #FOLD_DOWN} and
     * on), breathing with the body, shrugged now and then, rousing (a shake) now and then; walking, lifted a little for balance; the change's
     * spread ({@link Motion#spread}): thrown wide and up.
     */
    static void wing(DragonPose pose, Shape sh, int s, Motion mo, float cyc, float flap, float air, float t) {
        float sgn = s == 0 ? 1.0F : -1.0F;
        int[] w = sh.wing[s];
        // ---- the air: the stroke
        float amp = lerp(1.0F, 0.72F, mo.speed);
        float high = 50.0F * amp;
        float low = -36.0F * amp;
        float lift;
        float flex;
        float fwd;
        float tip;
        if (cyc < DOWN_PART) {
            float u = cyc / DOWN_PART;
            lift = high + (low - high) * smooth(u);
            flex = 0.0F;
            fwd = 9.0F * (float) Math.sin(Math.PI * u);
            tip = 10.0F * (float) Math.sin(Math.PI * u);
        } else {
            float u = (cyc - DOWN_PART) / (1.0F - DOWN_PART);
            lift = low + (high - low) * smooth(u);
            flex = (float) Math.sin(Math.PI * Math.min(1.0F, u * 1.15F));
            fwd = -14.0F * flex;
            tip = -12.0F * flex;
        }
        lift *= flap;
        flex *= flap;
        fwd *= flap;
        tip *= flap;
        float sweep = 24.0F * smooth((mo.speed - 0.4F) / 0.6F) * air * (1.0F - mo.flare) + 16.0F * mo.dive;
        float glide = mo.glide * air * (1.0F - mo.flare);
        float aLift = lift + air * (4.0F + glide * (3.0F + 2.0F * (float) Math.sin(t * 0.8 + s))) + mo.flare * 28.0F;
        float aSweep = fwd - sweep + mo.flare * 4.0F;
        float diveFold = 0.45F * mo.dive;
        float aElbow = 40.0F * flex + 60.0F * diveFold;
        float aWrist = -72.0F * flex - 80.0F * diveFold;
        float aFinger = -24.0F * flex;
        float aTip = tip;
        // ---- the ground: folded, breathing, shrugging and rousing now and then, lifted a little with the steps for balance
        float breath = 2.0F * (float) Math.sin(t * 1.3);
        float shrug = 8.0F * smooth(((float) Math.sin(t * 0.43 + s * 0.6) - 0.78F) / 0.22F);
        float rouse = smooth(((float) Math.sin(t * 0.19 + 2.0) - 0.9F) / 0.1F);
        float shake = rouse * 5.0F * (float) Math.sin(t * 34.0 + s);
        float balance = mo.walk * 6.0F * Math.max(0.0F, (float) Math.sin(mo.stride + (s == 0 ? Math.PI : 0.0)));
        float gLift = FOLD_DOWN + breath + shrug + shake + balance + rouse * 10.0F;
        float gOnto = FOLD_ONTO;
        float gElbow = FOLD_ELBOW - rouse * 12.0F;
        float gWrist = FOLD_WRIST + rouse * 18.0F + balance * 1.5F;
        float gFinger = FOLD_FINGER;
        // the change's spread: thrown out wide to the sides (as the human's arms were), a great bird's spread, shaking
        float sp = mo.spread;
        float spShake = sp * 3.0F * (float) Math.sin(t * 29.0 + s * 1.7);
        gLift = lerp(gLift, 8.0F + spShake, sp);
        gOnto = lerp(gOnto, 0.0F, sp);
        gElbow = lerp(gElbow, 4.0F, sp);
        gWrist = lerp(gWrist, -4.0F + spShake, sp);
        gFinger = lerp(gFinger, 0.0F, sp);
        float p = mo.perch;
        float shoulderLift = lerp(aLift, gLift, p);
        float shoulderOnto = lerp(0.0F, gOnto, p);
        float shoulderSweep = lerp(aSweep, 0.0F, p);
        float elbow = lerp(aElbow, gElbow, p);
        float wrist = lerp(aWrist, gWrist, p);
        float finger = lerp(aFinger, gFinger, p);
        float tipBend = lerp(aTip, 0.0F, p);
        turn(pose, w[0], sgn * shoulderLift, 0, 0, 1);
        turn(pose, w[0], shoulderOnto, 1, 0, 0);
        turn(pose, w[0], sgn * shoulderSweep, 0, 1, 0);
        turn(pose, w[1], sgn * elbow, 0, 1, 0);
        turn(pose, w[2], sgn * wrist, 0, 1, 0);
        turn(pose, w[2], sgn * tipBend, 0, 0, 1);
        turn(pose, w[3], sgn * finger, 0, 1, 0);
        turn(pose, w[3], sgn * tipBend * 0.6F, 0, 0, 1);
    }

    /** The part of a stride a foot is on the ground (a walk: each foot down more than half the time). */
    static final float STANCE = 0.6F;
    /** The hip's swing either way walking (degrees). */
    static final float HIP_SWING = 30.0F;

    /**
     * A leg (the owner: "his feet don't move when he walks"). Walking, a bird's stride: the foot planted flat and drawn back under the
     * body from ahead to behind (the STANCE part of the stride), then lifted: the tarsus folding up behind, the toes clenching shut,
     * swung forward high, thrown out and spread just before it plants again; the body dipping as each foot takes the weight (pose()).
     * Standing: the toes flat on the ground, the weight shifting from foot to foot, now and then one foot lifted, its talons clenching
     * and opening (scratching at the ground). In the air: tucked under the body, the toes curled; reaching down and ahead, the talons
     * spread, to land; the touchdown's give. {@code stand}: the body's lean on the ground (degrees, undone at the hips).
     */
    static void leg(DragonPose pose, Shape sh, int s, Motion mo, float air, float t, float stand) {
        int[] l = sh.leg[s];
        float sideSign = s == 0 ? 1.0F : -1.0F;
        // ---- the air
        float tuck = air * (1.0F - mo.flare) * (38.0F + 20.0F * mo.speed);
        float reach = mo.flare * -30.0F;
        float airAnkle = air * (1.0F - mo.flare) * 30.0F + mo.flare * -10.0F;
        float airToes = air * (1.0F - mo.flare) * -45.0F + mo.flare * 20.0F;
        // ---- the ground: the stride (+ hip: the foot ahead)
        float step = mo.perch * mo.walk;
        double ph = mo.stride + (s == 0 ? 0.0 : Math.PI);
        float c = (float) (ph / (Math.PI * 2.0));
        c -= (float) Math.floor(c);
        float hip;
        float ankle;
        float toes;
        if (c < STANCE) {
            float u = c / STANCE;
            hip = HIP_SWING - 2.0F * HIP_SWING * u;
            ankle = -12.0F * (float) Math.sin(Math.PI * u);
            toes = -(hip + ankle); // planted flat
        } else {
            float u = (c - STANCE) / (1.0F - STANCE);
            float up = (float) Math.sin(Math.PI * u);
            hip = -HIP_SWING + 2.0F * HIP_SWING * smooth(u);
            ankle = -78.0F * up;
            // clenched shut as it lifts, thrown open as it reaches to plant
            float open = smooth((u - 0.65F) / 0.35F);
            toes = 70.0F * up * (1.0F - open) - (hip + ankle) * open;
        }
        hip *= step;
        ankle *= step;
        toes *= step;
        float splay = step * 5.0F;
        // ---- standing: the weight shifting, a foot lifted now and then, the talons flexing
        float still = mo.perch * (1.0F - mo.walk);
        float shift = still * 3.0F * (float) Math.sin(t * 0.9 + s * Math.PI);
        float raise = still * smooth(((float) Math.sin(t * 0.47 + s * 2.4) - 0.8F) / 0.2F);
        float clench = still * (0.5F + 0.5F * (float) Math.sin(t * 5.0 + s)) * raise;
        hip += shift + 22.0F * raise;
        ankle += -36.0F * raise;
        toes += 50.0F * clench + (14.0F * raise - shift) * (1.0F - clench);
        // the touchdown's give: the knees folding under the weight
        float give = mo.land * 22.0F;
        float upright = mo.perch * stand;
        turn(pose, l[0], tuck + reach + upright + hip - give * 0.6F, 1, 0, 0);
        turn(pose, l[0], sideSign * (splay + 3.0F * still), 0, 0, 1);
        turn(pose, l[1], ankle + airAnkle - give, 1, 0, 0);
        turn(pose, l[2], toes + airToes + give * 1.6F, 1, 0, 0);
    }

    /**
     * The tail laid along a line: each segment turned (the least turn) so it points where its line goes. In the air it streams back
     * from the pelvis, a little low, a slow wave running down it (smaller and quicker the faster it flies, all but straight gliding); on
     * the ground it goes down to the ground and drags behind, crawling in an S that runs back along it as the creature walks, its tip
     * curling.
     */
    static void tail(Shape sh, DragonPose pose, Motion mo, float air, float t) {
        int n = sh.tail.length;
        // the pelvis's global frame (its rotation as a 3 x 3 and its place)
        float[] g = pose.global;
        int pb = sh.pelvis;
        float[] rot = {g[pb * 12], g[pb * 12 + 1], g[pb * 12 + 2], g[pb * 12 + 4], g[pb * 12 + 5], g[pb * 12 + 6], g[pb * 12 + 8],
            g[pb * 12 + 9], g[pb * 12 + 10]};
        orthonormal(rot);
        float[] at = {g[sh.tail[0] * 12 + 3], g[sh.tail[0] * 12 + 7], g[sh.tail[0] * 12 + 11]};
        // the ground: under the feet as they stand now (their toes' joints, less the talons' reach below them at rest)
        float floor = Float.MAX_VALUE;
        for (int side = 0; side < 2; side++) {
            int toe = sh.leg[side][2];
            floor = Math.min(floor, g[toe * 12 + 7] - (sh.ry(toe) - sh.feet));
        }
        floor += 0.05F;
        float wave = (float) (t * (1.2 + 1.5 * mo.speed));
        float px = 0.0F;
        float py = -1.0F;
        float pz = 0.0F;
        float crawl = mo.stride * 1.3F + t * 0.6F;
        for (int i = 0; i < n; i++) {
            int b = sh.tail[i];
            float[] next = i + 1 < n ? new float[] {sh.rx(sh.tail[i + 1]), sh.ry(sh.tail[i + 1]), sh.rz(sh.tail[i + 1])} : sh.tailEnd;
            float rdx = next[0] - sh.rx(b);
            float rdy = next[1] - sh.ry(b);
            float rdz = next[2] - sh.rz(b);
            float len = (float) Math.sqrt(rdx * rdx + rdy * rdy + rdz * rdz);
            float u = i / (float) (n - 1);
            // where the line goes: in the air, back and a little down with the wave; on the ground, down to it then back along it
            float ax = 0.22F * u * (float) Math.sin(wave * 0.7 - u * 4.0);
            float ay = -0.22F + (0.38F - 0.22F * mo.speed) * (0.35F + u) * (float) Math.sin(wave - u * 6.5) * (1.0F - 0.7F * mo.glide);
            float az = 1.0F;
            float radius = 0.55F - 0.35F * u;
            float gx;
            float gy;
            float gz;
            // on the ground (the owner's line): back from the hips nearly level, sloping down to the ground about half way along, then
            // along it; standing, swaying slowly from side to side; walking, crawling in an S that runs back along it; the tip flicking
            float side = (0.35F + 0.5F * u) * (mo.walk * (float) Math.sin(crawl - u * 7.5)
                + (1.0F - mo.walk) * 0.55F * (float) Math.sin(t * 0.55 - u * 3.5));
            if (at[1] > floor + radius + 0.1F) {
                gx = side;
                gy = -(0.3F + 0.45F * u);
                gz = 1.0F;
            } else {
                gx = side;
                gy = 0.8F * (floor + radius - at[1]);
                gz = 1.0F;
            }
            if (i >= n - 3) {
                gy += 0.18F * (i - (n - 4)) * Math.max(0.0F, (float) Math.sin(t * 2.3)); // the tip flicking up and curling
                gx += 0.15F * (i - (n - 4)) * (float) Math.sin(t * 3.1);
            }
            float tx = lerp(gx, ax, air);
            float ty = lerp(gy, ay, air);
            float tz = lerp(gz, az, air);
            float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            tx /= tl;
            ty /= tl;
            tz /= tl;
            // never bent more than TAIL_BEND from the segment before (a sharper turn pinches the skin and swings the tail's fire off)
            if (i > 0) {
                float c = tx * px + ty * py + tz * pz;
                float lim = (float) Math.cos(Math.toRadians(TAIL_BEND));
                if (c < lim) {
                    // the target turned back toward the previous direction, onto the cone's edge
                    float ox = tx - px * c;
                    float oy = ty - py * c;
                    float oz = tz - pz * c;
                    float ol = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
                    float sn = (float) Math.sin(Math.toRadians(TAIL_BEND));
                    if (ol > 1.0E-6F) {
                        tx = px * lim + ox / ol * sn;
                        ty = py * lim + oy / ol * sn;
                        tz = pz * lim + oz / ol * sn;
                    }
                }
            }
            px = tx;
            py = ty;
            pz = tz;
            // the bone's local turn: its rest direction (model axes) onto the target seen from its parent's frame
            float lx = rot[0] * tx + rot[3] * ty + rot[6] * tz;
            float ly = rot[1] * tx + rot[4] * ty + rot[7] * tz;
            float lz = rot[2] * tx + rot[5] * ty + rot[8] * tz;
            float[] q = onto(rdx / len, rdy / len, rdz / len, lx, ly, lz);
            int o = b * 4;
            pose.q[o] = q[0];
            pose.q[o + 1] = q[1];
            pose.q[o + 2] = q[2];
            pose.q[o + 3] = q[3];
            // its frame now: the parent's times its own turn; the next joint along the target
            rot = mulRot(rot, toRot(q));
            at[0] += tx * len;
            at[1] += ty * len;
            at[2] += tz * len;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the change

    /** Each group grown in the change (Motion.grown): the ribbon alight from nothing, the wings, the neck and the legs to their size, the
     * tail unrolling joint by joint. */
    static void grow(DragonPose pose, Shape sh, Motion mo) {
        scale(pose, sh.halo, mo.grown[G_RIBBON]);
        float wings = 0.72F + 0.28F * mo.grown[G_WINGS];
        scale(pose, sh.wing[0][0], wings);
        scale(pose, sh.wing[1][0], wings);
        int n = sh.tail.length;
        for (int i = 0; i < n; i++) {
            float k = clamp(mo.grown[G_TAIL] * (n + 3) - i);
            scale(pose, sh.tail[i], 0.2F + 0.8F * k);
        }
        scale(pose, sh.neck1, 0.55F + 0.45F * mo.grown[G_NECK]);
        float legs = 0.75F + 0.25F * mo.grown[G_LEGS];
        scale(pose, sh.leg[0][0], legs);
        scale(pose, sh.leg[1][0], legs);
    }

    /**
     * The black ribbon drawn in toward the ring's centre (the halo bone) by {@code k} (0 gone, 1 itself): it snaps alight in the change
     * (Motion.grown[G_RIBBON]); its wisps are skinned to the chest and the neck too, so the halo's own scale cannot hide them.
     */
    public static void ribbonGrown(Shape sh, DragonPose pose, int part, float[] pos, float k) {
        if (k >= 1.0F || sh.kind[part] != RIBBON) {
            return;
        }
        float[] g = pose.global;
        float cx = g[sh.halo * 12 + 3];
        float cy = g[sh.halo * 12 + 7];
        float cz = g[sh.halo * 12 + 11];
        for (int v = 0; v < pos.length / 3; v++) {
            pos[v * 3] = cx + (pos[v * 3] - cx) * k;
            pos[v * 3 + 1] = cy + (pos[v * 3 + 1] - cy) * k;
            pos[v * 3 + 2] = cz + (pos[v * 3 + 2] - cz) * k;
        }
    }

    static void scale(DragonPose pose, int bone, float s) {
        s = Math.max(0.001F, s);
        pose.s[bone * 3] = s;
        pose.s[bone * 3 + 1] = s;
        pose.s[bone * 3 + 2] = s;
    }

    // ------------------------------------------------------------------------------------------------------------------ alive

    /** The parts' kinds that move on their own: their vertices' reach (from the bake) makes the tips move most. */
    public static boolean flowing(Shape sh, int part) {
        return sh.reach[part] != null;
    }

    /**
     * The flowing parts alive (the owner: "All fire should feel alive, not particles, the actual model should act like Tori Tori No Mi"):
     * after skinning, each vertex of a flowing part moved by its reach (its tips most, its root not at all) in the model's space:
     * <ul>
     *   <li>fire (the fiery mane down the back, the collar and ruff at the neck, the tail's fire, the cheeks' blazes): licking up (the
     *       world's up, {@code upX..upZ} in the model's space), rippling out along its normal in waves that run out to the tips, each
     *       tongue flickering out of step;</li>
     *   <li>the black ribbon: rolling waves running round its ring, its wisps curling out and back like smoke;</li>
     *   <li>the mane's hair: swaying, blown back by the wind of the flight ({@code windX..windZ}: the flight's backward, model space).</li>
     * </ul>
     * {@code alive} scales it (0 still: the change grows the fire in).
     */
    public static void flow(Shape sh, int part, float[] pos, float[] nrm, float seconds, float upX, float upY, float upZ, float windX,
                            float windY, float windZ, float wind, float alive) {
        float[] reach = sh.reach[part];
        if (reach == null || alive <= 0.0F) {
            return;
        }
        float[] ph = sh.phase[part];
        int kind = sh.kind[part];
        int nv = reach.length;
        float t = seconds;
        for (int v = 0; v < nv; v++) {
            float r = reach[v];
            if (r <= 0.0F) {
                continue;
            }
            float p = ph[v];
            float nx = nrm[v * 3];
            float ny = nrm[v * 3 + 1];
            float nz = nrm[v * 3 + 2];
            float dx;
            float dy;
            float dz;
            if (kind == FIRE) {
                float k = r * (float) Math.sqrt(r);
                float lick = 0.16F + 0.12F * (float) Math.sin(t * 7.3 + p * 31.0 - r * 4.0);
                float ripple = 0.09F * (float) Math.sin(t * 11.0 + p * 17.0 - r * 7.0);
                float flick = 0.05F * (float) Math.sin(t * 17.0 + p * 53.0 + r * 3.0);
                dx = (upX * lick + nx * ripple + windX * wind * 0.12F) * k + flick * k * nz;
                dy = (upY * lick + ny * ripple + windY * wind * 0.12F) * k;
                dz = (upZ * lick + nz * ripple + windZ * wind * 0.12F) * k - flick * k * nx;
            } else if (kind == RIBBON) {
                // 3.9.5 (the owner: "The ribbon is completely broken. It doesn't resemble flames anymore"): its tongues keep their shape,
                // only licking up and fluttering out to their tips, quick and small as a flame's
                float k = (float) Math.pow(r, 1.4);
                float flutter = 0.06F * (float) Math.sin(t * 5.3 - r * 6.0 + p * 19.0);
                float lick = 0.05F + 0.04F * (float) Math.sin(t * 3.9 + p * 11.0 - r * 4.0);
                dx = (nx * flutter + upX * lick + windX * wind * 0.1F) * k;
                dy = (ny * flutter + upY * lick + windY * wind * 0.1F) * k;
                dz = (nz * flutter + upZ * lick + windZ * wind * 0.1F) * k;
            } else {
                // 3.9.5 (the owner: "His hair doesn't move"): the mane's hair swaying in waves that run out to its tips, in gusts,
                // blown back by the wind of its going (walking too)
                float k = r * r;
                float gust = 0.65F + 0.35F * (float) Math.sin(t * 0.9 + p * 5.0);
                float sway = 0.26F * gust * (float) Math.sin(t * 2.3 + p * 23.0 - r * 3.5);
                float side = 0.14F * gust * (float) Math.sin(t * 1.7 + p * 13.0 - r * 2.5);
                float blow = 0.5F * wind;
                dx = (nx * sway + side + windX * blow) * k;
                dy = (ny * sway * 0.5F + windY * blow) * k;
                dz = (nz * sway + windZ * blow) * k;
            }
            pos[v * 3] += dx * alive;
            pos[v * 3 + 1] += dy * alive;
            pos[v * 3 + 2] += dz * alive;
        }
    }

    /** The fire's heat at a vertex (0 to 1): bright bands running out along each flame to its tips (its colour toward white-gold). */
    public static float heat(Shape sh, int part, int v, float seconds) {
        float[] reach = sh.reach[part];
        if (reach == null || sh.kind[part] != FIRE) {
            return 0.0F;
        }
        float r = reach[v];
        float band = 0.5F + 0.5F * (float) Math.sin(seconds * 6.0 - r * 9.0 + sh.phase[part][v] * 20.0);
        return clamp((1.0F - r) * 0.35F + band * band * band * 0.55F * (0.3F + 0.7F * r));
    }

    // ------------------------------------------------------------------------------------------------------------------ timings

    /** The ambient mouth on the ground or hovering: shut, opening slowly now and then (a hungry, silent gape), shutting again. */
    static float ambientMouth(float t, float perch) {
        float cycle = (float) ((t * 0.16) % 1.0);
        float gape = smooth((cycle - 0.55F) / 0.15F) * (1.0F - smooth((cycle - 0.82F) / 0.12F));
        return 0.12F + 0.6F * gape * (0.6F + 0.4F * perch);
    }

    /** The bite's lunge (0 to 1 and back over its run: the neck thrown forward). */
    public static float biteLunge(float bite) {
        if (bite <= 0.0F || bite >= 1.0F) {
            return 0.0F;
        }
        return bite < 0.35F ? -0.25F * smooth(bite / 0.35F) : bite < 0.55F ? -0.25F + 1.25F * outQuad((bite - 0.35F) / 0.2F)
            : 1.0F - smooth((bite - 0.55F) / 0.45F);
    }

    /** The bite's mouth: thrown wide as it rears back, snapped shut at the lunge's end, opened a little after. */
    public static float biteMouth(float bite) {
        if (bite <= 0.0F || bite >= 1.0F) {
            return 0.0F;
        }
        return bite < 0.4F ? smooth(bite / 0.3F) : bite < 0.55F ? 1.0F - outQuad((bite - 0.4F) / 0.15F) : 0.15F * (1.0F - smooth((bite - 0.6F) / 0.4F));
    }

    /**
     * The disturbing twitch on the ground: now and then the head snaps over to one side (an owl's tilt, too far), holds, and snaps back;
     * degrees of roll.
     */
    static float twitch(float t) {
        float c = (float) ((t * 0.21) % 1.0);
        float side = ((int) Math.floor(t * 0.21) & 1) == 0 ? 1.0F : -1.0F;
        float snap = smoothStep((c - 0.70F) / 0.04F) * (1.0F - smoothStep((c - 0.86F) / 0.05F));
        return side * 38.0F * snap + 4.0F * (float) Math.sin(t * 23.0) * snap;
    }

    /** A jerk: the head's look snapping between still angles (no glide between them), -1 to 1. */
    static float jerk(float t, float every) {
        int k = (int) Math.floor(t / every);
        float c = t / every - k;
        float a = hash(k);
        float b = hash(k + 1);
        return a + (b - a) * smoothStep((c - 0.85F) / 0.1F);
    }

    /** The pigeon's head bob over one step (phase radians): held back most of the step, then thrust forward fast; -1 to 1. */
    static float thrust(float phase) {
        float c = (float) ((phase / (Math.PI * 2.0)) % 1.0);
        if (c < 0) {
            c += 1.0F;
        }
        return c < 0.75F ? 1.0F - 2.0F * smooth(c / 0.75F) : -1.0F + 2.0F * outQuad((c - 0.75F) / 0.25F);
    }

    static float hash(int k) {
        int h = k * 0x27D4EB2D;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return ((h & 0xFFFF) / 65535.0F) * 2.0F - 1.0F;
    }

    // ------------------------------------------------------------------------------------------------------------------ the math

    /** Turns a bone by degrees about an axis of its parent's frame (applied before its own turn). */
    static void turn(DragonPose pose, int bone, float degrees, float ax, float ay, float az) {
        if (bone < 0 || degrees == 0.0F) {
            return;
        }
        float l = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        pose.bend(bone, degrees, ax / l, ay / l, az / l);
    }

    /** A bone's turn set to: yaw about the parent's up, then pitch about its across (degrees). */
    static void quat(DragonPose pose, int bone, float yaw, float pitch) {
        turn(pose, bone, yaw, 0, 1, 0);
        turn(pose, bone, pitch, 1, 0, 0);
    }

    /** The least turn (quaternion x, y, z, w) taking unit a onto unit b. */
    static float[] onto(float ax, float ay, float az, float bx, float by, float bz) {
        float bl = (float) Math.sqrt(bx * bx + by * by + bz * bz);
        bx /= bl;
        by /= bl;
        bz /= bl;
        float d = ax * bx + ay * by + az * bz;
        float cx = ay * bz - az * by;
        float cy = az * bx - ax * bz;
        float cz = ax * by - ay * bx;
        float w = 1.0F + d;
        if (w < 1.0E-5F) {
            // opposite: half a turn about any axis across a
            float px = Math.abs(ax) < 0.9F ? 1.0F : 0.0F;
            float py = px == 1.0F ? 0.0F : 1.0F;
            cx = ay * 0 - az * py;
            cy = az * px - ax * 0;
            cz = ax * py - ay * px;
            w = 0.0F;
        }
        float l = (float) Math.sqrt(cx * cx + cy * cy + cz * cz + w * w);
        return new float[] {cx / l, cy / l, cz / l, w / l};
    }

    static float[] toRot(float[] q) {
        float x = q[0];
        float y = q[1];
        float z = q[2];
        float w = q[3];
        return new float[] {1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w), 2 * (x * y + z * w), 1 - 2 * (x * x + z * z),
            2 * (y * z - x * w), 2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y)};
    }

    static float[] mulRot(float[] a, float[] b) {
        float[] o = new float[9];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                o[r * 3 + c] = a[r * 3] * b[c] + a[r * 3 + 1] * b[3 + c] + a[r * 3 + 2] * b[6 + c];
            }
        }
        return o;
    }

    /** A rotation's columns made unit and square (a pose's global carries the bones' scale). */
    static void orthonormal(float[] m) {
        for (int c = 0; c < 3; c++) {
            float l = (float) Math.sqrt(m[c] * m[c] + m[3 + c] * m[3 + c] + m[6 + c] * m[6 + c]);
            if (l > 1.0E-6F) {
                m[c] /= l;
                m[3 + c] /= l;
                m[6 + c] /= l;
            }
        }
    }

    static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    static float clamp(float t) {
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    static float clampf(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    static float smooth(float t) {
        t = clamp(t);
        return t * t * (3.0F - 2.0F * t);
    }

    static float smoothStep(float t) {
        return smooth(t);
    }

    static float outQuad(float t) {
        t = clamp(t);
        return 1.0F - (1.0F - t) * (1.0F - t);
    }
}
