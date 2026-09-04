package com.hs1n.lifeXp_challenge.fabric.client;

import com.hs1n.lifeXp_challenge.client.LifeXpChallengeClient;
import net.fabricmc.api.ClientModInitializer;

public class LifeXpChallengeClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LifeXpChallengeClient.init();
    }
}
