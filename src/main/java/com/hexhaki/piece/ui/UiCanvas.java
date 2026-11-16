package com.hexhaki.piece.ui;

/**
 * What a menu draws on, without Minecraft types (ported from HexGhoul): the game hands its GUI through {@code GuiCanvas}, the preview
 * tool (tools/preview/UiPreview.java) an image, so the same drawing code makes both and every menu can be judged offline.
 */
public interface UiCanvas {
    void fill(int x0, int y0, int x1, int y1, int argb);

    void gradient(int x0, int y0, int x1, int y1, int top, int bottom);

    void text(String text, int x, int y, int argb, boolean shadow);

    default void centered(String text, int centerX, int y, int argb, boolean shadow) {
        this.text(text, centerX - this.width(text) / 2, y, argb, shadow);
    }

    int width(String text);

    /** The viewing player's face from their skin, {@code size} square (a plain square where there is none). */
    void face(int x, int y, int size);

    void push();

    void translate(float x, float y);

    void scale(float factor);

    void pop();

    /** A texture ("namespace:path/to.png") drawn into (x, y, w, h) from its own (u, v, uw, vh) of a texW x texH sheet. */
    default void texture(String id, int x, int y, int w, int h, int u, int v, int uw, int vh, int texW, int texH) {
    }

    /** Text scaled by {@code factor} with its top-left at (x, y). */
    default void bigText(String text, int x, int y, int argb, boolean shadow, float factor) {
        this.push();
        this.translate(x, y);
        this.scale(factor);
        this.text(text, 0, 0, argb, shadow);
        this.pop();
    }

    default void bigCentered(String text, int centerX, int y, int argb, boolean shadow, float factor) {
        this.bigText(text, Math.round(centerX - this.width(text) * factor / 2.0F), y, argb, shadow, factor);
    }

    /** Fills a convex polygon one pixel row at a time (GUI fills are axis-aligned rectangles). */
    default void polygon(float[] xs, float[] ys, int argb) {
        float top = Float.MAX_VALUE;
        float bottom = -Float.MAX_VALUE;
        for (float y : ys) {
            top = Math.min(top, y);
            bottom = Math.max(bottom, y);
        }
        int n = xs.length;
        for (int row = (int) Math.floor(top); row < (int) Math.ceil(bottom); row++) {
            float centre = row + 0.5F;
            float left = Float.MAX_VALUE;
            float right = -Float.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                if (ys[i] <= centre && ys[j] > centre || ys[j] <= centre && ys[i] > centre) {
                    float x = xs[i] + (centre - ys[i]) / (ys[j] - ys[i]) * (xs[j] - xs[i]);
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
            int from = Math.round(left);
            int to = Math.round(right);
            if (to > from) {
                this.fill(from, row, to, row + 1, argb);
            }
        }
    }

    /** {@code text} shortened with "..." until it fits {@code width} pixels. */
    default String fit(String text, int width) {
        if (this.width(text) <= width) {
            return text;
        }
        String shortened = text;
        while (!shortened.isEmpty() && this.width(shortened + "...") > width) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened.stripTrailing() + "...";
    }

    /** {@code text} broken into lines of at most {@code width} pixels; returns the y after the last line. */
    default int wrap(String text, int x, int y, int width, int argb, int lineHeight, int maxLines) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : words) {
            String tried = line.length() == 0 ? word : line + " " + word;
            if (this.width(tried) > width && line.length() > 0) {
                if (lines == maxLines - 1) {
                    this.text(this.fit(tried, width), x, y, argb, false);
                    return y + lineHeight;
                }
                this.text(line.toString(), x, y, argb, false);
                y += lineHeight;
                lines++;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(tried);
            }
        }
        if (line.length() > 0) {
            this.text(line.toString(), x, y, argb, false);
            y += lineHeight;
        }
        return y;
    }
}
