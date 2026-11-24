Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there: the owner's
answer this chat).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (newest: sections F to R: the owner's requests verbatim and answers, the
base GIFs read frame by frame (G), the 3D lightning (I), the HUD (J), the Mochi moves rebuilt (K), Buzz Cut Mochi (L, M), the owner's
"use my imapct frame" and their notes on Buzz Cut, Lotus, the Mochi feeling dead and WiFi Haki (N), the chat that released 3.6.3 (O),
the DoughV2 GIFs read frame by frame (P), 3.6.4 and 3.6.5 (Q), where the fourth Mochi chat stopped (R), the fifth chat's six awakened
moves built on their GIFs and 3.6.6 (S), where it stopped (T), the Phoenix request (U), 3.6.7 (V), 3.6.8 (W), the Phoenix animated
(X) and the Phoenix fruit in game, 3.6.9 (Y)), then docs/handoff/next-ai-3.6.0.md sections I to
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

Where things stand: 3.6.9 is the last release: the Tori Tori no Mi, Model: Phoenix in game (Mythical; Z turns the player into the
owner's phoenix, flying as in creative a little faster, and back, 3 s cooldown, no meter; the change a spiral of blue fire; next-ai-3.6.3.md
Y) and Buzz Cut with the body still, only the right arm swinging (N 31). Before it: 3.6.8 (Build #250, Release #159): Buzz Cut's whirl a
spiral widening every turn, Scorching Buzz Cut seven turns with a far bigger crater and always catching, every dough sound real CC0
slime; 3.6.7: Mogura held mid-shaft and only while swinging, no impact frames in the Mochi fruit, no Buzz Cut cutscene; 3.6.6: the
whole Mochi Awakening.

Do next: (1) check the last push's Build and Release runs; (2) the owner's word on the Phoenix in game comes first (write it verbatim
into the handoff, fix, render again); (3) then the Phoenix's moves on the wiki's GIFs (scratchpad pgifs/, or download them again from
official-fruit-battlegrounds.fandom.com/wiki/Phoenix: Fujiazami, Talon Slam / Hawk Rush, Phoenix Inferno, Bluebird, in both forms),
each read frame by frame first, built in the house style (PhoenixFire's tongues, the cel phoenix), judged beside its GIF. Item 4 of the
owner's Mochi note 22 came empty: ask for it.

The cloud environment must be the personal "HexPiece" (it now also allows official-fruit-battlegrounds.fandom.com and
static.wikia.nocookie.net for the reference GIFs; download them into the scratchpad as next-ai-3.6.3.md D says, never commit them). A
session on Default: get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass; take care that a sed does not hit two clips); keep
every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push
and stop without checking CI.
