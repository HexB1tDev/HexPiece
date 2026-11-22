Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zi of next-ai-3.2.2.md
(2.9.6 to 3.2.2); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

Where things stand: 3.1.9 (the Hie Hie no Mi), 3.2.0 (the Devil Fruits Creative tab, rarity ranks, the fruit energy bar) and 3.2.1 (one
violet energy bar for every fruit) are pushed and green. 3.2.2 is IN PROGRESS, committed WITHOUT a full build (the owner asked for this
handoff mid-work; build.py still says 3.2.1): section 8zh has the owner's eight requests after 3.2.1 verbatim (with his screenshots
described), section 8zi what is done (the Ice fruit's melodic sounds replaced by real ice recordings, checked by their spectra; the cut
crystal of the first renders back in hie/HieArt for frozen bodies, their shatter, the frost a punch leaves and the Ice Ball's crystal
prison, rendered and judged but not yet used by the game) and what is left, file by file:
1. Frozen bodies in the drawn crystal that shatters, no real ice blocks round them or in the Ice Ball (real ice melts into water).
2. Frost punches: in fruit mode the Ice fruit's left click frosts what it hits (the body turning blue), four hits freeze it in crystal,
   the next hit from its freezer shatters it; bare-handed attackers without Haki get the frost too, from the arm.
3. Ice Age ("Ice World"): its frozen ground gone after 8 s, and everything (not the caster) inside its area frozen while it lasts.
4. The Gura ultimate's bent ground shaped as the cone pushing forward, not a round bowl.
5. The Haki lightning on every move as thin as the Conqueror's release's (now "ridiculously fat").
6. No ground cracks, dust, rocks or pools in midair (Shima Yurashi's cracks hang in the air): ground art only on the ground under it.
7. Then the Nika fruit (Gear 5) rework: the transformation on Z, current keys, stretchy moves and M1s untransformed, much shorter
   transformation music, a white HUD, better Haki versions, reworked third-person animations (Bajrang Gun turning in midair to aim
   down), Rubber World bending the real ground like rubber; first person left alone; the owner loves the fruit: change carefully.
Run python3 build.py first and fix what fails, then items 1 to 6 as version 3.2.2 (release notes, handoff, push, both runs green).

The cloud environment is the personal "HexPiece", now env_01PmnEXrwDhxzij8M5q6P6bL (freesound.org, cdn.freesound.org, tenor.com and
the rest); if freesound.org is refused, the session is on Default: switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; versions roll over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the
handoff and this prompt first, commit, push and stop without checking CI.
