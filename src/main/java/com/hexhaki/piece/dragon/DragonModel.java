package com.hexhaki.piece.dragon;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 2.7.0: the Uo Uo no Mi's dragon (the owner's model, input/kaido_dragon_form.zip), as tools/dragon_mesh.py packs it
 * ({@code assets/hexhaki/dragon/seiryu.sry}): the rig (rest pose, binds), every part's vertices with their four skin weights, and the
 * model's own takes. Pure (no Minecraft API): the game, the tests and tools/preview/DragonPreview read the same file the same way.
 */
public final class DragonModel {
    /** The packed positions' range (the FBX's units). */
    static final float EXTENT = 128.0F;
    public static final int OPAQUE = 0;
    public static final int CUTOUT = 1;
    public static final int GLOW = 2;

    public final String[] names;
    public final int[] parent;
    /** Rest local: t (3), q (4: x, y, z, w), s (3) per bone. */
    public final float[] restT;
    public final float[] restQ;
    public final float[] restS;
    /** The inverse binds, 3 x 4 row-major per bone. */
    public final float[] inverseBind;
    public final Part[] parts;
    public final Map<String, Clip> clips = new HashMap<>();
    private final Map<String, Integer> byName = new HashMap<>();

    /** A mesh: positions, uvs, normals, four bones and weights per vertex, quads. */
    public static final class Part {
        public final String name;
        public final int kind;
        public final float[] pos;
        public final float[] uv;
        public final float[] nrm;
        public final byte[] bone;
        public final float[] weight;
        public final int[] quads;

        Part(String name, int kind, float[] pos, float[] uv, float[] nrm, byte[] bone, float[] weight, int[] quads) {
            this.name = name;
            this.kind = kind;
            this.pos = pos;
            this.uv = uv;
            this.nrm = nrm;
            this.bone = bone;
            this.weight = weight;
            this.quads = quads;
        }

        public int vertices() {
            return this.pos.length / 3;
        }
    }

    /** A take: channels of one bone each (rotation, translation or scale), sampled at {@link #fps}. */
    public static final class Clip {
        public final String name;
        public final float fps;
        public final int frames;
        public final short[] bone;
        public final byte[] kind;
        /** Per channel: frames x 4 (rotation x, y, z, w) or frames x 3. */
        public final float[][] values;

        Clip(String name, float fps, int frames, short[] bone, byte[] kind, float[][] values) {
            this.name = name;
            this.fps = fps;
            this.frames = frames;
            this.bone = bone;
            this.kind = kind;
            this.values = values;
        }

        /** Its length in seconds (the last frame's time). */
        public float seconds() {
            return (this.frames - 1) / this.fps;
        }
    }

    private DragonModel(String[] names, int[] parent, float[] restT, float[] restQ, float[] restS, float[] inverseBind, Part[] parts) {
        this.names = names;
        this.parent = parent;
        this.restT = restT;
        this.restQ = restQ;
        this.restS = restS;
        this.inverseBind = inverseBind;
        this.parts = parts;
        for (int i = 0; i < names.length; i++) {
            this.byName.put(names[i], i);
        }
    }

    public int bones() {
        return this.names.length;
    }

    /** The bone named, or -1. */
    public int bone(String name) {
        Integer i = this.byName.get(name);
        return i == null ? -1 : i;
    }

    public static DragonModel read(InputStream raw) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw), 1 << 16));
        byte[] magic = new byte[4];
        in.readFully(magic);
        if (magic[0] != 'S' || magic[1] != 'R' || magic[2] != 'Y' || magic[3] != '1' || in.readInt() != 1) {
            throw new IOException("not a dragon model");
        }
        int nb = in.readInt();
        String[] names = new String[nb];
        int[] parent = new int[nb];
        float[] t = new float[nb * 3];
        float[] q = new float[nb * 4];
        float[] s = new float[nb * 3];
        float[] inv = new float[nb * 12];
        for (int b = 0; b < nb; b++) {
            names[b] = in.readUTF();
            parent[b] = in.readShort();
            for (int k = 0; k < 3; k++) {
                t[b * 3 + k] = in.readFloat();
            }
            for (int k = 0; k < 4; k++) {
                q[b * 4 + k] = in.readFloat();
            }
            for (int k = 0; k < 3; k++) {
                s[b * 3 + k] = in.readFloat();
            }
            for (int k = 0; k < 12; k++) {
                inv[b * 12 + k] = in.readFloat();
            }
        }
        int np = in.readInt();
        Part[] parts = new Part[np];
        for (int p = 0; p < np; p++) {
            String name = in.readUTF();
            int kind = in.readByte();
            int nv = in.readInt();
            int nq = in.readInt();
            float[] pos = new float[nv * 3];
            float[] uv = new float[nv * 2];
            float[] nrm = new float[nv * 3];
            byte[] bone = new byte[nv * 4];
            float[] weight = new float[nv * 4];
            for (int v = 0; v < nv; v++) {
                for (int k = 0; k < 3; k++) {
                    pos[v * 3 + k] = in.readShort() / 32767.0F * EXTENT;
                }
                uv[v * 2] = in.readUnsignedShort() / 65535.0F;
                uv[v * 2 + 1] = in.readUnsignedShort() / 65535.0F;
                float nx = in.readByte();
                float ny = in.readByte();
                float nz = in.readByte();
                float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len < 1.0E-3F) {
                    ny = 1.0F;
                    len = 1.0F;
                }
                nrm[v * 3] = nx / len;
                nrm[v * 3 + 1] = ny / len;
                nrm[v * 3 + 2] = nz / len;
                for (int k = 0; k < 4; k++) {
                    bone[v * 4 + k] = in.readByte();
                }
                for (int k = 0; k < 4; k++) {
                    weight[v * 4 + k] = in.readUnsignedByte() / 255.0F;
                }
            }
            int[] quads = new int[nq * 4];
            for (int i = 0; i < quads.length; i++) {
                quads[i] = in.readUnsignedShort();
            }
            parts[p] = new Part(name, kind, pos, uv, nrm, bone, weight, quads);
        }
        DragonModel model = new DragonModel(names, parent, t, q, s, inv, parts);
        int nc = in.readInt();
        for (int c = 0; c < nc; c++) {
            String name = in.readUTF();
            float fps = in.readFloat();
            int frames = in.readInt();
            int channels = in.readInt();
            short[] cb = new short[channels];
            byte[] ck = new byte[channels];
            float[][] values = new float[channels][];
            for (int k = 0; k < channels; k++) {
                cb[k] = in.readShort();
                ck[k] = in.readByte();
                int width = ck[k] == 0 ? 4 : 3;
                float[] v = new float[frames * width];
                for (int i = 0; i < v.length; i++) {
                    v[i] = ck[k] == 0 ? in.readShort() / 32767.0F : in.readFloat();
                }
                values[k] = v;
            }
            model.clips.put(name, new Clip(name, fps, frames, cb, ck, values));
        }
        return model;
    }
}
