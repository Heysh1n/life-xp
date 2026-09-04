package com.hs1n.lifeXp_challenge.util;

import com.hs1n.lifeXp_challenge.service.AttributeService;
import net.minecraft.world.entity.player.Player;

/**
 * Утилита для точной нелинейной работы с очками опыта Minecraft.
 * Корректно рассчитывает полный опыт из уровней и прогресса без использования
 * устаревшего или забагованного totalExperience.
 */
public final class ExperienceUtils {

    private ExperienceUtils() {}

    /**
     * Возвращает количество очков опыта, необходимое для перехода с текущего уровня на следующий.
     */
    public static int getXpNeededToLevelUp(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }

    /**
     * Возвращает общее количество очков опыта от 0 до заданного уровня.
     * Формула для уровня L:
     * L >= 32: 4.5*L^2 - 162.5*L + 2220
     * L >= 16: 2.5*L^2 - 40.5*L + 360
     * L < 16:  L^2 + 6*L
     */
    public static int getTotalXpForLevel(int level) {
        if (level <= 0) return 0;
        if (level >= 32) {
            return (int) Math.round(4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 16) {
            return (int) Math.round(2.5 * level * level - 40.5 * level + 360);
        } else {
            return level * level + 6 * level;
        }
    }

    /**
     * Возвращает точное суммарное количество очков опыта игрока,
     * вычисляя его строго из experienceLevel и experienceProgress.
     */
    public static int getPlayerTotalXp(Player player) {
        int level = player.experienceLevel;
        if (level < 0) return 0;

        int base = getTotalXpForLevel(level);
        int progress = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return Math.max(0, base + progress);
    }

    /**
     * Принудительно устанавливает игроку точное суммарное количество очков опыта
     * с корректным пересчётом уровня и полосы прогресса.
     */
    public static void setPlayerTotalXp(Player player, int totalXp) {
        totalXp = Math.max(0, totalXp);
        player.totalExperience = totalXp;

        int level = 0;
        while (getTotalXpForLevel(level + 1) <= totalXp) {
            level++;
        }

        player.experienceLevel = level;
        int remainingXp = totalXp - getTotalXpForLevel(level);
        int needed = getXpNeededToLevelUp(level);
        player.experienceProgress = needed > 0 ? (float) remainingXp / (float) needed : 0.0f;

        // Пересчёт атрибутов мода
        AttributeService.recalculate(player);
    }

    /**
     * Вычитает у игрока указанное количество очков опыта.
     * @return фактически списанное количество очков.
     */
    public static int deductPlayerXp(Player player, int pointsToDeduct) {
        if (pointsToDeduct <= 0) return 0;
        int current = getPlayerTotalXp(player);
        if (current <= 0) return 0;

        int toDeduct = Math.min(current, pointsToDeduct);
        setPlayerTotalXp(player, current - toDeduct);
        return toDeduct;
    }

    /**
     * Расчёт уровня по количеству сырых очков опыта.
     */
    public static int calculateLevelFromXp(int totalXp) {
        if (totalXp <= 0) return 0;
        int level = 0;
        while (getTotalXpForLevel(level + 1) <= totalXp) {
            level++;
        }
        return level;
    }
}
