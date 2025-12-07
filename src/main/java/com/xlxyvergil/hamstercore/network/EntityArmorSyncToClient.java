package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityArmorSyncToClient {
    private final int entityId;
    private final double armor;

    public EntityArmorSyncToClient(int entityId, double armor) {
        this.entityId = entityId;
        this.armor = armor;
    }

    public EntityArmorSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.armor = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.armor);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端线程上执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setArmor(armor);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(LivingEntity entity) {
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityArmorSyncToClient(entity.getId(), cap.getArmor())
            );
        });
    }
}