package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.gura.GuraSounds;
import com.hexhaki.piece.gura.Heave;
import com.hexhaki.piece.network.PieceNetwork;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.1.6, the Gura Gura no Mi's ultimate on the End key. The owner (verbatim, with docs/handoff/gura-ref/ultimate-whitebeard-punch.gif:
 * Whitebeard, the right fist cocked high beside his head in a bright white bubble): "the user pulls the fist, like the right fist, like
 * back, like more to the left, like a little further away from the face, but like around that area. He charges up, and like this is a
 * charge move. It's not a tap. It's no option to be a tap. You have to charge it fully for it to activate. It's basically the right click
 * on steroids ... It charges the bubble around the fist and gets bigger and bigger ... and then it's released. The entire floor in front
 * like bends and breaks into like squares ... it just shatters everything in front of it ... a massive like right click, basically, with
 * a bigger, wider range and like in a big cone in front of the user, causing the floor to like shift and like go up and like break into
 * many fissures and just up and down. Like the cooldown of this move should be absolutely gigantic. It should be like 5 minutes cooldown.
 * If Haki is activated, I feel like the lightning from Conqueror, the thick lightning, should like shoot everywhere from like where the
 * floor is breaking, and actually also stun and all, like just make this an absolutely crazy move."
 *
 * <p>Held (End, client/gura/GuraKeys): gura_ult_charge, the bubble ({@link #BUBBLE}) swelling on the cocked fist for {@link #FULL}
 * ticks, the ground trembling more and more round the feet, the player slowed. Let go before it is full: nothing (no tap, no cooldown).
 * Full, let go (or held {@link #HOLD_MOST}): gura_ult_release, the air shatters at the fist, a shock and a wave go out over a cone
 * {@link #CONE} degrees each side, {@link #REACH} blocks (x {@link GuraMoves#HAKI_RANGE} with Ryou on): the ground in it heaves and breaks
 * into squares (gura/Heave, drawn with the world's own blocks on every client), fissures tear open along it ({@link GuraMoves.Rift},
 * self-repairing), craters burst on its middle line, everyone in it is struck as the wave reaches them, thrown up and knocked down. With
 * the Conqueror's Haki and Ryou on: thick Conqueror's lightning out of the breaking ground all along the wave, and those struck stunned
 * (asked for by name for this move). Cooldown {@link #COOLDOWN} (5 minutes).
 */
public final class GuraUltimate {
    /** The End key's ability number (client/gura/GuraKeys sends it; the original's slots are 0 to 8). */
    static final int SLOT = 40;
    /** Its bubble's id in BUBBLE_ON/OFF (client/gura/GuraFx: swelling over {@link #FULL}). */
    static final int BUBBLE = 3;
    /** Ticks to a full charge; the longest it may be held before it lets itself go. */
    public static final int FULL = 80;
    static final int HOLD_MOST = 220;
    public static final int COOLDOWN = 6000;
    static final double REACH = Heave.REACH;
    static final double CONE = Heave.CONE;
    static final double WAVE = Heave.WAVE;
    static final float DAMAGE = 18.0F;
    /** How long those struck stay down; with the Conqueror's Haki, how long they are stunned. */
    static final int DOWN = 100;
    static final int STUN = 200;

    private GuraUltimate() {
    }

    static void press(ServerPlayer player, GuraMoves.State s, long now) {
        if (!GuraMoves.ready(player, GuraKit.ULTIMATE)) {
            return;
        }
        s.ultSince = now;
        GuraMoves.animate(player, "gura_ult_charge");
        GuraMoves.fx(player, GuraMoves.BUBBLE_ON, BUBBLE);
        GuraMoves.sound(player, player.m_20182_(), GuraSounds.BOOM, 1.0F, 1.3F);
    }

    static void letGo(ServerPlayer player, GuraMoves.State s) {
        long held = GuraMoves.now(player) - s.ultSince;
        if (held >= FULL) {
            release(player, s);
            return;
        }
        // "It's not a tap ... You have to charge it fully for it to activate"
        s.ultSince = -1;
        GuraMoves.fx(player, GuraMoves.BUBBLE_OFF, BUBBLE);
        GuraMoves.animate(player, "gura_ult_drop");
        player.m_5661_(Component.m_237113_("§7Charge it fully to let it go"), true);
    }

    /** Every tick while it charges: the player held back, the ground trembling more as the bubble grows, lightning round the fist. */
    static void tick(ServerPlayer player, GuraMoves.State s, long now) {
        long held = now - s.ultSince;
        if (held >= HOLD_MOST) {
            release(player, s);
            return;
        }
        Vec3 v = player.m_20184_();
        player.m_20334_(v.f_82479_ * 0.4, v.f_82480_, v.f_82481_ * 0.4);
        double g = Math.min(1.0, held / (double) FULL);
        ServerLevel level = (ServerLevel) player.m_9236_();
        if (held % 10 == 0) {
            Impact.quake(level, player.m_20182_(), 12.0 + 20.0 * g, (float) (0.15 + 0.45 * g), 10);
        }
        if (held % 20 == 10) {
            GuraMoves.fx(player, GuraMoves.TREMOR, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 2.0 + 3.5 * g, GuraMoves.seed());
        }
        if (held >= 3 && held % 5 == 0) {
            ConquerorAura.mixed(player, GuraMoves.fist(player), 1.4 + 2.6 * g, 3 + (int) (5 * g), -1.6, true, 1.0 + 0.6 * g);
        }
        if (held == FULL) {
            // full: the bubble flares (a crack in the air at the fist), and it may be let go
            Vec3 fist = GuraMoves.fist(player);
            GuraMoves.fx(player, GuraMoves.CRACK, 0, fist, player.m_20154_(), 0, 0, 0, 0.7, GuraMoves.seed());
            GuraMoves.sound(player, fist, GuraSounds.CRACK, 1.2F, 0.7F);
            GuraMoves.sound(player, fist, GuraSounds.BOOM, 1.4F, 0.75F);
        }
    }

    static void release(ServerPlayer player, GuraMoves.State s) {
        s.ultSince = -1;
        GuraMoves.fx(player, GuraMoves.BUBBLE_OFF, BUBBLE);
        GuraMoves.cool(player, GuraKit.ULTIMATE, COOLDOWN);
        long now = GuraMoves.now(player);
        s.busyUntil = now + 26;
        GuraMoves.animate(player, "gura_ult_release");
        GuraMoves.later(player, 3, () -> strike(player));
    }

    /** The fist driven out: the air shatters, the shock and the wave go out over the cone. */
    static void strike(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 flat = GuraMoves.flat(player);
        Vec3 fist = GuraMoves.fist(player);
        double b = GuraMoves.boost(player);
        double reach = REACH * b;
        Vec3 feet = player.m_20182_();
        Vec3 ground = GuraMoves.ground(level, feet.m_82520_(0, 0.5, 0), 1.0, 4.0);
        Vec3 base = ground != null ? ground : feet;
        boolean haki = ConquerorAura.awakened(player);
        int seed = GuraMoves.seed();
        GuraMoves.fx(player, GuraMoves.CRACK, 1, fist.m_82549_(flat.m_82490_(0.8)), flat, 0, 0, 0, 3.2, GuraMoves.seed());
        GuraMoves.fx(player, GuraMoves.SHOCK, 1, fist, flat, 0.7, 1.0, 0, reach, GuraMoves.seed());
        GuraMoves.fx(player, GuraMoves.HEAVE, 0, base, flat, CONE, WAVE, haki ? 1.0 : 0.0, reach, seed);
        ConquerorAura.mixed(player, fist.m_82549_(flat.m_82490_(0.8)), 6.0, 12, -1.5, false, 2.2);
        // fissures fanned across the cone (narrow, self-repairing), the middle one the longest
        double[] fan = {-0.8, -0.42, 0.0, 0.42, 0.8};
        for (int i = 0; i < fan.length; i++) {
            double a = Math.toRadians(CONE * fan[i]);
            Vec3 dir = new Vec3(flat.f_82479_ * Math.cos(a) - flat.f_82481_ * Math.sin(a), 0, flat.f_82479_ * Math.sin(a) + flat.f_82481_ * Math.cos(a));
            Vec3 start = base.m_82549_(dir.m_82490_(3.0));
            Vec3 g = GuraMoves.ground(level, start, 2.0, 6.0);
            if (g == null) {
                continue;
            }
            double length = (reach - 3.0) * (i == 2 ? 0.95 : 0.7 + 0.08 * Math.abs(i - 2));
            int ticks = (int) Math.ceil(length / GuraMoves.RIFT_STEP);
            GuraMoves.fx(player, GuraMoves.RIFT, GuraMoves.seed(), g, dir, -1.0, 0, 0, length, ticks);
            GuraMoves.live(new GuraMoves.Rift(level, player, g, dir, length, b > 1.0, true));
        }
        // craters bursting on the middle line as the wave passes
        int craters = 0;
        for (double d = 8.0; d < reach && craters < 4; d += reach / 4.0) {
            Vec3 g = GuraMoves.ground(level, base.m_82549_(flat.m_82490_(d)).m_82520_(0, 2.0, 0), 3.0, 8.0);
            if (g == null) {
                continue;
            }
            craters++;
            double radius = 2.4 * (b > 1.0 ? 1.3 : 1.0);
            int s = GuraMoves.seed();
            GuraMoves.later(player, (int) (d / WAVE), () -> {
                Craters.open(level, player, g, radius);
                GuraMoves.fx(player, GuraMoves.QUAKE, 0, g, flat, 0.9, 0, 0, radius * 1.4, s);
                GuraMoves.sound(player, g, GuraSounds.CRUMBLE, 1.8F, 0.8F);
            });
        }
        // everyone in the cone struck as the wave reaches them
        double cos = Math.cos(Math.toRadians(CONE + 4.0));
        List<LivingEntity> struck = new ArrayList<>();
        for (LivingEntity body : PikaMoves.foes(player, base.m_82549_(flat.m_82490_(reach * 0.5)), reach * 0.5 + 3.0)) {
            Vec3 rel = body.m_20182_().m_82546_(base);
            Vec3 out = new Vec3(rel.f_82479_, 0, rel.f_82481_);
            double d = out.m_82553_();
            if (d > reach + 1.0 || Math.abs(rel.f_82480_) > 12.0 || d > 1.0 && out.m_82490_(1.0 / d).m_82526_(flat) < cos) {
                continue;
            }
            struck.add(body);
            Vec3 away = d < 1.0E-3 ? flat : out.m_82490_(1.0 / d);
            Vec3 thrown = away.m_82490_(1.6).m_82549_(flat.m_82490_(0.6)).m_82520_(0, 1.5, 0);
            GuraMoves.later(player, (int) (d / WAVE), () -> {
                if (!body.m_6084_()) {
                    return;
                }
                if (PikaMoves.hit(player, body, DAMAGE, thrown, true)) {
                    if (haki) {
                        ConquerorRelease.hold(level, body, STUN);
                    } else {
                        GuraMoves.downOnLanding(level, body, DOWN);
                    }
                }
                ConquerorAura.mixed(player, com.hexhaki.piece.server.StyleCombat.middle(body), 2.6, 5, -1.0, false, 1.6);
            });
        }
        struck.sort((x, y) -> Double.compare(x.m_20182_().m_82557_(base), y.m_20182_().m_82557_(base)));
        if (!struck.isEmpty()) {
            LivingEntity first = struck.get(0);
            Vec3 at = com.hexhaki.piece.server.StyleCombat.middle(first);
            GuraMoves.later(player, (int) (at.m_82554_(base) / WAVE), () -> PieceNetwork.around(player, new com.hexhaki.piece.network.S2CClash(
                GuraMoves.frame(), at.f_82479_, at.f_82480_, at.f_82481_, (float) flat.f_82479_, (float) flat.f_82481_, 3.0F, 0, 0,
                player.m_19879_(), first.m_19879_(), 0)));
        }
        // the wave front: the ground groaning along it, and (with the Conqueror's Haki) thick lightning out of the breaking ground
        GuraMoves.live(new Front(level, player, base, flat, reach, haki, seed));
        Impact.quake(level, base, 70.0 * b, 1.0F, 30);
        GuraMoves.sound(player, fist, GuraSounds.BOOM, 2.6F, 0.6F);
        GuraMoves.sound(player, fist, GuraSounds.BOOM, 2.0F, 0.85F);
        GuraMoves.sound(player, fist, GuraSounds.CRACK, 1.8F, 0.75F);
        GuraMoves.sound(player, base, GuraSounds.QUAKE, 2.4F, 0.85F);
    }

    /** The wave going out over the cone: lightning and the ground's sound along its front. */
    static final class Front implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 base;
        final Vec3 flat;
        final double reach;
        final boolean haki;
        final java.util.SplittableRandom random;
        int age;

        Front(ServerLevel level, ServerPlayer owner, Vec3 base, Vec3 flat, double reach, boolean haki, int seed) {
            this.level = level;
            this.owner = owner;
            this.base = base;
            this.flat = flat;
            this.reach = reach;
            this.haki = haki;
            this.random = new java.util.SplittableRandom(seed * 31L + 7L);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            double d = Math.min(this.reach, this.age * WAVE);
            if (this.haki && this.age % 2 == 0) {
                // "the thick lightning, should like shoot everywhere from like where the floor is breaking"
                for (int k = 0; k < 2; k++) {
                    double a = Math.toRadians(CONE * (this.random.nextDouble() * 2.0 - 1.0));
                    double r = Math.max(4.0, d - this.random.nextDouble() * 6.0);
                    Vec3 dir = new Vec3(this.flat.f_82479_ * Math.cos(a) - this.flat.f_82481_ * Math.sin(a), 0,
                        this.flat.f_82479_ * Math.sin(a) + this.flat.f_82481_ * Math.cos(a));
                    Vec3 g = GuraMoves.ground(this.level, this.base.m_82549_(dir.m_82490_(r)).m_82520_(0, 3.0, 0), 3.0, 10.0);
                    if (g != null) {
                        ConquerorAura.mixed(this.owner, g.m_82520_(0, 0.4, 0), 5.0 + 2.0 * this.random.nextDouble(), 9, -0.4, false, 2.2);
                    }
                }
            }
            if (this.age % 6 == 3 && this.age < 30) {
                Vec3 at = this.base.m_82549_(this.flat.m_82490_(d));
                GuraMoves.sound(this.owner, at, GuraSounds.CRUMBLE, 1.6F, 0.75F + 0.03F * (this.age % 7));
            }
            if (this.age % 4 == 0) {
                Impact.quake(this.level, this.base.m_82549_(this.flat.m_82490_(d)), 24.0, 0.6F, 8);
            }
            return d >= this.reach || !this.owner.m_6084_();
        }
    }
}
