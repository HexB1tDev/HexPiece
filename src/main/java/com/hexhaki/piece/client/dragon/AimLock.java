package com.hexhaki.piece.client.dragon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.player.Input;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.8.5 (the owner: "whenever the blast breath is being charged, you can freely aim it, but when you let go, your aim is instantly
 * locked into that position without being able to move until it's over. It's the drawback of that move. So you have to actually think
 * where you're gonna aim it"): while one's own Blast Breath burns (either form), the view is held where it was let go (the server's
 * S2CUoFx FIRE_BEAM carries that aim, the beam keeps it: server/UoMoves.Beam): every tick and every frame the player's turn and tilt
 * are put back on it (before the frame is drawn: the mouse's turn of that frame undone), so the camera, the dragon's head and the beam
 * stay on it. 2.8.7 (the owner, asked whether the dragon should be held still too: "it should yes"): the body is held where it was as
 * well: no walking, flying up or down, or drifting (every movement key's input dropped, the motion stopped each tick). Freed when the
 * beam ends (its life, or the server's FIRE_BEAM_OFF), on leaving the world, or if the player dies.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class AimLock {
    private static boolean locked;
    private static float yaw;
    private static float pitch;
    private static int left;

    private AimLock() {
    }

    /** A beam began for {@code user}, aimed at ({@code aimYaw}, {@code aimPitch}), burning {@code ticks}: one's own holds the view. */
    public static void beam(int user, float aimYaw, float aimPitch, int ticks) {
        LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || self.m_19879_() != user) {
            return;
        }
        locked = true;
        yaw = aimYaw;
        pitch = aimPitch;
        left = ticks + 1;
        hold(self);
    }

    /** {@code user}'s beam ended early (the server's FIRE_BEAM_OFF). */
    public static void off(int user) {
        LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self != null && self.m_19879_() == user) {
            locked = false;
        }
    }

    /** Whether one's own view is held by a beam now. */
    public static boolean locked() {
        return locked;
    }

    static void hold(LocalPlayer player) {
        player.m_20334_(0.0, 0.0, 0.0);
        player.m_146922_(yaw);
        player.m_146926_(pitch);
        player.f_19859_ = yaw;
        player.f_19860_ = pitch;
        player.f_20885_ = yaw;
        player.f_20886_ = yaw;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!locked) {
            return;
        }
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (player == null || !player.m_6084_()) {
            locked = false;
            return;
        }
        hold(player);
        if (event.phase == TickEvent.Phase.END && --left <= 0) {
            locked = false;
        }
    }

    /** Before each frame is drawn (the mouse has turned the player already this frame): back onto the held aim. */
    @SubscribeEvent
    public static void frame(TickEvent.RenderTickEvent event) {
        if (!locked || event.phase != TickEvent.Phase.START) {
            return;
        }
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (player != null) {
            hold(player);
        }
    }

    /** 2.8.7: no movement key moves the body while the beam burns (walking, strafing, flying up or down). */
    @SubscribeEvent
    public static void movement(MovementInputUpdateEvent event) {
        if (!locked || event.getEntity() != Minecraft.m_91087_().f_91074_) {
            return;
        }
        Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        locked = false;
    }
}
