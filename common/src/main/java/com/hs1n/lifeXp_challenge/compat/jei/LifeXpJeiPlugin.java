package com.hs1n.lifeXp_challenge.compat.jei;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.registry.ModPotions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class LifeXpJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // ── Информационные вкладки предметов ──
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

        // ── Рецепты варки зельеварения в JEI ──
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiBrewingRecipe> brewingRecipes = new ArrayList<>();

        // 1. Awkward Potion + shield_core = Potion of XP Shield I (3:00)
        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        ItemStack xpShield1 = PotionContents.createItemStack(Items.POTION, ModPotions.XP_SHIELD);
        brewingRecipes.add(factory.createBrewingRecipe(
                List.of(ModItems.SHIELD_CORE.get().getDefaultInstance()),
                awkward,
                xpShield1,
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/xp_shield")
        ));

        // 2. Potion of XP Shield I + Redstone = Long Potion of XP Shield I (8:00)
        ItemStack longXpShield1 = PotionContents.createItemStack(Items.POTION, ModPotions.LONG_XP_SHIELD);
        brewingRecipes.add(factory.createBrewingRecipe(
                List.of(Items.REDSTONE.getDefaultInstance()),
                xpShield1,
                longXpShield1,
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/long_xp_shield")
        ));

        // 3. Potion of XP Shield I + Glowstone Dust = Strong Potion of XP Shield II (1:30)
        ItemStack strongXpShield2 = PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_XP_SHIELD);
        brewingRecipes.add(factory.createBrewingRecipe(
                List.of(Items.GLOWSTONE_DUST.getDefaultInstance()),
                xpShield1,
                strongXpShield2,
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/strong_xp_shield")
        ));

        // 4. Strong Potion of XP Shield II + Nether Star = Ultra Potion of XP Shield III (1:00)
        ItemStack ultraXpShield3 = PotionContents.createItemStack(Items.POTION, ModPotions.ULTRA_XP_SHIELD);
        brewingRecipes.add(factory.createBrewingRecipe(
                List.of(Items.NETHER_STAR.getDefaultInstance()),
                strongXpShield2,
                ultraXpShield3,
                ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/ultra_xp_shield")
        ));

        // 5 & 6. Взрывные (Gunpowder) и Оседающие (Dragon's Breath) зелья для всех 4 ступеней
        List<Holder<Potion>> allPotions = List.of(
                ModPotions.XP_SHIELD,
                ModPotions.LONG_XP_SHIELD,
                ModPotions.STRONG_XP_SHIELD,
                ModPotions.ULTRA_XP_SHIELD
        );

        int index = 1;
        for (Holder<Potion> potion : allPotions) {
            ItemStack normal = PotionContents.createItemStack(Items.POTION, potion);
            ItemStack splash = PotionContents.createItemStack(Items.SPLASH_POTION, potion);
            ItemStack lingering = PotionContents.createItemStack(Items.LINGERING_POTION, potion);

            // Normal + Gunpowder -> Splash
            brewingRecipes.add(factory.createBrewingRecipe(
                    List.of(Items.GUNPOWDER.getDefaultInstance()),
                    normal,
                    splash,
                    ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/splash_" + index)
            ));

            // Splash + Dragon's Breath -> Lingering
            brewingRecipes.add(factory.createBrewingRecipe(
                    List.of(Items.DRAGON_BREATH.getDefaultInstance()),
                    splash,
                    lingering,
                    ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "brewing/lingering_" + index)
            ));

            index++;
        }

        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
    }
}
