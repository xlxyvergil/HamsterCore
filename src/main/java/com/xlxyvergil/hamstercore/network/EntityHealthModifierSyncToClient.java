package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityHealthModifierCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityHealthModifierSyncToClient {
    private final int entityId;
    private final double healthModifier;
    private final boolean initialized;

    public EntityHealthModifierSyncToClient(int entityId, double healthModifier, boolean initialized) {
        this.entityId = entityId;
        this.healthModifier = healthModifier;
        this.initialized = initialized;
    }

    public EntityHealthModifierSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.healthModifier = buf.readDouble();
        this.initialized = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.healthModifier);
        buf.writeBoolean(this.initialized);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端线程上执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setHealthModifier(healthModifier);
                        // 如果未初始化，确保标记为已初始化
                        if (!initialized) {
                            cap.setHealthModifier(healthModifier);
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(LivingEntity entity) {
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel) {
            entity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new EntityHealthModifierSyncToClient(entity.getId(), cap.getHealthModifier(), cap.isInitialized())
                );
            });
        }
    }
}