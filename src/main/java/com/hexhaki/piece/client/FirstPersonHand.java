package com.hexhaki.piece.client;

import com.hexhaki.piece.style.HandMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.4 (the owner: "We need to give them better first-person simple animations ... for every move ... Quick"; "All three": the Light,
 * Gura and Magma fruits): while the local player plays one of their moves in first person, the game's own right hand (and what is in it)
 * is moved by style/HandMoves before the game draws it: shifted and turned about the hand, eased from wherever it was when the move began.
 * Nothing is drawn here (a hand of its own did not show on the owner's device in 3.0.5): only the pose the game draws its hand in. The
 * main hand is drawn first and the off hand after it on the same stack, so the off hand gets the move taken back off.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FirstPersonHand {
    private static final double[] NOW = new double[6];
    private static final double[] TARGET = new double[6];
    private static final double[] FROM = new double[6];
    private static final double[] APPLIED = new double[6];
    private static boolean applied;
    private static String clip;
    private static long start;
    /** How long a move eases in from the hand's last pose (ticks). */
    static final double BLEND = 2.0;

    private FirstPersonHand() {
    }

    /**
     * The hand's move this frame for the local player (into {@link #NOW}); false when none (not first person, or no fruit move playing).
     */
    static boolean current(float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91066_.m_92176_() != CameraType.FIRST_PERSON) {
            return false;
        }
        String name = FirstPersonLight.current(player.m_19879_());
        String playing = com.hexhaki.fruit.client.FruitAnimations.clip(player);
        if (name == null || !name.equals(playing) || !HandMoves.moves(name)) {
            java.util.Arrays.fill(NOW, 0.0);
            clip = null;
            return false;
        }
        long began = FirstPersonLight.started(player.m_19879_());
        if (!name.equals(clip) || began != start) {
            // a new move: ease in from where the hand was
            System.arraycopy(NOW, 0, FROM, 0, 6);
            clip = name;
            start = began;
        }
        double ticks = FirstPersonLight.now() - began + partial;
        HandMoves.at(name, ticks, TARGET);
        HandMoves.blend(FROM, TARGET, ticks / BLEND, NOW);
        return true;
    }

    /** A point of the hand's view space moved as the hand is this frame (the fist's effects follow the hand): into {@code out}. */
    public static void move(double[] v, float partial, double[] out) {
        if (current(partial)) {
            HandMoves.apply(NOW, v, out);
        } else {
            System.arraycopy(v, 0, out, 0, 3);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hand(RenderHandEvent event) {
        PoseStack pose = event.getPoseStack();
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            applied = false;
            if (!current(event.getPartialTick())) {
                return;
            }
            System.arraycopy(NOW, 0, APPLIED, 0, 6);
            push(pose, APPLIED);
            applied = true;
        } else if (applied) {
            pop(pose, APPLIED);
            applied = false;
        }
    }

    /** The move put on the stack: shifted, then turned about the hand (yaw, pitch, roll). */
    static void push(PoseStack pose, double[] m) {
        double[] p = HandMoves.PIVOT;
        pose.m_85837_(p[0] + m[0], p[1] + m[1], p[2] + m[2]);
        pose.m_252781_(Axis.f_252436_.m_252977_((float) m[4]));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) m[3]));
        pose.m_252781_(Axis.f_252403_.m_252977_((float) m[5]));
        pose.m_85837_(-p[0], -p[1], -p[2]);
    }

    /** The move taken back off the stack, step by step in the other order. */
    static void pop(PoseStack pose, double[] m) {
        double[] p = HandMoves.PIVOT;
        pose.m_85837_(p[0], p[1], p[2]);
        pose.m_252781_(Axis.f_252403_.m_252977_((float) -m[5]));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) -m[3]));
        pose.m_252781_(Axis.f_252436_.m_252977_((float) -m[4]));
        pose.m_85837_(-p[0] - m[0], -p[1] - m[1], -p[2] - m[2]);
    }
}
