package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.config.ElementConfig;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.impl.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;

/**
 * 武器元素属性应用器
 * 负责从配置文件应用元素属性到武器
 */
public class WeaponElementApplier {
    
    /**
     * 从配置文件应用元素属性到武器
     * @param stack 武器物品
     */
    public static void applyElementsFromConfig(ItemStack stack) {
        // 只对武器和工具应用元素属性
        if (!ElementHelper.isWeaponOrTool(stack)) {
            return;
        }
        
        // 获取武器配置
        WeaponConfig config = ElementConfig.getInstance().getWeaponConfig(stack);
        
        // 应用基础属性
        applyBaseAttributes(stack, config);
        
        // 应用元素配比
        applyElementRatios(stack, config);
    }
    
    /**
     * 应用基础属性
     */
    private static void applyBaseAttributes(ItemStack stack, WeaponConfig config) {
        // 应用暴击率
        CriticalChanceAttribute criticalChanceAttr = (CriticalChanceAttribute) ElementRegistry.getAttribute(ElementType.CRITICAL_CHANCE);
        if (criticalChanceAttr != null) {
            ElementInstance criticalChanceInstance = new ElementInstance(criticalChanceAttr, config.getCriticalChance());
            ElementHelper.addElementAttribute(stack, criticalChanceInstance);
        }
        
        // 应用暴击伤害
        CriticalDamageAttribute criticalDamageAttr = (CriticalDamageAttribute) ElementRegistry.getAttribute(ElementType.CRITICAL_DAMAGE);
        if (criticalDamageAttr != null) {
            ElementInstance criticalDamageInstance = new ElementInstance(criticalDamageAttr, config.getCriticalDamage());
            ElementHelper.addElementAttribute(stack, criticalDamageInstance);
        }
        
        // 应用触发率
        TriggerChanceAttribute triggerChanceAttr = (TriggerChanceAttribute) ElementRegistry.getAttribute(ElementType.TRIGGER_CHANCE);
        if (triggerChanceAttr != null) {
            ElementInstance triggerChanceInstance = new ElementInstance(triggerChanceAttr, config.getTriggerChance());
            ElementHelper.addElementAttribute(stack, triggerChanceInstance);
        }
    }
    
    /**
     * 应用元素配比
     */
    private static void applyElementRatios(ItemStack stack, WeaponConfig config) {
        // 获取元素配比，优先使用配置文件中的配比，如果没有则使用基于物品类型的默认配比
        Map<String, Double> elementRatios = config.getElementRatios();
        if (elementRatios.isEmpty()) {
            elementRatios = WeaponConfig.getDefaultElementRatios(stack);
        }
        
        // 获取物品的基础攻击伤害
        double baseDamage = ElementHelper.getBaseAttackDamage(stack);
        
        for (var entry : elementRatios.entrySet()) {
            String elementName = entry.getKey();
            double ratio = entry.getValue();
            
            // 跳过比例为0的元素
            if (ratio <= 0.0) {
                continue;
            }
            
            // 获取元素类型
            ElementType elementType = ElementType.byName(elementName);
            if (elementType == null) {
                continue;
            }
            
            // 获取元素属性
            ElementAttribute attribute = ElementRegistry.getAttribute(elementType);
            if (attribute == null) {
                continue;
            }
            
            // 计算元素伤害值（基于物品的实际伤害值）
            double elementValue = baseDamage * ratio;
            
            // 创建元素实例
            ElementInstance elementInstance = new ElementInstance(attribute, elementValue);
            
            // 添加到物品
            ElementHelper.addElementAttribute(stack, elementInstance);
        }
    }

    /**
     * 为玩家的所有武器应用元素属性
     * @param player 玩家
     */
    public static void applyElementsToPlayerWeapons(Player player) {
        // 检查主手
        ItemStack mainHandItem = player.getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            applyElementsFromConfig(mainHandItem);
        }
        
        // 检查副手
        ItemStack offHandItem = player.getOffhandItem();
        if (!offHandItem.isEmpty()) {
            applyElementsFromConfig(offHandItem);
        }
        
        // 检查物品栏
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (!slotItem.isEmpty()) {
                applyElementsFromConfig(slotItem);
            }
        }
    }
}