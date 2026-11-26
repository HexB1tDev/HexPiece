package com.hexhaki.piece.server;

import com.hexhaki.piece.ironfist.IronSounds;
import com.hexhaki.piece.neko.LeopardForm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.7.9 (the owner: "first of all, Z (transformation, get the base animations, and m1s.)"; the reference LeopardV2's M1, Six Styles: "a
 * 5-hit combo of kicks, punches and a mini Rokuogan blast at the last", read frame by frame: docs/handoff/next-ai-3.7.6.md): M1 in both
 * forms. Each click throws the next blow of the five (a pause longer than {@link #COMBO_WINDOW} starts again from the first): a front kick,
 * a claw swipe, a roundhouse kick, a double punch, and Rokuogan, the shock of both fists thrust out together that throws whoever it hits
 * far away. The human plays the clips neko_m1_1 to 5 (tools/neko_anims.py); the leopard its own takes (client/neko/NekoFx: combo_a,
 * combo_b, combo_c, skill_a, skill_b). The blow lands {@link #M1_HIT} ticks after the click on whoever is in front (as the leopard
 * further: it is 5 blocks tall); "M1s the same damage" in both forms; the Conqueror's lightning at each one struck, out of Rokuogan's
 * blast (POINT ONE). Every client draws it from small messages (S2CUoFx kinds {@link #M1} and {@link #M1_LAND}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NekoMoves {
    /** A blow thrown: user; bx the blow (1 to 5), by 1 as the leopard; x, y, z the chest, a the way. */
    public static final int M1 = 421;
    /** A blow landing: user; bx the blow, by 1 as the leopard; x, y, z where it lands (the struck one's middle, or the air in front). */
    public static final int M1_LAND = 422;
    public static final int BLOWS = 5;
    /** Ticks between clicks (the human's, the leopard's), after the fifth, the pause that starts the combo again, the blow's beat. */
    public static final int M1_EVERY = 8;
    public static final int M1_EVERY_LEOPARD = 7;
    public static final int COMBO_REST = 16;
    public static final int COMBO_WINDOW = 26;
    public static final int M1_HIT = 3;
    /** Damage (the reference's 69 over five, about a fifth), the reach (blocks) and the arc in front (degrees, each side). */
    public static final float[] M1_HURT = {2.0F, 2.0F, 2.0F, 2.5F, 5.0F};
    public static final double REACH = 3.4;
    public static final double REACH_LEOPARD = 5.2;
    static final double ARC = 55.0;
    /** Rokuogan's throw (blocks a tick along the look, up). */
    static final double ROKU_PUSH = 2.0;
    static final double ROKU_LIFT = 0.55;

    static final class State {
        int combo;
        long last = -1000;
        long next;
    }

    /** A blow due to land. */
    record Due(ServerPlayer player, long at, int blow, boolean leopard) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<Due> DUE = new ArrayList<>();

    private NekoMoves() {
    }

    /** M1 (the kit's attack button), in either form; not while it changes. */
    static void m1(ServerPlayer player) {
        int phase = NekoServer.phase(player);
        if (phase == LeopardForm.FORMING || phase == LeopardForm.UNFORMING || player.m_20159_()) {
            return;
        }
        boolean leopard = phase == LeopardForm.LEOPARD;
        State s = STATES.computeIfAbsent(player.m_20148_(), k -> new State());
        long now = player.m_9236_().m_46467_();
        if (now < s.next) {
            return;
        }
        if (now - s.last > COMBO_WINDOW) {
            s.combo = 0;
        }
        int blow = s.combo % BLOWS + 1;
        s.combo = blow == BLOWS ? 0 : s.combo + 1;
        s.last = now;
        s.next = now + (leopard ? M1_EVERY_LEOPARD : M1_EVERY) + (blow == BLOWS ? COMBO_REST : 0);
        if (!leopard) {
            ItoMoves.animate(player, "neko_m1_" + blow);
        }
        Vec3 look = player.m_20154_();
        ItoMoves.fx(player, M1, blow, ItoMoves.chest(player), look, blow, leopard ? 1 : 0, 0, blow == BLOWS ? 16 : 10);
        ItoMoves.sound(player, ItoMoves.chest(player), IronSounds.SWING, leopard ? 1.0F : 0.7F, leopard ? 0.8F : 1.1F + blow * 0.04F);
        Due due = new Due(player, now + (blow == BLOWS ? M1_HIT + 2 : M1_HIT), blow, leopard);
        DUE.add(due);
        // 3.8.4: on the clash board (HexGhoul's): a blow coming back at this one as it lands meets it (server/Clashes)
        ClashBoard.swing(player, now, due.at(), leopard ? REACH_LEOPARD : REACH, M1_HURT[blow - 1],
            blow == 1 || blow == 3 ? com.hexhaki.piece.clash.ClashRules.Kind.KICK : com.hexhaki.piece.clash.ClashRules.Kind.FIST, () -> true,
            () -> DUE.remove(due));
    }

    /** The blow lands: whoever is in front struck, pushed (Rokuogan throws them far), the lightning at each. */
    static void land(Due due) {
        ServerPlayer player = due.player();
        if (!player.m_6084_()) {
            return;
        }
        // 3.8.4: met by a blow coming back at it: the clash plays instead (server/Clashes)
        boolean met = Clashes.landing(player, player.m_9236_().m_46467_(), other -> com.hexhaki.fruit.FruitUtil.foe(player, other));
        ClashBoard.landed(player);
        if (met) {
            return;
        }
        int blow = due.blow();
        boolean leopard = due.leopard();
        double reach = leopard ? REACH_LEOPARD : REACH;
        Vec3 look = player.m_20154_();
        Vec3 flat = ItoMoves.flat(player);
        Vec3 point = ItoMoves.chest(player).m_82549_(look.m_82490_(reach * 0.6));
        boolean any = false;
        for (LivingEntity body : ItoMoves.ahead(player, reach, ARC)) {
            float damage = M1_HURT[blow - 1];
            Vec3 push = blow == BLOWS ? new Vec3(flat.f_82479_ * ROKU_PUSH, ROKU_LIFT, flat.f_82481_ * ROKU_PUSH)
                : flat.m_82490_(0.3).m_82520_(0.0, 0.1, 0.0);
            if (MaguMoves.hurt(player, body, damage)) {
                StyleCombat.push(body, push);
            }
            Vec3 middle = StyleCombat.middle(body);
            if (!any) {
                point = middle;
            }
            any = true;
            ItoMoves.lightning(player, middle);
        }
        ItoMoves.fx(player, M1_LAND, blow, point, look, blow, leopard ? 1 : 0, any ? 1 : 0, 0);
        if (any) {
            ItoMoves.sound(player, point, blow == BLOWS ? IronSounds.BOOM : IronSounds.PUNCH, blow == BLOWS ? 1.6F : 1.0F,
                blow == BLOWS ? 1.1F : 0.95F + blow * 0.05F);
        }
        if (blow == BLOWS) {
            // Rokuogan: a blast of shock out of the fists, whatever it meets (the Conqueror's lightning out of it)
            ConquerorAura.exploded(player, (ServerLevel) player.m_9236_(), point, 2.5);
            if (!any) {
                ItoMoves.sound(player, point, IronSounds.BOOM, 1.1F, 1.25F);
            }
        }
    }

    static void forget(ServerPlayer player) {
        STATES.remove(player.m_20148_());
        DUE.removeIf(d -> d.player() == player);
    }

    static void clear() {
        STATES.clear();
        DUE.clear();
    }

    @SubscribeEvent
    public static void nekoTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || DUE.isEmpty() || !(event.level instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        List<Due> ready = new ArrayList<>();
        for (Iterator<Due> it = DUE.iterator(); it.hasNext(); ) {
            Due d = it.next();
            if (d.player().m_9236_() == level && now >= d.at()) {
                it.remove();
                ready.add(d);
            } else if (now - d.at() > 40) {
                it.remove(); // its player gone to another world or away
            }
        }
        for (Due d : ready) {
            land(d);
        }
    }
}
