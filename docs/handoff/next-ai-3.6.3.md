# HexPiece: handoff for 3.6.3 (written at the owner's word: "Handoff with references and prompt now")

This is the newest handoff. Read it whole, then `docs/handoff/next-ai-3.6.0.md` (this chat's every request, verbatim, sections A to N:
read I to N closely: the lightning rework and the whole Mochi story), then POINT ONE and sections 0 to 7 of
`docs/handoff/next-ai-3.2.2.md` (every standing rule). Where they disagree, this file is newest and wins.

Branch: `claude/nice-bell-gam7qf` (fetch it first). Repository: HexB1tDev/HexPiece. `build.py`'s VERSION is **3.6.6** (released by the
fifth Mochi chat: the Awakening's whole set, section S; check its Build and Release runs). Next: the owner's notes (section T).

---------------------------------------------------------------------------------------------------------------------------------------

## A. POINT ONE (the owner's standing orders: keep this first and pass it on)

0. The Mochi fruit is FLUID AND ALIVE (the owner, N 16 and N 19: "more stretchy, more animated, more squishy, it's mochi for god's sake,
   the arms should have more elasticity"; "mention the fluidity and aliveness of the mochi we want to the new AI"): every arm, fist,
   donut and mass of dough stretches, wobbles, squashes, springs, breathes and drips; never stiff, never popping in or out (N 19).

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
4. "the tip of the lightning has to be sharp" (sent while the 3D lightning was being built: every bolt's free end a sharp point, never
   a round or blunt end).

## I. Done in the next chat (committed; not yet versioned): the one Haki lightning made truly 3D, with sharp tips

What was 2D (found by rendering the release and a move's blast close up, tools in the scratchpad: BoltLook = SparkPreview.render at
960x640, HakiColourShots for all seven colours):
- The storm's strokes (the release on G, every move's blast through ConquerorAura, the sparks, the clash, the sword and style crackles:
  all ConquerorStorm.strike / bolts / blast / ClashBurst pairs of Bolt.BODY + Bolt.EDGE) drew their rim as FLAT bands (BoltMesh.band:
  a stripe of colour each side of a black stripe, then a black ink stripe): at 3.6.0's thin widths the whole bolt read as a flat 2D
  cartoon line. Now (BoltMesh EDGE case): ONE round lit tube (tube3d) shaded by BoltMesh.rimLitShade: glossy black (or the set's own body
  colour) facing the eye, a dark sheen of the colour on its lit side, the Haki colour wrapping its silhouette (rim light, white-hot at the
  very edge), a glossy highlight; inked outside; the glow beyond. The paired body stroke (alpha 0xF0: BoltMesh.paired) draws nothing
  (it would only hide under the tube): no extra quads.
- The fighting styles' Armament crackle (StyleArt.crackle) drew two crossed flat strips in a fixed red: now it is the one Haki lightning
  (ConquerorStorm.strike drawn by BoltMesh.build into the style batch, the glow through BoltMesh.glowed), in the thrower's Haki colour
  (client/fx/StyleVfx.rim sets StyleArt.crackleRim from HakiBolts.colorOf, now public).
- The original's flat Photon beams were still made as a fallback (HakiLightning's 800-beam budget, and any composition not played
  through HakiBolts): HakiLightning.draw no longer makes them at all; only the 3D bolts exist.
- Sharp tips (owner: "the tip of the lightning has to be sharp"): ConquerorStorm.sharpen brings a stroke's width to 0 in a straight
  needle at its free ends (about 6x its width long, 0.25 blocks at least, 35 % of it at most): every storm strand and blast bolt at both
  ends (bolts(Strand); the strand's own widths, read by the server, untouched), every strike at its far end (strike(..., sharp); the
  chained pieces of the original's compositions and the blade crackle pass false: BoltPainter.point points their free ends), ClashBurst.
- LightningTests.solid3d: in every Haki colour the blast's rim strokes are shaded tubes (many shades, not bands), the body strokes add
  nothing, every blast bolt is pointed at both ends, a strike at its far end, a chained piece keeps its end full.

## J. Done in the next chat (committed; not yet versioned): no HUD while the powers are off (H 3)

While every power is off (R held: server/Powers, the client's BindsClient.off through CombatMode.powersOff): the abilities panel
(AbilityHud.draw), the fruit energy bar (EnergyBar.draw) and the old "POWERS OFF (hold R)" badge (CombatMode.powers) draw nothing; the
only thing left is the small fill bar while R is held toward the switch (feedback for turning them back on). Everything is back the
moment the powers are on. Screen flashes of effects (IronFx, ClashFx, FinisherFx) and the broken-bones overlay are effects, not HUD:
left as they were.

5. The owner's answers (AskUserQuestion, after the 3D lightning and the HUD were committed): release: "Hold until Mochi is done" (no
   3.6.3 yet: the lightning and HUD fixes ship with the finished Mochi kit); branch: "claude/nice-bell-gam7qf" (push there, where the
   Build and Release workflows run; this session's own ccr-* branch has no workflow).

## K. Done in the next chat (committed; unversioned): the Mochi material and Lotus Mochi Combo after their GIFs

- The dough's colour (MochiArt MOCHI_*): sampled from the GIFs (near pure white where lit, a soft cool grey shade about 215, 213, 222,
  the deepest about 204, 195, 200): now white, the shade DAD6E0, the deep C6BEC9, and most of a lump white (the wrap starts earlier).
  Before it was a lilac 194, 182, 197 down to 156, 141, 166: far too dark next to the reference.
- Lotus Mochi Combo rebuilt on the reference's beats (G; MochiDraw.LOTUS_*): a comet of dough flung from the LEFT hand high over and
  down to the user's left (the reference's donut stands at the user's left: the last handoff's "fix the side" was wrong), curling into a
  swirl (4), balled up (12) and flashing white in thin shock rings; the big donut (radius 1.95, 2.8 left, 0.9 ahead; in one's own first
  person 2.3 further ahead) out of the flash (15), dripping and splatting on the ground, shock rings pulsing; a second flash (31) and its
  fist punches onto the one aimed at (34; server: LOTUS_REACH 8, 6 damage); thrown 13 up in 10 ticks (39: a white burst, a ring, dough
  and rocks on the ground, streaks under them), held there (server/MochiMoves.Juggled moves them along MochiDraw.lotusHeight); three
  small donuts punch them from the left, the right and below (57, 67, 78: 3 each, a white burst and rings each); a giant fist out of a
  donut 7 over them (87) rides them down from 96 into the ground at 106: a white flash, a flat splat of dough, rocks thrown, a ring
  rising, 12 damage, the knockdown, a crater that grows back (Craters.open 2.2), a quake and the Conqueror's lightning out of the impact
  (ConquerorAura.exploded: POINT ONE). The giant arm is drawn back at once after the slam (the reference shows none).
- Messages (MochiMoves.LOTUS, bx the phase): LOTUS_PUNCH, LOTUS_HIT, LOTUS_AIR, LOTUS_SLAM, LOTUS_GROUND; client MochiFx.drawLotus.
- The user's clip mochi_lotus (tools/mochi_anims.py lotus(): the left arm flings, holds the donut, draws back, thrusts with the punch,
  both arms flung up and out with the launch; arms only, lint clean, never over the face) and its first-person hand (HandMoves.LOTUS).
- HudTests: the beats in the reference's order, the body on the ground for the punch, high for the air blows, down at the slam, the
  donut at the user's left. Preview: MochiShots |mlotus:age[:c] plays the whole combo (the body moved as the server does).
