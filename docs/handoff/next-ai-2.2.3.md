# HexPiece: handoff after 2.2.3

Read CLAUDE.md first. This note is the newest; next-ai-2.2.1.md covers 2.1.9 to 2.2.2 in detail (the finisher taken from HexGhoul and
turned upright, the slow draw and sheathe, the abilities panel), next-ai-2.1.5.md the 3D Haki lightning, next-ai-2.1.4.md the
Swordsman. The next version is 2.2.4. Every version from 2.1.4 to 2.2.3 was released with both workflows green; nothing has ever been
tested in game (only the owner can).

## Released this session
- 2.1.9 whole sheaths, light sword swings, Haki on the hands only; 2.2.0 the Haki slash 64 tall at full charge.
- 2.2.1 HexGhoul's Steel Blade finisher with a vertical cut (`sword/Ragdoll`, `CutPose`, `Carcass`, `Finisher`, `server/Finishers`,
  `client/sword/FinisherFx`; renders `docs/images/finisher-upright.png`, `finisher-insides.png`); the slow draw (`sword_draw`, 22 ticks,
  `SwordServer.drawFirst`: nothing usable until drawn; the quick draw unchanged) and slow sheathe (20 ticks).
- 2.2.2 one abilities panel on the left, HexGhoul's (`ui/AbilityPanel`, `ui/AbilityHudView`, `client/AbilityHud`; the original's
  `HakiHud`, `FruitHud`, `GearHud` return first via `PiecePatch.HUDS_REPLACED`; render `docs/images/abilities-hud.png`).
- 2.2.3 the panel regrouped (the owner: "every combat style should be together with the haki. Like, there should be both in the same
  menu, same category. The keys should be usable just like that in any form. Like, the only menu that is separate is the devil fruit
  powers"): `AbilityPanel.of` gives the style + Haki panel whenever the fruit's own mode is off (style lines dim with combat mode off),
  and the fruit's panel alone (no Haki lines) in its mode. `HudTests`.

## Open: the owner's requests, in order
1. **The creation screen's figures** (the owner: "the models of the female and the player are completely fucking horrible ... you can
   tell that the female body is, like, broken in the image ... Just make them, like, black silhouettes. You don't have to be adding
   details to it"). `ui/CreationView.genders` draws `PirateUi.figure(...)` (blocky rectangles + `c.face`, the player's face) on two
   cards. Replace with plain black silhouettes (near-black fill, no face, no clothes): a man (round head, neck, broad shoulders, torso
   tapering to the waist, arms hanging, legs) and a woman (head with long hair to the shoulders, narrower shoulders, an hourglass waist,
   wider hips, slim arms, legs), built from smooth polygons with `UiCanvas.polygon` (scanline fill; many points for curves). Drop the
   `c.face` call. Check `LogbookView` (posters use `c.face`) and anywhere else `PirateUi.figure` is used. Render with
   `tools/preview/UiPreview.java` (creation-0.png) and judge it.
2. **"The keys should be usable just like that in any form"**: Haki's keys (Ctrl + R Armament, V Observation, the Conqueror's quick slots
   G/K/L/M) should work whatever style or mode. Check in code that combat mode or a style never swallows them (`client/StyleInput`,
   `CombatMode`, `PieceHooks.fruitKeysActive`, the original's `BindsClient`/`QuickKeys`); in the Gomu fruit's Devil Fruit mode the quick
   slot keys are Gear 5's (`GearKeys.tick`), which is the fruit's own panel, so that is expected.
3. **One menu** (the owner: "there's two different types of GUI ... the mastery menu, and then there's another one ... put them all on
   the same thing ... check the fruit mastery GUI. It's still old. ... reorganize the mod as best you can"; and now: style and Haki in
   the same category, the devil fruit its own). The original's screens: `com/hexhaki/binds/client/AbilityBook` (Ability Book: Haki /
   Devil Fruit chapters, quick-slot binding), `com/hexhaki/client/screen/HakiMasteryScreen` (Haki techniques), the fruit mastery screen
   (look under `com/hexhaki/fruit/client` and `com/hexhaki/client/devilfruit/screen`), and ours, `client/LogbookScreen` +
   `ui/LogbookView` (the Captain's Log, opened by `HakiKeys.MASTERY`). Make one menu in our style (the Captain's Log) with a
   "Style & Haki" section (the style's moves and mastery, Haki techniques and their quick slots) and a "Devil Fruit" section (its moves,
   mastery, binding), and route the original's screens to it (PiecePatch call redirects where they are opened).
4. **The Haki slash as a thick energy wave** (the owner: "it's not about just being tall. It's meant to be like an energy wave ... see
   Mihawk's energy projectile from his blade. It is much bigger than that, and it's thick"). Now `client/sword/SlashRenderer.crescent`
   draws two crossed flat bands (`edge`). Plan: a lofted volume along the crescent's spine (u 0..1 tip to tip, the middle bowed forward
   ~0.28 of its height), its cross-section a lens/ellipse (forward radius ~0.09 h, sideways ~0.045 h at the middle, tapering to points
   at the tips), drawn as a dark Haki core shell plus two or three colour glow shells in `RenderType.debugQuads` (alpha blended: never
   additive "eyes", the owner's setup draws those opaque), trailing ghosts as now; the crackle along its edge stays. Server: widen the
   cut and the hits to match (`SwordRules.slashThickness`, max 1.8 now; `HakiSlashes.queueCells` samples across `thick`), keeping the
   per-tick budgets. Render it with `tools/preview/BoltPreview.java` (the `bigslash` sheet) and judge it.

## Not verified (only the owner can test in game)
- The finisher in game (the pose read off the model under PlayerAnimator, other players' skins, blood pools, performance with several
  corpses), the slow draw's feel, the abilities panel's key names and placement over the chat, the fruit kits' own gauges beside it,
  Gear 5's lines.
