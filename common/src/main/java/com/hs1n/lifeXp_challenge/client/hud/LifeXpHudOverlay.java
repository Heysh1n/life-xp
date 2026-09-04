package com.hs1n.lifeXp_challenge.client.hud;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиентский HUD-оверлей для LIFE-XP Challenge.
 * Рисует компактную панель в верхнем левом углу экрана:
 *   - Текущий уровень / maxLevel
 *   - Полоска прогресса до maxLevel
 *   - Kill Streak (если > 0)
 */
public class LifeXpHudOverlay {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 5;
    private static final int PADDING = 4;

    // Цвета (ARGB)
    private static final int BG_COLOR       = 0xAA000000; // полупрозрачный чёрный
    private static final int BAR_BG_COLOR   = 0xFF333333; // тёмно-серый
    private static final int BAR_FILL_COLOR = 0xFF55FF55; // зелёный
    private static final int BAR_MAX_COLOR  = 0xFFFFD700; // золотой (при maxLevel)
    private static final int TEXT_COLOR     = 0xFFFFFFFF; // белый
    private static final int STREAK_COLOR   = 0xFFFF6600; // оранжевый

    public static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.gui.getDebugOverlay().showDebugScreen()) {
            return;
        }

        LocalPlayer player = mc.player;
        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int maxLevel = config.getMaxLevel();
        int currentLevel = player.experienceLevel;

        // Позиция (верхний левый угол с отступом)
        int x = 4;
        int y = 4;

        // Рассчитываем размеры панели
        String levelText = "Lvl " + currentLevel + " / " + maxLevel;
        int textWidth = mc.font.width(levelText);
        int panelWidth = Math.max(BAR_WIDTH, textWidth) + PADDING * 2;
        int panelHeight = PADDING + mc.font.lineHeight + 2 + BAR_HEIGHT + PADDING;

        // Kill streak текст (если есть)
        int streak = KillStreakService.getClientStreak();
        String streakText = null;
        if (streak > 0) {
            streakText = "🔥 Streak: " + streak + " (+" + streak + "%)";
            panelWidth = Math.max(panelWidth, mc.font.width(streakText) + PADDING * 2);
            panelHeight += mc.font.lineHeight + 2;
        }

        // Прогресс до следующего уровня
        float progressToNext = player.experienceProgress;
        String progressText = "XP: " + (int)(progressToNext * 100) + "%";
        panelWidth = Math.max(panelWidth, mc.font.width(progressText) + PADDING * 2);
        panelHeight += mc.font.lineHeight + 2;

        // Фон панели
        graphics.fill(x, y, x + panelWidth, y + panelHeight, BG_COLOR);

        int textY = y + PADDING;

        // Строка 1: Уровень
        graphics.drawString(mc.font, levelText, x + PADDING, textY, TEXT_COLOR, true);
        textY += mc.font.lineHeight + 2;

        // Строка 2: Полоска прогресса до maxLevel
        float ratio = Math.min((float) currentLevel / maxLevel, 1.0f);
        int barX = x + PADDING;
        int barY = textY;
        int fillWidth = (int) (BAR_WIDTH * ratio);
        int barColor = ratio >= 1.0f ? BAR_MAX_COLOR : BAR_FILL_COLOR;

        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BG_COLOR);
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, barColor);
        }
        textY += BAR_HEIGHT + 2;

        // Строка 3: XP до следующего уровня
        graphics.drawString(mc.font, progressText, x + PADDING, textY, 0xFFAAFFAA, true);
        textY += mc.font.lineHeight + 2;

        // Строка 4: Kill Streak (если есть)
        if (streakText != null) {
            graphics.drawString(mc.font, streakText, x + PADDING, textY, STREAK_COLOR, true);
        }
    }
}
