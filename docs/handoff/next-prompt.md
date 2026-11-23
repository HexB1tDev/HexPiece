Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/pensive-heisenberg-mkr9cl: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zt of next-ai-3.2.2.md
(2.9.6 to 3.3.1); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional),
and that lightning is EXACTLY the Conqueror's release's own bolts (ConquerorStorm.blast cuts the release's bolt short: never thin lines,
never fat tubes, never re-scaled, never any other lightning; section 8zn has the owner's fury after it went wrong more than ten times:
render any lightning change beside the release from the same camera before pushing).

Where things stand: 3.2.2 (the Ice fruit's crystal and frost punches, Ice World's field, the Gura cone, thin Haki lightning, no ground
art in midair, the Magma X fix) is pushed and green. 3.2.3 (section 8zl: the Nika fruit part 1: Z Gear 5, X Rocket / Star Gun, C
Whip, V Gatling, B Giant / Bajrang Gun, N Coil Spring / Lightning Lance, M Rubber World; the panel renaming them with Haki in Gear 5,
white; the stretch moves untransformed; drums instead of the transformation music) is pushed and green. 3.2.4 (8zm: the transformation
in about 4 s with a new animation, the punch's wobble gone) is pushed and green. 3.2.5 (8zn: the move lightning the release's own again)
is pushed and green. 3.2.6 (8zo: the Bajrang Gun's rounded fist, swept down as the body turns head-first) is pushed and green. 3.2.7 (8zp: Dawn Whip as a
cartoon spin) is pushed and green. 3.2.8 (8zq: Giant holds what it grabs, in the drawn fist on every client) is pushed and green. 3.2.9 (8zr: Star Gun's struck
body as rubber) is pushed and green. 3.3.0 (8zs: Rubber World's ground rolling in rubber waves) is pushed and green. 3.3.1 (8zt: Toon
Force, Dawn Whip and the Gear 5 jump pose redone to the owner's target look) is pushed unchecked: first check its "Build HexPiece"
and "Release HexPiece" runs.

Next: the owner's insisted jump change, as 3.3.2 (section 8zt item 5 has the words verbatim and how to read them): the Shift+Space
Sky Bounce removed; the fruit user's normal jump much higher and always playing gear5_bounce (3.3.1's laughing skip); every jump turns
the ground under the user to rubber (a small Rubber World, for the user only); and Rubber World (M) then works on everyone else, "to
confuse the target, displace them and overwhelm". Then the rest of the Nika rework (better Haki versions: Dawn Pistol; never touch the
lightning lance). The owner's target look for the fruit is Gear 5 Luffy's laughing skip (8zt item 3): every new pose with a raised arm
must pass tools/anim.face_covered (no hand drawn over the face from the front, three-quarters or the side) and be looked at, never
passed on a 3D test alone (the owner: "how the FUCK is that a pass").

The cloud environment is the personal "HexPiece", env_01DEzkwiWiBUXF6hnQ29v2S5 (freesound.org, cdn.freesound.org, opengameart.org); if
freesound.org is refused, the session is on Default: get_environment, then switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; no music (the owner: copyright); versions roll over at 9; a push is done only when GitHub's runs are green;
at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.

The owner during 3.2.9: "I want gifs when it's all done": when the work is done, send GIFs of every move changed (tools/make_gif.py
from FxPreview frames, tools/clip_gif.py for clips with Toon Force; docs/gifs/ has 3.2.8's, 3.2.9's and 3.3.0's; 3.3.1's Toon Force
grid and the two redone clips still to make), with SendUserFile, and say they are previews, not the game.
