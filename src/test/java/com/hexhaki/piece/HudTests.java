package com.hexhaki.piece;

import com.hexhaki.piece.ui.AbilityHudView;
import com.hexhaki.piece.ui.AbilityPanel;
import com.hexhaki.piece.ui.EnergyBarView;
import com.hexhaki.piece.ui.UiCanvas;
import java.util.List;
import java.util.Map;

/** 2.2.2: the one abilities panel on the left (HexGhoul's): what it lists in each mode, and its card's size. */
public final class HudTests {
    static final Map<String, String> KEYS = Map.of("attack", "LMB", "use", "RMB", "special_a", "Y", "special_b", "U", "toggle", "R", "armament",
        "Ctrl+R", "observation", "V", "menu", "P");

    public static void main(String[] args) throws Exception {
        energy();
        nika();
        ranks();
        sword();
        style();
        fruit();
        off();
        layout();
        Check.done("HudTests");
    }

    static AbilityPanel.Input input(String style, boolean combat, String sword, String fruit, boolean fruitMode, boolean armament) {
        return new AbilityPanel.Input(KEYS, style, 0xFF6F8BAE, combat, List.of("Combo", "Heavy Kick", "Flame Kick", "Sky Walk"), sword, fruit,
            0xFFFFE070, fruitMode, List.of(new AbilityPanel.Move("G", "Light Beam", "", 30, 60), new AbilityPanel.Move("K", "Yata Mirror", "400", 0, 0)),
            true, armament, false, false, List.of(new AbilityPanel.Move("G", "King's Awakening (tap / hold)", "", 0, 0)), 60, 100);
    }

    static boolean has(AbilityPanel.Panel panel, String keys, String name) {
        for (AbilityPanel.Row row : panel.rows()) {
            if (row.keys().equals(keys) && row.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    static void sword() {
        AbilityPanel.Panel sheathed = AbilityPanel.of(input("Swordsman", true, "sheathed", null, false, false));
        Check.that(sheathed.title().equals("SWORDSMAN") && sheathed.subtitle().equals("katana sheathed"), "the Swordsman's panel says the katana is in its saya");
        Check.that(has(sheathed, "LMB", "Draw (slow)"), "sheathed: the attack key draws, slowly");
        Check.that(has(sheathed, "Ctrl+LMB", "Quick draw"), "the quick draw on Ctrl and the attack key");
        Check.that(has(sheathed, "RMB hold", "Charged cut"), "held right click: the charged cut");
        AbilityPanel.Panel drawn = AbilityPanel.of(input("Swordsman", true, "drawn", null, false, true));
        Check.that(has(drawn, "LMB", "Cut string") && has(drawn, "RMB hold", "Haki slash"), "drawn with Armament on: the string and the Haki slash");
        Check.that(has(drawn, "Ctrl+R", "Armament: on"), "Armament on Ctrl + R, lit");
        Check.near(drawn.energy(), 0.6, 1e-9, "the Haki energy under the title");
    }

    static void style() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Black Leg", true, null, "Pika Pika", false, false));
        Check.that(has(panel, "LMB", "Combo") && has(panel, "RMB", "Heavy Kick") && has(panel, "Y", "Flame Kick") && has(panel, "U", "Sky Walk"),
            "a style's string, heavy and two specials on their keys");
        Check.that(has(panel, "R", "Swap to Pika Pika"), "R swaps to the fruit");
        Check.that(has(panel, "G", "King's Awakening (tap / hold)"), "the Conqueror's quick slots");
        Check.that(has(panel, "P", "Captain's Log"), "the Captain's Log key");
    }

    static void fruit() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Black Leg", false, null, "Pika Pika", true, false));
        Check.that(panel.title().equals("PIKA PIKA"), "the fruit's own mode: its name");
        AbilityPanel.Row beam = panel.rows().get(0);
        Check.that(beam.name().equals("Light Beam") && !beam.locked() && Math.abs(beam.ready() - 0.5) < 1e-9, "a recharging move, half way");
        AbilityPanel.Row mirror = panel.rows().get(1);
        Check.that(mirror.locked() && mirror.needs().equals("400") && mirror.colour() == AbilityPanel.DIM, "a move not reached yet: dim, with what it needs");
        Check.that(has(panel, "R", "Back to Black Leg and Haki"), "R back to the style and Haki");
        Check.that(!has(panel, "Ctrl+R", "Armament"), "the fruit's panel is the fruit's alone: Haki is with the style");
    }

    static void off() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Six Powers", false, null, null, false, false));
        Check.that(panel.title().equals("SIX POWERS") && has(panel, "R", "Combat mode"), "combat mode off: the style and Haki, and the key that turns it on");
        Check.that(has(panel, "Ctrl+R", "Armament") && has(panel, "G", "King's Awakening (tap / hold)"), "Haki's keys listed with the style, in any form");
        Check.that(AbilityPanel.seconds(30).equals("1.5s") && AbilityPanel.seconds(400).equals("20s"), "recharges in seconds");
    }

