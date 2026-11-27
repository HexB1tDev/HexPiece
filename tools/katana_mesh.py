#!/usr/bin/env python3
"""2.1.4: the owner's katanas (input/Hex_Katana_Atelier_1.20.1.zip, "Hex Katana Atelier", 25 blades and 25 saya as OBJ meshes with one
512x512 atlas each) packed for the game. Standard library only (CI installs nothing): build.py calls pack() for every model.

The mesh is kept whole (the owner: "Preserve the detailed geometry rather than reducing the models to flat item sprites"): every
vertex, face and named part (01_Blade_forged_shinogi, 04_Tsuba_..., 13_Tsukamaki_..., 21_Saya_..., ...). Format (gzip, little
endian), read by client/sword/KatanaMesh.java:
  b"HKM2", int32 version (2) (2.7.4; b"HKM1" before: the vertices as 14-byte records, still read by decode)
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
    """2.7.4: format 2 (HKM2): the same header and groups, then the columns as byte planes (the owner: "reduce size"): each of x, y,
    z, u, v as the zigzag of its difference from the vertex before, in three planes (low, middle, high byte), then the normals' x, y, z
    (one byte each), then the quads' corners (flattened) as zigzag differences in three planes. Neighbouring vertices are near, so the
    planes are mostly small numbers and zeros: 15.1 MB of meshes pack into about 6, exactly the same geometry."""
    lo = [min(v[i] for v in vertices) for i in range(3)]
    hi = [max(v[i] for v in vertices) for i in range(3)]
    offset = [(lo[i] + hi[i]) / 2.0 for i in range(3)]
    scale = [max(1e-6, (hi[i] - lo[i]) / 2.0) for i in range(3)]
    if len(vertices) > 65535:
        raise ValueError(f"{len(vertices)} vertices: too many for 16-bit indices")
    out = io.BytesIO()
    out.write(b"HKM2")
    out.write(struct.pack("<i3f3f3i", 2, *offset, *scale, len(vertices), len(quads), len(groups)))
    for name, first, count in groups:
        raw = name.encode("utf-8")
        out.write(struct.pack("<H", len(raw)))
        out.write(raw)
        out.write(struct.pack("<ii", first, count))
    clamp = lambda value, a, b: max(a, min(b, value))
    columns = [[], [], [], [], []]
    normals = [[], [], []]
    for x, y, z, u, v, nx, ny, nz in vertices:
        q = [round((c - offset[i]) / scale[i] * 32767) for i, c in enumerate((x, y, z))]
        for k, value in enumerate([clamp(c, -32767, 32767) for c in q] + [clamp(round(u * 65535), 0, 65535), clamp(round(v * 65535), 0, 65535)]):
            columns[k].append(value)
        for k, n in enumerate((nx, ny, nz)):
            normals[k].append(clamp(round(n * 127), -127, 127) & 0xFF)
    for column in columns:
        out.write(planes(column))
    for column in normals:
        out.write(bytes(column))
    out.write(planes([c for quad in quads for c in quad]))
    return gzip.compress(out.getvalue(), compresslevel=9, mtime=0)


def planes(values):
    """The zigzag of each value's difference from the one before, as three byte planes (low, middle, high)."""
    lo, mid, hi = bytearray(), bytearray(), bytearray()
    previous = 0
    for value in values:
        d = value - previous
        previous = value
        z = (d << 1) ^ (d >> 31)
        z &= 0xFFFFFFFF
        lo.append(z & 0xFF)
        mid.append(z >> 8 & 0xFF)
        hi.append(z >> 16 & 0xFF)
    return bytes(lo + mid + hi)


def unplanes(raw, at, count):
    """{@link planes} undone: the values, and where the planes end."""
    values = []
    previous = 0
    for i in range(count):
        z = raw[at + i] | raw[at + count + i] << 8 | raw[at + 2 * count + i] << 16
        d = (z >> 1) ^ -(z & 1)
        previous += d
        values.append(previous)
    return values, at + 3 * count


def decode(data):
    """A packed mesh back as (vertices, quads, groups), for the off-game previews and the build's checks (format 1 or 2)."""
    raw = gzip.decompress(data)
    assert raw[:4] in (b"HKM1", b"HKM2")
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
    if raw[:4] == b"HKM2":
        columns = []
        for _ in range(5):
            column, at = unplanes(raw, at, nv)
            columns.append(column)
        normals = []
        for _ in range(3):
            normals.append([b - 256 if b > 127 else b for b in raw[at:at + nv]])
            at += nv
        flat, at = unplanes(raw, at, nq * 4)
        vertices = [(ox + columns[0][i] / 32767 * sx, oy + columns[1][i] / 32767 * sy, oz + columns[2][i] / 32767 * sz,
                     columns[3][i] / 65535, columns[4][i] / 65535, normals[0][i] / 127, normals[1][i] / 127, normals[2][i] / 127)
                    for i in range(nv)]
        quads = [tuple(flat[i * 4:i * 4 + 4]) for i in range(nq)]
        return vertices, quads, groups
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


