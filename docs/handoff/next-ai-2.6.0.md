# HexPiece: handoff after 2.6.0 (the Conqueror's sounds: 35 % quieter, the evil voice line louder and voice-only, a CC0 explosion on letting go of G; the stun by hold: tap 5 s, hold 10-20 s, good 30 s)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.9.md (the Water Bullet crash, first person with an empty hand,
the arm under Haki, the first-person draw/sheathe DrawView + HolsterHand, the temper), next-ai-2.5.5.md, next-ai-2.5.3.md (Ryou's
fist aura: "beautiful and perfect": keep it), next-ai-2.5.1.md (**the owner's device: Android phone**, Amethyst, MobileGlues).

Branch `claude/nice-bell-gam7qf` (the newest branch: 2.5.9 was pushed there after the 2.4.7 handoff; `git fetch` it first).
**The next version is 2.6.1.** Nothing has ever been tested in game except by the owner.

## The owner (session 9, after 2.5.9, verbatim)
1. "Conqueror sounds reduced by 35% but the line evil voice line increased by a ton, its barely audible, after the key is let go,
   add a proper explosion sound (FROM THE WEB)"
2. "The Conqueror release stun increased tap G = 5 seconds stun, hold G (min) = 10 seconds, max hold G = 20 seconds, Max hold G good
   alignment = 30 seconds."
3. "After the previous tasks are done, push and then focus on bettering the first person Sheath / Unsheath animations so it's as
   smooth as possible without buggy frames."
4. (added) "also cut the voice line (evil) so that as soon as the voice ends, the sound ends, cause currently after the voice line
   theres a weird SFX when it's just meant to be the voice line"

## Done (2.6.0): sounds
- **35 % quieter**: the original plays its Haki sounds a second late (`HakiServerController.delayedSound`: `ServerTimeline.later(20)`,
  volume x 0.5, then `SoundBalance` ceilings: conqueror_charge 0.44, release 0.5, supreme_blast 0.55). PiecePatch redirects the
  `SoundBalance.pos` call in `lambda$delayedSound$26` to `server/ConquerorSounds.pos`: the five `conqueror_*` events (compared to
  `ModSounds.*.get()`, no new SRG reference) play at `SoundBalance.volume(...) * 0.65` (ceilings first, so it really is 35 %). The last
  stand's direct `SoundBalance.pos` calls are untouched. A volume over 1 is only reach (the client clamps the gain at 1): that is why
  the reduction is applied after the ceiling, and why the roar is lowered through its sounds.json "volume".
- **The evil roar** (evil_release_*): still sent at 1.6 (`AlignmentSounds.VOLUME`, server reach 25.6 blocks); its sounds.json
  "volume" is 0.40625 (gain 0.65) and attenuation_distance 77 (= 48 x 1.6: the same fade curve, 0.65 x). Files unchanged.
- **The evil voice line**: `tools/evil_conqueror_sounds.py --voice` (only the voices). VOICE is 0 - 1.20 s of input/EvilConqueror.mp3
  (formants above 1.5 kHz hit the noise floor at 1.16 s; after it the clip is a rumble to 2.78 s, then a sweep with a 3.5 kHz tone:
  the owner's "weird SFX"), 90 ms taper. VOICE_CHAIN: highpass 90 Hz, -3 dB at 350, +5 at 2.5 k, +3 at 5 k, compressor -26 dB 4:1,
  +4 dB into a tanh soft clip, -4 dB; target -6.8 LUFS (the limiter stops it at -8.6). The 300-4000 Hz band went from -13..-19 dB
  RMS to -10..-13 (the old mix: the voice band 5-8 dB under the whole, the rumble compressed up to the voice's level). Not lowered in
  game (played at 1.6 = full gain). Tested: LightningTests.conquerorMix reads the Ogg length (1.1-1.3 s).
- **The explosion** (`piece.conqueror.explosion`, `tools/conqueror_explosion.py SRC`): four CC0 recordings from the GitHub collection
  lavenderdotpet/CC0-Public-Domain-Sounds (commit f2b6264; opengameart.org, freesound, kenney.nl, archive.org and wikimedia are blocked
  in this environment: GitHub over git works, a blobless clone + `git checkout <commit> -- <paths>`). Layers: rubberduck's sci-fi
  explosion_01 (body), rubberduck's cannon_01 (crack), Warfork's gren_strong_explosion2 (punch), Kenney's lowFrequency_explosion_000
  (rumble); each faded over its last quarter (a cut layer clicked); a "Boom.wav" from Ben Burnes was dropped (a 525 Hz drum tone
  and a hard cut). Pitch 0.88, a 2.6 s convolution tail, low shelf -6 dB at 140 Hz (the phone speaker), +3 at 650, +4 at 2.6 k:
  60-160 Hz +5.6 dB over 500-2000 (the first mix had +15), crest 8.4 dB, -12 LUFS, 3.6 s, mono. Judged by spectrogram and band
  levels (no tones, no clicks, a smooth decay); nobody has listened to it.
  - Played at once (not a second late) in `ConquerorRelease.visual` for both visuals: `haki/ConquerorMix`: sent at 4 x its loudness
    (sounds.json volume 0.25, attenuation 64): full 1.0 (64 blocks), partial 0.5-0.85 by charge, tap 0.4; pitch 0.92 full to 1.06,
    tap 1.15 (PieceSounds.play varies it 5 %).
  - The original's supreme blast (35 % quieter) still follows a second later on a full release: the release notes ask the owner.

## Done (2.6.0): the stun (`haki/ConquerorStun`, `server/ConquerorRelease`)
- `ConquerorStun.TAP` 100; `held(alignment, power, heldAtFull)` = HOLD_MIN 200 + (most - 200) x progress, most = HOLD_MAX 400 (good:
  GOOD_MAX 600); progress = (charge x 160 + held-at-full (0..RAMP 100, at full charge only) - 10) / (160 + 100 - 10), clamped (160:
  the original's ticks to fill, the charge = ticks x 0.625; 10: the tap's limit). The shortest hold (11 ticks, charge 6) gives exactly
  200. The old EVIL_MAX 180 / NEUTRAL_MAX 200 / GOOD_MAX 300 / FIRST 0.3 / LIGHTNING 40 are gone.
- `Release` now exists for taps too (stun = TAP). `hurt` (the original's damage in applyConqueror, redirected) collects a tap's bodies
  (`r.tapped`); after the original's apply (its stunConquerorTap: 30 ticks) each is `hold`-ed for 100 ticks.
- A partial (held, not full) release: everything `taken` in its radius (`target`: the original's combat targets) and everything its
  lightning strikes is `hold`-ed for its stun (was: the lightning's 40 ticks; the original's push stun 60+tier x 8 for overwhelmed
  bodies only). A full release: forceMax with the stun (unchanged otherwise), an evil one's lightning beyond the radius too.
- `hold(level, body, ticks)`: slowness 10 / weakness 6 for exactly `ticks` (the old ones removed first) + `HakiFalls.knockDown`.

## Open
1. **Next: the first-person draw / sheathe (the owner's item 3)**: "as smooth as possible without buggy frames". `sword/DrawView`
   (pure poses), `client/sword/HolsterHand` (picks them by the anim), `tools/draw_preview.py` renders the game's frames.
2. The owner's report on 2.6.0 (the voice, the explosions, the old blast after, the stun times), 2.5.9 and before (next-ai-2.5.9.md).

## Tools
- `tools/conqueror_explosion.py SRC` (2.6.0), `tools/evil_conqueror_sounds.py [--voice]`, `tools/draw_preview.py`,
  `tools/katana_temper.py`, `tools/holster_preview.py`, `tools/ryou_preview.py`, `tools/srg_shapes.py <joined.tsrg>`.
- The original's code: CFR (`org.benf:cfr:0.152` from Maven Central) over input/hexhaki-2.1.0.jar into the scratchpad.
