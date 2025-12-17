package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * Basic层条目类
 * 表示Basic层的单个元素条目
 */
public class BasicEntry {
    private final String type;     // 元素类型
    private final String source;   // 来源 ( "USER" | "DEF")
    private final int order;       // 添加顺序
    
    public BasicEntry(String type, String source, int order) {
        this.type = type;
        this.source = source;
        this.order = order;
    }
    
    public String getType() {
        return type;
    }
    
    public String getSource() {
        return source;
    }
    
    public int getOrder() {
        return order;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicEntry that = (BasicEntry) o;
        return order == that.order &&
               Objects.equals(type, that.type) &&
               Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, source, order);
    }
    
    /**
     * 将BasicEntry转换为NBT标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type);
        tag.putString("source", source);
        tag.putInt("order", order);
        return tag;
    }
    
    /**
     * 从NBT标签创建BasicEntry
     */
    public static BasicEntry fromNBT(CompoundTag tag) {
        String type = tag.getString("type");
        String source = tag.getString("source");
        int order = tag.getInt("order");
        return new BasicEntry(type, source, order);
    }
}