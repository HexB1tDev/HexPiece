# HexPiece: handoff after 2.3.6 to 2.4.1 (clashes; the Galaxy Fist made Iron Fist's; Fishman Karate's rework; Six Powers' air in 3D)

Read CLAUDE.md first. This note is the newest; next-ai-2.3.2.md covers 2.3.2 to 2.3.5 (one animation set for every blade, the
owner's in-game bugs, Haki hands from the skin, bigger swings), next-ai-2.3.0.md the clash rules in the owner's words, next-ai-2.2.6.md
the open requests in detail. Branch `claude/nice-bell-gam7qf`. **The next version is 2.4.2** (`python3 tools/next_version.py --write`).
Nothing was ever tested in game (only the owner can).

## State (session 4)
- Asked first, as the handoff said: the owner had NOT tested 2.3.4 or 2.3.5 in game yet ("Not yet, carry on") and had not looked at
  the Fishman Karate white screen again. So both are still open: ask again next session.
- 2.3.6 released: clashes (next-ai-2.3.0.md item 1). Build green locally (PieceVerify proves every reference, 30 sword animations
  natural and clear, 11 suites); check the branch's Build and Release runs.

## Done in 2.4.1: Six Powers (next-ai-2.2.6.md item 4: "Improve air particles, speed them up and make them 3d. (finger pistol with haki
shoots projectiles)")
- `water/WaterMesh.Look` (deep, shallow, gathered, base alpha, rim alpha): WATER, AIR (0.03 + 0.8 fresnel: all but clear, a bright rim),
  HAKI (black glossy); body/ring/shade take a look (the old calls are WATER).
