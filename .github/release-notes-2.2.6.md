# HexPiece 2.2.6

Your notes on the lightning, the slash, the saya and the icons. Nothing here has been tested in game yet; the build's checks and tests
pass, and every change was rendered from the game's own drawing code and judged (`docs/images/haki-lightning-3d.png`,
`docs/images/conqueror-release-3d.png`, `docs/images/haki-slash-energy.png`, GIF `docs/images/haki-slash-flow.gif`).

## Haki lightning: 3D, outlined, all of it
- Every stroke is a real tube now, not a flat strip turned to the camera: lit by a light from above-left of the screen, with a shadow
  side, a lit side and a hard highlight. The coloured cores are white-hot down their middle. The black bodies (the original's black
  lightning, the storm's huge bolts) are glossy black, edge-lit in your Ryou colour.
- Every stroke has a black ink outline: cores, black bodies, the little spurs, the katana's crackle, the slash's crackle, and the
  full-power storm (its coloured rim now sits between the black bolt and an ink line).
- No more square ends: every free end narrows to a needle point, and a strike racing out has a spear point at its front. The
  outline wraps round the tip.

## Haki doesn't come out of your body
- The Conqueror's bolts now tear out of a shell round you (about two blocks out, up over your head, ringing your feet on the ground)
  and run outward, like your drawing, never out of your chest. The server's stun paths follow the same bolts.
- Any other Haki lightning that played on your body now crawls over that shell instead.

## The Haki slash: flowing energy, not a solid
- Same shape (the torn wave with its tongues), but it's light now: a see-through skin in your colour, denser at its edges, a white-hot
  core inside, bands of light pouring back from the cutting edge into the tongues, white streaks racing back along its flanks, a
  white-hot cutting edge, and flickering see-through tongues. The world shows through it. Its Haki crackle stays inked.

## The saya never turns black
- With Armament on, only the sword goes black, never its saya (first person: the original blackened everything you hold).

## Fixed
- Every katana's inventory icon showed as a missing texture: the icons were in a folder Minecraft 1.20 never stitches into the item
  atlas. They're under `textures/item/katana` now, and the build refuses an icon anywhere else.

## Coming next (asked, not in this version)
- Gryphon (a saber at the hip) and Yoru (roped to your back, its own draw, never black with Haki), the Galaxy Fist as Iron Fist's
  Ryou move, Six Powers' faster 3D air and Haki finger pistol shots, and the Fishman Karate rework (Brick Fist, Water Bullet, Water
  Prison).

## Unchanged
The network protocol is the same as 2.2.1.
