package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEffectCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private EntityEffectCapability capability = null;
    private final LazyOptional<EntityEffectCapability> optional;

    public EntityEffectCapabilityProvider() {
        this(() -> new EntityEffectCapability());
    }

    public EntityEffectCapabilityProvider(java.util.function.Supplier<EntityEffectCapability> supplier) {
        this.optional = LazyOptional.of(supplier);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == EntityEffectCapability.CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        if (capability == null) {
            capability = optional.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty"));
        }
        return capability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (capability == null) {
            capability = optional.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty"));
        }
        capability.deserializeNBT(nbt);
    }
}