package com.hs1n.lifeXp_challenge.compat.jei;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class LifeXpJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                ModItems.LIFE_BOTTLE.get(),
                Component.translatable("jei.life_xp_challenge.life_bottle.info")
        );
        registration.addIngredientInfo(
                ModItems.DYNAMIC_XP_BOTTLE.get(),
                Component.translatable("jei.life_xp_challenge.dynamic_xp_bottle.info")
        );
        registration.addIngredientInfo(
                ModItems.SHIELD_CORE.get(),
                Component.translatable("jei.life_xp_challenge.shield_core.info")
        );
    }
}
