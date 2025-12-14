package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 复合元素修饰符类
 * 处理复合元素（病毒、腐蚀、辐射、爆炸、磁力）的修饰符
 */
public class ComplexElementModifier extends BaseElementModifier {
    
    public ComplexElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue, AttributeModifier.Operation.ADDITION);
        // 确保是复合元素类型
        if (!elementType.isComplex()) {
            throw new IllegalArgumentException("ElementType must be complex type");
        }
    }
}