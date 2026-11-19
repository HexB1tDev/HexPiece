# HexPiece 2.6.1

The first-person sword, smoothed out frame by frame, plus your other notes. Nothing here has been tested in game yet; the build's
checks pass.

## First-person draw: one clean motion
- **The flip you spotted is gone** (the blade going up, then right with the handle sticking out of the fist, then up again). Once
  the point leaves the saya, the blade now rises in a single arc into your hand. It turns one way only, slows as it settles, and
  never points at the camera.
- **No more spinning fist.** As the blade settled, the fist could spin almost a full turn in one tick (the blade lies nearly
  along your forearm there). The fist now stays steady.
- **The empty saya sinks out of view** instead of vanishing all at once.

## First-person sheathe
- The blade is snapped down to the left (the chiburi), held a moment with its flat toward you, then laid at the saya's mouth and
  slid home. Before, it flicked right, swung back up through the middle, then over to the left. Now it turns one way only.
- The saya rises into view as your left hand comes to its mouth.

## Right click (the Haki slash charge) in first person
- **The charge now has a first-person animation.** The blade is drawn up and back over your right shoulder, your fist at the upper
  right. It trembles harder the longer you charge, then the release swings down from there.
- With the sword sheathed, it is drawn straight up out of the saya into the charge. Yoru comes off your back into it.

## Haki coating on swords
- **Only the blade turns black.** The handle, guard and saya keep their own colours. Before, the handle and guard went black too.
- **It spreads, like your arms.** The black runs up the blade from the guard to the point over about a second, starting just
  after your fist turns black, with a ragged edge rather than a straight line. Turning Haki off, it draws back from the point.
- White glints now run along the black blade, as in your reference.

## Sound
- **The Ryou sound is about 9 dB quieter** (it was a 19-second sound at nearly full volume). The normal Armament sound that follows
  it is unchanged.

## Please test
- First person: draw, sheathe and right-click charge with a katana, Gryphon and Yoru. Look for any frame that jumps or looks wrong.
- Turn Haki on and off with a sword out. The black should creep up the blade and stay off the handle.
- Is the Ryou sound quiet enough now?
