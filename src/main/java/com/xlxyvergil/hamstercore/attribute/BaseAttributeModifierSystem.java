package com.xlxyvergil.hamstercore.attribute;

import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 基础属性修饰符系统
 * 负责生成基础属性修饰符（基础护盾、护盾系数、基础护甲、护甲系数）
 */
public class BaseAttributeModifierSystem {
    
    // 为每个属性修饰符定义唯一的UUID
    private static final UUID COMBINED_SHIELD_MODIFIER_UUID = UUID.fromString("2518d292-635c-43ed-b3aa-f67e3cf63aeb");
    private static final UUID COMBINED_ARMOR_MODIFIER_UUID = UUID.fromString("0ae9d9e4-6323-4651-bc21-1424f46230f9");
    
    /**
     * 应用基础属性修饰符（护盾和基础（带系数））
     * @param entity 目标实体
     */
    public static void applyBaseAttributeModifiers(LivingEntity entity) {
        // 移除所有现有的基础属性修饰符
        removeBaseAttributeModifiers(entity);
        
        // 计算并应用基础护盾修饰符
        applyBaseShieldModifier(entity);
        
        // 计算并应用基础护甲修饰符
        applyBaseArmorModifier(entity);
    }
    
    
    /**
     * 计算并应用组合护盾修饰符（基础护盾 * 护盾系数）
     * @param entity 目标实体
     */
    private static void applyBaseShieldModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr == null) return;
        
        // 移除现有的护盾修饰符
        shieldAttr.removeModifier(COMBINED_SHIELD_MODIFIER_UUID);
        
        // 计算最终护盾值（基础护盾 * 护盾系数）
        double finalShield = calculateFinalShield(entity);
        
        // 只有当最终护盾值大于0时才添加修饰符
        if (finalShield > 0) {
            // 添加组合护盾值修饰符（使用ADDITION操作）
            shieldAttr.addTransientModifier(new AttributeModifier(
                COMBINED_SHIELD_MODIFIER_UUID,
                "Combined Shield Modifier",
                finalShield,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }

    /**
     * 计算并应用组合护甲修饰符（基础护甲 * 护甲系数）
     * @param entity 目标实体
     */
    private static void applyBaseArmorModifier(LivingEntity entity) {
        AttributeInstance armorAttr = entity.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr == null) return;
        
        // 移除现有的护甲修饰符
        armorAttr.removeModifier(COMBINED_ARMOR_MODIFIER_UUID);
        
        // 计算最终护甲值（基础护甲 * 护甲系数）
        double finalArmor = calculateFinalArmor(entity);
        
        // 只有当最终护甲值大于0时才添加修饰符
        if (finalArmor > 0) {
            // 添加组合护甲值修饰符（使用ADDITION操作）
            armorAttr.addTransientModifier(new AttributeModifier(
                COMBINED_ARMOR_MODIFIER_UUID,
                "Combined Armor Modifier",
                finalArmor,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }

    /**
     * 计算实体的最终护盾值（基础护盾 * 护盾系数）
     * @param entity 目标实体
     * @return 最终护盾值
     */
    private static double calculateFinalShield(LivingEntity entity) {
        if (entity instanceof Player) {
            // 玩家使用配置文件中的基础护盾值，系数固定为1
            ShieldConfig shieldConfig = ShieldConfig.load();
            if (shieldConfig != null) {
                return shieldConfig.getPlayerBaseShield();
            }
            // 如果ShieldConfig加载失败，返回默认的玩家护盾值
            return 200.0; // 默认玩家护盾值，与ShieldConfig中的默认值一致
        } else {
            // 非玩家实体根据派系和等级计算最终护盾值
            double[] finalShield = {0.0}; // 使用数组来规避lambda中的final限制
            entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                    // 加载护盾配置
                    ShieldConfig shieldConfig = ShieldConfig.load();
                    if (shieldConfig != null) {
                        // 获取基础护盾值
                        double baseShield = shieldConfig.getBaseShieldForEntity(entity.getType());
                        
                        // 计算护盾系数
                        int level = levelCap.getLevel();
                        double shieldCoefficient = 1.0;
                        
                        // 护盾系数计算公式：f(x) = 1 + 0.02 × (x - 基础等级)^1.76
                        if (level > 20) {
                            shieldCoefficient = 1 + 0.02 * Math.pow(level - 20, 1.76);
                        }
                        
                        // 最终护盾值 = 基础护盾 * 护盾系数
                        finalShield[0] = baseShield * shieldCoefficient;
                    }
                });
            });
            return finalShield[0];
        }
    }

    /**
     * 计算实体的最终护甲值（基础护甲 * 护甲系数）
     * @param entity 目标实体
     * @return 最终护甲值
     */
    private static double calculateFinalArmor(LivingEntity entity) {
        if (entity instanceof Player) {
            // 玩家使用默认基础护甲值，系数固定为1
            return 20.0;
        } else {
            // 非玩家实体根据类型和等级计算最终护甲值
            double[] finalArmor = {0.0}; // 使用数组来规避lambda中的final限制
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                // 加载护甲配置
                ArmorConfig armorConfig = ArmorConfig.load();
                if (armorConfig != null) {
                    // 获取基础护甲值
                    double baseArmor = armorConfig.getArmorForEntity(entity.getType());
                    
                    // 计算护甲系数
                    int level = levelCap.getLevel();
                    double armorCoefficient = 1.0;
                    
                    // 护甲系数计算公式：f(x) = 1 + 0.4 × (x - 基础等级)^0.75
                    if (level > 20) {
                        double levelDiff = Math.max(0, level - 20);
                        armorCoefficient = 1 + 0.4 * Math.pow(levelDiff, 0.75);
                        
                        // 限制护甲系数，间接限制护甲值上限为2700
                        if (baseArmor > 0) {
                            armorCoefficient = Math.min(armorCoefficient, 2700.0 / baseArmor);
                        }
                    }
                    
                    // 最终护甲值 = 基础护甲 * 护甲系数
                    finalArmor[0] = baseArmor * armorCoefficient;
                }
            });
            return finalArmor[0];
        }
    }
    

    
    /**
     * 移除实体上的所有基础属性修饰符
     * @param entity 目标实体
     */
    private static void removeAllBasicModifiers(LivingEntity entity) {
        // 移除基础属性修饰符
        removeBaseAttributeModifiers(entity);
    }
    
    /**
     * 移除实体上的基础属性修饰符（基础护盾和基础护甲）
     * @param entity 目标实体
     */
    private static void removeBaseAttributeModifiers(LivingEntity entity) {
        // 移除护盾属性的组合修饰符
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr != null) {
            shieldAttr.removeModifier(COMBINED_SHIELD_MODIFIER_UUID);
        }
        
        // 移除护甲属性的组合修饰符
        AttributeInstance armorAttr = entity.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr != null) {
            armorAttr.removeModifier(COMBINED_ARMOR_MODIFIER_UUID);
        }
    }
}