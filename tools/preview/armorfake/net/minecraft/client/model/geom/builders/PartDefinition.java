package net.minecraft.client.model.geom.builders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

/** Preview stand-in: vanilla's PartDefinition (its cubes, its pose, its children), baked into a ModelPart. */
public class PartDefinition {
    final CubeListBuilder cubes;
    final PartPose pose;
    final Map<String, PartDefinition> children = new LinkedHashMap<>();

    PartDefinition(CubeListBuilder cubes, PartPose pose) {
        this.cubes = cubes;
        this.pose = pose;
    }

    public PartDefinition addOrReplaceChild(String name, CubeListBuilder cubes, PartPose pose) {
        PartDefinition child = new PartDefinition(cubes, pose);
        this.children.put(name, child);
        return child;
    }

    ModelPart bake(int texW, int texH) {
        List<ModelPart.Cube> baked = new ArrayList<>();
        for (float[] b : this.cubes.boxes) {
            baked.add(new ModelPart.Cube((int) b[0], (int) b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], texW, texH));
        }
        Map<String, ModelPart> kids = new LinkedHashMap<>();
        this.children.forEach((name, child) -> kids.put(name, child.bake(texW, texH)));
        ModelPart part = new ModelPart(baked, kids);
        part.loadPose(this.pose);
        return part;
    }
}
