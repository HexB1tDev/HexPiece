# HexPiece 2.2.5

The Conqueror's full-power release, as you asked while 2.2.4 was being built. Nothing here has been tested in game yet; the build's
checks and tests pass, and the release was rendered from the game's own drawing code and judged (GIF:
`docs/images/conqueror-release.gif`; the full-charge Haki slash: `docs/images/haki-slash-full-charge.gif`).

## JoyBoy is every full-charge release
- There is no JoyBoy on/off any more: every Conqueror's release at full charge (charge 96% or more, the radial one) is the JoyBoy /
  supreme one, for everyone, at any mastery (its cinematic, its sounds and aftershocks, its storm).
- `/hp joyboy on|off` now just says so. In the menu, the JoyBoy line is open for everyone and says what it does.

## The release looks like your frames
- From a blinding flash where you stand, a few enormous black bolts tear out in every direction (a fresh spread over the whole
  sphere every time): one smooth ribbon each, long straight runs and sharp kinks, leaving the flash thin and swelling to about three
  blocks across, then tapering far out, with a hot rim and a glow in your Ryou colour; now and then a branch. The shockwave sweeps
  the ground under it. Seen from close behind you, you stay visible in the middle of it.

## Fissures that heal, like HexGhoul's
- The bolts that go down strike the ground near you and tear along it outward; under them the ground splits open: jagged fissures up
  to about five blocks wide and six deep where the bolt first strikes, narrowing as it races out, with side cracks, opening as the
  strike reaches each part.
- They grow back on their own 15 to 25 seconds later, lowest blocks first, exactly as they were (HexGhoul's repair, ported): nothing
  drops; chests and anything with a block entity, fluids, unbreakable blocks, redstone parts and the walls behind paintings are never
  torn; nothing seals over a player's head; a block someone placed in the gap is kept; everything is put back when the server stops.
  Protected blocks (spawn protection, claims) are left alone.
- This replaces the original's permanent destruction of the supreme release. Server config: `conqueror.fullChargeFissures` (on).

## Fixed
- 2.2.4's colour change kept the full-power storm from being laid on the terrain (it would have floated); it lies on the ground
  again.

## Unchanged
The network protocol is the same as 2.2.1.
