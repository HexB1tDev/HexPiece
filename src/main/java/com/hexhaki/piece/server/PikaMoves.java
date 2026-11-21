package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fruit.Fruits;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.pika.PikaSounds;
import com.hexhaki.fx.Impact;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.8: the Pika Pika no Mi's moves (server/PikaKit), as Borsalino uses them. Every hit, throw and move of the player is worked out
 * here on the server's ticks, each in step with its animation (tools/pika_anims.py keeps the same beats); what everyone sees is sent
 * as {@link S2CUoFx} kinds from {@link #LASER} on (client/pika/PikaFx draws them in 3D).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class PikaMoves {
    // ---- what the client draws (S2CUoFx kinds; 32 and up are the Light fruit's)
    /** A quick laser: x, y, z the fingertip, a where it struck, size its width. */
    public static final int LASER = 32;
    /** Light gathering at the fingertip (user), and let go. */
    public static final int CHARGE_ON = 33;
    public static final int CHARGE_OFF = 34;
    /** The charged beam: x, y, z the fingertip, a its end, size its width. */
    public static final int BEAM = 35;
    /** Light blowing up: x, y, z, size its radius. */
    public static final int BURST = 36;
    /** A kick or a cut landing: x, y, z where, a the way it went, ticks the step, size 1 when it struck someone. */
    public static final int KICK = 37;
    /** Yata no Kagami forming between the hands (user). */
    public static final int YATA_FORM = 38;
    /** Yata no Kagami's way: x, y, z from (the feet), a to. */
    public static final int YATA_PATH = 39;
    /** Yasakani no Magatama on and off (user): the light in the crossed hands. */
    public static final int VOLLEY_ON = 40;
    public static final int VOLLEY_OFF = 41;
    /** One bead: x, y, z from, a where it lands, ticks its flight, size its size. */
    public static final int BEAD = 42;
    /** Ama no Murakumo out (user, ticks how long) and away. */
    public static final int SWORD_ON = 43;
    public static final int SWORD_OFF = 44;
    /** A cut of the sword: x, y, z the hips, a the swing's axis, b (start, sweep, radius), ticks the cut. */
    public static final int SLASH = 45;
    /** The wave of light the fifth cut throws: x, y, z, a its step a tick, ticks its flight, size its width. */
    public static final int WAVE = 46;
    /** The light gathering in the leg (user), and let go. */
    public static final int KICK_CHARGE_ON = 47;
    public static final int KICK_CHARGE_OFF = 48;
    /** The speed-of-light kick: x, y, z from, a where it landed, id the one kicked (-1 none), size the charge. */
    public static final int LIGHT_KICK = 49;
    /** One clone: x, y, z where it rises from, a where it runs to, id who it runs at (-1 a place), ticks its number. */
    public static final int CLONE = 51;
    /** A blow through the body of light: x, y, z. */
    public static final int DODGE = 52;
    /** Falling on them as light: x, y, z where it lands, a from, size its radius. */
    public static final int DIVE = 53;
    /** Streaming as light (user) on and off. */
    public static final int STREAM_ON = 54;
    public static final int STREAM_OFF = 55;

    // ---- numbers
    static final String YATA_KEY = "pika_yata";
    static final String YATA_TIME = "pika_yata_t";
    static final int YATA_MAX = 3;
    static final int YATA_RECHARGE = 80;
    static final double YATA_RANGE = 60.0;
    static final double LASER_RANGE = 70.0;
    static final double BEAM_RANGE = 90.0;
    static final int BEAM_FULL = 40;
    static final int TAP = 6;
    static final int VOLLEY_MAX = 56;
    static final int VOLLEY_EVERY = 2;
    static final int VOLLEY_WINDUP = 6;
    static final double VOLLEY_SPREAD = 8.0;
    static final int SWORD_TICKS = 400;
    static final int KICK_FULL = 40;
    static final double KICK_RANGE = 45.0;
    static final int CLONE_COUNT = 7;
    static final int STREAM_MAX = 60;
    static final int COMBO_GAP = 14;

    /** Each player's state of the kit. */
    static final class State {
        int kick;
        long kickAt = Long.MIN_VALUE;
        int cut;
        long cutAt = Long.MIN_VALUE;
        long busyUntil;
        long swordUntil;
        boolean guard;
        long guardSince;
        long chargeSince = -1;
        long kickChargeSince = -1;
        long volleySince = -1;
        boolean volleyFocus;
        int volleyBeads;
        long streamSince = -1;
        int seed;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static int next;

    private PikaMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double bz, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
            (float) a.f_82480_, (float) a.f_82481_, (float) bx, (float) by, (float) bz, (float) size, ticks));
    }

    static void fx(ServerPlayer player, int kind) {
        PieceNetwork.around(player, S2CUoFx.of(kind, 0, player.m_19879_()));
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        // 2.9.0: the Light fruit's own web recordings (pika/PikaSounds), the original's sounds by their plain names otherwise
        SoundEvent event = name.startsWith("piece.") ? com.hexhaki.piece.PieceSounds.event(name) : Fruits.sound(name);
        if (event != null) {
            player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, event, SoundSource.PLAYERS, volume, pitch);
        }
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return move.cooldown() == null || FruitData.ready(player, move.cooldown());
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        FruitData.cooldown(player, move.cooldown(), ticks);
        FruitServer.sync(player);
    }

    static void animate(ServerPlayer player, String name) {
        HakiServerController.animate(player, name, 1);
    }

    static void later(ServerPlayer player, int ticks, Runnable action) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new UoMoves.Later(level, now(player) + ticks, () -> {
            if (player.m_6084_()) {
                action.run();
            }
        }));
    }

    static boolean sneak(int flags) {
        return (flags & 1) != 0;
    }

    static boolean sprint(int flags) {
        return (flags & 2) != 0;
    }

    static boolean air(int flags) {
        return (flags & 4) != 0;
    }

    static boolean sword(ServerPlayer player) {
        return state(player).swordUntil > now(player);
    }

    // ------------------------------------------------------------------------------------------------------ aiming

    /** The right fingertip, out in front of the right shoulder at the eye's height less a little. */
    static Vec3 fingertip(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 side = look.m_82537_(new Vec3(0, 1, 0));
        side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
        return player.m_146892_().m_82549_(look.m_82490_(0.9)).m_82549_(side.m_82490_(0.36)).m_82520_(0.0, -0.22, 0.0);
    }

    /** The foe under the crosshair within {@code range} (a few degrees' allowance), or null. */
    static LivingEntity aimed(ServerPlayer player, double range, double slack) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 eye = player.m_146892_();
        Vec3 look = player.m_20154_();
        Vec3 end = eye.m_82549_(look.m_82490_(range));
        BlockHitResult wall = level.m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(eye) : range;
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(eye, eye.m_82549_(look.m_82490_(reach))).m_82400_(slack * reach + 2.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 rel = StyleCombat.middle(body).m_82546_(eye);
            double along = rel.m_82526_(look);
            if (along < 0.5 || along > reach + 1.0) {
                continue;
            }
            double off = rel.m_82546_(look.m_82490_(along)).m_82553_();
            if (off > body.m_20205_() * 0.5 + 0.6 + along * slack) {
                continue;
            }
            double score = off / Math.max(1.0, along) + along * 0.002;
            if (score < bestScore) {
                bestScore = score;
                best = body;
            }
        }
        return best;
    }

    /** Where the eye's line meets a block within {@code range}, or its end. */
    static Vec3 point(ServerPlayer player, double range) {
        Vec3 eye = player.m_146892_();
        Vec3 end = eye.m_82549_(player.m_20154_().m_82490_(range));
        BlockHitResult wall = player.m_9236_().m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : end;
    }

    static List<LivingEntity> foes(ServerPlayer player, Vec3 at, double radius) {
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity body : player.m_9236_().m_6443_(LivingEntity.class, UoMoves.box(at, at).m_82400_(radius + 1.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            if (StyleCombat.middle(body).m_82554_(at) <= radius + body.m_20205_() * 0.5) {
                out.add(body);
            }
        }
        return out;
    }

    /** A hit: damage, then a throw (and Impact's weight). */
    static boolean hit(ServerPlayer player, LivingEntity body, float damage, Vec3 thrown, boolean heavy) {
        DamageSource source = StyleCombat.of(player);
        body.f_19802_ = 0;
        if (!body.m_6469_(source, damage)) {
            return false;
        }
        Impact.hit(player, body, StyleCombat.spec(heavy ? com.hexhaki.piece.style.Hit.Weight.HEAVY : com.hexhaki.piece.style.Hit.Weight.MEDIUM,
            thrown));
        return true;
    }

    /** Every foe in front within {@code reach} and {@code degrees} of the look: hit; returns how many. */
    static int cone(ServerPlayer player, double reach, double degrees, float damage, Vec3 thrown, boolean heavy) {
        Vec3 eye = player.m_146892_();
        Vec3 look = player.m_20154_();
        double cos = Math.cos(Math.toRadians(degrees));
        int n = 0;
        for (LivingEntity body : foes(player, eye.m_82549_(look.m_82490_(reach * 0.5)), reach)) {
            Vec3 rel = StyleCombat.middle(body).m_82546_(eye);
            double d = rel.m_82553_();
            if (d > reach + body.m_20205_() * 0.5 || d > 0.5 && rel.m_82490_(1.0 / d).m_82526_(look) < cos) {
                continue;
            }
            n += hit(player, body, damage, thrown, heavy) ? 1 : 0;
        }
        return n;
    }

    /** Light blowing up at {@code at}: damage falling off, a throw out; nothing broken (the owner's world stays). */
    static void burst(ServerPlayer player, Vec3 at, double radius, float damage, double knock) {
        UoMoves.explode(player, (ServerLevel) player.m_9236_(), at, new Vec3(0, 1, 0), radius, damage, 0, knock);
        fx(player, BURST, 0, at, Vec3.f_82478_, 0, 0, 0, radius, 0);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot, int flags) {
        if (slot == PikaKit.YATA.slot()) {
            if (sneak(flags)) {
                stream(player);
            } else {
                yata(player);
            }
        } else if (slot == PikaKit.YASAKANI.slot()) {
            volley(player, sneak(flags));
        } else if (slot == PikaKit.MURAKUMO.slot()) {
            murakumo(player);
        } else if (slot == PikaKit.LIGHT_KICK.slot()) {
            kickCharge(player);
        } else if (slot == PikaKit.CLONES.slot()) {
            clones(player);
        }
    }

    static void release(ServerPlayer player, int slot) {
        State s = state(player);
        if (slot == PikaKit.YASAKANI.slot() && s.volleySince >= 0) {
            stopVolley(player);
        } else if (slot == PikaKit.LIGHT_KICK.slot() && s.kickChargeSince >= 0) {
            lightKick(player);
        } else if (slot == PikaKit.YATA.slot() && s.streamSince >= 0) {
            stopStream(player);
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: kicks (or cuts)

    static void attack(ServerPlayer player, int flags) {
        State s = state(player);
        long now = now(player);
        if (now < s.busyUntil || s.volleySince >= 0 || s.kickChargeSince >= 0 || s.chargeSince >= 0) {
            return;
        }
        if (sword(player)) {
            cut(player, s, now);
            return;
        }
        if (air(flags) && player.m_146909_() > 50.0F) {
            dive(player, s, now);
            return;
        }
        if (sprint(flags) && !air(flags)) {
            dashKick(player, s, now);
            return;
        }
        int step = now - s.kickAt <= COMBO_GAP ? s.kick % 5 + 1 : 1;
        s.kick = step;
        s.kickAt = now;
        s.busyUntil = now + (step == 5 ? 8 : 5);
        animate(player, "pika_kick_" + step);
        sound(player, player.m_20182_(), PikaSounds.WHOOSH, 0.8F, 0.95F + 0.06F * step);
        Vec3 look = player.m_20154_();
        if (step < 5) {
            later(player, 2, () -> kick(player, step, look));
        } else {
            // the barrage: four kicks too fast to see, then the last that throws them
            for (int i = 0; i < 4; i++) {
                int beat = 1 + i;
                later(player, beat, () -> kick(player, 5, player.m_20154_()));
            }
            later(player, 6, () -> {
                Vec3 at = player.m_146892_().m_82549_(player.m_20154_().m_82490_(2.2));
                int n = cone(player, 4.2, 55, 5.0F, player.m_20154_().m_82490_(2.2).m_82520_(0, 0.45, 0), true);
                fx(player, KICK, 0, at, player.m_20154_(), 0, 0, 0, n > 0 ? 2 : 0, 6);
                sound(player, at, n > 0 ? PikaSounds.HIT_HEAVY : PikaSounds.WHOOSH, 1.1F, 1.0F);
            });
        }
    }

    /** One kick of the five: the cone in front; the rising kick throws them up. */
    static void kick(ServerPlayer player, int step, Vec3 look) {
        Vec3 at = player.m_146892_().m_82549_(player.m_20154_().m_82490_(1.9)).m_82520_(0, step == 4 ? -0.3 : -0.5, 0);
        Vec3 thrown = step == 4 ? new Vec3(look.f_82479_ * 0.3, 1.25, look.f_82481_ * 0.3) : look.m_82490_(step == 5 ? 0.25 : 0.7).m_82520_(0, 0.12, 0);
        int n = cone(player, 3.8, 50, step == 5 ? 2.0F : 2.5F + 0.5F * step, thrown, step == 4);
        fx(player, KICK, 0, at, player.m_20154_(), 0, 0, 0, n > 0 ? 1 : 0, step);
        if (n > 0) {
            sound(player, at, PikaSounds.HIT, 1.0F, 1.0F);
        }
    }

    /** Sprinting: across the distance as light and a kick on arrival. */
    static void dashKick(ServerPlayer player, State s, long now) {
        LivingEntity target = aimed(player, 16.0, 0.08);
        Vec3 from = player.m_20182_();
        Vec3 to;
        if (target != null) {
            to = beside(player, target, 1.6);
        } else {
            to = room(player, from, point(player, 12.0).m_82546_(player.m_20154_().m_82490_(0.8)));
        }
        s.busyUntil = now + 8;
        s.kick = 0;
        animate(player, "pika_dash_kick");
        travel(player, to, target);
        fx(player, YATA_PATH, 0, from, to, 0, 0, 0, 0, 0);
        sound(player, to, PikaSounds.ZIP, 1.0F, 1.0F);
        later(player, 2, () -> {
            int n = cone(player, 4.0, 55, 5.0F, player.m_20154_().m_82490_(2.0).m_82520_(0, 0.5, 0), true);
            Vec3 at = player.m_146892_().m_82549_(player.m_20154_().m_82490_(1.8)).m_82520_(0, -0.4, 0);
            fx(player, KICK, 0, at, player.m_20154_(), 0, 0, 0, n > 0 ? 2 : 0, 7);
            sound(player, at, n > 0 ? PikaSounds.HIT_HEAVY : PikaSounds.WHOOSH, 1.1F, 1.0F);
        });
    }

    /** In the air, looking down: fall on them as light, a burst where it lands. */
    static void dive(ServerPlayer player, State s, long now) {
        Vec3 eye = player.m_146892_();
        BlockHitResult ground = player.m_9236_().m_45547_(new ClipContext(eye, eye.m_82549_(player.m_20154_().m_82490_(40.0)),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (ground.m_6662_() != HitResult.Type.BLOCK) {
            return;
        }
        Vec3 from = player.m_20182_();
        Vec3 land = room(player, from, ground.m_82450_().m_82520_(0, 0.1, 0));
        s.busyUntil = now + 10;
        animate(player, "pika_dive");
        travel(player, land, null);
        player.f_19789_ = 0.0F;
        fx(player, DIVE, 0, land, from, 0, 0, 0, 4.5, 0);
        later(player, 1, () -> {
            UoMoves.explode(player, (ServerLevel) player.m_9236_(), land, new Vec3(0, 1, 0), 4.5, 6.0F, 0, 1.6);
            Impact.quake((ServerLevel) player.m_9236_(), land, 18.0, 0.6F, 10);
            sound(player, land, PikaSounds.EXPLODE, 1.3F, 1.15F);
        });
    }

    /** Beside {@code target}, on the side toward the player, {@code gap} blocks off, facing it. */
    static Vec3 beside(ServerPlayer player, LivingEntity target, double gap) {
        Vec3 t = target.m_20182_();
        Vec3 back = player.m_20182_().m_82546_(t);
        back = new Vec3(back.f_82479_, 0, back.f_82481_);
        back = back.m_82556_() < 1.0E-6 ? player.m_20154_().m_82490_(-1) : back.m_82541_();
        return room(player, t, t.m_82549_(back.m_82490_(gap + target.m_20205_() * 0.5)));
    }

    /** {@code to}, or short of the first block between {@code from} and it (the player's box must fit). */
    static Vec3 room(ServerPlayer player, Vec3 from, Vec3 to) {
        Vec3 eyeFrom = from.m_82520_(0, 1.0, 0);
        Vec3 eyeTo = to.m_82520_(0, 1.0, 0);
        BlockHitResult wall = player.m_9236_().m_45547_(new ClipContext(eyeFrom, eyeTo, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 at = to;
        if (wall.m_6662_() == HitResult.Type.BLOCK) {
            Vec3 way = eyeTo.m_82546_(eyeFrom);
            double length = way.m_82553_();
            double keep = Math.max(0.0, wall.m_82450_().m_82554_(eyeFrom) - 0.8);
            at = length < 1.0E-6 ? from : from.m_82549_(way.m_82490_(keep / length));
        }
        for (int up = 0; up < 3; up++) {
            AABB box = player.m_20191_().m_82383_(at.m_82546_(player.m_20182_()));
            if (player.m_9236_().m_45756_(player, box)) {
                return at;
            }
            at = at.m_82520_(0, 1.0, 0);
        }
        return from;
    }

    /** Moved as light to {@code to} (facing {@code target} if there is one), no fall taken. */
    static void travel(ServerPlayer player, Vec3 to, LivingEntity target) {
        float yaw = player.m_146908_();
        float pitch = player.m_146909_();
        if (target != null) {
            Vec3 d = StyleCombat.middle(target).m_82546_(to.m_82520_(0, player.m_20192_(), 0));
            yaw = (float) Math.toDegrees(Math.atan2(-d.f_82479_, d.f_82481_));
            pitch = (float) -Math.toDegrees(Math.atan2(d.f_82480_, Math.sqrt(d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_)));
        }
        player.m_8999_((ServerLevel) player.m_9236_(), to.f_82479_, to.f_82480_, to.f_82481_, yaw, pitch);
        player.m_5616_(yaw);
        player.m_20334_(0, 0, 0);
        player.f_19864_ = true;
        player.f_19789_ = 0.0F;
    }

    // ------------------------------------------------------------------------------------------------------ Ama no Murakumo's cuts

    static void cut(ServerPlayer player, State s, long now) {
        int step = now - s.cutAt <= COMBO_GAP ? s.cut % 5 + 1 : 1;
        s.cut = step;
        s.cutAt = now;
        s.busyUntil = now + 6;
        animate(player, "pika_sword_" + step);
        sound(player, player.m_20182_(), PikaSounds.SWING, 0.9F, 0.95F + 0.05F * step);
        later(player, 2, () -> {
            Vec3 look = player.m_20154_();
            int n = cone(player, 5.0, 70, 3.5F + 0.5F * step, look.m_82490_(step == 5 ? 1.4 : 0.6).m_82520_(0, 0.15, 0), step == 5);
            // the cut's crescent: across (1, 3), back across (2, 4), down through (5)
            Vec3 hips = player.m_20182_().m_82520_(0, 1.1, 0).m_82549_(look.m_82490_(0.6));
            Vec3 axis = step == 5 ? look.m_82537_(new Vec3(0, 1, 0)).m_82541_() : new Vec3(0, step % 2 == 0 ? -1 : 1, 0).m_82549_(look.m_82490_(0.3));
            fx(player, SLASH, 0, hips, axis, -Math.PI * 0.2 + Math.toRadians(-player.m_146908_()), Math.PI * 1.1, 3.0, n, step);
            if (n > 0) {
                sound(player, hips, PikaSounds.HIT, 1.0F, 0.9F);
            }
            if (step == 5) {
                wave(player);
            }
        });
    }

    /** The fifth cut's wave of light: forward 24 blocks, cutting whatever it passes. */
    static void wave(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 flat = new Vec3(look.f_82479_, look.f_82480_ * 0.5, look.f_82481_).m_82541_();
        Vec3 start = player.m_20182_().m_82520_(0, 1.0, 0).m_82549_(flat.m_82490_(1.2));
        Vec3 step = flat.m_82490_(2.4);
        int life = 10;
        fx(player, WAVE, 0, start, step, 0, 0, 0, 3.0, life);
        ServerLevel level = (ServerLevel) player.m_9236_();
        long born = now(player);
        List<LivingEntity> struck = new ArrayList<>();
        LIVE.add(new UoMoves.Live() {
            int age;

            @Override
            public boolean tick(long now) {
                this.age++;
                Vec3 at = start.m_82549_(step.m_82490_(this.age));
                for (LivingEntity body : foes(player, at, 2.6)) {
                    if (!struck.contains(body)) {
                        struck.add(body);
                        hit(player, body, 6.0F, flat.m_82490_(1.0).m_82520_(0, 0.3, 0), true);
                    }
                }
                return this.age >= life || now - born > life + 2;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ RMB: the laser, the beam, the guard

    static void use(ServerPlayer player, int flags) {
        State s = state(player);
        long now = now(player);
        if (sword(player)) {
            s.guard = true;
            s.guardSince = now;
            animate(player, "pika_sword_guard");
            return;
        }
        if (s.chargeSince >= 0 || s.volleySince >= 0 || s.kickChargeSince >= 0 || now < s.busyUntil) {
            return;
        }
        s.chargeSince = now;
        animate(player, "pika_laser_charge");
        fx(player, CHARGE_ON);
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.guard) {
            s.guard = false;
            animate(player, "pika_sword_idle");
            return;
        }
        if (s.chargeSince < 0) {
            return;
        }
        long held = now(player) - s.chargeSince;
        s.chargeSince = -1;
        fx(player, CHARGE_OFF);
        if (held < TAP) {
            if (ready(player, PikaKit.LASER)) {
                quickLaser(player);
            } else {
                animate(player, "pika_laser_end");
            }
        } else {
            beam(player, (float) Math.min(1.0, held / (double) BEAM_FULL));
        }
    }

    /** A finger pointed and a laser out of its tip: where it strikes, it blows up. */
    static void quickLaser(ServerPlayer player) {
        cool(player, PikaKit.LASER, 10);
        state(player).busyUntil = now(player) + 6;
        animate(player, "pika_laser");
        later(player, 2, () -> {
            Vec3 from = fingertip(player);
            LivingEntity target = aimed(player, LASER_RANGE, 0.02);
            Vec3 to = target != null ? StyleCombat.middle(target) : point(player, LASER_RANGE);
            fx(player, LASER, 0, from, to, 0, 0, 0, 0.22, 0);
            sound(player, from, PikaSounds.LASER, 1.0F, 1.0F);
            if (target != null) {
                hit(player, target, 4.0F, player.m_20154_().m_82490_(0.4).m_82520_(0, 0.25, 0), false);
            }
            UoMoves.explode(player, (ServerLevel) player.m_9236_(), to, player.m_20154_(), 1.8, 2.5F, 0, 0.6);
            fx(player, BURST, 0, to, Vec3.f_82478_, 0, 0, 0, 2.4, 0); // 2.9.0: a real explosion where it strikes
            sound(player, to, PikaSounds.EXPLODE, 1.0F, 1.3F);
        });
    }

    /** The light gathered at the fingertip let go: a beam burning through everything in its way, blowing up at its end. */
    static void beam(ServerPlayer player, float charge) {
        if (!ready(player, PikaKit.LASER)) {
            animate(player, "pika_laser_end");
            return;
        }
        cool(player, PikaKit.LASER, 40 + (int) (40 * charge));
        state(player).busyUntil = now(player) + 10;
        animate(player, "pika_laser_fire");
        Vec3 from = fingertip(player);
        Vec3 look = player.m_20154_();
        Vec3 to = point(player, BEAM_RANGE);
        double width = 0.7 + 1.3 * charge;
        fx(player, BEAM, 0, from, to, 0, 0, 0, width, 0);
        sound(player, from, PikaSounds.BEAM, 2.0F, 1.0F);
        double length = to.m_82554_(from);
        Vec3 way = to.m_82546_(from).m_82541_();
        for (LivingEntity body : player.m_9236_().m_6443_(LivingEntity.class, UoMoves.box(from, to).m_82400_(width + 1.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            Vec3 rel = StyleCombat.middle(body).m_82546_(from);
            double along = rel.m_82526_(way);
            if (along < 0 || along > length || rel.m_82546_(way.m_82490_(along)).m_82553_() > width + body.m_20205_() * 0.5) {
                continue;
            }
            hit(player, body, 6.0F + 8.0F * charge, look.m_82490_(1.2 + charge).m_82520_(0, 0.4, 0), true);
        }
        double radius = 3.0 + 4.0 * charge;
        UoMoves.explode(player, (ServerLevel) player.m_9236_(), to, look, radius, 5.0F + 7.0F * charge, 0, 1.4 + 1.6 * charge);
        Impact.quake((ServerLevel) player.m_9236_(), to, radius * 4.0, (float) Math.min(1.0, radius / 6.0), 12);
        sound(player, to, PikaSounds.EXPLODE, 2.2F, 0.9F);
    }

    // ------------------------------------------------------------------------------------------------------ Z: Yata no Kagami

    static int yataLeft(ServerPlayer player) {
        CompoundTag data = FruitData.get(player);
        return data.m_128441_(YATA_KEY) ? data.m_128451_(YATA_KEY) : YATA_MAX;
    }

    static void yata(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (now < s.busyUntil || s.volleySince >= 0 || yataLeft(player) <= 0) {
            return;
        }
        CompoundTag data = FruitData.get(player);
        data.m_128405_(YATA_KEY, yataLeft(player) - 1);
        if (!data.m_128441_(YATA_TIME) || data.m_128454_(YATA_TIME) <= now) {
            data.m_128356_(YATA_TIME, now + YATA_RECHARGE);
        }
        FruitServer.sync(player);
        s.busyUntil = now + 6;
        animate(player, "pika_yata");
        fx(player, YATA_FORM);
        sound(player, player.m_20182_(), PikaSounds.SPARKLE, 0.8F, 1.1F);
        later(player, 4, () -> {
            LivingEntity target = aimed(player, YATA_RANGE, 0.03);
            Vec3 from = player.m_20182_();
            Vec3 to;
            if (target != null) {
                to = beside(player, target, 1.4);
            } else {
                Vec3 p = point(player, YATA_RANGE);
                to = room(player, from, p.m_82546_(player.m_20154_().m_82490_(0.7)).m_82520_(0, -player.m_20192_() * 0.5, 0));
            }
            travel(player, to, target);
            fx(player, YATA_PATH, 0, from, to, 0, 0, 0, 0, 0);
            sound(player, to, PikaSounds.TRAVEL, 1.0F, 1.0F);
        });
    }

    /** Sneak + Z: stream along the look as light while it is held (up to 3 s), striking what you pass. */
    static void stream(ServerPlayer player) {
        State s = state(player);
        if (s.streamSince >= 0 || now(player) < s.busyUntil || !ready(player, PikaKit.YATA)) {
            return;
        }
        s.streamSince = now(player);
        animate(player, "pika_stream");
        fx(player, STREAM_ON);
        sound(player, player.m_20182_(), PikaSounds.TRAVEL, 1.0F, 1.1F);
    }

    static void stopStream(ServerPlayer player) {
        State s = state(player);
        if (s.streamSince < 0) {
            return;
        }
        s.streamSince = -1;
        cool(player, PikaKit.YATA, 40);
        fx(player, STREAM_OFF);
        player.m_20334_(player.m_20184_().f_82479_ * 0.2, 0.1, player.m_20184_().f_82481_ * 0.2);
        player.f_19864_ = true;
        animate(player, "pika_stream_end");
    }

    // ------------------------------------------------------------------------------------------------------ X: Yasakani no Magatama

    static void volley(ServerPlayer player, boolean focus) {
        State s = state(player);
        long now = now(player);
        if (s.volleySince >= 0 || now < s.busyUntil || !ready(player, PikaKit.YASAKANI)) {
            return;
        }
        s.volleySince = now;
        s.volleyFocus = focus;
        s.volleyBeads = 0;
        s.seed = ++next;
        animate(player, "pika_yasakani");
        fx(player, VOLLEY_ON);
        sound(player, player.m_20182_(), PikaSounds.SPARKLE, 1.0F, 0.9F);
        // up into the air, as over Marineford
        player.m_20334_(player.m_20184_().f_82479_ * 0.3, 0.75, player.m_20184_().f_82481_ * 0.3);
        player.f_19864_ = true;
    }

    static void stopVolley(ServerPlayer player) {
        State s = state(player);
        if (s.volleySince < 0) {
            return;
        }
        long held = now(player) - s.volleySince;
        s.volleySince = -1;
        cool(player, PikaKit.YASAKANI, 80 + (int) Math.min(VOLLEY_MAX, held) * 2);
        fx(player, VOLLEY_OFF);
        animate(player, "pika_yasakani_end");
    }

    /** A tick of the torrent: three beads from the crossed hands onto the ground where the player looks (all onto one when focused). */
    static void volleyTick(ServerPlayer player, State s, long now) {
        long held = now - s.volleySince;
        // held in the air while it pours
        Vec3 v = player.m_20184_();
        player.m_20334_(v.f_82479_ * 0.6, held < VOLLEY_WINDUP ? v.f_82480_ : Math.max(-0.02, v.f_82480_ * 0.3), v.f_82481_ * 0.6);
        player.f_19864_ = true;
        player.f_19789_ = 0.0F;
        if (held > VOLLEY_MAX) {
            stopVolley(player);
            return;
        }
        if (held < VOLLEY_WINDUP) {
            return;
        }
        Vec3 hands = player.m_146892_().m_82549_(player.m_20154_().m_82490_(0.7)).m_82520_(0, -0.35, 0);
        LivingEntity target = s.volleyFocus ? aimed(player, 50.0, 0.06) : null;
        Vec3 centre = target != null ? StyleCombat.middle(target) : point(player, 50.0);
        double spread = target != null ? 1.4 : VOLLEY_SPREAD;
        ServerLevel level = (ServerLevel) player.m_9236_();
        // 2.9.0 (the owner: "The X move is shooting countless projectiles. It's way too many"): one bullet every other tick (three a
        // tick before: 210 over the 3.5 s), some 25 in all
        if ((held - VOLLEY_WINDUP) % VOLLEY_EVERY != 0) {
            return;
        }
        for (int i = 0; i < 1; i++) {
            int k = s.volleyBeads++;
            double a = level.m_213780_().m_188500_() * Math.PI * 2.0;
            double r = spread * Math.sqrt(level.m_213780_().m_188500_());
            Vec3 land = centre.m_82520_(Math.cos(a) * r, 0, Math.sin(a) * r);
            Vec3 way = land.m_82546_(hands);
            double length = way.m_82553_();
            int flight = Math.max(2, (int) Math.ceil(length / 3.2));
            fx(player, BEAD, k, hands, land, 0, 0, 0, target != null ? 0.55 : 0.7, flight);
            sound(player, hands, PikaSounds.ZAP, 0.7F, 0.9F + level.m_213780_().m_188501_() * 0.25F);
            Vec3 step = way.m_82490_(1.0 / flight);
            LIVE.add(new UoMoves.Live() {
                int age;
                Vec3 at = hands;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    Vec3 to = this.at.m_82549_(step);
                    BlockHitResult wall = level.m_45547_(new ClipContext(this.at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    boolean end = this.age >= flight || wall.m_6662_() == HitResult.Type.BLOCK;
                    if (!end) {
                        for (LivingEntity body : foes(player, to, 0.9)) {
                            end = true;
                            break;
                        }
                    }
                    this.at = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : to;
                    if (end) {
                        UoMoves.explode(player, level, this.at, step.m_82541_(), target != null ? 1.6 : 2.2, target != null ? 1.2F : 2.0F, 0, 0.55);
                    }
                    return end;
                }

                @Override
                public ServerLevel level() {
                    return level;
                }
            });
        }
        if (held % 4 == 0) {
            sound(player, centre, PikaSounds.POP, 0.9F, 1.3F + level.m_213780_().m_188501_() * 0.3F);
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Ama no Murakumo

    static void murakumo(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (sword(player)) {
            s.swordUntil = 0;
            s.guard = false;
            fx(player, SWORD_OFF);
            sound(player, player.m_20182_(), PikaSounds.SWORD_OFF, 0.9F, 1.0F);
            animate(player, "pika_sword_away");
            FruitServer.sync(player);
            return;
        }
        if (now < s.busyUntil || !ready(player, PikaKit.MURAKUMO)) {
            return;
        }
        cool(player, PikaKit.MURAKUMO, 40);
        s.busyUntil = now + 8;
        animate(player, "pika_murakumo");
        sound(player, player.m_20182_(), PikaSounds.SPARKLE, 0.9F, 1.2F);
        later(player, 5, () -> {
            s.swordUntil = now(player) + SWORD_TICKS;
            fx(player, SWORD_ON, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0, SWORD_TICKS);
            sound(player, player.m_20182_(), PikaSounds.SWORD_ON, 1.0F, 1.0F);
            FruitServer.sync(player);
        });
    }

    static boolean guards(ServerPlayer player, DamageSource source) {
        State s = STATES.get(player.m_20148_());
        if (s == null || !s.guard || !sword(player) || source.m_7270_() == null) {
            return false;
        }
        Vec3 from = source.m_7270_().m_82546_(player.m_20182_());
        from = new Vec3(from.f_82479_, 0, from.f_82481_);
        Vec3 face = new Vec3(player.m_20154_().f_82479_, 0, player.m_20154_().f_82481_);
        if (from.m_82556_() < 1.0E-6 || face.m_82556_() < 1.0E-6 || from.m_82541_().m_82526_(face.m_82541_()) < Math.cos(Math.toRadians(70))) {
            return false;
        }
        Vec3 at = player.m_146892_().m_82549_(player.m_20154_().m_82490_(0.8));
        fx(player, KICK, 0, at, player.m_20154_(), 0, 0, 0, 3, 9);
        sound(player, at, PikaSounds.PARRY, 1.0F, 1.2F);
        return true;
    }

    // ------------------------------------------------------------------------------------------------------ V: the speed-of-light kick

    static void kickCharge(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (s.kickChargeSince >= 0 || now < s.busyUntil || s.volleySince >= 0 || !ready(player, PikaKit.LIGHT_KICK)) {
            return;
        }
        s.kickChargeSince = now;
        animate(player, "pika_light_kick_charge");
        fx(player, KICK_CHARGE_ON);
        sound(player, player.m_20182_(), PikaSounds.SPARKLE, 0.6F, 1.4F);
    }

    /**
     * "Have you ever been kicked at the speed of light?": on the one looked at as light (a flash where you were), the kick, and they go
     * across the field; with no one there, a beam out of the leg.
     */
    static void lightKick(ServerPlayer player) {
        State s = state(player);
        long held = now(player) - s.kickChargeSince;
        s.kickChargeSince = -1;
        fx(player, KICK_CHARGE_OFF);
        float charge = (float) Math.min(1.0, held / (double) KICK_FULL);
        cool(player, PikaKit.LIGHT_KICK, 100 + (int) (60 * charge));
        s.busyUntil = now(player) + 10;
        LivingEntity target = aimed(player, KICK_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        animate(player, "pika_light_kick");
        if (target != null) {
            // 2.9.0 (the owner: "kick from the side, like comes from the back and then rotates and it hits on the left side of the mob
            // ... around the head area"): gone as light and out of it behind them, over their left shoulder at the height of their
            // head, turned away; he spins round (the animation) and the kick lands in the left side of the head on tick 3, throwing them
            // away to their right
            float yaw = target.f_20883_;
            Vec3 face = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
            Vec3 left = new Vec3(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw)));
            double reach = target.m_20205_() * 0.5 + 0.85;
            double lift = Math.max(0.0, target.m_20192_() - LIGHT_KICK_FOOT);
            Vec3 t = target.m_20182_();
            Vec3 to = room(player, t, t.m_82549_(face.m_82490_(-reach * 0.55)).m_82549_(left.m_82490_(reach)).m_82520_(0, lift, 0));
            travel(player, to, target);
            Vec3 thrown = left.m_82490_(-(2.8 + 2.6 * charge)).m_82549_(face.m_82490_(0.6)).m_82520_(0, 0.6 + 0.35 * charge, 0);
            fx(player, LIGHT_KICK, target.m_19879_(), from, to, thrown.f_82479_, thrown.f_82480_, thrown.f_82481_, charge, 0);
            sound(player, from, PikaSounds.ZIP, 1.2F, 1.0F);
            sound(player, to, PikaSounds.TRAVEL, 1.2F, 1.1F);
            hold(player, to, LIGHT_KICK_HOLD);
            later(player, LIGHT_KICK_LANDS, () -> {
                if (!target.m_6084_()) {
                    return;
                }
                hit(player, target, 7.0F + 9.0F * charge, thrown, true);
                Vec3 head = new Vec3(target.m_20185_(), target.m_20186_() + target.m_20192_(), target.m_20189_()).m_82549_(left.m_82490_(target.m_20205_() * 0.5));
                fx(player, KICK, 0, head, left.m_82490_(-1.0), 0, 0, 0, 3, 8);
                Impact.quake((ServerLevel) player.m_9236_(), head, 20.0 + 20.0 * charge, 0.5F + 0.5F * charge, 14);
                sound(player, head, PikaSounds.KICK_BLAST, 1.8F, 0.9F);
                sound(player, head, PikaSounds.HIT_HEAVY, 1.6F, 0.85F);
            });        } else {
            // no one there: the kick fires a beam out of the leg (as at Zoro)
            later(player, 2, () -> {
                Vec3 foot = player.m_20182_().m_82520_(0, 0.9, 0).m_82549_(player.m_20154_().m_82490_(0.8));
                Vec3 to = point(player, 40.0);
                fx(player, BEAM, 1, foot, to, 0, 0, 0, 0.8 + 0.8 * charge, 0);
                sound(player, foot, PikaSounds.BEAM, 1.6F, 1.1F);
                UoMoves.explode(player, (ServerLevel) player.m_9236_(), to, player.m_20154_(), 3.0 + 3.0 * charge, 5.0F + 6.0F * charge, 0,
                    1.4 + 1.4 * charge);
                Impact.quake((ServerLevel) player.m_9236_(), to, 16.0, 0.5F, 10);
            });
        }
    }

    /** 2.9.0: the light kick: how high over his feet the kicking foot goes (blocks), how long he hangs there, the tick it lands. */
    static final double LIGHT_KICK_FOOT = 1.45;
    static final int LIGHT_KICK_HOLD = 8;
    static final int LIGHT_KICK_LANDS = 3;

    /** 2.9.0: held where he is (in the air if he is) for {@code ticks}, no fall taken: a kick in mid-air hangs until it lands. */
    static void hold(ServerPlayer player, Vec3 at, int ticks) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        long until = now(player) + ticks;
        LIVE.add(new UoMoves.Live() {
            @Override
            public boolean tick(long now) {
                if (!player.m_6084_() || player.m_9236_() != level) {
                    return true;
                }
                player.m_20334_(0.0, 0.0, 0.0);
                player.f_19864_ = true;
                player.f_19789_ = 0.0F;
                if (player.m_20182_().m_82557_(at) > 0.04) {
                    player.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
                }
                return now >= until;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ B: the clones

    /** As at Egghead: light thrown out of the body, every bead of it a Borsalino of light running at someone with a sword, cutting, bursting. */
    static void clones(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (now < s.busyUntil || s.volleySince >= 0 || !ready(player, PikaKit.CLONES)) {
            return;
        }
        cool(player, PikaKit.CLONES, 300);
        s.busyUntil = now + 10;
        animate(player, "pika_clones");
        sound(player, player.m_20182_(), PikaSounds.SPARKLE, 1.2F, 1.0F);
        List<LivingEntity> near = foes(player, player.m_20182_().m_82549_(player.m_20154_().m_82490_(8.0)), 22.0);
        Vec3 me = player.m_20182_();
        near.sort((a, b) -> Double.compare(a.m_20182_().m_82557_(me), b.m_20182_().m_82557_(me)));
        Vec3 chest = player.m_20182_().m_82520_(0, 1.2, 0);
        ServerLevel level = (ServerLevel) player.m_9236_();
        for (int i = 0; i < CLONE_COUNT; i++) {
            LivingEntity target = near.isEmpty() ? null : near.get(i % near.size());
            double a = Math.toRadians(-player.m_146908_()) + (i - (CLONE_COUNT - 1) / 2.0) * 0.5;
            Vec3 spot = target != null ? target.m_20182_()
                : room(player, player.m_20182_(), player.m_20182_().m_82520_(Math.sin(a) * 9.0, 0, Math.cos(a) * 9.0));
            fx(player, CLONE, target != null ? target.m_19879_() : -1, chest, spot, 0, 0, 0, 0, i);
            if (target != null) {
                int index = i;
                for (int cut = 0; cut < 2; cut++) {
                    LIVE.add(new UoMoves.Later(level, now + 10 + cut * 6 + index % 3, () -> {
                        if (target.m_6084_() && target.m_20182_().m_82557_(player.m_20182_()) < 40 * 40) {
                            sound(player, target.m_20182_(), PikaSounds.SWING, 0.8F, 1.0F + 0.05F * (index % 4));
                            hit(player, target, 2.5F, target.m_20182_().m_82546_(player.m_20182_()).m_82541_().m_82490_(0.4).m_82520_(0, 0.2, 0), false);
                        }
                    }));
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the body of light

    static void dodged(ServerPlayer player, DamageSource source) {
        Vec3 at = source.m_7270_() == null ? FruitUtil.chest(player)
            : FruitUtil.chest(player).m_82549_(FruitUtil.flat(source.m_7270_().m_82546_(player.m_20182_())).m_82490_(0.3));
        fx(player, DODGE, 0, at, Vec3.f_82478_, 0, 0, 0, 0, 0);
        sound(player, at, PikaSounds.SPARKLE, 0.5F, 1.5F);
    }

    /** What the client needs to pose and draw the player. */
    static void view(ServerPlayer player, CompoundTag tag) {
        State s = STATES.get(player.m_20148_());
        tag.m_128379_("sword", s != null && s.swordUntil > now(player));
        tag.m_128405_("yata", yataLeft(player));
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        long now = now(player);
        // Yata no Kagami comes back one use at a time
        CompoundTag data = FruitData.get(player);
        if (data.m_128441_(YATA_TIME) && now >= data.m_128454_(YATA_TIME) && yataLeft(player) < YATA_MAX) {
            data.m_128405_(YATA_KEY, yataLeft(player) + 1);
            if (yataLeft(player) < YATA_MAX) {
                data.m_128356_(YATA_TIME, now + YATA_RECHARGE);
            } else {
                data.m_128473_(YATA_TIME);
            }
            FruitServer.sync(player);
        }
        player.f_19789_ = 0.0F; // light takes no fall
        PikaKit.flight(player, FruitData.active(player) && !FruitServer.inSea(player));
        if (s == null) {
            return;
        }
        if (s.volleySince >= 0) {
            volleyTick(player, s, now);
        }
        if (s.kickChargeSince >= 0 && now - s.kickChargeSince > KICK_FULL + 20) {
            lightKick(player);
        }
        if (s.chargeSince >= 0 && now - s.chargeSince > BEAM_FULL + 40) {
            useUp(player);
        }
        if (s.streamSince >= 0) {
            long held = now - s.streamSince;
            if (held > STREAM_MAX) {
                stopStream(player);
            } else {
                Vec3 look = player.m_20154_();
                player.m_20334_(look.f_82479_ * 2.2, look.f_82480_ * 2.2, look.f_82481_ * 2.2);
                player.f_19864_ = true;
                if (held % 6 == 0) {
                    for (LivingEntity body : foes(player, player.m_20182_().m_82520_(0, 1.0, 0), 2.2)) {
                        hit(player, body, 3.0F, look.m_82490_(1.0).m_82520_(0, 0.4, 0), false);
                    }
                }
            }
        }
        if (s.swordUntil > 0 && now >= s.swordUntil) {
            s.swordUntil = 0;
            s.guard = false;
            fx(player, SWORD_OFF);
            FruitServer.sync(player);
        }
    }

    /** The fruit set aside or lost: nothing of it keeps going. */
    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.volleySince >= 0) {
            fx(player, VOLLEY_OFF);
        }
        if (s.chargeSince >= 0) {
            fx(player, CHARGE_OFF);
        }
        if (s.kickChargeSince >= 0) {
            fx(player, KICK_CHARGE_OFF);
        }
        if (s.streamSince >= 0) {
            fx(player, STREAM_OFF);
        }
        if (s.swordUntil > now(player)) {
            fx(player, SWORD_OFF);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
    }

    @SubscribeEvent
    public static void pikaTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || LIVE.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        List<UoMoves.Live> due = new ArrayList<>();
        for (UoMoves.Live live : LIVE) {
            if (live.level() == level) {
                due.add(live);
            }
        }
        for (UoMoves.Live live : due) {
            boolean done;
            try {
                done = live.tick(now);
            } catch (RuntimeException failure) {
                done = true;
            }
            if (done) {
                LIVE.remove(live);
            }
        }
    }

}
