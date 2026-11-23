import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.gear.NikaDawn;
import com.hexhaki.piece.gear.NikaGoof;
import com.hexhaki.piece.gear.NikaSkin;
import com.hexhaki.piece.gear.NikaBend;
import com.hexhaki.piece.gear.NikaHold;
import com.hexhaki.piece.gear.RubberJump;
import com.hexhaki.piece.gear.RubberWave;

/**
 * 3.2.6: the Nika fruit's drawn shapes (gear/NikaArt) for FxPreview, round the scene's middle (the ground at 0, a body standing at the
 * middle facing +z). Shots after the camera: {@code |nfist:radius[:way]} the Bajrang Gun's giant fist over a body for scale (way: "down"
 * knuckles to the ground, "ahead" toward +z, "up" over its head).
 *
 * <p>3.2.8: {@code |ngrab:yaw:age[:arm]} the Gum-Gum Giant (the player model's own boxes, placed and scaled as PlayerRenderer and
 * GearBodyDeformer do, four times the size) turned {@code yaw} degrees, its right arm out at {@code arm} degrees of pitch (-90: the
 * hold), a body grabbed {@code age} ticks ago standing 6 blocks off at the start, flown into the fist as gear/NikaHold places it.
 *
 * <p>3.2.9: {@code |nbend:t[:strength]} a body Star Gun struck {@code t} ticks ago (gear/NikaBend: held 26, let go over 17, as the
 * original's starHit sends), the blow toward +x; a faint outline of the body at rest beside it.
 *
 * <p>3.3.0: {@code |nworld:t[:radius[:end]]} Rubber World's ground {@code t} ticks after the release (gear/RubberWave), the field's
 * middle at the scene's middle: each grass block whose ground rises drawn lifted and tilted with the wave (as client/gear/RubberWorldDraw
 * draws the world's own blocks), within 22 blocks; two bodies standing in it ride the waves.
 *
 * <p>3.3.2: {@code |nhop:t[:land]} Gear 5's jump {@code t} ticks after the take-off (gear/RubberJump.hop; {@code land} 1: after the
 * landing): the ground springing up and rippling round the middle, the jumper (rising as the lifted jump does, or standing there when
 * landed) and a bystander 2.6 blocks off riding the ripple.
 *
 * <p>3.3.2: {@code |nchaos:t[:radius]} Rubber World on everyone else {@code t} ticks after the release: the ground as {@code |nworld},
 * the user standing untouched at the middle, and eight bodies run tick by tick as server/RubberChaos throws them (gear/RubberWave.toss
 * where a crest rises under one standing on the real, flat ground; the game's gravity and drag; a fixed seed for the sideways throw).
 */
