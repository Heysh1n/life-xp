package com.hs1n.lifeXp_challenge.client;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.item.DynamicXpBottleItem;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Клиентские визуальные эффекты LIFE-XP:
 * 1. Динамическая виньетка (HUD overlay) — затемнение при опыте < 50% от капа.
 * 2. Частицы пепла (ASH) — спавн вокруг игрока при уровне < 10% от капа.
 * 3. Обязательное отключение виньетки и частиц пепла, если игрок isDeadOrDying().
 * 4. ARGB-маска 0xFF000000 для корректного рендера жидкостей бутылок.
 */
public final class LifeXpVisualsClient {

    public static final Identifier VIGNETTE =
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "textures/gui/vignette.png");

    public static final int ARGB_MASK = 0xFF000000;

    private LifeXpVisualsClient() {}

    /** Вызывается из {@link LifeXpChallengeClient#init()} один раз. */
    public static void register() {
        // HUD-оверлей виньетки
        ClientGuiEvent.RENDER_HUD.register(LifeXpVisualsClient::renderVignette);
        // Тик для спавна частиц
        ClientTickEvent.CLIENT_POST.register(LifeXpVisualsClient::onClientTick);
    }

    /**
     * Предоставляет цвет жидкости для пузырьков с ARGB-маской 0xFF000000.
     */
    public static int getBottleFluidColor(ItemStack stack) {
        return ARGB_MASK | (DynamicXpBottleItem.getXpColor(stack) & 0x00FFFFFF);
    }

    /**
     * Предоставляет цвет для ItemColorHandler / ItemTintSource с ARGB-маской.
     */
    public static int getBottleColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return getBottleFluidColor(stack);
        }
        return ARGB_MASK | 0x00FFFFFF;
    }

    // ═══════════════════════════════════════════════════════════
    //  Виньетка
    // ═══════════════════════════════════════════════════════════

    private static void renderVignette(GuiGraphicsExtractor guiGraphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) return;

        LocalPlayer player = mc.player;
        if (player.isSpectator() || player.isCreative() || player.isDeadOrDying()) return;

        LifeXpConfig config = LifeXpConfig.INSTANCE;
        int maxLevel = Math.max(1, config.getMaxLevel());
        float currentXp = player.experienceLevel;

        // Начинает затемнять экран при опыте < 50% от капа, расчет альфы строго ограничен [0.0, 1.0].
        float halfCap = maxLevel * 0.5F;
        float alpha = Math.clamp(1.0F - (currentXp / halfCap), 0.0F, 1.0F);
        if (alpha <= 0.001F) return; // Полностью прозрачна — пропускаем рендер

        int screenWidth  = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int alphaInt = Math.clamp((int) (alpha * 255.0F), 0, 255);
        int color = (alphaInt << 24) | 0x00FFFFFF;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight, color);
    }

    // ═══════════════════════════════════════════════════════════
    //  Частицы пепла
    // ═══════════════════════════════════════════════════════════

    private static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;
        if (!player.isLocalPlayer() || player.isSpectator() || player.isCreative() || player.isDeadOrDying()) return;

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
