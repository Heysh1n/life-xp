package com.hs1n.lifeXp_challenge.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Миксин в AnvilMenu для снятия ограничения наковальни «Слишком дорого» (Too Expensive / 40 уровней).
 */
@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu {

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int lifeXp$removeAnvilLimit(int original) {
        return Integer.MAX_VALUE;
    }
}
