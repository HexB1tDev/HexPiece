# HexPiece 2.4.6

Good and evil, and your evil Conqueror sound. Nothing here has been tested in game yet; the build's checks pass.

## Alignment: good and evil
- **Every player now has an alignment** from -100 (pure evil) through 0 (neutral) to +100 (pure good). It is saved with your HexPiece
  profile and starts at 0.
- **The bar is in the mastery menu** (the Captain's Log, first page, under the Haki bars). Pure evil is at the left end, neutral is
  the mark in the middle, pure good is at the right end. It fills red towards evil and gold towards good, with a pointer at your value
  and its name beside it (Pure Evil, Evil, Neutral, Good, Pure Good).
- **Command for testing:** `/hexpiece alignment <-100 to 100>` sets yours, and `/hexpiece alignment` tells you where you stand.
  Like the other /hexpiece commands, it needs operator rights.
- **Evil is -25 or lower, good is +25 or higher**, and everything between is neutral. Tell me if you want those lines somewhere else.
- Killing will make you evil once the humans are in. Nothing changes your alignment yet except the command.

## The evil Conqueror's sounds
Made from the clip you sent. It is two parts, the "Sawagashii" voice line and then the release sound, and I split it into those two.
- **Full charge:** when you are evil, holding G and the charge reaches full, the voice line plays once. Everyone nearby hears it.
- **Full release:** when you let a full charge go, the release sound plays on top of the original's blast and rumble.
- **Bass boosted and much louder:** about 11 dB louder than your clip, with heavy bass.
- **By gender:**
  - A man hears your voice line as it is, with the most bass.
  - A woman gets a woman's version: raised 7 semitones, with the voice's character raised too, so it sounds like a woman and not a
    sped-up man. It has a lighter bass boost and the same loudness. The release sound is raised 2 semitones for her.
- **Good** will get its own release sound when you send one. Neutral keeps the original sounds.

## Network
Protocol 9 (was 8): a 2.4.6 server and client only talk to each other.
