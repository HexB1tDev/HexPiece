package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.client.sword.KatanaMesh;
import com.hexhaki.piece.client.sword.KatanaRenderer;
import com.hexhaki.piece.gun.Gun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * 3.8.8: draws the owner's guns and their ball from their own meshes ({@code assets/hexhaki/gun/<name>.hkm}, tools/katana_mesh.py
 * packs them from art/gun) on their one atlas ({@code textures/gun/gun.png}: each material's toon ramp, baked by tools/gun_bake.py),
 * with the game's own entity light, as the katanas are drawn. Frame: model px (the grip's pivot at 0, the barrel forward -z, up +y),
 * scaled by {@link Gun#SCALE}; the full mesh near the eye, its levels of detail further off.
 */
public final class GunRenderer {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/gun/gun.png");
    /** Model px: the trigger (where the finger is: tools/gun_anims.py TRIGGER) and the muzzle's centre (MUZZLE). */
    public static final float[] TRIGGER = {0.0F, -0.3F, -2.8F};
    public static final float[] MUZZLE = {0.0F, 2.2F, -15.1F};
    private static final Map<String, KatanaMesh> MESHES = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();
    static final float FULL_WITHIN = 2.5F;
    static final float MID_WITHIN = 10.0F;

    private GunRenderer() {
    }

    static KatanaMesh mesh(String name) {
        KatanaMesh mesh = MESHES.get(name);
        if (mesh == null && !FAILED.contains(name)) {
            try {
                mesh = KatanaMesh.read("/assets/hexhaki/gun/" + name + ".hkm", name);
                MESHES.put(name, mesh);
            } catch (Exception failure) {
                FAILED.add(name);
                HexPiece.LOGGER.error("HexPiece could not read the gun mesh {}", name, failure);
            }
        }
        return mesh;
    }

    static String lod(PoseStack.Pose pose) {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F);
        pose.m_252922_().transformPosition(origin);
        float distance = (float) Math.sqrt(origin.x * origin.x + origin.y * origin.y + origin.z * origin.z);
        return distance < FULL_WITHIN ? "" : distance < MID_WITHIN ? "_mid" : "_far";
    }

    /** The gun in the pose's frame (model px, scaled here by {@link Gun#SCALE}). */
    public static void draw(PoseStack pose, MultiBufferSource buffers, Gun gun, int light) {
        pose.m_85836_();
        pose.m_85841_(Gun.SCALE, Gun.SCALE, Gun.SCALE);
        PoseStack.Pose last = pose.m_85850_();
        KatanaMesh mesh = mesh(gun.key() + lod(last));
        if (mesh != null) {
            VertexConsumer buffer = buffers.m_6299_(RenderType.m_110458_(TEXTURE));
            KatanaRenderer.quads(last, buffer, mesh, 0, mesh.quadCount(), light);
        }
        pose.m_85849_();
    }

    /** The ball (model px, its centre at 0) at {@code scale} blocks a px; pure black with Haki (the owner: "bullets come out black"). */
    public static void ball(PoseStack pose, MultiBufferSource buffers, int light, float scale, boolean haki) {
        KatanaMesh mesh = mesh(Gun.BULLET);
        if (mesh == null) {
            return;
        }
        pose.m_85836_();
        pose.m_85841_(scale, scale, scale);
        VertexConsumer buffer = buffers.m_6299_(RenderType.m_110458_(TEXTURE));
        if (haki) {
            KatanaRenderer.tinted(pose.m_85850_(), buffer, mesh, light, 0.0F, 0.0F, 0.0F);
        } else {
            KatanaRenderer.quads(pose.m_85850_(), buffer, mesh, 0, mesh.quadCount(), light);
        }
        pose.m_85849_();
    }
}
