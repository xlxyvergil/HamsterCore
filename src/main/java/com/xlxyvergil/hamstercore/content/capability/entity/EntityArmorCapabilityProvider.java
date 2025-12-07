package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityArmorCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityArmorCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private EntityArmorCapability capability = null;
    private final LazyOptional<EntityArmorCapability> lazyCapability = LazyOptional.of(this::createCapability);

    private EntityArmorCapability createCapability() {
        if (capability == null) {
            capability = new EntityArmorCapability();
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

    public void initializeArmor(LivingEntity entity) {
        // 获取实体的等级
        int level = entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
            .map(levelCap -> levelCap.getLevel())
            .orElse(20); // 默认等级20
        
        // 获取实体的基础护甲值
        double baseArmor = EntityArmorCapability.getBaseArmorForEntity(entity);
        
        // 计算并设置实际护甲值
        createCapability().calculateAndSetArmor(baseArmor, level, 20); // 基础等级为20
    }
}