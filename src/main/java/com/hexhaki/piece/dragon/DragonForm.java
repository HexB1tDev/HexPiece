package com.hexhaki.piece.dragon;

/**
 * 2.7.0 (the owner: "Add a new fruit called Uo Uo No Mi, make Z a button to transform into the model I sent, animate the model, add a
 * transformation animation, flying animation and de-transform animation"; "it should be at least 6 times bigger than the player";
 * "cant you just reapply the whole model and just resize it?"): the dragon form's timing, poses and placement. Pure: the game draws
 * what this poses, tools/preview/DragonPreview renders the same, DragonTests checks it.
 * <ul>
 *   <li>The model is the owner's, untouched, one block a unit ({@link #SCALE}): the hover 14 blocks tall (12.3 measured from the
 *       claws), 6.8 times a player, 25 long; its head {@link #HEAD_HEIGHT} above the player's feet, so the claws hang to them.</li>
 *   <li>Transformation ({@link #FORMING}): the model's own take (metamorphosis_a, 2.2 s): the dragon bursts head first out of the
 *       ground behind the player in a long rising line, arcs over and coils down into its hover where the player stands.</li>
 *   <li>The hover: the model's idle take, looped from the moment the transformation ends (no seam).</li>
 *   <li>Flight: blended in by the player's speed: the spine laid out straight behind the head (the model's bind is a dragon flying
 *       straight ahead), a travelling wave down it, the limbs and mane as in the hover; the whole body pitched with the look and
 *       banked into turns.</li>
 *   <li>The way back ({@link #UNFORMING}): the model's own take (metamorphosis_b, 1.5 s): uncoiling, it dives head first into the
 *       ground and the human stands where it went in.</li>
 * </ul>
 */
public final class DragonForm {
    public static final int HUMAN = 0;
    public static final int FORMING = 1;
    public static final int DRAGON = 2;
    public static final int UNFORMING = 3;
    /** Blocks a model unit. */
    public static final float SCALE = 1.0F;
    /** The hover's head above the player's feet (blocks): the claws, 6 units under it, at the feet. */
    public static final float HEAD_HEIGHT = 6.0F;
    /** The transformation and the way back: their takes' lengths (ticks). */
    public static final int FORM_TICKS = 44;
    public static final int UNFORM_TICKS = 30;
    /**
     * The transformation grows the dragon out of the player's chest ({@link #CHEST}, blocks over the feet) and the way back shrinks it
     * into it: the human is gone once the dragon outgrows it, and back as the last of it goes in.
     */
    public static final float HUMAN_GONE_AT = 0.15F;
    public static final float HUMAN_BACK_AT = 1.30F;
    static final float CHEST = 1.0F;
    /** The growth (seconds from the start) and the shrink (seconds before the end). */
    static final float GROW = 0.7F;
    /** Its size at the first and last moment: inside the player (its take starts as an 85-unit line). */
    static final float SMALLEST = 0.015F;
    static final float SHRINK = 0.75F;
    /** How long the take's own path takes to leave the player's place (the transformation) and to reach it (the way back). */
    static final float LEAVE = 1.2F;
    static final float REACH_FROM = 0.3F;
    /** How long the transformation's end settles onto the hover's first frame (seconds). */
    static final float SETTLE = 0.45F;
    /** How long the way back takes over the hover or the flight (seconds). */
    static final float UNFORM_BLEND = 0.3F;
    /** The flight wave: its period (seconds at a hover's pace) and its length (segments of the spine). */
    static final float WAVE_PERIOD = 1.6F;
    static final float WAVE_SEGMENTS = 11.0F;

    /** The mantle's tails fluttering in flight (degrees). */
    static final float COAT_FLUTTER = 3.0F;
    static final String[] TAIL = new String[23];
    static final String[] NECK = {"Body_Chest", "Head_Neck_01", "Head_Neck_02", "Head_Neck_03", "Head_Neck_04", "Head_Neck_05", "Head_Face"};

    static {
        for (int i = 0; i < 23; i++) {
            TAIL[i] = String.format("Body_%02d", i + 1);
        }
    }

    private DragonForm() {
    }

