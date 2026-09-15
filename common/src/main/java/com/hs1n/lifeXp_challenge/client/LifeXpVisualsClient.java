package com.hs1n.lifeXp_challenge.client;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиентские визуальные эффекты, заменяющие легаси-туман (FogRenderer mixin).
 * <p>
 * 1. Динамическая виньетка (HUD overlay) — края экрана затемняются при низком XP.
 * 2. Частицы пепла (ASH) — спавнятся вокруг игрока при очень низком уровне опыта.
 */
public final class LifeXpVisualsClient {

    private static final ResourceLocation VIGNETTE =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "textures/gui/vignette.png");

    private LifeXpVisualsClient() {}

    /** Вызывается из {@link LifeXpChallengeClient#init()} один раз. */
    public static void register() {
        // HUD-оверлей виньетки
        ClientGuiEvent.RENDER_HUD.register(LifeXpVisualsClient::renderVignette);
        // Тик для спавна частиц
        ClientTickEvent.CLIENT_POST.register(LifeXpVisualsClient::onClientTick);
    }

    // ═══════════════════════════════════════════════════════════
    //  Виньетка
    // ═══════════════════════════════════════════════════════════

    private static void renderVignette(GuiGraphics guiGraphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        if (player.isDeadOrDying() || player.isSpectator() || player.isCreative()) return;

        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int maxLevel = Math.max(1, config.getMaxLevel());
        float currentXp = player.experienceLevel;

        // alpha = 1.0 при 0 XP, 0.0 при ≥ 50% maxLevel
        float alpha = Math.clamp(1.0F - (currentXp / (maxLevel * 0.5F)), 0.0F, 1.0F);
        if (alpha <= 0.001F) return; // Полностью прозрачна — пропускаем рендер

        int screenWidth  = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(VIGNETTE, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Сброс цвета
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════════
    //  Частицы пепла
    // ═══════════════════════════════════════════════════════════

    private static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;
        if (!player.isLocalPlayer() || player.isDeadOrDying() || player.isSpectator() || player.isCreative()) return;

        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int maxLevel = Math.max(1, config.getMaxLevel());
        float threshold = maxLevel * 0.1F; // 10% от капа

        if (player.experienceLevel >= threshold) return;

        // 30% шанс каждый тик
        if (player.getRandom().nextFloat() > 0.30F) return;

        int count = 1 + player.getRandom().nextInt(2); // 1-2 частицы
        for (int i = 0; i < count; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 3.0; // радиус ~1.5 блоков
            double offsetY = player.getRandom().nextDouble() * 2.0;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 3.0;

            mc.level.addParticle(
                    ParticleTypes.ASH,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    0.0, 0.0, 0.0
            );
        }
    }
}
