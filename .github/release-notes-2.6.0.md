# HexPiece 2.6.0

The Conqueror's sounds and stun, as you asked. Nothing here has been tested in game yet; the build's checks pass.

## Sounds
- **Every original Conqueror sound is 35% quieter**: the charge, the release, the full release's old blast and the awakening.
  The evil roar on release is 35% quieter too. It still carries just as far.
- **The evil voice line ("Sawagashii") is much louder, and it is only the voice now.** The rumble and the odd sweep that came
  after it are cut: the sound ends when the voice does (1.2 s). It was mixed for big speakers, with the voice buried under heavy
  bass, so on a phone you mostly heard the rumble. Now the voice comes first: about 4 dB louder where speech is heard, with
  clearer "s" and "sh". It is also the only Conqueror sound that was not lowered.
- **A real explosion the moment you let go of G**, made from free (CC0, public domain) explosion recordings from the web: a
  crack, a heavy boom and a rumble that rolls away over about 3.5 seconds. A full charge gets the full blast, heard up to 64
  blocks away. A partial charge gets a smaller blast, louder the more it was charged. A tap gets a small, higher pop. It plays
  right away, with no one-second delay like the original's sounds.
  - The original's old blast (35% quieter now) still follows a second later on a full release. If you want it gone, say so.

## Stun
- **Tap G: 5 seconds** for everything the tap hits (the original gave 1.5).
- **Hold G: 10 seconds at the shortest hold.** It grows evenly the longer you hold, first while the charge fills, then while you
  keep holding at full charge (5 seconds more).
- **The longest hold: 20 seconds** (evil or neutral) **or 30 seconds** (good).
- Who gets stunned depends on your alignment, as before:
  - A good release stuns hostile mobs and players.
  - An evil full charge stuns everything.
  - Any partial release also stuns whatever its lightning hits.
- A partial release now stuns everything it takes in its radius for its full stun time. Before, it only stunned what its push
  overwhelmed, for 3 to 4 seconds.

## Please test
- Hold G as an evil player until full charge: is the voice line clear now, and does it end with the voice?
- Let go at full charge, at half charge and on a tap: do the explosions sound right? Is the old blast a second later welcome or
  in the way?
- Stun times: tap about 5 s, a short hold about 10 s, a full hold kept at full for 5 s about 20 s (30 s when good).

Next: smoother first-person draw and sheathe animations.
