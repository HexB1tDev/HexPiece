Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zt of next-ai-3.2.2.md
(2.9.6 to 3.3.4); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: up to 3.3.5 everything is pushed and green (sections 8zl to 8zz). 3.3.6, the Horo Horo no Mi, is IN PROGRESS
(8zzb): pushed at the owner's word ("Enough prompt and handoff") without a local build or a CI check: first run python3 build.py and
check the branch's "Build HexPiece" run, fix anything red, then finish it as 8zzb lists (client/horo/HoroFx and HoroClient, the
astral spirit drawn as the player's own ghostly body, sounds, tests, release notes). Read 8zz before any art: the owner called Supa's
first flat single-colour blades "really poor and poorly made", chose "Katana steel" and said "Yes, build it"; the cuts in the air were
rebuilt as the Light fruit's wave. Horo's ghosts (horo/HoroArt) were judged good in renders (docs/handoff/horo-ghosts-3.3.6.jpg). Judge
every effect against the Light, Gura and Magma art before showing it; thin pale lines and flat polygons are not acceptable.

Next: finish 3.3.6 Horo Horo (8zv verbatim, 8zw the keys the owner accepted, 8zzb the state); then the owner's Rare fruit, the Ito Ito
no Mi (Doflamingo, 8zza verbatim): plan its keys and ask first. Ask how 3.3.2 and 3.3.3 feel (asked, not yet answered). Then the rest
of the Nika rework (better Haki versions: Dawn Pistol; never touch the lightning lance). The owner's target look for the Nika fruit is
Gear 5 Luffy's laughing skip (8zt item 3, 8zu item 5): every pose with a raised arm keeps the hand off the face from the front
(tools/anim.face_covered) and is looked at beside the owner's pictures from every side; a raised fist the owner wants forward stays
forward.

The cloud environment is the personal "HexPiece", env_01VDpZP1V4zScExADvRmie8m (freesound.org, cdn.freesound.org, opengameart.org); if
freesound.org is refused, the session is on Default: get_environment, then switch_environment to it. Check every new sound AS CUT for
pitched lines (8zy has the measure that works).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; no music (the owner: copyright); versions roll over at 9; a push is done only when GitHub's runs are green;
at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.

The owner during 3.2.9: "I want gifs when it's all done"; during 3.3.4: "no need for gif, proceed to the next fruit". Send renders (or a
GIF when the owner wants one) with SendUserFile, and say they are previews, not the game.
