#!/usr/bin/env python3
"""2.6.0: the Conqueror's release explosion (sound event piece.conqueror.explosion), made from CC0 recordings from the web.

The owner (testing 2.5.9): "after the key is let go, add a proper explosion sound (FROM THE WEB)". Every letting go of G plays it at the
moment of release (server/ConquerorSounds.explode: a full charge at full volume, a partial one by its charge, a tap small).

The sources (all CC0 1.0, public domain, free for any use, no attribution required; credited in SOURCES.txt) come from the GitHub
collection lavenderdotpet/CC0-Public-Domain-Sounds (commit f2b6264f9ab89fabc266914c3654685d68c5a39b, its LICENSE is CC0 1.0):
  - the body:   "50-cc0-sci-fi-sfx/explosion_01.ogg" (rubberduck's "50 CC0 Sci-Fi SFX", OpenGameArt)
  - the crack:  "25-CC0-bang-sfx/cannon_01.ogg" (rubberduck's "25 CC0 bang / firework SFX", OpenGameArt)
  - the punch:  "warfork-cc0/sounds/weapons/gren_strong_explosion2.ogg" (Warfork's CC0 assets, Team Forbidden)
  - the rumble: "sci-fi-sounds/Audio/lowFrequency_explosion_000.ogg" (Kenney's "Sci-fi Sounds", CC0)

Each is made mono at 44.1 kHz, lined up on its onset (the first sample over a tenth of its peak) and faded out over its last
quarter (a layer cut off while still sounding clicks); the mix is lowered a little in
pitch (bigger), given a rolling tail (a convolution with 2.6 s of decaying, darkened noise: the boom rolling away over open ground),
the sub no speaker plays cut (the 2.4.8 lesson: sub-bass only eats the limiter's headroom), the low end held down and the roar and
crack raised (LOW_SHELF), compressed gently and brought to TARGET LUFS into a slow limiter, faded out, written as mono Ogg Vorbis (positional playback).

Usage (ffmpeg with libvorbis, numpy):
  git clone --filter=blob:none --no-checkout https://github.com/lavenderdotpet/CC0-Public-Domain-Sounds SRC
  git -C SRC checkout f2b6264f9ab89fabc266914c3654685d68c5a39b -- <the four paths above>
  python3 tools/conqueror_explosion.py SRC
"""
import os
import subprocess
import sys

import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "sounds", "piece", "conqueror")
RATE = 44100

# (path in the collection, gain, delay in seconds after the common onset)
LAYERS = [
    ("50-cc0-sci-fi-sfx/explosion_01.ogg", 1.0, 0.0),
    ("25-CC0-bang-sfx/cannon_01.ogg", 0.55, 0.0),
    ("warfork-cc0/sounds/weapons/gren_strong_explosion2.ogg", 0.32, 0.004),
    ("sci-fi-sounds/Audio/lowFrequency_explosion_000.ogg", 0.55, 0.03),
]
# the whole lowered by this much in pitch (and lengthened by its inverse)
PITCH = 0.88
# the tail: its length (s), how fast it dies (dB per second), how dark (low-pass, Hz) and how loud against the dry sound
TAIL = 2.6
TAIL_DECAY = 26.0
TAIL_DARK = 1400
TAIL_WET = 0.42
# 2.6.0: the owner plays on a phone (Android): its speaker plays little under 200 Hz, so the weight is kept moderate and the roar and
# crack (0.5-3 kHz) brought up, where a phone, a laptop and headphones all play it
LOW_SHELF = -6
TARGET = -11.0
LENGTH = 3.6


def load(path):
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", path, "-ac", "1", "-ar", str(RATE), "-f", "f32le", "-"], capture_output=True,
                         check=True).stdout
    return np.frombuffer(raw, np.float32).astype(np.float64)


def onset(x):
    a = np.abs(x)
    return int(np.argmax(a > 0.1 * a.max()))


def loudness(path):
    out = subprocess.run(["ffmpeg", "-hide_banner", "-i", path, "-af", "ebur128=framelog=quiet", "-f", "null", "-"], capture_output=True,
                         text=True).stderr
    for line in out.splitlines():
        if line.strip().startswith("I:"):
            return float(line.split()[1])
    raise RuntimeError("no loudness for " + path)


def write(path, x):
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-f", "f32le", "-ar", str(RATE), "-ac", "1", "-i", "-", path],
                   input=x.astype(np.float32).tobytes(), check=True)


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    src = sys.argv[1]
    lead = int(0.01 * RATE)
    mix = np.zeros(int(4.0 * RATE))
    for path, gain, delay in LAYERS:
        x = load(os.path.join(src, path))
        x = x / np.abs(x).max()
        x = x[max(0, onset(x) - lead):].copy()
        tail = len(x) // 4
        x[-tail:] *= 0.5 + 0.5 * np.cos(np.linspace(0.0, np.pi, tail))
        at = int(delay * RATE)
        n = min(len(x), len(mix) - at)
        mix[at:at + n] += gain * x[:n]
    mix /= np.abs(mix).max()

    # the tail's impulse: decaying noise, darkened, the first 25 ms left out (the dry sound is the start)
    rng = np.random.default_rng(1607)
    t = np.arange(int(TAIL * RATE)) / RATE
    ir = rng.standard_normal(len(t)) * 10 ** (-TAIL_DECAY * t / 20) * np.minimum(1.0, t / 0.025)
    tmp = os.path.join(OUT, "explosion.tmp")
    os.makedirs(OUT, exist_ok=True)
    dry, irw, shaped = tmp + ".dry.wav", tmp + ".ir.wav", tmp + ".shaped.wav"
    write(dry, mix)
    write(irw, ir / np.abs(ir).max())
    try:
        chain = (f"[1:a]lowpass=f={TAIL_DARK}:poles=2,lowpass=f={TAIL_DARK}:poles=2[ir];"
                 f"[0:a]asplit[d][w];[w][ir]afir=dry=10:wet=10:irgain=1[t];"
                 f"[d]apad=whole_dur={TAIL + 2.0}[dd];[dd][t]amix=inputs=2:weights='1 {TAIL_WET}':normalize=0:duration=longest,"
                 f"asetrate={RATE * PITCH:.0f},aresample={RATE},"
                 "highpass=f=40:poles=2,highpass=f=40:poles=2,"
                 f"lowshelf=g={LOW_SHELF}:f=140:w=0.7,equalizer=f=650:t=q:w=1.0:g=3,equalizer=f=2600:t=q:w=1.2:g=4,"
                 "acompressor=threshold=-18dB:ratio=2.5:attack=20:release=250:makeup=1dB,"
                 f"atrim=0:{LENGTH}")
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", dry, "-i", irw, "-filter_complex", chain, "-ac", "1", shaped], check=True)
        final = os.path.join(OUT, "explosion.ogg")
        gain = TARGET - loudness(shaped)
        fade = f"afade=t=out:st={LENGTH - 0.9}:d=0.9"
        for _ in range(5):
            subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", shaped, "-af",
                            f"volume={gain:.2f}dB,alimiter=limit=0.89:attack=4:release=180:level=false,{fade}", "-c:a", "libvorbis",
                            "-q:a", "6", final], check=True)
            missing = TARGET - loudness(final)
            if abs(missing) < 0.25 or gain > 30:
                break
            gain += missing
        print(f"{final}: {loudness(final):.1f} LUFS")
    finally:
        for f in (dry, irw, shaped):
            if os.path.exists(f):
                os.remove(f)


if __name__ == "__main__":
    main()
