# HexPiece: start here (written at 3.8.1; the session after 3.8.0)

Read CLAUDE.md, then this file whole, then docs/handoff/next-ai-3.7.6.md (sections 1 to 8: where things stood at 3.8.0, the
Leopard, every standing rule of the last days), then the older handoffs it names as needed.

## 1. The owner's requests this session (verbatim, with a GIF in a zip and a picture)

The owner sent `Haki.zip` (one GIF, 382 x 216, 157 frames at 50 ms: the anime's Conqueror's Haki clash, Luffy against Kaido and Big
Mom: red and blue Haki meeting between them, a black ring round a bright sphere at the point where they meet, red lightning from one side
and blue from the other, the pressure wave, everyone round them blown away). Not committed (the owner's media); read frame by frame in
the scratchpad. And a picture: an aerial photo of a small round tropical island ringed by a wide white sand beach, a reef and
turquoise shallows round it, deep blue sea beyond (the look wanted for the islands: not committed).

The owner, verbatim:
"Using the very latest build/action/release.

To do list:

1. Reduce Conqueror's Haki Release (g) end lag, when releasing the full charge of haki it causes a ton of lag for a bit then stops.
2. New, Conqueror's Haki Clash from now on whenever 2 players release conqueror's Haki (charged G for at least 2 seconds within
eachother's Haki release range) they collide creating a massive pressure wave between the 2 players lightning of both parties flying
outwards a massive impact in the precise point of collision like the gif shows (that ring on the point of impact), Then a mini game
begins, Whoever has more Conqueror's Haki Mastery has way more ease in this game, a bar appears on the middle of the screen "TAP M1"
whoever manages to get their bar color to the enemy's side wins, the bar has to be really well made and decorated. Note: During the
clash wind is blowing outwards from both players like they're in 1 single massive spherical bubble anything or anyone going close to
this perimeter is blasted away in a ragdoll in a very violent way (go through blocks as well in a horizotal tragectory), at the end
whoever wins sends the other player flying also in a horizontal tragectory, ragdoll and stunned for double the winner's max stun of the
conqueror.
3.  World Generation, big one,  the world now will have our own generation, it supports TerraBlender and Biomes o plenty, the world
will now generate in Arquipelagos (big islands but most of it will be sea) key is that every single river IS CONNECTED TO THE SEA no
matter what, it's meant to be made for a boat travel/exploration type of thing. Make sure Structures spawn in the islands and the SEA
is deeper than normal. (should support every modded structure) and every island MUST have a beach, long.
4.  I want a proper clashing system like our other mod has HexGhoul."

- "Using the very latest build/action/release": the newest was v3.8.0 = the branch's head 91344f3 (the 3.8.0 handoff); built on it.
- The order chosen (each its own version, the owner tests each): 3.8.1 item 1 (the release's lag), then item 2 (the Conqueror's clash),
  item 4 (the clashes as HexGhoul's), item 3 (the world generation).

## 2. The owner's second message this session (verbatim, with two pictures and a zip)

Sent while 3.8.1 was being built (the session's worker restarted under it; nothing was lost). The zip `NewModels.zip` (as sent, never
altered: `input/newmodels/NewModels.zip`) holds three Blender files: `First_gun.blend`, `First_mace.blend`,
`Clothes_Replacements.blend`. The pictures (not committed; described): (1) the anime's Sanji in a blue striped shirt holding a flintlock
pistol out one-handed at arm's length, right arm straight, the gun level (subtitle "Don't you mock me!"); (2) the anime's Nami aiming a
big-bored flintlock pistol one-handed straight at the viewer, the barrel huge in the foreground. Both: the one-handed gun pose wanted.

The owner, verbatim:
"First of all setup a auto wakeup 5 hours from now.

Reworks:
(Only after the world and conqueror haki are done)

Swords in first person are being weilded in a very strange way and unrealistic, I dont Like any of it,  from now on, when grabbing a
blade as a swords man you'll go into third person, forcefully, the camera adjusts to better aid your swings and aim.

Different Animations for Different Weapons.

Katanas Category (stays as it is)
Saber Caterogy (Similar to Katanas but more aggresion, the idle and unsheath animation stay as it is (Gryphon))
Mace Category (Right handed, rests on the back like Yoru, the pull out is slow, when idle the player rests it on it's right shoulder,
has range, hits ragdoll but are slow when cocking them, fast on the swing).
Yoru Category (Yoru is a one of a kind legendary Blade, stays as it is, and is uncontested the strongest blade that will ever be added)
Gun (Pistols, usually held with one hand (right) in a badass pose, all weapons only shoot 1 bullet at a time, and auto reloads after,
bullets are also the same model from now one (I'll send it) when haki is on, bullets come out black (pure black), those bullets are
much much faster and go through blocks (10 blocks max), simple reload animation, put 1 bullet in the hole of the barrel).

Whenever I give you a weapon from now on Ill name one of these categories, write this in every  handoff.

I also will be sending clothes replacements for the ones currently in game.


PS: Make sure guns are lowered in a really relaxed way, pointing down at the floor sometimes playing with them by spinning them on the
hand, running animation, etc (these animations will serve for every gun from now on)

Sort every Katana into the category of katana
Gryphon into saber and yoru into yoru."
(The message ended with "Tente novamente", Portuguese for "Try again": the app's own retry text, not a request.)

- Done at once: the wake-up 5 hours later (a one-time scheduled message to this session, 2026-11-27 03:02 UTC).
- **STANDING RULE, to copy into EVERY handoff from now on (the owner: "write this in every handoff"): every weapon the owner gives
  comes with one of these categories, and it is made by its category's animations:**
  - **Katana**: as the katanas are now (every katana sorted here).
  - **Saber**: like the katanas but more aggressive; the idle and the unsheathe stay as they are (Gryphon is a saber).
  - **Mace**: right-handed, rests on the back like Yoru, a slow pull-out, at idle rested on the right shoulder; has range; its hits
    ragdoll; slow to cock, fast on the swing (`First_mace.blend` is the first).
  - **Yoru**: one of a kind, stays as it is, uncontested the strongest blade that will ever be added (Yoru alone).
  - **Gun** (pistols): held in the right hand in a badass pose (the two pictures); one bullet a shot, reloads by itself after (a simple
    reload: one bullet put into the barrel's mouth); every bullet the same model (the owner will send it); with Haki on the bullets come
    out pure black, much much faster, through blocks (10 at most); lowered in a really relaxed way, pointing at the floor, sometimes
    spun on the hand; a running animation; these animations serve every gun (`First_gun.blend` is the first).
- The other reworks (after the world generation and the Conqueror's Haki items are done): a blade drawn as a swordsman forces third
  person, the camera placed to aid the swings and the aim (first person's sword holding is disliked whole); the clothes replacements
  (`Clothes_Replacements.blend`; more to come).
- The order now: 3.8.1 (the release's lag), the Conqueror's clash (request 1 item 2), the world generation (request 1 item 3), then
  the clashes as HexGhoul's (request 1 item 4) and the weapon reworks above. (The owner put the reworks after "the world and conqueror
  haki"; the HexGhoul clashes were not ordered against them: ask.)
