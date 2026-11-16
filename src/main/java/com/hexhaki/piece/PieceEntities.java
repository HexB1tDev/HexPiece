package com.hexhaki.piece;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.npc.SeaFighter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 2.1.1: HexPiece's entities: the pirate and the marine (one class, its side by its type), a player's size, monsters (so a patrol goes
 * when no one is near). They come in patrols ({@code SeaSpawner}) and by command, never from the world's own spawning.
 */
public final class PieceEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HexPiece.MODID);
    public static final RegistryObject<EntityType<SeaFighter>> PIRATE = ENTITIES.register("pirate", () -> EntityType.Builder.<SeaFighter>m_20704_(
        SeaFighter::new, MobCategory.MONSTER).m_20699_(0.6F, 1.8F).m_20702_(10).m_20717_(2).m_20712_(HexHaki.id("pirate").toString()));
    public static final RegistryObject<EntityType<SeaFighter>> MARINE = ENTITIES.register("marine", () -> EntityType.Builder.<SeaFighter>m_20704_(
        SeaFighter::new, MobCategory.MONSTER).m_20699_(0.6F, 1.8F).m_20702_(10).m_20717_(2).m_20712_(HexHaki.id("marine").toString()));

    private PieceEntities() {
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Attributes {
        private Attributes() {
        }

        @SubscribeEvent
        public static void attributes(EntityAttributeCreationEvent event) {
            event.put(PIRATE.get(), SeaFighter.attributes().m_22265_());
            event.put(MARINE.get(), SeaFighter.attributes().m_22265_());
        }
    }
}
