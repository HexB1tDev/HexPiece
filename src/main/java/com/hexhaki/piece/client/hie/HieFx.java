package com.hexhaki.piece.client.hie;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.hie.HieArt;
import com.hexhaki.piece.hie.HieSounds;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.HieMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.1.9: the Hie Hie no Mi's moves as every client sees them (server/HieMoves sends S2CUoFx kinds 120 to 139; client/dragon/UoFx hands
 * them here). The frozen bodies, the Ice Ball and the frozen ground are real ice blocks the server puts in the world (the owner, asked:
 * "Real ice blocks", "Real ice ground"); where the world refused them (the mobGriefing rule, a claim) the game's own ice block is drawn
 * there instead, so it looks the same. Everything else is hie/HieArt's smooth glassy ice ("Smooth glassy ice"), drawn once a frame after
 * the particles through the game's own debugQuads type (alpha blended, depth tested, no culling: the owner's device), eye-centred: the
 * spears forming round the user and flying, the orb gathering and flying, the pheasant, bursts, the mist of the freeze, the saber's cuts,
 * the ice fist (client/FirstPersonLight.fist; in first person in the hand's own pass, client/HandPass), the slide along the road (one's
 * own body carried by the game's own movement), the frost held while ice gathers (client/LoopSound: faded out the moment it ends), the
 * attack button given back to the game's punch (the saber's cut while it is out).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HieFx {
    private static long clock;
    private static boolean kitAdded;

    /** One effect in the world. */
    record Spot(int kind, int id, int user, Vec3 at, Vec3 dir, Vec3 b, double size, int ticks, long born) {
    }

    /** A body frozen: where it is held, its size, whether real ice went round it (else the blocks drawn here), until when. */
    record Frozen(Vec3 at, long[] drawn, long until) {
    }

    /** An Ice Ball's prison: its middle, its radius, the blocks drawn here where no real ice could go, until when. */
    record Ball(Vec3 at, double radius, long[] drawn, long born, long until, int seed) {
    }

    /** Ice Age's freeze running over the ground: where, its way, half width, reach, the ground's height at its front's mist points. */
    record Sheet(Vec3 at, double fx, double fz, double half, double reach, double[] heights, long born, int seed) {
    }

    /** The ice road shooting along: from, to, how long it grows. */
    record Road(Vec3 from, Vec3 to, int ticks, long born, int seed) {
    }

    static final int MOST_SPOTS = 96;
    static final double FAR = 160.0;
    /** Ice Age's front mist: across its width so many points, out along its reach a height every so many blocks. */
    static final int SHEET_ACROSS = 9;
    static final double SHEET_STEP = 2.0;
    private static final List<Spot> SPOTS = new ArrayList<>();
    private static final List<Ball> BALLS = new ArrayList<>();
    private static final List<Sheet> SHEETS = new ArrayList<>();
    private static final List<Road> ROADS = new ArrayList<>();
    static final Map<Integer, Frozen> FROZEN = new HashMap<>();
    /** Partisan's spears formed round each user: the tick each slot's spear took shape (-1: none). */
    static final Map<Integer, long[]> FORMS = new HashMap<>();
    static final Map<Integer, Long> GATHERS = new HashMap<>();
    static final Map<Integer, Long> COATS = new HashMap<>();
    static final Map<Integer, Long> SABERS = new HashMap<>();
    static final double[] ROW = new double[SHEET_ACROSS * 3];

    private HieFx() {
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
    }

    static boolean firstPerson(int id) {
        return self(id) && Minecraft.m_91087_().f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
    }

    /** Whether {@code id} holds the Ice Saber now (client/hie/HieClient draws it in the hand). */
    public static boolean saber(int id) {
        Long until = SABERS.get(id);
        return until != null && clock <= until;
    }

    /** client/HandPass: whether {@code id} has the Ice Ball's orb or the ice fist to draw on the first-person fist. */
    public static boolean handWanted(int id) {
        return GATHERS.containsKey(id) || COATS.containsKey(id);
    }

    /** client/HandPass: one's own orb gathering, or the ice coating the fist, in the hand's pass, on the fist. */
    public static void handPass(FireMesh.Sink sink, int id, float partial) {
        double[] v = com.hexhaki.piece.pika.LightArt.firstPersonFingertip(-0.05);
        Long since = GATHERS.get(id);
        if (since != null) {
            HieArt.gather(sink, v[0], v[1], v[2], 0.12, clock - since + partial, id);
        }
        Long coat = COATS.get(id);
        if (coat != null) {
            HieArt.coat(sink, v[0], v[1], v[2], 0, 0, -1, 0.5, clock - coat + partial, id);
        }
    }

    static void add(Spot spot) {
        if (SPOTS.size() >= MOST_SPOTS) {
            SPOTS.remove(0);
        }
        SPOTS.add(spot);
    }

    static void frost(int user, java.util.function.BooleanSupplier on) {
        com.hexhaki.piece.client.LoopSound.play(HieSounds.FROST, entity(user), 0.7F, 1.1F, true, true, 1.2, on);
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        Vec3 b = new Vec3(m.bx(), m.by(), m.bz());
        switch (m.kind()) {
            case HieMoves.FREEZE -> {
                boolean real = m.bz() > 0.5F;
                boolean ball = m.by() > 0.5F;
                long[] drawn = real || ball ? new long[0] : encased(at, m.size(), m.bx());
                FROZEN.put(m.id(), new Frozen(at, drawn, clock + m.ticks() + 2));
                add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), 24, clock));
            }
            case HieMoves.THAW -> {
                FROZEN.remove(m.id());
                if (m.by() > 0.5F && m.bz() < 0.5F) {
                    add(new Spot(HieMoves.SHATTER, m.id(), m.user(), at, a, b, m.size(), (int) HieArt.SHATTER_LIFE, clock));
                }
            }
            case HieMoves.SPEAR -> {
                int slot = (int) m.bz();
                long[] formed = FORMS.get(m.user());
                if (slot > 0 && formed != null && slot - 1 < formed.length) {
                    formed[slot - 1] = -1;
                }
                add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), m.ticks(), clock));
            }
            case HieMoves.FORM -> {
                long[] formed = FORMS.computeIfAbsent(m.user(), k -> {
                    long[] slots = new long[HieMoves.SPEARS_MOST];
                    java.util.Arrays.fill(slots, -1L);
                    return slots;
                });
                boolean first = true;
                for (long f : formed) {
                    first &= f < 0;
                }
                if (m.id() >= 0 && m.id() < formed.length) {
                    formed[m.id()] = clock;
                }
                if (first) {
                    int user = m.user();
                    frost(user, () -> FORMS.containsKey(user));
                }
            }
            case HieMoves.FORM_OFF -> FORMS.remove(m.user());
            case HieMoves.BURST -> {
                int ended = (int) m.by();
                if (ended != 0) {
                    SPOTS.removeIf(s -> (s.kind == HieMoves.SPEAR || s.kind == HieMoves.ORB || s.kind == HieMoves.PHEASANT) && s.id == ended);
                }
                add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), (int) HieArt.burstLife(m.size()), clock));
            }
            case HieMoves.GATHER -> {
                Long since = clock;
                long until = clock + m.ticks() + 1;
                GATHERS.put(m.user(), since);
                frost(m.user(), () -> since.equals(GATHERS.get(m.user())) && clock < until);
            }
            case HieMoves.ORB -> {
                GATHERS.remove(m.user());
                add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), m.ticks(), clock));
            }
            case HieMoves.BALL -> {
                int orb = (int) m.by();
                SPOTS.removeIf(s -> s.kind == HieMoves.ORB && s.id == orb);
                long[] drawn = m.bz() > 0.5F ? new long[0] : sphere(at, m.size());
                BALLS.add(new Ball(at, m.size(), drawn, clock, clock + m.ticks() + 1, m.id()));
            }
            case HieMoves.SHATTER -> {
                BALLS.removeIf(ball -> ball.seed == m.id());
                add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), (int) HieArt.SHATTER_LIFE, clock));
            }
            case HieMoves.PHEASANT, HieMoves.SLASH, HieMoves.SCATTER -> add(new Spot(m.kind(), m.id(), m.user(), at, a, b, m.size(), m.ticks(), clock));
            case HieMoves.SHEET -> SHEETS.add(new Sheet(at, a.f_82479_, a.f_82481_, m.bx(), m.size(), sheetHeights(at, a.f_82479_, a.f_82481_, m.bx(),
                m.size()), clock, m.id()));
            case HieMoves.ROAD -> ROADS.add(new Road(at, a, Math.max(1, m.ticks()), clock, m.id()));
            case HieMoves.SLIDE -> {
                if (self(m.user())) {
                    move = Move.dash(at, m.id(), m.ticks(), (int) m.bx());
                }
            }
            case HieMoves.COAT_ON -> COATS.put(m.user(), clock);
            case HieMoves.COAT_OFF -> COATS.remove(m.user());
            case HieMoves.SABER_ON -> SABERS.put(m.user(), clock + m.ticks());
            case HieMoves.SABER_OFF -> SABERS.remove(m.user());
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ice blocks drawn

    /** The free blocks a body's box takes up standing at {@code at} (as server/HieFreeze.encase picks them), where the game's ice is drawn. */
    static long[] encased(Vec3 at, double half, double height) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return new long[0];
        }
        List<Long> out = new ArrayList<>();
        for (int y = (int) Math.floor(at.f_82480_ + 0.05); y <= (int) Math.floor(at.f_82480_ + height - 0.05); y++) {
            for (int x = (int) Math.floor(at.f_82479_ - half + 0.05); x <= (int) Math.floor(at.f_82479_ + half - 0.05); x++) {
                for (int z = (int) Math.floor(at.f_82481_ - half + 0.05); z <= (int) Math.floor(at.f_82481_ + half - 0.05) && out.size() < 12; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.m_8055_(pos).m_60795_()) {
                        out.add(pos.m_121878_());
                    }
                }
            }
        }
        return out.stream().mapToLong(Long::longValue).toArray();
    }

    /** The free blocks within {@code radius} of {@code at} (an Ice Ball's, where the game's ice is drawn). */
    static long[] sphere(Vec3 at, double radius) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return new long[0];
        }
        List<Long> out = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        int cx = (int) Math.floor(at.f_82479_);
        int cy = (int) Math.floor(at.f_82480_);
        int cz = (int) Math.floor(at.f_82481_);
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r && out.size() < 160; dz++) {
                    double ox = cx + dx + 0.5 - at.f_82479_;
                    double oy = cy + dy + 0.5 - at.f_82480_;
                    double oz = cz + dz + 0.5 - at.f_82481_;
                    BlockPos pos = new BlockPos(cx + dx, cy + dy, cz + dz);
                    if (ox * ox + oy * oy + oz * oz <= radius * radius && level.m_8055_(pos).m_60795_()) {
                        out.add(pos.m_121878_());
                    }
                }
            }
        }
        return out.stream().mapToLong(Long::longValue).toArray();
    }

    /** The game's own ice block at every one of {@code blocks} (where the world took no real ice). */
    static int drawIce(PoseStack pose, MultiBufferSource buffers, net.minecraft.client.renderer.block.BlockRenderDispatcher dispatcher, ClientLevel level,
                       Vec3 cam, long[] blocks) {
        net.minecraft.world.level.block.state.BlockState ice = net.minecraft.world.level.block.Blocks.f_50126_.m_49966_();
        for (long key : blocks) {
            BlockPos pos = BlockPos.m_122022_(key);
            pose.m_85836_();
            pose.m_85837_(pos.m_123341_() - cam.f_82479_, pos.m_123342_() - cam.f_82480_, pos.m_123343_() - cam.f_82481_);
            dispatcher.m_110912_(ice, pose, buffers, LevelRenderer.m_109541_(level, pos), OverlayTexture.f_118083_);
            pose.m_85849_();
        }
        return blocks.length;
    }

    /** The ground's height (relative to {@code at}) under Ice Age's front mist points, worked out once: [across][out]. */
    static double[] sheetHeights(Vec3 at, double fx, double fz, double half, double reach) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        int out = (int) Math.ceil(reach / SHEET_STEP) + 1;
        double[] heights = new double[SHEET_ACROSS * out];
        if (level == null) {
            return heights;
        }
        double base = Math.atan2(fz, fx);
        for (int i = 0; i < SHEET_ACROSS; i++) {
            double ang = base - half + 2.0 * half * i / (SHEET_ACROSS - 1);
            double y = at.f_82480_;
            for (int k = 0; k < out; k++) {
                double d = k * SHEET_STEP;
                double px = at.f_82479_ + Math.cos(ang) * d;
                double pz = at.f_82481_ + Math.sin(ang) * d;
                BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(px, y + 4.0, pz), new Vec3(px, y - 8.0, pz), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY, null));
                if (hit.m_6662_() == HitResult.Type.BLOCK) {
                    y = hit.m_82450_().f_82480_;
                }
                heights[i * out + k] = y - at.f_82480_;
            }
        }
        return heights;
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.HIE, new HieClient()); // a fruit the original never had: no client kit of its own
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        SPOTS.removeIf(s -> clock - s.born > life(s) + 1);
        BALLS.removeIf(b -> clock > b.until + 2);
        SHEETS.removeIf(s -> (clock - s.born) * HieArt.SHEET_SPEED > s.reach + 12.0);
        ROADS.removeIf(r -> clock - r.born > r.ticks + 16);
        FROZEN.values().removeIf(f -> clock > f.until);
        FORMS.keySet().removeIf(id -> entity(id) == null);
        GATHERS.entrySet().removeIf(e -> clock - e.getValue() > HieMoves.GATHER_TICKS + 6 || entity(e.getKey()) == null);
        COATS.entrySet().removeIf(e -> clock - e.getValue() > 80 || entity(e.getKey()) == null);
        SABERS.entrySet().removeIf(e -> clock > e.getValue() || entity(e.getKey()) == null);
        if (move != null) {
            move(minecraft.f_91074_, level);
        }
    }

    static double life(Spot s) {
        return switch (s.kind) {
            case HieMoves.SPEAR, HieMoves.ORB, HieMoves.PHEASANT -> s.ticks + 2;
            case HieMoves.BURST -> HieArt.burstLife(s.size);
            case HieMoves.SHATTER -> HieArt.SHATTER_LIFE;
            case HieMoves.SLASH -> HieArt.SLASH_LIFE;
            case HieMoves.SCATTER -> HieArt.SCATTER_LIFE;
            default -> s.ticks;
        };
    }

    // ------------------------------------------------------------------------------------------------------ one's own slide

    /** Ice Time's lunge and the slide along the road: one's own body carried to a place over so many ticks (following whom it goes for), then held. */
    static final class Move {
        Vec3 to;
        int target;
        Vec3 targetFrom;
        int left;
        int hold;
        boolean holding;

        static Move dash(Vec3 to, int target, int ticks, int hold) {
            Move m = new Move();
            m.to = to;
            m.target = target;
            Entity e = target >= 0 ? entity(target) : null;
            m.targetFrom = e != null ? e.m_20182_() : null;
            m.left = Math.max(1, ticks);
            m.hold = hold;
            return m;
        }
    }

    static Move move;

    static void move(net.minecraft.client.player.LocalPlayer self, ClientLevel level) {
        Move m = move;
        if (self == null || !self.m_6084_() || self.m_20159_()) {
            move = null;
            return;
        }
        Vec3 pos = self.m_20182_();
        Entity e = m.target >= 0 ? level.m_6815_(m.target) : null;
        Vec3 to = m.to;
        if (e != null && m.targetFrom != null) {
            to = to.m_82549_(e.m_20182_().m_82546_(m.targetFrom));
        }
        Vec3 step;
        if (!m.holding) {
            step = to.m_82546_(pos).m_82490_(1.0 / Math.max(1, m.left));
            if (--m.left <= 0) {
                m.holding = true;
                if (e != null) {
                    face(self, to, e);
                }
            }
        } else {
            step = Vec3.f_82478_;
            if (--m.hold < 0) {
                move = null;
            }
        }
        self.m_20334_(step.f_82479_, step.f_82480_, step.f_82481_);
        self.f_19789_ = 0.0F;
    }

    static void face(net.minecraft.client.player.LocalPlayer self, Vec3 feet, Entity e) {
        Vec3 d = e.m_20182_().m_82520_(0, e.m_20206_() * 0.6, 0).m_82546_(feet.m_82520_(0, self.m_20192_(), 0));
        float yaw = (float) Math.toDegrees(Math.atan2(-d.f_82479_, d.f_82481_));
        float pitch = (float) -Math.toDegrees(Math.atan2(d.f_82480_, Math.sqrt(d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_)));
        self.m_146922_(yaw);
        self.m_146926_(pitch);
        self.f_19859_ = yaw;
        self.f_19860_ = pitch;
        self.f_20885_ = yaw;
        self.f_20886_ = yaw;
    }

    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (move == null || event.getEntity() != Minecraft.m_91087_().f_91074_) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    /**
     * The attack button the game's own punch with the Ice fruit (not while the orb gathers, the fist is iced or one's own slide goes; with
     * the saber out the original's own message carries it to the server as the saber's cut).
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void punch(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !event.isCanceled()) {
            return;
        }
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || move != null || saber(self.m_19879_()) || GATHERS.containsKey(self.m_19879_()) || COATS.containsKey(self.m_19879_())
            || !com.hexhaki.fruit.client.FruitClient.active() || com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) != Fruit.HIE
            || !self.m_21205_().m_41619_()) {
            return;
        }
        event.setCanceled(false);
        event.setSwingHand(true);
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    static final class Writer implements FireMesh.Sink {
        VertexConsumer buffer;
        Matrix4f matrix;

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                int c = argb[k];
                this.buffer.m_252986_(this.matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F).m_5752_();
            }
        }
    }

    static final Writer SINK = new Writer();

    static boolean idle() {
        return SPOTS.isEmpty() && BALLS.isEmpty() && SHEETS.isEmpty() && ROADS.isEmpty() && FROZEN.isEmpty() && FORMS.isEmpty() && GATHERS.isEmpty()
            && COATS.isEmpty();
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || idle()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        Vec3 cam = event.getCamera().m_90583_();
        float partial = event.getPartialTick();
        // where the world took no real ice, the game's own ice block drawn in its place (first: under every see-through effect)
        boolean drew = false;
        try {
            net.minecraft.client.renderer.block.BlockRenderDispatcher dispatcher = minecraft.m_91289_();
            for (Frozen f : FROZEN.values()) {
                if (f.drawn.length > 0 && f.at.m_82557_(cam) < FAR * FAR) {
                    drawIce(pose, buffers, dispatcher, level, cam, f.drawn);
                    drew = true;
                }
            }
            for (Ball b : BALLS) {
                if (b.drawn.length > 0 && b.at.m_82557_(cam) < FAR * FAR) {
                    drawIce(pose, buffers, dispatcher, level, cam, b.drawn);
                    drew = true;
                }
            }
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: the Ice fruit's ice blocks could not be drawn", failure);
            FROZEN.clear();
            BALLS.clear();
        }
        if (drew) {
            buffers.m_109911_();
        }
        RenderType type = RenderType.m_269166_();
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        try {
            SINK.buffer = buffers.m_6299_(type);
            draw(level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: an Ice fruit move could not be drawn", failure);
            clear();
        }
        FireMesh.palette(FireMesh.BAND);
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    static boolean far(Vec3 at, Vec3 cam) {
        return at.m_82557_(cam) > FAR * FAR;
    }

    static void draw(ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        // Ice Age's mist rolling out ahead of the freeze
        for (Sheet s : SHEETS) {
            double age = clock - s.born + partial;
            double front = age * HieArt.SHEET_SPEED;
            if (front > s.reach + 1.0 || far(s.at, cam)) {
                continue;
            }
            int out = (int) Math.ceil(s.reach / SHEET_STEP) + 1;
            int k = Math.min(out - 1, (int) Math.round(front / SHEET_STEP));
            double base = Math.atan2(s.fz, s.fx);
            for (int i = 0; i < SHEET_ACROSS; i++) {
                double ang = base - s.half + 2.0 * s.half * i / (SHEET_ACROSS - 1);
                ROW[i * 3] = s.at.f_82479_ + Math.cos(ang) * front - cx;
                ROW[i * 3 + 1] = s.at.f_82480_ + s.heights[i * out + k] + 0.7 - cy;
                ROW[i * 3 + 2] = s.at.f_82481_ + Math.sin(ang) * front - cz;
            }
            HieArt.mistRow(SINK, ROW, SHEET_ACROSS, 1.2 + front * 0.04, 0.5 * (1.0 - front / (s.reach + 2.0)) + 0.1, age, s.seed);
        }
        // the road's growing end: mist and a cold streak shooting on
        for (Road r : ROADS) {
            double age = clock - r.born + partial;
            if (age > r.ticks + 2 || far(r.from, cam)) {
                continue;
            }
            double t = Math.min(1.0, age / r.ticks);
            Vec3 end = r.from.m_82549_(r.to.m_82546_(r.from).m_82490_(t));
            Vec3 v = r.to.m_82546_(r.from).m_82490_(1.0 / r.ticks);
            HieArt.mist(SINK, end.f_82479_ - cx, end.f_82480_ + 0.6 - cy, end.f_82481_ - cz, 1.0, 0.45, age, r.seed);
            HieArt.trail(SINK, end.f_82479_ - cx, end.f_82480_ + 0.3 - cy, end.f_82481_ - cz, v.f_82479_, v.f_82480_, v.f_82481_, 0.4, age, r.seed);
        }
        // mist at the foot of every Ice Ball
        for (Ball b : BALLS) {
            if (far(b.at, cam)) {
                continue;
            }
            double age = clock - b.born + partial;
            for (int k = 0; k < 3; k++) {
                double a = 6.283 * k / 3.0 + age * 0.015 + b.seed;
                HieArt.mist(SINK, b.at.f_82479_ + Math.cos(a) * b.radius * 0.9 - cx, b.at.f_82480_ - b.radius * 0.7 - cy, b.at.f_82481_ + Math.sin(a) * b.radius * 0.9 - cz,
                    b.radius * 0.4, 0.3 * Math.min(1.0, age / 4.0), age, b.seed + k);
            }
        }
        // Partisan's spears in the air round their users, pointing where each looks
        for (Map.Entry<Integer, long[]> e : FORMS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            Vec3 feet = player.m_20318_(partial);
            Vec3 look = player.m_20252_(partial);
            Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
            flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
            Vec3 right = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
            Vec3 aim = player.m_20299_(partial).m_82549_(look.m_82490_(24.0));
            long[] formed = e.getValue();
            for (int k = 0; k < formed.length; k++) {
                if (formed[k] < 0) {
                    continue;
                }
                double[] s = HieArt.slot(k);
                double bob = Math.sin((clock + partial) * 0.12 + k) * 0.06;
                Vec3 p = feet.m_82549_(right.m_82490_(s[0])).m_82520_(0, s[1] + bob, 0).m_82549_(flat.m_82490_(-s[2]));
                Vec3 d = aim.m_82546_(p).m_82541_();
                double grow = (clock - formed[k] + partial) / 4.0;
                HieArt.spear(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, d.f_82479_, d.f_82480_, d.f_82481_, HieArt.SPEAR_LENGTH, grow, 0.95,
                    e.getKey() * 31 + k);
            }
        }
        for (Spot s : SPOTS) {
            if (far(s.at, cam)) {
                continue;
            }
            double age = clock - s.born + partial;
            double x = s.at.f_82479_ - cx;
            double y = s.at.f_82480_ - cy;
            double z = s.at.f_82481_ - cz;
            switch (s.kind) {
                case HieMoves.SPEAR -> {
                    if (age <= s.ticks) {
                        double vl = Math.max(1.0E-6, s.dir.m_82553_());
                        double px = x + s.dir.f_82479_ * age;
                        double py = y + s.dir.f_82480_ * age;
                        double pz = z + s.dir.f_82481_ * age;
                        HieArt.spear(SINK, px, py, pz, s.dir.f_82479_ / vl, s.dir.f_82480_ / vl, s.dir.f_82481_ / vl, s.size, 1.0, 1.0, s.id);
                        HieArt.trail(SINK, px - s.dir.f_82479_ / vl * s.size * 0.5, py - s.dir.f_82480_ / vl * s.size * 0.5,
                            pz - s.dir.f_82481_ / vl * s.size * 0.5, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, 0.16, age, s.id);
                    }
                }
                case HieMoves.ORB -> {
                    if (age <= s.ticks) {
                        double px = x + s.dir.f_82479_ * age;
                        double py = y + s.dir.f_82480_ * age;
                        double pz = z + s.dir.f_82481_ * age;
                        HieArt.orb(SINK, px, py, pz, s.size, age, s.id);
                        HieArt.trail(SINK, px, py, pz, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, age, s.id);
                    }
                }
                case HieMoves.PHEASANT -> {
                    if (age <= s.ticks) {
                        double px = x + s.dir.f_82479_ * age;
                        double py = y + s.dir.f_82480_ * age;
                        double pz = z + s.dir.f_82481_ * age;
                        HieArt.pheasant(SINK, px, py, pz, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, age, s.id);
                        HieArt.trail(SINK, px, py, pz, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size * 0.6, age, s.id);
                    }
                }
                case HieMoves.BURST -> HieArt.burst(SINK, x, y, z, s.size, s.b.f_82479_, age, s.id);
                case HieMoves.SHATTER -> HieArt.shatter(SINK, x, y, z, s.size, age, s.id);
                case HieMoves.SCATTER -> HieArt.scatter(SINK, x, y, z, s.size, age, s.id);
                case HieMoves.FREEZE -> {
                    // the freeze's mist rolling off its foot
                    double fade = 1.0 - Math.min(1.0, age / s.ticks);
                    for (int k = 0; k < 2; k++) {
                        double a = 6.283 * k / 2.0 + s.id + age * 0.02;
                        HieArt.mist(SINK, x + Math.cos(a) * (s.size + 0.5), y + 0.25 + age * 0.01, z + Math.sin(a) * (s.size + 0.5), 0.55 + age * 0.01,
                            0.4 * fade, age, s.id + k);
                    }
                }
                case HieMoves.SLASH -> {
                    // the crescent's middle out along the look, round the cut's own axis
                    double ax = s.dir.f_82479_;
                    double ay = s.dir.f_82480_;
                    double az = s.dir.f_82481_;
                    FireMesh.basis(ax, ay, az, BASIS);
                    double lx = s.b.f_82479_;
                    double ly = s.b.f_82480_;
                    double lz = s.b.f_82481_;
                    double start = Math.atan2(lx * BASIS[3] + ly * BASIS[4] + lz * BASIS[5], lx * BASIS[0] + ly * BASIS[1] + lz * BASIS[2]) - 1.1;
                    HieArt.slash(SINK, x, y, z, ax, ay, az, start, s.size, age, s.id);
                }
                default -> {
                }
            }
        }
        // the orb gathering, the iced fist: on the posed fist (one's own in first person: in the hand's pass)
        for (Map.Entry<Integer, Long> e : GATHERS.entrySet()) {
            Vec3 fist = fist(level, e.getKey(), partial);
            if (fist != null) {
                HieArt.gather(SINK, fist.f_82479_ - cx, fist.f_82480_ - cy, fist.f_82481_ - cz, HieMoves.ORB_RADIUS, clock - e.getValue() + partial,
                    e.getKey());
            }
        }
        for (Map.Entry<Integer, Long> e : COATS.entrySet()) {
            Vec3 fist = fist(level, e.getKey(), partial);
            if (fist != null && level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 look = player.m_20252_(partial);
                HieArt.coat(SINK, fist.f_82479_ - cx, fist.f_82480_ - cy, fist.f_82481_ - cz, look.f_82479_, look.f_82480_, look.f_82481_, 1.0,
                    clock - e.getValue() + partial, e.getKey());
            }
        }
    }

    static final double[] BASIS = new double[6];

    /** {@code id}'s right fist this frame in the world (null in one's own first person: the hand's pass draws it there). */
    static Vec3 fist(ClientLevel level, int id, float partial) {
        if (!(level.m_6815_(id) instanceof Player player) || firstPerson(id)) {
            return null;
        }
        Vec3 fist = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
        if (fist == null) {
            Vec3 look = player.m_20252_(partial);
            fist = player.m_20299_(partial).m_82549_(look.m_82490_(0.8)).m_82520_(0, -0.3, 0);
        }
        return fist;
    }

    static void clear() {
        SPOTS.clear();
        BALLS.clear();
        SHEETS.clear();
        ROADS.clear();
        FROZEN.clear();
        FORMS.clear();
        GATHERS.clear();
        COATS.clear();
        SABERS.clear();
        move = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
