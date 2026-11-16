import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import jdk.internal.org.objectweb.asm.tree.TypeInsnNode;
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException;
import jdk.internal.org.objectweb.asm.tree.analysis.BasicVerifier;

/**
 * Proves the HexPiece jar without Minecraft (nothing can be downloaded in the cloud sessions):
 * <ol>
 *   <li>every new or changed method passes ASM's stack/type analysis ({@link BasicVerifier});</li>
 *   <li>every reference to the mod's own classes resolves to a class and member that exists in the output jar;</li>
 *   <li>every Minecraft/Forge/library reference (anything not {@code java/}, {@code javax/}, {@code jdk/} or the mod's) made by new or
 *       changed code is either already made by the original 2.1.0 jar (proven at runtime) or listed in an allowlist with its evidence;
 *       members are matched on their declaring class, found through the stub and jar hierarchy;</li>
 *   <li>dedicated server safety: a class outside a {@code client} package never names a client-only class (Minecraft's
 *       {@code net/minecraft/client}, {@code com/mojang/blaze3d}, or any {@code /client/} package of the mod) except inside a lambda body
 *       (only run where DistExecutor sends it), and every event subscriber in a client package is registered for {@code Dist.CLIENT}.</li>
 * </ol>
 * Usage: PieceVerify output.jar input.jar stub-classes-dir changed-list.txt allowlist...
 */
public final class PieceVerify implements Opcodes {
    static final String OURS = "com/hexhaki/";
    static final String NEW = "com/hexhaki/piece/";

    record Ref(String kind, String owner, String name, String desc) {
        String key() {
            return this.kind.equals("C") ? "C " + this.owner : this.kind + " " + this.owner + " " + this.name + " " + this.desc;
        }
    }

    final Map<String, ClassNode> output = new HashMap<>();
    final Map<String, ClassNode> stubs = new HashMap<>();
    final Map<String, ClassNode> input = new HashMap<>();
    final Set<String> proven = new HashSet<>();
    final Set<String> allowed = new HashSet<>();

    static Map<String, ClassNode> readJar(Path path) throws IOException {
        Map<String, ClassNode> nodes = new HashMap<>();
        try (ZipFile jar = new ZipFile(path.toFile())) {
            for (ZipEntry entry : java.util.Collections.list(jar.entries())) {
                if (entry.getName().endsWith(".class")) {
                    ClassNode node = new ClassNode();
                    new ClassReader(jar.getInputStream(entry).readAllBytes()).accept(node, 0);
                    nodes.put(node.name, node);
                }
            }
        }
        return nodes;
    }

