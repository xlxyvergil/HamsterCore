package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class EntityShieldCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_shield");
    
    private final EntityShieldCapability shieldCapability = new EntityShieldCapability();
    private final LazyOptional<EntityShieldCapability> optional = LazyOptional.of(() -> shieldCapability);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == EntityShieldCapabilityProvider.CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return shieldCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        shieldCapability.deserializeNBT(nbt);
    }
    
    public EntityShieldCapability getCapability() {
        return shieldCapability;
    }
    
    public static final Capability<EntityShieldCapability> CAPABILITY = null; // 这将在主类中注册
}