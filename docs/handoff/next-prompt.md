Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zzl of next-ai-3.2.2.md
(2.9.6 to 3.5.2); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: 3.4.9 (section 8zzh: the owner's Gear 5 list) is green (Build #196, Release #140). 3.5.0 (section 8zzj: the owner's
Ito list, 8zzi verbatim) is green (Build #197, Release #141). 3.5.1 (section 8zzk: the Awakening's tentacles rebuilt massive: a giant
rope of three thick twisted plies of white string shading lilac-purple, a Haki-black spear tip driven through the body, cords bursting
from its foot; rooted beside a near body so it never comes up under the user) is green (Build #198, Release #142). 3.5.2 (section
8zzl: Sky Road rebuilt at the owner's "Sky walk needs a rework ... make sky walk insanely epic": one string at a time, W/A/S/D a Spider-Man
swing that holds its height, a tap of Space a leap to a string further away, Space held a climb on woven steps of string like stairs,
Shift a drop, still one hand hanging; multiplayer through network/C2SItoSky and S2CUoFx 254) is green (Build #200, Release #143); 3.5.3 (the owner: "For the stairs its
only 1 string not more", "No its a normal string. Not a thick one": one thin string a step) and 3.5.4 (section 8zzm: the owner's Birdcage bug: closing froze everyone and dragged them to the middle;
now they move freely inside, only touching its strings cuts them and the closing wall sweeps them in, the full damage when it shuts)
are green (3.5.3: Build #201, Release #144; 3.5.4: Build #202, Release #145).

3.5.5 (sections 8zzn verbatim, 8zzo the plan and the owner's answers, 8zzp as built): the Legendary Mochi Mochi no Mi, everything in
one version (base moves on LMB RMB Z X C V B N, the Awakening on M a 30 s mode with its own five techniques on Z X C V B, Future Sight
on a double tap of sneak, Haki-black fists and arms only with Armament) is green (Build #207, Release #146); its hourly resume task
is deleted.

3.5.6 (section 8zzq): the Mochi arms square bars with the fist a block at their very end, as the owner asked after 3.5.5; green
(Build #209, Release #147).

Next: wait for the owner's notes on 3.5.6 (Mochi), 3.4.9 (Gear 5), 3.5.0 to 3.5.4 (Ito) and their lists for the other fruits. The fist is a right fist
on the right arm (the owner: "thats the left hand on the right arm"). Read 8zz, 8zzb and 8zzk before any art: thin pale lines and flat
polygons are not acceptable; hot strings use ItoArt's HOT tint; never lay a see-through ribbon over or through an opaque body (it punches
holes in it: 8zzk).

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
