# HexPiece: handoff after 2.2.4

Read CLAUDE.md first. This note is the newest; next-ai-2.2.3.md and next-ai-2.2.1.md cover 2.1.9 to 2.2.3 (the finisher, the slow
draw, the abilities panel), next-ai-2.1.5.md the 3D Haki lightning, next-ai-2.1.4.md the Swordsman. The next version is 2.2.5. The
work branch is now `claude/cool-einstein-uhqsh9` (2.2.3 was on `claude/friendly-allen-g9nrzx`, same history). Nothing has ever been
tested in game (only the owner can).

## Released in 2.2.4 (all four requests of 2.2.3's list, plus two the owner sent while it was built)
- **Silhouettes** on the creation screen: `ui/Silhouette` (each body's right half as control points in head units, mirrored, joined by
  a centripetal Catmull-Rom spline, sharp only at marked points), drawn by the new `UiCanvas.shape` (any simple polygon, even-odd, in
  quarter-pixel rows merged into runs) inside `UiCanvas.batch` (`GuiCanvas`: `drawManaged`, one flush). `PirateUi.figure` is gone.
  Renders: `docs/images/start-menu-1-gender.png` (GUI scale 3), `start-menu-silhouettes.png`.
- **Haki keys in every form**: the original's "powers off" (`Binds.off`, toggled by X for anyone without the Gomu fruit) also stopped
  every Haki key (`HakiKeys$ForgeBus.tick`: toggles, quick slots; `movementInput`: the leap) and every Haki action on the server
  (`HakiServerController.onAction`). `PiecePatch.CALL_REDIRECTS` sends those three calls to `PieceHooks.hakiKeysOff` /
  `RyouServer.hakiOff` (false). The fruit still reads the switch (`FruitClient.active`). Nothing of ours swallows Haki keys; in the Gomu
  fruit's Devil Fruit mode `GearKeys.tick` gives the Haki keys to Gear 5's moves (the original's, kept: the fruit's own panel).
- **One menu**: `ui/LogbookView` (pure: Overview, Style & Haki, Devil Fruit; `Section`/`Entry`/`Row`, a scrolling list, a detail pane,
  the 3x3 quick slots, Reset Slots / Respec / Close, `at` hit tests), `client/LogbookData` (reads the original: `HakiMasteryScreen`'s
  public `mastery`/`unlocked`/`requirement`, `HakiTechnique.forType`, `BindsClient` slots and names, `ClientKits` moves,
  `GearClientState` abilities and upgrades; the Ability Book's quick ids: CONQ_AWAKENING 100, CONQ_DOMINION 101, CONQ_SOVEREIGN 102,
  ARM_GALAXY_WAVE 103; permanent keys: Reinforcement and Ryou on (Ctrl+)R, Presence V, Leap Shift+Space, Blade Cut Swing),
  `client/LogbookScreen` (binds with `BindsClient.bind`, a quick slot's own key binds too, `BindsClient.reset`, buys with the
  original's `C2SGearUpgrade(index, false)`, respec `(0, true)`, the original's click sound), `client/MenuSwap` (the Ability Book ->
  Overview, Haki Techniques -> Style & Haki, Gear mastery -> Devil Fruit). Tests `MenuTests`; renders `docs/images/one-menu.png`,
  `one-menu-small.png`, `captains-log-swordsman.png`.
- **The Haki slash as the manga's energy wave** (the owner sent a panel: a turquoise wave tearing up out of the ground, its back torn
  into spikes, "its not meant to be a smooth thing, it's meant to be like this"): `sword/SlashMesh` (pure, shared by the game and the
  preview): a fin rising out of the trench (`Frame.front/back/across`: `SwordRules.SLASH_FRONT` 0.18, `SLASH_BACK` 0.48 of the height,
  `SLASH_SIDE` 0.07 each side in the ground, `slashProfile` (1-v)^0.6 up to the peak), 22 back tongues (blades of flame: thin across,
  broad in the plane; low ones short and in the plane, upper ones fanning out; 40% long; flickering), 3 crest tongues; a flat-shaded
  body, whiter streak ribbons, an ink hull (the far side of a pushed-out copy) in the solid batch (`RenderType.textBackground`, wound
  to face the eye; quads whose two triangles disagree are split), a glow shell in the translucent batch (`debugQuads`, sorted far to
  near: `Quads.sortBackToFront`). It grows its back in over its first blocks, sinks and fades over 3 ticks at its range. The crackle
  runs up the leading edge (`SlashMesh.edge`). Server: `HakiSlashes.queueCells` cuts each row as wide as the body there
  (`SwordRules.slashThicknessAt`), LINGER 120 ticks (same per-tick budgets). Render `docs/images/haki-slash-wave.png`; tests
  `SwordTests.wave` (every solid triangle faces the eye from any side, body width = trench width at every height).
  Lessons from the renders: translucent layers in one depth-writing batch punch holes in each other (keep the opaque core in its own
  batch, few well separated glow layers); a lofted curve's quads twist near tips (split them).
