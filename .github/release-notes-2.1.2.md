# HexPiece 2.1.2

Combat mode on R and Photon particles for the fighting styles. Nothing here has been tested in game yet.

## Combat mode (R), as in HexGhoul
- **Tap R** to go into combat mode, tap it again to leave. Your fighting style's moves (M1 string, right-click signature, Y and U
  specials) only come out in combat mode. A badge over the hotbar shows your style while it's on.
- **Ctrl + R** is now Armament Haki (R was Armament before). In Devil Fruit mode, with a fruit kit on, or before you have set sail,
  R stays the original's (Gear 5's transformation, Armament). Combat mode switches itself off when any of those starts.
- In combat mode M1 throws your style at blocks too (to mine bare-handed, leave combat mode). The right click still opens doors and
  chests.
- **Combat stances**: in combat mode you hold your style's stance (arms only, so walking stays normal), and everyone near sees it:
  - **Black Leg**: hands clasped behind the back.
  - **Six Powers**: left hand behind the back, right hand loose at the side.
  - **Fishman Karate**: right fist chambered at the hip, left palm open forward.
  - **Iron Fist**: a boxer's guard, fists at the chin.

  The stance sits under every other animation: moves and Haki play over it and ease back into it. It eases out while you hold an
  item, ride, swim, fly, sleep or have a fruit kit on, and comes back afterwards.
- Black Leg's hands (and Six Powers' back hand) are now properly clasped at the small of the back in every move, not hanging at the
  hips.

## Photon particles (no vanilla particles)
- **Air on every swing**: every blow thrown cuts the air, hit or miss. A crescent and streaks fly on along the blow, heavy blows add a
  ring of air and ground dust, and spins throw streaks all round. Blows that land burst in sparks.
- **Air Blade**: a crescent of cut air flies the length of the shot with ghost blades and a streak trail, then bursts where it ends.
- **Fishman Karate is water**: droplets off every blow, a splash and spray where it lands, water bullets that fly, drip and burst, the
  Shark Skin Counter's turning ring of water, and a wave for the ground punch.
- Also: Flame Leg burns while it spins, Shave and the Shoulder Charge kick up dust, Sky Walk leaves rings in the air, Iron Body throws
  off steel sparks.
- Respects your particle setting (All / Decreased / Minimal).

## Note for servers
The network protocol changed: 2.1.2 clients and servers only connect to each other (2.1.1 is refused at login).
