# HexPiece 2.5.5

The "Fishman Karate white screen", found with your screenshots and your log. Nothing here has been tested in game yet; the build's
checks pass. Everything from 2.5.4 is in here too.

## The white screen: found and fixed
- **It was never Fishman Karate.** It was the original mod's hit flash, the white flash a heavy blow sends to the screen. That flash
  remembers when it ends using the world's own clock, and it was never cleared, not even when you left a world.
- In your log you fought marines in one world, then created a new world. A new world's clock starts near zero, so to the flash it was
  still running, and it covered the whole screen in white every frame, for as long as the old world had been played.
- The start menu covered it, so it showed up the moment you picked your style and pressed Set Sail. Only the abilities panel's title
  and the chat box were drawn in front of it, as your screenshots showed. Any style would have done the same.
- Now the original's leftover hit effects (the flash, the combo counter and its title, the camera shake and kick) are cleared whenever
  you change worlds or the world's clock goes back, and the flash is only ever drawn while it is actually running. A leftover flash
  can no longer whiten the screen.

## Please test
- Fight something until you see a hit flash, leave, make a new world, pick Fishman Karate and set sail: the world should show.
- Still to check from 2.5.4: the free head in sword animations, the hand on the sheathed sword in first person, the Haki slash's
  30-second cooldown.
