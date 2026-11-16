# Next AI: after 2.1.1 (the first HexPiece release)

Read `CLAUDE.md` first (the house rules: versions roll over at 9, releases keep 5, the Imu fruit stays gone, judge your own renders,
server safety, never alter the owner's files). Answer the owner in English. Never claim anything was tested in game: nothing can run here.

## The owner's requests for 2.1.1 (quoted)
"Okay, this is hex piece. What you're going to be doing is bettering all the animations, importing this new GitHub that I just made,
imported there using the workflow of ... hex school [HexGhoul]. Import the workflow from there to be like 5 versions, and like whatever
reaches 9 switches to the upper number and all. Delete the previous 5."
"Better every single animation. Get rid of the Imu fruit (Devil Devil fruit) Get rid of it ENTIRELY, but keep it's assets, for future use on
other things. Improve Haki's lightning, usage, animation, and everything else. Add pirate NPCS and Marines. Custom main menu with the same
Male/female as HexGhoul (with a custom pirate UI). New mastery menu (also pirate UI, like blue, white, with a ship?) Custom fightning styles
like HexGhoul with custom movesets, extremely well cohreographed. (also picked at the startt) and also decide between being a pirate or a
marine."
Mid-session, on two original animations (armament_activate, acoc_activate side views): "jesus dont you ever pass shit like this" and
"also feel free to take whatever asset from HexGhoul".

## How it is built
- `input/hexhaki-2.1.0.jar` (the only source of the original; mod id `hexhaki`), `patch/PiecePatch.java` (ASM: bootstrap hook, lightning
  trampolines, Imu local names, mods.toml), `src/main/java/com/hexhaki/piece/` (the expansion), `stubs/` (compile-only, from HexGhoul plus a
  few), `patch/PieceVerify.java` (every new external reference proven by the original jar or `verify/*.txt` with evidence; server safety;
  bytecode analysis), `build.py` (also the archive scope, the Imu check, the animation lint, the tests).
- New SRG names: MCPConfig `joined.tsrg` and Fabric `intermediary` 1.20.1 are on raw.githubusercontent.com (reachable); see the evidence
  lines in `verify/api-allowlist.txt`. HexGhoul's input jar (`hexkagune-1.0.0.jar`) proves the entity-registration calls ([hexkagune]).
- Tools: `tools/anim.py` (playerAnimator sampler, renderer, lint: matches PlayerAnimator + bendy-lib as verified in HexGhoul's RigPose),
  `tools/anim_preview.py` (contact sheets), `tools/anim_polish.py` (the rework of every original; run it after changing `anim_hand.py`),
  `tools/anim_hand.py` (hand-made Haki animations), `tools/style_anims.py` (the 29 style animations AND the generated
  `client/npc/StylePoses.java` for NPCs: always regenerate both together), `tools/npc_skins.py`, `tools/lightning_preview.py` +
  `tools/preview/LightningDump.java`, `tools/preview/UiPreview.java` (renders the start menu and the Captain's Log), `tools/next_version.py`.

## What is where
- Start menu: `ui/CreationView` (pure, previewed), `client/CreationScreen`, `server/ProfileServer` (Forge persisted player data
  `PlayerPersisted.HexPiece`), `network/*` (channel `hexhaki:piece`, protocol "1": bump it whenever a message changes).
- Fighting styles: `style/*` (pure: `StyleMove` timings and hits, `StyleCombo`), `server/StyleServer` (players), `server/StyleCombat` (shared
  with NPCs: targets, damage through `Impact.hit`, carry), `client/StyleInput` (M1/RMB empty hand, Y/U `PieceKeys`), `client/StyleFx`.
- Captain's Log: `ui/LogbookView`, `client/LogbookScreen`, `client/MenuSwap` (replaces the original AbilityBook on open; the log's button lets
  it through once).
- Female body: `FemaleBody`, `client/render/FemaleFigure|FemalePlayers|FigureLayer` (ported from HexGhoul; no hitbox change).
- NPCs: `npc/SeaFighter` (brain in `m_8024_`), `npc/SeaRules`, `PieceEntities`, `server/SeaSpawner` (patrols), `server/PieceCommands`,
  `client/npc/SeaFighterRenderer` + `StylePoser`/`StylePoses`.
- Haki lightning: `haki/Jag`, `haki/ConquerorBolts`, `haki/LightningPaths` (server + client), `client/haki/HakiLightning` (painters).

## Not done / to ask the owner
- Nothing was tested in game. First feedback comes from the owner's test of the CI jar (pre-release `build-<branch>` or release v2.1.1).
- "Custom main menu" was read as the start (character) menu on first join, as HexGhoul's Male/Female screen is. The Minecraft title screen
  is unchanged: ask whether they also want a pirate title screen.
- The Imu fruit: the 2.1.0 jar had no Imu code left, only two variable names (renamed) and the Black Soul assets (kept, unregistered).
  If they have a newer jar with Imu code, it must be supplied.
- Animations: every original passed the automatic rework; only seven Haki ones were hand-made. Next candidates for hand-made reworks:
  galaxy_*, conqueror_charge*, gear5_*, light_* (Pika), the thragg_ pairs (keep attacker/victim ticks in step).
- NPCs: no bendy-lib on NPC models, so bent elbows/knees are approximated; no spawn eggs (commands and patrols only); no loot yet.
- The female bust layer does not follow a body bend during an animation (the head and arms do).
- The style specials' numbers (damage, cooldowns, mastery thresholds) are first guesses: ask how they play.

## Renders (docs/images/)
start-menu-1-gender, -2-flag, -3-style (tools/preview/UiPreview), captains-log-overview, -marine-haki, -style, conqueror-lightning
(tools/lightning_preview.py), npc-skins (front and back of all 16 looks), style-black-leg and style-specials (tools/anim_preview.py),
armament-acoc-before-after (the two originals the owner flagged, rows 1 and 3, against their reworks, rows 2 and 4).
