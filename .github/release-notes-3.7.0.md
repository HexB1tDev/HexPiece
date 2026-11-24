# HexPiece 3.7.0

Your five Phoenix notes. **Not tested in game yet:** the build and all 13 test suites pass, and the phoenix and its transformation
were rendered in previews.

## The wings get no shadows, and the phoenix glows
Your words: "the wings should not get shadows" and "it should also glow".
- The dark undersides came from the game's own sun shading, which darkened every face pointing down. The phoenix now ignores it,
  and all of its fire (wings, mantle, tail, crest) is drawn at full brightness, so no side of it is ever in shade.
- A soft cyan glow now surrounds its body and runs along each wing, pulsing gently.

## Fewer particles, and the skin itself moves like fire
Your words: "less particles, more skin moving itself like it's made of fire".
- The flames flying off the body are cut from 64 to 24.
- The body itself now moves more. The edges and tips of the feathers lick up and flicker like flame tips, waves run out along the
  wings, and bright bands of heat flow over the skin from the body out to the tips.

## No more turning human when you switch to third person
Your words: "whenever the player goes into third person it reverts to human and then back to phoenix".
This was a bug. Switching from first to third person made the phoenix's drawing crash, which wiped it until the server re-sent it
about 2 seconds later. It now stays the phoenix in every camera.

## A new transformation: your body slowly becomes the phoenix
Your words: "improve it so the players body slowly becomes the phoenix, arms become the wings and etc. and make the overall phoenix
bigger".
- **Z:** your arms rise out to the sides like wings while blue fire catches on your hands and runs up your arms. Then the fire
  spreads over your body, head and legs, and each part turns into swirling blue fire as it goes.
- The phoenix then opens out of that figure of fire. At first it is shaped like you, with its wings straight out where your arms
  were. It grows slowly to full size and takes its own shape as the fire figure burns away into it. The whole transformation takes
  2 seconds.
- **Turning back** does the same in reverse. The phoenix shrinks back into your shape and the figure of fire, you appear inside it,
  the fire burns off your body and down your arms to your hands, and your arms come down.
- The phoenix is about 1.4 times bigger, with a wingspan of about 10 blocks (it was about 7). The camera pulls back further to fit
  it.
