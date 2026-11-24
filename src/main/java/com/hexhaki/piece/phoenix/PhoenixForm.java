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
            java.util.List<int[]> tips = new java.util.ArrayList<>();
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
                    // the far half of every fire part streams tongues: its points there, a few a part
                    int step = Math.max(1, nv / 3);
                    for (int v = step / 2; v < nv; v += step) {
                        if (this.reach[i][v] > 0.55F) {
                            tips.add(new int[] {i, v});
                        }
                    }
                }
            }
            // spread evenly over every part, at most TONGUES of them
            int keep = Math.min(TONGUES, tips.size());
            this.anchors = new int[keep * 2];
            for (int k = 0; k < keep; k++) {
                int[] t = tips.get((int) ((long) k * tips.size() / keep));
                this.anchors[k * 2] = t[0];
                this.anchors[k * 2 + 1] = t[1];
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
        float flap = (1.0F - glide) * (1.0F - dive);
        float amp = (42.0F - 12.0F * speed) * flap;
        float sb = (float) Math.sin(beat);
        float cb = (float) Math.cos(beat);
        // the upstroke (the wing rising: sin falling) folds it in
        float up = Math.max(0.0F, -cb);
        float fold = flap * 0.75F * up * up + dive * 0.85F;
        for (int s = 0; s < 2; s++) {
            float m0 = s == 0 ? 1.0F : -1.0F;
            int[] w = sh.wing[s];
            // the rest's raised wings levelled out for flight, the flap, its forward sweep down and back up, the dive's sweep
            turn(pose, sh, w[0], m0 * (amp * sb + 14.0F * speed + 22.0F * dive), 0, 0, 1);
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
            // the legs tucked up and back in flight, hanging under the hover
            turn(pose, sh, sh.leg[s], 35.0F * speed + 6.0F * flap * (float) Math.sin(beat - 2.0F), 1, 0, 0);
        }
        // the body: lifted with each downstroke, rocked by it; leaning into the flight at speed and nose down in the dive
        pose.t[sh.root * 3 + 1] += 0.32F * flap * (float) Math.sin(beat - 1.0F);
        turn(pose, sh, sh.root, -55.0F * speed - 30.0F * dive, 1, 0, 0);
        turn(pose, sh, sh.body, 4.5F * flap * (float) Math.sin(beat - 0.4F), 1, 0, 0);
        // the head held steady against the body's rock, lifted back up to look ahead when it leans
        turn(pose, sh, sh.neck, -4.0F * flap * (float) Math.sin(beat - 0.4F) + 30.0F * speed + 15.0F * dive, 1, 0, 0);
        turn(pose, sh, sh.head, 2.0F * (float) Math.sin(seconds * 0.9F) + 10.0F * speed, 1, 0, 0);
        turn(pose, sh, sh.head, 6.0F * (float) Math.sin(seconds * 0.43F), 0, 1, 0);
        // the tail: swinging late behind the beat, streaming out behind at speed; its three streamers swaying on their own
        turn(pose, sh, sh.tailRoot, 8.0F * flap * (float) Math.sin(beat - 1.7F) + 25.0F * speed + 30.0F * dive, 1, 0, 0);
        for (int k = 0; k < 3; k++) {
            turn(pose, sh, sh.tail[k], 9.0F * (float) Math.sin(seconds * 1.9F + k * 1.3F) * (1.0F - 0.5F * speed), 0, 0, 1);
            turn(pose, sh, sh.tail[k], 7.0F * (float) Math.sin(seconds * 1.4F + k * 2.1F) + 6.0F * flap * (float) Math.sin(beat - 2.4F - k * 0.4F),
                1, 0, 0);
        }
        pose.solve();
    }

    /** How fast the wings beat (radians a second): slow and deep hovering, quicker and shallower at speed. */
    public static float beatRate(Motion mo) {
        return (float) (2 * Math.PI / (1.05F - 0.4F * clamp(mo.speed)));
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
        float[] p = {s, 0, 0, 0, 0, s, 0, -shape.floor * s + lift, 0, 0, s, 0};
        System.arraycopy(p, 0, place, 0, 12);
    }
}
