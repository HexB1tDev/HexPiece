# HexPiece 2.5.4

Your requests after 2.5.3: the head in the sword animations, the first-person view with the sword sheathed, and the sword slash's
cooldown. Nothing here has been tested in game yet; the build's checks pass. Everything from 2.5.3 is in here too.

## The head moves freely in every sword animation
- No sword animation moves the head any more (draws, cuts, the guard, the idle, the sheathing, the charge, the release, the clash,
  the aerial moves, Yoru's and Gryphon's too). The head follows where you look, in third person and for other players.
- Everything else in those animations (the arms, the body, the blade) is exactly as it was. The check that no blade passes through
  the body or the head still passes for all 30 sword animations.
- The pirates' and marines' sword moves no longer move their heads either.

## First person with the sword sheathed
- While your katana is in its saya at your hip, first person now shows it there: the handle and guard rise from the bottom left of
  the screen, with the saya's mouth under the guard and your left hand resting on the end of the handle. It no longer shows the
  sheathed sword carried in your right hand.
- The arm is the game's own, so your skin, Armament's black hand and the Ryou aura all show on it. Under Armament the hilt is black,
  as in third person.
- Standing ready, your thumb pushes the blade a little out of the saya now and then, as in third person.
- It sways and bobs with the view as the game's hands do, and rises into view when you switch to the sword.
- Unchanged: once the blade is drawn, the hand holds the bare blade. During a swing you see the arm and blade as before.
- Yoru is carried on your back, so with Yoru sheathed both hands are empty and nothing is shown.
- If you hold something in your off hand, it stays in that hand and only the hilt shows.
- The Ryou aura on the first-person hand now stays on the arm when an animation bends the elbow (the first-person arm is always drawn
  straight).

## The Haki slash
- **The range is unchanged,** as you asked (up to 220 blocks at full charge).
- **The cooldown is 30 seconds** (it was 3). Your existing worlds pick up the new value too.
- While the Haki slash is cooling down, the charged cut still works as a plain steel cut with its own short cooldown, and the action
  bar shows how long until the Haki slash is ready again.

## Please test
- Swing, draw and sheathe in third person: does the head follow where you look?
- First person with the sword sheathed: does the hand on the hilt look right? Try it standing still, walking and with Haki on.
- The Haki slash: 30 seconds between slashes, with the steel cut in between.
- Still waiting from 2.5.3: the Fishman Karate white screen (where you select it, whether it stays white, whether Esc works behind it,
  other styles, a screenshot).
