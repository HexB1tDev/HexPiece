Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.1.8.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zb of next-ai-3.1.8.md
(2.9.6 to 3.1.8); open the older next-ai-*.md only for a feature's detail. If freesound.org or tenor.com is refused, the session is on
the Default environment: switch_environment to "HexPiece" (section 7).

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

3.1.1 to 3.1.3 are the Gura Gura no Mi (sections 8r, 8s, 8u) and the Magu Magu no Mi (8t). 3.1.4 (8v): first-person hand moves for
every Light, Gura and Magma move. 3.1.5 (8x, green): the Conqueror's lightning out of every fruit's explosions (Gear 5 too, through
PiecePatch.gearLightning), the Light right click dragging foes to the beam's end, the Magma hound rebuilt as a wolf's head flying
where you aim, Ryusei Kazan's hold-to-load meteors. 3.1.6 (8y): the Gura ultimate, Island Breaker, on N since 3.1.7 (8z: the owner's "end key" was the N key, one more fruit
move key beside Z X C V B): charge only, the
bubble swelling on the cocked fist, the ground in a huge cone heaving and breaking into squares (drawn with the world's own blocks,
gura/Heave + client/gura/HeaveDraw), five fissures, craters, a 5-minute cooldown, with Haki thick Conqueror's lightning out of the
breaking ground and a 10-second stun.

3.1.8 (8zb): the Gura ultimate really bends the ground (a bowl with rings, its rim heaved up, slabs up and down, all growing back),
the Haki lightning on every fruit's moves is the Conqueror's release's own bolts, every charge on the first-person fist drawn in the
hand's own pass. Next (8za, the owner's open requests verbatim, in order): 2. the Hie Hie no Mi (Aokiji's ice fruit) as 3.1.9
(references in docs/handoff/hie-ref, the move set in 8za); 3. a Creative tab for the fruits only, rarity ranks (common, uncommon,
rare, legendary, mythical) in the fruits' names, a custom fruit energy bar over the food bar (an original design).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
