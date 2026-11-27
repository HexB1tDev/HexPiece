package com.hexhaki.piece.armor;

/**
 * 3.8.9: how a coat moves on its wearer (the owner: "The arms cannot clip through the coats, when walking the coat should be pushed back
 * like the wind is pushing it", "the arms should never interact with the coat's arms", with pictures of Garp walking, his coat over his
 * shoulders blown straight back from them like a flag, its empty sleeves streaming back with it). Pure: the same rule and numbers as
 * tools/coat_cloth.py (its renders and penetration checks), tested by ArmorTests. Points are model px (x the wearer's left, y down from the
 * top of the body, z back); a coat's points at rest are as baked (tools/clothes_bake.py: the body's frame).
 * <ol>
 *   <li>The wind of walking (the body's frame): going forward ({@code back} 1 at full speed) the coat is blown back, each point turned
 *       about the shoulders' line by more the lower it hangs (its depth below {@link #SHOULDER} over {@link #LENGTH} px, to the power
 *       {@link #CURVE}: most of the turn near the shoulders, the rest of the coat a sheet), fluttering; its open front panels swing out
 *       round the sides first (about the vertical at each shoulder's outer edge, {@link #OPEN}), so they go back round the arms; going
 *       sideways blows it across; going backwards, forward.</li>
 *   <li>Kept off the body (the body's frame): a point of the coat's front over the torso is turned back no further than the torso's
 *       front; below the hips each leg is a wedge about the hip line (its stride, its half-depth over the distance down): the cloth ahead
 *       of it stays ahead, the cloth behind behind. Worked out from the coat's surfaces at rest ({@link #FRONT_PANEL},
 *       {@link #BACK_PANEL}) so both faces of the cloth move together.</li>
 *   <li>Kept off the arms (model space): each arm is two boxes (the upper arm, the forearm past the elbow), grown by {@link #MARGIN}; a
 *       point of the coat's body inside one is pushed out along the way it lay from that arm at rest, to the box's side, its lining
 *       {@link #LINING} px less far than its cloth. The coat's sleeves are never touched by the arms.</li>
 *   <li>The sleeves: each empty sleeve hinged at its shoulder ({@link #SLEEVE_HINGE}), hanging flared {@link #FLARE} out (clear of the
 *       arm under it), blown back from the hinge further than the coat ({@link #SLEEVE_BACK}), fluttering more.</li>
 * </ol>
 */
public final class CoatCloth {
    public static final byte BODY = 0;
    public static final byte LINING_KIND = 1;
    public static final byte SLEEVE_RIGHT = 2;
    public static final byte SLEEVE_LEFT = 3;

    static final float SHOULDER = 1.0F;
    static final float LENGTH = 20.0F;
    static final float CURVE = 0.75F;
    static final float BACK = 1.0F;
    static final float SIDE = 0.35F;
    static final float FLUTTER = 0.09F;
    static final float TORSO_FRONT = -2.0F;
    static final float TORSO_HALF = 4.0F;
    static final float MARGIN = 0.35F;
    static final float LINING = 0.25F;
    static final float HIP = 12.0F;
    static final float LEG_HALF = 2.0F;
    static final float LEG_BAND = 2.3F;
    static final float FRONT_PANEL = -3.0F;
    static final float BACK_PANEL = 3.6F;
    static final float[] LEG_X = {-1.9F, 1.9F};
    /** The arms' boxes in their own frames (px from the pivot): x (wide right, wide left, slim right, slim left), y, z. */
    static final float[][] ARM_X = {{-3.0F, 1.0F}, {-1.0F, 3.0F}, {-2.0F, 1.0F}, {-1.0F, 2.0F}};
    static final float[] UPPER_Y = {-2.0F, 4.0F};
    static final float[] FORE_Y = {4.0F, 10.0F};
    static final float[] ARM_Z = {-2.0F, 2.0F};
    static final float[][] ARM_PIVOT = {{-5.0F, 2.0F, 0.0F}, {5.0F, 2.0F, 0.0F}};
    static final float OPEN = 1.1F;
    static final float[] OPEN_AT = {8.6F, 0.5F};
    static final float[][] SLEEVE_HINGE = {{-7.6F, 0.6F, 0.2F}, {7.6F, 0.6F, 0.2F}};
    static final float SLEEVE_LENGTH = 12.0F;
    static final float FLARE = 0.17F;
    static final float SLEEVE_BACK = 1.25F;
    static final float SLEEVE_FLUTTER = 0.16F;
    /** The wearer's speed (blocks a tick, along or across the way they face) that blows the coat back fully. */
    public static final float WIND_SPEED = 0.26F;

