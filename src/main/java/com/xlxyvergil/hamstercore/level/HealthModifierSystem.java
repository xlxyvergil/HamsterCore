package com.xlxyvergil.hamstercore.level;

import com.xlxyvergil.hamstercore.config.LevelConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityHealthModifierCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class HealthModifierSystem {
    // 生命值修饰符的UUID，用于唯一标识和避免重复添加
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("f2a0b1c2-d3e4-5f6a-7b8c-9d0e1f2a3b4c");
    private static final String HEALTH_MODIFIER_NAME = "HamsterCore.HealthModifier";
    
    // 公式常量：5的24次方根除以5
    private static final double CONSTANT = Math.pow(5, 1.0 / 24) / 5;
    
    private static LevelConfig levelConfig;
    
    public static void init(LevelConfig config) {
        levelConfig = config;
    }
    
    /**
     * 计算生命值修饰符的数值
     * @param level 实体等级
     * @param baseLevel 基础等级
     * @return 生命值修饰符数值
     */
    public static double calculateHealthModifier(int level, int baseLevel) {
        if (level <= baseLevel) {
            return 0.0; // 如果实体等级小于等于基础等级，修饰符为0
        }
        
        // 应用公式：f₂(x) = 1 + (5^(1/24))/5 × (x - 基础等级)^0.72
        // 对于乘法修饰符，我们需要返回的值是 (f₂(x) - 1)，因为Minecraft的乘法修饰符是基于基础值的百分比
        return CONSTANT * Math.pow(level - baseLevel, 0.72);
    }
    
    /**
     * 为实体计算并应用生命值修饰符
     * @param entity 实体
     */
    public static void applyHealthModifier(LivingEntity entity) {
        // 检查生命值系统是否启用
        if (levelConfig != null && !levelConfig.isHealthSystemEnabled()) {
            // 如果系统被禁用，移除已有的修饰符
            applyModifierToEntity(entity, 0.0);
            return;
        }
        
        // 获取实体等级
        int entityLevel = entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getLevel())
                .orElse(20); // 默认等级20
        
        // 获取基础等级
        int baseLevel = levelConfig != null ? levelConfig.getDistanceBaseLevel() : 50;
        
        // 计算修饰符数值
        double modifierValue = calculateHealthModifier(entityLevel, baseLevel);
        
        // 存储修饰符到capability中
        entity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> cap.setHealthModifier(modifierValue));
        
        // 应用修饰符到实体属性
        applyModifierToEntity(entity, modifierValue);
    }
    
    /**
     * 为实体应用生命值修饰符
     * @param entity 实体
     * @param modifierValue 修饰符数值
     */
    private static void applyModifierToEntity(LivingEntity entity, double modifierValue) {
        // 获取实体的最大生命值属性实例
        AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) {
            return; // 如果实体没有最大生命值属性，跳过
        }
        
        // 记录旧的最大生命值
        double oldMaxHealth = entity.getMaxHealth();
        
        // 移除已存在的修饰符（如果有）
        AttributeModifier existingModifier = healthAttribute.getModifier(HEALTH_MODIFIER_UUID);
        if (existingModifier != null) {
            healthAttribute.removeModifier(existingModifier);
        }
        
        // 使用Operation.MULTIPLY_TOTAL (Operation 2) 应用生命值修饰符
        // 计算公式：最终生命值 = 基础生命值 × (1 + modifierValue)
        healthAttribute.addPermanentModifier(
                new AttributeModifier(
                        HEALTH_MODIFIER_UUID,
                        HEALTH_MODIFIER_NAME,
                        modifierValue,
                        Operation.MULTIPLY_TOTAL
                )
        );
        
        // 获取新的最大生命值
        double newMaxHealth = entity.getMaxHealth();
        
        // 调整实体当前生命值以匹配新的最大生命值
        if (newMaxHealth > oldMaxHealth) {
            // 如果最大生命值增加了，相应地增加当前生命值
            float healthIncrease = (float) (newMaxHealth - oldMaxHealth);
            entity.heal(healthIncrease);
        } else if (entity.getHealth() > newMaxHealth) {
            // 如果最大生命值减少了且当前生命值高于新的最大值，则调整当前生命值
            entity.setHealth((float) newMaxHealth);
        }
    }
}