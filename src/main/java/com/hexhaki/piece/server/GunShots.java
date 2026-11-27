package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.PieceSounds;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.gun.GunSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CGunShot;
import com.hexhaki.piece.style.Hit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.8: the guns' balls, flown on the server (no entity: every client flies the same ball from {@link S2CGunShot}, client/gun/
 * GunBullets). The owner: "all weapons only shoot 1 bullet at a time ... when haki is on, bullets come out black (pure black), those
 * bullets are much much faster and go through blocks (10 blocks max)".
 * <ul>
 *   <li>A ball flies {@link GunRules#speed} blocks a tick from the muzzle along the aim, falling as {@link GunRules#drop} says, for
 *       {@link GunRules#RANGE} blocks at most. Each tick the line it flew is tested: the first body it passes (grown by the ball's size,
 *       never its shooter's) is hit and the ball stops in it; a plain ball stops at the first block in its way.</li>
 *   <li>With Armament Haki the ball flies straight and far faster, pure black, and goes through solid blocks, {@link GunRules#PIERCE} of
 *       them at most (each block once, however many ticks it spends in it), tearing each out of its way ({@link #punch}); the next one
 *       stops it, as does a block nothing breaks. It still stops in the first body.</li>
 *   <li>A ball never leaves loaded chunks, and every one is gone when the server stops.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class GunShots {
    private static final List<Ball> ACTIVE = new ArrayList<>();
    private static int nextId = 1;
    /** How finely a Haki ball's line is walked for the blocks it goes through (blocks). */
    static final double WALK = 0.2;

    static final class Ball {
        int id;
        ServerLevel level;
        ServerPlayer owner;
        Vec3 start;
        Vec3 velocity;
        boolean haki;
        int age;
        int pierced;
        final Set<Long> through = new HashSet<>();
    }

    private GunShots() {
    }

    static void fire(ServerPlayer owner, Vec3 from, Vec3 dir, boolean haki) {
        Ball ball = new Ball();
        ball.id = nextId++;
        ball.level = (ServerLevel) owner.m_9236_();
        ball.owner = owner;
        ball.start = from;
        ball.velocity = dir.m_82490_(GunRules.speed(haki));
        ball.haki = haki;
        ACTIVE.add(ball);
        PieceNetwork.around(owner, new S2CGunShot(S2CGunShot.FLY, ball.id, owner.m_19879_(), from.f_82479_, from.f_82480_, from.f_82481_,
            ball.velocity.f_82479_, ball.velocity.f_82480_, ball.velocity.f_82481_, haki));
        // POINT ONE: with Ryou and the Conqueror's Haki, the shot throws the Conqueror's lightning out of the muzzle (visual only)
        ConquerorAura.mixed(owner, from, 1.1, 4, 1.0);
    }

    static Vec3 at(Ball ball, double ticks) {
        return new Vec3(GunRules.at(ball.start.f_82479_, ball.velocity.f_82479_, ticks, ball.haki, false),
            GunRules.at(ball.start.f_82480_, ball.velocity.f_82480_, ticks, ball.haki, true),
            GunRules.at(ball.start.f_82481_, ball.velocity.f_82481_, ticks, ball.haki, false));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Ball> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Ball ball = it.next();
            try {
                if (step(ball)) {
                    it.remove();
                }
            } catch (RuntimeException failure) {
                it.remove(); // a ball never stops the server
            }
        }
    }

    /** One tick of a ball's flight; true when it is done. */
    static boolean step(Ball ball) {
        if (ball.owner.m_213877_() || ball.age > GunRules.life(ball.haki)) {
            stop(ball, at(ball, ball.age), S2CGunShot.AIR);
            return true;
        }
        Vec3 a = at(ball, ball.age);
        Vec3 b = at(ball, ball.age + 1);
        ball.age++;
        // the blocks: a plain ball stops at the first in its way, a Haki ball after PIERCE of them
        Vec3 end = b;
        int struck = S2CGunShot.AIR;
        if (!ball.haki) {
            BlockHitResult block = ball.level.m_45547_(new ClipContext(a, b, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ball.owner));
            if (block.m_6662_() != HitResult.Type.MISS) {
                end = block.m_82450_();
                struck = S2CGunShot.BLOCK;
            }
        } else {
            double length = b.m_82546_(a).m_82553_();
            int samples = Math.max(1, (int) Math.ceil(length / WALK));
            for (int k = 1; k <= samples; k++) {
                Vec3 p = a.m_82549_(b.m_82546_(a).m_82490_(k / (double) samples));
                BlockPos pos = BlockPos.m_274561_(p.f_82479_, p.f_82480_, p.f_82481_);
                if (!ball.level.m_46805_(pos)) {
                    end = p;
                    struck = S2CGunShot.AIR;
                    break;
                }
                long key = pos.m_121878_();
                if (ball.through.contains(key)) {
                    continue;
                }
                BlockState state = ball.level.m_8055_(pos);
                if (state.m_60795_() || state.m_60812_(ball.level, pos).m_83281_()) {
                    continue;
                }
                ball.through.add(key);
                if (!GunRules.through(true, ball.pierced) || state.m_60800_(ball.level, pos) < 0.0F) {
                    end = p;
                    struck = S2CGunShot.BLOCK; // the eleventh block, or one nothing breaks (bedrock)
                    break;
                }
                ball.pierced++;
                punch(ball, pos, state);
            }
        }
        // the first body on the line it flew (up to where the blocks stopped it)
        LivingEntity hit = null;
        Vec3 hitAt = null;
        double best = Double.MAX_VALUE;
        AABB area = new AABB(Math.min(a.f_82479_, end.f_82479_), Math.min(a.f_82480_, end.f_82480_), Math.min(a.f_82481_, end.f_82481_),
            Math.max(a.f_82479_, end.f_82479_), Math.max(a.f_82480_, end.f_82480_), Math.max(a.f_82481_, end.f_82481_)).m_82400_(1.0);
        for (LivingEntity other : ball.level.m_6443_(LivingEntity.class, area,
            e -> e != ball.owner && e.m_6084_() && !e.m_5833_())) {
            Optional<Vec3> on = other.m_20191_().m_82400_(GunRules.BALL).m_82371_(a, end);
            if (on.isPresent()) {
                double d = on.get().m_82554_(a);
                if (d < best) {
                    best = d;
                    hit = other;
                    hitAt = on.get();
                }
            }
        }
        if (hit != null) {
            strike(ball, hit, hitAt);
            stop(ball, hitAt, S2CGunShot.BODY);
            return true;
        }
        if (struck != S2CGunShot.AIR || end != b) {
            stop(ball, end, struck);
            return true;
        }
        return false;
    }

    /**
     * 3.9.3 (the owner: "The pistol bullets with haki on are not going through any blocks ( I asked to go through at least 10 blocks)"):
     * the balls went through, but inside the blocks, unseen. Now each block a Haki ball goes through bursts (its own break, seen and
     * heard) and is torn out of its way as a crater's are (server/Scars: nothing dropped, protected and claimed blocks left, grown back
     * {@link Craters#REPAIR} later; the world's mobGriefing rule obeyed as Craters obeys it): the ball leaves a tunnel.
     */
    static void punch(Ball ball, BlockPos pos, BlockState state) {
        ball.level.m_46796_(2001, pos, net.minecraft.world.level.block.Block.m_49956_(state));
        if (ball.level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_)) {
            Scars scars = Scars.of(ball.level);
            if (scars.room()) {
                scars.tear(ball.owner, pos, java.util.List.of(), Craters.REPAIR);
            }
        }
    }

    static void strike(Ball ball, LivingEntity other, Vec3 at) {
        other.f_19802_ = 0;
        Vec3 dir = ball.velocity.m_82541_();
        if (other.m_6469_(StyleCombat.of(ball.owner), GunRules.damage(ball.haki))) {
            Impact.hit(ball.owner, other, StyleCombat.spec(ball.haki ? Hit.Weight.MEDIUM : Hit.Weight.LIGHT,
                dir.m_82490_(ball.haki ? 0.5 : 0.25).m_82520_(0.0, 0.1, 0.0)));
        }
    }

    static void stop(Ball ball, Vec3 at, int struck) {
        PieceNetwork.around(ball.owner, new S2CGunShot(S2CGunShot.STOP, ball.id, ball.owner.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
            struck, 0.0, 0.0, ball.haki));
        if (struck != S2CGunShot.AIR) {
            PieceSounds.playAt(ball.owner, at.f_82479_, at.f_82480_, at.f_82481_, GunSounds.HIT, ball.haki ? 1.1F : 0.8F, ball.haki ? 0.8F : 1.2F);
            if (ball.haki) {
                // POINT ONE: a Haki ball's impact is its blast: the Conqueror's lightning out of it (with Ryou and the Conqueror's Haki)
                ConquerorAura.mixed(ball.owner, at, 1.6, 5, 1.5);
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        ACTIVE.clear();
    }
}
