# HexPiece: start here (written at 3.7.6, the owner back on Sunday)

This is the newest handoff. It is self-contained for the next work; the older ones hold the history and every standing rule in full:
docs/handoff/next-ai-3.6.3.md (sections A to AN: every owner request since 3.6.3 verbatim, and what each version did),
docs/handoff/next-ai-3.6.0.md sections I to N, and docs/handoff/next-ai-3.2.2.md (POINT ONE and sections 0 to 7: every standing rule).
Read CLAUDE.md first, then this file whole, then next-ai-3.6.3.md sections AH to AN (the last day's work), then the rest as needed.

## 1. Where things stand

- Repo HexB1tDev/HexPiece, branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest; push only there; no pull requests).
- Last release **3.7.6**, GitHub green: Build #261, Release #167 (3.7.5: Build #260, Release #166). `build.py` VERSION 3.7.6; the next
  version the owner should test is **3.7.7** (`python3 tools/next_version.py 3.7.6`). After a version bump delete the old jar from
  `build/` (DragonTests reads the first `build/hexpiece-*.jar` it finds).
- 3.7.5 (next-ai-3.6.3.md AJ): the owner's bug list AH 1-7: Buzz Cut's head held still, the Tori human's effects drawn again, a calm
  imposing phoenix walk, X as the phoenix a fire tornado (blue and gold; carries, burns, flings; 1.4x with Conqueror's lightning under
  Conqueror's Haki and Ryou), every Phoenix move's fire rebuilt in the house's style (phoenix/PhoenixBlast: real explosions,
  fireballs, the tornado; no tongue particles), no 360 spins anywhere, the Horo projection's arms still and slim for slim skins.
- 3.7.6 (AN): Bluebird charges, flies and blows up as a **sphere of flames** (PhoenixBlast.sphere; no bird, no tail); in every human
  Phoenix move the **arms themselves become the phoenix's wings** (PhoenixForm.armWing, PhoenixFx.armWings via
  PhoenixClient.renderBody; the arms hidden), burning in and away like the full transformation (PhoenixForm.armFire, armHidden,
  wingShown; PhoenixFire.arm, front). Never scaled.
- Nothing has been tested in game by anyone but the owner. Never claim it was.

## 2. The owner's standing rules added on the last day (keep them, pass them on)

- "this whole shrinking the model till it disappears sucks major ass, I dont wanna see it again": NEVER scale a model or a body in
  or out to make it appear or go. Things burn into being and burn away (as the Phoenix's full transformation and its arm wings do).
- "the moves should never make the bird spin a 360": no 360 spins of a transformed body in any move.
- "I dont like the fire particles from the moves": no tongue particles in moves; the house's 3D fire (FireMesh puffs, ribbons,
  explosions as dragon/UoFire and phoenix/PhoenixBlast build them).
- "I dont need gifs fix it": render previews to judge your own work (the rule stands), but do not spend time making GIFs or GIF pages
  unless the owner asks for them.
- Every effect within the phone budget: under 16000 quads a frame, counting the phoenix's own model (11551 faces) plus its fire and
  glow (about 1.7k) when it is beside it: anything drawn next to the phoenix has about 2.7k quads (DragonTests.phoenixMoves checks it;
  PhoenixBlast.BLAST_QUALITY 0.7 and local FireMesh.quality drops are how the big effects fit).

## 3. What to do next, in order

### 3.1 The Phoenix's transformed M1: a flamethrower out of its opened beak (DONE in 3.7.7: section 5)
The owner, verbatim: "Transformed M1, shall make the bird open it's beak and act as a flamethrower (blasting flames at the target like
the dragon does)."
- The dragon's M1 fire breath is the model: server/UoMoves.flame (M1 held in dragon form: C2SDragon from client/dragon/DragonInput;
  BREATH ticks, the cone's half-angle, who is breathing since when), its art in dragon/UoFire, its sound UoSounds.FLAME_START/LOOP.
  Do the same for the phoenix in its blue and gold (PhoenixFire.CYAN, GOLD palettes; PhoenixBlast's puffs and ribbons), the flames
  pouring out of the beak along the look, damage in the cone, set alight; Conqueror's lightning with Haki and Ryou (ConquerorAura.mixed
  out of the flames, as POINT ONE says).
