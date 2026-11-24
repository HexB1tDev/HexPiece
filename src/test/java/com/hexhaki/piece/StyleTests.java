package com.hexhaki.piece;

import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.HandMoves;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleCombo;
import com.hexhaki.piece.style.StyleMove;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 2.1.1: the fighting styles' movesets, their animations, the string's chaining, renown and mastery numbers. */
public final class StyleTests {
    static final Path ANIMATIONS = Path.of("src/main/resources/assets/hexhaki/player_animation");

    public static void main(String[] args) throws Exception {
        movesets();
        animations();
        combo();
        rules();
        galaxy();
        powers();
        removed();
        tumble();
        air();
        show();
        hands();
        Check.done("StyleTests");
    }

    static void movesets() {
        for (FightingStyle style : FightingStyle.choices()) {
            if (style.sword()) {
                continue; // 2.1.4: the sword's moves are SwordMove's (SwordTests)
            }
            if (style == FightingStyle.BRAWLER) {
                // 3.5.7: the old moves gone; 3.5.8: Garp's are its own (server/IronFist), none of StyleMove's
                for (Slot slot : Slot.values()) {
                    Check.that(StyleMove.of(style, slot) == null, "Iron Fist's " + slot + " is server/IronFist's, not StyleMove's");
                }
                Check.that(style.armed() && style.choice(), "Iron Fist: picked and armed with Garp's moves");
                continue;
            }
            for (Slot slot : Slot.values()) {
                StyleMove move = StyleMove.of(style, slot);
                Check.that(move != null, style + " has a move for " + slot);
                Check.that(move.duration() > 0 && move.cancel() <= move.duration(), move + ": its cancel is inside it");
                Check.that(slot.string() == (move.cooldown() == 0), move + ": string blows have no cooldown, the others do");
                Check.that(slot.string() ? move.mastery() == 0 : true, move + ": the string is open from the start");
                Check.that(slot != Slot.HEAVY || move.mastery() == 0, move + ": the signature is open from the start");
                Check.that(slot != Slot.SPECIAL_A || move.mastery() > 0 && move.mastery() < 150, move + ": the first special opens early");
                Check.that(slot != Slot.SPECIAL_B || move.mastery() >= 150 && move.mastery() <= 400, move + ": the second special opens later");
                // 2.8.4: the show's own moves on C, V and B: one early, one midway, one late
                Check.that(slot != Slot.EXTRA_C || move.mastery() > 0 && move.mastery() <= 50, move + ": C opens early");
                Check.that(slot != Slot.EXTRA_V || move.mastery() > 50 && move.mastery() <= 200, move + ": V opens midway");
                Check.that(slot != Slot.EXTRA_B || move.mastery() > 200 && move.mastery() <= 400, move + ": B opens late");
                Check.that(!slot.extra() || move.cooldown() > 0, move + ": an extra recharges");
                int last = -1;
                for (Hit hit : move.hits()) {
                    Check.that(hit.tick() > 0 && hit.tick() < move.duration(), move + ": every blow lands inside the move");
                    Check.that(hit.tick() >= last, move + ": blows in order");
                    last = hit.tick();
                    Check.that(hit.damage() > 0 && hit.damage() <= 14, move + ": damage in range");
                    Check.that(hit.reach() > 0 && hit.reach() <= 25, move + ": reach in range");
                }
                boolean defensive = move.effect() == StyleMove.Effect.GUARD
                    || move.effect() == StyleMove.Effect.AIR_STEP || move.effect() == StyleMove.Effect.DODGE;
                Check.that(defensive || move.hits().length > 0, move + ": an attack has blows");
                Check.that(move.animation().startsWith("style_" + style.id() + "_"), move + ": its animation is named for its style");
            }
            StyleMove finisher = StyleMove.of(style, Slot.M1_4);
            for (Hit hit : finisher.hits()) {
                Check.that(hit.weight() == Hit.Weight.FINISHER, style + ": the fourth blow is a finisher");
            }
        }
        Check.that(StyleMove.of(FightingStyle.NONE, Slot.M1_1) == null, "bare hands have no moves");
    }

