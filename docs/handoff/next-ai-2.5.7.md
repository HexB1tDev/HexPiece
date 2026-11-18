# HexPiece: handoff after 2.5.7 (2.5.7: the crash on joining a world fixed; 2.5.6: the first-person draw and sheathing, the blades' temper)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.5.md (the white screen, the free head, the holster view, the
slash's cooldown), next-ai-2.5.3.md (Ryou's fist aura: the owner calls it "beautiful and perfect": keep it), next-ai-2.5.2.md,
next-ai-2.5.1.md (**the owner's device**: Android, Amethyst, MobileGlues = OpenGL ES).

Branch `claude/nice-bell-gam7qf`. **2.5.7 is released** (if the Release run is green: check). **The next version is 2.5.8**
(`python3 tools/next_version.py --write`). Nothing has ever been tested in game except by the owner.

## The owner (session 8, after 2.5.5, verbatim)
- "the swords are not satisfying at all, the pull out animation is horrid, if you can even call it that, the pull out animation needs
  to be really gorgeous to look at, sheath and unsheath both" then "I mean the first person"
- "also improve the temper on all swords but yoru, the blade is too bland not good looking at all, simply improve the texture thats
  it, same for Gryphon"
- (The chat was cleared by accident once; the work was on disk and a WIP commit was pushed.)

## Done (2.5.7): the crash on joining any world (2.5.5 and 2.5.6 had it)
- The owner sent a crash report (2.5.6, Windows, NVIDIA): `NullPointerException: Cannot invoke "String.isEmpty()" because
  "com.hexhaki.fx.client.ImpactClient.title" is null` at `ImpactClient.drawTitle` from `hud`; then "the crash happens in new worlds
  too". Cause: 2.5.5's `client/fx/ImpactReset.reset` (run whenever the client level changes: every world join) set `title` to null;
  the original's static initializer starts it as "" and drawTitle calls `title.isEmpty()` every frame.
- Fix: reset sets every field to the original's own starting value: `title` and `comboMove` "", `comboAt` and `titleAt`
  Long.MIN_VALUE, the rest 0; `set` refuses null. build.py `check_impact_fields` now reads the original's `static {}` (javap) and fails
  unless every `set("name", value)` in ImpactReset.java is that field's starting value (and none starts null); checked to catch the
  old `set("title", null)`. `pushed` (a per-frame flag of shudderBegin/End) is deliberately not reset.
- Lesson: when clearing another class's state by reflection, restore its initializer's values, never null/0 by assumption.

## Done (2.5.6): the first-person draw, held and sheathing
- `sword/DrawView` (pure; `Pose(saya, blade, right, left, shine)` as step lists, `Frame(p, q)` = position + quaternion, keyed and eased;
  `ROTATE = 4` step = angle about any axis; `HolsterView.step` multiplies it out by Rodrigues). `client/sword/HolsterHand` picks the
  pose by the local player's anim: DRAW -> `draw` / `backDraw` (Yoru), with `BladePaths.drawTick(name)` (the server's tick the point
  clears the mouth); SHEATHE -> `sheathe` / `backSheathe` with `BladePaths.sheathTick`; drawn (`SwordAnims.drawn`) -> `held`; Yoru on
  the back -> nothing (hands free); else `holster` (2.5.4) with the Koiguchi push. Swings stay playerAnimator's. It cancels
  RenderHandEvent and draws the saya, the blade (ArmamentItemBuffers.black under Armament, never the saya), the right arm
  (`PlayerRenderer.renderRightHand` m_117770_, new stub + allowlist [srg] evidence) and the left (m_117813_, only with the off hand empty).
- Draw timeline (ticks, `warp` aligns them to the server's drawTick): GRIP_AT 6 (right hand on the handle), PUSH_AT 9 (koiguchi glint),
  HOLD_AT 11, the slide with saya-biki (SAYA_BIKI 9 degrees, SAYA_PULL 7 px) until CLEAR_AT 19.5 = the point at the mouth
  (`clear(katana) = tip + 0.6`), FREE 20, then the whip: `along` (even pace by turn and distance) through exit -> OUT -> CROSS -> WHIP ->
  HELD with "outcubic", DRAW_END 28. Sheathe: FLICK_AT 2.6 (chiburi, outcubic), FLICK_HOLD 6, LAY_AT 10 (the back at the mouth),
  REST_AT 12, the slow slide DEEP_AT 18, HOME_AT 21 (aligned to the sheathTick), SHEATHE_END 24.
- The right arm runs from R_SHOULDER {0.50,-1.0,-0.38} to the fist, the blade along it (the first try locked the arm to the blade and
  it swung through the camera).
- SwordTests.drawView: continuity (no jump: guard < 0.4, tip < 1.0 per quarter tick), the fist on the handle, everything in front of
  the eye, the point at the current mouth at the drawTick (Gryphon's curve: tolerance 0.26), home at the sheathTick.
- `tools/draw_preview.py OUT_DIR [katana] [--step 1.0] [--slim]` renders the game's own frames (tools/preview/DrawDump.java).

## Done (2.5.6): the temper
- `tools/katana_temper.py` (numpy + PIL; run locally, results committed): per blade (25 katanas + Gryphon; Yoru untouched) it
  rasterises the owner's blade triangles into UV space (position, normal per texel), finds the flat faces (|nz| > 0.55), "across" per
  texture row (0 at the edge, flipped by the mesh's geometry), and the owner's hamon line: the first thin bright stroke from the edge
  (a 2D ridge on luminance, thin in any of 4 directions at 2 or 3 texels, > 0.35 x the 99th percentile; at across 0.035 - 0.66),
  outliers against a 7-row median dropped. No continuous line (Gryphon: p90 row step 0.19 vs <= 0.02 on the katanas) -> irregular
  gunome. Paint: body = the owner's rgb x polish x grain x hada (folded-steel lines, sin(across*20 + warp)); the hardened edge a misty
  frost of the edge's tint (15% of the owner's kept); a sigmoid boundary (0.010), a glow (0.018), nie specks, ashi streaks; the edge
  bevel brightened.
- Output `textures/tempered/<key>.png` + `manifest.json` (key -> SHA-256 of the owner's atlas). build.py `tempered()` packs them in
  place of the atlases (katana_assets, legend_assets for Gryphon) and fails if a source atlas changed ("run tools/katana_temper.py").
  The atlases grew by about 2 MB in all (noisier PNGs; the jar is 30.7 MB).
- Judged: all 26 traced against the owner's outlines (overlay of the detected line on every original), before/after strips, 3D
  close-ups of Blackwater, Crimson Dawn, Midnight Lotus, Gryphon. Review scripts were scratch (not in the repo); the overlay is easy
  to rebuild from `hamon_line`.
- Not done: the inventory icons (tools/katana_render.py icons) still show the old blades; at icon size the temper hardly shows. Redo
  them if the owner asks.

## Open
1. The owner's report on 2.5.7 (no crash on joining a world) and 2.5.6: the first-person draw / held / sheathe (only judged in
   renders), the temper.
2. From 2.5.4 / 2.5.5: the free head, the holster view, the slash's 30 s cooldown, the white screen (confirm it is gone).
3. Waiting on the owner from 2.4.7 - 2.5.2 (next-ai-2.5.2.md).

## Tools
- `tools/draw_preview.py` (2.5.6), `tools/katana_temper.py [--preview DIR]` (2.5.6), `tools/holster_preview.py` (2.5.4),
  `tools/ryou_preview.py` (2.5.3), `tools/sword_check.py`, `tools/srg_shapes.py <joined.tsrg>` after adding SRG references.
