# HexPiece 2.1.9

Three of your bug reports fixed. Nothing here has been tested in game yet: the fixes were rendered and judged off-game, and the build's
checks and tests pass. The bigger Haki slash, the finisher in HexGhoul's style and the single HUD come next.

## Fixed
- **See-through sheaths.** Seen from a few blocks away, a sheath switches to a simpler mesh, and that mesh had slits along it where the
  simplifier pulled vertices apart. You could see the sheath's inside and the world behind through them. The simpler meshes are now
  closed: every merged vertex shares one position, and the sheath's inner wall (never seen from a distance) is left out so it can't
  show through as dark specks.
- **Black sword swings.** The swing trail, and every glow added by this mod (the Haki lightning's glow, its ground flashes, the
  shockwave, the Haki slash's light, the blade's shine, Ryou's hand rim), now uses plain transparency instead of light added on top.
  Some setups draw added light as an opaque sheet, which is why your swings came out as black slabs. A swing is now a pale steel
  streak that fades out.
- **Haki blackened the whole arm.** Armament Haki and Ryou now blacken only the **hands**: from the wrist to the fist, fading in over
  the wrist, in first and third person. Your sleeve and skin show above it, and Ryou's coloured rim still outlines the black hand.

## Unchanged
The network protocol is the same as 2.1.4.