    static void animations() throws Exception {
        Pattern end = Pattern.compile("\"endTick\":\\s*(\\d+)");
        for (StyleMove move : StyleMove.values()) {
            Path file = ANIMATIONS.resolve(move.animation() + ".json");
            Check.that(Files.exists(file), move + ": its animation " + file + " exists");
            String json = Files.readString(file);
            Matcher matcher = end.matcher(json);
            Check.that(matcher.find() && Integer.parseInt(matcher.group(1)) == move.duration(), move + ": its animation lasts as long as the move");
            for (Hit hit : move.hits()) {
                Check.that(json.contains("\"tick\": " + hit.tick() + ","), move + ": its animation has a keyframe on its hit tick " + hit.tick());
            }
        }
        // 3.5.7: Fishman Karate's clips gone with it, Iron Fist's old ones with its old moves (3.5.8: Garp's own in their place)
        try (var files = Files.list(ANIMATIONS)) {
            Check.that(files.map(f -> f.getFileName().toString()).noneMatch(n -> n.startsWith("style_fishman_karate_")
                || n.equals("style_brawler_jab.json") || n.equals("style_brawler_gatling.json") || n.equals("style_brawler_honesty.json")),
                "no Fishman Karate clip left, nor the old Iron Fist's");
        }
        for (String clip : new String[] {"punch_l", "punch_r", "love", "meteor_hold", "meteor_throw", "crusher", "blue_hole", "divide", "infinitum",
            "impact"}) {
            Check.that(Files.exists(ANIMATIONS.resolve("style_brawler_" + clip + ".json")), "Iron Fist's " + clip + " clip");
        }
        // 2.1.2: combat mode's stances: one per style, a loop, the arms only (walking, the body and the head stay the game's)
        Pattern part = Pattern.compile("\"(body|torso|head|rightLeg|leftLeg|rightItem|leftItem)\":");
        Check.that(FightingStyle.BRAWLER.stance() == null, "Iron Fist holds no stance: no attacking pose while idle, walking or sprinting");
        for (FightingStyle style : FightingStyle.choices()) {
            if (style.stance() == null) {
                Check.that(!Files.exists(ANIMATIONS.resolve("style_" + style.id() + "_stance.json")), style + ": no stance file left over");
                continue;
            }
            Path file = ANIMATIONS.resolve(style.stance() + ".json");
            Check.that(Files.exists(file), style + ": its combat stance " + file + " exists");
            String json = Files.readString(file);
            Check.that(json.contains("\"isLoop\": true"), style + ": its stance loops");
            Check.that(json.contains("\"rightArm\":") && json.contains("\"leftArm\":"), style + ": its stance holds both arms");
            Check.that(!part.matcher(json).find(), style + ": its stance moves the arms only");
        }
    }

    static void combo() {
        StyleMove first = StyleMove.of(FightingStyle.SIX_POWERS, Slot.M1_1);
        Check.that(StyleCombo.startFor(null, 0, 100) == 100, "a first press starts at once");
        StyleMove hook = StyleMove.of(FightingStyle.BLACK_LEG, Slot.M1_2);
        Check.that(StyleCombo.startFor(hook, 100, 101) == -1, "a press early in a long blow's wind-up is dropped");
        long cancel = 100 + first.cancel();
        Check.that(StyleCombo.startFor(first, 100, cancel - StyleCombo.BUFFER) == cancel, "a quick blow's press is buffered to its cancel tick");
        Check.that(StyleCombo.startFor(first, 100, cancel - 2) == cancel, "a press just before the cancel tick waits for it (buffered)");
        Check.that(StyleCombo.startFor(first, 100, cancel + 1) == cancel + 1, "a press after the cancel tick starts at once");
        Check.that(StyleCombo.next(first, 100, cancel) == Slot.M1_2, "the string goes on");
        Check.that(StyleCombo.next(first, 100, 100 + first.duration() + StyleCombo.WINDOW + 1) == Slot.M1_1, "too late: it starts over");
        StyleMove finisher = StyleMove.of(FightingStyle.SIX_POWERS, Slot.M1_4);
        Check.that(StyleCombo.next(finisher, 100, 200) == Slot.M1_1, "after the finisher it starts over");
        Check.that(StyleCombo.startFor(finisher, 100, 100 + finisher.duration()) == -1, "the finisher rests before a new string");
    }

