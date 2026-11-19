# HexPiece 2.6.9

The crash you sent, and very likely the lag and flicker with Haki on in first person. Nothing here has been tested in game yet; the
build's checks pass.

- **Found it: an endless loop in first person with Haki on.** Drawing your arm calls Haki, and Haki was drawing your arm again,
  over and over every frame, until the game gave up on that frame. That was the extreme lag and the blade dropping in and out. On
  2.6.8 the game then crashed trying to write the error to the log.
- **Fixed:** while your Haki arm is being drawn, the inner call no longer loops back into Haki. Your arm is drawn once, already
  blackened, then its coat.
