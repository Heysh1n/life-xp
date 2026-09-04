package com.hs1n.lifeXp_challenge.network;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Простая сеть для синхронизации Kill Streak с клиентом (для HUD).
 */
public class LifeXpNetworking {
    public static final ResourceLocation SYNC_STREAK_ID =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "sync_streak");

    /** Вызывать на клиенте при инициализации. */
    public static void registerClientReceiver() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C, SYNC_STREAK_ID, (buf, context) -> {
                    int streak = buf.readVarInt();
                    context.queue(() -> KillStreakService.setClientStreak(streak));
                });
    }

    /** Отправить текущий стрик клиенту. */
    public static void syncStreakToClient(ServerPlayer player) {
        int streak = KillStreakService.getStreak(player);
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        buf.writeVarInt(streak);
        NetworkManager.sendToPlayer(player, SYNC_STREAK_ID, buf);
    }
}