    static void rules() {
        Check.near(PieceRules.masteryDamage(0), 1.0, 1e-6, "no mastery, plain damage");
        Check.near(PieceRules.masteryDamage(1000), 1.6, 1e-6, "full mastery, 1.6x");
        Check.that(PieceRules.title(Faction.PIRATE, 0).equals("Nobody") && PieceRules.title(Faction.PIRATE, 150_000_000L).equals("Supernova"),
            "pirate titles by bounty");
        Check.that(PieceRules.title(Faction.MARINE, 0).equals("Seaman Recruit") && PieceRules.title(Faction.MARINE, 7000).equals("Fleet Admiral"),
            "marine ranks by merit");
        Check.that(PieceRules.nextTitle(Faction.PIRATE, 150_000_000L) == 300_000_000L, "the next title");
        Check.that(PieceRules.renownFor(Faction.PIRATE, 2) > PieceRules.renownFor(Faction.PIRATE, 0), "a captain is worth more");
        Check.that(PieceRules.berries(1_234_000L).equals("1,234,000"), "berries print like a poster");
        Check.that(Faction.PIRATE.enemy() == Faction.MARINE && Faction.MARINE.enemy() == Faction.PIRATE, "the two sides");
    }

    /**
     * 3.5.9 (the owner: "HOLDING R will now fully disable all powers and abilities (but fruit passives)"): R held switches every power;
     * the original's switch comes here too and no longer removes the kit (its passives); every message that starts a power asks first.
     */
    static void powers() throws Exception {
        String patch = java.nio.file.Files.readString(java.nio.file.Path.of("patch/PiecePatch.java"));
        Check.that(patch.contains("\"togglePowers\", PLAYER_VOID, POWERS, \"toggle\", 1}")
            && patch.contains("\"active\", \"(Lnet/minecraft/server/level/ServerPlayer;)Z\",\n            POWERS, \"passives\", 1}")
            && patch.contains("\"hasFruit\", \"(Lnet/minecraft/world/entity/player/Player;)Z\", POWERS,\n            \"gomuThrough\", 1}")
            && patch.contains("PiecePatch::gomuPowersOff"), "the original's switch, the passives, the Gomu fruit's Haki and actions: through Powers");
        String powers = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/com/hexhaki/piece/server/Powers.java"));
        Check.that(powers.contains("kit.cancel(player)") && !powers.contains("kit.removed(") && !powers.contains(".removed(player)"),
            "powers off cancels the fruit's moves and forms, never removes it (its passives stay)");
        for (String handler : new String[] {"C2SCombat", "C2SStyleAction", "C2SSword", "C2SDragon", "C2SNikaJump", "C2SItoSky"}) {
            String code = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/com/hexhaki/piece/network/" + handler + ".java"));
            Check.that(code.contains("Powers.off("), handler + " asks whether every power is off");
        }
        String ryou = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/com/hexhaki/piece/server/RyouServer.java"));
        Check.that(ryou.contains("|| Powers.off(player)"), "no Haki while every power is off");
        Check.that(com.hexhaki.piece.client.CombatMode.HOLD >= 8 && com.hexhaki.piece.client.CombatMode.HOLD <= 20,
            "R held about half a second switches the powers; a tap stays a tap");
    }

