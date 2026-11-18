Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.4.7.md (the
newest: 2.4.7 IN PROGRESS, stopped mid-session: the HUD fix and the Ryou skin/glow are done and pushed but unreleased; the Conqueror
charge/release animations are not done), then next-ai-2.4.6.md and next-ai-2.4.5.md. build.py VERSION is already 2.4.7 (unreleased):
do not bump it again until 2.4.7 is released. The last push was made without a CI check (the stop rule): check the branch's last
"Build HexPiece" run first and fix it if red.

Then finish the owner's item 3: "I want a badass animation for the Conqueror Haki charge / release. the current one is dog shit."
(conqueror_gear5_charge while holding G, supreme_conqueror_release for a full charge, conqueror_gear5_release for a partial one: plan in
the handoff). Author them in a generator, judge them yourself with tools/anim_preview.py and anim.lint, then release 2.4.7 with plain
release notes (the old HUD gone, Ryou's hands black in the skin spreading pixel by pixel with the game's own glow round the fists, the
new Conqueror animations) and ask the owner to test.

Run on the "HexPiece" cloud environment. House rules (CLAUDE.md): versions roll over at 9, keep only 5 releases, a push is done only
when GitHub's runs are green, never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. The owner does not want renders or GIFs unless they ask (still check your own work numerically or with the preview tools).
Stop rule: when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next prompt FIRST, commit,
push, and stop, without checking CI.
