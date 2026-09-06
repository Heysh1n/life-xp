package com.hs1n.lifeXp_challenge.client.config;

import com.hs1n.lifeXp_challenge.config.AttributeConfigNode;
import com.hs1n.lifeXp_challenge.config.DifficultyDirection;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Экран конфигурации YACL v3.
 * Категория «Пресеты» — 4 кнопки быстрых шаблонов.
 * Категория «Общие» — maxLevel, координаты смерти, налог опыта при смерти + настройки тумана.
 * Категория «Атрибуты» — группы по 3 поля ввода (start / mid / end).
 * Весь текст через Component.translatable() для мультиязычности.
 */
public class LifeXpConfigScreen {

    // ── Локализованные ключи атрибутов ─────────────────────────
    private static final Map<String, String> ATTR_TRANSLATION_KEYS = new LinkedHashMap<>();

    static {
        ATTR_TRANSLATION_KEYS.put("max_health",               "lifexp.attr.max_health");
        ATTR_TRANSLATION_KEYS.put("movement_speed",           "lifexp.attr.movement_speed");
        ATTR_TRANSLATION_KEYS.put("attack_damage",            "lifexp.attr.attack_damage");
        ATTR_TRANSLATION_KEYS.put("attack_speed",             "lifexp.attr.attack_speed");
        ATTR_TRANSLATION_KEYS.put("armor_toughness",          "lifexp.attr.armor_toughness");
        ATTR_TRANSLATION_KEYS.put("knockback_resistance",     "lifexp.attr.knockback_resistance");
        ATTR_TRANSLATION_KEYS.put("block_interaction_range",  "lifexp.attr.block_interaction_range");
        ATTR_TRANSLATION_KEYS.put("entity_interaction_range", "lifexp.attr.entity_interaction_range");
        ATTR_TRANSLATION_KEYS.put("block_break_speed",        "lifexp.attr.block_break_speed");
        ATTR_TRANSLATION_KEYS.put("sneaking_speed",           "lifexp.attr.sneaking_speed");
        ATTR_TRANSLATION_KEYS.put("submerged_mining_speed",   "lifexp.attr.submerged_mining_speed");
        ATTR_TRANSLATION_KEYS.put("sweeping_damage_ratio",    "lifexp.attr.sweeping_damage_ratio");
        ATTR_TRANSLATION_KEYS.put("safe_fall_distance",       "lifexp.attr.safe_fall_distance");
        ATTR_TRANSLATION_KEYS.put("oxygen_bonus",             "lifexp.attr.oxygen_bonus");
        ATTR_TRANSLATION_KEYS.put("burning_time",             "lifexp.attr.burning_time");
    }

    public static Screen create(Screen parent) {
        LifeXpConfig config   = LifeXpConfig.INSTANCE;
        LifeXpConfig defaults = new LifeXpConfig();

        var builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("lifexp.config.title"))
                .save(LifeXpConfig::save);

        // ════════════════════════════════════════════════════════
        //  Категория 0 — Пресеты
        // ════════════════════════════════════════════════════════
        builder.category(ConfigCategory.createBuilder()
                .name(Component.translatable("lifexp.config.category.presets"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("lifexp.config.presets.group"))
                        .description(OptionDescription.of(
                                Component.translatable("lifexp.config.presets.description")))

                        // ── Пресет 1: LIFE-XP CORE ──
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("lifexp.config.preset.core"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.preset.core.desc")))
                                .action((screen, opt) -> applyPresetAndReopen(screen, parent, LifeXpPresets::applyCore))
                                .build())

                        // ── Пресет 2: Vanilla+ ──
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("lifexp.config.preset.vanilla_plus"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.preset.vanilla_plus.desc")))
                                .action((screen, opt) -> applyPresetAndReopen(screen, parent, LifeXpPresets::applyVanillaPlus))
                                .build())

                        // ── Пресет 3: BabyMode ──
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("lifexp.config.preset.baby_mode"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.preset.baby_mode.desc")))
                                .action((screen, opt) -> applyPresetAndReopen(screen, parent, LifeXpPresets::applyBabyMode))
                                .build())

                        // ── Пресет 4: Real Hardcore ──
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("lifexp.config.preset.real_hardcore"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.preset.real_hardcore.desc")))
                                .action((screen, opt) -> applyPresetAndReopen(screen, parent, LifeXpPresets::applyRealHardcore))
                                .build())

                        .build())
                .build());

