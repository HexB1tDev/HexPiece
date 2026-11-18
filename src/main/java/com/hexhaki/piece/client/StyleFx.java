package com.hexhaki.piece.client;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.network.S2CStyleFx;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Motion;
import com.hexhaki.piece.style.StyleMove;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 2.1.2 (the owner: "since we use photon, add water particles to fish man, air particles for swings of moves and air blade"): what a
 * fighting-style move looks like besides its animation and the original's impact (hit-stop, shake, flash), all on Photon
 * ({@link PieceFx}), no vanilla particles:
 * <ul>
 *   <li>every blow thrown cuts the air where it goes, hit or miss: a crescent, streaks flying on along the blow, a ring of air and the
 *       ground's dust for the heavy ones; a spin throws its streaks all round;</li>
 *   <li>a blow that lands bursts in sparks;</li>
 *   <li>Fishman Karate is water: droplets off every blow, a splash and mist where it lands, bullets of water that fly and burst, the
 *       counter's stance a ring of water turning round the body, the ground punch a wave;</li>
 *   <li>the Air Blade is a crescent of cut air flying the length of the shot, trailing streaks, bursting where it ends;</li>
 *   <li>the Flame Leg burns while it spins, Shave and the Shoulder Charge kick up dust as they go, Iron Body throws off steel sparks.</li>
 * </ul>
 */
public final class StyleFx {
    static final int WHITE = 0xC8FFFFFF;
    static final int WATER = 0xE8C4ECFF;
    static final int WATER_DEEP = 0xE07CC4F0;
    static final int DUST = 0x70D8CFC0;
    static final Random RANDOM = new Random();

    private StyleFx() {
    }

    public static void accept(S2CStyleFx message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        StyleMove move = StyleMove.byOrdinal(message.move());
        if (level == null || move == null || !PieceFx.near(new Vec3(message.x(), message.y(), message.z()), 72.0)) {
            return;
        }
        switch (message.stage()) {
            case 0 -> {
                if (move.effect() == StyleMove.Effect.BULLETS) {
                    // 2.3.8: the Water Bullet's drops gather over the fist from the move's start
                    com.hexhaki.piece.client.fx.WaterFx.gather(message.entity(), level.m_46467_());
                }
                started(level, move, message);
            }
            case 1 -> landed(move, new Vec3(message.x(), message.y(), message.z()), flat(message.dx(), message.dz()));
            case 2 -> shot(move, new Vec3(message.dx(), message.dy(), message.dz()), new Vec3(message.x(), message.y(), message.z()));
            case 3 -> swing(move, new Vec3(message.x(), message.y(), message.z()), flat(message.dx(), message.dz()), (int) Math.round(message.dy()));
            default -> {
            }
        }
    }

