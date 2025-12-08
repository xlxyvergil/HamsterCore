package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 元素属性实例类，表示一个具体的元素属性
 * 简化设计，只保留必要信息：数值、类型、位置、是否生效、来源
 */
public class ElementInstance {
    private final ElementType type;       // 元素类型
    private final double value;          // 元素数值
    private final int position;           // 在NBT中的位置（顺序）
    private final boolean isActive;       // 是否生效
    private final ElementSource source;   // 元素来源
    
    // 构造函数
    public ElementInstance(ElementType type, double value, int position, 
                         boolean isActive, ElementSource source) {
        this.type = type;
        this.value = value;
        this.position = position;
        this.isActive = isActive;
        this.source = source;
        
        // 参数范围验证
        if (value < 0.0) {
            throw new IllegalArgumentException("Element value must be non-negative");
        }
    }
    
    // 便利构造方法
    public ElementInstance(ElementType type, double value, int position) {
        this(type, value, position, true, ElementSource.DIRECT);
    }
    
    // 便利方法
    public ElementInstance withValue(double newValue) {
        return new ElementInstance(type, newValue, position, isActive, source);
    }
    
    public ElementInstance withActiveState(boolean newActiveState) {
        return new ElementInstance(type, value, position, newActiveState, source);
    }
    
    // Getters
    public ElementType getType() { return type; }
    public double getValue() { return value; }
    public int getPosition() { return position; }
    public boolean isActive() { return isActive; }
    public boolean isInactive() { return !isActive; }
    public ElementSource getSource() { return source; }
    
    /**
     * 检查是否应该触发效果
     * @param random 随机数生成器
     * @param triggerChance 武器的触发率
     * @return 是否触发
     */
    public boolean shouldTrigger(java.util.Random random, double triggerChance) {
        return isActive && random.nextDouble() <= triggerChance;
    }
    
    /**
     * 从NBT创建实例
     */
    public static ElementInstance fromNBT(CompoundTag tag) {
        String typeName = tag.getString("type");
        ElementType elementType = ElementType.byName(typeName);
        if (elementType == null) {
            return null;
        }
        
        double value = tag.getDouble("value");
        int position = tag.getInt("position");
        boolean isActive = tag.getBoolean("is_active");
        String sourceStr = tag.getString("source");
        ElementSource source;
        
        try {
            source = ElementSource.valueOf(sourceStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 兼容旧版本或错误数据，默认为DIRECT
            source = ElementSource.DIRECT;
        }
        
        return new ElementInstance(elementType, value, position, isActive, source);
    }
    
    /**
     * 保存到NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.getName());
        tag.putDouble("value", value);
        tag.putInt("position", position);
        tag.putBoolean("is_active", isActive);
        tag.putString("source", source.name().toLowerCase());
        return tag;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ElementInstance that = (ElementInstance) obj;
        return position == that.position && type == that.type;
    }
    
    @Override
    public int hashCode() {
        return 31 * type.hashCode() + position;
    }
    
    @Override
    public String toString() {
        return String.format("ElementInstance{type=%s, value=%.2f, position=%d, active=%s, source=%s}",
                           type, value, position, isActive, source);
    }
}