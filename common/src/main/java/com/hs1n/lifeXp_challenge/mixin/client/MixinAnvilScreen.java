package com.hs1n.lifeXp_challenge.mixin.client;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Клиентский миксин в AnvilScreen для отключения надписи «Слишком дорого» (Too Expensive).
 */
@Mixin(AnvilScreen.class)
public abstract class MixinAnvilScreen {

    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40))
    private int lifeXp$removeTooExpensiveLabel(int original) {
        return Integer.MAX_VALUE;
    }
}
