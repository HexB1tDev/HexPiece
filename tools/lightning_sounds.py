#!/usr/bin/env python3
"""3.6.0: every Haki lightning sound new (the owner: "Also all lightning sounds are to change to new more bass boosted ones (get from the
web) and be lowered in volume (ONLY the lightning sounds), Full COnqueror on releasing should not play all the lightning sounds at
once, just one"). Every clip is a CC0 recording on Freesound.org (tools/web_sounds.py: the licence checked, cut, made mono, its low
end lifted (web_sounds.BASS: +7 dB at 120 Hz, nothing under 45 Hz), normalised, faded, Ogg Vorbis) into
src/main/resources/assets/hexhaki/sounds/piece/lightning/web/ with SOURCES.txt. Nothing is mixed or layered.

- strike1..3: the Conqueror's release's lightning (conqueror_release): close, explosive thunder cracks.
- zap1..3: the Haki lightning's short crack (the original's armament_ambient_zap, which every Haki bolt, crackle and coated hit plays):
  the first moment of a deep close crack.
- thunder: a storm bolt striking the ground (the original's last_stand_lightning): a clap and its rumble.
Lowered in sounds.json (their "volume"); client/haki/HakiBolts lets one lightning sound through at a time (the full release's many bolts
are heard as one).

Usage: python3 tools/lightning_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/lightning/web")

SOUNDS = {
    "explosive": ("TRP", 574412),          # "Thunder big crack explosive close ms 77mel 190903"
    "super_close": ("danielmcadams", 483684),  # "Zoom H4n - Super Close Lightning Strike and Thunderstorm"
    "sizzle": ("kvgarlic", 693540),        # "LoudThunder with crack and Sizzle JULY 1 2023"
    "pretty_close": ("TRP", 616992),       # "210914 Thunder, crack, pretty close"
    "clap": ("Kinoton", 760214),           # "Thunder Clap And Rumble #7"
}

B = web_sounds.BASS
# our file: (sound, start s, length s, fade in s, fade out s, equaliser)
CLIPS = {
    "strike1": ("explosive", 1.45, 4.0, 0.0, 2.0, B),
    "strike2": ("sizzle", 2.55, 4.0, 0.0, 2.0, B),
    "strike3": ("super_close", 18.8, 4.0, 0.0, 2.0, B),
    "zap1": ("explosive", 1.65, 0.65, 0.0, 0.32, B),
    "zap2": ("pretty_close", 1.15, 0.6, 0.0, 0.3, B),
    "zap3": ("clap", 0.6, 0.6, 0.0, 0.3, B),
    "thunder": ("clap", 0.55, 3.5, 0.0, 2.0, B),
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, {}, OUT, "The Haki lightning's sounds (tools/lightning_sounds.py).", only)


if __name__ == "__main__":
    sys.exit(main())
