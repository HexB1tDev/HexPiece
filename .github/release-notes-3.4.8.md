# HexPiece 3.4.8

A command for testing the fighting styles, and a first optimization pass. Not tested in game yet: the build and all tests pass.

**New command (operators)**
- `/hexpiece style mastery` shows your fighting style's mastery.
- `/hexpiece style mastery <0-1000>` sets it; every move it opens on the way is announced ("... learned!").

**Optimization (nothing looks or plays different)**
- The Haki lightning (the Conqueror's release and every Ryou move's lightning) no longer creates thousands of throwaway objects
  every frame: it reuses its buffers. Checked to draw exactly the same quads as before.
- Sparks, streaks, rocks and tubes in every fruit's effects reuse their buffers too.
- A dragon (Uo Uo no Mi) out of your view is no longer posed and drawn every frame.
