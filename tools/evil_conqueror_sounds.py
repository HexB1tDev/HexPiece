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

2.4.8 (the owner, testing 2.4.6: "The sound of the evil conquerors is not even audible, bro. I can hear it, and it's just super
glitchy."): the 2.4.6 boost (bass g=11 at 95 Hz plus asubboost's feedback sub) put the 20-60 Hz band 12 dB over the mids (the clip
has them level), and 2.4.7's harder one 21 dB over: energy no laptop speaker or most headphones play, on which the limiter clamped,
pushing the audible voice and roar down and pumping them. Now every file: the sub under 38 Hz cut (two 2-pole high-passes), the bass
boost in the range speakers play (a low shelf near 150 Hz, the male release also +3 dB at 90 Hz), a little presence near 2.5-3 kHz,
a gentle compressor (-20 dB, 3:1, 10 ms / 200 ms) and a slow limiter (5 ms / 150 ms), no sub boost, no saturation. Measured against
the 500-2000 Hz band: the male release's 60-150 Hz about +7 dB and its 20-60 Hz level with the mids; the female one's 60-150 Hz about
+3; the voices' about +4 (male) and 0 (female); crest factors 8-10 dB (not crushed); -10 LUFS or a little under, the limiter at -1 dB.

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

# 2.4.8: the sub no speaker plays, cut (it only took the limiter's headroom); a gentle compressor
SUB = "highpass=f=38:poles=2,highpass=f=38:poles=2"
COMPRESS = "acompressor=threshold=-20dB:ratio=3:attack=10:release=200:makeup=2dB"
FADE = "afade=t=in:d=0.01,areverse,afade=t=in:d=0.25,areverse"

# a woman's voice from a man's: up 15 % with the formants (sample rate up, tempo back), then up the rest of the way with the formants
# kept (rubberband), 5.8 semitones in all
FORMANT = 1.17
PITCH = 2 ** (7.0 / 12)


def female_voice(rate):
    return (f"asetrate={rate * FORMANT:.0f},aresample={rate},atempo={1 / FORMANT:.5f},"
            f"rubberband=pitch={PITCH / FORMANT:.5f}:formant=preserved:pitchq=quality")


# how loud the finished sounds are (integrated loudness, LUFS; the clip itself is about -23): Minecraft cannot play a file louder than
# it is (a volume over 1 only carries it further). With the energy where speakers play it, -10 is much louder to the ear than 2.4.6's
# -12.7 was (most of that was sub).
TARGET = -9.5


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


def run(part, chain, name, rate=44100, target=TARGET):
    """The part shaped by chain, measured, raised to target into a slow limiter (raised again until the file reaches it, or as near
    as the limiter allows), written as Ogg Vorbis."""
    start, end = part
    cut = ["-ss", str(start)] + (["-to", str(end)] if end is not None else [])
    shaped = os.path.join(OUT, name + ".tmp.wav")
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", SOURCE] + cut + ["-ac", "1", "-ar", str(rate), "-af",
                    f"{SUB},{chain},{COMPRESS}", shaped], check=True)
    gain = target - loudness(shaped)
    final = os.path.join(OUT, name + ".ogg")
    for _ in range(5):
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", shaped, "-af",
                        f"volume={gain:.2f}dB,alimiter=limit=0.89:attack=5:release=150:level=false,{FADE}", "-c:a", "libvorbis",
                        "-q:a", "6", final], check=True)
        missing = target - loudness(final)
        if abs(missing) < 0.25 or gain > 30:
            break
        gain += missing
    os.remove(shaped)
    print(f"{final}: {loudness(final):.1f} LUFS, below 150 Hz {band(final):.1f} dB RMS")


def main():
    if not os.path.exists(SOURCE):
        sys.exit("the owner's clip is missing: " + SOURCE)
    os.makedirs(OUT, exist_ok=True)
    rate = 44100
    # male: "this but more bass boosted" (the release the most); female: lighter
    run(VOICE, "lowshelf=g=8:f=150:w=0.6,equalizer=f=2500:t=q:w=1.2:g=2", "evil_voice_male", rate)
    run(VOICE, f"{female_voice(rate)},lowshelf=g=6:f=200:w=0.6,highshelf=g=2:f=3500", "evil_voice_female", rate)
    run(RELEASE, "lowshelf=g=9:f=160:w=0.6,equalizer=f=90:t=q:w=1:g=3,equalizer=f=2800:t=q:w=1.2:g=2", "evil_release_male", rate)
    run(RELEASE, f"rubberband=pitch={2 ** (2 / 12):.5f}:formant=preserved,lowshelf=g=5:f=180:w=0.6,equalizer=f=3000:t=q:w=1.2:g=2",
        "evil_release_female", rate)


if __name__ == "__main__":
    main()
