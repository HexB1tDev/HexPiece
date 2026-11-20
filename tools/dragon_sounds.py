#!/usr/bin/env python3
"""2.8.6: the Uo Uo no Mi's sounds, every one from the web, free to use (the owner: "giving every move of the dragon custom sounds from
the web. And I mean, don't make any sound yourself. They all have to be from the web, and they have to be the best versions they can be.
So pick something epic for everything. Wind sound effects, fire sound effects, fire like blasts, fire explosions, charging, everything
comes from the web").

Every clip is a CC0 (public domain) recording on Freesound.org, chosen for its rating and downloads; this script fetches each one's
high-quality preview, checks on its page that it is CC0, cuts the part used (seconds into it), makes it mono (Minecraft places only mono
sounds in the world), peak-normalises it to -1 dBFS, fades it in and out and writes it as Ogg Vorbis into
src/main/resources/assets/hexhaki/sounds/piece/uo/, with SOURCES.txt naming every clip's origin. The build never downloads: the .ogg
files are kept in the repository.

Usage: python3 tools/dragon_sounds.py   (needs ffmpeg and access to freesound.org and cdn.freesound.org)
"""
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/uo")

# Freesound sound: (author's page name, sound id)
SOUNDS = {
    "roar_dreeme": ("Dreeme", 546391),            # "Dragon Roar.wav", rated 4.9
    "roar_joel": ("JoelAudio", 85568),            # "DRAGON_ROAR.wav", 27k downloads
    "vortex_boom": ("modusmogulus", 785947),       # "Trailer Vortex Boom"
    "heavy_whoosh": ("SoundBiterSFX", 730959),     # "Heavy Whoosh"
    "low_whoosh": ("FALL-E", 697432),              # "Heavy Whoosh, Swoosh, Low"
    "instant_wind": ("Raclure", 483599),           # "Instant Wind", rated 4.9
    "gusts": ("SamsterBirdies", 565140),           # "Heavy gusts of wind"
    "hurricane": ("Vrymaa", 735350),               # "Wind - Hurricane Typhoon Storm"
    "vortex": ("ShlockFest", 489396),              # "wooshing vortex"
    "swirl": ("Qvantor", 877983),                  # "Whoosh Swirl Spinning"
    "flamethrower": ("AslakHostaker", 395039),     # "Flamethrower_Weapon_Long_Bursts", rated 4.8
    "fire_breath": ("bspiller5", 157616),          # "Fire Breath.wav", 8k downloads
    "dragon_fire": ("soundmast123", 532156),       # "dragon breathe fire", rated 4.9
    "charge": ("Sojan", 407052),                   # "Power Charge", rated 4.8
    "massive": ("Hugofski", 177556),               # "massive explosion"
    "fire_explosion": ("ReadeOnly", 186932),       # "fire_explosion.wav", 13k downloads
    "large_explosion": ("V-ktor", 482993),         # "large explosion 1"
    "fireball": ("julianmateo_", 522705),          # "Fireball, Woosh, Pass, fast", rated 4.9
    "fireball_hit": ("Blankened", 431174),         # "Fireball Explosion.wav", 8k downloads
    "flame_burst": ("magnuswaker", 592572),        # "Flame Burst"
    "deep_boom": ("Za-Games", 539968),             # "Deep Boom", rated 4.9
    "bite": ("LucasDuff", 467701),                 # "monster bite", 9k downloads
}

