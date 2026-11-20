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
