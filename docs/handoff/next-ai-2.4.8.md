# HexPiece: handoff after 2.4.8 (the evil Conqueror sounds fixed)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.7.md covers 2.4.7 (the old HUD, Ryou's black hands and glow, the new
Conqueror charge/release animations from tools/conqueror_anims.py, the full release's old sounds removed), next-ai-2.4.6.md the
alignment and the evil sounds. Branch `claude/nice-bell-gam7qf`. **2.4.8 is released** (`.github/release-notes-2.4.8.md`); **the next
version is 2.4.9** (`python3 tools/next_version.py --write`). Never tested in game; the owner was asked to test.

## The owner (session 6, after testing 2.4.6, verbatim)
"Fair warning, I just tested the previous version, like the one that I technically sent you before all these instructions. The sound
of the evil conquerors is not even audible, bro. I can hear it, and it's just super glitchy. Fix it if you haven't yet."

## Found and fixed (2.4.8)
- Ruled out first: nothing stops or ducks HexPiece's sounds (the original's HakiClientState stops only its own events; the
  "supreme_conqueror" cinematic has no sound code; the roar plays once per release, the voice once per charge).
- The cause was my processing (2.4.6): bass g=11 at 95 Hz + asubboost (feedback sub) + lowshelf put the 20-60 Hz band 12 dB above the
  500-2000 Hz band (the owner's clip has every band within ~3 dB); the limiter (2 ms / 60 ms) clamped on that sub and pumped, the
  audible part sat at -23 dB. 2.4.7 (asked "louder and more bass boosted" before this test) made it 21 dB. Measured with a band
  analysis and spectrograms (scratch).
- `tools/evil_conqueror_sounds.py` now remakes all four: two 2-pole high-passes at 38 Hz, the boost as a low shelf near 150-200 Hz
  (male release +3 dB at 90 Hz too), a little presence (2.5-3 kHz), acompressor -20 dB 3:1 10/200 ms, gain into alimiter 5/150 ms at
  -1 dB, target -9.5 LUFS (reaches about -10.3). Result vs 2.4.6: the 500-2000 Hz band +4.3 dB (male release), +4.9 (male voice),
  +2.5 (female files); sub level with the mids; 60-150 Hz over the mids: male release +6.6, male voice +3.7, female release +2.3,
  female voice -1.1 (the clip -1.6); crest 9-10 dB. SOURCES.txt says so.
- Lesson: "bass boosted" for the owner's ears means the punchy low end speakers play (80-200 Hz), never sub under 60 Hz; measure the
  band balance against the source, not only the LUFS.

## Open
1. The owner's report on 2.4.8: the evil voice and roar (man and woman), and everything from 2.4.7 (the three animations, the black
   hands and their glow, the HUD, the release's sounds: should neutral keep the old ones, should the explosion come at once).
2. If the voice line and the roar overlapping (a release within ~3 s of reaching full) sounds messy: stop the voice at the release
   (a ClientboundStopSoundPacket to the players near; needs stubs and the verify allowlist).
3. From before: 2.4.5/2.4.6 items (lunge, ranges, release lag, alignment bar), good's release sound, killing humans -> evil, the
   evil/good thresholds, the Fishman Karate white screen, the stunned player's camera.
