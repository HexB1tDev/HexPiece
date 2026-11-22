package com.hexhaki.piece;

import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 2.7.0: the Uo Uo no Mi's dragon, as the built jar carries it (the owner's model, tools/dragon_mesh.py) and dragon/DragonForm poses and
 * places it: the owner's "at least 6 times bigger than the player", the claws never under the player's feet in the hover, and no jump
 * anywhere (the transformation into the hover, the hover's loop, the flight blend, the hover into the way back), the dragon growing
 * out of the player's chest and shrinking back into it, and facing Minecraft's way.
 */
public final class DragonTests {
    static DragonModel model;
    static DragonForm.Shape shape;

    /**
     * 2.7.5 (the owner: "the keys HUD a ton of them say No slot"): the panel and the log ask for a move's key by its ability, which is
     * its default slot ({@code FruitMove.slot}, as the original's FruitHud reads it), never its place in the list. Every Uo move with a
     * quick slot sits on it in the original's default layout (in each form), the form's moves on slots of their own; M1 has none.
     */
    static void keys() {
        java.util.List<java.util.List<com.hexhaki.fruit.FruitMove>> forms = java.util.List.of(com.hexhaki.piece.server.UoKit.HUMAN,
            com.hexhaki.piece.server.UoKit.DRAGON);
        boolean outOfOrder = false;
        for (java.util.List<com.hexhaki.fruit.FruitMove> moves : forms) {
            int[] layout = com.hexhaki.binds.Binds.defaults(com.hexhaki.fruit.Fruit.UO, moves);
            java.util.Set<Integer> taken = new java.util.HashSet<>();
            for (int i = 0; i < moves.size(); i++) {
                com.hexhaki.fruit.FruitMove move = moves.get(i);
                int slot = move.slot();
                if (move.key().startsWith("[LMB")) {
                    Check.that(slot < 0, move.name() + " is on M1, no quick slot");
                    continue;
                }
                Check.that(slot >= 0 && slot < 9 && layout[slot] == slot, move.name() + " is on its quick slot " + move.key());
                Check.that(taken.add(slot), move.name() + " has a slot of its own");
                outOfOrder |= slot != i;
            }
        }
        Check.that(outOfOrder, "the moves are listed out of slot order (the case the HUD got wrong)");
    }

    /**
     * 2.9.6: the Light fruit's Z (the body gone as light): its head, its ribbon, a reflection, its flashes and the streaks seen from inside
     * it are finite and, all at once with two reflections showing, well within a phone's budget, every tick of their lives.
     */
    static void lightTravel() {
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (double v : xyz) {
                finite[0] &= Double.isFinite(v);
            }
        };
        double[] pts = new double[16 * 3];
        for (int i = 0; i < 16; i++) {
            pts[i * 3] = 2.0 - i * 3.0;
            pts[i * 3 + 1] = i < 6 ? 1.0 + i * 0.6 : 4.6 - (i - 6) * 0.6;
            pts[i * 3 + 2] = 8.0 + i * 0.5;
        }
        for (int age = 0; age <= 12; age++) {
            count[0] = 0;
            com.hexhaki.piece.pika.LightArt.ribbon(sink, pts, 16, 0.8, 1.0);
            com.hexhaki.piece.pika.LightArt.head(sink, pts[0], pts[1], pts[2], 1, 0, 0, age, 3);
            com.hexhaki.piece.pika.LightArt.reflect(sink, -4.0, 0.1, 9.0, 0, 1, 0, 1.0, age, 5);
            com.hexhaki.piece.pika.LightArt.reflect(sink, -20.0, 5.0, 16.0, 1, 0, 0, 1.0, age * 0.5, 6);
            com.hexhaki.piece.pika.LightArt.flare(sink, -40.0, 1.0, 20.0, 1.6, age, 7);
            com.hexhaki.piece.pika.LightArt.rush(sink, 0, 0, 1, age, 8);
            com.hexhaki.piece.pika.LightArt.star(sink, 0, 0, 0.1, 2.0, 0, 1.0); // at the eye: nothing (never round the eye)
            Check.that(finite[0], "the light's travel is finite at " + age);
            Check.that(count[0] > 200 && count[0] < 8000, "the light's travel fits a phone's budget at " + age + " (" + count[0] + " quads)");
        }
        Check.that(com.hexhaki.piece.server.PikaMoves.LIGHT_SPEED < 10.0, "the light goes under the server's movement check (10 blocks a tick)");
    }

    /**
     * 3.1.1: the Gura fruit's quakes (gura/GuraArt) fit a phone's budget at every moment, all of a move's pieces at once (a full Kaishin:
     * the crack, the shock, five craters; Kabutowari's landing; Shima Yurashi held with its two cracks and let go; the rift; the tsunami;
     * the bubble near the eye), every vertex finite; the leap and the dash go under the server's movement check.
     */
    static void guraQuakes() {
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (double v : xyz) {
                finite[0] &= Double.isFinite(v);
            }
        };
        double speed = com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED;
        double[] heights = new double[40];
        for (int age = 0; age <= 64; age += 2) {
            count[0] = 0;
            com.hexhaki.piece.gura.GuraArt.crack(sink, 0.3, -0.2, 1.6, 0, 0, 1, 5.0, age, 7);
            com.hexhaki.piece.gura.GuraArt.shock(sink, 0.3, -0.2, 1.0, 0, 0, 1, 40.0, 0.48, 1.0, speed, age, 5);
            for (int k = 0; k < 5; k++) {
                com.hexhaki.piece.gura.GuraArt.quake(sink, 0.3, -1.6, 6.0 + 7.0 * k, 4.5, 0.8, age - k * 2.3, 20 + k);
            }
            com.hexhaki.piece.gura.GuraArt.bubble(sink, 0.4, -0.4, 1.0, 0.22, age, 3);
            Check.that(finite[0], "Kaishin is finite at " + age);
            Check.that(count[0] < 16000, "a full Kaishin fits a phone's budget at " + age + " (" + count[0] + " quads)");
            count[0] = 0;
            com.hexhaki.piece.gura.GuraArt.crack(sink, 0, 0.7, 4.0, 0, 0, -1, 2.4, age, 12);
            com.hexhaki.piece.gura.GuraArt.quake(sink, 0, -1.6, 4.0, 7.0, 1.0, age, 13);
            com.hexhaki.piece.gura.GuraArt.shake(sink, 0, -1.6, 0, com.hexhaki.piece.server.GuraMoves.SHAKE_RADIUS, age, 10, 6);
            com.hexhaki.piece.gura.GuraArt.crack(sink, 1.3, 0, 0.3, 1, 0, 0, 1.7, Math.min(age, 7.5), 8);
            com.hexhaki.piece.gura.GuraArt.crack(sink, -1.3, 0, 0.3, -1, 0, 0, 1.7, Math.min(age, 7.5), 9);
            com.hexhaki.piece.gura.GuraArt.tilt(sink, 0, -1.6, 0, com.hexhaki.piece.server.GuraMoves.SHAKE_RADIUS, 1.0, speed, age, 6);
            com.hexhaki.piece.gura.GuraArt.rift(sink, 0, -1.6, 1.4, 0, 1, com.hexhaki.piece.server.GuraMoves.RIFT_LENGTH,
                com.hexhaki.piece.server.GuraMoves.RIFT_STEP, heights, age, 8);
            for (int side = -1; side <= 1; side += 2) {
                com.hexhaki.piece.gura.GuraArt.tsunami(sink, -side * com.hexhaki.piece.server.GuraMoves.TSUNAMI_SIDE, -1.6,
                    com.hexhaki.piece.server.GuraMoves.TSUNAMI_AHEAD, side * com.hexhaki.piece.server.GuraMoves.TSUNAMI_SPEED, 0,
                    com.hexhaki.piece.server.GuraMoves.TSUNAMI_LENGTH, com.hexhaki.piece.server.GuraMoves.TSUNAMI_HEIGHT,
                    com.hexhaki.piece.server.GuraMoves.TSUNAMI_LIFE, com.hexhaki.piece.server.GuraMoves.TSUNAMI_RISE,
                    com.hexhaki.piece.server.GuraMoves.TSUNAMI_RISE, age, 11 + side);
            }
            com.hexhaki.piece.gura.GuraArt.tremor(sink, 2, -1.6, 2, 6.0, age, 4);
            Check.that(finite[0], "the ground's quakes are finite at " + age);
            Check.that(count[0] < 16000, "every ground quake at once fits a phone's budget at " + age + " (" + count[0] + " quads)");
        }
        Check.that(com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED * 2 < 10.0 && com.hexhaki.piece.server.GuraMoves.TSUNAMI_SPEED < 10.0,
            "the Gura fruit's own moves go under the server's movement check (10 blocks a tick)");
    }

    /**
     * 3.1.6: the Gura ultimate fits a phone's budget at every moment: its wave over the cone at the longest reach (with Ryou on), the
     * air's crack, the shock, the five fissures and the craters, the bubble at its biggest, under 16,000 see-through quads; the heaving
     * ground's blocks drawn at once stay near {@link com.hexhaki.piece.gura.Heave#MOST_DRAWN} whatever the reach (and are cut there);
     * every plate rests before and after its life and moves within bounds; no tap (a full charge only), five minutes to recover.
     */
    static void guraUltimate() {
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (double v : xyz) {
                finite[0] &= Double.isFinite(v);
            }
        };
        double reach = com.hexhaki.piece.gura.Heave.REACH * 1.6;
        double speed = com.hexhaki.piece.gura.Heave.WAVE;
        double[] heights = new double[80];
        for (int age = 0; age <= 90; age += 2) {
            count[0] = 0;
            com.hexhaki.piece.gura.GuraArt.heave(sink, 0, -1.62, 0, 0, 1, com.hexhaki.piece.gura.Heave.CONE, reach, speed, age, 9);
            com.hexhaki.piece.gura.GuraArt.crack(sink, 0.3, -0.2, 1.6, 0, 0, 1, 3.2, age, 7);
            com.hexhaki.piece.gura.GuraArt.shock(sink, 0.3, -0.2, 1.0, 0, 0, 1, reach, 0.7, 1.0, com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED,
                age, 5);
            for (int k = -2; k <= 2; k++) {
                double a = Math.toRadians(com.hexhaki.piece.gura.Heave.CONE * 0.4 * k);
                com.hexhaki.piece.gura.GuraArt.rift(sink, Math.sin(a) * 3.0, -1.6, Math.cos(a) * 3.0, Math.sin(a), Math.cos(a), reach * 0.9,
                    com.hexhaki.piece.server.GuraMoves.RIFT_STEP, heights, 0.55, age, 30 + k);
            }
            for (int k = 0; k < 4; k++) {
                double d = 8.0 + reach / 4.0 * k;
                com.hexhaki.piece.gura.GuraArt.quake(sink, 0.3, -1.6, d, 2.4 * 1.3 * 1.4, 0.9, age - d / speed, 40 + k);
            }
            com.hexhaki.piece.gura.GuraArt.bubble(sink, 0.6, -0.1, 1.2, 1.05, age, 3);
            Check.that(finite[0], "the ultimate is finite at " + age);
            Check.that(count[0] < 16000, "the ultimate fits a phone's budget at " + age + " (" + count[0] + " quads)");
        }
        for (double r : new double[] {com.hexhaki.piece.gura.Heave.REACH, reach}) {
            java.util.List<com.hexhaki.piece.gura.Heave.Plate> plates = com.hexhaki.piece.gura.Heave.plates(0.5, 0.5, 0.3, 1.0,
                com.hexhaki.piece.gura.Heave.CONE, r, speed, 77);
            Check.that(plates.size() > 40, "the ultimate breaks the ground into many squares (" + plates.size() + " at reach " + r + ")");
            int most = 0;
            for (double t = 0; t < r / speed + com.hexhaki.piece.gura.Heave.LIFE + 4; t += 0.5) {
                most = Math.max(most, com.hexhaki.piece.gura.Heave.drawn(plates, t));
            }
            Check.that(most <= com.hexhaki.piece.gura.Heave.MOST_DRAWN * 1.5, "the heaving blocks stay near the budget at reach " + r
                + " (" + most + " at most)");
            for (com.hexhaki.piece.gura.Heave.Plate p : plates) {
                Check.that(com.hexhaki.piece.gura.Heave.lift(p, p.delay() - 0.5) == 0.0
                    && com.hexhaki.piece.gura.Heave.lift(p, p.delay() + com.hexhaki.piece.gura.Heave.LIFE + 0.1) == 0.0
                    && com.hexhaki.piece.gura.Heave.tip(p, p.delay() + com.hexhaki.piece.gura.Heave.LIFE + 0.1) == 0.0,
                    "a plate rests before the wave and after its life");
                for (double u = 0; u < com.hexhaki.piece.gura.Heave.LIFE; u += 1.0) {
                    double h = com.hexhaki.piece.gura.Heave.lift(p, p.delay() + u);
                    double tip = com.hexhaki.piece.gura.Heave.tip(p, p.delay() + u);
                    Check.that(Double.isFinite(h) && Math.abs(h) <= 1.5 && Math.abs(tip) < Math.toRadians(23.0), "a plate moves within bounds");
                }
            }
        }
        Check.that(com.hexhaki.piece.server.GuraUltimate.COOLDOWN == 6000 && com.hexhaki.piece.server.GuraUltimate.FULL >= 40,
            "the ultimate needs a full charge and takes five minutes to recover");
        // 3.1.8: the Conqueror's lightning out of a blast is the release's own bolts, short-lived, within a phone's budget
        int[] bq = {0};
        com.hexhaki.piece.haki.BoltMesh.Sink boltSink = (glow, xyz, argb) -> bq[0]++;
        java.util.List<com.hexhaki.piece.haki.Bolt> blast = com.hexhaki.piece.haki.ConquerorStorm.blast(9.0, 14, -0.5, 21L, 1.35);
        int blastMost = 0;
        float widest = 0;
        for (com.hexhaki.piece.haki.Bolt b : blast) {
            Check.that(b.duration <= 16, "a blast's bolt snaps quickly (" + b.duration + " ticks)");
            for (float w : b.widths) {
                widest = Math.max(widest, w);
            }
        }
        for (double age = 0; age < 20; age += 1) {
            bq[0] = 0;
            for (com.hexhaki.piece.haki.Bolt b : blast) {
                com.hexhaki.piece.haki.BoltMesh.build(b, com.hexhaki.piece.haki.BoltMesh.Frame.at(0, -1.6, -10), age, 0.002, boltSink);
            }
            blastMost = Math.max(blastMost, bq[0]);
        }
        Check.that(blastMost < 1600, "the biggest blast's lightning fits (" + blastMost + " quads)");
        // 3.2.2 (the owner: "ridiculously fat compared to" the release's): thin lines, as the release's look from the player (under 0.6)
        Check.that(widest > 0.2F && widest < 0.6F, "a blast's bolts are as thin as the release's look (" + widest + " blocks)");
        // 3.1.8: the ground bent (Heave.bend); 3.2.2: in the cone (the owner: "in a cone ... you made a sphere, like a circle"): a trench
        // deepening forward with heaved sides, squares lifted and sunk, nothing behind the feet or out past the cone's sides
        for (double length : new double[] {com.hexhaki.piece.gura.Heave.REACH, com.hexhaki.piece.gura.Heave.BEND_MOST}) {
            int deepest = 0;
            int highest = 0;
            int up = 0;
            int down = 0;
            int moved = 0;
            boolean outside = false;
            double deepAt = 0.0;
            for (int x = -70; x <= 70; x++) {
                for (int z = -70; z <= 70; z++) {
                    int o = com.hexhaki.piece.gura.Heave.bend(z + 0.5, x + 0.5, x, z, length, com.hexhaki.piece.gura.Heave.CONE, 5);
                    if (o < deepest) {
                        deepAt = z + 0.5;
                    }
                    double off = Math.toDegrees(Math.atan2(Math.abs(x + 0.5), z + 0.5));
                    outside |= o != 0 && (z + 0.5 <= 0.0 || off > com.hexhaki.piece.gura.Heave.CONE + 14.0);
                    deepest = Math.min(deepest, o);
                    highest = Math.max(highest, o);
                    up += o > 0 ? 1 : 0;
                    down += o < 0 ? 1 : 0;
                    moved += o != 0 ? 1 : 0;
                    outside |= o != 0 && (z + 0.5 < -6.0 || Math.hypot(x + 0.5, z + 0.5) > length + 8.0);
                }
            }
            Check.that(deepest <= -4 && deepest >= -com.hexhaki.piece.gura.Heave.SINK_MOST && highest >= 2
                && highest <= com.hexhaki.piece.gura.Heave.LIFT_MOST, "the ground sinks into a trench and its sides heave up at " + length + " ("
                + deepest + " to " + highest + ")");
            Check.that(deepAt > length * 0.3, "the trench is deepest out in the cone at " + length + " (" + deepAt + " blocks ahead)");
            Check.that(up > 100 && down > 400, "parts of the ground lift and parts sink at " + length + " (" + up + " up, " + down + " down)");
            Check.that(!outside && moved < 4200, "the bent ground stays in the cone at " + length + " (" + moved + " columns)");
        }
    }

    /**
     * 3.1.3: the Magma fruit's effects (magu/MaguArt) fit a phone's budget: a giant fist flying near the eye with its burst, the burning
     * ground, three meteors, the hound, two geysers, the stream, the spike and the swelling fist all at once stay under 16,000 quads,
     * every vertex finite.
     */
    static void maguMagma() {
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (double v : xyz) {
                finite[0] &= Double.isFinite(v);
            }
        };
        double[] stream = new double[14 * 3];
        for (int i = 0; i < 14; i++) {
            stream[i * 3] = 2.0;
            stream[i * 3 + 1] = -1.6;
            stream[i * 3 + 2] = 3.0 + i * 1.6;
        }
        for (int age = 0; age <= 60; age += 3) {
            // Dai Funka at a full charge: the giant fist near the eye, its blast, the burning ground round it
            count[0] = 0;
            com.hexhaki.piece.magu.MaguArt.fist(sink, 0.4, -0.2, 3.0 + age * 1.7, 0, 0, 1, 2.2, age, 7);
            com.hexhaki.piece.magu.MaguArt.burst(sink, 0, -1.0, 12.0, 8.0, 1.0, age, 9);
            com.hexhaki.piece.magu.MaguArt.pool(sink, 0, -1.6, 10.0, 4.5, age, 160, 4);
            for (int k = 0; k < 4; k++) {
                com.hexhaki.piece.magu.MaguArt.pool(sink, -5 + 3 * k, -1.6, 13.0, 2.4, age, 120, 5 + k);
            }
            com.hexhaki.piece.magu.MaguArt.swell(sink, 0.4, -0.4, 1.0, 0.22, age, 3);
            check(count[0], finite[0], "Dai Funka", age);
            // Ryusei Kazan: three meteors falling, three bursting, the ground burning where six fell, the fist up in the sky
            count[0] = 0;
            for (int k = 0; k < 3; k++) {
                com.hexhaki.piece.magu.MaguArt.meteor(sink, -6 + 6 * k, 10.0 - (age % 12), 18.0, 0, -2.2, 0, 0.9, age, 20 + k);
                com.hexhaki.piece.magu.MaguArt.burst(sink, -5 + 5 * k, -1.2, 14.0 + k, 2.8, 0.35, (age + k * 4) % 40, 30 + k);
            }
            for (int k = 0; k < 6; k++) {
                com.hexhaki.piece.magu.MaguArt.pool(sink, -6 + 2.4 * k, -1.6, 12.0 + (k % 2) * 4, 1.6, age, 60, 40 + k);
            }
            com.hexhaki.piece.magu.MaguArt.meteor(sink, 0, 4.0 + age, 1.0, 0, 2.6, 0, 0.6, age, 50);
            check(count[0], finite[0], "Ryusei Kazan", age);
            // Inugami Guren, Magma Flow and Meigo near each other
            count[0] = 0;
            com.hexhaki.piece.magu.MaguArt.hound(sink, 0, 0.4, 6.0 + age * 0.4, 0, -0.2, 1, com.hexhaki.piece.server.MaguMoves.HOUND_SCALE, age, 11);
            com.hexhaki.piece.magu.MaguArt.stream(sink, stream, 14, age, 6);
            com.hexhaki.piece.magu.MaguArt.pierce(sink, 0, -0.4, 3.0, 0, 0, 1, 1.0, age % 16, 8);
            check(count[0], finite[0], "the hound, the stream and Meigo", age);
            // Bakuretsu Kazan: the boiling ground round the eye and the six geysers alive at once (one every 5 ticks, 28 ticks each)
            count[0] = 0;
            com.hexhaki.piece.magu.MaguArt.pool(sink, 0, -1.6, 0.0, com.hexhaki.piece.server.MaguMoves.ERUPT_RADIUS, age, 100, 12);
            for (int k = 0; k < 6; k++) {
                double ga = k * 1.05;
                com.hexhaki.piece.magu.MaguArt.geyser(sink, Math.cos(ga) * 5.0, -1.6, Math.sin(ga) * 5.0 + 2.0, com.hexhaki.piece.server.MaguMoves.GEYSER_HEIGHT,
                    (age + k * 5) % 28, 3 + k);
            }
            check(count[0], finite[0], "Bakuretsu Kazan", age);
        }
    }

    /**
     * 3.1.9: the Ice fruit's drawn ice (hie/HieArt, the smooth glassy look) fits a phone's budget at every moment: Partisan's eight spears
     * round the eye and eight flying with their trails and bursts; the pheasant close by with its big burst; the Ice Ball's orb, its mist
     * and its breaking; Ice Age's mist row; a saber cut, the ice fist and the body of ice scattering; every vertex finite.
     */
    static void hieIce() {
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (double v : xyz) {
                finite[0] &= Double.isFinite(v);
            }
        };
        double[] row = new double[9 * 3];
        for (int i = 0; i < 9; i++) {
            row[i * 3] = -8 + 2 * i;
            row[i * 3 + 1] = -1.0;
            row[i * 3 + 2] = 10.0;
        }
        for (int age = 0; age <= 60; age += 3) {
            count[0] = 0;
            for (int k = 0; k < com.hexhaki.piece.server.HieMoves.SPEARS_MOST; k++) {
                double[] slot = com.hexhaki.piece.hie.HieArt.slot(k);
                com.hexhaki.piece.hie.HieArt.spear(sink, -slot[0], slot[1] - 1.62, -slot[2], 0, 0, 1, com.hexhaki.piece.hie.HieArt.SPEAR_LENGTH,
                    age / 4.0, 1.0, 40 + k);
                com.hexhaki.piece.hie.HieArt.spear(sink, -3 + k, 0.5, 3.0 + age * 2.4, 0, 0, 1, com.hexhaki.piece.hie.HieArt.SPEAR_LENGTH, 1.0, 1.0, 50 + k);
                com.hexhaki.piece.hie.HieArt.trail(sink, -3 + k, 0.5, 2.0 + age * 2.4, 0, 0, 2.4, 0.16, age, 50 + k);
                com.hexhaki.piece.hie.HieArt.burst(sink, -3 + k, 0.0, 14.0, 0.9, 0.3, (age + k * 2) % 34, 60 + k);
            }
            check(count[0], finite[0], "Partisan", age);
            count[0] = 0;
            com.hexhaki.piece.hie.HieArt.pheasant(sink, 0, 0.5, 6.0 + age * 0.4, 0, 0, 1, com.hexhaki.piece.server.HieMoves.PHEASANT_SCALE, age, 13);
            com.hexhaki.piece.hie.HieArt.trail(sink, 0, 0.5, 6.0 + age * 0.4, 0, 0, 1.6, 1.4, age, 13);
            com.hexhaki.piece.hie.HieArt.burst(sink, 0, -0.5, 12.0, 4.5, 1.0, age, 14);
            check(count[0], finite[0], "Pheasant Beak", age);
            count[0] = 0;
            com.hexhaki.piece.hie.HieArt.gather(sink, 0.4, -0.3, 0.8, 0.35, age % 12, 15);
            com.hexhaki.piece.hie.HieArt.orb(sink, 0, 0, 3.0 + age * 1.6, 0.35, age, 16);
            com.hexhaki.piece.hie.HieArt.trail(sink, 0, 0, 3.0 + age * 1.6, 0, 0, 1.6, 0.35, age, 16);
            com.hexhaki.piece.hie.HieArt.shatter(sink, 0, -0.5, 8.0, com.hexhaki.piece.server.HieMoves.BALL_RADIUS * 1.2, age % 32, 17);
            for (int k = 0; k < 3; k++) {
                com.hexhaki.piece.hie.HieArt.mist(sink, -2 + 2 * k, -1.2, 8.0, 1.0, 0.3, age, 18 + k);
            }
            com.hexhaki.piece.hie.HieArt.mistRow(sink, row, 9, 1.6, 0.5, age, 19);
            check(count[0], finite[0], "Ice Ball and Ice Age", age);
            count[0] = 0;
            com.hexhaki.piece.hie.HieArt.slash(sink, 0, -0.35, 0, 0, 1, 0, 0.5, 2.4, age % 13, 20);
            com.hexhaki.piece.hie.HieArt.coat(sink, 0.4, -0.3, 0.8, 0, 0, 1, 1.0, age, 21);
            com.hexhaki.piece.hie.HieArt.scatter(sink, 0, -0.7, 3.0, 1.8, age % 15, 22);
            com.hexhaki.piece.hie.HieArt.burst(sink, 0, -0.4, 3.0, 2.4, 1.0, age, 23);
            check(count[0], finite[0], "the saber, the ice fist and the ice body", age);
            // 3.2.2: a crowd frozen in crystal by Ice Age (client/hie/HieFx: FULL_FROZEN in full, the rest lite), shattering (FULL_SHATTERS
            // in full), with the Ice Ball's prison breaking beside them
            count[0] = 0;
            for (int k = 0; k < 24; k++) {
                com.hexhaki.piece.hie.HieArt.lite = k >= 6;
                com.hexhaki.piece.hie.HieArt.block(sink, -9 + (k % 8) * 2.5, -1.6, 6.0 + (k / 8) * 3.0, 0.3, 1.9, age, 80, 30 + k);
            }
            for (int k = 0; k < 12; k++) {
                com.hexhaki.piece.hie.HieArt.lite = k >= 4;
                com.hexhaki.piece.hie.HieArt.crystalShatter(sink, -6 + k, -0.6, 9.0, 1.3, age % 32, 60 + k);
            }
            com.hexhaki.piece.hie.HieArt.lite = false;
            com.hexhaki.piece.hie.HieArt.prison(sink, 0, -0.5, 7.0, com.hexhaki.piece.server.HieMoves.BALL_RADIUS, age, 80, 80);
            check(count[0], finite[0], "a crowd frozen in crystal and shattering", age);
            // the frost on a dozen bodies (FULL_FROSTS in full), half of it from the arm
            count[0] = 0;
            for (int k = 0; k < 12; k++) {
                com.hexhaki.piece.hie.HieArt.lite = k >= 6;
                if (k % 2 == 0) {
                    com.hexhaki.piece.hie.HieArt.frost(sink, -6 + k, -1.6, 5.0, 0.3, 1.9, 0.75, age, 90 + k);
                } else {
                    com.hexhaki.piece.hie.HieArt.armFrost(sink, -6 + k, -1.6, 5.0, -1, 0, 0.3, 1.9, 0.75, age, 90 + k);
                }
            }
            com.hexhaki.piece.hie.HieArt.lite = false;
            check(count[0], finite[0], "a dozen frosted bodies", age);
        }
    }

    static void check(int quads, boolean finite, String what, int age) {
        Check.that(finite, what + " is finite at " + age);
        Check.that(quads < 16000, what + " fits a phone's budget at " + age + " (" + quads + " quads)");
    }

    public static void main(String[] args) throws Exception {
        Path jar;
        try (var files = Files.list(Path.of("build"))) {
            jar = files.filter(p -> p.getFileName().toString().matches("hexpiece-.*\\.jar")).findFirst().orElseThrow();
        }
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry("assets/hexhaki/dragon/seiryu.sry");
            Check.that(entry != null, "the jar carries the dragon");
            Check.that(zip.getEntry("assets/hexhaki/textures/piece/dragon/seiryu.png") != null, "and its texture");
            Check.that(zip.getEntry("assets/hexhaki/textures/item/uo_uo_no_mi.png") != null, "and the fruit's icon");
            Check.that(zip.getEntry("assets/hexhaki/textures/item/hie_hie_no_mi.png") != null
                && zip.getEntry("assets/hexhaki/models/item/hie_hie_no_mi.json") != null, "the Ice fruit's icon and model");
            try (InputStream in = zip.getInputStream(entry)) {
                model = DragonModel.read(in);
            }
        }
        shape = new DragonForm.Shape(model);
        keys();
        fire();
        lightTravel();
        guraQuakes();
        guraUltimate();
        maguMagma();
        hieIce();
        Check.that(model.parts.length == 6 && model.bones() >= 92, "every mesh and bone of the owner's model");
        for (String clip : new String[] {"idle", "form", "unform"}) {
            Check.that(model.clips.get(clip) != null, "the take " + clip);
        }
        Check.near(model.clips.get("form").seconds(), DragonForm.FORM_TICKS / 20.0, 0.051, "the transformation is its take's length");
        Check.near(model.clips.get("unform").seconds(), DragonForm.UNFORM_TICKS / 20.0, 0.051, "the way back is its take's length");
        int glow = 0;
        for (DragonModel.Part part : model.parts) {
            glow += part.kind == DragonModel.GLOW ? 1 : 0;
        }
        Check.that(glow == 2, "the two flames are drawn as fire");

        // the hover over the player: at least six players tall, the claws at the feet, the head over the player's
        float[] lo = {1e9F, 1e9F, 1e9F};
        float[] hi = {-1e9F, -1e9F, -1e9F};
        for (float t = 0.0F; t < model.clips.get("idle").seconds(); t += 0.25F) {
            float[][] p = placed(DragonForm.DRAGON, 0.0F, t, 0.0F, 0.0F);
            for (float[] part : p) {
                for (int v = 0; v < part.length; v += 3) {
                    for (int k = 0; k < 3; k++) {
                        lo[k] = Math.min(lo[k], part[v + k]);
                        hi[k] = Math.max(hi[k], part[v + k]);
                    }
                }
            }
        }
        Check.that(hi[1] - lo[1] >= 6.0F * 1.8F, "the hover is at least six players tall (" + (hi[1] - lo[1]) + " blocks)");
        Check.that(lo[1] >= -0.35F, "the claws never hang under the player's feet (" + lo[1] + ")");
        Check.that(hi[2] - lo[2] >= 20.0F, "and long (" + (hi[2] - lo[2]) + " blocks)");

        // no jumps: the transformation's end is the hover's start; the hover's start is the way back's
        Check.that(gap(placed(DragonForm.FORMING, model.clips.get("form").seconds(), 0.0F, 0.0F, 0.0F),
            placed(DragonForm.DRAGON, 0.0F, 0.0F, 0.0F, 0.0F)) < 0.6F, "the transformation lands in the hover");
        for (float hover : new float[] {0.0F, 0.7F, 1.9F}) {
            for (float fly : new float[] {0.0F, 0.6F, 1.0F}) {
                Check.that(gap(placed(DragonForm.DRAGON, 0.0F, hover, fly, hover), placed(DragonForm.UNFORMING, 0.0F, hover, fly, hover)) < 0.02F,
                    "the way back starts where the dragon is (hover " + hover + ", flight " + fly + ")");
            }
        }
        float loop = model.clips.get("idle").seconds();
        Check.that(gap(placed(DragonForm.DRAGON, 0.0F, 0.0F, 0.0F, 0.0F), placed(DragonForm.DRAGON, 0.0F, loop, 0.0F, 0.0F)) < 0.05F,
            "the hover loops without a seam");
        // smooth in time and in the flight's blend (at 40 frames a second: no vertex further than the takes' own fastest)
        for (int phase : new int[] {DragonForm.FORMING, DragonForm.UNFORMING}) {
            float length = DragonForm.length(phase) / 20.0F;
            float[][] before = placed(phase, 0.0F, 1.0F, 0.0F, 0.0F);
            for (float t = 0.025F; t <= length; t += 0.025F) {
                float[][] now = placed(phase, t, 1.0F, 0.0F, 0.0F);
                Check.that(gap(before, now) < 4.5F, "phase " + phase + " moves smoothly at " + t + " s");
                before = now;
            }
        }
        // the flight uncoils the 65-block body: its tail's tip sweeps about 60 blocks over the whole blend, a few each twentieth, and
        // never jumps (a step far out of line with the one before would be a rotation taking the long way round)
        float[][] before = placed(DragonForm.DRAGON, 0.0F, 1.0F, 0.0F, 1.0F);
        float last = -1.0F;
        for (float fly = 0.05F; fly <= 1.0001F; fly += 0.05F) {
            float[][] now = placed(DragonForm.DRAGON, 0.0F, 1.0F, fly, 1.0F);
            float step = gap(before, now);
            Check.that(step < 5.0F, "the flight blends in smoothly at " + fly + " (" + step + ")");
            Check.that(last < 0.0F || step < last * 1.6F + 0.3F, "and evenly at " + fly + " (" + step + " after " + last + ")");
            last = step;
            before = now;
        }

        // 2.7.0 (the owner: "careful with the cape, the body cant clip through"): in flight the mantle never enters the body's tube
        // (its radius the body mesh's half height about the straight bind's spine), at any moment of the wave, blended in or full
        int coat = -1;
        int bodyPart = -1;
        for (int i = 0; i < model.parts.length; i++) {
            coat = model.parts[i].name.equals("coat") ? i : coat;
            bodyPart = model.parts[i].name.equals("body") ? i : bodyPart;
        }
        DragonModel.Part body = model.parts[bodyPart];
        float radius = 0.0F;
        for (int v = 0; v < body.vertices(); v++) {
            if (Math.abs(body.pos[v * 3]) < 1.2F) {
                radius = Math.max(radius, Math.abs(body.pos[v * 3 + 1]));
            }
        }
        String[] spine = new String[25];
        spine[0] = "Body_Chest";
        spine[1] = "Body_Pelvis";
        for (int i = 1; i <= 23; i++) {
            spine[i + 1] = String.format("Body_%02d", i);
        }
        for (float fly : new float[] {0.5F, 0.75F, 1.0F}) {
            for (float age = 0.0F; age < 1.6F; age += 0.1F) {
                DragonPose pose = new DragonPose(model);
                DragonForm.pose(model, shape, pose, DragonForm.DRAGON, 0.0F, 1.0F, fly, age, new float[3]);
                float[][] points = new float[spine.length][3];
                for (int i = 0; i < spine.length; i++) {
                    pose.origin(model.bone(spine[i]), points[i]);
                }
                DragonModel.Part mantle = model.parts[coat];
                float[] pos = new float[mantle.vertices() * 3];
                pose.skin(mantle, pos, new float[pos.length]);
                float nearest = Float.MAX_VALUE;
                for (int v = 0; v < pos.length; v += 3) {
                    for (int i = 0; i + 1 < spine.length; i++) {
                        nearest = Math.min(nearest, segment(pos, v, points[i], points[i + 1]));
                    }
                }
                Check.that(nearest >= radius, "the mantle stays out of the body (flight " + fly + ", " + age + " s: " + nearest + " < " + radius + ")");
            }
        }

        // 2.7.2: the jaw and the bite: the jaw opens (the mouth moves), the bite never jumps and ends back on the hover exactly
        Check.that(gap(posed(0.0F, -1.0F), posed(1.0F, -1.0F)) > 0.5F, "the jaw opens");
        Check.that(gap(posed(0.0F, -1.0F), posed(0.0F, 1.0F)) < 0.01F, "the bite ends where the hover is");
        float[][] was = posed(0.0F, 0.0F);
        Check.that(gap(posed(0.0F, -1.0F), was) < 0.01F, "the bite starts where the hover is");
        for (float b = 0.025F; b <= 1.0001F; b += 0.025F) {
            float[][] now = posed(0.0F, b);
            Check.that(gap(was, now) < 2.5F, "the bite moves smoothly at " + b);
            was = now;
        }

        // out of the chest and back into it
        float[] chest = {0.0F, 1.0F, 0.0F};
        Check.that(far(placed(DragonForm.FORMING, 0.0F, 0.0F, 0.0F, 0.0F), chest) < 1.6F, "the dragon starts inside the player");
        Check.that(far(placed(DragonForm.UNFORMING, DragonForm.UNFORM_TICKS / 20.0F, 1.0F, 0.0F, 0.0F), chest) < 1.6F,
            "and ends inside the player");
        Check.that(!DragonForm.humanHidden(DragonForm.FORMING, 0.0F) && DragonForm.humanHidden(DragonForm.DRAGON, 0.0F)
            && !DragonForm.humanHidden(DragonForm.UNFORMING, DragonForm.UNFORM_TICKS / 20.0F), "the human until it comes out, after it goes in");
        Check.that(DragonForm.grow(DragonForm.FORMING, DragonForm.HUMAN_GONE_AT) > 0.1F, "the human goes once the dragon outgrows it");

        // Minecraft's facing: yaw 0 looks to +z, 90 to -x (the model's front is its +z: the third column)
        float[] place = new float[12];
        DragonForm.placement(shape.hoverHead, new float[3], 1.0F, 0.0F, 0.0F, 0.0F, place);
        Check.that(place[2 * 4 + 2] > 0.99F * DragonForm.SCALE, "yaw 0: the front to +z");
        DragonForm.placement(shape.hoverHead, new float[3], 1.0F, 90.0F, 0.0F, 0.0F, place);
        Check.that(place[0 * 4 + 2] < -0.99F * DragonForm.SCALE && Math.abs(place[2 * 4 + 2]) < 1e-4, "yaw 90: the front to -x");
        DragonForm.placement(shape.hoverHead, new float[3], 1.0F, 0.0F, 30.0F, 0.0F, place);
        Check.that(place[1 * 4 + 2] < -0.49F * DragonForm.SCALE, "looking down pitches the front down");
        Check.near(DragonForm.flightTarget(0.0), 0.0, 1e-6, "no flight standing still");
        Check.near(DragonForm.flightTarget(0.55), 1.0, 1e-6, "all of it at creative flight's pace");
        wave();
        Check.done("DragonTests");
    }

    /**
     * 2.7.6 (the owner: the fire "coming from inside the mouth not from anywhere else", "bright and glowy", "all of it must be 3d"):
     * the roar opens the jaw wide and the inside of the gape lies between the jaws behind the closed mouth's tip; the breath's first
     * fire is born there, small enough for the gape, and its far end is out along the aim; every move's quads are finite, the same
     * each time for the same moment, and within a budget a phone can draw; the bands are hot to cold in order.
     */
    static void fire() {
        Check.that(DragonForm.jawAngle(0.0F, -1.0F) == 0.0F && DragonForm.jawAngle(1.0F, -1.0F) >= 55.0F, "the roar throws the jaw wide");
        DragonPose pose = new DragonPose(model);
        float[] anchor = new float[3];
        DragonForm.pose(model, shape, pose, DragonForm.DRAGON, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, -1.0F, anchor);
        float[] in = new float[6];
        Check.that(DragonForm.mouth(shape, pose, DragonForm.jawAngle(1.0F, -1.0F), in), "the dragon has a mouth");
        Check.near(Math.sqrt(in[3] * in[3] + in[4] * in[4] + in[5] * in[5]), 1.0, 1.0E-4, "the mouth's way is a direction");
        int jaw = model.bone("jaw");
        float[] g = pose.global;
        double hinge = Math.sqrt(Math.pow(in[0] - g[jaw * 12 + 3], 2) + Math.pow(in[1] - g[jaw * 12 + 7], 2) + Math.pow(in[2] - g[jaw * 12 + 11], 2));
        Check.near(hinge, DragonForm.MOUTH_DEEP, 1.0E-3, "the fire starts inside the gape, behind the jaw's tip");
        // the open jaw's way and the mouth's way are half the opening apart (the mouth's middle)
        double jx = g[jaw * 12 + 2];
        double jy = g[jaw * 12 + 6];
        double jz = g[jaw * 12 + 10];
        double jl = Math.sqrt(jx * jx + jy * jy + jz * jz);
        double apart = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, (jx * in[3] + jy * in[4] + jz * in[5]) / jl))));
        Check.near(apart, DragonForm.jawAngle(1.0F, -1.0F) / 2.0, 1.0, "the fire leaves between the jaws");

        // the breath, the mouth 30 blocks ahead of the eye looking along +z
        double[] mouth = {0.0, -2.0, 30.0};
        double[] way = {0.0, -0.6, 0.8};
        com.hexhaki.piece.dragon.UoFire.Aim aim = (ago, out) -> {
            out[0] = mouth[0];
            out[1] = mouth[1];
            out[2] = mouth[2];
            out[3] = 0.0;
            out[4] = 0.0;
            out[5] = 1.0;
            out[6] = way[0];
            out[7] = way[1];
            out[8] = way[2];
        };
        double[] box = {1e9, 1e9, 1e9, -1e9, -1e9, -1e9};
        int[] count = {0};
        boolean[] finite = {true};
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            count[0]++;
            for (int k = 0; k < 12; k++) {
                finite[0] &= Double.isFinite(xyz[k]);
                box[k % 3] = Math.min(box[k % 3], xyz[k]);
                box[3 + k % 3] = Math.max(box[3 + k % 3], xyz[k]);
            }
        };
        // a moment after it starts: all of it is still at the mouth (within the gape and the first blocks out of it)
        com.hexhaki.piece.dragon.UoFire.breath(sink, aim, 0.4, -1.0, 35.0, 3);
        Check.that(count[0] > 0 && finite[0], "the breath starts at once");
        double far = Math.max(Math.abs(box[3] - mouth[0]), Math.abs(box[0] - mouth[0]));
        Check.that(far < 2.5 && box[1] > mouth[1] - 2.5 && box[5] < mouth[2] + 3.0 && box[2] > mouth[2] - 2.5,
            "the breath's first fire is at the mouth, nowhere else (" + java.util.Arrays.toString(box) + ")");
        // in full: out to the aim's end, within the budget
        java.util.Arrays.fill(box, 0, 3, 1e9);
        java.util.Arrays.fill(box, 3, 6, -1e9);
        count[0] = 0;
        com.hexhaki.piece.dragon.UoFire.breath(sink, aim, 20.0, -1.0, 35.0, 3);
        Check.that(finite[0] && box[5] > mouth[2] + 30.0, "the breath reaches out along the aim (" + box[5] + ")");
        Check.that(count[0] < 16000, "the breath fits a phone's budget (" + count[0] + " quads)");
        int full = count[0];
        count[0] = 0;
        com.hexhaki.piece.dragon.UoFire.breath(sink, aim, 20.0, -1.0, 35.0, 3);
        Check.that(count[0] == full, "the same moment draws the same fire");
        // the rest: finite, within budget
        String[] moves = {"orb", "blast", "explosion", "air", "airBurst", "tornado", "bite"};
        for (String move : moves) {
            for (double age = 0.0; age <= 30.0; age += 2.5) {
                count[0] = 0;
                switch (move) {
                    case "orb" -> com.hexhaki.piece.dragon.UoFire.orb(sink, aim, age * 2.0, Math.min(1.0, age / 30.0), 5);
                    case "blast" -> com.hexhaki.piece.dragon.UoFire.fireball(sink, 0, 0, 20 + age, 0, 0, 3.6, 2.4, age, age * 3.6, true, 5);
                    case "explosion" -> com.hexhaki.piece.dragon.UoFire.explosion(sink, 0, -3, 25, 8.0, age, 5);
                    case "air" -> com.hexhaki.piece.dragon.UoFire.airShot(sink, 0, 0, 10 + age, 0, 0, 4.2, 1.3, age, age * 4.2, 5);
                    case "airBurst" -> com.hexhaki.piece.dragon.UoFire.airBurst(sink, 0, 0, 12, 0, 0, 1, 1.6, age / 3.0, 5);
                    case "tornado" -> com.hexhaki.piece.dragon.UoFire.tornado(sink, 0, -3, 20, age, Math.min(1.0, age / 6.0), 5);
                    default -> com.hexhaki.piece.dragon.UoFire.bite(sink, aim, age / 30.0, 5);
                }
                Check.that(finite[0], move + " is finite at " + age);
                Check.that(count[0] < 16000, move + " fits a phone's budget at " + age + " (" + count[0] + " quads)");
            }
        }
        // 2.7.7 (the owner: Blast Breath "a longer line of fire, like wayyyy longer"): as it bursts 35 blocks out, its whole way burns
        java.util.Arrays.fill(box, 0, 3, 1e9);
        java.util.Arrays.fill(box, 3, 6, -1e9);
        count[0] = 0;
        com.hexhaki.piece.dragon.UoFire.line(sink, 0, 0, 5, 0, 0, 1, 3.2, 35.0 / 3.2, 35.0, 1.05, 14.0, 0, 0, 0, 9);
        Check.that(finite[0] && box[2] < 5.0 + 3.0 && box[5] > 5.0 + 32.0, "Blast Breath's line of fire runs its whole way (" + box[2] + " to "
            + box[5] + ")");
        Check.that(count[0] < 16000, "the line fits a phone's budget (" + count[0] + " quads)");
        count[0] = 0;
        com.hexhaki.piece.dragon.UoFire.line(sink, 0, 0, 5, 0, 0, 1, 3.2, 35.0 / 3.2 + 15.0, 35.0, 1.05, 14.0, 0, 0, 0, 9);
        Check.that(count[0] == 0, "and burns out after");
        int[] bands = com.hexhaki.piece.dragon.FireMesh.BAND;
        for (int i = 1; i < bands.length; i++) {
            int a = bands[i - 1];
            int b = bands[i];
            Check.that(((a >> 16) & 255) + ((a >> 8) & 255) + (a & 255) < ((b >> 16) & 255) + ((b >> 8) & 255) + (b & 255),
                "the fire's bands brighten from cold to hot");
        }
    }

    /** The hovering dragon (model units) with its jaw {@code mouth} open and {@code bite} into its bite. */
    static float[][] posed(float mouth, float bite) {
        DragonPose pose = new DragonPose(model);
        DragonForm.pose(model, shape, pose, DragonForm.DRAGON, 0.0F, 1.0F, 0.0F, 1.0F, mouth, bite, new float[3]);
        float[][] out = new float[model.parts.length][];
        for (int i = 0; i < model.parts.length; i++) {
            out[i] = new float[model.parts[i].vertices() * 3];
            pose.skin(model.parts[i], out[i], new float[out[i].length]);
        }
        return out;
    }

    /** The dragon's points placed at the player's feet (blocks), facing yaw 0. */
    static float[][] placed(int phase, float seconds, float hover, float flight, float age) {
        DragonPose pose = new DragonPose(model);
        float[] anchor = new float[3];
        DragonForm.pose(model, shape, pose, phase, seconds, hover, flight, age, anchor);
        float[] shift = new float[3];
        DragonForm.shift(shape, phase, seconds, shift);
        float[] place = new float[12];
        DragonForm.placement(anchor, shift, DragonForm.grow(phase, seconds), 0.0F, 0.0F, 0.0F, place);
        float[][] out = new float[model.parts.length][];
        for (int i = 0; i < model.parts.length; i++) {
            DragonModel.Part part = model.parts[i];
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            pose.skin(part, pos, nrm);
            for (int v = 0; v < pos.length; v += 3) {
                float x = pos[v];
                float y = pos[v + 1];
                float z = pos[v + 2];
                for (int k = 0; k < 3; k++) {
                    pos[v + k] = place[k * 4] * x + place[k * 4 + 1] * y + place[k * 4 + 2] * z + place[k * 4 + 3];
                }
            }
            out[i] = pos;
        }
        return out;
    }

    /** The distance from point {@code o} of {@code p} to the segment a-b. */
    static float segment(float[] p, int o, float[] a, float[] b) {
        float abx = b[0] - a[0];
        float aby = b[1] - a[1];
        float abz = b[2] - a[2];
        float apx = p[o] - a[0];
        float apy = p[o + 1] - a[1];
        float apz = p[o + 2] - a[2];
        float t = Math.max(0.0F, Math.min(1.0F, (apx * abx + apy * aby + apz * abz) / Math.max(1e-9F, abx * abx + aby * aby + abz * abz)));
        float dx = apx - abx * t;
        float dy = apy - aby * t;
        float dz = apz - abz * t;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** The furthest any point moved between two posings (blocks). */
    static float gap(float[][] a, float[][] b) {
        float most = 0.0F;
        for (int i = 0; i < a.length; i++) {
            for (int v = 0; v < a[i].length; v += 3) {
                float dx = a[i][v] - b[i][v];
                float dy = a[i][v + 1] - b[i][v + 1];
                float dz = a[i][v + 2] - b[i][v + 2];
                most = Math.max(most, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
            }
        }
        return most;
    }

    /** The furthest point from {@code c}. */
    static float far(float[][] a, float[] c) {
        float most = 0.0F;
        for (float[] part : a) {
            for (int v = 0; v < part.length; v += 3) {
                float dx = part[v] - c[0];
                float dy = part[v + 1] - c[1];
                float dz = part[v + 2] - c[2];
                most = Math.max(most, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
            }
        }
        return most;
    }

    /**
     * 2.8.5 (the owner: "make them higher ... up and down more, like more angular", "I don't mean like increase the amount of
     * wobbles"): the flight wave rises and falls about twice as far as 2.8.4's (2.5 + 4 degrees a segment, the neck 1.5), sharper than a
     * sine (straighter runs, sharper turns), its period and length unchanged.
     */
    static void wave() {
        Check.that(DragonForm.WAVE_UP >= 2.0F * 2.5F && DragonForm.WAVE_UP + DragonForm.WAVE_UP_TAIL >= 2.0F * 6.5F, "the wave twice as high");
        Check.that(DragonForm.NECK_UP >= 2.0F * 1.5F, "and the neck's");
        Check.near(DragonForm.WAVE_PERIOD, 1.6, 1e-6, "as many waves a second as before");
        Check.near(DragonForm.WAVE_SEGMENTS, 11.0, 1e-6, "and as long");
        for (int i = 0; i <= 64; i++) {
            double x = i * Math.PI * 2.0 / 64.0;
            float v = DragonForm.shape(x);
            Check.that(Math.abs(v) <= 1.0F + 1e-6F, "the wave's shape stays within its height");
            Check.that(Math.signum(v) == Math.signum((float) Math.sin(x)) || Math.abs(v) < 1e-6F, "and rises and falls with the sine");
        }
        // angular: halfway up a rise it is straighter (closer to a line) than the sine, so the turns at the top are sharper
        Check.that(DragonForm.shape(Math.PI / 6.0) < Math.sin(Math.PI / 6.0) + 0.06 && DragonForm.shape(Math.PI / 6.0) > 0.3, "straighter runs");
    }
}
