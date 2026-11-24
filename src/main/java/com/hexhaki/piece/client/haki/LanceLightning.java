package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/**
 * 2.9.4 (the owner: "Get rid of this fucking old lighting from everywhere, including Gear 5, and replace it with the new one in
 * Conqueror's Haiku type, the one that has the color of Rio"): Gear 5's storm lance drew its lightning as flat jittered strips of its own
 * (the original's StormLanceVfx.bolt, re-laid every frame). PiecePatch hands every such bolt here instead ({@link #bolt}) and has the
 * lance's own draw ask {@link #draw} first: each becomes the Haki's 3D lightning (haki/Bolt, drawn by HakiBolts as the Conqueror's
 * release is: a black body with a rim in the Ryou colour of the player it is nearest to), re-crackled every other tick so it flickers.
 * The lance's rings stay its own.
 */
public final class LanceLightning {
    record Pending(Vec3 a, Vec3 b, float width, float alpha, long seed) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();
    /** At most this many strokes a frame (the lance asks for some forty). */
    static final int MOST = 96;

    private LanceLightning() {
    }

    /** The lance asked for a bolt from {@code a} to {@code b} (world): kept for this frame's {@link #draw}. */
    public static void bolt(Vec3 a, Vec3 b, float width, float alpha, long seed, int segments, double jitter) {
        if (PENDING.size() < MOST && width > 0.0F && a.m_82557_(b) > 1.0E-4) {
            PENDING.add(new Pending(a, b, width, alpha, seed));
        }
    }

    /** The lance's draw, first: this frame's bolts as 3D lightning in the view ({@code camera}: the eye, world). */
    public static void draw(PoseStack pose, Vec3 camera) {
        if (PENDING.isEmpty()) {
            return;
        }
        try {
            ClientLevel level = Minecraft.m_91087_().f_91073_;
            if (level == null) {
                return;
            }
            Pending first = PENDING.get(0);
            int color = HakiBolts.colorOf(HakiBolts.nearestPlayer(level, first.a.f_82479_, first.a.f_82480_, first.a.f_82481_));
            long flicker = level.m_46467_() / 2;
            List<Bolt> bolts = new ArrayList<>(PENDING.size() * 2);
            for (Pending p : PENDING) {
                double[] a = {p.a.f_82479_ - camera.f_82479_, p.a.f_82480_ - camera.f_82480_, p.a.f_82481_ - camera.f_82481_};
                double[] b = {p.b.f_82479_ - camera.f_82479_, p.b.f_82480_ - camera.f_82480_, p.b.f_82481_ - camera.f_82481_};
                float w = Math.max(0.02F, p.width * 0.6F);
                long seed = p.seed * 31L + flicker;
                // 3.6.0: the one Haki lightning (ConquerorStorm.strike: the release's model, its crooked runs)
                for (Bolt bolt : ConquerorStorm.strike(a, b, w, 0.45F, color, 0.0F, 8, 0.0F, seed)) {
                    bolts.add(bolt.tinted(color));
                }
            }
            double[] ages = new double[bolts.size()];
            java.util.Arrays.fill(ages, 2.0);
            HakiBolts.drawInView(pose, bolts, ages, level, 0.5F);
        } catch (RuntimeException failure) {
            // lightning never stops the game
        } finally {
            PENDING.clear();
        }
    }
}
