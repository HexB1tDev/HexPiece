# HexPiece 3.0.7

Your "stuck in this position, can't use left click" bug. Not tested in game yet; the build and tests pass.

- **No more getting stuck in the sword's standing pose.** That pose (right arm hanging a little out from the side) is Ama no
  Murakumo's stance, and while it plays the right arm is held still, so your punches couldn't swing it. It was kept on while the
  server still said "the sword is out", and when the sword went away with the move cut short (switching modes with R, dying,
  putting the fruit away during C), the server never said the sword was gone, so the pose stayed with no sword in your hand. Now the
  pose lasts only as long as the sword really does. If it ever still sticks, tell me what you'd just done.
