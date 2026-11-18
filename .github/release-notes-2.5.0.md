# HexPiece 2.5.0

Evil Conqueror damage: half of what's left, never a kill. Nothing here has been tested in game yet; the build's checks pass.
Everything from 2.4.9 is in here too (the release by alignment, the stun that grows while you hold G at full, the 1-block sword slash,
the stunned mob's circle in your Ryou colour).

## Evil release damage
- **An evil release now takes half of whatever health each body it hits has left.** That covers everything it stuns in its radius
  and everything its lightning strikes, mobs and players alike, on a full or a partial charge.
- **It never kills.** A body at half a heart or less takes no damage at all, and each release only ever takes half of what is left.
- **It is always exactly half,** whatever armour the body wears: if armour soaks some of the hit, the health is still set to half.
- A creative-mode player, or anything the game is already keeping from harm, is not hurt.
- **Good** still deals no damage. **Neutral** still deals the original's damage.

## Please test
- Be evil (`/hexpiece alignment -100`) and release on a few mobs and a friend: every one you hit should drop to half its health and
  stay alive, and anything already at half a heart should take nothing.
- And the 2.4.9 list: the alignments' stuns, the hold at full, the partial charge's 2-second stun, the circle's colour, the slash.
