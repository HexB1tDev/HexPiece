# HexPiece 2.4.9

The Conqueror's release now depends on your alignment, the stun grows the longer you hold G at full, and the sword slash is 1 block
wide. Nothing here has been tested in game yet; the build's checks pass.

## Conqueror's release, by alignment
**Full charge** (hold G until it's full, then let go). The stun grows the longer you keep holding G after the charge fills. If you let
go the moment it fills, it's 30% of the maximum; after 5 more seconds of holding, it's the full maximum.
- **Good:** stuns hostile mobs and players in the release's radius, up to **15 seconds**. It deals **no damage** to anything.
- **Evil:** stuns **everything** in the radius (cows, villagers, every mob and player), up to **9 seconds**. Only what the
  lightning actually hits takes damage. The storm's lightning reaches far past the radius, and whatever it hits out there is
  damaged and stunned too.
- **Neutral** (you didn't say, so it stays as it was): hostile mobs and players in the radius are damaged and stunned, up to
  **10 seconds**.
- Your own horse and anything riding you are never stunned by your own release.

**Partial charge** (let go before it's full), any alignment: anything the lightning hits, mob or player, is knocked down for **2
seconds**.

Everything stunned falls over and lies there, as before, and gets up when the stun ends. Its slowness now ends when it gets up,
instead of lasting the original's fixed 10 seconds.

## The red circle on stunned mobs
- When the release stuns a mob, the circle of particles and the small lightning around it were the original's red. That effect is
  drawn on the mob, and mobs have no Ryou colour. It now takes the colour of the player whose release hit it.

## Sword slash: 1 block wide
- At every charge, the slash's hit area, the trench it cuts and the wave you see are all **1 block wide**. It keeps its height and
  its shape from the side. Before, it was up to 2 blocks for the hit and the trench, and up to 9 blocks for the wave.

## Please test
- A full release as good, evil and neutral (`/hexpiece alignment 100`, `-100`, `0`): who falls, for how long, who gets hurt. Try
  letting go right as it fills, and again after holding at full for 5 seconds.
- A partial release near some cows: the ones the lightning hits should fall for 2 seconds.
- The circle round a stunned mob should be your Ryou colour.
- The sword slash at small and full charge, from behind and from the side.
- And still the evil sounds from 2.4.8, the Conqueror animations from 2.4.7, and the 2.4.5/2.4.6 items.
