Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zzf of next-ai-3.2.2.md
(2.9.6 to 3.4.3); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: up to 3.4.2 everything is pushed and green (3.4.2: Build #184, Release #133: the owner's Ito notes, Supa's blades
in third person and sharper, Horo's projection; section 8zzf). 3.4.3 (8zzf: the Noro fruit's Z as a held cone aimed once at the press,
everything in it slowed; the Ito fruit's Sky Road on the air jump, a run through the sky on thin strings, built to the owner's long spec
quoted verbatim in 8zzf) was pushed; its Build #185 and Release #134 were still running at the handoff: check them first and fix anything
red.

Next, as told to the owner: 3.4.4 the Nika fruit's Goof Ball (the Whip without Haki: a held cartoon spin into a ball of smoke and the
player's skin punching every way, dizzy after with a symbol over the head) and the new Dawn Whip (with Haki in Gear 5; the owner's six
phases verbatim in 8zzf: the wind-up twist storing tension, the stretched leg thrust out, the explosive unwinding, the lateral whip
impact sending the target flying, the uncontrolled overspin that drills the user into the ground, the rubber-toy recovery). It is planned
in 8zzf ("3.4.4 ... NOT started": the original's whip code, its private methods, the key path, the hook without a branch, the free
effect kinds 165 to 179, the plan for both moves): read that first. The owner has not said how Dawn Pistol (3.3.9 to 3.4.1), Horo or the
3.4.2/3.4.3 work play: ask once 3.4.4 is out. The fist is a right fist on the right arm (the owner: "thats the left hand on the right
arm"). Read 8zz and 8zzb before any art: thin pale lines and flat polygons are not acceptable; hot strings use ItoArt's HOT tint.

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
