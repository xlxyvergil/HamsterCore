package com.xlxyvergil.hamstercore.element;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * 元素属性抽象基类，定义元素属性的基本属性和行为
 * 所有具体的元素类都将继承这个基类
 */
public abstract class ElementAttribute {
    
    protected final ElementType type;
    protected final double defaultValue;
    protected final AttributeModifier.Operation operation;
    
    public ElementAttribute(ElementType type, double defaultValue, AttributeModifier.Operation operation) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.operation = operation;
    }
    
    /**
     * 获取元素类型
     */
    public ElementType getType() {
        return type;
    }
    
    /**
     * 获取默认值
     */
    public double getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * 获取操作类型
     */
    public AttributeModifier.Operation getOperation() {
        return operation;
    }
    
    /**
     * 获取元素的显示名称
     */
    public MutableComponent getDisplayName() {
        return type.getColoredName();
    }
    
    /**
     * 获取元素的描述信息
     */
    public abstract MutableComponent getDescription(ItemStack stack);
    
    /**
     * 获取元素的属性修饰符
     */
    public abstract AttributeModifier createModifier(ItemStack stack, double value);
    
    /**
     * 格式化数值显示     */
    public String formatValue(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value);
        }
    }
    
    /**
     * 获取元素的唯一标识符
     */
    public String getIdentifier() {
        return "hamstercore:" + type.getName();
    }
    
    @Override
    public String toString() {
        return "ElementAttribute{" + type.getName() + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ElementAttribute)) return false;
        ElementAttribute other = (ElementAttribute) obj;
        return type == other.type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
}