# HexPiece 3.0.1

More of your Z notes. Not tested in game yet; the build and tests pass, and I rendered the new slam.

- **Z from above slams them down.** If the light comes down on someone steeply (or from over their head), you appear just above and in
  front of them and bring an axe kick down onto them: they're driven straight into the ground with a burst of light and a quake where
  they hit it, and ragdolled at once. From the side it's still the flying kick that throws them far.
- **Z lasts as long as you hold the key.** The 3-second limit is gone; let go and you're a body again. (There's only a 30-second safety
  stop, in case the game ever misses you letting go, like opening a screen while holding Z.)
- **First person: no trail of light.** Your own trail is only drawn in third person and for other players; in first person you see only
  the speed streaks rushing past (the trail started right at your eyes and filled the screen).
- About "Z sometimes stops working after recasting": in 2.9.9 Z had 3 hidden uses, each coming back 4 seconds after the last one came
  back, so a few casts in a row left it doing nothing for several seconds with nothing showing why. Since 3.0.0 that's one plain
  4-second cooldown after each cast, shown in the move panel. If it still stops working after that, tell me what you did just before.
