package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityHealthModifierCapability implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_health_modifier");
    public static final Capability<EntityHealthModifierCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private double healthModifier = 0.0;
    private boolean initialized = false;

    public double getHealthModifier() {
        return healthModifier;
    }

    public void setHealthModifier(double healthModifier) {
        this.healthModifier = healthModifier;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("HealthModifier", healthModifier);
        tag.putBoolean("Initialized", initialized);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        healthModifier = tag.getDouble("HealthModifier");
        initialized = tag.getBoolean("Initialized");
    }
}