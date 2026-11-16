package com.hexhaki.piece.npc;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.server.ProfileServer;
import com.hexhaki.piece.server.StyleCombat;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.fx.Impact;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 2.1.1 (the owner: "Add pirate NPCS and Marines"): a pirate or a marine (its side is its entity type). Grunts, officers and captains
 * ({@link SeaRules}), each with a look (a skin, a man or a woman) and a fighting style of its side. It hunts the other side's fighters and
 * the players who sail under the other flag (and whoever strikes it), and fights with the very moves players do: the same strings,
 * signatures and specials, landed through the same combat ({@link StyleCombat}) and drawn with the same choreography on every client.
 */
public class SeaFighter extends PathfinderMob implements SeaNpc {
    static final EntityDataAccessor<Integer> LOOK = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    /** The move under way (its ordinal + 1; 0: none) and the game tick it began (mod 2^30), for every client's pose. */
    static final EntityDataAccessor<Integer> MOVE = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> MOVE_START = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final String SAVED = "HexPieceFighter";

    private boolean ready;
    private StyleMove move;
    private long moveStart;
    private boolean counterUsed;
    private Slot nextString = Slot.M1_1;
    private long nextAttack;
    private long nextWander;
    private int retarget;
    private LivingEntity foe;
    private final Map<StyleMove, Long> cooldowns = new EnumMap<>(StyleMove.class);
    private final Set<Integer> struck = new HashSet<>();

    public SeaFighter(EntityType<? extends SeaFighter> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.m_21552_()
            .m_22268_(Attributes.f_22276_, SeaRules.HEALTH[0])
            .m_22268_(Attributes.f_22279_, SeaRules.SPEED[0])
            .m_22268_(Attributes.f_22277_, 32.0)
            .m_22268_(Attributes.f_22278_, 0.15);
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(LOOK, 0);
        this.f_19804_.m_135372_(STYLE, FightingStyle.BRAWLER.ordinal());
        this.f_19804_.m_135372_(MOVE, 0);
        this.f_19804_.m_135372_(MOVE_START, 0);
    }