# 2.1.4: levels of detail for a katana seen from afar (vertex clustering per part, hard edges kept by bucketing the normals; the owner's
# full mesh is untouched and drawn whenever it is close): cell size (model px; the blade's half of it), normal buckets per axis
LODS = {"mid": (0.22, 1.0), "far": (0.36, 0.8)}


def outer_only(vertices, quads, groups):
    """The mesh without the hollow saya's inner wall (faces of a "hollow" part whose normal points back toward the tube's middle, found
    per slice along it): nothing shows it from a distance, and merged with the outer wall a few tenths of a pixel away it would fight
    it for the depth (dark specks over the saya)."""
    out_q, out_g = [], []
    for name, first, count in groups:
        start = len(out_q)
        part = quads[first:first + count]
        if "hollow" not in name:
            out_q.extend(part)
            out_g.append((name, start, len(part)))
            continue
        slices = {}
        for quad in part:
            for vi in quad:
                x, y, z = vertices[vi][:3]
                acc = slices.setdefault(round(y / 0.5), [0.0, 0.0, 0])
                acc[0] += x
                acc[1] += z
                acc[2] += 1
        for quad in part:
            cx = sum(vertices[vi][0] for vi in quad) / len(quad)
            cy = sum(vertices[vi][1] for vi in quad) / len(quad)
            cz = sum(vertices[vi][2] for vi in quad) / len(quad)
            nx = sum(vertices[vi][5] for vi in quad)
            nz = sum(vertices[vi][7] for vi in quad)
            acc = slices.get(round(cy / 0.5))
            if acc is not None and acc[2]:
                ox = cx - acc[0] / acc[2]
                oz = cz - acc[1] / acc[2]
                if nx * ox + nz * oz < 0.0:
                    continue  # it faces into the tube: the inner wall
            out_q.append(quad)
        out_g.append((name, start, len(out_q) - start))
    return out_q, out_g


def simplify(vertices, quads, groups, cell, buckets):
    """The mesh with every part's vertices merged per grid cell; faces that collapse are dropped, duplicates removed; the parts keep
    their names and order.

    2.1.9 (the owner: "some holsters are seethrough"): every vertex of a cell takes the SAME position (the cell's average over all its
    vertices), so the surface stays closed. Before, vertices were merged per cell and normal bucket, each bucket averaging its own
    position, and on a smooth curved saya the buckets' averages drifted apart: slits opened along the tube where the normal crossed a
    bucket's edge, and the saya's inside and the world behind showed through them. The normal bucket still keeps apart what faces
    opposite ways (the saya's inner and outer walls, a few tenths of a pixel apart) for their normals and uvs."""
    import math
    quads, groups = outer_only(vertices, quads, groups)
    positions = {}
    for name, first, count in groups:
        size = cell * (0.5 if name.startswith("01_Blade") else 1.0)
        for quad in quads[first:first + count]:
            for vi in quad:
                x, y, z = vertices[vi][:3]
                key = (name, round(x / size), round(y / size), round(z / size))
                acc = positions.setdefault(key, [0.0, 0.0, 0.0, 0])
                acc[0] += x
                acc[1] += y
                acc[2] += z
                acc[3] += 1
    out_v, index, out_q, out_g = [], {}, [], []
    seen = set()
    for name, first, count in groups:
        start = len(out_q)
        size = cell * (0.5 if name.startswith("01_Blade") else 1.0)
        for quad in quads[first:first + count]:
            ids = []
            cells = []
            for vi in quad:
                x, y, z, u, v, nx, ny, nz = vertices[vi]
                where = (name, round(x / size), round(y / size), round(z / size))
                key = where + (round(nx * buckets), round(ny * buckets), round(nz * buckets))
                if key not in index:
                    index[key] = len(out_v)
                    out_v.append([where, 0.0, 0.0, 0.0, 0.0, 0.0, 0])
                k = index[key]
                acc = out_v[k]
                acc[1] += u
                acc[2] += v
                acc[3] += nx
                acc[4] += ny
                acc[5] += nz
                acc[6] += 1
                ids.append(k)
                cells.append(where)
            # a face whose corners fall in fewer than three cells has collapsed (its corners now share positions)
            if len(set(cells)) < 3:
                continue
            uniq = list(dict.fromkeys(ids))
            if len(uniq) < 3:
                continue
            key = tuple(sorted(uniq))
            if key in seen:
                continue
            seen.add(key)
            out_q.append(tuple(uniq) if len(uniq) == 4 else (uniq[0], uniq[1], uniq[2], uniq[2]))
        out_g.append((name, start, len(out_q) - start))
    verts = []
    for where, u, v, nx, ny, nz, n in out_v:
        p = positions[where]
        n = n or 1.0
        length = math.sqrt(nx * nx + ny * ny + nz * nz) or 1.0
        verts.append((p[0] / p[3], p[1] / p[3], p[2] / p[3], u / n, v / n, nx / length, ny / length, nz / length))
    return verts, out_q, out_g


