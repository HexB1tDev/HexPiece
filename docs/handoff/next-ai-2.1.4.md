# Next AI: 2.1.4 (Swordsman, the 25 katanas, Swordsman pirates) is IN PROGRESS; the owner said "handoff + prompt"

Read `CLAUDE.md`, then this, then `next-ai-2.1.3.md` (what 2.1.3 did and where), `next-ai-2.1.2.md`, `next-ai-2.1.1.md`. Released:
v2.1.1, v2.1.2, v2.1.3 (branch `claude/friendly-allen-g9nrzx`, every CI run green). `build.py` VERSION is still 2.1.3: bump to 2.1.4
(`tools/next_version.py`) when 2.1.4 is ready, with `.github/release-notes-2.1.4.md`. Answer the owner in English; never claim
in-game testing.

## The owner's request still open (quoted; the parts done in 2.1.3 are in next-ai-2.1.3.md)
Swordsman and the katanas:
- "Pirate NPCs must actually use the applicable Fishman, Iron Fist and Swordsman combat styles through their combat AI, rather than
  merely displaying their poses. Respect existing eligibility, equipment, progression and spawn rules. Their attacks must use the same
  animation timing, reach, recovery and hit rules as the corresponding player attacks. Swordsman pirates need a valid sword and matching
  sheath. Marines are excluded from this update; preserve their existing combat behaviour." (Note: npc/SeaFighter ALREADY chooses and
  lands real Black Leg / Iron Fist / Fishman Karate moves through StyleCombat, SeaRules.styles gives pirates those three. Ask the owner
  what they saw in game.)
- "Add Swordsman as a complete combat style integrated into the existing selection, progression and input systems. Use my supplied
  katana collection and each katana's matching sheath. Preserve their individual blade shapes, colours, guards, handle details and
  temper patterns. Improve the finish with crisp edges, convincing metal shading and controlled highlights that make the blades shine as
  they move. Preserve the detailed geometry rather than reducing the models to flat item sprites."
- Choreography: "use One Piece's projected sword cuts and metal-cutting swordsmanship as inspiration for the Haki slash. Use Rurouni
  Kenshin's rapid draw attacks as inspiration for the transition from stillness into a decisive cut. Ground the handling in iaido's
  draw-and-cut, follow-up cut and resheathing sequence. These are references for an original combat style, not instructions to copy
  named techniques or introduce additional swords, powers or elemental attacks."
- "Make Swordsman M1 attacks the most elaborate and polished melee animations in the mod. Implement at least twelve genuinely distinct
  attack motions across connected combo sequences and contextual variations. Include a fast draw cut, opposing diagonal cuts,
  horizontal cuts in both directions, a rising cut, a controlled thrust, a two-handed overhead cut, a pivoting cut, an advancing cut and
  aerial variations. Give the movements different silhouettes, timing and follow-through; rotating the same swing or changing its
  particles does not count as a new animation."
- "Connect these motions into a flowing grounded combo with movement and aerial branches using the existing movement inputs. Choose
  branches predictably from the player's movement state, and keep combo progression server-authoritative. Clearly define the
  continuation window and reset the sequence after it expires. Use coordinated shoulders, elbows, wrists, torso rotation, hips and foot
  placement. Hands must maintain believable contact with the handle, including during two-handed attacks. Do not make the sword spin
  independently of the grip or force the player into uncontrolled movement."
- "Make the choreography flashy through blade paths, timing, poses and transitions. Use restrained blade trails, brief edge glints and
  sharply timed impact effects ... Trails must follow the actual blade and fade quickly ... Ordinary sword attacks should not emit fire
  or unrelated elemental effects. Preserve player camera control, and make first-person attacks visibly match the direction, rhythm and
  impact timing of the third-person animation."
- "Each M1 press should request one attack, with a small input buffer for the next legal combo step. Do not let repeated packets bypass
  recovery or generate unlimited queued attacks. Associate damage with defined strike windows and swept blade volumes so fast swings
  cannot pass through targets between ticks. Prevent duplicate damage from overlapping samples. Any attack with multiple damaging cuts
  must have explicitly defined separate hit windows. Ordinary M1 attacks must not inherit the charged Haki attack's terrain destruction."
