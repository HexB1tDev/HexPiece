# HexPiece 2.3.0

Fixes the crash on boot ("Overlay already registered: hexhaki:hexpiece_combat"). It has been in every version since 2.2.2, so
2.2.2 to 2.2.9 could not start. Everything in those versions (the abilities panel, JoyBoy, the 3D lightning, Gryphon and Yoru, the
slash's foam, the sword sounds) reaches the game for the first time with this one. Nothing here has been tested in game yet; the
build's checks and tests pass.

## The crash
- Forge names the code it generates to call each event handler after the handler's package, its class's short name, the method
  and the event. The combat-mode banner and the abilities panel were both registered by a method `Overlay.register` in the same
  package, so they got the same name: the second one ran the first one's code, and the game stopped on the duplicate banner.
- Each of the three on-screen overlays (the combat-mode banner, the abilities panel, the finisher's screen effect) now has a
  handler name of its own.
- The build now checks every event handler in the jar, the original mod's included, and fails if two of them would get the same
  generated name, so this cannot come back.

## Unchanged
Everything else is as in 2.2.9 (the Swordsman's sounds). The network protocol is the same as 2.2.1.
