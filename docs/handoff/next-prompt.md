Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.5.8.md (the
newest: 2.5.8's first person with an empty hand and the arm under Haki, an open question on "some swords"; 2.5.7's fix of the crash on joining any world; 2.5.6's first-person draw, held and sheathing, sword/DrawView + client/sword/HolsterHand; the blades' temper,
tools/katana_temper.py -> textures/tempered/, packed by build.py while the source hashes match), then next-ai-2.5.5.md (the white
screen fix, the free head, the holster view, the Haki slash's 30 s cooldown), next-ai-2.5.3.md (Ryou's fist aura, which the owner
calls "beautiful and perfect": keep it), next-ai-2.5.2.md and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES through
MobileGlues). 2.5.8 is released; the next version is 2.5.9 (python3 tools/next_version.py --write). Check the branch's last "Build
HexPiece" and "Release HexPiece" runs first.

Then act on the owner's report of 2.5.8 (empty-hand first person, the arm with Haki on, their answer on "some swords"), 2.5.7 and 2.5.6: the first-person draw and sheathing (tools/draw_preview.py renders the game's own frames;
judge it yourself, don't send renders unless asked) and the new temper on the blades (all but Yoru).

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). Rendering on
the owner's device: alpha-blended, depth-tested geometry that is right without culling, through the game's own RenderTypes; never a
framebuffer blit or post chain in the middle of the level (it broke the projection for everything after it). Stop rule: when the owner
says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit, push, and stop, without
checking CI.
