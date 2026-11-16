# Next AI: after 2.1.3 (Ryou, R between style and fruit, natural Fishman/Iron Fist arms); Swordsman is next (2.1.4)

Read `CLAUDE.md`, then this, then `docs/handoff/next-ai-2.1.2.md` and `next-ai-2.1.1.md`. Released: v2.1.1, v2.1.2, v2.1.3 (branch
`claude/friendly-allen-g9nrzx`). Answer the owner in English; never claim in-game testing: nothing here has run in game.

## The owner's request (2.1.3 + 2.1.4), quoted in parts
The full request is long; its parts, in the owner's words where it matters:
1. "Fix the Fishman and Iron Fist stances so the player's arms remain naturally lowered when idle, walking or sprinting. Selecting a
   combat style must not permanently activate an attacking pose. Raise the arms only during an attack's anticipation, strike and
   recovery ... Preserve natural leg movement during attacks that allow movement. Interrupted attacks, switching styles, item use,
   swimming, climbing, mounting and death must release their animation overrides correctly" -> DONE in 2.1.3.
2. "Reduce the excessive muscle size produced by Advanced Haki Armament. Adapt the smaller muscle shapes and proportions from
   HexGhoul" -> DONE (the original's HakiPhysiqueLayer/HakiChestLayer constants, PiecePatch FLOATS). HexGhoul has NO player muscle
   assets (its muscle is kagune/flesh meshes: Graft, Owl, Peace): told the owner; ask which assets they meant.
3. "Rename Advanced Haki Armament to Ryou everywhere visible" -> DONE (PiecePatch TEXTS, lang). "Flowing Armament / Ryuo" was renamed
   "Flowing Armament" (two near-identical names otherwise): tell the owner.
4. Mid-request: "I think its best whenever Ryou is unlcked it fully replaces regular haki, and we empty out the J key for now." -> DONE
   (RyouServer.afterArmament at the end of toggleArmament; toggleAcoc is a trampoline to RyouServer.acocKey, empty).
5. "When a player first unlocks Ryou, assign them one random, clearly visible glow colour on the server and save it permanently ...
   Every Ryou effect belonging to that player must read this same saved colour." -> DONE for the hands, the activation, the ambient
   (RyouServer, S2CRyou, client/haki/RyouColors). NOT DONE: the original's "advanced" technique impacts (Galaxy fist, Thragg, King's
   Grip advanced variants in HakiVfx) keep their own red/black colours; recolouring them means patching many colour constants.
6. "Ryou should look like blackened hands with a strong, clean glow outlining their shape ... Remove the fire particles from every
   Ryou activation, idle and movement effect" -> DONE (client/haki/RyouHands: inverted hull, dark band + additive rim + 3 halo hulls,
   bendy-lib bend via haki/ArmBend; RyouFx; previews docs/images/ryou-hands.png, tools/ryou_preview.py).
7. Mid-request: "tapping R switches from Combat style and fruit (if any is eaten)." -> DONE (client/CombatMode; Gear 5 moved to
   Ctrl + R in Devil Fruit mode; kit fruit M1 via PieceHooks.fruitKeysActive).
8. "Pirate NPCs must actually use the applicable Fishman, Iron Fist and Swordsman combat styles through their combat AI ...
   Marines are excluded" -> NOT DONE (2.1.4). Check first what npc/SeaFighter's brain already does (2.1.1 notes say NPCs already fight
   with the same styles and moves through StyleCombat): find out what the owner saw ("rather than merely displaying their poses").
9. Swordsman, the 25 katanas, M1 combo (12+ distinct motions), M2 charged slash, the Haki slash and its terrain cut, the sheath at the
   hip, draw/sheath, tiers and rarities and descriptions -> NOT DONE (2.1.4). See the plan below.

