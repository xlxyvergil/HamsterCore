package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EntityFactionSyncToClient {
    private int entityId;
    private Faction faction;

    public EntityFactionSyncToClient(int entityId, Faction faction) {
        this.entityId = entityId;
        this.faction = faction;
    }

    public EntityFactionSyncToClient(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.faction = buf.readEnum(Faction.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeEnum(this.faction);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端线程执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    // 更新实体的派系Capability
                    livingEntity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setFaction(faction);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(LivingEntity entity) {
        entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityFactionSyncToClient(entity.getId(), cap.getFaction())
            );
        });
    }
}