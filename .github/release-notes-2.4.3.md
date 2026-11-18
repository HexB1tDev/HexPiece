# HexPiece 2.4.3

Pirates and Marines now have idle gestures that match their rank. Nothing here has been tested in game yet; the build's checks pass.

## Gestures by rank
When there is no fight, an NPC now picks from its rank's own gestures as well as its side's:
- **Captains swagger.** A captain brushes the dust off its coat with the back of its hand and flicks it away, chin up. Or it
  stands chest out and chin up, feet planted wide, rocking on its heels, slowly looking its crew over from one side to the other.
  Captains laugh more, and Marine captains salute less, since they're the ones being saluted.
- **Officers give orders.** An officer throws an arm out to point the crew one way, jabs, sweeps the point the other way, then
  gives a firm nod, its other hand behind its back the whole time.
- **Recruits fidget.** A recruit shifts its weight from foot to foot, wrings its hands in front of it and glances over each
  shoulder. Or it hangs its head, scuffs a toe at the deck, kicks something away and sighs. Recruits also yawn and stretch more.

## Fixed
- **The lookout's hand no longer sinks into its face.** The hand now shades the right brow and turns with the head, and the body
  takes most of the turn.

## Network
The same protocol as 2.4.2 (6). The new gestures are new values of the existing gesture field.
