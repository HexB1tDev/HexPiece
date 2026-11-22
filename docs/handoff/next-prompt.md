Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.1.4.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8v of next-ai-3.1.4.md
(2.9.6 to 3.1.4); open the older next-ai-*.md only for a feature's detail. If freesound.org or tenor.com is refused, the session is on
the Default environment: switch_environment to "HexPiece" (section 7).

3.1.1 to 3.1.3 are the Gura Gura no Mi (sections 8r, 8s, 8u) and the Magu Magu no Mi (8t). 3.1.4 (section 8v): a short first-person
hand move for every Light, Gura and Magma move (style/HandMoves, client/FirstPersonHand transforming the game's own hand), the white orb
over the head with Haki on fixed (no MIXED flash, Kaishin's lightning on the drawn fist), a new CC0 charge hum for the Gura right click.
Not tested in game.

First check the branch's last "Build HexPiece" and "Release HexPiece" runs (3.1.4) are green. Then build the owner's open requests in
section 8w of next-ai-3.1.4.md, quoted verbatim there: 3.1.5 (the Magma hound's head and its aim with pitch, Ryusei Kazan's
hold-to-load meteors that all fall on release, the Light right click dragging foes to the beam's end and exploding, the Conqueror's
lightning on every fruit's moves with the Conqueror's Haki and Ryou on, visual only), then 3.1.6 (the Gura ultimate on the End key,
reference GIF in docs/handoff/gura-ref/). Then follow section 9.

Earlier: 3.1.0: no Light fruit stance at all. 3.0.9: no sea effects while the body is light. 3.0.8: a drawn katana's ready pose plays
under every other animation. 3.0.0 to 3.0.7 and 2.9.6 to 2.9.9: the Light fruit rebuilt after Kizaru at Egghead (sections 8b to 8p).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
