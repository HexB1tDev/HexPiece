package com.hexhaki.piece.client.fx;

import com.hexhaki.fx.client.ImpactClient;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.5.5: the "Fishman Karate white screen" (the owner: "as soon as I select it, the entire screen goes full white", "the screen is FULLY
 * white permanently, no matter what I do", with screenshots and the log): it was never the style. The original's hit effects
 * ({@code fx/client/ImpactClient}: the white flash a heavy blow sends, its combo counter and title, the camera's shake and kick) keep
 * their start and end as the world's game time and are never cleared, not even when leaving a world. A blow landed in one world (the
 * owner's: marines fought, a game time of hundreds of thousands of ticks) left a flash whose end lay far ahead of a newer world's clock,
 * which starts near 0: from the moment that world was joined the flash was "still running", at full strength (its progress below 0
 * reads as its first frame), drawn over the whole HUD after everything else ({@code RenderGuiEvent.Post}) every frame for as long as
 * the old world had run. The start menu (a new world's first screen) covered it; Set Sail closed the menu and the view went white,
 * whichever style was picked. Only what the game draws a hair in front showed through: the shadowed text of the abilities panel's
 * title and the chat box.
 * <p>
 * Now: whenever the client's world changes, or its clock goes back, the original's impact state is cleared ({@link #reset}); and its
 * flash is drawn only while time runs forward from its start ({@link #flash}, PiecePatch CALL_REDIRECTS in {@code ImpactClient.hud}),
 * so a stale flash can never cover the view again.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ImpactReset {
    private static Object lastLevel;
    private static long lastTime = Long.MIN_VALUE;
    private static boolean broken;

    private ImpactReset() {
    }

    /**
     * The original's flash ({@code ImpactClient.drawFlash}, widened by the patch), drawn only for a {@code progress} from 0 (its start)
     * on: below 0 the world's clock is behind the flash's start (a flash from another world, or the clock turned back), and the stale
     * flash is cleared instead.
     */
    public static void flash(GuiGraphics graphics, int width, int height, float progress) {
        if (!(progress >= 0.0F) || progress > 4.0F) {
            reset();
            return;
        }
        ImpactClient.drawFlash(graphics, width, height, progress);
    }

    @SubscribeEvent
    public static void impactWorld(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        long time = level.m_46467_();
        if (level != lastLevel || time < lastTime) {
            reset();
        }
        lastLevel = level;
        lastTime = time;
    }

    @SubscribeEvent
    public static void impactLeft(ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
        lastLevel = null;
        lastTime = Long.MIN_VALUE;
    }

    /**
     * The original's impact state back to exactly what it starts with (its static initializer; build.py check_impact_fields proves
     * every value against it): no flash, combo, title, shake, kick, FOV punch or shudder. 2.5.7: the title and the combo's move are
     * the empty text, never null (2.5.6's null crashed the game on joining any world: the original's drawTitle asks
     * {@code title.isEmpty()} every frame), and the combo's and title's times the earliest there is, as the original sets them.
     */
    static void reset() {
        if (broken) {
            return;
        }
        try {
            set("flash", 0);
            set("flashStart", 0L);
            set("flashUntil", 0L);
            set("comboCount", 0);
            set("comboMove", "");
            set("comboAt", Long.MIN_VALUE);
            set("title", "");
            set("titleAt", Long.MIN_VALUE);
            set("shakeUntil", 0L);
            set("shake", 0.0F);
            set("kick", 0.0F);
            set("kickBefore", 0.0F);
            set("fovPunch", 0.0F);
            set("fovBefore", 0.0F);
            Field shudder = ImpactClient.class.getDeclaredField("SHUDDER");
            shudder.setAccessible(true);
            if (shudder.get(null) instanceof Map<?, ?> map) {
                map.clear();
            }
        } catch (ReflectiveOperationException | RuntimeException failure) {
            broken = true; // the flash guard still holds
        }
    }

    static void set(String name, Object value) throws ReflectiveOperationException {
        if (value == null) {
            throw new IllegalArgumentException(name); // the original reads its fields without a null check
        }
        Field field = ImpactClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }
}
