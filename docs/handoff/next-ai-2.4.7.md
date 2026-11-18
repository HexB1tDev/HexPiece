# HexPiece: handoff after 2.4.7 (the owner's 2.4.6 report: the old HUD, Ryou's hands, the Conqueror's animations; the release sounds)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.6.md covers 2.4.6 (alignment, the evil Conqueror's sounds), next-ai-2.4.5.md
2.4.5 (the owner's 2.4.4 report: first-person blade, M1 lunge, range by sword, the release's lag and landings). Branch
`claude/nice-bell-gam7qf`. **2.4.7 is released** (`.github/release-notes-2.4.7.md`); **the next version is 2.4.8**
(`python3 tools/next_version.py --write`). Never tested in game; the owner was asked to test.

## The owner's report after 2.4.6 (verbatim, with two screenshots)
1. "I dont want the Ryou haki to be a painted outline, just turn the player's hands black the skin itself like the Nika fruit changes
   the skin (slowly pixel by pixel, exactly how the skin turns white), and the glow around the outline is not meant to be literal
   painted lines, it's meant to be kinda like the minecraft glow effect around the firsts, thats it." (screenshot: black hands with
   cyan bands along every edge of the hand box and a cyan star disc)
2. "Why the fuck is the mastery hud menu back?" (screenshot: the original's bottom-left panel: HEXHAKI 2000/2000, ARM/OBS/HAO 1000,
   PINNACLE, under HexPiece's own abilities panel)
3. "I want a badass animation for the Conqueror Haki charge / release. the current one is dog shit."

## Done in session 5 (released in 2.4.7, not tested in game)
2. **The old HUD**: it is the original's fourth HUD, `com.hexhaki.patch.HudSkin.render` (RenderGuiEvent.Post, registered for
   Dist.CLIENT), drawn whenever the original's "board" setting (`HakiClientState.boardEnabled`) is on; the patch only gated HakiHud,
   FruitHud and GearHud. Now `PiecePatch.HUDS_REPLACED` holds it too (`AbilityHud.replaces()` first), build.py `PATCHED` lists it, and
   `PieceVerify` counts a class registered only for Dist.CLIENT as client code wherever its package is (was: only `/client/`).
