Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there: the owner's
answer this chat).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (newest: sections F to O: the owner's requests verbatim and answers, the
base GIFs read frame by frame (G), the 3D lightning (I), the HUD (J), the Mochi moves rebuilt (K), Buzz Cut Mochi (L, M), the owner's
"use my imapct frame" (N) and the chat that finished Buzz Cut and released 3.6.3 (O)), then docs/handoff/next-ai-3.6.0.md sections I to
N, then POINT ONE and sections 0 to 7 of docs/handoff/next-ai-3.2.2.md (every standing rule).

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's (G): thin, zig-zagging out of the player every way, in the player's Haki colour, and (this chat, the owner: "make the
lightning 3d once it for all, all of it", "the tip of the lightning has to be sharp") drawn as round lit 3D tubes everywhere with
needle-sharp tips, never a flat strip or the original's flat beams (next-ai-3.6.3.md I). Never add another lightning look.

The owner's newest standing word on the Mochi fruit: "It is very important that you do not give up until the moves look exactly like
the reference GIFs that you have. If they don't look like the reference GIFs exactly, to very minute detail, I don't want them."

Where things stand: 3.6.3 is released (check its Build and Release runs on the branch first; fix them if red): the 3D lightning
with sharp tips, the HUD hidden while powers are off, and the Mochi fruit's base kit after the reference GIFs (Mochi Punch, Grilled
Mochi, Mochi Muddle, Lotus Mochi Combo, Chestnut Mochi, Mochi Anemone, Buzz Cut Mochi with the owner's impact frame); every old Mochi
move, its art and its clips removed.

Do next (next-ai-3.6.3.md O, "Next"): 3.6.4, the awakened DoughV2 set on M (the seven moves promised in F 7): read each DoughV2 GIF
frame by frame first (section D), write what it shows into the handoff, then build each move on its beats, render it side by side with
its GIF, fix and render again until it matches; release 3.6.4 (next_version, notes, handoff, push, both runs green, send previews).

The cloud environment must be the personal "HexPiece" (it now also allows official-fruit-battlegrounds.fandom.com and
static.wikia.nocookie.net for the reference GIFs; download them into the scratchpad as next-ai-3.6.3.md D says, never commit them). A
session on Default: get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass; take care that a sed does not hit two clips); keep
every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push
and stop without checking CI.
