Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there; no pull
requests).

Read CLAUDE.md, then docs/handoff/next-ai-3.7.6.md WHOLE: its sections 1 to 4 (written at 3.7.6), then sections 5 and 6 at its end
(the last session: 3.7.7 the Phoenix's transformed M1 flamethrower, 3.7.8 Zoan forms locked out of Style, 3.7.9 the new Neko Neko no Mi,
Model: Leopard's first part: the owner's model baked 5 blocks tall, Z as a ball of black flames with evil red eyes, the base animations
from the model's own takes, the black-flame mane, tail and ribbon, the faces, Six Styles (M1) in both forms, the icon; the owner's
words verbatim; every class, constant, preview and test; what is next). Then docs/handoff/next-ai-3.6.3.md sections AH to AN, the rest of
next-ai-3.6.3.md as needed, next-ai-3.6.0.md sections I to N, and POINT ONE and sections 0 to 7 of docs/handoff/next-ai-3.2.2.md (every
standing rule).

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's: thin, zig-zagging out of the player every way, in the player's Haki colour, drawn as round lit 3D tubes with needle-sharp
tips (next-ai-3.6.3.md I). Never add another lightning look.

The owner's standing rules (next-ai-3.7.6.md section 2 and the last session): NEVER scale a model or a body in or out to make it appear
or go: things burn into being and burn away. No 360 spins of a transformed body. No tongue particles in moves: the house's 3D fire.
"I dont need gifs": render to judge your own work, but no GIFs or GIF pages unless asked. Keep every frame under the 16k-quad phone
budget, a transformed model's own faces included (the phoenix 11.5k, the leopard about 11.7k). The Leopard's flames are BLACK ("black
flames not purple"): black to charcoal, no purple, no red (red only in the change's ball's eyes and Rokuogan's rim). The owner is short on
usage: do what they ask first and keep each step small and released.

The owner's standing order on the Mochi's feel: the mochi is FLUID AND ALIVE, never stiff or still (next-ai-3.6.3.md N 19), and "It is
very important that you do not give up until the moves look exactly like the reference GIFs that you have."

Do next, in this order: (1) check the last push's Build run (3.8.0, the Ito Ito no Mi's Sky Road keeping its swing and speed, was green: Build
#266, Release #171; next-ai-3.7.6.md section 7; the owner: "Peak gj! see ya tomorrow twin"); (2) the owner's word on 3.8.0's Sky Road and on 3.7.7, 3.7.8 and 3.7.9 in game comes first
(write it verbatim into the handoff, fix, render, release); (3) the Leopard's
other moves, each in both forms, after reading their GIFs frame by frame (next-ai-3.7.6.md 3.2's table and section 6): X Finger Pistol
Barrage, C Sonic Kick, V Infinity Drive, B Unrelenting Assault, and the transformed-only Heavens Demise; release each step the owner
should test as its own version (3.8.1 next; versions roll over at 9).

The cloud environment must be the personal "HexPiece" (it allows static.wikia.nocookie.net for the reference GIFs; download them into the
scratchpad, never commit them; they are animated WebP at 100 ms a frame). A session on Default: get_environment, then
switch_environment to HexPiece (network_only, its switch_statement copied exactly). Blender's module for the bakes: python3 -m venv
/tmp/bpy && /tmp/bpy/bin/pip install bpy (tools/leopard_bake.py patches its FBX importer's one line itself).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass); everything alpha-blended, never additive; every sound
a CC0 web recording; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first,
commit, push and stop without checking CI.
