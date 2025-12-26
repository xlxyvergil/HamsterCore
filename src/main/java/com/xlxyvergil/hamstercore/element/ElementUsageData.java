package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素使用数据类
 * 用于存储元素属性的NBT数据，类似Apotheosis的词缀系统
 * 将元素计算结果直接存储到物品的NBT中
 */
public class ElementUsageData {
    
    // NBT标签键名
    private static final String ELEMENT_DATA_KEY = "ElementUsageData";
    private static final String CRITICAL_STATS_KEY = "CriticalStats";
    private static final String PHYSICAL_ELEMENTS_KEY = "PhysicalElements";
    private static final String FACTION_ELEMENTS_KEY = "FactionElements";
    private static final String COMBINED_ELEMENTS_KEY = "CombinedElements";
    
    /**
     * 将元素数据写入物品的NBT标签中
     * @param stack 物品栈
     * @param elementData 元素数据
     */
    public static void writeElementDataToItem(ItemStack stack, ElementData elementData) {
        if (stack.isEmpty()) {
            return;
        }
        
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag elementTag = new CompoundTag();
        
        // 写入暴击相关统计
        if (elementData.getCriticalStats() != null && !elementData.getCriticalStats().isEmpty()) {
            CompoundTag criticalStatsTag = new CompoundTag();
            for (Map.Entry<String, Double> entry : elementData.getCriticalStats().entrySet()) {
                criticalStatsTag.putDouble(entry.getKey(), entry.getValue());
            }
            elementTag.put(CRITICAL_STATS_KEY, criticalStatsTag);
        }
        
        // 写入物理元素
        if (elementData.getPhysicalElements() != null && !elementData.getPhysicalElements().isEmpty()) {
            CompoundTag physicalElementsTag = new CompoundTag();
            for (Map.Entry<String, Double> entry : elementData.getPhysicalElements().entrySet()) {
                physicalElementsTag.putDouble(entry.getKey(), entry.getValue());
            }
            elementTag.put(PHYSICAL_ELEMENTS_KEY, physicalElementsTag);
        }
        
        // 写入派系元素
        if (elementData.getFactionElements() != null && !elementData.getFactionElements().isEmpty()) {
            CompoundTag factionElementsTag = new CompoundTag();
            for (Map.Entry<String, Double> entry : elementData.getFactionElements().entrySet()) {
                factionElementsTag.putDouble(entry.getKey(), entry.getValue());
            }
            elementTag.put(FACTION_ELEMENTS_KEY, factionElementsTag);
        }
        
        // 写入复合元素
        if (elementData.getCombinedElements() != null && !elementData.getCombinedElements().isEmpty()) {
            CompoundTag combinedElementsTag = new CompoundTag();
            for (Map.Entry<String, Double> entry : elementData.getCombinedElements().entrySet()) {
                combinedElementsTag.putDouble(entry.getKey(), entry.getValue());
            }
            elementTag.put(COMBINED_ELEMENTS_KEY, combinedElementsTag);
        }
        
        tag.put(ELEMENT_DATA_KEY, elementTag);
        stack.setTag(tag);
    }
    
    /**
     * 从物品的NBT标签中读取元素数据
     * @param stack 物品栈
     * @return 元素数据
     */
    public static ElementData readElementDataFromItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return new ElementData();
        }
        
        CompoundTag tag = stack.getTag();
        if (!tag.contains(ELEMENT_DATA_KEY)) {
            return new ElementData();
        }
        
        CompoundTag elementTag = tag.getCompound(ELEMENT_DATA_KEY);
        ElementData elementData = new ElementData();
        
        // 读取暴击相关统计
        if (elementTag.contains(CRITICAL_STATS_KEY)) {
            CompoundTag criticalStatsTag = elementTag.getCompound(CRITICAL_STATS_KEY);
            for (String key : criticalStatsTag.getAllKeys()) {
                elementData.getCriticalStats().put(key, criticalStatsTag.getDouble(key));
            }
        }
        
        // 读取物理元素
        if (elementTag.contains(PHYSICAL_ELEMENTS_KEY)) {
            CompoundTag physicalElementsTag = elementTag.getCompound(PHYSICAL_ELEMENTS_KEY);
            for (String key : physicalElementsTag.getAllKeys()) {
                elementData.getPhysicalElements().put(key, physicalElementsTag.getDouble(key));
            }
        }
        
        // 读取派系元素
        if (elementTag.contains(FACTION_ELEMENTS_KEY)) {
            CompoundTag factionElementsTag = elementTag.getCompound(FACTION_ELEMENTS_KEY);
            for (String key : factionElementsTag.getAllKeys()) {
                elementData.getFactionElements().put(key, factionElementsTag.getDouble(key));
            }
        }
        
        // 读取复合元素
        if (elementTag.contains(COMBINED_ELEMENTS_KEY)) {
            CompoundTag combinedElementsTag = elementTag.getCompound(COMBINED_ELEMENTS_KEY);
            for (String key : combinedElementsTag.getAllKeys()) {
                elementData.getCombinedElements().put(key, combinedElementsTag.getDouble(key));
            }
        }
        
        return elementData;
    }
    
    /**
     * 检查物品是否包含元素数据
     * @param stack 物品栈
     * @return 是否包含元素数据
     */
    public static boolean hasElementData(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        
        CompoundTag tag = stack.getTag();
        return tag.contains(ELEMENT_DATA_KEY);
    }
    
    /**
     * 从物品中移除元素数据
     * @param stack 物品栈
     */
    public static void removeElementData(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        
        CompoundTag tag = stack.getTag();
        if (tag.contains(ELEMENT_DATA_KEY)) {
            tag.remove(ELEMENT_DATA_KEY);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
    
    /**
     * 元素数据内部类
     * 存储各种类型的元素值
     */
    public static class ElementData {
        // 暴击相关统计：暴击率、暴击伤害、触发率等
        private Map<String, Double> criticalStats = new HashMap<>();
        
        // 物理元素值
        private Map<String, Double> physicalElements = new HashMap<>();
        
        // 派系元素值
        private Map<String, Double> factionElements = new HashMap<>();
        
        // 复合元素值
        private Map<String, Double> combinedElements = new HashMap<>();
        
        public Map<String, Double> getCriticalStats() {
            return criticalStats;
        }
        
        public void setCriticalStats(Map<String, Double> criticalStats) {
            this.criticalStats = criticalStats;
        }
        
        public Map<String, Double> getPhysicalElements() {
            return physicalElements;
        }
        
        public void setPhysicalElements(Map<String, Double> physicalElements) {
            this.physicalElements = physicalElements;
        }
        
        public Map<String, Double> getFactionElements() {
            return factionElements;
        }
        
        public void setFactionElements(Map<String, Double> factionElements) {
            this.factionElements = factionElements;
        }
        
        public Map<String, Double> getCombinedElements() {
            return combinedElements;
        }
        
        public void setCombinedElements(Map<String, Double> combinedElements) {
            this.combinedElements = combinedElements;
        }
        
        /**
         * 清空所有元素数据
         */
        public void clear() {
            criticalStats.clear();
            physicalElements.clear();
            factionElements.clear();
            combinedElements.clear();
        }
        
        /**
         * 检查是否为空
         * @return 如果没有任何元素数据则返回true
         */
        public boolean isEmpty() {
            return criticalStats.isEmpty() && 
                   physicalElements.isEmpty() && 
                   factionElements.isEmpty() && 
                   combinedElements.isEmpty();
        }
    }
}