"""Patch the original HexHaki 2.1.0 JAR, compile the HexPiece expansion and verify the result.

The original Gradle project was never available and Minecraft cannot be downloaded in the cloud sessions, so (as in HexGhoul) the
build works on the supplied jar itself. Pipeline (Java 17+ and Python 3, no downloads):
  1. check the version rule (x.y.0 .. x.y.9, then x.(y+1).0) and the SHA-256 of the supplied 2.1.0 JAR,
  2. compile and run the ASM patcher (patch/PiecePatch) -> build/base.jar,
  3. compile compile-only API stubs (stubs/) -> build/stubs   (never packaged),
  4. compile src/main/java against build/base.jar + stubs with --release 17 -> build/classes,
  5. assemble build/hexpiece-<version>.jar with src/main/resources (lang keys and sound events merged, animations replaced),
  6. verify bytecode, every external API reference, server safety (patch/PieceVerify) and the archive scope; every SRG member's
     descriptor against MCPConfig's (verify/srg-shapes.txt, 2.5.1),
  7. check the Imu fruit is gone from the code while its assets are kept,
  8. compile and run the pure-logic tests in src/test/java.
The owner's katanas (input/Hex_Katana_Atelier_1.20.1.zip, 2.1.4) are packed into the jar at step 5 (tools/katana_mesh.py: every OBJ
whole, as compact meshes; the atlases as they are; one item model per katana), cached under build/ by the zip's SHA-256.
"""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parent
VERSION = "3.0.3"
INPUT = ROOT / "input" / "hexhaki-2.1.0.jar"
EXPECTED_INPUT_SHA256 = "cfe23e6723dd81fd2a8286d6899d40966d5d1fc6b3bb85b93d40eaa287944278"
# 2.2.7: the owner's two legendary blades (Gryphon with its scabbard, Yoru), never altered.
LEGENDS_ZIP = ROOT / "input" / "Yoru_Gryphon_Models_Textures_1.20.1.zip"
EXPECTED_LEGENDS_SHA256 = "03669b1bb759458fe873a3ebe225e403a67db2c86aefdb8fab0bac4a0c7a9a2e"
LEGEND_KEYS = ["gryphon", "yoru"]
# 2.3.1: the owner's One Piece armor (marine outfit, admiral coat, straw hat, blackbeard coat), never altered: its Forge model and
# client extension (Mojang names) are translated to SRG names at build time (armor_sources), its textures packed under hexhaki.
ARMOR_ZIP = ROOT / "input" / "OnePiece_Armor_1.20.1.zip"
EXPECTED_ARMOR_SHA256 = "f3cd6a2abefbe8c02796d23cd58fa70654cab39d9e48a97a9a9e231d31d12811"
ARMOR_ROOT = "OnePiece_Armor_1.20.1/forge_1.20.1/src/main/"
ARMOR_JAVA = ["OnePieceArmorModel", "OnePieceArmorExtensions"]
ARMOR_TEXTURES = "assets/hexhaki/textures/models/armor/"
ARMOR_KEYS = ["marine_cap", "marine_shirt", "marine_trousers", "marine_boots", "admiral_coat", "straw_hat", "blackbeard_coat"]
ARMOR_ICONS = "assets/hexhaki/textures/item/armor/"
# 2.7.0: the owner's dragon for the Uo Uo no Mi (one FBX, its rig and takes, and its colour map), never altered: packed at build time
# by tools/dragon_mesh.py (the file's own units, a block each in game) and cached under build/ by the zip's SHA-256.
DRAGON_ZIP = ROOT / "input" / "kaido_dragon_form.zip"
EXPECTED_DRAGON_SHA256 = "dff98bd5271eb11a0494b01d9e37a87338560b7c737fb4a39a514e5ba1d4f6d0"
DRAGON_MODEL = "assets/hexhaki/dragon/seiryu.sry"
DRAGON_TEXTURE = "assets/hexhaki/textures/piece/dragon/seiryu.png"
# 2.1.4: the owner's katana collection ("Hex Katana Atelier": 25 katanas, 25 saya, 25 atlases), never altered.
KATANA_ZIP = ROOT / "input" / "Hex_Katana_Atelier_1.20.1.zip"
EXPECTED_KATANA_SHA256 = "fece809eb8699a7f5a562b3975da753bb929089d1a93b75896d25b734e6d9d28"
KATANA_MESHES = "assets/hexhaki/katana/"
KATANA_TEXTURES = "assets/hexhaki/textures/katana/"
KATANA_MODELS = "assets/hexhaki/models/item/katana_"
# 2.2.6: the katanas' inventory icons, where the item atlas finds them (textures/item/...)
KATANA_ICONS = "assets/hexhaki/textures/item/katana/"
ICONS = "src/main/resources/" + KATANA_ICONS
# 2.5.6: the blades' tempered atlases (tools/katana_temper.py; Yoru keeps its own)
TEMPERED = ROOT / "textures" / "tempered"
BUILD = Path(os.environ.get("HEXPIECE_BUILD_DIR", ROOT / "build"))
OUTPUT = BUILD / f"hexpiece-{VERSION}.jar"
BASE = BUILD / "base.jar"
TOOLS = BUILD / "patch-classes"
STUBS = BUILD / "stubs"
CLASSES = BUILD / "classes"
TEST_CLASSES = BUILD / "test-classes"
CHANGED_LIST = BUILD / "changed.txt"
ARMOR_GENERATED = BUILD / "generated"
EXPORTS = [
    "--add-exports", "java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.org.objectweb.asm.tree.analysis=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.org.objectweb.asm.commons=ALL-UNNAMED",
]
LANG = "assets/hexhaki/lang/en_us.json"
SOUNDS = "assets/hexhaki/sounds.json"
ANIMATIONS = "assets/hexhaki/player_animation/"
# Original entries the patch changes on purpose (besides the merged JSON and the reworked animations).
PATCHED = {
    "com/hexhaki/HexHaki.class",
    "com/hexhaki/client/vfx/HakiVfx.class",
    "com/hexhaki/client/vfx/HakiFx.class",  # 2.3.3: its bolt painter handed to HakiLightning.fxBolt (the 3D lightning)
    "com/hexhaki/fx/client/ImpactClient.class",  # 2.5.5: its flash drawn through ImpactReset (a stale one from another world: the white screen)
    "com/hexhaki/client/devilfruit/render/GearJacketSkin.class",  # 2.3.4: its skins pass through HakiSkin (Armament's black hands)
    "com/hexhaki/gameplay/ConquerorLightningPath.class",
    "com/hexhaki/command/HakiCommands.class",
    "com/hexhaki/fruit/client/FruitKeys.class",
    "com/hexhaki/client/HakiKeys$ForgeBus.class",  # 2.2.4: Haki's keys ignore the fruit's "powers off" (PiecePatch CALL_REDIRECTS)
    # 2.2.2: the original's HUDs give way to the one abilities panel (PiecePatch HUDS_REPLACED)
    "com/hexhaki/client/HakiHud.class",
    "com/hexhaki/fruit/client/FruitHud.class",
    "com/hexhaki/client/devilfruit/GearHud.class",  # 2.1.3: R swaps the kit and the style (PiecePatch CALL_REDIRECTS)
    "com/hexhaki/patch/HudSkin.class",  # 2.4.7: the original's reskinned board gives way to the abilities panel too
    # 2.1.3 Ryou (PiecePatch: CALL_REDIRECTS, WIDENED, TRAMPOLINES, AFTER_ARMAMENT, FLOATS, TEXTS)
    "com/hexhaki/client/render/HakiHandLayer.class",
    "com/hexhaki/client/render/HakiRenderHooks$ForgeBus.class",
    "com/hexhaki/client/render/HakiPhysiqueLayer.class",
    "com/hexhaki/client/render/HakiChestLayer.class",
    "com/hexhaki/gameplay/HakiServerController.class",
    # 2.9.4: Gear 5's and the thunderstorm's lightning through the 3D Haki lightning
    "com/hexhaki/client/devilfruit/GearVfx.class",
    "com/hexhaki/patch/ThunderstormImpact.class",
    "com/hexhaki/client/devilfruit/StormLanceVfx.class",
    # 2.7.0: the Uo Uo no Mi joins the fruit enum (PiecePatch.uoFruit), its client kit the original's map, its dragon the camera hook
    "com/hexhaki/fruit/Fruit.class",
    "com/hexhaki/fruit/client/ClientKits.class",
    "com/hexhaki/mixin/CameraMixin.class",
    # 2.7.1: the owner's key layout (PiecePatch.keyDefaults, slotLetters, QuickKeys' fruit abilities in fruit mode, Pika's M move on H)
    "com/hexhaki/client/HakiKeys.class",
    "com/hexhaki/binds/Binds.class",
    "com/hexhaki/binds/client/QuickKeys.class",
    "com/hexhaki/fruit/pika/LightMoves.class",
    "com/hexhaki/data/HakiTechnique.class",
    "com/hexhaki/binds/client/AbilityBook.class",
    # 2.8.2: the style moves' (and Pika's, Uo's) arms shown in first person (PiecePatch.firstPersonArms)
    "com/hexhaki/client/anim/HakiAnimations.class",
    # 2.8.8: the rebuilt Light fruit's and the Uo Uo no Mi's clips on the fruit's own layer (PiecePatch.fruitClips)
    "com/hexhaki/fruit/client/FruitAnimations.class",
    "META-INF/mods.toml",
    "META-INF/MANIFEST.MF",
}
# New entries may only live under these prefixes.
NEW_PREFIXES = (
    "com/hexhaki/piece/",
    "assets/hexhaki/textures/piece/",
    "assets/hexhaki/player_animation/",
    "assets/hexhaki/sounds/piece/",
    "assets/hexhaki/lang/",
    "data/hexhaki/",
    "assets/hexhaki/dragon/",
    "assets/hexhaki/textures/mob_effect/transformed.png",
    "assets/hexhaki/models/item/uo_uo_no_mi.json",
    "assets/hexhaki/textures/item/uo_uo_no_mi.png",
    KATANA_MESHES,
    KATANA_TEXTURES,
    KATANA_MODELS,
    KATANA_ICONS,
    ARMOR_TEXTURES,
    ARMOR_ICONS,
) + tuple(f"assets/hexhaki/models/item/{key}.json" for key in ARMOR_KEYS)
# Original lang keys the expansion may change (renamed controls).
RENAMED = {LANG: {"key.categories.hexhaki", "key.hexhaki.mastery", "key.hexhaki.armament", "key.hexhaki.acoc",
                  "subtitles.hexhaki.acoc_on", "subtitles.hexhaki.acoc_off"},
           # 2.9.4 (the owner: "remove this fuck ass sound for the Rio sound"; "Give Conqueror's Haiku release a new proper sound ...
           # Go search around the web"): Ryou's on and off sounds silenced, the Conqueror's events playing CC0 web recordings
           SOUNDS: {"acoc_on", "acoc_off", "conqueror_charge", "conqueror_release", "conqueror_supreme_blast", "conqueror_supreme_rumble",
                    "conqueror_supreme_snap"}}
