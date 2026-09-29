package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.util.ExperienceOrbHelper;
import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class OreVeinMinerService {

    private OreVeinMinerService() {}

    /** Prevents recursive vein mining when destroyBlock triggers BlockEvent.BREAK again. */
    public static final ThreadLocal<Boolean> IS_VEIN_MINING = ThreadLocal.withInitial(() -> false);

    private static final int MAX_VEIN_BLOCKS = 6; // total including origin

    private static final TagKey<Item> PICKAXES =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "pickaxes"));
    private static final TagKey<Block> MINECRAFT_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "ores"));
    private static final TagKey<Block> COMMON_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));
    private static final TagKey<Block> COAL_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "coal_ores"));
    private static final TagKey<Block> DIAMOND_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "diamond_ores"));
    private static final TagKey<Block> EMERALD_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "emerald_ores"));
    private static final TagKey<Block> LAPIS_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "lapis_ores"));
    private static final TagKey<Block> REDSTONE_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "redstone_ores"));

    private static final BlockPos[] OFFSETS = {
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0), new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
    };

    /**
     * Attempts vein mining from the origin block.
     * @return number of extra blocks broken (not counting origin).
     */
    public static int tryVeinMine(ServerLevel level, ServerPlayer player, BlockPos origin,
                                   BlockState originState, ItemStack tool, int enchLevel) {
        if (enchLevel < 2) return 0;
        if (!tool.is(PICKAXES)) return 0;
        if (IS_VEIN_MINING.get()) return 0;
        if (!isOre(originState)) return 0;

        Block targetBlock = originState.getBlock();

        // BFS for connected identical ore blocks
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        List<BlockPos> toBreak = new ArrayList<>();

        while (!queue.isEmpty() && visited.size() < MAX_VEIN_BLOCKS) {
            BlockPos current = queue.poll();
            for (BlockPos offset : OFFSETS) {
                BlockPos neighbor = current.offset(offset);
                if (visited.contains(neighbor)) continue;
                if (visited.size() >= MAX_VEIN_BLOCKS) break;

                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.getBlock() == targetBlock) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    toBreak.add(neighbor);
                }
            }
        }

        if (toBreak.isEmpty()) return 0;

        RandomSource random = level.getRandom();
        int totalBonusXp = 0;

        try {
            IS_VEIN_MINING.set(true);
            for (BlockPos pos : toBreak) {
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) continue;

                // Break block with drops
                level.destroyBlock(pos, true, player);

                // Durability damage
                tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

                // Exhaustion (same as vanilla mining)
                player.causeFoodExhaustion(0.005f);

                // Bonus XP from XP Harvester
                int bonusXp = calculateOreBonus(state, enchLevel, random);
                if (bonusXp > 0) {
                    try {
                        ExperienceSourceHelper.IS_ORE_XP.set(true);
                        ExperienceOrbHelper.spawnClumpedOrb(level, Vec3.atCenterOf(pos), bonusXp);
                    } finally {
                        ExperienceSourceHelper.IS_ORE_XP.set(false);
                    }
                    totalBonusXp += bonusXp;
                }

                // If tool broke, stop
                if (tool.isEmpty()) break;
            }
        } finally {
            IS_VEIN_MINING.set(false);
        }

        // Track ore XP for advancement
        if (totalBonusXp > 0) {
            AdvancementService.addOreXp(player, totalBonusXp);
        }

        // Grant miner_vein advancement if total blocks >= 6
        int totalBlocks = toBreak.size() + 1; // +1 for origin
        if (totalBlocks >= MAX_VEIN_BLOCKS) {
            AdvancementService.grantAdvancement(player, AdvancementService.MINER_VEIN);
        }

        return toBreak.size();
    }

    private static boolean isOre(BlockState state) {
        return state.is(MINECRAFT_ORES)
                || state.is(COMMON_ORES)
                || state.is(COAL_ORES)
                || state.is(DIAMOND_ORES)
                || state.is(EMERALD_ORES)
                || state.is(LAPIS_ORES)
                || state.is(REDSTONE_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES);
    }

    private static int calculateOreBonus(BlockState state, int level, RandomSource random) {
        if (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COPPER_ORES)) {
            return (1 + random.nextInt(2)) * level;
        } else if (state.is(BlockTags.GOLD_ORES)) {
            return (2 + random.nextInt(2)) * level;
        } else if (state.is(LAPIS_ORES)) {
            return (2 + random.nextInt(4)) * level;
        } else if (state.is(REDSTONE_ORES)) {
            return (1 + random.nextInt(5)) * level;
        } else if (state.is(DIAMOND_ORES)) {
            return (3 + random.nextInt(5)) * level;
        } else if (state.is(EMERALD_ORES)) {
            return (3 + random.nextInt(4)) * level;
        } else if (state.is(COAL_ORES)) {
            return (1 + random.nextInt(2)) * level;
        }
        return level; // fallback for unknown ores
    }
}