    static Map<String, ClassNode> readDir(Path dir) throws IOException {
        Map<String, ClassNode> nodes = new HashMap<>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".class"))::iterator) {
                ClassNode node = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(node, 0);
                nodes.put(node.name, node);
            }
        }
        return nodes;
    }

    static boolean jdk(String owner) {
        return owner.startsWith("java/") || owner.startsWith("javax/") || owner.startsWith("jdk/") || owner.startsWith("sun/");
    }

    static String element(String internal) {
        if (internal == null) {
            return null;
        }
        if (internal.startsWith("[")) {
            Type type = Type.getType(internal).getElementType();
            return type.getSort() == Type.OBJECT ? type.getInternalName() : null;
        }
        return internal;
    }

    static void classesOf(String desc, Set<String> out) {
        if (desc == null) {
            return;
        }
        Type type = desc.startsWith("(") ? Type.getMethodType(desc) : Type.getType(desc);
        if (type.getSort() == Type.METHOD) {
            for (Type arg : type.getArgumentTypes()) {
                classesOf(arg.getDescriptor(), out);
            }
            classesOf(type.getReturnType().getDescriptor(), out);
        } else if (type.getSort() == Type.ARRAY) {
            classesOf(type.getElementType().getDescriptor(), out);
        } else if (type.getSort() == Type.OBJECT) {
            out.add(type.getInternalName());
        }
    }

    /** Everything a class names: members it touches and classes it mentions. */
    static List<Ref> refs(ClassNode node, boolean lambdasToo, Set<String> mentioned) {
        List<Ref> refs = new ArrayList<>();
        if (node.superName != null) {
            mentioned.add(node.superName);
        }
        mentioned.addAll(node.interfaces);
        for (FieldNode field : node.fields) {
            classesOf(field.desc, mentioned);
        }
        for (MethodNode method : node.methods) {
            classesOf(method.desc, mentioned);
            boolean lambda = method.name.startsWith("lambda$");
            if (lambda && !lambdasToo) {
                continue;
            }
            for (TryCatchBlockNode block : method.tryCatchBlocks) {
                if (block.type != null) {
                    mentioned.add(block.type);
                }
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call) {
                    refs.add(new Ref("M", element(call.owner), call.name, call.desc));
                    classesOf(call.desc, mentioned);
                } else if (insn instanceof FieldInsnNode field) {
                    refs.add(new Ref("F", field.owner, field.name, field.desc));
                    classesOf(field.desc, mentioned);
                } else if (insn instanceof TypeInsnNode type) {
                    String owner = element(type.desc);
                    if (owner != null) {
                        mentioned.add(owner);
                    }
                } else if (insn instanceof MultiANewArrayInsnNode array) {
                    classesOf(array.desc, mentioned);
                } else if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Type type) {
                    classesOf(type.getDescriptor(), mentioned);
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    classesOf(indy.desc, mentioned);
                    for (Object arg : indy.bsmArgs) {
                        if (arg instanceof Handle handle) {
                            refs.add(new Ref(handle.getTag() <= H_PUTSTATIC ? "F" : "M", handle.getOwner(), handle.getName(), handle.getDesc()));
                            classesOf(handle.getDesc(), mentioned);
                        }
                    }
                }
            }
        }
        for (Ref ref : refs) {
            if (ref.owner != null) {
                mentioned.add(ref.owner);
            }
        }
        mentioned.removeIf(name -> name == null);
        return refs;
    }

    ClassNode find(String name) {
        ClassNode node = this.output.get(name);
        if (node == null) {
            node = this.stubs.get(name);
        }
        if (node == null) {
            node = this.input.get(name);
        }
        return node;
    }

    /** The class that declares {@code ref} (walking super classes and interfaces), or null if none known does. */
    String declaring(Ref ref) {
        List<String> queue = new ArrayList<>(List.of(ref.owner));
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            String owner = queue.remove(0);
            if (owner == null || !seen.add(owner)) {
                continue;
            }
            ClassNode node = this.find(owner);
            if (node == null) {
                if (jdk(owner)) {
                    return owner;
                }
                continue;
            }
            if (ref.kind.equals("M")) {
                for (MethodNode method : node.methods) {
                    if (method.name.equals(ref.name) && method.desc.equals(ref.desc)) {
                        return owner;
                    }
                }
            } else {
                for (FieldNode field : node.fields) {
                    if (field.name.equals(ref.name) && field.desc.equals(ref.desc)) {
                        return owner;
                    }
                }
            }
            queue.add(node.superName);
            queue.addAll(node.interfaces);
        }
        return null;
    }

    boolean accepted(Ref ref) {
        if (this.proven.contains(ref.key()) || this.allowed.contains(ref.key())) {
            return true;
        }
        String owner = this.declaring(ref);
        if (owner == null) {
            return false;
        }
        Ref declared = new Ref(ref.kind, owner, ref.name, ref.desc);
        return jdk(owner) || this.proven.contains(declared.key()) || this.allowed.contains(declared.key());
    }

    void loadAllowlist(Path path) throws IOException {
        for (String raw : Files.readAllLines(path)) {
            String line = raw.contains("#") ? raw.substring(0, raw.indexOf('#')).trim() : raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts[0].equals("C") && parts.length == 2) {
                this.allowed.add("C " + parts[1]);
            } else if ((parts[0].equals("M") || parts[0].equals("F")) && parts.length == 4) {
                this.allowed.add(parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3]);
            } else {
                throw new AssertionError("bad allowlist line in " + path + ": " + raw);
            }
        }
    }

    static boolean clientPackage(String name) {
        return name.contains("/client/");
    }

    static boolean clientOnly(String name) {
        return name.startsWith("net/minecraft/client/") || name.startsWith("com/mojang/blaze3d/") || name.startsWith("dev/kosmx/")
            || name.startsWith("com/lowdragmc/") || (name.startsWith(OURS) && clientPackage(name));
    }

    static boolean subscribesForClient(ClassNode node) {
        List<AnnotationNode> annotations = node.visibleAnnotations == null ? List.of() : node.visibleAnnotations;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;")) {
                if (annotation.values != null) {
                    for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
                        if ("value".equals(annotation.values.get(i)) && names(annotation.values.get(i + 1)).contains("CLIENT")) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return true; // not a subscriber at all
    }

    /** An annotation value flattened to text: an enum constant is a {descriptor, name} pair, an array a list of them. */
    static String names(Object value) {
        if (value instanceof String[] pair) {
            return String.join(" ", pair);
        }
        if (value instanceof java.util.List<?> list) {
            StringBuilder out = new StringBuilder();
            for (Object item : list) {
                out.append(names(item)).append(' ');
            }
            return out.toString();
        }
        return String.valueOf(value);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            throw new IllegalArgumentException("Usage: PieceVerify output.jar input.jar stub-classes-dir changed-list.txt allowlist...");
        }
        PieceVerify verify = new PieceVerify();
        verify.output.putAll(readJar(Path.of(args[0])));
        verify.input.putAll(readJar(Path.of(args[1])));
        verify.stubs.putAll(readDir(Path.of(args[2])));
        Set<String> changed = new TreeSet<>();
        for (String line : Files.readAllLines(Path.of(args[3]))) {
            if (!line.isBlank() && line.trim().endsWith(".class")) {
                changed.add(line.trim().substring(0, line.trim().length() - ".class".length()));
            }
        }
        for (int i = 4; i < args.length; i++) {
            verify.loadAllowlist(Path.of(args[i]));
        }
        // What the original jar already does is proven at runtime: its references, on their owner and on their declaring class.
        for (ClassNode node : verify.input.values()) {
            Set<String> mentioned = new HashSet<>();
            for (Ref ref : refs(node, true, mentioned)) {
                if (ref.owner == null || ref.owner.startsWith(OURS)) {
                    continue;
                }
                verify.proven.add(ref.key());
                String owner = verify.declaring(ref);
                if (owner != null) {
                    verify.proven.add(new Ref(ref.kind, owner, ref.name, ref.desc).key());
                }
            }
            for (String name : mentioned) {
                verify.proven.add("C " + name);
            }
        }
        Set<String> audited = new TreeSet<>(changed);
        for (String name : verify.output.keySet()) {
            if (name.startsWith(NEW)) {
                audited.add(name);
            }
        }
        List<String> problems = new ArrayList<>();
        int analysed = 0;
        for (String name : audited) {
            ClassNode node = verify.output.get(name);
            if (node == null) {
                problems.add("audited class missing from the jar: " + name);
                continue;
            }
            for (MethodNode method : node.methods) {
                if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) != 0) {
                    continue;
                }
                try {
                    new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
                    analysed++;
                } catch (AnalyzerException failure) {
                    problems.add("bytecode analysis failed in " + name + "." + method.name + method.desc + ": " + failure.getMessage());
                }
            }
            Set<String> mentioned = new HashSet<>();
            List<Ref> refs = refs(node, true, mentioned);
            for (Ref ref : refs) {
                if (ref.owner == null || jdk(ref.owner)) {
                    continue;
                }
                if (ref.owner.startsWith(OURS)) {
                    String declaring = verify.output.get(ref.owner) == null ? null : verify.declaring(ref);
                    if (verify.output.get(ref.owner) == null) {
                        problems.add(name + " -> missing class " + ref.owner);
                    } else if (declaring == null) {
                        problems.add(name + " -> missing member " + ref.key());
                    } else if (!declaring.startsWith(OURS) && !jdk(declaring) && !verify.accepted(ref)) {
                        problems.add(name + " -> unproven inherited " + ref.key());
                    }
                } else if (!verify.accepted(ref)) {
                    problems.add(name + " -> unproven " + ref.key());
                }
            }
            for (String other : mentioned) {
                if (jdk(other) || other.startsWith(OURS)) {
                    if (other.startsWith(OURS) && verify.output.get(other) == null) {
                        problems.add(name + " -> missing class " + other);
                    }
                    continue;
                }
                if (!verify.proven.contains("C " + other) && !verify.allowed.contains("C " + other)) {
                    problems.add(name + " -> unproven C " + other);
                }
            }
            // Dedicated server safety.
            if (!clientPackage(name)) {
                Set<String> named = new HashSet<>();
                refs(node, false, named);
                for (String other : named) {
                    if (clientOnly(other)) {
                        problems.add("server-side class " + name + " names client-only " + other + " outside a lambda");
                    }
                }
            } else if (name.startsWith(NEW) && !subscribesForClient(node)) {
                problems.add("client event subscriber " + name + " is not registered for Dist.CLIENT");
            }
        }
        if (!problems.isEmpty()) {
            problems.stream().distinct().sorted().forEach(problem -> System.err.println("  " + problem));
            throw new AssertionError(problems.stream().distinct().count() + " verification problems");
        }
        System.out.println("PieceVerify: " + audited.size() + " classes audited, " + analysed + " methods analysed, every reference proven.");
    }
}
