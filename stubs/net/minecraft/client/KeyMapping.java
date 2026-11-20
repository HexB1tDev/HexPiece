// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client;

public class KeyMapping {
    public KeyMapping(String name, com.mojang.blaze3d.platform.InputConstants.Type type, int key, String category) { throw new UnsupportedOperationException(); }
    /** consumeClick */ public boolean m_90859_() { throw new UnsupportedOperationException(); }
    /** isDown */ public boolean m_90857_() { throw new UnsupportedOperationException(); }
    /** saveString (yarn getBoundKeyTranslationKey): "key.keyboard.r", "key.mouse.left" */ public String m_90865_() { throw new UnsupportedOperationException(); }
    /** Forge: the modifier the player bound with the key */ public net.minecraftforge.client.settings.KeyModifier getKeyModifier() { throw new UnsupportedOperationException(); }
    /** setKey (2.7.1: client/KeyLayout) */ public void m_90848_(com.mojang.blaze3d.platform.InputConstants.Key key) { throw new UnsupportedOperationException(); }
    /** resetMapping: the key map rebuilt from every mapping's key (2.7.1) */ public static void m_90854_() { throw new UnsupportedOperationException(); }
    /** matches(keyCode, scanCode) */ public boolean m_90832_(int keyCode, int scanCode) { throw new UnsupportedOperationException(); }
}
