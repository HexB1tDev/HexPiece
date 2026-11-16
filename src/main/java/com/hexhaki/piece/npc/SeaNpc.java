package com.hexhaki.piece.npc;

import com.hexhaki.piece.Faction;

/** 2.1.1: an NPC of the sea's two sides: its side and rank (0 grunt, 1 officer, 2 captain). */
public interface SeaNpc {
    Faction faction();

    int rank();
}
