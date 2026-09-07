package com.hs1n.lifeXp_challenge.util;

import com.hs1n.lifeXp_challenge.mixin.ExperienceOrbAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ExperienceOrbHelper {
    public static final double MERGE_RADIUS = 2.0;

    private ExperienceOrbHelper() {}

    /**
     * Spawns a single clumped ExperienceOrb or merges into an existing orb within 2.0 blocks.
     */
    public static void spawnClumpedOrb(ServerLevel level, Vec3 pos, int totalXp) {
        if (totalXp <= 0) return;

        AABB aabb = new AABB(
                pos.x - MERGE_RADIUS, pos.y - MERGE_RADIUS, pos.z - MERGE_RADIUS,
                pos.x + MERGE_RADIUS, pos.y + MERGE_RADIUS, pos.z + MERGE_RADIUS
        );

        List<ExperienceOrb> nearby = level.getEntitiesOfClass(
                ExperienceOrb.class, aabb, orb -> orb.isAlive() && !orb.isRemoved()
        );

        if (!nearby.isEmpty()) {
            ExperienceOrb target = nearby.get(0);
            ExperienceOrbAccessor accessor = (ExperienceOrbAccessor) target;
            accessor.lifeXp$setValue(target.getValue() + totalXp);
            accessor.lifeXp$setCount(1);
            return;
        }

        ExperienceOrb orb = new ExperienceOrb(level, pos.x, pos.y, pos.z, totalXp);
        level.addFreshEntity(orb);
    }
}
