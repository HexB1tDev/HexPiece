#!/usr/bin/env python3
"""2.2.9: the Swordsman's sounds, taken from the web, free to use (the owner: "grab sounds from the web for the swords unsheath, swipe,
etc, all the sounds must come from the web (be free for use)"). Every clip comes from a CC0 (public domain) pack on OpenGameArt.org,
downloaded here, trimmed, made mono (Minecraft places only mono sounds in the world), peak-normalised, faded out and written as Ogg
Vorbis into src/main/resources/assets/hexhaki/sounds/piece/sword/, with SOURCES.txt beside them naming every clip's origin.

Usage: python3 tools/sword_sounds.py   (needs ffmpeg and access to opengameart.org; the build never downloads: the .ogg files are kept)
"""
import array
import io
import math
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

# our file: (pack, path inside it, start s, length s or None, cut, shape)
#   cut: None (from `start`, the silence trimmed), "onset" (from where it first rises within 20 dB of its loudest moment: the noise
#        floor and quiet lead-in dropped, so the hit lands on its tick) or "click" (from the attack of its loudest transient: a
#        sheathe's slide dropped, only the guard's click left)
#   shape: None, or (fade in s, fade out from s): the air whoosh is a steady loop; shaped, each piece is one rush that rises and dies
CLIPS = {}
for i in range(1, 6):
    CLIPS[f"draw_ring{i}"] = ("rpg", f"RPG Sound Pack/battle/sword-unsheathe{'' if i == 1 else i}.wav", 0.0, None, None, None)
for i, n in enumerate((3, 4, 2), 1):
    CLIPS[f"draw_slide{i}"] = ("vehicle", f"sfx/knife-unsheathe-0{n}.wav", 0.0, None, None, None)
# 2.2.9: the seax sheathes are a slide ending in the guard's click: only the click (the first cut began 0.12 s and 0.55 s early)
for i, n in enumerate((2, 4, 3, 5), 1):
    CLIPS[f"guard_click{i}"] = ("vehicle", f"sfx/seax-sheathe-0{n}.wav", 0.0, None, "click", None)
for i, n in enumerate((2, 5), 1):
    CLIPS[f"sheathe_slide{i}"] = ("vehicle", f"sfx/knife-sheathe-0{n}.wav", 0.0, None, None, None)
for i in range(1, 14):
    CLIPS[f"swing{i}"] = ("swishes", f"swishes/swish-{i}.wav", 0.0, None, None, None)
for i, n in enumerate((2, 3, 4, 6, 8), 1):
    CLIPS[f"shing{i}"] = ("starninjas", f"sword - StarNinjas/sword.{n}.ogg", 0.0, None, "onset", None)
for i, n in enumerate((2, 3, 5, 8, 10), 1):
    CLIPS[f"clash{i}"] = ("clashes", f"sword_clash.{n}.ogg", 0.0, None, "onset", None)
# the air whoosh is a loop of five swells (1.05 s apart, loudest at 0.4 s, 1.45 s...): two long rushes for the Haki slash, each
# rising out of nothing and dying away, and two short ones for the heavy cuts, cut round a swell's peak
CLIPS["whoosh1"] = ("whoosh", None, 0.0, 1.0, None, (0.06, 0.3))
CLIPS["whoosh2"] = ("whoosh", None, 1.05, 1.0, None, (0.06, 0.25))
CLIPS["rush1"] = ("whoosh", None, 2.3, 0.36, None, (0.05, 0.1))
CLIPS["rush2"] = ("whoosh", None, 3.35, 0.36, None, (0.05, 0.08))

def fetch(name):
    with urllib.request.urlopen(BASE + name, timeout=120) as response:
        return response.read()


