Continue HexPiece (repo HexB1tDev/HexPiece, branch claude/nice-bell-gam7qf: fetch it first, it is the newest).

Read CLAUDE.md, then docs/handoff/next-ai-3.6.0.md WHOLE (the newest handoff: every request of the last chat verbatim, what 3.5.9 built,
what 3.6.0 has built so far, exactly what is left and in which order, how to build, render and test here), then sections POINT ONE and
0 to 7 of docs/handoff/next-ai-3.2.2.md (every standing rule: talking to the owner, the house rules, clean moves and animations, lag,
rendering on the owner's Android phone, sounds only from the web and never made by hand, Haki and lightning). The full history with the
owner's verbatim messages is in next-ai-3.2.2.md sections 8b to 8zzx (8zzv holds this chat's requests numbered 1 to 12, 8zzw 3.5.9 as
built, 8zzx 3.6.0 in progress).

POINT ONE (the owner's, never drop it, pass it on): every fruit made in the style of the Light, Gura and Magma fruits (3D meshes, clips
without the head or unused limbs, a first-person hand move per move, CC0 web sounds); with the Conqueror's Haki and Ryou on every move
of every fruit throws the Conqueror's lightning, out of its end explosion when it has one; and there is ONE Haki lightning in the whole
game, the Conqueror's release's (G), crooked as 3.6.0 made it (ConquerorStorm.jagged / strike for every other maker), drawn in the
player's chosen Haki colour. Never add another lightning look; render any lightning change beside the release from the same camera.

Where things stand: 3.5.9 is released and green (Build #216, Release #150): hold R turns every power off but fruit passives, Meteor
Fist one size, Blue Hole remade, Galaxy Impact lighter, impact frames only on clashes (for both fighters), WiFi Haki a Swordsman's on K
with its slow partial draw and lightning from the sheath's gap, the Galaxy Fist and the Haki Grip removed. 3.6.0 is on the branch, NOT
versioned or released: the Haki colour and Ryou colour pages on the start menu (existing players get them once), one crooked Haki
lightning everywhere (coated hits included), the coat past the elbow with a ragged edge, the arm glow fading up the arm. The new
bass-heavy lightning recordings are cut and checked (sounds/piece/lightning/web, tools/lightning_sounds.py) but NOT wired in.

Do next, in order (next-ai-3.6.0.md section F has the detail): (1) wire the lightning sounds (sounds.json: conqueror_release ->
strike1..3, the original's armament_ambient_zap -> zap1..3 and last_stand_lightning -> thunder, all quieter; build.py RENAMED[SOUNDS];
one lightning sound at a time on the client so the full release is heard as one), then release 3.6.0 (next_version, release notes,
handoff, push, both GitHub runs green) and send the owner the colour pages and the seven-colour lightning renders (previews, not the
game); (2) ask the owner the two open questions (the Quake ultimate's own impact frame; Meteor Fist's growing speed and blast); (3)
make every new fruit and every style obey /hp op on/off; (4) the owner's Mochi list (ask for the end of their cut-off line about the
rolling donut, with its picture, first); (5) Buzz Cut Mochi: references first, then plan, then build as one uninterrupted cinematic.

The cloud environment must be the personal "HexPiece" (it allows freesound.org): if a session starts on Default, get_environment, then
switch_environment to HexPiece (network_only, its switch_statement copied exactly).

The rules that matter most: answer in English and do exactly what the owner asks, nothing unasked; write every new request verbatim into
the handoff the moment it arrives; never claim in-game testing; render and judge every animation, effect and menu yourself; never key
the head or unused limbs; keep every effect cheap (about 16k quads at most); everything alpha-blended, never additive; every sound a CC0
web recording checked with tools/tonal_lines.py; versions roll over at 9; a push is done only when GitHub's runs are green; at 98%
weekly usage write the handoff and this prompt first, commit, push and stop without checking CI.
