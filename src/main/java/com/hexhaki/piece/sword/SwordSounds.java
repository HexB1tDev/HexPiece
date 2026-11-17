package com.hexhaki.piece.sword;

import com.hexhaki.piece.style.Hit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 2.2.9: when the Swordsman's sounds play (pure). The owner: "grab sounds from the web for the swords unsheath, swipe, etc, all the
 * sounds must come from the web (be free for use)": every clip is from a CC0 pack on OpenGameArt.org (tools/sword_sounds.py,
 * {@code assets/hexhaki/sounds/piece/sword/SOURCES.txt}); the sound events are {@code hexhaki:piece.sword.*} ({@code PieceSounds}).
 *
 * <p>Each animation has its beats, timed to what the blade does in it (BladePaths' noto beats, Yoru's hand-off on the back): the thumb
 * breaking the saya's seal, the blade sliding out, ringing free as it clears the mouth; the chiburi flick, the slide home, the guard
 * clicking against the koiguchi; Yoru's slow, deep pull out of its ropes and the great sweep over the shoulder. The cuts swing on their
 * own windows (the light and the heavy swish, a ring of steel on the heavy ones), the Haki slash rushes the air.
 *
 * <p>Players and NPC swordsmen hear the same beats: each keeps a {@link Cursor} that plays every beat of what its sword is doing once,
 * on its tick ({@link #of(int, Katana)}: the animation's own beats and its cuts' swings, pitched for the blade).
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
    /** A beat missed by at most this many ticks (a tick skipped, the animation begun after this tick's sounds) is still played. */
    public static final int CATCH_UP = 2;

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
            // the quick draw: out of the saya and straight down the line, the dash rushing the air (3-6)
            case "sword_quick_draw" -> new Beat[] {new Beat(0, DRAW, 0.7F, 1.35F), new Beat(2, SHING, 1.0F, 1.05F),
                new Beat(3, SWING_HEAVY, 0.9F, 1.1F)};
            case "sword_quick_cut" -> new Beat[] {new Beat(2, SWING, 0.9F, 1.0F), new Beat(3, SWING_HEAVY, 0.9F, 1.1F)};
            // the charged cut let go (1-3): the deep swish and the steel ringing through it (a Haki slash adds its rush as it flies)
            case "sword_release" -> new Beat[] {new Beat(1, SWING_HEAVY, 1.0F, 0.95F), new Beat(1, SHING, 0.8F, 0.95F)};
            // the draws from the saya into a cut (the blade clears the mouth at 1-2; the cut's own swing follows)
            case "sword_draw_cut", "sword_advancing_draw" -> new Beat[] {new Beat(0, DRAW, 0.75F, 1.3F)};
            case "sword_aerial_draw" -> new Beat[] {new Beat(0, DRAW, 0.7F, 1.4F)};
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

    /**
     * The beats of what a sword is doing: {@link SwordAnims} {@code anim} with {@code katana}. The animation's own beats ({@link
     * #of(String)}); a move's cuts each swing at the start of their window (heavy: the deep swish and a quieter ring of steel; a long
     * one, as the Wheel's circle and the Pivot's full turn, swings again halfway). Pitched for the blade ({@link #pitch}), except
     * Yoru's own animations on the back, pitched already. In order of their ticks.
     */
    public static Beat[] of(int anim, Katana katana) {
        String name = SwordAnims.name(anim, katana);
        List<Beat> beats = new ArrayList<>(Arrays.asList(of(name)));
        if (anim >= SwordAnims.MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - SwordAnims.MOVES);
            if (move != null) {
                for (SwordMove.Cut cut : move.cuts()) {
                    boolean heavy = cut.weight().ordinal() >= Hit.Weight.HEAVY.ordinal();
                    beats.add(new Beat(cut.from(), swing(heavy), heavy ? 0.95F : 0.85F, 1.0F));
                    if (heavy) {
                        beats.add(new Beat(cut.from(), SHING, 0.55F, 1.1F));
                    }
                    if (cut.to() - cut.from() >= 4) {
                        beats.add(new Beat(cut.from() + (cut.to() - cut.from() + 1) / 2, swing(heavy), 0.7F, 0.92F));
                    }
                }
            }
        }
        float pitch = name != null && name.endsWith("_back") ? 1.0F : pitch(katana);
        Beat[] out = new Beat[beats.size()];
        for (int i = 0; i < out.length; i++) {
            Beat b = beats.get(i);
            out[i] = new Beat(b.tick(), b.sound(), b.volume(), b.pitch() * pitch);
        }
        Arrays.sort(out, Comparator.comparingInt(Beat::tick));
        return out;
    }

    /**
     * What one sword has played of what it is doing. {@link #advance} each tick with what it is doing now: every beat comes once, on
     * its tick (or up to {@link #CATCH_UP} late), a new animation (or the same one begun again) starts from its first beat, and a beat
     * long past (an animation first seen halfway through) is never played.
     */
    public static final class Cursor {
        private int anim = -1;
        private long start;
        private int heard = -1;

        /** The beats due now: {@code anim} began at {@code start} (game ticks), it is {@code now}. */
        public Beat[] advance(int anim, long start, long now, Katana katana) {
            if (anim != this.anim || start != this.start) {
                this.anim = anim;
                this.start = start;
                this.heard = -1;
            }
            long elapsed = now - start;
            if (elapsed < 0 || elapsed > Integer.MAX_VALUE || elapsed <= this.heard) {
                return NONE;
            }
            int t = (int) elapsed;
            List<Beat> due = new ArrayList<>();
            for (Beat beat : of(anim, katana)) {
                if (beat.tick() > this.heard && beat.tick() <= t && beat.tick() >= t - CATCH_UP) {
                    due.add(beat);
                }
            }
            this.heard = t;
            return due.toArray(NONE);
        }
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