    /**
     * 3.5.9 (the owner: "Galaxy Fist  Removed entirely.", "Haki Grip  Removed entirely as well", "Wifi haki becomes a Sword's man
     * ability only (switch the key to a free quickslot not L)"): the original's starts and ends of both go nowhere; WiFi Haki's through
     * HexPiece; every layout loses both and keeps WiFi Haki for a Swordsman alone, never on L.
     */
    static void galaxy() throws Exception {
        String patch = java.nio.file.Files.readString(java.nio.file.Path.of("patch/PiecePatch.java"));
        for (String gone : new String[] {"beginConvergence", "releaseConvergence", "beginDominion", "releaseDominion"}) {
            Check.that(patch.contains("{CONTROLLER, \"onAction\", CONTROLLER, \"" + gone + "\", PLAYER_VOID, QUICK, \"gone\", 1}"),
                gone + " goes nowhere");
        }
        Check.that(patch.contains("\"sovereignStrike\", PLAYER_VOID, QUICK, \"wifi\", 1}") && !patch.contains("520, 0, 2"),
            "WiFi Haki through HexPiece; the Galaxy Fist's mastery row gone with it");
        Check.that(!java.nio.file.Files.exists(java.nio.file.Path.of("src/main/java/com/hexhaki/piece/server/GalaxyFist.java")),
            "no Galaxy Fist class left");
        int[] old = {100, 101, 102, 103, -1, -1, -1, -1, -1};
        int[] sword = com.hexhaki.piece.haki.QuickSlots.clean(old, true);
        Check.that(java.util.Arrays.equals(sword, new int[] {100, 102, -1, -1, -1, -1, -1, -1, -1}),
            "a Swordsman: the Grip and the Galaxy Fist gone, WiFi Haki off L onto K " + java.util.Arrays.toString(sword));
        int[] other = com.hexhaki.piece.haki.QuickSlots.clean(old, false);
        Check.that(java.util.Arrays.equals(other, new int[] {100, -1, -1, -1, -1, -1, -1, -1, -1}), "anyone else: no WiFi Haki");
        int[] busyK = com.hexhaki.piece.haki.QuickSlots.clean(new int[] {100, 1, 102, -1, 4, 5, 6, 7, 8}, true);
        Check.that(busyK[3] == 102 && busyK[2] == -1 && busyK[1] == 1, "K taken by the fruit: WiFi Haki on H, never L");
        int[] full = com.hexhaki.piece.haki.QuickSlots.clean(new int[] {100, 1, 102, 3, 4, 5, 6, 7, 8}, true);
        Check.that(full[2] != 102, "no free slot but L: WiFi Haki left off rather than on L");
        int[] kept = com.hexhaki.piece.haki.QuickSlots.clean(new int[] {100, -1, -1, -1, -1, 102, -1, -1, -1}, true);
        Check.that(kept[5] == 102 && kept[1] == -1, "WiFi Haki where the player put it (not L) stays there");
        for (int[] layout : new int[][] {sword, other, busyK, full, kept}) {
            for (int a : layout) {
                Check.that(!com.hexhaki.piece.haki.QuickSlots.gone(a), "no layout keeps a gone power");
            }
        }
    }

    /**
     * 3.5.7 (the owner: "Remove FIshman karate from the game entirely"): no Fishman Karate left: not a style, no move of it, old saves'
     * code for it read as no style (and no other style takes its number); styles keep their save codes.
     */
    static void removed() {
        for (FightingStyle style : FightingStyle.values()) {
            Check.that(!style.id().contains("fishman") && !style.display().toLowerCase(java.util.Locale.ROOT).contains("fishman"),
                style + ": not Fishman Karate");
            Check.that(FightingStyle.byCode(style.code()) == style, style + ": its save code reads back");
        }
        Check.that(FightingStyle.byCode(3) == FightingStyle.NONE, "Fishman Karate's old save code reads as no style");
        Check.that(FightingStyle.BLACK_LEG.code() == 1 && FightingStyle.SIX_POWERS.code() == 2 && FightingStyle.BRAWLER.code() == 4
            && FightingStyle.SWORDSMAN.code() == 5, "every other style keeps its old save number");
        for (StyleMove move : StyleMove.values()) {
            Check.that(!move.name().startsWith("FK_") && !move.name().startsWith("IF_"), move + ": not a Fishman Karate nor an old Iron Fist move");
        }
    }