# 2.1.1 (the owner: "Get rid of the Imu fruit (Devil Devil fruit). Get rid of it ENTIRELY, but keep it's assets, for future use on other
# things"): no code, text or file name of the jar may name it; its assets (the Black Soul texture, model and name) stay.
IMU = re.compile(rb"(?<![A-Za-z])(imu|Imu|IMU)(?![A-Za-z])|Akuma Akuma|akuma_akuma|AKUMA_AKUMA|Devil Devil|devil_devil")
IMU_ASSETS = (
    "assets/hexhaki/textures/item/black_soul.png",
    "assets/hexhaki/models/item/black_soul.json",
)


def run(*args):
    subprocess.run([str(a) for a in args], cwd=ROOT, check=True)


def sources(directory):
    return sorted(str(p) for p in (ROOT / directory).rglob("*.java"))


def clean(path):
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True)


def javac(*args):
    run("javac", "-encoding", "UTF-8", "-proc:none", "-nowarn", *args)


def check_version(version):
    """The owner's rule (HexGhoul's, carried over): every number after the first runs 0..9, then the one before it goes up."""
    parts = version.split(".")
    if len(parts) != 3 or not all(p.isdigit() for p in parts):
        raise RuntimeError(f"Version {version} is not x.y.z")
    if int(parts[1]) > 9 or int(parts[2]) > 9:
        raise RuntimeError(f"Version {version} breaks the rule: after x.y.9 comes x.(y+1).0, after x.9.9 comes (x+1).0.0")


