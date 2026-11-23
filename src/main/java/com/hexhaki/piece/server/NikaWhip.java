package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearAbility;
import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.gear.GearSounds;
import com.hexhaki.piece.gear.NikaDawn;
import com.hexhaki.piece.gear.NikaGoof;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.4: the Nika fruit's C in Gear 5 (the owner: "Whip and Dawn Whip Rework"; then "the Goof ball is only usable in transformed state").
 * patch/PiecePatch has the original's {@code GearServerController.whip} ask {@link #unlocked} first: untransformed, C is the original's
 * Gum-Gum Whip as before; in Gear 5 the original stops there and this runs:
 * <ul>
 *   <li><b>Goof Ball</b> (no Haki; held: client/gear/NikaKeys sends the let go, network/C2SNikaWhip): a cartoon spin-up
 *       (gear5_goof_spin), then the body is a spinning ball of smoke and its own limbs punching out every way (every client draws it:
 *       client/gear/NikaWhipClient, gear/NikaGoof); everyone within {@link #GOOF_REACH} punched every {@link #GOOF_EVERY} ticks and
 *       knocked about; let go (at least {@link NikaGoof#LEAST} ticks of ball) or after {@link NikaGoof#MOST} the ball pops and the user
 *       is dizzy for {@link NikaGoof#DIZZY} ticks (slowed, no moves, stars over the head, gear5_goof_dizzy);</li>
 *   <li><b>Dawn Whip</b> (with Haki: the original's {@code dawn}, Gear 5 with Armament or Ryou), the owner's six phases
 *       (gear/NikaDawn's timeline, gear5_dawn_whip): rooted and wound up like a spring; the right leg stretched out as the original's
 *       rubber leg (its S2CGearStretch, Haki-coated) while the body unwinds; at {@link NikaDawn#IMPACT} everyone round within the leg's
 *       reach struck and launched sideways (the way the leg sweeps) through blocks up to obsidian, the Conqueror's lightning at each
 *       strike; the overspin, drifting out of control; drilling into the ground (a self-repairing shaft {@link NikaDawn#DEPTH} deep,
 *       the world's mobGriefing rule obeyed); popped back out with a rubber bounce (gear5_dawn_pop).</li>
 * </ul>
 * While either runs (and while dizzy) the fruit's other moves are refused (server/NikaRules.allowed asks {@link #busy}); no fall damage
 * through Dawn Whip. The C key's own cooldown is kept here ({@link #REST}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NikaWhip {
    /** S2CUoFx kinds (the Nika fruit's 160 to 179; client/dragon/UoFx routes 165 to 172 to client/gear/NikaWhipClient). */
    public static final int GOOF = 165;
    public static final int GOOF_HIT = 166;
    public static final int GOOF_POP = 167;
    public static final int DAWN = 168;
    public static final int DAWN_HIT = 169;
    public static final int DAWN_DRILL = 170;
    public static final int DAWN_POP = 171;
    public static final int STOP = 172;

    /** Goof Ball's punches: how near, how often, how hard, how far they knock (and at most this many bodies a punch). */
    static final double GOOF_REACH = 2.6;
    static final int GOOF_EVERY = 3;
    static final float GOOF_DAMAGE = 2.5F;
    static final double GOOF_KNOCK = 0.55;
    static final int GOOF_MOST = 6;
    /** Dawn Whip's blow and the launch it gives (blocks a tick; the hardest block it tears through: obsidian, as Dawn Pistol's). */
    static final float DAWN_DAMAGE = 16.0F;
    static final double LAUNCH_SPEED = 3.4;
    static final int LAUNCH_STRAIGHT = 6;
    static final double CRATER = 1.8;
    /** The overspin's drift (blocks a tick) and the pop's lift (a tick, up: out of a three-block shaft and two more). */
    static final double DRIFT = 0.16;
    static final double POP_LIFT = 1.05;
    /** How long the shaft stays open before it grows back (ticks: well after the user is out). */
    static final long SHAFT_BACK = 300;
    /** C's rest after Goof Ball's daze and after Dawn Whip's landing. */
    static final int GOOF_REST = 40;
    static final int DAWN_REST = 60;

    static final class Goof {
        long start;
        long letGo = -1;
        long lastSound;
        long lastLightning;
        int seed;
    }

    static final class Dawn {
        long start;
        double wx;
        double wz;
        int seed;
        BlockPos mouth;
        boolean torn;
        int dug;
        double heading;
        long lastLightning;
    }

    static final Map<UUID, Goof> GOOFS = new HashMap<>();
    static final Map<UUID, Dawn> DAWNS = new HashMap<>();
    /** The dazed after Goof Ball: until when. */
    static final Map<UUID, Long> DIZZY = new HashMap<>();
    static final Map<UUID, Long> REST = new HashMap<>();

    private NikaWhip() {
    }

    /**
     * In the original's {@code GearServerController.whip}, in place of {@code GearData.unlocked(player, WHIP)}: untransformed (or any
     * other ability) the original's answer; in Gear 5 Goof Ball or Dawn Whip starts here and the original's whip goes no further.
     */
    public static boolean unlocked(Player player, GearAbility ability) {
        if (!GearData.unlocked(player, ability)) {
            return false;
        }
        if (ability != GearAbility.WHIP || !(player instanceof ServerPlayer server) || !GearData.flag(server, "gear5")) {
            return true;
        }
        try {
            start(server);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Goof Ball / Dawn Whip could not start", failure);
        }
        return false;
    }

    /** Whether one of these runs for {@code player} (or it is dazed after Goof Ball): its other moves wait. */
    public static boolean busy(Player player) {
        if (GOOFS.isEmpty() && DAWNS.isEmpty() && DIZZY.isEmpty()) {
            return false;
        }
        UUID id = player.m_20148_();
        return GOOFS.containsKey(id) || DAWNS.containsKey(id) || DIZZY.containsKey(id);
    }

    static void start(ServerPlayer player) {
        UUID id = player.m_20148_();
        long now = player.m_9236_().m_46467_();
        if (busy(player) || GearData.integer(player, "transformTick") > 0 || player.m_20159_()) {
            return;
        }
        Long rest = REST.get(id);
        if (rest != null && now < rest && !HakiServerController.noCooldowns(player)) {
            return;
        }
        int seed = (int) (player.m_217043_().m_188505_() & 0x7FFFFFFF);
        if (com.hexhaki.devilfruit.gameplay.GearServerController.dawn(player)) {
            Dawn dawn = new Dawn();
            dawn.start = now;
            Vec3 look = player.m_20154_();
            double l = Math.sqrt(look.f_82479_ * look.f_82479_ + look.f_82481_ * look.f_82481_);
            dawn.wx = l < 1.0E-4 ? 0.0 : look.f_82479_ / l;
            dawn.wz = l < 1.0E-4 ? 1.0 : look.f_82481_ / l;
            dawn.seed = seed;
            dawn.heading = Math.atan2(dawn.wz, dawn.wx);
            DAWNS.put(id, dawn);
            player.m_5661_(Component.m_237113_("§6Dawn Whip"), true);
            HakiServerController.animate(player, "gear5_dawn_whip", 2);
            PieceNetwork.around(player, new S2CUoFx(DAWN, 0, player.m_19879_(), player.m_20185_(), player.m_20186_(), player.m_20189_(),
                (float) dawn.wx, 0, (float) dawn.wz, 0, 0, 0, (float) NikaDawn.REACH, seed));
            com.hexhaki.piece.PieceSounds.playAt(player, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), GearSounds.WIND, 1.0F,
                1.0F);
        } else {
            Goof goof = new Goof();
            goof.start = now;
            goof.seed = seed;
            GOOFS.put(id, goof);
            HakiServerController.animate(player, "gear5_goof_spin", 2);
            PieceNetwork.around(player, new S2CUoFx(GOOF, 0, player.m_19879_(), player.m_20185_(), player.m_20186_(), player.m_20189_(), 0, 0,
                0, 1, 0, 0, 0, seed));
        }
    }

    /** network/C2SNikaWhip: C let go (Goof Ball ends once it has had its least). */
    public static void letGo(ServerPlayer player) {
        Goof goof = GOOFS.get(player.m_20148_());
        if (goof != null && goof.letGo < 0) {
            goof.letGo = player.m_9236_().m_46467_();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || GOOFS.isEmpty() && DAWNS.isEmpty() && DIZZY.isEmpty()
            || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.m_20148_();
        long now = player.m_9236_().m_46467_();
        try {
            Goof goof = GOOFS.get(id);
            if (goof != null) {
                goofTick(player, goof, now);
            }
            Dawn dawn = DAWNS.get(id);
            if (dawn != null) {
                dawnTick(player, dawn, now);
            }
            Long dizzy = DIZZY.get(id);
            if (dizzy != null && (now >= dizzy || !player.m_6084_())) {
                DIZZY.remove(id);
                if (player.m_6084_()) {
                    HakiServerController.animate(player, NikaJump.idle(player), 3);
                }
            }
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Goof Ball / Dawn Whip failed", failure);
            stop(player);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Goof Ball

    static void goofTick(ServerPlayer player, Goof goof, long now) {
        if (!player.m_6084_() || player.m_5833_() || !GearData.flag(player, "gear5") || GearData.integer(player, "transformTick") > 0) {
            goofEnd(player, goof, now, false);
            return;
        }
        long ball = now - goof.start - NikaGoof.SPIN;
        if (ball >= 0 && ball % GOOF_EVERY == 0) {
            punch(player, goof, now);
        }
        boolean done = ball >= NikaGoof.MOST || goof.letGo >= 0 && ball >= NikaGoof.LEAST;
        if (done) {
            goofEnd(player, goof, now, true);
        }
    }

    static void punch(ServerPlayer player, Goof goof, long now) {
        Vec3 middle = player.m_20182_().m_82520_(0, NikaGoof.MIDDLE, 0);
        int n = 0;
        for (LivingEntity body : PikaMoves.foes(player, middle, GOOF_REACH)) {
            if (n >= GOOF_MOST) {
                break;
            }
            if (!body.m_6469_(StyleCombat.of(player), GOOF_DAMAGE)) {
                continue; // still shaking off the last punch (the game's own moment of grace)
            }
            n++;
            // knocked every which way: mostly away from the ball, thrown about at random
            Vec3 at = StyleCombat.middle(body);
            double ox = at.f_82479_ - middle.f_82479_;
            double oz = at.f_82481_ - middle.f_82481_;
            double ol = Math.max(1.0E-3, Math.sqrt(ox * ox + oz * oz));
            double a = (player.m_217043_().m_188500_() - 0.5) * 2.2;
            double kx = ox / ol * Math.cos(a) - oz / ol * Math.sin(a);
            double kz = ox / ol * Math.sin(a) + oz / ol * Math.cos(a);
            double resist = 1.0;
            body.m_20256_(body.m_20184_().m_82520_(kx * GOOF_KNOCK * resist, 0.22 + 0.15 * player.m_217043_().m_188500_(), kz * GOOF_KNOCK * resist));
            body.f_19864_ = true;
            PieceNetwork.around(body, new S2CUoFx(GOOF_HIT, body.m_19879_(), player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
                (float) kx, 0, (float) kz, 0, 0, 0, body.m_20206_(), (int) (now & 0x7FFFFFFF)));
            if (now - goof.lastLightning >= 8 && ConquerorAura.mixed(player, at, 1.6, 3, 1.0)) {
                goof.lastLightning = now;
            }
        }
        if (n > 0 && now - goof.lastSound >= 4) {
            goof.lastSound = now;
            com.hexhaki.piece.PieceSounds.playAt(player, middle.f_82479_, middle.f_82480_, middle.f_82481_, GearSounds.GOOF_PUNCH, 0.55F,
                0.95F + 0.3F * player.m_217043_().m_188501_());
        }
    }

    static void goofEnd(ServerPlayer player, Goof goof, long now, boolean dazed) {
        UUID id = player.m_20148_();
        GOOFS.remove(id);
        Vec3 middle = player.m_20182_().m_82520_(0, NikaGoof.MIDDLE, 0);
        boolean balled = now - goof.start >= NikaGoof.SPIN;
        PieceNetwork.around(player, new S2CUoFx(GOOF_POP, 0, player.m_19879_(), middle.f_82479_, middle.f_82480_, middle.f_82481_, 0, 0, 0,
            balled ? 1 : 0, 0, 0, 0, dazed && player.m_6084_() ? NikaGoof.DIZZY : 0));
        if (balled) {
            com.hexhaki.piece.PieceSounds.playAt(player, middle.f_82479_, middle.f_82480_, middle.f_82481_, GearSounds.GOOF_POP, 1.0F, 1.0F);
        }
        if (dazed && player.m_6084_()) {
            DIZZY.put(id, now + NikaGoof.DIZZY);
            player.m_7292_(new MobEffectInstance(MobEffects.f_19597_, NikaGoof.DIZZY, 2, false, false, false));
            HakiServerController.animate(player, "gear5_goof_dizzy", 2);
            REST.put(id, now + NikaGoof.DIZZY + GOOF_REST);
        } else {
            REST.put(id, now + GOOF_REST);
            if (player.m_6084_()) {
                HakiServerController.animate(player, NikaJump.idle(player), 3);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ Dawn Whip

    static void dawnTick(ServerPlayer player, Dawn dawn, long now) {
        if (!player.m_6084_() || player.m_5833_() || !GearData.flag(player, "gear5")) {
            dawnEnd(player, dawn, now, true);
            return;
        }
        long age = now - dawn.start;
        player.f_19789_ = 0.0F;
        if (age <= NikaDawn.DRILL) {
            // the user's own walking is taken away while the move turns them about (the slowness is hidden)
            player.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 4, 9, false, false, false));
        }
        if (age < NikaDawn.IMPACT || age >= NikaDawn.OVERSPIN && age < NikaDawn.POP) {
            // rooted: wound up on the spot, and drilling straight down
            Vec3 v = player.m_20184_();
            player.m_20256_(new Vec3(0, Math.min(v.f_82480_, age >= NikaDawn.OVERSPIN ? 0.0 : v.f_82480_), 0));
            player.f_19864_ = true;
        }
        if (age == NikaDawn.IMPACT - NikaDawn.STRETCH_LEAD) {
            stretch(player, dawn);
        }
        if (age == NikaDawn.WIND) {
            com.hexhaki.piece.PieceSounds.playAt(player, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), GearSounds.SPIN, 1.0F,
                1.1F);
        }
        if (age == NikaDawn.IMPACT) {
            sweep(player, dawn, now);
        }
        if (age > NikaDawn.IMPACT && age < NikaDawn.OVERSPIN) {
            // out of control: drifting about on a heading that wanders
            if (age % 4 == 0) {
                dawn.heading += (player.m_217043_().m_188500_() - 0.5) * 1.6;
            }
            Vec3 v = player.m_20184_();
            player.m_20256_(new Vec3(Math.cos(dawn.heading) * DRIFT, v.f_82480_, Math.sin(dawn.heading) * DRIFT));
            player.f_19864_ = true;
        }
        if (age == NikaDawn.OVERSPIN) {
            drill(player, dawn);
        }
        if (age > NikaDawn.OVERSPIN && dawn.torn && dawn.dug < NikaDawn.DEPTH && (age - NikaDawn.OVERSPIN) % NikaDawn.DRILL_STEP == 0) {
            dig(player, dawn);
        }
        if (age == NikaDawn.POP) {
            pop(player, dawn);
        }
        if (age == NikaDawn.POP + 8 && dawn.torn && dawn.mouth != null && player.m_20186_() < dawn.mouth.m_123342_() + 1.0) {
            // never left down the shaft: put back on top if the throw did not carry it out
            player.m_6021_(dawn.mouth.m_123341_() + 0.5, dawn.mouth.m_123342_() + 1.05, dawn.mouth.m_123343_() + 0.5);
        }
        if (age > NikaDawn.POP + 3 && player.m_20096_() || age > NikaDawn.POP + NikaDawn.LANDING) {
            dawnEnd(player, dawn, now, false);
        }
    }

    /** The original's rubber leg (its own S2CGearStretch: the right leg, StretchStyle WHIP, Haki-coated), out at the impact. */
    static void stretch(ServerPlayer player, Dawn dawn) {
        float retract = 1.0F - 0.55F * GearData.rank01(player, com.hexhaki.devilfruit.data.GearUpgrade.SNAP_BACK);
        com.hexhaki.network.HakiNetwork.tracking(player, new com.hexhaki.network.msg.S2CGearStretch(player.m_19879_(),
            com.hexhaki.devilfruit.data.RubberLimb.RIGHT_LEG.ordinal(), com.hexhaki.devilfruit.data.StretchStyle.WHIP.ordinal(),
            (float) NikaDawn.REACH, 1.0F, retract, 1, dawn.wx, 0.0, dawn.wz, false, player.m_217043_().m_188505_()));
    }

    /**
     * The impact: everyone round within the leg's reach and {@link NikaDawn#SWEEP_HEIGHT} of the hips struck and launched sideways, the
     * way the leg sweeps (gear5_dawn_whip turns the leg toward the user's left: round the user that is (r.z, 0, -r.x) for a body in the
     * flat way r), a little outward and up, through blocks up to obsidian; a crater and a dust blast where each comes down.
     */
    static void sweep(ServerPlayer player, Dawn dawn, long now) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 hips = player.m_20182_().m_82520_(0, 0.75, 0);
        boolean first = true;
        int lightning = 0;
        for (LivingEntity body : PikaMoves.foes(player, hips, NikaDawn.REACH)) {
            Vec3 at = StyleCombat.middle(body);
            if (Math.abs(at.f_82480_ - hips.f_82480_) > NikaDawn.SWEEP_HEIGHT + body.m_20206_() * 0.5) {
                continue;
            }
            double rx = at.f_82479_ - hips.f_82479_;
            double rz = at.f_82481_ - hips.f_82481_;
            double rl = Math.sqrt(rx * rx + rz * rz);
            if (rl < 1.0E-3) {
                rx = dawn.wx;
                rz = dawn.wz;
                rl = 1.0;
            }
            rx /= rl;
            rz /= rl;
            double cx = rz + rx * 0.35;
            double cz = -rx + rz * 0.35;
            double cl = Math.sqrt(cx * cx + cz * cz);
            Vec3 carry = new Vec3(cx / cl, 0, cz / cl);
            if (!PikaMoves.hit(player, body, DAWN_DAMAGE, carry.m_82490_(1.2).m_82520_(0, 0.3, 0), true)) {
                continue;
            }
            PieceNetwork.around(body, new S2CUoFx(DAWN_HIT, body.m_19879_(), player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
                (float) carry.f_82479_, 0, (float) carry.f_82481_, 0, 0, 0, body.m_20206_(), (int) (now & 0x7FFFFFFF) + body.m_19879_()));
            if (lightning < 3 && ConquerorAura.mixed(player, at, 2.4, 5, 1.2)) {
                lightning++;
            }
            if (first) {
                first = false;
                com.hexhaki.piece.PieceSounds.playAt(player, at.f_82479_, at.f_82480_, at.f_82481_, GearSounds.CRACK, 1.4F, 1.0F);
                com.hexhaki.piece.PieceSounds.playAt(player, at.f_82479_, at.f_82480_, at.f_82481_, com.hexhaki.piece.gura.GuraSounds.BOOM,
                    1.2F, 1.2F);
                com.hexhaki.fx.Impact.quake(level, at, 18.0, 0.55F, 8);
            }
            PikaMoves.Launch launch = new PikaMoves.Launch(level, player, body, carry, LAUNCH_SPEED);
            launch.hardest = NikaPistol.LAUNCH_HARDEST;
            launch.straight = LAUNCH_STRAIGHT;
            launch.landed = (owner, where) -> {
                Craters.open(level, owner, where, CRATER);
                GuraMoves.fx(owner, GuraMoves.QUAKE, 2, where, new Vec3(0, 1, 0), 0.8, 0, 0, 2.6, (int) (level.m_46467_() & 0xFFFF));
                ConquerorAura.exploded(owner, level, where, 2.6);
                com.hexhaki.fx.Impact.quake(level, where, 18.0, 0.6F, 10);
                com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.BOOM,
                    1.4F, 0.95F);
            };
            launch.stopped = (owner, where) -> {
                com.hexhaki.fx.Impact.quake(level, where, 14.0, 0.45F, 8);
                com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.CRUMBLE,
                    1.0F, 1.0F);
            };
            NikaPistol.LIVE.add(launch);
        }
    }

    /** The drill begins: over the middle of the block under the feet, the shaft opened a layer at a time (mobGriefing obeyed). */
    static void drill(ServerPlayer player, Dawn dawn) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        BlockPos under = BlockPos.m_274561_(player.m_20185_(), player.m_20186_() - 0.2, player.m_20189_());
        boolean ground = player.m_20096_() && !level.m_8055_(under).m_60795_();
        dawn.mouth = under;
        dawn.torn = ground && level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_);
        if (dawn.torn) {
            // over the middle of the shaft (a short slide hidden by the spin and the dust), then the first layer at once
            player.m_6021_(under.m_123341_() + 0.5, player.m_20186_(), under.m_123343_() + 0.5);
            dig(player, dawn);
        }
        PieceNetwork.around(player, new S2CUoFx(DAWN_DRILL, 0, player.m_19879_(), under.m_123341_() + 0.5, under.m_123342_() + 1.0,
            under.m_123343_() + 0.5, 0, 0, 0, dawn.torn ? 1 : 0, 0, 0, 0, dawn.seed));
        com.hexhaki.piece.PieceSounds.playAt(player, player.m_20185_(), player.m_20186_(), player.m_20189_(), GearSounds.SPIN, 1.2F, 0.8F);
        com.hexhaki.piece.PieceSounds.playAt(player, player.m_20185_(), player.m_20186_(), player.m_20189_(),
            com.hexhaki.piece.gura.GuraSounds.CRUMBLE, 1.2F, 0.9F);
    }

    /** One layer of the shaft torn out (the first with its four neighbours: a wider mouth), growing back {@link #SHAFT_BACK} later. */
    static void dig(ServerPlayer player, Dawn dawn) {
        if (dawn.mouth == null) {
            return;
        }
        Scars scars = Scars.of((ServerLevel) player.m_9236_());
        BlockPos at = new BlockPos(dawn.mouth.m_123341_(), dawn.mouth.m_123342_() - dawn.dug, dawn.mouth.m_123343_());
        int done = scars.tear(player, at, List.of(), SHAFT_BACK);
        if (done == Scars.REFUSED) {
            dawn.dug = NikaDawn.DEPTH; // bedrock, a claim: the drill goes no further
            if (at.equals(dawn.mouth)) {
                dawn.torn = false;
            }
            return;
        }
        if (dawn.dug == 0) {
            for (int k = 0; k < 4; k++) {
                int dx = k == 0 ? 1 : k == 1 ? -1 : 0;
                int dz = k == 2 ? 1 : k == 3 ? -1 : 0;
                scars.tear(player, new BlockPos(at.m_123341_() + dx, at.m_123342_(), at.m_123343_() + dz), List.of(), SHAFT_BACK);
            }
        }
        dawn.dug++;
        com.hexhaki.piece.PieceSounds.playAt(player, at.m_123341_() + 0.5, at.m_123342_() + 0.5, at.m_123343_() + 0.5,
            com.hexhaki.piece.gura.GuraSounds.CRUMBLE, 0.8F, 1.1F);
    }

    /** Popped back out of the ground: thrown up out of the shaft with the rubber bounce. */
    static void pop(ServerPlayer player, Dawn dawn) {
        Vec3 v = player.m_20184_();
        player.m_20256_(new Vec3(v.f_82479_ * 0.2, dawn.torn ? POP_LIFT : POP_LIFT * 0.6, v.f_82481_ * 0.2));
        player.f_19864_ = true;
        HakiServerController.animate(player, "gear5_dawn_pop", 2);
        BlockPos mouth = dawn.mouth != null ? dawn.mouth : BlockPos.m_274561_(player.m_20185_(), player.m_20186_() - 0.2, player.m_20189_());
        PieceNetwork.around(player, new S2CUoFx(DAWN_POP, 0, player.m_19879_(), mouth.m_123341_() + 0.5, mouth.m_123342_() + 1.0,
            mouth.m_123343_() + 0.5, 0, 0, 0, dawn.torn ? 1 : 0, 0, 0, 0, dawn.seed));
        com.hexhaki.piece.PieceSounds.playAt(player, mouth.m_123341_() + 0.5, mouth.m_123342_() + 1.0, mouth.m_123343_() + 0.5,
            GearSounds.GOOF_POP, 1.2F, 0.85F);
    }

    static void dawnEnd(ServerPlayer player, Dawn dawn, long now, boolean cut) {
        UUID id = player.m_20148_();
        DAWNS.remove(id);
        REST.put(id, now + DAWN_REST);
        if (cut) {
            if (dawn.torn && dawn.mouth != null && player.m_6084_() && player.m_20186_() < dawn.mouth.m_123342_() + 1.0) {
                player.m_6021_(dawn.mouth.m_123341_() + 0.5, dawn.mouth.m_123342_() + 1.05, dawn.mouth.m_123343_() + 0.5);
            }
            PieceNetwork.around(player, new S2CUoFx(STOP, 0, player.m_19879_(), player.m_20185_(), player.m_20186_(), player.m_20189_(), 0, 0,
                0, 0, 0, 0, 0, 0));
        }
        if (player.m_6084_()) {
            player.m_21195_(MobEffects.f_19597_);
            if (!cut) {
                HakiServerController.animate(player, NikaJump.idle(player), 3);
            }
        }
    }

    /** Everything of the player's stopped at once (an error, death, logging out). */
    static void stop(ServerPlayer player) {
        UUID id = player.m_20148_();
        long now = player.m_9236_().m_46467_();
        Goof goof = GOOFS.remove(id);
        Dawn dawn = DAWNS.remove(id);
        DIZZY.remove(id);
        if (dawn != null) {
            dawnEnd(player, dawn, now, true);
        } else if (goof != null) {
            PieceNetwork.around(player, new S2CUoFx(STOP, 0, player.m_19879_(), player.m_20185_(), player.m_20186_(), player.m_20189_(), 0, 0,
                0, 0, 0, 0, 0, 0));
        }
    }

    /** No fall damage through Dawn Whip (its drift, the drill, the pop's landing). */
    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        if (!DAWNS.isEmpty() && event.getEntity() instanceof ServerPlayer player && DAWNS.containsKey(player.m_20148_())) {
            event.setCanceled(true);
        }
    }

    /** The original's walk plays its run or its stance as the body leaves and touches the ground: held back while these run. */
    public static boolean holds(ServerPlayer player, String name) {
        return (GOOFS.containsKey(player.m_20148_()) || DAWNS.containsKey(player.m_20148_()) || DIZZY.containsKey(player.m_20148_()))
            && ("gear5_run".equals(name) || "gear5_stance".equals(name) || "__clear__".equals(name));
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stop(player);
        }
        REST.remove(event.getEntity().m_20148_());
    }

    /** The ids of everyone mid-move (for tests). */
    static List<UUID> running() {
        List<UUID> out = new ArrayList<>(GOOFS.keySet());
        out.addAll(DAWNS.keySet());
        return out;
    }
}
