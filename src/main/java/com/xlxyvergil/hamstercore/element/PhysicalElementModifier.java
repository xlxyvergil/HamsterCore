package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 物理元素修饰符类
 * 处理物理元素（冲击、穿刺、切割）的修饰符
 */
public class PhysicalElementModifier extends BaseElementModifier {
    
    public PhysicalElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue, AttributeModifier.Operation.ADDITION);
        // 确保是物理元素类型
        if (!elementType.isPhysical()) {
            throw new IllegalArgumentException("ElementType must be physical type");
        }
    }
}