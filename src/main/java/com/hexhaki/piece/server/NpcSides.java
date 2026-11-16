package com.hexhaki.piece.server;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.npc.SeaNpc;
import net.minecraft.world.entity.LivingEntity;

/** 2.1.1: which side an NPC sails for, for renown: its rank (0 grunt, 1 officer, 2 captain) if it is of {@code side}'s enemy, else -1. */
public final class NpcSides {
    private NpcSides() {
    }

    public static int rankIfEnemy(LivingEntity entity, Faction side) {
        if (entity instanceof SeaNpc npc && npc.faction() == side.enemy()) {
            return npc.rank();
        }
        return -1;
    }
}
