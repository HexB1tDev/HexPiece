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

2.6.0 (the owner, testing 2.5.9: "the evil voice line increased by a ton, its barely audible" and "cut the voice line (evil) so that
as soon as the voice ends, the sound ends, cause currently after the voice line theres a weird SFX when it's just meant to be the voice
line"): the voice line is only the voice now, "Sa-wa-ga-shi-i" to the end of its last vowel (the formants above 1.5 kHz are at the
noise floor from 1.16 s: VOICE ends at 1.20 s, the last 90 ms tapered); the rumble after it and the sweep at 2.78 s are gone. It was
mixed for big speakers (its 300-4000 Hz band 5-8 dB under the whole, the rumble under it carried as loud as the voice, inaudible on a
phone's speaker, the owner's device): now speech first (VOICE_CHAIN: the low end under 90 Hz cut, the mud near 350 Hz lowered, the
presence near 2.5 kHz and the "s"/"sh" near 5 kHz raised, a firm compressor, a soft clip that adds edge without crackle) and brought
to VOICE_TARGET, 3.5 LU over 2.4.8's; in game it is the only Conqueror sound not lowered by 2.6.0's 35 % (server/ConquerorSounds).

Needs ffmpeg with rubberband and libvorbis (the cloud sessions have it). Usage: python3 tools/evil_conqueror_sounds.py [--voice] (--voice: the voice lines only)
"""
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = os.path.join(ROOT, "input", "EvilConqueror.mp3")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "sounds", "piece", "conqueror")

# the two parts (seconds): the voice (2.6.0: only the voice, to the end of its last vowel) and the release after the silence
VOICE = (0.0, 1.20)
VOICE_FADE = "afade=t=in:d=0.01,areverse,afade=t=in:d=0.09,areverse"
# 2.6.0: the voice for any speaker (a phone's too): speech first
VOICE_CHAIN = ("highpass=f=90:poles=2,equalizer=f=350:t=q:w=1.0:g=-3,equalizer=f=2500:t=q:w=1.0:g=5,equalizer=f=5000:t=q:w=1.2:g=3,"
               "acompressor=threshold=-26dB:ratio=4:attack=4:release=90:makeup=4dB,volume=4dB,asoftclip=type=tanh,volume=-4dB")
VOICE_TARGET = -6.8
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


def run(part, chain, name, rate=44100, target=TARGET, fade=FADE):
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
                        f"volume={gain:.2f}dB,alimiter=limit=0.89:attack=5:release=150:level=false,{fade}", "-c:a", "libvorbis",
                        "-q:a", "6", final], check=True)
        missing = target - loudness(final)
        if abs(missing) < 0.25 or gain > 30:
            break
        gain += missing
    os.remove(shaped)
    print(f"{final}: {loudness(final):.1f} LUFS, below 150 Hz {band(final):.1f} dB RMS")


def main():
    voice_only = "--voice" in sys.argv[1:]
    if not os.path.exists(SOURCE):
        sys.exit("the owner's clip is missing: " + SOURCE)
    os.makedirs(OUT, exist_ok=True)
    rate = 44100
    # male: "this but more bass boosted" (the release the most); female: lighter
    run(VOICE, f"lowshelf=g=2:f=160:w=0.6,{VOICE_CHAIN}", "evil_voice_male", rate, VOICE_TARGET, VOICE_FADE)
    run(VOICE, f"{female_voice(rate)},highpass=f=130:poles=2,{VOICE_CHAIN}", "evil_voice_female", rate, VOICE_TARGET, VOICE_FADE)
    if voice_only:
        return
    run(RELEASE, "lowshelf=g=9:f=160:w=0.6,equalizer=f=90:t=q:w=1:g=3,equalizer=f=2800:t=q:w=1.2:g=2", "evil_release_male", rate)
    run(RELEASE, f"rubberband=pitch={2 ** (2 / 12):.5f}:formant=preserved,lowshelf=g=5:f=180:w=0.6,equalizer=f=3000:t=q:w=1.2:g=2",
        "evil_release_female", rate)


if __name__ == "__main__":
    main()
