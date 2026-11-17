package net.minecraft.client.model.geom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Preview stand-in: vanilla's ModelPart (pivot, turn, visibility, cubes, children), what the preview walks to draw it. */
public final class ModelPart {
    /** A cube: its texture offset, corner and size (model pixels), how much it is grown on every side, the texture's size. */
    public record Cube(int u, int v, float x, float y, float z, float w, float h, float d, float grow, int texW, int texH) {
    }

    public float x, y, z, xRot, yRot, zRot;
    public boolean visible = true;
    public final List<Cube> cubes;
    public final Map<String, ModelPart> children;

    public ModelPart(List<Cube> cubes, Map<String, ModelPart> children) {
        this.cubes = cubes;
        this.children = new LinkedHashMap<>(children);
    }

    public ModelPart getChild(String name) {
        ModelPart part = this.children.get(name);
        if (part == null) {
            throw new java.util.NoSuchElementException("Can't find part " + name);
        }
        return part;
    }

    public void copyFrom(ModelPart other) {
        this.xRot = other.xRot; this.yRot = other.yRot; this.zRot = other.zRot;
        this.x = other.x; this.y = other.y; this.z = other.z;
    }

    public void loadPose(PartPose pose) {
        this.x = pose.x; this.y = pose.y; this.z = pose.z;
        this.xRot = pose.xRot; this.yRot = pose.yRot; this.zRot = pose.zRot;
    }
}
