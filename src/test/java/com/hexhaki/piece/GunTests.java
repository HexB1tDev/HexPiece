package com.hexhaki.piece;

import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunAnims;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.sword.Katana;
import java.io.File;

/**
 * 3.8.8: the guns (the owner's category Gun: "all weapons only shoot 1 bullet at a time, and auto reloads after ... when haki is on,
 * bullets come out black (pure black), those bullets are much much faster and go through blocks (10 blocks max)"): the rules, the
 * clips' names and their files, the timings the server and the clips share.
 */
public final class GunTests {
    private GunTests() {
    }

    public static void main(String[] args) {
        rules();
        anims();
        clips();
        Check.done("GunTests");
    }

    static void rules() {
        Check.that(GunRules.speed(true) >= 2.5 * GunRules.speed(false), "a Haki ball is much, much faster");
        Check.that(GunRules.drop(true, 30) == 0.0 && GunRules.drop(false, 30) > 0.0, "a Haki ball flies straight, a plain one falls");
        Check.that(GunRules.drop(false, 10) < 0.7, "a plain ball falls little over its first 32 blocks");
        Check.that(GunRules.life(false) * GunRules.SPEED >= GunRules.RANGE && GunRules.life(true) * GunRules.HAKI_SPEED >= GunRules.RANGE,
            "both reach the range");
        Check.that(GunRules.PIERCE == 10, "the owner's 10 blocks at most");
        int through = 0;
        while (GunRules.through(true, through)) {
            through++;
        }
        Check.that(through == 10, "a Haki ball goes through 10 blocks and the 11th stops it");
        Check.that(!GunRules.through(false, 0), "a plain ball goes through none");
        Check.that(GunRules.damage(true) > GunRules.damage(false), "a Haki ball hits harder");
        Check.that(!GunRules.ready(100, GunRules.loadedAt(100)), "one ball at a time: empty once fired");
        Check.that(GunRules.ready(GunRules.loadedAt(100), GunRules.loadedAt(100)), "loaded again when the reload ends");
        Check.that(GunRules.loadedAt(0) == GunAnims.FIRE_TICKS + GunRules.RELOAD, "the fire clip and the reload make up the wait");
        Check.that(Math.abs(GunRules.at(5.0, 2.0, 3.0, false, true) - (11.0 - GunRules.drop(false, 3.0))) < 1e-9, "a ball's height falls");
        Check.that(GunRules.at(5.0, 2.0, 3.0, true, true) == 11.0, "a Haki ball's does not");
        Check.that(!GunRules.spins(GunRules.SPIN_AFTER - 1) && GunRules.spins(GunRules.SPIN_AFTER)
            && GunRules.spins(GunRules.SPIN_AFTER + GunRules.SPIN_EVERY) && !GunRules.spins(GunRules.SPIN_AFTER + 1), "the spin now and then");
    }

    static void anims() {
        Check.that(GunAnims.bucket(-90.0F) == 0 && GunAnims.bucket(90.0F) == GunAnims.BUCKETS - 1, "the look's pitch clamped to the clips");
        for (int b = 0; b < GunAnims.BUCKETS; b++) {
            Check.that(GunAnims.bucket(GunAnims.pitchOf(b)) == b, "each clip's own pitch is its bucket");
            Check.that(GunAnims.bucket(GunAnims.pitchOf(b) + GunAnims.STEP / 2.0F - 0.01F) == b, "half a step on is still the bucket");
        }
        Check.that(GunAnims.bucket(0.0F) == 3, "a level look is the middle clip");
        Check.that(GunAnims.firing(GunAnims.FIRE) && GunAnims.firing(GunAnims.FIRE + GunAnims.BUCKETS - 1)
            && !GunAnims.firing(GunAnims.RELOAD) && !GunAnims.firing(GunAnims.HOLD), "the fire codes");
        Check.that(GunAnims.name(GunAnims.HOLD, false, false, false).equals(GunAnims.IDLE), "still: the idle");
        Check.that(GunAnims.name(GunAnims.HOLD, true, false, false).equals(GunAnims.WALK), "walking");
        Check.that(GunAnims.name(GunAnims.HOLD, true, true, false).equals(GunAnims.RUN), "sprinting");
        Check.that(GunAnims.name(GunAnims.HOLD, false, false, true).equals(GunAnims.SPIN), "the spin");
        Check.that(GunAnims.name(GunAnims.NONE, false, false, false) == null, "no gun, no clip");
        Check.that(GunAnims.ballInHand(GunAnims.RELOAD, GunAnims.BALL_FROM) && !GunAnims.ballInHand(GunAnims.RELOAD, GunAnims.BALL_AT),
            "the ball in the hand until it reaches the muzzle");
        Check.that(GunAnims.ballInMuzzle(GunAnims.RELOAD, GunAnims.BALL_AT) == 0.0F
            && GunAnims.ballInMuzzle(GunAnims.RELOAD, GunAnims.BALL_HOME) < 0.0F, "then into the muzzle, and gone");
        Check.that(GunAnims.BALL_HOME < GunRules.RELOAD, "the ball home before the reload ends");
        for (Gun gun : Gun.values()) {
            Check.that(gun.category() == Katana.Category.GUN && gun.kindLine().startsWith("Gun"), "every gun is a Gun, its tooltip says so");
            Check.that(Gun.byOrdinal(gun.ordinal()) == gun, "by ordinal");
        }
        Check.that(Gun.byOrdinal(-1) == null && Gun.byOrdinal(Gun.values().length) == null, "no gun past the list");
    }

    /** Every clip a gun can play is in the jar's animations (tools/gun_anims.py), with the length the server waits for. */
    static void clips() {
        File folder = new File("src/main/resources/assets/hexhaki/player_animation");
        if (!folder.isDirectory()) {
            return; // run from elsewhere: the names are checked above
        }
        String[] names = {GunAnims.IDLE, GunAnims.WALK, GunAnims.RUN, GunAnims.SPIN, GunAnims.SPIN_DRAW, GunAnims.RELOAD_CLIP};
        for (String name : names) {
            Check.that(new File(folder, name + ".json").isFile(), "the clip " + name);
        }
        for (int b = 0; b < GunAnims.BUCKETS; b++) {
            Check.that(new File(folder, GunAnims.fireClip(b) + ".json").isFile(), "the fire clip " + b);
        }
        Check.that(length(new File(folder, GunAnims.RELOAD_CLIP + ".json")) == GunRules.RELOAD, "the reload clip is the reload's length");
        Check.that(length(new File(folder, GunAnims.fireClip(3) + ".json")) == GunAnims.FIRE_TICKS, "a fire clip is the shot's length");
        Check.that(length(new File(folder, GunAnims.SPIN_DRAW + ".json")) == GunAnims.DRAW_TICKS, "the draw clip is the draw's length");
        Check.that(length(new File(folder, GunAnims.SPIN + ".json")) == GunAnims.SPIN_TICKS, "the spin clip is the spin's length");
    }

    static int length(File file) {
        try {
            String text = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"endTick\":\\s*(\\d+)").matcher(text);
            return m.find() ? Integer.parseInt(m.group(1)) : -1;
        } catch (java.io.IOException failure) {
            return -1;
        }
    }
}
