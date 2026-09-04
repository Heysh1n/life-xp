package com.hs1n.lifeXp_challenge.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

/**
 * Утилита для унифицированного форматирования и отправки сообщений мода с префиксом.
 */
public final class MessageUtils {

    private MessageUtils() {}

    public static MutableComponent prefix() {
        return Component.literal("[LIFE-XP Challenge] ").withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent info(String translationKey, Object... args) {
        return prefix().append(Component.translatable(translationKey, args).withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent error(String translationKey, Object... args) {
        return prefix().append(Component.translatable(translationKey, args).withStyle(ChatFormatting.RED));
    }

    public static void sendInfo(Player player, String translationKey, Object... args) {
        player.sendSystemMessage(info(translationKey, args));
    }

    public static void sendError(Player player, String translationKey, Object... args) {
        player.sendSystemMessage(error(translationKey, args));
    }

    public static void sendActionBarError(Player player, String translationKey, Object... args) {
        player.displayClientMessage(error(translationKey, args), true);
    }

    public static void sendActionBarInfo(Player player, String translationKey, Object... args) {
        player.displayClientMessage(info(translationKey, args), true);
    }
}
