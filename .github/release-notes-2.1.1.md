# HexPiece 2.1.1

The first HexPiece release, built from HexHaki 2.1.0 with the HexGhoul workflow (pre-release per branch, releases from release notes,
only the newest 5 releases and workflow runs kept, versions rolling over at 9). Nothing here has been tested in game yet.

## Set sail (the start menu)
- On first join a pirate-styled menu (a parchment scroll over a dusk sea, a ship sailing past) asks: **Male or Female**, **Pirate or
  Marine**, and your **fighting style**. Female players are drawn with the female body from HexGhoul (slim arms, hips, a bust made from
  your own skin, hidden under a chestplate). An operator can let a player choose again: `/hexpiece reset`.

## Fighting styles
Four bare-handed styles, each with a four-blow M1 string, a right-click signature and two specials (Y and U) that open with style mastery:
- **Black Leg** (hands behind the back): Snap Kick, Spinning Hook, Low Sweep, Mutton Crush; Neck Shot; Flame Leg Whirlwind; Sky Walk Drop.
- **Six Powers**: Finger Pistols, Storm Leg Slice, Iron Palm; Shave; Air Blade; Iron Body.
- **Fishman Karate**: Shark Palm, Back Fist, Rising Knee, Thousand-Brick Fist; Water Bullets; Shark Skin Counter; Five-Thousand Brick Wave.
- **Iron Fist**: Jab, Cross, Hook, Uppercut; Haymaker; Gatling Jabs; Shoulder Charge.

M1 and the right click only throw style moves with an empty hand, outside Devil Fruit mode, and not while you aim at a block (you can
still mine and open doors). Every blow is choreographed (wind-up, strike, follow-through) and lands through HexHaki's own impact system:
hit-stop, shake, flash and launch. Blows that land raise the style's mastery (up to 1.6x damage).

## Pirates and Marines
- Patrols of pirates and marines (grunts, officers, captains; men and women) roam the overworld, fight each other, and attack players under
  the other flag. They fight with the same styles and moves players do.
- Beating the other side raises a pirate's **bounty** (Rookie ... Emperor of the Sea) or a marine's **merit** (Seaman Recruit ... Fleet Admiral).
- Operators: `/hexpiece spawn pirate|marine [0-2]`, `/hexpiece patrol pirate|marine`.

## The Captain's Log (new mastery menu)
The mastery key now opens a blue-and-white log: a white-sailed ship flying your flag, your wanted poster (or marine service record),
Haki mastery, your style's moves and mastery, your Devil Fruit. Its buttons still open the Ability Book (binds) and the Haki techniques.

## Haki
- New lightning everywhere: the Conqueror's bolts arc out over the ground, crackle and fork (the same shape the server's stuns use); every
  lightning beam in the mod is crackled, tapered, struck over a tick, forked and flickers into a new shape.
- Hand-made animations: Armament activation, Observation, Advanced coating, Conqueror's release, Haki Leap charge and release, Haki Grip.

## Every animation
All 106 animations reworked. 51 of the originals came apart in game (torso off the hips, head off the neck, arms off the shoulders, hands
through the head): all fixed, and the build now refuses any animation that comes apart. Plus in-out easing, snaps with a settle, wind-ups,
weight and stances, counter-swings and breathing.

## Removed
- The Imu fruit (Devil Devil fruit): its last traces are gone from the code; its assets (Black Soul texture, model, name) are kept for later.
