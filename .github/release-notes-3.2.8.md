# HexPiece 3.2.8

Nika fruit (Gear 5) rework, part 5: Giant holds what it grabs. Not tested in game yet: the build and all tests pass, and the hold was
rendered from every side.

**Giant's grab (right-click as the giant)**
- What you grab now stays in your right fist. Before, the server moved it and everyone's game drew it a few ticks late, so it trailed
  behind your hand when you walked or turned, and sank a little between updates. Now every player's game puts it in the fist itself,
  every tick, so it moves exactly with your hand.
- It sits where the drawn fist really is: the giant's right arm held straight out, the fist wrapped round the body's middle.
- It flies into your hand as your arm comes up, instead of jumping there, and it faces you while you hold it.
- In first person it stays in front of you as you turn.
- Grabbing, throwing and the 15-second hold limit are unchanged.
