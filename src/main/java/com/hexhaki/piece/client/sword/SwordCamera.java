package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.SwordAnims;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.5 (the owner: "Swords in first person are being weilded in a very strange way and unrealistic, I dont Like any of it, from now on,
 * when grabbing a blade as a swords man you'll go into third person, forcefully, the camera adjusts to better aid your swings and aim"):
 * while one's own blade is out of its saya the view is third person (first person is switched away from at once, and again if picked
 * while it is out), and goes back to first person when the blade is sheathed if that is where it was. The third-person camera glides
 * to {@link #SHOULDER} over the right shoulder, {@link #UP} higher and {@link #BACK} behind (the game's own is 4 straight back): the
 * blade's arc and what the crosshair is on both in view, never the back of the head over the target. Its place goes through the
 * original's camera hook (client/dragon/DragonCamera.position), stopped short of any block. 3.8.8: a gun in the hand too (the owner's
 * category Gun: aimed from over the shoulder, the crosshair where the ball goes: client/gun/GunInput).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SwordCamera {
    static final double SHOULDER = 0.85;
    static final double UP = 0.3;
    static final double BACK = 3.1;
    /** How fast the camera glides to the shoulder and back (of the way a tick). */
    static final float GLIDE = 0.2F;
    static boolean fromFirstPerson;
    static float out;
    static float lastOut;

    private SwordCamera() {
    }

    /** Is one's own blade out of its saya now (3.8.8: or a gun in the hand)? */
    static boolean drawn(LocalPlayer player) {
        int id = player.m_19879_();
        if (com.hexhaki.piece.client.gun.GunClient.gun(id) != null) {
            return true;
        }
        Katana katana = SwordClient.katana(id);
        return katana != null && SwordAnims.drawn(SwordClient.anim(id), SwordClient.ticks(player, 0.0F), katana);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        lastOut = out;
        if (player == null) {
            out = 0.0F;
            fromFirstPerson = false;
            return;
        }
        boolean drawn;
        try {
            drawn = drawn(player);
        } catch (RuntimeException failure) {
            drawn = false;
        }
        if (drawn && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
            fromFirstPerson = true;
        } else if (!drawn && fromFirstPerson) {
            if (minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK) {
                minecraft.f_91066_.m_92157_(CameraType.FIRST_PERSON);
            }
            fromFirstPerson = false;
        }
        out = drawn ? Math.min(1.0F, out + GLIDE) : Math.max(0.0F, out - GLIDE);
    }

    /** The camera's place for {@code entity} (one's own player, third person from behind, the blade out), or null: the game's own. */
    public static Vec3 position(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (entity != minecraft.f_91074_ || minecraft.f_91066_.m_92176_() != CameraType.THIRD_PERSON_BACK) {
            return null;
        }
        float o = lastOut + (out - lastOut) * partial;
        if (o <= 0.0F) {
            return null;
        }
        double e = o * o * (3.0 - 2.0 * o);
        Vec3 eye = entity.m_20299_(partial);
        Vec3 look = entity.m_20252_(partial);
        double rl = Math.hypot(look.f_82479_, look.f_82481_);
        Vec3 right = rl < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-look.f_82481_ / rl, 0.0, look.f_82479_ / rl);
        Vec3 shoulder = com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, eye,
            eye.m_82549_(right.m_82490_(SHOULDER * e)).m_82520_(0.0, UP * e, 0.0));
        double back = 4.0 + (BACK - 4.0) * e;
        return com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, shoulder, shoulder.m_82546_(look.m_82490_(back)));
    }
}
