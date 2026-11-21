import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.8.9 (the owner's crash on 2.8.8: "java.lang.VerifyError: Expecting a stackmap frame at branch target 9", the mod refused at load):
 * every class the patch changed is linked here by the JVM itself, as the game links it, from the built jar over the compile-only stubs:
 * its own verifier checks its bytecode and stack map frames. A VerifyError fails the build. A class that cannot be linked for want of
 * something the stubs leave out, or whose static start-up the stubs refuse, has passed verification all the same or is reported.
 * Usage: JvmVerify output.jar stub-classes-dir changed-list.txt
 */
public final class JvmVerify {
    public static void main(String[] args) throws Exception {
        URL[] urls = {Path.of(args[0]).toUri().toURL(), Path.of(args[1]).toUri().toURL()};
        List<String> classes = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of(args[2]))) {
            if (line.trim().endsWith(".class") && !line.contains("module-info")) {
                classes.add(line.trim().substring(0, line.trim().length() - ".class".length()).replace('/', '.'));
            }
        }
        int verified = 0;
        List<String> unchecked = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        try (URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getPlatformClassLoader())) {
            for (String name : classes) {
                try {
                    Class.forName(name, true, loader);
                    verified++;
                } catch (VerifyError e) {
                    failed.add(name + ": " + e.getMessage().lines().findFirst().orElse(""));
                } catch (ExceptionInInitializerError e) {
                    verified++; // linked (so verified); its static start-up needs the game
                } catch (LinkageError e) {
                    if (e.getCause() instanceof VerifyError v) {
                        failed.add(name + ": " + v.getMessage());
                    } else if (e instanceof NoClassDefFoundError && String.valueOf(e.getMessage()).startsWith("Could not initialize class")) {
                        verified++; // linked (so verified); a class its static start-up needs failed its own start-up over the stubs
                    } else {
                        unchecked.add(name + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                    }
                }
            }
        }
        if (!failed.isEmpty()) {
            throw new AssertionError("the JVM refuses " + failed.size() + " patched class(es):\n  " + String.join("\n  ", failed));
        }
        System.out.println("JvmVerify: " + verified + " patched classes linked by the JVM's own verifier" + (unchecked.isEmpty() ? ""
            : ", " + unchecked.size() + " not linkable over the stubs: " + unchecked));
    }
}
