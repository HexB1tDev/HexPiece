#!/usr/bin/env python3
"""3.5.9: WiFi Haki's clip (the owner: "The user (while holding the key) slowly unsheaths the sword JUST A LITTLE BIT, the lightning
emergers from the opening in the sheath").

Writes src/main/resources/assets/hexhaki/player_animation/wifi_haki.json (the original's clip name: the original plays it when WiFi Haki
starts and clears it when it ends) from the sword draw's own solved poses (sword_draw.json, tools/sword_anims.py: the right fist on the
hilt, the left hand bringing the saya forward, its thumb on the guard), each beat the draw's pose at the tick its blade is as far out
as WiFi Haki's (sword/WifiDraw.drawTick, mirrored here): the hand settles on the hilt, the thumb breaks the seal, the blade eases slowly a
little way out, then a loop holds it there trembling. Only the arms, the upper body's turn and bend and the saya's frame (leftItem) are
keyed: never the head or the legs (the player still looks round and walks).

Usage: python3 tools/wifi_anims.py
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anim  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CLIPS = os.path.join(ROOT, "src/main/resources/assets/hexhaki/player_animation")
# sword/WifiDraw
GRIP, PUSH, OUT, LOOP = 9, 14, 40, 8
THUMB, MOST, TREMBLE = 1.2, 2.9, 0.08
DRAW_GRIP, DRAW_PUSH, DRAW_HOLD, DRAW_HALF, DRAW_HALF_OUT = 5.0, 9.0, 11.0, 17.0, 8.0
PARTS = {
    "rightArm": ("pitch", "yaw", "roll", "bend"),
    "leftArm": ("pitch", "yaw", "roll", "bend"),
    "body": ("yaw", "bend"),
    "leftItem": ("x", "y", "z", "pitch", "yaw", "roll"),
}


def in_out_sine(u):
    return -(math.cos(math.pi * u) - 1.0) / 2.0


def push(t):
    if t < GRIP:
        return 0.0
    if t < PUSH:
        return THUMB * math.sin((t - GRIP) / (PUSH - GRIP) * math.pi / 2)
    if t < OUT:
        return THUMB + (MOST - THUMB) * in_out_sine((t - PUSH) / (OUT - PUSH))
    c = ((t - OUT) % LOOP) / LOOP
    return MOST - TREMBLE * math.sin(math.pi * c)


def draw_tick(t):
    if t < GRIP:
        return DRAW_GRIP * max(0.0, t) / GRIP
    if t < PUSH:
        return DRAW_GRIP + (DRAW_PUSH - DRAW_GRIP) * (t - GRIP) / (PUSH - GRIP)
    y = (push(t) - THUMB) / (DRAW_HALF_OUT - THUMB)
    x = math.acos(max(-1.0, min(1.0, 1.0 - 2.0 * max(0.0, y)))) / math.pi
    return DRAW_HOLD + (DRAW_HALF - DRAW_HOLD) * x


def main():
    draw = anim.load(os.path.join(CLIPS, "sword_draw.json"))
    ticks = [3, 6, GRIP, 11, PUSH, 19, 24, 29, 34, OUT, OUT + LOOP // 2, OUT + LOOP]
    moves = []
    for t in ticks:
        pose = draw.pose(draw_tick(t))
        easing = "inoutsine" if t <= PUSH else "linear"
        for part, channels in PARTS.items():
            values = pose.get(part, {})
            for channel in channels:
                moves.append({"tick": t, "easing": easing, part: {channel: round(float(values.get(channel, 0.0)), 3)}})
    data = {"version": 3, "name": "wifi_haki", "author": "HexPiece 3.5.9",
            "description": "WiFi Haki held: the right hand on the hilt, the left thumb breaking the seal, the blade eased slowly a "
                           "little way out of the saya and held there trembling (client/sword/WifiPull slides it).",
            "emote": {"beginTick": 0, "endTick": OUT + LOOP, "stopTick": OUT + LOOP + 4, "isLoop": True, "returnTick": OUT,
                      "degrees": True, "easeBeforeKeyframe": True, "moves": moves}}
    with open(os.path.join(CLIPS, "wifi_haki.json"), "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=1)
        handle.write("\n")
    print("wrote wifi_haki.json:", ", ".join(f"t{t} draw {draw_tick(t):.2f} out {push(t):.2f}" for t in ticks))


if __name__ == "__main__":
    main()
