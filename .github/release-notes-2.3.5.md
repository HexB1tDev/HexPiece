# HexPiece 2.3.5

Bigger sword swings, and swings you can see in first person. Nothing here has been tested in game yet. The build's checks pass
(every sword animation natural and clear, every half tick), and the cuts were rendered and checked in third and first person.

## The cuts
- **The arm is stretched.** At full reach the sword arm is now nearly straight. It used to stop with the elbow a little bent on
  every cut.
- **Longer swings.** Every cut's arc goes further both ways: the wind-up coils further back and the follow-through drives further
  through. The ticks are the same, so the blade moves faster.
- **More range.** Hits follow the blade's real path, so the stretched arm and the longer arc reach further.

## First person
- **You see the swing.** During a cut, the release, the quick draw and the quick cut, first person shows your sword arm and the
  blade sweeping across the view, the right arm only, never the left. Standing, walking, drawing and sheathing keep the game's own
  hand, so the arm isn't hanging in your view the rest of the time.

## Also
- Two recoveries (the horizontal cut's and the plunge's) no longer spin the arm once round at their end.

## Unchanged
The network protocol is the same as 2.2.1.