- M2: "a chargeable sword slash. Pressing and holding right-click draws the sword if necessary and enters a readable charging animation;
  releasing performs the attack. Use a configurable maximum charge duration, defaulting to 40 ticks. Clamp charge at its maximum ... The
  server must calculate charge from validated start and release actions using server time. Define one release per charge, and cancel
  cleanly if the player dies, loses the sword, changes styles or enters an incompatible action. Do not fire automatically merely because
  maximum charge has been reached."
- "Without Haki active, M2 releases a powerful physical sword cut with local melee reach. With Haki active, it releases a travelling
  slash projectile. Longer charging must produce a visibly larger slash, with matching increases in its actual collision and
  terrain-cutting dimensions. Keep minimum and maximum size, travel distance, damage, resource costs and cooldowns configurable ...
  Capture the validated aim direction and Haki state at release, and align the animation, projectile, damage and terrain cut with that
  direction."
- "The Haki projectile must use the player's saved Ryou colour, including its outline and trail. Do not hardcode it to red ... this
  attack must not grant advanced Haki before it is unlocked. If ... basic Armament users [can] launch the slash before unlocking Ryou,
  use a neutral visual fallback until their personal Ryou colour exists. Do not assign their permanent Ryou colour early." (Use
  client/haki/RyouColors.orNeutral and haki/RyouPalette.NEUTRAL; the colour is only ever given by server/RyouServer.ensure.)
- "The projectile must create a narrow vertical cutting plane along its travel path, rather than an explosion or a large rectangular
  excavation. Charging increases the height of this plane and the visible slash together. It can cut block types regardless of their
  normal hardness or required mining tool, including obsidian, with bedrock remaining immune. Bedrock must stop the cut locally so blocks
  directly behind it are not removed through it. Make the attack's visible shape, entity hit volume and removed blocks agree."
- "Perform terrain destruction on the server, respecting world boundaries, loaded chunks, block-break cancellation and existing
  protection rules. Handle block entities and drops correctly without duplication. Pirate terrain destruction must also respect the
  server's mob-griefing settings. Bound both block checks and block changes per tick, deduplicate affected positions and impose finite
  projectile range and lifetime ... Ordinary melee attacks and non-Haki M2 attacks must not perform this terrain cut." (HexGhoul's
  server/BlockSmash shows the proven pattern: MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, owner)),
  level.m_46805_ loaded, state.m_60800_ destroy speed (< 0: unbreakable, stops the cut).)
- "Whenever an eligible sword is anywhere in the player's normal carried inventory, show it sheathed at their side, even when its hotbar
  slot is not selected. Items inside nested containers or an Ender Chest do not count ... If several swords are available, use one
  active sword consistently: prioritise an explicitly equipped sword, otherwise retain the most recently used sword while it remains
  available, otherwise choose the first eligible inventory slot ... Preserve the active sword's identity through ordinary inventory
  rearrangement. (The sword is never usable, unsheatheable or of any use until combat mode is enabled, ONLY players with the swordsman
  combat can unsheath it)."
- "Attach the matching sheath to the player's hip so it follows the body naturally during walking, sprinting, crouching and attacks.
  Mirror the arrangement appropriately for handedness. While sheathed, the blade must be inside its own sheath with the handle visible.
  While drawn, the empty sheath stays attached to the hip and the same sword appears in the attacking hand. Drawing is a visual and
  combat state change; it must not create another item or silently replace inventory stacks."
- "Draw the sword only for sword-based M1, M2 or abilities that require it ... From the sheathed state, the first sword attack must flow
  directly into a draw cut without consuming the input on a separate draw animation. Keep the weapon drawn between connected combo
  attacks and throughout charging or sword abilities."
- "When the attack sequence finishes and no compatible input is buffered, perform a fast, clearly visible sheathing animation and
  return to relaxed movement. Use a short configurable inactivity delay ... an eight-tick delay followed by a four-to-six-tick sheathing
  animation. A new valid sword attack during sheathing must transition smoothly back into combat without duplicating the blade, losing
  the input or resetting into an unrelated pose. Align the blade with the sheath opening rather than making it disappear beside the
  player."
- "Coordinate custom rendering with the normal held-item renderer so the sword is never displayed twice ... Right-clicking containers,
  using an item or performing another higher-priority interaction must not accidentally start charging. Validate the selected sword,
  reach, charge, cooldowns and resources on the server. Synchronise the active sword, combo step, draw state, charge state and Ryou
  colour so remote observers see the same actions."
