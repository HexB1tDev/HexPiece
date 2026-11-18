Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.4.4.md (the
newest: the owner's in-game report after 2.4.4, five issues with where to look), then next-ai-2.3.6.md (2.3.6 to 2.4.4 in detail) and
next-ai-2.3.2.md (2.3.2 to 2.3.5, the sword animation solver and the owner's sword rule). 2.4.4 is released (v2.4.4); the next
version is 2.4.5 (`python3 tools/next_version.py --write`). Check the branch's last "Build HexPiece" run is green first.

Fix, in this order, the owner's report (their words in the handoff):
1. Gryphon (and any blade) in first person shows its saya in the hand while the sword is drawn (KatanaItemRenderer.held draws both).
2. The M1 swings: the arm must be fully stretched at the strike, the blade pointed forward far from the chest, the most range it can
   have (the handoff describes the owner's reference pose: a wide lunge, the sword arm straight out from the shoulder, the blade on its
   line). Re-solve the sword animations (tools/sword_anims.py, ~16 min on 4 cores with a long timeout) and keep build.py's pose check
   green.
3. Range by sword, Yoru the longest, without per-blade animation sets (the owner's rule: one set for every blade; never re-check per
   blade): by blade length / a reach factor per katana where the swept path becomes the hit.
4. The Conqueror's full-charge release lags a ton: find whether it is frames or ticks (ask the owner if unsure) and cut the cost
   (suspects in the handoff: fissure block changes, the storm's bolt meshes every frame, particles).
5. The release lightning "feels like it always goes to the same spot": trace the seed of ConquerorLightningPath.generate and give every
   release its own (server hit checks and client bolts must agree); spread the landings round the player.

Then release 2.4.5 with release notes in plain words, and ask the owner to test it. The owner tests in game at their office and said
they no longer need renders: do not send GIFs or renders unless asked (still check your own work numerically or with the preview tools).

Run on the "HexPiece" cloud environment. HexGhoul is at HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms (public: clone
read-only). House rules: versions roll over at 9, keep only 5 releases, never claim in-game testing, every sound from the web and free
to use (or the owner's own). The owner's device draws additive or culling-dependent geometry as solid boxes: use alpha-blended,
depth-tested geometry that is right without culling. Stop rule (CLAUDE.md): when the owner says so, or the moment the WEEKLY usage
reaches 98%, write the handoff and the next prompt FIRST, commit, push, and stop, without checking CI.
