# HexPiece: handoff after 3.0.9 (every rule in one place, where things stand, what is next)

This is the newest handoff. Read it whole before touching anything. It gathers every standing rule the owner has given (CLAUDE.md's
house rules plus everything learned since 2.1), so you do not have to dig them out of the older notes. The full history (verbatim
messages, what each version did) is in next-ai-2.7.0.md (2.7.0 to 2.9.5) and the older next-ai-*.md files; open them only when you
need the detail of a feature. 2.9.6 to 3.0.9 (the Light fruit after Egghead, and 3.0.8 the katana stance) are written up in full in sections 8b to 8o below.

Branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest). **3.0.9 is pushed (check its "Build HexPiece" and "Release
HexPiece" runs are green before anything else; 2.9.6 to 3.0.8 are green). The next version is 3.1.0 (after x.y.9 comes x.(y+1).0).**

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

## 9. What to do next (3.1.0)

1. Check 3.0.9's runs. Ask the owner whether Z over the sea is right now, whether the katana pose or the Light stance still sticks or
   covers a move, how the right click looks in first person, the bigger blast, C's wave, V's kick, X, the punch. Still unanswered:
   2.9.5's Conqueror sounds, 2.9.4's Haki.
2. Not rendered in a preview yet: Gear 5's and the storm lance's 3D bolts (tools/preview/BoltPreview or FxPreview). Render and judge.
3. Lag: the dragon's per-frame CPU skinning; re-run the per-tick/per-frame handler audit. Ask the owner where they still see lag.
4. Still unanswered by the owner: the fighting styles in first person (2.8.2), the 3D blows (2.8.3), the show moves (2.8.4), the
   dragon's wave and sounds. Ask, do not guess.
