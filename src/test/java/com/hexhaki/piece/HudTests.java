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
        // 3.4.6 (the owner: Goof Ball "should be only usable in Gear 5 mode ... Same for Giant"): no Whip, no Giant untransformed
        java.util.Set<String> stretch = java.util.Set.of("PISTOL", "ROCKET_START", "ROCKET_RELEASE", "GATLING_START", "GATLING_RELEASE",
            "COIL_START", "COIL_RELEASE");
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
        goofAndDawnWhip();
        brokenBones();
        giantThrow();
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
        // 3.4.0 (the owner: "the fist goes through the body, stretching the skin to insane amounts and then causes a catastrophic
        // knockback that destroys all blocks in it's way"): the skin out of the body, held while the body is pinned, snapped back as it
        // is launched; through every block up to obsidian
        Check.that(com.hexhaki.piece.gear.NikaSkin.out(-0.1) == 0 && com.hexhaki.piece.gear.NikaSkin.out(com.hexhaki.piece.gear.NikaSkin.life()) == 0,
            "dawnPistol: no skin before the blow or after it snaps back");
        // 3.4.1 (the owner: "the skin streches to the limit slower and slower (the further it gets)"): fast at first, slowing, at its
        // limit when the body is launched
        double first = com.hexhaki.piece.gear.NikaSkin.out(1) - com.hexhaki.piece.gear.NikaSkin.out(0);
        double lastTick = com.hexhaki.piece.gear.NikaSkin.out(com.hexhaki.piece.gear.NikaSkin.PIN - 0.01)
            - com.hexhaki.piece.gear.NikaSkin.out(com.hexhaki.piece.gear.NikaSkin.PIN - 1);
        Check.that(first > 5 * lastTick && com.hexhaki.piece.gear.NikaSkin.out(com.hexhaki.piece.gear.NikaSkin.PIN - 0.01) > 0.98,
            "dawnPistol: the skin stretches slower and slower to its limit (" + first + " then " + lastTick + ")");
        Check.that(com.hexhaki.piece.server.NikaPistol.STRAIGHT >= 5 && com.hexhaki.piece.server.NikaPistol.LAUNCH_SPEED >= 4.5
            && com.hexhaki.piece.server.NikaPistol.LAUNCH_SPEED < 10, "dawnPistol: launched in a straight line, fast (and under the game's check)");
        Check.that(com.hexhaki.piece.gear.NikaSkin.out(com.hexhaki.piece.gear.NikaSkin.PIN + com.hexhaki.piece.gear.NikaSkin.SNAP * 0.6) < 0.3,
            "dawnPistol: snapped back as the body is launched");
        Check.that(com.hexhaki.piece.gear.NikaSkin.LENGTH >= 5.0, "dawnPistol: the skin stretched to insane amounts (" + com.hexhaki.piece.gear.NikaSkin.LENGTH + ")");
        Check.that(com.hexhaki.piece.server.NikaPistol.LAUNCH_HARDEST >= 50.0F, "dawnPistol: launched through every block up to obsidian");
        int[] skinQuads = new int[1];
        com.hexhaki.piece.horo.AstralArt.Skin skinCount = (xyz, uv, argb, nx, ny, nz) -> skinQuads[0]++;
        com.hexhaki.piece.gear.NikaSkin.skin(skinCount, 0, 1, 5, 0, 0, 1, com.hexhaki.piece.gear.NikaSkin.LENGTH, 0.4,
            com.hexhaki.piece.gear.NikaSkin.BULB, 0, 1, 0, 1, -1, 0);
        quads[0] = 0;
        com.hexhaki.piece.gear.NikaSkin.heat(count, 0, 1, 5, 0, 0, 1, com.hexhaki.piece.gear.NikaSkin.LENGTH, 0.4, com.hexhaki.piece.gear.NikaSkin.BULB,
            1.0, 0);
        Check.that(skinQuads[0] > 50 && skinQuads[0] + quads[0] < 1200, "dawnPistol: the stretched skin cheap (" + skinQuads[0] + " + " + quads[0] + ")");
    }

    /**
     * 3.4.4: Goof Ball and Dawn Whip (the owner's "Whip and Dawn Whip Rework"; "the Goof ball is only usable in transformed state"): the
     * clips key no head and last what the timelines say (gear/NikaGoof, gear/NikaDawn, server/NikaWhip run them); Dawn Whip's yaw has
     * the leg along the look at the impact and lands on whole turns where it hands over; their own message kinds (none of the Nika
     * fruit's others); the leg's stretch sent so it is out at the impact; the art cheap (one user's busiest moment under 16,000 quads)
     * and gone after its life; only the limbs' faces turned to the eye drawn.
     */
    static void goofAndDawnWhip() {
        for (String name : new String[] {"gear5_goof_spin", "gear5_goof_dizzy", "gear5_dawn_whip", "gear5_dawn_pop"}) {
            String text = clipText(name);
            Check.that(!text.contains("\"head\""), "goofWhip: " + name + " keys no head");
            Check.that(com.hexhaki.piece.gear.ToonForce.score(name) != null, "goofWhip: " + name + " has Toon Force beats");
        }
        Check.that(clipText("gear5_goof_spin").contains("\"endTick\":" + (com.hexhaki.piece.gear.NikaGoof.SPIN + 4) + ",")
            && clipText("gear5_goof_spin").contains("\"returnTick\":" + com.hexhaki.piece.gear.NikaGoof.SPIN + ","),
            "goofWhip: the spin-up loops from the ball's tick");
        Check.that(clipText("gear5_dawn_whip").contains("\"endTick\":" + (com.hexhaki.piece.gear.NikaDawn.POP + 1) + ","),
            "goofWhip: Dawn Whip's clip runs to the pop");
        Check.that(clipText("gear5_dawn_pop").contains("\"endTick\":" + com.hexhaki.piece.gear.NikaDawn.POP_CLIP + ","),
            "goofWhip: the pop's clip");
        // the body's yaw at the impact: the leg along the look (a whole number of turns), and whole turns where the clip hands over
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{\"tick\":(\\d+),\"easing\":\"\\w+\",\"body\":\\{\"yaw\":(-?[0-9.]+)\\}\\}")
            .matcher(clipText("gear5_dawn_whip"));
        java.util.Map<Integer, Double> yaws = new java.util.HashMap<>();
        while (m.find()) {
            yaws.put(Integer.parseInt(m.group(1)), Double.parseDouble(m.group(2)));
        }
        Double impact = yaws.get(com.hexhaki.piece.gear.NikaDawn.IMPACT);
        Check.that(impact != null && Math.abs(impact % 360.0) < 1.0E-6 && impact > 0, "goofWhip: the leg along the look at the impact (" + impact + ")");
        Double last = yaws.get(com.hexhaki.piece.gear.NikaDawn.POP);
        Check.that(last != null && Math.abs(last % 360.0) < 1.0E-6, "goofWhip: whole turns where the clip hands over (" + last + ")");
        Double wound = yaws.get(com.hexhaki.piece.gear.NikaDawn.WIND);
        Check.that(wound != null && wound < -250, "goofWhip: wound up the wrong way first (" + wound + ")");
        // 3.4.6 (the owner: "I don't see the leg in your render"): the leg drawn by gear/NikaDawn.leg turns as the clip turns the body
        // (its yaw table is the clip's at every key from 10 to 28), is at its full reach at the impact, and back in after
        for (int k = 0; k < com.hexhaki.piece.gear.NikaDawn.YAW_TICKS.length; k++) {
            int tick = (int) com.hexhaki.piece.gear.NikaDawn.YAW_TICKS[k];
            Double keyed = yaws.get(tick);
            Check.that(keyed != null && Math.abs(keyed - com.hexhaki.piece.gear.NikaDawn.YAW[k]) < 1.0E-6,
                "goofWhip: the leg's turn at " + tick + " is the clip's (" + keyed + ", " + com.hexhaki.piece.gear.NikaDawn.YAW[k] + ")");
        }
        Check.that(Math.abs(com.hexhaki.piece.gear.NikaDawn.legLength(com.hexhaki.piece.gear.NikaDawn.IMPACT) - com.hexhaki.piece.gear.NikaDawn.REACH) < 1.0E-9
            && com.hexhaki.piece.gear.NikaDawn.legLength(com.hexhaki.piece.gear.NikaDawn.LEG_OUT - 0.5) == 0.0
            && com.hexhaki.piece.gear.NikaDawn.legLength(com.hexhaki.piece.gear.NikaDawn.LEG_BACK + 0.5) == 0.0,
            "goofWhip: the leg at its full reach at the impact, not out before or after");
        int[] legQuads = new int[1];
        com.hexhaki.piece.gear.NikaDawn.leg((xyz, uv, argb, nx, ny, nz) -> legQuads[0]++, new com.hexhaki.piece.style.StyleShow.Place(0.3, -1.6,
            4.0, 0.4), com.hexhaki.piece.gear.NikaDawn.IMPACT, false, com.hexhaki.piece.gear.NikaDawn.COAT);
        Check.that(legQuads[0] > 12 && legQuads[0] < 80, "goofWhip: the leg drawn, cheaply (" + legQuads[0] + " quads)");
        Check.that(com.hexhaki.piece.gear.NikaDawn.WIND < com.hexhaki.piece.gear.NikaDawn.IMPACT
            && com.hexhaki.piece.gear.NikaDawn.IMPACT < com.hexhaki.piece.gear.NikaDawn.OVERSPIN
            && com.hexhaki.piece.gear.NikaDawn.OVERSPIN + com.hexhaki.piece.gear.NikaDawn.DEPTH * com.hexhaki.piece.gear.NikaDawn.DRILL_STEP
                <= com.hexhaki.piece.gear.NikaDawn.DRILL
            && com.hexhaki.piece.gear.NikaDawn.DRILL < com.hexhaki.piece.gear.NikaDawn.POP, "goofWhip: the six phases in order, the shaft dug before the drill ends");
        int[] kinds = {com.hexhaki.piece.server.NikaWhip.GOOF, com.hexhaki.piece.server.NikaWhip.GOOF_HIT, com.hexhaki.piece.server.NikaWhip.GOOF_POP,
            com.hexhaki.piece.server.NikaWhip.DAWN, com.hexhaki.piece.server.NikaWhip.DAWN_HIT, com.hexhaki.piece.server.NikaWhip.DAWN_DRILL,
            com.hexhaki.piece.server.NikaWhip.DAWN_POP, com.hexhaki.piece.server.NikaWhip.STOP};
        for (int i = 0; i < kinds.length; i++) {
            int k = kinds[i];
            Check.that(k == 165 + i, "goofWhip: kind " + k + " in order from 165");
            Check.that(k != com.hexhaki.piece.server.NikaPistol.POW && k != com.hexhaki.piece.server.NikaJump.HOP
                && k != com.hexhaki.piece.server.NikaJump.LAND && k != com.hexhaki.piece.server.GiantHold.HOLD
                && k != com.hexhaki.piece.server.GiantHold.LET_GO && k < 180, "goofWhip: kind " + k + " is its own");
        }
        // the busiest moment: one ball with its limbs, three bursts, a pop and the stars, then Dawn Whip's every effect at its height
        int[] quads = new int[1];
        com.hexhaki.piece.dragon.FireMesh.Sink count = (xyz, argb) -> quads[0]++;
        int[] skins = new int[1];
        boolean[] outward = {true};
        com.hexhaki.piece.horo.AstralArt.Skin skin = (xyz, uv, argb, nx, ny, nz) -> {
            skins[0]++;
            // only the faces turned to the eye (the phone culls nothing: a far face would show through)
            double cx = (xyz[0] + xyz[6]) * 0.5;
            double cy = (xyz[1] + xyz[7]) * 0.5;
            double cz = (xyz[2] + xyz[8]) * 0.5;
            if (nx * cx + ny * cy + nz * cz > 1.0E-9) {
                outward[0] = false;
            }
        };
        int most = 0;
        for (double age = 0; age <= 30; age += 0.5) {
            quads[0] = 0;
            com.hexhaki.piece.gear.NikaGoof.ball(count, 0.4, 0.9, 3.0, age, 7);
            com.hexhaki.piece.gear.NikaGoof.limbs(skin, 0.4, 0.9, 3.0, age, 7, false);
            com.hexhaki.piece.gear.NikaGoof.burst(count, 1.0, 1.0, 3.0, age % 6, 0.32, 3);
            com.hexhaki.piece.gear.NikaGoof.burst(count, -1.0, 1.0, 3.0, (age + 2) % 6, 0.32, 4);
            com.hexhaki.piece.gear.NikaGoof.pop(count, 3.0, 0.9, 3.0, age % 10, 5);
            com.hexhaki.piece.gear.NikaGoof.dizzy(count, 3.0, 2.0, 3.0, age, com.hexhaki.piece.gear.NikaGoof.DIZZY, 5);
            most = Math.max(most, quads[0]);
        }
        Check.that(most > 1000 && most < 16000, "goofWhip: Goof Ball drawn, under 16,000 quads (" + most + ")");
        Check.that(skins[0] > 0 && outward[0], "goofWhip: the limbs' skin faces drawn, only those turned to the eye");
        most = 0;
        for (double age = 0; age <= com.hexhaki.piece.gear.NikaDawn.POP; age += 0.5) {
            quads[0] = 0;
            double t = age - com.hexhaki.piece.gear.NikaDawn.WIND;
            com.hexhaki.piece.gear.NikaDawn.coil(count, 0.4, -1.0, 3.0, age, 5);
            com.hexhaki.piece.gear.NikaDawn.smear(count, new com.hexhaki.piece.style.StyleShow.Place(0.4, -1.0, 3.0, 0.3), age, 5);
            for (int k = 0; k < 4; k++) {
                com.hexhaki.piece.gear.NikaDawn.crack(count, 3.0 + k, 0, 6.0, 1, 0, age - com.hexhaki.piece.gear.NikaDawn.IMPACT, 1.8, k);
            }
            com.hexhaki.piece.gear.NikaDawn.whirl(count, 0.4, -1.0, 3.0, age - com.hexhaki.piece.gear.NikaDawn.IMPACT, 0.5, 5);
            com.hexhaki.piece.gear.NikaDawn.drill(count, 0.4, -1.0, 3.0, age - com.hexhaki.piece.gear.NikaDawn.OVERSPIN, true, 5);
            com.hexhaki.piece.gear.NikaDawn.pop(count, 0.4, -1.0, 3.0, age - com.hexhaki.piece.gear.NikaDawn.POP + 6, 5);
            most = Math.max(most, quads[0]);
        }
        Check.that(most > 300 && most < 16000, "goofWhip: Dawn Whip drawn, under 16,000 quads (" + most + ")");
        quads[0] = 0;
        com.hexhaki.piece.gear.NikaGoof.pop(count, 0, 0.9, 3.0, com.hexhaki.piece.gear.NikaGoof.POP_LIFE + 0.5, 5);
        com.hexhaki.piece.gear.NikaDawn.crack(count, 0, 0.9, 3.0, 1, 0, com.hexhaki.piece.gear.NikaDawn.CRACK_LIFE + 0.5, 1.8, 5);
        com.hexhaki.piece.gear.NikaDawn.pop(count, 0, 0.9, 3.0, com.hexhaki.piece.gear.NikaDawn.POP_LIFE + 0.5, 5);
        com.hexhaki.piece.gear.NikaGoof.dizzy(count, 0, 2.0, 3.0, com.hexhaki.piece.gear.NikaGoof.DIZZY + 1, com.hexhaki.piece.gear.NikaGoof.DIZZY, 5);
        Check.that(quads[0] == 0, "goofWhip: every effect gone after its life");
        Check.that(com.hexhaki.piece.gear.NikaGoof.reach(0) < 0.01 && com.hexhaki.piece.gear.NikaGoof.reach(0.4) > 0.99
            && com.hexhaki.piece.gear.NikaGoof.reach(1.0) < 0.01, "goofWhip: a limb punches out and comes back in");
    }

    /**
     * 3.4.5: Broken Bones (the owner: "slowness 1 and randomly fall into ragdoll at random times as long as the effect is active"; asked,
     * 20 s and a fall every 3 to 6 s): its numbers, its own X-ray message kind (none of the Gura fruit's others), the GIF's timing (12
     * frames of 240 ms, played once), its name.
     */
    static void brokenBones() {
        Check.that(com.hexhaki.piece.server.BrokenBones.TICKS == 400, "brokenBones: 20 seconds");
        Check.that(com.hexhaki.piece.server.BrokenBones.LEAST == 60 && com.hexhaki.piece.server.BrokenBones.LEAST
            + com.hexhaki.piece.server.BrokenBones.SPREAD == 120, "brokenBones: a fall every 3 to 6 seconds");
        Check.that(com.hexhaki.piece.server.BrokenBones.FALL > 0 && com.hexhaki.piece.server.BrokenBones.FALL < 60,
            "brokenBones: each fall short (" + com.hexhaki.piece.server.BrokenBones.FALL + " ticks)");
        int xray = com.hexhaki.piece.server.BrokenBones.XRAY;
        int[] gura = {com.hexhaki.piece.server.GuraMoves.CRACK, com.hexhaki.piece.server.GuraMoves.BUBBLE_ON, com.hexhaki.piece.server.GuraMoves.BUBBLE_OFF,
            com.hexhaki.piece.server.GuraMoves.SHOCK, com.hexhaki.piece.server.GuraMoves.QUAKE, com.hexhaki.piece.server.GuraMoves.SHAKE_ON,
            com.hexhaki.piece.server.GuraMoves.SHAKE_OFF, com.hexhaki.piece.server.GuraMoves.RIFT, com.hexhaki.piece.server.GuraMoves.TSUNAMI,
            com.hexhaki.piece.server.GuraMoves.LEAP, com.hexhaki.piece.server.GuraMoves.DASH, com.hexhaki.piece.server.GuraMoves.TREMOR,
            com.hexhaki.piece.server.GuraMoves.HEAVE};
        for (int k : gura) {
            Check.that(k != xray, "brokenBones: the X-ray's kind is its own (" + k + ")");
        }
        Check.that(xray >= 60 && xray < 80, "brokenBones: the X-ray in the Gura fruit's kinds");
        Check.that(text("src/main/resources/assets/hexhaki/lang/en_us.json").contains("\"effect.hexhaki.broken_bones\": \"Broken Bones\""),
            "brokenBones: its name");
        Check.that(text("src/main/java/com/hexhaki/piece/client/gura/BonesOverlay.java").contains("FRAMES = 12;")
            && text("src/main/java/com/hexhaki/piece/client/gura/BonesOverlay.java").contains("FRAME_MS = 240L;"),
            "brokenBones: the GIF's own timing (12 frames of 240 ms)");
    }

    /**
     * 3.4.7 (the owner: the Giant's throw "on a ... horizontal line, similar to the others ... our own custom effect whenever the entity
     * crashes against something"): the flight flat and straight (well under the game's movement check), its own message kinds (none of
     * the Nika fruit's others), the trail and the crash cheap and gone after their life, the patch row in place.
     */
    static void giantThrow() {
        Check.that(com.hexhaki.piece.server.GiantThrow.SPEED > 2.0 && com.hexhaki.piece.server.GiantThrow.SPEED < 10.0
            && com.hexhaki.piece.server.GiantThrow.STRAIGHT >= 8, "giantThrow: a fast, long, straight flight");
        int fly = com.hexhaki.piece.server.GiantThrow.FLY;
        int crash = com.hexhaki.piece.server.GiantThrow.CRASH;
        Check.that(fly == 173 && crash == 174 && fly > com.hexhaki.piece.server.NikaWhip.STOP, "giantThrow: its own kinds after Dawn Whip's");
        int[] quads = new int[1];
        com.hexhaki.piece.dragon.FireMesh.Sink count = (xyz, argb) -> quads[0]++;
        int most = 0;
        for (double age = 0; age <= 16; age += 0.5) {
            quads[0] = 0;
            com.hexhaki.piece.gear.NikaThrow.trail(count, 0.3, 4.0, 6.0, 0, 1, age, 1.9, 5);
            com.hexhaki.piece.gear.NikaThrow.crash(count, 0.3, 1.0, 6.0, 0, 1, age, 1.9, 5);
            most = Math.max(most, quads[0]);
        }
        Check.that(most > 200 && most < 6000, "giantThrow: the trail and crash drawn, cheaply (" + most + ")");
        quads[0] = 0;
        com.hexhaki.piece.gear.NikaThrow.trail(count, 0.3, 4.0, 6.0, 0, 1, com.hexhaki.piece.gear.NikaThrow.TRAIL_LIFE + 0.5, 1.9, 5);
        com.hexhaki.piece.gear.NikaThrow.crash(count, 0.3, 1.0, 6.0, 0, 1, com.hexhaki.piece.gear.NikaThrow.CRASH_LIFE + 0.5, 1.9, 5);
        Check.that(quads[0] == 0, "giantThrow: gone after their life");
        Check.that(text("patch/PiecePatch.java").contains("\"com/hexhaki/piece/server/GiantThrow\", \"integer\", 2}"),
            "giantThrow: GiantGrab.release's two GearData.integer calls go through GiantThrow");
    }

    static String text(String path) {
        try {
            return java.nio.file.Files.readString(java.nio.file.Path.of(path));
        } catch (java.io.IOException failure) {
            throw new AssertionError(failure);
        }
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
        // 3.4.9 (the owner, on the giant: "every ground rippling, everything should just be the same except the player's much bigger"):
        // a normal body's ripple is exactly as before; the giant's (4x) reaches 4 times as far round, rises about twice as high, lasts twice
        // as long, and is 0 outside its reach and life
        java.util.Random random = new java.util.Random(7);
        for (int i = 0; i < 2000; i++) {
            double r = random.nextDouble() * 7;
            double a = random.nextDouble() * 6.3;
            double t = random.nextDouble() * 20 - 1;
            boolean landing = random.nextBoolean();
            Check.that(com.hexhaki.piece.gear.RubberJump.hop(r, a, t, landing, 1.0) == com.hexhaki.piece.gear.RubberJump.hop(r, a, t, landing),
                "rubberJump: a normal body's ripple is unchanged");
        }
        for (boolean landing : new boolean[] {false, true}) {
            double most = 0;
            double far = 0;
            double life = com.hexhaki.piece.gear.RubberJump.life(4.0);
            double radius = (landing ? com.hexhaki.piece.gear.RubberJump.LAND_RADIUS : com.hexhaki.piece.gear.RubberJump.HOP_RADIUS) * 4;
            for (double t = -1; t <= life + 2; t += 0.5) {
                for (double r = 0; r < radius + 3; r += 0.5) {
                    for (double a = 0; a < 6.3; a += 0.9) {
                        double h = com.hexhaki.piece.gear.RubberJump.hop(r, a, t, landing, 4.0);
                        most = Math.max(most, h);
                        if (h > 0.05) {
                            far = Math.max(far, r);
                        }
                        if (t > life || t < 0 || r > radius) {
                            Check.that(h == 0, "rubberJump: a giant's ripple is still outside its life and reach (" + h + ")");
                        }
                    }
                }
            }
            Check.that(most > 1.6 && most < 4.8, "rubberJump: a giant's ripple rises about twice as high (" + most + ")");
            Check.that(far > radius * 0.6, "rubberJump: a giant's ripple runs out about 4 times as far (" + far + " of " + radius + ")");
        }
        Check.that(com.hexhaki.piece.gear.RubberJump.life(4.0) == 2 * com.hexhaki.piece.gear.RubberJump.LIFE, "rubberJump: a giant's lasts twice as long");
        // 3.4.9: Gear 5's smoke (the owner: the white outline replaced "with like some kind of like photon smoke"): every wisp starts on the
        // body (within its box, a little out), rises, grows, and the giant's are its size; a modest number a tick
        double[] w = new double[com.hexhaki.piece.gear.NikaSmoke.FIELDS];
        for (double scale : new double[] {1.0, 4.0}) {
            for (int i = 0; i < 500; i++) {
                com.hexhaki.piece.gear.NikaSmoke.spawn(scale, random.nextDouble() * 360 - 180, random, w);
                double out = Math.hypot(w[com.hexhaki.piece.gear.NikaSmoke.X], w[com.hexhaki.piece.gear.NikaSmoke.Z]);
                Check.that(out <= 0.5 * scale && w[com.hexhaki.piece.gear.NikaSmoke.Y] >= 0.15 * scale && w[com.hexhaki.piece.gear.NikaSmoke.Y] <= 1.85 * scale,
                    "nikaSmoke: a wisp starts on the body (" + out + ", " + w[com.hexhaki.piece.gear.NikaSmoke.Y] + ")");
                Check.that(w[com.hexhaki.piece.gear.NikaSmoke.VY] > 0, "nikaSmoke: smoke rises");
                Check.that(w[com.hexhaki.piece.gear.NikaSmoke.SIZE1] > w[com.hexhaki.piece.gear.NikaSmoke.SIZE0], "nikaSmoke: a wisp grows");
                Check.that(w[com.hexhaki.piece.gear.NikaSmoke.LIFETIME] >= 10 && w[com.hexhaki.piece.gear.NikaSmoke.LIFETIME] <= 40,
                    "nikaSmoke: a wisp lives under two seconds");
                Check.that(w[com.hexhaki.piece.gear.NikaSmoke.SIZE1] <= 1.5 * scale, "nikaSmoke: a wisp is the body's size at most");
            }
        }
        Check.that(com.hexhaki.piece.gear.NikaSmoke.rate(1.0, 1.0) * (com.hexhaki.piece.gear.NikaSmoke.LIFE + com.hexhaki.piece.gear.NikaSmoke.LIFE_SPREAD) < 120
            && com.hexhaki.piece.gear.NikaSmoke.rate(4.0, 1.0) * (com.hexhaki.piece.gear.NikaSmoke.LIFE + com.hexhaki.piece.gear.NikaSmoke.LIFE_SPREAD) < 200,
            "nikaSmoke: a body keeps at most a couple of hundred wisps alive");
        Check.that(com.hexhaki.piece.gear.NikaSmoke.rate(1.0, 0.0) == 0, "nikaSmoke: none while the transformation's presence is gone");
        Check.that((com.hexhaki.piece.gear.NikaSmoke.FROM >>> 24) < 0xC0, "nikaSmoke: see-through smoke");
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
        Check.that(com.hexhaki.piece.server.ItoKit.AWAKEN.key().equals("[M]") && com.hexhaki.piece.server.ItoKit.CAGE.key().equals("[N] hold")
            && com.hexhaki.piece.server.ItoKit.OVERHEAT.key().equals("[X] hold")
            && com.hexhaki.piece.server.ItoKit.MOVES.size() == 11, "the Ito keys as accepted");
        Check.that(com.hexhaki.piece.client.FruitKeyN.ABILITY_M == 41 && com.hexhaki.piece.client.FruitKeyN.ABILITY_SKY == 42,
            "the M key and the air step reach the kit");
        skyRoad();
        birdcage();
        mochi();
        ironFist();
        impactSprite();
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
            com.hexhaki.piece.server.ItoKit.MOVES, com.hexhaki.piece.server.MochiKit.ALL,
            com.hexhaki.piece.server.UoKit.MOVES)) {
            for (com.hexhaki.fruit.FruitMove move : moves) {
                if (move.cooldown() != null) {
                    Check.that(com.hexhaki.piece.server.FruitEnergy.listed(move), "energy: " + move.name() + " has its cost listed");
                }
            }
        }
    }

    /**
     * 3.5.5 (the owner: "research katakuris mochi fruit and add it ... Legendary fruit"; their answers: the Awakening on M a 30 s mode
     * whose Z X C V B are its own five techniques, the dodge a three-second window on a double tap of sneak with a 20 s cooldown).
     */
    /** 3.5.8: Iron Fist remade (the owner's seven moves, their keys, needs and beats). */
    static void ironFist() {
        com.hexhaki.piece.ironfist.IronMove[] panel = com.hexhaki.piece.ironfist.IronMove.PANEL;
        String[] keys = {"LMB", "RMB", "Z hold", "X", "C", "V", "B", "N"};
        Check.that(panel.length == keys.length, "Iron Fist: the punches and the seven moves");
        for (int i = 0; i < keys.length; i++) {
            Check.that(panel[i].key().equals(keys[i]), "Iron Fist: " + panel[i].display() + " on " + keys[i] + " (as the owner chose)");
        }
        com.hexhaki.piece.ironfist.IronMove.Need none = com.hexhaki.piece.ironfist.IronMove.Need.NONE;
        com.hexhaki.piece.ironfist.IronMove.Need ryou = com.hexhaki.piece.ironfist.IronMove.Need.RYOU;
        Check.that(panel[0].need() == none && panel[1].need() == none && panel[2].need() == none, "Iron Fist: the punches, Fist of Love and "
            + "Meteor Fist need nothing");
        Check.that(com.hexhaki.piece.ironfist.IronMove.CRUSHER.need() == com.hexhaki.piece.ironfist.IronMove.Need.ARMAMENT,
            "Mountain Crusher needs Armament (or Ryou)");
        Check.that(com.hexhaki.piece.ironfist.IronMove.BLUE_HOLE.need() == ryou && com.hexhaki.piece.ironfist.IronMove.DIVIDE.need() == ryou
            && com.hexhaki.piece.ironfist.IronMove.INFINITUM.need() == ryou, "Blue Hole, Galaxy Divide and Infinitum Explosion need Ryou");
        Check.that(com.hexhaki.piece.ironfist.IronMove.IMPACT.need() == com.hexhaki.piece.ironfist.IronMove.Need.CONQUEROR_RYOU
            && com.hexhaki.piece.ironfist.IronMove.IMPACT.cooldown() == 3600, "Galaxy Impact: Conqueror's and Ryou, three minutes' cooldown");
        Check.that(com.hexhaki.piece.ironfist.IronMove.IMPACT_ENERGY > 0.0 && com.hexhaki.piece.ironfist.IronMove.IMPACT_ENERGY <= 1.0,
            "Galaxy Impact takes a share of the Haki energy");
        Check.that(com.hexhaki.piece.ironfist.IronMove.DIVIDE_LENGTH == 40.0 && com.hexhaki.piece.ironfist.IronMove.DIVIDE_HEIGHT == 20.0
            && com.hexhaki.piece.ironfist.IronMove.DIVIDE_WIDTH == 5.0, "Galaxy Divide: 40 forward, 20 up, 5 across (the owner's numbers)");
        // 3.6.1 (the owner: speed and blast "Fixed too", "Full power always"): every throw is the full one, however long Z is held
        for (int held = 0; held <= com.hexhaki.piece.ironfist.IronMove.METEOR_MOST; held++) {
            Check.that(com.hexhaki.piece.ironfist.IronMove.meteorCharge(held) == 1.0, "Meteor Fist: full power at any hold (" + held + ")");
        }
        Check.that(com.hexhaki.piece.ironfist.IronMove.meteorSpeed(1.0) == 4.2 && com.hexhaki.piece.ironfist.IronMove.meteorBlast(1.0) == 6.0,
            "Meteor Fist: the full hold's speed and 6-block blast");
        // 3.5.9: Meteor Fist's cannonball one size (the owner: "should not fucking increase size")
        Check.that(com.hexhaki.piece.ironfist.IronMove.METEOR_RADIUS > 0.3 && com.hexhaki.piece.ironfist.IronMove.METEOR_RADIUS < 1.0,
            "Meteor Fist: one cannonball size");
        // 3.5.9 Blue Hole: an uppercut at arm's reach; a hit flies up and slows to the top, the user leaps after it and grabs it there,
        // throws it down faster and faster, and comes down after it, all within the clip
        Check.that(com.hexhaki.piece.ironfist.IronMove.HOLE_REACH > 2.5
            && com.hexhaki.piece.ironfist.IronMove.HOLE_REACH < 5.0, "Blue Hole: an uppercut at arm's reach");
        Check.that(com.hexhaki.piece.ironfist.IronMove.holeRise(0) == 0.0
            && com.hexhaki.piece.ironfist.IronMove.holeRise(com.hexhaki.piece.ironfist.IronMove.HOLE_RISE) == com.hexhaki.piece.ironfist.IronMove.HOLE_HEIGHT,
            "Blue Hole: the body flies up to its height");
        for (int t = 1; t <= com.hexhaki.piece.ironfist.IronMove.HOLE_RISE; t++) {
            double rise = com.hexhaki.piece.ironfist.IronMove.holeRise(t) - com.hexhaki.piece.ironfist.IronMove.holeRise(t - 1);
            double before = t < 2 ? Double.MAX_VALUE : com.hexhaki.piece.ironfist.IronMove.holeRise(t - 1) - com.hexhaki.piece.ironfist.IronMove.holeRise(t - 2);
            Check.that(rise > 0.0 && rise <= before, "Blue Hole: the body rises and slows to the top at " + t);
        }
        Check.that(com.hexhaki.piece.ironfist.IronMove.holeThrow(0) == 0.0
            && com.hexhaki.piece.ironfist.IronMove.holeThrow(com.hexhaki.piece.ironfist.IronMove.HOLE_FALL) == 1.0
            && com.hexhaki.piece.ironfist.IronMove.holeThrow(1) < 0.5, "Blue Hole: thrown down faster and faster");
        Check.that(com.hexhaki.piece.ironfist.IronMove.HOLE_JUMP + com.hexhaki.piece.ironfist.IronMove.HOLE_LEAP == com.hexhaki.piece.ironfist.IronMove.HOLE_RISE,
            "Blue Hole: the user reaches the top as the body does");
        Check.that(com.hexhaki.piece.ironfist.IronMove.HOLE_HIT + com.hexhaki.piece.ironfist.IronMove.HOLE_RISE + com.hexhaki.piece.ironfist.IronMove.HOLE_HANG
            + 1 + com.hexhaki.piece.ironfist.IronMove.HOLE_DROP <= com.hexhaki.piece.ironfist.IronMove.HOLE_TICKS, "Blue Hole: the user is down before the clip ends");
        Check.that(com.hexhaki.piece.ironfist.IronMove.HOLE_MISS_COOLDOWN < com.hexhaki.piece.ironfist.IronMove.BLUE_HOLE.cooldown(),
            "Blue Hole: a miss ends it with a short cooldown");
        int n = com.hexhaki.piece.ironfist.IronMove.barrageCount();
        Check.that(n >= 18, "Infinitum Explosion: a barrage (" + n + " punches)");
        for (int k = 1; k < n; k++) {
            Check.that(com.hexhaki.piece.ironfist.IronMove.barrageTick(k) >= com.hexhaki.piece.ironfist.IronMove.barrageTick(k - 1)
                && com.hexhaki.piece.ironfist.IronMove.barrageTick(k) < com.hexhaki.piece.ironfist.IronMove.INF_END,
                "the barrage's punches come in order before its end");
        }
        Check.that(com.hexhaki.piece.ironfist.IronMove.INF_END < com.hexhaki.piece.ironfist.IronMove.INF_FINISH
            && com.hexhaki.piece.ironfist.IronMove.INF_FINISH < com.hexhaki.piece.ironfist.IronMove.INF_TICKS, "the barrage, then the last punch");
        double top = com.hexhaki.piece.ironfist.IronMove.IMPACT_HEIGHT;
        Check.that(com.hexhaki.piece.ironfist.IronMove.impactRise(0) == 0.0
            && com.hexhaki.piece.ironfist.IronMove.impactRise(com.hexhaki.piece.ironfist.IronMove.IMPACT_TOP) == top
            && com.hexhaki.piece.ironfist.IronMove.impactRise(com.hexhaki.piece.ironfist.IronMove.IMPACT_PUNCH) == top
            && com.hexhaki.piece.ironfist.IronMove.impactRise(com.hexhaki.piece.ironfist.IronMove.IMPACT_TICKS) == 0.0,
            "Galaxy Impact: up, held in the sky through the punch, back down");
        double last = 0.0;
        for (int t = 0; t <= com.hexhaki.piece.ironfist.IronMove.IMPACT_TICKS; t++) {
            double h = com.hexhaki.piece.ironfist.IronMove.impactRise(t);
            Check.that(h >= 0.0 && h <= top && Math.abs(h - last) < 6.0, "Galaxy Impact's flight is smooth at " + t);
            last = h;
        }
        Check.that(com.hexhaki.piece.ironfist.IronMove.impactCharge(com.hexhaki.piece.ironfist.IronMove.IMPACT_TOP) == 0.0
            && com.hexhaki.piece.ironfist.IronMove.impactCharge(com.hexhaki.piece.ironfist.IronMove.IMPACT_SILENCE) == 1.0,
            "Galaxy Impact's fist fills while it hangs");
        Check.that(com.hexhaki.piece.ironfist.IronMove.impactLanding(top) < com.hexhaki.piece.ironfist.IronMove.IMPACT_DESCEND,
            "Galaxy Impact's force lands before the body comes down");
    }

    /** 3.5.8: the owner's impact frame: plays once, its veil falls with the white flash and lifts, it fits the screen. */
    static void impactSprite() {
        double length = com.hexhaki.piece.clash.ImpactSprite.length();
        Check.that(length > 15.0 && length < 40.0, "the impact frame plays about a second (" + length + " ticks)");
        Check.that(com.hexhaki.piece.clash.ImpactSprite.frame(0.0) == 0 && com.hexhaki.piece.clash.ImpactSprite.frame(length + 0.01) == -1,
            "the impact frame plays from its first frame once");
        int prev = 0;
        for (double age = 0.0; age < length; age += 0.05) {
            int f = com.hexhaki.piece.clash.ImpactSprite.frame(age);
            Check.that(f >= prev && f < com.hexhaki.piece.clash.ImpactSprite.FRAMES, "the impact frame's frames in order");
            Check.that(com.hexhaki.piece.clash.ImpactSprite.u(f) + com.hexhaki.piece.clash.ImpactSprite.CELL <= com.hexhaki.piece.clash.ImpactSprite.SHEET
                && com.hexhaki.piece.clash.ImpactSprite.v(f) + com.hexhaki.piece.clash.ImpactSprite.CELL <= com.hexhaki.piece.clash.ImpactSprite.SHEET,
                "the impact frame's frame " + f + " is on the sheet");
            prev = f;
        }
        double flash = com.hexhaki.piece.clash.ImpactSprite.FLASH / com.hexhaki.piece.clash.ImpactSprite.PER_SECOND * 20.0;
        for (float power = 1.0F; power <= 5.0F; power += 1.0F) {
            Check.that(com.hexhaki.piece.clash.ImpactSprite.veil(flash - 0.1, power) == 0.0
                && com.hexhaki.piece.clash.ImpactSprite.veil(flash, power) > 0.3
                && com.hexhaki.piece.clash.ImpactSprite.veil(flash + 8.0, power) == 0.0, "the veil falls with the flash and lifts");
            int side = com.hexhaki.piece.clash.ImpactSprite.side(power, 1920, 1080);
            Check.that(side >= 700 && side <= 1080 * 1.45 + 1, "the impact frame's size on a 1080p screen (" + side + ")");
        }
        Check.that(com.hexhaki.piece.network.S2CClash.CLASSIC == 8, "the Gura ultimate keeps the old impact frame (S2CClash.CLASSIC)");
        // 3.5.9 (the owner: "Impact frames are only on: Did 2 attacks clash? (if yes then the impact frame plays for both)")
        com.hexhaki.piece.network.S2CClash met = new com.hexhaki.piece.network.S2CClash(1, 0, 0, 0, 1, 0, 2, 0, 0, 10, 20,
            com.hexhaki.piece.network.S2CClash.CLASH);
        Check.that(met.framed(10) && met.framed(20), "a clash's impact frame plays for both who clashed");
        Check.that(!met.framed(30) && !met.framed(-1), "and for no one else");
        com.hexhaki.piece.network.S2CClash hit = new com.hexhaki.piece.network.S2CClash(2, 0, 0, 0, 1, 0, 2, 0, 0, 10, 20, 0);
        Check.that(!hit.framed(10) && !hit.framed(20), "a hit's burst plays no impact frame");
        Check.that((com.hexhaki.piece.network.S2CClash.CLASH & 0xFF) == com.hexhaki.piece.network.S2CClash.CLASH,
            "the clash flag fits the message's flag byte");
    }

    static void mochi() {
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.MOCHI) == com.hexhaki.piece.FruitRanks.Rank.LEGENDARY,
            "ranks: the Mochi fruit Legendary (the owner: \"Legendary fruit\")");
        // 3.6.3 (the owner: "I WANT THE OTHER MOVES, all of them, I want yours gone"; "Dough base + V2 awakened", keep the left-click
        // punches): the reference's six on Z X C V B N, the punches on LMB, the Awakening on M
        java.util.List<com.hexhaki.fruit.FruitMove> base = com.hexhaki.piece.server.MochiKit.MOVES;
        String[] keys = {"[LMB]", "[Z]", "[X]", "[C]", "[V]", "[B]", "[N]", "[M]"};
        String[] names = {"Mochi Punch", "Grilled Mochi", "Mochi Muddle", "Lotus Mochi Combo", "Chestnut Mochi", "Mochi Anemone",
            "Buzz Cut Mochi", "Awakening"};
        Check.that(base.size() == keys.length, "the Mochi keys: eight (" + base.size() + ")");
        for (int i = 0; i < keys.length; i++) {
            Check.that(base.get(i).key().startsWith(keys[i]) && base.get(i).name().equals(names[i]), "the Mochi keys: " + keys[i] + " is "
                + names[i] + " (" + base.get(i).name() + ")");
        }
        for (String gone : new String[] {"Tsuki", "Yanagi", "Kaku", "Zan Giri", "Sticky", "Future Sight", "Nagare", "Kagami", "Amadare",
            "Muso", "Ginkaku"}) {
            for (com.hexhaki.fruit.FruitMove move : com.hexhaki.piece.server.MochiKit.ALL) {
                Check.that(!move.name().contains(gone), "the old Mochi move " + gone + " is gone");
            }
        }
        Check.that(com.hexhaki.piece.server.MochiKit.GRILLED.hold() && com.hexhaki.piece.server.MochiKit.ANEMONE.hold(),
            "Grilled Mochi and the Anemone are held");
        Check.that(com.hexhaki.piece.server.MochiMoves.yakiCharge(0) > 0.0 && com.hexhaki.piece.server.MochiMoves.yakiCharge(1000) == 1.0
            && com.hexhaki.piece.server.MochiMoves.yakiRadius(1.0) > 2.5 * com.hexhaki.piece.server.MochiMoves.yakiRadius(0.0),
            "Grilled Mochi: the longer the hold, the bigger the explosion (the owner)");
        Check.that(com.hexhaki.piece.server.MochiMoves.BUZZ_COOLDOWN == 6000, "Buzz Cut Mochi: five minutes' cooldown (the owner's word)");
        Check.that(com.hexhaki.piece.mochi.MochiDraw.PUNCH_OUT == 3.0, "the Mochi fruit's strikes land on the drawn beats");
        // 3.6.3 (the owner: the moves to look "exactly like the reference GIFs"): Lotus Mochi Combo's beats in the reference's order and
        // its body thrown up, held high and driven down onto the ground it left
        int[] air = com.hexhaki.piece.mochi.MochiDraw.LOTUS_AIR;
        Check.that(com.hexhaki.piece.mochi.MochiDraw.LOTUS_OPEN < com.hexhaki.piece.mochi.MochiDraw.LOTUS_CHARGE
            && com.hexhaki.piece.mochi.MochiDraw.LOTUS_CHARGE < com.hexhaki.piece.mochi.MochiDraw.LOTUS_PUNCH
            && com.hexhaki.piece.mochi.MochiDraw.LOTUS_PUNCH < com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH
            && com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH + com.hexhaki.piece.mochi.MochiDraw.LOTUS_RISE < air[0]
            && air[air.length - 1] < com.hexhaki.piece.mochi.MochiDraw.LOTUS_DROP
            && com.hexhaki.piece.mochi.MochiDraw.LOTUS_DROP < com.hexhaki.piece.mochi.MochiDraw.LOTUS_FALL
            && com.hexhaki.piece.mochi.MochiDraw.LOTUS_FALL < com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND,
            "Lotus: ribbon, donut, punch, launch, blows in the air, the giant fist, the fall and the ground, in the reference's order");
        Check.that(com.hexhaki.piece.mochi.MochiDraw.lotusHeight(com.hexhaki.piece.mochi.MochiDraw.LOTUS_PUNCH) == 0.0
            && com.hexhaki.piece.mochi.MochiDraw.lotusHeight(air[1]) > com.hexhaki.piece.mochi.MochiDraw.LOTUS_HIGH - 0.5
            && com.hexhaki.piece.mochi.MochiDraw.lotusHeight(com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND - 0.2) < 1.0
            && com.hexhaki.piece.mochi.MochiDraw.lotusHeight(com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND) == 0.0,
            "Lotus: the one hit is on the ground for the punch, high for the blows in the air, back on the ground at the slam");
        double[] d = com.hexhaki.piece.mochi.MochiDraw.lotusAt(0, 1, new double[3]);
        Check.that(d[0] > 2.0 && d[2] > 0.0, "Lotus: the donut stands at the user's left, a little ahead (facing +z, the left is +x)");
    }

    /**
     * 3.5.2 (the owner: "Sky walk needs a rework, I dont want 2 constant lines holding me up I wanna climb up like stairs when I hold Space,
     * I wanna jump from 1 string to another further away, I want to swing like spiderman"): held forward, swings one string at a time
     * hold their height and carry the body far and fast (never past the game's movement check), each let go ahead of its anchor; a leap
     * lands where it aims; a climb's step rises a step; a string never yanks the body in nor swings the feet into the ground.
     */
    static void skyRoad() {
        double[] h = {0, 30, 0};
        double[] v = {0, 0, 0};
        double[] an = new double[5];
        double[] a = new double[3];
        double[] push = {0, 0, com.hexhaki.piece.ito.SkyPhysics.PUMP};
        double cruise = 30;
        int swings = 0;
        double low = 1e9;
        double high = -1e9;
        double fastest = 0;
        int mode = 0;
        int since = 0;
        double length = 0;
        double first = 0;
        for (int t = 0; t < 400; t++) {
            since++;
            if (mode == 0) {
                com.hexhaki.piece.ito.SkyPhysics.anchor(h, 0, 1, Math.hypot(v[0], v[2]), 0.5, cruise, Double.NaN, an);
                double gap = Math.sqrt((an[0] - h[0]) * (an[0] - h[0]) + (an[1] - h[1]) * (an[1] - h[1]) + (an[2] - h[2]) * (an[2] - h[2]));
                Check.that(an[4] >= gap - 1.0E-9, "Sky Road: a string catches no shorter than the gap (never a snap)");
                a[0] = an[0];
                a[1] = an[1];
                a[2] = an[2];
                length = an[4];
                first = an[3];
                mode = 1;
                since = 0;
            }
            if (mode == 1) {
                com.hexhaki.piece.ito.SkyPhysics.fly(v, com.hexhaki.piece.ito.SkyPhysics.FLY_GRAVITY);
                if (since >= com.hexhaki.piece.ito.SkyPhysics.THROW) {
                    mode = 2;
                    since = 0;
                    swings++;
                }
            } else if (mode == 2) {
                length = com.hexhaki.piece.ito.SkyPhysics.reel(length, first);
                com.hexhaki.piece.ito.SkyPhysics.swing(h, v, a, length, push, com.hexhaki.piece.ito.SkyPhysics.SWING_DRAG);
                if (com.hexhaki.piece.ito.SkyPhysics.letGo(h, a, v, 0, 1)) {
                    Check.that(h[2] > a[2], "Sky Road: a swing lets go ahead of its anchor");
                    com.hexhaki.piece.ito.SkyPhysics.kick(v);
                    mode = 3;
                    since = 0;
                }
                Check.that(since < 60, "Sky Road: every swing lets go");
            } else {
                com.hexhaki.piece.ito.SkyPhysics.fly(v, com.hexhaki.piece.ito.SkyPhysics.FLY_GRAVITY);
                if (since >= com.hexhaki.piece.ito.SkyPhysics.NEXT_LATEST
                    || since >= com.hexhaki.piece.ito.SkyPhysics.NEXT_SOONEST && v[1] < 0.05) {
                    mode = 0;
                }
            }
            h[0] += v[0];
            h[1] += v[1];
            h[2] += v[2];
            low = Math.min(low, h[1]);
            high = Math.max(high, h[1]);
            fastest = Math.max(fastest, Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
        }
        Check.that(swings >= 10 && h[2] > 250, "Sky Road: swinging carries the body far: " + swings + " swings, " + h[2] + " blocks in 20 s");
        Check.that(low > cruise - 5 && high < cruise + 6, "Sky Road: a swing holds its height: " + low + " to " + high);
        Check.that(fastest <= com.hexhaki.piece.ito.SkyPhysics.TOP + 1.0E-9 && com.hexhaki.piece.ito.SkyPhysics.TOP < 3.0,
            "Sky Road: never faster than the movement check allows");
        // near the ground the anchor is raised so the swing's bottom keeps the feet off it
        double[] g = {0, 3.5, 0};
        com.hexhaki.piece.ito.SkyPhysics.anchor(g, 1, 0, 0.5, 0.0, 3.5, 0.0, an);
        Check.that(an[1] - an[3] >= com.hexhaki.piece.ito.SkyPhysics.GRIP + com.hexhaki.piece.ito.SkyPhysics.CLEAR - 1.0E-9,
            "Sky Road: a swing's bottom keeps the feet off the ground");
        // low over the ground the string catches long and reels the body up to it, a little a tick
        Check.that(an[4] > an[3] && com.hexhaki.piece.ito.SkyPhysics.reel(an[4], an[3]) == an[4] - com.hexhaki.piece.ito.SkyPhysics.PULL,
            "Sky Road: low down the string reels the body up, never snapping it");
        // a leap lands on the string it aims at
        double[] from = {5, 40, -3};
        double[] at = new double[3];
        double[] lv = new double[3];
        com.hexhaki.piece.ito.SkyPhysics.leap(from, 30, -15, at, lv);
        for (int t = 0; t < com.hexhaki.piece.ito.SkyPhysics.LEAP_TICKS; t++) {
            from[0] += lv[0];
            from[1] += lv[1];
            from[2] += lv[2];
            com.hexhaki.piece.ito.SkyPhysics.fly(lv, com.hexhaki.piece.ito.SkyPhysics.FLY_GRAVITY);
        }
        Check.that(Math.abs(from[0] - at[0]) < 0.05 && Math.abs(from[1] - at[1]) < 0.05 && Math.abs(from[2] - at[2]) < 0.05,
            "Sky Road: a leap lands where it aims");
        Check.that(Math.hypot(at[0] - 5, at[2] + 3) > 10.0, "Sky Road: a leap reaches a string further away");
        // a climbing step rises a step
        double up = 0;
        double[] sv = new double[3];
        for (int k = 0; k < com.hexhaki.piece.ito.SkyPhysics.STEP_TICKS; k++) {
            com.hexhaki.piece.ito.SkyPhysics.step(k, 0, 1, sv);
            up += sv[1];
        }
        Check.that(Math.abs(up - com.hexhaki.piece.ito.SkyPhysics.STEP_RISE) < 1.0E-9, "Sky Road: a step climbs one step");
    }

    /**
     * 3.5.4 (the owner: "Bird cage is freezing and forcing entities to the middle, regarless if the cage pulls them or Not, thats wrong,
     * they are free to move around at all times, if they touch the cage they get damage and get pushed along with it, only at the end are
     * they free after taking the full damage"): away from the wall a body is left alone (its own movement kept), standing or closing; at the
     * wall it slides along it as it likes, only its push through the wall taken away, and the closing wall sweeps it in at least as fast
     * as the wall comes.
     */
    static void birdcage() {
        double sweep = 0.12;
        Check.that(com.hexhaki.piece.server.ItoMoves.cageShove(1, 0, 0, -6.0, 0.3, 0.0, -0.2, true, sweep) == null
            && com.hexhaki.piece.server.ItoMoves.cageShove(1, 0, 0, -6.0, 0.3, 0.0, -0.2, true, 0.0) == null,
            "Birdcage: inside, away from its strings, a body moves freely (no pull to the middle)");
        double[] v = com.hexhaki.piece.server.ItoMoves.cageShove(1, 0, 0, -0.5, 0.4, 0.0, 0.25, true, sweep);
        Check.that(v != null && v[0] <= -(com.hexhaki.piece.server.ItoMoves.CAGE_PUSH + sweep) + 1.0E-9 && Math.abs(v[2] - 0.25) < 1.0E-9,
            "Birdcage: touching the closing wall, swept in with it, still free to slide along it");
        double[] w = com.hexhaki.piece.server.ItoMoves.cageShove(1, 0, 0, -0.5, -0.3, 0.0, 0.0, true, sweep);
        Check.that(w != null && w[0] < -0.3, "Birdcage: moving away from the wall, a body keeps its own speed");
        double[] o = com.hexhaki.piece.server.ItoMoves.cageShove(1, 0, 0, 0.6, -0.4, 0.0, 0.0, false, 0.0);
        Check.that(o != null && o[0] >= com.hexhaki.piece.server.ItoMoves.CAGE_PUSH - 1.0E-9, "Birdcage: outside, kept out");
    }
}
