package com.hexhaki.piece.ui;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;

/**
 * 2.1.1 (the owner: "New mastery menu (also pirate UI, like blue, white, with a ship?)"): the Captain's Log (pure, previewed by
 * tools/preview/UiPreview). A navy sea with white crests, a white-sailed ship flying the player's flag, and a sailcloth panel with blue
 * trim: the overview (a wanted poster for a pirate, a service record for a marine), Haki mastery, the fighting style's moves and mastery,
 * the Devil Fruit. Buttons open the original Ability Book (to bind abilities) and Haki technique list, which it replaces on the key.
 */
public final class LogbookView {
    public static final String[] TABS = {"Overview", "Haki", "Style", "Devil Fruit"};
    static final String[] HAKI = {"Armament", "Observation", "Conqueror's"};

    /** What the log shows: the player's name and profile, each Haki's mastery (0..1000) and rank, their fruit and its mastery. */
    public record Data(String name, PieceProfile profile, int[] haki, String[] hakiRanks, String fruit, int fruitMastery, int tab) {
    }

    public record Layout(int width, int height, int x0, int y0, int x1, int y1, int shipX) {
        public static Layout of(int width, int height) {
            int w = Math.min(330, width - 150);
            int h = Math.min(250, height - 30);
            int x1 = width - Math.max(10, (width - w - 140) / 2);
            int x0 = x1 - w;
            int y0 = (height - h) / 2;
            return new Layout(width, height, x0, y0, x1, y0 + h, Math.max(70, x0 / 2));
        }

        int[] tab(int index) {
            int w = (this.x1 - this.x0 - 16) / TABS.length;
            int x = this.x0 + 8 + index * w;
            return new int[] {x, this.y0 + 22, x + w - 2, this.y0 + 36};
        }

        int[] button(int index) {
            String[] labels = buttons();
            int w = (this.x1 - this.x0 - 16 - (labels.length - 1) * 6) / labels.length;
            int x = this.x0 + 8 + index * (w + 6);
            return new int[] {x, this.y1 - 22, x + w, this.y1 - 7};
        }
    }

    static String[] buttons() {
        return new String[] {"Ability Book", "Haki Techniques", "Close"};
    }

    private LogbookView() {
    }

    static boolean inside(int[] box, double x, double y) {
        return x >= box[0] && x < box[2] && y >= box[1] && y < box[3];
    }

    /** "tab:N", "book", "haki", "close" or null. */
    public static String at(Layout l, double x, double y) {
        for (int i = 0; i < TABS.length; i++) {
            if (inside(l.tab(i), x, y)) {
                return "tab:" + i;
            }
        }
        String[] ids = {"book", "haki", "close"};
        for (int i = 0; i < ids.length; i++) {
            if (inside(l.button(i), x, y)) {
                return ids[i];
            }
        }
        return null;
    }

