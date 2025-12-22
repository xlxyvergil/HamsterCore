package com.xlxyvergil.hamstercore.attribute;

import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.faction.Factions;
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
    private static final UUID BASE_SHIELD_MODIFIER_UUID = UUID.fromString("2518d292-635c-43ed-b3aa-f67e3cf63aeb");
    private static final UUID SHIELD_COEFFICIENT_MODIFIER_UUID = UUID.fromString("7289e065-12e6-48dd-8ae4-9b1089464ffc");
    private static final UUID BASE_ARMOR_MODIFIER_UUID = UUID.fromString("0ae9d9e4-6323-4651-bc21-1424f46230f9");
    private static final UUID ARMOR_COEFFICIENT_MODIFIER_UUID = UUID.fromString("9c4dcd7d-743f-4916-b4ad-2455dc2b078a");
    
    /**
     * 应用基础属性修饰符到实体
     * @param entity 目标实体
     */
    public static void applyBasicModifiers(LivingEntity entity) {
        // 移除所有现有的基础属性修饰符
        removeAllBasicModifiers(entity);
        
        // 计算并应用基础护盾修饰符
        applyBaseShieldModifier(entity);
        
        // 计算并应用护盾系数修饰符
        applyShieldCoefficientModifier(entity);
        
        // 计算并应用基础护甲修饰符
        applyBaseArmorModifier(entity);
        
        // 计算并应用护甲系数修饰符
        applyArmorCoefficientModifier(entity);
    }
    
    /**
     * 计算并应用基础护盾修饰符
     * @param entity 目标实体
     */
    private static void applyBaseShieldModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr == null) return;
        
        float baseShield = 0;
        
        if (entity instanceof Player) {
            // 玩家使用默认基础护盾值
            baseShield = 100.0f;
        } else {
            // 非玩家实体根据派系和等级计算基础护盾值
            entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                    // 获取派系
                    Factions faction = factionCap.getFaction();
                    int level = levelCap.getLevel();
                    
                    // 加载护盾配置
                    ShieldConfig shieldConfig = ShieldConfig.load();
                    if (shieldConfig != null) {
                        // 根据派系和等级计算基础护盾值
                        baseShield = shieldConfig.getBaseShieldForEntity(entity.getType());
                        
                        // 如果配置中没有找到，则使用默认值
                        if (baseShield < 0) {
                            baseShield = shieldConfig.getDefaultShield();
                        }
                    }
                });
            });
        }
        
        // 添加基础护盾值修饰符
        shieldAttr.addTransientModifier(new AttributeModifier(
            BASE_SHIELD_MODIFIER_UUID,
            "Base Shield Modifier",
            baseShield,
            AttributeModifier.Operation.ADDITION
        ));
    }
    
    /**
     * 计算并应用护盾系数修饰符
     * 护盾系数计算公式：f(x) = 1 + 0.02 × (x - 基础等级)^1.76
     * @param entity 目标实体
     */
    private static void applyShieldCoefficientModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr == null) return;
        
        float shieldCoefficient = 1.0f;
        
        if (entity instanceof Player) {
            // 玩家护盾系数为1.0
            shieldCoefficient = 1.0f;
        } else {
            // 非玩家实体根据等级计算护盾系数
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                int level = levelCap.getLevel();
                
                // 护盾系数计算公式：f(x) = 1 + 0.02 × (x - 基础等级)^1.76
                if (level > 20) {
                    shieldCoefficient = 1 + 0.02f * (float) Math.pow(level - 20, 1.76);
                }
            });
        }
        
        // 添加护盾系数修饰符
        shieldAttr.addTransientModifier(new AttributeModifier(
            SHIELD_COEFFICIENT_MODIFIER_UUID,
            "Shield Coefficient Modifier",
            shieldCoefficient,
            AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }
    
    /**
     * 计算并应用基础护甲修饰符
     * @param entity 目标实体
     */
    private static void applyBaseArmorModifier(LivingEntity entity) {
        AttributeInstance armorAttr = entity.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr == null) return;
        
        double baseArmor = 0;
        
        if (entity instanceof Player) {
            // 玩家使用默认基础护甲值
            baseArmor = 20.0;
        } else {
            // 非玩家实体根据类型和等级计算基础护甲值
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                // 加载护甲配置
                ArmorConfig armorConfig = ArmorConfig.load();
                if (armorConfig != null) {
                    // 获取基础护甲值
                    baseArmor = armorConfig.getArmorForEntity(entity.getType());
                    
                    // 如果配置中没有找到，则使用默认值
                    if (baseArmor < 0) {
                        baseArmor = armorConfig.getDefaultArmor();
                    }
                }
            });
        }
        
        // 添加基础护甲值修饰符
        armorAttr.addTransientModifier(new AttributeModifier(
            BASE_ARMOR_MODIFIER_UUID,
            "Base Armor Modifier",
            baseArmor,
            AttributeModifier.Operation.ADDITION
        ));
    }
    
    /**
     * 计算并应用护甲系数修饰符
     * 护甲系数计算公式：f(x) = 1 + 0.4 × (x - 基础等级)^0.75
     * @param entity 目标实体
     */
    private static void applyArmorCoefficientModifier(LivingEntity entity) {
        AttributeInstance armorAttr = entity.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr == null) return;
        
        double armorCoefficient = 1.0;
        
        if (entity instanceof Player) {
            // 玩家护甲系数为1.0
            armorCoefficient = 1.0;
        } else {
            // 非玩家实体根据等级计算护甲系数
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                int level = levelCap.getLevel();
                
                // 护甲系数计算公式：f(x) = 1 + 0.4 × (x - 基础等级)^0.75
                if (level > 20) {
                    double levelDiff = Math.max(0, level - 20);
                    armorCoefficient = 1 + 0.4 * Math.pow(levelDiff, 0.75);
                    
                    // 限制护甲系数，间接限制护甲值上限为2700
                    armorCoefficient = Math.min(armorCoefficient, 2700.0 / 20.0); // 假设基础护甲为20
                }
            });
        }
        
        // 添加护甲系数修饰符
        armorAttr.addTransientModifier(new AttributeModifier(
            ARMOR_COEFFICIENT_MODIFIER_UUID,
            "Armor Coefficient Modifier",
            armorCoefficient,
            AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }
    
    /**
     * 移除实体上的所有基础属性修饰符
     * @param entity 目标实体
     */
    private static void removeAllBasicModifiers(LivingEntity entity) {
        // 移除护盾属性的基础修饰符
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr != null) {
            shieldAttr.removeModifier(BASE_SHIELD_MODIFIER_UUID);
            shieldAttr.removeModifier(SHIELD_COEFFICIENT_MODIFIER_UUID);
        }
        
        // 移除护甲属性的基础修饰符
        AttributeInstance armorAttr = entity.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr != null) {
            armorAttr.removeModifier(BASE_ARMOR_MODIFIER_UUID);
            armorAttr.removeModifier(ARMOR_COEFFICIENT_MODIFIER_UUID);
        }
    }
}