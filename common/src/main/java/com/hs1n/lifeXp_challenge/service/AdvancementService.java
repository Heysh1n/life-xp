package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModMobEffects;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class AdvancementService {

//? if >= 26.1 {
    public static final Identifier LIFE_OR_DEATH =
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/life_or_death");
    public static final Identifier MINER_VEIN =
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/miner_vein");
    public static final Identifier MASTER_ANGLER =
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/master_angler");
//?} else {
  /*public static final ResourceLocation LIFE_OR_DEATH =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/life_or_death");
    public static final ResourceLocation MINER_VEIN =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/miner_vein");
    public static final ResourceLocation MASTER_ANGLER =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "main/master_angler");*/
//?}

    private static final String TAG_PREFIX_ORE_XP = "lifexp_ore_xp_";
    private static final String TAG_PREFIX_FISH_XP = "lifexp_fish_xp_";

    public static void checkLifeOrDeath(ServerPlayer player) {
        if (!player.isAlive() || player.getHealth() > 1.0f) {
            return;
        }
        MobEffectInstance effect = player.getEffect(ModMobEffects.XP_SHIELD);
        if (effect != null && effect.getAmplifier() >= 2) {
            grantAdvancement(player, LIFE_OR_DEATH);
        }
    }

    public static void addOreXp(ServerPlayer player, int amount) {
        if (amount <= 0 || isAdvancementDone(player, MINER_VEIN)) return;
        int current = getPlayerTagCount(player, TAG_PREFIX_ORE_XP) + amount;
        setPlayerTagCount(player, TAG_PREFIX_ORE_XP, current);
        if (current >= 1000) {
            grantAdvancement(player, MINER_VEIN);
        }
    }

    public static void addFishingXp(ServerPlayer player, int amount) {
        if (amount <= 0 || isAdvancementDone(player, MASTER_ANGLER)) return;
        int current = getPlayerTagCount(player, TAG_PREFIX_FISH_XP) + amount;
        setPlayerTagCount(player, TAG_PREFIX_FISH_XP, current);
        if (current >= 500) {
            grantAdvancement(player, MASTER_ANGLER);
        }
    }

//? if >= 26.1 {
    public static boolean isAdvancementDone(ServerPlayer player, Identifier id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement == null) return false;
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static void grantAdvancement(ServerPlayer player, Identifier advancementId) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(advancementId);
//?} else {
  /*public static boolean isAdvancementDone(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement == null) return false;
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static void grantAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(advancementId);*/
//?}
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    private static int getPlayerTagCount(ServerPlayer player, String prefix) {
//? if >= 26.1 {
        for (String tag : player.entityTags()) {
//?} else {
      /*for (String tag : player.getTags()) {*/
//?}
            if (tag.startsWith(prefix)) {
                try {
                    return Integer.parseInt(tag.substring(prefix.length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private static void setPlayerTagCount(ServerPlayer player, String prefix, int count) {
//? if >= 26.1 {
        player.entityTags().removeIf(tag -> tag.startsWith(prefix));
//?} else {
      /*player.getTags().removeIf(tag -> tag.startsWith(prefix));*/
//?}
        player.addTag(prefix + count);
    }
}
