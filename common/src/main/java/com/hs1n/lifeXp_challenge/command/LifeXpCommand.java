package com.hs1n.lifeXp_challenge.command;

import com.hs1n.lifeXp_challenge.config.AttributeConfigNode;
import com.hs1n.lifeXp_challenge.config.DifficultyDirection;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class LifeXpCommand {

    private static final String[] ATTR_KEYS = {
            "max_health", "movement_speed", "attack_damage", "attack_speed",
            "armor_toughness", "knockback_resistance", "block_interaction_range",
            "entity_interaction_range", "block_break_speed", "sneaking_speed",
            "submerged_mining_speed", "sweeping_damage_ratio", "safe_fall_distance",
            "oxygen_bonus", "burning_time"
    };

    private LifeXpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lifexp")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("reload")
                        .executes(LifeXpCommand::executeReload))

                .then(Commands.literal("status")
                        .executes(LifeXpCommand::executeStatus))

                .then(Commands.literal("get")
                        .then(Commands.literal("maxLevel")
                                .executes(ctx -> executeGetSimple(ctx, "maxLevel",
                                        String.valueOf(LifeXpConfig.INSTANCE.getMaxLevel()))))
                        .then(Commands.literal("xpShieldEnable")
                                .executes(ctx -> executeGetSimple(ctx, "xpShieldEnable", String.valueOf(LifeXpConfig.INSTANCE.isEnableXpShield()))))
                        .then(Commands.literal("xpShieldPoints")
                                .executes(ctx -> executeGetSimple(ctx, "xpShieldPoints", String.valueOf(LifeXpConfig.INSTANCE.getXpShieldPointsPerDamage()))))
                        .then(Commands.literal("xpShieldEnable")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            LifeXpConfig.INSTANCE.setEnableXpShield(BoolArgumentType.getBool(ctx, "value"));
                                            return saveAndNotify(ctx, "xpShieldEnable", String.valueOf(LifeXpConfig.INSTANCE.isEnableXpShield()));
                                        })))
                        .then(Commands.literal("xpShieldPoints")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            LifeXpConfig.INSTANCE.setXpShieldPointsPerDamage(IntegerArgumentType.getInteger(ctx, "value"));
                                            return saveAndNotify(ctx, "xpShieldPoints", String.valueOf(LifeXpConfig.INSTANCE.getXpShieldPointsPerDamage()));
                                        })))
                        .then(Commands.literal("deathXpTax")
                                .executes(ctx -> executeGetSimple(ctx, "deathXpTax",
                                        String.format("%.4f", LifeXpConfig.INSTANCE.getDeathXpTax()))))
                        .then(Commands.literal("deathCoords")
                                .executes(ctx -> executeGetSimple(ctx, "deathCoords",
                                        String.valueOf(LifeXpConfig.INSTANCE.isShowDeathCoordinates()))))
                        .then(Commands.literal("fog")
                                .then(Commands.literal("enable")
                                        .executes(ctx -> executeGetSimple(ctx, "fog.enable",
                                                String.valueOf(LifeXpConfig.INSTANCE.isEnableCustomFog()))))
                                .then(Commands.literal("start")
                                        .executes(ctx -> executeGetSimple(ctx, "fog.start",
                                                String.format("%.2f", LifeXpConfig.INSTANCE.getFogStartDistance()))))
                                .then(Commands.literal("mid")
                                        .executes(ctx -> executeGetSimple(ctx, "fog.mid",
                                                String.format("%.2f", LifeXpConfig.INSTANCE.getFogMidDistance()))))
                                .then(Commands.literal("end")
                                        .executes(ctx -> executeGetSimple(ctx, "fog.end",
                                                String.format("%.2f", LifeXpConfig.INSTANCE.getFogEndDistance())))))
                        .then(Commands.literal("attr")
                                .then(Commands.argument("attribute", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ATTR_KEYS, builder))
                                        .executes(LifeXpCommand::executeGetAttr))))

                .then(Commands.literal("set")
                        .then(Commands.literal("maxLevel")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(LifeXpCommand::executeSetMaxLevel)))
                        .then(Commands.literal("xpShieldEnable")
                                .executes(ctx -> executeGetSimple(ctx, "xpShieldEnable", String.valueOf(LifeXpConfig.INSTANCE.isEnableXpShield()))))
                        .then(Commands.literal("xpShieldPoints")
                                .executes(ctx -> executeGetSimple(ctx, "xpShieldPoints", String.valueOf(LifeXpConfig.INSTANCE.getXpShieldPointsPerDamage()))))
                        .then(Commands.literal("xpShieldEnable")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            LifeXpConfig.INSTANCE.setEnableXpShield(BoolArgumentType.getBool(ctx, "value"));
                                            return saveAndNotify(ctx, "xpShieldEnable", String.valueOf(LifeXpConfig.INSTANCE.isEnableXpShield()));
                                        })))
                        .then(Commands.literal("xpShieldPoints")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            LifeXpConfig.INSTANCE.setXpShieldPointsPerDamage(IntegerArgumentType.getInteger(ctx, "value"));
                                            return saveAndNotify(ctx, "xpShieldPoints", String.valueOf(LifeXpConfig.INSTANCE.getXpShieldPointsPerDamage()));
                                        })))
                        .then(Commands.literal("deathXpTax")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                        .executes(LifeXpCommand::executeSetDeathXpTax)))
                        .then(Commands.literal("deathCoords")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(LifeXpCommand::executeSetDeathCoords)))
                        .then(Commands.literal("fog")
                                .then(Commands.literal("enable")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(LifeXpCommand::executeSetFogEnable)))
                                .then(Commands.literal("start")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> executeSetFogDistance(ctx, "start"))))
                                .then(Commands.literal("mid")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> executeSetFogDistance(ctx, "mid"))))
                                .then(Commands.literal("end")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> executeSetFogDistance(ctx, "end")))))
                        .then(Commands.literal("attr")
                                .then(Commands.argument("attribute", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ATTR_KEYS, builder))
                                        .then(Commands.literal("start")
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> executeSetAttr(ctx, "start"))))
                                        .then(Commands.literal("mid")
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> executeSetAttr(ctx, "mid"))))
                                        .then(Commands.literal("end")
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> executeSetAttr(ctx, "end")))))))

                .then(Commands.literal("preset")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(LifeXpPresets.PRESET_NAMES, builder))
                                .executes(LifeXpCommand::executePreset)))
                                
                .then(Commands.literal("export")
                        .executes(LifeXpCommand::executeExport))
                        
                .then(Commands.literal("top")
                        .executes(LifeXpCommand::executeTop))
        );
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        LifeXpConfig.load();
        int count = recalculateAllPlayers(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(
                () -> prefix().append(Component.translatable("lifexp.command.reload.success", count)
                        .withStyle(ChatFormatting.GREEN)),
                true);
        return count;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> ctx) {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        CommandSourceStack source = ctx.getSource();

        source.sendSuccess(() -> Component.translatable("lifexp.command.status.header")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        source.sendSuccess(() -> Component.translatable("lifexp.command.status.general",
                        cfg.getMaxLevel(),
                        cfg.getDeathXpTax(),
                        cfg.isShowDeathCoordinates() ? "ON" : "OFF")
                .withStyle(ChatFormatting.YELLOW), false);

        source.sendSuccess(() -> Component.translatable("lifexp.command.status.fog",
                        cfg.isEnableCustomFog() ? "ON" : "OFF",
                        cfg.getFogStartDistance(),
                        cfg.getFogMidDistance(),
                        cfg.getFogEndDistance())
                .withStyle(ChatFormatting.AQUA), false);

        source.sendSuccess(() -> Component.translatable("lifexp.command.status.attr_header")
                .withStyle(ChatFormatting.GRAY), false);

        for (Map.Entry<String, AttributeConfigNode> entry : cfg.getNodes().entrySet()) {
            String key = entry.getKey();
            AttributeConfigNode node = entry.getValue();
            String dir = node.getDirection() == DifficultyDirection.INVERSE ? "↑=Harder" : "↑=Easier";
            source.sendSuccess(() -> Component.translatable("lifexp.command.status.attr",
                            key,
                            node.getStartValue(),
                            node.getMidValue(),
                            node.getEndValue(),
                            dir)
                    .withStyle(ChatFormatting.GRAY), false);
        }

        return 1;
    }

    private static int executeGetSimple(CommandContext<CommandSourceStack> ctx, String param, String value) {
        ctx.getSource().sendSuccess(
                () -> prefix().append(Component.translatable("lifexp.command.get.value", param, value)
                        .withStyle(ChatFormatting.YELLOW)),
                false);
        return 1;
    }

    private static int executeGetAttr(CommandContext<CommandSourceStack> ctx) {
        String attrName = StringArgumentType.getString(ctx, "attribute");
        AttributeConfigNode node = LifeXpConfig.INSTANCE.getNode(attrName);
        if (node == null) {
            ctx.getSource().sendFailure(
                    prefix().append(Component.translatable("lifexp.command.error.unknown_attr", attrName)
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }

        String dir = node.getDirection() == DifficultyDirection.INVERSE ? "↑=Harder" : "↑=Easier";
        ctx.getSource().sendSuccess(
                () -> prefix().append(Component.translatable("lifexp.command.status.attr",
                                attrName,
                                node.getStartValue(),
                                node.getMidValue(),
                                node.getEndValue(),
                                dir)
                        .withStyle(ChatFormatting.YELLOW)),
                false);
        return 1;
    }

    private static int executeSetMaxLevel(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        LifeXpConfig.INSTANCE.setMaxLevel(value);
        return saveAndNotify(ctx, "maxLevel", String.valueOf(value));
    }

    private static int executeSetDeathXpTax(CommandContext<CommandSourceStack> ctx) {
        double value = DoubleArgumentType.getDouble(ctx, "value");
        LifeXpConfig.INSTANCE.setDeathXpTax(value);
        return saveAndNotify(ctx, "deathXpTax", String.format("%.4f", value));
    }

    private static int executeSetDeathCoords(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        LifeXpConfig.INSTANCE.setShowDeathCoordinates(value);
        return saveAndNotify(ctx, "deathCoords", String.valueOf(value));
    }

    private static int executeSetFogEnable(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        LifeXpConfig.INSTANCE.setEnableCustomFog(value);
        return saveAndNotify(ctx, "fog.enable", String.valueOf(value));
    }

    private static int executeSetFogDistance(CommandContext<CommandSourceStack> ctx, String field) {
        double value = DoubleArgumentType.getDouble(ctx, "value");
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        switch (field) {
            case "start" -> cfg.setFogStartDistance(value);
            case "mid"   -> cfg.setFogMidDistance(value);
            case "end"   -> cfg.setFogEndDistance(value);
        }
        return saveAndNotify(ctx, "fog." + field, String.format("%.2f", value));
    }

    private static int executeSetAttr(CommandContext<CommandSourceStack> ctx, String field) {
        String attrName = StringArgumentType.getString(ctx, "attribute");
        double value = DoubleArgumentType.getDouble(ctx, "value");

        AttributeConfigNode node = LifeXpConfig.INSTANCE.getNode(attrName);
        if (node == null) {
            ctx.getSource().sendFailure(
                    prefix().append(Component.translatable("lifexp.command.error.unknown_attr", attrName)
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }

        switch (field) {
            case "start" -> node.setStartValue(value);
            case "mid"   -> node.setMidValue(value);
            case "end"   -> node.setEndValue(value);
        }

        return saveAndNotify(ctx, attrName + "." + field, String.format("%.4f", value));
    }

    private static int executePreset(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");

        if (!LifeXpPresets.apply(name)) {
            ctx.getSource().sendFailure(
                    prefix().append(Component.translatable("lifexp.command.error.unknown_preset", name)
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }

        LifeXpConfig.save();
        int count = recalculateAllPlayers(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(
                () -> prefix().append(Component.translatable("lifexp.command.preset.applied", name, count)
                        .withStyle(ChatFormatting.GREEN)),
                true);
        return count;
    }

    private static int executeExport(CommandContext<CommandSourceStack> ctx) {
        String json = LifeXpConfig.exportToJsonString();
        ctx.getSource().sendSuccess(() -> prefix().append(Component.literal("Конфиг экспортирован! Нажмите, чтобы скопировать.")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.COPY_TO_CLIPBOARD, json))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Нажмите для копирования JSON"))))), false);
        return 1;
    }

    private static int executeTop(CommandContext<CommandSourceStack> ctx) {
        java.util.List<ServerPlayer> players = new java.util.ArrayList<>(ctx.getSource().getServer().getPlayerList().getPlayers());
        players.sort((p1, p2) -> Integer.compare(p2.experienceLevel, p1.experienceLevel));

        ctx.getSource().sendSuccess(() -> Component.literal("--- Топ игроков LifeXP (Онлайн) ---").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        int rank = 1;
        for (ServerPlayer player : players) {
            final int currentRank = rank;
            ctx.getSource().sendSuccess(() -> Component.literal(currentRank + ". " + player.getScoreboardName() + " - Lvl " + player.experienceLevel)
                    .withStyle(currentRank <= 3 ? ChatFormatting.YELLOW : ChatFormatting.WHITE), false);
            rank++;
            if (rank > 10) break;
        }
        return 1;
    }

    private static MutableComponent prefix() {
        return Component.literal("[LIFE-XP] ").withStyle(ChatFormatting.GOLD);
    }

    private static int saveAndNotify(CommandContext<CommandSourceStack> ctx, String param, String value) {
        LifeXpConfig.save();
        int count = recalculateAllPlayers(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(
                () -> prefix().append(Component.translatable("lifexp.command.set.success", param, value)
                        .withStyle(ChatFormatting.GREEN)),
                true);
        return count > 0 ? count : 1;
    }

    private static int recalculateAllPlayers(MinecraftServer server) {
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AttributeService.recalculate(player);
            count++;
        }
        return count;
    }
}