def pack(zip_path):
    """{model name: packed mesh bytes} for every model, and its levels of detail (<name>_mid, <name>_far)."""
    out = {}
    for name, text in models(zip_path):
        mesh = parse(text)
        out[name] = encode(*mesh)
        for lod, (cell, buckets) in LODS.items():
            out[f"{name}_{lod}"] = encode(*simplify(*mesh, cell, buckets))
    return out


# 2.2.7: the owner's two legendary blades (input/Yoru_Gryphon_Models_Textures_1.20.1.zip: "Add these 2 blades to the game ... Gryphon sits
# where katanas are but it's a saber, Yoru is big so it's on the back of the player, it's roughly the size of an entire player"). Their OBJs
# are Forge item models in blocks (Gryphon 2.4 long, Yoru 3.05), not the atelier's pixels: each is taken into the katanas' frame (model
# px, the guard at y 0, the blade up +y, the spine and the point's curve to +x, the edge to -x), scaled to its size in the game (Gryphon
# 1.78 blocks as the owner's own item display holds it, about a katana; Yoru 2.75, half as tall again as a player: the owner, of a first
# Yoru as tall as a player: "Yoru IS way bigger than that"). Gryphon curves the other way:
# mirrored (its knucklebow then guards the edge side, as a saber's does). Its scabbard is a separate model: moved so its throat meets the
# bell guard. Every blade part is renamed 01_Blade_... (the game's highlight and the LODs know a blade by that).
LEGENDS_PREFIX = "assets/hexpiece/"
LEGENDS = {
    "gryphon": {"scale": 1.775 * 16 / 2.397, "centre": (0.4965, 0.775, 0.5), "mirror": True},
    "gryphon_sheath": {"scale": 1.775 * 16 / 2.397, "centre": (0.4965, 0.775 - 1.232, 0.5), "mirror": True},
    "yoru": {"scale": 2.75 * 16 / 3.053, "centre": (0.5, 0.822, 0.5), "mirror": False},
}
LEGEND_BLADES = {"Saber_blade": "01_Blade_saber", "Black_blade": "01_Blade_black"}


def legend_models(zip_path):
    """[(model name, obj text)] of the owner's legendary blades."""
    with zipfile.ZipFile(zip_path) as z:
        return [(name, z.read(f"{LEGENDS_PREFIX}models/item/{name}.obj").decode("utf-8")) for name in LEGENDS]


def legend_textures(zip_path):
    """{blade name: png bytes} of the legendary blades' atlases."""
    with zipfile.ZipFile(zip_path) as z:
        return {name: z.read(f"{LEGENDS_PREFIX}textures/item/{name}.png") for name in ("gryphon", "yoru")}


def normalise(mesh, spec):
    """A legendary blade's parsed mesh in the katanas' frame (see LEGENDS)."""
    vertices, quads, groups = mesh
    k = spec["scale"]
    cx, cy, cz = spec["centre"]
    flip = -1.0 if spec["mirror"] else 1.0
    out = [((x - cx) * k * flip, (y - cy) * k, (z - cz) * k, u, v, nx * flip, ny, nz) for x, y, z, u, v, nx, ny, nz in vertices]
    if spec["mirror"]:
        # a mirror turns every face inside out: wind them back (the triangles' doubled corner stays last)
        quads = [(q[2], q[1], q[0], q[0]) if q[2] == q[3] else (q[3], q[2], q[1], q[0]) for q in quads]
    groups = [(LEGEND_BLADES.get(name, name), first, count) for name, first, count in groups]
    return out, quads, groups


