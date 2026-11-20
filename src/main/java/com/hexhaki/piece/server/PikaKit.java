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
 * FruitServer's; this kit adds the flash, the light flight and the moves (server/PikaMoves).
 */
public final class PikaKit extends FruitKit {
    public static final FruitMove KICKS = new FruitMove("Light-Speed Kicks", "[LMB]", 0, null,
        "Kicks at the speed of light, five that run into each other: a front kick, a roundhouse, a spinning back kick, a rising kick that"
            + " throws them up, and a barrage too fast to see. Sprint into it to arrive as light and kick on arrival; in the air, look down"
            + " and fall on them as light.");
    public static final FruitMove LASER = new FruitMove("Laser", "[RMB]", 0, "pika_laser",
        "As at Sabaody and Marineford: a finger pointed and a laser out of its tip that blows up where it strikes. Hold to gather the"
            + " light at the fingertip and let go a beam that burns through everything in its way.");
    public static final FruitMove YATA = new FruitMove("Yata no Kagami", "[Z]", 0, "pika_yata",
        "Light formed between the hands and reflected: you go along it as light and are where you looked, at once (beside whoever you"
            + " looked at). Three uses that come back on their own. Sneak to stream as light where you look for as long as you hold it.");
    public static final FruitMove YASAKANI = new FruitMove("Yasakani no Magatama", "[X] hold", 0, "pika_yasakani",
        "Up into the air, arms crossed, and both hands loose a torrent of light bullets that blow up whatever they touch, over the whole"
            + " ground where you look for as long as you hold it. Sneak to bring it all down on one.");
    public static final FruitMove MURAKUMO = new FruitMove("Ama no Murakumo", "[C]", 0, "pika_murakumo",
        "The hands brought together and drawn apart: a long sword of solid light. While it lasts the attack button cuts (five, the last"
            + " a wave of light) and the use button guards. C again puts it away.");
    public static final FruitMove LIGHT_KICK = new FruitMove("Speed of Light Kick", "[V] hold", 0, "pika_light_kick",
        "\"Have you ever been kicked at the speed of light?\" The light gathered in the leg, let go, and you are on the one you look at,"
            + " kicking them across the field and through whatever stands behind. Held longer, it lands harder. With no one there, the kick"
            + " fires a beam out of the leg.");
    public static final FruitMove CLONES = new FruitMove("Yasakani no Magatama: Clones", "[B]", 0, "pika_clones",
        "As at Egghead: the light thrown out of the body and every bead of it a Borsalino of light, each running at someone near with a"
            + " sword of light, cutting, and bursting.");
    public static final FruitMove BODY = new FruitMove("Body of Light", "passive", 0, null,
        "A Logia: blows and shots pass through you as light unless they come coated in Armament Haki. You fly as light (double-tap jump),"
            + " leaving a streak of it, and never take a fall. The sea takes it all away.");
    public static final List<FruitMove> MOVES = List.of(KICKS, LASER, YATA, YASAKANI, MURAKUMO, LIGHT_KICK, CLONES, BODY);

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

    /** The light flight while the fruit is in use and out of the sea (double-tap jump), as the original gave it. */
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
        flight(player, FruitData.active(player) && !FruitServer.inSea(player));
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

    /** Ama no Murakumo guarding: a blow from in front, met with the blade. */
    @Override
    public boolean blocks(ServerPlayer player, DamageSource source) {
        return PikaMoves.guards(player, source);
    }

    @Override
    public void reset() {
        PikaMoves.clear();
    }
}
