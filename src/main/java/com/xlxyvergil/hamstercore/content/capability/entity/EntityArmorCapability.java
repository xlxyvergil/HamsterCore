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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityArmorCapability implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_armor");
    public static final Capability<EntityArmorCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private Double armor = null; // 使用Double包装类型，null表示未计算
    private EntityType<?> entityType;

    public double getArmor() {
        if (armor == null) {
            armor = calculateArmor();
        }
        return armor;
    }
    
    public void setArmor(double armor) {
        this.armor = armor;
    }

    private double calculateArmor() {
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
        
        LOGGER.debug("Base armor for entity {}: {}", entityType.getDescriptionId(), baseArmor);
        return baseArmor;
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
        double baseArmor = calculateArmor();
        
        // 护甲系数=1+0.4×(当前怪物等级-基础等级)^0.75
        double levelDiff = Math.max(0, level - baseLevel);
        double armorMultiplier = 1 + 0.4 * Math.pow(levelDiff, 0.75);
        
        // 确保护甲系数不会小于1（避免降级）
        armorMultiplier = Math.max(1.0, armorMultiplier);
        
        // 当前护甲值(AR)=基础护甲×护甲系数
        this.armor = baseArmor * armorMultiplier;
        
        LOGGER.debug("Calculated armor: baseArmor={}, level={}, baseLevel={}, levelDiff={}, armorMultiplier={}, final armor={}",
                baseArmor, level, baseLevel, levelDiff, armorMultiplier, this.armor);
    }

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
            armor = null; // 未序列化的标记，下次访问时重新计算
        }
    }
}