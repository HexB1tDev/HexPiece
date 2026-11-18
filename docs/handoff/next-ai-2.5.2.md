# HexPiece: handoff after 2.5.2 (stopped by the owner: two bugs open, the Ryou fist's target look given)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.1.md (2.5.1 and 2.5.2: the Ryou crash fix and the SRG check,
the animations by alignment, the falling view, the glow softened, the tap's stun and tint, the release's draw cost, **the owner's
device**), next-ai-2.5.0.md (evil half-health damage), next-ai-2.4.9.md (the release by alignment, the 1-block slash), next-ai-2.4.8.md
(the evil sounds), next-ai-2.4.7.md (HUD, Ryou skin and glow, the first Conqueror animations).

Branch `claude/nice-bell-gam7qf`. **2.5.2 is released and green** (Build and Release). The working tree was clean at a1bfa19 when the
owner stopped this session; nothing of the work below is started in code. **The next version is 2.5.3** (`python3
tools/next_version.py --write`). Nothing has ever been tested in game except by the owner.

## The owner's device (read this before touching any rendering)
Android phone, the Amethyst launcher, **MobileGlues** (OpenGL ES 3.2 on an Adreno 810; the game sees "GL version 4.0.0 MobileGlues
1.3.4"), graphics "fast", 3 GB heap. Mods: geckolib, fdlib, architectury, playeranimator 1.0.2-rc1, ldlib, photon 1.1.17, pehkui,
bendylib 4.0.0, libraryferret, citadel, hexpiece. Every GL call goes through a GL -> GLES translation layer. Consequences seen:
additive or culling-dependent geometry draws as solid boxes; framebuffer tricks are unreliable (see the glow below); OpenGL ES has no
`glGetTexImage` (MobileGlues emulates texture readback). Prefer plain alpha-blended, depth-tested geometry that is right without
culling, drawn through the game's own RenderTypes and buffers.

## Open 1: Ryou's fist glow still flickers on and off, and the target look

The owner (verbatim, in order):
- 2.4.6: "the glow around the outline is not meant to be literal painted lines, it's meant to be kinda like the minecraft glow
  effect around the firsts, thats it."
- 2.5.1: "bugs: the glow is too intense, and is flickering on and off."
- 2.5.2: "glow is still flickering"
- Then, with a picture: **"this is the fucking target look can you not do this?"**. The picture is saved as
  **`docs/handoff/ryou-fist-target.jpg`** (1400x788; look at it first).

What the picture shows (Luffy's Conqueror-coated punch; the owner's Ryou colour replaces its red):
- The fist and forearm are **glossy jet black**: the black skin with **white specular highlights** on each knuckle and along the top
  of the fingers (a hard, shiny coating, not matte).
- A **thick, soft aura hugging the silhouette** of the fist and forearm, following its shape closely (not a box, not lines):
  - right at the black edge, a **thin hot rim** (near white, pale pink-orange);
  - then a **bright band in the colour** (red/coral in the picture), a few pixels thick on screen;
  - then a **wide, soft outer haze** (magenta/pink, fading out), its edge **wavy and flame-like**, licking outward and trailing back
    along the arm;
  - thicker round the knuckles and the leading edge, thinner along the forearm.
- **Sparks and embers** streaking past (thin yellow-white streaks), and a few soft glowing blobs drifting round it.

How it is drawn now (2.4.7, 2.5.2): `client/haki/RyouGlow` records each fist's box (wrist to fist, bent with the elbow) on the screen
and, once the level is drawn, draws the boxes into the target of its own `PostChain` (vanilla's `shaders/post/entity_outline.json`),
runs it, and blits the result over the main framebuffer (`RenderTarget.m_83957_` blitToScreen). First-person fists are composited at
the first `RenderGuiOverlayEvent.Pre`. 2.5.2 cut the strength (`INTENSITY` 0.65) and stopped copying the world's depth into the target
(it was the first suspect for the flicker): **it still flickers**, so the framebuffer path itself is unreliable on MobileGlues (render
target switches, the post chain's passes, the blit; vanilla's own Glowing may well flicker there too). Do not try to patch this path.

**Do this instead (recommended):** remove the PostChain glow (`RyouGlow`, its `stage`/`hud` subscribers, the `entity_outline`
chain; keep the class only if something else needs it) and draw the aura as **geometry in the hand layer**, the way everything else
that works on the owner's device is drawn:
1. Where: `client/haki/RyouHands.glow` (called for each arm from the original's `HakiHandLayer.render` and `renderFirstPerson` through
   the patch's TRAMPOLINES row for `renderAdvancedFistGlow`); it has the arm's `ModelPart`, the pose, the buffers, the player, `slim`,
   `right`, and the elbow bend (`bend()`, `bent()` with `ArmBend`). Draw into the `MultiBufferSource` it is given, in the same pose,
   so third person, first person, other players and depth all come for free.
2. Shape: several nested **shells** round the fist and the forearm (the coated part: wrist up to `FIST`, a little up the forearm,
   fading there), each larger and fainter, e.g. 5-6 shells from 0.3 px to ~3 px out (model pixels), **rounded** (a capsule or a box
   with chamfered/rounded edges built from rings of points round the arm's length, so no hard box corners). Bend them with the elbow
   exactly as the hand is bent (`bent()`).
3. The look of a silhouette aura on a solid: per vertex, how much the shell faces the eye (the normal against the direction to the
   camera, in the pose's space): **bright where it is edge-on (the rim), nearly clear where it faces the eye**. That is what makes it
   hug the outline like the picture instead of reading as a coloured box. Inner shells: hot (the Ryou colour mixed toward white); outer
   shells: the colour, then a pinker, fainter haze (shift toward magenta like the picture, or just lower value and alpha).
4. Flame: displace the outer shells' vertices outward with smooth noise that **flows back along the arm** over time (`age`), so the
   outer edge is wavy and licks back; keep the inner rim steady.
5. Draw: **alpha-blended, no additive**, depth-tested, no depth write, **both faces** (or a RenderType without culling): look at what
   `HakiBolts.write` uses for its glow batch (`RenderType.m_269166_()`, its corners' colour = hue at full brightness, alpha = brightness)
   and the sword slash (`SlashMesh`/`SlashRenderer`, alpha-blended, sorted far to near) for patterns that are known to work there.
   Mind the owner's earlier complaint that it was too intense: keep the peak alpha moderate; the rim is the brightest part.
6. The gloss: the black skin is `HakiSkin`/`haki/HandCoat` (pixel by pixel, `HandCoat.coat`). Add a few **near-white highlight pixels**
   on the knuckles and the top of the fist in `HandCoat` (a specular streak), so the black reads as glossy like the picture. RyouTests
   covers HandCoat: extend it.
7. Sparks: now and then a short bright streak or ember off the fist (`client/fx/PieceFx`, `PieceFx.SPARK`/`MOTE`, alpha-blended; see
   `HakiBolts.strike` for the pattern), more while punching.
8. Keep: the Ryou colour (`RyouColors.orNeutral(player id)`), the slow breath (`RyouHands.pulse`) and the flare as Ryou comes on
   (`RyouFx.flare`), the skin's spread (`HakiSkin`, 1 s).
9. Judge it yourself (CLAUDE.md: render, look, fix, render again): `tools/ryou_preview.py` (read it; it renders the Ryou hands), or a
   scratch renderer like `tools/preview/BoltPreview`'s Raster (alpha-blended triangles with depth). Put your render next to
   `docs/handoff/ryou-fist-target.jpg` and compare: the aura must hug the fist's outline, rim bright, haze soft and wavy. Do not send
   the owner renders unless they ask.
10. New Minecraft references: prove them (`verify/api-allowlist.txt` with [srg] evidence, stubs in `stubs/`), and run
    `python3 tools/srg_shapes.py` after a build to refresh `verify/srg-shapes.txt`; `build.py` checks every SRG member's descriptor
    against MCPConfig's joined.tsrg (that check exists because 2.4.7 named the wrong method and crashed the owner's game).

## Open 2: "sometimes the player's skin vanishes when starting a world"

The owner (verbatim): "and sometimes the player's skin vanishes when starting a world." Not investigated past reading the code;
ask the owner if unclear: is the whole body invisible or does the skin turn to the default Steve/Alex; is Armament or Ryou on when it
happens; does it come back when Haki is toggled off and on.

Where to look: `client/haki/HakiSkin` (2.3.4, 2.4.7). While a player's Armament or Ryou is on, every skin the original's
`GearJacketSkin.resolve` returns is passed through `HakiSkin.resolve`, which makes a copy of the skin with blackened hands: it **reads
the skin's pixels back from its GL texture** (`NativeImage.m_85045_` downloadTexture, after binding the skin's texture and reading its
size with `glGetTexLevelParameter`), keeps them (`Entry.base`), registers a `DynamicTexture` (`hexhaki:dynamic/haki_hands/<uuid>`),
and caches the entry per player and skin. The original's Gear 5 skin (`GearJacketSkin.create`) does the same readback.

Likely cause: at world start the player's Haki state comes back on at once (the server syncs it), so `resolve` runs in the first
frames, possibly before the skin texture's pixels are uploaded (a downloaded skin is allocated, then filled on a later render-thread
task), or the readback itself fails: OpenGL ES has no `glGetTexImage`, MobileGlues emulates it. A blank (all transparent) readback
becomes an all-transparent copy, cached for that skin and never read again, and the player is drawn with it: **invisible**. It would
only clear when the skin location changes or Haki goes off.

Fix (recommended):
1. Validate the readback in `HakiSkin.create`: if the image is (nearly) all transparent, or the head's front face (8..16, 8..16 on a
   64-wide skin, scaled) has no opaque pixel, treat it as a failure: return null (the existing `RETRY` waits a second and tries again;
   meanwhile the player keeps their own skin and `RyouHands.coating` draws the coat over the hands).
2. Also re-check a cached entry once a few seconds after it is made (the texture may have been re-uploaded with the real skin): if
   the source texture's pixels differ from `base`, rebuild.
3. Better still, avoid the GL readback for the player's own skin data where possible (the default skins are resources:
   `ResourceManager`; a downloaded skin is a file in the skin cache), but only with proven references.
4. Check the original's `GearJacketSkin` path too (Gear 5's white skin): if it shows the same blank copy, guard it the same way through
   the patch (it is `PiecePatch`'s JACKET/`hakiSkinHook`).
5. Test what can be tested: the validation logic as a pure function over a NativeImage-like int array (RyouTests).

## Waiting on the owner (from 2.4.7 - 2.5.2; ask when they report)
- Whether 2.5.2's tap stuns every mob in its cone and its ring is their colour; the release by alignment (good 15 s no damage, evil
  9 s half health never killing, neutral 10 s), the hold ramp, the partial charge's 2 s lightning stun, the stunned mob's circle colour,
  the 1-block slash, the animations by alignment, the falling first-person view, whether the full release still lags (FPS or server).
- Choices to confirm: neutral hears only the explosion on a full release; the explosion still comes about a second after letting go
  (the original's delay); neutral's 10 s; the ramp 30 % -> 100 % over 5 s held at full.
- Good's release sound (the owner will send it); killing humans -> evil once humans exist; the Fishman Karate white screen (never
  reproduced).

## Tools (session 6)
- `tools/conqueror_anims.py` (the Conqueror animations, all alignments; poses and helpers at the top), `tools/anim_preview.py`
  (contact sheets), `anim.lint`.
- `tools/evil_conqueror_sounds.py` (the evil voice and roar from the owner's clip; measure the band balance, not only LUFS: 2.4.6's
  sub-bass wall was inaudible and pumped).
- `tools/srg_shapes.py` + `verify/srg-shapes.txt` + `build.py check_srg_shapes`.
- The original's code: CFR (Maven Central `org.benf:cfr:0.152`) over input/hexhaki-2.1.0.jar into the scratchpad; MCPConfig's
  1.20.1 joined.tsrg from GitHub raw (`MinecraftForge/MCPConfig/master/versions/release/1.20.1/joined.tsrg`): methods are
  `obf desc m_N_ id` lines, fields `obf f_N_ id`.
