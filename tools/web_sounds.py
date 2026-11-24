#!/usr/bin/env python3
"""2.9.0: what tools/dragon_sounds.py and tools/pika_sounds.py share: every sound from the web, never made by hand (the owner: "don't
make any sound yourself. They all have to be from the web"; "you cannot fucking create sounds").

A recording is a CC0 (public domain) sound on Freesound.org: its page is read to check the licence, its high-quality preview fetched
(kept in $SOUND_CACHE when set, so a second run downloads nothing). A clip is a part of one recording cut out, made mono (Minecraft
places only mono sounds in the world), peak-normalised to -1 dBFS, faded in and out and written as Ogg Vorbis. A loop is a part cut
the same way whose end is crossfaded into its start, so it can be played round and round without a seam or a click (a charge held,
a sword humming). Nothing is synthesised: every sample in a file is the recording's own.
"""
import array
import math
import os
import re
import subprocess
import tempfile

CACHE = os.environ.get("SOUND_CACHE") or tempfile.mkdtemp()


def page(user, sid):
    return f"https://freesound.org/people/{user}/sounds/{sid}/"


def fetch(user, sid):
    """The recording's file (downloaded once), its title and preview URL; refuses anything that is not CC0."""
    os.makedirs(CACHE, exist_ok=True)
    out = os.path.join(CACHE, f"{sid}.ogg")
    html = ""
    for _ in range(4):
        html = subprocess.run(["curl", "-s", "--max-time", "30", "-A", "Mozilla/5.0", page(user, sid)], capture_output=True,
                              text=True).stdout
        if 'data-ogg="' in html:
            break
    if "creativecommons.org/publicdomain/zero/1.0" not in html:
        raise RuntimeError(f"{page(user, sid)} is not CC0 (or did not load): not used")
    title = re.search(r'data-title="([^"]*)"', html).group(1)
    preview = re.search(r'data-ogg="([^"]*)"', html).group(1).replace("-lq.ogg", "-hq.ogg")
    if not os.path.exists(out) or os.path.getsize(out) == 0:
        subprocess.run(["curl", "-s", "--max-time", "180", "-o", out, preview], check=True)
    return out, title, preview


# 3.6.0 (the owner: "all lightning sounds are to change to new more bass boosted ones (get from the web)"): the recording's own punchy
# low end lifted (a +7 dB shelf at 120 Hz, the 80 to 200 Hz speakers play) and nothing under 45 Hz kept (never sub: 2.4.8's lesson).
# An equaliser on the recording itself: nothing is added to it.
BASS = "highpass=f=45:poles=2,bass=g=7:f=120:w=0.9"
# 3.6.8: the room's low hum (a steady line under 90 Hz some recordings carry) filtered out: an equaliser on the recording, nothing added
HUM = "highpass=f=95:poles=2,highpass=f=95:poles=2"


def pcm(src, start, length, eq=None):
    """The part of {src} from {start} for {length} seconds (None: to the end), mono 44.1 kHz floats; {eq} an ffmpeg filter (BASS)."""
    cut = ["-ss", f"{start:.3f}"] + (["-t", f"{length:.3f}"] if length else [])
    af = ["-af", eq] if eq else []
    raw = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", src, "-ac", "1", "-ar", "44100"] + af + ["-f", "f32le", "-"],
                         capture_output=True, check=True).stdout
    a = array.array("f")
    a.frombytes(raw[:len(raw) // 4 * 4])
    return a


def write(samples, out):
    """Floats to Ogg Vorbis (bit-exact: the same input gives the same file)."""
    with tempfile.NamedTemporaryFile(suffix=".f32", delete=False) as handle:
        handle.write(samples.tobytes())
        tmp = handle.name
    try:
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-hide_banner", "-f", "f32le", "-ar", "44100", "-ac", "1", "-i", tmp, "-c:a",
                        "libvorbis", "-q:a", "6", "-fflags", "+bitexact", "-flags:a", "+bitexact", "-serial_offset", "7", out],
                       check=True)
    finally:
        os.unlink(tmp)


def normalise(a, peak_db=-1.0):
    peak = max((abs(v) for v in a), default=0.0)
    if peak > 1.0e-6:
        g = 10.0 ** (peak_db / 20.0) / peak
        for i in range(len(a)):
            a[i] *= g
    return a


