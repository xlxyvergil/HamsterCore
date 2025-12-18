package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * 武器数据类
 * 合并了WeaponElementData的功能，包含两层数据结构
 * Basic: 基础数据 - 记录元素名称、来源和添加顺序
 * InitialModifiers: 初始修饰符列表
 */
public class WeaponData {
    // MOD相关信息字段
    public String modid;
    public String itemId;
    public String gunId;
    public String translationKey;
    
    // Basic层: 记录元素名称、来源和添加顺序
    private final Map<String, List<BasicEntry>> basicElements = new LinkedHashMap<>();
    
    // InitialModifiers层: 初始修饰符列表
    private final List<InitialModifierEntry> initialModifiers = new ArrayList<>();
    
    /**
     * 添加Basic层元素
     * 每种元素类型只能有一个条目，记录首次添加的信息
     */
    public void addBasicElement(String type, String source, int order) {
        // 检查该类型是否已存在
        if (basicElements.containsKey(type)) {
            // 如果已存在，不进行任何操作，保持首次添加的记录
            return;
        }
        
        BasicEntry entry = new BasicEntry(type, source, order);
        basicElements.put(type, new ArrayList<>(Arrays.asList(entry)));
    }
    
    /**
     * 获取按优先级排序的Basic层元素列表
     * 优先级顺序: def ＜ user
     */
    public List<BasicEntry> getSortedBasicElements() {
        List<BasicEntry> sortedEntries = new ArrayList<>();
        
        // 收集所有BasicEntry
        for (List<BasicEntry> entries : basicElements.values()) {
            sortedEntries.addAll(entries);
        }
        
        // 按照优先级排序: def ＜ user
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
     * @param source 来源 ("def", "user")
     * @return 优先级数值，越小优先级越高
     */
    private int getPriority(String source) {
        switch (source.toUpperCase()) {
            case "DEF": 
                return 1; // 最低优先级
            case "USER":
                return 0; // 最高优先级
            default:
                return 1; // 未知来源，最低优先级
        }
    }
    
    /**
     * 获取Basic层元素（按添加顺序排序）
     */
    public Map<String, List<BasicEntry>> getBasicElements() {
        // 创建一个新的LinkedHashMap，按照order顺序重新排列
        Map<String, List<BasicEntry>> sortedBasicElements = new LinkedHashMap<>();
        
        // 将所有entry收集到一个列表中，按order排序
        List<Map.Entry<String, List<BasicEntry>>> entries = new ArrayList<>(basicElements.entrySet());
        entries.sort((e1, e2) -> {
            int order1 = e1.getValue().get(0).getOrder();
            int order2 = e2.getValue().get(0).getOrder();
            return Integer.compare(order1, order2);
        });
        
        // 按顺序添加到新的Map中
        for (Map.Entry<String, List<BasicEntry>> entry : entries) {
            sortedBasicElements.put(entry.getKey(), entry.getValue());
        }
        
        return sortedBasicElements;
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
        
        // 保存InitialModifiers层数据
        ListTag initialModifiersTag = new ListTag();
        for (InitialModifierEntry entry : initialModifiers) {
            initialModifiersTag.add(entry.toNBT());
        }
        tag.put("initialModifiers", initialModifiersTag);
        
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
                // 确保每种元素类型只保留第一个条目（按order排序）
                if (!entries.isEmpty()) {
                    entries.sort((e1, e2) -> Integer.compare(e1.getOrder(), e2.getOrder()));
                    List<BasicEntry> singleEntry = new ArrayList<>();
                    singleEntry.add(entries.get(0)); // 只保留order最小的条目
                    data.basicElements.put(key, singleEntry);
                }
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponData that = (WeaponData) o;
        return Objects.equals(modid, that.modid) &&
               Objects.equals(itemId, that.itemId) &&
               Objects.equals(gunId, that.gunId) &&
               Objects.equals(translationKey, that.translationKey) &&
               Objects.equals(basicElements, that.basicElements) &&
               Objects.equals(initialModifiers, that.initialModifiers);
    }
    
    /**
     * 获取指定类型的使用值
     * 从InitialModifiers层获取数据
     */
    public Double getUsageValue(String type) {
        for (InitialModifierEntry entry : initialModifiers) {
            if (entry.getElementType().equals(type)) {
                return entry.getAmount();
            }
        }
        return null;
    }
    
    /**
     * 获取所有使用元素
     * 从InitialModifiers层获取数据
     */
    public Map<String, Double> getUsageElements() {
        Map<String, Double> usageElements = new HashMap<>();
        for (InitialModifierEntry entry : initialModifiers) {
            usageElements.put(entry.getElementType(), entry.getAmount());
        }
        return usageElements;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(modid, itemId, gunId, translationKey, basicElements, initialModifiers);
    }
}