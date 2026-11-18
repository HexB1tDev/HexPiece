Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.4.5.md (the
newest: what 2.4.5 changed for the owner's 2.4.4 report: the first-person blade, the M1 lunge, range by sword, the Conqueror's release
lag and lightning landings), then next-ai-2.4.4.md (the owner's report and reference pose), next-ai-2.3.6.md and next-ai-2.3.2.md (the
sword solver and the owner's rule: one animation set for every blade). 2.4.5 is released (v2.4.5); the next version is 2.4.6
(`python3 tools/next_version.py --write`). Check the branch's last "Build HexPiece" run is green first.

Ask the owner first how 2.4.5 went in game (they test at their office): the M1 lunge (third and first person), the blade with no
sheath in first person, each sword's range (Yoru the longest), the full release's lag (if it still lags: FPS or the server freezing?)
and where its lightning strikes. Fix what they report, in their order.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
