package com.xlxyvergil.hamstercore.util;

import com.google.common.collect.Multimap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * 武器适用物品查找器
 * 查找所有可以应用元素属性的物品
 */
public class WeaponApplicableItemsFinder {
    
    /**
     * 查找所有可应用元素属性的物品
     * @return 可应用的物品ID集合
     */
    public static Set<ResourceLocation> findApplicableItems() {
        Set<ResourceLocation> applicableItems = new HashSet<>();
        
        // 遍历所有已注册的物品
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
            if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) {
                continue;
            }
            
            // 检查物品是否为武器或工具
            if (isWeaponOrTool(item)) {
                applicableItems.add(itemKey);
            }
        }
        
        DebugLogger.log("找到 %d 个可应用元素属性的物品", applicableItems.size());
        return applicableItems;
    }
    
    /**
     * 检查物品是否为武器或工具
     * 优先基于物品本身的攻击伤害属性判断，其次基于名称判断
     * @param item 要检查的物品
     * @return 如果是武器或工具返回true
     */
    private static boolean isWeaponOrTool(Item item) {
        // 1. 优先检查物品是否具有攻击伤害属性（最准确的判断方式）
        if (hasAttackDamage(item)) {
            DebugLogger.log("物品 %s 具有攻击伤害属性，标记为武器", item.getDescriptionId());
            return true;
        }
        
        // 2. 对于没有攻击伤害属性的物品，通过名称判断是否为武器（用于弓、弩等间接伤害武器）
        String className = item.getClass().getSimpleName().toLowerCase();
        String itemName = item.getDescriptionId().toLowerCase();
        
        // 检查是否为远程武器或特殊武器（这些可能没有直接的攻击伤害属性）
        boolean isRangedWeapon = className.contains("bow") || className.contains("crossbow") ||
                                 className.contains("trident") || className.contains("mace") ||
                                 className.contains("hammer") || className.contains("spear") ||
                                 className.contains("dagger") || className.contains("katana") ||
                                 itemName.contains("bow") || itemName.contains("crossbow") ||
                                 itemName.contains("trident") || itemName.contains("weapon") ||
                                 itemName.contains("arrow") || itemName.contains("bolt");
        
        if (isRangedWeapon) {
            DebugLogger.log("物品 %s 通过名称判断为远程/特殊武器", item.getDescriptionId());
            return true;
        }
        
        // 3. 最后检查工具类物品（铲子、镐子等也可能造成少量伤害）
        boolean isTool = className.contains("sword") || className.contains("axe") ||
                        className.contains("pickaxe") || className.contains("shovel") ||
                        className.contains("hoe") || itemName.contains("sword") || 
                        itemName.contains("axe") || itemName.contains("pickaxe") ||
                        itemName.contains("shovel") || itemName.contains("hoe");
        
        if (isTool) {
            DebugLogger.log("物品 %s 通过名称判断为工具", item.getDescriptionId());
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查物品是否具有攻击伤害属性
     * 纯粹基于物品本身的默认属性，不依赖玩家
     * @param item 要检查的物品
     * @return 如果物品有攻击伤害属性返回true
     */
    private static boolean hasAttackDamage(Item item) {
        try {
            // 获取物品的默认属性修饰符（纯粹基于物品本身，不需要物品栈）
            Multimap<Attribute, AttributeModifier> modifiers = item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
            
            // 检查是否包含攻击伤害属性
            if (!modifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
                return false;
            }
            
            // 检查攻击伤害值是否大于0
            for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
                if (modifier.getAmount() > 0) {
                    DebugLogger.log("物品 %s 的攻击伤害值: %.2f", item.getDescriptionId(), modifier.getAmount());
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            DebugLogger.log("检查物品 %s 的攻击伤害属性时出错: %s", item.getDescriptionId(), e.getMessage());
            return false;
        }
    }
}