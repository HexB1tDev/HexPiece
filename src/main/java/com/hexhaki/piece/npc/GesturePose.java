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
                // the right hand flat over the eyes, the head turning slowly out to one side and across to the other
                arm(p, RIGHT_ARM, -140.0F, -70.0F, -10.0F);
                arm(p, LEFT_ARM, 4.0F, 0.0F, -10.0F);
                float scan = (float) Math.sin((t - IN) * 0.075);
                p[HEAD][YAW] = 34.0F * scan;
                p[HEAD][PITCH] = -6.0F;
                p[BODY][YAW] = -6.0F * scan;
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

    static float smooth(float t) {
        float u = Math.max(0.0F, Math.min(1.0F, t));
        return u * u * (3.0F - 2.0F * u);
    }
}
