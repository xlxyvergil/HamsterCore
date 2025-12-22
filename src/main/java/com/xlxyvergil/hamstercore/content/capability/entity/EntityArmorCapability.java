package com.xlxyvergil.hamstercore.content.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityArmorCapability implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation("hamstercore", "entity_armor");
    
    public static final Capability<EntityArmorCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    
    // 移除baseArmor字段，仅保留armor字段
    private Double armor = null; // 实际护甲值，来自Attribute系统
    private EntityType<?> entityType;
    
    public double getArmor() {
        return armor != null ? armor : 0.0;
    }
    
    public void setArmor(double armor) {
        this.armor = armor;
    }
    
    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }
    
    // 移除所有内部计算逻辑
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (armor != null) {
            tag.putDouble("Armor", armor);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Armor")) {
            armor = tag.getDouble("Armor");
        } else {
            armor = null;
        }
    }
}