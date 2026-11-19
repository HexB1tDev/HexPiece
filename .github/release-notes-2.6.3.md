# HexPiece 2.6.3

A fix for the first-person sword with Haki on. Nothing here has been tested in game yet; the build's checks pass.

- **The sword's first-person animations no longer switch off.** Before, one error drawing your arm (it happened with Haki on) turned
  off the draw, sheathe, charge and held views for the rest of the session. You then saw the plain item: the katana stuck in its
  saya in your hand, no animations. Now an error only skips that one frame.
- **A sheathed katana is never shown in its saya in your hand** in first person. It stays at your hip.
- **The Haki coat reaches the very top of the blade.** The blade was black all the way up, but the white glint is brightest at the
  tip and washed it out. The glint is now faint on a coated blade.
