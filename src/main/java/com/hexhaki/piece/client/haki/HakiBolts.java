package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.BoltPainter;
import com.hexhaki.piece.haki.RyouPalette;
import com.hexhaki.registry.ModSounds;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.1.5: the 3D Haki lightning in the world (the owner: "We really need to rework the lightning of the Haki so it's actual 3D lightning
 * ... with a black outline ... That is the most important thing about the mod").
 *
 * <p>The original builds each Haki effect as a Photon composition, lays its lightning in it as flat textured beams, and plays it:
 * following an entity ({@code HakiVfx.play}: at the entity's feet, turned with it, Photon's XROT), or fixed in the world
 * ({@code playAtWorld}, {@code playAtFixed}). {@link HakiLightning} now also paints every beam as {@link Bolt}s and keeps them with the
 * composition ({@link #pending}); the patch has the three players ask {@link #played} first, which takes the composition's beams back
 * out, sets its bolts going from the same anchor, and lets the rest of the composition (its particles, rings, flashes) play as before.
 * A composition that never goes through those players (the Galaxy Fist's charge, which keeps its own effect) keeps its beams: nothing
 * is ever lost.
 *
 * <p>Every frame each live bolt becomes {@link BoltMesh} geometry in camera space: the outline, the body and the core (2.2.6: lit 3D
 * tubes, inked, the key light set from the view by {@link #light}) in one opaque, unlit, fogged batch ({@code RenderType.textBackground}),
 * then the glow in one blended batch, so the world and the bodies in front of a bolt hide it, and the glow never paints over its own ink.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HakiBolts {
    /** Most strokes alive at once; past it a composition keeps the original's beams. */
    static final int MOST = 5000;
    /** A pixel one block from the eye (70 degrees on a 1080-line screen): the floor for the ink's width. */
    static final double PIXEL = 2.0 * Math.tan(Math.toRadians(35.0)) / 1080.0;
    /** A composition not played within this long is forgotten (its beams stay with it). */
    static final long STALE_NANOS = 5_000_000_000L;
    /** Most ground strikes one composition leaves. */
    static final int MOST_LANDINGS = 12;
    static final Map<FX, Pending> PENDING = new IdentityHashMap<>();
    static final List<Live> LIVE = new ArrayList<>();
    static int alive;
    private static final Quads SOLID = new Quads();
    private static final Quads GLOW = new Quads();

    /** The bolts painted into one composition before it is played, and the beams they stand in for. */
    static final class Pending {
        final List<Bolt> bolts = new ArrayList<>();
        final List<IFXObject> beams = new ArrayList<>();
        final BoltPainter.Chain chain = new BoltPainter.Chain();
        final long made = System.nanoTime();
        /** 2.1.7: bolts given as heights above the ground (the full-power storm): laid on the terrain when let go. */
        final java.util.Set<Bolt> ground = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        /** 2.1.7: the full-power shockwave's reach (0: none). */
        double wave;
        /** 2.2.5: the full-power flash at the centre. */
        boolean burst;
        /** 2.4.7: the full release's and its aftershocks': no zap, no thunder (the blast and the evil roar are its only sounds). */
        boolean silent;
    }

    /** Bolts set going: at a fixed point, or following {@code entity} (feet, turned with it); where their free ends strike the ground. */
    record Live(List<Bolt> bolts, Entity entity, double x, double y, double z, long start, float end, List<Landing> landings, double wave,
                int color, double flash) {
    }

    /** Where a bolt's free end strikes the ground (world, on its surface), when (ticks from the start), how big, and whether it was heard. */
    static final class Landing {
        final double x;
        final double y;
        final double z;
        final float at;
        final int color;
        final double size;
        final boolean loud;
        final boolean thunder;
        boolean struck;

        Landing(double x, double y, double z, float at, int color, double size, boolean loud, boolean thunder) {
            this.thunder = thunder;
            this.x = x;
            this.y = y;
            this.z = z;
            this.at = at;
            this.color = color;
            this.size = size;
            this.loud = loud;
        }
    }

    private HakiBolts() {
    }

    /** The bolts being painted into {@code fx}. */
    static Pending pending(FX fx) {
        Pending pending = PENDING.get(fx);
        if (pending == null) {
            if (PENDING.size() > 64) {
                long now = System.nanoTime();
                PENDING.values().removeIf(p -> now - p.made > STALE_NANOS);
            }
            pending = new Pending();
            PENDING.put(fx, pending);
        }
        return pending;
    }

    /** 2.4.7: {@code fx}'s lightning lands without a sound. */
    static void silence(FX fx) {
        if (fx != null) {
            pending(fx).silent = true;
        }
    }

    /** {@code HakiVfx.play(entity, fx)}: true if nothing of the composition is left for Photon to play. */
    public static boolean played(FX fx, Entity entity) {
        return entity != null && launch(fx, entity, entity, entity.m_9236_(), 0, 0, 0);
    }

    /** {@code HakiVfx.playAtWorld(entity, x, y, z, fx)}. */
    public static boolean playedAt(FX fx, Entity entity, double x, double y, double z) {
        return entity != null && launch(fx, null, entity, entity.m_9236_(), x, y, z);
    }

    /** {@code HakiVfx.playAtFixed(level, x, y, z, fx)}. */
    public static boolean playedFixed(FX fx, ClientLevel level, double x, double y, double z) {
        return launch(fx, null, null, level, x, y, z);
    }

    /**
     * 2.9.4 (the owner: "Get rid of this fucking old lighting from everywhere, including Gear 5"): Gear 5's effects play through their
     * own {@code GearVfx.playAt(x, y, z, fx)}, which never asked here, so their lightning showed as the original's flat beams and its
     * 3D bolts were never let go. PiecePatch has it ask now: the bolts go, in the Ryou colour of the player nearest (the Gear 5 user).
     */
    public static boolean playedGear(FX fx, double x, double y, double z) {
        silence(fx); // 2.9.5: Gear 5's moves have their own sounds: its lightning brings no crackle of ours
        return launch(fx, null, null, Minecraft.m_91087_().f_91073_, x, y, z);
    }

    /** 2.9.4: the same for the thunderstorm's impact ({@code ThunderstormImpact.play}), which plays its composition itself at {@code at}. */
    public static boolean playedAtPoint(FX fx, Vec3 at) {
        silence(fx); // 2.9.5: as Gear 5's
        return at != null && launch(fx, null, null, Minecraft.m_91087_().f_91073_, at.f_82479_, at.f_82480_, at.f_82481_);
    }

    /** {@code entity}: what the effect rides on (null: a fixed point); {@code owner}: whose it is (its colour), or null. */
    static boolean launch(FX fx, Entity entity, Entity owner, Level level, double x, double y, double z) {
        try {
            Pending pending = PENDING.remove(fx);
            if (pending == null || pending.bolts.isEmpty() || level == null || alive + pending.bolts.size() > MOST) {
                return false;
            }
            List<IFXObject> objects = fx.getMainFX().objects();
            // 2.4.5: by identity through a set (the release's thousands of beams against its thousands of objects, compared pair by
            // pair, were a hitch of their own the moment it was let go)
            java.util.Set<IFXObject> beams = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            beams.addAll(pending.beams);
            objects.removeIf(beams::contains);
            if (entity != null) {
                // 2.2.6 (the owner: "Haki is not meant to come directly from the player's body"): lightning laid on the body it plays on
                // crawls over the shell round it instead (the storm's strands start outside it already, and are found by identity below)
                for (int i = 0; i < pending.bolts.size(); i++) {
                    Bolt bolt = pending.bolts.get(i);
                    if (pending.ground.contains(bolt)) {
                        continue;
                    }
                    double[] moved = com.hexhaki.piece.haki.ConquerorBolts.offBody(bolt.points);
                    if (moved != bolt.points) {
                        pending.bolts.set(i, new Bolt(moved, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed,
                            bolt.dance));
                    }
                }
            }
            if (!pending.ground.isEmpty()) {
                onGround(pending, entity, x, y, z, level);
            }
            // 2.2.4: the lightning is its owner's Ryou colour (the player it plays for, or the nearest one to a fixed point); after the
            // storm is laid on the ground (that finds its bolts by identity) and before its strikes are found (they take its colour)
            int color = colorOf(owner != null ? owner : nearestPlayer(level, x, y, z));
            pending.bolts.replaceAll(bolt -> bolt.tinted(color));
            BoltPainter.point(pending.bolts);
            boolean storm = pending.wave > 0;
            int loud = pending.silent ? 0 : storm ? 6 : 3;
            List<Landing> landings = landings(pending.bolts, entity, x, y, z, level, storm ? 20 : MOST_LANDINGS, loud, storm);
            float end = 0;
            for (Bolt bolt : pending.bolts) {
                end = Math.max(end, bolt.end());
            }
            for (Landing landing : landings) {
                end = Math.max(end, landing.at + (float) BoltMesh.SCORCH);
            }
            if (storm) {
                end = Math.max(end, (float) BoltMesh.WAVE);
            }
            if (pending.burst) {
                end = Math.max(end, (float) BoltMesh.BURST);
            }
            LIVE.add(new Live(pending.bolts, entity, x, y, z, level.m_46467_(), end, landings, pending.wave, color | 0xFF000000,
                pending.burst ? 10.0 + pending.wave * 0.1 : 0.0));
            alive += pending.bolts.size();
            if (!pending.silent && (!landings.isEmpty() || pending.bolts.size() >= 12)) {
                // the composition's own crackle, at its anchor (the original plays the heavy charge and release sounds itself)
                Vec3 at = entity != null ? entity.m_20182_() : new Vec3(x, y, z);
                zap(level, at.f_82479_, at.f_82480_ + 1.0, at.f_82481_, (float) Math.min(0.55, 0.18 + pending.bolts.size() * 0.0004));
            }
            return objects.isEmpty();
        } catch (RuntimeException failure) {
            return false; // the original plays it as it was
        }
    }

    /**
     * The free ends of the coloured strokes (no other stroke carries on from them) that come down on solid ground, one per half block,
     * at most {@link #MOST_LANDINGS}; the first few are heard.
     */
    /**
     * 2.1.7: the storm's bolts laid on the real terrain: each point's height above the ground becomes a height above the top of the
     * ground under it (found looking down from a little above the player, so a cave's ceiling is never taken for the ground), so the
     * web runs over hills and down into valleys and the sky's bolts come down on the ground where they land.
     */
    static void onGround(Pending pending, Entity entity, double x, double y, double z, Level level) {
        double ox = x;
        double oy = y;
        double oz = z;
        double cos = 1.0;
        double sin = 0.0;
        if (entity != null) {
            Vec3 at = entity.m_20182_();
            ox = at.f_82479_;
            oy = at.f_82480_;
            oz = at.f_82481_;
            double turn = Math.toRadians(-90.0 - entity.m_146908_());
            cos = Math.cos(turn);
            sin = Math.sin(turn);
        }
        Map<Long, Double> heights = new java.util.HashMap<>();
        for (int i = 0; i < pending.bolts.size(); i++) {
            Bolt bolt = pending.bolts.get(i);
            if (!pending.ground.contains(bolt)) {
                continue;
            }
            double[] points = bolt.points.clone();
            for (int k = 0; k < bolt.count(); k++) {
                double lx = points[k * 3];
                double lz = points[k * 3 + 2];
                double wx = ox + lx * cos + lz * sin;
                double wz = oz - lx * sin + lz * cos;
                points[k * 3 + 1] += ground(level, wx, wz, oy, heights) - oy;
            }
            pending.bolts.set(i, new Bolt(points, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
        }
    }

    /** The top of the ground in the column at (wx, wz): the first solid block under air, looking down from 14 above {@code near}. */
    static double ground(Level level, double wx, double wz, double near, Map<Long, Double> heights) {
        int bx = (int) Math.floor(wx);
        int bz = (int) Math.floor(wz);
        long key = (long) bx << 32 ^ (bz & 0xFFFFFFFFL);
        Double known = heights.get(key);
        if (known != null) {
            return known;
        }
        int from = (int) Math.floor(near) + 14;
        double found = near;
        boolean air = false;
        for (int by = from; by >= from - 40; by--) {
            boolean solid = !level.m_8055_(BlockPos.m_274561_(bx + 0.5, by + 0.5, bz + 0.5)).m_60795_();
            if (solid && air) {
                found = by + 1.0;
                break;
            }
            air |= !solid;
        }
        heights.put(key, found);
        return found;
    }

    static List<Landing> landings(List<Bolt> bolts, Entity entity, double x, double y, double z, Level level, int most, int loud, boolean thunder) {
        List<Landing> landings = new ArrayList<>();
        java.util.Set<Long> roots = new java.util.HashSet<>();
        for (Bolt bolt : bolts) {
            roots.add(BoltPainter.Chain.key(new double[] {bolt.points[0], bolt.points[1], bolt.points[2]}));
        }
        double ox = x;
        double oy = y;
        double oz = z;
        double cos = 1.0;
        double sin = 0.0;
        if (entity != null) {
            Vec3 at = entity.m_20182_();
            ox = at.f_82479_;
            oy = at.f_82480_;
            oz = at.f_82481_;
            double turn = Math.toRadians(-90.0 - entity.m_146908_());
            cos = Math.cos(turn);
            sin = Math.sin(turn);
        }
        java.util.Set<Long> cells = new java.util.HashSet<>();
        for (Bolt bolt : bolts) {
            int last = bolt.points.length - 3;
            double[] end = {bolt.points[last], bolt.points[last + 1], bolt.points[last + 2]};
            if (bolt.kind != Bolt.CORE && bolt.kind != Bolt.EDGE || roots.contains(BoltPainter.Chain.key(end))) {
                continue;
            }
            double wx = ox + end[0] * cos + end[2] * sin;
            double wy = oy + end[1];
            double wz = oz - end[0] * sin + end[2] * cos;
            BlockPos below = BlockPos.m_274561_(wx, wy - 0.3, wz);
            if (level.m_8055_(below).m_60795_() || !cells.add(Math.round(wx * 2) * 0x9E3779B1L ^ Math.round(wz * 2))) {
                continue;
            }
            double size = Math.max(0.5, Math.min(1.4, bolt.widths[0] * 22.0));
            landings.add(new Landing(wx, below.m_123342_() + 1.0, wz, bolt.delay + bolt.crawl, bolt.color, size, landings.size() < loud,
                thunder));
            if (landings.size() >= most) {
                break;
            }
        }
        return landings;
    }

    /** A short Haki zap (the original's own Armament zap), heard where it happens. */
    public static void zap(Level level, double x, double y, double z, float volume) {
        if (level instanceof ClientLevel client) {
            client.m_7785_(x, y, z, ModSounds.ARMAMENT_AMBIENT_ZAP.get(), SoundSource.PLAYERS, volume,
                0.85F + (float) Math.random() * 0.3F, false);
        }
    }

    /** A landing as it strikes: sparks thrown up off the ground in its colour, and (the first few) a zap. */
    static void strike(ClientLevel level, Landing landing) {
        Vec3 at = new Vec3(landing.x, landing.y + 0.05, landing.z);
        int hot = BoltMesh.mix(landing.color, 0xFFFFFFFF, 0.55);
        for (int i = 0; i < PieceFx.count(6); i++) {
            double a = Math.random() * Math.PI * 2.0;
            double out = 0.06 + Math.random() * 0.12;
            PieceFx.at(PieceFx.SPARK, true, 6 + (int) (Math.random() * 6), at)
                .velocity(new Vec3(Math.cos(a) * out, 0.12 + Math.random() * 0.18, Math.sin(a) * out)).fall(0.045)
                .colors(hot, landing.color & 0x00FFFFFF).size(0.14, 0.02).spawn();
        }
        if (landing.loud && landing.thunder) {
            // the storm's strikes crack like thunder (the original's own Last Stand lightning)
            level.m_7785_(landing.x, landing.y, landing.z, ModSounds.LAST_STAND_LIGHTNING.get(), SoundSource.PLAYERS, 1.6F,
                0.8F + (float) Math.random() * 0.25F, false);
        } else if (landing.loud) {
            zap(level, landing.x, landing.y, landing.z, 0.35F);
        }
    }

    /**
     * 2.2.4: the colour of the lightning an entity throws: a player's Ryou colour (every player has one from their first login), the
     * neutral pale steel for anyone else (a pirate captain) or while a player's has not arrived yet.
     */
    static int colorOf(Entity owner) {
        if (!(owner instanceof Player)) {
            // 2.4.9: a Conqueror hit on a mob is drawn in the colour of the player whose release struck it (HakiTint)
            int struck = HakiTint.hitColour();
            if (struck != 0) {
                return struck & 0xFFFFFF;
            }
        }
        return (owner instanceof Player ? RyouColors.orNeutral(owner.m_19879_()) : RyouPalette.NEUTRAL) & 0xFFFFFF;
    }

    /** The player nearest a fixed point (a world effect's), within 24 blocks, or null. */
    static Entity nearestPlayer(Level level, double x, double y, double z) {
        if (!(level instanceof ClientLevel client)) {
            return null;
        }
        Entity best = null;
        double bestDistance = 24.0 * 24.0;
        for (Player player : client.m_6907_()) {
            Vec3 at = player.m_20182_();
            double d = (at.f_82479_ - x) * (at.f_82479_ - x) + (at.f_82480_ - y) * (at.f_82480_ - y) + (at.f_82481_ - z) * (at.f_82481_ - z);
            if (d < bestDistance) {
                bestDistance = d;
                best = player;
            }
        }
        return best;
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || LIVE.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            LIVE.clear();
            alive = 0;
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            LIVE.clear(); // lightning never stops the game
            alive = 0;
        }
    }

    /** How much the glow shows under this sky: 1 at night, half at noon. */
    public static double shine(ClientLevel level, float partial) {
        double sky = level.m_104805_(partial);
        return 1.0 - 0.5 * Math.max(0.0, Math.min(1.0, (sky - 0.25) / 0.75));
    }

    /**
     * 2.2.6: the bolts' key light for quads drawn through {@code matrix}: {@link BoltMesh#VIEW_LIGHT} (above and to the left of the
     * view) taken back into the frame the quads are built in, so every tube is lit from the same side of the screen.
     */
    static void light(Matrix4f matrix) {
        Matrix4f inverse = new Matrix4f(matrix).invert();
        Vector3f origin = inverse.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        double[] l = BoltMesh.VIEW_LIGHT;
        Vector3f toward = inverse.transformPosition(new Vector3f((float) l[0], (float) l[1], (float) l[2]));
        BoltMesh.light(toward.x - origin.x, toward.y - origin.y, toward.z - origin.z);
    }

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        long now = level.m_46467_();
        double shine = shine(level, partial);
        light(pose.m_85850_().m_252922_());
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        // 2.5.2: the view's forward, in the eye-centred world the bolts are placed in (strokes wholly outside the view are not built)
        Matrix4f inverse = new Matrix4f(pose.m_85850_().m_252922_()).invert();
        Vector3f origin = inverse.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f tip = inverse.transformPosition(new Vector3f(0.0F, 0.0F, -1.0F));
        double ax = tip.x - origin.x;
        double ay = tip.y - origin.y;
        double az = tip.z - origin.z;
        double al = Math.max(1.0E-9, Math.sqrt(ax * ax + ay * ay + az * az));
        double[] ahead = {ax / al, ay / al, az / al};
        double[] centre = new double[3];
        Iterator<Live> it = LIVE.iterator();
        while (it.hasNext()) {
            Live live = it.next();
            double ticks = (now - live.start) + partial;
            if (ticks > live.end + 1 || live.entity != null && (live.entity.m_213877_() || live.entity.m_9236_() != level)) {
                it.remove();
                alive -= live.bolts.size();
                continue;
            }
            BoltMesh.Frame frame;
            if (live.entity != null) {
                Vec3 at = live.entity.m_20318_(partial);
                float yaw = live.entity.f_19859_ + wrap(live.entity.m_146908_() - live.entity.f_19859_) * partial;
                frame = BoltMesh.Frame.turned(at.f_82479_ - cam.f_82479_, at.f_82480_ - cam.f_82480_, at.f_82481_ - cam.f_82481_,
                    Math.toRadians(-90.0 - yaw));
            } else {
                frame = BoltMesh.Frame.at(live.x - cam.f_82479_, live.y - cam.f_82480_, live.z - cam.f_82481_);
            }
            for (Bolt bolt : live.bolts) {
                double[] b = bolt.bounds();
                frame.place(b, centre, 0);
                if (BoltMesh.seen(centre[0], centre[1], centre[2], b[3], ahead[0], ahead[1], ahead[2])) {
                    BoltMesh.build(bolt, frame, ticks - bolt.delay, PIXEL, shine, sink);
                }
            }
            if (live.wave > 0 || live.flash > 0) {
                double cx = (live.entity != null ? live.entity.m_20318_(partial).f_82479_ : live.x) - cam.f_82479_;
                double cy = (live.entity != null ? live.entity.m_20318_(partial).f_82480_ : live.y) - cam.f_82480_;
                double cz = (live.entity != null ? live.entity.m_20318_(partial).f_82481_ : live.z) - cam.f_82481_;
                if (live.wave > 0) {
                    BoltMesh.ring(cx, cy, cz, ticks, live.wave, live.color, shine, sink);
                }
                if (live.flash > 0) {
                    // 2.2.5: the flash the storm tears out of, at the chest (2.3.6: a clash's, smaller, where the blows met)
                    BoltMesh.burst(cx, cy + com.hexhaki.piece.haki.ConquerorStorm.CHEST, cz, ticks, live.flash, live.color, shine, sink);
                }
            }
            for (Landing landing : live.landings) {
                double age = ticks - landing.at;
                if (age >= 0 && !landing.struck) {
                    landing.struck = true;
                    strike(level, landing);
                }
                BoltMesh.landing(landing.x - cam.f_82479_, landing.y - cam.f_82480_, landing.z - cam.f_82481_, age, landing.color, landing.size,
                    shine, sink);
            }
        }
        write(Minecraft.m_91087_().m_91269_().m_110104_(), pose.m_85850_().m_252922_(), pose.m_85850_().m_252943_(), 0, 0, 0, true);
    }

    /**
     * 2.3.6: two Ryou users' blows met at ({@code x}, {@code y}, {@code z}): both colours tear out of it in a sphere
     * ({@link com.hexhaki.piece.haki.ClashBurst}), each colour with its own flash on the point; those that come down run along the ground
     * {@code floor} blocks under it.
     */
    public static void clash(ClientLevel level, double x, double y, double z, double floor, float power, int colorA, int colorB, long seed) {
        if (level == null) {
            return;
        }
        try {
            List<Bolt> all = com.hexhaki.piece.haki.ClashBurst.bolts(power, colorA, colorB, floor, seed);
            if (alive + all.size() > MOST) {
                return;
            }
            // two strokes a strand: the strands of the first colour are the even ones
            List<Bolt> first = new ArrayList<>();
            List<Bolt> second = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                ((i / 2) % 2 == 0 ? first : second).add(all.get(i));
            }
            double chest = com.hexhaki.piece.haki.ConquerorStorm.CHEST;
            double flash = 1.6 + 0.7 * power;
            long now = level.m_46467_();
            float end = (float) BoltMesh.BURST;
            for (Bolt bolt : all) {
                end = Math.max(end, bolt.end());
            }
            // the anchor a chest below the point: the flash is drawn a chest above its anchor; the bolts are lifted to the point
            List<Bolt> liftedA = lift(first, chest);
            List<Bolt> liftedB = lift(second, chest);
            // no shock ring (the storm's, this small, lies on the ground as a plate): the dust is ClashFx's
            LIVE.add(new Live(liftedA, null, x, y - chest, z, now, end, new ArrayList<>(), 0.0, colorA | 0xFF000000, flash));
            LIVE.add(new Live(liftedB, null, x, y - chest, z, now, end, new ArrayList<>(), 0.0, colorB | 0xFF000000, flash * 0.7));
            alive += all.size();
            zap(level, x, y, z, 0.5F);
        } catch (RuntimeException failure) {
            // lightning never stops the game
        }
    }

    /** The bolts moved {@code up} blocks (their points are from the clash point; the anchor sits a chest under it). */
    /**
     * 2.9.1: the Conqueror's lightning sparking off its user (haki/ConquerorSparks): {@code bolts} (blocks from {@code x, y, z}) let go
     * at once, a flash of {@code flash} at the chest over that point if above 0, the zap heard when {@code loud}. Never past {@link #MOST}.
     */
    public static void sparks(ClientLevel level, Entity owner, double x, double y, double z, List<Bolt> bolts, double flash, float loud) {
        if (level == null || bolts.isEmpty() || alive + bolts.size() > MOST) {
            return;
        }
        // 2.9.4 (the owner: "Instead of using the proper lightning ... that adopts the Rio color ... Let's give him the old version"): the
        // Conqueror's own lightning, painted as the release's is, in its owner's Ryou colour (2.9.1 drew it in the original's red)
        int color = colorOf(owner);
        bolts = new ArrayList<>(bolts);
        bolts.replaceAll(bolt -> bolt.tinted(color));
        BoltPainter.point(bolts);
        float end = (float) BoltMesh.BURST;
        for (Bolt bolt : bolts) {
            end = Math.max(end, bolt.end());
        }
        LIVE.add(new Live(bolts, null, x, y, z, level.m_46467_(), end, new ArrayList<>(), 0.0, color | 0xFF000000, flash));
        alive += bolts.size();
        if (loud > 0.0F) {
            zap(level, x, y + 1.0, z, loud);
        }
    }

    static List<Bolt> lift(List<Bolt> bolts, double up) {
        List<Bolt> out = new ArrayList<>(bolts.size());
        for (Bolt bolt : bolts) {
            double[] p = bolt.points.clone();
            for (int i = 1; i < p.length; i += 3) {
                p[i] += up;
            }
            out.add(new Bolt(p, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
        }
        return out;
    }

    /**
     * Bolts in a model's own frame (a katana's: the pose of the layer drawing it): the eye is found in that frame by undoing the pose,
     * the bolts are built round it and drawn through the pose, so they ride on the blade wherever the animation puts it. {@code ages}
     * holds each bolt's age (ticks after its delay). Used inside the entity pass: the batches are left for the game to draw.
     */
    public static void drawInFrame(PoseStack pose, MultiBufferSource buffers, List<Bolt> bolts, double[] ages, float partial) {
        if (bolts.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double shine = level == null ? 1.0 : shine(level, partial);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Vector3f eye = new Matrix4f(matrix).invert().transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        light(matrix);
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        BoltMesh.Frame frame = BoltMesh.Frame.at(-eye.x, -eye.y, -eye.z);
        for (int i = 0; i < bolts.size(); i++) {
            BoltMesh.build(bolts.get(i), frame, ages[i], PIXEL, shine, sink);
        }
        write(buffers, matrix, pose.m_85850_().m_252943_(), eye.x, eye.y, eye.z, false);
    }

    /**
     * Bolts already placed in camera space (world minus the camera), each at its age, drawn now with the level's camera pose
     * ({@code pose} as RenderLevelStageEvent hands it: turned to the view, not moved): the flying Haki slash's crackle.
     */
    public static void drawInView(PoseStack pose, List<Bolt> bolts, double[] ages, ClientLevel level, float partial) {
        if (bolts.isEmpty()) {
            return;
        }
        double shine = shine(level, partial);
        light(pose.m_85850_().m_252922_());
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        BoltMesh.Frame frame = BoltMesh.Frame.at(0, 0, 0);
        for (int i = 0; i < bolts.size(); i++) {
            BoltMesh.build(bolts.get(i), frame, ages[i], PIXEL, shine, sink);
        }
        write(Minecraft.m_91087_().m_91269_().m_110104_(), pose.m_85850_().m_252922_(), pose.m_85850_().m_252943_(), 0, 0, 0, true);
    }

    /** The gathered quads, moved by (ox, oy, oz) and through {@code matrix}: the ink batch, then the glow batch. */
    static void write(MultiBufferSource buffers, Matrix4f matrix, Matrix3f normal, double ox, double oy, double oz, boolean end) {
        if (SOLID.size > 0) {
            RenderType ink = RenderType.m_269058_();
            VertexConsumer buffer = buffers.m_6299_(ink);
            for (int q = 0; q < SOLID.size; q++) {
                for (int k = 0; k < 4; k++) {
                    int c = SOLID.colors[q * 4 + k];
                    buffer.m_252986_(matrix, (float) (SOLID.xyz[q * 12 + k * 3] + ox), (float) (SOLID.xyz[q * 12 + k * 3 + 1] + oy),
                            (float) (SOLID.xyz[q * 12 + k * 3 + 2] + oz))
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F)
                        .m_85969_(0xF000F0).m_5752_();
                }
            }
            if (end && buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109912_(ink);
            }
        }
        if (GLOW.size > 0) {
            // 2.1.9: alpha blended, not additive (the owner's sword swings came out black: some setups draw additive geometry opaque).
            // A glow's colour is its light (its brightness is its strength): drawn as its hue at full brightness, as opaque as it was
            // bright; every corner of a quad takes the hue of its brightest, so a glow fades out, never darkens, toward its rim.
            RenderType light = RenderType.m_269166_();
            VertexConsumer buffer = buffers.m_6299_(light);
            for (int q = 0; q < GLOW.size; q++) {
                int top = 0;
                int hue = 0xFFFFFF;
                for (int k = 0; k < 4; k++) {
                    int c = GLOW.colors[q * 4 + k];
                    int bright = Math.max((c >>> 16) & 255, Math.max((c >>> 8) & 255, c & 255));
                    if (bright > top) {
                        top = bright;
                        hue = c;
                    }
                }
                float scale = top > 0 ? 1.0F / top : 0.0F;
                float r = ((hue >>> 16) & 255) * scale;
                float g = ((hue >>> 8) & 255) * scale;
                float b = (hue & 255) * scale;
                for (int k = 0; k < 4; k++) {
                    int c = GLOW.colors[q * 4 + k];
                    float alpha = Math.max((c >>> 16) & 255, Math.max((c >>> 8) & 255, c & 255)) / 255.0F * 0.9F;
                    buffer.m_252986_(matrix, (float) (GLOW.xyz[q * 12 + k * 3] + ox), (float) (GLOW.xyz[q * 12 + k * 3 + 1] + oy),
                            (float) (GLOW.xyz[q * 12 + k * 3 + 2] + oz))
                        .m_85950_(r, g, b, alpha).m_5752_();
                }
            }
            if (end && buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109912_(light);
            }
        }
    }

    static float wrap(float degrees) {
        float d = degrees % 360.0F;
        if (d >= 180.0F) {
            d -= 360.0F;
        }
        if (d < -180.0F) {
            d += 360.0F;
        }
        return d;
    }

    /** Quads gathered for one batch (reused frame to frame). */
    static final class Quads {
        float[] xyz = new float[12 * 1024];
        int[] colors = new int[4 * 1024];
        int size;

        void clear() {
            this.size = 0;
        }

        void add(double[] v, int[] c) {
            if ((this.size + 1) * 12 > this.xyz.length) {
                this.xyz = java.util.Arrays.copyOf(this.xyz, this.xyz.length * 2);
                this.colors = java.util.Arrays.copyOf(this.colors, this.colors.length * 2);
            }
            for (int i = 0; i < 12; i++) {
                this.xyz[this.size * 12 + i] = (float) v[i];
            }
            System.arraycopy(c, 0, this.colors, this.size * 4, 4);
            this.size++;
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING.clear();
        LIVE.clear();
        alive = 0;
    }
}
