# HexPiece 2.3.2

Your sword bugs, and the finisher's line on the victim's screen. Nothing here has been tested in game yet: the build's checks and
tests pass, and the sword animations were rendered and judged.

## Gryphon and Yoru
- **Gryphon's pull-out fixed.** Every blade used to slide out of its saya along one fixed curve. Gryphon's saber curves three times
  as hard as a katana, so its hidden part came out through the scabbard's side mid-draw. Now each of the 27 blades slides along its
  own curve, measured from its own model.
- **Every sword plays the same animations**, as you asked: the regular katanas, Gryphon and Yoru all share one set of cuts, guard
  and idle. Only Yoru has its own draw, sheathing and draw into the charge, because it comes off the back, not the hip.
- **Yoru is never on the back and in the hand at once.** A legendary blade is drawn the moment combat starts and never drawn again
  mid-fight. The draw cuts and the quick draw play their drawn versions with Gryphon or Yoru, and the quick draw waits for the
  legend's own draw to finish.
- **Yoru's draw rebuilt.** The hand reaches up behind the head to the hilt and pulls the great sword up out of its ropes. The hand
  takes it as it hangs, edge out, and turns it over as it comes off the back. The point swings out behind, up over the head and
  down in front, all on the right side, never across the body. The sheathing is the same arc backwards, the sword held the same way.
- **Yoru on the back** now hangs with its hilt out over the right shoulder, beside the head, not behind it, and the ropes hold it
  there.

## Slower draws
- A katana's draw takes 28 ticks (it was 22) and its sheathing 24 (20), Gryphon's the same. Yoru's are the slowest: 44 and 36.
- After the draw the blade whips out forward first, then swings out to the side. It used to swing straight across the body.

## The body is free while holding a sword
- **On the move**, the guard holds only the sword arm. The head looks where you look, and the body and the left arm swing as you walk.
- **Standing still**, an idle takes over: you breathe, shift your weight, the free arm hangs and moves, and the blade comes alive in
  the hand, turned to the light and flicked. The head always stays yours.

## No more twisted arms
- Every sword animation was solved again with a natural arm: the shoulder turns no further than a shoulder does, and the elbow
  folds the right way and never shut. The wrist bends only as far as a hand holding a sword can, no arm passes through the body, and
  the sword, its handle and guard included, never passes through an arm, the body or the head.
- The build now checks all of this on every sword animation (the shared set and Yoru's own), every half tick, and fails if one
  breaks it.

## The finisher, seen by its victim
- The cut's red line runs down the middle of your screen, from top to bottom.
- When your body comes apart, so does your view: the picture splits in two along the line, and the halves fall away from each
  other.

## The NPCs come alive
Your request for HexGhoul's ambient animations: pirates and Marines standing idle now and then do something with their body,
in HexGhoul's shapes. They shade their eyes with a hand and look out to sea one way, then the other. They fold their arms, stretch,
yawn, crack their knuckles, shrug, rub their chin, laugh, or tap a foot. Marines also salute and stand at ease, hands behind their
backs. One comes every 6 to 18 seconds while nothing is happening, and it stops the moment they spot a foe.

## Also
- The NPCs' new skins and blinking (from the last work session) are in this build.

## Unchanged
The network protocol is the same as 2.2.1.
