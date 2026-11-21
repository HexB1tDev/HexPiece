#!/usr/bin/env python3
"""2.9.0: the Pika Pika no Mi's sounds, every one from the web (the owner: "The sounds are atrocious ... I keep telling you to get the
sounds from the web ... you cannot fucking create sounds"). Before, the Light fruit played the original mod's generic whooshes and
impacts; now every move has its own CC0 recordings from Freesound.org, chosen for their ratings and downloads: lasers, a laser charge
and its hum held, blasts of light, explosions of light, teleports and light-speed zips for Yata no Kagami and the speed-of-light kick,
quick whooshes and punches for the kicks, a sword of light igniting, humming, swinging and going out, magic sparkles for the clones.

tools/web_sounds.py fetches each recording's high-quality preview (checking on its page that it is CC0), cuts the part used (seconds
into it), makes it mono, peak-normalises it, fades it, crossfades a loop's end into its start, and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/pika/ with SOURCES.txt naming every clip's origin. The build never downloads.

Usage: python3 tools/pika_sounds.py [--only name,name]   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import sys

import web_sounds

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/pika")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "laser": ("Daleonfire", 505235),             # "Laser2", rated 4.8
    "soft_laser": ("Filmscore", 515866),         # "Soft Shot Gun Laser Blast", rated 4.8
    "laser_charge": ("StavSounds", 701702),      # "laser-charge.wav", rated 4.7
    "spell_loop": ("Bertsz", 500910),            # "Spell charge loop", rated 4.9
    "ion": ("qubodup", 814049),                  # "Ion Cannon", rated 4.8
    "blasts": ("SamsterBirdies", 731984),        # "Devastating Laser Blasts", rated 4.7
    "magic_blast": ("qubodup", 814044),          # "Creation Magic Blast", rated 5.0
    "space_boom": ("qubodup", 814054),           # "Space Explosion", rated 4.7
    "zaps": ("mju90", 703337),                   # "Noise Laser Zaps.wav", rated 5.0
    "teleport": ("SamsterBirdies", 639987),      # "Teleport activate", rated 4.9
    "zip": ("martian", 19307),                   # "hi speed electronic whoosh 1.wav", 4.9k downloads
    "woosh": ("florianreichelt", 683101),        # "quick woosh", 24k downloads, rated 4.7
    "woosh_mid": ("DJT4NN3R", 449996),           # "whoosh_short_mid.wav", rated 4.7
    "punches": ("rsellick", 545523),             # "boxing mitt punches strike thud whack.wav", rated 4.7
    "hard_hit": ("npeng", 482267),               # "Hard impact hit.wav"
    "void_punch": ("The_Elementus", 873746),     # "Low_Mid_End_Void_Energy_Blast_Punch_02"
    "saber": ("Za-Games", 540214),               # "Laser Sword (Ignition/Waving/Putting Away)"
    "saber_hum": ("Sheyvan", 703384),            # "Lightsaber Hum and Swings 2", rated 5.0
    "sparkle": ("Qvantor", 877981),              # "Whoosh Magic Sparkle", rated 5.0
    "transition": ("qubodup", 856641),           # "Cartoon Magic Transition", rated 4.8
    "parry": ("chungkury", 760636),              # "Metal_Sword_Parry_Impact_Hit_4", rated 4.8
    # 2.9.6: going as light (Z, held: the owner's "turn you into light itself, and travel in a beam ... and reflect"), and each reflection
    "beam_loop": ("unfa", 584191),               # "Weapons Beam Loop"
    "glass_ping": ("Qvantor", 878247),           # "UI Hover Glass Ping"
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    # the laser out of the fingertip, the light gathering there, the beam let go
    "laser1": ("laser", 0.0, None, 0.0, 0.15),
    "laser2": ("soft_laser", 0.0, None, 0.0, 0.45),
    "charge": ("laser_charge", 0.0, None, 0.02, 0.25),
    "beam1": ("blasts", 0.0, 4.8, 0.0, 1.8),
    "beam2": ("blasts", 10.7, 4.8, 0.0, 1.8),
    "beam3": ("ion", 0.0, None, 0.0, 1.5),
    # light blowing up: the laser's and the beam's, the beads'
    "explode1": ("magic_blast", 0.0, 5.0, 0.0, 2.2),
    "explode2": ("space_boom", 0.0, 7.0, 0.0, 2.8),
    "pop1": ("magic_blast", 0.0, 1.4, 0.0, 0.7),
    # Yasakani no Magatama's bullets of light
    "zap1": ("zaps", 0.1, 0.8, 0.0, 0.25),
    "zap2": ("zaps", 2.06, 0.8, 0.0, 0.25),
    "zap3": ("zaps", 4.08, 0.8, 0.0, 0.25),
    "zap4": ("zaps", 6.08, 0.8, 0.0, 0.25),
    # going as light: Yata no Kagami, the speed-of-light kick, the dash
    "travel1": ("teleport", 0.08, 2.6, 0.0, 1.1),
    "travel2": ("zip", 0.0, None, 0.0, 0.2),
    # the kicks: the swish, the blow landing, the hardest blows
    "whoosh1": ("woosh", 0.12, 0.9, 0.0, 0.45),
    "whoosh2": ("woosh_mid", 0.0, 1.0, 0.0, 0.45),
    "hit1": ("punches", 1.17, 0.35, 0.0, 0.15),
    "hit2": ("punches", 6.21, 0.35, 0.0, 0.15),
    "hit3": ("punches", 7.77, 0.35, 0.0, 0.15),
    "hit4": ("punches", 11.53, 0.4, 0.0, 0.18),
    "hit_heavy1": ("hard_hit", 1.73, 1.4, 0.0, 0.8),
    "hit_heavy2": ("hard_hit", 3.77, 1.4, 0.0, 0.8),
    "kick_blast": ("void_punch", 0.0, None, 0.0, 0.2),
    # Ama no Murakumo: drawn, its cuts, put away; the guard
    "sword_on": ("saber", 0.0, 0.62, 0.0, 0.2),
    "swing1": ("saber", 1.15, 0.75, 0.03, 0.3),
    "swing2": ("saber", 2.35, 0.75, 0.03, 0.3),
    "swing3": ("saber_hum", 50.4, 0.75, 0.03, 0.3),
    "swing4": ("saber_hum", 51.15, 0.7, 0.03, 0.3),
    "swing5": ("saber_hum", 51.82, 0.75, 0.03, 0.3),
    "sword_off": ("saber", 3.45, 1.2, 0.02, 0.6),
    "parry1": ("parry", 0.0, None, 0.0, 0.5),
    # the clones of light, a blow passing through the body of light, the mirror forming
    "sparkle1": ("sparkle", 0.3, 2.2, 0.02, 1.0),
    "sparkle2": ("transition", 0.1, 1.6, 0.02, 0.6),
    # 2.9.6: the light reflected off a block or the water
    "reflect1": ("glass_ping", 0.0, None, 0.0, 0.12),
}

# our loop: (sound, start s, length s, crossfade s): the light held at the fingertip, the sword's hum
LOOPS = {
    "charge_hold": ("spell_loop", 0.3, 4.0, 0.6),
    "hum": ("saber_hum", 20.8, 2.4, 0.5),
    # 2.9.6: the body gone as light, round and round while Z is held (faded out the moment it reforms)
    "light_travel": ("beam_loop", 0.0, 1.6, 0.4),
}


def main():
    only = None
    if len(sys.argv) > 2 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    web_sounds.build(SOUNDS, CLIPS, LOOPS, OUT, "The Pika Pika no Mi's sounds (tools/pika_sounds.py).", only)


if __name__ == "__main__":
    sys.exit(main())
