#!/usr/bin/env python3
"""2.2.9: the Swordsman's sounds, taken from the web, free to use (the owner: "grab sounds from the web for the swords unsheath, swipe,
etc, all the sounds must come from the web (be free for use)"). Every clip comes from a CC0 (public domain) pack on OpenGameArt.org,
downloaded here, trimmed, made mono (Minecraft places only mono sounds in the world), peak-normalised, faded out and written as Ogg
Vorbis into src/main/resources/assets/hexhaki/sounds/piece/sword/, with SOURCES.txt beside them naming every clip's origin.

Usage: python3 tools/sword_sounds.py   (needs ffmpeg and access to opengameart.org; the build never downloads: the .ogg files are kept)
"""
import io
import os
import subprocess
import sys
import tempfile
import urllib.request
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/hexhaki/sounds/piece/sword")
BASE = "https://opengameart.org/sites/default/files/"

# pack key: (download, OpenGameArt page, author, licence)
PACKS = {
    "rpg": ("rpg_sound_pack.zip", "https://opengameart.org/content/rpg-sound-pack", "artisticdude", "CC0 1.0"),
    "swishes": ("swishes.zip", "https://opengameart.org/content/swishes-sound-pack", "artisticdude", "CC0 1.0"),
    "starninjas": ("sword_-_starninjas_1.zip", "https://opengameart.org/content/20-sword-sound-effects-attacks-and-clashes", "StarNinjas",
                   "CC0 1.0"),
    "clashes": ("sword_clash_-_starninjas_0.zip", "https://opengameart.org/content/20-sword-sound-effects-attacks-and-clashes",
                "StarNinjas", "CC0 1.0"),
    "vehicle": ("weapons-apparel.zip", "https://opengameart.org/content/fantasy-weapons-and-apparel-sfx-library", "Vehicle (Jan Schupke)",
                "CC0 1.0"),
    "whoosh": ("whoosh2_0.wav", "https://opengameart.org/content/air-whoosh", "pyranostudios", "CC0 1.0"),
}

# our file: (pack, path inside it, start s, length s or None)
CLIPS = {}
for i in range(1, 6):
    CLIPS[f"draw_ring{i}"] = ("rpg", f"RPG Sound Pack/battle/sword-unsheathe{'' if i == 1 else i}.wav", 0.0, None)
for i, n in enumerate((3, 4, 2), 1):
    CLIPS[f"draw_slide{i}"] = ("vehicle", f"sfx/knife-unsheathe-0{n}.wav", 0.0, None)
for i, n in enumerate((2, 4), 1):
    CLIPS[f"guard_click{i}"] = ("vehicle", f"sfx/seax-sheathe-0{n}.wav", 0.0, None)
for i, n in enumerate((2, 5), 1):
    CLIPS[f"sheathe_slide{i}"] = ("vehicle", f"sfx/knife-sheathe-0{n}.wav", 0.0, None)
for i in range(1, 14):
    CLIPS[f"swing{i}"] = ("swishes", f"swishes/swish-{i}.wav", 0.0, None)
for i, n in enumerate((2, 3, 4, 6, 8), 1):
    CLIPS[f"shing{i}"] = ("starninjas", f"sword - StarNinjas/sword.{n}.ogg", 0.0, None)
for i, n in enumerate((2, 3, 5, 8, 10), 1):
    CLIPS[f"clash{i}"] = ("clashes", f"sword_clash.{n}.ogg", 0.0, None)
# the air whoosh is a loop of five rushes: two of them, one each
CLIPS["whoosh1"] = ("whoosh", None, 0.0, 1.0)
CLIPS["whoosh2"] = ("whoosh", None, 1.05, 1.0)


def fetch(name):
    with urllib.request.urlopen(BASE + name, timeout=120) as response:
        return response.read()


def convert(data, suffix, start, length, out):
    """A clip as Minecraft wants it: mono 44.1 kHz Ogg Vorbis, the silence round it trimmed, peak at -1 dBFS, a 15 ms fade-out."""
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as handle:
        handle.write(data)
        src = handle.name
    try:
        cut = ["-ss", str(start)] + (["-t", str(length)] if length else [])
        trim = ("silenceremove=start_periods=1:start_threshold=-55dB:start_silence=0.005,"
                "areverse,silenceremove=start_periods=1:start_threshold=-55dB:start_silence=0.02,areverse")
        # measure the peak, then normalise to -1 dBFS
        probe = subprocess.run(["ffmpeg", "-v", "info", "-hide_banner"] + cut + ["-i", src, "-af", trim + ",volumedetect", "-f", "null", "-"],
                               capture_output=True, text=True).stderr
        peak = 0.0
        for line in probe.splitlines():
            if "max_volume:" in line:
                peak = float(line.split("max_volume:")[1].split("dB")[0])
        gain = -1.0 - peak
        # the clip's length after trimming, for the fade
        dur = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", src, "-af", trim, "-f", "wav", "-"],
                             capture_output=True).stdout
        seconds = max(0.05, (len(dur) - 44) / (2 * 44100.0)) if dur else 0.5
        fade = f"afade=t=out:st={max(0.0, seconds - 0.015):.3f}:d=0.015"
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-ar", "44100",
                        "-af", f"{trim},volume={gain:.2f}dB,{fade}", "-c:a", "libvorbis", "-q:a", "5", out], check=True)
    finally:
        os.unlink(src)


def main():
    os.makedirs(OUT, exist_ok=True)
    packs = {}
    for key, (name, *_rest) in PACKS.items():
        print("downloading", name)
        packs[key] = fetch(name)
    lines = ["HexPiece sword sounds: every clip below is from a CC0 1.0 (public domain) pack on OpenGameArt.org, free to use for any",
             "purpose, no attribution required; credited here with thanks. Each was trimmed, made mono, normalised and faded out",
             "(tools/sword_sounds.py).", ""]
    for ours, (pack, inner, start, length) in CLIPS.items():
        name, page, author, licence = PACKS[pack]
        if inner is None:
            data, suffix = packs[pack], os.path.splitext(name)[1]
        else:
            with zipfile.ZipFile(io.BytesIO(packs[pack])) as z:
                match = [n for n in z.namelist() if n.endswith(inner) and "__MACOSX" not in n]
                if not match:
                    sys.exit(f"{inner} is not in {name}")
                data, suffix = z.read(match[0]), os.path.splitext(inner)[1]
        convert(data, suffix, start, length, os.path.join(OUT, ours + ".ogg"))
        where = inner or name
        part = f" ({start:.2f}-{start + length:.2f} s)" if length else ""
        lines.append(f"{ours}.ogg: \"{os.path.basename(where)}\"{part} from \"{os.path.splitext(name)[0]}\" by {author}, {licence}, {page}")
        print(ours)
    with open(os.path.join(OUT, "SOURCES.txt"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
