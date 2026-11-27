#!/usr/bin/env python3
"""3.8.3: writes the archipelago world generation's data (the owner: "World Generation, big one, the world now will have our own
generation, it supports TerraBlender and Biomes o plenty, the world will now generate in Arquipelagos (big islands but most of it will
be sea) key is that every single river IS CONNECTED TO THE SEA no matter what, it's meant to be made for a boat travel/exploration type of
thing. Make sure Structures spawn in the islands and the SEA is deeper than normal. (should support every modded structure) and every
island MUST have a beach, long.").

Only the overworld's terrain inputs are replaced (density functions; the biome source, the surface rules, the structures and every
mod's own worldgen are left as they are, so TerraBlender's regions (Biomes O' Plenty's) place their biomes and every mod's structures
spawn by biome as on any world):
  minecraft:overworld/continents  the islands: the island noise (hexhaki:archipelago, islands of a few hundred blocks to over a
                                  thousand) over the vanilla continents (whole regions of islands, and open seas), lowered (most is sea)
  minecraft:overworld/erosion     the vanilla erosion, held flat along the coasts (every coast a beach biome, never a stony shore)
  minecraft:overworld/ridges      the rivers' lines (hexhaki:river_lines: rivers that never close on themselves, so every river runs
                                  coast to coast) signed by the vanilla ridge noise (the biome variants as before)
  minecraft:overworld/offset      the vanilla terrain height (hexhaki:vanilla/offset), its coast band shaped into a long gentle beach,
                                  every river's channel held under the sea level along its whole line, the sea floor lowered (deeper)
The vanilla files it copies (offset, erosion) are read from a folder of 1.20.1's own data (misode/mcmeta 1.20.1-data), never committed.

Usage: python3 tools/archipelago_data.py VANILLA_DATA_DIR      (then: tools/archipelago_preview.py VANILLA_DATA_DIR out.png)
"""
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# --- the numbers -----------------------------------------------------------------------------------------------------------------
# the islands: the island noise's weight, the vanilla continents' weight, and how far it all is lowered (the sea's share)
ISLANDS = 0.95
CONTINENTS = 0.35
LOWER = -0.42
ISLAND_NOISE = {"firstOctave": -9, "amplitudes": [1.0, 1.0, 0.7, 0.4, 0.25]}
# the coast widened: the raw island value mapped so the coast band (continentalness -0.19 to -0.11, where the game puts its beaches and
# this the beach's gentle slope) spans a raw range nearly four times its own (-0.30 to 0.0): every beach about four times wider
WIDEN = [(-1.5, -1.5, 1.0), (-0.42, -0.42, 1.0), (-0.30, -0.19, 0.27), (0.0, -0.11, 0.27), (0.12, 0.0, 1.0), (1.5, 1.38, 1.0)]
# sea level 63: the surface's height for a total offset is about y = 128 + 128 x offset
def y_of(offset):
    return 128.0 + 128.0 * offset


def offset_of(y):
    return (y - 128.0) / 128.0


# the beach: across the coast band (continentalness), how much of the vanilla shape gives way to the beach's (0..1) and its height
BEACH_MASK = [(-0.27, 0.0), (-0.21, 1.0), (-0.10, 1.0), (-0.035, 0.0)]
BEACH_Y = [(-0.27, 50.0), (-0.21, 58.5), (-0.17, 61.8), (-0.135, 63.6), (-0.10, 65.2), (-0.035, 69.5)]
# the river channel: the most the ground may rise near a river's line (by ridges_folded, -1 on the line): under the sea there
RIVER_CAP = [(-1.0, 57.0), (-0.885, 58.0), (-0.79, 64.2), (-0.70, 80.0), (-0.58, 150.0)]
# the deeper sea: how much the floor is lowered, by continentalness (vanilla deep ocean floor ~y 36 -> ~y 4)
DEEPER = [(-1.2, -0.27), (-0.70, -0.25), (-0.455, -0.18), (-0.30, -0.10), (-0.19, -0.025), (-0.11, 0.0)]
# the coasts' erosion: held to this flat band (beaches, never stony shores) across the coast
EROSION_KEEP = [(-0.31, 1.0), (-0.24, 0.25), (-0.06, 0.25), (0.0, 1.0)]
EROSION_ADD = [(-0.31, 0.0), (-0.24, 0.1), (-0.06, 0.1), (0.0, 0.0)]


