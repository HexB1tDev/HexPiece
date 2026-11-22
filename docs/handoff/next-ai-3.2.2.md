# HexPiece: handoff for 3.2.2 (in progress; every rule in one place, where things stand, what is next)

This is the newest handoff. Read it whole before touching anything. It gathers every standing rule the owner has given (CLAUDE.md's
house rules plus everything learned since 2.1), so you do not have to dig them out of the older notes. The full history (verbatim
messages, what each version did) is in next-ai-2.7.0.md (2.7.0 to 2.9.5) and the older next-ai-*.md files; open them only when you
need the detail of a feature. 2.9.6 to 3.1.0 (the Light fruit after Egghead, and 3.0.8 the katana stance) are written up in full in
sections 8b to 8p below; section 8q is the Gura Gura no Mi's research and plan, section 8r what 3.1.1 built of it, 8s 3.1.2's fixes, 8t
the Magu Magu no Mi (3.1.3), 8u 3.1.3's Gura changes, 8v 3.1.4 (first-person hands, the Haki orb, the charge sound), 8w the owner's
requests for 3.1.5 and 3.1.6 (verbatim, both done), 8x what 3.1.5 did, 8y the Gura ultimate (3.1.6) and 8z its key moved to N (3.1.7),
8za the owner's open requests, 8zb 3.1.8, 8zc to 8ze the Hie Hie no Mi (3.1.9: its plan, the owner's answers on real ice, as built),
8zf 3.2.0 (the Devil Fruits tab, the rarity ranks, the fruit energy bar).

Branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest). **3.1.9 (Build #151, Release #109), 3.2.0 (Build #152, Release
#110) and 3.2.1 (Build #153, Release #111) are pushed and green. 3.2.2 is IN PROGRESS and committed WITHOUT a full build (the owner
asked for this handoff mid-work): build.py still says 3.2.1. Section 8zh holds the owner's eight requests after 3.2.1 (verbatim, with
the screenshots described); section 8zi says exactly what is done and what is left, file by file. Start there.** The cloud environment
is the personal "HexPiece", now env_01PmnEXrwDhxzij8M5q6P6bL (freesound.org, tenor.com and the rest); a session on Default is refused
freesound.org: switch_environment to it.

## POINT ONE (the owner, 3.1.4: always keep this first in every handoff and pass it on to every AI after you)

