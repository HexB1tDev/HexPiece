# HexPiece: start here (written at 3.8.1; the session after 3.8.0)

Read CLAUDE.md, then this file whole, then docs/handoff/next-ai-3.7.6.md (sections 1 to 8: where things stood at 3.8.0, the
Leopard, every standing rule of the last days), then the older handoffs it names as needed.

## 1. The owner's requests this session (verbatim, with a GIF in a zip and a picture)

The owner sent `Haki.zip` (one GIF, 382 x 216, 157 frames at 50 ms: the anime's Conqueror's Haki clash, Luffy against Kaido and Big
Mom: red and blue Haki meeting between them, a black ring round a bright sphere at the point where they meet, red lightning from one side
and blue from the other, the pressure wave, everyone round them blown away). Not committed (the owner's media); read frame by frame in
the scratchpad. And a picture: an aerial photo of a small round tropical island ringed by a wide white sand beach, a reef and
turquoise shallows round it, deep blue sea beyond (the look wanted for the islands: not committed).

The owner, verbatim:
"Using the very latest build/action/release.

To do list:

1. Reduce Conqueror's Haki Release (g) end lag, when releasing the full charge of haki it causes a ton of lag for a bit then stops.
2. New, Conqueror's Haki Clash from now on whenever 2 players release conqueror's Haki (charged G for at least 2 seconds within
eachother's Haki release range) they collide creating a massive pressure wave between the 2 players lightning of both parties flying
outwards a massive impact in the precise point of collision like the gif shows (that ring on the point of impact), Then a mini game
begins, Whoever has more Conqueror's Haki Mastery has way more ease in this game, a bar appears on the middle of the screen "TAP M1"
whoever manages to get their bar color to the enemy's side wins, the bar has to be really well made and decorated. Note: During the
clash wind is blowing outwards from both players like they're in 1 single massive spherical bubble anything or anyone going close to
this perimeter is blasted away in a ragdoll in a very violent way (go through blocks as well in a horizotal tragectory), at the end
whoever wins sends the other player flying also in a horizontal tragectory, ragdoll and stunned for double the winner's max stun of the
conqueror.
3.  World Generation, big one,  the world now will have our own generation, it supports TerraBlender and Biomes o plenty, the world
will now generate in Arquipelagos (big islands but most of it will be sea) key is that every single river IS CONNECTED TO THE SEA no
matter what, it's meant to be made for a boat travel/exploration type of thing. Make sure Structures spawn in the islands and the SEA
is deeper than normal. (should support every modded structure) and every island MUST have a beach, long.
4.  I want a proper clashing system like our other mod has HexGhoul."

- "Using the very latest build/action/release": the newest was v3.8.0 = the branch's head 91344f3 (the 3.8.0 handoff); built on it.
- The order chosen (each its own version, the owner tests each): 3.8.1 item 1 (the release's lag), then item 2 (the Conqueror's clash),
  item 4 (the clashes as HexGhoul's), item 3 (the world generation).

## 2. The owner's second message this session (verbatim, with two pictures and a zip)

Sent while 3.8.1 was being built (the session's worker restarted under it; nothing was lost). The zip `NewModels.zip` (as sent, never
altered: `input/newmodels/NewModels.zip`) holds three Blender files: `First_gun.blend`, `First_mace.blend`,
`Clothes_Replacements.blend`. The pictures (not committed; described): (1) the anime's Sanji in a blue striped shirt holding a flintlock
pistol out one-handed at arm's length, right arm straight, the gun level (subtitle "Don't you mock me!"); (2) the anime's Nami aiming a
big-bored flintlock pistol one-handed straight at the viewer, the barrel huge in the foreground. Both: the one-handed gun pose wanted.

The owner, verbatim:
"First of all setup a auto wakeup 5 hours from now.

Reworks:
(Only after the world and conqueror haki are done)

Swords in first person are being weilded in a very strange way and unrealistic, I dont Like any of it,  from now on, when grabbing a
blade as a swords man you'll go into third person, forcefully, the camera adjusts to better aid your swings and aim.

Different Animations for Different Weapons.

Katanas Category (stays as it is)
Saber Caterogy (Similar to Katanas but more aggresion, the idle and unsheath animation stay as it is (Gryphon))
Mace Category (Right handed, rests on the back like Yoru, the pull out is slow, when idle the player rests it on it's right shoulder,
has range, hits ragdoll but are slow when cocking them, fast on the swing).
Yoru Category (Yoru is a one of a kind legendary Blade, stays as it is, and is uncontested the strongest blade that will ever be added)
Gun (Pistols, usually held with one hand (right) in a badass pose, all weapons only shoot 1 bullet at a time, and auto reloads after,
bullets are also the same model from now one (I'll send it) when haki is on, bullets come out black (pure black), those bullets are
much much faster and go through blocks (10 blocks max), simple reload animation, put 1 bullet in the hole of the barrel).

Whenever I give you a weapon from now on Ill name one of these categories, write this in every  handoff.

I also will be sending clothes replacements for the ones currently in game.


PS: Make sure guns are lowered in a really relaxed way, pointing down at the floor sometimes playing with them by spinning them on the
hand, running animation, etc (these animations will serve for every gun from now on)

Sort every Katana into the category of katana
Gryphon into saber and yoru into yoru."
(The message ended with "Tente novamente", Portuguese for "Try again": the app's own retry text, not a request.)

- Done at once: the wake-up 5 hours later (a one-time scheduled message to this session, 2026-11-27 03:02 UTC).
- **STANDING RULE, to copy into EVERY handoff from now on (the owner: "write this in every handoff"): every weapon the owner gives
  comes with one of these categories, and it is made by its category's animations:**
  - **Katana**: as the katanas are now (every katana sorted here).
  - **Saber**: like the katanas but more aggressive; the idle and the unsheathe stay as they are (Gryphon is a saber).
  - **Mace**: right-handed, rests on the back like Yoru, a slow pull-out, at idle rested on the right shoulder; has range; its hits
    ragdoll; slow to cock, fast on the swing (`First_mace.blend` is the first).
  - **Yoru**: one of a kind, stays as it is, uncontested the strongest blade that will ever be added (Yoru alone).
  - **Gun** (pistols): held in the right hand in a badass pose (the two pictures); one bullet a shot, reloads by itself after (a simple
    reload: one bullet put into the barrel's mouth); every bullet the same model (the owner will send it); with Haki on the bullets come
    out pure black, much much faster, through blocks (10 at most); lowered in a really relaxed way, pointing at the floor, sometimes
    spun on the hand; a running animation; these animations serve every gun (`First_gun.blend` is the first).
- The other reworks (after the world generation and the Conqueror's Haki items are done): a blade drawn as a swordsman forces third
  person, the camera placed to aid the swings and the aim (first person's sword holding is disliked whole); the clothes replacements
  (`Clothes_Replacements.blend`; more to come).
- The order now: 3.8.1 (the release's lag), the Conqueror's clash (request 1 item 2), the world generation (request 1 item 3), then
  the clashes as HexGhoul's (request 1 item 4) and the weapon reworks above. (The owner put the reworks after "the world and conqueror
  haki"; the HexGhoul clashes were not ordered against them: ask.)

## 3. 3.8.1: the full Conqueror's release's end lag (request 1 item 1)

Measured outside the game at Conqueror mastery 1000 (the scratchpad's bench/StormBench, prof/Heavy, bench/Fis; the original's
HakiVfx read with javap):
- **The fissures' regrowth**: a full release tears 3,600 to 4,700 distinct blocks (51 to 63 chunk columns); each came back at its own
  moment (Scars: RESTORE_MIN 300 + a per-block jitter over RESTORE_JITTER 200 ticks): a few a tick in every section for ten seconds,
  about 2,000 section mesh rebuilds on every client near. **Now** `haki/ScarLedger.sectionJitter(x, y, z, span)` gives every block of a
  16-block chunk section one moment (Scars.tear's default path; the `restoreIn` path, Blast Breath's 30 s ripple, unchanged): ~100
  rebuilds. Tearing unchanged (DESTROY_BUDGET 240).
- **The original's Photon particles**: conquerorRelease's bursts (60+80c, 48+52c, 50+95c, 16+28c, 96, 190, 72, 180, 150; its
  fullChargeConquerorBurst 420+360+460; fullChargeConquerorDome five shells of 125-180 x 4-5 cycles = 3,575; supremePressureFronts;
  three aftershocks 130+90c each): ~7,000 big soft see-through puffs in two seconds. **Now** `client/haki/HakiStorm.thin(fx)` (the full
  release's composition as the patch hands it over, and each full aftershock) sets every burst's count to
  `haki/ConquerorStorm.thinned(n)` (THIN 0.35, bursts of THIN_FROM 24 or fewer whole). Stubs: EmissionSetting.getBursts, Burst (time,
  cycles, interval, getCount, setCount), NumberFunction.get(float, Supplier); verify/api-allowlist.txt 3.8.1 block ([photon] evidence:
  Burst.getCount, NumberFunction.get; the rest the original already calls).
- **The 3D storm**: ~22,000 quads at its peak in third person (tubes 13k along ~4,000 kinks, glow 5k, ink 3k), ~4-10 ms a frame on this
  machine's CPU. **Now** `haki/BoltMesh.heavy` (set each frame by client/haki/HakiBolts.draw from the last frame's quads:
  `BoltMesh.heavy(was, quads)`, over BUDGET 9000 -> heavy, back under LIGHT 6000): a heavy frame draws each stroke through
  `BoltMesh.coarse` (pieces under DETAIL_HEAVY 64 px of a 1080-line screen merged, 2, 4 or 8 points at a time, the ends kept) and
  tubes with at most ROUND_HEAVY 4 faces: ~12,900 quads at the peak. Rendered side by side at the game's own pixel scale (BoltPreview
  stormSheet, the scratchpad's bp/fair3.png): the same bolts and zig-zags. Light frames draw exactly as before.
- Tests (LightningTests): sectionJitter (one moment a section, spread over the span), thinned, BoltMesh.heavy's hysteresis,
  `heavyStorm` (the peak under two thirds of its quads drawn heavy, every bolt still drawn, coarse keeps the ends and its own points).
- Not changed: the storm's bolts, the fissures' shape and timing, the release's sounds, stun and throws.
- Nothing tested in game. Ask the owner how the full release feels now (and whether any lag is left: if so, when: at the release, the
  next two seconds, or 15-25 s later when the ground grows back).
- 3.8.1's runs: Build #268 and Release #172 green (v3.8.1 is the latest release).

## 4. 3.8.2: the Conqueror's Haki clash (request 1 item 2)

- **Rules (pure, tested):** `haki/ConquerorClashRules`: HOLD_LEAST 40 ticks on both sides; `clashes(releaseHeld, chargeHeld, distance,
  rRelease, rCharge)` (distance <= the smaller radius); INTRO 30 ticks (the collision, the bar growing in), GAME_MOST 300 (15 s; then
  whoever is ahead, even: the greater mastery, then A); `weight(mine, theirs)` = 2 (m + 150) / (m + t + 300) (1000 vs 0: 1.69 vs 0.31);
  TAP 0.045 a tap times the weight; `drift` DRIFT 0.004 x the weights' difference a tick (the stronger presses on its own); TAP_GAP 1
  tick; `meeting(v)` 0.5 + 0.3 v along A to B; `bubble(gap)` max(8, gap/2 + 5) round the middle; `caught` within SKIN 2 of it;
  BLAST_SPEED 2.6 a tick level for BLAST_TICKS 14, down BLAST_DOWN 70; the loser THROW_SPEED 3.2 for THROW_TICKS 20 after FINISH 8;
  `loserStun(winnerAlignment)` = 2 x ConquerorStun.most (800 or 1200 ticks).
- **Server:** `server/ConquerorClash`: `charging` (when each began charging, from ConquerorRelease.charging), `held`, `busy`, `begin`
  (from ConquerorRelease.visual at a radial release: the nearest other player charging 2 s+ within both radii
  (ConquerorLightningPath.radius for each); B's charge ended (HakiData conquerorCharging false, charge 0); both faced to each other
  (ServerPlayer teleportTo with yaw), the `conqueror_clash` clip on both, ModSounds.CONQUEROR_SUPREME_BLAST, piece.clash,
  piece.gura.quake, Impact.quake, S2CConquerorClash START), `tap` (C2SConquerorTap), the tick (both held (teleport back past 0.3 blocks,
  velocity 0), the tug from INTRO, S2C TUG every 2 ticks, WON (the winner's supreme_conqueror_release by alignment), after FINISH the
  loser launched + ConquerorRelease.hold for the stun, END; GONE if either dies, leaves or changes world), `wind` (every entity but the
  two, not riding, not a spectator, within the bubble + SKIN: launched outward level), `launch`/`fly` (a Blast: teleportTo along the
  level line each tick, through blocks, then up to the first room (Level.noCollision, 48 at most); a living body knocked down
  (HakiFalls.knockDown, its ragdoll) with the blast's velocity set first so it falls away), `attacked` (no damage while flying).
- **Swallowed releases:** ConquerorRelease.visual (begin, or nothing while busy), apply, forceMax, fissures, sound, `cinematic` and
  `aftershocks` (new patch redirects in releaseConqueror: HakiNetwork.to (the supreme cinematic) and scheduleAftershocks, widened)
  do nothing for a player in a clash; `target` never takes a player in a clash (another's release leaves the two alone).
- **Network:** S2CConquerorClash (id 24; phases START, TUG, WON, END, GONE; both ids, positions, radius, v, colours, masteries, age),
  C2SConquerorTap (25); PROTOCOL 21.
- **Client:** `client/haki/ConquerorClashFx`: accept (START: ClashFx's impact frame on the two (an S2CClash with CLASH|OVERWHELMED,
  power 3) and HakiBolts.clash power 3 at the meeting point; WON: another; END: one at the loser in the winner's colour), tick (the
  bar eased toward the server's, HakiBolts.clash power 2 every 9 ticks at the meeting point), `attack` (M1 cancelled for the two, a tap
  sent after INTRO, a soft clash sound), `draw` (from HakiBolts.draw: ClashArt.impact, two ClashArt.stream, ClashArt.bubble,
  ClashArt.wave), `hud` (ClashBarView for the two; it fades out over 30 ticks after the end). HakiBolts.stage also runs while a clash
  is on.
- **Art:** `haki/ClashArt` (pure): impact (CORE 1.9, bursting to BURST 4.2 at the collision, SIDES 36: a white-hot core, a black ink
  ring (opaque), jagged flares in each half's colour beyond it, facing the eye, split by A's way), stream (16 flowing ribbons of the
  fighter's colour into the impact), bubble (WIND 56 white streaks racing out, three faint rings), wave (WAVE 16 ticks: a ring facing
  the eye racing out to 1.6 x the bubble, half in each colour, its foot a white ring on the ground). About 600-900 quads.
- **The bar:** `ui/ClashBarView` (pure, UiCanvas): at 72% of the screen's height, half width 34% of it (70 to 150); renders:
  tools/preview/ClashBarPreview (docs/images/clash-bar-3.8.2.png).
- **The stance:** tools/conqueror_anims.py CLASH `conqueror_clash` (looping 46 ticks, CLASH_IN 6: braced, fists clenched low and
  flared, leaning in, the head glaring; a tremor, the breath, a surge once a loop); lint clean; rendered and judged.
- Renders: tools/preview/ConquerorClashPreview (docs/images/conqueror-clash-3.8.2.png: the collision, the pressure wave, the holding
  clash, the meeting point pushed each way).
- Tests: LightningTests.conquerorClash (the rules: who clashes, weights, the drift, the tug held at even taps, a lone tapper wins in
  4 s, a novice cannot out-tap an idle master, time up, the meeting point, the bubble, the stun; the art's budget and finiteness).
- Not done / to ask: moves other than M1 are not blocked during a clash (the fruit's keys still work while held in place); the camera
  is left as it is; NPCs never clash (players only, as asked).

## 5. 3.8.3: the archipelago world generation (request 1 item 3)

- **Data, all generated by `tools/archipelago_data.py VANILLA_DATA_DIR`** (VANILLA_DATA_DIR: a folder holding 1.20.1's own
  `minecraft/worldgen/...`: download from misode/mcmeta branch `1.20.1-data` raw files into the scratchpad, never commit; the
  overrides it writes are committed): `data/minecraft/worldgen/density_function/overworld/{continents,erosion,ridges,offset}.json`
  replace the overworld's terrain inputs; `data/hexhaki/worldgen/noise/archipelago.json` (firstOctave -9, amplitudes 1, 1, .7, .4,
  .25), `data/hexhaki/worldgen/density_function/islands.json` (LOWER -0.24 + 0.95 archipelago + 0.5 vanilla continentalness),
  `.../vanilla/offset.json`, `.../vanilla/erosion.json` (vanilla's copies the overrides wrap). Nothing else of the world generation is
  touched (biome source, surface rules, structures, every mod's data): TerraBlender/BOP and modded structures work as on vanilla
  terrain. build.py NEW_PREFIXES has `data/minecraft/worldgen/density_function/overworld/`.
  - continents = spline(hexhaki:islands) through WIDEN (the coast band -0.19..-0.11 spread over raw -0.30..0.0: beaches ~4x wider).
  - erosion = vanilla x EROSION_KEEP(c) + EROSION_ADD(c): in the coast band 0.1 + 0.25 e (erosion index 3-4: beach biomes, never
    stony shores).
  - ridges = hexhaki:river_lines x clamp(1000 x vanilla ridge noise, -1, 1) (the vanilla sign kept for the biome variants).
  - offset = min(vanilla + BEACH_MASK(c) x (BEACH_Y(c) - vanilla), RIVER_CAP(ridges_folded)) + DEEPER(c): the coast band shaped to
    a gentle beach (y 58.5 at c -0.21 to 65.2 at -0.10), the river's channel capped at y 57-58 within |w| 0.04 (rising to 64 by
    0.07, free by 0.14), the sea floor lowered up to 0.27 (deep ocean ~y 8, vanilla ~36). Surface y ~ 128 + 128 x offset.
- **Code:** `world/RiverField` (pure: three families of rivers, each the level lines F = k S of F = x cos a + z sin a + W, W five
  waves (wavelengths 3100..210) whose slopes add to BENDS 0.85 < 1, so F has no top or hollow and no river line can close: every
  river crosses every island coast to coast; SPACING 1500, 1900, 2400, angles 50-70 degrees apart; NARROW 3.0; seeded),
  `world/RiverLines` (the density function type `hexhaki:river_lines`, a DensityFunction.SimpleFunction (compute = RiverField.field
  at the column, 0..1), registered through a DeferredRegister on Registries.DENSITY_FUNCTION_TYPE from HexPiece.init; the seed from
  LevelEvent.Load's ServerLevel.getSeed). Stubs: levelgen/DensityFunction (+ FunctionContext, ContextProvider, Visitor,
  SimpleFunction), util/KeyDispatchDataCodec, Registries.f_256746_, ServerLevel.m_7328_; verify/api-allowlist.txt 3.8.3 block (SRG
  names resolved through Yarn 1.20.1 -> intermediary -> obf -> joined.tsrg; DFU MapCodec.unit [lib]); srg-shapes refreshed.
- **Preview:** `tools/archipelago_preview.py VANILLA_DATA_DIR OUT.png [--seed --size --step --x0 --z0]` evaluates the density functions
  as 1.20.1 does (Perlin/NormalNoise statistics, shift, splines; the river field through the game's own RiverField via
  tools/preview/RiverDump), colours by the estimated height (sand: the coast band on land). Renders kept: docs/images/
  archipelago-map-3.8.3.png (16 km, seed 2: ~30% land), archipelago-island-3.8.3.png (2.5 km: the beaches 20-60 blocks, the rivers
  25-50 wide running coast to coast). Measured: land 20-30%, deep ocean floor ~y 8, the sea's mean ~y 25-35.
- **Tests:** WorldTests (F rises along every family's way on 16,000 sampled lines and its slope never vanishes, for five seeds; the
  field 0 on a river and within 0..1; a river followed 4000 blocks each way goes on; other seeds other rivers).
- To ask the owner: how the islands' size and the sea's share feel, the beaches' width, the rivers' width and spacing (RiverField
  SPACING_*, NARROW; archipelago_data LOWER, ISLANDS, ISLAND_NOISE, WIDEN, BEACH_Y, DEEPER); a new world is needed.
- Not done: vanilla's own river biomes elsewhere (none: rivers come only from RiverField); lakes inside islands may exist (lagoons
  where the island noise dips; they are not rivers).

## 6. 3.8.4: clashes as HexGhoul's (request 1 item 4)

HexGhoul's (read at its tag v4.3.1, cloned read-only into /home/user/hexb1tdev/hexghoul: `control/server/KaguneClash`, `control/ClashPower`,
`control/ClashRules`) and HexPiece's (2.3.6: `server/Clashes`, `clash/ClashRules` already took HexGhoul's 3.4.3 rules: STANDS, GAP 2,
thrown by the ratio) compared. What HexPiece lacked, now done:
- **The clash board** (`server/ClashBoard`, KaguneClash's swing/landed): a blow that is not a style's or a sword's says when it sets
  out (`swing(player, now, lands, reach, hit, kind, live, stop)`, or `swing(...)` returning a `cut` flag for a scheduled hit) and
  `lands(player, cut)` at its landing (Clashes.landing, then off the board). `Clashes.blowOf` falls back to the board, `Clashes.stop`
  stops a board blow. On the board: NekoMoves.m1/land (Six Styles, both forms: kicks KICK, the rest FIST), MochiMoves.punch (FIST)
  and tridentSwing (BLADE), SupaMoves.claw (BLADE), ItoMoves.slash (BLADE), IronFist.punch (FIST). Not on it: Gura's punch (a quake
  shock, not a blow), fruit moves other than M1s, projectiles (HexGhoul's swat of shards: not done).
- **Strength** (`clash/ClashPower`, HexGhoul's 3.2.4 scale): BLOW 5 x power (LOW 1 to HIGH 3 by `progress`: 0.5 the discipline's
  mastery (the sword's for a blade, the fruit's (FruitData.mastery, 0-1000) or the style's for a board blow, else the style's), 0.3
  Armament's, 0.2 Conqueror's) x heft (root of hit / BLOW, 0.3..3) x ARMED 1.25. `Clashes.strength`/`progress` (a sea fighter by
  SeaRules.MASTERY[rank]); `collide` throws by the ratio of the strengths (its power, the burst's size, still from the blows' damage).
- **Overwhelmed** (ratio >= GAP 2): `Clashes.thrown` flings level through blocks (ConquerorClash.launch: FLUNG_SPEED 1.7 a tick for
  FLUNG_TICKS 11) and knocks down (HakiFalls, FLUNG_DOWN 50), piece.iron.boom.
- Tests: ClashTests.power (a master's jab out-weighs a beginner's heavy move, the master barely moves and stands at 2x, a maxed armed
  fighter overwhelms a novice, even is even, Armament counts, heft bounded).
- To ask the owner: whether more moves should be on the board (fruit X/C/V moves, projectiles meeting blows), how often clashes
  happen (ClashRules.WINDUP 3, FACING), whether the strength weights feel right.

## 7. What to do next (in order)

1. 3.8.4 to 3.8.7 green (3.8.7: Build #277, Release #178). Check 3.8.8's runs (the Gun, section 13).
2. The owner's word on 3.8.1 (the release's lag), 3.8.2 (the Conqueror's clash), 3.8.3 (the archipelago: needs a new world),
   3.8.4 (the clashes) comes first: write it verbatim here, fix, render, release.
3. The weapon reworks (section 2; the owner put them after the world and the Conqueror's Haki, both done):
   - A blade drawn by a swordsman forces third person, the camera set to aid the swings and the aim (first person's sword holding
     disliked whole: "very strange way and unrealistic, I dont Like any of it").
   - Weapon categories (THE STANDING RULE below): sort every katana into Katana, Gryphon into Saber, Yoru into Yoru; the Saber's
     more aggressive attack animations (idle and draw as they are); the Mace (First_mace.blend: rests on the back like Yoru, a slow
     draw, idle on the right shoulder, reach, ragdolling hits, slow to cock, fast swing); the Gun (First_gun.blend: one-handed in the
     right hand in a badass pose, one bullet a shot and a reload putting one bullet in the barrel's mouth, the bullet model the owner
     will send (ask for it if it has not come), Haki on: pure black bullets, much faster, through up to 10 blocks; relaxed lowered
     poses pointing at the floor, spinning it on the hand, a running animation; for every gun).
   - DONE: forced third person (3.8.5), the categories and the Saber (3.8.6), the Mace (3.8.7), the Gun (3.8.8).
   - The clothes replacements (Clothes_Replacements.blend; more to come): ask the owner which armor piece each replaces.
   - The Saber's kesa and makko and the Mace's gyaku yoko and plunge written for their own grips (they fall back to the katanas').
   - The Blender files are in input/newmodels/NewModels.zip (unpack into the scratchpad; Blender's Python module as for the
     leopard: python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy).
4. (The Leopard is gone for good: section 18. Never its moves.)

## THE STANDING RULE: weapon categories (the owner: "Whenever I give you a weapon from now on Ill name one of these categories, write this
in every handoff")
Every weapon the owner gives comes with one of these categories and is made with its category's animations:
- **Katana**: as the katanas are now (every katana is a Katana).
- **Saber**: like the katanas but more aggressive; the idle and the draw stay as they are (Gryphon is a Saber).
- **Mace**: right-handed, rests on the back like Yoru, a slow pull-out, at idle rested on the right shoulder; has range; its hits
  ragdoll; slow to cock, fast on the swing.
- **Yoru**: one of a kind, stays as it is, uncontested the strongest blade that will ever be added (Yoru alone).
- **Gun** (pistols): right hand, a badass pose; one bullet a shot, reloads by itself (one bullet into the barrel's mouth); every bullet
  the same model (the owner sends it); Haki on: pure black bullets, much faster, through blocks (10 at most); relaxed lowered poses
  pointing at the floor, spun on the hand, a running animation; these animations for every gun.

## 8. 3.8.5: a drawn blade forces third person (the first weapon rework)

- `client/sword/SwordCamera`: each client tick, one's own blade drawn (SwordClient.katana + SwordAnims.drawn: the guard between
  cuts counts as drawn) and the view first person -> THIRD_PERSON_BACK (again whenever picked while drawn); sheathed and it came from
  first person -> FIRST_PERSON. `position(entity, partial)` (asked by client/dragon/DragonCamera.position, the original's camera
  hook, before Gear 5's giant): third person from behind, eased in by GLIDE 0.2 a tick: SHOULDER 0.85 to the right, UP 0.3, BACK 3.1
  (the game's 4), clipped by DragonCamera.clip. Nothing rendered (a camera place, not a model or a clip).
- Next: the categories (THE STANDING RULE), the Saber's more aggressive cuts, the Mace, the Gun, the clothes (section 7).

## 9. 3.8.6: the categories and the Saber's cuts

- `sword/Katana.Category` {KATANA, SABER, MACE, YORU, GUN} (label), `Katana.category()` (GRYPHON SABER, YORU YORU, every other KATANA);
  `kindLine()` leads with the label ("Katana  ·  Hamon: ...", "Saber  ·  ...", "Yoru  ·  Kokuto ..."). SwordTests.categories.
- The Saber's cuts: tools/sword_anims.py `SABER` (Blade "saber", Gryphon's mesh "gryphon", suffix "_saber", the katanas' home pose and
  noto), build() writes `moves(False)` again with `AGGRESSIVE`: `aggress(beat)` (a copy) body yaw x AGG_TURN 1.22, forward bend x
  AGG_BEND 1.15 + 2, lunge z x AGG_LUNGE 1.22, crouch y x AGG_CROUCH 1.12, head pitch + AGG_GLARE 3; timing, blade targets and easings
  kept. AGG_SKIP {sword_kesa, sword_makko}: with Gryphon's mesh those two brush the body (0.8 / 1.0 px) even unaggressed (the katana
  clips Gryphon plays for them in game already do); to write them for the saber's grip, adjust their wind and overhead beats for
  the bigger handle. 14 `sword_<cut>_saber.json` solved (`python3 tools/sword_anims.py --only <names>`, ~1 min a move on 4 cores);
  BladePaths regenerated without re-solving (`SOLVE=False; build(); java_paths(); java_npc_poses()`: the 30 old paths byte-identical,
  14 added; NPC poses exclude "_saber"). build.py's sword pose check: 44 animations clean.
- `SwordAnims.name(anim, katana)`: a Saber with no suffix of its own -> `saber(base)` (cached: base + "_saber" where BladePaths has it,
  else the katanas'). SwordTests: a katana never plays a Saber cut, every Saber cut has its path and the katanas' timing, the guard,
  draw and sheathing stay.
- Render: docs/images/saber-yoko-3.8.6.png (the katanas' yoko over the Saber's, three-quarter and side).
- Next (section 7): the Mace and the Gun from input/newmodels/NewModels.zip, the clothes; the Saber's kesa and makko.

## 10. The owner's Blender files, opened (bpy 5.3 in /tmp/bpy; the scratchpad's inspect_blend.py lists them)

- **First_gun.blend**: a flintlock pistol, cel-shaded by materials only (no images): `Flintlock • grip pivot` (an empty, the grip's
  pivot: every part its child): `Brass barrel • stepped crown` (1904 verts; 'Cel • lemon brass', 'Front rim • reflected sky'), `Brass
  collar`, `Recessed collar at stock`, `Chestnut stock` ('Cel • chestnut'), `Gilt side plate` x2, `Wide gilt guard`, `Underslung rod
  and rolled end`, `Rod end • inset`, `Dark muzzle face` ('Muzzle • inner shadow'), `Rear lock thumb disc`, brass pivot medallions x4,
  gold lock pins x8, pin markings and pivot recesses ('Ink • sepia'); ~130 CURVE objects drawing the ink lines (Inset lip hatch, Rim
  hatch, Stock grain, Longitudinal ink, Pivot ink ring, Rod cap mark, Lock accent, Gold lever, Gold trigger silhouette, Inlaid sweeping
  line): convert them to meshes (bevelled curves) or bake them as dark strips. About 2.2 units long. **`Cast lead ball`** (child of
  `Round shot • pivot`, 'Cel • round shot', 0.31 across): the owner's bullet ("bullets are also the same model from now on (I'll send
  it)": this looks like it; confirm with the owner). Cameras: 'Camera', 'Profile • full silhouette'.
- **First_mace.blend**: `Continuous tapered iron body` (2304 verts, 0.9 x 0.9 x 5.05, 'Blued black iron'), spikes in rows (Spike 01.1
  to 03.x, 0.18 to 0.31 tall), `Suspension ring` at its end: a black spiked iron club (kanabo-like). Lights and a camera only else.
- **Clothes_Replacements.blend**: four outfits, each on its own rig (10 bones): Marine uniform, Admiral coat, Straw hat, Blackbeard coat
  (cameras '0N / <outfit> / Front|Back'; meshes: bare arms, boots and welts, brass buttons with gilt faces, a blind turned hem, ...):
  replacements for the clothes in game (PieceArmor / armor models): map each to the armor piece it replaces (ask the owner which).
- The plan for the Mace and the Gun (not started): bake each to the blades' HKM2 mesh (tools/katana_mesh.py's format; write an
  exporter like tools/leopard_bake.py: bpy -> triangles with per-material colours into a small atlas), add the weapon as a Katana
  entry of category MACE / GUN (or its own item if the sword system does not fit a gun), its poses and moves (sword_anims.py: the
  Mace's back carry like Yoru's, the slow draw, the shoulder idle (sword_anims carry() has a shoulder pose), slow cocking then fast
  swings: its own timing, so its own SwordMoves or a per-category time warp with matching hit windows; the ragdoll on hit:
  HakiFalls.knockDown), the Gun's (right-hand poses, the relaxed lowered idle, the spin, the run, the reload, one bullet a shot, a
  bullet entity: the lead ball, black and faster through up to 10 blocks with Haki on).

## 11. The owner, after 3.8.6's summary (verbatim)
"Never stop i mean it"
- STANDING: keep working through the list without pausing for a reply (the Mace, the Gun, the clothes, the Saber's kesa and makko,
  then the Leopard's moves), releasing each step; at 98% weekly usage write the handoff and next prompt first, push and stop.

## 12. 3.8.7: the Iron Mace (category Mace)

- **Model:** tools/mace_bake.py (bpy: /tmp/bpy/bin/python tools/mace_bake.py) bakes First_mace.blend (unpacked from
  input/newmodels/NewModels.zip) into art/mace/iron_mace.obj (2546 triangles: the body decimated to DECIMATE 0.32, each spike to
  SPIKE_FACES 40, the ring RING_FACES 160; Blender z -> game +y, y 0 at GRIP_Z 1.35 (the handle's top), LENGTH 40 px end to end:
  y -10.35 to 29.65; groups 01_Blade_iron, 02_Spikes_iron, 03_Ring_iron) and art/mace/iron_mace.png (a 128 atlas: the body round its
  axis on the left half, blued black iron with grain and a cold sheen; the spikes base to tip worn to steel; the ring). katana_mesh
  MACES / mace_meshes / mace_textures / pack_maces (with LODs); sword_view.mesh/texture read it; katana_render.py mace-icons (a pale
  steel-blue outline: a dark one vanished into the black) -> textures/item/katana/iron_mace_icon.png. build.py mace_assets (mesh +
  _mid + _far, atlas, item model).
- **Katana.IRON_MACE** ("iron_mace", "Iron Mace", Grade SAIJO_O_WAZAMONO, Carry.BACK, edge 1.65, reach 0.6): category MACE, legend
  (worn on the back, drawn whenever you fight, never carried by sea patrols), not blackened by Armament, tip 29.65, kindLine "Mace  ·
  spiked black iron  ·  throws what it hits"; PieceItems' tab after Yoru; lang item.hexhaki.katana_iron_mace.
- **Back carry generalised from Yoru:** SwordAnims.suffix "_yoru" for every Carry.BACK weapon's draw/sheathe/charge draw (the mace
  comes off the back with Yoru's slow draw), CHARGE_DRAW mapping, SwordRules.drawTicks/sheatheTicks.
- **Its clips** (tools/sword_anims.py MACE profile, MACING: `mace(move)`: body yaw x1.1, bend x1.2, crouch x1.2; the beats before the
  first cut eased "inoutsine" (cocked slowly), the first cut's beat "inexpo" (accelerating into the hit): the hit ticks unchanged;
  carry() for MACE: sword_guard_mace (walking: on the right shoulder) and sword_idle_mace (140 ticks: on the shoulder, sinking with the
  breath, heaved up once and let settle); MACE.home = Yoru's; SHOULDER["mace"] = Yoru's). SwordAnims.name -> own(base, "_mace") where
  BladePaths has it; idle(MACE) -> sword_idle_mace. java_slides skips iron_mace (no saya).
- **Server (SwordServer):** MACE_CHAIN 5 more ticks before a buffered next blow, MACE_REST 8 after a string (slow to cock); `strike`:
  a mace's hit thrown MACE_THROW 1.8 harder (at least 0.35 up) and knocked down MACE_DOWN 30 ticks (HakiFalls: the ragdoll).
- MACE_SKIP {sword_gyaku_yoko, sword_plunge}: with the mace's thick head the gyaku yoko passes 0.4 px through the head, the plunge's
  arm flips between ticks 6 and 7, even with the katanas' body: the mace plays the katanas' there (SwordAnims.own falls back). 16
  `_mace` clips (14 cuts, the guard, the idle) solved and clean; BladePaths 60. build.py MACE_KEYS (katana_keys), sword_view skips a
  saya for maces. Renders: docs/images/mace-model-3.8.7.png, mace-idle-3.8.7.png, mace-yoko-3.8.7.png.
- SwordTests: 28 blades; the mace a Mace on the back, never above Yoru (edge, reach), Yoru's draw and sheathing, its idle and guard
  on the shoulder, its draw as slow as Yoru's.
- 3.8.7's runs: Build #277 and Release #178 green.

## 13. 3.8.8: the Flintlock Pistol (category Gun)

- **Model:** tools/gun_bake.py (/tmp/bpy/bin/python tools/gun_bake.py) bakes First_gun.blend into art/gun/flintlock.obj (4255
  triangles: every mesh under "Flintlock • grip pivot", MESH_FACES 420 a part, SMALL_FACES 24 under SMALL, pin markings under TINY left
  out; the ink curves bevelled at least INK_LEAST; frame: the grip's pivot at 0, the barrel forward -z, up +y, SCALE 4.2 px a unit:
  16.7 px long), art/gun/lead_ball.obj (the bullet: "Cast lead ball", 96 triangles, 1.3 px) and art/gun/gun.png (a 64 atlas: each
  material a row of its own toon ramp; each corner's u where KEY lights it). katana_mesh GUNS/BULLET/gun_meshes/gun_texture/pack_guns
  (LODs for the gun); build.py gun_assets -> assets/hexhaki/gun/*.hkm, textures/gun/gun.png, models/item/gun_flintlock.json (icon:
  katana_render.py gun-icons -> textures/item/gun/flintlock_icon.png). Mesh points: TRIGGER (0, -0.3, -2.8), MUZZLE (0, 2.2, -15.1).
- **Code:** gun/Gun (enum: FLINTLOCK; SCALE 0.7/16, BULLET_SCALE 2.6/16; category GUN), gun/GunItem (plain Item, anyone's; tooltip
  category first), gun/GunRules (AIM 3, RELOAD 26, RELOAD_FROM 6, SPEED 3.2, HAKI_SPEED 9.5, GRAVITY 0.012, RANGE 110, PIERCE 10,
  DAMAGE 7, HAKI_DAMAGE 10.5, the muzzle 1.1 ahead / 0.37 right / 0.17 below the eye, AIM_SLACK 25, the spin SPIN_AFTER 120 /
  SPIN_EVERY 260), gun/GunAnims (NONE, HOLD, DRAW 16, RELOAD, FIRE + bucket: 7 buckets 20 degrees apart from -60; clip names; the
  ball in hand BALL_FROM 4..BALL_AT 12, into the muzzle to BALL_HOME 15), gun/GunSounds. server/GunServer (the state machine: draw ->
  hold; M1 from the hold, loaded -> FIRE bucket; the shot at AIM; RELOAD at FIRE_TICKS 9; loaded at its end; put away empty, it
  reloads after the draw; sounds LOAD at BALL_AT, COCK at the end), server/GunShots (balls flown on the server: a plain ball's line
  clipped by blocks, a Haki ball walks its line 0.2 a step through PIERCE solid blocks; the first body on the line is hit; ConquerorAura.
  mixed at the muzzle and at a Haki ball's impact: POINT ONE). network C2SGun (28: the point under the crosshair), S2CGun (26: state),
  S2CGunShot (27: FLY / STOP), PROTOCOL "22". Client: client/gun/GunClient (layer piece_gun, priority 925; idle/walk/run/spin by the
  body; turns the body to the look while firing), GunLayer (the gun in the hand: the same chain as tools/gun_anims.py gun_matrix:
  fist, FINGER (0, 1.96, 0.21) + the clip's rightItem move, its rightItem turn, Ry(180) Rx(90), the trigger on the finger; the ball
  in the left fingertips and into the muzzle; muzzle flash: debug quads, alpha-blended; smoke: the game's cloud), GunRenderer,
  GunBullets (balls drawn as the server flies them, streaks, sparks/puffs at the stop), GunItemRenderer (ground, frame, first-person
  hand), GunInput (M1 with a gun: the crosshair point from the camera's centre line), GunSight (the third-person sight, the reload
  bar: rendered mock checked on sky, grass and pale ground). SwordCamera.drawn: a gun in the hand forces third person too.
  KatanaMesh.read, KatanaRenderer.quads/tinted, SwordLayer.channel/bend/bendAbout, SwordFx.world made public for the gun.
- **Clips:** tools/gun_anims.py (python3 tools/gun_anims.py [--preview dir]; ~6 s): gun_idle (80, loop), gun_walk and gun_run (the
  right arm's pitch left to the game's swing: only its yaw/roll/bend and the item keyed), gun_spin (30: two turns round the finger,
  SPIN_SIDE -2.5 px out beside the forearm, never through it), gun_draw (16: one turn), gun_fire_0..6 (9: solved so the barrel lies
  along the look; the recoil softer aimed steeply up), gun_reload (26: the right arm solved to hold the muzzle at (-0.05, 1.45, -0.42)
  tipped up-left; the left hand solved along a path from the belt up before the body to the muzzle and back: rig.solve_left_at, the
  natural-arm limits). check() fails an aim off the look by 4 degrees, an arm strained past 12 (the spin's hand turns exempt), the
  ball off the muzzle at BALL_AT. Renders: docs/images/gun-model-3.8.8.png, gun-fire-3.8.8.png, gun-reload-3.8.8.png, gun-spin-3.8.8.png.
- **Sounds:** tools/gun_sounds.py (CC0, Freesound): shot1 (bruno.auzet "old musket bang", cut on the bang), shot2 (mlsulli "Musket
  Shot"), boom1 (fennelliott "musket.wav", the Haki shot's boom), cock1 (N3pomuk's old flintlock click), load1 (jriches1's ramrod),
  spin1 (GrayEpic's gun spin); the hum lines filtered (web_sounds.HUM); tonal_lines: +6 to +8 over noise, as the accepted whip crack
  and clash impact. A ball striking: the clash impacts. **Network:** this session moved onto the owner's "HexPiece" environment (it
  allows freesound.org); the Default one blocks it.
- **Ask the owner:** whether "Cast lead ball" is the bullet model they meant ("I'll send it").
- Tests: GunTests (the rules, the buckets, the clip names and lengths); SwordTests' sound checks cover sounds/piece/gun.
- 3.8.8's runs: Build #278 and Release #179 green.

## 14. The owner, during the clothes work (verbatim)
"Set a wake up in 1 hour and 5 minutes from now
The arms cannot clip through the coats, when walking the coat should be pushed back like the wind is pushing it"
(then: "Atingi meu limite de uso enquanto você trabalhava, mas ele já foi reiniciado. Por favor, continue de onde parou." = their usage
limit was hit and reset: continue.) Then: "reschedule the wake up to 5 hours from now": the 1 h 5 min one deleted; wake-up
trig_01BGaVyTLFeBy7CqJDHWdWE9 at 13:03 UTC 2026-11-27.

## 15. 3.8.9 (in progress): the owner's clothes replacements
- tools/clothes_bake.py (/tmp/bpy/bin/python tools/clothes_bake.py) bakes Clothes_Replacements.blend: the four outfits stand on
  mannequins (rigs of 10 bones in Minecraft pixels, each turned 15 degrees for the file's cameras: everything is taken in the rig's own
  frame) and each mesh is bound wholly to one bone. Pieces: marine_cap (Marine cap), marine_shirt (Tunic, sleeves, rolled cuffs,
  sailor collar, neckerchief), marine_trousers (Trouser), marine_boots (Boot), admiral_coat and blackbeard_coat (the coat parts, not
  the mannequin's Jersey), straw_hat; the mannequins' skin, eyes, hair, nose, lips, neck, jersey stay out. Evaluated with the owner's
  Solidify (cloth thickness), the Bevels left off (sub-pixel, six times the faces). DECIMATE: cap 900, hat 1300, epaulettes 45%.
  fit(): the mannequin's arms are 2.9 px deep and 0.3 px in, its head 7.85 wide and 0.68 higher, its legs 0.2 out: each part's
  cloth is carried onto the game's boxes. Groups (each in its part's frame, model px from its pivot): head, body_upper / body_lower
  (split at y 6, the body's bend), each arm's upper/lower (4 px down: the elbow), each leg's upper/lower (thigh/shin bones).
- The look: each material is a two-colour speckle (white noise of the surface's own place floored 1.07 cells a px: Minecraft-sized
  cloth pixels) and three carry a print (JUSTICE on the admiral coat, MARINE and the gull on the tunic's back, the cap's lettering:
  their masks mixed by alpha): baked into art/clothes/clothes.png (1024 x 512): big faces their own texel patch (DENSITY 2 a px,
  PRINT_DENSITY 8), small faces one texel of their material's palette row.
- tools/clothes_view.py renders them on the rig as the game will (the skirt bend: below the hips each point of a coat turns about
  the hip line with the legs' swing, blended by side and by depth: no tearing). katana_mesh CLOTHES / clothes_meshes /
  clothes_texture / pack_clothes (with LODs, the groups kept).
- DONE as 3.8.9: build.py clothes_assets (assets/hexhaki/clothes/<piece>.hkm with LODs, textures/clothes/clothes.png);
  client/armor/ClothesModel (a HumanoidModel with an empty part tree, prepare() copies the layer's pose; rigid groups on their parts
  with the forearm/knee bends (SwordLayer.bend), the body's bend undone for body_lower and the legs, slim arms narrowed 0.75 about
  the slim box's middle; coats through armor/CoatCloth per vertex in model px, drawn from the root (bent pose for the chest and
  sleeves, unbent for the hips), face normals recomputed with the owner's winding); ArmorClient returns it;
  PieceArmorItem.getArmorTexture -> PieceArmor.CLOTHES_TEXTURE. The coat rule (armor/CoatCloth.java = tools/coat_cloth.py):
  (1) wind: back = forward speed / WIND_SPEED 0.26 (eased, per entity, ClothesModel.wind), each point turned back about the shoulder
  line by BACK 1.0 x depth^0.75, the open front panels swung out round the sides (OPEN 1.1 about the verticals at x +-8.6), flutter;
  across for sideways; (2) the torso's front and the legs' wedges keep the cloth off the body; (3) keepOut, three rounds of: the ring push (the collar as a ring round the neck in 72 sectors:
  the head's box (grown 0.35, sampled 25 a side) reach at the collar's heights against the collar's inside at rest per sector, the
  difference (less 0.25) max-filtered over 2 sectors and smoothed, each collar point (rest y < 2, fading) set at least that far out
  past its rest radius: cloth and lining together, no folding; idempotent) and the arms' push (upper and forearm boxes, no margin over
  the shoulder; each point out along the way it lay from the arm at rest, by ray exit); the sleeves (right_sleeve/left_sleeve groups) are never pushed
  (the owner: "the arms should never interact with the coat's arms"): hinged at the shoulders, flared 0.17 rad out, blown back
  SLEEVE_BACK 1.25. The bake fits the coats onto the game's arms at rest (arm_fit) and opens the collars round the game's head
  (collar()), so at rest the push moves nothing (ArmorTests checks it, and that no pose leaves a coat point in an arm or the head).
  Icons re-rendered (clothes_view.icons). Renders: docs/images/clothes-3.8.9.png, coats-walking-3.8.9.png, coats-head-arms-3.8.9.png.
  The old OnePieceArmorModel is still generated (unused).
- The owner, after that (verbatim): "the arms should never interact with the  coat's arms" and "see what I mean?" with two pictures:
  Garp walking, his coat worn over the shoulders, blown straight back from the shoulders like a flag, its empty sleeves streaming
  back with it, his arms swinging free under and in front of it. So: the coat's sleeves are the coat's (never on, carried or pushed
  by the arms); walking, the whole coat, sleeves too, is blown back from the shoulders.
- The owner, with a crop of the coats' back renders (the collar ringed): "head is clipping through the coat": the standing collars were
  shaped round the mannequin's smaller, higher head; they must clear the game's 8 x 8 head, and a turned or raised head must not cut them.
- The owner, with a crop of coats-head-arms-3.8.9.png (head turned 50 degrees and raised; the collar crumpled behind it, its lining
  showing in spikes): "the fuck is this mess behind the head". The per-point head push folded the collar: replaced by a ring push
  (the collar moved out as a whole, direction by direction, eased round the neck; cloth and lining together).
- 3.8.9's runs: Build #279 and Release #180 green.

## 16. 3.9.0: the Saber's kesa and makko, the Mace's gyaku yoko
- tools/sword_anims.py: SABER_COIL (strike() takes the Saber's own coil for sword_kesa: fist H(6.4, -4.4, 2.0); sword_makko: fist
  H(6.8, -6.0, 1.6), blade and edge as the katanas'): the katanas' coils put Gryphon's arm 0.8 px through the body (kesa, tick 1.5)
  and its pommel 1 px into the brow (makko, ticks 4-5) whatever the aggression. AGG_SKIP empty: all 14 Saber cuts its own.
  MACE_COIL: sword_gyaku_yoko from H(-1.2, 8.0, 6.4) (the katanas' BACKHAND_COIL put the mace's head 0.4 px through the head).
  MACE_SKIP = {sword_plunge}: its arm flips 117 degrees between ticks 6 and 7 with the mace's grip, heavy or not: give the mace's
  plunge a beat between them (its own Move) to write it. BladePaths regenerated without re-solving (SOLVE False, build(),
  java_paths(), java_slides(), java_npc_poses()): 63 paths. SwordTests: Gryphon's kesa is sword_kesa_saber.
  Renders: docs/images/saber-kesa-3.9.0.png, saber-makko-3.9.0.png, mace-gyaku-yoko-3.9.0.png.
- The owner, after the mace's backhand render (verbatim): "I think the swing needs more leverage" (taken as the mace's swings: the
  body loading into them: wound further back round the arc, twisted harder into the coil, lower, turned further through the hit).

## 17. 3.9.1: the mace's leverage
- tools/sword_anims.py: MACE_ARC 1.18 (strike(): the coil widened ARC_WIND x MACE_ARC round the arc), mace(): the coil's beats
  (0 < tick < the first cut) yaw x MACE_COIL_TURN 1.32 and crouch x MACE_COIL_CROUCH 1.35, the cut's and after yaw x MACE_THROUGH
  1.18 (on top of MACE_TURN/BEND/CROUCH). MACE_EXCEPT (rules arc, coil_turn, coil_crouch, through): kesa {arc 1.06, coil_crouch 1.0,
  coil_turn 1.15} (its arm crossed the body, its end hit the floor), makko {arc 1.0, coil_crouch 1.0}, yoko {arc 1.08, coil_crouch
  1.0} (the floor), pivot_cut {coil_turn 1.0, coil_crouch 1.0, through 1.0} (the left arm). All 15 mace cuts re-solved and clear.
  Asked in the notes whether the owner meant the Saber's swings too. Renders: docs/images/mace-gyaku-yoko-3.9.1.png, mace-yoko-3.9.1.png.
- 3.9.0's runs: Build #280 and Release #181 green. 3.9.1: Build #281, Release #182 (check them).
- The Mace's plunge stays the katanas': its drive is one tick (6 to 7), so no beat can go between (playerAnimator ticks are whole);
  writing it means its own plunge Move with the drive over two ticks and the server's cut window moved with it (SwordMove timings).
- The owner (verbatim): "No I did not mean the saber, and yes the fullet is in the first gun model": the leverage is the mace's only
  (the Saber stays as it is); the bullet is First_gun.blend's "Cast lead ball" (confirmed: every gun's bullet, as 3.8.8 made it).

## 18. THE OWNER: THE LEOPARD IS GONE (verbatim)
"I said and will say it again Leopard is to fully disappear, entirely, gone"
- The Neko Neko no Mi, Model: Leopard (3.7.9 on) removed entirely: its item, its moves, its forms, its code, its assets, its names,
  every mention in game text; never "the Leopard's other moves" again. (Section 7 / next-prompt pointed at its other moves: wrong.)
- Done as 3.9.2: deleted neko/ (LeopardArt, LeopardForm), client/neko/ (NekoClient, NekoFx), server/NekoKit, NekoMoves, NekoServer,
  assets/hexhaki/neko/lucci.sry, textures/piece/neko/, the item model and icon, the 7 neko_* animations, the lang name,
  tools/leopard_bake.py, neko_anims.py, neko_icon.py, tools/preview/LeopardPreview.java; PiecePatch's NEKO fruit (the last added
  enum constant: no other ordinal moves); its hooks in FruitRanks, HexPiece, CombatMode, DragonCamera, UoFx (kinds 420-439),
  Transformed, HandMoves; DragonTests' leopard checks; build.py's prefixes. A saved world where someone ate it still loads: the
  original reads the fruit by id (Fruit.byId) and an unknown id is no fruit; the item, unregistered, drops out of inventories.
  build.py check_leopard fails if the jar names leopard, neko or lucci anywhere; CLAUDE.md has the rule. The owner's own upload
  input/neko/ is left untouched (never alter the owner's files): nothing reads it.
- The mace plunge, tried with its drive over two ticks (tools/sword_anims.py: a beat at 7 when MACING, the drive at 8, inside the server's
  6 to 9 window): the arm then flips between 7 and 8 (121 degrees): the flip comes from the mace's final point-down pose itself (its
  grip), not the drive's speed. MACE_SKIP keeps {sword_plunge} (the katanas' plunge); next: a different end pose for the mace (the
  point down with the palm turned the other way, or the left hand on the haft), then re-solve.
- 3.9.1's runs: Release #182 green; its Build #281 cancelled by the handoff push, Build #282 (the same code) green.

## 19. The owner's new fruit (verbatim, with Saint_Marcus_Mars_Itsumade.blend and a picture of the fruit)
"New Fruit:

Name: ????
Rarity: Mythical
Type: Zoan

Animations needed:
Open/Close Mouth
Ambient Animation
Landing Animation
Walk animation (scary/disturbing)
Flying slow
Flying really fast
Glide
Bite

Body changes:
All fire should feel alive, not particles, the actual model should  act like Tori Tori No Mi (only on the firey mane, black ribbon, tail and cheeks.

Moves:
Only the transformation for now.
Z - Transforms  (DIsturbing progressive transformation)"
- The files, kept as sent: input/itsumade/Saint_Marcus_Mars_Itsumade.blend, input/itsumade/fruit.webp (the fruit's picture: a carved
  brown fruit with swirls under green leaves, wreathed in black-purple flame).

## 20. The owner's in-game list (verbatim; sent while the new fruit's model was being baked; with three pictures)
"1. The coat's textures are completely fucked.
2. The mace right click should  keep the same animation however, instead of a blade slash/cut, it creates a crater (with haki on and
lightning from conqueror).
3. The mace's Texture is flickering repeatedly nonstop and you do not need to be a swords man to use the mace or pistol.
4. Mace swings should be wayyy slower.
5. The pistol bullets with haki on are not going through any blocks ( I asked to go through at least 10 blocks)
6. The pistol isnt being held right, it's resting in the hand instead of being held properly
7. The world generation I asked for I truly dont see it, I created a new world and I see no islands? It could be my phone Im not sure
but its one of them.
8. IF you could fix the head of my "????" fruit so it's 1 singular thing instead of a broken looking head id be thankful."
- Picture 1 (in game, third person from behind): the player holding the pistol at the thigh, the barrel hanging straight down out of
  the fist like a stick; a green line drawn from the hand: a hook at the hand turning down along the gun (the grip should sit in the
  hand, the gun held as a pistol is held, not resting in the fist).
- Picture 2 (in game, from behind): the player in a coat (white, gold and maroon) whose texture is noise (garbled speckles), the black
  spiked mace (Haki on) over the shoulder.
- Picture 3: the anime's Itsumade head (the target for 8): one head: a long hooked yellow beak open with teeth, a red mask over the
  eyes with a blue eye, the brown mane swept back, an orange flame at the cheek.

## 21. The owner, while the list in 20 was being fixed (verbatim, newest last)
- "you cannot look at the current gun holding and call it good bro wtf is that, is he holding a dildo?" (with a crop of the gun_idle
  preview, three-quarter front: the gun tipped forward-down out of the bottom of the fist; a green "7" drawn over it: a short bar
  across the bottom of the hand, the long stroke down along the leg). Lesson: judge renders as hard as the owner does; a cylinder
  pointing out of the fist reads wrong.
- "Gun handle goes where the green circle is" (the same preview, a green circle on the lower front of the right arm, at the fist):
  the grip in the middle of the fist. Done: tools/gun_anims.py held(): the relaxed holds (idle, walk, run) keep the grip's pivot (the
  gun's origin, in its handle) at the fist's centre whatever the wrist's cock; idle pitch -25 (walk -31, run 9): the barrel out of the
  front of the hand, pointing ahead and down at the floor.
- "also the fiery mane of my model Is wrong I just noticed, it should follow the green line" (with a crop of the back view of the
  Itsumade: a green line drawn down the middle of the tail's back, from under the body to the tail's fire): the flame-edged dorsal fin
  and its flicks run down the tail's side edge; they belong on the middle of its back (the dorsal line).

## 22. THE OWNER'S ITSUMADE SPEC (verbatim; sent twice, the second time with "Tente novamente"; with Reference.zip and four pictures)
Files kept as sent: input/itsumade/Reference.zip (Haki_Color.gif, Yokai Beam.gif, Haki_and_Portalfire.gif) and input/itsumade/pictures/
(head_from_above_manga.png: the manga's Itsumade from above (the head: a long beak open with teeth, the face with black markings, the
white mane swept back; the black cloud ribbon; the wings); yokai_beam_frame.png (a cyan glow with dark shapes); pentagram_frame.png (a
glowing cyan pentagram in a ring of runes on the ground, dark blue lightning round it); haki_lightning_frame.png (a thick dark blue
lightning bolt with a pale core)).

"1. Fruit "????" Renamed to "Itsumade"
2. I also  sent an image of the head from above to aid in making it perfect.


Itsuamade Modes:
Passive:
The user cannot be killed, unless the attacker has Ryou Haki activated, every time the user is about to die he vanishes in a black smoke
and appears somewhere safe 10 blocks away with full HP and Energy.

The user's conqueror's haki  (G) can now kill and changes color to the exact color scheme of the reference I attached in the ZIP called
Haki_color. This changes every color of the user's haki including ryou to be a greeish/blueish scary tone.

Untransformed:
Default minecraft M1

Z - Transform / Untransform
X -  Gorosei Stare (Focus your sight on a target and send them flying.)
C - Enourmous Presence (Your body unleashes conqueror's haki automaticly for 10 seconds,  once in a while a lightning comes out of the
body stunning anything it touches.)
V - Yokai Blast (Point your hand forward and charge a beam of bright orange/black flames, cannot be aimed after release, beam continues
for a minimum of 1 second and at max charge 5 seconds, the flames are super clean, beautiful effects like the dragon's breath but more
intense,  goes through a maximum of 15 blocks at max charge and 5 blocks at minimum then self repairs)
B- Thy Death: Conqueror's haki lightning fall from the clouds down to the floor (making sure it always hits the ground) for 10 seconds
in a circle around the player (25 block radius) when touching the floor the lightning explodes into an AOE of a sphere with 3 diameter,
stun + damage.
N - Hellish Teleport: a black/scary menu appears on the user's screen, 3 Buttons on the menu(also custom made and cool looking) each
button acts as a location save point clicking 1 of those buttons will save the current player location then you gotta figure out a way
to clear a button and reset the location to another spot. rename a teleport area, etc.

When clicking a location a petagram 6x6, will start forming on the floor that adapts to terrain, after it fully forms (takes around 10
seconds), anything or anyone standing on it is teleported to the saved location.

Teleport style: the people and mobs/ entities, all turn into black flames and all of them come together at one point and blast into
the sky from the center of the pentagram.
On Arrival, the beam falls from the sky  onto another landing pentagram on the saved location (the 2 pentagrams form at the same time,
landing and entry, and everything  is visible like falling from the sky , going up to the clouds etc) has to be a spectacle to behold.
The pentagram should also have blue/pink fire around it like the reference



Transformation:

Size: 10 blocks tall, big wing span.
M1- Wind Gust: a slow move, the user's wings flap to create massive wind gusts that push away every entity backwards that is in the
cone infront of the user.
M2- The user grabs with his talons whatever entity and is free to carry them endlessly. (only while flying) (Recasting M2, drops them).

Z - Transform / UnTransform
X - Bow : The user's massive transformed body start emmiting conqueror's haki lightning stunning anything they hit for a period of 15
seconds (THe lightning never comes from the inside of the transformation model).
C - Enourmous bite : The user lunges it's massive neck forward biting where he looked at, causing damage, bleed and broken bones (our
effect from gura)
V - Yokai Beam : Same as yokai blast but more concentrated, hotter, goes through more blocks and explodes at the end into a big ball of
fire."

## 23. The owner on the islands (verbatim) and 3.9.3's world generation
- (The tool run that lowered the land share was interrupted by the owner, then:) "should be more apart, you should need to travel to
  find islands, theres too many"
- Done: tools/archipelago_data.py ISLAND_NOISE firstOctave -9 (as 3.8.3), CONTINENTS 0.5 -> 0.35, LOWER -0.24 -> -0.42: only the tops of
  the island noise are land: islands 300 to 1500 blocks, 1.5 to 3 km of deep sea between them, land about 14% (seed 2). Render:
  docs/images/archipelago-map-3.9.3.png (8 km). (3.8.3's islands, 1 to 3 km and 30% land, read as no islands on a phone: "I created a
  new world and I see no islands?")
- The reference GIFs read (scratchpad itsuref/*_sheet.png; 374x211 each):
  - Haki_Color.gif (286 frames): the Gorosei's Conqueror's Haki: thick deep indigo / blue-violet lightning bolts with darker edges and
    bright GREEN glints where they fork or flare, over lilac-pink smoke clouds; earlier frames: a cyan pentagram in a ring of runes on a
    dark floor. The user's Haki colours (Ryou and Conqueror's) take this: deep indigo bolts, violet rims, green sparks.
  - Yokai Beam.gif (147 frames): the Itsumade's mouth opens, an orange-white ball of fire forms in it, an orange ring with radiating
    rays (a halo) opens before the face, thin bright orange laser lines and a white-hot core beam shoot out, orange embers streak, the
    dark wings spread wide (black above, white under-feathers), a huge orange glare at the source.
  - Haki_and_Portalfire.gif (300 frames): a glowing cyan pentagram inside a ring of runes on a grid floor, rimmed by cyan and hot
    MAGENTA-PINK flames; a black stream pours down into its centre; blue spheres full of sparkles; black shapes made of cyan-lit smoke
    with orange eyes rise out of it; pink fire edging everything.

## 24. 3.9.3 (Build #285 and Release #184 GREEN): the owner's list in 20 fixed
- 1 coats: client/armor/ClothesModel draws the whole mesh always (lod() removed); katana_mesh.pack_clothes packs no LODs (simplify
  averages uvs across the atlas's patches: the noise).
- 2 mace right click: server/SwordServer.maceSlam (tickRelease, MACE_SLAM_TICK 2): crater 1.7 + 1.9 x charge (x1.5 with Armament) 2.3
  ahead on the ground (Craters, PieceConfig.TERRAIN), foes hurt/thrown/knocked down (strike's mace path), IronFist.debris, Impact.quake,
  ConquerorAura.exploded (POINT ONE), IronSounds.BOOM; no Haki slash for maces; messages "Haki slam"/"Slam".
- 3 mace: no glint (KatanaRenderer.draw skips highlight for Category.MACE: per-vertex glint on black coarse normals flickered);
  anyone: SwordServer.ready = swordsman || maceInHand; client/StyleInput.maceInHand sends PRESS/CHARGE whatever the style/combat mode.
- 4 mace slower: SwordRules.MACE_SLOW 2; SwordAnims.slowed(name) (a mace's own cut clips); the 11 cut clips stretched x2
  (tools/sword_anims.py slow_clip on write; clearance and java_paths read slowed clips at t x MACE_SLOW (the move's own time));
  SwordServer.slow/moveTick (cut windows, BladePaths, carry once a clip tick), SwordCombo.buffered(.., slow), the clash window scaled,
  the sound cursor on the slowed clock.
- 5 Haki balls: GunShots.punch: each pierced block's break shown (levelEvent 2001) and torn out (Scars, Craters.REPAIR, mobGriefing);
  bedrock stops the ball.
- 6 pistol hold: tools/gun_anims.py held() (the grip's pivot at the fist's centre + HANG_FORWARD 1.6 px ahead) and hang_down() (pitch
  and roll solved so the barrel points HANG_DIR: HANG_AHEAD 22 degrees ahead of straight down, the owner's drawing); IDLE/WALK/RUN.
- 7 islands: section 23.
- Lesson (cost a rebuild): never name a local `k` in sword_anims.clearance (its blade loop reuses k).

## 25. The Itsumade: the plan (owner's spec in 19, 20.8, 21, 22)
Name "Itsumade" (lang), Mythical Zoan. Stages (one version each, the owner testing between):
- 3.9.4 THE FORM: tools/itsumade_bake.py (bpy) -> assets/hexhaki/itsumade/itsumade.sry (DragonModel version 2: a colour a vertex) +
  textures/piece/itsumade/itsumade.png; the owner's notes in the bake: the head fused into one skin (HEAD_FUSED: voxel remesh
  HEAD_VOXEL 0.026, colours from the nearest original surface incl. the face paint), the fiery mane turned onto the tail's back
  (dorsal_angles/dorsal_turn), ink lines thinner than INK 0.016 left out; the rig (36 bones: root, chest, neck1-2, head, jaw, halo,
  pelvis, tail1-14, shoulder/elbow/wrist/finger L/R, hip/ankle/toes L/R; the body skinned by the distance along its surface).
  Then itsumade/ItsuForm (pure: Shape, pose for hover, fly slow, fly fast, glide, landing, scary walk, ambient, mouth open/close,
  bite; living fire like PhoenixForm.flame/heat on kinds 2 (fire: ruffs, collar, dorsal fin and flicks, tail fire, temple blazes =
  cheeks) and 3 (the black ribbon: the cloud ring, its wisps)); SCALE ~0.775 blocks a unit (the owner: "10 blocks tall"); the
  disturbing progressive transformation (Z); server/ItsuServer (like PhoenixServer: creative flight), ItsuKit (fruit kit),
  client/itsumade/ItsuFx (like PhoenixFx: CPU skin, hide the human, camera), the fruit in PiecePatch (ITSU, Mythical), its icon from
  input/itsumade/fruit.webp, CC0 sounds; tools/preview/ItsuPreview.java; tests.
- 3.9.5+ THE KIT: the passive (no death unless Ryou: black smoke, safe spot 10 blocks away, full HP and energy), the user's Haki
  recoloured (Haki_Color.gif: deep indigo bolts, violet rims, green sparks; G can kill), X Gorosei Stare, C Enormous Presence,
  V Yokai Blast, B Thy Death, N Hellish Teleport (the menu, 3 slots, the pentagrams, the black-flame beam), transformed M1 Wind Gust,
  M2 talons carry, X Bow, C Enormous Bite (bleed, Gura's broken bones), V Yokai Beam.

## 26. The owner on the fused head (verbatim; with a crop of the head preview: smeared teal patches round dark blobs, and the anime's
## eye: input/itsumade/pictures/eye_reference_anime.png: a red mask, a sharp narrow angry eye, a blue rim, a black pupil and liner)
"what's up with the eyes?"
- Why: the fused skull carried the face paint as vertex colours (too coarse for the markings) and sry_view colours each triangle flat.
- Fix (3.9.4 bake): the eyes fused into the skull too, and the skull TEXTURED: cylindrical uvs about the beak's axis (the seam under the
  jaw), its texture baked texel by texel from the owner's own surfaces (their colours and the painted face, at each texel's nearest
  point): every marking as sharp as the owner painted it.
- The owner, right after (verbatim): "theres no face paint bro thats part of the beak/head and the blue is the eyes / jesus" — the red is
  the head's own colouring (its texture), the blue shapes ARE the eyes; never call it face paint. The fix stands: the fused head
  textured texel by texel from the owner's surfaces, so the eyes keep their exact shape.
- The owner: "setup an auto wake up in exactly 4 hours and 34 minutes" -> scheduled trig_01MHMRns1H36p6hqF4fr8UaQ, fires 2026-11-27 18:02 UTC.
- The owner (verbatim, with the walk previews: the folded wings crumpled, feathers sticking out): "the animation needs to be much much
  more detailed, the walk/land looks like shit, I mean look at the wings jesus... / and the monster is way too small". So: no deep wing
  fold on the ground (rigid feathers on blended membranes crumple): the wings mantle (held up, arched forward round the body, the
  hands hanging to the ground); SCALE raised (bigger than 10 blocks standing); much more secondary motion; judge GIFs, not stills.
- The owner (verbatim, with the stand preview from the front, two green lines down the body's sides from the shoulders to the hips, in
  front of the wings): "the black fire ribbon has to be infront of the wings AT ALL TIMES". So: the ribbon's arms always pass in front
  of the wings (never a wing swept forward past it; the ribbon held a little forward of the wing roots), in every pose and in flight.
- (The bake's wings re-rigged per vertex along the span, feathers too: they bend with the arm as one sheet instead of sticking out.)
- The owner (verbatim, with the stand preview from the side, a green line from the tail's tip along the ground rising gently to the
  body and on to the head, nearly level): "plus it's too stiff, why is the bird standing up like a person, ITS A BIRD LIKE MONSTER".
  So: on the ground the body is LEVEL (pitched forward like a bird or a raptor), the legs upright under it, the neck reaching forward and
  up, the tail streaming back nearly level to the ground; and far more motion (never stiff).
- The owner (verbatim, with a walk frame: a green line along the creature's back from behind the wings over the shoulders to the head):
  "the fire ribbon has to bend too along with the body so it looks good". So the ribbon is skinned to the body's chain (each vertex by
  its nearest centreline point: the arch over the head rides the neck and head, the arms down the sides the chest and hips), not held
  on one bone or upright.
- The owner (verbatim): "and the legs are too stiff / The fire all of it also has to glow, black fire glows black, the rest glows
  orange". So: the legs fully alive (a bird's stride: the tarsus folding and the toes curling as each foot lifts, spreading as it
  plants; standing, the weight shifting, a foot lifted and the talons flexing now and then); every flame glows: the orange fire an
  orange glow round it, the black ribbon a BLACK glow (a dark haze) round it.
- The owner (verbatim, with the standing frame where the ribbon's end stretched to the ground; a green hook drawn from the shoulder down
  round the chest): "jesus whats up with the ribbon here". The bug: the ribbon's lower ends weighted to the tail's bones (their nearest
  centreline points), dragged off along the tail. Fixed in the bake: the ribbon never weighted past the pelvis.
- The owner (verbatim, on a walk frame with a dark line from the legs to the ground): "WHAT THE FUCK IS UP WITH THE RIBBON HOW DOES THIS
  PASS". The line was the ribbon itself: its arch on the neck bones, its arms on the chest, the neck swinging 44 degrees round (the file's
  head faces aside) tore it into a 3.7-unit band. Fixed: the ribbon blended smoothly from the chest (its ends) to neck1 (toward its top);
  tools/preview Stretch check (no triangle edge longer than at rest by more than 0.5 units in any pose) run before any render is shown.
  LESSON: measure stretch numerically in every pose before showing renders; never show a frame with a defect in it.
- The owner (verbatim): "as soon as the 2d fruit + transformation is done push an update, remember that the 2d fruit is not meant to have
  a background, yours clearly has a brown background". So 3.9.4 ships as soon as the icon (no background: only the fruit, its leaves and
  its flames) and the transformation are done.
- The owner (verbatim, with the second cut-out icon: the transparent one, the husk's hollow middle circled): "Just use this with a red
  berry in the center". Done: tools/itsu_icon.py (that cut-out: one connected shape, no parchment) + berry(): a red berry filling the
  hollow between the husk's arms, shaded round with a glint.
- The owner (verbatim): "uhhh the berry needs more details.. it looks like female genetalia rn..". The berry filled the husk's tall slit.
  Now: a ROUND berry (a ball drawn over the husk's middle, raspberry drupelets, a dark rim, a glint), the rest of the gap the husk's own
  shadowed inside: no slit shape left.
- The owner (verbatim): "push an update now. please, I wanna see it before I get home." -> 3.9.4 pushed as soon as its build passed.

## 27. 3.9.4 (Build #288 and Release #185 GREEN): the Itsumade, part 1 (the fruit, Z)
- Fruit ITSU ("itsu", item itsu_itsu_no_mi, lang "§dItsumade", colour 8A2BE2; PiecePatch ITSU after TORI, ordinal 12), FruitRanks
  MYTHICAL, HexPiece registers server/ItsuKit (FORM [Z] only). Icon tools/itsu_icon.py (the owner's picture cut out + a round berry).
- server/ItsuServer (from PhoenixServer: PHASE 440, FLY_SPEED 0.06, MARK HexPieceItsuFlight; cracks CRACKS/CRACKS_BACK, the roar at
  SCREAM 49), clips itsu_form (40) / itsu_unform (34) (tools/itsu_anims.py: body, torso, arms only).
- itsumade/ItsuForm: FORM_TICKS 64, UNFORM_TICKS 34, SCALE 1.2, the change (GROW/SHRINK per group, lurch(), size(), HUMAN_GONE 30,
  HUMAN_BACK 16), the poses (GROUND_PITCH -62: level on the ground; wings draped MANTLE_*; legs a bird's stride; the tail chain solver
  with TAIL_BEND 24 and the posed feet's floor; the head NECK_FRONT 44.4 + HEAD_FRONT 13.3; the jaw JAW_*), flow() (fire, ribbon,
  hair alive), heat(), glow() (FireMesh.shell: orange 0xFF7A1C round the fire, black 0x0A0312 round the ribbon; anchors GLOW_SPACING
  1.25 at rest).
- client/itsumade/ItsuFx (from PhoenixFx: CPU skin, own shading LIGHT/SHADE, world light, fire/ribbon full bright, glow after the
  particles, hide the human, camera back 4 + 16, up 4.5), ItsuClient; hooks UoFx (440-459), DragonCamera, CombatMode.zoanOut,
  Transformed.zoan.
- DragonTests.itsumade: the rig, faces <= 20000, glow shells <= 60, every state's poses finite and stretched <= ITSU_STRETCH 0.9, the
  growth never shrinking, gone at the end.
- Previews: tools/preview/ItsuPreview (scratchpad itsuprev.sh, itsugif2.sh); the scratchpad's dbg/Stretch.java (worst stretch per part).
- NEXT (the owner's spec in 22): the passive, the Haki colours, X Gorosei Stare, C Enormous Presence, V Yokai Blast, B Thy Death, N
  Hellish Teleport (menu + pentagrams), transformed M1 Wind Gust, M2 talons, X Bow, C Enormous Bite (+ bite clip: ItsuForm.bite), V
  Yokai Beam; dedicated CC0 sounds; keep judging every pose as GIFs; the wing feathers' small stretch on the beats (0.8).

## 28. The owner's list after 3.9.4 (verbatim, in order; uploads kept as sent in input/armor/)
- "@Doflamingo_Feather_Coat.blend New coat (I dont think needs any adjusting) called Flamingo Coat" (input/armor/Doflamingo_Feather_Coat.blend)
- "also theres currently a problem in the swordsman mode, it auto picksup the mace even if I dont tell it to, it should only pull it off
  when I select the item in the inventory. Also the Mace does not require rope around the body to hold it. Again this applies for all
  items, when holding literally anything with Ryou + Conqueror's haki the lightning around the body gets transfered to that item, this
  applies for our weapons especially. the Mace does not have lightning right now, and has sword sounds.."
- "@Crimson_Brocade_Coat.blend And heres another Armor named "Greatest Swordsman"" (input/armor/Crimson_Brocade_Coat.blend)
- "The spin of the ambient animation (spin) of the GUN is offcenter, and the left arm is not meant to have animation when firing (its
  putting the left arm on the hip, another bug, whenever the gun does the spin and it ends, it goes backwards when the animation finishes
  it should not undo itself... Then make sure every piece of clothing adapts to the animations and GENDER (breasts on female)."
- "from now on all devil fruits are now gonna have  minecraft sounds, (except explosions and haki related things)"
- "Now that I'm looking again, Yoru, the blade is also kind of glitching. If you look at the blade, just non-stop, like kind of texture
  problems. I think the pictures that I sent kind of it's kind of hard to show you exactly what I mean, but you can gather that it's the
  texture is like bugging. It's only on one side though. The other one seems to be fine. Like strange. Like depends on the perspective
  you're looking at it. But it's still weird as fuck. Also, all the tabs that we got in the mod in the creative menu, they should all be
  named hex piece and then whatever." (3 pictures: dark streaks over one face of the Yoru's blade)
- "Oh boy, now that I get home, I truly see the bugs sometimes. Whenever using a move that it does an animation, sometimes briefly, the
  the entire cape breaks." (picture: the coat torn, a bubble of cloth)
- "also the other armors..." (picture: the marine outfit wrong)

## 29. THE OWNER ON THE ITSUMADE IN GAME (verbatim; with four pictures: the tail's back stripe a flat orange band; the wings thrown up
## in a V seen from above with a grey smoke round the head; standing seen from below: the body floating, the legs hanging, the talons
## curled like hooks (circled with question marks); flying from behind with a dark smoke cloud round the head and the ribbon)
"about the Itsumade fruit. This is a little much. Like, the glowing is not what I meant. This is not glow. This is smoke that you create
around the thing. I don't like it, and it's ugly. Like, a lot of things about this fruit are completely broken. First of all, the third
person, like, when you switch to one of the modes in third person, you're gonna see just his feet. It's weird. This is not how wings pose
of a bird. Please research how a fucking bird uses his wings for once. Oh my god, but like this is horrible. Like his feet don't move when
he walks. His neck is too stiff. His hair doesn't move. Like, what is this shit? Like, I'm truly asking, what is this? The animation of
flying, the wings are way too fast. He looks like a fucking mosquito. This is honestly a worst work, if I'm being honest. It looks
horrible. Like, when you're landing, you go into the ground. What the fuck is this shit?

The ribbon is completely broken. It doesn't resemble flames anymore. It looks horrible. It it's dude, you're fucking butchering all this.
I mean, I'm seriously asking, what the fuck is this shit? And don't you even dream about leaving the transformation animation as it is.
You better continue and making this shit not be a shrinking animation. The fucking animation is atrocious. I mean, it's just growing out
of me. Instead of like the player progressively transforming, no. Just coming out of me. The bugs are unending. You're not gonna stop
until this entire fruit is done. Every bug is addressed, everything is fixed. The world generation is not adapting to Terra Blender and
Biomes of Plenty well. I don't see like the islands are way too fucking close, sometimes merged together. This is not fucking this cannot
pass. This is a fucking shame. You should be ashamed of this shit. working faster. This whatever you're doing, it's taking way too long.
Start getting faster, bro. I swear to God."
So, in order: (1) no glow shells at all (the fire is bright on its own); (2) the third person cameras show the whole creature; (3) bird
wing poses (folded along the back standing; real flight strokes); (4) the feet step when it walks; (5) a living neck; (6) the hair
moving; (7) slow, heavy wingbeats; (8) landing on the ground, never into it; (9) the ribbon as flames again; (10) the change: the player's
own body transforming progressively, not the creature growing out of it; (11) the islands with TerraBlender and Biomes O' Plenty: far
apart, never merged. Then the list in 28.

## 30. 3.9.5 (Build #289 and Release #186 GREEN): the owner's lists in 28 and 29 (part)
- Itsumade (29): ItsuForm.glow and its anchors removed (ItsuFx, preview, DragonTests). Wings (ItsuForm.wing): FOLD_* on the ground (a
  bird's fold: FOLD_DOWN -78 about z, FOLD_ONTO -16 about x, FOLD_ELBOW 78 / FOLD_WRIST -118 / FOLD_FINGER -18 about y in the wing's
  plane; breath, shrug, rouse shake, walking balance); the air stroke (DOWN_PART 0.56: downstroke spread high 50 to low -36, the
  recovery flexed at the elbow 40 / wrist -72 / finger -24, the tip bending), BEAT_SLOW 0.42 / BEAT_FAST 0.6 beats a second, heave()
  (HEAVE 0.22 units, the root raised). Legs (leg): STANCE 0.6, HIP_SWING 30, the planted foot flat (toes = -(hip + ankle)), the swing's
  tarsus folding -78, toes clenching then opening to plant; the legs' upright term is the real ground pitch (it was GROUND_PITCH even in
  the change's upright form). GROUND_PITCH -52. Neck: Motion.turn (the look's yaw off the body's), swayYaw/swayPitch, the beats' wave,
  the dive holding it straight (DragonTests caught 0.94 in the ruff). Hair (flow, HAIR): sway 0.26 with gusts and a side wave, wind on
  the ground when walking (ItsuFx). Ribbon (flow, RIBBON): flutter 0.06, lick 0.05 (it was a 0.18 roll that melted the tongues).
  The change: SWAP 24 / UNSWAP 20, humanScale() (HUMAN_BIG 1.7 in lurches; ItsuFx.swell/swellDone push and pop the player's pose
  in RenderPlayerEvent Pre LOWEST / Post), SWAP_SIZE 0.6, Motion.spread (the wings thrown out wide at the swap, folded at 57-64),
  ribbonGrown() (the ribbon drawn into the halo: its wisps are skinned to the chest too), the fire unlit (embers) until G_FIRE.
  ItsuFx: the anchor in the air is the chest less chestHeight() (and less the heave) so it lands on its feet; the camera for
  THIRD_PERSON_FRONT too, raised by chestHeight. Renders: scratchpad itsu2/ (v1 stand/walk, v2 walk close, v3 the beat, form3).
- Islands (29): world/IslandField (pure: CELL 2800, JITTER 0.5, EMPTY 0.25, SMALLEST 230 / LARGEST 760, LOBES, held() caps each radius
  by APART 0.5 of the nearest neighbour less GAP 260 and SHELF 260, so coasts stay >= 520 apart), world/IslandLines (density function
  type hexhaki:island_field, registered in RiverLines' TYPES), islands.json = island_field + 0.12 vanilla continentalness
  (archipelago_data.py DETAIL). WorldTests.islands. Map: docs/images/archipelago-map-3.9.5.png (24 km, seed 2).
- Coat (28): CoatCloth.armsPush PUSH_FULL 0.9 / PUSH_NONE 1.8 (coat_cloth.py the same); ArmorTests: walking swings still leave
  nothing inside, no pose moves the coat 6 px. The scratchpad's coat/scan.py scans every clip (it found 6-9 px balloons before).
- Armors (28): client/armor/ClothesUnder hides the skin's outer layers under the clothes (the owner's ghost boxes).
- Yoru (28): KatanaRenderer.highlight draws the glint GLINT_LIFT 0.9995 toward the eye (it z-fought the steel on one face).
- Tabs (28): HexPiece Weapons / HexPiece Armor / HexPiece Devil Fruits (+ lang itemGroup.hexhaki.fruits); ArmorTests.tabs.
- Mace (28): SwordServer.scan never picks a mace that is not the main-hand item; SwordLayer draws no ropes for it; the Conqueror's
  SPARKS go off the held item (client/haki/HeldLine: SwordLayer records a drawn weapon's line; else a short line out of the fist).
- NEXT: the mace's own sounds (SwordSounds by category; sounds.json can point an event at a vanilla one: {"name":
  "minecraft:entity.player.attack.sweep", "type": "event"}; SwordTests' sounds check must allow it), every devil fruit's sounds on
  vanilla events (the same sounds.json way, explosions and Haki kept), the gun (spin centred, the left arm free when firing, the spin's
  end not undoing), the Flamingo Coat and Greatest Swordsman (input/armor/*.blend, tools/clothes_bake.py), clothes on female bodies, then
  the Itsumade's moves (22).

## 31. 3.9.6: Minecraft's sounds for the fruits, the mace's own, the gun
- tools/vanilla_sounds.py (VANILLA: 113 fruit events -> minecraft events with volume and pitch; KEEP: the 14 explosions; MACE; ITSU):
  rewrites those sounds.json entries as {"name": "minecraft:...", "type": "event"} and deletes the recordings nothing plays (148). Every
  name checked against 1.20.1's sounds.json (misode/mcmeta 1.20.1-assets: all 126 references exist). SwordTests.vanilla(): such an
  entry plays only Minecraft's events; webSounds/mixedSounds/redone skip them (their least counts no longer apply).
- SwordSounds.MACE_* (in ALL) and SwordSounds.of(sound, katana) (a mace's own for a blade's: beats and the release's slam).
- ItsuServer.CRACK "piece.itsu.crack" (bone block breaking) for its cracks (registered in PieceSounds; subtitle "Bones crack").
- tools/gun_anims.py: SPIN_SIDE -1.2 (was -2.5: off-centre), the spin's last tick keys the hold's own angle with constant easing (no
  unwinding), the fire clips key no left arm (written = PARTS less leftArm). Previews: scratchpad gunprev/.
- NEXT: the Flamingo Coat (input/armor/Doflamingo_Feather_Coat.blend: blocks as units, four feather meshes under the empty "COAT |
  torso attachment", one 256 x 1024 pixel atlas "Plumage and cloth | pixel palette") and the Greatest Swordsman (Crimson_Brocade_Coat.blend:
  an armature "COAT • pose controls" of 7 bones, 18 meshes, 12 pixel-weave textures 64 x 64): both into tools/clothes_bake.py's frame
  (model px: x the wearer's left, y down from the top of the body, z back), groups body_upper/_lower(+_in)/right_sleeve/left_sleeve,
  their own textures (the owner: "I dont think needs any adjusting"), new PieceArmor entries (CHEST), icons, ArmorTests; then clothes on
  female bodies (FemaleBody: the chest's bust); then the Itsumade's moves (22).