    /** What a model needs once: its bones by name and the head's place in the hover and in flight. */
    public static final class Shape {
        final int pelvis;
        final int face;
        final int[] tail = new int[TAIL.length];
        final int[] neck = new int[NECK.length];
        final int[] mantle;
        final int[] coatChain;
        public final int jaw;
        /** The head in the hover (the idle's average) and in straight flight (model units). */
        public final float[] hoverHead = new float[3];
        public final float[] flightHead = new float[3];
        /** The head where the transformation's take starts and where the way back's ends (model units, after the alignment). */
        public final float[] formStart = new float[3];
        public final float[] unformEnd = new float[3];

        public Shape(DragonModel model) {
            this.pelvis = model.bone("Body_Pelvis");
            this.face = model.bone("Head_Face");
            for (int i = 0; i < TAIL.length; i++) {
                this.tail[i] = model.bone(TAIL[i]);
            }
            for (int i = 0; i < NECK.length; i++) {
                this.neck[i] = model.bone(NECK[i]);
            }
            this.jaw = model.bone("jaw");
            this.coatChain = new int[] {model.bone("c_coat02"), model.bone("c_coat03"), model.bone("l_coat02"), model.bone("r_coat02")};
            // the mantle: coat_root and every bone under it
            int root = model.bone("coat_root");
            java.util.List<Integer> under = new java.util.ArrayList<>();
            for (int b = 0; b < model.bones(); b++) {
                for (int up = b; up >= 0; up = model.parent[up]) {
                    if (up == root) {
                        under.add(b);
                        break;
                    }
                }
            }
            this.mantle = under.stream().mapToInt(Integer::intValue).toArray();
            DragonPose pose = new DragonPose(model);
            DragonModel.Clip idle = model.clips.get("idle");
            float[] o = new float[3];
            int n = 0;
            for (float t = 0.0F; t < idle.seconds(); t += 0.1F) {
                pose.rest();
                pose.clip(idle, t, true, 1.0F);
                pose.solve();
                pose.origin(this.face, o);
                for (int k = 0; k < 3; k++) {
                    this.hoverHead[k] += o[k];
                }
                n++;
            }
            for (int k = 0; k < 3; k++) {
                this.hoverHead[k] /= n;
            }
            pose.rest();
            pose.clip(idle, 0.0F, true, 1.0F);
            straighten(this, pose, 0.0F, 1.0F, 0.0F);
            pose.solve();
            pose.origin(this.face, this.flightHead);
            pose.rest();
            pose.clip(model.clips.get("form"), 0.0F, false, 1.0F);
            pose.solve();
            pose.origin(this.face, this.formStart);
            DragonModel.Clip unform = model.clips.get("unform");
            pose.rest();
            pose.clip(unform, unform.seconds(), false, 1.0F);
            pose.solve();
            pose.origin(this.face, this.unformEnd);
        }
    }

    /** The phase's length in ticks (the hover and the human: none). */
    public static int length(int phase) {
        return phase == FORMING ? FORM_TICKS : phase == UNFORMING ? UNFORM_TICKS : 0;
    }

    /** Whether the human body is hidden {@code seconds} into {@code phase}. */
    public static boolean humanHidden(int phase, float seconds) {
        return phase == DRAGON || phase == FORMING && seconds >= HUMAN_GONE_AT || phase == UNFORMING && seconds < HUMAN_BACK_AT;
    }

    /** Whether the dragon is drawn {@code seconds} into {@code phase}. */
    public static boolean shown(int phase, float seconds) {
        return phase == FORMING || phase == DRAGON || phase == UNFORMING && seconds < UNFORM_TICKS / 20.0F;
    }

    /**
     * The pose {@code seconds} into {@code phase}: {@code hover} the seconds since the hover began (the idle's own clock),
     * {@code flight} how much it flies (0 to 1), {@code age} a running clock (seconds) for the wave. Leaves the pose solved and
     * writes the point placed at the anchor ({@link #placement}) into {@code anchor}.
     */
    public static void pose(DragonModel model, Shape shape, DragonPose pose, int phase, float seconds, float hover, float flight, float age,
                            float[] anchor) {
        pose(model, shape, pose, phase, seconds, hover, flight, age, 0.0F, -1.0F, anchor);
    }

