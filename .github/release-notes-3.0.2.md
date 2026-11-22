# HexPiece 3.0.2

Your note on the Light fruit's right click. Not tested in game yet; the build and tests pass, and I rendered both.

- **Tap: just the laser.** A thin laser from your fingertip with a small glint where it hits; it still hurts whoever it hits, but no
  explosion at the end any more (no blast, no explosion sound).
- **Hold: the explosion lingers.** The blast at the end of the charged beam was drawn as part of the beam, and the beam is gone after
  12 ticks, so the blast vanished mid-explosion. Now it is its own effect and plays out in full: the light gathers, a flash and a dome
  of light, a ball of light with dust thrown up, then the dust cloud fading out over about 3 seconds (bigger and longer the longer you
  charged). The same goes for the beam V fires out of the leg when there's no one to kick.
