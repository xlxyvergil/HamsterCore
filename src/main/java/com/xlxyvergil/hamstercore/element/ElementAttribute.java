package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * 元素属性类
 */
public class ElementAttribute extends RangedAttribute {
    private final ElementType elementType;
    private final AttributeModifier.Operation operation;
    
    public ElementAttribute(ElementType elementType, double defaultValue, double min, double max) {
        this(elementType, defaultValue, min, max, AttributeModifier.Operation.ADDITION);
    }
    
    public ElementAttribute(ElementType elementType, double defaultValue, double min, double max, AttributeModifier.Operation operation) {
        super("attribute.name.hamstercore." + elementType.getName(), defaultValue, min, max);
        this.elementType = elementType;
        this.operation = operation;
        this.setSyncable(true);
    }
    
    public ElementType getElementType() {
        return elementType;
    }
    
    public AttributeModifier.Operation getOperation() {
        return operation;
    }
}