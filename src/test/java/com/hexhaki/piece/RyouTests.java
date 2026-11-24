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
        handCoat();
        gloss();
        aura();
        skinCheck();
        hakiColours();
        creation();
        Check.done("RyouTests");
    }

    /**
     * 3.6.0 (the owner's seven Haki colours, chosen on the start menu): seven sets, named by their colours alone, each rim its own and
     * none an old random Ryou colour (so a stroke's rim tells its set); the lightning's body is the set's own (black for the two black
     * ones), its glow the set's (cyan round the green rim of Green, Cyan and Black).
     */
    static void hakiColours() {
        com.hexhaki.piece.haki.HakiColour[] all = com.hexhaki.piece.haki.HakiColour.values();
        Check.that(all.length == 7, "the owner's seven Haki colours");
        String[] names = {"Black and Red", "Gold and Yellow", "Purple", "Green", "Blue", "Green, Cyan and Black", "Red and Light Red"};
        java.util.Set<Integer> rims = new java.util.HashSet<>();
        for (int i = 0; i < all.length; i++) {
            com.hexhaki.piece.haki.HakiColour c = all[i];
            Check.that(c.display.equals(names[i]), "named by its colours: " + c.display);
            Check.that(rims.add(c.rim) && !RyouPalette.given(0xFF000000 | c.rim), c.display + ": its rim is its own");
            Check.that(com.hexhaki.piece.haki.HakiColour.byRim(0xFF000000 | c.rim) == c && com.hexhaki.piece.haki.HakiColour.byCode(c.code()) == c,
                c.display + ": found again by its rim and its code");
            int body = com.hexhaki.piece.haki.Bolt.body(c.rim);
            Check.that(c.body == 0 ? Math.max((body >> 16) & 255, Math.max((body >> 8) & 255, body & 255)) <= 40 : body == c.body,
                c.display + ": the lightning's body " + (c.body == 0 ? "black" : "its own colour"));
            for (String who : new String[] {"Luffy", "Roger", "Kaido", "Zoro", "Shanks", "Whitebeard"}) {
                Check.that(!c.display.contains(who), c.display + ": no character's name");
            }
        }
        Check.that(com.hexhaki.piece.haki.HakiColour.glowFor(com.hexhaki.piece.haki.HakiColour.GREEN_CYAN_BLACK.rim) == 0x26EEFF,
            "Green, Cyan and Black glows cyan round its green rim");
        Check.that(com.hexhaki.piece.haki.HakiColour.bodyFor(0xFF2B3D) == 0 && com.hexhaki.piece.haki.HakiColour.byRim(0xFF2B3D) == null,
            "an old random colour keeps the black body");
        Check.that(com.hexhaki.piece.haki.HakiColour.byCode(0) == null && com.hexhaki.piece.haki.HakiColour.byCode(8) == null,
            "no colour chosen yet reads as none");
        Check.that(com.hexhaki.piece.haki.HakiColour.nearest(0xFFD93A) == com.hexhaki.piece.haki.HakiColour.GOLD_YELLOW
            && com.hexhaki.piece.haki.HakiColour.nearest(0x2E9BFF) == com.hexhaki.piece.haki.HakiColour.BLUE,
            "an old colour's nearest set");
    }

    /** 3.6.0: the start menu's fourth page is the Haki colour; a player who set sail before it gets that page alone. */
    static void creation() {
        com.hexhaki.piece.ui.CreationView.State state = new com.hexhaki.piece.ui.CreationView.State();
        Check.that(com.hexhaki.piece.ui.CreationView.STEPS.length == 4, "four pages");
        state.step = com.hexhaki.piece.ui.CreationView.COLOUR;
        state.gender = com.hexhaki.piece.Gender.MALE;
        state.faction = com.hexhaki.piece.Faction.PIRATE;
        state.style = com.hexhaki.piece.style.FightingStyle.SWORDSMAN;
        Check.that(!com.hexhaki.piece.ui.CreationView.click(state, "next"), "no colour picked: no setting sail");
        com.hexhaki.piece.ui.CreationView.click(state, "pick:5");
        Check.that(state.colour == com.hexhaki.piece.haki.HakiColour.GREEN_CYAN_BLACK, "a card picks its colour");
        Check.that(com.hexhaki.piece.ui.CreationView.click(state, "next"), "the colour picked: Set Sail");
        com.hexhaki.piece.ui.CreationView.State only = com.hexhaki.piece.ui.CreationView.State.colourOnly();
        com.hexhaki.piece.ui.CreationView.click(only, "back");
        Check.that(only.step == com.hexhaki.piece.ui.CreationView.COLOUR, "the colour page alone has nowhere to go back to");
        com.hexhaki.piece.ui.CreationView.click(only, "pick:0");
        Check.that(com.hexhaki.piece.ui.CreationView.click(only, "next") && only.colour == com.hexhaki.piece.haki.HakiColour.BLACK_RED,
            "and sends the colour");
        com.hexhaki.piece.ui.CreationView.Layout l = com.hexhaki.piece.ui.CreationView.Layout.of(427, 240);
        for (int i = 0; i < 7; i++) {
            int[] box = l.colourCardsForTests()[i];
            Check.that(box[0] >= l.x0() && box[2] <= l.x1() && box[2] - box[0] >= 40 && box[3] - box[1] >= 40,
                "colour card " + i + " fits a small screen");
        }
    }

    /** 2.3.4: Armament blackens the hand in the skin itself: the arm's lowest rows and the fist's end, both layers, nothing else. */
    static void handCoat() {
        java.util.function.IntBinaryOperator wide = (x, y) -> Math.round(com.hexhaki.piece.haki.HandCoat.amount(x, y, 64, 64, false) * 100);
        Check.that(wide.applyAsInt(44, 31) == 100 && wide.applyAsInt(40, 29) == 100, "the right hand's last rows are black");
        Check.that(wide.applyAsInt(44, 20) == 0 && wide.applyAsInt(44, 25) == 0, "the arm above the wrist keeps its skin");
        Check.that(wide.applyAsInt(44, 27) == 45 && wide.applyAsInt(44, 28) == 80, "fading in over the wrist");
        Check.that(wide.applyAsInt(48, 16) == 100 && wide.applyAsInt(44, 16) == 0, "the fist's end black, the shoulder's top not");
        Check.that(wide.applyAsInt(36, 63) == 100 && wide.applyAsInt(52, 63) == 100 && wide.applyAsInt(44, 47) == 100,
            "the left hand and both sleeves too");
        Check.that(wide.applyAsInt(20, 31) == 0 && wide.applyAsInt(8, 8) == 0 && wide.applyAsInt(4, 31) == 0, "the body, head and legs untouched");
        Check.that(com.hexhaki.piece.haki.HandCoat.amount(53, 31, 64, 64, true) == 1.0F
            && com.hexhaki.piece.haki.HandCoat.amount(55, 31, 64, 64, true) == 0.0F, "a slim arm's strip is 14 wide");
        Check.that(com.hexhaki.piece.haki.HandCoat.amount(88, 62, 128, 128, false) == 1.0F, "a 128 skin scaled");
        Check.that(com.hexhaki.piece.haki.HandCoat.amount(36, 63, 64, 32, false) == 0.0F
            && com.hexhaki.piece.haki.HandCoat.amount(44, 31, 64, 32, false) == 1.0F, "an old 64 x 32 skin: the one arm both use");
        int coated = com.hexhaki.piece.haki.HandCoat.coat(0xFFB08070, 1.0F);
        Check.that((coated >>> 24) == 0xFF && (coated & 255) < 24 && ((coated >> 16) & 255) < 30, "black, its shading kept faintly");
        Check.that(com.hexhaki.piece.haki.HandCoat.coat(0x00B08070, 1.0F) >>> 24 == 0, "an empty sleeve pixel stays empty");
        Check.that(com.hexhaki.piece.haki.HandCoat.coat(0xFFB08070, 0.0F) == 0xFFB08070, "untouched at nothing");
        // 2.4.7 (the owner: "turn the player's hands black the skin itself like the Nika fruit changes the skin (slowly pixel by pixel"):
        // the black spreads: nothing at 0, all of it at 1, never lighter as it goes on, the fist's end first and the wrist last, and
        // neighbouring pixels out of step with each other (pixel by pixel, not a band)
        int pixels = 0;
        int stepped = 0;
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                float full = com.hexhaki.piece.haki.HandCoat.amount(x, y, 64, 64, false);
                if (full <= 0.0F) {
                    Check.that(com.hexhaki.piece.haki.HandCoat.at(x, y, 64, 64, false, 1.0F) == 0.0F, "nothing past the hand");
                    continue;
                }
                pixels++;
                Check.that(com.hexhaki.piece.haki.HandCoat.at(x, y, 64, 64, false, 0.0F) == 0.0F, "untouched as it starts");
                Check.near(com.hexhaki.piece.haki.HandCoat.at(x, y, 64, 64, false, 1.0F), full, 1e-6, "all of it at the end");
                float last = 0.0F;
                for (int k = 1; k <= 20; k++) {
                    float now = com.hexhaki.piece.haki.HandCoat.at(x, y, 64, 64, false, k / 20.0F);
                    Check.that(now >= last - 1e-6, "never lighter as the coat comes on");
                    last = now;
                }
                if (x + 1 < 64 && com.hexhaki.piece.haki.HandCoat.amount(x + 1, y, 64, 64, false) > 0.0F
                    && Math.abs(com.hexhaki.piece.haki.HandCoat.reveal(x, y, 64, 64, false)
                        - com.hexhaki.piece.haki.HandCoat.reveal(x + 1, y, 64, 64, false)) > 0.02F) {
                    stepped++;
                }
            }
        }
        Check.that(pixels > 100 && stepped > pixels / 3, "pixel by pixel: most neighbours turn at their own moment (" + stepped + " of " + pixels + ")");
        // the right arm (40, 16): its fist's end (48..51, 16..19) before its knuckle row (row 11: y 31) before its wrist (row 6: y 26)
        float fist = com.hexhaki.piece.haki.HandCoat.reveal(49, 17, 64, 64, false);
        float knuckles = com.hexhaki.piece.haki.HandCoat.reveal(45, 31, 64, 64, false);
        float wrist = com.hexhaki.piece.haki.HandCoat.reveal(45, 26, 64, 64, false);
        Check.that(fist < knuckles + 0.15F && knuckles < wrist, "the fist first, then up the hand to the wrist (" + fist + ", " + knuckles + ", "
            + wrist + ")");
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

    /** 2.5.3: the coat's gloss: a few near-white pixels on each hand, on the hand only, laid on only as the black sets. */
    static void gloss() {
        for (boolean slim : new boolean[] {false, true}) {
            int shiny = 0;
            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 64; x++) {
                    float shine = com.hexhaki.piece.haki.HandCoat.shine(x, y, 64, 64, slim);
                    float amount = com.hexhaki.piece.haki.HandCoat.amount(x, y, 64, 64, slim);
                    Check.that(shine >= 0.0F && shine <= 1.0F, "a gloss is a share");
                    if (shine > 0.0F) {
                        shiny++;
                        Check.that(amount >= 1.0F, "the gloss only where the hand is fully black (" + x + ", " + y + ")");
                    }
                    int pixel = 0xFFB08070;
                    Check.that(com.hexhaki.piece.haki.HandCoat.finish(pixel, x, y, 64, 64, slim, 0.6F)
                        == com.hexhaki.piece.haki.HandCoat.coat(pixel, 0.6F), "no gloss before the black has set");
                    int done = com.hexhaki.piece.haki.HandCoat.finish(pixel, x, y, 64, 64, slim, 1.0F);
                    Check.that(done >>> 24 == 0xFF, "the gloss keeps the alpha");
                    Check.that(com.hexhaki.piece.haki.HandCoat.finish(0x00B08070, x, y, 64, 64, slim, 1.0F) >>> 24 == 0,
                        "an empty sleeve pixel stays empty under the gloss");
                    if (shine > 0.0F) {
                        Check.that((done & 255) > (com.hexhaki.piece.haki.HandCoat.coat(pixel, 1.0F) & 255) + 40, "a gloss pixel is light");
                    }
                }
            }
            // per arm and sleeve: the knuckles' edge (the end's front row), its spot, the front streak, the outer side
            Check.that(shiny >= 4 * 8 && shiny <= 4 * 14, "a handful of gloss pixels on each hand and sleeve (" + shiny + ", slim " + slim + ")");
            int big = 0;
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    if (com.hexhaki.piece.haki.HandCoat.shine(x, y, 128, 128, slim) > 0.0F) {
                        big++;
                    }
                }
            }
            Check.that(big == shiny * 4, "a 128 skin's gloss is the same pixels, scaled");
        }
    }

    /**
     * 2.5.3: the fist aura (haki/FistAura; the owner's target picture): seen from every side, near and far, bent or not, each arm: it is
     * built, finite, every quad turned to the eye (so culling never takes one; mirrored, every one turned round), the rim bright, the
     * whole thing hugging the fist (never far out), the same every time it is built, and steady from one frame to the next (the owner:
     * "flickering on and off").
     */
    static void aura() {
        int views = 0;
        for (float bend : new float[] {0.0F, -1.2F, -2.2F}) {
            for (boolean slim : new boolean[] {false, true}) {
                for (boolean right : new boolean[] {true, false}) {
                    float middle = (slim ? 0.5F : 1.0F) * (right ? -1.0F : 1.0F);
                    float min = middle - (slim ? 1.5F : 2.0F);
                    float max = middle + (slim ? 1.5F : 2.0F);
                    float[] fist = ArmBend.bent(0.0F, bend, min, max, middle / 16.0F, 8.5F / 16.0F, 0.0F);
                    float[] wrist = ArmBend.bent(0.0F, bend, min, max, middle / 16.0F, 4.6F / 16.0F, 0.0F);
                    double[] up = {wrist[0] - fist[0], wrist[1] - fist[1], wrist[2] - fist[2]};
                    double ul = Math.sqrt(up[0] * up[0] + up[1] * up[1] + up[2] * up[2]);
                    for (int i = 0; i < 26; i++) {
                        double[] d = sphere(i);
                        for (double distance : new double[] {0.6, 3.0, 14.0}) {
                            double[] eye = {fist[0] + d[0] * distance, fist[1] + d[1] * distance, fist[2] + d[2] * distance, 1};
                            for (boolean mirrored : new boolean[] {false, true}) {
                                java.util.List<float[]> quads = new java.util.ArrayList<>();
                                int[] top = {0};
                                com.hexhaki.piece.haki.FistAura.build(0.0F, bend, slim, right, eye, mirrored, 37.5F, 99, 0xFF2B3D, 1.0F, 0.5F,
                                    (q, c) -> {
                                        quads.add(q.clone());
                                        for (int k = 0; k < 4; k++) {
                                            top[0] = Math.max(top[0], c[k] >>> 24);
                                        }
                                    });
                                // looking down the arm from the elbow's side the outline is the wrist's, where the aura has faded (and
                                // the forearm hides the fist): from anywhere else, an aura with a bright rim
                                boolean alongArm = (d[0] * up[0] + d[1] * up[1] + d[2] * up[2]) / ul > 0.55;
                                if (!alongArm) {
                                    Check.that(quads.size() > 20, "an aura from every side (" + quads.size() + " quads)");
                                    Check.that(top[0] >= 200, "its rim bright (" + top[0] + ")");
                                }
                                for (float[] q : quads) {
                                    double[] n = cross(sub(q, 6, 0), sub(q, 9, 3));
                                    double[] mid = {(q[0] + q[6]) / 2, (q[1] + q[7]) / 2, (q[2] + q[8]) / 2};
                                    double facing = n[0] * (eye[0] - mid[0]) + n[1] * (eye[1] - mid[1]) + n[2] * (eye[2] - mid[2]);
                                    for (float v : q) {
                                        Check.that(Float.isFinite(v), "finite");
                                    }
                                    Check.that(mirrored ? facing <= 1e-12 : facing >= -1e-12, "every quad turned to the eye (mirrored: away)");
                                    double reach = Math.sqrt(Math.pow(mid[0] - fist[0], 2) + Math.pow(mid[1] - fist[1], 2) + Math.pow(mid[2] - fist[2], 2));
                                    double far = Math.max(1.0, distance * 16.0 / 0.9375 / 120.0);
                                    Check.that(reach < (14.0 * far) / 16.0, "it hugs the fist (" + reach * 16 + " px out)");
                                }
                                views++;
                            }
                        }
                    }
                }
            }
        }
        Check.that(views == 3 * 2 * 2 * 26 * 3 * 2, "every view built");
        // the same each time, and steady: a twentieth of a tick later every point of the strip moves a little, never jumps
        double[] eye = {-0.9, 0.7, -0.6, 1};
        java.util.List<float[]> a = strip(eye, 50.0F, new java.util.ArrayList<>());
        java.util.List<float[]> b = strip(eye, 50.0F, new java.util.ArrayList<>());
        Check.that(a.size() == b.size(), "built the same each time");
        for (int i = 0; i < a.size(); i++) {
            Check.that(java.util.Arrays.equals(a.get(i), b.get(i)), "the same quads each time");
        }
        for (float age = 0.0F; age < 200.0F; age += 7.3F) {
            java.util.List<int[]> colours = new java.util.ArrayList<>();
            java.util.List<int[]> later = new java.util.ArrayList<>();
            java.util.List<float[]> now = strip(eye, age, colours);
            java.util.List<float[]> next = strip(eye, age + 0.05F, later);
            Check.that(now.size() == next.size(), "the strip keeps its quads from frame to frame");
            for (int i = 0; i < now.size(); i++) {
                for (int k = 0; k < 12; k++) {
                    Check.that(Math.abs(now.get(i)[k] - next.get(i)[k]) < 0.08F / 16.0F, "no point jumps between frames");
                }
                for (int k = 0; k < 4; k++) {
                    Check.that(Math.abs((colours.get(i)[k] >>> 24) - (later.get(i)[k] >>> 24)) <= 6, "no flicker between frames");
                }
            }
        }
        // the eye inside the fist: nothing to outline
        java.util.List<float[]> inside = new java.util.ArrayList<>();
        com.hexhaki.piece.haki.FistAura.build(0.0F, 0.0F, false, true, new double[] {-1.0 / 16, 8.0 / 16, 0, 1}, false, 1.0F, 1, 0xFFFFFF, 1.0F,
            0.0F, false, (q, c) -> inside.add(q));
        Check.that(inside.isEmpty(), "no outline from inside the fist");
        // nothing at no strength
        java.util.List<float[]> none = new java.util.ArrayList<>();
        com.hexhaki.piece.haki.FistAura.build(0.0F, 0.0F, false, true, eye, false, 1.0F, 1, 0xFFFFFF, 0.0F, 0.0F, (q, c) -> none.add(q));
        Check.that(none.isEmpty(), "nothing when it is off");
    }

    static java.util.List<float[]> strip(double[] eye, float age, java.util.List<int[]> colours) {
        java.util.List<float[]> out = new java.util.ArrayList<>();
        com.hexhaki.piece.haki.FistAura.build(0.0F, -0.4F, false, true, eye, false, age, 5, 0x2E7BFF, 0.95F, 0.0F, false, (q, c) -> {
            out.add(q.clone());
            colours.add(c.clone());
        });
        return out;
    }

    /** 26 directions round a point: the cube's faces, edges and corners. */
    static double[] sphere(int i) {
        int n = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    if (n++ == i) {
                        double l = Math.sqrt(x * x + y * y + z * z);
                        return new double[] {x / l, y / l, z / l};
                    }
                }
            }
        }
        throw new IllegalArgumentException();
    }

    static double[] sub(float[] q, int a, int b) {
        return new double[] {q[a] - q[b], q[a + 1] - q[b + 1], q[a + 2] - q[b + 2]};
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    /**
     * 2.5.3 (the owner: "sometimes the player's skin vanishes when starting a world"): a skin read back empty is never taken for a skin;
     * a real one (its base layer opaque, as the game makes every downloaded skin) always is, any size; a copy is told apart from its
     * skin by any pixel it samples.
     */
    static void skinCheck() {
        int[] blank = new int[64 * 64];
        Check.that(!com.hexhaki.piece.haki.SkinCheck.real((x, y) -> blank[y * 64 + x], 64, 64), "an empty readback is not a skin");
        int[] skin = new int[64 * 64];
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                // the base layer opaque, the outer layer (the hat, the jacket, the sleeves) empty
                boolean outer = y < 16 && x >= 32 || y >= 32 && y < 48 && x < 56 || y >= 48 && (x < 16 || x >= 48);
                skin[y * 64 + x] = outer ? 0 : 0xFF000000 | (x * 4) << 16 | (y * 4) << 8 | 0x60;
            }
        }
        Check.that(com.hexhaki.piece.haki.SkinCheck.real((x, y) -> skin[y * 64 + x], 64, 64), "a real skin with an empty outer layer");
        Check.that(com.hexhaki.piece.haki.SkinCheck.real((x, y) -> skin[(y / 2) * 64 + x / 2], 128, 128), "a 128 skin");
        Check.that(com.hexhaki.piece.haki.SkinCheck.real((x, y) -> skin[y * 64 + x], 64, 32), "an old 64 x 32 skin");
        int[] faceless = skin.clone();
        for (int y = 8; y < 16; y++) {
            for (int x = 8; x < 16; x++) {
                faceless[y * 64 + x] = 0;
            }
        }
        Check.that(!com.hexhaki.piece.haki.SkinCheck.real((x, y) -> faceless[y * 64 + x], 64, 64), "no face, not a skin read back");
        Check.that(!com.hexhaki.piece.haki.SkinCheck.real((x, y) -> skin[y * 64 + x], 32, 32), "too small for a skin");
        Check.that(com.hexhaki.piece.haki.SkinCheck.same((x, y) -> skin[y * 64 + x], (x, y) -> skin[y * 64 + x], 64, 64), "a skin is itself");
        int[] other = skin.clone();
        other[20 * 64 + 22] ^= 0x00010000;
        Check.that(!com.hexhaki.piece.haki.SkinCheck.same((x, y) -> skin[y * 64 + x], (x, y) -> other[y * 64 + x], 64, 64),
            "a changed pixel tells them apart");
        Check.that(!com.hexhaki.piece.haki.SkinCheck.same((x, y) -> skin[y * 64 + x], (x, y) -> blank[y * 64 + x], 64, 64),
            "a skin is not an empty copy");
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
