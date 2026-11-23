#!/usr/bin/env python3
"""3.2.3: the Nika fruit's transformation drums, every one from the web (the owner: "Don't you dare create one ... Go search around the
web. Get proper sounds in."; then, on Gear 5's transformation: "if you could give the transformation just the drum sounds and just get
rid of the music because of copyright, that would be perfect"): CC0 recordings from Freesound.org, the drums of liberation as the chest
is pounded: a deep tribal floor tom for each beat, one heavy cinematic drum hit for the last. 3.3.9: Dawn Pistol's cartoon punch (the
owner accepted "a new CC0 punch sound from the web"; broadband, +3 dB over plain noise in tools/tonal_lines.py). 3.4.4: Goof Ball and
Dawn Whip (a rubber stretch for the wind-up, a swirling whoosh for the unwinding and the ball's loop, a whip's crack, a poof of smoke;
each checked as cut with tools/tonal_lines.py: within 4 dB of plain noise, narrow).

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used, makes it
mono, peak-normalises it, fades it and writes it as Ogg Vorbis into src/main/resources/assets/hexhaki/sounds/piece/gear/ with
SOURCES.txt naming every clip's origin. The build never downloads. A beat rings about a second and a half: never past the
transformation.

Usage: python3 tools/gear_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/gear")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "floor_tom": ("hello_flowers", 30822),               # "Tomtom6_TribalFloor.wav"
    "heavy_drum": ("choomaque-crispydinner", 577408),    # "crdn_cinematic_heavy_drums - 14.wav"
    "cartoon_punch": ("modusmogulus", 785936),           # "Cartoon Punch Cassette F" (3.3.9: Dawn Pistol's POW)
    "rubber": ("mickdow", 253327),                       # "Rubber Stretch Rip 1.wav" (3.4.4: Dawn Whip's wind-up)
    "swirl": ("Qvantor", 877983),                        # "Whoosh Swirl Spinning" (3.4.4: the unwinding, Goof Ball's loop)
    "whip": ("peterbullmusic", 351267),                  # "Mixed Whip Crack 1.wav" (3.4.4: Dawn Whip's crack)
    "poof": ("Planman", 208111),                         # "Poof of Smoke" (3.4.4: Goof Ball popping, the pop out of the ground)
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    "drum1": ("floor_tom", 0.0, 1.5, 0.0, 0.6),
    "drum2": ("heavy_drum", 0.0, None, 0.0, 0.3),
    "dawn_pistol1": ("cartoon_punch", 0.0, None, 0.0, 0.05),
    "whip_wind1": ("rubber", 0.1, 0.9, 0.05, 0.3),
    "whip_spin1": ("swirl", 0.3, 1.3, 0.05, 0.4),
    "whip_crack1": ("whip", 0.45, 0.4, 0.0, 0.15),
    "goof_pop1": ("poof", 0.1, 0.6, 0.0, 0.25),
}

LOOPS = {
    "goof_spin": ("swirl", 0.5, 2.0, 0.6),
}


# 3.4.9 (the owner: "we also need a sound redesign for most of the abilities. The like mostly like cartoony sounds, so go get them from
# the web"): the original's own Gear 5 events play these (sounds.json overrides them; sounds/piece/nika). Each checked as cut with
# tools/tonal_lines.py against plain noise of its own length (broadband: no whine, hum or ringing).
NIKA_OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/nika")
NIKA_SOUNDS = {
    "zip": ("se2001", 541507),             # "Cartoon Quick Zip 02": the stretching punch thrown
    "slap": ("AdminMP", 383201),           # "Slap - Cartoony": a stretched fist landing
    "kungfu": ("Sadiquecat", 786323),      # "Kung-Fu punch SFX (A-B) (Edited)": the Gatling's fists
    "kungfu2": ("peridactyloptrix", 210897),  # "Kung Fu punch 5": the Gatling's fists
    "swosh": ("qubodup", 59988),           # "SWOSH-01 44.1kHz": Rocket's haul
    "swing": ("SypherZent", 420668),       # "Basic Melee Swing / Miss / Whoosh": a dodge
    "balloon": ("xkeril", 760346),         # "Inflating a party balloon (fast)": Gigante, the Bajrang Gun's fist swelling
    "explosion": ("modusmogulus", 784205), # "Cartoon Explosion (designed on cassette tape lol)": the Bajrang Gun's impact
    "deep": ("DJT4NN3R", 449990),          # "whoosh_long_low.wav": the Bajrang Gun's slam, the Giant's throw
    "rubber": ("mickdow", 253327),         # "Rubber Stretch Rip 1.wav": Coil Spring let go
    "pop": ("unfa", 245646),               # "Cartoon Pop (Distorted)": the bounding walk's bounds
    "splat": ("foxgirl", 437312),          # "melonexplosion.wav": Rubber World let go
}
NIKA_CLIPS = {
    "pistol1": ("zip", 0.0, None, 0.0, 0.05),
    "impact1": ("slap", 0.05, 0.6, 0.0, 0.15),
    "gatling1": ("kungfu", 0.0, None, 0.0, 0.08),
    "gatling2": ("kungfu2", 0.25, 0.4, 0.0, 0.1),
    "rocket1": ("swosh", 0.0, None, 0.0, 0.08),
    "dodge1": ("swing", 0.0, None, 0.0, 0.05),
    "giant1": ("balloon", 0.25, 0.75, 0.03, 0.15),
    "charge1": ("balloon", 1.25, 0.65, 0.03, 0.15),
    "slam1": ("deep", 0.3, 2.0, 0.05, 0.5),
    "throw1": ("deep", 0.6, 1.1, 0.03, 0.35),
    "boom1": ("explosion", 0.0, 2.8, 0.0, 0.8),
    "coil1": ("rubber", 2.0, 0.8, 0.03, 0.2),
    "bounce1": ("pop", 0.0, None, 0.0, 0.03),
    "world1": ("splat", 0.0, None, 0.0, 0.15),
}


def nika_level(name):
    """(gain in dB, compressed louder): the hits and pops short and punchy; the Gatling's fists (every 3 ticks) and the bounding walk's
    bounds (every step) 2 and 3 dB down; the rest as recorded."""
    if name.startswith("gatling"):
        return -2.0, True
    if name == "bounce1":
        return -3.0, True
    if name in ("impact1", "coil1", "world1"):
        return 0.0, True
    return 0.0, False


def level(name):
    """(gain in dB below full scale, compressed louder): the beats are the transformation's only sound, loud and punchy; Dawn Pistol's
    punch (-11.3 LUFS as recorded) 3 dB down, beside the original's own impact sound that plays with it."""
    if name == "dawn_pistol1":
        return 3.0, False
    if name == "goof_spin":
        return -6.0, False  # 3.4.4: held for seconds under the punches (as the Supa fruit's spin)
    if name in ("whip_wind1", "whip_spin1"):
        return 0.0, False
    if name in ("whip_crack1", "goof_pop1"):
        return 0.0, True  # short: compressed louder
    return 0.0, True


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    if not only or only & (set(CLIPS) | set(LOOPS)):
        web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Nika fruit's transformation drums (tools/gear_sounds.py).", only, level)
    if not only or only & set(NIKA_CLIPS):
        web_sounds.build(NIKA_SOUNDS, NIKA_CLIPS, {}, NIKA_OUT, "Gear 5's moves, redone as cartoon sounds (tools/gear_sounds.py, 3.4.9).",
                         only, nika_level)


if __name__ == "__main__":
    main()
