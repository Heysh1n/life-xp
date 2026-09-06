package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.config.AttributeConfigNode;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис пересчёта атрибутов игрока на основе его уровня опыта.
 * Работает строго в O(1).
 * Кэширует уровень опыта игрока и срабатывает ТОЛЬКО при изменении experienceLevel.
 * Игнорирует изменения experienceProgress, защищая сервер от Watchdog Crash.
 */
public final class AttributeService {

    private static final String MOD_ID = "life_xp_challenge";

    /** Маппинг конфиг-ключ → ванильный Holder<Attribute> (строго 1.21.1). */
    private static final Map<String, Holder<Attribute>> ATTRIBUTE_MAP = new LinkedHashMap<>();

    /** Кэш уровня игрока (UUID -> experienceLevel) для O(1) проверки. */
    private static final Map<UUID, Integer> LAST_LEVEL_CACHE = new ConcurrentHashMap<>();

    static {
        ATTRIBUTE_MAP.put("max_health",               Attributes.MAX_HEALTH);
        ATTRIBUTE_MAP.put("movement_speed",           Attributes.MOVEMENT_SPEED);
        ATTRIBUTE_MAP.put("attack_damage",            Attributes.ATTACK_DAMAGE);
        ATTRIBUTE_MAP.put("attack_speed",             Attributes.ATTACK_SPEED);
        ATTRIBUTE_MAP.put("armor_toughness",          Attributes.ARMOR_TOUGHNESS);
        ATTRIBUTE_MAP.put("knockback_resistance",     Attributes.KNOCKBACK_RESISTANCE);
        ATTRIBUTE_MAP.put("block_interaction_range",  Attributes.BLOCK_INTERACTION_RANGE);
        ATTRIBUTE_MAP.put("entity_interaction_range", Attributes.ENTITY_INTERACTION_RANGE);
        ATTRIBUTE_MAP.put("block_break_speed",        Attributes.BLOCK_BREAK_SPEED);
        ATTRIBUTE_MAP.put("sneaking_speed",           Attributes.SNEAKING_SPEED);
        ATTRIBUTE_MAP.put("submerged_mining_speed",   Attributes.SUBMERGED_MINING_SPEED);
        ATTRIBUTE_MAP.put("sweeping_damage_ratio",    Attributes.SWEEPING_DAMAGE_RATIO);
        ATTRIBUTE_MAP.put("safe_fall_distance",       Attributes.SAFE_FALL_DISTANCE);
        ATTRIBUTE_MAP.put("oxygen_bonus",             Attributes.OXYGEN_BONUS);
        ATTRIBUTE_MAP.put("burning_time",             Attributes.BURNING_TIME);
    }

    private AttributeService() {}

    /** Очистить кэш для конкретного игрока (при дисконнекте/респавне). */
    public static void clearCache(UUID uuid) {
        if (uuid != null) {
            LAST_LEVEL_CACHE.remove(uuid);
        }
    }

    /** Очистить кэш для всех игроков (при смене конфига/пресета). */
    public static void clearAllCache() {
        LAST_LEVEL_CACHE.clear();
    }

    // ── Интерполяция (O(1)) ───────────────────────────────────────

    /**
     * Кусочно-линейная интерполяция с динамическим maxLevel (O(1)).
     */
    public static double interpolate(com.hs1n.lifeXp_challenge.config.AttributeConfigNode node, int level, int maxLevel) {
        if (node.getFormulaMode() == com.hs1n.lifeXp_challenge.config.FormulaMode.FORMULA) {
            if (level <= 0) return node.getFormulaBase();
            double base = node.getFormulaBase();
            double multiplier = node.getFormulaMultiplier();
            double exponent = node.getFormulaExponent();
            return base + Math.pow(level * multiplier, exponent);
        }

        if (maxLevel <= 0) maxLevel = 1;
        if (level <= 0) return node.getStartValue();
        if (level >= maxLevel) return node.getEndValue();

        int half = maxLevel / 2;
        if (half <= 0) half = 1;

        if (level <= half) {
            double t = (double) level / half;
            return node.getStartValue() + (node.getMidValue() - node.getStartValue()) * t;
        } else {
            double t = (double) (level - half) / (maxLevel - half);
            return node.getMidValue() + (node.getEndValue() - node.getMidValue()) * t;
        }
    }

    /**
     * Кусочно-линейная интерполяция для произвольных значений (O(1)).
     */
    public static double interpolate(double startVal, double midVal, double endVal, int level, int maxLevel) {
        if (maxLevel <= 0) maxLevel = 1;
        if (level <= 0) return startVal;
        if (level >= maxLevel) return endVal;

        int half = maxLevel / 2;
        if (half <= 0) half = 1;

        if (level <= half) {
            double t = (double) level / half;
            return startVal + (midVal - startVal) * t;
        } else {
            double t = (double) (level - half) / (maxLevel - half);
            return midVal + (endVal - midVal) * t;
        }
    }

    /** ResourceLocation для модификатора конкретного атрибута. */
    private static ResourceLocation modifierId(String attrKey) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "xp_scaling_" + attrKey);
    }

    // ── Применение (O(1)) ─────────────────────────────────────────

    /**
     * Стандартный пересчёт атрибутов с проверкой кэша.
     * Срабатывает ТОЛЬКО если player.experienceLevel действительно изменился.
     */
    public static void recalculate(Player player) {
        recalculate(player, false);
    }

    /**
     * Пересчитать и применить все модификаторы для игрока.
     * @param force если true, пересчитывает даже если уровень совпадает с кэшем (например, смена конфига или респавн).
     */
    public static void recalculate(Player player, boolean force) {
        if (player == null) return;
        int level = player.experienceLevel;
        UUID uuid = player.getUUID();

        if (!force) {
            Integer cachedLevel = LAST_LEVEL_CACHE.get(uuid);
            if (cachedLevel != null && cachedLevel == level) {
                // Уровень не изменился — выходим в O(1), игнорируя опыт-прогресс и не спамя пересчёт
                return;
            }
        }
        LAST_LEVEL_CACHE.put(uuid, level);

        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int maxLevel = config.getMaxLevel();

        for (Map.Entry<String, Holder<Attribute>> entry : ATTRIBUTE_MAP.entrySet()) {
            String key                   = entry.getKey();
            Holder<Attribute> holder      = entry.getValue();
            AttributeConfigNode node     = config.getNode(key);
            if (node == null) continue;

            AttributeInstance instance = player.getAttribute(holder);
            if (instance == null) continue;

            double value        = interpolate(node, level, maxLevel);
            ResourceLocation id = modifierId(key);

            // Удаляем старый, ставим новый (zero-tick обновление)
            instance.removeModifier(id);
            if (value != 0.0) {
                instance.addPermanentModifier(new AttributeModifier(
                        id, value, AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        // Принудительно срезаем текущее здоровье до нового максимума (zero-tick fix)
        double newMaxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        if (player.getHealth() > newMaxHealth) {
            player.setHealth((float) newMaxHealth);
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            MilestoneService.checkMilestones(serverPlayer);
        }
    }
}
