Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.3.2.md
(newest; it says what was pushed and whether its "Build HexPiece" run was green), then next-ai-2.3.2-wip.md, next-ai-2.3.1.md and the
older ones. The version in build.py is 2.3.2 and it is NOT released yet: `.github/release-notes-2.3.2.md` is drafted but must only be
committed once every sword animation passes the build's pose check (that publishes v2.3.2). First: `python3 build.py`, fix what fails,
get the branch's "Build HexPiece" run green.

Top priority, finish the owner's sword work: every sword animation (katana, `_gryphon`, `_yoru`) must pass `build.py check_sword_poses`
(tools/sword_anims.py clearance: natural arms, no arm or sword through the body, arms or head, no point in the floor). Its
SWORD_POSE_EXCEPTIONS lists what is left (each a known problem to fix, then delete from the list). The owner's words this session: "the
arm is twisted in a bunch of these, how does this pass", "the sword handle clipping through the shoulder?", "the blade edge should be
facing up", "the shoulder is grabbing the sword in the opposite way it's meant to" (Yoru's hilt is taken from above now). Render and judge
the FULL sheets of every Gryphon and Yoru move (`python3 tools/sword_anims.py --no-solve --preview DIR`, zoom on frames), GIFs too; the
owner sees the renders, so judge them yourself first. Then: clashes with impact frames (the owner's Armament and Ryou rules,
next-ai-2.3.0.md), Galaxy Fist, the Fishman Karate rework, Six Powers. Run on the "HexPiece" cloud environment. HexGhoul is at
HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms. House rules: versions roll over at 9, keep only 5 releases, judge every render
yourself (GIFs too), never claim in-game testing, every sound from the web and free to use. When the owner says so, or at about 90% of
the 5-hour usage, stop and write the handoff and the next prompt.
