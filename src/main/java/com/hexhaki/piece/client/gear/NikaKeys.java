package com.hexhaki.piece.client.gear;

import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.devilfruit.GearMovement;
import com.hexhaki.network.GearAction;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.network.msg.C2SGearAction;
import com.hexhaki.piece.client.PieceKeys;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * 3.2.3 (the owner: "make sure that it adapts the new keys ... All of the moves, literally all of them, even transformation to Z, and
 * all the others following the transformation, X, C, V, B, N, M ... the hotkey versions should replace the slots still as they do";
 * asked, "Star Gun takes X"): the Nika fruit's keys in Devil Fruit mode, as every fruit's. The original's {@code GearKeys.tick} read the
 * Haki keys (R, J, K, L, G, H, B and the unbound one); patch/PiecePatch.nikaKeys makes it ask {@link #tick} first, which does it all.
 * <ul>
 *   <li>Z: Gear 5 (transform, and back).</li>
 *   <li>X: Gum-Gum Rocket (held); with Haki on in Gear 5: Star Gun.</li>
 *   <li>C: Gum-Gum Whip; in Gear 5 Goof Ball (held), with Haki on Dawn Whip (3.4.4: server/NikaWhip).</li>
 *   <li>V: Gum-Gum Gatling (held).</li>
 *   <li>B: Gum-Gum Gigante (held; with Haki on in Gear 5 the server charges the Bajrang Gun).</li>
 *   <li>N: Coil Spring (held; with Haki on in Gear 5 the lightning lance).</li>
 *   <li>M: Rubber World (held).</li>
 *   <li>Left click: the stretching punch (Dawn Pistol with Haki on in Gear 5). Shift + Space stays Sky Bounce (GearKeys.movementInput).</li>
 * </ul>
 * The keys are the fruit's quick-slot keys as the player bound them (Z {@code SLOT_6}, X {@code MODE}, C {@code SLOT_7}, V
 * {@code SLOT_8}, B {@code STAR_GUN}), N {@link PieceKeys#FRUIT_N} and M {@link PieceKeys#FRUIT_M}.
 */
public final class NikaKeys {
    private static boolean xDown;
    private static boolean cDown;
    private static boolean xStar;
    private static boolean gatlingDown;
    private static boolean giantDown;
    private static boolean coilDown;
    private static boolean rubberDown;
    private static boolean attackDown;

    private NikaKeys() {
    }

    /** Whether the moves are their Haki versions now: as the server's {@code GearServerController.dawn}, Gear 5 with Armament or Ryou on. */
    public static boolean haki() {
        return GearClientState.gearFive && (HakiClientState.armamentOn || HakiClientState.acocOn);
    }

    static void send(GearAction action) {
        if (Minecraft.m_91087_().f_91074_ != null) { // in a world (the original asked for the connection: one comes with the player)
            HakiNetwork.CHANNEL.sendToServer(new C2SGearAction(action));
        }
    }

    static KeyMapping z() {
        return HakiKeys.SLOT_6;
    }

    static KeyMapping x() {
        return HakiKeys.MODE;
    }

    static KeyMapping c() {
        return HakiKeys.SLOT_7;
    }

    static KeyMapping v() {
        return HakiKeys.SLOT_8;
    }

    static KeyMapping b() {
        return HakiKeys.STAR_GUN;
    }

    /** The original's {@code GearKeys.tick} in Devil Fruit mode: every key read here instead (true: the original's own does nothing). */
    public static boolean tick(Minecraft minecraft) {
        com.hexhaki.patch.GiantGrabClient.tick();
        GearMovement.tick();
        if (minecraft.f_91074_ == null) {
            releaseHeld();
            return true;
        }
        if (GearClientState.transforming()) {
            releaseHeld();
            drain();
            return true;
        }
        while (z().m_90859_()) {
            send(GearAction.GEAR_FIVE_TOGGLE);
        }
        // C: the whip on the press (in Gear 5 the server makes it Goof Ball or Dawn Whip); 3.4.4: Goof Ball is held, so its let go is
        // sent too (network/C2SNikaWhip; the server ignores it for anything else)
        boolean c = c().m_90857_();
        boolean cTapped = false;
        while (c().m_90859_()) {
            cTapped = true;
        }
        if (c && !cDown) {
            send(GearAction.WHIP);
        } else if (!c && cDown) {
            letGoWhip();
        } else if (!c && cTapped) {
            send(GearAction.WHIP);
            letGoWhip();
        }
        cDown = c;
        // X: Rocket while held, or (Haki on in Gear 5) Star Gun on the press
        boolean x = x().m_90857_();
        boolean tapped = false;
        while (x().m_90859_()) {
            tapped = true;
        }
        if (x && !xDown) {
            xStar = haki();
            send(xStar ? GearAction.STAR_GUN : GearAction.ROCKET_START);
        } else if (!x && xDown) {
            if (!xStar) {
                send(GearAction.ROCKET_RELEASE);
            }
        } else if (!x && tapped) {
            // pressed and let go within one tick
            if (haki()) {
                send(GearAction.STAR_GUN);
            } else {
                send(GearAction.ROCKET_START);
                send(GearAction.ROCKET_RELEASE);
            }
        }
        xDown = x;
        gatlingDown = held(v().m_90857_(), gatlingDown, GearAction.GATLING_START, GearAction.GATLING_RELEASE);
        giantDown = held(b().m_90857_(), giantDown, GearAction.GIANT_START, GearAction.GIANT_RELEASE);
        coilDown = held(PieceKeys.FRUIT_N.m_90857_(), coilDown, GearAction.COIL_START, GearAction.COIL_RELEASE);
        rubberDown = held(PieceKeys.FRUIT_M.m_90857_(), rubberDown, GearAction.RUBBER_WORLD_START, GearAction.RUBBER_WORLD_RELEASE);
        while (PieceKeys.FRUIT_M.m_90859_()) {
            // only whether M is held matters
        }
        boolean attack = minecraft.f_91066_.f_92096_.m_90857_();
        if (attack && !attackDown) {
            send(GearAction.PISTOL);
        }
        attackDown = attack;
        return true;
    }

    static void letGoWhip() {
        if (Minecraft.m_91087_().f_91074_ != null) {
            com.hexhaki.piece.network.PieceNetwork.toServer(new com.hexhaki.piece.network.C2SNikaWhip());
        }
    }

    /** A held move: its start on the press, its let go on the release; returns whether it is held now. */
    static boolean held(boolean now, boolean was, GearAction start, GearAction release) {
        if (now && !was) {
            send(start);
        } else if (!now && was) {
            send(release);
        }
        return now;
    }

    /** Every held move let go (the original's {@code GearKeys.releaseHeld} asks this first). */
    public static void releaseHeld() {
        if (xDown && !xStar) {
            send(GearAction.ROCKET_RELEASE);
        }
        if (gatlingDown) {
            send(GearAction.GATLING_RELEASE);
        }
        if (giantDown) {
            send(GearAction.GIANT_RELEASE);
        }
        if (coilDown) {
            send(GearAction.COIL_RELEASE);
        }
        if (rubberDown) {
            send(GearAction.RUBBER_WORLD_RELEASE);
        }
        if (cDown) {
            letGoWhip();
        }
        cDown = false;
        xDown = false;
        gatlingDown = false;
        giantDown = false;
        coilDown = false;
        rubberDown = false;
        attackDown = false;
    }

    /** Every press waiting on the keys thrown away (the original's {@code GearKeys.drain} asks this first). */
    public static void drain() {
        for (KeyMapping key : new KeyMapping[] {z(), x(), c(), v(), b(), PieceKeys.FRUIT_N, PieceKeys.FRUIT_M}) {
            while (key.m_90859_()) {
                // dropped
            }
        }
    }
}
