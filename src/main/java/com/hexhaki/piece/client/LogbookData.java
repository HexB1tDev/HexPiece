package com.hexhaki.piece.client;

import com.hexhaki.binds.Binds;
import com.hexhaki.binds.client.BindsClient;
import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeyLabels;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.screen.HakiMasteryScreen;
import com.hexhaki.data.HakiRank;
import com.hexhaki.data.HakiTechnique;
import com.hexhaki.data.HakiType;
import com.hexhaki.devilfruit.data.GearAbility;
import com.hexhaki.devilfruit.data.GearMastery;
import com.hexhaki.devilfruit.data.GearUpgrade;
import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.ui.LogbookView;
import com.hexhaki.piece.ui.LogbookView.Entry;
import com.hexhaki.piece.ui.LogbookView.Section;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * 2.2.4: what the one menu ({@link LogbookScreen}, {@link LogbookView}) shows, read from the original's client state the way its
 * Ability Book, Haki Techniques and Gear mastery screens read it: each Haki path's mastery and techniques ({@code HakiMasteryScreen}'s
 * public helpers), the quick slots ({@code BindsClient}), a kit fruit's moves ({@code ClientKits}), the Nika fruit's techniques and
 * upgrades ({@code GearClientState}).
 */
final class LogbookData {
    static final HakiType[] TYPES = {HakiType.ARMAMENT, HakiType.OBSERVATION, HakiType.CONQUEROR};
    static final int[] COLOURS = {0xFF1D3F66, 0xFF2C6BB5, 0xFF8A2030};
    static final String[] NAMES = {"Armament Haki", "Observation Haki", "Conqueror's Haki"};
    static final String NIKA = "Hito Hito no Mi, Model Nika";

    private LogbookData() {
    }

