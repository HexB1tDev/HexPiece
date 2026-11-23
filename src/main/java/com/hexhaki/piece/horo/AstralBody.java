package com.hexhaki.piece.horo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 3.4.2 (the owner: "Remember that Horo Astral Projection the Projection IS IMMORTAL, and can use fruit moves, however the user's real
 * body left behind is very much damageable, and if killed the projection dies too"): the Horo user's body left standing while their
 * spirit is out, a real body now so it can be struck: every blow on it lands on its owner (server/HoroMoves.bodyHurt), so the body
 * dying is the user dying and the projection with it; mobs that lose the spirit come for it. Every client draws it as before
 * (client/horo/HoroFx: the user's own skin, standing asleep); its renderer draws nothing of its own. It never moves and is never kept:
 * one whose owner is no longer out of their body is gone on its next tick.
 */
public class AstralBody extends PathfinderMob {
    static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.m_135353_(AstralBody.class, EntityDataSerializers.f_135028_);

    private ServerPlayer owner;

    public AstralBody(EntityType<? extends AstralBody> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 20.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22277_, 16.0);
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(OWNER, -1);
    }

    @Override
    protected void m_8099_() {
        // no mind of its own: it stands where it was left
    }

    /** Left by {@code owner} (server side). */
    public void leftBy(ServerPlayer owner) {
        this.owner = owner;
        this.f_19804_.m_135381_(OWNER, owner.m_19879_());
    }

    public ServerPlayer owner() {
        return this.owner;
    }

    public int ownerId() {
        return this.f_19804_.m_135370_(OWNER);
    }

    @Override
    protected void m_8024_() {
        if (this.m_9236_() instanceof ServerLevel) {
            if (this.owner == null || !this.owner.m_6084_() || !com.hexhaki.piece.server.HoroMoves.leftBody(this.owner, this)) {
                this.m_146870_();
                return;
            }
            Vec3 v = this.m_20184_();
            this.m_20256_(new Vec3(0.0, Math.min(0.0, v.f_82480_), 0.0)); // it stands: nothing pushes it about
        }
    }

    /** Every blow lands on its owner (the body is theirs); it never takes harm of its own. */
    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_9236_() instanceof ServerLevel && this.owner != null) {
            com.hexhaki.piece.server.HoroMoves.bodyHurt(this, source, amount);
        }
        return false;
    }

    @Override
    public boolean m_6785_(double distanceSqr) {
        return false;
    }
}
