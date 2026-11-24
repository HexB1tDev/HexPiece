package com.hexhaki.piece.client;

import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.network.C2SStyleAction;
import com.hexhaki.piece.network.C2SSword;
import com.hexhaki.piece.sword.KatanaItem;
import net.minecraft.client.gui.screens.Screen;
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
 * mode (2.1.3: a kit fruit's M1 steps aside in combat mode, {@link PieceHooks#fruitKeysActive}): M1 throws the string (at a block too: to mine, leave combat
 * mode), the right click the signature (not at a block: doors and chests still open), Z and X the specials (2.7.1; Y and U before), C, V and
 * B the show's own moves (2.8.4). The server decides what each press does.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class StyleInput {
    private StyleInput() {
    }

    /** 2.1.4: the Swordsman's right button is held (a charge asked for, its release not yet sent). */
    private static boolean charging;

    static boolean ready(Minecraft minecraft) {
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91080_ != null) {
            return false;
        }
        PieceProfile self = PieceClient.self();
        boolean hand = player.m_21205_().m_41619_() || self.style().sword() && KatanaItem.of(player.m_21205_()) != null;
        return hand && CombatMode.on() && self.chosen() && self.style().choice() && self.style().armed() && !GearClientState.devilFruitMode();
    }

    static boolean swordsman() {
        return PieceClient.self().style().sword();
    }

    /**
     * 2.3.3 (the owner: "just because you're a swordsman, it doesn't mean you're required a fucking sword. You can use normal
     * Minecraft hits when you don't have a sword on"): a Swordsman with no katana anywhere in the inventory (as the server last said:
     * {@link com.hexhaki.piece.client.sword.SwordClient#katana}) fights with the game's own punch; only the other styles' M1s are
     * always their own.
     */
    static boolean swordless(Minecraft minecraft) {
        return swordsman() && com.hexhaki.piece.client.sword.SwordClient.katana(minecraft.f_91074_.m_19879_()) == null;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!ready(minecraft) || swordless(minecraft)) {
            return; // the game's own punch and use
        }
        HitResult aimed = minecraft.f_91077_;
        boolean atBlock = aimed != null && aimed.m_6662_() == HitResult.Type.BLOCK;
        if (event.isAttack()) {
            event.setCanceled(true);
            // 2.3.5: the cut itself is seen in first person (SwordClient.swing): no vanilla hand bob over it
            event.setSwingHand(false);
            if (swordsman()) {
                // 2.1.4: one press, one cut asked for; Ctrl + M1 the quick draw
                PieceNetwork.toServer(new C2SSword(Screen.m_96637_() ? C2SSword.QUICK_DRAW : C2SSword.PRESS));
            } else {
                PieceNetwork.toServer(new C2SStyleAction(Slot.M1_1.ordinal()));
            }
        } else if (event.isUseItem()) {
            if (atBlock) {
                return; // doors, chests and the like still open (and never start a charge)
            }
            event.setCanceled(true);
            event.setSwingHand(false);
            if (swordsman()) {
                if (!charging) {
                    charging = true; // held: the release is sent when the button is let go (tick)
                    PieceNetwork.toServer(new C2SSword(C2SSword.CHARGE));
                }
            } else {
                PieceNetwork.toServer(new C2SStyleAction(Slot.HEAVY.ordinal()));
            }
        }
    }

    private static boolean ironZ;
    private static boolean ironN;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean ready = ready(minecraft);
        if (charging) {
            if (minecraft.f_91080_ != null || minecraft.f_91074_ == null) {
                charging = false; // a screen opened: the charge is given up, never released by itself
                PieceNetwork.toServer(new C2SSword(C2SSword.CANCEL));
            } else if (!minecraft.f_91066_.f_92095_.m_90857_()) {
                charging = false;
                PieceNetwork.toServer(new C2SSword(C2SSword.RELEASE));
            }
        }
        // 3.5.8: Iron Fist's Z is held (Meteor Fist) and N is its ultimate (Galaxy Impact): pressed and let go are sent
        boolean iron = ready && PieceClient.self().style() == com.hexhaki.piece.style.FightingStyle.BRAWLER;
        boolean zHeld = iron && PieceKeys.SPECIAL_A.m_90857_();
        if (zHeld != ironZ) {
            if (zHeld) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_A.ordinal()));
            } else if (minecraft.f_91074_ != null) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_A.ordinal() + com.hexhaki.piece.server.IronFist.RELEASE));
            }
            ironZ = zHeld;
        }
        boolean nHeld = iron && PieceKeys.FRUIT_N.m_90857_();
        if (nHeld && !ironN) {
            PieceNetwork.toServer(new C2SStyleAction(com.hexhaki.piece.server.IronFist.N_KEY));
        }
        ironN = nHeld;
        while (PieceKeys.SPECIAL_A.m_90859_()) {
            if (ready && !iron) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_A.ordinal()));
            }
        }
        while (PieceKeys.SPECIAL_B.m_90859_()) {
            if (ready) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.SPECIAL_B.ordinal()));
            }
        }
        // 2.8.4: the show's own moves of each style on C, V and B (the Swordsman's keys stay the sword's)
        boolean extras = ready && !swordsman();
        while (PieceKeys.EXTRA_C.m_90859_()) {
            if (extras) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.EXTRA_C.ordinal()));
            }
        }
        while (PieceKeys.EXTRA_V.m_90859_()) {
            if (extras) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.EXTRA_V.ordinal()));
            }
        }
        while (PieceKeys.EXTRA_B.m_90859_()) {
            if (extras) {
                PieceNetwork.toServer(new C2SStyleAction(Slot.EXTRA_B.ordinal()));
            }
        }
    }
}
