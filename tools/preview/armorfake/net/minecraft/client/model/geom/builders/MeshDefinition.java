package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.PartPose;

/** Preview stand-in: vanilla's MeshDefinition (its root part). */
public class MeshDefinition {
    final PartDefinition root = new PartDefinition(CubeListBuilder.create(), PartPose.ZERO);

    public PartDefinition getRoot() {
        return this.root;
    }
}
