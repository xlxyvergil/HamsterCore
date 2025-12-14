package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * 元素属性类
 */
public class ElementAttribute extends RangedAttribute {
    private final ElementType elementType;
    
    public ElementAttribute(ElementType elementType, double defaultValue, double min, double max) {
        super("attribute.name.hamstercore." + ((elementType != null && elementType.getName() != null) ? elementType.getName() : "unknown"), defaultValue, min, max);
        this.elementType = elementType;
        this.setSyncable(true);
    }
    
    public ElementType getElementType() {
        return elementType;
    }
}