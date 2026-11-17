package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.ModelPart;

/** Preview stand-in: vanilla's LayerDefinition (a mesh and its texture's size), baked. */
public class LayerDefinition {
    final MeshDefinition mesh;
    final int texW;
    final int texH;

    LayerDefinition(MeshDefinition mesh, int texW, int texH) {
        this.mesh = mesh;
        this.texW = texW;
        this.texH = texH;
    }

    public static LayerDefinition create(MeshDefinition mesh, int texW, int texH) {
        return new LayerDefinition(mesh, texW, texH);
    }

    public ModelPart bakeRoot() {
        return this.mesh.root.bake(this.texW, this.texH);
    }
}
