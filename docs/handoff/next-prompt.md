Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.5.3.md (the
newest: Ryou's fist aura as geometry, the release's lightning under Ryou fixed, the skin readback checked; the Fishman Karate white
screen still open), then next-ai-2.5.2.md (the owner's target picture docs/handoff/ryou-fist-target.jpg and what waits on the owner)
and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES through MobileGlues). 2.5.3 is released; the next version is 2.5.4
(python3 tools/next_version.py --write). Check the branch's last "Build HexPiece" and "Release HexPiece" runs first.

Then act on the owner's report of 2.5.3: the aura's look against their picture (tools/ryou_preview.py renders it with the game's own
code; judge it yourself, don't send renders unless asked), whether the release's lightning shows with Ryou on, whether the skin stays
at world start, and their answers about the Fishman Karate white screen (where they select it, whether it stays white, Esc, other
styles, a screenshot). The handoff lists where was already searched and the next idea.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). Rendering on
the owner's device: alpha-blended, depth-tested geometry that is right without culling, through the game's own RenderTypes; never a
framebuffer blit or post chain in the middle of the level (it broke the projection for everything after it). Stop rule: when the owner
says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit, push, and stop, without
checking CI.
