package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 特殊元素修饰符类
 * 处理特殊元素（暴击率、暴击伤害、触发几率）的修饰符
 */
public class SpecialElementModifier extends BaseElementModifier {
    
    public SpecialElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue, AttributeModifier.Operation.ADDITION);
        // 确保是特殊元素类型
        if (!elementType.isSpecial()) {
            throw new IllegalArgumentException("ElementType must be special type");
        }
    }
}