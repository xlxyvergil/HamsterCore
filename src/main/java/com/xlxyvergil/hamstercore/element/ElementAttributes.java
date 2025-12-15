package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;
import com.xlxyvergil.hamstercore.util.ElementHelper;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

/**
 * 元素属性API层
 * 参考GunsmithLib的GunAttributes实现方式，提供对元素属性的公共访问
 */
public class ElementAttributes {
    
    /**
     * 获取元素类型对应的属性
     * 
     * @param elementType 元素类型
     * @return 元素属性，以Optional包装
     */
    public static Optional<ElementBasedAttribute> getAttribute(ElementType elementType) {
        return Optional.ofNullable(ElementRegistry.getAttributeValue(elementType));
    }
    
    /**
     * 获取元素类型对应的属性注册对象
     * 
     * @param elementType 元素类型
     * @return 元素属性注册对象，以Optional包装
     */
    public static Optional<RegistryObject<ElementBasedAttribute>> getAttributeRegistry(ElementType elementType) {
        return Optional.ofNullable(ElementRegistry.getAttribute(elementType));
    }
    
    /**
     * 检查元素类型是否有对应的属性
     * 
     * @param elementType 元素类型
     * @return 如果有对应的属性则返回true，否则返回false
     */
    public static boolean hasAttribute(ElementType elementType) {
        return getAttribute(elementType).isPresent();
    }
    
    /**
     * 获取元素类型对应的属性值
     * 
     * @param elementType 元素类型
     * @param itemStack 物品堆
     * @return 属性值，如果属性不存在则返回0.0
     */
    public static double getValue(ElementType elementType, ItemStack itemStack) {
        return getAttribute(elementType)
                .map(attribute -> ElementHelper.getElementValueFromItem(itemStack, attribute))
                .orElse(0.0);
    }
    
    /**
     * 为元素类型创建属性修饰符条目
     * 
     * @param elementType 元素类型
     * @param id 修饰符UUID
     * @param amount 修饰符数值
     * @param operation 修饰符操作类型
     * @param name 修饰符名称
     * @return 元素属性修饰符条目，以Optional包装
     */
    public static Optional<ElementAttributeModifierEntry> createModifierEntry(
            ElementType elementType, 
            java.util.UUID id, 
            double amount, 
            AttributeModifier.Operation operation, 
            String name) {
        if (!hasAttribute(elementType)) {
            return Optional.empty();
        }
        
        return Optional.of(new ElementAttributeModifierEntry(elementType, id, amount, name, operation));
    }
    
    /**
     * 为元素类型创建属性修饰符条目（使用默认名称）
     * 
     * @param elementType 元素类型
     * @param id 修饰符UUID
     * @param amount 修饰符数值
     * @param operation 修饰符操作类型
     * @return 元素属性修饰符条目，以Optional包装
     */
    public static Optional<ElementAttributeModifierEntry> createModifierEntry(
            ElementType elementType, 
            java.util.UUID id, 
            double amount, 
            AttributeModifier.Operation operation) {
        if (!hasAttribute(elementType)) {
            return Optional.empty();
        }
        
        return Optional.of(new ElementAttributeModifierEntry(elementType, id, amount, elementType.getDisplayName(), operation));
    }
    
    /**
     * 检查属性是否为元素属性
     * 
     * @param attribute 属性对象
     * @return 如果是元素属性则返回true，否则返回false
     */
    public static boolean isElementAttribute(Attribute attribute) {
        return attribute instanceof ElementBasedAttribute;
    }
    
    /**
     * 将属性转换为元素属性
     * 
     * @param attribute 属性对象
     * @return 元素属性，以Optional包装
     */
    public static Optional<ElementBasedAttribute> asElementAttribute(Attribute attribute) {
        if (isElementAttribute(attribute)) {
            return Optional.of((ElementBasedAttribute) attribute);
        }
        return Optional.empty();
    }
    
    /**
     * 获取元素属性的元素类型
     * 
     * @param attribute 元素属性
     * @return 元素类型，以Optional包装
     */
    public static Optional<ElementType> getElementType(ElementBasedAttribute attribute) {
        if (attribute != null) {
            return Optional.of(attribute.getElementType());
        }
        return Optional.empty();
    }
}
