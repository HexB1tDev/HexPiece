Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zzg of next-ai-3.2.2.md
(2.9.6 to 3.4.4); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: up to 3.4.3 everything is pushed and green (3.4.3: Release #134, Build #187). 3.4.4 (8zzg: the Nika fruit's C:
untransformed the plain Gum-Gum Whip; in Gear 5 Goof Ball, held, a cartoon fight cloud with the user's own Gear 5 limbs punching out,
dizzy after with stars over the head; with Haki the new Dawn Whip in the owner's six phases, ending drilled into the ground and popping
back out) was pushed: check its Build and Release runs first and fix anything red.

Next, as told to the owner: 3.4.5 the Gura ultimate's Broken Bones (8zzg has the owner's request verbatim and their four answers): when
the Gura ultimate (N) strikes a player, the owner's bone-fracture GIF plays once on that player's screen as an X-ray overlay; everyone
struck (never the user) is stunned and ragdolled and gets the new effect Broken Bones for 20 s (Slowness I, a random ragdoll fall every
3 to 6 s). The GIF is art/broken-bones.gif (the owner's own file, sent in a zip during 3.4.4's session: 12 frames of 240 ms, 356 x 358). The owner has not said how Dawn Pistol (3.3.9 to 3.4.1), Horo or the 3.4.2 to 3.4.4 work play: ask once 3.4.5
is out. The fist is a right fist on the right arm (the owner: "thats the left hand on the right arm"). Read 8zz and 8zzb before any art:
thin pale lines and flat polygons are not acceptable; hot strings use ItoArt's HOT tint.

The cloud environment is the personal "HexPiece" (freesound.org, cdn.freesound.org, opengameart.org); if freesound.org is refused, the
session is on Default: get_environment, then switch_environment to it (network_only). Freesound also rate-limits its pages for a minute or two (403):
wait and retry. Check every new sound AS CUT with tools/tonal_lines.py against plain noise of the same length (8zzb).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; no music (the owner: copyright); versions roll over at 9; a push is done only when GitHub's runs are green;
at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.

The owner during 3.2.9: "I want gifs when it's all done"; during 3.3.4: "no need for gif, proceed to the next fruit". Send renders (or a
GIF when the owner wants one) with SendUserFile, and say they are previews, not the game.
