package com.hexhaki.piece.client.sword;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * 2.1.4: one of the owner's katana or saya meshes, whole (every face of input/Hex_Katana_Atelier_1.20.1.zip's OBJ), as
 * tools/katana_mesh.py packs it ({@code assets/hexhaki/katana/<name>.hkm}): positions in model pixels (the guard at y 0, the blade up
 * +y, its edge to -x), uvs, normals, quads, and the named parts. Read once from the mod's own jar (a plain class-path resource, no
 * Minecraft API), on the client only.
 */
public final class KatanaMesh {
    final float[] pos;
    final float[] uv;
    final float[] nrm;
    final int[] quads;
    /** The quads of the blade (part 01_Blade...), drawn again for its highlight. */
    final int bladeFirst;
    final int bladeCount;

    private KatanaMesh(float[] pos, float[] uv, float[] nrm, int[] quads, int bladeFirst, int bladeCount) {
        this.pos = pos;
        this.uv = uv;
        this.nrm = nrm;
        this.quads = quads;
        this.bladeFirst = bladeFirst;
        this.bladeCount = bladeCount;
    }

    public int quadCount() {
        return this.quads.length / 4;
    }

    /**
     * 3.3.5 (the Supa Supa no Mi's blades, supa/SupaBlades): this mesh's steel alone (its 01_Blade quads), reshaped by {@code shape}
     * (wider about its own spine, thicker, its curve eased, its edge rolling): every 0.5 px band along the blade its spine (the most +x
     * of it) and its edge (the most -x). The uvs and normals are the steel's own, so it is textured and lit as the katana is. Made once
     * a shape (KatanaRenderer keeps it); null if the mesh has no steel.
     */
    static KatanaMesh shaped(KatanaMesh src, com.hexhaki.piece.supa.SupaBlades.Shape shape) {
        if (src.bladeCount <= 0) {
            return null;
        }
        java.util.Map<Integer, float[]> bands = new java.util.HashMap<>();
        int start = src.bladeFirst * 4;
        int end = (src.bladeFirst + src.bladeCount) * 4;
        for (int i = start; i < end; i++) {
            int v = src.quads[i];
            float x = src.pos[v * 3];
            int band = (int) Math.floor(src.pos[v * 3 + 1] / 0.5F);
            float[] b = bands.computeIfAbsent(band, k -> new float[] {-Float.MAX_VALUE, Float.MAX_VALUE});
            b[0] = Math.max(b[0], x);
            b[1] = Math.min(b[1], x);
        }
        float[] pos = src.pos.clone();
        boolean[] done = new boolean[src.pos.length / 3];
        // 3.4.2: the blade's tip (its furthest point along), where every Supa blade's point narrows to nothing
        float top = 0.0F;
        for (int i = start; i < end; i++) {
            top = Math.max(top, src.pos[src.quads[i] * 3 + 1]);
        }
        for (int i = start; i < end; i++) {
            int v = src.quads[i];
            if (done[v]) {
                continue;
            }
            done[v] = true;
            float x = src.pos[v * 3];
            float y = src.pos[v * 3 + 1];
            float[] b = bands.get((int) Math.floor(y / 0.5F));
            pos[v * 3] = shape.x(x, y, b[0], b[1], top);
            pos[v * 3 + 1] = shape.y(y, top);
            pos[v * 3 + 2] = shape.z(src.pos[v * 3 + 2], y, top);
        }
        int[] quads = java.util.Arrays.copyOfRange(src.quads, start, end);
        return new KatanaMesh(pos, src.uv, src.nrm, quads, 0, src.bladeCount);
    }

    /**
     * 3.3.5 (Spider: the body turned to steel): a box from (x0, y0, z0) to (x1, y1, z1) (model px) skinned with {@code src}'s steel: each
     * face the middle of the steel's texture (the blade's uvs, a stretch of it along the blade), its outward normal, all of it steel
     * (so the highlight runs over it as over a blade).
     */
    static KatanaMesh box(KatanaMesh src, float x0, float y0, float z0, float x1, float y1, float z1) {
        float u0 = Float.MAX_VALUE;
        float u1 = -Float.MAX_VALUE;
        float v0 = Float.MAX_VALUE;
        float v1 = -Float.MAX_VALUE;
        for (int i = src.bladeFirst * 4, end = (src.bladeFirst + src.bladeCount) * 4; i < end; i++) {
            int v = src.quads[i];
            u0 = Math.min(u0, src.uv[v * 2]);
            u1 = Math.max(u1, src.uv[v * 2]);
            v0 = Math.min(v0, src.uv[v * 2 + 1]);
            v1 = Math.max(v1, src.uv[v * 2 + 1]);
        }
        if (src.bladeCount <= 0) {
            u0 = 0;
            u1 = 1;
            v0 = 0;
            v1 = 1;
        }
        // a small window in the middle of the steel's flat (the long way of it along the blade: whichever of u, v spans more), clear of
        // the bevel's lines that striped the first skin
        boolean alongV = v1 - v0 >= u1 - u0;
        float a0 = alongV ? v0 + (v1 - v0) * 0.45F : u0 + (u1 - u0) * 0.45F;
        float a1 = alongV ? v0 + (v1 - v0) * 0.5F : u0 + (u1 - u0) * 0.5F;
        float b0 = alongV ? u0 + (u1 - u0) * 0.3F : v0 + (v1 - v0) * 0.3F;
        float b1 = alongV ? u0 + (u1 - u0) * 0.6F : v0 + (v1 - v0) * 0.6F;
        float[][] corners = {
            {x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] faces = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {4, 5, 1, 0}, {3, 2, 6, 7}};
        float[][] normals = {{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}};
        float[] pos = new float[24 * 3];
        float[] uv = new float[24 * 2];
        float[] nrm = new float[24 * 3];
        int[] quads = new int[24];
        float[][] corner = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        for (int f = 0; f < 6; f++) {
            for (int k = 0; k < 4; k++) {
                int v = f * 4 + k;
                System.arraycopy(corners[faces[f][k]], 0, pos, v * 3, 3);
                System.arraycopy(normals[f], 0, nrm, v * 3, 3);
                float s = corner[k][0];
                float t = corner[k][1];
                uv[v * 2] = alongV ? b0 + (b1 - b0) * s : a0 + (a1 - a0) * t;
                uv[v * 2 + 1] = alongV ? a0 + (a1 - a0) * t : b0 + (b1 - b0) * s;
                quads[v] = v;
            }
        }
        return new KatanaMesh(pos, uv, nrm, quads, 0, 6);
    }

