package com.hexhaki.piece.client.gear;

import com.hexhaki.piece.gear.ToonForce;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.1: the Nika fruit's Toon Force on every client (gear/ToonForce has the beats and the spring). patch/PiecePatch.toonClips makes the
 * original's {@code HakiAnimations.playRemote(id, clip, priority)} (every clip any player starts) tell {@link #clip} first; each tick
 * the clip's beats kick that player's spring, and the body is drawn squashed or stretched by it round the feet (third person only: not
 * one's own first-person pass).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaToon {
    static final class Toon {
        ToonForce.Score score;
        double start;
        /** The clip's ticks already played. */
        double played;
        final double[] spring = new double[2];
        double before;
    }

    static final Map<Integer, Toon> TOONS = new HashMap<>();
    /** Whether each Pre pushed a pose its Post pops. */
    private static final Deque<Boolean> FRAMES = new ArrayDeque<>();

    private NikaToon() {
    }

    /** At the head of HakiAnimations.playRemote: a clip started on player {@code id} (a Nika clip's beats begin; any other ends them). */
    public static void clip(int id, String name) {
        com.hexhaki.piece.client.sword.WifiPull.clip(id, name); // 3.5.9: WiFi Haki's blade eased out of its saya while its clip plays
        ToonForce.Score score = name == null ? null : ToonForce.score(name);
        Toon toon = TOONS.get(id);
        if (score == null) {
            if (toon != null) {
                toon.score = null;
            }
            return;
        }
        if (toon == null) {
            toon = new Toon();
            TOONS.put(id, toon);
        }
        toon.score = score;
        toon.start = clock();
        // a beat on the clip's first tick lands at once
        toon.played = -1.0E-6;
    }

    static double clock() {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TOONS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            TOONS.clear();
            return;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        double now = clock();
        Iterator<Toon> it = TOONS.values().iterator();
        while (it.hasNext()) {
            Toon toon = it.next();
            double kick = 0.0;
            if (toon.score != null) {
                double t = now - toon.start;
                kick = toon.score.kicks(toon.played, t);
                toon.played = t;
                if (!toon.score.loop() && t > toon.score.end() + 2) {
                    toon.score = null;
                }
            }
            toon.before = toon.spring[0];
            ToonForce.step(toon.spring, kick);
            if (toon.score == null && ToonForce.still(toon.spring) && Math.abs(toon.before) < 0.003) {
                it.remove();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pre(RenderPlayerEvent.Pre event) {
        Toon toon = TOONS.get(event.getEntity().m_19879_());
        boolean pushed = false;
        if (toon != null && !FirstPersonMode.isFirstPersonPass()) {
            double x = toon.before + (toon.spring[0] - toon.before) * event.getPartialTick();
            if (Math.abs(x) > 0.003) {
                float wide = (float) ToonForce.wide(x);
                event.getPoseStack().m_85836_();
                event.getPoseStack().m_85841_(wide, (float) ToonForce.tall(x), wide);
                pushed = true;
            }
        }
        FRAMES.push(pushed);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void post(RenderPlayerEvent.Post event) {
        Boolean pushed = FRAMES.poll();
        if (pushed != null && pushed) {
            event.getPoseStack().m_85849_();
        }
    }
}
