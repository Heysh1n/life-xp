package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;

public class MilestoneService {
    private static final Identifier MILESTONE_10 = Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_10");
    private static final Identifier MILESTONE_50 = Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_50");
    private static final Identifier MILESTONE_100 = Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/milestone_100");

    public static void checkMilestones(ServerPlayer player) {
        int maxLevel = LifeXpConfig.INSTANCE.getMaxLevel();
        if (maxLevel <= 0) return;

        float progress = (float) player.experienceLevel / (float) maxLevel;

        if (progress >= 0.10f) grantAdvancement(player, MILESTONE_10, 10);
        if (progress >= 0.50f) grantAdvancement(player, MILESTONE_50, 50);
        if (progress >= 1.00f) grantAdvancement(player, MILESTONE_100, 100);
    }

    private static void grantAdvancement(ServerPlayer player, Identifier advancementId, int percent) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(advancementId);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
                
                // Styling
                ChatFormatting highlight = ChatFormatting.AQUA;
                if (percent == 50) highlight = ChatFormatting.DARK_PURPLE;
                if (percent == 100) highlight = ChatFormatting.GOLD;

                String flavorKey = "lifexp.milestone.flavor." + percent;
                
                Component message = Component.literal("[✦ LifeXP] ").withStyle(highlight, ChatFormatting.BOLD)
                    .append(player.getDisplayName().copy().withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" достиг " + percent + "% капа! ").withStyle(highlight))
                    .append(Component.translatable(flavorKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

                player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);

                // Play sounds
                if (percent >= 50) {
                    player.connection.send(new ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), 
                        SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 1.0f, percent == 100 ? 0.8f : 1.2f, player.getRandom().nextLong()
                    ));
                } else {
                    player.connection.send(new ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EXPERIENCE_ORB_PICKUP), 
                        SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 1.0f, 0.5f, player.getRandom().nextLong()
                    ));
                }
            }
        }
    }
}