    public static void draw(UiCanvas c, Layout l, Data data, String hover, double time) {
        PieceProfile p = data.profile();
        boolean marine = p.faction() == Faction.MARINE;
        // the sea at night: navy, white crests, the ship riding it
        c.gradient(0, 0, l.width, l.height / 2, 0xFF081A33, PirateUi.NAVY);
        PirateUi.sea(c, 0, l.height / 2, l.width, l.height, time, PirateUi.NAVY_LIGHT, 0xFF061224, 0xB0FFFFFF);
        for (int i = 0; i < 26; i++) {
            int sx = (i * 73) % Math.max(1, l.width);
            int sy = (i * 37) % Math.max(1, l.height / 2 - 10);
            int a = (int) (90 + 80 * Math.sin(time * 2 + i));
            c.fill(sx, sy, sx + 1, sy + 1, a << 24 | 0xFFFFFF);
        }
        PirateUi.circle(c, l.width * 0.12F, l.height * 0.12F, 9, 0xFFF4F1E0);
        PirateUi.circle(c, l.width * 0.12F + 3, l.height * 0.12F - 2, 8, 0xFF0B1F3C);
        PirateUi.ship(c, l.shipX, l.height / 2 + 24, Math.min(120, l.x0 - 20), time, marine ? 0xFFE9EEF5 : 0xFF2A1C10, PirateUi.SAIL,
            marine ? PirateUi.SAIL : 0xFF151515, marine);
        c.centered(data.name(), l.shipX, l.height / 2 + 34, 0xFFFFFFFF, true);
        c.centered(p.faction().choice() ? p.faction().display() + " - " + p.title() : "No flag yet", l.shipX, l.height / 2 + 45,
            marine ? PirateUi.BLUE_LIGHT : PirateUi.GOLD, true);
        // the sailcloth panel
        c.fill(l.x0 + 4, l.y0 + 4, l.x1 + 4, l.y1 + 4, 0x70000000);
        c.fill(l.x0, l.y0, l.x1, l.y1, PirateUi.SAIL);
        c.gradient(l.x0, l.y1 - 40, l.x1, l.y1, 0x00DCE6F2, PirateUi.SAIL_SHADE);
        c.fill(l.x0, l.y0, l.x1, l.y0 + 18, PirateUi.BLUE);
        c.fill(l.x0, l.y0 + 18, l.x1, l.y0 + 20, PirateUi.BLUE_INK);
        PirateUi.border(c, l.x0, l.y0, l.x1, l.y1, PirateUi.BLUE_INK);
        PirateUi.border(c, l.x0 + 2, l.y0 + 20, l.x1 - 2, l.y1 - 2, PirateUi.BLUE_LIGHT);
        PirateUi.gull(c, l.x0 + 14, l.y0 + 10, 7, 0xFFFFFFFF);
        c.text(marine ? "MARINE SERVICE RECORD" : "CAPTAIN'S LOG", l.x0 + 26, l.y0 + 5, 0xFFFFFFFF, true);
        String rightTitle = PieceRules.berries(p.renown());
        c.text(marine ? "Merit " + p.renown() : rightTitle, l.x1 - 8 - c.width(marine ? "Merit " + p.renown() : rightTitle), l.y0 + 5, 0xFFDDEBFF, false);
        for (int i = 0; i < TABS.length; i++) {
            int[] b = l.tab(i);
            boolean open = data.tab() == i;
            boolean over = ("tab:" + i).equals(hover);
            c.fill(b[0], b[1], b[2], b[3], open ? PirateUi.BLUE : over ? PirateUi.BLUE_LIGHT : 0xFFE4ECF6);
            PirateUi.border(c, b[0], b[1], b[2], b[3], open ? PirateUi.BLUE_INK : 0xFF9CB6D6);
            c.centered(TABS[i], (b[0] + b[2]) / 2, b[1] + 3, open ? 0xFFFFFFFF : PirateUi.BLUE_INK, false);
        }
        int top = l.y0 + 42;
        switch (data.tab()) {
            case 1 -> haki(c, l, data, top);
            case 2 -> style(c, l, data, top);
            case 3 -> fruit(c, l, data, top);
            default -> overview(c, l, data, top, time);
        }
        String[] labels = buttons();
        String[] ids = {"book", "haki", "close"};
        for (int i = 0; i < labels.length; i++) {
            int[] b = l.button(i);
            boolean over = ids[i].equals(hover);
            c.fill(b[0], b[1], b[2], b[3], over ? PirateUi.BLUE : PirateUi.BLUE_INK);
            c.fill(b[0], b[1], b[2], b[1] + 1, 0x60FFFFFF);
            c.centered(labels[i], (b[0] + b[2]) / 2, b[1] + 4, 0xFFFFFFFF, false);
        }
    }

    static void label(UiCanvas c, String text, int x, int y) {
        c.text(text, x, y, PirateUi.BLUE, false);
    }

