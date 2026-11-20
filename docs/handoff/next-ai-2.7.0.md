# HexPiece: handoff after 2.7.0 (the Uo Uo no Mi with the owner's dragon: Z transforms, flies as creative, transforms back; the left hand under Haki fixed)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.6.2.md (the katanas 1.25 bigger, the first-person draw and
sheathing, the Haki coat on blades, the Conqueror's sounds and stun), next-ai-2.5.9.md, next-ai-2.5.3.md (Ryou's fist aura:
"beautiful and perfect": keep it), next-ai-2.5.1.md (**the owner's device: Android phone**, Amethyst, MobileGlues).

Branch `claude/nice-bell-gam7qf`. **2.7.2 is pushed (check its "Build HexPiece" and "Release HexPiece" runs first; 2.7.0's were green, 2.7.1's were
running). The next version is 2.7.3: the dragon's locks and the Transformed effect (the owner's item 8), then the lag and size pass.**

## The owner (session 10, verbatim)
1. "Current bugs: Haki when holding a katana makes left hand invisible." (fixed, 2.7.0)
2. "Smooth out the mod to avoid lag and overall reduce size (if possible)." (**NOT DONE: next**, see below)
3. "Add a new fruit called Uo Uo No Mi, make Z a button to transform into the model I sent, animate the model, add a transformation
   animation, flying animation and de-transform animation." (done, 2.7.0)
4. During it: "it should be at least 6 times bigger than the player"; "that is way too small"; "its too thin and too small"; (after a
   girth reshape) "god what a mess, cant you just reapply the whole model and just resize it? this looks more and more deformed" (so:
   the model is the owner's EXACTLY, uniformly scaled, never reshaped); "the face is literally not on the head" (fixed: see the
   converter); "make sure it's perfectly controllable, no fuck ups, it should be as smooth as flying in creative"; "Also add some cloud
   particles in a large area around the player when transforming / de-transforming"; "careful with the cape, the body cant clip
   through" (fixed, tested).
5. **NEXT (verbatim), "After the dragon transformation is done, and pushed. Dragon New Moves:**
   Untransformed:
   x - Air Pressure: The user points right hand forward and beautiful air blasts (can be taken from the Six Powers) shoots from it
   dealing damage + knockback. (25 blocks max range).
   C - Blast breath: The user shoots a fire blast from his mouth (a sort of line that travels fast and explodes in the end) causes burn,
   knockpack and damage.(35 blocks max range)
   V - Wind Force: Swipe your arm forward hard, forms a tornado that drags any and all entities forward along with it, flying and
   twisting around along it. (greenish tornado). (25 blocks max range)
   B - Dragon Embers: Shoot multiple fireballs from your mouth that scatter up into the sky and then rain down on any entity in the
   area. (15 blocks max range).
   Transformed:
   M1 (Held): Open your massive jaw and unleash a fire breath as long as M1 is held. (35 blocks range)
   X: Blast Breath: Open your mouth, a fire ball charges in it and the longer you hold the Key the bigger it gets, then unleash a massive
   fire blast. (45-100) Blocks Range)
   C: Gigantic Bite: lunge forward and bite your target.
   New Feature, Dragons can come in different colors, other than blue. (always random)
   (Use minecraft particles and some Photon (Only for sparks etc) fire is made of a colored Cloud particles and smoke, etc (minecraft
   particles).
   NOTE: Add the moves to the Fruit ability hud and to the fruit progression menu."

6. (after 2.7.0's renders) "In the next version make sure the cape also changes colors, the fur as well (as long as it matches and
   blends well to look beautiful with the scales color), from the render I gotta say Im impressed, good job."
7. **The new key layout (the owner, asked about X = Ability Book and V = Observation clashing with the moves):** "All keys of the mod
   (but haki) will now be the following Z, X, C, V, B, N, M. R For switching modes / Haki." Then chose: **Conqueror stays G;
   Observation V -> J; the Conqueror's quick abilities on K, L, H (M -> H); the Ability Book X -> O; the style specials Y/U -> Z/X in
   combat mode; Z X C V B N M are the fruit's moves in fruit mode** (R switches modes, Ctrl + R Armament as now). So the Uo moves: X Air
   Pressure, C Blast Breath, V Wind Force, B Dragon Embers (human), Z Dragon Form, and in dragon form M1, X, C. The original's quick
   slots are 9 KeyMappings (BindsClient.keys(), defaults G K L M B Z C N H, Binds.DEFAULT_KEYS / HAKI_KEYS G K L M) and HakiKeys
   (observation V 86, conqueror G 71, mode H 72, acoc J 74, dominion K, strike L, convergence M, mastery P, loadout X 88): the new
   defaults go through PiecePatch (their INTS/TEXTS) and HexPiece's PieceKeys; check Binds.slotOf's key regex and every place the HUD,
   the Ability Book text and the Captain's Log name a key.

8. **AFTER the moves (verbatim):** "This is the next thing after the moves are done: Uo Uo, you cannot switch from fruit mode to any
   mode or use any form of haki, ONLY use what the transformation abilities allow, nothing else, plus you gain a new universal effect
   called Transformed, doubles your HP and you get resistance as well. This effect will also apply to Nika (the fruit)." (So: while
   the dragon is out, R does nothing, no Haki key or Haki ability works (Armament, Observation, Conqueror and its quick abilities,
   Ryou), only the dragon's moves; a new MobEffect "Transformed" (registered by HexPiece: max health x2 through an attribute modifier,
   plus Resistance) while the dragon is out AND while Nika's Gear 5 is on (GearClientState.gearFive / the server's GearData).)

