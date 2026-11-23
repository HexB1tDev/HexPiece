#!/usr/bin/env python3
"""3.3.6: the Horo Horo no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Perona's ghosts: a soft breathy swoosh of air for a ghost sent off, a deep
low whoosh for someone sinking into despair (Gloom), a poof of smoke for a Mini Hollow bursting, a deep boom for the Toku Hollow's
blast, a rising woosh for the spirit leaving its body (and going back), and a sustained wind for Ghost Network's ghosts spreading out.
No voice, moan, wail or laugh: a voice is pitched, and the owner will not have melodic sounds.

Every clip is broadband with no pitched line, measured AS CUT with tools/tonal_lines.py (the long-term spectrum's most prominent line
over its 121 neighbouring bins, and its sharpest peak over the bins 4 to 10 either side, each against plain noise of the clip's own
length): as cut, the sharpest peaks +1.9 to +5.2 dB over noise (the Ice fruit's clips the owner accepted read up to +6.8). Refused: a reversed
breath whining at 9.5 kHz (+9.7), a slow swoosh with a 721 Hz line (+16), a low thud that is a 48 Hz tone (+46), a deep explosion's
54 Hz hum (+9), the smoke poof whose attack rings at 2.2 kHz (+8.5), the mouth swoosh's first 0.4 s with a 743 Hz line (+5).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/horo/ with
SOURCES.txt naming every clip's origin. The build never downloads. No clip outlasts its move.

Usage: python3 tools/horo_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/horo")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "swoosh": ("newagesoup", 372520),         # "Mouth-swoosh-01.wav"
    "deepwhoosh": ("Logicogonist", 807976),   # "bass rumble - deepwhoosh 1"
    "poof": ("Planman", 208111),              # "Poof of Smoke"
    "boom": ("Za-Games", 539968),             # "Deep Boom"
    "woosh": ("florianreichelt", 683096),     # "Woosh"
    "wind": ("martian", 403003),              # "magical rising wind or  sea .wav"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "ghost1": ("swoosh", 0.4, 0.6, 0.03, 0.25),
    "despair1": ("deepwhoosh", 0.1, 1.3, 0.02, 0.5),
    "pop1": ("poof", 0.1, 0.6, 0.0, 0.25),
    "boom1": ("boom", 0.0, 2.4, 0.0, 0.9),
    "spirit1": ("woosh", 0.15, 1.3, 0.05, 0.4),
    "network1": ("wind", 6.9, 2.0, 0.3, 0.7),
}

LOOPS = {}


def level(name):
    """(gain in dB below full scale, compressed louder), measured (ffmpeg ebur128, integrated) against the Noro, Supa and Ice fruits'
    (-14 to -22 LUFS): the boom up front, the ghosts' swooshes and the wind under it."""
    if name == "boom1":
        return 0.0, True  # the Toku Hollow's huge blast: compressed louder (-11.9 LUFS)
    if name == "network1":
        return -4.0, False  # a wind under everything else (-18.8)
    if name == "ghost1":
        return -5.0, False  # sent off in streams of five: a soft swoosh (-11.1 at full scale), under Supa's swish (-17.4)
    if name == "despair1":
        return -3.0, False  # (-13.5 at full scale)
    if name == "pop1":
        return 0.0, True  # a faint poof (-20.8): compressed louder, four of them at once
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Horo Horo no Mi's sounds (tools/horo_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
