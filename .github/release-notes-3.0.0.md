# HexPiece 3.0.0

Your notes on Z after 2.9.9. Not tested in game yet; the build and tests pass.

- **The kick puts you right in front of them, facing them.** The light goes 3 blocks a tick and the server noticed the hit a tick or
  two late, so you ended up past them or beside them, sometimes turned back the way you came. Now the light looks a tick ahead (never
  through a wall). Once it meets someone, you glide (2 ticks) to the spot directly in front of them on the side you came from, at a
  height where the kick meets their chest, turn to face them, and the kick lands as you arrive.
- **A cooler reflection sound.** Each reflection now plays a laser "zing" ("Laser Zing Shot" by Qvantor on Freesound, CC0) instead of
  the glass ping.
- **Z has a cooldown now: 4 seconds**, starting the moment you're a body again (when you let go, after 3 seconds, or when you kick
  someone). It shows in the move panel like the others. The old 3 uses are gone: they never showed anywhere, which is why it looked
  like there was no cooldown at all.
