package com.hexhaki.piece.server;

import com.hexhaki.piece.PieceSounds;
import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunAnims;
import com.hexhaki.piece.gun.GunItem;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.gun.GunSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CGun;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.8: the guns on the server (the owner's category Gun: "all weapons only shoot 1 bullet at a time, and auto reloads after"). Anyone
 * holding a gun in the main hand: taken in hand it is spun into the relaxed hold ({@link GunAnims#DRAW}); M1 ({@code network/C2SGun},
 * with the point under the crosshair) from the hold, loaded, raises it ({@link GunAnims#FIRE}, the clip of the look's pitch) and
 * {@link GunRules#AIM} ticks later fires one ball ({@link GunShots}); {@link GunRules#RELOAD_FROM} ticks after that the reload plays
 * ({@link GunAnims#RELOAD}) and at its end it is loaded again. Put away mid-reload, it comes back empty and reloads after its draw. What
 * it is doing goes to everyone near ({@link S2CGun}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class GunServer {
    private static final Map<UUID, Hold> HOLDS = new HashMap<>();

    static final class Hold {
        int gun = -1;
        int anim = GunAnims.NONE;
        int start;
        boolean empty;
        boolean fired;
        Vec3 aim;
    }

    private GunServer() {
    }

    static int now(ServerPlayer player) {
        return (int) (player.m_9236_().m_46467_() & 0x3FFFFFFF);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Gun gun = GunItem.of(player.m_21205_());
        Hold hold = HOLDS.get(player.m_20148_());
        if (hold == null) {
            if (gun == null) {
                return;
            }
            hold = new Hold();
            HOLDS.put(player.m_20148_(), hold);
        }
        int now = now(player);
        int g = gun == null ? -1 : gun.ordinal();
        if (g != hold.gun) {
            hold.gun = g;
            hold.fired = true;
            set(player, hold, g < 0 ? GunAnims.NONE : GunAnims.DRAW, now);
            return;
        }
        if (g < 0) {
            return;
        }
        int t = (now - hold.start) & 0x3FFFFFFF;
        if (hold.anim == GunAnims.DRAW && t >= GunAnims.DRAW_TICKS) {
            set(player, hold, hold.empty ? GunAnims.RELOAD : GunAnims.HOLD, now);
        } else if (GunAnims.firing(hold.anim)) {
            if (!hold.fired && t >= GunRules.AIM) {
                hold.fired = true;
                hold.empty = true;
                shoot(player, hold.aim);
            }
            if (t >= GunAnims.FIRE_TICKS) {
                set(player, hold, GunAnims.RELOAD, now);
            }
        } else if (hold.anim == GunAnims.RELOAD) {
            if (t == GunAnims.BALL_HOME) {
                PieceSounds.playAt(player, player.m_20185_(), player.m_20186_() + 1.2, player.m_20189_(), GunSounds.LOAD, 0.7F, 1.15F);
            }
            if (t >= GunRules.RELOAD) {
                hold.empty = false;
                PieceSounds.playAt(player, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), GunSounds.COCK, 0.8F, 1.0F);
                set(player, hold, GunAnims.HOLD, now);
            }
        }
    }

    /** M1 with a gun: from the hold (or its draw), loaded, raise it and fire at {@code aim} (the point under the crosshair). */
    public static void fire(ServerPlayer player, Vec3 aim) {
        Hold hold = HOLDS.get(player.m_20148_());
        if (hold == null || hold.gun < 0 || hold.empty || !(hold.anim == GunAnims.HOLD || hold.anim == GunAnims.DRAW)
            || GunItem.of(player.m_21205_()) == null || player.m_5833_() || !player.m_6084_()) {
            return;
        }
        hold.aim = aim;
        hold.fired = false;
        set(player, hold, GunAnims.FIRE + GunAnims.bucket(player.m_146909_()), now(player));
    }

    /** The ball leaves the muzzle for the aim (the look itself if the aim sent is past the range or off the look). */
    static void shoot(ServerPlayer player, Vec3 aim) {
        Vec3 eye = player.m_146892_();
        Vec3 look = player.m_20154_();
        Vec3 flat = new Vec3(-look.f_82481_, 0.0, look.f_82479_);
        Vec3 right = flat.m_82553_() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : flat.m_82541_();
        Vec3 up = right.m_82537_(look).m_82541_();
        Vec3 from = eye.m_82549_(look.m_82490_(GunRules.MUZZLE_AHEAD)).m_82549_(right.m_82490_(GunRules.MUZZLE_RIGHT))
            .m_82546_(up.m_82490_(GunRules.MUZZLE_DOWN));
        Vec3 dir = aim == null ? null : aim.m_82546_(from);
        if (dir == null || dir.m_82553_() < 0.5 || aim.m_82554_(eye) > GunRules.RANGE + 8.0
            || Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dir.m_82541_().m_82526_(look))))) > GunRules.AIM_SLACK) {
            dir = eye.m_82549_(look.m_82490_(GunRules.RANGE)).m_82546_(from);
        }
        boolean haki = com.hexhaki.data.HakiData.flag(player, "armamentOn");
        GunShots.fire(player, from, dir.m_82541_(), haki);
        PieceSounds.playAt(player, from.f_82479_, from.f_82480_, from.f_82481_, GunSounds.SHOT, 2.2F, haki ? 0.85F : 1.0F);
        if (haki) {
            PieceSounds.playAt(player, from.f_82479_, from.f_82480_, from.f_82481_, GunSounds.HAKI_SHOT, 1.2F, 1.5F);
        }
    }

    static void set(ServerPlayer player, Hold hold, int anim, int now) {
        hold.anim = anim;
        hold.start = now;
        PieceNetwork.around(player, new S2CGun(player.m_19879_(), hold.gun, anim, now));
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            Hold hold = HOLDS.get(subject.m_20148_());
            if (hold != null && hold.gun >= 0) {
                PieceNetwork.sendTo(viewer, new S2CGun(subject.m_19879_(), hold.gun, hold.anim, hold.start));
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        HOLDS.remove(event.getEntity().m_20148_());
    }
}
