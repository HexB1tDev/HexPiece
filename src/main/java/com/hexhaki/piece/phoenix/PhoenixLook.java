package com.hexhaki.piece.phoenix;

/**
 * 3.6.7 (the owner: "Mythical Fruit: Tori Tori no Mi, Model: Phoenix ... I made the model and sent it in the zip"): how the owner's
 * phoenix is coloured, exactly as their file colours it (tools/phoenix_bake.py read it): every material is unlit, its colour its base
 * colour (or its fire texture: the model's atlas, assets/hexhaki/textures/piece/phoenix/marco.png) times a colour ramp of how much the
 * surface faces one fixed light ({@link #LIGHT}: dot(normal, light) * 0.5 + 0.5, eased between the ramp's stops). The file has two ramps:
 * the cool one (plumage, bill, eyes: the model's part kind 0) and the warm one (the tail, mane, wings and fire: kind 2). Pure: the game's
 * renderer, the previews and the tests share it.
 */
public final class PhoenixLook {
    private PhoenixLook() {
    }

    /** The file's light (the model's space: +Y up, the head toward -Z), unit. */
    public static final float[] LIGHT = {0.4294F, 0.7091F, -0.5593F};
    /** The model's parts' kinds: the cool ramp, the warm one (flame: it ripples). */
    public static final int COOL = 0;
    public static final int WARM = 2;
    /** The ramps' stops: position, then r, g, b (linear, as the file's; past 1 a touch brighter than the base). */
    static final float[][] COOL_RAMP = {{0.0F, 0.39F, 0.52F, 0.61F}, {0.26F, 0.48F, 0.62F, 0.69F}, {0.44F, 0.63F, 0.78F, 0.84F},
        {0.56F, 0.86F, 0.96F, 0.98F}, {0.76F, 0.99F, 1.03F, 1.025F}, {1.0F, 1.045F, 1.055F, 1.045F}};
    static final float[][] WARM_RAMP = {{0.0F, 0.48F, 0.66F, 0.74F}, {0.26F, 0.54F, 0.73F, 0.81F}, {0.45F, 0.74F, 0.9F, 0.95F},
        {0.57F, 0.93F, 0.99F, 1.0F}, {0.81F, 1.02F, 1.045F, 1.048F}, {1.0F, 1.045F, 1.065F, 1.065F}};

    /**
     * The tint of a vertex of a part of {@code kind} whose normal (world or model space, the same as {@code light}'s) is (nx, ny, nz):
     * into out (r, g, b, 0 to 1: the texture's colour is multiplied by it). The ramp's linear values are taken to the screen's sRGB
     * as the file's render does, so the base colours keep their brightness.
     */
    public static void tint(int kind, float nx, float ny, float nz, float[] light, float[] out) {
        float d = nx * light[0] + ny * light[1] + nz * light[2];
        float f = d * 0.5F + 0.5F;
        float[][] ramp = kind == WARM ? WARM_RAMP : COOL_RAMP;
        int i = 1;
        while (i < ramp.length - 1 && f > ramp[i][0]) {
            i++;
        }
        float[] a = ramp[i - 1];
        float[] b = ramp[i];
        float u = Math.max(0.0F, Math.min(1.0F, (f - a[0]) / Math.max(1.0E-6F, b[0] - a[0])));
        // the file's "ease" interpolation
        u = u * u * (3.0F - 2.0F * u);
        for (int k = 0; k < 3; k++) {
            float lin = a[k + 1] + (b[k + 1] - a[k + 1]) * u;
            out[k] = Math.min(1.0F, srgb(lin));
        }
    }

    /** Linear to sRGB (a multiplier on an sRGB texture is applied to its linear light, near enough for a tint). */
    static float srgb(float c) {
        c = Math.max(0.0F, c);
        return c <= 0.0031308F ? c * 12.92F : (float) (1.055 * Math.pow(c, 1 / 2.4) - 0.055);
    }

    /** Blocks per model unit: the owner's "Like the GIF" (the wingspan about 7 blocks: the file's is 17.6 units). */
    public static final float SCALE = 0.4F;
}
