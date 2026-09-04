package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import dev.architectury.registry.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final dev.architectury.registry.registries.RegistrySupplier<CreativeModeTab> LIFE_XP_TAB = CREATIVE_MODE_TABS.register("life_xp_tab", () ->
            dev.architectury.registry.CreativeTabRegistry.create(Component.translatable("itemGroup.life_xp_challenge.life_xp_tab"), () -> new net.minecraft.world.item.ItemStack(ModItems.LIFE_BOTTLE.get())));
}
