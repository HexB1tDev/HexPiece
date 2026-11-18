# HexPiece: handoff after 2.5.0 (the evil release's half-health damage)

Read CLAUDE.md first. This note is the newest; next-ai-2.4.9.md covers 2.4.9 (the Conqueror's release by alignment, the ramp, the
partial lightning stun, the hit's tint, the 1-block slash: the design and where it lives), next-ai-2.4.8.md the evil sounds' remake,
next-ai-2.4.7.md the animations. Branch `claude/nice-bell-gam7qf`. **2.5.0 is released**; **the next version is 2.5.1**
(`python3 tools/next_version.py --write`). Never tested in game.

## The owner (session 6, after 2.4.9, verbatim)
"And I do think that the evil one should actually deal damage, but never kill. It should always, deal, make it deal, half the current
HP of the mob or person hit. Always half, but even if the player or entity has half a heart, then it doesn't deal any damage. It's not
meant to kill."

## Done (2.5.0)
- Read as: an evil release (full or partial) damages everything it hits (all it fells or pushes in its radius, the original's hurt
  calls, and everything its lightning strikes), not only lightning hits as in 2.4.9 (the owner's "should actually deal damage"
  answered 2.4.9's "only what the lightning hits takes damage"). The notes say so; ask if lightning-only was meant.
- `ConquerorStun.evilDamage(health)`: half, 0 at SPARED 1.0 (half a heart) or under; `hurts(alignment)`: not good.
- `ConquerorRelease.halve(body, source)`: hurt for half the health left (the game's protections apply: creative, invulnerability
  ticks), then, if armour took some, the health set to exactly half (LivingEntity.m_21223_/m_21153_, proven). `hurt` (the redirected
  original calls): good false, evil `halve` once a body a release (Release.hurt ids), neutral the original's. `apply`: an evil
  release's struck bodies the radius did not hurt are halved too. LightningTests: half, nothing at half a heart, never kills over 50
  releases.

## Open
1. The owner's report on 2.4.9/2.5.0 (alignments, ramp, partial stun, circle colour, slash, evil damage) and on 2.4.7/2.4.8.
2. From before (next-ai-2.4.9.md's list).
