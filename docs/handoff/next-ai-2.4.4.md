# HexPiece: handoff after 2.4.4 (the owner's in-game report: swords, Conqueror's release)

Read CLAUDE.md first. This note is the newest; next-ai-2.3.6.md covers 2.3.6 to 2.4.4 in detail (clashes, the Galaxy Fist, the
Fishman Karate rework, Six Powers' 3D air, Haki slash clashes, rank gestures, the Haki stun fall), next-ai-2.3.2.md 2.3.2 to 2.3.5
(one animation set for every blade, Haki hands, bigger swings, first-person swings). Branch `claude/nice-bell-gam7qf`.
**The next version is 2.4.5** (`python3 tools/next_version.py --write`). 2.4.4 is released (v2.4.4; Build and Release green).

## The owner, after testing (verbatim, with a reference picture)
1. "Gryphon in first person when unsheathed still shows has having the sheath on when it doesnt. (swings work but yeah)."
2. "The swings are not fixed, the arm barely extends forward during m1 swings. (the arms should be fully stretched and the blade
   pointed forward far from the player's chest, to be the max range it can be."
3. "Different swords should have different range (Yoru is the biggest)."
4. "Conqueror Haki full release lags a ton when releasing the full charge."
5. "Conqueror release lightning is good, but the positioning of the lightning bothers me, feels like the lightning is going always
   to the same spot (feels like it, doesnt mean it is)".

The reference picture (not kept in the repo: a game character's art): a swordsman in a very wide, low lunge, feet far apart, the
front knee deeply bent, the rear leg nearly straight, the torso turned side-on to the target; the sword arm thrown fully straight out
from the shoulder (no bend at the elbow), level with the shoulder and angled a little up, the blade continuing the arm's line far out
past the hand; the free arm stretched straight back the other way for balance, open hand; the head turned to the target, eyes along
the blade. That is the reach the owner wants at the end of an M1 cut: the arm straight, the blade far out in front, away from the chest.

Also said this session: "I don't need to render anything anymore. I'll test when I get to my office." Do not send the owner GIFs or
renders unless they ask. The house rule still stands for yourself: judge poses numerically or with the preview tools before shipping.

## Where to look (found while writing this, not yet acted on)
1. **Gryphon's saya in first person.** The first-person hand is drawn by `client/sword/KatanaItemRenderer.held()`: it always draws the
   blade *and* the saya (`KatanaRenderer.draw(..., false, true, ...)`), whether or not the sword is drawn. SwordLayer (third person)
   knows `SwordAnims.drawn(anim, ticks, katana)`; `held()` does not ask. Fix: in first person, when the local player's sword is drawn
   (SwordClient.anim / SwordAnims.drawn for that player), draw the blade alone (no saya), and while sheathed keep both. Check why the
   owner noticed it on Gryphon (its saya is the most visible; its draw is 32 ticks, `SwordRules.drawTicks`), and fix it for every blade.
2. **M1 reach.** 2.3.5 raised `REACH` to 8.4 and widened the arcs (`tools/sword_anims.py`: `strike()`, `widen`, ARC_WIND 1.2,
   ARC_END 1.12, ARC_OVER 1.18), but the solved arm still folds: the owner sees the arm "barely extends forward". In the GIF render
   (`tools/sword_gif.py OUT 'sword_(kesa|yoko|...)' --views fp,three`) the blade also leaves the first-person screen for most of a cut.
   What is wanted: at each cut's strike beat the arm fully straight (elbow bend ~0), thrown forward from the shoulder, the blade on the
   arm's line pointing forward, far from the chest (the reference above), and the hit volume (`BladePaths`, the blade's swept path)
   reaching as far as that goes. The solver (`sword_rig.solve_blade`, `_Arm`, the strain limits) and the pose check
   (`check_sword_poses` in build.py) must still pass; the owner's sword rule: one animation set for every blade (see below).
   The first-person swing is playerAnimator's third-person arm (SwordClient: FirstPersonMode.THIRD_PERSON_MODEL, right arm only); if
   it still reads poorly, a first-person-only swing on the vanilla hand (RenderHandEvent) was the noted alternative (next-ai-2.3.2.md).
3. **Range by sword (Yoru the biggest).** Every blade plays the katanas' one animation set (the owner: "skip the checks on every single
   katana ... Just apply the same fix to every single one ... They're all the same"), so give range by blade, not by animation: the
   blade's own length (KatanaRenderer.LENGTH vs YORU_LENGTH; `tools/sword_anims.Blade` profiles KATANA, GRYPHON, YORU) and/or a reach
   multiplier per `Katana` (grade, legend) applied where the swept path becomes the hit (BladePaths / SwordServer's hit test). Yoru
   longest, then the legendary and great grades, then the rest; say the numbers in the release notes.
4. **Full-release lag.** Unknown yet whether it is the client (frames) or the server (ticks): ask the owner, or make both cheaper.
   Suspects: the healing fissures (`server/Fissures` + `Scars`: many `setBlock` calls in one tick, each a client chunk rebuild; spread
   them over ticks, cap them); the release storm on the client (`client/haki/HakiStorm`, `ConquerorStorm.release` strands, each built
   into BoltMesh quads every frame: cache the meshes or cut the strand count / points); the knockdowns of many bodies at once (2.4.4's
   HakiFalls sends one S2CFall per body: cheap, but check); PieceFx particle counts on release; the original's own release work
   (HakiServerController.applyConqueror, HakiVfx.conquerorRelease). Measure before changing: time the pieces with System.nanoTime in a
   dev run if one is possible, or count what each spawns.
5. **Lightning "always the same spot".** The storm strands (`ConquerorStorm.release(mastery, seed, supreme)`) come from a fresh seed per
   release (ConquerorRelease.visual: `player.getRandom().nextLong()`) and a randomly turned sphere (`Sphere.directions`), so they do
   vary; the range lightning that strikes the ground is the original's `ConquerorLightningPath.generate(charge, mastery, seed, dome)`
   (redirected to `haki/LightningPaths` -> `ConquerorBolts.generate`), whose seed comes from the original's callers
   (HakiServerController near its `generate` call, HakiVfx near its own): trace where that seed comes from. If it is fixed per player
   (UUID, entity id) or per mastery, every release lands the same pattern around the player: make it per release, the same on server
   and client (the server's hit checks and the client's bolts must still agree). Also check the landings are spread round the player
   in every direction and at varied distances, not clustered.

## Done this session (session 4), in short
2.3.6 clashes; 2.3.7 the Galaxy Fist made Iron Fist's (Ryou up); 2.3.8 to 2.4.0 the Fishman Karate rework (Water Bullet, Brick Fist,
Water Prison, rag dolls); 2.4.1 Six Powers' 3D air and Haki finger shots; 2.4.2 Haki slash clashes; 2.4.3 NPC gestures by rank and
the lookout fix (+ `NpcTests.gestureClearance`); a GIF reel of everything (artifact claude.ai/artifact/Qez8HbAQ31bWyRr2PrtsYG);
2.4.4 the Haki stun fall for every entity (HakiFalls replaces the original's knockDown), the Water Bullet arch centred over the head,
a much stronger Brick Fist. Details: next-ai-2.3.6.md.

## House rules (CLAUDE.md has them all)
Versions roll over at 9 (2.4.9 -> 2.5.0). One version per push the owner should test, with `.github/release-notes-<version>.md`
written for the owner (plain words, what they will see). A push is done only when GitHub's "Build HexPiece" (and "Release HexPiece"
when notes change) runs are green. The Imu fruit stays gone. Never claim in-game testing. Client-only code in a `client` package.
Never alter the owner's files in place. Sounds from the web, free to use. The owner's device draws additive or culling-dependent
geometry as solid boxes: alpha-blended, depth-tested, right without culling. Stop rule: at 98% weekly usage write the handoff and
next prompt first, commit, push, stop without checking CI.
