# HexPiece: handoff after 2.2.1

Read CLAUDE.md first. This note covers 2.1.9 to 2.2.1; next-ai-2.1.5.md covers the 3D Haki lightning (2.1.5 to 2.1.8) and
next-ai-2.1.4.md the Swordsman. The next version is 2.2.2.

## Done
- **2.1.9**: whole sheaths (the katanas' distance meshes stay closed: `tools/katana_mesh.py` simplify with one position per cell, the
  hollow saya's inner wall left out), every glow alpha blended in `RenderType.debugQuads` (the owner's setup drew additive "eyes"
  opaque: black sword swings), Armament Haki and Ryou coat the hands only (`RyouHands.coating`).
- **2.2.0**: the Haki slash at full charge 64 tall, 220 far (`SwordRules`, `HakiSlashes`, `SlashRenderer`, new config keys).
- **2.2.1, the finisher taken from HexGhoul and turned upright** (the owner: "the slice in half should 100% be taken from Hex Ghoul ...
  the cut and everything about it. Just make sure instead of horizontal, it's vertical"). Source: HexGhoul branch
  `claude/eager-ritchie-xni1ms` (4.2.9 Steel Blade finisher: `BladeFinisher`, `FinisherClient`, `Carcass`, `Finisher`, `Ragdoll`).
  - `sword/Ragdoll` is HexGhoul's verbatim, plus `settle`: a piece in contact that hasn't moved 0.02 blocks or turned 0.03 rad in 40
    ticks sleeps (a half lying across a step's lip rocked for ever).
  - `sword/CutPose`: the body's pose as vanilla's ModelParts place it (pivot, ZYX turn, scale per part), read off the HumanoidModel
    the body was just drawn with (`FinisherFx.posed`), so the line and the halves are where the body was.
  - `sword/Carcass`: the cut is an upright `Plane` along the blade through the body's middle (`Plane.from(start)`, start the side
    the blade came in from). Every face of vanilla's cubes (HexGhoul's UV code) is clipped by it; a part it passes through by less
    than 1 px goes whole to one side (the legs, which overlap the middle by 0.1 px); each cut solid cube gets a section in 1/3 px
    cells (`Cut` clips each cell to the section, labels edges for the walls) coloured by `Finisher.inside` (a 3D anatomy in the
    part's own pixels: head = skull, brain folds, cerebellum, stem, mouth; trunk = fat/back muscle, spine knobs, canal and cord,
    vertebrae and discs, breastbone, ribs, heart and lungs, midriff, liver, guts; limbs = bone ring and marrow; MEAT; DRY bone).
    `wound` walks the section of each cut cube blown up past the skin layer (`LINE_OUT` + layer; the head's hat 0.5), drawn from
    the chest's front middle both ways by the angle about `centre`, hidden where it is pressed inside another cut part.
    `launch`: each half slides 0.02 off the cut and tips outward at 0.075 rad a tick.
  - `server/Finishers` = HexGhoul's `BladeFinisher` (held `Finisher.STUN` 40 ticks, slowness, motion stopped, mobs `setNoAi`, every
    attack/use/interaction cancelled, nothing hurts it), entered from `HakiSlashes` (Ryou slash that would kill). At the split:
    `S2CFinisher.SPLIT`, the original's `Gore.burst` both ways, `ModSounds.INTERNAL_HIT`, the kill with the slash's source.
  - `client/sword/FinisherFx` = HexGhoul's `FinisherClient`: the line in `RenderLivingEvent.Post` (entityTranslucent white, full
    bright), the body hidden (`RenderLivingEvent.Pre`/`RenderPlayerEvent.Pre`), the halves simulated against `level.clip` COLLIDER
    and drawn at AFTER_ENTITIES (skin in entityTranslucent, caps white), 40 s then a 5 s fade, the original's blood (`GoreRegistry
    .BLOOD` particles from each cut while falling, `BloodStains.pool` where a half rests). The victim's own screen line kept.
  - Render: `docs/images/finisher-upright.png`, `finisher-insides.png` (`tools/preview/FinisherPreview.java`, the owner's skin in
    `art/user_skin.png`); tests `FinisherTests`.
- **2.2.1, slow draw and sheathe** (the owner: "the sheath and unsheath is too fast. Slow those animations down. Don't make the blade
  usable until it's fully unsheathed ... Unless it's a quick draw"): `SwordAnims.DRAW` (`sword_draw`, 22 ticks, out of the saya at
  16), `sword_sheathe` 20 ticks (home at 17). `SwordServer.drawFirst`: from the saya a press or the charge plays the draw; a press during
  it is kept (`drawCut`), a charge held through it begins at its end (`drawCharge`); the quick draw cancels it and goes. Players no
  longer get the draw-cut moves (NPCs still may).

## Open (the owner's requests, in order)
1. **The Haki slash as a thick energy wave** (the owner: "it's not about just being tall. It's meant to be like an energy wave ... see
   Mihawk's energy projectile from his blade. It is much bigger than that, and it's thick"). A volumetric crescent with real
   thickness, not a plane; keep the server's cut consistent with what is drawn.
2. **One HUD on the left, HexGhoul's** (the owner: "all the abilities, everything has to be in the same menu, like HUD on the left ... I
   don't care about mastery in the bottom. I want you to copy the one from Hex Ghoul"): HexGhoul `client/AbilityHud.java`,
   `AbilityHudView.java`, `docs/images/abilities-hud.png` on `claude/eager-ritchie-xni1ms`. Drop the original's bottom mastery HUD.
3. **One menu** (the owner: "there's two different types of GUI ... the mastery menu, and then there's another one ... put them all on
   the same thing ... check the fruit mastery GUI. It's still old. ... reorganize the mod as best you can"): merge the original's
   mastery screens, Haki techniques, the fruit mastery GUI and the Captain's Log into one, in our style.

## Not verified (only the owner can test in game)
- The finisher in game: the pose read off the model under PlayerAnimator, the halves' textures on other players' skins, the blood
  pools' placement, performance with several corpses (each half ~1-2k cap quads), the skin layer distance with 3D Skin Layers.
- The slow draw's feel, and the Haki crackle on the slow slide (it uses the same noto beats).
