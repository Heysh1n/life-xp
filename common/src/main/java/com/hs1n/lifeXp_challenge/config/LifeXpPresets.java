package com.hs1n.lifeXp_challenge.config;

/**
 * Общие пресеты конфигурации, доступные и из клиентского GUI, и из серверных команд.
 * Извлечено из LifeXpConfigScreen для устранения дублирования кода.
 */
public final class LifeXpPresets {

    /** Имена пресетов для автодополнения команд. */
    public static final String[] PRESET_NAMES = {"core", "vanilla_plus", "baby_mode", "real_hardcore"};

    private LifeXpPresets() {}

    /**
     * Применить пресет по имени.
     * @return true если пресет найден и применён, false если имя неизвестно.
     */
    public static boolean apply(String name) {
        return switch (name) {
            case "core"           -> { applyCore();          yield true; }
            case "vanilla_plus"   -> { applyVanillaPlus();   yield true; }
            case "baby_mode"      -> { applyBabyMode();      yield true; }
            case "real_hardcore"  -> { applyRealHardcore();   yield true; }
            default -> false;
        };
    }

    // ── Хелпер для установки значений нода ─────────────────────

    private static void set(LifeXpConfig cfg, String key, double start, double mid, double end) {
        AttributeConfigNode node = cfg.getNode(key);
        if (node == null) return;
        node.setStartValue(start);
        node.setMidValue(mid);
        node.setEndValue(end);
    }

    // ════════════════════════════════════════════════════════════
    //  Пресет 1: LIFE-XP CORE — хардкорная прогрессия
    // ════════════════════════════════════════════════════════════

    public static void applyCore() {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        cfg.setMaxLevel(1000);
        cfg.setDeathXpTax(0.5);

        cfg.setEnableCustomFog(true);
        cfg.setFogStartDistance(4.0);
        cfg.setFogMidDistance(40.0);
        cfg.setFogEndDistance(200.0);

        set(cfg, "max_health",              -19.0,  -8.0,   12.0);
        set(cfg, "movement_speed",          -0.02,   0.0,    0.02);
        set(cfg, "attack_damage",            0.0,    1.5,    4.0);
        set(cfg, "attack_speed",            -0.5,    0.0,    1.5);
        set(cfg, "armor_toughness",          0.0,    3.0,    6.0);
        set(cfg, "knockback_resistance",     0.0,    0.2,    0.5);
        set(cfg, "block_interaction_range",  0.0,    0.5,    1.5);
        set(cfg, "entity_interaction_range", 0.0,    0.5,    1.5);
        set(cfg, "block_break_speed",       -0.1,    0.1,    0.4);
        set(cfg, "sneaking_speed",           0.0,    0.05,   0.15);
        set(cfg, "submerged_mining_speed",   0.0,    0.5,    2.0);
        set(cfg, "sweeping_damage_ratio",    0.0,    0.15,   0.4);
        set(cfg, "safe_fall_distance",       0.0,    1.0,    3.0);
        set(cfg, "oxygen_bonus",             2.0,    0.0,   -1.0);
        set(cfg, "burning_time",            -0.5,    0.0,    1.5);
    }

    // ════════════════════════════════════════════════════════════
    //  Пресет 2: Vanilla+ (Half of LXP Core)
    // ════════════════════════════════════════════════════════════

    public static void applyVanillaPlus() {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        cfg.setMaxLevel(500);
        cfg.setDeathXpTax(0.5);

        cfg.setEnableCustomFog(false);
        cfg.setFogStartDistance(5.0);
        cfg.setFogMidDistance(60.0);
        cfg.setFogEndDistance(200.0);

        set(cfg, "max_health",              -14.0,  -6.0,    4.0);
        set(cfg, "movement_speed",           0.0,    0.005,  0.015);
        set(cfg, "attack_damage",            0.0,    1.0,    2.5);
        set(cfg, "attack_speed",             0.0,    0.3,    0.8);
        set(cfg, "armor_toughness",          0.0,    2.0,    4.0);
        set(cfg, "knockback_resistance",     0.0,    0.1,    0.25);
        set(cfg, "block_interaction_range",  0.0,    0.3,    0.8);
        set(cfg, "entity_interaction_range", 0.0,    0.3,    0.8);
        set(cfg, "block_break_speed",        0.0,    0.1,    0.3);
        set(cfg, "sneaking_speed",           0.0,    0.03,   0.1);
        set(cfg, "submerged_mining_speed",   0.0,    0.5,    1.5);
        set(cfg, "sweeping_damage_ratio",    0.0,    0.1,    0.3);
        set(cfg, "safe_fall_distance",       0.0,    0.5,    1.5);
        set(cfg, "oxygen_bonus",             0.0,    0.0,    0.0);
        set(cfg, "burning_time",             0.0,    0.0,    0.0);
    }

