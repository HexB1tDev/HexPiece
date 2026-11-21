package com.hexhaki.piece.style;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.8.8: a player skin's boxes as the game lays them on a 64 x 64 skin (pure): for a box {@code w} x {@code h} x {@code d} pixels whose
 * texture starts at ({@code u}, {@code v}), the top at (u + d, v), the bottom at (u + d + w, v), and round its sides from (u, v + d):
 * the -x side, the front, the +x side, the back. Its faces as corner indices (StyleRig's numbering: bit 0 the x side, bit 1 down the
 * box, bit 2 the back) and the skin's rectangle for each (pixels: u0, v0, u1, v1), {@code from} to {@code to} of its height (a limb's
 * half: its rows only, the top on the upper half, the bottom on the lower).
 */
public final class SkinBox {
    private SkinBox() {
    }

    /** A part of the skin: where its box's texture starts, its size, the outer layer's start ({@code -1}: none). */
    public record Part(int u, int v, int w, int h, int d, int outerU, int outerV) {
    }

    public static final Part HEAD = new Part(0, 0, 8, 8, 8, 32, 0);
    public static final Part BODY = new Part(16, 16, 8, 12, 4, 16, 32);
    public static final Part RIGHT_ARM = new Part(40, 16, 4, 12, 4, 40, 32);
    public static final Part LEFT_ARM = new Part(32, 48, 4, 12, 4, 48, 48);
    public static final Part RIGHT_LEG = new Part(0, 16, 4, 12, 4, 0, 32);
    public static final Part LEFT_LEG = new Part(16, 48, 4, 12, 4, 0, 48);

    /** The faces of {@code part}'s box (the outer layer's if {@code outer}) for rows {@code from} to {@code to} of its height. */
    public static int[][] faces(Part part, boolean outer, int from, int to) {
        int u = outer ? part.outerU() : part.u();
        int v = outer ? part.outerV() : part.v();
        int w = part.w();
        int d = part.d();
        int row0 = v + d + from;
        int row1 = v + d + to;
        List<int[]> out = new ArrayList<>();
        out.add(new int[] {0, 1, 3, 2, u + d, row0, u + d + w, row1});
        out.add(new int[] {5, 4, 6, 7, u + d + w + d, row0, u + 2 * d + 2 * w, row1});
        out.add(new int[] {1, 5, 7, 3, u + d + w, row0, u + d + w + d, row1});
        out.add(new int[] {4, 0, 2, 6, u, row0, u + d, row1});
        if (from == 0) {
            out.add(new int[] {4, 5, 1, 0, u + d, v, u + d + w, v + d});
        }
        if (to == part.h()) {
            out.add(new int[] {2, 3, 7, 6, u + d + w, v, u + d + w + w, v + d});
        }
        return out.toArray(new int[0][]);
    }
}
