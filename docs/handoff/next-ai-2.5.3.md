# HexPiece: handoff after 2.5.3 (Ryou's fist aura as geometry; the release's lightning back under Ryou; the skin readback checked)

Read CLAUDE.md first. This note is the newest. Before it: next-ai-2.5.2.md (the owner's target picture, the device, the two bugs this
version fixes), next-ai-2.5.1.md (2.5.1 and 2.5.2: **the owner's device**: Android, Amethyst, MobileGlues = OpenGL ES 3.2 on an Adreno).

Branch `claude/nice-bell-gam7qf`. **2.5.3 is released** (if the Release run is green: check). **The next version is 2.5.4**
(`python3 tools/next_version.py --write`). Nothing has ever been tested in game except by the owner.

## The owner (session 7, verbatim)
- (with docs/handoff/ryou-fist-target.jpg) "this is the fucking target look can you not do this?"
- "bug: whenever Ryou haki is activated Haki isn't shooting lightning on Conquerors release."
- "Also YES the fishman karate white screen bug is still present, as soon as I select it, the entire screen goes full white."
- (Portuguese) "I hit my usage limit while you were working, but it's been reset. Please continue from where you left off."

## Done (2.5.3)
- **The release's lightning gone under Ryou: found.** 2.4.7's `RyouGlow` ran its post chain and `RenderTarget.blitToScreen` at
  `AFTER_PARTICLES`, in the middle of the level. `blitToScreen` calls `RenderSystem.setProjectionMatrix(ortho)` and never restores it,
  so everything drawn after it in that frame (HakiBolts at AFTER_PARTICLES: the release's lightning; also SlashRenderer, SwordFx,
  AirFx, WaterFx, the weather) was projected flat and clipped away, whenever a Ryou fist was on screen. **RyouGlow is deleted**
  (with its allowlist lines for PostChain/RenderTarget). Rule for the future: never blit or run a post chain mid-level.
- **Ryou's fist aura as geometry** (the owner's picture): `haki/FistAura` (pure, tested) + `client/haki/RyouAura` (drawing) +
  `RyouHands.glow` (calls it per arm, through the original's `renderAdvancedFistGlow` trampoline, as before).
  - FistAura: the coated part of the arm (sleeve-layer size, `TOP` 4.6 px down to the fist's end) bent with `ArmBend`; its silhouette
    from the eye (faces turned to the eye vs away; the edges between them chained into a loop); stations along the loop (every 0.6 px,
    rounded corners every 0.33 rad); rings swept outward in the plane square to the eye's ray (`RINGS` px: an inner fade over the
    black, the hot rim at 0, the band, the haze; `ALPHAS`, `HUES` = rim colour / band / haze); the haze's reach waves with smooth value
    noise flowing up the arm (`flame`, smoothed along the loop: no radial streaks) and trails further where the outline faces up the
    arm; thinner up the forearm (`width`), fading out by `TOP` (`along`); everything pulled `PULL` 1 px toward the eye along its own
    ray (same screen place) so the fist never hides its own rim; grows with distance past `FAR` 120 px so it keeps a size on screen;
    every quad wound to face the eye (`emit`; `mirrored` turns them round); sparks (gold, 6 idle, 14 in a punch, deterministic from
    age + seed). Colours from the Ryou colour (`colours`: rim = 72% to white, band = hue at full brightness, haze = paler and turned
    a little toward pink).
  - RyouAura: `RenderType.entityShadow(white)` (`m_110485_`): the entity shadow's type, alpha blended with **no alpha cut-off** (its
    fragment shader discards nothing; entityTranslucent/Emissive discard below 0.1, which would cut the soft haze), no depth write, unlit,
    back faces culled (fine: every quad faces the eye). The eye in arm space from `inverse(modelView * pose)`; orthographic (the
    inventory) detected through `transformProject`; `mirrored` = det(modelView * pose) < 0 XOR the projection flips the screen (the
    GUI's y-down ortho with the inventory's z-flipped pose: both, so no turn). **In the level the quads are kept (through the pose) and
    drawn at `AFTER_BLOCK_ENTITIES`**, so opaque entities and block entities drawn after the player never paint over the aura (it writes
    no depth); the first-person hand and the inventory draw at once into their own buffers. A failure turns it off for the session.
  - Glossy black: `HandCoat.shine`/`finish` (a few near-white `GLOSS` pixels per hand: the knuckles' front edge with one bright spot,
    a short streak on the arm's front face above the end (the top of a punching fist), a soft one on the outer side; set only once a
    pixel is 75% black); `HakiSkin.compose` uses `finish`.
  - Judged with `tools/ryou_preview.py` (now a wrapper for `tools/preview/RyouPreview.java`: the game's own FistAura/HandCoat/ArmBend
    on a texel-accurate player arm; sheet, flow strip, close-ups beside the target). Not sent to the owner (they don't want renders).
- **The vanishing skin (likely cause fixed):** pure `haki/SkinCheck.real` (a real skin's base layer is opaque: the game clears the
  alpha of the head, body, arms and legs of every downloaded skin, `HttpTexture.processLegacySkin`; so the head's front and the body's
  front must be >= 75% opaque) and `same`. `HakiSkin.read` checks every readback (an empty one is dropped: the player keeps their skin,
  retried each second, every 10 s after 6 misses; `RyouHands.coating` draws the coat meanwhile); a cached copy is re-read at 1, 3, 8,
  20 s and then every minute (`CHECKS`): if the skin's texture object or pixels changed it is made again; either way the copy is
  uploaded again. Gear 5: PiecePatch VIRTUAL_REDIRECTS sends `GearJacketSkin.create`'s `NativeImage.m_85045_` to
  `HakiSkin.readBack`, which throws on an empty readback, so the original closes its copy and retries (its own RETRY) instead of
  keeping it.
- New references (proven, srg-shapes refreshed): `RenderType.m_110485_` (entityShadow), `LivingEntity.m_21324_` (getAttackAnim),
  `RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES` ([forge] evidence), `NativeImage.m_85084_` (getHeight, the original's),
  `Matrix4f.determinant` (the original's). Stubs updated.
- Tests (RyouTests, ~8.5 M checks): the aura from 26 directions x 3 distances x 3 bends x slim/wide x right/left x mirrored: built,
  finite, every quad facing the eye, the rim bright, hugging the fist; deterministic; steady between frames (no point moves > 0.08 px,
  no alpha > 6/255 in a twentieth of a tick); nothing from inside the fist or at no strength; the gloss on the hands only, only once set,
  alpha kept, scaled for 128 skins; SkinCheck (empty, real with an empty outer layer, 128, 64x32, faceless, too small, same).

## Open
1. **The Fishman Karate white screen** (the owner: "as soon as I select it, the entire screen goes full white"). Still not found.
   Searched: CreationScreen/CreationView/PirateUi (the start menu: nothing style-specific but the card colour 0xFF3FC7B5 and texts),
   AbilityHud, CombatMode, StanceLayer (Fishman holds no stance, like Iron Fist), StyleFx/WaterFx (only on moves), server
   ProfileServer.create (nothing per style). The 2.5.3 notes ask the owner where they select it, whether it stays white, whether Esc
   works behind it, other styles, a screenshot. Note: 2.4.7 - 2.5.2's RyouGlow blit could also white/blank parts of the frame on
   MobileGlues, but only with Ryou on, so it is not this. Next idea if the owner says "the start menu": draw the creation screen's
   step 2 with each style picked through GuiCanvas on the real GL path and compare the GL state after (blend, shader colour, depth),
   in particular `PirateUi.line`/`polygon` (the picked card's check mark) under MobileGlues.
2. Waiting on the owner (from 2.4.7 - 2.5.2, see next-ai-2.5.2.md "Waiting on the owner") plus: does the aura look like the picture,
   is the flicker gone, is the release's lightning back with Ryou on, does the skin stay at world start.
3. Possible polish if the owner asks: soft glowing blobs drifting round the fist (the picture has a few; not done), a stronger aura
   while punching (only the sparks grow now), the aura on the forearm too when Gear/Armament coats more of the arm.

## Tools
- `tools/ryou_preview.py [out]` (2.5.3, Java RyouPreview over build/classes). `tools/srg_shapes.py [joined.tsrg]` after a build when
  build.py names a missing SRG member. Mojang names <-> SRG: joined.tsrg (MCPConfig, GitHub raw) + FabricMC intermediary
  `mappings/1.20.1.tiny` + yarn `mappings/.../*.mapping` (branch 1.20.1), all GitHub raw. Vanilla 1.20.1 shaders: misode/mcmeta branch
  `1.20.1-assets`, `assets/minecraft/shaders/core/`. Forge 1.20.1 sources and patches: MinecraftForge/MinecraftForge branch 1.20.1.
- No decompiler in this environment (downloading CFR was refused): read the original with `javap -c -p` over the unzipped jar.
