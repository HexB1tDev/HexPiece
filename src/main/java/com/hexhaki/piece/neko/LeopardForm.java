package com.hexhaki.piece.neko;

import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;

/**
 * 3.7.9 (the owner: "New Fruit: Legendary Leopard Fruit, Name: Neko Neko No Mi, Type: Zoan, Model in the Zip, Size: 5 blocks tall. Animate
 * it properly, realistic fluid animations, the mane and body hair IS black flames, the black ribbon around the leopard is black flames as
 * well, We need Idle, running, jumping, transforming and de-transforming animations, facial animations too (simple)"): the owner's Lucci
 * leopard (assets/hexhaki/neko/lucci.sry, tools/leopard_bake.py) posed and coloured. Pure: the client, the previews and the tests share it.
 * <p>
 * The model's space: +Y up, the face toward -Z, the left arm toward -X, units the file's hundredths (about 2.5 tall); {@link #SCALE} makes
 * it 5 blocks. Its takes are the game's own (25 a second): idle_a, run, jump, jump_lp, jump_end and the rest (combo, skill, dodge...).
 * Its faces (face_normal, face_attack, face_damage, face_sp01) and hands (open, close, shigan, shugan, each side) are separate meshes
 * shown one at a time ({@link #shown}: the simple facial animation). Its mane and body hair (kind 2) and ribbon (kind 3) are black
 * flames: their geometry licks and flows like fire ({@link #flame}) and burns black, a charcoal heat running through it
 * ({@link #flameColour}), drawn alpha-blended after the particles.
 */
public final class LeopardForm {
    private LeopardForm() {
    }

    public static final int HUMAN = 0;
    public static final int FORMING = 1;
    public static final int LEOPARD = 2;
    public static final int UNFORMING = 3;

    /**
     * The change. The owner: "the transformation should be a ball of burning flames with evil red eyes within": black flames gather round
     * the slouching human into a ball (BALL_IN ticks: they fly in, nothing grows), it burns with two evil red eyes inside it, and at the
     * roar (ROAR) it bursts and the leopard stands there; going back, the flames gather round the leopard into a bigger ball (BALL_IN),
     * it burns with the eyes in it, then burns away from BURN_AWAY to UNFORM_TICKS, the human standing where it was (HUMAN_BACK). Never
     * scaled (the owner's rule): the ball is fire coming and going, the bodies are there or not.
     */
    public static final int FORM_TICKS = 34;
    public static final float BALL_IN = 6.0F;
    public static final int ROAR = 24;
    public static final int UNFORM_TICKS = 24;
    public static final float BURN_AWAY = 12.0F;
    public static final float HUMAN_BACK = 15.0F;
    /** The balls: the human's (blocks across its middle, its middle over the feet), the leopard's. */
    public static final float BALL_SMALL = 1.55F;
    public static final float BALL_SMALL_Y = 1.0F;
    public static final float BALL_BIG = 2.6F;
    public static final float BALL_BIG_Y = 2.3F;

    /** Whether the leopard is there (1) or not (0): out of the ball's burst, gone into the ball going back. */
    public static float revealed(int phase, float ticks) {
        if (phase == LEOPARD) {
            return 1.0F;
        }
        if (phase == FORMING) {
            return ticks >= ROAR ? 1.0F : 0.0F;
        }
        if (phase == UNFORMING) {
            return ticks < BALL_IN ? 1.0F : 0.0F;
        }
        return 0.0F;
    }

    /** Whether the human is hidden (in the ball, or the leopard out), {@code ticks} into the phase. */
    public static boolean humanHidden(int phase, float ticks) {
        return phase == LEOPARD || phase == FORMING && ticks >= BALL_IN || phase == UNFORMING && ticks < HUMAN_BACK;
    }

    /** How far the ball's flames have gathered (0 to 1) and how much of it still burns (1 to 0), {@code ticks} into the phase; out[2] 1 big. */
    public static void ball(int phase, float ticks, float[] out) {
        out[0] = 0.0F;
        out[1] = 0.0F;
        out[2] = 0.0F;
        if (phase == FORMING && ticks < ROAR + 2) {
            out[0] = smooth(ticks / BALL_IN);
            out[1] = ticks < ROAR ? 1.0F : 1.0F - (ticks - ROAR) / 2.0F;
        } else if (phase == UNFORMING) {
            out[0] = smooth(ticks / BALL_IN);
            out[1] = 1.0F - smooth((ticks - BURN_AWAY) / (UNFORM_TICKS - BURN_AWAY));
            out[2] = 1.0F;
        }
    }

