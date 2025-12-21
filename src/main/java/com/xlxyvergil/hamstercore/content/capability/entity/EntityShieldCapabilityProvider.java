package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityShieldCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityShieldCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final LazyOptional<EntityShieldCapability> lazyCapability = LazyOptional.of(EntityShieldCapability::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CAPABILITY) {
            return lazyCapability.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return lazyCapability.map(EntityShieldCapability::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        lazyCapability.ifPresent(cap -> cap.deserializeNBT(nbt));
    }
    
    public void setEntityType(EntityType<?> entityType) {
        lazyCapability.ifPresent(cap -> cap.setEntityType(entityType));
    }
}