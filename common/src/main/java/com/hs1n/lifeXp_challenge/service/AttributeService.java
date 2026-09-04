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

/**
 * Сервис пересчёта атрибутов игрока на основе его уровня опыта.
 * Использует кусочно-линейную интерполяцию и операцию ADD_VALUE.
 * Никогда не изменяет BaseValue и SCALE.
 */
public final class AttributeService {

    private static final String MOD_ID = "life_xp_challenge";

    /** Маппинг конфиг-ключ → ванильный Holder<Attribute> (строго 1.21.1). */
    private static final Map<String, Holder<Attribute>> ATTRIBUTE_MAP = new LinkedHashMap<>();

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

    // ── Интерполяция ───────────────────────────────────────────

    /**
     * Кусочно-линейная интерполяция с динамическим maxLevel.
     * <pre>
     *   level ≤ 0          → startValue
     *   level = maxLevel/2  → midValue
     *   level ≥ maxLevel    → endValue
     * </pre>
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
     * Кусочно-линейная интерполяция для произвольных значений (для тумана и т.д.).
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

    // ── Применение ─────────────────────────────────────────────

    /**
     * Пересчитать и применить все модификаторы для игрока.
     * Вызывается при любом изменении опыта, респавне, входе на сервер.
     */
    public static void recalculate(Player player) {
        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int level    = player.experienceLevel;
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
