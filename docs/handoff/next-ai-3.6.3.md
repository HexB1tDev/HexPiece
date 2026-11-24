# HexPiece: handoff for 3.6.3 (written at the owner's word: "Handoff with references and prompt now")

This is the newest handoff. Read it whole, then `docs/handoff/next-ai-3.6.0.md` (this chat's every request, verbatim, sections A to N:
read I to N closely: the lightning rework and the whole Mochi story), then POINT ONE and sections 0 to 7 of
`docs/handoff/next-ai-3.2.2.md` (every standing rule). Where they disagree, this file is newest and wins.

Branch: `claude/nice-bell-gam7qf` (fetch it first). Repository: HexB1tDev/HexPiece. `build.py`'s VERSION is **3.6.2** (released, green:
Build #222, Release #153). The work below is committed and pushed **unversioned**: the next version to release is **3.6.3**
(`python3 tools/next_version.py --write`), once the base Mochi kit is finished, rendered and judged.

---------------------------------------------------------------------------------------------------------------------------------------

## A. POINT ONE (the owner's standing orders: keep this first and pass it on)

1. Every fruit in the style of the Light, Gura and Magma fruits: 3D meshes as FireMesh / GuraArt / MaguArt build them, clips with no head
   and no unused limb keyed, a first-person hand move per move, CC0 web sounds, every effect rendered and judged by you.
2. With the Conqueror's Haki and Ryou on, every move of every fruit throws the Conqueror's lightning, out of its end explosion when it has
   one (ConquerorAura.exploded / ItoMoves.lightning).
3. ONE Haki lightning in the whole game: the Conqueror's release's (G). Since this chat it is THIN (ConquerorStorm.WIDEST 0.9: the owner,
   "JESUS WHY IS THE LIGHTNING SO THICK"), zig-zags off its own straight way (no tangles), and is thrown level, up and down all round the
   player (ConquerorStorm.spread: "the middle is empty"). Never add another lightning look; render any lightning change beside the release
   from the same camera, with the previews' glow drawn as the game draws it (SparkPreview.glowed).
4. The player's chosen Haki colour (the lightning) and Ryou colour (the arm glow) from the start menu; never name a character in game.

## B. Where things stand

