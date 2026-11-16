# Next AI: 2.1.6 is released (2.1.5: 3D Haki lightning, Haki on the katana; 2.1.6: ground strikes and sound)

Read `CLAUDE.md`, then this, then `next-ai-2.1.4.md` (the Swordsman: where everything is), `next-ai-2.1.3.md`, `next-ai-2.1.2.md`,
`next-ai-2.1.1.md`. The next version is 2.1.7 (`tools/next_version.py`). Answer the owner in English. Never claim in-game testing.

## 2.1.6 (the owner, asked about poses, sound and colours: "Do what you think is best")
- Ground strikes: `HakiBolts.landings` finds, at launch, the free ends of CORE strokes that come down on a non-air block (one per half
  block, at most 12); `BoltMesh.landing` draws an additive flash spreading on the ground (7 ticks) with a hot middle, and a translucent
  scorch (40 ticks; the ink batch honours alpha now); `HakiBolts.strike` throws sparks (PieceFx) and zaps the first three.
- Sound: `HakiBolts.zap` plays the original's `ModSounds.ARMAMENT_AMBIENT_ZAP` locally (as its HakiRenderHooks does): one per launched
  composition at its anchor, one per loud landing, and from `HakiDraw` every third crackle set (once a player).
- Decided, not done: no new idle poses (the owner's earlier "left hand rests on the hilt" stands); Armament-only blade lightning stays
  red/black, Ryou colour with Ryou up.
- Render: `docs/images/haki-lightning-strike.png` (BoltPreview's strike scene).

## What the owner said after 2.1.4 (quoted, voice-typed: "hockey" is Haki)
"They're gonna eventually find them [the katanas] in chests and quests. That is not for you to decide now ... for now, just with a give
command or like in creative." "just keep the blood from that mod. Don't bother picking it up from the other one." "I don't really care
much for the [grades] ... I want a functional version, and I want you to move to the hockey as soon as possible. Like, I want you to
come up with your own little ideas, such as study One Piece, check Shanks, for example, and Mihawk. Their stances, their poses, their
ambient positions ... if you have hockey activated, a cool interaction you could do, like when you unsheath the blade, like lightning
comes out of it like slowly when you're unsheathing it ... Rayleigh does against Blackbeard ... That's just an idea ... We really need to
rework the lightning of the hockey so it's actual 3D lightning. It actually looks good and it feels good to look at, like with a black
outline to it and whatsoever. That is the most important thing about the mod."

## What 2.1.5 did
- **3D lightning, pure and tested** (`haki/`, `LightningTests`): `Bolt` (a stroke: crackled path by self-similar midpoint
  displacement `Bolt.path`, a swelling width, colour, delay, duration, crawl, dance every two ticks; kinds BODY near-black, CORE,
  HEART pale, AURA faint, read from the original's palette), `BoltPainter` (one original beam -> strokes: main, a re-strike halfway
  for long-lived ones, a fork sometimes; `Chain` makes pieces of a long bolt strike one after another at `SPEED` 9 blocks/tick;
  `point` tapers only free ends), `BoltMesh` (one frame of geometry in camera space: a ribbon turned to the eye at every point with
  mitred width and side continuity, round discs at ends and sharp kinks, layers ink outline / body / shaded core / heart pushed
  toward the eye so they never z-fight, additive glow behind with rounded caps, spurs, the strike's leading spark, flash, flicker,
  fade; folded quads split into eye-facing triangles; LOD halves the points when pieces are under 5 px), `DrawCrackle` (the katana's
  crackle: sets of short arcs along the blade and sparks from the saya mouth, a set every 2 ticks).
- **Into the game** (`client/haki/HakiBolts`): `HakiLightning` still makes the original's Photon beams (the fallback) but also paints
  each beam as bolts kept per composition (`HakiBolts.pending`); `patch/PiecePatch.playHook` puts `if (HakiBolts.played*(fx, anchor))
  return;` at the head of `HakiVfx.play` / `playAtWorld` / `playAtFixed` (with an F_SAME frame); `launch` removes the beams from the
  composition, sets the bolts going from the anchor (an entity: feet, turned by Photon's XROT = -(90 + yaw) degrees; else the world
  point) and answers true only if the composition is now empty. Drawn at AFTER_PARTICLES: ink in `RenderType.textBackground`
  (unlit, fogged, depth written, back faces culled), glow in `eyes`. Glow scaled by `ClientLevel.getSkyBrightness` (1 at night, 0.5
  at noon). Budget `MOST` 5000 live strokes. The Galaxy Fist's charge builds its own EntityEffect: it keeps the beams.
- **Haki on the katana** (`client/sword/HakiDraw`, called from `SwordLayer`): Armament on (local: `HakiClientState.armamentOn/acocOn`;
  others: `RemoteHakiStates`) -> the draw slide crackles (mouth sparks + arcs on the bared steel), a flash as the blade comes free,
  a crackle growing with the charge, a burst on release, sparks on sheathing; Ryou up -> a constant faint hum in the Ryou colour
  (red otherwise). Drawn in the blade's own frame by `HakiBolts.drawInFrame` (the eye found by inverting the pose: JOML
  `Matrix4f.invert`). The Haki slash crackles along its leading edge (`SlashRenderer.crackle`, `HakiBolts.drawInView`).
- **Readiness** (`sword/Koiguchi`, `StanceLayer.swordReady`): standing still in combat mode with a katana, after 40 ticks and every
  150, the thumb pushes the guard 1.2 px out (eased), holds 16 ticks, slides home; with Haki the gap sparks.
- Renders judged and kept: `docs/images/haki-lightning-*.png`, `haki-sword-draw.png`, `haki-slash-crackle.png`, `sword-koiguchi.png`
  (`tools/preview/BoltPreview.java` rasterises the game's own quads with a depth buffer and back-face culling).

## Not verified (only the owner can test in game)
- Everything in game: the look under real shaders/fog/bloom-less Photon, performance of a full Conqueror dome (about 20k quads a
  frame near the player), the XROT rotation of entity-anchored effects (taken from Photon 2's source; Photon 1.1.17's was not
  found: if bolts of fist effects appear beside instead of in front, the angle's sign or offset is the first suspect), the first-
  person view of the blade crackle, Fabulous graphics.

## Ideas not done
- A screen-space light flash for very close strikes; lightning lighting the terrain (no dynamic light in vanilla).
