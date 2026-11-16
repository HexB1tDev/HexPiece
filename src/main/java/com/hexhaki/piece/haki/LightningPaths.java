package com.hexhaki.piece.haki;

import com.hexhaki.gameplay.ConquerorLightningPath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * 2.1.1: the body of {@code ConquerorLightningPath.generate} (the patch makes the original a straight call here). Used by the server for
 * the charge's contact stuns and by every client for the bolts it draws; the shapes are {@link ConquerorBolts}'.
 */
public final class LightningPaths {
    private LightningPaths() {
    }

    public static List<ConquerorLightningPath.Bolt> generate(float charge, int mastery, long seed, boolean dome) {
        double radius = ConquerorLightningPath.radius(mastery, ConquerorBolts.clampCharge(charge));
        List<double[]> shapes = ConquerorBolts.generate(charge, mastery, seed, dome, radius);
        List<ConquerorLightningPath.Bolt> bolts = new ArrayList<>(shapes.size());
        for (double[] shape : shapes) {
            List<Vec3> points = new ArrayList<>(shape.length / 3);
            for (int i = 0; i + 2 < shape.length; i += 3) {
                points.add(new Vec3(shape[i], shape[i + 1], shape[i + 2]));
            }
            bolts.add(new ConquerorLightningPath.Bolt(points));
        }
        return bolts;
    }
}
