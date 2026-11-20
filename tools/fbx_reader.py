"""2.7.0: a minimal reader of binary FBX files (7.x): the node tree with its properties and arrays (zlib ones inflated). Used by
tools/dragon_mesh.py; reads, never runs anything from the file."""
import struct, zlib, sys

class Node:
    __slots__ = ("name", "props", "children")
    def __init__(self, name, props, children):
        self.name, self.props, self.children = name, props, children
    def find(self, name):
        for c in self.children:
            if c.name == name:
                return c
        return None
    def findall(self, name):
        return [c for c in self.children if c.name == name]
    def __repr__(self):
        return f"Node({self.name}, {len(self.props)} props, {len(self.children)} children)"

ARR = {b'f': ('f', 4), b'd': ('d', 8), b'l': ('q', 8), b'i': ('i', 4), b'b': ('b', 1)}

def read_prop(data, off):
    t = data[off:off + 1]; off += 1
    if t == b'Y': return struct.unpack_from('<h', data, off)[0], off + 2
    if t == b'C': return bool(data[off]), off + 1
    if t == b'I': return struct.unpack_from('<i', data, off)[0], off + 4
    if t == b'F': return struct.unpack_from('<f', data, off)[0], off + 4
    if t == b'D': return struct.unpack_from('<d', data, off)[0], off + 8
    if t == b'L': return struct.unpack_from('<q', data, off)[0], off + 8
    if t in (b'S', b'R'):
        n = struct.unpack_from('<I', data, off)[0]; off += 4
        raw = data[off:off + n]; off += n
        if t == b'S':
            try: return raw.decode('utf-8', 'replace'), off
            except Exception: return raw, off
        return raw, off
    if t.lower() in ARR or t in ARR:
        code, size = ARR[t]
        length, enc, clen = struct.unpack_from('<III', data, off); off += 12
        raw = data[off:off + clen]; off += clen
        if enc == 1: raw = zlib.decompress(raw)
        import numpy as np
        dt = {'f': '<f4', 'd': '<f8', 'q': '<i8', 'i': '<i4', 'b': 'i1'}[code]
        return np.frombuffer(raw, dtype=dt, count=length), off
    raise ValueError(f"bad prop type {t!r} at {off}")

def read_node(data, off, v64):
    if v64:
        end, nprops, plen = struct.unpack_from('<QQQ', data, off); off += 24
    else:
        end, nprops, plen = struct.unpack_from('<III', data, off); off += 12
    nlen = data[off]; off += 1
    name = data[off:off + nlen].decode('ascii', 'replace'); off += nlen
    if end == 0:
        return None, off
    props = []
    for _ in range(nprops):
        p, off = read_prop(data, off)
        props.append(p)
    children = []
    sentinel = 25 if v64 else 13
    while off < end:
        if end - off == sentinel and data[off:end] == b'\0' * sentinel:
            off = end; break
        c, off = read_node(data, off, v64)
        if c is None: break
        children.append(c)
    return Node(name, props, children), end

def load(path):
    return load_bytes(open(path, 'rb').read())


def load_bytes(data):
    assert data[:21] == b'Kaydara FBX Binary  \0', data[:23]
    version = struct.unpack_from('<I', data, 23)[0]
    v64 = version >= 7500
    off = 27
    top = []
    while off < len(data):
        n, off2 = read_node(data, off, v64)
        if n is None: break
        top.append(n); off = off2
    return version, Node('root', [], top)

def dump(node, depth=0, maxdepth=3, out=sys.stdout):
    def short(p):
        if hasattr(p, 'shape'): return f"arr[{p.shape[0]}]"
        if isinstance(p, bytes): return f"b{len(p)}"
        s = repr(p)
        return s[:60]
    out.write("  " * depth + node.name + " " + " ".join(short(p) for p in node.props) + "\n")
    if depth < maxdepth:
        for c in node.children:
            dump(c, depth + 1, maxdepth, out)