    /**
     * 2.7.2: as above, with the moves' jaw and head: {@code mouth} how far the jaw is open (0 to 1: the breath, the charge), {@code bite}
     * how far into the Gigantic Bite (0 to 1, or below 0 for none: the jaws thrown wide and the head reared, then snapped shut and
     * driven forward).
     */
    public static void pose(DragonModel model, Shape shape, DragonPose pose, int phase, float seconds, float hover, float flight, float age,
                            float mouth, float bite, float[] anchor) {
        pose(model, shape, pose, phase, seconds, hover, flight, age, anchor, mouth, bite);
    }

    static void pose(DragonModel model, Shape shape, DragonPose pose, int phase, float seconds, float hover, float flight, float age,
                     float[] anchor, float mouth, float bite) {
        pose.rest();
        DragonModel.Clip idle = model.clips.get("idle");
        float fly = Math.max(0.0F, Math.min(1.0F, flight));
        if (phase == FORMING) {
            pose.clip(model.clips.get("form"), seconds, false, 1.0F);
            // the take ends near, not on, the hover's first frame (up to 3.5 units apart, the mane): eased onto it over its end
            float end = FORM_TICKS / 20.0F;
            pose.clip(idle, 0.0F, false, smooth((seconds - (end - SETTLE)) / SETTLE));
            fly = 0.0F;
        } else {
            pose.clip(idle, hover, true, 1.0F);
            if (phase == UNFORMING) {
                fly *= 1.0F - smooth(seconds / UNFORM_BLEND);
            }
            if (fly > 0.0F) {
                straighten(shape, pose, age, fly, fly);
            }
            if (phase == UNFORMING) {
                pose.clip(model.clips.get("unform"), seconds, false, smooth(seconds / UNFORM_BLEND));
            }
        }
        if (phase == DRAGON) {
            jaws(shape, pose, mouth, bite);
        }
        pose.solve();
        for (int k = 0; k < 3; k++) {
            anchor[k] = shape.hoverHead[k] + (shape.flightHead[k] - shape.hoverHead[k]) * fly;
        }
    }

    /**
     * The flight: the spine laid toward a straight line ahead of it by {@code weight} (the pelvis at the origin, every segment along
     * its bind), a wave travelling down from the neck to the tail by {@code wave}: up and down most, a little to the sides, larger
     * toward the tail.
     */
    static void straighten(Shape shape, DragonPose pose, float age, float weight, float wave) {
        float[] q = new float[4];
        set(pose, shape.pelvis, 0.0F, 0.0F, 0.0F, 1.0F, weight);
        lerpT(pose, shape.pelvis, 0.0F, 0.0F, 0.0F, weight);
        float phase = (float) (age / WAVE_PERIOD * Math.PI * 2.0);
        float step = (float) (Math.PI * 2.0 / WAVE_SEGMENTS);
        for (int i = 0; i < shape.tail.length; i++) {
            float along = i / (float) (shape.tail.length - 1);
            float up = (2.5F + 4.0F * along) * wave * (float) Math.sin(phase - i * step);
            float side = (1.2F + 2.3F * along) * wave * (float) Math.sin(phase * 0.71F - i * step * 0.8F + 1.3F);
            euler(up, side, q);
            set(pose, shape.tail[i], q[0], q[1], q[2], q[3], weight);
            lerpRest(pose, shape.tail[i], weight);
        }
        // 2.7.0 (the owner: "careful with the cape, the body cant clip through"): the cloud mantle on the shoulders (the model's
        // "coat", hung from the pelvis) keeps the hover's angles once the body lies straight and rears up through it; in flight it
        // goes back to the model's own bind (the shawl the artist laid over the shoulders of the straight body, clear of it), a little
        // flutter on its tails (DragonTests: never inside the body)
        for (int i = 0; i < shape.mantle.length; i++) {
            set(pose, shape.mantle[i], 0.0F, 0.0F, 0.0F, 1.0F, weight);
            lerpRest(pose, shape.mantle[i], weight);
        }
        for (int i = 0; i < shape.coatChain.length; i++) {
            pose.bend(shape.coatChain[i], COAT_FLUTTER * wave * (float) Math.sin(phase * 1.7F - i * 1.1F), 1.0F, 0.0F, 0.0F);
        }
        for (int i = 0; i < shape.neck.length; i++) {
            // the neck carries the wave forward a little, the head held level
            float up = i == shape.neck.length - 1 ? 0.0F : 1.5F * wave * (float) Math.sin(phase + (i + 1) * step);
            euler(up, 0.0F, q);
            set(pose, shape.neck[i], q[0], q[1], q[2], q[3], weight);
            lerpRest(pose, shape.neck[i], weight);
        }
    }

