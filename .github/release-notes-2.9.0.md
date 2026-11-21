# HexPiece 2.9.0

The owner's verdicts on the Light fruit and the dragon, point by point. Nothing here has been tested in game yet; every animation and
effect changed was rendered and checked frame by frame, and the build's checks pass.

## The Light fruit (Pika Pika no Mi)

- **The clones show.** Drawing a clone's skin broke the game's shared buffer, and the error wiped every Light effect on screen. The skins
  now draw in their own pass.
- **The head is never locked.** No Light animation touches the head again, and each move moves only the parts it uses. The kicks move
  only the legs and the body, the laser only the right arm, and the idle stance only the arms (hands in pockets).
- **Jumping and flying:** a jump is the game's own jump, hands in pockets. Only real flight has a pose: upright, leaning a little when
  you go fast, never laid flat.
- **Ama no Murakumo** is held at the bottom of its handle, low at your side, pointing ahead. The blade leans toward the line of the arm,
  so every swing sweeps it. The five cuts are new: a level slash, a backhand, a diagonal down, a rising cut, and an overhead finisher with
  a lunge and the wave. **In first person** the game's hand steps aside while you cut, guard or draw the sword, and your arm and the
  sword sweep across the view, posed exactly as in third person.
- **The laser leaves your fingertip,** where the hand really is every frame, in first and third person; it no longer drifts as you
  move. Aiming it in first person shows your pointing arm.
- **Light explosions are real explosions:** the light gathers for a moment, then a flash, a dome and a ring over the ground, a golden
  ball, and gold-lit dust that hangs and thins over 2 to 3 seconds. The quick laser now sets one off where it strikes.
- **Yasakani no Magatama** fires one bullet every other tick (about 25 in all, down from over 200).
- **Damage is cut** across the fruit, about half or less.
- **Speed of Light Kick (V):** you vanish in light and reappear behind the target, over their left shoulder, at the height of their
  head. You spin a full turn, and the kick lands in the left side of their head, throwing them off to their right.
- **Egghead:** Yata no Kagami (Z) bends off what it meets: ground, walls, and water, which throws you up into the air. Up to three
  bounces in 60 blocks, stopping beside anyone in its way. Sneak+Z goes straight where you look. **Sprinting while flying turns you into
  light,** about 40 blocks a second; your body becomes a streak of light.
- **Every Light sound is a web recording** (CC0, Freesound.org, each credited in sounds/piece/pika/SOURCES.txt): lasers, charges that
  hum for as long as you hold, beams, explosions, teleports, kicks, a sword of light that ignites, hums, swings and goes out.

## The dragon (Uo Uo no Mi)

- **The body follows its own path.** The head turns first, and the body curves behind it along the way the head went, the tail coming
  round last, up and down as well as sideways. It no longer swings round like a stick.
- **Slower flight:** creative flight's pace or a little under (0.07 before, 0.04 now).
- **Blast Breath:**
  - The charge sound builds for its whole length (8.5 s), then holds for as long as you keep charging, and fades when you let go.
  - No fire breath or bite while charging or while the beam burns.
  - The beam barely digs; the final explosion is the big bomb: a blinding flash, a fireball that climbs into a mushroom cloud, a shock
    ring of dust far over the ground, and burning ground round its crater. Smaller explosions keep going off round the rim for about
    3.5 s, and the smoke takes 9 s to thin away.
- **Less lag:** the fire effects use a quarter of the geometry when they are not filling the view.
