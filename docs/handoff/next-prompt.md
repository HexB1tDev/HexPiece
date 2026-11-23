Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zze of next-ai-3.2.2.md
(2.9.6 to 3.4.1); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: up to 3.4.0 everything is pushed and green (3.4.0 Dawn Pistol's skin stretched through the body: Build #181,
Release #131). 3.4.1 (section 8zze: Dawn Pistol after the owner's "How IS THIS IN HELL STRAIGHT" and "It also has to be FAST, but when it
lands and stuns (the skin streches to the limit slower and slower ...) ... projected in a straight line FAST. adding a dust explosion on
landing (and making a crater)": the arm a looping rubber curve, through the body to the fist inside the stretched skin, slower and slower
to its limit, then a straight fast launch through blocks, a crater and dust where it lands) is pushed: check its Build and Release runs
are green first and fix anything red, then ask how Dawn Pistol plays. The fist is a right fist on the right arm, three-quarters of a
block across (the owner: "thats the left hand on the right arm", "the fist is too big, way too big"). Read 8zz and 8zzb before any art: the owner called Supa's first flat blades "really poor and poorly
made"; judge every effect against the Light, Gura and Magma art before showing it; thin pale lines and flat polygons are not acceptable.

Next after that: the owner has not tested Horo (3.3.6) or Ito (3.3.7) yet ("Not tested yet"): ask again how they play. Their word on
the strings (8zzd, verbatim): "it looks like dust", "its meant to be slimmer, they're too thick", "sharper in the end too not just
square ends..", "where are the sharp ends?": keep every string slim, solid (no coplanar glow layers: they speckle) and pointed at both
ends; judge strings at 840 pixels wide or more. Then the rest of the Nika rework (better Haki versions; never touch the lightning
lance). The owner's target look for the Nika fruit is Gear 5 Luffy's laughing skip (8zt item 3, 8zu item 5): every pose with a raised
arm keeps the hand off the face from the front (tools/anim.face_covered) and is looked at beside the owner's pictures from every side;
a raised fist the owner wants forward stays forward.

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
