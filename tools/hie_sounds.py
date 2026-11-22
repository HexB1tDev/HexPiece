#!/usr/bin/env python3
"""3.1.9: the Hie Hie no Mi's sounds (3.2.2: only real ice, no pitched sound: see SOUNDS), every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Aokiji's ice: an ice spell's freeze and a "Freeze" for a body frozen solid,
ice growing and frost cracking for ice forming (Ice Age's spread, the road, the spears and the pheasant taking shape), ice cracking
for a frozen body about to break, ice breaking for it shattering, a chill hit for a spear or a fist landing, an ice avalanche for Ice
Age's wave, and a frost loop held while ice gathers (the spears, the ball, the saber).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/hie/ with SOURCES.txt naming every clip's origin. The build never downloads. No clip
outlasts its move (the frost is a loop the client fades out the moment the move ends).

Usage: python3 tools/hie_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/hie")

# Freesound sound: (author's page name, sound id)
# 3.2.2 (the owner: "There's a sound that you added that sounds like a fucking fairy, bro ... only a few sound like actual ice, the
# other ones don't. It's too melodic"): every clip is a real ice recording that is broadband noise (cracks, crunches, breaks), checked
# by its spectrum (spectral flatness over 0.3, no pitched lines); the pitched ones (an ice spell's chimes, a "Freeze" with a hum, a
# creaking fracture, a chill hit, a whistling avalanche, a frost crack with a tone in it) are gone.
SOUNDS = {
    "ice_cracking": ("Mooe", 169897),          # "ice_cracking_01.flac"
    "ice_cracks": ("karlis.stigis", 168712),   # "Ice Cracks"
    "slow_cracking": ("PhilllChabbb", 268862), # "Slow Cracking Ice"
    "cracking_n": ("northern87", 369982),      # "Ice cracking_northern87"
    "crunch": ("JackMenhorn", 213578),         # "Ice Break Crunch Crack Foot 01"
    "crack_short": ("wwstudioswastaken", 624163),  # "ice_cracking_01.flac"
    "ice_crack": ("j_p_higgins", 262635),      # "Ice Crack 1"
    "ice_break": ("InMotionAudio", 719973),    # "ICEBrk_Break04_InMotionAudio_FREESampleSunday"
    "break_ice": ("davilca", 677230),          # "Break Ice"
    "frost_loop": ("qubodup", 844261),         # "Subzero Frost Loop"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "freeze1": ("ice_cracking", 32.5, 2.2, 0.02, 0.6),
    "freeze2": ("ice_cracks", 4.35, 2.2, 0.02, 0.6),
    "form1": ("slow_cracking", 24.4, 2.4, 0.05, 0.8),
    "form2": ("cracking_n", 54.6, 2.4, 0.05, 0.8),
    "crack1": ("ice_crack", 0.0, None, 0.0, 0.2),
    "crack2": ("crack_short", 0.0, None, 0.0, 0.15),
    "shatter1": ("ice_break", 0.0, None, 0.0, 0.2),
    "shatter2": ("break_ice", 3.8, 2.0, 0.0, 0.8),
    "hit1": ("crunch", 85.45, 0.9, 0.0, 0.3),
    "age1": ("slow_cracking", 9.7, 3.6, 0.05, 1.2),
}

# our loop: (sound, start s, length s, crossfade s): frost gathering while ice is held (the spears, the ball, the saber)
LOOPS = {
    "frost": ("frost_loop", 0.0, 4.8, 0.6),
}


def level(name):
    """(gain in dB below full scale, compressed louder): the frost sits under the blows (it is held for seconds)."""
    # measured (ffmpeg ebur128, integrated) against the Gura crack (-20.8 LUFS), the Magma burst (-21.5) and the Gura boom (-16.2)
    if name == "frost":
        return -3.0, False  # -35 LUFS at -8 dB: too faint under the wind
    if name in ("crack1", "shatter2", "crack2", "hit1", "form2"):
        return 0.0, True  # sparse peaks: compressed louder
    if name == "form1":
        return -5.0, False  # -15.4 at full scale: bright, loud
    if name == "age1":
        return -4.0, False  # -14.6 at full scale
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Hie Hie no Mi's sounds (tools/hie_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
