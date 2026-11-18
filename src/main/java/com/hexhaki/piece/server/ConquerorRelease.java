package com.hexhaki.piece.server;

import com.hexhaki.command.HakiCommands;
import com.hexhaki.data.ConquerorMode;
import com.hexhaki.data.HakiData;
import com.hexhaki.data.HakiType;
import com.hexhaki.gameplay.ConquerorLightningPath;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.network.msg.S2CHakiVisual;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.hexhaki.piece.haki.ConquerorStun;
import com.hexhaki.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 2.2.5: where the original's Conqueror's release asks HexPiece (calls redirected by {@code patch/PiecePatch.java} in
 * {@code HakiServerController.releaseConqueror}).
 * <ul>
 *   <li>The owner: "the hexpiece joyboy on/off, is not a thing anymore, anyone by default at max charge shall be able to do that. (keep
 *       and override the default max conqueror with the joyboy one since it's better.)": the release's JoyBoy switch reads true
 *       ({@link #joyBoy}; its mastery 1000 is 0 by the patch), so every full-charge radial release is the supreme one, for everyone;
 *       {@code /hp joyboy} says so ({@link #joyBoyCommand}).</li>
 *   <li>The owner: "At full charge, it should cause severe tears on the floor like fissures where the lightnings hit, then repair
 *       themselves automaticly (same as HexGhoul)": the release's visual goes out with a seed chosen here ({@link #visual}, as the
 *       original chose it: the player's random), kept for the moment; the supreme release's permanent destruction (the original's
 *       {@code SupremeHakiDestruction}) is the self-repairing fissures under the storm that seed draws ({@link #fissures},
 *       {@link Fissures}).</li>
 *   <li>2.4.7, the owner: "the crackle lightning sound at the end is to be removed along with all the old sounds of the letting go of G
 *       button max charge, keep the explosion sound and the new one we just added": the full release keeps only the original's blast
 *       (the explosion) and the evil roar ({@link AlignmentSounds}); its snap ({@link #sound}) and its rumble ({@link #rumble}) are
 *       gone, and its lightning lands silently (client/haki/HakiStorm, HakiBolts). A partial release keeps its own sound.</li>
 *   <li>2.4.9, the owner: "every mob hit with the lightning from the Conqueror's release is to be fucking felled ... the longer you hold
 *       the G button, at max charge, I think the stun should go up to 10 seconds", then by alignment ({@link ConquerorStun}): the
 *       radial release's targets ({@link #target}), its damage ({@link #hurt}) and its full charge's stun ({@link #forceMax}) are
 *       HexPiece's, and what its lightning strikes is found here ({@link #struck}: the bolts the clients draw, rebuilt from the
 *       release's seed: its range lightning, and a full charge's storm). Each release is {@link #current} from its visual to its
 *       sounds, all within the original's {@code releaseConqueror}, in one server tick.</li>
 * </ul>
 */
public final class ConquerorRelease {
    /** The seed of each player's release just sent, by entity id (taken by the fissures in the same tick). */
    private static final Map<Integer, Long> SEEDS = new HashMap<>();
    /** 2.4.9: the game tick each charging player's charge filled (a full charge's stun grows with how long G is held after). */
    private static final Map<UUID, Long> FULL_SINCE = new HashMap<>();
    /** 2.4.9: the radial release being let out now (between its visual and its sounds, in one tick), or null. */
    static Release current;

    /** 2.4.9: a radial release being let out: who, when, its seed, charge, mastery, the releaser's alignment, and its stun. */
    static final class Release {
        final ServerPlayer player;
        final long tick;
        final long seed;
        final float power;
        final int mastery;
        final int alignment;
        final boolean full;
        final int stun;
        /** The bodies its lightning strikes (found as the release's damage is dealt), and (2.4.9) the bodies it has hurt. */
        List<LivingEntity> struck = List.of();
        final Set<Integer> hurt = new HashSet<>();

        Release(ServerPlayer player, long seed, float power) {
            this.player = player;
            this.tick = player.m_9236_().m_46467_();
            this.seed = seed;
            this.power = power;
            this.mastery = HakiData.mastery(player, HakiType.CONQUEROR);
            this.alignment = ProfileServer.of(player).alignment();
            this.full = power >= 0.999F;
            Long since = FULL_SINCE.get(player.m_20148_());
            this.stun = ConquerorStun.full(this.alignment, since == null ? 0L : Math.max(0L, this.tick - since));
        }
    }

    private ConquerorRelease() {
    }

    /** The release's JoyBoy switch: always on. */
    public static boolean joyBoy(Player player) {
        return true;
    }

    /** The original's {@code HakiServerController.visual}, with the seed kept. */
    public static void visual(ServerPlayer player, S2CHakiVisual.Visual visual, float power, int variant) {
        WaterPrisons.conqueror(player); // 2.4.0: a Water Prison round the one letting it out bursts
        HakiFalls.released(player); // 2.4.4: the bodies it knocks down fall away from it
        long seed = player.m_217043_().m_188505_();
        current = null;
        if (visual == S2CHakiVisual.Visual.CONQUEROR_RELEASE) {
            SEEDS.put(player.m_19879_(), seed);
            AlignmentSounds.released(player, power); // 2.4.6: an evil player's full release roars
            try {
                current = new Release(player, seed, power);
            } catch (RuntimeException failure) {
                current = null; // the release goes on as the original's
            }
        }
        HakiNetwork.tracking(player, new S2CHakiVisual(player.m_19879_(), visual, power, variant, seed));
    }

    /** In place of the supreme release's permanent destruction: fissures under the storm, that heal. */
    public static void fissures(ServerPlayer player, double radius) {
        Long seed = SEEDS.remove(player.m_19879_());
        if (seed != null) {
            Fissures.open(player, seed, HakiData.mastery(player, HakiType.CONQUEROR));
        }
    }

    /**
     * The release's sounds ({@code HakiServerController.sound}, which plays each a second later, as the original's {@code delayedSound}
     * does): all but the full release's snap.
     */
    public static void sound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        current = null; // the release is done (its sounds come last)
        if (sound == ModSounds.CONQUEROR_SUPREME_SNAP.get()) {
            return;
        }
        HakiServerController.delayedSound(player, sound, volume, pitch);
    }

    /** The full release's rumble ({@code scheduleSupremeAudio}): left out. */
    public static void rumble(ServerPlayer player) {
    }

    /** The release being let out by {@code player} now, or null. */
    static Release active(ServerPlayer player) {
        Release r = current;
        return r != null && r.player == player && r.tick == player.m_9236_().m_46467_() ? r : null;
    }

    /** 2.4.9: called each server tick for a player (AlignmentSounds): the moment their Conqueror's charge fills is kept. */
    static void charging(ServerPlayer player, boolean charging, int charge) {
        UUID id = player.m_20148_();
        if (!charging) {
            FULL_SINCE.remove(id);
        } else if (charge >= 100 && !FULL_SINCE.containsKey(id)) {
            FULL_SINCE.put(id, player.m_9236_().m_46467_());
        }
    }

    static void loggedOut(UUID id) {
        FULL_SINCE.remove(id);
    }

    /**
     * 2.4.9: the original's {@code applyConqueror} (the release's push, damage and effects on what is in its radius). A radial release:
     * what its lightning strikes is found first (so its damage can follow the releaser's alignment, {@link #hurt}); after it, a partial
     * charge's lightning fells whatever it struck for {@link ConquerorStun#LIGHTNING} ticks (any alignment), and an evil full charge's
     * lightning hurts and fells what it struck beyond the radius (its storm runs far past it).
     */
    public static void apply(ServerPlayer player, double radius, float power, ConquerorMode mode, boolean joy, boolean maxHold, boolean tapped) {
        Release r = tapped ? null : active(player);
        if (r != null) {
            try {
                r.struck = struck(r, radius);
            } catch (RuntimeException failure) {
                r.struck = List.of();
            }
        }
        HakiServerController.applyConqueror(player, radius, power, mode, joy, maxHold, tapped);
        if (r == null || !(player.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        boolean evil = com.hexhaki.piece.Alignment.evil(r.alignment);
        Vec3 at = player.m_20182_();
        for (LivingEntity body : r.struck) {
            if (!body.m_6084_()) {
                continue;
            }
            // an evil release's lightning takes half of what each body it strikes has left (once: not again for one hurt in the radius)
            if (evil && r.hurt.add(body.m_19879_())) {
                halve(body, player.m_269291_().m_269075_(player));
            }
            if (!r.full) {
                fell(level, body, ConquerorStun.LIGHTNING);
            } else if (evil && body.m_20182_().m_82554_(at) > radius) {
                fell(level, body, r.stun);
            }
        }
    }

    /**
     * 2.4.9 (the owner: "half the current HP of the mob or person hit. Always half, but even if the player or entity has half a heart,
     * then it doesn't deal any damage. It's not meant to kill."): {@code body} hurt by {@code source} for half its health left, and
     * brought to exactly that half if its armour took some of the blow; nothing at half a heart or under; never killed. A body the game
     * keeps from harm (a creative player, one just hurt) is not hurt.
     */
    static boolean halve(LivingEntity body, DamageSource source) {
        float health = body.m_21223_();
        float amount = ConquerorStun.evilDamage(health);
        if (amount <= 0.0F) {
            return false;
        }
        float left = health - amount;
        boolean hit = body.m_6469_(source, amount);
        if (hit && body.m_6084_() && body.m_21223_() > left) {
            body.m_21153_(left);
        }
        return hit;
    }

    /**
     * 2.4.9: the original's {@code forceMaxConquerorStun} (a full charge's stun on what is in its radius) for the releaser's stun
     * ({@link ConquerorStun#full}: by alignment, longer the longer G was held after the charge filled); every body it fells is down
     * (the ones already down too) and slowed and weakened for exactly that long (the original's push gave them 10 seconds of it).
     */
    public static void forceMax(ServerPlayer player, double radius, int ticks) {
        Release r = active(player);
        int stun = r != null ? r.stun : ticks;
        HakiServerController.forceMaxConquerorStun(player, radius, stun);
        if (r == null || !(player.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        for (LivingEntity body : level.m_6443_(LivingEntity.class, player.m_20191_().m_82400_(radius),
            e -> e != player && e.m_6084_() && e.m_20280_(player) <= radius * radius && target(player, e))) {
            body.m_21195_(MobEffects.f_19597_);
            body.m_21195_(MobEffects.f_19613_);
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, stun, 10, false, false));
            body.m_7292_(new MobEffectInstance(MobEffects.f_19613_, stun, 6, false, false));
            HakiFalls.knockDown(level, body, stun, false);
        }
    }

    /** {@code body} felled for {@code ticks}: the original's forced stun (frozen, its aim dropped) and the fall (HakiFalls). */
    static void fell(ServerLevel level, LivingEntity body, int ticks) {
        if (!body.m_6084_()) {
            return;
        }
        body.m_7292_(new MobEffectInstance(MobEffects.f_19613_, ticks, 3, false, false));
        HakiFalls.knockDown(level, body, ticks, false);
    }

    /**
     * 2.4.9: whom the release takes ({@code isConquerorCombatTarget} in the original's {@code applyConqueror} and
     * {@code forceMaxConquerorStun}): an evil full charge every mob and player but the releaser's own mount and riders; else the
     * original's (players, hostile mobs, bosses, a mob that targets the releaser).
     */
    public static boolean target(ServerPlayer source, LivingEntity body) {
        Release r = active(source);
        if (r != null && ConquerorStun.fellsAll(r.alignment, r.full)) {
            return felled(source, body);
        }
        return HakiServerController.isConquerorCombatTarget(source, body);
    }

    /** A body any release may fell: a mob or a player, not the releaser, nor what they ride, nor what rides them. */
    static boolean felled(ServerPlayer source, LivingEntity body) {
        return body != source && (body instanceof Mob || body instanceof Player) && body != source.m_20202_() && body.m_20202_() != source;
    }

    /**
     * 2.4.9: the release's damage ({@code LivingEntity.hurt} in the original's {@code applyConqueror}): a good player's hurts no one,
     * an evil player's takes half of what is left ({@link #halve}), a neutral one's as the original's.
     */
    public static boolean hurt(LivingEntity body, DamageSource source, float amount) {
        Release r = current;
        if (r != null && r.tick == body.m_9236_().m_46467_()) {
            if (!ConquerorStun.hurts(r.alignment)) {
                return false;
            }
            if (com.hexhaki.piece.Alignment.evil(r.alignment)) {
                return r.hurt.add(body.m_19879_()) && halve(body, source);
            }
        }
        return body.m_6469_(source, amount);
    }

    /**
     * 2.4.9: the bodies (mobs and players, not the releaser) the release's lightning strikes, as the clients draw it from its seed: its
     * range lightning (the bolts {@code ConquerorLightningPath.generate} lays out from the releaser's feet; struck as the original's
     * charge lightning strikes, through the body's box grown by a little), and a full charge's storm ({@link ConquerorStorm}, laid on
     * the ground: struck where a strand runs by the body at its height).
     */
    static List<LivingEntity> struck(Release r, double radius) {
        ServerPlayer player = r.player;
        if (!(player.m_9236_() instanceof ServerLevel level)) {
            return List.of();
        }
        Vec3 at = player.m_20182_();
        List<ConquerorLightningPath.Bolt> bolts = ConquerorLightningPath.generate(r.power, r.mastery, r.seed, true);
        List<ConquerorStorm.Strand> storm = r.full ? ConquerorStorm.release(r.mastery, r.seed, true) : List.of();
        double reach = (r.full ? Math.max(radius, ConquerorStorm.reach(r.mastery)) : radius) + 3.0;
        double grow = 0.52 + 0.24 * r.power;
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity body : level.m_6443_(LivingEntity.class, player.m_20191_().m_82400_(reach),
            e -> e.m_6084_() && felled(player, e))) {
            AABB box = body.m_20191_().m_82400_(grow);
            boolean hit = false;
            for (ConquerorLightningPath.Bolt bolt : bolts) {
                List<Vec3> points = bolt.points();
                for (int i = 1; i < points.size() && !hit; i++) {
                    hit = box.m_82371_(at.m_82549_(points.get(i - 1)), at.m_82549_(points.get(i))).isPresent();
                }
                if (hit) {
                    break;
                }
            }
            if (!hit && !storm.isEmpty()) {
                double bottom = body.m_20096_() ? 0.0 : Math.max(0.0, body.m_20186_() - at.f_82480_);
                hit = ConquerorStorm.strikes(storm, body.m_20185_() - at.f_82479_, body.m_20189_() - at.f_82481_, bottom,
                    bottom + body.m_20206_(), body.m_20205_() / 2.0 + 0.6);
            }
            if (hit) {
                out.add(body);
            }
        }
        return out;
    }

    /**
     * 2.5.1 (the owner: "evil gets one animation (EVIL/Disturbing) and good gets another (Peaceful/in pain) like a hand on the face and
     * then a stare"): the original's {@code animate} in the Conqueror's charge and release, the animation the releaser's alignment's
     * ({@link ConquerorStun#animation}).
     */
    public static void animate(ServerPlayer player, String name, int fade) {
        String chosen = name;
        try {
            chosen = ConquerorStun.animation(name, ProfileServer.of(player).alignment());
        } catch (RuntimeException failure) {
            // the original's
        }
        HakiServerController.animate(player, chosen, fade);
    }

    /** {@code /hp joyboy on|off}: there is no switch any more. */
    public static int joyBoyCommand(CommandSourceStack source, ServerPlayer player, boolean on) {
        HakiCommands.reply(source, "§eJoyBoy is not a switch any more: every full-charge Conqueror's release is the JoyBoy one, for everyone.");
        return 1;
    }
}
