package com.hexhaki.piece;

import com.hexhaki.piece.haki.ArmBend;
import com.hexhaki.piece.haki.RyouPalette;
import java.util.HashSet;
import java.util.Set;

/** 2.1.3: Ryou's glow colours (bright and saturated, any random number gives one) and the bent arm its rim is drawn on. */
public final class RyouTests {
    public static void main(String[] args) {
        palette();
        bend();
        tint();
        Check.done("RyouTests");
    }

    /** 2.3.3: the Conqueror's red particles take the player's Ryou hue, keeping their brightness and alpha. */
    static void tint() {
        int blue = 0xFF3355FF;
        int red = 0xCCFF2020;
        int out = com.hexhaki.piece.haki.HueTint.tint(red, blue);
        Check.that((out >>> 24) == 0xCC, "the alpha is kept");
        int r = (out >> 16) & 255;
        int g = (out >> 8) & 255;
        int b = out & 255;
        Check.that(b == 255 && r < 80 && g < 120, "a bright red turns a bright blue: " + Integer.toHexString(out));
        int dark = com.hexhaki.piece.haki.HueTint.tint(0xFF600000, blue);
        Check.that((dark & 255) == 0x60 && ((dark >> 16) & 255) < 0x30, "a dark red ember turns a dark blue one: " + Integer.toHexString(dark));
        Check.that(com.hexhaki.piece.haki.HueTint.tint(0xFFFFFFFF, blue) == 0xFFFFFFFF, "white stays white");
        Check.that(com.hexhaki.piece.haki.HueTint.tint(0xFF000000, blue) == 0xFF000000, "black stays black");
        int same = com.hexhaki.piece.haki.HueTint.tint(red, 0xFFFF2020);
        boolean close = true;
        for (int shift = 0; shift < 32; shift += 8) {
            close &= Math.abs(((same >> shift) & 255) - ((red >> shift) & 255)) <= 2;
        }
        Check.that(close, "a red Ryou keeps the red: " + Integer.toHexString(same));
        int pale = com.hexhaki.piece.haki.HueTint.tint(red, 0xFFE0E0FF);
        Check.that(((pale >> 16) & 255) > 0x80 && (pale & 255) == 255, "a pale Ryou pales the particles: " + Integer.toHexString(pale));
    }

    static void palette() {
        Set<Integer> seen = new HashSet<>();
        for (int color : RyouPalette.COLORS) {
            int r = (color >> 16) & 255;
            int g = (color >> 8) & 255;
            int b = color & 255;
            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));
            Check.that(max >= 200, String.format("%06X is bright (its strongest channel at least 200)", color));
            Check.that(max - min >= 70, String.format("%06X is a colour, not a grey", color));
            Check.that(seen.add(color), String.format("%06X is in the palette once", color));
        }
        for (int random : new int[] {0, 1, -1, 15, 16, Integer.MIN_VALUE, Integer.MAX_VALUE, 123456789}) {
            int picked = RyouPalette.pick(random);
            Check.that(RyouPalette.given(picked) && (picked >>> 24) == 255, "any random number picks an opaque palette colour (" + random + ")");
        }
        Check.that(!RyouPalette.given(RyouPalette.NEUTRAL | 0xFF000000), "the neutral fallback is never a player's own colour");
    }

    static void bend() {
        float px = 1.0F / 16.0F;
        float[] straight = ArmBend.bent(0.0F, 0.0F, -3, 1, -1 * px, 10 * px, 0);
        Check.near(straight[1], 10 * px, 1e-6, "no bend leaves the hand where it was");
        // the fist's end on the arm's centre line, the elbow flexed 90 degrees forward: up to the elbow's height, six pixels forward
        float[] flexed = ArmBend.bent(0.0F, (float) (-Math.PI / 2), -3, 1, -1 * px, 10 * px, 0);
        Check.near(flexed[0], -1 * px, 1e-5, "a flex keeps the hand in the arm's plane");
        Check.near(flexed[1], 4 * px, 1e-5, "a 90 degree flex lifts the fist to the elbow's height");
        Check.near(flexed[2], -6 * px, 1e-5, "a negative bend flexes the forearm forward (-z)");
        float[] shoulder = ArmBend.bent(0.0F, (float) (-Math.PI / 2), -3, 1, -1 * px, -2 * px, 0);
        Check.near(shoulder[1], -2 * px, 1e-6, "the shoulder end does not move");
        // the hull's corners stay outside the hand's own corners as it bends (the rim never cuts into the fist)
        for (float amount = -2.4F; amount <= 0.01F; amount += 0.3F) {
            float[] hand = ArmBend.bent(0.6F, amount, -3, 1, 1 * px, 10 * px, 2 * px);
            float[] rim = ArmBend.bent(0.6F, amount, -3, 1, 1.55F * px, 10.55F * px, 2.55F * px);
            double out = Math.sqrt(Math.pow(rim[0] - hand[0], 2) + Math.pow(rim[1] - hand[1], 2) + Math.pow(rim[2] - hand[2], 2));
            Check.that(out > 0.5 * px && out < 1.6 * px, "the rim's corner stays just outside the fist's at bend " + amount);
        }
    }
}
