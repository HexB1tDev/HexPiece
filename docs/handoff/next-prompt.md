Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.4.6.md (the
newest: good and evil, the alignment bar, the evil Conqueror's sounds from the owner's clip), then next-ai-2.4.5.md (the owner's 2.4.4
report fixed: the first-person blade, the M1 lunge, range by sword, the release's lag and lightning landings), next-ai-2.4.4.md (that
report and the reference pose), next-ai-2.3.6.md and next-ai-2.3.2.md (the sword solver and the owner's rule: one animation set for every
blade). 2.4.6 is released (v2.4.6); the next version is 2.4.7 (`python3 tools/next_version.py --write`). Check the branch's last "Build
HexPiece" run is green first.

Ask the owner first how 2.4.5 and 2.4.6 went in game (they test at their office): the M1 lunge, the blade with no sheath in first person,
each sword's range, the full release's lag (if it still lags: FPS or the server freezing?) and where its lightning strikes; the alignment
bar, `/hexpiece alignment`, the evil voice line and roar (loud enough, the female version), where evil and good should begin (now -25 and
+25). Good's release sound comes when they send it; killing humans makes a player evil once humans exist. Fix what they report, in their
order.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
