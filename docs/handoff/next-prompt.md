Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest). Read CLAUDE.md, then
docs/handoff/next-ai-2.6.0.md (the newest: 2.6.0's Conqueror sounds, 35 % quieter through server/ConquerorSounds, the evil voice
line voice-only and louder, the CC0 explosion on letting go of G, tools/conqueror_explosion.py; the stun by hold in
haki/ConquerorStun: tap 5 s, hold 10-20 s, good 30 s), then next-ai-2.5.9.md (the first-person draw and sheathing: sword/DrawView +
client/sword/HolsterHand, tools/draw_preview.py; the blades' temper), next-ai-2.5.3.md (Ryou's fist aura, which the owner calls
"beautiful and perfect": keep it) and next-ai-2.5.1.md (the owner's device: Android, OpenGL ES through MobileGlues). The next version
is 2.6.1 (python3 tools/next_version.py --write). Check the branch's last "Build HexPiece" and "Release HexPiece" runs first.

Then the owner's item 3: "focus on bettering the first person Sheath / Unsheath animations so it's as smooth as possible without buggy
frames" (render with tools/draw_preview.py, judge every frame yourself, fix and render again), and act on their report of 2.6.0.

House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only when GitHub's runs are green, never claim
in-game testing, every sound from the web and free to use (or the owner's own). Rendering on the owner's device: alpha-blended,
depth-tested geometry that is right without culling, through the game's own RenderTypes; never a framebuffer blit or post chain in the
middle of the level. Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next
prompt FIRST, commit, push, and stop, without checking CI.
