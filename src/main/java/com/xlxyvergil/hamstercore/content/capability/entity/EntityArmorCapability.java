package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ArmorConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityArmorCapability implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_armor");
    public static final Capability<EntityArmorCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private Double baseArmor = null; // 基础护甲值，来自配置文件
    private Double armor = null; // 实际护甲值，根据baseArmor和等级计算
    private EntityType<?> entityType;

    public double getArmor() {
        if (armor == null) {
            // 如果实际护甲值未计算，则先确保基础护甲值已计算
            getBaseArmor();
            // 然后计算实际护甲值
            updateArmor();
        }
        return armor;
    }
    
    public void setArmor(double armor) {
        this.armor = armor;
    }
    
    public double getBaseArmor() {
        if (baseArmor == null) {
            baseArmor = calculateBaseArmor();
        }
        return baseArmor;
    }
    
    public void setBaseArmor(double baseArmor) {
        this.baseArmor = baseArmor;
        // 当基础护甲值改变时，更新实际护甲值
        updateArmor();
    }

    private double calculateBaseArmor() {
        if (entityType == null) {
            return 0.0;
        }
        
        // 获取配置
        ArmorConfig config = ArmorConfig.load();
        if (config == null) {
            return 20.0; // 默认护甲值
        }
        
        // 获取基础护甲值
        double baseArmor = config.getArmorForEntity(entityType);
        if (baseArmor < 0) {
            baseArmor = config.getDefaultArmor();
        }
        
        return baseArmor;
    }
    
    /**
     * 更新实际护甲值（基于基础护甲值和等级）
     */
    private void updateArmor() {
        if (baseArmor == null) {
            armor = null;
            return;
        }
        
        // 默认使用等级20计算，实际使用时会通过initializeEntityCapabilities更新
        double levelDiff = Math.max(0, 20 - 20);
        double armorMultiplier = 1 + 0.4 * Math.pow(levelDiff, 0.75);
        
        // 确保护甲系数不会小于1（避免降级）
        armorMultiplier = Math.max(1.0, armorMultiplier);
        
        // 当前护甲值(AR)=基础护甲×护甲系数
        this.armor = baseArmor * armorMultiplier;
        
        // 限制护甲值上限为2700
        this.armor = Math.min(this.armor, 2700.0);
    }
    
    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }
    
    /**
     * 根据基础护甲值和等级计算实际护甲值
     * 护甲系数=1+0.4×(当前怪物等级-基础等级)^0.75
     * 当前护甲值(AR)=基础护甲×护甲系数
     *
     * @param baseLevel 基础等级（通常为20）
     * @param level 怪物等级
     */
    public void initializeEntityCapabilities(int baseLevel, int level) {
        // 确保基础护甲值已计算
        getBaseArmor();
        
        // 护甲系数=1+0.4×(当前怪物等级-基础等级)^0.75
        double levelDiff = Math.max(0, level - baseLevel);
        double armorMultiplier = 1 + 0.4 * Math.pow(levelDiff, 0.75);
        
        // 确保护甲系数不会小于1（避免降级）
        armorMultiplier = Math.max(1.0, armorMultiplier);
        
        // 当前护甲值(AR)=基础护甲×护甲系数
        this.armor = this.baseArmor * armorMultiplier;
        
        // 限制护甲值上限为2700
        this.armor = Math.min(this.armor, 2700.0);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (baseArmor != null) {
            tag.putDouble("BaseArmor", baseArmor);
        }
        if (armor != null) {
            tag.putDouble("Armor", armor);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("BaseArmor")) {
            baseArmor = tag.getDouble("BaseArmor");
        } else {
            baseArmor = null;
        }
        if (tag.contains("Armor")) {
            armor = tag.getDouble("Armor");
        } else {
            armor = null;
        }
    }
}