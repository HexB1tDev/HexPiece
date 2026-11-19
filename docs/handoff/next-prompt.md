Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest). Read CLAUDE.md, then
docs/handoff/next-ai-2.6.2.md (the newest: 2.6.2 the katanas and Gryphon 1.25 times bigger,
Katana.SIZE, the animations re-solved by tools/sword_anims.py; 2.6.1's first-person draw and sheathing without flips, sword/DrawView with
SwordTests.drawSmooth; the right-click charge in first person; the Haki coat on the blade only, spreading, sword/BladeCoat; the
quieter Ryou sound; and 2.6.0's Conqueror sounds and stun), then next-ai-2.5.9.md (the first-person draw and sheathing: sword/DrawView +
client/sword/HolsterHand, tools/draw_preview.py; the blades' temper), next-ai-2.5.3.md (Ryou's fist aura, which the owner calls
"beautiful and perfect": keep it) and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES through MobileGlues). The next version
is 2.6.4 (2.6.3 released; check its runs first). Check the branch's last "Build HexPiece" and "Release HexPiece" runs first.

Then act on the owner's report of 2.6.2 (the size), 2.6.1 and 2.6.0 (render with tools/draw_preview.py, judge every frame yourself, fix and render
again).

House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only when GitHub's runs are green, never claim
in-game testing, every sound from the web and free to use (or the owner's own). Rendering on the owner's device: alpha-blended,
depth-tested geometry that is right without culling, through the game's own RenderTypes; never a framebuffer blit or post chain in the
middle of the level. Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next
prompt FIRST, commit, push, and stop, without checking CI.
