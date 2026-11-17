package com.hexhaki.piece.sword;

import java.util.ArrayList;
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
 * 2.1.4: one of the owner's 25 katanas as an item (the owner: "give the sword beautiful description, tiers, and rarities, the names are
 * in the image"): its name, its grade's rarity colour, a tooltip with the grade, the hamon and the blade's own story. One to a slot.
 * <p>
 * Deliberately a plain {@link Item}, not a vanilla sword: the original's Haki Blade Cut fires for any vanilla sword swung with Armament
 * on, and a katana does nothing on its own (the owner: "The sword is never usable, unsheatheable or of any use until combat mode is
 * enabled, ONLY players with the swordsman combat can unsheath it"). A Swordsman in combat mode fights with it ({@code SwordServer});
 * anyone else only carries it at the hip. It is drawn by its own renderer everywhere (client/sword/KatanaItemRenderer): the whole mesh
 * on the ground and in item frames, the inventory icon in the inventory, and nothing in the hand (the player's own sword layer draws it,
 * sheathed at the hip or drawn in the hand, so it is never seen twice).
 */
public final class KatanaItem extends Item {
    /** The words a line of the tooltip's description holds. */
    static final int WRAP = 38;
    private final Katana katana;

    public KatanaItem(Katana katana) {
        super(new Item.Properties().m_41487_(1).m_41497_(rarity(katana.grade())));
        this.katana = katana;
    }

    public Katana katana() {
        return this.katana;
    }

    /** The katana a stack holds, or null if it is not one. */
    public static Katana of(ItemStack stack) {
        return stack != null && !stack.m_41619_() && stack.m_41720_() instanceof KatanaItem item ? item.katana : null;
    }

    static Rarity rarity(Grade grade) {
        return switch (grade.rarity()) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        Grade grade = this.katana.grade();
        lines.add(Component.m_237113_(grade.title() + "  ·  " + grade.english()).m_130938_(style -> style.m_178520_(grade.colour())));
        lines.add(this.katana.legend() ? Component.m_237113_(this.katana.kindLine()).m_130938_(style -> style.m_178520_(0xE0B860))
            : Component.m_237113_(this.katana.kindLine()).m_130940_(ChatFormatting.GRAY));
        for (String line : wrap(this.katana.description(), WRAP)) {
            lines.add(Component.m_237113_(line).m_130944_(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        lines.add(Component.m_237113_(String.format(java.util.Locale.ROOT, "Edge x%.2f", this.katana.edge())
            + (this.katana.reach() > 0.5 ? "  ·  long reach" : "")).m_130940_(ChatFormatting.GRAY));
        if (this.katana.legend()) {
            lines.add(Component.m_237113_(this.katana.carry() == Katana.Carry.BACK ? "Worn on the back; drawn whenever you fight"
                : "Drawn whenever you fight").m_130938_(style -> style.m_178520_(0xE0B860)));
        }
        lines.add(Component.m_237113_("Swordsman: drawn in combat mode (R)").m_130938_(style -> style.m_178520_(0x8FB3D9)));
    }

    /** {@code text} broken into lines of at most {@code width} characters, at spaces. */
    public static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /** Its renderer, on a client only (Forge calls this from the constructor there). */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.hexhaki.piece.client.sword.KatanaItemRenderer.register(consumer));
    }
}
