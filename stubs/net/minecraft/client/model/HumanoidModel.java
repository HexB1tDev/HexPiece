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

    /** 2.1.4: fbs$a (intermediary class_572$class_573); constants keep their names. */
    public enum ArmPose { EMPTY, ITEM }
}
