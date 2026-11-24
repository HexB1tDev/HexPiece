package com.hexhaki.piece.client;

import com.hexhaki.fruit.client.FruitClient;

/**
 * 2.1.3: where the original's client code asks HexPiece (calls redirected by {@code patch/PiecePatch.java}'s CALL_REDIRECTS).
 */
public final class PieceHooks {
    private PieceHooks() {
    }

    /**
     * A kit fruit's M1 and right click ({@code FruitKeys}): the kit is active and the player is not in combat mode. R switches between
     * the two (the owner: "tapping R switches from Combat style and fruit (if any is eaten)"); the kit's bound abilities keep their keys.
     */
    /**
     * 2.8.8 (the owner: "Light animations arent playing"): the rebuilt Light fruit's clips and the Uo Uo no Mi's play on the fruit's own
     * layer (the original's FruitAnimations), above its stance and standing it down while they play, as the original's "light_" did
     * (PiecePatch.fruitClips).
     */
    public static boolean fruitClip(String name) {
        return name != null && (name.startsWith("pika_") || name.startsWith("uo_") || name.startsWith("gura_") || name.startsWith("magu_") || name.startsWith("hie_") || name.startsWith("ito_") && !name.startsWith("ito_sky_")
            || name.startsWith("ito_") // 3.4.2: the String fruit's own clips
            || name.startsWith("mochi_")); // 3.5.5: the Mochi fruit's
    }

    /**
     * 2.9.0: a player ({@code id}) was told to play the clip {@code name} (HakiAnimations.playRemote, before anything else): the Light
     * fruit's first person and fingertip follow it (client/FirstPersonLight).
     */
    public static void played(int id, String name) {
        try {
            FirstPersonLight.played(id, name);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a clip could not be followed", failure);
        }
    }

    public static boolean fruitKeysActive() {
        return FruitClient.active() && !CombatMode.on();
    }

    /**
     * 2.2.4 (the owner: "The keys should be usable just like that in any form"): the original's "powers off" switch (X without the Gomu
     * fruit, {@code BindsClient.off}) also silenced Armament, Observation, Ryou, the Haki leap and every Haki quick slot. Haki's keys
     * ({@code HakiKeys$ForgeBus.tick}, {@code movementInput}) no longer ask it: it turns the fruit off ({@code FruitClient.active} still
     * reads it), never Haki. Only an open screen pauses them, as before.
     */
    public static boolean hakiKeysOff() {
        // 3.5.9 (the owner: "HOLDING R will now fully disable all powers and abilities (but fruit passives)"): the powers-off bit is
        // the original's again (server/Powers), and it now holds Haki back too
        return com.hexhaki.binds.client.BindsClient.off();
    }

    /**
     * 2.8.2 (the owner: "give the current M1s a first person (some are missing, like 6 powers, black leg, water kungfu and iron
     * first)"): the original plays every animation in first person with both arms hidden ({@code HakiAnimations.playRemote}), so a
     * style's blows, the Light fruit's and the Uo Uo no Mi's human moves showed nothing there. Those now show the arms as they move (as
     * the Swordsman's swings do); the kicks' legs are drawn by client/FirstPersonLegs. Everything else keeps the original's choice.
     */
    /**
     * 3.3.9: the original's {@code HakiAnimations.OWN_FIRST_PERSON_ARM.contains(name)} (its stretching moves: first person is the
     * original's own stretched arm, PlayerAnimator left out of it), with Dawn Pistol's two clips added: their first person is exactly
     * the Pistol's (the owner: "The first person is fine").
     */
    public static boolean ownFirstPerson(java.util.Set<?> own, Object name) {
        return own.contains(name) || com.hexhaki.piece.server.NikaPistol.CLIP_RIGHT.equals(name)
            || com.hexhaki.piece.server.NikaPistol.CLIP_LEFT.equals(name);
    }

    public static dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration firstPersonArms(
        dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration config, String name) {
        if (name != null && (name.startsWith("style_") && !name.startsWith("style_swordsman") || name.startsWith("pika_")
            || name.startsWith("uo_") || name.startsWith("gura_") || name.startsWith("magu_") || name.startsWith("hie_")
            // 3.2.7: Dawn Whip's own clip shows the whip's very arms in first person, as gear5_whip does (the owner: first person is fine)
            || name.equals("gear5_dawn_whip")
            // 3.4.4: Goof Ball's spin-up: the arms windmilling in first person before the ball (then client/gear/NikaWhipClient's fists)
            || name.equals("gear5_goof_spin"))) {
            return config.setShowRightArm(true).setShowLeftArm(true);
        }
        return config;
    }
}
