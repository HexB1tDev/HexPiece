# HexPiece 2.2.9

The Swordsman gets real sword sounds: the draw, the sheathe, every swing and the Haki slash. All of them come from the web and are
free to use: every clip is from a CC0 (public domain) pack on OpenGameArt.org, and each one is credited (its pack, author, licence and
page) in `assets/hexhaki/sounds/piece/sword/SOURCES.txt`. Nothing here has been tested in game yet; the build's checks and tests
pass, and every clip's loudness over time was plotted and checked against the animation ticks it plays on.

## What you hear
- **The slow draw**: the thumb pushes the guard (a click), the blade slides out of the saya, and it rings free as it clears the mouth.
- **The sheathe**: the flick that shakes the blade clean, the slide back in, and the guard clicking home as the blade seats.
- **Every cut** swishes on its first hit tick. Heavy cuts and finishers make a deeper rush with steel ringing through it. The Wheel
  and the Pivot Cut swing again halfway round.
- **The draw cuts** (Draw Cut, Dashing Draw, Falling Draw) slide out of the saya first. The **quick draw** slides, rings and rushes
  down the line.
- **The charged cut** rings as it is let go. A **Haki slash** adds a long rush of air as the wave flies off, louder (and heard
  farther away) the more it was charged.
- **Yoru** has its own, slower and deeper: the hand closing on the hilt, the great sword pulled up out of its ropes and swept over the
  shoulder. Its cuts are lower than a katana's; **Gryphon**'s saber is a little higher.
- **NPC swordsmen** play the same sounds as players (until now they swung in silence).
- The clips are cut so each hit lands on its tick (some downloads started with up to half a second of silence or rustle). Subtitles
  name each sound.

## Unchanged
The network protocol is the same as 2.2.1. Sounds play on the server for everyone near, through the game's own sound events
(`hexhaki:piece.sword.*`).
