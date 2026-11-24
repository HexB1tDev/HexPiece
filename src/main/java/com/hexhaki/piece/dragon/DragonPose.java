package com.hexhaki.piece.dragon;

/**
 * 2.7.0: a pose of the dragon's rig: every bone's local rotation, translation and scale (from the rest pose, its takes blended in,
 * bends added on top), solved into the skinning matrices, and the parts skinned with them (four bones a vertex, linear blend).
 * Pure; one per renderer (not thread safe), reused frame after frame without allocating.
 */
public final class DragonPose {
    final DragonModel model;
    public final float[] t;
    public final float[] q;
    public final float[] s;
    /** Bone globals (model space, blocks), 3 x 4 row-major. */
    public final float[] global;
    /** Skinning matrices: global x inverse bind. */
    public final float[] skin;
    private final float[] tmp = new float[4];
    private final float[] local = new float[12];

    public DragonPose(DragonModel model) {
        this.model = model;
        int n = model.bones();
        this.t = new float[n * 3];
        this.q = new float[n * 4];
        this.s = new float[n * 3];
        this.global = new float[n * 12];
        this.skin = new float[n * 12];
        rest();
    }

    /** Every bone at rest. */
    public void rest() {
        System.arraycopy(this.model.restT, 0, this.t, 0, this.t.length);
        System.arraycopy(this.model.restQ, 0, this.q, 0, this.q.length);
        System.arraycopy(this.model.restS, 0, this.s, 0, this.s.length);
    }

    /**
     * A take at {@code seconds} (looped or held at its ends) blended over the pose by {@code weight} (1: replaces it): rotations by
     * normalized lerp on the short way, translations and scales linearly. Between its frames it is interpolated the same way.
     */
    public void clip(DragonModel.Clip clip, float seconds, boolean loop, float weight) {
        if (clip == null || weight <= 0.0F) {
            return;
        }
        float f = seconds * clip.fps;
        int last = clip.frames - 1;
        if (loop && last > 0) {
            f = f % last;
            if (f < 0) {
                f += last;
            }
        } else {
            f = Math.max(0.0F, Math.min(last, f));
        }
        int a = (int) Math.floor(f);
        int b = Math.min(last, a + 1);
        float u = f - a;
        float w = Math.min(1.0F, weight);
        for (int c = 0; c < clip.bone.length; c++) {
            int bone = clip.bone[c];
            float[] v = clip.values[c];
            if (clip.kind[c] == 0) {
                float[] r = this.tmp;
                nlerp(v, a * 4, v, b * 4, u, r);
                int o = bone * 4;
                float dot = this.q[o] * r[0] + this.q[o + 1] * r[1] + this.q[o + 2] * r[2] + this.q[o + 3] * r[3];
                float sign = dot < 0 ? -1.0F : 1.0F;
                float x = this.q[o] + (r[0] * sign - this.q[o]) * w;
                float y = this.q[o + 1] + (r[1] * sign - this.q[o + 1]) * w;
                float z = this.q[o + 2] + (r[2] * sign - this.q[o + 2]) * w;
                float ww = this.q[o + 3] + (r[3] * sign - this.q[o + 3]) * w;
                float len = (float) Math.sqrt(x * x + y * y + z * z + ww * ww);
                this.q[o] = x / len;
                this.q[o + 1] = y / len;
                this.q[o + 2] = z / len;
                this.q[o + 3] = ww / len;
            } else {
                float[] dst = clip.kind[c] == 1 ? this.t : this.s;
                int o = bone * 3;
                for (int k = 0; k < 3; k++) {
                    float value = v[a * 3 + k] + (v[b * 3 + k] - v[a * 3 + k]) * u;
                    dst[o + k] += (value - dst[o + k]) * w;
                }
            }
        }
    }

    static void nlerp(float[] p, int i, float[] r, int j, float u, float[] out) {
        float dot = p[i] * r[j] + p[i + 1] * r[j + 1] + p[i + 2] * r[j + 2] + p[i + 3] * r[j + 3];
        float sign = dot < 0 ? -1.0F : 1.0F;
        float x = p[i] + (r[j] * sign - p[i]) * u;
        float y = p[i + 1] + (r[j + 1] * sign - p[i + 1]) * u;
        float z = p[i + 2] + (r[j + 2] * sign - p[i + 2]) * u;
        float w = p[i + 3] + (r[j + 3] * sign - p[i + 3]) * u;
        float len = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        out[0] = x / len;
        out[1] = y / len;
        out[2] = z / len;
        out[3] = w / len;
    }

    /**
     * Turns a bone by {@code degrees} about an axis of its PARENT's frame (unit x, y, z): the bend is applied before the bone's own
     * rotation, so it swings the bone and everything after it.
     */
    public void bend(int bone, float degrees, float ax, float ay, float az) {
        if (bone < 0 || degrees == 0.0F) {
            return;
        }
        float half = (float) Math.toRadians(degrees) * 0.5F;
        float sn = (float) Math.sin(half);
        float bx = ax * sn;
        float by = ay * sn;
        float bz = az * sn;
        float bw = (float) Math.cos(half);
        int o = bone * 4;
        float x = this.q[o];
        float y = this.q[o + 1];
        float z = this.q[o + 2];
        float w = this.q[o + 3];
        // b * q
        this.q[o] = bw * x + bx * w + by * z - bz * y;
        this.q[o + 1] = bw * y - bx * z + by * w + bz * x;
        this.q[o + 2] = bw * z + bx * y - by * x + bz * w;
        this.q[o + 3] = bw * w - bx * x - by * y - bz * z;
    }

