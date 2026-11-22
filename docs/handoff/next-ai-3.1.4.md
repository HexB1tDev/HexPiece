# HexPiece: handoff after 3.1.4 (every rule in one place, where things stand, what is next)

This is the newest handoff. Read it whole before touching anything. It gathers every standing rule the owner has given (CLAUDE.md's
house rules plus everything learned since 2.1), so you do not have to dig them out of the older notes. The full history (verbatim
messages, what each version did) is in next-ai-2.7.0.md (2.7.0 to 2.9.5) and the older next-ai-*.md files; open them only when you
need the detail of a feature. 2.9.6 to 3.1.0 (the Light fruit after Egghead, and 3.0.8 the katana stance) are written up in full in
sections 8b to 8p below; section 8q is the Gura Gura no Mi's research and plan, section 8r what 3.1.1 built of it, 8s 3.1.2's fixes, 8t
the Magu Magu no Mi (3.1.3), 8u 3.1.3's Gura changes, 8v 3.1.4 (first-person hands, the Haki orb, the charge sound) and 8w the owner's
requests still open (3.1.5 and 3.1.6).

Branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest). **3.1.4 is pushed: check its "Build HexPiece" and "Release HexPiece"
runs are green before anything else (3.1.3 and before are green). The next version is 3.1.5.**

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

## 8w. The owner's requests still open (verbatim; build them in this order)

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

## 9. What to do next (3.1.5)

1. Check 3.1.4's runs (Build and Release HexPiece on claude/nice-bell-gam7qf).
2. Build 8w's 3.1.5 (Magma C hound head aimed with pitch, Magma X hold-count meteors, the Light right click dragging to the beam's end and
   exploding, the Conqueror's lightning on every fruit's moves), render and judge, push; then 3.1.6 (the Gura ultimate).
3. Still unanswered: how the first-person hands (3.1.4) and the Gura fruit with Haki play; 3.1.2's Gura fixes; the Light fruit and
   katana fixes (3.0.x to 3.1.0); 2.9.5's Conqueror sounds, 2.9.4's Haki.
4. Not rendered in a preview yet: Gear 5's and the storm lance's 3D bolts. Render and judge.
5. Lag: the dragon's per-frame CPU skinning; ask the owner where they still see lag.