        // ════════════════════════════════════════════════════════
        //  Категория 1 — Общие + Визуальные эффекты
        // ════════════════════════════════════════════════════════
        builder.category(ConfigCategory.createBuilder()
                .name(Component.translatable("lifexp.config.category.general"))

                // ── Прогрессия ──
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("lifexp.config.general.progression"))
                        .description(OptionDescription.of(
                                Component.translatable("lifexp.config.general.progression.desc")))
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("lifexp.config.general.max_level"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.max_level.desc")))
                                .binding(defaults.getMaxLevel(), config::getMaxLevel, config::setMaxLevel)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("lifexp.config.general.death_xp_tax"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.death_xp_tax.desc")))
                                .binding(defaults.getDeathXpTax(), config::getDeathXpTax, config::setDeathXpTax)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 1.0).step(0.05))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("lifexp.config.general.death_coords"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.death_coords.desc")))
                                .binding(defaults.isShowDeathCoordinates(), config::isShowDeathCoordinates, config::setShowDeathCoordinates)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())
                        .build())

                // ── Туман (Visuals) ──
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("lifexp.config.general.fog"))
                        .description(OptionDescription.of(
                                Component.translatable("lifexp.config.general.fog.desc")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("lifexp.config.general.fog.enable"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.fog.enable.desc")))
                                .binding(defaults.isEnableCustomFog(), config::isEnableCustomFog, config::setEnableCustomFog)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("lifexp.config.general.fog.start"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.fog.start.desc")))
                                .binding(defaults.getFogStartDistance(), config::getFogStartDistance, config::setFogStartDistance)
                                .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("lifexp.config.general.fog.mid"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.fog.mid.desc")))
                                .binding(defaults.getFogMidDistance(), config::getFogMidDistance, config::setFogMidDistance)
                                .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("lifexp.config.general.fog.end"))
                                .description(OptionDescription.of(
                                        Component.translatable("lifexp.config.general.fog.end.desc")))
                                .binding(defaults.getFogEndDistance(), config::getFogEndDistance, config::setFogEndDistance)
                                .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                                .build())
                        .build())
                .build());

        // ════════════════════════════════════════════════════════
        //  Категория 2 — Атрибуты
        // ════════════════════════════════════════════════════════
        var attrCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("lifexp.config.category.attributes"));

        for (Map.Entry<String, AttributeConfigNode> entry : config.getNodes().entrySet()) {
            String key               = entry.getKey();
            AttributeConfigNode node = entry.getValue();
            AttributeConfigNode def  = defaults.getNode(key);
            String translationKey    = ATTR_TRANSLATION_KEYS.getOrDefault(key, "lifexp.attr." + key);

            boolean isInverse = node.getDirection() == DifficultyDirection.INVERSE;

            var group = OptionGroup.createBuilder()
                    .name(Component.translatable(translationKey))
                    .description(OptionDescription.of(
                            Component.translatable("lifexp.config.attr.desc",
                                    Component.translatable(translationKey),
                                    Component.translatable(isInverse
                                            ? "lifexp.config.direction.inverse"
                                            : "lifexp.config.direction.direct"))));

            // ── startValue ──
            group.option(Option.<Double>createBuilder()
                    .name(Component.translatable("lifexp.config.attr.start"))
                    .description(OptionDescription.of(
                            Component.translatable("lifexp.config.attr.start.desc")))
                    .binding(def.getStartValue(), node::getStartValue, node::setStartValue)
                    .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                    .build());

            // ── midValue ──
            group.option(Option.<Double>createBuilder()
                    .name(Component.translatable("lifexp.config.attr.mid"))
                    .description(OptionDescription.of(
                            Component.translatable("lifexp.config.attr.mid.desc")))
                    .binding(def.getMidValue(), node::getMidValue, node::setMidValue)
                    .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                    .build());

            // ── endValue ──
            group.option(Option.<Double>createBuilder()
                    .name(Component.translatable("lifexp.config.attr.end"))
                    .description(OptionDescription.of(
                            Component.translatable("lifexp.config.attr.end.desc")))
                    .binding(def.getEndValue(), node::getEndValue, node::setEndValue)
                    .controller(opt -> DoubleFieldControllerBuilder.create(opt))
                    .build());

            attrCategory.group(group.build());
        }

        builder.category(attrCategory.build());

        return builder.build().generateScreen(parent);
    }

    // ════════════════════════════════════════════════════════════
    //  Утилита переоткрытия экрана
    // ════════════════════════════════════════════════════════════

    private static void applyPresetAndReopen(Screen currentScreen, Screen parent, Runnable presetApplier) {
        presetApplier.run();
        LifeXpConfig.save();
        Minecraft.getInstance().setScreen(create(parent));
    }

}
