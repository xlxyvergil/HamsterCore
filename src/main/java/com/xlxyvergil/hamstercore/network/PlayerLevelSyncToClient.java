package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PlayerLevelSyncToClient {
    private final CompoundTag data;
    private final int playerId;

    public PlayerLevelSyncToClient(int playerId, CompoundTag data) {
        this.playerId = playerId;
        this.data = data;
    }

    public PlayerLevelSyncToClient(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeNbt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端线程上执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(playerId);
                if (entity instanceof Player player) {
                    player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.deserializeNBT(data);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer) {
            player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new PlayerLevelSyncToClient(player.getId(), cap.serializeNBT())
                );
            });
        }
    }
}