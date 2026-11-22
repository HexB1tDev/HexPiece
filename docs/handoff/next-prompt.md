Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.1.1.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8r of next-ai-3.1.1.md
(2.9.6 to 3.1.1); open the older next-ai-*.md only for a feature's detail. If freesound.org or tenor.com is refused, the session is on
the Default environment: switch_environment to "HexPiece" (section 7).

3.1.1 is the Gura Gura no Mi (section 8r), built from the Whitebeard and Blackbeard GIFs in docs/handoff/gura-ref/ after the owner
okayed the move set ("Go ahead") and asked for the landing quake ("Add it"): RMB Kaishin (tap / hold), Z Shima Yurashi (hold),
X Kabutowari, C Tsunami, V Gekishin (hold), B Ground Splitter, LMB the game's punch, the passive (no fall damage, a quake where you
land hard). Not tested in game.

First check the branch's last "Build HexPiece" and "Release HexPiece" runs (3.1.1) are green. Then follow section 9 of
next-ai-3.1.1.md: ask the owner how the Gura fruit plays and fix what they say (the next version is 3.1.2); the owner still has to say
how 3.0.x to 3.1.0's Light fruit and katana fixes play, and 2.9.5's and 2.9.4's.

Earlier: 3.1.0: no Light fruit stance at all. 3.0.9: no sea effects while the body is light. 3.0.8: a drawn katana's ready pose plays
under every other animation. 3.0.0 to 3.0.7 and 2.9.6 to 2.9.9: the Light fruit rebuilt after Kizaru at Egghead (sections 8b to 8p).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