    /** The model's units to blocks: its standing idle 5 blocks tall (the owner: "Size: 5 blocks tall"). */
    public static final float SCALE = 2.08F;

    /** Part kinds (tools/leopard_bake.py): the skin and clothes, the black flames of the mane and body hair, the ribbon's. */
    public static final int BODY = 0;
    public static final int HAIR = 2;
    public static final int RIBBON = 3;

    /** The faces (Motion.face) and hand poses (Motion.hands). */
    public static final int FACE_NORMAL = 0;
    public static final int FACE_ATTACK = 1;
    public static final int FACE_DAMAGE = 2;
    public static final int FACE_ROAR = 3;
    public static final int HAND_OPEN = 0;
    public static final int HAND_CLOSE = 1;
    public static final int HAND_SHIGAN = 2;
    public static final int HAND_SHUGAN = 3;
    static final String[] FACES = {"face_normal", "face_attack", "face_damage", "face_sp01"};
    static final String[] HANDS = {"open", "close", "shigan", "shugan"};

    /** The rig's bones and parts found once. */
    public static final class Shape {
        /** Each part's kind, and for a face or a hand pose which one it is (-1: always shown). */
        public final int[] kind;
        public final int[] face;
        public final int[] hand;
        public final int[] side;
        /** Each flame vertex's reach out along its part (0 at its root, 1 at its farthest tip), its heat's own phase. */
        public final float[][] reach;
        public final int pelvis;
        public final int chest;
        public final int neck;
        public final int head;
        /** The bind pose's height (model units) and the idle's standing height. */
        public final float height;
        /**
         * The owner: "the flames of the mane, tail and ribbon make sure those are real looking burning flames": the points tongues of black
         * flame rise off (part, vertex: the far tips of the mane, the tail's tuft, the ribbon, the elbow and shoulder hair, the beard), and
         * each one's size (blocks).
         */
        public final int[] anchors;
        public final float[] anchorSize;

        public Shape(DragonModel m) {
            int np = m.parts.length;
            this.kind = new int[np];
            this.face = new int[np];
            this.hand = new int[np];
            this.side = new int[np];
            this.reach = new float[np][];
            DragonPose rest = new DragonPose(m);
            rest.solve();
            float lo = Float.MAX_VALUE;
            float hi = -Float.MAX_VALUE;
            for (int i = 0; i < np; i++) {
                DragonModel.Part part = m.parts[i];
                String name = part.name;
                this.kind[i] = part.kind;
                this.face[i] = -1;
                this.hand[i] = -1;
                this.side[i] = -1;
                for (int k = 0; k < FACES.length; k++) {
                    if (name.equals(FACES[k])) {
                        this.face[i] = k;
                    }
                }
                for (int k = 0; k < HANDS.length; k++) {
                    if (name.equals("l_hand_" + HANDS[k]) || name.equals("r_hand_" + HANDS[k])) {
                        this.hand[i] = k;
                        this.side[i] = name.startsWith("l_") ? 0 : 1;
                    }
                }
                int nv = part.vertices();
                for (int v = 0; v < nv; v++) {
                    lo = Math.min(lo, part.pos[v * 3 + 1]);
                    hi = Math.max(hi, part.pos[v * 3 + 1]);
                }
                if (part.kind != HAIR && part.kind != RIBBON || nv == 0) {
                    continue;
                }
                // the root: the pivot of the bone the part leans on most; the reach of each vertex from it, the farthest 1
                float[] use = new float[m.bones()];
                for (int v = 0; v < nv; v++) {
                    for (int k = 0; k < 4; k++) {
                        use[part.bone[v * 4 + k] & 0xFF] += part.weight[v * 4 + k];
                    }
                }
                int root = 0;
                for (int b = 1; b < use.length; b++) {
                    if (use[b] > use[root]) {
                        root = b;
                    }
                }
                float px = rest.global[root * 12 + 3];
                float py = rest.global[root * 12 + 7];
                float pz = rest.global[root * 12 + 11];
                float[] r = new float[nv];
                float most = 1.0E-6F;
                for (int v = 0; v < nv; v++) {
                    float dx = part.pos[v * 3] - px;
                    float dy = part.pos[v * 3 + 1] - py;
                    float dz = part.pos[v * 3 + 2] - pz;
                    r[v] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    most = Math.max(most, r[v]);
                }
                for (int v = 0; v < nv; v++) {
                    r[v] /= most;
                }
                this.reach[i] = r;
            }
            this.height = hi - lo;
            // the tongues' points: each flame part's far tips, a few evenly among them
            String[] names = {"hair", "tail_hair", "hagoromo", "elbow_hair", "hair_sp", "beard"};
            int[] quota = {6, 3, 4, 2, 2, 1};
            float[] sizes = {0.42F, 0.38F, 0.34F, 0.26F, 0.26F, 0.22F};
            java.util.List<int[]> chosen = new java.util.ArrayList<>();
            java.util.List<Float> size = new java.util.ArrayList<>();
            for (int g = 0; g < names.length; g++) {
                for (int i = 0; i < np; i++) {
                    if (!m.parts[i].name.equals(names[g]) || this.reach[i] == null) {
                        continue;
                    }
                    java.util.List<Integer> tips = new java.util.ArrayList<>();
                    for (int v = 0; v < this.reach[i].length; v++) {
                        if (this.reach[i][v] > 0.8F) {
                            tips.add(v);
                        }
                    }
                    int keep = Math.min(quota[g], tips.size());
                    for (int k = 0; k < keep; k++) {
                        chosen.add(new int[] {i, tips.get((int) ((long) (2 * k + 1) * tips.size() / (2L * keep)))});
                        size.add(sizes[g]);
                    }
                }
            }
            this.anchors = new int[chosen.size() * 2];
            this.anchorSize = new float[chosen.size()];
            for (int k = 0; k < chosen.size(); k++) {
                this.anchors[k * 2] = chosen.get(k)[0];
                this.anchors[k * 2 + 1] = chosen.get(k)[1];
                this.anchorSize[k] = size.get(k);
            }
            this.pelvis = m.bone("Body_Pelvis");
            this.chest = m.bone("Body_Chest");
            this.neck = m.bone("Head_Neck");
            this.head = m.bone("Head_Face");
        }
    }

