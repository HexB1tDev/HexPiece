package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

/**
 * 2.7.8 (the owner: "better the Lightfruit in every way, make it cooler, more dramatic, better animations, fix broken areas of it (its
 * terrible in every single way). Do a deep research about Kizaru One piece, copy his moves and how he uses them exactly."): the Pika
 * Pika no Mi rebuilt after Borsalino, in place of the original's LightKit (HexPiece registers it over it). His moves as he uses them:
 * kicks at the speed of light, lasers from a fingertip, Yata no Kagami (light formed between the hands, and he goes along it),
 * Yasakani no Magatama (up in the air, arms crossed, a torrent of light from both hands), Ama no Murakumo (a sword of solid light),
 * "have you ever been kicked at the speed of light?", and Egghead's clones of light. Keys as the owner set them for every fruit (Z X
 * C V B, the attack and use buttons). The Logia body (blows pass through as light unless coated in Armament) is the original's
 * FruitServer's; this kit adds the flash and the moves (server/PikaMoves); 2.9.9: no flight (only Z, as light).
 */
public final class PikaKit extends FruitKit {
    public static final FruitMove LASER = new FruitMove("Laser", "[RMB]", 0, "pika_laser",
        "As at Sabaody and Marineford: a finger pointed and a laser out of its tip that blows up where it strikes. Hold to gather the"
            + " light at the fingertip and let go a beam that burns through everything in its way.");
    public static final FruitMove YATA = new FruitMove("Yata no Kagami", "[Z] hold", 0, "pika_yata",
        "As at Egghead: you become light itself and go as a beam where you look, reflected off everything it meets, every block at any"
            + " angle and the sea (thrown at the water, it comes back up off it), striking whoever it passes through. Let go and you are a"
            + " body again where the light is, still going with some of its speed. Three uses that come back on their own.");
    public static final FruitMove YASAKANI = new FruitMove("Yasakani no Magatama", "[X] hold", 0, "pika_yasakani",
        "As at Egghead: up into the air over them, both arms thrust out, and a torrent of slugs of light out of both palms that burst"
            + " wherever they land, over the whole ground where you look for as long as you hold it; fly about over them while it pours. Sneak"
            + " to bring it all down on one.");
    public static final FruitMove MURAKUMO = new FruitMove("Ama no Murakumo", "[C]", 0, "pika_lunge",
        "As at Egghead: a sword of solid light drawn out of the joined hands, and you fly at them with it thrust out ahead, through the one"
            + " you look at and on past them, cutting everyone the blade passes. Where you arrive, one wide cut and a wave of light, and the"
            + " sword is gone.");
    public static final FruitMove LIGHT_KICK = new FruitMove("Speed of Light Kick", "[V] hold", 0, "pika_light_kick",
        "\"Have you ever been kicked at the speed of light?\" The light gathered in the leg, let go: gone as light and out of it behind"
            + " the one you look at, over their shoulder, you spin round and kick them in the side of the head, across the field. Held"
            + " longer, it lands harder. With no one there, the kick fires a beam out of the leg.");
    public static final FruitMove CLONES = new FruitMove("Yasakani no Magatama: Clones", "[B]", 0, "pika_clones",
        "As at Egghead: the light thrown out of the body and every bead of it a Borsalino of light, each flying at someone near with a"
            + " sword of light, cutting, and bursting.");
    public static final FruitMove BODY = new FruitMove("Body of Light", "passive", 0, null,
        "A Logia: blows and shots pass through you as light unless they come coated in Armament Haki. You never take a fall. You do not"
            + " fly: the only way through the air is as light (Z). The sea takes it all away.");
    public static final List<FruitMove> MOVES = List.of(LASER, YATA, YASAKANI, MURAKUMO, LIGHT_KICK, CLONES, BODY);

    @Override
    public Fruit fruit() {
        return Fruit.PIKA;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button (again every 5 ticks while held), 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> PikaMoves.press(player, ability, flags);
            case 1 -> PikaMoves.release(player, ability);
            case 2 -> PikaMoves.attack(player, flags);
            case 3 -> PikaMoves.use(player, flags);
            case 4 -> PikaMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        PikaMoves.tick(player);
    }

    /**
     * The flight the original gave the Light fruit (double-tap jump). 2.9.9 (the owner: "why can light fly? The only flight should be
     * through Z."): never given any more; called with false to take away what an older version gave.
     */
    static void flight(ServerPlayer player, boolean on) {
        if (player.m_7500_() || player.m_5833_()) {
            return;
        }
        var abilities = player.m_150110_();
        if (abilities.f_35936_ == on) {
            return;
        }
        abilities.f_35936_ = on;
        if (!on) {
            abilities.f_35935_ = false;
        }
        abilities.m_35943_(on ? 0.075F : 0.05F);
        player.m_6885_();
    }

    @Override
    public void granted(ServerPlayer player) {
        flight(player, false);
        CompoundTag data = FruitData.get(player);
        if (!data.m_128441_(PikaMoves.YATA_KEY)) {
            data.m_128405_(PikaMoves.YATA_KEY, PikaMoves.YATA_MAX);
        }
    }

    @Override
    public void removed(ServerPlayer player) {
        PikaMoves.forget(player);
        flight(player, false);
    }

    @Override
    public void cancel(ServerPlayer player) {
        PikaMoves.forget(player);
    }

    /** What the client needs to pose and draw the player: the sword's end, Yata's uses left. */
    @Override
    public void view(ServerPlayer player, CompoundTag tag, boolean self) {
        PikaMoves.view(player, tag);
    }

    /** A blow passed through the body of light: a flash where it went through. */
    @Override
    public void dodged(ServerPlayer player, DamageSource source) {
        PikaMoves.dodged(player, source);
    }

    /** Light takes no fall and no wall. */
    @Override
    public boolean immune(ServerPlayer player, DamageSource source) {
        return source.m_276093_(DamageTypes.f_268671_) || source.m_276093_(DamageTypes.f_268576_);
    }

    @Override
    public void reset() {
        PikaMoves.clear();
    }
}
