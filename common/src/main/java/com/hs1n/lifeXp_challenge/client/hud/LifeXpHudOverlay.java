package com.hs1n.lifeXp_challenge.client.hud;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;

/**
 * Графический клиентский HUD-оверлей для LIFE-XP Challenge.
 * Отрисовывает ряд из 10 сфер прогресса над полоской голода (hunger bar) справа.
 * Скрывается при F1, F3, а также в режимах Creative и Spectator.
 */
public class LifeXpHudOverlay {

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.gui.getDebugOverlay().showDebugScreen()) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player.isSpectator() || player.isCreative()) {
            return;
        }

        LifeXpConfig config = LifeXpConfig.INSTANCE;
        if (!config.isShowHudBubbles()) {
            return;
        }

        int maxLevel = config.getMaxLevel();
        if (maxLevel <= 0) {
            maxLevel = 1;
        }

        int currentLevel = player.experienceLevel;
        float progress = (float) currentLevel / (float) maxLevel;
        int filledCount = Math.clamp((int) (progress * 10.0f), 0, 10);

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Базовая позиция над полоской голода
        int y = screenHeight - 49;

        // Смещение при наличии сердец транспорта (лошади и т.д.)
        if (player.getVehicle() instanceof LivingEntity vehicle) {
            int maxHearts = (int) Math.ceil((double) vehicle.getMaxHealth() / 2.0);
            int rows = (int) Math.ceil((double) maxHearts / 10.0);
            if (rows > 1) {
                y -= (rows - 1) * 10;
            }
        }

        // Смещение, если отображаются пузырьки воздуха (под водой)
        if (player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply()) {
            y -= 10;
        }

        // 10 иконок над полоской сытости (справа, выравнивание по ширине ванильного hunger bar)
        // Ванильный hunger bar располагается от (screenWidth / 2 + 10) до (screenWidth / 2 + 91)
        int startX = screenWidth / 2 + 10;
        for (int i = 0; i < 10; i++) {
            int iconX = startX + i * 8;
            renderOrb(graphics, iconX, y, i < filledCount, progress);
        }

        // Серия убийств (Kill Streak), если активна
        int streak = KillStreakService.getClientStreak();
        if (streak > 0) {
            String streakText = "🔥 " + streak + " (+" + streak + "%)";
            int textX = (screenWidth / 2 + 90) - mc.font.width(streakText);
            graphics.drawString(mc.font, streakText, textX, y - 10, 0xFFFF6600, true);
        }
    }

    /**
     * Отрисовка одной 8x8 сферы опыта через GuiGraphics.fill.
     * Заполненная сфера окрашивается динамически в соответствии с тиром прогресса:
     * - progress < 0.50f: зелёный
     * - 0.50f <= progress < 1.00f: аквамариновый
     * - progress >= 1.00f: золотой
     */
    private static void renderOrb(GuiGraphics graphics, int x, int y, boolean filled, float progress) {
        if (!filled) {
            // Тёмный контур пустой сферы (8x8)
            graphics.fill(x + 2, y,     x + 6, y + 1, 0xFF141414);
            graphics.fill(x + 2, y + 7, x + 6, y + 8, 0xFF141414);
            graphics.fill(x + 1, y + 1, x + 2, y + 2, 0xFF141414);
            graphics.fill(x + 6, y + 1, x + 7, y + 2, 0xFF141414);
            graphics.fill(x + 1, y + 6, x + 2, y + 7, 0xFF141414);
            graphics.fill(x + 6, y + 6, x + 7, y + 7, 0xFF141414);
            graphics.fill(x,     y + 2, x + 1, y + 6, 0xFF141414);
            graphics.fill(x + 7, y + 2, x + 8, y + 6, 0xFF141414);

            // Полупрозрачное затемнённое тело
            graphics.fill(x + 2, y + 1, x + 6, y + 2, 0x88242424);
            graphics.fill(x + 1, y + 2, x + 7, y + 6, 0x88242424);
            graphics.fill(x + 2, y + 6, x + 6, y + 7, 0x88242424);

            // Тусклая центральная точка глубины
            graphics.fill(x + 3, y + 3, x + 5, y + 5, 0x88383838);
        } else {
            int outline, main, highlight, shadow;
            if (progress >= 1.00f) {
                outline   = 0xFF382500; // Тёмно-янтарный контур
                main      = 0xFFFFD700; // Золотой
                highlight = 0xFFFFF59D; // Светло-золотой блик
                shadow    = 0xFFB8860B; // Тёмное золото
            } else if (progress >= 0.50f) {
                outline   = 0xFF0B2D30; // Тёмный циан
                main      = 0xFF55E2E9; // Аквамарин
                highlight = 0xFFD5FFFF; // Светлый блик
                shadow    = 0xFF15767A; // Глубокий циан
            } else {
                outline   = 0xFF0A2B0A; // Тёмно-зелёный
                main      = 0xFF4ADE4A; // Ярко-зелёный
                highlight = 0xFFC8FFA0; // Салатовый блик
                shadow    = 0xFF158015; // Тёмно-зелёная тень
            }

            // Контур (8x8)
            graphics.fill(x + 2, y,     x + 6, y + 1, outline);
            graphics.fill(x + 2, y + 7, x + 6, y + 8, outline);
            graphics.fill(x + 1, y + 1, x + 2, y + 2, outline);
            graphics.fill(x + 6, y + 1, x + 7, y + 2, outline);
            graphics.fill(x + 1, y + 6, x + 2, y + 7, outline);
            graphics.fill(x + 6, y + 6, x + 7, y + 7, outline);
            graphics.fill(x,     y + 2, x + 1, y + 6, outline);
            graphics.fill(x + 7, y + 2, x + 8, y + 6, outline);

            // Заполненное тело
            graphics.fill(x + 2, y + 1, x + 6, y + 2, main);
            graphics.fill(x + 1, y + 2, x + 7, y + 5, main);
            graphics.fill(x + 1, y + 5, x + 7, y + 6, shadow);
            graphics.fill(x + 2, y + 6, x + 6, y + 7, shadow);

            // Спекулярный блик (вверху слева)
            graphics.fill(x + 2, y + 2, x + 4, y + 4, highlight);
        }
    }
}
