#!/usr/bin/env python3
"""3.8.8: the guns' sounds (the owner's category Gun), CC0 recordings of real black-powder guns from Freesound.org: the shot (an old
musket's flint snapping and its charge going off, a hair apart, as a flintlock fires; and a second musket's clean bang), under a Haki
shot a deeper boom (a third musket's), the lock's hammer drawn back when the reload ends (an old flintlock's own click), the ball
pressed home (a ramrod in the barrel), the pistol spun round the finger (a gun spun in the hand). A ball striking keeps the clash
impacts the mod has (gun/GunSounds).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/gun/ with SOURCES.txt
naming every clip's origin. Each is measured as cut with tools/tonal_lines.py (no pitched line: the owner will not have melodic sounds).
The build never downloads.

Usage: python3 tools/gun_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/gun")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "musket": ("bruno.auzet", 538795),     # "old musket bang.wav": the flint's snap, then the charge
    "shot": ("mlsulli", 234869),           # "Musket Shot.wav"
    "boom": ("fennelliott", 347647),       # "musket.wav": a deep, long boom
    "lock": ("N3pomuk", 656397),           # "old flintlock primer gun clicking.flac"
    "ramrod": ("jriches1", 817931),        # "Ram rod slide out"
    "spin": ("GrayEpic", 697619),          # "L & R GUN SPIN And HOLSTER.mp3"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s, equaliser)
CLIPS = {
    # the shot: the charge going off (cut on it: the flint's snap 0.13 s before it would lag the muzzle's flash); a clean bang
    "shot1": ("musket", 0.255, 0.95, 0.002, 0.55, None),
    "shot2": ("shot", 0.70, 1.1, 0.002, 0.6, web_sounds.HUM),
    # under a Haki shot: the deep boom (its first, loudest part; the room's hum under it filtered out: web_sounds.HUM)
    "boom1": ("boom", 0.06, 1.4, 0.004, 0.9, web_sounds.HUM),
    # the hammer drawn back: one click of the old lock
    "cock1": ("lock", 4.93, 0.28, 0.001, 0.12, None),
    # the ball pressed home: the end of the rod's slide and its knock in the barrel 0.15 s in (played as the ball reaches the muzzle,
    # gun/GunAnims.BALL_AT, the knock as it goes home, BALL_HOME)
    "load1": ("ramrod", 0.53, 0.3, 0.01, 0.1, None),
    # spun round the finger
    "spin1": ("spin", 1.25, 0.85, 0.06, 0.3, web_sounds.HUM),
}
LOOPS = {}
LEVELS = {
    "shot1": (0.0, True),   # a gunshot: pushed
    "shot2": (0.0, True),
    "boom1": (-2.0, True),
    "spin1": (-4.0, False),
    "load1": (-2.0, False),
}


def level(name):
    return LEVELS.get(name, (0.0, False))


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The guns' sounds (tools/gun_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
