package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;

/**
 * 3.6.7 (the owner: "Tori Tori no Mi, Model: Phoenix ... I made the model and sent it in the zip, first of all animate it so it looks
 * good, good flying animations"; "its a burning phoenix"; "the animations have to be top tier"): the owner's phoenix (the baked model,
 * tools/phoenix_bake.py) posed: its own take ("soar": the file's soaring wingbeat) and the states built on its rig, every one alive:
 * the flames rippling all the time ({@link #flame}). Pure: the game's renderer, the previews and the tests share it.
 */
public final class PhoenixForm {
    private PhoenixForm() {
    }

    /** 3.6.9: the form's phases (server/PhoenixServer), how long it takes to form and to go (ticks). */
    public static final int HUMAN = 0;
    public static final int FORMING = 1;
    public static final int PHOENIX = 2;
    public static final int UNFORMING = 3;
    public static final int FORM_TICKS = 30;
    public static final int UNFORM_TICKS = 18;

    /** How far the phoenix has formed (0 to 1, a little past 1 as it bursts out), {@code ticks} into {@code phase}. */
    public static float grow(int phase, float ticks) {
        return switch (phase) {
            case FORMING -> (float) springy((ticks - 4.0F) / (FORM_TICKS - 6.0F));
            case UNFORMING -> 1.0F - smooth((ticks - 4.0F) / (UNFORM_TICKS - 4.0F));
            case PHOENIX -> 1.0F;
            default -> 0.0F;
        };
    }

    /** How wrapped its wings are round its body (the reference's frames 112 and 136: a ring of wing round it), forming and going. */
    public static float wrap(int phase, float ticks) {
        return switch (phase) {
            case FORMING -> 0.6F * (1.0F - smooth((ticks - 8.0F) / 12.0F));
            case UNFORMING -> 0.6F * smooth(ticks / 8.0F);
            default -> 0.0F;
        };
    }

    /** Whether the human body is hidden (the phoenix is out, or most of the way). */
    public static boolean humanHidden(int phase, float ticks) {
        return phase == PHOENIX || phase == FORMING && ticks >= 8.0F || phase == UNFORMING && ticks < UNFORM_TICKS - 4.0F;
    }

    static float smooth(float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        return t * t * (3.0F - 2.0F * t);
    }

    static double springy(double t) {
        double u = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - Math.pow(1.0 - u, 3.0) + 0.15 * Math.sin(Math.PI * u) * u;
    }

    /** The rig's bones the poses turn, found once, and the model's floor (its lowest point at rest, model units). */
    public static final class Shape {
        public final int root;
        public final int body;
        public final int neck;
        public final int neckReach;
        public final int head;
        public final int tailRoot;
        public final int[] tail = new int[3];
        public final int[][] wing = new int[2][5];
        public final int[] leg = new int[2];
        public final float floor;
        public final float top;
        /** 3.6.9: its claws' lowest point (standing on them), its chest's height (held at the player's middle in flight). */
        public final float feet;
        public final float chest;
        /** Every bone's parent, and its rest rotation in the model's space (3 x 3 row-major): bends are given in the model's axes. */
        final int[] parent;
        final float[] restRot;
        /**
         * 3.6.7 (the owner: "the body is made of flames, my model did not burn, which is what I want. moving flames"): which parts are
         * fire (all but the bill, eyes, feet, legs, insignia and the head and breast's own mesh), each vertex's reach out along its part
         * from its bone's pivot (0 there, 1 at its farthest tip), and the points the fire's tongues stream off (part, vertex).
         */
        public final boolean[] fire;
        public final float[][] reach;
        public final int[] anchors;
        /** Each fire point's tongues' size (1 the wings' and the mantle's). */
        public final float[] anchorSize;

