Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/pensive-heisenberg-mkr9cl: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zm of next-ai-3.2.2.md
(2.9.6 to 3.2.4); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

Where things stand: 3.2.2 (the Ice fruit's crystal and frost punches, Ice World's field, the Gura cone, thin Haki lightning, no ground
art in midair, the Magma X fix) is pushed and green. 3.2.3 (section 8zl: the Nika fruit part 1: Z Gear 5, X Rocket / Star Gun, C
Whip, V Gatling, B Giant / Bajrang Gun, N Coil Spring / Lightning Lance, M Rubber World; the panel renaming them with Haki in Gear 5,
white; the stretch moves untransformed; drums instead of the transformation music) is pushed and green. 3.2.4 (8zm: the transformation
in about 4 s with a new animation, the punch's wobble gone) is pushed: first check its "Build HexPiece" and "Release HexPiece" runs.

Next: the rest of the Nika fruit (Gear 5) rework, the owner's biggest request (section 8zk lists all of it with the owner's answers;
8zh item 8 and 8zj item 2 have their words verbatim, with their reference images described; section 9 says how): every third-person
animation redone (Bajrang Gun from above with the body turning in the air to slam a big rounded fist down, Dawn Whip scrapped and redone as a
cartoon spin after Luffy vs Kizaru, Star Gun's struck body more rubbery), Giant holding bodies properly, Rubber World bending the real
ground in rubbery waves, better Haki versions, Toon Force everywhere ("like a Bugs Bunny movie"); first person left alone. The owner
loves the fruit, the transformation and the clouds: change carefully, in its own versions, rendering every clip.

The cloud environment is the personal "HexPiece", env_01DEzkwiWiBUXF6hnQ29v2S5 (freesound.org, cdn.freesound.org, opengameart.org); if
freesound.org is refused, the session is on Default: get_environment, then switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; no music (the owner: copyright); versions roll over at 9; a push is done only when GitHub's runs are green;
at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