    static void layout() {
        UiCanvas measure = new UiCanvas() {
            public void fill(int x0, int y0, int x1, int y1, int argb) {
            }

            public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
            }

            public void text(String text, int x, int y, int argb, boolean shadow) {
            }

            public int width(String text) {
                return text.length() * 6;
            }

            public void face(int x, int y, int size) {
            }

            public void push() {
            }

            public void translate(float x, float y) {
            }

            public void scale(float factor) {
            }

            public void pop() {
            }
        };
        AbilityPanel.Panel panel = AbilityPanel.of(input("Swordsman", true, "drawn", null, false, true));
        AbilityHudView.Layout layout = AbilityHudView.layout(measure, panel);
        Check.that(layout.height() == 3 + 9 + 4 + panel.rows().size() * 10 + 3 - 1, "one line a move under the title and the gauge");
        int longest = 0;
        for (AbilityPanel.Row row : panel.rows()) {
            longest = Math.max(longest, measure.width(row.name()));
        }
        Check.that(layout.width() >= longest + layout.keyWidth(), "wide enough for every name after the keys");
        Check.that(layout.height() * AbilityHudFits.SCALE < 140, "small on screen: it fits above the chat");
    }

    /** The game's scale (client/AbilityHud.SCALE: 0.6), kept here so the pure test does not load the client class. */
    static final class AbilityHudFits {
        static final float SCALE = 0.6F;
    }

    /**
     * 3.2.0: the fruit energy bar (ui/EnergyBarView) draws only inside its place over the food bar (the food row's width, its emblem's
     * round at the right end, the leaf over it), whatever the energy, the white chip, the time and a refused move's shake.
     */
    static void energy() {
        int right = 200;
        int top = 100;
        int[] box = {right - EnergyBarView.WIDTH - 3, top - EnergyBarView.EMBLEM, right + EnergyBarView.EMBLEM / 2 + 4, top + EnergyBarView.HEIGHT + 4};
        for (double fill : new double[] {0.0, 0.05, 0.18, 0.5, 1.0}) {
            for (double denied : new double[] {0.0, 1.0}) {
                for (double time = 0; time < 40; time += 7) {
                    boolean[] inside = {true};
                    int[] drawn = {0};
                    UiCanvas canvas = new UiCanvas() {
                        @Override
                        public void fill(int x0, int y0, int x1, int y1, int argb) {
                            drawn[0]++;
                            inside[0] &= x0 >= box[0] && x1 <= box[2] && y0 >= box[1] && y1 <= box[3];
                        }

                        @Override
                        public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
                            this.fill(x0, y0, x1, y1, top);
                        }

                        @Override
                        public void text(String text, int x, int y, int argb, boolean shadow) {
                            inside[0] = false;
                        }

                        @Override
                        public int width(String text) {
                            return text.length() * 6;
                        }

                        @Override
                        public void face(int x, int y, int size) {
                            inside[0] = false;
                        }

                        @Override
                        public void push() {
                        }

                        @Override
                        public void translate(float x, float y) {
                        }

                        @Override
                        public void scale(float factor) {
                        }

                        @Override
                        public void pop() {
                        }
                    };
                    EnergyBarView.draw(canvas, right, top, fill, Math.min(1.0, fill + 0.2), 0x9FE6FF, time, denied);
                    Check.that(drawn[0] > 40 && inside[0], "energy bar: drawn in its place (fill " + fill + ", denied " + denied + ", t " + time + ")");
                }
            }
        }
    }

    /**
     * 3.2.0 (the owner: the fruits' rarity "in the name and everything"; asked, Mythical the Nika fruit and the Uo Uo no Mi, Legendary the
     * rest): every fruit has its rank, its name in the language file is written in that rank's colour, the Devil Fruits tab's title is
     * there, and every fruit move with a cooldown has its energy cost listed (server/FruitEnergy).
     */
    /**
     * 3.2.3 (the owner, asked: "All stretch moves" work untransformed): the stretching punch, Rocket, Whip, Gatling, Giant and Coil
     * Spring (press and let go) go ahead out of Gear 5; Rubber World, Star Gun and the transformation's own toggle are left to the
     * original's rules (3.3.2: Sky Bounce is gone).
     */
    static void nika() {
        java.util.Set<String> stretch = java.util.Set.of("PISTOL", "ROCKET_START", "ROCKET_RELEASE", "WHIP", "GATLING_START", "GATLING_RELEASE",
            "GIANT_START", "GIANT_RELEASE", "COIL_START", "COIL_RELEASE", "GIANT_GRAB");
        for (com.hexhaki.network.GearAction action : com.hexhaki.network.GearAction.values()) {
            Check.that(com.hexhaki.piece.server.NikaRules.stretch(action) == stretch.contains(action.name()),
                "nika: " + action + (stretch.contains(action.name()) ? " works untransformed" : " needs Gear 5"));
        }
        for (String needed : stretch) {
            Check.that(java.util.Arrays.stream(com.hexhaki.network.GearAction.values()).anyMatch(a -> a.name().equals(needed)),
                "nika: the original has " + needed);
        }
        // 3.2.6: the Bajrang Gun's slam clip turns the body and points the arm by gear/NikaSlam's very numbers (client/gear/NikaFist
        // draws the fist where they say): at each key, the body's pitch (PlayerAnimator's: back positive) and the arm's
        String clip;
        try {
            clip = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/player_animation/gear5_bajrang_slam.json"));
        } catch (java.io.IOException failure) {
            throw new AssertionError(failure);
        }
        for (int k = 0; k < com.hexhaki.piece.gear.NikaSlam.T.length; k++) {
            int tick = (int) com.hexhaki.piece.gear.NikaSlam.T[k];
            double body = -com.hexhaki.piece.gear.NikaSlam.PITCH[k];
            double arm = com.hexhaki.piece.gear.NikaSlam.armPitch(k);
            Check.that(keyed(clip, tick, "body", fmt(body)), "nika: the slam's body pitch at " + tick + " is NikaSlam's (" + body + ")");
            Check.that(keyed(clip, tick, "rightArm", fmt(arm)), "nika: the slam's arm pitch at " + tick + " is NikaSlam's (" + arm + ")");
        }
        giantHold();
        starRubber();
        rubberWorld();
        toonForce();
        rubberJump();
        dawnPistol();
    }

    /**
     * 3.3.9: Dawn Pistol (the owner: "Build as proposed"; first person left alone): its two clips keep every arm move of gear5_pistol
     * exactly (first person is the original's own, and the third-person arms match it), key no head and no leg, and hold the body square
     * (within 6 degrees, no overshooting easing) while the stretched arm is out (ticks 6 to 13: the original draws it in the turned body's
     * frame); the POW is a Nika kind of its own, cheap, and gone after its life.
     */
    static void dawnPistol() {
        java.util.regex.Pattern arms = java.util.regex.Pattern.compile("\\{\"tick\":[^{}]*\"(?:rightArm|leftArm)\":\\{[^{}]*\\}\\}");
        java.util.regex.Pattern yaw = java.util.regex.Pattern.compile("\\{\"tick\":([0-9.]+),\"easing\":\"(\\w+)\",\"body\":\\{\"yaw\":(-?[0-9.]+)\\}\\}");
        java.util.List<String> pistol = matches(arms, clipText("gear5_pistol"));
        Check.that(pistol.size() >= 16, "dawnPistol: gear5_pistol's arm moves read (" + pistol.size() + ")");
        for (String name : new String[] {com.hexhaki.piece.server.NikaPistol.CLIP_RIGHT, com.hexhaki.piece.server.NikaPistol.CLIP_LEFT}) {
            String text = clipText(name);
            Check.that(matches(arms, text).equals(pistol), "dawnPistol: " + name + "'s arms are gear5_pistol's exactly");
            Check.that(!text.contains("\"head\"") && !text.contains("Leg\""), "dawnPistol: " + name + " keys no head and no leg");
            java.util.regex.Matcher m = yaw.matcher(text);
            int keys = 0;
            double wound = 0;
            while (m.find()) {
                double tick = Double.parseDouble(m.group(1));
                double value = Double.parseDouble(m.group(3));
                wound = Math.max(wound, Math.abs(value));
                keys++;
                if (tick >= 6 && tick <= 13) {
                    Check.that(Math.abs(value) <= 6 && !m.group(2).contains("back") && !m.group(2).contains("elastic"),
                        "dawnPistol: " + name + " square while the arm is out (" + value + " at " + tick + ")");
                }
            }
            Check.that(keys >= 6 && wound >= 30, "dawnPistol: " + name + " winds the body up (" + wound + ")");
        }
        int pow = com.hexhaki.piece.server.NikaPistol.POW;
        Check.that(pow >= 160 && pow < 180 && pow != com.hexhaki.piece.server.NikaJump.HOP && pow != com.hexhaki.piece.server.NikaJump.LAND
            && pow != com.hexhaki.piece.server.GiantHold.HOLD && pow != com.hexhaki.piece.server.GiantHold.LET_GO, "dawnPistol: the POW's own Nika kind");
        int[] quads = new int[1];
        com.hexhaki.piece.dragon.FireMesh.Sink count = (xyz, argb) -> quads[0]++;
        int most = 0;
        for (double age = 0; age <= com.hexhaki.piece.gear.NikaArt.POW_LIFE; age += 0.5) {
            quads[0] = 0;
            com.hexhaki.piece.gear.NikaArt.pow(count, 0.3, 1.0, 5.0, 0, 0, 1, age, 1.9, 7);
            most = Math.max(most, quads[0]);
        }
        Check.that(most > 60 && most < 400, "dawnPistol: the POW drawn, cheaply (" + most + " quads at most)");
        quads[0] = 0;
        com.hexhaki.piece.gear.NikaArt.pow(count, 0.3, 1.0, 5.0, 0, 0, 1, com.hexhaki.piece.gear.NikaArt.POW_LIFE + 0.5, 1.9, 7);
        Check.that(quads[0] == 0, "dawnPistol: the POW gone after its life");
        Check.that(com.hexhaki.piece.server.NikaPistol.PISTOL_AWARD == 5, "dawnPistol: the Pistol's landing (the original's award 5)");
        // the owner: "make sure it bends the entities a ton more and looks better than the current one" (gear/NikaBend.dawn)
        double[] shape = new double[5];
        double[] star = new double[5];
        Check.that(!com.hexhaki.piece.gear.NikaBend.dawn(-0.1, 1, shape) && shape[com.hexhaki.piece.gear.NikaBend.SHEAR] == 0,
            "dawnPistol: the body at rest before the blow");
        Check.that(!com.hexhaki.piece.gear.NikaBend.dawn(com.hexhaki.piece.gear.NikaBend.dawnLength() + 0.1, 1, shape)
            && shape[com.hexhaki.piece.gear.NikaBend.UP] == 1, "dawnPistol: the body at rest after the pull");
        double pulled = 0;
        double squashed = 1;
        double starMost = 0;
        double last = 0;
        double jump = 0;
        for (double t = 0; t < com.hexhaki.piece.gear.NikaBend.dawnLength(); t += 0.25) {
            com.hexhaki.piece.gear.NikaBend.dawn(t, 1, shape);
            // how far the top of a body 1.8 high is flung along the blow
            double top = shape[com.hexhaki.piece.gear.NikaBend.SHEAR] * shape[com.hexhaki.piece.gear.NikaBend.UP] * 1.8;
            pulled = Math.max(pulled, top);
            if (t > 1) {
                jump = Math.max(jump, Math.abs(top - last));
            }
            last = top;
            squashed = Math.min(squashed, shape[com.hexhaki.piece.gear.NikaBend.UP]);
            Check.that(shape[com.hexhaki.piece.gear.NikaBend.ACROSS] > 0.3 && shape[com.hexhaki.piece.gear.NikaBend.UP] > 0.4,
                "dawnPistol: the body never folds to nothing");
            com.hexhaki.piece.gear.NikaBend.shape(t, 26, 17, 1, star);
            starMost = Math.max(starMost, Math.abs(star[com.hexhaki.piece.gear.NikaBend.SHEAR] * star[com.hexhaki.piece.gear.NikaBend.UP] * 1.8));
        }
        Check.that(pulled > 3.5 && pulled < 6, "dawnPistol: the top flung far along the blow (" + pulled + " blocks)");
        Check.that(pulled > 3 * starMost, "dawnPistol: a ton more than Star Gun's bend (" + pulled + " against " + starMost + ")");
        Check.that(squashed < 0.85, "dawnPistol: it snaps back through a squash (" + squashed + ")");
        Check.that(jump < 1.6, "dawnPistol: no jumps past the smack (" + jump + " a quarter tick)");
        com.hexhaki.piece.gear.NikaBend.dawn(5, 0.4, shape);
        double weak = shape[com.hexhaki.piece.gear.NikaBend.SHEAR];
        com.hexhaki.piece.gear.NikaBend.dawn(5, 1, shape);
        Check.that(weak < 0.5 * shape[com.hexhaki.piece.gear.NikaBend.SHEAR], "dawnPistol: a heavy body pulled less");
    }

    static String clipText(String name) {
        try {
            return java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/player_animation/" + name + ".json"))
                .replaceAll("\\s+", "");
        } catch (java.io.IOException failure) {
            throw new AssertionError(failure);
        }
    }

    static java.util.List<String> matches(java.util.regex.Pattern pattern, String text) {
        java.util.List<String> out = new java.util.ArrayList<>();
        java.util.regex.Matcher m = pattern.matcher(text);
        while (m.find()) {
            out.add(m.group());
        }
        java.util.Collections.sort(out);
        return out;
    }

    /**
     * 3.3.2 (the owner: "default jumping is much higher ... it always turns the ground into rubber"; asked, Gear 5 only, about 5 blocks):
     * the jump goes about five blocks up, its ground only ever rises, springs up under the take-off and not under the landing feet,
     * settles within its life and its reach; Rubber World throws what stands where a crest rises, harder for a faster rise.
     */
    static void rubberJump() {
        double y = 0;
        double v = com.hexhaki.piece.gear.RubberJump.lifted(0.42);
        double peak = 0;
        for (int t = 0; t < 60 && y >= 0; t++) {
            y += v;
            v = (v - 0.08) * 0.98;
            peak = Math.max(peak, y);
        }
        Check.that(peak > 4.5 && peak < 5.5, "rubberJump: a Gear 5 jump goes about five blocks up (" + peak + ")");
        Check.that(com.hexhaki.piece.gear.RubberJump.lifted(-0.1) == -0.1, "rubberJump: a body not going up is not lifted");
        for (boolean landing : new boolean[] {false, true}) {
            double most = 0;
            for (double t = -1; t <= com.hexhaki.piece.gear.RubberJump.LIFE + 2; t += 0.5) {
                for (double r = 0; r < 8; r += 0.25) {
                    for (double a = 0; a < 6.3; a += 0.7) {
                        double h = com.hexhaki.piece.gear.RubberJump.hop(r, a, t, landing);
                        Check.that(h >= 0 && h < 2.4, "rubberJump: the ground rises 0 to 2.4 (" + h + " at r " + r + ", t " + t + ")");
                        most = Math.max(most, h);
                        if (t > com.hexhaki.piece.gear.RubberJump.LIFE || t < 0
                            || r > (landing ? com.hexhaki.piece.gear.RubberJump.LAND_RADIUS : com.hexhaki.piece.gear.RubberJump.HOP_RADIUS)) {
                            Check.that(h == 0, "rubberJump: still outside its life and reach (" + h + " at r " + r + ", t " + t + ")");
                        }
                    }
                }
            }
            Check.that(most > 0.4, "rubberJump: the ground visibly rises (" + most + ")");
            // 3.3.8 (the owner: "should be higher like lift more"; asked, "About 2x"): the first ring lifts about twice 3.3.2's
            Check.that(most > (landing ? 0.85 : 1.0), "rubberJump: 3.3.8's ripples lift about twice as high (" + most + ")");
        }
        Check.that(com.hexhaki.piece.gear.RubberJump.FALL_MIN >= 1.5 && com.hexhaki.piece.gear.RubberJump.FALL_MIN <= 2.5,
            "rubberJump: a real fall ripples the landing, a step down does not");
        Check.that(com.hexhaki.piece.gear.RubberJump.hop(0, 0, 1.5, false) > 0.4, "rubberJump: the take-off's ground springs up under the feet");
        Check.that(com.hexhaki.piece.gear.RubberJump.hop(0.3, 0, 1, true) < 0.05, "rubberJump: the landing feet's own footing stays still");
        Check.that(com.hexhaki.piece.gear.RubberWave.toss(0.01) == 0, "rubberJump: a still ground throws nothing");
        Check.that(com.hexhaki.piece.gear.RubberWave.toss(0.3) > com.hexhaki.piece.gear.RubberWave.toss(0.08)
            && com.hexhaki.piece.gear.RubberWave.toss(0.08) > 0, "rubberJump: a faster crest throws harder");
        // the very waves every client draws throw bodies: a ring's front rising under a body standing in its way
        double rise = com.hexhaki.piece.gear.RubberWave.rise(5, 0, 5, 40, Double.POSITIVE_INFINITY)
            - com.hexhaki.piece.gear.RubberWave.rise(5, 0, 4, 40, Double.POSITIVE_INFINITY);
        Check.that(com.hexhaki.piece.gear.RubberWave.toss(rise) > 0, "rubberJump: Rubber World's first ring throws a body 5 blocks out (" + rise + ")");
        Check.that(com.hexhaki.piece.gear.ToonForce.score("gear5_jump") != null && com.hexhaki.piece.gear.ToonForce.score("gear5_land") != null,
            "rubberJump: the jump and the landing have Toon Force beats");
    }

    /**
     * 3.3.1: Toon Force (gear/ToonForce): the Nika moves have beats, the transformation and the lance none (the owner loves them), the
     * spring rings and settles within bounds, a loop's beats come again, and the body never folds to nothing.
     */
    static void toonForce() {
        for (String clip : new String[] {"gear5_pistol", "gear5_dawn_pistol", "gear5_dawn_pistol_left", "gear5_gatling", "gear5_rocket",
            "gear5_whip", "gear5_dawn_whip", "gear5_giant_grab",
            "gear5_giant_throw", "gear5_gigante_laugh", "gear5_coil_charge", "gear5_coil_release", "gear5_bounce", "gear5_bajrang_slam",
            "gear5_dodge_backflip", "gear5_stance"}) {
            Check.that(com.hexhaki.piece.gear.ToonForce.score(clip) != null, "nika: " + clip + " has Toon Force beats");
            Check.that(new java.io.File("src/main/resources/assets/hexhaki/player_animation/" + clip + ".json").exists(),
                "nika: Toon Force's " + clip + " is a real clip");
        }
        for (String clip : new String[] {"gear5_awaken", "gear5_lance_charge", "gear5_lance_throw", "pika_beam", "__clear__"}) {
            Check.that(com.hexhaki.piece.gear.ToonForce.score(clip) == null, "nika: no Toon Force on " + clip);
        }
        com.hexhaki.piece.gear.ToonForce.Score rocket = com.hexhaki.piece.gear.ToonForce.score("gear5_rocket");
        double[] spring = new double[2];
        double most = 0;
        double least = 0;
        double played = -1.0E-6;
        for (int t = 1; t < 80; t++) {
            com.hexhaki.piece.gear.ToonForce.step(spring, rocket.kicks(played, t));
            played = t;
            most = Math.max(most, spring[0]);
            least = Math.min(least, spring[0]);
            Check.that(Math.abs(spring[0]) <= com.hexhaki.piece.gear.ToonForce.MOST, "nika: Toon Force within its most");
        }
        Check.that(most > 0.3 && least < -0.5, "nika: the rocket squashes and stretches (" + least + " to " + most + ")");
        Check.that(com.hexhaki.piece.gear.ToonForce.still(spring), "nika: the spring settles after the move (" + spring[0] + ")");
        com.hexhaki.piece.gear.ToonForce.Score gatling = com.hexhaki.piece.gear.ToonForce.score("gear5_gatling");
        Check.that(Math.abs(gatling.kicks(12, 16.5) - gatling.kicks(3, 7.5)) < 1e-9 && gatling.kicks(20, 40) != 0,
            "nika: the Gatling's beats come again as it loops");
        Check.that(Math.abs(Math.abs(gatling.kicks(11.9, 12.1)) - 0.28) < 1e-9, "nika: one beat on the loop's end, not two");
        Check.that(com.hexhaki.piece.gear.ToonForce.tall(com.hexhaki.piece.gear.ToonForce.MOST) > 0.5
            && com.hexhaki.piece.gear.ToonForce.wide(-com.hexhaki.piece.gear.ToonForce.MOST) > 0.6, "nika: the body never folds to nothing");
    }

    /**
     * 3.3.0: Rubber World's ground (gear/RubberWave): only ever up and never past its most, nothing outside the field or before the
     * release, the rings going outward, the ground under the user's feet left still at the release, waves rolling while it lasts and
     * settled once it has ended.
     */
    static void rubberWorld() {
        double inf = Double.POSITIVE_INFINITY;
        double radius = 40;
        for (double t = -2; t < 140; t += 1.5) {
            for (double r = 0; r < 46; r += 0.7) {
                for (double a = -3; a < 3.2; a += 1.1) {
                    double h = com.hexhaki.piece.gear.RubberWave.rise(r, a, t, radius, inf);
                    Check.that(h >= 0 && h <= com.hexhaki.piece.gear.RubberWave.MOST, "nika: Rubber World's ground within 0 and its most (" + h + ")");
                    if (r > radius || t < 0) {
                        Check.that(h == 0, "nika: no rubber ground outside the field or before the release");
                    }
                }
            }
        }
        Check.that(com.hexhaki.piece.gear.RubberWave.rise(0.3, 0, 0.5, radius, inf) < 0.05, "nika: the user's own footing still at the release");
        double before = crest(6);
        double after = crest(14);
        Check.that(after > before + 1.5 && after < before + 3.5, "nika: Rubber World's first ring goes outward, slowly (3.3.3: " + before
            + " to " + after + ")");
        // 3.3.3 (the owner: "different sizes some much bigger than others, and slower"): the waves differ, a few much bigger
        double low = 9;
        double high = 0;
        for (int i = 0; i < 12; i++) {
            low = Math.min(low, com.hexhaki.piece.gear.RubberWave.waveHeight(i));
            high = Math.max(high, com.hexhaki.piece.gear.RubberWave.waveHeight(i));
        }
        Check.that(high > 2.5 * low && high > 1.2, "nika: Rubber World's waves of different sizes, some much bigger (" + low + " to " + high + ")");
        // 3.3.8 (the owner: "Rippes from world of rubber should be higher like lift more"; asked, "About 2x"): twice 3.3.3's 0.35 to 1.9
        Check.that(com.hexhaki.piece.gear.RubberWave.WAVE_HIGH > 3.5 && com.hexhaki.piece.gear.RubberWave.WAVE_LOW > 0.6
            && com.hexhaki.piece.gear.RubberWave.MOST >= com.hexhaki.piece.gear.RubberWave.WAVE_HIGH,
            "nika: Rubber World's waves twice as high as 3.3.3's, and room for the biggest");
        Check.that(com.hexhaki.piece.gear.RubberWave.SPEED_HIGH <= 0.2, "nika: Rubber World's waves roll slowly");
        double most = 0;
        for (double r = 10; r < 30; r += 0.25) {
            most = Math.max(most, com.hexhaki.piece.gear.RubberWave.rise(r, 0.4, 80, radius, inf));
        }
        Check.that(most > 0.3, "nika: the ground still rolling in waves while the field lasts (" + most + ")");
        double settled = 0;
        for (double r = 0; r < radius; r += 0.25) {
            settled = Math.max(settled, com.hexhaki.piece.gear.RubberWave.rise(r, 0.4, 100 + com.hexhaki.piece.gear.RubberWave.FADE + 0.01, radius, 100));
        }
        Check.that(settled == 0, "nika: the ground settled once the field has ended");
        Check.that(com.hexhaki.piece.client.gear.RubberWorldDraw.MOST_DRAWN <= 520, "nika: Rubber World draws at most 520 blocks a frame");
    }

    /** Where the release's first ring is highest along one way at {@code t}. */
    static double crest(double t) {
        double best = 0;
        double at = 0;
        for (double r = 0; r < 30; r += 0.1) {
            double h = com.hexhaki.piece.gear.RubberWave.rise(r, 0, t, 40, Double.POSITIVE_INFINITY);
            if (h > best) {
                best = h;
                at = r;
            }
        }
        return at;
    }

    /**
     * 3.2.9: Star Gun's struck body as rubber (gear/NikaBend): from rest and back to rest, never a jump between frames, stretched further
     * than the original's 2.35 at full strength, past its own shape into a squash after the let go, alive through the hold.
     */
    static void starRubber() {
        double[] a = new double[5];
        double[] b = new double[5];
        double hold = 26;
        double release = 17;
        double end = com.hexhaki.piece.gear.NikaBend.length(hold, release);
        com.hexhaki.piece.gear.NikaBend.shape(0, hold, release, 1, a);
        Check.that(Math.abs(a[0] - 1) + Math.abs(a[1] - 1) + Math.abs(a[2] - 1) + Math.abs(a[3]) + Math.abs(a[4]) < 1e-9, "nika: Star Gun's body starts at rest");
        Check.that(!com.hexhaki.piece.gear.NikaBend.shape(end + 0.01, hold, release, 1, a), "nika: Star Gun's bend ends");
        com.hexhaki.piece.gear.NikaBend.shape(end - 0.01, hold, release, 1, a);
        Check.that(Math.abs(a[2] - 1) < 0.02 && Math.abs(a[3]) < 0.02, "nika: Star Gun's body back at rest by the end (" + a[2] + ", " + a[3] + ")");
        double longest = 0;
        double shortest = 9;
        double leanMin = 9;
        double leanMax = -9;
        double holdMin = 9;
        double holdMax = 0;
        for (double t = 0; t < end; t += 0.05) {
            com.hexhaki.piece.gear.NikaBend.shape(t, hold, release, 1, a);
            com.hexhaki.piece.gear.NikaBend.shape(t + 0.05, hold, release, 1, b);
            double jump = Math.max(Math.abs(a[2] - b[2]), Math.max(Math.abs(a[3] - b[3]), Math.abs(a[4] - b[4])));
            Check.that(jump < (t < com.hexhaki.piece.gear.NikaBend.RAMP ? 0.2 : 0.06), "nika: Star Gun's body never jumps (" + t + ": " + jump + ")");
            Check.that(a[0] > 0.3 && a[0] < 2 && a[1] > 0.5 && a[1] < 1.6 && a[2] >= 0.42, "nika: Star Gun's body within bounds at " + t);
            longest = Math.max(longest, a[2]);
            if (t > com.hexhaki.piece.gear.NikaBend.RAMP + hold) {
                shortest = Math.min(shortest, a[2]);
            }
            if (t > com.hexhaki.piece.gear.NikaBend.RAMP + 8 && t < com.hexhaki.piece.gear.NikaBend.RAMP + hold) {
                holdMin = Math.min(holdMin, a[2]);
                holdMax = Math.max(holdMax, a[2]);
            }
            leanMin = Math.min(leanMin, a[3]);
            leanMax = Math.max(leanMax, a[3]);
        }
        Check.that(longest > 2.6, "nika: Star Gun stretches the body further than the original's 2.35 (" + longest + ")");
        Check.that(shortest < 0.7, "nika: let go, it springs back past its shape into a squash (" + shortest + ")");
        Check.that(holdMax - holdMin > 0.2, "nika: still quivering late in the hold (" + holdMin + " to " + holdMax + ")");
        Check.that(leanMin < -0.15 && leanMax > 0.6, "nika: the top left behind by the smack, then whipping forward (" + leanMin + ", " + leanMax + ")");
        com.hexhaki.piece.gear.NikaBend.shape(com.hexhaki.piece.gear.NikaBend.RAMP + 10, hold, release, 0.4, a);
        com.hexhaki.piece.gear.NikaBend.shape(com.hexhaki.piece.gear.NikaBend.RAMP + 10, hold, release, 1, b);
        Check.that(a[2] < b[2], "nika: a weaker blow (knockback resistance) stretches less");
    }

    /**
     * 3.2.8: the Giant's hold (gear/NikaHold): the fist is the middle of the right arm's box 8 pixels down it, the arm held straight out,
     * the model placed as PlayerRenderer places it (worked out here the long way, box corner by corner), for any turn and size; the
     * body flies in from where it stood, all the way by the clip's reach.
     */
    static void giantHold() {
        double[] fist = new double[3];
        for (double yaw : new double[] {0, 37, 90, -150, 270}) {
            for (double scale : new double[] {1, 2.5, 4}) {
                com.hexhaki.piece.gear.NikaHold.fist(3, 64, -7, yaw, scale, scale, fist);
                // the arm's box (pixels from its pivot -5, 2, 0): x -3 to 1, y -2 to 10, z -2 to 2; its middle line x -1, z 0
                double[] arm = model(-5 - 1, 2 + 8, 0, yaw, scale);
                double[] armRot = new double[3];
                // pitched -90: the pivot's (0, 8, 0) goes to (0, 0, -8)
                armRot[0] = -5 - 1;
                armRot[1] = 2;
                armRot[2] = -8;
                double[] out = model(armRot[0], armRot[1], armRot[2], yaw, scale);
                double gap = Math.abs(out[0] + 3 - fist[0]) + Math.abs(out[1] + 64 - fist[1]) + Math.abs(out[2] - 7 - fist[2]);
                Check.that(gap < 1e-9, "nika: the Giant's fist (" + yaw + ", x" + scale + ") is the arm's, held out (" + gap + ")");
                Check.that(Math.abs(arm[1] - out[1]) > 0.1 * scale, "nika: the arm held out raises the fist");
                // the fist to the body's right and in front of it
                double r = Math.toRadians(yaw);
                double ahead = -(fist[0] - 3) * Math.sin(r) + (fist[2] + 7) * Math.cos(r);
                double right = -(fist[0] - 3) * Math.cos(r) - (fist[2] + 7) * Math.sin(r);
                Check.that(ahead > 0.4 * scale && right > 0.3 * scale, "nika: the fist ahead and to the right (" + ahead + ", " + right + ")");
            }
        }
        Check.that(com.hexhaki.piece.gear.NikaHold.reach(0) == 0 && com.hexhaki.piece.gear.NikaHold.reach(com.hexhaki.piece.gear.NikaHold.REACH_FROM) == 0,
            "nika: a grabbed body waits for the arm");
        Check.that(com.hexhaki.piece.gear.NikaHold.reach(com.hexhaki.piece.gear.NikaHold.REACH) == 1
            && com.hexhaki.piece.gear.NikaHold.reach(1000) == 1, "nika: in the fist by the reach");
        for (double t = 0; t < 12; t += 0.25) {
            Check.that(com.hexhaki.piece.gear.NikaHold.reach(t + 0.25) >= com.hexhaki.piece.gear.NikaHold.reach(t), "nika: the fly-in never goes back");
        }
        Check.that(com.hexhaki.piece.server.GiantHold.HOLD >= 160 && com.hexhaki.piece.server.GiantHold.LET_GO < 180,
            "nika: the hold's kinds are the Nika fruit's (160 to 179, UoFx sends them to NikaHoldClient)");
    }

    /** A model point (pixels) to the world, relative to the feet: PlayerRenderer's 1.501 lift, 0.9375, flip, turn; then the giant's size. */
    static double[] model(double px, double py, double pz, double yaw, double scale) {
        double mx = px / 16.0;
        double my = py / 16.0 - 1.501;
        double mz = pz / 16.0;
        double fx = -0.9375 * mx;
        double fy = -0.9375 * my;
        double fz = 0.9375 * mz;
        double r = Math.toRadians(180 - yaw);
        return new double[] {scale * (fx * Math.cos(r) + fz * Math.sin(r)), scale * fy, scale * (-fx * Math.sin(r) + fz * Math.cos(r))};
    }

    /** Whether {@code clip} keys {@code part}'s pitch at {@code tick} to {@code value} (whatever the indentation). */
    static boolean keyed(String clip, int tick, String part, String value) {
        return java.util.regex.Pattern.compile("\"tick\":\\s*" + tick + ",\\s*\"easing\":\\s*\"inoutsine\",\\s*\"" + part
            + "\":\\s*\\{\\s*\"pitch\":\\s*" + java.util.regex.Pattern.quote(value) + "\\s*\\}").matcher(clip).find();
    }

    /** A number as the clip writer (Python's json) writes it: 70.0, -12.0. */
    static String fmt(double v) {
        return String.valueOf(Math.round(v * 1000.0) / 1000.0);
    }

    static void ranks() throws Exception {
        String lang = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/lang/en_us.json"));
        for (com.hexhaki.fruit.Fruit fruit : com.hexhaki.fruit.Fruit.values()) {
            com.hexhaki.piece.FruitRanks.Rank rank = com.hexhaki.piece.FruitRanks.of(fruit);
            if (fruit == com.hexhaki.fruit.Fruit.NONE) {
                Check.that(rank == null, "ranks: no fruit, no rank");
                continue;
            }
            Check.that(rank != null, "ranks: " + fruit + " has a rank");
            Check.that(lang.contains("\"item.hexhaki." + fruit.id + "_" + fruit.id + "_no_mi\": \"" + rank.code), "ranks: " + fruit + "'s name in its colour");
        }
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.NORO) == com.hexhaki.piece.FruitRanks.Rank.COMMON,
            "ranks: the Noro fruit Common (the owner: \"New common Fruits\")");
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.SUPA) == com.hexhaki.piece.FruitRanks.Rank.UNCOMMON,
            "ranks: the Supa fruit Uncommon (the owner: \"Supa Supa No Mi (UnCommon)\")");
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.ITO) == com.hexhaki.piece.FruitRanks.Rank.RARE,
            "ranks: the Ito fruit Rare (the owner: \"New Rare Fruit: Ito Ito No Mi (Rare)\")");
        // 3.3.7: the owner's Birdcage ("40 blocks, 45 s") and the keys they accepted (M the Awakening, two passives)
        Check.that(com.hexhaki.piece.server.ItoMoves.CAGE_RADIUS == 40.0 && com.hexhaki.piece.server.ItoMoves.CAGE_TICKS == 900,
            "the Birdcage: 40 blocks, 45 seconds");
        Check.that(com.hexhaki.piece.server.ItoKit.AWAKEN.key().equals("[M]") && com.hexhaki.piece.server.ItoKit.CAGE.key().equals("[N]")
            && com.hexhaki.piece.server.ItoKit.MOVES.size() == 11, "the Ito keys as accepted");
        Check.that(com.hexhaki.piece.client.FruitKeyN.ABILITY_M == 41 && com.hexhaki.piece.client.FruitKeyN.ABILITY_SKY == 42,
            "the M key and the air step reach the kit");
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.HORO) == com.hexhaki.piece.FruitRanks.Rank.COMMON,
            "ranks: the Horo fruit Common (the owner: \"Horo Horo No Mi (common)\")");
        // 3.3.6 (the owner: "make sure gloom is it's own custom effect ... miss often"; they chose 8 in 10 and 8 seconds)
        Check.that(com.hexhaki.piece.server.Gloom.MISS == 0.8 && com.hexhaki.piece.server.Gloom.TICKS == 160
            && com.hexhaki.piece.server.HoroMoves.DESPAIR_TICKS == com.hexhaki.piece.server.Gloom.TICKS
            && com.hexhaki.piece.server.HoroMoves.DOWN_TICKS <= 40, "Gloom: 8 in 10 attacks miss, 8 seconds, after a short fall");
        Check.that(lang.contains("\"effect.hexhaki.gloom\": \"Gloom\""), "Gloom: its name");
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.UO) == com.hexhaki.piece.FruitRanks.Rank.MYTHICAL
            && com.hexhaki.piece.FruitRanks.NIKA == com.hexhaki.piece.FruitRanks.Rank.MYTHICAL, "ranks: the Uo Uo and Nika fruits Mythical");
        Check.that(lang.contains("\"item.hexhaki.nika_fruit\": \"" + com.hexhaki.piece.FruitRanks.NIKA.code), "ranks: the Nika fruit's name in its colour");
        for (java.util.List<com.hexhaki.fruit.FruitMove> moves : java.util.List.of(com.hexhaki.piece.server.GuraKit.MOVES,
            com.hexhaki.piece.server.HieKit.MOVES, com.hexhaki.piece.server.MaguKit.MOVES, com.hexhaki.piece.server.NoroKit.MOVES,
            com.hexhaki.piece.server.PikaKit.MOVES, com.hexhaki.piece.server.SupaKit.MOVES, com.hexhaki.piece.server.HoroKit.MOVES,
            com.hexhaki.piece.server.ItoKit.MOVES,
            com.hexhaki.piece.server.UoKit.MOVES)) {
            for (com.hexhaki.fruit.FruitMove move : moves) {
                if (move.cooldown() != null) {
                    Check.that(com.hexhaki.piece.server.FruitEnergy.listed(move), "energy: " + move.name() + " has its cost listed");
                }
            }
        }
    }
}
