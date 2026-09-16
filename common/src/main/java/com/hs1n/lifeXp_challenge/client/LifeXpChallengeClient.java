package com.hs1n.lifeXp_challenge.client;

import com.hs1n.lifeXp_challenge.client.hud.LifeXpHudOverlay;
import com.hs1n.lifeXp_challenge.network.LifeXpNetworking;
import dev.architectury.event.events.client.ClientGuiEvent;

public class LifeXpChallengeClient {

    public static void init() {
        // Регистрируем получатель пакетов Kill Streak
        LifeXpNetworking.registerClientReceiver();

        // Регистрируем HUD-оверлей
        ClientGuiEvent.RENDER_HUD.register(LifeXpHudOverlay::render);
        LifeXpVisualsClient.register();

        ClientGuiEvent.SET_SCREEN.register(screen -> {
            if (screen instanceof net.minecraft.client.gui.screens.PauseScreen && net.minecraft.client.Minecraft.getInstance().player != null) {
                LifeXpNetworking.sendForceSyncAttributes();
            }
            return dev.architectury.event.CompoundEventResult.pass();
        });
    }
}
