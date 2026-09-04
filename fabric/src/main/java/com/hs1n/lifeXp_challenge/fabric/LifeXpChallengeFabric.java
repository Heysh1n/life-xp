package com.hs1n.lifeXp_challenge.fabric;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import net.fabricmc.api.ModInitializer;

public class LifeXpChallengeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LifeXpChallenge.init();
    }
}
