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

Where things stand: up to 3.3.1 everything is pushed and green (sections 8zl to 8zt). 3.3.2 (8zu: Sky Bounce gone; every Gear 5
jump about five blocks high with the laughing skip, the ground springing and rippling like rubber, no fall damage; Rubber World
throwing everyone but its user about; the skip pose redone from the owner's limb-by-limb description after they were furious it was
still wrong: the right fist raised FORWARD, never to the side) is pushed and green. 3.3.3 (8zx: Rubber World's waves slow and of
different sizes, at the owner's word) is pushed and green. **3.3.4, the Noro Noro no Mi, is IN PROGRESS (8zy)**: pushed at the owner's
word ("Enough, handoff and prompt, now") without a local build or a CI check: first check the branch's "Build HexPiece" run and fix
anything red, then finish it as 8zy lists.

Next: the owner's three new fruits (8zv verbatim, 8zw the keys the owner accepted): finish 3.3.4 Noro Noro, then 3.3.5 Supa Supa
(Uncommon; with Armament the blades black-purple like the katanas), then 3.3.6 Horo Horo, each built as the Ice fruit was (8zc, 8ze)
and Noro is, with "simple reused animations already in the mod". Freesound refused the last session (403): Noro's sounds borrow the
mod's own CC0 recordings for now; try Freesound again and give it its own. Ask how 3.3.2 and 3.3.3 feel. Then the rest of the Nika
rework (better Haki versions: Dawn Pistol; never touch the lightning lance). The owner's target look for the Nika fruit is Gear 5
Luffy's laughing skip (8zt item 3, 8zu item 5): every pose with a raised arm keeps the hand off the face from the front
(tools/anim.face_covered) and is looked at beside the owner's pictures from every side; a raised fist the owner wants forward stays
forward.

The cloud environment is the personal "HexPiece", env_016v9rNSffz2fds2ha514nhY (freesound.org, cdn.freesound.org, opengameart.org); if
freesound.org is refused, the session is on Default: get_environment, then switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; no music (the owner: copyright); versions roll over at 9; a push is done only when GitHub's runs are green;
at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.

The owner during 3.2.9: "I want gifs when it's all done": when the work is done, send GIFs of every move changed (tools/make_gif.py
from FxPreview frames, tools/clip_gif.py for clips with Toon Force; docs/gifs/ has 3.2.8's to 3.3.2's), with SendUserFile, and say they are previews, not
the game.
