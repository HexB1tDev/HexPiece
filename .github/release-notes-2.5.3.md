# HexPiece 2.5.3

Your reports after 2.5.2: the Ryou glow still flickering (with the picture of the look you want), the skin vanishing at world start,
no lightning on the Conqueror's release while Ryou is on, and the Fishman Karate white screen. Nothing here has been tested in game
yet; the build's checks pass. Everything from 2.5.2 is in here too.

## Ryou's fists: the look from your picture
- **A new glow, built from scratch.** The old one was a screen effect laid over the whole picture, which is what kept flickering on
  your phone. The new one is plain see-through shapes drawn round each fist, the same kind of drawing your phone already shows
  correctly for shadows and other effects.
- **It hugs the fist's outline from every angle,** in first person, third person, on other players and in the inventory, and it bends
  with the elbow like the arm does:
  - a thin, white-hot rim right at the edge of the black;
  - a thick, bright band in your Ryou colour;
  - a soft, pinker haze beyond it, with a wavy, flame-like edge that drifts back along the arm;
  - gold sparks streaking off the fist, more of them while you punch.
- **The black is glossy now:** a few near-white highlight pixels on the knuckles and the top of the fist, like the shine in your picture.
- It still breathes slowly and flares as Ryou comes on. Walls in front of the fist hide it, as they should.

## Lightning on the Conqueror's release with Ryou on
- **Found and fixed.** The old glow drew its screen effect in the middle of drawing the world, and that left the game's camera
  settings broken for everything drawn after it in the same frame. The release's lightning is drawn after it, so it vanished whenever a
  Ryou fist was on screen. Sword slashes, Six Powers' air and the water moves could vanish the same way. The old glow is gone, so they
  all show again.

## Your skin vanishing at world start
- **Likely fixed.** While Armament or Ryou is on (or Gear 5), the mod copies your skin to paint the hands black (or the body white).
  At world start that copy could come back empty, and an empty copy drew you invisible. Now:
  - every copy is checked: an empty one is thrown away, you keep your normal skin, and it tries again a second later;
  - the copy is checked again a few times in the first seconds (your downloaded skin can arrive just after the default one) and then
    every minute, and it is made again if your skin changed;
  - Gear 5's white skin gets the same check.
- If it still happens, tell me: is the whole body invisible, or does the skin turn into Steve or Alex? Was Armament, Ryou or Gear 5 on?

## Fishman Karate white screen
- **Not found yet.** Nothing that Fishman Karate draws covers the screen, so I need a little more from you:
  - Where do you select it: the start menu's "How do you fight?" page, or somewhere else?
  - Does the screen stay white? Can you still move or open the menu (Esc) behind it?
  - Does it happen with any other style?
  - A screenshot of the white screen would help a lot.

## Please test
- Ryou: does the glow look like your picture now, and has the flicker stopped?
- With Ryou on, a full Conqueror's release: is the lightning back?
- Start a world a few times with Armament or Ryou on: does your skin stay?
- The questions above about the Fishman Karate white screen.
