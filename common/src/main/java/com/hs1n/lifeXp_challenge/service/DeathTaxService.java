package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathTaxService {
    private static final Map<UUID, DamageSource> LAST_FATAL_SOURCES = new ConcurrentHashMap<>();

    public static void onPlayerDeath(ServerPlayer player, DamageSource source) {
        LAST_FATAL_SOURCES.put(player.getUUID(), source);
    }

    public static double calculateSmartTax(ServerPlayer player) {
        DamageSource source = LAST_FATAL_SOURCES.get(player.getUUID());
        double baseTax = LifeXpConfig.INSTANCE.getDeathXpTax();
        
        if (source == null) return baseTax;

        // Эпичные битвы (Визер, Варден, Дракон Края, боссы)
        if (source.getEntity() != null) {
            String entityName = source.getEntity().getType().getDescriptionId(); // например, entity.minecraft.wither
            if (entityName.contains("wither") || entityName.contains("warden") || entityName.contains("ender_dragon")) {
                return Math.max(0.01, baseTax * 0.1); // 10% от текущего налога
            }
        }

        // Глупые смерти (Лава, Падение, Утопление, Огонь, Голодание)
        if (source.is(DamageTypeTags.IS_FALL) || 
            source.is(DamageTypeTags.IS_FIRE) || 
            source.is(DamageTypeTags.IS_DROWNING) ||
            source.getMsgId().equals("lava") ||
            source.getMsgId().equals("starve") ||
            source.getMsgId().equals("inWall")) {
            return Math.min(1.0, baseTax * 5.0); // Жестокий налог, например 50% если база 10%
        }

        return baseTax;
    }
}
