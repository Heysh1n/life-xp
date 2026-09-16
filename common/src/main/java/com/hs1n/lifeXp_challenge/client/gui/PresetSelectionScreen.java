package com.hs1n.lifeXp_challenge.client.gui;

import com.hs1n.lifeXp_challenge.network.LifeXpNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PresetSelectionScreen extends Screen {

    public record PresetCard(
            String id,
            Component title,
            Component badge,
            Component description,
            Component stats,
            int accentColor
    ) {}

    private static final int CARD_WIDTH = 320;
    private static final int CARD_HEIGHT = 200;

    // Strict ARGB color constants (0xAARRGGBB)
    private static final int COLOR_BG_GRADIENT_TOP = 0x90000000;
    private static final int COLOR_BG_GRADIENT_BOTTOM = 0xC0000000;
    private static final int COLOR_CARD_BG = 0xE0121216;
    private static final int COLOR_CARD_BORDER = 0xFF4A4D59;
    private static final int COLOR_SEPARATOR = 0xFF3A3D48;

    private static final int COLOR_TITLE = 0xFFFFFFFF;
    private static final int COLOR_SUBTITLE = 0xFFAAAAAA;
    private static final int COLOR_BADGE = 0xFFAAAAAA;
    private static final int COLOR_DESCRIPTION = 0xFFCCCCCC;
    private static final int COLOR_MODIFIERS_HEADER = 0xFFFFFF55;
    private static final int COLOR_STATS = 0xFFEEEEEE;
    private static final int COLOR_PAGE_INDICATOR = 0xFFAAAAAA;

    private final List<PresetCard> presets = new ArrayList<>();
    private int currentIndex = 0;

    private Button prevButton;
    private Button nextButton;
    private Button selectButton;

    public PresetSelectionScreen() {
        super(Component.translatable("lifexp.gui.preset.title"));
        initPresetList();
    }

    private void initPresetList() {
        presets.add(new PresetCard(
                "core",
                Component.translatable("lifexp.config.preset.core"),
                Component.translatable("lifexp.gui.preset.badge.hardcore"),
                Component.translatable("lifexp.config.preset.core.desc"),
                Component.translatable("lifexp.gui.preset.core.stats"),
                0xFFFFD700
        ));
        presets.add(new PresetCard(
                "vanilla_plus",
                Component.translatable("lifexp.config.preset.vanilla_plus"),
                Component.translatable("lifexp.gui.preset.badge.balanced"),
                Component.translatable("lifexp.config.preset.vanilla_plus.desc"),
                Component.translatable("lifexp.gui.preset.vanilla_plus.stats"),
                0xFF55FF55
        ));
        presets.add(new PresetCard(
                "real_hardcore",
                Component.translatable("lifexp.config.preset.real_hardcore"),
                Component.translatable("lifexp.gui.preset.badge.extreme"),
                Component.translatable("lifexp.config.preset.real_hardcore.desc"),
                Component.translatable("lifexp.gui.preset.real_hardcore.stats"),
                0xFFFF5555
        ));
        presets.add(new PresetCard(
                "core_no_streaks",
                Component.translatable("lifexp.config.preset.core_no_streaks"),
                Component.translatable("lifexp.gui.preset.badge.classic_hardcore"),
                Component.translatable("lifexp.config.preset.core_no_streaks.desc"),
                Component.translatable("lifexp.gui.preset.core_no_streaks.stats"),
                0xFFFFAA00
        ));
        presets.add(new PresetCard(
                "purist",
                Component.translatable("lifexp.config.preset.purist"),
                Component.translatable("lifexp.gui.preset.badge.purist"),
                Component.translatable("lifexp.config.preset.purist.desc"),
                Component.translatable("lifexp.gui.preset.purist.stats"),
                0xFF55FFFF
        ));
        presets.add(new PresetCard(
                "baby_mode",
                Component.translatable("lifexp.config.preset.baby_mode"),
                Component.translatable("lifexp.gui.preset.badge.casual"),
                Component.translatable("lifexp.config.preset.baby_mode.desc"),
                Component.translatable("lifexp.gui.preset.baby_mode.stats"),
                0xFFFF55FF
        ));
    }

    @Override
    protected void init() {
        super.init();

        int cardX = (this.width - CARD_WIDTH) / 2;
        int cardY = (this.height - CARD_HEIGHT) / 2 + 10;
        int buttonY = cardY + CARD_HEIGHT - 32;

        this.prevButton = this.addRenderableWidget(
                Button.builder(Component.literal("◀"), btn -> {
                    if (currentIndex > 0) {
                        currentIndex--;
                    } else {
                        currentIndex = presets.size() - 1;
                    }
                }).bounds(cardX + 16, buttonY, 36, 20).build()
        );

        this.selectButton = this.addRenderableWidget(
                Button.builder(Component.translatable("lifexp.gui.preset.select").withStyle(ChatFormatting.BOLD), btn -> {
                    PresetCard current = presets.get(currentIndex);
                    LifeXpNetworking.sendChoosePreset(current.id());
                    this.onClose();
                }).bounds(cardX + 60, buttonY, CARD_WIDTH - 120, 20).build()
        );

        this.nextButton = this.addRenderableWidget(
                Button.builder(Component.literal("▶"), btn -> {
                    if (currentIndex < presets.size() - 1) {
                        currentIndex++;
                    } else {
                        currentIndex = 0;
                    }
                }).bounds(cardX + CARD_WIDTH - 52, buttonY, 36, 20).build()
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Semi-transparent dark background gradient
        guiGraphics.fillGradient(0, 0, this.width, this.height, COLOR_BG_GRADIENT_TOP, COLOR_BG_GRADIENT_BOTTOM);

        int cardX = (this.width - CARD_WIDTH) / 2;
        int cardY = (this.height - CARD_HEIGHT) / 2 + 10;

        // 2. Card background panel and border
        guiGraphics.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + CARD_HEIGHT, COLOR_CARD_BG);
        guiGraphics.outline(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, COLOR_CARD_BORDER);

        // 3. Advance to next stratum to guarantee text and widgets render strictly above the background
        guiGraphics.nextStratum();

        // 4. Screen titles
        guiGraphics.centeredText(this.font, Component.translatable("lifexp.gui.preset.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), this.width / 2, cardY - 24, COLOR_TITLE);
        guiGraphics.centeredText(this.font, Component.translatable("lifexp.gui.preset.subtitle").withStyle(ChatFormatting.GRAY), this.width / 2, cardY - 12, COLOR_SUBTITLE);

        // 5. Card contents
        if (!presets.isEmpty()) {
            PresetCard current = presets.get(currentIndex);

            // Card Header: Preset Title
            guiGraphics.centeredText(this.font, current.title(), this.width / 2, cardY + 12, current.accentColor());

            // Badge
            guiGraphics.centeredText(this.font, current.badge(), this.width / 2, cardY + 26, COLOR_BADGE);

            // Separator line
            guiGraphics.fill(cardX + 16, cardY + 38, cardX + CARD_WIDTH - 16, cardY + 39, COLOR_SEPARATOR);

            // Description
            guiGraphics.textWithWordWrap(this.font, current.description(), cardX + 20, cardY + 46, CARD_WIDTH - 40, COLOR_DESCRIPTION);

            // Modifiers / Stats section
            guiGraphics.text(this.font, Component.translatable("lifexp.gui.preset.modifiers_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE), cardX + 20, cardY + 86, COLOR_MODIFIERS_HEADER);
            guiGraphics.textWithWordWrap(this.font, current.stats(), cardX + 20, cardY + 100, CARD_WIDTH - 40, COLOR_STATS);

            // Page indicator (e.g. 1 / 6)
            String pageStr = (currentIndex + 1) + " / " + presets.size();
            guiGraphics.centeredText(this.font, Component.literal(pageStr).withStyle(ChatFormatting.GRAY), this.width / 2, cardY + CARD_HEIGHT - 44, COLOR_PAGE_INDICATOR);
        }

        // 6. Render widgets (buttons) on top of the card
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }
}
