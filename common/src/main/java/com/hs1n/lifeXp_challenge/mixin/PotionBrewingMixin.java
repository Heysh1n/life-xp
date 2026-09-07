package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    @Inject(method = "addVanillaMixes", at = @At("TAIL"))
    private static void lifeXp$addCustomPotionMixes(PotionBrewing.Builder builder, CallbackInfo ci) {
        // Awkward Potion + shield_core = Зелье XP-Щита (3:00)
        builder.addMix(Potions.AWKWARD, ModItems.SHIELD_CORE.get(), ModPotions.XP_SHIELD);
        // XP-Щит + Redstone = Долгое зелье XP-Щита (8:00)
        builder.addMix(ModPotions.XP_SHIELD, Items.REDSTONE, ModPotions.LONG_XP_SHIELD);
        // XP-Щит + Glowstone = Сильное зелье XP-Щита II (1:30)
        builder.addMix(ModPotions.XP_SHIELD, Items.GLOWSTONE_DUST, ModPotions.STRONG_XP_SHIELD);
        // Сильное зелье XP-Щита II + Звезда Незера = Ультра зелье XP-Щита III (1:00)
        builder.addMix(ModPotions.STRONG_XP_SHIELD, Items.NETHER_STAR, ModPotions.ULTRA_XP_SHIELD);
    }
}
