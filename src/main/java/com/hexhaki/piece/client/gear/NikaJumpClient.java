package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.devilfruit.RemoteGearStates;
import com.hexhaki.devilfruit.data.BajrangPhase;
import com.hexhaki.devilfruit.data.GearAbility;
import com.hexhaki.piece.gear.RubberJump;
import com.hexhaki.piece.network.C2SNikaJump;
import com.hexhaki.piece.network.PieceNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.2 (the owner: "shift + space ability is removed, default jumping is much higher and always does the jump animation we're doing,
 * it always turns the ground into rubber for the player using the fruit"; asked, "Gear 5 only" and "About 5 blocks"): Gear 5's jump on
 * one's own client, where the body is moved.
 * <ul>
 *   <li>Sky Bounce is gone: the original's Shift + Space (GearMovement.movementInput) asks {@link #usable} whether Sky Bounce may go, and
 *       it never may; Shift + Space is an ordinary jump.</li>
 *   <li>Every jump in Gear 5 goes {@link RubberJump#LIFT} faster up (about 4.9 blocks; not while transforming, as the giant, in the
 *       Bajrang Gun, flying, riding or in water), the server is told (C2SNikaJump: the clip, the ripples everyone else sees, no fall
 *       damage), and the ground under it springs up and ripples at once on this screen (client/gear/RubberWorldDraw), and again where
 *       it lands.</li>
 * </ul>
 * 3.3.8 (the owner: "every fall should make the floor ripple"): any landing in Gear 5 from {@link RubberJump#FALL_MIN} blocks or more
 * over the feet ripples here at once, as server/NikaJump has everyone else see it (the Bajrang Gun's fall, a ledge, the bounding run).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaJumpClient {
    /** A jump in the air, its ticks so far (its landing ripples here at once). */
    private static boolean airborne;
    private static int airTicks;
    /** 3.3.8: in the air in Gear 5, the highest the feet have been (NaN on the ground). */
    private static double peak = Double.NaN;

    private NikaJumpClient() {
    }

    /** patch/PiecePatch, in GearMovement.movementInput: whether {@code ability} may go: Sky Bounce never (removed, 3.3.2). */
    public static boolean usable(GearAbility ability) {
        return ability != GearAbility.SKY_BOUNCE && GearClientState.usable(ability);
    }

    @SubscribeEvent
    public static void jumped(LivingEvent.LivingJumpEvent event) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (player == null || event.getEntity() != player || !may(player)) {
            return;
        }
        Vec3 v = player.m_20184_();
        player.m_20334_(v.f_82479_, RubberJump.lifted(v.f_82480_), v.f_82481_);
        PieceNetwork.toServer(new C2SNikaJump());
        RubberWorldDraw.hop(player.m_20185_(), player.m_20186_(), player.m_20189_(), false, size(player));
        airborne = true;
        airTicks = 0;
    }

    /**
     * Gear 5, and nothing holding the body: not transforming, not in the Bajrang Gun, not flying, riding or swimming. 3.4.9 (the owner:
     * the giant's "jumping animations and all should remain the same as the one we got"): the giant jumps as the normal body does.
     */
    static boolean may(LocalPlayer player) {
        if (!GearClientState.gearFive || GearClientState.transforming() || player.m_150110_().f_35935_ || player.m_20159_() || player.m_20069_()) {
            return false;
        }
        RemoteGearStates.State state = RemoteGearStates.get(player.m_19879_());
        return state == null || state.bajrang == null || state.bajrang == BajrangPhase.NONE;
    }

    /** 3.4.9: how many times the normal body this one is drawn (Giant's 4, growing and shrinking): its ripples are that big. */
    static double size(LocalPlayer player) {
        RemoteGearStates.State state = RemoteGearStates.get(player.m_19879_());
        return state == null ? 1.0 : Math.max(1.0, Math.min(6.0, state.scale(1.0F)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (player == null) {
            airborne = false;
            peak = Double.NaN;
            return;
        }
        boolean ground = player.m_20096_();
        double y = player.m_20186_();
        if (!ground) {
            if (!GearClientState.gearFive || player.m_150110_().f_35935_ || player.m_20159_() || player.m_20069_()) {
                peak = Double.NaN;
            } else {
                peak = Double.isNaN(peak) ? y : Math.max(peak, y);
            }
        }
        if (airborne) {
            airTicks++;
            if (airTicks > 200) {
                airborne = false;
            } else if (airTicks >= 3 && ground) {
                airborne = false;
                peak = Double.NaN;
                RubberWorldDraw.hop(player.m_20185_(), y, player.m_20189_(), true, size(player));
                return;
            }
        }
        if (ground && !Double.isNaN(peak)) {
            if (peak - y >= RubberJump.FALL_MIN && GearClientState.gearFive && !airborne) {
                RubberWorldDraw.hop(player.m_20185_(), y, player.m_20189_(), true, size(player));
            }
            peak = Double.NaN;
        }
    }
}
