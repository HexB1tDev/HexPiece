# HexPiece: handoff, 2.2.9 in progress (not released)

Read CLAUDE.md first. This note is the newest; next-ai-2.2.8.md covers 2.2.7 (Gryphon and Yoru) and 2.2.8 (the slash's foam and ink),
next-ai-2.2.6.md the 3D inked lightning, needle tips, lightning off the body, the slash as light, the saya never black, the icon fix.
The work branch is `claude/nice-bell-gam7qf`. `build.py` says 2.2.9 but there are NO release notes for it yet: 2.2.9 is unreleased work
in progress (the branch pre-release `build-claude-nice-bell-gam7qf` carries it); v2.2.8 is the latest release. Finish 2.2.9, then add
`.github/release-notes-2.2.9.md`. Nothing has ever been tested in game (only the owner can).

## The owner, this session (after 2.2.8, quoted)
- "Continue and make sure to grab sounds from the web for the swords unsheath, swipe, etc, all the sounds must come from the web (be free
  for use)". Then: "Enough, push, handoff and prompt now" (the session stopped here).

## Done for 2.2.9 (in the branch, built and tested, not yet audible)
- **Network**: the session's Default environment blocked every sound site; a personal environment "HexPiece" (env_01VDpZP1V4zScExADvRmie8m)
  was created for this repository allowing kenney.nl, opengameart.org, freesound.org, cdn.freesound.org. Only OpenGameArt answers (Kenney
  and Freesound answer 403 to scripts: do not spoof a browser to get past that). OpenGameArt has everything needed, all CC0.
- **The clips** (`tools/sword_sounds.py`: downloads the CC0 packs from OpenGameArt, trims, mono 44.1 kHz, -1 dBFS peak, 15 ms fade,
  Ogg Vorbis q5; writes `src/main/resources/assets/hexhaki/sounds/piece/sword/*.ogg` (37 clips, 416 KB) and `SOURCES.txt` naming each
  clip's pack, author, licence and page): draw_slide1-3 (Vehicle's knife unsheathes), draw_ring1-5 (artisticdude's RPG Sound Pack
  sword-unsheathe), guard_click1-2 (Vehicle's seax sheathe clicks), sheathe_slide1-2 (Vehicle's knife sheathes), swing1-13
  (artisticdude's Swishes), shing1-5 (StarNinjas' sword attacks), clash1-5 (StarNinjas' clashes), whoosh1-2 (pyranostudios' air whoosh,
  two rushes of the loop). All CC0 1.0.
- **Sound events** (`src/main/resources/assets/hexhaki/sounds.json`, merged into the original's by build.py; subtitles in en_us.json):
  `piece.sword.draw`, `draw_ring`, `guard_click`, `sheathe`, `swing`, `swing_heavy` (whooshes and low-pitched swishes), `shing`,
  `clash`, `slash` (the whooshes pitched down). Registered by `PieceSounds` (DeferredRegister on SOUND_EVENTS, `HexHaki.id(name)`;
  `PieceSounds.register(bus)` in `HexPiece.init`; stub `ForgeRegistries.SOUND_EVENTS` added, the original makes the same calls).
  `PieceSounds.play(user, name, volume, pitch)` plays at the user for everyone near (SoundSource.PLAYERS, pitch varied 5%).
- **The beat table** (`sword/SwordSounds`, pure): per animation name (SwordAnims.name(anim, katana)) the beats (tick, sound, volume,
  pitch): the slow draw (guard click 4, slide 9, ring 15), the sheathe (flick 2, slide 10, click 17), the charge draw, the quick draw
  (slide + shing), the quick cut, Yoru's draw (hand on hilt 7, deep slide 8, heavy sweeps 12 and 16, low shing 14), Yoru's sheathe and
  charge draw. `swing(heavy)`, `pitch(katana)` (Yoru 0.78, Gryphon 1.06).

## Next for 2.2.9 (not done)
1. **Play them** (server): in `SwordServer.tick`, right after `legend(...)` and before the `busy` return, play the beats of
   `SwordAnims.name(anim(state), state.katana)` whose tick equals `now - animStart(state)` (via `PieceSounds.play`). Cuts: replace the
   `StyleCombat.sound(player, ... "fx.whoosh_heavy" : "fx.whoosh_light")` at `cut.from()` in `tickMove` with
   `SwordSounds.swing(heavy)` at `SwordSounds.pitch(katana)` (and a quieter `SHING` on heavy cuts); the release (line ~663, ~670) and
   the quick draw (~701) with `SLASH` / `SHING`; a quiet `CLASH` (0.45, pitch 0.9..1.1) in `strike` beside the impact. NPC swordsmen
   (`SeaFighter.swordStep`/`showSword`: `swordAnim()`, `swordAnimStart()`): the same beats and cut swings (they play no swing today).
2. A test in SwordTests: every beat's sound is in `SwordSounds.ALL`, every `ALL` name is in sounds.json with files that exist, every
   file in the folder is credited in SOURCES.txt.
3. Release notes 2.2.9 (say every sound is CC0 from OpenGameArt and credited in SOURCES.txt), push, both workflows green.

## Then the owner's open requests, in order
1. **Galaxy Fist becomes Iron Fist's** (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU
   haki. ONLY for iron fist."): next-ai-2.2.6.md item 2.
2. **Fishman Karate rework**: next-ai-2.2.6.md item 3 (Brick Fist, Water Bullet, Water Prison, water condensing out of the air). The
   owner was asked whether Water Prison's 5 s perfect stun should break on Conqueror's Haki or damage: no answer yet (default: breakable).
3. **Six Powers**: faster 3D air, Haki finger pistol shots (next-ai-2.2.6.md item 4).

## Not verified (only the owner can test in game)
- 2.2.6 to 2.2.8 in game (see their handoffs); the new sounds' loudness and timing once they play.
