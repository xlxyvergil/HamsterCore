package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityArmorCapability implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_armor");

    private double armor = 0;
    private boolean initialized = false;

    public double getArmor() {
        LOGGER.debug("Getting armor value: " + armor);
        return armor;
    }

    public void setArmor(double armor) {
        LOGGER.debug("Setting armor to: " + armor);
        this.armor = armor;
        this.initialized = true;
    }

    public boolean isInitialized() {
        LOGGER.debug("Armor capability initialized: " + initialized);
        return initialized;
    }

    /**
     * 根据基础护甲值和等级计算实际护甲值
     * 护甲系数=1+0.4×(当前怪物等级-基础等级)^0.75
     * 当前护甲值(AR)=基础护甲×护甲系数
     * 
     * @param baseArmor 基础护甲值
     * @param level 怪物等级
     * @param baseLevel 基础等级（通常为20）
     */
    public void calculateAndSetArmor(double baseArmor, int level, int baseLevel) {
        // 即使基础护甲为0，也应该根据等级计算护甲值
        
        // 护甲系数=1+0.4×(当前怪物等级-基础等级)^0.75
        // 修复：当等级低于基础等级时，应该为0而不是负数
        double levelDiff = Math.max(0, level - baseLevel);
        double armorMultiplier = 1 + 0.4 * Math.pow(levelDiff, 0.75);
        
        // 确保护甲系数不会小于1（避免降级）
        armorMultiplier = Math.max(1.0, armorMultiplier);
        
        // 当前护甲值(AR)=基础护甲×护甲系数
        this.armor = baseArmor * armorMultiplier;
        this.initialized = true;
        
        // 调试信息：打印计算过程
        LOGGER.debug("calculateAndSetArmor: baseArmor=" + baseArmor + ", level=" + level + ", baseLevel=" + baseLevel + ", levelDiff=" + levelDiff + ", armorMultiplier=" + armorMultiplier + ", final armor=" + this.armor);
    }
    
    /**
     * 获取指定实体类型的基础护甲值
     * @param entity 实体
     * @return 基础护甲值
     */
    public static double getBaseArmorForEntity(LivingEntity entity) {
        ArmorConfig armorConfig = ArmorConfig.load();
        if (armorConfig == null) {
            return 20.0; // 默认返回20.0而不是0
        }
        
        EntityType<?> entityType = entity.getType();
        
        LOGGER.debug("Getting armor for entity: " + entityType.getDescriptionId());
        
        double armor = armorConfig.getArmorForEntity(entityType);
        LOGGER.debug("Config returned armor value: " + armor + " for entity: " + entityType.getDescriptionId());
        // 如果没有配置，则返回配置文件中的默认值
        double finalArmor = armor >= 0 ? armor : armorConfig.getDefaultArmor();
        LOGGER.debug("Final base armor value: " + finalArmor + " (config default: " + armorConfig.getDefaultArmor() + ")");
        return finalArmor;
    }
    
    /**
     * 获取指定实体类型的基础护甲值
     * @param entityType 实体类型
     * @param faction 派系
     * @return 基础护甲值
     */
    public static double getBaseArmorForEntity(EntityType<?> entityType, String faction) {
        ArmorConfig armorConfig = ArmorConfig.load();
        if (armorConfig == null) {
            return 20.0; // 默认返回20.0而不是0
        }
        
        double armor = armorConfig.getArmorForEntity(entityType, faction);
        // 如果没有配置，则返回配置文件中的默认值
        return armor >= 0 ? armor : armorConfig.getDefaultArmor();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Armor", armor);
        tag.putBoolean("Initialized", initialized);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        armor = tag.getDouble("Armor");
        initialized = tag.getBoolean("Initialized");
    }
}