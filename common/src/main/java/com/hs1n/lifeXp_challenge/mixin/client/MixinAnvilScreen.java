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

//? if >= 26.1 {
    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
//?} else {
  /*@ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40))*/
//?}
    private int lifeXp$removeTooExpensiveLabel(int original) {
        return Integer.MAX_VALUE;
    }
}
