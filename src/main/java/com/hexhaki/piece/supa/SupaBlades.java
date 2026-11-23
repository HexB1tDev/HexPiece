package com.hexhaki.piece.supa;

/**
 * 3.3.5: the Supa Supa no Mi's blades, made of the mod's own katana steel (the owner, on the first flat blades: "it honestly all looks
 * really poor and poorly made"; asked, they chose "Katana steel", then said "Yes, build it" to docs/handoff/supa-arm-blades-3.3.5.jpg).
 * Every blade is a katana's steel alone (its 01_Blade part: the owner's lit, textured mesh with its temper line, Silver Crane's bright
 * silver) reshaped by {@link Shape} (client/sword/KatanaMesh.shaped: wider about its own spine, thicker, its curve eased, its edge
 * rolling in waves) and set on a limb by the numbers here; with Armament on it is coated the katanas' very dark purple exactly as they are
 * (client/sword/KatanaRenderer.steel). tools/supa_view.py draws them off-game with the same numbers. Pure.
 */
public final class SupaBlades {
    private SupaBlades() {
    }

    /**
     * How a katana's steel is reshaped (katana model px: the guard at y 0, the blade up +y, its edge to -x): every 0.5 px band along it,
     * its spine (most +x) and edge (most -x); a vertex {@code d} in from the spine, {@code u = d / (spine - edge)}:
     * {@code x' = spine * curve - d * widen - wave * sin(2 pi y / period) * u^2}, {@code y' = y * length}, {@code z' = z * thick}.
     */
    public record Shape(String id, float widen, float thick, float length, float wave, float period, float curve) {
        /** {@code (x, y)}'s new x, given its band's spine and edge. */
        public float x(float x, float y, float spine, float edge) {
            float w = Math.max(0.05F, spine - edge);
            float d = spine - x;
            float u = Math.max(0.0F, Math.min(1.0F, d / w));
            return spine * this.curve - d * this.widen - this.wave * (float) Math.sin(2.0 * Math.PI * y / this.period) * u * u;
        }
    }

    /** Daz Bones' arm blade (the owner's references: broad steel with a wavy edge out of the forearm). */
    public static final Shape ARM = new Shape("arm", 4.2F, 2.6F, 0.82F, 0.45F, 7.5F, 0.4F);
    /** A finger blade (Spar Claw): short, narrow, its curve kept (a claw). */
    public static final Shape CLAW = new Shape("claw", 1.3F, 1.8F, 0.26F, 0.0F, 1.0F, 1.0F);
    /** One blade of Spiral Hollow's drill (several stand round the forearm, twisted like a screw's threads). */
    public static final Shape DRILL = new Shape("drill", 1.3F, 2.2F, 0.5F, 0.0F, 1.0F, 0.2F);
    /** Atomic Spurt's skate blade under a foot (its curve lifts its front like a skate's). */
    public static final Shape SKATE = new Shape("skate", 2.6F, 2.2F, 0.46F, 0.0F, 1.0F, 1.0F);

    /** The arm blade's guard end on the arm (px, the right arm's frame: x out, y down the arm), its section turned round the arm. */
    public static final float[] ARM_AT = {-1.6F, 3.2F, 0.0F};
    public static final float ARM_TURN = 30.0F;

    /** The finger blades: four out of the fist (px, the right arm's frame), fanned (degrees round z), across the hand. */
    public static final float CLAW_Y = 9.4F;
    public static final float[] CLAW_X = {-2.5F, -1.5F, -0.5F, 0.5F};
    public static final float[] CLAW_FAN = {14.0F, 5.0F, -5.0F, -14.0F};

    /** The drill: so many blades round the forearm's middle (px, the right arm's frame), their roots this far out, leaning in this much
     *  toward a point past the fist, each turned along itself like a thread. */
    public static final int DRILL_BLADES = 6;
    public static final float[] DRILL_AXIS = {-1.0F, 4.6F, 0.0F};
    public static final float DRILL_OUT = 2.5F;
    public static final float DRILL_LEAN = 11.0F;
    /** Each blade turned this far along itself: 90 lays its flat along the cone's surface, the rest its thread's slant. */
    public static final float DRILL_TWIST = 115.0F;
    /** How fast the drill turns (degrees a tick). */
    public static final float DRILL_SPIN = 36.0F;

    /** Spar Break: so many arm blades standing out round the body's middle (px, the body's frame), turning this fast (degrees a tick). */
    public static final int SPIN_BLADES = 8;
    public static final float SPIN_Y = 7.0F;
    public static final float SPIN_OUT = 3.0F;
    public static final float SPIN_SPEED = 50.0F;

    /** The skate blade under a foot (px, a leg's frame: the sole at y 12): its heel end, running forward (-z) with its edge down. */
    public static final float[] SKATE_AT = {0.0F, 12.4F, 3.2F};

    /**
     * Spider's steel skin: every part's box (model px, the part's own frame) grown by {@link #SKIN} px, the steel's texture on its
     * faces, lit and shining as the blades (client/sword/KatanaRenderer.steelBox). Head, body, arms (wide: 4 across), legs.
     */
    public static final float SKIN = 0.45F;
    public static final float[][] PARTS = {
        {-4, -8, -4, 4, 0, 4}, {-4, 0, -2, 4, 12, 2}, {-3, -2, -2, 1, 10, 2}, {-1, -2, -2, 3, 10, 2}, {-2, 0, -2, 2, 12, 2}, {-2, 0, -2, 2, 12, 2},
    };
    /** The slim (Alex) arms: 3 across. */
    public static final float[][] SLIM_ARMS = {{-2, -2, -2, 1, 10, 2}, {-1, -2, -2, 2, 10, 2}};
}