    static String pretty(String constant) {
        String lower = constant.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return lower.isEmpty() ? lower : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    static LogbookView.Data build() {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        PieceProfile profile = PieceClient.self();
        int[] haki = new int[3];
        String[] ranks = new String[3];
        for (int i = 0; i < 3; i++) {
            haki[i] = Math.max(0, Math.min(1000, HakiMasteryScreen.mastery(TYPES[i])));
            ranks[i] = pretty(HakiRank.of(haki[i]).name());
        }
        String fruit = "";
        int fruitMastery = 0;
        Fruit kit = FruitClient.fruit();
        boolean kitFruit = FruitClient.owned() && kit != null && kit != Fruit.NONE;
        if (kitFruit) {
            fruit = kit.display;
            fruitMastery = FruitClient.mastery();
        } else if (GearClientState.hasFruit) {
            fruit = NIKA;
            fruitMastery = GearClientState.mastery;
        }
        List<Section> styleHaki = new ArrayList<>();
        styleHaki.add(LogbookView.style(profile));
        for (int i = 0; i < 3; i++) {
            styleHaki.add(haki(i, haki[i]));
        }
        List<Section> fruitPage = new ArrayList<>();
        int points = -1;
        if (kitFruit) {
            fruitPage.add(kit(kit, fruitMastery));
        } else if (GearClientState.hasFruit) {
            fruitPage.add(nika());
            fruitPage.add(upgrades());
            points = GearClientState.freePoints;
        }
        String[] slotKeys = new String[9];
        String[] slotNames = new String[9];
        int[] slotAbilities = new int[9];
        for (int slot = 0; slot < 9; slot++) {
            slotKeys[slot] = slotKey(slot);
            int ability = BindsClient.ability(slot);
            slotAbilities[slot] = ability;
            slotNames[slot] = ability < 0 ? "" : abilityName(ability, kitFruit ? kit : null);
        }
        String name = player == null ? "Captain" : player.m_36316_().getName();
        return new LogbookView.Data(name, profile, haki, ranks, fruit, fruitMastery, styleHaki, fruitPage, slotKeys, slotNames, slotAbilities,
            points);
    }

    /** A quick slot's key as a short cap ("G", "Ctrl+B"). */
    static String slotKey(int slot) {
        KeyMapping[] keys = BindsClient.keys();
        return slot < keys.length ? AbilityHud.name(keys[slot]) : BindsClient.shortKey(BindsClient.keyName(slot));
    }

    /** The key a quick ability sits on, or "-". */
    static String keyOf(int ability) {
        int slot = BindsClient.slotOf(ability);
        return slot < 0 ? "-" : slotKey(slot);
    }

    /**
     * The key a kit fruit's move is on now: its quick slot's key (the move's ability is its default slot, {@code FruitMove.slot}, as the
     * original's FruitHud.onKey reads it), "-" if that ability is on no slot, or its own key ("LMB hold") if it has no slot.
     */
    /** 3.4.2: the fruit move keys this mod adds (client/PieceKeys FRUIT_N, FRUIT_M), named as bound in Controls. */
    static final String[][] OWN_KEYS = {{"[N]"}, {"[M]"}};

    static String moveKey(com.hexhaki.fruit.FruitMove move) {
        int slot = move.slot();
        if (slot >= 0) {
            return keyOf(slot);
        }
        // 3.4.2 (the owner: "Awakening is for some fucking reason bound to H (which makes it not work)"): "[N]" and "[M]" are this mod's
        // own keys (client/FruitKeyN); the original's labels read its old letters M and N as its Convergence key (H) and slot 8 (V)
        String key = move.key() == null ? "" : move.key();
        for (String[] own : OWN_KEYS) {
            if (key.startsWith(own[0])) {
                String name = AbilityHud.name("[N]".equals(own[0]) ? PieceKeys.FRUIT_N : PieceKeys.FRUIT_M);
                String rest = key.substring(own[0].length()).trim();
                return rest.isEmpty() ? name : name + " " + rest;
            }
        }
        return "passive".equals(move.key()) ? "-" : live(move.key()).replace("[", "").replace("]", "");
    }

    /** 3.5.9: WiFi Haki is a Swordsman's alone (the owner). */
    static boolean swordsman() {
        com.hexhaki.piece.PieceProfile self = PieceClient.self();
        return self.chosen() && self.style() != null && self.style().sword();
    }

    /** The Conqueror's (and Armament's) four quick powers, as the original Ability Book numbers them (100 + n). */
    static int quick(HakiTechnique t) {
        if (t == HakiTechnique.CONQ_AWAKENING) {
            return Binds.HAKI_BASE;
        } else if (t == HakiTechnique.CONQ_DOMINION) {
            return Binds.HAKI_BASE + 1;
        } else if (t == HakiTechnique.CONQ_SOVEREIGN) {
            return Binds.HAKI_BASE + 2;
        } else if (t == HakiTechnique.ARM_GALAXY_WAVE) {
            return Binds.HAKI_BASE + 3;
        }
        return -1;
    }

    /** The techniques on keys of their own (null: passive). Armament is Ctrl + R for a player with a style (R is combat mode); Ryou
     *  comes with Armament once unlocked (2.1.3). */
    static String permanent(HakiTechnique t) {
        String armament = (CombatMode.styled() ? "Ctrl+" : "") + AbilityHud.name(HakiKeys.ARMAMENT);
        if (t == HakiTechnique.ARM_REINFORCEMENT || t == HakiTechnique.CONQ_ACOC) {
            return armament;
        } else if (t == HakiTechnique.OBS_PRESENCE) {
            return AbilityHud.name(HakiKeys.OBSERVATION);
        } else if (t == HakiTechnique.ARM_HAKI_LEAP) {
            return "Shift+Space";
        } else if (t == HakiTechnique.ARM_BLADE_CUT) {
            return "Swing";
        }
        return null;
    }

    static String abilityName(int ability, Fruit kit) {
        if (com.hexhaki.piece.haki.QuickSlots.gone(ability)) {
            return ""; // 3.5.9: the Haki Grip and the Galaxy Fist are gone
        }
        if (Binds.isHaki(ability)) {
            for (HakiTechnique t : HakiTechnique.values()) {
                if (quick(t) == ability) {
                    return t.display;
                }
            }
            return AbilityHud.hakiName(ability);
        }
        if (kit != null) {
            for (FruitMove move : ClientKits.get(kit).moves()) {
                if (move.slot() == ability) {
                    return move.name();
                }
            }
        }
        return "";
    }

    /** Text with the original's key tags ("[R]", "[G]") read as the keys they are now. */
    static String live(String text) {
        if (text == null) {
            return "";
        }
        String armament = (CombatMode.styled() ? "Ctrl+" : "") + AbilityHud.name(HakiKeys.ARMAMENT);
        return BindsClient.live(text.replace("[R]", "[" + armament + "]"), false);
    }

    static Section haki(int i, int mastery) {
        HakiType type = TYPES[i];
        HakiRank rank = HakiRank.of(mastery);
        List<Entry> entries = new ArrayList<>();
        HakiTechnique next = null;
        for (HakiTechnique t : HakiTechnique.forType(type)) {
            if (t == HakiTechnique.ARM_GALAXY_WAVE || t == HakiTechnique.CONQ_DOMINION) {
                continue; // 3.5.9: the Galaxy Fist and the Haki Grip, removed entirely (the owner)
            }
            if (t == HakiTechnique.CONQ_SOVEREIGN && !swordsman()) {
                continue; // 3.5.9: WiFi Haki, a Swordsman's alone
            }
            boolean open = HakiMasteryScreen.unlocked(t, mastery);
            int quick = quick(t);
            String own = permanent(t);
            String key = quick >= 0 ? keyOf(quick) : own != null ? own : "-";
            String info = quick >= 0 ? (t == HakiTechnique.CONQ_SOVEREIGN ? "hold" : "tap or hold")
                + " its quick slot" : own != null ? "its own key" : "passive: always on";
            Entry entry = Entry.of(t.display, key, info, live(t.description), open, "Needs " + HakiMasteryScreen.requirement(t));
            if (t == HakiTechnique.CONQ_JOYBOY) {
                // 2.2.5 (the owner: "anyone by default at max charge shall be able to do that"): no switch, no unlock
                entry = Entry.of(t.display, "Full", "every full-charge release", "Every Conqueror's release at full charge is the JoyBoy one, for"
                    + " everyone: the supreme release, its storm of black lightning in every direction and the fissures it tears in the ground"
                    + " (they grow back on their own).", true, "");
                open = true;
            }
            entries.add(quick >= 0 ? entry.bindable(quick) : entry);
            if (!open && next == null) {
                next = t;
            }
        }
        List<String> about = new ArrayList<>();
        about.add("Rank " + pretty(rank.name()) + ", mastery " + mastery + "/1000" + (rank.nextMin() > rank.min ? "; the next rank at " + rank.nextMin()
            + "." : "."));
        if (next != null) {
            about.add("Next: " + next.display + " (" + HakiMasteryScreen.requirement(next) + ").");
        }
        about.add(switch (type) {
            case ARMAMENT -> "Trained by coated hits, guarding, Blade Cuts and the Haki Leap.";
            case OBSERVATION -> swordsman() ? "Trained by exploring, surviving, reading danger, dodging and holding WiFi Haki locks."
                : "Trained by exploring, surviving, reading danger and dodging.";
            default -> swordsman() ? "Trained by releasing the King's pressure and landing WiFi Haki." : "Trained by releasing the King's pressure.";
        });
        if (type == HakiType.ARMAMENT) {
            about.add("Haki energy " + Math.round(HakiClientState.energy) + "/" + Math.round(HakiClientState.maxEnergy) + ".");
        }
        float progress = mastery / 1000F;
        return new Section(NAMES[i], pretty(rank.name()) + " " + mastery + "/1000", COLOURS[i], progress, about, entries);
    }

    static Section kit(Fruit fruit, int mastery) {
        List<Entry> entries = new ArrayList<>();
        // 2.7.2 (the owner: "Add the moves to the Fruit ability hud and to the fruit progression menu"): the Uo Uo no Mi's log lists
        // every move, its human form's and its dragon's (the panel only the form's own)
        List<FruitMove> moves = fruit == Fruit.UO ? com.hexhaki.piece.server.UoKit.MOVES : ClientKits.get(fruit).moves();
        for (FruitMove move : moves) {
            boolean open = mastery >= move.mastery();
            int slot = move.slot();
            boolean passive = "passive".equals(move.key());
            String key = moveKey(move);
            String info = slot >= 0 ? (move.hold() ? "hold" : "tap") + " its quick slot" + (move.cooldown() != null ? ", then it recharges" : "")
                : passive ? "passive: always on" : "its own key";
            Entry entry = Entry.of(move.name(), key, info, live(move.description()), open, "Needs fruit mastery " + move.mastery());
            entries.add(slot >= 0 ? entry.bindable(slot) : entry);
        }
        return new Section(fruit.display, "Mastery " + mastery, 0xFF000000 | fruit.colour, Math.min(1F, mastery / 1000F), List.of(
            "Mastery " + mastery + ". Using the fruit's moves trains it.",
            "R swaps your hands between the fighting style and the fruit: M1 and the right button are the fruit's while it is out.",
            "Its quick powers keep their slots whatever your hands hold."), entries);
    }

    static Section nika() {
        // 3.2.3: every move on its key (client/gear/NikaKeys), the Haki versions named, the stretch moves untransformed too
        Minecraft minecraft = Minecraft.m_91087_();
        Object[][] rows = {
            {GearAbility.GEAR_FIVE, AbilityHud.name(HakiKeys.SLOT_6)},
            {GearAbility.PISTOL, AbilityHud.name(minecraft.f_91066_.f_92096_) + " (Dawn Pistol with Haki in Gear 5)"},
            {GearAbility.ROCKET, AbilityHud.name(HakiKeys.MODE) + " hold"},
            {GearAbility.STAR_GUN, AbilityHud.name(HakiKeys.MODE) + " with Haki in Gear 5"},
            {GearAbility.WHIP, AbilityHud.name(HakiKeys.SLOT_7) + " hold, Gear 5 only: Goof Ball (Dawn Whip with Haki)"},
            {GearAbility.GATLING, AbilityHud.name(HakiKeys.SLOT_8) + " hold"},
            {GearAbility.GIANT, AbilityHud.name(HakiKeys.STAR_GUN) + " hold, Gear 5 only (Bajrang Gun with Haki)"},
            {GearAbility.COIL, AbilityHud.name(com.hexhaki.piece.client.PieceKeys.FRUIT_N) + " hold (the lightning lance with Haki in Gear 5)"},
            {GearAbility.RUBBER_WORLD, AbilityHud.name(com.hexhaki.piece.client.PieceKeys.FRUIT_M) + " hold"}};
        List<Entry> entries = new ArrayList<>();
        for (Object[] row : rows) {
            GearAbility ability = (GearAbility) row[0];
            boolean open = GearClientState.unlocked(ability);
            boolean five = gearFiveOnly(ability);
            String lock = five ? "Needs Gear 5 and fruit mastery " + ability.mastery : "Needs fruit mastery " + ability.mastery;
            String info = five ? "in Gear 5" : "in Devil Fruit mode, transformed or not";
            // the original's text names Giant's old key ([G]); B is its key now (the label rewriter shows it as bound)
            String text = ability == GearAbility.GIANT ? ability.description.replace("[G]", "[B]") : ability.description;
            entries.add(Entry.of(ability.display, (String) row[1], info, HakiKeyLabels.live(text), open, lock));
        }
        long into = GearMastery.intoLevel(GearClientState.xp);
        long cost = Math.max(1, GearMastery.nextLevelCost(GearClientState.xp));
        return new Section(NIKA, "Mastery " + GearClientState.mastery, AbilityHud.NIKA_WHITE, Math.min(1F, into / (float) cost), List.of(
            "Mastery " + GearClientState.mastery + "/" + GearMastery.MAX_LEVEL + ", " + into + "/" + cost + " XP to the next level.",
            "R swaps between your fighting style and Devil Fruit mode, where the fruit has the keys: Z for Gear 5, X C V B N M its moves.",
            "Ctrl + R is Armament: with it (or Ryou) on in Gear 5 the moves become their Haki versions. Levels earn points for the upgrades below.",
            "In Gear 5 every jump bounces about five blocks high off ground that turns to rubber under you, and lands without fall damage."),
            entries);
    }

    /** 3.2.3 (the owner, asked: "All stretch moves" untransformed): the Nika moves only Gear 5 has (server/NikaRules). */
    static boolean gearFiveOnly(GearAbility ability) {
        return ability == GearAbility.STAR_GUN || ability == GearAbility.RUBBER_WORLD;
    }

    static Section upgrades() {
        List<Entry> entries = new ArrayList<>();
        for (GearUpgrade upgrade : GearUpgrade.values()) {
            int rank = GearClientState.rank(upgrade);
            entries.add(Entry.upgrade(upgrade.display, upgrade.description, upgrade.ordinal(), rank, upgrade.maxRank,
                rank >= upgrade.maxRank ? 0 : upgrade.costFor(rank)));
        }
        int total = Math.max(1, GearClientState.totalPoints);
        int free = GearClientState.freePoints;
        return new Section("Upgrades", free + (free == 1 ? " point free" : " points free"), 0xFFE8C26A, free / (float) total, List.of(
            free + " of " + GearClientState.totalPoints + " points free.", "Pick an upgrade and buy its next rank; Respec gives every point back."),
            entries);
    }
}
