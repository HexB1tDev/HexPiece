# HexPiece 2.2.1

The Ryou finisher is now HexGhoul's finisher, cut upright instead of across the waist. Drawing and sheathing the katana are slow, and
the blade can't be used until it is fully out (except the quick draw). Nothing here has been tested in game yet: the finisher was
rendered off-game on your skin and judged there, and the build's checks and tests pass.

## The finisher, taken from HexGhoul and turned upright
When a Haki slash released with Ryou on would kill something:
- **The stun**: it is held where it stands for 2 seconds. It can't attack, use items, interact or walk, a mob's mind is switched off,
  and nothing else can hurt it.
- **The line**: HexGhoul's thin, glowing red line draws itself round the body along the blade's upright plane. It starts at the middle
  of the chest's front and runs both ways at once, up over the face and the crown and down through the crotch, and the two ends meet
  in the middle of the back. It sits just past the skin's second layer, and past 3D Skin Layers' voxels when that mod is installed. A
  player being finished also sees a red line run down the middle of their own screen.
- **The cut**: when the line closes, the body falls apart in two halves, each with its own arm and leg. Faced, the cut runs from the
  crown to the crotch; a blade from the side cuts the body front from back. Each half has the body's own skin (players, the pirates
  and marines, zombies, skeletons), and each cut shows what is inside: the skull and the brain's folds, the spine's column of vertebrae
  and discs, the heart and lungs, the guts' coils. Zombies show meat, skeletons dry bone.
- **The fall**: HexGhoul's ragdoll. The halves fall each to its own side and come to rest on whatever is there: slopes, steps, slabs,
  walls. They lie there 40 seconds, then fade over 5. No vanilla death animation plays.
- **The blood**: the original mod's own blood bursts from the cut, keeps dripping from each half as it falls, and pools where the
  halves come to rest.
- Bodies not built like a person (spiders, cows...) get the line round them, then the blood and the usual death. Bosses and anything
  bigger than 3 by 4 blocks are simply hit.

## Drawing and sheathing
- **The draw is slow (about a second)**: with the katana in its saya, a cut or the charge first plays the draw. The hand settles on the
  hilt, the thumb pushes the guard out, the blade slides out of the saya and swings free into the guard. **Nothing can be done with
  the blade until it is fully out**: a cut pressed during the draw is kept and thrown the moment the blade is out, and a charge held
  through the draw begins then. The old instant draw-cuts are gone for players.
- **The quick draw (Ctrl + left click) is as fast as ever**, straight from the saya.
- **Sheathing is slow too (a second)**: the blade is flicked clean, laid to the mouth of the saya and slid home. Until it is home you
  can still cut straight away; once it is home, the next cut needs the whole draw again.

## Network
The finisher's packet changed: client and server must both be 2.2.1.