    /** 2.3.9: the rag doll a knocked body becomes (HexGhoul's; kept for every fall). */
    static void tumble() {
        // the rag doll (HexGhoul's)
        com.hexhaki.piece.style.Tumble.Frame start = com.hexhaki.piece.style.Tumble.at(0, Double.NaN);
        Check.that(start.tumble() == 0 && start.flat() == 0, "upright the instant it is struck");
        com.hexhaki.piece.style.Tumble.Frame air = com.hexhaki.piece.style.Tumble.at(8, Double.NaN);
        Check.that(air.tumble() > 1.0 && air.head() > 1.0, "over and over in the air, the head snapped round");
        com.hexhaki.piece.style.Tumble.Frame lying = com.hexhaki.piece.style.Tumble.at(20, 10);
        Check.near(lying.flat(), 1.0, 1e-9, "flat once down");
        Check.that(!com.hexhaki.piece.style.Tumble.over(10 + com.hexhaki.piece.style.Tumble.DOWN_TICKS, 10)
            && com.hexhaki.piece.style.Tumble.over(10 + com.hexhaki.piece.style.Tumble.DOWN_TICKS + com.hexhaki.piece.style.Tumble.GETUP_TICKS, 10),
            "it lies there, then gets up");
        com.hexhaki.piece.style.Tumble.Frame up = com.hexhaki.piece.style.Tumble.at(10 + com.hexhaki.piece.style.Tumble.DOWN_TICKS
            + com.hexhaki.piece.style.Tumble.GETUP_TICKS, 10);
        Check.that(up.flat() < 1.0e-6 && Math.abs(up.head()) < 1.0e-9, "and stands as it was");
    }

    /** 2.4.1: Six Powers' air in 3D. */
    static void air() {
        Check.near(com.hexhaki.piece.air.AirMesh.taper(0), 1.0, 1e-9, "a cut of air is whole in its middle");
        Check.that(com.hexhaki.piece.air.AirMesh.taper(1) < 1e-6 && com.hexhaki.piece.air.AirMesh.taper(-1) < 1e-6, "and drawn to a point at its tips");
        double[] f = {0, 0, 1};
        double[] a = com.hexhaki.piece.air.AirMesh.tilted(f, new double[] {1, 0, 0}, 35);
        Check.near(a[0] * f[0] + a[1] * f[1] + a[2] * f[2], 0.0, 1e-9, "tilted, its tips' line stays square to its flight");
        Check.near(Math.toDegrees(Math.acos(a[0])), 35.0, 1e-6, "tilted by the leg's sweep");
        int[] tris = {0};
        boolean[] see = {true};
        com.hexhaki.piece.air.AirMesh.crescent(new com.hexhaki.piece.air.AirMesh.Crescent(0, 0, -4, f, new double[] {1, 0, 0}, 1.6,
            Math.toRadians(70), 0.1, 0.3, 1.0), 24, 8, (xyz, argb) -> {
                tris[0]++;
                for (int c : argb) {
                    see[0] &= (c >>> 24) < 250;
                }
            });
        Check.that(tris[0] == 24 * 8 * 2, "a crescent of whole triangles");
        Check.that(see[0], "air is never solid");
        Check.that(com.hexhaki.piece.water.WaterMesh.AIR.base() < 0.1 && com.hexhaki.piece.water.WaterMesh.HAKI.base() > 0.8,
            "air all but clear straight on, Haki black");
        Check.that(com.hexhaki.piece.client.fx.AirFx.BLADE_SPEED > 2.2 * 1.9, "the Air Blade at least twice as fast as it was");
        Check.that(com.hexhaki.piece.server.AirServer.RANGE >= 15 && com.hexhaki.piece.server.AirServer.SPEED >= 3, "the finger's Haki bullet flies far, fast");
    }

