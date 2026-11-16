# HexPiece 2.1.8

The Conqueror's lightning spreads over a whole sphere, randomly every time. Nothing here has been tested in game yet: it was rendered and
judged off-game, and the build's checks and tests pass.

## Lightning in a sphere
- **The Conqueror's lightning now shoots out over a whole sphere around you, in new random directions every time**: up into the sky,
  level, and down. It no longer fans out in a ring on the ground with fixed gaps between the bolts. This covers the charge's bolts,
  the release's bolts and the full-power storm.
- **No blind spots**: each time, the bolts are spread evenly over the sphere (each covers its own share of it) and the whole set is
  turned at random. One release never leaves a big empty side, and over a few releases every part of the sphere gets struck.
- **Bolts that go down** strike the ground where their line meets it, then race along the ground outward, so the ground around you is
  struck too (with the flashes, sparks and scorches of 2.1.6).
- **The full-power storm** is now one big sphere of 8 to 13 huge bolts (up to 120 blocks out), replacing 2.1.7's ring of ground bolts,
  sky bolts and arches. It's not more lightning than before, just spread everywhere. The shockwave ring stays.
- The charge's lightning stuns what it touches along the very same bolts, so a bolt that goes up into the sky stuns nothing on
  the ground.

## Unchanged
The release's gameplay reach (its stuns, pushes and the Supreme Haki's destruction) is the original's. The network protocol is the same
as 2.1.4.
