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
 *       {@link #WINDOW} ticks sends the struck body {@link #FAR} times as far back as the hit did and lifts it, draws the cartoon POW
 *       where it lands ({@link #POW}: client/gear/NikaPow), which every client also takes as the body pulled out lengthways like rubber
 *       (the owner: "make sure it bends the entities a ton more and looks better than the current one": gear/NikaBend.dawn), and plays
 *       the cartoon punch. The original's own impact (and with it the Conqueror's lightning,
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
    /** The struck body thrown this many times as far back as the original's hit throws it, and lifted up to this (blocks a tick). */
    public static final double FAR = 2.0;
    public static final double LIFT = 0.45;

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
        // knocked far back: the hit's own throw (it already weighs the body's knockback resistance) made FAR times as long, and lifted
        Vec3 v = target.m_20184_();
        double flat = Math.sqrt(v.f_82479_ * v.f_82479_ + v.f_82481_ * v.f_82481_);
        double share = Math.min(1.0, flat / 1.25);
        if (flat > 1.0E-4) {
            target.m_20334_(v.f_82479_ * FAR, Math.max(v.f_82480_, LIFT * share), v.f_82481_ * FAR);
            target.f_19864_ = true;
        }
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
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        THROWN.remove(event.getEntity().m_20148_());
    }
}
