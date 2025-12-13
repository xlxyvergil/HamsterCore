package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器数据类
 * 存储三层NBT数据结构：Basic层、Usage层和Def层
 */
public class WeaponData {
    // Basic层：记录元素名称、来源和添加顺序
    private final Map<String, List<BasicEntry>> basicElements = new LinkedHashMap<>();
    
    // Usage层：元素复合后的元素类型和数值
    private final Map<String, Double> usageElements = new HashMap<>();
    
    // Def层：默认元素数据
    private final Map<String, Double> defElements = new HashMap<>();
    
    /**
     * Basic层条目内部类
     * 表示Basic层的单个元素条目
     */
    public static class BasicEntry {
        private final String type;     // 元素类型
        private final String source;   // 来源 ("CONFIG" | "USER" | "DEF")
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
    
    /**
     * 添加Basic层元素
     */
    public void addBasicElement(String type, String source, int order) {
        BasicEntry entry = new BasicEntry(type, source, order);
        basicElements.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
    }
    
    /**
     * 获取按优先级排序的Basic层元素列表
     * 优先级顺序: Def > Config > User
     */
    public List<BasicEntry> getSortedBasicElements() {
        List<BasicEntry> sortedEntries = new ArrayList<>();
        
        // 收集所有BasicEntry
        for (List<BasicEntry> entries : basicElements.values()) {
            sortedEntries.addAll(entries);
        }
        
        // 按照优先级排序: Def > Config > User
        sortedEntries.sort((entry1, entry2) -> {
            int priority1 = getPriority(entry1.getSource());
            int priority2 = getPriority(entry2.getSource());
            
            // 首先按优先级排序
            int priorityComparison = Integer.compare(priority1, priority2);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            // 然后按添加顺序排序
            return Integer.compare(entry1.getOrder(), entry2.getOrder());
        });
        
        return sortedEntries;
    }
    
    /**
     * 获取来源优先级
     * @param source 来源 ("DEF", "CONFIG", "USER")
     * @return 优先级数值，越小优先级越高
     */
    private int getPriority(String source) {
        switch (source.toUpperCase()) {
            case "DEF": 
                return 0; // 最高优先级
            case "CONFIG":
                return 1; // 中等优先级
            case "USER":
                return 2; // 最低优先级
            default:
                return 3; // 未知来源，最低优先级
        }
    }
    
    /**
     * 设置Usage层元素
     */
    public void setUsageElement(String type, double value) {
        usageElements.put(type, value);
    }
    
    /**
     * 设置Def层元素
     */
    public void setDefElement(String type, double value) {
        defElements.put(type, value);
    }
    
    /**
     * 获取Basic层元素
     */
    public Map<String, List<BasicEntry>> getBasicElements() {
        return basicElements;
    }
    
    /**
     * 获取Usage层元素
     */
    public Map<String, Double> getUsageElements() {
        return usageElements;
    }
    
    /**
     * 获取Def层元素
     */
    public Map<String, Double> getDefElements() {
        return defElements;
    }
    
    /**
     * 清空Usage层元素
     */
    public void clearUsageElements() {
        usageElements.clear();
    }
    
    /**
     * 计算Usage层数据（基于Def层和Basic层数据）
     * 这个方法在物品属性被查询时调用，用于动态计算元素值
     */
    public void computeUsageData(ItemStack stack) {
        // 使用ElementCombinationModifier来计算复合元素
        ElementCombinationModifier.computeElementCombinations(this, stack);
    }
    
    /**
     * 将WeaponData转换为NBT标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        
        // 保存Basic层数据
        CompoundTag basicTag = new CompoundTag();
        for (Map.Entry<String, List<BasicEntry>> entry : basicElements.entrySet()) {
            ListTag listTag = new ListTag();
            for (BasicEntry basicEntry : entry.getValue()) {
                listTag.add(basicEntry.toNBT());
            }
            basicTag.put(entry.getKey(), listTag);
        }
        tag.put("basic", basicTag);
        
        // 保存Usage层数据
        CompoundTag usageTag = new CompoundTag();
        for (Map.Entry<String, Double> entry : usageElements.entrySet()) {
            usageTag.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put("usage", usageTag);
        
        // 保存Def层数据
        CompoundTag defTag = new CompoundTag();
        for (Map.Entry<String, Double> entry : defElements.entrySet()) {
            defTag.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put("def", defTag);
        
        return tag;
    }
    
    /**
     * 从NBT标签创建WeaponData
     */
    public static WeaponData fromNBT(CompoundTag tag) {
        WeaponData data = new WeaponData();
        
        // 读取Basic层数据
        if (tag.contains("basic", Tag.TAG_COMPOUND)) {
            CompoundTag basicTag = tag.getCompound("basic");
            for (String key : basicTag.getAllKeys()) {
                ListTag listTag = basicTag.getList(key, Tag.TAG_COMPOUND);
                List<BasicEntry> entries = new ArrayList<>();
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag entryTag = listTag.getCompound(i);
                    entries.add(BasicEntry.fromNBT(entryTag));
                }
                data.basicElements.put(key, entries);
            }
        }
        
        // 读取Usage层数据
        if (tag.contains("usage", Tag.TAG_COMPOUND)) {
            CompoundTag usageTag = tag.getCompound("usage");
            for (String key : usageTag.getAllKeys()) {
                data.usageElements.put(key, usageTag.getDouble(key));
            }
        }
        
        // 读取Def层数据
        if (tag.contains("def", Tag.TAG_COMPOUND)) {
            CompoundTag defTag = tag.getCompound("def");
            for (String key : defTag.getAllKeys()) {
                data.defElements.put(key, defTag.getDouble(key));
            }
        }
        
        return data;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponData that = (WeaponData) o;
        return Objects.equals(basicElements, that.basicElements) &&
               Objects.equals(usageElements, that.usageElements) &&
               Objects.equals(defElements, that.defElements);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(basicElements, usageElements, defElements);
    }
}