package com.hs1n.lifeXp_challenge.client.config;

import com.hs1n.lifeXp_challenge.client.config.LifeXpConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class LifeXpModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LifeXpConfigScreen::create;
    }
}
