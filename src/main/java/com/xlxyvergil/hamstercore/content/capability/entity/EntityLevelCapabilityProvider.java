package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.level.LevelSystem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityLevelCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityLevelCapability> CAPABILITY = EntityLevelCapability.CAPABILITY;

    private EntityLevelCapability capability = null;
    private final LazyOptional<EntityLevelCapability> lazyCapability = LazyOptional.of(this::createCapability);

    private EntityLevelCapability createCapability() {
        if (capability == null) {
            capability = new EntityLevelCapability();
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

    public void initializeLevel(LivingEntity entity) {
        int level = LevelSystem.calculateEntityLevel(entity);
        createCapability().setLevel(level);
    }
}