    @Override
    protected void m_8099_() {
        this.f_21345_.m_25352_(0, new FloatGoal(this));
        this.f_21345_.m_25352_(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.f_21345_.m_25352_(8, new RandomLookAroundGoal(this));
    }

    @Override
    public Faction faction() {
        return this.m_6095_() == PieceEntities.MARINE.get() ? Faction.MARINE : Faction.PIRATE;
    }

    @Override
    public int rank() {
        return SeaRules.rankOf(this.look());
    }

    public int look() {
        return this.f_19804_.m_135370_(LOOK);
    }

    public boolean female() {
        return SeaRules.female(this.look());
    }

    public FightingStyle style() {
        return FightingStyle.byOrdinal(this.f_19804_.m_135370_(STYLE));
    }

    /** The move under way as every client sees it (null: none), and since when (game ticks mod 2^30). */
    public StyleMove shownMove() {
        int ordinal = this.f_19804_.m_135370_(MOVE);
        return ordinal <= 0 ? null : StyleMove.byOrdinal(ordinal - 1);
    }

    public int shownStart() {
        return this.f_19804_.m_135370_(MOVE_START);
    }

    /** Made what it is: its look (so its rank), its style; its health and speed to its rank; named; kept in its saved data. */
    public void setup(int look, FightingStyle style) {
        int rank = SeaRules.rankOf(look);
        this.f_19804_.m_135381_(LOOK, look);
        this.f_19804_.m_135381_(STYLE, style.ordinal());
        this.m_21051_(Attributes.f_22276_).m_22100_(SeaRules.HEALTH[rank]);
        this.m_21051_(Attributes.f_22279_).m_22100_(SeaRules.SPEED[rank]);
        this.m_21153_(this.m_21233_());
        this.m_6593_(Component.m_237113_(SeaRules.title(this.faction(), rank)));
        CompoundTag saved = new CompoundTag();
        saved.m_128405_("Look", look);
        saved.m_128405_("Style", style.ordinal());
        this.getPersistentData().m_128365_(SAVED, saved);
        this.ready = true;
    }

    /** After a load (or a first tick with nothing set): what it was, from its saved data, or a grunt of a style of its side. */
    void restore() {
        CompoundTag data = this.getPersistentData();
        if (data.m_128441_(SAVED)) {
            CompoundTag saved = data.m_128469_(SAVED);
            this.setup(saved.m_128451_("Look"), FightingStyle.byOrdinal(saved.m_128451_("Style")));
        } else {
            FightingStyle[] styles = SeaRules.styles(this.faction());
            this.setup(SeaRules.look(0, this.m_217043_().m_188503_(8)), styles[this.m_217043_().m_188503_(styles.length)]);
        }
    }

    boolean ally(LivingEntity other) {
        if (other instanceof SeaFighter fighter) {
            return fighter.faction() == this.faction();
        }
        return other instanceof ServerPlayer player && ProfileServer.of(player).faction() == this.faction();
    }

    /** Someone it fights: the other side's fighters, players under the other flag (not in creative), or whoever it is already after. */
    boolean foe(LivingEntity other) {
        if (other == null || other == this || !other.m_6084_() || other.m_5833_() || this.ally(other)) {
            return false;
        }
        if (other instanceof Player player && player.m_7500_()) {
            return false;
        }
        if (other == this.foe) {
            return true;
        }
        if (other instanceof SeaFighter fighter) {
            return fighter.faction() == this.faction().enemy();
        }
        return other instanceof ServerPlayer player && ProfileServer.of(player).faction() == this.faction().enemy();
    }

    LivingEntity findFoe() {
        LivingEntity hurtBy = this.m_21188_();
        if (hurtBy != null && this.foeOrRevenge(hurtBy) && hurtBy.m_20280_(this) < SeaRules.CHASE * SeaRules.CHASE) {
            return hurtBy;
        }
        AABB around = this.m_20191_().m_82400_(SeaRules.SIGHT);
        LivingEntity best = null;
        double nearest = Double.MAX_VALUE;
        List<LivingEntity> seen = this.m_9236_().m_6443_(LivingEntity.class, around, this::foe);
        for (LivingEntity other : seen) {
            double distance = other.m_20280_(this);
            if (distance < nearest) {
                nearest = distance;
                best = other;
            }
        }
        return best;
    }

    boolean foeOrRevenge(LivingEntity other) {
        if (other == null || !other.m_6084_() || other.m_5833_() || this.ally(other)) {
            return false;
        }
        return !(other instanceof Player player && player.m_7500_());
    }

    void face(LivingEntity target) {
        double dx = target.m_20185_() - this.m_20185_();
        double dz = target.m_20189_() - this.m_20189_();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        this.m_146922_(yaw);
        this.f_20883_ = yaw;
    }

    @Override
    protected void m_8024_() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (!this.ready) {
            this.restore();
        }
        long now = level.m_46467_();
        if (this.foe != null && (!this.foe.m_6084_() || this.foe.m_20280_(this) > SeaRules.CHASE * SeaRules.CHASE || this.ally(this.foe))) {
            this.foe = null;
        }
        if (--this.retarget <= 0) {
            this.retarget = 10;
            LivingEntity found = this.findFoe();
            if (found != null) {
                this.foe = found;
            }
        }
        if (this.move != null) {
            this.tickMove(level, now);
            return;
        }
        if (this.foe == null) {
            if (now >= this.nextWander && this.m_21573_().m_26571_()) {
                this.nextWander = now + 80 + this.m_217043_().m_188503_(120);
                double angle = this.m_217043_().m_188500_() * Math.PI * 2;
                double reach = 4 + this.m_217043_().m_188500_() * 7;
                this.m_21573_().m_26519_(this.m_20185_() + Math.cos(angle) * reach, this.m_20186_(), this.m_20189_() + Math.sin(angle) * reach,
                    0.8);
            }
            return;
        }
        this.m_21563_().m_24960_(this.foe, 30.0F, 30.0F);
        double distance = Math.sqrt(this.foe.m_20280_(this));
        if (distance > SeaRules.STRIKE) {
            if (now % 5 == 0) {
                this.m_21573_().m_5624_(this.foe, this.rank() == 0 ? 1.15 : 1.25);
            }
            return;
        }
        this.m_21573_().m_26573_();
        this.face(this.foe);
        if (now >= this.nextAttack) {
            this.begin(this.choose(now), now);
        }
    }