    /** 2.7.4: {@code count} values from three byte planes (low, middle, high) of zigzag differences (tools/katana_mesh.py planes). */
    static int[] planes(ByteBuffer b, int count) {
        int at = b.position();
        int[] out = new int[count];
        int previous = 0;
        for (int i = 0; i < count; i++) {
            int z = Byte.toUnsignedInt(b.get(at + i)) | Byte.toUnsignedInt(b.get(at + count + i)) << 8
                | Byte.toUnsignedInt(b.get(at + 2 * count + i)) << 16;
            previous += (z >>> 1) ^ -(z & 1);
            out[i] = previous;
        }
        b.position(at + 3 * count);
        return out;
    }

    static KatanaMesh load(String name) throws IOException {
        byte[] packed;
        try (InputStream in = KatanaMesh.class.getResourceAsStream("/assets/hexhaki/katana/" + name + ".hkm")) {
            if (in == null) {
                throw new IOException("missing mesh " + name);
            }
            try (GZIPInputStream gz = new GZIPInputStream(in); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                gz.transferTo(out);
                packed = out.toByteArray();
            }
        }
        ByteBuffer b = ByteBuffer.wrap(packed).order(ByteOrder.LITTLE_ENDIAN);
        // 2.7.4: format 2 (tools/katana_mesh.py: the columns as zigzag differences in byte planes, 60 % smaller); 1 still read
        byte m0 = b.get();
        byte m1 = b.get();
        byte m2 = b.get();
        byte m3 = b.get();
        int version = b.getInt();
        if (m0 != 'H' || m1 != 'K' || m2 != 'M' || !(m3 == '1' && version == 1 || m3 == '2' && version == 2)) {
            throw new IOException("not a katana mesh: " + name);
        }
        float[] offset = {b.getFloat(), b.getFloat(), b.getFloat()};
        float[] scale = {b.getFloat(), b.getFloat(), b.getFloat()};
        int vertices = b.getInt();
        int quadCount = b.getInt();
        int groups = b.getInt();
        int bladeFirst = 0;
        int bladeCount = 0;
        for (int g = 0; g < groups; g++) {
            byte[] raw = new byte[Short.toUnsignedInt(b.getShort())];
            b.get(raw);
            String part = new String(raw, StandardCharsets.UTF_8);
            int first = b.getInt();
            int count = b.getInt();
            if (part.startsWith("01_Blade")) {
                bladeFirst = first;
                bladeCount = count;
            }
        }
        float[] pos = new float[vertices * 3];
        float[] uv = new float[vertices * 2];
        float[] nrm = new float[vertices * 3];
        int[] quads = new int[quadCount * 4];
        if (version == 2) {
            for (int k = 0; k < 5; k++) {
                int[] column = planes(b, vertices);
                for (int i = 0; i < vertices; i++) {
                    if (k < 3) {
                        pos[i * 3 + k] = offset[k] + column[i] / 32767.0F * scale[k];
                    } else {
                        uv[i * 2 + k - 3] = column[i] / 65535.0F;
                    }
                }
            }
            for (int k = 0; k < 3; k++) {
                for (int i = 0; i < vertices; i++) {
                    nrm[i * 3 + k] = b.get() / 127.0F;
                }
            }
            int[] flat = planes(b, quads.length);
            System.arraycopy(flat, 0, quads, 0, quads.length);
            return new KatanaMesh(pos, uv, nrm, quads, bladeFirst, bladeCount);
        }
        for (int i = 0; i < vertices; i++) {
            for (int k = 0; k < 3; k++) {
                pos[i * 3 + k] = offset[k] + b.getShort() / 32767.0F * scale[k];
            }
            uv[i * 2] = Short.toUnsignedInt(b.getShort()) / 65535.0F;
            uv[i * 2 + 1] = Short.toUnsignedInt(b.getShort()) / 65535.0F;
            for (int k = 0; k < 3; k++) {
                nrm[i * 3 + k] = b.get() / 127.0F;
            }
            b.get();
        }
        for (int i = 0; i < quads.length; i++) {
            quads[i] = Short.toUnsignedInt(b.getShort());
        }
        return new KatanaMesh(pos, uv, nrm, quads, bladeFirst, bladeCount);
    }
}
