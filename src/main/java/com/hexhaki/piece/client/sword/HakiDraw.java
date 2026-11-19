package com.hexhaki.piece.client.sword;

import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.piece.client.haki.HakiBolts;
import com.hexhaki.piece.client.haki.RyouColors;
import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.DrawCrackle;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.sword.SwordRules;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 2.1.5: Haki lightning off a Swordsman's katana (the owner: "if you have Haki activated, a cool interaction you could do, like when you
 * unsheath the blade, like lightning comes out of it like slowly when you're unsheathing it ... Rayleigh does against Blackbeard ...
 * That's just an idea ... come up with your own little things").
 *
 * <p>With Armament Haki on, the katana answers it:
 * <ul>
 *   <li><b>the draw</b>: as the blade slides out of its saya, lightning spits out of the saya's mouth and leaps along the bared steel,
 *       more of it the more is bared; the moment the blade comes free, a flash of it runs the whole blade;</li>
 *   <li><b>the charge</b> (the right button held): a crackle along the blade that grows with the charge, so everyone sees how full it
 *       is;</li>
 *   <li><b>the release</b>: a last burst along the blade as the cut goes;</li>
 *   <li><b>the sheathing</b>: a few sparks at the mouth as the steel goes home;</li>
 *   <li>with <b>Ryou</b> up the blade hums with a faint crackle all the time it is drawn.</li>
 * </ul>
 * 2.2.4: every crackle is in the player's own Ryou colour (the owner: "all lightnings in the game are to reflect the same color as
 * RYOU").
 * <ul>
 * </ul>
 * The bolts are {@link DrawCrackle}'s, drawn by {@link HakiBolts#drawInFrame} in the blade's own frame, so they ride on it through
 * every cut, in first and third person; a soft zap (the original's own Armament zap) goes with every third set.
 */
public final class HakiDraw {
    /** The blade, guard to tip, in blocks. */
    static final double BLADE = 20.5 * KatanaRenderer.SCALE;

    /** 2.6.2: the bare blade's length (blocks) for {@code player}'s sword as it is drawn ({@link KatanaRenderer#px}). */
    static double blade(AbstractClientPlayer player) {
        return 20.5 * KatanaRenderer.px(SwordClient.katana(player.m_19879_()));
    }
    private static final java.util.Map<Integer, Long> HEARD = new java.util.HashMap<>();

    private HakiDraw() {
    }

    /** 0: no Armament; 1: Armament; 2: Armament with Ryou. */
    static int haki(AbstractClientPlayer player) {
        if (player == Minecraft.m_91087_().f_91074_) {
            return !HakiClientState.armamentOn ? 0 : HakiClientState.acocOn ? 2 : 1;
        }
        RemoteHakiStates.State state = RemoteHakiStates.get(player.m_19879_());
        return state == null || !state.armament() ? 0 : state.acoc() ? 2 : 1;
    }

    /** 2.2.4 (the owner: "all lightnings in the game are to reflect the same color as RYOU"): the player's Ryou colour, Ryou up or not. */
    static int color(AbstractClientPlayer player, int haki) {
        return RyouColors.orNeutral(player.m_19879_());
    }

    /** In the blade's frame while it slides along the saya, {@code out} model px out of it. */
    static void sliding(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, int anim, float ticks, float out, float partial) {
        int haki = haki(player);
        if (haki == 0) {
            return;
        }
        double bared = out * KatanaRenderer.px(SwordClient.katana(player.m_19879_()));
        boolean drawing = anim != SwordAnims.SHEATHE;
        double strength = (drawing ? 0.6 + 0.6 * Math.min(1.0, bared / blade(player)) : 0.5) * (haki == 2 ? 1.25 : 1.0);
        crackle(pose, buffers, player, ticks, 0.0, bared, bared, strength, color(player, haki), partial);
    }

    /** In the blade's frame in the hand. */
    static void held(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, int anim, float ticks, float partial) {
        int haki = haki(player);
        if (haki == 0) {
            return;
        }
        double strength = 0.0;
        String name = SwordAnims.name(anim, SwordClient.katana(player.m_19879_()));
        float[] noto = name == null ? null : BladePaths.notoOut(name);
        if (noto != null && noto.length >= 2 && anim != SwordAnims.SHEATHE) {
            float free = noto[noto.length - 2];
            if (ticks >= free && ticks < free + 5) {
                strength = 1.4 * (1.0 - (ticks - free) / 6.0); // the flash as the blade comes free
            }
        } else if (name != null && BladePaths.drawTick(name) >= 0 && ticks >= BladePaths.drawTick(name) && ticks < BladePaths.drawTick(name) + 5) {
            strength = 1.2;
        }
        if (anim == SwordAnims.CHARGE || anim == SwordAnims.CHARGE_DRAW) {
            strength = Math.max(strength, 0.25 + 1.25 * Math.min(1.0, ticks / SwordRules.CHARGE_MAX));
        }
        if (anim == SwordAnims.RELEASE && ticks < 5) {
            strength = Math.max(strength, 1.6);
        }
        if (haki == 2) {
            strength = Math.max(strength, 0.3) * 1.15;
        }
        if (strength > 0.0) {
            crackle(pose, buffers, player, ticks, 0.0, blade(player), Double.NaN, strength, color(player, haki), partial);
        }
    }

    static void crackle(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, float ticks, double from, double to, double mouth,
                        double strength, int color, float partial) {
        long seed = player.m_19879_() * 0x2545F4914F6CDD1DL;
        long window = (long) Math.floor(ticks / DrawCrackle.WINDOW);
        Long heard = HEARD.get(player.m_19879_());
        if (window % 3 == 0 && (heard == null || heard != window)) {
            // a soft zap every third set (once a player, though the layer may draw twice a frame)
            HEARD.put(player.m_19879_(), window);
            HakiBolts.zap(player.m_9236_(), player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(),
                (float) (0.05 + 0.07 * Math.min(1.5, strength)));
        }
        List<Bolt> bolts = new ArrayList<>();
        List<Double> ages = new ArrayList<>();
        for (long w = Math.max(0, window - 2); w <= window; w++) {
            for (Bolt bolt : DrawCrackle.set(seed, w, from, to, mouth, strength, color)) {
                double age = ticks - w * DrawCrackle.WINDOW - bolt.delay;
                if (age >= 0 && age <= bolt.duration) {
                    bolts.add(bolt);
                    ages.add(age);
                }
            }
        }
        double[] at = new double[ages.size()];
        for (int i = 0; i < at.length; i++) {
            at[i] = ages.get(i);
        }
        HakiBolts.drawInFrame(pose, buffers, bolts, at, partial);
    }
}
