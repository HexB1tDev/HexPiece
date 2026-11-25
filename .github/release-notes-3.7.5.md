# HexPiece 3.7.5

Your bug list after 3.7.4, all seven items. **Not tested in game yet:** the build and all 13 test suites pass, and every new effect
was rendered and checked in previews.

## 1. Buzz Cut (Mochi)
"During every buzz cut mochi the head is not meant to spin or rotate when looking around"
- During every Buzz Cut the player's head is held still, facing the way it faced when the move began. Looking around moves only
  your camera, not the head.

## 2. No effects for the human (Tori Tori no Mi, untransformed)
"No vfx is showing with the tori tori no mi (untransformed)"
- Found the cause: the Phoenix's effects were only drawn while someone was in phoenix form, so the human moves (Fujiazami, Talon
  Slam, Phoenix Inferno, Bluebird and their wings) drew nothing. They now draw whether or not anyone is transformed.

## 3. The phoenix's walk
"the body is hobbling too much, its meant to be imposing"
- Much calmer walk: the body barely bobs, sways or rolls; it walks tall and steady with a slower, longer stride. The head moves
  just a little with each step.

## 4. X as the phoenix: Fire Tornado
"from now on X transformed becomes a massive tornado like the dragon fruit but bigger and made of beautiful flames (blue and
yellow), causes burn and damage + carry stun"
- The phoenix sweeps its wings forward and a huge tornado of blue and gold flame rises 4 blocks in front of it and runs on the way
  you looked (35 blocks). It is 18 blocks tall, bigger than the dragon's (10).
- Whoever it takes is carried round and up inside it (they can't get out), burnt every half second and set on fire, then flung out
  at the end.
- With the Conqueror's Haki and Ryou on, it is 1.4 times bigger and throws the Conqueror's lightning out of its funnel.
- No more ram, and no more 360 spin.

## 5 and 6. The fire, the explosions, no more spins, one Bluebird
"I dont like the fire particles from the moves." "Explosions and slams from moves lack real explosion ... the moves should never make
the bird spin a 360 and the B move should be 1 projectile that makes a massive explosion in the shape of a bird."
- All the small flame tongue particles are gone from the Phoenix's moves. Every move's fire is now made the same way as the dragon
  fruit's: 3D fire clouds, fireballs and real explosions (a white flash, a ball of fire thrown out that turns to smoke, a shock ring
  over the ground, sparks and rocks flying), in the phoenix's blue and gold.
- Talon Slam, the Phoenix Inferno dive and the end of Fujiazami each blow up in a real explosion.
- No more 360s: Hawk Rush now grabs, carries the target under its claws for a moment and throws them in an explosion. The phoenix no
  longer spins in Phoenix Inferno's whirlpool; it holds its wings spread.
- Bluebird is ONE ball in both forms (the phoenix's is bigger: 22 damage, 7-block blast). It charges and flies as a sphere of blue
  and gold fire. When it hits, it blows up and a giant phoenix of blue flame bursts out of the explosion: wings spread about 15
  blocks across, feathers of flame, gold-tipped tail, beating its wings once and rising as it burns away.

## 7. Horo Horo no Mi projection
"should not allow the arms to wobble when moving ... and should adopt the player model of the player (its always assuming the player
is steve model)"
- The projection's arms no longer swing when you move in any direction (they only move when you punch).
- The projection now uses your own skin's model: slim (Alex) arms when your skin is slim, wide (Steve) arms otherwise.
