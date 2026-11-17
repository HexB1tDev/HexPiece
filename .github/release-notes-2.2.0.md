# HexPiece 2.2.0

The right-click Haki slash, at full charge, is now enormous. Nothing here has been tested in game yet: the slash was rendered at full
size and judged off-game, and the build's checks and tests pass.

## The Haki slash
- **At full charge** the slash is a plane **64 blocks tall** that flies **220 blocks** at 4 blocks a tick. Before, it was 6 tall and
  flew 36. It splits whatever stands in its way: hills, trees, houses. The gash is up to about 2 blocks wide.
- **It grows with the charge**: its size goes with the charge squared, so a half charge is a big slash and only a full one splits
  the land. Damage at full charge is 40 (was 24).
- **It stands up from your feet** (a shallow trench at most 2.5 blocks deep) instead of being centred on your eyes, so it towers over
  the land rather than carving a canyon under you.
- **It looks its size**: the crescent's band widens with its height, and a second band crosses it, so from behind or ahead it reads as
  a tall blade of light instead of a thin line. The crackle along its edge grows with it.
- **The server keeps up**: each slash may now look at 2,400 blocks and cut 240 a tick. Like a vanilla explosion, a plain block it cuts
  drops itself with a chance of one over the slash's size (so a giant cut doesn't bury the world in items). A block with contents, like
  a chest, always drops everything. Bedrock still stops the cut, and protected areas, world limits and unloaded chunks are respected.
- **Pirate captains** keep their old, smaller slash.
- The server settings for the slash's size and damage have new names (`slashHeightLeast`/`Full`, `slashRangeLeast`/`Full`,
  `slashDamageLeast`/`Full`, `cutBlocksATick`), so a world's saved 2.1 values don't keep the old small slash.

## Unchanged
The network protocol is the same as 2.1.4.
