# HexPiece

Forge 1.20.1 mod (mod id `hexhaki`) built from the supplied `hexhaki-2.1.0.jar` (HexHaki: Armament, Observation and Conqueror's Haki,
Devil Fruits, gore). The original Gradle project was never available, so this repository contains the **exact input JAR**, a narrow,
verified bytecode patch (`patch/PiecePatch.java`) and a separately compiled expansion package (`com.hexhaki.piece`), the same way the
owner's HexGhoul repository is built.

## Build

```bash
python3 build.py
```

Needs Java 17+ and Python 3 with Pillow for the preview tools; no downloads. Output: `build/hexpiece-<version>.jar`.

## Releases

- Every push to a `claude/**` branch publishes the jar as the pre-release `build-<branch>`.
- Pushing `.github/release-notes-<version>.md` publishes `v<version>`; afterwards only the newest 5 releases and 5 workflow runs are kept.
- Versions: after x.y.9 comes x.(y+1).0 (`tools/next_version.py`).

## Changes

See `.github/release-notes-*.md` for each version and `docs/handoff/` for the state of the work.
