# HexPiece: handoff after 2.3.1 (the owner's One Piece armor)

Read CLAUDE.md first. This note is the newest; next-ai-2.3.0.md covers 2.3.0 (the crash on boot every build from 2.2.2 to 2.2.9 had,
the new PieceVerify check on event handler names), the slash-and-finisher showcase (`tools/preview/SlashShowcase.java`) and the
owner's requests in their words; next-ai-2.2.9.md the sword sounds. The work branch is `claude/nice-bell-gam7qf`; the next version is
**2.3.2**. Nothing has ever been tested in game (only the owner can); 2.3.0 is the first build since 2.2.1 that can boot (unconfirmed).

## The owner (quoted)
- With `input/OnePiece_Armor_1.20.1.zip`: "New armor for NPCS and for the player (create a custom tab in creative as well), NPCS for
  now only spawn with the Marine Armor (not admiral or strawhat or black coat)." Done in 2.3.1.
- Earlier, still open: "the Marines and Pirate skins are shit and need to be recreated and bettered, you CAN take skins from HexGhoul";
  the clashes (every move clashable, an impact frame on each, Armament needed against a blade, Ryou meeting Ryou bursts both colours):
  next-ai-2.3.0.md.

## Released in 2.3.1
- **The pack, never altered** (`input/OnePiece_Armor_1.20.1.zip`, checksum in build.py `EXPECTED_ARMOR_SHA256`): Blockbench models
  (steve/alex), a Forge `OnePieceArmorModel` (generated geometry: MeshDefinition/CubeListBuilder, 76 to 303 cubes a design, 256 logical
  UV on 1024 px atlases) and `OnePieceArmorExtensions` (an `IClientItemExtensions` whose `getGenericArmorModel` copies the player's pose
  and per-slot visibility; slim fit for slim players), textures under `assets/hexpiece/`, INTEGRATION.md.
- **build.py translates it** (`armor_sources`, `ARMOR_SRG`): the two classes from the zip into `build/generated/com/hexhaki/piece/
  client/armor/` with Mojang names turned into SRG (outside string literals: part names stay), compiled with the expansion;
  `armor_assets` packs the four atlases at `assets/hexhaki/textures/models/armor/`. To change the translation, edit ARMOR_SRG; never
  copy the generated files into src.
- **Items** (`armor/PieceArmor`, pure: key, display, slot, the pack's style and texture, `marineIssue`; `armor/PieceArmorItem extends
  ArmorItem`, `ArmorMaterials.IRON` as HexGhoul's outfits): `hexhaki:marine_cap`, `marine_shirt`, `marine_trousers`, `marine_boots`,
  `admiral_coat`, `straw_hat`, `blackbeard_coat`. `getArmorTexture` returns the pack's atlas. `initializeClient` (DistExecutor, the
  client class only inside the lambda) hands `client/armor/ArmorClient`, which makes the pack's extension on first draw: Forge calls
  initializeClient from Item's constructor, before the item's own fields are set.
- **Creative tab** "One Piece Armor" (`PieceItems.ARMOR_TAB`, id `hexhaki:armor`, the straw hat for icon). Names in en_us.json, item
  models `models/item/<key>.json`, 32 px icons `textures/item/armor/<key>.png` rendered from the pack's own geometry
  (`tools/preview/ArmorPreview.java icons`).
- **NPCs**: `SeaFighter.setup` puts a Marine in the Marine outfit, head to feet, when its chest slot is empty (a loaded Marine keeps what
  it wears); pirates wear nothing; vanilla's drop chance. `SeaFighterRenderer` now has a `HumanoidArmorLayer` (inner/outer player armor
  layers, the context's ModelManager): Forge's armor-model hook then draws the pack's model.
- **Proof**: the builders are the original jar's (HakiChestLayer); new evidence in verify/api-allowlist.txt (2.3.1 block: PartPose
  offsetAndRotation, HumanoidModel(ModelPart), setAllVisible, the arm poses, riding, Model, HumanoidArmorLayer and its constructor,
  ModelManager, getModelManager, the player armor layers), each from joined.tsrg with intermediary and yarn 1.20.1 (raw GitHub).
- **Preview** (`tools/preview/ArmorPreview.java`): the pack's original Mojang-named source compiled against stand-ins
  (`tools/preview/armorfake`: the same builder calls, baking the same tree), drawn as HumanoidArmorLayer draws it (per slot, cutout,
  no cull), vanilla's cube UV layout and part transforms, on the owner's skin (slim) and a wide mannequin: `docs/images/
  one-piece-armor.png`.
- **Tests** (`ArmorTests`): the pieces match the pack's INTEGRATION.md table and rig_manifest.json (slot, style, texture), build.py's
  ARMOR_KEYS, only the Marine outfit is NPC issue (head to feet), each item's model, 32 px icon and name, no Mojang name left in the
  generated classes.

## Open: the owner's requests, in order
1. **Better Pirate and Marine skins** (HexGhoul's may be taken: `src/main/resources/assets/hexkagune/textures/entity/` human/, ccg/,
   ghoul/...). The Marines now wear the outfit over their skins: the skins matter most for faces, hair and the pirates.
2. **Every move is clashable, with an impact frame on each clash**: next-ai-2.3.0.md item 1 (with the owner's two rules).
3. **Galaxy Fist becomes Iron Fist's**, 4. **Fishman Karate rework**, 5. **Six Powers**: next-ai-2.2.6.md items 2 to 4.

## Not verified (only the owner can test in game)
- That the game boots (2.3.0's fix); the armor in game: the fit on real skins, the coats' tails while running and crouching, a female
  Marine's bust layer under the shirt (SeaFighterRenderer.Bust is drawn before the armor layer), the icons in the inventory.
