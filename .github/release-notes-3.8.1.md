# HexPiece 3.8.1

The Conqueror's Haki full release (G) no longer lags after you let go. **Not tested in game yet:** the build and all test suites
pass; the release's costs were measured outside the game (how many lightning quads a frame, how many blocks torn and regrown, how many
particles). Nothing else changed.

## The full release's end lag
"Reduce Conqueror's Haki Release (g) end lag, when releasing the full charge of haki it causes a ton of lag for a bit then stops."

What was lagging, measured at Conqueror mastery 1000:
- **The fissures growing back.** A full release tears some 4,000 to 5,000 blocks out of the ground, and every one of them came back at
  its own random moment over ten seconds: a few blocks a tick in every chunk section round you, so the game rebuilt the same sections'
  meshes again and again (about 2,000 rebuilds) for those ten seconds. **Now each chunk section's torn blocks grow back all at once**
  (about 100 rebuilds in all), still 15 to 25 seconds after the release, section by section.
- **The original's particles.** The release's glow, burst, five-shell dome, pressure fronts and three aftershocks threw about 7,000
  big see-through puffs in the first two seconds. **Now each of those bursts keeps about a third of its particles** (small bursts stay
  whole), so the dome, the burst and the shockwaves look the same, only less thick.
- **The 3D lightning storm.** At its peak it built about 22,000 quads a frame (in third person). **Now, only while the lightning on
  screen is over its budget, the tiniest kinks of each bolt (shorter on screen than about 6% of its height) are drawn as one straight
  piece and the tubes nearest the camera get a few less faces**: about 12,900 quads at the peak, with the same bolts, the same big
  zig-zags and the same colours (checked side by side in renders). Everything goes back to full detail as soon as it thins out; every
  other Haki lightning looks exactly as before.
- The fissures still tear open under the storm exactly as before.