def punch(a):
    """2.9.3: a recording made to sound louder without going past full scale: its quiet parts brought up under its peaks (a compressor,
    then a limiter), as a mix engineer does; nothing is added to it."""
    with tempfile.NamedTemporaryFile(suffix=".f32", delete=False) as handle:
        handle.write(a.tobytes())
        tmp = handle.name
    try:
        raw = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner", "-f", "f32le", "-ar", "44100", "-ac", "1", "-i", tmp, "-af",
                              "acompressor=threshold=-20dB:ratio=4:attack=8:release=250:makeup=8,alimiter=limit=0.95",
                              "-f", "f32le", "-ar", "44100", "-ac", "1", "-"], capture_output=True, check=True).stdout
    finally:
        os.unlink(tmp)
    b = array.array("f")
    b.frombytes(raw[:len(raw) // 4 * 4])
    return b


def clip(src, start, length, fade_in, fade_out, out, gain_db=0.0, louder=False, eq=None):
    """One clip of a recording (see the module), {gain_db} under full scale less 1 dB, {louder} pushed (punch), {eq} its equaliser
    (BASS); returns its length."""
    a = pcm(src, start, length, eq)
    if louder:
        a = punch(a)
    n = len(a)
    fi = int(fade_in * 44100)
    fo = max(int(0.015 * 44100), int(fade_out * 44100))
    for i in range(min(fi, n)):
        a[i] *= math.sin(0.5 * math.pi * i / fi)
    for i in range(min(fo, n)):
        t = i / fo
        a[n - 1 - i] *= t * t * t  # cubic: the tail dies away
    normalise(a, -1.0 + gain_db)
    write(a, out)
    return n / 44100.0


def loop(src, start, length, cross, out, gain_db=0.0):
    """A seamless loop of {length} seconds from {start}: its last {cross} seconds crossfaded (equal power) into its first."""
    a = pcm(src, start, length + cross)
    c = int(cross * 44100)
    n = len(a) - c
    b = array.array("f", a[:n])
    for i in range(c):
        t = i / c
        # the start of the loop is the recording's run-on past its end fading out under its own beginning fading in
        b[i] = a[i] * math.sin(0.5 * math.pi * t) + a[n + i] * math.cos(0.5 * math.pi * t)
    normalise(b, -1.0 + gain_db)
    write(b, out)
    return n / 44100.0


def build(sounds, clips, loops, out_dir, title, only=None, level=None):
    """Fetches every recording, writes every clip and loop (or {only} those named) into {out_dir} with its SOURCES.txt."""
    os.makedirs(out_dir, exist_ok=True)
    got = {}
    for key, (user, sid) in sounds.items():
        got[key] = fetch(user, sid)
        print(f"fetched {key}: {got[key][1]} ({page(user, sid)})")
    lines = [title + " Every recording is CC0 1.0 (public domain) on Freesound.org; the files",
             "here are cut from its high-quality preview, made mono, peak-normalised and faded (a loop: its end crossfaded into its",
             "start). Nothing is made by hand. Thanks to every author named.", ""]
    for name, spec in list(clips.items()) + list(loops.items()):
        key = spec[0]
        user, sid = sounds[key]
        path, rec_title, _ = got[key]
        target = os.path.join(out_dir, name + ".ogg")
        if only and name not in only:
            scratch = os.path.join(tempfile.mkdtemp(), name + ".ogg")
        else:
            scratch = target
        gain, louder = level(name) if level else (0.0, False)
        if name in loops:
            _, start, length, cross = spec[:4]
            seconds = loop(path, start, length, cross, scratch, gain)
            what = f"a loop from {start:.1f} s, {seconds:.2f} s long"
        else:
            _, start, length, fade_in, fade_out = spec[:5]
            eq = spec[5] if len(spec) > 5 else None
            seconds = clip(path, start, length, fade_in, fade_out, scratch, gain, louder, eq)
            what = f"from {start:.1f} s, {seconds:.2f} s long" + (", its low end lifted (bass +7 dB at 120 Hz, nothing under 45 Hz)" if eq == BASS
                                                               else ", the room's hum under 95 Hz filtered out" if eq == HUM else "")
        if gain:
            what += f", {-gain:.1f} dB quieter"
        if louder:
            what += ", compressed to sound louder"
        lines.append(f"{name}.ogg: \"{rec_title}\" by {user}, {page(user, sid)} (CC0 1.0), {what}")
        if scratch == target:
            print(f"wrote {name}.ogg ({seconds:.2f} s)")
    with open(os.path.join(out_dir, "SOURCES.txt"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")
