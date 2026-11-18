# HexPiece: handoff after 2.4.5 (the owner's 2.4.4 report: swords, the Conqueror's release)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.4.md has the owner's report in their words (five issues, the reference
pose), next-ai-2.3.6.md 2.3.6 to 2.4.4, next-ai-2.3.2.md the sword solver and the owner's sword rule. Branch `claude/nice-bell-gam7qf`.
**The next version is 2.4.6** (`python3 tools/next_version.py --write`). Never tested in game (only the owner can).

## State (kept current while working; session 5)
- Done and committed locally: items 1-3 of the report (below). Working on: item 4 (the release's lag), then item 5 (the lightning's
  landings), then the release notes and the push.

## Done in 2.4.5
1. **No saya in the first-person hand while drawn** (the owner: "Gryphon in first person when unsheathed still shows has having the
   sheath on"): `client/sword/KatanaItemRenderer.held` (the game's own first-person hand: the guard, the idle, the draws and
   sheathings leave first person to it, SwordClient) drew the blade and the saya always. Now `drawnNow(katana)`: the local player's
   sword drawn (`SwordAnims.drawn`, as SwordLayer asks in third person) -> the bare blade, glinting; the saya only while the blade is in
   it. Every blade.
2. **M1 reach** (the owner: "the arms should be fully stretched and the blade pointed forward far from the player's chest"): measured
   first: every cut was straight for half a tick with the blade steeply up, then folded (elbow 45-124 deg) and held the follow-through
   with the blade wrapped round behind the left hip (tip 0.4-1 block BEHIND the feet in the hold). Now (tools/sword_anims.py, the
   block after `ARC_WIND`): `lunge(body, blade, edge)` (blade and edge in the world: right, up, forward, turned into the layer frame
   through the beat's body, `world()`), `extend(pose)` (the fist LUNGE_REACH 8.55 px from the shoulder: the arm straight, elbow ~2;
   8.4 was 22 deg), `reaching(beat)` (the reach grip: the hand slides LUNGE_SLIDE 3 px down the handle to its end, so the hand can tip
   the blade LUNGE_KINK 30 deg off the arm's line, towards its edge, without the pommel going through the forearm; elbow target -2).
   `strike()` takes `end_blade`/`end_edge` (world) instead of `end`/`overshoot`: the mid beat extended (not the thrust's or the rising
   cut's: `mid_extended=False`), the end and the hold in the lunge (LEGS_LUNGE_WIDE, L_LUNGE: the free arm straight back, the head
   turned back to the foe). densify swings the fist round the shoulder next to a lunge beat and carries the slide. The cut stops EDGE
   DOWN (an edge to the side put the arm 30 deg sideways: across the middle of the first-person view). The pivot cut, aerial cross,
   wheel, plunge, the three draw cuts and the charged release likewise. `tools/sword_rig.py` ULNAR 40 -> 60 (the rig's handle crosses
   the fist square; a real hand's 40 plus the handle's slant across the palm). Results (tip ahead of the feet during the hit, old ->
   new): kesa 1.56 -> 2.55, gyaku_kesa 1.74 -> 2.44, yoko 1.73 -> 2.57, gyaku_yoko 1.49 -> 2.47, kiriage 1.67 -> 2.31, tsuki 2.24 ->
   2.73, makko 2.04 -> 2.59, advancing 1.87 -> 2.61, draw cut 1.78 -> 2.23, release 2.07 -> 2.59; the held end pose now 2.2-2.7 ahead
   (was behind the player). 16 animations re-solved; the others kept as they were (re-solved, five came out different with the wider
   wrist and three of Yoru's dipped 0.04 into the floor: the old files restored; all pass). Pose check green.
3. **Range by sword** (the owner: "Different swords should have different range (Yoru is the biggest)"; one animation set for every
   blade): `Katana.reach()` = its own length past the path's blade (`Katana.tip()`, model px measured from each mesh, build.py
   `check_blade_tips`; `SwordRules.PATH_TIP` 21, `PX` 0.9375/16) + `Grade.reach()` (Wazamono 0, Ryo 0.15, O 0.3, Saijo 0.45) + a
   legend's own (Gryphon 0.3, Yoru 0.75). Yoru +1.86, Gryphon +0.83, Imperial Phoenix +0.64 ... Amber Wasp -0.11. Added past the
   blade's swept path (`SwordServer.sweep`'s extend) for cuts, the clash reach, the quick draw, now also the charged physical cut and
   NPC cuts (SeaFighter.reach). The tooltip says "Reach +x.x". SwordTests.reach.