def legend_meshes(zip_path):
    """{name: (vertices, quads, groups)} of the legendary blades, in the katanas' frame."""
    return {name: normalise(parse(text), LEGENDS[name]) for name, text in legend_models(zip_path)}


def pack_legends(zip_path):
    """{model name: packed mesh bytes} for the legendary blades and their levels of detail."""
    out = {}
    for name, mesh in legend_meshes(zip_path).items():
        out[name] = encode(*mesh)
        for lod, (cell, buckets) in LODS.items():
            out[f"{name}_{lod}"] = encode(*simplify(*mesh, cell, buckets))
    return out


# 3.8.7: the owner's mace (First_mace.blend), baked by tools/mace_bake.py into art/mace/ in the katanas' frame already (model px,
# the grip's top at y 0): packed as the legendary blades are, with its levels of detail
MACES = {"iron_mace": os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "art", "mace")}


def mace_meshes():
    """{name: (vertices, quads, groups)} of the maces, in the katanas' frame."""
    out = {}
    for name, folder in MACES.items():
        with open(os.path.join(folder, name + ".obj"), encoding="utf-8") as h:
            out[name] = parse(h.read())
    return out


def mace_textures():
    """{name: png bytes} of the maces' atlases."""
    out = {}
    for name, folder in MACES.items():
        with open(os.path.join(folder, name + ".png"), "rb") as h:
            out[name] = h.read()
    return out


def pack_maces():
    """{model name: packed mesh bytes} for the maces and their levels of detail."""
    out = {}
    for name, mesh in mace_meshes().items():
        out[name] = encode(*mesh)
        for lod, (cell, buckets) in LODS.items():
            out[f"{name}_{lod}"] = encode(*simplify(*mesh, cell, buckets))
    return out


# 3.8.8: the owner's gun (First_gun.blend), baked by tools/gun_bake.py into art/gun/ in the guns' own frame (model px, the grip's pivot
# at 0, the barrel forward -z, up +y): the flintlock with its levels of detail and the lead ball (every gun's bullet), on one atlas
GUN_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "art", "gun")
GUNS = ("flintlock",)
BULLET = "lead_ball"


def gun_meshes():
    """{name: (vertices, quads, groups)} of the guns and the bullet, in the guns' frame."""
    out = {}
    for name in GUNS + (BULLET,):
        with open(os.path.join(GUN_DIR, name + ".obj"), encoding="utf-8") as h:
            out[name] = parse(h.read())
    return out


def gun_texture():
    """The guns' atlas (png bytes): every gun's and the bullet's toon ramps."""
    with open(os.path.join(GUN_DIR, "gun.png"), "rb") as h:
        return h.read()


def pack_guns():
    """{model name: packed mesh bytes} for the guns (with their levels of detail) and the bullet."""
    out = {}
    for name, mesh in gun_meshes().items():
        out[name] = encode(*mesh)
        if name != BULLET:
            for lod, (cell, buckets) in LODS.items():
                out[f"{name}_{lod}"] = encode(*simplify(*mesh, cell, buckets))
    return out


# 3.8.9: the owner's clothes (Clothes_Replacements.blend), baked by tools/clothes_bake.py into art/clothes/: one mesh an armor piece, its
# faces grouped by the body part that carries them (each in that part's frame), all on one atlas
CLOTHES_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "art", "clothes")
CLOTHES = ("marine_cap", "marine_shirt", "marine_trousers", "marine_boots", "admiral_coat", "straw_hat", "blackbeard_coat")


def clothes_meshes():
    """{piece: (vertices, quads, groups)} of the clothes."""
    out = {}
    for name in CLOTHES:
        with open(os.path.join(CLOTHES_DIR, name + ".obj"), encoding="utf-8") as h:
            out[name] = parse(h.read())
    return out


def clothes_texture():
    """The clothes' atlas (png bytes)."""
    with open(os.path.join(CLOTHES_DIR, "clothes.png"), "rb") as h:
        return h.read()


def pack_clothes():
    """{model name: packed mesh bytes} for every piece. 3.9.3: no levels of detail (simplify averages the texture coordinates of the
    vertices it merges; the clothes' atlas is made of separate patches, so a merged face sampled other patches: the owner's "The coat's
    textures are completely fucked")."""
    return {name: encode(*mesh) for name, mesh in clothes_meshes().items()}


if __name__ == "__main__":
    out = sys.argv[2]
    os.makedirs(out, exist_ok=True)
    for name, data in pack(sys.argv[1]).items():
        with open(os.path.join(out, name + ".hkm"), "wb") as handle:
            handle.write(data)
        print(name, len(data) // 1024, "KB")
