# HexPiece: handoff, 2.3.2 work in progress (the owner's sword bugs, natural arms, the victim's split view, NPC gestures)

**State when the owner said stop**: everything below is committed as WORK IN PROGRESS. A full re-solve of the sword animations was
killed mid-run, so the animation JSONs are a mix of the last two solver versions and BladePaths/SwordPoses are from the run before:
**first run the full solve** (`python3 tools/sword_anims.py`, ~16 min on 4 cores), then `python3 build.py`. The build's new pose check
(`check_sword_poses`) WILL FAIL until the remaining problems are fixed (about 50 of the 65 sword animations still report something; most
are under 1 px). The pushed commit's "Build HexPiece" run is expected red for that reason. The release notes are drafted at
`docs/handoff/release-notes-2.3.2-draft.md`: move them to `.github/release-notes-2.3.2.md` only when the build is green (that
publishes v2.3.2).

## Where the pose check stood (last complete solve, before the last two metric changes)
- arm through the body: 19 moves, median 0.5 px; sword (blade) through the body: 30 moves, median 0.9 (mostly the katana's pommel cap
  against the belly at the end of recoveries: now counted as the handle, allowed to 0.8); through the right forearm: 39, median 0.7
  (the pommel cap at the wrist: handle); through the head: 17, median 0.7, worst 3.4 (Yoru: idle t68, sheathe t23-25, gyaku_kesa);
  through the left arm: ~7 moves, 1-2 px (the backhand wind-ups: `clear_left` now nudges hand-set left arms out of the blade).
- wrist tipped past ulnar/radial: 9 moves (worst at t1.5 of gyaku_yoko/pivot/gyaku_kesa wind-ups: now routed out in front, `route`);
  hand turned (pronation) past 80: 12 moves, median 11; "upper arm turned" was a broken metric (undefined with the arm straight up):
  replaced by `fold_strain` (the elbow folding out or back), not yet solved with.
- `anim.lint` "rightArm through the head" (the old lint, also in build.py check_animations): 6 moves.
- Next fixes in order: re-solve with the current code; read `depth.py`-style summaries (see Tools); fix choreography where the
  transitions still pass through the body (add waypoints); Yoru's draw/sheathe/idle by eye; then clear SWORD_POSE_EXCEPTIONS (empty).

Read CLAUDE.md first. This note is the newest; next-ai-2.3.2-wip.md was the stopped session's (its open list is carried below),
next-ai-2.3.1.md the armor, next-ai-2.3.0.md the clash rules in the owner's words. Branch `claude/nice-bell-gam7qf`. **The next
version is 2.3.3.** Nothing was ever tested in game (only the owner can).

## The owner, this session (quoted)
- On my first shoulder carry: "the blade edge should be facing up..", "and the arm is twisted in a bunch of these, how does this
  pass", "does this look right? the sword handle clipping through the shoulder?", and on Yoru's draw: "the only issue I see is that
  the shoulder is grabbing the sword in the opposite way it's meant to." (the hand came at the hilt from below, elbow out and down).
- On the finisher's screen line: "the bar should split the whole screen in 2".
- The owner sees the renders as they are made: judge them yourself first, every time.

## Done in 2.3.2
- **Per-blade saya slide** (`tools/saya_slide.py` -> generated `sword/SayaSlide.java`, used by `client/sword/SwordLayer.slide` and
  `sword_rig.arc(out, key)`): for each px drawn, the rigid turn+shift that keeps the part still inside on the saya's line, fitted from
  the blade's mesh (worst miss < 0.25 px for all 26 hip blades). Before: one circle (SORI 180) for all; Gryphon (curves ~3x harder)
  came out through its scabbard's side.
- **Blade profiles** in `tools/sword_anims.py` (`Blade`: KATANA, GRYPHON, YORU; `PROFILE` while writing; `build()` writes each set;
  names get `_gryphon` / `_yoru`). Each has its home pose (cuts start/end there), noto line (`noto_p`, `noto_out`, `noto_pull`,
  `draw_slide`), guard (walking, right arm only), idle (standing, head free). `SwordAnims.name(anim, katana)` maps every anim to the
  blade's own; a legend's draw moves -> its branch's first cut, quick draw -> quick cut, charge draw -> charge (Yoru keeps its own).
  `SwordAnims.idle(katana)`; `SwordClient` plays the idle when the player has stood still `STILL_AFTER` ticks, the guard otherwise.