1. **Ryou hands**:
   - Skin: `haki/HandCoat.reveal/at` (as the original's `GearJacketPattern`: each hand pixel its own moment: the fist's end first, then
     up from the knuckles to the wrist, jittered by a per-pixel hash; SPREAD 0.18). `client/haki/HakiSkin`: per-player `Fade` (on
     ON_NANOS 1 s, off OFF_NANOS 0.6 s), the skin's pixels kept (`base`), the hand recomposed in 48 steps as it spreads and draws back,
     the copy dropped once clear. RyouTests checks the spread. Judged with a strip render (scratch only).
   - Glow: `RyouHands.glow` no longer paints bands or halo discs; it hands the fist's box (WRIST..FIST, COAT outside the hand, bent
     at the elbow) to the new `client/haki/RyouGlow`, which records each box face in normalised device coordinates (the current
     RenderSystem projection x model-view x pose), then draws the boxes into its own `shaders/post/entity_outline.json` PostChain's
     "final" target (the world's depth copied in for third person), runs the chain and blends it over the frame as vanilla
     `LevelRenderer.doEntityOutline` does. World boxes at RenderLevelStageEvent AFTER_PARTICLES; first-person hand boxes (recorded after
     it) at the first RenderGuiOverlayEvent.Pre; AFTER_SKY resets the frame. On any failure it stops (`broken`) and the hands stay
     black. New references proven in verify/api-allowlist.txt ("2.4.7" block); stubs added. NOT seen in game: if the glow is missing,
     wrong or covers the hand, check RenderSystem's matrices at record time, the AFTER_PARTICLES binding, and blitToScreen's state.

## Done in session 6 (released as 2.4.7; the evil roar remade again in 2.4.8: next-ai-2.4.8.md)
3. **The Conqueror's charge and releases** (the owner: "I want a badass animation for the Conqueror Haki charge / release. the current
   one is dog shit."): `tools/conqueror_anims.py` writes conqueror_gear5_charge, supreme_conqueror_release and conqueror_gear5_release
   (`tools/anim_polish.py` GENERATED: it leaves them alone now). Judged with contact sheets (front/side/three, tick by tick through the
   crunch; scratch only), anim.lint clean, feet within 0.13 px of the ground every half tick (each beat's body height solved from its
   legs: `grounded`), the charge's loop seam exact.
   - Charge (the server roots the player while charging; full at 160 ticks): build 0-160 then a loop 160-200 (isLoop, returnTick 160).
     COIL_LIGHT -> COIL_DEEP: fists pulled in at the waist, then up under the chest, upper arms back, elbows flared; wide stance, knees
     bent over upright shins; the body bend (the hunch) 8 -> 22; the head hangs, then lifts to glare. A breath (body bend, arm pitch)
     quickening from 32 to 20 ticks a breath (phase integrated, whole breaths in the loop), a tremor (sums of sines, 7-17 cycles in 40
     ticks, all under half a cycle a tick) growing 0.25 -> 1, shudders at 54, 92, 128 and one a loop (182).
   - Releases: from the full coil, the crunch at tick 2 (forearms crossed before the chest, curled, right knee drawn up), the burst at
     5 (outexpo: the foot stamped down wide, arms flung wide/down/back, chest open with a bend back, head back), an overshoot at 7, the
     roar held shaking (dying down) to 26 (supreme; 18 partial), the exhale, standing at 56 (48). The partial one at reach 0.6.
   - The files are one move a beat with every part and channel in it (the original's own animations load that format: 771 such
     moves in the input jar): the charge is 110 KB (one channel a move was 553 KB).
4. **The release's sounds** (the owner, session 6: "The volume of the EVIL conqueror release has to be louder and more bass boosted, the
   crackle lightning sound at the end is to be removed along with all the old sounds of the letting go of G button max charge, keep
   the explosion sound and the new one we just added."):
   - Which they were (decompiled): the server's `HakiServerController.sound` is the original's `delayedSound` (20 ticks later, volume
     x 0.5, capped by `SoundBalance`): the supreme release's SNAP (a bright crack) and BLAST (a 4.6 s boom, 64 % of it under 150 Hz:
     the explosion), and `scheduleSupremeAudio`'s RUMBLE (a crackling tail, 25 ticks after); the client's `HakiBolts`: a zap at each
     lightning composition's launch, LAST_STAND_LIGHTNING thunder cracks where the full storm's bolts land, zaps where the others land;
     the three aftershocks (18, 42, 74 ticks after, `HakiVfx.aftershock`) are lightning compositions too (their crackle, at the end).
   - Now (for every player: my reading of "all the old sounds"; the notes ask whether neutral should keep them): `PiecePatch`
     CALL_REDIRECTS in releaseConqueror: `sound` -> `ConquerorRelease.sound` (drops SUPREME_SNAP, passes the rest to `delayedSound`),
     `scheduleSupremeAudio` -> `ConquerorRelease.rumble` (nothing); in `HakiVfx.accept0`: `aftershock` -> `HakiStorm.aftershock`
     (VFX_WIDENED +aftershock; marks the composition `HakiBolts.silence`). `HakiBolts.Pending.silent` (the full storm sets it too): no
     launch zap, no loud landing. Kept: the BLAST and the evil roar. A partial release keeps CONQUEROR_RELEASE and its zaps.
   - The evil release roar: `tools/evil_conqueror_sounds.py` (the voice lines only with `--voice`; unchanged): deeper bass and sub
     boost (male bass g=15 at 80 Hz, asubboost wet 1, lowshelf +6, +4 dB at 55 Hz; female g=11 at 100 Hz, wet 0.7), compression
     -30 dB 6:1, then the gain driven into a tanh soft clip (threshold 0.85) and the limiter, raised until -9.5 LUFS: -9.7 LUFS (was
     -12.7 male, -12.4 female), below 150 Hz -4.8 dB RMS male (was -9.2), -6.5 female (was -13.6); crest ~3.4 dB (heavily compressed:
     a harder drive, -8.5 LUFS, left a 2 dB crest, a square wave). Minecraft cannot play a file louder than it is.
   - The blast still comes 20 ticks after the release (the original's delayedSound), the roar at once: the notes ask.

## Open
1. The owner's report on 2.4.7 (the animations, the black hands and their glow, the HUD, the sounds) and still on 2.4.5/2.4.6 (the
   lunge, ranges, release lag and landings, the alignment bar, the female voice): ask.
2. Whether a neutral player should keep the full release's old sounds; whether the blast should come at the moment of release.
3. Good's release sound when they send it; killing humans -> evil once humans exist; evil/good thresholds (-25/+25) to confirm.
4. Older: the Fishman Karate white screen, the stunned player's own camera.

## Tools (session 6)
- `tools/conqueror_anims.py`: the three Conqueror animations; prints the lint of each. Poses (COIL_LIGHT/DEEP, CRUNCH, BURST, OVER,
  ROAR, EXHALE) at the top; `grounded(q)` sets a beat's body height from its legs; `nudge` adds deltas (arms/legs mirrored).
- Judging a pose before it goes in a file: draw it with `anim.draw` on `anim.skeleton`'s boxes (a turnaround of variants side by side);
  a tick-by-tick sheet of a file: `anim.load(path).pose(t)` for each tick. Scratch only (the owner wants no renders unless asked).
