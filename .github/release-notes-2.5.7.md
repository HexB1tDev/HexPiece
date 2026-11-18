# HexPiece 2.5.7

An urgent fix: 2.5.5 and 2.5.6 crashed the game as soon as you joined any world, new or old. Everything from 2.5.6 (the
first-person draw and sheathing, the blades' new temper) is in here too. Nothing has been tested in game yet; the build's checks pass.

## The crash on joining a world: fixed
- **What it was:** the white-screen fix from 2.5.5 clears the original mod's leftover hit effects whenever you join a world. It
  cleared the hit title to "nothing at all" instead of an empty title. The original checks that title on every frame, and it
  crashed on the very first frame in the world.
- **Now:** every one of those effects is reset to exactly the value the original mod starts it with. The build now checks each
  value against the original mod itself, so this can't come back.
- The white-screen fix itself is unchanged: a leftover flash from another world still can't cover your screen.

## Please test
- Join a new world and an old one: no crash.
- Then, from 2.5.6: drawing and sheathing in first person, and the new temper on the blades.
