package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

public class MilestoneService {
    private static final ResourceLocation LEVEL_100 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/level_100");
    private static final ResourceLocation LEVEL_250 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/level_250");
    private static final ResourceLocation LEVEL_500 = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/level_500");

    public static void checkMilestones(ServerPlayer player) {
        int level = player.experienceLevel;
        if (level >= 100) grantAdvancement(player, LEVEL_100);
        if (level >= 250) grantAdvancement(player, LEVEL_250);
        if (level >= 500) grantAdvancement(player, LEVEL_500);
    }

    private static void grantAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }
}
