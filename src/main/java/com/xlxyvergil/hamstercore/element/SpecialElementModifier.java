package com.xlxyvergil.hamstercore.element;

/**
 * 特殊元素修饰符类
 * 处理特殊属性（暴击率、暴击伤害、触发率）和派系元素的修饰符
 */
public class SpecialElementModifier extends BaseElementModifier {
    
    public SpecialElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue);
        // 确保是特殊元素类型
        if (!elementType.isSpecial()) {
            throw new IllegalArgumentException("ElementType must be special type");
        }
    }
}