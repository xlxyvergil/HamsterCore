package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncEntityEffectsPacket {

    private final int entityId;
    private final List<MobEffectInstance> effects;

    public SyncEntityEffectsPacket(int entityId, List<MobEffectInstance> effects) {
        this.entityId = entityId;
        this.effects = effects;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(effects.size());
        for (MobEffectInstance effect : effects) {
            ResourceLocation effectName = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
            buf.writeResourceLocation(effectName);
            buf.writeInt(effect.getAmplifier());
            buf.writeInt(effect.getDuration());
            buf.writeBoolean(effect.isAmbient());
            buf.writeBoolean(effect.isVisible());
        }
    }

    public static SyncEntityEffectsPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int size = buf.readInt();
        List<MobEffectInstance> effects = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation effectName = buf.readResourceLocation();
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectName);
            int amplifier = buf.readInt();
            int duration = buf.readInt();
            boolean ambient = buf.readBoolean();
            boolean visible = buf.readBoolean();

            if (effect != null) {
                effects.add(new MobEffectInstance(effect, duration, amplifier, ambient, visible));
            }
        }

        return new SyncEntityEffectsPacket(entityId, effects);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端线程执行
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity) {
                    // 更新实体状态效果缓存
                    EntityEffectManager.updateEntityEffectsClientSide(entityId, effects);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}