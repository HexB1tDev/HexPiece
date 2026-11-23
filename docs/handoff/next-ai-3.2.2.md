# HexPiece: handoff for 3.2.2 (every rule in one place, where things stand, what is next)

This is the newest handoff. Read it whole before touching anything. It gathers every standing rule the owner has given (CLAUDE.md's
house rules plus everything learned since 2.1), so you do not have to dig them out of the older notes. The full history (verbatim
messages, what each version did) is in next-ai-2.7.0.md (2.7.0 to 2.9.5) and the older next-ai-*.md files; open them only when you
need the detail of a feature. 2.9.6 to 3.1.0 (the Light fruit after Egghead, and 3.0.8 the katana stance) are written up in full in
sections 8b to 8p below; section 8q is the Gura Gura no Mi's research and plan, section 8r what 3.1.1 built of it, 8s 3.1.2's fixes, 8t
the Magu Magu no Mi (3.1.3), 8u 3.1.3's Gura changes, 8v 3.1.4 (first-person hands, the Haki orb, the charge sound), 8w the owner's
requests for 3.1.5 and 3.1.6 (verbatim, both done), 8x what 3.1.5 did, 8y the Gura ultimate (3.1.6) and 8z its key moved to N (3.1.7),
8za the owner's open requests, 8zb 3.1.8, 8zc to 8ze the Hie Hie no Mi (3.1.9: its plan, the owner's answers on real ice, as built),
8zf 3.2.0 (the Devil Fruits tab, the rarity ranks, the fruit energy bar), 8zg 3.2.1, 8zh the owner's requests after 3.2.1, 8zi 3.2.2 as
built, 8zj the owner's stress-test notes during 3.2.2 (the Magma X bug, done; the Nika fruit rework), 8zk the Nika plan and the
owner's answers, 8zl 3.2.3 (Nika part 1), 8zm 3.2.4 (Nika part 2), 8zn 3.2.5 (the lightning again: read it), 8zo 3.2.6 (the Bajrang Gun), 8zp 3.2.7 (Dawn Whip), 8zq 3.2.8 (Giant's hold), 8zr 3.2.9 (Star Gun's rubber body; the owner wants GIFs), 8zs 3.3.0 (Rubber World's rolling ground), 8zt 3.3.1 (Toon Force, Dawn Whip and the jump pose redone; the owner's jump request),
8zu 3.3.2 (the rubber jump, Rubber World on everyone else, the skip pose redone from the owner's description: read it), 8zv the
owner's three new fruits (verbatim), 8zw their accepted keys, 8zx 3.3.3 (Rubber World's slow waves), 8zy 3.3.4 (Noro), 8zz 3.3.5 (Supa),
8zza the owner's Rare fruit request (the Ito Ito no Mi, verbatim), 8zzb 3.3.6 (Horo: cute ghosts, the Gloom effect, the astral spirit),
8zzc/8zzd 3.3.7 (Ito), 8zze 3.3.8 and 3.3.9 (the owner's answers, the rubber ripples, the Dawn Pistol accepted), 8zzf 3.4.0 to
3.4.3, 8zzg 3.4.4 (Goof Ball, Dawn Whip) and the owner's Broken Bones request (3.4.5).

Branch `claude/nice-bell-gam7qf` since 3.3.2 (fetch it first: it is the newest; it continues claude/pensive-heisenberg-mkr9cl). **3.1.9, 3.2.0
and 3.2.1 are pushed and green; 3.2.2 (8zi: the Ice fruit's crystal, frost punches and Ice World field, the Gura cone, thin Haki
lightning, no ground art in midair, the Magma X fix) is pushed and green; 3.2.3 (8zl: the Nika fruit's keys Z X C V B N M, Haki names,
white panel, stretch moves untransformed, drums instead of the music) is pushed and green; 3.2.4 (8zm: the four-second transformation
and its new animation, the punch's wobble) is pushed and green; 3.2.5 (8zn: the move lightning the release's own again, after the
owner's anger) is pushed and green; 3.2.6 (8zo: the Bajrang Gun's fist and slam) is pushed and green; 3.2.7 (8zp: Dawn Whip's cartoon spin) is pushed and green; 3.2.8 (8zq: Giant holds
what it grabs) is pushed and green; 3.2.9 (8zr: Star Gun's struck body as rubber) is pushed and green; 3.3.0 (8zs: Rubber World's ground
rolling in rubber waves) is pushed and green; 3.3.1 (8zt: Toon Force, Dawn Whip and the jump pose redone to the owner's target
look) is pushed and green; 3.3.2 (8zu: the rubber jump, Rubber World on everyone else, the skip pose redone from the owner's
description) is pushed and green; 3.3.3 (8zx: Rubber World's slow waves) is pushed and green; 3.3.4 (8zy: the Noro Noro no Mi) is
pushed and green (Build #171, Release #125); 3.3.5 (8zz: the Supa Supa no Mi, its blades of katana steel) is pushed and green (Build
#172, Release #126); 3.3.6 (8zzb: the Horo Horo no Mi, its cute ghosts, Gloom, the astral spirit) is pushed and green (Build #175, Release #127); 3.3.7
(8zzc the plan, 8zzd as built: the Ito Ito no Mi, its slim pointed strings) is pushed and green (Build #177, Release #128); 3.3.8 (8zze:
the rubber ripples twice as high, every Gear 5 fall ripples) is pushed and green (Build #179, Release #129); 3.3.9 (8zze: Dawn Pistol)
is green (Build #180, Release #130); 3.4.0 (8zze: the skin stretched through the body) is green (Build #181, Release #131); 3.4.1 (8zze:
the looping arm, the slowing stretch, the straight launch, the crater) is pushed and green (Build #182, Release #132); 3.4.2 (8zzf: the
owner's Ito notes, Supa's blades, the Horo projection) is pushed and green (Build #184, Release #133); 3.4.3 (8zzf: Noro's held cone, Ito's
Sky Road) is green (Release #134, Build #187). 3.4.4 (8zzg: Goof Ball, the new Dawn Whip) is green (Build #188, Release #135). 3.4.5
(8zzg: the Gura ultimate's Broken Bones) is pushed: check its runs.**
The cloud environment is the personal "HexPiece" (env_01PmnEXrwDhxzij8M5q6P6bL in 3.3.6's session; freesound.org, cdn.freesound.org, opengameart.org); a session on
Default is refused freesound.org: get_environment lists it under saved_for_session_repositories, switch_environment to it.

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
3. **That lightning is EXACTLY the Conqueror's release's lightning, and nothing else** (the owner, 3.2.5, furious: "I don't know how many
   times I have to fucking say this ... The lightning is to be exactly the same as the one in Conqueror's hockey release. The one that
   you added for the fruits is the old lightning effects. They are not meant to be in use anymore. In fact, they should be gone from
   the game entirely ... This has happened over 10 times by now."). `haki/ConquerorStorm.blast` IS the release's `bolt()` cut short:
   the width at each point is the release's own at that distance from where the bolt starts (`RELEASE_SPAN` 80: thin where it leaves,
   swelling as it goes, tapering at its end), the release's kinks (`STEP`), branches, black body and Ryou-coloured rim. **Never scale a
   blast's width on its own** (3.1.8 squeezed the release's width curve into short bolts: "ridiculously fat"; 3.2.2 drew them a fifth as
   wide: thin black lines, "the old lightning effects"). Never use any other lightning look for a move (the old thin burst of arcs is
   deleted). Before changing anything that draws lightning, render it beside the release from the same camera (the scratchpad's
   Compare.java pattern: SparkPreview.render with `far` true, the release round the player, a blast 1.5 and 8 ahead) and judge them
   the same.

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
9. Git: commit on the branch the session names (3.2.2: `claude/pensive-heisenberg-mkr9cl`), push with `git push -u origin <branch>`. No
   pull requests unless asked.
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

- **A move's Haki lightning is the Conqueror's release's lightning, exactly** (POINT ONE 3; 8zn): the release's own bolts cut short
  (ConquerorStorm.blast, RELEASE_SPAN). Never thin lines, never fat tubes, never any other lightning look; never re-scale its width.
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

## 8zi. Done (3.2.2): the owner's requests 8zh 1 to 7, and the Magma X bug (8zj 1)

**Done in the previous session (committed then unbuilt, built in 3.2.2):**
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

**Done in 3.2.2 (nothing tested in game; build, tests and renders):**
- **No real ice round bodies** (`server/HieFreeze` rewritten): `encase`, `breakIce`, the crack ids and destroyBlockProgress are gone;
  FREEZE's bz is always 0; every client draws `HieArt.block` at the held feet (`client/hie/HieFx` FROZEN: at, half, height, born, life,
  ball; a re-freeze keeps the crystal's age). THAW shattered -> a CRYSTAL spot (`HieFx.CRYSTAL` -1, `HieArt.crystalShatter`, size the
  body's height x 0.7); inside a ball no shatter of its own unless smashed. The "inWall" cancel is gone (no blocks). The crack sound
  plays CRACK_TICKS 24 before the end (the crystal's cracks are drawn by HieArt.block's CRACK_TIME).
- **Frost** (8zh 3, 4): `HieFreeze.frost(owner, body, hits, fromArm)` replaces chill: FROST_FULL 4 hits within FROST_KEEP 100 ticks
  freeze FROST_FREEZE_TICKS 80; each hit slows (amplifier hits - 1), plays CRACK quietly and sends **CHILL 140** (id the body, bx 1
  from the arm, by its height, size the fraction, ticks the keep). `HieFreeze.punch` (AttackEntityEvent, proven by the original's own
  use): a ServerPlayer with FruitData.fruit HIE, FruitData.active, an empty main hand, no saber out, a living target: a body it froze
  -> `smash` (SMASH_AFTER 4 ticks after it froze: shatters next tick with SMASH_DAMAGE 8), else frost +1 at most every PUNCH_EVERY 8
  ticks a body. `HieMoves.cold` (spears, saber cuts): smash or frost 1; the road's fist frost 2; `dodged`: frost(attacker, 1, fromArm
  true). The move bursts (Pheasant's explosion and so on) never smash. Client: FROSTS (fraction, arm, since, until), drawn over the
  moving body (`HieArt.frost`; from the arm `HieArt.armFrost`: the right arm (yBodyRot f_20883_) turned blue under a crystal sleeve,
  spreading over the body past 0.25), fading over its last 10 ticks, dropped on FREEZE. `HieArt.skin` (a blue column) makes the body
  read as turning blue. Kinds 120 to 159 now go to HieFx (`client/dragon/UoFx`).
- **Ice Ball** (`HieMoves.prison`): no blocks (`ball`, `inside`, `spread` gone); BALL bz 0; clients draw `HieArt.prison` and, on its
  SHATTER, a crystalShatter of radius x 1.2 (a SHATTER with no ball, the saber's, stays glassy).
- **Ice Time** TIME_HOLD 70 (was 100), a miss 55.
- **Ice World** (Ice Age): GROUND_BACK 160 (8 s, the road's too); the move's Live lasts GROUND_BACK: the wave front as before (struck 6,
  frozen AGE_HOLD 80, lightning out of the first two), then every AGE_FIELD_EVERY 4 ticks everyone in the cone (PikaMoves.foes: every
  living thing but the caster, his allies, creative players, his own pets and armour stands) not frozen and not in GRACE 20 ticks after
  thawing frozen AGE_FIELD_HOLD 60 (no damage). Newcomers are struck as by the front.
- **Budget** (`HieArt.lite`: no mist, half the little crystals, 8 shards; `HieFx` FULL_FROZEN 6, FULL_FROSTS 6, FULL_SHATTERS 4 drawn in
  full, the rest and anything past LITE_FAR 24 lite): DragonTests.hieIce "a crowd frozen in crystal and shattering" (24 crystals, 12
  shatters, the prison: worst 14,909 quads) and "a dozen frosted bodies" (worst 4,650). Renders: docs/handoff/hie-crystal-3.2.2.jpg.
- **The Gura cone** (8zh 5, `gura/Heave.bend` rewritten): in the cone's frame, a trench (q = angle / cone; depth 5-8 x (1 - q^2),
  ramping in over 0.45 of the length, shallowing over the last 0.2), arcs across it every 5 blocks round the feet, the sides heaved 3.4
  along the cone's edges (gaussian 3 blocks off the edge line), the ground shoved into a 3.6 pile at the front, the 4x4 slabs as before
  (halved in the deep middle); nothing behind the feet or past 14 degrees outside the cone (DragonTests). The Bend runs only with ground
  under the feet. GuraShots `|gbend` now shades the ground by height. Render: docs/handoff/gura-cone-3.2.2.jpg.
- **Thin Haki lightning** (8zh 6): `ConquerorStorm.BLAST_WIDTH` 0.08 (was 0.4) of the release's width: at most about 0.39 blocks
  (DragonTests: 0.2 to 0.6). SparkPreview rows "blast 9" and "ultimate thick" read as thin black lines with a rim.
- **No ground art in midair** (8zh 7): `GuraFx.ground(at, up, down)` (a clip, Fluid.ANY; GROUND_DOWN 3): QUAKE, TREMOR, the LEAP's dust,
  SHAKE_ON's spreading cracks (Shake.at null: none drawn, the rumble and the view shake stay), SHAKE_OFF's tilt, RIFT and HEAVE are put
  on the ground under them or not drawn; MaguFx does the same for POOL, GEYSER and ERUPT. The fists' white air cracks (CRACK) and the
  shock rings (SHOCK) stay: they are the air breaking (Whitebeard), by design since 3.1.1. The owner, asked in the 3.2.2 notes
  (verbatim): "Why is this being put in question? I said the cracks on the floor, not the air glassing cracks" and "I said the cracks
  on the floor are the only ones that are meant to leave, not the air cracks." So the air cracks stay: never remove them, never ask
  again. (Speech-to-text turned "the cracks of" into "agronomy": read through it.)
- **Magma X** (8zj 1): tools/magu_anims.py `up` arm(-168, 0, -22, -10), `pulled` arm(-110, 0, -14, -8) (with the arm up, the old +16
  roll tipped the fist over the head); the SKY fists leave from feet + 2.45, 0.5 to the right, 0.1 ahead; `MaguArt.meteor(..., trail)`
  draws a fist thrown up with a trail only as long as it has flown (was 8 blocks of streaks under it at launch: "particles ... from the
  player's feet"). MaguShots `|msky:age`. First person untouched (HandMoves PUMP).

## 8zj. The owner's stress-test notes during 3.2.2 (verbatim, with the images described)

"I'm gonna now list a few bugs that I find while I'm now like actually stress testing this on my PC. Magu no Mi, the lava fruit, the X
move is the only bug I see in it, where the animation makes the hand go inside the player's head instead of being a little more to the
right. So basically just move it a little bit to the right instead of the left. Also, I think the projectiles are coming from a bit too
below. Honestly, still on the X move, by the way. The projectiles are coming from too low in the player's body, causing particles to
summon from the player's feet.

2. About the Nika fruit, we gotta do a lot of reworks. It is really old, bro. This is like really old. It was the first fruit I made, so
since I got you now, we're gonna do a major rework on this shit. The punching animation makes the body wobble too much in the third
person. I don't know why it wobbles too much. The transformation animation is completely broken. It's just busted. It looks bad. It
doesn't look badass at all. The pounding on the chest is cool, but the rest of it is horrible. Bajarang gun, as already said, that move
is completely awful looking. The fist is like should be, I don't know, perhaps like more shaped like a fist and more rounder. I'm not
saying make it completely round, but like it should be a fist, a big one. And it should be coming first from above, and then the player
slowly rotates it downwards and slams it down. This is one of the strongest moves in the entire game, so it should definitely be
something special. The only one that is actually properly decent is what's it called? The one Star Gun, yeah, the Star Gun is okay. It
could push the entities model a bit further and make it more like rubber, but it's good. Dawn Whip needs a complete rework. That move
has to be entirely scrapped and redone. I recommend you check and get your references for Luffy against Kizaru, where he uses that move
with that name, Dawn Whip, and he actually rotates like a cartoon character. I think that the player's feet should also rotate like a
cartoon character. Like I'll send you some reference images for reference, but just make sure that this fruit feels like a cartoon,
like Bugs Bunny type of deal. Like every move should feel like a Bugs Bunny movie, where it makes barely any sense because he's just very
goofy, very cartoonish, because he has kind of Toon Force. You can research that on your own if you feel like you need to, if you don't
know what it is. This fruit has to be special, considering it's the rarest fruit in the game, or it will be once we add a rarity system.
Like we need to make this one super special. Giant is not holding the creatures right. Sometimes they get off the player's hand. It's
overall really junky. Also make sure that it adapts the new keys. And that the moves change name whenever you activate hotkey, because
currently they don't. And I already said to the previous AI, I hope you wrote in the next hand, like on the previous handoff, is that we
need to adapt this fruit to the new keys. All of the moves, literally all of them, even transformation to Z, and all the others following
the transformation, X, C, V, B, N, M, and that's it. Not the transformation, I mean the hotkey versions should replace the slots still as
they do. The HUD should like rename them as soon as hotkey is activated. And yeah, that should be it for now."

Images sent with it (not kept: they are only in that conversation):
1. In game at night, the player (white top, red hair skin) doing Ryusei Kazan: the raised right hand inside the side of the head.
2. In game from behind: Ryusei Kazan's fist of magma high above, a dark smoke ball under it, and orange flame spikes in a line from it
   down to the player's feet and the ground. Both fixed in 3.2.2 (8zi, Magma X).
3. Anime frame (Bajrang Gun): Gear 5 Luffy grinning in the air over his giant fist, the fist huge, rounded and soft like a balloon but
   still a fist (knuckles and fingers folded), tan, with motion lines.
4. Anime frame (the cartoon run): Gear 5 Luffy bent forward running, his legs stretched to twice their length in huge rubbery strides,
   the feet flopping, like an old cartoon.
5. Anime frame (Rubber World / Toon Force): a tiny Gear 5 Luffy jumping over ground that rolls in soft rubbery waves under a full moon.

Read as: (1) done in 3.2.2. (2) the Nika fruit rework, with 8zh item 8: see 8zk.

## 8zk. The Nika fruit rework: what the owner asked (8zh 8 and 8zj 2), to plan and build next

The owner's points, in one list (keep every one; ask before removing anything they did not name):
1. **Keys**: like every fruit: the transformation on Z; the other moves on X, C, V, B, N, M (the fruit move keys; M was unused). With
   the Haki on, the Haki versions replace the slots "as they do", and the HUD (the abilities panel) renames them the moment the Haki
   goes on (today it does not).
2. **Untransformed**: the stretchy moves and the M1s work in the normal, untransformed state too.
3. **Transformation**: much shorter music; the animation reworked ("completely broken ... doesn't look badass at all"): keep the
   chest pounding (the drum of liberation: "cool"), redo the rest; the clouds and the look of the transformation are loved: keep them.
4. **Third person**: every animation reworked to today's standard (first person is fine: leave it). The punch makes the body wobble too
   much: find why (likely the body or its roll keyed, or two clips fighting) and calm it. Turn the whole body when a move needs it.
5. **Bajrang Gun** (one of the strongest moves in the game, "something special"): the fist a big, rounded but real fist (ref image 3:
   ballooned knuckles and folded fingers, not a ball), coming first from above, the player rotating slowly in the air to aim it down,
   then slamming it down (the whole body in it, not just the arm).
6. **Star Gun**: keep; push the struck body's model further and make it more rubbery.
7. **Dawn Whip**: scrap and redo after Luffy vs Kizaru (Egghead): he spins like a cartoon character, the feet too (ref image 4: the
   legs stretched into huge floppy strides).
8. **Giant** (the giant form): bodies held in the hand slip off: hold them properly ("overall really junky").
9. **Rubber World**: the real ground bends like rubber, rolling in waves (ref image 5), through Scars as the Gura ultimate's bend,
   always put back.
10. **HUD** white instead of yellow; **Haki versions** better.
11. **Toon Force** everywhere: every move goofy and cartoonish, "like a Bugs Bunny movie" (squash and stretch, big anticipation,
    exaggerated arcs, smears, things that make barely any sense). The rarest fruit (Mythical): it must feel special.
The original as found (3.2.2's session, javap on input/hexhaki-2.1.0.jar): com/hexhaki/devilfruit has NikaFruitItem, data/ (AbilityMode,
BajrangPhase, GearAbility, GearCloudShape, GearData, GearMastery, GearTiming, GearUpgrade, RubberLimb, StretchCurve, StretchStyle) and
gameplay/ (GearEvents, GearFields, GearServerController). `GearAbility` (mastery, requiresGearFive, display, key, description): PISTOL
(0, base, "Gum-Gum Pistol", Attack: the arm stretched to the target), ROCKET (15, "[V]": an arm anchored, hauled to it), WHIP (35, "[K]":
a leg swept in an arc; the owner's "Dawn Whip"), GEAR_FIVE (0, "[R]": the transformation), GATLING (50, "[L] hold"), SKY_BOUNCE (80,
"[Shift]+[Space]"), GIANT (110, "[G] toggle": Gigante, right-click grabs one body and again throws it; under Armament or the Advanced
Conqueror's [G] charges the Bajrang Gun), COIL (150, "[M] hold-release": a coiled launch; with Haki a lightning lance), RUBBER_WORLD
(200, "[J] hold-release": a field, "nothing is placed or broken"), STAR_GUN (260, "[B]", Haki only: an arm curving round cover to a
target up to 50 blocks, folding everything on the curve). Its keys clash with the Haki keys (R mode, G Conqueror, J Observation, K L
H): hence the owner's Z X C V B N M. `GearTiming`: TRANSFORM_TICKS 170 (gather 34, loosen 106, rise 128, float 146, land 170),
DRUM_STRIKES, REVERT_TICKS 14. Clips (original player_animation): gear5_awaken, gear5_stance, gear5_run, gear5_pistol, gear5_don_gun_left
/ _right, gear5_gatling, gear5_rocket, gear5_whip, gear5_bounce, gear5_coil_charge / _release, gear5_lance_charge / _throw,
gear5_giant_charge / _grab / _throw, gear5_gigante_laugh, gear5_bajrang_charge / _slam, gear5_dodge_* (4), conqueror_gear5_charge /
_release. Sounds: gear5_*.ogg (ambient_transformation is the transformation music; dawn_pistol, dawn_whip, bajrang_*, rubber_world ...).
Client side: com/hexhaki/client/devilfruit GearKeys (its keys), GearHud (the yellow HUD), GearClientState, GearMovement, GearVfx,
RemoteGearStates, RubberBendStates, RubberLimbStates, render/ (GearAuraLayer, GearBodyDeformer, GearCloudLayer and GearCloudMesh: the
clouds, GearJacket*, GearOutfit*, GearRenderHooks, RubberBendDeformer, RubberLimbGeometry/Layer/Mesh: the stretched limbs),
screen/GearMasteryScreen; network GearAction, C2SGearAction, C2SGearUpgrade, S2CGearEntity, S2CGearFx; mixin GearJacketSkinMixin.
PiecePatch already touches GearHud, GearVfx, GearJacketHaki, GearJacketSkin and GearServerController (grep "Gear" in it).
GearKeys.tick reads the Haki key mappings (com/hexhaki/client/HakiKeys) for Gear 5's moves: ARMAMENT -> GEAR_FIVE_TOGGLE, DOMINION ->
WHIP, STAR_GUN -> STAR_GUN, OBSERVATION -> ROCKET_START/RELEASE, STRIKE -> GATLING_START/RELEASE, CONQUEROR -> GIANT_START/RELEASE,
ACOC -> RUBBER_WORLD_START/RELEASE, CONVERGENCE -> COIL_START/RELEASE, the attack -> PISTOL; each sent as C2SGearAction(GearAction).
So the rework can keep the server's GearActions and remap only the client keys to the fruit keys (Z X C V B N M) in fruit mode, as
the other fruits read them (client/FruitKeyN for N).
**The owner's answers (AskUserQuestion, 3.2.2's session):** keys = "Star Gun takes X": Z Gear 5 (transform / revert), X Rocket, C Whip,
V Gatling, B Giant, N Coil Spring, M Rubber World, left click the stretching punch (Pistol), Shift+Space Sky Bounce; **with Haki on**: X Star
Gun, C Dawn Whip, B Bajrang Gun, N Lightning Lance, left click Dawn Pistol (the panel renames them the moment Haki goes on).
Untransformed = "All stretch moves": Pistol, Gatling, Rocket, Whip, Giant and Coil Spring work without Gear 5; Gear 5 only: Rubber World,
Sky Bounce, Bajrang Gun, Star Gun and the Dawn moves. Music: first answered "Only while transforming", then replaced by the owner
(verbatim): "Honestly, if you could give the transformation just the drum sounds and just get rid of the music because of copyright, that
would be perfect." So **no music at all** (gear5_ambient_transformation silenced for good, never bring it back): the transformation plays
only drum hits (CC0 web recordings, tools/web_sounds.py), on the chest pounding's beats. Transformation = "Shorten to about 4 s": keep the
chest pounding and the clouds, cut the slow parts.
How: the original's code is com/hexhaki/devilfruit (GearServerController and the rest, Gear 5's moves and keys), patched through
patch/PiecePatch (as gearLightning hooks it); its animations are among the original player_animation files (reworked ones go under
src/main/resources/assets/hexhaki/player_animation/). Read it all first, list its moves, keys, clips, HUD and sounds, and how its
transformation works; then plan file by file, ask the owner what is unclear (AskUserQuestion: which move goes on which key, what the
untransformed set is), and build in several versions, rendering every clip in third person (POINT ONE's style: no head, no unused limb).

## 8zl. Done (3.2.3): the Nika fruit, part 1: keys, Haki names, white panel, stretch moves untransformed, drums

The owner's answers are in 8zk. Nothing tested in game: build, tests, the panel rendered (UiPreview hud-6, hud-7: docs/handoff/nika-panel-3.2.3.png).
- **Keys** (`client/gear/NikaKeys`; patch/PiecePatch.nikaKeys: `GearKeys.tick` first asks `NikaKeys.tick(minecraft)` and returns when it
  says true (always); `releaseHeld` and `drain` call NikaKeys' first): in Devil Fruit mode (the original runs GearKeys.tick instead of the
  quick slots there) Z `HakiKeys.SLOT_6` GEAR_FIVE_TOGGLE; X `HakiKeys.MODE` ROCKET_START/RELEASE held, or STAR_GUN on the press when
  `NikaKeys.haki()` (Gear 5 and Armament or Ryou on, as the server's `dawn`); C `SLOT_7` WHIP; V `SLOT_8` GATLING held; B `STAR_GUN`
  GIANT held (the server makes it the Bajrang Gun under dawn); N `PieceKeys.FRUIT_N` COIL held (the lance under dawn); M
  `PieceKeys.FRUIT_M` (new, "Fruit move M", GLFW 77) RUBBER_WORLD held; the attack PISTOL. It calls the original's first two steps
  itself (`GiantGrabClient.tick`, `GearMovement.tick`), sends `C2SGearAction` on `HakiNetwork.CHANNEL` (only with a local player: the
  stubs have no getConnection). The quick-slot keys are the player's own (Binds: G K L H Haki, B Z C V X the fruit). build.py PATCHED
  lists GearKeys. `CombatMode.power`: Ctrl + R is Armament in every mode now (it toggled Gear 5 in Devil Fruit mode).
- **The panel** (`AbilityHud.gear/gearKeys`, `NIKA_WHITE` 0xFFF2F0E6 for Gomu Gomu and Gear 5, was yellow 0xFFE8C26A untransformed):
  rows {ability, key, name, Gear 5 only}: Haki on in Gear 5 renames them (Dawn Pistol, Star Gun on X in place of Rocket, Dawn Whip,
  Bajrang Gun, Lightning Lance); untransformed, Rubber World and Sky Bounce are dimmed with "Gear 5". The Ability Book's Nika section
  (`LogbookData.nika`) lists all ten with their keys and Haki versions, Giant's text's "[G]" read as "[B]", white.
- **Untransformed** (`server/NikaRules`; patch/PiecePatch.gearUntransformed): `onAction`'s Gear 5 gate (`GearData.flag(player,
  "gear5")`, every action but the toggle) became `NikaRules.allowed(player, action)` (Gear 5, or `stretch(action)`: PISTOL,
  ROCKET_START/RELEASE, WHIP, GATLING_START/RELEASE, GIANT_START/RELEASE, COIL_START/RELEASE, GIANT_GRAB); the same check in
  `tickGatling`, `beginGiant`, `beginCoil`, `releaseCoil` became `NikaRules.stretched` (has the fruit). Gear 5 only stays: Rubber World,
  Sky Bounce, Star Gun, and the Haki versions (the original's `dawn` needs Gear 5). HudTests.nika pins the list.
- **Drums, no music** (`tools/gear_sounds.py`, sounds/piece/gear, SOURCES.txt; `gear/GearSounds`; `server/GearDrums`; patch/PiecePatch
  .gearDrums): `gear5_ambient_transformation` (the 128 s music) is `"sounds": []` (build.py RENAMED[SOUNDS]); on each of the five beats
  (`GearTiming.DRUM_STRIKES` 40, 54, 68, 82, 96 of the 170-tick transformation) `tickTransform`'s two vanilla sounds (`SoundEvents
  .f_12316_`, `f_12090_`) go to `GearDrums.beat` (piece.gear.drum: drum1, "Tomtom6_TribalFloor.wav" by hello_flowers 30822, a deep
  tribal floor tom, 1.5 s, -14.5 LUFS; pitch 0.94 rising 0.025 a beat) and `GearDrums.under` (nothing); the last beat piece.gear.drum_big
  (drum2, "crdn_cinematic_heavy_drums - 14.wav" by choomaque-crispydinner 577408, -13.1 LUFS). Rejected: "taiko.wav" 486140 (a roll),
  "Mystic Power Taiko ... Bass Kick" 347126 (sounds processed). A drum has a body pitch by nature: the owner asked for drums.
- The session started on Default (freesound.org refused): the personal HexPiece environment is env_016v9rNSffz2fds2ha514nhY now
  (switch_environment to it; env_01PmnEXrwDhxzij8M5q6P6bL no longer exists).

## 8zm. Done (3.2.4): the Nika fruit, part 2: the four-second transformation, its new animation, the punch's wobble

Nothing tested in game: build, tests, every clip rendered (tools/anim_preview.py, docs/handoff/nika-awaken-3.2.4.jpg).
- **The clock twice a tick** (patch/PiecePatch.gearFast, gearFastClient; build.py PATCHED lists RemoteGearStates): the transformation's
  clock (GearData "transformTick", 1 to 170; its beats are compile-time constants inlined everywhere: tickTransform 34, 128, 129, 164,
  170, DRUM_STRIKES 40-96, FinalServerPatch.transformTick 10/128/163, GearJacketSkin 96/128, NikaChatter 164/170, GearTiming's
  manifest/lift/landSquash) runs at double speed: `GearServerController.tick` calls `tickTransform` a second time right after the first,
  and `RemoteGearStates.advance` steps the clients' copy by 2. Every beat still lands on its own clock tick; 85 game ticks in all. The
  lock (lockUntil now + 170) ends when the clock does (it is set to 0 at 170). The leap (vy 0.52 at clock 129) is real physics: he lands
  about when the clock's landing comes (~17 ticks).
- **gear5_awaken** (tools/nika_anims.py, 85 ticks, body/torso/arms only, no head, no leg): stagger 5, coil 13, burst 17, chest pounds
  on 20/27/34/41/48 (right, left, right, left, both: the arm wound out wide and back with the chest puffed out at beat - 3, the fist
  on the chest at the beat (outexpo), an overshooting rebound at + 2), loose as rubber 50-53, crouch 58-63, the star at 64 (arms up in
  a V: with the arm up a NEGATIVE roll spreads it out, as Magma's sky fist), the float 68-71 (arms wide, leaning back, rocking), the
  fall 80, the squash landing 82, the stance 85. anim.lint clean (the first star with a positive roll went through the head).
- **The wobble** (the owner: "The punching animation makes the body wobble too much in the third person"): gear5_pistol swung the body
  bend +13 then -17 with x/y/z shifts, the torso yaw ±10 and roll ±5, the head yaw ±15 and pitch ±8; gear5_gatling twisted torso and
  head ±10 every 3 ticks; gear5_stance keyed the head (it froze the look). nika_anims.calm keeps every arm track exactly (first person
  shows them: HakiAnimations' OWN_FIRST_PERSON_ARM lists gear5_pistol, gatling, whip, rocket, coil_release, giant_grab, giant_throw),
  drops the head and puts in: the punch body bend 0, 7 at 5 (the limb flies on tick 5), 9 at 8, 3 at 14, 0 at 22; the Gatling body bend
  7 steady and torso yaw ±3; the stance its own bend, y bob and torso yaw, half its torso roll. tools/anim_polish.py's GENERATED skips
  these four (run nika_anims.py after it).

## 8zn. Done (3.2.5): the Haki lightning on moves is the release's own again; the owner's anger about it (verbatim)

The owner, after 3.2.4 (two screenshots: (1) in third person on a hill, the player with a pale blue ball at the fist (Gear 5's lance or
a charge) and two long THIN black lines zig-zagging up into the sky from him, crossed out in red with "???"; (2) the Conqueror's release:
thick black bolts with a pale blue-white rim, kinked and branching, one tearing along the ground, ticked in green):
"Okay, this is really testing my patience. I don't know how many times I have to say this. I truly don't know how many times I have to
fucking say this. Every time you're adding lightning to my fucking abilities with the hockey on, you do the same fucking shit I tell you
not to do and you keep ignoring it. Let's see if we get this shit straight again. The lightning is to be exactly the same as the one in
Conqueror's hockey release. The one that you added for the fruits is the old lightning effects. They are not meant to be in use anymore.
In fact, they should be gone from the game entirely. And yet here they are again. But no, you keep fucking adding it. Like, I don't
understand what's the deal with you. Like, I keep telling you to add to the handoff that this lightning is not meant to be used
anymore. And yet here it is again. This has happened over 10 times by now.
I also don't understand why you replaced the fucking lightning lens [lance]. That was perfect how it was, mind you. And no one mentioned
that move to you, yet you just felt entitled to just get rid of the lightning it had and just, just fucking remove it. ... And my user
wanted the lightning for the abilities that use hotkey [Haki]. You know what? I'm just gonna give him the old piece of shit. ..."
- What it was: 3.2.2's `ConquerorStorm.BLAST_WIDTH` 0.08 (8zi) made every move's Haki lightning (ConquerorAura.mixed/exploded/gear ->
  ConquerorSparksClient -> ConquerorStorm.blast) a fifth as wide: thin black lines with no rim to see. The lance itself
  (client/haki/LanceLightning, the original's lance through it since 2.9.4) was never changed; what the owner saw at the lance is the
  move lightning every Gear 5 effect throws (ConquerorAura.gear), which had become those thin lines. Its key moved from H to N in 3.2.3
  only because the owner chose N for Coil Spring / Lightning Lance (8zk); it still fires from N with Haki in Gear 5.
- Fix (`haki/ConquerorStorm`): `BLAST_WIDTH` gone; `blast` calls the release's `bolt()` with the release's `size` and `STEP` and a `span`
  (`RELEASE_SPAN` 80): `spanWidth(walked, total, span, size)` = the release's `width(walked / span)` (so a blast bolt is the first
  stretch of a release bolt: 0.43 x size where it leaves, ~0.9 at 6 blocks, ~2 at 15) x a taper to a quarter over its last third;
  branches the same. The release itself (span 0) is unchanged. The old thin `ConquerorSparks.burst` (3.1.2's arcs, unused since 3.1.8)
  is deleted. DragonTests: a blast's bolts leave it under 0.75 blocks wide and swell past 1.0, never past the release's 3.6 x 1.35.
  Rendered from one third-person camera: the release round the player, a punch's blast 1.5 ahead and a big one 8 ahead: the same bolts
  (docs/handoff/lightning-same-3.2.5.png).

## 8zo. Done (3.2.6): the Nika fruit, part 3: the Bajrang Gun

The owner (8zj 2): "Bajarang gun ... the animation needs work ... The fist is like should be, I don't know, perhaps like more shaped like a
fist and more rounder. I'm not saying make it completely round, but like it should be a fist, a big one. And it should be coming first
from above, and then the player slowly rotates it downwards and slams it down. This is one of the strongest moves in the entire game";
8zh 8: "rotate it mid-air to like, actually aim down instead of just standing still in the arm, just doing all the work". Nothing tested
in game: build, tests, renders (docs/handoff/nika-bajrang-3.2.6.jpg).
- How the original works: B (STAR_GUN mapping) with dawn -> beginBajrang: ASCENT (climbs, BAJRANG_ALTITUDE 75), CHARGE (held aloft, the
  fist growing 2.5 to 15), release -> SLAM 46 ticks (the arm stretching down to the ground: bajrangReach), IMPACT 70, retract 26, FALL;
  clips gear5_bajrang_charge (at the start) and gear5_bajrang_slam (at the release) only. The client's `BajrangFistLayer` (a RenderLayer
  on the player model) draws the stretched arm from the right arm's pivot to `fistCentre(phase, reach, radius, flow)` (model frame: x left,
  y down, z behind; CHARGE up-out beside the head, else straight down by the reach) with `RubberLimbMesh.emit` three times (skin, sleeve,
  Haki coat), the hand cube scaled `hand` = fist / 0.25 (its half size = the fist's radius) at the arm's end.
- `gear/NikaSlam` (pure): the slam's keys T {0, 4, 10, 13, 16, 116, 142}, the fist's world way THETA {-8, -30, 165, 180, 180, 180, 180}
  (0 up, 90 ahead, 180 down) and the body's forward pitch PITCH {-12, -22, 55, 72, 70, 70, 10}, in-out sine between keys; the arm's own
  pitch = theta - 180 - pitch. tools/nika_anims.py writes gear5_bajrang_slam (142 ticks, the slam + impact + retract) and
  gear5_bajrang_charge (loop 26: leaning back 12, the arm straight up, a knee raised) from the same numbers, the body's pitch NEGATED
  (PlayerAnimator: positive leans back; the first render dived on its back); keys body pitch, both arms, both legs (a midair move: the
  owner wants the whole body in it), never the head. HudTests.nika checks the clip against NikaSlam at every key.
- `client/gear/NikaFist` + patch/PiecePatch.nikaFist (build.py PATCHED lists BajrangFistLayer): `begin(player, partial)` at the render's
  start (remembers since when each player's SLAM/IMPACT/FALL has gone on), `centre` in place of fistCentre (CHARGE: straight up over the
  shoulder, bobbing; the slam: the way theta - pitch in the turned body's frame, length k = 1.1 + radius during the swing, then the
  reach), `emitSkin` (the first emit: the arm with its hand cube at HIDE 0.5, and gear/NikaArt.fist round it through client/render/Bulk
  with the player's skin texel at the hand's end (u 50/64, slim 48.5/64, v 18/64), overlay none, an even normal) and `emitOther`.
- `gear/NikaArt.fist` (pure; FxPreview `|nfist:radius[:way]`, tools/preview/NikaShots): a ballooned fist of superellipsoid lumps: the back
  of the hand and palm, four fingers (first joints in a row, a knuckle over each, the curled tips under), the thumb across their lower
  front and its ball at the side, the wrist; cel-shaded in four bands by NikaArt.light; ~1,000 quads (fine 1,260).

## 8zp. Done (3.2.7): the Nika fruit, part 4: Dawn Whip as a cartoon spin

The owner (8zj 2): "Dawn Whip needs a complete rework. That move has to be entirely scrapped and redone. I recommend you check and get your
references for Luffy against Kizaru, where he uses that move with that name, Dawn Whip, and he actually rotates like a cartoon character. I
think that the player's feet should also rotate like a cartoon character". (No GIFs fetched: this environment allows only freesound and
opengameart; built from the description and the cartoon-run reference image 4.) Nothing tested in game: build, tests, rendered every
tick (docs/handoff/nika-dawn-whip-3.2.7.png).
- The original: C (WHIP) -> `GearServerController.whip`: a leg (alternating), dawn makes it Dawn Whip (more reach, damage, "Dawn Whip" on
  the action bar), `animate(player, "gear5_whip", 2)` for both, the rubber leg's sweep (StretchStyle WHIP: windup 5, extend 5: it hits on
  tick 10, hold 2, retract 7).
- `server/NikaRules.whipClip` (patch/PiecePatch.dawnWhip: whip's one animate call): with the original's dawn condition (Gear 5 and
  Armament or Ryou) the clip `gear5_dawn_whip`, else gear5_whip as before.
- `gear5_dawn_whip` (tools/nika_anims.dawn_whip, 20 ticks): the arms are gear5_whip's very moves (first person: `client/PieceHooks
  .firstPersonArms` shows its arms as the original shows gear5_whip's); the body coils (yaw -40), spins two whole turns (yaw to 690 by
  tick 12, fastest at the hit on 10), overshoots to 738 and wobbles back to 720, then a "constant" step to 0 on tick 20 (720 is 0 seen:
  the clip's fade-out never unwinds the turns); both legs pedal through the spin (one forward bent 74, the other back straight, swapping
  every tick), then plant.

## 8zq. Done (3.2.8): the Nika fruit, part 5: Giant holds what it grabs

The owner (8zj 2): "Giant is not holding the creatures right. Sometimes they get off the player's hand. It's overall really junky."
Nothing tested in game: build, tests, the hold rendered (docs/handoff/nika-giant-hold-3.2.8.png: the fly-in, tick 0 to 8).
- The original (`com/hexhaki/patch/GiantGrab`, javap): right-click as the giant (GiantGrabClient, Gear 5 with targetScale >= 1.05) sends
  GIANT_GRAB; `grab` takes the best living body within 6 x 4 blocks in a 0.35 cone (GearData "grabHeld" its id, "grabTicks" 0, clip
  gear5_giant_grab looping from tick 10, the arm straight out from tick 8); `tick` (from GearServerController) holds it up to 300 ticks
  and within 24 x 4 blocks: slowness, mining fatigue and weakness 255 for 6 ticks, fall reset, motion zeroed, then the body moved to
  `hand(player, held)` (the eye times 4, 0.5 x 4 forward, 0.3125 x 4 right, 0.245 x 4 down, minus half its height) with teleportTo
  (a player: connection.teleport), syncMotion; again throws it (`release`), `drop`/`clear` let go. Why it was junky: only the server
  moved it; every client drew it from the position updates (every 3 ticks for mobs, smoothed over 3 more, after the trip from the
  server), so it trailed the hand when the giant walked or turned, and it fell between updates.
- `gear/NikaHold.fist` (pure): the middle of the right fist as the player model draws it, the arm straight out (pivot -5, 2 px; the box's
  middle a pixel further out; 8 px down it; PlayerRenderer's 0.9375 and 1.501 lift; the giant's own across/up sizes round the feet).
  `reach(age)`: the fly-in (0 until tick 2, ease-out to 1 at tick 8, the clip's arm). HudTests.giantHold checks the fist against the
  model worked out box corner by corner for several turns and sizes.
- `server/GiantHold` (patch/PiecePatch.giantHold: GiantGrab.tick's `hand` call, after its `syncMotion` in tick, after `syncMotion` in grab,
  the head of `clear`; exact counts and locals asserted): `hand` = NikaHold.fist at the server's body turn, times 4; `grabbed`/`holding`
  send S2CUoFx HOLD (160: user the giant, id the body, ticks since the grab, x y z where it stood) at the grab and every 10 ticks;
  `letGo` sends LET_GO (161) when clear() drops a held body. 160 to 179 are the Nika fruit's kinds (client/dragon/UoFx routes them).
- `client/gear/NikaHoldClient` (ClientTickEvent END, after every entity has moved): each hold puts the body in the giant's fist as drawn
  this tick and last tick (both its places: xOld/xo and the position, so the frames between follow the fist exactly), the giant's sizes
  from RemoteGearStates exactly as GearBodyDeformer scales the model (scale, squash, the presence wobble), motion and fall zeroed,
  turned to face the giant; it flies in from where it stood (NikaHold.reach). The local player held is left to the server (their own
  camera). The giant's own first person: the view's turn instead of the body's (PlayerAnimator's first-person arm turns with the view).
  A hold not heard of for 30 ticks, or whose giant or body is gone, ends.

## 8zr. Done (3.2.9): the Nika fruit, part 6: Star Gun's struck body as rubber; the owner wants GIFs

The owner (8zj 2): "the Star Gun is okay. It could push the entities model a bit further and make it more like rubber, but it's good."
And during 3.2.9's session: **"I want gifs when it's all done"**: at the end of the work, send GIFs of every move changed (SendUserFile),
made from the previews (`tools/make_gif.py` turns FxPreview's frame folder, output ending in "/", into a looping GIF; kept in
docs/gifs/). Done so far: docs/gifs/nika-giant-hold-3.2.8.gif (the fly-in, then the giant turning with the body in the fist),
docs/gifs/nika-star-gun-rubber-3.2.9.gif. They are previews, not the game: say so.
- The original: `GearServerController.starHit(player, target, damage, way, hold)` (damage capped at the target's health - 1) sends
  `S2CRubberBend(id, strength = max(0.4, 1 - knockback resistance), yaw = atan2(way.x, way.z), hold = StretchStyle.STAR.hold 26, release
  17)`; `RubberBendStates` keeps it by entity (ms clock: a 120 ms ramp, the hold, the release with a faint 0.35 sin(9u) wobble);
  `RubberBendDeformer.applyBend` (RenderLivingEvent.Pre for non-players, RenderPlayerEvent.Pre for players) turned the pose to the way,
  leaned it (m12 0.55 e), scaled 1/(1 + 0.5 e) across, 1 - 0.24 e up, 1 + 1.35 e along. Only Star Gun sends it.
- `gear/NikaBend.shape(t, hold, release, strength)` (pure): the smack (2.4 ticks, out-back to 1 + 1.9 s along), the hold (quivering
  0.16 e^(-t/6) sin 1.9t and pulsing 0.08 sin 0.8t; the lean from -0.45 x 0.55 s whipping forward past 0.55 s and swaying 0.14 s), the
  let go (stretch and lean as damped springs: through a squash to rest), 14 more ticks; across = along^-0.55, up = along^-0.24 (the
  squash stands taller), pushed back 0.38 per unit of stretch. HudTests.starRubber: from and to rest, no jumps, longer than 2.6, a
  squash under 0.7, still quivering late in the hold, weaker blows bend less.
- `client/gear/NikaBendClient` (patch/PiecePatch.starRubberStates: the head of RubberBendStates.accept; starRubberDeformer: applyBend's
  body replaced by `return NikaBendClient.apply(pose, entity)`): its own hits by entity (ms clock, pruned), the pose turned to the
  blow, pushed back, leaned (one static Matrix4f, only m12 set), scaled, turned back. FxPreview `|nbend:t[:strength]`.

## 8zs. Done (3.3.0): the Nika fruit, part 7: Rubber World's ground rolling like rubber

The owner (8zh 8): "Rubber world, we need the world to actually physically bounce like rubber. This is going to be the most challenging
thing in the mod. We need to actually make the world behave like rubber, like the ground itself be manipulated so that it behaves like
it's made of like pure slime, sort of, as in like actually wave and all, like bend to the user's will." Reference image 5: a tiny Gear 5
Luffy jumping over ground rolling in soft rubbery waves. Nothing tested in game: build, tests, rendered (docs/gifs/nika-rubber-world-
3.3.0.gif: the release's rings, the rolling waves, two bodies riding them, the field ending at tick 100 and settling).
- The original: M (RUBBER_WORLD_START/RELEASE) `beginRubberWorld` (clip gear5_coil_charge, "rubberWorldHeld", charge to 60 ticks),
  `releaseRubberWorld`: c = charge / 60; radius lerp(c, 2.5, 42 + 36 x the REACH upgrade); lasting lerp(c, 100, 240) ticks; cooldown
  lerp(c, 200, 600); `GearFields.add(level, feet, radius, ticks)` (everything in it bounces: RESTITUTION 1.15, IDLE_BOUNCE 0.92,
  MAX_REBOUND 2.6; no fall damage), clip gear5_coil_release, `S2CGearFx` RUBBER_FIELD at the feet (power = the radius; GearFields sends
  it again every 20 ticks while the field lasts: GearVfx.rubberField, a photon ground ring), the sound GEAR5_RUBBER_WORLD.
- Not through Scars (the plan in 8zk said so): moving real blocks every tick would rebuild chunks on every client (the phone). The ground
  is drawn instead, as the Gura ultimate's heave draws it: copies of the world's own blocks (BlockRenderDispatcher.renderSingleBlock),
  rising out of the real ground, which never changes.
- `gear/RubberWave.rise(r, angle, t, radius, end)` (pure, 0 to 1.6 blocks, only ever up): three rings from 1.5 blocks out at ticks 0, 9,
  18 (0.75 blocks a tick, half width 2.3, heights 1.3, 0.9, 0.6, fading outward; the user's own footing within 0.9 stays still), then
  rounded waves rolling outward (length 9, 0.42 a tick, 0.6 high, sin^1.6 crests) growing in from tick 8 over 24 ticks, wobbling round
  the circle (3 and 2 lobes drifting); a 5-block rim to the edge; settles over FADE 20 ticks once ended. `slope` for the tilt.
  HudTests.rubberWorld.
- `client/gear/RubberWorldDraw` (patch/PiecePatch.rubberWorld: the head of GearVfx.play(S2CGearFx)): `play` keeps each field (a new
  middle: born; the same one again: heard; not heard for 30 ticks: ended, then settling); RenderLevelStageEvent AFTER_PARTICLES: round
  the camera nearest first (AROUND: 28 blocks, sorted once), where the ground rises, the column's top solid block (and the one under it
  above 0.6) drawn lifted and tilted about the flat axis across the way out by 0.6 x atan(slope) (a rise outward faces its top inward);
  at most MOST_DRAWN 520 blocks a frame, at most 360 new column lookups a frame, the columns kept on a grid round the field (no
  allocation a frame). RenderLivingEvent.Pre (LOWEST): every living body within 6 blocks of the field's height is drawn lifted by the
  ground under it (not one's own first-person pass); a translation only, inside the dispatcher's own push. FxPreview `|nworld:t[:radius[:end]]`.

## 8zt. Done (3.3.1): Toon Force; Dawn Whip and the Gear 5 jump pose redone to the owner's target look; the owner's new jump request

The owner during this session (verbatim, in order):
1. "I want gifs when it's all done" (send GIFs of every move changed when the work is done: SendUserFile; they are previews, say so).
2. On the Toon Force GIF's Dawn Whip and Sky Bounce cells: "not a fan of these 2 animations ngl".
3. "this is the target look" (image: Gear 5 Luffy mid-skip, from a three-quarter view: leaning back laughing, head thrown back, one
   arm raised with the fist up beside and above his head (elbow bent), the other arm swung down and back with a clenched fist, one
   knee driven high in front with the foot reaching forward, the other leg bent with the foot kicked up behind, white clothes, purple
   sash, white cloud hair).
4. Circling the first try's raised fist (drawn over the face in the side and three-quarter views) with question marks; then "the arm
   bro, look at the arm"; then, circling the second try's fist (beside the head but drawn over its corner from the three-quarter view):
   "how the FUCK is that a pass". (The old check, tools/anim.lint, only asked whether a forearm goes THROUGH the head in 3D; a fist a
   pixel beside the head passed. Never call a pose fine from a 3D test: look at the picture, and run tools/anim.face_covered.)
5. **NOT DONE YET, the next version (3.3.2), the owner insists**: "also one change I insist shift + space ability is removed, default
   jumping is much higher and always does the jump animation we're doing, it always turns the ground into rubber for the player using
   the fruit, basically world of rubber on every jump, world of rubber makes it so for everyone else, it's main goal is to confuse the
   target, displace them and overwhealm". Read as: (a) Sky Bounce (Shift+Space, GearServerController.skyBounce, the AbilityHud row,
   the Logbook entry) removed; (b) the fruit user's normal jump much higher and always playing gear5_bounce (the skip clip of 3.3.1);
   (c) every jump of the fruit user turns the ground under them to rubber (a small Rubber World: client/gear/RubberWorldDraw's waves
   round the landing and the take-off, gear/RubberWave; the bounce only for the user); (d) Rubber World (M) then works on everyone
   else: its goal "to confuse the target, displace them and overwhelm" (throw the others about on the waves: knocked up and sideways
   by the crests, their movement scrambled, nausea-like confusion; the user unaffected). Not decided: Gear 5 only or any time with the
   fruit (Sky Bounce was Gear 5 only; "for the player using the fruit"): ask in one short question if unsure, or take Gear 5 only and
   say so. Then 3.3.3 and on: the rest (better Haki versions: Dawn Pistol; never touch the lightning lance; GIFs).
   **Asked in 3.3.2's session (AskUserQuestion), the owner chose: "Gear 5 only" (untransformed the jump stays normal) and "About 5
   blocks" for one tap of Space (holding Space keeps bouncing on every landing).** 3.3.2 is written up in 8zu.
- Toon Force (gear/ToonForce, client/gear/NikaToon, patch/PiecePatch.toonClips: the head of HakiAnimations.playRemote(id, clip,
  priority) tells NikaToon.clip): each Gear 5 clip's beats (tick, kick: + squashes short and wide, - stretches tall and thin) kick a
  per-player spring (PULL 0.3, DAMP 0.2 a tick, within +-1.2); RenderPlayerEvent.Pre (LOWEST) scales the body round the feet (tall
  1 - 0.3 x, wide 1 + 0.26 x), Post pops; not in the first-person pass. A loop's beats after its returnTick come again each period.
  None on gear5_awaken or the lance. HudTests.toonForce. tools/preview/ToonDump prints the spring for tools/clip_gif.py.
- tools/clip_gif.py: GIFs of clips (tools/anim's box player, half a tick a frame, --toon, several clips in a grid); tools/make_gif.py:
  GIFs from FxPreview frame folders. docs/gifs: Giant's hold (3.2.8), Star Gun (3.2.9), Rubber World (3.3.0). The Toon Force grid GIF
  was made with the old Dawn Whip and jump clips and deleted: make it again (pistol, rocket, dawn whip, giant throw, gigante laugh,
  bounce, with --toon) and one of the two redone clips.
- tools/nika_anims.py `target(fist, lean, knee)`: the skip (body pitch +lean (back), bend -12; the fist arm arm(30, 0, 135, -30): up and
  out to the side, a little back, so the hand is beside the head and never over the face; the other arm(66, 0, 30, -30) mirrored;
  the front knee leg(knee, 0, 34), the back leg leg(34, 0, 96)). gear5_dawn_whip (20 ticks): wind-up, two turns with the arms out and
  a little down (roll 62: they sweep under the head as the body spins) and a knee tucked, overshoot, the fist raised by the side (an
  arm(34, 0, 82) key first), the skip from 16, the arm lowered by the side at 19, the yaw stepped 720 -> 0 at 20. gear5_bounce (14
  ticks, now OWNED; anim_polish leaves it alone): crouch, a star-jump launch (arms up by the sides, arm(24, 0, 150)), the skip from 7,
  lowered by the side at 12, rest at 14. Toon beats retimed for both.
- tools/anim.face_covered(pose): in the views front, three (35), threeleft (-35), side (90), sideleft (-90), is a hand (the last two
  thirds of a forearm's box) drawn over the face (the head's front half, shrunk half a pixel) and in front of the head? It flags both
  poses the owner circled. build.py checks FACE_CLEAR_CLIPS (gear5_dawn_whip, gear5_bounce) at every quarter tick. Mod-wide it would
  flag 183 clips (guards and charges hold arms before the face on purpose): use it for any new pose with a raised arm.

## 8zu. Done (3.3.2): Gear 5's rubber jump, Rubber World on everyone else, the skip pose redone after the owner said it was STILL wrong

Asked first (AskUserQuestion), the owner chose for the new jump: "Gear 5 only" and "About 5 blocks" (8zt item 5). Then, while the jump's
clips were being made, the owner (verbatim, in order):
1. "THE POSE IS STILL WRONG, how are you messing this shit up" (the same anime picture again: Gear 5 Luffy mid-skip, see 8zt item 3).
2. "Even a fucking stick figure is easy to make dammit", with a stick figure (docs/handoff/nika-skip-stick-3.3.2.png, the owner's own
   drawing): a box head; a level shoulder line; on one side the upper arm level and the forearm straight UP (the fist at about mid-head
   height, beside the head); on the other side the arm hanging straight DOWN with the hand hooked in; on the raised arm's side the thigh
   level forward and the shin straight down; the other thigh straight down and its shin straight back, level. Body upright.
3. After the session rendered a right-angle version (front: the upper arm level out to the side, a little back, forearm up beside the
   head; side: front thigh level, shin down, back shin kicked straight back) and began reworking Dawn Whip's way into the skip:
   "NO NO NO NO NO NO NO NO NO FUCK, JESUS fuck off."
4. "the  right arm is meant to be pointing forward NOT THE SIDE"
5. A silhouette of Gear 5 Luffy skipping across a full moon, the anime picture again, and the owner's description of it, limb by limb
   (verbatim): "suspended high in mid-air in an extremely dynamic, playful, and exaggerated jumping pose, as though joyfully skipping or
   running through the sky. His entire body forms a loose, asymmetrical, almost S-shaped silhouette, with his chest pushed forward, his
   back slightly arched, and his limbs spread dramatically in different directions. Head and expression: His head is tilted slightly
   backward and turned toward the left side of the image, with his chin lifted. ... Right arm (upper-left of image): His right arm is
   raised high above his head, bent sharply at the elbow. His upper arm extends diagonally upward from his shoulder toward the left side
   of the image, while his forearm angles upward and toward the right, ending in a tightly clenched fist above his forehead. The raised
   arm creates a powerful, celebratory pumping motion, resembling someone triumphantly dancing or running. Left arm (right side of
   image): His left arm extends outward and backward from his shoulder, with the elbow projecting prominently toward the right side of
   the image. His forearm then bends downward almost vertically, ending in a relaxed but clenched fist hanging approximately at waist
   level. This produces an exaggerated running-arm silhouette that counterbalances his raised right fist. Torso and waist: His chest is
   pushed outward and slightly upward, with his shoulders thrown back and his abdomen exposed. His upper body is twisted subtly toward
   the viewer, allowing a clear view of his chest despite his head turning sideways. His spine curves naturally backward, while his hips
   rotate slightly in the opposite direction, creating a springy, elastic sense of motion. ... Right leg (left side of image): His right
   leg is thrown dramatically forward and upward toward the left side of the frame. His thigh rises high, nearly horizontal, while his
   knee bends prominently. His lower leg angles downward and farther left, with his sandal-covered foot extending toward the
   bottom-left corner. The leg forms a huge, exaggerated kicking or skipping motion, with the knee raised higher than the hips. Left leg
   (lower-right of image): His other leg trails underneath and behind his body, with the thigh descending diagonally from the hips. His
   knee bends deeply, and his lower leg curls backward toward the right side of the image. His foot is lifted upward behind him, with
   the sandal hanging at an angle and the sole nearly vertical. This leg forms a loose, bouncing curve that counterbalances the
   forward-raised leg. Overall silhouette and motion: The pose resembles a single frozen frame of an impossibly energetic cartoon dance.
   One fist is thrust triumphantly upward, the opposite fist hangs downward, one knee is kicked high forward, and the opposite leg curls
   backward. His limbs are asymmetrical rather than mirrored, creating the impression that he is skipping through the air rather than
   flying or performing a conventional jump."
   What 3.3.1 (and the first 3.3.2 tries) had wrong: the raised arm went out to the SIDE (it goes up and FORWARD), the LEFT leg was the
   front one (it is the RIGHT, the raised fist's side), the low arm was swung down-back with the elbow soft (the elbow juts back, the
   forearm hangs straight down to the waist), the back shin did not curl up behind.
   Built (tools/nika_anims.target, used by gear5_dawn_whip, gear5_bounce, gear5_jump): rightArm arm(-135 - pump, 8, 0, -50) (upper arm
   45 degrees up and forward, 8 out so the fist stays beside the head from the front even through Dawn Whip's last turn; forearm
   straight up; the fist at the head's top: a Minecraft arm cannot reach above the head with the elbow bent), leftArm
   mirror(arm(60, 0, 18, -60)), rightLeg leg(-100, 0, 60), leftLeg mirror(leg(30, 0, 110)), body pitch 10 (back), bend -12 (chest out).
   Into the skip: the launch shoots the right arm up, a little out (arm(-160, 0, -20, -8): up by the side it swung across the face);
   Dawn Whip goes through arm(-90, 20, 0, -20) at 15; out of it the arm comes down in front (LOWERED arm(-40, 0, 0, -30)). build.py's
   face check is the front view only now (from the side and three-quarters the fist is up in front of the head, as the owner wants).
   Rendered beside the owner's pictures (front, both three-quarters, side): shown to the owner for a yes before the jump's code.

The owner then said "Continue" (the pose render had been sent; no other verdict). 3.3.2 as built (nothing tested in game: build, tests,
every clip and the ground rendered; GIFs docs/gifs/nika-jump-3.3.2.gif, nika-toon-force-3.3.2.gif, nika-rubber-jump-3.3.2.gif,
nika-rubber-world-others-3.3.2.gif):
- **Sky Bounce gone**: patch CALL_REDIRECTS: GearMovement.movementInput's GearClientState.usable(SKY_BOUNCE) -> client/gear/NikaJumpClient
  .usable (never Sky Bounce: Shift + Space is a plain jump); GearServerController.mechanicsAction's and skyBounce's GearData.unlocked ->
  server/NikaRules.unlocked (never Sky Bounce). The panel row (AbilityHud.gearKeys), the Ability Book row (LogbookData.nika, plus a line
  on the jump), UiPreview, gearFiveOnly updated; TEXTS rewords GearUpgrade BUOYANCY ("Gear 5's bounding steps spring higher, with a
  softer, floatier descent."; it still raises the original's walk hops). build.py PATCHED: GearMovement, GearFields, GearUpgrade.
- **The jump** (gear/RubberJump pure: LIFT 0.48, `lifted(vy)`; HudTests.rubberJump: about 4.9 blocks): client/gear/NikaJumpClient on
  LivingEvent.LivingJumpEvent (new stub + allowlist line, [forge] 1.20.1: posted by ForgeHooks.onLivingJump inside jumpFromGround after
  the speed is set) for the local player in Gear 5 (not transforming, giant scale < 1.05, no Bajrang phase, not flying, riding or in
  water): vy + LIFT, C2SNikaJump (network message 20, PROTOCOL 14) and its own take-off ripple at once; its own landing ripple from a
  ClientTickEvent once on the ground. server/NikaJump.jumped (Gear 5, not transforming, not flying or riding; one per 5 ticks): HOP
  (S2CUoFx 162) to everyone near, gear5_jump unless busy (gatling, rocketing, coilHeld, lanceHolding, giantHeld, gigante,
  rubberWorldHeld, bajrangPhase, grabHeld), a JUMPS entry; PlayerTickEvent: on the ground 3+ ticks after: LAND (163), then gear5_land
  (or gear5_run when "bounding"), after LAND_TICKS 8 the idle track (gear5_stance / gear5_run / __clear__, the original's idleTrack);
  LivingFallEvent cancelled while an entry lasts. HakiServerController.animate's head calls NikaJump.played (another clip took over:
  the landing leaves it alone); tickBouncyWalk's two animate calls go through NikaJump.walkClip (run/stance/clear held back while the
  jump's clip plays). ToonForce beats for gear5_jump (loop) and gear5_land.
- **The rubber ground** (gear/RubberJump.hop, only up, 0 to ~1): take-off: the ground under the feet springs up 0.55 for 3 ticks, two
  rings (0.7 and 0.35 high, from 0.7 blocks at 0.55 a tick) to HOP_RADIUS 5.5; landing: two rings (0.8, 0.4) to LAND_RADIUS 4.5, the
  feet's footing still; LIFE 18 ticks. client/gear/RubberWorldDraw: HOPS (MOST_HOPS 10) beside Rubber World's FIELDS, drawn first, the
  same block copies (height/slope by kind); `accept` ignores one's own (drawn at once); bodies ride only when on the ground now.
  client/dragon/UoFx routes 162/163 there. FxPreview `|nhop:t[:1]` (about 400 quads at most).
- **Rubber World on everyone else** (server/RubberChaos): releaseRubberWorld's GearFields.add -> RubberChaos.add(level, centre, radius,
  ticks, player) (PiecePatch.rubberChaos; it still calls GearFields.add: no fall damage in the field, the clients' RUBBER_FIELD every
  20 ticks); GearFields.bounceWithin emptied (PiecePatch.fieldsQuiet: it bounced everyone, the user too). LevelTickEvent: per field one
  box query; a body (not the user, on the ground, not riding or flying) where RubberWave.rise climbs at least TOSS_RISE 0.04 in a tick
  is thrown (RubberWave.toss/tossUp/tossOut/tossSide: up 0.5-0.95, out 0.1-0.3 but back toward the middle 0.8 as hard one time in
  BACK_CHANCE 0.35, sideways 0.2-0.5 at random; one throw per TOSS_GAP 6 ticks), its fall reset; a Mob stops, forgets its target and
  spins 90-180 degrees; a Player gets nausea 140 ticks. FxPreview `|nchaos:t[:radius]` runs the same rule on eight bodies (the first try
  threw them outward only: a 16-block field was empty in 50 ticks; now they tumble about inside it).
- First person: the jump and landing clips hide the arms as every non-listed Gear 5 clip does (the stance already does).

## 8zv. The owner's new request during 3.3.2 (verbatim): three new common fruits, Horo Horo, Noro Noro, Supa Supa

With three zips of reference pictures (kept only in the session's scratchpad: anime frames and the series' fruit art are never put in
the repository): Horo: Perona with Negative Hollows (round white see-through ghosts with tongues out, wispy tails), the Toku Hollow (a
giant round grinning ghost), the fruit (cream-yellow lobes with ghost faces, curled tips); Noro: the fruit (purple oval with swirls, a
green ring for a stem), Foxy shooting the Noroma photons (pink-violet concentric rings out of the hands); Supa: Spiral Hollow (white
drill blades round the forearms), Daz Bones' arm blade (steel with a wavy edge, from the forearm) and finger blades, the fruit (blue
and white crescent blades).

"New common Fruits:
(simple reused animations already in the mod).

Horo Horo No Mi (common):

Core Abilities

* Negative Hollow: Intangible flying ghosts that pass through targets and plunge them into extreme depression and lost willpower.
* Mini Hollow / Ghost Bomb: Small, tangible ghosts that latch onto targets and detonate on command.
* Toku Hollow (Special Ghost): Large, tangible ghosts that create massive shockwaves and explosions when detonated.
* Ghost Doppelgänger & Astral Projection: Lets the user project a floating ghost body or separate their spirit to scout, fly, and speak
  remotely.
* Ghost Network: Links the user's consciousness to multiple ghosts for surveillance and information gathering. [1, 2, 3, 4, 5]

Noro Noro No Mi (Common)

The Noro Noro no Mi is a Paramecia-type Devil Fruit in One Piece eaten by Foxy. It allows the user to emit "Noroma" photon beams that
drastically slow down whatever they hit for 30 seconds. [1, 2]
Core Mechanics

* 30-Second Slowdown: Targets hit by Noroma photons—including people, objects, or projectiles like cannonballs—move in extreme slow
  motion for 30 seconds before returning to normal speed. [1, 2, 3]
* Delayed Kinetic Force: While a target is slowed, kinetic energy from strikes accumulates without immediately taking effect, then
  releases all at once when the 30-second timer ends. [1, 2, 3]
* Tactical & Environmental Uses: Slowed objects or opponents can be used as temporary platforms or obstacles, requiring good timing from
  the user.

Supa Supa No Mi (UnCommon):

Core Abilities

* Blade Transformation: Turns any body part—such as fingers, arms, legs, or the entire body—into sharp steel blades.
* Slashing Immunity: Grants steel-like hardness, making the user immune to standard blade and slash attacks.
* Ranged & Spinning Cuts: Enables flying slashes, spinning saw or drill-like blades, and cutting force without direct physical contact.
* High-Speed Mobility: Lets the user sprout blades from their feet to skate or slide rapidly across surfaces. [1, 2, 3, 4, 5, 6, 7]

Signature Techniques

* Spar Claw: Turns the undersides of the fingers into slashing blades.
* Spider: Hardens the body for heavy defense.
* Atomic Spar: Delivers devastating X-shaped slashes.
* Spiral Hollow: Forms spinning, drill-like blades on the forearms.
* Atomic Spurt: Combines blade-footed skating with high-speed strikes.
* Sparkling Daisy & Spar Break: Additional offensive blade techniques. [1, 2, 3]

(haki blackens the blades as in makes them purpleish like the katanas).

Search for references yourself (gifs) but I attached some looks of the fruits 2d and looks of moves."

Read as: three new fruits in the Devil Fruits tab, Horo and Noro COMMON (one star), Supa UNCOMMON (two stars); animations reused from
the mod's own clips (no new clip work beyond picking and, if needed, small variants); POINT ONE still holds for everything else (3D
meshes in the house style, first-person hand moves, CC0 web sounds, the Conqueror's lightning out of every explosion with the
Conqueror's Haki and Ryou on); Supa's blades with Armament (Haki) on go black-purple like the katanas' Haki colour. The plan and the
owner's answers follow in 8zw.

## 8zw. The plan for the three fruits and the owner's answers (AskUserQuestion, 3.3.2's session: every option "As proposed")

Order: **one fruit per version: 3.3.3 Noro Noro, 3.3.4 Supa Supa, 3.3.5 Horo Horo.** Keys as every fruit (the attack button the game's
punch unless said, RMB the use button, Z X C V B, N via client/FruitKeyN). The owner accepted these layouts as written:

Horo Horo no Mi (Perona, Common):
- LMB the game's punch. RMB **Negative Hollow**: tap one ghost, hold for a stream (up to 5); they fly through bodies; whoever they pass
  falls to their knees in despair ~6 s: can't attack, barely moves.
- Z **Mini Hollow (Ghost Rap)**: 4 little ghosts latch onto the nearest foes; Z again detonates them (or after 6 s).
- X **Toku Hollow**: a giant grinning ghost floats where you look; X again, or touching someone or a wall: a huge shockwave explosion.
- C **Astral Projection**: the spirit leaves the body and flies as a see-through ghost (not through walls), unseen by mobs, up to 20 s;
  the body stays behind, helpless; C again or time up: back in it.
- V **Ghost Network**: 6 ghosts spread out round the user for 30 s; every living thing within 48 blocks glows (seen through walls).

Noro Noro no Mi (Foxy, Common):
- LMB the game's punch. RMB **Noroma Photon**: tap a burst of pink rings, hold a steady beam; anything hit moves in extreme slow motion
  for 30 s: mobs, players, and arrows or fireballs hang in the air.
- Z **Noroma Spread**: a wide fan of rings in front. X **Noroma Mirror**: a mirror in front for 5 s that sends projectiles back slowed
  (and one's own beam on). C **Time's Up**: ends every slowdown the user put on now, releasing everything stored at once. V **Slow
  Field**: a dome of photons round the user for 8 s; whatever enters it is slowed (projectiles stop in the air).
- Passive **Delayed Kinetic Force**: blows on a slowed target do nothing yet; they add up and all hit at once (damage and knockback)
  when its slowdown ends.

Supa Supa no Mi (Daz Bones, Uncommon; with Armament on the blades go black-purple like the katanas):
- LMB **Spar Claw**: finger blades, a combo of claw swipes. RMB **Flying Slash**: a crescent of cutting force from the arm blade; hold
  to charge a bigger one.
- Z **Atomic Spar**: an X-shaped double slash with both arm blades. X **Spiral Hollow**: drills spin on both forearms; a dash forward
  drilling through everything in a line. C **Atomic Spurt**: skate on foot blades at high speed for a few seconds, slashing whoever is
  passed. V **Spider**: the whole body hardened steel for 8 s: blows mostly bounce off, no knockback, bare-handed attackers cut, the
  user slower. B **Sparkling Daisy**: a lunge through the one looked at, both arm blades out. N **Spar Break**: the whole body a
  spinning blade, cutting all round.
- Passive **Slashing Immunity**: swords, axes and blades cannot cut the user unless coated in Armament Haki.

## 8zx. Done (3.3.3): Rubber World's waves slow and of different sizes

The owner, during the Noro fruit's start (verbatim): "Rubber world's rippes should  be different sizes some much bigger than others, and
slower, they're too fast, going way too fast. they're meant to be like slow waves". Shipped at once as 3.3.3 (so **the fruits moved
one version on: 3.3.4 Noro Noro, 3.3.5 Supa Supa, 3.3.6 Horo Horo**). Nothing tested in game: build, tests, renders over a field's life
(docs/gifs/nika-rubber-world-3.3.3.gif, nika-rubber-world-others-3.3.3.gif at real speed).
- gear/RubberWave.rise: the release's three rings each their own (born 0, 14, 30; heights 1.5, 0.6, 1.1; half widths 3, 2, 2.6;
  0.3, 0.24, 0.27 blocks a tick: they went 0.75); then a wave born round the user about every WAVE_EVERY 45 ticks from tick 40 (jittered),
  each its own height (WAVE_LOW 0.35 to WAVE_HIGH 1.9, squared so a few are much bigger: `waveHeight(i)`), half width (1.8 to 3.6) and
  speed (0.12 to 0.2 a tick: the old rolling waves went 0.42), rising out of the feet over 10 ticks, rolling outward, wobbling round the
  circle; none born after the field ends; MOST 2.0 (the two drawn blocks, no gap). The first try (a wave every 22 ticks, half widths
  2 to 4.5) overlapped into one raised plateau by tick 180: spaced out. RubberChaos throws on the same waves (only the big swells rise
  fast enough). HudTests.rubberWorld: the first ring moves 1.5 to 3.5 blocks in 8 ticks, heights differ more than 2.5 times, the waves
  at most 0.2 a tick. The jump's ripples (RubberJump) unchanged (the owner named Rubber World).

## 8zy. Done (3.3.4): the Noro Noro no Mi (first pushed unchecked at the owner's word, finished in the next session)

3.3.3 (8zx) is pushed and green (Build #169, Release #124). The session then moved onto the personal HexPiece environment
(env_016v9rNSffz2fds2ha514nhY: get_environment, switch_environment, network_only) and began the Noro fruit (8zw's keys). Pushed at the
owner's word WITHOUT a final local build or a CI check: **first check the branch's "Build HexPiece" run; if red, fix it.** The last
local build that passed (EXIT 0, 13 suites) was before these were changed: HudTests.ranks (NoroKit.MOVES, the Noro fruit Common),
DragonTests.noroPhotons (budget, mirror geometry), SwordTests (Noro's sounds: the "registered" list and a check of each borrowed clip),
sounds.json and en_us.json (Noro's events and subtitles), NoroArt's bolder rings. build.py VERSION is already 3.3.4; no release notes
yet (so only the build-<branch> pre-release changes).

Built (all in the tree):
- **Enum** (PiecePatch.uoFruit): NORO {"noro", "Noro Noro no Mi", "Slow", "C77DE0", lore}, ordinal 6, not a Logia. FruitRanks: COMMON.
  HexPiece.init registers NoroKit. en_us "§fNoro Noro no Mi". Item model and icon (tools/noro_icon.py: a tilted purple oval with
  swirls and a green figure-8 stem, after the owner's reference; judged against it). build.py lists both; DragonTests checks them.
- **server/NoroKit** (RMB Noroma Photon, Z Noroma Spread, X Noroma Mirror, C Time's Up, V Slow Field, passive Delayed Kinetic Force),
  **server/NoroMoves** (S2CUoFx kinds 180-192: SHOT, POP, BEAM_ON/OFF, SLOW, UNSLOW, STORE, RELEASE, MIRROR/MIRROR_OFF, FIELD, REFLECT,
  SNAP; client/dragon/UoFx routes 180-199 to client/noro/NoroFx): RMB tapped (< 5 ticks) a burst (1.8 a tick, 36 blocks, the first
  body or other's projectile it passes slowed, POP; bounces off mirrors), held the beam (up to 70 ticks, 30 blocks, off mirrors,
  everything along it slowed; clients trace it themselves from the user's look); Z seven bursts across 70 degrees; X a mirror 2.2 ahead
  facing the user for 100 ticks (noro/NoroMirror, pure: projectiles crossing it reflected and slowed); C Time's Up (every slowdown the
  user put on ends, the stored blows land); V a dome radius 6 for 160 ticks slowing whatever enters. Clips reused (the owner: "simple
  reused animations already in the mod"): hie_partisan_launch (tap), pika_laser_charge / pika_laser_end (beam), hie_pheasant (spread),
  hie_ball_gather (mirror), hie_partisan_raise (time's up), hie_ice_age (field). FruitEnergy COSTS noro_photon 20, noro_spread 25,
  noro_mirror 25, noro_timesup 20, noro_field 40. The Conqueror's lightning: ConquerorAura.exploded at every burst's catch, each newly
  slowed body on the beam, and every release (POINT ONE).
- **server/NoroSlow**: 600 ticks (30 s); a living body's motion cut to 0.2 every tick at LivingTickEvent (crawls, falls slowly), plus
  slowness 5, weakness 1, slow falling (a player moves themself); a Projectile (proven: getOwner, motion, setNoGravity, setPos) hangs,
  crawling at 0.04 of its speed without gravity, then flies on at full speed; Delayed Kinetic Force: LivingAttackEvent (HIGH) on a
  slowed body with an attacker is cancelled and stored (damage, push 0.35 a blow, a STORE flash and a thud); at the end or Time's Up
  all land at once (capped at 60 % of its most health and 40, push at most 2.6), RELEASE drawn, the release sound. At most 64 slowed.
- **noro/NoroArt** (FireMesh rings, shells, rays: shot, pop, beam with rings swelling toward the far end, aura, slowShot, store,
  release, mirror, dome, snap, reflect), **client/noro/NoroFx** (draws all of it; the slowed shots' crawl kept on every client; the beam's
  hum through LoopSound; the punch given back), **client/noro/NoroClient** (ClientKits). FxPreview shots in tools/preview/NoroShots
  (|nshot |nbeam |naura |npop |nstore |nrelease |nmirror |ndome |nsnap), judged: docs/gifs/noro-photons-3.3.4.gif.
- **Sounds**: Freesound refused this session (HTTP 403 from freesound.org itself through the proxy; the web-fetch reader could not
  resolve it either). So noro/NoroSounds' seven events play CC0 recordings the mod already has, each at its own pitch in sounds.json:
  photon pika/zap1-2 (0.8), beam gura/charge loop (1.3), slow uo/air2 (0.7), store pika/hit1-2 (0.7, volume 0.6), release pika/pop1
  (0.9), mirror pika/sparkle1 (1.15), field pika/travel1 (0.8). Nothing made or mixed by hand. **When Freesound answers, give the fruit
  recordings of its own** (tools/noro_sounds.py as tools/hie_sounds.py, a sounds/piece/noro folder with SOURCES.txt, SwordTests
  webSounds("noro", ...)), and tell the owner the borrowed ones were a stopgap.

Finished in the next session (nothing tested in game: build, 13 suites, renders, sound measurements):
- The unchecked push's Build HexPiece #170 was green; the local build too.
- First person (tools/preview/HandPreview over the six reused clips: docs/handoff/noro-hands-3.3.4.jpg): every hand on screen, attached
  low right, never over the view. The reused clips' effects are keyed on network kinds, never on clip names (no ice orb, no light glow
  on a Noro user). The panel (UiPreview hud-8, rows from NoroKit.MOVES as AbilityHud.kit makes them: docs/handoff/noro-panel-3.3.4.png)
  reads RMB Z X C V and the passive "-". The effects re-rendered and judged (FxPreview, the busiest the dome at about 1,000 quads).
- Stress-test fixes: **the mirror turns a fireball's power too** (AbstractHurtingProjectile xPower/yPower/zPower f_36813_/14_/15_,
  proven in verify/api-allowlist.txt from joined.tsrg byv C_1153_ + intermediary class_1668; srg-shapes regenerated): reflected
  without it, a ghast's fireball turned straight back at the user. **NoroSlow.clear** on NoroKit.reset (ServerStoppingEvent, before
  the save): every slowed shot given back its gravity and speed (none saved hanging forever), SLOWED emptied (no stale entities kept
  across worlds). **Crowd sounds capped** (NoroMoves.sounds: at most 2 SLOW per 4 ticks and 2 RELEASE per 6 ticks per user): a Slow
  Field or Time's Up on a crowd no longer stacks twenty 2 s explosions.
- **Sounds: the fruit's own** (tools/noro_sounds.py, sounds/piece/noro, SOURCES.txt; SwordTests webSounds("noro", ..., 11)): photon1
  reg7783 "release_air_pffft" 131302, photon2 wubitog "Steam/hiss" 234782 (from 0.8 s), beam (loop) brunoboselli "Air (or steam)
  pressure release" 457294, slow1 Kinoton "Deep Whoosh #5" 558827, slow2 oMzig "Deep Whoosh Speed Ramp" 789075, store1 insanity54
  "body_hit" 276600, store2 NeoSpica "Muffled thud" 512241, release1 Electroviolence "Swoosh Explosion" 234555 (from 0.6 s), mirror1/2
  emptysound "twistedwooshes-plastic5" 205989, field1 SukritSen "Steam" 398077. sounds.json plays them at pitch 1 (the borrowed clips'
  pitches gone). **The pitch check that works** (the old tonal.py was lost with its scratchpad): the long-term spectrum (8192-point
  FFT, half overlap, 40 Hz to 12 kHz), each bin's dB over the median of its 121 neighbours, the top line: plain noise about +3.5 dB,
  the Ice clips the owner accepted +4.4 to +13.4, these +4.4 to +9.8 AS CUT (measure the cut clip: the slow-motion sound whined at
  11.7 kHz +33 dB, the steam hiss's first half second hummed at 199 Hz, the explosion's start had a 1.26 kHz line). The borrowed clips
  were pitched (the sparkle 60% of its energy in lines, the Gura charge a hum). Loudness (ebur128 integrated): bursts -15.5, release
  -13.9, slowing -15 and -20, mirror -18.5/-20.2, dome -16.8, beam -18.1, thuds -20.4. The Freesound CDN is slow (about 30 s a file,
  some 502s): fetch in parallel with retries.
- The owner, when this was ready (verbatim): "no need for gif, proceed to the next fruit". So no GIF for 3.3.4; on to Supa Supa.

## 8zz. Done (3.3.5): the Supa Supa no Mi; the owner on the first blade renders, the blades made of katana steel

Wired (in the tree, not yet built as a whole): PiecePatch SUPA {"supa", "Supa Supa no Mi", "Blade", "8FB3DA", lore} ordinal 7, not a
Logia; FruitRanks UNCOMMON; HexPiece.init registers server/SupaKit (not written yet); PieceSounds registers supa/SupaSounds (SWISH, DRAW,
CUT, GLANCE, RIP, SKATE loop, SPIN loop: names only, no clips yet); UoFx routes kinds 200-219 to client/supa/SupaFx (not written
yet); en_us "§aSupa Supa no Mi" and the subtitles; the icon (tools/supa_icon.py: a blue fruit with white crescent blades in a pinwheel,
a stem and a leaf) and item model; build.py VERSION 3.3.5 and NEW_PREFIXES. Found for the moves: FruitKit.blocks(player, source)
(the original calls it when the fruit is active and not in the sea: cancels the attack) and FruitServer.hakiInfused(source) (the
attacker has Armament or Ryou on) for Slashing Immunity; SwordItem, AxeItem (HexGhoul's list) and the mod's KatanaItem for "a blade";
NikaToon squashes ANYONE who plays a gear5_ clip (never reuse gear5_dawn_whip for another fruit); the reused clips chosen by render:
pika_sword_1/2 (Spar Claw), hie_partisan_raise then hie_partisan_throw (Flying Slash), pika_murakumo (Atomic Spar: arms cross then
sweep out), hie_road_slide then hie_road_punch (Spiral Hollow; the slide for Atomic Spurt), hie_partisan_launch (Spider), pika_lunge
(Sparkling Daisy), gura_tilt (Spar Break: arms out wide while a ring of blades spins).

The owner on the first renders (flat single-colour polygons: supa/SupaArt's armBlade, claws, drill, steelBox; tools/preview/SupaShots),
verbatim: "the arm blade looks really bad", then "it honestly all looks really poor and poorly made." Asked (AskUserQuestion), the owner
chose **"Katana steel"**: every blade made from the mod's own katana steel (the owner's lit, textured blade mesh with its temper line),
reshaped broad with Daz's wavy edge, so Haki coats it black-purple exactly as the katanas; the flying cuts redone in the Light/Gura glow
style; rendered beside a katana for the owner's yes before any move code. Built for that: tools/supa_view.py (sword_view/sword_rig: the
figure and the katana steel, textured and lit as the game does): a katana's 01_Blade part reshaped (per 0.5 px band its spine and edge;
x' = spine * curve - d * widen - wave * sin(2 pi y / period) * u^2, y' = y * length, z' = z * thick), set on the forearm (ARM_AT, turned
ARM_TURN 30 degrees round the arm), the coat multiplied toward (62, 34, 88). Chosen: Silver Crane's steel, widen 4.2, thick 2.6, length
0.82, wave 0.45, period 7.5, curve 0.4 (Azure Tide blue-grey and Blackwater dark compared). Render: docs/handoff/supa-arm-blades-3.3.5.jpg
(bare and with Armament, beside the katana), sent to the owner: **"Yes, build it"**.

Built (nothing tested in game: build, 13 suites, every blade and cut rendered and judged, every sound measured):
- **The steel** (client/sword/KatanaMesh.shaped: a katana's 01_Blade quads reshaped by supa/SupaBlades.Shape, cached per katana, level
  of detail and shape; KatanaMesh.box: a box skinned with a small window in the middle of the steel's flat; KatanaRenderer.steel and
  steelBox: drawn through the katana's own quads / coated / highlight, so lit, textured and coated as the katanas, BladeCoatClient's
  gradual coat). Silver Crane's steel. client/supa/SupaClient.renderBody puts it on the limbs (the left arm mirrored): ARM (ARM_AT,
  ARM_TURN 30), CLAW x4 out of the fist (CLAW_X, CLAW_Y, CLAW_FAN), DRILL x6 round the forearm (DRILL_AXIS, OUT 2.5, LEAN 11 in, TWIST
  115: flats along the cone's surface, a thread's slant), SKATE under each foot (Rx -90 Ry 90: blade forward, edge down), Spider's
  steel skin (every part's box grown SKIN), Spar Break's 8 arm blades round the body's middle whirling (SPIN_*). firstPerson: the
  same on the game's own arm (LightArt.FIRST_PERSON_GRIP's first 8 steps; FirstPersonHand's move is already on the pose). Renders:
  docs/handoff/supa-blades-3.3.5.jpg (every move's blades on its clip's pose, bare and coated), supa-first-person-3.3.5.jpg.
  tools/supa_view.py mirrors SupaClient step by step (--parts, --first); DragonTests.supaCuts checks its ARM numbers equal SupaBlades.
- **The cuts in the air** (supa/SupaArt, client/supa/SupaFx; the first ones were thin pale lines: rebuilt as the Light fruit's wave
  is, LightArt.wave: a level crescent five bands deep, a wide glow under a white core; with Armament a dark core in a violet glow): the
  flying slash (level: the owner called Murakumo's vertical wave "very strange"), the X (two crescents at +-45), claw marks (four, two
  bands deep), sparks, the glance (ring and sparks), Spiral Hollow's wake (rings open where the thread runs, round a glowing core),
  Sparkling Daisy's cut (two bright rays in a glow), Spar Break's blur (three bands of arcs), skate marks (each client its own from the
  skater's feet). Render: docs/handoff/supa-cuts-3.3.5.jpg. Budget DragonTests.supaCuts (a crowd of everything under 16,000 quads).
- **The moves** (server/SupaKit, SupaMoves; S2CUoFx 200-209: PARTS (a mask: ARMS, CLAWS, DRILLS, SKATES, STEEL), SLASH, SLASH_END,
  CLAW, SPARK, GLANCE, DASH (one's own client carries the body, as the Ice fruit's slide), TRAIL, CROSS, SPIN): LMB Spar Claw
  (pika_sword_1/2 by turns every 7 ticks, a 3.2 cone 75 degrees each side, 4 damage), RMB Flying Slash (tap: 6 damage, 24 blocks; held
  past 5 ticks hie_partisan_raise, full in 30 more: 14 damage, 36 blocks, 2.4 times as big; hie_partisan_throw; cuts through everyone
  once, ends at a wall with sparks), Z Atomic Spar (pika_murakumo, the X flies 16 blocks, 11 damage), X Spiral Hollow (hie_grab_reach,
  12 blocks at 1.5 a tick, 9 damage each passed, hie_road_punch at the end), C Atomic Spurt (70 ticks, Speed IV, 5 damage to each
  passed once per 10 ticks, hie_road_slide), V Spider (160 ticks, Slowness I, hie_partisan_launch; SupaKit.blocks stops every blow
  with an attacker unless FruitServer.hakiInfused, a bare fist cut 3 once per 10 ticks), B Sparkling Daisy (pika_lunge through the one
  aimed at 2.2 a tick, 14 damage as it passes, an X flash, 6 to others passed), N Spar Break (36 ticks, gura_tilt, 3.5 damage all round
  every 4 ticks, a last burst 5 damage thrown out + ConquerorAura.exploded), passive Slashing Immunity (SupaKit.blocks: a melee blow
  with a SwordItem, AxeItem or KatanaItem in hand, unless Haki-coated). Energy supa_* in FruitEnergy.COSTS. The Conqueror's lightning
  at every strike through SupaMoves.lightning (at most one burst a player per 4 ticks: ConquerorAura.mixed has no throttle of its own).
- **Sounds** (tools/supa_sounds.py, sounds/piece/supa, SwordTests webSounds("supa", ..., 7)): swish1 Qvantor "Swish Swipe Short"
  877976, draw1 mywhats "knife sharpening" 175520 (from 0.55 s), cut1 ihitokage "Sword stab 1" 395370 (0.1 s, 0.25 s), glance1
  velcronator "Sword Impact" 733887, rip1 reg7783 "cloth_ripping" 133440, skate (loop) 13FPanska_Marval_Lukas "Skating" 379329, spin
  (loop) Qvantor "Whoosh Swirl Spinning" 877983. **The pitch check, refined:** a short clip's peaks read higher by chance alone (plain
  noise: about +10 dB at 0.25 s, +7 at 0.45 s, +4.5 at 1 s, +3.5 long): judge a clip by its excess over noise of its own length.
- The panel and book rows come from SupaKit.MOVES (Spar Claw "LMB", the passive "-") as every fruit's.

## 8zza. The owner's new request during 3.3.5 (verbatim): a Rare fruit, the Ito Ito no Mi (on the to-do list, not started)

With four pictures (kept only in the session's uploads: anime frames and the series' fruit art never go in the repository): the fruit
(a white pear-shaped fruit covered in black-outlined swirls, a curly pink stem), Doflamingo's Overheat (huge waves of white string
shading purple at their heart, rising behind him on both sides), and two more of Doflamingo slicing with his strings and of Overheat.

"Add to the todo-list:

New Rare Fruit:

Ito Ito No Mi (Rare):
The Strings Fruit

Core Devil Fruit Abilities

* Slicing & Whipping. Doflamingo generates nearly invisible, razor-sharp white strings from his body to slice targets at long range
  (e.g., Overheat, Goshikito, Fretsaw).
* Puppet Manipulation (Parasite). He attaches thin threads to a victim's spine to control their body against their will like a
  marionette.
* Aerial Travel (Sora no Michi). He shoots strings into clouds to swing and fly across the sky.
* String Clones (Black Knight). He weaves compressed strings into a physical, controllable duplicate of himself that can speak and
  fight.
* Internal Self-Surgery. He uses strings inside his body to stitch together damaged organs as emergency first aid.
* Defense & Area Denial (Spider's Web & Birdcage). He creates dense defensive webs to block attacks and forms a massive, shrinking dome
  (Torikago) that slices everything in its path and blocks communication signals.

Awakened Abilities

* Environmental Transmutation. Upon Awakening, Doflamingo turns surrounding inorganic matter—such as buildings and the ground—into
  massive waves of controllable strings.
* Large-Scale Assaults. He uses awakened terrain to overwhelm opponents with techniques like Break White (crushing waves of string) and
  Ever White.

Haki-Enhanced Versions & Techniques

* Busoshoku (Armament) Imbuement. Doflamingo coats his strings in invisible or hardened black Busoshoku Haki, drastically increasing
  their durability and cutting power so they can bypass Logia defenses and withstand Admiral-level attacks.
* Haki-Infused Awakened Attacks (God Thread / Sixteen Holy Bullets). He gathers awakened strings into sixteen massive, Haki-coated
  spears fired simultaneously to pierce high-level defenses.
* Reinforced Birdcage & Spider's Web. Infusing defensive strings and the Birdcage with Haki makes them nearly uncuttable, requiring
  overwhelming Haki or extreme physical force (like Luffy's Gear Fourth) to break.
* Haoshoku (Conqueror's) Clash. While not directly infused into strings like Armament, Doflamingo combines his string strikes with
  Haoshoku Haki clashes during close-quarters combat.

Research Do Flamingo's Devil fruit, get reference gifs, reuse already in the game first person and third person animations."

(The owner's message carried source links: fandom wikis, a YouTube video, a TikTok, a Facebook post, a CBR article.) Read as: a fourth
new fruit, RARE (three stars), after Supa (3.3.5) and Horo (3.3.6) unless the owner says otherwise; research Doflamingo's fruit and get
reference GIFs (the HexPiece environment allows tenor.com and media.tenor.com); reuse the mod's own first- and third-person animations;
POINT ONE for everything else (the house style, the Conqueror's lightning out of every explosion with Haki and Ryou). Plan the keys
and ask the owner (AskUserQuestion) before building, as 8zw did for the other three. Lessons from Supa (8zz): judge the art against the
Light/Gura/Magma art before showing it; a flat single-colour mesh is not acceptable.

## 8zzb. Done (3.3.6): the Horo Horo no Mi (first pushed unchecked at the owner's word, finished in the next session); cute ghosts; Gloom

3.3.5 (8zz) is pushed and green (Build #172, Release #126). The owner then (verbatim): "bitch wheres Horo Horo No mi? did you just
skip it?" (it was not skipped: the accepted order was Noro 3.3.4, Supa 3.3.5, Horo 3.3.6; said so in one line and started it), then
"Enough prompt and handoff". **Pushed WITHOUT a local build or a CI check: first build it and fix whatever is red** (HoroMoves and
HoroKit were never compiled; the DamageTypeTags stub gained f_268738_ BYPASSES_INVULNERABILITY, which the original's FruitServer.physical
reads: run tools/srg_shapes.py if build.py asks).

Done in the tree:
- **Wiring**: PiecePatch HORO {"horo", "Horo Horo no Mi", "Ghost", "F3E3A6", lore} ordinal 8; FruitRanks COMMON; HexPiece.init registers
  server/HoroKit; PieceSounds registers horo/HoroSounds (GHOST, DESPAIR, POP, BOOM, SPIRIT, NETWORK: names only, **no clips and no
  sounds.json entries yet**); en_us "§fHoro Horo no Mi" and the subtitles; icon (tools/horo_icon.py: cream lobes with curled tips, two
  little ghost faces, a stem, after the owner's reference) and item model; build.py VERSION 3.3.6 and NEW_PREFIXES; FruitEnergy COSTS
  horo_negative 25, horo_mini 30, horo_toku 40, horo_astral 30, horo_network 35. **UoFx's route of kinds 220-239 to
  client/horo/HoroFx is cut out** (a comment marks the place) until HoroFx exists.
- **horo/HoroArt** (rendered and judged: docs/handoff/horo-ghosts-3.3.6.jpg, horo-gloom-3.3.6.jpg; tools/preview/HoroShots in FxPreview:
  |hnegative |hnegatives |hmini |htoku |hpop |hboom |hgloom |hnetwork): `ghost` one smooth see-through body (a round head flowing into
  a swaying, curling tail; rim bright, middle clear, tail fading; a soft glow) with a face by kind: NEGATIVE droopy half-shut eyes, an
  open drooping mouth, a pink tongue lolling (`tongue`); MINI round eyes, small smile; TOKU round eyes under heavy brows, a grin full of
  teeth. `burst` (a Mini's pop size 1, the Toku's blast size 6: a flash, a shock shell, two rings, banded ghost smoke SMOKE deep violet
  to white through FireMesh.puff, wisps); `gloom` (despair: a blue-grey raincloud RAINCLOUD over the head, long wavering gloom lines,
  a dim shadow); `link`. No per-frame allocation (static scratch arrays).
- **server/HoroKit** (RMB, Z, X, C, V; LMB the game's punch) and **server/HoroMoves** (S2CUoFx 220 GHOST, 222 DESPAIR, 223 MINI, 224
  MINI_POP, 225 TOKU, 226 TOKU_BOOM (bx 1: dropped without a blast), 227 ASTRAL (ticks 0: back in the body), 228 NETWORK (ticks 0: off)):
  RMB tap one Negative Hollow (hie_partisan_throw), held a stream every 6 ticks up to 5 (pika_laser_charge, pika_laser_end); a ghost
  flies 45 ticks at 0.6 a tick through walls and bodies; everyone it passes: HakiFalls.knockDown 120 ticks (despair), weakness,
  slowness, a mob's target dropped, DESPAIR drawn, the Conqueror's lightning (HoroMoves.lightning: at most one burst per 4 ticks). Z four
  Mini Hollows to the nearest foes within 16 (cycling; with none they wait in a row ahead), Z again or 120 ticks: each bursts through
  UoMoves.explode (radius 2.5, 6 damage: the Conqueror's lightning is inside it). X the Toku Hollow (hie_ball_gather, at 8 ticks
  hie_ball_throw) drifting 0.25 a tick for 200 ticks from 3 ahead; X again, a wall or a foe within 2: UoMoves.explode radius 6, 14
  damage, knock 1.4, Impact.quake. C Astral Projection 400 ticks (hie_partisan_raise): the body's place kept, mayfly and flying given
  (DragonServer's way), invisibility, the spirit untouchable but by the void and unable to strike (HoroMoves.spirit on
  LivingAttackEvent), mobs' targets on it dropped every 10 ticks; C again, time up, death, removal or the server stopping
  (HoroMoves.clear before the save): teleported back to the body, abilities and invisibility restored. V Ghost Network 600 ticks
  (magu_rise): every 20 ticks every living thing within 48 gets GLOWING (f_19619_, proven) for 40 ticks.

Next session (3.3.6, session 2): the unchecked push's Build HexPiece #173 was red (HoroMoves.despair's redundant `(float)` cast under
-Werror; f_19619_, f_268738_ and m_5448_ missing from verify/srg-shapes.txt). Both fixed: the local build is EXIT 0 with 13 suites.
Freesound answered 403 at the start of the session (the HexPiece environment, env_01PmnEXrwDhxzij8M5q6P6bL, is the current one).
The owner, on the ghosts, verbatim: "The ghosts are meant to be cute. I mean," (cut off). Proposed to them: rounder puffier heads, big
round shiny eyes (the Negative Hollow's droopy but round), pink blush cheeks, a short rounded tongue, a shorter tail with a curl; the
Toku Hollow a wide cartoon grin instead of rows of teeth and heavy brows. Waiting for the rest of their message before changing HoroArt.
The owner, next (verbatim): "also make sure gloom is it's own custom effect, causes blindness and slowness, attacks of all kinds, haki,
fruit , style, m1s, etc, miss often (miss the Horo Horo user and any other entity the player tries to hit." Asked (AskUserQuestion), they
chose: **a short fall, then Gloom** (the Negative Hollow knocks down for 40 ticks, StunFall's least), **8 in 10 attacks miss**, **8
seconds**. Built: server/Gloom (PieceEffects.GLOOM "gloom", "Gloom" in en_us, icon tools/gloom_icon.py: a dark raincloud with three blue
gloom lines, textures/mob_effect/gloom.png): HARMFUL, its own MULTIPLY_TOTAL -0.45 movement-speed modifier (as Slowness III), Blindness
renewed every 20 ticks for 45 (its icon hidden; m_6584_ isDurationEffectTick and m_6742_ applyEffectTick overridden: both checked in
joined.tsrg's MobEffect, bey), and Gloom.miss (LivingAttackEvent HIGH): any attack whose source entity is a gloomed LivingEntity is
cancelled 80 % of the time, whoever it is aimed at. HoroMoves.despair: knockDown 40 ticks then Gloom.put 160 (the old weakness and
slowness gone); the Negative Hollow's book text says so.

Finished in session 2 (nothing tested in game: build, 13 suites, renders, sound measurements):
- **client/horo/HoroFx** (as NoroFx/SupaFx; UoFx routes 220-239 again) and **client/horo/HoroClient** (ClientKits on the first tick):
  Negative Hollows (from + step * age, weaving, fading in and out), despair's gloom following each body (over it as it lies: the new
  Tumbles.lying gives the fall's angle, way, slide and lift), Mini Hollows flying from the user's chest to whom they cling over
  HoroArt.MINI_FLY 10 ticks then clinging (HoroArt.cling: round the body, bobbing, facing out) or waiting at their point, the Toku
  Hollow drifting (facing its way), bursts (a pop size 1, the blast TOKU_RADIUS; bx 1 drops without one), Ghost Network's six watchers
  (HoroArt.watch: spreading out over 16 ticks, circling at their own heights; no link lines: thin pale lines are not acceptable, and
  HoroArt.link is gone), the punch given back (HoroFx.punch). Caps: 64 of each.
- **Astral Projection drawn** (horo/AstralArt, pure; tools/preview/AstralPreview renders it in art/user_skin.png): HoroFx.asSpirit cancels
  RenderPlayerEvent.Pre for a spirit; the spirit is the player model in its own skin and outer layer (StyleRig boxes, SkinBox faces built
  once), washed WASH F2EEFF at 0.62 and see-through, **only the faces turned to the eye** (culled on the CPU from each face's normal: the
  phone culls nothing; without it the far side's skin showed through the near side and the body read as a muddle), the thighs at 0.6
  and shins at 0.16 (a ghost fades toward its feet), three pale shells (0.35 px white 0.14, 1.4 px lavender 0.11, 3.2 px 0.05) and a
  faint heart glow, floating as a Japanese ghost (AstralArt.floating: upper arms level forward, forearms hanging 34 degrees, legs hanging
  together slightly back, bobbing; its head turned as the player looks), full bright; not drawn in one's own first person. The body left
  behind (AstralArt.asleep: standing, arms hanging, head dropped 32 degrees as if asleep) in its own skin, lit by the world where it
  stands (LevelRenderer.m_109541_) and shaded by its faces' normals. Both skins through entityTranslucent before the light's batch
  (2.9.0's lesson). Judged from six sides: docs/handoff/horo-astral-3.3.6.jpg. A soft ghost puff where the spirit goes back in.
- **Cute ghosts** (the owner: "The ghosts are meant to be cute"): HoroArt.face redone, big eyes with a white shine each (`shine`),
  rosy cheeks (`blush`, BLUSH FF8FAE): the Negative Hollow's sleepy round eyes under a soft lid, a little open mouth, a short tongue
  swinging; the Mini's big round eyes and small smile; the Toku's round eyes, wide happy grin with a pink tongue and two little fangs
  (no rows of teeth, no heavy brows); tails shorter (2.0 r, the Toku 1.7 r); 18 sides and 7 head rings, and a lighter mesh (11 sides, 4 + 6
  rings) for a ghost under 0.09 of its distance on the screen. Judged and sent: docs/handoff/horo-cute-3.3.6.jpg.
- **Sounds** (tools/horo_sounds.py, sounds/piece/horo, SOURCES.txt; SwordTests webSounds("horo", ..., 6)): ghost1 newagesoup
  "Mouth-swoosh-01" 372520 (0.4 s on, -5 dB), despair1 Logicogonist "bass rumble - deepwhoosh 1" 807976 (-3 dB), pop1 Planman "Poof of
  Smoke" 208111 (compressed louder), boom1 Za-Games "Deep Boom" 539968 (compressed), spirit1 florianreichelt "Woosh" 683096, network1
  martian "magical rising wind or sea" 403003 (from 6.9 s, -4 dB). Loudness -11.9 (boom) to -18.8 (wind) LUFS. **tools/tonal_lines.py
  is the pitch check, kept in the repository now**: the 121-bin line and a narrow peak (over the bins 4 to 10 either side: a tone stands
  over its near neighbours, a bass slope at the band's low edge does not), each against noise of the clip's own length; the Ice clips the
  owner accepted read up to +6.8 dB narrow; these +1.9 to +5.2 as cut. Freesound rate-limits pages (403 for a minute or two): wait.
- **Lag**: the busiest moment one user can make (a stream, gloom over eight, the watchers, a spirit's light, and the Minis and Toku or
  their bursts) under 16,000 quads (DragonTests.horoGhosts): the blast 10 puffs (was 14: 6,500 quads at 7 blocks), a pop 4, gloom's
  cloud 3. HudTests: Horo Common, its moves' energy, Gloom's numbers and name; DragonTests: the icon, model and Gloom's icon.
- First person of every reused clip (HandPreview: low right, never over the view) and the panel (UiPreview hud-9: RMB Z X C V) checked.

What was asked to be done, as first planned (kept for the record):
1. **client/horo/HoroFx** (as client/noro/NoroFx and client/supa/SupaFx): accept the kinds above; put client/horo/HoroClient (a
   ClientKit: HoroKit.MOVES, no stance) in ClientKits.KITS on the first tick; give the attack button back to the game's punch (NoroFx.punch);
   draw at AFTER_PARTICLES (debugQuads): Negative Hollows (from + step * age, a little wobble, facing their way), despair over each body
   (follow the body: its top), Mini Hollows (flying from the user's chest to the target's chest over about 10 ticks, then clinging and
   bobbing; waiting at their point with no target), the Toku Hollow drifting, bursts, the network's six ghosts circling the user with
   links. Re-add the route in client/dragon/UoFx.
2. **Astral Projection on the client**: RenderPlayerEvent.Pre cancelled for a spirit (MaguFx.asMagma hides the player the same way: no
   floating armour); the spirit drawn as the player's own body washed ghost-white and see-through with pale shells, floating
   (StylePoser.sample("pika_float", age)), and the body left behind in the player's own skin at the kept place, standing: generalise
   pika/LightClone.body (its WASH, SHELLS, SHELL and the gold heart as parameters; the Light clones keep theirs) and make
   client/pika/PikaFx.SkinWriter usable from client/horo (its skin pass: SKIN.buffers, Bulk.read, use(player.m_108560_()), end()).
3. tools/horo_sounds.py (CC0, broadband: a breathy whoosh for a ghost, a low dull sigh-free thud or wind for despair (no voice, no
   moan: pitched), a soft poof for a pop, a deep explosion for the blast, a rising whoosh for the spirit, swirling wind for the network;
   each checked AS CUT against plain noise of its own length, 8zz), sounds.json, SwordTests webSounds("horo", ..., 6) and its
   "registered" line.
4. Tests: HudTests (Horo Common; HoroKit.MOVES in the energy list), DragonTests (the icon and model; a budget test: a stream, minis, the
   Toku, two bursts, a crowd's gloom, the network under 16,000 quads).
5. First person of the reused clips (HandPreview), the panel (UiPreview kitRows), release notes 3.3.6, push, both runs green; render
   the moves for the owner (they said "no need for gif" for Noro: renders are fine).
6. Then the **Ito Ito no Mi** (8zza: plan the keys and ask first).

## 8zzc. The plan for the Ito Ito no Mi (3.3.7; researched in 3.3.6's session; the owner's answers at the end)

Research: the fandom wiki is behind a browser check (curl gets "Just a moment..."); the techniques are well known: Tamaito (string
bullets from the fingertips), Goshikito (five strings slashing down), Overheat (a red-hot rope of tightly wound string lashed like a
whip: the Tenor reference doflamingo-overheat shows it glowing orange-red, a white-hot core), Fulbright (strings hanging from the hands),
Parasite (threads into a body: a marionette), Sora no Michi (strings to the clouds: walking the sky), Black Knight (a string clone),
Kumo no Sugaki (Spider's Web, a shield), Torikago (the Birdcage: a dome of strings closing in, cutting all it passes), the awakening
(the ground turned to waves of string: Break White, Ever White) and God Thread (sixteen Haki-coated spears of string fired at once).
Reference GIFs (Tenor, kept only in the scratchpad): doflamingo-overheat, doflamingo-dressrossa, doflamingo-string,
donquixote-doflamingo-vs.

The art (POINT ONE): never thin pale lines. A string is drawn as the Light fruit's beam and wave are: a bright core under a wide soft
glow, several strands together, cel-banded where it is thick (Overheat: red-hot banded rope, orange to white at its heart, as the Magma
fruit's fire bands are); the Birdcage a dome of glowing strands; Armament turns every string black-purple like the katanas. Black Knight
is the player's own body (horo/AstralArt's way) woven of white string. Animations reused from the mod's own clips.

Proposed keys (as the other fruits: LMB, RMB, Z X C V B N M, a passive):
- LMB **String Slash**: lashes of string from the fingers, a combo (pika_sword_1/2), reach 4.5 blocks.
- RMB **Tamaito**: tap a string bullet that pierces, hold for a burst of up to six.
- Z **Goshikito**: five strings slash down onto the spot looked at (a deep cut in the ground, big damage).
- X **Overheat**: a red-hot rope of string lashes out in a long line ahead, setting what it hits on fire.
- C **Parasite**: threads into the one looked at: for 5 s they cannot move on their own; you drag them where you look.
- V **Spider's Web**: a web of string in front for 6 s: projectiles stop in it, blows from the front are blocked.
- B **Black Knight**: a string clone of yourself fights beside you for 15 s.
- N **Birdcage**: a huge cage of strings round you for 30 s, shrinking slowly; whoever touches its strings is cut; no one gets out.
- M **Awakening: Break White**: the ground round you turns to waves of white string rolling outward; with Armament on, **God Thread**:
  sixteen Haki-coated spears of string fired at once at what you look at.
- Passive **Sora no Michi**: jump again in the air to walk on strings (a few steps up the sky, then down); **Self-Surgery**: below a
  third of your health, strings stitch you up (healing for a few seconds, once a minute).

**The owner's answers** (AskUserQuestion, 3.3.6's session): the keys **"As proposed"** (exactly the list above); the Birdcage **"40
blocks, 45 s"** (a huge cage like Dressrosa's, shrinking slowly over 45 seconds). Rare (three stars), version 3.3.7. Not started.

## 8zzd. Done (3.3.7): the Ito Ito no Mi (the owner: "Ima only test when I get home, proceed")

Built (nothing tested in game: build EXIT 0, 13 suites, every effect rendered and judged third and first person, every sound measured):
- **Wiring**: PiecePatch ITO {"ito", "Ito Ito no Mi", "String", "F27BB8", lore} ordinal 9; FruitRanks RARE ("§9Ito Ito no Mi");
  HexPiece.init registers server/ItoKit; PieceSounds registers ito/ItoSounds (SWISH, SHOT, SLICE, BURN, PULL, WEB, CAGE, WAVE, STEP:
  names only, no clips or sounds.json entries yet); en_us subtitles; icon tools/ito_icon.py (a white pear with black swirls, a curly
  pink stem; judged) and item model; build.py VERSION 3.3.7 and NEW_PREFIXES; FruitEnergy ito_* costs; UoFx routes kinds >= 240 to
  client/ito/ItoFx (**S2CUoFx's kind is one byte: 255 is the last kind there is**; the Ito fruit uses 240 to 252).
- **client/FruitKeyN**: M sends ability 41 (ABILITY_M) for a fruit with a move on "[M]"; a jump pressed in the air with the Ito fruit
  (fruit mode, not flying, not in water) sends 42 (ABILITY_SKY). The original's FruitServer.action passes any ability number to the kit
  once fruit mode is on.
- **server/ItoKit + ItoMoves** (8zzc's keys): LMB String Slash (pika_sword_1/2, 4.5 reach, 70 degrees, 4.5 damage); RMB Tamaito (tap one
  piercing bullet 2.4 a tick, 40 blocks, 4 damage; held a burst of six, pika_laser_charge/end); Z Goshikito (hie_partisan_throw; 5 strings
  fall for 6 ticks onto the spot looked at within 30; 12 damage in an 8 x 2.8 strip; the Conqueror's lightning out of it); X Overheat
  (hie_pheasant; a 16-block line at tick 4: 10 damage, 5 s on fire, thrown aside); C Parasite (hie_grab_reach; the one aimed at within 24
  dragged where the user looks for 100 ticks, C again lets go); V Spider's Web (hie_ball_gather; 2.2 ahead, radius 2.4, 120 ticks:
  projectiles crossing it held fast, gravity given back after; ItoKit.blocks stops blows from in front); B Black Knight (hie_partisan_raise;
  300 ticks, a server-side copy walking to the nearest foe within 16 at 0.3 a tick, cutting 5 every 14 ticks, a KNIGHT message every 4
  ticks, the clients interpolate); N Birdcage (magu_rise; the owner's 40 blocks closing to 10 over 900 ticks, every 2 ticks whoever is
  within 1.2 of its dome cut 4 (once per 10 ticks) and pushed back in or kept out; N again takes it down); M Break White (hie_ice_age;
  three waves 8 ticks apart rolling 0.5 a tick to 18, 7 damage, thrown up) or with Armament God Thread (hie_ball_throw; 16 spears meet
  at the aimed body or point within 40 after 8 ticks: UoMoves.explode radius 3.5, 14 damage); passive Sora no Michi (three air steps,
  lift 0.5, fall forgotten); passive Self-Surgery (below a third, Regeneration II 6 s, once a minute). Armament: strings coated (bz 1
  in every message) and 25 % more damage.
- **ito/ItoArt** (strands: three soft ribbons turned to the eye, a white-hot core, a pink-white halo, a wide pink glow; coated: a dark
  core in a violet glow; Overheat a banded red-hot tube with two strands wound round it and an orange glow) and **client/ito/ItoFx**
  (everything above; the Black Knight's skin through AstralArt.knight: pale, nearly solid, string-white seams, walking pose
  AstralArt.walking or the Light fruit's sword cuts). **Next: render and judge every effect (tools/preview/ItoShots), sounds, tests.**
- **The owner on the strings** (verbatim, as they watched the renders): "Not a fan of the strings, it looks like dust." (the strand's
  glow, halo and core ribbons lay in one plane and fought in the depth test pixel by pixel: specks; and the core was thinner than a pixel
  with clear edges), then "its meant to be slimmer, they're too thick", then "sharper in the end too not just square ends..". Fixed in
  ItoArt.strand: each ribbon a hair nearer the eye than the one under it (layer's lift: 0, 0.002, 0.004 of the distance), the core solid
  (opaque to its edges) and never under about a pixel and a half (PIXEL 0.0016 rad), every string SLIM 0.42 of the width each move gives
  it, and its last POINT 30 % narrowing to a needle point (rings: ItoArt.ring, no point). Overheat stays a red-hot rope. Rendered and
  sent: docs/handoff/ito-strings-3.3.7.jpg (phone-like resolution, halved). **Keep strings this way: slim, solid, pointed; render at
  840 wide or more (FxPreview --size) before judging: small previews show thin strings as specks.**
- The owner, shown God Thread's spears with square ends: "where are the sharp ends?" Every string now tapers at BOTH ends
  (ItoArt.taper: its last POINT 30 % and its first POINT_START 15 %; ItoArt.line at least 6 pieces so the points have a body between
  them); God Thread's spears are sharp black thorns, each with a long diamond head. docs/handoff/ito-sharp-3.3.7.jpg (sent);
  the Armament variants: docs/handoff/ito-coated-3.3.7.jpg (dark slim strings, a faint violet glow: if the owner wants Haki to read more
  strongly, raise COAT_GLOW's layer, never the width).
- First person: every string and Overheat's rope thin near the eye (ItoArt.near: NEAR 1.5, NEAR_ROPE 3), one's own web drawn at 0.45;
  ItoFx.hand puts one's own strings in front of the view's lower right. The reused clips' hand moves checked (HandPreview: low right).
  The Black Knight rendered in a real skin (AstralPreview with ASTRAL_KNIGHT=1): a pale glowing copy, walking, violet with Armament.
- Sounds (tools/ito_sounds.py, sounds/piece/ito, SwordTests webSounds("ito", ..., 9)): swish1 hz37 "Swish wipe" 667419 (-6 dB), shot1
  ssierra1202 "swing" 391952, slice1 PEDBRA "Slicing" 377214 (from 0.2 s, -3 dB; the first pick, "Corte", was all sub-bass: a thump),
  burn1 hnhnh "fire-whoosh" 244926, pull1 ssierra1202 "ropes" 391946 (from 1.2 s), web1 Qvantor "Whoosh Flutter Wings" 877979, cage1
  Henrythetrain "Air Rush" 135785 (from 1.0 s, -2 dB), wave1 DJT4NN3R "whoosh_long_low" 449990, step1 Qvantor "UI Hover Micro Swish"
  878248; each as cut -2.5 to +4.9 dB over noise (tools/tonal_lines.py narrow); loudness -21 to -14 LUFS (ebur128 reads clips under
  0.4 s as -70: use volumedetect's mean for those).
- Tests: HudTests (Ito Rare, the Birdcage's 40 and 900, the keys, M 41 and the air step 42, the energy list), DragonTests (the icon and
  model, itoStrings: the busiest moment one user can make under 16,000 quads coated and bare; the Awakening is one key, Break White bare
  or God Thread coated, never both; the cage closing 40 to 10; a string ends in a point). Panel (UiPreview hud-10). Release notes 3.3.7.

## 8zze. This session (3.3.8, 3.3.9): the owner's answers, the rubber ripples, the Dawn Pistol accepted

3.3.7's Build #177 and Release #128 were green (and the handoff's Build #178). This session started on Default: switch_environment to
HexPiece (env_01DEzkwiWiBUXF6hnQ29v2S5 now) for Freesound. Asked (AskUserQuestion), the owner answered:
- Horo (3.3.6): **"Not tested yet"**. Ito (3.3.7): **"Not tested yet"**. Ask again next time.
- 3.3.2 / 3.3.3 (the jump, Rubber World), verbatim: **"Rippes from world of rubber should be higher like lift more, falling from Bajarang
  gun doesnt make the body ripple when the player falls (every fall should make the floor ripple)."** Asked which and how much: **"Both"**
  (Rubber World's waves and the jump/landing ripples) and **"About 2x"**.
- Dawn Pistol: **"Build as proposed"** (the proposal, as asked: a quick cartoon wind-up in third person (arm yanked far back, body
  twisting, the whole body thrown into the punch; first person left alone), the stretched arm ending in a big ballooned rubber fist like
  the Bajrang Gun's (smaller) coated black by Haki, a cartoon "POW" burst on hit (a bold star-shaped flash and shock ring), the struck
  body bent like rubber as Star Gun does and knocked far back, the Conqueror's lightning out of the impact with the Conqueror's Haki and
  Ryou on, and a new CC0 punch sound from the web (the original's unused gear5_dawn_pistol.ogg hums at 59 Hz: +10 dB over noise)).

**3.3.8 as built** (nothing tested in game: build, 13 suites, rendered: docs/gifs/nika-rubber-world-3.3.8.gif, nika-landing-3.3.8.gif):
- gear/RubberWave: RING_AMP {3.0, 1.2, 2.2}, WAVE_LOW 0.7, WAVE_HIGH 3.8, MOST 4.0 (widths and speeds as 3.3.3). `underTall(h)` =
  max(1, h - 0.75): client/gear/RubberWorldDraw draws the block under a risen column stretched (PoseStack scale y) down a quarter into the
  real ground, so a 4-block swell has no gap and still costs 2 blocks a column (MOST_DRAWN 520 kept); `tilt(slope)` = atan(slope) x 0.6
  capped at TILT_MOST 0.61 (3.3.3's steepest: twice as high is twice as steep). tools/preview/NikaShots draws the same columns.
- gear/RubberJump: SPRING 1.1, HOP_AMP {1.4, 0.7}, LAND_AMP {1.6, 0.8} (a landing's visible peak 0.94, was 0.47); `FALL_MIN` 2.0.
- Every fall ripples: server/NikaJump.falls (PlayerTickEvent END; returns at once on the ground with nothing tracked): FALLS keeps each
  airborne Gear 5 player's highest feet (flying, riding, water, death forget it; respawn and dimension change too); landing FALL_MIN or
  more under it sends LAND to everyone near, unless one of their jumps is landing (it ripples once on its own). The original zeroes the
  fall distance in the Bajrang Gun and its bounding walk, so the height is tracked, not fallDistance. client/gear/NikaJumpClient does the
  same for one's own landing at once (the server's LAND skips the user). The original's bounding run lands from its big bounds: each
  ripples now (the owner: "every fall").
- HudTests: ripples about twice 3.3.2's, Rubber World's waves twice 3.3.3's, FALL_MIN between 1.5 and 2.5.

**3.3.9 (Dawn Pistol), found for the build:** the original's `GearServerController.pistol` (javap): PISTOL unlocked, M1 cooldown 20,
`HakiServerController.animate(player, "gear5_pistol", 2)`, `dawn` -> no GEAR5_PISTOL sound, "§6Dawn Pistol" on the action bar and
SoundEvents.f_11874_ (0.9, 0.75); `throwLimb(player, limb (alternating), StretchStyle.PISTOL (windup 6, extend 4, hold 3, retract 7),
reach x 1.15, width 1.25, damage x dawnPower, knock 1.25 (0.85 bare), 5)`; the hit is `lambda$throwLimb$11` (nearestOnSegment, `hit`,
fx IMPACT at the body, a sound) after impactTick. `sendStretch` sends S2CGearStretch with dawn; the client's
`RubberLimbLayer.draw(..., Stretch, dawn, ...)` emits the arm through GearOutfitLayer.emitWithCuff (skin), RubberLimbMesh.emit (sleeve)
and, with dawn, RubberLimbMesh.emit again with DAWN_FIRE (textures/effect/gear5_aura.png); `renderFirstPerson` is first person (leave
it). client/gear/NikaFist shows how the Bajrang fist replaced the hand cube (HIDE, NikaArt.fist through Bulk).

**3.3.9 as built** (Dawn Pistol; nothing tested in game: build, 13 suites, every part rendered, the sound measured; GIFs
docs/gifs/nika-dawn-pistol-3.3.9.gif, nika-dawn-pistol-pull-3.3.9.gif, nika-dawn-pistol-windup-3.3.9.gif). The owner, verbatim, while it
was built (they watched the renders): on the fist at 5.5 times the hand, "thats the left hand on the right arm..." and "and the fist is
too big, way too big"; then "make sure it bends the entities a ton more and looks better than the current one" with a frame of Luffy vs
Lucci (Cap 1101: Lucci's leopard body pulled out like taffy, several times its length, along Luffy's stretched arm).
- server/NikaPistol + patch/PiecePatch.dawnPistol: GearServerController.pistol's animate -> `NikaPistol.clip` (with `dawn`:
  gear5_dawn_pistol or gear5_dawn_pistol_left by GearData "pistolSide", true = the right arm, set by alternate() just before; the throw
  remembered); after `hit` in the `lambda$throwLimb$` landing, `NikaPistol.struck(player, target, way, award)` (the hit's own locals; the
  lambda's last argument is the award: the Pistol's 5, the Gatling's 1): within WINDOW 24 ticks of a Dawn throw, the hit's own throw
  (it already weighs knockback resistance) x FAR 2.0 and lifted to LIFT 0.45, f_19864_; S2CUoFx POW 164 (id the body, user the thrower,
  the body's middle, ax ay az the way, bx the pull's strength max(0.4, thrown / 1.25), size its height); piece.gear.dawn_pistol at the
  body. The original's fx(IMPACT) (and so ConquerorAura.gear's lightning) still comes.
- client/gear/NikaPistolArm + PiecePatch.pistolArm (build.py PATCHED: RubberLimbLayer): `RubberLimbLayer.draw` tells `begin(stretch)`
  (its ninth local); its GearOutfitLayer.emitWithCuff -> `emitSkin`, its two RubberLimbMesh.emit -> `emitOther`. For PISTOL with
  `variant` 1 (the original's dawn; it already draws the arm Haki-black, 0.11 0.11 0.14, with the gear5_aura fire round it) the hand
  cube is hidden (NikaFist.HIDE) and the ballooned fist drawn FIRST into the skin's batch (emitWithCuff may switch buffers for the cuff):
  NikaFist.fistAt -> NikaArt.fist(..., right) at BALLOON 3.0 x the hand's half size (5.5 was "way too big"), inflating from FROM 0.4 by
  FULL_AT 2.5 blocks out, tint COAT 0x3A3248 over the skin texel, the back of the hand up (model -y), **a right hand on the right arm**:
  NikaArt.fist builds a LEFT hand (thumb on F x U); `right` mirrors the thumb (3.3.9's first render had a left hand on the right arm).
  The Bajrang Gun's fist is unchanged (also built as NikaArt's left hand: check it if the owner asks). Stub added:
  dev/kosmx/playerAnim/impl/IUpperPartHelper (GearOutfitLayer implements it).
- First person exactly the Pistol's: PiecePatch.firstPersonArms redirects playRemote's `OWN_FIRST_PERSON_ARM.contains(name)` to
  `PieceHooks.ownFirstPerson` (adds the two Dawn clips: FirstPersonMode NONE, the original's own first-person arm).
- tools/nika_anims.dawn_pistol: both clips are gear5_pistol's arm moves exactly plus DAWN_PISTOL's **body** yaw and bend (a torso yaw
  turns the torso mesh alone in PlayerAnimator: the arms would come off), wind-up yaw -42 (the rig: -z is forward, a negative body yaw
  puts the right shoulder back), snapped to +6 at 7 and square (within 6) while the arm is out, ticks 6 to 13 (the original draws the
  stretched arm in the turned body's frame). ToonForce beats (2 squash, 6 stretch, 10 squash). No head, no leg.
- gear/NikaArt.pow (POW_LIFE 12: a comic starburst facing the eye, 10 spikes, ink 0x1C1426 / white / warm core 0xFFE7A0, each band a
  hair nearer the eye; 8 impact lines; two shock rings round the punch line; under 400 quads), client/gear/NikaPow (MOST 16, debugQuads,
  AFTER_PARTICLES; UoFx routes POW before the 160-179 hold route) which also starts the pull: client/gear/NikaBendClient.dawn ->
  gear/NikaBend.dawn: the body pulled out lengthways (shear DAWN_PULL 2.8 blocks along per block up, across 1/(1+0.7e), up 1-0.18e,
  along 1+0.8e; smack 2, hold 9 quivering, snap back 10 through a squash, wobble 10). The first try (4.5, up 1-0.38e) lay flat as a
  plank. Star Gun's bend (NikaBend.shape) unchanged.
- Sound: tools/gear_sounds.py "cartoon_punch" modusmogulus "Cartoon Punch Cassette F" 785936 -> gear/dawn_pistol1.ogg (3 dB down: -13.6
  LUFS, +2.9 dB over noise in tools/tonal_lines.py); GearSounds.DAWN_PISTOL, sounds.json, en_us "A rubber fist pounds"; SwordTests
  webSounds("gear", ..., 3). Rejected (pitched): CartoonPunch 563356 (980 Hz +11), Punch 2 Heavy 490769 (54 Hz +14), 535362 (183 Hz),
  heroiclobber 645864 (221 Hz).
- verify/srg-shapes.txt: AABB.getCenter m_82399_ (joined.tsrg from MCPConfig on GitHub raw).
- HudTests.dawnPistol: the clips' arms equal gear5_pistol's, no head or leg, square while the arm is out, wound up 30+; POW's kind; the
  POW cheap and gone after its life; the pull at rest at both ends, the top flung 3.5 to 6 blocks, more than 3 x Star Gun's, a squash,
  no jumps, a heavy body pulled less. Previews: NikaShots `|npow:age`, `|npistol:reach[:pow age[:balloon]]`, `|ndbend:t`, `|ndawn:t`.

**3.4.0 (Dawn Pistol's final blow)**. 3.3.9's runs were green (Build #180, Release #130). The owner, verbatim, on 3.3.9's GIFs: "it looks
cool but please get a gif of Gear 5 luffy vs luccy the final blow, its not that... the fist goes through the body, stretching the skin to
insane amounts and then causes a catastrophic knockback that destroys all blocks in it's way", with a frame (KIRA AMVs' Egghead AMV: Luffy's
arm looping round, the fist driven into Lucci's back, his skin bulging out of his front in a long funnel, red then orange then white-hot at
its end) and a YouTube link (https://www.youtube.com/watch?v=T1ChQRs6uhI: the page answers a bot check here; i.ytimg.com gives its stills:
the same AMV). The HexPiece environment now allows tenor.com, media.tenor.com, media1.tenor.com, www.youtube.com, i.ytimg.com (added this
session at the owner's request). The Tenor GIF that shows the blow: media.tenor.com/VTcdI2kEM2AAAAAC/luffy-vs-lucci-one-piece.gif (Lucci's
spotted skin stretched along Luffy's arm to the glowing fist, then smashed down through buildings); kept only in the scratchpad.
- gear/NikaSkin (pure): `out(t)` (out-back to full in GROW 2, held quivering to PIN 5, snapped back over SNAP 2.5), `skin(...)` (a cone
  out of the body along the way, LENGTH 7: body-wide (radius 0.7 x the body's width, 0.3 to 1.2) narrowing evenly to NECK 0.27, a bulb
  BULB 0.42 round the fist at the end; 16 rings x 12 sides; the texture's region u round it, v stretched along), `heat(...)` (bands a
  hair outside it from 0.38 of the way: red C81E1E, F0501E, FFA02A, FFF0C0; the fist a white-hot shell in an orange glow). The first
  render (power 2.2 to a 0.2 neck) read as a thin stick: now a cone.
- client/gear/NikaSkinClient (from NikaPow.accept): the struck body's texture as FinisherFx finds it (a player's skin; renderer.m_5478_;
  a HumanoidModel's chest region 20..28 x 20..32 of 64 x 64, 64 x 32 for skeletons and strays; the middle 16..32 of anything else),
  entityTranslucent, the light where it stands, a batch per body, then the heat through debugQuads; not one's own in first person.
- server/NikaPistol: no more x2 throw; `Pin` holds the body (motion zero, put back past 0.12 with m_6021_ as HieFreeze does) until
  NikaSkin.PIN, then `PikaMoves.Launch` along the flat way at LAUNCH_SPEED 3.6 x (0.55 + 0.45 x share) with `hardest` LAUNCH_HARDEST 50
  (obsidian; Launch gained the field, the Light kick and Gekishin keep iron), a quake and the Gura boom at the launch, Gura's hit and
  crumble where it stops; NikaPistol.LIVE ticked on LevelTickEvent (a snapshot: the pin adds its launch).
- NikaBend.dawn (3.3.9's taffy pull), NikaBendClient.dawn and their tests and previews are gone. The POW is three-fifths of 3.3.9's size
  (it hid the body). Previews: NikaShots `|nskin:t`, `|ndawn:t` (the whole move: arm, skin, POW, launch).

**3.4.1 (Dawn Pistol: the arm, the timing, the landing)**. 3.4.0 was green (Build #181, Release #131). The owner, verbatim, in order:
"does the arm bend? I dont want it to be a straight line"; "Excuse me??? How IS THIS IN HELL STRAIGHT" (their Egghead frame with Luffy's arm
traced in green: from Luffy it hooks down and back, runs along Lucci, hooks round past him and comes back through him to the funnel);
"It also has to be FAST,  but when it lands and stuns (the skin streches to the limit slower and slower (the further it gets) and when it
reaches the max, the entity is projected in a straight line FAST. adding a dust explosion on landing (and making a crater)."
- gear/NikaArc (pure): the arm's path, KNOTS 14 points of a looping trochoid in the plane of the way and up-and-out (0.8 up, 0.6 to the
  arm's outside): progress u L - A sin(3 pi u), offset A (1 - cos(3 pi u)) - 2 A u, A = 0.18 L (grown in over 2 blocks): it leaves the
  shoulder going back, loops once, arrives going forward fast. client/gear/NikaPistolArm draws a Dawn Pistol's arm (all three passes) as
  a quadratic B-spline of 12 pieces through those points, each piece the original's own RubberLimbMesh.emit (the skin's first piece
  through GearOutfitLayer.emitWithCuff, written LAST: the cuff may switch batches), each piece's mesh side square to the loop's plane,
  `per` max(3, segments / 4); the fist along the last stretch. (3.4.1's first try lifted the original's single control point: still one
  bow; a single quadratic cannot loop.)
- Through the body: NikaSkinClient keeps the thrower (POW's user); `NikaSkinClient.reach(level, user, now)` gives the skin's stretch;
  NikaPistolArm.through (the skin's pass, which has the player) lays the loop out to the body (BODY_AT: the shoulder's world distance to
  the body's middle / 0.9375 along the arm's own way) and runs a straight piece on to REACH (+ the stretch) where the fist is, so the loop
  holds still while the arm goes on through; the sleeve and dawn fire passes reuse it (`reaching`).
- gear/NikaSkin: PIN 10 (the stretch = the hold): out(t) = 1 - (1 - t/PIN)^3 (slower and slower to its limit, a faint quiver), launched at
  PIN, SNAP 2. The original's limb lives windup 6 + extend 4 + hold 3 + retract 7 = 20 ticks: it lands at 10 and ends at 20, as the
  launch.
- server: PikaMoves.Launch gained `straight` (its first ticks along carry x speed, no gravity, ground ignored) and `landed` (called in
  down()); NikaPistol: LAUNCH_SPEED 5.0, STRAIGHT 8, landed: Craters.open CRATER 2.4, GuraMoves.fx QUAKE (bx 1.0 strength, size 3.2: dust,
  rings, rocks, cracks), ConquerorAura.exploded 3.2, Impact.quake, the Gura boom.
- HudTests.dawnPistol: the stretch's first tick over 5x its last, at its limit at PIN, a straight fast launch under the game's movement
  check. Previews: NikaShots `|ndawn:t` (the loop, through the body, the launch), `|ndland:age` (the landing).

## 8zzf. The owner on the Ito fruit after playing it (verbatim; 3.4.2 works on it)

Asked (AskUserQuestion) how Dawn Pistol (3.3.9 to 3.4.1), Horo (3.3.6) and Ito (3.3.7) play, the owner answered with a message instead
(Dawn Pistol and Horo: no answer yet, ask again later):
"Ito Ito No Mi problems changes NEEDED:

Z Often doesnt work and looks bad overall (redo entirely)
X Should be a charge beam that DOES NOT COME BACK (explodes on landing) when it ends the strings scatter into every direction (The
charging should be one arm pointed forward and the strings gathering infront of the fist.

I dont like how M1 looks animationwise, its shitty.

Black knight looks odd as well, it should be a clone that roams on it's own / follows the player, when killed it explodes into tons of
strings, if the key is recast while it's alive it explodes into strings too.

Awakening is for some fucking reason bound to H (which makes it not work)"

Then (verbatim): "Bird cage should be also  a chargem the user points at the sky and shoots a stedy stream of strings that spread into
the cage when it reaches the right height. (the longer you hold it the bigger it gets (max is the current size), when recasting it, the
bird cage slowly closes, cutting down anything caught in the cage, all entities are pulled towards the center of the cage and when the
cage fully closes, they take damage + bleed."

Then (verbatim): "Another bug, Supa Supa No Mi, no blades are showing in third person at all.
ANd the blades have to be dramaticly sharper with a pointy tip. all of them.

in Horo Horo No Mi, the projections arms are NOT TO BE ANIMATED, in this form the user can do everything normal from minecraft, but cant
use style or haki at all."

Then the Nika fruit (verbatim, with a picture: a frame of Gear 5 Luffy as a spinning blur):
"Whip and Dawn Whip Rework:

Without Haki the leg whip move, becomes "Goof Ball" the user starts spinning super fast in a cartoon way and then becomes a  spinning
ball of smoke and the player's skin mixed,  punching in all directions, this is a held move, when it ends the user is temporarily dizzy
(with a dizzy symbol on top).

Dawn whip (Goof Ball with Haki on)

Dawn Whip is an extremely powerful, high-speed spinning kick in which Luffy twists his entire rubber body into a tightly wound spring,
stretches one leg outward, and releases the accumulated rotational force to strike his opponent like a gigantic whip.
What makes this attack unique is that Luffy doesn't simply spin around and kick someone. He uses the unnatural elasticity of his Gear 5
body to generate tremendous rotational momentum, turning himself into a living, rubber-powered spinning weapon.
It's essentially a combination of three movements: twisting, unwinding, and whipping. The technique is first demonstrated against Rob
Lucci in his Awakened Zoan form during the Egghead arc.

Complete movement breakdown
Phase 1 — Winding up the body
Luffy begins preparing the attack by twisting his body around its vertical axis. Rather than rotating his whole body as a rigid object,
he uses his rubber physiology to twist his torso, hips, and lower body far beyond normal anatomical limits.
His body behaves like an elastic spring being wound tighter and tighter.
During this preparation:
* His torso coils around itself, storing elastic tension.
* His hips rotate to prepare the sweeping kick.
* His upper body becomes increasingly twisted relative to his lower body.
* His posture becomes exaggerated and cartoonishly distorted, something Gear 5 allows him to perform without injuring himself.
The important detail is that the twisting happens before the main kick. He is building up the force that will power the strike, rather
than immediately swinging his leg.
The tighter the wind-up, the more violently his body can unwind.
Phase 2 — Extending the attacking leg
As he releases the twisted position, Luffy thrusts one leg outward into a nearly horizontal kicking position.
His attacking leg stretches beyond its normal length, allowing his foot to reach opponents considerably farther away than an ordinary
kick could.
The leg effectively becomes the flexible striking section of an enormous whip.
The body positioning is important:
* His hip acts as the point from which the kicking leg swings outward.
* His knee and ankle can remain extended while the rubber leg stretches.
* His foot travels along a wide circular path around his body.
* His torso provides the rotation needed to accelerate that extended leg.
Luffy also reinforces the striking leg with Armament Haki, providing hardness and additional striking power while retaining his body's
rubber-like movement.
Phase 3 — Explosive rotational release
This is the defining moment of Dawn Whip.
The elastic tension stored in Luffy's twisted body is suddenly released. His torso begins unwinding, forcing his hips and extended leg
into a tremendously fast circular movement.
Rather than a conventional roundhouse kick, his entire body becomes the engine driving the attack.
Imagine twisting a rubber band repeatedly and then letting it go. The rubber band rapidly rotates in the opposite direction as it
releases the stored energy.
Luffy performs the same principle with his entire body.
His extended leg sweeps around him, and the tip of that leg travels considerably faster than the center of his rotating body.
The motion becomes so fast that the animation emphasizes it through exaggerated spinning, rotational streaks, and cartoon-like
distortion.
The kick's real power comes from the combination of elastic recoil, rotational acceleration, an extended striking radius, and Haki
reinforcement.
Phase 4 — The whip-like impact
As the attacking leg sweeps across his opponent, Luffy's foot and lower leg collide with the target at enormous speed.
This is a lateral sweeping impact, not a downward stomp or a straightforward kick.
The leg behaves like a whip because its movement is transmitted from the rotating body outward toward the striking foot.
The result is an extremely sudden, violent sideways blow.
Against Rob Lucci, the attack connects and sends him flying with substantial force. Lucci is knocked away into the surrounding structure.
The characteristic impact is a combination of a high-speed kick and the snapping sensation of a whip.
Phase 5 — Uncontrolled spinning after impact
This is one of the most important details of the technique, and it is what makes Dawn Whip so distinctly Gear 5.
The attack doesn't simply end when Luffy hits his opponent.
Because he releases so much rotational force, Luffy actually struggles to stop spinning.
His body continues rotating at high speed, even after the target has been launched away.
During the continuation:
* His arms and legs are pulled outward and around by the rotation.
* His upper body continues following the momentum of the twisting motion.
* His entire silhouette becomes increasingly distorted, resembling a spinning top or propeller.
* Instead of immediately recovering his normal fighting stance, Luffy loses control of his direction.
* The spinning eventually carries him downward, where he drills into the ground.
In the Egghead fight, he literally spins into the floor, burrowing beneath it like a cartoon drill. This is an actual consequence shown
after the attack, not just a visual effect.
Phase 6 — The exaggerated Gear 5 recovery
Once the rotation becomes uncontrollable, Luffy's movement stops resembling conventional martial arts entirely.
His body acts like an overspun rubber toy.
His limbs and torso can stretch, compress, and deform as the momentum pulls him around. His body remains flexible enough to survive the
absurd movements without conventional physical injury.
The sequence finishes with Luffy underground after his excessive spinning, rather than stopping gracefully in a traditional combat
stance.
This gives Dawn Whip a very unusual combination of characteristics: a genuinely devastating attack followed by a ridiculous,
cartoon-inspired loss of control."
(The message's source links, "Das Wiki für One Piece", "One Place", "Fandom", are left out.)

Then (verbatim): "Noro Noro No Mi Z move should be a held Large cone infront of the player not multiple projectiles  anything caught in
that beam is slowed in time.

While holding the key the beam cannot be reaimed."

Then (verbatim): "Ito Ito No mi double jump tap now has a new move:

Sky Road movement ability

I want this to feel incredibly stylish, fluid, fast, and intimidating. It should NOT resemble ordinary creative-mode flight, elytra
flight, or a player simply floating through the air.

When activated, the player launches into the air with a graceful, almost effortless movement. Extremely thin, semi-transparent white
strings shoot upward from the player's fingertips, extending toward invisible anchor points high above them, as though attached to clouds.

The player becomes suspended by these strings, but rather than hanging motionlessly, they begin running through the sky.

The running animation should be unique. The player's upper body leans slightly forward, their arms spread loosely outward with their
fingers extended, and their legs perform exaggerated, elegant running strides through empty air. Each step should resemble a powerful
midair bound rather than ordinary walking.

With every stride, the strings tighten and pull the player forward, creating the illusion that they're using the sky itself as a pathway.

The movement should feel almost supernatural. The player takes long, floating strides, effortlessly covering enormous distances while
barely appearing to exert themselves.

At higher speeds, the running transitions into a more aggressive aerial sprint. The player leans farther forward, their legs cycle
faster, and individual steps become elongated, almost like they're skating or leaping between invisible footholds.

The strings dynamically detach and reconnect to new positions above and ahead of the player, pulling them along their trajectory. They
must remain visually thin and elegant, never looking like giant ropes or bulky grappling hooks.

Directional movement must be completely fluid. When turning left or right, the player's entire body banks naturally into the turn while
the strings angle toward the new direction. Sharp turns should create elegant, sweeping aerial arcs rather than instant, robotic
rotations.

Sudden acceleration should produce a brief, powerful forward slingshot as the strings become taut and launch the player in the desired
direction.

The player should also be able to stop dramatically in midair. When they release movement, their legs stop running, their body
straightens, and they transition into a relaxed suspended stance with one leg slightly bent and their arms loosely positioned at their
sides.

Add subtle, high-quality visual details: faint white motion streaks behind the player's feet, occasional tiny flashes as strings tighten,
slight body sway during suspended movement, and smooth animation transitions between hovering, accelerating, running, turning, and
stopping.

The player must retain full directional control, including smooth ascending and descending movement. The camera should subtly
communicate speed without excessive shaking.

Most importantly, this ability should feel like the player is confidently sprinting across the heavens, carried forward by nearly
invisible strings. Every movement should feel graceful, arrogant, effortless, and powerful, exactly like Doflamingo.

Do not reuse vanilla sprinting animations or simply rotate the player horizontally into a flying pose. Create a dedicated aerial
locomotion system with proper animations, procedural string rendering, momentum-aware body positioning, and smooth transitions. All
movement must remain responsive and multiplayer-safe in Minecraft Forge 1.20.1."

Then (verbatim): "Remember that Horo Astral Projection the Projection IS IMMORTAL, and can use fruit moves, however the user's real body
left behind is very much damageable, and if killed the projection dies too."

Plan told to the owner: 3.4.2 the Ito fixes, Supa's blades, the Horo projection; 3.4.3 Noro's held cone and Ito's Sky Road; 3.4.4 Goof
Ball and the new Dawn Whip.

Found: the panel (AbilityHud.kit) and the Ability Book (LogbookData.moveKey) read a move's "[M]" through the original's
BindsClient.live -> HakiKeyLabels.of("M"): the original's letter M is its Convergence key, rebound to H since 2.7.1 (PiecePatch
.keyDefaults), so the panel said H while the move listens on M (client/FruitKeyN, PieceKeys.FRUIT_M). The book read "[N]" the same way
(the original's N is slot 8, V since 2.7.1). Goshikito "often doesn't work": it struck where the look met a BLOCK (ClipContext ignores
bodies), so aiming at a mob put the cuts on the ground behind it.

**3.4.2 as built** (nothing tested in game: build EXIT 0 with 13 suites, every new clip and effect rendered and judged in third and
first person; renders docs/handoff/ito-*-3.4.2.jpg, supa-*-3.4.2.jpg, horo-astral-arms-3.4.2.jpg):
- **Keys on the panel and in the book** (client/AbilityHud.kit, client/LogbookData.moveKey + OWN_KEYS): "[N]" and "[M]" moves are named
  by PieceKeys.FRUIT_N / FRUIT_M as bound in Controls (never through BindsClient.live / HakiKeyLabels: the original's letters M and N
  are its Convergence key (H) and slot 8 (V)).
- **The Ito fruit's own clips** (tools/ito_anims.py, client/npc/ItoPoses; "ito_" added to PieceHooks.fruitClip/firstPersonArms, StandUp,
  StylePoser, FirstPersonLight.fist, style/HandMoves): ito_slash_1 (forehand: the hand cocked by the ear, flicked out across; tick 2),
  ito_slash_2 (backhand: the folded forearm flung open), ito_goshiki (hand up by 3, raked down on 5), ito_heat_charge (one arm pointed
  straight ahead, trembling), ito_heat_fire (the recoil), ito_cage_charge (the arm straight up at the sky), ito_cage_cast, ito_cage_close
  (the raised hand yanked down to the hip), ito_knight. Arms and upper body only, lint clean, no hand over the face from the front.
- **Goshikito** (ItoMoves.goshikito/rake; ItoArt.goshiki, drawRake, sweep): the strip from the feet along the flat look (or toward the
  aimed body: PikaMoves.aimed 24, slack 0.15), GOSHIKI_LENGTH 22 long, GOSHIKI_WIDTH 2.4 each side, struck on GOSHIKI_RAKE 5 (12 damage,
  pushed down, lightning through cut()), ConquerorAura.exploded at its middle. Drawn from the posed fist (ItoFx.fist): five strings hang
  up while the hand is raised, swing over (the tips lagging) with a glowing sheet behind them, land along five lines; the cuts glow (a
  dark groove, light laid flat, a strand) and cool by GOSHIKI_LIFE 44; dust along them.
- **Overheat** (ItoKit "[X] hold"; ItoMoves.heatCharge/heatFire, HEAT_FULL 40, HEAT_MOST 96 lets go by itself; OVERHEAT bx HEAT_CHARGE /
  HEAT_FIRE / HEAT_DROP): fired along the full look from the fist at HEAT_SPEED 5, reach 18 to 40 by charge, it stops at the first body or
  block; on arrival UoMoves.explode (2.2 + 2.6c radius, 8 + 10c damage, fire 3 + 4c s; the lightning) and SCATTER heat. Drawn:
  ItoArt.knot (six hot strands spiralling into a banded white-hot ball KNOT_AHEAD 0.55 ahead of the fist; one's own in first person in
  the hand's pass, client/HandPass + ItoFx.handPass, at HAND_KNOT 0.45 size), ItoArt.beam (the rope: ItoArt.rope, a banded tube with two
  wound strands; one's own in first person a red-hot ribbon starting OWN_BEAM_GAP 1.2 ahead: a tube seen end-on is a red wedge), then
  the rope falling apart and cooling over BREAK_LIFE 12; the blast ItoArt.scatter heat (FireMesh fire puffs, the house's banded fire, and
  18 red-hot strands flung out).
- **Hot strands** (ItoArt.tint: HOT, COOLING, BRIGHT palettes; a tinted strand glows harder): never pale wisps (the owner's rule).
- **Black Knight** (ito/StringKnight, a PathfinderMob registered as hexhaki:black_knight, MobCategory.CREATURE (MISC is not proven);
  client/ito/KnightRenderer: the player model in its weaver's own skin, slim or wide, its lashes posed from ito_slash_1/2 through
  StylePoser; ItoArt.woven draws five slim strings wound round it): ItoMoves.knightTick (from its customServerAiStep): weaver gone ->
  discarded; time KNIGHT_TICKS 600 -> burst; fights the weaver's nearest foe within 16 (m_5624_ path, KNIGHT_REACH 2.2, a lash every 14
  ticks: SLASH fx sent as the knight (fxAs), cut 5, the mob it cuts targets it), else follows past KNIGHT_STRAY 6 or roams round the weaver;
  teleported back past 28. HEALTH 40; the weaver's blows pass through; killed, B again, or time up: knightBurst (SCATTER white, 8 damage
  within KNIGHT_BURST 4.5, the lightning, cooldown KNIGHT_REST 200). ItoMoves.foes and the slash skip one's own knight.
- **Birdcage** (ItoKit "[N] hold"; CAGE bx CAGE_CHARGING / CAGE_STANDING / CAGE_CLOSING): held, cageSize grows 8 to 40 over CAGE_FULL 80
  (CAGE_HOLD_MOST 96 casts by itself; ItoArt.stream from the fist to the crown, as high as the cage is wide, its first strings fanning
  down; one's own starts OWN_STREAM_GAP 3 above the hand); cast, it stands CAGE_TICKS 900 at that radius (the strings spread from the
  crown over CAGE_SPREAD 20; cut and pushed in or out at the wall); N again: cageClose over CAGE_CLOSE 100 (ItoArt.closing: smoothstep to
  CAGE_SHUT 1.2), everyone inside pulled to the middle harder as it shuts, cut at the wall; shut: CAGE_DAMAGE 18 and Gore.bleed(5, 200) (the
  original's bleeding), SCATTER white, the lightning, a quake.
- **Budget**: DragonTests.itoStrings now models one charge at a time (stream with knot, or cage with beam) and stays under 16,000; to get
  there: bursts 18 strands, the cage's bars 8 pieces, Break White's strings every third piece, God Thread's glow on every other tip.
- **Supa** (client/supa/SupaClient.renderBody): the original FruitLayer's last argument is "drawn in the world" (the layer's origin within
  8 of the player: false only in a screen's figure); 3.3.5 named it invisible and returned: no blades in the world. Sharper: SupaBlades
  .Shape gained `point` (ARM 0.42, CLAW 0.55, DRILL 0.6, SKATE 0.32: width and wave narrowing to 0 at the tip, thickness to 0.3, the point
  drawn STRETCH 0.45 longer) and thinner steel (thick 1.9 / 1.4 / 1.6 / 1.6); client/sword/KatanaMesh.shaped passes the blade's top;
  tools/supa_view.py mirrors it (DragonTests checks).
- **Horo Astral Projection**: the spirit's arms hang and swing as a player's (AstralArt.floating with speed and the game's attack anim;
  HoroFx.spiritPose); the spirit is immortal (HoroMoves.spirit cancels every blow; below the world it returns to the body), strikes and
  uses the fruit's moves (press/use no longer refuse while astral), but style and sword actions (network/C2SStyleAction, C2SSword) and
  Haki (RyouServer.hakiOff) are refused; the body left behind is horo/AstralBody (hexhaki:astral_body; client/horo/BodyRenderer draws
  nothing, HoroFx draws it as before): every blow on it goes to its owner (HoroMoves.bodyHurt with `forwarding`), mobs that lose the spirit
  target it.

**3.4.3 as built** (nothing tested in game: build EXIT 0 with 13 suites; renders docs/handoff/noro-cone-3.4.3.jpg,
ito-sky-road-3.4.3.jpg, ito-sky-clips-3.4.3.jpg):
- **Noroma Spread held** (NoroKit "[Z] hold", case 1 -> NoroMoves.pressUp; NoroMoves.spread/spreadEnd/coneTick, CONE_ON 193 / CONE_OFF 194):
  the way fixed at the press (State.coneWay: never re-aimed), CONE_RANGE 20, CONE_DEGREES 30, CONE_MOST 80; every CONE_EVERY 4 ticks
  every living foe (seen from the hands: a block clip) and other people's shots inside it slowed (NoroSlow), the lightning on each newly
  slowed body. Clips pika_laser_charge / pika_laser_end (reused, as the beam). NoroArt.cone (rings one every CONE_STEP 1.6 as wide as
  the cone, sliding out, a faint cone of light, motes, a ball at the hands); client/noro/NoroFx CONES (the beam's hum through LoopSound
  while held). Preview NoroShots |ncone:age[:fp].
- **Sky Road** (the owner's spec verbatim above): server/ItoMoves.sky/skyTick/skyEnd (ABILITY_SKY 42 from client/FruitKeyN's air jump):
  mayfly on while it runs (the Horo spirit's way: no server kicks a floater; flying itself forced off), fall forgotten, ends on the ground
  (SKY_GROUNDED 3 ticks), at SKY_MOST 600, riding, dying or in water; SKY_REST 30 before the next; skyFall keeps the drop after it
  harmless. SKY (251) bx 1 on / 0 off. client/ito/SkyRoad: the runner's own client moves it (MovementInputUpdateEvent takes the keys;
  ClientTickEvent END sets the velocity: RUN 0.62 / SPRINT 1.15 a tick, the heading turned at most TURN_RUN 11 / TURN_SPRINT 7 degrees a
  tick (arcs), speed eased, a slingshot (x SLING 1.5) from a standstill and x1.35 on breaking into a sprint, climbing with the look and
  with jump/sneak (CLIMB 0.42), gliding to a stop and bobbing when let go). Every client, for every runner: momentum from its movement
  -> lean (5 to 31 degrees, +10 in a slingshot) and bank (3x the turn, at most 35) round the hips (RenderPlayerEvent.Pre LOWEST / Post
  HIGHEST, only when the Pre ran); clip by speed (ito_sky_hover below 0.07, ito_sky_run, ito_sky_sprint above 0.82, with hysteresis)
  through the original's HakiAnimations.playRemote; two anchors (right hand to the right of the way, left to the left, 14 to 18 up,
  6 + 16 x speed ahead) re-thrown when passed under or 36 away (hovering: kept overhead), a flash on each catch and stride; the feet's
  streak; one's own first person: roll 0.22 x bank, FOV x (1 + 0.09 x speed). ItoArt.sky (a thin string, 0.022 wide, fading to the sky)
  and ItoArt.streak. tools/ito_anims.py sky(): the clips key the legs (it is running) but never the head; firstPersonArms leaves ito_sky_
  out (the game's own hand in first person). The 3.3.7 air steps (Sora no Michi) are gone; ItoKit's passive is "Sky Road".
- Stub: ViewportEvent.ComputeFov.setFOV (proven by HexGhoul's list); srg-shapes regenerated (keySprint f_92091_, isSprinting m_20142_).

**3.4.4 (Goof Ball and the new Dawn Whip): found, planned, NOT started in code** (the owner said "Handoff + prompt, now" here):
- The original: `GearServerController.whip(ServerPlayer)` is PRIVATE static (`dawn(ServerPlayer)` is public; `sendStretch`, `onCooldown`,
  `setCooldown` private). whip(): `GearData.unlocked(player, WHIP)`, `onCooldown(player, "whip")`, `alternate(player, "whipSide")` (the
  leg), dawn -> reach armReach(8.5) x 1.2, damage punchDamage(9) x dawnPower, "§6Dawn Whip" on the action bar, SoundEvents f_11874_,
  `setCooldown(player, "whip", 60)`, `animate(player, "gear5_whip", 2)` (patched to NikaRules.whipClip: gear5_dawn_whip with dawn),
  `sendStretch(player, leg, StretchStyle.WHIP, reach, width, FinalServerPatch.whipAim(flatten(look), player), dawn)`, then
  `ServerTimeline.later(level, WHIP.impactTick() (10), lambda$whip$6)` (its own hit; NOT throwLimb, so NikaPistol.struck is never called).
- The keys: client/gear/NikaKeys sends `GearAction.WHIP` on C's press only (`while (c().m_90859_()) send(GearAction.WHIP)`); Goof Ball is
  held, so its let go needs a message of ours (a C2SNikaWhip(down) as network message 21, PieceNetwork PROTOCOL "15"; C2SNikaJump (20)
  is the pattern).
- Hook (no new branch): a CALL_REDIRECTS row in PiecePatch for whip's `GearData.unlocked(Player, GearAbility)` -> a server/NikaWhip
  .unlocked that starts Goof Ball (no Haki: !GearServerController.dawn) or the new Dawn Whip (dawn) and returns false so the original
  stops there; keep its own cooldowns. For Dawn Whip's stretched rubber leg, either make whip() public in the patch (MethodNode.access)
  and call it at the release tick with a pass-through flag (the original then sends the leg stretch, its hit, its sound, its cooldown and
  the clip through whipClip), or draw the leg ourselves.
- S2CUoFx kinds of the Nika fruit in use: 160 HOLD, 161 LET_GO (GiantHold), 162 HOP, 163 LAND (NikaJump), 164 POW (NikaPistol: POW also
  starts Dawn Pistol's stretched skin, client/gear/NikaSkinClient: never reuse it for the whip); 165 to 179 are free (UoFx routes 160-179
  to the Nika fruit's client classes: check the route before adding).
- Plan, Goof Ball (C without Haki, untransformed or in Gear 5, held up to ~5 s): a cartoon spin (a new gear5_ clip, Toon Force beats in
  gear/ToonForce), then the body hidden (RenderPlayerEvent.Pre cancelled, as HoroFx.asSpirit) inside a spinning cartoon fight cloud (banded
  grey-white puffs swirling, motion arcs) with the player's own skin in it: arms and legs (skin boxes through entityTranslucent, as
  horo/AstralArt draws skins) punching out of it every way, small POW stars (NikaArt.pow, small); every 4 ticks everyone within ~2.6
  blocks punched (small damage, pushed every which way); let go (or the time up): the ball pops, the user dizzy ~3 s (slowness; cartoon
  stars circling over the head; a slight sway of one's own view), then the rest. Sounds: the mod's own CC0 clips (gear/dawn_pistol1
  cartoon punch, quiet and capped; supa/spin loop for the ball) or new CC0 ones checked with tools/tonal_lines.py.
- Plan, Dawn Whip (C with Haki in Gear 5), the owner's six phases: (1) wind-up ~12 ticks, rooted, body coiled (a long negative body yaw
  wind and Toon Force squash), (2)+(3) the release: the rubber leg stretched out (the original's whip called at the release) and the
  body unwinding in fast turns, (4) the lateral sweep hitting all round at the impact tick: struck bodies launched sideways (tangent)
  through blocks as PikaMoves.Launch does (Dawn Pistol/Gekishin), a small comic flash (not POW 164), the Conqueror's lightning (POINT
  ONE), (5) the overspin ~1.5 s (a fast spin clip, arms and legs flung out, Toon Force stretching, the body drifting uncontrolled), then
  drilling down: a shaft ~3 deep torn under the user (Scars.tear, self-repairing; mobGriefing obeyed) and the user lowered into it with
  dust, (6) after a moment underground, popped back up out of the hole with a rubber squash and bounce (never left inside the shaft
  when Scars puts the blocks back). Render every clip (tools/anim_preview.py, face check from the front) and every effect.

## 8zzg. This session (3.4.4, then 3.4.5): Goof Ball and Dawn Whip; the owner's Broken Bones request (verbatim)

3.4.3 was green (Release #134; Build #187 on the handoff commit carries 3.4.3's code: #185 and #186 were only cancelled by newer pushes).
The session started on Default: switch_environment to HexPiece (env_01VDpZP1V4zScExADvRmie8m now) for Freesound.

**The owner's new request during 3.4.4 (verbatim)**, with a zip holding one GIF ("Sequência de fratura do esqueleto pixelizado.gif": a
pixel-art X-ray of a Minecraft player's skeleton, grey bones in a see-through grey body on black, cracking and then shattering into
flying bone pieces; 12 frames of 240 ms, 356 x 358, 2.9 s): "Another Update I want, whenever the Gura Gura No Mi hits the ultimate move N,
this gif I sent fills the screen (in a way that looks good) and plays till the end only once, the user is stunned (and ragdolled)  and a
temporary effect is applied called Broken Bones, this causes the player to have slowness 1 and randomly fall into ragdoll at random times
as long as the effect is active."
Asked (AskUserQuestion), the owner chose: the GIF on **the screen of the player who is hit** (mobs only get the rest); **everyone the
ultimate hits** is stunned, ragdolled and gets Broken Bones (never the Gura user); Broken Bones **20 s, a fall every 3 to 6 s**; the
look **an X-ray overlay** (the skeleton big in the middle, its black a dark see-through tint over the game, a quick fade in and out,
played once). Planned as 3.4.5, after 3.4.4.

The owner, while Goof Ball's first renders were made (verbatim): "the Goof ball is only usable in transformed state so it should have the
skin of the transformed state." So: C untransformed stays the plain Gum-Gum Whip (the original's); in Gear 5 without Haki it is Goof Ball;
in Gear 5 with Haki, Dawn Whip. The ball's limbs and one's own first-person fists are drawn in the Gear 5 skin (the original's
GearJacketSkin.resolve, the same white skin the body wears: HakiSkin's Armament black included).

**3.4.4 as built** (Goof Ball, Dawn Whip; nothing tested in game: build EXIT 0 with 13 suites, every clip and effect rendered and judged
third and first person: docs/handoff/nika-goof-ball-3.4.4.jpg, nika-dawn-whip-fx-3.4.4.jpg, nika-dawn-whip-clip-3.4.4.jpg,
nika-goof-whip-clips-3.4.4.jpg):
- **The hook**: PiecePatch CALL_REDIRECTS row: GearServerController.whip's `GearData.unlocked(Player, GearAbility)` -> server/NikaWhip
  .unlocked: untransformed the original's answer (its Gum-Gum Whip as before, its clip through NikaRules.whipClip, now always the
  original's own); in Gear 5 it starts Goof Ball (no Haki) or Dawn Whip (the original's `dawn`) and answers false (the original stops).
  The cooldown is NikaWhip.REST (Goof Ball 40 after the daze, Dawn Whip 60 after landing; HakiServerController.noCooldowns obeyed).
  NikaRules.allowed refuses the fruit's other actions while NikaWhip.busy (a ball, its daze, Dawn Whip); NikaJump.jumped refuses too and
  NikaJump.walkClip holds the walk's run/stance back (NikaWhip.holds).
- **Keys**: client/gear/NikaKeys: C sends WHIP on the press and network/C2SNikaWhip (message 21, PieceNetwork PROTOCOL "15") on the let
  go (a tap within a tick sends both); releaseHeld lets go too. Panel (AbilityHud.gearKeys): "Goof Ball" with " hold" in Gear 5, "Dawn
  Whip" with Haki, the Gum-Gum Whip untransformed; the Ability Book row says so.
- **S2CUoFx kinds** 165 GOOF, 166 GOOF_HIT, 167 GOOF_POP (bx 1 balled, ticks the daze), 168 DAWN (a the look's way, size the reach), 169
  DAWN_HIT (a the throw), 170 DAWN_DRILL (bx torn), 171 DAWN_POP, 172 STOP; client/dragon/UoFx routes 165-172 to client/gear/NikaWhipClient
  (before the 160-179 Giant route). 173 to 179 still free.
- **Goof Ball** (gear/NikaGoof: SPIN 8, LEAST 12, MOST 100, DIZZY 60, RADIUS 1.05, MIDDLE 0.95): gear5_goof_spin (a cartoon wind-up,
  whirling with the arms windmilling at or under the shoulder and the legs pedalling, looping from 8); from tick 8 the body is hidden
  (RenderPlayerEvent.Pre) and the ball drawn: 16 banded grey-white puffs (NikaGoof.CLOUD palette) churning, 4 speed arcs, ground dust,
  small comic bursts (NikaGoof.burst: Dawn Pistol's POW bands, 8 spikes; NikaArt.pow was too big for the ball), the limbs: 6 skin boxes
  (arms and legs, slim arms 3 px) shot out every PUNCH 4 ticks in new directions, stretched up to 1.55x, the head popping out of the
  top for 5 ticks in 14; only faces turned to the eye; **the Gear 5 skin** (GearJacketSkin.resolve, the owner: "it should have the skin
  of the transformed state"), lit by the world. Server: every 3 ticks up to 6 foes within 2.6 take 2.5 (the game's own invulnerability
  spaces them) and are knocked about (0.55 out at a random angle, 0.22-0.37 up), GOOF_HIT each, a quiet cartoon punch at most every 4
  ticks, ConquerorAura.mixed throttled (never with Ryou in practice: Ryou on makes it Dawn Whip). End: GOOF_POP, the pop (puffs flung
  out, a ring), Slowness III 60 ticks (hidden), gear5_goof_dizzy (the body wobbling round in a circle, arms loose; legs and head the
  player's own), five cartoon stars circling over the head (NikaGoof.dizzy), one's own view rolling +-4 and yawing +-2 degrees. First
  person in the ball: no hands (RenderHandEvent), 8 puffs framing the view and one's own Gear 5 fists jabbing in from the lower corners
  (NikaGoof.firstPerson). Sound: piece.gear.goof (a loop through LoopSound while the ball lasts), goof_punch (dawn_pistol1), goof_pop.
- **Dawn Whip** (gear/NikaDawn: WIND 12, STRETCH_LEAD 10, IMPACT 16, OVERSPIN 44, DRILL 56, POP 66, DEPTH 3, DRILL_STEP 3, REACH 10.2,
  SWEEP_HEIGHT 2.4): gear5_dawn_whip (67 ticks; the old 3.3.2 clip replaced): wound to yaw -284 by 12 (hunched, arms hugging the waist,
  legs crossed and lagging the turn), unwound 160 degrees a tick to 360 at the impact (the leg along the look), the overspin (arms flung
  out under the shoulder, legs splayed, wobbling), the drill (arms wrapped round the chest, legs together, 110 degrees a tick), slowing
  under the ground to whole turns at 66, a constant step to 0. A positive body yaw swings the forward leg to the user's LEFT (tools/anim
  rig): the smear follows that and the struck are launched along (r.z, 0, -r.x) + 0.35 r. gear5_dawn_pop (16): squashed, shot up, the
  skip (the owner's target pose), lowered. Toon Force beats for all four. Server: rooted (motion zeroed, hidden Slowness X renewed) to
  the impact and from the drill to the pop; at 6 the original's own S2CGearStretch (RIGHT_LEG, StretchStyle WHIP, reach 10.2, variant 1:
  Haki-black with the dawn fire) so the rubber leg is out at 16; at 16 every foe within reach and 2.4 of the hips hit 16 (PikaMoves.hit,
  heavy) and launched (PikaMoves.Launch 3.4 a tick, straight 6, through up to obsidian; landed: Craters 1.8, GuraMoves QUAKE, exploded
  lightning, the boom), DAWN_HIT, ConquerorAura.mixed at up to three strikes; 17 to 43 drifting at 0.16 a tick on a wandering heading;
  44 the drill: over the middle of the block under the feet (m_6021_), a shaft torn a layer every 3 ticks (the mouth with its four
  neighbours) through Scars.tear growing back after 300 ticks, mobGriefing obeyed (none: it only spins in place); 66 thrown up 1.05 (0.63
  without a shaft) with gear5_dawn_pop; at 74 put on top of the shaft if still in it; ended on landing (or 40 after the pop); no fall
  damage throughout. Effects (NikaWhipClient): the coil (white spiral tension lines with a Haki-violet edge, dust drawn in), the smear (a
  translucent fan from the hips to the reach trailing the leg, a white and Haki-violet leading edge, speed lines; one fan only: two
  stacked translucent fans blocked each other in stripes), the crack (NikaArt.pow, shock rings along the throw, streaks), the whirl (arcs
  round the body, spun-off dust), the drill (earth puffs in a spiral, stones, a dust ring), the pop out of the hole; first person: no
  coil or whirl round one's own eye, speed streaks sweeping left to right across the view (NikaDawn.firstPerson). Sounds: whip_wind
  ("Rubber Stretch Rip 1" by mickdow 253327), whip_spin ("Whoosh Swirl Spinning" by Qvantor 877983), whip_crack ("Mixed Whip Crack 1" by
  peterbullmusic 351267), the Gura fruit's boom and crumble, goof_pop at the pop. Every new clip as cut within +2.2 to +4.5 dB of plain
  noise (narrow, tools/tonal_lines.py); -15 to -24 LUFS. Rejected: digging 384362 (a 65 Hz hum +20 dB), the cut pull-stretch 645710 (+7.2
  narrow at 118 Hz), Whip Cracks 118068 (+6.6 to +7.1).
- Freesound rate-limits pages and the CDN (403, empty files): tools/gear_sounds.py checks every recording's page on each run; retry with
  a wait (the scratchpad's gear_retry.py pattern: web_sounds.fetch wrapped, 35 s between tries).
- build.py FACE_CLEAR_CLIPS gained gear5_dawn_pop, gear5_goof_spin, gear5_goof_dizzy (every quarter tick from the front); HudTests
  .goofAndDawnWhip (clips, timelines, kinds, budgets: Goof Ball's busiest under 16,000, Dawn Whip's too); SwordTests webSounds("gear",
  ..., 8).

**3.4.5 as built** (Broken Bones; nothing tested in game: build EXIT 0 with 13 suites; the overlay rendered over a scene on a 2400 x 1080
screen: docs/handoff/gura-broken-bones-3.4.5.jpg). 3.4.4's runs were green (Build #188, Release #135).
- **server/BrokenBones** (a MobEffect, PieceEffects.BROKEN_BONES "broken_bones", "Broken Bones" in en_us, HARMFUL 0xD8CDB4): its own
  MULTIPLY_TOTAL -0.15 movement-speed modifier (Slowness I); looked at every 5 ticks (m_6584_): each body's next fall in NEXT (game time),
  then HakiFalls.knockDown FALL 30 ticks and the next LEAST 60 + up to SPREAD 60 later (3 to 6 s). `struck(level, body, stunned)`: the
  effect TICKS 400 (20 s), the first fall only after the strike's own stun (STUN 200 with the Conqueror's Haki, DOWN 100 without), and
  for a ServerPlayer S2CUoFx XRAY 73 sent to that player only (PieceNetwork.sendTo). server/GuraUltimate.strike calls it after a hit
  lands (the stun and the fall there unchanged).
- **client/gura/BonesOverlay** (UoFx routes 73 there, before the Gura route 60-79): one's own XRAY starts it (System.nanoTime, so each
  frame is the GIF's 240 ms whatever the frame rate); RegisterGuiOverlaysEvent.registerAboveAll "hexpiece_broken_bones": the view dimmed
  (fill, 0.62 of 0x05030A), the frame blitted (GuiGraphics.m_280411_ with the sheet's size) at 0.86 of the screen's height in the middle,
  RenderSystem.setShaderColor's alpha for the fades (FADE_IN 150 ms, FADE_OUT 350 ms after the last frame); played once.
- **Assets**: art/broken-bones.gif is the owner's file, unchanged; tools/bones_texture.py lays its 12 frames out 4 x 3
  (textures/gui/broken_bones.png, 1424 x 1074) with the black made see-through (alpha from the brightest channel: clear at 10, solid at
  72: the black background gone, the grey body half seen, the bones solid). tools/bones_icon.py draws the icon (a bone snapped in two,
  18 x 18). build.py NEW_PREFIXES lists both. Tests: HudTests.brokenBones (20 s, 3 to 6 s, the kind its own, the name, the GIF's timing),
  DragonTests (the icon, the sheet's size, alpha and clear background in the jar).

## 9. What to do next (after 3.3.7)

1. 3.4.5 (8zzg: Broken Bones) is pushed: check its Build and Release runs and fix anything red. Then ask the owner how Dawn Pistol
   (3.3.9 to 3.4.1), Horo, 3.4.2, 3.4.3, Goof Ball and Dawn Whip (3.4.4) and Broken Bones (3.4.5) play, and work on what they say.
2. The owner has not tested Horo (3.3.6) or Ito (3.3.7) yet (8zze): ask again how they play. Strings: keep them slim, solid and pointed
   at both ends (8zzd).
3. (Done in 3.3.7: the Ito Ito no Mi, built as the Ice fruit was (8zc, 8ze) and Noro, Supa and Horo are
   (8zy, 8zz, 8zzb), animations reused from the mod's own clips, POINT ONE for the rest (3D art in the house style, first-person hands,
   CC0 web sounds, the Conqueror's lightning out of every explosion). Its strings are thin by nature: make them read as the Light,
   Gura and Magma art does (a glowing core under a wide soft glow, several strands, Overheat's wave of white strings shading purple at its
   heart), never thin pale lines.
3b. Sounds: Freesound needs the HexPiece environment (this session was on it: env_01PmnEXrwDhxzij8M5q6P6bL; if a session starts on
   Default, get_environment lists HexPiece under saved_for_session_repositories: switch_environment, network_only). Check every new clip
   AS CUT with tools/tonal_lines.py (8zzb).
4. Ask the owner how 3.3.2 (the jump, the skip pose) and 3.3.3 (Rubber World's slow waves) feel.
5. The rest of the Nika rework: better Haki versions (Dawn Pistol; the lightning lance never touched). Any raised arm: the hand off
   the face from the front (tools/anim.face_covered views front), and look at it beside the owner's pictures from every side.
6. The owner: "I want gifs when it's all done" (8zr, 8zt): a GIF of each move changed, sent with SendUserFile; previews, not the game.
7. Still unanswered: the Ice fruit after 3.2.2, the Nika keys (3.2.3), the energy (FruitEnergy.COSTS, Gear 5 spending energy?),
   3.1.5 to 3.1.8, the first-person hands (3.1.4), 3.1.2's Gura fixes, the Light fruit and katana fixes, 2.9.5's Conqueror sounds.
8. Lag: the dragon's per-frame CPU skinning; ask the owner where they still see lag.