Released this chat, all green on GitHub:
- **3.6.0** (Build #219, Release #151): the Haki and Ryou colour pages; one crooked Haki lightning everywhere, made thin, zig-zagging out
  of the player level/up/down (next-ai-3.6.0.md H0); the coat past the elbow, the glow fading up the arm; the new bass-heavy CC0 lightning
  sounds, 6 dB quieter, one at a time (haki/LightningGate, client/haki/LightningSounds; the full release plays one strike).
- **3.6.1** (Build #221, Release #152): Meteor Fist at full power every throw (the owner: "Fixed too", "Full power always"); the Quake
  ultimate keeps its frame ("Keep it").
- **3.6.2** (Build #222, Release #153): every new fruit and every style obeys `/hp op on/off` (server/OpMode: no cooldowns, no energy,
  every style move open).

In progress (committed, unversioned): **the Mochi Mochi no Mi rebuilt on the owner's reference moveset** (section C). The owner, in
order: the Mochi list (8zzv 11), then "omg please look at these gifs in the page, they have the target looks, of every single move bro
https://official-fruit-battlegrounds.fandom.com/wiki/Dough", "their VFX especially the v2 is literally all I want, its beautiful", and
"Idk if you understand, I WANT THE OTHER MOVES, all of them, I want yours gone". Their answers: **"Dough base + V2 awakened"** (Dough's six
on Z X C V B N; M turns on the Awakening, which swaps in DoughV2's six, with trident swings on left click); keep only **"Left-click
punches"** of the old kit (Mochi Tsuki, Sticky Mochi, Future Sight and every other old move removed).

## C. The Mochi rebuild: what is done, what is left

Done (builds green locally, 13 suites; previews rendered):
- The material (MochiArt.lit): mochi, Haki and iron lit smoothly (wrap light, lilac shade, thickness, warm bounce, light through the
  edges, a satin shine; glossy Haki black with a cold blue rim and a blue shine that is never white) instead of five flat bands.
- The donut (MochiArt.donut, MochiDraw.donut): puffy, lumpy, dripping, a fist filling its hole (knuckles out), an arm out of the hole as
  wide as the hole; it forms from a lump that puffs up and opens its hole (MochiDraw.springy) and goes the same way backward. Every donut
  of the fruit is this one (the owner: "the rolling donut ... should look like the donuts in the awakening v move, look at it 1 of those").
- Arms as the arm itself: MochiFx.mochiArms hides the player's own arm and sleeve (and in first person the hand) while a mochi punch's arm
  is out (the owner: "turn the user's arms into the mochi not create another arm above it"); AstralArt.hide lets previews do the same.
- server/MochiKit and server/MochiMoves rewritten on the new kit (the old moves gone from the server and the client):
  - LMB **Mochi Punch** (kept).
  - Z **Grilled Mochi** ([Z] hold; the old held Yaki Mochi, the owner's "held, the bigger the explosion the longer, only 1 arm pointed"):
    the pointed forearm swells into a puffy mass with lumps and drips (MochiDraw.swell), clip keys only the right arm; let go, the fist
    flies on fire; **a body it meets is carried along on it** (GRILLED_CARRY 8 ticks, the reference's) then it explodes: radius 2.2 to 7
    by the hold (yakiRadius), damage 9 to 20 (+25 % coated), a white-hot star of rays and rings of fire (MochiDraw.boom).
  - X **Mochi Muddle**: a trail of dough poured along the ground ahead (13 long, 2.6 wide, spreading 2.2 a tick, lying 90 ticks): who it
    reaches or who walks in is stuck 40 ticks and hurt 7 near its start to 4 at its end (once each). Art MochiDraw.muddle. Judged: good.
  - C **Lotus Mochi Combo**: a donut beside the shoulder, its fist pounding the one ahead five times (held in mochi), an uppercut throws
    them up (tick 14), a giant fist out of a donut LOTUS_HIGH over them slams them down (tick 26: knockdown, quake, the lightning). Art
    MochiDraw.lotus / lotusSlam. **Not right yet**: the donut sits on the user's LEFT (MochiDraw.lotus uses `x - sx`; the right is `+sx`
    there: fix it) and its jab did not show in the render at age 6: check its punch phase; render the slam's donut in frame.
  - V **Chestnut Mochi**: a spiked donut over the right shoulder (MochiDraw.chestnutOffset, shared by the server and the client) for 100
    ticks, a spike every 4 ticks at the nearest foe within 16 (1.3 each). Art MochiDraw.chestnut / spike. Render it with a camera that
    shows the donut whole, and compare with the reference (section D).
  - B **Mochi Anemone** ([B] hold): a cluster of 8 donuts over the place aimed at (14 off, radius 3.4), Haki fists punching down out of
    them every 3 ticks; who is inside is stuck and hit. Art MochiDraw.anemoneArea. Judged: reads well (donuts with black arms down).
  - N **Buzz Cut Mochi** (cooldown 6000 ticks, the owner's "5 minutes"): for now the old wheel (now one donut rolling, ground torn up
    behind it, dust) and the club; **the owner's full cinematic (next-ai-3.2.2.md 8zzv 10, verbatim) is still to build**: see E.
  - M **Awakening**: says its moves come in the next version (MochiKit.AWAKENED = MOVES; MochiFx.awakened false).
  - FruitEnergy: mochi_yaki 30, mochi_muddle 25, mochi_lotus 30, mochi_chestnut 30, mochi_anemone 35, mochi_buzzcut 50, mochi_awaken 50.
  - Tests: HudTests.mochi rewritten (the keys and names, the old moves gone, the holds, the five-minute cooldown, the beats).

Left for 3.6.3, in order:
1. Fix Lotus (side and jab), check Chestnut's look, render every new move from several cameras and in first person (MochiShots |mfirst
   needs the new moves), judge, fix, render again. Compare each with its reference GIF (section D) and bring the effects toward them:
   the owner wants the reference's VFX ("literally all I want"): fire arcs and embers round the donuts' fists, white swirl trails, white
   splats, shockwave rings, a white-hot star burst on explosions.
2. Clips: the new moves reuse old clips (Muddle "mochi_nagare", Lotus "mochi_punch_1", Chestnut "mochi_amadare", Anemone "mochi_donuts",
   Buzz Cut "mochi_club"). Write proper ones in tools/mochi_anims.py if they read wrong (no head, no unused limb; lint must pass), and
   check style/HandMoves gives each a first-person hand move.
3. DragonTests.mochiArt: add the new pieces (muddle, lotus, lotusSlam, chestnut, spike, anemoneArea) to the quad budget (under 16k) and
   the owner's black-arm rule; drop the old pieces it still draws.
4. Remove the dead old art (MochiDraw tsuki, kaku, yanagi, ground, wave, walls, rain, dome, hole, glob, patch, the ring-round-the-user
   anemone, donuts) and the old clips (tools/mochi_anims.py, client/npc/MochiPoses) once nothing uses them; check client/FruitKeyN's
   double-tap-Shift dodge no longer shows for the Mochi fruit.
5. **Buzz Cut Mochi** as the owner described (8zzv 10): the wheel (one donut, as now) rolling and catching (anyone near too, the
   reference's), fluidly unrolling into the body, the right forearm swelling into a long spiked Haki mace (no fist, part of the arm),
   the horizontal swing, the body stuck to the mace, three full windmill rotations round the shoulder with thick curved trails, the
   pause overhead, the overhead slam with the body under the mace, a crater, fractures, debris, crimson lightning, two black-and-white
   impact frames (the owner's own word for this move overrides "frames only on clashes"), a cutscene camera for both, 5-minute cooldown.
6. Release 3.6.3: next_version, `.github/release-notes-3.6.3.md` (plain words: the old Mochi moves are gone, each new move as the player
   sees it; the Awakening's set comes next), handoff, push, both GitHub runs green, send the owner previews (SendUserFile, "previews, not
   the game").
7. Then 3.6.4: **the awakened DoughV2 set** on M: LMB Mogura Lacerations (five trident swings, the last breaks a block), Z Grilled Lance,
   X Elastic Lasso, C Oblivion, V Power Mochi (two flaming donuts at the shoulders 20 s: the left click becomes their fists), B Blazing
   Storm, N Scorching Buzz Cut (each's look in D).

## D. The references (the owner's target look and moveset; describe, never copy assets)

Pages: https://official-fruit-battlegrounds.fandom.com/wiki/Dough and the awakened DoughV2. The page itself is behind a bot check; its
API is not: `https://official-fruit-battlegrounds.fandom.com/api.php?action=parse&page=Dough&format=json&prop=images|wikitext` (and
`page=DoughV2`), then `action=query&titles=File:X.gif&prop=imageinfo&iiprop=url` for each file. The HexPiece environment allows
official-fruit-battlegrounds.fandom.com (added this chat) and static.wikia.nocookie.net. Most "GIFs" are animated WebP: read them with
Pillow in a fresh scratch folder (`python3 -I`), converting each frame as it comes (ImageSequence), and make contact sheets. Never commit
them (another game's assets).

The files (download into the scratchpad; the `?cb=` part may be dropped):
- Dough (the base kit): Grilled Dough `https://static.wikia.nocookie.net/official-fruit-battlegrounds/images/9/90/Grilleddough.gif`,
  Dough Muddle `.../images/a/a3/Doughmuddle.gif`, Lotus Dough Combo `.../images/6/64/Lotuscombo.gif`, Chestnut
  `.../images/f/ff/Chestnut.gif`, Anemone `.../images/3/3b/Anemone.gif`, Buzzcut `.../images/2/26/Buzzcut.gif`.
- DoughV2 (the awakened kit): Trident Lacerations `.../images/a/a8/9hkng3.gif`, Grilled Lance `.../images/0/01/1205.gif`, Elastic Lasso
  `.../images/d/d1/Elastic_Lasso.gif`, Oblivion `.../images/4/4f/Oblivion.gif`, Power Dough `.../images/2/2e/Power_Dough.gif`, Blazing
  Storm `.../images/8/87/Blazing_Storm.gif`, Scorching Buzzcut `.../images/7/7c/Scorching_Buzzcut.gif`.
  (`...` = `https://static.wikia.nocookie.net/official-fruit-battlegrounds`.)

The page's own words for each move (the moveset to match):
- Grilled Dough: "User forms a dough fist around their arm, dough with armament haki fist at the end of it. The user then throws said
  dough fist forward which explodes with fire once it has reached its maximum range or hits an object. If it hits or runs into an enemy,
  it will hit the enemy and then push them along with the projectile."
- Dough Muddle: "User forms a trail of dough on the ground, stunning any player that walks inside it. Damage decreases the farther the
  player is from the beginning of the trail and stuns them."
- Lotus Dough Combo: "User summons an armored dough fist and punches and whoever is hit will be launched up in the air and then thrown
  back down."
- Chestnut: "User creates a donut in the air covered in spikes that shoots spikes attacking the nearest player that will always hit as
  long as they are in range."
- Anemone: "User summons a barrage of armored fists and will continuously attack anything that gets caught inside, can be held to
  increase the duration of the attack."
- Buzzcut: "User turns into a wheel of dough that can go up walls but not travel sideways. If the user runs into another player as the
  wheel, they will grab them and swing them around, grabbing anyone else whos near, and then slams them to the ground."
- Trident Lacerations (V2 M1): "User wields Katakuri's trident and swings it 5 times with the last hit bypassing."
- Grilled Lance: "User holds their lance out with mochi forming around it and spinning it violently, and if charged; inflates the said
  user's arm with haki infused mochi and it is engulfed in flames. It then is launched at the opponent. And explodes after a few seconds."
- Elastic Lasso: "User forms a stretchy, elastic white lasso made out of mochi ... and throws it at an opponent to bring them towards the
  user, allowing itself as a combo extender."
- Oblivion: "User stomps the ground, sending a surge of mochi in its path with low range, grabbing the opponent and exploding on them if
  caught."
- Power Dough: "User forms two dough donuts with coated haki flaming fists for ~20 seconds that will act as your new and improved M1s with
  increased range."
- Blazing Storm: "User forms 6 Haki coloured flaming dough arms that barrage the opponent violently, ending with a final strike. This move
  can be aimed."
- Scorching Buzzcut: "User turns into a hot, flaming wheel of dough covered in spikes, rolling the opponent over (If the user holds the
  move). If the user stops holding the move, they will begin to swing their arm, which is now a giant spiked mochi fist coated with fire
  and haki, 3 times, in a very wide radius, before finally slamming them down with a larger AOE than its predecessor."

What the GIFs show (read frame by frame this chat):
- Overall: the dough bright white to ivory, puffy, lumpy, splashy: blobs with soft grey shading, drips, droplets and splats flung off;
  black Haki arms are long square bars; round every move orange fire (flames on the dough, crescent fire arcs swirling, embers), white
  motion arcs and swirl trails, shockwave rings, flying debris; explosions a white-hot star burst with long spiky rays, then fire swirls.
- Grilled Dough: side-on, one arm out; a huge puffy white dough mass swells round it splattering droplets, a black fist at its end;
  about a second; the fist flies and bursts in a fireball with concentric rings of fire and a dark ring.
- Buzzcut: a bright white upright donut (motion-blurred) rolling fast, white wind slashes, smoke puffs, the floor's tiles torn up and
  flipping in a long winding double row; on contact a black spiked club; the victim swung round: a whirling white vortex of motion trails
  on the ground; a cinematic angle, a black-and-white frame; the slam: a black splattered crater, red streaks, blocks flying.
- Anemone: a cluster of many small white donuts in the air over the target area, each firing long black Haki arms down into it, white
  bursts, a white translucent dome flash, swirl trails, white splats left on the ground fading.
- Power Dough: two big lumpy white donuts at the shoulders, drips hanging, a soft orange-red fire glow behind; a black fist fills each hole
  and punches out as a long black bar wrapped in orange fire arcs, embers flying; a fiery burst with rings as they appear.
- Blazing Storm: a fiery burst with orange arcs; six long black square arms from white mochi puffs at the user thrust together at the
  target, wrapped in fire; the last strike all six fully out; a big fire explosion with white blobs and black shards.
- Scorching Buzzcut: an upright white wheel with black spikes wreathed in flames, fire spraying off the ground, the ground cracking; the
  swing: a huge ring of motion on the ground (white swirls, a purple Haki glow ring, an orange fire swirl, black rocks whirled round, red
  lightning); the victim flung up in a column of fire; a grey frame, a white flash; the slam: a crater, blocks flying, red lightning, fire.
- Grilled Lance: the arm swells into a big round puffy mochi ball with purple Haki glints and flames; launched; a huge white-hot star
  burst with long rays, the screen flashing white, then fire swirls, embers, flying chunks.
- Oblivion: a surge of mochi erupts at the target (cream splashes, a swirl ring on the ground), rises into white walls round it like a
  basin holding it; then bursts outward: cream shards everywhere, pink-magenta energy and red lightning.
- (Not yet looked at closely: Dough Muddle, Lotus Dough Combo, Chestnut, Elastic Lasso, Trident Lacerations: make contact sheets of
  them before finishing those moves.)

## E. How to work here (this chat's)

- Environment: the personal "HexPiece" (it allows freesound.org, the One Piece wiki, the Fruit Battlegrounds wiki, static.wikia). A
  session on Default: get_environment, then switch_environment (network_only, its switch_statement copied exactly).
- Build: `python3 build.py` (about 70 s, 13 suites); a new SRG name in client code needs `python3 tools/srg_shapes.py` (it fetches
  MCPConfig's joined.tsrg).
- Mochi previews: compile only MochiArt/MochiDraw into a scratch folder before build/classes for quick looks:
  `javac -nowarn -cp $CP -d $S/mart src/main/java/com/hexhaki/piece/mochi/MochiArt.java src/main/java/com/hexhaki/piece/mochi/MochiDraw.java`
  then `java -cp $S/mart:$S/fx:$CP FxPreview $D/seiryu.sry $D/seiryu.png out.png --size=480x360 'e:yaw:pitch:dist:tx:ty:tz|mmuddle:4' ...`
  (FxPreview compiled with `-sourcepath tools/preview` into $S/fx; $D the dragon cache). Shots: mparts, mdonut, mpunch, myaki (t < 0
  held, then flight and blast; a 4th field the hold), mwheel, mclub, mmuddle, mlotus, mchestnut, marea, mfirst. The preview cannot build
  game objects (Vec3 is a stub that throws): keep positions in plain helpers (MochiDraw.chestnutOffset).
- Lightning previews: SparkPreview.render draws glow as the game does (alpha = brightness); HakiColourShots is the owner's camera.
- Stop rule: at 98% weekly usage write this file and next-prompt.md first, commit, push, stop without checking CI.

## F. The next chat's requests, verbatim (newest last)

1. (At the start, after the environment switch was first refused, then allowed: "Switch + add hosts"; the HexPiece environment now also
   allows official-fruit-battlegrounds.fandom.com and static.wikia.nocookie.net.) The owner: "It is very important that you do not give
   up until the moves look exactly like the reference GIFs that you have. If they don't look like the reference GIFs exactly, to very
   minute detail, I don't want them."

## G. The base GIFs read frame by frame (the next chat; the target for every base move "to very minute detail")

Timing: the WebP "GIFs" run 40 to 80 ms a frame (tools: the scratchpad's webpdur.py reads the ANMF durations; Pillow says 0), Buzzcut
and Chestnut are real GIFs at 48 and 40 ms. Roughly one frame = one game tick (50 ms). "User" = the one using the move.

- **Grilled Dough** (249 frames, 11.6 s; the target stands about 3 blocks in front of the user, side-on camera): f3 standing; f6 the
  right arm thrust out level at the target; f9 a huge white puff bursts round the arm (bigger than the user); f12 it swells tallest
  (twice the user's height, splatters flying); f15 to f30 it settles into a thick horizontal arm of dough (as thick as the user's torso,
  about 1.5 user-heights long) from the shoulder, puffy and lumpy, a black Haki fist block at its end, drips falling off it and white
  splats left on the floor under it; f33 another big puff (the throw); f36 the fist leaves, a spark of orange fire; f38 to f62 the
  explosion at the target: a half-dome of CONCENTRIC RINGS (a tunnel: bright orange-yellow fire rings, a dark brown-black ring between
  them, white-hot edges), facing the camera, fire filling its middle, black smoke puffs and white droplets thrown out, damage 60; f64 to
  f110 the rings fade into a pale grey smoke dome shell with fire inside fading; then the target BURNS (2.5 damage ticks with a flame
  icon) for about 6 s more.
- **Dough Muddle** (129 frames, 6.1 s; camera behind the user, the target about 6 blocks ahead): f20 to f44 a charge pose (both arms out
  to the sides, about 1.2 s); f44 to f46 the user dives forward; f48 a white block of dough engulfs the user; f50 to f54 a huge splash
  bursts out and up (a column of dough, droplets thrown everywhere); f56 to f66 it lands as a big flat splat puddle round the user (the
  user crouched in it, sunk to the waist), the column falling behind, translucent swirl rings (wind) round the puddle on the ground;
  f70 to f82 the puddle runs forward to the target and under it (a broad white trail), the user stands up; f88 to f128 the puddle keeps
  spreading wider with SPIKY radial splash edges (like spilled paint), splats scattered round, faint swirl rings.
- **Lotus Dough Combo** (162 frames, 7.5 s; camera behind the user, the target about 6 blocks ahead): f18 a white ribbon of dough swoops
  in from the user (a comet trail) and f21 curls into a swirling ring beside the target (on the user's left of it), f24 to f27 the
  swirl balls up; f30 a big bright white flash ball with translucent shock rings round it; f33 to f48 a DONUT stands there facing the
  target (its black fist in the hole), big spherical translucent white shock rings round it, white splats falling under it; f51 a big
  white flash, the fist punches out of it into the target (f53 to f57), 11.2 damage, white splats; f59 to f69 the target is launched (the
  camera shakes and follows it); f72 to f125 the target high in the sky: two or three white bursts with rings hit it there (11.2 each),
  then a black Haki bar from above (f111 to f117), a white puff, the target falls with white speed streaks (f126 to f128); f130 a white
  desaturated flash frame; f132 to f161 the slam: a flat white dough splat on the ground ringed by big tan rock chunks thrown up, cubes
  flying, a white shock ring rising and widening above it, 45 damage.
- **Chestnut** (247 frames, 9.9 s; the user at the right, three targets at the left): f17 to f21 a comet of dough flung UP from the user
  curls into a swirl; f24 to f28 a white ball high over the user (about four user-heights up); f32 it flashes (a white puff with rings);
  f36 a DARK SPIKED DISC (Haki black-grey, a flattened donut covered in spikes round its rim and on its faces, about 1.5 user-heights
  across, tilted about 45 degrees) with a white translucent ring round it; f40 to f186 it hangs there and fires: a thin white tracer line
  from it to the nearest target with a small black spike flying down it, a white-blue spark star on the hit (3.6 damage), one about
  every 5 frames (4 ticks); about 6 s; f190 it turns white, f194 it falls flat (a white spiky disc) onto the user, f198 to f212 it splats
  into a white puddle round the user's feet with droplets and a faint ring.
- **Anemone** (274 frames, 9.1 s; the user in the foreground, the area about 8 blocks ahead): f12 to f18 about twenty small white donuts
  pop up scattered over the area at many heights (1 to 6 blocks up, spread wide); f24 to f192 long black Haki bars shoot out of them
  diagonally down into the area, many at once, white bursts where they land, a translucent white dome over the area, white and cyan swirl
  trails, a dark crater in the middle, rocks; f192 to f210 a last strike, the dome collapses into splashes; f210 to f270 white splats left
  on the ground fading out (about 2 s).
- **Buzzcut** (200 frames, 9.6 s; camera above): f15 a white puff (the user becomes the wheel); f20 to f80 a bright white blurred wheel
  rolling fast, black wind lines, a long winding DOUBLE ROW of torn-up floor tiles flipped up behind it; f85 to f95 it hits a target: a
  black spiked club, 22 damage; f100 to f140 a whirling white vortex of motion rings on the ground round the user (the victim swung
  round); f145 a grey frame; f150 to f160 a close cinematic camera on the user swinging the giant black arm down; f165 a pink-white flash
  frame with red streaks; f170 to f180 the impact, red sparks; f185 to f195 a black star-shaped splatter crater with cracks, blocks flying.

## H. The owner's next messages (verbatim; sent while the Mochi GIFs were being read, before "carry on")

2. "before you carry on 2 things. I asked over 10 times, the conquerors haki is the 3d lightning, yet the AI keeps putting the 2d, over
   and over and over again, please for the last time, make the lightning 3d once it for all, all of it.. im so tired of seeing this fuck
   ass 2d lightning, I keep asking for it to be gone, yet you keep flipping me off."
3. "Also when powers are turned off, the hud on the screen is to dissappear until the power is once again activated."