- The beak opening: the owner's phoenix (assets/hexhaki/phoenix/marco.sry, 213 parts, 21 bones) has a "head" bone but no jaw bone;
  the bill is its "Bill" parts. Split them (lower bill = the Bill vertices under the mouth line, found by position, as Shape.feet does
  for the toes) and turn the lower part about a hinge at the mouth's corners while breathing (Motion field, e.g. `beak`, 0 to 1).
  Render it (tools/preview/PhoenixPreview, a new scene) and judge it.
- Wire M1 for the phoenix only while transformed (PhoenixServer's form state; the human keeps its M1).

### 3.2 The new fruit: Neko Neko no Mi, Model: Leopard (Lucci) (not started)
The owner, verbatim:
"New Fruit:
Legendary Leopard Fruit
Name: Neko Neko No Mi
Type: Zoan
Model in the Zip
Size: 5 blocks tall
Todo:
Animate it properly, realistic fluid animations, the mane and body hair IS black flames, the black ribbon around the leopard is black
flames as well, We need Idle, running, jumping, transforming and de-transforming animations, facial animations too (simple)
Moves (Contains both transformed and untransformed):
https://official-fruit-battlegrounds.fandom.com/wiki/LeopardV2"

**The owner's files (never alter them; they are in the repository):**
- `input/neko/one-piece-bounty-rush-lucci-awakened-form.zip` (the zip as sent: `source/lucci.rar` and
  `textures/pl_lucci_eggh02_diff.png`).
- `input/neko/lucci/` (the same, unpacked so nobody has to open the rar):
  - `pl_lucci_eggh02 (merge).fbx`: binary FBX 7.3, 8.5 MB, skinned. Read it with `tools/fbx_reader.py` (`version, root =
    fbx_reader.load(path)`; `root.find('Objects')`), or import it with Blender's Python module as the phoenix was baked
    (`python3 -m venv /tmp/bpy && /tmp/bpy/bin/pip install bpy`, then `bpy.ops.import_scene.fbx(filepath=...)`; tools/phoenix_bake.py
    is the template: decimate for the phone, bake to the dragon's .sry format, an atlas texture).
  - `pl_lucci_eggh02_diff.png` (the diffuse texture from the rar), `zip-textures/pl_lucci_eggh02_diff.png` (the zip's copy: a few
    bytes differ, use the rar's), `stg_gasha_tex_bayer_dither.png` (a 149-byte dither mask the game used for transparency).
- What is in the FBX (read on the last day):
  - 22 meshes: body (2237 vertices), hair (1142: the mane), tail (135), tail_hair (378), elbow_hair (358), beard (316), hagoromo
    (1335: **the ribbon round the leopard**), hair_sp (294); the face in four expressions as separate meshes: face_normal, face_attack,
    face_damage, face_sp01 (**the simple facial animation: show one at a time**); the hands in four poses each side: l/r_hand_open,
    _close, _shigan (Finger Pistol), _shugan (show one at a time); l/r_arm_a.
  - 3 materials: pl_lucci_eggh02 (the body), pl_lucci_eggh02_trans_hair (the mane and body hair: the owner's "black flames"),
    pl_lucci_eggh02_trans_hagoromo (the ribbon: "black flames as well").
  - 68 bones: Body_Pelvis, Body_Belly, Body_Chest, Head_Neck, Head_Face, beard_01/02_joint, l/r_ear_01_joint, LArm_/RArm_ Clavicle,
    Upper, Fore, LHand_/RHand_ Fore_sup, Palm, LLeg_/RLeg_ Thigh, Calf, LFoot_/RFoot_ Heel, Toe, hair (b_c_hair_01..04,
    c_hair_sp_root, l/r_hair_sp_01..03), l/r_elbow_hair_joint, tail_01..tail_09, tail_end, and the ribbon's smoke chain (smoke_root,
    c_smoke_01_*, l/r_smoke_01..04_*).
  - 32 animation takes from the game itself, usable as they are or as the base of ours: **pl_lucci_eggh02_idle_a, _run, _jump,
    _jump_lp, _jump_end** (the owner's idle, running and jumping), _combo_a, _combo_b, _combo_c, _combo_b_change, _combo_b_change_end,
    _skill_a, _skill_b, _dodge, _damage, _stun, _shake, _slammed, _down, _down_end, _blownback_lp, _blownback_end, _electric_shock,
    _victory, _victory_lp, _flagget, _flagget_lp, _flagget_end, and camera takes. No transform take: the transformation and its reverse
    are ours to make (burning into the leopard in black flames, never scaling: section 2).
  - Height: the body mesh is 2.30 units tall (y up): scale about 2.17 to make it 5 blocks tall, as the owner asks.
- The look: the body as the texture; the mane, the body hair (hair, tail_hair, elbow_hair, beard, hair_sp) and the ribbon (hagoromo)
  **are black flames**: draw them as living fire, black with a dark-red/purple rim (the Phoenix's skin flows like fire through
  PhoenixForm.heat and PhoenixForm.flame: do the same in black), alpha-blended, never additive.
- The moves (the wiki page LeopardV2; download its GIFs into the scratchpad and read them frame by frame, as next-ai-3.6.3.md D and AD
  did for the Phoenix's; never commit them):

  | Key (suggested, as the Phoenix's) | Move | The wiki | GIFs (base, transformed) |
  |---|---|---|---|
  | M1 | Six Styles | a 5-hit combo of kicks, punches and a mini Rokuogan blast at the last; blockable until the last hit; 69 dmg | SixStylesBase.gif, SixStylesForm.gif |
  | X | Finger Pistol Barrage | a rapid barrage of finger strikes dragging the target along, the last hit sends them flying; blockable; 58 | FingerPBase.gif, FingerPForm.gif |
  | C | Sonic Kick | winds up the leg and kicks, sending a bird (an air blade shaped like a bird) at high speed that explodes at the end; drags anyone in its path who doesn't block, the explosion breaks guard; 62.5 (transformed: another animation, +1 damage) | SonicKickBase.gif, SonicKickForm.gif |
  | V | Infinity Drive | teleports to the target, blitzes them with afterimages, a last strike flings them a random way; blockable; 52 | InfinityDriveBase.gif, InfinityDriveForm.gif |
  | B | Unrelenting Assault | teleports to the cursor's area and barrages it with claws for a while, then reappears and slams the ground, sending them into the air; blockable until the last tick; 116 | AssaultBase.gif, AssaultForm.gif |
  | Z | Transform | slouches down, taps into the awakened power and transforms, a roar that sends nearby players back before the form is revealed; transformed: M1s the same damage, a voice line on Soru, speed up DRASTICALLY; Sonic Kick another animation | Leov2Transform.gif |
  | (transformed only) | Heavens Demise | grabs a target, sends them into the air, blitzes them in a cutscene-like sequence and finishes with a Rokuogan straight down; unblockable; 272 | HeavensDemiseForm.gif |

  The GIFs (800 x 452), each at https://static.wikia.nocookie.net/official-fruit-battlegrounds/images/ +
  8/87/AssaultBase.gif, 2/20/AssaultForm.gif, b/b9/FingerPBase.gif, d/d8/FingerPForm.gif, 6/6e/HeavensDemiseForm.gif,
  3/3f/InfinityDriveBase.gif, 4/46/InfinityDriveForm.gif, 2/2a/Leov2Transform.gif, f/fd/SixStylesBase.gif, 8/85/SixStylesForm.gif,
  6/60/SonicKickBase.gif, a/a0/SonicKickForm.gif; the fruit's art 2/26/Leopard_v2.png (4096 x 4096). The page's wikitext:
  `https://official-fruit-battlegrounds.fandom.com/api.php?action=parse&page=LeopardV2&prop=wikitext&format=json`. Both hosts are
  allowed in the personal "HexPiece" cloud environment.
- How a fruit is added (the Phoenix, 3.6.9, is the template: next-ai-3.6.3.md U to Y): patch/PiecePatch.java (a String[] like TORI:
  {"id", "full name", "short name", "colour hex", "description"}, added to `extra` and `names` near line 924: this adds the Fruit
  enum value), HexPiece.java (`Fruits.register(Fruit.X, Kit::new)`), a server kit (server/PhoenixKit: the move list per form) and its
  moves (server/PhoenixMoves), the form's server state (server/PhoenixServer), the client kit (client/phoenix/PhoenixClient, put in
  ClientKits by client/phoenix/PhoenixFx), FruitRanks.java (its rank: the owner says Legendary), client/LogbookData, the lang entry
  (`item.hexhaki.<id>_no_mi`), the item's icon and model (assets/hexhaki/textures/item, models/item; draw a new icon: never use the
  Black Soul assets of the removed fruit, CLAUDE.md), the model baked into assets, the clips in tools/*_anims.py (lint must pass:
  never key the head or unused limbs), CC0 sounds (or the house's existing CC0 events), tests in src/test (budgets, kit lists).
- POINT ONE for every move: the house's 3D style (meshes, clips without the head, a first-person hand move per move, CC0 sounds) and
  the Conqueror's lightning on every move with Conqueror's Haki and Ryou (out of the end explosion when there is one).

### 3.3 Zoan forms locked out of Style (DONE in 3.7.8: section 5)
The owner, verbatim: "In no ZOAN transformed state can you switch to Style or any other mode aside from fruit. (Tori, gear 5,
dragon, etc)."
- The mode switch is client/CombatMode.java (R swaps between the fighting style and the fruit; the Gomu fruit through
  GearKeys.switchMode; the original's AbilityMode HAKI / DEVIL_FRUIT). While transformed, refuse the switch (and any other way into a
  style), and put it back to the fruit if somehow out of it; check it on the server too if the mode is server-side.
- Transformed states: the Phoenix (client PhoenixFx.selfOut(); server PhoenixServer / PhoenixMoves.phoenix(player)), the dragon
  (client DragonClient.selfOut(); server DragonServer), Gear 5 (client/gear/NikaKeys and the original's gear state), the leopard
  (new). A short message on the action bar when refused is fine.

### 3.4 Smaller open items
- The arm wings are third person only: in first person the hand is still a hand (ClientKit.firstPerson can draw a wing on the
  first-person arm, as SupaClient.firstPerson draws blades). Ask the owner if they want it.
- Item 4 of the owner's Mochi note 22 came empty: ask for it.

## 4. How to work here

- Build: `python3 build.py` (ASM patch, javac -Werror, PieceVerify, 13 test suites). Then a push to the branch publishes the jar as a
  pre-release; a new `.github/release-notes-<version>.md` publishes `v<version>` as the latest release. A push is done only when
  GitHub's "Build HexPiece" (and "Release HexPiece" when notes change) runs are green (mcp__github__actions_list, method
  list_workflow_runs, branch filter).
- Stubs (stubs/) are compile-only: use only methods they have (e.g. no JOML Vector3f(Vector3f) copy or normalize(): do it by hand).
- Previews: tools/preview/PhoenixPreview.java (compile it with phoenix/*.java, dragon/DragonPose.java against build/classes,
  build/base.jar, build/stubs; run `java ... PhoenixPreview src/main/resources/assets/hexhaki/phoenix/marco.sry
  src/main/resources/assets/hexhaki/textures/piece/phoenix/marco.png out.png --size=420x320 scene:seconds:yaw:pitch:dist[:tx:ty:tz]`).
  Scenes: walk, hover, fly, glide, dive, human, form, unform, land, m-fujih, m-fujip (the tornado), m-talon, m-hawk, m-infh, m-infp,
  m-blueh, m-bluep, m-boom, m-tornado, m-tornado2, m-sphere, m-armwings. tools/anim_preview.py renders the player clips. Make a
  leopard preview the same way.
- Every owner message: write it verbatim into the handoff the moment it arrives (append to next-ai-3.6.3.md or this file), answer in
  English, do exactly what is asked and nothing unasked.
- At 98% weekly usage: write the handoff and next-prompt.md first, commit, push, stop (no CI wait).
- Commits end with the trailers the session gives (Co-Authored-By and Claude-Session); no model names in commits or code.

## 5. The next session (started Sunday): what it did

- Started on Default; switched to the personal "HexPiece" environment (network_only). 3.7.6's runs were green (Build #261, Release
  #167; the handoff push Build #262 green). The owner sent no word on 3.7.5 or 3.7.6 in game this session.
- The local branch was a stale clone (ba40945, 50 commits behind the force-updated remote): kept on the local branch
  `backup/stale-ba40945` (never pushed), the branch reset to the remote's d81ffdb.

### 3.7.7: the Phoenix's transformed M1, the flamethrower (3.1)
- **The beak:** phoenix/PhoenixForm.split (run by Shape's constructor): the owner's bill ("Bill | ivory tapered profile", one closed
  mesh, no jaw bone) split along its mouth lines ("Bill L/R | mouth line": their points fitted with a straight line): each face whose
  middle lies under it (JAW_LINE 0.004), within the lines' length (the hook in front is the upper bill's), is the lower jaw's; vertices
  both jaws use are copied (dragon/DragonModel.replace: in memory only, the file never changed; no face added). Shape.bill, jaw[],
  hingeY/Z (the mouth lines' back ends), billTip, jawTip, corners. PhoenixForm.skin -> jaw(): the lower jaw turned down about the
  hinge by Motion.beak * JAW_OPEN (30 degrees). PhoenixForm.gape: the flame's point (GAPE 0.7 of the way from the corners to the
  jaws' tips) and the beak's way.
- **The pose:** Motion.beak, breath, aim (the look's pitch). pose(): breathing, the body reared back (BREATH_REAR 10), the neck thrust
  (BREATH_THRUST -22 - 0.4 aim, a shudder), the head (BREATH_HEAD 16 - 0.35 aim), the idle look about held still, the wings held
  wide, the beat's amplitude cut by 45%.
- **The art:** phoenix/PhoenixBlast.breath (UoFire.breath's construction in the phoenix's palettes): BREATH_RATE 1 puff a tick living
  BREATH_LIFE 12 ticks (cyan, every fourth gold, torn into tongues, the dark-blue cel outline), the white jet cooling to cyan (30% of
  the range), the heart in the beak, two glows, shock rings every 4 ticks; BREATH_QUALITY 0.65. Budget: about 2.6k quads at its
  peak; DragonTests.phoenixMoves checks it beside the phoenix (every frame < 16k).
- **Server:** server/PhoenixMoves.flame (public; network/C2SDragon calls it beside UoMoves.flame, each returning for the other's
  form), class Flame (a Live move: other moves refused while it runs): FLAME 408 on/off, FLAME_RANGE 24, FLAME_CONE 10, FLAME_MAX 100,
  FLAME_LEAST 6, a burn every FLAME_EVERY 5 ticks of FLAME_HURT 3 (hit(): the Conqueror's lightning at the one burnt; at the wall it
  strikes when none), alight FLAME_FIRE 5 s, cooldown FLAME_COOL 80 when it stops; the beak at BEAK_UP 2.2 over the feet, BEAK_AHEAD
  2.3 along the look; sounds UoSounds.FLAME_START, FLAME_LOOP (pitch 1.15). server/PhoenixKit.FLAMETHROWER ("Phoenix: Flamethrower",
  "[LMB] hold", "phoenix_flame"), in PHOENIX (6 moves now) and MOVES.
- **Client:** client/phoenix/PhoenixBreath (new): the flames (accept from PhoenixMoveFx), the gape where PhoenixFx.draw last drew it
  (PhoenixBreath.drawn), a 24-tick track of the beak and aim (the puffs follow the aim they left with), the reach eased to the wall,
  pose() (beak opens over 3 ticks, shuts over 5; the aim in over 4, out over 8), draw() from PhoenixMoveFx.draw. In first person the
  flame leaves from 2.5 ahead and 0.9 under the view. PhoenixFx.phoenixAttack cancels the game's attack while one's phoenix is out;
  PhoenixFx.flame sends C2SDragon on M1's state (selfPhoenix: out, no screen).
- Previews: tools/preview/PhoenixPreview scenes m-beak (the beak alone opening and shutting), m-flame (held 40 ticks, aim 8 down),
  m-flamed (aim 35 down, 9 blocks), m-flameg (standing on the ground); compile with phoenix/*.java, dragon/DragonPose, DragonModel,
  FireMesh, UoFire (the scratchpad's pprev.sh).
- Tests: DragonTests.phoenix (the split: no face lost, 20 or more lower-jaw vertices, stable when split again; opened, the jaw's tip
  0.3 down, the upper bill still, the gape between the jaws facing ahead), phoenixMoves (the flamethrower's budget), the kit (6
  phoenix moves, FLAMETHROWER at 5).

### 3.7.8: Zoan forms locked out of Style (3.3)
- server/Transformed.zoan (transformed(): the dragon, Gear 5; or the phoenix from the change to the way back; ADD THE LEOPARD when it
  comes), FRUIT_ONLY (the action-bar text). StyleServer.combat refuses "on" while zoan; Transformed.transformedTick drops a stance
  found on while zoan (every 10 ticks).
- The original's mode key (Devil Fruit mode / Haki mode: HakiKeys' LOADOUT -> GearKeys.switchMode -> GearServerController.onAction ->
  switchMode): PiecePatch widens GearServerController.switchMode to public and redirects onAction's call to Transformed.switchMode
  (refused while zoan, else the original's).
- Client: CombatMode.zoan (DragonClient.selfOut, PhoenixFx.selfOut (now public), GearClientState.gearFive): R's tap refused with the
  message (Ctrl + R Armament and R held for powers off unchanged); combat mode turned off if on while zoan.
