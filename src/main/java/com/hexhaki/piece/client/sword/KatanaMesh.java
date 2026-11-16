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
        if (b.get() != 'H' || b.get() != 'K' || b.get() != 'M' || b.get() != '1' || b.getInt() != 1) {
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
        int[] quads = new int[quadCount * 4];
        for (int i = 0; i < quads.length; i++) {
            quads[i] = Short.toUnsignedInt(b.getShort());
        }
        return new KatanaMesh(pos, uv, nrm, quads, bladeFirst, bladeCount);
    }
}
