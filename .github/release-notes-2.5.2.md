# HexPiece 2.5.2

Your 2.5.1 report: Ryou's glow, the tap of G, its ring. Also, the full release is cheaper to draw. Nothing here has been tested in
game yet; the build's checks pass. Everything from 2.5.1 is in here too.

## Ryou's glow: softer, and no more flickering
- **Less intense:** the glow round the fists is now a little under half as bright as before.
- **The flickering:** your crash report showed you play on Android (Amethyst with MobileGlues, which translates the game's graphics
  for the phone's GPU). The glow copied the world's depth from one image to another every frame, so walls could hide it, and that
  copy is unreliable on that kind of setup. That is the likeliest cause of the on/off flicker, so the copy is gone. The glow now
  shows through walls, the same as Minecraft's own Glowing effect does.
- If it still flickers, tell me whether it's only in first person, only in third person, or both.

## Tap of G
- **It stuns every mob in its cone now,** wild horses, cows and villagers included, not only hostile mobs and players. They fall as
  the release's victims do.
- **Its damage follows your alignment,** like the releases: good deals none, evil takes half of what's left (never kills), neutral
  deals the original's.
- **Its ring is your Ryou colour.** The rings, the flash and the smoke of the tap were drawn by a part of the original mod that never
  took your colour; now they do.

## The full release, cheaper to draw
None of this changes what you see:
- **Lightning outside your view isn't built.** In first person you stand in the middle of the storm, so about 40% of it is behind
  you or off to the sides. The cut-off is wider than any field of view the game can show.
- **The lightning's flicker is computed about three times faster,** without creating new objects for every point of every bolt on
  every frame (less memory churn, a common cause of stutter). It flickers exactly as before; a test checks that.
- **Invisible strokes are skipped.** About a third of the original's strokes draw nothing but were still worked out every frame.

Altogether, building the lightning takes about 15% less time in third person and about 40% less in first person. If the full release
still lags, tell me whether your FPS drops (stutter) or the server stalls (freeze, then a jump).

## Please test
- Ryou: is the glow soft enough, and has the flicker stopped?
- Tap G at a horse or a cow: they should fall, and the ring should be your colour.
- A full release: smoother than before?
