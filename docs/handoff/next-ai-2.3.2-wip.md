# HexPiece: handoff, 2.3.2 work in progress (stopped by the owner mid-work)

Read CLAUDE.md first, then next-ai-2.3.1.md (the armor) and next-ai-2.3.0.md (the clash rules in the owner's words). Branch
`claude/nice-bell-gam7qf`. Released: 2.3.1. **The next version is 2.3.2** (`build.py` VERSION is still 2.3.1; bump with
`tools/next_version.py --write`, write `.github/release-notes-2.3.2.md` when it is ready for the owner). Nothing was ever tested in game.

**This push is unverified work in progress**: committed the moment the owner said stop. `python3 build.py` was NOT run on it (the
SwordLayer change below has never been compiled) and CI was not checked. First thing: run `python3 build.py`, fix what fails, check the
branch's "Build HexPiece" run is green.

## Uncommitted-until-now work in this push (all unverified by build)
- **New NPC skins** (`tools/npc_skins.py`, rewritten): 128 px Alex skins with HexGhoul's human faces (`art/npc/human_NN.png` and
  `_closed.png`, sources in `art/npc/SOURCES.txt`), hair fixed at the back of the head (`hair_back`, `clean_sides`), 8 painted pirate
  outfits (stripes and bandana, vest, bodice, blue stripes, green officer coat, plum coat, red captain coat with skull tricorn, black coat
  with feathered tricorn) and Marines in a white shirt and navy trousers (the armor goes over them). Writes `textures/piece/npc/
  <side>_<n>.png` and `<side>_<n>_closed.png`. Preview: `docs/images/npc-skins.png` (`tools/preview/ArmorPreview.java outfits`).
- **NPC slim model + blinking**: `client/npc/SeaFighterRenderer` uses the slim player model (`ModelLayers.f_171166_`, `super(root, true)`)
  and a blink layer (closed-eye textures for 3 ticks, `npc/Blinks.java`, a port of HexGhoul's EtoRules.blink: every 80 ticks).
  `client/armor/ArmorClient` gives sea fighters the armor's slim fit. **Check `ModelLayers.f_171166_` and `m_174023_` are proven**
  (verify/api-allowlist.txt) or PieceVerify will fail.
- **The pull-out bug the owner circled ("pulls sword out, but nothing goes with the arm?")**: during the noto slide, before the draw tick,
  the blade stayed in the saya while the hand pulled. Fixed in `client/sword/SwordLayer.java` (`sliding`: the blade rides the saya's curve
  by `notoOut` with the hand) and `tools/sword_anims.py` (`noto_out`, `sliding`, `game_blade`, the preview). Rendered fine for the katana.
  **Still wrong**: SwordLayer's `SORI = 180` for every blade; Gryphon's curve radius is about 78 model px (sheath 79.3), so its hidden part
  pokes out of the scabbard mid-slide. Make sori per katana (Katana.sori(), rig.arc(out, sori), sheathed_grip).

## The owner's open requests, newest first (quoted)
1. "pulls sword out, but nothing goes with the arm? lol" (fixed above, Gryphon's curve still to fix), "HERE IT IS the bug": Yoru's draw
   cut at t3 shows Yoru both on the back and in the hand. "Gyphon and Yoru's pull outs are broken review each, and fix it, you can clearly
   see in the sheets that it's fucking broken" / "THE FULL SHEET, thats not all": render the FULL contact sheets of every Gryphon and Yoru
   move (`sword_anims.preview`), judge every frame.
2. "using sword makes the player's body too stiff, the head doesnt move, and yoru clips through the floor, adapt it so the body is more
   free, more real, more ambient animation with all swords. The pull out animation is way too fast on some swords, yoru should be slower
   than all. Yoru and Gryphon have custom ambient animations." and "Also the left arm is not even moving... why is it frozen".
   Diagnosis: `sword_guard` (24-tick loop) keys head, body, torso and both arms, and PlayerAnimator freezes every keyed part. Plan:
   - guard becomes arms-only (rightArm + rightItem); separate standing idles with ambient life; never key head or leftArm while walking.
   - Draws slower: katana 28 ticks (beats scaled from 22, draw tick 16 to 20), Gryphon 32, Yoru 44 (slowest). Sheathes likewise.
     SwordRules.DRAW_TICKS/SHEATHE_TICKS, SwordAnims.duration/name per katana, BladePaths draw/sheath ticks, SwordSounds beats, tests.
   - Gryphon its own draw/sheathe on its curve; Yoru's `sword_draw_back` rebuilt (now the hand jumps, the blade crosses the body at
     t16.5-19 and ends in the floor at t22) and `sword_sheathe_back` (starts in the floor).
   - Yoru clips the floor because it uses katana moves (guard, cut ends, mid-cuts): generate "_yoru"/"_gryphon" variants of every drawn
     move with ground clearance (tip = grip + dir x (tip_px - GRIP), checked in entity space with rig.layer_to_entity; rotate toward up
     until the tip is above about 0.08 blocks). Yoru tip 32.2 px, pommel -11.81; Gryphon tip 22.44, pommel -5.96.
   - Legends must never draw from a hip saya mid-fight (Yoru's quick draw/draw cuts reach for a saya it does not have).
   - Custom Yoru and Gryphon carries and idles (Yoru on the right shoulder, "present" pose).
3. "the NPCS are all alex, can have the same ambient animations as HexGhoul, Ken, touka, eto, Moro, etc etc etc": Alex done (above);
   port HexGhoul's ambient gestures (EtoGesture, EtoPose, RigPose subset; persona idles; the server picks the gesture, the client poses).
4. "when a player suffers the finisher, a line also should show on the middle of their screen vertically (same as the cut)"
   (FinisherFx.screen exists: check it shows for the victim, render it).
5. "the Marines and Pirate skins are shit and need to be recreated and bettered, you CAN take skins from HexGhoul": done above, judge again.
6. Clashes: "every move is clashable (combat style wise, and yes you can take the impacts from HexGhoul's latest version, however I want a
   impact frame whenever a clash happens.)" and "to clash with swords man, any form of armament haki HAS TO BE ON, otherwise the sword cant
   be clashed and since it has more range, will come up on top, if haki is on, clash is possible. When 2 ryou hakis clash, both lightning
   colors explode outwards from the clash area in a sphere (similar to conquerors but smaller)."
7. Older: Galaxy Fist for Iron Fist (Ryou only), the Fishman Karate rework, Six Powers (next-ai-2.2.6.md).

## Files to know
`tools/sword_anims.py` (moves, `yoru()`, `stance_write`, `java_paths`, `java_npc_poses`; `--only a,b`, `--preview`), `tools/sword_rig.py`
(rig, `arc`, `saya_matrix`, `back_matrix`), `client/sword/SwordLayer.java`, `SwordClient.java` (priority 920), `StanceLayer.java` (850),
`sword/SwordAnims.java`, `sword/SwordRules.java`, `sword/SwordSounds.java`, `server/SwordServer.java`, `Katana.java`.
