package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityLevelSyncToClient {
    private final int entityId;
    private final int level;

    public EntityLevelSyncToClient(int entityId, int level) {
        this.entityId = entityId;
        this.level = level;
    }

    public EntityLevelSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.level = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.level);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端线程上执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setLevel(level);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(LivingEntity entity) {
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityLevelSyncToClient(entity.getId(), cap.getLevel())
            );
        });
    }
}