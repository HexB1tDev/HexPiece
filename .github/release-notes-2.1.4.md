# HexPiece 2.1.4

The Swordsman: a fifth fighting style with your 25 katanas and their saya, and pirates who fight with it. Nothing here has been tested
in game yet: the animations, the menus and the katanas were rendered and judged off-game, and the build's checks and tests pass.

## The katanas
- **Your 25 katanas from the Hex Katana Atelier**, each with its own saya, as items (`hexhaki:katana_<name>`) in their own creative
  tab, **Katanas**. Your full 3D models, textures, guards, handles and temper lines are kept, not flattened into sprites. The
  inventory shows each one's icon; dropped on the ground or in an item frame it shows the real model, blade in saya.
- **Grades, rarities and descriptions**: Saijo O Wazamono (epic: Obsidian Eclipse, Dragonbone, Imperial Phoenix), O Wazamono (rare),
  Ryo Wazamono (uncommon), Wazamono (common). Each has its own description and temper line in the tooltip. A better grade cuts harder
  (x1.0 to x1.36).
- **Shine**: the blade has a moving band of light and an edge flash as it turns, so it glints when it moves. Far away, simpler meshes
  are drawn (at 2.5 and 10 blocks) so a crowd of swordsmen stays light.
- **At the hip**: a katana anywhere in your own inventory (hotbar, main inventory or offhand, not in a shulker box or the ender chest)
  hangs sheathed at your left hip, for every style, even when it is not in your hand. With several, the one in your hand wins, then
  the one you used last, then the first slot. Moving it around the inventory keeps it the same sword. The saya turns with your hip
  but never moves away from it, and the hilt stays visible.

## Swordsman
- **A fifth style** in the start menu and the Captain's Log, with mastery like the others. Only a Swordsman in **combat mode (R)**
  can draw, and only by attacking (with the katana or nothing in the main hand). Drawing never touches your inventory.
- **Right-handed**, one hand on the hilt for every cut. In combat mode with the sword sheathed, your left hand rests on its hilt at
  the hip while you stand. Drawn and between cuts, the sword hangs low in your right hand with the point toward the ground ahead, and
  the left arm hangs relaxed.
- **M1: 15 different cuts in three strings** (three draw cuts and twelve more), chosen by what you are doing when you press:
  - *Standing*: Draw Cut, then Kesa (diagonal down), Reverse Kesa, Horizontal, Return Horizontal, Thrust, Rising Cut, Overhead.
  - *Moving*: Dashing Draw, Advancing Cut, Pivot Cut (a spin).
  - *In the air*: Falling Draw, Aerial Cross (two cuts), Wheel (a flip), Plunge.
  
  From the saya, the first press is that string's **draw cut**: the blade leaves along the saya's line straight into the cut, with no
  separate draw animation. Then each press is the next cut. Press again within 12 ticks after a cut ends to continue the string, or
  it starts over. Changing string (stopping, running, jumping) starts that string's own sequence.
- **Fair hits**: each press asks for one cut. One press is kept if made in the last 4 ticks before the cut can be cancelled, and
  never more. The server decides everything. A cut only hurts in its own hit windows (the Aerial Cross has two), along the blade's
  real path swept four times a tick, so fast swings can't skip past a target. Each body is hit at most once per window. Ordinary cuts
  never break blocks.
- **Sheathing**: 8 ticks after your last action the blade goes back into the saya over 6 ticks. The tip enters the saya's mouth, the
  blade slides in along its curve, and the left hand brings the saya forward to meet it. A cut pressed while sheathing carries on
  from where the blade is.
- **Right click: the charged slash.** Hold to charge (the sword draws if it has to, then a readable raised guard). Let go to cut.
  Charge is measured on the server's clock, capped at 40 ticks by default, and never fires by itself. You get one release per charge.
  - *Without Armament Haki*: a heavy physical cut that reaches further the longer you charged (24-tick cooldown).
  - *With Armament Haki*: a **Haki slash** that flies off in your **Ryou colour** (a neutral colour if you have no Ryou colour yet;
    using it never gives you one). It costs Haki and has a 60-tick cooldown. The longer the charge, the taller, longer, stronger and
    costlier it is (1.6 to 6 blocks tall, 12 to 36 blocks of travel). It **cuts a thin vertical plane through terrain** as it flies,
    obsidian included, and what it shows, hits and removes all match.
    Bedrock is never cut and shields the blocks behind it. The cut respects block-break protection, world limits and unloaded chunks,
    drops the blocks' items (never twice), and is capped per tick. Without enough Haki the cut is steel alone.
- **The Ryou finisher**: with Ryou on, a Haki slash that would kill holds the kill back. The victim freezes for about a second. A
  precise red line then traces the cut around its body (up the front, over the top, down the back), and when it closes, the body
  falls in two along the cut with the original's gore and blood. A player struck this way is frozen but can look around, sees a red
  line rise up the middle of their view, then dies cut in half. Bosses (the Wither) are simply hit.
- **Ctrl + M1: Quick Draw.** A flash of a draw that carries you 5 blocks forward in four ticks, cutting everything along the line
  once (3 s cooldown). It also works with the blade already drawn.
- **Restrained effects**: thin trails that follow the blade's real edge and fade fast, a brief glint, and sharp sparks on impact. No
  fire or elemental effects on ordinary cuts.
- **First person** shows the same cut as everyone else sees, in the same rhythm. Your camera stays yours.
- Anything that makes a sword fight impossible (death, losing the katana, another style, leaving combat mode, an item in the hand,
  swimming, climbing, riding) stops everything at once and the blade is back in its saya. A Swordsman with no katana is told so.

## Pirates
- **Pirates now also fight as Swordsmen** (two in five patrol members), besides kicking, boxing and Fishman Karate. Their katana is
  chosen by rank (grunts the common grades, captains the best) and hangs at their hip. They use the same cuts, timing, hit windows and
  swept blade paths as players. Officers and captains sometimes charge the slash. A captain's flies as a Haki slash, and it only cuts
  terrain while the `mobGriefing` rule allows it.
- **Marines are unchanged.**

## Settings (server)
`serverconfig/hexpiece-server.toml` in each world, section `swordsman`: the charge length, the charged cut's damage, reach and
cooldown, the Haki slash's height, travel, damage, Haki cost and cooldown, the blocks it may cut per tick, whether it cuts terrain at
all, and the sheathing delay.

## Note for servers
The network protocol changed: 2.1.4 clients and servers only connect to each other.
