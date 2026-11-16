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
import jdk.internal.org.objectweb.asm.tree.InsnNode;
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
