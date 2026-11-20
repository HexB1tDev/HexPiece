Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest). Read CLAUDE.md, then
docs/handoff/next-ai-2.7.0.md (the newest: the Uo Uo no Mi with the owner's dragon model: Z transforms (the model's own takes, grown
out of the player), flies as creative flight, transforms back; the left hand under Haki fixed), then next-ai-2.6.2.md, next-ai-2.5.9.md,
next-ai-2.5.3.md (Ryou's fist aura: "beautiful and perfect": keep it) and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES
through MobileGlues). The next version is 2.7.7 (2.7.6 pushed: every Uo attack rebuilt as 3D cel-banded fire out of the roaring mouth, harder hits; 2.7.5 the fruit HUD's "no slot" keys fixed, the dragon's colour rolled once per fruit; 2.7.4 the jar 24 MB, the swords drawn faster; 2.7.3 the dragon's locks and the Transformed effect; 2.7.2 the seven moves; 2.7.1 the colours and the key layout; check the runs first).

First ask the owner how 2.7.6's 3D fire looks and runs in game (next-ai-2.7.0.md "Done (2.7.6)" and "Next (2.7.7)"; the anime
references are docs/handoff/uo-fire-target-1.jpg and -2.jpg) (the dragon camera stays as it is: the owner's choice). Then ask how the dragon, its moves and colours look and feel in game, and where they still see lag (next-ai-2.7.0.md
"Done (2.7.4)" lists what the first pass did and did not do). Render every animation and effect you change (tools/dragon_gif.py, tools/preview/DragonPreview,
tools/anim_preview.py), judge every frame yourself, fix and render again.

House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only when GitHub's runs are green, never claim
in-game testing, every sound from the web and free to use (or the owner's own, or the original mod's). The owner's dragon model is
used exactly as sent, scaled uniformly (never reshaped). Rendering on the owner's device: alpha-blended, depth-tested geometry that is
right without culling, through the game's own RenderTypes; never a framebuffer blit or post chain in the middle of the level. Stop
rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit, push,
and stop, without checking CI.
