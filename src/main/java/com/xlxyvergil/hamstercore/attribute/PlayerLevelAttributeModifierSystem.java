package com.xlxyvergil.hamstercore.attribute;

import com.xlxyvergil.hamstercore.level.PlayerLevelManager;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 玩家等级属性修饰符系统
 * 负责生成基于玩家等级的属性修饰符（护盾、护甲等）
 * 这些修饰符应该在基础属性修饰符之后、衍生属性修饰符之前应用
 */
public class PlayerLevelAttributeModifierSystem {
    
    // 为每个属性修饰符定义唯一的UUID
    private static final UUID LEVEL_SHIELD_MODIFIER_UUID = UUID.fromString("0dc2e6b9-dcd8-4a19-bc67-f96231255458");
    private static final UUID LEVEL_ARMOR_MODIFIER_UUID = UUID.fromString("1612894f-6c6a-4f36-9195-74f42bd2ed70");
    private static final UUID LEVEL_HEALTH_MODIFIER_UUID = UUID.fromString("1949671c-4ea9-449e-9b5d-212a3555454e");
    
    /**
     * 应用基于玩家等级的属性修饰符
     * @param player 目标玩家
     * @param playerLevel 玩家等级
     */
    public static void applyPlayerLevelModifiers(Player player, int playerLevel) {
        // 移除所有现有的玩家等级属性修饰符
        removeAllPlayerLevelModifiers(player);
        
        // 计算并应用基于等级的护盾修饰符
        applyLevelShieldModifier(player, playerLevel);
        
        // 计算并应用基于等级的护甲修饰符
        applyLevelArmorModifier(player, playerLevel);
        
        // 计算并应用基于等级的生命值修饰符
        applyLevelHealthModifier(player, playerLevel);
    }
    
    /**
     * 计算并应用基于等级的护盾修饰符
     * @param player 目标玩家
     * @param playerLevel 玩家等级
     */
    private static void applyLevelShieldModifier(Player player, int playerLevel) {
        AttributeInstance shieldAttr = player.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr == null) return;
        
        // 根据玩家等级计算护盾加成
        // 这里使用一个简单的线性增长公式，你可以根据需要调整
        double levelShieldBonus = PlayerLevelManager.getShieldBonus(playerLevel);
        
        // 添加基于等级的护盾修饰符
        if (levelShieldBonus > 0) {
            shieldAttr.addTransientModifier(new AttributeModifier(
                LEVEL_SHIELD_MODIFIER_UUID,
                "Level Shield Bonus",
                levelShieldBonus,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 计算并应用基于等级的护甲修饰符
     * @param player 目标玩家
     * @param playerLevel 玩家等级
     */
    private static void applyLevelArmorModifier(Player player, int playerLevel) {
        AttributeInstance armorAttr = player.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr == null) return;
        
        // 根据玩家等级计算护甲加成
        // 这里使用一个简单的线性增长公式，你可以根据需要调整
        double levelArmorBonus = PlayerLevelManager.getBaseArmorBonus(playerLevel);
        
        // 添加基于等级的护甲修饰符
        if (levelArmorBonus > 0) {
            armorAttr.addTransientModifier(new AttributeModifier(
                LEVEL_ARMOR_MODIFIER_UUID,
                "Level Armor Bonus",
                levelArmorBonus,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 计算并应用基于等级的生命值修饰符
     * @param player 目标玩家
     * @param playerLevel 玩家等级
     */
    private static void applyLevelHealthModifier(Player player, int playerLevel) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null) return;
        
        // 根据玩家等级计算生命值加成
        double healthBonus = PlayerLevelManager.getHealthBonus(playerLevel);
        
        // 移除旧的修饰符
        healthAttr.removeModifier(LEVEL_HEALTH_MODIFIER_UUID);
        
        // 添加新的生命值修饰符
        if (healthBonus > 0) {
            healthAttr.addTransientModifier(new AttributeModifier(
                LEVEL_HEALTH_MODIFIER_UUID,
                "Player Level Health Bonus",
                healthBonus,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 移除玩家身上的所有等级属性修饰符
     * @param player 目标玩家
     */
    private static void removeAllPlayerLevelModifiers(Player player) {
        // 移除护盾属性的等级修饰符
        AttributeInstance shieldAttr = player.getAttribute(EntityAttributeRegistry.SHIELD.get());
        if (shieldAttr != null) {
            shieldAttr.removeModifier(LEVEL_SHIELD_MODIFIER_UUID);
        }
        
        // 移除护甲属性的等级修饰符
        AttributeInstance armorAttr = player.getAttribute(EntityAttributeRegistry.ARMOR.get());
        if (armorAttr != null) {
            armorAttr.removeModifier(LEVEL_ARMOR_MODIFIER_UUID);
        }
        
        // 移除生命值属性的等级修饰符
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(LEVEL_HEALTH_MODIFIER_UUID);
        }
    }
}