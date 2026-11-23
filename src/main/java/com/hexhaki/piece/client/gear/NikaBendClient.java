package com.hexhaki.piece.client.gear;

import com.hexhaki.network.msg.S2CRubberBend;
import com.hexhaki.piece.gear.NikaBend;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * 3.2.9: a body Star Gun strikes, drawn as rubber (gear/NikaBend). patch/PiecePatch.starRubber makes the original's
 * {@code RubberBendStates.accept} tell {@link #accept} first (when, how hard, which way, how long held and let go), and its
 * {@code RubberBendDeformer.applyBend} (round every living body's and player's drawing) ask {@link #apply} instead: the same contract
 * (push a pose and say so, or neither).
 */
public final class NikaBendClient {
    static final class Hit {
        long start;
        double hold;
        double release;
        double strength;
        float yaw;
        /** 3.3.9: Dawn Pistol's pull (gear/NikaBend.dawn), not Star Gun's bend. */
        boolean dawn;
    }

    static final Map<Integer, Hit> HITS = new HashMap<>();
    private static final double[] SHAPE = new double[5];
    private static final Matrix4f SHEAR = new Matrix4f();
    private static final Quaternionf TURN = new Quaternionf();

    private NikaBendClient() {
    }

    /** At the head of RubberBendStates.accept. */
    public static void accept(S2CRubberBend message) {
        long now = System.currentTimeMillis();
        prune(now);
        Hit hit = new Hit();
        hit.start = now;
        hit.hold = Math.max(0, message.holdTicks());
        hit.release = Math.max(1, message.releaseTicks());
        hit.strength = message.strength();
        hit.yaw = message.yaw();
        HITS.put(message.entityId(), hit);
    }

    /** 3.3.9: Dawn Pistol has struck body {@code id} ({@code strength} 0.4 to 1, the blow's way {@code yaw}): pulled out like rubber. */
    public static void dawn(int id, float strength, float yaw) {
        long now = System.currentTimeMillis();
        prune(now);
        Hit hit = new Hit();
        hit.start = now;
        hit.strength = strength;
        hit.yaw = yaw;
        hit.dawn = true;
        HITS.put(id, hit);
    }

    static void prune(long now) {
        Iterator<Hit> it = HITS.values().iterator();
        while (it.hasNext()) {
            Hit h = it.next();
            if ((now - h.start) / 50.0 > (h.dawn ? NikaBend.dawnLength() : NikaBend.length(h.hold, h.release))) {
                it.remove();
            }
        }
    }

    /** In place of RubberBendDeformer.applyBend: pushes the rubber shape round {@code entity}'s drawing and says so, or does nothing. */
    public static boolean apply(PoseStack pose, LivingEntity entity) {
        Hit hit = HITS.get(entity.m_19879_());
        if (hit == null) {
            return false;
        }
        double t = (System.currentTimeMillis() - hit.start) / 50.0;
        if (!(hit.dawn ? NikaBend.dawn(t, hit.strength, SHAPE) : NikaBend.shape(t, hit.hold, hit.release, hit.strength, SHAPE))) {
            HITS.remove(entity.m_19879_());
            return false;
        }
        pose.m_85836_();
        pose.m_252781_(TURN.rotationY(hit.yaw));
        pose.m_85837_(0, 0, SHAPE[NikaBend.BACK]);
        pose.m_85850_().m_252922_().mul(SHEAR.m12((float) SHAPE[NikaBend.SHEAR])); // only m12 ever set: the rest identity
        pose.m_85841_((float) SHAPE[NikaBend.ACROSS], (float) SHAPE[NikaBend.UP], (float) SHAPE[NikaBend.ALONG]);
        pose.m_252781_(TURN.rotationY(-hit.yaw));
        return true;
    }
}
