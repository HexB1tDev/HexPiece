package com.hexhaki.piece;

import com.hexhaki.piece.armor.PieceArmor;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * 2.3.1: the owner's One Piece armor (input/OnePiece_Armor_1.20.1.zip). The pieces are the pack's (its INTEGRATION.md table and
 * rig_manifest.json: each item's slot, model style and texture), a Marine NPC wears only the Marine outfit (the owner: "NPCS for now only
 * spawn with the Marine Armor (not admiral or strawhat or black coat)"), every item has its model, icon and name, and the pack's classes as
 * build.py translates them name nothing in Mojang's names any more.
 */
public final class ArmorTests {
    static final Path ZIP = Path.of("input/OnePiece_Armor_1.20.1.zip");
    static final String ROOT = "OnePiece_Armor_1.20.1/";
    static final Path RESOURCES = Path.of("src/main/resources/assets/hexhaki");

    public static void main(String[] args) throws Exception {
        pack();
        npcs();
        items();
        translated();
        clothes();
        Check.done("ArmorTests");
    }

    static String slotOf(String type) {
        return switch (type) {
            case "HELMET", "HEAD" -> "HEAD";
            case "CHESTPLATE", "CHEST" -> "CHEST";
            case "LEGGINGS", "LEGS" -> "LEGS";
            case "BOOTS", "FEET" -> "FEET";
            default -> throw new AssertionError("unknown slot " + type);
        };
    }

    static void pack() throws Exception {
        try (ZipFile zip = new ZipFile(ZIP.toFile())) {
            String integration = new String(zip.getInputStream(zip.getEntry(ROOT + "INTEGRATION.md")).readAllBytes(), StandardCharsets.UTF_8);
            // | Marine cap | HELMET | MARINE | marine_outfit.png |
            Matcher row = Pattern.compile("(?m)^\\| ([A-Za-z ]+) \\| ([A-Z]+) \\| ([A-Z_]+) \\| ([a-z_]+\\.png) \\|$").matcher(integration);
            Map<String, String[]> table = new HashMap<>();
            while (row.find()) {
                table.put(row.group(1).trim().toLowerCase().replace(' ', '_'), new String[] {row.group(2), row.group(3), row.group(4)});
            }
            Check.that(table.size() == PieceArmor.values().length, "every piece of the pack's table is an item (" + table.size() + ")");
            for (PieceArmor piece : PieceArmor.values()) {
                String[] want = table.get(piece.key());
                Check.that(want != null, piece + ": in the pack's table");
                Check.that(slotOf(want[0]).equals(piece.slot().name()), piece + ": worn where the pack says (" + want[0] + ")");
                Check.that(want[1].equals(piece.style()), piece + ": drawn in the pack's style " + want[1]);
                Check.that(want[2].equals(piece.textureFile()), piece + ": with the pack's texture " + want[2]);
                Check.that(zip.getEntry(ROOT + "forge_1.20.1/src/main/resources/assets/hexpiece/textures/models/armor/" + piece.textureFile()) != null,
                    piece + ": its texture is in the pack");
                Check.that(piece.texture().equals("hexhaki:textures/models/armor/" + piece.textureFile()), piece + ": drawn from under hexhaki");
            }
            String manifest = new String(zip.getInputStream(zip.getEntry(ROOT + "rig_manifest.json")).readAllBytes(), StandardCharsets.UTF_8);
            for (PieceArmor piece : PieceArmor.values()) {
                Matcher m = Pattern.compile("\"" + piece.key() + "\": \"([A-Z]+)\"").matcher(manifest);
                Check.that(m.find() && slotOf(m.group(1)).equals(piece.slot().name()), piece + ": the rig manifest's slot agrees");
            }
            String model = new String(zip.getInputStream(zip.getEntry(ROOT + "forge_1.20.1/src/main/java/dev/hexb1t/onepiecearmor/client/OnePieceArmorModel.java"))
                .readAllBytes(), StandardCharsets.UTF_8);
            for (PieceArmor piece : PieceArmor.values()) {
                Check.that(model.contains("enum Style { MARINE, ADMIRAL, BLACKBEARD, STRAW_HAT }") && model.contains(piece.style()),
                    piece + ": its style is one of the pack's model's");
            }
        }
        // build.py packs the same pieces (models and icons by key)
        String build = Files.readString(Path.of("build.py"));
        Matcher keys = Pattern.compile("ARMOR_KEYS = \\[([^\\]]*)\\]").matcher(build);
        Check.that(keys.find(), "build.py lists the armor keys");
        Set<String> listed = new HashSet<>();
        for (String k : keys.group(1).split(",")) {
            listed.add(k.trim().replace("\"", ""));
        }
        Set<String> ours = new HashSet<>();
        for (PieceArmor piece : PieceArmor.values()) {
            ours.add(piece.key());
        }
        Check.that(listed.equals(ours), "build.py's ARMOR_KEYS are the pieces: " + listed);
    }

    static void npcs() {
        int issued = 0;
        for (PieceArmor piece : PieceArmor.values()) {
            if (piece.marineIssue()) {
                issued++;
                Check.that(piece.style().equals("MARINE"), piece + ": a Marine wears only the Marine outfit");
            }
        }
        Check.that(issued == 4, "the Marine outfit: cap, shirt, trousers, boots");
        Check.that(!PieceArmor.ADMIRAL_COAT.marineIssue() && !PieceArmor.STRAW_HAT.marineIssue() && !PieceArmor.BLACKBEARD_COAT.marineIssue(),
            "no NPC spawns in the admiral's coat, the straw hat or the black coat");
        PieceArmor[] outfit = PieceArmor.marineOutfit();
        String[] order = {"HEAD", "CHEST", "LEGS", "FEET"};
        Check.that(outfit.length == 4, "four pieces");
        for (int i = 0; i < 4; i++) {
            Check.that(outfit[i].slot().name().equals(order[i]) && outfit[i].marineIssue(), "the outfit head to feet (SeaFighter equips it so)");
        }
    }

    static void items() throws Exception {
        String lang = Files.readString(RESOURCES.resolve("lang/en_us.json"));
        for (PieceArmor piece : PieceArmor.values()) {
            Path model = RESOURCES.resolve("models/item/" + piece.key() + ".json");
            Check.that(Files.exists(model), piece + ": its item model");
            Check.that(Files.readString(model).contains("\"hexhaki:item/armor/" + piece.key() + "\""), piece + ": its icon in the item atlas (textures/item)");
            Path icon = RESOURCES.resolve("textures/item/armor/" + piece.key() + ".png");
            Check.that(Files.exists(icon), piece + ": its icon");
            BufferedImage image = ImageIO.read(icon.toFile());
            Check.that(image.getWidth() == 32 && image.getHeight() == 32, piece + ": a 32 px icon, as the katanas'");
            int opaque = 0;
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    opaque += (image.getRGB(x, y) >>> 24) > 200 ? 1 : 0;
                }
            }
            Check.that(opaque > 60 && opaque < 1000, piece + ": the icon shows the piece on a clear ground (" + opaque + " px)");
            Check.that(lang.contains("\"item.hexhaki." + piece.key() + "\": \"" + piece.display() + "\""), piece + ": its name");
        }
    }

    static void translated() throws Exception {
        Path dir = Path.of("build/generated/com/hexhaki/piece/client/armor");
        for (String name : new String[] {"OnePieceArmorModel", "OnePieceArmorExtensions"}) {
            String code = Files.readString(dir.resolve(name + ".java"));
            Check.that(code.startsWith("// Generated by build.py from input/OnePiece_Armor_1.20.1.zip"), name + ": generated from the pack");
            Check.that(code.contains("package com.hexhaki.piece.client.armor;"), name + ": in a client package");
            for (String mojang : new String[] {".addOrReplaceChild(", ".texOffs(", ".addBox(", "offsetAndRotation(", "PartPose.ZERO", ".getRoot()",
                "LayerDefinition.create(", ".bakeRoot()", ".copyFrom(", ".visible", ".getChild(", "setAllVisible(", ".getModelName()", "attackTime",
                "crouching", "leftArmPose", "rightArmPose", "rightArm.", "leftLeg."}) {
                Check.that(!code.contains(mojang), name + ": no Mojang name left (" + mojang + ")");
            }
        }
        String model = Files.readString(dir.resolve("OnePieceArmorModel.java"));
        Check.that(model.contains("m_171599_(\"head\"") && model.contains("m_171599_(\"body\"") && model.contains("m_171324_(\"marine_shirt\")"),
            "part names (strings) are left as they are");
    }

    /**
     * 3.8.9: the owner's clothes replacements (art/clothes, tools/clothes_bake.py) and how a coat moves (armor/CoatCloth; the owner: "The
     * arms cannot clip through the coats, when walking the coat should be pushed back like the wind is pushing it", "the arms should never
     * interact with the coat's arms", "head is clipping through the coat"): every piece baked; standing still a coat is the owner's; however
     * the arms and the head are posed no point of the coat's body is left inside them; blown back it goes back, its front never behind
     * the torso's front.
     */
    static void clothes() throws Exception {
        for (PieceArmor piece : PieceArmor.values()) {
            Check.that(Files.exists(Path.of("art/clothes/" + piece.key() + ".obj")), piece + ": its clothes baked");
        }
        Check.that(Files.exists(Path.of("art/clothes/clothes.png")), "the clothes' atlas");
        Check.that(PieceArmor.CLOTHES_TEXTURE.equals("hexhaki:textures/clothes/clothes.png"), "worn with the clothes' atlas");
        for (String coat : new String[] {"admiral_coat", "blackbeard_coat"}) {
            java.util.List<float[]> vs = new java.util.ArrayList<>();
            Map<Integer, Byte> kinds = new HashMap<>();
            String group = "";
            for (String line : Files.readAllLines(Path.of("art/clothes/" + coat + ".obj"))) {
                String[] t = line.trim().split("\\s+");
                if (t[0].equals("v")) {
                    vs.add(new float[] {Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])});
                } else if (t[0].equals("g")) {
                    group = t[1];
                } else if (t[0].equals("f")) {
                    byte k = group.endsWith("_sleeve") ? (group.startsWith("left") ? com.hexhaki.piece.armor.CoatCloth.SLEEVE_LEFT
                        : com.hexhaki.piece.armor.CoatCloth.SLEEVE_RIGHT) : group.startsWith("body_") ? (group.endsWith("_in")
                        ? com.hexhaki.piece.armor.CoatCloth.LINING_KIND : com.hexhaki.piece.armor.CoatCloth.BODY) : (byte) -1;
                    for (int i = 1; i < t.length; i++) {
                        kinds.put(Integer.parseInt(t[i].split("/")[0]) - 1, k);
                    }
                }
            }
            int n = vs.size();
            float[] rest = new float[n * 3];
            byte[] kind = new byte[n];
            for (int i = 0; i < n; i++) {
                System.arraycopy(vs.get(i), 0, rest, i * 3, 3);
                kind[i] = kinds.getOrDefault(i, (byte) -1);
            }
            float[] out = new float[n * 3];
            com.hexhaki.piece.armor.CoatCloth.body(rest, kind, n, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, out);
            float most = 0.0F;
            for (int i = 0; i < n; i++) {
                if (kind[i] == com.hexhaki.piece.armor.CoatCloth.BODY || kind[i] == com.hexhaki.piece.armor.CoatCloth.LINING_KIND) {
                    for (int k = 0; k < 3; k++) {
                        most = Math.max(most, Math.abs(out[i * 3 + k] - rest[i * 3 + k]));
                    }
                }
            }
            Check.that(most < 1.0E-3F, coat + ": standing still, the coat hangs as the owner made it (moved " + most + " px)");
            // blown back: the bottom goes back; the front over the torso stays ahead of it
            com.hexhaki.piece.armor.CoatCloth.body(rest, kind, n, 1.0F, 0.0F, 0.7F, 0.6F, -0.6F, out);
            double back = 0.0;
            int bottom = 0;
            for (int i = 0; i < n; i++) {
                if (kind[i] != com.hexhaki.piece.armor.CoatCloth.BODY) {
                    continue;
                }
                if (rest[i * 3 + 1] > 17.0F) {
                    back += out[i * 3 + 2] - rest[i * 3 + 2];
                    bottom++;
                }
                if (rest[i * 3 + 2] < -0.5F && Math.abs(rest[i * 3]) < 3.5F && rest[i * 3 + 1] < 11.0F && rest[i * 3 + 1] > 0.5F) {
                    Check.that(out[i * 3 + 2] <= -2.0F + 1.0E-3F, coat + ": blown back, its front stays ahead of the torso");
                }
            }
            Check.that(bottom > 0 && back / bottom > 4.0, coat + ": walking, the coat is blown back (" + (back / Math.max(1, bottom)) + " px)");
            // the arms and the head, however posed, never inside the coat's body
            float[] ways = com.hexhaki.piece.armor.CoatCloth.restWays(rest, n, false);
            // at rest the arms and the head push nothing (the bake sat the coat on the game's arms and round its head)
            float[] still = rest.clone();
            float[][] restFrames = {rigid(-5.0F, 2.0F, 0.0F, 0, 0, 0), rigid(-5.0F, 2.0F, 0.0F, 0, 0, 0), rigid(5.0F, 2.0F, 0.0F, 0, 0, 0),
                rigid(5.0F, 2.0F, 0.0F, 0, 0, 0)};
            float[] inner = com.hexhaki.piece.armor.CoatCloth.ringRest(rest, n);
            com.hexhaki.piece.armor.CoatCloth.keepOut(still, kind, rest, n, ways, restFrames, rigid(0, 0, 0, 0, 0, 0), inner, false);
            float shift = 0.0F;
            for (int i = 0; i < still.length; i++) {
                shift = Math.max(shift, Math.abs(still[i] - rest[i]));
            }
            Check.that(shift < 1.0E-3F, coat + ": at rest the arms and the head push nothing (" + shift + " px)");
            float[][] poses = {{-1.2F, 0.0F, 0.0F}, {1.0F, 0.3F, 0.0F}, {-0.4F, 0.0F, 1.2F}, {0.0F, 0.0F, -1.3F}, {-1.6F, -0.5F, 0.4F},
                {0.0F, 0.0F, 0.0F}, {0.6F, -0.8F, 0.9F}};
            for (float[] arm : poses) {
                float[] model = out.clone();
                float[][] frames = new float[4][];
                for (int side = 0; side < 2; side++) {
                    float[] f = rigid(side == 0 ? -5.0F : 5.0F, 2.0F, 0.0F, arm[0] * (side == 0 ? 1 : -1), arm[1], arm[2] * (side == 0 ? 1 : -1));
                    frames[side * 2] = f;
                    frames[side * 2 + 1] = f;
                }
                float[] head = rigid(0.0F, 0.0F, 0.0F, arm[0] * 0.5F, arm[1] + 0.9F, 0.0F);
                com.hexhaki.piece.armor.CoatCloth.keepOut(model, kind, rest, n, ways, frames, head, inner, false);
                int inside = 0;
                for (int i = 0; i < n; i++) {
                    if (kind[i] != com.hexhaki.piece.armor.CoatCloth.BODY && kind[i] != com.hexhaki.piece.armor.CoatCloth.LINING_KIND) {
                        continue;
                    }
                    for (int side = 0; side < 2; side++) {
                        float[] l = local(frames[side * 2], model, i);
                        float x0 = side == 0 ? -3.0F : -1.0F;
                        if (l[0] > x0 + 0.01F && l[0] < x0 + 3.99F && l[1] > -1.99F && l[1] < 9.99F && Math.abs(l[2]) < 1.99F) {
                            inside++;
                        }
                    }
                    float[] h = local(head, model, i);
                    if (rest[i * 3 + 1] < 0.0F && Math.abs(h[0]) < 3.99F && Math.abs(h[2]) < 3.99F && h[1] > -7.99F && h[1] < -0.01F) {
                        inside++;
                    }
                }
                Check.that(inside == 0, coat + ": no point of the coat left inside an arm or the head (" + inside + ")");
            }
        }
    }

    /** A rigid frame (3 x 4) at (x, y, z) turned Rz Ry Rx (radians), as ClothesModel.frame makes a part's. */
    static float[] rigid(float x, float y, float z, float rx, float ry, float rz) {
        double cx = Math.cos(rx);
        double sx = Math.sin(rx);
        double cy = Math.cos(ry);
        double sy = Math.sin(ry);
        double cz = Math.cos(rz);
        double sz = Math.sin(rz);
        return new float[] {(float) (cz * cy), (float) (cz * sy * sx - sz * cx), (float) (cz * sy * cx + sz * sx), x, (float) (sz * cy),
            (float) (sz * sy * sx + cz * cx), (float) (sz * sy * cx - cz * sx), y, (float) (-sy), (float) (cy * sx), (float) (cy * cx), z};
    }

    /** Point i of {@code p} in a rigid frame's own coordinates. */
    static float[] local(float[] m, float[] p, int i) {
        float x = p[i * 3] - m[3];
        float y = p[i * 3 + 1] - m[7];
        float z = p[i * 3 + 2] - m[11];
        return new float[] {m[0] * x + m[4] * y + m[8] * z, m[1] * x + m[5] * y + m[9] * z, m[2] * x + m[6] * y + m[10] * z};
    }
}
