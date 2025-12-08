package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 元素实例类，表示一个具体的元素及其数值和位置信息
 */
public class ElementInstance {
    private final ElementType type;      // 元素类型
    private final double value;          // 元素数值
    private final int position;          // 元素位置
    private final boolean active;        // 是否生效
    
    public ElementInstance(ElementType type, double value, int position, boolean active) {
        this.type = type;
        this.value = value;
        this.position = position;
        this.active = active;
    }
    
    public ElementType getType() {
        return type;
    }
    
    public double getValue() {
        return value;
    }
    
    public int getPosition() {
        return position;
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * 创建一个新的元素实例，仅更改激活状态
     */
    public ElementInstance withActiveState(boolean newActiveState) {
        if (this.active == newActiveState) {
            return this;
        }
        return new ElementInstance(this.type, this.value, this.position, newActiveState);
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
        
        return new ElementInstance(elementType, value, position, isActive);
    }
    
    /**
     * 保存到NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.getName());
        tag.putDouble("value", value);
        tag.putInt("position", position);
        tag.putBoolean("is_active", active);
        return tag;
    }
    
    @Override
    public String toString() {
        return String.format("ElementInstance{type=%s, value=%.2f, position=%d, active=%s}", 
                           type.getName(), value, position, active);
    }
}