package com.hs1n.lifeXp_challenge.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillStreakService {
    private static final Map<UUID, Integer> KILL_STREAKS = new ConcurrentHashMap<>();
    
    // Множитель: 1% дополнительного опыта за каждое убийство моба в серии
    private static final double STREAK_BONUS_PER_KILL = 0.01;
    private static final int MAX_STREAK = 100; // Максимум +100% опыта (х2)

    public static void onMobKilled(ServerPlayer player) {
        if (!com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE.isEnableKillStreak()) return;
        UUID uuid = player.getUUID();
        int streak = KILL_STREAKS.getOrDefault(uuid, 0) + 1;
        streak = Math.min(streak, MAX_STREAK);
        KILL_STREAKS.put(uuid, streak);

        // Синхронизируем стрик с клиентом для HUD
        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.syncStreakToClient(player);

        // Каждые 10 убийств уведомляем игрока (опционально)
        if (streak % 10 == 0) {
            double bonus = streak * STREAK_BONUS_PER_KILL * 100;
            player.displayClientMessage(Component.translatable("lifexp.message.kill_streak", streak, (int)bonus).withStyle(net.minecraft.ChatFormatting.GOLD), true);
        }
    }

    public static void resetStreak(ServerPlayer player) {
        KILL_STREAKS.remove(player.getUUID());
        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.syncStreakToClient(player);
    }

    public static int modifyExperience(ServerPlayer player, int amount) {
        if (!com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE.isEnableKillStreak()) return amount;
        int streak = KILL_STREAKS.getOrDefault(player.getUUID(), 0);
        if (streak > 0) {
            double multiplier = 1.0 + (streak * STREAK_BONUS_PER_KILL);
            return (int) Math.round(amount * multiplier);
        }
        return amount;
    }

    // ── Client-side streak for HUD ──
    private static int clientStreak = 0;

    public static int getClientStreak() {
        return clientStreak;
    }

    public static void setClientStreak(int streak) {
        clientStreak = streak;
    }

    /** Получить серверный стрик игрока (для серверного кода). */
    public static int getStreak(ServerPlayer player) {
        return KILL_STREAKS.getOrDefault(player.getUUID(), 0);
    }
}