- Grilled Mochi after its GIF (G): the swell (MochiDraw.swell, its ground for splats now passed): pressed, a huge white puff bursts
  round the pointed arm (SWELL_BURST 10 ticks, bigger than the user, splattering), settling into a long thick arm of lumpy WHITE dough
  (1.5 to 2.4 long, 0.38 to 0.58 thick by the hold) with the fist at its end: with Armament only the fist is black (the reference's
  dough mass stays white; before, the whole mass went black), drips falling and splatting on the ground; the throw bursts in a white
  puff (yakiPuff) as the fist flies on its fire jet; the explosion (boom, BOOM_LIFE 70): a white-hot flash, a small fireball, nested
  rings turned to the eye (orange fire rings with yellow-white inner edges, dark scorched rings between, a white-hot outer edge: the
  reference's tunnel of rings, the ground hiding the lower half), Magma's black-brown smoke puffs (FireMesh.SMOKE) off its rim, small
  white droplets, embers, then a pale grey dome of smoke lingering while the fire dies; the burn 6 s (UoMoves.explode fire 6).
- Mochi Muddle after its GIF (MochiDraw.MUDDLE_*, ticks from the press): the charge with both arms out (clip mochi_muddle, to 22), the
  dive (24 to 27, the body leaning in), a block of dough engulfing the user (MUDDLE_START - 4), the splash at their feet (MUDDLE_START
  28: a column of dough thrown up 3.8 and falling back, a crown of splashes, droplets everywhere), a thick pool round them (MUDDLE_POOL
  2.3, a mound over their legs that flattens), from MUDDLE_RUN 14 the puddle running out ahead at 0.6 a tick to 10 long (3 wide near,
  4.6 far), spiky splashed edges (36 flat spikes), splats round it, faint swirl rings; it lies MUDDLE_LIE 100 ticks then sinks. Server:
  the splash at the user's own feet (no longer 1.2 ahead), the pool and the trail glue and hurt as drawn (muddleReach, muddleWidth).
  First-person hand: HandMoves.MUDDLE.
6. The owner asked: "question, when does the V2 moves come in?" Answered: right after the base kit: 3.6.3 releases the six base moves
   matched to their GIFs (with the lightning and HUD fixes), then 3.6.4 brings the awakened DoughV2 set on M, each matched to its GIF.
7. The owner asked: "when is the v2 meant to be usable in game as in what moves are you planned to make from v2". Answered: in 3.6.4,
   right after 3.6.3 (the base six): M turns on the Awakening, which swaps the kit to all seven DoughV2 moves, each matched to its GIF:
   LMB Trident Lacerations (five swings, the last breaks a block), Z Grilled Lance (the lance with mochi spinning round it; held, the arm
   inflates with flaming Haki mochi, launched, exploding after a moment), X Elastic Lasso (yanks the target to the user), C Oblivion (a
   stomp, a surge of mochi traps them in walls, then bursts), V Power Mochi (two flaming donuts with Haki fists at the shoulders about
   20 s: left click becomes their longer punches), B Blazing Storm (six flaming Haki arms barrage the aimed target, a final strike), N
   Scorching Buzz Cut (a flaming spiked wheel rolling over them while held; let go: three wide swings of a giant spiked flaming fist and
   a bigger slam).
- Chestnut Mochi after its GIF (MochiDraw.CHESTNUT_*): a comet of dough flung straight up out of the right hand (clip mochi_chestnut,
  hand HandMoves.UP_END_HELD), curling into a swirl high over the user, a white ball, a flash in rings (to CHESTNUT_FORM 15), then the
  spiked disc (radius 1.15, 14 rim spikes and face spikes; Haki black with Armament, white without) hanging CHESTNUT_HIGH 4.2 over the
  head, tilted half way between the aim and straight down, ringed in thin white light, kicking as it fires; each shot a thin white
  tracer the whole way (FireMesh.ray), a black spike racing down it in 2 ticks, a white-blue spark star on the hit; it fires from 15 for
  CHESTNUT_FIRE 120 (server CHESTNUT_TICKS 135, 1.1 a spike), then turns white, lies flat, falls onto the user and splats into a
  puddle with droplets and a ring round their feet (CHESTNUT_FALL 30).
- Mochi Anemone after its GIF (MochiDraw.anemoneArea, ANEMONE_*): 20 small donuts (ANEMONE_SMALL 0.6, anemoneDonut: scattered 0.35 to
  1.6 radii out, 1.4 to 5.5 up) popping up one after another, 8 more standing half sunk in the ground round the area like arches; the
  Haki bars shoot out of them into the area (the server's strikes), a white burst and ring where each leaves its donut, a glow where it
  lands; a bright see-through white dome over the area (brighter the more bars strike), white swirls round it and pale blue ones high
  over it, a dark crater and rocks; let go: a last flash, the dome bursts into splashes, the donuts puff away, 14 white splats lie over
  the ground fading (ANEMONE_LINGER 50). Its many donuts are drawn coarse (MochiArt.low: coarser rings, each fist one rounded block):
  about 6k quads without the bars (it was 12.8k).
8. The owner's answer (AskUserQuestion) on Buzz Cut Mochi's swing (the GIF whirls the stuck victims round the user horizontally; the
   owner's 8zzv 10 text said three vertical windmill rotations): "GIF: horizontal whirl" (the victims whirled round the user level
   with the ground in a white vortex of motion rings, then the arm raised overhead and slammed into a black star crater). Everything
   else of 8zzv 10 stands where the GIF agrees (the wheel, the fluid unroll, the spiked Haki forearm, others near caught too, the two
   black-and-white impact frames, the crater, the 5-minute cooldown).
9. The owner (while Buzz Cut's first renders were shown): "thats not the final look right? I mean have you looked at the reference and
   at yours?" Answered: no, not final; the gaps named: the GIF's wheel is a bright motion-blurred white blur (ours a clean donut), the
   torn ground is flat floor planks flipped in a double row (ours brown rocks), the crater is a big black splatter star (ours smaller,
   navy, greenish dust), the user turning with the whirl, the impact frames and the camera not in the preview yet.

## L. Buzz Cut Mochi: in progress (art, clip and hand committed; the server and client NOT yet switched over)

Built (MochiDraw "Buzz Cut Mochi" section, pure; preview MochiShots |mbuzz:age plays the whole thing from the press):
- The timeline from the catch (BUZZ_*): unrolled 4, the swing's blow 7, whirled 5 turns to 52 (buzzTurn: speeding up over its first
  third, ending where it began), raised overhead by 60 (buzzLift 1.95 rad), a beat to 63, slammed down by 66 (lift -0.42: onto the
  ground ahead), held to 76, the arm back by 86. buzzReach 3.4 to 5.4 (stretching in the whirl), buzzSwell. buzzArm(t, feet, yaw0):
  the right shoulder, the arm's way, its reach and THE USER'S OWN YAW NOW (yaw0 - turn in degrees: the user turns with the arm).
- buzzWheel (an upright donut of BUZZ_WHEEL 1.3 rolling, pressed on the ground, white arcs of motion round its rim, black wind
  lines, smoke), arc (an arc of light in any plane), buzzTiles (a double row of chunks flipped up behind the wheel along its path,
  BUZZ_TILES 70), buzzMace (the spiked forearm: white upper arm, a long black tube swelling to 0.65 thick, a rounded end, 6 rings of 6
  spikes and 5 out of its end, crimson flickers; the material MochiArt.HAKI_CRIMSON: Haki black lit crimson, the owner's "deep crimson
  highlights"), buzzVortex (white rings of motion on the ground spiralling behind the arm, a thick trail behind its end), buzzCrater
  (a white flash, a black star of splats and cracks, red sparks and a ring, dust, rocks; BUZZ_CRATER_LIFE 110).
- Clip mochi_buzz (from the catch; arms and body; lint clean) and HandMoves.BUZZ.

Left to do (the plan):
1. Server (MochiMoves.buzzCut, Wheel): the roll BUZZ_ROLL 60 ticks max (the client moves its own player along its LOOK each tick,
   speed ramping to BUZZ_SPEED: steerable like the GIF's winding path; MochiFx.Carry/roll to rewrite), the server checks foes within
   BUZZ_WHEEL + 0.6 each tick; on contact catch them and anyone within 2.8 (and later anyone the mace end passes within 1.5 of, up to
   6): a new Live (Whirl) positions every victim along the mace each tick (MochiDraw.buzzArm), the blow at BUZZ_HIT (8 each, the
   impact frame, the lightning), the slam at BUZZ_SLAM (18 each, knockDown 60, Craters.open 3.5, ConquerorAura.exploded, Impact.quake),
   done at BUZZ_END. Messages (kind WHEEL): phase 1 roll, 0 roll ended (no catch: unroll), 2 caught (at the feet, by yaw0, id the
   first victim), 3 the slam's ground, 4 the swing's blow (at the mace end), 6 a victim attached (id).
2. Client (MochiFx): replace the Wheel record by a Buzz record (path history for buzzTiles, caught time, feet, yaw0, victims); hide
   the user's model while rolling (asWheel) and the right arm from the catch to BUZZ_END (hideArms, mochiArms); the local user's own
   yaw set each tick to buzzArm()[7] and their input frozen (steady); the impact frames on phase 4 and 3 for the user and the victims
   (ClashFx needs a public frame(at, power, colours, seed, classic) setting its Shown); the cutscene camera for the user and victims
   (like IronFx.camera/filmAngles/cinema, called from client/dragon/DragonCamera.position): a low side shot at the catch, high above
   looking down in the whirl (the GIF's top-down vortex), low in front looking up at the raised arm and the slam (frames 150 to 160),
   pulling back after.
3. Then look again, side by side with the GIF, at what the owner named (L 9 above): the wheel a bright motion-blurred white blur, the
   torn ground flat floor planks (not rocks), the crater a big black splatter star, the dust not greenish.
10. The owner, on Buzz Cut's mace: "dude, the arm increases to insane ammounts and then slams into the ground" (with an anime frame:
    Katakuri's Buzz Cut Mochi: his arm a long THIN WHITE band of stretched mochi, bowed like elastic, reaching far across the frame,
    flaring at its end into a splash of dough that holds a DARK SPIKED BALL (the Haki mace head, round, spikes all round); not a thick
    black club). So: the arm white and stretching to an insane length (about 16 overhead), the black spiked ball at its end, slammed
    down from there into the ground far ahead (the crater where the ball lands).
11. The owner uploaded a GIF (Officialref.zip, s9cBv2.gif, 601 frames, 320x180, plays forward then back): "this gif is from the
    actual show" (Katakuri's Buzz Cut Mochi on Luffy). Read frame by frame: the mace ball is ENORMOUS (Luffy a tiny figure stuck among
    its blunt conical spikes, frames 40 to 70; dark navy-black with pale highlights), at the end of the thin white elastic arm (10 to
    30); Katakuri's arm swollen and raised high (120 to 140), whipped down in a white blur (220), a white flash (250); the slam leaves a
    VAST FIELD OF GREY RUBBLE BLOCKS, the thin arm coming down from the sky into it, Luffy lying broken in the debris (270 to 340).
- Done after L 10 and 11: the mace is the owner's frame and clip now (MochiDraw.buzzMace): a thin WHITE elastic band of dough bowed
  behind its swing (buzzBend), flaring into tongues of dough round an ENORMOUS black crimson-lit spiked ball (buzzBall: up to BUZZ_BALL
  2.5 with the reach; 14 spikes, crimson flickers); buzzReach 3.4 at the catch, 6.5 at the whirl's end (lift 0.3 so the ball clears
  the ground), shot up to BUZZ_REACH_UP 16 as it is raised (straight up), slammed down to BUZZ_REACH_SLAM 13 ahead with its ball half
  buried in the ground. The ones caught sit on the ball's top (buzzStuck: server and client both use it) and lie crushed under it from
  the slam. The wheel at speed a bright white blur (a glow and blurred ring copies); the torn ground flat tan planks (MochiDraw.plank)
  tumbling at their own angles; the crater a big black star (16 rays and drops, HAKI_CRIMSON), pale dust (FireMesh.shell), 24 rocks
  thrown and (the show's) a field of 30 grey rubble blocks heaved up round it. Preview |mbuzz:age. STILL TO DO: the server and client
  wiring of L 1 and 2 (the move in game still plays the old wheel and club).

## M. Where this chat stopped (the owner: "handoff and prompt please")

Committed and pushed earlier this chat (all green locally, Build #225 green on GitHub for the first push): the 3D lightning and sharp
tips (I), the HUD hidden while powers are off (J), the Mochi material and Lotus, Grilled, Muddle, Chestnut and Anemone after their GIFs
(K), Buzz Cut's art after the owner's frame and the show (L, "Done after L 10 and 11").

In the last commit of this chat (see git log; check its GitHub Build run): Buzz Cut Mochi's server and client wiring (L 1 and 2):
- Server (MochiMoves): buzzCut starts the roll (BUZZ_ROLLING); Wheel catches the first foe the wheel meets and anyone within 2.8 (up
  to BUZZ_MOST 6), else BUZZ_MISSED after BUZZ_ROLL; Whirl from the catch: the blow at BUZZ_HIT (BUZZ_BLOW, 8 each), the ones caught
  moved onto the ball each tick (MochiDraw.buzzStuck), anyone the whirling ball passes near stuck too (BUZZ_STUCK), the slam at
  BUZZ_SLAM (BUZZ_GROUND, 20 each, knockDown 60, Craters.open 4, ConquerorAura.exploded, Impact.quake). The old club (MochiDraw.club,
  CLUB messages) is no longer sent.
- Client (MochiFx): Buzz records in WHEELS (the way rolled for the torn ground, the catch, yaw0, the victims), MARKS (the blow and the
  crater); one's own wheel rolls where one looks (Carry/roll, speeding up to BUZZ_SPEED); the user hidden while rolling and two ticks
  of the unroll; the right arm hidden from the catch to BUZZ_END (mochiArms: the mace is the arm); one's own yaw set to buzzArm()[7]
  each tick and held still (buzzTick); the impact frames on the blow and the slam for the user and the ones caught
  (client/fx/ClashFx.frame, new); the cutscene camera (MochiFx.camera, filmAngles, cinema: third person forced and restored), called
  from client/dragon/DragonCamera.position; drawBuzz draws it all.
- NOT done yet: none of this has been rendered or judged in motion beyond the preview (|mbuzz); the in-game camera shots are guesses
  to judge (by the user at the catch, high over the whirl, low in front for the raise and slam, back over the crater). Sounds: the old
  ROLL/SQUISH/SLAM; check they fit. HudTests: add the Buzz Cut beats (buzzStuck under the ball at the slam, buzzReach 16 up, etc.).

Next, in order:
1. If the last push's GitHub Build is red, fix it first.
2. Judge Buzz Cut again against both its references (the Fruit Battlegrounds Buzzcut GIF and the owner's show clip
   Officialref.zip/s9cBv2.gif, uploaded in this chat: ask the owner to send it again if it is gone) and the owner's 8zzv 10 text with
   the horizontal whirl (F 8); render it in first person too.
3. Task 5 of the plan: clips and hands are in for every new move; DragonTests.mochiArt with the new pieces (lotus*, muddle, chestnut,
   spike, anemoneArea, buzz*) under 16k quads; remove the dead old Mochi art (tsuki, kaku, yanagi, ground, wave, walls, rain, dome,
   hole, glob, patch, the old ring anemone, donuts, wheel, club, spiked if unused) and old clips (tools/mochi_anims.py, MochiPoses).
4. Release 3.6.3 (the owner held it until the Mochi kit is done: F 5): next_version, release notes (the 3D lightning, sharp tips, the
   HUD, the six base Mochi moves after their references), handoff, push, both GitHub runs green, send previews (SendUserFile).
5. 3.6.4: the awakened DoughV2 set on M (F 7 lists the seven moves as promised to the owner).

## N. The next chat (fourth on Mochi): the owner's messages, verbatim (newest last)

12. "use my imapct frame that looks like shit" (sent while Buzz Cut Mochi's impact frame was being judged: the classic frame
    (ImpactFrame: the negative, then the black frame with rays) rendered over the slam shot looked wrong; the owner wants THEIR impact
    frame, the one from 3.5.8 (clash/ImpactSprite, tools/impact_texture.py), on Buzz Cut's blow and slam.)

13. The owner (after 3.6.3's previews, with our slam render marked: a green line from the hand curving DOWN under the straight way to
    the ball, where our white arm arched UP over it): "fair warning buzzcut mochi is spinning with the air motion wrong (the end of the
    arm is heavier so the arm so  the heavy end should always be after the mochi elastic part". So: the elastic band leads the motion
    and the heavy ball trails after it (the band bows toward where it is going, never back against it), and at rest it sags under the
    ball's weight; in the slam the band bows down under the straight way.

14. The owner (with a sketch: three donuts round the victim in the air, top, right and lower left; an arrow up from the ground (the
    launch); the right donut's arm reaching across to the lower-left donut, the top donut's arm reaching down-left to it too, and the top
    donut's arm straight down at the end, fists as triangles at the arms' ends): "Lotus mochi is NOT approved either, the idea is there
    but its not like the gif, the image I sent is an example, the entity is basically stopped in mid air when in reality they're meant to
    be pushed in ragdoll from side to side depending where he was punched from basically from donut to donut carrying basically being
    glued with each punch to the next, fast." So: in the air the one hit is never held still: each donut's fist strikes them and carries
    them, stuck to it, fast across to the next donut, whose fist strikes them on from the side it stands on, tumbling like a ragdoll;
    at the end the top donut's fist drives them straight down. (Also "either": Buzz Cut Mochi is not approved yet: N 13.)

15. The owner's answer (AskUserQuestion, after N 13 and 14 were fixed and Trident Lacerations built): "Release fixes now
    (Recommended)": 3.6.4 now with the Buzz Cut and Lotus fixes and M turning on the Awakening with Trident Lacerations on the left
    click; Z to N while awakened come in 3.6.5 (until then they say so on the action bar).

16. The owner (while 3.6.4 was being released): "overall I am happy but feels like mochi is dead, it should be more stretchy, more
    animated, more squishy, it's mochi for god's sake, the arms should have more elasticity, etc" (the whole Mochi fruit: every arm,
    donut and mass of dough to stretch, wobble, squash and spring like mochi, never stiff).
17. The owner: "I also found traces of the old Haki in the wifi haki, it should use our latest one." (WiFi Haki still draws something
    of the old Haki look: it must use the one 3D Haki lightning, POINT ONE.)

18. The owner: "enough prompt + hand off now" (this chat stopped here: section R).

19. The owner: "mention the fluidity and aliveness of the mochi we want to the new AI". A STANDING ORDER for every Mochi move, base and
    awakened, now and later (with N 16: "it should be more stretchy, more animated, more squishy, it's mochi for god's sake, the arms
    should have more elasticity"): the mochi is never stiff or still. Every arm stretches like dough (thinner the further it is drawn
    out, a wobble rippling along it, sagging under its weight, shooting past its reach and springing back, snapping back with a
    rebound); every fist squashes as it lands; every donut and mass of dough breathes, wobbles, puffs up past its size and settles,
    drips; everything forms and goes fluidly (a lump swelling into its shape, slumping away), never popping in or out; dough splashes
    and splats when it hits. Build every new piece this way (MochiDraw.arm, elastic, springy, donut's breathing, MochiArt.CHURN are the
    tools) and judge every render for it as hard as for the reference's look.

22. The owner (after 3.6.6, while the Phoenix was being started: "btw before proceeding to the Phoenix, Mochi has some issues:"):
    "1. Trident in the awakening should not be held at the tip of the rod it it should be in the middle of the hand and should disappear
    when not using M1s, and manist from a mochi only when using M1s.
    2. I want 0 impact frames in this fruit, IMPACT FRAMES ARE ONLY FOR CLASHES.
    3. Buzz Cut Mochi is no longer a cutscene, you can look around, but the camera adapts to the height of the vertical lift of the move,
    the hit player(s)  dont get any special camera.
    4." (item 4 arrived empty: ask for it.) So: Mogura held by the middle of its shaft in the hand, shown only while the left click's
    swings are going (it forms out of mochi as they start and goes back into it after); no impact frame anywhere in the Mochi fruit
    (Buzz Cut's two are removed: N 12 is overruled); Buzz Cut's cutscene camera gone: the user looks around freely, the camera only
    rising with the arm's lift (third person), the ones caught get no camera of their own.

25. The owner (while the Phoenix was being started): "Also buzz Cut mochi in the awakening state has a ton of trouble catching people
    into the full move, a lot of times it doesnt count the full move." (Scorching Buzz Cut: whoever the wheel rolled over was often not
    caught when N was let go: the wheel kept rolling on past them, more than SCORCH_NEAR behind, and the hits knocked them away.)

27. The owner (with a sketch: a spiral drawn from a small square at its middle (the user) widening outward over about two and a half
    turns): "Buzz cut is great, but with the more it spins the wider it should get, this applies for every version. I just want the thing
    to be wider" So: Buzz Cut Mochi and Scorching Buzz Cut: the whirl spirals outward, the arm reaching further with every turn, the
    whole thing much wider at its end.
28. The owner: "also literally every sound of dough sounds like shit, get new ones from the web of slime, viscous stuff, dough etc" So:
    every Mochi dough sound replaced by CC0 web recordings of slime, viscous goo, dough (stretch, squish, splat, the fists landing, the
    swell, the slam and stamp of dough).
29. The owner: "Tente novamente" ("try again": the session's worker had restarted; carry on).

30. The owner: "the awakened version of Buzz Cut should create a much bigger crater and spin 2 more times" (Scorching Buzz Cut: seven
    turns of the whirl, Buzz Cut's five; its crater far bigger than Buzz Cut's).

## O. The fourth Mochi chat: Buzz Cut judged and finished, the old Mochi art removed, 3.6.3 released

Build #233 (the last push of the chat before) was green. Done, rendered and judged against the Buzzcut GIF, the owner's show clip
(Officialref.zip/s9cBv2.gif) and 8zzv 10 with the horizontal whirl (previews: MochiShots |mbuzz:age; the cutscene's own shots from
MochiDraw.buzzCamera, rendered with FxPreview's new `--fov=70`, the game's field of view; the scratchpad's CamShots turned
buzzCamera into e: shots):
- The torn floor: the reference's pale khaki square slabs (MochiDraw.SLAB, sampled from frames 55 to 70), a close double row
  (BUZZ_TILE_GAP 0.6, up to BUZZ_PATH_MOST 112 points: the whole longest roll), thrown up as the wheel passes and dropped back tipped
  at their own angles, lying BUZZ_TILES 100 ticks then sinking into the ground (no fade). Before: small brown planks 1.1 apart.
- The wheel: white droplets of dough flung off its rim and long white wind slashes (frames 55 to 70), with the glow, black wind lines
  and smoke it had.
- The whirl's vortex: a whole disc of 40 soft white swirl streaks (MochiDraw.spiral: flat, spiralling in, bright just behind the head)
  whirling with the arm, densest in a ring round the middle, a bright core and glow round the feet, the thick trail of the ball's
  path (frames 125 to 140). Before: five thin rings.
- The slam: a fan of 32 crimson (and a few white) streaks sprayed up from round the giant ball (frames 165 to 182), red specks
  spattered over the black for 1.5 s; the crater pure black (MochiArt.INK; it was HAKI_CRIMSON: maroon); the rubble field 40 bigger
  grey blocks (RUBBLE greys) out to 9.5.
- The arm contracting back: the ball shrinks away into the arm (no little black ball left on the white arm).
- The cutscene camera (MochiDraw.buzzCamera, pure: client/mochi/MochiFx.camera and filmAngles call it; no allocation a frame): low at
  the user's LEFT and ahead for the catch and swing (it was behind the right shoulder: the ball swung into the lens), high over the
  whirl (17 up: the whole vortex in frame), low in front looking up the arm to the raised ball, then 13 off to the side for the slam
  (it was 4 ahead: inside the rubble field) and pulled back over the crater.
- The impact frames: **the owner's sprite frame** (N 12: "use my imapct frame"; ClashFx.frame now shows ImpactSprite, never the
  classic ImpactFrame), on the blow and on the slam, for the user and the ones caught. Rendered over the slam shot: the owner's blue
  burst with its dark veil.
- The roll's rumble (gura/rumble, 8 s long) was played once by the server and outlasted the roll: now the client holds it through
  client/LoopSound only while the wheel rolls (faded out on the catch or the stop).
- HudTests.buzzCut: the beats in order, the whirl level, the ones caught off the ground on the ball, whole turns, raised over 15 up,
  slammed over 12 ahead with the one caught on the ground under the ball, the camera never inside the rubble, the torn row kept whole.
- Chestnut Mochi's spike hits now throw the Conqueror's lightning (POINT ONE: it was the one Mochi move without it).
- The tube (MochiArt.tube) keeps its last good direction where points coincide: Chestnut's comet and Lotus' ribbon drew NaN vertices
  on their very first frame (found by the new budget test).

Removed (the owner: "I want yours gone"): MochiDraw's old art (tsuki, yanagi, kaku, club, spiked, wheel, the ring anemone, donuts,
dome, walls, wave, rain, ground, groundLumps, glob, patch, the dodge's hole, their constants), the client's Club record and CLUB
message (MochiMoves.CLUB), the old clips (mochi_tsuki_charge/thrust, mochi_yanagi, mochi_kaku, mochi_club, mochi_anemone, mochi_throw,
mochi_wave, mochi_walls, mochi_rain, mochi_dome: tools/mochi_anims.py, their JSONs, MochiPoses, HandMoves), the double-tap-Shift
dodge (client/FruitKeyN: Future Sight was gone from the server already). Kept for 3.6.4: MochiDraw.trident, the mochi_awaken clip,
MochiMoves.AWAKEN. DragonTests.mochiArt now budgets every new move from press to last splat (the punch, Grilled, Muddle, Lotus,
Chestnut, the Anemone with a dozen bars, Buzz Cut's wheel over its whole torn row and every tick of its cinematic): all under 16k
(Buzz Cut's heaviest about 6k).

Not done (honest): the classic impact frame cannot go fully grey on the owner's phone without a shader (it is not used here any
more); the victims stay upright on the ball (other players' models are not turned); the whirl has no sound of its own (none asked).

Released: **3.6.3** (`.github/release-notes-3.6.3.md`), green on GitHub: Build #234, Release #154. Previews sent to the owner
(SendUserFile): Buzz Cut side by side with its reference frames, its slam, the owner's frame over the slam, the five other base moves.
The DoughV2 GIFs are read frame by frame in section P (for 3.6.4).

Next: **3.6.4, the awakened DoughV2 set on M** (F 7, as promised to the owner): M turns the Awakening on and swaps the kit to LMB
Mogura Lacerations (five trident swings, the last breaks a block), Z Grilled Lance, X Elastic Lasso, C Oblivion, V Power Mochi, B
Blazing Storm, N Scorching Buzz Cut. First read each DoughV2 GIF frame by frame (section D lists them; Trident Lacerations and Elastic
Lasso were never looked at), write what each shows here, then build each move on its beats the way the base kit was built (art in
MochiDraw, server in MochiMoves/MochiKit.AWAKENED, client in MochiFx, clips in tools/mochi_anims.py, hands in HandMoves, budget in
DragonTests, beats in HudTests), render side by side with its GIF, fix, render again.

## P. The DoughV2 GIFs read frame by frame (for 3.6.4; the awakened kit's target "to very minute detail")

Every V2 GIF is an animated WebP at 600x450 (Scorching Buzzcut 500x375), about 50 ms a frame: one frame is about one game tick. The
awakened colours: Haki MAGENTA-PINK (about FF66EE to C040FF) and black ink for the trident and the Haki; orange fire everywhere; the
dough cream-ivory in Oblivion, white elsewhere.
- **Trident Lacerations** (LMB, 9hkng3.gif, 100 frames; camera behind the user, the target 2 ahead): five swings, 10.5 each. f15 a
  thrust: the trident's dark tip through the target in a jagged magenta burst; f18 magenta crescent arcs round the target, a white
  flash on it; f27 a horizontal spin: a big black ink brush-stroke spiral on the ground round the user with a magenta arc inside;
  f30 a magenta crescent slash level through the target, a white star spark; f39 a diagonal upward black ink crescent edged magenta;
  f42 black ink claw streaks flying off up-left, grey dust trailing (f45 to 51); f54 a jagged magenta burst with black specks round
  the target; f63 the black ink spiral on the ground again with a magenta ring; f66 a low magenta crescent sweep up (the 5th, the
  one that breaks a block): the target LAUNCHED straight up very high, white vertical speed lines, a white ring on the ground (f69 to
  78). The user holds the trident in the right hand (a long thin dark shaft, a pale three-pronged head).
- **Grilled Lance** (Z, 1205.gif, 120 frames; behind the user, the target 1 to 2 ahead): f14 to 22 the arm out, a white puff of mochi,
  a PURPLE Haki ring spinning round it; f24 to 26 black ink puffs bursting off it with a purple star flare; f28 to 34 the arm swollen
  into a big smooth round cream-white ball (larger than the user's torso), purple sparkles flying round it, a purple rim, fire
  licking its base (f34); f38 to 52 it is driven into the target: it turns into orange fire with star flares at the target (about a
  second, the target burning, sparks); f56 a half-dome of concentric fire rings (Grilled Dough's explosion); f60 the explosion: brown
  rock shards and fire; f64 to 76 a dark spike (the lance) standing in the target with white star flares, embers; f80 a SECOND, huge
  white-hot flash filling the screen with orange rays ("explodes after a few seconds"); f84 to 96 huge orange fire swirls and arcs
  sweeping past the camera, brown chunks flying (60 damage); f100 to 118 embers and fire streaks fading.
- **Elastic Lasso** (X, Elastic_Lasso.gif, 60 frames): f12 the user spins: white wind arcs and PURPLE rings round them; f14 to 18 huge
  white wind arcs and big purple swirl rings (the lasso swung round flat); f20 a black and purple burst at the user (the throw); f22 a
  jagged pink burst; f24 to 26 white shards flying and a thin WHITE elastic band shot from the user's hand far out to the target (the
  camera follows); f28 to 36 the band taut from the hand to the target (7.5), the target yanked back along it; f38 to 40 the target
  arrives at the user; f40 to 42 a pink-and-black burst with white shards (22.5); f44 to 58 white bits falling.
- **Oblivion** (C, Oblivion.gif, 80 frames; the target about 4 ahead): f15 the user stomps; f18 a CREAM surge of mochi erupts with a
  pink burst; f21 a big cream eruption, white swirl rings on the ground; f24 a pink-and-black burst; f27 to 42 the cream mochi stands
  as a wide jagged wall all round the target (a basin, about 2 high, 5 wide) holding it (18.8); f45 to 51 the walls sag and break into
  bits, pink and black spikes in it; f54 to 57 pink lines, red lightning streaks; f60 a big black-and-magenta star burst; f63 to 69 cream
  shards and pink streaks blown out everywhere (toward the camera), red lightning lines on the ground; f72 to 78 cream shards falling.
- **Power Dough** (V, Power_Dough.gif, 160 frames): f15 two cream mochi splashes either side of the user (red streaks); f20 two big
  orange fireballs; f25 two white lumpy DONUTS stand upright at the user's left and right (waist to chest high, facing forward), orange
  fire arcs round them; f30 to 155 they hover there with a soft orange-red glow behind, a black fist in each hole; each punch (left
  click): a jagged yellow-orange flash at the donut (f65, f90) and a LONG BLACK SQUARE BAR wrapped in orange and red fire shooting out
  of it to the target about 5 away (f70, f90, f105), fire arcs swirling (f80), 22.5 a hit; the last throws the target far (f110,
  32.5); about 20 s in all.
- **Blazing Storm** (B, Blazing_Storm.gif, 140 frames; the target about 8 ahead): f16 a white puff at the user; f20 to 24 a fiery burst
  with white mochi blobs round the user; f28 to 96 SIX long black square Haki arms thrust out of white mochi puffs at the user at the
  target over and over (a barrage, 1.2 a hit, many hits), wrapped in orange fire streaks, converging on it (aimed); f100 to 108 the
  last strike: all six fully out at once, each from its white puff, converging; f112 a big fire explosion at the target with white
  mochi blobs and black shards; f116 to 124 an orange haze fading.
- **Scorching Buzzcut** (N, Scorching_Buzzcut.gif, 218 frames; behind the user): f12 the user becomes an upright WHITE wheel with BLACK
  SPIKES round its rim, wreathed in flames, white droplets flung; f18 to 60 it rolls away, fire spraying off the ground behind it, the
  ground cracking, sparks, 2.2 a tick to whoever it rolls over; f72 the target whirled on a fire ring; f84 to 120 the whirl: a huge
  ring of motion on the ground (white swirl streaks, a PURPLE Haki glow ring, an ORANGE FIRE swirl ring, purple orbs, black rocks
  whirled round, red lightning) with the thin white arm round it, three wide swings; f126 a column of fire, the target flung up in
  it; f132 to 150 the arm straight up like a pole, the target high on it with a ring of fire at its end; f156 to 162 two pale grey
  impact frames; f168 the slam: a black crater, red lightning, rubble; f174 pink-purple rings orbiting a fiery ball at the impact;
  f180 to 186 a huge block of the ground flung up past the camera; f192 to 216 the dust settling.

## Q. In progress (the fourth Mochi chat, after 3.6.3): the owner's two notes first, then the awakened kit

- N 13 done (unversioned): MochiDraw.buzzBend bows the band toward where the arm is going (the ball trailing after it), sagging under
  the ball's weight (BUZZ_SAG), kept over the ground; HudTests checks it. Rendered: the band hangs under the straight way in the slam.
- N 14 done (unversioned): Lotus Mochi Combo's air phase rebuilt as the owner drew it: MochiDraw.LOTUS_STATIONS (the body's places
  after the launch and after each of four blows, zig-zagging side to side), lotusBody (the server's Juggled moves the body so; each blow
  carries it there in LOTUS_CARRY 3 ticks, then it hangs sinking a little), lotusDonut (each blow's donut behind the body on the side it
  is struck from), lotusChain (the donut forms 6 ticks before its blow, its fist strikes and stays glued to the body through the carry,
  its arm stretching, then snaps back; a white burst, rings, streaks of the flight); the client draws the chain from the launch
  (LotusRise now carries the way faced and the coat) and tumbles the body like a rag doll (MochiFx.lotusTumble: over backward as it is
  launched, then spun over the way each blow drives it; RenderLivingEvent Pre/Post, rotations pushed one by one: only
  Quaternionf.rotateAxis is on the allow-list). Blows at 50, 56, 62, 68; the giant fist over the last station from 69, the fall from 73,
  the ground at 82 (it was 106). HudTests checks the side-to-side carries; preview |mlotus:age (the body tumbling as pitch and roll).
- The Awakening (3.6.4 work, committed unversioned): M toggles it (MochiMoves.awaken; AWAKEN messages, resent every AWAKE_RESEND ticks
  so late comers know; MochiFx.AWAKE); its kit MochiKit.AWAKENED (all seven named, FruitEnergy costs); the left click is Trident
  Lacerations, built: five swings (clips mochi_trident_1..5 in tools/mochi_anims.py, lint clean; HandMoves), Mogura held on the
  player's real right arm while awakened (client/mochi/MochiClient.renderBody and firstPerson, KatanaRenderer.steelBox boxes, the
  shaft out of the fist's front: MochiV2.GRIP_*), each swing's trail from the very clip (MochiV2.mogura + StylePoser: magenta
  crescents, black ink for the spin and the rising slash), the spin's ink spiral on the ground, magenta jagged bursts and white star
  sparks where it strikes, the 5th breaks a raised shield and throws them up (white ring, speed streaks). Art in mochi/MochiV2.java;
  preview MochiShots |mtrident:k:age. Z to N while awakened do nothing yet (MochiMoves.pressAwake): Grilled Lance, Elastic Lasso,
  Oblivion, Power Mochi, Blazing Storm and Scorching Buzz Cut are still to build (section P has their GIFs read).

Released after N 15: **3.6.4** (`.github/release-notes-3.6.4.md`): Buzz Cut's band (N 13), Lotus' donut-to-donut chain (N 14), the
Awakening on M with Trident Lacerations. Next, **3.6.5**: the six other awakened moves on Z to N (MochiMoves.pressAwake says "comes in
the next version" for each until then), each built on its GIF's beats as section P reads them, rendered beside its GIF, judged; in this
order: V Power Mochi (it changes the left click), Z Grilled Lance, X Elastic Lasso, C Oblivion, B Blazing Storm, N Scorching Buzz Cut
(reuse Buzz Cut's machinery: MochiMoves Wheel/Whirl, MochiDraw.buzz*, MochiFx Buzz, with fire, spikes on the wheel, three wide swings
and a bigger slam). The owner may still send notes on the base kit: they come first (write them into N verbatim).

Open after 3.6.4 (the owner's newest notes came first: both done in 3.6.5): N 16 (the Mochi alive: stretchy, squishy, elastic arms)
and N 17 (WiFi Haki on the one 3D Haki lightning).

3.6.4 green: Build #238, Release #155.
- N 17 done (unversioned, for 3.6.5): WiFi Haki's arc was the original's flat Photon beams (HakiVfx.acceptWifiHaki0 -> wifiHakiArc): the
  patch now sends HakiVfx.acceptWifiHaki to client/haki/WifiBolts.accept, which draws haki/ConquerorSparks.wifi (one crooked stroke from
  where it leaves the blade to the target, crawling out over the message's travel ticks, two or three forks, sharp tips) through
  HakiBolts.sparks: the one 3D Haki lightning in the user's Haki colour. Rendered beside the release (the scratchpad's WifiShot on
  SparkPreview.render): the same black lit tubes with the rim.
- N 16 done (3.6.5): the Mochi alive: MochiDraw.arm (every mochi arm: punches, donut fists, Lotus, the Anemone's bars) thins in its
  middle the further it is drawn out, carries a wave of wobble running along it (side to side and up and down; a third of it with Haki)
  and sags under its weight, all eased to nothing at its two ends (DragonTests' square-section check reads the shoulder's ring); the
  punch shoots out past its reach and springs back (MochiDraw.elastic), its fist squashing out as it lands; every donut breathes (ring
  and tube out of step) and its tube swells as its arm pushes out; the dough's lumps churn twice as fast (MochiArt.CHURN); Buzz Cut's
  band carries the same wave and thins as it stretches. HudTests checks the overshoot.
- 3.6.5 released with N 16 and N 17 (the owner's precedent N 15: fixes ship at once). The six awakened moves come after: in the game
  their keys say "is coming in a later version".
- Next: **3.6.6**, the six awakened moves (V Power Mochi, Z Grilled Lance, X Elastic Lasso, C Oblivion, B Blazing Storm, N Scorching
  Buzz Cut), as the end of section Q says; the owner's notes first whenever they come.

## R. Where the fourth Mochi chat stopped (the owner: "enough prompt + hand off now")

Released this chat: 3.6.3 (Build #234, Release #154, green), 3.6.4 (Build #238, Release #155, green), **3.6.5** (Build #240, Release #156, green).

In progress, committed unversioned after 3.6.5 (the next version is **3.6.6**): **Power Mochi** (awakened V), built but not yet
judged to the end:
- Art (mochi/MochiV2, "V: Power Mochi"): powerAt (each donut 1.2 out at the shoulder, 1.5 up, 0.2 back, bobbing), powerDonut (a cream
  splash at 0 to 6, a fireball with fire arcs at 3 to 11, the lumpy white donut out of it from 7 with a black Haki fist in its hole,
  flames licking over its top, a red-orange glow 1.1 behind it; going back into a puff of fire), powerPunch (the fist out in POWER_OUT
  2 ticks on MochiDraw.elastic, a long black Haki bar (MochiDraw.arm, coated) wrapped in fire puffs streaming back and orange streaks,
  a jagged yellow-orange flash at the donut, a burst of fire and red sparks where it lands; POWER_LIFE 10).
- Server (MochiMoves: POWER 285, POWER_PUNCH 286): V awakened starts it (ready/cool MochiKit.POWER 600, POWER_TICKS 400 = 20 s); while
  it lasts the awakened left click is its punches (one donut then the other, every POWER_EVERY 7 ticks, 6 damage, on fire 2 s, pushed,
  the Conqueror's lightning: POINT ONE) instead of the trident; it ends after its time, when the Awakening goes off, or on forget; the
  state is sent again every AWAKE_RESEND ticks (by 1: shown formed).
- Client (MochiFx: POWERS, POWER_HITS, powerPlace: at the shoulders, or in one's own first person 1.35 out to each side, 1 ahead, low).
- Clips mochi_power (arms flung out as the donuts burst out), mochi_power_1 and _2 (the arm on the punching donut's side points), lint
  clean; HandMoves (UP_DOWN, POINT). Preview MochiShots |mpower:age[:punch[:side]].
- Rendered: the splash and the two fireballs read like the GIF's frames 15 to 20. The last fix (the glow moved 1.1 behind the donut so it
  no longer tints the donuts pink: the GIF's donuts are white with the glow round them) was rendered once more (scratchpad r/power2.png)
  but not yet looked at: render |mpower:30 and |mpower:60:2:1 again and judge them beside Power_Dough.gif frames 30 and 66 to 70
  (section P). Still to add: DragonTests.mochiArt budget for powerDonut x2 + powerPunch; HudTests (Power's keys, its 20 s, the punch's
  reach); then judge it in first person.

Then the five other awakened moves, in this order: Z Grilled Lance, X Elastic Lasso, C Oblivion, B Blazing Storm, N Scorching Buzz Cut
(section P reads each GIF; until built their keys say "is coming in a later version": MochiMoves.pressAwake). Release them as 3.6.6
when done (or sooner if the owner asks: they chose "Release fixes now" before, N 15). The owner's notes come first whenever they
arrive: write them verbatim into section N.

How this chat worked (tools in the scratchpad, gone with the session; rebuild them as needed): CamShots (MochiDraw.buzzCamera to FxPreview
e: shots), WifiShot (SparkPreview.render of a strike beside the release), pick.py/sheet.py (GIF frame sheets: `python3 -I`), FxPreview
`--fov=70` (the game's field of view) for any camera shot the game frames. Compile previews against fresh classes: MochiArt, MochiDraw,
MochiV2 and client/npc/MochiPoses + StylePoser (regenerated clips) into a scratch folder ahead of build/classes.

## S. The fifth Mochi chat (after 3.6.5): Power Mochi finished, the five other awakened moves

Build #243 (the handoff push before this chat) was checked first. The session was on Default: switched to HexPiece (network_only).

- **Power Mochi** (awakened V) judged beside Power_Dough.gif frames 30, 50 to 56, 66 to 72 (side-by-side renders, the GIF's camera
  behind-left of the user, the target ahead-right) and fixed (mochi/MochiV2):
  - The donuts as big as the user (POWER_DONUT 0.9), head high at each side (POWER_UP 1.75, POWER_SIDE 1.4, POWER_BACK 0).
  - The glow: a halo turned to the eye just behind each donut (MochiV2.halo: two FireMesh.rings, clear inside the outline, orange
    then red outside it). It was a shell 1.1 behind the donut: from the GIF's camera it lay on the ground as an orange stain.
  - The hole from behind: the dough the fist grows out of fills it (a flattened ball behind the fist): from behind it reads pale, as
    the GIF's; the black fist only from the front, further out of the hole. While its punch is out the hole is empty (powerDonut's
    new `out`, the client reads its PowerHit; MochiV2.powerOut).
  - The punch: a thick black bar as wide as the hole (MochiDraw.hole), seams of fire glowing along each face by its edge, broken and
    kinked, riding the bar's wobble exactly (new MochiDraw.armAt: the arm's centre and half width at t, which MochiDraw.arm now uses),
    flames licking off its first half; the fist caught in a puff of white dough as it lands; fire and red sparks there.
  - The launch burst (frames 52 to 56, a tick a frame): a fireball engulfing the donut (tick 0 to 2), yellow rays thrown forward,
    thick crescents of orange fire with yellow hearts bracketing the donut either side and opening out (ticks 1 to 6), crimson
    splinters sprayed all round (straight streaks: the GIF's jagged red lines are its lightning; ours is the one Haki lightning, which
    the punch already throws with Haki and Ryou on, POINT ONE). Right by the eye the burst is drawn smaller (`near`).
  - First person: one's own donuts 2 ahead, 1.7 out, 0.55 low (MochiFx.FP_*), half in view at the view's edges (at 1 ahead they
    filled both sides of the view). Preview `|mfirst:age:-:power[:punch[:side]]` with `fp:0`.
  - DragonTests: both donuts forming and every tick of a punch, near and far, under 16k (about 5.5k at most). HudTests.powerMochi:
    20 s, its reach, the donuts taking turns (each fist back before its donut punches again), one at each side head high, the punch
    springing past its reach and drawn back by its end.
- **Grilled Lance** (awakened Z, held) built on 1205.gif read frame by frame (a tick a frame from its frame 14), rendered beside it
  (MochiShots `|mlance:age`, the target 6 ahead, a tap: let go as it fills):
  - Held (MochiV2.lanceCharge, LANCE_*): a white puff of mochi at the hand, a magenta ring of Haki spinning round the arm (4 to 11),
    a burst of black ink blobs with big magenta star flares (11: MochiV2.flare, a four-pointed flare turned to the eye), the arm
    swollen into a great smooth cream ball (LANCE_BALL 0.75, full at LANCE_FULL 16, breathing), magenta sparkles round it, a magenta
    halo, fire at its base; held at most LANCE_MOST 60. Let go early: it goes the moment it is full.
  - Flight (lanceFly): the ball at LANCE_SPEED 1.6 a tick, LANCE_RANGE 18, a few tongues of fire off its back (no long jet: the
    first render's jet was a huge wedge), sparkles, a white ring as it leaves.
  - Where it strikes (lanceHit, ticks from the strike): the ball bursting into fire burning in them with big orange star flares
    (to LANCE_BOOM 22), the first blast (Grilled Mochi's boom at LANCE_BOOM_SIZE 3.6 played three times as fast: the GIF's rings
    are gone in about six frames, so the spikes after it show), dark brown rocks thrown (MochiV2.chunks), a magenta ring over the
    ground; black Haki spikes standing in them (cones up to 4.2 tall) with white slashes and orange star flares; the second blast at
    LANCE_BOOM2 46 (a white-hot flash filling the view, 22 long rays, nine great swirls of fire opening out, bigger rocks, embers).
  - Server (MochiMoves LANCE 287: LANCE_HOLD, LANCE_FLY, LANCE_HIT, LANCE_DROP; class Lance): the first body the ball meets is hit
    (4), held where it is without the glue drawn (stick(..., false)) and set on fire; the blasts through UoMoves.explode (8 then 14,
    a quarter more with Armament; each throws the Conqueror's lightning: POINT ONE) with quakes; no left clicks while it is held.
    Cooldown 220 ticks.
  - Clips mochi_lance_charge (the arm thrust out, a tremble) and mochi_lance_fire (drawn back, driven out); HandMoves DEEP and JAB.
  - DragonTests: every tick of the charge, flight and strike, near and far, under 16k. HudTests: its beats in order, the second
    blast bigger and harder, the ball as big as the GIF's.
- **Elastic Lasso** (awakened X) on Elastic_Lasso.gif (a tick a frame from its frame 12; `|mlasso:age[:miss]`): the lasso band whirled
  round the user at the chest inside white wind arcs and magenta rings, black bits on the ground (MochiV2.lassoWhirl, to LASSO_THROW
  8); the throw's pink-and-black burst (MochiV2.pinkBurst: the awakened Haki's pink fire cut in PINK_BANDS, black splinters, white
  shards, a white ring; played twice as fast for the throw); the band (lassoBand: thick white bar, a wave running down it, a zig-zag
  kink by its end) shoots out past its reach in LASSO_SHOOT 3 (lassoOut: elastic), a loop round the one caught, a small burst there;
  held LASSO_HOLD 2, they are yanked back along it in LASSO_PULL 7 (server Yank: velocity toward 1.6 in front of the user, lifted a
  little mid-pull) and land in a big pink burst (hit 7, held LASSO_STUN 16 ticks: "allowing itself as a combo extender"); the band
  back into the hand in LASSO_BACK 3; a miss springs back. Server MochiMoves LASSO 288 (LASSO_WHIRL, LASSO_THROWN with the caught
  body's id, LASSO_ARRIVED); catch 2.5 (hit: the Conqueror's lightning). Clip mochi_lasso (the arm whirling overhead out to the side:
  the lint caught the first try through the head; thrown; hauled), HandMoves THROW.
- **Oblivion** (awakened C) on Oblivion.gif (`|mobliv:age`, the one held 5 ahead): a stomp (rings of white and pink at the feet, the
  clip's body and arms: legs are never keyed), a big cream eruption at the user's front (twice their height) with pink rays and cream
  bits, the surge of cream lumps running along the ground to them (OBL_SURGE 5 to OBL_GRAB 10), a pink-and-black burst there and white
  swirls on the ground, eight broad cream slabs rising round them one after another into a low basin (OBL_WALL 2.8, OBL_TALL 1.7,
  CREAM bands: the GIF's ivory; first drawn as a tight tall crown of ten, widened after the side-by-side with frame 40), breathing,
  foam at their feet; from OBL_BREAK 31 the walls sag, tip out and break into splats, bits lying on
  the ground, black Haki spikes and pink streaks standing in them, pink rings; OBL_BURST 43: a black-and-magenta star (solid black
  cones flung out, magenta rays, a pink heart) and 36 cream shards blown out everywhere. Server OBLIVION 289 (a, the feet to theirs):
  OBL_RANGE 8, the grab hits 6 and holds whoever is within the walls (not drawn as glue), the burst UoMoves.explode 12 (POINT ONE).
  Clip mochi_oblivion, HandMoves DOWN.
- **Blazing Storm** (awakened B) on Blazing_Storm.gif (`|mstorm:age`, the point 8 ahead; rendered beside frames 44 and 104): six
  mochi tendrils sprouting out of the user's back into six white puffs (MochiV2.stormRoot: three a side at the shoulders), a burst of
  fire and dough; from STORM_FORM 8 to STORM_BARRAGE 80 each arm jabs out of its puff onto the point (stormOut: a STORM_JAB 5 cycle,
  out of step) as a black Haki bar wrapped in long glowing bands of orange and yellow and a dark smear of speed, fire crescents curling
  at the user's front; STORM_LAST 84 all six fully out, thicker, held; STORM_BOOM 94 an explosion where they meet (a white flash, fire,
  arcs of fire, white blobs and black shards). Server STORM 290: a volley every 4 ticks re-aimed at what the user looks at (STORM_AIM:
  "can be aimed"; the client eases between aims), 1 a hit on fire, the lightning; the last strike 6; the explosion UoMoves.explode 10.
  Clip mochi_storm (both arms driving, a shake each volley), HandMoves PUSH.
- All three: DragonTests budgets every tick (Elastic Lasso, Oblivion near and far, Blazing Storm near and far: all under 16k, Blazing
  Storm the heaviest at about 9.5k close); HudTests checks their beats.
- **Scorching Buzz Cut** (awakened N, held) on Scorching_Buzzcut.gif (`|mscorch:age`: |mbuzz with the fire): Buzz Cut Mochi's own
  machinery with a scorch flag (server Wheel.scorch/scorchTick and Whirl.scorch; client Buzz.scorch, BuzzMark.scorch; the BUZZ_ROLLING
  and BUZZ_GROUND messages carry by 1) and MochiV2's pieces over it: scorchWheel (twelve black Haki spikes round the rim turning with
  it, a fire halo, flames round the rim, fire and sparks spraying off the ground behind, a burning trail), scorchBall (the giant ball on
  fire the whole cinematic), scorchWhirl (five bands of fire trailing the arm with yellow hearts and dark bands between, a white swirl
  in the middle, a thick magenta ring round the edge, purple orbs and black rocks whirled round; Buzz Cut's white swirl drawn at 0.4
  under it), scorchColumn (a column of fire up out of the middle late in the whirl to just after the raise: scorchColumnAlpha),
  scorchCrater (a fiery dark ball in four magenta and pink rings orbiting outside the giant ball, fire round the crater, a great block
  of the ground thrown up past the eye and falling). Server: N held rolls the wheel (no catch on contact): every 3 ticks whoever it
  rolls over is hurt 1 and set on fire (the lightning); let go (MochiMoves.pressUp sets scorchHeld false) or at BUZZ_ROLL: whoever it
  rolled over still within 4 and anyone within 2.8 are caught, else it unrolls; the blow 10 (on fire), the slam 26, a crater 5.5 and a
  burning UoMoves.explode round it (the "larger AOE"). Book: "[N] hold". The whirl keeps Buzz Cut's five turns (the owner chose the
  horizontal whirl for Buzz Cut; the reference's "3 times" is not split out yet: ask if the owner wants exactly three).
  DragonTests: every piece at once, every tick of the cinematic, under 16k (about 9k).

**Released: 3.6.6** (`.github/release-notes-3.6.6.md`): the Awakening's whole set (V Power Mochi, Z Grilled Lance, X Elastic Lasso,
C Oblivion, B Blazing Storm, N Scorching Buzz Cut). Previews sent to the owner (SendUserFile): each move's GIF frames over our renders
(scratchpad final/1-6, gone with the session; rebuild with tools like the scratchpad's compose.py: GIF frames on top, the preview's
renders under them).

Also after the side-by-sides: Grilled Lance's ball cream (the GIF's ivory) and ten magenta flares in its ink burst; Scorching Buzz
Cut's slam rings moved outside the giant ball (3.3 out) so they show round it as in frame 186.

Honest gaps (judge them first if the owner says a move is off): the GIF's red jagged streaks in Power Mochi, Oblivion and Scorching Buzz
Cut are left to the one Haki lightning (POINT ONE: never another lightning look); the moves' sounds reuse the Mochi set (INFLATE, FIRE,
BOOM, SLAM, STRETCH, SQUISH, PUNCH, STOMP); Blazing Storm's GIF barrage is denser and brighter than ours; Grilled Lance's GIF camera is
much closer, so its blasts fill the view there; none of it is tested in game.

## T. Where the fifth Mochi chat stopped

3.6.6 pushed with its release notes (check its Build and Release runs first). Next: the owner's notes on the awakened moves (write them
verbatim into section N, fix them first); otherwise compare each awakened move again against its GIF at more beats (section P) and
close the honest gaps above.

3.6.6 green on GitHub: Build #246, Release #157.

## U. The owner's next request (verbatim, sent while 3.6.6's runs were finishing): the Phoenix fruit

20. The owner (with Phoenix.zip, their own model): "Next up:

    Mythical Fruit:
    Tori Tori no Mi, Model: Phoenix
    Zoan Type.

    I made the model and sent it in the zip, first of all animate it so it looks good, good flying animations, etc.

    Z (transforms/detransforms)

    Other moves:
    https://official-fruit-battlegrounds.fandom.com/wiki/Phoenix#Move_Info"
21. The owner's answers (AskUserQuestion): the meter: "No meter, cooldown (Recommended)" (Z turns the Phoenix on and off any time, a
    short cooldown after turning back, as the dragon fruit does); the size: "Like the GIF (Recommended)" (wingspan about 7 blocks, the
    body about as tall as the player).

23. The owner (while the Phoenix model was being baked): "Like I said before doing any move on phoenix I wanna see the model with your
    lively animations, its a burning phoenix, so get references yourself first of Marco the phoenix," So: no move yet; first the
    owner's model animated and alive (burning: its blue and gold flames moving all the time), after references of Marco's phoenix
    gathered and read, shown to the owner in previews.

24. The owner: "the animations have to be top tier" (the Phoenix's: judge every render as hard as the house rule says).

26. The owner (interrupting the first motion work): "again, cant stress this enough, the body is made of flames, my model did not burn,
    which is what I want. moving flames" So: the phoenix's body IS fire: every flame part (wings, mantle lobes, mane, tail fans and
    streamers) licking and flowing all the time, strongest at the tips, and tongues of cyan and gold fire streaming off the body
    continuously; the owner's file has no burning: we add it. Judge every render for it first.

What the zip holds (read with Blender's own Python module: `python3 -m venv <scratch>/venv/bpy && <venv>/bin/pip install bpy` (5.3,
Python 3.13); it opens the owner's .blend as it is): Marco_Phoenix.blend (Blender 4.3 file, zstd): 213 mesh parts (bill, eyes, feet,
claws, 18 tail fans and their shafts, 98 tail flame parts, mane, mantle, wings), about 88k vertices and 175k triangles, each part
weighted to one bone of a 21-bone rig ("RIG | Marco the Phoenix": root, body, neck, neck_reach, head, tail_root, tail.1-3, wing_inner,
wing_elbow, wing_outer, wing_hand, wing_tip and leg each side), one action "Marco | soaring wingbeat" (frames 1 to 73 at 30 fps) and 36
shape-key actions (flame flicker), flat materials glowing in their own colour (cyan plumage, gold and marigold mane and tail flames,
ivory bill and feet, blue markings), three fire textures (Body_revival_fire.png, Flying_tongues.png, Tidal_fire.png), studio lights
and five cameras; plus three reference images (Marco's phoenix). The wiki's Phoenix page (its Move Info read: Fujiazami, Talon Slam /
Hawk Rush, Phoenix Inferno, Bluebird, Transform; the Phoenix Meter) and its ten GIFs are in the scratchpad (pgifs/).

The plan (POINT ONE style: the owner's model exactly, uniformly scaled, never reshaped): bake the .blend with bpy into the dragon's
model format (sea/DragonModel's .sry: parts, rigid skin to the 21 bones, the clips; a small palette texture with each material's colour,
the glowing parts as glow) decimated for the phone, the flame flicker done in the renderer; reuse the dragon fruit's form machinery
(DragonForm, DragonRenderer, flight, camera) for a Phoenix form; write its clips (hover, takeoff, soar and wingbeat, glide, dive, the
wings wrapped round between beats as in the GIF, transform in a cyan sphere of light, back); then the moves: X Fujiazami, C Talon Slam
(transformed Hawk Rush), V Phoenix Inferno, B Bluebird, both forms each, on the GIFs read frame by frame.

## V. 3.6.7: the owner's three Mochi notes (N 22), done before the Phoenix

- Mogura (MochiV2.BACK 20, CROSS 21, TIP 32: gripped at the middle of its 41 px shaft; HudTests checks as much behind the hand as ahead
  to the crossbar). Shown only while the swings go on: client/mochi/MochiFx.MOGURA (by user: the first swing's tick, the last's, which)
  and MochiV2.moguraForm (springy out over MOGURA_FORM 2.5 ticks from the first swing, gone over MOGURA_GONE 5 after MOGURA_LINGER 10
  past the last swing's clip); MochiClient scales it out of the grip by it (third and first person); MochiFx.drawMogura draws the white
  wrap of mochi it forms out of and goes back into (MochiV2.moguraMochi, along the shaft as the swing's clip holds it; not in one's own
  first person). Preview |mtrident:0:age shows the forming.
- No impact frame anywhere in the Mochi fruit (the owner: "IMPACT FRAMES ARE ONLY FOR CLASHES"; N 12's frames on Buzz Cut removed).
- Buzz Cut Mochi (and Scorching): the cutscene is gone (MochiDraw.buzzCamera, MochiFx's filmed/cinema/filmAngles and its test removed):
  no forced third person, no forced view angles, no look lock; the user's body still turns with the arm (its yBodyRot set for
  drawing, every client) and stands where it caught (input held: MochiFx.swinging); in third person (back) one's own camera rises and
  pulls back with how high the giant ball is over the feet (MochiFx.camera: up 0.6 and back 0.45 a block of height past 2.5); the ones
  caught get nothing.
- 3.6.7 released (`.github/release-notes-3.6.7.md`). The owner's item 4 came empty: ask for it.
- The Phoenix work so far: tools/phoenix_bake.py written (bpy; not yet run to the end nor wired); see U for the plan.

## W. 3.6.8: the owner's Mochi notes 25, 27, 28, 30

- Scorching Buzz Cut's catch (N 25): MochiMoves.Wheel.scorchTick: whoever comes within SCORCH_MEET 2.7 of the wheel (now and half a
  step back) is rolled over and carried pinned under its front (set where the wheel's front is each tick), hurt 1 and burning every 3
  ticks; let go (or at BUZZ_ROLL), every one carried plus anyone within SCORCH_NEAR 4 is caught: the full move always follows.
- The spiral (N 27, both versions): MochiDraw.buzzReach in the whirl grows with how far round it has turned (to BUZZ_REACH_MOST 14, was
  6.5), buzzLift keeps the ball just over the ground at any reach. Rendered from above: widening every turn.
- Scorching (N 30): SCORCH_TURNS 7 (MochiDraw.buzzTurn/buzzArm/buzzBend/buzzStuck take the turns; Buzz Cut 5); its crater drawn
  SCORCH_CRATER_SCALE 1.9 times as big (MochiDraw.Scaled: a sink scaling what it gets about a point, no extra quads; the scorch fire
  too), Craters.open 7.5 and the burning blast 8.5 round it. The client knows Scorching from BUZZ_ROLLING's by or BUZZ_CAUGHT's size.
- The dough sounds (N 28): tools/mochi_sounds.py rewritten: 12 clips from 5 CC0 slime/goo/clay recordings (greenlinker's slime
  stretching and slapping, rubberduck9999's slime squishing, Funky_Audio's clay slaps, Eneasz's squishes), every one +4.3 dB over noise
  at most as cut (tools/tonal_lines.py), the room's hum under 95 Hz filtered (web_sounds.HUM: a 4-pole high-pass; the bass lift brought
  hums back: not used); sounds.json and MochiSounds.EVENTS point at them (stretch1-2, slap1-3, squish1-2, splat1-2, swell1, slam1, stamp1).
- Budget: the wider spiral made Scorching's every-piece-at-once frame 16.4k: its fire ring four bands, the burning ball five tongues
  (about 15.3k now).
- build.py NEW_PREFIXES allows assets/hexhaki/phoenix/ (the Phoenix model, unused until the fruit is registered).
