# HexPiece 3.8.0

The Ito Ito no Mi's Sky Road keeps swinging and keeps its speed. **Not tested in game yet:** the build and all 13 test suites
pass; the swinging was run tick by tick through the game's own numbers and checked in previews. Nothing else changed (the Tori Tori
and the Neko Neko are untouched).

## Sky Road: no more stopping, no more lost momentum
"If possible using the very latest release, please make the Ito ito no mi, not stop swinging or lose momentum, currently both things are
happening which ruin the fun"
- **No time limit.** Sky Road used to shut off after 30 seconds wherever you were. Now it lasts as long as you stay in the air: only
  touching the ground (or water, riding, dying) ends it.
- **Letting go of the keys no longer stops you.** Before, letting go of W for even half a second dropped you into the one-handed hang
  and your speed was gone in half a second. Now your swing keeps going on its own momentum, lets go at the top and the next string
  carries you on, swing after swing. It fades only slowly (about a tenth a swing), and only when you have really slowed down (after
  about ten seconds with no key) do you settle into the one-handed hang. Press a direction again at any time and you swing on at full
  speed.
- **No speed lost when a string catches you.** Each time a new string went tight, the part of your speed going away from it was thrown
  away (up to three quarters of it when you came in fast). Now that speed is turned into the swing. Holding W, you now cruise at about
  21 blocks a second (was about 15); sprinting, about 27 (was about 18).
- **Turning keeps your speed.** The next string used to go wherever your keys pointed, straight across your path, and stop you. Now it
  leans toward where you are already going, so turning bends your swings round in a curve. Holding the key the other way still
  swings you back.
- **The swings stay clear of the ground.** They used to bottom out a block over the ground, so every bump, tree or hill scraped you
  (any wall touched cut your speed to a third) and grazing the ground ended Sky Road. Now your feet stay at least 4 blocks over the
  ground at the bottom of each swing: starting low, the first string pulls you up into the sky, and over a hill you rise with it (sneak
  to come back down).
- **Brushing a wall only stops the way it blocks.** You slide along it with the rest of your speed instead of losing it all.
- **Flatter release when fast.** At speed you let go a little earlier in the swing, so your momentum carries you forward instead of
  flinging you up into the sky.
- The leap (tap jump), the climb (hold jump), the drop (sneak), the tricks and the strings look and work as before.
