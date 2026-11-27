# HexPiece 3.9.3

Your in-game list, fixed. **Not tested in game yet:** the build and every test suite pass, and every changed animation, model and
the world's map were rendered and checked.

## 1. The coats' textures
"The coat's textures are completely fucked." Found and fixed: past 3 blocks from the camera (always, in third person) the clothes
switched to simplified distance versions of their meshes, and simplifying them averaged their texture coordinates across the atlas's
separate patches, so every face showed random bits of the atlas (the noise in your picture). The clothes now always draw their whole
mesh (the distance versions only saved a fifth of the faces), at every distance.

## 2. The mace's right click: a crater
"The mace right click should keep the same animation however, instead of a blade slash/cut, it creates a crater (with haki on and
lightning from conqueror)."
- The same charge and release, but the blow comes down on the ground ahead: **a crater** torn out there (it grows back after 30
  seconds, as every crater does), wider and harder the longer you charged, **half again as big with Armament Haki on**; everyone in it
  is hurt, thrown up and out and knocked down, earth flies, the ground shakes.
- **With Ryou and the Conqueror's Haki, the Conqueror's lightning bursts out of the crater** (visual).
- No Haki slash any more for the mace. The Haki slam shares the Haki slash's cooldown; the plain slam the charged cut's.

## 3. The mace: no flicker, and anyone can use it
- "The mace's Texture is flickering repeatedly nonstop": the swords' sky glint (a white band reckoned from every vertex's normal) ran
  over the mace's black iron, whose faces are coarse, so it jumped on and off them as it moved. The mace has no glint now: its own
  texture carries its cold sheen.
- "you do not need to be a swords man to use the mace or pistol": **anyone holding the mace in the main hand swings it** (M1) and slams
  it (right click), whatever their fighting style, combat mode on or off (outside Devil Fruit mode). The pistol already fired for
  anyone.

## 4. The mace swings way slower
"Mace swings should be wayyy slower": every mace swing now takes **twice as long** (its animations written twice as long, and its
hits, its sounds and its clash timing on the same slower clock, so the blow still lands where the swing is).

## 5. The pistol's Haki balls through 10 blocks
"The pistol bullets with haki on are not going through any blocks": the balls did go on through, but inside the blocks, out of sight.
Now **every block a Haki ball goes through bursts and is torn out of its way**: it leaves a tunnel up to 10 blocks deep (the 11th
block stops it, as does bedrock), grown back after 30 seconds like a crater (nothing dropped; protected blocks left alone).

## 6. The pistol held properly
"The pistol isnt being held right", then "Gun handle goes where the green circle is", "Make the gun point fully down man" and "thats
too much now up a bit": standing, walking and running, **the grip sits in the fist** (the handle hooking out of the front of the hand),
and **the barrel hangs pointing at the floor, a little ahead** (22 degrees ahead of straight down, as you drew it). Before, the barrel
came straight out of the bottom of the fist like a pipe, the handle hidden in the arm. The spin, the shot and the reload start and end
in the new hold.

## 7. The islands
"The world generation I asked for I truly dont see it, I created a new world and I see no islands?" and "should be more apart, you
should need to travel to find islands, theres too many": the islands were 1 to 3 km across with 30% of the world land, so standing
on one it looked like any land. Now **only the island noise's peaks rise out of the sea: islands 300 to 1500 blocks across, 1.5 to 3 km
of deep sea between them** (about 14% land): you sail to find the next one. Every island keeps its long beach and its rivers running
to the sea. **A new world is needed** to see it.
