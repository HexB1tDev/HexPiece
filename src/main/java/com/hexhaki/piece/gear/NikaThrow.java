package com.hexhaki.piece.gear;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.4.7 (the owner: the Giant's throw "on a ... horizontal line ... our own custom effect whenever the entity crashes against something
 * ... you gotta give it your special touch"): the Gum-Gum Giant's throw as everyone sees it (pure; client/gear/NikaWhipClient and
 * tools/preview/NikaShots draw the very same quads): {@link #trail} the white speed streaks and rings behind the body while it flies flat
 * (server/GiantThrow), {@link #crash} where it slams into something: a big comic POW, earth bursting up out of the impact, impact
 * lines and a dust ring (the server adds the Gura fruit's dust, rocks, cracks and a crater, and the shaking). Alpha-blended, cheap.
 */
public final class NikaThrow {
    private NikaThrow() {
    }

    /** How long the trail lasts after the throw (its straight flight and a little more) and the crash (ticks). */
    public static final double TRAIL_LIFE = 16.0;
    public static final double CRASH_LIFE = 14.0;

    /**
     * Behind a body thrown along ({@code wx}, {@code wz}) (its middle at x y z, eye-relative), {@code age} ticks after the throw, {@code
     * height} its height: eight white speed lines streaming back from round it, a cartoon ring of wind every few ticks left behind it. About
     * 60 quads.
     */
    public static void trail(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double age, double height, int seed) {
        if (age < 0.0 || age > TRAIL_LIFE) {
            return;
        }
        double wl = Math.sqrt(wx * wx + wz * wz);
        if (wl < 1.0E-6) {
            return;
        }
        double ux = wx / wl;
        double uz = wz / wl;
        double fade = age < TRAIL_LIFE - 4.0 ? 1.0 : (TRAIL_LIFE - age) / 4.0;
        double r = Math.max(0.5, height * 0.45);
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0 + FireMesh.rand(seed, k) * 0.5;
            double side = Math.cos(a) * r;
            double up = Math.sin(a) * r;
            double lag = 0.3 + 0.6 * FireMesh.rand(seed, k + 10);
            double length = 2.5 + 2.0 * FireMesh.rand(seed, k + 20);
            double px = x - uz * side - ux * lag;
            double py = y + up;
            double pz = z + ux * side - uz * lag;
            FireMesh.ray(sink, px, py, pz, -ux, 0, -uz, length, 0.05, 0, 0.0, 0xFFFFFF, 0.75 * fade);
        }
        // rings of wind left behind, one every three ticks, opening and fading
        for (int k = 0; k < 3; k++) {
            double born = Math.floor(age / 3.0) * 3.0 - k * 3.0;
            double t = age - born;
            if (born < 0.0 || t > 6.0) {
                continue;
            }
            double back = 1.0 + t * 1.6;
            FireMesh.ring(sink, x - ux * back, y, z - uz * back, ux, 0, uz, r * (1.0 + 0.25 * t), 0.18, 0.2, 0xFFFFFF,
                0.6 * (1.0 - t / 6.0) * fade);
        }
    }

    /**
     * The crash (the body's middle at x y z, eye-relative), {@code age} ticks ago, thrown along ({@code wx}, {@code wz}), {@code size} its
     * height: a big comic POW (Dawn Pistol's bands), earth bursting up round it and a dust ring (Dawn Whip's pop out of the ground). About
     * 1,500 quads at its height.
     */
    public static void crash(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double age, double size, int seed) {
        if (age < 0.0 || age > CRASH_LIFE) {
            return;
        }
        NikaArt.pow(sink, x, y, z, wx, 0, wz, age, size * 1.4, seed);
        NikaDawn.pop(sink, x, y - size * 0.5, z, age, seed);
    }
}
