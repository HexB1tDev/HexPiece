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
            try (InputStream in = zip.getInputStream(entry)) {
                model = DragonModel.read(in);
            }
        }
        shape = new DragonForm.Shape(model);
        keys();
        fire();
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
