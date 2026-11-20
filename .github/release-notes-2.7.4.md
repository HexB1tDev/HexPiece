# HexPiece 2.7.4

Smaller and lighter. Nothing here has been tested in game yet; the build's checks pass.

- **The mod is 24.0 MB instead of 32.7 MB** (2.6.9 was 30.7 MB before the dragon was added):
  - The katana and legendary blade models are stored in a tighter format: 15.1 MB down to 6.0 MB, with exactly the same geometry.
    Every one of the 150 models was checked to load identically in the game's own loader.
  - The blades' textures no longer carry an alpha channel they never used: 0.64 MB less, every pixel the same.
- **Swords draw faster:** each blade and saya vertex is written in one call instead of seven, so many swordsmen on screen (you,
  players, pirates and marines) cost much less every frame. Same models, same look.
- Your input files are untouched: only the copies packed into the jar changed.
