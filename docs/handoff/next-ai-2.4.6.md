# HexPiece: handoff after 2.4.6 (good and evil: the alignment, the evil Conqueror's sounds)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.5.md covers 2.4.5 (the owner's 2.4.4 report: the first-person blade, the
M1 lunge, range by sword, the Conqueror's release lag and landings), next-ai-2.4.4.md that report in the owner's words. Branch
`claude/nice-bell-gam7qf`. **The next version is 2.4.7** (`python3 tools/next_version.py --write`). Never tested in game.

## The owner (session 5, verbatim, with input/EvilConqueror.mp3)
"New Good and Evil ALignment bar, killing (coming soon humans) will result in the player being evil, when good or evil the full
conqueror release gets a custom sound, for early testing, create the bar of good/evil in the mastery menu, and a command to set your
alignment, 100 is pure good, 0 is neutal, -100 pure evil. Evil gets the sound I sent, when it's full charge (holding G) the voice line
plays " SAWAGASHII" and when released the other sound plays. Bass boost it and increase the volume, make it more female if the player
is a woman, male is this but more bass boosted."

## Done in 2.4.6
- `Alignment` (pure): MIN -100 .. MAX 100, EVIL at or below -25, GOOD at or above 25 (my choice; the notes ask the owner), `label`
  (Pure Evil / Evil / Neutral / Good / Pure Good), `along` (the bar), `evilVoice/evilRelease(gender)` (female, else male), SOUNDS.
- Stored: `PieceProfile.alignment` (record's last component), `ProfileServer` root "Alignment", `setAlignment` (clamps, syncs);
  `S2CProfile` carries it in the full (own) profile (a byte), protocol 9; `PieceClient.self()` has it.
- `/hexpiece alignment` (shows) and `/hexpiece alignment <-100..100>` (sets) in `server/PieceCommands` (ops, as all /hexpiece).
- The bar: `ui/LogbookView.overview` under the Haki bars (the Captain's Log is the mastery menu: MenuSwap opens it for the original's
  mastery screens), `ui/PirateUi.alignment` (red half and gold half about a neutral mark, filled from the middle, a pointer). Judged
  with tools/preview/UiPreview (logbook-overview, -small, marine): fits at 640x360 and 427x240.
- Sounds: `tools/evil_conqueror_sounds.py` (ffmpeg with rubberband, libvorbis) cuts the owner's clip at its silence (3.39-3.58 s): the
  voice line (0-3.39: "Sa-wa-ga-shi-i", sibilants at ~0.1 and ~0.8 s, a man's voice ~140 Hz, then rumble) and the release (3.58-end,
  an unvoiced roar). Male: bass g=11 at 95 Hz, asubboost, lowshelf, compressor; female voice: up 7 semitones with formants up 17 %
  (asetrate+atempo then rubberband formant-preserved), bass g=6, a slight high shelf; female release: +2 semitones, formants kept,
  bass g=6. All brought to -12 LUFS (the clip is -23) and limited to -1 dB, mono 44.1 kHz Ogg. Measured: the speech band 9-12 dB
  louder than the clip, the low end +23 dB (male) / +17 dB (female); female voice ~189 Hz. `sounds/piece/conqueror/SOURCES.txt`.
  Events `piece.conqueror.evil_{voice,release}_{male,female}` (sounds.json, attenuation 48; subtitles), registered in PieceSounds.
- `server/AlignmentSounds`: each server tick (PlayerTickEvent END) the voice line when the original's `conquerorCharge` reaches 100
  while `conquerorCharging` (once a charge, VOICED); `ConquerorRelease.visual` (CONQUEROR_RELEASE) -> `released(player, power)`: the
  roar at power 1 (a full charge). Both only for an evil player, `PieceSounds.voice` (no pitch variation), volume 1.6 (~77 blocks).
  The original's supreme release sounds (snap, blast, rumble) have no voice, so they stay.
- Tests: MenuTests.alignment, SwordTests (the sounds defined, subtitled, mono Ogg, credited).

## Open
1. Ask the owner how 2.4.5 and 2.4.6 went in game: the lunge, the first-person blade, the ranges, the release's lag (FPS or the
   server?) and its landings; the alignment bar and the evil voice/roar (loud enough? female version right?); where evil and good
   should begin (now -25 / +25).
2. Good's own full-release sound (the owner will send it): add `Alignment.goodRelease`, the files, the events; neutral keeps the
   original's.
3. Killing humans -> evil, once humans exist (the owner: "coming soon"): ProfileServer.setAlignment(player, alignment - step) on a kill.
4. Older: the Fishman Karate white screen, the stunned player's own camera.
