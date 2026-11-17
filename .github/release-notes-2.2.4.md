# HexPiece 2.2.4

Your list from 2.2.3, plus the slash and colour notes you sent while it was being built. Nothing here has been tested in game yet;
the build's checks and tests pass, and every picture below was rendered from the game's own drawing code and judged before release.

## The start menu
- **The two figures are plain black silhouettes** now: a man (broad shoulders, arms hanging a little away) and a woman (long hair
  to the shoulders, narrow waist, hips, slimmer arms). No face, no clothes, no blocks: smooth outlines drawn in quarter pixels, so
  they stay clean at any GUI scale. (`docs/images/start-menu-1-gender.png`, `start-menu-silhouettes.png`)

## Haki's keys work in every form
- Ctrl + R (Armament), V (Observation), the Haki leap and the Conqueror's quick slots work whatever style you use, in combat mode or
  out of it. The original's "powers off" switch (X without the Gomu fruit) used to silence all of Haki as well; now it only turns
  the fruit's powers off. (In the Gomu fruit's own Devil Fruit mode those keys are still Gear 5's moves, as before.)

## One menu
- **P opens one menu, the Captain's Log**, and the old Ability Book, Haki Techniques and Gear mastery screens are gone into it:
  every key that opened them opens the log instead.
- **Overview**: your wanted poster (or service record), flag, title, fruit and every mastery at a glance.
- **Style & Haki**: your fighting style's moves and mastery, then Armament, Observation and Conqueror's Haki with every technique,
  its key and what it still needs. Pick a quick power and click a quick slot (or press its key) to put it there; Reset Slots puts
  the original layout back.
- **Devil Fruit**: a fruit's moves, mastery and quick slots, or the Nika fruit's techniques and its upgrades, with Buy and Respec.
- Scroll with the wheel, move with the arrow keys, Tab turns the pages. (`docs/images/one-menu.png`, `one-menu-small.png`)

## The Haki slash is an energy wave
- Drawn after the manga panel you sent: a wave of light tearing up out of the ground, a steep front to a sharp peak, its back torn
  into long tongues streaming up and back (they flicker), whiter streaks along it, an ink outline, a glow. It is thick: about 9
  blocks across where it leaves the ground at full charge, so from behind you see a ragged tower of light, from the side the wave.
- The trench it cuts is as wide as the wave's body at each height (wide in the ground, thin at the peak); the per-tick limits on
  blocks are the same, it just keeps cutting a little longer after it lands. (`docs/images/haki-slash-wave.png`)

## Everything Haki is your Ryou colour
- Every player now gets their Ryou colour the first time they log in (before, only when Ryou unlocked). It is still given once and
  kept for good.
- All Haki lightning wears it: the Conqueror's releases and their storm and shockwave, Armament's bolts, the crackle on your katana,
  the slash and its crackle. The black body of the lightning stays black; the rest is your colour. Pirate captains' lightning is
  pale steel.

## Not done yet (next)
- The JoyBoy release for everyone at full charge (no on/off), the Conqueror's release drawn like your frames (huge black bolts with a
  glowing rim in every direction from the player), and the self-repairing fissures where it strikes at full charge. These are next.

## Unchanged
The network protocol is the same as 2.2.1.
