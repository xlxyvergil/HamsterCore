package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFactionCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<EntityFactionCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private EntityFactionCapability capability = null;
    private final LazyOptional<EntityFactionCapability> lazyCapability = LazyOptional.of(this::createCapability);

    private EntityFactionCapability createCapability() {
        if (capability == null) {
            capability = new EntityFactionCapability();
        }
        return capability;
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
        return createCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createCapability().deserializeNBT(nbt);
    }
}