def envelope(src, cut):
    """The clip's loudness (dB) in 5 ms steps, as ffmpeg decodes it."""
    raw = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", src, "-f", "f32le", "-ac", "1", "-ar", "44100", "-"],
                         capture_output=True, check=True).stdout
    samples = array.array("f")
    samples.frombytes(raw[:len(raw) // 4 * 4])
    step = 220
    out = []
    for i in range(0, len(samples) - step + 1, step):
        power = sum(v * v for v in samples[i:i + step]) / step
        out.append(10.0 * math.log10(power + 1e-18))
    return out


def attack(src, cut, how):
    """Seconds into the cut where the sound proper begins (see CLIPS: "onset" or "click")."""
    env = envelope(src, cut)
    if not env:
        return 0.0
    peak = max(range(len(env)), key=lambda i: env[i])
    if how == "onset":
        first = next(i for i, v in enumerate(env) if v > env[peak] - 20.0)
        return max(0.0, first * 0.005 - 0.010)
    i = peak
    while i > 0 and env[i - 1] > env[peak] - 10.0:
        i -= 1
    return max(0.0, i * 0.005 - 0.005)


def convert(data, suffix, start, length, how, shape, out):
    """A clip as Minecraft wants it: mono 44.1 kHz Ogg Vorbis, the silence round it trimmed, peak at -1 dBFS, a 15 ms fade-out."""
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as handle:
        handle.write(data)
        src = handle.name
    try:
        if how:
            lead = attack(src, ["-ss", str(start)] + (["-t", str(length)] if length else []), how)
            start += lead
            if length:
                length -= lead
        cut = ["-ss", f"{start:.3f}"] + (["-t", f"{length:.3f}"] if length else [])
        trim = ("silenceremove=start_periods=1:start_threshold=-55dB:start_silence=0.005,"
                "areverse,silenceremove=start_periods=1:start_threshold=-55dB:start_silence=0.02,areverse")
        if shape:
            trim = ""  # a piece of a steady loop: nothing to trim, its rise and fall are made below
        # measure the peak, then normalise to -1 dBFS
        probe = subprocess.run(["ffmpeg", "-v", "info", "-hide_banner"] + cut + ["-i", src, "-af", (trim + "," if trim else "")
                               + "volumedetect", "-f", "null", "-"], capture_output=True, text=True).stderr
        peak = 0.0
        for line in probe.splitlines():
            if "max_volume:" in line:
                peak = float(line.split("max_volume:")[1].split("dB")[0])
        gain = -1.0 - peak
        # the clip's length after trimming, for the fade
        dur = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", src] + (["-af", trim] if trim else [])
                             + ["-ac", "1", "-f", "wav", "-"], capture_output=True).stdout
        seconds = max(0.05, (len(dur) - 44) / (2 * 44100.0)) if dur else 0.5
        if shape:
            rise, fall = shape
            fade = f"afade=t=in:d={rise:.3f}:curve=qsin,afade=t=out:st={fall:.3f}:d={max(0.05, seconds - fall):.3f}:curve=cub"
        else:
            fade = f"afade=t=out:st={max(0.0, seconds - 0.015):.3f}:d=0.015"
        chain = (trim + "," if trim else "") + f"volume={gain:.2f}dB,{fade}"
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-ar", "44100",
                        "-af", chain, "-c:a", "libvorbis", "-q:a", "5", out], check=True)
        return start, length
    finally:
        os.unlink(src)

def main():
    os.makedirs(OUT, exist_ok=True)
    packs = {}
    for key, (name, *_rest) in PACKS.items():
        print("downloading", name)
        packs[key] = fetch(name)
    lines = ["HexPiece sword sounds: every clip below is from a CC0 1.0 (public domain) pack on OpenGameArt.org, free to use for any",
             "purpose, no attribution required; credited here with thanks. Each was trimmed (the guard clicks, shings and clashes cut to their",
             "attack, so they land on their tick; the steady air whoosh cut into single rushes that rise and die), made mono,",
             "normalised and faded out (tools/sword_sounds.py).", ""]
    for ours, (pack, inner, start, length, how, shape) in CLIPS.items():
        name, page, author, licence = PACKS[pack]
        if inner is None:
            data, suffix = packs[pack], os.path.splitext(name)[1]
        else:
            with zipfile.ZipFile(io.BytesIO(packs[pack])) as z:
                match = [n for n in z.namelist() if n.endswith(inner) and "__MACOSX" not in n]
                if not match:
                    sys.exit(f"{inner} is not in {name}")
                data, suffix = z.read(match[0]), os.path.splitext(inner)[1]
        start, length = convert(data, suffix, start, length, how, shape, os.path.join(OUT, ours + ".ogg"))
        where = inner or name
        part = f" ({start:.2f}-{start + length:.2f} s)" if length else (f" (from {start:.2f} s)" if start > 0 else "")
        lines.append(f"{ours}.ogg: \"{os.path.basename(where)}\"{part} from \"{os.path.splitext(name)[0]}\" by {author}, {licence}, {page}")
        print(ours)
    with open(os.path.join(OUT, "SOURCES.txt"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
