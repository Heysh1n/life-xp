package com.hs1n.lifeXp_challenge.util;

import com.hs1n.lifeXp_challenge.service.AttributeService;
import net.minecraft.world.entity.player.Player;

/**
 * Утилита для точной нелинейной работы с очками опыта Minecraft в O(1).
 * Корректно рассчитывает полный опыт из уровней и прогресса без циклов,
 * предотвращая Watchdog Crash и переполнение 32-битного Integer при высоких уровнях.
 */
public final class ExperienceUtils {

    private ExperienceUtils() {}

    /**
     * Возвращает количество очков опыта, необходимое для перехода с текущего уровня на следующий (O(1)).
     */
    public static int getXpNeededToLevelUp(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + Math.max(0, level) * 2;
        }
    }

    /**
     * Возвращает общее количество очков опыта от 0 до заданного уровня в O(1).
     * Формула для уровня L:
     * L >= 32: 4.5*L^2 - 162.5*L + 2220
     * L >= 16: 2.5*L^2 - 40.5*L + 360
     * L < 16:  L^2 + 6*L
     */
    public static long getTotalXpForLevel(int level) {
        if (level <= 0) return 0L;
        long l = level;
        if (l >= 32) {
            return Math.round(4.5 * l * l - 162.5 * l + 2220.0);
        } else if (l >= 16) {
            return Math.round(2.5 * l * l - 40.5 * l + 360.0);
        } else {
            return l * l + 6L * l;
        }
    }

    /**
     * Возвращает точное суммарное количество очков опыта игрока (long),
     * вычисляя его строго в O(1) из experienceLevel и experienceProgress.
     */
    public static long getPlayerTotalXp(Player player) {
        if (player == null) return 0L;
        int level = player.experienceLevel;
        if (level < 0) return 0L;

        long base = getTotalXpForLevel(level);
        long progress = Math.round((double) Math.max(0.0f, Math.min(1.0f, player.experienceProgress)) * player.getXpNeededForNextLevel());
        return Math.max(0L, base + progress);
    }

    /**
     * Расчёт уровня по суммарному пулу очков опыта строго за O(1) без циклов (обратные квадратичные корни).
     */
    public static int calculateLevelFromXp(long totalXp) {
        if (totalXp <= 0L) return 0;
        int level;
        if (totalXp < 352L) {
            level = (int) Math.floor(Math.sqrt(totalXp + 9.0) - 3.0);
        } else if (totalXp < 1628L) {
            level = (int) Math.floor((81.0 + Math.sqrt(40.0 * totalXp - 7839.0)) / 10.0);
        } else {
            level = (int) Math.floor((325.0 + Math.sqrt(72.0 * totalXp - 54215.0)) / 18.0);
        }

        // Защитная O(1) проверка краевых условий округления корней
        if (getTotalXpForLevel(level + 1) <= totalXp) {
            level++;
        } else if (level > 0 && getTotalXpForLevel(level) > totalXp) {
            level--;
        }

        return Math.max(0, level);
    }

    /**
     * Принудительно устанавливает игроку точное суммарное количество очков опыта (long)
     * с корректным O(1) пересчётом уровня и полосы прогресса.
     */
    public static void setPlayerTotalXp(Player player, long totalXp) {
        if (player == null) return;
        totalXp = Math.max(0L, totalXp);
        player.totalExperience = (int) Math.min(Integer.MAX_VALUE, totalXp);

        int level = calculateLevelFromXp(totalXp);
        player.experienceLevel = level;

        long base = getTotalXpForLevel(level);
        long remaining = Math.max(0L, totalXp - base);
        int needed = getXpNeededToLevelUp(level);
        player.experienceProgress = needed > 0 ? Math.max(0.0f, Math.min(1.0f, (float) remaining / (float) needed)) : 0.0f;

        // Пересчёт атрибутов мода
        AttributeService.recalculate(player);
    }

    /**
     * Перегрузка для int totalXp.
     */
    public static void setPlayerTotalXp(Player player, int totalXp) {
        setPlayerTotalXp(player, (long) Math.max(0, totalXp));
    }

    /**
     * Вычитает у игрока указанное количество очков опыта (long).
     * Никогда не прибавляет опыт и не уходит в отрицательные значения.
     * @return фактически списанное количество очков.
     */
    public static long deductPlayerXp(Player player, long pointsToDeduct) {
        if (player == null || pointsToDeduct <= 0L) return 0L;
        long current = getPlayerTotalXp(player);
        if (current <= 0L) return 0L;

        long toDeduct = Math.max(0L, Math.min(current, pointsToDeduct));
        long newXp = Math.max(0L, current - toDeduct);
        newXp = Math.min(current, newXp);

        setPlayerTotalXp(player, newXp);
        return toDeduct;
    }

    /**
     * Перегрузка для обратной совместимости с вызовами, передающими int.
     */
    public static int deductPlayerXp(Player player, int pointsToDeduct) {
        return (int) Math.min(Integer.MAX_VALUE, deductPlayerXp(player, (long) pointsToDeduct));
    }
}
