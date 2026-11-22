Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.0.8.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8n of next-ai-3.0.8.md (2.9.6 to 3.0.8); open the older next-ai-*.md only for a
feature's detail.

3.0.8 is pushed: a drawn katana's ready pose plays under every other animation (it covered the Haki and style moves). 3.0.7: no
more sticking in the sword's stance (it held the right arm, so no punch swing). 3.0.6: Ama no Murakumo's
wave flies level (it stood upright, an arch). 3.0.5: in first person the game's own hand stays during the right click (the light at its fingertip), the held
beam's blast is 5 to 13 blocks and throws much harder. 3.0.4: X no longer lags (fewer, slower slugs, a small flash where each lands), left click is the game's punch again, V
is HexGhoul's Speed Kick (in front of them, an impact frame, sent far through blocks up to iron). 3.0.3: the first-person arm (the laser's, the sword's) comes in from the lower right pointing forward. 3.0.2: the right
click's tap is only the small laser (no blast), the held beam's blast lingers in full. 3.0.1: Z from
above slams them into the ground with an axe kick; Z lasts as long as the key is held; one's own trail is
not drawn in first person. 3.0.0: Z's kick puts the player directly in front of the one kicked, facing them; a laser-zing reflection sound from the
web; Z has a 4 s cooldown. 2.9.9: no flight for the Light fruit (only Z), and Z meeting someone is a flying kick that blasts them
away and ragdolls them; 2.9.8 held the light sword by a short grip with the ring at the fist and redrew it after the owner's reference; 2.9.7, the Light fruit after Kizaru at Egghead (no M1s at all; Z: the body becomes light, reflected off every block and
the water; C: Ama no Murakumo's lunge; X: Yasakani as at Egghead, held in the air while it pours; V: the speed-of-light kick as a dash
of light; B: clones flying in; the flight pose with the arms out, now only seen in creative flight). The next version is 3.0.9 (then 3.1.0). First check the branch's last "Build
HexPiece" and "Release HexPiece" runs, then ask the owner how the sword and the Light fruit play now, and how 2.9.5's Conqueror sounds and 2.9.4's
Haki play (both still untested), and fix whatever they report first. Then follow section 9 of next-ai-3.0.8.md.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
