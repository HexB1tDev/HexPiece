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

2.4.7 (the owner: "The volume of the EVIL conqueror release has to be louder and more bass boosted"): the release is shaped harder
than the voice line: a deeper bass and sub boost (male more than female), heavier compression, a tanh saturation (the boosted low end
grows harmonics that small speakers and headphones can play) and a louder finish (RELEASE_TARGET, limited at the same ceiling).

Needs ffmpeg with rubberband and libvorbis (the cloud sessions have it). Usage: python3 tools/evil_conqueror_sounds.py [--voice]   (--voice: the voice lines too)
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
# 2.4.7: the release, louder still (see the top)
RELEASE_TARGET = -9.5
RELEASE_LOUD = "acompressor=threshold=-30dB:ratio=6:attack=3:release=140:makeup=12dB"


def loudness(path):
    """The integrated loudness (LUFS) of an audio file, by ffmpeg's ebur128."""
    out = subprocess.run(["ffmpeg", "-hide_banner", "-i", path, "-af", "ebur128=framelog=quiet", "-f", "null", "-"], capture_output=True,
                         text=True).stderr
    for line in out.splitlines():
        if line.strip().startswith("I:"):
            return float(line.split()[1])
    raise RuntimeError("no loudness for " + path)


def band(path, cutoff=150):
    """The RMS level (dB) of a file below cutoff Hz: how much low end it has."""
    out = subprocess.run(["ffmpeg", "-hide_banner", "-i", path, "-af", f"lowpass=f={cutoff},lowpass=f={cutoff},astats=metadata=0", "-f",
                          "null", "-"], capture_output=True, text=True).stderr
    for line in out.splitlines():
        if "RMS level dB" in line:
            return float(line.split(":")[-1])
    raise RuntimeError("no level for " + path)


def run(part, chain, name, rate=44100, target=TARGET, drive=False):
    """The part shaped by chain, measured, brought to target and held under full scale by the limiter, written as Ogg Vorbis. drive
    (2.4.7, the release): the gain goes into a tanh saturation before the limiter, raised until the finished file reaches target."""
    start, end = part
    cut = ["-ss", str(start)] + (["-to", str(end)] if end is not None else [])
    shaped = os.path.join(OUT, name + ".tmp.wav")
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", SOURCE] + cut + ["-ac", "1", "-ar", str(rate), "-af", chain,
                    shaped], check=True)
    gain = target - loudness(shaped)
    final = os.path.join(OUT, name + ".ogg")
    for _ in range(6 if drive else 1):
        clip = "asoftclip=type=tanh:threshold=0.85," if drive else ""
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", shaped, "-af",
                        f"volume={gain:.2f}dB,{clip}alimiter=limit=0.89:attack=2:release=60:level=false,{FADE}", "-c:a", "libvorbis",
                        "-q:a", "6", final], check=True)
        missing = target - loudness(final)
        if abs(missing) < 0.3:
            break
        gain += missing
    os.remove(shaped)
    print(f"{final}: {loudness(final):.1f} LUFS, below 150 Hz {band(final):.1f} dB RMS")


def main():
    if not os.path.exists(SOURCE):
        sys.exit("the owner's clip is missing: " + SOURCE)
    os.makedirs(OUT, exist_ok=True)
    rate = 44100
    # male: "this but more bass boosted"
    male_bass = "bass=g=11:f=95:w=0.7,asubboost=dry=1:wet=0.6:decay=0.4:feedback=0.2:cutoff=90,lowshelf=g=4:f=180"
    female_bass = "bass=g=6:f=140:w=0.7"
    if "--voice" in sys.argv:  # the voice lines are as 2.4.6 made them; only remade when asked
        run(VOICE, f"{male_bass},{LOUD}", "evil_voice_male", rate)
        run(VOICE, f"{female_voice(rate)},{female_bass},highshelf=g=2:f=3500,{LOUD}", "evil_voice_female", rate)
    # 2.4.7: the release louder and with more bass than the voice line ("louder and more bass boosted")
    release_male_bass = ("bass=g=15:f=80:w=0.8,asubboost=dry=1:wet=1:decay=0.6:feedback=0.35:cutoff=100,lowshelf=g=6:f=200,"
                         "equalizer=f=55:t=q:w=1.2:g=4")
    release_female_bass = "bass=g=11:f=100:w=0.8,asubboost=dry=1:wet=0.7:decay=0.5:feedback=0.25:cutoff=110,lowshelf=g=4:f=220"
    run(RELEASE, f"{release_male_bass},{RELEASE_LOUD}", "evil_release_male", rate, RELEASE_TARGET, True)
    run(RELEASE, f"rubberband=pitch={2 ** (2 / 12):.5f}:formant=preserved,{release_female_bass},{RELEASE_LOUD}", "evil_release_female",
        rate, RELEASE_TARGET, True)


if __name__ == "__main__":
    main()