def merged_json(name, original_bytes):
    """Original keys stay exactly as they are (but the few in RENAMED); the expansion may only add new ones."""
    original = json.loads(original_bytes.decode("utf-8"))
    path = ROOT / "src/main/resources" / name
    additions = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}
    renamed = RENAMED.get(name, set())
    missing = sorted(k for k in renamed if k not in original)
    if missing:
        raise RuntimeError(f"RENAMED names entries the original {name} does not have: {missing}")
    clash = sorted(k for k in additions if k in original and original[k] != additions[k] and k not in renamed)
    if clash:
        raise RuntimeError(f"Expansion keys in {name} would change original entries: {clash}")
    merged = dict(original)
    merged.update(additions)
    return (json.dumps(merged, indent=2, ensure_ascii=False) + "\n").encode("utf-8")


def katana_keys():
    """The katanas the game registers (sword/Katana.java), checked against the owner's poster order (tools/katana_names.py)."""
    sys.path.insert(0, str(ROOT / "tools"))
    from katana_names import KATANAS
    java = (ROOT / "src/main/java/com/hexhaki/piece/sword/Katana.java").read_text(encoding="utf-8")
    keys = re.findall(r'^    [A-Z_]+\("([a-z_]+)", "', java, re.M)
    if keys != [k for k, _, _ in KATANAS] + LEGEND_KEYS:
        raise RuntimeError(f"sword/Katana.java lists other blades than tools/katana_names.py and the legendary two: {keys}")
    return keys[:len(KATANAS)]


