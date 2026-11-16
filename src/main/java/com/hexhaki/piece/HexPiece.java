package com.hexhaki.piece;

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
        LOGGER.info("HexPiece expansion starting");
    }
}
