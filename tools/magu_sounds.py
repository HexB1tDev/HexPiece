#!/usr/bin/env python3
"""3.1.3: the Magu Magu no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Akainu's magma: lava bubbling (a loop, while the fist swells or the ground
boils), fire whooshes for a fist or a stream of magma thrown, fireball bursts where magma lands, a big punchy explosion for the giant
fist and the hound's flood, hisses where magma meets flesh, a flame burst's roar for the hound and the geysers.

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/magu/ with SOURCES.txt naming every clip's origin. The build never downloads. No clip
outlasts its move (the bubbling is a loop the client fades out the moment the move ends).

Usage: python3 tools/magu_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/magu")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "lava_flow": ("Jim-Bretherick", 618013),     # "Bubbling lava flowing1.wav"
    "fire_whoosh": ("LookIMadeAThing", 260555),  # "Basic Fire Whoosh 2"
    "fire_whoosh2": ("hnhnh", 244926),           # "fire-whoosh.wav"
    "fireball": ("Blankened", 431174),           # "Fireball Explosion.wav"
    "fire_expl": ("ReadeOnly", 186932),          # "fire_explosion.wav"
    "punchy_boom": ("misosound", 251759),        # "BOOMING PUNCHY EXPLOSION - close, big SCHOEPS MK41.wav"
    "lava_ball": ("tiagusilva37", 737659),       # "SFX_HOT_BALL_LAVA_SHORT"
    "hiss_fire": ("bassimat", 870180),           # "Water Poured onto a Burning Fireplace - Hissing Steam and Sizzling Embers"
    "flame_burst": ("magnuswaker", 592572),      # "Flame Burst"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "whoosh1": ("fire_whoosh", 0.0, 1.6, 0.0, 0.6),
    "whoosh2": ("fire_whoosh2", 0.0, 2.2, 0.0, 0.8),
    "burst1": ("fireball", 0.0, None, 0.0, 0.3),
    "burst2": ("fire_expl", 0.6, 2.6, 0.0, 1.1),
    "boom1": ("punchy_boom", 0.0, None, 0.0, 1.2),
    "sizzle1": ("lava_ball", 0.0, None, 0.0, 0.4),
    "sizzle2": ("hiss_fire", 1.0, 2.4, 0.05, 1.0),
    "roar1": ("flame_burst", 0.0, 2.8, 0.0, 1.2),
}

# our loop: (sound, start s, length s, crossfade s): magma bubbling while it is gathered or the ground boils
LOOPS = {
    "bubble": ("lava_flow", 20.0, 8.0, 1.0),
}


def level(name):
    """(gain in dB below full scale, compressed louder): the bubbling sits under the blows (it is held for seconds)."""
    if name == "bubble":
        return -6.0, False
    if name == "burst2":
        return -4.0, False  # -7.8 LUFS momentary at full scale: brought to the Light fruit's explosion (-11.8)
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Magu Magu no Mi's sounds (tools/magu_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
