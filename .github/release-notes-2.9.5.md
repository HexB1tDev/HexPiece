# HexPiece 2.9.5

Your two notes on the Conqueror's release sounds after 2.9.4. Nothing here has been tested in game yet; the build's checks pass.

- **The charge sound stops when you let go.** You were right, it was the charge. The original mod plays its charge sound when you
  press G and again at 38, 58, 78 and 100 % charge, each a second late. That worked with its old 2-second sound. With 2.9.4's 9-second
  recording, five of them stacked up, and the last one started a second after a full charge, so it rang for up to 10 seconds after you
  let go. Now the charge is one sound: it starts when you press G, keeps going (a crackling loop of the same recording) for as long as
  you hold, and fades out within half a second of letting go.
- **No more pile of lightning crackles when you let go.** The loud "all at once" lightning was the 3D lightning's own crackle: a
  partial release played it four times at once (3.4 seconds each), and the charge's lightning kept adding more the whole time you
  held G. Both are silent now, as a full release has been since 2.4.7. What you hear after letting go: the boom for your path straight
  away, then one thunder a second later (a partial release); the world-ending explosion and the boom (a full release).
- **The three release thunders are equally loud now.** One was about 2 dB louder and one about 7 dB quieter than the third.
- **Gear 5's lightning no longer crackles.** 2.9.4 added that by accident when it made Gear 5's lightning 3D (Gear 5 has its own
  sounds).
- Correction: 2.9.4's notes said a full charge plays a thunder crack and rolling thunder after its explosion. It doesn't: you had those
  removed in 2.4.7, and they still are.
