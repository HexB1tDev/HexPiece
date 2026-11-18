Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf). Read CLAUDE.md, then docs/handoff/next-ai-2.3.6.md (the
newest: clashes in 2.3.6, the Galaxy Fist made Iron Fist's in 2.3.7, Fishman's Water Bullet in 2.3.8, Brick Fist in 2.3.9 and Water Prison in 2.4.0, Six Powers' 3D air in 2.4.1, Haki slashes clashing in 2.4.2, the NPCs' gestures by rank in 2.4.3), then next-ai-2.3.2.md (2.3.2 to 2.3.5) and next-ai-2.3.0.md
(the clash rules in the owner's words). 2.4.3 is released (v2.4.3); the next version is 2.4.4 (`python3 tools/next_version.py --write`). Check the branch's
"Build HexPiece" run of the last push is green first.

The owner had not tested 2.3.4 or 2.3.5 in game at the start of session 4, and 2.3.6 (clashes) and 2.3.7 (Galaxy Fist) are new: ask
how the Haki hands, the bigger and first-person sword swings, the clashes (too often or too rarely? the impact frame on their device?
the recoils?) and the Galaxy Fist (Iron Fist with Ryou up) and Fishman's Water Bullet, Brick Fist (the rag dolls), Water Prison, Six Powers' 3D air and Haki finger shots, and the NPCs' rank gestures (captains swagger, officers point, recruits fidget) went before starting new work, and fix what they report first. Still open from their earlier test: the Fishman Karate white screen (ask
when it happens and for a full screenshot).

The owner's sword rule (in their words): "skip the checks on every single katana ... Just apply the same fix to every single one.
Just copy. They're all the same." Every blade plays the katanas' one animation set (sword_clash included); only Yoru's draw, sheathe
and charge draw off its back are its own. Never bring per-blade sets back; never re-check per blade.

Every request on the owner's lists is done (the last, Six Powers' air, in 2.4.1; the handoffs' own open items too); after their reports, ask what is next;
Run on the "HexPiece" cloud environment. HexGhoul is at
HexB1tDev/HexGhoul, branch claude/eager-ritchie-xni1ms (public: clone read-only). House rules: versions roll over at 9, keep only 5
releases, judge every render yourself (GIFs too; `tools/sword_gif.py` has a first-person view, `tools/preview/ImpactPreview.java`
renders the impact frame), never claim in-game testing, every sound from the web and free to use (or the owner's own). The owner's
device draws additive or culling-dependent geometry as solid boxes: use alpha-blended, depth-tested geometry that is right without
culling. Stop rule (CLAUDE.md): when the owner says so, or the moment the WEEKLY usage reaches 98%, write the handoff and the next
prompt FIRST, commit, push, and stop, without checking CI.
