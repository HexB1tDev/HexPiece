# HexPiece: handoff after 2.9.6 (every rule in one place, where things stand, what is next)

This is the newest handoff. Read it whole before touching anything. It gathers every standing rule the owner has given (CLAUDE.md's
house rules plus everything learned since 2.1), so you do not have to dig them out of the older notes. The full history (verbatim
messages, what each version did) is in next-ai-2.7.0.md (2.7.0 to 2.9.5) and the older next-ai-*.md files; open them only when you
need the detail of a feature. 2.9.6 (the Light fruit after Egghead) is written up in full in section 8b below.

Branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest). **2.9.6 is pushed (check its "Build HexPiece" and "Release
HexPiece" runs are green before anything else). The next version is 2.9.7.**

---------------------------------------------------------------------------------------------------------------------------------------

## 0. Talking to the owner

- Answer in English, plainly. Say what changed in words a player understands (what they will see and hear), not class names.
- The owner speaks through speech-to-text. Read through it: "hockey" / "Haiku" = **Haki**, "Rio" = **Ryou**, "wonomi" / "uo uo" = the
  **Uo Uo no Mi** (the dragon fruit), "live fruit" = the **Light fruit** (Pika Pika no Mi), "M1s" = left-click attacks.
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

## 9. What to do next (2.9.7)

1. Check 2.9.6's runs. Ask the owner how the new Z plays (holding it, the reflections off blocks and the sea, passing through mobs,
   reforming with speed), how the smooth dashes feel (sprint kick, dive, V), and how the light looks. Also, still unanswered: 2.9.5's
   Conqueror charge and release sound, 2.9.4's Haki. Fix whatever they report first.
2. If they want more of the Egghead rework: X Yasakani (face-down hover, slugs of light raining while you move), the Egghead flight pose
   (feet-first, spinning), the clones flying in. Ask before adding.
3. Not rendered in a preview yet: Gear 5's and the storm lance's 3D bolts (tools/preview/BoltPreview or FxPreview). Render and judge.
4. Lag: the dragon's per-frame CPU skinning; re-run the per-tick/per-frame handler audit. Ask the owner where they still see lag.
5. Still unanswered by the owner: the fighting styles in first person (2.8.2), the 3D blows (2.8.3), the show moves (2.8.4), the
   dragon's wave and sounds. Ask, do not guess.
