# Next AI: 2.1.2 half done (Photon particles, combat mode on R); the owner said "Stop" and asked for this handoff

Read `CLAUDE.md` and `docs/handoff/next-ai-2.1.1.md` first (house rules, how the repo is built, what is where). Released so far: v2.1.1
(branch `claude/friendly-allen-g9nrzx`, every CI run green). Answer the owner in English; never claim in-game testing.

## The owner's request for 2.1.2 (quoted exactly)
"now since we use photon, add water particles to fish man, air particles for swings of moves and air blade, same as HexGhoul tap R to
enable combat."
(Earlier, also: "show me a gif of each fighting style": done, `tools/style_gif.py`, `docs/images/style-*.gif`.)

## State: all of 2.1.2's code so far is COMMITTED on the branch (after the stop, the repository's hook asked for a clean tree)
It builds green locally (`hexpiece-2.1.2.jar`, every reference proven, 4 suites pass) but has no release notes yet, so no release was
published. `docs/handoff/wip-2.1.2.patch` is the same code as a patch, kept for reference only: it is already applied, do NOT apply it again.
It holds:
1. **Photon particles (built and verified green before the stop, not pushed):**
   - `client/fx/PieceFx.java`: HexGhoul's `RcFx` ported (Photon `TileParticle` motes on pooled, batched `ParticleEmitter`s; drag,
     weight, wander, colour fade, size change), plus `later(ticks, Runnable)`, a client-tick scheduler for effects in sequence.
     Every Photon/Minecraft call it makes is already in `verify/hexghoul-allowlist.txt` ([photon] evidence).
   - Sprites `assets/hexhaki/textures/piece/particle/`: HexGhoul's mote, puff, streak, ring, slash, spark, steam, flame, ember (the
     owner: "feel free to take whatever asset from HexGhoul") and a new water `drop.png`.
   - `client/StyleFx.java` rewritten on Photon (no vanilla particles): stage 3 = a blow thrown (crescent `slash`, `streak`s along the
     blow, a `ring` and ground dust for heavy blows, streaks all round for spins); stage 1 = a blow landed (sparks, ring); Fishman Karate
     = water (droplets off every blow, splash + spray on hits, `wave` for the ground punch, the counter's turning ring of water); stage 2
     shots: the Air Blade's flying crescent (velocity mote + 2 ghosts + streak trail each tick via `later`, a burst at the end) and the
     water bullets (a flying drop, a dripping trail, a splash on arrival); Flame Leg flames while it spins; dust for Shave and the
     Shoulder Charge; air-step rings for Sky Walk; Iron Body's steel sparks.
   - `server/StyleCombat.land` now sends stage 3 for every non-line blow, hit or miss (dy = weight + 10 if all round).
2. **Combat mode on R (written; builds and verifies; the stances are still missing):**
   - `client/CombatMode.java`: on the client tick at HIGH priority (before the original's `HakiKeys$ForgeBus.tick`), consumes
     `HakiKeys.ARMAMENT` (R) clicks: R toggles combat mode (`C2SCombat`), Ctrl + R (`Screen.m_96637_`) sends the original's
     `HakiAction.ARMAMENT_TOGGLE` (what `HakiKeys.toggles` does). Not in Devil Fruit mode (R = Gear 5's transformation there) and not
     before the player has set sail. A HUD badge over the hotbar (`RegisterGuiOverlaysEvent.registerBelowAll`).
   - `network/C2SCombat.java` (message 5), protocol bumped to "2" in `PieceNetwork`.
   - `server/StyleServer`: `State.combat`, `combat(player, on)` (plays `style_<id>_stance` or `__clear__` via `FruitUtil.animate`,
     action-bar line), `press` refused while combat is off, the stance re-played when a move ends in combat mode.
   - `client/StyleInput.ready()` needs `CombatMode.on()`; M1 is now taken at blocks too in combat mode (leave combat mode to mine);
     the right click still leaves blocks to vanilla.
   - Lang: `key.hexhaki.armament` = "Combat mode (R) / Armament Haki (Ctrl + R)" (added to `RENAMED` in build.py). `VERSION = "2.1.2"`.

## To do, in order
1. Check the branch's latest "Build HexPiece" run ended green (it built locally before the push).
2. **Write the four stance loops** `style_black_leg_stance`, `style_six_powers_stance`, `style_fishman_karate_stance`, `style_brawler_stance`
   in `tools/style_anims.py`: ARMS ONLY (rightArm, leftArm pinned, so walking legs stay vanilla), `isLoop` true, ~40 ticks, a slight breath;
   poses: Black Leg hands behind the back (`BL_HANDS`), Six Powers left hand behind the back, Fishman Karate right fist at the hip and
   left palm forward, Iron Fist the boxing guard. `write()` pins every part: add an arms-only variant. Must pass the lint (no forearm
   through the head). Add a StyleTests check that the four exist. Regenerate (`python3 tools/style_anims.py`; it also rewrites
   `client/npc/StylePoses.java`: keep both in step).
3. Render and judge (the owner's rule): `python3 tools/style_gif.py` (style GIFs), and preview the particle shapes if you can; the Photon
   effects themselves cannot be rendered here, say so.
4. Release notes `.github/release-notes-2.1.2.md` (combat mode R / Ctrl + R for Armament, the Photon particles), update the README and
   this handoff, commit, push, check "Build HexPiece" and "Release HexPiece" end green and v2.1.2 is on the Releases page.

## Ask the owner after 2.1.2
- Is Ctrl + R for Armament Haki right, or should Armament go to another key?
- How the particles look in game (amounts, sizes, the water's blue, the Air Blade's speed).
- The open questions of 2.1.1 still stand (a pirate title screen? style numbers? a newer jar with the Imu fruit?).
