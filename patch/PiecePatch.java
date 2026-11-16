import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

/**
 * HexPiece: the narrow, reproducible bytecode patch of the original HexHaki 2.1.0 jar (no source was ever supplied).
 *
 * <ul>
 *   <li>{@code HexHaki.<init>} hands the mod bus to {@code com.hexhaki.piece.HexPiece.init} (registries, network, NPCs).</li>
 *   <li>Haki lightning: {@code ConquerorLightningPath.generate} (shared by server hit checks and client visuals, a pure function of
 *       its seed) and the two segment painters in {@code HakiVfx} are trampolines into {@code com.hexhaki.piece}.</li>
 *   <li>{@code HakiVfx}'s beam helpers are widened to public so the new painters draw with the original's own Photon setup.</li>
 *   <li>{@code META-INF/mods.toml} and the manifest carry the HexPiece name and version.</li>
 * </ul>
 * Nothing in Minecraft is modified. Usage: PiecePatch input.jar output.jar version
 */
public final class PiecePatch implements Opcodes {
    static final String MOD = "com/hexhaki/HexHaki";
    static final String VFX = "com/hexhaki/client/vfx/HakiVfx";
    static final String PATH = "com/hexhaki/gameplay/ConquerorLightningPath";
    static final String COMMANDS = "com/hexhaki/command/HakiCommands";
    /**
     * 2.1.1 (the owner: "Get rid of the Imu fruit (Devil Devil fruit) Get rid of it ENTIRELY, but keep it's assets"): the 2.1.0 jar has no
     * Imu code left but two names of it in its debug tables: the "remove fruit" command's flag for a kit fruit (it is
     * {@code fruit != NONE}) and an empty Imu-only branch flag on the world effects packet (variant bit 8, nothing reads it). Both are
     * renamed; the Black Soul texture, model and name stay in the jar for later use. build.py checks no "imu" is left anywhere.
     */
    static final String[][] IMU_LOCALS = {{COMMANDS, "removeFruit", "kitFruit"}, {VFX, "acceptWorld", "unusedVariant"}};
    static final String IMU = new String(new char[] {'i', 'm', 'u'});
    static final String BOOT = "com/hexhaki/piece/HexPiece";
    static final String LIGHTNING = "com/hexhaki/piece/client/haki/HakiLightning";
    static final String PATHS = "com/hexhaki/piece/haki/LightningPaths";
    static final String BUS = "net/minecraftforge/eventbus/api/IEventBus";
    static final String CONTEXT = "net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext";
    static final String SEGMENT = "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;II)V";
    static final String GENERATE = "(FIJZ)Ljava/util/List;";
    /** HakiVfx helpers the new painters reuse (access widened only, bodies untouched). */
    static final String[] VFX_WIDENED = {"rageBeam", "beam", "add", "fx", "play", "playAtWorld", "playAtFixed"};
    /** HakiVfx painters handed to {@link #LIGHTNING}: name -> method there. */
    static final String[][] VFX_TRAMPOLINES = {{"addHakiLightningSegment", "segment"}, {"addConquerorStormSegment", "storm"}};
    /**
     * HakiVfx's lightning methods and how many "beam (or rageBeam) then add" pairs each has: every pair becomes one call to
     * {@code HakiLightning.bolt} (or {@code rage}) with the same stack (fx, from, to, width, colour, duration, delay, bloom).
     */
    static final Map<String, Integer> LIGHTNING_PAIRS = Map.of(
        "addAdvancedGalaxyFistLightning", 1, "addConquerorRangeLightning", 7, "addLastStandBodyBolts", 7,
        "addLastStandOverheadArches", 11, "lightningFamilies", 5, "lightningFamiliesAt", 4, "lightningFamiliesDelayed", 2,
        "skyBreak", 4, "skyRift", 4, "horizonCracks", 2);
    /**
     * 2.1.3: calls in the original redirected into {@code com.hexhaki.piece} (same descriptor, static): class, method, the call's owner,
     * name and descriptor, the new owner and name, how many such calls the method must have.
     * <ul>
     *   <li>R switches between the fighting style and the fruit (the owner: "tapping R switches from Combat style and fruit (if any is
     *       eaten)"): a kit fruit's M1 and right click ({@code FruitKeys.tick}, {@code FruitKeys.interact}) ask
     *       {@code PieceHooks.fruitKeysActive} (the kit is active and combat mode is off) instead of {@code FruitClient.active}.</li>
     * </ul>
     */
    static final String HOOKS = "com/hexhaki/piece/client/PieceHooks";
    static final String HAND = "com/hexhaki/client/render/HakiHandLayer";
    static final String RYOU_HANDS = "com/hexhaki/piece/client/haki/RyouHands";
    static final String RYOU_FX = "com/hexhaki/piece/client/haki/RyouFx";
    static final String RYOU_SERVER = "com/hexhaki/piece/server/RyouServer";
    static final String CONTROLLER = "com/hexhaki/gameplay/HakiServerController";
    static final String COATING = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/geom/ModelPart;FZLnet/minecraft/client/player/AbstractClientPlayer;Z)V";
    static final String AURA = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FZFZZ)V";
    static final String AMBIENT = "(Lnet/minecraft/world/entity/Entity;FZJZ)V";
    static final String GROUND_STORM = "(Lnet/minecraft/world/entity/Entity;FZJ)V";
    static final Object[][] CALL_REDIRECTS = {
        {"com/hexhaki/fruit/client/FruitKeys", "tick", "com/hexhaki/fruit/client/FruitClient", "active", "()Z", HOOKS, "fruitKeysActive", 1},
        {"com/hexhaki/fruit/client/FruitKeys", "interact", "com/hexhaki/fruit/client/FruitClient", "active", "()Z", HOOKS, "fruitKeysActive", 1},
        // 2.1.3 Ryou (the owner: "Ryou should look like blackened hands with a strong, clean glow outlining their shape. Keep the black
        // coating readable beneath the coloured rim. ... instead of covering the arms with red effects. Remove the fire particles from
        // every Ryou activation, idle and movement effect"): the coating stays plain black, the arm aura is left off while Ryou is up,
        // the ambient effects of a Ryou player are RyouFx's (no embers, the player's own colour).
        {HAND, "render", "com/hexhaki/client/devilfruit/render/GearJacketHaki", "renderCoating", COATING, RYOU_HANDS, "coating", 2},
        {HAND, "renderFirstPerson", "com/hexhaki/client/devilfruit/render/GearJacketHaki", "renderCoating", COATING, RYOU_HANDS, "coating", 1},
        {HAND, "render", HAND, "renderArmAura", AURA, RYOU_HANDS, "armAura", 2},
        {HAND, "renderFirstPerson", HAND, "renderArmAura", AURA, RYOU_HANDS, "armAura", 1},
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "tick", VFX, "armamentAmbient", AMBIENT, RYOU_FX, "ambient", 1},
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "tick", VFX, "armamentGroundStorm", GROUND_STORM, RYOU_FX, "groundStorm", 1},
    };
    /** 2.1.3: methods widened to public (bodies untouched) so HexPiece can call the originals: class, names. */
    static final String[][] WIDENED = {{HAND, "renderArmAura"}};
    /**
     * 2.1.3: static methods whose whole body becomes a call into HexPiece (same descriptor): class, method, descriptor, new owner, name.
     * The fist glow becomes Ryou's coloured rim; the fist flames are gone; J (Advanced Haki, now Ryou) is left empty (the owner: "I think
     * its best whenever Ryou is unlcked it fully replaces regular haki, and we empty out the J key for now").
     */
    static final String[][] TRAMPOLINES = {
        {HAND, "renderAdvancedFistGlow", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FZZ)V", RYOU_HANDS, "glow"},
        {HAND, "renderAdvancedFistFlames", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FFZZ)V", RYOU_HANDS, "flames"},
        {CONTROLLER, "toggleAcoc", "(Lnet/minecraft/server/level/ServerPlayer;)V", RYOU_SERVER, "acocKey"},
    };
    /** 2.1.3: once Ryou is unlocked, switching Armament on brings it (RyouServer.afterArmament runs before every return). */
    static final String[] AFTER_ARMAMENT = {CONTROLLER, "toggleArmament", "(Lnet/minecraft/server/level/ServerPlayer;)V", RYOU_SERVER, "afterArmament"};
    /**
     * 2.1.3 (the owner: "Reduce the excessive muscle size produced by Advanced Haki Armament ... Keep the result close to the normal player
     * silhouette, with restrained muscular definition rather than inflated shoulders, oversized biceps or an enlarged torso"): the
     * physique's float constants, class, method, from, to, how many. Arms 1.4x wide at full bulk become 1.07x; the shoulders pushed out
     * about 3 px become under half a pixel; the chest copy 1.55x wide and 1.36x deep becomes 1.06x and 1.10x (a hint of pectorals).
     */
    static final Object[][] FLOATS = {
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "pre", 0.4F, 0.07F, 1},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "armSeparation", 2.2F, 0.24F, 1},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "armSeparation", 0.35F, 0.08F, 1},
        {"com/hexhaki/client/render/HakiChestLayer", "render", 0.55F, 0.06F, 1},
        {"com/hexhaki/client/render/HakiChestLayer", "render", 0.3575F, 0.10F, 1},
    };
    /** The physique's named constants, kept in step with {@link #FLOATS} (class, field, value). */
    static final Object[][] FLOAT_FIELDS = {
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "MAX_ARM_GAIN", 0.07F},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "MAX_CHEST_GAIN", 0.06F},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "ARM_CLEARANCE", 0.08F},
    };
    /**
     * 2.1.3 (the owner: "Rename Advanced Haki Armament to Ryou everywhere visible to the player, including menus, tooltips, notifications
     * and ability descriptions"): text the player reads, class, from, to, how many (string constants and string-concatenation recipes;
     * identifiers such as acocOn, advancedHakiUnlocked and the network fields are untouched, so saves and packets stay as they were). The
     * two Advanced Haki messages of HakiServerController were in toggleAcoc, which is RyouServer.acocKey now (J is empty).
     */
    static final Object[][] TEXTS = {
        {"com/hexhaki/data/HakiTechnique", "Advanced Haki", "Ryou", 1},
        {"com/hexhaki/data/HakiTechnique", "[J] Late-game multiplier. Requires ARM 850 + HAO 850 and active Armament; upgrades nearly every major technique.",
            "Late-game multiplier. Requires ARM 850 + HAO 850. Once unlocked it replaces your Armament (Ctrl + R): blackened hands outlined in your own glow colour, and nearly every major technique upgraded.", 1},
        {"com/hexhaki/data/HakiTechnique", "Flowing Armament / Ryuo", "Flowing Armament", 1},
        {"com/hexhaki/data/HakiTechnique", "Ember Fists", "Ryou Fists", 1},
        {"com/hexhaki/data/HakiTechnique", "With Advanced Haki [J] up, both fists ignite: red-to-orange flame trails on the hands and sky-fed bolts landing between the outward arcs.",
            "With Ryou up, both fists blacken behind a bright outline in your own glow colour, and sky-fed bolts land between the outward arcs.", 1},
        {"com/hexhaki/data/HakiTechnique", "Late-game Armament control required for Advanced Haki [J].", "Late-game Armament control required for Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "Passive milestone: emitted Armament techniques reach a 16% total mastery-step bonus and prepare the path to Advanced Haki.",
            "Passive milestone: emitted Armament techniques reach a 16% total mastery-step bonus and prepare the path to Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "[M HOLD, RELEASE TO FIRE] Wind a Haki punch and let go. A tap throws a short close-range burst; a full hold throws a long, wide pressure cylinder that detonates where it lands. Length, width, damage, cost and cooldown all scale continuously with how long you held it, with Armament mastery, and with whether you are bare, coated, or under Advanced Haki.",
            "[M HOLD, RELEASE TO FIRE] Wind a Haki punch and let go. A tap throws a short close-range burst; a full hold throws a long, wide pressure cylinder that detonates where it lands. Length, width, damage, cost and cooldown all scale continuously with how long you held it, with Armament mastery, and with whether you are bare, coated, or under Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Advanced tiers raise the launch height, the damage and the size of the blast.",
            "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Ryou tiers raise the launch height, the damage and the size of the blast.", 1},
        {"com/hexhaki/data/HakiTechnique", "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Advanced Haki its landing scatters grounded Haki strikes around the impact.",
            "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Ryou its landing scatters grounded Haki strikes around the impact.", 1},
        {"com/hexhaki/binds/client/AbilityBook", "Permanent:  \u0001 Armament   \u0001 Observation   \u0001 Advanced Haki   \u0001 powers on/off   \u0001 this book\u0001",
            "Permanent:  \u0001 style / fruit (Ctrl: Armament, Ryou)   \u0001 Observation   \u0001 (empty)   \u0001 powers on/off   \u0001 this book\u0001", 1},
    };
    static final String BEAM = "(Lorg/joml/Vector3f;Lorg/joml/Vector3f;FIIIZ)Lcom/lowdragmc/photon/client/gameobject/emitter/beam/BeamEmitter;";
    static final String ADD = "(Lcom/lowdragmc/photon/client/fx/FX;Lcom/lowdragmc/photon/client/gameobject/IFXObject;)V";
    static final String BOLT = "(Lcom/lowdragmc/photon/client/fx/FX;Lorg/joml/Vector3f;Lorg/joml/Vector3f;FIIIZ)V";

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: PiecePatch input.jar output.jar version");
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String version = args[2];
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipFile jar = new ZipFile(input.toFile())) {
            for (ZipEntry entry : java.util.Collections.list(jar.entries())) {
                // Directory entries are kept too: the original jar has them, and the output keeps every original entry.
                entries.put(entry.getName(), entry.isDirectory() ? new byte[0] : jar.getInputStream(entry).readAllBytes());
            }
        }
        entries.put(MOD + ".class", transform(entries.get(MOD + ".class"), PiecePatch::bootstrap));
        entries.put(VFX + ".class", transform(entries.get(VFX + ".class"), PiecePatch::lightningPainters));
        entries.put(PATH + ".class", transform(entries.get(PATH + ".class"), PiecePatch::lightningPaths));
        for (String[] local : IMU_LOCALS) {
            entries.put(local[0] + ".class", transform(entries.get(local[0] + ".class"), node -> renameLocal(node, local[1], IMU, local[2])));
        }
        for (Object[] redirect : CALL_REDIRECTS) {
            String owner = (String) redirect[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> redirectCalls(node, redirect)));
        }
        for (String[] widen : WIDENED) {
            entries.put(widen[0] + ".class", transform(entries.get(widen[0] + ".class"), node -> {
                for (int i = 1; i < widen.length; i++) {
                    MethodNode method = method(node, widen[i], null);
                    method.access = (method.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
                }
            }));
        }
        for (String[] t : TRAMPOLINES) {
            entries.put(t[0] + ".class", transform(entries.get(t[0] + ".class"), node -> trampoline(method(node, t[1], t[2]), t[3], t[4])));
        }
        entries.put(AFTER_ARMAMENT[0] + ".class", transform(entries.get(AFTER_ARMAMENT[0] + ".class"), PiecePatch::afterArmament));
        for (Object[] f : FLOATS) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceFloat(node, f)));
        }
        for (Object[] f : FLOAT_FIELDS) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> {
                for (FieldNode field : node.fields) {
                    if (field.name.equals(f[1])) {
                        field.value = f[2];
                        return;
                    }
                }
                throw new AssertionError("missing field " + owner + "." + f[1]);
            }));
        }
        for (Object[] text : TEXTS) {
            String owner = (String) text[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceText(node, text)));
        }
        entries.put("META-INF/mods.toml", modsToml(entries.get("META-INF/mods.toml"), version));
        entries.put("META-INF/MANIFEST.MF", manifest(entries.get("META-INF/MANIFEST.MF"), version));
        Files.createDirectories(output.toAbsolutePath().getParent());
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        System.out.println("Patched " + input.getFileName() + " -> " + output.getFileName() + " (" + version + ")");
    }

    interface Change {
        void apply(ClassNode node);
    }

    static byte[] transform(byte[] bytes, Change change) {
        if (bytes == null) {
            throw new AssertionError("class to patch is missing");
        }
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        change.apply(node);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static MethodNode method(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && (desc == null || method.desc.equals(desc))) {
                return method;
            }
        }
        throw new AssertionError("missing method " + node.name + "." + name + (desc == null ? "" : desc));
    }

    /** HexHaki(FMLJavaModLoadingContext): before every return, HexPiece.init(context.getModEventBus()). */
    static void bootstrap(ClassNode node) {
        MethodNode init = method(node, "<init>", "(L" + CONTEXT + ";)V");
        int hooks = 0;
        for (AbstractInsnNode insn = init.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == RETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(ALOAD, 1));
                call.add(new MethodInsnNode(INVOKEVIRTUAL, CONTEXT, "getModEventBus", "()L" + BUS + ";", false));
                call.add(new MethodInsnNode(INVOKESTATIC, BOOT, "init", "(L" + BUS + ";)V", false));
                init.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks != 1) {
            throw new AssertionError("expected one return in HexHaki.<init>, found " + hooks);
        }
    }

    static void lightningPainters(ClassNode node) {
        for (String name : VFX_WIDENED) {
            for (MethodNode method : node.methods) {
                if (method.name.equals(name)) {
                    method.access = (method.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
                }
            }
        }
        for (String[] pair : VFX_TRAMPOLINES) {
            trampoline(method(node, pair[0], SEGMENT), LIGHTNING, pair[1]);
        }
        for (Map.Entry<String, Integer> entry : LIGHTNING_PAIRS.entrySet()) {
            int redirected = redirectPairs(method(node, entry.getKey(), null));
            if (redirected != entry.getValue()) {
                throw new AssertionError("expected " + entry.getValue() + " lightning beams in HakiVfx." + entry.getKey() + ", found " + redirected);
            }
        }
    }

    static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }

    /** Every {@code beam}/{@code rageBeam} call straight followed by {@code add} becomes one {@code HakiLightning} call. */
    static int redirectPairs(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(VFX) && call.desc.equals(BEAM)
                && (call.name.equals("beam") || call.name.equals("rageBeam"))) {
                AbstractInsnNode next = nextReal(call);
                if (next instanceof MethodInsnNode add && add.getOpcode() == INVOKESTATIC && add.owner.equals(VFX) && add.name.equals("add")
                    && add.desc.equals(ADD)) {
                    MethodInsnNode bolt = new MethodInsnNode(INVOKESTATIC, LIGHTNING, call.name.equals("beam") ? "bolt" : "rage", BOLT, false);
                    method.instructions.set(call, bolt);
                    method.instructions.remove(add);
                    insn = bolt;
                    count++;
                }
            }
        }
        return count;
    }

    /** One {@link #CALL_REDIRECTS} row: every matching static call in the method goes to the new owner instead; the count must match. */
    static void redirectCalls(ClassNode node, Object[] redirect) {
        MethodNode method = method(node, (String) redirect[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(redirect[2])
                && call.name.equals(redirect[3]) && call.desc.equals(redirect[4])) {
                call.owner = (String) redirect[5];
                call.name = (String) redirect[6];
                call.itf = false;
                count++;
            }
        }
        if (count != (Integer) redirect[7]) {
            throw new AssertionError("expected " + redirect[7] + " calls of " + redirect[2] + "." + redirect[3] + " in " + node.name + "."
                + redirect[1] + ", found " + count);
        }
    }

    /** RyouServer.afterArmament(player) before every return of HakiServerController.toggleArmament. */
    static void afterArmament(ClassNode node) {
        MethodNode method = method(node, AFTER_ARMAMENT[1], AFTER_ARMAMENT[2]);
        int hooks = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == RETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(ALOAD, 0));
                call.add(new MethodInsnNode(INVOKESTATIC, AFTER_ARMAMENT[3], AFTER_ARMAMENT[4], AFTER_ARMAMENT[2], false));
                method.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks < 1) {
            throw new AssertionError("no return in " + node.name + "." + AFTER_ARMAMENT[1]);
        }
    }

    /** One {@link #FLOATS} row: the float constant in the method becomes the new value; the count must match. */
    static void replaceFloat(ClassNode node, Object[] f) {
        MethodNode method = method(node, (String) f[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Float value && value.floatValue() == (Float) f[2]) {
                ldc.cst = f[3];
                count++;
            }
        }
        if (count != (Integer) f[4]) {
            throw new AssertionError("expected " + f[4] + " constants " + f[2] + " in " + node.name + "." + f[1] + ", found " + count);
        }
    }

    /** One {@link #TEXTS} row: the string constant (or string-concatenation recipe) in any method becomes the new text. */
    static void replaceText(ClassNode node, Object[] text) {
        String from = unescape((String) text[1]);
        String to = unescape((String) text[2]);
        int count = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof LdcInsnNode ldc && from.equals(ldc.cst)) {
                    ldc.cst = to;
                    count++;
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        if (from.equals(indy.bsmArgs[i])) {
                            indy.bsmArgs[i] = to;
                            count++;
                        }
                    }
                }
            }
        }
        if (count != (Integer) text[3]) {
            throw new AssertionError("expected " + text[3] + " texts '" + from + "' in " + node.name + ", found " + count);
        }
    }

    /** The {@code \\u0001} of a concatenation recipe written in the tables above. */
    static String unescape(String text) {
        return text.replace("\\u0001", "\u0001");
    }

    /** Renames the one debug-table local {@code from} in {@code owner.method} to {@code to}. */
    static void renameLocal(ClassNode node, String methodName, String from, String to) {
        int renamed = 0;
        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName) && method.localVariables != null) {
                for (var local : method.localVariables) {
                    if (local.name.equals(from)) {
                        local.name = to;
                        renamed++;
                    }
                }
            }
        }
        if (renamed != 1) {
            throw new AssertionError("expected one local '" + from + "' in " + node.name + "." + methodName + ", found " + renamed);
        }
    }

    static void lightningPaths(ClassNode node) {
        trampoline(method(node, "generate", GENERATE), PATHS, "generate");
    }

    /** Replaces a static method's body with a straight call to {@code owner.name} with the same descriptor. */
    static void trampoline(MethodNode method, String owner, String name) {
        if ((method.access & ACC_STATIC) == 0) {
            throw new AssertionError("trampolines are for static methods: " + method.name);
        }
        InsnList body = new InsnList();
        int slot = 0;
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            body.add(new VarInsnNode(arg.getOpcode(ILOAD), slot));
            slot += arg.getSize();
        }
        body.add(new MethodInsnNode(INVOKESTATIC, owner, name, method.desc, false));
        body.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
        method.instructions = body;
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        method.maxLocals = slot;
    }

    static byte[] modsToml(byte[] bytes, String version) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        String patched = replaceOnce(text, "version=\"2.1.0\"", "version=\"" + version + "\"");
        patched = replaceOnce(patched, "displayName=\"HexHaki\"", "displayName=\"HexPiece\"");
        patched = replaceOnce(patched, "description='''High-end, multiplayer-visible Armament, Observation and Conqueror's Haki systems.'''",
            "description='''HexPiece: Haki, Devil Fruits, fighting styles, pirates and marines.'''");
        return patched.getBytes(StandardCharsets.UTF_8);
    }

    static byte[] manifest(byte[] bytes, String version) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return replaceOnce(text, "Implementation-Version: 2.1.0", "Implementation-Version: " + version).getBytes(StandardCharsets.UTF_8);
    }

    static String replaceOnce(String text, String from, String to) {
        int at = text.indexOf(from);
        if (at < 0 || text.indexOf(from, at + 1) >= 0) {
            throw new AssertionError("expected exactly one '" + from + "'");
        }
        return text.substring(0, at) + to + text.substring(at + from.length());
    }

    static List<String> widened() {
        return List.of(VFX_WIDENED);
    }
}
