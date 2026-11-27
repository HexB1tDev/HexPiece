package com.hexhaki.piece.gun;

import com.hexhaki.piece.sword.Katana;

/**
 * 3.8.8: the guns (the owner's category Gun: "Pistols, usually held with one hand (right) in a badass pose, all weapons only shoot 1
 * bullet at a time, and auto reloads after, bullets are also the same model from now one"). Each is one of the owner's models, baked by
 * tools/gun_bake.py into art/gun and packed by build.py ({@code assets/hexhaki/gun/<key>.hkm}); every gun fires the same bullet, the
 * owner's lead ball ({@link #BULLET}). Every gun the owner gives from now on is one more constant here, played by the same animations
 * (the owner: "these animations will serve for every gun from now on").
 */
public enum Gun {
    FLINTLOCK("flintlock", "Flintlock Pistol",
        "A brass-mouthed flintlock on a chestnut stock, gilt from the lock to the guard. One lead ball, pressed home by hand, and a crack that "
            + "carries across the bay.");

    /** The bullet's mesh: the owner's cast lead ball, every gun's. */
    public static final String BULLET = "lead_ball";
    /** Blocks per model px of a gun in the hand (the owner's flintlock, 16.7 px long, is about 0.7 of a block). */
    public static final float SCALE = 0.7F / 16.0F;
    /** Blocks per model px of a flying bullet (drawn well over its true size, or a ball the size of a pea is never seen). */
    public static final float BULLET_SCALE = 2.6F / 16.0F;

    private final String key;
    private final String title;
    private final String description;

    Gun(String key, String title, String description) {
        this.key = key;
        this.title = title;
        this.description = description;
    }

    public String key() {
        return this.key;
    }

    public String title() {
        return this.title;
    }

    public String description() {
        return this.description;
    }

    /** Its weapon category (sword/Katana.Category): every gun is a Gun. */
    public Katana.Category category() {
        return Katana.Category.GUN;
    }

    /** The tooltip's line under the name: the category first, as every weapon's. */
    public String kindLine() {
        return category().label + "  ·  one ball, then a reload  ·  right hand";
    }

    /** The gun of an ordinal ({@link #ordinal()}), or null. */
    public static Gun byOrdinal(int ordinal) {
        Gun[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }
}
