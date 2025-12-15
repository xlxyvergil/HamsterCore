package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * 基于元素的属性类，支持不同元素类型的属性
 * 参考Apotheosis的PercentBasedAttribute实现
 */
public class ElementAttribute extends RangedAttribute {
    private final ElementType elementType;
    private final boolean isPercentBased;
    
    public ElementAttribute(ElementType elementType, double defaultValue, double min, double max, boolean isPercentBased) {
        super("attribute.name.hamstercore." + elementType.getName(), defaultValue, min, max);
        this.elementType = elementType;
        this.isPercentBased = isPercentBased;
        this.setSyncable(true);
    }
    
    public ElementType getElementType() {
        return elementType;
    }
    
    /**
     * 是否为百分比属性
     */
    public boolean isPercentBased() {
        return isPercentBased;
    }
}
