package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;

/** Preview stand-in: vanilla's HumanoidModel (its seven parts from the root, the pose state the armor copies). */
public class HumanoidModel<T extends net.minecraft.world.entity.LivingEntity> {
    public enum ArmPose { EMPTY, ITEM }

    public final ModelPart root;
    public final ModelPart head, hat, body, rightArm, leftArm, rightLeg, leftLeg;
    public float attackTime;
    public boolean riding;
    public boolean young;
    public boolean crouching;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;

    public HumanoidModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public void setAllVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.body.visible = visible;
        this.rightArm.visible = visible;
        this.leftArm.visible = visible;
        this.rightLeg.visible = visible;
        this.leftLeg.visible = visible;
    }
}
