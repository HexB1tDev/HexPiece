Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest). Read CLAUDE.md, then
docs/handoff/next-ai-2.7.0.md (the newest: the Uo Uo no Mi with the owner's dragon model: Z transforms (the model's own takes, grown
out of the player), flies as creative flight, transforms back; the left hand under Haki fixed), then next-ai-2.6.2.md, next-ai-2.5.9.md,
next-ai-2.5.3.md (Ryou's fist aura: "beautiful and perfect": keep it) and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES
through MobileGlues). The next version is 2.7.2 (2.7.1 pushed: the dragon's random colours and the owner's key layout; check its "Build HexPiece" and "Release HexPiece" runs first).

Then build the owner's Uo Uo no Mi moves (verbatim in next-ai-2.7.0.md: human X Air Pressure, C Blast Breath, V Wind Force, B Dragon
Embers; dragon M1 held fire breath, X charged Blast Breath, C Gigantic Bite; fire from coloured cloud particles
and smoke, Photon only for sparks; every move in the fruit ability HUD and the Captain's Log), then the lag and size pass the owner
asked for in session 10 (not started). Render every animation and effect you change (tools/dragon_gif.py, tools/preview/DragonPreview,
tools/anim_preview.py), judge every frame yourself, fix and render again.

House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only when GitHub's runs are green, never claim
in-game testing, every sound from the web and free to use (or the owner's own, or the original mod's). The owner's dragon model is
used exactly as sent, scaled uniformly (never reshaped). Rendering on the owner's device: alpha-blended, depth-tested geometry that is
right without culling, through the game's own RenderTypes; never a framebuffer blit or post chain in the middle of the level. Stop
rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit, push,
and stop, without checking CI.
