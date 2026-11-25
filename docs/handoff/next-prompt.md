Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest; push there: the owner's
answer this chat).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (newest: sections F to R: the owner's requests verbatim and answers, the
base GIFs read frame by frame (G), the 3D lightning (I), the HUD (J), the Mochi moves rebuilt (K), Buzz Cut Mochi (L, M), the owner's
"use my imapct frame" and their notes on Buzz Cut, Lotus, the Mochi feeling dead and WiFi Haki (N), the chat that released 3.6.3 (O),
the DoughV2 GIFs read frame by frame (P), 3.6.4 and 3.6.5 (Q), where the fourth Mochi chat stopped (R), the fifth chat's six awakened
moves built on their GIFs and 3.6.6 (S), where it stopped (T), the Phoenix request (U), 3.6.7 (V), 3.6.8 (W), the Phoenix animated
(X), the Phoenix fruit in game, 3.6.9 (Y), the owner's notes on it (Z), 3.7.0 (AA), the owner's next notes (AB), 3.7.1 (AC), the move GIFs read (AD), 3.7.2 (AE), 3.7.3 (AF), the owner's note on the walk and the feet with 3.7.4 (AG), the owner's bug list after 3.7.4 (AH), the owner's newest requests: the
Phoenix's Bluebird and M1, the new Leopard fruit, Zoan forms locked out of Style (AI), and 3.7.5 (AJ)), then docs/handoff/next-ai-3.6.0.md sections I to
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

Where things stand: 3.7.5 is the last release (the owner's bug list AH 1-7, next-ai-3.6.3.md AJ: Buzz Cut's head held still, the
Tori fruit's human effects drawn again, a calm imposing phoenix walk, X as the phoenix a fire tornado of blue and gold flame (carries,
burns, flings; bigger with Conqueror's lightning under Conqueror's Haki and Ryou), every Phoenix move's fire rebuilt in the house's
style (phoenix/PhoenixBlast: real explosions, fireballs, no tongue particles), no 360 spins anywhere, Bluebird one ball whose blast is
a giant phoenix of flame, the Horo projection's arms still and slim for slim skins). Before it: 3.7.4 (the feet and the walk, AG),
3.7.3 and 3.7.2 (the Phoenix's kit, AE, AF), 3.7.1, 3.7.0, 3.6.9 (the Phoenix fruit), 3.6.6 to 3.6.8 (the Mochi fruit).

Do next, in this order (the owner's words verbatim in next-ai-3.6.3.md AI; write every new request there the moment it comes):
(1) check the last push's Build and Release runs.
(2) The Phoenix: "The Blue bird move should shoot and charge a sphere not a bird (made of flames)." 3.7.5 already charges and fires a
    sphere of fire (PhoenixBlast.fireball) in both forms and only its blast is a bird: make sure nothing bird-shaped is left in the
    charge or the flight, and ask the owner if they meant more. "Transformed M1, shall make the bird open it's beak and act as a
    flamethrower (blasting flames at the target like the dragon does)": the phoenix's M1 as a flamethrower out of its opened beak
    (the beak opening animated on the model's head/jaw bones in PhoenixForm.pose; the flames as the dragon fruit's M1 flamethrower:
    find it in server/UoMoves and dragon/UoFire, in the phoenix's blue and gold; Conqueror's lightning with Haki and Ryou).
(3) The new fruit, the Neko Neko no Mi (Legendary, Zoan: Lucci's leopard): the owner's model is
    input/neko/one-piece-bounty-rush-lucci-awakened-form.zip (how to open its .rar and the FBX: next-ai-3.6.3.md AI); bake it as the
    phoenix was baked (tools/fbx_reader.py, tools/phoenix_bake.py, dragon/DragonModel .sry), 5 blocks tall. "Animate it properly,
    realistic fluid animations, the mane and body hair IS black flames, the black ribbon around the leopard is black flames as well,
    We need Idle, running, jumping, transforming and de-transforming animations, facial animations too (simple)". Its moves, both
    forms: https://official-fruit-battlegrounds.fandom.com/wiki/LeopardV2 (download its GIFs into the scratchpad, read them frame by
    frame as next-ai-3.6.3.md D and AD did for the Phoenix, never commit them). Register it like the Phoenix fruit (server/PhoenixServer,
    PhoenixKit, the fruit's item, icon, panel), POINT ONE for every move.
(4) "In no ZOAN transformed state can you switch to Style or any other mode aside from fruit. (Tori, gear 5, dragon, etc)": while
    transformed as the phoenix, the dragon, Gear 5 or the leopard, switching to Style (or any mode other than the fruit) is refused.
(5) The 3.7.5 GIFs (tools/preview/PhoenixPreview: m-fujip, m-hawk, m-infp, m-bluep, m-blueh, m-boom, m-tornado2, m-bird, walk) on
    the GIF page https://claude.ai/artifact/8rkQGG2vrMuSZoZWoJdoB7. Item 4 of the owner's Mochi note 22 came empty: ask for it.

The cloud environment must be the personal "HexPiece" (it now also allows official-fruit-battlegrounds.fandom.com and
static.wikia.nocookie.net for the reference GIFs; download them into the scratchpad as next-ai-3.6.3.md D says, never commit them). A
session on Default: get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself, side by side
with its reference; never key the head or unused limbs (tools/anim lint must pass; take care that a sed does not hit two clips); keep
every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0 web recording; versions roll
over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push
and stop without checking CI.
