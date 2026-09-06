package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MilestoneService {
    private static final ResourceLocation MILESTONE_10 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_10");
    private static final ResourceLocation MILESTONE_50 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_50");
    private static final ResourceLocation MILESTONE_100 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_100");

    public static void checkMilestones(ServerPlayer player) {
        int maxLevel = LifeXpConfig.INSTANCE.getMaxLevel();
        if (maxLevel <= 0) return;

        float progress = (float) player.experienceLevel / (float) maxLevel;

        if (progress >= 0.10f) grantAdvancement(player, MILESTONE_10, "lifexp.milestone.10");
        if (progress >= 0.50f) grantAdvancement(player, MILESTONE_50, "lifexp.milestone.50");
        if (progress >= 1.00f) grantAdvancement(player, MILESTONE_100, "lifexp.milestone.100");
    }

    private static void grantAdvancement(ServerPlayer player, ResourceLocation advancementId, String translationKey) {
        AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
                player.sendSystemMessage(Component.translatable(translationKey));
            }
        }
    }
}
