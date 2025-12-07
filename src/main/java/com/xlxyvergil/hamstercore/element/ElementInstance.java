package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;

/**
 * 元素属性实例类，表示一个具体的元素属性
 * 连接ElementAttribute和实际应用的桥梁
 */
public record ElementInstance(
    ElementAttribute attribute,
    double value
) {
    
    /**
     * 创建一个元素属性实例
     * @param attribute 元素属性类型
     * @param value 元素数值
     */
    public ElementInstance {
        // 参数范围验证
        if (value < 0.0) {
            throw new IllegalArgumentException("Element value must be non-negative");
        }
    }
    
    /**
     * 静态工厂方法，创建带有默认值的元素实例
     */
    public static ElementInstance create(ElementAttribute attribute, double value) {
        return new ElementInstance(attribute, value);
    }
    
    /**
     * 获取元素类型
     */
    public ElementType getType() {
        return attribute.getType();
    }
    
    /**
     * 检查是否应该触发效果
     * @param random 随机数生成器
     * @param triggerChance 武器的触发率
     * @return 是否触发
     */
    public boolean shouldTrigger(java.util.Random random, double triggerChance) {
        return random.nextDouble() <= triggerChance;
    }
    
    /**
     * 将此元素实例保存到NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getType().getName());
        tag.putDouble("value", value);
        return tag;
    }
    
    /**
     * 从NBT加载元素实例
     */
    public static ElementInstance fromNBT(CompoundTag tag) {
        String typeName = tag.getString("type");
        ElementType type = ElementType.byName(typeName);
        if (type == null) {
            return null;
        }
        
        double value = tag.getDouble("value");
        
        ElementAttribute attribute = ElementRegistry.getAttribute(type);
        if (attribute == null) {
            return null;
        }
        
        return new ElementInstance(attribute, value);
    }
    
    /**
     * 创建一个新的元素实例，带有不同的数值
     */
    public ElementInstance withValue(double newValue) {
        return new ElementInstance(attribute, newValue);
    }
}