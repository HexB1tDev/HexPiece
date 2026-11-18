# HexPiece 2.4.4

Your feedback on the GIFs: the Water Bullet sat off to one side, the Brick Fist looked weak, and the Haki stun made bodies look up oddly.
Nothing here has been tested in game yet; the build's checks pass.

## Haki stun: everything falls down
- **Every Haki stun now knocks the body down.** That covers the Conqueror's release, its tap, the full charge, running into a
  charging user and the last stand's blast, on any entity: players, vanilla mobs and modded mobs.
- **A small fall.** The body tips over from its feet, mostly straight away from whoever let the Haki out, sometimes to one side.
  It slides back a little, thumps down with a small bounce, and lies there with its head lolled to one side, twitching now and then.
- **It stays down while it's stunned**, at least 2 seconds and longer for a longer stun, frozen as before. Then it gets back up.
- **The old look is gone.** On mobs the stun forced the swimming pose, which only tipped their heads back. Players got the old
  knockdown and pain animations. Neither is used any more.

## Fishman Karate
- **Water Bullet:** the five drops now gather in an arch centred over the head, not over the raised fist off to the right. The arm
  stays raised straight up: a Minecraft arm can't reach over the middle of its own head without going through it.
- **Brick Fist hits much harder:**
  - Five thick rings of water (was three thin ones), opening wider and moving faster, riding a torrent of water out of the fist.
  - A burst of water and spray off the fist, and a heavy splash on everything it hits.
  - It lands like a finisher, with more hit-stop and screen shake, and you hear rushing water and a heavy thump.
  - The punch is planted and stepped into: feet set wide, the body sunk and wound up, then the rear leg drives and the body lunges
    behind the fist.

## Network
Protocol 7 (was 6): a 2.4.4 server and client only talk to each other.