    static void overview(UiCanvas c, Layout l, Data data, int top, double time) {
        PieceProfile p = data.profile();
        int left = l.x0 + 10;
        int posterW = 92;
        int posterX = l.x1 - 10 - posterW;
        int posterY = top;
        int posterH = Math.min(118, l.y1 - 30 - top);
        if (p.faction() == Faction.MARINE) {
            c.fill(posterX, posterY, posterX + posterW, posterY + posterH, 0xFFEAF1FA);
            PirateUi.border(c, posterX, posterY, posterX + posterW, posterY + posterH, PirateUi.BLUE);
            c.centered("RANK", posterX + posterW / 2, posterY + 5, PirateUi.BLUE, false);
            PirateUi.gull(c, posterX + posterW / 2F, posterY + 30, 14, PirateUi.BLUE);
            c.face(posterX + posterW / 2 - 14, posterY + 40, 28);
            c.centered(c.fit(p.title(), posterW - 6), posterX + posterW / 2, posterY + 74, PirateUi.BLUE_INK, false);
            int stars = Math.min(5, (int) (p.renown() / 400));
            for (int i = 0; i < stars; i++) {
                PirateUi.circle(c, posterX + posterW / 2F - (stars - 1) * 5 + i * 10, posterY + 90, 3, PirateUi.GOLD);
            }
            c.centered("Merit " + p.renown(), posterX + posterW / 2, posterY + posterH - 12, PirateUi.BLUE, false);
        } else {
            c.fill(posterX, posterY, posterX + posterW, posterY + posterH, 0xFFE8D7AE);
            PirateUi.border(c, posterX, posterY, posterX + posterW, posterY + posterH, 0xFF8A6A3C);
            c.bigCentered("WANTED", posterX + posterW / 2, posterY + 4, PirateUi.INK, false, 1.5F);
            c.fill(posterX + 14, posterY + 19, posterX + posterW - 14, posterY + 64, 0xFFBFA77A);
            c.face(posterX + posterW / 2 - 20, posterY + 21, 40);
            c.centered("DEAD OR ALIVE", posterX + posterW / 2, posterY + 67, PirateUi.INK, false);
            c.centered(c.fit(data.name(), posterW - 6), posterX + posterW / 2, posterY + 78, PirateUi.INK, false);
            c.centered(c.fit(PieceRules.berries(p.renown()), posterW - 6), posterX + posterW / 2, posterY + 92, PirateUi.RED, false);
            c.centered("berries", posterX + posterW / 2, posterY + 102, PirateUi.INK_SOFT, false);
        }
        int y = top;
        label(c, "Name", left, y);
        c.text(c.fit(data.name(), posterX - left - 60), left + 56, y, PirateUi.BLUE_INK, false);
        y += 12;
        label(c, "Flag", left, y);
        c.text(p.faction().choice() ? p.faction().display() : "None yet", left + 56, y, PirateUi.BLUE_INK, false);
        y += 12;
        label(c, "Title", left, y);
        c.text(c.fit(p.title(), posterX - left - 60), left + 56, y, PirateUi.BLUE_INK, false);
        y += 12;
        label(c, "Body", left, y);
        c.text(p.gender() == Gender.UNSET ? "-" : p.gender().display(), left + 56, y, PirateUi.BLUE_INK, false);
        y += 12;
        label(c, "Style", left, y);
        c.fill(left + 56, y, left + 63, y + 7, p.style().colour() | 0xFF000000);
        c.text(p.style().display(), left + 67, y, PirateUi.BLUE_INK, false);
        y += 16;
        int barRight = posterX - 10;
        label(c, "Style mastery", left, y);
        c.text(p.styleMastery() + "/" + PieceRules.MAX_MASTERY, barRight - c.width(p.styleMastery() + "/" + PieceRules.MAX_MASTERY), y,
            PirateUi.BLUE_INK, false);
        PirateUi.bar(c, left, y + 10, barRight, y + 15, p.styleMastery() / (float) PieceRules.MAX_MASTERY, PirateUi.BLUE, 0xFFD6E2F0);
        y += 22;
        for (int i = 0; i < 3; i++) {
            label(c, HAKI[i], left, y);
            String rank = data.hakiRanks()[i];
            c.text(c.fit(rank, 70), barRight - c.width(c.fit(rank, 70)), y, PirateUi.BLUE_INK, false);
            PirateUi.bar(c, left, y + 10, barRight, y + 14, data.haki()[i] / 1000F, i == 2 ? 0xFF8A2030 : 0xFF1D3F66, 0xFFD6E2F0);
            y += 19;
        }
        long next = PieceRules.nextTitle(p.faction(), p.renown());
        if (p.faction().choice() && next > 0) {
            String goal = p.faction() == Faction.PIRATE ? "Next title at " + PieceRules.berries(next) + " berries" : "Next rank at " + next + " merit";
            c.text(c.fit(goal, l.x1 - l.x0 - 20), left, Math.min(y + 2, l.y1 - 34), PirateUi.BLUE, false);
        }
    }

    static void haki(UiCanvas c, Layout l, Data data, int top) {
        int left = l.x0 + 10;
        int right = l.x1 - 10;
        String[] lines = {
            "Coat your body black and hard: blows land through anything, coated strikes throw black lightning.",
            "Read the field: sense every presence, dodge what is coming before it comes.",
            "The will of a king: a roar that knocks the weak out and splits the sky with black lightning."};
        int y = top;
        for (int i = 0; i < 3; i++) {
            c.fill(left - 2, y - 2, right + 2, y + 44, i % 2 == 0 ? 0xFFEEF3FA : 0xFFF7F9FC);
            c.text(HAKI[i] + " Haki", left, y, PirateUi.BLUE_INK, false);
            String rank = data.hakiRanks()[i] + "  " + data.haki()[i] + "/1000";
            c.text(rank, right - c.width(rank), y, PirateUi.BLUE, false);
            PirateUi.bar(c, left, y + 11, right, y + 17, data.haki()[i] / 1000F, i == 2 ? 0xFF8A2030 : PirateUi.BLUE, 0xFFD6E2F0);
            c.wrap(lines[i], left, y + 21, right - left, 0xFF4A5E7A, 9, 2);
            y += 50;
        }
        c.text("Techniques and their keys: Haki Techniques, Ability Book.", left, Math.min(y, l.y1 - 34), PirateUi.BLUE, false);
    }

