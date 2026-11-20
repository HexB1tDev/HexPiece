# HexPiece 2.7.5

Two fixes. Nothing here has been tested in game yet; the build's checks pass.

- **The fruit panel's keys:** the ability HUD no longer shows "no slot" next to the moves. It was looking each move up by its
  place in the list instead of by its own quick slot, so any fruit whose moves are not listed in slot order (the Uo Uo no Mi
  above all) got "no slot" on most lines. Every move now shows the key it is really on: Z X C V B in human form, Z / LMB / X / C
  as a dragon. A move you have taken off every quick slot shows "-".
- **The dragon's colour is chosen once:** the colour is rolled when you eat the Uo Uo no Mi and kept from then on, through every
  transformation, death and rejoin. Eat the fruit again later and you get a new roll. A fruit eaten before 2.7.5 keeps the colour
  of its next transformation.
- In dragon form the panel no longer offers "Back to ... and Haki", since R is locked while you are transformed.
