package com.hexhaki.piece.npc;

/**
 * 2.3.3: how a fighter's body shows a {@link Gesture} (pure, previewed by tools/preview/GestureDump.java): for a moment of it, every
 * part's pose in the layout the NPC renderer takes from its moves ({@code [part][x, y, z, pitch, yaw, roll, bend, weight]}; parts body,
 * head, right arm, left arm, right leg, left leg; degrees, the body's x y z in blocks, + up). The shapes are HexGhoul's EtoPose's, in
 * degrees on the vanilla model's own axes (an arm's pitch - raises it forward, a right arm's roll + swings it out, a left arm's - ; a
 * head's pitch + looks down, its yaw + turns it to its right). An NPC's arms do not bend: each is drawn straight, so a hand that must
 * reach the brow or the chin is laid there by the whole arm. Every gesture eases in from the fighter's own pose over its first
 * {@link #IN} ticks and back out over its last {@link #OUT} (the parts' weights); a leg it leaves alone keeps walking as the game moves it.
 */
public final class GesturePose {
    public static final int PARTS = 8;
    public static final int WEIGHT = 7;
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int RIGHT_ARM = 2;
    static final int LEFT_ARM = 3;
    static final int RIGHT_LEG = 4;
    static final int LEFT_LEG = 5;
    static final int X = 0;
    static final int Y = 1;
    static final int PITCH = 3;
    static final int YAW = 4;
    static final int ROLL = 5;
    static final float IN = 7.0F;
    static final float OUT = 9.0F;

    private GesturePose() {
    }

