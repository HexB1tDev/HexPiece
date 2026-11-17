# HexPiece: handoff after 2.2.7

Read CLAUDE.md first. This note is the newest; next-ai-2.2.6.md covers 2.2.6 (the 3D inked lightning, needle tips, lightning off the
body, the slash as light, the saya never black, the icon fix), next-ai-2.2.5.md and older the rest. The next version is 2.2.8. The
work branch is `claude/nice-bell-gam7qf`. Nothing has ever been tested in game (only the owner can).

## The owner, this session (after 2.2.6, quoted)
- With `input/Yoru_Gryphon_Models_Textures_1.20.1.zip`: "Add these 2 blades to the game in the file I sent, Gryphon sits where katanas
  are but it's a saber, Yoru is big so it's on the back of the player, it's roughly the size of an entire player (make it tied with a
  rope to the player's back and it also needs a pull out animation, these are the 2 strongest blades in the game, YORU is the strongest,
  Yoru never goes black it gets the lightning around it but never turns black with haki on." A picture of Mihawk wearing Yoru.
- A zip of references (not in the repo: uploaded to the session that made 2.2.7): `SwordBeam2.gif` (Mihawk's slash in Marineford: a
  huge mint wave filled with foam cells, lighter blobs ringed in darker teal, darker outlines on its torn edges), `Unsheath.gif` (Mihawk
  reaching up behind his head to Yoru's hilt over his right shoulder, one sweep, then holding it low at the hip angled up across the
  front), `SwordBeam.url` (https://media1.tenor.com/m/vf5isZQBOX0AAAAd/jozu-mihawk.gif).
- "Yoru swings and is held the same way as the katana, same for the saber, difference is, Yoru has more range and is pulled out whenever
  the user activates the combat style and is put back only when deactivating combat mode/switching to fruit, same for gryphon".

## Released in 2.2.7
- **Meshes** (`tools/katana_mesh.py` LEGENDS): the zip's OBJs are Forge item models in blocks; each is taken into the katanas' frame
  (model px, guard at y 0, blade +y, spine/curve +x, edge -x), scaled (Gryphon 1.78 blocks, Yoru 2.75: the owner, of a first one as
  tall as a player, "Yoru IS way bigger than that"), Gryphon mirrored (its curve
  ran the other way) with its faces rewound, its separate scabbard moved so the throat meets the bell guard; blade parts renamed
  `01_Blade_*` (highlight and LODs). Yoru's origin sits so the fist (GRIP -2.4) is clear of the cross guard. `build.py legend_assets`
  packs them (LODs, atlases 1024^2, item models, checksum `EXPECTED_LEGENDS_SHA256`, cache `build/legend-cache`). Icons:
  `tools/katana_render.py legend-icons` (Yoru's gloss at 0.45: a black blade only glints).
- **`sword/Katana`**: `GRYPHON`, `YORU` at the end (ordinals 25, 26). New fields: `carry()` (HIP / BACK), `edge()` (Gryphon 1.55,
  Yoru 1.75; katanas their grade's), `reach()` (blocks added to a cut's sweep and the quick draw: Gryphon 0.25, Yoru 1.1),
  `blackens()` (false for Yoru), `legend()`, `kindLine()` (tooltip). NPCs never pick a legend (`SeaRules.katana`); the creative tab
  lists them last. `SwordServer.edge/reach`.
- **Always drawn in a fight** (`SwordServer.legend`, from `tick`): ready (Swordsman, combat mode, free hand) and not drawn -> the full
  draw starts; never sheathed for idling; not ready while drawn (combat mode off, the fruit taken up, an item in the hand) -> every
  action dropped and the full sheathe plays to its end out of combat (`State.stowing`), then the reset. Back in the fight mid-sheathe:
  it stays in the hand.
- **Yoru on the back** (`sword/BackCarry`, pure, tested): `GUARD` (-4.0, -1.0, 5.6) body px, `TILT` 33 deg, `LEAN` 8 deg (found by
  search, tools/yoru_fit.py: over tilt, lean and position, the mesh's vertices kept out of the head, body, arms and legs
  boxes, the lowest point above the ground, the grip within 8 px of the right shoulder, the pommel above the head; the least tilt, then
  the least gap: upright, 2.75 blocks go through the ground or the head and the grip is out of reach): hilt above the head over the
  right shoulder, blade across the back past the left leg, standing ~2.6 px off the back; two ropes (`baldric`, `waist`: box-hugging
  loops smoothed by Chaikin, tented over the blade where they cross it, from its own depth there: `fromBlade`), six-sided hemp tubes with a three-strand helix.
  `SwordLayer.back` draws the ropes and the sheathed Yoru on the body part (its bend kept), the drawn one in the hand (gloss 0.45, never
  black). `KatanaRenderer.ropes`, `mesh` returns no saya for BACK. `StanceLayer`: `style_swordsman_back_stance` (arms loose) for Yoru.
- **Yoru's animations** (`tools/sword_anims.py yoru()`, solved on the rig; `sword_rig.back`, `back_grip`, `back_ropes`): `sword_draw_back`
  (22 ticks, in the hand at 8: reach up behind the head, lift 2.4 px out of the ropes (more is out of reach), back over the shoulder, straight up, forward, low at the
  right hip angled up across the front, guard), `sword_sheathe_back` (20, home at 14), `sword_charge_draw_back` (26, in the hand at 3,
  loops from 6), `style_swordsman_back_stance`. `SwordAnims.name(anim, katana)` / `drawn(anim, ticks, katana)` map DRAW, SHEATHE,
  CHARGE_DRAW to them for BACK (server and client); every other animation is the katana's (the quick draw and release from the back snap
  into the hand at their draw tick). Every existing animation re-solved byte-identical. Clearance: two "fist in front of the face"
  half-ticks left (as the katana's own draws have).
- Renders: `docs/images/yoru-draw.png`, `yoru-sheathe-charge.png`, `gryphon-hip.png`, `legend-icons.png`.

## Open: the owner's requests, in order
1. **The slash after the Marineford reference** (the owner's `SwordBeam2.gif`): add foam cells (lighter blobs ringed in a darker shade of
   the colour, drifting back along the flow and up into the tongues) and darker cel outlines on the torn edges (the skin's rim a
   darker, denser shade); the Ryou colour stays.
2. **Galaxy Fist becomes Iron Fist's** (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU
   haki. ONLY for iron fist."): see next-ai-2.2.6.md item 2.
3. **Fishman Karate rework**: see next-ai-2.2.6.md item 3 (Brick Fist, Water Bullet, Water Prison, water condensing out of the air).
4. **Six Powers**: faster 3D air, Haki finger pistol shots (next-ai-2.2.6.md item 4).

## Not verified (only the owner can test in game)
- Everything of 2.2.7 in game: the blades' size and placement, the ropes on other skins (slim arms, 3D skin layers), Yoru's draw and
  sheathe under PlayerAnimator, the hand-over at the draw tick (solved within ~0.7 px), the auto draw/stow on combat mode, the reach.
- From 2.2.6: the 3D lightning, the shell, the slash as light, the saya in first person, the icons.