    // ════════════════════════════════════════════════════════════
    //  Пресет 3: BabyMode — для новичков
    // ════════════════════════════════════════════════════════════

    public static void applyBabyMode() {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        cfg.setMaxLevel(200);
        cfg.setDeathXpTax(0.8);

        cfg.setEnableCustomFog(false);
        cfg.setFogStartDistance(5.0);
        cfg.setFogMidDistance(60.0);
        cfg.setFogEndDistance(200.0);

        set(cfg, "max_health",               0.0,   20.0,   40.0);
        set(cfg, "movement_speed",           0.0,    0.05,   0.1);
        set(cfg, "attack_damage",            2.0,    5.0,   10.0);
        set(cfg, "attack_speed",             0.5,    1.5,    3.0);
        set(cfg, "armor_toughness",          2.0,    6.0,   12.0);
        set(cfg, "knockback_resistance",     0.2,    0.5,    0.8);
        set(cfg, "block_interaction_range",  0.5,    1.5,    3.0);
        set(cfg, "entity_interaction_range", 0.5,    1.5,    3.0);
        set(cfg, "block_break_speed",        0.2,    0.5,    1.0);
        set(cfg, "sneaking_speed",           0.05,   0.15,   0.3);
        set(cfg, "submerged_mining_speed",   0.5,    2.0,    4.0);
        set(cfg, "sweeping_damage_ratio",    0.1,    0.3,    0.6);
        set(cfg, "safe_fall_distance",       1.0,    3.0,    6.0);
        set(cfg, "oxygen_bonus",             1.0,    2.0,    3.0);
        set(cfg, "burning_time",            -0.3,   -0.5,   -0.7);
    }

    // ════════════════════════════════════════════════════════════
    //  Пресет 4: Real Hardcore (Пукан в огне)
    // ════════════════════════════════════════════════════════════

    public static void applyRealHardcore() {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        cfg.setMaxLevel(10000);
        cfg.setDeathXpTax(0.2);

        cfg.setEnableCustomFog(true);
        cfg.setFogStartDistance(3.0);
        cfg.setFogMidDistance(20.0);
        cfg.setFogEndDistance(150.0);

        set(cfg, "max_health",              -19.0,  -10.0,   0.0);
        set(cfg, "movement_speed",          -0.03,  -0.01,   0.0);
        set(cfg, "attack_damage",           -0.5,    0.0,    1.0);
        set(cfg, "attack_speed",            -1.0,   -0.3,    0.0);
        set(cfg, "armor_toughness",          0.0,    1.0,    3.0);
        set(cfg, "knockback_resistance",     0.0,    0.05,   0.15);
        set(cfg, "block_interaction_range", -0.5,    0.0,    0.5);
        set(cfg, "entity_interaction_range",-0.5,    0.0,    0.5);
        set(cfg, "block_break_speed",       -0.2,   -0.05,   0.1);
        set(cfg, "sneaking_speed",          -0.05,   0.0,    0.05);
        set(cfg, "submerged_mining_speed",  -0.5,    0.0,    0.5);
        set(cfg, "sweeping_damage_ratio",    0.0,    0.05,   0.15);
        set(cfg, "safe_fall_distance",      -1.0,    0.0,    1.0);
        set(cfg, "oxygen_bonus",            -1.0,   -0.5,    0.0);
        set(cfg, "burning_time",             2.0,    1.0,    0.0);
    }
}
