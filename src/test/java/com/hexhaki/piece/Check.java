package com.hexhaki.piece;

/** A tiny assertion helper for the pure-logic suites (they run as plain mains, without JUnit, in build.py). */
public final class Check {
    private static int passed;

    private Check() {
    }

    public static void that(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("FAILED: " + what);
        }
        passed++;
    }

    public static void near(double actual, double expected, double tolerance, String what) {
        that(Math.abs(actual - expected) <= tolerance, what + " (expected " + expected + " +- " + tolerance + ", was " + actual + ")");
    }

    public static void done(String suite) {
        System.out.println(suite + ": " + passed + " checks passed");
    }
}
