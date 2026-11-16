package com.hexhaki.piece.client;

import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.network.C2SStyleAction;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.style.Slot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: a fighting style's inputs. 2.1.2: only in combat mode (R, {@link CombatMode}), with an empty main hand, outside Devil Fruit
 * mode and with no fruit kit active (those keep M1 as the original had it): M1 throws the string (at a block too: to mine, leave combat
 * mode), the right click the signature (not at a block: doors and chests still open), Y and U the specials. The server decides what each press does.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class StyleInput {
    private StyleInput() {
    }

    static boolean ready(Minecraft minecraft) {
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91080_ != null || !player.m_21205_().m_41619_()) {
            return false;
        }
        PieceProfile self = PieceClient.self();
        return CombatMode.on() && self.chosen() && self.style().choice() && !FruitClient.active() && !GearClientState.devilFruitMode();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!ready(minecraft)) {
            return;
        }
        HitResult aimed = minecraft.f_91077_;
        boolean atBlock = aimed != null && aimed.m_6662_() == HitResult.Type.BLOCK;
        if (event.isAttack()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            PieceNetwork.toServer(new C2SStyleAction(Slot.M1_1.ordinal()));
        } else if (event.isUseItem()) {
            if (atBlock) {
                return; // doors, chests and the like still open
            }
            event.setCanceled(true);
            event.setSwingHand(false);
            PieceNetwork.toServer(new C2SStyleAction(Slot.HEAVY.ordinal()));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean ready = ready(minecraft);
        while (PieceKeys.SPECIAL_A.m_90859_()) {
            if (ready) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_A.ordinal()));
            }
        }
        while (PieceKeys.SPECIAL_B.m_90859_()) {
            if (ready) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_B.ordinal()));
            }
        }
    }
}
