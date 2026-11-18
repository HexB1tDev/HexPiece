Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.5.2.md (the
newest: two bugs open with detailed instructions), then next-ai-2.5.1.md (the owner's device: Android, OpenGL ES through MobileGlues,
and what that means for rendering). 2.5.2 is released and green; the next version is 2.5.3 (python3 tools/next_version.py --write).
Check the branch's last "Build HexPiece" and "Release HexPiece" runs first.

1. Ryou's fist glow still flickers on and off, and the owner sent the target look: look at docs/handoff/ryou-fist-target.jpg first
   ("this is the fucking target look can you not do this?"). Replace the PostChain/framebuffer glow (client/haki/RyouGlow) with an
   aura drawn as geometry in the hand layer (RyouHands.glow): rounded nested shells hugging the fist and forearm, bent with the elbow,
   bright at the silhouette's edge (facing the eye: nearly clear), a hot rim, the Ryou colour, a soft wavy flame-like outer haze
   flowing back along the arm, alpha-blended (never additive), depth-tested, right without culling; glossy black knuckles (highlight
   pixels in HandCoat); sparks. The handoff has the steps. Render it yourself and compare with the picture until it matches.
2. "sometimes the player's skin vanishes when starting a world": HakiSkin reads the skin back from its GL texture; at world start
   (or through MobileGlues' emulated readback) that can come back blank and the blank copy is cached: validate the readback, retry,
   re-check. The handoff has the steps.

Release 2.5.3 with plain release notes and ask the owner to test (and the items still waiting from 2.4.7 - 2.5.2, listed in the
handoff).

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