- **Every Haki lightning in the Ryou colour** (the owner: "make sure it matches the color of RYOU always, same for the conquerors
  haki releases, all lightnings in the game are to reflect the same color as RYOU"): `RyouServer.ensure` gives every player their
  colour at login (it was at the Ryou unlock), still once and for good; `HakiBolts.launch` tints every painted stroke to the owner's
  colour (`Bolt.tinted`: BODY stays black, CORE the colour, HEART whitened, AURA as faint as it was; the owner is the entity it plays
  on, or the nearest player to a fixed point; non-players pale steel), the Conqueror's shockwave ring too; `HakiDraw.color` is the
  Ryou colour always; the slash already sent it. Not recoloured: the original's Photon beams in a composition played some other way
  (the Galaxy Fist charge keeps them), and the original's non-lightning Photon effects.

## Open: the owner's requests, in order (sent during 2.2.4, not started)
1. **JoyBoy for everyone** (the owner: "the hexpiece joyboy on/off, is not a thing anymore, anyone by default at max charge shall be
   able to do that. (keep and override the default max conqueror with the joyboy one since it's better.)"). The original has a JoyBoy
   flag (`HakiClientState.joyBoy`, `HakiTechnique.CONQ_JOYBOY`, the server's toggle; look for it in `HakiServerController`, the
   commands `HakiCommands`, and `HakiVfx.conquerorRelease`'s variant). Make the full-charge Conqueror release always the JoyBoy one,
   remove the on/off (its key/command/technique entry: the menu lists `CONQ_JOYBOY` from `HakiTechnique.forType`).
2. **The Conqueror's release drawn like the owner's two frames** (anime stills: from a small point where the player stands, enormous
   black lightning bolts, thick jagged ribbons with a glowing red rim, shoot out in every direction across the sky, a bright pink-white
   flash at the centre). "Like this in all directions." In the Ryou colour (request above). Our current storm: `haki/ConquerorStorm`
   (8..13 huge sphere-direction bolts at full charge), drawn by `BoltMesh` (ink outline, body, core, glow). The frames' bolts are much
   THICKER (black ribbons several blocks wide near the player, tapering), very jagged, few, with a strong coloured rim glow, plus a big
   white-pink flash/sphere at the centre. Render with `tools/preview/BoltPreview.java` (`bolt-storm.png`, `bolt-hero-burst.png`).
3. **Fissures that repair themselves** (the owner: "At full charge, it should cause severe tears on the floor like fissures where the
   lightnings hit, then repair themselves automaticly (same as HexGhoul)"). HexGhoul (HexB1tDev/HexGhoul, branch
   claude/eager-ritchie-xni1ms) has a self-repairing terrain effect: find it there (look for "repair", "restore", "crater", "fissure")
   and port it: on the server, where the full-charge release's ground bolts land (`HakiBolts.onGround` lays them on terrain client
   side; the server has `ConquerorBolts`/`ConquerorLightningPath` stun paths), open jagged fissures (blocks removed or swapped, saved
   with their states), then put every block back after a while; never destroy block entities, respect protection (BreakEvent) and
   mobGriefing as `HakiSlashes` does.

## Not verified (only the owner can test in game)
- The menu: the original's data read live (technique requirements, quick ids, kit moves), key binding by key press, the Gear upgrade
  packets, the click sound; the silhouettes at the owner's GUI scale.
- The wave: its look under real lighting and fog (the solid batch is unlit and fogged like the lightning's ink), performance (about
  3.9k quads a slash a frame), the trench's width and how long it keeps cutting.
- The Ryou colours: every lightning effect launched through `HakiBolts` picking the right owner (the original's `playAtWorld`
  passes an entity: assumed to be the caster).
