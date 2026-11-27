package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.gun.GunAnims;
import com.hexhaki.piece.gun.GunRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.8: the gun's sight. The game draws its crosshair only in first person, and a gun in the hand puts the view in third person over
 * the shoulder (client/sword/SwordCamera), so with a gun the screen's middle gets a sight of its own: four short ticks round a dot, the
 * ball going exactly there (client/gun/GunInput aims along the camera's centre line). Empty (fired, reloading) the sight dims and a bar
 * under it fills as the ball goes home.
 */
public final class GunSight {
    private GunSight() {
    }

    static void draw(GuiGraphics graphics, int width, int height, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON || GunClient.gun(player.m_19879_()) == null) {
            return;
        }
        int anim = GunClient.anim(player.m_19879_());
        float ticks = GunClient.ticks(player, partial);
        boolean empty = anim == GunAnims.RELOAD || GunAnims.firing(anim) && ticks >= GunRules.AIM;
        int cx = width / 2;
        int cy = height / 2;
        int ink = empty ? 0x70F4E8D0 : 0xF0F4E8D0;
        int edge = empty ? 0x50101014 : 0xC0101014;
        int gap = GunAnims.firing(anim) && ticks >= GunRules.AIM && ticks < GunRules.AIM + 3 ? 4 : 2;
        // the outline, then the ticks and the dot
        tick(graphics, cx, cy, gap, 4, edge, 1);
        tick(graphics, cx, cy, gap, 4, ink, 0);
        graphics.m_280509_(cx - 1, cy - 1, cx + 1, cy + 1, ink);
        if (anim == GunAnims.RELOAD) {
            float fill = Math.max(0.0F, Math.min(1.0F, ticks / GunRules.RELOAD));
            graphics.m_280509_(cx - 9, cy + 9, cx + 9, cy + 11, 0x90101014);
            graphics.m_280509_(cx - 9, cy + 9, cx - 9 + Math.round(18 * fill), cy + 11, 0xE0E0B860);
        }
    }

    /** The four ticks {@code gap} px out from the middle, {@code len} long, grown by {@code grow} px each way (the outline). */
    static void tick(GuiGraphics graphics, int cx, int cy, int gap, int len, int colour, int grow) {
        graphics.m_280509_(cx - gap - len - grow, cy - grow, cx - gap + grow, cy + 1 + grow, colour);
        graphics.m_280509_(cx + gap + 1 - grow, cy - grow, cx + gap + 1 + len + grow, cy + 1 + grow, colour);
        graphics.m_280509_(cx - grow, cy - gap - len - grow, cx + 1 + grow, cy - gap + grow, colour);
        graphics.m_280509_(cx - grow, cy + gap + 1 - grow, cx + 1 + grow, cy + gap + 1 + len + grow, colour);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        public static void registerGunSight(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_gun_sight", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height, partialTick));
        }
    }
}
