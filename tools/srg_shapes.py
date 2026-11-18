#!/usr/bin/env python3
"""2.5.1: the real shape of every obfuscated (SRG) member HexPiece's own classes reference, from MCPConfig's joined.tsrg for 1.20.1.

Why (the owner's crash on Ryou, 2.5.0): RyouGlow called RenderTarget.m_83961_(int, int, boolean) for blitToScreen; in 1.20.1 m_83961_
is RenderTarget's c (Z)V and blitToScreen(int, int, boolean) is m_83957_. The stub and the allowlist both had the wrong name, so the
build passed and the game threw NoSuchMethodError. Each SRG id names exactly one member, so a reference whose descriptor differs from
the id's own in joined.tsrg is wrong. This writes verify/srg-shapes.txt (each referenced SRG method's descriptor as joined.tsrg has it,
obfuscated; each referenced field), and build.py (check_srg_shapes) checks every reference against it, offline.

Run it when build.py says a reference is not in the file (needs GitHub raw files, or a joined.tsrg given):
  python3 tools/srg_shapes.py [path/to/joined.tsrg]      (after a build: it reads build/classes)
"""
import os
import re
import subprocess
import sys
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
URL = "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/1.20.1/joined.tsrg"
OUT = os.path.join(ROOT, "verify", "srg-shapes.txt")
REF = re.compile(r"// (?:Method|InterfaceMethod|Field) ([\w/$]+)\.(m_\d+_|f_\d+_):(\S+)")


def references(classes):
    """Every SRG member referenced by HexPiece's classes under classes: {(name, descriptor)}."""
    files = []
    for base, _, names in os.walk(os.path.join(classes, "com", "hexhaki", "piece")):
        files += [os.path.join(base, n) for n in names if n.endswith(".class")]
    found = set()
    for i in range(0, len(files), 200):
        out = subprocess.run(["javap", "-c", "-p"] + files[i:i + 200], capture_output=True, text=True, check=True).stdout
        for _, name, desc in REF.findall(out):
            found.add((name, desc))
    return found


def shape(desc):
    return re.sub(r"L[^;]+;", "L;", desc)


def main():
    text = open(sys.argv[1], encoding="utf-8").read() if len(sys.argv) > 1 else urllib.request.urlopen(URL).read().decode("utf-8")
    methods = {}
    fields = set()
    for line in text.splitlines():
        if line.startswith("\t") and not line.startswith("\t\t"):
            parts = line.strip().split(" ")
            if len(parts) == 4 and parts[2].startswith("m_"):
                methods.setdefault(parts[2], set()).add(parts[1])
            elif len(parts) == 3 and parts[1].startswith("f_"):
                fields.add(parts[1])
    refs = references(os.path.join(ROOT, "build", "classes"))
    lines = []
    wrong = []
    for name in sorted({n for n, _ in refs}):
        if name.startswith("m_"):
            if name not in methods:
                wrong.append(f"{name}: no such method in joined.tsrg")
                continue
            lines += [f"{name} {d}" for d in sorted(methods[name])]
            for n, d in refs:
                if n == name and shape(d) not in {shape(x) for x in methods[name]}:
                    wrong.append(f"{name} {d}: joined.tsrg has {', '.join(sorted(methods[name]))}")
        else:
            if name not in fields:
                wrong.append(f"{name}: no such field in joined.tsrg")
                continue
            lines.append(name)
    with open(OUT, "w", encoding="utf-8") as handle:
        handle.write("# SRG members HexPiece references, as MCPConfig's 1.20.1 joined.tsrg has them (method: id, obfuscated descriptor; "
                     "field: id). Written by tools/srg_shapes.py; checked by build.py.\n")
        handle.write("\n".join(lines) + "\n")
    print(f"{OUT}: {len(lines)} members")
    for w in wrong:
        print("WRONG", w)
    sys.exit(1 if wrong else 0)


if __name__ == "__main__":
    main()
