# HexPiece 2.5.1

The crash when activating Ryou is fixed. Also: the Conqueror's animations by alignment, upper body only, and a stunned player's own
view now falls with them. Nothing here has been tested in game yet; the build's checks pass. Everything from 2.5.0 is in here too.

## The crash when activating Ryou
- Your crash report showed it: the glow round Ryou's fists (new in 2.4.7) called a game function under the wrong internal name, so
  the game crashed the moment the glow was first drawn. It now uses the right name.
- If the glow ever fails again for any reason, it just turns itself off and the hands stay black. It can no longer crash the game.
- The build now checks every one of the game functions HexPiece uses (547 of them) against Minecraft 1.20.1's real names. This was the
  only wrong one.

## Conqueror's Haki animations: one set per alignment
All of them move only the upper body now. Your legs stay as the game draws them.
- **Good (peaceful, in pain):** while you charge, one hand is pressed over your face and the other arm clutches your stomach, head
  bowed into the hand. You breathe heavily, the hand trembles more as the charge fills, and now and then you double over. As it
  reaches full, the hand slides off your face and you straighten into a calm, level stare, held almost still.
  - **Full release:** a breath in, then the pressure goes out with barely a movement: your open arms ease out from your sides, your
    chest opens, the stare doesn't move.
  - **Partial release:** the hand snaps down off your face into the stare and your arms ease out a little.
- **Evil (disturbing):** while you charge, your head drops forward limp and your arms hang like a puppet's. As it fills, the head
  creeps up and over to an unnatural tilt, staring sideways. Your shoulders hitch, your head jerks and your fingers twitch, more and
  more often, while your body sways slowly.
  - **Full release:** your head snaps up and back as your arms fly wide and high, back arched, in a shaking laugh. Then your head drops
    limp and your arms fall.
  - **Partial release:** a smaller version with a short laugh.
- **Neutral:** the power stance from 2.4.7 (fists at the waist, elbows out, hunched, shaking; the crunch and the roar), upper body
  only now.
- The tap (a quick G press) is unchanged for everyone.

## A stunned player's own view falls with them
- When a Haki stun knocks you down, your own first-person view now goes over with you. Fall backwards and you end up looking at the
  sky; fall forwards and you're looking at the ground; fall to one side and the world lies on its side. You get up with the view
  when the stun ends.
- Other players already saw you fall; now you feel it too. In third person your body falls as before.

## Please test
- Charge and release (full, then partial) as good, evil and neutral (`/hexpiece alignment 100`, `-100`, `0`), in third person or
  with a friend watching.
- Get knocked down by someone's release and watch your own view.
- Still waiting on the 2.5.0 list (who falls, for how long, the evil half-health damage, the circle colour, the 1-block slash).
