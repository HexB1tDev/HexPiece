package net.minecraft.client.model.geom.builders;

import java.util.ArrayList;
import java.util.List;

/** Preview stand-in: vanilla's CubeListBuilder (a texture offset, then boxes at it). */
public class CubeListBuilder {
    public final List<float[]> boxes = new ArrayList<>();
    private int u;
    private int v;

    public static CubeListBuilder create() {
        return new CubeListBuilder();
    }

    public CubeListBuilder texOffs(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public CubeListBuilder addBox(float x, float y, float z, float w, float h, float d, CubeDeformation grow) {
        this.boxes.add(new float[] {this.u, this.v, x, y, z, w, h, d, grow.grow});
        return this;
    }
}
