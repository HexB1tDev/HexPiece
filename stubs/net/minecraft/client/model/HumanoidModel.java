// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.model;

public class HumanoidModel<T extends net.minecraft.world.entity.LivingEntity> extends EntityModel<T> {
    /** head */ public net.minecraft.client.model.geom.ModelPart f_102808_;
    /** hat: the skin's outer layer of the head */ public net.minecraft.client.model.geom.ModelPart f_102809_;
    /** body (the torso) */ public net.minecraft.client.model.geom.ModelPart f_102810_;
    /** rightArm */ public net.minecraft.client.model.geom.ModelPart f_102811_;
    /** leftArm */ public net.minecraft.client.model.geom.ModelPart f_102812_;
    /** rightLeg */ public net.minecraft.client.model.geom.ModelPart f_102813_;
    /** leftLeg */ public net.minecraft.client.model.geom.ModelPart f_102814_;

    /** leftArmPose (2.3.1; fbs.r, yarn field_3399 leftArmPose) */ public ArmPose f_102815_;
    /** rightArmPose (2.3.1; fbs.s, yarn field_3395) */ public ArmPose f_102816_;
    /** crouching (2.3.1; fbs.t, yarn field_3400 sneaking) */ public boolean f_102817_;

    protected HumanoidModel() {
    }

    /** 2.3.1: HumanoidModel(ModelPart root) (fbs.<init>(Lfee;)V), as the armor model is built */
    public HumanoidModel(net.minecraft.client.model.geom.ModelPart root) {
    }

    /** setAllVisible (2.3.1; fbs.d_(Z)V m_8009_, yarn setVisible) */ public void m_8009_(boolean visible) { }

    /** 2.1.4: fbs$a (intermediary class_572$class_573); constants keep their names. */
    public enum ArmPose { EMPTY, ITEM }
}