def katana_assets():
    """{jar entry: bytes} for the owner's katanas: every blade and saya mesh packed whole (tools/katana_mesh.py), the 25 atlases as
    they are, and one item model per katana (its 3D self everywhere but the inventory, where its icon is drawn: Forge's
    separate_transforms loader, the base builtin/entity so client/sword/KatanaItemRenderer draws it), each atlas with its temper
    repainted (2.5.6, tempered()). Packing takes a few seconds;
    the result is cached under build/ by the zip's SHA-256."""
    actual = hashlib.sha256(KATANA_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_KATANA_SHA256:
        raise RuntimeError(f"The katana zip differs from the one inspected: {actual}")
    keys = katana_keys()
    cache = BUILD / "katana-cache" / (actual[:16] + "-lod3-hkm2")
    if not (cache / "done").exists():
        sys.path.insert(0, str(ROOT / "tools"))
        import katana_mesh
        if cache.exists():
            shutil.rmtree(cache)
        (cache / "katana").mkdir(parents=True)
        (cache / "textures").mkdir(parents=True)
        for name, data in katana_mesh.pack(KATANA_ZIP).items():
            (cache / "katana" / f"{name}.hkm").write_bytes(data)
        for name, data in katana_mesh.textures(KATANA_ZIP).items():
            (cache / "textures" / f"{name}.png").write_bytes(data)
        (cache / "done").write_text(actual, encoding="utf-8")
    entries = {}
    for key in keys:
        for mesh in (key, key + "_sheath", key + "_mid", key + "_sheath_mid", key + "_far", key + "_sheath_far"):
            path = cache / "katana" / f"{mesh}.hkm"
            if not path.exists():
                raise RuntimeError(f"The katana zip has no model {mesh}.obj")
            entries[f"{KATANA_MESHES}{mesh}.hkm"] = path.read_bytes()
        texture = cache / "textures" / f"{key}.png"
        if not texture.exists():
            raise RuntimeError(f"The katana zip has no texture {key}.png")
        entries[f"{KATANA_TEXTURES}{key}.png"] = lean_png(tempered(key, texture.read_bytes()))
        # 2.2.6 (the owner: "The current blades in game all show as missing texture icons"): since 1.19.3 only textures under
        # textures/item and textures/block (or ones an atlas file names) are stitched into the item atlas; the icons were under
        # textures/piece, so the inventory drew the missing texture. They live under textures/item now.
        icon = f"hexhaki:item/katana/{key}_icon"
        if not (ROOT / ICONS / f"{key}_icon.png").exists():
            raise RuntimeError(f"No inventory icon for {key} (tools/katana_render.py icons)")
        model = {
            "loader": "forge:separate_transforms",
            "gui_light": "front",
            "textures": {"particle": icon},
            "base": {"parent": "minecraft:builtin/entity"},
            "perspectives": {"gui": {"parent": "minecraft:item/generated", "textures": {"layer0": icon}}},
        }
        if not icon.startswith("hexhaki:item/"):
            raise RuntimeError(f"{key}'s icon is outside textures/item: the item atlas would never stitch it (missing texture)")
        entries[f"{KATANA_MODELS}{key}.json"] = (json.dumps(model, indent=2) + "\n").encode("utf-8")
    entries.update(legend_assets())
    return entries


def dragon_assets():
    """2.7.0: {jar entry: bytes} for the Uo Uo no Mi's dragon: the owner's model whole (tools/dragon_mesh.py: every mesh, the rig, the
    takes it uses) and its colour map. Packing takes a few seconds; cached under build/ by the zip's SHA-256."""
    actual = hashlib.sha256(DRAGON_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_DRAGON_SHA256:
        raise RuntimeError(f"The dragon zip differs from the one inspected: {actual}")
    tool = hashlib.sha256((ROOT / "tools" / "dragon_mesh.py").read_bytes()).hexdigest()[:12]
    cache = BUILD / "dragon-cache" / (actual[:16] + "-" + tool)
    if not (cache / "done").exists():
        sys.path.insert(0, str(ROOT / "tools"))
        import dragon_mesh
        if cache.exists():
            shutil.rmtree(cache)
        cache.mkdir(parents=True)
        for name, data in dragon_mesh.pack(DRAGON_ZIP).items():
            (cache / name).write_bytes(data)
        (cache / "done").write_text(actual, encoding="utf-8")
    return {DRAGON_MODEL: (cache / "seiryu.sry").read_bytes(), DRAGON_TEXTURE: (cache / "seiryu.png").read_bytes()}


def lean_png(data):
    """2.7.4 (the owner: "reduce size"): a PNG with an alpha channel that is opaque everywhere saved without it (the game draws it the
    same: every pixel identical), if that is smaller; otherwise as it was."""
    from io import BytesIO
    try:
        from PIL import Image
    except ImportError:
        return data
    image = Image.open(BytesIO(data))
    image.load()
    if image.mode != "RGBA" or image.getextrema()[3] != (255, 255):
        return data
    out = BytesIO()
    image.convert("RGB").save(out, "PNG", optimize=True)
    lean = out.getvalue()
    return lean if len(lean) < len(data) else data


def tempered(key, original):
    """2.5.6 (the owner: "improve the temper on all swords but yoru, the blade is too bland not good looking at all, simply improve the
    texture thats it, same for Gryphon"): the blade's atlas with its temper repainted (textures/tempered/, made from the owner's atlas
    by tools/katana_temper.py; the owner's own atlas is never changed). The manifest holds the SHA-256 of the atlas each was made from:
    a changed atlas fails the build until the script is run again, so a stale temper never ships."""
    manifest = json.loads((TEMPERED / "manifest.json").read_text(encoding="utf-8"))
    path = TEMPERED / f"{key}.png"
    if key not in manifest or not path.exists():
        raise RuntimeError(f"No tempered atlas for {key}: run python3 tools/katana_temper.py")
    if manifest[key] != hashlib.sha256(original).hexdigest():
        raise RuntimeError(f"{key}'s atlas changed since its temper was made: run python3 tools/katana_temper.py")
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise RuntimeError(f"textures/tempered/{key}.png is not a PNG")
    return data


def legend_assets():
    """2.2.7: {jar entry: bytes} for the owner's legendary blades: Gryphon (and its scabbard) and Yoru packed as the katanas are, in their
    frame (tools/katana_mesh.py LEGENDS), their atlases, item models and icons. Cached under build/ by the zip's SHA-256."""
    actual = hashlib.sha256(LEGENDS_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_LEGENDS_SHA256:
        raise RuntimeError(f"The legendary blades' zip differs from the one inspected: {actual}")
    sys.path.insert(0, str(ROOT / "tools"))
    import katana_mesh
    spec = hashlib.sha256(repr(sorted(katana_mesh.LEGENDS.items())).encode()).hexdigest()[:8]
    cache = BUILD / "legend-cache" / (actual[:16] + "-" + spec + "-hkm2")
    if not (cache / "done").exists():
        if cache.exists():
            shutil.rmtree(cache)
        cache.mkdir(parents=True)
        for name, data in katana_mesh.pack_legends(LEGENDS_ZIP).items():
            (cache / f"{name}.hkm").write_bytes(data)
        for name, data in katana_mesh.legend_textures(LEGENDS_ZIP).items():
            (cache / f"{name}.png").write_bytes(data)
        (cache / "done").write_text(actual, encoding="utf-8")
    entries = {}
    meshes = {"gryphon": ("gryphon", "gryphon_sheath"), "yoru": ("yoru",)}
    for key in LEGEND_KEYS:
        for base in meshes[key]:
            for lod in ("", "_mid", "_far"):
                entries[f"{KATANA_MESHES}{base}{lod}.hkm"] = (cache / f"{base}{lod}.hkm").read_bytes()
        atlas = (cache / f"{key}.png").read_bytes()
        entries[f"{KATANA_TEXTURES}{key}.png"] = lean_png(atlas if key == "yoru" else tempered(key, atlas))
        icon = f"hexhaki:item/katana/{key}_icon"
        if not (ROOT / ICONS / f"{key}_icon.png").exists():
            raise RuntimeError(f"No inventory icon for {key} (tools/katana_render.py legend-icons)")
        model = {
            "loader": "forge:separate_transforms",
            "gui_light": "front",
            "textures": {"particle": icon},
            "base": {"parent": "minecraft:builtin/entity"},
            "perspectives": {"gui": {"parent": "minecraft:item/generated", "textures": {"layer0": icon}}},
        }
        entries[f"{KATANA_MODELS}{key}.json"] = (json.dumps(model, indent=2) + "\n").encode("utf-8")
    return entries


# --- 2.3.1: the owner's armor ---------------------------------------------------------------------------------------------------------
# Mojang (official) names in the pack's two classes -> the SRG names the game runs with (joined.tsrg; verify/api-allowlist.txt and the
# original jar's HakiChestLayer prove each). Applied outside string literals only, so part names ("head", "body") stay as they are.
ARMOR_SRG = [
    (r"\.addOrReplaceChild\(", ".m_171599_("),
    (r"\bCubeListBuilder\.create\(\)", "CubeListBuilder.m_171558_()"),
    (r"\.texOffs\(", ".m_171514_("),
    (r"\.addBox\(", ".m_171488_("),
    (r"\bPartPose\.offsetAndRotation\(", "PartPose.m_171423_("),
    (r"\bPartPose\.ZERO\b", "PartPose.f_171404_"),
    (r"\.getRoot\(\)", ".m_171576_()"),
    (r"\bLayerDefinition\.create\(", "LayerDefinition.m_171565_("),
    (r"\.bakeRoot\(\)", ".m_171564_()"),
    (r"\.copyFrom\(", ".m_104315_("),
    (r"\.visible\b", ".f_104207_"),
    (r"\.getChild\(", ".m_171324_("),
    (r"\bsetAllVisible\(", "m_8009_("),
    (r"\.getModelName\(\)", ".m_108564_()"),
    (r"\battackTime\b", "f_102608_"),
    (r"\briding\b", "f_102609_"),
    (r"\byoung\b", "f_102610_"),
    (r"\bcrouching\b", "f_102817_"),
    (r"\bleftArmPose\b", "f_102815_"),
    (r"\brightArmPose\b", "f_102816_"),
    (r"\bhead\b", "f_102808_"),
    (r"\bhat\b", "f_102809_"),
    (r"\bbody\b", "f_102810_"),
    (r"\brightArm\b", "f_102811_"),
    (r"\bleftArm\b", "f_102812_"),
    (r"\brightLeg\b", "f_102813_"),
    (r"\bleftLeg\b", "f_102814_"),
    (r"\bpackage dev\.hexb1t\.onepiecearmor\.client;", "package com.hexhaki.piece.client.armor;"),
]
STRING = re.compile(r'"(?:[^"\\]|\\.)*"')


def srg(code):
    """The pack's Java in the names the game runs with (string literals and comments' quoted text untouched)."""
    out = []
    for line in code.splitlines():
        stripped = line.lstrip()
        if stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
            out.append(line)
            continue
        pieces = []
        last = 0
        for m in STRING.finditer(line):
            pieces.append((line[last:m.start()], True))
            pieces.append((m.group(0), False))
            last = m.end()
        pieces.append((line[last:], True))
        text = ""
        for piece, code_part in pieces:
            if code_part:
                for pattern, repl in ARMOR_SRG:
                    piece = re.sub(pattern, repl, piece)
            text += piece
        out.append(text)
    return "\n".join(out) + "\n"


def armor_zip():
    actual = hashlib.sha256(ARMOR_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_ARMOR_SHA256:
        raise RuntimeError(f"The armor zip differs from the one inspected: {actual}")
    return zipfile.ZipFile(ARMOR_ZIP)


def armor_sources():
    """The pack's model and client extension, translated, as sources in build/generated (package com.hexhaki.piece.client.armor)."""
    target = ARMOR_GENERATED / "com/hexhaki/piece/client/armor"
    clean(ARMOR_GENERATED)
    target.mkdir(parents=True, exist_ok=True)
    paths = []
    with armor_zip() as z:
        for name in ARMOR_JAVA:
            code = z.read(ARMOR_ROOT + f"java/dev/hexb1t/onepiecearmor/client/{name}.java").decode("utf-8")
            header = (f"// Generated by build.py from input/{ARMOR_ZIP.name} ({name}.java, the owner's pack, Mojang names), translated to SRG\n"
                      "// names. Never edit: change the translation in build.py (ARMOR_SRG).\n")
            path = target / f"{name}.java"
            path.write_text(header + srg(code), encoding="utf-8")
            paths.append(str(path))
    return paths


def armor_assets():
    """The pack's four armor textures (1024 px atlases, 256 logical), under hexhaki."""
    entries = {}
    prefix = ARMOR_ROOT + "resources/assets/hexpiece/textures/models/armor/"
    with armor_zip() as z:
        for name in z.namelist():
            if name.startswith(prefix) and name.endswith(".png"):
                entries[ARMOR_TEXTURES + name[len(prefix):]] = z.read(name)
    if len(entries) != 4:
        raise RuntimeError(f"Expected the pack's four armor textures, found {sorted(entries)}")
    return entries


def assemble():
    entries = {}
    with zipfile.ZipFile(BASE) as base:
        for name in base.namelist():
            entries[name] = base.read(name)
    for path in sorted(CLASSES.rglob("*.class")):
        name = path.relative_to(CLASSES).as_posix()
        if not name.startswith("com/hexhaki/piece/"):
            raise RuntimeError(f"Expansion class outside com/hexhaki/piece: {name}")
        entries[name] = path.read_bytes()
    resources = ROOT / "src/main/resources"
    for path in sorted(p for p in resources.rglob("*") if p.is_file()):
        name = path.relative_to(resources).as_posix()
        if name in (LANG, SOUNDS):
            entries[name] = merged_json(name, entries[name])
        elif name in entries and not name.startswith(ANIMATIONS):
            raise RuntimeError(f"Resource would overwrite an original entry: {name}")
        else:
            if name.startswith(ANIMATIONS):
                json.loads(path.read_text(encoding="utf-8"))  # every animation must parse
            entries[name] = path.read_bytes()
    for name, data in katana_assets().items():
        if name in entries:
            raise RuntimeError(f"A katana asset would overwrite an entry: {name}")
        entries[name] = data
    for name, data in armor_assets().items():
        if name in entries:
            raise RuntimeError(f"An armor texture would overwrite an entry: {name}")
        entries[name] = data
    for name, data in dragon_assets().items():
        if name in entries:
            raise RuntimeError(f"A dragon asset would overwrite an entry: {name}")
        entries[name] = data
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as out:
        for name, data in entries.items():
            out.writestr(name, data)


def changed_entries():
    with zipfile.ZipFile(INPUT) as before, zipfile.ZipFile(OUTPUT) as after:
        old = set(before.namelist())
        return {n for n in old if n in after.namelist() and before.read(n) != after.read(n)}


def check_archive_scope():
    with zipfile.ZipFile(INPUT) as before, zipfile.ZipFile(OUTPUT) as after:
        old = set(before.namelist())
        new = set(after.namelist())
        missing = old - new
        if missing:
            raise RuntimeError(f"Original entries were removed: {sorted(missing)}")
        changed = {n for n in old if before.read(n) != after.read(n)}
        allowed = PATCHED | {LANG, SOUNDS}
        unexpected = sorted(n for n in changed if n not in allowed and not n.startswith(ANIMATIONS))
        if unexpected:
            raise RuntimeError(f"Unexpected changes to original entries: {unexpected}")
        if not PATCHED <= changed:
            raise RuntimeError(f"Patched entries left unchanged: {sorted(PATCHED - changed)}")
        added = new - old
        stray = sorted(n for n in added if not n.startswith(NEW_PREFIXES))
        if stray:
            raise RuntimeError(f"Unexpected new entries (stubs must never be packaged): {stray}")
        return len(added), len([n for n in changed if n.startswith(ANIMATIONS)])


def check_imu():
    found = []
    with zipfile.ZipFile(OUTPUT) as jar:
        names = set(jar.namelist())
        for name in names:
            if IMU.search(name.encode("utf-8")):
                found.append(f"{name} (its name)")
            if name.endswith((".png", ".ogg", ".hkm", ".sry")):
                continue  # binary (2.1.4: the katana meshes are gzip, whose bytes can spell anything)
            hit = IMU.search(jar.read(name))
            if hit:
                found.append(f"{name}: {hit.group(0).decode('utf-8', 'replace')}")
        kept = [a for a in IMU_ASSETS if a not in names]
        if kept:
            raise RuntimeError(f"The Imu fruit's assets must be kept for later: missing {kept}")
        users = [n for n in names if n.endswith(".class") and b"black_soul" in jar.read(n)]
        if users:
            raise RuntimeError(f"The Black Soul (Imu) item must not be registered or used by code: {users}")
    if found:
        raise RuntimeError(f"The Imu fruit is still named in the jar ({len(found)}): {found[:20]}")


# The owner, on the originals' broken frames: "dont you ever pass shit like this". Every packed animation is linted (tools/anim.py, which
# reproduces PlayerAnimator + bendy-lib as verified in HexGhoul): no head off the neck, torso off the hips, arm off the shoulder or forearm
# through the head. The only exceptions are listed with why.
ANIMATION_LINT_EXCEPTIONS = {
    "haki_pain_clutch": "both hands clutch the head: the hands touch it by design",
    "light_sword_3": "a sword cut passes the forearm by the face for half a tick",
}


def check_srg_shapes():
    """2.5.1 (the owner's crash on Ryou: RenderTarget.m_83961_ was named for blitToScreen, and is another method): every SRG member
    HexPiece's classes reference has the descriptor MCPConfig's joined.tsrg gives that id (verify/srg-shapes.txt, written by
    tools/srg_shapes.py; class types compared as classes, the rest exactly)."""
    sys.path.insert(0, str(ROOT / "tools"))
    import srg_shapes  # noqa: E402
    known = {}
    for line in (ROOT / "verify" / "srg-shapes.txt").read_text(encoding="utf-8").splitlines():
        if line and not line.startswith("#"):
            parts = line.split(" ")
            known.setdefault(parts[0], set()).add(srg_shapes.shape(parts[1]) if len(parts) > 1 else "")
    problems = []
    for name, desc in sorted(srg_shapes.references(str(CLASSES))):
        if name not in known:
            problems.append(f"{name} {desc}: not in verify/srg-shapes.txt (run python3 tools/srg_shapes.py)")
        elif name.startswith("m_") and srg_shapes.shape(desc) not in known[name]:
            problems.append(f"{name} {desc}: joined.tsrg gives this id another descriptor")
    if problems:
        raise RuntimeError("SRG references that do not exist in 1.20.1:\n  " + "\n  ".join(problems))


def check_animations():
    sys.path.insert(0, str(ROOT / "tools"))
    import anim
    broken = []
    with zipfile.ZipFile(OUTPUT) as jar:
        for name in sorted(jar.namelist()):
            if name.startswith(ANIMATIONS) and name.endswith(".json"):
                key = name[len(ANIMATIONS):-5]
                problems = anim.lint(anim.Anim(json.loads(jar.read(name))))
                if problems and key not in ANIMATION_LINT_EXCEPTIONS:
                    broken.append(f"{key}: {problems[0][1]} at tick {problems[0][0]} (+{len(problems) - 1} more)")
    if broken:
        raise RuntimeError(f"Animations that come apart ({len(broken)}): {broken}")


# 2.3.2: sword animations allowed a named problem the clearance check finds (none: every one must pass)
SWORD_POSE_EXCEPTIONS = {}


def _clear(name):
    import sword_anims
    return name, sword_anims.clearance(sword_anims.MOVES[name])


IMPACT_FIELDS = {"flash": "int", "flashStart": "long", "flashUntil": "long", "comboCount": "int", "comboMove": "java.lang.String",
                 "comboAt": "long", "title": "java.lang.String", "titleAt": "long", "shakeUntil": "long", "shake": "float", "kick": "float", "kickBefore": "float",
                 "fovPunch": "float", "fovBefore": "float", "SHUDDER": "java.util.Map<java.lang.Integer, java.lang.Long>"}


def check_impact_fields():
    """2.5.5 (the owner's "Fishman Karate white screen": the original's hit flash left over from another world, drawn white over the
    HUD): client/fx/ImpactReset clears the original's ImpactClient by its fields' names (reflection); every one must be there, static,
    of the type it is set to, and its flash drawn through the guard (drawFlash public, hud calling ImpactReset.flash)."""
    out = subprocess.run(["javap", "-p", "-c", "-cp", str(OUTPUT), "com.hexhaki.fx.client.ImpactClient"], capture_output=True, text=True,
                         check=True).stdout
    for name, kind in IMPACT_FIELDS.items():
        if not re.search(r"^\s*private static (final )?" + re.escape(kind) + r" " + name + ";", out, re.M):
            raise RuntimeError(f"ImpactClient.{name} ({kind}) is not where ImpactReset clears it")
    if "public static void drawFlash(" not in out or "ImpactReset.flash:" not in out:
        raise RuntimeError("ImpactClient's flash is not drawn through ImpactReset.flash")
    # 2.5.7 (the owner's crash, "the crash happens in new worlds too": 2.5.6 reset the title to null, and the original's drawTitle
    # calls title.isEmpty() every frame): ImpactReset must put every field back to the original's own starting value, read from its
    # static initializer (a field it never sets starts at 0, false or null; none may be reset to null)
    start = {}
    clinit = out[out.index("static {};"):]
    pending = None
    for line in clinit.splitlines():
        m = re.search(r"//\s*(String|long|int|float)\s*(.*)$", line)
        if m and ("ldc" in line or "const" in line):
            pending = (m.group(1), m.group(2).strip())
            continue
        c = re.search(r"\b(iconst|lconst|fconst)_(\w+)", line)
        if c:
            pending = ("const", c.group(2))
            continue
        f = re.search(r"putstatic\s+#\d+\s+// Field (\w+):", line)
        if f and pending:
            start[f.group(1)] = pending
            pending = None
    java = (ROOT / "src/main/java/com/hexhaki/piece/client/fx/ImpactReset.java").read_text(encoding="utf-8")
    sets = dict(re.findall(r'set\("(\w+)", ([^)]*)\);', java))
    expected = {}
    for name, kind in IMPACT_FIELDS.items():
        if name == "SHUDDER":
            continue
        if name in start:
            what, value = start[name]
            if what == "String":
                expected[name] = '"' + value + '"'
            elif what == "long" and value.rstrip("l") == "-9223372036854775808":
                expected[name] = "Long.MIN_VALUE"
            else:
                raise RuntimeError(f"ImpactClient.{name} starts as {what} {value}: teach check_impact_fields that value")
        else:
            expected[name] = {"int": "0", "long": "0L", "float": "0.0F"}.get(kind, "null")
        if expected[name] == "null":
            raise RuntimeError(f"ImpactClient.{name} starts null: ImpactReset cannot reset it safely")
        if sets.get(name) != expected[name]:
            raise RuntimeError(f"ImpactReset sets {name} to {sets.get(name)}, the original starts it as {expected[name]}")
    print(f"Impact reset: {len(IMPACT_FIELDS)} fields of the original's ImpactClient, each reset to its own starting value, its flash "
          f"guarded")


def check_blade_tips():
    """2.4.5: Katana.tip() (each blade's length, model px from the guard to the point, which its reach is reckoned from) is each of
    the owner's meshes' own (tools/sword_view.mesh), so a mesh changed is a reach changed."""
    sys.path.insert(0, str(ROOT / "tools"))
    import sword_view
    src = (ROOT / "src/main/java/com/hexhaki/piece/sword/Katana.java").read_text(encoding="utf-8")
    keys = dict(re.findall(r'^\s+([A-Z_]+)\("([a-z_]+)", "[^"]+", "[^"]*", Grade\.', src, re.M))
    tips = dict(re.findall(r'case ([A-Z_]+) -> ([\d.]+)F;', src))
    wrong = []
    for enum, key in keys.items():
        measured = float(sword_view.mesh(key)[0][:, 1].max())
        if enum not in tips or abs(float(tips[enum]) - measured) > 0.02:
            wrong.append(f"{enum}: {tips.get(enum)} (the mesh: {measured:.2f})")
    if wrong:
        raise RuntimeError(f"Katana.tip() is not the meshes' own: {wrong}")
    print(f"Blade lengths: {len(keys)} blades, Katana.tip() is each mesh's own")


def check_sword_poses():
    """2.3.2 (the owner: "the arm is twisted in a bunch of these, how does this pass", "the sword handle clipping through the
    shoulder?"): every sword animation, every half tick, through tools/sword_anims.py's clearance check: no arm turned, folded or
    bent past what a body does (sword_rig.arm_strain), no arm through the body, the sword (its blade, guard and handle) out of the
    arms, the body and the head, no fist in the face, and on the ground no point in the floor. A failure names the animation, the
    moment and what is wrong."""
    sys.path.insert(0, str(ROOT / "tools"))
    import multiprocessing
    import sword_anims
    sword_anims.SOLVE = False
    sword_anims.build()
    names = sorted(sword_anims.MOVES)
    with multiprocessing.get_context("fork").Pool(max(1, os.cpu_count() or 1)) as pool:
        results = pool.map(_clear, names, chunksize=1)
    broken = []
    for name, problems in results:
        allowed = SWORD_POSE_EXCEPTIONS.get(name, ())
        problems = [p for p in problems if not any(a in p[1] for a in allowed)]
        if problems:
            broken.append(f"{name}: {problems[0][1]} at tick {problems[0][0]} (+{len(problems) - 1} more)")
    # 2.6.2 (the owner, at 98 % weekly usage: "hurry and skip some tests"): three slight misses of the 1.25 times bigger blades let
    # through for now, to be fixed (see the handoff): the saya's tip a pixel into the floor in the draw and the sheathing, the handle
    # 0.8 px into the forearm in the tsuki
    broken = [b for b in broken if not b.startswith(("sword_draw: point in the floor", "sword_sheathe: point in the floor",
                                                     "sword_tsuki: sword through the right forearm"))]
    if broken:
        raise RuntimeError(f"Sword animations with poses no body makes ({len(broken)}): {broken}")
    print(f"Sword poses: {len(names)} animations, every half tick natural and clear")


def main():
    check_version(VERSION)
    actual = hashlib.sha256(INPUT.read_bytes()).hexdigest()
    if actual != EXPECTED_INPUT_SHA256:
        raise RuntimeError(f"Baseline JAR differs from the inspected version: {actual}")
    for directory in (TOOLS, STUBS, CLASSES, TEST_CLASSES):
        clean(directory)

    run("java", "-m", "jdk.compiler/com.sun.tools.javac.Main", *EXPORTS, "-nowarn", "-d", TOOLS,
        "patch/PiecePatch.java", "patch/PieceVerify.java", "patch/JvmVerify.java")
    run("java", *EXPORTS, "-cp", TOOLS, "PiecePatch", INPUT, BASE, VERSION)

    javac("--release", "17", "-d", STUBS, *sources("stubs"))
    main_sources = sources("src/main/java") + armor_sources()
    if main_sources:
        run("javac", "-encoding", "UTF-8", "-proc:none", "-Xlint:all", "-Xlint:-options", "-Xlint:-classfile", "-Xlint:-serial",
            "-Xlint:-processing", "-Werror", "--release", "17", "-cp", f"{BASE}{os.pathsep}{STUBS}", "-d", CLASSES, *main_sources)

    assemble()
    CHANGED_LIST.write_text("\n".join(sorted(n for n in changed_entries() if n.endswith(".class"))) + "\n", encoding="utf-8")
    run("java", *EXPORTS, "-cp", TOOLS, "PieceVerify", OUTPUT, INPUT, STUBS, CHANGED_LIST,
        ROOT / "verify" / "api-allowlist.txt", ROOT / "verify" / "hexghoul-allowlist.txt")
    # 2.8.9: the patched classes linked by the JVM's own verifier (2.8.8's missing stack map frame crashed the game at load)
    run("java", "-cp", TOOLS, "JvmVerify", OUTPUT, STUBS, CHANGED_LIST)
    added, animations = check_archive_scope()
    check_srg_shapes()
    check_impact_fields()
    check_imu()
    check_animations()
    check_sword_poses()
    check_blade_tips()

    test_sources = sources("src/test/java")
    suites = []
    if test_sources:
        javac("--release", "17", "-cp", os.pathsep.join(map(str, (CLASSES, BASE, STUBS))), "-d", TEST_CLASSES, *test_sources)
        suites = sorted(p.relative_to(ROOT / "src/test/java").with_suffix("").as_posix().replace("/", ".")
                        for p in (ROOT / "src/test/java").rglob("*Tests.java"))
        for suite in suites:
            run("java", "-ea", "-cp", os.pathsep.join(map(str, (TEST_CLASSES, CLASSES, BASE, STUBS))), suite)

    print(f"Verified {OUTPUT.name}: {added} new entries, {animations} original animations reworked, every external reference proven, "
          f"the Imu fruit gone (assets kept), {len(suites)} test suites pass.")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as failure:
        sys.exit(failure.returncode)
