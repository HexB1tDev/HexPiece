package com.hexhaki.piece.client.gear;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.client.render.Bulk;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaSkin;
import com.hexhaki.piece.horo.AstralArt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.0: Dawn Pistol's fist through the body, drawn (the owner: "the fist goes through the body, stretching the skin to insane amounts";
 * gear/NikaSkin): from the POW (client/gear/NikaPow), every client stretches the struck body's own skin (its own texture, the region of
 * its chest where it is built like a person) out of its far side along the blow to the fist, lit where it stands, through the game's
 * translucent entity type in a batch of its own; then the heat and the white-hot fist over it through debugQuads (alpha-blended, depth
 * tested: the owner's phone). The body is held while it stretches (server/NikaPistol.Pin), then launched: the skin snaps back into it as it
 * flies. Not one's own skin in one's own first person. At most {@link #MOST} at once.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaSkinClient {
    static final int MOST = 8;
    static final double FAR = 96.0;

    static final class Through {
        int id;
        float wx;
        float wy;
        float wz;
        double born;
    }

    static final List<Through> THROUGH = new ArrayList<>();

    private NikaSkinClient() {
    }

    /** From client/gear/NikaPow: Dawn Pistol has struck body {@code id} along the unit way (wx, wy, wz). */
    public static void start(int id, float wx, float wy, float wz, double born) {
        for (int i = THROUGH.size() - 1; i >= 0; i--) {
            if (THROUGH.get(i).id == id) {
                THROUGH.remove(i);
            }
        }
        if (THROUGH.size() >= MOST) {
            THROUGH.remove(0);
        }
        Through t = new Through();
        t.id = id;
        double l = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (l < 1.0E-6) {
            return;
        }
        t.wx = (float) (wx / l);
        t.wy = (float) (wy / l);
        t.wz = (float) (wz / l);
        t.born = born;
        THROUGH.add(t);
    }

    /** The skin's writer: the entity type's full vertex (the texture's uv, the light where the body stands, its faces' normals). */
    static final class SkinWriter implements AstralArt.Skin {
        final float[] m = new float[12];
        final float[] n = new float[9];
        VertexConsumer out;
        int light;

        @Override
        public void quad(double[] xyz, float[] uv, int argb, double nx, double ny, double nz) {
            float r = ((argb >>> 16) & 255) / 255.0F;
            float g = ((argb >>> 8) & 255) / 255.0F;
            float b = (argb & 255) / 255.0F;
            float a = ((argb >>> 24) & 255) / 255.0F;
            for (int k = 0; k < 4; k++) {
                Bulk.vertex(this.out, this.m, this.n, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2], r, g, b, a, uv[k * 2],
                    uv[k * 2 + 1], OverlayTexture.f_118083_, this.light, (float) nx, (float) ny, (float) nz);
            }
        }
    }

    static final class Writer implements FireMesh.Sink {
        VertexConsumer buffer;
        org.joml.Matrix4f matrix;

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                int c = argb[k];
                this.buffer.m_252986_(this.matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F).m_5752_();
            }
        }
    }

    static final SkinWriter SKIN = new SkinWriter();
    static final Writer HEAT = new Writer();
    /** The skin's region in its texture: u0, u1, v0, v1 (a person's chest; a 64 x 32 skeleton's; the middle of anything else). */
    static final float[] REGION = new float[4];

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || THROUGH.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            THROUGH.clear();
            return;
        }
        float partial = event.getPartialTick();
        double now = level.m_46467_() + partial;
        THROUGH.removeIf(t -> now - t.born > NikaSkin.life() || now < t.born - 40);
        if (THROUGH.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        Vec3 cam = event.getCamera().m_90583_();
        boolean first = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        try {
            // the skins first, each texture its own batch ended before the next (and before the heat's)
            Bulk.read(pose.m_85850_(), SKIN.m, SKIN.n);
            for (Through t : THROUGH) {
                Entity e = level.m_6815_(t.id);
                if (!(e instanceof LivingEntity body) || first && body == minecraft.f_91074_) {
                    continue;
                }
                double age = now - t.born;
                double len = NikaSkin.LENGTH * NikaSkin.out(age);
                if (len < 0.05) {
                    continue;
                }
                ResourceLocation skin = skin(minecraft, body);
                if (skin == null) {
                    continue;
                }
                double x = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial;
                double y = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial + body.m_20206_() * 0.55;
                double z = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial;
                if ((x - cam.f_82479_) * (x - cam.f_82479_) + (z - cam.f_82481_) * (z - cam.f_82481_) > FAR * FAR) {
                    continue;
                }
                RenderType type = RenderType.m_110473_(skin);
                SKIN.out = buffers.m_6299_(type);
                SKIN.light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(x, y, z));
                NikaSkin.skin(SKIN, x - cam.f_82479_, y - cam.f_82480_, z - cam.f_82481_, t.wx, t.wy, t.wz, len, radius(body), NikaSkin.BULB,
                    REGION[0], REGION[1], REGION[2], REGION[3], 0xFFFFFFFF, age);
                buffers.m_109912_(type);
                SKIN.out = null;
            }
            // the heat and the fist, in one batch
            RenderType heat = RenderType.m_269166_();
            HEAT.matrix = pose.m_85850_().m_252922_();
            HEAT.buffer = buffers.m_6299_(heat);
            FireMesh.quality = Math.max(0.35, PieceFx.density());
            for (Through t : THROUGH) {
                Entity e = level.m_6815_(t.id);
                if (!(e instanceof LivingEntity body) || first && body == minecraft.f_91074_) {
                    continue;
                }
                double age = now - t.born;
                double out = NikaSkin.out(age);
                double x = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial;
                double y = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial + body.m_20206_() * 0.55;
                double z = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial;
                NikaSkin.heat(HEAT, x - cam.f_82479_, y - cam.f_82480_, z - cam.f_82481_, t.wx, t.wy, t.wz, NikaSkin.LENGTH * out, radius(body),
                    NikaSkin.BULB, Math.min(1.0, out * 1.2), age);
            }
            buffers.m_109912_(heat);
            HEAT.buffer = null;
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's stretched skin could not be drawn", failure);
            THROUGH.clear();
            SKIN.out = null;
            HEAT.buffer = null;
        }
    }

    /** How round the skin is where it leaves the body (blocks). */
    static double radius(LivingEntity body) {
        return Math.max(0.3, Math.min(1.2, body.m_20205_() * 0.7));
    }

    /** The body's own texture, and {@link #REGION} set to the part of it stretched out; null if it has none to give. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static ResourceLocation skin(Minecraft minecraft, LivingEntity body) {
        if (body instanceof AbstractClientPlayer player) {
            region(20, 28, 20, 32, 64, 64);
            return player.m_108560_();
        }
        EntityRenderer renderer = minecraft.m_91290_().m_114382_(body);
        if (renderer == null) {
            return null;
        }
        ResourceLocation skin = renderer.m_5478_(body);
        if (skin == null) {
            return null;
        }
        if (renderer instanceof LivingEntityRenderer living && living.m_7200_() instanceof HumanoidModel<?>) {
            String path = skin.m_135815_();
            if (path.contains("skeleton") || path.contains("stray")) {
                region(20, 28, 20, 32, 64, 32);
            } else {
                region(20, 28, 20, 32, 64, 64);
            }
        } else {
            region(16, 32, 16, 32, 64, 64);
        }
        return skin;
    }

    static void region(int x0, int x1, int y0, int y1, int w, int h) {
        REGION[0] = x0 / (float) w;
        REGION[1] = x1 / (float) w;
        REGION[2] = y0 / (float) h;
        REGION[3] = y1 / (float) h;
    }
}
