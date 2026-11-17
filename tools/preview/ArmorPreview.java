import dev.hexb1t.onepiecearmor.client.OnePieceArmorModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * 2.3.1: the owner's One Piece armor off-game: the pack's own OnePieceArmorModel (input/OnePiece_Armor_1.20.1.zip, its Mojang-named source
 * compiled against the stand-ins in tools/preview/armorfake) on the owner's skin (art/user_skin.png, vanilla's player model), drawn as the
 * game's HumanoidArmorLayer draws it: one pass per slot after the model copies the player's pose (copyPoseFrom), cutout, both faces, the
 * pack's 1024 px atlas at 256 logical. Cubes laid out as vanilla's ModelPart.Cube (corners, the six faces' UVs); parts placed as
 * ModelPart.translateAndRotate (pivot in pixels, then Z, Y, X turns); the model space put in the world as the entity renderer does
 * (scale -1, -1, 1 and 1.501 up, turned to face +z). Also the items' 32 px icons.
 *
 * <p>javac -d build/armorpreview $(find tools/preview/armorfake -name "*.java") OnePieceArmorModel.java tools/preview/ArmorPreview.java
 * && java -cp build/armorpreview ArmorPreview sheet|icons out [skin.png]
 */
public final class ArmorPreview {
    static final String ZIP = "input/OnePiece_Armor_1.20.1.zip";
    static final String TEXTURES = "OnePiece_Armor_1.20.1/forge_1.20.1/src/main/resources/assets/hexpiece/textures/models/armor/";
    static final Map<String, BufferedImage> ATLAS = new HashMap<>();
    static BufferedImage skin;

    record Piece(String key, OnePieceArmorModel.Style style, EquipmentSlot slot, String texture) {
    }

    static final Piece[] PIECES = {
        new Piece("marine_cap", OnePieceArmorModel.Style.MARINE, EquipmentSlot.HEAD, "marine_outfit"),
        new Piece("marine_shirt", OnePieceArmorModel.Style.MARINE, EquipmentSlot.CHEST, "marine_outfit"),
        new Piece("marine_trousers", OnePieceArmorModel.Style.MARINE, EquipmentSlot.LEGS, "marine_outfit"),
        new Piece("marine_boots", OnePieceArmorModel.Style.MARINE, EquipmentSlot.FEET, "marine_outfit"),
        new Piece("admiral_coat", OnePieceArmorModel.Style.ADMIRAL, EquipmentSlot.CHEST, "admiral_coat"),
        new Piece("straw_hat", OnePieceArmorModel.Style.STRAW_HAT, EquipmentSlot.HEAD, "straw_hat"),
        new Piece("blackbeard_coat", OnePieceArmorModel.Style.BLACKBEARD, EquipmentSlot.CHEST, "blackbeard_coat")};

    public static void main(String[] args) throws Exception {
        try (ZipFile zip = new ZipFile(ZIP)) {
            for (String name : new String[] {"marine_outfit", "admiral_coat", "straw_hat", "blackbeard_coat"}) {
                ATLAS.put(name, ImageIO.read(new ByteArrayInputStream(zip.getInputStream(zip.getEntry(TEXTURES + name + ".png")).readAllBytes())));
            }
        }
        skin = ImageIO.read(new File(args.length > 2 ? args[2] : "art/user_skin.png"));
        if (args[0].equals("icons")) {
            icons(new File(args[1]));
        } else {
            sheet(new File(args[1]));
        }
    }

    static boolean slim(BufferedImage s) {
        return (s.getRGB(54, 20) >>> 24) == 0 && (s.getRGB(55, 25) >>> 24) == 0;
    }

    // --- vanilla's player model (PlayerModel.createMesh), its outer layer as children of each limb (it copies the limb's pose) ---
    static HumanoidModel<LivingEntity> player(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation none = new CubeDeformation(0.0F);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, none), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.5F)), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, none), PartPose.ZERO);
        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4, 0, -2, 8, 12, 4, new CubeDeformation(0.25F)), PartPose.ZERO);
        float w = slim ? 3 : 4;
        float armY = slim ? 2.5F : 2.0F;
        PartDefinition ra = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(slim ? -2 : -3, -2, -2, w, 12, 4, none),
            PartPose.offset(-5, armY, 0));
        ra.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(slim ? -2 : -3, -2, -2, w, 12, 4, new CubeDeformation(0.25F)),
            PartPose.ZERO);
        PartDefinition la = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1, -2, -2, w, 12, 4, none),
            PartPose.offset(5, armY, 0));
        la.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1, -2, -2, w, 12, 4, new CubeDeformation(0.25F)), PartPose.ZERO);
        PartDefinition rl = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4, none),
            PartPose.offset(-1.9F, 12, 0));
        rl.addOrReplaceChild("pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4, new CubeDeformation(0.25F)), PartPose.ZERO);
        PartDefinition ll = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2, 0, -2, 4, 12, 4, none),
            PartPose.offset(1.9F, 12, 0));
        ll.addOrReplaceChild("pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2, 0, -2, 4, 12, 4, new CubeDeformation(0.25F)), PartPose.ZERO);
        return new HumanoidModel<>(LayerDefinition.create(mesh, 64, 64).bakeRoot());
    }

    /** A pose: idle (arms a touch out, as vanilla's breathing leaves them), or a step (vanilla's walk at full swing). */
    static void pose(HumanoidModel<LivingEntity> m, String pose) {
        m.rightArm.zRot = 0.05F;
        m.leftArm.zRot = -0.05F;
        if (pose.equals("walk")) {
            m.rightArm.xRot = -0.65F;
            m.leftArm.xRot = 0.65F;
            m.rightLeg.xRot = 0.7F;
            m.leftLeg.xRot = -0.7F;
        }
    }

    // --- drawing ---
    static final class Raster {
        final int w;
        final int h;
        final float[] depth;
        final int[] argb;
        final double[] eye;
        final double[] fw;
        final double[] rt;
        final double[] up;
        final double focal;
        final boolean ortho;
        final double scale;

        Raster(int w, int h, double[] eye, double[] look, double fovDeg, boolean ortho, double orthoHeight, int background) {
            this.w = w;
            this.h = h;
            this.depth = new float[w * h];
            this.argb = new int[w * h];
            java.util.Arrays.fill(this.depth, Float.MAX_VALUE);
            java.util.Arrays.fill(this.argb, background);
            this.eye = eye;
            double[] f = norm(sub(look, eye));
            double[] r = norm(cross(f, new double[] {0, 1, 0}));
            this.fw = f;
            this.rt = r;
            this.up = cross(r, f);
            this.focal = (h / 2.0) / Math.tan(Math.toRadians(fovDeg) / 2);
            this.ortho = ortho;
            this.scale = h / orthoHeight;
        }

        double[] project(double[] p) {
            double[] q = sub(p, this.eye);
            double x = dot(q, this.rt);
            double y = dot(q, this.up);
            double z = dot(q, this.fw);
            if (z < 0.05) {
                return null;
            }
            if (this.ortho) {
                return new double[] {this.w / 2.0 + x * this.scale, this.h / 2.0 - y * this.scale, 1.0 / z};
            }
            return new double[] {this.w / 2.0 + x / z * this.focal, this.h / 2.0 - y / z * this.focal, 1.0 / z};
        }

        void tri(double[] p0, double[] p1, double[] p2, double[] t0, double[] t1, double[] t2, BufferedImage tex, double light) {
            double[][] s = {project(p0), project(p1), project(p2)};
            for (double[] q : s) {
                if (q == null) {
                    return;
                }
            }
            double area = (s[1][0] - s[0][0]) * (s[2][1] - s[0][1]) - (s[2][0] - s[0][0]) * (s[1][1] - s[0][1]);
            if (Math.abs(area) < 1.0E-12) {
                return;
            }
            int x0 = (int) Math.max(0, Math.floor(Math.min(s[0][0], Math.min(s[1][0], s[2][0]))));
            int x1 = (int) Math.min(this.w - 1, Math.ceil(Math.max(s[0][0], Math.max(s[1][0], s[2][0]))));
            int y0 = (int) Math.max(0, Math.floor(Math.min(s[0][1], Math.min(s[1][1], s[2][1]))));
            int y1 = (int) Math.min(this.h - 1, Math.ceil(Math.max(s[0][1], Math.max(s[1][1], s[2][1]))));
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    double px = x + 0.5;
                    double py = y + 0.5;
                    double l0 = ((s[1][0] - px) * (s[2][1] - py) - (s[2][0] - px) * (s[1][1] - py)) / area;
                    double l1 = ((s[2][0] - px) * (s[0][1] - py) - (s[0][0] - px) * (s[2][1] - py)) / area;
                    double l2 = 1 - l0 - l1;
                    if (l0 < 0 || l1 < 0 || l2 < 0) {
                        continue;
                    }
                    double iz = l0 * s[0][2] + l1 * s[1][2] + l2 * s[2][2];
                    float z = (float) (1.0 / iz);
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    double k0 = this.ortho ? l0 : l0 * s[0][2] / iz;
                    double k1 = this.ortho ? l1 : l1 * s[1][2] / iz;
                    double k2 = this.ortho ? l2 : l2 * s[2][2] / iz;
                    double u = t0[0] * k0 + t1[0] * k1 + t2[0] * k2;
                    double v = t0[1] * k0 + t1[1] * k1 + t2[1] * k2;
                    int tx = Math.max(0, Math.min(tex.getWidth() - 1, (int) Math.floor(u * tex.getWidth() - 1.0E-6)));
                    int ty = Math.max(0, Math.min(tex.getHeight() - 1, (int) Math.floor(v * tex.getHeight() - 1.0E-6)));
                    int c = tex.getRGB(tx, ty);
                    if ((c >>> 24) < 26) {
                        continue; // cutout
                    }
                    int r = (int) Math.min(255, ((c >> 16) & 255) * light);
                    int g = (int) Math.min(255, ((c >> 8) & 255) * light);
                    int b = (int) Math.min(255, (c & 255) * light);
                    this.argb[i] = 0xFF000000 | r << 16 | g << 8 | b;
                    this.depth[i] = z;
                }
            }
        }
    }

    /** A 4x4 transform (row-major), applied to points. */
    static double[] mul(double[] a, double[] b) {
        double[] m = new double[16];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                double s = 0;
                for (int k = 0; k < 4; k++) {
                    s += a[r * 4 + k] * b[k * 4 + c];
                }
                m[r * 4 + c] = s;
            }
        }
        return m;
    }

    static double[] translate(double x, double y, double z) {
        return new double[] {1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1};
    }

    static double[] rotX(double a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[] {1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1};
    }

    static double[] rotY(double a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[] {c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1};
    }

    static double[] rotZ(double a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[] {c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
    }

    static double[] apply(double[] m, double x, double y, double z) {
        return new double[] {m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11]};
    }

    /** Model space (pixels, y down, the front -z) into the world (blocks, y up, facing +z at the origin), as the entity renderer puts it. */
    static final double[] WORLD = mul(new double[] {1.0 / 16, 0, 0, 0, 0, -1.0 / 16, 0, 1.501, 0, 0, -1.0 / 16, 0, 0, 0, 0, 1}, new double[] {
        1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});

    static void part(Raster r, ModelPart part, double[] m, BufferedImage tex) {
        if (!part.visible) {
            return;
        }
        double[] here = mul(m, translate(part.x, part.y, part.z));
        here = mul(here, mul(rotZ(part.zRot), mul(rotY(part.yRot), rotX(part.xRot))));
        for (ModelPart.Cube cube : part.cubes) {
            cube(r, cube, here, tex);
        }
        for (ModelPart child : part.children.values()) {
            part(r, child, here, tex);
        }
    }

    /** Vanilla's ModelPart.Cube: eight corners (grown), six faces with their UVs (DOWN, UP, WEST, NORTH, EAST, SOUTH). */
    static void cube(Raster r, ModelPart.Cube c, double[] m, BufferedImage tex) {
        double x0 = c.x() - c.grow();
        double y0 = c.y() - c.grow();
        double z0 = c.z() - c.grow();
        double x1 = c.x() + c.w() + c.grow();
        double y1 = c.y() + c.h() + c.grow();
        double z1 = c.z() + c.d() + c.grow();
        double[][] v = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        double u = c.u();
        double vv = c.v();
        double w = c.w();
        double h = c.h();
        double d = c.d();
        double f4 = u, f5 = u + d, f6 = u + d + w, f7 = u + d + w + w, f8 = u + d + w + d, f9 = u + d + w + d + w;
        double f10 = vv, f11 = vv + d, f12 = vv + d + h;
        int[][] polys = {{5, 4, 0, 1}, {2, 3, 7, 6}, {0, 4, 7, 3}, {1, 0, 3, 2}, {5, 1, 2, 6}, {4, 5, 6, 7}};
        double[][] uvs = {{f5, f10, f6, f11}, {f6, f11, f7, f10}, {f4, f11, f5, f12}, {f5, f11, f6, f12}, {f6, f11, f8, f12}, {f8, f11, f9, f12}};
        double[][] normals = {{0, 1, 0}, {0, -1, 0}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}, {0, 0, 1}};
        double[] world = mul(WORLD, m);
        double[] l1 = norm(new double[] {0.2, 1.0, -0.7});
        double[] l2 = norm(new double[] {-0.2, 1.0, 0.7});
        for (int f = 0; f < 6; f++) {
            double[][] p = new double[4][];
            for (int k = 0; k < 4; k++) {
                double[] q = v[polys[f][k]];
                p[k] = apply(world, q[0], q[1], q[2]);
            }
            // Polygon: corner 0 (u2, v1), 1 (u1, v1), 2 (u1, v2), 3 (u2, v2)
            double[] uv = uvs[f];
            double[][] t = {{uv[2] / c.texW(), uv[1] / c.texH()}, {uv[0] / c.texW(), uv[1] / c.texH()}, {uv[0] / c.texW(), uv[3] / c.texH()},
                {uv[2] / c.texW(), uv[3] / c.texH()}};
            double[] n0 = apply(world, normals[f][0], normals[f][1], normals[f][2]);
            double[] o = apply(world, 0, 0, 0);
            double[] n = norm(sub(n0, o));
            // both faces are drawn (cutout, no cull): light by the side facing the eye
            if (dot(n, sub(p[0], r.eye)) > 0) {
                n = new double[] {-n[0], -n[1], -n[2]};
            }
            double light = Math.min(1.0, 0.4 + 0.6 * (Math.max(0, dot(n, l1)) + Math.max(0, dot(n, l2))));
            r.tri(p[0], p[1], p[2], t[0], t[1], t[2], tex, light);
            r.tri(p[0], p[2], p[3], t[0], t[2], t[3], tex, light);
        }
    }

    static void humanoid(Raster r, HumanoidModel<LivingEntity> m, BufferedImage tex) {
        double[] id = translate(0, 0, 0);
        for (ModelPart p : new ModelPart[] {m.head, m.hat, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg}) {
            part(r, p, id, tex);
        }
    }

    /** The armor of {@code style} on {@code body}: each slot it is worn in, as HumanoidArmorLayer draws them. */
    static void armor(Raster r, HumanoidModel<LivingEntity> body, OnePieceArmorModel.Style style, boolean slim, EquipmentSlot[] slots, String texture) {
        OnePieceArmorModel model = new OnePieceArmorModel(style, slim);
        for (EquipmentSlot slot : slots) {
            model.copyPoseFrom(body, slot);
            humanoid(r, model, ATLAS.get(texture));
        }
    }

    static EquipmentSlot[] slots(OnePieceArmorModel.Style style) {
        return switch (style) {
            case MARINE -> new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            case STRAW_HAT -> new EquipmentSlot[] {EquipmentSlot.HEAD};
            default -> new EquipmentSlot[] {EquipmentSlot.CHEST};
        };
    }

    static String texture(OnePieceArmorModel.Style style) {
        return switch (style) {
            case MARINE -> "marine_outfit";
            case ADMIRAL -> "admiral_coat";
            case BLACKBEARD -> "blackbeard_coat";
            case STRAW_HAT -> "straw_hat";
        };
    }

    /** A plain grey mannequin skin with Steve's (wide) arms, to show the standard fit. */
    static BufferedImage mannequin() {
        BufferedImage m = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                boolean outer = y < 16 && x >= 32 || y >= 32 && y < 48 || y >= 48 && (x < 16 || x >= 48);
                m.setRGB(x, y, outer ? 0 : 0xFF8A8F96 + ((x + y) % 2) * 0x050505);
            }
        }
        return m;
    }

    static void sheet(File out) throws Exception {
        int tw = 300;
        int th = 420;
        boolean ownSlim = slim(skin);
        OnePieceArmorModel.Style[] styles = OnePieceArmorModel.Style.values();
        String[] views = {"front", "three-quarter", "back", "walking"};
        List<BufferedImage> tiles = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        BufferedImage mannequin = mannequin();
        for (int fit = 0; fit < 2; fit++) {
            boolean slim = fit == 0 ? ownSlim : !ownSlim;
            BufferedImage s = fit == 0 ? skin : mannequin;
            for (OnePieceArmorModel.Style style : styles) {
                for (String view : views) {
                    if (fit == 1 && !(view.equals("three-quarter") || view.equals("back"))) {
                        continue;
                    }
                    double[] eye = switch (view) {
                        case "front" -> new double[] {0.0, 1.2, 3.4};
                        case "back" -> new double[] {-1.4, 1.5, -3.1};
                        case "walking" -> new double[] {2.6, 1.1, 2.2};
                        default -> new double[] {1.9, 1.5, 2.8};
                    };
                    Raster r = new Raster(tw * 2, th * 2, new double[] {eye[0] * 1.08, eye[1] + 0.08, eye[2] * 1.08}, new double[] {0, 1.02, 0}, 40,
                        false, 0, 0xFF2A2F38);
                    HumanoidModel<LivingEntity> body = player(slim);
                    pose(body, view.equals("walking") ? "walk" : "idle");
                    humanoid(r, body, s);
                    armor(r, body, style, slim, slots(style), texture(style));
                    tiles.add(down(r, tw, th));
                    labels.add(style + (fit == 0 ? " on the owner's skin (" + (slim ? "Alex" : "Steve") + ")" : " Steve fit (mannequin)") + ", " + view);
                }
            }
        }
        write(out, "The owner's One Piece armor: the pack's own model and textures as the game draws them", tiles, labels, 6, tw, th);
    }

    static BufferedImage down(Raster r, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int k = r.w / w;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = 0, rr = 0, g = 0, b = 0;
                for (int dy = 0; dy < k; dy++) {
                    for (int dx = 0; dx < k; dx++) {
                        int c = r.argb[(y * k + dy) * r.w + x * k + dx];
                        int ca = c >>> 24;
                        a += ca;
                        rr += ((c >> 16) & 255) * ca;
                        g += ((c >> 8) & 255) * ca;
                        b += (c & 255) * ca;
                    }
                }
                int n = k * k;
                if (a == 0) {
                    img.setRGB(x, y, 0);
                } else {
                    img.setRGB(x, y, (a / n) << 24 | (rr / a) << 16 | (g / a) << 8 | (b / a));
                }
            }
        }
        return img;
    }

    /** Each item's icon: the piece alone, three-quarter from the front and a little above, framed to fill 32 px. */
    static void icons(File dir) throws Exception {
        dir.mkdirs();
        for (Piece piece : PIECES) {
            HumanoidModel<LivingEntity> body = player(false);
            pose(body, "idle");
            // the legs a little apart (trousers and boots read as two), the cap and the leg pieces seen lower and more from the front
            boolean legs = piece.slot() == EquipmentSlot.LEGS || piece.slot() == EquipmentSlot.FEET;
            if (legs) {
                body.rightLeg.zRot = 0.2F;
                body.leftLeg.zRot = -0.2F;
            }
            // find the piece's extent in the view, then frame it
            double[] eye = legs ? new double[] {1.5, 1.25, 3.6} : piece.key().equals("marine_cap") ? new double[] {1.9, 1.75, 3.4} : new double[] {2.4, 2.2, 3.4};
            double[] centre = centre(piece, body);
            double[] look = centre;
            double[] dir2 = norm(sub(eye, new double[] {0, 1, 0}));
            if (piece.key().equals("marine_cap")) {
                dir2 = norm(sub(eye, new double[] {0, 1.55, 0}));
            }
            double[] e = {centre[0] + dir2[0] * 6, centre[1] + dir2[1] * 6, centre[2] + dir2[2] * 6};
            double height = extent(piece, body, e, look) * 1.08;
            int big = 256;
            Raster r = new Raster(big, big, e, look, 0, true, height, 0);
            OnePieceArmorModel model = new OnePieceArmorModel(piece.style(), false);
            model.copyPoseFrom(body, piece.slot());
            onlyPiece(model, piece);
            humanoid(r, model, ATLAS.get(piece.texture()));
            BufferedImage icon = down(r, 32, 32);
            ImageIO.write(icon, "png", new File(dir, piece.key() + ".png"));
            System.out.println(new File(dir, piece.key() + ".png"));
        }
    }

    /** A Marine piece alone: the cap, the shirt, the trousers or the boots (copyPoseFrom shows the waistband with the trousers). */
    static void onlyPiece(OnePieceArmorModel model, Piece piece) {
    }

    static double[] centre(Piece piece, HumanoidModel<LivingEntity> body) {
        double[] box = bounds(piece, body);
        return new double[] {(box[0] + box[3]) / 2, (box[1] + box[4]) / 2, (box[2] + box[5]) / 2};
    }

    /** The piece's bounds in the world (every visible cube corner). */
    static double[] bounds(Piece piece, HumanoidModel<LivingEntity> body) {
        OnePieceArmorModel model = new OnePieceArmorModel(piece.style(), false);
        model.copyPoseFrom(body, piece.slot());
        double[] box = {1e9, 1e9, 1e9, -1e9, -1e9, -1e9};
        for (ModelPart p : new ModelPart[] {model.head, model.hat, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg}) {
            corners(p, translate(0, 0, 0), box);
        }
        return box;
    }

    static void corners(ModelPart part, double[] m, double[] box) {
        if (!part.visible) {
            return;
        }
        double[] here = mul(m, translate(part.x, part.y, part.z));
        here = mul(here, mul(rotZ(part.zRot), mul(rotY(part.yRot), rotX(part.xRot))));
        double[] world = mul(WORLD, here);
        for (ModelPart.Cube c : part.cubes) {
            for (int i = 0; i < 8; i++) {
                double[] p = apply(world, (i & 1) == 0 ? c.x() : c.x() + c.w(), (i & 2) == 0 ? c.y() : c.y() + c.h(), (i & 4) == 0 ? c.z() : c.z() + c.d());
                for (int k = 0; k < 3; k++) {
                    box[k] = Math.min(box[k], p[k]);
                    box[k + 3] = Math.max(box[k + 3], p[k]);
                }
            }
        }
        for (ModelPart child : part.children.values()) {
            corners(child, here, box);
        }
    }

    /** How tall (blocks) the piece's bounds stand in an orthographic view from {@code eye} toward {@code look}, the larger of its width and height. */
    static double extent(Piece piece, HumanoidModel<LivingEntity> body, double[] eye, double[] look) {
        double[] box = bounds(piece, body);
        double[] f = norm(sub(look, eye));
        double[] r = norm(cross(f, new double[] {0, 1, 0}));
        double[] u = cross(r, f);
        double minX = 1e9, maxX = -1e9, minY = 1e9, maxY = -1e9;
        for (int i = 0; i < 8; i++) {
            double[] p = {(i & 1) == 0 ? box[0] : box[3], (i & 2) == 0 ? box[1] : box[4], (i & 4) == 0 ? box[2] : box[5]};
            double x = dot(sub(p, look), r);
            double y = dot(sub(p, look), u);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return Math.max(maxX - minX, maxY - minY);
    }

    static void write(File out, String title, List<BufferedImage> tiles, List<String> labels, int columns, int tw, int th) throws Exception {
        int rows = (tiles.size() + columns - 1) / columns;
        BufferedImage sheet = new BufferedImage(tw * columns, 30 + rows * (th + 20), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(22, 24, 30));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString(title, 10, 21);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for (int i = 0; i < tiles.size(); i++) {
            int x = (i % columns) * tw;
            int y = 30 + (i / columns) * (th + 20);
            g.drawImage(tiles.get(i), x, y, null);
            g.setColor(new Color(230, 230, 230));
            g.drawString(labels.get(i), x + 6, y + th + 14);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
        System.out.println(out + " written");
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return new double[] {a[0] / l, a[1] / l, a[2] / l};
    }
}
