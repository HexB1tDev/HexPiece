package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.piece.itsumade.ItsuArt;
import com.hexhaki.piece.network.C2SGate;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CGates;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.9.9: Hellish Teleport (the owner's spec: "N - Hellish Teleport: a black/scary menu appears on the user's screen, 3 Buttons on the
 * menu (also custom made and cool looking) each button acts as a location save point clicking 1 of those buttons will save the current
 * player location then you gotta figure out a way to clear a button and reset the location to another spot. rename a teleport area,
 * etc. When clicking a location a petagram 6x6, will start forming on the floor that adapts to terrain, after it fully forms (takes
 * around 10 seconds), anything or anyone standing on it is teleported to the saved location. Teleport style: the people and mobs/
 * entities, all turn into black flames and all of them come together at one point and blast into the sky from the center of the
 * pentagram. On Arrival, the beam falls from the sky onto another landing pentagram on the saved location (the 2 pentagrams form at the
 * same time, landing and entry ...)").
 *
 * <p>The three gates live in the player's persisted data (kept through death); N opens the menu (client/itsumade/GateScreen), its clicks
 * come back as C2SGate ({@link #act}). Opening one: both pentagrams form at once over {@link ItsuArt#GATE_FORM} ticks (every client near
 * either end draws its own, client/itsumade/ItsuGateFx, S2CUoFx kinds 460 to 464); then whatever stands on the near one is swallowed by
 * black flames drawn to its middle, a pillar of them blasts up into the sky, a beam of them falls out of the sky onto the far one, and
 * everything taken stands there where it stood on the near one.
 */
public final class ItsuGates {
    /** S2CUoFx kinds (client/itsumade/ItsuGateFx). */
    public static final int FORM = 460;
    public static final int ENGULF = 461;
    public static final int PILLAR = 462;
    public static final int ARRIVE = 463;
    public static final int END = 464;

    /** The black flames take what stands on it (ticks), and how long the pillars burn. */
    public static final int SWALLOW = 8;
    public static final int PILLAR_LIFE = 44;
    /**
     * 4.0.2 (the owner: "you should be able to see yourself going into the beam of fire into the sky ... And then falling down from the
     * sky ... Into the other circle ... But no, it's instant"): how long what was taken is carried up the pillar into the sky (ticks), and
     * how long it takes to come down the other beam onto the far circle; how much of the climb it takes to be drawn into the middle.
     */
    public static final int RISE = 50;
    /** 4.0.3 (the owner: "it falls down slowly onto the circle"): six seconds down the far beam. */
    public static final int FALL = 120;
    static final double GATHER = 0.3;
    /** How high the pillars reach (blocks over the pentagram). */
    public static final double SKY = 90.0;
    /** How far from either end a client is told of it (blocks). */
    static final double WATCH = 128.0;
    static final int COOL = 1200;
    static final String TAG = "ItsuGates";
    static final String[] NUMERALS = {"I", "II", "III"};
    static final int NAME_MOST = 20;

    record Gate(boolean set, String name, int x, int y, int z, String world) {
        static final Gate EMPTY = new Gate(false, "", 0, 0, 0, "");
    }

    private static final Map<UUID, Ritual> RITUALS = new HashMap<>();

    private ItsuGates() {
    }

    // ------------------------------------------------------------------------------------------------------ the gates kept

    static Gate[] gates(ServerPlayer player) {
        CompoundTag root = ProfileServer.root(player);
        CompoundTag all = root.m_128441_(TAG) ? root.m_128469_(TAG) : new CompoundTag();
        Gate[] out = new Gate[S2CGates.SLOTS];
        for (int i = 0; i < out.length; i++) {
            String key = "G" + i;
            if (!all.m_128441_(key)) {
                out[i] = Gate.EMPTY;
                continue;
            }
            CompoundTag g = all.m_128469_(key);
            out[i] = new Gate(g.m_128471_("Set"), g.m_128461_("Name"), g.m_128451_("X"), g.m_128451_("Y"), g.m_128451_("Z"), g.m_128461_("World"));
        }
        return out;
    }

    static void keep(ServerPlayer player, Gate[] gates) {
        CompoundTag all = new CompoundTag();
        for (int i = 0; i < gates.length; i++) {
            CompoundTag g = new CompoundTag();
            g.m_128379_("Set", gates[i].set());
            g.m_128359_("Name", gates[i].name());
            g.m_128405_("X", gates[i].x());
            g.m_128405_("Y", gates[i].y());
            g.m_128405_("Z", gates[i].z());
            g.m_128359_("World", gates[i].world());
            all.m_128365_("G" + i, g);
        }
        ProfileServer.root(player).m_128365_(TAG, all);
    }

    static String world(ServerPlayer player) {
        return player.m_9236_().m_46472_().m_135782_().toString();
    }

    static String fallbackName(int slot) {
        return "Gate " + NUMERALS[slot];
    }

    /** The gates told to the player's own client ({@code open}: the menu opened). */
    static void tell(ServerPlayer player, boolean open) {
        Gate[] gates = gates(player);
        boolean[] set = new boolean[gates.length];
        String[] names = new String[gates.length];
        int[] xs = new int[gates.length];
        int[] ys = new int[gates.length];
        int[] zs = new int[gates.length];
        String[] worlds = new String[gates.length];
        for (int i = 0; i < gates.length; i++) {
            set[i] = gates[i].set();
            names[i] = gates[i].set() ? gates[i].name() : "";
            xs[i] = gates[i].x();
            ys[i] = gates[i].y();
            zs[i] = gates[i].z();
            worlds[i] = gates[i].world();
        }
        int left = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, FruitData.cooldownLeft(player, ItsuKit.TELEPORT.cooldown())));
        PieceNetwork.sendTo(player, new S2CGates(open, set, names, xs, ys, zs, worlds, world(player), left));
    }

    /** N (as a human): the menu. */
    public static void menu(ServerPlayer player) {
        tell(player, true);
        ItoMoves.sound(player, player.m_20182_(), ItsuServer.GATE_MENU, 0.8F, 1.0F);
    }

    /** A click in the menu (C2SGate). */
    public static void act(ServerPlayer player, int action, int slot, String text) {
        if (FruitData.fruit(player) != Fruit.ITSU || slot < 0 || slot >= S2CGates.SLOTS) {
            return;
        }
        Gate[] gates = gates(player);
        Gate g = gates[slot];
        switch (action) {
            case C2SGate.BIND -> {
                BlockPos at = new BlockPos((int) Math.floor(player.m_20185_()), (int) Math.floor(player.m_20186_() + 0.01),
                    (int) Math.floor(player.m_20189_()));
                gates[slot] = new Gate(true, g.set() ? g.name() : fallbackName(slot), at.m_123341_(), at.m_123342_(), at.m_123343_(),
                    world(player));
                keep(player, gates);
                player.m_5661_(Component.m_237113_(gates[slot].name() + " is bound here."), true);
                ItoMoves.sound(player, player.m_20182_(), ItsuServer.GATE_BIND, 0.9F, 1.0F);
            }
            case C2SGate.CLEAR -> {
                gates[slot] = Gate.EMPTY;
                keep(player, gates);
                ItoMoves.sound(player, player.m_20182_(), ItsuServer.GATE_CLEAR, 0.8F, 1.0F);
            }
            case C2SGate.RENAME -> {
                if (!g.set()) {
                    return;
                }
                String name = clean(text);
                gates[slot] = new Gate(true, name.isEmpty() ? fallbackName(slot) : name, g.x(), g.y(), g.z(), g.world());
                keep(player, gates);
            }
            case C2SGate.GO -> {
                go(player, slot, g);
                return;
            }
            default -> {
                return;
            }
        }
        tell(player, false);
    }

    /** A name as the player typed it, kept to what a sign would show: no formatting codes, no control characters, at most 20 long. */
    static String clean(String text) {
        StringBuilder out = new StringBuilder();
        for (char c : (text == null ? "" : text).toCharArray()) {
            if (c >= 32 && c != 127 && c != '§' && out.length() < NAME_MOST) {
                out.append(c);
            }
        }
        return out.toString().trim();
    }

    // ------------------------------------------------------------------------------------------------------ opening a gate

    static void go(ServerPlayer player, int slot, Gate g) {
        if (!g.set() || RITUALS.containsKey(player.m_20148_()) || ItsuServer.phase(player) != com.hexhaki.piece.itsumade.ItsuForm.HUMAN) {
            return;
        }
        if (!g.world().equals(world(player))) {
            player.m_5661_(Component.m_237113_(g.name() + " lies in another world."), true);
            return;
        }
        if (!ItsuSaint.ready(player, ItsuKit.TELEPORT)) {
            long left = FruitData.cooldownLeft(player, ItsuKit.TELEPORT.cooldown());
            player.m_5661_(Component.m_237113_("The gates are still closed (" + Math.max(1, (left + 19) / 20) + " s)."), true);
            return;
        }
        Vec3 from = new Vec3(Math.floor(player.m_20185_()) + 0.5, player.m_20186_(), Math.floor(player.m_20189_()) + 0.5);
        Vec3 to = new Vec3(g.x() + 0.5, g.y(), g.z() + 0.5);
        if (from.m_82554_(to) < ItsuArt.GATE_RADIUS * 2.0 + 2.0) {
            player.m_5661_(Component.m_237113_("You already stand at " + g.name() + "."), true);
            return;
        }
        ItsuSaint.cool(player, ItsuKit.TELEPORT, COOL);
        Ritual ritual = new Ritual(player, (ServerLevel) player.m_9236_(), from, to);
        RITUALS.put(player.m_20148_(), ritual);
        UoMoves.live(ritual);
        ItoMoves.animate(player, "itsu_gate");
        ritual.both(FORM, 0, 0, 0, Vec3.f_82478_, 0, 0, 0); // ticks: how far along it already is
        ItoMoves.sound(player, from, ItsuServer.GATE_FORM, 1.6F, 1.0F);
        ritual.soundAt(to, ItsuServer.GATE_FORM, 1.6F);
    }

    /** One gate opening: both pentagrams forming, then what stands on the near one taken through to the far one. */
    static final class Ritual implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final Vec3 from;
        final Vec3 to;
        int age;
        final List<Entity> taken = new ArrayList<>();
        final List<Vec3> offsets = new ArrayList<>();

        Ritual(ServerPlayer user, ServerLevel level, Vec3 from, Vec3 to) {
            this.user = user;
            this.level = level;
            this.from = from;
            this.to = to;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        /** Every client near the far end (and the user's own, wherever it is). */
        List<ServerPlayer> farWatchers() {
            List<ServerPlayer> out = new ArrayList<>();
            for (ServerPlayer p : this.level.m_6907_()) {
                if (p == this.user || p.m_20182_().m_82554_(this.to) <= WATCH) {
                    out.add(p);
                }
            }
            if (!out.contains(this.user)) {
                out.add(this.user);
            }
            return out;
        }

        S2CUoFx message(int kind, int id, Vec3 at, Vec3 a, double bx, double by, int ticks) {
            return new S2CUoFx(kind, id, this.user.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_, (float) a.f_82480_,
                (float) a.f_82481_, (float) bx, (float) by, 0, (float) ItsuArt.GATE_RADIUS, ticks);
        }

        void near(S2CUoFx message) {
            PieceNetwork.around(this.user, message);
        }

        void far(S2CUoFx message) {
            for (ServerPlayer p : this.farWatchers()) {
                PieceNetwork.sendTo(p, message);
            }
        }

        /** The same kind of message at both ends (id 0 the near one, 1 the far one). */
        void both(int kind, int unused, double bx, double by, Vec3 a, double x, double y, int ticks) {
            this.near(this.message(kind, 0, this.from, a, bx, by, ticks));
            this.far(this.message(kind, 1, this.to, a, bx, by, ticks));
        }

        void soundAt(Vec3 at, String name, float volume) {
            com.hexhaki.piece.PieceSounds.playAt(this.user, at.f_82479_, at.f_82480_, at.f_82481_, name, volume, 1.0F);
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || this.age > 100000) {
                this.both(END, 0, 0, 0, Vec3.f_82478_, 0, 0, 0);
                this.release();
                RITUALS.remove(this.user.m_20148_());
                return true;
            }
            int form = ItsuArt.GATE_FORM;
            if (this.age < form && this.age % 50 == 25) {
                ItoMoves.sound(this.user, this.from, ItsuServer.GATE_HUM, 1.2F, 0.8F + this.age / 400.0F);
                this.soundAt(this.to, ItsuServer.GATE_HUM, 1.2F);
            }
            int up = form + SWALLOW;
            int top = up + RISE;
            int down = top + FALL;
            if (this.age == form) {
                this.swallow();
            } else if (this.age > up && this.age <= top) {
                this.carry((this.age - up) / (double) RISE, false);
                if (this.age == top - SWALLOW) {
                    // the beam starts falling out of the sky onto the far circle as they reach the top of this one
                    this.far(this.message(PILLAR, 1, this.to, Vec3.f_82478_, SKY, 0, FALL + SWALLOW + 24));
                }
                if (this.age == top) {
                    this.through();
                }
            } else if (this.age > top && this.age <= down) {
                this.carry((this.age - top) / (double) FALL, true);
                if (this.age == down) {
                    this.land();
                }
            } else if (this.age >= down + 40) {
                this.both(END, 0, 0, 0, Vec3.f_82478_, 0, 0, 0);
                RITUALS.remove(this.user.m_20148_());
                return true;
            }
            return false;
        }

        /**
         * Where what was taken is {@code t} (0 to 1) of the way through the climb up the near pillar ({@code falling} false: from where
         * it stood, drawn into the middle over the first GATHER of it, up to SKY over the circle) or down the far beam ({@code falling}:
         * from SKY over the far circle down onto it, spread back out where it stood over the last GATHER).
         */
        Vec3 path(Vec3 off, double t, boolean falling) {
            double ease = t * t * (3.0 - 2.0 * t);
            if (!falling) {
                double in = Math.min(1.0, t / GATHER);
                double keep = 1.0 - in * in * (3.0 - 2.0 * in);
                return this.from.m_82520_(off.f_82479_ * keep, Math.max(0.0, off.f_82480_) + SKY * ease, off.f_82481_ * keep);
            }
            double out = Math.max(0.0, (t - (1.0 - GATHER)) / GATHER);
            double spread = out * out * (3.0 - 2.0 * out);
            return this.to.m_82520_(off.f_82479_ * spread, Math.max(0.0, off.f_82480_) + 0.05 + SKY * (1.0 - ease), off.f_82481_ * spread);
        }

        /** Every taken body moved along its path this tick: by its motion (smooth on its client), put back on it if it strayed. */
        void carry(double t, boolean falling) {
            double next = Math.min(1.0, t + 1.0 / (falling ? FALL : RISE));
            for (int i = 0; i < this.taken.size(); i++) {
                Entity e = this.taken.get(i);
                if (!e.m_6084_() || e.m_9236_() != this.level) {
                    continue;
                }
                Vec3 off = this.offsets.get(i);
                Vec3 now = this.path(off, t, falling);
                Vec3 then = this.path(off, next, falling);
                if (e.m_20182_().m_82554_(now) > 3.0) {
                    e.m_6021_(now.f_82479_, now.f_82480_, now.f_82481_);
                }
                e.m_20242_(true);
                e.m_20334_(then.f_82479_ - e.m_20185_(), then.f_82480_ - e.m_20186_(), then.f_82481_ - e.m_20189_());
                e.f_19789_ = 0.0F;
                e.f_19864_ = true;
            }
        }

        /** It has formed: what stands on it swallowed by black flames, drawn to its middle; the pillar up, the beam down. */
        void swallow() {
            double reach = ItsuArt.GATE_RADIUS + 0.4;
            for (Entity e : this.level.m_6443_(Entity.class, UoMoves.box(this.from.m_82520_(-reach, -1.5, -reach),
                this.from.m_82520_(reach, 2.5, reach)), e -> e.m_6084_() && !e.m_5833_() && !e.m_20159_())) {
                Vec3 at = e.m_20182_();
                double dx = at.f_82479_ - this.from.f_82479_;
                double dz = at.f_82481_ - this.from.f_82481_;
                if (dx * dx + dz * dz > reach * reach) {
                    continue;
                }
                this.taken.add(e);
                this.offsets.add(at.m_82546_(this.from));
                // the black flames over it the whole climb (its client draws them on the body as it goes)
                this.near(this.message(ENGULF, e.m_19879_(), at, this.from, e.m_20206_(), 0, SWALLOW + RISE));
                e.m_20334_(0.0, 0.0, 0.0);
            }
            this.near(this.message(PILLAR, 0, this.from, Vec3.f_82478_, SKY, 0, SWALLOW + RISE + 16));
            // 4.0.0 (POINT ONE): the Conqueror's lightning out of the blast up, and (through) out of the landing
            ConquerorAura.mixed(this.user, this.from.m_82520_(0.0, 1.0, 0.0), 3.5, 8, -1.0);
            ItoMoves.sound(this.user, this.from, ItsuServer.GATE_GO, 2.5F, 1.0F);
            this.soundAt(this.to, ItsuServer.GATE_GO, 2.5F);
        }

        /** At the top of the near pillar: everything taken is put at the top of the far beam, in its black flames, to come down it. */
        void through() {
            // the far end loaded (made in full) before anything is put there
            this.level.m_6325_((int) Math.floor(this.to.f_82479_) >> 4, (int) Math.floor(this.to.f_82481_) >> 4);
            for (int i = 0; i < this.taken.size(); i++) {
                Entity e = this.taken.get(i);
                if (!e.m_6084_() || e.m_9236_() != this.level) {
                    continue;
                }
                Vec3 at = this.path(this.offsets.get(i), 0.0, true);
                e.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
                e.m_20334_(0.0, 0.0, 0.0);
                e.f_19789_ = 0.0F;
                e.f_19864_ = true;
                if (e instanceof ServerPlayer p && p != this.user) {
                    // a player taken along sees the far end too (the user is told of it from the start)
                    PieceNetwork.sendTo(p, this.message(FORM, 1, this.to, Vec3.f_82478_, 0, 0, ItsuArt.GATE_FORM));
                    PieceNetwork.sendTo(p, this.message(PILLAR, 1, this.to, Vec3.f_82478_, SKY, 0, FALL + 24));
                }
                this.far(this.message(ENGULF, e.m_19879_(), at, this.to, e.m_20206_(), 1, FALL));
            }
            ItoMoves.sound(this.user, this.to.m_82520_(0.0, SKY * 0.5, 0.0), ItsuServer.GATE_GO, 2.5F, 1.0F);
        }

        /** Down on the far circle: gravity given back, the black flames peeling off, the landing heard. */
        void land() {
            for (int i = 0; i < this.taken.size(); i++) {
                Entity e = this.taken.get(i);
                if (!e.m_6084_()) {
                    continue;
                }
                e.m_20242_(false);
                e.m_20334_(0.0, 0.0, 0.0);
                e.f_19789_ = 0.0F;
                e.f_19864_ = true;
                this.far(this.message(ARRIVE, e.m_19879_(), e.m_20182_(), Vec3.f_82478_, e.m_20206_(), 0, 14));
            }
            this.soundAt(this.to, ItsuServer.GATE_LAND, 2.5F);
            ConquerorAura.mixed(this.user, this.to.m_82520_(0.0, 1.0, 0.0), 3.5, 8, -1.0);
        }

        /** Gravity given back to whatever is still being carried (the ritual cut short). */
        void release() {
            for (Entity e : this.taken) {
                if (e.m_6084_()) {
                    e.m_20242_(false);
                }
            }
        }
    }

    public static void forget(ServerPlayer player) {
        Ritual r = RITUALS.remove(player.m_20148_());
        if (r != null) {
            r.both(END, 0, 0, 0, Vec3.f_82478_, 0, 0, 0);
            r.age = Integer.MAX_VALUE / 2; // it ends on its next tick
        }
    }
}
