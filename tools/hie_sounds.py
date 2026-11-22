#!/usr/bin/env python3
"""3.1.9: the Hie Hie no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
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
SOUNDS = {
    "ice_spell": ("Qvantor", 880760),         # "Ice Spell Freeze"
    "freeze": ("lori.mortimer", 723875),      # "Freeze"
    "ice_fracture": ("Vrymaa", 735401),       # "Ice - Fracture, roots or growing plants"
    "frost_crack": ("Sanderboah", 709137),    # "Ice / Frost cracking"
    "ice_crack": ("j_p_higgins", 262635),     # "Ice Crack 1"
    "ice_break": ("InMotionAudio", 719973),   # "ICEBrk_Break04_InMotionAudio_FREESampleSunday"
    "break_ice": ("davilca", 677230),         # "Break Ice"
    "chill_hit": ("JustInvoke", 138484),      # "Chill Hit"
    "avalanche": ("craigsmith", 675734),      # "S27-07 Ice avalanche sliding & breaking up; weird.wav"
    "frost_loop": ("qubodup", 844261),        # "Subzero Frost Loop"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "freeze1": ("ice_spell", 0.0, None, 0.0, 0.5),
    "freeze2": ("freeze", 0.9, 2.4, 0.02, 0.8),
    "form1": ("ice_fracture", 1.8, 2.6, 0.05, 0.9),
    "form2": ("frost_crack", 2.8, 2.4, 0.05, 0.9),
    "crack1": ("ice_crack", 0.0, None, 0.0, 0.2),
    "crack2": ("frost_crack", 47.8, 1.8, 0.02, 0.6),
    "shatter1": ("ice_break", 0.0, None, 0.0, 0.2),
    "shatter2": ("break_ice", 3.8, 2.0, 0.0, 0.8),
    "hit1": ("chill_hit", 0.0, None, 0.0, 0.15),
    "age1": ("avalanche", 0.0, 3.6, 0.0, 1.4),
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
    if name == "freeze1":
        return -4.0, False  # -13.4 at full scale
    if name in ("freeze2", "age1"):
        return -2.0, False
    if name in ("form1", "form2"):
        return -3.0, False
    if name in ("crack1", "shatter2"):
        return 0.0, True  # sparse peaks (-25, -26.5): compressed louder
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Hie Hie no Mi's sounds (tools/hie_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
