# HexPiece: handoff after 2.6.2 (2.6.2: the katanas and Gryphon 1.25 times bigger everywhere, the sword animations re-solved; 2.6.1: the first-person draw and sheathing without flips, the right-click charge in first person, the Haki coat on the blade only and spreading, the Ryou sound quieter; 2.6.0: the Conqueror's sounds and stun)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.9.md (the Water Bullet crash, first person with an empty hand,
the arm under Haki, the first-person draw/sheathe DrawView + HolsterHand, the temper), next-ai-2.5.5.md, next-ai-2.5.3.md (Ryou's
fist aura: "beautiful and perfect": keep it), next-ai-2.5.1.md (**the owner's device: Android phone**, Amethyst, MobileGlues).

Branch `claude/nice-bell-gam7qf` (the newest branch: 2.5.9 was pushed there after the 2.4.7 handoff; `git fetch` it first).
**2.6.3 is released (its runs not checked: stopped at the usage limit). The next version is 2.6.4.** 2.6.3: HolsterHand no longer turns itself off for good on one failure (with Haki on, the original's arm hook threw: the game's item, the saya in the hand, showed from then on): a failure costs its frame, three in a row a second's rest, the arm event guarded; KatanaItemRenderer never draws the saya in first person; the glint dimmed by the coat (it washed out the black tip). 2.6.0 and 2.6.1 are released (their runs were green). build.py's VERSION
is still 2.6.1; 2.6.2's release notes wait in docs/handoff/release-notes-2.6.2-draft.md (NOT in .github, so nothing was released).

## Let through in 2.6.2 (fix next): build.py check_sword_poses skips three misses of the bigger blades: sword_draw (the saya's tip
0.06 block into the floor at tick 5.5) and sword_sheathe (tick 20.5), sword_tsuki (the handle 0.8 px into the right forearm at 5.5).
Fix in tools/sword_anims.py (the saya's tilt in those beats, the tsuki's wrist), re-solve, remove the skip.

## (done) to finish 2.6.2 (the bigger katanas)
1. The Java and rig changes below are committed but NOT built or tested. The animation re-solve (`python3 tools/sword_anims.py`,
   about 5 minutes) was interrupted: some sword_*.json are re-solved for the bigger blade, the rest (and BladePaths.java) are not.
   Run the whole solve again, then `python3 build.py` (check_sword_poses: the bigger blade clear of the body; SwordTests: drawView,
   drawSmooth, holster with Katana.SIZE). Fix what fails.
2. Render and judge: tools/draw_preview.py (first person at the size), tools/sword_view/sword_gif (third person: the draw and
   sheathing, the blade staying in the hand and the saya). If 1.25 looks wrong, change Katana.SIZE and sword_rig.SIZE together.
3. `python3 tools/next_version.py --write` (2.6.2), move the draft back to .github/release-notes-2.6.2.md, commit, push, check the
   "Build HexPiece" and "Release HexPiece" runs. Nothing has ever been tested in game except by the owner.

## The owner (session 9, after 2.5.9, verbatim)
1. "Conqueror sounds reduced by 35% but the line evil voice line increased by a ton, its barely audible, after the key is let go,
   add a proper explosion sound (FROM THE WEB)"
2. "The Conqueror release stun increased tap G = 5 seconds stun, hold G (min) = 10 seconds, max hold G = 20 seconds, Max hold G good
   alignment = 30 seconds."
3. "After the previous tasks are done, push and then focus on bettering the first person Sheath / Unsheath animations so it's as
   smooth as possible without buggy frames."
4. (added) "also cut the voice line (evil) so that as soon as the voice ends, the sound ends, cause currently after the voice line
   theres a weird SFX when it's just meant to be the voice line"
5. (after 2.6.0, on a render of the old draw, t22-t23.5) "see what I mean? ... I mean wtf is this sequence"
6. "Haki coating is ONLY on the blade of the swords, never on the handle or saya"
7. "also the haki coating (ryou sound) is too high, too loud." and "the swords man right click has no first person animation"
8. "please make sure the haki coating on the blade is also progressive not instant (like the arms slowly turning black)."
9. (after 2.6.1) "and increase the overall size of the katanas, and gryphon I find them too small. even on the hand they're small"

## Done (2.6.2): the katanas and Gryphon bigger
- `Katana.SIZE` 1.25, `Katana.size()` (Yoru 1.0: not asked, 2.75 blocks already). One factor everywhere a sword's model px become
  blocks: `KatanaRenderer.px(katana)` (the draw's scale, the grip offsets in SwordLayer.hand, the saya slide, Yoru's lift and glide,
  KatanaItemRenderer, SeaFighterRenderer, SwordFx's trail, HakiDraw's bare blade), DrawView (the grip `KatanaGrip.GRIP * sz` in
  byIntent/gripped/lean/settled/trembled, the slide `r * size`, the saya pull and the left fist at the mouth `* Katana.SIZE`) and
  HolsterView.fist (`REST`, `OVER` `* Katana.SIZE`).
- The third-person animations depend on the blade's length (the hand's pull out of the saya, the point clearing the mouth on the
  draw tick, the clearance from the body): `tools/sword_rig.py` `SIZE` 1.25 and `scale(key)` (grip, hip `HIP_SCALE`, back for Yoru,
  saya_to_hold, sword_anims' glide), and `python3 tools/sword_anims.py` re-solved every sword animation, BladePaths.java (the cuts'
  paths, so the hit sweep follows the longer blade) and SayaSlide.java (unchanged: mesh px). `Katana.reach()` counts its own length
  against the paths' at their sizes (`tip * size - PATH_TIP * SIZE`): Yoru's total reach unchanged, the others about 0.3 block more
  (the path).
