#!/usr/bin/env python3
"""2.1.4: the owner's katanas (input/Hex_Katana_Atelier_1.20.1.zip, "Hex Katana Atelier", 25 blades and 25 saya as OBJ meshes with one
512x512 atlas each) packed for the game. Standard library only (CI installs nothing): build.py calls pack() for every model.

The mesh is kept whole (the owner: "Preserve the detailed geometry rather than reducing the models to flat item sprites"): every
vertex, face and named part (01_Blade_forged_shinogi, 04_Tsuba_..., 13_Tsukamaki_..., 21_Saya_..., ...). Format (gzip, little
endian), read by client/sword/KatanaMesh.java:
  b"HKM1", int32 version (1)
  float32 offset[3], float32 scale[3]          position = offset + q / 32767 * scale (model pixels: 1 = 1/16 block; the guard at y 0,
                                               the blade up +y, its edge to +x, the handle down to -y)
  int32 vertices, int32 quads, int32 groups
  groups:   uint16 name length, name (utf-8), int32 first quad, int32 quad count
  vertices: int16 x, y, z; uint16 u, v (0..65535, v from the top of the image); int8 nx, ny, nz; int8 0     (14 bytes)
  quads:    uint16 a, b, c, d (a triangle repeats its last corner)
Corners that share position, uv and normal are one vertex. Faces stay in the file's order, grouped by part in the order parts first
appear.

Usage: python3 tools/katana_mesh.py <zip> <out dir>   (writes <name>.hkm for every model)
"""
import gzip
import io
import os
import struct
import sys
import zipfile

PREFIX = "Hex_Katana_Atelier_1.20.1/"


def parse(text):
    """The OBJ as (vertices [(x, y, z, u, v, nx, ny, nz)], quads [(a, b, c, d)], groups [(name, first, count)])."""
    positions, uvs, normals = [], [], []
    group_faces = {}
    order = []
    current = "default"
    for line in text.splitlines():
        if not line or line[0] == "#":
            continue
        tag, _, rest = line.partition(" ")
        if tag == "v":
            positions.append(tuple(float(t) for t in rest.split()[:3]))
        elif tag == "vt":
            p = rest.split()
            uvs.append((float(p[0]), float(p[1])))
        elif tag == "vn":
            normals.append(tuple(float(t) for t in rest.split()[:3]))
        elif tag == "g" or tag == "o":
            current = rest.strip() or "default"
        elif tag == "f":
            if current not in group_faces:
                group_faces[current] = []
                order.append(current)
            corners = []
            for token in rest.split():
                parts = token.split("/")
                corners.append((int(parts[0]) - 1, int(parts[1]) - 1 if len(parts) > 1 and parts[1] else -1,
                                int(parts[2]) - 1 if len(parts) > 2 and parts[2] else -1))
            if len(corners) == 3:
                corners.append(corners[2])
            elif len(corners) > 4:
                # a polygon: fanned into quads (a, i, i+1, i+2)
                for i in range(1, len(corners) - 2, 2):
                    fan = [corners[0], corners[i], corners[i + 1], corners[min(i + 2, len(corners) - 1)]]
                    group_faces[current].append(fan)
                continue
            group_faces[current].append(corners)
    vertices, index, quads, groups = [], {}, [], []
    for name in order:
        first = len(quads)
        for face in group_faces[name]:
            quad = []
            for p, t, n in face:
                key = (p, t, n)
                if key not in index:
                    x, y, z = positions[p]
                    u, v = uvs[t] if t >= 0 else (0.0, 0.0)
                    nx, ny, nz = normals[n] if n >= 0 else (0.0, 1.0, 0.0)
                    index[key] = len(vertices)
                    vertices.append((x, y, z, u, 1.0 - v, nx, ny, nz))
                quad.append(index[key])
            quads.append(tuple(quad))
        groups.append((name, first, len(quads) - first))
    return vertices, quads, groups


