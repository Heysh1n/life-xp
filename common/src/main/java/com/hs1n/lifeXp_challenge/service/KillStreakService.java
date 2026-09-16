package com.hs1n.lifeXp_challenge.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

/**
 * Kill-streak service with persistent entity-tag storage.
 * Streak state survives logouts/restarts via entity tags.
 * 5-minute timeout resets streak on kill or join.
 */
public class KillStreakService {

    private static final String TAG_STREAK_PREFIX = "lifexp_streak_";
    private static final String TAG_KILL_TIME_PREFIX = "lifexp_last_kill_time_";

    private static final double STREAK_BONUS_PER_KILL = 0.01;
    private static final int MAX_STREAK = 100;
    private static final long TIMEOUT_MS = 300_000L; // 5 minutes

    public static void onMobKilled(ServerPlayer player) {
        if (!com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE.isEnableKillStreak()) return;

        long now = System.currentTimeMillis();
        long lastKillTime = getTagLong(player, TAG_KILL_TIME_PREFIX);

        int streak;
        if (lastKillTime > 0 && (now - lastKillTime) > TIMEOUT_MS) {
            streak = 0; // timed out
        } else {
            streak = getTagInt(player, TAG_STREAK_PREFIX);
        }

        // Increment
        streak = Math.min(streak + 1, MAX_STREAK);

        // Save
        setTagInt(player, TAG_STREAK_PREFIX, streak);
        setTagLong(player, TAG_KILL_TIME_PREFIX, now);

        // Sync to client HUD
        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.syncStreakToClient(player);

        // Notify every 10 kills
        if (streak % 10 == 0) {
            double bonus = streak * STREAK_BONUS_PER_KILL * 100;
            player.sendOverlayMessage(Component.translatable("lifexp.message.kill_streak", streak, (int) bonus)
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    public static void resetStreak(ServerPlayer player) {
        setTagInt(player, TAG_STREAK_PREFIX, 0);
        removeTag(player, TAG_KILL_TIME_PREFIX);
        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.syncStreakToClient(player);
    }

    /**
     * Called on player join. Checks timeout and resets if expired. Syncs to client.
     */
    public static void onPlayerJoin(ServerPlayer player) {
        if (!com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE.isEnableKillStreak()) return;

        long lastKillTime = getTagLong(player, TAG_KILL_TIME_PREFIX);
        if (lastKillTime > 0 && (System.currentTimeMillis() - lastKillTime) > TIMEOUT_MS) {
            setTagInt(player, TAG_STREAK_PREFIX, 0);
            removeTag(player, TAG_KILL_TIME_PREFIX);
        }

        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.syncStreakToClient(player);
    }

    public static int modifyExperience(ServerPlayer player, int amount) {
        if (!com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE.isEnableKillStreak()) return amount;
        int streak = getStreak(player);
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

    /** Get server-side streak from entity tags. */
    public static int getStreak(ServerPlayer player) {
        return getTagInt(player, TAG_STREAK_PREFIX);
    }

    // ── Tag-based persistent storage helpers ──

    private static int getTagInt(ServerPlayer player, String prefix) {
        for (String tag : player.entityTags()) {
            if (tag.startsWith(prefix)) {
                try {
                    return Integer.parseInt(tag.substring(prefix.length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private static void setTagInt(ServerPlayer player, String prefix, int value) {
        player.entityTags().removeIf(tag -> tag.startsWith(prefix));
        player.addTag(prefix + value);
    }

    private static long getTagLong(ServerPlayer player, String prefix) {
        for (String tag : player.entityTags()) {
            if (tag.startsWith(prefix)) {
                try {
                    return Long.parseLong(tag.substring(prefix.length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0L;
    }

    private static void setTagLong(ServerPlayer player, String prefix, long value) {
        player.entityTags().removeIf(tag -> tag.startsWith(prefix));
        player.addTag(prefix + value);
    }

    private static void removeTag(ServerPlayer player, String prefix) {
        player.entityTags().removeIf(tag -> tag.startsWith(prefix));
    }
}
