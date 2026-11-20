# HexPiece 2.7.7

Two fixes from your testing. Nothing here has been tested in game yet; the build's checks pass and both were rendered and checked
off-game (first person included).

- **First person: the fire comes from the right height.** In first person your own dragon is not drawn (your eye is inside it), and
  its fire used to start up at the dragon's head height, from the top of the screen. Now the breath, the charged ball and the blast
  leave from just under the middle of your view, held to your eye as you move, and go onto your crosshair.
- **The dragon's fire hits what you aim at, in both views.** The dragon's mouth is far above your eye, so its breath, blast and bite
  used to run parallel to your aim, a few blocks above it. They now go from the mouth onto the first block or enemy under your
  crosshair.
- **Blast Breath (C) is a long line of fire:** as the fireball races out it leaves fire burning along its whole way, from your mouth
  to where it bursts (35 blocks), still burning for a moment after. The dragon's charged blast leaves a thick line of fire too.
- Fire right in front of your eye is kept small, so it never buries your crosshair, and the camera is never inside a flame.