public final class NikaShots {
    private NikaShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        switch (move[0]) {
            case "nfist" -> {
                double r = move.length > 1 ? Double.parseDouble(move[1]) : 1.5;
                String way = move.length > 2 ? move[2] : "ahead";
                HieShots.body(sink, -ex, -ey, -ez);
                NikaArt.tint = 0xE7B48F; // a skin tone (the game uses the player's own skin texel)
                NikaArt.light(-0.33, 0.79, -0.51);
                switch (way) {
                    case "down" -> NikaArt.fist(sink, -ex, 2.6 + r - ey, 1.0 - ez, 0, -1, 0, 0, 0, -1, r, true);
                    case "up" -> NikaArt.fist(sink, -0.3 - ex, 2.4 + r - ey, -ez, 0, 1, 0, 0, 0, -1, r, true);
                    default -> NikaArt.fist(sink, -ex, 1.4 - ey, 1.2 + r - ez, 0, 0, 1, 0, 1, 0, r, true);
                }
                NikaArt.tint = 0xFFFFFF;
            }
            case "npow" -> {
                // 3.3.9: Dawn Pistol landing: |npow:age[:size] (the struck body 5 ahead, its POW, the thrower at the middle)
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 2;
                double size = move.length > 2 ? Double.parseDouble(move[2]) : 1.9;
                double back = 0.9 * Math.max(0, age);
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, 5.0 + back - ez);
                NikaArt.pow(sink, -ex, 0.95 - ey, 5.0 + back - ez, 0, 0, 1, age, size, 7);
            }
            case "nskin" -> {
                // 3.4.0: Dawn Pistol through the body, |nskin:t (t ticks after the hit; the body 4.5 ahead, the blow along +z): the
                // body's skin stretched out of its far side to the fist (here the shirt's red where the game uses the body's own texture),
                // the heat over it, the POW where the fist went in; launched at NikaSkin.PIN
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                double flown = 0;
                double v = 3.6;
                for (int k = NikaSkin.PIN; k < (int) t; k++) {
                    flown += v;
                }
                if (t > NikaSkin.PIN) {
                    flown += v * Math.min(1, t - Math.max(NikaSkin.PIN, (int) t));
                }
                double bz = 4.5 + flown;
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, bz - ez);
                double len = NikaSkin.LENGTH * NikaSkin.out(t);
                double glow = Math.min(1.0, NikaSkin.out(t) * 1.2);
                com.hexhaki.piece.horo.AstralArt.Skin flat = (xyz, uv, argb, nx, ny, nz) -> {
                    double lit = nx * -0.33 + ny * 0.79 + nz * -0.51;
                    double band = lit > 0.5 ? 1.0 : lit > 0.1 ? 0.86 : lit > -0.3 ? 0.72 : 0.6;
                    int c = FireMesh.argb(1.0, FireMesh.mix(0, 0xB03A2E, band));
                    sink.quad(xyz, new int[] {c, c, c, c});
                };
                NikaSkin.skin(flat, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, 0, 1, 0, 1, 0xFFFFFFFF, t);
                NikaSkin.heat(sink, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, glow, t);
                NikaArt.pow(sink, -ex, 0.95 - ey, 4.5 - ez, 0, 0, 1, t, 1.9, 7);
            }
            case "ndawn" -> {
                // 3.4.0: Dawn Pistol over time, |ndawn:t (t ticks after the arm leaves; this scene's arm lands on tick 4): the Haki-black
                // arm and ballooned fist reaching into the body, the POW, its skin stretched out through it to the white-hot fist (the
                // shirt's red here; the body's own texture in game), held while pinned, then snapping back as it is launched (3.6 a tick)
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 5;
                double land = 4.0;
                double tl = t - land;
                double bz0 = 4.6;
                // 3.4.1: fast out; then through the body to the fist at the skin's end while it stretches; gone as the body is launched
                double reach = t < land ? 0.6 + (bz0 - 0.6) * (1 - Math.pow(1 - t / land, 3))
                    : tl < NikaSkin.PIN ? bz0 + Math.max(0, NikaSkin.LENGTH * NikaSkin.out(tl) - NikaSkin.BULB) : 0;
                double flown = tl > NikaSkin.PIN ? 5.0 * (tl - NikaSkin.PIN) : 0;
                double bz = bz0 + flown;
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, bz - ez);
                int skin = 0xE7B48F;
                int coat = FireMesh.mix(0, skin, 0.12);
                double sx = -0.37;
                double sy = 1.3;
                if (reach > 0.3) {
                    // 3.4.9: one straight arm from the shoulder to the fist, on through the body while the skin stretches
                    // (client/gear/NikaPistolArm)
                    FireMesh.tube(sink, sx - ex, sy - ey, -ez, -ex, 1.15 - ey, reach - ez, 0.13, 0.13, coat, coat, coat, 0.0, 1.0, 0.0, 0.0, 1);
                    double dx = -sx;
                    double dy = 1.15 - sy;
                    double dz = reach;
                    double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    NikaArt.tint = mulRgb(skin, 0x3A3248);
                    NikaArt.light(-0.33, 0.79, -0.51);
                    double r = 0.125 * 3.0 * (0.4 + 0.6 * Math.min(1.0, dl / 2.5));
                    NikaArt.fist(sink, dx / dl * (dl + r) + sx - ex, sy + dy / dl * (dl + r) - ey, dz / dl * (dl + r) - ez, dx, dy, dz, 0, 1, 0, r,
                        true, true);
                    NikaArt.tint = 0xFFFFFF;
                }
                if (tl >= 0) {
                    double len = NikaSkin.LENGTH * NikaSkin.out(tl);
                    com.hexhaki.piece.horo.AstralArt.Skin flat = (xyz, uv, argb, nx, ny, nz) -> {
                        double lit = nx * -0.33 + ny * 0.79 + nz * -0.51;
                        double band = lit > 0.5 ? 1.0 : lit > 0.1 ? 0.86 : lit > -0.3 ? 0.72 : 0.6;
                        int c = FireMesh.argb(1.0, FireMesh.mix(0, 0xB03A2E, band));
                        sink.quad(xyz, new int[] {c, c, c, c});
                    };
                    NikaSkin.skin(flat, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, 0, 1, 0, 1, 0xFFFFFFFF, tl);
                    NikaSkin.heat(sink, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, Math.min(1.0, NikaSkin.out(tl) * 1.2), tl);
                    NikaArt.pow(sink, -ex, 0.95 - ey, bz0 - ez, 0, 0, 1, tl, 1.9, 7);
                }
            }
            case "ndland" -> {
                // 3.4.1: where a Dawn Pistol's body comes down: |ndland:age (the crater's dust explosion: the Gura fruit's quake, 3.2)
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                HieShots.body(sink, 0.8 - ex, -ey, -ez);
                com.hexhaki.piece.gura.GuraArt.quake(sink, -ex, -ey, -ez, 3.2, 1.0, age, 7);
            }
            case "npistol" -> {
                // 3.3.9: the Dawn Pistol's arm at its reach: |npistol:reach[:pow age] (the arm Haki-black as the original draws it, the
                // ballooned fist round its end as client/gear/NikaPistolArm draws it, the struck body and the POW when it has landed)
                double reach = move.length > 1 ? Double.parseDouble(move[1]) : 4.5;
                double age = move.length > 2 ? Double.parseDouble(move[2]) : -1;
                HieShots.body(sink, -ex, -ey, -ez);
                int skin = 0xE7B48F;
                int coat = FireMesh.mix(0, skin, 0.12); // the original's dawn arm: the skin times 0.11, 0.11, 0.14
                double sx = -0.37;
                double sy = 1.3;
                double tx = 0.0;
                double ty = 1.15;
                double tz = reach;
                FireMesh.tube(sink, sx - ex, sy - ey, -ez, tx - ex, ty - ey, tz - ez, 0.13, 0.13, coat, coat, coat, 0.0, 1.0, 0.0, 0.0, 1);
                double grow = 0.4 + 0.6 * Math.min(1.0, reach / 2.5);
                double r = 0.125 * (move.length > 3 ? Double.parseDouble(move[3]) : 3.0) * grow;
                double dx = tx - sx;
                double dy = ty - sy;
                double dz = tz;
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                NikaArt.tint = mulRgb(skin, 0x3A3248);
                NikaArt.light(-0.33, 0.79, -0.51);
                // the thrower's right arm (x -0.37: facing +z, the right is -x): a right hand, its thumb on the inside
                NikaArt.fist(sink, tx + dx / dl * r - ex, ty + dy / dl * r - ey, tz + dz / dl * r - ez, dx, dy, dz, 0, 1, 0, r, true, true);
                NikaArt.tint = 0xFFFFFF;
                if (age >= 0) {
                    HieShots.body(sink, -ex, -ey, tz + 2 * r + 0.3 - ez);
                    NikaArt.pow(sink, -ex, 0.95 - ey, tz + 2 * r + 0.3 - ez, 0, 0, 1, age, 1.9, 7);
                }
            }
            case "ngrab" -> {
                double yaw = move.length > 1 ? Double.parseDouble(move[1]) : 0;
                double age = move.length > 2 ? Double.parseDouble(move[2]) : 20;
                double arm = move.length > 3 ? Double.parseDouble(move[3]) : -90;
                double scale = 4.0;
                giant(sink, -ex, -ey, -ez, yaw, scale, arm);
                double[] fist = new double[3];
                NikaHold.fist(0, 0, 0, yaw, scale, scale, fist);
                double k = NikaHold.reach(age);
                double fromX = 2.5;
                double fromZ = 6.0;
                double bx = fromX + (fist[0] - fromX) * k;
                double by = (fist[1] - 0.95) * k;
                double bz = fromZ + (fist[2] - fromZ) * k;
                HieShots.body(sink, bx - ex, by - ey, bz - ez);
            }
            case "nbend" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 0;
                double strength = move.length > 2 ? Double.parseDouble(move[2]) : 1;
                double[] shape = new double[5];
                NikaBend.shape(t, 26, 17, strength, shape);
                bent(sink, -ex, -ey, -ez, Math.PI / 2, shape);
                double[] rest = {1, 1, 1, 0, 0};
                bent(sink, -ex, -ey, -ez - 2.2, Math.PI / 2, rest);
            }
            case "nworld" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 20;
                double radius = move.length > 2 ? Double.parseDouble(move[2]) : 40;
                double end = move.length > 3 ? Double.parseDouble(move[3]) : Double.POSITIVE_INFINITY;
                for (int x = -22; x < 22; x++) {
                    for (int z = -22; z < 22; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        if (r > 22) {
                            continue;
                        }
                        double angle = Math.atan2(cz, cx);
                        double h = RubberWave.rise(r, angle, t, radius, end);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberWave.slope(r, angle, t, radius, end));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                for (double[] b : new double[][] {{5.5, 3.5}, {-3.5, 9.5}}) {
                    double r = Math.hypot(b[0], b[1]);
                    double h = RubberWave.rise(r, Math.atan2(b[1], b[0]), t, radius, end);
                    HieShots.body(sink, b[0] - ex, h - ey, b[1] - ez);
                }
            }
            case "nhop" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 1;
                boolean landing = move.length > 2 && move[2].equals("1");
                // 3.4.9: |nhop:t:land:size a body size times the normal one (the giant: 4), its ripples as gear/RubberJump makes them
                double size = move.length > 3 ? Double.parseDouble(move[3]) : 1.0;
                int span = (int) Math.ceil(7 * size);
                for (int x = -span; x < span; x++) {
                    for (int z = -span; z < span; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        double angle = Math.atan2(cz, cx);
                        double h = RubberJump.hop(r, angle, t, landing, size);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberJump.slope(r, angle, t, landing, size));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                // the jumper: up as the lifted jump goes (0.9 a tick, the game's gravity and drag), or standing after the landing
                double y = 0;
                if (!landing) {
                    double v = RubberJump.lifted(0.42);
                    for (int k = 0; k < (int) t; k++) {
                        y += v;
                        v = (v - 0.08) * 0.98;
                    }
                    y += v * (t - (int) t);
                }
                if (size > 1.0) {
                    giant(sink, -ex, y - ey, -ez, 0, size, 0);
                } else {
                    HieShots.body(sink, -ex, y - ey, -ez);
                }
                double bx = 2.6 * size;
                double bz = 0.9 * size;
                double bh = RubberJump.hop(Math.hypot(bx, bz), Math.atan2(bz, bx), t, landing, size);
                HieShots.body(sink, bx - ex, bh - ey, bz - ez);
            }
            // 3.4.4: Goof Ball. |ngoof:age[:slim] the ball (age ticks after it formed) at the middle with the user's limbs punching out
            // of it in art/user_skin.png, a bystander 2.4 off; |ngoofpop:age the ball bursting; |ndizzy:age the stars over a dizzy body;
            // |ngoofp:age:pitch one's own first person in the ball (with the camera fp:pitch)
            case "ngoof" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 8;
                boolean slim = move.length > 2 && move[2].equals("slim");
                HieShots.body(sink, 2.4 - ex, -ey, 0.6 - ez);
                com.hexhaki.piece.gear.NikaGoof.ball(sink, -ex, NikaGoof.MIDDLE - ey, -ez, age, 11);
                NikaGoof.limbs(skin(sink), -ex, NikaGoof.MIDDLE - ey, -ez, age, 11, slim);
            }
            case "ngoofpop" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 2;
                HieShots.body(sink, -ex, -ey, -ez);
                NikaGoof.pop(sink, -ex, NikaGoof.MIDDLE - ey, -ez, age, 11);
                NikaGoof.dizzy(sink, -ex, 1.95 - ey, -ez, age, NikaGoof.DIZZY, 11);
            }
            case "ndizzy" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 20;
                HieShots.body(sink, -ex, -ey, -ez);
                NikaGoof.dizzy(sink, -ex, 1.95 - ey, -ez, age, NikaGoof.DIZZY, 11);
            }
            case "ngoofp" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 8;
                double pr = Math.toRadians(move.length > 2 ? Double.parseDouble(move[2]) : 10);
                double fy = -Math.sin(pr);
                double fz = Math.cos(pr);
                NikaGoof.firstPerson(sink, skin(sink), age, 11, false, 0, fy, fz, 0, fz, -fy, -1, 0, 0);
            }
            // 3.4.4: Dawn Whip, the user at the middle facing +z (a stand-in body; tools/anim_preview.py shows the clip): |ncoil:age the
            // wind-up; |nsmear:age (ticks after the wind-up) the release's smear with a body struck at the leg's reach ahead; |ncrack:age
            // the crack on a body thrown to the left; |nwhirl:age[:drill] the overspin; |ndrill:age[:torn] the drill; |ndpop:age the pop;
            // |nwhipfp:age:pitch one's own first person through the spin (with fp:pitch)
            case "ncoil" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 8;
                HieShots.body(sink, -ex, -ey, -ez);
                com.hexhaki.piece.gear.NikaDawn.coil(sink, -ex, -ey, -ez, age, 5);
            }
            case "nsmear" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 15;
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, 7.0 - ez);
                NikaDawn.smear(sink, new com.hexhaki.piece.style.StyleShow.Place(-ex, -ey, -ez, 0.0), age, 5);
            }
            // 3.4.6 (the owner: "I don't see the leg in your render ... I needed to see an accurate render"): |ndwhip:t the whole of Dawn
            // Whip t ticks in: the user (art/user_skin.png; the game uses the Gear 5 skin) posed by gear5_dawn_whip as client/npc/StylePoser
            // samples it, the striking leg stretched out Haki-coated (gear/NikaDawn.leg, the very quads the game draws), the smear, the
            // coil or the overspin's arcs, two bodies 7 and 5 blocks off (struck and launched at the impact) and their cracks
            // 3.4.9: |nsmoke:seed[:scale[:clip[:tick]]] Gear 5's smoke aura (gear/NikaSmoke, the very spawn rule client/gear/NikaSmokeFx
            // uses, run for 40 ticks as PieceFx's Mote moves them: drag, wander, growing, fading) round the user posed by a clip
            case "nsmoke" -> {
                long seed = move.length > 1 ? Long.parseLong(move[1]) : 1;
                double scale = move.length > 2 ? Double.parseDouble(move[2]) : 1.0;
                String clip = move.length > 3 ? move[3] : "gear5_stance";
                float tick = move.length > 4 ? Float.parseFloat(move[4]) : 10.0F;
                float[][] pose = com.hexhaki.piece.client.npc.StylePoser.sample(clip, tick);
                com.hexhaki.piece.style.StyleShow.Place place = new com.hexhaki.piece.style.StyleShow.Place(-ex, -ey, -ez, 0.0);
                if (scale <= 1.0) {
                    com.hexhaki.piece.horo.AstralArt.left(skin(sink), pose, place);
                } else {
                    giant(sink, -ex, -ey, -ez, 0, scale, 0);
                }
                smoke(sink, -ex, -ey, -ez, scale, seed);
            }
            case "ndwhip" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 16;
                // |ndwhip:t:fp: one's own first person (with the camera fp:pitch): no body of one's own, no coil or arcs round the eye
                boolean fp = move.length > 2 && move[2].equals("fp");
                float[][] pose = com.hexhaki.piece.client.npc.StylePoser.sample("gear5_dawn_whip", (float) t);
                com.hexhaki.piece.style.StyleShow.Place place = new com.hexhaki.piece.style.StyleShow.Place(-ex, -ey, -ez, 0.0);
                if (!fp) {
                    com.hexhaki.piece.horo.AstralArt.left(skin(sink), pose, place);
                }
                NikaDawn.leg(skin(sink), place, t, false, NikaDawn.COAT);
                NikaDawn.legGlow(sink, place, t, NikaDawn.LEG_GLOW);
                NikaDawn.smear(sink, place, t, 5);
                if (fp) {
                    NikaDawn.firstPerson(sink, t - NikaDawn.WIND, 5, 0, -Math.sin(Math.toRadians(20)), Math.cos(Math.toRadians(20)), 0,
                        Math.cos(Math.toRadians(20)), Math.sin(Math.toRadians(20)), -1, 0, 0);
                } else if (t <= NikaDawn.WIND + 1) {
                    NikaDawn.coil(sink, -ex, -ey, -ez, t, 5);
                }
                if (!fp && t > NikaDawn.IMPACT && t < NikaDawn.DRILL) {
                    NikaDawn.whirl(sink, -ex, -ey, -ez, t - NikaDawn.IMPACT, Math.max(0, Math.min(1, (t - NikaDawn.OVERSPIN) / 4.0)), 5);
                }
                // the struck: 7 ahead and 5 to the left-back; thrown sideways the way the leg sweeps from the impact on
                double[][] bodies = {{0, 7}, {-4, -3}};
                for (double[] b : bodies) {
                    double r = Math.hypot(b[0], b[1]);
                    double rx = b[0] / r;
                    double rz = b[1] / r;
                    double cx = rz + rx * 0.35;
                    double cz = -rx + rz * 0.35;
                    double cl = Math.hypot(cx, cz);
                    double flown = t > NikaDawn.IMPACT ? Math.min(30, (t - NikaDawn.IMPACT) * 3.4) : 0;
                    double bx = b[0] + cx / cl * flown;
                    double bz = b[1] + cz / cl * flown;
                    HieShots.body(sink, bx - ex, -ey, bz - ez);
                    if (t >= NikaDawn.IMPACT) {
                        NikaDawn.crack(sink, b[0] - ex, 0.95 - ey, b[1] - ez, cx / cl, cz / cl, t - NikaDawn.IMPACT, 1.8, 5);
                    }
                }
            }
            case "ncrack" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 2;
                double flown = Math.max(0, age) * 1.6;
                HieShots.body(sink, flown - ex, -ey, 4.0 - ez);
                NikaDawn.crack(sink, -ex + 0.2, 0.95 - ey, 4.0 - ez, 1, 0, age, 1.8, 5);
            }
            case "nwhirl" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 8;
                double drill = move.length > 2 ? Double.parseDouble(move[2]) : 0;
                HieShots.body(sink, -ex, -ey, -ez);
                NikaDawn.whirl(sink, -ex, -ey, -ez, age, drill, 5);
            }
            case "ndrill" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
                boolean torn = move.length < 3 || !move[2].equals("0");
                double sunk = torn ? Math.min(NikaDawn.DEPTH, age / NikaDawn.DRILL_STEP) : 0;
                HieShots.body(sink, -ex, -sunk - ey, -ez);
                NikaDawn.whirl(sink, -ex, -sunk - ey, -ez, age + 28, 1.0, 5);
                NikaDawn.drill(sink, -ex, -ey, -ez, age, torn, 5);
            }
            case "ndpop" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                double up = Math.max(0, 1.05 * age - 0.04 * age * age) - NikaDawn.DEPTH;
                HieShots.body(sink, -ex, up - ey, -ez);
                NikaDawn.pop(sink, -ex, -ey, -ez, age, 5);
            }
            case "nwhipfp" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
                double pr = Math.toRadians(move.length > 2 ? Double.parseDouble(move[2]) : 10);
                double fy = -Math.sin(pr);
                double fz = Math.cos(pr);
                NikaDawn.firstPerson(sink, age, 5, 0, fy, fz, 0, fz, -fy, -1, 0, 0);
            }
            // 3.4.7: the Giant's throw. |nthrow:age a body flown flat along +z from the giant's fist (age ticks after the throw, 3.5 a
            // tick) with its trail; |ngcrash:age:size the crash against a wall 6 ahead
            case "nthrow" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
                double z = 2.0 + 3.5 * Math.min(age, 4.0); // server/GiantThrow.SPEED
                HieShots.body(sink, -ex, 4.0 - ey, z - ez);
                com.hexhaki.piece.gear.NikaThrow.trail(sink, -ex, 4.95 - ey, z - ez, 0, 1, age, 1.9, 5);
            }
            case "ngcrash" -> {
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                double size = move.length > 2 ? Double.parseDouble(move[2]) : 1.9;
                HieShots.box(sink, -3.5 - ex, -ey, 7.0 - ez, 3.5 - ex, 4.0 - ey, 8.0 - ez, 0x8A8279);
                HieShots.body(sink, -ex, -ey, 6.4 - ez);
                com.hexhaki.piece.gear.NikaThrow.crash(sink, -ex, 0.95 - ey, 6.6 - ez, 0, 1, age, size, 5);
            }
            case "nchaos" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 30;
                double radius = move.length > 2 ? Double.parseDouble(move[2]) : 16;
                double inf = Double.POSITIVE_INFINITY;
                for (int x = -18; x < 18; x++) {
                    for (int z = -18; z < 18; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        double angle = Math.atan2(cz, cx);
                        double h = RubberWave.rise(r, angle, t, radius, inf);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberWave.slope(r, angle, t, radius, inf));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                HieShots.body(sink, -ex, -ey, -ez);
                java.util.Random random = new java.util.Random(7);
                for (int b = 0; b < 8; b++) {
                    double a0 = b * Math.PI / 4 + 0.3;
                    double r0 = 3.2 + 1.3 * (b % 3);
                    double[] q = {Math.cos(a0) * r0, 0, Math.sin(a0) * r0};
                    double[] v = {0, 0, 0};
                    long next = 0;
                    for (int k = 1; k <= (int) t; k++) {
                        boolean ground = q[1] <= 0;
                        double r = Math.hypot(q[0], q[2]);
                        double angle = Math.atan2(q[2], q[0]);
                        double s = r <= radius ? RubberWave.toss(RubberWave.rise(r, angle, k, radius, inf) - RubberWave.rise(r, angle, k - 1, radius, inf)) : 0;
                        if (ground && s > 0 && k >= next) {
                            next = k + RubberWave.TOSS_GAP;
                            double ox = q[0] / Math.max(r, 1e-6);
                            double oz = q[2] / Math.max(r, 1e-6);
                            double side = RubberWave.tossSide(s) * (random.nextDouble() < 0.5 ? -1 : 1) * (0.5 + random.nextDouble());
                            double out = RubberWave.tossOut(s) * (random.nextDouble() < RubberWave.BACK_CHANCE ? -RubberWave.BACK : 1);
                            v[0] = v[0] * 0.3 + ox * out - oz * side;
                            v[1] = RubberWave.tossUp(s);
                            v[2] = v[2] * 0.3 + oz * out + ox * side;
                        }
                        q[0] += v[0];
                        q[1] += v[1];
                        q[2] += v[2];
                        if (q[1] <= 0) {
                            q[1] = 0;
                            v[0] *= 0.55;
                            v[2] *= 0.55;
                            v[1] = 0;
                        } else {
                            v[1] = (v[1] - 0.08) * 0.98;
                            v[0] *= 0.91;
                            v[2] *= 0.91;
                        }
                    }
                    double r = Math.hypot(q[0], q[2]);
                    double lift = q[1] <= 0 ? RubberWave.rise(r, Math.atan2(q[2], q[0]), t, radius, inf) : 0;
                    HieShots.body(sink, q[0] - ex, q[1] + lift - ey, q[2] - ez);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * The player model's boxes (wide arms) as PlayerRenderer draws them at the feet x, y, z turned {@code yaw} (0 faces +z), scaled
     * {@code scale} times round the feet, the right arm pitched {@code arm} degrees (the rest standing).
     */
    static void giant(FireMesh.Sink sink, double x, double y, double z, double yaw, double scale, double arm) {
        // pivot (pixels), box from, box size, pitch, colour
        double[][] parts = {
            {0, 0, 0, -4, -8, -4, 8, 8, 8, 0, 0xD9A77E},
            {0, 0, 0, -4, 0, -2, 8, 12, 4, 0, 0xF2F0E6},
            {-5, 2, 0, -3, -2, -2, 4, 12, 4, arm, 0xD9A77E},
            {5, 2, 0, -1, -2, -2, 4, 12, 4, 0, 0xD9A77E},
            {-1.9, 12, 0, -2, 0, -2, 4, 12, 4, 0, 0x3A3A44},
            {1.9, 12, 0, -2, 0, -2, 4, 12, 4, 0, 0x3A3A44},
        };
        double r = Math.toRadians(180 - yaw);
        double cr = Math.cos(r);
        double sr = Math.sin(r);
        for (double[] p : parts) {
            double pitch = Math.toRadians(p[9]);
            double cp = Math.cos(pitch);
            double sp = Math.sin(pitch);
            double[][] c = new double[8][3];
            for (int i = 0; i < 8; i++) {
                double vx = p[3] + ((i & 1) != 0 ? p[6] : 0);
                double vy = p[4] + ((i & 2) != 0 ? p[7] : 0);
                double vz = p[5] + ((i & 4) != 0 ? p[8] : 0);
                // ModelPart: about X by its pitch, then to its pivot (pixels)
                double ry = vy * cp - vz * sp;
                double rz = vy * sp + vz * cp;
                double mx = (p[0] + vx) / 16.0;
                double my = (p[1] + ry) / 16.0 - 1.501;
                double mz = (p[2] + rz) / 16.0;
                // PlayerRenderer: 0.9375, then scale(-1, -1, 1), then about Y by 180 - yaw; the giant round the feet
                double fx = -0.9375 * mx;
                double fy = -0.9375 * my;
                double fz = 0.9375 * mz;
                c[i][0] = x + scale * (fx * cr + fz * sr);
                c[i][1] = y + scale * fy;
                c[i][2] = z + scale * (-fx * sr + fz * cr);
            }
            int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
            double[] shade = {0.62, 0.62, 0.95, 0.45, 0.78, 0.78};
            for (int k = 0; k < 6; k++) {
                int col = FireMesh.argb(1.0, FireMesh.mix(0, (int) p[10], shade[k]));
                double[] q = new double[12];
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(c[order[k][i]], 0, q, i * 3, 3);
                }
                sink.quad(q, new int[] {col, col, col, col});
            }
        }
    }

    /** HieShots.body's boxes changed as client/gear/NikaBendClient changes a drawing: in the blow's frame (yaw) scaled, leaned, pushed. */
    static void bent(FireMesh.Sink sink, double x, double y, double z, double yaw, double[] shape) {
        double[][] boxes = {
            {-0.3, 0, -0.15, 0.3, 0.75, 0.15, 0x3A4A7A},
            {-0.3, 0.75, -0.15, 0.3, 1.4, 0.15, 0xB03A2E},
            {-0.25, 1.4, -0.25, 0.25, 1.9, 0.25, 0xD9A77E},
        };
        double c = Math.cos(yaw);
        double sn = Math.sin(yaw);
        int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        double[] shade = {0.62, 0.62, 0.95, 0.45, 0.78, 0.78};
        for (double[] b : boxes) {
            double[][] corner = new double[8][3];
            for (int i = 0; i < 8; i++) {
                double vx = (i & 1) != 0 ? b[3] : b[0];
                double vy = (i & 2) != 0 ? b[4] : b[1];
                double vz = (i & 4) != 0 ? b[5] : b[2];
                // into the blow's frame: about Y by -yaw (JOML: x' = x cos + z sin, z' = -x sin + z cos)
                double lx = vx * Math.cos(-yaw) + vz * Math.sin(-yaw);
                double lz = -vx * Math.sin(-yaw) + vz * Math.cos(-yaw);
                lx *= shape[NikaBend.ACROSS];
                double ly = vy * shape[NikaBend.UP];
                lz *= shape[NikaBend.ALONG];
                lz += shape[NikaBend.SHEAR] * ly + shape[NikaBend.BACK];
                corner[i][0] = x + lx * c + lz * sn;
                corner[i][1] = y + ly;
                corner[i][2] = z - lx * sn + lz * c;
            }
            for (int k = 0; k < 6; k++) {
                int col = FireMesh.argb(1.0, FireMesh.mix(0, (int) b[6], shade[k]));
                double[] q = new double[12];
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(corner[order[k][i]], 0, q, i * 3, 3);
                }
                sink.quad(q, new int[] {col, col, col, col});
            }
        }
    }

    /**
     * One grass block (its top at y), from x, z, turned about the flat axis across the way out (rx, rz) by {@code tilt} radians round
     * its top's middle (a positive tilt faces its top inward, as the client turns it).
     */
    /** Two colours multiplied (a texel times a vertex colour, as the game draws it). */
    static int mulRgb(int a, int b) {
        int r = ((a >> 16) & 255) * ((b >> 16) & 255) / 255;
        int g = ((a >> 8) & 255) * ((b >> 8) & 255) / 255;
        int bl = (a & 255) * (b & 255) / 255;
        return (r << 16) | (g << 8) | bl;
    }

    static void grass(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt) {
        box(sink, x, y, z, rx, rz, tilt, 0, -1, true);
    }

    /**
     * 3.3.8: a risen column as client/gear/RubberWorldDraw draws it: the grass block lifted {@code h} (its top at {@code y}) and, past 0.6,
     * the block under it stretched down to the real ground (gear/RubberWave.underTall).
     */
    static void column(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt, double h) {
        box(sink, x, y, z, rx, rz, tilt, 0, -1, true);
        if (h > 0.6) {
            box(sink, x, y, z, rx, rz, tilt, -1, -1 - RubberWave.underTall(h), false);
        }
    }

    /** A block from {@code top} to {@code bottom} below the lifted top middle (x + 0.5, y, z + 0.5), tilted about the flat axis across. */
    static void box(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt, double top, double bottom, boolean grassy) {
        double c = Math.cos(tilt);
        double sn = Math.sin(tilt);
        double[][] corner = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double vx = ((i & 1) != 0 ? 1 : 0) - 0.5;
            double vy = ((i & 2) != 0 ? top : bottom);
            double vz = ((i & 4) != 0 ? 1 : 0) - 0.5;
            // about the tangent (-rz, 0, rx): the radial part and the height turn, the tangent part stays
            double radial = vx * rx + vz * rz;
            double tangent = -vx * rz + vz * rx;
            double radial2 = radial * c - vy * sn;
            double up2 = radial * sn + vy * c;
            corner[i][0] = x + 0.5 + radial2 * rx - tangent * rz;
            corner[i][1] = y + up2;
            corner[i][2] = z + 0.5 + radial2 * rz + tangent * rx;
        }
        int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        // faces: the two across z, the bottom, the top (grass), the two across x (dirt)
        int[] colour = {0x7A5634, 0x7A5634, 0x6B4A2E, grassy ? 0x6FB043 : 0x7A5634, 0x7A5634, 0x7A5634};
        double[] shade = {0.8, 0.8, 0.5, 1.0, 0.62, 0.62};
        for (int k = 0; k < 6; k++) {
            int col = FireMesh.argb(1.0, FireMesh.mix(0, colour[k], shade[k]));
            double[] q = new double[12];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(corner[order[k][i]], 0, q, i * 3, 3);
            }
            sink.quad(q, new int[] {col, col, col, col});
        }
    }

    // ------------------------------------------------------------------------------------------------------ 3.4.4: a skin in the preview

    static java.awt.image.BufferedImage userSkin;

    /** Skin quads (AstralArt.Skin) drawn into {@code sink} texel by texel in art/user_skin.png, lit as the game lights an entity. */
    static java.awt.image.BufferedImage[] smokeSprites;

    /** 3.4.9: Gear 5's smoke round feet (x, y, z): 40 ticks of wisps as client/gear/NikaSmokeFx spawns them and PieceFx moves them. */
    static void smoke(FireMesh.Sink sink, double x, double y, double z, double scale, long seed) {
        if (smokeSprites == null) {
            smokeSprites = new java.awt.image.BufferedImage[2];
            String[] names = {"steam", "puff"};
            for (int i = 0; i < 2; i++) {
                try {
                    smokeSprites[i] = javax.imageio.ImageIO.read(new java.io.File("src/main/resources/assets/hexhaki/textures/piece/particle/" + names[i] + ".png"));
                } catch (java.io.IOException failure) {
                    throw new IllegalStateException(failure);
                }
            }
        }
        java.util.Random random = new java.util.Random(seed);
        double[] w = new double[com.hexhaki.piece.gear.NikaSmoke.FIELDS];
        int now = 40;
        double carry = 0;
        java.util.List<double[]> drawn = new java.util.ArrayList<>();
        for (int born = 0; born < now; born++) {
            carry += com.hexhaki.piece.gear.NikaSmoke.rate(scale, 1.0);
            while (carry >= 1.0) {
                carry -= 1.0;
                com.hexhaki.piece.gear.NikaSmoke.spawn(scale, 0.0, random, w);
                int age = now - born;
                if (age > w[com.hexhaki.piece.gear.NikaSmoke.LIFETIME]) {
                    continue;
                }
                double px = w[0];
                double py = w[1];
                double pz = w[2];
                double vx = w[3];
                double vy = w[4];
                double vz = w[5];
                java.util.Random jitter = new java.util.Random(random.nextLong());
                for (int k = 0; k < age; k++) {
                    double j = com.hexhaki.piece.gear.NikaSmoke.WANDER;
                    vx += (jitter.nextFloat() - 0.5) * j;
                    vy += (jitter.nextFloat() - 0.5) * j * 0.5;
                    vz += (jitter.nextFloat() - 0.5) * j;
                    vx *= com.hexhaki.piece.gear.NikaSmoke.DRAG;
                    vy *= com.hexhaki.piece.gear.NikaSmoke.DRAG;
                    vz *= com.hexhaki.piece.gear.NikaSmoke.DRAG;
                    px += vx;
                    py += vy;
                    pz += vz;
                }
                double t = age / w[com.hexhaki.piece.gear.NikaSmoke.LIFETIME];
                double open = Math.min(1.0, age / 4.0);
                boolean puff = w[com.hexhaki.piece.gear.NikaSmoke.PUFF] > 0.5;
                int from = puff ? com.hexhaki.piece.gear.NikaSmoke.FROM_PUFF : com.hexhaki.piece.gear.NikaSmoke.FROM;
                double alpha = ((from >>> 24) / 255.0) * (1 - t) * open * open * (3 - 2 * open);
                double size = w[6] + (w[7] - w[6]) * t;
                drawn.add(new double[] {x + px, y + py, z + pz, size, alpha, puff ? 1 : 0});
            }
        }
        for (double[] d : drawn) {
            billboard(sink, d[0], d[1], d[2], d[3], d[4], smokeSprites[(int) d[5]]);
        }
    }

    /** A sprite facing the eye (at the origin), {@code size} across, its texels as little quads (14 x 14), white tinted by alpha. */
    static void billboard(FireMesh.Sink sink, double x, double y, double z, double size, double alpha, java.awt.image.BufferedImage sprite) {
        double d = Math.sqrt(x * x + y * y + z * z);
        double fx = -x / d;
        double fy = -y / d;
        double fz = -z / d;
        // right = up x toward-eye, up' = toward-eye x right
        double rx = fz;
        double ry = 0;
        double rz = -fx;
        double rl = Math.max(1.0E-6, Math.sqrt(rx * rx + rz * rz));
        rx /= rl;
        rz /= rl;
        double ux = fy * rz - fz * ry;
        double uy = fz * rx - fx * rz;
        double uz = fx * ry - fy * rx;
        int n = 14;
        double cell = size / n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sx = (int) ((i + 0.5) * sprite.getWidth() / n);
                int sy = (int) ((j + 0.5) * sprite.getHeight() / n);
                int texel = sprite.getRGB(sx, sy);
                double a = ((texel >>> 24) & 255) / 255.0 * alpha;
                if (a < 0.01) {
                    continue;
                }
                double a0 = -size / 2 + i * cell;
                double b0 = size / 2 - j * cell;
                int argb = ((int) (a * 255) << 24) | (texel & 0xFFFFFF);
                double[] q = new double[12];
                double[][] corners = {{a0, b0}, {a0 + cell, b0}, {a0 + cell, b0 - cell}, {a0, b0 - cell}};
                for (int c = 0; c < 4; c++) {
                    q[c * 3] = x + rx * corners[c][0] + ux * corners[c][1];
                    q[c * 3 + 1] = y + ry * corners[c][0] + uy * corners[c][1];
                    q[c * 3 + 2] = z + rz * corners[c][0] + uz * corners[c][1];
                }
                sink.quad(q, new int[] {argb, argb, argb, argb});
            }
        }
    }

    static com.hexhaki.piece.horo.AstralArt.Skin skin(FireMesh.Sink sink) {
        if (userSkin == null) {
            try {
                userSkin = javax.imageio.ImageIO.read(new java.io.File("art/user_skin.png"));
            } catch (java.io.IOException failure) {
                throw new IllegalStateException(failure);
            }
        }
        return (xyz, uv, argb, nx, ny, nz) -> {
            double shade = Math.min(1.0, 0.4 + 0.6 * (Math.max(0, nx * 0.2 + ny * 1.0 - nz * 0.7) / 1.24 + Math.max(0, -nx * 0.2 + ny * 1.0 + nz * 0.7) / 1.24));
            int u0 = Math.round(uv[0] * 64);
            int v0 = Math.round(uv[1] * 64);
            int u1 = Math.round(uv[2] * 64);
            int v1 = Math.round(uv[5] * 64);
            int tw = u1 - u0;
            int th = v1 - v0;
            if (tw <= 0 || th <= 0) {
                return;
            }
            for (int i = 0; i < tw; i++) {
                for (int j = 0; j < th; j++) {
                    int texel = userSkin.getRGB(u0 + i, v0 + j);
                    if ((texel >>> 24) < 26) {
                        continue;
                    }
                    int c = 0xFF000000 | (int) (((texel >> 16) & 255) * ((argb >> 16) & 255) / 255 * shade) << 16
                        | (int) (((texel >> 8) & 255) * ((argb >> 8) & 255) / 255 * shade) << 8 | (int) ((texel & 255) * (argb & 255) / 255 * shade);
                    double[] q = new double[12];
                    texel(q, 0, xyz, i / (double) tw, j / (double) th);
                    texel(q, 1, xyz, (i + 1) / (double) tw, j / (double) th);
                    texel(q, 2, xyz, (i + 1) / (double) tw, (j + 1) / (double) th);
                    texel(q, 3, xyz, i / (double) tw, (j + 1) / (double) th);
                    sink.quad(q, new int[] {c, c, c, c});
                }
            }
        };
    }

    static void texel(double[] q, int k, double[] xyz, double s, double t) {
        for (int a = 0; a < 3; a++) {
            double top = xyz[a] + (xyz[3 + a] - xyz[a]) * s;
            double bottom = xyz[9 + a] + (xyz[6 + a] - xyz[9 + a]) * s;
            q[k * 3 + a] = top + (bottom - top) * t;
        }
    }
}
