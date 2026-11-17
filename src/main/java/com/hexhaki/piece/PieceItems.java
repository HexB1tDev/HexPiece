package com.hexhaki.piece;

import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.KatanaItem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 2.1.4: HexPiece's items: the owner's 25 katanas ({@code hexhaki:katana_<key>}, in the poster's order) and their own creative tab,
 * "HexPiece Katanas", the grades from the lowest to the Supreme.
 */
public final class PieceItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexPiece.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_, HexPiece.MODID);
    public static final Map<Katana, RegistryObject<Item>> KATANAS = new EnumMap<>(Katana.class);

    static {
        for (Katana katana : Katana.values()) {
            KATANAS.put(katana, ITEMS.register("katana_" + katana.key(), () -> new KatanaItem(katana)));
        }
    }

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("katanas", () -> CreativeModeTab.builder()
        .m_257941_(Component.m_237113_("HexPiece Katanas"))
        .m_257737_(() -> stack(Katana.OBSIDIAN_ECLIPSE))
        .m_257501_((parameters, output) -> {
            for (int grade = 0; grade < 4; grade++) {
                for (Katana katana : Katana.values()) {
                    if (katana.grade().rarity() == grade && !katana.legend()) {
                        output.m_246342_(stack(katana));
                    }
                }
            }
            // 2.2.7: the legendary blades last, Yoru at the very end
            output.m_246342_(stack(Katana.GRYPHON));
            output.m_246342_(stack(Katana.YORU));
        })
        .m_257652_());

    private PieceItems() {
    }

    /** One katana, as a stack. */
    public static ItemStack stack(Katana katana) {
        return new ItemStack(KATANAS.get(katana).get(), 1);
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
