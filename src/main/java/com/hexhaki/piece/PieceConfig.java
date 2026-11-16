package com.hexhaki.piece;

import com.hexhaki.piece.sword.SwordRules;
import java.util.function.Supplier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 2.1.4: HexPiece's server config ({@code serverconfig/hexpiece-server.toml} in each world): the Swordsman's numbers (defaults in
 * {@link SwordRules}). Read through {@link #get}, which falls back to the default before the world's config is loaded.
 */
public final class PieceConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Supplier<Double> CHARGE_MAX;
    public static final Supplier<Double> CUT_DAMAGE_MIN;
    public static final Supplier<Double> CUT_DAMAGE_MAX;
    public static final Supplier<Double> CUT_REACH_MIN;
    public static final Supplier<Double> CUT_REACH_MAX;
    public static final Supplier<Double> CUT_COOLDOWN;
    public static final Supplier<Double> SLASH_HEIGHT_MIN;
    public static final Supplier<Double> SLASH_HEIGHT_MAX;
    public static final Supplier<Double> SLASH_RANGE_MIN;
    public static final Supplier<Double> SLASH_RANGE_MAX;
    public static final Supplier<Double> SLASH_DAMAGE_MIN;
    public static final Supplier<Double> SLASH_DAMAGE_MAX;
    public static final Supplier<Double> SLASH_COST_MIN;
    public static final Supplier<Double> SLASH_COST_MAX;
    public static final Supplier<Double> SLASH_COOLDOWN;
    public static final Supplier<Double> CUT_BREAKS;
    public static final Supplier<Double> SHEATHE_DELAY;
    public static final Supplier<Boolean> TERRAIN;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("swordsman");
        CHARGE_MAX = b.comment("Ticks of holding the right button that make a full charge (20 = 1 second).")
            .defineInRange("chargeMaxTicks", SwordRules.CHARGE_MAX, 10, 200);
        CUT_DAMAGE_MIN = b.comment("The charged cut without Haki: damage at the least charge.").defineInRange("cutDamageMin",
            SwordRules.CUT_DAMAGE_MIN, 0, 200);
        CUT_DAMAGE_MAX = b.comment("... and at the full charge.").defineInRange("cutDamageMax", SwordRules.CUT_DAMAGE_MAX, 0, 200);
        CUT_REACH_MIN = b.comment("How far past the blade's tip the charged cut reaches (blocks), least charge.")
            .defineInRange("cutReachMin", SwordRules.CUT_REACH_MIN, 0, 8);
        CUT_REACH_MAX = b.comment("... full charge.").defineInRange("cutReachMax", SwordRules.CUT_REACH_MAX, 0, 8);
        CUT_COOLDOWN = b.comment("Ticks before the next charged cut.").defineInRange("cutCooldown", SwordRules.CUT_COOLDOWN, 0, 1200);
        SLASH_HEIGHT_MIN = b.comment("The Haki slash: its height (blocks) at the least charge.").defineInRange("slashHeightMin",
            SwordRules.SLASH_HEIGHT_MIN, 0.5, 32);
        SLASH_HEIGHT_MAX = b.comment("... at the full charge.").defineInRange("slashHeightMax", SwordRules.SLASH_HEIGHT_MAX, 0.5, 32);
        SLASH_RANGE_MIN = b.comment("How far it travels (blocks), least charge.").defineInRange("slashRangeMin", SwordRules.SLASH_RANGE_MIN,
            2, 128);
        SLASH_RANGE_MAX = b.comment("... full charge.").defineInRange("slashRangeMax", SwordRules.SLASH_RANGE_MAX, 2, 128);
        SLASH_DAMAGE_MIN = b.comment("Its damage, least charge.").defineInRange("slashDamageMin", SwordRules.SLASH_DAMAGE_MIN, 0, 400);
        SLASH_DAMAGE_MAX = b.comment("... full charge.").defineInRange("slashDamageMax", SwordRules.SLASH_DAMAGE_MAX, 0, 400);
        SLASH_COST_MIN = b.comment("The Haki energy it costs, least charge.").defineInRange("slashCostMin", SwordRules.SLASH_COST_MIN, 0,
            1000);
        SLASH_COST_MAX = b.comment("... full charge.").defineInRange("slashCostMax", SwordRules.SLASH_COST_MAX, 0, 1000);
        SLASH_COOLDOWN = b.comment("Ticks before the next Haki slash.").defineInRange("slashCooldown", SwordRules.SLASH_COOLDOWN, 0, 2400);
        CUT_BREAKS = b.comment("Blocks one slash may cut a tick (each looks at six times as many).").defineInRange("cutBlocksPerTick",
            SwordRules.CUT_BREAKS_PER_TICK, 0, 512);
        SHEATHE_DELAY = b.comment("Ticks without a cut before the sword is sheathed.").defineInRange("sheatheDelay",
            SwordRules.SHEATHE_DELAY, 2, 200);
        TERRAIN = b.comment("Whether the Haki slash cuts terrain at all (pirates also follow the mobGriefing rule).")
            .define("slashCutsTerrain", true);
        b.pop();
        SPEC = b.build();
    }

    private PieceConfig() {
    }

    static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "hexpiece-server.toml");
    }

    /** A config value, or its default before the world's config is loaded. */
    public static double get(Supplier<Double> value, double fallback) {
        try {
            Double v = value.get();
            return v == null ? fallback : v;
        } catch (RuntimeException notLoaded) {
            return fallback;
        }
    }

    public static boolean get(Supplier<Boolean> value, boolean fallback) {
        try {
            Boolean v = value.get();
            return v == null ? fallback : v;
        } catch (RuntimeException notLoaded) {
            return fallback;
        }
    }

    public static int chargeMax() {
        return (int) Math.round(get(CHARGE_MAX, SwordRules.CHARGE_MAX));
    }
}
