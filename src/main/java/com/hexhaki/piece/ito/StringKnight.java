package com.hexhaki.piece.ito;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;

/**
 * 3.4.2 (the owner, after playing 3.3.7's Black Knight: "Black knight looks odd as well, it should be a clone that roams on it's own /
 * follows the player, when killed it explodes into tons of strings, if the key is recast while it's alive it explodes into strings
 * too"): Doflamingo's Black Knight, a clone of its weaver woven of string, now a real body: it walks the world (the game's own
 * pathfinding), can be struck and killed, and every client draws it in its weaver's own skin (client/ito/KnightRenderer). Its mind is
 * server/ItoMoves.knightTick (follow, roam, fight the weaver's foes); dead, pressed again or out of time it bursts into strings
 * (ItoMoves.knightBurst). It is never kept: a knight whose weaver is gone (a reload, a logout) unravels on its next tick.
 */
public class StringKnight extends PathfinderMob {
    /** Its weaver's entity id (the skin it wears), whether its strings are Haki-coated, its last lash (1 + which; 0 none) and when. */
    static final EntityDataAccessor<Integer> WEAVER = SynchedEntityData.m_135353_(StringKnight.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> COAT = SynchedEntityData.m_135353_(StringKnight.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> CUT = SynchedEntityData.m_135353_(StringKnight.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> CUT_START = SynchedEntityData.m_135353_(StringKnight.class, EntityDataSerializers.f_135028_);
    /** Its health (twenty hearts: it takes a fight to bring it down). */
    public static final float HEALTH = 40.0F;

    private ServerPlayer weaver;
    private long until;
    private LivingEntity foe;
    private long nextCut;
    private long nextWander;
    private int cuts;
    private boolean burst;

    public StringKnight(EntityType<? extends StringKnight> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.m_21552_()
            .m_22268_(Attributes.f_22276_, HEALTH)
            .m_22268_(Attributes.f_22279_, 0.32)
            .m_22268_(Attributes.f_22277_, 32.0);
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(WEAVER, -1);
        this.f_19804_.m_135372_(COAT, 0);
        this.f_19804_.m_135372_(CUT, 0);
        this.f_19804_.m_135372_(CUT_START, 0);
    }

    @Override
    protected void m_8099_() {
        this.f_21345_.m_25352_(0, new FloatGoal(this));
    }

    /** Woven by {@code weaver} (server side): coated with Haki or not, lasting until {@code until} (game ticks). */
    public void weave(ServerPlayer weaver, boolean coat, long until) {
        this.weaver = weaver;
        this.until = until;
        this.f_19804_.m_135381_(WEAVER, weaver.m_19879_());
        this.f_19804_.m_135381_(COAT, coat ? 1 : 0);
        this.m_21153_(HEALTH);
    }

    /** Its weaver (the server's; null on clients and once its weaver is gone). */
    public ServerPlayer weaver() {
        return this.weaver;
    }

    /** Its weaver's entity id, as every client knows it (-1: none yet). */
    public int weaverId() {
        return this.f_19804_.m_135370_(WEAVER);
    }

    public boolean coated() {
        return this.f_19804_.m_135370_(COAT) != 0;
    }

    /** Its last lash: 0 or 1 (which way, as the weaver's String Slash), -1 none; and the game tick it began (mod 2^30). */
    public int shownCut() {
        return this.f_19804_.m_135370_(CUT) - 1;
    }

    public int shownCutStart() {
        return this.f_19804_.m_135370_(CUT_START);
    }

    public long until() {
        return this.until;
    }

    public LivingEntity foe() {
        return this.foe;
    }

    public void foe(LivingEntity foe) {
        this.foe = foe;
    }

    public long nextCut() {
        return this.nextCut;
    }

    public long nextWander() {
        return this.nextWander;
    }

    public void nextWander(long at) {
        this.nextWander = at;
    }

    /** A lash begins now (every client poses it, ito_slash_1 or _2 by turns); the next not before {@code every} ticks. */
    public int cut(long now, int every) {
        this.nextCut = now + every;
        int which = this.cuts++ % 2;
        this.f_19804_.m_135381_(CUT, which + 1);
        this.f_19804_.m_135381_(CUT_START, (int) (now & 0x3FFFFFFF));
        return which;
    }

    /** Turned to face {@code body}. */
    public void face(LivingEntity body) {
        double dx = body.m_20185_() - this.m_20185_();
        double dz = body.m_20189_() - this.m_20189_();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        this.m_146922_(yaw);
        this.f_20883_ = yaw;
    }

    /** Marks it burst; true if it already had (it bursts once). */
    public boolean burst() {
        boolean was = this.burst;
        this.burst = true;
        return was;
    }

    @Override
    protected void m_8024_() {
        if (this.m_9236_() instanceof ServerLevel level) {
            com.hexhaki.piece.server.ItoMoves.knightTick(this, level);
        }
    }

    /** Its weaver's blows pass through it; a killing blow bursts it into strings at once (no fall, no loot). */
    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_9236_() instanceof ServerLevel && this.weaver != null
            && (source.m_7639_() == this.weaver || source.m_7640_() == this.weaver)) {
            return false;
        }
        boolean hurt = super.m_6469_(source, amount);
        if (this.m_9236_() instanceof ServerLevel && this.m_21223_() <= 0.0F) {
            com.hexhaki.piece.server.ItoMoves.knightBurst(this.weaver, this);
        } else if (hurt && source.m_7639_() instanceof LivingEntity attacker && attacker != this.weaver && attacker != this) {
            this.foe = attacker; // whoever strikes it is fought
        }
        return hurt;
    }

    /** It never goes for being far from players: it ends with its time or its weaver. */
    @Override
    public boolean m_6785_(double distanceSqr) {
        return false;
    }
}
