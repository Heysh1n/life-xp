package com.hs1n.lifeXp_challenge.client;

import com.hs1n.lifeXp_challenge.client.hud.LifeXpHudOverlay;
import com.hs1n.lifeXp_challenge.item.LifeBottleItem;
import com.hs1n.lifeXp_challenge.network.LifeXpNetworking;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;

public class LifeXpChallengeClient {

    public static void init() {
        // Регистрируем получатель пакетов Kill Streak
        LifeXpNetworking.registerClientReceiver();

        // Регистрируем HUD-оверлей
        ClientGuiEvent.RENDER_HUD.register(LifeXpHudOverlay::render);
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if (tintIndex != 0) return -1;
            
            int stored = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getInt("StoredXp");
            if (stored <= 0) return 0xA0FF3C; // 160, 255, 60 (default color)
            
            float ratio = Math.min((float) stored / 3000f, 1f);
            int r = (int) (160 - 150 * ratio); // 160 -> 10
            int g = (int) (255 - 175 * ratio); // 255 -> 80
            int b = (int) (60 - 40 * ratio);   // 60 -> 20
            
            return (r << 16) | (g << 8) | b;
        }, ModItems.DYNAMIC_XP_BOTTLE.get());

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if (tintIndex != 0) return -1;
            return LifeBottleItem.hasSavedInventory(stack) ? 0xF59E0B : 0xDC2626;
        }, ModItems.LIFE_BOTTLE.get());
    }
}
