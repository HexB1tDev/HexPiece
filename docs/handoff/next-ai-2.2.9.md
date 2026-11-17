# HexPiece: handoff after 2.2.9 (the Swordsman's sounds, from the web)

Read CLAUDE.md first. This note is the newest; next-ai-2.2.8.md covers 2.2.7 (Gryphon and Yoru) and 2.2.8 (the slash's foam and ink),
next-ai-2.2.6.md the 3D inked lightning, needle tips, lightning off the body, the slash as light, the saya never black, the icon fix.
The work branch is `claude/nice-bell-gam7qf`. 2.2.9 is released; the next version is **2.3.0** (versions roll over at 9). Nothing
has ever been tested in game (only the owner can).

## The owner (quoted)
- After 2.2.8: "Continue and make sure to grab sounds from the web for the swords unsheath, swipe, etc, all the sounds must come from
  the web (be free for use)". Done in 2.2.9.
- During 2.2.9: "add to the to-do list that every move is clashable (combat style wise, and yes you can take the impacts from
  HexGhoul's latest version, however I want a impact frame whenever a clash happens.)". Open: item 1 below.

## Network: the "HexPiece" environment
The environment the 2.2.9 sessions used had disappeared by the second session (only "Default" was visible, and it answers 403 for
opengameart.org). A new personal environment "HexPiece" (env_01PmnEXrwDhxzij8M5q6P6bL) was created allowing `opengameart.org` only,
and the session moved onto it. Kenney and Freesound answer 403 to scripts: do not spoof a browser to get past that. OpenGameArt has
plenty of CC0 audio (water, impacts, whooshes) for the next requests.

## Released in 2.2.9
- **The clips** (`tools/sword_sounds.py`, re-run against OpenGameArt; 41 clips, all CC0 1.0, each credited in
  `src/main/resources/assets/hexhaki/sounds/piece/sword/SOURCES.txt` with its pack, author, licence and page): draw_slide1-3,
  draw_ring1-5, guard_click1-4, sheathe_slide1-2, swing1-13, shing1-5, clash1-5, whoosh1-2, rush1-2.
  - New in the tool: `cut` per clip: `"onset"` (from the first 5 ms window within 20 dB of the loudest, minus 10 ms: drops a noise
    floor or lead-in) and `"click"` (from the attack of the loudest transient: walks back from the peak while within 10 dB). The
    first cut of these clips had dead air before the hit: guard_click2 0.55 s (11 ticks), shing1 0.25 s, shing5 0.22 s, clash5
    0.20 s. Now every click, shing and clash peaks within 1 tick of its start. `shape` (fade in, fade out from): the air whoosh is a
    steady 5.25 s loop of swells 1.05 s apart, so pieces of it are shaped into single rushes (quarter-sine rise, cubic decay to the
    end). whoosh1/2 are 1.0 s (the Haki slash, pitched ~0.6 in sounds.json), rush1/2 0.36 s (the heavy swings).
  - Envelopes were plotted against a tick grid (50 ms) and judged.
- **Sound events** (`sounds.json`, merged into the original's by build.py; subtitles in en_us.json): `piece.sword.draw`, `draw_ring`,
  `guard_click`, `sheathe`, `swing`, `swing_heavy` (rush1/2 and low swishes), `shing`, `clash` (registered, not played yet: kept
  for the clashes, item 1), `slash` (the whooshes pitched down). `PieceSounds` registers them; `PieceSounds.play(user, beat)` /
  `play(user, name, volume, pitch)` plays at the user for everyone near (SoundSource.PLAYERS, pitch varied 5%).
- **The beats** (`sword/SwordSounds`, pure): `of(name)` per animation (the slow draw: guard click 4, slide 9, ring 15 (clears the
  mouth at 16); the sheathe: flick 2, slide 10, click 17 (= its sheath tick); the charge draw; the quick draw (slide 0, shing 2, rush
  3); the quick cut; the release (rush + shing at 1); the three draw cuts (slide at 0); Yoru's draw (click 7, deep slide 8 = its
  hand-over tick, sweeps 12 and 16, low shing 14), sheathe and charge draw). `of(anim, katana)` adds each cut's swing at its
  `from()` (heavy and finisher: `swing_heavy` + a quieter `shing`; a cut 4+ ticks long swings again halfway: Wheel, Pivot) and
  pitches everything for the blade (`pitch`: Yoru 0.78, Gryphon 1.06), except Yoru's own `_back` animations (pitched already).
- **`SwordSounds.Cursor`**: per sword, `advance(anim, start, now, katana)` returns the beats due: each once, on its tick, up to
  `CATCH_UP` (2) late if a tick was missed or the animation began after this tick's sounds; a new animation (or the same begun again)
  starts over; beats long past are never played.
- **Wired**: players in `SwordServer.playerTick` (`hear`, after `tick`, before `sync`); NPCs in `SeaFighter.swordBrain` (after
  `showSword`: they swung in silence before). The original's `fx.whoosh_*` at cuts, the release and the quick draw are gone (the beats
  cover them); the Haki slash launch plays `SLASH` at 0.8 + 0.6 x charge (an NPC's 0.8 + 0.5 x charge). Hits keep the original's
  `fx.impact_*`.
- **Tests** (`SwordTests.sounds`): every beat is a registered sound inside its animation's length, in order, sane volume and a pitch
  in 0.5..2; the draw slide comes no later than the draw tick; every cut swings at its window (heavy ones ring); the sheathe's last
  beat is the guard click at its sheath tick; each blade its own pitch; the cursor plays each beat exactly once on its tick (every
  tick and every other tick), never twice in a tick, not late when first seen halfway, again when drawn again; every event is in
  sounds.json with a subtitle, every referenced clip exists, every clip is played, credited (pack, author, CC0 1.0, OpenGameArt
  page), Ogg Vorbis, mono, 44.1 kHz.

## Open: the owner's requests, in order
1. **Every move is clashable, with an impact frame on each clash** (the owner: "every move is clashable (combat style wise, and yes
   you can take the impacts from HexGhoul's latest version, however I want a impact frame whenever a clash happens.)"). Every
   fighting-style move (the Swordsman's cuts and slashes, Iron Fist, Fishman Karate, Six Powers; player against player and against
   NPC fighters) meets an opposing move landing at the same moment: both are stopped and thrown back, and an impact frame plays.
   Start by reading HexGhoul's latest impacts and impact frames (HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms: attach it
   with add_repo, read only) and how a style move's hit window is known on the server (`StyleCombat`, `SwordServer.sweep`,
   `SwordMove.Cut`, `StyleMove`). `piece.sword.clash` is registered and waiting for this.
2. **Galaxy Fist becomes Iron Fist's** (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU
   haki. ONLY for iron fist."): next-ai-2.2.6.md item 2.
3. **Fishman Karate rework**: next-ai-2.2.6.md item 3 (Brick Fist, Water Bullet, Water Prison, water condensing out of the air). The
   owner was asked whether Water Prison's 5 s perfect stun should break on Conqueror's Haki or damage: no answer yet (default:
   breakable). Water sounds: OpenGameArt (CC0), as the sword's.
4. **Six Powers**: faster 3D air, Haki finger pistol shots (next-ai-2.2.6.md item 4).

## Not verified (only the owner can test in game)
- The sounds' loudness against the original's sounds and each other, their timing in play, the subtitles.
- 2.2.6 to 2.2.8 in game (see their handoffs).
