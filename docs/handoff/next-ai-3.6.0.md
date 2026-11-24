# HexPiece: handoff for 3.6.0 (written mid-version at the owner's word: "prepare an extremely detailed handoff and prompt for the next AI, including all changes we are making and have made in this chat")

This is the newest handoff. It covers the whole chat that built 3.5.9 and started 3.6.0. Read it whole, then read
`docs/handoff/next-ai-3.2.2.md` for every standing rule (sections POINT ONE and 0 to 7) and the history (8b to 8zzx). Where the two
disagree, this file is newer and wins (POINT ONE 3 below is the owner's newest word on the lightning).

Branch: `claude/nice-bell-gam7qf` (fetch it first). Repository: HexB1tDev/HexPiece. 3.5.9 is green on GitHub (Build HexPiece #216,
Release HexPiece #150). **3.6.0 is versioned and pushed with its release notes (the next session's first push; section E 7 has the
sounds it finished)**: check section H for its GitHub runs. The next version is **3.6.1** (`python3 tools/next_version.py --write`).

---------------------------------------------------------------------------------------------------------------------------------------

## A. POINT ONE (the owner's standing orders: keep this first in every handoff and pass it on)

1. **Every fruit is made in the same style as the Light, Gura and Magma fruits**: 3D meshes built the way FireMesh / GuraArt / MaguArt
   build them (banded alpha-blended shells, puffs, arcs, rays, streaks, lumps, shards), PlayerAnimator clips written by tools/*_anims.py
   with no leg and no head keyed (unless the move uses them), a short first-person hand move for every move, CC0 web sounds, every effect
   rendered and judged by you before pushing.
2. **With the Conqueror's Haki and Ryou on, every move of every fruit throws the Conqueror's lightning (visual only)**, out of the move's
   end explosion when it has one (not optional), at the strike point otherwise.
3. **There is ONE Haki lightning in the whole game: the Conqueror's release's (button G).** The owner, this chat: "Haki Coated attacks
   from fruits and styles are to have the exact same LIGHTNING as the one in Conquerors's charge (Holding G)" and "Also every lightning
   should be more croocked, its going too straight Look at haki references man. Just make sure ALL haki is shared, only 1 type and style
   of haki model should be in use, THE CONQUEROR HAKI RELEASE (BUTTON G)". Since 3.6.0's work: the release's bolt
   (`haki/ConquerorStorm.bolt`) is more crooked (STEP 3, WANDER 0.85, a sharper inner kink per run, branches of 3 to 5 runs) and
   `ConquerorStorm.jagged` / `ConquerorStorm.strike` give that same model (a black body and a coloured rim on crooked runs, the release's
   taper) at any size to every other lightning maker. **Never add another lightning look.** Never re-scale a blast's width on its own
   (8zn in next-ai-3.2.2.md). Render any lightning change beside the release from the same camera (tools/preview/SparkPreview,
   tools/preview/HakiColourShots) before pushing.
4. **The owner's colours (new this chat):** a player picks a **Haki colour** (the lightning) and a **Ryou colour** (the glow round the
   coated arms) on the start menu. Never name a character in the game's text: every colour is named by its colours only. The forbidden
   fruit's owner (one of the owner's Haki colour references) must never be named anywhere: build.py `check_imu` fails the build.

## B. Talking to the owner (short; next-ai-3.2.2.md section 0 has the rest)

- English, plainly; what they will see and hear, not class names. They type fast through speech-to-text ("hockey" = Haki, "Rio" =
  Ryou, "wonomi" = Uo Uo no Mi). They send many requests mid-work: write each one verbatim into the handoff at once (this chat did, in
  next-ai-3.2.2.md 8zzv), finish what you are on, then take them in order.
- Do exactly what they ask, nothing unasked; ask once (AskUserQuestion) only when truly ambiguous. Never claim in-game testing.
- A message can arrive cut off (8zzv 11 ends "should look  like"): ask them to resend the rest; do not guess.
- They sent pictures this chat: the koiguchi lightning (WiFi Haki), a red sky of zig-zag lightning (crookedness), a coated Luffy
  (coat past the elbow), their own crop of our render circling where the glow stopped. When they send a picture, describe it in the
  handoff (the image is not kept).

## C. The owner's requests in this chat, verbatim, and where each stands

From the prompt (section 8zzu of next-ai-3.2.2.md has them with the plan):
1. "HOLDING R will now fully disable all powers and abilities (but fruit passives)." **Done in 3.5.9.**
2. "iron fist Z, the cannon ball should not fucking increase size, wtf man" **Done in 3.5.9** (speed and blast still grow with the hold:
   told the owner in the release notes; they have not answered).
3. "Blue hole should be: / Uppercut / Did it hit someone? No? Ends here. / If Yes: / the entity goes flying then the user catches up
   (jumps to the entity) grabs them and throws them down to the floor, hard." **Done in 3.5.9.**
4. "galaxy impact is too laggy. / Impact frames are only on: / Did 2 attacks clash? (if yes then the impact frame plays for both)"
   **Done in 3.5.9** (the Quake fruit's N ultimate keeps its own old frame: told the owner, unanswered).
5. "Wifi haki becomes a Sword's man ability only (switch the key to a free quickslot not L)." **Done in 3.5.9** (on K).
6. "Galaxy Fist  Removed entirely." **Done in 3.5.9.**

During the work (next-ai-3.2.2.md section 8zzv has them all verbatim, numbered 1 to 12):
7. "Haki Grip  Removed entirely as well" **Done in 3.5.9.**
8. WiFi Haki's animation: "The user (while holding the key) slowly unsheaths the sword JUST A LITTLE BIT, the lightning emergers from the
   opening in the sheath." (picture: the guard pushed out of the saya's mouth, red-black lightning from the gap) **Done in 3.5.9.**
9. The Haki colour on the start menu (seven sets; verbatim in 8zzv 5). **Done on the branch (3.6.0, not released).**
10. "Also all lightning sounds are to change to new more bass boosted ones (get from the web) and be lowered in volume (ONLY the
    lightning sounds), Full COnqueror on releasing should not play all the lightning sounds at once, just one (currently they're all
    playing at once when the key is released causing a ridiculously loud sound)." **Done in 3.6.0** (section E 7).
11. "Haki Coated attacks from fruits and styles are to have the exact same LIGHTNING as the one in Conquerors's charge (Holding G)."
    **Done on the branch (3.6.0).**
12. "Also every lightning should be more croocked ... only 1 type and style of haki model should be in use, THE CONQUEROR HAKI RELEASE
    (BUTTON G)" **Done on the branch (3.6.0).**
13. "All new fruits and every style have to obey /hp op on/off" **Not started.** Section F step 4.
14. The Mochi ultimate, Buzz Cut Mochi: the owner's full description is verbatim in 8zzv 10 (read it whole). **Not started; "Please get
    References first".** Section F step 6.
15. The Mochi list (8zzv 11, verbatim): M1s and X turn the arms themselves into mochi (no extra arm layer over them); the mochi material
    more realistic (depth, shade, colour, 3D); Mochi Anemone's fists fill the whole hole and fit the donuts' size like the Awakening's V;
    mochi appearing and vanishing realistically, not popping; Z lifts the leg and turns it into the many legs; C held, the explosion
    bigger the longer, one arm pointed and nothing else animated; and "In the new Buzz Cut Mochi and Zan Giri mochi the rolling donut the
    player turns into it should look  like" (cut off: ask the owner for the rest and the picture). **Not started.** Section F step 5.
16. The Ryou colour (8zzv 12, verbatim: "also add a menu to pick the Ryou color" with seven colours) and "Haki coating on the arm should be
    covering even higher up in the arm unevenly" (picture: forearms black past the elbow, the edge uneven, a pink-red glow).
    **Done on the branch (3.6.0).**
17. Their note on our render (they circled the forearm's sides above the glow's end): "make sure the haki glow fades properly,its ending
    too shortly all of a sudden." **Done on the branch (3.6.0).**
18. "prepare an extremely detailed handoff and prompt for the next AI, including all changes we are making and have made in this chat"
    (this file and docs/handoff/next-prompt.md).

## D. 3.5.9 as built (released, green: Build #216, Release #150)

Full detail: next-ai-3.2.2.md section 8zzw. In short, by file:

**Hold R (every power off, fruit passives kept).**
- `client/CombatMode`: R handled by edges (`HakiKeys.ARMAMENT.isDown` m_90857_, clicks drained so the original never sees them). A tap
  (Ctrl: Armament) acts on release; held `HOLD` = 12 ticks sends `C2SPowers(off)` once. While powers are off: combat mode forced off, a
  tap only says "Powers off: hold R to turn them on". Overlay "hexpiece_powers" (registered above all): a fill bar while R is held, a
  "POWERS OFF (hold R)" badge while off. Only for players who have set sail with a style (`styled()`); others keep the original's R.
- `network/C2SPowers` (id 23), PROTOCOL 19 then (now 20, see E).
- `server/Powers`: the original's own powers bit (`Binds.off`, synced to `BindsClient.off`). `set(player, off)`: Binds.off,
  `HakiServerController.cancelAll`, the fruit kit's `cancel` (never `removed`: passives stay), the Gomu fruit's
  `GearServerController.cancelAll` and `endGearFive` (widened by the patch), `StyleServer.combat(false)`, `SwordServer.stop`,
  `IronFist.forget`, `__clear__` animation, action-bar message, syncs. `toggle`, `passives` (owns), `gomuThrough`, `refuse`.
- Patch rows (PiecePatch CALL_REDIRECTS): FruitServer.action's and GearServerController.onAction's `togglePowers` -> Powers.toggle;
  FruitServer.attacked's `FruitData.active` -> Powers.passives; HakiServerController.onAction's `GearData.hasFruit` -> Powers.gomuThrough;
  `PiecePatch.gomuPowersOff` puts a guard at the head of GearServerController.onAction (one jump, FrameNode after its label).
- Held back while off: Haki (`RyouServer.hakiOff` adds `Powers.off`; `PieceHooks.hakiKeysOff` returns `BindsClient.off()`), fruit
  abilities (the original's FruitData.active), and the handlers of C2SCombat (only turning it on), C2SStyleAction, C2SSword, C2SDragon
  (flame), C2SNikaJump, C2SItoSky. Test: StyleTests.powers.

**Iron Fist.**
- Meteor Fist: `IronMove.METEOR_RADIUS` 0.55, one size (meteorRadius removed).
- Blue Hole remade: server `IronFist.blueHole` / `uppercut` / `Flight` / `slam` (from the saved patch); `IronMove` HOLE_* beats
  (HOLE_HIT 6, HOLE_REACH 3.6, HOLE_RISE 8, HOLE_HEIGHT 9, HOLE_JUMP 2, HOLE_LEAP 6, HOLE_HANG 4, HOLE_FALL 4, HOLE_DROP 10, HOLE_TICKS
  35, HOLE_MISS_COOLDOWN 40, holeRise, holeThrow); a miss clears the clip (`FruitUtil.animate(player, "__clear__", 4)` a tick after the
  uppercut), so does a flight cut short. Client `IronFx.hole` phases 0 to 7 (record Throw, `IronFx.thrown`, `Carry.fall` eases the drop
  in). Clip `style_brawler_blue_hole` (tools/style_anims.py: coil, uppercut on 6, tucked leap with the arm reaching up, grab 14, wound
  back by the side 17, throw 18, fall, landing low 29, leaning over the hole; the raised arm keeps roll 0 so it never crosses the head:
  anim.lint is clean). Previews: tools/preview/IronShots `ihup`, `ithrow`, `ihole`.
- Galaxy Impact lighter: IMPACT_CROUCH_EVERY 10, IMPACT_CHARGE_EVERY 8, impactBolts 2..5, landing bolts 6, blast bolts and rocks every 12
  ticks (6 bolts, 10 rocks), foes every 4 ticks, crater IMPACT_CRATER 13 / IMPACT_DEPTH 6.5 / 6 blocks above, IMPACT_SCATTER 600, one
  dust wall in the client's strike. DragonTests.ironArt: under 9000 quads at its heaviest.

**Impact frames only on clashes.** `S2CClash.CLASH` (16) set by Clashes' three senders; `S2CClash.framed(self)`: the screen frame only
for a clash and only on its two fighters (`a`, `b`), or the Gura ultimate's CLASSIC frame (kept). ClashFx.accept returns before the
frame otherwise (the world burst stays). IronFist's `frame()` and its six calls are gone. HudTests checks `framed`.

**Haki quick slots.** `haki/QuickSlots` (pure): the Haki Grip (101) and the Galaxy Fist (103) off every layout; WiFi Haki (102) a
Swordsman's only and never on L (slots G K L H = 0..3): on K, else H, else any free slot. `server/HakiQuick`: the patch sends
begin/releaseDominion and begin/releaseConvergence to `gone()`, sovereignStrike / releaseSovereignStrike to `wifi()` / `wifiRelease()`
(Swordsman check), `wifiHakiHandPoint` (in sovereignStrike and sendWifiHakiArc) to `mouth()` (the koiguchi at the left hip when the katana
is sheathed there: `SwordServer.sheathedAtHip`); `tidy()` at the head of `Binds.sync` (PiecePatch.tidySlots) rewrites the slots through
`Binds.set`; `ProfileServer.sync` calls `Binds.sync`. GalaxyFist.java and PieceRules.galaxyFist deleted; the INTS 520 row gone; TEXTS:
"[Hold L]" -> "[Hold K]". LogbookData hides the Grip and the Galaxy Fist and shows WiFi Haki to Swordsmen only. StyleTests.galaxy.

**WiFi Haki's animation.** `sword/WifiDraw` (pure timeline: hand on the hilt 9, thumb 1.2 px 14, 2.9 px by 40, an 8-tick trembling loop,
home in 5); `tools/wifi_anims.py` writes the original's `wifi_haki.json` from `sword_draw.json`'s solved poses at the draw tick where
its blade is as far out (arms, body yaw and bend, leftItem; no head, no legs; a loop, returnTick 40). `client/sword/WifiPull`
(NikaToon.clip hands it every clip: "wifi_haki" starts the pull, any other clip ends it); SwordLayer slides the blade and lays the saya
by the Haki layer's leftItem while the clip plays; `HakiDraw.wifi` crackles out of the gap; HolsterHand shows the draw's first-person
hands at `WifiDraw.drawTick`. SwordTests.wifi. Previews: the scratchpad's wifi_view.py (sword_rig) and `BoltPreview wifi`.

## E. 3.6.0 so far (on the branch, builds green locally, NOT released)

1. **Haki colour** (`haki/HakiColour`: BLACK_RED, GOLD_YELLOW, PURPLE, GREEN, BLUE, GREEN_CYAN_BLACK, RED_LIGHT_RED; each `body` (0 =
   the black body edge-lit in the rim's hue), `rim`, `glow`; `byRim`, `bodyFor`, `glowFor`, `nearest`, `code`/`byCode`; no rim is one of
   the old random `RyouPalette` colours). The lightning: `Bolt.body(rgb)` returns the set's body; `Bolt.tinted` uses it;
   `BoltMesh` shades a coloured body with `colouredBodyShade` (bright bodies: `coloured()` top channel > 45) and draws the EDGE glow in
   `HakiColour.glowFor`. `RyouServer.choose(player, colour)` stores "HakiColour" and sets "RyouColor" (the lightning colour every
   existing path already reads) to the set's rim.
2. **Ryou colour** (`haki/RyouColour`: RED_ORANGE, PURPLE_BLACK, GREEN, PINK, RED_YELLOW, BLUE, GOLD; `band`, `haze`; `byBand`).
   `RyouServer.chooseRyou` stores "RyouAura"; `RyouServer.aura(player)`. `S2CRyou(entity, color, aura)`: color = the Haki colour (the
   lightning, slashes), aura = the Ryou colour; client `RyouColors.aura(id)` (falls back to the Haki colour) used by `RyouHands` (the arm
   glow) and `RyouFx` (glints, the come-on burst, streaks). `FistAura.colours` takes the set's band and haze.
3. **The start menu** (`ui/CreationView`): five pages; page 4 (`COLOUR`) the Haki colours (cards four over three: `Layout.colourCards`,
   each a crooked forked bolt drawn as the game draws one: glow, ink, rim, body: `swatch`, `stroke`), page 5 (`RYOU`) the Ryou colours (a
   coated fist in its soft glow: `ryous`, `fist`, `box`). `State.colour`, `State.ryou`, `State.colourOnly()` (a player who set sail
   before: pages 4 and 5 only, back stops at page 4, the button reads "Choose"). The summary line is fitted between the buttons.
   `C2SCreate(gender, faction, style, colour, ryou)` (codes); `S2COpenCreation(again, colourOnly)`; `ProfileServer.create(..., colour,
   ryou)` (a player already set sail with no colours yet: sets both only), `ProfileServer.coloured`, the login sends the colour pages to
   one who has none. `CreationScreen(colourOnly)`, `PieceClient.openCreation(again, colourOnly)`. PROTOCOL **20**.
   Previews: `UiPreview` writes creation-0..4 and creation-colour-only (compile it against `build/classes:build/base.jar:build/stubs`).
4. **One crooked lightning** (POINT ONE 3): `ConquerorStorm` STEP 3.0 (was 6), WANDER 0.85 (a downward strand keeps the old gentle wander
   on its glide to where it lands, so it lands out where it was sent), inner kink 0.3 of the run at 35 to 65 % along, branches 3 to 5
   runs turning harder; a strand that is not a downward one never touches the ground within 3.5 blocks of the feet; the run that strikes
   the ground keeps its inner kink in the air. `ConquerorStorm.jagged(a, b, seed)` and `strike(a, b, width, tip, rim, delay, duration,
   crawl, seed)` (BODY + EDGE on crooked runs) now draw: `HakiLightning.draw` (the original's painted effects: each original bolt, a
   glow + body/core + heart triple, is painted ONCE from its body or core beam (`!bloom` and not AURA) at twice its width, its glow and
   heart add nothing), `ConquerorSparks.arc`, `DrawCrackle.stroke`, `LanceLightning`. Tests: DragonTests' biggest blast now under 2200
   quads (was 1600: the kinks cost about a quarter more), LightningTests.crackle checks the release's model (BODY and EDGE only), the
   landing tests pass. Renders: `HakiColourShots` (the release and a blast in all seven sets), `SparkPreview` (body and blade sparks,
   blasts, the release), `BoltPreview wifi`. Note: SparkPreview's close blast row can show a huge wedge: a point behind its very close
   camera (the preview has no near clipping; the game does). Judge blasts from the far camera.
5. **The coat past the elbow** (`haki/HandCoat.coat(column, row)`): every column round the arm has its own edge between rows 2 and 5
   (EDGE_HIGH, EDGE_LOW), 0.7 on the edge row, now and then a 0.45 tongue above; `reveal` spreads up over 9 rows. RyouTests.handCoat
   checks black past the elbow, the edge rows 2 to 5, at least three different edges round the arm.
6. **The glow fades up the arm** (`haki/FistAura`): `top(bend)` = 1.2 px on a straight arm, 4.6 px (below the elbow) once bent 0.3 rad,
   eased between (the aura's outline is the bent box's and must not fold across the bend: a fixed top above the elbow broke RyouTests.aura
   for bent arms); `along(y, top)` fades over 62 % of the arm below the top (was 2.6 px: it stopped short). Render: tools/ryou_preview.py.

7. **The lightning sounds (8zzv 6), finished in the next session:**
   - sounds.json: `conqueror_release` -> piece/lightning/web/strike1..3 (volumes 0.385, 0.442, 0.5, attenuation 48), the original's
     `armament_ambient_zap` -> zap1..3 (0.537, 0.61, 0.724, default attenuation as the original's) and `last_stand_lightning` ->
     thunder (0.624, attenuation 64). Each volume sets the file's loudest moment (ffmpeg ebur128 momentary max) to -19 LUFS: 6 dB under
     the old release thunders' -13 (the old Armament zap was -8, the old Last Stand lightning -11). build.py RENAMED[SOUNDS] adds
     armament_ambient_zap and last_stand_lightning. The booms and every other sound unchanged.
   - Where the original plays them (checked with javap): conqueror_release only in a PARTIAL release (HakiServerController
     .releaseConqueror, through `sound` -> delayedSound -> ConquerorSounds.pos, 35 % quieter); the full (supreme) release played snap +
     blast + rumble instead. armament_ambient_zap: HakiRenderHooks$ForgeBus.tick's playMildArmamentZap (every 110 ticks while Armament
     is on, 0.16 to 0.21), StormLanceVfx.tick (Gear 5's lance, 0.8), and ours (HakiBolts.zap: compositions 0.18-0.55, landings 0.35,
     clashes 0.5, body sparks 0.25, a drawn blade's 0.9; HakiDraw's crackle 0.05-0.155). last_stand_lightning: HakiEvents.tickLastStand
     (the original's Last Stand, a burst every 10 to 18 ticks at 2.3) and HakiBolts.strike (the storm's landings, 1.6).
   - **One at a time:** `haki/LightningGate` (pure: ranks ZAP 1, THUNDER 2, STRIKE 3; LENGTH the recordings' lengths 650 / 3500 /
     4000 ms; a lightning sound starts only when none rings or it outranks the one that does) and `client/haki/LightningSounds` (a
     Dist.CLIENT subscriber to Forge's PlaySoundEvent: every sound about to start is asked; a held-back one is set to null). One place for
     every source, the server's and the original's included. verify/srg-shapes.txt gained SoundInstance.m_7904_ (tools/srg_shapes.py).
   - **The full release's one sound:** `ConquerorRelease.sound` plays the release's strike (CONQUEROR_RELEASE at FULL_STRIKE 2.1, the
     snap's pitch 0.96) where the dropped snap was, so a full release is heard as one strike with its explosion (told the owner in the
     release notes).
   - Test: LightningTests.lightningSounds (the three events play only the new credited CC0 files, each with a lowered volume, the files as
     long as the gate holds, the explosion unchanged, the gate's rules).

Committed with the handoff before (for the record):
- `tools/web_sounds.py`: `BASS` (ffmpeg "highpass=f=45:poles=2,bass=g=7:f=120:w=0.9": the recording's low end lifted, nothing under 45
  Hz kept) and an optional `eq` on `pcm`/`clip`; a clip spec's sixth field is its eq.
- `tools/lightning_sounds.py` and its output `src/main/resources/assets/hexhaki/sounds/piece/lightning/web/` (strike1..3, zap1..3,
  thunder, SOURCES.txt): CC0 Freesound recordings (TRP 574412, kvgarlic 693540, danielmcadams 483684, TRP 616992, Kinoton 760214).
  Checked with tools/tonal_lines.py: every clip's narrow line within about +5 dB over noise (strike2 +2.9, zap1 +2.0); their low-band
  "lines" are thunder's own low end and match the accepted conqueror thunder3 (+12.5 line, +6.2 narrow). NOT yet in sounds.json and
  NOT played by anything (step F1).
  Measured (ffmpeg ebur128, every file peak-normalised to -1 dBFS): strike1 -13.4 LUFS, strike2 -14.6, strike3 -18.6, thunder -18.3,
  zap1 -14.0, zap2 -16.1, zap3 -16.3; the old conqueror/web/thunder1 is -18.6. So "lowered in volume" needs sounds.json volumes of about
  0.35 for strike1..3 (strike1 then about -22.5 LUFS, some 4 dB under the old thunder1), and quiet zaps (HakiBolts.zap's own volumes
  0.05 to 0.55 multiply the sounds.json one: try 0.5 there, and measure rather than guess).

## F. What to do next, in order

1. **DONE (E 7): the lightning sounds, then 3.6.0 released.** What it said, kept for the record:
   - sounds.json: `conqueror_release` -> piece/lightning/web/strike1..3; override the original's `armament_ambient_zap` (HakiBolts.zap
     plays it for every Haki bolt, crackle, coated hit and landing) -> zap1..3; override `last_stand_lightning` (HakiBolts.strike's storm
     thunder) -> thunder. Give each a "volume" well under the old ones (the owner: "lowered in volume"; measure with ffmpeg ebur128 against
     the old files: aim a few dB quieter than the old thunder1 at the same distance). build.py's RENAMED[SOUNDS] lists the original's
     events allowed to change: add the two overridden events there (the build fails otherwise). Check where else the original plays these
     two events and how often (next-ai-3.2.2.md section 5: the original replays some sounds).
   - "just one" on the full release: gate every Haki lightning sound on the client: in `client/haki/HakiBolts.zap` and the thunder in
     `HakiBolts.strike`, one lightning sound per ~6 ticks (a static last-played time; skip the rest). Then the full release's many
     compositions and landings are heard as one. Also check the release's own server sounds (`ConquerorRelease.sound`: the supreme snap is
     already dropped, the rumble too; `conqueror_release` plays once).
   - The owner wants ONLY the lightning sounds quieter: leave the booms (piece.conqueror.explosion*) and every other sound alone.
   - Then: `python3 tools/next_version.py --write` (3.6.0), `.github/release-notes-3.6.0.md` (plain words: the Haki and Ryou colour pages
     (existing players get them once at login), the one crooked lightning everywhere including coated hits, the coat past the elbow and
     the glow fading up the arm, the new quieter bass-heavy lightning sounds heard one at a time), a section 8zzx update in
     next-ai-3.2.2.md or here, push, and check "Build HexPiece" and "Release HexPiece" are green (mcp__github__actions_list,
     list_workflow_runs, branch filter).
   - Render the start menu (UiPreview) and the lightning (HakiColourShots, SparkPreview) once more after any change, and send the owner
     the colour pages and the seven-colour lightning sheet with SendUserFile (say they are previews, not the game).
2. Ask the owner (one AskUserQuestion, after 3.6.0 is out): should the Quake N ultimate's own impact frame go too (frames only on
   clashes), and should Meteor Fist's speed and blast still grow with the hold.
3. Wait for the owner's notes on 3.5.9 and 3.6.0.
4. **/hp op on/off (8zzv 9).** Find the original's op switch: its command (com.hexhaki.command.*: look for "op") and what it sets
   (likely HakiServerController.noCooldowns, HakiData.unlimitedEnergy or a flag of its own). Make every HexPiece fruit kit (server/*Kit,
   *Moves: their cooldowns through FruitServer / FruitData cooldown maps and FruitEnergy.COSTS) and every style (StyleServer, SwordServer,
   IronFist's `cooled`/`cool`, Clash rules) obey it the way the original's moves do: no cooldowns and no energy cost while it is on.
   Test it (a pure rule where possible).
5. **The Mochi list (8zzv 11)** after asking for the cut-off line and its picture. The Mochi code: server/MochiMoves, MochiKit,
   client/mochi/*, mochi/MochiArt (pure art), tools/mochi_anims.py, tools/preview/MochiShots. Read next-ai-3.2.2.md 8zzn to 8zzq first
   (the owner's Mochi requests and what 3.5.5 / 3.5.6 built: square arms, fists at their end). Arms "turned into mochi" means the arm
   itself is drawn as mochi (hide the vanilla arm part while the mochi arm is out, the way Gear 5 / Nika's rubber arm replaces the arm:
   read how client/gear does it), not a second arm over it. The material: deeper shading, colour variation, specular, a soft
   subsurface-like rim; render it beside the current one and judge.
6. **Buzz Cut Mochi (8zzv 10)**: references first (the owner said so): Katakuri's Zangiri Mochi and Buzz Cut Mochi against Luffy on Whole
   Cake Island (the anime episodes, the fandom wiki onepiece.fandom.com is reachable from the HexPiece environment; tenor/giphy GIFs too).
   Describe the references in the handoff, plan the phases (the wheel, the reform, the spiked Haki mace forearm, the stick, three
   windmill rotations, the overhead slam, the crater), ask the owner if anything is unclear, then build it as one uninterrupted
   cinematic (both parties in a cutscene: camera like Galaxy Impact's, IronFx.camera / DragonCamera.position chain), its two impact frames
   (the owner's newer specific word for this move overrides "frames only on clashes" for it), 5-minute cooldown, replacing Zan Giri Mochi
   while Haki is on.

## G. How to work here (what this chat learned)

- **The cloud environment**: a new session may start on "Default", which refuses freesound.org. get_environment, then
  switch_environment to "HexPiece" (env_01PmnEXrwDhxzij8M5q6P6bL, mode network_only, its switch_statement copied exactly). It allows
  freesound.org, cdn.freesound.org, opengameart.org, tenor/giphy, onepiece.fandom.com, static.wikia.nocookie.net.
- **Build**: `python3 build.py` (about 70 s; 13 test suites). Logs: redirect to the scratchpad and grep for error|exception|FAIL.
- **Previews** (compile into the scratchpad, never into build/classes, except a one-off recompile of a changed view class):
  `CP=build/classes:build/base.jar:build/stubs; javac -nowarn -cp $CP -sourcepath tools/preview -d $SCRATCH/prev tools/preview/X.java`
  then `java -cp $SCRATCH/prev:$CP X ...`. FxPreview (IronShots, MochiShots, ...) needs the dragon cache:
  `D=$(ls -d build/dragon-cache/*/ | head -1); java ... FxPreview $D/seiryu.sry $D/seiryu.png out.png --size=480x360 'e:yaw:pitch:dist:tx:ty:tz|ishot:age'`.
  UiPreview: `java ... UiPreview outdir`. Ryou arms: `python3 tools/ryou_preview.py outdir`. Clips: `python3 tools/anim_preview.py
  out.png clip.json --frames N --views front,side,three`; for big chosen ticks write a small script on `tools/anim.py` (anim.load,
  anim.draw); for sword clips with the katana use tools/sword_rig.py and tools/sword_view.py (saya_matrix, arc).
- **Animation lint**: build.py runs anim.lint (an arm through the head fails). A raised right arm's ROLL swings it toward the head:
  keep roll near 0 and use yaw to swing it out.
- **The original's code**: unzip input/hexhaki-2.1.0.jar's classes into the scratchpad and `javap -p -c -constants`. HakiServerController
  is huge: dump it to a file and grep. Useful entry points found this chat: HakiServerController.onAction (its tableswitch on
  HakiAction), sovereignStrike / stopWifiHaki (WiFi Haki), Binds (the nine quick slots; HAKI_BASE 100; defaults fill 100..103 into free
  slots), FruitServer.togglePowers, GearServerController.cancelAll / endGearFive, HakiAnimations.playRemote ("__clear__" fades a clip
  out), HakiVfx.acceptWifiHaki0.
- **Patch rules**: a new redirect row names caller, call, new owner/method and the exact count; a new jump needs a FrameNode after its
  label; widen a private original method in WIDENED to call it; every class the patch touches must be in build.py PATCHED.
- **Sounds**: tools/web_sounds.py (CC0 only, mono, normalised, faded); this chat added the BASS eq. Check every clip with
  tools/tonal_lines.py against noise; on thunder the low-band "line" is the thunder's own low end (compare with the accepted
  conqueror/web/thunder3); a narrow line more than ~6 dB over noise or a sub hum (under 60 Hz) means a different recording.
- **Lag**: every effect under ~16k quads (DragonTests); the lightning's kinks cost quads: keep STEP at 3 unless the owner asks for more.
- **Stop rule** (CLAUDE.md): at 98% weekly usage write the handoff and next-prompt first, commit, push, stop without checking CI.

## H0. What 3.6.0 did about the owner's messages 19 to 22 (section I) before it was pushed

The causes, found by measuring: (1) `ConquerorStorm.width` drew the release's bolts up to 3.6 x 0.75 = 2.7 blocks wide (a move's
blast 1.3 blocks wide 10 blocks out), and with 3.6.0's kink every 3 blocks they folded over themselves; (2) each run's heading was a
free wander (h x 0.35 + way x 0.65 + random x 0.85), so a bolt could turn back on itself: tangles and loops; (3) `Sphere.directions`
then "down strands land, up strands rise" left no bolt going out level: the chest-high middle always empty (the owner's green lines);
(4) the previews (SparkPreview.render and so HakiColourShots) drew BoltMesh's glow band OPAQUE (dark red fading to solid black), while
the game (HakiBolts.write) draws it as its hue with alpha = brightness: the "thick black outlines" in the sheet were partly the preview's.
What changed (one model: the release's, every blast's with it, POINT ONE 3 kept):
- `ConquerorStorm.WIDEST` 0.9 (was 3.6): 0.68 blocks at the widest at size 1. `HIT_WIDTH` keeps `strikes()` (the release striking
  bodies for its stun) as wide as before: gameplay unchanged. A branch's width share is width / WIDEST (it divided by the old 3.6).
- The path: `bolt()`'s non-gliding runs zig-zag off the bolt's own way by ZIG_LEAST 25 to ZIG_MOST 55 degrees, alternating sides (80 %),
  across a side that turns slowly (`across`); a run that strayed past 1 + 0.12 x walked from its line zags back. The glide of a
  downward strand to where it lands is as before (landing tests pass). WANDER is gone.
- `ConquerorStorm.spread(count)`: a golden-angle turn round the player per bolt, by thirds: level (LEVEL_LEAST 3 to LEVEL_MOST 14 degrees
  up from the chest), up (20 to 72), down (15 to 55 under: they land and tear as before). Used by `release` and `blast` (blast's old
  "two in three flipped up" is gone). A bolt not sent down never drops under AIR 0.7 over the ground.
- `BoltMesh.near(dist)`: within NEAR 8 blocks of the eye a bolt's width falls to NEAR_LEAST 0.1 at the eye (one passing a third-person
  camera never walls the screen).
- `SparkPreview.glowed`: the preview draws glow as the game does. DragonTests' blast width checks follow WIDEST.
- Renders judged: the seven-colour sheet from the owner's camera (HakiColourShots), and the scratchpad's Burst (the release round the
  player from the front, behind in third person and far; a blast): thin crooked bolts every way, the middle filled.

## H. 3.6.0 pushed (this file's last update)

- **3.6.0 is green on GitHub: Build HexPiece #219, Release HexPiece #151** (commit 47392ce).
- Sent the owner (SendUserFile, said to be previews, not the game): the start menu's Haki colour page (creation-3) and Ryou colour page
  (creation-4) from UiPreview, the seven-colour lightning sheet from HakiColourShots (after the thinning, the owner's own camera) and the
  scratchpad's Burst sheet (the release round the player from the front, behind and far, and a blast). Wait for their word on the new
  lightning before anything else touches it.
- Next: section F steps 2 to 6 (ask the two open questions; /hp op on/off; the Mochi list after asking for the cut-off line; Buzz Cut
  Mochi).

## I. The owner's new message during 3.6.0's release (verbatim; 3.6.0 not pushed yet when it came)

19. "JESUS WHY IS THE LIGHTNING SO THICK" (with a screenshot of HakiColourShots' sheet: the release seen over the shoulder at t6 and
    t14 and a move's blast at t4, in all seven colours: wide flat ribbons with thick black outlines, folding at every kink).
20. "this is how the lightnign should shoot, the green cirtcle is the player." (with a picture: an anime frame, Wano, two huge
    characters behind; the player circled small in the middle; the Conqueror's lightning bursting OUT OF THE PLAYER in every direction,
    left, right, up and down, long bolts reaching to the frame's edges, each crooked with sharp angular zig-zags and forks; near the player
    thin white-hot lines, further out dark red bodies outlined in a bright white-pink glowing rim; the widest bolts about a third of the
    player's height across (about half a block), most thinner: nothing near the 2.7 blocks the release was drawn at.)
21. "see how different it is from your mess?" (with a crop of our HakiColourShots render: the release over the shoulder, its bolts wide
    black-and-red ribbons stacked and folded on each other at every kink, a tangle above the player.)
22. "notice that the green lines are always empty, you are never using those spaces, I been noticing that, its only shooting down and
    up, the middle is empty" (with our render, two green lines drawn level out to either side of the player at chest height: no bolt
    ever goes out sideways there; the release's bolts only rise into the sky or come down onto the ground.)
23. The owner's answers (AskUserQuestion, after 3.6.0 was green): the Quake fruit's N ultimate's own impact frame: "Keep it"; Meteor
    Fist's speed and blast growing with the hold: "Fixed too" (same speed and blast whatever the hold, like the size). Section F step 2
    is done; 3.6.1 makes Meteor Fist's speed and blast fixed.
24. Asked which fixed power (AskUserQuestion): "Full power always" (every throw the old full hold's: speed 4.2, blast 6, damage 22;
    holding Z only aims).

## J. 3.6.1 (the owner's answers 23 and 24)

- `IronMove.meteorCharge` always 1.0 (was a third at a tap, full after METEOR_FULL 40 ticks): every Meteor Fist throw is the full hold's
  (meteorSpeed 4.2, meteorBlast 6, damage 10 + 12, crater, debris, quake and boom at their full). Held METEOR_MOST 80 ticks it still
  throws by itself. Its book text: "Every throw is at full power however long you hold." HudTests checks every hold is full.
- The Quake fruit's N ultimate keeps its CLASSIC frame (S2CClash.framed): no change.
- .github/release-notes-3.6.1.md. Next: section F steps 3 to 6 (/hp op on/off for every new fruit and style; the Mochi list after
  asking for the cut-off line and its picture; Buzz Cut Mochi). Wait for the owner's word on 3.6.0's lightning first if it comes.

## K. 3.6.2: /hp op on/off for every new fruit and style (8zzv 9, the owner: "All new fruits and every style have to obey /hp op on/off")

3.6.1 is green (Build #221, Release #152; the handoff push before it Build #220).
What the original's op does (javap): `/hp op on|off` (HakiCommands.setOp) sets HakiData's "opMode" flag; `HakiData.op(player)` (server
side) is read by FruitData.ready (cooldown passes), FruitData.cooldown (none set), FruitData.unlocked (every move open), FruitServer.pay
and .attacked (no energy), HakiData.unlimitedEnergy (so HakiData.consume is free), HakiData.longValue and GearData.longValue (cooldown
keys read as 0), HakiServerController.noCooldowns, LightKit.
HexPiece: the fruits' cooldowns already went through FruitData (every *Moves.ready/cool; Mochi through ItoMoves'; the Gura ultimate
through GuraMoves.cool; Gear 5's through GearData and NikaWhip's noCooldowns). New `server/OpMode.on(player)` (HakiData.op) now also
skips: FruitEnergy.afford/spend (no energy), StyleServer's mastery lock and move cooldowns (set and checked), IronFist.cooled and Galaxy
Impact's energy check, SwordServer's Haki slash / charged cut / quick draw cooldowns (set and checked), DragonServer's RESTING after
turning back. StyleTests.opMode checks each. The client's book and HUD still show style locks by mastery (the server lets them through).

