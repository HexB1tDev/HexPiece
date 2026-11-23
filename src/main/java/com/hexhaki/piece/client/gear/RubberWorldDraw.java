package com.hexhaki.piece.client.gear;

import com.hexhaki.network.msg.S2CGearFx;
import com.hexhaki.piece.gear.RubberJump;
import com.hexhaki.piece.gear.RubberWave;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.NikaJump;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.0: Rubber World's ground rolling like rubber (gear/RubberWave says how high it rises where and when). The original's field
 * (GearFields: everything in it bounces, no fall damage) sends its RUBBER_FIELD effect (S2CGearFx: the middle, power = the radius) at
 * the release and every second while it lasts; patch/PiecePatch.rubberWorld makes {@code GearVfx.play} tell {@link #play} first. A
 * field not heard of for {@link #QUIET} ticks has ended and its ground settles.
 *
 * <p>Each frame the ground's own blocks where it rises are drawn as copies lifted and tilted with the wave (the top solid block of each
 * column, and the one under it when it rises far: the way the Gura ultimate's heave draws them), nearest the camera first, at most
 * {@link #MOST_DRAWN} blocks, within {@link #NEAR} blocks; the real ground stays and the copies rise out of it. Every living body
 * standing in a field is drawn lifted by the ground under it (it rides the waves). Columns are found once each (at most
 * {@link #LOOKUPS} new a frame) and kept, on a grid round the field, while it lasts.
 *
 * <p>3.3.2: Gear 5's jump turns the ground to rubber for a moment ({@link #hop}: gear/RubberJump.hop): under the take-off it springs up
 * after the feet and ripples out, and ripples again round the landing. One's own jumps come from client/gear/NikaJumpClient at once,
 * everyone else's from the server (server/NikaJump.HOP and LAND); each is a small field of its own, drawn first, kept apart from Rubber
 * World's ({@link #MOST_HOPS} at most).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class RubberWorldDraw {
    /** The most blocks drawn in one frame, and how far from the camera. */
    public static final int MOST_DRAWN = 520;
    static final int NEAR = 28;
    /** New columns looked up in one frame at most. */
    static final int LOOKUPS = 360;
    /** A field not heard of for this long has ended (the original says it again every 20 ticks). */
    static final int QUIET = 30;
    /** The share of the wave's slope the blocks tilt by. */
    static final double TILT = 0.6;
    /** The most jump ripples kept at once (the oldest goes first). */
    static final int MOST_HOPS = 10;
    /** A field's kind: Rubber World's, a jump's take-off, a jump's landing. */
    static final int WORLD = 0;
    static final int TAKE_OFF = 1;
    static final int LANDING = 2;

    /** One column: its top solid block, the one under it (or null), where, how it is lit. */
    record Column(BlockState top, BlockState under, int x, int y, int z, int light) {
    }

    /** A column with nothing to draw (none, under water, a block entity). */
    static final Column NONE = new Column(null, null, 0, 0, 0, 0);

    static final class Field {
        double x;
        double y;
        double z;
        double radius;
        double born;
        double heard;
        double end = Double.POSITIVE_INFINITY;
        int kind = WORLD;
        /** The columns found, on a grid round the middle (no allocation a frame): from (minX, minZ), size by size. */
        int minX;
        int minZ;
        int size;
        Column[] columns;
    }

    static final List<Field> FIELDS = new ArrayList<>();
    static final List<Field> HOPS = new ArrayList<>();
    /** The columns round the camera, nearest first (dx, dz pairs). */
    static final int[] AROUND;
    private static final org.joml.Quaternionf TURN = new org.joml.Quaternionf();

    static {
        List<int[]> offsets = new ArrayList<>();
        for (int dx = -NEAR; dx <= NEAR; dx++) {
            for (int dz = -NEAR; dz <= NEAR; dz++) {
                if (dx * dx + dz * dz <= NEAR * NEAR) {
                    offsets.add(new int[] {dx, dz});
                }
            }
        }
        offsets.sort((a, b) -> Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]));
        AROUND = new int[offsets.size() * 2];
        for (int i = 0; i < offsets.size(); i++) {
            AROUND[i * 2] = offsets.get(i)[0];
            AROUND[i * 2 + 1] = offsets.get(i)[1];
        }
    }

    private RubberWorldDraw() {
    }

    /** At the head of the original's GearVfx.play: a Rubber World field begun, or said again. */
    public static void play(S2CGearFx message) {
        if (message.kind() != S2CGearFx.Kind.RUBBER_FIELD.ordinal()) {
            return;
        }
        double now = clock(0.0F);
        for (Field f : FIELDS) {
            double dx = f.x - message.x();
            double dz = f.z - message.z();
            if (dx * dx + dz * dz < 2.25 && Math.abs(f.y - message.y()) < 1.5 && now <= f.end) {
                f.heard = now;
                return;
            }
        }
        if (FIELDS.size() >= 6) {
            FIELDS.remove(0);
        }
        Field f = new Field();
        f.x = message.x();
        f.y = message.y();
        f.z = message.z();
        f.radius = Math.max(2.5, message.power());
        f.size = 2 * (int) Math.ceil(f.radius) + 3;
        f.minX = (int) Math.floor(f.x) - f.size / 2;
        f.minZ = (int) Math.floor(f.z) - f.size / 2;
        f.columns = new Column[f.size * f.size];
        f.born = now;
        f.heard = now;
        FIELDS.add(f);
    }

    /** A Gear 5 jump's take-off (or landing) at the feet (x, y, z): the ground there turns to rubber for a moment. */
    public static void hop(double x, double y, double z, boolean landing) {
        if (Minecraft.m_91087_().f_91073_ == null) {
            return;
        }
        if (HOPS.size() >= MOST_HOPS) {
            HOPS.remove(0);
        }
        Field f = new Field();
        f.kind = landing ? LANDING : TAKE_OFF;
        f.x = x;
        f.y = y;
        f.z = z;
        f.radius = landing ? RubberJump.LAND_RADIUS : RubberJump.HOP_RADIUS;
        f.size = 2 * (int) Math.ceil(f.radius) + 3;
        f.minX = (int) Math.floor(f.x) - f.size / 2;
        f.minZ = (int) Math.floor(f.z) - f.size / 2;
        f.columns = new Column[f.size * f.size];
        f.born = clock(0.0F);
        f.heard = f.born;
        f.end = f.born + RubberJump.LIFE;
        HOPS.add(f);
    }

    /** server/NikaJump's HOP and LAND (from client/dragon/UoFx): someone else's jump (one's own rippled already, at once). */
    public static void accept(S2CUoFx m) {
        LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self != null && self.m_19879_() == m.user()) {
            return;
        }
        hop(m.x(), m.y(), m.z(), m.kind() == NikaJump.LAND);
    }

    /** How high the ground rises {@code r} from field {@code f}'s middle at {@code angle}, {@code t} ticks after it began. */
    static double height(Field f, double r, double angle, double t) {
        if (f.kind == WORLD) {
            return RubberWave.rise(r, angle, t, f.radius, f.end - f.born);
        }
        return RubberJump.hop(r, angle, t, f.kind == LANDING);
    }

    static double slope(Field f, double r, double angle, double t) {
        if (f.kind == WORLD) {
            return RubberWave.slope(r, angle, t, f.radius, f.end - f.born);
        }
        return RubberJump.slope(r, angle, t, f.kind == LANDING);
    }

    static double clock(float partial) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_() + partial;
    }

    /** How high the ground rises at (x, z) under field {@code f} at {@code now}. */
    static double rise(Field f, double x, double z, double now) {
        double dx = x - f.x;
        double dz = z - f.z;
        return height(f, Math.sqrt(dx * dx + dz * dz), Math.atan2(dz, dx), now - f.born);
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || FIELDS.isEmpty() && HOPS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            FIELDS.clear();
            HOPS.clear();
            return;
        }
        double now = clock(event.getPartialTick());
        FIELDS.removeIf(f -> {
            if (f.end == Double.POSITIVE_INFINITY && now - f.heard > QUIET) {
                f.end = now;
            }
            return now > f.end + RubberWave.FADE + 1;
        });
        HOPS.removeIf(f -> now > f.end + 1 || now < f.born - 40);
        Vec3 cam = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        try {
            BlockRenderDispatcher blocks = minecraft.m_91289_();
            int budget = MOST_DRAWN;
            // the jumps' ripples first: small, and where the players are
            for (Field f : HOPS) {
                double fx = f.x - cam.f_82479_;
                double fz = f.z - cam.f_82481_;
                if (budget <= 0 || Math.sqrt(fx * fx + fz * fz) > f.radius + NEAR) {
                    continue;
                }
                budget -= draw(level, f, pose, buffers, blocks, cam, now, budget);
            }
            for (Field f : FIELDS) {
                double fx = f.x - cam.f_82479_;
                double fz = f.z - cam.f_82481_;
                if (Math.sqrt(fx * fx + fz * fz) > f.radius + NEAR) {
                    continue;
                }
                budget -= draw(level, f, pose, buffers, blocks, cam, now, budget);
                if (budget <= 0) {
                    break;
                }
            }
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Rubber World's ground could not be drawn", failure);
            FIELDS.clear();
            HOPS.clear();
        }
        buffers.m_109911_();
    }

    /** Draws field {@code f}'s risen ground round the camera; returns how many blocks it drew (at most {@code budget}). */
    static int draw(ClientLevel level, Field f, PoseStack pose, MultiBufferSource buffers, BlockRenderDispatcher blocks, Vec3 cam, double now,
                    int budget) {
        int cx = (int) Math.floor(cam.f_82479_);
        int cz = (int) Math.floor(cam.f_82481_);
        int foot = (int) Math.floor(f.y - 0.01);
        double t = now - f.born;
        int drawn = 0;
        int lookups = 0;
        for (int i = 0; i < AROUND.length && drawn < budget; i += 2) {
            int x = cx + AROUND[i];
            int z = cz + AROUND[i + 1];
            double dx = x + 0.5 - f.x;
            double dz = z + 0.5 - f.z;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r > f.radius) {
                continue;
            }
            double angle = Math.atan2(dz, dx);
            double h = height(f, r, angle, t);
            if (h < 0.03) {
                continue;
            }
            int gx = x - f.minX;
            int gz = z - f.minZ;
            if (gx < 0 || gz < 0 || gx >= f.size || gz >= f.size) {
                continue;
            }
            Column c = f.columns[gx * f.size + gz];
            if (c == null) {
                if (lookups >= LOOKUPS) {
                    continue;
                }
                lookups++;
                c = column(level, x, foot, z);
                f.columns[gx * f.size + gz] = c;
            }
            if (c == NONE) {
                continue;
            }
            boolean deep = h > 0.6 && c.under != null;
            drawn += deep ? 2 : 1;
            double tilt = Math.atan(slope(f, r, angle, t)) * TILT;
            double rx = r > 1.0E-6 ? dx / r : 1.0;
            double rz = r > 1.0E-6 ? dz / r : 0.0;
            pose.m_85836_();
            // the block's top middle, lifted; turned about the flat axis across the way out (a rise outward faces its top inward)
            pose.m_85837_(x + 0.5 - cam.f_82479_, c.y + 1 + h - cam.f_82480_, z + 0.5 - cam.f_82481_);
            pose.m_252781_(TURN.rotationAxis((float) tilt, (float) -rz, 0.0F, (float) rx));
            pose.m_85837_(-0.5, -1.0, -0.5);
            blocks.m_110912_(c.top, pose, buffers, c.light, OverlayTexture.f_118083_);
            if (deep) {
                pose.m_85837_(0.0, -1.0, 0.0);
                blocks.m_110912_(c.under, pose, buffers, c.light, OverlayTexture.f_118083_);
            }
            pose.m_85849_();
        }
        return drawn;
    }

    /** The top solid block of the column at (x, z) near the height {@code foot}, or NONE (none, under water, a block entity). */
    static Column column(ClientLevel level, int x, int foot, int z) {
        for (int y = foot + 10; y >= foot - 14; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.m_6425_(pos).m_76178_()) {
                return NONE;
            }
            BlockState state = level.m_8055_(pos);
            if (state.m_60795_() || state.m_60812_(level, pos).m_83281_()) {
                continue;
            }
            if (level.m_7702_(pos) != null) {
                return NONE;
            }
            BlockPos below = new BlockPos(x, y - 1, z);
            BlockState under = level.m_8055_(below);
            if (under.m_60795_() || level.m_7702_(below) != null) {
                under = null;
            }
            return new Column(state, under, x, y, z, LevelRenderer.m_109541_(level, new BlockPos(x, y + 1, z)));
        }
        return NONE;
    }

    /**
     * Every living body standing in a field (or a jump's ripple) is drawn lifted by the ground under it: it rides the waves (not one's
     * own first person; not a body in the air, 3.3.2).
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void ride(RenderLivingEvent.Pre<?, ?> event) {
        if (FIELDS.isEmpty() && HOPS.isEmpty() || FirstPersonMode.isFirstPersonPass()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!entity.m_20096_()) {
            return;
        }
        float partial = event.getPartialTick();
        double x = entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * partial;
        double y = entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * partial;
        double z = entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * partial;
        double now = clock(partial);
        double lift = 0.0;
        for (Field f : FIELDS) {
            if (Math.abs(y - f.y) < 6.0) {
                lift = Math.max(lift, rise(f, x, z, now));
            }
        }
        for (Field f : HOPS) {
            if (Math.abs(y - f.y) < 2.0) {
                lift = Math.max(lift, rise(f, x, z, now));
            }
        }
        if (lift > 0.01) {
            event.getPoseStack().m_85837_(0.0, lift, 0.0);
        }
    }
}
