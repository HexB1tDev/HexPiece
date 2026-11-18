#!/usr/bin/env python3
"""2.4.6: the evil Conqueror's sounds, made from the owner's own clip (input/EvilConqueror.mp3, kept unchanged).

The owner: "Evil gets the sound I sent, when it's full charge (holding G) the voice line plays " SAWAGASHII" and when released the other
sound plays. Bass boost it and increase the volume, make it more female if the player is a woman, male is this but more bass boosted."

The clip is two parts with a silence between them (3.39-3.58 s): the voice line ("Sa-wa-ga-shi-i", its sibilants at about 0.1 s and
0.8 s, voiced at a man's pitch, 100-140 Hz, then a rumble to 3.39 s) and the release (3.58 s to the end, a broadband roar with falling
sweeps, unvoiced). Each is written twice (mono Ogg Vorbis, 44.1 kHz, for positional playback):
  - male: the clip's own voice, the low end boosted hard (bass and sub), compressed and brought up to near full scale;
  - female: the voice raised 7 semitones with its formants raised 17 % (a woman's voice, not a sped-up man's), a lighter bass boost,
    the same loudness. The release (no voice in it) is raised 2 semitones with its formants kept, a lighter bass boost.

Needs ffmpeg with rubberband and libvorbis (the cloud sessions have it). Usage: python3 tools/evil_conqueror_sounds.py
"""
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = os.path.join(ROOT, "input", "EvilConqueror.mp3")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "sounds", "piece", "conqueror")

# the two parts (seconds), cut at the silence between them
VOICE = (0.0, 3.39)
RELEASE = (3.58, None)

# loud: compressed (then brought to TARGET and limited, in run)
LOUD = "acompressor=threshold=-24dB:ratio=4:attack=4:release=160:makeup=8dB"
FADE = "afade=t=in:d=0.01,areverse,afade=t=in:d=0.25,areverse"

# a woman's voice from a man's: up 15 % with the formants (sample rate up, tempo back), then up the rest of the way with the formants
# kept (rubberband), 5.8 semitones in all
FORMANT = 1.17
PITCH = 2 ** (7.0 / 12)


def female_voice(rate):
    return (f"asetrate={rate * FORMANT:.0f},aresample={rate},atempo={1 / FORMANT:.5f},"
            f"rubberband=pitch={PITCH / FORMANT:.5f}:formant=preserved:pitchq=quality")


# how loud the finished sounds are (integrated loudness, LUFS; the clip itself is about -23): game sound effects sit near -12 to -14,
# and Minecraft cannot play a file louder than it is (a volume over 1 only carries it further)
TARGET = -12.0


def loudness(path):
    """The integrated loudness (LUFS) of an audio file, by ffmpeg's ebur128."""
    out = subprocess.run(["ffmpeg", "-hide_banner", "-i", path, "-af", "ebur128=framelog=quiet", "-f", "null", "-"], capture_output=True,
                         text=True).stderr
    for line in out.splitlines():
        if line.strip().startswith("I:"):
            return float(line.split()[1])
    raise RuntimeError("no loudness for " + path)


def run(part, chain, name, rate=44100):
    """The part shaped by chain, measured, brought to TARGET and held under full scale by the limiter, written as Ogg Vorbis."""
    start, end = part
    cut = ["-ss", str(start)] + (["-to", str(end)] if end is not None else [])
    shaped = os.path.join(OUT, name + ".tmp.wav")
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", SOURCE] + cut + ["-ac", "1", "-ar", str(rate), "-af", chain,
                    shaped], check=True)
    gain = TARGET - loudness(shaped)
    final = os.path.join(OUT, name + ".ogg")
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", shaped, "-af",
                    f"volume={gain:.2f}dB,alimiter=limit=0.89:attack=2:release=60:level=false,{FADE}", "-c:a", "libvorbis", "-q:a", "6",
                    final], check=True)
    os.remove(shaped)
    print(f"{final}: {loudness(final):.1f} LUFS")


def main():
    if not os.path.exists(SOURCE):
        sys.exit("the owner's clip is missing: " + SOURCE)
    os.makedirs(OUT, exist_ok=True)
    rate = 44100
    # male: "this but more bass boosted"
    male_bass = "bass=g=11:f=95:w=0.7,asubboost=dry=1:wet=0.6:decay=0.4:feedback=0.2:cutoff=90,lowshelf=g=4:f=180"
    female_bass = "bass=g=6:f=140:w=0.7"
    run(VOICE, f"{male_bass},{LOUD}", "evil_voice_male", rate)
    run(VOICE, f"{female_voice(rate)},{female_bass},highshelf=g=2:f=3500,{LOUD}", "evil_voice_female", rate)
    run(RELEASE, f"{male_bass},{LOUD}", "evil_release_male", rate)
    run(RELEASE, f"rubberband=pitch={2 ** (2 / 12):.5f}:formant=preserved,{female_bass},{LOUD}", "evil_release_female", rate)


if __name__ == "__main__":
    main()
