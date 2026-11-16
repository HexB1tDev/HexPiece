package com.hexhaki.piece.server;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.npc.SeaRules;
import com.hexhaki.piece.style.FightingStyle;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: /hexpiece (operators): {@code reset} lets the player choose gender, flag and style again on the start menu;
 * {@code spawn pirate|marine [rank]} stands a fighter (0 grunt, 1 officer, 2 captain) where they are, who stays;
 * {@code patrol pirate|marine} sends a patrol nearby.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class PieceCommands {
    private PieceCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.m_82127_("hexpiece").requires(source -> source.m_6761_(2))
            .then(Commands.m_82127_("reset").executes(PieceCommands::reset))
            .then(Commands.m_82127_("spawn")
                .then(Commands.m_82127_("pirate").executes(context -> spawn(context, Faction.PIRATE, 0))
                    .then(Commands.m_82129_("rank", IntegerArgumentType.integer(0, 2))
                        .executes(context -> spawn(context, Faction.PIRATE, IntegerArgumentType.getInteger(context, "rank")))))
                .then(Commands.m_82127_("marine").executes(context -> spawn(context, Faction.MARINE, 0))
                    .then(Commands.m_82129_("rank", IntegerArgumentType.integer(0, 2))
                        .executes(context -> spawn(context, Faction.MARINE, IntegerArgumentType.getInteger(context, "rank"))))))
            .then(Commands.m_82127_("patrol")
                .then(Commands.m_82127_("pirate").executes(context -> patrol(context, Faction.PIRATE)))
                .then(Commands.m_82127_("marine").executes(context -> patrol(context, Faction.MARINE)))));
    }

    static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().m_81375_();
        ProfileServer.reset(player);
        player.m_5661_(Component.m_237113_("Choose again: the start menu is open."), false);
        return 1;
    }

    static int spawn(CommandContext<CommandSourceStack> context, Faction side, int rank) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().m_81375_();
        if (!(player.m_9236_() instanceof ServerLevel level)) {
            return 0;
        }
        FightingStyle[] styles = SeaRules.styles(side);
        int look = SeaRules.look(rank, level.m_213780_().m_188503_(8));
        double ahead = 3.0;
        double yaw = Math.toRadians(player.m_146908_());
        boolean made = SeaSpawner.place(level, side, player.m_20185_() - Math.sin(yaw) * ahead, player.m_20189_() + Math.cos(yaw) * ahead, look,
            styles[level.m_213780_().m_188503_(styles.length)], true) != null;
        player.m_5661_(Component.m_237113_(made ? SeaRules.title(side, rank) + " stands before you." : "No room to stand one there."), false);
        return made ? 1 : 0;
    }

    static int patrol(CommandContext<CommandSourceStack> context, Faction side) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().m_81375_();
        if (!(player.m_9236_() instanceof ServerLevel level)) {
            return 0;
        }
        double yaw = Math.toRadians(player.m_146908_());
        int placed = SeaSpawner.patrol(level, side, player.m_20185_() - Math.sin(yaw) * 12, player.m_20189_() + Math.cos(yaw) * 12,
            SeaRules.patrol(level.m_213780_().m_188500_(), 1.0), level.m_213780_());
        player.m_5661_(Component.m_237113_(placed + " " + (side == Faction.MARINE ? "marines" : "pirates") + " on patrol ahead."), false);
        return placed;
    }
}