- tools/draw_preview.py and tools/holster_preview.py draw the meshes at the size.

## Done (2.6.1): the first-person draw and sheathing (sword/DrawView)
- The causes, measured (a scratch analyser: the blade's way in the view per frame, its share across the view, the turn per tick of
  the blade and of the right arm): (a) the draw's post-clear path went exit -> OUT -> CROSS (pointing into the screen: a stub) ->
  WHIP (pointing right) -> HELD: slerping CROSS->WHIP swung the point over the top, then back: view angle 188 -> 158 -> 64 -> 11 -> 21
  -> 110 degrees; (b) the sheathing flicked right then swung back up over to the left (stubs, the fist 339 degrees/tick at t0.5);
  (c) `gripped()` took the arm's roll from the blade's way across the arm, a tiny vector when the blade nearly continues the forearm
  (held: 23 degrees apart, 8 on the way in): the fist spun up to 192 degrees/tick.
- Now: the draw after the point clears is `curve()` (a quadratic Bezier, de Casteljau by slerp: no corner) exit -> SPRING -> held with
  "outquart": the view angle falls 188 -> 110 monotonically, at least 0.92 across the view, the turn rate falling evenly (39, 30, 25,
  21 ... degrees/tick). The exit uses the saya at CLEAR_AT (a fixed start). The edge turns edge-on for about a tick in the fastest
  part (a glint). The sheathing: held -> FLICK (point at 186 degrees, edge UP, flat to the eye) with "outquad" over FLICK_AT 4, held
  (settled: 2.5 degrees lower) to FLICK_HOLD 7, carried to the laid frame by LAY_AT 10 (under 3 degrees of wobble), then as before.
  `gripped()` adds 0.6 x the view's up (across the arm) to the blade's way: no flips. The empty saya sinks SAYA_SINK 0.55 over
  SINK_TICKS 5 after the draw (was: vanished at FREE+4) and rises back before the sheathing.
- SwordTests.drawSmooth (every blade, quarter ticks): never under half across the view; at most 26 degrees a quarter tick for the
  blade and the right fist; the draw's point rises one way only after the clear; the sheathing's snap one way only; the charge too.

## Done (2.6.1): the right-click charge in first person
- Right click = the Haki slash charge (SwordAnims.CHARGE from the held blade, CHARGE_DRAW from the saya; RELEASE is a swing, still
  playerAnimator's in first person). HolsterHand drew the static held/holster pose for both. Now `DrawView.charge` (held -> CHARGE_GRIP
  {0.62, 0, -0.86}, the blade back over the right shoulder {-0.24, 0.76, 0.60}, in CHARGE_RAISE 5 ticks), `chargeDraw` (the hands to
  the handle and mouth, the blade slid out by QUICK_CLEAR 2.2, swept up by QUICK_UP 6.5 with "outquad", the saya sinking) and
  `backCharge` (Yoru: from over the shoulder). Then `trembled`: two smooth sines about the grip, 0.35 -> 1.6 degrees over 40 ticks.
  A legendary blade's CHARGE_DRAW is the drawn charge (as SwordAnims.name maps it). tools/draw_preview.py writes charge.png and
  charge_draw.png.

## Done (2.6.1): the Haki coat on swords
- Until 2.6.0 Armament's black buffers (the original's `ArmamentItemBuffers.black`: a vertex colour override) took the whole sword
  mesh: the handle and guard went black (the saya was kept out in 2.2.6). Now `KatanaRenderer.draw(..., coat)` paints only the mesh's
  `01_Blade...` quads (every mesh, every LOD has it first), each vertex at `255 x (1 - BladeCoat.at(y / tip, vertex, coat))`; the
  rest in its own colours; the highlight now white over the black (it was black through the wrapper). No black buffers for swords
  anywhere (SwordLayer, HolsterHand, KatanaItemRenderer ignore the original's).
- `sword/BladeCoat` (pure, tested): ON_NANOS 1.1 s after DELAY_NANOS 0.35 s (the fist first), OFF_NANOS 0.6 s, SPREAD 0.18, reveal =
  0.02 + along x 0.70 + jitter x 0.10 (guard first, ragged). `client/sword/BladeCoatClient`: per-player fades (by entity id, cleared
  per level), on = SwordLayer.blackened (Armament). Judged on a render of the held blade at 8 moments (scratch).

## Done (2.6.1): the Ryou sound
- `RyouServer.afterArmament` played the original's acoc_on (19 s, -3 to -8 dB RMS for most of it) at 0.45 straight to everyone
  near; now ACOC_ON_VOLUME 0.16. ARMAMENT_ON (via delayedSound, 0.45) unchanged.

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
1. The owner's report on 2.6.1 (the first-person draw, sheathing and charge; the coat on the blade; the Ryou sound) and on 2.6.0
   (the voice, the explosions, the old blast after, the stun times), 2.5.9 and before (next-ai-2.5.9.md).
2. The release (RELEASE, a swing) in first person is playerAnimator's THIRD_PERSON_MODEL view; the charge's first-person pose was
   placed to read as "over the shoulder", not fitted to where the model's arm starts the release: if the owner sees a jump at the
   release, fit CHARGE_GRIP/CHARGE_BLADE to the release's first frame.

## Tools
- `tools/conqueror_explosion.py SRC` (2.6.0), `tools/evil_conqueror_sounds.py [--voice]`, `tools/draw_preview.py`,
  `tools/katana_temper.py`, `tools/holster_preview.py`, `tools/ryou_preview.py`, `tools/srg_shapes.py <joined.tsrg>`.
- The original's code: CFR (`org.benf:cfr:0.152` from Maven Central) over input/hexhaki-2.1.0.jar into the scratchpad.
