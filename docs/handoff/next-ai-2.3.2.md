# HexPiece: handoff, 2.3.2 work in progress (the owner's sword bugs, natural arms, the victim's split view, NPC gestures)

## STATE WHEN THE OWNER SAID STOP (session 2; read this first)
- Pushed as work in progress, CI NOT checked (the owner's 98% / stop rule). The last full solve was STOPPED mid-run on purpose, so
  the animation JSONs are a mix of this session's solver versions; BladePaths/SayaSlide/SwordPoses were regenerated from them with
  `SOLVE=False` so the Java compiles. `python3 tools/sword_check.py` on them: 39/65 moves fail, mostly files written before this
  session's fixes (wound ends, the old Yoru draw). Version 2.3.2 is still NOT released (notes: release-notes-2.3.2-draft.md).
- FIRST: `python3 tools/sword_anims.py` (the full solve now takes 40-60 min on 4 cores: run it in the background with a 2-hour
  limit; the 30-min default killed one), then `python3 tools/sword_check.py` (3 s: every problem kind with its worst depth and
  ticks; `tools/sword_check.py 'sword_.*_yoru'` for a subset), fix, `--only a,b` to re-solve a few, then a final full solve, then
  `python3 build.py`. The Java side of Yoru's glide (SwordLayer.back/backLift/backGlide/slerpZyx) has NOT been compiled yet.
- Expected after the full solve (from the targeted tests): the katana and Gryphon moves pass or nearly; left to judge and fix:
  Yoru's draw/sheathe/charge draw with the NEW carry (untested with it: render it, check the ropes), aerial_wheel's fist at t2.5,
  gyaku_kesa_yoru's head 0.7 at t2, kiriage_yoru body 0.5, any "written wound" end left (unwrap's end switch), and the near-limit
  0.4-0.6 px residuals. Then render the FULL sheets of every Gryphon and Yoru move (`--no-solve --preview DIR`, a column every 2
  ticks) and GIFs, judge them, and only then move .github/release-notes-2.3.2.md into place (it publishes v2.3.2).
- The owner asked: "apply the same animation to all the diff katanas that are not Gryphon and Yoru": they already share one set
  (names without a suffix); only Gryphon (`_gryphon`) and Yoru (`_yoru`) have their own. Keep it that way.
- Tools added: `tools/sword_check.py` (the summary), `tools/sword_zoom.py MOVE t1,t2 OUT.png [views] [pitch]` (big frames, the
  game's own blade incl. Yoru's lift/glide).

## Session 2 (running state, kept current: if this section is the newest thing, read it first)
- The owner's new stop rule (also in CLAUDE.md): at 98% WEEKLY usage, write this handoff and next-prompt.md first, commit, push,
  stop. No CI check.
- Found: build.py's pose check never switched the sword mesh (every Gryphon/Yoru move was checked against the katana's):
  `clearance()` now calls `rig.set_sword(move.profile.key)` itself.
- First full solve this session: 48/65 moves failed. All twisted-arm failures (hand turned, wrist tipped, elbow folding out)
  were on HALF ticks only: each solved tick was natural, the game's interpolation between two ticks was not (the hand flipping
  +80 to -80 pronation in one tick in sword_plunge t14-15; makko's Euler angles jumping 50-60 deg at t2-3).
- Solver changes (tools/sword_rig.py, tools/sword_anims.py): `refine()` after each solve: the game's in-between frames
  (`gap_problems`, every half tick, interpolated exactly as anim.pose does) are checked, and a beat whose gap is bad is solved again
  with those frames priced in (`solve_blade(between=...)`, `rig.blend`, `rig.nearest_euler`). `solve_right`/`solve_left` split out
  of `solve`. The arm's centre line is kept out of the head (`_Arm.head_depth`, HEAD_MARGIN 0.6: the CI lint failures on makko and
  aerial_wheel). The solvers keep SOLVER_SPARE 0.45 px inside the check's margins; COLLIDE_WEIGHT 400; Yoru's mesh sampled with
  ~108 points (was 53 of 271). Test on quick_cut, makko, plunge, kesa_gryphon: all four pass.
