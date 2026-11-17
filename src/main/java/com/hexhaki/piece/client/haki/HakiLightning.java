package com.hexhaki.piece.client.haki;

import com.hexhaki.client.vfx.HakiVfx;
import com.hexhaki.piece.haki.BoltPainter;
import com.hexhaki.piece.haki.Jag;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import java.util.SplittableRandom;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 2.1.1 (the owner: "Improve Haki's lightning, usage, animation, and everything else"): every Haki lightning beam the original draws
 * goes through here. The patch hands over the two segment painters ({@code addHakiLightningSegment}, {@code addConquerorStormSegment})
 * and every "make a beam, add it" pair in the original's lightning methods (the Conqueror's range lightning and families, the Last
 * Stand's bolts and arches, the sky breaks and rifts, the advanced Galaxy fist).
 *
 * <p>Where the original laid one straight textured beam from A to B, a bolt here is:
 * <ul>
 *   <li>crackled ({@link Jag#path}, seeded by its ends, so the dark sheath and the red filament drawn over the same segment stay one bolt);</li>
 *   <li>tapered, thick at its root and thin at its tip;</li>
 *   <li>struck, not switched on: it runs from A to B over a tick or two (each piece a little later than the one before it);</li>
 *   <li>forked: some segments shed a short, thinner branch that dies first;</li>
 *   <li>re-struck: a long-lived bolt flickers into a second shape halfway through its life, as real lightning does.</li>
 * </ul>
 * The original's colours and widths are kept (they are the author's palette); its beams and their Photon setup are reused as they are
 * ({@code HakiVfx.beam}/{@code rageBeam}, widened to public by the patch). A budget keeps a huge Conqueror release from flooding Photon:
 * past it, bolts are drawn straight as before.
 *
 * <p>2.1.5: every beam is also painted as 3D lightning ({@link BoltPainter}: crackled, outlined, glowing tubes) and kept with its
 * composition in {@link HakiBolts}; when the composition is played, the 3D bolts take the flat beams' place. The beams are still made,
 * as the fallback for a composition played some other way.
 */
public final class HakiLightning {
    /** Beams this painter may add in one burst window before falling back to the original's straight beams. */
    static final int BUDGET = 2400;
    static final long WINDOW_NANOS = 60_000_000L;
    static final long SALT = 0x4841_4B49L;
    private static long windowStart;
    private static int spent;

    private HakiLightning() {
    }

    /** A "beam + add" pair of the original's lightning methods. */
    public static void bolt(FX fx, Vector3f from, Vector3f to, float width, int color, int duration, int delay, boolean bloom) {
        draw(fx, from.x, from.y, from.z, to.x, to.y, to.z, width, color, duration, delay, bloom, false, width >= 0.03F);
    }

    /** A "rageBeam + add" pair (the rage lightning texture). */
    public static void rage(FX fx, Vector3f from, Vector3f to, float width, int color, int duration, int delay, boolean bloom) {
        draw(fx, from.x, from.y, from.z, to.x, to.y, to.z, width, color, duration, delay, bloom, true, width >= 0.03F);
    }

    /** {@code addHakiLightningSegment(fx, a, b, duration, delay)}: a dark crimson aura, the black sheath and the hot red filament. */
    public static void segment(FX fx, Vec3 a, Vec3 b, int duration, int delay) {
        float ax = (float) a.f_82479_;
        float ay = (float) a.f_82480_;
        float az = (float) a.f_82481_;
        float bx = (float) b.f_82479_;
        float by = (float) b.f_82480_;
        float bz = (float) b.f_82481_;
        draw(fx, ax, ay, az, bx, by, bz, 0.13F, 0x582A0008, duration + 3, delay, false, true, false);
        draw(fx, ax, ay, az, bx, by, bz, 0.072F, 0xF0060008, duration + 2, delay, false, true, true);
        draw(fx, ax, ay, az, bx, by, bz, 0.022F, 0xFFFF1E10, duration, delay, true, true, true);
    }

    /** {@code addConquerorStormSegment(fx, a, b, duration, delay)}: the storm's thick black bolt with its red heart. */
    public static void storm(FX fx, Vec3 a, Vec3 b, int duration, int delay) {
        float ax = (float) a.f_82479_;
        float ay = (float) a.f_82480_;
        float az = (float) a.f_82481_;
        float bx = (float) b.f_82479_;
        float by = (float) b.f_82480_;
        float bz = (float) b.f_82481_;
        draw(fx, ax, ay, az, bx, by, bz, 0.16F, 0xD9080609, duration + 2, delay, false, false, true);
        draw(fx, ax, ay, az, bx, by, bz, 0.04F, 0xFFE31F36, duration, delay, true, false, true);
    }

    /**
     * 2.3.3 (the owner: "Tap G to release ... the Lightning does not match the one in the Conqueror's release. Like, it's still the old
     * Lightning"): {@code HakiFx.bolt(fx, a, b, scale, glow, core, white, duration, delay)}, the bolt path the Conqueror's tap release,
     * King's Grip, Thragg's moves and the armament ground storm draw with ({@code HakiFx.boltPath}, through {@code advancedBolt} and
     * {@code armamentBolt}): its three beams (the glow 0.15 wide, the core 0.052, the white-hot heart 0.017, times the scale) as the
     * same 3D lightning as every other Haki bolt.
     */
    public static void fxBolt(FX fx, Vec3 a, Vec3 b, float scale, int glow, int core, int white, int duration, int delay) {
        float ax = (float) a.f_82479_;
        float ay = (float) a.f_82480_;
        float az = (float) a.f_82481_;
        float bx = (float) b.f_82479_;
        float by = (float) b.f_82480_;
        float bz = (float) b.f_82481_;
        draw(fx, ax, ay, az, bx, by, bz, 0.15F * scale, glow, duration + 2, delay, false, false, false);
        draw(fx, ax, ay, az, bx, by, bz, 0.052F * scale, core, duration + 1, delay, false, false, true);
        draw(fx, ax, ay, az, bx, by, bz, 0.017F * scale, white, duration, delay, true, false, true);
    }

    static boolean spend(int beams) {
        long now = System.nanoTime();
        if (now - windowStart > WINDOW_NANOS) {
            windowStart = now;
            spent = 0;
        }
        if (spent + beams > BUDGET) {
            return false;
        }
        spent += beams;
        return true;
    }

    static void draw(FX fx, float ax, float ay, float az, float bx, float by, float bz, float width, int color, int duration, int delay,
                     boolean bloom, boolean rage, boolean forks) {
        HakiBolts.Pending pending = HakiBolts.pending(fx);
        double[] a = {ax, ay, az};
        double[] b = {bx, by, bz};
        float start = pending.chain.start(a, delay);
        BoltPainter.strokes(a, b, width, color, duration, start, forks, SALT, pending.bolts);
        pending.chain.land(a, b, start, duration);
        double length = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay) + (bz - az) * (bz - az));
        int levels = Jag.levels(length);
        if (!spend((1 << levels) + 2)) {
            emit(fx, ax, ay, az, bx, by, bz, width, color, duration, delay, bloom, rage);
            return;
        }
        long seed = Jag.seed(ax, ay, az, bx, by, bz, SALT);
        strike(fx, new double[] {ax, ay, az}, new double[] {bx, by, bz}, levels, seed, width, color, duration, delay, bloom, rage, forks,
            length);
        if (duration >= 10 && length >= 1.0) {
            // The re-strike: halfway through, the bolt snaps into a new shape and fades out in it.
            int half = duration / 2;
            strike(fx, new double[] {ax, ay, az}, new double[] {bx, by, bz}, levels, seed ^ 0x5245_5354L, width * 0.9F, fade(color, 0.75F),
                duration - half, delay + half, bloom, rage, false, length);
        }
    }

    static void strike(FX fx, double[] a, double[] b, int levels, long seed, float width, int color, int duration, int delay, boolean bloom,
                       boolean rage, boolean forks, double length) {
        double[] path = Jag.path(a, b, levels, 0.2, seed);
        int pieces = path.length / 3 - 1;
        int crawlTicks = Math.min(2, Math.max(0, duration / 4));
        for (int i = 0; i < pieces; i++) {
            double t = pieces == 1 ? 0.0 : i / (double) (pieces - 1);
            int crawl = (int) Math.floor(t * crawlTicks + 1.0E-6);
            float w = (float) (width * Jag.taper((i + 0.5) / pieces, 0.55));
            emit(fx, (float) path[i * 3], (float) path[i * 3 + 1], (float) path[i * 3 + 2], (float) path[i * 3 + 3], (float) path[i * 3 + 4],
                (float) path[i * 3 + 5], w, color, Math.max(1, duration - crawl), delay + crawl, bloom, rage);
        }
        if (!forks || length < 1.2 || pieces < 2) {
            return;
        }
        SplittableRandom random = new SplittableRandom(seed ^ 0x464F524BL);
        if (random.nextDouble() >= 0.38) {
            return;
        }
        int at = 1 + random.nextInt(Math.max(1, pieces - 1));
        double[] from = {path[at * 3], path[at * 3 + 1], path[at * 3 + 2]};
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        double[] across = Jag.across(dx, dy, dz);
        double side = random.nextDouble() < 0.5 ? -1 : 1;
        double reach = length * (0.22 + random.nextDouble() * 0.18);
        double forward = 0.75;
        double out = 0.65 * side;
        double[] tip = {
            from[0] + (dx / length * forward + across[0] * out) * reach,
            from[1] + (dy / length * forward + across[1] * out) * reach - reach * 0.12,
            from[2] + (dz / length * forward + across[2] * out) * reach};
        double[] fork = Jag.path(from, tip, Math.min(2, Jag.levels(reach) + 1), 0.26, random.nextLong());
        int forkPieces = fork.length / 3 - 1;
        int start = delay + (int) Math.floor(at / (double) pieces * crawlTicks) + 1;
        int life = Math.max(1, (int) (duration * 0.65));
        for (int i = 0; i < forkPieces; i++) {
            float w = (float) (width * 0.55 * Jag.taper((i + 0.5) / forkPieces, 0.4));
            emit(fx, (float) fork[i * 3], (float) fork[i * 3 + 1], (float) fork[i * 3 + 2], (float) fork[i * 3 + 3], (float) fork[i * 3 + 4],
                (float) fork[i * 3 + 5], w, color, life, start, bloom, rage);
        }
    }

    static void emit(FX fx, float ax, float ay, float az, float bx, float by, float bz, float width, int color, int duration, int delay,
                     boolean bloom, boolean rage) {
        Vector3f from = new Vector3f(ax, ay, az);
        Vector3f to = new Vector3f(bx, by, bz);
        IFXObject beam = rage ? HakiVfx.rageBeam(from, to, width, color, duration, delay, bloom)
            : HakiVfx.beam(from, to, width, color, duration, delay, bloom);
        HakiVfx.add(fx, beam);
        HakiBolts.pending(fx).beams.add(beam);
    }

    /** {@code color} with its alpha scaled by {@code share}. */
    static int fade(int color, float share) {
        int alpha = (color >>> 24) & 0xFF;
        return (Math.round(alpha * share) & 0xFF) << 24 | (color & 0xFFFFFF);
    }
}
