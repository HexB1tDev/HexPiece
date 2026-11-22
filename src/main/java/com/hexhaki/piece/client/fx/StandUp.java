package com.hexhaki.piece.client.fx;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.2 (the owner: "whenever you sneak on the floor while right clicking, your body completely breaks. Your torso moves forward. Like,
 * it should just cancel the sneak animation"): while a player plays one of the fruits' moves (the Gura fruit's and the Light fruit's
 * clips on the original's fruit layer, client FruitAnimations), the game's sneaking pose is not drawn: the model stands (its crouch flag
 * cleared after the renderer set it, and the eighth of a block the renderer drops a sneaking body put back), so the move's own pose is
 * not bent over again by the crouch. Only the look: the player still sneaks (slow, at the edge of blocks).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class StandUp {
    /** How far down the game draws a sneaking player (PlayerRenderer's offset). */
    static final double CROUCH_DROP = 0.125;
    private static final Set<Integer> RAISED = new HashSet<>();

    private StandUp() {
    }

    /** Whether {@code player} is in one of the fruits' moves now. */
    static boolean moving(Player player) {
        String clip = com.hexhaki.fruit.client.FruitAnimations.clip(player);
        return clip != null && (clip.startsWith("gura_") || clip.startsWith("pika_") || clip.startsWith("magu_"));
    }

    /** Last (after anything that cancels the drawing): the crouch taken off a player in a move. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void before(RenderPlayerEvent.Pre event) {
        if (!(event.getRenderer().m_7200_() instanceof PlayerModel<?> model) || !model.f_102817_ || !moving(event.getEntity())) {
            return;
        }
        model.f_102817_ = false;
        event.getPoseStack().m_85837_(0.0, CROUCH_DROP, 0.0);
        RAISED.add(event.getEntity().m_19879_());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void after(RenderPlayerEvent.Post event) {
        if (RAISED.remove(event.getEntity().m_19879_())) {
            event.getPoseStack().m_85837_(0.0, -CROUCH_DROP, 0.0);
        }
    }
}
