package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementAttributeEvents.ElementModifierWrapper;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;
import com.xlxyvergil.hamstercore.handler.ElementModifierEventHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

/**
 * 元素属性API
 * 提供元素属性注册和管理的相关接口
 */
public class ElementAttributeAPI {
    
    /**
     * 获取指定类型的元素属性
     * 
     * @param type 元素类型
     * @return 元素属性实例
     */
    public static RegistryObject<ElementAttribute> getElementAttribute(ElementType type) {
        return ElementRegistry.getAttribute(type);
    }
    
    /**
     * 获取指定类型的元素属性值
     * 
     * @param type 元素类型
     * @return 元素属性实例，如果未注册则返回null
     */
    public static ElementAttribute getElementAttributeValue(ElementType type) {
        return ElementRegistry.getAttributeValue(type);
    }
    
    /**
     * 检查物品是否有任何元素修饰符
     * 
     * @param stack 物品堆
     * @return 如果物品有元素修饰符则返回true，否则返回false
     */
    public static boolean hasAnyElementModifiers(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 从物品的NBT中加载武器数据
        WeaponData weaponData = WeaponDataManager.loadElementData(stack);
        return weaponData != null && weaponData.getInitialModifiers() != null && !weaponData.getInitialModifiers().isEmpty();
    }
    
    /**
     * 获取物品上的所有元素修饰符
     * 
     * @param stack 物品堆
     * @return 包含所有元素修饰符的映射表，键为属性，值为修饰符包装器集合
     */
    public static Map<Attribute, Set<ElementModifierWrapper>> getAllElementModifiers(ItemStack stack) {
        Map<Attribute, Set<ElementModifierWrapper>> result = new HashMap<>();
        
        if (stack.isEmpty()) {
            return result;
        }
        
        // 从物品的NBT中加载武器数据
        WeaponData weaponData = WeaponDataManager.loadElementData(stack);
        if (weaponData == null || weaponData.getInitialModifiers() == null || weaponData.getInitialModifiers().isEmpty()) {
            return result;
        }
        
        // 转换初始修饰符为ElementAttributeModifierEntry
        List<ElementAttributeModifierEntry> elementModifiers = ElementModifierEventHandler.convertToElementModifierEntries(weaponData.getInitialModifiers());
        
        // 转换为ElementModifierWrapper格式
        for (ElementAttributeModifierEntry modifierEntry : elementModifiers) {
            // 获取对应的元素属性
            RegistryObject<ElementAttribute> attributeRegistry = ElementRegistry.getAttribute(modifierEntry.getElementType());
            if (attributeRegistry != null && attributeRegistry.isPresent()) {
                // 创建AttributeModifier
                AttributeModifier minecraftModifier = new AttributeModifier(
                    modifierEntry.getId(),
                    modifierEntry.getName(),
                    modifierEntry.getAmount(),
                    modifierEntry.getOperation()
                );
                
                // 为所有装备槽创建包装器（这里默认使用MAINHAND，实际应根据物品类型决定）
                ElementModifierWrapper wrapper = new ElementModifierWrapper(
                    attributeRegistry.get(),
                    minecraftModifier,
                    EquipmentSlot.MAINHAND
                );
                
                // 添加到结果映射中
                result.computeIfAbsent(attributeRegistry.get(), k -> new HashSet<>()).add(wrapper);
            }
        }
        
        return result;
    }
}