    /** How it moves this frame (the client works it out from the player; the previews set it). */
    public static final class Motion {
        /** 0 standing to 1 running; the run's phase (0 to 1 a cycle: a step of each foot). */
        public float speed;
        public float stride;
        /** 0 to 1 off the ground; how long since it left the ground (ticks: the take-off), since it touched down (the landing). */
        public float air;
        public float up = 100.0F;
        public float down = 100.0F;
        /** The face shown (FACE_*) and each hand's pose (HAND_*). */
        public int face = FACE_NORMAL;
        public final int[] hands = {HAND_OPEN, HAND_OPEN};
        /** 3.7.9, the change: 0 to 1 how far it has its own form (unused by the pose yet: the change burns, never scales). */
        public float form = 1.0F;
        /** A blow of Six Styles under way (1 to 5, 0 none) and how far into it (ticks). */
        public int blow;
        public float blowAge;
        /** The roar's face and pose (0 to 1). */
        public float roar;
    }

    /**
     * Six Styles as the leopard: each blow one of the model's own takes (combo_a, combo_b, combo_c, combo_a again for the double punch,
     * skill_b's two-handed thrust for Rokuogan), from where in it the strike lands ({@link #BLOW_AT}: the take's second at the server's
     * blow), played over {@link #BLOW_TICKS}; eased in and out over the idle and run.
     */
    static final String[] BLOW_TAKES = {"combo_a", "combo_b", "combo_c", "combo_a", "skill_b"};
    static final float[] BLOW_FROM = {0.05F, 0.05F, 0.05F, 0.25F, 0.1F};
    public static final float[] BLOW_TICKS = {10.0F, 10.0F, 10.0F, 10.0F, 18.0F};

    /** Whether part {@code index} is drawn: one face and one hand pose each side at a time. */
    public static boolean shown(Shape sh, int index, Motion mo) {
        if (sh.face[index] >= 0) {
            return sh.face[index] == mo.face;
        }
        if (sh.hand[index] >= 0) {
            return sh.hand[index] == mo.hands[sh.side[index]];
        }
        return true;
    }

    private static final float[] PIN = new float[4];

    /** The take-off, the landing (ticks). */
    public static final float TAKE_OFF = 8.0F;
    public static final float LANDING = 12.0F;

