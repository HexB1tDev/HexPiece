Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.5.1.md (the
newest: 2.5.1 released: the Conqueror's charge and release animations by alignment, upper body only (good: a hand over the face then a
calm stare; evil: limp, an unnatural head tilt, twitches, a laugh); a stunned player's own first-person view falls with them), then
next-ai-2.5.0.md (the evil half-health damage), next-ai-2.4.9.md (the release by alignment, the 1-block slash) and next-ai-2.4.7.md.
The next version is 2.5.2 (python3 tools/next_version.py --write). Check the branch's last "Build HexPiece" and "Release HexPiece"
runs first.

Ask the owner how 2.4.7-2.5.1 went in game and do what they report. If they still see the full release lag, find out whether it is
FPS or the server, then see next-ai-2.5.1.md "Not done" 1.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