# our file: (sound, start s, length s or None for the rest, fade in s, fade out s)
CLIPS = {
    # the dragon's roars: the transformation, the bite, the blast's release
    "roar1": ("roar_dreeme", 0.0, 4.4, 0.01, 0.9),
    "roar2": ("roar_dreeme", 5.9, 3.6, 0.02, 0.8),
    "roar3": ("roar_joel", 0.0, None, 0.0, 0.4),
    # the body forming out of the player, and going back
    "form_boom": ("vortex_boom", 0.0, None, 0.0, 0.8),
    "whoosh1": ("heavy_whoosh", 11.3, 3.2, 0.05, 0.9),
    "whoosh2": ("heavy_whoosh", 16.9, 3.0, 0.05, 0.9),
    "whoosh3": ("heavy_whoosh", 0.9, 2.6, 0.05, 0.8),
    "unform": ("low_whoosh", 47.0, 4.3, 0.1, 1.2),
    "lunge": ("low_whoosh", 25.6, 3.2, 0.03, 1.0),
    # wind: flight's gusts, the Air Pressure shot, the Wind Force tornado
    "gust1": ("gusts", 44.0, 4.5, 0.6, 1.4),
    "gust2": ("gusts", 11.0, 4.0, 0.6, 1.3),
    "gust3": ("hurricane", 41.0, 4.0, 0.6, 1.3),
    "air1": ("instant_wind", 1.0, 2.8, 0.05, 0.9),
    "air2": ("swirl", 0.4, 1.6, 0.02, 0.6),
    "air_hit": ("deep_boom", 0.0, 2.2, 0.0, 0.8),
    "tornado1": ("vortex", 50.0, 4.5, 0.5, 1.5),
    "tornado2": ("hurricane", 60.0, 4.5, 0.5, 1.5),
    # fire: the breath's ignition and its roar held, the beam, the embers
    "flame_start": ("flamethrower", 2.0, 2.6, 0.0, 0.6),
    "flame_loop1": ("flamethrower", 5.0, 3.0, 0.15, 0.6),
    "flame_loop2": ("flamethrower", 16.0, 3.0, 0.15, 0.6),
    "beam1": ("dragon_fire", 0.0, 5.0, 0.0, 1.6),
    "beam2": ("fire_breath", 1.2, 4.0, 0.05, 1.4),
    "charge": ("charge", 0.3, 3.2, 0.05, 0.3),
    "fireball1": ("fireball", 0.9, 2.1, 0.02, 0.5),
    "ember_hit1": ("fireball_hit", 0.0, None, 0.0, 0.3),
    "ember_hit2": ("flame_burst", 0.0, 1.6, 0.0, 0.6),
    # explosions: the fireballs' and the beam's blasts, the last and biggest
    "explode1": ("fire_explosion", 1.2, None, 0.0, 0.8),
    "explode2": ("large_explosion", 0.0, None, 0.0, 0.9),
    "blast1": ("massive", 7.3, 6.2, 0.0, 2.5),
    # the Gigantic Bite's jaws
    "bite1": ("bite", 0.3, None, 0.0, 0.3),
}


def page(user, sid):
    return f"https://freesound.org/people/{user}/sounds/{sid}/"


def fetch(user, sid, into):
    html = subprocess.run(["curl", "-s", "--max-time", "30", "-A", "Mozilla/5.0", page(user, sid)], capture_output=True, text=True,
                          check=True).stdout
    if "creativecommons.org/publicdomain/zero/1.0" not in html:
        raise RuntimeError(f"{page(user, sid)} is not CC0: not used")
    title = re.search(r'data-title="([^"]*)"', html).group(1)
    preview = re.search(r'data-ogg="([^"]*)"', html).group(1).replace("-lq.ogg", "-hq.ogg")
    subprocess.run(["curl", "-s", "--max-time", "180", "-o", into, preview], check=True)
    return title, preview


def convert(src, start, length, fade_in, fade_out, out):
    cut = ["-ss", f"{start:.3f}"] + (["-t", f"{length:.3f}"] if length else [])
    probe = subprocess.run(["ffmpeg", "-v", "info", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-af", "volumedetect", "-f", "null", "-"],
                           capture_output=True, text=True).stderr
    peak = 0.0
    for line in probe.splitlines():
        if "max_volume:" in line:
            peak = float(line.split("max_volume:")[1].split("dB")[0])
    raw = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-ar", "44100", "-f", "s16le", "-"],
                         capture_output=True, check=True).stdout
    seconds = len(raw) / (2 * 44100.0)
    chain = [f"volume={-1.0 - peak:.2f}dB"]
    if fade_in > 0:
        chain.append(f"afade=t=in:d={fade_in:.3f}:curve=qsin")
    chain.append(f"afade=t=out:st={max(0.0, seconds - fade_out):.3f}:d={max(0.015, fade_out):.3f}:curve=cub")
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-ar", "44100", "-af", ",".join(chain),
                    "-c:a", "libvorbis", "-q:a", "6", out], check=True)
    return seconds


def main():
    os.makedirs(OUT, exist_ok=True)
    work = tempfile.mkdtemp()
    got = {}
    for key, (user, sid) in SOUNDS.items():
        path = os.path.join(work, f"{sid}.ogg")
        got[key] = (path,) + fetch(user, sid, path)
        print(f"fetched {key}: {got[key][1]} ({page(user, sid)})")
    lines = ["The Uo Uo no Mi's sounds (tools/dragon_sounds.py). Every recording is CC0 1.0 (public domain) on Freesound.org; the files",
             "here are cut from its high-quality preview, made mono, peak-normalised and faded. Thanks to every author named.", ""]
    for name, (key, start, length, fade_in, fade_out) in CLIPS.items():
        user, sid = SOUNDS[key]
        path, title, preview = got[key]
        seconds = convert(path, start, length, fade_in, fade_out, os.path.join(OUT, name + ".ogg"))
        lines.append(f"{name}.ogg: \"{title}\" by {user}, {page(user, sid)} (CC0 1.0), from {start:.1f} s, {seconds:.2f} s long")
        print(f"wrote {name}.ogg ({seconds:.2f} s)")
    with open(os.path.join(OUT, "SOURCES.txt"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    sys.exit(main())
