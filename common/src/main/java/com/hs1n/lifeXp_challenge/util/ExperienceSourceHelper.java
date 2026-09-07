package com.hs1n.lifeXp_challenge.util;

public final class ExperienceSourceHelper {
    public static final ThreadLocal<Boolean> IS_ORE_XP = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Boolean> IS_FISHING_XP = ThreadLocal.withInitial(() -> false);

    private ExperienceSourceHelper() {}
}