- "Finally, give the sword beautiful description, tiers, and rarities, the names are in the image." (DONE in sword/Katana, sword/Grade.)
- "Verify ... Report what was implemented, what was actually tested and any missing source assets or remaining problems without
  presenting unverified behaviour as complete."

## Done so far for 2.1.4 (committed, built green, NOT wired into the game yet: nothing registers or draws them)
- `input/Hex_Katana_Atelier_1.20.1.zip`: the owner's zip, unaltered (sha256 fece809e...6d28). 25 katanas `models/obj/<key>.obj`
  (~14.5k faces, mostly quads, 30+ named parts: 01_Blade_forged_shinogi, 02_Habaki, 04_Tsuba_*, 11_Samegawa, 13_Tsukamaki,
  15_Kashira, 17_Mekugi ...), 25 saya `<key>_sheath.obj` (~15k faces: 21_Saya_hollow_curved_lacquer, 22_Koiguchi, 24_Kojiri,
  32_Sageo ...), 25 atlases `textures/<key>.png` (512x512; the poster says 1024: it is 512). No "sheathed assembly" models (the poster's
  third set): draw blade + saya together, they share one frame (the blade sits inside its saya).
- Facts measured: model units are pixels; guard at y 0, blade up +y to y ~20-21, handle down to y ~-6.6; the EDGE is on -x (the
  cross-section thins there), the back (mune) +x, the tip curves toward +x; blade ~1.3 px wide, ~0.18 px thick. OBJ v is flipped
  (1 - v) for Minecraft (checked: textures land right). Drawn at KatanaRenderer.SCALE = 0.62/16 (the whole katana ~1.07 blocks).
