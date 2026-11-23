package com.hexhaki.piece.server;

import com.hexhaki.piece.PieceEffects;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 3.4.5 (the owner: "whenever the Gura Gura No Mi hits the ultimate move N, this gif I sent fills the screen (in a way that looks good)
 * and plays till the end only once, the user is stunned (and ragdolled)  and a temporary effect is applied called Broken Bones, this
 * causes the player to have slowness 1 and randomly fall into ragdoll at random times as long as the effect is active"; asked: the GIF on
 * the screen of the player who is hit, everyone the ultimate hits, 20 s with a fall every 3 to 6 s, an X-ray overlay): Broken Bones.
 * server/GuraUltimate's strike calls {@link #struck} for everyone it hits (never its user; the strike's own stun and fall come first):
 * the effect for {@link #TICKS}, and a player struck is sent {@link #XRAY} (client/gura/BonesOverlay plays the owner's GIF once over
 * their screen). While it lasts the body is slowed as the game's Slowness I (its own modifier) and every {@link #LEAST} to
 * {@link #LEAST} + {@link #SPREAD} ticks its legs give way: a short ragdoll fall (HakiFalls.knockDown, {@link #FALL} ticks).
 */
public final class BrokenBones extends MobEffect {
    /** How long it lasts (20 s), the falls' spacing (3 to 6 s), each fall's length (ticks). */
    public static final int TICKS = 400;
    public static final int LEAST = 60;
    public static final int SPREAD = 60;
    public static final int FALL = 30;
    /** S2CUoFx kind (the Gura fruit's 60 to 79; client/dragon/UoFx routes it to client/gura/BonesOverlay): the X-ray on one's screen. */
    public static final int XRAY = 73;
    /** As the game's Slowness I (-15 % of the speed). */
    static final double SLOW = -0.15;
    static final String SLOW_ID = "6b2e9c41-0d7f-4a53-9e18-3c5a7f20b8d4";
    /** Each body's next fall (game time). */
    static final Map<UUID, Long> NEXT = new HashMap<>();

    public BrokenBones() {
        super(MobEffectCategory.HARMFUL, 0xD8CDB4);
        this.m_19472_(Attributes.f_22279_, SLOW_ID, SLOW, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** Looked at every 5 ticks. */
    @Override
    public boolean m_6584_(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public void m_6742_(LivingEntity body, int amplifier) {
        if (!(body.m_9236_() instanceof ServerLevel level) || !body.m_6084_()) {
            return;
        }
        long now = level.m_46467_();
        UUID id = body.m_20148_();
        Long next = NEXT.get(id);
        if (next == null) {
            NEXT.put(id, now + LEAST + body.m_217043_().m_188503_(SPREAD + 1));
            return;
        }
        if (now >= next) {
            HakiFalls.knockDown(level, body, FALL, false);
            NEXT.put(id, now + FALL + LEAST + body.m_217043_().m_188503_(SPREAD + 1));
        }
    }

    /**
     * Struck by the Gura ultimate: Broken Bones for {@link #TICKS} (its first random fall only after the strike's own stun of
     * {@code stunned} ticks), and on a player's own screen the X-ray of their skeleton breaking.
     */
    public static void struck(ServerLevel level, LivingEntity body, int stunned) {
        if (!body.m_6084_()) {
            return;
        }
        long now = level.m_46467_();
        body.m_7292_(new MobEffectInstance(PieceEffects.BROKEN_BONES.get(), TICKS, 0, false, false, true));
        if (NEXT.size() > 512) {
            NEXT.values().removeIf(t -> t < now - TICKS);
        }
        NEXT.put(body.m_20148_(), now + stunned + LEAST + body.m_217043_().m_188503_(SPREAD + 1));
        if (body instanceof ServerPlayer player) {
            PieceNetwork.sendTo(player, new S2CUoFx(XRAY, player.m_19879_(), player.m_19879_(), player.m_20185_(), player.m_20186_(),
                player.m_20189_(), 0, 0, 0, 0, 0, 0, 0, 0));
        }
    }
}
