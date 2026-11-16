package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.command.LifeXpCommand;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import com.hs1n.lifeXp_challenge.network.LifeXpNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PresetManager {

    private PresetManager() {}

    /**
     * Вызывается в LifecycleEvent.SERVER_STARTED.
     * Проверяет, является ли сервер выделенным (Dedicated).
     * Dedicated -> форсируем хардкорный пресет (Core).
     * Integrated (одиночная игра) -> щадящий пресет (Vanilla+).
     */
    public static void onServerStarted(MinecraftServer server) {
        if (!LifeXpConfig.INSTANCE.isPresetInitialized()) {
            if (server.isDedicatedServer()) {
                LifeXpPresets.applyCore();
                LifeXpChallenge.LOGGER.info("[LIFE-XP] Dedicated server detected. Auto-configured HARDCORE (Core) preset.");
            } else {
                LifeXpPresets.applyVanillaPlus();
                LifeXpChallenge.LOGGER.info("[LIFE-XP] Integrated server detected. Auto-configured LENIENT (Vanilla+) preset.");
            }
            LifeXpConfig.INSTANCE.setPresetInitialized(true);
            LifeXpConfig.save();
        }
    }

    /**
     * Проверка прав оператора (Уровень 2).
     */
//? if >= 26.1 {
    public static boolean hasOperatorPermissions(ServerPlayer player) {
        return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }
//?} else {
  /*public static boolean hasOperatorPermissions(ServerPlayer player) {
        return player.hasPermissions(2);
    }*/
//?}

    /**
     * Проверка наличия тега выбора пресета у игрока.
     */
    public static boolean hasPresetTag(ServerPlayer player) {
//? if >= 26.1 {
        return player.entityTags().contains(LifeXpCommand.PRESET_TAG);
//?} else {
      /*return player.getTags().contains(LifeXpCommand.PRESET_TAG);*/
//?}
    }

    /**
     * Безопасное открытие меню выбора пресета при первом входе.
     * Открывается ТОЛЬКО если игрок имеет права оператора (hasPermissions(2)).
     * Обычные игроки не имеют доступа и сразу помечаются тегом, предотвращая открытие меню и эксплойт.
     */
    public static void checkAndPromptPreset(ServerPlayer player) {
        if (!LifeXpConfig.INSTANCE.isPresetInitialized() && !hasPresetTag(player)) {
            if (hasOperatorPermissions(player)) {
                LifeXpNetworking.sendOpenPresetScreen(player);
            } else {
                player.addTag(LifeXpCommand.PRESET_TAG);
            }
        }
    }
}
