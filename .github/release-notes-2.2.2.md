# HexPiece 2.2.2

One abilities panel on the left, copied from HexGhoul, replaces every HUD. Nothing here has been tested in game yet: the panel was
rendered off-game and judged there, and the build's checks and tests pass.

## The abilities panel
- **One small card on the left, just above the chat**, in HexGhoul's style: a title in the mode's colour with what you are doing next to
  it, the Haki energy as a thin gauge under it, then one line per move with **your own keys** (as bound in Controls) and the move's
  name in its colour.
- **Combat mode**: your style's moves (the combo, the heavy, the two specials). The Swordsman shows the katana's state: sheathed, the
  attack key draws (slowly); drawn, it cuts. The held right click is the charged cut, or the Haki slash with Armament on, and Ctrl +
  left click is the quick draw.
- **Fruit mode**: the fruit's moves on the keys you bound them to. A move you haven't reached yet is dim, with the mastery it needs. A
  move that is recharging is dim too, with the seconds left, and its key cap's underline fills back up. The Gomu fruit shows Gear 5's
  moves. A fruit's own extra gauge is still drawn next to the panel.
- **Haki is always there**: Armament (Ctrl + R, lit when on, "+ Ryou"), Observation, and the Conqueror's powers on your quick slots.
- **R** (swap between your style and your fruit, or turn combat mode on) and the **Captain's Log** key are at the bottom.
- **Gone**: the original Haki HUD with its mastery bars at the bottom, the fruit HUD, the Gear 5 HUD and our old combat badge over the
  hotbar.
- The panel hides while any screen is open.

## Unchanged
The network protocol is the same as 2.2.1.
