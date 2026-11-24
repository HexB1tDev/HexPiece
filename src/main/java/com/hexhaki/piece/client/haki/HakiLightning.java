package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.hexhaki.piece.haki.Jag;
import com.lowdragmc.photon.client.fx.FX;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 2.1.1 (the owner: "Improve Haki's lightning, usage, animation, and everything else"): every Haki lightning beam the original draws
 * goes through here. The patch hands over the two segment painters ({@code addHakiLightningSegment}, {@code addConquerorStormSegment})
 * and every "make a beam, add it" pair in the original's lightning methods (the Conqueror's range lightning and families, the Last
 * Stand's bolts and arches, the sky breaks and rifts, the advanced Galaxy fist).
 *
 * <p>2.1.5: every beam is painted as 3D lightning and kept with its composition in {@link HakiBolts}; when the composition is played, the
 * 3D bolts go. 3.6.0: each is the one Haki lightning, the release's model on its crooked runs ({@link ConquerorStorm#strike}). 3.6.3
 * (the owner: "make the lightning 3d once it for all, all of it"): the original's flat beams are never made at all (no budget fallback,
 * no fallback for a composition played some other way): only the 3D bolts, drawn as round lit tubes ({@code haki/BoltMesh}).
 */
public final class HakiLightning {
    static final long SALT = 0x4841_4B49L;

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

    static void draw(FX fx, float ax, float ay, float az, float bx, float by, float bz, float width, int color, int duration, int delay,
                     boolean bloom, boolean rage, boolean forks) {
        HakiBolts.Pending pending = HakiBolts.pending(fx);
        double[] a = {ax, ay, az};
        double[] b = {bx, by, bz};
        float start = pending.chain.start(a, delay);
        // 3.6.0 (the owner: "Haki Coated attacks from fruits and styles are to have the exact same LIGHTNING as the one in Conquerors's
        // charge", "only 1 type and style of haki  model should be in use, THE CONQUEROR HAKI RELEASE (BUTTON G)"): each of the original's
        // bolts (a glow, a body or core, and a hot heart laid on one line) is painted once, as the release's model on its crooked runs
        // (ConquerorStorm.strike): from its body or core beam; its glow and its heart add nothing of their own
        if (!bloom && Bolt.kindOf(color) != Bolt.AURA) {
            int rim = Bolt.kindOf(color) == Bolt.BODY ? ConquerorStorm.EDGE & 0xFFFFFF : color & 0xFFFFFF;
            long seed = Jag.seed(ax, ay, az, bx, by, bz, SALT);
            float crawl = (float) Math.min(duration * 0.5, Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay) + (bz - az) * (bz - az)) / 10.0);
            pending.bolts.addAll(ConquerorStorm.strike(a, b, width * 2.0F, 0.35F, rim, start, duration, crawl, seed, false));
        }
        pending.chain.land(a, b, start, duration);
        // 3.6.3 (the owner: "the conquerors haki is the 3d lightning, yet the AI keeps putting the 2d, over and over and over again ...
        // make the lightning 3d once it for all, all of it"): the original's flat textured beams are never made any more, not even as a
        // fallback (past a budget, or for a composition played some way that does not ask HakiBolts, they showed as the old flat 2D
        // lightning): the 3D bolts above are the only lightning there is
    }

}
