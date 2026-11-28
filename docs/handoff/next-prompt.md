Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there; no pull
requests).

Read CLAUDE.md, then docs/handoff/next-ai-3.8.1.md WHOLE (the owner's messages verbatim and every version since 3.8.1; the newest
sections at its end: 22 the owner's Itsumade spec, 29 their Itsumade complaints, 32-34 the islands and the parked clothes, 35-38 the
Itsumade's kit: 3.9.7 the passive, the Haki colours and the moves in its form; 3.9.8 X Gorosei Stare, C Enormous Presence, V Yokai
Blast, B Thy Death; 37 the owner on the pentagram, verbatim; 3.9.9 N Hellish Teleport; 40 4.0.0's fixes (N's key number!); 41 4.0.1's
rivers per island; 43 THE OWNER'S LIST ON THE ITSUMADE IN GAME, verbatim; 44 "focus only on the untransformed version" (the
form PARKED until their new jointed model); 45 4.0.2 (the human's half of 43)). Then docs/handoff/next-ai-3.7.6.md and the older
handoffs it names as needed.

THE STANDING RULE (the owner: "Whenever I give you a weapon from now on Ill name one of these categories, write this in every handoff"):
every weapon comes with a category, made with its category's animations: Katana (as the katanas are now), Saber (more aggressive than
the katanas; idle and draw as they are: Gryphon), Mace (right-handed, rests on the back like Yoru, slow draw, idle on the right
shoulder, reach, ragdolling hits, slow to cock, fast swing), Yoru (Yoru alone, the strongest blade ever), Gun (one-handed, badass pose,
one bullet a shot, reload one bullet into the barrel, the owner's bullet model, Haki on: pure black faster bullets through up to 10
blocks, relaxed lowered poses, spun on the hand, a running animation). Copy it into every handoff you write.

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move); with the Conqueror's Haki and Ryou on every move of every fruit throws the
Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's release's (3D tubes with
needle tips): a bolt between two points is ConquerorStorm.between (never wifi/strike strokes). Never add another lightning look,
never a ring the owner did not ask for. Fruit sounds are Minecraft's own (tools/vanilla_sounds.py), except explosions and Haki.

Do next, in this order: (1) check 4.0.2's runs (Build and Release on the branch) are green (3.9.7 to 4.0.1 were); (2) the owner's word
on 4.0.2 in game comes first (write it verbatim into the handoff, fix, render, release); (3) the owner's focus: "focus on the
new fruit and world gen" (the clothes are parked: docs/handoff/clothes-wip-3.9.7.patch). Only the Itsumade's untransformed moves until the
owner's new jointed model arrives (44); its form's points of 43 wait for it. THE LEOPARD AND THE IMU FRUIT ARE GONE FOR GOOD (CLAUDE.md).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself and NAME
EVERY DEFECT you see before calling it done (the owner: "How does this shit pass your tests and you have the balls to say that they
pass?"): tools/anim_preview.py for clips, tools/preview/FxPreview (ItsuArt: iball, ibeam, iring, iwave, iclouds, iburst, ipenta,
ipillar, iengulf), tools/archipelago_preview.py for the world (the rivers per island: WorldTests walks them), SparkPreview "<out> itsu" for its bolts, UiPreview "<dir> gates" for its menu; never key the head or unused limbs;
everything alpha-blended, never additive, and never a see-through glow over something drawn after it (it cuts it away: section 37's
lesson); versions roll over at 9 (4.0.3 next); a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff
and this prompt first, commit, push and stop without checking CI.
