"""Patch the original HexHaki 2.1.0 JAR, compile the HexPiece expansion and verify the result.

The original Gradle project was never available and Minecraft cannot be downloaded in the cloud sessions, so (as in HexGhoul) the
build works on the supplied jar itself. Pipeline (Java 17+ and Python 3, no downloads):
  1. check the version rule (x.y.0 .. x.y.9, then x.(y+1).0) and the SHA-256 of the supplied 2.1.0 JAR,
  2. compile and run the ASM patcher (patch/PiecePatch) -> build/base.jar,
  3. compile compile-only API stubs (stubs/) -> build/stubs   (never packaged),
  4. compile src/main/java against build/base.jar + stubs with --release 17 -> build/classes,
  5. assemble build/hexpiece-<version>.jar with src/main/resources (lang keys and sound events merged, animations replaced),
  6. verify bytecode, every external API reference, server safety (patch/PieceVerify) and the archive scope,
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
VERSION = "2.3.0"
INPUT = ROOT / "input" / "hexhaki-2.1.0.jar"
EXPECTED_INPUT_SHA256 = "cfe23e6723dd81fd2a8286d6899d40966d5d1fc6b3bb85b93d40eaa287944278"
# 2.2.7: the owner's two legendary blades (Gryphon with its scabbard, Yoru), never altered.
LEGENDS_ZIP = ROOT / "input" / "Yoru_Gryphon_Models_Textures_1.20.1.zip"
EXPECTED_LEGENDS_SHA256 = "03669b1bb759458fe873a3ebe225e403a67db2c86aefdb8fab0bac4a0c7a9a2e"
LEGEND_KEYS = ["gryphon", "yoru"]
# 2.1.4: the owner's katana collection ("Hex Katana Atelier": 25 katanas, 25 saya, 25 atlases), never altered.
KATANA_ZIP = ROOT / "input" / "Hex_Katana_Atelier_1.20.1.zip"
EXPECTED_KATANA_SHA256 = "fece809eb8699a7f5a562b3975da753bb929089d1a93b75896d25b734e6d9d28"
KATANA_MESHES = "assets/hexhaki/katana/"
KATANA_TEXTURES = "assets/hexhaki/textures/katana/"
KATANA_MODELS = "assets/hexhaki/models/item/katana_"
# 2.2.6: the katanas' inventory icons, where the item atlas finds them (textures/item/...)
KATANA_ICONS = "assets/hexhaki/textures/item/katana/"
ICONS = "src/main/resources/" + KATANA_ICONS
BUILD = Path(os.environ.get("HEXPIECE_BUILD_DIR", ROOT / "build"))
OUTPUT = BUILD / f"hexpiece-{VERSION}.jar"
BASE = BUILD / "base.jar"
TOOLS = BUILD / "patch-classes"
STUBS = BUILD / "stubs"
CLASSES = BUILD / "classes"
TEST_CLASSES = BUILD / "test-classes"
CHANGED_LIST = BUILD / "changed.txt"
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
    "com/hexhaki/gameplay/ConquerorLightningPath.class",
    "com/hexhaki/command/HakiCommands.class",
    "com/hexhaki/fruit/client/FruitKeys.class",
    "com/hexhaki/client/HakiKeys$ForgeBus.class",  # 2.2.4: Haki's keys ignore the fruit's "powers off" (PiecePatch CALL_REDIRECTS)
    # 2.2.2: the original's HUDs give way to the one abilities panel (PiecePatch HUDS_REPLACED)
    "com/hexhaki/client/HakiHud.class",
    "com/hexhaki/fruit/client/FruitHud.class",
    "com/hexhaki/client/devilfruit/GearHud.class",  # 2.1.3: R swaps the kit and the style (PiecePatch CALL_REDIRECTS)
    # 2.1.3 Ryou (PiecePatch: CALL_REDIRECTS, WIDENED, TRAMPOLINES, AFTER_ARMAMENT, FLOATS, TEXTS)
    "com/hexhaki/client/render/HakiHandLayer.class",
    "com/hexhaki/client/render/HakiRenderHooks$ForgeBus.class",
    "com/hexhaki/client/render/HakiPhysiqueLayer.class",
    "com/hexhaki/client/render/HakiChestLayer.class",
    "com/hexhaki/gameplay/HakiServerController.class",
    "com/hexhaki/data/HakiTechnique.class",
    "com/hexhaki/binds/client/AbilityBook.class",
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
    KATANA_MESHES,
    KATANA_TEXTURES,
    KATANA_MODELS,
    KATANA_ICONS,
)
# Original lang keys the expansion may change (renamed controls).
RENAMED = {LANG: {"key.categories.hexhaki", "key.hexhaki.mastery", "key.hexhaki.armament", "key.hexhaki.acoc",
                  "subtitles.hexhaki.acoc_on", "subtitles.hexhaki.acoc_off"}, SOUNDS: set()}
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
    separate_transforms loader, the base builtin/entity so client/sword/KatanaItemRenderer draws it). Packing takes a few seconds;
    the result is cached under build/ by the zip's SHA-256."""
    actual = hashlib.sha256(KATANA_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_KATANA_SHA256:
        raise RuntimeError(f"The katana zip differs from the one inspected: {actual}")
    keys = katana_keys()
    cache = BUILD / "katana-cache" / (actual[:16] + "-lod3")
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
        entries[f"{KATANA_TEXTURES}{key}.png"] = texture.read_bytes()
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


def legend_assets():
    """2.2.7: {jar entry: bytes} for the owner's legendary blades: Gryphon (and its scabbard) and Yoru packed as the katanas are, in their
    frame (tools/katana_mesh.py LEGENDS), their atlases, item models and icons. Cached under build/ by the zip's SHA-256."""
    actual = hashlib.sha256(LEGENDS_ZIP.read_bytes()).hexdigest()
    if actual != EXPECTED_LEGENDS_SHA256:
        raise RuntimeError(f"The legendary blades' zip differs from the one inspected: {actual}")
    sys.path.insert(0, str(ROOT / "tools"))
    import katana_mesh
    spec = hashlib.sha256(repr(sorted(katana_mesh.LEGENDS.items())).encode()).hexdigest()[:8]
    cache = BUILD / "legend-cache" / (actual[:16] + "-" + spec)
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
        entries[f"{KATANA_TEXTURES}{key}.png"] = (cache / f"{key}.png").read_bytes()
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
            if name.endswith((".png", ".ogg", ".hkm")):
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


def main():
    check_version(VERSION)
    actual = hashlib.sha256(INPUT.read_bytes()).hexdigest()
    if actual != EXPECTED_INPUT_SHA256:
        raise RuntimeError(f"Baseline JAR differs from the inspected version: {actual}")
    for directory in (TOOLS, STUBS, CLASSES, TEST_CLASSES):
        clean(directory)

    run("java", "-m", "jdk.compiler/com.sun.tools.javac.Main", *EXPORTS, "-nowarn", "-d", TOOLS,
        "patch/PiecePatch.java", "patch/PieceVerify.java")
    run("java", *EXPORTS, "-cp", TOOLS, "PiecePatch", INPUT, BASE, VERSION)

    javac("--release", "17", "-d", STUBS, *sources("stubs"))
    main_sources = sources("src/main/java")
    if main_sources:
        run("javac", "-encoding", "UTF-8", "-proc:none", "-Xlint:all", "-Xlint:-options", "-Xlint:-classfile", "-Xlint:-serial",
            "-Xlint:-processing", "-Werror", "--release", "17", "-cp", f"{BASE}{os.pathsep}{STUBS}", "-d", CLASSES, *main_sources)

    assemble()
    CHANGED_LIST.write_text("\n".join(sorted(n for n in changed_entries() if n.endswith(".class"))) + "\n", encoding="utf-8")
    run("java", *EXPORTS, "-cp", TOOLS, "PieceVerify", OUTPUT, INPUT, STUBS, CHANGED_LIST,
        ROOT / "verify" / "api-allowlist.txt", ROOT / "verify" / "hexghoul-allowlist.txt")
    added, animations = check_archive_scope()
    check_imu()
    check_animations()

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
