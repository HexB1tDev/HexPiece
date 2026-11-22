# HexPiece 3.2.0

A Creative tab with only the Devil Fruits, a rarity rank for every fruit, and a fruit energy bar over the food bar that every fruit move
spends (your answers: the proposed ranks, and "Moves spend it"). Not tested in game yet: the build and tests pass, and the bar was
rendered and checked in all its states.

**Devil Fruits tab**
- A new Creative tab, "Devil Fruits", with every fruit and nothing else: the Legendary ones first, then the Mythical ones, the Nika
  fruit last.

**Rarity ranks**
- Mythical: the Nika fruit (Gear 5) and the Uo Uo no Mi. Legendary: the Gura Gura, Pika Pika, Magu Magu and Hie Hie no Mi. Common,
  Uncommon and Rare are kept for fruits to come.
- A fruit's name is written in its rank's colour everywhere it shows (gold for Legendary, pink-purple for Mythical), and its tooltip
  shows the rank with its stars under the name.

**Fruit energy**
- A new bar right over the food bar, in your fruit's own colour: a slanted bar filling out from a little swirled fruit emblem on its
  right end. It moves up a row while you are in water (where the air bubbles go). It only shows while you have a fruit, and not in
  creative or spectator.
- Every move of the Light, Gura, Magma and Ice fruits and of the dragon spends energy: small moves about 15 to 20, big ones 30 to 40,
  the Gura ultimate 70, a quick right-click tap about a third of its move. What a move just cost shows in white a moment before it
  drains away.
- Energy refills by itself (empty to full in about half a minute) after a second of rest from your last move. A move you cannot
  afford does not go off: the bar flashes red and shakes, and "Not enough fruit energy" shows above the hotbar. When it is low, the
  bar blinks.
- Gear 5 (the Nika fruit) does not spend energy yet: its moves are the original mod's own code. Creative mode never spends energy.