1. **Every fruit is made in the same style as the Light, Gura and Magma fruits.** The owner (verbatim): "It's important to be always
   written in the handoff is point one, that the models and everything that it creates should be from in exactly like the other fruits,
   like basically the same style of animation that you've been doing for the light, quake, and magma. Those models are absolutely
   beautiful, and I love how you made them. So just keep that in mind, and always tell the other AIs like that they are to follow this
   same exact idea." So: 3D meshes built the way FireMesh/GuraArt/MaguArt/the Light fruit's art build them (banded alpha-blended shells,
   puffs, arcs, rays, streaks, lumps, shards), PlayerAnimator clips written by tools/*_anims.py with no leg and no head keyed, a short
   first-person hand move for every move (style/HandMoves), CC0 web sounds, every effect rendered and judged.
2. **With the Conqueror's Haki and Ryou on, every move of every fruit throws the Conqueror's lightning (visual only, no stun), and a
   move that ends in an explosion throws it out of that explosion.** The owner (verbatim): "If the move has an explosion at the end,
   well, then the explosion is the thing that gets the lightning. So basically, right click of the light fruit, that the end at the end
   around the explosion is where the lightning is going to come from. If you get what I mean, it's not the beam itself. ... And the
   explosions and all this hockey [Haki] thing on the explosions is not optional. It is like if the move has an explosion at the end, the
   explosion is what gets the lightning like blowing from outwards of it." Use `ConquerorAura.mixed` (gated on the Conqueror's Haki
   awakened and Ryou on) at each explosion, outward from it; a move with no explosion throws it at its strike point.

---------------------------------------------------------------------------------------------------------------------------------------

## 0. Talking to the owner

- Answer in English, plainly. Say what changed in words a player understands (what they will see and hear), not class names.
- The owner speaks through speech-to-text. Read through it: "hockey" / "Haiku" = **Haki**, "Rio" = **Ryou**, "wonomi" / "uo uo" = the
  **Uo Uo no Mi** (the dragon fruit), "live fruit" = the **Light fruit** (Pika Pika no Mi), "M1s" = left-click attacks, "the end key" =
  **the N key** (3.1.6 put the Gura ultimate on End: wrong; 3.1.7 moved it to N). The fruit move keys are Z X C V B N (M still unused).
- They are blunt and swear when something is wrong. Do not argue and do not apologise at length: find the real cause, fix it, say what
  it was in one line.
- **Do exactly what they asked, nothing unasked.** They are angry when something they did not ask for appears ("you keep it in the
  game without my permission") or something they removed comes back. If you think something else should change, ask first.
- **Never claim anything was tested in game.** Only the owner can. Say what you checked (build, tests, renders, measurements) and
  what you could not (e.g. "not rendered in a preview").
- When they report a problem, quote their words verbatim in the handoff, then what you found and what you did.
- When a request is ambiguous, ask once with AskUserQuestion; otherwise act.

## 1. House rules (CLAUDE.md: binding, read it too)

1. **Versions:** the last number runs 0 to 9; after x.y.9 comes x.(y+1).0 (`python3 tools/next_version.py --write` sets build.py's
   VERSION). One version per push the owner should test. A handoff-only push needs no new version.
2. **Releases:** every push to a `claude/**` branch publishes the jar as the pre-release `build-<branch>`. Adding
   `.github/release-notes-<version>.md` publishes `v<version>` as the latest release (write it for the owner: plain words, what they
   will see and hear, what was not tested). Only the 5 newest releases and workflow runs are kept.
3. **A push is done only when GitHub says so:** after every push check that the branch's "Build HexPiece" run (and "Release HexPiece"
   when release notes changed) end green (`mcp__github__actions_list`, method list_workflow_runs, branch filter). A local
   `python3 build.py` EXIT 0 is not enough.
4. **Stop rule:** the moment WEEKLY usage reaches 98%: write the handoff (docs/handoff/next-ai-*.md) and docs/handoff/next-prompt.md
   FIRST, commit everything, push, and stop. No CI wait. Keep the handoff current while you work so this is one commit.
5. **The Imu fruit (Devil Devil fruit) is gone for good.** Its assets (textures/item/black_soul.png, models/item/black_soul.json, the
   "Black Soul" name) stay in the jar unregistered; build.py `check_imu` fails if any code, text or file name names it.
6. **Never alter the owner's files in place** (input/hexhaki-2.1.0.jar, their textures, sounds, models). Changes go through the patch
   (patch/PiecePatch.java) or the expansion (src/main/java/com/hexhaki/piece/). Original animations may be replaced by reworked ones
   under src/main/resources/assets/hexhaki/player_animation/.
7. **Server safety:** client-only code lives in a `client` package; PieceVerify fails if a class elsewhere names a client class
   outside a lambda, or a client event subscriber is not registered for `Dist.CLIENT`.
8. **Judge your own renders:** "the animations always have to be top tier, no excuses whatsoever". Every animation, pose, menu,
   texture or effect you change: render it (tools/anim_preview.py, tools/ui_preview.py, tools/lightning_preview.py, the
   tools/preview/*.java previews), look at it yourself, fix and render again until it is right.
9. Git: commit on `claude/nice-bell-gam7qf` only, push with `git push -u origin claude/nice-bell-gam7qf`. No pull requests unless asked.
   Commit messages end with the two trailer lines the session gives you. No model names in commits, code or docs. Keep the owner's
   email private.

## 2. Clean moves and animations (the owner's quality bar)

- **Top tier or nothing.** Every move must read clearly in third person AND first person. Render every move in first person too
  (tools/preview/FirstPersonPreview, client/FirstPersonLight, client/FirstPersonLegs): arms and kicking legs attached to the body,
  never floating in front of the camera, never covering the face (Mutton Shot and Sky Walk were fixed for that).
- **PlayerAnimator freezes every part a clip keys.** Never key the head, and never key an arm or leg the move does not use (the sword
  learned it in 2.3.2 and 2.5.4; the Light fruit forgot it in 2.7.8 and the owner saw "M1s locking your head into place"). The
  animation tools write only the parts used (tools/pika_anims.py write_parts, tools/style_anims.py, tools/uo_anims.py).
- **Stances only where they belong:** a flight stance only in real flight, a charge pose only while charging; a move's own clip must
  never play under a stance (2.8.8: the Light fruit's moves were invisible because its stance overrode them).
- **Swords: one animation set for every blade** (the owner: "skip the checks on every single katana ... Just apply the same fix to
  every single one ... They're all the same"). Range differs by blade (Yoru the biggest), never the animation. build.py's
  `check_sword_poses` must pass. Blades are held by their grip.
- **The dragon model is the owner's EXACTLY,** uniformly scaled (6x the player and more), never reshaped; the mantle never inside the
  body in flight (DragonTests). The dragon camera stays as it is (the owner's choice). The dragon's movement is 2.8.9's with
  FLY_SPEED 0.04: **never bring the body trail back** (2.9.0's DragonForm.Trail lagged on turns and was reverted at the owner's word).
- **Keep what the owner praised:** Ryou's fist aura ("beautiful and perfect": do not touch it), the dragon's look and colours, the
  Blast Breath bomb.
- Moves are aggressive and violent but readable: big impacts, hitstop and shake through the original's Impact (`com.hexhaki.fx.Impact`),
  bodies thrown and knocked down through HakiFalls; damage balanced (2.9.0 halved the Light fruit's: never one-shot).
- No M1s or other moves while a charge is held (the dragon's Blast Breath rule; apply the same to any new charged move).

## 3. Lag: keep it to a minimum (the owner: "this mod is meant to be more optimized. It's super laggy"; "All of this while avoiding as much lag as possible")

The owner plays on an Android phone (section 4). Every effect has to be cheap there.
- **Per-frame budget:** a move's effect stays under about 16,000 quads on screen (DragonTests checks every dragon move; StyleTests the
  blows). Cap counts: HakiBolts.MOST 5000 live bolts, MOST_LANDINGS 12; PieceFx.count(n) scales particle counts by the player's
  particle setting; FireMesh.detail drops detail with distance.
- **No per-frame CPU work that grows with the scene:** no allocation per frame in render paths, no re-meshing every frame when a cached
  mesh will do, no per-vertex skinning on the CPU that is not needed (the dragon's per-frame skinning is the known remaining cost: see
  section 9). Use client/render/Bulk (one VertexConsumer.m_5954_ per vertex, the pose read once a mesh).
- **Never draw the same lightning twice** (2.4.5 cut the supreme release's second pass: 80,000 quads a frame). Never a second render
  pass or a framebuffer copy for an effect.
- **Server ticks:** every per-tick handler returns at once when it has nothing to do (check the cheap condition first, then the flag
  reads). Throttle bursts per player (ConquerorAura: one burst per 8 ticks).
- **Network:** send one small message (S2CUoFx: kind, ids, a point, a direction, a size, a seed) and let every client build the effect
  itself from the seed; never stream geometry or per-tick positions.
- **Blocks:** world damage within per-tick budgets and always growing back (Scars, Craters, Fissures repair themselves).
- When the owner reports lag, ask where (which move, which screen, how many players or mobs, Haki on or off) before guessing, and
  revert a change that made it worse rather than patching around it (2.9.2).

## 4. Rendering on the owner's device

Android, the Amethyst launcher, **MobileGlues** (OpenGL ES 3.2 through a GL translation layer, an Adreno GPU), Graphics: Fast, 3 GB heap.
Mods beside HexPiece: geckolib, fdlib, architectury, playeranimator, ldlib, photon, pehkui, bendylib, libraryferret, citadel.
- Everything alpha-blended and depth-tested, correct **without** back-face culling, through the game's own RenderTypes (debugQuads at
  RenderLevelStageEvent AFTER_PARTICLES for the effects). **Never additive blending** (it drew as solid boxes on the owner's phone).
- **Never a framebuffer blit, depth copy or post chain in the middle of the level** (2.5.3: unreliable on MobileGlues).
- Translucent layers that write depth punch holes in each other: keep an opaque core in its own batch, draw the translucent layers far
  to near.
- A shared BufferBuilder must not switch vertex format mid-batch (2.9.0: the Light clones' skins threw and wiped every Light effect;
  skins are drawn in their own pass first).

## 5. Sounds (the owner: "Don't you dare create one ... Go search around the web. Get proper sounds in.")

- **Every sound is a real recording from the web, free to use (CC0)**, or the owner's own clip, or the original mod's. **Never
  synthesise, layer or hand-mix a sound.** tools/web_sounds.py fetches from Freesound.org, refuses anything not CC0, cuts, makes mono,
  normalises, fades, makes seamless loops, writes bit-exact Ogg Vorbis and a SOURCES.txt crediting every author. Each set has its own
  script: tools/dragon_sounds.py, tools/pika_sounds.py, tools/conqueror_sounds.py, tools/sword_sounds.py. Set SOUND_CACHE to keep
  downloads. Freesound is reachable from the cloud session.
- **Loudness lives in the files.** Minecraft clamps a sound's gain at 1: a volume over 1 only adds range (range = max(volume, 1) x the
  sounds.json attenuation_distance). To make something quieter, lower the file (web_sounds `gain_db`) or the sounds.json "volume"; to
  make it louder past full scale, compress it (web_sounds `punch`). Measure with ffmpeg ebur128 (momentary max and integrated LUFS)
  against its neighbours and the sounds the owner already likes, never by ear guesses. "Bass boosted" means the punchy 80-200 Hz, never
  sub under 60 Hz.
- **A sound never outlasts its move.** A charge or held move plays through client/LoopSound (follows the entity, fades in, loops while
  held, fades out over 8 ticks when it ends, never cut off, never left ringing): see client/haki/ConquerorChargeSound,
  client/dragon/UoFx (Blast Breath), client/pika/PikaFx.
- **The original replays and delays its sounds:** HakiServerController plays every Haki sound 20 ticks late (delayedSound), and replays
  its Conqueror charge at 38, 58, 78 and 100 %. A long new file on one of its events stacks up (2.9.5's bug). When you give an original
  event a new file, check every place the original plays it and how often.
- Many sounds at once are loud: when an effect plays sounds per strike or per body, cap them to one or two and keep them quiet
  (2.9.5: four 3.4 s zaps at once on a release were "ridiculously loud").
- `"sounds": []` in sounds.json silences one of the original's events (build.py's RENAMED[SOUNDS] lists the events allowed to change).
- Removed at the owner's word, never bring back: Ryou's on/off sound (acoc_on, acoc_off), the full release's snap, rumble and crackle
  (2.4.7), the Conqueror's 2.6.0 hand-layered explosion. The evil path's voice line ("SAWAGASHII") and roar are the owner's own clip:
  keep them.

## 6. Haki and lightning

- **Every Haki lightning is the 3D lightning of the Advanced Conqueror's Haki** (client/haki/HakiBolts, BoltMesh, BoltPainter): black
  body, rim in **the user's Ryou colour** (HakiBolts.colorOf). Never the original's flat beams, never its red-black, anywhere, Gear 5
  included (2.9.4 routed GearVfx.playAt, ThunderstormImpact and StormLanceVfx through it).
- The Conqueror's sparks off the body and off a drawn blade (server/ConquerorAura, haki/ConquerorSparks) show **only while Ryou is on**
  (armamentOn and acocOn) and the Conqueror's Haki has awakened; never with Haki off.
- The release: charge faster with mastery (ConquerorAura.rate: 8 s at none, 2.5 s at 1000); a full charge is always the supreme one;
  fissures 1 to 2 blocks wide following the lightning, healing themselves (no craters); bodies in the radius thrown back and knocked
  down at once (ConquerorRelease.throwBack), a tap a shove, a full charge further than Blast Breath; stun by alignment
  (ConquerorStun: tap 5 s, held 10-20 s, good full 30 s); damage half the current health, never a kill.
- Lightning compositions that have their own sounds land silently (HakiBolts.silence: the release, the charge, aftershocks, Gear 5).

## 7. How the repository works (short; CLAUDE.md and the older notes have the rest)

- `python3 build.py`: PiecePatch (ASM) patches the original jar; javac -Werror compiles src/main/java/com/hexhaki/piece/ against it and
  stubs/; PieceVerify proves every new Minecraft/Forge reference (verify/api-allowlist.txt, srg proofs from a joined.tsrg in the
  scratchpad; `python3 tools/srg_shapes.py <joined.tsrg>` regenerates verify shapes); JvmVerify links every patched class with the JVM's
  own verifier; the archive scope (build.py PATCHED lists every original class the patch may change); then the 13 test suites
  (src/test/java/com/hexhaki/piece/*Tests.java). Minecraft and Forge cannot be downloaded here; GitHub raw files and Maven Central can.
- 2.9.6: the cloud environment is "HexPiece" (not Default): its network allows opengameart.org, freesound.org and cdn.freesound.org
  (sounds), tenor.com, media.tenor.com, media1.tenor.com, giphy.com, media.giphy.com, i.giphy.com (reference GIFs: Tenor's search page
  lists media.tenor.com/<id>AAAAM/<name>.gif; AAAAC is the full GIF), onepiece.fandom.com and static.wikia.nocookie.net. If a session
  starts on Default, get_environment lists HexPiece under saved_for_session_repositories: switch_environment to it.
- A new SRG member in the stubs needs verify/srg-shapes.txt: `curl` MCPConfig's joined.tsrg from GitHub raw into the scratchpad, then
  `python3 tools/srg_shapes.py <joined.tsrg>` after a build. A field the original reads through LivingEntity (f_19862_
  horizontalCollision, f_19863_ verticalCollision) must be read through a LivingEntity-typed variable so the reference matches.
- **Patch rules:** a new jump target needs a FrameNode (`new FrameNode(F_SAME, 0, null, 0, null)`) after its label (2.8.8 crashed the
  game at load without one). Prefer hooks without branches. A redirect row in PiecePatch's table names the caller method, the call,
  the new owner/method and how many calls it must find (the build fails if the count is wrong).
- Never mutate a collection inside its own iterator or removeIf when the step calls out (walk a snapshot). When resetting another
  class's state by reflection, restore its initializer's values, never null or 0 by assumption.
- Key places: server/ (moves, Haki, sounds), client/ (effects, sounds, first person), haki/ (pure lightning, storm, fissures, mix),
  dragon/ and client/dragon/ (Uo Uo no Mi), pika/ and client/pika/ (Light fruit), style/ and server/StyleCombat (fighting styles),
  sword/ and client/sword/ (katanas), npc/ (pirates and marines), network/ (S2CUoFx carries the dragon's, the Light fruit's (>= 32)
  and the Conqueror's (>= 80) effects).
- Keys: Z X C V B N M the fruit's moves in fruit mode; R switches modes (Ctrl + R Armament); G Conqueror, J Observation, K L H the
  Conqueror's quick abilities; O the Ability Book; the style specials Z/X and show moves C/V/B in combat mode.

## 8. Where things stand (2.9.0 to 2.9.5, all pushed and green)

- 2.9.0: the Light fruit after Kizaru (moves animate, clones, Ama no Murakumo by its grip, laser from the fingertip, light flight,
  Yata no Kagami reflecting, every sound from the web); the dragon's Blast Breath fixes (charge sound held, no M1 while charging, the
  bomb).
- 2.9.1: the Haki edition part 1 (charge faster with mastery, sparks off the body and drawn blades, a bigger storm).
- 2.9.2: the dragon's movement reverted to 2.8.9's (speed 0.04 kept). 2.9.3: dragon sounds 3 dB quieter, the final explosion louder.
- 2.9.4: Ryou silent; the Conqueror's release sounds from the web per path (neutral bass boom, good trailer boom, evil dark impact;
  thunder; full: world-ending explosion); every lightning 3D in the Ryou colour (Gear 5 too); sparks only with Ryou on; fissures 1-2
  wide; close bodies thrown and knocked down.
- 2.9.5: the Conqueror's charge is one held sound that fades out the moment G is let go (the original replayed it five times, a
  second late, and the 9 s clip rang up to 10 s after the release); the release's and the charge's lightning crackles silenced;
  Gear 5's lightning silent (its own sounds); the three thunders level-matched. Details: next-ai-2.7.0.md "Done (2.9.5)".

## 8b. Done (2.9.6, pushed): the Light fruit after Kizaru at Egghead

- The owner (verbatim, Goal 1): "Find gifs of Kizaru in egg head island vs Luffy for reference, rework all of his abilities to be smooth,
  fun to use. 1 Ability (Z) has to turn you into light itself, and travel in a beam where you're looking at and reflect against any
  object/water."
- Asked (AskUserQuestion), the owner chose: Z "Hold Z" (tap or hold: light where you look, bouncing off everything; let go or after
  about 3 s you reform where the light is, keeping some speed), and for mobs "Hit and pass through" (struck, the light goes on; it
  bounces only off blocks and water). On 2.9.5 (Conqueror charge/release sound) and 2.9.4 (Haki sparks, Gear 5 lightning, fissures,
  bodies thrown): "Not tested yet" for both. Ask again.
- Reference: 29 Tenor GIFs of Egghead Kizaru (kept only in the scratchpad, never in the repo: they are the anime's frames). What they
  show: he travels as a ribbon of light with a white-hot head, zig-zagging through Egghead's city with a gold ring at every reflection;
  he flies feet-first, straight, spinning, rings of light bursting behind; Egghead's four-pointed star glint sits on every point of his
  light (fingertip lasers, the light round Luffy); Yasakani: face-down in the air, arms forward, a rain of oval slugs of light; clones
  fill the sky.
- Z (server/PikaMoves.light, lightTick, lightOff; client/pika/PikaFx.Glide): pressed, the body is light at once (LIGHT_ON 54: the
  flash, a beam-hum loop while it lasts), going the way the player looked at LIGHT_SPEED 3 blocks a tick. The PLAYER'S OWN CLIENT moves
  the body (Glide FREE: in ClientTickEvent END the velocity for the next tick is set, every movement key zeroed through
  MovementInputUpdateEvent), so it is smooth on screen and the server's own movement check agrees (it allows under 10 blocks a tick:
  DragonTests checks LIGHT_SPEED < 10). Reflection: where the last tick's move was cut short along an axis (LivingEntity f_19862_ /
  f_19863_ and the per-axis shortfall), that axis is turned back (block faces are square to the axes, so that is the mirror reflection);
  water or lava met from above (a fluid clip from the feet) shortens the step onto its face and turns it back up the next tick. The
  server follows the reported positions: whoever is within 1.2 of each tick's way is struck once (LIGHT_HIT 3.5, a flash, a hit
  sound), and a turn of more than about 10 degrees between two ticks is a reflection (REFLECT 56 at the corner: a ring on the face, a
  star, a glass ping, at most one per 2 ticks). Let go (a tap lasts LIGHT_MIN 6 ticks) or after LIGHT_MAX 60: LIGHT_OFF 55, the body
  is back where the light is, with LIGHT_CARRY 0.9 of its speed (off the sea it flies up). Three uses, 4 s each (Yata's, the HUD's).
  The body is hidden while light (RenderPlayerEvent.Pre, as the light flight), no hands in first person (RenderHandEvent), and in first
  person light streaks rush past (LightArt.rush). If LIGHT_OFF never came, the client's LIGHTS entry expires and the glide stops.
- Smooth dashes (PikaMoves.dash, DASH 57; Glide DASH then HOLD): the sprinting kick, the air dive and the speed-of-light kick no longer
  teleport (the old travel() snapped the view). The client carries the body there over ceil(distance / 6) ticks (following the target's
  movement), turns it to face the target, hangs there (3, 4, 8 ticks); the server lands the kick or the explosion on arrival (light kick:
  its clip starts a tick before arrival, the kick on the clip's tick 3; LIGHT_KICK fx is sent as it lands). The server's hold() and
  travel() are gone.
- Look (pika/LightArt): star (Egghead's glint: two long crossed rays square to the eye, two short, gold under white, kept small near the
  eye), head (white-hot ball, star, rings thrown off behind), ribbon (the way it went, narrowing into the head), reflect (rings on the
  face, star, flash), flare (the body turning into light or back), rush (first person). Stars added to the laser's fingertip, the
  beam, the charge, Yasakani's crossed hands and every impact flash. The light flight (sprint while flying) now draws the same head.
  FxPreview shots: `e:yaw:pitch:dist:tx:ty:tz` (camera round a point, no dragon), `|zpath:t` (a ground bounce over time), `|head:t`,
  `|reflect:age`, `|wallreflect:age`, `|star:size`, `|flare:age`, `|rush:t` (with `fp:pitch`). Render: docs/handoff/pika-light-travel-2.9.6.jpg.
  Budget: all of Z's light at once with two reflections stays under 8,000 quads (DragonTests.lightTravel); rush is 16 streaks.
- Sounds (tools/pika_sounds.py, CC0, SOURCES.txt): light_travel.ogg (a loop of "Weapons Beam Loop" by unfa, piece.pika.light_travel,
  played through client/LoopSound while LIGHTS holds the player, faded out on reform) and reflect1.ogg ("UI Hover Glass Ping" by
  Qvantor, piece.pika.reflect). -19.5 and -20.9 LUFS integrated (the sword hum is -15.4).
- Not done (ask first): X (Yasakani) as at Egghead (face-down hover, the rain of oval slugs while moving), the flight pose (feet-first,
  spinning), C and B unchanged. Not tested in game: everything above (only the build, the tests and the renders).

## 8c. Done (2.9.7, pushed): the Light fruit, second pass after the Egghead GIFs

- The owner (verbatim, after 2.9.6): "get rid of the  M1s entirely, of the light fruit, and adapt the other abilities to your reference
  gifs dammit, scap moves if you can make better moves"
- The other GIFs studied (beyond 8b): the finger laser (pointing, rings of light round the finger as it charges); Yasakani (up in the
  air, cape spread, a storm of oval slugs bursting into stars; at Egghead face down over them, arms thrust out, stars on the palms);
  the speed-of-light kick (a streak with a star at its head from far off, a burst of rays where it lands); Ama no Murakumo (the sword
  with a ring and a cross at the fist; Kizaru flying at Luffy laid out with it thrust ahead); the clones (a swarm of light Kizarus
  flying at Luffy); the flight (arms out from the sides, legs together).
- M1s gone: PikaMoves.attack does nothing (kicks, the sprint dash kick, the air dive, the sword's cuts and guard removed with their
  code); PikaKit.KICKS out of MOVES; PikaKit.blocks gone (no guard). Animations removed: pika_kick_1..5, pika_dash_kick, pika_dive,
  pika_sword_3..5, pika_sword_guard, pika_sword_away (pika_sword_1/2 kept: the clones and the lunge's last cut).
- C (PikaMoves.lunge, LUNGE_*): the sword drawn (SWORD_ON for the move's length, pika_lunge), after LUNGE_DRAW 3 ticks a SEEN dash
  (DASH with by 1: PikaFx.LUNGES, not hidden, a 0.6 gold trail) at LUNGE_SPEED 2.6 to LUNGE_PAST 2.5 blocks beyond the aimed foe (or
  LUNGE_RANGE 26 where he looks), cutting whoever is within 1.6 of each tick's way once (lungeTick: 7 damage, heavy, thrown along);
  on arrival pika_sword_1, two ticks later a level cone cut (5.5 blocks, 85 degrees, 6 damage) with its SLASH crescent and the WAVE
  (PikaMoves.wave, 5 damage); cooldown 80. pika_lunge: body pitch -72 (laid out flat, head toward them), the sword arm LUNGE_R
  arm(-122, 4, 0, 0) so the blade points straight ahead, the left arm swept back, the legs trailing. First person: the game's own hand
  holds the sword (FirstPersonLight ARM has no pika_lunge: an arm hung from a laid-out body floats below the view; tried an upright
  copy of the clip, the blade pointed up or out of view, so dropped).
- PlayerAnimator's body pitch: POSITIVE LEANS BACK (style_anims' high kick leans back with +10; the preview agrees). pika_fly had +16
  since 2.9.0 ("leaning into the way he goes" was wrong): now -16/-18. pika_stream (+70) is unused.
- X (Yasakani): pose pika_yasakani after Egghead (rising with the arms out, then both arms thrust out toward the ground ahead, POUR_R
  arm(-62, -14, 8, -6), the body bent over them, jolting as it pours); PikaFx draws a LightArt.palm (glow, light, star) on each palm
  (PikaFx.palm: 0.95 up, 0.62 ahead, 0.42 to the side) in place of the crossed hands; the bullets are long oval slugs
  (LightArt.bullet: stretch 2.6, a longer streak); every burst flashes a star (LightArt.burst, size rr * 3.2 at the flash). Server:
  volley turns flying on (when the player may fly) and no longer damps the motion: the player flies about while it pours.
- B: PikaFx.clone flies each clone at its foe in an arc (up 2.5 to 3.5 blocks over the middle) in pika_lunge's pose at tick 9 with
  its afterimages, then the two cuts and the burst as before (server unchanged).
- Flight: pika_float and pika_fly hold the arms out from the sides (OPEN_R arm(-14, 0, 36, -8)), legs together; pika_fly leans
  forward. Renders: docs/handoff/pika-egghead-2.9.7.jpg (the lunge from the side; Yasakani side and back with the float; the clones
  flying and cutting, in the owner's skin). Previews: PikaPreview gained `--fp` (the eye at 1.62, limbs only); ClonePreview shows the
  flying clones; FxPreview `|palm:age`.
- Not tested in game: all of it.

## 8d. Done (2.9.8, pushed): the light sword held and drawn after the owner's reference

- The owner (verbatim): "I dont like the sword". Asked what (AskUserQuestion), they picked "Its look" and wrote "its held wrong, and
  looks bad ngl." Their reference is docs/handoff/murakumo-ref-1.png (Egghead: the sword hanging from the right hand, the ring with its
  cross right at the fist, the blade down and ahead past the legs) and murakumo-ref-2.png (held level, a burst of light at the hand).
- Held: LightClone.GUARD 0.14 (was 0.66: the ring two thirds of a block out along the blade, the hand on a bare rod) and LightArt.GRIP
  0.26 (the grip behind the ring, was 0.8): the hand closes on a short grip with the ring right past the fist, nothing sticking out
  (the owner's 2.8.9 "grabbing it way too up" was the grip sticking out past the hand). Both grips (THIRD_PERSON_GRIP,
  FIRST_PERSON_GRIP), LightClone.held (the clones, FirstPersonLight's posed arm) use GUARD. TILT 50 kept (the blade 40 degrees off the
  forearm's line: hanging, it points down and ahead, as in the reference).
- Rest: pika_sword_idle / pika_sword_walk (and the start and end of pika_sword_1/2) hang the arm easy at the side (SWORD_REST arm(-6, 0,
  12, 0)) instead of holding it out ahead (HOLD_R).
- Look (LightArt.blade): BLADE_LENGTH 1.9 (was 2.6), BLADE_WIDTH 0.05 (was 0.032), the sheath 2.1x the core, the glow sheets wider and
  brighter, the ring and its cross twice, square to each other (rodZ(..., alongX)), so the crosshair reads from every side, the rays at
  the hand 1.2x longer, a glint at the point. Render: docs/handoff/pika-sword-2.9.8.jpg (at rest from two sides, close-ups, first
  person through GripPreview, the lunge and the level cut).

## 8e. Done (2.9.9, pushed): no flight for the Light fruit; Z's collision is a kick

- The owner (verbatim, with a frame of Kizaru's light-speed kick sending Gear 5 Luffy flying, light streaks round him): "why can light
  fly? The only flight should be through Z. Plus when Z collides with an entity it should be a kick like the reference I sent causing
  ragdoll after the kick and a ton of knockback."
- No flight: PikaKit.flight is only ever called with false now (granted, and every tick in PikaMoves.tick: it takes away the mayfly an
  older version gave; it leaves creative and spectator alone). PikaFx's light flight (sprinting while flying, LIGHT_FLIGHT) and lit()'s
  fast-flier case are gone (lit = Z or a dash). X's volley no longer turns flying on: it lifts (0.75 up) and holds the player in the
  air while it pours (the 2.9.6 damping). The BODY text says so. pika_float / pika_fly are now only seen in creative flight.
- Z's kick (PikaMoves.zKick): in lightTick, the first foe (nearest along the way) within 1.2 of the tick's way ends the light: LIGHT_OFF
  with id = the one kicked and bx = Z_KICK_HOLD 8 (the client faces them and hangs: Glide.hold), pika_z_kick (a flying kick: knees
  tucked, the right leg driven out level with their chest, leaning back off it), hit Z_KICK_DAMAGE 9 heavy, thrown Z_KICK_THROW 4.6
  blocks a tick along the light's flat way + 1.15 up, then HakiFalls.knockDown where they land (or after 2 s) for the rest of
  Z_KICK_STUN 100 ticks (as UoMoves.blowAway), LIGHT_KICK fx (the kicked one's trail of light, the big flash), a KICK flash, a quake,
  KICK_BLAST and HIT_HEAVY. LIGHT_HIT (the old pass-through blow) is gone.
- Not tested in game.

## 8f. Done (3.0.0, pushed): Z's kick in front of them, a new reflection sound, Z's cooldown

- The owner (verbatim, three messages while it was being built): "when kicking, the player stops directly infront of the entity
  looking at it. currently im stopping a bit after it, or on the side." / "sometimes looking behind me" / "Also the reflection sound
  needs to be different, get a cooler reflection sound from the web" / "Z has no cooldown by the way."
- Why: the server reads where the body is a tick late and the light goes 3 blocks a tick, so the kick was noticed after the light had
  passed them; the client then turned to face them from there (often back the way it came).
- Kick placement (PikaMoves.lightTick, zKick, kicked; PikaFx LIGHT_OFF): the server looks one tick ahead (crossed from the last point to
  c + way * LIGHT_SPEED, only foes seen without a block between: seen()); zKick works out the stand directly in front of them on the
  light's incoming (flat) side, Z_KICK_GAP 0.85 off their side, lifted so the kick meets their chest (height * 0.6 - 1.15), room()
  checked; LIGHT_OFF carries that stand's middle (x, y, z), the one kicked (id), Z_KICK_HOLD (bx) and Z_KICK_GLIDE 2 (by); the client
  glides there (Glide.dash: following them, turned to them from the stand at arrival) and hangs; pika_z_kick starts a tick before
  arrival; the blow (kicked()) lands on arrival.
- Reflection sound: reflect1.ogg is now "Laser Zing Shot" by Qvantor (Freesound 881251, CC0), 0.6 s, -18 LUFS (tools/pika_sounds.py
  laser_zing; the glass ping is gone).
- Cooldown: FruitMove's fourth field is its COOLDOWN key (not an animation); Z's ("pika_yata") was never started. Now
  cool(PikaKit.YATA, Z_COOLDOWN 80) when the light ends (lightOff, zKick); ready() checked on press. The three uses (YATA_KEY, YATA_TIME,
  the recharge, the view tag) are gone. (MURAKUMO's fourth field "pika_lunge" is its cooldown key too: fine.)
- X's and Z's book texts updated (X held in the air; Z kicks whoever it meets, 4 s cooldown).
- Not tested in game.

## 8g. Done (3.0.1, pushed): Z's slam from above, Z held as long as the key, no own trail in first person

- The owner (verbatim, while 3.0.0 was being pushed): "and sometimes Z stops working after recasting, but overall it's a very fun
  move." / "If Z hits from above the entity make it slams the entity downwards, not to the side with a downwards kick animation" /
  "Also Z does not end till the user lets go of the Key." / "Also in first person you should not see the trail of light, only in
  third, but you do see the speed marks" (with two screenshots: in first person looking down, the ribbon's gold sheath a huge cone from
  the eye; the speed streaks in first person, a green line drawn along the light's bounce).
- "Stops working after recasting": in 2.9.9 Z had 3 uses, each recharged 4 s after the last (one timer), unseen; 3.0.0 replaced them
  with one visible 4 s cooldown. Told the owner to report what they did if it still happens.
- Slam (PikaMoves.zKick, kicked(..., slam), Z_SLAM_STEEP -0.55): the light's way steeper than that down, or the player over 0.9 of their
  height: the stand is over them (height + 0.15) and just in front (half width + 0.35), pika_z_slam starts at once (an axe kick: the
  leg swung up past the head on tick 1, the heel down on tick 2 with the body bent over it, as the 2-tick glide arrives), thrown
  straight down (-0.7 x Z_KICK_THROW, 0.3 along), a BURST of 2.6 and a stronger quake on the ground under them (PikaMoves.point(body,
  depth)), EXPLODE, knocked down from tick 2 on the ground.
- LIGHT_MAX 600 (was 60): Z lasts as long as it is held; 30 s is only a safety for a lost key release.
- PikaFx: one's own trail (ribbon, lunge trail, light head) is not drawn in first person; the rush streaks are.
- Not tested in game.

## 8h. Done (3.0.2, pushed): the right click's laser and the held beam's lingering blast

- The owner (verbatim): "More, the Right click (tap) should just be the laser (small) no explosion at the end, the HELD version should
  have a lingering explosion effect, it's dissappearing instantly after it explodes, which makes it look ugly."
- Tap (PikaMoves.quickLaser): only the laser and its hit on the one aimed at; no UoMoves.explode, no BURST, no EXPLODE sound.
- Held (PikaMoves.beam) and V's leg beam: why it vanished: LightArt.beam drew the blast (burst) inside itself and the beam's LINE is
  dropped after BEAM_LIFE 12 ticks, cutting a 40-70 tick burst off. Now the server sends its own BURST (2 + 3 x charge; V's 2 + 2 x
  charge), which lives LightArt.burstLife (gather, flash, dome, ball, dust fading), and LightArt.beam no longer draws a burst.
- Not tested in game.

## 8i. Done (3.0.3, pushed): the first-person arm points forward

- The owner (verbatim, with a screenshot of the laser charging in first person: the dark sleeve a huge box from the middle of the view
  to the right, the charge's light at the right): "also in first person the hand is bugged, it pointing to the side and not forward".
- Why: client/FirstPersonLight hangs the posed right arm from the eye as the body has it: the shoulder sits 0.36 right, 0.32 down and
  at the eye's own depth (z about 0), the fist 0.63 ahead; the upper arm runs past the camera, projected huge across the view (a vertex
  behind the eye can fold to the other side on some drivers).
- Fix: FirstPersonLight.VIEW_SHIFT {0.12 right, 0.15 down, 0.5 ahead} applied in place(view) before the view's pitch and yaw: the arm,
  the sword in the fist (FirstPersonLight's posed sword) and the laser's fingertip all move with it. PikaPreview gained `--shift x,y,z`
  (with --fp: the view moved back, up and left by it, the same thing). Rendered pika_laser_charge and pika_sword_1 in first person.
- Not tested in game.

## 8j. Done (3.0.4, pushed): X's lag, the punch back, V as HexGhoul's Speed Kick

- The owner (verbatim): "X move is causing extreme lag, and is shooting too fast, too many projectiles. LEft click should be default
  punching instead of nothing. LEft kick (projectile) also sufferes from the lack of explosion linger thing. Light kick should now be
  like V, kicks forward but much much farther with an impact frame and sending through blocks (like Speed kick in HexGhoul)".
  Read as: V (the speed-of-light kick) kicks forward, much farther, an impact frame, the body sent through blocks.
- X: every bead landing added a full LightArt.burst (~1,500 quads, 28 ticks), a dozen alive at once: now PikaFx adds a DODGE spot
  (LightArt.impact with its star, ~80 quads, 7 ticks); VOLLEY_EVERY 5 (was 2), VOLLEY_SPEED 2.2 blocks a tick (was 3.2).
- Punch: PikaFx.punch (InteractionKeyMappingTriggered, LOWEST, receiveCanceled): undoes the original FruitKeys.interact's cancel of
  the attack from an empty hand in fruit mode for the Light fruit (FruitClient.active, fruit PIKA, empty main hand, not gliding): the
  game's own punch and swing. PikaMoves.attack (kind 2) still does nothing.
- V's leg beam blast: already its own BURST since 3.0.2.
- V (PikaMoves.lightKick with a target, speedKick, Launch): HexGhoul's Speed Kick (hexghoul repo: PaleMoves.kick/plan/kickLands,
  BlockSmash.kick/carried, PaleRules.KICK_*; cloned read-only to /home/user/hexb1tdev/hexghoul): a light DASH to straight in front of
  them on his side (Z_KICK_GAP, lifted to the chest), pika_z_kick a tick before arrival, on arrival speedKick: hit 7 + 9 x charge
  heavy, an impact frame (Clashes' S2CClash, no Haki colours, power 1.6 + charge, its own ids from 1 << 30), LIGHT_KICK and KICK fx,
  a quake, then Launch (a UoMoves.Live): carried along the flat kick at V_SPEED 4.6 x (0.7 + 0.3 x charge) (+ V_UP 0.5 on its first
  tick), each tick every colliding block in its swept box (not the floor it skims) up to V_HARDEST 5 (iron) torn out through
  Scars.tear (restored 15-25 s, never dropped; hanging supports kept), V_PER_BLOCK 0.96 of its speed each, at most V_MOST_A_TICK 24
  and V_MOST_BLOCKS 160; anything else stops it dead (a small BURST, HIT_HEAVY, (speed - 1) x 2.5 damage); down after 2 ticks on the
  ground, past V_FLIGHT 60 ticks or under 0.7: HakiFalls.knockDown for the rest of Z_KICK_STUN 100. The old behind-left spin kick
  (2.9.0) is gone; pika_light_kick is still the no-target beam's clip.
- New SRG members (srg-shapes regenerated): getCollisionShape, AABB.expandTowards, VoxelShape.isEmpty, Entity.hasImpulse.
- Not tested in game.

## 8k. Done (3.0.5, pushed): the arm in first person during the right click; the held beam's bigger blast

- The owner (verbatim, with a screenshot in first person charging the right click: the light's white ball at the lower right of the
  crosshair, no arm anywhere): "right click is still not showing the arm"; then "also the right click explosion range (full charge
  and Mid charge, is too small, and entities are barely getting knocked back)".
- Why no arm: client/FirstPersonLight cancelled the game's own hand (RenderHandEvent) while a laser or sword clip played and drew a
  posed arm of its own at AFTER_PARTICLES; on the owner's device that arm never showed (3.0.3's VIEW_SHIFT put its corners in the
  lower right of the view, computed; it was still not seen), so no arm at all. (The original's FruitFirstPerson only asks the kit's
  firstPerson and cancels the hand if it returns true; PikaClient's returns false; FruitAnimations plays fruit clips with
  PlayerAnimator's FirstPersonMode.NONE.) Not found why it never showed; not needed any more:
- Fix: FirstPersonLight.ARM is empty (no clip draws the posed arm or hides the hand): the game's own first-person right hand always
  shows; the sword in it through PikaClient.firstPerson (LightArt.FIRST_PERSON_GRIP, judged in 2.8.8); the laser's fingertip in
  first person is LightArt.firstPersonFingertip(0.1) (FIRST_PERSON_GRIP's steps up to the fist, 0.1 on: about 0.75 right, 0.43 down,
  1.2 ahead of the eye) turned with the look (FirstPersonLight.fingertip, view branch). Third person still uses the posed fingertip.
- Blast (PikaMoves.beam): radius 5 + 8 x charge (was 3 + 4), knock 2.6 + 3.4 x charge (was 1.4 + 1.6; UoMoves.explode, HEAVY over
  1.5), the BURST 3 + 5 x charge (was 2 + 3), the quake radius x 4.
- Not tested in game.

## 8l. Done (3.0.6, pushed): Ama no Murakumo's wave level

- The owner (verbatim, with a screenshot from behind: a thin arch of light standing up in the distance): "Ama no Murako is sending
  slashes in a weird way, like, it's going vertical its very strange".
- Why: PikaFx drew the lunge's WAVE with FireMesh.arc round its own way (the axis), so the crescent stood square across its path, an
  arch. Now LightArt.wave: a crescent round the up axis, its middle on the wave's point, bowed out ahead (start angle from basis(0, 1,
  0): atan2(-wx, -wz)), five bands 0.15 apart (gold 0.75, white core), 150 to 240 quads. FxPreview `|wave:age`.
- Not tested in game.

## 8m. Done (3.0.7, pushed): stuck in the sword's stance

- The owner (verbatim, with a screenshot of the player stood, the right arm hanging a little out from the side, no sword): "another
  bug, the player sometimes gets stuck in this position and cant use left click (the fists dont move) etc."
- Why (from the code; not reproduced): PikaClient.stance played pika_sword_idle / pika_sword_walk (they key the right arm, so the
  vanilla punch swing cannot move it) while PikaFx.sword(id) OR the server's "sword" in the player's synced state; that state is only
  resent by FruitServer.sync, and PikaMoves.forget (mode switch, death, the fruit set aside: STATES dropped) never resent it, so a
  sword that went with its move left "sword" true for good.
- Fix: the stance follows only PikaFx.sword (the client's SWORD_ON timer, always running out); forget() also syncs when a sword was out.
- Not tested in game. Asked the owner to say what they had done if it still sticks.

## 8n. Done (3.0.8, pushed): the katana's ready pose under every other animation

- The owner (verbatim, with a screenshot of the player standing with Yoru drawn, held low at the side): "this pose is breaking every
  other animation."
- Why: client/sword/SwordClient played every sword animation (the guard and the still idle too) on its PlayerAnimator layer at 920,
  above the original's Haki/style layer (900): a drawn sword's ready pose covered every Haki and style move.
- Fix: the guard and the idle (anim GUARD) play on a layer of their own, STANCE_LAYER at STANCE_PRIORITY 880 (under 900 and the
  fruits' 1100); cuts, draws, sheathings and the rest stay at 920; switching between the two fades the one left out; existing()
  returns the stance's layer while GUARD plays, so SwordLayer's held blade still reads the playing layer's rightItem channel.
  ModifierLayer.getAnimation is not a proven reference (PieceVerify): not used.
- Not tested in game. Asked the owner to name any move that still does not show with a sword out.

## 8o. Done (3.0.9, pushed): no sea while the body is light

- The owner (verbatim): "Another bug, while relecting over water, the player sinks to the sea floor nearly instantly. while in yata no
  agami, the player NEVER suffers the effect of the sea."
- Why: the original's FruitServer.inSea is the player's isInWater (not in a boat); FruitServer.tick then, for fruit owners, sinks the
  player (x, z x 0.7, y at most -0.08 while the eyes are in water), gives weakness and slowness every second, and on first entering
  (the "seaHeld" flag) cancels the kit (PikaMoves.forget: LIGHT_OFF, the glide ends). The water reflection stopped the feet on the
  surface, inside the water's block.
- Fix: PiecePatch VIRTUAL_REDIRECTS row: in FruitServer.inSea, Player.m_20072_ -> PikaMoves.inWater(Player): false while the player's
  body is light (moving(): Z, its kick's glide and hold, a dash as light), else isInWater. build.py PATCHED lists FruitServer (JvmVerify
  links it). The client's water bounce now turns back 0.15 over the surface (PikaFx.glide). srg-shapes regenerated (isInWater).
- Not tested in game.

## 8p. Done (3.1.0, pushed): no Light fruit stance at all

- The owner (verbatim, with a screenshot of the player stood with the hands in the pockets): "this pose presists, its not needed, and
  it bugs like shit".
- PikaClient.stance returns null always (the original's FruitAnimations plays a fruit's stance without end; the Light stances keyed
  the arms, and pika_float/pika_fly the whole body, so the vanilla punch and other animations could not move them). pika_idle,
  pika_walk, pika_float, pika_fly, pika_sword_idle, pika_sword_walk are no longer played as stances (the files stay: the clones and
  previews sample some). Never bring a Light stance back.
- Not tested in game.

## 8q. Researched (3.1.0's last push): the Gura Gura no Mi after Whitebeard and Blackbeard

- The owner (verbatim, 2026-11-22): "Now do Gura Gura, get reference gifs from white beard and black beard both". Then, while the
  kit was being started: "When you get the material for the reference upload it to GitHub and give me a handoff and a prompt for the
  next ai". So this session stopped before writing any code: **the build is still 3.1.0; nothing of the Gura fruit exists yet.**
- **The references are in `docs/handoff/gura-ref/`** (README.md: every GIF, who, what it shows, which move it informs, its Tenor
  link; `sheets/`: all 56 as frame strips; `clips/`: the 30 that show the powers as small MP4s). Read the README and look at the sheets
  before designing anything. What they agree on: the white quake **bubble** round the fist; the **air cracking like glass** (a web of
  jagged white lines with a cyan glow, radial cracks joined by broken rings, hanging where the fist struck, then shattering away);
  white **shock rings and curved shock lines** racing ahead with rocks and dust; the **ground**: crater, white shock lines thrown up,
  debris, dust dome, concentric rings seen from above, a split running across it; the **tsunami** walls; Blackbeard's **open shaking
  palm** and his **dive from the sky**. His darkness (Yami) clips are not this fruit: never use them.
- **What the original has**: `Fruit.GURA` (id "gura", "Gura Gura no Mi", element "Tremor", a Paramecia, its lore line) and the item's
  model and texture (`gura_gura_no_mi`), but **no kit at all**: `Fruits.register` leaves it the `Fruits$EmptyKit` (no moves), no
  animation in the jar, and `FruitAnimations.handles` knows a "quake_" prefix with no clip behind it. The kit is built from scratch,
  registered the way HexPiece registers the Light fruit's (`HexPiece.init`: `Fruits.register(Fruit.GURA, GuraKit::new)`).

### The proposed move set (this session's design; the owner has NOT seen it)

Keys as the owner set them for every fruit (Z X C V B, the attack and use buttons). Show the owner this list in one AskUserQuestion
(go ahead / change) before building, unless they already said to just build it.

| Key | Move | What it does | Refs |
|-----|------|--------------|------|
| LMB | the game's punch | as the Light fruit since 3.0.4 (give the attack back to the game as `PikaFx.punch` does, for `Fruit.GURA`) | |
| RMB tap / hold | Kaishin (Sea Quake) | tap: the bubble flashes on the fist, a punch into the air, the air cracks like glass at the fist, a shockwave cone about 14 blocks. Hold (to 40 ticks): the bubble swells on the drawn-back, trembling fist; let go: the air shatters, the shockwave reaches 18 to 40 blocks, the ground under its path torn (small self-repairing craters), everyone in the cone thrown far, ragdolled from half charge, an impact frame at full | 00 01 02 07 15 52 |
| Z hold | Shima Yurashi (Island Shaker) | both arms thrust out to the sides, the air cracking at both fists, then he grips the air: while held (up to 5 s) the ground quakes about 24 blocks round: everyone's view shakes (`Impact.quake`) and tilts (camera roll), foes hop and stagger every half second, cracks spread over the ground; let go: he pulls the air down, the "island tilts": everyone in range thrown out and ragdolled, a big lurch of the view | 47 48 |
| X | Kabutowari (Helmet Splitter) | a leap at the one looked at (up to about 26 blocks, an arc the player's own client flies, as `PikaFx.Glide.dash`) and the quake fist brought down on their head: the glass crack over them, slammed into the ground, a crater with concentric rings, everyone near thrown up. No one aimed: the leap goes where he looks and he dives fist first into the ground (Blackbeard onto Law) | 04 06 32 34 52 |
| C | Tsunami | both fists punched out, a wall of water (about 9 high, 22 wide) rises ahead and rolls on about 40 blocks, carrying everyone it meets and dumping them down; drawn as a cel-shaded curling wave with a foam crest and spray (no real water blocks) | 29 51 |
| V hold | Gekishin (Violent Quake) | the bubble gathered on the drawn-back fist (up to 1.5 s); let go: a short dash to just in front of the one looked at (up to about 16 blocks) and the bubble driven point-blank into them: the shock bursts out of their back and cracks the air behind them, an impact frame, sent flying through blocks up to iron (as `PikaMoves.Launch`). No one there: the punch cracks the air ahead | 00 11 14 53 |
| B | Ground Splitter | both fists into the ground: a rift runs out ahead about 36 blocks, opening block by block (Scars, self-repairing), everyone along it thrown up and ragdolled | 28 37 41 55 |
| passive | Tremor landing | no fall damage; a hard landing quakes the ground round the feet (a small ring that knocks foes back). Unasked: ask the owner before adding it | 50 |

**Asked (AskUserQuestion, 3.1.1's session): the owner chose "Go ahead" for the whole set above and "Add it" for the passive.**
Built as 3.1.1: section 8r.

### How to build it (what was found in the code; the Light fruit is the template)

- Files to add: `server/GuraKit.java` (the move list, `action` kinds 0 key down, 1 key up (only for moves whose key text says "hold"),
  2 attack, 3 use down, 4 use up; `immune` for fall damage), `server/GuraMoves.java` (as `PikaMoves`: per-player State, `fx(...)`,
  `later`, `ready`/`cool`, `aimed`, `point`, `foes`, `hit`, `LIVE` list ticked in a LevelTickEvent), `gura/GuraArt.java` (pure 3D art
  as `pika/LightArt`: eye-centred, a `FireMesh.Sink`, previewable off-game), `gura/GuraSounds.java`, `client/gura/GuraClient.java`
  (a `ClientKit`, put in `ClientKits.KITS` on the first client tick as `PikaFx.tick` does for PIKA), `client/gura/GuraFx.java` (as
  `PikaFx`: `accept`, the clock, drawn once a frame AFTER_PARTICLES through `RenderType.m_269166_()`, the leap mover, the punch
  hand-back, the camera tilt), `tools/gura_anims.py`, `tools/gura_sounds.py`.
- Hooks to touch: `client/dragon/UoFx.accept` routes kinds >= 80 to the Conqueror's sparks and >= 32 to `PikaFx`: route **60 to 79**
  to `GuraFx` before the >= 32 check (the Light fruit uses 32 to 57; `S2CUoFx` writes the kind as a byte). `PieceHooks.fruitClip` and
  `PieceHooks.firstPersonArms`: add "gura_". `client/npc/StylePoser.data`: "gura_" clips from a generated `GuraPoses` (`java_poses`
  in `tools/style_anims.py`, as `pika_anims.py` makes `PikaPoses`). Sounds in `sounds.json` and the lang file.
- Ready-made pieces: `Impact.quake(level, at, radius, strength, ticks)` (the original's view shake for players near);
  `Impact.hit(user, body, StyleCombat.spec(weight, launch))`; `UoMoves.explode(user, level, at, dir, radius, damage, fire, knock)`;
  `Craters.open(level, owner, at, radius)` (self-repairing ball, at most 13); `Scars.of(level).tear(owner, pos, supports, restoreIn)`
  (TORN / LEFT / REFUSED; `PikaMoves.Launch` shows a body driven through blocks with it); `HakiFalls.knockDown(level, body, ticks,
  false)` (the ragdoll); an impact frame is an `S2CClash` sent as `PikaMoves.speedKick` sends it; `HakiServerController.animate(player,
  name, 1)` plays a clip; `ViewportEvent.ComputeCameraAngles#setRoll` is already used (`client/fx/Tumbles.fallView`), so the Z tilt
  needs nothing new proven. A client-flown move: `PikaFx.Glide` (velocity set a tick ahead, movement keys zeroed in
  `MovementInputUpdateEvent`).
- The bubble on the fist: in third person from the playing clip's pose (`StylePoser.sample` + `StyleRig.limb(pose, RIGHT_ARM, ...)`,
  placed as `FirstPersonLight.place` does: add a public wrapper; `FirstPersonLight.CLIPS` already records every clip played); in first
  person the game's own right hand stays (as the Light fruit since 3.0.5) and the fist is `LightArt.firstPersonFingertip(past)` turned
  with the look, as `FirstPersonLight.fingertip` does.
- The art (plan): the air crack as camera-facing line quads (each segment's width axis = cross(segment, its midpoint), the camera being
  at the origin), a white core over a wider cyan glow, rays grown out in 2 ticks, held, faded and broken into `FireMesh.shard`s by
  about 26 ticks; the bubble a `FireMesh.shell` with a bright rim, spinning `FireMesh.arc`s and ripple shells; the shockwave
  `FireMesh.ring`s travelling and widening along the punch with streaks; the ground: radial cracks glowing white then cooling dark,
  concentric rings, a ring of dust puffs (`FireMesh.SMOKE`), rocks on arcs (`FireMesh.shard`); the tsunami a curling profile swept
  across about 20 columns in flat cel bands (deep blue to white foam), foam puffs on the crest, spray off the lip, rising in 10 ticks
  and crashing down at the end, its base following the ground under its middle. Keep each effect well under the 16k quad budget.
- Animations: Whitebeard is heavy and wide-legged. Clips (keying only the parts each move uses, never the head; positive body pitch
  leans BACK): gura_punch, gura_charge, gura_release, gura_charge_end, gura_shake, gura_tilt, gura_leap, gura_slam, gura_tsunami,
  gura_fist_charge, gura_gekishin, gura_split. No stance (the owner removed the Light fruit's: never give a fruit a held stance).
- Sounds (CC0 Freesound only, `tools/web_sounds.py` refuses anything else): an earthquake rumble to loop for Z, glass cracking and
  shattering (the air breaking), a deep boom for the punch, rocks crumbling, a big wave crashing for C, a whoosh for the leap. No sound
  outlasting its move.
- Render every effect and pose (FxPreview shots, PikaPreview-style pose sheets, third and first person) and judge them against the
  sheets in `gura-ref/` before pushing. Version 3.1.1 with release notes.
- Getting more references: Tenor's search pages load from this environment (`https://tenor.com/search/<words>-gifs`; parse
  `<img src="https://media.tenor.com/<id>AAAAM/<name>.gif" alt=...>`, download `<id>AAAAC/<name>.gif` for the full size); Giphy
  answers 403.

## 8r. Done (3.1.1, pushed): the Gura Gura no Mi

- The owner (verbatim): "Now do Gura Gura, get reference gifs from white beard and black beard both". Shown the move set of 8q in one
  AskUserQuestion: "Go ahead (Recommended)"; the passive (no fall damage, a landing quake): "Add it".
- Session note: this session started on the Default environment (freesound.org refused); `switch_environment` to HexPiece
  (env_01VDpZP1V4zScExADvRmie8m) fixed it (handoff section 7).
- **Server** (`server/GuraKit`, `server/GuraMoves`; registered in `HexPiece.init` over the original's empty kit): S2CUoFx kinds 60 to 79
  (`CRACK` 60, `BUBBLE_ON/OFF` 61/62, `SHOCK` 63, `QUAKE` 64, `SHAKE_ON/OFF` 65/66, `RIFT` 67, `TSUNAMI` 68, `LEAP` 69, `DASH` 70,
  `TREMOR` 71); `client/dragon/UoFx.accept` routes 60-79 to `client/gura/GuraFx` before the Light fruit's >= 32. Every punch's shock
  reaches bodies as the drawn shock does (`SHOCK_SPEED` 3 blocks a tick: `GuraMoves.shock` hits each body `d / 3` ticks late).
  - RMB Kaishin: tap (< `TAP` 6 ticks) `gura_punch`, on tick 2 a CRACK 1.5, a SHOCK 14 blocks (26 degrees), 5 damage, knock 1.6, cooldown
    16. Held (`gura_charge`, BUBBLE_ON id 0, full at 40): `gura_release`, on tick 1 a CRACK 2 to 5, SHOCK 18 to 40 blocks, 6 to 16 damage,
    knock 2 to 5, knocked down (HakiFalls via `downOnLanding`) from half a charge, an impact frame (S2CClash, ids from 2^30 + 2^28) on
    the first struck at full, from a third of a charge up to 5 self-repairing craters (`Craters.open` 1.2 to 2.5) along the way with a
    QUAKE each as the shock passes. Cooldown 40 to 100.
  - Z Shima Yurashi (hold, up to `SHAKE_MAX` 100): `gura_shake` (arms thrown out to the sides by tick 4: CRACK id 2 at both fists,
    held as long as Z is), SHAKE_ON (radius 24): the player walks at half speed, every 10 ticks `Impact.quake` and foes on the ground
    hop and take 1. Let go: `gura_tilt`, SHAKE_OFF (power 0.25 to 1 by time held), everyone in 24 blocks thrown out (1.4 to 3.6) and up,
    4 to 12 damage, knocked down; `Impact.quake` radius 48. Cooldown 140 + held. Clients: view roll and pitch shake while held,
    a 10-degree lurch when let go (`GuraFx.tilt`, ViewportEvent.ComputeCameraAngles), the rumble loop (LoopSound, faded out on release).
  - X Kabutowari: `PikaMoves.aimed` 26 blocks; a LEAP (the player's own client flies a parabola, `GuraFx.Move`, 6 to 16 ticks, height
    2.5 + 0.18 x distance) to just in front of them (or the ground where he looks), `gura_leap` then `gura_slam` 3 ticks before landing;
    on landing: CRACK over their head (2.4), 12 damage thrown down, knocked down 100 ticks, `Craters.open` 2.6, QUAKE 7, everyone within
    6.5 thrown up (5 damage). Cooldown 120. The bubble shows on the fist through the leap.
  - C Tsunami: `gura_tsunami`, on tick 4 the wave (22 wide, 9 high, rises in 8 ticks, 1.1 blocks a tick for 38 ticks; drawn, never real
    water): whoever its face meets is caught (4 damage, then 1 every 8 ticks), carried with it, dumped down and knocked down when it
    breaks (`GuraMoves.Wave`; its foot follows the ground). Cooldown 220.
  - V Gekishin (hold, full at 30): `gura_fist_charge`, BUBBLE_ON id 1; let go: a DASH (3 blocks a tick) to just in front of the aimed one
    (16 blocks), `gura_gekishin` a tick before arrival; 9 to 22 damage, an impact frame, CRACK behind them, a SHOCK out of their back
    (those behind take 4 to 9), then `PikaMoves.Launch` (its new `stopped` hook draws a Gura QUAKE instead of the light burst) through
    blocks up to iron. No one aimed: a punch into the air (CRACK + SHOCK 12 to 22). Cooldown 120 to 180.
  - B Ground Splitter: `gura_split`, on tick 4 a RIFT 36 blocks, 2 a tick along the ground's own lie (`GuraMoves.Rift`: 3 wide, 2 deep
    and 1 deeper through `Scars.tear`, back in 15 to 25 s, at most 220 blocks, mobGriefing obeyed), everyone along it thrown up and
    sideways (7 damage), knocked down. Cooldown 180.
  - Passive: `GuraKit.immune` (no fall, no wall damage); a landing after a fall of 4+ blocks (`State.lastFall`, not after its own leap or
    dash, not in water): TREMOR (radius 2.5 to 7), foes in it knocked back, `Impact.quake`, a boom (at most every 10 ticks).
  - LMB: `GuraFx.punch` gives the attack back to the game's own punch (as `PikaFx.punch`), not while a bubble, the shake or one's own
    leap or dash is on.
- **Art** (`gura/GuraArt`, pure, eye-centred, previewable): `crack` (13 jagged spokes and 3 broken rings, white core in a cyan glow, a
  flash; holds to tick 8, then drifts apart, fades and throws glass shards, gone by 26), `bubble` (rim-bright see-through shell, white
  heart, outer glow, two ripples, three arcs, flickering skin cracks), `shock` (front ring and a trailing one, 9 curved shock arcs
  leaning back, 14 streaks, never within 4 blocks of the eye), `quake` (cracks from the crater's edge glowing then cooling dark with
  branches, 3 rings pressed into the ground one after another, a shock wall, 8 white shock lines thrown up, rocks on arcs, light grey
  dust), `tremor`, `shake` (cracks spreading over 50 ticks, a pulse every lurch, dust), `tilt` (a shock wall to 24, dust wall, rocks),
  `rift` (dark crevasse with jagged edges, white-hot light in the split near its front, side cracks, rocks, dust; heights per block
  computed once on the client), `tsunami` (13-point curling profile x 22 columns in flat cel bands, light streaks up its back, white
  streaks down its face, foam on the crest and foot, spray off the lip; it breaks forward at the end). One's own crack in first person
  is pushed out to 1.7x its radius so it never fills the view (ref 39: the sky cracking); one's own bubble in first person at most 0.22.
- **Budget:** `DragonTests.guraQuakes`: a full Kaishin (crack, shock, five quakes, bubble) and every ground effect at once near the eye
  stay under 16,000 quads at every moment (dust and foam puffs capped). In the renders: Kaishin 3,600, Kabutowari 2,100, the tsunami
  2,000, the rift 1,200, the tilt 1,050.
- **Client** (`client/gura/GuraFx`, `GuraClient` put in `ClientKits.KITS` on the first client tick; no stance): `FirstPersonLight.fist`
  (new, public) places the bubble on the posed right fist in third person and on the game's own hand in first person.
  `PieceHooks.fruitClip` and `firstPersonArms` know "gura_"; `StylePoser.data` reads `client/npc/GuraPoses` (generated).
- **Animations** (`tools/gura_anims.py`, 11 clips, only the parts used, never the head, build.py's lint clean): gura_punch, gura_charge,
  gura_release, gura_shake, gura_tilt, gura_leap, gura_slam, gura_tsunami, gura_fist_charge, gura_gekishin, gura_split. A raised
  arm overhead needs roll ~16 (more roll swings it through the head when upside down: `anim.lint` says so).
- **Sounds** (`tools/gura_sounds.py`, CC0, SOURCES.txt): boom (Qvantor "Boom with Rumble", zazz "Deep Cinematic Impact 4"), crack
  (unfa "Glass Break", LukaCafuka "Glass panel shattering"), rumble loop (crunchymaniac "Hard Earthquake", 6 dB down), quake
  (uagadugu "Cracking Earthquake"), crumble (ALLANZ10D "Rocks Crumbling", NeoSpica "Rock Smash"), wave (kyles "waves ocean crash"),
  whoosh (Qvantor "Whoosh Deep Heavy"), hit (Qvantor "Heavy Slam Impact"). Loudest momentary: boom1 -11.2 LUFS (the Light fruit's
  explode1 -11.8); no clip longer than 3.6 s.
- Previews: FxPreview shots `|gcrack:age[:radius]`, `|gbubble:age:radius`, `|gshock:age:reach:strength`, `|gquake:age:radius`,
  `|gtremor:age`, `|gshake:held`, `|gtilt:age`, `|grift:age`, `|gwave:age`, `|gkaishin:age:charge`, `|gslam:age`, first person
  `fp:pitch|gfp:age:pitch:bubble|crack` (tools/preview/GuraShots). Poses: PikaPreview takes gura_ names (compile it with
  DragonPreview, StylePreview, FxPreview and GuraShots).
- Not tested in game: all of it.

## 8s. Done (3.1.2, pushed): the Gura fruit after the owner's first play

- The owner (verbatim): "The fruit is good. The ability moves are good ... The tsunami move. This never happens in the show. Like
  this, it never sends a fucking, never sends a wave straight forward. He smashes both his arms to the side, each one to the left, one
  to the right, and then two waves come from both left and right, slowly growing ... where's the water coming from? ... it should only
  move like if you're like close to water, like from a big body of water. This move should not work like unless you're near water." /
  "the sword, the last breaking effect [the glass breaking effect], it's too large and it's like spawning instantly. Like it should crack
  slowly, and the lines of the crack should be way slimmer. I mean, it should actually be like a glass." / "the entire body is being
  animated, including the legs, which I tell you not to do. And this also happens in a few other like fruits and moves that when you're
  on the floor sneaking and you right click, the entire body just breaks ... Your torso moves forward. Like, it should just cancel the
  sneak animation." / "The shockwave ... never mind. It's good." Then: "The X move, Kabutowari ... should make you jump way higher. It's
  barely anything, and it should only do the cracks around like touches a s[olid] surface. Currently, if you look up ... you're gonna do
  the crack animation on the sky, and you can just keep flying with this move unrealistically" (with a screenshot: looking up, the
  ground cracks, rocks and dust drawn in the sky over the player). Then: "if hockey [Haki] Rio [Ryou] is activated, I think that the
  moves should add lightning from the Conqueror effect to it. Because if you check Blackbeard's attacks, you can see that he apparently
  can use Conquerors mixed with the fruit. So please check the reference of Blackbeard and try to copy what he does."
- **Tsunami** (`GuraMoves.tsunami`, `Wave`, `nearWater`, `sea`): only with `WATER_LEAST` 40 water columns (one every 2 blocks, the first
  non-air block from 4 over the feet to 12 under is water: `FluidState.m_205070_(FluidTags.f_13131_)`, newly proven in
  verify/api-allowlist.txt + srg-shapes; stubs FluidState/Fluid/FluidTags) within `WATER_RADIUS` 24; else an action-bar message and no
  cooldown. gura_tsunami: arms crossed, then smashed out to the sides on tick 4 (a small CRACK at both fists); two TSUNAMI walls
  (`TSUNAMI_LENGTH` 26 along his way, 9 high) rise `TSUNAMI_SIDE` 18 blocks to his left and right, centred `TSUNAMI_AHEAD` 16 ahead,
  over `TSUNAMI_RISE` 24 ticks (S2CUoFx by = hold), then roll inward at 0.6 a tick and meet in the middle (`TSUNAMI_LIFE` 52); the
  right-hand one's crash slams everyone caught or between them (5 damage, knocked down 70). Both server and client sit the wave on the
  water's face (`ClipContext.Fluid.ANY`). `GuraArt.tsunami` gained `hold` (it stays where it rose that long). Cooldown 240.
- **Glass crack** (`GuraArt.crack`): `CRACK_GROW` 12 ticks: each of 11 cracks starts up to 3 ticks late and runs out in 8 little jumps
  (each hop quick, then a pause), side branches off steps 2 and 4, the broken rings close across between cracks as both pass;
  lines `max(0.012, r*0.011)` (were 0.03 / r*0.026) with a faint cyan edge 3.2x at 0.35 alpha; a small flash (r*0.1) for 3 ticks;
  holds to `CRACK_HOLD` 26, shatters by `CRACK_LIFE` 44. Server sizes cut ~40% (tap 0.9, charged 1.2-2.6, shake 1.0, slam 1.2-1.4,
  Gekishin 1.2-2.3).
- **No legs** (`tools/gura_anims.py` has its own `write_parts`): no Gura clip keys a leg, and the body only yaw, roll and bend (never
  x/y/z or pitch: those move the whole model). The tsunami pose is new (arms smashed out to the sides).
- **Sneaking** (`client/fx/StandUp`, new): while `FruitAnimations.clip(player)` is a gura_ or pika_ move, RenderPlayerEvent.Pre (LOWEST)
  clears the model's crouch flag (f_102817_, after PlayerRenderer.setModelProperties set it) and lifts the pose 0.125 (the crouch's
  render drop), put back in Post. Only the look: the player still sneaks.
- **Kabutowari** (`GuraMoves.kabutowari`, `slam`, `struck`): arc `LEAP_HIGH` 8 + 0.25 x distance (+ any climb), 12 to 22 ticks at 1.5
  blocks a tick across; always lands on solid ground: with a target, the ground under the stand in front of them; no target, where the
  look meets a block, or (the sky) as far along the ground's way as the look reaches across (straight up: straight up and down where
  he stands), the ground under it within `LEAP_DROP` 40, else no leap and no cooldown. At arrival the target is struck (the crack over
  the head, 12 damage); the ground (crack, crater, QUAKE, the throw) only once a block is under his feet (`LEAP_WAIT` 30 ticks at most).
- **Conqueror mixed in** (`ConquerorAura.mixed`, kind `MIXED` 84, `ConquerorSparks.burst`, ConquerorSparksClient; refs 28, 55: the
  crackle at Blackbeard's fist, 32, 34: lightning over the struck ground's rings): only with Ryou on and the Conqueror's Haki awakened
  (`ConquerorAura.awakened`, the same rule as every Conqueror spark), a burst of the 3D Conqueror lightning (black, the Ryou-colour
  rim, HakiBolts, silent) at: the charging fist every 6 ticks, Kaishin's crack and its first two craters, the tap, both fists of
  Shima Yurashi, each lurch round the feet, its release (7 blocks, 14 arcs), Kabutowari's head crack and ground, Gekishin's crack behind
  them, the tsunami's fists, the rift's start and every 4 ticks along it, the landing quake (strong ones). Rendered: SparkPreview's new
  "gura burst" row.
- Renders this session: the crack over time, the two walls, the new poses (docs/handoff/gura-fix-3.1.2.jpg).
- Not tested in game.

## 8t. Done (3.1.3, pushed): the Magu Magu no Mi after Akainu

- The owner (verbatim): "Now do the same as you did with the Gurunomi for the Magunomi, the Magma fruit that is already in the game
  but has no abilities as well. Search for Akainu's feats and gifts for reference and make his abilities. Make sure not to animate the
  feet, like the legs, unless it's absolutely needed."
- References: `docs/handoff/magu-ref/` (README: what each of 46 Tenor GIFs shows and which move it informs; sheets/). Tenor needed
  tenor.com, media.tenor.com and media1.tenor.com added to the HexPiece environment (done this session, update_environment).
- Asked (AskUserQuestion): **"Go ahead"** for: LMB the game's punch; RMB Dai Funka (tap: a magma fist shot forward, fire trail, black
  smoke, bursting; hold: the fist swells into a giant black-crusted magma fist, let go: it flies and explodes, burning magma pools);
  Z Meigo (a short dash, the magma fist through the one looked at, magma out of their back, set on fire, knocked down); X Ryusei Kazan
  (hold: a fist punched at the sky, magma fists rain like meteors where he looks while held); C Inugami Guren (a giant magma hound's
  head surging forward along the ground, jaws snapping, mauling and carrying those it bites, bursting into a flood of magma); V
  Bakuretsu Kazan (fists into the ground: the ground round him bubbling magma, geysers launching foes, burning a few seconds); B Magma
  Flow (melted into a stream of magma sliding along the ground where he looks, burning whoever it passes, rising in an eruption).
  Passive: **"Add it"**: immune to fire and lava, bare-handed attackers without Haki set on fire. No clip keys a leg.
- **Server** (`server/MaguKit`, `server/MaguMoves`; registered in HexPiece.init): S2CUoFx kinds 100 to 113 (FIST, BURST (by: the seed of
  the fist or meteor that burst, the clients stop drawing it), POOL, SWELL_ON/OFF, METEOR, SKY, HOUND, ERUPT, GEYSER, FLOW, DASH, PIERCE,
  TOUCH); UoFx routes 100-119 to `client/magu/MaguFx` before the Conqueror's >= 80. Nothing is real lava: burning ground is a list of
  `Pool`s ticked in `maguTick` (1.5 damage and 3 s of fire every 10 ticks, at most 24); blocks only through Craters.
  - RMB Dai Funka: tap (< 6 ticks) `magu_punch`, a fist (0.42, 2.4 a tick, 40 blocks) bursting on the first body or block (6 + blast 4,
    fire 4-5 s, a little pool); held (`magu_charge`, SWELL_ON, full at 40) `magu_release`, a giant fist (0.8 to 2.2, 1.7 a tick): blast 3
    to 8 blocks (6 to 16, fire 6 to 12 s, knock 1.6 to 4), the first body struck 6 to 14 more, an impact frame at full, a crater 1.4 to
    3.4 and burning ground round it. Cooldown 14 / 60 to 140.
  - Z Meigo: aimed 16, a DASH (2.6 a tick, `magu_meigo_wind` then `magu_meigo` a tick before arrival): 13 damage, 10 s fire, thrown,
    an impact frame, PIERCE (the spike of magma out of their back), those behind burned; no one: 8 blocks and a stab. Cooldown 140.
  - X Ryusei Kazan (hold, up to 80): `magu_sky`, SKY (a fist punched up), from tick 8 a METEOR every 4 ticks onto where he looks (8 blocks
    round, a 12-tick fall), each a blast 2.8 (5 damage, fire), a pool. Cooldown 120 + 2 x held.
  - C Inugami Guren: `magu_hound`, on tick 3 the HOUND (1.3 a tick for 24 ticks, scale 2.2; its foot follows the ground): bites burn (6,
    6 s) and carry; at the end a flood (blast 4.5, a pool 3) and those carried thrown down. Cooldown 200.
  - V Bakuretsu Kazan: `magu_erupt`, on tick 4 ERUPT (radius 9, 100 ticks): a GEYSER every 5 ticks for 45 (5 damage, thrown up 1.5,
    fire), everyone on the ground in it burned every 10 ticks. Cooldown 240.
  - B Magma Flow: FLOW up to 18 blocks along the ground (1.6 a tick; the client hides the body and its hand, flies it, draws a stream
    from its last places); whoever it passes burned (6, 6 s); at the end `magu_rise` and an eruption (blast 3.5, thrown up). Cooldown 160.
  - Passive: `MaguKit.immune` = `DamageSource.m_269533_(DamageTypeTags.f_268745_ IS_FIRE)` (fire, lava, magma blocks; both read by the
    original, stubs added, srg-shapes regenerated); fire put out every tick (Entity.m_6060_, LivingEntity.m_20095_, HexGhoul-proven);
    `MaguKit.dodged` (a blow through the Logia body without Haki): a bare-handed attacker within 5 blocks set on fire 4 s, 3 damage, TOUCH.
- **Art** (`magu/MaguArt`; `FireMesh.lump`, new: flat heat (no shading by the view), hot spots where the noise swells, red-brown crust
  where it sinks and along the fine noise's zero lines; an outline or vertex-wide black lines made every band show as rings round each
  lump, so none; at most 80 faces unless a big lump fills the view): `MAGMA` palette (black crust to yellow-white), `fist` (palm, four
  knuckles, thumb, a glow, fire behind, dark smoke), `burst`, `pool` (heart yellow to red edge, black crust rim, bubbles, wisps, cooling),
  `meteor` (one long lump, fire, smoke, streaks), `hound` (skull, long snout, dark mouth, snapping jaw, ears, glowing eyes, fangs, neck),
  `geyser`, `stream` (seven lumps), `pierce`, `swell`. Budget: `DragonTests.maguMagma` per move (Dai Funka at full with its pools,
  Ryusei Kazan's three meteors and bursts and six pools, the hound with the stream and Meigo, Bakuretsu's ground and six geysers) under
  16,000 at every moment. FxPreview shots in `tools/preview/MaguShots` (`|mfist:age:size`, `|mburst`, `|mpool`, `|mmeteor`, `|mhound`,
  `|mgeyser`, `|mstream`, `|mpierce`, `|mswell`, `|mfp`).
- **Animations** (`tools/magu_anims.py`, gura_anims' write_parts: no leg, body yaw/roll/bend only, lint clean): magu_punch, magu_charge,
  magu_release, magu_meigo_wind, magu_meigo, magu_sky, magu_sky_end, magu_hound, magu_erupt, magu_melt, magu_rise. StandUp, PieceHooks,
  StylePoser (MaguPoses) and FirstPersonLight.fist know magu_.
- **Sounds** (`tools/magu_sounds.py`, CC0): bubble loop (Jim-Bretherick "Bubbling lava flowing1", 6 dB down), whoosh (LookIMadeAThing,
  hnhnh), burst (Blankened "Fireball Explosion", ReadeOnly "fire_explosion" 4 dB down: it measured -7.8), boom (misosound "BOOMING
  PUNCHY EXPLOSION"), sizzle (tiagusilva37 "HOT_BALL_LAVA_SHORT", bassimat "Water Poured onto a Burning Fireplace"), roar (magnuswaker
  "Flame Burst").
- Not tested in game.

## 8u. Done (3.1.3, pushed): the Gura fruit with Haki, its shock rings

- The owner (verbatim, while 3.1.3 was being built): "with Hotkey [Haki] activated on the Gouda fruit, Jammer fruit [the Gura fruit], I
  think that it needs a bit more potency when Hotkey is activated. Like every move should have like their range like vastly increased.
  ... Currently I think it's good where the damage is, but I think it needs a bit more visual destruction, more impacts to the moves
  ... The growth root [the Gura fruit] is like insanely strong in the show. And also I found a bug that the impact of the fruit, like the
  the rings around the impact are like opposite to where the impact was" (a screenshot: two white arcs of a punch's shock bowed the wrong
  way up in the sky).
- `GuraMoves.boost`: `HAKI_RANGE` 1.6 with Ryou on (`ConquerorAura.ryou`, new: armamentOn and acocOn, the Conqueror's Haki not needed):
  every reach x 1.6 (the tap's shock, Kaishin's, the shake's radius (State.shakeRadius), Kabutowari's leap, the tsunami's walls (length
  x 1.6, height x 1.4, further ahead; Wave carries its own), Gekishin's reach and shock, the rift's length, the landing quake); more
  destruction: Kaishin up to 9 craters 6 blocks apart, 1.4x wider; Kabutowari's crater, quake and throw x 1.5; the rift 5 wide and 4 deep
  (RIFT bx 1: drawn 1.8x as wide), 3x the blocks; quakes reach further. Damage unchanged.
- The arcs (`GuraArt.shock`): leaning back toward the eye by 0.45 of their radius, a 40-block shock's 21-block ring had its arcs' ends so
  much nearer the eye than their middles that they bowed the other way; now the drawn front is at most 1.5 + 0.16 a block out, the arcs
  lean 0.12 ahead, the rings' rise a little ahead too. Rendered from behind: compact, bowed round the line of the punch.
- Not tested in game.

## 8v. Done (3.1.4, pushed): first-person hands for every fruit move, the Haki orb, the Gura charge sound

- The owner (verbatim): "We need to give them better first-person simple animations. Like, just make them simple for every move of the
  two fruits we made. Quick. Better light. And better quake." Asked which fruits: "All three" (Light, Gura, Magma).
- `style/HandMoves.java` (pure): a short track per kind of move (rows {tick, dx, dy, dz, pitch, yaw, roll} in the hand's view space,
  turned about `PIVOT` {0.45, -0.55, -0.7}, pitch + tips the forearm up, yaw + toward the crosshair): JAB, DRAWN, DEEP, BIG, SIDE,
  SIDE_QUICK, TILT, UP, UP_END, SLAM, DOWN, SWEEP, SWEEP_BACK, POINT, POINT_TAP, RECOIL, POINT_END, PUSH, PUSH_END, THRUST, GATHER_LEG,
  KICK, THROW, COCKED, MELT, RISE; `move(clip)` maps every pika_ move clip and every gura_/magu_ clip (held poses tremble). Walking,
  idle, float, fly, stream and the sword's idle/walk leave the hand alone.
- `client/FirstPersonHand.java`: `RenderHandEvent` (priority HIGH): the MAIN_HAND push transforms the game's own hand's PoseStack (no
  custom arm: 3.0.5's never showed on the owner's phone), OFF_HAND pops it; 2-tick blend between clips (FirstPersonLight.current/started,
  FruitAnimations.clip). `FirstPersonLight.fist` and the fingertip view apply the same move, so effects stay on the drawn fist.
- Every pose keeps the fist on screen and never jumps: StyleTests.hands (the fist of tools/preview/HandPreview, low in the view as the
  game draws it, within 0.66 of the half-height, under 0.2 a block per quarter tick). Renders judged: hand7.png (charge pulls the fist
  back and in, release drives it at the crosshair, slam lifts then drives down, rise lifts the arm). Pitfall: HandPreview's output folder
  held an old HandMoves class that shadowed the build's; delete com/ from it before rendering.
- The orb (the owner, with a screenshot: "This happens with Gur Hakion. The fucking right click gets the little glowing orb above the
  player instead of around the fist"): ConquerorAura.MIXED drew a flash a chest above its point (HakiBolts.sparks' flash), so the charge's
  lightning at the fist put a white orb over the head. MIXED now has no flash, and Kaishin's gathering lightning carries bz = 1: every
  client puts it on the fist it draws (`GuraFx.fist`, now public) instead of the server's guess.
- The charge sound (the owner: "Also want a better charge sound move for the right click. For the right click of Quake. That charge sound
  is horrible. Like it sounds like a car trepidating. Get another one."): the bubble played the earthquake rumble pitched up 1.35-1.6x
  (bursts every ~0.15 s on its spectrogram: the engine rattle). New `piece.gura.charge` (GuraSounds.CHARGE): a loop cut from Qvantor's
  "Magic Portal Hum" (Freesound 880775, CC0), 1.2 s for 3.6 s, 3 dB down, played at pitch 1.0 (Kaishin) / 0.9 (Gekishin), faded out the
  moment the bubble goes. The rumble stays for Shima Yurashi.
- Not tested in game.

## 8w. The owner's requests (verbatim): 3.1.5 done (8x), 3.1.6 done (8y)

**3.1.5 (the Magma fruit, the Light fruit's right click, the Conqueror's lightning for every fruit):**
- Magma C and X: "It's about the Magu no Mi, the lava fruit. I like it. I truly do like what you did with it. The effects are great. The
  right click is perfect. Although I would like you to improve the C move. The I think that's meant to be a hound, but like it does the
  face does not look like a hound at all. I'd like you to better it. The barrel looks like one, and it should go where the player is
  aiming, not directly in front of the player. So, because if I aim down, it's not going down; it's just going like straight ahead of
  me, uh, which is not fun. Also, there's another thing: the X move. It should be. It's you got the right gist of it. You got the right
  gist of it, and I like it. It should be like you hold X, and the more you hold X, the more fireballs you throw at the sky. When you
  let go, that's when they fall. It should be the exact amount of fireballs you fire to the sky fall to the floor. Um, Same design and
  all, just make sure you get the concept right, please."
- Light right click: "Also would argue and would like for the light right click to do the same as the magma hound, like the C move of
  magma. I think the beam should drag entities to the end of it and then explode, like the magma hound does. That was a really good idea
  you had there."
- Every fruit with the Conqueror's Haki: "Okay, I think from now on, I think I got the good picture of it. Every fruit, when Haki is
  activated, no matter what, like, and if the player has Conqueror's Haki, that is. Yeah, let's do it like that. If the player has
  Conqueror's Haki and, like, in Rio activated, every move pretty much releases the exact lightning from the Conqueror's move, but that
  doesn't stun. It's just for visual effect. Since Haki already gives a buff, like a buff in speed, then it's not needed. It just every
  move from every fruit does that." (Gura already mixes it in via ConquerorAura.mixed; do the same for the Light fruit, the Magma fruit,
  the Uo Uo dragon and every other fruit's moves, gated on the player HAVING the Conqueror's Haki and Ryou on; visual only.)

**3.1.6 (the Gura fruit's ultimate on the End key):** the owner's reference GIF is docs/handoff/gura-ref/ultimate-whitebeard-punch.gif
(Whitebeard, the right fist cocked high beside his head in a bright white bubble, thick golden lightning streaming off it). Verbatim:
"Also think that Gora should have a new move on the end key, like the absolute ultimate, which is like the user pulls the fist, like the
right fist, like back, like more to the left, like a little further away from the face, but like around that area. He charges up, and
like this is a charge move. It's not a tap. It's no option to be a tap. You have to charge it fully for it to activate. It's basically
the right click on steroids, if you know what I mean. Um, It charges the bubble around the fist and gets bigger and bigger and bigger and
bigger and bigger, and then it's released. The entire floor in front like bends and breaks into like squares, and like it just acts like
as if the ground is like completely made of fucking rubber. Well, not like that, but you know what I mean. Like it just shatters
everything in front of it. It's a it's a massive like right click, basically, with a bigger, wider range and like in a big cone in front
of the user, causing the floor to like shift and like go up and like break into many fissures and just up and down. Like the cooldown of
this move should be absolutely gigantic. It should be like 5 minutes cooldown. If Haki is activated, I feel like the lightning from
Conqueror, the thick lightning, should like shoot everywhere from like where the floor is breaking, and actually also stun and all, like
just make this an absolutely crazy move."
(The later "doesn't stun" rule is for every move's lightning in general; this move's stun was asked for by name, so keep it here and
tell the owner.)

## 8x. Done (3.1.5, pushed and green): the Conqueror's lightning out of every fruit, the Light beam drags, the Magma hound, Ryusei Kazan, Gear 5

The owner's words are in 8w. Half of this was written by the session before (stopped by the owner mid-work, committed unbuilt); this
session built it, rendered and judged it, finished the hound's look, gave Gear 5 the lightning and released it.
- **The Conqueror's lightning out of every explosion** (POINT ONE, 2): `ConquerorAura.exploded(player, level, at, radius)` (gated on
  `awakened`: the Conqueror's Haki and Ryou on; visual only, no stun; blasts under radius 4 at most one per `BLAST_GAP` 6 ticks a player;
  grounded on the ground under the blast; count 3 to 14, radius 0.8 of the blast's, 1.8 to 9), called at the top of `UoMoves.explode`
  (every Light, Magma and dragon blast, so the Light right click's lightning comes out of the explosion at the beam's end, not the
  beam). Moves with no explosion: `PikaMoves.fx` throws it for KICK (a kick or cut landing) and LASER (the tap laser's end);
  `MaguMoves.fx` for PIERCE (Meigo). Gura already had its own (ConquerorAura.mixed). `forget` clears BLAST_NEXT.
- **The burst's shape** (`haki/ConquerorSparks.burst`, used by every MIXED burst, Gura's too): out of a blast on the ground half the
  arcs went down into the ground unseen and a 9-block blast's arcs were as thin as a fist's. Now the arcs that do not ground go up and
  out (a downward one steeper than -0.2 is mirrored up at 0.8), and widths x max(1, sqrt(radius / 4)). Rendered: SparkPreview's new
  "blast 9" row (radius 7.2, 11 arcs, 8 blocks ahead, seen over the caster's shoulder): a fan of bolts erupting up and out of the blast.
- **Gear 5** (PiecePatch.gearLightning, `GEAR_SERVER` com/hexhaki/devilfruit/gameplay/GearServerController, now in build.py PATCHED):
  the original sends every Gear effect through its private `fx(player, S2CGearFx.Kind, at, size)`; a call to
  `ConquerorAura.gear(player, kind, at, size)` is put at its start (no branch, no frame). `gear` throws `exploded(..., size x 2.4,
  1.8 to 10)` for the kinds in `GEAR_BLASTS` (IMPACT: a blow's impact, AWAKEN_BURST, AWAKEN_LAND: landings and the giant fist's slam,
  COIL_RELEASE, RUBBER_FIELD, STAR_GUN), not BOUNCE (walking and sky bounces) nor its own lightning kinds (THROW_LIGHTNING,
  AWAKEN_LIGHTNING, LIGHTNING_LANCE) nor STAR_TRAIL. Gear's clips already spark off the body through HakiServerController.animate
  (ConquerorAura.moved). Gatling's many impacts are held to one burst per 6 ticks by BLAST_GAP.
- **The Light right click drags** (`PikaMoves.beam`): whoever the beam crosses is hit (6 + 8 x charge, a tiny push) and dragged along it
  to just short of its end (`DRAG_SPEED` 5 blocks a tick at most, all arriving together, `DRAG_MOST` 12 ticks), then the end explodes
  (the 3.0.5 blast, BURST, quake, sound) the moment they arrive (1 tick after when no one was caught).
- **The Magma hound** (C): `MaguArt.hound(sink, x, y, z, fx, fy, fz, scale, age, seed)` a dog's head in its own aimed frame
  (part/point/fang/spike helpers, crust 0.7): skull and cheek ruff, brow ridges, a muzzle of three tapering lumps with a dark nose, the
  lower jaw hinged under the ear snapping 26 to 48 degrees open on a dark gape with a tongue, fangs top and bottom, small mean eyes
  (0.1 and 0.07, yellow) under the brow, **wolf ears** (a lump at the base and a `spike`: a FireMesh.tube cone 0.9 long to a point,
  orange to dark red; this session: the round lump ears read as a mouse's from behind, the caster's view), **three hackles** (low cones)
  along the nape. A ring of mane spikes round the head was tried and dropped (it read as a sea urchin). Renders judged (FxPreview
  through MaguShots `mhound:age[:fy]`): side `e:270:5:10:0:2:11.8`, behind `e:180:12:7:0:2:9`, front `e:0:6:9:0:2:9`, first person
  `fp:0`: a snarling hound from every side, 4.1k to 5.8k quads (docs/handoff/hound-lightning-3.1.5.jpg: the hound, then the blast lightning). Server (`MaguMoves.hound`): starts ahead of the chest (eye - 0.2), flies
  straight along the look (up or down), bursts where its jaws meet a block (`erupt(..., seed)` so clients drop it) or at the end of its
  life; client `MaguFx.Hound` is a record, straight flight, removed on BURST's `ended`. DragonTests' budget passes.
- **Ryusei Kazan** (X): `MaguMoves.rain/rainTick/stopRain/meteor`: while held a fist of magma goes up (SKY) at tick `THROW_FIRST` 3 and
  every `THROW_EVERY` 6 (at most `THROWS_MOST` 15, State.thrown); let go (or at `RAIN_HOLD_MOST` 140) and exactly that many meteors fall
  where he looks, one every `FALL_EVERY` 2 ticks, spread wider the more there are; cooldown 80 + 14 per fist. MaguKit's text updated.
  `tools/magu_anims.py` magu_sky (96 ticks): a punch up at 3 and every 6 ticks, the straight arm swung down to `arm(-110, 0, 24, -8)`
  (forward, a little over level) between. Tried this session: a bent elbow beside the head for the pull (fist cocked by the ear): every
  version fails `anim.lint` ("through the head" while the elbow bends overhead), so the straight pump stays. Rendered (PikaPreview
  `--frames 12 --to 33`, views 0, 20, 90): up on 3, 9, 15..., forward on 6, 12. HandPreview `magu_sky`: the first-person fist thrusts up
  to the crosshair on every throw and drops between (HandMoves PUMP).
- POINT ONE is at the top of this handoff and a RULE in CLAUDE.md (the owner asked every AI to be told).
- Not tested in game: all of it.

## 8y. Done (3.1.6, pushed and green): the Gura ultimate, Island Breaker (on End then; on N since 3.1.7, 8z)

The owner's words are in 8w (verbatim); reference GIF docs/handoff/gura-ref/ultimate-whitebeard-punch.gif. The name "Island Breaker"
is this session's (the owner gave none); say so if they ask.
- **Key** (`client/PieceKeys.ULTIMATE`, "key.hexpiece.ultimate" "Fruit ultimate (hold)", GLFW 269 End; `client/gura/GuraKeys`): in fruit
  mode with the Gura fruit, no screen open, the End key's down and up go to the server through the original's own fruit message
  (`FruitKeys.send(0 or 1, 40, flags)`: C2SFruit -> FruitServer.action -> GuraKit.action -> GuraMoves.press/release with ability 40,
  `GuraUltimate.SLOT`, outside the original's slots 0 to 8). Let go is always sent once pressed (a screen, the mode or the fruit
  changing let it go too). `GuraKit.ULTIMATE` ("[END] hold", cooldown key "gura_ultimate") is in GuraKit.MOVES before the passive;
  `AbilityHud.kit` shows its key as bound in Controls.
- **Server** (`server/GuraUltimate`, GuraMoves.State.ultSince, busy() counts it): press (ready, not busy): `gura_ult_charge`, BUBBLE_ON
  id 3; each tick while held (`GuraUltimate.tick`): the player slowed x0.4, `Impact.quake` every 10 ticks growing, a TREMOR ring every 20,
  the Conqueror's lightning on the fist every 5 (ConquerorAura.mixed atFist, thicker as it grows); at `FULL` 80 a crack at the fist and
  a boom (full). Let go under 80: nothing ("Charge it fully to let it go" on the action bar, `gura_ult_drop`, no cooldown). Let go full
  or held `HOLD_MOST` 220: `release`: cooldown `COOLDOWN` 6000 (5 min), `gura_ult_release`, 3 ticks later `strike`: CRACK 3.2 at the
  fist, SHOCK over the reach, HEAVE (kind 72: the feet's ground, the flat way, CONE, WAVE, reach, a seed), a thick lightning burst at
  the fist, five narrow fissures fanned across the cone (`GuraMoves.Rift` with `narrow`: 1 wide, 2 deep (3 with Ryou), half the blocks,
  no strikes, no lightning or quakes of its own; RIFT bx -1 so clients draw it 0.55 wide; self-repairing through Scars), four craters on
  the middle line as the wave passes (Craters.open 2.4, x1.3 with Ryou; QUAKE radius x1.4), everyone in the cone (CONE + 4 degrees,
  within 12 blocks up or down) struck when the wave reaches them (`WAVE` 2 blocks a tick): 18 damage, thrown out 1.6 + 0.6 ahead and 1.5
  up, knocked down 100 ticks; **with `ConquerorAura.awakened` (the Conqueror's Haki and Ryou on): `ConquerorRelease.hold` 200 ticks (10 s
  stun: slowness, weakness, felled)** plus a burst on each body; an impact frame (S2CClash 3.0) on the first struck; `Front` (a Live):
  with the Haki, two thick bursts (5 to 7 blocks, 9 arcs, thick 2.2) on the ground near the wave front every 2 ticks, crumbling sounds,
  quakes along it. Reach `Heave.REACH` 44, x `HAKI_RANGE` 1.6 with Ryou on.
- **Thick lightning:** `ConquerorAura.mixed(..., thick)` sends thickness in S2CUoFx `ax` (0 = the usual);
  `ConquerorSparksClient` passes it to `ConquerorSparks.burst(radius, count, floor, seed, thick)` (capped 3).
- **The heaving ground** (`gura/Heave`, pure; `client/gura/HeaveDraw`): the ground in the cone is cut into plates on the world's block
  grid (2x2, a 4x4 slab 28% of the time); `BREAK` 0.42 of them break (times (44 / reach)^2, so as many blocks move whatever the reach);
  each, when the wave reaches it (d / WAVE + up to 2 ticks), rises and bounces: lift = amp e^(-u/13) sin(pi u / 8) (amp 0.45 to 1.4,
  less far out), tipped about its own flat axis up to 6 to 22 degrees, faded to rest by `LIFE` 46. HeaveDraw finds each column's top
  solid block once (not under water, no block entity), keeps its light, and each frame draws the plates that show with the world's own
  block models (`BlockRenderDispatcher.renderSingleBlock`, newly proven: verify/api-allowlist.txt 3.1.6 block, stubs
  BlockRenderDispatcher, Minecraft.m_91289_, BufferSource.m_109911_ endBatch; srg-shapes regenerated) lifted and tipped, plus the
  block under when up past half a block; at most `MOST_DRAWN` 420 blocks a frame, nearest first; drawn first in GuraFx.stage (solid),
  then endBatch(), then the see-through effects. The real ground stays (a plate sinking back just shows the ground).
- **Art** (`GuraArt.heave`): a white shock arc on the wave front over the cone, a glow under it, 9 ground cracks through the cone, a thin
  dust wall (8 puffs, alpha 0.4: the first render's dust hid the squares), rocks thrown up. The narrow rifts draw fewer side cracks,
  puffs and rocks (five at once). `DragonTests.guraUltimate`: every see-through piece at once (wave at 70 blocks, crack, shock, five
  fissures, four quakes, the biggest bubble) under 16,000 quads at every moment (about 14,000 at most); the heaving blocks near the
  budget at both reaches; every plate at rest before its wave and after its life, bounded; cooldown 6000 and a full charge only.
- **Bubble** (GuraFx, id 3): radius 0.3 to 1.05 over 80 ticks (eased), first person at most 0.42; its hum the Gura charge loop at
  pitch 0.72 and full volume.
- **Animations** (`tools/gura_anims.py` ultimate(): `COCKED` = rightArm arm(-88, 64, 10, -100) (the upper arm out to the side and back,
  the forearm up, the fist beside the head; pitch -150 or a bent arm over the head fail the lint, through the head), leftArm forward,
  body yaw -24 bend -4): gura_ult_charge (220 ticks, trembling harder to 80), gura_ult_release (24: driven out and down, leaning in),
  gura_ult_drop (8). Lint clean. HandMoves ULT_COCK (the fist raised on the right of the view, trembling 1.6), ULT_RELEASE (driven out
  at the crosshair), ULT_DROP; StyleTests.hands exempts the release and drop's start (they start from the cocked hand).
- **Renders judged** (docs/handoff/gura-ultimate-3.1.6.jpg): FxPreview through GuraShots `|gult:age[:boost]` (the plates as grass-topped
  dirt cubes where HeaveDraw puts the world's blocks) from behind the caster (`e:180:16:6:0:1.8:5`) and from above
  (`e:180:50:34:0:0:22`): the fan of fissures, the arc on the front, broken squares heaving all over the cone, crater rings; PikaPreview
  gura_ult_charge from four sides; FxPreview `|gbubble` at 0.45, 0.8, 1.05; HandPreview the three clips; SparkPreview's new rows
  "blast 9" and "ultimate thick".
- Sounds: only the Gura fruit's existing CC0 recordings (boom, crack, quake, crumble, the charge hum); nothing new, none outlasting it.
- Not tested in game: all of it. Not previewed with the real block models (no Minecraft here): HeaveDraw's look in game is unverified.

## 8z. Done (3.1.7, pushed and green): the ultimate on N, not End

- The owner (verbatim, after 3.1.6): "It doesn't require ultimate key. It's just another extra key for the moves I said. God damn it.
  We're just like adding n to one. That's it." So "the end key" in 8w was the **N key**: one more fruit move key beside Z X C V B (2.7.1:
  "All keys of the mod (but haki) will now be the following Z, X, C, V, B, N, M").
- Why a mapping of its own: the original's nine quick slots are all used (Binds.DEFAULT_KEYS as patched: G K L H Haki, B Z C V X the
  fruit), and N has been free since 2.7.1 moved the original's slot 8 from N to V. `client/PieceKeys.FRUIT_N` ("key.hexpiece.fruit_n",
  "Fruit move N", GLFW 78) and `client/FruitKeyN` (was client/gura/GuraKeys): in fruit mode, when the active fruit's ClientKit has a move
  whose key starts "[N]", N's press and let go go through `FruitKeys.send(0/1, FruitKeyN.ABILITY 40, flags)`; any fruit can now put a move
  on N ("[N]" or "[N] hold") and take ability 40 in its kit's press/release. `GuraKit.ULTIMATE` is "[N] hold"; AbilityHud shows any
  "[N]" move's key as N is bound. `PieceKeys.ULTIMATE` (End) and its lang line are gone. Nothing else changed.
- Not tested in game.

## 8za. The owner's requests still open (verbatim, in this order)

**1. (3.1.8, DONE: 8zb) The Gura ultimate and the Haki lightning size.** With a reference GIF (scratchpad only: Blackbeard's "Tremor Crush!", 68
frames: he shouts it, a huge blue quake bubble with red-white lightning, the air shattering like glass round him, then the ground
pressed down into a huge deep bowl with concentric rings, its rim heaved up, rock spires round it, lightning crackling over it):
"Someone has said the end move being the ultimate of the tremor fruit. Like, it doesn't resemble the GIF. Also, the lightning from the
explosions and all when Haki is activated is not the same as the one in Conqueror's Release. If you check, the one in Conqueror's
Release is way thicker. Like, it's bigger than that one, than the one of the moves for some reason. It should be the same size like as
the one in Conquer. And like, yeah, just adapt the ultimate. Like, keep the same looks and everything. Just make it more destructive.
Like I said, the ground should like subside each other. It should like more than just break apart in fissures. It should like parts of
it should lift, other parts should like go down. Like, it should fucking bend the terrain where it passed. You know what I mean?"

**1b. (3.1.8, DONE: 8zb) The charge on the first-person fist.** Verbatim, with two first-person screenshots (the Gura bubble left of the
middle of the view, the fist at the lower right): "Another bug is that whenever a sphere is like charging, it should always be on the
fist, even in the first person. Currently, it's not. It's never currently standing on the fist." Then, with a screenshot of the Magma
fist swelling beside the hand: "Same is happening with the other fruits, if I'm being honest. They like the sphere is rotating with the
players like look around, and like it should be just in front of the hand at all times, you know what I mean? For every fruit this
should be kind of a thing."

**2. (3.1.9, DONE: 8zc to 8ze) The Hie Hie no Mi (Aokiji's ice fruit).** "Can you do the same as we did with light and magma? Can you create the
ice fruit, the Aokiji one? Design the models, get the references, make sure this one is also equally as smooth. The ice is custom, not
Minecraft ice shit. Like actually freeze them and like blocks of ice." / "Find against GARP and all into that, to just make it as
accurate as possible." References: docs/handoff/hie-ref (README, sheets). The move set proposed (AskUserQuestion; the owner did not
pick an option, they sent the messages above and below instead, which say to do it: "after all this is done, all the ice fruit and
everything is done"): LMB punch; RMB Ice Block: Partisan (tap one spear, hold spears in the air, let go all fly); Z Ice Time (dash,
grab, frozen solid); X Ice Ball (orb thrown, a ball of ice swallows them: Garp's prison); C Ice Block: Pheasant Beak; V Ice Age (a
freezing wave in a wide cone, the ground and sea an ice sheet with ridges, all frozen); B Ice Road + ice fist (the Garp fight); N Ice
Saber; passives: the ice body dodge (shards, reform; bare-handed attackers frostbitten), walking on water (the sea freezing under the
feet, custom ice), no cold damage. Freezing: locked inside a custom faceted ice crystal block, cracking like glass and shattering.
The fruit does not exist in the original: add it to the Fruit enum as PiecePatch.uoFruit adds UO (ordinal 5), its item hie_hie_no_mi
(an icon made like tools/uo_icon.py's), CC0 sounds (tools/web_sounds.py), POINT ONE.

**3. (after the ice fruit, "for the future") three things.** Verbatim: "Also, after all this is done, all the ice fruit and everything
is done, we're going to need 3 things. And those things are going to be for the future. But first, we're going to need a tab in
Creative for only the fruits. We're going to have to evaluate them in rarity, like in the name and everything. It's going to have like
a rank, like between common, uncommon, rare, legendary, and mythical. And then we're going to need to have an energy bar for the
fruits, appearing over the food bar with a custom good-looking bar, like not just a normal bar, you know what I mean? It should be as
creative as the one in Tokyo Ghoul mod and the Hex Kagune mod. I'm not saying copy that one, absolutely not. Just make your own, just be
creative and make it look good." (a Creative tab with only the fruits; each fruit a rarity rank common / uncommon / rare / legendary /
mythical, shown in its name and tooltip; a fruit energy bar over the food bar, an original design, not a copy of HexGhoul's or
HexKagune's.)

## 8zb. Done (3.1.8, pushed and green): the Gura ultimate bends the ground, the release's lightning on every move, charges on the first-person fist

The owner's words are in 8za (1 and 1b).
- **The ground bent** (`gura/Heave.bend`, pure; `server/GuraUltimate.Bend`; `Scars.swap`, new): the owner's reference is Blackbeard's
  "Tremor Crush!" (the ground pressed down into a huge bowl with concentric rings, its rim heaved up). Every column within
  `Heave.BEND_MOST` 56 blocks of the cone (or of the bowl) moves a whole number of blocks: a bowl (length / 2 across, its middle 0.55
  of the way out) sunk 5 to 8 deep (length x 0.15), a ring stepped into it every 5 blocks (cos, amplitude 1), its rim heaved up 3.4
  (gaussian, 3 wide), and in the cone 4x4 slabs on the world's grid lifted 1.2 or 2 (30%) or sunk (26%), half as much inside the bowl;
  clamped -9 to +4. `Bend` lists the columns once (sorted by distance) and as the wave reaches them (`WAVE` 2 blocks a tick) moves them
  within Scars' budget (240 blocks a tick, the rest the next tick): a sunk column tears its top k blocks (and a plant on it) and swaps
  the new top to the old top's block (the grass goes down with it); a lifted one swaps its top to the block under it and builds up
  under-blocks with the top on them. Skipped: water (a fluid on the way down), leaves (trees stand), sand and gravel for lifting
  (FallingBlock), block entities, a body in the way of a lift; only with mobGriefing. Everything back after `BEND_BACK` 1200 ticks (+40).
  `Scars.swap(owner, pos, to, supports, restoreIn)`: as `tear`'s checks (protection, claims, the BreakEvent, paintings' walls), records
  the original and the block put (`swapped`), restores the original only while that block is still there. LeavesBlock stub (proven by
  verify/hexghoul-allowlist.txt). The fissures, craters, heaving slabs (HeaveDraw) and everything else of 8y stay.
  Renders judged (FxPreview through GuraShots `|gbend:age[:boost]`, the columns as grass-topped dirt 10 blocks over the scene's ground):
  the first try's 2x2 squares at random made a checkerboard that hid the bowl; with 4x4 slabs, half as many in the bowl, a deeper bowl
  and a higher rim it reads as a stepped crater with concentric rings and a heaved rim (docs/handoff/gura-bend-lightning-3.1.8.jpg).
- **The Haki lightning on every move is the release's** (`ConquerorStorm.blast(radius, count, floor, seed, size)`, used by
  `client/haki/ConquerorSparksClient` for every MIXED burst: ConquerorAura.mixed/exploded, so every fruit and Gear 5): the release's own
  `bolt()` (now parametrised by chest height, start and run length) out of the blast point: 1 to 6 bolts (count / 3), 8 to 40 blocks
  (2.5 x the radius), two in three sent up out of the ground, those aimed down striking the ground under the blast; width the release's
  near a player (`size x 0.4`: about 1.4 blocks, the release's 3.6 is reached 30 blocks out of a 90-block bolt), kinked twice as often
  for their length, branching more on short ones; they snap in about 11 ticks (0.42 of the release's burn). `size` 1, 1.35 for the
  ultimate's (`ax` >= 2), 0.65 for the crackle round a charging fist (`bz`). The old thin `ConquerorSparks.burst` is unused by the game
  now (SparkPreview's "gura burst" row draws the new one). Budget: the biggest blast is under 1,600 quads (DragonTests); the ultimate's
  `Front` throws one burst every 3 ticks (was 2 every 2). SparkPreview rows "blast 9", "ultimate thick" and "release" (the release
  itself at the same distance) judged side by side.
- **Charges on the first-person fist** (`client/HandPass`, new): the cause: one's own first-person charge was drawn in the world at the
  fist's place in the hand's view space turned into the world, but the game draws its hand in its own pass with its own field of view
  (70) and bobbing, so at any other FOV it landed elsewhere and swung with the look. Now `GuraFx.handPass` (the bubble), `MaguFx.handPass`
  (the swelling fist) and `PikaFx.handPass` (the laser's gathering light) draw one's own in first person in the hand's pass: the main
  hand's pose is taken at RenderHandEvent MAIN_HAND (LOWEST, after FirstPersonHand's move), and at OFF_HAND the pending batch (the arm)
  is ended and the charge drawn through that pose at `LightArt.firstPersonFingertip` (debugQuads, then ended). The world pass skips one's
  own in first person. Not previewable here (the preview has one projection for everything); not tested in game.
- Not tested in game: all of it.

## 8zc. The plan (3.1.9, the Hie Hie no Mi; built in 3.1.9: 8ze; the freezing and the ground changed at the owner's word: 8zd)

The owner's words are in 8za (item 2), the references in docs/handoff/hie-ref (README: what each of 82 Tenor GIFs shows and which move
it informs; sheets/). The owner did not pick an option on the proposed move set; their next messages assumed it would be built ("after
all this is done, all the ice fruit and everything is done"), so build the proposed set and all three passives, and say so.

**Done in the code (unbuilt):**
- `patch/PiecePatch.uoFruit` now adds two fruits to the original's enum: UO (ordinal 4, as before) and **HIE (ordinal 5)**: `HIE` = {"hie",
  "Hie Hie no Mi", "Ice", "9FE6FF", lore}, **logia true** (the constructor's flag; PIKA and MAGU are Logia), so the original registers its
  item `hie_hie_no_mi` in the fruits' creative tab, eats, saves and binds it, and its Logia dodge calls the kit's `dodged` (as MaguKit's).
  `$values` now holds 4 + 2. Not built: check the patch (IntInsnNode BIPUSH for the ordinals) and that the item gets a model.
- Sounds (`tools/hie_sounds.py`, CC0, SOURCES.txt, written to src/main/resources/assets/hexhaki/sounds/piece/hie/): freeze1 (Qvantor "Ice
  Spell Freeze" 880760), freeze2 (lori.mortimer "Freeze" 723875), form1 (Vrymaa "Ice - Fracture, roots or growing plants" 735401), form2
  and crack2 (Sanderboah "Ice / Frost cracking" 709137), crack1 (j_p_higgins "Ice Crack 1" 262635), shatter1 (InMotionAudio
  "ICEBrk_Break04" 719973), shatter2 (davilca "Break Ice" 677230), hit1 (JustInvoke "Chill Hit" 138484), age1 (craigsmith "Ice avalanche
  sliding & breaking up" 675734), frost (qubodup "Subzero Frost Loop" 844261, a loop). Integrated loudness -15 to -25 LUFS (the Gura crack
  -20.8, the Magma burst -21.5), the frost loop -30. Two candidates failed to download (InMotionAudio 719169, Magdaadga 420392): unused.
- `hie/HieSounds` (FREEZE, FORM, CRACK, SHATTER, HIT, AGE, FROST; EVENTS), registered in `PieceSounds`; sounds.json `piece.hie.*`;
  en_us.json: the subtitles and `item.hexhaki.hie_hie_no_mi` "Hie Hie no Mi".

**Left to build (the plan, following the Magu fruit file by file: MaguKit, MaguMoves, MaguArt, MaguFx, MaguClient, magu_anims.py,
MaguShots):**
1. Item: `models/item/hie_hie_no_mi.json` (as uo_uo_no_mi.json) and `textures/item/hie_hie_no_mi.png` (an icon made by a script, as
   tools/uo_icon.py: a pale blue-white fruit with swirls, like the series' Hie Hie no Mi).
2. `server/HieKit` (FruitKit: moves list, action kinds 0 key down, 1 up, 2 attack, 3 use down, 4 up; `dodged`; `immune` to freezing
   (DamageTypes FREEZE) for the cold passive) registered in `HexPiece.init` (`Fruits.register(Fruit.HIE, HieKit::new)`); `client/hie/
   HieClient` put in `ClientKits.KITS` on the first client tick (as GuraFx/MaguFx do).
3. The moves (`server/HieMoves`), keys as every fruit (Z X C V B, N since 3.1.7 via client/FruitKeyN: a move whose key starts "[N]",
   ability 40): LMB the game's punch (as PikaFx.punch / GuraFx.punch give it back); RMB **Ice Block: Partisan** (tap: one spear of ice at
   where he looks; hold: spears form in the air round him, one every few ticks up to 8, let go: they all fly at the aimed point; each hit
   crusts the target in ice and slows it, three freeze it solid; ref 62); Z **Ice Time** (a short dash, he takes hold of the one he looks
   at, they freeze solid at once; refs 47, 51, 77, 78); X **Ice Ball** (an orb gathered between the hands, thrown; where it lands a huge
   ball of ice swallows everyone near, frozen inside it, then it cracks like glass and shatters; refs 76, 09: Garp's prison); C **Ice
   Block: Pheasant Beak** (a giant pheasant of ice flies where he aims, freezing what it touches, bursting into ice spikes at the end:
   the Conqueror's lightning out of that burst); V **Ice Age** (palm to the ground: a freezing wave rolls out in a wide cone, the ground
   and the sea an ice sheet with jagged ridges, everyone in it frozen; refs 32, 42, 45, 48, 04); B **Ice Road + ice fist** (the Garp
   fight: a road of jagged ice shoots ahead along the ground, he slides along it (the player's own client flies it, as GuraFx.Move) and
   drives an ice-coated fist into the one he looks at; refs 01, 15, 38); N **Ice Saber** (a sword of ice in the hand for a while, its cuts
   freeze). Passives: the ice body (Logia: blows without Haki pass through, the body shatters into ice shards and reforms; a bare-handed
   attacker frostbitten), walking on water (the sea freezing under his feet as custom ice, melting back behind him; ref 80), no cold
   damage.
4. **Freezing** (the core; the owner: "The ice is custom, not Minecraft ice shit. Like actually freeze them and like blocks of ice"): a
   frozen body is held where it is (velocity zero every tick, slowness and weakness as ConquerorRelease.hold gives, no jump), encased in
   a custom ice crystal block drawn round its bounding box (faceted, cyan to white, white edge lines, frost mist), cracking like glass
   near the end (GuraArt.crack's style on its faces) and shattering into shards with damage. Never real ice blocks.
5. Network: S2CUoFx kinds **120 to 139** for the ice fruit; `client/dragon/UoFx.accept` routes them to `client/hie/HieFx` before the
   Conqueror's >= 80 (as 100-119 go to MaguFx). POINT ONE: `ConquerorAura.exploded` at every burst (Pheasant Beak's end, Ice Ball, Ice
   Age), `ConquerorAura.mixed` at strikes.
6. Art (`hie/HieArt`, pure, eye-centred, previewable; FireMesh-style banded flat colours, alpha-blended, depth-tested, never additive):
   the look from the references (hie-ref README "What they agree on"): faceted crystal, cel-shaded cyan to white, hard flat faces, white
   edge lines, bright highlights; the freeze block, a spear, the orb and the ice ball, the pheasant (body, wings spread, long tail, beak,
   built from crystal shards), the ice sheet with white crack lines and jagged ridges, the road with spikes along both sides, the saber,
   frost mist, shards. Previews: `tools/preview/HieShots` (as MaguShots, wired into FxPreview), every effect rendered and judged; a
   budget test in DragonTests (under 16,000 quads per move).
7. Animations `tools/hie_anims.py` (gura_anims' write_parts: no leg, no head, body yaw/roll/bend only; lint clean), `client/npc/HiePoses`
   (generated) read by `StylePoser`; add "hie_" to `PieceHooks.fruitClip` and `firstPersonArms`, `client/fx/StandUp`,
   `FirstPersonLight.fist` (the gura_/magu_ prefix check), `style/HandMoves.move` (a first-person hand move per clip), `client/HandPass`
   (a charge on the fist: the Partisan's and the Ice Ball's gathering).
8. Bump to 3.1.9, `.github/release-notes-3.1.9.md`, handoff, push, both runs green.

## 8zd. 3.1.9 session 2: the owner on the first ice renders; real ice blocks

- Built and fixed: the unbuilt start of 8zc now builds (SwordTests listed the new piece.hie.* sounds); the item's icon
  (tools/hie_icon.py, textures/item/hie_hie_no_mi.png, models/item/hie_hie_no_mi.json, build.py NEW_PREFIXES); S2CUoFx reads its kind
  unsigned (the ice kinds run to 139); UoFx routes 120-139 to client/hie/HieFx; HexPiece.init registers HieKit; PieceHooks, StandUp,
  FirstPersonLight.fist, StylePoser and HandPass know hie_; server/HieKit, server/HieMoves (every move), server/HieFreeze (held bodies,
  the chill: three cold hits freeze); hie/HieArt; tools/preview/HieShots (FxPreview `|hblock`, `|hball`, `|hspears`, ...).
- The owner, verbatim, on the first renders (every piece of ice drawn as pointed hexagonal crystals: the freeze block a crystal pillar,
  spikes everywhere on the ground, the pheasant and spears of crystal blades): "It is very clear that that ice does not look good, bro.
  Look at They're just like crystals. I mean, all of them are just crystals. Looks weird. Like what? The ground can also turn into like
  actual ice blocks, bro. I'm not stopping you from using Minecraft stuff. It's just God, it looks like shit. What the fuck is that?"
- Asked (AskUserQuestion), the owner chose: frozen body = **"Real ice blocks"** (real Minecraft ice blocks placed round the body,
  see-through, holding it, broken with the game's ice-breaking particles and sound, the world put back); Ice Age and the Ice Road =
  **"Real ice ground"** (the ground's top blocks turn to ice and packed ice, the sea freezes into ice you can walk on, ridges of packed
  ice, everything back after about 20 s); the spears, pheasant, orb, saber and bursts = **"Smooth glassy ice"** (their shapes kept,
  drawn smooth and chunky like the game's ice texture: pale, see-through, soft white highlights, far fewer pointed spikes).
  So 8zc's "never Minecraft ice" is overruled by the owner for the ground and the frozen bodies: never bring the crystal look back.
- Proven for the blocks (verify/api-allowlist.txt 3.1.9): Blocks ICE f_50126_, PACKED_ICE f_50354_, FROSTED_ICE f_50449_, BLUE_ICE
  f_50568_ (joined.tsrg cpo dO/iC/kI/mW joined with FabricMC intermediary 1.20.1: field_10295/10225/10110/10384); DamageTypeTags
  IS_FREEZING f_268419_ (the no-cold passive). A frozen body's suffocation inside its ice is cancelled by its message id "inWall".

## 8ze. Done (3.1.9, pushed): the Hie Hie no Mi as built

Read 8zc (the plan) and 8zd (the owner's answers) first. What the code does (nothing here was tested in game):
- **Moves** (`server/HieMoves`; kinds 120 to 139 in S2CUoFx, decoded unsigned since 3.1.9; `client/dragon/UoFx` routes them to
  `client/hie/HieFx`): RMB Partisan (tap: one spear from the fist, 6 damage; hold: a spear FORMed every 5 ticks from tick 4, up to 8,
  in `HieArt.slot(k)` round him; let go: each flies from its slot at the aim one tick apart, 4 damage, `HieFreeze.chill`; cooldown 14 /
  40 + 8 a spear); Z Ice Time (aimed 12, a SLIDE at 2.4 a tick, `hie_grab_reach` then `hie_grab`, frozen 100 ticks, 5 damage; no one:
  whoever is within 2.2 of the hand; cooldown 160); X Ice Ball (`hie_ball_gather` 10 ticks with the orb on the fist, then
  `hie_ball_throw`, the ORB at 1.6 a tick for 36 blocks, the BALL 2.6 round sat on the ground, everyone in it frozen 80 ticks (ball:
  no own ice), real ice blocks filling the sphere's air but never round anyone else, the game's cracks over the last 24 ticks, broken
  with the game's effect at 14 of them and the Conqueror's lightning out of the break; cooldown 220); C Pheasant Beak (on tick 3 the
  PHEASANT, scale 2.4, 1.6 a tick for 30 ticks, freezing what it passes 60 ticks, 7 damage; bursts where the beak meets a block or at
  its end: UoMoves.explode 4.5 (the lightning), everyone in it frozen 50; cooldown 180); V Ice Age (palm down on tick 5: the SHEET, a
  cone 26 blocks, 50 degrees each side, the front at `HieArt.SHEET_SPEED` 1.5 a tick: the ground's top blocks swapped to packed ice,
  still water to packed ice, ridges of packed ice tipped with blue ice along 5 lines and here and there, put back after
  `GROUND_BACK` 400 ticks; everyone the front reaches frozen 80 ticks, 6 damage, lightning at two a tick; cooldown 260); B Ice Road
  (aimed 22: the ROAD of packed ice 3 wide along the ground with 1-2 blocks of ice by both sides every other block, grown as he SLIDEs
  along it at 1.8 a tick with the fist COATed; on arrival 12 damage, an impact frame, chill 2, UoMoves.explode 2.4; cooldown 200);
  N Ice Saber (`FruitKeyN` ability 40: SABER_ON for 400 ticks, the attack button (the original sends kind 2) cuts every 9 ticks,
  `hie_saber_cut_1`/`_2` by turns, a cone 4.2 blocks 75 degrees, 6 damage, chill 1, a SLASH crescent; N again or time up: it shatters,
  cooldown 200). Any other move puts a drawn saber away first.
- **Freezing** (`server/HieFreeze`): held where it froze (no gravity, put back past 0.12, slowness 9, Clashes.stagger, its attacks
  cancelled, its "inWall" damage cancelled), real ICE in every free block its box takes (at most 12, through `Scars.swap`: claims,
  protection and mobGriefing obeyed, put back if the server stops); the game's cracks (`Level.destroyBlockProgress`, breaker ids from
  0x50000000) over the last 24 ticks; at the end or past 14 damage taken (shattered on the next tick, never inside the blow) the ice
  (or the water it melted into) goes back to air with the game's break effect (levelEvent 2001) at up to 6 blocks, 4 damage, the
  glassy SHATTER drawn. FREEZE's bz says whether real ice went in: if not, every client draws the game's own ice block models there
  (`HieFx.drawIce`, BlockRenderDispatcher as HeaveDraw). Chill: three cold hits within 100 ticks freeze 60 ticks; each one slows.
- **Scars** gained `swap(..., water)`: a still water source may be swapped (the sea frozen) and is put back as water.
- **Passives**: `HieKit.dodged` (SCATTER drawn, a bare-handed attacker within 5 frostbitten: 3 damage, chill); walking on water
  (`HieMoves.walkOnWater`, every other tick: still water under the feet, or up to 1.6 under them, turned to the game's frosted ice in a
  disc of radius 2 (the Frost Walker's way), put back as water after 60 to 90 ticks; not while sneaking or riding); `HieKit.immune`:
  `DamageTypeTags.IS_FREEZING`.
- **Drawn** (`hie/HieArt`, "Smooth glassy ice"): `lathe` (a profile turned round an axis) and `lump` (a rounded icosphere) coloured
  like glass per vertex (`glass`: pale cyan and clear facing the eye, white and solid at the rim, a soft highlight to the light); spears
  (a leaf head, flat 0.45), the orb and its gathering, the pheasant (body, head, beak, crest, five feathers a wing, three tail
  plumes), bursts (a lump swelling then chunks), shatter, coat, saber (in the grip frame with a fixed view), slash, scatter, mist
  (two soft white shells), mistRow (Ice Age's front, the road's end), trail. The crystal look of the first renders is gone for good.
  Budget: `DragonTests.hieIce` (eight spears round the eye and eight flying with bursts, the pheasant and its big burst, the ball's
  break with the orb and mist, the saber, fist and scatter) under 16,000 quads at every moment.
- **Client** (`client/hie/HieFx`, `HieClient` in ClientKits on the first tick; the saber drawn in the hand by `renderBody` /
  `firstPerson` through LightArt's grips; the orb and the iced fist on the posed fist, in first person through `HandPass`); the frost
  loop (LoopSound) only while the spears form or the orb gathers; the punch given back unless the saber is out.
- **Animations** (`tools/hie_anims.py`, 14 clips, no leg, no head, lint clean, `client/npc/HiePoses`), first-person hand moves in
  `style/HandMoves` (StyleTests.hands checks them). Renders judged: docs/handoff/hie-glass-3.1.9.jpg (the glassy ice; the poses from the
  front three-quarter). FxPreview shots in `tools/preview/HieShots` (`|hspears:age`, `|hflight`, `|horb`, `|hgather`, `|hpheasant`,
  `|hburst:age:r`, `|hshatter`, `|hcoat`, `|hsaber`, `|hslash`, `|hscatter`, `|hmist`). The real blocks cannot be previewed here.
- Proven this session (verify/api-allowlist.txt 3.1.9): Blocks WATER f_49990_, ICE, PACKED_ICE, FROSTED_ICE, BLUE_ICE (joined.tsrg cpo
  joined with FabricMC intermediary 1.20.1 from GitHub raw), DamageTypeTags IS_FREEZING, Level.destroyBlockProgress m_6801_
  (method_8517); srg-shapes regenerated.

## 8zf. Done (3.2.0, pushed): 8za item 3, the fruits' Creative tab, rarity ranks, the energy bar

- Asked (AskUserQuestion, 3.1.9's session), the owner chose: ranks = **"My proposal"**: Mythical: the Nika fruit (Gear 5) and the Uo Uo
  no Mi; Legendary: Gura Gura, Pika Pika, Magu Magu, Hie Hie; Common / Uncommon / Rare kept for future fruits. The energy bar =
  **"Moves spend it"**: every fruit move spends energy (big moves and ultimates more), it refills over time, a move that cannot be
  afforded does not go off; the bar over the food bar shows it (an original design, not HexGhoul's or HexKagune's).
- Found: the original puts the Nika fruit (registry/ModItems.NIKA_FRUIT) and its fruits (fruit/Fruits.ITEM, Fruits.creative) into
  vanilla Creative tabs; HexGhoul's list proves CreativeModeTab.builder/title/icon/displayItems/build, ItemTooltipEvent and the GUI
  overlay events.
- 3.1.9's runs: Build HexPiece #151 and Release HexPiece #109 green.
- **Tab** (`PieceItems.FRUIT_TAB`, "Devil Fruits"): every `Fruits.ITEM` fruit by rank (FruitRanks.Rank order), then
  `ModItems.NIKA_FRUIT`. The original's own adding of them to a vanilla tab is left as it is.
- **Ranks** (`FruitRanks`, pure: COMMON §f, UNCOMMON §a, RARE §9, LEGENDARY §6, MYTHICAL §d, 1 to 5 stars; `of(fruit)`, `NIKA`): the
  names in en_us.json carry the rank's colour code (the original's pika/magu/gura/nika_fruit names overridden: build.py RENAMED[LANG]);
  `client/FruitTooltip` puts "★★★★★ Mythical" (stars, then the name, in its colour) under the name (ItemTooltipEvent, index 1).
- **Energy** (`server/FruitEnergy`): 0 to 100 a player, in memory (full after a relog); refills 0.16 a tick after 20 ticks' rest;
  `afford(player, move)` in every kit's `ready` (Gura, Hie, Magu, Pika, Uo: false says "Not enough fruit energy" on the action bar and
  flashes the bar, at most every 10 ticks), `spend(player, move, cooldown)` in every kit's `cool` (the Ice Saber's when it forms);
  `COSTS` per cooldown key (HudTests.ranks: every move with a cooldown listed), a "[RMB]" move's tap (cooldown <= 20) 35%; creative and
  spectator never spend. Not Gear 5 (the original's code): say so if the owner asks. `network/S2CEnergy` (message 19, protocol 13):
  the tenths and a refused flag, sent when the whole number changes.
- **Bar** (`ui/EnergyBarView`, pure, UiPreview "energy"; `client/EnergyBar`, overlay "hexpiece_fruit_energy" above all; one colour
  since 3.2.1, 8zg): 81 x 7 GUI px,
  slanted 3, its right end (an 11 px swirled fruit emblem with a stem and leaf) at the food row's right end, 49 px over the screen's
  bottom (59 in water); the fill in the fruit's colour (Fruit.colour), a light band on top and a dark one under, swirl stripes sliding,
  notches every fifth (3.2.0 coloured it by the fruit: replaced by one colour in 3.2.1), the chip (what was just spent) white for 10 ticks then draining, blinking under 25%, a refused move's red frame
  and 1 px shake for 12 ticks. Shown only with a fruit (not the Nika fruit), not in creative or spectator. Render judged:
  docs/handoff/energy-bar-3.2.0.png (full, a move just spent, low, refused). HudTests.energy keeps every pixel in its place.

## 8zg. Done (3.2.1): one universal energy bar

- The owner (verbatim, while 3.2.0 was being checked): "I think the energy bars should be universal. You shouldn't be able like, you
  shouldn't be creating new ones for each fruit, bro. That's just we're gonna have way more fruits and, like, we're not gonna bother
  adding a custom bar for every single one of them. Just turn make them one color and call it a day."
- `EnergyBarView.COLOUR` 0xA46BFF (violet: no vanilla bar is violet) for every fruit; client/EnergyBar no longer reads Fruit.colour.
  UiPreview "energy" renders the one bar in its four states (docs/handoff/energy-bar-3.2.1.png). Never give a fruit its own bar colour.

## 8zh. The owner's requests after 3.2.1 (verbatim, in the order sent; 3.2.2 works on them)

1. "Ice World is lasting way too long, and it's meant to freeze everyone, like, in there. There's a sound that you added that sounds like
   a fucking fairy, bro. It's one of the sounds when you click Z, one of them. Like, only a few sound like actual ice, the other ones
   don't. It's too melodic, bro. Like, I don't know why you keep making sounds so melodic." ("Ice World" = Ice Age, V.)
2. "Also, the ice sometimes melts. Ice world is like the world itself. The ice lasts way too long, and I don't think it's disappearing.
   Um, if it is, it's taking a bit too long. Plus, like I said, nothing is really being frozen. Like anything that comes in there
   should freeze. I don't like ice time. I think like the entity should be like in a crystal like you had before that then shatters.
   Like I truly don't like ice time where it's like meant to be one of the most satisfying abilities."
3. "Also, I think that we could implement a feature that is pretty cool. That just like imagine whenever an entity punches you that
   doesn't have hotkey [Haki] on, their limbs that they hit with like turn blue, like the skin. Let's say it's recolored to blue, and
   then the player like I don't know can shatter them. I guess I don't know. Like we could implement something like this. Just an
   idea. Or like they just freeze in place, like if they hit the player repeatedly. I don't know, something like that."
4. "Shows up to me while in fruit mode whenever you left-click someone. You should be able to, like, freeze them slowly, and when we
   punch them a certain number of times, they get completely frozen, and when you hit them again, you can shatter them. I think that
   would be a cool mechanic in general. Like, there's so many possibilities with this fruit. It just feels very, very bare. Very. It
   looks good, but it just looks very. It's missing out a few stuff, like quite a lot of stuff."
5. (The Gura ultimate) "Also, the end move of Goro no Mi [Gura], you got it right. It's what I wanted to do with the terrain. That's it.
   But in a cone, like a cone forming this, like slowly forming, going forward, like pushing forward. You got the right idea, like I
   said, but you made a sphere, like a circle. That's not very accurate. And also, the ground should fix itself to how it was, exactly
   how it was, and return any item that is broken. Like oh, it self repairs. Okay, never mind. I think it's fine. Just do what I said
   though."
6. "Another thing is that the lightning, whenever you like I told you to do, whenever Ryohaki [Ryou Haki] is activated, the lightning
   coming out of the out of the explosion or like anything at all, the lightning is too thick. If you compare the two lightnings of the
   Conqueror's release and the one you just added, the one you added is ridiculously fat compared to the other one." (Two screenshots:
   the release's thin black bolts with a pale rim and fine branches, beside fat black tubes from the move lightning; a close-up of a
   huge fat tube crossed out in red.)
7. "Another thing that is bothering me a lot lately is that cracks form in the midair. Like if you use the move midair, no cracks or
   puddles or anything like lava stuff. None of that should appear midair. I mean, it doesn't make sense and looks bad if you ask me."
   (Screenshot: the player in the air, arms thrown out (Shima Yurashi), the dark ground cracks spread round him at his own height in the
   air, grey dust puffs in the air, two white glass crack webs at his fists, shock rings in the sky.)
8. (The Nika fruit, Gear 5) "Next, I also want you to better the Nika animations and adapt the controls to what we have now. All the
   moves there that were like before, they're all not adapted to our current situation. The animations are old and outdated, and I
   don't like it. You gotta better them. Badgerangan, Badgerangun [Bajrang Gun], that move, um, it's just the animation needs work. It
   needs work to be more realistic and match our current mod. Transformation should be in Z. The music is lasting for far too long. It
   should be like way faster. The stretchy moves like M1s and whatsoever, they should all be used in the normal like non-transformed
   state. Like Nika needs a ton of work. This is going to require some elbow grease, and this is going to be really, we have to be
   really careful because the fruit is beautiful, the transformation is beautiful, the clouds are beautiful, the move ideas are all
   there. It's perfectly working. We need to work on it. We need to change the color of the HUD to be white instead of yellow. We need
   to better the hotkey [Haki] versions. We need to better the animation most of all. The first person is fine for the most part. I
   don't think there's any issue with it. So we don't have to be touching that. We just need to better the outside animations. Like
   rotate the character when need be, like in midair, like Badgerangun, like rotate it mid-air to like, actually aim down instead of
   just standing still in the arm, just doing all the work. Rubber world, we need the world to actually physically bounce like rubber.
   This is going to be the most challenging thing in the mod. We need to actually make the world behave like rubber, like the ground
   itself be manipulated so that it behaves like it's made of like pure slime, sort of, as in like actually wave and all, like bend to
   the user's will. Yeah, that's pretty much it. I don't think we're going to be able to finish in this session. So all these pictures,
   all that I told you to do recently, they should all be written in the next handoff and prompt when I tell you to. I'm gonna wait till
   you reach 93% usage, then I'm gonna ask you for it."
   (So: the Nika fruit's moves keyed like every fruit (transformation on Z), its third-person animations reworked (Bajrang Gun turned in
   midair to aim down, the whole body in it), the transformation music much shorter, the stretchy moves and M1s usable untransformed,
   the HUD white not yellow, the Haki versions better, Rubber World bending the real ground like slime in waves; first person left
   alone. Careful: the owner loves the fruit, the transformation, the clouds and the move ideas. The owner will ask for the handoff and
   prompt at 93% usage.)
- Session note: this session started on Default (freesound.org refused); the personal HexPiece environment is now
  env_01PmnEXrwDhxzij8M5q6P6bL (switch_environment to it).
- Found: the melodic clips by their spectra (tools: spectral flatness and the share of the 8 strongest bins): freeze1 ("Ice Spell
  Freeze": sustained chime lines, the fairy one, played by Ice Time's freeze), freeze2, form1, form2, hit1 and age1 carry pitched bands;
  crack1, crack2, shatter1, shatter2 and the frost loop are broadband (real ice).

## 8zi. 3.2.2 IN PROGRESS: what is done (committed, unbuilt) and what is left, file by file

**Done:**
- **The sounds** (8zh 1): every Ice fruit clip is now a real ice recording that is broadband noise (no pitched lines). tools/hie_sounds.py
  rewritten (SOUNDS, CLIPS, level()), clips regenerated, SOURCES.txt credits them: freeze1 Mooe "ice_cracking_01" (169897, from 32.5 s),
  freeze2 karlis.stigis "Ice Cracks" (168712, 4.35 s), form1 PhilllChabbb "Slow Cracking Ice" (268862, 24.4 s, -5 dB), form2 northern87
  "Ice cracking" (369982, 54.6 s, compressed louder), hit1 JackMenhorn "Ice Break Crunch Crack Foot 01" (213578, 85.45 s), crack2
  wwstudioswastaken "ice_cracking_01" (624163), age1 PhilllChabbb (9.7 s, 3.6 s, -4 dB); crack1, shatter1, shatter2 and the frost loop
  kept. Measured: spectral flatness 0.33 to 0.69 (shatter2 0.10 only from its silences; no pitched lines on its spectrogram), the 8
  strongest bins' share 0.02 to 0.12, loudness -18.7 to -22.4 LUFS integrated (frost -30). The old freeze1 ("Ice Spell Freeze") was the
  "fairy" (sustained chime lines), freeze2/form1/form2/hit1/age1 had pitched bands. The tonality check (the scratchpad's tonal.py:
  flatness and the share of the 8 strongest FFT bins, energy-weighted) is worth keeping for every new sound: refuse a clip under
  flatness 0.3 or over 0.1 peak share.
- **The crystal again** (8zh 2): hie/HieArt gained a crystal section (the first renders' cut crystal: ICE palette, facet, line, KB
  scratch, crystal, face4, face3, crackLine, CRACK_TIME, `block` (a body frozen in crystal, cracking over its last 24 ticks),
  `crystalShatter` (CRYSTAL_SHATTER_LIFE 32: flash, glow, a ring of shards, 18-24 crystal shards spinning out and falling, a frost ring,
  mist), `frost` (the frost a punch leaves: a thin blue skin of crystal creeping up the body with its fraction 0..1, crystals crusting
  it, mist), `prison` (the Ice Ball's faceted crystal ball, each edge drawn once, spikes, cracks). Everything else in HieArt stays the
  smooth glassy ice. Rendered and judged through tools/preview/HieShots (`|hfrost:age:fraction`, `|hblock:age[:life]`,
  `|hcshatter:age`, `|hprison:age[:life]`): the frost reads as the body turning blue and crusting at 0.25/0.5/0.75, the crystal block
  as before, the shatter a satisfying burst, the prison a crystal ball with the body inside. Not yet used by the game (below).

**Left, in this order (then version 3.2.2, release notes, handoff, push, both runs green):**
1. `server/HieFreeze`: no real ICE any more (it melts near light into water and, when broken, leaves water: the owner's "the ice
   sometimes melts"): remove `encase`, `breakIce`, the crack ids and the destroyBlockProgress calls; FREEZE's bz always 0 (every client
   draws HieArt.block; ball holds draw nothing of their own). The shatter on thaw sends THAW shattered as now (client: crystalShatter).
   **Frost** (8zh 3 and 4) in place of `chill`: `frost(owner, body, hits, fromArm)`, FROST_FULL 4 hits within 100 ticks freeze it 80
   ticks in crystal; each hit sends a new kind CHILL 140 (id the body, size the fraction, bx 1 when from the arm (an attacker's frost
   starts at the arm), ticks the keep); **a blow from the freezer on a body it froze shatters it on the next tick with 8 more damage**
   ("when you hit them again, you can shatter them"); **punches**: `AttackEntityEvent` (a real left click; the owner: "while in fruit
   mode whenever you left-click someone"): the attacker a ServerPlayer with FruitData.fruit HIE, FruitData.active (fruit mode), an empty
   main hand, no saber out, the target living: frozen by this player -> shatter; else frost +1 (at most once every 8 ticks a target,
   so spam-clicking does not freeze in a second). Prove `AttackEntityEvent.getTarget` first: add to verify/api-allowlist.txt
   `M net/minecraftforge/event/entity/player/AttackEntityEvent getTarget ()Lnet/minecraft/world/entity/Entity;   # [forge] 1.20.1
   event/entity/player/AttackEntityEvent.java: public Entity getTarget()` (the stub already has it; the class is in HexGhoul's list).
   `HieMoves.dodged`: frost(attacker, 1, fromArm true) (the owner: "their limbs that they hit with like turn blue"). The spears and
   saber cuts: frost +1 as chill did.
2. `server/HieMoves`: the Ice Ball without real blocks (remove `ball`, `inside`, `spread`, its crack steps and breakIce; BALL bz 0;
   clients draw HieArt.prison); Ice Time's hold 70 (its shatter sooner: "meant to be one of the most satisfying abilities"); **Ice Age**
   (8zh 1, 2: "Ice World"): `GROUND_BACK` 400 -> 160 (8 s), and a field that lasts as long: every 4 ticks every living thing but the
   caster (not only foes: "anything that comes in there should freeze") inside the cone (reach 26, 50 degrees each side, within 6 up or
   down) is frozen 60 ticks if not frozen and not in its 20-tick grace after thawing. Keep the frosted ice under the feet on water (the
   Frost Walker's, it melts by design); say so if the owner means that by "melts".
3. `client/hie/HieFx`: FROZEN draws HieArt.block at the held body (drop drawIce, encased, sphere, the BlockRenderDispatcher pass);
   THAW shattered -> a crystalShatter spot (size about the body's height x 0.7); BALL -> HieArt.prison; kind 140 CHILL -> a map body ->
   {fraction, fromArm, since, until} drawn with HieArt.frost (an arm's frost: draw it over the upper half); drop it on FREEZE. Route
   kinds 120-159 to HieFx in `client/dragon/UoFx` (only 120-139 now).
4. `DragonTests.hieIce`: add block, crystalShatter, frost and prison to the budget.
5. **The Gura ultimate's ground in a cone** (8zh 5): `gura/Heave.bend` now presses a round bowl (length/2 across, middle 0.55 out) with
   rings and a heaved rim; the owner wants the same bending but in the cone, forming and pushing forward with the wave: make the
   depression follow the cone (deepening and widening with the distance, its edges along the cone's sides heaved up, the rings arcs
   across the cone), keep the slabs and the self-repair. Render through GuraShots `|gbend:age` from above and behind.
6. **The Haki lightning on moves is too fat** (8zh 6: "ridiculously fat compared to" the release's): `haki/ConquerorStorm.blast` (used
   by client/haki/ConquerorSparksClient for every MIXED burst) draws the release's bolts with width `size x 0.4` (about 1.4 blocks near
   a player): the release's own bolts read as thin black lines with a pale rim and fine branches near the player. Make the move bolts
   as thin as the release's are where the player sees them (start about a fifth of now and judge in SparkPreview rows "blast 9",
   "ultimate thick" beside "release" at the same distance), keep their count and reach.
7. **No ground effects in midair** (8zh 7): ground cracks, dust, rocks and pools must sit on the ground under where they happen, and
   not appear at all with no ground near: Gura's QUAKE, TREMOR, SHAKE (Shima Yurashi draws its spreading cracks at the player's feet:
   in midair they hang in the air: the screenshot), TILT, RIFT starts, the ultimate's heave; Magma's POOL and ERUPT (pools already look
   for ground 4 under: check the client too). Do it on the client: the ground found once under the point (a clip down a few blocks,
   Fluid.ANY), the ground art drawn there, skipped when none. Ask the owner whether the white glass cracks at the fists (Whitebeard's
   air cracking, by design since 3.1.1) count too, before removing them.
8. **The Nika fruit (Gear 5) rework** (8zh 8, the biggest): the transformation on Z, the moves on the current keys, the stretchy moves
   and M1s usable untransformed, the transformation music much shorter, the HUD white instead of yellow, better Haki versions, the
   third-person animations reworked (Bajrang Gun: the body turned in midair to aim down, the whole body in it, not just the arm), Rubber
   World bending the real ground like rubber/slime in waves (through Scars, put back, like the Gura ultimate's bend); first person left
   alone. The original's code: com/hexhaki/devilfruit/gameplay/GearServerController and the rest of com/hexhaki/devilfruit (patched
   through patch/PiecePatch, as gearLightning hooks it). The owner loves the fruit, the transformation and the clouds: change carefully,
   ask before removing anything.

## 9. What to do next (3.2.2)

1. Run `python3 build.py` first (the committed 3.2.2 work is unbuilt: only HieArt and the preview were compiled on their own).
2. Do 8zi's "Left" items 1 to 4 (the Ice fruit), then 5 to 7, release 3.2.2 with notes, handoff, push, both runs green. Then 8 (Nika),
   carefully, in its own versions.
3. Ask the owner how the Ice fruit plays after 3.2.2 (the crystal, the frost punches, Ice Age's field, the new sounds), and how the
   energy feels (FruitEnergy.COSTS, the refill) and whether Gear 5 should spend energy too.
4. Still unanswered: 3.1.5 to 3.1.8 (the hound, Ryusei Kazan, the Light beam's drag, the Gura ultimate's lag on the phone), the
   first-person hands (3.1.4), 3.1.2's Gura fixes, the Light fruit and katana fixes (3.0.x to 3.1.0), 2.9.5's Conqueror sounds.
5. POINT ONE for any new fruit: same style (meshes, clips without legs or head, first-person hands, CC0 sounds), the Conqueror's
   lightning out of its explosions with Haki (UoMoves.explode does it for free; else ConquerorAura.exploded/mixed at the strike).
6. Lag: the dragon's per-frame CPU skinning; ask the owner where they still see lag.
