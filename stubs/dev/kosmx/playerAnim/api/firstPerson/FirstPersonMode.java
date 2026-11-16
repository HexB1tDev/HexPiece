// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package dev.kosmx.playerAnim.api.firstPerson;

public enum FirstPersonMode {
    NONE, VANILLA, THIRD_PERSON_MODEL, DISABLED;

    /** 2.1.4: is this render pass playerAnimator's first-person pass? */
    public static boolean isFirstPersonPass() { throw new UnsupportedOperationException(); }
}
