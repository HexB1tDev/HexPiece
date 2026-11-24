Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there: the owner's
answer this chat).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (newest: sections F to M are this chat's: the owner's requests verbatim and
answers, the base GIFs read frame by frame (G), the 3D lightning (I), the HUD (J), the Mochi moves rebuilt (K), Buzz Cut Mochi (L) and
where it stopped (M)), then docs/handoff/next-ai-3.6.0.md sections I to N, then POINT ONE and sections 0 to 7 of
docs/handoff/next-ai-3.2.2.md (every standing rule).

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's (G): thin, zig-zagging out of the player every way, in the player's Haki colour, and (this chat, the owner: "make the
lightning 3d once it for all, all of it", "the tip of the lightning has to be sharp") drawn as round lit 3D tubes everywhere with
needle-sharp tips, never a flat strip or the original's flat beams (next-ai-3.6.3.md I). Never add another lightning look.

The owner's newest standing word on the Mochi fruit: "It is very important that you do not give up until the moves look exactly like
the reference GIFs that you have. If they don't look like the reference GIFs exactly, to very minute detail, I don't want them."

Where things stand: 3.6.2 is the last release. Committed, unversioned (the owner: "Hold until Mochi is done"): the 3D lightning, the
HUD hidden while powers are off, the Mochi material white as in the GIFs, Lotus Mochi Combo, Grilled Mochi, Mochi Muddle, Chestnut
Mochi and Mochi Anemone rebuilt beat by beat on their GIFs, Buzz Cut Mochi's art after the GIF, the owner's anime frame and the show
clip (a thin white elastic arm stretching to an insane length, an enormous spiked Haki ball, the victims stuck on it, the horizontal
whirl, the slam into a rubble field and black star crater) and its first server/client wiring (next-ai-3.6.3.md M: check the last
push's GitHub Build first).

Do next, in order (next-ai-3.6.3.md M): (1) fix the build if red; (2) judge Buzz Cut in motion and in first person against its
references and the owner's 8zzv 10 text (horizontal whirl), fix, render again; (3) DragonTests.mochiArt with the new pieces under 16k
quads, remove the dead old Mochi art and clips; (4) release 3.6.3 (next_version, notes, handoff, push, both runs green, send previews);
(5) 3.6.4: the awakened DoughV2 set on M (the seven moves promised in next-ai-3.6.3.md F 7), each matched to its GIF the same way.

The cloud environment must be the personal "HexPiece" (it now also allows official-fruit-battlegrounds.fandom.com and
static.wikia.nocookie.net for the reference GIFs; download them into the scratchpad as next-ai-3.6.3.md D says, never commit them). A
session on Default: get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass; take care that a sed does not hit two clips); keep
every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push
and stop without checking CI.
