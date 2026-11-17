Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.3.2.md
(its first section, "Session 3, part 2: 2.3.3", is the newest), then next-ai-2.3.0.md (the clash rules in the owner's words). 2.3.3
is released (the owner's in-game bug fixes); the next version is 2.3.4 (`python3 tools/next_version.py --write`). Still open from the
owner's test: their skin and the Fishman Karate white screen (see the handoff for what to ask). Check the last push's "Build HexPiece" and
"Release HexPiece" runs are green first.

The owner's sword rule (this session, in their words): "skip the checks on every single katana ... Just apply the same fix to every
single one. Just copy. They're all the same." Every blade plays the katanas' one animation set; only Yoru's draw, sheathe and charge
draw off its back are its own. Never bring per-blade sets back; never re-check per blade.

Next, in the owner's order: clashes with impact frames (next-ai-2.3.0.md item 1: every move clashable, an impact frame on every
clash, Armament needed to clash a blade, Ryou meeting Ryou bursts both colours in a sphere); the pure rules are done
(`clash/ClashRules`, `ClashTests`), the plan for the world side and the impact frame is in the handoff. Then Galaxy Fist for Iron
Fist, the Fishman Karate rework, Six Powers (next-ai-2.2.6.md). Run on the "HexPiece" cloud environment. HexGhoul is at
HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms (public: clone read-only). House rules: versions roll over at 9, keep only 5
releases, judge every render yourself (GIFs too), never claim in-game testing, every sound from the web and free to use. Stop rule
(CLAUDE.md): when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
