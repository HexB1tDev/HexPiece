# HexPiece 2.4.8

The evil Conqueror sounds, fixed after your test. Nothing here has been tested in game yet; the build's checks pass. Everything from
2.4.7 is in here too (new Conqueror charge and release animations, the old HUD gone, Ryou's black hands, the old release sounds
removed).

## Why the evil sounds were hard to hear and glitchy
- My bass boost was the problem. It piled almost all of the sound into very deep sub-bass, about 12 dB above everything else. Laptop
  speakers and most headphones cannot play that. The loudness limiter then kept clamping down on that deep rumble, which pushed the
  part you can actually hear (the voice and the roar) down and made it pump and stutter. 2.4.7 made it worse, so do not judge it on
  2.4.7.

## What changed (all four sounds: the voice line and the release roar, man's and woman's)
- **The deep rumble no speaker plays is cut,** so it no longer takes up the volume.
- **The bass boost is now where speakers can play it** (the punchy low end, around 90 to 160 Hz). The man's roar has the most bass,
  then the man's voice line. The woman's versions have lighter bass, as before.
- **No more crushing:** gentle compression and a slow limiter, so nothing pumps or crackles.
- **Louder where you hear it:** the voice and the roar are about 4 to 5 dB louder than in 2.4.6 for a man, and about 2.5 dB louder
  for a woman. They are still your clip, with the same words and the same roar.

## Still the same as 2.4.7
- A full release now plays only the explosion and, for an evil player, the roar. The crackling lightning at the end and the old snap
  and rumble are gone for every player.
- The voice line plays once when a full charge is reached, and the roar plays the moment you let go.

## Please test
- The evil voice line and roar, as a man and as a woman. Use `/hexpiece alignment -100` to be evil.
- The Conqueror animations, the black hands and their glow, the HUD, and anything still waiting from 2.4.5 and 2.4.6 (see the 2.4.7
  notes).
