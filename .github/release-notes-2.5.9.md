# HexPiece 2.5.9

An urgent fix for the server crash you sent. Everything from 2.5.8 (first person with an empty hand, your arm with Haki on) is in
here too. Nothing here has been tested in game yet; the build's checks pass.

## The crash with the Water Bullet: fixed
- **What it was:** when a Water Bullet stopped before all its drops were fired (you or the fighter using it got hit, died, or the
  move was cut short), HexPiece dropped the leftover drops. But it did that in a way that broke its own list of Water Bullets
  in progress, and the whole world crashed.
- **Now:** the leftover drops fall as intended, and nothing that happens during that step can break the list. Any error in the
  water's own handling now just removes that one drop or bullet; the game keeps running.

## Please test
- Use the Water Bullet and get hit (or stop it) halfway, and fight a sea fighter who uses it: no crash.
- From 2.5.8: first person with an empty hand, and your arm with Haki on.
- Still waiting on your answer: which swords showed no draw (Gryphon and Yoru draw on entering combat mode, the katanas on your
  first cut).
