package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.data.LifeXpGlobalData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Менеджер сюжетных эвентов мода LIFE-XP Challenge.
 * Отвечает за триггеры прогрессии и босс-файтов (v2.0).
 */
public final class StoryEventManager {

    public static final long BOSS_EVENT_XP_THRESHOLD = 100_000L;
    public static final String TAG_BOSS_EVENT_TRIGGERED = "boss_event_100k_triggered";

    private StoryEventManager() {}

    /**
     * Проверка условия активации сюжетного босс-эвента на 100 000 XP.
     *
     * @param player игрок, получивший опыт
     * @param currentLifeXp суммарный текущий опыт игрока
     */
    public static void checkBossEventTrigger(ServerPlayer player, long currentLifeXp) {
        if (currentLifeXp < BOSS_EVENT_XP_THRESHOLD || player == null || player.level().getServer() == null) {
            return;
        }

        LifeXpGlobalData globalData = LifeXpGlobalData.get(player.level().getServer());
        CompoundTag playerData = globalData.getPlayerData(player);

        // O(1) проверка: если эвент уже сработал ранее — прерываем выполнение
//? if >= 26.1 {
        if (playerData.getBooleanOr(TAG_BOSS_EVENT_TRIGGERED, false)) {
            return;
        }
//?} else {
      /*if (playerData.getBoolean(TAG_BOSS_EVENT_TRIGGERED)) {
            return;
        }*/
//?}

        // Фиксируем триггер в глобальной базе данных
        playerData.putBoolean(TAG_BOSS_EVENT_TRIGGERED, true);
        globalData.setDirty();

        // ── Временная логика (Заглушка 1.6.5) ──
        player.sendSystemMessage(Component.literal("Твой разум переполнен опытом. Что-то пробуждается...")
                .withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED));

//? if >= 26.1 {
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 255));
//?} else {
      /*player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 255));*/
//?}
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));

        // ── Заглушки v2.0 ──
        sendCutscenePacket(player);
        if (player.level() instanceof ServerLevel serverLevel) {
            spawnApophisAltar(serverLevel, player.blockPosition());
        }
    }

    /**
     * Отправка сетевого пакета воспроизведения кат-сцены клиенту.
     */
    private static void sendCutscenePacket(ServerPlayer player) {
        // TODO: v2.0 implementation
    }

    /**
     * Спавн алтаря Апофиса в мире на координатах игрока.
     */
    private static void spawnApophisAltar(ServerLevel level, BlockPos pos) {
        // TODO: v2.0 implementation
    }
}
