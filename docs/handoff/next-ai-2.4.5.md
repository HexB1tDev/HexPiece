# HexPiece: handoff after 2.4.5 (the owner's 2.4.4 report: swords, the Conqueror's release)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.4.md has the owner's report in their words (five issues, the reference
pose), next-ai-2.3.6.md 2.3.6 to 2.4.4, next-ai-2.3.2.md the sword solver and the owner's sword rule. Branch `claude/nice-bell-gam7qf`.
**The next version is 2.4.6** (`python3 tools/next_version.py --write`). Never tested in game (only the owner can).

## State (session 5)
- All five items of the owner's 2.4.4 report done and released as 2.4.5 (`.github/release-notes-2.4.5.md`); the owner was asked to
  test. Nothing is tested in game. Ask the owner first, next session: how the M1 lunge, the first-person blade, the ranges and the
  release (lag and landings) feel; whether the release still lags and whether it is FPS or the server (the notes ask them).
- The owner does not want renders or GIFs unless asked; judge your own work numerically or with the preview tools.

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
4. **The full release's lag** (measured with harnesses over the pure classes, scratch only: count and time what one release makes):
   - Server/clients, the fissures: one release tore out ~10,000 unique blocks over ~50 chunk sections (cuts every 0.6 blocks, 4.8
     wide and 6 deep), 700 a tick (each a light update and a chunk-section rebuild on every client near), and regrew 900 a tick 15-25 s
     later. Now `haki/Fissure` HALF_WIDTH 1.5, DEPTH 3.5 (~3,300 blocks), `server/Scars` DESTROY_BUDGET 240, CHECK_BUDGET 12,000,
     RESTORE_BUDGET 200.
   - Client, the 3D lightning: every "beam + add" pair of the original's range lightning becomes 3D bolts (BoltPainter.strokes: the
     black sheath, the red, the white core, the ribbons and forks each their own bolt); a supreme release drew the range lightning
     twice (`addConquerorRangeLightning(..., seed ^ 0x6A09E667F3BCC909L, true, 7)`): ~3,000 bolts, 75,000 points, a peak of 80,000
     quads a frame (BoltMesh.build every frame, ~9-13 ms of CPU here before the vertices are written). Now the patch
     (CALL_REDIRECTS row: `HakiVfx.conquerorRelease`'s two calls -> `client/haki/HakiStorm.range`, `addConquerorRangeLightning`
     widened via VFX_WIDENED) draws only the first pass: ~1,570 bolts, a peak of 53,000 quads (~6 ms). Thinning the second pass
     instead (dropping the strokes under 0.06 wide) saved only 7 %: the sheath and red tubes are the cost.
   - Client, the launch: every call also made Photon fallback beams (Jag pieces and a re-strike, up to `HakiLightning.BUDGET`),
     removed at launch pair by pair (`containsSame`, quadratic). Now an identity set; BUDGET 800 (2400).
   - Not changed: the original's own release particles (~1,400 Photon particles for the supreme one), HakiBolts' per-frame meshing
     (could be cached per bolt when its dance is off; the bolts dance every frame now).
   - Not known: whether the owner's lag was FPS or TPS (both made cheaper; the notes ask).
5. **Lightning "always the same spot"**: traced: the seed is fresh every release (`ConquerorRelease.visual`: the player's random,
   sent in S2CHakiVisual to every client and kept in SEEDS for the server's fissures; the charge's own contact bolts get a fresh one
   per emission in the original's `emitConquerorChargeLightning`). The release's damage does not use the bolts at all (the original's
   applyConqueror is radius-based). What repeated was the shape: every bolt aimed down met the ground ~2.4 blocks from the feet
   (ConquerorBolts: where its line crosses the floor; ConquerorStorm: grounded from its START when aimed steeply) and ran outward.
   Now (`haki/ConquerorBolts`, dome only): `landings()` deals the bolts aimed below LANDS_BELOW 0.2 round the player (one each in its
   own sector from a random start, +-0.3 of a sector, the sectors in the order of their own headings) at distances spread by area
   between LAND_NEAR 0.3 and LAND_FAR 0.95 of the radius (shuffled); `strike()` arcs each out of the shell (`arcHigh`: rising up to
   LAND_ARC 0.16 of the line, at most 6 blocks) onto the ground there, then a short run on. `haki/ConquerorStorm.bolt`: a strand aimed
   down glides from the chest to strike LAND_NEAR 0.08 .. LAND_FAR 0.38 of its length out (by area), then tears on. LightningTests
   .landings (sectors, distances, coverage over 200 releases). Render (scratch): tools/preview/LightningDump with four full releases.
   PieceNetwork protocol 8 (the storm's shape is shared by the server's fissures and the clients' bolts).

## Open
1. Whatever the owner reports from testing 2.4.5 (the lunge in first and third person; the hand at the handle's end; the ranges; the
   release's lag and landings).
2. If the release still lags: find which (FPS or TPS) first. Client options left: cache BoltMesh per bolt between dances, merge the
   coaxial sheath/red/core strokes into one bolt kind, cut the original's release particles. Server: the fissures' size again.
3. Older open items (next-ai-2.3.6.md): the Fishman Karate white screen (never reproduced), the stunned player's own camera does not
   fall with them.

## Tools (this session)
- tools/sword_anims.py `--only a,b` (13 s a move; the whole set ~4.5 min on 4 cores now). After a full solve, moves not redesigned can
  come out different with the solver's limits changed: compare with git and restore those that were fine (then regenerate with
  `sa.SOLVE=False; sa.build(); sa.java_paths(); sa.java_npc_poses()`).
- Measuring a move's reach: load the written JSON with `anim.Anim`, `sa.game_blade(move, q, t)`, the tip `rig.point(m, (0,
  rig.BLADE_TIP, 0))`: forward is -z, right +x, up +y (blocks from the feet).
- The first-person view of a swing: `tools/sword_gif.py` view `fp` (sword_view.EyeCamera, 12 deg down, fov 70).
- The original's code: CFR (Maven Central `org.benf:cfr:0.152`) over the classes of input/hexhaki-2.1.0.jar, in the scratchpad.