    /**
     * The leopard posed: the game's own idle breathing under everything, its run over it by the speed (its phase from the ground covered:
     * the feet never slide), off the ground the jump's take-off, then its hang in the air, and the landing as it touches down.
     */
    public static void pose(DragonModel m, Shape sh, DragonPose pose, Motion mo, float seconds) {
        pose.rest();
        pose.clip(m.clips.get("idle_a"), seconds, true, 1.0F);
        DragonModel.Clip run = m.clips.get("run");
        if (run != null && mo.speed > 0.0F) {
            pose.clip(run, mo.stride * run.seconds(), true, clamp(mo.speed) * (1.0F - clamp(mo.air)));
        }
        float air = clamp(mo.air);
        if (air > 0.0F) {
            DragonModel.Clip jump = m.clips.get("jump");
            DragonModel.Clip hang = m.clips.get("jump_lp");
            if (mo.up < TAKE_OFF && jump != null) {
                pose.clip(jump, mo.up / 20.0F + jump.seconds() * 0.35F, false, air);
            } else {
                pose.clip(hang, seconds, true, air);
            }
        }
        if (mo.blow > 0 && mo.blow <= BLOW_TAKES.length) {
            DragonModel.Clip take = m.clips.get(BLOW_TAKES[mo.blow - 1]);
            float length = BLOW_TICKS[mo.blow - 1];
            float k = Math.min(smooth(mo.blowAge / 2.0F), 1.0F - smooth((mo.blowAge - length + 3.0F) / 3.0F));
            if (take != null && k > 0.0F) {
                // the take's own travel along the ground taken out (the player moves the leopard; the take only its body)
                int[] pinned = {0, sh.pelvis};
                for (int b = 0; b < 2; b++) {
                    int bone = pinned[b];
                    if (bone >= 0) {
                        PIN[b * 2] = pose.t[bone * 3];
                        PIN[b * 2 + 1] = pose.t[bone * 3 + 2];
                    }
                }
                float span = take.seconds() * 0.75F;
                pose.clip(take, BLOW_FROM[mo.blow - 1] * take.seconds() + span * Math.min(1.0F, mo.blowAge / length), false, k);
                for (int b = 0; b < 2; b++) {
                    int bone = pinned[b];
                    if (bone >= 0) {
                        pose.t[bone * 3] = PIN[b * 2];
                        pose.t[bone * 3 + 2] = PIN[b * 2 + 1];
                    }
                }
            }
        }
        if (mo.down < LANDING && air < 0.5F) {
            DragonModel.Clip end = m.clips.get("jump_end");
            float k = 1.0F - smooth(mo.down / LANDING);
            pose.clip(end, mo.down / 20.0F, false, k * (1.0F - air));
        }
        pose.solve();
    }

    /**
     * The black flames alive: every vertex of the mane, body hair and ribbon licks out along its reach, the tips most, waves running out
     * to them, flickering up like the tips of flames. {@code part} is model part {@code index}; pos and nrm its skinned vertices.
     */
    public static void flame(Shape sh, int index, DragonModel.Part part, float[] pos, float[] nrm, float seconds) {
        float[] reach = sh.reach[index];
        if (reach == null) {
            return;
        }
        float amp0 = sh.kind[index] == RIBBON ? FLAME * 0.7F : FLAME;
        float t = seconds;
        float phase = index * 1.618F;
        int n = part.vertices();
        for (int v = 0; v < n; v++) {
            float r = reach[v];
            if (r < 0.08F) {
                continue;
            }
            float amp = amp0 * r * (float) Math.sqrt(r);
            float x = pos[v * 3];
            float y = pos[v * 3 + 1];
            float z = pos[v * 3 + 2];
            float run = r * 6.0F - t * 8.0F + phase;
            float w1 = (float) Math.sin(run + x * 2.1F);
            float w2 = (float) Math.sin(run * 1.7F + z * 2.7F - t * 4.9F);
            float lick = 0.5F + 0.5F * (float) Math.sin(run * 0.8F + y * 3.1F - t * 6.3F);
            float tip = Math.max(0.0F, (r - 0.55F) / 0.45F);
            float flick = (float) Math.sin(t * 15.0F + phase * 3.0F + v * 0.37F) * (float) Math.sin(t * 6.1F + v * 0.11F);
            pos[v * 3] += nrm[v * 3] * amp * 0.5F * w1 + amp * 0.3F * w2;
            pos[v * 3 + 1] += nrm[v * 3 + 1] * amp * 0.5F * w1 + amp * (0.5F * lick + 0.15F * w2) + amp * tip * tip * (0.6F * lick + 0.25F * flick);
            pos[v * 3 + 2] += nrm[v * 3 + 2] * amp * 0.5F * w1 + amp * 0.25F * w2;
        }
    }

    /** How far the flames lick (model units at the tips). */
    static final float FLAME = 0.045F;

