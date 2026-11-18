# HexPiece: handoff after 2.5.5 (2.5.5: the white screen fixed; 2.5.4: the head free in sword animations, first person with the sword sheathed, the Haki slash's cooldown)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.3.md (Ryou's fist aura as geometry, the release's lightning
under Ryou, the skin readback; the Fishman Karate white screen still open), next-ai-2.5.2.md, next-ai-2.5.1.md (**the owner's device**:
Android, Amethyst, MobileGlues = OpenGL ES).

Branch `claude/nice-bell-gam7qf`. **2.5.5 is released** (if the Release run is green: check). **The next version is 2.5.6**
(`python3 tools/next_version.py --write`). Nothing has ever been tested in game except by the owner.

## The owner (session 7, after 2.5.3, verbatim)
- "haki is beautiful and perfect, well done." "and yes the skin is fixed" (2.5.3's aura and skin check: done, keep them).
- "Now please reduce the range of the sword slash, its WAYYY too far." then "forget the range resize, keep it as it is but vastly
  increase the cooldown of the sword slash projection move."
- "the sword first person, if the sword is holstered the hand should be resting on the holster too, not just on the players hand,
  the first persons are meant to reflect exactly what the player is doing"
- "Top priorities: 1. Fix the head on sword animations, it should not be animated and should be free 2. Fix the first person.
  3. Resume duties"

## Done (2.5.4)
- **The head free in every sword animation:** `tools/sword_anims.py` writes no head channel any more (`solve_and_write`: parts without
  "head"; the solver still poses the head for its clearance checks); the 26 written `sword_*.json` that keyed the head had those keys
  dropped (1,383 keyframes; every other key byte for byte as before: checked against HEAD); `client/npc/SwordPoses.java` regenerated
  (`sa.SOLVE = False; sa.build(); sa.java_npc_poses()`). build.py's pose check: 30 animations clear. The stances (arms only) and the
  other styles' animations were not touched (not asked).
- **The Haki slash:** range unchanged (16 - 220). `SwordRules.SLASH_COOLDOWN` 600 ticks (was 60), config key renamed
  `slashCooldownTicks` (0 - 12000) so saved worlds take it. `SwordServer.charge`: while the Haki slash cools, Armament on, the charge
  is a steel cut (its own `cutReady`), the action bar showing the slash's wait; `release` no longer says "Not enough Haki" for that.
  SwordTests checks 600 and >= 10x the cut's cooldown.
- **First person with the sword sheathed** (`sword/HolsterView` pure + `client/sword/HolsterHand`): `RenderHandEvent` for the main
  hand, the item the local player's active katana, not drawn (`SwordAnims.drawn`) and no swing (`SwordClient.swing`): cancelled and
  replaced: the sheathed katana (saya, then the blade with the hilt, black under Armament, `Koiguchi` push when standing ready) at
  `HolsterView.katana` steps and the left arm through the game's own `PlayerRenderer.renderLeftHand` (skin, Armament, Ryou aura) at
  `HolsterView.arm` steps (only with the off hand empty). HolsterView is set by intent (guard position, handle direction, edge up;
  fist REST px down the handle and OVER px above it, forearm direction, back of hand) and works out the Y-X-Z turns (`euler`). Equip
  progress lowers it 0.6 as the game's hands; a 0.006 breath. Yoru (on the back): cancelled, nothing drawn (the hands are empty).
  `tools/holster_preview.py` (with `tools/preview/HolsterDump.java`) renders it with the owner's meshes from the game's own steps;
  judged through several iterations (the fist first hid the whole hilt; now it rests on the handle's end, the guard and the saya's
  mouth showing to its left, the arm rising from below the screen like the game's own hands). SwordTests.holster: 15 checks.
- **The Ryou aura and coat on the first-person hand are drawn straight** (`RyouHands.glowFirstPerson`, `coatingFirstPerson`; PiecePatch
  CALL_REDIRECTS in `HakiHandLayer.renderFirstPerson`): playerAnimator leaves the first-person hand unbent, but the aura read the
  playing animation's elbow bend (the stance bends the left one ~50 degrees, and the holster's arm is that one).

## Done (2.5.5): the "Fishman Karate white screen" (open since 2.3.2)
- The owner: "The starter Menu, yes the screen is FULLY white permanently, no matter what I do. Yes I can open the Esc. No it does not
  happen with other styles", two screenshots (white; only the abilities panel's shadowed title "FISHMAN KARATE" and the chat line
  "You set sail as a pirate, fighting with Fishman Karate." showing, the chat box's background dark navy: the world under it) and the
  log of that session (2.5.2, a fresh "New World (6)" after fighting marines in another world; no error at all).
- Cause: the original's `com/hexhaki/fx/client/ImpactClient` (hit flash, combo, title, shake; RenderGuiEvent.Post) keeps
  flashStart/flashUntil/comboAt/titleAt/shakeUntil in `ClientLevel.getGameTime` and never clears them (no logout handler). A flash
  from a world with a large game time is "running" in a new world (clock near 0) for hours: `hud` draws it while now < flashUntil + 3,
  with progress < 0 read as full strength, a white quad over the whole HUD at z 0 after everything: only what the game draws a hair
  in front survives (shadowed text's glyphs, the chat at z 50). The start menu covers it until Set Sail. Not style-specific.
- Fix: `client/fx/ImpactReset` clears ImpactClient's fields by reflection whenever the client level changes or its game time goes
  back, and on logout; PiecePatch WIDENED `ImpactClient.drawFlash` and CALL_REDIRECTS its call in `hud` to `ImpactReset.flash`, which
  draws only for 0 <= progress <= 4 (else clears). build.py `check_impact_fields` proves every field name and type in the patched jar
  and the guarded call (a reflection test in the suites cannot load the class: Minecraft is not on the test classpath).
- Not checked: other original effects keyed on game time (HakiImpactScreen, KingsGripImpactFrame, ConquerorShockScreen,
  CinematicController) may have the same stale-state flaw across worlds; none was seen.

## Open
1. The owner's report on 2.5.4 (the head, the holster view, the cooldown). Possible follow-ups: the holster view during a draw (the
   left hand stays on the hilt until the blade is out, then the right hand holds it: a first-person draw could be animated); the
   third-person stance only rests the hand on the hilt when standing still in combat mode, first person always while sheathed (the
   owner's words: "if the sword is holstered the hand should be resting on the holster"); ask if they want them to match differently.
2. The white screen: fixed in 2.5.5 (above); ask the owner to confirm.
3. Waiting on the owner from 2.4.7 - 2.5.2 (next-ai-2.5.2.md).

## Tools
- `tools/holster_preview.py [out.png] [katana]` (2.5.4), `tools/ryou_preview.py` (2.5.3), `tools/sword_check.py` (sword poses),
  `tools/srg_shapes.py <joined.tsrg>` after adding SRG references (joined.tsrg from MCPConfig on GitHub raw).
