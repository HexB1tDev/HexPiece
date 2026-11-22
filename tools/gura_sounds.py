#!/usr/bin/env python3
"""3.1.1: the Gura Gura no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org, chosen for what Whitebeard's quakes sound like: a deep boom with its rumble
for the quake let out of the fist, glass breaking for the air cracking, an earthquake's rumble held while a quake is held (a loop), an
earthquake's crack and roar when the island tilts, rocks crumbling for the rift and the craters, a big wave crashing for the tsunami,
a heavy whoosh for the leap and the dash, a heavy slam for a blow landing.

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used (seconds
into it), makes it mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/gura/ with SOURCES.txt naming every clip's origin. The build never downloads. Every
clip is short enough never to outlast its move (the rumble is a loop the client fades out the moment the quake is let go).

Usage: python3 tools/gura_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/gura")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "boom_rumble": ("Qvantor", 879263),          # "Boom with Rumble"
    "deep_impact": ("zazz.sound.design", 754423),  # "DSGNImpt_Deep Cinematic Impact 4_ZAZZ"
    "glass_break": ("unfa", 221528),             # "Glass Break"
    "glass_panel": ("LukaCafuka", 758207),       # "Glass panel shattering"
    "earthquake": ("crunchymaniac", 678418),     # "Hard Earthquake.mp3"
    "quake_crack": ("uagadugu", 222521),         # "Cracking Earthquake (cracking soil, cracking stone)"
    "rocks": ("ALLANZ10D", 155934),              # "Rocks Crumbling from room or cave"
    "rock_smash": ("NeoSpica", 512243),          # "Rock Smash"
    "waves": ("kyles", 450634),                  # "waves ocean crash on beach nearby wide big metallic sound.flac"
    "whoosh": ("Qvantor", 877977),               # "Whoosh Deep Heavy"
    "slam": ("Qvantor", 879268),                 # "Heavy Slam Impact"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    # the quake let out of the fist
    "boom1": ("boom_rumble", 0.0, 3.6, 0.0, 1.6),
    "boom2": ("deep_impact", 0.0, 3.2, 0.0, 1.5),
    # the air cracking like glass
    "crack1": ("glass_break", 0.0, None, 0.0, 0.3),
    "crack2": ("glass_panel", 0.0, None, 0.0, 0.3),
    # the island tilted, a slam into the ground
    "quake1": ("quake_crack", 0.4, 3.0, 0.0, 1.2),
    # rock torn apart and falling
    "crumble1": ("rocks", 3.8, 2.5, 0.05, 1.0),
    "crumble2": ("rock_smash", 2.35, 2.0, 0.0, 0.9),
    # the tsunami breaking
    "wave1": ("waves", 0.5, 3.2, 0.3, 1.4),
    # the leap, the dash
    "whoosh1": ("whoosh", 0.0, 1.6, 0.0, 0.6),
    # a blow landing hard
    "hit1": ("slam", 0.0, None, 0.0, 0.25),
}

# our loop: (sound, start s, length s, crossfade s): the ground quaking while a quake is held
LOOPS = {
    "rumble": ("earthquake", 6.0, 8.0, 1.0),
}


def level(name):
    """(gain in dB below full scale, compressed louder): the rumble sits under the blows (it is held for seconds)."""
    if name == "rumble":
        return -6.0, False
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Gura Gura no Mi's sounds (tools/gura_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
