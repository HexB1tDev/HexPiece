// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.model;

public abstract class EntityModel<T extends net.minecraft.world.entity.Entity> extends Model {
    /** attackTime (2.3.1: the armor copies the pose; fbf.c, yarn handSwingProgress) */ public float f_102608_;
    /** riding (2.3.1; fbf.d) */ public boolean f_102609_;
    /** young (1.3.15: a child's model draws its own little-one proportions instead) */ public boolean f_102610_;
    /** renderToBuffer (declared on Model) */ public void m_7695_(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer consumer,
        int light, int overlay, float red, float green, float blue, float alpha) { throw new UnsupportedOperationException(); }
}
