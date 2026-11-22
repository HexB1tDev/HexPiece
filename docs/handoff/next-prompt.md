Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.2.0.md whole: it holds every standing rule in one place (talking to the owner, the house
rules, clean moves and animations, keeping lag to a minimum, rendering on the owner's Android phone, sounds only from the web and
never made by hand, the Haki lightning style, what must never come back), where things stand and what to do next. The full history
with the owner's verbatim messages is in docs/handoff/next-ai-2.7.0.md (2.7.0 to 2.9.5) and sections 8b to 8zf of next-ai-3.2.0.md
(2.9.6 to 3.2.0); open the older next-ai-*.md only for a feature's detail.

POINT ONE of the handoff (the owner's, never drop it, pass it on): every fruit is made in the same style as the Light, Gura and Magma
fruits (their 3D meshes, clips without legs or head, first-person hand moves, CC0 sounds); and with the Conqueror's Haki and Ryou on,
every move of every fruit throws the Conqueror's lightning (visual only), out of the move's end explosion when it has one (not optional).

3.1.9 (sections 8zc, 8zd, 8ze) is the Hie Hie no Mi, Aokiji's ice fruit: RMB Partisan, Z Ice Time, X Ice Ball, C Pheasant Beak, V Ice
Age, B Ice Road + ice fist, N Ice Saber, the ice body, walking on water, no cold damage. The owner saw the first renders (every piece of
ice a pointed crystal) and said: "They're just like crystals ... Looks weird ... The ground can also turn into like actual ice blocks,
bro. I'm not stopping you from using Minecraft stuff"; asked, he chose real Minecraft ice blocks round frozen bodies (server/HieFreeze,
through server/Scars, put back afterwards), real ice ground for Ice Age and the road (packed ice, the sea frozen, ridges, back after
about 20 s), and smooth glassy ice (hie/HieArt: lathes and lumps, no crystal facets) for everything thrown or held. Never bring the
crystal look back.

3.2.0 (section 8zf) is 8za's item 3: the "Devil Fruits" Creative tab, rarity ranks (Mythical: the Nika fruit and the Uo Uo no Mi;
Legendary: Gura, Pika, Magu, Hie; the owner's choice) in the fruits' names and tooltips, and the fruit energy bar over the food bar
that every move of HexPiece's fruit kits spends (server/FruitEnergy; Gear 5 not yet).

Next: check 3.2.0's runs are green if the last session did not; ask the owner how 3.1.5 to 3.2.0 play (the ice fruit's real ice, the
energy costs and refill, whether Gear 5 should spend energy) and do what they ask.

The cloud environment is the personal "HexPiece" (env_016v9rNSffz2fds2ha514nhY); if tenor.com or freesound.org is refused, the session
is on Default: switch_environment to it.

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; never claim in-game testing;
render and judge every animation and effect yourself, third and first person; never key the head or unused limbs; keep every effect
cheap (about 16k quads at most, capped counts, no per-frame allocation, one small message and the clients build the rest); on the
owner's phone everything alpha-blended and depth-tested, never additive, never a framebuffer blit; every sound a CC0 web recording
(tools/web_sounds.py), never synthesised or mixed, and no sound outlasting its move; versions roll over at 9; a push is done only
when GitHub's runs are green; at 98% weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
