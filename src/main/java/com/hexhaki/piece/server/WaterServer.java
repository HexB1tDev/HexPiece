package com.hexhaki.piece.server;

import net.minecraft.world.phys.Vec3;

/**
 * 2.3.8: Fishman Karate's water in the world. 3.5.7 (the owner: "Remove FIshman karate from the game entirely"): its Water Bullet is gone
 * with it; what is left is the line test the air bullets, the Uo fruit's breath and the clashes share.
 */
public final class WaterServer {
    private WaterServer() {
    }

    /** Where along from..to (0..1) the point nearest {@code p} lies. */
    static double closest(Vec3 from, Vec3 to, Vec3 p) {
        Vec3 d = to.m_82546_(from);
        Vec3 q = p.m_82546_(from);
        double l = d.f_82479_ * d.f_82479_ + d.f_82480_ * d.f_82480_ + d.f_82481_ * d.f_82481_;
        return l < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, (q.f_82479_ * d.f_82479_ + q.f_82480_ * d.f_82480_ + q.f_82481_ * d.f_82481_) / l));
    }
}
