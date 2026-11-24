#!/usr/bin/env python3
"""3.5.5, every dough sound replaced in 3.6.8 (the owner: "literally every sound of dough sounds like shit, get new ones from the web of
slime, viscous stuff, dough etc"): the Mochi Mochi no Mi's dough sounds, CC0 recordings of real slime, goo and clay from Freesound.org:
an arm of mochi stretching (slime stretched and squeezed), a fist of mochi landing (slime and soft clay slapped), mochi forming and
sticking (slime squished), a splat of mochi, the swell of an arm (a long squeeze of slime), a heavy slam of dough (the heaviest
slap of slime), the stamp of a foot sinking into dough. The fire, the blast and the wheel's rumble are not dough:
kept (mochi/MochiSounds).

Every clip is broadband with no pitched line, measured AS CUT with tools/tonal_lines.py (the owner will not have melodic sounds); a
steady room hum under 90 Hz some of the recordings carry is filtered out (web_sounds.HUM), an equaliser on the recording, nothing added.
Refused this time (pitched): a cartoon splat (+11 over noise at 97 Hz), a mud splat (+8 at 156 Hz), a slime squish whining at 11.2 kHz
(+15), a viscous gurgle (+15 at 190 Hz), a syrup slop (+8 to +9), every jello squish (a 59 Hz hum the filter cannot take off: +8 to +13); the bass lift (web_sounds.BASS) brought the
slapping's and the jelly's own hum back (+18 to +25 at 59 to 65 Hz): not used.

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
    "stretching": ("greenlinker", 794270),   # "Slime stretching and squeezing"
    "slapping": ("greenlinker", 794272),     # "Slime slapping"
    "squishing": ("rubberduck9999", 680447), # "slime - squishing sounds"
    "clay": ("Funky_Audio", 696020),         # "LIQImpt_clay soft slaps"
    "squishes": ("Eneasz", 216882),          # "squishes.wav"
}

HUM = web_sounds.HUM
# our file: (sound, start s, length s or None for the rest, fade in s, fade out s, equaliser)
CLIPS = {
    # an arm stretching out: a long stretch of slime pulled, and a second one
    "stretch1": ("squishing", 97.88, 0.62, 0.02, 0.22, HUM),
    "stretch2": ("stretching", 153.58, 0.62, 0.02, 0.22, HUM),
    # a fist of mochi landing: wet slaps of slime and soft clay
    "slap1": ("slapping", 2.66, 0.36, 0.003, 0.14, HUM),
    "slap2": ("slapping", 6.03, 0.36, 0.003, 0.14, HUM),
    "slap3": ("clay", 5.51, 0.36, 0.003, 0.14, HUM),
    # mochi forming, sticking, closing round someone
    "squish1": ("stretching", 85.42, 0.6, 0.01, 0.22, HUM),
    "squish2": ("stretching", 215.90, 0.5, 0.01, 0.2, HUM),
    # a splat of mochi
    "splat1": ("slapping", 18.23, 0.5, 0.004, 0.22, HUM),
    "splat2": ("squishes", 71.70, 0.5, 0.004, 0.22, HUM),
    # an arm swelling: a long squeeze of slime
    "swell1": ("stretching", 104.58, 1.5, 0.08, 0.4, HUM),
    # a heavy slam of dough: the heaviest slap of slime (pushed louder)
    "slam1": ("slapping", 16.60, 0.5, 0.003, 0.25, HUM),
    # the stamp of a foot into dough
    "stamp1": ("slapping", 17.22, 0.45, 0.003, 0.2, HUM),
}

LOOPS = {}


def level(name):
    return LEVELS.get(name, (0.0, False))


LEVELS = {
    "squish1": (-3.0, False),  # on every glob, hole and the Awakening: a little under full scale
    "squish2": (-3.0, False),
    "stretch1": (-2.0, False),
    "stretch2": (-2.0, False),
    "slam1": (0.0, True),      # the heaviest blow of the fruit: pushed
    "stamp1": (0.0, True),
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Mochi Mochi no Mi's sounds (tools/mochi_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