        public Shape(DragonModel m) {
            this.parent = m.parent;
            DragonPose rest = new DragonPose(m);
            rest.solve();
            this.restRot = new float[m.bones() * 9];
            for (int b = 0; b < m.bones(); b++) {
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        this.restRot[b * 9 + r * 3 + c] = rest.global[b * 12 + r * 4 + c];
                    }
                }
            }
            int np = m.parts.length;
            this.fire = new boolean[np];
            this.reach = new float[np][];
            // the fire points by body part (the reference: the flames stream off the wings' edges most, then the mantle, the crest
            // and the tail): each group's candidates, then each group's share of the tongues spread evenly over them
            String[] groups = {"Wing L", "Wing R", "Mantle", "Mane", "Blue tail fan", "Tail"};
            int[] quota = {16, 16, 10, 8, 6, 8};
            java.util.List<java.util.List<int[]>> pools = new java.util.ArrayList<>();
            for (int g = 0; g < groups.length; g++) {
                pools.add(new java.util.ArrayList<>());
            }
            for (int i = 0; i < np; i++) {
                DragonModel.Part part = m.parts[i];
                String name = part.name;
                this.fire[i] = !(name.startsWith("Bill") || name.startsWith("Eye") || name.startsWith("Foot") || name.startsWith("Leg")
                    || name.startsWith("Insignia") || name.startsWith("Marco"));
                int nv = part.vertices();
                this.reach[i] = new float[nv];
                if (nv == 0) {
                    continue;
                }
                int bone = part.bone[0] & 0xFF;
                float px = rest.global[bone * 12 + 3];
                float py = rest.global[bone * 12 + 7];
                float pz = rest.global[bone * 12 + 11];
                float most = 1.0E-6F;
                for (int v = 0; v < nv; v++) {
                    float dx = part.pos[v * 3] - px;
                    float dy = part.pos[v * 3 + 1] - py;
                    float dz = part.pos[v * 3 + 2] - pz;
                    this.reach[i][v] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    most = Math.max(most, this.reach[i][v]);
                }
                for (int v = 0; v < nv; v++) {
                    this.reach[i][v] /= most;
                }
                if (this.fire[i]) {
                    int g = -1;
                    for (int k = 0; k < groups.length && g < 0; k++) {
                        if (name.startsWith(groups[k])) {
                            g = k;
                        }
                    }
                    if (g >= 0) {
                        // the far part of it (its edge and tips), a few points a part
                        int step = Math.max(1, nv / 12);
                        for (int v = step / 2; v < nv; v += step) {
                            if (this.reach[i][v] > 0.6F) {
                                pools.get(g).add(new int[] {i, v});
                            }
                        }
                    }
                }
            }
            java.util.List<int[]> chosen = new java.util.ArrayList<>();
            java.util.List<Integer> group = new java.util.ArrayList<>();
            for (int g = 0; g < groups.length; g++) {
                java.util.List<int[]> pool = pools.get(g);
                int keep = Math.min(quota[g], pool.size());
                for (int k = 0; k < keep; k++) {
                    chosen.add(pool.get((int) ((long) (2 * k + 1) * pool.size() / (2L * keep))));
                    group.add(g);
                }
            }
            this.anchors = new int[chosen.size() * 2];
            this.anchorSize = new float[chosen.size()];
            for (int k = 0; k < chosen.size(); k++) {
                this.anchors[k * 2] = chosen.get(k)[0];
                this.anchors[k * 2 + 1] = chosen.get(k)[1];
                int g = group.get(k);
                // the wings' and the mantle's tongues the biggest, the crest's and tail's smaller
                this.anchorSize[k] = g <= 2 ? 1.0F : g == 3 ? 0.75F : 0.6F;
            }
            this.root = m.bone("root");
            this.body = m.bone("body");
            this.neck = m.bone("neck");
            this.neckReach = m.bone("neck_reach");
            this.head = m.bone("head");
            this.tailRoot = m.bone("tail_root");
            for (int i = 0; i < 3; i++) {
                this.tail[i] = m.bone("tail." + (i + 1));
            }
            String[] seg = {"wing_inner", "wing_elbow", "wing_outer", "wing_hand", "wing_tip"};
            for (int s = 0; s < 2; s++) {
                String side = s == 0 ? ".L" : ".R";
                for (int k = 0; k < 5; k++) {
                    this.wing[s][k] = m.bone(seg[k] + side);
                }
                this.leg[s] = m.bone("leg" + side);
            }
            float lo = Float.MAX_VALUE;
            float hi = -Float.MAX_VALUE;
            for (DragonModel.Part part : m.parts) {
                for (int v = 0; v < part.vertices(); v++) {
                    lo = Math.min(lo, part.pos[v * 3 + 1]);
                    hi = Math.max(hi, part.pos[v * 3 + 1]);
                }
            }
            this.floor = lo;
            this.top = hi;
            float feetLo = Float.MAX_VALUE;
            for (DragonModel.Part part : m.parts) {
                if (part.name.startsWith("Foot")) {
                    for (int v = 0; v < part.vertices(); v++) {
                        feetLo = Math.min(feetLo, part.pos[v * 3 + 1]);
                    }
                }
            }
            this.feet = feetLo == Float.MAX_VALUE ? lo : feetLo;
            int bodyBone = m.bone("body");
            DragonPose at = new DragonPose(m);
            at.solve();
            this.chest = bodyBone < 0 ? (lo + hi) * 0.5F : at.global[bodyBone * 12 + 7] + 0.6F;
        }
    }

    /**
     * The fire alive (the owner: "the body is made of flames ... moving flames"; the reference: Marco's blue fire never still, flowing
     * like liquid): every fire part flows all the time: waves run out along it from its root to its tips, licking upward and fluttering
     * side to side, the tips moving most (their reach), the warm flames (tail, mane, wings' fire) more than the cool plumage.
     * {@code part} is model part {@code index}; pos and nrm its skinned vertices (model units), changed in place.
     */
    public static void flame(Shape sh, int index, DragonModel.Part part, float[] pos, float[] nrm, float seconds) {
        if (!sh.fire[index]) {
            return;
        }
        float[] reach = sh.reach[index];
        float amp0 = part.kind == PhoenixLook.WARM ? FLAME : FLAME * 0.6F;
        float t = seconds;
        float phase = index * 1.618F;
        int n = part.vertices();
        for (int v = 0; v < n; v++) {
            float r = reach[v];
            if (r < 0.05F) {
                continue;
            }
            float amp = amp0 * r * (float) Math.sqrt(r);
            float x = pos[v * 3];
            float y = pos[v * 3 + 1];
            float z = pos[v * 3 + 2];
            // a wave running out to the tips, and a quicker flicker crossing it
            float run = r * 7.0F - t * 9.0F + phase;
            float w1 = (float) Math.sin(run);
            float w2 = (float) Math.sin(run * 1.7F + x * 1.3F + z * 0.9F - t * 5.3F);
            float lick = 0.5F + 0.5F * (float) Math.sin(run * 0.8F + y * 1.1F - t * 6.7F);
            float side = (float) Math.sin(run * 1.3F + 1.0F + y * 0.7F);
            pos[v * 3] += nrm[v * 3] * amp * 0.55F * w1 + amp * 0.35F * side;
            pos[v * 3 + 1] += nrm[v * 3 + 1] * amp * 0.55F * w1 + amp * (0.45F * lick + 0.15F * w2);
            pos[v * 3 + 2] += nrm[v * 3 + 2] * amp * 0.55F * w1 + amp * 0.25F * w2;
        }
    }

    /** As {@link #flame}, every part's index found by identity (previews). */
    public static void flame(Shape sh, DragonModel m, DragonModel.Part part, float[] pos, float[] nrm, float seconds) {
        for (int i = 0; i < m.parts.length; i++) {
            if (m.parts[i] == part) {
                flame(sh, i, part, pos, nrm, seconds);
                return;
            }
        }
    }

    /** How many tongues of fire stream off the body at once (the effect's budget: about 40 quads each). */
    public static final int TONGUES = 64;

    /** How long a tongue of fire lasts (ticks, the shortest), how high it rises in that time (blocks). */
    public static final float TONGUE_LIFE = 8.0F;
    public static final float TONGUE_RISE = 1.1F;

    /**
     * Turns {@code bone} by {@code degrees} about an axis given in the MODEL's space (x right of the screen when it faces you: the
     * model's -X is its left wing; y up; z backward, the head toward -Z), through the bone's own pivot: the axis is taken into its
     * parent's rest frame (DragonPose.bend turns about the parent's axes).
     */
    static void turn(DragonPose pose, Shape sh, int bone, float degrees, float ax, float ay, float az) {
        if (bone < 0 || degrees == 0.0F) {
            return;
        }
        int p = sh.parent[bone];
        if (p < 0) {
            pose.bend(bone, degrees, ax, ay, az);
            return;
        }
        float[] r = sh.restRot;
        int o = p * 9;
        float lx = r[o] * ax + r[o + 3] * ay + r[o + 6] * az;
        float ly = r[o + 1] * ax + r[o + 4] * ay + r[o + 7] * az;
        float lz = r[o + 2] * ax + r[o + 5] * ay + r[o + 8] * az;
        float l = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
        pose.bend(bone, degrees, lx / l, ly / l, lz / l);
    }

    /** How far the fire flows at its tips (model units: 0.4 blocks each). */
    static final float FLAME = 0.42F;

    /** The flight's state each frame (the client works it out from how the player moves; the previews set it). */
    public static final class Motion {
        /** 0 hovering in place to 1 full speed ahead. */
        public float speed;
        /** 0 to 1: soaring on the owner's own take instead of beating. */
        public float glide;
        /** 0 to 1: wings swept back, diving. */
        public float dive;
        /** 0 to 1: how far it has formed (the transformation; 1 formed). */
        public float form = 1.0F;
        /** 0 to 1: standing on the ground, wings folded. */
        public float perch;
        /** 0 to 1: its wings wrapped round its body (forming and going). */
        public float wrap;
    }

    /**
     * The phoenix posed (the owner: "good flying animations"; "the animations have to be top tier"): {@code beat} the wingbeat's phase
     * (radians, run on by the caller at the beat's own rate: {@link #beatRate}), {@code seconds} the clock. A full powered flap: the
     * downstroke sweeps down and a little forward, the joints out along the wing following late so a wave runs out to the tips (the
     * follow-through); the upstroke folds the wing in (elbow back, hand forward) and lifts it; the body rises with each downstroke and
     * rocks with it, the head held steady against it (a bird's), the tail swinging late and its three streamers swaying lazily on their
     * own; at speed the body leans forward into the flight and the beat is shallower and quicker; gliding it soars on the owner's take;
     * diving the wings sweep back folded.
     */
    public static void pose(DragonModel m, Shape sh, DragonPose pose, Motion mo, float beat, float seconds) {
        pose.rest();
        float glide = clamp(mo.glide);
        float dive = clamp(mo.dive);
        float speed = clamp(mo.speed);
        if (glide > 0.0F) {
            pose.clip(m.clips.get("soar"), seconds, true, glide);
        }
        float perch = clamp(mo.perch);
        float wrapped = clamp(mo.wrap);
        // standing (the reference's phoenix on the ground, frames 96 to 144): on its claws, its wings still beating, slower and gentler
        float flap = (1.0F - glide) * (1.0F - dive) * (1.0F - wrapped);
        float amp = (34.0F - 10.0F * speed) * flap * (1.0F - 0.35F * perch);
        // the power stroke: the downstroke quick, the recovery slower (the phase run faster through the downstroke's middle)
        beat = beat + 0.35F * (float) Math.sin(beat);
        float sb = (float) Math.sin(beat);
        float cb = (float) Math.cos(beat);
        // the upstroke (the wing rising: sin falling) folds it in
        float up = Math.max(0.0F, -cb);
        float fold = flap * 0.75F * up * up + dive * 0.85F;
        for (int s = 0; s < 2; s++) {
            float m0 = s == 0 ? 1.0F : -1.0F;
            int[] w = sh.wing[s];
            // the rest's raised wings levelled out for flight, the flap, its forward sweep down and back up, the dive's sweep
            // the stroke centred a little under the rest's raised wings: about 52 degrees up at its top, 16 under level at its bottom
            turn(pose, sh, w[0], m0 * (amp * sb + 12.0F * flap + 14.0F * speed + 22.0F * dive), 0, 0, 1);
            turn(pose, sh, w[0], m0 * (-9.0F * cb * flap + 55.0F * dive + 8.0F * speed), 0, 1, 0);
            // the follow-through: every joint out along the wing a little later, the wave running out to the tips
            turn(pose, sh, w[1], m0 * amp * 0.32F * (float) Math.sin(beat - 0.6F), 0, 0, 1);
            turn(pose, sh, w[2], m0 * amp * 0.30F * (float) Math.sin(beat - 1.1F), 0, 0, 1);
            turn(pose, sh, w[3], m0 * amp * 0.28F * (float) Math.sin(beat - 1.6F), 0, 0, 1);
            turn(pose, sh, w[4], m0 * amp * 0.26F * (float) Math.sin(beat - 2.1F), 0, 0, 1);
            // the fold: the elbow back, the hand forward, the tip in
            turn(pose, sh, w[1], m0 * fold * 42.0F, 0, 1, 0);
            turn(pose, sh, w[3], -m0 * fold * 58.0F, 0, 1, 0);
            turn(pose, sh, w[4], -m0 * fold * 25.0F, 0, 1, 0);
            // wrapped: the wings swept forward and round the body like a ring of fire (the reference's frames 112 and 136)
            turn(pose, sh, w[0], m0 * wrapped * 30.0F, 0, 0, 1);
            turn(pose, sh, w[0], -m0 * wrapped * 75.0F, 0, 1, 0);
            turn(pose, sh, w[2], -m0 * wrapped * 60.0F, 0, 1, 0);
            turn(pose, sh, w[3], -m0 * wrapped * 55.0F, 0, 1, 0);
            // the legs tucked up and back in flight, hanging under the hover
            // (about the model's x a negative turn swings a hanging part back: the head is toward -Z)
            turn(pose, sh, sh.leg[s], -45.0F * speed - 30.0F * dive + 6.0F * flap * (float) Math.sin(beat - 2.0F), 1, 0, 0);
        }
        // the body: lifted with each downstroke, rocked by it; leaning into the flight at speed and nose down in the dive
        pose.t[sh.root * 3 + 1] += 0.32F * flap * (1.0F - perch) * (float) Math.sin(beat - 1.0F);
        turn(pose, sh, sh.root, -65.0F * speed - 30.0F * dive, 1, 0, 0);
        turn(pose, sh, sh.body, 4.5F * flap * (float) Math.sin(beat - 0.4F), 1, 0, 0);
        // the head held steady against the body's rock, lifted back up to look ahead when it leans
        turn(pose, sh, sh.neck, -4.0F * flap * (float) Math.sin(beat - 0.4F) + 38.0F * speed + 15.0F * dive, 1, 0, 0);
        turn(pose, sh, sh.head, 2.0F * (float) Math.sin(seconds * 0.9F) + 10.0F * speed, 1, 0, 0);
        turn(pose, sh, sh.head, (6.0F + 14.0F * perch) * (float) Math.sin(seconds * 0.43F), 0, 1, 0);
        // the tail: swinging late behind the beat, streaming out behind at speed; its three streamers swaying on their own
        turn(pose, sh, sh.tailRoot, 8.0F * flap * (float) Math.sin(beat - 1.7F) - 30.0F * speed - 30.0F * dive, 1, 0, 0);
        for (int k = 0; k < 3; k++) {
            turn(pose, sh, sh.tail[k], 9.0F * (float) Math.sin(seconds * 1.9F + k * 1.3F) * (1.0F - 0.5F * speed), 0, 0, 1);
            turn(pose, sh, sh.tail[k], 7.0F * (float) Math.sin(seconds * 1.4F + k * 2.1F) + 6.0F * flap * (float) Math.sin(beat - 2.4F - k * 0.4F)
                - 22.0F * speed, 1, 0, 0);
        }
        pose.solve();
    }

    /** How fast the wings beat (radians a second): slow and deep hovering, quicker and shallower at speed. */
    public static float beatRate(Motion mo) {
        return (float) (2 * Math.PI / (1.05F - 0.4F * clamp(mo.speed) + 0.55F * clamp(mo.perch)));
    }

    static float clamp(float t) {
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    /**
     * A preview's pose by name (tools/preview/PhoenixPreview): "rest", "soar" (the owner's take at {@code seconds}), "hover", "fly",
     * "fast", "glide", "dive"; and the place of the model in the world into {@code place} (3 x 4: blocks, its feet over the ground
     * at the origin, facing -Z).
     */
    public static void preview(DragonModel m, Shape shape, DragonPose pose, String state, float seconds, float[] place) {
        Motion mo = new Motion();
        float lift = 1.5F;
        switch (state) {
            case "hover" -> mo.speed = 0.0F;
            case "fly" -> mo.speed = 0.6F;
            case "fast" -> mo.speed = 1.0F;
            case "glide" -> {
                mo.speed = 0.5F;
                mo.glide = 1.0F;
            }
            case "dive" -> {
                mo.speed = 1.0F;
                mo.dive = 1.0F;
            }
            case "perch" -> {
                mo.perch = 1.0F;
                lift = 0.0F;
            }
            case "wrap" -> mo.wrap = 1.0F;
            default -> lift = 0.0F;
        }
        if (state.equals("rest") || state.equals("soar") || state.startsWith("t")) {
            pose.rest();
            if (state.equals("soar")) {
                pose.clip(m.clips.get("soar"), seconds, true, 1.0F);
            }
            if (state.startsWith("tx") || state.startsWith("ty") || state.startsWith("tz")) {
                int bone = shape.wing[0][state.length() > 2 ? state.charAt(2) - '0' : 0];
                turn(pose, shape, bone, 40.0F, state.charAt(1) == 'x' ? 1 : 0, state.charAt(1) == 'y' ? 1 : 0, state.charAt(1) == 'z' ? 1 : 0);
            }
            pose.solve();
        } else {
            pose(m, shape, pose, mo, seconds * beatRate(mo), seconds);
        }
        float s = PhoenixLook.SCALE;
        float base = state.equals("perch") ? -shape.feet * s : -shape.floor * s + lift;
        float[] p = {s, 0, 0, 0, 0, s, 0, base, 0, 0, s, 0};
        System.arraycopy(p, 0, place, 0, 12);
    }
}
