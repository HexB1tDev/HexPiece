#!/usr/bin/env python3
"""3.3.6: the pitch check every new sound must pass (docs/handoff 8zy and 8zz; the owner will not have melodic sounds: "It's too
melodic, bro"), kept as a tool this time (the first one was lost with its scratchpad).

The clip's long-term spectrum (8192-point FFTs, Hann windowed, half overlapping, their power averaged), 40 Hz to 12 kHz; each bin's
level in dB over the median of its 121 neighbouring bins; the highest is the clip's most prominent line. Plain noise of the same length
reads high too, by chance alone (about +10 dB at 0.25 s, +7 at 0.45 s, +4.5 at 1 s, +3.5 long), so a clip is judged by its excess over
noise of its own length (the mean of five noises): within about 4 dB is broadband; a whine, a hum or a ringing blade stands 8 to 30 dB
over it.

Usage: python3 tools/tonal_lines.py clip.ogg [start_s length_s] ...   (needs ffmpeg and numpy)
"""
import subprocess
import sys

import numpy as np

RATE = 44100
N = 8192


def samples(path, start=0.0, length=None):
    cut = ["-ss", f"{start:.3f}"] + (["-t", f"{length:.3f}"] if length else [])
    raw = subprocess.run(["ffmpeg", "-v", "error", "-hide_banner"] + cut + ["-i", path, "-ac", "1", "-ar", str(RATE), "-f", "f32le", "-"],
                         capture_output=True, check=True).stdout
    return np.frombuffer(raw[:len(raw) // 4 * 4], dtype=np.float32).astype(np.float64)


def spectrum(x):
    """The long-term spectrum in dB and its bins' frequencies."""
    if len(x) < N:
        x = np.concatenate([x, np.zeros(N - len(x))])
    window = np.hanning(N)
    power = np.zeros(N // 2 + 1)
    frames = 0
    for at in range(0, len(x) - N + 1, N // 2):
        power += np.abs(np.fft.rfft(x[at:at + N] * window)) ** 2
        frames += 1
    power /= max(1, frames)
    return 10.0 * np.log10(power + 1e-20), np.fft.rfftfreq(N, 1.0 / RATE)


def narrow(x):
    """(the sharpest peak's height in dB over the bins 4 to 10 either side of it, its frequency): a tone is a few bins wide and stands
    well over its near neighbours; a bass slope or a broad hump does not (the 121-bin line reads both alike at the band's low edge)."""
    db, freqs = spectrum(x)
    lo = np.searchsorted(freqs, 40.0)
    hi = np.searchsorted(freqs, 12000.0)
    best = (-1e9, 0.0)
    for i in range(max(lo, 10), hi):
        near = np.concatenate([db[i - 10:i - 3], db[i + 4:i + 11]])
        excess = db[i] - np.median(near)
        if excess > best[0]:
            best = (excess, freqs[i])
    return best


def line(x):
    """(the most prominent line's excess in dB over its neighbours, its frequency in Hz)."""
    if len(x) < N:
        x = np.concatenate([x, np.zeros(N - len(x))])
    window = np.hanning(N)
    power = np.zeros(N // 2 + 1)
    frames = 0
    for at in range(0, len(x) - N + 1, N // 2):
        power += np.abs(np.fft.rfft(x[at:at + N] * window)) ** 2
        frames += 1
    power /= max(1, frames)
    freqs = np.fft.rfftfreq(N, 1.0 / RATE)
    db = 10.0 * np.log10(power + 1e-20)
    lo = np.searchsorted(freqs, 40.0)
    hi = np.searchsorted(freqs, 12000.0)
    best = (-1e9, 0.0)
    for i in range(lo, hi):
        a = max(0, i - 60)
        b = min(len(db), i + 61)
        excess = db[i] - np.median(db[a:b])
        if excess > best[0]:
            best = (excess, freqs[i])
    return best


def noise(length_s):
    """The line plain noise of this length reads (the mean of five)."""
    rng = np.random.default_rng(7)
    n = max(N, int(length_s * RATE))
    return float(np.mean([line(rng.standard_normal(n))[0] for _ in range(5)]))


def judge(path, start=0.0, length=None):
    x = samples(path, start, length)
    excess, at = line(x)
    base = noise(len(x) / RATE)
    return excess, at, base, len(x) / RATE


def noise_narrow(length_s):
    rng = np.random.default_rng(11)
    n = max(N, int(length_s * RATE))
    return float(np.mean([narrow(rng.standard_normal(n))[0] for _ in range(5)]))


def report(path, start=0.0, length=None):
    """One line: the 121-bin line and the narrow peak, each against noise of the clip's length."""
    x = samples(path, start, length)
    seconds = len(x) / RATE
    e, at = line(x)
    n, nat = narrow(x)
    return (f"[{start:.2f} +{seconds:.2f}s] line +{e:.1f} at {at:.0f} Hz ({e - noise(seconds):+.1f} over noise); narrow +{n:.1f} at "
            f"{nat:.0f} Hz ({n - noise_narrow(seconds):+.1f} over noise)")


def main():
    args = sys.argv[1:]
    i = 0
    while i < len(args):
        path = args[i]
        start = 0.0
        length = None
        if i + 2 < len(args) + 1 and i + 2 <= len(args) - 1 and _num(args[i + 1]) and _num(args[i + 2]):
            start = float(args[i + 1])
            length = float(args[i + 2])
            i += 3
        else:
            i += 1
        print(path, report(path, start, length))


def _num(s):
    try:
        float(s)
        return True
    except ValueError:
        return False


if __name__ == "__main__":
    main()
