# HexPiece 2.4.5

Your report after 2.4.4 had five points; all five are in this version. Nothing here has been tested in game yet; the build's checks pass.

## Swords
- **No sheath in your hand in first person.** With the sword drawn (on guard, standing still, after a draw), the first-person hand
  now holds the bare blade. The sheath only shows while the blade is in it. This is fixed for every sword, not just Gryphon.
- **The M1 cuts reach out.** Every cut now strikes with the arm fully straight and ends in a long lunge, like your picture:
  - the front knee bent deep, the body turned side-on;
  - the sword arm thrown straight out from the shoulder, the blade running on along its line and pointing at the target;
  - the free arm stretched straight back.

  The hand slides down to the end of the handle as the arm goes out, for extra reach.
  - The blade's tip now reaches about 2.3 to 2.7 blocks in front of you. Before, it reached 1.5 to 2.2 for an instant, then folded back.
  - The end of the cut is held out in front of you, not wrapped round behind your hip.
  - In first person your arm comes up from the lower right and the blade points at the crosshair.

  This covers the ground, moving and air strings, the three draw cuts and the charged slash.
- **Every sword has its own range.** Every blade still uses the same animations, as you asked. The range comes from the blade
  itself: its length, its grade, and extra for the two legendary blades. Here is how far each reaches past a plain katana's cut:
  - Yoru: +1.86 blocks, the longest by far
  - Gryphon: +0.83
  - Supreme grade: +0.41 to +0.64 (Imperial Phoenix reaches furthest)
  - Great grade: +0.32 to +0.45
  - Skillful grade: +0.10 to +0.33
  - Plain grade: -0.11 to +0.12 (Amber Wasp, the shortest blade, a little less)

  With the new lunge, a plain katana's cut reaches about 3.3 blocks in front of you, and Yoru's about 5.2. Each sword's tooltip shows
  its reach. Swordsman NPCs and the charged slash use it too.

## Conqueror's full release
- **Much less lag.** I measured what a full-charge release costs. Three things were heavy:
  - **The fissures.** They tore out about 10,000 blocks in under a second, and each block meant a lighting update and a chunk redraw
    for every player nearby. Then all of it grew back just as fast. The fissures are now narrower and shallower (about 3,300 blocks),
    and they open and heal more gradually.
  - **The close lightning was drawn twice**, a second copy just after the first: up to 80,000 lightning pieces every frame at its
    peak. The second copy is gone; the big storm bolts still reach out far past it.
  - **The moment of release stalled** while thousands of hidden backup beams were made and thrown away. That is fixed.

  I couldn't tell from here whether you lost frames (FPS) or the server slowed down, so I made both sides cheaper. If it still lags,
  tell me which: does your FPS drop, or does the world freeze for a moment (mobs and blocks stop)?
- **The lightning strikes all around you, somewhere new each time.** Every release already had its own random pattern. The problem
  was that every bolt that came down hit the ground right at your feet and ran outward from there. Now:
  - the close bolts that come down arc out and strike the ground at their own spots, spread all the way round you, from near to far;
  - the big storm bolts that come down glide out and strike the ground at their own distances, and the fissures open from there.

## Network
Protocol 8 (was 7): a 2.4.5 server and client only talk to each other.
