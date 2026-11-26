Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there; no pull
requests).

Read CLAUDE.md, then docs/handoff/next-ai-3.8.1.md WHOLE (the owner's two messages of the last session verbatim; 3.8.1 the full
Conqueror's release's lag, 3.8.2 the Conqueror's Haki clash with its TAP M1 bar, 3.8.3 the archipelago world generation, 3.8.4 the
clashes as HexGhoul's; section 7 what to do next and THE STANDING RULE of the weapon categories). Then docs/handoff/next-ai-3.7.6.md
(where things stood at 3.8.0, the Leopard, the standing rules of the days before), and the older handoffs it names as needed.

THE STANDING RULE (the owner: "Whenever I give you a weapon from now on Ill name one of these categories, write this in every handoff"):
every weapon comes with a category, made with its category's animations: Katana (as the katanas are now), Saber (more aggressive than
the katanas; idle and draw as they are: Gryphon), Mace (right-handed, rests on the back like Yoru, slow draw, idle on the right
shoulder, reach, ragdolling hits, slow to cock, fast swing), Yoru (Yoru alone, the strongest blade ever), Gun (one-handed, badass pose,
one bullet a shot, reload one bullet into the barrel, the owner's bullet model, Haki on: pure black faster bullets through up to 10
blocks, relaxed lowered poses, spun on the hand, a running animation). Copy it into every handoff you write.

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's (3D tubes with needle tips). Never add another lightning look.

Do next, in this order: (1) check 3.8.4's runs (Build and Release on the branch) are green; (2) the owner's word on 3.8.1 to 3.8.4 in
game comes first (write it verbatim into the handoff, fix, render, release; 3.8.3 needs a new world); (3) the weapon reworks (forced
third person for a drawn blade with a camera for the swings, the categories, the Mace and the Gun from input/newmodels/NewModels.zip,
the clothes replacements; ask for the bullet model if it has not come); (4) the Leopard's other moves.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself; never key the
head or unused limbs (tools/anim lint must pass); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9 (3.8.5 next); a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first,
commit, push and stop without checking CI. The world generation's preview and data: tools/archipelago_preview.py, tools/archipelago_data.py
(vanilla 1.20.1 data from misode/mcmeta 1.20.1-data into the scratchpad, never committed).
