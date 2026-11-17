package com.hexhaki.piece.npc;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.PieceItems;
import com.hexhaki.piece.PieceSounds;
import com.hexhaki.piece.armor.PieceArmor;
import com.hexhaki.piece.server.ProfileServer;
import com.hexhaki.piece.server.StyleCombat;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.PieceConfig;
import com.hexhaki.piece.server.HakiSlashes;
import com.hexhaki.piece.server.SwordServer;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.sword.SwordCombo;
import com.hexhaki.piece.sword.SwordMove;
import com.hexhaki.piece.sword.SwordRules;
import com.hexhaki.piece.sword.SwordSounds;
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
import net.minecraft.world.entity.EquipmentSlot;
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
    /** 2.1.4: a swordsman's katana (ordinal + 1; 0 none), what its sword is doing (SwordAnims) and since when (mod 2^30). */
    static final EntityDataAccessor<Integer> KATANA = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> SWORD = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> SWORD_START = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    /** 2.3.3: what it is doing with its body while idle ({@link Gesture} ordinal) and since when (game ticks mod 2^30). */
    static final EntityDataAccessor<Integer> GESTURE = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final EntityDataAccessor<Integer> GESTURE_START = SynchedEntityData.m_135353_(SeaFighter.class, EntityDataSerializers.f_135028_);
    static final String SAVED = "HexPieceFighter";

    private boolean ready;
    private StyleMove move;
    private long moveStart;
    private boolean counterUsed;
    private Slot nextString = Slot.M1_1;
    private long nextAttack;
    private long nextWander;
    private long nextGesture = -1;
    private long gestureStart;
    private int retarget;
    private LivingEntity foe;
    private final Map<StyleMove, Long> cooldowns = new EnumMap<>(StyleMove.class);
    private final Set<Integer> struck = new HashSet<>();
    // 2.1.4: the swordsman's sword (the same strings, cut windows and swept blade as a player's: SwordCombo, SwordServer.sweep)
    private SwordMove swordMove;
    private long swordStart;
    private SwordMove swordLast;
    private long swordLastEnd = -100000;
    private boolean drawn;
    private long sheathingUntil = -1;
    private long idleSince;
    private long chargeStart = -1;
    private int chargeFor;
    private boolean chargeDrew;
    private long releaseStart = -1;
    private double releaseCharge;
    private long slashReady;
    private final java.util.List<Set<Integer>> swordStruck = new java.util.ArrayList<>();
    /** 2.2.9: what has been heard of what its sword is doing (the same beats as a player's: SwordSounds). */
    private final SwordSounds.Cursor swordHeard = new SwordSounds.Cursor();
    private final Set<Integer> releaseStruck = new HashSet<>();

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
        this.f_19804_.m_135372_(KATANA, 0);
        this.f_19804_.m_135372_(SWORD, 0);
        this.f_19804_.m_135372_(SWORD_START, 0);
        this.f_19804_.m_135372_(GESTURE, 0);
        this.f_19804_.m_135372_(GESTURE_START, 0);
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

    /** 2.1.4: its katana (null: none). */
    public Katana katana() {
        int k = this.f_19804_.m_135370_(KATANA);
        return k <= 0 ? null : Katana.byOrdinal(k - 1);
    }

    /** 2.1.4: what its sword is doing (SwordAnims) and since when (game ticks mod 2^30). */
    public int shownSword() {
        return this.f_19804_.m_135370_(SWORD);
    }

    public int shownSwordStart() {
        return this.f_19804_.m_135370_(SWORD_START);
    }

    /** 2.3.3: its idle gesture as every client sees it (NONE: none), and since when (game ticks mod 2^30). */
    public Gesture gesture() {
        return Gesture.byOrdinal(this.f_19804_.m_135370_(GESTURE));
    }

    public int gestureStart() {
        return this.f_19804_.m_135370_(GESTURE_START);
    }

    void setGesture(Gesture gesture, long now) {
        this.f_19804_.m_135381_(GESTURE, gesture.ordinal());
        this.f_19804_.m_135381_(GESTURE_START, (int) (now & 0x3FFFFFFF));
        this.gestureStart = now;
    }

    /**
     * 2.3.3 (the owner: "the NPCS ... can have the same ambient animations as HexGhoul"): with no foe, now and then, standing still, a
     * gesture of its side's ({@link Gesture#pick}); it stands for it (no wandering off mid-gesture) and is gone the moment a foe is
     * found. Otherwise it wanders as before. True if it is standing for a gesture.
     */
    boolean idle(long now) {
        if (this.nextGesture < 0) {
            this.nextGesture = now + Gesture.pause(this.m_217043_().m_188500_());
        }
        Gesture shown = this.gesture();
        if (shown != Gesture.NONE) {
            if (!shown.over(now - this.gestureStart)) {
                return true;
            }
            this.setGesture(Gesture.NONE, now);
            this.nextGesture = now + Gesture.pause(this.m_217043_().m_188500_());
        }
        if (now >= this.nextGesture && this.m_21573_().m_26571_() && this.shownSword() == com.hexhaki.piece.sword.SwordAnims.NONE
            && this.m_20096_() && !this.m_20069_()) {
            this.setGesture(Gesture.pick(this.faction(), this.m_217043_().m_188500_()), now);
            this.m_21573_().m_26573_();
            return true;
        }
        return false;
    }

    /** 2.3.3: any gesture dropped at once (a foe found, a move begun). */
    void endGesture(long now) {
        if (this.gesture() != Gesture.NONE) {
            this.setGesture(Gesture.NONE, now);
            this.nextGesture = now + Gesture.pause(this.m_217043_().m_188500_());
        }
    }

    /** Made what it is: its look (so its rank), its style; its health and speed to its rank; named; kept in its saved data. */
    public void setup(int look, FightingStyle style) {
        this.setup(look, style, style.sword() ? SeaRules.katana(SeaRules.rankOf(look), this.m_217043_().m_188503_(64)) : null);
    }

    public void setup(int look, FightingStyle style, Katana katana) {
        int rank = SeaRules.rankOf(look);
        this.f_19804_.m_135381_(LOOK, look);
        this.f_19804_.m_135381_(STYLE, style.ordinal());
        this.f_19804_.m_135381_(KATANA, katana == null ? 0 : katana.ordinal() + 1);
        this.m_21051_(Attributes.f_22276_).m_22100_(SeaRules.HEALTH[rank]);
        this.m_21051_(Attributes.f_22279_).m_22100_(SeaRules.SPEED[rank]);
        this.m_21153_(this.m_21233_());
        this.m_6593_(Component.m_237113_(SeaRules.title(this.faction(), rank)));
        CompoundTag saved = new CompoundTag();
        saved.m_128405_("Look", look);
        saved.m_128405_("Style", style.ordinal());
        saved.m_128405_("Katana", katana == null ? -1 : katana.ordinal());
        this.getPersistentData().m_128365_(SAVED, saved);
        // 2.3.1 (the owner: "NPCS for now only spawn with the Marine Armor"): a Marine in the Marine outfit, head to feet (not again
        // after a load: it keeps what it wears)
        if (this.faction() == Faction.MARINE && this.m_6844_(EquipmentSlot.CHEST).m_41619_()) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            PieceArmor[] outfit = PieceArmor.marineOutfit();
            for (int i = 0; i < slots.length; i++) {
                this.m_8061_(slots[i], PieceItems.stack(outfit[i]));
            }
        }
        this.ready = true;
    }

    /** After a load (or a first tick with nothing set): what it was, from its saved data, or a grunt of a style of its side. */
    void restore() {
        CompoundTag data = this.getPersistentData();
        if (data.m_128441_(SAVED)) {
            CompoundTag saved = data.m_128469_(SAVED);
            FightingStyle style = FightingStyle.byOrdinal(saved.m_128451_("Style"));
            Katana katana = saved.m_128441_("Katana") ? Katana.byOrdinal(saved.m_128451_("Katana")) : null;
            if (style.sword() && katana == null) {
                katana = SeaRules.katana(SeaRules.rankOf(saved.m_128451_("Look")), this.m_217043_().m_188503_(64));
            }
            this.setup(saved.m_128451_("Look"), style, katana);
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
        if (this.foe != null || this.move != null) {
            this.endGesture(now);
        }
        if (this.style().sword()) {
            this.swordBrain(level, now);
            return;
        }
        if (this.move != null) {
            this.tickMove(level, now);
            return;
        }
        if (this.foe == null) {
            if (this.idle(now)) {
                return;
            }
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

    // --- 2.1.4: the swordsman ---------------------------------------------------------------------------------------------------

    int swordAnim() {
        if (this.swordMove != null) {
            return SwordAnims.of(this.swordMove);
        }
        if (this.releaseStart >= 0) {
            return SwordAnims.RELEASE;
        }
        if (this.chargeStart >= 0) {
            return this.chargeDrew ? SwordAnims.CHARGE_DRAW : SwordAnims.CHARGE;
        }
        if (this.sheathingUntil >= 0) {
            return SwordAnims.SHEATHE;
        }
        return this.drawn ? SwordAnims.GUARD : SwordAnims.NONE;
    }

    long swordAnimStart() {
        if (this.swordMove != null) {
            return this.swordStart;
        }
        if (this.releaseStart >= 0) {
            return this.releaseStart;
        }
        if (this.chargeStart >= 0) {
            return this.chargeStart;
        }
        if (this.sheathingUntil >= 0) {
            return this.sheathingUntil - SwordRules.SHEATHE_TICKS;
        }
        return this.idleSince;
    }

    void showSword() {
        int anim = this.swordAnim();
        int start = (int) (this.swordAnimStart() & 0x3FFFFFFF);
        if (this.f_19804_.m_135370_(SWORD) != anim) {
            this.f_19804_.m_135381_(SWORD, anim);
        }
        if (this.f_19804_.m_135370_(SWORD_START) != start) {
            this.f_19804_.m_135381_(SWORD_START, start);
        }
    }

    /**
     * The swordsman's fight: the same strings as a player's ({@link SwordCombo}: drawn from the saya into its draw, then the ground
     * string; on the move the moving one), the same cut windows and the blade's swept path ({@code SwordServer.sweep}), the same
     * motions. Officers and captains now and then charge the slash; a captain's flies as a Haki slash (its terrain cut only while the
     * mobGriefing rule allows it). With no foe left the sword goes back in its saya.
     */
    void swordBrain(ServerLevel level, long now) {
        try {
            this.swordStep(level, now);
        } finally {
            this.showSword();
            for (SwordSounds.Beat beat : this.swordHeard.advance(this.swordAnim(), this.swordAnimStart(), now, this.katana())) {
                PieceSounds.play(this, beat);
            }
        }
    }

    void swordStep(ServerLevel level, long now) {
        Katana katana = this.katana();
        float edge = katana == null ? 1.0F : katana.edge();
        int mastery = SeaRules.MASTERY[this.rank()];
        DamageSource source = this.m_269291_().m_269333_(this);
        if (this.swordMove != null) {
            int tick = (int) (now - this.swordStart);
            if (tick >= this.swordMove.duration() || !this.m_6084_()) {
                this.swordLast = this.swordMove;
                this.swordLastEnd = now;
                this.swordMove = null;
                this.idleSince = now;
            } else {
                if (this.foe != null) {
                    this.face(this.foe);
                }
                StyleCombat.carry(this, this.swordMove.motion(), tick);
                SwordMove.Cut[] cuts = this.swordMove.cuts();
                for (int i = 0; i < cuts.length; i++) {
                    SwordMove.Cut cut = cuts[i];
                    if (tick >= cut.from() && tick <= cut.to()) {
                        SwordServer.sweep(this, this.swordMove.animation(), tick, SwordRules.EXTEND, this.swordStruck.get(i),
                            cut.damage() * edge * PieceRules.masteryDamage(mastery), cut.weight(), cut.back(), cut.up(), cut.stun(), this::foe,
                            source);
                    }
                }
                return;
            }
        }
        if (this.releaseStart >= 0) {
            int tick = (int) (now - this.releaseStart);
            if (tick >= 15) {
                this.releaseStart = -1;
                this.idleSince = now;
            } else {
                StyleCombat.carry(this, com.hexhaki.piece.style.Motion.lunge(1, 0.45), tick);
                if (this.rank() >= 2 && tick == 2) {
                    Vec3 aim = this.foe == null ? StyleCombat.facing(this) : this.foe.m_146892_().m_82546_(this.m_146892_()).m_82541_();
                    double c = this.releaseCharge;
                    HakiSlashes.launch(this, this.m_146892_().m_82549_(aim.m_82490_(0.6)), aim,
                        SwordRules.lerp(SwordRules.SLASH_HEIGHT_MIN, SwordRules.NPC_SLASH_HEIGHT_MAX, c),
                        SwordRules.lerp(SwordRules.SLASH_RANGE_MIN, SwordRules.NPC_SLASH_RANGE_MAX, c), SwordRules.SLASH_SPEED,
                        (float) (SwordRules.lerp(SwordRules.SLASH_DAMAGE_MIN, SwordRules.SLASH_DAMAGE_MAX, c) * edge * 0.75), 0, false,
                        PieceConfig.get(PieceConfig.TERRAIN, true), source, this::foe);
                    PieceSounds.play(this, SwordSounds.SLASH, (float) (0.8 + 0.5 * c), SwordSounds.pitch(katana));
                } else if (this.rank() < 2 && tick >= 1 && tick <= 3) {
                    float damage = (float) SwordRules.lerp(SwordRules.CUT_DAMAGE_MIN, SwordRules.CUT_DAMAGE_MAX, this.releaseCharge) * edge * 0.8F;
                    SwordServer.sweep(this, "sword_release", tick, SwordRules.lerp(SwordRules.CUT_REACH_MIN, SwordRules.CUT_REACH_MAX,
                        this.releaseCharge), this.releaseStruck, damage, Hit.Weight.FINISHER, 1.1, 0.25, 16, this::foe, source);
                }
                return;
            }
        }
        if (this.chargeStart >= 0) {
            if (this.foe != null) {
                this.face(this.foe);
                this.m_21573_().m_26573_();
            }
            if (now - this.chargeStart >= this.chargeFor) {
                this.releaseCharge = SwordRules.charge(now - this.chargeStart, SwordRules.CHARGE_MAX);
                this.chargeStart = -1;
                this.releaseStart = now;
                this.releaseStruck.clear();
            }
            return;
        }
        if (this.sheathingUntil >= 0 && now >= this.sheathingUntil) {
            this.sheathingUntil = -1;
            this.drawn = false;
        }
        if (this.foe == null) {
            if (this.drawn && this.sheathingUntil < 0 && now - this.idleSince >= 30) {
                this.sheathingUntil = now + SwordRules.SHEATHE_TICKS;
            }
            if (!this.drawn && this.idle(now)) {
                return;
            }
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
        boolean charge = this.rank() >= 1 && now >= this.slashReady && distance < 9.0 && this.m_217043_().m_188501_() < 0.04F;
        if (charge) {
            this.chargeDrew = !this.drawn || this.sheathingUntil >= 0;
            this.drawn = true;
            this.sheathingUntil = -1;
            this.chargeStart = now;
            this.chargeFor = 14 + this.m_217043_().m_188503_(18);
            this.slashReady = now + 140;
            return;
        }
        if (distance > SeaRules.STRIKE + 0.4) {
            if (now % 5 == 0) {
                this.m_21573_().m_5624_(this.foe, this.rank() == 0 ? 1.15 : 1.25);
            }
            return;
        }
        this.m_21573_().m_26573_();
        this.face(this.foe);
        if (now >= this.nextAttack) {
            boolean sheathed = !this.drawn || this.sheathingUntil >= 0;
            Vec3 v = this.m_20184_();
            SwordMove.Branch branch = SwordCombo.branch(this.m_20096_(), Math.sqrt(v.f_82479_ * v.f_82479_ + v.f_82481_ * v.f_82481_));
            SwordMove move = SwordCombo.next(branch, sheathed, this.swordLast, this.swordLastEnd, now);
            this.swordMove = move;
            this.swordStart = now;
            this.drawn = true;
            this.sheathingUntil = -1;
            this.swordStruck.clear();
            for (int i = 0; i < move.cuts().length; i++) {
                this.swordStruck.add(new HashSet<>());
            }
            int pause = this.rank() == 2 ? 1 : this.rank() == 1 ? 3 : 6;
            this.nextAttack = now + (move.finisher() ? move.duration() + 8 + this.m_217043_().m_188503_(10)
                : move.cancel() + this.m_217043_().m_188503_(pause + 1));
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