    /** The pose {@code age} ticks into {@code gesture} (all weights 0 for NONE and once it is over). */
    public static float[][] pose(Gesture gesture, float age) {
        float[][] p = new float[PARTS][WEIGHT + 1];
        if (gesture == null || gesture == Gesture.NONE || age < 0.0F || age > gesture.ticks) {
            return p;
        }
        float t = age;
        boolean legs = false;
        switch (gesture) {
            case LOOKOUT -> {
                // the right hand flat over the eyes, the head turning slowly out to one side and across to the other. 2.4.3: the hand held
                // in front of the right brow and turned with the head, the body taking most of the turn (the lint in tools/gesture_preview.py
                // found the hand sunk 3 px into the face: a straight arm cannot reach across to the middle of the brow, and the head
                // turned into it)
                float scan = (float) Math.sin((t - IN) * 0.075);
                arm(p, RIGHT_ARM, -140.0F, -30.0F + 11.0F * scan, -20.0F);
                arm(p, LEFT_ARM, 4.0F, 0.0F, -10.0F);
                p[HEAD][YAW] = 16.0F * scan;
                p[HEAD][PITCH] = -6.0F;
                p[BODY][YAW] = -20.0F * scan;
            }
            case CROSS, TAP -> {
                folded(p);
                float glance = (float) Math.max(0.0, Math.sin(t * 0.07 - 1.2));
                p[HEAD][YAW] = gesture == Gesture.CROSS ? 24.0F - 26.0F * glance : 0.0F;
                p[HEAD][PITCH] = gesture == Gesture.CROSS ? -4.0F : 6.0F;
                p[HEAD][ROLL] = gesture == Gesture.TAP ? -7.0F : 0.0F;
                p[BODY][ROLL] = 2.5F;
                legs = true;
                p[RIGHT_LEG][ROLL] = 3.5F;
                p[LEFT_LEG][ROLL] = 1.0F;
                if (gesture == Gesture.TAP) {
                    float tap = (float) Math.abs(Math.sin(t * Math.PI / 7.0));
                    p[RIGHT_LEG][PITCH] = -12.0F * tap;
                }
            }
            case STRETCH -> {
                float reach = (float) Math.sin(Math.min(1.0, t / 26.0) * Math.PI * 0.5);
                float back = t > Gesture.STRETCH.ticks - 18 ? smooth((t - (Gesture.STRETCH.ticks - 18)) / 18.0F) : 0.0F;
                reach *= 1.0F - back * 0.6F;
                arm(p, RIGHT_ARM, -168.0F * reach, 0.0F, 13.0F * reach);
                arm(p, LEFT_ARM, -168.0F * reach, 0.0F, -13.0F * reach);
                p[HEAD][PITCH] = -22.0F * reach;
                p[BODY][Y] = 0.05F * reach;
                p[BODY][ROLL] = 3.0F * (float) Math.sin(t * 0.18) * reach;
            }
            case YAWN -> {
                float yawn = (float) Math.sin(Math.min(1.0, t / 26.0) * Math.PI);
                arm(p, RIGHT_ARM, -118.0F, -30.0F, 2.0F);
                float up = t > 22.0F ? smooth((t - 22.0F) / 10.0F) : 0.0F;
                arm(p, LEFT_ARM, -150.0F * up, 0.0F, -14.0F * up);
                p[HEAD][PITCH] = -20.0F * yawn;
                p[BODY][Y] = 0.025F * yawn;
            }
            case CRACK -> {
                // both fists brought together in front of the chest, pushed out until the knuckles go, the neck rolled after
                float rise = smooth(Math.min(1.0F, t / 12.0F));
                float push = t < 16.0F ? 0.0F : t < 20.0F ? smooth((t - 16.0F) / 4.0F) : t < 30.0F ? 1.0F : 1.0F - smooth((t - 30.0F) / 8.0F);
                arm(p, RIGHT_ARM, (-62.0F - 18.0F * push) * rise, -26.0F * rise, -22.0F * rise);
                arm(p, LEFT_ARM, (-62.0F - 18.0F * push) * rise, 26.0F * rise, 22.0F * rise);
                float neck = t < 34.0F ? 0.0F : (float) Math.sin(Math.min(1.0, (t - 34.0F) / 16.0) * Math.PI);
                p[HEAD][ROLL] = 16.0F * neck;
                p[HEAD][PITCH] = 6.0F * rise - 4.0F * neck;
            }
            case SHRUG -> {
                float up = (float) Math.sin(Math.min(1.0, t / Gesture.SHRUG.ticks) * Math.PI);
                arm(p, RIGHT_ARM, -24.0F * up, 22.0F * up, 30.0F * up);
                arm(p, LEFT_ARM, -24.0F * up, -22.0F * up, -30.0F * up);
                p[BODY][Y] = 0.04F * up;
                p[HEAD][ROLL] = 17.0F * up;
                p[HEAD][PITCH] = -4.0F * up;
            }
            case CHIN -> {
                arm(p, RIGHT_ARM, -112.0F, -36.0F, 6.0F);
                arm(p, LEFT_ARM, -46.0F, 40.0F, -3.0F);
                p[HEAD][ROLL] = 7.0F;
                p[HEAD][PITCH] = 6.0F + 3.0F * (float) Math.sin(t * 0.06);
                p[HEAD][YAW] = -8.0F;
            }
            case LAUGH -> {
                float shake = (float) Math.sin(t * 2.2);
                float hard = (float) Math.sin(Math.min(1.0, t / Gesture.LAUGH.ticks) * Math.PI);
                p[HEAD][PITCH] = (-26.0F + 3.0F * shake) * hard;
                arm(p, RIGHT_ARM, (-43.0F + 3.5F * shake) * hard, -26.0F * hard, -4.0F * hard);
                arm(p, LEFT_ARM, (-43.0F + 3.5F * shake) * hard, 26.0F * hard, 4.0F * hard);
                p[BODY][Y] = -0.012F * shake * hard;
                p[BODY][PITCH] = -4.0F * hard;
            }
            case SALUTE -> {
                // snapped up to the right brow in four ticks, held, brought down
                float up = t < 4.0F ? smooth(t / 4.0F) : t < 38.0F ? 1.0F : 1.0F - smooth((t - 38.0F) / 8.0F);
                arm(p, RIGHT_ARM, -136.0F * up, -42.0F * up, -18.0F * up);
                arm(p, LEFT_ARM, 0.0F, 0.0F, -3.0F);
                p[HEAD][PITCH] = -2.0F;
                legs = true;
            }
            case EASE -> {
                // feet apart, hands behind the back, a slow sway of the weight from foot to foot
                float sway = (float) Math.sin(t * 0.045);
                arm(p, RIGHT_ARM, 26.0F, 0.0F, -11.0F);
                arm(p, LEFT_ARM, 26.0F, 0.0F, 11.0F);
                legs = true;
                p[RIGHT_LEG][ROLL] = 6.0F;
                p[LEFT_LEG][ROLL] = -6.0F;
                p[BODY][ROLL] = 1.5F * sway;
                p[HEAD][PITCH] = -3.0F;
                p[HEAD][YAW] = 10.0F * sway;
            }
            case BRUSH -> {
                // 2.4.3, a captain's: the back of the right hand brought across to the left of the coat's front (a straight arm can reach
                // no higher without going through the chin), three strokes of the dust off it, each swept out and down, a last flick out
                // to the side, the arm dropped and the chin up after it; the left shoulder held back to be brushed, the left arm easy
                float up = smooth(t / 10.0F);
                float flick = smooth((t - 34.0F) / 5.0F);
                float down = smooth((t - 40.0F) / 9.0F);
                float stroke = t < 10.0F || t > 34.0F ? 0.0F : ((t - 10.0F) % 8.0F) / 8.0F;
                // each stroke: a quick sweep out and down over its first half, the hand brought back over its second
                float brush = stroke < 0.45F ? smooth(stroke / 0.45F) : 1.0F - smooth((stroke - 0.45F) / 0.55F);
                float across = up * (1.0F - flick);
                float pitch = (-84.0F + 16.0F * brush) * across - 50.0F * flick * (1.0F - down);
                float yaw = (-58.0F + 28.0F * brush) * across + 16.0F * flick * (1.0F - down);
                float roll = -4.0F * across + 22.0F * flick * (1.0F - down) + 4.0F * down;
                arm(p, RIGHT_ARM, pitch, yaw, roll);
                arm(p, LEFT_ARM, 10.0F * across + 6.0F, 0.0F, -9.0F);
                float look = up * (1.0F - flick);
                float proud = smooth((t - 36.0F) / 8.0F);
                p[HEAD][YAW] = -20.0F * look + 8.0F * proud * (float) Math.sin((t - 40.0F) * 0.12);
                p[HEAD][PITCH] = 21.0F * look - 11.0F * proud;
                p[HEAD][ROLL] = -5.0F * look;
                p[BODY][YAW] = 7.0F * look;
                p[BODY][PITCH] = 3.0F * proud;
            }
            case SWAGGER -> {
                // 2.4.3, a captain's: a roll of the shoulders, then chest out and chin up, arms held out from the sides and back, feet
                // planted wide, rocking on its heels; a slow look over the crew from one side to the other and back, a nod at each end
                float set = smooth(t / 14.0F);
                float roll = t < 16.0F ? (float) Math.sin(t * Math.PI / 8.0) * (1.0F - t / 16.0F) : 0.0F;
                float scan = (float) Math.sin(Math.max(0.0F, t - 14.0F) / (Gesture.SWAGGER.ticks - 24.0F) * Math.PI * 2.0);
                float ends = (float) Math.pow(Math.abs(scan), 6.0);
                arm(p, RIGHT_ARM, 14.0F * set, 0.0F, 19.0F * set + 6.0F * roll);
                arm(p, LEFT_ARM, 14.0F * set, 0.0F, -19.0F * set + 6.0F * roll);
                legs = true;
                p[RIGHT_LEG][ROLL] = 7.0F * set;
                p[LEFT_LEG][ROLL] = -7.0F * set;
                p[BODY][PITCH] = (4.0F + 1.5F * (float) Math.sin(t * 0.14)) * set; // rocked back on the heels and forward, slowly
                p[BODY][Y] = 0.02F * set;
                p[BODY][ROLL] = 3.0F * roll;
                p[BODY][YAW] = -8.0F * scan * set;
                p[HEAD][YAW] = 34.0F * scan * set;
                p[HEAD][PITCH] = -11.0F * set + 7.0F * ends;
            }
            case ORDER -> {
                // 2.4.3, an officer's: the right arm thrown out to point ahead and to the right, a jab, swept across to point ahead and to
                // the left, a jab, dropped; a firm nod; the left hand behind the back the whole time, the feet set
                float out = smooth(t / 5.0F) * (1.0F - smooth((t - 44.0F) / 8.0F));
                float sweep = smooth((t - 22.0F) / 8.0F);
                float jab = (float) (Math.max(0.0, Math.sin((t - 7.0F) * Math.PI / 4.0)) * (t > 7.0F && t < 11.0F ? 1.0 : 0.0)
                    + Math.max(0.0, Math.sin((t - 33.0F) * Math.PI / 4.0)) * (t > 33.0F && t < 37.0F ? 1.0 : 0.0));
                float way = 24.0F - 44.0F * sweep;
                arm(p, RIGHT_ARM, (-90.0F - 6.0F * jab) * out, way * out, 2.0F * out);
                arm(p, LEFT_ARM, 26.0F, 0.0F, 11.0F);
                legs = true;
                p[RIGHT_LEG][ROLL] = 4.0F;
                p[LEFT_LEG][ROLL] = -4.0F;
                float nod = t > 52.0F && t < 62.0F ? (float) Math.sin((t - 52.0F) * Math.PI / 10.0) : 0.0F;
                p[HEAD][YAW] = 0.75F * way * out;
                p[HEAD][PITCH] = -3.0F + 5.0F * jab + 13.0F * nod;
                p[BODY][YAW] = -0.25F * way * out;
            }
            case FIDGET -> {
                // 2.4.3, a recruit's: the weight from foot to foot (the free foot's heel up), hands wrung together in front of the belly,
                // quick glances over one shoulder and the other, a little hunched
                float shift = (float) Math.sin(t * 0.21);
                float wring = (float) Math.sin(t * 0.9);
                arm(p, RIGHT_ARM, -42.0F + 3.0F * wring, -21.0F + 4.0F * wring, -12.0F);
                arm(p, LEFT_ARM, -42.0F - 3.0F * wring, 21.0F + 4.0F * wring, 12.0F);
                legs = true;
                p[RIGHT_LEG][PITCH] = -7.0F * Math.max(0.0F, shift);
                p[LEFT_LEG][PITCH] = -7.0F * Math.max(0.0F, -shift);
                p[RIGHT_LEG][ROLL] = 1.5F;
                p[LEFT_LEG][ROLL] = -1.5F;
                p[BODY][ROLL] = 2.5F * shift;
                p[BODY][X] = 0.03F * shift;
                p[BODY][PITCH] = -3.0F;
                p[HEAD][YAW] = keyed(t, new float[] {0, 9, 17, 23, 38, 44, 54, 60}, new float[] {0, -30, -30, 12, 12, 32, 32, -6}, 5.0F);
                p[HEAD][PITCH] = 9.0F - 4.0F * Math.abs(p[HEAD][YAW]) / 32.0F;
                p[HEAD][ROLL] = -0.12F * p[HEAD][YAW];
            }
            case SCUFF -> {
                // 2.4.3, a recruit's: head hung, the weight on the left foot, the right toe scuffed at the deck three times; something
                // kicked away; the head up a little and a sigh, the shoulders dropping
                float scuffing = t > 8.0F && t < 56.0F ? (float) Math.sin((t - 8.0F) * Math.PI * 2.0 / 16.0) : 0.0F;
                float kick = t > 58.0F && t < 66.0F ? (float) Math.sin((t - 58.0F) * Math.PI / 8.0) : 0.0F;
                float sigh = t > 64.0F ? (float) Math.sin(Math.min(1.0F, (t - 64.0F) / 14.0F) * Math.PI) : 0.0F;
                float lean = smooth(t / 8.0F);
                float swing = (float) Math.sin(t * 0.11);
                arm(p, RIGHT_ARM, 5.0F + 4.0F * swing - 10.0F * kick, 0.0F, 4.0F);
                arm(p, LEFT_ARM, 5.0F - 4.0F * swing + 8.0F * kick, 0.0F, -4.0F);
                legs = true;
                p[RIGHT_LEG][PITCH] = -12.0F * scuffing - 30.0F * kick;
                p[RIGHT_LEG][ROLL] = -3.0F * Math.abs(scuffing) + 2.0F;
                p[LEFT_LEG][ROLL] = -1.0F;
                p[BODY][ROLL] = 2.0F * lean;
                p[BODY][X] = -0.025F * lean;
                p[BODY][Y] = -0.015F * sigh;
                p[HEAD][PITCH] = 21.0F - 10.0F * smooth((t - 60.0F) / 6.0F) + 5.0F * sigh;
                p[HEAD][YAW] = -9.0F + 6.0F * kick;
                p[HEAD][ROLL] = 5.0F;
            }
            default -> {
                return p;
            }
        }
        float w = envelope(gesture, t);
        p[BODY][WEIGHT] = w;
        p[HEAD][WEIGHT] = w;
        p[RIGHT_ARM][WEIGHT] = w;
        p[LEFT_ARM][WEIGHT] = w;
        if (legs) {
            p[RIGHT_LEG][WEIGHT] = w;
            p[LEFT_LEG][WEIGHT] = w;
        }
        return p;
    }