    /** How far the jaw opens at most (degrees), and the head's rear and lunge in the bite. */
    static final float JAW_OPEN = 38.0F;
    static final float BITE_REAR = -18.0F;
    static final float BITE_LUNGE = 14.0F;

    /**
     * The jaw opened (about the hinge's side axis: the jaw's front swings down) and, in the bite, the head reared back with the jaws wide
     * (to 0.45), then snapped shut and driven forward and down (0.45 to 0.6), easing back to the hover after.
     */
    static void jaws(Shape shape, DragonPose pose, float mouth, float bite) {
        float open = Math.max(0.0F, Math.min(1.0F, mouth));
        float head = 0.0F;
        if (bite >= 0.0F) {
            float b = Math.min(1.0F, bite);
            if (b < 0.45F) {
                float t = smooth(b / 0.45F);
                open = Math.max(open, t);
                head = BITE_REAR * t;
            } else if (b < 0.6F) {
                float t = smooth((b - 0.45F) / 0.15F);
                open = Math.max(open, 1.0F - t);
                head = BITE_REAR + (BITE_LUNGE - BITE_REAR) * t;
            } else {
                head = BITE_LUNGE * (1.0F - smooth((b - 0.6F) / 0.4F));
            }
        }
        if (shape.jaw >= 0 && open > 0.0F) {
            pose.bend(shape.jaw, JAW_OPEN * open, 1.0F, 0.0F, 0.0F);
        }
        if (head != 0.0F) {
            pose.bend(shape.face, head, 1.0F, 0.0F, 0.0F);
            for (int i = 3; i < shape.neck.length - 1; i++) {
                pose.bend(shape.neck[i], head * 0.35F, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    /** A rotation of {@code up} degrees about x, then {@code side} about y, as (x, y, z, w). */
    static void euler(float up, float side, float[] out) {
        double hx = Math.toRadians(up) / 2;
        double hy = Math.toRadians(side) / 2;
        float sx = (float) Math.sin(hx);
        float cx = (float) Math.cos(hx);
        float sy = (float) Math.sin(hy);
        float cy = (float) Math.cos(hy);
        // qy * qx
        out[0] = cy * sx;
        out[1] = sy * cx;
        out[2] = -sy * sx;
        out[3] = cy * cx;
    }

    static void set(DragonPose pose, int bone, float x, float y, float z, float w, float weight) {
        if (bone < 0) {
            return;
        }
        int o = bone * 4;
        float dot = pose.q[o] * x + pose.q[o + 1] * y + pose.q[o + 2] * z + pose.q[o + 3] * w;
        float s = dot < 0 ? -1.0F : 1.0F;
        float nx = pose.q[o] + (x * s - pose.q[o]) * weight;
        float ny = pose.q[o + 1] + (y * s - pose.q[o + 1]) * weight;
        float nz = pose.q[o + 2] + (z * s - pose.q[o + 2]) * weight;
        float nw = pose.q[o + 3] + (w * s - pose.q[o + 3]) * weight;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz + nw * nw);
        pose.q[o] = nx / len;
        pose.q[o + 1] = ny / len;
        pose.q[o + 2] = nz / len;
        pose.q[o + 3] = nw / len;
    }

    static void lerpT(DragonPose pose, int bone, float x, float y, float z, float weight) {
        if (bone < 0) {
            return;
        }
        pose.t[bone * 3] += (x - pose.t[bone * 3]) * weight;
        pose.t[bone * 3 + 1] += (y - pose.t[bone * 3 + 1]) * weight;
        pose.t[bone * 3 + 2] += (z - pose.t[bone * 3 + 2]) * weight;
    }

    static void lerpRest(DragonPose pose, int bone, float weight) {
        if (bone >= 0) {
            float[] r = pose.model.restT;
            lerpT(pose, bone, r[bone * 3], r[bone * 3 + 1], r[bone * 3 + 2], weight);
        }
    }

    /**
     * The take's path moved across (model units, x and z) so the transformation starts under the player and the way back ends in
     * them: the head's offset from the hover, faded out as the dragon rises (by {@link #LEAVE}) and in as it dives.
     */
    public static void shift(Shape shape, int phase, float seconds, float[] out) {
        out[0] = 0.0F;
        out[1] = 0.0F;
        out[2] = 0.0F;
        float[] from;
        float w;
        if (phase == FORMING) {
            from = shape.formStart;
            w = 1.0F - smooth(seconds / LEAVE);
        } else if (phase == UNFORMING) {
            from = shape.unformEnd;
            w = smooth((seconds - REACH_FROM) / (UNFORM_TICKS / 20.0F - REACH_FROM));
        } else {
            return;
        }
        out[0] = (shape.hoverHead[0] - from[0]) * w;
        out[2] = (shape.hoverHead[2] - from[2]) * w;
    }

    /** The dragon's size about the player's chest: growing out of it as it forms, shrinking into it as it goes back. */
    public static float grow(int phase, float seconds) {
        if (phase == FORMING) {
            return SMALLEST + (1.0F - SMALLEST) * smooth(seconds / GROW);
        }
        if (phase == UNFORMING) {
            float end = UNFORM_TICKS / 20.0F;
            return SMALLEST + (1.0F - SMALLEST) * (1.0F - smooth((seconds - (end - SHRINK)) / SHRINK));
        }
        return 1.0F;
    }

    /**
     * Model units to the player's place (blocks, from the feet; 3 x 4 row-major): the point moved by {@code shift} (model units), the
     * anchor to the head's height over the feet, turned to face {@code yaw} (Minecraft's: 0 looks to +z), pitched by {@code pitch}
     * (down positive) and banked by {@code bank} (degrees, right wing down positive), at {@link #SCALE}, then {@code grow} times its
     * size about the chest.
     */
    public static void placement(float[] anchor, float[] shift, float grow, float yaw, float pitch, float bank, float[] out) {
        double y = Math.toRadians(-yaw);
        double p = Math.toRadians(pitch);
        double b = Math.toRadians(bank);
        float cy = (float) Math.cos(y);
        float sy = (float) Math.sin(y);
        float cp = (float) Math.cos(p);
        float sp = (float) Math.sin(p);
        float cb = (float) Math.cos(b);
        float sb = (float) Math.sin(b);
        // R = Ry * Rx * Rz
        float[] ry = {cy, 0, sy, 0, 1, 0, -sy, 0, cy};
        float[] rx = {1, 0, 0, 0, cp, -sp, 0, sp, cp};
        float[] rz = {cb, -sb, 0, sb, cb, 0, 0, 0, 1};
        float[] r = mul3(mul3(ry, rx), rz);
        for (int row = 0; row < 3; row++) {
            float tx = 0.0F;
            for (int c = 0; c < 3; c++) {
                out[row * 4 + c] = r[row * 3 + c] * SCALE * grow;
                tx += r[row * 3 + c] * SCALE * (shift[c] - anchor[c]);
            }
            float centre = row == 1 ? CHEST : 0.0F;
            out[row * 4 + 3] = centre + (tx + (row == 1 ? HEAD_HEIGHT : 0.0F) - centre) * grow;
        }
    }

    static float[] mul3(float[] a, float[] b) {
        float[] o = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                o[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
            }
        }
        return o;
    }

    /** 0 to 1 eased (smoothstep), clamped. */
    public static float smooth(float x) {
        float t = Math.max(0.0F, Math.min(1.0F, x));
        return t * t * (3.0F - 2.0F * t);
    }

    /** How much a speed (blocks a tick) flies: none at a drift, all of it at creative flight's pace. */
    public static float flightTarget(double speed) {
        return smooth((float) ((speed - 0.04) / 0.36));
    }
}
