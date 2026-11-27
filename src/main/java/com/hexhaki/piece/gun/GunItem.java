package com.hexhaki.piece.gun;

import com.hexhaki.piece.sword.KatanaItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

/**
 * 3.8.8: one of the owner's guns as an item ({@code hexhaki:gun_<key>}): anyone may fire it (it is no fighting style's), held in the
 * right hand. Its name, its category leading its tooltip as every weapon's (the owner's standing rule), what it does. Deliberately a
 * plain {@link Item}: M1 with it in the main hand is the gun's ({@code client/gun/GunInput}, {@code server/GunServer}), never a punch.
 * It is drawn by its own renderer everywhere ({@code client/gun/GunItemRenderer}): the whole mesh on the ground and in item frames, the
 * icon in the inventory, and nothing in a hand (the player's gun layer draws it there, as the animations move it).
 */
public final class GunItem extends Item {
    private final Gun gun;

    public GunItem(Gun gun) {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE));
        this.gun = gun;
    }

    public Gun gun() {
        return this.gun;
    }

    /** The gun a stack holds, or null if it is not one. */
    public static Gun of(ItemStack stack) {
        return stack != null && !stack.m_41619_() && stack.m_41720_() instanceof GunItem item ? item.gun : null;
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.m_237113_(this.gun.kindLine()).m_130938_(style -> style.m_178520_(0xD9B26F)));
        for (String line : KatanaItem.wrap(this.gun.description(), 38)) {
            lines.add(Component.m_237113_(line).m_130944_(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        lines.add(Component.m_237113_(String.format(java.util.Locale.ROOT, "Damage %.1f  ·  Reload %.1f s", GunRules.DAMAGE,
            GunRules.RELOAD / 20.0F)).m_130940_(ChatFormatting.GRAY));
        lines.add(Component.m_237113_("M1: raise it and fire one ball; it reloads by itself").m_130938_(style -> style.m_178520_(0x8FB3D9)));
        lines.add(Component.m_237113_("Armament Haki: the ball flies pure black, far faster, through up to " + GunRules.PIERCE + " blocks")
            .m_130938_(style -> style.m_178520_(0xB38FD9)));
    }

    /** Its renderer, on a client only (Forge calls this from the constructor there). */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.hexhaki.piece.client.gun.GunItemRenderer.register(consumer));
    }
}
