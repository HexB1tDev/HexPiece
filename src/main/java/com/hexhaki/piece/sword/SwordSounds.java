package com.hexhaki.piece.sword;

/**
 * 2.2.9: when the Swordsman's sounds play (pure). The owner: "grab sounds from the web for the swords unsheath, swipe, etc, all the
 * sounds must come from the web (be free for use)": every clip is from a CC0 pack on OpenGameArt.org (tools/sword_sounds.py,
 * {@code assets/hexhaki/sounds/piece/sword/SOURCES.txt}); the sound events are {@code hexhaki:piece.sword.*} ({@code PieceSounds}).
 *
 * <p>Each animation has its beats, timed to what the blade does in it (BladePaths' noto beats, Yoru's hand-off on the back): the thumb
 * breaking the saya's seal, the blade sliding out, ringing free as it clears the mouth; the chiburi flick, the slide home, the guard
 * clicking against the koiguchi; Yoru's slow, deep pull out of its ropes and the great sweep over the shoulder. The cuts swing on their
 * own windows (the light and the heavy swish, a ring of steel on the heavy ones), hits clash, the Haki slash rushes the air.
 */
public final class SwordSounds {
    public static final String DRAW = "piece.sword.draw";
    public static final String DRAW_RING = "piece.sword.draw_ring";
    public static final String GUARD_CLICK = "piece.sword.guard_click";
    public static final String SHEATHE = "piece.sword.sheathe";
    public static final String SWING = "piece.sword.swing";
    public static final String SWING_HEAVY = "piece.sword.swing_heavy";
    public static final String SHING = "piece.sword.shing";
    public static final String CLASH = "piece.sword.clash";
    public static final String SLASH = "piece.sword.slash";
    /** Every sound event, in the order they are registered. */
    public static final String[] ALL = {DRAW, DRAW_RING, GUARD_CLICK, SHEATHE, SWING, SWING_HEAVY, SHING, CLASH, SLASH};

    /** One beat: {@code tick} ticks into the animation, {@code sound} at {@code volume} and {@code pitch}. */
    public record Beat(int tick, String sound, float volume, float pitch) {
    }

    private static final Beat[] NONE = new Beat[0];

    private SwordSounds() {
    }

    /** The beats of animation {@code name} (SwordAnims.name(anim, katana)). */
    public static Beat[] of(String name) {
        if (name == null) {
            return NONE;
        }
        return switch (name) {
            // the slow draw: the thumb pushes the guard (3-6), the blade slides out (8-16), clears the mouth at 16
            case "sword_draw" -> new Beat[] {new Beat(4, GUARD_CLICK, 0.55F, 1.3F), new Beat(9, DRAW, 0.9F, 0.95F),
                new Beat(15, DRAW_RING, 0.95F, 1.0F)};
            // the sheathe: the chiburi flick (3), laid to the mouth (8), slid home (10-17), the guard clicking home at 17
            case "sword_sheathe" -> new Beat[] {new Beat(2, SWING, 0.6F, 1.1F), new Beat(10, SHEATHE, 0.9F, 1.0F),
                new Beat(17, GUARD_CLICK, 0.9F, 1.0F)};
            // drawing straight up into the charge, and the iai from the saya
            case "sword_charge_draw" -> new Beat[] {new Beat(0, DRAW, 0.8F, 1.2F), new Beat(2, DRAW_RING, 0.95F, 1.05F)};
            case "sword_quick_draw" -> new Beat[] {new Beat(0, DRAW, 0.7F, 1.35F), new Beat(2, SHING, 1.0F, 1.05F)};
            case "sword_quick_cut" -> new Beat[] {new Beat(2, SWING, 0.9F, 1.0F)};
            // Yoru on the back: the hand closes on the hilt (7), the great sword pulled up out of its ropes (8-11), swept over the
            // shoulder and over the head (12-15), brought down and round (16)
            case "sword_draw_back" -> new Beat[] {new Beat(7, GUARD_CLICK, 0.5F, 0.7F), new Beat(8, DRAW, 1.0F, 0.62F),
                new Beat(12, SWING_HEAVY, 0.9F, 0.7F), new Beat(14, SHING, 0.85F, 0.68F), new Beat(16, SWING_HEAVY, 0.8F, 0.8F)};
            case "sword_sheathe_back" -> new Beat[] {new Beat(3, SWING_HEAVY, 0.8F, 0.75F), new Beat(9, SHEATHE, 0.95F, 0.6F),
                new Beat(14, GUARD_CLICK, 0.85F, 0.6F)};
            case "sword_charge_draw_back" -> new Beat[] {new Beat(2, GUARD_CLICK, 0.5F, 0.7F), new Beat(3, DRAW, 1.0F, 0.65F),
                new Beat(4, SWING_HEAVY, 0.85F, 0.75F)};
            default -> NONE;
        };
    }

    /** The swing a cut of this weight makes (heavy: the deep swish). */
    public static String swing(boolean heavy) {
        return heavy ? SWING_HEAVY : SWING;
    }

    /** How a blade pitches its swings: Yoru's great sword lower, Gryphon's saber a little higher, a katana as it is. */
    public static float pitch(Katana katana) {
        if (katana == null) {
            return 1.0F;
        }
        return switch (katana) {
            case YORU -> 0.78F;
            case GRYPHON -> 1.06F;
            default -> 1.0F;
        };
    }
}
