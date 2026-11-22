# HexPiece: rules for every session

Read the newest `docs/handoff/next-ai-*.md` first: it holds what was done, what is open and what to do next. Answer the owner in English.

How this repository works (same model as the owner's HexGhoul repository): the original Forge 1.20.1 mod jar `input/hexhaki-2.1.0.jar`
(mod id `hexhaki`) is the only source of the original code. `python3 build.py` patches it with ASM (`patch/PiecePatch.java`), compiles the
new code in `src/main/java/com/hexhaki/piece/` against it and the compile-only API stubs in `stubs/`, assembles
`build/hexpiece-<version>.jar`, proves every new Minecraft/Forge reference (`patch/PieceVerify.java`, `verify/*.txt`) and runs the tests.
Minecraft, Forge and their mavens cannot be downloaded in the cloud sessions; GitHub raw files and Maven Central can.

RULE: versions. The owner: "whatever reaches 9 switches to the upper number". A version's last number is 0 to 9: after x.y.9 comes
x.(y+1).0, after x.9.9 comes (x+1).0.0 (`tools/next_version.py`; `VersionTests` and `build.py` fail otherwise). One version per push that
the owner should test. `build.py`'s `VERSION` names the jar `hexpiece-<version>.jar`.

RULE: releases. Every push to a `claude/**` branch publishes the jar as the pre-release `build-<branch>`. Adding or changing
`.github/release-notes-<version>.md` publishes `v<version>` as the latest release; after it, only the 5 newest releases and the 5 newest
workflow runs are kept (the owner: keep 5 versions, delete the older ones).

RULE: a push is done only when GitHub says so. After every push, check the branch's "Build HexPiece" run (and "Release HexPiece" when release
notes changed) ends green. A local `python3 build.py` EXIT 0 is not enough on its own.

RULE: one style for every fruit, and Haki lightning out of explosions (the owner: "always tell the other AIs like that they are to
follow this same exact idea"). Every fruit's models, effects and animations are made exactly like the Light, Gura and Magma fruits'
(3D meshes as FireMesh/GuraArt/MaguArt build them, clips with no leg or head keyed, a first-person hand move per move, CC0 sounds).
With the Conqueror's Haki and Ryou on, every move of every fruit throws the Conqueror's lightning (visual only, no stun): a move that
ends in an explosion throws it outward from that explosion, not from its beam ("not optional"). The handoff keeps both as POINT ONE.

RULE: the Imu fruit (the Devil Devil fruit) is gone for good. The owner: "Get rid of the Imu fruit (Devil Devil fruit) Get rid of it ENTIRELY,
but keep it's assets, for future use on other things." Its assets (`textures/item/black_soul.png`, `models/item/black_soul.json`, the
"Black Soul" name) stay in the jar, unregistered; `build.py` (`check_imu`) fails if any code, text or file name names it again.

RULE: judge your own renders (the owner's rule from HexGhoul: "the animations always have to be top tier, no excuses whatsoever"). Every
animation, pose, menu or texture you change, render it (`tools/anim_preview.py`, `tools/ui_preview.py`, `tools/lightning_preview.py`),
look at it yourself, fix it and render again until it is right. Never claim something was tested in game: only the owner can.

RULE: server safety. Client-only code lives in a `client` package; `PieceVerify` fails if a class elsewhere names a client class outside a
lambda, or a client event subscriber is not registered for `Dist.CLIENT`.

RULE: never alter the owner's files (the input jar, their textures and sounds) in place: changes go through the patch or the expansion.
Original animations may be replaced by reworked ones under `src/main/resources/assets/hexhaki/player_animation/`.

RULE: stopping (the owner: "It's when you reach 98% weekly that you stop ... you release the handoff in prompt first, and then you
push the update to GitHub without checking"). The moment the weekly usage reaches 98%: write the handoff (`docs/handoff/next-ai-*.md`)
and the next prompt (`docs/handoff/next-prompt.md`) FIRST, commit everything, push, and stop. No more checks, no CI wait. Keep the
handoff current while working so this takes one commit.
