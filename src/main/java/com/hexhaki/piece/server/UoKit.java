package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 2.7.0: the Uo Uo no Mi's kit in the original's fruit system (its enum's UO, PiecePatch.uoFruit). 2.7.2: its moves (the owner's, in
 * his own words in each description): in human form X Air Pressure, C Blast Breath, V Wind Force, B Dragon Embers; Z the dragon form;
 * in dragon form M1 (held) the fire breath, X (held) the charged Blast Breath, C the Gigantic Bite. A move's "[key]" is its quick
 * slot (Binds' letters: B Z C V X are slots 4 to 8); a key shared by a human and a dragon move is the form's (server/UoMoves). The
 * quick keys answer in fruit mode only (2.7.1).
 */
public final class UoKit extends FruitKit {
    public static final FruitMove DRAGON_FORM = new FruitMove("Dragon Form", "[Z]", 0, null,
        "Become Seiryu, the azure dragon, out of your own body: it bursts up out of you, arcs over and coils in the sky where you stood."
            + " Fly as you fly in creative (jump to rise, sneak to sink, sprint for speed); the dragon follows every turn. Z again and it"
            + " dives back into you. The sea takes it away.");
    public static final FruitMove AIR_PRESSURE = new FruitMove("Air Pressure", "[X]", 0, "uo_air",
        "The right hand driven out three times: a blast of compressed air from each, striking and throwing whatever it meets up to 25"
            + " blocks away.");
    public static final FruitMove BLAST_BREATH = new FruitMove("Blast Breath", "[C]", 0, "uo_breath",
        "A breath drawn in and thrown out as a line of fire that races 35 blocks and bursts at the end: burning, throwing, hurting all"
            + " around it.");
    public static final FruitMove WIND_FORCE = new FruitMove("Wind Force", "[V]", 0, "uo_wind",
        "The arm swept forward hard enough to raise a green tornado that runs 25 blocks, dragging everything it catches round and up"
            + " along with it, and flinging it at the end.");
    public static final FruitMove DRAGON_EMBERS = new FruitMove("Dragon Embers", "[B]", 0, "uo_embers",
        "Fireballs thrown up out of the mouth to scatter over the sky, then rain down on everyone within 15 blocks.");
    public static final FruitMove FIRE_BREATH = new FruitMove("Dragon: Fire Breath", "[LMB] hold", 0, "uo_flame",
        "In dragon form: the massive jaw opened, a breath of fire 35 blocks long for as long as M1 is held.");
    public static final FruitMove CHARGED_BLAST = new FruitMove("Dragon: Blast Breath", "[X] hold", 0, "uo_blast",
        "In dragon form: a fireball gathering in the mouth, bigger the longer X is held, then let go as a massive blast (45 to 100"
            + " blocks).");
    public static final FruitMove GIGANTIC_BITE = new FruitMove("Dragon: Gigantic Bite", "[C]", 0, "uo_bite",
        "In dragon form: a lunge forward and the jaws snapped shut on whatever is in front of them.");
    public static final List<FruitMove> HUMAN = List.of(DRAGON_FORM, AIR_PRESSURE, BLAST_BREATH, WIND_FORCE, DRAGON_EMBERS);
    public static final List<FruitMove> DRAGON = List.of(DRAGON_FORM, FIRE_BREATH, CHARGED_BLAST, GIGANTIC_BITE);
    public static final List<FruitMove> MOVES = List.of(DRAGON_FORM, AIR_PRESSURE, BLAST_BREATH, WIND_FORCE, DRAGON_EMBERS, FIRE_BREATH,
        CHARGED_BLAST, GIGANTIC_BITE);

    @Override
    public Fruit fruit() {
        return Fruit.UO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind} 0 is a key going down, 1 coming up; {@code ability} the quick slot it is bound to (the move's own by default). */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == DRAGON_FORM.slot()) {
            UoMoves.forget(player);
            DragonServer.toggle(player);
        } else if (kind == 0) {
            UoMoves.press(player, ability);
        } else if (kind == 1) {
            UoMoves.release(player, ability);
        }
    }

    /** The fruit eaten (or its kit given back on joining): its dragon's colour chosen if it has none yet (DragonServer.colour). */
    @Override
    public void granted(ServerPlayer player) {
        DragonServer.colour(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        UoMoves.forget(player);
        DragonServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        UoMoves.forget(player);
        DragonServer.restore(player);
    }
}
