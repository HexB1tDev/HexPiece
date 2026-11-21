package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.client.LoopSound;
import com.hexhaki.piece.haki.ConquerorMix;
import com.hexhaki.piece.server.ConquerorAura;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.9.5 (the owner: "There's one sound in particular when you release Conqueror's ... that is way too long, and it keeps playing like
 * way long after you let go of the key. I think it's a charge sound"): a Conqueror's charge, heard as one sound held from the moment G
 * is pressed to the moment it is let go ({@link ConquerorMix#CHARGE}; server/ConquerorAura sends {@link ConquerorAura#CHARGE} and
 * {@link ConquerorAura#CHARGE_END}). The build-up (a web recording's power charge) plays from the press; once it is nearly spent
 * ({@link #HOLD_AT}) a loop of its own crackling tail fades in under it and goes round for as long as G is held; when G is let go both
 * fade out within {@link LoopSound}'s fade (0.4 s), never cut off and never left playing.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ConquerorChargeSound {
    /** How loud the charge is held: about the original's own (its ceiling 0.44, 35 % quieter: 0.29). */
    static final float VOLUME = 0.3F;
    /** Ticks from the press when the held loop takes over: the build-up's recording runs 9 s, its last 0.6 s fading out. */
    public static final int HOLD_AT = 160;
    /** The longest a charge is held without word that it ended (a message lost): a minute. */
    static final int MOST = 1200;
    /** Over the body's feet (blocks): the chest. */
    static final double UP = 1.0;
    private static final Map<Integer, Charge> CHARGES = new HashMap<>();
    private static long clock;

    /** One charge being held: when it began, and whether its loop has taken over. */
    static final class Charge {
        final long since;
        boolean held;

        Charge(long since) {
            this.since = since;
        }
    }

    private ConquerorChargeSound() {
    }

    /** ConquerorSparksClient: a charge of the entity {@code user} began or ended. */
    public static void accept(int kind, int user) {
        if (kind == ConquerorAura.CHARGE) {
            start(user);
        } else {
            CHARGES.remove(user); // its sounds see it gone and fade out
        }
    }

    static void start(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        Entity entity = level == null ? null : level.m_6815_(id);
        if (entity == null) {
            return;
        }
        Charge charge = new Charge(clock);
        CHARGES.put(id, charge); // a charge still held from before ends here: its sounds fade
        LoopSound.play(ConquerorMix.CHARGE, entity, VOLUME, 1.0F, false, false, UP, () -> CHARGES.get(id) == charge);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.m_91104_()) {
            return; // paused: the sounds are too
        }
        clock++;
        if (CHARGES.isEmpty()) {
            return;
        }
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            CHARGES.clear();
            return;
        }
        for (Iterator<Map.Entry<Integer, Charge>> it = CHARGES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Charge> e = it.next();
            int id = e.getKey();
            Charge charge = e.getValue();
            long age = clock - charge.since;
            if (age > MOST) {
                it.remove();
            } else if (!charge.held && age >= HOLD_AT) {
                charge.held = true;
                Entity entity = level.m_6815_(id);
                if (entity != null) {
                    LoopSound.play(ConquerorMix.CHARGE_HOLD, entity, VOLUME, 1.0F, true, true, UP, () -> CHARGES.get(id) == charge);
                }
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CHARGES.clear();
    }
}
