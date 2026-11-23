package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.devilfruit.gameplay.GearServerController;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.gear.GearSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.9: Dawn Pistol, the Nika fruit's left click with Haki on in Gear 5 (the owner: "We need to better the hotkey [Haki] versions";
 * asked, "Build as proposed"). The original's {@code GearServerController.pistol} throws the same stretched arm as the Pistol, a little
 * further and harder, with the same clip; patch/PiecePatch.dawnPistol makes it ask here:
 * <ul>
 *   <li>{@link #clip}, in place of its {@code animate(player, "gear5_pistol", 2)}: with Haki on, the cartoon wind-up
 *       {@code gear5_dawn_pistol} (the right arm thrown) or {@code gear5_dawn_pistol_left} (the left: the original alternates them and has
 *       just chosen), and the throw remembered ({@link #THROWN}). Their arms are the Pistol's own, so first person stays as it is.</li>
 *   <li>{@link #struck}, right after the original's {@code hit} where the thrown arm lands on a body: a Dawn Pistol thrown within
 *       {@link #WINDOW} ticks draws the cartoon POW where it lands ({@link #POW}: client/gear/NikaPow) and plays the cartoon punch;
 *       3.4.0 (the owner: "the fist goes through the body, stretching the skin to insane amounts and then causes a catastrophic knockback
 *       that destroys all blocks in it's way"): the body is held ({@link Pin}) while every client draws its skin stretched out through it
 *       to the fist (client/gear/NikaSkinClient, from the POW), then launched through every block up to obsidian. The original's own impact (and with it the Conqueror's lightning,
 *       ConquerorAura.gear, with the Conqueror's Haki and Ryou on) still comes.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NikaPistol {
    /** S2CUoFx kind (the Nika fruit's 160 to 179): the POW where a Dawn Pistol lands (id the body struck, user the thrower, x y z the
     * body's middle, ax ay az the way the fist went, size the body's height). */
    public static final int POW = 164;
    public static final String CLIP_RIGHT = "gear5_dawn_pistol";
    public static final String CLIP_LEFT = "gear5_dawn_pistol_left";
    /** A Dawn Pistol thrown lands within this many ticks (the Pistol's wind-up 6 and reach 4, and some slack), or never. */
    static final int WINDOW = 24;

    static final Map<UUID, Long> THROWN = new HashMap<>();

    private NikaPistol() {
    }

    /** patch/PiecePatch, in GearServerController.pistol: the clip, the Dawn Pistol's own with Haki on. */
    public static void clip(ServerPlayer player, String name, int priority) {
        if (!GearServerController.dawn(player)) {
            HakiServerController.animate(player, name, priority);
            return;
        }
        // the original's alternate() has just chosen the arm: "pistolSide" true is the right one
        boolean right = GearData.flag(player, "pistolSide");
        if (THROWN.size() > 256) {
            THROWN.clear();
        }
        THROWN.put(player.m_20148_(), player.m_9236_().m_46467_());
        HakiServerController.animate(player, right ? CLIP_RIGHT : CLIP_LEFT, priority);
    }

    /** The original's award number for the Pistol's landing (the throw's lambda shares it with the Gatling's, which is 1). */
    public static final int PISTOL_AWARD = 5;

    /**
     * patch/PiecePatch, in the throw's landing (GearServerController's throwLimb lambda), right after its hit on {@code target}; {@code award}
     * says whose throw it was (the Pistol's {@link #PISTOL_AWARD}).
     */
    public static void struck(ServerPlayer player, LivingEntity target, Vec3 way, int award) {
        if (THROWN.isEmpty() || award != PISTOL_AWARD || player == null || target == null) {
            return;
        }
        Long at = THROWN.remove(player.m_20148_());
        if (at == null || player.m_9236_().m_46467_() - at > WINDOW) {
            return;
        }
        try {
            land(player, target, way);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's landing failed", failure);
        }
    }

    static void land(ServerPlayer player, LivingEntity target, Vec3 way) {
        // how hard it goes: the hit's own throw already weighs the body's knockback resistance (1.25 for a body that takes it all)
        Vec3 v = target.m_20184_();
        double flat = Math.sqrt(v.f_82479_ * v.f_82479_ + v.f_82481_ * v.f_82481_);
        double share = Math.min(1.0, flat / 1.25);
        ServerLevel level = (ServerLevel) player.m_9236_();
        AABB box = target.m_20191_();
        Vec3 mid = box.m_82399_();
        double wx = way == null ? 0.0 : way.f_82479_;
        double wy = way == null ? 0.0 : way.f_82480_;
        double wz = way == null ? 0.0 : way.f_82481_;
        double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wl < 1.0E-6) {
            Vec3 look = player.m_20154_();
            wx = look.f_82479_;
            wy = look.f_82480_;
            wz = look.f_82481_;
            wl = Math.max(1.0E-6, Math.sqrt(wx * wx + wy * wy + wz * wz));
        }
        wx /= wl;
        wy /= wl;
        wz /= wl;
        // the POW, and with it the body pulled out like rubber (bx: how hard, a weaker pull for a body that barely moved: every client
        // bends it, client/gear/NikaPow -> NikaBendClient.dawn, gear/NikaBend.dawn)
        PieceNetwork.around(target, new S2CUoFx(POW, target.m_19879_(), player.m_19879_(), mid.f_82479_, mid.f_82480_, mid.f_82481_,
            (float) wx, (float) wy, (float) wz, (float) Math.max(0.4, share), 0, 0, (float) (box.f_82292_ - box.f_82289_),
            (int) (level.m_46467_() & 0x7FFFFFFF)));
        com.hexhaki.piece.PieceSounds.playAt(player, mid.f_82479_, mid.f_82480_, mid.f_82481_, GearSounds.DAWN_PISTOL, 1.0F, 1.0F);
        // 3.4.0: held where it was struck while the fist stretches its skin out through it (gear/NikaSkin.PIN), then launched
        Vec3 carry = new Vec3(wx, 0, wz);
        if (carry.m_82553_() < 0.2) {
            Vec3 look = player.m_20154_();
            carry = new Vec3(look.f_82479_, 0, look.f_82481_);
        }
        carry = carry.m_82553_() < 1.0E-4 ? new Vec3(0, 0, 1) : carry.m_82541_();
        LIVE.add(new Pin(level, player, target, target.m_20182_(), carry, level.m_46467_() + com.hexhaki.piece.gear.NikaSkin.PIN,
            LAUNCH_SPEED * (0.55 + 0.45 * share)));
    }

    /**
     * 3.4.0: the launch: blocks a tick (a body that takes the whole blow) and the hardest block it tears through (obsidian). 3.4.1 (the
     * owner: "the entity is projected in a straight line FAST. adding a dust explosion on landing (and making a crater)"): faster, flown in a
     * straight line for {@link #STRAIGHT} ticks, then falling; where it comes down a crater ({@link #CRATER}, self-repairing) and a dust
     * explosion (the Gura fruit's quake: dust, rings, rocks, cracks), the ground shaking, the Conqueror's lightning out of it.
     */
    public static final double LAUNCH_SPEED = 5.0;
    public static final float LAUNCH_HARDEST = 50.0F;
    public static final int STRAIGHT = 8;
    public static final double CRATER = 2.4;

    static final java.util.List<UoMoves.Live> LIVE = new java.util.ArrayList<>();

    /**
     * 3.4.0: the struck body held where it was struck (its motion stopped every tick) until {@code until}, the fist stretching its skin out
     * through it (every client draws it: client/gear/NikaSkinClient); then the skin snaps back and the body is launched along {@code carry}
     * through every block up to obsidian (the Light fruit's PikaMoves.Launch: torn out through the self-repairing Scars, never dropped,
     * claims and protection obeyed; knocked down where it stops), the ground shaking.
     */
    static final class Pin implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 at;
        final Vec3 carry;
        final long until;
        final double speed;

        Pin(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 at, Vec3 carry, long until, double speed) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.at = at;
            this.carry = carry;
            this.until = until;
            this.speed = speed;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.body.m_6084_() || this.body.m_9236_() != this.level || !this.owner.m_6084_()) {
                return true;
            }
            if (now < this.until) {
                this.body.m_20256_(Vec3.f_82478_);
                this.body.f_19864_ = true;
                this.body.f_19789_ = 0.0F;
                // put back where it was struck once it has moved (as the Ice fruit holds a frozen body; a player too)
                if (this.body.m_20182_().m_82557_(this.at) > 0.12 * 0.12) {
                    this.body.m_6021_(this.at.f_82479_, this.at.f_82480_, this.at.f_82481_);
                }
                return false;
            }
            Vec3 mid = this.body.m_20191_().m_82399_();
            com.hexhaki.fx.Impact.quake(this.level, mid, 20.0, 0.6F, 10);
            com.hexhaki.piece.PieceSounds.playAt(this.owner, mid.f_82479_, mid.f_82480_, mid.f_82481_, com.hexhaki.piece.gura.GuraSounds.BOOM, 1.4F,
                1.15F);
            PikaMoves.Launch launch = new PikaMoves.Launch(this.level, this.owner, this.body, this.carry, this.speed);
            launch.hardest = LAUNCH_HARDEST;
            launch.straight = STRAIGHT;
            launch.landed = (owner, where) -> {
                Craters.open(this.level, owner, where, CRATER);
                GuraMoves.fx(owner, GuraMoves.QUAKE, 2, where, new Vec3(0, 1, 0), 1.0, 0, 0, 3.2, (int) (this.level.m_46467_() & 0xFFFF));
                ConquerorAura.exploded(owner, this.level, where, 3.2);
                com.hexhaki.fx.Impact.quake(this.level, where, 22.0, 0.7F, 12);
                com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.BOOM,
                    1.6F, 0.9F);
            };
            launch.stopped = (owner, where) -> {
                com.hexhaki.fx.Impact.quake(this.level, where, 16.0, 0.5F, 8);
                com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.HIT,
                    1.4F, 0.8F);
                com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.CRUMBLE,
                    1.0F, 1.0F);
            };
            LIVE.add(launch);
            return true;
        }
    }

    @SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || LIVE.isEmpty() || !(event.level instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        // a snapshot: a pin adds its launch while it is walked
        java.util.List<UoMoves.Live> due = new java.util.ArrayList<>();
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

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        THROWN.remove(event.getEntity().m_20148_());
    }
}
