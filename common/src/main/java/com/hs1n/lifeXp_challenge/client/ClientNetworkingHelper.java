package com.hs1n.lifeXp_challenge.client;

import com.hs1n.lifeXp_challenge.client.gui.PresetSelectionScreen;
import net.minecraft.client.Minecraft;

public final class ClientNetworkingHelper {
    private ClientNetworkingHelper() {}

    public static void openPresetScreen() {
        Minecraft mc = Minecraft.getInstance();
//? if >= 26.1 {
        mc.setScreenAndShow(new PresetSelectionScreen());
//?} else {
      /*mc.setScreen(new PresetSelectionScreen());*/
//?}
    }
}
