import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.FrameNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.TypeInsnNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

/**
 * HexPiece: the narrow, reproducible bytecode patch of the original HexHaki 2.1.0 jar (no source was ever supplied).
 *
 * <ul>
 *   <li>{@code HexHaki.<init>} hands the mod bus to {@code com.hexhaki.piece.HexPiece.init} (registries, network, NPCs).</li>
 *   <li>Haki lightning: {@code ConquerorLightningPath.generate} (shared by server hit checks and client visuals, a pure function of
 *       its seed) and the two segment painters in {@code HakiVfx} are trampolines into {@code com.hexhaki.piece}.</li>
 *   <li>{@code HakiVfx}'s beam helpers are widened to public so the new painters draw with the original's own Photon setup.</li>
 *   <li>{@code META-INF/mods.toml} and the manifest carry the HexPiece name and version.</li>
 * </ul>
 * Nothing in Minecraft is modified. Usage: PiecePatch input.jar output.jar version
 */
public final class PiecePatch implements Opcodes {
    static final String MOD = "com/hexhaki/HexHaki";
    static final String VFX = "com/hexhaki/client/vfx/HakiVfx";
    static final String PATH = "com/hexhaki/gameplay/ConquerorLightningPath";
    static final String COMMANDS = "com/hexhaki/command/HakiCommands";
    /**
     * 2.1.1 (the owner: "Get rid of the Imu fruit (Devil Devil fruit) Get rid of it ENTIRELY, but keep it's assets"): the 2.1.0 jar has no
     * Imu code left but two names of it in its debug tables: the "remove fruit" command's flag for a kit fruit (it is
     * {@code fruit != NONE}) and an empty Imu-only branch flag on the world effects packet (variant bit 8, nothing reads it). Both are
     * renamed; the Black Soul texture, model and name stay in the jar for later use. build.py checks no "imu" is left anywhere.
     */
    static final String[][] IMU_LOCALS = {{COMMANDS, "removeFruit", "kitFruit"}, {VFX, "acceptWorld", "unusedVariant"}};
    static final String IMU = new String(new char[] {'i', 'm', 'u'});
    static final String BOOT = "com/hexhaki/piece/HexPiece";
    static final String LIGHTNING = "com/hexhaki/piece/client/haki/HakiLightning";
    static final String PATHS = "com/hexhaki/piece/haki/LightningPaths";
    /** 2.1.5: the 3D lightning's anchor hooks (client). */
    static final String BOLTS = "com/hexhaki/piece/client/haki/HakiBolts";
    /** 2.1.7: the full-power storm (client). */
    static final String STORM = "com/hexhaki/piece/client/haki/HakiStorm";
    /**
     * 2.2.2 (the owner: "all the abilities, everything has to be in the same menu, like HUD on the left. Like, I don't care about mastery
     * in the bottom"): the original's three HUDs (Haki with its mastery bars, the fruit's, Gear 5's) first ask {@code AbilityHud.replaces()}
     * and draw nothing when it answers true: HexGhoul's one panel on the left shows it all.
     */
    static final String HUD = "com/hexhaki/piece/client/AbilityHud";
    static final String[][] HUDS_REPLACED = {
        {"com/hexhaki/client/HakiHud", "draw", "(Lnet/minecraftforge/client/event/RenderGuiOverlayEvent$Post;)V"},
        {"com/hexhaki/fruit/client/FruitHud", "render", "(Lnet/minecraftforge/client/event/RenderGuiEvent$Post;)V"},
        {"com/hexhaki/client/devilfruit/GearHud", "draw", "(Lnet/minecraftforge/client/event/RenderGuiOverlayEvent$Post;)V"},
        // 2.4.7 (the owner, with a screenshot of the original's energy and mastery panel in the bottom left: "Why the fuck is the mastery
        // hud menu back?"): the original's fourth HUD, its reskinned board (com.hexhaki.patch.HudSkin), drawn whenever its "board"
        // setting is on; it was never asked, so it came back the moment that setting was switched on
        {"com/hexhaki/patch/HudSkin", "render", "(Lnet/minecraftforge/client/event/RenderGuiEvent$Post;)V"}};
    static final String[][] PLAY_HOOKS = {
        {"play", "(Lnet/minecraft/world/entity/Entity;Lcom/lowdragmc/photon/client/fx/FX;)V", "played",
            "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/entity/Entity;)Z"},
        {"playAtWorld", "(Lnet/minecraft/world/entity/Entity;DDDLcom/lowdragmc/photon/client/fx/FX;)V", "playedAt",
            "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/entity/Entity;DDD)Z"},
        {"playAtFixed", "(Lnet/minecraft/client/multiplayer/ClientLevel;DDDLcom/lowdragmc/photon/client/fx/FX;)V", "playedFixed",
            "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/client/multiplayer/ClientLevel;DDD)Z"}};
    static final String BUS = "net/minecraftforge/eventbus/api/IEventBus";
    static final String CONTEXT = "net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext";
    static final String SEGMENT = "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;II)V";
    static final String GENERATE = "(FIJZ)Ljava/util/List;";
    /** HakiVfx helpers the new painters reuse (access widened only, bodies untouched). */
    static final String[] VFX_WIDENED = {"rageBeam", "beam", "add", "fx", "play", "playAtWorld", "playAtFixed", "particleTex",
        "addConquerorRangeLightning", "aftershock"};
    /** 2.3.4: the original's skin swap (Gear 5's white skin) and HexPiece's Armament hands on it. */
    static final String JACKET = "com/hexhaki/client/devilfruit/render/GearJacketSkin";
    static final String HAKI_SKIN = "com/hexhaki/piece/client/haki/HakiSkin";
    /** 2.5.5: the original's hit effects (flash, combo, title, shake), and HexPiece's guard over its flash. */
    static final String IMPACT = "com/hexhaki/fx/client/ImpactClient";
    static final String IMPACT_RESET = "com/hexhaki/piece/client/fx/ImpactReset";
    static final String SKIN_DESC = "(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/resources/ResourceLocation;)"
        + "Lnet/minecraft/resources/ResourceLocation;";
    /** 2.3.3: the original's other bolt painter, handed to HakiLightning.fxBolt. */
    static final String HAKI_FX = "com/hexhaki/client/vfx/HakiFx";
    static final String FX_BOLT = "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FIIIII)V";
    /** 2.3.3: the Conqueror's particles in the player's Ryou colour (client/haki/HakiTint). */
    static final String TINT = "com/hexhaki/piece/client/haki/HakiTint";
    static final String PARTICLE_TEX = "(IIFFIZLnet/minecraft/resources/ResourceLocation;)"
        + "Lcom/lowdragmc/photon/client/gameobject/emitter/particle/ParticleEmitter;";
    /** HakiVfx's Conqueror and last-stand painters whose own particleTex calls are tinted, and how many each has. */
    static final Map<String, Integer> TINTED_TEX = Map.of("addConquerorRangeLightning", 1, "addLastStandBodyBolts", 1,
        "addLastStandStormParticles", 2, "lastStandAura", 1, "lastStandPulse", 1);
    /** HakiVfx painters handed to {@link #LIGHTNING}: name -> method there. */
    static final String[][] VFX_TRAMPOLINES = {{"addHakiLightningSegment", "segment"}, {"addConquerorStormSegment", "storm"}};
    /**
     * HakiVfx's lightning methods and how many "beam (or rageBeam) then add" pairs each has: every pair becomes one call to
     * {@code HakiLightning.bolt} (or {@code rage}) with the same stack (fx, from, to, width, colour, duration, delay, bloom).
     */
    static final Map<String, Integer> LIGHTNING_PAIRS = Map.of(
        "addAdvancedGalaxyFistLightning", 1, "addConquerorRangeLightning", 7, "addLastStandBodyBolts", 7,
        "addLastStandOverheadArches", 11, "lightningFamilies", 5, "lightningFamiliesAt", 4, "lightningFamiliesDelayed", 2,
        "skyBreak", 4, "skyRift", 4, "horizonCracks", 2);
    /**
     * 2.1.3: calls in the original redirected into {@code com.hexhaki.piece} (same descriptor, static): class, method, the call's owner,
     * name and descriptor, the new owner and name, how many such calls the method must have.
     * <ul>
     *   <li>R switches between the fighting style and the fruit (the owner: "tapping R switches from Combat style and fruit (if any is
     *       eaten)"): a kit fruit's M1 and right click ({@code FruitKeys.tick}, {@code FruitKeys.interact}) ask
     *       {@code PieceHooks.fruitKeysActive} (the kit is active and combat mode is off) instead of {@code FruitClient.active}.</li>
     * </ul>
     */
    static final String HOOKS = "com/hexhaki/piece/client/PieceHooks";
    /** 2.7.0: the original's fruit enum, the new fruit (id, name, element, colour, lore) and the client kits' map. */
    static final String FRUIT = "com/hexhaki/fruit/Fruit";
    /** 2.7.1: the original's keys and quick slots, and the new default key codes (GLFW: J 74, H 72, X 88, O 79, V 86; -1 unbound). */
    static final String HAKI_KEYS = "com/hexhaki/client/HakiKeys";
    static final String BINDS = "com/hexhaki/binds/Binds";
    static final Map<String, Integer> KEY_CODES = Map.of("key.hexhaki.observation", 74, "key.hexhaki.acoc", -1,
        "key.hexhaki.convergence", 72, "key.hexhaki.mode", 88, "key.hexhaki.loadout", 79, "key.hexhaki.slot8", 86);
    static final String CLIENT_KITS = "com/hexhaki/fruit/client/ClientKits";
    static final String[] UO = {"uo", "Uo Uo no Mi", "Dragon", "4FB7E8",
        "A Mythical Zoan that makes its eater an Azure Dragon: Seiryu, a serpent of the sky as long as an island, who flies on clouds of"
            + " its own making. Kaido's fruit."};
    /** 3.1.9: the Hie Hie no Mi (id, name, element, colour, lore): a Logia, Admiral Aokiji's. */
    static final String[] HIE = {"hie", "Hie Hie no Mi", "Ice", "9FE6FF",
        "A Logia that makes its eater an Ice Human: to become ice and freeze anything it touches solid, to freeze the very sea into a field"
            + " of ice. Admiral Aokiji's fruit."};
    /** 3.3.4: the Noro Noro no Mi (the owner: "New common Fruits ... Noro Noro No Mi"): a Paramecia, Foxy's. */
    static final String[] NORO = {"noro", "Noro Noro no Mi", "Slow", "C77DE0",
        "A Paramecia that lets its eater shoot Noroma photons: whatever they touch moves in extreme slow motion for thirty seconds, and"
            + " every blow it takes meanwhile lands all at once when the time is up. Foxy the Silver Fox's fruit."};
    /** 3.3.6: the Horo Horo no Mi (the owner: "Horo Horo No Mi (common)"): a Paramecia, Perona's. */
    static final String[] HORO = {"horo", "Horo Horo no Mi", "Ghost", "F3E3A6",
        "A Paramecia that lets its eater make ghosts: hollows that drain the will to fight from whoever they pass through, little ones that"
            + " burst, a giant one that explodes, and lets the eater's own spirit leave the body. Perona's fruit."};
    /** 3.3.7: the Ito Ito no Mi (the owner: "New Rare Fruit: Ito Ito No Mi (Rare) The Strings Fruit"): a Paramecia, Doflamingo's. */
    static final String[] ITO = {"ito", "Ito Ito no Mi", "String", "F27BB8",
        "A Paramecia that lets its eater make strings sharp enough to cut steel: to slice and whip, to work others like puppets, to walk"
            + " the sky, to weave a copy of themself and a cage that closes in on everyone inside. Donquixote Doflamingo's fruit."};
    /** 3.5.5: the Mochi Mochi no Mi (the owner: "research katakuris mochi fruit and add it ... Legendary fruit"): a Special Paramecia. */
    static final String[] MOCHI = {"mochi", "Mochi Mochi no Mi", "Mochi", "F2E6CF",
        "A Special Paramecia that turns its eater's body into mochi and lets them make it: to stretch and swell, to trap whoever it sticks"
            + " to, to shape fists, drills, wheels and rings, to let blows pass through holes in it; awakened, the ground itself becomes"
            + " mochi. Charlotte Katakuri's fruit."};
    /** 3.3.5: the Supa Supa no Mi (the owner: "Supa Supa No Mi (UnCommon)"): a Paramecia, Daz Bones' (Mr. 1). */
    static final String[] SUPA = {"supa", "Supa Supa no Mi", "Blade", "8FB3DA",
        "A Paramecia that turns any part of its eater's body into steel blades: arms, fingers, feet, the whole body, hard enough that no"
            + " plain blade can cut it. Daz Bones' fruit."};
    static final String HAND = "com/hexhaki/client/render/HakiHandLayer";
    static final String RYOU_HANDS = "com/hexhaki/piece/client/haki/RyouHands";
    static final String RYOU_FX = "com/hexhaki/piece/client/haki/RyouFx";
    static final String RYOU_SERVER = "com/hexhaki/piece/server/RyouServer";
    static final String CONTROLLER = "com/hexhaki/gameplay/HakiServerController";
    static final String CONQUEROR = "com/hexhaki/piece/server/ConquerorRelease";
    /** 3.5.9: the Conqueror's quick abilities (the Haki Grip and the Galaxy Fist gone, WiFi Haki a Swordsman's): server/HakiQuick. */
    static final String QUICK = "com/hexhaki/piece/server/HakiQuick";
    static final String PLAYER_VOID = "(Lnet/minecraft/server/level/ServerPlayer;)V";
    static final String COATING = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/geom/ModelPart;FZLnet/minecraft/client/player/AbstractClientPlayer;Z)V";
    static final String AURA = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FZFZZ)V";
    static final String AMBIENT = "(Lnet/minecraft/world/entity/Entity;FZJZ)V";
    static final String GROUND_STORM = "(Lnet/minecraft/world/entity/Entity;FZJ)V";
    static final String APPLY_CONQUEROR = "(Lnet/minecraft/server/level/ServerPlayer;DFLcom/hexhaki/data/ConquerorMode;ZZZ)V";
    static final String COMBAT_TARGET = "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;)Z";
    static final String ANIMATE = "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;I)V";
    static final String EMITTER = "Lcom/lowdragmc/photon/client/gameobject/emitter/particle/ParticleEmitter;";
    static final String EMITTER_5 = "(IIFFI)" + EMITTER;
    static final Object[][] CALL_REDIRECTS = {
        {"com/hexhaki/fruit/client/FruitKeys", "tick", "com/hexhaki/fruit/client/FruitClient", "active", "()Z", HOOKS, "fruitKeysActive", 1},
        {"com/hexhaki/fruit/client/FruitKeys", "interact", "com/hexhaki/fruit/client/FruitClient", "active", "()Z", HOOKS, "fruitKeysActive", 1},
        // 2.7.1 (the owner's new keys: "All keys of the mod (but haki) will now be the following Z, X, C, V, B, N, M. R For switching
        // modes"): the quick keys' fruit abilities only in fruit mode (combat mode has the style's specials on Z and X); Haki's in both
        {"com/hexhaki/binds/client/QuickKeys", "press", "com/hexhaki/fruit/client/FruitClient", "active", "()Z", HOOKS, "fruitKeysActive", 1},
        // 2.2.4 (the owner: "The keys should be usable just like that in any form"): the fruit's "powers off" switch no longer
        // silences Haki: its keys, the Haki leap and the server's Haki actions stop asking it
        {"com/hexhaki/client/HakiKeys$ForgeBus", "tick", "com/hexhaki/binds/client/BindsClient", "off", "()Z", HOOKS, "hakiKeysOff", 1},
        {"com/hexhaki/client/HakiKeys$ForgeBus", "movementInput", "com/hexhaki/binds/client/BindsClient", "off", "()Z", HOOKS, "hakiKeysOff", 1},
        {CONTROLLER, "onAction", "com/hexhaki/binds/Binds", "off", "(Lnet/minecraft/world/entity/player/Player;)Z", RYOU_SERVER, "hakiOff", 1},
        // 2.2.5 (the owner: "the hexpiece joyboy on/off, is not a thing anymore, anyone by default at max charge shall be able to do that.
        // (keep and override the default max conqueror with the joyboy one since it's better.)", and "At full charge, it should cause
        // severe tears on the floor like fissures where the lightnings hit, then repair themselves automaticly (same as HexGhoul)"): in
        // the Conqueror's release the JoyBoy switch reads true (and INTS drops its mastery 1000 to 0: every full-charge radial release is
        // the supreme one); the release's visual goes out through HexPiece with a seed it keeps, so the server knows the storm the clients
        // draw; the supreme release's permanent destruction is self-repairing fissures along that storm's ground strikes instead
        {CONTROLLER, "releaseConqueror", "com/hexhaki/data/HakiData", "joyBoy", "(Lnet/minecraft/world/entity/player/Player;)Z", CONQUEROR, "joyBoy", 1},
        {CONTROLLER, "releaseConqueror", CONTROLLER, "visual", "(Lnet/minecraft/server/level/ServerPlayer;Lcom/hexhaki/network/msg/S2CHakiVisual$Visual;FI)V",
            CONQUEROR, "visual", 2},
        {CONTROLLER, "releaseConqueror", "com/hexhaki/gameplay/SupremeHakiDestruction", "unleash", "(Lnet/minecraft/server/level/ServerPlayer;D)V",
            CONQUEROR, "fissures", 1},
        // 2.1.3 Ryou (the owner: "Ryou should look like blackened hands with a strong, clean glow outlining their shape. Keep the black
        // coating readable beneath the coloured rim. ... instead of covering the arms with red effects. Remove the fire particles from
        // every Ryou activation, idle and movement effect"): the coating stays plain black, the arm aura is left off while Ryou is up,
        // the ambient effects of a Ryou player are RyouFx's (no embers, the player's own colour).
        {HAND, "render", "com/hexhaki/client/devilfruit/render/GearJacketHaki", "renderCoating", COATING, RYOU_HANDS, "coating", 2},
        // 2.5.4: the first-person hand is drawn straight (playerAnimator leaves it alone): its coat and aura are not bent with the elbow
        {HAND, "renderFirstPerson", "com/hexhaki/client/devilfruit/render/GearJacketHaki", "renderCoating", COATING, RYOU_HANDS,
            "coatingFirstPerson", 1},
        {HAND, "renderFirstPerson", HAND, "renderAdvancedFistGlow",
            "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FZZ)V",
            RYOU_HANDS, "glowFirstPerson", 1},
        {HAND, "render", HAND, "renderArmAura", AURA, RYOU_HANDS, "armAura", 2},
        {HAND, "renderFirstPerson", HAND, "renderArmAura", AURA, RYOU_HANDS, "armAura", 1},
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "tick", VFX, "armamentAmbient", AMBIENT, RYOU_FX, "ambient", 1},
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "tick", VFX, "armamentGroundStorm", GROUND_STORM, RYOU_FX, "groundStorm", 1},
        // 3.5.9 (the owner: "Galaxy Fist  Removed entirely.", "Haki Grip  Removed entirely as well", "Wifi haki becomes a Sword's man
        // ability only"): the Galaxy Fist's start and end (its "convergence") and the Haki Grip's (its "dominion") do nothing; WiFi Haki's
        // ("sovereign strike") go through HexPiece, which lets only a Swordsman hold it; its lightning leaves the saya's mouth at the hip
        {CONTROLLER, "onAction", CONTROLLER, "beginConvergence", PLAYER_VOID, QUICK, "gone", 1},
        {CONTROLLER, "onAction", CONTROLLER, "releaseConvergence", PLAYER_VOID, QUICK, "gone", 1},
        {CONTROLLER, "onAction", CONTROLLER, "beginDominion", PLAYER_VOID, QUICK, "gone", 1},
        {CONTROLLER, "onAction", CONTROLLER, "releaseDominion", PLAYER_VOID, QUICK, "gone", 1},
        {CONTROLLER, "onAction", CONTROLLER, "sovereignStrike", PLAYER_VOID, QUICK, "wifi", 1},
        {CONTROLLER, "onAction", CONTROLLER, "releaseSovereignStrike", PLAYER_VOID, QUICK, "wifiRelease", 1},
        {CONTROLLER, "sovereignStrike", CONTROLLER, "wifiHakiHandPoint", "(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/world/phys/Vec3;",
            QUICK, "mouth", 1},
        {CONTROLLER, "sendWifiHakiArc", CONTROLLER, "wifiHakiHandPoint", "(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/world/phys/Vec3;",
            QUICK, "mouth", 1},
        // 2.4.5 (the owner: "Conqueror Haki full release lags a ton when releasing the full charge"): the release's range lightning goes
        // through HexPiece, which leaves out the supreme release's second pass (the same bolts again from a second seed, 7 ticks
        // later: half the release's 3D lightning, 80,000 quads a frame at its height)
        {VFX, "conquerorRelease", VFX, "addConquerorRangeLightning", "(Lcom/lowdragmc/photon/client/fx/FX;FIJZI)V", STORM, "range", 2},
        // 2.9.5 (the owner: "after you let go, the all lightning sounds all at once, it's ridiculously loud"): the charge's lightning
        // (its range-lightning call, every few ticks while G is held) goes through HexPiece too, which makes it land silently: its
        // long crackles piled up through the charge and past its end (the charge is heard as its own held sound now)
        {VFX, "conquerorCharge", VFX, "addConquerorRangeLightning", "(Lcom/lowdragmc/photon/client/fx/FX;FIJZI)V", STORM, "charge", 1},
        // 2.4.7 (the owner: "the crackle lightning sound at the end is to be removed along with all the old sounds of the letting go of G
        // button max charge, keep the explosion sound and the new one we just added"): the release's sounds go through HexPiece, which
        // drops the full release's snap and leaves out its rumble (the blast, the explosion, stays; a partial release keeps its own), and
        // the full release's aftershocks are made silent (their lightning crackled after the blast)
        {CONTROLLER, "releaseConqueror", CONTROLLER, "sound", "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/sounds/SoundEvent;FF)V",
            CONQUEROR, "sound", 3},
        {CONTROLLER, "releaseConqueror", CONTROLLER, "scheduleSupremeAudio", "(Lnet/minecraft/server/level/ServerPlayer;)V", CONQUEROR,
            "rumble", 1},
        {VFX, "accept0", VFX, "aftershock", "(FJ)Lcom/lowdragmc/photon/client/fx/FX;", STORM, "aftershock", 1},
        // 2.4.9 (the owner: "every mob hit with the lightning from the Conqueror's release is to be fucking felled ... the longer you hold
        // the G button, at max charge, I think the stun should go up to 10 seconds", then by alignment): the radial release's effects on
        // what is in its radius, its full charge's stun, and whom both take go through HexPiece (ConquerorRelease, haki/ConquerorStun)
        {CONTROLLER, "releaseConqueror", CONTROLLER, "applyConqueror", APPLY_CONQUEROR, CONQUEROR, "apply", 1},
        {CONTROLLER, "releaseConqueror", CONTROLLER, "forceMaxConquerorStun", "(Lnet/minecraft/server/level/ServerPlayer;DI)V", CONQUEROR,
            "forceMax", 1},
        {CONTROLLER, "lambda$applyConqueror$1", CONTROLLER, "isConquerorCombatTarget", COMBAT_TARGET, CONQUEROR, "target", 1},
        {CONTROLLER, "lambda$forceMaxConquerorStun$2", CONTROLLER, "isConquerorCombatTarget", COMBAT_TARGET, CONQUEROR, "target", 1},
        // 2.5.1 (the owner: "evil gets one animation (EVIL/Disturbing) and good gets another (Peaceful/in pain)"): the charge's and the
        // release's animations go through HexPiece, which plays the releaser's alignment's (tools/conqueror_anims.py)
        {CONTROLLER, "beginConqueror", CONTROLLER, "animate", ANIMATE, CONQUEROR, "animate", 1},
        {CONTROLLER, "releaseConqueror", CONTROLLER, "animate", ANIMATE, CONQUEROR, "animate", 2},
        // 2.5.2 (the owner: "the ring is still red (the ring that forms around the blast area)"): the tap's rings, flash and smoke
        // (HakiFx's own emitters, never tinted) in the Ryou colour
        {VFX, "conquerorTapCone", HAKI_FX, "airRing", EMITTER_5, TINT, "airRing", 1},
        {VFX, "conquerorTapCone", HAKI_FX, "flash", EMITTER_5, TINT, "flash", 1},
        {VFX, "conquerorTapCone", HAKI_FX, "smoke", "(IIFFIFLjava/util/Random;)" + EMITTER, TINT, "smoke", 1},
        // 2.5.5 (the owner's "Fishman Karate white screen": a flash from another world, its game-time end far ahead of the new world's
        // clock, drawn full white over the HUD every frame): the flash goes through HexPiece, which draws it only while time runs
        // forward from its start and clears a stale one (client/fx/ImpactReset)
        {IMPACT, "hud", IMPACT, "drawFlash", "(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", IMPACT_RESET, "flash", 1},
        // 2.7.0 (the Uo Uo no Mi's dragon, 14 blocks tall): the original's camera hook (CameraMixin, at the end of Camera.setup) takes
        // its fixed camera place from HexPiece, which hands back the original's own (King's Grip) or, in dragon form, a place far
        // enough behind and above to see the dragon (client/dragon/DragonCamera)
        {"com/hexhaki/mixin/CameraMixin", "hexhaki$cinematicOffset", "com/hexhaki/client/cinematic/CinematicController",
            "kingsGripCameraPosition", "(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/world/phys/Vec3;",
            "com/hexhaki/piece/client/dragon/DragonCamera", "position", 1},
        // 2.6.9 (the owner's crash: endless recursion of the first-person arm with Haki on): the original's arm handler draws through
        // HexPiece, which skips it while HexPiece itself draws the arm from inside it (RyouHands.DRAWING_ARM)
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "arm", HAND, "renderFirstPerson",
            "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/PlayerModel;Lnet/minecraft/client/player/AbstractClientPlayer;ZZFZF)V",
            RYOU_HANDS, "firstPerson", 1},
        // 2.6.0 (the owner: "Conqueror sounds reduced by 35% but the line evil voice line increased by a ton"): the Haki sounds the
        // original plays a second late (its delayedSound) go through HexPiece, which plays its Conqueror sounds 35 % quieter
        // (server/ConquerorSounds; every other sound as it was)
        {CONTROLLER, "lambda$delayedSound$26", "com/hexhaki/patch/SoundBalance", "pos",
            "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
            "com/hexhaki/piece/server/ConquerorSounds", "pos", 1},
        // 3.3.2 (the owner: "shift + space ability is removed"): Shift + Space's own check (GearMovement.movementInput asks whether Sky
        // Bounce may go) and the server's (mechanicsAction and skyBounce ask whether it is unlocked) never let it: it is a plain jump
        {"com/hexhaki/client/devilfruit/GearMovement", "movementInput", "com/hexhaki/client/devilfruit/GearClientState", "usable",
            "(Lcom/hexhaki/devilfruit/data/GearAbility;)Z", "com/hexhaki/piece/client/gear/NikaJumpClient", "usable", 1},
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "mechanicsAction", "com/hexhaki/devilfruit/data/GearData", "unlocked",
            "(Lnet/minecraft/world/entity/player/Player;Lcom/hexhaki/devilfruit/data/GearAbility;)Z", "com/hexhaki/piece/server/NikaRules",
            "unlocked", 1},
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "skyBounce", "com/hexhaki/devilfruit/data/GearData", "unlocked",
            "(Lnet/minecraft/world/entity/player/Player;Lcom/hexhaki/devilfruit/data/GearAbility;)Z", "com/hexhaki/piece/server/NikaRules",
            "unlocked", 1},
        // 3.4.4 (the owner: "Whip and Dawn Whip Rework"; "the Goof ball is only usable in transformed state"): the whip asks
        // server/NikaWhip whether it is unlocked: untransformed the original's answer (its Gum-Gum Whip as before); in Gear 5 Goof Ball or
        // the new Dawn Whip starts there and the answer is no, so the original's whip goes no further
        // 3.4.9 (the owner: the M1s "should have a cooldown, otherwise you can just spam it and cancel the animation ... You shouldn't be
        // able to punch until the arm is fully returned to your body"): the Pistol asks server/NikaRules.armOut, which holds it while the
        // last arm is still out in every mode (the original's own 20-tick lock, cd_pistol, was skipped by its no-cooldown mode)
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "pistol", "com/hexhaki/devilfruit/gameplay/GearServerController", "onCooldown",
            "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;)Z", "com/hexhaki/piece/server/NikaRules", "armOut", 1},
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "whip", "com/hexhaki/devilfruit/data/GearData", "unlocked",
            "(Lnet/minecraft/world/entity/player/Player;Lcom/hexhaki/devilfruit/data/GearAbility;)Z", "com/hexhaki/piece/server/NikaWhip",
            "unlocked", 1},
        // 3.4.7 (the owner: the Giant's throw "on a ... horizontal line, similar to the others ... our own custom effect whenever the
        // entity crashes against something"): GiantGrab.release's two GearData.integer calls ask server/GiantThrow, which never keeps
        // "grabThrown" (the original's arc and vanilla explosion never follow) and launches the body flat in a straight line instead
        {"com/hexhaki/patch/GiantGrab", "release", "com/hexhaki/devilfruit/data/GearData", "integer",
            "(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;I)V", "com/hexhaki/piece/server/GiantThrow", "integer", 2},
        // 3.3.2: Gear 5's walk switches its run and its stance as the body leaves and touches the ground: through server/NikaJump, which
        // holds them back while the jump's laughing skip plays
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "tickBouncyWalk", "com/hexhaki/gameplay/HakiServerController", "animate",
            ANIMATE, "com/hexhaki/piece/server/NikaJump", "walkClip", 2},
    };
    /**
     * 2.4.9: instance calls made static: class, method, call owner, name, descriptor, new owner, new name, how many. The new method takes
     * the receiver first, then the call's arguments. The Conqueror's release's damage ({@code LivingEntity.hurt}) asks HexPiece
     * ({@code ConquerorRelease.hurt}: a good player's hurts no one, an evil player's only what its lightning struck).
     */
    static final Object[][] VIRTUAL_REDIRECTS = {
        {CONTROLLER, "applyConqueror", "net/minecraft/world/entity/LivingEntity", "m_6469_", "(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            CONQUEROR, "hurt", 3},
        // 2.5.3 (the owner: "sometimes the player's skin vanishes when starting a world"): Gear 5's white skin reads the player's skin
        // back from its texture through HexPiece (HakiSkin.readBack), which throws when the readback is not a real skin (haki/SkinCheck),
        // so the original closes its copy and tries again later instead of drawing the player with an empty one
        {JACKET, "create", "com/mojang/blaze3d/platform/NativeImage", "m_85045_", "(IZ)V", HAKI_SKIN, "readBack", 1},
        // 2.6.9: the original's arm handler cancels the arm event through HexPiece (not while HexPiece draws the arm from inside it)
        {"com/hexhaki/client/render/HakiRenderHooks$ForgeBus", "arm", "net/minecraftforge/client/event/RenderArmEvent", "setCanceled", "(Z)V",
            RYOU_HANDS, "cancelArm", 1},
        // 3.0.9 (the owner: "while reflecting over water, the player sinks to the sea floor nearly instantly. while in yata no kagami, the
        // player NEVER suffers the effect of the sea"): the fruits' sea check (sinking, weakness, the fruit's moves cancelled) asks
        // HexPiece whether the player is in the water: never while their body is light
        {"com/hexhaki/fruit/FruitServer", "inSea", "net/minecraft/world/entity/player/Player", "m_20072_", "()Z",
            "com/hexhaki/piece/server/PikaMoves", "inWater", 1},
    };
    /** 2.1.3: methods widened to public (bodies untouched) so HexPiece can call the originals: class, names. */
    static final String[][] WIDENED = {{HAND, "renderArmAura", "renderFirstPerson"}, {COMMANDS, "reply"}, {IMPACT, "drawFlash"},
        {CONTROLLER, "sovereignStrike", "releaseSovereignStrike", "wifiHakiHandPoint", "applyConqueror", "forceMaxConquerorStun",
            "isConquerorCombatTarget"}};
    /**
     * 2.2.5: int constants: class, method, from, to, how many. The Conqueror's supreme (JoyBoy) release asked for mastery 1000: any
     * mastery now ({@link #CALL_REDIRECTS}: no JoyBoy switch either). 2.3.7: the Galaxy Fist's Armament 520.
     */
    static final Object[][] INTS = {
        {CONTROLLER, "releaseConqueror", 1000, 0, 1},
    };
    /**
     * 2.1.3: static methods whose whole body becomes a call into HexPiece (same descriptor): class, method, descriptor, new owner, name.
     * The fist glow becomes Ryou's coloured rim; the fist flames are gone; J (Advanced Haki, now Ryou) is left empty (the owner: "I think
     * its best whenever Ryou is unlcked it fully replaces regular haki, and we empty out the J key for now").
     */
    static final String[][] TRAMPOLINES = {
        {HAND, "renderAdvancedFistGlow", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FZZ)V", RYOU_HANDS, "glow"},
        {HAND, "renderAdvancedFistFlames", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;FFZZ)V", RYOU_HANDS, "flames"},
        {CONTROLLER, "toggleAcoc", "(Lnet/minecraft/server/level/ServerPlayer;)V", RYOU_SERVER, "acocKey"},
        // 2.2.5: "/hp joyboy on|off" is not a thing any more: it says so (every full-charge release is the JoyBoy one)
        {COMMANDS, "setJoyBoy", "(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/level/ServerPlayer;Z)I", CONQUEROR, "joyBoyCommand"},
        // 2.4.4 (the owner: "whenever haki stun lands on the entity or player, no matter if it's modded or not, I want that entity to
        // ragdoll and like fall ... instead of the current animation, that makes them like just like look up weird"): every stun's
        // knockdown (the release, the tap, the full charge, a charging user, the last stand) is a fall (HakiFalls), not the swimming pose
        // and the player knockdown animations
        {CONTROLLER, "knockDown", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;IZ)V",
            "com/hexhaki/piece/server/HakiFalls", "knockDown"},
    };
    /** 2.1.3: once Ryou is unlocked, switching Armament on brings it (RyouServer.afterArmament runs before every return). */
    static final String[] AFTER_ARMAMENT = {CONTROLLER, "toggleArmament", "(Lnet/minecraft/server/level/ServerPlayer;)V", RYOU_SERVER, "afterArmament"};
    /**
     * 2.1.3 (the owner: "Reduce the excessive muscle size produced by Advanced Haki Armament ... Keep the result close to the normal player
     * silhouette, with restrained muscular definition rather than inflated shoulders, oversized biceps or an enlarged torso"): the
     * physique's float constants, class, method, from, to, how many. Arms 1.4x wide at full bulk become 1.07x; the shoulders pushed out
     * about 3 px become under half a pixel; the chest copy 1.55x wide and 1.36x deep becomes 1.06x and 1.10x (a hint of pectorals).
     */
    static final Object[][] FLOATS = {
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "pre", 0.4F, 0.07F, 1},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "armSeparation", 2.2F, 0.24F, 1},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "armSeparation", 0.35F, 0.08F, 1},
        {"com/hexhaki/client/render/HakiChestLayer", "render", 0.55F, 0.06F, 1},
        {"com/hexhaki/client/render/HakiChestLayer", "render", 0.3575F, 0.10F, 1},
    };
    /**
     * 3.4.9 (the owner: the Bajrang Gun is "still jumping too high up, so I think we just reduce the blocks that the player jumps high,
     * like a little lower"): double constants, class, method, from, to, how many, and the named constant kept in step (or null). The
     * climb's top, 75 blocks over where it began, becomes 55 (the slam's reach is measured down to the ground, so it follows).
     */
    static final Object[][] DOUBLES = {
        {"com/hexhaki/devilfruit/gameplay/GearServerController", "beginBajrang", 75.0, 55.0, 1, "BAJRANG_ALTITUDE"},
    };
    /** The physique's named constants, kept in step with {@link #FLOATS} (class, field, value). */
    static final Object[][] FLOAT_FIELDS = {
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "MAX_ARM_GAIN", 0.07F},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "MAX_CHEST_GAIN", 0.06F},
        {"com/hexhaki/client/render/HakiPhysiqueLayer", "ARM_CLEARANCE", 0.08F},
    };
    /**
     * 2.1.3 (the owner: "Rename Advanced Haki Armament to Ryou everywhere visible to the player, including menus, tooltips, notifications
     * and ability descriptions"): text the player reads, class, from, to, how many (string constants and string-concatenation recipes;
     * identifiers such as acocOn, advancedHakiUnlocked and the network fields are untouched, so saves and packets stay as they were). The
     * two Advanced Haki messages of HakiServerController were in toggleAcoc, which is RyouServer.acocKey now (J is empty).
     */
    static final Object[][] TEXTS = {
        // 2.7.1: the fourth quick slot is H now (was M): the Pika fruit's move declared on it stays on it
        {"com/hexhaki/fruit/pika/LightMoves", "[M] hold", "[H] hold", 1},
        // 3.3.2: Sky Bounce is gone (the owner: "shift + space ability is removed"); Buoyancy still raises Gear 5's bounding steps
        {"com/hexhaki/devilfruit/data/GearUpgrade", "Extra Sky Bounces per airtime and a softer, floatier descent.",
            "Gear 5's bounding steps spring higher, with a softer, floatier descent.", 1},
        {"com/hexhaki/data/HakiTechnique", "Advanced Haki", "Ryou", 1},
        {"com/hexhaki/data/HakiTechnique", "[J] Late-game multiplier. Requires ARM 850 + HAO 850 and active Armament; upgrades nearly every major technique.",
            "Late-game multiplier. Requires ARM 850 + HAO 850. Once unlocked it replaces your Armament (Ctrl + R): blackened hands outlined in your own glow colour, and nearly every major technique upgraded.", 1},
        {"com/hexhaki/data/HakiTechnique", "Flowing Armament / Ryuo", "Flowing Armament", 1},
        {"com/hexhaki/data/HakiTechnique", "Ember Fists", "Ryou Fists", 1},
        {"com/hexhaki/data/HakiTechnique", "With Advanced Haki [J] up, both fists ignite: red-to-orange flame trails on the hands and sky-fed bolts landing between the outward arcs.",
            "With Ryou up, both fists blacken behind a bright outline in your own glow colour, and sky-fed bolts land between the outward arcs.", 1},
        {"com/hexhaki/data/HakiTechnique", "Late-game Armament control required for Advanced Haki [J].", "Late-game Armament control required for Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "Passive milestone: emitted Armament techniques reach a 16% total mastery-step bonus and prepare the path to Advanced Haki.",
            "Passive milestone: emitted Armament techniques reach a 16% total mastery-step bonus and prepare the path to Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "[M HOLD, RELEASE TO FIRE] Wind a Haki punch and let go. A tap throws a short close-range burst; a full hold throws a long, wide pressure cylinder that detonates where it lands. Length, width, damage, cost and cooldown all scale continuously with how long you held it, with Armament mastery, and with whether you are bare, coated, or under Advanced Haki.",
            "[H HOLD, RELEASE TO FIRE] Wind a Haki punch and let go. A tap throws a short close-range burst; a full hold throws a long, wide pressure cylinder that detonates where it lands. Length, width, damage, cost and cooldown all scale continuously with how long you held it, with Armament mastery, and with whether you are bare, coated, or under Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Advanced tiers raise the launch height, the damage and the size of the blast.",
            "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Ryou tiers raise the launch height, the damage and the size of the blast.", 1},
        {"com/hexhaki/data/HakiTechnique", "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Advanced Haki its landing scatters grounded Haki strikes around the impact.",
            "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Ryou its landing scatters grounded Haki strikes around the impact.", 1},
        // 3.5.9 (the owner: "Wifi haki becomes a Sword's man ability only (switch the key to a free quickslot not L)", and its blade eased
        // out of the saya with the lightning out of the gap)
        {CONTROLLER, "WiFi Haki [Hold L] requires ARM 450 + OBS 450 + HAO 500.", "WiFi Haki [Hold K] requires ARM 450 + OBS 450 + HAO 500.", 1},
        {"com/hexhaki/data/HakiTechnique", "[L HOLD] Lock one visible target with 1/2/3 separated arched Haki beams. Range, duration and damage grow with ARM + OBS + HAO mastery.",
            "[K HOLD] A Swordsman's alone. The blade eased a finger's width out of its saya, and out of the gap Haki lightning locks one visible target with 1/2/3 separated arched beams. Range, duration and damage grow with ARM + OBS + HAO mastery.", 1},
        {"com/hexhaki/binds/client/AbilityBook", "Permanent:  \u0001 Armament   \u0001 Observation   \u0001 Advanced Haki   \u0001 powers on/off   \u0001 this book\u0001",
            "Permanent:  \u0001 style / fruit (Ctrl: Armament, Ryou)   \u0001 Observation   \u0001 (empty)   \u0001 powers on/off   \u0001 this book\u0001", 1},
    };
    static final String BEAM = "(Lorg/joml/Vector3f;Lorg/joml/Vector3f;FIIIZ)Lcom/lowdragmc/photon/client/gameobject/emitter/beam/BeamEmitter;";
    static final String ADD = "(Lcom/lowdragmc/photon/client/fx/FX;Lcom/lowdragmc/photon/client/gameobject/IFXObject;)V";
    static final String BOLT = "(Lcom/lowdragmc/photon/client/fx/FX;Lorg/joml/Vector3f;Lorg/joml/Vector3f;FIIIZ)V";

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: PiecePatch input.jar output.jar version");
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String version = args[2];
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipFile jar = new ZipFile(input.toFile())) {
            for (ZipEntry entry : java.util.Collections.list(jar.entries())) {
                // Directory entries are kept too: the original jar has them, and the output keeps every original entry.
                entries.put(entry.getName(), entry.isDirectory() ? new byte[0] : jar.getInputStream(entry).readAllBytes());
            }
        }
        entries.put(MOD + ".class", transform(entries.get(MOD + ".class"), PiecePatch::bootstrap));
        entries.put(VFX + ".class", transform(entries.get(VFX + ".class"), PiecePatch::lightningPainters));
        // 2.3.4: every skin the original's GearJacketSkin.resolve hands back goes through HakiSkin.resolve (Armament blackens the
        // hands in the skin itself, as Gear 5 whitens it)
        entries.put(JACKET + ".class", transform(entries.get(JACKET + ".class"), PiecePatch::hakiSkinHook));
        // 2.3.3: HakiFx's bolt (the tap release's, King's Grip's, Thragg's lightning) is the 3D lightning too
        entries.put(HAKI_FX + ".class", transform(entries.get(HAKI_FX + ".class"),
            node -> trampoline(method(node, "bolt", FX_BOLT), LIGHTNING, "fxBolt")));
        entries.put(PATH + ".class", transform(entries.get(PATH + ".class"), PiecePatch::lightningPaths));
        for (String[] local : IMU_LOCALS) {
            entries.put(local[0] + ".class", transform(entries.get(local[0] + ".class"), node -> renameLocal(node, local[1], IMU, local[2])));
        }
        for (Object[] redirect : CALL_REDIRECTS) {
            String owner = (String) redirect[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> redirectCalls(node, redirect)));
        }
        for (Object[] redirect : VIRTUAL_REDIRECTS) {
            String owner = (String) redirect[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> redirectVirtual(node, redirect)));
        }
        for (String[] widen : WIDENED) {
            entries.put(widen[0] + ".class", transform(entries.get(widen[0] + ".class"), node -> {
                for (int i = 1; i < widen.length; i++) {
                    MethodNode method = method(node, widen[i], null);
                    method.access = (method.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
                }
            }));
        }
        for (String[] t : TRAMPOLINES) {
            entries.put(t[0] + ".class", transform(entries.get(t[0] + ".class"), node -> trampoline(method(node, t[1], t[2]), t[3], t[4])));
        }
        entries.put(AFTER_ARMAMENT[0] + ".class", transform(entries.get(AFTER_ARMAMENT[0] + ".class"), PiecePatch::afterArmament));
        for (Object[] f : FLOATS) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceFloat(node, f)));
        }
        for (Object[] f : INTS) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceInt(node, f)));
        }
        for (Object[] f : DOUBLES) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceDouble(node, f)));
        }
        for (Object[] f : FLOAT_FIELDS) {
            String owner = (String) f[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> {
                for (FieldNode field : node.fields) {
                    if (field.name.equals(f[1])) {
                        field.value = f[2];
                        return;
                    }
                }
                throw new AssertionError("missing field " + owner + "." + f[1]);
            }));
        }
        for (Object[] text : TEXTS) {
            String owner = (String) text[0];
            entries.put(owner + ".class", transform(entries.get(owner + ".class"), node -> replaceText(node, text)));
        }
        for (String[] hud : HUDS_REPLACED) {
            entries.put(hud[0] + ".class", transform(entries.get(hud[0] + ".class"), node -> replacedHud(method(node, hud[1], hud[2]))));
        }
        entries.put(FRUIT + ".class", transform(entries.get(FRUIT + ".class"), PiecePatch::uoFruit));
        entries.put(HAKI_KEYS + ".class", transform(entries.get(HAKI_KEYS + ".class"), PiecePatch::keyDefaults));
        entries.put(BINDS + ".class", transform(entries.get(BINDS + ".class"), PiecePatch::slotLetters));
        entries.put(BINDS + ".class", transform(entries.get(BINDS + ".class"), PiecePatch::tidySlots));
        entries.put(HAKI_ANIMATIONS + ".class", transform(entries.get(HAKI_ANIMATIONS + ".class"), node -> {
            firstPersonArms(node);
            toonClips(node);
        }));
        entries.put(CONTROLLER + ".class", transform(entries.get(CONTROLLER + ".class"), PiecePatch::conquerorHaki));
        entries.put(GEAR_KEYS + ".class", transform(entries.get(GEAR_KEYS + ".class"), PiecePatch::nikaKeys));
        entries.put(REMOTE_GEAR + ".class", transform(entries.get(REMOTE_GEAR + ".class"), PiecePatch::gearFastClient));
        entries.put(BAJRANG_LAYER + ".class", transform(entries.get(BAJRANG_LAYER + ".class"), PiecePatch::nikaFist));
        entries.put(RUBBER_LIMB_LAYER + ".class", transform(entries.get(RUBBER_LIMB_LAYER + ".class"), PiecePatch::pistolArm));
        entries.put(GIANT_GRAB + ".class", transform(entries.get(GIANT_GRAB + ".class"), PiecePatch::giantHold));
        entries.put(RUBBER_BEND_STATES + ".class", transform(entries.get(RUBBER_BEND_STATES + ".class"), PiecePatch::starRubberStates));
        entries.put(RUBBER_BEND_DEFORMER + ".class", transform(entries.get(RUBBER_BEND_DEFORMER + ".class"), PiecePatch::starRubberDeformer));
        entries.put(GEAR_SERVER + ".class", transform(entries.get(GEAR_SERVER + ".class"), node -> {
            gearLightning(node);
            gearDrums(node);
            gearUntransformed(node);
            gearFast(node);
            dawnWhip(node);
            dawnPistol(node);
            rubberChaos(node);
        }));
        entries.put(GEAR_FIELDS + ".class", transform(entries.get(GEAR_FIELDS + ".class"), PiecePatch::fieldsQuiet));
        entries.put(GEAR_AURA + ".class", transform(entries.get(GEAR_AURA + ".class"), PiecePatch::gearAura));
        // 2.9.4 (the owner: "Get rid of this fucking old lighting from everywhere, including Gear 5"): the effects that played their
        // lightning themselves, past HakiBolts, and showed the original's flat beams
        entries.put(GEAR_VFX + ".class", transform(entries.get(GEAR_VFX + ".class"), node -> {
            playHook(method(node, "playAt", "(DDDLcom/lowdragmc/photon/client/fx/FX;)V"), "playedGear", "(Lcom/lowdragmc/photon/client/fx/FX;DDD)Z");
            rubberWorld(node);
        }));
        entries.put(THUNDERSTORM + ".class", transform(entries.get(THUNDERSTORM + ".class"), PiecePatch::thunderstormBolts));
        entries.put(STORM_LANCE + ".class", transform(entries.get(STORM_LANCE + ".class"), PiecePatch::lanceBolts));
        entries.put(FRUIT_ANIMATIONS + ".class", transform(entries.get(FRUIT_ANIMATIONS + ".class"), PiecePatch::fruitClips));
        entries.put(CLIENT_KITS + ".class", transform(entries.get(CLIENT_KITS + ".class"), node -> {
            for (FieldNode field : node.fields) {
                if (field.name.equals("KITS")) {
                    field.access = (field.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
                    return;
                }
            }
            throw new AssertionError("missing ClientKits.KITS");
        }));
        entries.put("META-INF/mods.toml", modsToml(entries.get("META-INF/mods.toml"), version));
        entries.put("META-INF/MANIFEST.MF", manifest(entries.get("META-INF/MANIFEST.MF"), version));
        Files.createDirectories(output.toAbsolutePath().getParent());
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        System.out.println("Patched " + input.getFileName() + " -> " + output.getFileName() + " (" + version + ")");
    }

    interface Change {
        void apply(ClassNode node);
    }

    static byte[] transform(byte[] bytes, Change change) {
        if (bytes == null) {
            throw new AssertionError("class to patch is missing");
        }
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        change.apply(node);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        byte[] out = writer.toByteArray();
        frames(out);
        return out;
    }

    /**
     * 2.8.9 (the owner's crash on 2.8.8: "java.lang.VerifyError: Expecting a stackmap frame at branch target 9 ... FruitAnimations.handles"):
     * the game verifies every class by its stack map frames (classes from Java 7 on must carry them), and a patch writes them only where
     * it adds them itself (COMPUTE_MAXS). Every patched class is read back and each method checked as the JVM checks it: every branch
     * target, switch case and exception handler, and every instruction after a jump, a return or a throw, must start with a frame.
     */
    static void frames(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        if ((node.version & 0xFFFF) < 50) {
            return;
        }
        for (MethodNode method : node.methods) {
            java.util.Set<LabelNode> targets = new java.util.HashSet<>();
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof JumpInsnNode jump) {
                    targets.add(jump.label);
                } else if (insn instanceof jdk.internal.org.objectweb.asm.tree.TableSwitchInsnNode table) {
                    targets.add(table.dflt);
                    targets.addAll(table.labels);
                } else if (insn instanceof jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode lookup) {
                    targets.add(lookup.dflt);
                    targets.addAll(lookup.labels);
                }
            }
            for (jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode block : method.tryCatchBlocks) {
                targets.add(block.handler);
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                boolean needs = insn instanceof LabelNode label && targets.contains(label);
                int op = insn.getOpcode();
                boolean ends = op == GOTO || op == ATHROW || op >= IRETURN && op <= RETURN
                    || insn instanceof jdk.internal.org.objectweb.asm.tree.TableSwitchInsnNode
                    || insn instanceof jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode;
                if (!needs && !ends) {
                    continue;
                }
                // the next real instruction must be preceded (among labels and line numbers) by a frame
                AbstractInsnNode next = ends ? insn.getNext() : insn;
                boolean frame = false;
                while (next != null && next.getOpcode() < 0) {
                    frame |= next instanceof FrameNode;
                    next = next.getNext();
                }
                if (next != null && !frame) {
                    throw new AssertionError(node.name + "." + method.name + method.desc
                        + ": no stack map frame where the JVM needs one (instruction " + method.instructions.indexOf(next) + ")");
                }
            }
        }
    }

    static MethodNode method(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && (desc == null || method.desc.equals(desc))) {
                return method;
            }
        }
        throw new AssertionError("missing method " + node.name + "." + name + (desc == null ? "" : desc));
    }

    /** HexHaki(FMLJavaModLoadingContext): before every return, HexPiece.init(context.getModEventBus()). */
    static void bootstrap(ClassNode node) {
        MethodNode init = method(node, "<init>", "(L" + CONTEXT + ";)V");
        int hooks = 0;
        for (AbstractInsnNode insn = init.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == RETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(ALOAD, 1));
                call.add(new MethodInsnNode(INVOKEVIRTUAL, CONTEXT, "getModEventBus", "()L" + BUS + ";", false));
                call.add(new MethodInsnNode(INVOKESTATIC, BOOT, "init", "(L" + BUS + ";)V", false));
                init.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks != 1) {
            throw new AssertionError("expected one return in HexHaki.<init>, found " + hooks);
        }
    }

    static void lightningPainters(ClassNode node) {
        for (String name : VFX_WIDENED) {
            for (MethodNode method : node.methods) {
                if (method.name.equals(name)) {
                    method.access = (method.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
                }
            }
        }
        for (String[] pair : VFX_TRAMPOLINES) {
            trampoline(method(node, pair[0], SEGMENT), LIGHTNING, pair[1]);
        }
        for (Map.Entry<String, Integer> entry : LIGHTNING_PAIRS.entrySet()) {
            int redirected = redirectPairs(method(node, entry.getKey(), null));
            if (redirected != entry.getValue()) {
                throw new AssertionError("expected " + entry.getValue() + " lightning beams in HakiVfx." + entry.getKey() + ", found " + redirected);
            }
        }
        for (String[] hook : PLAY_HOOKS) {
            playHook(method(node, hook[0], hook[1]), hook[2], hook[3]);
        }
        stormHook(method(node, "conquerorRelease", "(FIJ)Lcom/lowdragmc/photon/client/fx/FX;"));
        conquerorTint(node);
    }

    /** 2.3.4: before each return of {@code GearJacketSkin.resolve(player, skin)}: {@code result = HakiSkin.resolve(player, result)}. */
    static void hakiSkinHook(ClassNode node) {
        MethodNode method = method(node, "resolve", SKIN_DESC);
        int hooks = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == ARETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(ALOAD, 0));
                call.add(new InsnNode(SWAP));
                call.add(new MethodInsnNode(INVOKESTATIC, HAKI_SKIN, "resolve", SKIN_DESC, false));
                method.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks < 2) {
            throw new AssertionError("expected the returns of GearJacketSkin.resolve, found " + hooks);
        }
    }

    /**
     * 2.3.3: {@code accept0(message)} first tells {@code HakiTint.visual(message)} (2.4.9; was {@code owner(entityId)}) whose visual it builds;
     * {@code conquerorParticle} turns its colour (its fifth argument) through {@code HakiTint.conqueror} before using it; the
     * Conqueror's and the last stand's own {@code particleTex} calls go to {@code HakiTint.particleTex} (the same, the colour tinted).
     */
    static void conquerorTint(ClassNode node) {
        MethodNode accept = method(node, "accept0", "(Lcom/hexhaki/network/msg/S2CHakiVisual;)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        // 2.4.9: the whole message (a Conqueror hit takes its striker's colour: HakiTint.visual)
        head.add(new MethodInsnNode(INVOKESTATIC, TINT, "visual", "(Lcom/hexhaki/network/msg/S2CHakiVisual;)V", false));
        accept.instructions.insert(head);
        MethodNode particle = method(node, "conquerorParticle", "(IIFFIZ)Lcom/lowdragmc/photon/client/gameobject/emitter/particle/ParticleEmitter;");
        InsnList tint = new InsnList();
        tint.add(new VarInsnNode(ILOAD, 4));
        tint.add(new MethodInsnNode(INVOKESTATIC, TINT, "conqueror", "(I)I", false));
        tint.add(new VarInsnNode(ISTORE, 4));
        particle.instructions.insert(tint);
        for (Map.Entry<String, Integer> entry : TINTED_TEX.entrySet()) {
            int found = 0;
            for (AbstractInsnNode insn : method(node, entry.getKey(), null).instructions) {
                if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(VFX)
                    && call.name.equals("particleTex") && call.desc.equals(PARTICLE_TEX)) {
                    call.owner = TINT;
                    found++;
                }
            }
            if (found != entry.getValue()) {
                throw new AssertionError("expected " + entry.getValue() + " particleTex calls in HakiVfx." + entry.getKey() + ", found " + found);
            }
        }
    }

    /**
     * 2.1.7: {@code conquerorRelease(charge, variant, seed)} hands its composition to {@code HakiStorm.release(fx, charge, variant, seed)}
     * as it returns it (the full-power storm is painted into it, to be let go with it). No branch: no frame to add.
     */
    static void stormHook(MethodNode method) {
        int hooks = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == ARETURN) {
                InsnList call = new InsnList();
                call.add(new InsnNode(DUP));
                call.add(new VarInsnNode(FLOAD, 0));
                call.add(new VarInsnNode(ILOAD, 1));
                call.add(new VarInsnNode(LLOAD, 2));
                call.add(new MethodInsnNode(INVOKESTATIC, STORM, "release", "(Lcom/lowdragmc/photon/client/fx/FX;FIJ)V", false));
                method.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks < 1) {
            throw new AssertionError("no return in HakiVfx.conquerorRelease");
        }
    }

    /**
     * 2.1.5: HakiVfx's three players first hand the composition to {@code HakiBolts} ({@code if (HakiBolts.played...(fx, anchor))
     * return;}), which sets the composition's 3D bolts going from the same anchor and takes back the flat beams they stand in for; it
     * answers true only when nothing is left for Photon to play (so the original never logs an empty composition).
     */
    static void playHook(MethodNode method, String hook, String desc) {
        Type[] args = Type.getArgumentTypes(method.desc);
        int fxSlot = 0;
        for (int i = 0; i < args.length - 1; i++) {
            fxSlot += args[i].getSize();
        }
        if (!args[args.length - 1].getInternalName().equals("com/lowdragmc/photon/client/fx/FX")) {
            throw new AssertionError("HakiVfx." + method.name + " no longer takes the composition last");
        }
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, fxSlot));
        int slot = 0;
        for (int i = 0; i < args.length - 1; i++) {
            head.add(new VarInsnNode(args[i].getOpcode(ILOAD), slot));
            slot += args[i].getSize();
        }
        head.add(new MethodInsnNode(INVOKESTATIC, BOLTS, hook, desc, false));
        LabelNode play = new LabelNode();
        head.add(new JumpInsnNode(IFEQ, play));
        head.add(new InsnNode(RETURN));
        head.add(play);
        head.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(head);
    }

    /** 2.2.2: {@code if (AbilityHud.replaces()) return;} at the head of an original HUD's draw. */
    static void replacedHud(MethodNode method) {
        InsnList head = new InsnList();
        head.add(new MethodInsnNode(INVOKESTATIC, HUD, "replaces", "()Z", false));
        LabelNode draw = new LabelNode();
        head.add(new JumpInsnNode(IFEQ, draw));
        head.add(new InsnNode(RETURN));
        head.add(draw);
        head.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(head);
    }

    static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }

    static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode previous = insn.getPrevious();
        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    /** Every {@code beam}/{@code rageBeam} call straight followed by {@code add} becomes one {@code HakiLightning} call. */
    static int redirectPairs(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(VFX) && call.desc.equals(BEAM)
                && (call.name.equals("beam") || call.name.equals("rageBeam"))) {
                AbstractInsnNode next = nextReal(call);
                if (next instanceof MethodInsnNode add && add.getOpcode() == INVOKESTATIC && add.owner.equals(VFX) && add.name.equals("add")
                    && add.desc.equals(ADD)) {
                    MethodInsnNode bolt = new MethodInsnNode(INVOKESTATIC, LIGHTNING, call.name.equals("beam") ? "bolt" : "rage", BOLT, false);
                    method.instructions.set(call, bolt);
                    method.instructions.remove(add);
                    insn = bolt;
                    count++;
                }
            }
        }
        return count;
    }

    /** One {@link #CALL_REDIRECTS} row: every matching static call in the method goes to the new owner instead; the count must match. */
    static void redirectCalls(ClassNode node, Object[] redirect) {
        MethodNode method = method(node, (String) redirect[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(redirect[2])
                && call.name.equals(redirect[3]) && call.desc.equals(redirect[4])) {
                call.owner = (String) redirect[5];
                call.name = (String) redirect[6];
                call.itf = false;
                count++;
            }
        }
        if (count != (Integer) redirect[7]) {
            throw new AssertionError("expected " + redirect[7] + " calls of " + redirect[2] + "." + redirect[3] + " in " + node.name + "."
                + redirect[1] + ", found " + count);
        }
    }

    /** One {@link #VIRTUAL_REDIRECTS} row: every matching instance call in the method becomes a static call taking its receiver first. */
    static void redirectVirtual(ClassNode node, Object[] redirect) {
        MethodNode method = method(node, (String) redirect[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKEVIRTUAL && call.owner.equals(redirect[2])
                && call.name.equals(redirect[3]) && call.desc.equals(redirect[4])) {
                call.setOpcode(INVOKESTATIC);
                call.desc = "(L" + call.owner + ";" + call.desc.substring(1);
                call.owner = (String) redirect[5];
                call.name = (String) redirect[6];
                call.itf = false;
                count++;
            }
        }
        if (count != (Integer) redirect[7]) {
            throw new AssertionError("expected " + redirect[7] + " calls of " + redirect[2] + "." + redirect[3] + " in " + node.name + "."
                + redirect[1] + ", found " + count);
        }
    }

    /**
     * 2.7.0 (the owner: "Add a new fruit called Uo Uo No Mi"): the original's fruits are an enum ({@code Fruit}: NONE, PIKA, MAGU,
     * GURA); UO joins it last (ordinal 4: every saved fruit keeps its number), so the original registers its item
     * ({@code uo_uo_no_mi}, in the fruits' creative tab), eats it, saves it, binds its moves and shows it as its own. Its kit is
     * HexPiece's (server/UoKit, client/dragon/UoClient).
     */
    static void uoFruit(ClassNode node) {
        // 3.1.9 (the owner: "Can you create the ice fruit, the Aokiji one?"): the Hie Hie no Mi joins after it (ordinal 5); 3.3.4: the
        // Noro Noro no Mi (ordinal 6); 3.3.5: the Supa Supa no Mi (ordinal 7); 3.3.6: the Horo Horo no Mi (ordinal 8); 3.3.7: the Ito Ito
        // no Mi (ordinal 9); 3.5.5: the Mochi Mochi no Mi (ordinal 10)
        String[][] extra = {UO, HIE, NORO, SUPA, HORO, ITO, MOCHI};
        String[] names = {"UO", "HIE", "NORO", "SUPA", "HORO", "ITO", "MOCHI"};
        boolean[] logia = {false, true, false, false, false, false, false};
        String type = "L" + FRUIT + ";";
        for (String name : names) {
            node.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_ENUM, name, type, null, null));
        }
        MethodNode clinit = method(node, "<clinit>", "()V");
        MethodInsnNode values = null;
        for (AbstractInsnNode insn = clinit.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals(FRUIT) && call.name.equals("$values")) {
                values = call;
            }
        }
        if (values == null) {
            throw new AssertionError("Fruit.<clinit> has no $values call");
        }
        for (int k = 0; k < names.length; k++) {
            InsnList make = new InsnList();
            make.add(new TypeInsnNode(NEW, FRUIT));
            make.add(new InsnNode(DUP));
            make.add(new LdcInsnNode(names[k]));
            make.add(new IntInsnNode(BIPUSH, 4 + k));
            make.add(new LdcInsnNode(extra[k][0]));
            make.add(new LdcInsnNode(extra[k][1]));
            make.add(new LdcInsnNode(extra[k][2]));
            make.add(new InsnNode(logia[k] ? ICONST_1 : ICONST_0));
            make.add(new LdcInsnNode(Integer.parseInt(extra[k][3], 16)));
            make.add(new LdcInsnNode(extra[k][4]));
            make.add(new MethodInsnNode(INVOKESPECIAL, FRUIT, "<init>",
                "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/String;)V", false));
            make.add(new FieldInsnNode(PUTSTATIC, FRUIT, names[k], type));
            clinit.instructions.insertBefore(values, make);
        }
        MethodNode list = method(node, "$values", "()[" + type);
        AbstractInsnNode first = list.instructions.getFirst();
        while (first.getOpcode() < 0) {
            first = first.getNext();
        }
        if (first.getOpcode() != ICONST_4) {
            throw new AssertionError("Fruit.$values does not start with 4 values");
        }
        list.instructions.set(first, new IntInsnNode(BIPUSH, 4 + names.length));
        AbstractInsnNode end = list.instructions.getLast();
        while (end.getOpcode() != ARETURN) {
            end = end.getPrevious();
        }
        for (int k = 0; k < names.length; k++) {
            InsnList add = new InsnList();
            add.add(new InsnNode(DUP));
            add.add(new IntInsnNode(BIPUSH, 4 + k));
            add.add(new FieldInsnNode(GETSTATIC, FRUIT, names[k], type));
            add.add(new InsnNode(AASTORE));
            list.instructions.insertBefore(end, add);
        }
    }

    /**
     * 2.7.1 (the owner: "All keys of the mod (but haki) will now be the following Z, X, C, V, B, N, M. R For switching modes / Haki",
     * then: Conqueror stays G, Observation V -> J, the Conqueror's quick abilities on K, L, H (M -> H), the Ability Book X -> O): the
     * original's key defaults ({@code HakiKeys.<clinit>}: each KeyMapping's name, then its key code). The nine quick slots are
     * G K L H (Haki) and B Z C V X (the fruit's moves); J's old "acoc" key (empty: Ryou comes with Armament) is unbound. A player's
     * saved keys move once (client/KeyLayout).
     */
    static void keyDefaults(ClassNode node) {
        MethodNode clinit = method(node, "<clinit>", "()V");
        Map<String, Integer> left = new java.util.HashMap<>(KEY_CODES);
        String name = null;
        for (AbstractInsnNode insn = clinit.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String text) {
                name = text;
            } else if (insn instanceof IntInsnNode code && code.getOpcode() == BIPUSH && name != null && left.containsKey(name)) {
                int to = left.remove(name);
                clinit.instructions.set(code, to == -1 ? new InsnNode(ICONST_M1) : new IntInsnNode(BIPUSH, to));
                name = null;
                insn = clinit.instructions.getFirst(); // the list changed: start over (the done ones are gone from left)
            }
        }
        if (!left.isEmpty()) {
            throw new AssertionError("HakiKeys has no default for " + left.keySet());
        }
    }

    /** 2.8.2: the original's animation player, and PlayerAnimator's first-person configuration. */
    static final String HAKI_ANIMATIONS = "com/hexhaki/client/anim/HakiAnimations";
    static final String FRUIT_ANIMATIONS = "com/hexhaki/fruit/client/FruitAnimations";

    /**
     * 2.8.8 (the owner: "Light animations arent playing"): the original plays a fruit's stance without end on its own layer
     * (FruitAnimations, priority 1100) and only the clips it {@code handles} ("light_", "magma_", "quake_") as moves on that layer, which
     * stand the stance down while they play; every other clip goes to HakiAnimations' layer (900), under the stance, unseen. The rebuilt
     * Light fruit's clips ("pika_") and the Uo Uo no Mi's ("uo_") are its too: {@code handles} asks {@code PieceHooks.fruitClip(name)}
     * first.
     */
    static void fruitClips(ClassNode node) {
        MethodNode method = method(node, "handles", "(Ljava/lang/String;)Z");
        InsnList hook = new InsnList();
        LabelNode theirs = new LabelNode();
        hook.add(new VarInsnNode(ALOAD, 0));
        hook.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "fruitClip", "(Ljava/lang/String;)Z", false));
        hook.add(new JumpInsnNode(IFEQ, theirs));
        hook.add(new InsnNode(ICONST_1));
        hook.add(new InsnNode(IRETURN));
        hook.add(theirs);
        // 2.8.9: the jump's target needs its stack map frame (2.8.8 shipped without one: the game refused the class at load)
        hook.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(hook);
    }
    static final String FP_CONFIG = "dev/kosmx/playerAnim/api/firstPerson/FirstPersonConfiguration";

    /**
     * 2.8.2: in {@code HakiAnimations.playRemote(entity, name, fade)}, the first-person configuration (both arms hidden) goes through
     * {@code PieceHooks.firstPersonArms(config, name)} before it is set: a style's blows show their arms in first person.
     */
    static void firstPersonArms(ClassNode node) {
        MethodNode method = method(node, "playRemote", "(ILjava/lang/String;I)V");
        int hooks = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.name.equals("setFirstPersonConfiguration")) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 1));
                hook.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "firstPersonArms", "(L" + FP_CONFIG + ";Ljava/lang/String;)L" + FP_CONFIG + ";",
                    false));
                method.instructions.insertBefore(insn, hook);
                hooks++;
            }
        }
        if (hooks != 1) {
            throw new AssertionError("expected one setFirstPersonConfiguration in HakiAnimations.playRemote, found " + hooks);
        }
        // 3.3.9: the original's OWN_FIRST_PERSON_ARM (gear5_pistol and the other stretches: PlayerAnimator left out of first person,
        // the original's own stretched arm drawn there) is asked through PieceHooks.ownFirstPerson, which adds Dawn Pistol's clips: its
        // first person stays exactly the Pistol's (the owner: "The first person is fine")
        int owns = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKEINTERFACE && call.owner.equals("java/util/Set")
                && call.name.equals("contains") && call.getPrevious() instanceof VarInsnNode name && name.var == 1
                && name.getPrevious() instanceof FieldInsnNode set && set.name.equals("OWN_FIRST_PERSON_ARM")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, HOOKS, "ownFirstPerson", "(Ljava/util/Set;Ljava/lang/Object;)Z", false);
                method.instructions.set(call, ours);
                insn = ours;
                owns++;
            }
        }
        if (owns != 1) {
            throw new AssertionError("expected one OWN_FIRST_PERSON_ARM.contains in HakiAnimations.playRemote, found " + owns);
        }
        // 2.9.0: every clip a player is told to play goes past PieceHooks.played(id, name) first (no branch: no frame needed), so the
        // client knows which of the Light fruit's moves each player is in and since when (its first person, its fingertip)
        InsnList told = new InsnList();
        told.add(new VarInsnNode(ILOAD, 0));
        told.add(new VarInsnNode(ALOAD, 1));
        told.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "played", "(ILjava/lang/String;)V", false));
        method.instructions.insert(told);
    }

    static final String CONQUEROR_AURA = "com/hexhaki/piece/server/ConquerorAura";
    static final String GEAR_VFX = "com/hexhaki/client/devilfruit/GearVfx";
    static final String THUNDERSTORM = "com/hexhaki/patch/ThunderstormImpact";
    static final String STORM_LANCE = "com/hexhaki/client/devilfruit/StormLanceVfx";
    static final String LANCE_LIGHTNING = "com/hexhaki/piece/client/haki/LanceLightning";

    /**
     * 2.9.4: {@code ThunderstormImpact.play} builds its composition (its lightning through HakiFx.bolt, so painted as 3D bolts too) and
     * plays it itself (a BlockEffect at the point it hands {@code BlockPos.containing}): just before it makes that BlockEffect, the
     * composition and the point go to {@code HakiBolts.playedAtPoint} (its bolts let go, its flat beams taken out). The answer is
     * dropped: what is left (its flashes and smoke) plays as before. No branch: no frame.
     */
    static void thunderstormBolts(ClassNode node) {
        MethodNode play = method(node, "play", "(Lnet/minecraft/world/phys/Vec3;FJ)V");
        int hooks = 0;
        for (AbstractInsnNode insn = play.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof TypeInsnNode type && type.getOpcode() == NEW && type.desc.equals("com/lowdragmc/photon/client/fx/BlockEffect")) {
                AbstractInsnNode dup = nextReal(type);
                AbstractInsnNode fxLoad = nextReal(dup);
                if (!(fxLoad instanceof VarInsnNode fx) || fx.getOpcode() != ALOAD) {
                    throw new AssertionError("ThunderstormImpact.play: the BlockEffect is not made from a local composition");
                }
                // the point: the Vec3 handed to BlockPos.containing just before
                VarInsnNode point = null;
                for (AbstractInsnNode back = type.getPrevious(); back != null; back = back.getPrevious()) {
                    if (back instanceof MethodInsnNode call && call.name.equals("m_274446_")) {
                        AbstractInsnNode arg = call.getPrevious();
                        while (arg != null && arg.getOpcode() < 0) {
                            arg = arg.getPrevious();
                        }
                        if (arg instanceof VarInsnNode v && v.getOpcode() == ALOAD) {
                            point = v;
                        }
                        break;
                    }
                }
                if (point == null) {
                    throw new AssertionError("ThunderstormImpact.play: no point found for its BlockEffect");
                }
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, fx.var));
                hook.add(new VarInsnNode(ALOAD, point.var));
                hook.add(new MethodInsnNode(INVOKESTATIC, BOLTS, "playedAtPoint",
                    "(Lcom/lowdragmc/photon/client/fx/FX;Lnet/minecraft/world/phys/Vec3;)Z", false));
                hook.add(new InsnNode(POP));
                play.instructions.insertBefore(type, hook);
                hooks++;
            }
        }
        if (hooks != 1) {
            throw new AssertionError("expected one BlockEffect in ThunderstormImpact.play, found " + hooks);
        }
    }

    /**
     * 2.9.4: Gear 5's storm lance: its own flat bolt strips ({@code StormLanceVfx.bolt}) go to {@code LanceLightning.bolt} instead (a
     * trampoline), and its {@code draw(pose, camera)} first draws them as the Haki's 3D lightning ({@code LanceLightning.draw}). No branch
     * added: no frame.
     */
    static void lanceBolts(ClassNode node) {
        trampoline(method(node, "bolt", "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FFJID)V"), LANCE_LIGHTNING, "bolt");
        MethodNode draw = method(node, "draw", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;)V");
        InsnList first = new InsnList();
        first.add(new VarInsnNode(ALOAD, 0));
        first.add(new VarInsnNode(ALOAD, 1));
        first.add(new MethodInsnNode(INVOKESTATIC, LANCE_LIGHTNING, "draw", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;)V",
            false));
        draw.instructions.insert(first);
    }

    /**
     * 2.9.1 (the owner: "the higher your mastery on haki, like the faster the Conqueror release will be"; "even your Rio, every move,
     * like, should be coating you with lightning"): in HakiServerController, the release's charge a tick ({@code tickConquerorCharge}'s
     * 0.625) becomes {@code ConquerorAura.rate(player)}, and every clip set playing ({@code animate(player, name, fade)}) goes past
     * {@code ConquerorAura.moved(player, name)} first. Neither adds a branch: no frame needed.
     */
    static void conquerorHaki(ClassNode node) {
        MethodNode tick = method(node, "tickConquerorCharge", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        int rates = 0;
        for (AbstractInsnNode insn = tick.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Double d && d == 0.625) {
                InsnList rate = new InsnList();
                rate.add(new VarInsnNode(ALOAD, 0));
                rate.add(new MethodInsnNode(INVOKESTATIC, CONQUEROR_AURA, "rate", "(Lnet/minecraft/server/level/ServerPlayer;)D", false));
                tick.instructions.insertBefore(ldc, rate);
                AbstractInsnNode after = ldc.getNext();
                tick.instructions.remove(ldc);
                insn = after.getPrevious();
                rates++;
            }
        }
        if (rates != 1) {
            throw new AssertionError("expected one 0.625 in HakiServerController.tickConquerorCharge, found " + rates);
        }
        MethodNode animate = method(node, "animate", "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;I)V");
        InsnList moved = new InsnList();
        moved.add(new VarInsnNode(ALOAD, 0));
        moved.add(new VarInsnNode(ALOAD, 1));
        moved.add(new MethodInsnNode(INVOKESTATIC, CONQUEROR_AURA, "moved", "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;)V", false));
        // 3.3.2: and Gear 5's jump hears of every clip (another one taking over in the air: its landing leaves that one alone)
        moved.add(new VarInsnNode(ALOAD, 0));
        moved.add(new VarInsnNode(ALOAD, 1));
        moved.add(new MethodInsnNode(INVOKESTATIC, "com/hexhaki/piece/server/NikaJump", "played",
            "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;)V", false));
        animate.instructions.insert(moved);
    }

    static final String GEAR_SERVER = "com/hexhaki/devilfruit/gameplay/GearServerController";

    /**
     * 3.1.5 (POINT ONE of the handoff, the owner: "every move from every fruit does that"): Gear 5's server sends every one of its
     * effects through {@code GearServerController.fx(player, kind, at, size)} (the blows' impacts, the landings, the coil's release, the
     * giant fist's slam, the star gun); each goes past {@code ConquerorAura.gear(player, kind, at, size)} first, which throws the
     * Conqueror's lightning (visual only) out of the impacts when the player has the Conqueror's Haki and Ryou on. No branch added.
     */
    static void gearLightning(ClassNode node) {
        MethodNode fx = method(node, "fx", "(Lnet/minecraft/server/level/ServerPlayer;Lcom/hexhaki/network/msg/S2CGearFx$Kind;"
            + "Lnet/minecraft/world/phys/Vec3;F)V");
        InsnList call = new InsnList();
        call.add(new VarInsnNode(ALOAD, 0));
        call.add(new VarInsnNode(ALOAD, 1));
        call.add(new VarInsnNode(ALOAD, 2));
        call.add(new VarInsnNode(FLOAD, 3));
        call.add(new MethodInsnNode(INVOKESTATIC, CONQUEROR_AURA, "gear",
            "(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Enum;Lnet/minecraft/world/phys/Vec3;F)V", false));
        fx.instructions.insert(call);
    }

    /** 3.2.3: Gear 5's transformation drums (server/GearDrums). */
    static final String GEAR_DRUMS = "com/hexhaki/piece/server/GearDrums";

    /**
     * 3.2.3 (the owner: "give the transformation just the drum sounds and just get rid of the music because of copyright"): on each of the
     * chest pounding's beats ({@code GearTiming.DRUM_STRIKES}) {@code GearServerController.tickTransform} played two of the game's own
     * sounds ({@code SoundEvents.f_12316_} then {@code f_12090_}) through its {@code sound(player, event, volume, pitch)}: the first call
     * now goes to {@code GearDrums.beat} (a CC0 drum), the second to {@code GearDrums.under} (nothing: one drum a beat). The music itself
     * is silenced in sounds.json. Exactly one call after each of the two fields, or the build fails.
     */
    static void gearDrums(ClassNode node) {
        MethodNode tick = method(node, "tickTransform", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        String sound = "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/sounds/SoundEvent;FF)V";
        Map<String, String> to = Map.of("f_12316_", "beat", "f_12090_", "under");
        Map<String, Integer> done = new java.util.HashMap<>();
        for (AbstractInsnNode insn = tick.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC && field.owner.equals("net/minecraft/sounds/SoundEvents")
                && to.containsKey(field.name)) {
                for (AbstractInsnNode next = insn.getNext(); next != null; next = next.getNext()) {
                    if (next instanceof MethodInsnNode call && call.owner.equals(GEAR_SERVER) && call.name.equals("sound") && call.desc.equals(sound)) {
                        tick.instructions.set(call, new MethodInsnNode(INVOKESTATIC, GEAR_DRUMS, to.get(field.name), sound, false));
                        done.merge(field.name, 1, Integer::sum);
                        break;
                    }
                }
            }
        }
        if (!done.equals(Map.of("f_12316_", 1, "f_12090_", 1))) {
            throw new AssertionError("Gear 5's beat sounds are not the ones inspected: " + done);
        }
    }

    /** 3.2.3: the Nika fruit's keys (client/gear/NikaKeys). */
    static final String GEAR_KEYS = "com/hexhaki/client/devilfruit/GearKeys";
    static final String NIKA_KEYS = "com/hexhaki/piece/client/gear/NikaKeys";

    /**
     * 3.2.3 (the owner: "even transformation to Z, and all the others following the transformation, X, C, V, B, N, M"; asked, "Star Gun
     * takes X"): {@code GearKeys.tick(minecraft)} first asks {@code NikaKeys.tick(minecraft)} and returns at once when it says it did
     * everything (it always does: the fruit keys Z X C V B N M in place of the Haki keys); {@code releaseHeld} and {@code drain} call
     * NikaKeys' own first, so held moves are let go and waiting presses dropped there too. The original's code stays as it is behind them.
     */
    static void nikaKeys(ClassNode node) {
        MethodNode tick = method(node, "tick", "(Lnet/minecraft/client/Minecraft;)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        head.add(new MethodInsnNode(INVOKESTATIC, NIKA_KEYS, "tick", "(Lnet/minecraft/client/Minecraft;)Z", false));
        LabelNode theirs = new LabelNode();
        head.add(new JumpInsnNode(IFEQ, theirs));
        head.add(new InsnNode(RETURN));
        head.add(theirs);
        head.add(new FrameNode(F_SAME, 0, null, 0, null));
        tick.instructions.insert(head);
        for (String name : new String[] {"releaseHeld", "drain"}) {
            method(node, name, "()V").instructions.insert(new MethodInsnNode(INVOKESTATIC, NIKA_KEYS, name, "()V", false));
        }
    }

    /** 3.2.4: the client's clock of a Gear 5 transformation (RemoteGearStates.advance). */
    static final String REMOTE_GEAR = "com/hexhaki/client/devilfruit/RemoteGearStates";

    /**
     * 3.2.4 (the owner, asked: the transformation "Shorten to about 4 s"): the transformation runs on its own clock ({@code GearData
     * "transformTick"}, 1 to 170, its beats at fixed ticks: the burst at 34, the drums at 40 to 96, the leap at 129, the landing at 164;
     * the clients read the same clock for the clouds, the white skin and the chatter). Running that clock twice a tick halves it
     * everywhere at once: {@code GearServerController.tick} calls {@code tickTransform} a second time right after the first (it returns
     * at once when the transformation ended in the first), so every beat still lands on its own tick of the clock, 85 game ticks in all
     * (tools/nika_anims.py's gear5_awaken is 85 ticks with the same beats). Exactly one call, or the build fails.
     */
    static void gearFast(ClassNode node) {
        MethodNode tick = method(node, "tick", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        int calls = 0;
        for (AbstractInsnNode insn = tick.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals(GEAR_SERVER) && call.name.equals("tickTransform")) {
                InsnList again = new InsnList();
                MethodInsnNode second = new MethodInsnNode(INVOKESTATIC, GEAR_SERVER, "tickTransform", call.desc, false);
                again.add(new VarInsnNode(ALOAD, 0));
                again.add(second);
                tick.instructions.insert(call, again);
                calls++;
                insn = second;
            }
        }
        if (calls != 1) {
            throw new AssertionError("Gear 5's transformation clock is not called as inspected: " + calls);
        }
    }

    /**
     * 3.2.4: between the server's updates each client moves its own copy of the clock on ({@code RemoteGearStates.advance}: {@code
     * state.transformTick + 1} a tick, the clouds and the white skin following it): it moves by 2, as the server's now does. Exactly one
     * step, or the build fails.
     */
    static void gearFastClient(ClassNode node) {
        MethodNode advance = method(node, "advance", null);
        int steps = 0;
        for (AbstractInsnNode insn = advance.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field && field.getOpcode() == GETFIELD && field.name.equals("transformTick")
                && nextReal(field) instanceof InsnNode one && one.getOpcode() == ICONST_1 && nextReal(one) != null
                && nextReal(one).getOpcode() == IADD) {
                advance.instructions.set(one, new InsnNode(ICONST_2));
                steps++;
            }
        }
        if (steps != 1) {
            throw new AssertionError("RemoteGearStates.advance's transformation step is not the one inspected: " + steps);
        }
    }

    /**
     * 3.2.7 (the owner: "Dawn Whip needs a complete rework ... he actually rotates like a cartoon character"): {@code
     * GearServerController.whip}'s {@code HakiServerController.animate(player, "gear5_whip", 2)} becomes {@code NikaRules.whipClip} (the
     * cartoon spin with Haki on in Gear 5). Exactly one call, or the build fails.
     */
    static void dawnWhip(ClassNode node) {
        MethodNode whip = method(node, "whip", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        int calls = 0;
        for (AbstractInsnNode insn = whip.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals("com/hexhaki/gameplay/HakiServerController") && call.name.equals("animate")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, NIKA_RULES, "whipClip", call.desc, false);
                whip.instructions.set(call, ours);
                insn = ours;
                calls++;
            }
        }
        if (calls != 1) {
            throw new AssertionError("GearServerController.whip's clip is not as inspected: " + calls);
        }
    }

    static final String NIKA_PISTOL = "com/hexhaki/piece/server/NikaPistol";

    /**
     * 3.3.9 (the owner: "We need to better the hotkey [Haki] versions"; asked, "Build as proposed"): Dawn Pistol. {@code
     * GearServerController.pistol}'s {@code HakiServerController.animate(player, "gear5_pistol", 2)} becomes {@code NikaPistol.clip} (the
     * cartoon wind-up with Haki on); in the throw's landing (the {@code lambda$throwLimb$} that calls {@code hit}), right after that
     * {@code hit(player, target, damage, knock, way)}, {@code NikaPistol.struck(player, target, way, award)} is called with the very
     * locals the hit was given and the lambda's last argument (the Pistol's award 5; the Gatling's is 1). No branch, no frame. Exactly one
     * clip and one hit, or the build fails.
     */
    static void dawnPistol(ClassNode node) {
        MethodNode pistol = method(node, "pistol", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        int calls = 0;
        for (AbstractInsnNode insn = pistol.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals("com/hexhaki/gameplay/HakiServerController") && call.name.equals("animate")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, NIKA_PISTOL, "clip", call.desc, false);
                pistol.instructions.set(call, ours);
                insn = ours;
                calls++;
            }
        }
        if (calls != 1) {
            throw new AssertionError("GearServerController.pistol's clip is not as inspected: " + calls);
        }
        String hitDesc = "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;FFLnet/minecraft/world/phys/Vec3;)V";
        int hits = 0;
        for (MethodNode lambda : node.methods) {
            if (!lambda.name.startsWith("lambda$throwLimb$")) {
                continue;
            }
            Type[] args = Type.getArgumentTypes(lambda.desc);
            for (AbstractInsnNode insn = lambda.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != INVOKESTATIC || !call.owner.equals(GEAR_SERVER)
                    || !call.name.equals("hit") || !call.desc.equals(hitDesc)) {
                    continue;
                }
                // the hit's own arguments: player, target, damage, knock, way
                AbstractInsnNode[] loads = new AbstractInsnNode[5];
                AbstractInsnNode back = call.getPrevious();
                for (int k = 4; k >= 0; k--) {
                    loads[k] = back;
                    back = back == null ? null : back.getPrevious();
                }
                int[] ops = {ALOAD, ALOAD, FLOAD, FLOAD, ALOAD};
                for (int k = 0; k < 5; k++) {
                    if (!(loads[k] instanceof VarInsnNode load) || load.getOpcode() != ops[k]) {
                        throw new AssertionError("the Pistol's hit is not given plain locals as inspected");
                    }
                }
                if (args.length == 0 || args[args.length - 1].getSort() != Type.INT) {
                    throw new AssertionError("the throw's landing has no award as its last argument: " + lambda.desc);
                }
                int award = 0;
                for (Type arg : args) {
                    award += arg.getSize();
                }
                award -= 1;
                InsnList after = new InsnList();
                after.add(new VarInsnNode(ALOAD, ((VarInsnNode) loads[0]).var));
                after.add(new VarInsnNode(ALOAD, ((VarInsnNode) loads[1]).var));
                after.add(new VarInsnNode(ALOAD, ((VarInsnNode) loads[4]).var));
                after.add(new VarInsnNode(ILOAD, award));
                after.add(new MethodInsnNode(INVOKESTATIC, NIKA_PISTOL, "struck", "(Lnet/minecraft/server/level/ServerPlayer;"
                    + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;I)V", false));
                lambda.instructions.insert(call, after);
                hits++;
            }
        }
        if (hits != 1) {
            throw new AssertionError("the Pistol's landing hit is not as inspected: " + hits);
        }
    }

    static final String GEAR_FIELDS = "com/hexhaki/devilfruit/gameplay/GearFields";

    /**
     * 3.3.2 (the owner: "world of rubber makes it so for everyone else, it's main goal is to confuse the target, displace them and
     * overwhelm"): {@code GearServerController.releaseRubberWorld}'s {@code GearFields.add(level, centre, radius, ticks)} becomes
     * {@code RubberChaos.add(level, centre, radius, ticks, player)} (which still calls it): the field knows its user. Exactly one call.
     */
    static void rubberChaos(ClassNode node) {
        MethodNode release = method(node, "releaseRubberWorld", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        int calls = 0;
        for (AbstractInsnNode insn = release.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(GEAR_FIELDS) && call.name.equals("add")
                && call.desc.equals("(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DI)V")) {
                release.instructions.insertBefore(call, new VarInsnNode(ALOAD, 0));
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, "com/hexhaki/piece/server/RubberChaos", "add",
                    "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DILnet/minecraft/server/level/ServerPlayer;)V", false);
                release.instructions.set(call, ours);
                insn = ours;
                calls++;
            }
        }
        if (calls != 1) {
            throw new AssertionError("GearServerController.releaseRubberWorld's field is not as inspected: " + calls);
        }
    }

    /**
     * 3.3.2: the original's field bounced every body on the ground in it every tick, its user too ({@code GearFields.bounceWithin}): it
     * does nothing now (server/RubberChaos throws everyone but the user about on the waves). The field itself stays: no fall damage in it,
     * the clients told of it every second.
     */
    static void fieldsQuiet(ClassNode node) {
        MethodNode bounce = method(node, "bounceWithin", "(Lnet/minecraft/server/level/ServerLevel;Lcom/hexhaki/devilfruit/gameplay/GearFields$Field;J)V");
        InsnList body = new InsnList();
        body.add(new InsnNode(RETURN));
        bounce.instructions = body;
        bounce.tryCatchBlocks.clear();
        if (bounce.localVariables != null) {
            bounce.localVariables.clear();
        }
        bounce.visibleLocalVariableAnnotations = null;
        bounce.invisibleLocalVariableAnnotations = null;
    }

    /** 3.4.9: Gear 5's white outline (the whole body drawn twice more, scaled up round the chest). */
    static final String GEAR_AURA = "com/hexhaki/client/devilfruit/render/GearAuraLayer";

    /**
     * 3.4.9 (the owner: "the white aura around the player, it needs to be replaced with like some kind of like photon smoke ... the
     * outline that the player has, doesn't adapt well to animations"): GearAuraLayer.render draws nothing; Gear 5's aura is
     * client/gear/NikaSmokeFx's smoke. The layer stays registered (the original adds it), empty.
     */
    static void gearAura(ClassNode node) {
        MethodNode render = method(node, "render", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
            + "ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V");
        InsnList body = new InsnList();
        body.add(new InsnNode(RETURN));
        render.instructions = body;
        render.tryCatchBlocks.clear();
        if (render.localVariables != null) {
            render.localVariables.clear();
        }
        render.visibleLocalVariableAnnotations = null;
        render.invisibleLocalVariableAnnotations = null;
    }

    /** 3.2.8: the Giant's grab (server/GiantHold). */
    static final String GIANT_GRAB = "com/hexhaki/patch/GiantGrab";
    static final String GIANT_HOLD = "com/hexhaki/piece/server/GiantHold";

    /**
     * 3.2.8 (the owner: "Giant is not holding the creatures right. Sometimes they get off the player's hand. It's overall really junky"):
     * {@code GiantGrab.tick} puts the body at {@code GiantHold.hand} (the fist as the model draws it) in place of its own hand(...), and
     * after moving it calls {@code GiantHold.holding(player, held)}; {@code grab} calls {@code GiantHold.grabbed(player, held)} once it
     * has taken the body; {@code clear} first calls {@code GiantHold.letGo(player)}. The clients keep the body in the fist themselves
     * (client/gear/NikaHoldClient). Exactly one of each, in the locals inspected, or the build fails.
     */
    static void giantHold(ClassNode node) {
        String player = "Lnet/minecraft/server/level/ServerPlayer;";
        String living = "Lnet/minecraft/world/entity/LivingEntity;";
        MethodNode tick = method(node, "tick", "(" + player + ")V");
        int hands = 0;
        int moved = 0;
        for (AbstractInsnNode insn = tick.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(GIANT_GRAB)) {
                continue;
            }
            if (call.name.equals("hand")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, GIANT_HOLD, "hand", call.desc, false);
                tick.instructions.set(call, ours);
                insn = ours;
                hands++;
            } else if (call.name.equals("syncMotion")) {
                if (!(previousReal(call) instanceof VarInsnNode load) || load.getOpcode() != ALOAD || load.var != 2) {
                    throw new AssertionError("GiantGrab.tick's held body is not in local 2");
                }
                InsnList after = new InsnList();
                after.add(new VarInsnNode(ALOAD, 0));
                after.add(new VarInsnNode(ALOAD, 2));
                after.add(new MethodInsnNode(INVOKESTATIC, GIANT_HOLD, "holding", "(" + player + living + ")V", false));
                tick.instructions.insert(call, after);
                moved++;
            }
        }
        MethodNode grab = method(node, "grab", "(" + player + ")V");
        int grabbed = 0;
        for (AbstractInsnNode insn = grab.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals(GIANT_GRAB) && call.name.equals("syncMotion")) {
                if (!(previousReal(call) instanceof VarInsnNode load) || load.getOpcode() != ALOAD || load.var != 7) {
                    throw new AssertionError("GiantGrab.grab's body is not in local 7");
                }
                InsnList after = new InsnList();
                after.add(new VarInsnNode(ALOAD, 0));
                after.add(new VarInsnNode(ALOAD, 7));
                after.add(new MethodInsnNode(INVOKESTATIC, GIANT_HOLD, "grabbed", "(" + player + living + ")V", false));
                grab.instructions.insert(call, after);
                grabbed++;
            }
        }
        MethodNode clear = method(node, "clear", "(" + player + ")V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        head.add(new MethodInsnNode(INVOKESTATIC, GIANT_HOLD, "letGo", "(" + player + ")V", false));
        clear.instructions.insert(head);
        if (hands != 1 || moved != 1 || grabbed != 1) {
            throw new AssertionError("GiantGrab is not as inspected: hand " + hands + ", syncMotion in tick " + moved + ", in grab " + grabbed);
        }
    }

    /** 3.2.9: Star Gun's struck body as rubber (client/gear/NikaBendClient). */
    static final String RUBBER_BEND_STATES = "com/hexhaki/client/devilfruit/RubberBendStates";
    static final String RUBBER_BEND_DEFORMER = "com/hexhaki/client/devilfruit/render/RubberBendDeformer";
    static final String NIKA_BEND = "com/hexhaki/piece/client/gear/NikaBendClient";

    /**
     * 3.2.9 (the owner: "the Star Gun is okay. It could push the entities model a bit further and make it more like rubber"): the head
     * of {@code RubberBendStates.accept(S2CRubberBend)} calls {@code NikaBendClient.accept(message)}; {@code RubberBendDeformer.applyBend
     * (PoseStack, LivingEntity)} becomes {@code return NikaBendClient.apply(pose, entity)} (the same contract: a pose pushed and true,
     * or neither).
     */
    static void starRubberStates(ClassNode node) {
        MethodNode accept = method(node, "accept", "(Lcom/hexhaki/network/msg/S2CRubberBend;)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        head.add(new MethodInsnNode(INVOKESTATIC, NIKA_BEND, "accept", "(Lcom/hexhaki/network/msg/S2CRubberBend;)V", false));
        accept.instructions.insert(head);
    }

    static void starRubberDeformer(ClassNode node) {
        String desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;)Z";
        MethodNode apply = method(node, "applyBend", desc);
        InsnList body = new InsnList();
        body.add(new VarInsnNode(ALOAD, 0));
        body.add(new VarInsnNode(ALOAD, 1));
        body.add(new MethodInsnNode(INVOKESTATIC, NIKA_BEND, "apply", desc, false));
        body.add(new InsnNode(IRETURN));
        apply.instructions.clear();
        apply.tryCatchBlocks.clear();
        if (apply.localVariables != null) {
            apply.localVariables.clear();
        }
        apply.instructions.add(body);
    }

    /**
     * 3.3.0 (the owner: "Rubber world, we need the world to actually physically bounce like rubber"): the head of {@code GearVfx.play
     * (S2CGearFx)} tells {@code client/gear/RubberWorldDraw.play(message)} first (it keeps the RUBBER_FIELD fields and draws their ground
     * rolling in waves).
     */
    static void rubberWorld(ClassNode node) {
        MethodNode play = method(node, "play", "(Lcom/hexhaki/network/msg/S2CGearFx;)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        head.add(new MethodInsnNode(INVOKESTATIC, "com/hexhaki/piece/client/gear/RubberWorldDraw", "play", "(Lcom/hexhaki/network/msg/S2CGearFx;)V",
            false));
        play.instructions.insert(head);
    }

    /**
     * 3.3.1 (the owner: "every move should feel like a Bugs Bunny movie"): the head of {@code HakiAnimations.playRemote(id, clip,
     * priority)} (every clip any player starts, on every client) tells {@code client/gear/NikaToon.clip(id, clip)} first: the Nika
     * fruit's clips squash and stretch the body on a rubber spring (gear/ToonForce).
     */
    static void toonClips(ClassNode node) {
        MethodNode play = method(node, "playRemote", "(ILjava/lang/String;I)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ILOAD, 0));
        head.add(new VarInsnNode(ALOAD, 1));
        head.add(new MethodInsnNode(INVOKESTATIC, "com/hexhaki/piece/client/gear/NikaToon", "clip", "(ILjava/lang/String;)V", false));
        play.instructions.insert(head);
    }

    /** 3.2.6: the Bajrang Gun's giant fist (client/gear/NikaFist). */
    static final String BAJRANG_LAYER = "com/hexhaki/client/devilfruit/render/BajrangFistLayer";
    static final String NIKA_FIST = "com/hexhaki/piece/client/gear/NikaFist";

    /**
     * 3.2.6 (the owner, on the Bajrang Gun: "The fist is like should be ... more shaped like a fist and more rounder ... a big one. And it
     * should be coming first from above, and then the player slowly rotates it downwards and slams it down"): {@code BajrangFistLayer
     * .render} first tells {@code NikaFist.begin(player, partialTick)} whose fist it draws; its {@code fistCentre(phase, reach, radius,
     * flow)} becomes {@code NikaFist.centre} (the fist overhead, then swept over the front and down with the body's turn); its three
     * {@code RubberLimbMesh.emit} calls (the skin, the sleeve, the Haki coat) become {@code NikaFist.emitSkin} (the first: the arm with its
     * hand cube hidden, and the rounded fist) and {@code NikaFist.emitOther}. Exactly one centre and three emits, or the build fails.
     */
    static void nikaFist(ClassNode node) {
        MethodNode render = method(node, "render", "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
            + "Lnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 4));
        head.add(new VarInsnNode(FLOAD, 7));
        head.add(new MethodInsnNode(INVOKESTATIC, NIKA_FIST, "begin", "(Lnet/minecraft/client/player/AbstractClientPlayer;F)V", false));
        render.instructions.insert(head);
        int centres = 0;
        int emits = 0;
        for (AbstractInsnNode insn = render.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != INVOKESTATIC) {
                continue;
            }
            if (call.owner.equals(BAJRANG_LAYER) && call.name.equals("fistCentre")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, NIKA_FIST, "centre", call.desc, false);
                render.instructions.set(call, ours);
                insn = ours;
                centres++;
            } else if (call.owner.equals("com/hexhaki/client/devilfruit/render/RubberLimbMesh") && call.name.equals("emit")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, NIKA_FIST, emits == 0 ? "emitSkin" : "emitOther", call.desc, false);
                render.instructions.set(call, ours);
                insn = ours;
                emits++;
            }
        }
        if (centres != 1 || emits != 3) {
            throw new AssertionError("BajrangFistLayer.render is not as inspected: " + centres + " centres, " + emits + " emits");
        }
        bajrangBow(render);
    }

    /**
     * 3.4.9 (the owner, with a screenshot of the arm curving away under the player: "the arm, like after it hits the floor, it switches
     * direction and like it's bending to the left instead of to the right"): the original bows the stretched arm 0.14 of its length to
     * one side while it slams ({@code phase == SLAM ? -1 : 1}) and to the other side once it has hit (IMPACT, the retract, the fall), so
     * the whole arm swung over at the moment of impact. The other branch's {@code 1.0F} becomes the slam's {@code -1.0F}: it keeps its
     * bend. Exactly one, or the build fails.
     */
    static void bajrangBow(MethodNode render) {
        int found = 0;
        for (AbstractInsnNode insn = render.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC && field.name.equals("SLAM")
                && field.owner.equals("com/hexhaki/devilfruit/data/BajrangPhase") && nextReal(field) instanceof JumpInsnNode jump
                && jump.getOpcode() == IF_ACMPNE && nextReal(jump) instanceof LdcInsnNode minus && Float.valueOf(-1.0F).equals(minus.cst)
                && nextReal(jump.label) instanceof InsnNode one && one.getOpcode() == FCONST_1) {
                render.instructions.set(one, new LdcInsnNode(-1.0F));
                found++;
            }
        }
        if (found != 1) {
            throw new AssertionError("BajrangFistLayer.render's bow is not as inspected: " + found);
        }
    }

    static final String RUBBER_LIMB_LAYER = "com/hexhaki/client/devilfruit/render/RubberLimbLayer";
    static final String PISTOL_ARM = "com/hexhaki/piece/client/gear/NikaPistolArm";

    /**
     * 3.3.9 (Dawn Pistol, the owner: "Build as proposed"): {@code RubberLimbLayer.draw} first tells {@code NikaPistolArm.begin(stretch)}
     * which stretch it draws (its ninth local, the RubberLimbStates.Stretch parameter); its one {@code GearOutfitLayer.emitWithCuff} (the
     * skin) becomes {@code NikaPistolArm.emitSkin} and its two {@code RubberLimbMesh.emit} (the sleeve, the dawn fire) become {@code
     * NikaPistolArm.emitOther}: a Dawn Pistol's hand hidden in a ballooned Haki-black fist, anything else as before. Exactly one and two,
     * or the build fails.
     */
    static void pistolArm(ClassNode node) {
        String stretch = "Lcom/hexhaki/client/devilfruit/RubberLimbStates$Stretch;";
        MethodNode draw = null;
        for (MethodNode m : node.methods) {
            if (m.name.equals("draw") && m.desc.contains(stretch)) {
                if (draw != null) {
                    throw new AssertionError("RubberLimbLayer has more than one draw with a stretch");
                }
                draw = m;
            }
        }
        if (draw == null || (draw.access & ACC_STATIC) != 0) {
            throw new AssertionError("RubberLimbLayer.draw is not as inspected");
        }
        Type[] args = Type.getArgumentTypes(draw.desc);
        int slot = 1;
        int at = -1;
        for (Type arg : args) {
            if (arg.getDescriptor().equals(stretch)) {
                at = slot;
            }
            slot += arg.getSize();
        }
        if (at != 9) {
            throw new AssertionError("RubberLimbLayer.draw's stretch is not its ninth local: " + at);
        }
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, at));
        head.add(new MethodInsnNode(INVOKESTATIC, PISTOL_ARM, "begin", "(" + stretch + ")V", false));
        draw.instructions.insert(head);
        int skins = 0;
        int others = 0;
        for (AbstractInsnNode insn = draw.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != INVOKESTATIC) {
                continue;
            }
            if (call.owner.equals("com/hexhaki/client/devilfruit/render/GearOutfitLayer") && call.name.equals("emitWithCuff")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, PISTOL_ARM, "emitSkin", call.desc, false);
                draw.instructions.set(call, ours);
                insn = ours;
                skins++;
            } else if (call.owner.equals("com/hexhaki/client/devilfruit/render/RubberLimbMesh") && call.name.equals("emit")) {
                MethodInsnNode ours = new MethodInsnNode(INVOKESTATIC, PISTOL_ARM, "emitOther", call.desc, false);
                draw.instructions.set(call, ours);
                insn = ours;
                others++;
            }
        }
        if (skins != 1 || others != 2) {
            throw new AssertionError("RubberLimbLayer.draw is not as inspected: " + skins + " skins, " + others + " other passes");
        }
    }

    /** 3.2.3: which Nika moves work untransformed (server/NikaRules). */
    static final String NIKA_RULES = "com/hexhaki/piece/server/NikaRules";

    /**
     * 3.2.3 (the owner: "The stretchy moves like M1s and whatsoever, they should all be used in the normal like non-transformed state";
     * asked, "All stretch moves"): {@code GearServerController.onAction} refused every move but the transformation unless
     * {@code GearData.flag(player, "gear5")}: that check becomes {@code NikaRules.allowed(player, action)} (the {@code "gear5"} constant
     * replaced by the action, the call by ours). Gatling's tick, Giant's and Coil Spring's start and Coil Spring's let go checked it again:
     * there it becomes {@code NikaRules.stretched(player, "gear5")}. Exactly one check in each, or the build fails.
     */
    static void gearUntransformed(ClassNode node) {
        String flag = "(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Z";
        String data = "com/hexhaki/devilfruit/data/GearData";
        MethodNode action = method(node, "onAction", "(Lnet/minecraft/server/level/ServerPlayer;Lcom/hexhaki/network/GearAction;)V");
        int gates = 0;
        for (AbstractInsnNode insn = action.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && "gear5".equals(ldc.cst) && nextReal(ldc) instanceof MethodInsnNode call && call.owner.equals(data)
                && call.name.equals("flag") && call.desc.equals(flag)) {
                action.instructions.set(call, new MethodInsnNode(INVOKESTATIC, NIKA_RULES, "allowed",
                    "(Lnet/minecraft/server/level/ServerPlayer;Lcom/hexhaki/network/GearAction;)Z", false));
                action.instructions.set(ldc, new VarInsnNode(ALOAD, 1));
                gates++;
                break;
            }
        }
        for (String name : new String[] {"tickGatling", "beginGiant", "beginCoil", "releaseCoil"}) {
            MethodNode move = method(node, name, "(Lnet/minecraft/server/level/ServerPlayer;)V");
            for (AbstractInsnNode insn = move.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof LdcInsnNode ldc && "gear5".equals(ldc.cst) && nextReal(ldc) instanceof MethodInsnNode call && call.owner.equals(data)
                    && call.name.equals("flag") && call.desc.equals(flag)) {
                    move.instructions.set(call, new MethodInsnNode(INVOKESTATIC, NIKA_RULES, "stretched", flag, false));
                    gates++;
                    break;
                }
            }
        }
        if (gates != 5) {
            throw new AssertionError("Gear 5's checks are not the ones inspected: " + gates + " of 5");
        }
    }

    /** 2.7.1: the quick slots' letters ({@code Binds.DEFAULT_KEYS}, {@code HAKI_KEYS}) as keyDefaults sets the keys: M -> H, N -> V, H -> X. */
    static void slotLetters(ClassNode node) {
        MethodNode clinit = method(node, "<clinit>", "()V");
        Map<String, String> to = Map.of("M", "H", "N", "V", "H", "X");
        Map<String, Integer> found = new java.util.HashMap<>();
        for (AbstractInsnNode insn = clinit.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String text && to.containsKey(text)) {
                ldc.cst = to.get(text);
                found.merge(text, 1, Integer::sum);
            }
        }
        if (!found.equals(Map.of("M", 2, "N", 1, "H", 1))) {
            throw new AssertionError("Binds' slot letters are not the ones inspected: " + found);
        }
    }

    /**
     * 3.5.9 (the owner: the Haki Grip and the Galaxy Fist "Removed entirely", WiFi Haki a Swordsman's on a free quick slot, not L): the
     * head of {@code Binds.sync(player)} (every time the quick slots are sent) has {@code server/HakiQuick.tidy(player)} set them right
     * first (haki/QuickSlots). No branch, no frame.
     */
    static void tidySlots(ClassNode node) {
        MethodNode sync = method(node, "sync", "(Lnet/minecraft/server/level/ServerPlayer;)V");
        InsnList head = new InsnList();
        head.add(new VarInsnNode(ALOAD, 0));
        head.add(new MethodInsnNode(INVOKESTATIC, QUICK, "tidy", PLAYER_VOID, false));
        sync.instructions.insert(head);
    }

    /** RyouServer.afterArmament(player) before every return of HakiServerController.toggleArmament. */
    static void afterArmament(ClassNode node) {
        MethodNode method = method(node, AFTER_ARMAMENT[1], AFTER_ARMAMENT[2]);
        int hooks = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == RETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(ALOAD, 0));
                call.add(new MethodInsnNode(INVOKESTATIC, AFTER_ARMAMENT[3], AFTER_ARMAMENT[4], AFTER_ARMAMENT[2], false));
                method.instructions.insertBefore(insn, call);
                hooks++;
            }
        }
        if (hooks < 1) {
            throw new AssertionError("no return in " + node.name + "." + AFTER_ARMAMENT[1]);
        }
    }

    /** One {@link #INTS} row: the int pushed as a constant in the method becomes the new value (its own push); the count must match. */
    static void replaceInt(ClassNode node, Object[] f) {
        MethodNode method = method(node, (String) f[1], null);
        int from = (Integer) f[2];
        int to = (Integer) f[3];
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            boolean match = insn instanceof IntInsnNode push && (push.getOpcode() == BIPUSH || push.getOpcode() == SIPUSH) && push.operand == from
                || insn instanceof LdcInsnNode ldc && ldc.cst instanceof Integer value && value == from;
            if (match) {
                AbstractInsnNode replacement = to >= -1 && to <= 5 ? new InsnNode(ICONST_0 + to) : new LdcInsnNode(to);
                method.instructions.set(insn, replacement);
                insn = replacement;
                count++;
            }
        }
        if (count != (Integer) f[4]) {
            throw new AssertionError("expected " + f[4] + " int constants " + from + " in " + node.name + "." + f[1] + ", found " + count);
        }
    }

    /** One {@link #FLOATS} row: the float constant in the method becomes the new value; the count must match. */
    static void replaceFloat(ClassNode node, Object[] f) {
        MethodNode method = method(node, (String) f[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Float value && value.floatValue() == (Float) f[2]) {
                ldc.cst = f[3];
                count++;
            }
        }
        if (count != (Integer) f[4]) {
            throw new AssertionError("expected " + f[4] + " constants " + f[2] + " in " + node.name + "." + f[1] + ", found " + count);
        }
    }

    /** One {@link #DOUBLES} row: the double constant in the method becomes the new value (the count must match), and its named field. */
    static void replaceDouble(ClassNode node, Object[] f) {
        MethodNode method = method(node, (String) f[1], null);
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Double value && value.doubleValue() == (Double) f[2]) {
                ldc.cst = f[3];
                count++;
            }
        }
        if (count != (Integer) f[4]) {
            throw new AssertionError("expected " + f[4] + " constants " + f[2] + " in " + node.name + "." + f[1] + ", found " + count);
        }
        if (f[5] != null) {
            for (FieldNode field : node.fields) {
                if (field.name.equals(f[5])) {
                    field.value = f[3];
                    return;
                }
            }
            throw new AssertionError("missing " + node.name + "." + f[5]);
        }
    }

    /** One {@link #TEXTS} row: the string constant (or string-concatenation recipe) in any method becomes the new text. */
    static void replaceText(ClassNode node, Object[] text) {
        String from = unescape((String) text[1]);
        String to = unescape((String) text[2]);
        int count = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof LdcInsnNode ldc && from.equals(ldc.cst)) {
                    ldc.cst = to;
                    count++;
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        if (from.equals(indy.bsmArgs[i])) {
                            indy.bsmArgs[i] = to;
                            count++;
                        }
                    }
                }
            }
        }
        if (count != (Integer) text[3]) {
            throw new AssertionError("expected " + text[3] + " texts '" + from + "' in " + node.name + ", found " + count);
        }
    }

    /** The {@code \\u0001} of a concatenation recipe written in the tables above. */
    static String unescape(String text) {
        return text.replace("\\u0001", "\u0001");
    }

    /** Renames the one debug-table local {@code from} in {@code owner.method} to {@code to}. */
    static void renameLocal(ClassNode node, String methodName, String from, String to) {
        int renamed = 0;
        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName) && method.localVariables != null) {
                for (var local : method.localVariables) {
                    if (local.name.equals(from)) {
                        local.name = to;
                        renamed++;
                    }
                }
            }
        }
        if (renamed != 1) {
            throw new AssertionError("expected one local '" + from + "' in " + node.name + "." + methodName + ", found " + renamed);
        }
    }

    static void lightningPaths(ClassNode node) {
        trampoline(method(node, "generate", GENERATE), PATHS, "generate");
    }

    /** Replaces a static method's body with a straight call to {@code owner.name} with the same descriptor. */
    static void trampoline(MethodNode method, String owner, String name) {
        if ((method.access & ACC_STATIC) == 0) {
            throw new AssertionError("trampolines are for static methods: " + method.name);
        }
        InsnList body = new InsnList();
        int slot = 0;
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            body.add(new VarInsnNode(arg.getOpcode(ILOAD), slot));
            slot += arg.getSize();
        }
        body.add(new MethodInsnNode(INVOKESTATIC, owner, name, method.desc, false));
        body.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
        method.instructions = body;
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        method.maxLocals = slot;
    }

    static byte[] modsToml(byte[] bytes, String version) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        String patched = replaceOnce(text, "version=\"2.1.0\"", "version=\"" + version + "\"");
        patched = replaceOnce(patched, "displayName=\"HexHaki\"", "displayName=\"HexPiece\"");
        patched = replaceOnce(patched, "description='''High-end, multiplayer-visible Armament, Observation and Conqueror's Haki systems.'''",
            "description='''HexPiece: Haki, Devil Fruits, fighting styles, pirates and marines.'''");
        return patched.getBytes(StandardCharsets.UTF_8);
    }

    static byte[] manifest(byte[] bytes, String version) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return replaceOnce(text, "Implementation-Version: 2.1.0", "Implementation-Version: " + version).getBytes(StandardCharsets.UTF_8);
    }

    static String replaceOnce(String text, String from, String to) {
        int at = text.indexOf(from);
        if (at < 0 || text.indexOf(from, at + 1) >= 0) {
            throw new AssertionError("expected exactly one '" + from + "'");
        }
        return text.substring(0, at) + to + text.substring(at + from.length());
    }

    static List<String> widened() {
        return List.of(VFX_WIDENED);
    }
}