def encode(vertices, quads, groups):
    lo = [min(v[i] for v in vertices) for i in range(3)]
    hi = [max(v[i] for v in vertices) for i in range(3)]
    offset = [(lo[i] + hi[i]) / 2.0 for i in range(3)]
    scale = [max(1e-6, (hi[i] - lo[i]) / 2.0) for i in range(3)]
    if len(vertices) > 65535:
        raise ValueError(f"{len(vertices)} vertices: too many for 16-bit indices")
    out = io.BytesIO()
    out.write(b"HKM1")
    out.write(struct.pack("<i3f3f3i", 1, *offset, *scale, len(vertices), len(quads), len(groups)))
    for name, first, count in groups:
        raw = name.encode("utf-8")
        out.write(struct.pack("<H", len(raw)))
        out.write(raw)
        out.write(struct.pack("<ii", first, count))
    clamp = lambda value, a, b: max(a, min(b, value))
    for x, y, z, u, v, nx, ny, nz in vertices:
        q = [round((c - offset[i]) / scale[i] * 32767) for i, c in enumerate((x, y, z))]
        out.write(struct.pack("<3h2H4b", *[clamp(c, -32767, 32767) for c in q], clamp(round(u * 65535), 0, 65535),
                              clamp(round(v * 65535), 0, 65535), clamp(round(nx * 127), -127, 127), clamp(round(ny * 127), -127, 127),
                              clamp(round(nz * 127), -127, 127), 0))
    for quad in quads:
        out.write(struct.pack("<4H", *quad))
    return gzip.compress(out.getvalue(), compresslevel=9, mtime=0)


def decode(data):
    """A packed mesh back as (vertices, quads, groups), for the off-game previews and the build's checks."""
    raw = gzip.decompress(data)
    assert raw[:4] == b"HKM1"
    version, ox, oy, oz, sx, sy, sz, nv, nq, ng = struct.unpack_from("<i3f3f3i", raw, 4)
    at = 4 + struct.calcsize("<i3f3f3i")
    groups = []
    for _ in range(ng):
        (length,) = struct.unpack_from("<H", raw, at)
        at += 2
        name = raw[at:at + length].decode("utf-8")
        at += length
        first, count = struct.unpack_from("<ii", raw, at)
        at += 8
        groups.append((name, first, count))
    vertices = []
    for _ in range(nv):
        x, y, z, u, v, nx, ny, nz, _pad = struct.unpack_from("<3h2H4b", raw, at)
        at += 14
        vertices.append((ox + x / 32767 * sx, oy + y / 32767 * sy, oz + z / 32767 * sz, u / 65535, v / 65535, nx / 127, ny / 127, nz / 127))
    quads = [struct.unpack_from("<4H", raw, at + i * 8) for i in range(nq)]
    return vertices, quads, groups


def models(zip_path):
    """[(model name, obj text)] of every OBJ in the owner's zip."""
    with zipfile.ZipFile(zip_path) as z:
        names = sorted(n for n in z.namelist() if n.startswith(PREFIX + "models/obj/") and n.endswith(".obj"))
        return [(os.path.basename(n)[:-4], z.read(n).decode("utf-8")) for n in names]


def textures(zip_path):
    """{katana name: png bytes} of every atlas in the owner's zip."""
    with zipfile.ZipFile(zip_path) as z:
        return {os.path.basename(n)[:-4]: z.read(n) for n in z.namelist() if n.startswith(PREFIX + "textures/") and n.endswith(".png")}


def pack(zip_path):
    """{model name: packed mesh bytes} for every model."""
    return {name: encode(*parse(text)) for name, text in models(zip_path)}


if __name__ == "__main__":
    out = sys.argv[2]
    os.makedirs(out, exist_ok=True)
    for name, data in pack(sys.argv[1]).items():
        with open(os.path.join(out, name + ".hkm"), "wb") as handle:
            handle.write(data)
        print(name, len(data) // 1024, "KB")
