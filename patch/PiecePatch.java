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
import jdk.internal.org.objectweb.asm.tree.MethodNode;
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
    static final String HAND = "com/hexhaki/client/render/HakiHandLayer";
    static final String RYOU_HANDS = "com/hexhaki/piece/client/haki/RyouHands";
    static final String RYOU_FX = "com/hexhaki/piece/client/haki/RyouFx";
    static final String RYOU_SERVER = "com/hexhaki/piece/server/RyouServer";
    static final String CONTROLLER = "com/hexhaki/gameplay/HakiServerController";
    static final String CONQUEROR = "com/hexhaki/piece/server/ConquerorRelease";
    static final String GALAXY = "com/hexhaki/piece/server/GalaxyFist";
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
        // 2.3.7 (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU haki. ONLY for iron fist."):
        // the Galaxy Fist's start (its "convergence") goes through HexPiece, which lets only an Iron Fist fighter with Ryou up throw it
        {CONTROLLER, "onAction", CONTROLLER, "beginConvergence", "(Lnet/minecraft/server/level/ServerPlayer;)V", GALAXY, "begin", 1},
        // 2.4.5 (the owner: "Conqueror Haki full release lags a ton when releasing the full charge"): the release's range lightning goes
        // through HexPiece, which leaves out the supreme release's second pass (the same bolts again from a second seed, 7 ticks
        // later: half the release's 3D lightning, 80,000 quads a frame at its height)
        {VFX, "conquerorRelease", VFX, "addConquerorRangeLightning", "(Lcom/lowdragmc/photon/client/fx/FX;FIJZI)V", STORM, "range", 2},
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
    };
    /** 2.1.3: methods widened to public (bodies untouched) so HexPiece can call the originals: class, names. */
    static final String[][] WIDENED = {{HAND, "renderArmAura", "renderFirstPerson"}, {COMMANDS, "reply"}, {IMPACT, "drawFlash"},
        {CONTROLLER, "beginConvergence", "applyConqueror", "forceMaxConquerorStun", "isConquerorCombatTarget"}};
    /**
     * 2.2.5: int constants: class, method, from, to, how many. The Conqueror's supreme (JoyBoy) release asked for mastery 1000: any
     * mastery now ({@link #CALL_REDIRECTS}: no JoyBoy switch either). 2.3.7: the Galaxy Fist's Armament 520.
     */
    static final Object[][] INTS = {
        {CONTROLLER, "releaseConqueror", 1000, 0, 1},
        // 2.3.7: the Galaxy Fist asked for Armament mastery 520 (its "Haki Projection" lock): Ryou up is what opens it now
        {CONTROLLER, "beginConvergence", 520, 0, 2},
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
            "[M HOLD, RELEASE TO FIRE] Wind a Haki punch and let go. A tap throws a short close-range burst; a full hold throws a long, wide pressure cylinder that detonates where it lands. Length, width, damage, cost and cooldown all scale continuously with how long you held it, with Armament mastery, and with whether you are bare, coated, or under Ryou.", 1},
        {"com/hexhaki/data/HakiTechnique", "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Advanced tiers raise the launch height, the damage and the size of the blast.",
            "[K] Midgame cinematic finisher. Hold K on a target in your sights: the wind hauls them in onto a chambered fist, an uppercut launches them straight up, you appear above them mid-flight and hammer them back into the floor with both arms. Most of the damage is in the landing, not the launch, and the crater throws everything else clear. Physical/Haki/Ryou tiers raise the launch height, the damage and the size of the blast.", 1},
        {"com/hexhaki/data/HakiTechnique", "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Advanced Haki its landing scatters grounded Haki strikes around the impact.",
            "Passive milestone for Haki Projection. A full charge reaches roughly 30% further and wider, and past 85% under Ryou its landing scatters grounded Haki strikes around the impact.", 1},
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
        return writer.toByteArray();
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
