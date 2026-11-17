# HexPiece: handoff after 2.3.0 (the boot crash fixed)

Read CLAUDE.md first. This note is the newest; next-ai-2.2.9.md covers 2.2.9 (the Swordsman's sounds from the web: the clips, the
beats, the cursor, the "HexPiece" environment), next-ai-2.2.8.md 2.2.7 (Gryphon and Yoru) and 2.2.8 (the slash's foam and ink),
next-ai-2.2.6.md the 3D inked lightning and the open requests in detail. The work branch is `claude/nice-bell-gam7qf`; the next
version is **2.3.1**.

## The owner (quoted)
- With a crash report (Forge 47.4.10, Android, Amethyst launcher; mods: geckolib, fdlib, architectury, playeranimator, ldlib, photon,
  pehkui, bendylib, libraryferret, citadel, hexpiece 2.2.9): "also the game crashes on boot."
- "add to the to-do list that every move is clashable (combat style wise, and yes you can take the impacts from HexGhoul's latest
  version, however I want a impact frame whenever a clash happens.)" (item 1 below).
- Then, on the same item: "also idk if I mentioned but to clash with swords man, any form of armament haki HAS TO BE ON, otherwise
  the sword cant be clashed and since it has more range, will come up on top, if haki is on, clash is possible. When 2 ryou hakis
  clash, both lightning colors explode outwards from the clash area in a sphere (similar to conquerors but smaller)."

## Released in 2.3.0: the crash on boot
- `IllegalArgumentException: Overlay already registered: hexhaki:hexpiece_combat` in `CombatMode$Overlay.register`, called through
  `com.hexhaki.piece.client.__Overlay_register_RegisterGuiOverlaysEvent`. Forge's EventBus 6 names the class it generates for each
  `@SubscribeEvent` method `<package>.__<class simple name>_<method>_<event simple name>`; a second method with the same name gets the
  class already made for the first. `AbilityHud.Overlay.register` (2.2.2) and `CombatMode.Overlay.register` (2.1.2) were both
  `client.__Overlay_register_RegisterGuiOverlaysEvent`, so AbilityHud's subscription ran CombatMode's method a second time. **Every
  build from 2.2.2 to 2.2.9 crashed on boot**: nothing of 2.2.2+ has ever run in game, and the owner's in-game remarks before (the
  missing katana icons) were from 2.2.1 or older.
- Fix: the handlers are `registerCombatOverlay`, `registerAbilityOverlay`, `registerFinisherOverlay`.
- `PieceVerify` check 5 (`handlerName`): every `@SubscribeEvent` method in the output jar (the original's and ours, 216 now) must get
  its own generated name, or the build fails naming both. Name new handlers after what they do, never a bare `register`.
- The naming rule is in Forge's EventBus source (`IEventListenerFactory.getUniqueName`, `ModLauncherFactory`: `PENDING.computeIfAbsent
  (name, k -> callback)` keeps the first method, `Class.forName(name)` hands its class to every later one). HexGhoul has the same bug
  (told to the owner, not changed: not this repository): six `Registration.renderers(RegisterRenderers)` in
  `control/client/render` (GhoulNpc, FleshThrall, Child, Human, Agent, Eto renderers: only one of them runs, six times) and two
  `Frame.after(RenderLevelStageEvent)` (GhoulNpcRenderer, EtoRenderer).
- The other start-up handlers were read again (key mappings, entity renderers, the two AddLayers in different packages, entity
  attributes, the sound and entity registers): nothing else registers twice. Past this point the game has not started since 2.2.1,
  so a later failure (world join, first render) is possible: ask the owner for the next crash report if there is one.

## Open: the owner's requests, in order
1. **Every move is clashable, with an impact frame on each clash** (the owner: "every move is clashable (combat style wise, and yes
   you can take the impacts from HexGhoul's latest version, however I want a impact frame whenever a clash happens.)"). Every
   fighting-style move (the Swordsman's cuts and slashes, Iron Fist, Fishman Karate, Six Powers; player against player and against
   NPC fighters) meets an opposing move landing at the same moment: both stopped and thrown back, an impact frame plays.
   - **Against a Swordsman's blade**: only with some form of Armament Haki on (Armament, Ryou, Gear's...; `HakiData.flag(player,
     "armamentOn")` is how SwordServer reads it). Without it the blade cannot be clashed: the cut goes through, and with its longer
     reach the sword wins.
   - **Ryou meets Ryou**: both fighters' lightning colours (`RyouServer.color`) burst outward from the clash point in a sphere, like
     the Conqueror's full release (`haki/ConquerorBolts`, the storm) but smaller; the 3D inked bolts (`BoltMesh`) in both colours.
   HexGhoul (read only; `git clone --depth 1 --branch claude/eager-ritchie-xni1ms https://github.com/HexB1tDev/hexghoul`, public, no
   add_repo needed; its newest commit when read: "HexKagune 4.3.0") has it: `control/pale/MoveClashRules` (pure: families of moves
   that cancel each other, `window`, `reach`, `power` never below `BIG` 2.4, `STAGGER` 14 ticks), `control/server/MoveClash` (moves
   let go register with `begun`; each level tick one on its way that finds its target, within reach and facing it, doing the same
   family back cancels both: `KaguneClash.collide` (both thrown apart by the gap between their blows, `ClashRules.thrown/lifted`,
   overwhelmed: sent flying, the wind, the ground shaking), both stunned), `control/ClashRules`, tests `ClashTests`. Its impact
   visuals: `client/fx/KickDust` ("at the blow: a blinding flash on its middle, rays of light bursting out every way from it (the
   Kizaru frame), rings of..."): read these for the impact frame. HexPiece's side: `StyleCombat`, `StyleMove`, `SwordServer.sweep`
   (cut windows), `SwordMove.Cut`, `Impact`. `piece.sword.clash` (CC0, 5 clips) is registered and waiting for it.
2. **Galaxy Fist becomes Iron Fist's** ("Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU haki. ONLY
   for iron fist."): next-ai-2.2.6.md item 2.
3. **Fishman Karate rework**: next-ai-2.2.6.md item 3. Water Prison's 5 s stun: breakable unless the owner says otherwise.
4. **Six Powers**: faster 3D air, Haki finger pistol shots (next-ai-2.2.6.md item 4).

## Not verified (only the owner can test in game)
- That 2.3.0 starts (the crash is understood and the check proves the collision is gone, but nothing past it has run since 2.2.1).
- Everything of 2.2.2 to 2.2.9 in game (see their handoffs).
