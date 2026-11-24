package com.hexhaki.piece.client.dragon;

import com.hexhaki.client.cinematic.CinematicController;
import com.hexhaki.piece.dragon.DragonForm;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 2.7.0: the third-person camera of a player in dragon form. The original's camera hook (its CameraMixin, at the end of Camera.setup)
 * asks {@code CinematicController.kingsGripCameraPosition} for a fixed place; PiecePatch sends that call here, which hands back the
 * original's own place when it has one (King's Grip) and otherwise, while the dragon is out and the view is third person, a place
 * {@link #BACK} blocks behind the look and {@link #UP} over the eye, so the whole dragon is seen ahead of the camera (the vanilla 4
 * blocks would be inside its coils). It glides out as the dragon forms and in as it goes, follows the view smoothly (the player's own
 * eye and look, interpolated), and stops short of any block between the eye and it. First person is the game's own.
 */
public final class DragonCamera {
    static final double BACK = 26.0;
    static final double UP = 11.0;
    /** Kept off a wall it would go into (blocks). */
    static final double MARGIN = 0.4;

    private DragonCamera() {
    }

    public static Vec3 position(Entity entity, float partial) {
        Vec3 original = CinematicController.kingsGripCameraPosition(entity, partial);
        if (original != null || entity == null) {
            return original;
        }
        try {
            if (!DragonClient.ENTRIES.isEmpty()) {
                Vec3 dragon = dragon(entity, partial);
                if (dragon != null) {
                    return dragon;
                }
            }
            // 3.5.8: Galaxy Impact's low shot round its user (client/ironfist/IronFx)
            // 3.6.7: Buzz Cut Mochi's camera rising with the lift (client/mochi/MochiFx; its cutscene is gone at the owner's word)
            Vec3 mochi = com.hexhaki.piece.client.mochi.MochiFx.camera(entity, partial);
            if (mochi != null) {
                return mochi;
            }
            Vec3 iron = com.hexhaki.piece.client.ironfist.IronFx.camera(entity, partial);
            if (iron != null) {
                return iron;
            }
            // 3.4.9: Gear 5's giant has a camera its size (client/gear/GiantCamera)
            return com.hexhaki.piece.client.gear.GiantCamera.position(entity, partial);
        } catch (RuntimeException e) {
            return null; // the game's own camera
        }
    }

    static Vec3 dragon(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        CameraType type = minecraft.f_91066_.m_92176_();
        if (type == CameraType.FIRST_PERSON || DragonClient.entry(entity) == null) {
            return null;
        }
        float out = DragonForm.smooth(DragonClient.presence(entity.m_19879_(), partial));
        if (out <= 0.0F) {
            return null;
        }
        Vec3 eye = entity.m_20299_(partial);
        Vec3 look = entity.m_20252_(partial);
        if (type == CameraType.THIRD_PERSON_FRONT) {
            look = look.m_82490_(-1.0);
        }
        double back = 4.0 + (BACK - 4.0) * out;
        double up = UP * out;
        Vec3 raised = clip(entity, eye, eye.m_82520_(0.0, up, 0.0));
        return clip(entity, raised, raised.m_82520_(-look.f_82479_ * back, -look.f_82480_ * back, -look.f_82481_ * back));
    }

    /** {@code to}, or short of the first block between {@code from} and it. */
    public static Vec3 clip(Entity entity, Vec3 from, Vec3 to) {
        HitResult hit = entity.m_9236_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (hit.m_6662_() == HitResult.Type.MISS) {
            return to;
        }
        Vec3 at = hit.m_82450_();
        Vec3 way = to.m_82546_(from);
        double length = way.m_82553_();
        if (length < 1.0E-6) {
            return from;
        }
        double keep = Math.max(0.0, from.m_82554_(at) - MARGIN);
        return from.m_82549_(way.m_82490_(keep / length));
    }
}
