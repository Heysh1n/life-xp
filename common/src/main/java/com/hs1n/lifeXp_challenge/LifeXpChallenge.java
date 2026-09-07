package com.hs1n.lifeXp_challenge;

import com.hs1n.lifeXp_challenge.command.LifeXpCommand;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.registry.ModCreativeTabs;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.hs1n.lifeXp_challenge.service.LifeBottleService;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.EntityEvent;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifeXpChallenge {
    public static final String MOD_ID = "life_xp_challenge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LifeXpConfig.load();
        
        ModItems.ITEMS.register();
        com.hs1n.lifeXp_challenge.registry.ModMobEffects.MOB_EFFECTS.register();
        com.hs1n.lifeXp_challenge.registry.ModPotions.POTIONS.register();
        ModCreativeTabs.CREATIVE_MODE_TABS.register();

        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.registerServerReceiver();

        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> {
            LifeXpCommand.register(dispatcher);
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            AttributeService.recalculate(player);
            if (!player.getTags().contains(LifeXpCommand.PRESET_TAG)) {
                com.hs1n.lifeXp_challenge.network.LifeXpNetworking.sendOpenPresetScreen(player);
            }
        });

        PlayerEvent.PLAYER_RESPAWN.register((net.minecraft.server.level.ServerPlayer player, boolean conqueredEnd, net.minecraft.world.entity.Entity.RemovalReason removalReason) -> {
            AttributeService.recalculate(player);
            if (!conqueredEnd) {
                KillStreakService.resetStreak(player);
            }
        });

        PlayerEvent.CHANGE_DIMENSION.register((net.minecraft.server.level.ServerPlayer player, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> oldLevel, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> newLevel) -> {
            AttributeService.recalculate(player);
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                com.hs1n.lifeXp_challenge.service.DeathTaxService.onPlayerDeath(player, source);
            }
            if (source.getEntity() instanceof ServerPlayer player && entity != player) {
                KillStreakService.onMobKilled(player);
            }
            return dev.architectury.event.EventResult.pass();
        });

        dev.architectury.registry.CreativeTabRegistry.append(ModCreativeTabs.LIFE_XP_TAB, ModItems.DYNAMIC_XP_BOTTLE, ModItems.LIFE_BOTTLE, ModItems.SHIELD_CORE);
    }
}
