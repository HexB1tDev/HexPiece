# HexPiece 2.3.6

Clashes. When two blows meet, both stop dead, an impact frame flashes on screen and both fighters are knocked apart. Nothing here
has been tested in game yet. The build's checks pass, and the impact frame, the lightning burst and the recoils were rendered and
checked.

## When blows clash
- **Every move can clash.** Every fighting-style blow (Black Leg, Six Powers, Fishman Karate, Iron Fist) and every sword cut (the
  combo cuts, the charged cut, the quick draw), a player's or a pirate's or marine's, meets an opposing blow when both strike at
  about the same moment (within 3 ticks of each other), within reach, facing each other. A blow landing while the other fighter is
  still winding up is a normal hit, so fights aren't all clashes.
- **Both stop dead.** Neither blow lands. Both fighters are held still for a few ticks (the hit-stop), then thrown apart. The weaker
  blow's fighter is thrown further, and a blow twice as hard or more sends the other flying. A hard clash shakes the ground.
- **Knocked back.** Each style has its own recoil: the fist bounces back to guard the face and the free arm swings out for balance,
  the body leans away and steps back, then the stance comes back. Black Leg's kicking leg is thrown back down (the hands stay behind
  the back); Six Powers keeps its left hand behind its back. The sword is knocked up over the shoulder, then comes back down to
  guard (in first person too). Neither fighter can attack again until the stagger has passed (about 0.6 s, longer if overwhelmed).
- **Swords need Haki.** A bare fist or kick can only meet a blade with some form of Armament Haki on. Without it the blade comes
  through: the bare blow is lost and the cut lands. Two blades always clash.
- **Sound.** Your own clash sounds from HexGhoul (Clashes.zip), with steel ringing when a blade is in it.

## The impact frame
Everyone near a clash sees it on screen, under the HUD (the hotbar and bars stay readable):
1. **The negative**: the whole view inverted and its contrast pushed, ink focus lines rushing in on the point the blows met, a
   black star burst on it.
2. **The black frame**: the view gone black, white rays torn out of the point.
3. **The release**: the view back, a white flash fading off it, the last lines fading.

It lasts 2 ticks for an ordinary clash and up to 4 for the hardest, then fades over 2 more. The lines are redrawn every half tick,
so they flicker like hand-drawn frames.

## Ryou meets Ryou
When both fighters have Ryou on, both lightning colours tear out of the clash in a sphere, like the Conqueror's release but much
smaller: 3D inked bolts in each fighter's colour, by turns, up into the air and along the ground. The black frame's rays come in
both colours too.

## Where the blows meet
The flash, the shock rings and the sparks appear where the blows actually meet: halfway between the fighters, or where the shorter
reach ends (a fist meeting a longer blade meets it at the fist). Steel throws orange sparks; bare blows a burst of light, and dust
off the ground when it shakes.

## Network
The network protocol is now 5 (2.3.5 and older are refused at login, so both sides must update).
