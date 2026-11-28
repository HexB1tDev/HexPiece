# HexPiece 4.0.0

Fixes found going back over the Itsumade's new code (3.9.7 to 3.9.9) before you play it. **Not tested in game yet:** the build and
every test suite pass.

- **N now opens Hellish Teleport.** In 3.9.9 pressing N did nothing: N is HexPiece's own key and reaches the server under its own number,
  and the Itsumade compared it with something else. A test now checks it.
- **Every Itsumade move has its own fruit energy cost** (they all cost the default before): Wind Gust 20, Enormous Bite 25, Bow 45,
  Yokai Beam 45, Gorosei Stare 25, Enormous Presence 45, Yokai Blast 35, Thy Death 60, Hellish Teleport 40. The test that checks every
  kit's costs now checks the Itsumade's too.
- **The Conqueror's lightning with Ryou on, on every move** (the house rule): the Wind Gust (out over the cone), the Enormous Bite (at
  the jaws), the talons' grab, and Hellish Teleport (out of the blast up and out of the landing) throw it now too. Visual only, as
  everywhere.
