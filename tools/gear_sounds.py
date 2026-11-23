#!/usr/bin/env python3
"""3.2.3: the Nika fruit's transformation drums, every one from the web (the owner: "Don't you dare create one ... Go search around the
web. Get proper sounds in."; then, on Gear 5's transformation: "if you could give the transformation just the drum sounds and just get
rid of the music because of copyright, that would be perfect"): CC0 recordings from Freesound.org, the drums of liberation as the chest
is pounded: a deep tribal floor tom for each beat, one heavy cinematic drum hit for the last. 3.3.9: Dawn Pistol's cartoon punch (the
owner accepted "a new CC0 punch sound from the web"; broadband, +3 dB over plain noise in tools/tonal_lines.py).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/gear/ with
SOURCES.txt naming every clip's origin. The build never downloads. A beat rings about a second and a half: never past the
transformation.

Usage: python3 tools/gear_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/gear")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "floor_tom": ("hello_flowers", 30822),               # "Tomtom6_TribalFloor.wav"
    "heavy_drum": ("choomaque-crispydinner", 577408),    # "crdn_cinematic_heavy_drums - 14.wav"
    "cartoon_punch": ("modusmogulus", 785936),           # "Cartoon Punch Cassette F" (3.3.9: Dawn Pistol's POW)
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "drum1": ("floor_tom", 0.0, 1.5, 0.0, 0.6),
    "drum2": ("heavy_drum", 0.0, None, 0.0, 0.3),
    "dawn_pistol1": ("cartoon_punch", 0.0, None, 0.0, 0.05),
}

LOOPS = {}


def level(name):
    """(gain in dB below full scale, compressed louder): the beats are the transformation's only sound, loud and punchy; Dawn Pistol's
    punch (-11.3 LUFS as recorded) 3 dB down, beside the original's own impact sound that plays with it."""
    if name == "dawn_pistol1":
        return 3.0, False
    return 0.0, True


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Nika fruit's transformation drums (tools/gear_sounds.py).", only, level)


if __name__ == "__main__":
    main()
