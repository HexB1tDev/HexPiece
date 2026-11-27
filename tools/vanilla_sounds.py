#!/usr/bin/env python3
"""3.9.6 (the owner: "from now on all devil fruits are now gonna have minecraft sounds, (except explosions and haki related things)"):
every devil fruit's sound event plays one of Minecraft's own sound events (sounds.json: {"name": "minecraft:...", "type": "event"},
with a pitch and a volume of its own), never a recording of ours: the Uo (the dragon; the Itsumade borrows its roar and the Gura's
crack), Pika, Gura, Magu, Hie, Nika (gear and the original's gear5 events), Noro, Supa, Horo, Ito and Mochi fruits. Kept as they
are: the explosions (KEEP) and everything not a fruit's (Haki, the Conqueror, the swords, the guns, Iron Fist, the clashes). The
recordings no event plays any more are deleted (their credits stay in each folder's SOURCES.txt).

Usage: python3 tools/vanilla_sounds.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOUNDS = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "sounds.json")
CLIPS = os.path.join(ROOT, "src", "main", "resources", "assets", "hexhaki", "sounds", "piece")
FRUIT_FOLDERS = ("uo", "pika", "gura", "magu", "hie", "gear", "nika", "noro", "supa", "horo", "ito", "mochi")

# the explosions: their own recordings kept
KEEP = {"piece.uo.explode", "piece.uo.blast", "piece.uo.form", "piece.pika.explode", "piece.pika.kick_blast", "piece.gura.boom",
        "piece.gura.quake", "piece.gura.rumble", "piece.magu.boom", "piece.magu.burst", "piece.horo.boom", "piece.mochi.boom",
        "gear5_impact", "gear5_bajrang_impact"}

# event -> [(minecraft event, volume, pitch), ...] (more than one: the game picks one each time)
VANILLA = {
    # the Uo Uo no Mi (the dragon; the Itsumade's scream)
    "piece.uo.roar": [("entity.ender_dragon.growl", 1.0, 0.85)],
    "piece.uo.whoosh": [("entity.ender_dragon.flap", 1.0, 0.9), ("entity.ender_dragon.flap", 1.0, 0.75)],
    "piece.uo.unform": [("entity.illusioner.mirror_move", 1.0, 0.7)],
    "piece.uo.gust": [("entity.phantom.swoop", 1.0, 0.8)],
    "piece.uo.air": [("entity.phantom.flap", 1.0, 0.7)],
    "piece.uo.air_hit": [("entity.player.attack.knockback", 1.0, 0.8)],
    "piece.uo.tornado": [("item.trident.riptide_3", 1.0, 0.7)],
    "piece.uo.flame_start": [("item.firecharge.use", 1.0, 0.8)],
    "piece.uo.flame_loop": [("block.fire.ambient", 1.0, 0.8)],
    "piece.uo.charge": [("block.beacon.power_select", 1.0, 0.7)],
    "piece.uo.charge_build": [("block.beacon.activate", 1.0, 0.7)],
    "piece.uo.charge_hold": [("block.beacon.ambient", 1.0, 0.7)],
    "piece.uo.beam": [("entity.blaze.shoot", 1.0, 0.7)],
    "piece.uo.fireball": [("entity.ghast.shoot", 1.0, 0.8)],
    "piece.uo.ember_hit": [("block.fire.extinguish", 1.0, 1.0)],
    "piece.uo.lunge": [("entity.ravager.roar", 1.0, 1.0)],
    "piece.uo.bite": [("entity.evoker_fangs.attack", 1.0, 0.7)],
    # the Pika Pika no Mi (light)
    "piece.pika.laser": [("entity.guardian.attack", 1.0, 1.4)],
    "piece.pika.charge": [("block.beacon.activate", 1.0, 1.4)],
    "piece.pika.charge_hold": [("block.beacon.ambient", 1.0, 1.5)],
    "piece.pika.beam": [("block.respawn_anchor.charge", 1.0, 1.4)],
    "piece.pika.pop": [("entity.firework_rocket.blast", 1.0, 1.2)],
    "piece.pika.zap": [("entity.evoker.cast_spell", 1.0, 1.6)],
    "piece.pika.travel": [("entity.enderman.teleport", 1.0, 1.5)],
    "piece.pika.zip": [("entity.illusioner.mirror_move", 1.0, 1.5)],
    "piece.pika.whoosh": [("entity.phantom.swoop", 1.0, 1.6)],
    "piece.pika.hit": [("entity.player.attack.strong", 1.0, 1.2)],
    "piece.pika.hit_heavy": [("entity.player.attack.knockback", 1.0, 1.0)],
    "piece.pika.sword_on": [("block.beacon.activate", 1.0, 1.8)],
    "piece.pika.hum": [("block.beacon.ambient", 0.8, 1.8)],
    "piece.pika.swing": [("entity.player.attack.sweep", 1.0, 1.4)],
    "piece.pika.sword_off": [("block.beacon.deactivate", 1.0, 1.8)],
    "piece.pika.parry": [("item.shield.block", 1.0, 1.4)],
    "piece.pika.sparkle": [("block.amethyst_block.chime", 1.0, 1.4)],
    "piece.pika.light_travel": [("entity.enderman.teleport", 1.0, 1.7)],
    "piece.pika.reflect": [("block.amethyst_block.hit", 1.0, 1.5)],
    # the Gura Gura no Mi (quakes: its booms, quakes and rumble kept)
    "piece.gura.crack": [("block.glass.break", 1.0, 0.6), ("block.glass.break", 1.0, 0.5)],
    "piece.gura.charge": [("block.respawn_anchor.charge", 1.0, 0.6)],
    "piece.gura.crumble": [("block.deepslate.break", 1.0, 0.7), ("block.stone.break", 1.0, 0.6)],
    "piece.gura.wave": [("entity.warden.sonic_boom", 0.8, 0.9)],
    "piece.gura.whoosh": [("entity.phantom.swoop", 1.0, 0.6)],
    "piece.gura.hit": [("entity.player.attack.knockback", 1.0, 0.7)],
    # the Magu Magu no Mi (magma)
    "piece.magu.bubble": [("block.lava.pop", 1.0, 0.8)],
    "piece.magu.whoosh": [("entity.blaze.shoot", 1.0, 0.6)],
    "piece.magu.sizzle": [("block.lava.extinguish", 1.0, 0.8)],
    "piece.magu.roar": [("block.lava.ambient", 1.0, 0.6), ("entity.ravager.roar", 0.7, 0.8)],
    # the Hie Hie no Mi (ice)
    "piece.hie.freeze": [("entity.player.hurt_freeze", 1.0, 0.8)],
    "piece.hie.form": [("block.glass.place", 1.0, 0.8)],
    "piece.hie.crack": [("block.glass.hit", 1.0, 0.7)],
    "piece.hie.shatter": [("block.glass.break", 1.0, 0.9)],
    "piece.hie.hit": [("entity.player.hurt_freeze", 1.0, 1.0)],
    "piece.hie.age": [("block.powder_snow.break", 1.0, 0.6)],
    "piece.hie.frost": [("block.powder_snow.place", 1.0, 0.8)],
    # the Nika fruit (the transformation's drums, its cartoony moves)
    "piece.gear.drum": [("block.note_block.basedrum", 1.0, 0.8)],
    "piece.gear.drum_big": [("block.note_block.basedrum", 1.0, 0.5)],
    "piece.gear.dawn_pistol": [("entity.slime.attack", 1.0, 0.7)],
    "piece.gear.goof": [("block.note_block.didgeridoo", 1.0, 1.2)],
    "piece.gear.goof_punch": [("entity.slime.attack", 1.0, 1.2)],
    "piece.gear.goof_pop": [("entity.chicken.egg", 1.0, 0.8)],
    "piece.gear.whip_wind": [("entity.fishing_bobber.throw", 1.0, 0.6)],
    "piece.gear.whip_spin": [("item.trident.riptide_1", 1.0, 1.1)],
    "piece.gear.whip_crack": [("entity.fishing_bobber.retrieve", 1.0, 0.8)],
    "gear5_ambient_transformation": [("block.note_block.basedrum", 1.0, 0.7)],
    "gear5_pistol": [("entity.slime.jump", 1.0, 0.8)],
    "gear5_gatling": [("entity.slime.squish", 1.0, 1.1)],
    "gear5_rocket": [("entity.firework_rocket.launch", 1.0, 0.8)],
    "gear5_dodge": [("entity.slime.jump_small", 1.0, 1.2)],
    "gear5_giant": [("entity.ravager.step", 1.0, 0.6)],
    "gear5_whip": [("entity.fishing_bobber.throw", 1.0, 0.8)],
    "gear5_bajrang_charge": [("block.beacon.activate", 1.0, 0.8)],
    "gear5_bajrang_slam": [("entity.iron_golem.attack", 1.0, 0.7)],
    "gear5_coil": [("entity.slime.squish", 1.0, 0.7)],
    "gear5_sky_bounce": [("block.slime_block.fall", 1.0, 0.8)],
    "gear5_rubber_world": [("block.slime_block.place", 1.0, 0.7)],
    # the Noro Noro no Mi (slow beams)
    "piece.noro.photon": [("block.beacon.power_select", 1.0, 1.2)],
    "piece.noro.beam": [("block.beacon.ambient", 1.0, 1.2)],
    "piece.noro.slow": [("block.amethyst_block.resonate", 1.0, 0.6)],
    "piece.noro.store": [("block.beacon.activate", 1.0, 1.1)],
    "piece.noro.release": [("block.beacon.deactivate", 1.0, 1.1)],
    "piece.noro.mirror": [("block.amethyst_block.chime", 1.0, 0.8)],
    "piece.noro.field": [("block.portal.ambient", 0.6, 1.4)],
    # the Supa Supa no Mi (blades)
    "piece.supa.swish": [("entity.player.attack.sweep", 1.0, 1.1)],
    "piece.supa.draw": [("item.armor.equip_iron", 1.0, 1.1)],
    "piece.supa.cut": [("entity.player.attack.crit", 1.0, 1.0)],
    "piece.supa.glance": [("item.shield.block", 1.0, 1.2)],
    "piece.supa.rip": [("block.chain.break", 1.0, 0.9)],
    "piece.supa.skate": [("block.grindstone.use", 1.0, 1.2)],
    "piece.supa.spin": [("item.trident.riptide_1", 1.0, 1.3)],
    # the Horo Horo no Mi (ghosts: its boom kept)
    "piece.horo.ghost": [("entity.vex.ambient", 1.0, 0.9)],
    "piece.horo.despair": [("entity.ghast.ambient", 1.0, 0.8)],
    "piece.horo.pop": [("block.bubble_column.bubble_pop", 1.0, 0.8)],
    "piece.horo.spirit": [("entity.allay.ambient_without_item", 1.0, 0.7)],
    "piece.horo.network": [("entity.vex.charge", 1.0, 0.8)],
    # the Ito Ito no Mi (strings)
    "piece.ito.swish": [("block.tripwire.click_on", 1.0, 0.7)],
    "piece.ito.shot": [("entity.arrow.shoot", 1.0, 1.3)],
    "piece.ito.slice": [("entity.player.attack.sweep", 1.0, 1.5)],
    "piece.ito.burn": [("block.fire.extinguish", 1.0, 1.2)],
    "piece.ito.pull": [("entity.fishing_bobber.retrieve", 1.0, 0.9)],
    "piece.ito.web": [("block.wool.place", 1.0, 0.8)],
    "piece.ito.cage": [("block.chain.place", 1.0, 0.8)],
    "piece.ito.wave": [("entity.phantom.swoop", 1.0, 1.2)],
    "piece.ito.step": [("block.wool.step", 1.0, 1.0)],
    # the Mochi Mochi no Mi (mochi: its boom kept)
    "piece.mochi.stretch": [("block.honey_block.slide", 1.0, 0.8)],
    "piece.mochi.punch": [("entity.slime.attack", 1.0, 0.8)],
    "piece.mochi.squish": [("entity.slime.squish", 1.0, 0.8)],
    "piece.mochi.splat": [("block.slime_block.fall", 1.0, 0.7)],
    "piece.mochi.drill": [("block.honey_block.slide", 1.0, 1.2)],
    "piece.mochi.stomp": [("block.slime_block.fall", 1.0, 0.5)],
    "piece.mochi.slam": [("entity.slime.attack", 1.0, 0.5)],
    "piece.mochi.inflate": [("block.honey_block.place", 1.0, 0.7)],
    "piece.mochi.fire": [("item.firecharge.use", 1.0, 1.0)],
    "piece.mochi.roll": [("block.honey_block.slide", 1.0, 0.6)],
    "piece.mochi.pop": [("entity.chicken.egg", 1.0, 0.7)],
}

# 3.9.6: the mace's own sounds (the owner: the mace "has sword sounds"): a heavy iron club's
MACE = {
    "piece.mace.swing": [("entity.player.attack.sweep", 0.9, 0.55), ("entity.player.attack.nodamage", 1.0, 0.6)],
    "piece.mace.swing_heavy": [("entity.player.attack.knockback", 1.0, 0.6)],
    "piece.mace.iron": [("entity.iron_golem.attack", 1.0, 0.8)],
    "piece.mace.draw": [("item.armor.equip_iron", 1.0, 0.7)],
    "piece.mace.slam": [("entity.iron_golem.attack", 1.0, 0.55), ("entity.player.attack.strong", 1.0, 0.6)],
}


# 3.9.6: the Itsumade's bones cracking as it changes (it borrowed the Gura's crack: that is breaking glass now)
ITSU = {
    "piece.itsu.crack": [("block.bone_block.break", 1.0, 0.6), ("block.bone_block.break", 1.0, 0.5)],
    # 3.9.7: the passive: it vanishes in black smoke and stands again ten blocks away
    "piece.itsu.vanish": [("entity.illusioner.mirror_move", 1.0, 0.6), ("entity.enderman.teleport", 1.0, 0.5)],
    # 3.9.8: Gorosei Stare: the glare fixing on its target (the elder guardian's curse, low), and the target thrown (the warden's boom)
    "piece.itsu.stare": [("entity.elder_guardian.curse", 1.0, 0.7)],
    "piece.itsu.thrown": [("entity.warden.sonic_boom", 1.0, 0.85)],
}


def entry(sounds, subtitle, distance):
    out = []
    for name, volume, pitch in sounds:
        s = {"name": "minecraft:" + name, "type": "event", "volume": volume, "pitch": pitch}
        if distance:
            s["attenuation_distance"] = distance
        out.append(s)
    return {"subtitle": subtitle, "sounds": out}


def main():
    with open(SOUNDS, encoding="utf-8") as h:
        data = json.load(h)
    for name, sounds in list(VANILLA.items()) + list(MACE.items()) + list(ITSU.items()):
        old = data.get(name)
        if old is None and name not in MACE and name not in ITSU:
            raise SystemExit("not an event of ours: " + name)
        distance = None
        if old:
            for s in old.get("sounds", []):
                if isinstance(s, dict) and "attenuation_distance" in s:
                    distance = s["attenuation_distance"]
        data[name] = entry(sounds, (old or {}).get("subtitle", "subtitles.hexhaki." + name), distance or (24 if name in MACE else 48 if name in ITSU else None))
    # every fruit event either Minecraft's now, or an explosion kept
    for name in data:
        folder = name.split(".")[1] if name.startswith("piece.") else ("nika" if name.startswith("gear5_") else None)
        if folder in FRUIT_FOLDERS or name.startswith("gear5_"):
            if name not in VANILLA and name not in KEEP:
                raise SystemExit("a fruit's event left out: " + name)
    with open(SOUNDS, "w", encoding="utf-8") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    # the recordings nothing plays any more
    used = set()
    for e in data.values():
        for s in e.get("sounds", []):
            n = s["name"] if isinstance(s, dict) else s
            if isinstance(s, dict) and s.get("type") == "event":
                continue
            if n.startswith("hexhaki:piece/"):
                used.add(n[len("hexhaki:piece/"):])
    gone = 0
    for folder in FRUIT_FOLDERS:
        d = os.path.join(CLIPS, folder)
        for f in sorted(os.listdir(d)):
            if f.endswith(".ogg") and folder + "/" + f[:-4] not in used:
                os.remove(os.path.join(d, f))
                gone += 1
    print("fruit events on Minecraft's sounds:", len(VANILLA), "kept:", len(KEEP), "recordings deleted:", gone)


if __name__ == "__main__":
    main()
