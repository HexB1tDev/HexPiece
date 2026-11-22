Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.1.0.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8p of next-ai-3.1.0.md (2.9.6 to 3.1.0); open the older next-ai-*.md only for a
feature's detail.

The owner's current request: "Now do Gura Gura, get reference gifs from white beard and black beard both". The references are
gathered and on the branch in docs/handoff/gura-ref/ (README.md: every GIF, what it shows, which move it informs, its link; sheets/: all
56 as frame strips; clips/: the 30 that show the powers as small MP4s). No Gura code exists yet: the original gives the fruit no moves
at all, so the kit is built from scratch on the Light fruit's pattern. Section 8q of next-ai-3.1.0.md has what the original has, the
proposed move set (RMB Kaishin, Z Shima Yurashi, X Kabutowari, C Tsunami, V Gekishin, B Ground Splitter, the punch on LMB), every
ready-made piece of code to use and the plan for the art, animations and sounds.

First check the branch's last "Build HexPiece" run (and 3.1.0's "Release HexPiece", green). Then look at the reference sheets yourself,
show the owner the proposed Gura move set in one AskUserQuestion (go ahead or change), and build it as 3.1.1: the server kit, the 3D
effects, the animations, CC0 web sounds, every effect and pose rendered and judged against the references in third and first person,
release notes, push, runs green. Then follow section 9 of next-ai-3.1.0.md (the owner still has to say how 3.0.x to 3.1.0's Light
fruit and katana fixes play, and 2.9.5's and 2.9.4's).

Earlier: 3.1.0: no Light fruit stance at all. 3.0.9: no sea effects while the body is light. 3.0.8: a drawn katana's ready pose plays
under every other animation. 3.0.0 to 3.0.7 and 2.9.6 to 2.9.9: the Light fruit rebuilt after Kizaru at Egghead (sections 8b to 8p).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
