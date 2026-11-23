package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RemoteGearStates;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.4.9 (the owner: "the camera in the giant form should need be adjusted to adapt to its new size"): the third-person camera of a
 * Gear 5 giant (Gigante, drawn 4 times the body: the original's GearBodyDeformer) sits as far behind its eye as the game's own camera
 * sits behind a normal player's, times the giant's size: the vanilla 4 blocks become 16 behind the giant's own eye (which is 4 times as
 * high), so the whole giant is seen as a normal player is. It grows and shrinks with the body (RemoteGearStates' scale, interpolated) and
 * stops short of any block between the eye and it (client/dragon/DragonCamera.clip). First person keeps the original's own lift of the
 * eye (GearBodyDeformer.eyeLift). Asked by client/dragon/DragonCamera.position (the original's CameraMixin's fixed place).
 */
public final class GiantCamera {
    /** The game's own third-person distance behind the eye (blocks). */
    static final double VANILLA_BACK = 4.0;
    /** A body drawn at least this many times its size has the giant's camera. */
    static final float LEAST = 1.02F;

    private GiantCamera() {
    }

    /** The giant's camera place, or null (not a giant, first person: the game's own camera and the original's eye lift). */
    public static Vec3 position(Entity entity, float partial) {
        if (!(entity instanceof AbstractClientPlayer player)) {
            return null;
        }
        RemoteGearStates.State state = RemoteGearStates.get(player.m_19879_());
        if (state == null) {
            return null;
        }
        float scale = Math.max(0.2F, Math.min(6.0F, state.scale(partial)));
        if (scale < LEAST) {
            return null;
        }
        CameraType type = Minecraft.m_91087_().f_91066_.m_92176_();
        if (type == CameraType.FIRST_PERSON) {
            return null;
        }
        Vec3 feet = player.m_20318_(partial);
        Vec3 eye = player.m_20299_(partial);
        // the giant's own eye: the body is drawn scaled up round its feet
        Vec3 high = new Vec3(eye.f_82479_, feet.f_82480_ + (eye.f_82480_ - feet.f_82480_) * scale, eye.f_82481_);
        Vec3 look = player.m_20252_(partial);
        if (type == CameraType.THIRD_PERSON_FRONT) {
            look = look.m_82490_(-1.0);
        }
        double back = VANILLA_BACK * scale;
        Vec3 raised = com.hexhaki.piece.client.dragon.DragonCamera.clip(player, eye, high);
        return com.hexhaki.piece.client.dragon.DragonCamera.clip(player, raised,
            raised.m_82520_(-look.f_82479_ * back, -look.f_82480_ * back, -look.f_82481_ * back));
    }
}
