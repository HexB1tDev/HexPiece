# HexPiece: handoff, 2.4.7 IN PROGRESS (stopped by the owner mid-session; NOT released)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.6.md covers 2.4.6 (alignment, the evil Conqueror's sounds), next-ai-2.4.5.md
2.4.5 (the owner's 2.4.4 report: first-person blade, M1 lunge, range by sword, the release's lag and landings). Branch
`claude/nice-bell-gam7qf`. `build.py` VERSION is already **2.4.7** (unreleased: no `.github/release-notes-2.4.7.md` yet). The owner said
"Stop, handoff+prompt now": pushed without a CI check (the stop rule); check the branch's last "Build HexPiece" run first next session.

## The owner's report after 2.4.6 (verbatim, with two screenshots)
1. "I dont want the Ryou haki to be a painted outline, just turn the player's hands black the skin itself like the Nika fruit changes
   the skin (slowly pixel by pixel, exactly how the skin turns white), and the glow around the outline is not meant to be literal
   painted lines, it's meant to be kinda like the minecraft glow effect around the firsts, thats it." (screenshot: black hands with
   cyan bands along every edge of the hand box and a cyan star disc)
2. "Why the fuck is the mastery hud menu back?" (screenshot: the original's bottom-left panel: HEXHAKI 2000/2000, ARM/OBS/HAO 1000,
   PINNACLE, under HexPiece's own abilities panel)
3. "I want a badass animation for the Conqueror Haki charge / release. the current one is dog shit."

## Done (committed, pushed, not released, not tested in game)
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

## Not done: item 3, the Conqueror charge / release animations
- Which play (decompiled `HakiServerController`, CFR from Maven Central over input/hexhaki-2.1.0.jar): the charge (holding G) plays
  `conqueror_gear5_charge` (animate(player, ..., 5) in beginConquerorCharge); with HexPiece's JoyBoy patch every full-charge radial
  release plays `supreme_conqueror_release`, a partial one `conqueror_gear5_release`, a tap `conqueror_tap_point` (`conqueror_scream` is
  for maxHold when not supreme: effectively unused now). HexPiece already overrides all of them in
  src/main/resources/assets/hexhaki/player_animation/ (2.1.1 reworks, hand-written JSON, no generator): the charge moves ONLY the head
  (a twitch, 66-tick loop returning to 8); the releases are a head snap / an arm whip. The owner: "dog shit".
- Plan: write them in a generator (tools/style_anims.py's `write`/`p`/`arm`/`leg`/`shake` helpers and conventions; anim_hand.spine)
  and judge with tools/anim_preview.py (contact sheets, front/side) and anim.lint (no arm through the head/body):
  - charge (loop; ease in from what the player was doing): a power-up stance: feet wide, knees bent, body sunk and hunched a little,
    fists clenched low at the sides with the elbows out and the arms tense, chest heaving, the head lowered then raised to glare,
    a tremor building (shake on body roll / arms), now and then a shudder; returnTick after the wind-in.
  - supreme release (full charge): a 2-3 tick crunch (arms crossed in front, body curled, knees deep) then an explosive spread: arms
    flung wide and down-back, chest arched open, head thrown back (a roar), legs planted wide; held trembling through the shockwave
    (~20 ticks), then settling to standing by ~40-56 ticks.
  - partial release: the same spread at about half amplitude, shorter.
  - Keep the durations the server expects (check where the original stops/clears them), and that they are not per-gender.
- Then: release notes `.github/release-notes-2.4.7.md` (plain words: the HUD, the Ryou skin and glow, the new animations), handoff, push,
  check Build and Release are green, ask the owner to test.

## Open from before
The owner has not reported on 2.4.5/2.4.6 in game yet except this message (the lunge, ranges, release lag and landings, the alignment
bar and evil sounds: ask). Good's release sound when they send it; killing humans -> evil once humans exist; evil/good thresholds
(-25/+25) to confirm.
