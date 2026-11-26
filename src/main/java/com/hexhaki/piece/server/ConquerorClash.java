package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.data.HakiType;
import com.hexhaki.gameplay.ConquerorLightningPath;
import com.hexhaki.piece.haki.ConquerorClashRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CConquerorClash;
import com.hexhaki.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.2: the Conqueror's Haki clash ({@link ConquerorClashRules}: the owner's words). Two players: one lets out a release held 2 seconds
 * or more ({@link ConquerorRelease#visual}) while the other has been charging 2 seconds or more, each within the other's release
 * radius. Neither release goes off as a release: they collide. Both are held where they stand, facing each other; the impact and
 * its ring at the meeting point, the pressure wave, both colours' lightning flying out (the clients, {@link S2CConquerorClash}); then
 * the tug of "TAP M1" (each one's taps, {@link #tap}; the stronger Haki presses on its own), the meeting point sliding toward the one
 * losing. All the while a wind blows out of the bubble round them: any body (not the two) that comes inside it or near its skin is
 * blasted out level through everything in its way and falls ragdolled ({@link Blast}). The bar at a side (or the time up): the winner's
 * blow sends the loser flying level through everything, ragdolled, stunned for double the winner's longest Conqueror's stun.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class ConquerorClash {
    /** One clash. A let the release out; B was charging. */
    static final class Clash {
        final int id;
        final ServerLevel level;
        final ServerPlayer a;
        final ServerPlayer b;
        final Vec3 atA;
        final Vec3 atB;
        final Vec3 middle;
        final double radius;
        final long start;
        final int masteryA;
        final int masteryB;
        final int colorA;
        final int colorB;
        double v;
        int tapsA;
        int tapsB;
        long lastTapA = Long.MIN_VALUE;
        long lastTapB = Long.MIN_VALUE;
        /** 1 A won, -1 B won, 0 still on. */
        int won;
        long wonAt;

        Clash(int id, ServerLevel level, ServerPlayer a, ServerPlayer b) {
            this.id = id;
            this.level = level;
            this.a = a;
            this.b = b;
            this.atA = a.m_20182_();
            this.atB = b.m_20182_();
            this.middle = this.atA.m_82549_(this.atB).m_82490_(0.5);
            this.radius = ConquerorClashRules.bubble(this.atA.m_82554_(this.atB));
            this.start = level.m_46467_();
            this.masteryA = HakiData.mastery(a, HakiType.CONQUEROR);
            this.masteryB = HakiData.mastery(b, HakiType.CONQUEROR);
            this.colorA = RyouServer.color(a);
            this.colorB = RyouServer.color(b);
        }

        long age() {
            return this.level.m_46467_() - this.start;
        }

        boolean has(Entity e) {
            return e == this.a || e == this.b;
        }

        S2CConquerorClash message(int phase) {
            return new S2CConquerorClash(this.id, phase, this.a.m_19879_(), this.b.m_19879_(), this.atA.f_82479_, this.atA.f_82480_,
                this.atA.f_82481_, this.atB.f_82479_, this.atB.f_82480_, this.atB.f_82481_, (float) this.radius, (float) this.v,
                this.colorA, this.colorB, this.masteryA, this.masteryB, (int) this.age());
        }
    }

    /** A body blasted out (the bubble's wind, or the loser thrown): flown level through everything, then let down where there is room. */
    static final class Blast {
        final Entity body;
        final ServerLevel level;
        final double dx;
        final double dz;
        final double y;
        int left;

        Blast(Entity body, ServerLevel level, double dx, double dz, int ticks) {
            this.body = body;
            this.level = level;
            this.dx = dx;
            this.dz = dz;
            this.y = body.m_20186_();
            this.left = ticks;
        }
    }

    static final Map<Integer, Clash> CLASHES = new HashMap<>();
    static final Map<UUID, Clash> BY_PLAYER = new HashMap<>();
    static final Map<Integer, Blast> BLASTS = new HashMap<>();
    /** When each charging player began to charge (game tick). */
    static final Map<UUID, Long> CHARGING_SINCE = new HashMap<>();
    static int next = 1;

    private ConquerorClash() {
    }

    /** Each server tick for a player (ConquerorRelease.charging): when their charge began is kept. */
    static void charging(ServerPlayer player, boolean charging) {
        UUID id = player.m_20148_();
        if (!charging) {
            CHARGING_SINCE.remove(id);
        } else if (!CHARGING_SINCE.containsKey(id)) {
            CHARGING_SINCE.put(id, player.m_9236_().m_46467_());
        }
    }

    /** How long {@code player} has been charging G (ticks; 0 when not). */
    static long held(ServerPlayer player) {
        Long since = CHARGING_SINCE.get(player.m_20148_());
        return since == null ? 0L : Math.max(0L, player.m_9236_().m_46467_() - since);
    }

    /** True while {@code player} is in a clash: their release is swallowed by it, they are held where they stand. */
    public static boolean busy(Entity player) {
        return player != null && BY_PLAYER.containsKey(player.m_20148_());
    }

    /**
     * {@code player} lets their radial release out ({@code power} 0..1), G held {@code releaseHeld} ticks: a clash with another player
     * charging near, if there is one ({@link ConquerorClashRules#clashes}); true if it began (the release is then swallowed by it).
     */
    static boolean begin(ServerPlayer player, float power, long releaseHeld) {
        if (!(player.m_9236_() instanceof ServerLevel level) || busy(player) || releaseHeld < ConquerorClashRules.HOLD_LEAST) {
            return false;
        }
        double radius = ConquerorLightningPath.radius(HakiData.mastery(player, HakiType.CONQUEROR), power);
        ServerPlayer best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ServerPlayer other : level.m_6907_()) {
            if (other == player || !other.m_6084_() || other.m_5833_() || busy(other) || !HakiData.flag(other, "conquerorCharging")) {
                continue;
            }
            double distance = other.m_20182_().m_82554_(player.m_20182_());
            float charge = Math.max(0.04F, Math.min(1.0F, HakiData.integer(other, "conquerorCharge") / 100.0F));
            double theirs = ConquerorLightningPath.radius(HakiData.mastery(other, HakiType.CONQUEROR), charge);
            if (ConquerorClashRules.clashes(releaseHeld, held(other), distance, radius, theirs) && distance < bestDistance) {
                best = other;
                bestDistance = distance;
            }
        }
        if (best == null) {
            return false;
        }
        // the other's charge goes into the clash: it is let go of without a release of its own
        try {
            HakiData.flag(best, "conquerorCharging", false);
            HakiData.integer(best, "conquerorCharge", 0);
            HakiData.integer(best, "conquerorChargeTicks", 0);
        } catch (RuntimeException failure) {
            // the charge is the original's; the clash goes on
        }
        CHARGING_SINCE.remove(best.m_20148_());
        Clash clash = new Clash(next++, level, player, best);
        CLASHES.put(clash.id, clash);
        BY_PLAYER.put(player.m_20148_(), clash);
        BY_PLAYER.put(best.m_20148_(), clash);
        face(clash.a, clash.atA, clash.atB);
        face(clash.b, clash.atB, clash.atA);
        ConquerorRelease.animate(clash.a, "conqueror_clash", 4);
        ConquerorRelease.animate(clash.b, "conqueror_clash", 4);
        Vec3 meet = meeting(clash);
        level.m_6263_(null, meet.f_82479_, meet.f_82480_, meet.f_82481_, ModSounds.CONQUEROR_SUPREME_BLAST.get(), SoundSource.PLAYERS, 4.0F, 0.8F);
        com.hexhaki.piece.PieceSounds.playAt(clash.a, meet.f_82479_, meet.f_82480_, meet.f_82481_, com.hexhaki.piece.clash.ClashRules.SOUND, 3.0F, 0.7F);
        com.hexhaki.piece.PieceSounds.playAt(clash.a, meet.f_82479_, meet.f_82480_, meet.f_82481_, "piece.gura.quake", 2.5F, 0.8F);
        send(clash, S2CConquerorClash.START);
        try {
            com.hexhaki.fx.Impact.quake(level, meet, clash.radius * 2.5, 1.0F, 30);
        } catch (RuntimeException failure) {
            // the shake is the original's
        }
        return true;
    }

    /** Turns {@code player} at {@code at} to face {@code toward}. */
    static void face(ServerPlayer player, Vec3 at, Vec3 toward) {
        double dx = toward.f_82479_ - at.f_82479_;
        double dz = toward.f_82481_ - at.f_82481_;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        player.m_8999_(player.m_284548_(), at.f_82479_, at.f_82480_, at.f_82481_, yaw, 0.0F);
    }

    /** Where the two Haki meet now (at the chest's height). */
    static Vec3 meeting(Clash clash) {
        double t = ConquerorClashRules.meeting(clash.v);
        return clash.atA.m_82549_(clash.atB.m_82546_(clash.atA).m_82490_(t)).m_82520_(0.0, 1.3, 0.0);
    }

    /** M1 tapped by {@code player} (C2SConquerorTap). */
    public static void tap(ServerPlayer player) {
        Clash clash = BY_PLAYER.get(player.m_20148_());
        if (clash == null || clash.won != 0 || clash.age() < ConquerorClashRules.INTRO) {
            return;
        }
        long now = clash.level.m_46467_();
        if (player == clash.a && now - clash.lastTapA >= ConquerorClashRules.TAP_GAP) {
            clash.lastTapA = now;
            clash.tapsA++;
        } else if (player == clash.b && now - clash.lastTapB >= ConquerorClashRules.TAP_GAP) {
            clash.lastTapB = now;
            clash.tapsB++;
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Iterator<Clash> it = CLASHES.values().iterator();
        while (it.hasNext()) {
            Clash clash = it.next();
            boolean done;
            try {
                done = step(clash);
            } catch (RuntimeException failure) {
                done = true; // a clash that fails ends, never the server
            }
            if (done) {
                it.remove();
                BY_PLAYER.remove(clash.a.m_20148_(), clash);
                BY_PLAYER.remove(clash.b.m_20148_(), clash);
            }
        }
        Iterator<Blast> blasts = BLASTS.values().iterator();
        while (blasts.hasNext()) {
            Blast blast = blasts.next();
            try {
                if (fly(blast)) {
                    blasts.remove();
                }
            } catch (RuntimeException failure) {
                blasts.remove();
            }
        }
    }

    /** One tick of a clash; true when it is over. */
    static boolean step(Clash clash) {
        boolean aHere = here(clash, clash.a);
        boolean bHere = here(clash, clash.b);
        if (!aHere || !bHere) {
            send(clash, S2CConquerorClash.GONE);
            return true;
        }
        long age = clash.age();
        if (clash.won == 0) {
            hold(clash.a, clash.atA);
            hold(clash.b, clash.atB);
            if (age >= ConquerorClashRules.INTRO) {
                clash.v = ConquerorClashRules.step(clash.v, clash.tapsA, clash.tapsB, clash.masteryA, clash.masteryB);
                clash.tapsA = 0;
                clash.tapsB = 0;
                boolean over = age >= ConquerorClashRules.INTRO + ConquerorClashRules.GAME_MOST;
                clash.won = ConquerorClashRules.winner(clash.v, over, clash.masteryA, clash.masteryB);
                if (clash.won != 0) {
                    clash.wonAt = age;
                    clash.v = clash.won;
                    ServerPlayer winner = clash.won > 0 ? clash.a : clash.b;
                    ConquerorRelease.animate(winner, "supreme_conqueror_release", 2);
                    send(clash, S2CConquerorClash.WON);
                } else if (age % 2 == 0) {
                    send(clash, S2CConquerorClash.TUG);
                }
            }
            if (age % 24 == 0) {
                Vec3 meet = meeting(clash);
                com.hexhaki.piece.PieceSounds.playAt(clash.a, meet.f_82479_, meet.f_82480_, meet.f_82481_, "piece.uo.gust", 1.6F, 0.75F);
                com.hexhaki.piece.PieceSounds.playAt(clash.a, meet.f_82479_, meet.f_82480_, meet.f_82481_, "piece.gura.rumble", 1.2F, 0.9F);
            }
            wind(clash);
            return false;
        }
        // the winner's blow, then the loser flies
        ServerPlayer winner = clash.won > 0 ? clash.a : clash.b;
        ServerPlayer loser = clash.won > 0 ? clash.b : clash.a;
        hold(winner, clash.won > 0 ? clash.atA : clash.atB);
        if (age - clash.wonAt < ConquerorClashRules.FINISH) {
            hold(loser, clash.won > 0 ? clash.atB : clash.atA);
            wind(clash);
            return false;
        }
        Vec3 meet = loser.m_20182_().m_82520_(0.0, 1.2, 0.0);
        clash.level.m_6263_(null, meet.f_82479_, meet.f_82480_, meet.f_82481_, ModSounds.CONQUEROR_SUPREME_BLAST.get(), SoundSource.PLAYERS, 4.0F,
            0.95F);
        com.hexhaki.piece.PieceSounds.playAt(winner, meet.f_82479_, meet.f_82480_, meet.f_82481_, "piece.gura.boom", 3.0F, 0.85F);
        double dx = loser.m_20185_() - winner.m_20185_();
        double dz = loser.m_20189_() - winner.m_20189_();
        double l = Math.max(1.0E-6, Math.hypot(dx, dz));
        int stun = ConquerorClashRules.loserStun(ProfileServer.of(winner).alignment());
        BY_PLAYER.remove(loser.m_20148_(), clash);
        launch(clash.level, loser, dx / l * ConquerorClashRules.THROW_SPEED, dz / l * ConquerorClashRules.THROW_SPEED,
            ConquerorClashRules.THROW_TICKS, stun);
        ConquerorRelease.hold(clash.level, loser, stun);
        send(clash, S2CConquerorClash.END);
        return true;
    }

    /** {@code player} still in the clash's world, alive, here. */
    static boolean here(Clash clash, ServerPlayer player) {
        return player.m_6084_() && !player.m_213877_() && player.m_9236_() == clash.level && !player.m_5833_();
    }

    /** {@code player} held where they stood (back if they drift off it), unmoving. */
    static void hold(ServerPlayer player, Vec3 at) {
        if (player.m_20182_().m_82557_(at) > 0.09) {
            player.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
        }
        player.m_20256_(Vec3.f_82478_);
        player.f_19864_ = true;
    }

    /** The bubble's wind: every body but the two that is in it or near its skin is blasted out. */
    static void wind(Clash clash) {
        double reach = clash.radius + ConquerorClashRules.SKIN;
        AABB box = new AABB(clash.middle.f_82479_ - reach, clash.middle.f_82480_ - reach, clash.middle.f_82481_ - reach,
            clash.middle.f_82479_ + reach, clash.middle.f_82480_ + reach, clash.middle.f_82481_ + reach);
        List<Entity> caught = new ArrayList<>();
        for (Entity e : clash.level.m_45933_(null, box)) {
            if (clash.has(e) || BLASTS.containsKey(e.m_19879_()) || e.m_5833_() || !e.m_6084_() || e.m_20159_() || busy(e)) {
                continue;
            }
            if (ConquerorClashRules.caught(e.m_20182_().m_82554_(clash.middle), clash.radius)) {
                caught.add(e);
            }
        }
        for (Entity e : caught) {
            double dx = e.m_20185_() - clash.middle.f_82479_;
            double dz = e.m_20189_() - clash.middle.f_82481_;
            double l = Math.hypot(dx, dz);
            if (l < 1.0E-3) {
                double a = e.m_19879_() * 2.399963;
                dx = Math.cos(a);
                dz = Math.sin(a);
                l = 1.0;
            }
            launch(clash.level, e, dx / l * ConquerorClashRules.BLAST_SPEED, dz / l * ConquerorClashRules.BLAST_SPEED,
                ConquerorClashRules.BLAST_TICKS, ConquerorClashRules.BLAST_DOWN);
            com.hexhaki.piece.PieceSounds.playAt(clash.a, e.m_20185_(), e.m_20186_() + 1.0, e.m_20189_(), "piece.uo.whoosh", 1.2F, 0.8F);
        }
    }

    /** {@code body} sent flying level at ({@code dx}, {@code dz}) a tick for {@code ticks}, through everything; a body that lives falls ragdolled for {@code down}. */
    static void launch(ServerLevel level, Entity body, double dx, double dz, int ticks, int down) {
        BLASTS.put(body.m_19879_(), new Blast(body, level, dx, dz, ticks));
        body.m_20256_(new Vec3(dx, 0.0, dz));
        body.f_19864_ = true;
        if (body instanceof LivingEntity living) {
            HakiFalls.knockDown(level, living, down, false);
        }
    }

    /** One tick of a blasted body's flight; true when it is down. */
    static boolean fly(Blast blast) {
        Entity body = blast.body;
        if (body.m_213877_() || !body.m_6084_() || body.m_9236_() != blast.level) {
            return true;
        }
        double x = body.m_20185_() + blast.dx;
        double z = body.m_20189_() + blast.dz;
        body.m_6021_(x, blast.y, z);
        body.m_20256_(new Vec3(blast.dx, 0.0, blast.dz));
        body.f_19864_ = true;
        if (--blast.left > 0) {
            return false;
        }
        // let down where there is room: straight up out of whatever it flew into
        for (int up = 0; up < 48 && !blast.level.m_45786_(body); up++) {
            body.m_6021_(body.m_20185_(), Math.floor(body.m_20186_()) + 1.0, body.m_20189_());
        }
        body.m_20256_(new Vec3(blast.dx * 0.15, 0.0, blast.dz * 0.15));
        body.f_19864_ = true;
        return true;
    }

    /** A body flying through everything is not hurt by the walls it passes (nor anything else while it flies). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void attacked(LivingAttackEvent event) {
        LivingEntity body = event.getEntity();
        if (body != null && !body.m_9236_().f_46443_ && BLASTS.containsKey(body.m_19879_())) {
            event.setCanceled(true);
        }
    }

    static void send(Clash clash, int phase) {
        S2CConquerorClash message = clash.message(phase);
        PieceNetwork.around(clash.a, message);
        if (clash.b.m_9236_() == clash.level && clash.a.m_20280_(clash.b) > 64.0 * 64.0) {
            PieceNetwork.around(clash.b, message);
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CHARGING_SINCE.remove(event.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        CLASHES.clear();
        BY_PLAYER.clear();
        BLASTS.clear();
        CHARGING_SINCE.clear();
    }
}
