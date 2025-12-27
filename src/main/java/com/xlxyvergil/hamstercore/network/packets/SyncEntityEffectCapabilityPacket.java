package com.xlxyvergil.hamstercore.network.packets;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEntityEffectCapabilityPacket {

    private final int entityId;
    private final CompoundTag tag;

    public SyncEntityEffectCapabilityPacket(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(tag);
    }

    @OnlyIn(Dist.CLIENT)
    public void execute(Supplier<NetworkEvent.Context> context) {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity instanceof LivingEntity livingEntity) {
            EntityEffectCapability.getCapabilityOptional(livingEntity).ifPresent(c -> c.deserializeNBT(tag));
        }
    }

    public static SyncEntityEffectCapabilityPacket decode(FriendlyByteBuf buf) {
        return new SyncEntityEffectCapabilityPacket(buf.readInt(), buf.readNbt());
    }
}