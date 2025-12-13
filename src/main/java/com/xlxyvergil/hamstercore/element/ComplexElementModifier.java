package com.xlxyvergil.hamstercore.element;

/**
 * 复合元素修饰符类
 * 处理复合元素（爆炸、腐蚀、毒气、磁力、辐射、病毒）的修饰符
 */
public class ComplexElementModifier extends BaseElementModifier {
    
    public ComplexElementModifier(ElementType elementType, double minValue, double maxValue) {
        super(elementType, minValue, maxValue);
        // 确保是复合元素类型
        if (!elementType.isComplex()) {
            throw new IllegalArgumentException("ElementType must be complex type");
        }
    }
}