    /** The globals and skinning matrices from the locals. */
    public void solve() {
        int n = this.model.bones();
        float[] m = this.local;
        for (int b = 0; b < n; b++) {
            local(b, m);
            int p = this.model.parent[b];
            int o = b * 12;
            if (p < 0) {
                System.arraycopy(m, 0, this.global, o, 12);
            } else {
                mul(this.global, p * 12, m, 0, this.global, o);
            }
            mul(this.global, o, this.model.inverseBind, o, this.skin, o);
        }
    }

    void local(int b, float[] m) {
        float x = this.q[b * 4];
        float y = this.q[b * 4 + 1];
        float z = this.q[b * 4 + 2];
        float w = this.q[b * 4 + 3];
        float sx = this.s[b * 3];
        float sy = this.s[b * 3 + 1];
        float sz = this.s[b * 3 + 2];
        m[0] = (1 - 2 * (y * y + z * z)) * sx;
        m[1] = (2 * (x * y - z * w)) * sy;
        m[2] = (2 * (x * z + y * w)) * sz;
        m[3] = this.t[b * 3];
        m[4] = (2 * (x * y + z * w)) * sx;
        m[5] = (1 - 2 * (x * x + z * z)) * sy;
        m[6] = (2 * (y * z - x * w)) * sz;
        m[7] = this.t[b * 3 + 1];
        m[8] = (2 * (x * z - y * w)) * sx;
        m[9] = (2 * (y * z + x * w)) * sy;
        m[10] = (1 - 2 * (x * x + y * y)) * sz;
        m[11] = this.t[b * 3 + 2];
    }

    /** out = a x b (3 x 4 affine, row-major); out may not be a or b. */
    static void mul(float[] a, int i, float[] b, int j, float[] out, int k) {
        for (int r = 0; r < 3; r++) {
            float a0 = a[i + r * 4];
            float a1 = a[i + r * 4 + 1];
            float a2 = a[i + r * 4 + 2];
            float a3 = a[i + r * 4 + 3];
            out[k + r * 4] = a0 * b[j] + a1 * b[j + 4] + a2 * b[j + 8];
            out[k + r * 4 + 1] = a0 * b[j + 1] + a1 * b[j + 5] + a2 * b[j + 9];
            out[k + r * 4 + 2] = a0 * b[j + 2] + a1 * b[j + 6] + a2 * b[j + 10];
            out[k + r * 4 + 3] = a0 * b[j + 3] + a1 * b[j + 7] + a2 * b[j + 11] + a3;
        }
    }

    /** A bone's origin now (model space). */
    public void origin(int bone, float[] out) {
        out[0] = this.global[bone * 12 + 3];
        out[1] = this.global[bone * 12 + 7];
        out[2] = this.global[bone * 12 + 11];
    }

    /**
     * The part skinned: positions and normals (model space) into {@code pos} and {@code nrm} (3 a vertex), each through the blend of
     * its bones' skinning matrices (the normal by the same blend, renormalized: the rig has no shear).
     */
    public void skin(DragonModel.Part part, float[] pos, float[] nrm) {
        skin(part, part.pos, part.nrm, pos, nrm);
    }

    /** 3.7.1: as {@link #skin(DragonModel.Part, float[], float[])}, from the rest positions and normals given (a part re-shaped first). */
    public void skin(DragonModel.Part part, float[] restPos, float[] restNrm, float[] pos, float[] nrm) {
        float[] sk = this.skin;
        int nv = part.vertices();
        for (int v = 0; v < nv; v++) {
            float m0 = 0, m1 = 0, m2 = 0, m3 = 0, m4 = 0, m5 = 0, m6 = 0, m7 = 0, m8 = 0, m9 = 0, m10 = 0, m11 = 0;
            for (int k = 0; k < 4; k++) {
                float wt = part.weight[v * 4 + k];
                if (wt <= 0.0F) {
                    continue;
                }
                int o = (part.bone[v * 4 + k] & 0xFF) * 12;
                m0 += sk[o] * wt;
                m1 += sk[o + 1] * wt;
                m2 += sk[o + 2] * wt;
                m3 += sk[o + 3] * wt;
                m4 += sk[o + 4] * wt;
                m5 += sk[o + 5] * wt;
                m6 += sk[o + 6] * wt;
                m7 += sk[o + 7] * wt;
                m8 += sk[o + 8] * wt;
                m9 += sk[o + 9] * wt;
                m10 += sk[o + 10] * wt;
                m11 += sk[o + 11] * wt;
            }
            float x = restPos[v * 3];
            float y = restPos[v * 3 + 1];
            float z = restPos[v * 3 + 2];
            pos[v * 3] = m0 * x + m1 * y + m2 * z + m3;
            pos[v * 3 + 1] = m4 * x + m5 * y + m6 * z + m7;
            pos[v * 3 + 2] = m8 * x + m9 * y + m10 * z + m11;
            float nx = restNrm[v * 3];
            float ny = restNrm[v * 3 + 1];
            float nz = restNrm[v * 3 + 2];
            float rx = m0 * nx + m1 * ny + m2 * nz;
            float ry = m4 * nx + m5 * ny + m6 * nz;
            float rz = m8 * nx + m9 * ny + m10 * nz;
            float len = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
            if (len < 1.0E-6F) {
                len = 1.0F;
            }
            nrm[v * 3] = rx / len;
            nrm[v * 3 + 1] = ry / len;
            nrm[v * 3 + 2] = rz / len;
        }
    }
}
