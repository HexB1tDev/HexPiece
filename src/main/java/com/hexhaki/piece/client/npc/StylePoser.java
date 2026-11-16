package com.hexhaki.piece.client.npc;

import java.util.HashMap;
import java.util.Map;

/**
 * 2.1.1: samples a fighting-style animation for an NPC (PlayerAnimator animates players only) from {@link StylePoses}, the same
 * keyframes players play, interpolated as PlayerAnimator does (each keyframe's easing applies on the way into it). Returns, for a
 * moment of a move, every channel's value: {@code [part][channel]} (parts: body, head, rightArm, leftArm, rightLeg, leftLeg; channels:
 * x, y, z, pitch, yaw, roll, bend).
 */
public final class StylePoser {
    private static final Map<String, float[]> CACHE = new HashMap<>();

    private StylePoser() {
    }

    static float[] data(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            float[] data = StylePoses.of(n);
            return data == null ? new float[0] : data;
        });
    }

    public static float[][] sample(String name, float tick) {
        float[][] pose = new float[6][7];
        float[] data = data(name);
        int i = 0;
        while (i + 2 < data.length) {
            int part = (int) data[i];
            int channel = (int) data[i + 1];
            int count = (int) data[i + 2];
            int base = i + 3;
            pose[part][channel] = value(data, base, count, tick);
            i = base + count * 3;
        }
        return pose;
    }

    static float value(float[] data, int base, int count, float tick) {
        if (count == 0) {
            return 0.0F;
        }
        if (tick <= data[base]) {
            return data[base + 1];
        }
        for (int k = 0; k + 1 < count; k++) {
            float t0 = data[base + k * 3];
            float t1 = data[base + (k + 1) * 3];
            if (tick >= t0 && tick <= t1) {
                float v0 = data[base + k * 3 + 1];
                float v1 = data[base + (k + 1) * 3 + 1];
                float t = t1 > t0 ? (tick - t0) / (t1 - t0) : 1.0F;
                return v0 + (v1 - v0) * ease((int) data[base + (k + 1) * 3 + 2], t);
            }
        }
        return data[base + (count - 1) * 3 + 1];
    }

    /** linear, constant, in/out/inout sine, quad, cubic, expo, back (StylePoses' codes). */
    static float ease(int code, float t) {
        double x = t;
        return (float) switch (code) {
            case 1 -> x < 1 ? 0 : 1;
            case 2 -> 1 - Math.cos(x * Math.PI / 2);
            case 3 -> Math.sin(x * Math.PI / 2);
            case 4 -> -(Math.cos(Math.PI * x) - 1) / 2;
            case 5 -> x * x;
            case 6 -> 1 - (1 - x) * (1 - x);
            case 7 -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
            case 8 -> x * x * x;
            case 9 -> 1 - Math.pow(1 - x, 3);
            case 10 -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
            case 11 -> x == 0 ? 0 : Math.pow(2, 10 * x - 10);
            case 12 -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x);
            case 13 -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2;
            case 14 -> 2.70158 * x * x * x - 1.70158 * x * x;
            case 15 -> 1 + 2.70158 * Math.pow(x - 1, 3) + 1.70158 * Math.pow(x - 1, 2);
            case 16 -> x < 0.5 ? Math.pow(2 * x, 2) * ((2.5949095 + 1) * 2 * x - 2.5949095) / 2
                : (Math.pow(2 * x - 2, 2) * ((2.5949095 + 1) * (x * 2 - 2) + 2.5949095) + 2) / 2;
            default -> x;
        };
    }
}