- **Floor**: `lift_clear` turns a beat's blade up until its point is `FLOOR` (0.12 blocks) over the feet's floor; `solve(..., ground)`
  re-lifts after solving if the natural arm let it sag. Aerial moves (`ground=False`) are exempt; Yoru hanging on its back too (it hangs
  to the floor by design: `BackCarry`, now 0.8 px further off the back, `GUARD` z 6.4).
- **Natural arms** (`sword_rig`: `arm_strain`, `_Arm`, `solve_blade`): humeral twist (in 70, out 45), elbow (0..125), the hand's
  hold on the blade (ulnar 40, radial 25, pronation 80, wrist bend 65), no arm through the torso (elbow and forearm; a fist may rest on
  the body), and the sword's MESH (blade, guard, handle; `set_sword`, `sword_points`) out of the arm that holds it, the body and the
  head (`sword_hits`). `solve_blade(upper=...)` picks which way the elbow goes where two would reach (Yoru's hilt from above:
  `OVERHEAD`). Fixed-dict left arms must pass too (L_TUCK re-solved).
- **The check that makes "how does this pass" impossible**: `clearance()` reports strain (> 8 deg past a limit), arm through body
  (> 0.4 px), sword through an arm/body/head (> 0.35 px; Yoru's guard behind the head at its hilt 0.6), point in the floor, fist in
  the face; `build.py check_sword_poses` runs it on every sword animation every half tick and FAILS the build. CI installs numpy and
  Pillow for it (both workflows).
- **Densify through the noto line**: a beat on the saya's line counts by its blade there (`noto_frame`), so the swing from the line to a
  cut is designed and solved per tick (it used to cross the head). Draws whip forward first (`whip`), then out.
- **Durations**: `SwordRules.drawTicks/sheatheTicks(katana)`: katana 28/24, Gryphon 32/28, Yoru 44/36; the server uses them.
- **Sounds** retimed (`SwordSounds`), Yoru's own pre-pitched (`PITCHED`).
- **Finisher victim**: `ScreenLine` (pure: the line's bands, and `split(age)`, the halves' motion) and `ScreenSplit` (the frame copied
  with `GlStateManager._glBlitFrameBuffer` into a `TextureTarget`, registered as texture `hexhaki:piece_split_view`, blitted as two
  halves). New proven references in verify/api-allowlist.txt (2.3.2 block).

## Tools
- `python3 tools/sword_anims.py [--only a,b] [--preview DIR] [--no-solve]`: a full solve takes about 16 min on 4 cores (parallel);
  `--preview` writes one full sheet per move (a column every 2 ticks; long loops every 8). `--no-solve --preview DIR` renders the
  written animations only.
- A summary of what the check finds per move and per kind: build MOVES with `sword_anims.SOLVE = False; sword_anims.build()`, then for
  each move `rig.set_sword(move.profile.key); sword_anims.clearance(move)`; the depth is in brackets in each message.
- Zoom on frames: write a small script calling `sword_anims.build()` with `SOLVE=False`, then `sword_view` (see the session's
  `zoom.py` pattern: views front/three/side/back, camera pitch).

- **NPC gestures** (the owner: "the NPCS ... can have the same ambient animations as HexGhoul"): `npc/Gesture` (LOOKOUT, CROSS,
  STRETCH, YAWN, CRACK, SHRUG, CHIN, LAUGH, TAP; Marines' SALUTE, EASE; `pick(side, roll)` by side, `pause`), `npc/GesturePose`
  (pure, HexGhoul EtoPose's shapes in degrees on the vanilla model's axes, straight arms: an NPC has no elbows; eased in/out by the
  weights), `SeaFighter.idle` (synced GESTURE/GESTURE_START; stands for it, ends on a foe or a move), `SeaFighterRenderer` (Shown
  anim GESTURE). Preview: `tools/preview/GestureDump.java` + `tools/gesture_preview.py dump out.png [--keys]`. Tests in NpcTests.

## Open, in the owner's order
1. More gestures per persona (HexGhoul's have Ken/Touka/Eto styles: a captain could swagger, a recruit fidget); none tested in game.
2. Clashes with impact frames, the owner's Armament and Ryou rules (next-ai-2.3.0.md item 1).
3. Galaxy Fist for Iron Fist, the Fishman Karate rework, Six Powers (next-ai-2.2.6.md).

## Not verified (only the owner can)
Everything in game: the idle/guard swap, the split view on a real GPU and on Android (the framebuffer blit), Gryphon's slide.
