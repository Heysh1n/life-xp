package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.util.ExperienceOrbHelper;
import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class XpHarvesterService {

    public static final Identifier ENCHANTMENT_ID =
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "xp_harvester");

    private static final TagKey<Block> DIAMOND_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "diamond_ores"));
    private static final TagKey<Block> LAPIS_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "lapis_ores"));
    private static final TagKey<Block> REDSTONE_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "redstone_ores"));

    public static void init() {
        BlockEvent.BREAK.register(XpHarvesterService::onBlockBreak);
    }

    public static int getHarvesterLevel(ItemStack tool) {
        if (tool == null || tool.isEmpty()) return 0;
        ItemEnchantments enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(ENCHANTMENT_ID)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    public static EventResult onBlockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel) || player == null || player.isCreative()) {
            return EventResult.pass();
        }

        ItemStack tool = player.getMainHandItem();
        int enchantLevel = getHarvesterLevel(tool);
        if (enchantLevel <= 0) {
            return EventResult.pass();
        }

        RandomSource random = serverLevel.getRandom();

        // 1. Созревший CropBlock (isMaxAge) -> спавн 1-2 ExperienceOrb.
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            int totalCropXp = 1 + random.nextInt(2);
            ExperienceOrbHelper.spawnClumpedOrb(serverLevel, Vec3.atCenterOf(pos), totalCropXp);
            return EventResult.pass();
        }

        // 2. Ancient Debris -> 1% от общего опыта игрока, жесткий хардкап Math.min(bonus, 500).
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            long totalPlayerXp = ExperienceUtils.getPlayerTotalXp(player);
            int bonus = (int) Math.floor(totalPlayerXp * 0.01);
            bonus = Math.min(bonus, 500);
            if (bonus > 0) {
                spawnOreXp(serverLevel, Vec3.atCenterOf(pos), bonus);
            }
            return EventResult.pass();
        }

        // 3. Ванильные руды (железо, медь, золото, лазурит, редстоун, алмазы) -> бонусный скейлинг опыта от уровня чар.
        int bonusXp = calculateOreBonus(state, enchantLevel, random);
        if (bonusXp > 0) {
            spawnOreXp(serverLevel, Vec3.atCenterOf(pos), bonusXp);
        }

        return EventResult.pass();
    }

    private static int calculateOreBonus(BlockState state, int level, RandomSource random) {
        return switch (state) {
            case BlockState s when s.is(BlockTags.IRON_ORES) || s.is(BlockTags.COPPER_ORES) -> (1 + random.nextInt(2)) * level;
            case BlockState s when s.is(BlockTags.GOLD_ORES) -> (2 + random.nextInt(2)) * level;
            case BlockState s when s.is(LAPIS_ORES) -> (2 + random.nextInt(4)) * level;
            case BlockState s when s.is(REDSTONE_ORES) -> (1 + random.nextInt(5)) * level;
            case BlockState s when s.is(DIAMOND_ORES) -> (3 + random.nextInt(5)) * level;
            default -> 0;
        };
    }

    private static void spawnOreXp(ServerLevel level, Vec3 pos, int amount) {
        try {
            ExperienceSourceHelper.IS_ORE_XP.set(true);
            ExperienceOrbHelper.spawnClumpedOrb(level, pos, amount);
        } finally {
            ExperienceSourceHelper.IS_ORE_XP.set(false);
        }
    }

    private XpHarvesterService() {}
}
