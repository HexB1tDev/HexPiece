#!/usr/bin/env python3
"""3.3.5: the Supa Supa no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Daz Bones' blades: a short blade swish for every swing and thrown cut, a
knife being sharpened for blades sliding out of the body, a sword's stab for a blade cutting into someone, a sword striking armour for a
blow glancing off the steel body, cloth tearing for a cut ripping through, skating on ice (a loop) for Atomic Spurt and a spinning
swirl of air (a loop) for Spar Break.

Every clip is broadband with no pitched line (the owner, 3.2.1: "It's too melodic, bro"; a steel "shing" rings like a bell, so none is
used): measured as cut by the long-term spectrum's most prominent line over its 121 neighbouring bins, against plain noise of the same
length (noise alone reads about +10 dB at 0.25 s, +7 at 0.45 s, +4.5 at 1 s): every clip here within about 4 dB of that. Refused for
their ringing: three sword swings (+14 to +24 dB), an unsheathed sword (+21.5), a knife sharpened on a stone (+17.8) and a blade scraped
on a knife (+29), flesh sliced (+17), a bladed gut rip whining at 9.4 kHz, a metal clang (+12.7), the stab's low thump and its first
half-second's 12 kHz hiss, the knife's first scrape ringing at 711 Hz.

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/supa/ with SOURCES.txt naming every clip's origin. The build never downloads. No clip
outlasts its move (the skate and the spin are loops the client fades out the moment they end).

Usage: python3 tools/supa_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/supa")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "swish": ("Qvantor", 877976),             # "Swish Swipe Short"
    "sharpen": ("mywhats", 175520),           # "knife sharpening.wav"
    "stab": ("ihitokage", 395370),            # "Sword stab 1"
    "armour": ("velcronator", 733887),        # "Sword Impact"
    "rip": ("reg7783", 133440),               # "cloth_ripping.wav"
    "skating": ("13FPanska_Marval_Lukas", 379329),  # "Skating.wav"
    "swirl": ("Qvantor", 877983),             # "Whoosh Swirl Spinning"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "swish1": ("swish", 0.0, 0.6, 0.0, 0.25),
    "draw1": ("sharpen", 0.55, 0.28, 0.0, 0.12),
    "cut1": ("stab", 0.1, 0.25, 0.0, 0.1),
    "glance1": ("armour", 0.0, 0.7, 0.0, 0.35),
    "rip1": ("rip", 0.3, 1.0, 0.02, 0.4),
}

# our loop: (sound, start s, length s, crossfade s)
LOOPS = {
    "skate": ("skating", 1.0, 3.0, 0.5),
    "spin": ("swirl", 0.5, 2.0, 0.6),
}


def level(name):
    """(gain in dB below full scale, compressed louder), measured (ffmpeg ebur128, integrated) against the Noro and Ice fruits' (-14 to
    -22 LUFS): the swish and the cuts up front, the loops under them."""
    if name == "skate":
        return 0.0, True  # a faint recording (-31 LUFS at -6 dB): brought up under the cuts
    if name == "spin":
        return -6.0, False  # held for seconds under the cuts
    if name == "draw1":
        return 0.0, True  # a quiet scrape: compressed louder
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Supa Supa no Mi's sounds (tools/supa_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
