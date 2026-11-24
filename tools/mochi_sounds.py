#!/usr/bin/env python3
"""3.5.5: the Mochi Mochi no Mi's two sounds of its own, from the web (the owner: "Don't you dare create one ... Go search around the
web. Get proper sounds in."): CC0 recordings from Freesound.org, a slime squished for mochi forming, sticking and closing round someone
(the sticky glob, the Awakening, Future Sight's holes) and a slug stepped on for a splat of mochi (a glob landing, a tendril striking).
Refused (tools/tonal_lines.py, over noise of their own length): a slime squish with a whine at 11.2 kHz (+15.4), a slime slapped over
a 43 Hz hum (+29.3), a splat at 1.8 kHz (+19.1), wet splats (+8 to +10).
The rest of the fruit's sounds are the mod's own CC0 clips already (mochi/MochiSounds: a rubber stretch, punches, a balloon filling, a
fire blast, a rumble, a pop; their folders' SOURCES.txt credit them).

Every clip is broadband with no pitched line, measured AS CUT with tools/tonal_lines.py (the owner will not have melodic sounds):
squish1 +3.7 dB over noise, splat1 +1.1.

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/mochi/ with
SOURCES.txt naming every clip's origin. The build never downloads.

Usage: python3 tools/mochi_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/mochi")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "squish": ("Archos", 433840),         # "Slime 27.wav"
    "slug": ("Breviceps", 447930),        # "Step on a slug (Splat!) 2"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "squish1": ("squish", 0.1, 0.6, 0.01, 0.2),
    "splat1": ("slug", 0.05, 0.5, 0.005, 0.18),
}

LOOPS = {}


def level(name):
    return LEVELS.get(name, (0.0, False))


LEVELS = {
    "squish1": (-3.0, False),  # on every glob, hole and the Awakening: a little under full scale
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Mochi Mochi no Mi's sounds (tools/mochi_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
