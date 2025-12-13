package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

/**
 * 基础元素修饰符类
 */
public class BaseElementModifier {
    private final ElementType elementType;
    private final double minValue;
    private final double maxValue;
    
    public BaseElementModifier(ElementType elementType, double minValue, double maxValue) {
        this.elementType = elementType;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * 创建属性修饰符
     * @param stack 物品堆
     * @param value 数值
     * @param index 索引
     * @return 属性修饰符
     */
    public AttributeModifier createModifier(ItemStack stack, double value, int index) {
        UUID modifierUUID = ElementRegistry.getModifierUUID(elementType, index);
        String modifierName = ElementRegistry.getModifierName(elementType, index);
        
        return new AttributeModifier(modifierUUID, modifierName, value, AttributeModifier.Operation.ADDITION);
    }
    
    /**
     * 根据数值获取有效值（确保不小于0）
     * @param value 输入值
     * @return 有效值
     */
    protected double getValidValue(double value) {
        // 确保数值不小于0
        return Math.max(0, value);
    }
    
    /**
     * 获取元素类型
     * @return 元素类型
     */
    public ElementType getElementType() {
        return elementType;
    }
    
    /**
     * 获取最小值
     * @return 最小值
     */
    public double getMinValue() {
        return minValue;
    }
    
    /**
     * 获取最大值
     * @return 最大值
     */
    public double getMaxValue() {
        return maxValue;
    }
}