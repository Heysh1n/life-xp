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

    public static MutableComponent prefixLong() {
        return Component.translatable("lifexp.message.prefix").withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent prefixMedium() {
        return Component.translatable("lifexp.message.prefix_medium").withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent prefixShort() {
        return Component.translatable("lifexp.message.prefix_short").withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent info(String translationKey, Object... args) {
        return prefixShort().append(Component.translatable(translationKey, args).withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent error(String translationKey, Object... args) {
        return prefixShort().append(Component.translatable(translationKey, args).withStyle(ChatFormatting.RED));
    }

    public static void sendInfo(Player player, String translationKey, Object... args) {
//? if >= 26.1 {
        player.sendSystemMessage(info(translationKey, args));
//?} else {
        /*player.displayClientMessage(info(translationKey, args), false);*/
//?}
    }

    public static void sendError(Player player, String translationKey, Object... args) {
//? if >= 26.1 {
        player.sendSystemMessage(error(translationKey, args));
//?} else {
        /*player.displayClientMessage(error(translationKey, args), false);*/
//?}
    }

    public static void sendActionBarError(Player player, String translationKey, Object... args) {
//? if >= 26.1 {
        player.sendOverlayMessage(error(translationKey, args));
//?} else {
      /*player.displayClientMessage(error(translationKey, args), true);*/
//?}
    }

    public static void sendActionBarInfo(Player player, String translationKey, Object... args) {
//? if >= 26.1 {
        player.sendOverlayMessage(info(translationKey, args));
//?} else {
      /*player.displayClientMessage(info(translationKey, args), true);*/
//?}
    }
}
