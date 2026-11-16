package com.hexhaki.piece;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The owner's version rule (carried over from HexGhoul): "whatever reaches 9 switches to the upper number". After x.y.9 comes x.(y+1).0,
 * after x.9.9 comes (x+1).0.0. build.py's VERSION must obey it.
 */
public final class VersionTests {
    public static void main(String[] args) throws Exception {
        String build = Files.readString(Path.of("build.py"));
        Matcher matcher = Pattern.compile("VERSION = \"(\\d+)\\.(\\d+)\\.(\\d+)\"").matcher(build);
        Check.that(matcher.find(), "build.py names its VERSION");
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        Check.that(minor <= 9, "the middle number runs 0..9");
        Check.that(patch <= 9, "the last number runs 0..9");
        Check.that(next("2.1.8").equals("2.1.9") && next("2.1.9").equals("2.2.0") && next("2.9.9").equals("3.0.0"), "the next version rolls over at 9");
        Check.done("VersionTests");
    }

    /** The version after {@code version}. */
    static String next(String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]) + 1;
        if (patch > 9) {
            patch = 0;
            minor++;
        }
        if (minor > 9) {
            minor = 0;
            major++;
        }
        return major + "." + minor + "." + patch;
    }
}