## Done (2.7.0): the Uo Uo no Mi
- **The fruit** is a constant of the ORIGINAL's fruit enum: `PiecePatch.uoFruit` adds `Fruit.UO` (ordinal 4: id "uo", "Uo Uo no Mi",
  element "Dragon", colour 4FB7E8, lore) to `<clinit>` and `$values()`. So the original registers its item `hexhaki:uo_uo_no_mi`
  (Fruits.<clinit> loops Fruit.values(); recipe "<id>_<id>_no_mi"), its creative tab, eating, saving, binds, HUD. Item model and icon
  in src/main/resources (icon: tools/uo_icon.py, the original fruits' style), lang key added.
- **The kit:** `server/UoKit extends FruitKit` (registered in HexPiece.init: `Fruits.register(Fruit.UO, UoKit::new)`): one move,
  `FruitMove("Dragon Form", "[Z]", ...)`. A move's key "[Z]" puts it on the original's quick slot 5 (Binds.DEFAULT_KEYS G K L M B Z C N
  H); QuickKeys sends a press as `FruitKeys.send(0, ability, flags)` -> server `FruitServer.action(player, 0, ability, flags)` ->
  `kit.action(player, 0 (press) / 1 (release), ability (= the slot), flags)`. Quick keys work in combat mode too (HexPiece's R only
  swaps M1/right click). The client kit `client/dragon/UoClient` is put into the original's `ClientKits.KITS` (PiecePatch widens that
  private field to public) on the first client tick. **For the new moves:** give the kit more FruitMoves with keys "[X]", "[C]",
  "[V]", "[B]" (X is not among the original's 9 quick keys: check Binds.slotOf / BindsClient before choosing; C N H are slots 6-8, B
  slot 4) and handle kind 0/1 (press/release) in UoKit.action; M1 in dragon form is FruitKeys.tick's primary (LightKit.action kind 2
  = primary(player, flags), 3 = use, 4 = useRelease: see the original's LightKit.action tableswitch).