- `tools/katana_mesh.py` (stdlib only: CI installs no Python packages): OBJ -> packed gzip mesh ("HKM1", format in its docstring),
  6.5 s for all 50, 11 MB. NOT yet called by build.py: add a step that packs every model from the zip into
  `assets/hexhaki/katana/<name>.hkm` and copies the atlases to `assets/hexhaki/textures/katana/<key>.png` (cache by the zip's hash), and
  add those prefixes to build.py NEW_PREFIXES.
- `tools/katana_render.py` (numpy + PIL, off-game): `sheet out.png` (all 25 blades and saya, judged: textures right), `icons <dir>`
  (32x32 GUI icons: blade on the diagonal, widened 1.7x so it reads, a 1-px dark outline; judged). Icons committed in
  `assets/hexhaki/textures/piece/katana/<key>_icon.png`. `tools/katana_names.py`: the 25 keys/names/hamon in poster order.
- `sword/Grade` (pure): Wazamono (common), Ryo Wazamono (uncommon), O Wazamono (rare), Saijo O Wazamono (epic), edge x1.0..1.36.
  `sword/Katana` (pure): the 25 with name, hamon, grade, a description each (Saijo: Obsidian Eclipse, Dragonbone, Imperial Phoenix;
  O: Royal Amethyst, Cobalt Storm, Blood Moon, Sunfire, Starfall, Midnight Lotus; Ryo: Crimson Dawn, Azure Tide, Jade Serpent,
  Frostglass, Blackwater, Emerald Thorn, Ashen Wolf, Venom Orchid; the other 8 Wazamono).
- `client/sword/KatanaMesh` (reads .hkm from the mod jar by class-path resource), `client/sword/KatanaRenderer` (whole mesh with
  RenderType.entityCutoutNoCull = m_110458_, then the blade group again additive with eyes = m_110488_, coloured by `shine()`: the eye's
  ray reflected off each blade vertex's view-space normal, a band of sky light that slides as the blade turns plus an edge flash).
  Evidence added: Matrix3f.transform ([lib] JOML). `RyouTests` exists; add `SwordTests`.

## Still to build, in order (a plan; change what the code proves wrong)
1. build.py packing of meshes and atlases (above). Then items: `KatanaItem extends Item` (NOT SwordItem: the original's Haki Blade Cut,
   HakiAction.HAKI_BLADE_SLASH in HakiKeys$ForgeBus.tick, fires for vanilla sword swings with Armament on; check its instanceof test),
   stacksTo 1, rarity by grade (Item.Properties.m_41497_ and Rarity.EPIC are proven by the original's FruitItem; COMMON/UNCOMMON/RARE
   need [srg] lines: enum names are kept), tooltip m_7373_ (FruitItem overrides it the same way): grade, hamon, description, "Swordsman:
   drawn in combat mode (R)". A DeferredRegister<Item> in a new PieceItems, registered from HexPiece.init. Creative tab: HexGhoul's
   ControlItems used BuildCreativeModeTabContentsEvent; COMBAT's CreativeModeTabs field needs a [srg] line. Lang names.
2. Client: `initializeClient` -> `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ...)` (a client class only inside a lambda:
   PieceVerify's server-safety rule) giving IClientItemExtensions.getCustomRenderer = a BlockEntityWithoutLevelRenderer (HexGhoul's
   allowlist proves its constructor, m_108829_ renderByItem, Minecraft.m_167982_ / m_167973_). Item model JSON "parent":
   "builtin/entity", "gui_light": "front". In renderByItem: GUI -> the icon on a quad; GROUND/FIXED/HEAD -> blade + saya together;
   every hand context -> nothing (the player layer draws it: never twice). ItemDisplayContext GUI/GROUND/FIXED/HEAD need [srg] lines
   (joined.tsrg cfw g GUI, h GROUND, i FIXED, f HEAD; intermediary field_4317/4318/4319/4316).
3. Swordsman state on the server (new `server/SwordServer`): the active katana (an id in the stack's NBT so it survives rearranging;
   main inventory + hotbar + offhand only), drawn / sheathing / sheathed, the combo step, the charge; synced to trackers (a new S2C
   message; protocol "4"); only a Swordsman-style player in combat mode can draw. FightingStyle.SWORDSMAN at the END of the enum (sent
   by ordinal); the start menu (ui/CreationView) and the Captain's Log must take a fifth style; StyleTests expect four today.
4. The player layer (`client/sword/SwordLayer`, added to both player renderers like HakiHandLayer is): the saya at the left hip (right
   for a left-handed player) from the body part's transform, the blade inside it or in the main hand (the arm's transform + haki/ArmBend
   for the forearm + the animation's "rightItem" rotation as the wrist: AnimationApplier.get3DTransform needs [lib] evidence). First
   person: playerAnimator's first-person pass (FirstPersonMode) - check how HexGhoul's client/animation/FirstPersonArms does it.
5. Animations (a new tools/sword_anims.py on tools/anim.py, lint, previews with the blade drawn): draw cut, 12+ M1 motions (opposing
   diagonals, both horizontals, rising, thrust, two-handed overhead, pivot, advancing, aerial), charge hold, release (physical / Haki),
   sheathing (the blade's tip into the koiguchi). Left hand on the handle in two-handed cuts: solve it as tools did for the clasp
   (numeric IK). Bake each move's blade path (base and tip, per tick, player frame) into a generated Java table for the server's swept
   hit volumes.
6. Combat (`style/` pure tables + server): the combo graph (ground / moving / airborne branch picked from the player's state), a small
   input buffer, strike windows per cut, swept volumes, struck sets per window; M2 charge from server time (config: 40 ticks max), the
   physical cut, the Haki slash entity (register like PieceEntities), its renderer in the Ryou colour or neutral, the vertical cutting
   plane with a per-tick budget, protection, mobGriefing for pirates. ForgeConfigSpec is proven (HexGhoul's list).
7. Pirates: SeaRules.styles gives pirates SWORDSMAN too; a swordsman pirate gets a katana by rank (its look) and fights with the same
   tables; marines unchanged.
8. Previews to judge (the house rule): the katanas in hand and at the hip, every sword animation with its blade, the trail, the slash.
   Release notes 2.1.4, README, this handoff, push, both workflows green.

## Ask the owner after 2.1.4 (and from 2.1.3)
- Which HexGhoul muscle assets did they mean (none found for players)?
- Gear 5 moved to Ctrl + R in Devil Fruit mode: right? "Flowing Armament / Ryuo" renamed "Flowing Armament": right?
- The pirates already fight with real Fishman/Iron Fist moves through the shared combat: what did they see?
- The grades given to each katana (Saijo O / O / Ryo / Wazamono): their own ranking?