    /** The light the skin is lit by (unit, model space: from above, in front, the left), and how dark its shade side goes. */
    static final float LX = -0.35F;
    static final float LY = 0.8F;
    static final float LZ = -0.48F;
    static final float SHADE = 0.55F;

    /** A skin vertex's tint (r, g, b times the texture; [3] 0): lit by the one light, never darker than the shade. */
    public static void tint(Shape sh, int index, DragonModel.Part part, int v, float nx, float ny, float nz, float y, float seconds, float[] out) {
        float d = nx * LX + ny * LY + nz * LZ;
        float l = SHADE + (1.0F - SHADE) * Math.max(0.0F, d);
        out[0] = l;
        out[1] = l;
        out[2] = l;
        out[3] = 0.0F;
    }

    /** The black flames' palette (the owner: "black flames not purple"): black, the charcoal of its flowing heat, its hottest lick. */
    static final int BLACK = 0x030303;
    static final int EMBER = 0x161616;
    static final int VIOLET = 0x101010;
    static final int HOT = 0x343434;

    /**
     * A black flame's colour at a vertex (ARGB): black, with charcoal heat flowing out along it in bands from its root to its
     * tips (as the phoenix's skin flows: PhoenixForm.heat), the tips burning hottest and thinning out; alpha-blended.
     */
    public static int flameColour(Shape sh, int index, int v, float x, float z, float seconds) {
        float[] reach = sh.reach[index];
        float r = reach == null ? 0.5F : reach[v];
        float phase = index * 1.618F;
        float band = (float) Math.sin(r * 9.0F - seconds * 6.5F + phase + 0.9F * (float) Math.sin(x * 3.0F + z * 2.0F + seconds * 1.3F));
        float b = Math.max(0.0F, band);
        b = b * b;
        float heat = Math.min(1.0F, b * (0.25F + 0.75F * r));
        float violet = 0.5F + 0.5F * (float) Math.sin(seconds * 1.7F + phase + x * 4.0F);
        int warm = mix(EMBER, VIOLET, violet);
        int rgb = heat < 0.5F ? mix(BLACK, warm, heat * 2.0F) : mix(warm, HOT, (heat - 0.5F) * 1.2F);
        float alpha = 0.97F - 0.35F * Math.max(0.0F, r - 0.7F) / 0.3F;
        return (int) (Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | rgb;
    }

    static int mix(int a, int b, float t) {
        float u = Math.max(0.0F, Math.min(1.0F, t));
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * u);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * u);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * u);
        return r << 16 | g << 8 | bl;
    }

    static float clamp(float t) {
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    static float smooth(float t) {
        float u = clamp(t);
        return u * u * (3.0F - 2.0F * u);
    }

    /**
     * The previews' states (tools/preview/LeopardPreview): "idle", "run" (full speed), "walk" (half), "jump" (the take-off at
     * {@code seconds}, then the hang), "land" (the touchdown {@code seconds} after it), each with ",face=N" and ",hands=N" after it if
     * asked; {@code mo} filled with what was used.
     */
    public static void preview(DragonModel m, Shape sh, DragonPose pose, String state, float seconds, Motion mo) {
        String[] bits = state.split(",");
        for (String bit : bits) {
            if (bit.startsWith("face=")) {
                mo.face = Integer.parseInt(bit.substring(5));
            } else if (bit.startsWith("hands=")) {
                mo.hands[0] = Integer.parseInt(bit.substring(6));
                mo.hands[1] = mo.hands[0];
            }
        }
        DragonModel.Clip run = m.clips.get("run");
        float cycle = run == null ? 1.0F : run.seconds();
        switch (bits[0]) {
            case "run" -> {
                mo.speed = 1.0F;
                mo.stride = seconds / cycle % 1.0F;
            }
            case "walk" -> {
                mo.speed = 0.5F;
                mo.stride = seconds / (cycle * 1.6F) % 1.0F;
            }
            case "jump" -> {
                mo.air = 1.0F;
                mo.up = seconds * 20.0F;
            }
            case "land" -> {
                mo.air = 0.0F;
                mo.down = seconds * 20.0F;
            }
            case "blow1", "blow2", "blow3", "blow4", "blow5" -> {
                mo.blow = bits[0].charAt(4) - '0';
                mo.blowAge = seconds * 20.0F;
                mo.face = FACE_ATTACK;
                mo.hands[0] = HAND_CLOSE;
                mo.hands[1] = HAND_CLOSE;
            }
            default -> {
            }
        }
        pose(m, sh, pose, mo, seconds);
    }
}
