package com.hs1n.lifeXp_challenge.neoforge;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.client.LifeXpChallengeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.hs1n.lifeXp_challenge.client.config.LifeXpConfigScreen;

@Mod(LifeXpChallenge.MOD_ID)
public class LifeXpChallengeNeoForge {
    public LifeXpChallengeNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LifeXpChallenge.init();
        
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> LifeXpConfigScreen.create(parent));
        
        modEventBus.addListener(this::onClientSetup);
    }
    
    private void onClientSetup(final FMLClientSetupEvent event) {
        LifeXpChallengeClient.init();
    }
}