    static Vec3 flat(double x, double z) {
        double length = Math.sqrt(x * x + z * z);
        return length < 1.0E-6 ? new Vec3(0, 0, 1) : new Vec3(x / length, 0, z / length);
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    static boolean water(StyleMove move) {
        return move.style() == FightingStyle.FISHMAN_KARATE;
    }

    /** How high a move's blows travel (blocks above the feet): kicks low or high, punches at the chest, ground strikes at the ground. */
    static double height(StyleMove move) {
        return switch (move) {
            case BL_SWEEP -> 0.35;
            case BL_SKY -> 0.15;
            case BL_FLAME -> 0.6;
            case BL_SNAP, FK_KNEE -> 1.0;
            case BL_HOOK, BL_MUTTON -> 1.2;
            case BL_NECK, SP_STORM, SP_BLADE, IF_UPPER -> 1.55;
            default -> 1.35;
        };
    }

    // ------------------------------------------------------------------------------------------------------------- air

    /** A blow thrown ({@code code}: its weight 0..3, + 10 for a blow all round). */
    static void swing(StyleMove move, Vec3 feet, Vec3 facing, int code) {
        int weight = Math.floorMod(code, 10);
        boolean round = code / 10 % 2 == 1;
        double h = height(move);
        if (move.style() == FightingStyle.SIX_POWERS) {
            // 2.4.1: Six Powers' air in 3D, faster (client/fx/AirFx)
            com.hexhaki.piece.client.fx.AirFx.swing(move, feet, facing, h, weight, round);
            if (weight >= 2) {
                dust(feet, round ? null : facing, 8 + weight * 3);
            }
            return;
        }
        Vec3 side = new Vec3(-facing.f_82481_, 0, facing.f_82479_);
        if (round) {
            int n = PieceFx.count(14 + weight * 4);
            for (int i = 0; i < n; i++) {
                double a = Math.PI * 2 * i / n;
                Vec3 out = new Vec3(Math.cos(a), 0, Math.sin(a));
                Vec3 tangent = new Vec3(-Math.sin(a), 0, Math.cos(a));
                PieceFx.at(PieceFx.STREAK, false, 6, feet.m_82549_(out.m_82490_(1.3)).m_82520_(0, h + r(0.15), 0))
                    .velocity(tangent.m_82490_(0.45).m_82549_(out.m_82490_(0.12))).colors(WHITE, 0x00FFFFFF).size(0.38, 0.08).drag(0.84)
                    .bloom(0).spawn();
            }
            PieceFx.at(PieceFx.RING, false, 7, feet.m_82520_(0, h, 0)).colors(0x70FFFFFF, 0x00FFFFFF).size(0.6, 3.6 + weight * 0.5).bloom(0)
                .spawn();
        } else {
            Vec3 point = feet.m_82549_(facing.m_82490_(1.5)).m_82520_(0, h, 0);
            PieceFx.at(PieceFx.SLASH, false, 5, point).velocity(facing.m_82490_(0.18)).colors(0xA8FFFFFF, 0x00FFFFFF)
                .size(0.8F + 0.35F * weight, 1.7F + 0.5F * weight).drag(0.8F).bloom(0).spawn();
            int n = PieceFx.count(6 + weight * 3);
            for (int i = 0; i < n; i++) {
                Vec3 velocity = facing.m_82490_(0.5 + RANDOM.nextDouble() * 0.45).m_82549_(side.m_82490_(r(0.2))).m_82520_(0, r(0.12) + 0.02, 0);
                PieceFx.at(PieceFx.STREAK, false, 4 + RANDOM.nextInt(3), point.m_82520_(r(0.3), r(0.3), r(0.3))).velocity(velocity)
                    .colors(WHITE, 0x00FFFFFF).size(0.32, 0.05).drag(0.82).bloom(0).spawn();
            }
            if (weight >= 2) {
                PieceFx.at(PieceFx.RING, false, 6, point).colors(0x78FFFFFF, 0x00FFFFFF).size(0.3, 2.2F + 0.4F * weight).bloom(0).spawn();
            }
        }
        if (weight >= 2 || move.effect() == StyleMove.Effect.QUAKE) {
            dust(feet, round ? null : facing, 8 + weight * 3);
        }
        if (water(move)) {
            Vec3 from = round ? feet.m_82520_(0, h, 0) : feet.m_82549_(facing.m_82490_(1.2)).m_82520_(0, h, 0);
            droplets(from, round ? null : facing, 8 + weight * 5, 0.45 + weight * 0.08);
        }
        if (move.effect() == StyleMove.Effect.FIRE) {
            flames(feet.m_82520_(0, h, 0), 1.3, 14);
        }
    }

    /** Dust kicked up off the ground round {@code feet} (along {@code facing} if given). */
    static void dust(Vec3 feet, Vec3 facing, int wanted) {
        int n = PieceFx.count(wanted);
        for (int i = 0; i < n; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            Vec3 out = new Vec3(Math.cos(a), 0, Math.sin(a));
            if (facing != null && out.f_82479_ * facing.f_82479_ + out.f_82481_ * facing.f_82481_ < -0.2) {
                out = out.m_82490_(-1);
            }
            PieceFx.at(PieceFx.PUFF, false, 14 + RANDOM.nextInt(8), feet.m_82549_(out.m_82490_(0.5)).m_82520_(0, 0.1, 0))
                .velocity(out.m_82490_(0.18 + RANDOM.nextDouble() * 0.12).m_82520_(0, 0.03, 0)).colors(DUST, 0x00D8CFC0).size(0.35, 1.1)
                .drag(0.88).spawn();
        }
    }

    // ------------------------------------------------------------------------------------------------------------- hits

    static void landed(StyleMove move, Vec3 at, Vec3 facing) {
        int weight = 0;
        for (var hit : move.hits()) {
            weight = Math.max(weight, hit.weight().ordinal());
        }
        int n = PieceFx.count(6 + weight * 3);
        for (int i = 0; i < n; i++) {
            Vec3 velocity = new Vec3(r(1), r(0.8) + 0.2, r(1)).m_82541_().m_82490_(0.25 + RANDOM.nextDouble() * 0.25).m_82549_(facing.m_82490_(0.15));
            PieceFx.at(PieceFx.SPARK, true, 6 + RANDOM.nextInt(4), at).velocity(velocity).colors(0xFFFFF4D0, 0x00FF9040).size(0.2, 0.04)
                .drag(0.85).bloom(0).spawn();
        }
        PieceFx.at(PieceFx.RING, true, 5, at).colors(0x90FFF0D0, 0x00FFFFFF).size(0.2, 1.3F + 0.3F * weight).bloom(0).spawn();
        if (water(move)) {
            splash(at, facing, 1.0 + weight * 0.25);
        }
        if (move.effect() == StyleMove.Effect.FIRE) {
            flames(at, 0.4, 12);
        }
    }

    // ------------------------------------------------------------------------------------------------------------- water

    /** Water droplets thrown from {@code at}, along {@code facing} (null: all round), arcing down. */
    static void droplets(Vec3 at, Vec3 facing, int wanted, double speed) {
        int n = PieceFx.count(wanted);
        for (int i = 0; i < n; i++) {
            Vec3 direction = facing == null ? new Vec3(r(1), 0, r(1)) : facing.m_82549_(new Vec3(r(0.45), 0, r(0.45)));
            Vec3 velocity = direction.m_82541_().m_82490_(speed * (0.5 + RANDOM.nextDouble() * 0.6)).m_82520_(0, 0.08 + RANDOM.nextDouble() * 0.22, 0);
            PieceFx.at(PieceFx.DROP, false, 16 + RANDOM.nextInt(8), at.m_82520_(r(0.2), r(0.2), r(0.2))).velocity(velocity)
                .colors(WATER, 0x30C4ECFF).size(0.17F, 0.08F).fall(0.045F).drag(0.97F).bloom(0).spawn();
        }
    }

    /** A splash where water strikes: droplets every way, a blue ring, a cloud of spray. */
    static void splash(Vec3 at, Vec3 facing, double strength) {
        droplets(at, null, (int) (14 * strength), 0.35 * strength);
        droplets(at, facing, (int) (8 * strength), 0.5 * strength);
        PieceFx.at(PieceFx.RING, false, 8, at).colors(0xB0A8DCFF, 0x00A8DCFF).size(0.3, 1.8F * (float) strength).bloom(0).spawn();
        int n = PieceFx.count((float) (6 * strength));
        for (int i = 0; i < n; i++) {
            PieceFx.at(PieceFx.STEAM, false, 18 + RANDOM.nextInt(10), at.m_82520_(r(0.3), r(0.3), r(0.3)))
                .velocity(new Vec3(r(0.06), 0.02 + RANDOM.nextDouble() * 0.04, r(0.06))).colors(0x70E4F6FF, 0x00E4F6FF).size(0.5, 1.4)
                .drag(0.92).spawn();
        }
    }

    // ------------------------------------------------------------------------------------------------------------- shots

    /** A shot along {@code from} to {@code to}: the Air Blade's crescent, or a water bullet. */
    static void shot(StyleMove move, Vec3 from, Vec3 to) {
        if (move.style() == FightingStyle.SIX_POWERS) {
            com.hexhaki.piece.client.fx.AirFx.blade(from, to); // 2.4.1: the Air Blade, a crescent of air in 3D, twice as fast
            return;
        }
        Vec3 path = to.m_82546_(from);
        double distance = Math.max(0.5, path.m_82553_());
        Vec3 direction = path.m_82490_(1.0 / distance);
        boolean water = water(move);
        double speed = water ? 2.4 : 2.2;
        int life = (int) Math.ceil(distance / speed);
        Vec3 start = from.m_82549_(direction.m_82490_(1.0));
        if (water) {
            PieceFx.at(PieceFx.DROP, false, life, start).velocity(direction.m_82490_(speed)).colors(0xFFDDF4FF, 0xE0BDE6FF).size(0.36, 0.3)
                .drag(1.0F).bloom(0).spawn();
        } else {
            PieceFx.at(PieceFx.SLASH, true, life, start).velocity(direction.m_82490_(speed)).colors(0xE0EEF6FF, 0x80FFFFFF).size(1.8, 2.6)
                .drag(1.0F).bloom(0).spawn();
            for (int ghost = 1; ghost <= 2; ghost++) {
                int g = ghost;
                PieceFx.later(g, () -> PieceFx.at(PieceFx.SLASH, false, Math.max(1, life - g), start).velocity(direction.m_82490_(speed))
                    .colors(0x60FFFFFF, 0x00FFFFFF).size(1.6F - 0.25F * g, 2.0F).drag(1.0F).bloom(0).spawn());
            }
        }
        for (int t = 0; t <= life; t++) {
            Vec3 here = start.m_82549_(direction.m_82490_(Math.min(distance - 1.0, speed * t)));
            PieceFx.later(t, () -> {
                if (water) {
                    for (int i = 0; i < PieceFx.count(3); i++) {
                        PieceFx.at(PieceFx.DROP, false, 10, here.m_82520_(r(0.1), r(0.1), r(0.1))).velocity(new Vec3(r(0.03), 0.02, r(0.03)))
                            .colors(WATER, 0x20C4ECFF).size(0.1, 0.05).fall(0.035F).bloom(0).spawn();
                    }
                } else {
                    for (int i = 0; i < PieceFx.count(5); i++) {
                        PieceFx.at(PieceFx.STREAK, false, 6, here.m_82520_(r(0.4), r(0.4), r(0.4)))
                            .velocity(direction.m_82490_(-0.08).m_82520_(r(0.06), r(0.06), r(0.06))).colors(0xA0FFFFFF, 0x00FFFFFF).size(0.34, 0.06)
                            .drag(0.85).bloom(0).spawn();
                    }
                }
            });
        }
        PieceFx.later(life, () -> {
            if (water) {
                splash(to, direction, 0.8);
            } else {
                PieceFx.at(PieceFx.RING, false, 7, to).colors(0x90FFFFFF, 0x00FFFFFF).size(0.4, 3.0).bloom(0).spawn();
                dust(to.m_82520_(0, -0.5, 0), direction, 8);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------- starts

    static void started(ClientLevel level, StyleMove move, S2CStyleFx m) {
        Vec3 feet = new Vec3(m.x(), m.y(), m.z());
        Vec3 facing = flat(m.dx(), m.dz());
        int id = m.entity();
        switch (move.effect()) {
            case VANISH -> {
                dust(feet, null, 14);
                PieceFx.at(PieceFx.RING, false, 6, feet.m_82520_(0, 1.0, 0)).colors(0x80FFFFFF, 0x00FFFFFF).size(0.4, 2.6).bloom(0).spawn();
            }
            case FIRE -> {
                for (int t = 0; t < move.duration(); t++) {
                    int tick = t;
                    PieceFx.later(t, () -> {
                        Entity user = level.m_6815_(id);
                        Vec3 at = user == null ? feet : user.m_20182_();
                        double a = tick * Math.PI * 2 / 4.0;
                        Vec3 foot = at.m_82520_(Math.cos(a) * 1.0, 0.5, Math.sin(a) * 1.0);
                        flames(foot, 0.25, 4);
                    });
                }
            }
            case GUARD -> {
                for (int t = 0; t < move.duration(); t += 5) {
                    PieceFx.later(t, () -> {
                        Entity user = level.m_6815_(id);
                        Vec3 at = (user == null ? feet : user.m_20182_()).m_82520_(0, 1.0, 0);
                        for (int i = 0; i < PieceFx.count(5); i++) {
                            PieceFx.at(PieceFx.SPARK, true, 6, at.m_82520_(r(0.5), r(0.8), r(0.5))).velocity(new Vec3(r(0.1), 0.05, r(0.1)))
                                .colors(0xFFDCE6F0, 0x007890A8).size(0.16, 0.04).bloom(0).spawn();
                        }
                    });
                }
                PieceFx.at(PieceFx.RING, false, 8, feet.m_82520_(0, 1.0, 0)).colors(0xA0B8C4D0, 0x00B8C4D0).size(0.5, 2.4).bloom(0).spawn();
            }
            default -> {
            }
        }
        if (move.motion().kind() == Motion.Kind.DASH) {
            for (int t = move.motion().tick(); t <= move.motion().until(); t++) {
                PieceFx.later(t, () -> {
                    Entity user = level.m_6815_(id);
                    if (user != null) {
                        dust(user.m_20182_(), null, 4);
                        PieceFx.at(PieceFx.STREAK, false, 5, user.m_20182_().m_82520_(r(0.3), 0.9 + r(0.5), r(0.3)))
                            .velocity(facing.m_82490_(-0.2)).colors(0x90FFFFFF, 0x00FFFFFF).size(0.5, 0.1).bloom(0).spawn();
                    }
                });
            }
        }
        if (move.motion().kind() == Motion.Kind.LEAP) {
            for (int step : new int[] {move.motion().tick() + 4, move.motion().tick() + 7}) {
                PieceFx.later(step, () -> {
                    Entity user = level.m_6815_(id);
                    if (user != null) {
                        PieceFx.at(PieceFx.RING, false, 7, user.m_20182_()).colors(0xA0FFFFFF, 0x00FFFFFF).size(0.3, 1.8).bloom(0).spawn();
                    }
                });
            }
        }
    }

    /** Flames licking up from {@code at}. */
    static void flames(Vec3 at, double spread, int wanted) {
        int n = PieceFx.count(wanted);
        for (int i = 0; i < n; i++) {
            PieceFx.at(i % 3 == 0 ? PieceFx.EMBER : PieceFx.FLAME, true, 10 + RANDOM.nextInt(8), at.m_82520_(r(spread), r(spread * 0.5), r(spread)))
                .velocity(new Vec3(r(0.03), 0.05 + RANDOM.nextDouble() * 0.05, r(0.03))).colors(0xFFFFC060, 0x00FF3010).size(0.42, 0.1)
                .wander(0.02F).drag(0.94).spawn();
        }
    }
}
