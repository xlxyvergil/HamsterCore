package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityLevelCapability implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_level");
    public static final Capability<EntityLevelCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private int level = 20; // 默认等级改为20
    private boolean initialized = false;

    public int getLevel() {
        LOGGER.debug("Getting level value: " + level);
        return level;
    }

    public void setLevel(int level) {
        LOGGER.debug("Setting level to: " + level);
        this.level = level;
        this.initialized = true;
    }

    public boolean isInitialized() {
        LOGGER.debug("Level capability initialized: " + initialized);
        return initialized;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Level", level);
        tag.putBoolean("Initialized", initialized);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        level = tag.getInt("Level");
        initialized = tag.getBoolean("Initialized");
    }
}