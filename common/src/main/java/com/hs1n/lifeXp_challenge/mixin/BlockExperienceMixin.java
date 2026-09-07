package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockExperienceMixin {

    @Unique
    private static final TagKey<Block> COMMON_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));

    @Unique
    private static boolean lifeXp$isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(COMMON_ORES);
    }

    @Inject(method = "tryDropExperience", at = @At("HEAD"))
    private void lifeXp$beforeDropExperience(ServerLevel level, BlockPos pos, ItemStack itemStack, IntProvider intProvider, CallbackInfo ci) {
        Block self = (Block) (Object) this;
        if (lifeXp$isOre(self.defaultBlockState())) {
            ExperienceSourceHelper.IS_ORE_XP.set(true);
        }
    }

    @Inject(method = "tryDropExperience", at = @At("RETURN"))
    private void lifeXp$afterDropExperience(ServerLevel level, BlockPos pos, ItemStack itemStack, IntProvider intProvider, CallbackInfo ci) {
        ExperienceSourceHelper.IS_ORE_XP.set(false);
    }
}
