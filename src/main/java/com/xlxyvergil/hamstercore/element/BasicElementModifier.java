package com.xlxyvergil.hamstercore.element;

/**
 * 基础元素修饰符类
 * 处理基础元素（冰冻、电击、火焰、毒素）的修饰符
 */
public class BasicElementModifier extends BaseElementModifier {
    
    public BasicElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue);
        // 确保是基础元素类型
        if (!elementType.isBasic()) {
            throw new IllegalArgumentException("ElementType must be basic type");
        }
    }
}