    boolean cooled(StyleMove move, long now) {
        Long until = this.cooldowns.get(move);
        return move != null && (until == null || now >= until);
    }

    StyleMove choose(long now) {
        FightingStyle style = this.style();
        int rank = this.rank();
        float roll = this.m_217043_().m_188501_();
        StyleMove specialB = StyleMove.of(style, Slot.SPECIAL_B);
        StyleMove specialA = StyleMove.of(style, Slot.SPECIAL_A);
        StyleMove heavy = StyleMove.of(style, Slot.HEAVY);
        if (rank >= 2 && roll < 0.14F && this.cooled(specialB, now)) {
            return specialB;
        }
        if (rank >= 1 && roll < 0.3F && this.cooled(specialA, now)) {
            return specialA;
        }
        if (roll < 0.45F && this.cooled(heavy, now)) {
            return heavy;
        }
        StyleMove blow = StyleMove.of(style, this.nextString);
        this.nextString = StyleMove.nextInString(this.nextString);
        return blow;
    }

    void begin(StyleMove move, long now) {
        if (move == null) {
            return;
        }
        this.move = move;
        this.moveStart = now;
        this.counterUsed = false;
        this.struck.clear();
        if (move.cooldown() > 0) {
            this.cooldowns.put(move, now + move.cooldown() * 2L);
        }
        boolean finisher = move.slot() == Slot.M1_4;
        int pause = this.rank() == 2 ? 2 : this.rank() == 1 ? 4 : 7;
        this.nextAttack = now + (move.slot().string() && !finisher ? move.cancel() + this.m_217043_().m_188503_(pause)
            : move.duration() + 6 + this.m_217043_().m_188503_(10));
        if (finisher) {
            this.nextString = Slot.M1_1;
        }
        this.f_19804_.m_135381_(MOVE, move.ordinal() + 1);
        this.f_19804_.m_135381_(MOVE_START, (int) (now & 0x3FFFFFFF));
    }

    void tickMove(ServerLevel level, long now) {
        int tick = (int) (now - this.moveStart);
        if (tick >= this.move.duration() || !this.m_6084_()) {
            this.move = null;
            this.f_19804_.m_135381_(MOVE, 0);
            return;
        }
        if (this.foe != null) {
            this.face(this.foe);
        }
        StyleCombat.carry(this, this.move, tick);
        int mastery = SeaRules.MASTERY[this.rank()];
        DamageSource source = this.m_269291_().m_269333_(this);
        for (Hit hit : this.move.hits()) {
            if (hit.tick() == tick) {
                StyleCombat.land(this, this.move, hit, mastery, this::foe, this.struck, source);
            }
        }
    }

    /** Iron Body takes a fifth of each blow; the Shark Skin Counter turns the first aside and pays it back; whoever strikes is hunted. */
    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_9236_() instanceof ServerLevel && this.move != null && source.m_7639_() instanceof LivingEntity attacker && attacker != this) {
            if (this.move.effect() == StyleMove.Effect.GUARD) {
                amount *= 0.2F;
            } else if (this.move.effect() == StyleMove.Effect.COUNTER && !this.counterUsed) {
                this.counterUsed = true;
                Vec3 away = attacker.m_20182_().m_82546_(this.m_20182_());
                double length = Math.max(0.01, Math.sqrt(away.f_82479_ * away.f_82479_ + away.f_82481_ * away.f_82481_));
                attacker.f_19802_ = 0;
                attacker.m_6469_(this.m_269291_().m_269333_(this), 9.0F * PieceRules.masteryDamage(SeaRules.MASTERY[this.rank()]));
                Impact.hit(this, attacker, Impact.Spec.heavy(new Vec3(away.f_82479_ / length * 1.4, 0.3, away.f_82481_ / length * 1.4)));
                StyleCombat.sound(this, "fx.counter", 1.0F);
                return false;
            }
        }
        boolean hurt = super.m_6469_(source, amount);
        if (hurt && source.m_7639_() instanceof LivingEntity attacker && attacker != this && this.foeOrRevenge(attacker)) {
            this.foe = attacker;
        }
        return hurt;
    }

    /** Patrols go when no one is near (as monsters do); one named by hand or spawned by command stays. */
    @Override
    public boolean m_6785_(double distanceSqr) {
        return !this.getPersistentData().m_128471_("HexPieceStays");
    }
}
