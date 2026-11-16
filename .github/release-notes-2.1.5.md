# HexPiece 2.1.5

Haki lightning reworked as real 3D lightning with a black outline, plus Haki lightning on the katana. Nothing here has been tested in
game yet: the lightning was rendered and judged off-game, frame by frame, with the exact geometry the game draws, and the build's
checks and tests pass.

## 3D Haki lightning
- **Every Haki lightning bolt is now real 3D lightning.** That covers the Conqueror's charge and release, the dome, the sky breaks and
  rifts, the Last Stand's bolts and arches, the lightning families and the Galaxy impacts. The old flat textured beams are gone.
  - Each bolt is a crackled path in 3D, drawn as a round tube that faces you from every side.
  - It is **inked**: a crisp black outline (about a pixel and a half at any distance), the original's black body, a coloured core
    shaded like a lit tube, a thin white-hot heart, and a soft glow.
  - Walls and bodies in front of a bolt hide it. Fog fades it like the world.
- **It moves like lightning.**
  - A bolt races out from its root with a hot spark at its tip (a long bolt crosses its length in a few ticks), and flashes the
    moment it lands.
  - It flickers tick by tick and dances into a new crackle every two ticks. Short spurs flick out of its kinks.
  - It thins away at the end, core first, outline last. A long-lived bolt re-strikes into a new shape halfway through, and some bolts
    shed forks.
- **Chains and ends.** Long bolts made of several pieces strike along their length, one piece after another. Where a bolt really
  ends it comes to a point; inside a chain it stays full.
- **The original's colours are kept.** Black body, red core, pink heart, crimson aura; Galaxy blue and cyan. Bolts sit exactly where
  the original put its beams, on the same anchor (following the player, or fixed in the world), so the Conqueror's lightning still
  lines up with its stuns.
- **Day and night.** The glow is full at night and about half under a noon sky, so the bolts read by their ink in daylight instead
  of washing out.
- **Performance.** Far bolts use fewer kinks and simpler joints. A cap on live strokes leaves anything beyond it to the original's
  beams. One effect, the Galaxy Fist's charge, keeps the original's crackled beams because it is played its own way.

## Haki on the katana
- **The draw** (the idea from Rayleigh against Blackbeard): with Armament Haki on, lightning spits out of the saya's mouth and leaps
  along the bared steel as you draw, more of it the more steel is bared. A flash runs the whole blade the moment it comes free.
- **The charge**: holding the right button, lightning crackles along the blade, growing with the charge, so everyone can see how full
  it is. **The release** bursts along the blade.
- **Sheathing**: a few sparks at the mouth as the steel goes home.
- **Ryou**: with Ryou up, the drawn blade hums with a faint crackle all the time, in your own Ryou colour (black and red otherwise).
- **The Haki slash** now crackles with the same inked lightning along its leading edge, in its colour.
- All of it uses the same 3D bolts as the Conqueror's lightning, rides on the blade through every cut, and shows in first person
  too.

## The Swordsman's readiness
- Standing still in combat mode, a Swordsman now and then breaks the saya's seal with the thumb (*koiguchi o kiru*, the masters'
  tell before a cut, from studying Shanks, Mihawk and Rayleigh). The guard eases a thumb's width out, holds, and slides home. With
  Haki on, lightning sparks from the gap.

## Unchanged
Katanas are still only given by command or the creative tab. The finisher keeps this mod's own blood. The network protocol is the
same as 2.1.4.
