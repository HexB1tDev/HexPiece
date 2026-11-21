# HexPiece: handoff after 2.7.0 (the Uo Uo no Mi with the owner's dragon: Z transforms, flies as creative, transforms back; the left hand under Haki fixed)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.6.2.md (the katanas 1.25 bigger, the first-person draw and
sheathing, the Haki coat on blades, the Conqueror's sounds and stun), next-ai-2.5.9.md, next-ai-2.5.3.md (Ryou's fist aura:
"beautiful and perfect": keep it), next-ai-2.5.1.md (**the owner's device: Android phone**, Amethyst, MobileGlues).

Branch `claude/nice-bell-gam7qf`. **2.7.4 is pushed (check its "Build HexPiece" and "Release HexPiece" runs first; 2.7.0 - 2.7.2's were green, 2.7.3's running).
The next version is 2.7.5: the owner's report on the dragon, its moves and colours; more of the lag pass if they still see lag.**

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

9. **(after 2.7.4, verbatim) "its amazing, but the attacks have to be more aggressive more violent, perhaps time to switch over to
   Photon, go crazy with it (the attack particles), sparks, explosions, GO CRAZY all of it must be 3d." BUG: "Dragon keeps changing
   color, the color should be attributed one time only when eating the fruit." Then, with two anime frames of Kaido's Bolo Breath
   (docs/handoff/uo-fire-target-1.jpg: the charged orb in the mouth, glowing yellow core, red corona, light rays; uo-fire-target-2.jpg:
   the breath: white-hot at the mouth, bold cel bands of yellow, orange and red licks with dark red edges, white shock arcs, embers
   everywhere): "the fire has to be beautiful I mean look at the reference".**

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

## Done (2.7.3, pushed): the owner's item 8
- `PieceEffects` (DeferredRegister MOB_EFFECTS): `hexhaki:transformed`, a MobEffect whose own modifier (fixed uuid HEALTH_ID,
  MULTIPLY_BASE 1 on MAX_HEALTH) doubles the maximum health; icon textures/mob_effect/transformed.png (an azure dragon's eye), lang
  "effect.hexhaki.transformed". `server/Transformed` (every 10 ticks): on while the dragon is out (DragonServer.phase != HUMAN) or
  Gear 5 is on (GearData.hasFruit && GearData.flag(player, "gear5")): added infinite (-1), the health scaled to the same share of the
  doubled maximum; Resistance II 40 ticks renewed each time (never removing one from elsewhere); off: removed, the health clamped.
- Locks: `RyouServer.hakiOff` (the original's HakiServerController.onAction asks it, PiecePatch) is true while the dragon is out: every
  Haki action dropped on the server; `CombatMode.keys` drains R and Ctrl + R doing nothing while DragonClient.selfOut(). Haki already
  ON when transforming is not switched off (only nothing new): if the owner wants it switched off, call the original's toggles.

## Done (2.7.4, pushed): the owner's item 2 (size, lag), first pass
- Jar 32.7 MB (2.7.2) -> 24.0 MB. The katana meshes in format 2 (HKM2, tools/katana_mesh.py encode/planes/decode, client/sword/
  KatanaMesh.load reads 1 and 2): 15.1 -> 6.0 MB, proven identical in the game's loader for all 150 (a scratch harness loading both
  formats through KatanaMesh.load by reflection; build.py caches katana-cache/...-lod3-hkm2, legend-cache/...-hkm2). build.py
  `lean_png`: the blade atlases' all-opaque alpha dropped (pixel-identical), 0.64 MB. The original's sounds (3.8 MB) are theirs: not
  re-encoded.
- `client/render/Bulk`: the pose read once a mesh (transformPosition / transformDirection / Matrix3f.transform), each vertex one
  VertexConsumer.m_5954_ (the buffer's fast path) instead of the seven-call chain: KatanaRenderer.quads and coated (every blade and saya,
  every swordsman, every frame) and the dragon. The highlight (a POSITION_COLOR type) is still the chain.
- Not done (no measurement possible here): other per-frame costs (RyouAura, SwordLayer, the original's Haki layers); HolsterHand's 2.6.8
  diagnostic log line is still there (once a second during a draw with Haki on). Ask the owner where they see lag (which screen,
  how many players or NPCs, Haki on or off) before guessing further.

## Done (2.7.5, pushed): two of the owner's bugs
- **"the keys HUD a ton of them say No slot":** AbilityHud.kit asked BindsClient.keyFor(i) with the move's place in the list; a kit
  move's ability is its default slot (FruitMove.slot(), the letter's index in Binds.DEFAULT_KEYS, as the original's FruitHud.onKey
  reads it). The Uo lists are out of slot order, so most rows were "no slot" (old bug since 2.3.x; Pika's list happened to fit).
  Now LogbookData.moveKey(move) for the HUD and the log alike (slot -> the key it is on, "-" when unbound; no slot -> its own key,
  the HUD's cap shortened to "LMB"). DragonTests.keys() checks every Uo move sits on its slot in Binds.defaults for both forms. In
  dragon form the panel drops "Back to ... and Haki" (AbilityHud passes the keys without "toggle" while DragonClient.selfOut()).
  UiPreview renders both Uo panels (hud-4, hud-5).
- **"Dragon keeps changing color, the color should be attributed one time only when eating the fruit":** DragonServer.colour(player)
  keeps it in the original's fruit data ("uo_colour" in the "hexhaki_fruit" tag: FruitData.give makes a fresh tag on eating, FruitServer
  .clone copies it through death), rolled in UoKit.granted (eating, and login for an old fruit) or at the first transformation.

## Done (2.7.6, pushed): the owner's item 9 (violent, 3D, beautiful fire)
- The owner during it (verbatim): "it is very very good, just make sure it's bright and glowy, and it's coming from inside the mouth
  not from anywhere else", then "you can adjust the head/jaw to adapt the size, you have complete freedom".
- Pure (no game class, tested in DragonTests.fire, rendered by tools/preview/FxPreview): dragon/FireMesh (cel-banded puffs: each
  triangle cut along the band edges so every band is flat and sharp; palettes BAND fire, SMOKE, WIND, AIR; glows (soft shells), shock
  shells, crescents, rings, dashed rays, 3D streaks, rock shards, tubes; `quality` from the particle setting) and dragon/UoFire
  (breath, orb, fireball, explosion, airShot, airBurst, tornado, bite). DragonForm: the roar (JAW_OPEN 38 -> 58, FACE_LIFT -9),
  jawAngle, mouth (the inside of the gape MOUTH_DEEP from the hinge, and the mouth's own way: the jaw's turned back by half the
  opening about the face's x axis).
- Client: client/dragon/UoFx rewritten: everything drawn at RenderLevelStageEvent AFTER_PARTICLES through RenderType debugQuads
  (m_269166_, written straight into the buffer, eye-centred, the batch ended at once); no vanilla particles any more, Photon embers
  and sparks with bloom; shots placed analytically (start + velocity x age); a dragon's BLAST starts at the real mouth and blends onto
  the server's path in 5 ticks; the breath keeps the last 24 aims (a sweep, not a rigid beam) and stops at a wall (client ray);
  DragonRenderer gives the mouth's inside and way (6 floats). The jaw opens for the breath in 4 ticks and closes over 6 after it.
- Server (UoMoves): blast radius 4.5-12, damage 16-46, knock 1.8-4.2, fire 10; Blast Breath 4.5 / 11 / 1.6; Air Pressure 2.0 / 8 /
  1.8; embers 3.2 / 7 / 1.0 with a small quake; the shots' quake radius x5, stronger, 14 ticks.
- Budget: a full breath about 10-13k quads near, an explosion of radius 8 under 11k (DragonTests: every move under 16k).
- From the dragon's own camera (DragonCamera: straight behind and over the eye) the head and mane hide much of the breath and the orb
  (the camera looks down the line of fire). Asked the owner whether to move it over a shoulder; the owner (verbatim): "I think its
  fine as it is". The camera stays: do not move it.

## Done (2.7.7, pushed): two of the owner's reports on 2.7.6
- The owner (verbatim): "bug found, in first person the height does not match the head height, means the fire and projectiles are
  coming from above  (top of the screen)", then "also the Blast Breath Move should be a longer line of fire, like wayyyy longer".
- First person: the own dragon is not drawn there, so UoFx.mouthWay(player, partial) puts the mouth FIRST_AHEAD 4 blocks out and
  FIRST_UNDER 1.5 under the view's line, at the interpolated eye and look (held to the camera). FireMesh.puff never draws a puff
  the eye is in and caps a puff at a sixth of its distance from the eye; UoFire.glow never wraps the eye.
- Aim: server UoMoves.aimed/toward (the first foe or block on the eye's line within the range, else its end; the look when that is
  under 6 blocks from the mouth or turned more than 60 degrees from it): the charged blast's velocity, the breath's cone (worked out
  only on its damage ticks now) and the bite's jaws go from the mouth onto it. Client UoFx.aimed is the same (blocks only) for the
  breath's way, its reach and its embers, and the blast's visual starts at mouthWay (first person included).
- UoFire.line: every piece of a shot's way flares as the head passes and burns down (Blast Breath 14 ticks, size x1.5; the dragon's
  blast 12 ticks, size x0.42), drawn from the shot while it flies and from UoFx.LINES after it bursts. DragonTests checks the line runs
  its whole 35 blocks at the burst, fits the budget and burns out.
- FxPreview: `fp:pitch[:aimAhead]` renders first person (the own dragon hidden, the mouth as UoFx's, aimed onto the crosshair point);
  `|blast:age:charge:end` / `|bolt:age:size:end` draw the line after a burst at `end` blocks.

## Done (2.7.8, pushed): the Light fruit rebuilt after Kizaru (the owner's item 10), and the Uo beams
- The owner (verbatim): "Next better the Lightfruit in every way, make it cooler, more dramatic, better animations, fix broken areas
  of it (its terrible in every single way). Do a deep research about Kizaru One piece, copy his moves and how he uses them exactly."
  Earlier (2.7.1): "All keys of the mod (but haki) will now be the following Z, X, C, V, B, N, M".
- Research (onepiece.fandom.com and its mirrors are blocked here; web search): Yata no Kagami (light formed between the hands,
  reflected; he travels along it as light), Yasakani no Magatama (in the air, arms crossed, a torrent of light particles from both
  hands that blow up whatever they touch; at Egghead the particles became light clones of him with light swords), Ama no Murakumo
  (a sword of solid light; vs Rayleigh), the light-speed kick ("Have you ever been kicked at the speed of light?": Urouge, Luffy;
  a kick that fires a beam from the leg, vs Zoro), finger lasers (Apoo, Drake, Whitebeard, Marco from behind with two, Ace's key),
  the Logia body (reforms after Apoo blew him apart), lazy drawl, hands in pockets.
- The original: com/hexhaki/fruit/pika/LightKit (server, 50 KB), LightMoves, client LightClient (ClientKit: stance, heldFor,
  receive, tick, renderWorld, renderBody, firstPerson, hud), LightFx, LightBody, LightHands; animations light_*. Kit actions: kind 0
  key down (ability = quick slot), 1 key up, 2 attack (repeats every 5 ticks held), 3 use down, 4 use up; flags 1 sneak, 2 sprint,
  4 airborne (FruitKeys). FruitServer does the Logia pass-through itself (Fruit.logia; Armament stops it) and calls kit.dodged.
- Built (this was the plan): a new kit replacing LightKit (Fruits.register(Fruit.PIKA, ...) after the original's; ClientKits.KITS.put(PIKA, ...)), keys Z
  Yata no Kagami (tap: arrive where you look; hold: stream as light), X Yasakani no Magatama (rise, arms crossed, the torrent; sneak:
  one target), C Ama no Murakumo (the sword: LMB cuts, RMB guards), V the light-speed kick (hold, let go: on the target, kicked across
  the field), B Yasakani clones (Egghead); LMB kicks (5, sprint dash-kick, air heel drop), RMB finger laser (tap) / beam (hold).
  Visuals as 3D light (FireMesh with a LIGHT palette), new animations (tools/pika_anims.py), previews (FxPreview).

- Written: server/PikaKit (moves, dispatch, flight, Logia hooks) and server/PikaMoves (every move; S2CUoFx kinds 32 to 55, the
  constants at its top), client/pika/PikaFx (state + drawing at AFTER_PARTICLES through debugQuads; puts PikaClient in ClientKits on its
  first tick) and PikaClient (moves, stances pika_idle/walk/float/fly/sword_idle/sword_walk, the sword in the right hand through
  model.f_102811_ and in first person), pika/LightArt (pure: laser, charge, beam, burst, impact, bullet, crossed, mirror, path, blade,
  slash, trail, figure), FireMesh.LIGHT palette, tools/pika_anims.py (33 pika_*.json, all pass the build's lint), HexPiece.init registers
  PikaKit over the original's. Previews: FxPreview `|laser:age:dist`, `|charge:age:c`, `|beam:age:dist`, `|lburst:age:r`, `|bullets:age`,
  `|mirror:age`, `|path:age`, `|slash:age`, `|figure:t`, `|blade:tilt`, `|trail`, `|fbeam:age:charge`, `|hbeam:age`, `|roar`;
  animations: tools/anim_preview.py.
- The owner during it (verbatim): "About dragon, I want the jaw to open wider in every form, like way more both top jaw and lower jaw.",
  "Lets get this straight both versions of blast breath are BEAMS, not short blasts, it's still too short, and I mean the max charge of
  the beam should be ridiculously bigger, its overall too small.", "thinner close to the mouth. and bigger final explosion. also
  proportional to the holding charge.", "it should also destroy blocks and self repair after 30 seconds.", (on a render of the beam
  bending down out of the mouth) "dont let the beam bend, as soon as it hits a block, it explodes.", "the explosion should also cause
  massive knockback, and stun for 10 seconds (ragdoll)".
- Done for those: DragonForm JAW_OPEN 88, FACE_LIFT -26; UoFire.fireBeam (straight from the mouth onto the aim; UoFire.taper: 12% of
  its width at the mouth, full 6 x width + 4 blocks out; front at BEAM_SPEED 30 blocks a tick) and UoMoves.Beam (both Blast Breaths:
  human 1.4 wide, 50 long, 30 ticks; dragon 2.5-14 wide, 60-160 long, 30-70 ticks; burns every 4 ticks within the tapered width;
  explodes and opens a crater the moment it strikes a block, again every 6 ticks there; a last explosion of 6-24 (human 4) and a
  crater of 3-12 (human 2) when it ends); UoMoves.blowAway (the first strike's and the last explosion: Impact's heavy throw away from
  the blast, then HakiFalls.knockDown as the body lands, for the rest of BLOWN_STUN 200 ticks); S2CUoFx FIRE_BEAM 12 / FIRE_BEAM_OFF 13;
  server/Craters (a crater opened nearest-first through the existing Scars, within its budgets, every block back after Craters.REPAIR
  600 ticks: Scars.tear gained a restoreIn overload; the existing callers keep 15 to 25 s). The orb in the jaws 0.4 + 1.5 x charge.
- Careful: server/Scars.java is HexGhoul's self-repairing terrain (the Conqueror's fissures, the Haki slashes): extend it, never
  replace it (it was overwritten by mistake once in this session and restored from git before anything was committed).
- Not measured: a max-charge beam close up is about 17-22k quads (over the 16k the other moves keep); lower particle settings cut it.

## Done (2.7.9, 2.8.0, 2.8.1, pushed): the light sword
- The owner (verbatim, on FxPreview's blade render): "this is not a sword at all". 2.7.9 gave it a hilt and a broad blade; then
  (2.8.0, with two anime frames, docs/handoff/murakumo-ref-1.png and -2.png) "its meant to be a full light sword no handle".
  LightArt.blade now: no hilt, a thin rod of light (PikaClient.BLADE_LENGTH 2.6, BLADE_WIDTH 0.032 radius) white core in a pale-gold
  sheath (LightArt.rod), two glow sheets, and at the fist a ring of light in the rod's plane with a cross bar through it
  (LightArt.rodZ) and a starburst of nine rays, a small local glow (LightArt.glowLocal). Render: docs/handoff/pika-sword-2.8.0.png
  (FxPreview `|blade:20`, `|blade:-50`). 2.8.1 (the owner: "the grip handle rod has to be longer"): the rod runs 0.8 blocks back past
  the fist (was 0.32).

## Done (2.8.2, pushed): the fighting styles in first person, phase 1
- The owner (verbatim): "after that, adapt the VFX of the other fighting styles, add more custom perfect moves (with first person) and
  give the current M1s a first person (some are missing, like 6 powers, black leg, water kungfu and iron first). give each their show
  accurate moves if they lack it, better VFX on the M1s (m1 vfx on water kungfu suck ass), add variatations with the haki on/off, do
  not stop until every animation is completly perfected, bugless and astecticly pleasing to look at and play with." Then "Just make
  sure you do not stop".
- Arms: the original's HakiAnimations.playRemote plays every animation in first person (FirstPersonMode.THIRD_PERSON_MODEL) with both
  arms hidden (only OWN_FIRST_PERSON_ARM names were spared), so a style's punches showed nothing. PiecePatch.firstPersonArms hooks
  PieceHooks.firstPersonArms(config, name) before setFirstPersonConfiguration: `style_*` (not the Swordsman's, SwordClient has its
  own), `pika_*` and `uo_*` show both arms. build.py PATCHED lists HakiAnimations.class.
- Legs: PlayerAnimator never draws legs in first person. client/FirstPersonLegs (RenderLevelStageEvent AFTER_PARTICLES, the player's
  own skin through RenderType.entityCutoutNoCull, legs and trousers) draws them while the local player's move kicks (a leg raised past
  35 degrees at some moment; StyleFx stage 0 starts it). style/KickLeg (pure): the 4x12x4 leg split at the knee (bendy's bend turns
  the shin back), ModelPart's Z-Y-X order, then the body's own move/lean/spin about 0.7 blocks (PlayerAnimator's order) in the
  player's frame, turned by the body's yaw (yBodyRot).
- The owner corrected the framing twice while it was being judged (verbatim): "hell no thats way too high up and too big, hell no",
  then "legs are to remain attached to the body, not hanging infront of the screen bro". So the legs are where the body has them (no
  lift, no framing eye): a kick shows as the leg rises into the lower view, the axe kicks come up in front of you, looking down shows
  the whole kick. Never float them in front of the camera again.
- Mutton Shot and Sky Walk raised the leg straight up through the face (third person too: the leg went 8-10 degrees behind vertical
  with the body leaning back). tools/style_anims.py now peaks them at leg -136/-138 with the body pitch 10 (about 34 degrees in front
  of vertical, clear of the head).
- Preview: tools/preview/FirstPersonPreview.java (`javac -cp build/classes` KickLeg + the npc poses, then `java FirstPersonPreview
  out.png skin.png [--pitch deg] [--frames n] [MOVE ...]`; the camera at the eye, a target 3 blocks ahead).

## Done (2.8.3, pushed): the fighting styles' blows in 3D, Haki on and off
- The owner (part of the request in "Done (2.8.2)"): "better VFX on the M1s (m1 vfx on water kungfu suck ass), add variatations
  with the haki on/off". Render: docs/handoff/style-vfx-2.8.3.png (BL_SNAP, FK_PALM, FK_BRICK, IF_CROSS).
- style/StyleRig (pure): the player's body as tools/anim.py and PlayerAnimator + bendy-lib pose it (every limb bent at its middle,
  ModelPart Z-Y-X, the head and arms carried by the body's bend, then the body's move/lean/spin about 0.7 blocks, scale 0.9375), in
  the player's frame. style/StyleArt (pure, FireMesh quads, cel bands laid inside one another): smear (the sheet a striking fist or
  foot sweeps), thrust (rings of shocked air off a straight blow and speed lines), impact (the anime's impact star, a ring, a flash),
  splash (Fishman Karate: a crown of water, two ripples, drops that arc and fall, WaterMesh bodies), helix (water wound round the
  arm), crackle (Armament: black bolts edged red), its own finely cut ring. Looks: KICK (Black Leg, hot), AIR (Six Powers), WATER,
  SHOCK (Iron Fist), FIRE (the Flame Leg), HAKI and HAKI_WATER (black, red edge).
- style/StyleShow (pure): which limb strikes each blow (the fastest end into its tick; straight if driven along the limb and not
  flung by the body's spin; the Rising Knee strikes with the knee), the smear while that end moves fast (nothing in a slow wind-up),
  the thrust for straight blows (not Six Powers: AirFx keeps its rings), Fishman Karate's helix and hand splash, Haki's crackle; landed
  blows: impact + splash + crackle. Fades everything within 1.15 blocks of the eye (NEAR/CLEAR); in one's own first person the pushes
  and the hand's water are 0.55 as big. Not drawn for the moves with their own effects (Water Bullet, Prison, Brick Fist, Air Blade,
  Iron Body, Shave, Shoulder Charge) or the Swordsman.
- client/fx/StyleVfx: S2CStyleFx stage 0 starts a move on its user (stage 3 starts it for a user whose start never came, an NPC's),
  stage 1 a landed burst; drawn at AFTER_PARTICLES through debugQuads, every quad of the frame laid far to near in 5 cm steps (the
  bands of one look keep their order); Armament or Ryou on the user (RemoteHakiStates) turns them black and red. StyleFx keeps only the
  dust and the Flame Leg's flames as particles for these moves.
- Preview: tools/preview/StylePreview.java (`StylePreview out.png [--haki] [--fp] [--frames n] [--view deg] MOVE ...`; compile
  StyleRig/StyleArt/StyleShow, FireMesh, WaterMesh and the npc poses against build/classes, then DragonPreview, FxPreview and
  StylePreview). StyleTests.show checks the rig, every blow's limb and every moment's quads.

## The owner's queued requests (verbatim; TOP PRIORITY once the style extras below are out)
- "I want the dragon, whenever he moves and flies up and all, to like wobble his body a little, like more. And by more, I don't mean
  like increase the amount of wobbles of the body. I mean like make them higher, like, you know, like up and down more. Yeah, make it
  up and down more, like more angular whenever it moves and flies and shit, you know what I mean?" (bigger up-and-down amplitude of the
  flying dragon's body undulation, sharper angles; NOT more waves.)
- "And also, whenever the blast breath is being charged, you can freely aim it, but when you let go, your aim is instantly locked into
  that position without being able to move until it's over. It's the drawback of that move. So you have to actually think where you're
  gonna aim it." (Blast Breath, both forms: free aim while charging; on release the aim (camera/look and the beam) is frozen until the
  beam ends.) "This is like your top priority after you finish whatever you're doing right now."
- (Done in 2.8.5 and 2.8.6.) Then: "the next top priority thing is giving every move of the dragon custom sounds from the web. And I mean, don't make any sound
  yourself. They all have to be from the web, and they have to be the best versions they can be. So pick something epic for
  everything. Wind sound effects, fire sound effects, fire like blasts, fire explosions, charging, everything comes from the web."
  (Free-to-use sounds only, credited; every Uo move: breath, fireball, blast breath charge/beam/explosion, wind moves, tornado, roar,
  bite, transformation, flight.)

## Done (2.8.4, pushed): the show's own moves, three per style on C, V and B
- The owner (part of "Done (2.8.2)"'s request): "add more custom perfect moves (with first person) ... give each their show accurate
  moves if they lack it". Render: docs/handoff/style-extras-2.8.4.jpg.
- Slot EXTRA_C, EXTRA_V, EXTRA_B (PieceKeys EXTRA_C/V/B on C, V, B; StyleInput sends them in combat mode, not for the Swordsman);
  mastery C 20, V 100, B 250 (StyleTests: C 1-50, V 51-200, B 201-400, each recharges). The HUD, the panel, the logbook (keys C/V/B)
  and the creation screen list them; MenuTests expects 10 entries.
- The moves (StyleMove, animations in tools/style_anims.py `extras()`, judged with tools/anim_preview.py and anim.lint):
  Black Leg: Party Table Kick Course (handstand, legs split and spun, 4 hits round + a heavy), Anti-Manner Kick Course (handstand, both
  legs driven up, launches 1.35), Diable Jambe Premier Hachis (leg heated by a spin, 8 burning kicks + a roundhouse; FIRE look);
  Six Powers: Moon Walk (Effect.AIR_STEP: a hop of 0.95 that works in the air, fall forgotten, 10-tick recharge), Paper Art
  (Effect.DODGE: StyleServer.dodge cancels every blow or shot from someone for 2 s and drifts the body aside), Six King Gun (both fists,
  14 damage, a sphere of shock); Fishman Karate: Spear Wave (line 20, a long body of water), Vagabond Drill (line 11, a whirlpool
  drill), Five-Thousand Brick Fist (a hop and a fist into the ground, all round 6.5, a wave of water along the ground); Iron Fist: Fist
  of Love (overhead, drives them down, a shock ring on the ground), Fist Meteor (line 24, a black cannonball trailing smoke), Honesty
  Impact (line 10, rings racing down a tunnel of shock). Throws cock the arm behind the head (positive elbow bend: the lint's "arm
  through the head" otherwise). Fishman Karate's and Iron Fist's extras ease in (no tick-0 pose) and keep their legs (StyleTests).
- Effects: StyleShow.special (Moon Walk StyleArt.step, Paper Art StyleArt.papers, Six King Gun StyleArt.sphere, Five-Thousand and Fist of
  Love StyleArt.wave), StyleShow.shot/shotKind (StyleArt.shot: spear, drill, ball, tunnel) drawn by client/fx/StyleVfx from stage 2
  (StyleFx keeps its Photon shot only for the others). FirstPersonLegs skips moves that turn the body over (a handstand).

## Done (2.8.5, pushed): the owner's first two queued dragon requests
- The flight wave (DragonForm.straighten): the rise and fall per spine segment WAVE_UP 5.5 + WAVE_UP_TAIL 9.5 toward the tail (was
  2.5 + 4), the neck NECK_UP 3.4 (was 1.5), shaped by DragonForm.shape: a sine blended 0.6 toward a zig-zag (straighter runs, sharper
  turns); the same WAVE_PERIOD and WAVE_SEGMENTS (the owner: not more wobbles). Render, before and after: docs/handoff/dragon-wave-2.8.5.png.
  DragonTests.wave pins it; the cape-clearance check still passes.
- Blast Breath (both forms): server/UoMoves.Beam keeps the aim it had when it was let go (Beam.aim; it used to follow the look every
  tick) and S2CUoFx FIRE_BEAM carries that yaw and pitch; client/dragon/AimLock holds one's own view on it every tick and before every
  frame (RenderTickEvent START undoes the mouse's turn) until the beam's life ends, FIRE_BEAM_OFF, death or logout. Charging is free
  to aim, as before. Only the aim is held (the player can still move); if the owner meant movement too, hold the position as well.

## Done (2.8.6, pushed): the dragon's sounds, all from the web (the owner's third queued request)
- opengameart.org was refused by the session's network; a personal environment "HexPiece" (env_01DEzkwiWiBUXF6hnQ29v2S5) was made
  with the owner's approval: opengameart.org, freesound.org and cdn.freesound.org allowed, saved for this repository.
- tools/dragon_sounds.py fetches 22 CC0 recordings from Freesound.org (chosen by rating and downloads; each page checked for CC0 before
  use; its high-quality preview), cuts 31 clips (start, length, fades), mono 44.1 kHz Ogg Vorbis, peak -1 dBFS, into
  assets/hexhaki/sounds/piece/uo/ with SOURCES.txt crediting each (title, author, page, CC0). 1.2 MB.
- dragon/UoSounds (pure): 18 events (roar, form, whoosh, unform, gust, air, air_hit, tornado, flame_start, flame_loop, charge, beam,
  fireball, ember_hit, explode, blast, lunge, bite) and their clips; PieceSounds registers them (PieceSounds.event); sounds.json and the
  subtitles were added by script (big ones heard far: attenuation_distance 64-128).
- Wired: Z forming: form boom + whoosh, the roar once fully formed; going back: unform, a deep boom as it lands; flying fast (over 0.55
  blocks a tick): a gust every 70 ticks, louder with speed (DragonServer); Air Pressure: air, air_hit where it strikes; human Blast
  Breath: beam; Wind Force: tornado (again every 80 ticks while it travels); Dragon Embers: fireball, ember_hit at each landing; Fire
  Breath: flame_start, flame_loop every 50 ticks while it burns; the dragon's Blast Breath: charge on press, roar + beam on release,
  explode where it strikes, the blast (as big as the charge) at its end; Gigantic Bite: lunge + roar, bite. No hand-made sound.
- SwordTests.dragonSounds checks every event, clip, credit and file format.

## Done (2.8.7, pushed): Blast Breath holds the body still too
- Asked whether the aim lock should also hold the dragon in place, the owner: "it should yes". client/dragon/AimLock now also drops
  every movement key's input while the beam burns (MovementInputUpdateEvent: walking, strafing, flying up and down) and stops the
  motion every tick; server/UoMoves.beam stops the user's velocity as it starts. Both forms.

## Done (2.8.8, pushed): the owner's Light fruit bug report
- The owner (verbatim): "Bug: Light animations arent playing and the sword isnt being grabbed by the handle. Its boring and the light
  clones are bad to look it, overall terrible".
- Why nothing played: the original plays a fruit's stance without end on its own PlayerAnimator layer (FruitAnimations, priority
  1100) and only clips it `handles` ("light_", "magma_", "quake_") as moves there (standing the stance down while they play); every
  other clip went to HakiAnimations' layer (900), under the stance: every pika_ move since 2.7.8 played unseen. PiecePatch.fruitClips:
  FruitAnimations.handles asks PieceHooks.fruitClip first ("pika_" and "uo_"); build.py PATCHED lists FruitAnimations.class. (These
  moves now use the original's fruit first-person mode: the game's own hands.)
- The sword: LightArt.FIRST_PERSON_GRIP (the game's first-person right arm, ItemInHandRenderer.renderPlayerArm at rest, then the arm's
  pivot) and THIRD_PERSON_GRIP (from the arm's pose) put the guard LightClone.GUARD 0.24 ahead of the fist, the grip through the hand
  and 0.56 behind it. Render: docs/handoff/pika-grip-fp-2.8.8.jpg (tools/preview/GripPreview.java).
- The clones: pika/LightClone draws the user's own body (StyleRig: Minecraft's boxes, limbs bent, posed by the Light fruit's own
  animations, sampled from client/npc/PikaPoses, now generated by tools/pika_anims.py) in their own skin (style/SkinBox: the skin's
  UV layout) through entityTranslucent, full bright, washed white-gold; a white-hot shell just over the skin and two gold shells
  round every box (debugQuads); Ama no Murakumo held by its grip (LightClone.held). PikaFx.clone: they run at the foe (pika_sword_walk
  sped up) with two afterimages, cut twice (pika_sword_1, pika_sword_2), burst. Render: docs/handoff/pika-clones-2.8.8.jpg
  (tools/preview/ClonePreview.java out.png skin.png).

## Done (2.8.9, pushed): 2.8.8 crashed the game at load (fixed; never again)
- The owner's crash report (verbatim, Forge 47.4.23, Java 17): "java.lang.VerifyError: Expecting a stackmap frame at branch target 9
  ... com/hexhaki/fruit/client/FruitAnimations.handles(Ljava/lang/String;)Z @4: ifeq ... Expected stackmap frame at this location."
  PiecePatch.fruitClips (2.8.8) jumped to a new label without a FrameNode (the patch writes COMPUTE_MAXS, not frames): the mod failed to
  load. Fixed (FrameNode F_SAME after the label).
- Two guards so it cannot ship again: PiecePatch.frames checks every patched class as it is written (every branch target, switch case,
  handler and instruction after a jump, return or throw starts with a frame) and fails the build; patch/JvmVerify (a build step after
  PieceVerify) links every patched class through the JVM's own verifier over the stubs (29 of 29; empty verification-only stubs
  FormattedText, Position, IShape, Vector3fc added). Both reproduce the owner's exact error on the 2.8.8 jar.
- RULE for every future patch: a new jump target needs `new FrameNode(F_SAME, 0, null, 0, null)` (or the right frame) after its label.

## Next (2.9.0)
1. Check 2.8.9's runs. Tell the owner 2.8.8 crashed at load and 2.8.9 fixes it; then ask how the Light fruit plays now that its moves
   animate, how the clones and the sword look in game, and which moves still feel boring (the effects themselves are 2.7.8's: LightArt).
2. Ask how everything since 2.8.2 plays (first person in the styles, the new blows, the new moves, the dragon's wave, the Blast Breath
   lock, the dragon's sounds); nothing was tested in game.
3. Earlier items still open: the lag pass (the owner's item 2).
