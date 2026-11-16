# Next AI: after 2.1.2 (combat mode on R, Photon particles)

Read `CLAUDE.md` and `docs/handoff/next-ai-2.1.1.md` first (house rules, how the repo is built, what is where). Released: v2.1.1, v2.1.2
(branch `claude/friendly-allen-g9nrzx`). Answer the owner in English; never claim in-game testing: nothing here has run in game.

## The owner's request for 2.1.2 (quoted exactly)
"now since we use photon, add water particles to fish man, air particles for swings of moves and air blade, same as HexGhoul tap R to
enable combat."
(Earlier, also: "show me a gif of each fighting style": `tools/style_gif.py`, `docs/images/style-*.gif`.)

## What 2.1.2 is
1. **Photon particles** (`client/fx/PieceFx.java`, HexGhoul's `RcFx` ported: pooled, batched emitters of tinted `Mote`s; `later(ticks, r)`
   schedules pieces of an effect). Sprites in `assets/hexhaki/textures/piece/particle/` (HexGhoul's, plus a water `drop.png`).
   `client/StyleFx.java`: every blow thrown cuts the air (stage 3, sent by `server/StyleCombat.land` for every non-line blow, hit or
   miss), landed blows spark, the Air Blade flies as a crescent with ghosts and a trail, Fishman Karate is water (droplets, splash,
   bullets, the counter's ring, the wave), Flame Leg flames, dashes dust, Sky Walk rings, Iron Body sparks. Density follows the
   client's particle setting. The Photon effects cannot be rendered here; only the sprites were judged.
2. **Combat mode** (`client/CombatMode.java`): tap R toggles it (`network/C2SCombat`, message 5), Ctrl + R sends the original's
   `ARMAMENT_TOGGLE`. Taken at HIGH priority before the original's `HakiKeys$ForgeBus.tick` (both use `consumeClick`, verified in
   the original's bytecode). Not available (and switched off by itself) in Devil Fruit mode, with a fruit kit on (`FruitClient.active()`)
   or without a chosen style; `/hexpiece reset` switches it off on the server. HUD badge over the hotbar. `server/StyleServer.press`
   refuses moves while combat is off; `client/StyleInput` takes M1 at blocks too in combat mode.
3. **Combat stances**: `style_<id>_stance` (arms-only loops, 40 ticks, `tools/style_anims.py` `stances()` / `write_stance()`), held
   by `client/StanceLayer.java` on a playerAnimator layer of its own at priority 850, UNDER the original's (Haki 900, Don Gun 905,
   fruits 1100): moves and Haki play over it and ease back into it, nothing re-sends it. `network/S2CStance` (message 6) tells every
   client who holds which stance: on toggle (tracking + self), on `PlayerEvent.StartTracking`, cleared on logout. Each client eases
   it out while the player holds an item, rides, isn't STANDING/CROUCHING (swims, flies, sleeps, dies) or has a fruit kit on.
   First-person mode NONE: the stance leaves the vanilla first-person hand alone. Protocol is "2".
4. **Hands behind the back fixed**: 2.1.1's Black Leg "hands behind the back" only hung the hands at the back of each hip. `BACK_HAND =
   arm(94, -48, -75, -81)` (solved on the model: forearms folded across the small of the back, hands meeting, elbows at the sides)
   is now Black Leg's hands in every move and Six Powers' back hand. NPCs (no bendy-lib: straight arm at pitch + 45% bend) show it
   as straight arms crossing behind the back to meet at the small of it (judged).
5. **Preview tools**: `tools/anim.py`'s `draw` now renders Minecraft's boxes (torso, head and each limb half as a cuboid, faces turned
   away culled, painted far to near) and has `threeback` / `back` views; the old line renderer drew a forearm behind the back in front
   of the belly. `tools/anim_preview.py --turnaround` (one pose from every side). `tools/style_gif.py` opens on the stance (R) and
   holds it between moves, each move returning to it after its endTick, as the game now does.
6. Tests: `StyleTests` checks each style's stance exists, loops and animates the arms only. Evidence: `Pose.CROUCHING` ([srg]).

## Renders (docs/images/)
`combat-stances.png` (the four stances from front, 3/4, side, 3/4 back, back), `style-*.gif` (each style's moveset from its stance),
`style-black-leg.png` (the five Black Leg moves with the new clasp). Older renders (npc-skins, lightning, menus, style-specials,
armament-acoc-before-after) are 2.1.1's, drawn with the old line renderer.

## Ask the owner after 2.1.2
- Is Ctrl + R for Armament Haki right (Ctrl is also sprint), or should Armament go to another key?
- How the particles look in game (amounts, sizes, the water's blue, the Air Blade's speed) and the stances (Fishman's raised palm and
  the boxer's guard are held while walking: too much?).
- Should NPC pirates and marines hold their stances between moves too? (Not done: NPCs stand vanilla between moves.)
- The open questions of 2.1.1 still stand (a pirate title screen? style numbers? a newer jar with the Imu fruit?).
