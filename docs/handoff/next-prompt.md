Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-2.9.5.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5); open the older next-ai-*.md only for a
feature's detail.

2.9.5 is pushed and green: the Conqueror's charge is one held sound that fades out the moment G is let go, and the release's lightning
crackles are gone. The next version is 2.9.6. First check the branch's last "Build HexPiece" and "Release HexPiece" runs, then ask
the owner how the Conqueror's charge and release sound now and how 2.9.4 plays (sparks in the Ryou colour, Gear 5's 3D lightning,
thin fissures, bodies thrown back), and fix whatever they report first. Then follow section 9 of next-ai-2.9.5.md.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
