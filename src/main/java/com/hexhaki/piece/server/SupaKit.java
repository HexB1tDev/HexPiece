package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 3.3.5 (the owner: "Supa Supa No Mi (UnCommon) ... Blade Transformation ... Slashing Immunity ... Ranged & Spinning Cuts ... High-Speed
 * Mobility ... Spar Claw ... Spider ... Atomic Spar ... Spiral Hollow ... Atomic Spurt ... Sparkling Daisy & Spar Break ... (haki
 * blackens the blades as in makes them purpleish like the katanas)"; the keys asked and accepted as proposed, docs/handoff 8zw): Daz
 * Bones' Supa Supa no Mi, a Paramecia. The attack button is Spar Claw (no punch); the moves are server/SupaMoves; the blades are the
 * owner's katana steel (supa/SupaBlades, client/supa/SupaClient).
 */
public final class SupaKit extends FruitKit {
    public static final FruitMove CLAW = new FruitMove("Spar Claw", "[LMB]", 0, null,
        "Your fingers turn to blades: every swing of the attack button rakes whoever is in front of you with four claws of steel.");
    public static final FruitMove SLASH = new FruitMove("Flying Slash", "[RMB]", 0, "supa_slash",
        "A blade grows from your forearm and you throw a crescent of cutting force off it; hold to charge a bigger, longer one. It cuts"
            + " through everyone in its path.");
    public static final FruitMove ATOMIC = new FruitMove("Atomic Spar", "[Z]", 0, "supa_atomic",
        "Both arms turn to blades and cross: an X of cutting force flies out and cuts through everyone in its way.");
    public static final FruitMove SPIRAL = new FruitMove("Spiral Hollow", "[X]", 0, "supa_spiral",
        "Blades spin round both forearms like drills and you charge forward, drilling through everything in a line.");
    public static final FruitMove SPURT = new FruitMove("Atomic Spurt", "[C]", 0, "supa_spurt",
        "Blades grow from your feet and you skate at great speed for a few seconds, cutting whoever you pass.");
    public static final FruitMove SPIDER = new FruitMove("Spider", "[V]", 0, "supa_spider",
        "Your whole body turns to steel for 8 seconds: blows glance off it (unless coated in Haki), you are not knocked back, bare fists"
            + " that strike you are cut, and you move a little slower.");
    public static final FruitMove DAISY = new FruitMove("Sparkling Daisy", "[B]", 0, "supa_daisy",
        "Both arm blades out, you lunge straight through the one you look at, cutting them as you pass.");
    public static final FruitMove BREAK = new FruitMove("Spar Break", "[N]", 0, "supa_break",
        "Blades burst out all round your body and you spin, cutting everyone around you, ending in a burst that throws them away.");
    public static final FruitMove IMMUNE = new FruitMove("Slashing Immunity", "passive", 0, null,
        "Your body is as hard as steel: swords, axes and other blades cannot cut you unless they are coated in Armament Haki.");
    public static final List<FruitMove> MOVES = List.of(CLAW, SLASH, ATOMIC, SPIRAL, SPURT, SPIDER, DAISY, BREAK, IMMUNE);

    @Override
    public Fruit fruit() {
        return Fruit.SUPA;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> SupaMoves.press(player, ability);
            case 2 -> SupaMoves.claw(player);
            case 3 -> SupaMoves.use(player);
            case 4 -> SupaMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        SupaMoves.tick(player);
    }

    /**
     * Slashing Immunity (a blade without Haki glances off) and Spider (every blow without Haki glances off, no knockback, a bare fist
     * cut): the original asks this before a blow lands while the fruit is out (FruitServer.attacked); true stops it.
     */
    @Override
    public boolean blocks(ServerPlayer player, DamageSource source) {
        return SupaMoves.blocks(player, source);
    }

    @Override
    public void removed(ServerPlayer player) {
        SupaMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        SupaMoves.forget(player);
    }

    @Override
    public void reset() {
        SupaMoves.clear();
    }
}
