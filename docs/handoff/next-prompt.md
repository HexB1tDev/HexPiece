Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.1.5.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8x of next-ai-3.1.5.md
(2.9.6 to 3.1.5); open the older next-ai-*.md only for a feature's detail. If freesound.org or tenor.com is refused, the session is on
the Default environment: switch_environment to "HexPiece" (section 7).

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

3.1.1 to 3.1.3 are the Gura Gura no Mi (sections 8r, 8s, 8u) and the Magu Magu no Mi (8t). 3.1.4 (8v): first-person hand moves for
every Light, Gura and Magma move. 3.1.5 (8x, pushed): the Conqueror's lightning out of every fruit's explosions (Gear 5 too, through
PiecePatch.gearLightning), the Light right click dragging foes to the beam's end, the Magma hound rebuilt as a wolf's head flying
where you aim, Ryusei Kazan's hold-to-load meteors.

Next: 3.1.6, the Gura ultimate on the End key (section 8w, verbatim; reference GIF docs/handoff/gura-ref/ultimate-whitebeard-punch.gif):
charge only, the bubble swelling round the cocked right fist, released the ground in a huge cone ahead heaves and breaks into squares
and fissures, a 5-minute cooldown, with Haki on the thick Conqueror's lightning out of the breaking ground everywhere and it stuns
(asked for by name). Then section 9.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
