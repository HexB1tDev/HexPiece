# HexPiece 2.1.3

Ryou, R between your style and your fruit, and natural arms for Fishman Karate and Iron Fist. Nothing here has been tested in game
yet.

## Ryou (was Advanced Haki)
- **Renamed** to Ryou everywhere you read it: the Haki techniques and their descriptions, the Ability Book, the key list, the sound
  subtitles. Its unlock (850 Armament + 850 Conqueror mastery), mastery and power are unchanged; saves, configs and network data keep
  their old identifiers.
- **Ryou replaces regular Armament once unlocked**: switching Armament on (Ctrl + R) brings Ryou with it, off takes it off. **J is
  empty** for now.
- **Your own glow colour**: the first time you have Ryou unlocked, the server gives you one random bright colour and saves it with
  your player data for good (logging out, dying, changing dimension, toggling Haki or restarting the server never change it). Players
  who already had the unlock get theirs once. Everyone who sees you sees your colour.
- **New look**: blackened hands with a strong, clean rim of your colour around their silhouette, a faint glow falling off around it,
  fading out up the wrist, pulsing gently, flaring when Ryou comes on. It follows the hand through every animation (bent elbows too),
  in first and third person, is hidden by walls like anything else, and needs no shader pack. The red aura over the arms and **all the
  fire** (the flaming fists, the embers while idle and moving) are gone; glints and light trails in your colour instead.
- **Smaller physique**: Armament and Ryou no longer inflate you (arms up to 1.4x wide and pushed 3 px out, a chest 1.55x wide);
  now a hint of muscle that keeps the normal silhouette, with normal and slim arms, armour and first person as before.

## R: your style or your fruit
- **No fruit**: tap R for combat mode (as before).
- **With a fruit eaten, R swaps between your fighting style and your fruit.** Gomu: R leaves combat mode for Devil Fruit mode and
  back; **Gear 5 moved from R to Ctrl + R** in Devil Fruit mode (Ctrl + R is the mode's own power: Armament or Ryou in combat mode,
  Gear 5 in Devil Fruit mode). Pika: in combat mode M1 and the right click are your style's, otherwise the fruit's; its bound
  abilities keep their keys. The X key still switches Devil Fruit mode as before.

## Fishman Karate and Iron Fist
- **No more permanent guard**: in combat mode your arms hang and swing naturally when idle, walking or sprinting. Every blow raises
  them from wherever they are for its wind-up, strike and recovery, then lowers them smoothly back.
- **Your legs keep walking**: punches no longer freeze the legs, so you can walk and sprint through them (the Rising Knee, the
  Five-Thousand Brick Wave and the Shoulder Charge still use their legs). Six Powers' finger pistols and palm do the same.
- **Nothing gets stuck**: a move cut short by death, taking an item in hand, using an item, swimming, climbing, mounting, leaving
  combat mode or a style reset is let go at once, its animation released for everyone.
- Pirates and marines blend their blows the same way (their legs keep walking through punches).

## Note for servers
The network protocol changed: 2.1.3 clients and servers only connect to each other.
