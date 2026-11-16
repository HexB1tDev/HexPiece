#!/usr/bin/env python3
"""Prints the version after build.py's VERSION by the owner's rule: after x.y.9 comes x.(y+1).0, after x.9.9 comes (x+1).0.0.
With --write, also sets it in build.py."""
import re
import sys
from pathlib import Path

BUILD = Path(__file__).resolve().parent.parent / "build.py"


def next_version(version):
    major, minor, patch = (int(p) for p in version.split("."))
    patch += 1
    if patch > 9:
        patch, minor = 0, minor + 1
    if minor > 9:
        minor, major = 0, major + 1
    return f"{major}.{minor}.{patch}"


def main():
    text = BUILD.read_text(encoding="utf-8")
    current = re.search(r'VERSION = "(\d+\.\d+\.\d+)"', text).group(1)
    upcoming = next_version(current)
    if "--write" in sys.argv:
        BUILD.write_text(text.replace(f'VERSION = "{current}"', f'VERSION = "{upcoming}"', 1), encoding="utf-8")
    print(upcoming)


if __name__ == "__main__":
    main()
