package com.xlxyvergil.hamstercore.util;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.ModList;


/**
 * 武器判断工具类
 * 专门用于判断物品是否为武器或工具的逻辑
 */
public class WeaponJudgeUtil {
    
    /**
     * 检查物品是否为武器或工具
     * 首先排除slashblade:slashblade和modid为tacz的物品，然后参考Apotheosis的LootCategory系统识别机制
     * 
     * 排除方法：
     * 1. 检查物品ID是否为slashblade:slashblade
     * 2. 检查物品modid是否为tacz
     * 
     * 识别方法：
     * 3. 检查物品是否在forge:tools标签或其子标签中
     * 4. 检查物品是否能执行常见工具动作（如挖掘、砍伐等）
     * 5. 检查物品是否有正数的攻击伤害属性（武器特征）
     * 6. 检查物品是否为原版武器类型
     * 
     * @param item 要检查的物品
     * @return 如果是武器或工具返回true，slashblade:slashblade或tacz物品返回false
     */
    public static boolean isWeaponOrTool(Item item) {
        // 首先排除slashblade:slashblade和modid为tacz的物品
        if (isExcludedItem(item)) {
            return false;
        }
        
        // 3. 检查是否在forge:tools标签或其子标签中
        if (isInToolsTagOrSubtags(item)) {
            return true;
        }
        
        // 4. 检查是否能执行常见工具动作
        if (canPerformCommonToolActions(item)) {
            return true;
        }
        
        // 5. 检查是否为原版武器类型
        if (isVanillaWeaponType(item)) {
            return true;
        }
        
        // 6. 检查物品是否有正数的攻击伤害属性
        if (hasPositiveAttackDamage(item)) {
            return true;
        }
        
        return false;
    }

    /**
     * 检查物品是否在forge:tools标签或其子标签中
     * @param item 要检查的物品
     * @return 如果在tools标签或其子标签中返回true
     */
    private static boolean isInToolsTagOrSubtags(Item item) {
        // 遍历物品的所有标签
        for (TagKey<Item> tagKey : item.builtInRegistryHolder().tags().toList()) {
            // 检查是否是forge:tools标签或其子标签
            if (tagKey.location().getNamespace().equals("forge") && 
                tagKey.location().getPath().startsWith("tools")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查物品是否能执行常见工具动作
     * @param item 要检查的物品
     * @return 如果能执行常见工具动作返回true
     */
    private static boolean canPerformCommonToolActions(Item item) {
        ItemStack stack = new ItemStack(item);
        
        // 检查是否能执行常见的工具动作
        return stack.canPerformAction(ToolActions.AXE_DIG) ||
               stack.canPerformAction(ToolActions.PICKAXE_DIG) ||
               stack.canPerformAction(ToolActions.SHOVEL_DIG) ||
               stack.canPerformAction(ToolActions.HOE_DIG) ||
               stack.canPerformAction(ToolActions.SWORD_DIG) ||
               stack.canPerformAction(ToolActions.SHOVEL_FLATTEN) ||
               stack.canPerformAction(ToolActions.HOE_TILL) ||
               stack.canPerformAction(ToolActions.SWORD_SWEEP) ||
               stack.canPerformAction(ToolActions.SHIELD_BLOCK);
    }
    
    /**
     * 检查物品是否为原版武器类型
     * @param item 要检查的物品
     * @return 如果是原版武器类型返回true
     */
    private static boolean isVanillaWeaponType(Item item) {
        return item instanceof SwordItem ||
               item instanceof AxeItem ||
               item instanceof TridentItem ||
               item instanceof ShovelItem ||
               item instanceof PickaxeItem ||
               item instanceof HoeItem;
    }
    
    /**
     * 检查物品是否有正数的攻击伤害属性
     * @param item 要检查的物品
     * @return 如果有正数的攻击伤害属性返回true
     */
    private static boolean hasPositiveAttackDamage(Item item) {
        ItemStack stack = new ItemStack(item);
        
        // 获取物品的攻击伤害修饰符
        var attributeModifiers = item.getAttributeModifiers(EquipmentSlot.MAINHAND, stack);
        var attackDamageModifiers = attributeModifiers.get(Attributes.ATTACK_DAMAGE);
        
        // 检查基础攻击伤害（如果物品是武器类型）
        if (item instanceof TieredItem tieredItem) {
            if (tieredItem instanceof SwordItem) {
                // 原版剑的默认攻击伤害为3
                return true;
            } else if (tieredItem instanceof AxeItem) {
                // 原版斧头的默认攻击伤害通常大于0
                return true;
            }
        }
        
        // 检查是否有正数的攻击伤害修饰符
        if (attackDamageModifiers != null) {
            for (var modifier : attackDamageModifiers) {
                if (modifier.getAmount() > 0) {
                    return true;
                }
            }
        }
        
        // 对于一些特殊的武器类型
        if (item instanceof TridentItem) {
            return true; // 三叉戟是武器
        }
        
        return false;
    }
    
    /**
     * 检查物品是否为需要排除的物品（slashblade:slashblade或modid为tacz的物品）
     * @param item 要检查的物品
     * @return 如果是需要排除的物品返回true
     */
    private static boolean isExcludedItem(Item item) {
        // 检查物品注册名
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        String registryNameStr = registryName != null ? registryName.toString() : "";
        
        // 排除slashblade:slashblade
        if ("slashblade:slashblade".equals(registryNameStr)) {
            return true;
        }
        
        // 排除modid为tacz的物品
        if (registryName != null && "tacz".equals(registryName.getNamespace())) {
            return true;
        }
        
        return false;
    }
}