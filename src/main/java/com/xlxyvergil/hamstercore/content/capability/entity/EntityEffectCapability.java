package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.network.packets.SyncEntityEffectCapabilityPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityEffectCapability {

    public static Capability<EntityEffectCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private List<MobEffectInstance> cachedEffects = new ArrayList<>();

    public EntityEffectCapability() {
    }

    public static void attachEntityCapability(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            final EntityEffectCapability capability = new EntityEffectCapability();
            event.addCapability(HamsterCore.location("entity_effect_data"), new EntityEffectCapabilityProvider(() -> capability));
        }
    }

    public static void syncEntityCapability(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity livingEntity) {
            sync(livingEntity);
        }
    }

    public void updateEffects(Collection<MobEffectInstance> effects) {
        this.cachedEffects.clear();
        this.cachedEffects.addAll(effects);
    }

    public List<MobEffectInstance> getCachedEffects() {
        return this.cachedEffects;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag effectsList = new ListTag();
        
        for (MobEffectInstance effect : cachedEffects) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("Effect", net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()).toString());
            effectTag.putInt("Amplifier", effect.getAmplifier());
            effectTag.putInt("Duration", effect.getDuration());
            effectTag.putBoolean("Infinite", effect.isInfiniteDuration());
            effectTag.putBoolean("Ambient", effect.isAmbient());
            effectTag.putBoolean("Visible", effect.isVisible());
            effectsList.add(effectTag);
        }
        
        tag.put("CachedEffects", effectsList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        ListTag effectsList = tag.getList("CachedEffects", Tag.TAG_COMPOUND);
        cachedEffects.clear();
        
        for (int i = 0; i < effectsList.size(); i++) {
            CompoundTag effectTag = effectsList.getCompound(i);
            String effectName = effectTag.getString("Effect");
            net.minecraft.resources.ResourceLocation effectLocation = net.minecraft.resources.ResourceLocation.tryParse(effectName);
            
            if (effectLocation != null) {
                net.minecraft.world.effect.MobEffect effect = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(effectLocation);
                if (effect != null) {
                    MobEffectInstance effectInstance = new MobEffectInstance(
                        effect,
                        effectTag.getInt("Duration"),
                        effectTag.getInt("Amplifier"),
                        effectTag.getBoolean("Ambient"),
                        effectTag.getBoolean("Visible")
                    );
                    
                    if (effectTag.getBoolean("Infinite")) {
                        // 设置为无限持续时间
                        effectInstance = new MobEffectInstance(
                            effectInstance.getEffect(),
                            -1,
                            effectInstance.getAmplifier(),
                            effectInstance.isAmbient(),
                            effectInstance.isVisible()
                        );
                    }
                    
                    cachedEffects.add(effectInstance);
                }
            }
        }
    }

    public static void sync(LivingEntity entity) {
        getCapabilityOptional(entity).ifPresent(c -> 
            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), 
                new SyncEntityEffectCapabilityPacket(entity.getId(), c.serializeNBT())));
    }

    public static LazyOptional<EntityEffectCapability> getCapabilityOptional(LivingEntity entity) {
        return entity.getCapability(CAPABILITY);
    }

    public static EntityEffectCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new EntityEffectCapability());
    }
}