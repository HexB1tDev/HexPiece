# HexPiece: handoff after 2.2.6

Read CLAUDE.md first. This note is the newest; next-ai-2.2.5.md covers 2.2.5 (JoyBoy for everyone, the release drawn like the
owner's frames, self-repairing fissures), next-ai-2.2.4.md 2.2.4, next-ai-2.1.5.md the 3D Haki lightning's first version,
next-ai-2.1.4.md the Swordsman. The next version is 2.2.7. The work branch is `claude/nice-bell-gam7qf` (2.2.5 was on
`claude/cool-einstein-uhqsh9`, same history). Nothing has ever been tested in game (only the owner can).

## The owner, this session (quoted, in order)
- "the energy slice from the swordsman M2 looks horrible, it's meant to be a energy wave, the shape is right but its a solid thing,
  its clearly not a flowing energy beam sort of." Done in 2.2.6.
- "THe haki lightning has to be more 3d with black outlines, all of it, literally all of it". Done in 2.2.6.
- "I dont like the ending tips of the lightning it's like they're stopping in a square (the very tip of the lightning)" (a screenshot
  of the storm's blunt ends). Done in 2.2.6.
- A drawing over the close-up preview (a dome round the player, bolts tearing out of it): "Haki is not meant to come directly from the
  player's body". Done in 2.2.6.
- "Btw the sheath of the blade is never to be black when haki is on, it's just the sword itself." Done in 2.2.6.
- "THe current blades in game all show as missing texture icons (only the icons)". Fixed in 2.2.6.
- Open (below): Galaxy Fist for Iron Fist; Fishman Karate rework; Six Powers air; Gryphon and Yoru.

## Released in 2.2.6
- **3D lightning** (`haki/BoltMesh`): every opaque stroke is `tube3d`, the near half of a tube round its path (rings of 3..9 points by
  its size on screen, mitred, `DEPTH` 0.5 deep toward the eye but lit as round), coloured per vertex by a `Shade` from its normal:
  `facing` (to the eye), `lambert` (to the key light), `spec` (Blinn, ^22). The key light is `VIEW_LIGHT` (view space: above-left,
  a little toward the eye) taken into the frame the quads are built in: `HakiBolts.light(matrix)` in all three draw paths (inverse of
  the pose: two `transformPosition`s, no new JOML reference), `BoltPreview.Raster.light()` in the preview. Domes (`dome`: two rings
  and a crown) on ends and sharp kinks. A coarse ring lights its rims as nearer the middle (`rough`), so a far tube's rim colour does
  not smear over its faces. Hair-thin tubes (< 1.4 px) are flat ribbons of their lit colour.
  - CORE: `coreShade` (deep shadow side, lit side, white-hot middle by facing^5, highlight), lifted `CORE_LIFT` 0.8 radii toward the
    eye so it stands out of the black body round it; ink (`inked`), glow, spurs (3D, inked, pointed).
  - BODY: `bodyShade` (glossy black with a dark sheen of the hue, a highlight, edge-lit in the hue: (1 - facing)^4.5); inked. The
    hue comes from the body's colour: `Bolt.tinted` now keeps a black body as `Bolt.body(rgb)` (the colour scaled to a top channel of
    30: still a BODY by `kindOf`); `DrawCrackle` makes its bodies that way too.
  - EDGE (the storm's rim): the coloured band outside the black body (thinner: body x 1.07 + 1.2 px), then an ink band, then the glow.
  - HEART: draws nothing (a flat white stripe on a lit tube read as a sticker; the core is white-hot down its middle). The original
    lays hearts only inside cores (checked: only `addConquerorRangeLightning` has one, 0xFFFF7889).
  - Ink: `outline` = 0.3 x half + 1.5 px (x the fade), drawn as a ribbon just behind the tube (`ribbon` with lifts; discs at ends and
    sharp kinks); past a pointed end it runs on to a point of its own (`inked`), so the outline wraps the needle.
- **Tips**: `Bolt.pointed` narrows to exactly 0 at the end (test: "a needle, not a square stub"); `BoltMesh.spear` draws the racing
  front of a strike to a point (the last few widths, at most 2/5 of what has raced out); the least widths far away (`tip[i]`) give
  way where a stroke itself thins, so a far tip is not a blunt 3 px stub.
- **Off the body** (`haki/ConquerorBolts`): the shell is an egg centred 1.0 above the feet, `SHELL_OUT` 2.0 out, `SHELL_UP` 1.9 up;
  the mains root on it (`shell`), or on its foot ring on the ground (`ring`) for low directions, and run out to the original radius;
  a downward bolt strikes the ground beyond its root. The server's stun paths are the same function. `offBody(points)` moves every
  point inside the shell onto it; `HakiBolts.launch` applies it to every stroke of an effect played on an entity (not the storm's
  ground strands, found by identity). Tests: roots on the shell, never back through the body, `offBody`.
- **The slash as light** (`sword/SlashMesh.wave`): nothing in the solid batch any more. Layers, all alpha blended: the skin (`shell`
  scale 1: the saturated colour, (0.28 + 0.6 rim) dense, white along the leading edge), the core (`shell` scale 0.5, white-hot by
  facing), `flow` (two sets of bands sweeping back from the edge and up, 0.2 and 0.38 cycles a tick), `tongue`s (see-through flames,
  roots fading into the body, white root to coloured tip, pulses running out; `SPIKE_RING` 10), `streaks` (white dashes racing back
  and up the flanks), `edgeLine` (the white-hot cutting edge), the halo. `Quads.layers`: the sort is layer by layer (halo far, skin
  far, core far, tongues, core near, skin near, halo near, streaks) and far to near inside each: without it, see-through quads that
  write depth (debugQuads does) clipped each other into blocks. Tests `SwordTests.wave` (all light, see-through, it flows, layers in
  order).
- **The saya never black**: the original's `ItemInHandRendererMixin` hands an Armament user's held item black buffers; in first person
  `KatanaItemRenderer.held` draws the sword with them and the saya into `Minecraft...renderBuffers().bufferSource()` (the buffers the
  first-person hand is drawn with). Third person already drew the saya in plain buffers (`SwordLayer`).
- **Icons**: since 1.19.3 only `textures/item`, `textures/block` (or an atlas file) reach the item atlas; the icons moved from
  `textures/piece/katana` to `textures/item/katana` (`KATANA_ICONS`), and `build.py` fails on an icon outside `hexhaki:item/`.
- Preview: `BoltPreview` tints, moves off the body and points every sheet as the game does; draws the glow as the game blends it
  (`glowQuad`: hue at full brightness, alpha = brightness x 0.9; was additive); new `slashgif` mode (a 24-tall wave tracked by the
  camera, night and day, with its crackle). Renders: `docs/images/haki-lightning-3d.png`, `conqueror-release-3d.png`,
  `haki-slash-energy.png`, `haki-slash-flow.gif`.

## Open: the owner's requests, in order
1. **Gryphon and Yoru** (the owner, with `input/Yoru_Gryphon_Models_Textures_1.20.1.zip` (committed in 2.2.6, unaltered), and a
   picture of Mihawk wearing Yoru): "Add these 2 blades to the game in the file I sent, Gryphon sits where katanas are but it's a
   saber, Yoru is big so it's on the back of the player, it's roughly the size of an entire player (make it tied with a rope to the
   player's back and it also needs a pull out animation, these are the 2 strongest blades in the game, YORU is the strongest, Yoru
   never goes black it gets the lightning around it but never turns black with haki on." The picture: Yoru upright down the back,
   the hilt over the head, the cross guard across the shoulders, the blade past the knees. The zip stays in `input/` (never
   altered) and packed like the katanas (`tools/katana_mesh.py`, `build.py katana_assets`); grades: both above every katana, Yoru
   the top.
2. **Galaxy Fist becomes Iron Fist's** (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU
   haki. ONLY for iron fist."). Iron Fist is `FightingStyle.BRAWLER`. The Galaxy Fist is the original's Armament technique
   (`HakiVfx.startGalaxyFistCharge`, ARM_GALAXY_WAVE quick id 103 in the Ability Book/our menu). Remove it from Haki for everyone,
   give it to Iron Fist with Ryou up. Its charge lightning (`addAdvancedGalaxyFistLightning`, in an `EntityEffect` that follows the
   fist) is the one Haki lightning still drawn as the original's flat beams: draw it with `BoltMesh` too.
3. **Fishman Karate rework** (the owner: "make sure the water abilities are super realistic, like the water actually materializes from
   the air particles in the air, and forms realistic water moves"). New moves: "Brick Fist: The user punches where he's looking
   creating a water shockwave, causes knockback and ragdoll (Ragdoll mechanic can be taken from HexGhoul)"; "Water Bullet: The user
   pulls the right hand up into the sky and 5 dropplets of water form on top of the first in a horizontal arch line over the fist,
   then the user pushes the hand forward (pointing forward) and the bullets auto fire and lock on to any entity infront of the user.
   (damage)"; "Water Prison: The user pulls both hands up into the sky (Needs an entity to target) the entity selected gets trapped
   and floats a bit off the ground in a water bubble a bit bigger than the entity's or player's size for 5 seconds. (perfect stun,
   entity can only look around.)" Told the owner: a 5 s perfect stun on players needs a cooldown and a way out (Conqueror's Haki or
   enough damage bursts the bubble) unless they say otherwise; no answer yet.
4. **Six Powers** (the owner: "Improve air particles, speed them up and make them 3d. (finger pistol with haki shoots projectiles)").

## Not verified (only the owner can test in game)
- The 3D lightning's look under real fog and shaders, its cost (a full Conqueror dome near the player ~18-20k quads a frame, about
  as before), the view light's side; the off-body shell for every original effect played on an entity (Last Stand's body bolts now
  crawl over the shell); the slash's see-through layers under Fabulous graphics and with water/clouds behind it; the saya's colour in
  first person with Armament; the icons in the inventory.
