# HexPiece: handoff after 2.5.1 (the Conqueror's animations by alignment, upper body only; a stunned player's own view falls)

Read CLAUDE.md first. This note is the newest; next-ai-2.5.0.md covers the evil half-health damage, next-ai-2.4.9.md the release by
alignment (stuns, ramp, partial lightning, the hit's tint, the 1-block slash), next-ai-2.4.7.md the first animations and the release's
sounds. Branch `claude/nice-bell-gam7qf`. **2.5.1 is released**; **the next version is 2.5.2** (`python3 tools/next_version.py
--write`). Never tested in game. 2.5.2 (perf, below) is released too; the next version is 2.5.3.

## The owner (session 6, verbatim)
- "Continue to work while I test, theres tons to do"
- "tap g is good, the animation is fine" (conqueror_tap_point stays as it is)
- "only animate the top half of the body above the legs btw, evil gets one animation (EVIL/Disturbing) and good gets another
  (Peaceful/in pain) like a hand on the face and then a stare? idk"

## Done (2.5.1)
- **The owner's crash on activating Ryou** (crash-2026-11-18_12.54.15-client.txt, 2.5.0): `NoSuchMethodError
  RenderTarget.m_83961_(IIZ)V` in RyouGlow.composite (2.4.7's glow). In MCPConfig's 1.20.1 joined.tsrg, m_83961_ is egv `c (Z)V`;
  blitToScreen(int, int, boolean) is egv `c (IIZ)V` = **m_83957_** (2.4.7 read the wrong line; the stub and the allowlist evidence
  carried it, so the build passed). Fixed in RyouGlow, the stub, the allowlist. The glow's guard caught `Exception` only (a
  NoSuchMethodError is a LinkageError): now `Exception | LinkageError`, so it turns itself off instead.
  New check: `tools/srg_shapes.py` (reads joined.tsrg from GitHub raw, MCPConfig master, or a path) writes `verify/srg-shapes.txt`
  (547 members: each SRG method's obfuscated descriptor, each field); `build.py check_srg_shapes` (offline) fails when a HexPiece class
  references an SRG id with another descriptor shape, or one not in the file (then run the tool: it prints WRONG for bad ones). Proven
  with the old code: the build failed on m_83961_, the tool said "joined.tsrg has (Z)V". All 11,361 references checked; that was the
  only wrong one.
- `tools/conqueror_anims.py` rewritten: WRITTEN parts only (body bend, torso, head, arms: the legs and the body's height, lean and
  turn are the game's); three sets: neutral (the 2.4.7 power stance and crunch/burst, upper only), good (`conqueror_good_charge`,
  `_release`, `_release_partial`: FACE_HAND solved on the model with a grid search (scratch: the hand ~1.9 px from the face, no part in
  the head), STOMACH, PAIN_LIGHT -> PAIN_DEEP, heavy breath, trembling hand, jolts that carry the hand with the head (one that pitched
  the head alone put it through the hand: lint), the reveal 140-160 into STARE, the stare's loop; release: inhale, GOOD_PUSH, faint
  tremor, settle), evil (`conqueror_evil_*`: LIMP -> TILT (head roll 40), the sway, the shiver, EVIL_TWITCHES head/shoulder/fingers
  more and more often, four in the loop; release: EVIL_SNAP, EVIL_WIDE, the laugh (head bob 7 ticks), the drop to LIMP at 42).
  Judged with contact sheets and tick sheets (scratch); all lint-clean; loop seams whole cycles.
- `ConquerorStun.animation(name, alignment)` (pure; LightningTests checks every name exists and keys no legs);
  `ConquerorRelease.animate` (PiecePatch CALL_REDIRECTS: `animate` in beginConqueror (1) and releaseConqueror (2)).
- The stunned player's own first-person view (open since 2.4.4): `StunFall.view(angle, fallX, fallZ, yaw)` (pure, tested: backwards
  -> pitch to the sky, forwards -> the ground, sideways -> roll), `Tumbles.fallView` (ViewportEvent.ComputeCameraAngles, first person
  only; the yaw at the fall kept in `ownYaw`). The eye cannot be lowered (no event for the camera's position): the view turns as the
  head would.

## Done (2.5.2, the owner: "Continue")
- The full release's client cost, measured with a scratch harness (BoltPreview.conqueror(1.0, 1000, true, 8) through BoltPainter as
  the game paints it, plus the storm; 1,277 strokes; BoltMesh.build timed per frame, a counting Sink): worst frame ~7 ms of CPU for
  43,000 quads (third person), HEART strokes (375) built nothing but cost 0.43 ms, the dance 1.21 ms (a SplittableRandom and a
  Jag.across array a point a frame). Now: `BoltMesh.build` returns at once for HEART; `Bolt.dancing` caches each point's across
  vectors and reach and draws SplittableRandom's own SplitMix64 numbers without one (LightningTests: 6,000 checks the same values as
  SplittableRandom); `Bolt.bounds` (a lazily made sphere: dance and glow margins) + `BoltMesh.seen` (VIEW_HALF 80 degrees, wider than
  any FOV the game shows) cull strokes outside the view in `HakiBolts.draw` (the view's forward from the pose matrix). Third person
  ~15 % less, first person ~40 % less (the releaser stands in the dome). Not changed: the vertex writing (~170,000 vertices at the
  peak), BoltMesh's per-stroke arrays (~20 a stroke a frame: GC churn; a scratch-buffer refactor of BoltMesh's helpers would cut it),
  the original's Photon particles.

## Done (2.5.2, the owner's 2.5.1 report)
- **The owner's device** (from the crash report): Android, the Amethyst launcher, **MobileGlues** (OpenGL ES 3.2 on an Adreno 810,
  "GL version 4.0.0 MobileGlues 1.3.4"), Graphics fast, 8 cores, 3 GB heap. Mods: geckolib, fdlib, architectury, playeranimator,
  ldlib, photon, pehkui, bendylib, libraryferret, citadel, hexpiece. Everything rendered must work through a GL->GLES translation layer
  (this is also why additive/culling-dependent geometry drew as solid boxes): avoid framebuffer depth blits and exotic GL state.
- "bugs: the glow is too intense, and is flickering on and off": RyouGlow.INTENSITY 0.65 on the silhouette's alpha (the
  entity_outline shader's edge alpha and colour go with it: ~0.42 of the brightness); the world's depth is no longer copied into the
  chain's target (RenderTarget.copyDepthFrom, a glBlitFramebuffer of depth every frame: unreliable on MobileGlues, the likeliest
  flicker): the boxes are drawn without a depth test, so the glow shows through walls as vanilla Glowing does. Not seen in game.
- "tap G isnt stunning entities (tested on wild horse), the ring is still red (the ring that forms around the blast area)": the tap
  (CONQUEROR_TAP_CONE) is a Release too (`tap`): `target` takes every mob and player in its cone (any alignment); its hurt follows the
  alignment (good none, evil half, neutral the original's 4). The tap's rings/flash/smoke are `HakiFx.airRing/flash/smoke` (HakiFx
  emitters, never tinted; 0xB4FF9AA6 pinkish red): PiecePatch CALL_REDIRECTS in `HakiVfx.conquerorTapCone` -> `HakiTint.airRing/
  flash/smoke` (the colour through `conqueror`).

## Not done / next
1. If the owner still sees the release lag: FPS or the server? Client: BoltMesh's per-stroke allocations, merging the coaxial red/pale
   CORE strokes (visible change: judge renders), the Photon particles. Server: Fissures/Scars budgets.
2. The owner's reports on 2.4.7-2.5.1.
3. From before (next-ai-2.4.9.md's list).
