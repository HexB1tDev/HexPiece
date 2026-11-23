#!/usr/bin/env python3
"""3.3.4: the Noro Noro no Mi's sounds, every one from the web (the owner: "Don't you dare create one ... Go search around the web. Get
proper sounds in."): CC0 recordings from Freesound.org for Foxy's Noroma photons. 3.3.4's first push borrowed the mod's own clips
(Freesound refused that session); these replace them: a burst of air ("pffft") and a short steam hiss for a burst of photons let go, a
steady pressure hiss looped for the beam, a slow-motion whoosh and a deep whoosh for something caught and slowing down, a soft body hit
and a muffled thud for a blow held back, a swoosh explosion for the held blows landing at once, plastic wooshes for the mirror, a
rising steam hiss for the Slow Field's dome.

Every clip is broadband noise with no pitched line (the owner, 3.2.1: "It's too melodic, bro"): measured on the recordings by their
long-term spectrum's most prominent line over its 121 neighbouring bins, each clip as cut (plain noise: about +3.5 dB; the Ice
fruit's accepted clips +4.4 to +13.4; these +4.4 to +9.3; refused: a slow-motion sound whining at 11.7 kHz +33 dB, the first half
second of the steam hiss (a 199 Hz hum), the start of the swoosh explosion (a 1.26 kHz line), an epic whoosh with a 70 Hz hum at +18.7 dB, an air release whistling at 307 Hz +16.3 dB, a collision reverb ringing at 183 Hz
+14.7 dB, a noise burst ending in a falling chirp, a quick woosh, a warp and a force field with half their energy in pitched lines).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/noro/ with SOURCES.txt naming every clip's origin. The build never downloads. No clip
outlasts its move (the beam is a loop the client fades out the moment the beam ends).

Usage: python3 tools/noro_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/noro")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "pffft": ("reg7783", 131302),              # "release_air_pffft.wav"
    "steam_hiss": ("wubitog", 234782),         # "Steam/hiss"
    "pressure": ("brunoboselli", 457294),      # "Air (or steam) pressure release"
    "deep5": ("Kinoton", 558827),              # "Deep Whoosh #5"
    "deep_ramp": ("oMzig", 789075),            # "Deep Whoosh Speed Ramp"
    "body_hit": ("insanity54", 276600),        # "body_hit.wav"
    "muffled": ("NeoSpica", 512241),           # "Muffled thud"
    "swoosh_boom": ("Electroviolence", 234555),  # "Swoosh Explosion"
    "plastic": ("emptysound", 205989),         # "twistedwooshes-plastic5.wav"
    "steam": ("SukritSen", 398077),            # "Steam.wav"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "photon1": ("pffft", 0.2, 0.6, 0.0, 0.2),
    "photon2": ("steam_hiss", 0.8, 0.55, 0.02, 0.25),
    "slow1": ("deep5", 0.3, 1.6, 0.02, 0.7),
    "slow2": ("deep_ramp", 0.05, 1.4, 0.0, 0.5),
    "store1": ("body_hit", 0.2, 0.5, 0.0, 0.15),
    "store2": ("muffled", 0.05, 0.7, 0.0, 0.3),
    "release1": ("swoosh_boom", 0.6, 2.2, 0.03, 1.0),
    "mirror1": ("plastic", 2.8, 1.1, 0.03, 0.4),
    "mirror2": ("plastic", 7.9, 0.9, 0.03, 0.35),
    "field1": ("steam", 0.4, 2.6, 0.05, 0.8),
}

# our loop: (sound, start s, length s, crossfade s): the beam's steady hiss while RMB is held
LOOPS = {
    "beam": ("pressure", 8.0, 4.0, 0.6),
}


def level(name):
    """(gain in dB below full scale, compressed louder), measured (ffmpeg ebur128, integrated) against the Ice fruit's (-18.7 to
    -22.4 LUFS) and the Light fruit's zaps (-16.6): the bursts, the slowing and the release about -14 to -17, the mirror and the dome
    about -17 to -20, the stored thuds and the beam under them."""
    if name == "beam":
        return -6.0, False  # held for seconds under the rest (-18)
    if name in ("store1", "store2"):
        return -4.0, False  # a soft thud: held back, not landed (-20)
    if name == "photon1":
        return -1.5, False  # a burst of air: -9 when compressed, far over the zaps
    if name in ("photon2", "field1"):
        return -2.5, False
    if name == "mirror1":
        return -4.0, False  # as loud as mirror2
    if name in ("release1", "slow2"):
        return 0.0, True  # sparse peaks: compressed louder
    return 0.0, False


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Noro Noro no Mi's sounds (tools/noro_sounds.py).", only, level)


if __name__ == "__main__":
    sys.exit(main())
