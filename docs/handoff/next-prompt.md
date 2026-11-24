Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.3.md WHOLE (the newest handoff: where things stand, the Mochi rebuild done and left in
order, the owner's reference pages with every GIF link, the reference's own words for each move and what each GIF shows, how to preview
here), then docs/handoff/next-ai-3.6.0.md sections I to N (this chat's requests verbatim: the lightning made thin and thrown every way,
the Mochi list, the references, "I WANT THE OTHER MOVES, all of them, I want yours gone" and the owner's answers), then POINT ONE and
sections 0 to 7 of docs/handoff/next-ai-3.2.2.md (every standing rule).

POINT ONE (the owner's, never drop it, pass it on): every fruit in the style of the Light, Gura and Magma fruits (3D meshes, clips without
the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move of every
fruit throws the Conqueror's lightning, out of its end explosion when it has one; ONE Haki lightning in the game, the Conqueror's
release's (G), now thin (ConquerorStorm.WIDEST 0.9), zig-zagging straight out of the player level, up and down all round, in the
player's chosen Haki colour. Never add another lightning look; render any lightning change beside the release from the same camera.

Where things stand: 3.6.0 (colour pages, the one thin crooked lightning, the coat, the quieter one-at-a-time lightning sounds), 3.6.1
(Meteor Fist full power) and 3.6.2 (/hp op on/off for every new fruit and style) are released and green (Build #222, Release #153). The
Mochi fruit is being rebuilt on the owner's reference moveset (Fruit Battlegrounds' Dough and DoughV2: their VFX "literally all I want"),
committed and pushed but NOT versioned: the old moves are gone; LMB punches, Z Grilled Mochi (held), X Mochi Muddle, C Lotus Mochi
Combo, V Chestnut Mochi, B Mochi Anemone (held, over the aimed area) and N Buzz Cut Mochi (the wheel and club for now) are in; the new
donut look (lumpy, dripping, a fist filling its hole), the smooth mochi material and the arm hidden while it is mochi are in.

Do next, in order (next-ai-3.6.3.md C has the detail): (1) fix Lotus Mochi Combo (its donut is on the left side; its jab did not show),
check Chestnut, render every new move from several cameras and in first person, compare each with its reference GIF (download them as
section D says) and bring the effects toward the reference (fire arcs, embers, swirl trails, splats, star bursts); (2) clips and
first-person hand moves for the new moves; (3) DragonTests.mochiArt with the new pieces, under 16k quads; (4) remove the dead old Mochi
art and clips; (5) Buzz Cut Mochi as the owner's own cinematic (next-ai-3.2.2.md 8zzv 10, verbatim); (6) release 3.6.3 (next_version,
release notes, handoff, push, both GitHub runs green, send previews); (7) then 3.6.4: the awakened DoughV2 set on M (trident swings on
LMB, Grilled Lance, Elastic Lasso, Oblivion, Power Mochi, Blazing Storm, Scorching Buzz Cut).

The cloud environment must be the personal "HexPiece" (it allows freesound.org and the Fruit Battlegrounds wiki): if a session starts on
Default, get_environment, then switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself; never key
the head or unused limbs; keep every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0
web recording checked with tools/tonal_lines.py; versions roll over at 9; a push is done only when GitHub's runs are green; at 98%
weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
