package com.hs1n.lifeXp_challenge.event;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.util.ExperienceOrbHelper;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockBreakHandler {
    public static final ResourceLocation XP_HARVESTER_ID = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "xp_harvester");
    public static final ResourceKey<Enchantment> XP_HARVESTER_KEY = ResourceKey.create(Registries.ENCHANTMENT, XP_HARVESTER_ID);

    public static void init() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
                return EventResult.pass();
            }

            if (serverPlayer.isCreative()) {
                return EventResult.pass();
            }

            ItemStack mainHand = serverPlayer.getMainHandItem();
            int harvesterLevel = getHarvesterLevel(mainHand);
            if (harvesterLevel <= 0) {
                return EventResult.pass();
            }

            // 1. Созревший CropBlock (isMaxAge) -> спавн 1-2 ExperienceOrb
            if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                int orbCount = 1 + serverPlayer.getRandom().nextInt(2);
                for (int i = 0; i < orbCount; i++) {
                    serverLevel.addFreshEntity(new ExperienceOrb(
                            serverLevel,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            1
                    ));
                }
            }
            // 2. Ancient Debris -> 1% от текущего пула опыта игрока (макс 500 XP)
            else if (state.is(Blocks.ANCIENT_DEBRIS)) {
                long totalXp = ExperienceUtils.getPlayerTotalXp(serverPlayer);
                int bonus = (int) Math.min(500, Math.max(1, (long) (totalXp * 0.01)));
                if (bonus > 0) {
                    ExperienceOrbHelper.spawnClumpedOrb(serverLevel, Vec3.atCenterOf(pos), bonus);
                }
            }
            // 3. Ванильная руда (железо, медь, золото, лазурит, редстоун, алмазы) -> бонусный опыт от уровня чар
            else if (isVanillaOre(state)) {
                int bonus = harvesterLevel * (1 + serverPlayer.getRandom().nextInt(2));
                ExperienceOrbHelper.spawnClumpedOrb(serverLevel, Vec3.atCenterOf(pos), bonus);
            }

            return EventResult.pass();
        });
    }

    public static int getHarvesterLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(XP_HARVESTER_KEY) || entry.getKey().is(XP_HARVESTER_ID)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    private static boolean isVanillaOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.DIAMOND_ORES);
    }

    private BlockBreakHandler() {}
}
