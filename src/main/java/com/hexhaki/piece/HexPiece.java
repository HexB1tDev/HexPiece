package com.hexhaki.piece;

import com.hexhaki.piece.network.PieceNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

/**
 * HexPiece's start-up, called by the patched {@code HexHaki} constructor with the mod bus once the original has registered everything
 * of its own.
 */
public final class HexPiece {
    public static final String MODID = "hexhaki";
    public static final Logger LOGGER = LogUtils.getLogger();

    private HexPiece() {
    }

    public static void init(IEventBus bus) {
        PieceNetwork.init();
        PieceEntities.register(bus);
        PieceItems.register(bus);
        PieceSounds.register(bus);
        PieceConfig.register();
        PieceEffects.register(bus);
        // 2.7.0: the Uo Uo no Mi (the original's fruit enum's UO, PiecePatch.uoFruit) and its kit, the dragon form
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.UO, com.hexhaki.piece.server.UoKit::new);
        // 2.7.8: the Pika Pika no Mi rebuilt after Kizaru, in place of the original's LightKit (registered just before this)
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.PIKA, com.hexhaki.piece.server.PikaKit::new);
        // 3.1.1: the Gura Gura no Mi after Whitebeard and Blackbeard (the original gives it no moves at all: Fruits' empty kit)
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.GURA, com.hexhaki.piece.server.GuraKit::new);
        // 3.1.3: the Magu Magu no Mi after Akainu (the original gives it no moves either)
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.MAGU, com.hexhaki.piece.server.MaguKit::new);
        // 3.1.9: the Hie Hie no Mi after Aokiji (a fruit the original never had: PiecePatch.uoFruit adds it to the enum)
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.HIE, com.hexhaki.piece.server.HieKit::new);
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.NORO, com.hexhaki.piece.server.NoroKit::new); // 3.3.4
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.SUPA, com.hexhaki.piece.server.SupaKit::new); // 3.3.5
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.HORO, com.hexhaki.piece.server.HoroKit::new); // 3.3.6
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.ITO, com.hexhaki.piece.server.ItoKit::new); // 3.3.7
        com.hexhaki.fruit.Fruits.register(com.hexhaki.fruit.Fruit.MOCHI, com.hexhaki.piece.server.MochiKit::new); // 3.5.5
        LOGGER.info("HexPiece expansion started: profiles, the start menu, fighting styles, the katanas, pirates and marines, the Uo Uo no Mi");
    }
}