- The backhand coils (gyaku_yoko, pivot_cut) asked for the blade pointing straight left from the belly: a 90-degree wrist, so the
  natural arm stood the blade upright (a salute, not a coil) through the left shoulder. Now `BACKHAND_COIL`/`PIVOT_COIL`: the fist
  at the left hip, the blade pointing back, edge up (the iai grip), the free hand forward (L_AIM). `clear_left` now starts from
  several poses (LEFT_STARTS), stays near the tick before (`prev`) and keeps the left arm clear of the sword in the in-between frame
  too (`mid`, at the beat's eased fraction). Both pass.
- Second full solve (killed at the 30-min background limit; run it with a 2-hour timeout): 28/65 failing, most just over the limits.
  Found and fixed in code (not yet fully re-solved):
  - The shoulder had no range limit: makko/aerial_wheel raised the sword overhead by swinging the arm BACKWARDS over the shoulder
    (pitch -21 -> 65 -> 198) and wound the track 360. `rig.shoulder_back` (SHOULDER_BACK 60 behind, SHOULDER_OVER 190), in the
    solver's strain and the check ("shoulder bent back").
  - Tracks ending wound (aerial_wheel's pitch 341, quick_cut's wrist (-101, 138, -131) = (79, 42, 49) on the other Euler branch):
    PlayerAnimator's fades (AbstractFadeModifier) and the stop blend the written numbers, so these spin the arm or the hand a full
    or half turn in game. `unwrap()` rewritten: every spelling of each keyframe tried (`_spellings`), the one whose in-between
    frames are natural (`_between_strain`) and near; each track starts on the plain spelling (`_canonical`); if it would end on the
    other branch it changes over where the yaw is near 90 (`_detour` smallest: there both spellings are the same turn); loops end
    exactly on their return beat. New check `wound()` in clearance (wound ends, spins between ticks with a detour > 40, loops).
  - refine also re-solves beats whose own tick is a little through something (2.5x weights).
- Yoru's back draw: no natural arm reaches its hilt behind the head exactly (misses ~3 px / 19 deg; the hilt sits at the arm's
  reach limit, 8.4 of 8.5 px; a more upright carry would stand the point half a block in the floor). So while the hand lifts it out
  of its ropes the game draws it on its own line, lifted (`BladePaths.backLift`), then glides it into the hand over two ticks
  (`BladePaths.backGlide`: samples every quarter tick of the sword's frame seen from the hand's, the turn eased straight from the
  carry's to the hand's at the arc's first beat; quaternion slerp in Python `slerp_zyx` and Java `SwordLayer.slerpZyx`, no new
  JOML references). Sheathe and charge draw the same. Python mirror: `back_phase`, `back_glide`, `yoru_layer`, `game_blade`.
  The draw re-timed: lift 10-18, the arc's first leg (67 deg off the back) eased over 20/22/24. densify now designs the swing
  between the ropes and the arc every tick (`_gripping`).
- Yoru's carry moved (rig BACK_GUARD/TILT/LEAN and Java BackCarry, SwordTests checks they agree): guard (-7.5, 0, 5.4), tilt 45,
  lean 4 (was (-4, -1, 6.4), 33, 8). The hilt sits out over the right shoulder beside the head, not behind it: the old cross guard
  turned into the head whenever the sword came off the back (2-3 px), the hand missed the hilt by 3 px / 17 deg (now 1.6 / 8), and
  the point touched the floor (now 0.2 blocks off). Its first arc leg takes the hilt up and out to the right before it comes forward.
  The ropes follow the constants (BackCarry.fromBlade); render Yoru on the back again to judge the look.
- Solver speed: the 8 general starts get 1200 evaluations each (the seed and refine's starts 5000); about 1.6x faster.
- WARNING for edits: a slice edit once duplicated ~300 lines (the end marker matched earlier in the file) and Python ran the old
  `yoru()`. After any slice edit check `grep "^def " tools/sword_anims.py | sort | uniq -d` is empty.

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
