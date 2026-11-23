#!/usr/bin/env python3
"""3.3.7: the Ito Ito no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Doflamingo's strings: a quick swish for strings lashing out, a short swing
for a string bullet, a cut for strings slicing in, a fire whoosh for Overheat, ropes creaking taut for Parasite (and Self-Surgery, Black
Knight woven), wings fluttering for Spider's Web spreading, an air rush for the Birdcage rising, a long low whoosh for Break White's
waves and God Thread, a tiny swish for a step on the strings in the sky. No plucked string (it rings: the owner will not have melodic
sounds).

Every clip is broadband with no pitched line, measured AS CUT with tools/tonal_lines.py (the long-term spectrum's sharpest peak over the
bins 4 to 10 either side, against plain noise of the clip's own length): -2.5 to +5.1 dB over noise (the Ice fruit's clips the owner
accepted read up to +6.8). Refused: a whip crack ringing at 1.09 kHz (+8.1), scissors at 985 Hz, a cut whose weight was all under 60 Hz (a thump, not a slice), a fire whoosh's 97 Hz hum (+9.5), a
deep whoosh's 65 Hz tone (+12.2), a big swoosh (+16.6), a low whoosh's 1.1 kHz line (+9.4), the ropes' last creak (+7.0).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/ito/ with
SOURCES.txt naming every clip's origin. The build never downloads. No clip outlasts its move.

Usage: python3 tools/ito_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/ito")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "wipe": ("hz37", 667419),                  # "Swish wipe HZA 16-05-2022.wav"
    "swing": ("ssierra1202", 391952),          # "swing"
    "slicing": ("PEDBRA", 377214),             # "Slicing"
    "fire": ("hnhnh", 244926),                 # "fire-whoosh.wav"
    "ropes": ("ssierra1202", 391946),          # "ropes"
    "wings": ("Qvantor", 877979),              # "Whoosh Flutter Wings"
    "rush": ("Henrythetrain", 135785),         # "Air Rush.wav"
    "low": ("DJT4NN3R", 449990),               # "whoosh_long_low.wav"
    "micro": ("Qvantor", 878248),              # "UI Hover Micro Swish"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "swish1": ("wipe", 0.0, 0.44, 0.0, 0.12),
    "shot1": ("swing", 0.0, 0.35, 0.0, 0.12),
    "slice1": ("slicing", 0.2, 0.5, 0.01, 0.2),
    "burn1": ("fire", 0.08, 1.2, 0.02, 0.45),
    "pull1": ("ropes", 1.2, 0.8, 0.03, 0.3),
    "web1": ("wings", 0.1, 0.9, 0.02, 0.35),
    "cage1": ("rush", 1.0, 2.1, 0.2, 0.35),
    "wave1": ("low", 0.3, 2.4, 0.05, 0.8),
    "step1": ("micro", 0.0, 0.21, 0.0, 0.08),
}

LOOPS = {}


def level(name):
    """(gain in dB below full scale, compressed louder), measured (ffmpeg ebur128, integrated) against the Noro, Supa, Horo and Ice
    fruits' (-14 to -22 LUFS)."""
    return LEVELS.get(name, (0.0, False))


LEVELS = {
    "swish1": (-6.0, False),  # a lash every 8 ticks: under Supa's swish (-14.5 at full scale against its -22 mean)
    "slice1": (-3.0, False),  # a cut on every hit (-12 LUFS compressed was too loud): plain, a little under full scale
    "cage1": (-2.0, False),  # one big rush as the cage goes up (-11.6 at full scale)
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Ito Ito no Mi's sounds (tools/ito_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
