# HexPiece 2.3.9

Fishman Karate's rework, part 2: the Brick Fist, and bodies thrown as rag dolls. Nothing here has been tested in game yet. The
build's checks pass, and the shockwave and the punch were rendered and checked.

## Brick Fist (Y)
- **A punch where you look.** The fist is drawn back to the hip with the other hand sighting ahead, then driven straight out
  wherever you are looking, up or down included.
- **A shockwave of water.** Out of the fist rushes a shockwave: rings of real water racing out along your look, one a tick,
  opening wider and thinner as they go, shedding spray and droplets, gone at 8.5 blocks. A burst of water comes off the fist, with
  a splash.
- **Everything in its path** (about 1.4 blocks around its line) is hurt (9, more with style mastery), thrown hard along it and
  knocked off its feet.
- It replaces the Shark Skin Counter (Fishman's new moves take the specials' places). Opens at style mastery 60; recharges in 7 s.
  Pirate and marine Fishman fighters use it too.

## Rag dolls (taken from HexGhoul)
A body the Brick Fist knocks off its feet goes like HexGhoul's Speed Kick victims. Its head snaps the way it was driven, then the
body twists round and tumbles head first through the air, with water running off it. It comes down flat with a thump and lies there
a moment (1.4 s, slowed to nothing and unable to attack), then gets up. Players and mobs alike.

## Network
The same protocol as 2.3.8 (6).
