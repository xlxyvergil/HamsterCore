package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityShieldSyncToClient {
    private final int entityId;
    private final float currentShield;
    private final float maxShield;
    private final boolean isGatingActive;

    public EntityShieldSyncToClient(int entityId, float currentShield, float maxShield) {
        this.entityId = entityId;
        this.currentShield = currentShield;
        this.maxShield = maxShield;
        this.isGatingActive = false; // 默认值，需要调用者设置
    }
    
    public EntityShieldSyncToClient(int entityId, float currentShield, float maxShield, boolean isGatingActive) {
        this.entityId = entityId;
        this.currentShield = currentShield;
        this.maxShield = maxShield;
        this.isGatingActive = isGatingActive;
    }

    public EntityShieldSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.currentShield = buf.readFloat();
        this.maxShield = buf.readFloat();
        this.isGatingActive = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeFloat(this.currentShield);
        buf.writeFloat(this.maxShield);
        buf.writeBoolean(this.isGatingActive);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端和服务端都需要处理
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    EntityShieldCapability shieldCap = livingEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
                    if (shieldCap != null) {
                        shieldCap.setCurrentShield(currentShield);
                        shieldCap.setMaxShield(maxShield);
                        shieldCap.setGatingActive(isGatingActive);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void sync(LivingEntity entity) {
        EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        // 检查实体是否真正拥有有效的护盾能力
        if (shieldCap != null && shieldCap.getMaxShield() >= 0) {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
            );
        }
    }
}