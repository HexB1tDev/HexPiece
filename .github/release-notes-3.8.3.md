# HexPiece 3.8.3

The world now generates as an archipelago. **Not tested in game yet:** the build and all test suites pass; the terrain was worked out
from the game's own world generation formulas and checked on rendered top-down maps. **Make a new world** to see it (chunks
already made in an old world stay as they were; new chunks next to them would not match).

## Archipelago world generation
"the world will now generate in Arquipelagos (big islands but most of it will be sea) key is that every single river IS CONNECTED TO
THE SEA no matter what ... Make sure Structures spawn in the islands and the SEA is deeper than normal. (should support every modded
structure) and every island MUST have a beach, long."
- **Islands and sea:** about 70% of the world is sea. The land comes as islands from a few hundred blocks to two or three kilometres
  across, in clusters with open sea between them, all reachable by boat.
- **Every island has a long beach:** every coast has a wide band of sand (about 20 to 60 blocks) sloping gently into the water, and
  then shallow turquoise water before the deep sea. Coasts are always beaches, never stony shores.
- **Deeper sea:** the sea floor is much lower than normal (deep ocean around y 5 to 10 instead of y 35; ordinary sea around y 25 to
  35 instead of y 45).
- **Every river reaches the sea, guaranteed:** rivers are laid so that they can never form a closed loop (a mathematical guarantee,
  checked by the tests): every river runs right across each island from one coast to another, so you can always sail into it from
  the sea and out the other side. Rivers cross each other, making a network of waterways across the bigger islands. River channels
  are kept below sea level along their whole length, so they are never cut off by hills; they cut through as valleys or canyons.
  Rivers are about 25 to 50 blocks wide.
- **Biomes and structures as usual:** only the shape of the land changed. Biomes are placed by the game as always, so
  **TerraBlender and Biomes O' Plenty** biomes still appear, and every structure (vanilla or modded) spawns on the islands and in the
  sea by its biome as on any world. Ocean monuments, shipwrecks and ruins have plenty of deep sea.
