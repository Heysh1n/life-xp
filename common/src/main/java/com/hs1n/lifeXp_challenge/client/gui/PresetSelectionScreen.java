package com.hs1n.lifeXp_challenge.client.gui;

import com.hs1n.lifeXp_challenge.network.LifeXpNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
                0xFFD700
        ));
        presets.add(new PresetCard(
                "vanilla_plus",
                Component.translatable("lifexp.config.preset.vanilla_plus"),
                Component.translatable("lifexp.gui.preset.badge.balanced"),
                Component.translatable("lifexp.config.preset.vanilla_plus.desc"),
                Component.translatable("lifexp.gui.preset.vanilla_plus.stats"),
                0x55FF55
        ));
        presets.add(new PresetCard(
                "real_hardcore",
                Component.translatable("lifexp.config.preset.real_hardcore"),
                Component.translatable("lifexp.gui.preset.badge.extreme"),
                Component.translatable("lifexp.config.preset.real_hardcore.desc"),
                Component.translatable("lifexp.gui.preset.real_hardcore.stats"),
                0xFF5555
        ));
        presets.add(new PresetCard(
                "core_no_streaks",
                Component.translatable("lifexp.config.preset.core_no_streaks"),
                Component.translatable("lifexp.gui.preset.badge.classic_hardcore"),
                Component.translatable("lifexp.config.preset.core_no_streaks.desc"),
                Component.translatable("lifexp.gui.preset.core_no_streaks.stats"),
                0xFFAA00
        ));
        presets.add(new PresetCard(
                "purist",
                Component.translatable("lifexp.config.preset.purist"),
                Component.translatable("lifexp.gui.preset.badge.purist"),
                Component.translatable("lifexp.config.preset.purist.desc"),
                Component.translatable("lifexp.gui.preset.purist.stats"),
                0x55FFFF
        ));
        presets.add(new PresetCard(
                "baby_mode",
                Component.translatable("lifexp.config.preset.baby_mode"),
                Component.translatable("lifexp.gui.preset.badge.casual"),
                Component.translatable("lifexp.config.preset.baby_mode.desc"),
                Component.translatable("lifexp.gui.preset.baby_mode.stats"),
                0xFF55FF
        ));
    }

    @Override
    protected void init() {
        super.init();

        int cardWidth = 320;
        int cardHeight = 200;
        int cardX = (this.width - cardWidth) / 2;
        int cardY = (this.height - cardHeight) / 2 + 10;

        int buttonY = cardY + cardHeight - 32;

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
                }).bounds(cardX + 60, buttonY, cardWidth - 120, 20).build()
        );

        this.nextButton = this.addRenderableWidget(
                Button.builder(Component.literal("▶"), btn -> {
                    if (currentIndex < presets.size() - 1) {
                        currentIndex++;
                    } else {
                        currentIndex = 0;
                    }
                }).bounds(cardX + cardWidth - 52, buttonY, 36, 20).build()
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Simple dark vignette/gradient without vanilla blur shaders
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x90000000, 0xC0000000);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int cardWidth = 320;
        int cardHeight = 200;
        int cardX = (this.width - cardWidth) / 2;
        int cardY = (this.height - cardHeight) / 2 + 10;

        // Card body
        guiGraphics.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, 0xE0121216);
        guiGraphics.renderOutline(cardX, cardY, cardWidth, cardHeight, 0xFF4A4D59);

        // Titles
        guiGraphics.drawCenteredString(this.font, Component.translatable("lifexp.gui.preset.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), this.width / 2, cardY - 24, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("lifexp.gui.preset.subtitle").withStyle(ChatFormatting.GRAY), this.width / 2, cardY - 12, 0xAAAAAA);

        if (!presets.isEmpty()) {
            PresetCard current = presets.get(currentIndex);

            // Card Header: Preset Title
            guiGraphics.drawCenteredString(this.font, current.title(), this.width / 2, cardY + 12, current.accentColor());

            // Badge
            guiGraphics.drawCenteredString(this.font, current.badge(), this.width / 2, cardY + 26, 0xAAAAAA);

            // Separator line
            guiGraphics.hLine(cardX + 16, cardX + cardWidth - 16, cardY + 38, 0xFF3A3D48);

            // Description
            guiGraphics.drawWordWrap(this.font, current.description(), cardX + 20, cardY + 46, cardWidth - 40, 0xCCCCCC);

            // Modifiers / Stats section
            guiGraphics.drawString(this.font, Component.translatable("lifexp.gui.preset.modifiers_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE), cardX + 20, cardY + 86, 0xFFFF55);
            guiGraphics.drawWordWrap(this.font, current.stats(), cardX + 20, cardY + 100, cardWidth - 40, 0xEEEEEE);

            // Page indicator (e.g. 1 / 6)
            String pageStr = (currentIndex + 1) + " / " + presets.size();
            guiGraphics.drawCenteredString(this.font, Component.literal(pageStr).withStyle(ChatFormatting.DARK_GRAY), this.width / 2, cardY + cardHeight - 44, 0x888888);
        }
    }
}