    private CoatCloth() {
    }

    static float smooth(float a, float b, float x) {
        float t = Math.max(0.0F, Math.min(1.0F, (x - a) / (b - a)));
        return t * t * (3.0F - 2.0F * t);
    }

    static float clamp01(float x) {
        return Math.max(0.0F, Math.min(1.0F, x));
    }

    /**
     * Rules 1, 2 and 4: the coat's points at rest ({@code rest}, the body's frame) moved into {@code out} (the body's frame).
     * {@code back} and {@code across}: the wind, -1 to 1; {@code phase}: the flutter's (radians); the legs' pitch (radians, + back).
     */
    public static void body(float[] rest, byte[] kind, int n, float back, float across, float phase, float rightLeg, float leftLeg,
                            float[] out) {
        float strength = Math.min(1.0F, Math.abs(back) + Math.abs(across));
        float open = Math.max(0.0F, back);
        for (int i = 0; i < n; i++) {
            float x = rest[i * 3];
            float y = rest[i * 3 + 1];
            float z = rest[i * 3 + 2];
            if (kind[i] == SLEEVE_RIGHT || kind[i] == SLEEVE_LEFT) {
                sleeve(x, y, z, kind[i] == SLEEVE_LEFT ? 1 : 0, back, across, phase, strength, out, i);
                continue;
            }
            float depth = (float) Math.pow(clamp01((y - SHOULDER) / LENGTH), CURVE);
            float flutter = FLUTTER * strength * (float) Math.sin(phase + 0.35F * y + 0.25F * x);
            float theta = (back * BACK + flutter) * depth;
            float turnAcross = -across * SIDE * depth;
            // rule 2: the torso
            float r = Math.max(y - SHOULDER, 0.05F);
            float rho = (float) Math.sqrt(r * r + FRONT_PANEL * FRONT_PANEL);
            float psi = (float) Math.atan2(FRONT_PANEL, r);
            float allow = (float) Math.asin(Math.max(-1.0F, Math.min(1.0F, (TORSO_FRONT - MARGIN) / rho))) - psi;
            float over = smooth(TORSO_HALF + 0.9F, TORSO_HALF - 0.3F, Math.abs(x)) * (y < HIP + 1.0F ? 1.0F : 0.0F) * (z < -0.5F ? 1.0F : 0.0F);
            theta = theta + (Math.min(theta, allow) - theta) * over;
            // rule 1's opening: the front panels out round the sides
            float px = x;
            float pz = z;
            if (open > 0.0F && x != 0.0F) {
                float sign = Math.signum(x);
                float d = (float) Math.pow(clamp01((y - SHOULDER) / LENGTH), 0.6);
                float angle = -sign * OPEN * open * d * smooth(0.5F, -2.0F, z) * smooth(0.5F, 2.5F, Math.abs(x));
                float c = (float) Math.cos(angle);
                float s = (float) Math.sin(angle);
                float dx = x - sign * OPEN_AT[0];
                float dz = z - OPEN_AT[1];
                px = sign * OPEN_AT[0] + dx * c + dz * s;
                pz = OPEN_AT[1] - dx * s + dz * c;
            }
            // the wind's turn back about the shoulders
            float dy = y - SHOULDER;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);
            float my = SHOULDER + dy * c - pz * s;
            float mz = dy * s + pz * c;
            // rule 2: the legs' wedges (from the coat's surface at rest turned as the coat was)
            boolean front = z < 0.0F;
            float refZ = front ? FRONT_PANEL : BACK_PANEL;
            float ry = SHOULDER + dy * c - refZ * s;
            float rz = dy * s + refZ * c;
            float below = Math.max(ry - HIP, 0.05F);
            float rpsi = (float) Math.atan2(rz, below);
            float half = (float) Math.atan2(LEG_HALF + MARGIN, below);
            float extra = 0.0F;
            for (int leg = 0; leg < 2; leg++) {
                float alpha = leg == 0 ? rightLeg : leftLeg;
                float band = smooth(LEG_BAND, LEG_BAND - 1.2F, Math.abs(x - LEG_X[leg])) * smooth(HIP - 1.0F, HIP + 0.5F, ry);
                float need = front ? Math.min(0.0F, (alpha - half) - rpsi) : Math.max(0.0F, (alpha + half) - rpsi);
                extra += need * band;
            }
            if (extra != 0.0F) {
                float ec = (float) Math.cos(extra);
                float es = (float) Math.sin(extra);
                float ey = my - HIP;
                my = HIP + ey * ec - mz * es;
                mz = ey * es + mz * ec;
            }
            // the wind across
            float ac = (float) Math.cos(turnAcross);
            float as = (float) Math.sin(turnAcross);
            float ay = my - SHOULDER;
            out[i * 3] = px * ac - ay * as;
            out[i * 3 + 1] = SHOULDER + px * as + ay * ac;
            out[i * 3 + 2] = mz;
        }
    }

    /** Rule 4: one point of an empty sleeve (side 0 right, 1 left). */
    static void sleeve(float x, float y, float z, int side, float back, float across, float phase, float strength, float[] out, int i) {
        float hx = SLEEVE_HINGE[side][0];
        float hy = SLEEVE_HINGE[side][1];
        float hz = SLEEVE_HINGE[side][2];
        float flare = side == 1 ? -FLARE : FLARE;
        float c = (float) Math.cos(flare);
        float s = (float) Math.sin(flare);
        float dx = x - hx;
        float dy = y - hy;
        float fx = hx + dx * c - dy * s;
        float fy = hy + dx * s + dy * c;
        float depth = clamp01((y - hy) / SLEEVE_LENGTH);
        float flutter = SLEEVE_FLUTTER * strength * (float) Math.sin(phase * 1.3F + 0.5F * y + (side == 1 ? 1.7F : 0.0F));
        float theta = (back * SLEEVE_BACK + flutter) * (float) Math.pow(depth, 1.2);
        c = (float) Math.cos(theta);
        s = (float) Math.sin(theta);
        float ry = fy - hy;
        float rz = z - hz;
        float by = hy + ry * c - rz * s;
        float bz = hz + ry * s + rz * c;
        float a = -across * SIDE * depth;
        c = (float) Math.cos(a);
        s = (float) Math.sin(a);
        float ax = fx - hx;
        float ay = by - hy;
        out[i * 3] = hx + ax * c - ay * s;
        out[i * 3 + 1] = hy + ax * s + ay * c;
        out[i * 3 + 2] = bz;
    }

    /** Rule 3's ways: for each point, the unit way (x, z in the arm's frame) it lay from each arm's box's middle at rest (right, left). */
    public static float[] restWays(float[] rest, int n, boolean slim) {
        float[] ways = new float[n * 4];
        for (int side = 0; side < 2; side++) {
            float[] ax = ARM_X[(slim ? 2 : 0) + side];
            float cx = (ax[0] + ax[1]) / 2.0F;
            float cz = (ARM_Z[0] + ARM_Z[1]) / 2.0F;
            for (int i = 0; i < n; i++) {
                float dx = rest[i * 3] - ARM_PIVOT[side][0] - cx;
                float dz = rest[i * 3 + 2] - ARM_PIVOT[side][2] - cz;
                float len = (float) Math.sqrt(dx * dx + dz * dz);
                ways[i * 4 + side * 2] = len > 1.0E-6F ? dx / len : 0.0F;
                ways[i * 4 + side * 2 + 1] = len > 1.0E-6F ? dz / len : -1.0F;
            }
        }
        return ways;
    }

    /** 3.9.5: an arm this far into the cloth (px) pushes it out wholly; from there less and less, past PUSH_NONE not at all. */
    static final float PUSH_FULL = 0.9F;
    static final float PUSH_NONE = 1.8F;

    /** How far a point {@code c} (a box's frame) must go along {@code u} to leave the box lo..hi (the slabs' nearest far side). */
    static float exitAlong(float[] c, float[] u, float[] lo, float[] hi) {
        float t = Float.MAX_VALUE;
        for (int k = 0; k < 3; k++) {
            if (Math.abs(u[k]) > 1.0E-6F) {
                float far = u[k] > 0.0F ? hi[k] : lo[k];
                t = Math.min(t, (far - c[k]) / u[k]);
            }
        }
        return Math.max(t, 0.0F);
    }

    /**
     * Rule 3: the coat's body points ({@code points}, model px, in place) pushed out of the arms' boxes, each along the way it lay from
     * that arm at rest (in the arm's frame) until it is out, its lining LINING px less far. {@code frames}: four segment frames (right
     * upper, right forearm, left upper, left forearm), each a 3 x 4 row-major matrix taking the segment's own frame (px from the arm's
     * pivot) to model px. The top of the upper arm is the shoulder the coat rests on: no margin above it. The sleeves are left alone.
     */
    public static void armsPush(float[] points, byte[] kind, int n, float[] ways, float[][] frames, boolean slim) {
        float[] c = new float[3];
        float[] u = new float[3];
        for (int f = 0; f < 4; f++) {
            int side = f / 2;
            boolean upper = f % 2 == 0;
            float[] m = frames[f];
            float[] inv = invert(m);
            float[] ax = ARM_X[(slim ? 2 : 0) + side];
            float[] ys = upper ? UPPER_Y : FORE_Y;
            float[] lo = {ax[0] - MARGIN, ys[0] - (upper ? 0.0F : MARGIN), ARM_Z[0] - MARGIN};
            float[] hi = {ax[1] + MARGIN, ys[1] + MARGIN, ARM_Z[1] + MARGIN};
            for (int i = 0; i < n; i++) {
                if (kind[i] != BODY && kind[i] != LINING_KIND) {
                    continue;
                }
                float x = points[i * 3];
                float y = points[i * 3 + 1];
                float z = points[i * 3 + 2];
                c[0] = inv[0] * x + inv[1] * y + inv[2] * z + inv[3];
                c[1] = inv[4] * x + inv[5] * y + inv[6] * z + inv[7];
                c[2] = inv[8] * x + inv[9] * y + inv[10] * z + inv[11];
                if (c[0] <= lo[0] || c[0] >= hi[0] || c[1] <= lo[1] || c[1] >= hi[1] || c[2] <= lo[2] || c[2] >= hi[2]) {
                    continue;
                }
                u[0] = ways[i * 4 + side * 2];
                u[1] = 0.0F;
                u[2] = ways[i * 4 + side * 2 + 1];
                // 3.9.5 (the owner: "Whenever using a move that it does an animation, sometimes briefly, the the entire cape breaks", a
                // picture of the coat blown out round the arm like a bubble): an arm only a little into the cloth pushes it out of its way;
                // one deep in it (a move's arm swung through the coat) pushes it less and less, past PUSH_NONE not at all: never a bubble
                float need = exitAlong(c, u, lo, hi);
                float fade = 1.0F - smooth(PUSH_FULL, PUSH_NONE, need);
                if (fade <= 0.0F) {
                    continue;
                }
                float push = (need + (kind[i] == LINING_KIND ? 0.0F : LINING)) * fade;
                float lx = c[0] + push * u[0];
                float lz = c[2] + push * u[2];
                points[i * 3] = m[0] * lx + m[1] * c[1] + m[2] * lz + m[3];
                points[i * 3 + 1] = m[4] * lx + m[5] * c[1] + m[6] * lz + m[7];
                points[i * 3 + 2] = m[8] * lx + m[9] * c[1] + m[10] * lz + m[11];
            }
        }
    }

    /** The head's box in its own frame (x and z within HEAD_HALF of its axis, y from HEAD_Y0 to HEAD_Y1), grown by MARGIN. */
    static final float HEAD_HALF = 4.0F;
    static final float HEAD_Y0 = -8.0F;
    static final float HEAD_Y1 = 0.0F;

    /** The collar's points from this far below the top of the body up are widened round the head (fading in from there). */
    static final float HEAD_REACH = 2.0F;
    /**
     * 3.8.9 (the owner, of the collar crumpled behind a turned head: "the fuck is this mess behind the head"): the collar is a ring round
     * the neck in SECTORS directions; the head's reach (its box grown by MARGIN, sampled HEAD_SAMPLES a side, at the collar's heights
     * COLLAR_LOW to COLLAR_HIGH) against the collar's inside at rest in each direction.
     */
    static final int SECTORS = 72;
    static final int NEIGHBOURS = 2;
    static final float COLLAR_LOW = -3.5F;
    static final float COLLAR_HIGH = 0.0F;
    static final int HEAD_SAMPLES = 25;
    static final float RING_TOLERANCE = 0.25F;

    static int sector(float x, float z) {
        double a = Math.atan2(z, x);
        if (a < 0.0) {
            a += 2.0 * Math.PI;
        }
        return Math.min((int) (a / (2.0 * Math.PI) * SECTORS), SECTORS - 1);
    }

    /** Per direction round the neck: how far out the collar's inside lies at rest (the least radius of its points there; MAX if none). */
    public static float[] ringRest(float[] rest, int n) {
        float[] inner = new float[SECTORS];
        java.util.Arrays.fill(inner, Float.MAX_VALUE);
        for (int i = 0; i < n; i++) {
            float x = rest[i * 3];
            float z = rest[i * 3 + 2];
            if (rest[i * 3 + 1] < COLLAR_HIGH) {
                int s = sector(x, z);
                inner[s] = Math.min(inner[s], (float) Math.sqrt(x * x + z * z));
            }
        }
        return inner;
    }

    /** Per direction round the neck: how far out the head (its box grown by MARGIN; frame: its frame to model px) reaches at the collar. */
    static float[] headReach(float[] f) {
        float[] reach = new float[SECTORS];
        float h = HEAD_HALF + MARGIN;
        for (int iy = 0; iy < HEAD_SAMPLES; iy++) {
            float y = HEAD_Y0 + (HEAD_Y1 - HEAD_Y0) * iy / (HEAD_SAMPLES - 1);
            for (int it = 0; it < HEAD_SAMPLES; it++) {
                float t = -1.0F + 2.0F * it / (HEAD_SAMPLES - 1);
                reachOf(f, t * h, y, -h, reach);
                reachOf(f, t * h, y, h, reach);
                reachOf(f, -h, y, t * h, reach);
                reachOf(f, h, y, t * h, reach);
                // the bottom (the head's underside, at the neck)
                float b = -1.0F + 2.0F * iy / (HEAD_SAMPLES - 1);
                reachOf(f, t * h, HEAD_Y1, b * h, reach);
            }
        }
        return reach;
    }

    static void reachOf(float[] f, float x, float y, float z, float[] reach) {
        float my = f[4] * x + f[5] * y + f[6] * z + f[7];
        if (my <= COLLAR_LOW || my >= COLLAR_HIGH + MARGIN) {
            return;
        }
        float mx = f[0] * x + f[1] * y + f[2] * z + f[3];
        float mz = f[8] * x + f[9] * y + f[10] * z + f[11];
        int s = sector(mx, mz);
        reach[s] = Math.max(reach[s], (float) Math.sqrt(mx * mx + mz * mz));
    }

    /**
     * The collar (the coat's body points above HEAD_REACH, fading in from there) set out level from the neck, direction by direction, at
     * least as far as its place at rest plus how much the head ({@code frame}) reaches past the collar's inside there ({@code inner},
     * {@link #ringRest}), eased round the ring: cloth and lining together, so it widens where the head turns into it, never folds.
     * Idempotent: it alternates with the arms' push.
     */
    public static void ringPush(float[] points, byte[] kind, float[] rest, int n, float[] frame, float[] inner) {
        float[] reach = headReach(frame);
        float[] need = new float[SECTORS];
        for (int s = 0; s < SECTORS; s++) {
            need[s] = inner[s] == Float.MAX_VALUE ? 0.0F : Math.max(0.0F, reach[s] - inner[s] - RING_TOLERANCE);
        }
        float[] wide = new float[SECTORS];
        for (int s = 0; s < SECTORS; s++) {
            float m = need[s];
            for (int k = 1; k <= NEIGHBOURS; k++) {
                m = Math.max(m, Math.max(need[(s + k) % SECTORS], need[(s - k + SECTORS) % SECTORS]));
            }
            wide[s] = m;
        }
        float[] eased = new float[SECTORS];
        for (int s = 0; s < SECTORS; s++) {
            float e = (wide[(s + SECTORS - 1) % SECTORS] + 2.0F * wide[s] + wide[(s + 1) % SECTORS]) / 4.0F;
            eased[s] = Math.max(e, need[s]);
        }
        for (int i = 0; i < n; i++) {
            if (kind[i] != BODY && kind[i] != LINING_KIND) {
                continue;
            }
            float w = smooth(HEAD_REACH, 0.0F, rest[i * 3 + 1]);
            if (w <= 0.0F) {
                continue;
            }
            float rx = rest[i * 3];
            float rz = rest[i * 3 + 2];
            float target = (float) Math.sqrt(rx * rx + rz * rz) + eased[sector(rx, rz)] * w;
            float x = points[i * 3];
            float z = points[i * 3 + 2];
            float now = (float) Math.sqrt(x * x + z * z);
            if (now > 1.0E-6F && now < target) {
                points[i * 3] = x * target / now;
                points[i * 3 + 2] = z * target / now;
            }
        }
    }

    /** Rule 3: the collar widened round the head as a ring and the arms pushing the coat out of their way, alternately, three times. */
    public static void keepOut(float[] points, byte[] kind, float[] rest, int n, float[] ways, float[][] arms, float[] head, float[] inner,
                               boolean slim) {
        for (int round = 0; round < 3; round++) {
            ringPush(points, kind, rest, n, head, inner);
            armsPush(points, kind, n, ways, arms, slim);
        }
    }

    /** The inverse of a rigid 3 x 4 frame (its turn transposed). */
    static float[] invert(float[] m) {
        float[] r = new float[12];
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                r[a * 4 + b] = m[b * 4 + a];
            }
        }
        for (int a = 0; a < 3; a++) {
            r[a * 4 + 3] = -(r[a * 4] * m[3] + r[a * 4 + 1] * m[7] + r[a * 4 + 2] * m[11]);
        }
        return r;
    }

    /** The wind's target from the wearer's step this tick (blocks; along {@code yaw}, degrees, as the game faces): {back, across}. */
    public static float[] wind(double dx, double dz, float yaw) {
        double r = Math.toRadians(yaw);
        double forward = -Math.sin(r) * dx + Math.cos(r) * dz;
        double left = Math.cos(r) * dx + Math.sin(r) * dz;
        return new float[] {(float) Math.max(-1.0, Math.min(1.0, forward / WIND_SPEED)), (float) Math.max(-1.0, Math.min(1.0, left / WIND_SPEED))};
    }
}
