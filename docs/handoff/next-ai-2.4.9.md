# HexPiece: handoff after 2.4.9 (the Conqueror's release by alignment; the 1-block sword slash; the hit's tint)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.8.md covers the evil sounds' remake, next-ai-2.4.7.md the HUD, Ryou's hands,
the Conqueror animations (tools/conqueror_anims.py) and the release's old sounds. Branch `claude/nice-bell-gam7qf`. **2.4.9 is
released**; **the next version is 2.5.0** (`python3 tools/next_version.py --write`). Never tested in game.

## The owner (session 6, verbatim)
- "every mob hit with the lightning from the Conqueror's release is to be fucking felled. It does not deal damage. It just makes them
  stunned. Currently, only hostile mobs are being stunned. I don't see cows or anything else fall over. And the longer you hold the G
  button, at max charge, I think the stun should go up to 10 seconds, honestly."
- Asked (AskUserQuestion), answered: "Good Aligments = Stuns to hostiles and players only (15 seconds stun at max charge) (in the radius
  of the release) / Evil alignment = Stuns everything hit in the radius of the release, entities/players hit by the lightning take the
  damage (max stun 9 seconds)." Ramp: "Previous message." Partial: "Partial stun, the circle around is still red instead of having the
  ryou tint to it. entities hit by the lightnings ALL both evil/good stun all entities caught. 2 seconds."
- "the sword slash width on all charges should always be 1 block wide, 1x1, never more." Asked: "Hit and cut 1 block, wave too".

## Done (2.4.9)
- `haki/ConquerorStun` (pure, LightningTests.conquerorStun): GOOD_MAX 300, EVIL_MAX 180, NEUTRAL_MAX 200 ticks (neutral: my choice,
  the owner's first "up to 10 seconds"); `full(alignment, heldAtFull)` = most x (0.3 + 0.7 x min(1, held / RAMP 100)) (the start share
  and the 5 s ramp are my choices: the notes say so); LIGHTNING 40 (partial); `fellsAll` (evil full), `hurts` (good never, evil only
  struck, neutral as the original).
- `server/ConquerorRelease`: `current` Release (made in `visual` for CONQUEROR_RELEASE: seed, power, mastery, alignment, full =
  power >= 0.999, stun from FULL_SINCE; cleared in `sound`, the release's last call; `active` checks the player and the tick).
  `charging` (from AlignmentSounds' tick) keeps the tick each player's charge reached 100. `apply` (redirect of releaseConqueror's
  applyConqueror): finds `struck` first (the range lightning: `ConquerorLightningPath.generate(power, mastery, seed, true)`, HexPiece's
  LightningPaths on both sides, points from the feet, the body's box grown 0.52 + 0.24 power, AABB.clip as the original's charge
  contact; a full charge's storm: `ConquerorStorm.strikes`, strands within the body's half width + 0.6 + stroke at its height), calls
  the original, then: partial -> every struck body `fell` 40 ticks; evil full -> struck bodies past the radius hurt 10 and felled.
  `forceMax` (redirect of forceMaxConquerorStun): the original with the ramped stun, then every target in the radius: slowness and
  weakness reset to the stun (the original's push gave 200), HakiFalls.knockDown for the stun (also bodies already down). `target`
  (redirect of isConquerorCombatTarget in lambda$applyConqueror$1 and lambda$forceMaxConquerorStun$2): evil full -> any Mob or Player
  but the releaser, their vehicle and riders (`felled`); else the original's. `hurt` (PiecePatch VIRTUAL_REDIRECTS, new: the 3
  LivingEntity.m_6469_ calls in applyConqueror made static): ConquerorStun.hurts. WIDENED applyConqueror, forceMaxConquerorStun,
  isConquerorCombatTarget. Stubs: MobEffects.f_19613_, the 5-arg MobEffectInstance, AABB.m_82371_, Entity.m_20202_ (all used by the
  original, so proven).
- The red circle: `HakiVfx.conquerorPressureHit` (CONQUEROR_HIT, played on the struck body) has a Circle emitter and lightningFamilies;
  HakiTint took its colour from the struck mob (none). Now the accept0 hook passes the message (`HakiTint.visual`); a hit takes the
  colour of the last player whose visual was built within 3 s (`hitColour`), for its particles and (HakiBolts.colorOf) its bolts.
- Sword slash: `SwordRules.SLASH_WIDTH` 1.0 (the hit wall: slashThickness), `SLASH_BODY` 0.7 (the drawn body in the ground, tapering),
  TRENCH_MAX 1.0 (one cell a row: HakiSlashes.queueCells), `SlashMesh.squeeze` (everything drawn eased sideways into the 1-block slab
  with tanh past the body; from the side the wave is unchanged). SwordTests: thickness 1, one cell a row, every drawn corner within
  0.5 sideways. Judged with tools/preview BoltPreview.bigSlashSheet (scratch): a thin sheet from behind, the manga wave from the side.

## Open
1. The owner's report on 2.4.9 (alignments, the ramp, partial lightning stuns, the hit's colour, the slash) and on 2.4.7/2.4.8.
2. If the partial's lightning seldom catches anything (it is the literal bolts: 14-21 thin paths out to the radius), widen `grow`.
3. From before: neutral's old release sounds?, the explosion's 1 s delay, good's release sound, killing humans -> evil, the 2.4.5/2.4.6
   items, the Fishman Karate white screen, the stunned player's camera.
