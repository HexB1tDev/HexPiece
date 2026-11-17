# HexPiece: handoff after 2.3.6 and 2.3.7 (clashes; the Galaxy Fist made Iron Fist's)

Read CLAUDE.md first. This note is the newest; next-ai-2.3.2.md covers 2.3.2 to 2.3.5 (one animation set for every blade, the
owner's in-game bugs, Haki hands from the skin, bigger swings), next-ai-2.3.0.md the clash rules in the owner's words, next-ai-2.2.6.md
the open requests in detail. Branch `claude/nice-bell-gam7qf`. **The next version is 2.3.8** (`python3 tools/next_version.py --write`).
Nothing was ever tested in game (only the owner can).

## State (session 4)
- Asked first, as the handoff said: the owner had NOT tested 2.3.4 or 2.3.5 in game yet ("Not yet, carry on") and had not looked at
  the Fishman Karate white screen again. So both are still open: ask again next session.
- 2.3.6 released: clashes (next-ai-2.3.0.md item 1). Build green locally (PieceVerify proves every reference, 30 sword animations
  natural and clear, 11 suites); check the branch's Build and Release runs.

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
4. The Fishman Karate rework, then Six Powers (next-ai-2.2.6.md items 3 and 4). (Galaxy Fist: done in 2.3.7.)

## Not verified (only the owner can)
Everything in game: clashes happening at all (timing, facing, reach), the hit-stop and the throw (Impact.hit on both), the stagger,
the impact frame on a real GPU and on Android (the blend factors and the framebuffer blit), the Ryou burst, the recoils, NPC clashes.
