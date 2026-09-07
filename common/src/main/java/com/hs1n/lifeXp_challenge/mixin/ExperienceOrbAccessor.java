package com.hs1n.lifeXp_challenge.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Accessor("value")
    int lifeXp$getValue();

    @Accessor("value")
    void lifeXp$setValue(int value);

    @Accessor("count")
    int lifeXp$getCount();

    @Accessor("count")
    void lifeXp$setCount(int count);
}
