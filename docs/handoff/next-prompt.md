Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there: the owner's
answer this chat).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (newest: sections F to O: the owner's requests verbatim and answers, the
base GIFs read frame by frame (G), the 3D lightning (I), the HUD (J), the Mochi moves rebuilt (K), Buzz Cut Mochi (L, M), the owner's
"use my imapct frame" and their notes on Buzz Cut, Lotus, the Mochi feeling dead and WiFi Haki (N), the chat that released 3.6.3 (O),
the DoughV2 GIFs read frame by frame (P), 3.6.4 and 3.6.5 (Q) and where it stopped (R)), then docs/handoff/next-ai-3.6.0.md sections I to
N, then POINT ONE and sections 0 to 7 of docs/handoff/next-ai-3.2.2.md (every standing rule).

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's (G): thin, zig-zagging out of the player every way, in the player's Haki colour, and (this chat, the owner: "make the
lightning 3d once it for all, all of it", "the tip of the lightning has to be sharp") drawn as round lit 3D tubes everywhere with
needle-sharp tips, never a flat strip or the original's flat beams (next-ai-3.6.3.md I). Never add another lightning look.

The owner's standing order on the Mochi's feel ("it should be more stretchy, more animated, more squishy, it's mochi for god's sake, the
arms should have more elasticity"; "mention the fluidity and aliveness of the mochi we want to the new AI"): the mochi is FLUID AND
ALIVE, never stiff or still. Every arm stretches like dough (thinner the further it is drawn out, a wobble rippling along it, sagging,
shooting past its reach and springing back, snapping back with a rebound); every fist squashes as it lands; every donut and mass of
dough breathes, wobbles, puffs past its size and settles, drips; everything forms and goes fluidly, never popping in or out; dough
splashes and splats when it hits (next-ai-3.6.3.md N 19; tools: MochiDraw.arm, elastic, springy, the donut's breathing,
MochiArt.CHURN). Build every new move this way and judge every render for it.

The owner's newest standing word on the Mochi fruit: "It is very important that you do not give up until the moves look exactly like
the reference GIFs that you have. If they don't look like the reference GIFs exactly, to very minute detail, I don't want them."

Where things stand: 3.6.5 is the last release (commit 2b479fd; its Build #240 and Release #156 were still running when the last chat
stopped: check them first and fix them if red): the base Mochi kit after the reference GIFs with the owner's notes (Buzz Cut's elastic
band leads the heavy ball; Lotus knocks them donut to donut like a rag doll; every mochi arm stretchy, wobbling and thinning as it
stretches, the punch springing past its reach, the donuts breathing, the dough churning), WiFi Haki on the one 3D Haki lightning, and
the Awakening on M with Trident Lacerations on the left click (Mogura held in hand). Committed after it, unversioned: Power Mochi
(awakened V) built, its last render not yet judged (next-ai-3.6.3.md R).

Do next (next-ai-3.6.3.md R): (1) check 3.6.5's runs; (2) finish Power Mochi: render and judge it beside Power_Dough.gif (frames 30,
66 to 70), its budget in DragonTests and beats in HudTests, first person; (3) Z Grilled Lance, X Elastic Lasso, C Oblivion, B Blazing
Storm, N Scorching Buzz Cut, each built on its GIF's beats (section P), rendered side by side with its GIF, fixed and rendered again
until it matches; (4) release 3.6.6 (next_version, notes, handoff, push, both runs green, send previews). The owner's notes on anything
come first: write them verbatim into section N.

The cloud environment must be the personal "HexPiece" (it now also allows official-fruit-battlegrounds.fandom.com and
static.wikia.nocookie.net for the reference GIFs; download them into the scratchpad as next-ai-3.6.3.md D says, never commit them). A
session on Default: get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass; take care that a sed does not hit two clips); keep
every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push
and stop without checking CI.
