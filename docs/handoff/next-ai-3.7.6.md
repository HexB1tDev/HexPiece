# HexPiece: start here (written at 3.7.6, the owner back on Sunday)

This is the newest handoff. It is self-contained for the next work; the older ones hold the history and every standing rule in full:
docs/handoff/next-ai-3.6.3.md (sections A to AN: every owner request since 3.6.3 verbatim, and what each version did),
docs/handoff/next-ai-3.6.0.md sections I to N, and docs/handoff/next-ai-3.2.2.md (POINT ONE and sections 0 to 7: every standing rule).
Read CLAUDE.md first, then this file whole, then next-ai-3.6.3.md sections AH to AN (the last day's work), then the rest as needed.

## 1. Where things stand

- Repo HexB1tDev/HexPiece, branch `claude/nice-bell-gam7qf` (fetch it first: it is the newest; push only there; no pull requests).
- **Updated on Wednesday (section 7):** 3.8.0 the Ito Ito no Mi's Sky Road keeps swinging and its speed (the owner's only request);
  `build.py` VERSION 3.8.0, the next is 3.8.1.
- **Updated at the end of the Sunday session:** last releases 3.7.7 (the phoenix's flamethrower; Build #263, Release #168 green),
  3.7.8 (Zoan forms locked out of Style), 3.7.9 (the Leopard fruit: Z, the base animations, the M1s, the icon); sections 5 and 6 at
  the end of this file say what each did and what is next. `build.py` VERSION 3.7.9; the next version is **3.8.0** (versions roll over
  at 9). After a version bump delete the old jar from `build/` (DragonTests reads the first `build/hexpiece-*.jar` it finds).
- (Written at 3.7.6:) Last release **3.7.6**, GitHub green: Build #261, Release #167 (3.7.5: Build #260, Release #166).
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

### 3.2 The new fruit: Neko Neko no Mi, Model: Leopard (Lucci) (first part DONE in 3.7.9: Z, base animations, M1s; sections 5, 6)
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

### The owner, during the Leopard work (verbatim; with a 2D picture of the fruit)
"first of all, Z (transformation, get the base animations, and m1s.) thats your only goal today Im runnin out of usage.

I also sent the pic of the fruit 2d"
- The picture (not saved in the repository; describe, never copy it): a golden-yellow heart-shaped fruit with a cat's face shape, two
  pointed cat ears at the top each with a black spiral swirl on a pale inner ear, the whole skin covered in leopard rosettes (dark brown
  rings round lighter brown centres), a glossy white highlight on the left, and a green stem rising from the top centre into a T: one
  arm curling into a spiral on the left, the other ending in a small round yellow bud on the right. Black ink outline.
- So the Leopard's first version is: Z (the transformation and its reverse), the base animations (idle, run, jump, the face) and the
  M1s (Six Styles, both forms), and the fruit's item icon drawn after the picture. The other moves (X, C, V, B, Heavens Demise) later.

### The owner, while 3.7.9 was being finished (verbatim)
"the transformation should be a ball of burning flames with evil red eyes within, the flames of the mane, tail and ribbon make sure those
are real looking burning flames and call it a day enough work, push, handoff and prompt"
- And then (verbatim): "black flames not purple" (the flames' palette is black to charcoal only: no purple, no red; the red only in
  the ball's eyes and Rokuogan's rim).

### 3.7.9: the Neko Neko no Mi, Model: Leopard, first part (Z, the base animations, the M1s, the icon)
- **The model:** tools/leopard_bake.py (Blender's Python module: `/tmp/bpy/bin/python tools/leopard_bake.py`; it patches one line of
  the venv's FBX importer first and asks to be run again) bakes input/neko/lucci into assets/hexhaki/neko/lucci.sry (22 parts, 69
  bones, 4 weights a vertex, 27 takes at 25 a second, about 2.5 units tall) and copies the file's texture to
  textures/piece/neko/lucci.png. Kinds: 0 skin, 2 the black-flame hair (hair, tail_hair, elbow_hair, hair_sp, beard), 3 the ribbon
  (hagoromo). build.py NEW_PREFIXES has "assets/hexhaki/neko/".
- **neko/LeopardForm (pure):** SCALE 2.08 (the idle stands 5.0 blocks); Shape (each part's kind, face (face_normal, face_attack,
  face_damage, face_sp01) and hand pose (l/r_hand_open, close, shigan, shugan) shown one at a time: shown(); each flame vertex's
  reach; the tongues' anchors: 18 far tips of the mane, tail tuft, ribbon, elbow and shoulder hair, beard); Motion (speed, stride,
  air, up, down, face, hands, blow, blowAge, roar); pose(): idle_a always, run by the speed (its phase from the ground covered:
  NekoFx.STRIDE 5.5 blocks a cycle), jump (take-off TAKE_OFF 8), jump_lp in the air, jump_end landing (LANDING 12); a blow: one of
  the model's takes (BLOW_TAKES combo_a, combo_b, combo_c, combo_a, skill_b, from BLOW_FROM, over BLOW_TICKS 10 or 18) with the take's
  ground travel pinned out (world_joint and Body_Pelvis x, z kept). flame(): the flame vertices lick and flow; flameColour(): black
  with charcoal sheen flowing out along them. The change (the owner's "ball of burning flames with evil red eyes within"):
  FORM_TICKS 34 (the ball gathers over BALL_IN 6 round the slouching human, who is hidden from then; the roar at ROAR 24 bursts it
  and the leopard is there), UNFORM_TICKS 24 (the bigger ball gathers round the leopard by 6, the leopard gone; it burns away from
  BURN_AWAY 12, the human back at HUMAN_BACK 15); BALL_SMALL 1.55 at 1.0 over the feet, BALL_BIG 2.6 at 2.3; revealed(),
  humanHidden(), ball(). Never scaled.
- **neko/LeopardArt (pure):** BLACK_FIRE (the black flames' palette), puff, ball (16 black flame puffs flying in on a golden spiral,
  rolling, licking up; a dark heart; two red eyes: a glow, a slanted slit, a white-hot point), tongue (a black flame rising off a
  point, thinning to smoke), human (flames rising round the slouching human), line (unused now), roar (a dark shell racing out, a
  ring, white crescents, black flames thrown), strike (blows 1 to 4: an arc, three claw streaks, a wide flat arc, two jab streaks),
  land (a shock ring, a flash, sparks), rokuogan (a white shock shell with a dark red rim, two rings along the blow, crescents, sparks).
- **Server:** server/NekoServer (PHASE 420; Z toggles; REST 60; the roar at ROAR throws everyone within ROAR_REACH 7 back
  (ROAR_PUSH 1.4, ROAR_LIFT 0.45), UoSounds.ROAR pitched 1.35, ConquerorAura.exploded; Speed 5 (SPEED 4) renewed every 10 ticks as
  the leopard; the sea reverts it; restore on death, leaving, a change of world). server/NekoMoves (M1 421 thrown, M1_LAND 422
  landed): five blows by click (M1_EVERY 8, the leopard 7; after the fifth COMBO_REST 16; a pause over COMBO_WINDOW 26 starts again);
  each lands M1_HIT 3 ticks after the click (Rokuogan 5) on ItoMoves.ahead (REACH 3.4, the leopard 5.2, ARC 55): M1_HURT 2, 2, 2,
  2.5, 5 (the reference's 69 over five, a fifth; the same in both forms), a small push, Rokuogan's throw (ROKU_PUSH 2, ROKU_LIFT
  0.55), the Conqueror's lightning at each one hit and out of Rokuogan (ConquerorAura.exploded); IronSounds SWING, PUNCH, BOOM.
  server/NekoKit (FORM [Z], SIX_STYLES / SIX_STYLES_LEOPARD [LMB]; action kind 0 Z, kind 2 the attack button). PiecePatch NEKO
  (ordinal 12, colour E8B020), HexPiece registers it, FruitRanks LEGENDARY, lang "item.hexhaki.neko_neko_no_mi", the icon
  (tools/neko_icon.py) and item model. Transformed.zoan includes the leopard.
- **The human's clips:** tools/neko_anims.py: neko_form (slouch, the roar), neko_unform, neko_m1_1 to 5 (kick, claw swipe,
  roundhouse, double punch, Rokuogan); lint clean. style/HandMoves: neko_m1_* move the first-person hand.
- **Client:** client/neko/NekoFx (S2CUoFx 420 to 439 from client/dragon/UoFx; the leopard skinned on the CPU and drawn in the entity
  buffer (RenderType m_110458_, full bright, LeopardForm.tint's own shade), its flame parts and tongues after the particles (debugQuads,
  alpha blended), the ball, the roar, the blows; the human hidden; third person on the change; DragonCamera asks NekoFx.camera (4 +
  4.5 back, 2 up); the hurt face from the health dropping), client/neko/NekoClient (the panel: the form's list), CombatMode.zoan
  includes it.
- **Previews:** tools/preview/LeopardPreview (compile with neko/*.java, phoenix/*.java, dragon/DragonPose, DragonModel, FireMesh,
  UoFire, PhoenixPreview; the scratchpad's lprev.sh): states bind, take=<name>, idle, run, walk, jump, land, blow1 to 5, form, unform,
  fx1 to 5 (the leopard's blows with their effects), hfx1 to 5 (the human's); ",face=N", ",hands=N".
- **Tests:** DragonTests.leopard (parts, bones, takes; one face, one hand pose each side; 5 blocks tall; every state finite; the blows
  stay in place; the change one way, never scaled; the ball, the roar, the tongues and the blows beside the model under 16k; the kit).

## 6. What to do next (the Leopard, in order)
1. Check 3.7.9's runs; the owner's word on 3.7.7 (flamethrower), 3.7.8 (Zoan lock) and 3.7.9 (the Leopard) comes first, verbatim.
2. The Leopard's other moves, each in both forms, after reading their GIFs frame by frame (section 3.2's table; the scratchpad's
   reading so far: Transform = slouch in black and purple light, a roar, a black dome bursting out (now the owner's ball of black
   flames with red eyes instead); Six Styles = five blows half a second apart then Rokuogan launching the target; Finger Pistol =
   a storm of finger-jab streaks with black speed lines dragging the target, ending in a black burst that sends them flying): X Finger
   Pistol Barrage (the hands' shigan pose exists), C Sonic Kick (a bird-shaped air blade that explodes), V Infinity Drive (teleport,
   afterimages, a last strike), B Unrelenting Assault (teleport, claw barrage, a slam), and Heavens Demise (transformed only: grab,
   launch, a cutscene-like blitz, a Rokuogan straight down). POINT ONE for each; the Conqueror's lightning out of every end explosion.
3. Then 3.4's small items (the phoenix's arm wings in first person: ask; the Mochi note 22 item 4: ask).

## 7. The next session (Wednesday): 3.8.0, the Ito Ito no Mi's Sky Road keeps swinging and keeps its speed

The owner, verbatim (the session's only request): "If possible using the very latest release, please make the Ito ito no mi, not stop
swinging or lose momentum, currently both things are happening which ruin the fun, dont touch Tori TOri or the Neko Neko, only ito"
- The latest release was v3.7.9 = the branch's head ec5695a: built on it. Only the Ito fruit's Sky Road was changed (ito/SkyPhysics,
  client/ito/SkyRoad, server/ItoMoves' Sky Road timer, ItoKit's Sky Road text, HudTests.skyRoad, tools/preview/SkyRun); no Tori, Neko
  or other fruit file touched.
- **What stopped it and lost the momentum** (found by running client/ito/SkyRoad.drive's own state machine tick by tick against
  SkyPhysics; the scratchpad's sim/Sim3.java): (1) letting go of the keys even for half a second turned the swing into the hang, whose
  HANG_DRAG 0.93 took the speed from 0.66 to 0.03 blocks a tick in 10 ticks; with no key the next string was a hang too; (2) every
  string snapping taut (the body flying free inside a slack string, or the string reeling in onto a falling body) projected the body
  back onto the string's sphere and threw away the part of the velocity going out of the anchor: a quarter of the speed at a usual
  catch, three quarters when fast (1.6 to 0.37 in one tick); (3) the next string went along the keys' way, across the body's own way
  after a turn; (4) the swings bottomed out CLEAR 1.3 over the ground under the hand/anchor, so the terrain between scraped them (a wall
  touched cut V to 0.3 on both flat axes) and 3 ticks on the ground ended the run; (5) ItoMoves ended every run at SKY_MOST 600 (30 s).
- **3.8.0 as built** (nothing tested in game; build EXIT 0, 13 suites; the run simulated; SkyRun frames rendered and judged):
  - ito/SkyPhysics: `swing(..., keep)`: a swing's string (keep true; the hang keeps the plain projection) snapping taut (`catchTaut`:
    going out of the anchor faster than CATCH 0.12 a tick as it bites) gets a velocity of the same speed (at most TOP) that lands the grip
    exactly on the sphere (the part out of the anchor (L^2 - s^2 - was^2) / 2 was, the rest along the body's own way across it).
    `way(keys, drift)`: the next swing's flat way, the keys' way leaned toward the drift by up to KEEP 0.6 (at KEEP_SPEED 0.6 or faster),
    or with no key along the drift while the flat speed is CARRY 0.45 or more (else none: a hang). `spent`: a no-key swing that never got
    ahead and swings back slower than CARRY settles into the hang. `pump(v, sprint)`: PUMP / PUMP_SPRINT eased to none at SWING_SPEED
    1.05 / SPRINT_SPEED 1.35 (PUMP_EASE 0.25); `kicks`: the let go's kick only with a key and under that speed (with the catches keeping
    the speed, the old constant pump took every run to TOP, sprinting or not). `letGo`: the faster, the flatter: LET_GO 30 degrees at
    FLAT_FROM 0.6 a tick down to LET_GO_FAST 18 at 1.2. CLEAR 4.0 (was 1.3); `floor(groundY)` = ground + GRIP + CLEAR + DIP. `slide`: a
    wall stops only the axis it held back to under half.
  - client/ito/SkyRoad.drive: SWING goes on with or without a key (pump only with one; the cruise's look-up nudge only with one); let go
    kicks by `kicks`; no key and `spent` -> HANG. next(): `way` decides swing or hang; the cruise raised to `floor` of the ground found
    (under the hand and the anchor). The wall: LAST (one's own feet last tick) vs V per axis by `slide` (was V x 0.3).
  - server/ItoMoves: SKY_MOST Integer.MAX_VALUE / 4 (only the clients' count of it); skyTick no longer ends at the time; it ends on the
    ground (SKY_GROUNDED 3), in water, riding, dying.
  - ItoKit.SKY's text says so. tools/preview/SkyRun mirrors the new swing (pump, keep, kicks, spent, way, floor).
  - Numbers (the sim, held W from 30 blocks up, 20 s): the flat speed held at about 1.0 a tick (21 blocks a second; 3.7.9: 0.76),
    sprinting 1.27 (3.7.9: 0.93); a half second off the keys every three seconds or a steady turn: the same; let go of the keys at 1.0:
    0.89, 0.88, 0.82, 0.72, 0.63 ... a swing, the hang after about 12 s; the swings within -3 / +3 of the cruise; a start 1.2 over the
    ground rises clear and the feet never come under 4 over it again.
  - Tests: HudTests.skyRoad rewritten round `skyRun` (drive's swing, let go, flight and next string with SkyPhysics): far and fast held,
    heights held, never past TOP, sprint faster by 0.15, a moment off the keys and a turn keep 92% of the speed, a coast goes on 5 swings
    or more keeping 70% over its first three and then hangs, a low start stays clear of the ground, the taut catch keeps the speed and lands
    on the string (the plain projection loses 0.4 or more there), `way`, `slide`, no time limit; the leap and step checks kept.
- Release notes .github/release-notes-3.8.0.md. Next version 3.8.1.

- 3.8.0's runs: Build #266 and Release #171 green.
- The owner, after the summary (verbatim): "Peak gj! see ya tomorrow twin" (not said to be tested in game: ask how the Sky Road felt).

## 8. What to do next
1. The owner's word on the Sky Road in game comes first (verbatim into this file).
2. Then section 6 (the Leopard's other moves), as before.