## Swordsman plan (2.1.4): what was inspected, what to build
- The owner's zip (`Hex_Katana_Atelier_1.20.1.zip`, uploaded in the session, NOT in the repo): `models/obj/<name>.obj` (25 katanas,
  ~14.5k faces each, mostly quads, named groups: 01_Blade_forged_shinogi, 02_Habaki, 04_Tsuba_*, 11_Samegawa, 13_Tsukamaki,
  15_Kashira ...; units: 1 = a model pixel, the blade from y 0.5 to ~21, the handle down to y -6.6), `<name>_sheath.obj` (25 saya,
  ~15k faces: 21_Saya_hollow_curved_lacquer, 22_Koiguchi, 24_Kojiri, 32_Sageo ...), `textures/<name>.png` (one 512x512 atlas each;
  the poster says 1024px: it is 512). No "sheathed assembly" models (the poster's third set): compose blade + sheath.
  The MTL files point at ../../textures/: the Forge OBJ loader cannot read that; write our own.
- Names (from the owner's poster): Crimson Dawn (Choji), Silver Crane (Suguha), Obsidian Eclipse (Hitatsura), Azure Tide (Notare),
  Jade Serpent (Saka Choji), Sakura Veil (Gunome), Amber Wasp (Togari), Frostglass (Icebreak), Royal Amethyst (Toran), Copper Ronin
  (Midare), Cobalt Storm (Inazuma), Lotus Bloom (Ko Gunome), Blood Moon (Choji Midare), Pearl Heron (Hososuguha), Sunfire (Flame),
  Blackwater (Billow), Emerald Thorn (Thorn), Rosegold Whisper (Sugu Notare), Ashen Wolf (Sambon Sugi), Starfall (Starlight), Desert
  Mirage (Sudare), Venom Orchid (Poison Bloom), Dragonbone (Fossil), Imperial Phoenix (Phoenix), Midnight Lotus (Moonwater). The
  word under each name is its hamon (temper line pattern).
- Rendering idea (not built): bake each OBJ at build time into a compact binary mesh (positions, UVs, normals, per-group ids), draw it
  with our own VertexConsumer code (as HexGhoul draws its meshes) through Item.initializeClient's custom renderer (HexGhoul's allowlist
  proves BlockEntityWithoutLevelRenderer and initializeClient), a matcap-style highlight on the blade group computed per frame from the
  view-space normal so it shines as it moves (a second additive pass), a 2D icon rendered off-game for the GUI. The hip sheath is a
  RenderLayer on the player (body transform), the drawn blade attached to the right arm (haki/ArmBend gives the bent hand).
- The original's Haki Blade Cut (ARM_BLADE_CUT, HakiAction.HAKI_BLADE_SLASH) fires on vanilla SwordItem swings with Armament on: a
  katana must not be a vanilla SwordItem (or that path must be patched), or every swing launches the original's crescent.
- Verify needs evidence lines for every new Minecraft reference (Item registration: HexGhoul's list has Item, Item.Properties,
  SwordItem, Tier, CreativeModeTab, ItemRenderer.renderStatic, BEWLR, initializeClient).

## What 2.1.3 changed (where)
- `patch/PiecePatch.java`: CALL_REDIRECTS, WIDENED, TRAMPOLINES, AFTER_ARMAMENT, FLOATS, FLOAT_FIELDS, TEXTS (each with its count; the
  build fails if the original's bytecode is not as expected). `build.py` PATCHED lists every original class changed.
- `server/RyouServer` (colour, Armament brings Ryou, J empty), `network/S2CRyou` (message 7, protocol "3"), `haki/RyouPalette`,
  `haki/ArmBend` (pure, tested: RyouTests), `client/haki/RyouColors|RyouHands|RyouFx`, `client/PieceHooks`, `client/CombatMode`.
- `tools/style_anims.py`: EASE_IN / FREE_LEGS sets; Fishman/Iron Fist stances deleted; `server/StyleServer.interrupted/interrupt`;
  NPC blending in `client/npc/StylePoser` (weights) and `SeaFighterRenderer`.
- `tools/anim.py` draws Minecraft boxes (since 2.1.2's session), `tools/ryou_preview.py`.

## Ask the owner
- Which HexGhoul muscle assets did they mean (none found for players)?
- Is renaming "Flowing Armament / Ryuo" to "Flowing Armament" right?
- Should the original's Ryou-tier technique impacts (Galaxy fist, Thragg, King's Grip) also take the player's colour?
- Gear 5 on Ctrl + R in Devil Fruit mode: right?
