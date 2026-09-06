package com.hs1n.lifeXp_challenge.compat.emi;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@EmiEntrypoint
public class LifeXpEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(
                List.of(EmiStack.of(ModItems.LIFE_BOTTLE.get())),
                List.of(Component.translatable("jei.life_xp_challenge.life_bottle.info")),
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "/info/life_bottle")
        ));

        registry.addRecipe(new EmiInfoRecipe(
                List.of(EmiStack.of(ModItems.DYNAMIC_XP_BOTTLE.get())),
                List.of(Component.translatable("jei.life_xp_challenge.dynamic_xp_bottle.info")),
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "/info/dynamic_xp_bottle")
        ));

        registry.addRecipe(new EmiInfoRecipe(
                List.of(EmiStack.of(ModItems.SHIELD_CORE.get())),
                List.of(Component.translatable("jei.life_xp_challenge.shield_core.info")),
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "/info/shield_core")
        ));
    }
}
