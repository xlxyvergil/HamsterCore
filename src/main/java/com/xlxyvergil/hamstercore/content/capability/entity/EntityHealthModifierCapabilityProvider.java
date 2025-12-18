package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityHealthModifierCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityHealthModifierCapability> CAPABILITY = EntityHealthModifierCapability.CAPABILITY;

    private final LazyOptional<EntityHealthModifierCapability> lazyCapability = LazyOptional.of(EntityHealthModifierCapability::new);

    public EntityHealthModifierCapabilityProvider() {
    }

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
        return lazyCapability.map(EntityHealthModifierCapability::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        lazyCapability.ifPresent(cap -> cap.deserializeNBT(nbt));
    }

    public void setHealthModifier(double healthModifier) {
        lazyCapability.ifPresent(cap -> cap.setHealthModifier(healthModifier));
    }
}