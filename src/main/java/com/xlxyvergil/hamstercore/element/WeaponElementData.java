package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * 武器元素数据类，包含三层数据结构
 * Basic: 基础数据 - 记录元素名称、来源和添加顺序
 * Usage: 使用数据 - 元素复合后的元素类型和数值
 * InitialModifiers: 初始修饰符列表
 */
public class WeaponElementData {
    
    // Basic层: 记录元素名称、来源和添加顺序
    private final Map<String, List<BasicEntry>> basicElements = new LinkedHashMap<>();
    
    // Usage层: 元素复合后的元素类型和数值
    private final Map<String, Double> usageElements = new HashMap<>();
    
    // InitialModifiers层: 初始修饰符列表
    private final List<InitialModifierEntry> initialModifiers = new ArrayList<>();
    
    public WeaponElementData() {}
    
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
     * 获取Usage层指定类型的元素值
     */
    public Double getUsageValue(String type) {
        return usageElements.get(type);
    }
    
    /**
     * 获取所有Usage层元素
     */
    public Map<String, Double> getAllUsageValues() {
        return new HashMap<>(usageElements);
    }
    
    /**
     * 清空Usage层元素
     */
    public void clearUsageElements() {
        usageElements.clear();
    }
    
    /**
     * 添加初始修饰符
     */
    public void addInitialModifier(InitialModifierEntry entry) {
        initialModifiers.add(entry);
    }
    
    /**
     * 获取初始修饰符列表
     */
    public List<InitialModifierEntry> getInitialModifiers() {
        return initialModifiers;
    }
    
    /**
     * 将WeaponElementData转换为NBT标签
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
        
        // 保存InitialModifiers层数据
        ListTag initialModifiersTag = new ListTag();
        for (InitialModifierEntry entry : initialModifiers) {
            initialModifiersTag.add(entry.toNBT());
        }
        tag.put("initialModifiers", initialModifiersTag);
        
        return tag;
    }
    
    /**
     * 从NBT标签创建WeaponElementData
     */
    public static WeaponElementData fromNBT(CompoundTag tag) {
        WeaponElementData data = new WeaponElementData();
        
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
        
        // 读取InitialModifiers层数据
        if (tag.contains("initialModifiers", Tag.TAG_LIST)) {
            ListTag initialModifiersTag = tag.getList("initialModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < initialModifiersTag.size(); i++) {
                CompoundTag entryTag = initialModifiersTag.getCompound(i);
                data.initialModifiers.add(InitialModifierEntry.fromNBT(entryTag));
            }
        }
        
        return data;
    }
}