    /** In over {@link #IN} ticks, out over the last {@link #OUT}. */
    public static float envelope(Gesture gesture, float t) {
        float in = smooth(Math.min(1.0F, t / IN));
        float out = smooth(Math.min(1.0F, Math.max(0.0F, (gesture.ticks - t) / OUT)));
        return Math.min(in, out);
    }

    static void arm(float[][] p, int part, float pitch, float yaw, float roll) {
        p[part][PITCH] = pitch;
        p[part][YAW] = yaw;
        p[part][ROLL] = roll;
    }

    /** HexGhoul's folded arms: both forearms across the chest. */
    static void folded(float[][] p) {
        arm(p, RIGHT_ARM, -60.0F, 17.0F, -49.0F);
        arm(p, LEFT_ARM, -60.0F, -17.0F, 49.0F);
    }

    /**
     * 2.4.3: a value held at each of {@code values} from its tick in {@code times} (in order) and eased to the next over {@code ease}
     * ticks before that one's tick: quick turns between still holds.
     */
    static float keyed(float t, float[] times, float[] values, float ease) {
        float v = values[0];
        for (int i = 1; i < times.length; i++) {
            float from = times[i] - ease;
            if (t <= from) {
                break;
            }
            v = values[i - 1] + (values[i] - values[i - 1]) * smooth((t - from) / ease);
        }
        return v;
    }

    static float smooth(float t) {
        float u = Math.max(0.0F, Math.min(1.0F, t));
        return u * u * (3.0F - 2.0F * u);
    }
}
