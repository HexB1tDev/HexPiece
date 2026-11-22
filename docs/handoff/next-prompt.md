Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/pensive-heisenberg-mkr9cl: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.2.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zk of next-ai-3.2.2.md
(2.9.6 to 3.2.2); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

Where things stand: 3.2.2 is pushed (section 8zi: the Ice fruit's frozen bodies in cut crystal that shatters, frost punches turning
bodies blue and freezing them on the 4th hit, Ice World's 8 s ground freezing everyone in it, the Gura ultimate's ground bent as a cone
pushing forward, the Haki lightning on moves a fifth as thick, no ground cracks, dust or lava in midair, the Magma X arm and fists
fixed). First check its "Build HexPiece" and "Release HexPiece" runs are green.

Next: the Nika fruit (Gear 5) rework, the owner's biggest request (section 8zk lists all of it; 8zh item 8 and 8zj item 2 have their
words verbatim, with their reference images described): every move on the current keys (transformation on Z, then X C V B N M; the
Haki versions replace the slots and the HUD renames them the moment Haki goes on), the stretchy moves and M1s untransformed too, a much
shorter transformation music, the transformation animation redone (keep the chest pounding), every third-person animation redone (the
punch's wobble, Bajrang Gun from above with the body turning in the air to slam a big rounded fist down, Dawn Whip scrapped and redone as
a cartoon spin after Luffy vs Kizaru, Star Gun's struck body more rubbery), Giant holding bodies properly, Rubber World bending the real
ground in rubbery waves, a white HUD, better Haki versions, Toon Force everywhere ("like a Bugs Bunny movie"); first person left alone.
The owner loves the fruit, the transformation and the clouds: read the original's code first (com/hexhaki/devilfruit), plan, ask what
is unclear, and build it carefully in its own versions.

The cloud environment is the personal "HexPiece", env_01PmnEXrwDhxzij8M5q6P6bL (freesound.org, cdn.freesound.org, tenor.com and the
rest); if freesound.org is refused, the session is on Default: switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, never melodic (check a new sound's spectrum: broadband, no pitched lines), and no
sound outlasting its move; versions roll over at 9; a push is done only when GitHub's runs are green; at 98% weekly usage write the
handoff and this prompt first, commit, push and stop without checking CI.
