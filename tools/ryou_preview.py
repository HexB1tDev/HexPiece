#!/usr/bin/env python3
"""Renders Ryou's fists off-game to judge them (the house rule: judge every render yourself). 2.5.3: the game's own code does the work:
tools/preview/RyouPreview.java draws the player model's arm (every skin texel, bent by ArmBend, blackened and glossed by HandCoat) and
the aura exactly as haki/FistAura builds it (alpha blended, depth tested, no depth written), in several shots (the owner's picture angle,
side, bent elbow, first person, daylight, 8 blocks, the inventory), the flame over time, and close-ups beside the owner's target
(docs/handoff/ryou-fist-target.jpg). Run python3 build.py first (it compiles build/classes).

Usage: python3 tools/ryou_preview.py [out-dir]       (default build/ryou-preview)
"""
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "build", "ryou-preview")
    classes = os.path.join(ROOT, "build", "classes")
    preview = os.path.join(ROOT, "build", "preview")
    os.makedirs(preview, exist_ok=True)
    subprocess.run(["javac", "-cp", classes, "-d", preview, os.path.join(ROOT, "tools", "preview", "RyouPreview.java")], check=True)
    subprocess.run(["java", "-cp", preview + os.pathsep + classes, "RyouPreview", out], check=True, cwd=ROOT)


if __name__ == "__main__":
    main()