- **The model:** input/kaido_dragon_form.zip (the owner's: one binary FBX 7.3, 92-bone rig + 6 skinned meshes body / coat (the purple
  cloud mantle) / face_normal / hair / l_fire / r_fire, 27 takes, a 2048 JPG). `tools/fbx_reader.py` (binary FBX reader),
  `tools/dragon_mesh.py` (packs `assets/hexhaki/dragon/seiryu.sry`, gzip big-endian, format in its docstring, and the texture at 1024
  to `assets/hexhaki/textures/piece/dragon/seiryu.png`); build.py `dragon_assets()` (SHA-256 checked, cached in build/dragon-cache
  by the zip's and the tool's hashes). **The clusters' Transform matrices are NOT the meshes' binds in this file** (each is the
  inverse of the bone the mesh hung from in the source game): applying them slid the face 8.5 units down the neck ("the face is
  literally not on the head"); the points are already in bind space (identity). Takes kept: idle (idle_a, 3 s loop), form
  (metamorphosis_a, 2.2 s: bursts up out of the ground, arcs, coils into the hover), unform (metamorphosis_b, 1.5 s: uncoils, dives
  head first), stun, down (unused); each aligned on the hover's pelvis height (ANCHOR). skill_d / skill_e (25-27 s takes) are in the
  FBX, not packed: look at them for the moves (a breath? a bite?) before animating procedurally.
- **dragon/DragonModel** (reads the file), **DragonPose** (rest + takes blended (nlerp), `bend`, `solve`, linear-blend skinning, no
  allocation per frame), **DragonForm** (pure: phases HUMAN/FORMING/DRAGON/UNFORMING, SCALE 1 block a unit, HEAD_HEIGHT 6 (the hover's
  claws at the feet), the transformation grown out of the player's chest (SMALLEST 0.015 -> 1 over GROW 0.7 s, the take's path
  shifted under the player then released over LEAVE 1.2 s; its end settled onto the idle's first frame over SETTLE 0.45 s), the way
  back shrunk into the chest over its last SHRINK 0.75 s and steered into the player's column; flight = the spine eased to the
  straight bind with a travelling wave (WAVE_PERIOD 1.6 s, 11 segments), the mantle eased to its own bind (it clipped through the body
  with the hover's angles), pitched with the look x flight, banked from the turn; `placement` is the model -> player matrix.
- **The flight is the game's creative flight** (server/DragonServer: mayfly + flying at FLY_SPEED 0.07 (creative 0.05), given back as
  it was when the dragon goes (creative/spectator keep it), on death, logout, dimension change, and after a crash (a mark in the
  player's persistent data, `HexPieceDragonFlight`); LivingFallEvent cancelled while out and 10 s after). The client lifts the local
  player for the first 12 ticks of the transformation (0.42 blocks/tick up, flying on).
- **client/dragon/DragonClient**: per-player state from `S2CDragon` (message 16, PROTOCOL 10), the human hidden (RenderPlayerEvent.Pre)
  and the hands (RenderHandEvent) from HUMAN_GONE_AT 0.15 s to HUMAN_BACK_AT 1.3 s, the camera to third person (and back), cloud
  bursts and a wide cloud field (3-12 CLOUD particles a tick, 3-14 blocks out, swirling) for the first 40 ticks of the transformation and
  the last 26 of the way back, the flight blend eased (FLIGHT_EASE 0.07/tick: the 65-block body uncoils in about a second), the yaw
  eased toward the look, the bank. Drawn at RenderLevelStageEvent AFTER_ENTITIES for every dragon (never culled with the small player
  box), not for the local player in first person. **client/dragon/DragonRenderer**: CPU skinning, one bulk `VertexConsumer.m_5954_`
  call a vertex (allowlisted), entityCutoutNoCull for the body, eyes (additive) for the flames, light from LevelRenderer.getLightColor at
  the head. **client/dragon/DragonCamera**: PiecePatch redirects the original's CameraMixin (`hexhaki$cinematicOffset`, at Camera.setup's
  TAIL) call to `CinematicController.kingsGripCameraPosition` here: the original's place if any, else in dragon form BACK 26 / UP 11
  behind and over the eye, clipped against blocks, gliding in and out with the form.
- Sounds: the original's own (Fruits.sound "whoosh_heavy" deep for the rise and the dive, "slam" as it goes in). No new sound files.
- Tests: `DragonTests` (from the built jar): the hover >= 6 x 1.8 tall, claws never under the feet, no jump between the takes and the
  hover (form end -> hover < 0.6, hover -> way back < 0.02, the idle loop), smooth over time, the flight blend never jumping, out of
  and back into the chest, Minecraft's facing, **the mantle never inside the body in flight** (the owner's rule).
- Previews: `javac -cp build/classes -d build/preview tools/preview/DragonPreview.java` then `java -cp build/preview DragonPreview
  <sry> <png> <out.png | dir/> shots...` (shot kinds: `clip:sec:yaw:pitch:dist[:tx:ty:tz]`, `g:phase:sec:flight:look:bank:yaw:pitch:
  dist[:tx:ty:tz[:groundY]]` as placed in game, `v:phase:sec:flight:look:bank[:groundY]` from the game's camera); `python3
  tools/dragon_gif.py out.gif`. The model files are in build/dragon-cache/*/ after a build.

## Done (2.7.0): the left hand invisible under Haki with a katana
- The original's `GearJacketHaki.renderCoating(..., boolean)` last flag is "first person" (0 from HakiHandLayer.render for both arms, 1
  from renderFirstPerson), and RyouHands.coating/coatingFirstPerson took it as "right": every first-person LEFT arm under Armament (the
  hand on the holstered sword, at the saya's mouth: client/sword/HolsterHand) drew the RIGHT arm in its place (10 px aside, out of
  view). `RyouHands.right(player, arm)`: the arm is the renderer model's leftArm or not. Third person's coat had the left side's centre
  and bend for both arms too (only drawn where HakiSkin could not blacken the skin).

## Done (2.7.1, pushed: check its runs)
- **Done: random colours** (the owner's items 5 and 6): `dragon/DragonColours` (pure): 10 palettes (Azure = the owner's, Crimson,
  Emerald, Gold, Violet, Obsidian, Pearl, Inferno, Jade, Rose), each recolouring the owner's texture per pixel: the blues (scales,
  198 deg) by hue band x saturation x chroma, the purples (cape and fur, 268), the golds of the upper half (belly, horns, 45), each to
  the palette's hue / saturation x / value x; the black mane, eyes, teeth, claws and fire untouched. The server picks one at random at
  each transformation (S2CDragon now carries it, PROTOCOL 11); `client/dragon/DragonTextures` reads the owner's PNG once
  (NativeImage.m_85058_, allowlisted), recolours on a daemon worker, uploads on the render thread as `hexhaki:dragon/colour_<n>`, the
  owner's texture until ready. Judged on close renders (`DragonPreview --colour=N`); `DragonColourTests`.

- **Done: the key layout** (the owner's item 7): `PiecePatch.keyDefaults` (HakiKeys.<clinit>: observation 74 J, acoc unbound,
  convergence (slot 4) 72 H, mode (slot 9) 88 X, loadout 79 O, slot8 86 V), `slotLetters` (Binds.DEFAULT_KEYS G K L H B Z C V X,
  HAKI_KEYS G K L H), Pika's "[M] hold" -> "[H] hold", the Haki Projection text "[H HOLD...", QuickKeys.press's fruit abilities only in
  fruit mode (PieceHooks.fruitKeysActive), PieceKeys specials Z (90) / X (88), the logbook / creation / combat banner letters.
  `client/KeyLayout`: once (a mark `hexpiece-keys-2.7.1.txt` in the game folder), every key still on its old default moves to the new
  one (KeyMapping.setKey, resetMapping, Options.save: allowlisted). Forge 1.20.1's KeyMappingLookup.getAll hands a key to EVERY mapping
  on it, so Z/X (special and fruit slot) both get the press; the mode gates decide. **N and M are not slots** (the original has 9:
  Binds.SLOTS, layouts of 9): to give them moves, add HexPiece's own KeyMappings that send `FruitKeys.send(0/1, ability, flags)` for
  a kit's move, or widen the slots (invasive).

## Done (2.7.2, pushed): the moves (the owner's item 5)
- `server/UoKit`: FruitMoves HUMAN (Z Dragon Form, X Air Pressure, C Blast Breath, V Wind Force, B Dragon Embers), DRAGON (Z, "[LMB]
  hold" Fire Breath, "[X] hold" Blast Breath, C Gigantic Bite), MOVES all (the server's list; Binds lays them by their key's slot: X
  slot 8, C 6, V 7, B 4, Z 5; a key shared by a human and a dragon move is dispatched by the form). Cooldown keys uo_air, uo_breath,
  uo_wind, uo_embers, uo_flame, uo_blast, uo_bite (FruitData.ready / cooldown + FruitServer.sync: the panel's recharge).
  `client/dragon/UoClient.moves()` is the form's list (the panel); LogbookData lists UoKit.MOVES for the Uo fruit.
- `server/UoMoves`: everything simulated server-side in a ticked list (`Live`): Shot (AIR / BOLT / BLAST: flies at its speed, strikes
  the first foe within its size or the first block or its range's end, then `explode` (damage falling off, fire, Impact.hit throw,
  Impact.quake for fire)), Tornado (0.55 b/t for 25 blocks: entities inside the funnel (radius 2.2 -> 4.2 over 9 high) spun round
  (0.55), pulled to a ring, lifted 0.22, carried, 2 damage / 10 ticks, flung at the end), Ember (lands at its tick, explodes 2.6), the
  held breath (FLAMING: a 9 deg cone from the approximate mouth, stopped by blocks, 4 damage + 5 s fire / 5 ticks, at most 100 ticks,
  then 80 ticks' cooldown), the charge (CHARGING: X down -> up; range 45 + 55 x charge, full at 60 ticks), the bite (a 2.2 b/t lunge,
  the jaws shut at tick 7: 20 damage within 5.5 of the jaws, a heavy throw). Human-form beats equal tools/uo_anims.py's (AIR 4/7/10,
  BREATH 7, SWIPE 6, EMBERS 8); the animations uo_air_pressure / uo_blast_breath / uo_wind_force / uo_embers (rendered and linted).
- Packets: S2CUoFx (17: kinds AIR BOLT BLAST BURST TORNADO EMBER FLAME_ON/OFF CHARGE_ON/OFF BITE), C2SDragon (18: M1 held), PROTOCOL
  12. `client/dragon/UoFx` draws them from what was sent (shots advanced each tick, embers on two Bezier halves, the tornado from its
  start and step): fire = CLOUD particles via ParticleEngine.createParticle + Particle.setColor (the original's own calls) coloured by
  heat, SMOKE / LARGE_SMOKE / SMALL_FLAME, Photon only for sparks (PieceFx.SPARK); counts through PieceFx.count. The breath and the
  charge leave the mouth the renderer last drew (DragonRenderer.draw writes the jaw's placed point into the client entry). The jaw and
  the bite are posed by DragonForm.jaws (JAW_OPEN 38 deg, the head reared -18 then lunged 14 in the bite: 14 ticks, shut at 0.45-0.6);
  DragonTests check they never jump. **The particle effects were NOT rendered** (no particle preview exists): judge in game, or
  write a preview of UoFx's emission.
- Fixed in passing: `StyleCombat.sound` asked Fruits.sound for "fx.<name>" (the map's keys are plain names): every combat sound through
  it (sword impacts, water and finger-pistol whooshes) was silent; it strips the prefix now.

## Next (2.7.3)
1. Check 2.7.0's runs. Ask the owner how the dragon handles (nothing was tested in game).
2. **The moves** (item 5 above), in the ability HUD (AbilityHud.kit: ClientKits.get(fruit).moves()) and the Captain's Log
   (LogbookData.kit). Human-form animations: playerAnimator JSONs under src/main/resources/assets/hexhaki/player_animation/ (judge with
   tools/anim_preview.py). Six Powers' air blasts: style/ (the Air Blade, AirMesh, client/fx/AirFx). Fire as coloured CLOUD + smoke
   particles (vanilla), Photon only for sparks. Dragon-form moves: the jaw bone ("jaw") for the breath and bite, posed in DragonForm
   (add a mouth-open bend); the breath's origin = the Head_Face/jaw bone placed by DragonForm.placement (both pure: tests can check it).
3. **Random dragon colours:** a hue per player (random at each transformation, sent in S2CDragon), applied as a vertex colour multiply
   in DragonRenderer.emit (r, g, b now 1) or by a recoloured texture (DragonForm is pure: keep the colour maths there).
4. **The lag and size pass (the owner's item 2, not started):** jar 30.7 MB in 2.6.9 (32.5 with the dragon): katana meshes 14.2 MB
   (assets/hexhaki/katana/*.hkm, already gzip: a delta encoding of positions and quad indices would shrink them a lot; tools/
   katana_mesh.py + client/sword/KatanaMesh), katana atlases 7.6 MB (PNG, lossless recompression), the original's sounds 3.8 MB
   (theirs: do not re-encode). Lag: nothing measured; look for per-frame allocation and work in render paths (HolsterHand's 2.6.8
   diagnostic log line once a second during a draw with Haki on: remove once the owner confirms the Haki lag is gone).
