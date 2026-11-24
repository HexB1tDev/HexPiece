package com.hexhaki.piece.client;

import com.hexhaki.binds.Binds;
import com.hexhaki.binds.client.BindsClient;
import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.devilfruit.data.GearAbility;
import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.piece.client.sword.SwordClient;
import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.ui.AbilityHudView;
import com.hexhaki.piece.ui.AbilityPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.2.2 (the owner: "all the abilities, everything has to be in the same menu, like HUD on the left. Like, I don't care about mastery
 * in the bottom. I want you to copy the one from Hex Ghoul and put it here, basically in our way"): HexGhoul's abilities panel, on the
 * left above the chat ({@link AbilityPanel}, drawn by {@link AbilityHudView}): the style's moves in combat mode (the Swordsman's draw,
 * string, charge and quick draw), the fruit's or Gear 5's in fruit mode, every Haki power with the quick slots the player bound, the
 * swap key and the Captain's Log, with the keys bound in Controls. It replaces the original's three HUDs (Haki with its mastery bars,
 * the fruit's, Gear 5's: {@link #replaces}, which the patch asks first) and our old combat badge; a fruit's own gauge (its kit's HUD)
 * is drawn under it. Cheap: the keys are read twice a second, the panel is rebuilt only when what it shows changes, and a frame is a
 * few fills and lines of text. Hidden while any screen is open.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class AbilityHud {
    /** The panel's size on screen (GUI pixels per panel pixel): HexGhoul's (its owner found larger ones too big). */
    public static final float SCALE = 0.6F;
    private static final long KEY_NANOS = 500_000_000L;
    private static Map<String, String> keys = Map.of();
    /** 2.7.5: the same keys without the mode switch, for a transformed dragon (R is locked: DragonClient's dragon locks). */
    private static Map<String, String> lockedKeys = Map.of();
    private static long keysAt = Long.MIN_VALUE;
    private static AbilityPanel.Input shown;
    private static AbilityHudView.Layout layout;
    /** The longest recharge seen for each fruit move (its gauge's whole length). */
    private static final Map<String, Long> LONGEST = new HashMap<>();

    private AbilityHud() {
    }

    /** True: the original's HUDs (Haki, fruit, Gear 5) draw nothing; this panel shows it all (asked by the patched originals). */
    public static boolean replaces() {
        return true;
    }

    static void draw(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91080_ != null) {
            return;
        }
        long now = System.nanoTime();
        if (now - keysAt > KEY_NANOS || keysAt == Long.MIN_VALUE) {
            keys = keys(minecraft);
            Map<String, String> locked = new HashMap<>(keys);
            locked.remove("toggle");
            lockedKeys = locked;
            keysAt = now;
        }
        AbilityPanel.Input input;
        try {
            input = input(player);
        } catch (RuntimeException unknown) {
            return; // the panel never stops the game
        }
        GuiCanvas canvas = new GuiCanvas(graphics, minecraft.f_91062_, null);
        if (!input.equals(shown) || layout == null) {
            layout = AbilityHudView.layout(canvas, AbilityPanel.of(input));
            shown = input;
        }
        // Just above where the chat's lines go (vanilla draws them up from 40 above the bottom of the screen).
        ChatComponent chat = minecraft.f_91065_.m_93076_();
        int chatTop = height - 40 - (int)Math.ceil(chat.m_93814_() * chat.m_93815_());
        int bottom = chatTop - 4;
        AbilityHudView.draw(canvas, layout, 2, bottom, SCALE);
        // A fruit's own gauge (its kit's HUD, as the original's fruit HUD drew it) just under the panel's top... beside it.
        if (input.fruitMode() && FruitClient.owned() && FruitClient.fruit() != null && FruitClient.fruit() != Fruit.NONE) {
            try {
                ClientKits.get(FruitClient.fruit()).hud(graphics, 2 + Math.round(layout.width() * SCALE) + 4, bottom - 24, 128, FruitClient.local());
            } catch (RuntimeException ignored) {
                // a kit's gauge is extra
            }
        }
    }

    /** What the panel shows for {@code player} now. */
    static AbilityPanel.Input input(LocalPlayer player) {
        FightingStyle style = PieceClient.self().style();
        boolean combat = CombatMode.on();
        List<String> moves = new ArrayList<>();
        if (style == FightingStyle.BRAWLER) {
            for (com.hexhaki.piece.ironfist.IronMove move : com.hexhaki.piece.ironfist.IronMove.PANEL) {
                moves.add(move.display()); // 3.5.8: Garp's Iron Fist, N its ultimate
            }
        } else {
            for (Slot slot : new Slot[] {Slot.M1_1, Slot.HEAVY, Slot.SPECIAL_A, Slot.SPECIAL_B, Slot.EXTRA_C, Slot.EXTRA_V, Slot.EXTRA_B}) {
                StyleMove move = StyleMove.of(style, slot);
                moves.add(move == null ? "" : slot == Slot.M1_1 ? "Combo" : move.display());
            }
        }
        String sword = null;
        if (style.sword() && SwordClient.katana(player.m_19879_()) != null) {
            int anim = SwordClient.anim(player.m_19879_());
            sword = anim == SwordAnims.NONE ? "sheathed" : anim == SwordAnims.DRAW ? "drawing" : anim == SwordAnims.SHEATHE ? "sheathing" : "drawn";
        }
        // the fruit: a kit fruit (Pika, Magu, Gura) or the Gomu fruit (Gear 5)
        String fruit = null;
        int fruitColour = 0xFFE8C26A;
        boolean fruitMode = false;
        List<AbilityPanel.Move> fruitMoves = new ArrayList<>();
        if (GearClientState.hasFruit) {
            fruit = GearClientState.gearFive ? "Gear 5" : "Gomu Gomu";
            // 3.2.3 (the owner: "We need to change the color of the HUD to be white instead of yellow"): white, transformed or not
            fruitColour = NIKA_WHITE;
            fruitMode = GearClientState.devilFruitMode();
            if (fruitMode) {
                gear(fruitMoves);
            }
        } else if (FruitClient.owned() && FruitClient.fruit() != null && FruitClient.fruit() != Fruit.NONE) {
            Fruit f = FruitClient.fruit();
            fruit = f.display;
            fruitColour = 0xFF000000 | f.colour;
            fruitMode = FruitClient.active() && !combat;
            if (fruitMode) {
                kit(f, fruitMoves);
            }
        }
        boolean haki = HakiClientState.enabled || HakiClientState.armament > 0 || HakiClientState.observation > 0 || HakiClientState.conqueror > 0;
        List<AbilityPanel.Move> slots = new ArrayList<>();
        if (haki && HakiClientState.conqueror > 0 && !(fruitMode && GearClientState.hasFruit)) {
            int[] layout = BindsClient.layout();
            for (int slot = 0; layout != null && slot < layout.length; slot++) {
                int ability = layout[slot];
                if (Binds.isHaki(ability)) {
                    slots.add(new AbilityPanel.Move(BindsClient.keyName(slot), hakiName(ability), "", 0, 0));
                }
            }
        }
        return new AbilityPanel.Input(com.hexhaki.piece.client.dragon.DragonClient.selfOut() ? lockedKeys : keys, style.display(), style.colour(), combat, moves, sword, fruit, fruitColour, fruitMode, fruitMoves, haki,
            HakiClientState.armamentOn, HakiClientState.observationOn, HakiClientState.acocOn, slots, HakiClientState.energy,
            HakiClientState.maxEnergy);
    }

    /** The Conqueror's quick-slot powers, as the Ability Book names them. */
    static String hakiName(int ability) {
        return switch (ability - Binds.HAKI_BASE) {
            case 0 -> "King's Awakening (tap / hold)";
            case 1 -> "Haki Grip";
            case 2 -> "WiFi Haki";
            case 3 -> com.hexhaki.piece.ui.LogbookView.GALAXY; // 2.3.7: the original's Haki Projection (3.5.7: no one's until Iron Fist is remade)
            default -> "Haki";
        };
    }

    /** A kit fruit's moves: the key each is bound to, the mastery it needs, its recharge. */
    static void kit(Fruit fruit, List<AbilityPanel.Move> out) {
        ClientKit kit = ClientKits.get(fruit);
        List<FruitMove> moves = kit.moves();
        int mastery = FruitClient.mastery();
        for (int i = 0; i < moves.size(); i++) {
            FruitMove move = moves.get(i);
            // 2.7.5 (the owner: "the keys HUD a ton of them say No slot"): a move's ability is its default slot, not its place in the
            // list (BindsClient.keyFor(i) asked for ability i: the Uo moves, out of slot order, were all "no slot")
            String key = LogbookData.moveKey(move);
            if (move.key() != null && (move.key().startsWith("[N]") || move.key().startsWith("[M]"))) {
                // 3.1.7: the N key (client/FruitKeyN), as bound in Controls; 3.4.2: M too (the original's label read it as H)
                key = name(move.key().startsWith("[N]") ? PieceKeys.FRUIT_N : PieceKeys.FRUIT_M);
            } else if (move.slot() < 0 && key.indexOf(' ') > 0) {
                key = key.substring(0, key.indexOf(' ')); // "LMB hold": the cap names the key alone, as every other row's does
            }
            long wait = move.cooldown() == null || move.cooldown().isEmpty() ? 0 : Math.max(0, FruitClient.cooldown(move.cooldown()));
            long of = Math.max(wait, LONGEST.getOrDefault(move.name(), 0L));
            LONGEST.put(move.name(), wait > 0 ? of : 0L);
            out.add(new AbilityPanel.Move(key == null ? "-" : key, move.name(), move.mastery() > mastery ? String.valueOf(move.mastery()) : "", wait, of));
        }
    }

    /** 3.2.3: the Nika fruit's colour on the panel (the owner: "white instead of yellow"). */
    static final int NIKA_WHITE = 0xFFF2F0E6;

    /**
     * The Nika fruit's moves in Devil Fruit mode, on its keys (client/gear/NikaKeys: Z X C V B N M since 3.2.3). 3.2.3 (the owner: "the
     * moves change name whenever you activate Haki, because currently they don't ... The HUD should like rename them as soon as Haki is
     * activated"): with Haki on in Gear 5 ({@code NikaKeys.haki}, as the server's dawn) each row shows its Haki version (Dawn Pistol,
     * Star Gun on X, Dawn Whip, the Bajrang Gun, the lightning lance); out of Gear 5 the moves only Gear 5 has say so.
     */
    static void gear(List<AbilityPanel.Move> out) {
        boolean five = GearClientState.gearFive;
        for (Object[] row : gearKeys()) {
            GearAbility ability = (GearAbility)row[0];
            String needs = !GearClientState.unlocked(ability) ? "locked" : !five && (Boolean)row[3] ? "Gear 5" : "";
            out.add(new AbilityPanel.Move((String)row[1], (String)row[2], needs, 0, 0));
        }
    }

    /** {ability, key, name, Gear 5 only} for each of the Nika fruit's moves as client/gear/NikaKeys reads them now. */
    static Object[][] gearKeys() {
        Minecraft minecraft = Minecraft.m_91087_();
        boolean haki = com.hexhaki.piece.client.gear.NikaKeys.haki();
        boolean five = GearClientState.gearFive;
        return new Object[][] {
            {GearAbility.GEAR_FIVE, name(HakiKeys.SLOT_6), five ? "Gear 5 (end)" : "Gear 5", false},
            {GearAbility.PISTOL, name(minecraft.f_91066_.f_92096_), haki ? "Dawn Pistol" : GearAbility.PISTOL.display, false},
            haki ? new Object[] {GearAbility.STAR_GUN, name(HakiKeys.MODE), GearAbility.STAR_GUN.display, true}
                : new Object[] {GearAbility.ROCKET, name(HakiKeys.MODE) + " hold", GearAbility.ROCKET.display, false},
            // 3.4.4: in Gear 5 C is Goof Ball (held), with Haki Dawn Whip; untransformed the plain whip
            // 3.4.6: Goof Ball and Giant only in Gear 5 (the owner's word): dimmed with "Gear 5" untransformed
            {GearAbility.WHIP, name(HakiKeys.SLOT_7) + (haki ? "" : " hold"), haki ? "Dawn Whip" : "Goof Ball", true},
            {GearAbility.GATLING, name(HakiKeys.SLOT_8) + " hold", GearAbility.GATLING.display, false},
            {GearAbility.GIANT, name(HakiKeys.STAR_GUN) + " hold", haki ? "Bajrang Gun" : GearAbility.GIANT.display, true},
            {GearAbility.COIL, name(PieceKeys.FRUIT_N) + " hold", haki ? "Lightning Lance" : GearAbility.COIL.display, false},
            // 3.3.2: Sky Bounce (Shift + Space) is gone (the owner: "shift + space ability is removed"): Gear 5's own jump bounces now
            {GearAbility.RUBBER_WORLD, name(PieceKeys.FRUIT_M) + " hold", GearAbility.RUBBER_WORLD.display, true}};
    }

    /** The player's name for each key the panel names, as bound in Controls (Forge's modifier included). */
    static Map<String, String> keys(Minecraft minecraft) {
        Map<String, String> out = new HashMap<>();
        out.put("attack", name(minecraft.f_91066_.f_92096_));
        out.put("use", name(minecraft.f_91066_.f_92095_));
        out.put("special_a", name(PieceKeys.SPECIAL_A));
        out.put("special_b", name(PieceKeys.SPECIAL_B));
        out.put("extra_c", name(PieceKeys.EXTRA_C));
        out.put("extra_v", name(PieceKeys.EXTRA_V));
        out.put("extra_b", name(PieceKeys.EXTRA_B));
        out.put("extra_n", name(PieceKeys.FRUIT_N));
        out.put("toggle", name(HakiKeys.ARMAMENT));
        out.put("armament", "Ctrl+" + name(HakiKeys.ARMAMENT));
        out.put("observation", name(HakiKeys.OBSERVATION));
        out.put("menu", name(HakiKeys.MASTERY));
        return out;
    }

    /** A key as a short cap: "R", "LMB", "Ctrl+F". */
    static String name(KeyMapping key) {
        if (key == null) {
            return "-";
        }
        Enum<?> modifier = key.getKeyModifier();
        return keyName(key.m_90865_(), modifier == null ? "NONE" : modifier.name());
    }

    /** {@code saved} ("key.keyboard.r", "key.mouse.left") with Forge's modifier, as a short cap. */
    public static String keyName(String saved, String modifier) {
        String name = saved == null ? "" : saved;
        if (name.equals("key.keyboard.unknown") || name.isEmpty()) {
            return "-";
        }
        String shown;
        if (name.startsWith("key.mouse.")) {
            String button = name.substring("key.mouse.".length());
            shown = switch (button) {
                case "left" -> "LMB";
                case "right" -> "RMB";
                case "middle" -> "MMB";
                default -> "M" + button;
            };
        } else {
            String k = name.startsWith("key.keyboard.") ? name.substring("key.keyboard.".length()) : name;
            shown = switch (k) {
                case "left.control", "right.control" -> "Ctrl";
                case "left.shift", "right.shift" -> "Shift";
                case "left.alt", "right.alt" -> "Alt";
                case "space" -> "Space";
                case "tab" -> "Tab";
                case "grave.accent" -> "`";
                default -> k.length() == 1 ? k.toUpperCase(java.util.Locale.ROOT) : k.replace('.', ' ').toUpperCase(java.util.Locale.ROOT);
            };
        }
        return switch (modifier) {
            case "CONTROL" -> "Ctrl+" + shown;
            case "SHIFT" -> "Shift+" + shown;
            case "ALT" -> "Alt+" + shown;
            default -> shown;
        };
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        // 2.2.9: a name of its own: Forge's event bus names the handler it generates from the package, "Overlay", the method and the
        // event, so two Overlay.register in one package shared one, and the game crashed on boot ("Overlay already registered")
        public static void registerAbilityOverlay(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_abilities", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height));
        }
    }

    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        shown = null;
        layout = null;
        keysAt = Long.MIN_VALUE;
        LONGEST.clear();
    }
}
