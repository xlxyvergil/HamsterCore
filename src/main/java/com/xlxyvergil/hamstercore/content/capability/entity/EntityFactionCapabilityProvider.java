package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFactionCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityFactionCapability> CAPABILITY = EntityFactionCapability.CAPABILITY;

    private final LazyOptional<EntityFactionCapability> lazyCapability = LazyOptional.of(EntityFactionCapability::new);
    private EntityType<?> entityType;

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
        return lazyCapability.map(EntityFactionCapability::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        lazyCapability.ifPresent(cap -> cap.deserializeNBT(nbt));
    }
    
    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
        lazyCapability.ifPresent(cap -> cap.setEntityType(entityType));
    }
}