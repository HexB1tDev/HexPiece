Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.4.7.md (the
newest: 2.4.7 released: the old HUD gone, Ryou's hands black in the skin with the game's glow round the fists, new Conqueror charge and
release animations, the full release's old sounds removed and the evil roar louder), then next-ai-2.4.6.md and next-ai-2.4.5.md. The
next version is 2.4.8 (python3 tools/next_version.py --write). Check the branch's last "Build HexPiece" and "Release HexPiece" runs first.

Ask the owner how 2.4.7 went in game (the three animations, the black hands spreading and their glow, the HUD, the release sounds:
should neutral keep the old ones, should the explosion come at the moment of release) and what they still owe on 2.4.5/2.4.6 (the
lunge, ranges, release lag, the alignment bar, the female voice). Then do what they report.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
