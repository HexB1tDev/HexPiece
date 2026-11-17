# HexPiece: handoff after 2.2.5

Read CLAUDE.md first. This note is the newest; next-ai-2.2.4.md covers 2.2.4 (silhouettes, Haki keys in every form, the one menu, the
Haki slash as the manga's torn energy wave, every Haki lightning in the Ryou colour), next-ai-2.2.3.md / next-ai-2.2.1.md the finisher,
the slow draw and the abilities panel, next-ai-2.1.5.md the 3D Haki lightning, next-ai-2.1.4.md the Swordsman. The next version is
2.2.6. The work branch is `claude/cool-einstein-uhqsh9`. Nothing has ever been tested in game (only the owner can).

## The owner, this session (quoted, in order)
- "its not meant to be a smooth thing, it's meant to be like this" (a manga panel: a turquoise wave tearing up out of the ground, its
  back torn into spikes): the Haki slash, done in 2.2.4.
- "make sure it matches the color of RYOU always, same for the conquerors haki releases, all lightnings in the game are to reflect the
  same color as RYOU": done in 2.2.4.
- "the hexpiece joyboy on/off, is not a thing anymore, anyone by default at max charge shall be able to do that. (keep and override
  the default max conqueror with the joyboy one since it's better.)" + two anime frames ("this is how conqueror's is meant to be
  released, the circle is where the player is. Like this in all directions.") + "At full charge, it should cause severe tears on the
  floor like fissures where the lightnings hit, then repair themselves automaticly (same as HexGhoul)": done in 2.2.5.
- "wtf are those blobs" (a first render of the new storm: bolts built from many short strokes, each capped: bead chains): fixed by
  building each bolt as one polyline (below).
- "show me a gif of the conqueror's release when its done the full long range view" and "the full view of the full charge sword right
  click thing": `docs/images/conqueror-release.gif`, `docs/images/haki-slash-full-charge.gif` (BoltPreview `gifs`, below).

## Released in 2.2.5
- **JoyBoy for everyone** (server): `PiecePatch.CALL_REDIRECTS` in `HakiServerController.releaseConqueror`: `HakiData.joyBoy` ->
  `server/ConquerorRelease.joyBoy` (true); the new `INTS` table turns its `sipush 1000` (the JoyBoy mastery) into 0; so the supreme
  release (cinematic, sounds, aftershocks, legend) is every radial release at charge >= 0.96. `HakiCommands.setJoyBoy` is a trampoline
  to `ConquerorRelease.joyBoyCommand` (replies through the original's `reply`, widened to public). The menu's CONQ_JOYBOY line is
  open for everyone (`client/LogbookData.haki`). Client: `HakiStorm.release` draws the storm at charge >= 0.96 for everyone.
- **The release drawn like the owner's frames**: `haki/ConquerorStorm` now returns `Strand`s (one polyline per bolt or branch: points
  as heights above the ground, a width per point, a `tear` per point, delay, duration, length, seed): 7..10 bolts along `Sphere`
  directions from a flash at the chest (`START` 2.2, `CHEST` 1.2), runs of ~6 blocks with sharp kinks (each run kinked once more),
  thin out of the flash and full by a third of the way (`width`: 3.6 x size, 0.12 at the root, tapering), branches as their own
  strands. `ConquerorStorm.bolts(strand)`: a `Bolt.BODY` (black) and a new `Bolt.EDGE` stroke on the same points. `BoltMesh`'s EDGE:
  a rim **band** outside the black body and a glow **band** outside the rim (`band`, `glowBand`): never a full ribbon behind the
  body (a wide ribbon twists at its kinks and its back layers poke through: the "pink facets"). `BoltMesh.burst`: the centre flash
  (24-sided, white-hot middle to the colour to nothing, `BURST` 18 ticks), drawn by `HakiBolts` for a storm (`Pending.burst`,
  radius 10 + reach x 0.1). Landings count EDGE strokes. Lesson: very wide ribbons need one polyline (no caps between pieces) and
  coarse kinks (no crackle finer than the width).
- **Fissures that heal** (server): `ConquerorRelease.visual` (redirected from the release's two `visual(...)` calls) picks the seed
  as the original did (`player.getRandom().nextLong()`), sends the original's `S2CHakiVisual` with it and keeps it;
  `ConquerorRelease.fissures` (redirected from `SupremeHakiDestruction.unleash`, the original's permanent destruction) rebuilds the
  same storm (`ConquerorStorm.release(mastery, seed, true)`: the client's mastery is variant / 4 = the same number) and hands it to
  `server/Fissures`: `haki/Fissure.cuts` (pure) turns every grounded stretch into slots across its line (half width 0.6..2.4 x a gape,
  depth 1..6, a V; side cracks), opened the tick the strike reaches them; `Fissures.ground` finds the ground exactly as
  `HakiBolts.ground` does on the client; `server/Scars` (HexGhoul's `TerrainScars`, ported to SRG names, + Forge's BreakEvent per
  block as the slash does) tears blocks out silently (no drops, no block entities, no fluids/unbreakables/redstone parts/painting
  walls) and puts them back 15..25 s later (lowest first, never over a player's head, never over a placed block, all on server stop).
  `haki/ScarLedger` is HexGhoul's ledger (pure, tested). Config `conqueror.fullChargeFissures`. All the Minecraft references come from
  HexGhoul's verified list (`verify/hexghoul-allowlist.txt`); compile stubs added for the block classes and `HangingEntity`.
- **Fixed**: 2.2.4's tint replaced the storm's bolts before `HakiBolts.onGround` looked them up by identity (it would float): the tint
  runs after the ground pass now. `launch` takes the colour's owner separately (`playedAt`: the effect's entity).
- Renders: `docs/images/conqueror-release.gif` (night, from 160 blocks, half speed), `conqueror-release-sheet.png` (from below, third
  person, a cyan Ryou colour, day), `conqueror-release-close.png`, `haki-slash-full-charge.gif` (the 64-tall wave over its 220 blocks,
  from the side). `java -cp build/preview:build/classes BoltPreview gifs <dir>` writes the GIF frames (PIL assembles them).
- Tests: `LightningTests.storm` (strands, widths, thin roots, fissure cuts, the ledger), `SwordTests.wave`.

## Open / ideas (nothing asked yet)
- The fissures are not seen in the previews (they are blocks): only the owner can judge them in game. If they are too big or too
  many, `Fissure.HALF_WIDTH` / `DEPTH` and the side-crack chance are the knobs; `Scars.DESTROY_BUDGET` (700 a tick) caps the rate.
- The original's non-lightning parts of the release (its Photon dome and burst) are its own colours; the owner may want them in the
  Ryou colour too ("all lightnings ..." was about lightning).
- `/hp joyboy` still lists in the original's help text.

## Not verified (only the owner can test in game)
- Everything of 2.2.5 in game: the redirected release (cinematic, aftershocks, the seed reaching clients), the storm's look under
  real fog and sky, the flash's size, the fissures (where they open relative to the drawn bolts, how they look, their repair, the
  BreakEvent cost with protection mods), the JoyBoy line in the menu.
- From 2.2.4: the menu, the wave, the colours (see next-ai-2.2.4.md).
