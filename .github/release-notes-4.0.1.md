# HexPiece 4.0.1

The archipelago's rivers and island ground redone. **A new world is needed to see it.** Not tested in game yet: the build and every
test suite pass, and the terrain was checked off-game on the game's own density functions (seed 2: a 9.6 km map and close-ups).

- **Each island has its own rivers now.** Before, a few endless wavy lines crossed the whole world: they cut straight across the
  islands, some ran along just inside a coast and cut a thin strip off it, and where two met at a shallow angle they flooded half an
  island. Now every island has one river (a big one two) crossing it from sea to sea through its interior, curving gently; two rivers
  on one island cross once, off its middle, at a wide angle. Every river still reaches the sea at both ends (a boat goes straight
  through), none runs along a coast, none makes a ring or a lake. A test walks every river of thousands of islands to make sure.
- **No more hill bands along every river.** The ground beside each river rose into a ridge on both sides, like stripes; now it rises
  gently from the river into rolling hills that vary over the island.
- **No more lagoons cut off from the sea.** Some islands' interiors sank under the sea level into a big shallow lake behind the beach;
  the islands' own ground now never sinks under the sea (the rivers still cut their channels through it).
- **No more specks of water** scattered over the land (a one-block dip every few blocks along hidden lines).

Rivers stay 17 to 28 blocks wide.
