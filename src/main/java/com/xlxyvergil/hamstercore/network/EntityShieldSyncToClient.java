package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityShieldSyncToClient {
    private final int entityId;
    private final float currentShield;
    private final float maxShield;

    public EntityShieldSyncToClient(int entityId, float currentShield, float maxShield) {
        this.entityId = entityId;
        this.currentShield = currentShield;
        this.maxShield = maxShield;
    }

    public EntityShieldSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.currentShield = buf.readFloat();
        this.maxShield = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeFloat(this.currentShield);
        buf.writeFloat(this.maxShield);
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
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void sync(LivingEntity entity) {
        EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        if (shieldCap != null) {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
            );
        }
    }
}