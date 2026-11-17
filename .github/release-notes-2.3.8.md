# HexPiece 2.3.8

Fishman Karate's rework begins: the Water Bullet, and water that looks like water. Nothing here has been tested in game yet. The
build's checks pass, and the water and the move were rendered and checked.

## Water Bullet (right click)
- **The hand goes up to the sky.** Over the raised fist, the air's moisture is drawn in from all around as a fine mist, and five
  drops of water swell out of it one after another, wobbling, along a flat arch across the fist.
- **Pushed forward, they fire.** The hand is pushed forward, pointing, and the drops fire one a tick. Each one locks onto a foe in
  front (the nearest first; with fewer than five foes they share them) and turns after it as it flies, trailing spray. One that
  finds nothing flies straight on and falls apart into spray.
- **Where it strikes, it bursts:** spray, a ring, droplets flung out that fall, with Minecraft's own splash sounds. Each drop hurts
  (3.5, more with style mastery) and gives a light knock.
- If the move is cut short before every drop has fired, the drops still over the fist fall and splash.
- Pirate and marine Fishman Karate fighters throw it too.
- It replaces the old Water Bullets (three flicked drops). The Brick Fist and the Water Prison come next.

## The water itself
Every drop is drawn as a real body of water: clear in the middle where you look straight through it, bright and denser toward its
edge, with a white glint where the sky catches it and a pale glow on the far side where the light passing through gathers. It
wobbles like liquid, stretches into a teardrop as it flies, and is lit dimmer at night. It's alpha blended (never additive) and right
whether or not your device culls back faces.

## The white screen
You reported Fishman Karate turning the whole screen white. I still can't reproduce it. One likely cause: a large spray particle
spawned right at a player's own eyes fills the whole view with its colour. No HexPiece particle is spawned inside the first-person
camera any more. If the white screen still happens, please send a full screenshot and say when it happens.

## Network
The network protocol is now 6 (2.3.7 and older are refused at login, so both sides must update).
