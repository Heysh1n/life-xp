package com.hs1n.lifeXp_challenge.mixin.client;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Клиентский миксин для динамической перекраски цифры уровня опыта в HUD.
 * Меняет ванильный зеленый цвет (0xFF80FF20 = -8323296) в зависимости от прогресса капа уровня:
 * - < 0.50f: ванильный зелёный (0xFF80FF20)
 * - 0.50f <= progress < 1.00f: аквамариновый (0xFF55E2E9)
 * - >= 1.00f: золотой (0xFFFFD700)
 */
@Mixin(ContextualBar.class)
public interface MixinGui {

    @ModifyConstant(method = "extractExperienceLevel", constant = @Constant(intValue = -8323296))
    private static int lifeXp$tintExperienceLevel(int originalColor) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return originalColor;
        }

        int maxLevel = LifeXpConfig.INSTANCE.getMaxLevel();
        if (maxLevel <= 0) {
            return originalColor;
        }

        float progress = (float) player.experienceLevel / (float) maxLevel;
        if (progress >= 1.00f) {
            return 0xFFFFD700; // Золотой
        } else if (progress >= 0.50f) {
            return 0xFF55E2E9; // Аквамариновый
        } else {
            return -8323296; // 0xFF80FF20 Ванильный зелёный
        }
    }
}
