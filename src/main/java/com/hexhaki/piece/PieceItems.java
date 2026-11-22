package com.hexhaki.piece;

import com.hexhaki.piece.armor.PieceArmor;
import com.hexhaki.piece.armor.PieceArmorItem;
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
 * "HexPiece Katanas", the grades from the lowest to the Supreme. 2.3.1: the owner's One Piece armor ({@code hexhaki:<key>}) and its own
 * tab, "One Piece Armor" (the owner: "create a custom tab in creative as well").
 */
public final class PieceItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexPiece.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_, HexPiece.MODID);
    public static final Map<Katana, RegistryObject<Item>> KATANAS = new EnumMap<>(Katana.class);
    public static final Map<PieceArmor, RegistryObject<Item>> ARMOR = new EnumMap<>(PieceArmor.class);

    static {
        for (Katana katana : Katana.values()) {
            KATANAS.put(katana, ITEMS.register("katana_" + katana.key(), () -> new KatanaItem(katana)));
        }
        for (PieceArmor piece : PieceArmor.values()) {
            ARMOR.put(piece, ITEMS.register(piece.key(), () -> new PieceArmorItem(piece)));
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

    /** 2.3.1: the owner's armor, the Marine outfit head to feet first. */
    public static final RegistryObject<CreativeModeTab> ARMOR_TAB = TABS.register("armor", () -> CreativeModeTab.builder()
        .m_257941_(Component.m_237113_("One Piece Armor"))
        .m_257737_(() -> stack(PieceArmor.STRAW_HAT))
        .m_257501_((parameters, output) -> {
            for (PieceArmor piece : PieceArmor.values()) {
                output.m_246342_(stack(piece));
            }
        })
        .m_257652_());

    /**
     * 3.2.0 (the owner: "we're going to need a tab in Creative for only the fruits"): every Devil Fruit and nothing else, by rank
     * (FruitRanks: the Legendary ones, then the Mythical, the Nika fruit last), each named in its rank's colour.
     */
    public static final RegistryObject<CreativeModeTab> FRUIT_TAB = TABS.register("fruits", () -> CreativeModeTab.builder()
        .m_257941_(Component.m_237113_("Devil Fruits"))
        .m_257737_(() -> new ItemStack(com.hexhaki.fruit.Fruits.ITEM.get(com.hexhaki.fruit.Fruit.GURA).get(), 1))
        .m_257501_((parameters, output) -> {
            for (FruitRanks.Rank rank : FruitRanks.Rank.values()) {
                for (com.hexhaki.fruit.Fruit fruit : com.hexhaki.fruit.Fruit.values()) {
                    RegistryObject<Item> item = com.hexhaki.fruit.Fruits.ITEM.get(fruit);
                    if (item != null && FruitRanks.of(fruit) == rank) {
                        output.m_246342_(new ItemStack(item.get(), 1));
                    }
                }
            }
            output.m_246342_(new ItemStack(com.hexhaki.registry.ModItems.NIKA_FRUIT.get(), 1));
        })
        .m_257652_());

    private PieceItems() {
    }

    /** One armor piece, as a stack. */
    public static ItemStack stack(PieceArmor piece) {
        return new ItemStack(ARMOR.get(piece).get(), 1);
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
