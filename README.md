# HexPiece

Forge 1.20.1 mod (mod id `hexhaki`) built from the supplied `hexhaki-2.1.0.jar` (HexHaki: Armament, Observation and Conqueror's Haki,
Devil Fruits, gore). The original Gradle project was never available, so this repository contains the **exact input JAR**, a narrow,
verified bytecode patch (`patch/PiecePatch.java`) and a separately compiled expansion package (`com.hexhaki.piece`), the same way the
owner's HexGhoul repository is built.

## Build

```bash
python3 build.py
```

Needs Java 17+ and Python 3 with Pillow for the preview tools; no downloads. Output: `build/hexpiece-<version>.jar`.

Previews (judge every animation you change): `python3 tools/anim_preview.py out.png anim.json ... [--turnaround]` (Minecraft's boxes,
front to back), `python3 tools/ryou_preview.py out.png` (Ryou's hand rim, z-buffered), and the style GIFs: `javac -cp build/classes -d build/preview tools/preview/StyleDump.java`,
`java -cp build/preview:build/classes StyleDump > build/preview/styles.json`, `python3 tools/style_gif.py`. The Swordsman:
`python3 tools/sword_anims.py` (re-solves and writes every sword animation, `BladePaths` and the NPC poses; `--only a,b` for some)
and its contact sheets with the blade and saya drawn (`sword_anims.preview`); the 3D Haki lightning, rasterised from the game's own
quads: `javac -cp build/classes -d build/preview tools/preview/BoltPreview.java`, `java -cp build/preview:build/classes BoltPreview <dir>`;
the start menu and the Captain's Log:
`javac -cp build/classes -d build/preview tools/preview/UiPreview.java`, `java -cp build/preview:build/classes UiPreview <dir>`.

## Releases

- Every push to a `claude/**` branch publishes the jar as the pre-release `build-<branch>`.
- Pushing `.github/release-notes-<version>.md` publishes `v<version>`; afterwards only the newest 5 releases and 5 workflow runs are kept.
- Versions: after x.y.9 comes x.(y+1).0 (`tools/next_version.py`).

## What HexPiece adds

- A start menu (Male/Female as black silhouettes, Pirate/Marine, a fighting style) with a pirate UI; female players drawn with HexGhoul's female body.
- Five fighting styles (Black Leg, Six Powers, Fishman Karate, Iron Fist, Swordsman), each a choreographed M1 string and a right-click
  signature (the four unarmed ones also two specials, Y and U), with style mastery. They fight in combat mode (R, as in HexGhoul; with a fruit eaten R swaps the style and the fruit;
  Ctrl + R is the mode's power: Armament or Ryou, Gear 5), with Photon particles: air cut by every swing, the Air Blade's flying
  crescent, Fishman Karate's water.
- The Swordsman (2.1.4): the owner's 25 katanas and their saya as items with grades, rarities and descriptions, worn at the hip;
  right-handed draw cuts and 12 more cuts in standing, moving and aerial strings, sheathing, a charged cut or Haki slash in the Ryou
  colour (2.2.4: a torn energy wave rising out of the ground, as the manga draws Mihawk's; 2.2.6: drawn as flowing light, not a solid) that cuts a trench as wide as its body,
  the Ryou finisher, and Ctrl + M1's quick draw.
- Pirate and Marine NPC patrols (pirates also with katanas) that fight each other and the players of the other flag with the same moves; bounty and merit ranks.
- The Captain's Log (2.2.4: the one menu, the original's Ability Book, Haki Techniques and Gear mastery screens gone into it):
  overview, Style & Haki (moves, techniques, quick slots), Devil Fruit (moves, quick slots, the Nika fruit's upgrades).
- The Conqueror's full-power release (2.2.5): every full charge is the JoyBoy one; enormous black bolts with a rim of the Ryou colour
  tear out of a flash in every direction, and where they strike the ground it splits into fissures that grow back on their own
  (HexGhoul's self-repairing terrain).
- Haki lightning as real 3D lightning (2.1.5; 2.2.6: lit 3D tubes, every stroke inked, needle-sharp tips, tearing out of a shell round
  the player, never out of the body): crackled, with a black ink outline, a coloured core white-hot down its middle, black bodies
  edge-lit in the Ryou colour, and a glow, striking, flickering, dancing and thinning away; Haki lightning off the katana as it is
  drawn, charged and released.
- Every animation reworked (and linted so none comes apart); the Imu fruit removed (its assets kept).
- Ryou (the original's Advanced Haki, renamed): it comes with Armament once unlocked, in the player's own saved glow colour, as black
  hands with a coloured rim (no fire, no inflated physique). 2.2.4: every player has that colour from their first login, and all
  their Haki lightning and their Haki slash wear it.

## Changes

See `.github/release-notes-*.md` for each version and `docs/handoff/` for the state of the work.
