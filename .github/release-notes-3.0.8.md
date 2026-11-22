# HexPiece 3.0.8

Your note on the drawn katana's pose. Not tested in game yet; the build and tests pass.

- **The katana's ready pose no longer covers other animations.** With a sword drawn and held ready (like Yoru low at your side), its
  pose sat on an animation layer above the Haki and fighting-style moves, so whatever those moves did with your body was hidden under
  it. Now the ready pose (standing and walking with the sword out) sits under every other animation: any move plays over it, and it
  comes back once the move is done. The sword's own cuts, draws and sheathings are as before. If a particular move still doesn't
  show with a sword out, tell me which one.
