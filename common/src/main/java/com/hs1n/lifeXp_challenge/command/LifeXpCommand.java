package com.hs1n.lifeXp_challenge.command;

import com.hs1n.lifeXp_challenge.config.AttributeConfigNode;
import com.hs1n.lifeXp_challenge.config.DifficultyDirection;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.hs1n.lifeXp_challenge.util.MessageUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.ClickEvent;
import java.util.List;
import java.util.ArrayList;
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
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))

                .then(Commands.literal("reload")
                        .executes(LifeXpCommand::executeReload))

                .then(Commands.literal("status")
                        .executes(ctx -> executeStatus(ctx, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> executeStatus(ctx, IntegerArgumentType.getInteger(ctx, "page")))))

                .then(Commands.literal("get")
                        .then(Commands.literal("maxLevel")
                                .executes(ctx -> executeGetSimple(ctx, "maxLevel",
                                        String.valueOf(LifeXpConfig.INSTANCE.getMaxLevel()))))
                        .then(Commands.literal("deathXpTax")
                                .executes(ctx -> executeGetSimple(ctx, "deathXpTax",
                                        String.format("%.4f", LifeXpConfig.INSTANCE.getDeathXpTax()))))
                        .then(Commands.literal("deathCoords")
                                .executes(ctx -> executeGetSimple(ctx, "deathCoords",
                                        String.valueOf(LifeXpConfig.INSTANCE.isShowDeathCoordinates()))))
                        .then(Commands.literal("attr")
                                .then(Commands.argument("attribute", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ATTR_KEYS, builder))
                                        .executes(LifeXpCommand::executeGetAttr))))

                .then(Commands.literal("set")
                        .then(Commands.literal("maxLevel")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(LifeXpCommand::executeSetMaxLevel)))
                        .then(Commands.literal("deathXpTax")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                        .executes(LifeXpCommand::executeSetDeathXpTax)))
                        .then(Commands.literal("deathCoords")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(LifeXpCommand::executeSetDeathCoords)))
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
                () -> MessageUtils.prefixShort().append(Component.translatable("lifexp.command.reload.success", count)
                        .withStyle(ChatFormatting.GREEN)),
                true);
        return count;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> ctx, int page) {
        LifeXpConfig cfg = LifeXpConfig.INSTANCE;
        CommandSourceStack source = ctx.getSource();

        List<Map.Entry<String, AttributeConfigNode>> entries = new ArrayList<>(cfg.getNodes().entrySet());
        int itemsPerPage = 5;
        int totalPages = (int) Math.ceil((double) entries.size() / itemsPerPage);

        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        // Header: GOLD + DARK_GRAY
        source.sendSuccess(() -> Component.literal("====== ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Статус LifeXP").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY)), false);

        if (page == 1) {
            source.sendSuccess(() -> Component.translatable("lifexp.command.status.general",
                            cfg.getMaxLevel(),
                            cfg.getDeathXpTax(),
                            cfg.isShowDeathCoordinates() ? "ON" : "OFF")
                    .withStyle(ChatFormatting.YELLOW), false);

            source.sendSuccess(() -> Component.translatable("lifexp.command.status.visuals")
                    .withStyle(ChatFormatting.AQUA), false);
        }

        source.sendSuccess(() -> Component.literal(""), false);

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, entries.size());

        for (int i = start; i < end; i++) {
            Map.Entry<String, AttributeConfigNode> entry = entries.get(i);
            String key = entry.getKey();
            AttributeConfigNode node = entry.getValue();
            
            Component hoverText = Component.literal("Значение на 50%: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(node.getMidValue())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nСкейлинг: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(node.getDirection() == DifficultyDirection.INVERSE ? "Обратный (Сложнее)" : "Прямой (Легче)").withStyle(ChatFormatting.YELLOW));

            Component details = Component.literal(" [Детали...]").withStyle(style -> style
                .withColor(ChatFormatting.DARK_GREEN)
                .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(hoverText)));

            source.sendSuccess(() -> Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("lifexp.attr." + key).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(node.getStartValue())).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" ➔ ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.valueOf(node.getEndValue())).withStyle(ChatFormatting.WHITE))
                    .append(details), false);
        }

        source.sendSuccess(() -> Component.literal(""), false);

        final int finalPage = page;
        // Footer Pagination
        Component prev = page > 1 
            ? Component.literal("[◀]").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand("/lifexp status " + (finalPage - 1))))
            : Component.literal("[◀]").withStyle(ChatFormatting.DARK_GRAY);

        Component next = page < totalPages 
            ? Component.literal("[▶]").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand("/lifexp status " + (finalPage + 1))))
            : Component.literal("[▶]").withStyle(ChatFormatting.DARK_GRAY);

        
        source.sendSuccess(() -> Component.literal("    ")
                .append(prev)
                .append(Component.literal(" Страница " + finalPage + " из " + totalPages + " ").withStyle(ChatFormatting.GRAY))
                .append(next), false);

        return 1;
    }

    private static int executeGetSimple(CommandContext<CommandSourceStack> ctx, String param, String value) {
        ctx.getSource().sendSuccess(
                () -> MessageUtils.prefixShort().append(Component.translatable("lifexp.command.get.value", param, value)
                        .withStyle(ChatFormatting.YELLOW)),
                false);
        return 1;
    }

    private static int executeGetAttr(CommandContext<CommandSourceStack> ctx) {
        String attrName = StringArgumentType.getString(ctx, "attribute");
        AttributeConfigNode node = LifeXpConfig.INSTANCE.getNode(attrName);
        if (node == null) {
            ctx.getSource().sendFailure(
                    MessageUtils.prefixShort().append(Component.translatable("lifexp.command.error.unknown_attr", attrName)
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }

        Component hoverText = Component.literal("Значение на 50%: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(node.getMidValue())).withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\nСкейлинг: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(node.getDirection() == DifficultyDirection.INVERSE ? "Обратный (Сложнее)" : "Прямой (Легче)").withStyle(ChatFormatting.YELLOW));

        Component details = Component.literal(" [Детали...]").withStyle(style -> style
            .withColor(ChatFormatting.DARK_GREEN)
            .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(hoverText)));

        ctx.getSource().sendSuccess(() -> MessageUtils.prefixShort()
                .append(Component.translatable("lifexp.attr." + attrName).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(node.getStartValue())).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" ➔ ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.valueOf(node.getEndValue())).withStyle(ChatFormatting.WHITE))
                .append(details), false);
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


    private static int executeSetAttr(CommandContext<CommandSourceStack> ctx, String field) {
        String attrName = StringArgumentType.getString(ctx, "attribute");
        double value = DoubleArgumentType.getDouble(ctx, "value");

        AttributeConfigNode node = LifeXpConfig.INSTANCE.getNode(attrName);
        if (node == null) {
            ctx.getSource().sendFailure(
                    MessageUtils.prefixShort().append(Component.translatable("lifexp.command.error.unknown_attr", attrName)
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

    public static final String PRESET_TAG = "lifexp_preset_chosen";

    private static int executePreset(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();

        // Проверяем перманентную блокировку выбора пресета (Origins-style)
        if (LifeXpConfig.INSTANCE.isLockPresetAfterSelection() && player != null && player.entityTags().contains(PRESET_TAG)) {
            source.sendFailure(MessageUtils.prefixShort().append(Component.translatable("lifexp.error.preset_locked")
                    .withStyle(ChatFormatting.RED)));
            return 0;
        }

        String name = StringArgumentType.getString(ctx, "name");

        if (!LifeXpPresets.apply(name)) {
            source.sendFailure(
                    MessageUtils.prefixShort().append(Component.translatable("lifexp.command.error.unknown_preset", name)
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }

        // Перманентно выставляем тег игроку
        if (player != null) {
            player.addTag(PRESET_TAG);
        }

        LifeXpConfig.save();
        int count = recalculateAllPlayers(source.getServer());
        source.sendSuccess(
                () -> MessageUtils.prefixShort().append(Component.translatable("lifexp.command.preset.applied", name, count)
                        .withStyle(ChatFormatting.GREEN)),
                true);
        return count;
    }

    private static int executeExport(CommandContext<CommandSourceStack> ctx) {
        String json = LifeXpConfig.exportToJsonString();
        ctx.getSource().sendSuccess(() -> MessageUtils.prefixShort().append(Component.translatable("lifexp.command.export.success")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.CopyToClipboard(json))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.translatable("lifexp.command.export.hover"))))), false);
        return 1;
    }

    private static int executeTop(CommandContext<CommandSourceStack> ctx) {
        java.util.List<ServerPlayer> players = new java.util.ArrayList<>(ctx.getSource().getServer().getPlayerList().getPlayers());
        players.sort((p1, p2) -> Integer.compare(p2.experienceLevel, p1.experienceLevel));

        ctx.getSource().sendSuccess(() -> Component.literal("====== ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Топ Игроков").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY)), false);
                
        int rank = 1;
        for (ServerPlayer player : players) {
            final int currentRank = rank;
            
            ChatFormatting color = ChatFormatting.WHITE;
            if (currentRank == 1) color = ChatFormatting.GOLD;
            else if (currentRank == 2) color = ChatFormatting.GRAY;
            
            
            if (currentRank == 3) color = ChatFormatting.RED;
            if (currentRank > 3) color = ChatFormatting.DARK_GRAY;

            final ChatFormatting finalColor = color;
            ctx.getSource().sendSuccess(() -> Component.literal("  #" + currentRank + " ").withStyle(finalColor, ChatFormatting.BOLD)
                    .append(player.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" - Уровень ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(player.experienceLevel)).withStyle(ChatFormatting.YELLOW)), false);
            rank++;
            if (rank > 10) break;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal("=====================").withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int saveAndNotify(CommandContext<CommandSourceStack> ctx, String param, String value) {
        LifeXpConfig.save();
        int count = recalculateAllPlayers(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(
                () -> MessageUtils.prefixShort().append(Component.translatable("lifexp.command.set.success", param, value)
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
