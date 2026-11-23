# HexPiece 3.3.0

Nika fruit (Gear 5) rework, part 7: Rubber World makes the ground roll like rubber. Not tested in game yet: the build and all tests
pass, and the ground was rendered through a whole field (docs/gifs/nika-rubber-world-3.3.0.gif).

**Rubber World (M in Gear 5, hold and let go)**
- When you let go, three rings of ground swell up around you and roll outward one after another, each lower than the last, fading
  toward the edge of the field.
- After that, while the field lasts, the ground keeps heaving in rounded waves rolling outward, wobbling around the circle like slime.
  When the field ends, the ground settles flat again within a second.
- It is the world's own ground: the real blocks are drawn rising and tilting with the waves. Nothing is placed or broken, and the
  real ground never changes.
- Everyone standing in the field rides the waves up and down.
- The bounce and the no-fall-damage of the field are unchanged. To keep it light, at most 520 blocks move in a frame, the nearest first.