    /** 2.8.3: the styles' 3D blows (style/StyleRig, StyleArt, StyleShow): the rig stands as the game's model does, every blow is found
     * a limb, and every moment of every move draws sound quads, few enough. */
    static void show() {
        float[][] rest = new float[8][8];
        double[] hand = com.hexhaki.piece.style.StyleRig.limb(rest, com.hexhaki.piece.style.StyleRig.RIGHT_ARM, 0.0, 1.0, 0.0);
        Check.that(Math.abs(hand[0] + 6.0 / 16.0 * 0.9375) < 1.0e-9, "the right hand hangs at the right side (x " + hand[0] + ")");
        Check.that(Math.abs(hand[1] - (1.501 - 12.0 / 16.0) * 0.9375) < 1.0e-9, "the hand hangs at the hip's height");
        double[] foot = com.hexhaki.piece.style.StyleRig.limb(rest, com.hexhaki.piece.style.StyleRig.LEFT_LEG, 0.0, 1.0, 0.0);
        Check.that(Math.abs(foot[1]) < 0.01 && foot[0] > 0, "the left foot stands on the ground at the left");
        com.hexhaki.piece.style.StyleShow.Poser poser = com.hexhaki.piece.client.npc.StylePoser::sample;
        int[] quads = new int[1];
        boolean[] sound = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            quads[0]++;
            for (double v : xyz) {
                sound[0] &= Double.isFinite(v) && Math.abs(v) < 64.0;
            }
        };
        com.hexhaki.piece.style.StyleShow.Place place = new com.hexhaki.piece.style.StyleShow.Place(1.0, -1.6, 3.0, 0.7);
        for (StyleMove move : StyleMove.values()) {
            if (!com.hexhaki.piece.style.StyleShow.shown(move)) {
                continue;
            }
            com.hexhaki.piece.style.StyleShow.Strike[] strikes = com.hexhaki.piece.style.StyleShow.strikes(move, poser);
            Check.that(strikes.length == move.hits().length, move + ": every blow found");
            for (com.hexhaki.piece.style.StyleShow.Strike strike : strikes) {
                Check.that(strike.limb() >= 2 && strike.limb() <= 5, move + ": struck with a limb");
                double l = Math.sqrt(strike.dir()[0] * strike.dir()[0] + strike.dir()[1] * strike.dir()[1] + strike.dir()[2] * strike.dir()[2]);
                Check.that(Math.abs(l - 1.0) < 1.0e-6, move + ": its way is a unit vector");
            }
            Check.that(move.style() != FightingStyle.BLACK_LEG || strikes[0].limb() >= 4, move + ": Black Leg kicks");
            for (boolean haki : new boolean[] {false, true}) {
                for (float t = 0.0F; t <= move.duration() + 16; t += 0.5F) {
                    quads[0] = 0;
                    com.hexhaki.piece.style.StyleShow.attached(sink, move, haki, t, poser, place, 3);
                    for (int i = 0; i < strikes.length; i++) {
                        double age = t - strikes[i].tick();
                        if (age >= 0) {
                            com.hexhaki.piece.style.StyleShow.landed(sink, move, haki, age, new double[] {0.5, 0.0, 2.5}, new double[] {0, 0, 1},
                                strikes[i].size(), i);
                        }
                    }
                    Check.that(sound[0], move + (haki ? " +Haki" : "") + " at " + t + ": every corner finite and near");
                    Check.that(quads[0] < 40000, move + " at " + t + ": " + quads[0] + " quads, few enough");
                }
            }
        }
        Check.that(com.hexhaki.piece.style.StyleArt.of(FightingStyle.BRAWLER, true) == com.hexhaki.piece.style.StyleArt.HAKI,
            "Iron Fist turns black with Haki");
    }

    /**
     * 3.1.4 (the owner: "We need to give them better first-person simple animations. Like, just make them simple for every move of the
     * two fruits we made. Quick. Better light. And better quake."): every Gura and Magma clip, and every Light fruit move, moves the
     * first-person hand; the fist stays on screen through the whole move and never jumps.
     */
    static void hands() throws Exception {
        java.util.List<String> clips = new java.util.ArrayList<>();
        try (var files = Files.list(ANIMATIONS)) {
            files.map(f -> f.getFileName().toString().replace(".json", "")).filter(n -> n.startsWith("gura_") || n.startsWith("magu_") || n.startsWith("hie_")
                || n.startsWith("ito_") && !n.startsWith("ito_sky_") // 3.4.3: Sky Road's run leaves the first-person hand alone
                || n.startsWith("mochi_")) // 3.5.5: every Mochi fruit move moves it
                .sorted().forEach(clips::add);
        }
        Check.that(clips.size() >= 20, "hands: the Gura and Magma clips are found (" + clips.size() + ")");
        for (String light : new String[] {"pika_laser", "pika_laser_charge", "pika_laser_fire", "pika_laser_end", "pika_yasakani",
            "pika_yasakani_end", "pika_lunge", "pika_sword_1", "pika_sword_2", "pika_light_kick_charge", "pika_light_kick", "pika_z_kick",
            "pika_z_slam", "pika_clones", "pika_murakumo", "pika_yata"}) {
            clips.add(light);
        }
        Check.that(!HandMoves.moves("pika_walk") && !HandMoves.moves("pika_idle") && !HandMoves.moves(null),
            "hands: walking, standing and no clip leave the hand alone");
        double[] fist = {0.42, -0.42, -0.85}; // where the fist sits in tools/preview/HandPreview, low in the view as the game draws it
        double[] m = new double[6];
        double[] at = new double[3];
        double[] was = new double[3];
        for (String clip : clips) {
            Check.that(HandMoves.moves(clip), "hands: " + clip + " moves the first-person hand");
            HandMoves.at(clip, 0.0, m);
            boolean rest = true;
            for (double v : m) {
                rest &= Math.abs(v) < 0.2 || clip.endsWith("_end") || clip.equals("gura_slam") || clip.equals("gura_tilt")
                    || clip.equals("gura_release") || clip.equals("gura_gekishin") || clip.equals("magu_release") || clip.equals("magu_meigo")
                    || clip.equals("magu_rise") || clip.equals("pika_laser_fire") || clip.equals("gura_ult_release")
                    || clip.equals("gura_ult_drop") || clip.equals("hie_partisan_launch") || clip.equals("hie_grab")
                    || clip.equals("hie_ball_throw") || clip.equals("hie_road_punch") || clip.equals("ito_cage_cast")
                    || clip.equals("ito_heat_fire");
            }
            Check.that(rest, "hands: " + clip + " starts from the hand's place or from the pose before it");
            boolean seen = true;
            boolean smooth = true;
            for (int i = 0; i <= 160; i++) {
                HandMoves.at(clip, i * 0.25, m);
                HandMoves.apply(m, fist, at);
                seen &= at[2] < -0.3 && Math.abs(at[0] / -at[2]) < 1.15 && Math.abs(at[1] / -at[2]) < 0.66;
                if (i > 0) {
                    double step = Math.sqrt((at[0] - was[0]) * (at[0] - was[0]) + (at[1] - was[1]) * (at[1] - was[1])
                        + (at[2] - was[2]) * (at[2] - was[2]));
                    smooth &= step < 0.2;
                }
                System.arraycopy(at, 0, was, 0, 3);
            }
            Check.that(seen, "hands: " + clip + " keeps the fist on screen");
            Check.that(smooth, "hands: " + clip + " never jumps the fist");
        }
    }
}
