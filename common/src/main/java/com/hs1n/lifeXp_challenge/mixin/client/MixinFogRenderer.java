package com.hs1n.lifeXp_challenge.mixin.client;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Клиентский миксин для динамического тумана.
 * После установки ванильного тумана перезаписывает fogStart и fogEnd
 * значениями, интерполированными по уровню опыта игрока.
 * При малой дистанции (<15 блоков) делает цвет тумана чёрным.
 */
@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void lifeXp$overrideFog(Camera camera, FogRenderer.FogMode fogMode,
                                            float viewDistance, boolean thickFog,
                                            float partialTick, CallbackInfo ci) {
        LifeXpConfig config = LifeXpConfig.INSTANCE;
        if (!config.isEnableCustomFog()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int level    = player.experienceLevel;
        int maxLevel = config.getMaxLevel();

        double fogDist = AttributeService.interpolate(
                config.getFogStartDistance(),
                config.getFogMidDistance(),
                config.getFogEndDistance(),
                level, maxLevel
        );

        // Не даём дистанции быть меньше 1 блока
        if (fogDist < 1.0) fogDist = 1.0;

        float fogEnd   = (float) fogDist;
        float fogStart = fogEnd * 0.1f; // начало тумана — 10% от дистанции

        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);

        // При дистанции < 15 блоков — чёрный туман вместо «молока»
        if (fogDist < 15.0) {
            RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
    }
}
