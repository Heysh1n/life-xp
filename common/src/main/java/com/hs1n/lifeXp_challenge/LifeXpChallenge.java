package com.hs1n.lifeXp_challenge;

import com.hs1n.lifeXp_challenge.command.LifeXpCommand;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.registry.ModCreativeTabs;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.hs1n.lifeXp_challenge.service.CropHarvestService;
import com.hs1n.lifeXp_challenge.service.LifeBottleService;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
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
        com.hs1n.lifeXp_challenge.registry.ModDataComponents.init();
        com.hs1n.lifeXp_challenge.registry.ModMobEffects.init();
        com.hs1n.lifeXp_challenge.registry.ModPotions.init();
        com.hs1n.lifeXp_challenge.registry.ModLootRegistries.init();
        ModCreativeTabs.CREATIVE_MODE_TABS.register();

        com.hs1n.lifeXp_challenge.service.XpHarvesterService.init();
        com.hs1n.lifeXp_challenge.service.FishingLootService.init();

        com.hs1n.lifeXp_challenge.network.LifeXpNetworking.registerServerReceiver();

        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> {
            LifeXpCommand.register(dispatcher);
        });

        // Right-click crop harvest & AoE hoe
//? if == 1.21.4 {
        /*InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) ->
                CropHarvestService.onRightClickBlock(player, hand, pos, face).asMinecraft()
        );*/
//?} else {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(CropHarvestService::onRightClickBlock);
//?}

        PlayerEvent.PLAYER_JOIN.register(player -> {
            AttributeService.recalculate(player);
            KillStreakService.onPlayerJoin(player);
//? if >= 26.1 {
            if (!LifeXpConfig.INSTANCE.isPresetInitialized() && !player.entityTags().contains(LifeXpCommand.PRESET_TAG)) {
//?} else {
          /*if (!LifeXpConfig.INSTANCE.isPresetInitialized() && !player.getTags().contains(LifeXpCommand.PRESET_TAG)) {*/
//?}
                com.hs1n.lifeXp_challenge.network.LifeXpNetworking.sendOpenPresetScreen(player);
            }
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> {
            AttributeService.recalculate(player);
            if (!conqueredEnd) {
                KillStreakService.resetStreak(player);
            }
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> {
            AttributeService.recalculate(player);
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                com.hs1n.lifeXp_challenge.service.DeathTaxService.onPlayerDeath(player, source);
                KillStreakService.resetStreak(player);
            }
            if (source.getEntity() instanceof ServerPlayer player && entity != player) {
                KillStreakService.onMobKilled(player);
            }
            return dev.architectury.event.EventResult.pass();
        });

        dev.architectury.registry.CreativeTabRegistry.append(ModCreativeTabs.LIFE_XP_TAB, ModItems.DYNAMIC_XP_BOTTLE, ModItems.LIFE_BOTTLE, ModItems.SHIELD_CORE);
    }
}
