# HexPiece: handoff after 2.5.4 (the head free in sword animations; first person with the sword sheathed; the Haki slash's cooldown)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.3.md (Ryou's fist aura as geometry, the release's lightning
under Ryou, the skin readback; the Fishman Karate white screen still open), next-ai-2.5.2.md, next-ai-2.5.1.md (**the owner's device**:
Android, Amethyst, MobileGlues = OpenGL ES).

Branch `claude/nice-bell-gam7qf`. **2.5.4 is released** (if the Release run is green: check). **The next version is 2.5.5**
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

## Open
1. The owner's report on 2.5.4 (the head, the holster view, the cooldown). Possible follow-ups: the holster view during a draw (the
   left hand stays on the hilt until the blade is out, then the right hand holds it: a first-person draw could be animated); the
   third-person stance only rests the hand on the hilt when standing still in combat mode, first person always while sheathed (the
   owner's words: "if the sword is holstered the hand should be resting on the holster"); ask if they want them to match differently.
2. **The Fishman Karate white screen** (next-ai-2.5.3.md Open 1): still waiting for the owner's answers.
3. Waiting on the owner from 2.4.7 - 2.5.2 (next-ai-2.5.2.md).

## Tools
- `tools/holster_preview.py [out.png] [katana]` (2.5.4), `tools/ryou_preview.py` (2.5.3), `tools/sword_check.py` (sword poses),
  `tools/srg_shapes.py <joined.tsrg>` after adding SRG references (joined.tsrg from MCPConfig on GitHub raw).