- Pure `air/AirMesh.crescent`: an arc of radius R spanning +-70 deg, bulging along its flight, its tips swept back, a lens cross-section
  (thickness across its plane, depth along its bulge) tapering to the tips (`taper`), shaded AIR, back to front; `tilted` turns its tips'
  line about the flight (a kick's sweep).
- Client `client/fx/AirFx` (its own AFTER_PARTICLES pass, debugQuads): `StyleFx.swing` hands every Six Powers blow to `AirFx.swing`:
  the Storm Leg a crescent (R 1.0, tilted -30, 2 blocks a tick, 2.6 ticks), other blows a ring of air (AIR torus opening fast), all round
  a big ring; streaks STREAK_SPEED 1.8x. `StyleFx.shot` hands the Air Blade to `AirFx.blade`: a crescent R 1.6 tilted 35 deg at
  BLADE_SPEED 4.5 (was 2.2), two fainter ones a tick and two after, a ring and dust at its end.
- Server `server/AirServer`: SP_FINGER_R/L landing with Armament on (StyleServer.tick, players only: NPCs have no Haki) shoots a
  bullet of hardened air from the eye along the look (SPEED 4, RANGE 18, DAMAGE 4 x mastery): first foe along its path (box + 0.3) or
  block (ClipContext), `S2CWater.AIR_SHOT` / `AIR_HIT` (WaterFx.accept passes them to AirFx: a HAKI body stretched 3x, rings of air).
- Render: docs/images/six-powers-air.png (WaterPreview `air`, `airside`). Tests: StyleTests.air.

## Done in 2.4.0: the Fishman Karate rework, part 3 (the Water Prison): the rework is complete
- **Water Prison** (`StyleMove.FK_PRISON`, U, in the Five-Thousand Brick Wave's place; `Effect.PRISON`, no hits; mastery 200,
  cooldown 400, 22 ticks; pure `water/WaterPrison`): `StyleServer.press` needs `WaterPrisons.sights` (the nearest foe whose box, grown
  AIM 0.35, the look passes through within RANGE 16: `WaterPrison.sights`, a slab test) or refuses ("no one in your sights", nothing
  spent); then `WaterPrisons.aim`. Each tick (`WaterPrisons.tick`, from StyleServer.tick / SeaFighter.tickMove): GATHER 4 ->
  `S2CWater.PRISON_GATHER` (+ "block.bubble_column.upwards_inside"), SEAL 14 -> `seal`: lifted LIFT 0.6, noGravity, held HOLD 100
  ticks (`prisonTick`: put back by teleportTo if it drifts > 0.12, zero velocity, slowness 9, Clashes.stagger), `PRISON_SEAL`; ends
  (`release`: gravity back, `PRISON_BURST`) at HOLD, when BREAKS 14 damage has been dealt inside (`prisonHurt`), or on its Conqueror's
  release (`ConquerorRelease.visual` -> `WaterPrisons.conqueror`). `prisonAttack` cancels every attack of a held body (a perfect
  stun). Sea fighters throw it at their foe in range (SeaFighter: begin + aim; M1 instead if none). Interrupted or clashed moves drop
  the aim (`WaterPrisons.stop`). Client `WaterFx`: PRISONS: the mist drawn in onto the swelling ball (`WaterPrison.grown`), the ball
  (`WaterMesh.Body`, radius `WaterPrison.radius(height, width)`: 1.18 x half the body + 0.3, opacity PRISON) round the body's middle,
  air bubbles rising inside while held, `burstPrison` (spray, a ring, 12-22 droplets). Animation `style_fishman_karate_prison` (both hands
  up by 4, held, brought down together on 14; the anim lint caught a 22-degree roll at pitch -168 swinging the arms through the head:
  -176 / 8 now). Removed with the wave: its animation, `StyleFx.wave`. `S2CWater.index` is a short now (the hold's ticks).
  Tests: StyleTests.prison.
- Not done: drowning (the owner said "perfect stun, entity can only look around": no damage inside).

## Done in 2.3.9: the Fishman Karate rework, part 2 (the Brick Fist, rag dolls)
- **Brick Fist** (`StyleMove.FK_BRICK_FIST`, Y, in the Shark Skin Counter's place; `Effect.SHOCKWAVE`; pure `water/BrickFist`): a
  `Hit.line` at STRIKE 8 along the 3D look (REACH 8.5, RADIUS 1.4, damage 9, throwing 1.5 / 0.5), mastery 60, cooldown 140, a small
  lunge. `StyleCombat.land` (players and NPCs alike): a SHOCKWAVE move sends `S2CWater.SHOCK` (from the fist, eye + look * 0.9, along
  the look) instead of the shot visual, plays "entity.player.splash.high_speed", throws along the 3D look and knocks each body struck
  off its feet (`Knockdowns.knocked`). Client `WaterFx`: SHOCK -> a burst off the fist, then RINGS 3 rings (one a tick) of water
  (`WaterMesh.ring`, a torus: `BrickFist.travelled/major/minor/left`: 1.7 blocks a tick, opening 0.3 -> 1.9, thinning 0.2 -> 0.05,
  fading over the last 30 %), spray and droplets torn off them. Render: docs/images/brick-fist-shockwave.png (WaterPreview `shock`,
  `shockside`). Animation `style_fishman_karate_brick_fist` (18 ticks, in PUNCHES: eased in, the legs left to the game).
- **Rag dolls** (the owner: "Ragdoll mechanic can be taken from HexGhoul"): HexGhoul 6.2.0's Speed Kick tumble ported: pure
  `style/Tumble` (= KickTumble without Witness This: the head snaps, the body twists and tumbles, thump, DOWN_TICKS 28, GETUP_TICKS 12),
  server `server/Knockdowns` (= PaleKnockdown: landed when on the ground after 3 ticks or AIRBORNE_MAX 80; held down with slowness 6
  and staggered (Clashes.stagger: no blows) until up), client `client/fx/Tumbles` (= PaleReactions.tumble: RenderLivingEvent.Pre/Post
  LOWEST, the head squared on the body and snapped, the pose stack turned about the body's middle; water running off it as it flies,
  spray where it lands), told by `S2CWater.KNOCKED`. Tests: StyleTests.brickFist.
- The Shark Skin Counter is gone with its code (StyleServer.attacked, SeaFighter's counter branch, StyleFx's COUNTER effect,
  `Effect.COUNTER`) and its two animations.

## Done in 2.3.8: the Fishman Karate rework, part 1 (next-ai-2.2.6.md item 3)
- The rework's moves, decided (the owner listed three new moves; Fishman has three non-string slots): right click = Water Bullet
  (2.3.8, replaced the old Water Bullets), Y = Brick Fist (next, replaces the Shark Skin Counter), U = Water Prison (then, replaces the
  Five-Thousand Brick Wave). The M1 string stays. Effect.COUNTER's code (StyleServer.attacked, SeaFighter.m_6469_) and the
  counter/wave animations stay until their moves are replaced.
- **Water as a body** (pure `water/WaterMesh`): a sphere round its line of flight (poles on the line), wobbling (`wobble`: three slow
  lobes), drawn out into a teardrop (`stretch`: the back pulled 1x, the front 0.35x), shaded per vertex from the eye (`shade`: deep
  0x1A6FC4 to shallow 0x9ADCFF by fresnel^2.2, alpha 0.2 + 0.62 fresnel, the sky's specular ^48 from `SKY`, the gathered light on the
  side away from the sky), opacity `DROP` 1.6 / `PRISON` 1.15; triangles sorted back to front from the eye (the far inner wall first);
  `resolution` by size on screen. Drawn by `client/fx/WaterFx` into `RenderType.debugQuads` (alpha blended, no cull, as the glows
  since 2.1.9), each triangle a quad with its last point twice, RGB times the sky's light (`m_104805_`, 0.32 at night).
  Preview: `tools/preview/WaterPreview.java` (scenes drops, flight, bubble, close, bullet [age]); docs/images/water-bodies.png,
  water-bullet.png.
- **Water Bullet** (pure `water/WaterBullet`; `StyleMove.FK_BULLET`, `Effect.BULLETS`, its hits = the five shots' ticks and damage):
  the hand up by tick 4, drop i gathers from tick 4 + i over 6 ticks (`grown`), on a flat arch (`formation`: half width 0.5, middle 0.2
  higher, 0.32 over the fist at FIST (0.34 right, 2.18 up, 0.12 ahead)), fired at 16 + i (`fireTick`), 1.5 blocks a tick, turning at
  most 24 deg a tick after its target (`steer`), life 28, locked onto the foes within 24 blocks and 45 deg of the facing, nearest first,
  round again (`target`). Server `server/WaterServer` (`bullets` called each tick of the move from StyleServer.tick and
  SeaFighter.tickMove instead of StyleCombat.land; drops fly in its level tick: the first foe along the tick's path (its box + 0.45) or
  the first block (ClipContext) -> hurt + `Impact` light throw, BURST; out of life -> FADE; a move cut short -> SPILL). Sounds: the
  game's own by name (`WaterServer.vanilla`: ForgeRegistries.SOUND_EVENTS.getValue, "entity.fishing_bobber.splash",
  "entity.generic.splash", "block.bubble_column.upwards_inside" as the drops gather). `S2CWater` (protocol 6): FIRE, BURST, FADE, SPILL.
  Client `WaterFx`: the gathering starts from S2CStyleFx stage 0 of a BULLETS move (`StyleFx.accept`); mist motes drawn in (PieceFx
  DROP, fading in as they arrive) while each drop swells; flight simulated with the same `steer` after the target's client position;
  bursts: spray, a ring, 7 droplets (`Bit`s: little bodies under gravity). `ClashBlows.style` gives BULLETS moves no blow (their drops
  fly on their own). Animation `style_fishman_karate_bullet` (28 ticks; the old `_bullets` deleted). Tests: StyleTests.waterBullet,
  ClashTests.
- **The white screen** (the owner's report, still not reproduced): `PieceFx.inFace`: no PieceFx particle is spawned within
  `CLEARANCE` 0.9 + its size of the first-person eye (a spray puff at a fighter's own face fills the view with its tint: a suspect).
  Told the owner in the notes; ask again.

## Done in 2.3.7 (the owner, after 2.3.6: "Continue and dont stop")
- **Galaxy Fist for Iron Fist** (next-ai-2.2.6.md item 2; the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the
  user activates RYOU haki. ONLY for iron fist."). In the original it is `HakiTechnique.ARM_GALAXY_WAVE`, shown as "Haki Projection",
  quick power 103, started by `HakiAction.CONVERGENCE_START` (`HakiServerController.beginConvergence`: Armament mastery 520, energy 8,
  `galaxyPunching`, GALAXY_FIST_START, the `galaxy_beam_charge` animation) and let go by CONVERGENCE_RELEASE. The patch:
  CALL_REDIRECTS `onAction`'s call to `beginConvergence` -> `server/GalaxyFist.begin` (lets it go on only when
  `PieceRules.galaxyFist(style, ryou)`: BRAWLER with armamentOn and acocOn; tells anyone else why not), WIDENED `beginConvergence`,
  INTS its 520 -> 0 (twice: the check and the lock message). Logbook: `LogbookView.galaxy` adds "Galaxy Fist" to Iron Fist's page
  (bindable to 103, open once Ryou (CONQ_ACOC) is unlocked); `LogbookData.haki` leaves ARM_GALAXY_WAVE out of Armament's list;
  `AbilityHud.hakiName`/`abilityName` call 103 "Galaxy Fist". Tests: StyleTests (the rule, the patch rows), MenuTests (no Haki page
  offers 103; Iron Fist's does). Render: docs/images/logbook-iron-fist-galaxy.png (UiPreview `logbook-iron-fist-galaxy`).
- The 2.2.6 note said the Galaxy Fist's charge lightning (`addAdvancedGalaxyFistLightning`) was the last flat-beam Haki lightning: it
  is dead code in the original (its only caller `startGalaxyFistCharge` is never called; the charge is drawn by `GalaxyRenderer`,
  a disc and inflow ribbons). Its sky strikes go through HakiLightning (3D, Ryou colour). Nothing to convert.

## Done in 2.3.6
- **Pure** (`clash/`): `ClashRules` (2.3.3's rules; new: `WINDUP` 3: a blow meets another only in its last 3 ticks before landing, so
  two blows clash when both strike within ~3 ticks of each other, a hit on someone still winding up is a hit; `meeting`: where on the
  line between them they meet, as near the middle as both reaches allow; `SOUND`; `RECOIL` 12; `recoil(style)` = `style_<id>_clash`),
  `ClashBlows` (which blow a move has on its way at each tick: a StyleMove's hits (each sets out after the one before; a line shot is
  on its way only the tick it flies), a SwordMove's cuts (on its way to the end of its window), the charged cut (1-3), the quick draw
  (to 8); `bladeReach` from BladePaths, the very path that hurts), `ImpactFrame` (the screen geometry, below). Tests in `ClashTests`
  (2760 checks).
- **Server** `server/Clashes.landing(user, now, foe)`: called the moment a blow lands (StyleServer.tick and SeaFighter.tickMove before
  `StyleCombat.land`; every tick of a cut's window in SwordServer.tickMove / SeaFighter.swordStep; the steel charged cut; the quick
  draw's run). It looks for a foe within reach (`ClashRules.reaches`), both facing (60 deg), height within 2.6, whose blow is on its
  way. Met: `collide` stops both (`StyleServer.clashed` / `SwordServer.clashed` / `SeaFighter.clashed`, recoil on), staggers both
  (`Clashes.staggered`: no press, no queued blow, no NPC attack; slowness), `Impact.hit` both ways (hit-stop `frameTicks + 2`, then the
  throw `ClashRules.thrown/lifted` away from the other; FLASH_NONE: our own frame), `Impact.quake` if `quakes`, the sound
  (`piece.clash`: the owner's Clashes.zip copied from HexGhoul, `sounds/piece/clash/impact1-4.ogg`, SOURCES.txt; plus
  `piece.sword.clash` when a blade is in it), `S2CClash` to everyone tracking either (protocol 5; the client plays one clash per id).
  `bladeWins` (a bare blow without Armament meeting a blade): the bare move is cut off without a recoil, the blade lands. Armed =
  `HakiData.flag(player, "armamentOn")`; Ryou colour when `acocOn` too. Sea fighters have no Haki (their fists never meet a blade).
- **Recoils**: `style_<style>_clash` x4 (tools/style_anims.py `clash_recoils`: the fist bounced back to guard the face, the free arm out,
  the body leaning back, a step back, the stance again; Black Leg's leg thrown down and a hop back, hands behind; Six Powers' left
  hand stays behind), NPCs too (MOVE = `SeaFighter.CLASH_MOVE` -1, SeaFighterRenderer plays `ClashRules.recoil(style)`); `sword_clash`
  (tools/sword_anims.py, solved: the blade knocked up over the right shoulder, the guard again; `SwordAnims.CLASH` 9, 12 ticks;
  `SwordServer.State.clashStart`, SeaFighter `clashAt`; first person shows it, `SwordClient.swing`). Renders judged:
  docs/images/clash-recoils.png, clash-sword-recoil.png.
- **Client** `client/fx/ClashFx`: the burst where the blows met (PieceFx flash, rings, sparks fanned square to the line: orange for
  steel, white for bare; dust when it quakes); Ryou meeting Ryou: `HakiBolts.clash` (pure `haki/ClashBurst`: 8-17 thin inked bolts over
  the sphere, both colours by turns, 6-15 blocks, those coming down run along the ground; two Lives, each its colour's flash, no ring:
  the storm's ring this small lies on the ground like a plate; `Live.flash` is now the flash radius, the storm passes 10 + wave/10).
  The impact frame: an overlay registered BELOW all (the HUD stays readable), for viewers within `frameReach`: `ImpactFrame.at(age,
  ...)` batches: (1) the negative: INVERT (white quad, blend ONE_MINUS_DST_COLOR/ZERO) then CONTRAST (the frame blitted into a
  TextureTarget as ScreenSplit does, then drawn over itself with DST_COLOR/SRC_COLOR = 2c^2), ink focus lines and a small black star;
  (2) the black frame: white rays (both Ryou colours when both are Ryou) and a white star; (3) the release: a white flash fading and
  lines fading, TAIL 2 ticks. Lines redrawn every half tick. The point projected with the camera's look/up/left and the world's FOV
  (`ComputeFov.usedConfiguredFov`). Renders judged: docs/images/clash-impact-frame.png, clash-ryou-burst.png.
- New proven references (verify/api-allowlist.txt, 2.3.6 block): SourceFactor ONE_MINUS_DST_COLOR / DST_COLOR, DestFactor SRC_COLOR
  (joined.tsrg, unobfuscated), ComputeFov.usedConfiguredFov (Forge 1.20.1 source). Stubs: GlStateManager factors,
  RenderSystem.blendFuncSeparate, Camera look/up/left, ComputeFov.

## Tools
- `tools/preview/ImpactPreview.java` (javac -cp build/classes -d build/preview tools/preview/ImpactPreview.java; java -cp
  build/preview:build/classes ImpactPreview BG.png OUT POWER COLOR_A COLOR_B PX PY): the impact frame's batches rasterised over a
  picture exactly as the game blends them (frames at 60 fps). A backdrop: a frame of docs/images/slash-finisher-showcase.mp4
  (`ffmpeg -ss 2.2 -i ... -frames:v 1 bg.png`), the point at its figure's chest (640, 400).
- `BoltPreview clash DIR`: the Ryou burst, day and night sheets (`Raster.flash`; burst and ring now drawn at boltsAt's origin).
- `tools/sword_gif.py OUT '^sword_clash$' --views three,side,fp`.

## Open, in the owner's order
1. Whatever the owner reports from 2.3.4, 2.3.5 and now 2.3.6 in game. For clashes, ask: do they happen too often or too rarely
   (`ClashRules.WINDUP` 3, `FACING` 60 deg, `SLACK` 1 block), is the impact frame too much on every clash (it is on every one, as
   asked: `frameTicks` 2-4 + `TAIL` 2), does the negative look right on their device (if the screen goes plain white or black for a
   moment instead, the blend factors are the suspect: the INVERT batch alone, without CONTRAST, is the fallback).
2. The Fishman Karate white screen (not found; ask when it happens, a full screenshot; suspects: Photon bloom, ImpactClient's flash).
3. Not clashable yet: the flying Haki slash (HakiSlashes, a projectile): could meet a slash or a blow later. Iron Body and the
   Shark Skin Counter throw no blow (their own rules hold).
4. (Done in 2.4.0: the Water Prison (U, in the Five-Thousand Brick Wave's place: "pulls both hands up into the sky (Needs an
   entity to target) the entity selected gets trapped and floats a bit off the ground in a water bubble a bit bigger than the
   entity's or player's size for 5 seconds. (perfect stun, entity can only look around.)": breakable by enough damage or the
   Conqueror's, with a cooldown, unless the owner says otherwise).) Six Powers: done in 2.4.1. Every request on the old lists is
   done; what is left is the owner's reports, the flying Haki slash's clash (item 3), and the white screen (item 2). (Galaxy Fist: done in 2.3.7; Water Bullet: 2.3.8; Brick Fist: 2.3.9.)

## Not verified (only the owner can)
Everything in game: clashes happening at all (timing, facing, reach), the hit-stop and the throw (Impact.hit on both), the stagger,
the impact frame on a real GPU and on Android (the blend factors and the framebuffer blit), the Ryou burst, the recoils, NPC clashes.
