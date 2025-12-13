package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * 元素修饰符数值工具类
 * 提供从物品修饰符中获取元素数值的功能
 */
public class ElementModifierValueUtil {
    
    /**
     * 从物品的属性中获取指定元素类型的数值
     * 利用Forge的修饰符系统自动计算所有修饰符的最终值
     * @param stack 物品堆
     * @param elementType 元素类型
     * @return 元素数值
     */
    public static double getElementValueFromAttributes(ItemStack stack, ElementType elementType) {
        // 获取物品指定装备槽位上的所有攻击伤害属性修饰符
        Collection<AttributeModifier> modifiers = stack.getAttributeModifiers(stack.getEquipmentSlot()).get(Attributes.ATTACK_DAMAGE);
        if (modifiers == null || modifiers.isEmpty()) {
            return 0.0;
        }
        
        double totalValue = 0.0;
        
        // 遍历所有攻击伤害修饰符，查找与指定元素类型相关的修饰符
        for (AttributeModifier modifier : modifiers) {
            // 检查修饰符名称是否与元素类型匹配
            // 使用元素属性的标识符进行匹配
            String identifier = "hamstercore:" + elementType.getName();
            if (modifier.getName() != null && modifier.getName().equals(identifier)) {
                totalValue += modifier.getAmount();
            }
        }
        
        return totalValue;
    }
}