    static void style(UiCanvas c, Layout l, Data data, int top) {
        PieceProfile p = data.profile();
        FightingStyle style = p.style();
        int left = l.x0 + 10;
        int right = l.x1 - 10;
        if (!style.choice()) {
            c.wrap("No fighting style yet. Choose one when you set sail.", left, top, right - left, PirateUi.BLUE_INK, 10, 3);
            return;
        }
        int mastery = p.styleMastery();
        c.fill(left, top, left + 8, top + 8, style.colour() | 0xFF000000);
        c.text(style.display(), left + 12, top, PirateUi.BLUE_INK, false);
        String m = "Mastery " + mastery + "/" + PieceRules.MAX_MASTERY + "  (x" + String.format(java.util.Locale.ROOT, "%.2f",
            PieceRules.masteryDamage(mastery)) + " damage)";
        c.text(m, right - c.width(m), top, PirateUi.BLUE, false);
        PirateUi.bar(c, left, top + 11, right, top + 16, mastery / (float) PieceRules.MAX_MASTERY, style.colour() | 0xFF000000, 0xFFD6E2F0);
        int y = top + 22;
        Slot[] slots = Slot.values();
        int rowH = Math.max(14, Math.min(20, (l.y1 - 30 - y) / slots.length));
        for (Slot slot : slots) {
            StyleMove move = StyleMove.of(style, slot);
            if (move == null) {
                continue;
            }
            boolean open = mastery >= move.mastery();
            String key = switch (slot) {
                case M1_1 -> "M1 1";
                case M1_2 -> "M1 2";
                case M1_3 -> "M1 3";
                case M1_4 -> "M1 4";
                case HEAVY -> "RMB";
                case SPECIAL_A -> "Y";
                case SPECIAL_B -> "U";
            };
            c.fill(left - 2, y - 1, right + 2, y + rowH - 3, open ? 0xFFEEF3FA : 0xFFE6E6E6);
            c.fill(left - 2, y - 1, left + 26, y + rowH - 3, open ? PirateUi.BLUE : 0xFF8A8A8A);
            c.centered(key, left + 12, y + (rowH - 12) / 2, 0xFFFFFFFF, false);
            c.text(move.display(), left + 32, y + (rowH - 12) / 2, open ? PirateUi.BLUE_INK : 0xFF7A7A7A, false);
            String info = !open ? "opens at " + move.mastery() : move.cooldown() > 0 ? String.format(java.util.Locale.ROOT, "%.1fs", move.cooldown() / 20.0) : "";
            c.text(info, right - c.width(info), y + (rowH - 12) / 2, open ? PirateUi.BLUE : PirateUi.RED, false);
            if (rowH >= 18) {
                c.text(c.fit(move.description(), right - left - 120), left + 32 + Math.max(70, c.width(move.display()) + 8), y + (rowH - 12) / 2,
                    0xFF6A7E9A, false);
            }
            y += rowH;
        }
    }

    static void fruit(UiCanvas c, Layout l, Data data, int top) {
        int left = l.x0 + 10;
        int right = l.x1 - 10;
        boolean none = data.fruit() == null || data.fruit().isEmpty();
        c.text(none ? "No Devil Fruit" : data.fruit(), left, top, none ? 0xFF7A7A7A : 0xFF6A2A8A, true);
        int y = top + 14;
        if (!none) {
            String m = "Mastery " + data.fruitMastery();
            c.text(m, right - c.width(m), top, PirateUi.BLUE, false);
            PirateUi.bar(c, left, y, right, y + 6, Math.min(1F, data.fruitMastery() / 1000F), 0xFF6A2A8A, 0xFFD6E2F0);
            y += 14;
        }
        c.wrap(none ? "Eat a Devil Fruit to gain its power. You can only ever hold one: a second tears the body apart."
                : "Switch to Devil Fruit mode to fight with it. Bind its moves to your quick slots in the Ability Book.",
            left, y, right - left, PirateUi.BLUE_INK, 10, 4);
    }

    /** Data for the previews. */
    public static Data sample(int tab) {
        int[] mastery = new int[FightingStyle.values().length];
        mastery[FightingStyle.SIX_POWERS.ordinal()] = 240;
        PieceProfile profile = new PieceProfile(Gender.FEMALE, tab % 2 == 0 ? Faction.PIRATE : Faction.MARINE, FightingStyle.SIX_POWERS, true,
            mastery, tab % 2 == 0 ? 152_000_000L : 260L);
        return new Data("HexB1t", profile, new int[] {640, 310, 120}, new String[] {"Adept", "Novice", "Awakened"}, tab == 3 ? "Pika Pika no Mi" : "",
            420, tab);
    }
}
