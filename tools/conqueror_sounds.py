#!/usr/bin/env python3
"""2.9.4: the Conqueror's Haki release, every sound from the web (the owner: "Give Conqueror's Haiku release a new proper sound, like
even on like all paths, good, bad, normal. Give it a better sound like you gave Dragon. Go search around the web. Don't you dare create
one"). Every clip is a CC0 recording on Freesound.org (tools/web_sounds.py fetches, checks the licence, cuts, makes mono, normalises,
fades, writes Ogg Vorbis) into src/main/resources/assets/hexhaki/sounds/piece/conqueror/web/ with SOURCES.txt.

- The charge (the original's conqueror_charge event, now these): a power charge rising.
- The release (conqueror_release): a close lightning strike, one of three.
- The release's boom, the moment G is let go (piece.conqueror.explosion*): by the releaser's path: neutral a deep bass boom, good a
  dramatic trailer boom, evil a dark trailer impact.
- A full charge (conqueror_supreme_*): a world-ending explosion, the thunder's crack, rolling thunder after it.
Nothing is mixed or layered here: each file is one recording's own sound (the game plays the layers together).

Usage: python3 tools/conqueror_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/conqueror/web")

SOUNDS = {
    "power_charge": ("crashoverride6", 193610),     # "Power Charge", rated 4.8, 4.7k downloads
    "thunder_close": ("loganzsound", 840628),       # "Closeup Thunder Strike 01", rated 5.0
    "lightning_close": ("csengeri", 434359),        # "Close lightning strike 2018 07 07", rated 4.8, 5.8k downloads
    "thunder_extreme": ("TSP-Talk", 844420),        # "Extreme Thunder Impact - Munich Trudering", rated 4.8
    "rolling": ("lmbubec", 118806),                 # "16_Rolling_Thunder.wav", rated 4.4
    "world_ender": ("qubodup", 814057),             # "World Ender Explosion", rated 4.4
    "bass_boom": ("LordHannes", 268084),            # "Bass Boom", rated 4.5, 9.7k downloads
    "trailer_boom": ("modusmogulus", 785948),       # "Dramatic Trailer Boom with Tension", rated 4.9
    "dark_impact": ("AudioPapkin", 827561),         # "Dark Trailer Impact Sound Effect.", rated 4.7
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "charge": ("power_charge", 0.0, None, 0.0, 0.6),
    "thunder1": ("thunder_close", 0.5, 6.0, 0.0, 2.5),
    "thunder2": ("lightning_close", 2.9, 8.0, 0.0, 3.0),
    "thunder3": ("thunder_extreme", 0.6, 7.5, 0.0, 3.0),
    "crack": ("thunder_close", 0.5, 2.8, 0.0, 1.2),
    "rumble": ("rolling", 0.0, None, 0.3, 3.0),
    "world": ("world_ender", 0.0, None, 0.0, 2.0),
    "boom": ("bass_boom", 0.0, 6.0, 0.0, 2.5),
    "boom_good": ("trailer_boom", 0.0, None, 0.0, 0.8),
    "boom_evil": ("dark_impact", 10.55, 11.5, 0.0, 4.0),
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, {}, OUT, "The Conqueror's Haki release's sounds (tools/conqueror_sounds.py).", only)


if __name__ == "__main__":
    sys.exit(main())
