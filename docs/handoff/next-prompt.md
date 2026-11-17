Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.3.2.md: its
first section ("STATE WHEN THE OWNER SAID STOP", session 2) says exactly where things are; then its "Session 2" notes, then
next-ai-2.3.2-wip.md, next-ai-2.3.1.md, next-ai-2.3.0.md. The version in build.py is 2.3.2 and it is NOT released: the notes are
drafted at docs/handoff/release-notes-2.3.2-draft.md; move them to .github/release-notes-2.3.2.md only once every sword animation
passes the build's pose check (that publishes v2.3.2). The last push was work in progress and its CI was not checked.

First: the full solve (`python3 tools/sword_anims.py`, 40-60 min: run it in the background with a 2-hour limit), then
`python3 tools/sword_check.py` (the per-move summary), fix what is left, then `python3 build.py` (Yoru's new glide in
client/sword/SwordLayer has not been compiled yet), and get the branch's "Build HexPiece" run green.

Top priority, finish the owner's sword work: every sword animation (katana, `_gryphon`, `_yoru`) must pass `build.py
check_sword_poses`; SWORD_POSE_EXCEPTIONS must stay empty. The owner's words: "the arm is twisted in a bunch of these, how does this
pass", "the sword handle clipping through the shoulder?", "the blade edge should be facing up", "the shoulder is grabbing the sword in
the opposite way it's meant to". All regular katanas share one animation set (only Gryphon and Yoru have their own): keep it so.
Yoru's carry on the back was moved this session (hilt out over the right shoulder): render it on the back and judge the ropes. Render
and judge the FULL sheets of every Gryphon and Yoru move (`python3 tools/sword_anims.py --no-solve --preview DIR`,
`tools/sword_zoom.py` for frames), GIFs too; the owner sees the renders, so judge them yourself first. Then: clashes with impact
frames (the owner's Armament and Ryou rules, next-ai-2.3.0.md), Galaxy Fist, the Fishman Karate rework, Six Powers. Run on the
"HexPiece" cloud environment. HexGhoul is at HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms. House rules: versions roll over
at 9, keep only 5 releases, judge every render yourself (GIFs too), never claim in-game testing, every sound from the web and free to
use. Stop rule (CLAUDE.md): when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt
FIRST, commit, push, and stop, without checking CI.
