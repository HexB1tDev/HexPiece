package com.hexhaki.piece.ui;

import java.util.List;

/**
 * 2.2.2: the abilities panel drawn on any {@link UiCanvas} (the game's GUI or the preview's image), HexGhoul's {@code AbilityHudView}:
 * a small dark card with the mode's colour down its left edge, its title and subtitle on one line, the Haki energy as a thin gauge
 * under them, and one line a move: the player's keys in little key caps (the move's colour as their underline, filling back up while
 * it recharges), the move's name in its colour; a move not reached yet dim, with the mastery it needs; one recharging dim, with the
 * seconds left. Laid out once per panel ({@link Layout}), so a frame only fills and writes.
 */
public final class AbilityHudView {
    static final int PAD = 3;
    static final int LINE = 10;
    static final int CHIP_PAD = 2;
    static final int GAP = 3;
    static final int TITLE_GAP = 4;
    static final int BACK = 0xC0100C13;
    static final int EDGE = 0x30FFFFFF;
    static final int CHIP = 0xFF2A2430;
    static final int CHIP_SHADE = 0xFF17131B;
    /** The widest recharge a line shows. */
    static final String WIDEST = "00.0s";

    /** Where everything goes, worked out once per panel: its size, the keys' and the names' widths, room kept after names for a recharge. */
    public record Layout(AbilityPanel.Panel panel, int width, int height, int keyWidth, int nameWidth, int tail) {
    }

    private AbilityHudView() {
    }

    /** The panel's layout on {@code canvas} (text widths measured once). */
    public static Layout layout(UiCanvas canvas, AbilityPanel.Panel panel) {
        int keyWidth = 0;
        int nameWidth = 0;
        boolean waits = false;
        for (AbilityPanel.Row row : panel.rows()) {
            keyWidth = Math.max(keyWidth, canvas.width(row.keys()) + 2 * CHIP_PAD);
            nameWidth = Math.max(nameWidth, canvas.width(row.name() + (row.locked() ? " " + row.needs() : "")));
            waits |= row.of() > 0;
        }
        int tail = waits ? canvas.width(WIDEST) : 0;
        int body = keyWidth + GAP + nameWidth + (tail > 0 ? GAP + tail : 0);
        int header = canvas.width(panel.title()) + 1 + (panel.subtitle().isEmpty() ? 0 : TITLE_GAP + canvas.width(panel.subtitle()));
        int width = 2 + PAD + Math.max(body, header) + PAD + 2;
        int height = PAD + 9 + 4 + panel.rows().size() * LINE + PAD - 1;
        return new Layout(panel, width, height, keyWidth, nameWidth, tail);
    }

    /** Draws the panel with its bottom-left corner at ({@code left}, {@code bottom}), {@code scale} times its size. */
    public static void draw(UiCanvas c, Layout layout, int left, int bottom, float scale) {
        AbilityPanel.Panel panel = layout.panel();
        int w = layout.width();
        int h = layout.height();
        c.push();
        c.translate(left, bottom - h * scale);
        c.scale(scale);
        // The card, its corners cut, and the mode's colour down the left.
        c.fill(1, 0, w - 1, h, BACK);
        c.fill(0, 1, 1, h - 1, BACK);
        c.fill(w - 1, 1, w, h - 1, BACK);
        c.fill(1, 0, w - 1, 1, EDGE);
        c.fill(0, 1, 2, h - 1, panel.accent());
        int x = 2 + PAD;
        int y = PAD;
        c.text(panel.title(), x, y, panel.accent(), true);
        if (!panel.subtitle().isEmpty()) {
            c.text(panel.subtitle(), x + c.width(panel.title()) + 1 + TITLE_GAP, y, AbilityPanel.DIM, false);
        }
        y += 9 + 1;
        // Under the title: the Haki energy, a thin gauge (or the plain rule when there is none).
        int right = w - PAD;
        c.fill(x, y, right, y + 2, (panel.accent() & 0x00FFFFFF) | 0x40000000);
        if (panel.energy() >= 0.0) {
            c.fill(x, y, x + (int)Math.round((right - x) * panel.energy()), y + 2, AbilityPanel.ENERGY);
        }
        y += 4;
        List<AbilityPanel.Row> rows = panel.rows();
        for (int i = 0; i < rows.size(); i++) {
            AbilityPanel.Row row = rows.get(i);
            int top = y + i * LINE;
            int chip = c.width(row.keys()) + 2 * CHIP_PAD;
            double share = row.ready();
            boolean waiting = share < 1.0;
            boolean dim = row.locked() || waiting;
            // The key cap, right-aligned in the keys' column so the names line up; the move's colour as its underline.
            int chipLeft = x + layout.keyWidth() - chip;
            c.fill(chipLeft, top - 1, chipLeft + chip, top + 8, row.locked() ? CHIP_SHADE : CHIP);
            c.fill(chipLeft, top + 8, chipLeft + chip, top + 9, dim ? CHIP_SHADE : (row.colour() & 0x00FFFFFF) | 0xA0000000);
            if (waiting && !row.locked()) {
                c.fill(chipLeft, top + 8, chipLeft + (int)Math.round(chip * share), top + 9, (row.colour() & 0x00FFFFFF) | 0x70000000);
            }
            c.text(row.keys(), chipLeft + CHIP_PAD, top, dim ? AbilityPanel.DIM : AbilityPanel.TEXT, false);
            int nameX = x + layout.keyWidth() + GAP;
            c.text(row.name(), nameX, top, dim ? AbilityPanel.DIM : row.colour(), false);
            if (row.locked()) {
                c.text(" " + row.needs(), nameX + c.width(row.name()), top, 0xFF5A5260, false);
            }
            if (waiting && layout.tail() > 0) {
                String tail = AbilityPanel.seconds(row.left());
                c.text(tail, nameX + layout.nameWidth() + GAP + layout.tail() - c.width(tail), top, AbilityPanel.DIM, false);
            }
        }
        c.pop();
    }
}
