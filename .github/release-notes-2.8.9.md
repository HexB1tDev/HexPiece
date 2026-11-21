# HexPiece 2.8.9

- **Fixed: 2.8.8 would not load** ("Error loading mods ... java.lang.VerifyError: Expecting a stackmap frame at branch target 9").
  One of 2.8.8's changes to the original mod's code was missing a piece Java needs, so the game refused to load it. It is fixed, and
  every build now runs Java's own code checker on every changed class, so this kind of crash cannot ship again.
- Everything 2.8.8 brought is here: the Light fruit's moves animate, Ama no Murakumo is held by its handle, and the clones are rebuilt.
- Nothing here has been tested in game yet. The build's checks pass, including the new ones.