def spline(coordinate, points):
    """A smooth spline through (location, value) points, flat at each (derivative 0)."""
    return {"type": "minecraft:spline", "spline": {"coordinate": coordinate, "points": [
        {"location": float(loc), "value": float(val), "derivative": 0.0} for loc, val in points]}}


def write(path, data):
    full = os.path.join(DATA, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as h:
        json.dump(data, h, indent=2)
        h.write("\n")


def main():
    vanilla = sys.argv[1]
    vwg = os.path.join(vanilla, "minecraft", "worldgen")
    with open(os.path.join(vwg, "density_function", "overworld", "offset.json")) as h:
        offset = json.load(h)
    with open(os.path.join(vwg, "density_function", "overworld", "erosion.json")) as h:
        erosion = json.load(h)
    shifted = lambda noise: {"type": "minecraft:shifted_noise", "noise": noise, "shift_x": "minecraft:shift_x", "shift_y": 0.0,
                             "shift_z": "minecraft:shift_z", "xz_scale": 0.25, "y_scale": 0.0}
    c = "minecraft:overworld/continents"
    write("hexhaki/worldgen/noise/archipelago.json", ISLAND_NOISE)
    write("hexhaki/worldgen/density_function/vanilla/offset.json", offset)
    write("hexhaki/worldgen/density_function/vanilla/erosion.json", erosion)
    raw = {"type": "minecraft:add", "argument1": LOWER, "argument2": {
        "type": "minecraft:add",
        "argument1": {"type": "minecraft:mul", "argument1": ISLANDS, "argument2": shifted("hexhaki:archipelago")},
        "argument2": {"type": "minecraft:mul", "argument1": CONTINENTS, "argument2": shifted("minecraft:continentalness")}}}
    write("hexhaki/worldgen/density_function/islands.json", {"type": "minecraft:flat_cache", "argument": raw})
    write("minecraft/worldgen/density_function/overworld/continents.json", {"type": "minecraft:flat_cache", "argument": {
        "type": "minecraft:spline", "spline": {"coordinate": "hexhaki:islands", "points": [
            {"location": l, "value": v, "derivative": d} for l, v, d in WIDEN]}}})
    write("minecraft/worldgen/density_function/overworld/erosion.json", {"type": "minecraft:flat_cache", "argument": {
        "type": "minecraft:add",
        "argument1": {"type": "minecraft:mul", "argument1": "hexhaki:vanilla/erosion", "argument2": spline(c, EROSION_KEEP)},
        "argument2": spline(c, EROSION_ADD)}})
    write("minecraft/worldgen/density_function/overworld/ridges.json", {"type": "minecraft:flat_cache", "argument": {
        "type": "minecraft:mul", "argument1": {"type": "hexhaki:river_lines"},
        "argument2": {"type": "minecraft:clamp", "input": {"type": "minecraft:mul", "argument1": 1000.0,
                                                           "argument2": shifted("minecraft:ridge")}, "min": -1.0, "max": 1.0}}})
    base = "hexhaki:vanilla/offset"
    beach = {"type": "minecraft:add", "argument1": base, "argument2": {
        "type": "minecraft:mul", "argument1": spline(c, BEACH_MASK),
        "argument2": {"type": "minecraft:add", "argument1": spline(c, [(l, offset_of(y)) for l, y in BEACH_Y]),
                      "argument2": {"type": "minecraft:mul", "argument1": -1.0, "argument2": base}}}}
    capped = {"type": "minecraft:min", "argument1": beach,
              "argument2": spline("minecraft:overworld/ridges_folded", [(l, offset_of(y)) for l, y in RIVER_CAP])}
    write("minecraft/worldgen/density_function/overworld/offset.json", {"type": "minecraft:flat_cache", "argument": {
        "type": "minecraft:cache_2d", "argument": {"type": "minecraft:add", "argument1": capped, "argument2": spline(c, DEEPER)}}})
    print("wrote the archipelago's data under", DATA)


if __name__ == "__main__":
    main()
