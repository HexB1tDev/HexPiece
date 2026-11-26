package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.ClashArt;
import com.hexhaki.piece.haki.ConquerorClashRules;
import com.hexhaki.piece.network.C2SConquerorTap;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CClash;
import com.hexhaki.piece.network.S2CConquerorClash;
import com.hexhaki.piece.ui.ClashBarView;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.2: the Conqueror's Haki clash on the clients (server/ConquerorClash, haki/ConquerorClashRules: the owner's words). The collision:
 * the impact frame on the two who clashed (as every clash: client/fx/ClashFx), both colours' lightning tearing out of the meeting point
 * (the one Haki lightning: HakiBolts.clash, again and again while it holds), the pressure wave; then at the meeting point the white-hot
 * impact ringed in black, each half flaring in its fighter's colour ({@link ClashArt#impact}), each fighter's Haki pouring into it
 * ({@link ClashArt#stream}), the bubble's wind ({@link ClashArt#bubble}), all drawn with the lightning (HakiBolts.draw calls
 * {@link #draw}). For the two: the bar (ui/ClashBarView) and M1 taps sent instead of attacks.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ConquerorClashFx {
    /** A clash as this client knows it. */
    static final class Entry {
        S2CConquerorClash last;
        long started;
        double v;
        double shownV;
        int won;
        long wonAt = -1;
        long endAt = -1;
        long lastBurst = -100;
    }

    static final Map<Integer, Entry> ENTRIES = new HashMap<>();
    /** When the local player's last tap landed (client game tick), for the bar's pulse. */
    static long tappedAt = Long.MIN_VALUE;
    static double tapLight;

    private ConquerorClashFx() {
    }

    public static boolean any() {
        return !ENTRIES.isEmpty();
    }

    public static void accept(S2CConquerorClash m) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        long now = level.m_46467_();
        Entry entry = ENTRIES.get(m.id());
        if (m.phase() == S2CConquerorClash.GONE) {
            ENTRIES.remove(m.id());
            return;
        }
        if (entry == null) {
            entry = new Entry();
            entry.started = now - m.age();
            entry.shownV = m.v();
            ENTRIES.put(m.id(), entry);
            if (m.phase() == S2CConquerorClash.START) {
                collide(level, m, now);
            }
        }
        entry.last = m;
        entry.v = m.v();
        if (m.phase() == S2CConquerorClash.WON && entry.won == 0) {
            entry.won = m.v() >= 0 ? 1 : -1;
            entry.wonAt = now;
            Vec3 meet = meeting(m, m.v());
            HakiBolts.clash(level, meet.f_82479_, meet.f_82480_, meet.f_82481_, 1.3, 3.0F, m.colorA() & 0xFFFFFF, m.colorB() & 0xFFFFFF,
                m.id() * 31L + now);
        }
        if (m.phase() == S2CConquerorClash.END && entry.endAt < 0) {
            entry.endAt = now;
            Entity loser = level.m_6815_(entry.won > 0 ? m.b() : m.a());
            Vec3 at = loser != null ? loser.m_20182_().m_82520_(0.0, 1.2, 0.0) : meeting(m, entry.won);
            int winnerColor = (entry.won > 0 ? m.colorA() : m.colorB()) & 0xFFFFFF;
            HakiBolts.clash(level, at.f_82479_, at.f_82480_, at.f_82481_, 1.2, 3.0F, winnerColor, winnerColor, m.id() * 17L + now);
        }
    }

    /** The collision: the impact frame on the two, both colours tearing out of the meeting point, the shock. */
    static void collide(ClientLevel level, S2CConquerorClash m, long now) {
        Vec3 meet = meeting(m, 0.0);
        float dx = (float) (m.bx() - m.ax());
        float dz = (float) (m.bz() - m.az());
        float l = (float) Math.max(1.0E-6, Math.hypot(dx, dz));
        try {
            com.hexhaki.piece.client.fx.ClashFx.accept(new S2CClash(0x4343_0000 | (m.id() & 0xFFFF), meet.f_82479_, meet.f_82480_, meet.f_82481_,
                dx / l, dz / l, 3.0F, m.colorA() | 0xFF000000, m.colorB() | 0xFF000000, m.a(), m.b(), S2CClash.CLASH | S2CClash.OVERWHELMED));
        } catch (RuntimeException failure) {
            // the frame never stops the clash
        }
        HakiBolts.clash(level, meet.f_82479_, meet.f_82480_, meet.f_82481_, 1.3, 3.0F, m.colorA() & 0xFFFFFF, m.colorB() & 0xFFFFFF,
            m.id() * 7L + now);
    }

    /** Where the two Haki meet with the bar at {@code v} (world). */
    static Vec3 meeting(S2CConquerorClash m, double v) {
        double t = ConquerorClashRules.meeting(v);
        return new Vec3(m.ax() + (m.bx() - m.ax()) * t, m.ay() + (m.by() - m.ay()) * t + 1.3, m.az() + (m.bz() - m.az()) * t);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ENTRIES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            ENTRIES.clear();
            return;
        }
        long now = level.m_46467_();
        tapLight = Math.max(0.0, tapLight - 0.18);
        Iterator<Entry> it = ENTRIES.values().iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            S2CConquerorClash m = e.last;
            e.shownV += (e.v - e.shownV) * 0.35;
            if (e.endAt >= 0 && now - e.endAt > 30 || now - e.started > ConquerorClashRules.INTRO + ConquerorClashRules.GAME_MOST + 200) {
                it.remove();
                continue;
            }
            // both colours' lightning tearing out of the meeting point again and again while it holds
            if (e.endAt < 0 && now - e.lastBurst >= 9 && now - e.started >= 6) {
                e.lastBurst = now;
                Vec3 meet = meeting(m, e.shownV);
                HakiBolts.clash(level, meet.f_82479_, meet.f_82480_, meet.f_82481_, 1.3, 2.0F, m.colorA() & 0xFFFFFF, m.colorB() & 0xFFFFFF,
                    m.id() * 131L + now);
            }
        }
    }

    /** The clash this client's player is in, or null. */
    static Entry mine() {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null) {
            return null;
        }
        int self = minecraft.f_91074_.m_19879_();
        for (Entry e : ENTRIES.values()) {
            if (e.endAt < 0 && (e.last.a() == self || e.last.b() == self)) {
                return e;
            }
        }
        return null;
    }

    /** M1 in a clash is a tap of the tug, never an attack. */
    @SubscribeEvent
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        if (ENTRIES.isEmpty() || !event.isAttack()) {
            return;
        }
        Entry e = mine();
        if (e == null) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        Minecraft minecraft = Minecraft.m_91087_();
        long now = minecraft.f_91073_ == null ? 0 : minecraft.f_91073_.m_46467_();
        if (e.won == 0 && now - e.started >= ConquerorClashRules.INTRO) {
            PieceNetwork.toServer(new C2SConquerorTap());
            tappedAt = now;
            tapLight = 1.0;
            if (minecraft.f_91074_ != null && minecraft.f_91073_ != null) {
                Vec3 at = minecraft.f_91074_.m_20182_();
                minecraft.f_91073_.m_7785_(at.f_82479_, at.f_82480_ + 1.0, at.f_82481_,
                    com.hexhaki.piece.PieceSounds.event(com.hexhaki.piece.clash.ClashRules.SOUND), SoundSource.PLAYERS, 0.18F,
                    1.5F + (float) Math.random() * 0.2F, false);
            }
        }
    }

    /** Drawn with the Haki lightning (HakiBolts.draw), eye-centred: the impact, the two streams, the bubble. */
    static void draw(Vec3 cam, ClientLevel level, float partial, double shine, BoltMesh.Sink sink) {
        long now = level.m_46467_();
        for (Entry e : ENTRIES.values()) {
            S2CConquerorClash m = e.last;
            if (e.endAt >= 0) {
                continue;
            }
            double age = now - e.started + partial;
            Vec3 meet = meeting(m, e.shownV);
            double mx = meet.f_82479_ - cam.f_82479_;
            double my = meet.f_82480_ - cam.f_82480_;
            double mz = meet.f_82481_ - cam.f_82481_;
            double ax = m.ax() - meet.f_82479_;
            double ay = m.ay() + 1.3 - meet.f_82480_;
            double az = m.az() - meet.f_82481_;
            double al = Math.max(1.0E-6, Math.sqrt(ax * ax + ay * ay + az * az));
            ClashArt.impact(mx, my, mz, ax / al, ay / al, az / al, age, m.colorA(), m.colorB(), shine, sink);
            double[] to = {mx, my, mz};
            ClashArt.stream(new double[] {m.ax() - cam.f_82479_, m.ay() + 1.3 - cam.f_82480_, m.az() - cam.f_82481_}, to, age, m.colorA(), shine,
                sink);
            ClashArt.stream(new double[] {m.bx() - cam.f_82479_, m.by() + 1.3 - cam.f_82480_, m.bz() - cam.f_82481_}, to, age, m.colorB(), shine,
                sink);
            double[] middle = {(m.ax() + m.bx()) / 2.0 - cam.f_82479_, (m.ay() + m.by()) / 2.0 + 1.0 - cam.f_82480_,
                (m.az() + m.bz()) / 2.0 - cam.f_82481_};
            ClashArt.bubble(middle, m.radius(), age, shine, sink);
            // the pressure wave of the collision, standing square to the line between them, its foot racing out over the ground
            ClashArt.wave(mx, my, mz, ax / al, ay / al, az / al, 1.3, m.radius() * 1.6, age, m.colorA(), m.colorB(), shine, sink);
        }
    }

    /** The bar, for the two who clash. */
    static void hud(net.minecraft.client.gui.GuiGraphics graphics, int width, int height, float partial) {
        Entry e = mine();
        Minecraft minecraft = Minecraft.m_91087_();
        Entry ended = null;
        if (e == null && minecraft.f_91074_ != null) {
            int self = minecraft.f_91074_.m_19879_();
            for (Entry x : ENTRIES.values()) {
                if (x.endAt >= 0 && (x.last.a() == self || x.last.b() == self)) {
                    ended = x;
                }
            }
        }
        Entry show = e != null ? e : ended;
        if (show == null || minecraft.f_91073_ == null || minecraft.f_91074_ == null) {
            return;
        }
        S2CConquerorClash m = show.last;
        boolean meA = m.a() == minecraft.f_91074_.m_19879_();
        long now = minecraft.f_91073_.m_46467_();
        double age = now - show.started + partial;
        ClashBarView.State s = new ClashBarView.State();
        s.mine = meA ? show.shownV : -show.shownV;
        s.myColor = (meA ? m.colorA() : m.colorB()) | 0xFF000000;
        s.foeColor = (meA ? m.colorB() : m.colorA()) | 0xFF000000;
        s.myName = minecraft.f_91074_.m_36316_().getName();
        Entity foe = minecraft.f_91073_.m_6815_(meA ? m.b() : m.a());
        s.foeName = foe instanceof net.minecraft.world.entity.player.Player p ? p.m_36316_().getName() : "?";
        s.shown = Math.min(1.0, Math.max(0.0, (age - 10.0) / (ConquerorClashRules.INTRO - 10.0)));
        if (show.endAt >= 0) {
            s.shown = Math.max(0.0, 1.0 - (now - show.endAt + partial) / 30.0);
        }
        s.age = age;
        s.tap = tapLight;
        s.left = (int) Math.max(0, Math.ceil((ConquerorClashRules.INTRO + ConquerorClashRules.GAME_MOST - age) / 20.0));
        s.won = show.won == 0 ? 0 : (show.won > 0) == meA ? 1 : -1;
        ClashBarView.draw(new com.hexhaki.piece.client.ui.GuiCanvas(graphics, minecraft.f_91062_, null), width, height, s);
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        public static void registerConquerorClashOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("hexpiece_conqueror_clash", (gui, graphics, partialTick, width, height) -> hud(graphics, width, height, partialTick));
        }
    }
}
