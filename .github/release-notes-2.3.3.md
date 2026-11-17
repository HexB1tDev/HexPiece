# HexPiece 2.3.3

Fixes for the bugs you found in 2.3.2. Nothing here has been tested in game yet. The build's checks and tests pass, and the Ryou
hands were rendered and checked.

## Ryou's hands
- **A black hand with a coloured outline.** Each hand used to show as a solid box, black or in your colour. The outline was a
  shell drawn bigger than the hand, which only works where the game culls back faces, and on your setup it doesn't. Now the hand
  is coated black and a bright band in your Ryou colour runs along every edge of it, the inner side too, fading up the wrist. It
  reads the same whether your setup culls back faces or not.

## Conqueror's Haki
- **The particles are your Ryou colour.** The release, the charge, the tap and the last stand used the original red. Each particle
  now takes your Ryou colour and keeps its own brightness: dark embers stay dark, white sparks stay white. With no Ryou colour set,
  they stay red.
- **Tapping G uses the new lightning.** The tap release drew the old flat lightning. It now uses the same 3D lightning as the full
  release. So do King's Grip, Thragg's moves and the armament ground storm, which drew theirs the same old way.

## Swordsman
- **No katana, normal punches.** A Swordsman with no katana in the inventory now punches and uses items as normal. The sword's own
  M1s are only for when you carry one.
- **First person is the game's own.** The left arm no longer shows, and the right hand is the game's own, attached, holding the
  katana when it's in your hand. The swing shows on every cut.
- **The Haki slash cuts a narrow trench that heals.** The trench is at most 2 blocks wide, nothing drops, and every block grows
  back 15 to 25 seconds later. Chests, furnaces and other blocks that hold items are left alone, as are claimed and protected
  blocks, and paintings and item frames keep their walls.

## Not fixed yet (more detail needed)
- **Your skin.** Nothing in HexPiece changes a player's skin. The one hook in the original mod that touches it only works during
  Gear. The skin in your screenshots looks like one of Minecraft's default skins, which is what the game shows when your own skin
  can't be fetched.
- **The white screen with Fishman Karate.** I haven't found it yet.

## Unchanged
The network protocol is the same as 2.2.1.
