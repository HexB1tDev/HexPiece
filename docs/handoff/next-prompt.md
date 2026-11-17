Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.3.2.md: its
first section ("STATE WHEN THE OWNER SAID STOP", session 3) says where things are; then its "Session 3" parts 4 to 1, then
next-ai-2.3.0.md (the clash rules in the owner's words). 2.3.5 is released (v2.3.5); the next version is 2.3.6
(`python3 tools/next_version.py --write`). The last push (the handoff) was made without checking CI: check the branch's "Build
HexPiece" run is green first.

The owner had not tested 2.3.4 and 2.3.5 in game when they stopped: ask how the Haki hands (the skin itself blackened, the glowing
fists) and the bigger sword swings and first-person swings went before starting new work, and fix what they report first. Still
open from their earlier test: the Fishman Karate white screen (ask when it happens and for a full screenshot; see the handoff).

The owner's sword rule (in their words): "skip the checks on every single katana ... Just apply the same fix to every single one.
Just copy. They're all the same." Every blade plays the katanas' one animation set; only Yoru's draw, sheathe and charge draw off its
back are its own. Never bring per-blade sets back; never re-check per blade.

Next, in the owner's order: clashes with impact frames (next-ai-2.3.0.md item 1: every move clashable, an impact frame on every
clash, Armament needed to clash a blade, Ryou meeting Ryou bursts both colours in a sphere; the pure rules are done in
`clash/ClashRules` + `ClashTests`, the world side is planned in the handoff), then Galaxy Fist for Iron Fist, the Fishman Karate
rework, Six Powers (next-ai-2.2.6.md). Run on the "HexPiece" cloud environment. HexGhoul is at HexB1tDev/HexGhoul, branch
claude/eager-ritchie-xni1ms (public: clone read-only). House rules: versions roll over at 9, keep only 5 releases, judge every
render yourself (GIFs too; `tools/sword_gif.py` has a first-person view), never claim in-game testing, every sound from the web and
free to use. The owner's device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry
that is right without culling. Stop rule (CLAUDE.md): when the owner says so, or the moment the WEEKLY usage reaches 98%, write the
handoff and the next prompt FIRST, commit, push, and stop, without checking CI.
