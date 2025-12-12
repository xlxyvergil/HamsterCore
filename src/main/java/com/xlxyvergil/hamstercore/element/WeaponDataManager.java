package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.modifier.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器数据管理器
 * 负责管理NBT中的四层数据结构：Basic、Computed、Usage、Extra
 */
public class WeaponDataManager {
    
    // NBT标签常量
    public static final String ELEMENT_DATA = "ElementData";
    public static final String BASIC_DATA = "Basic";
    public static final String COMPUTED_DATA = "Computed";
    public static final String USAGE_DATA = "Usage";
    public static final String EXTRA_DATA = "Extra";
    
    /**
     * 从物品NBT读取武器元素数据
     */
    public static WeaponElementData loadElementData(ItemStack stack) {
        return loadElementData(stack, false);
    }
    
    /**
     * 从物品NBT读取武器元素数据
     * @param stack 物品堆
     * @param recomputeUsage 是否重新计算Usage层数据
     */
    public static WeaponElementData loadElementData(ItemStack stack, boolean recomputeUsage) {
        if (stack.isEmpty()) {
            return new WeaponElementData();
        }
        
        // 检查物品是否已有元素数据
        if (stack.hasTag()) {
            CompoundTag elementDataTag = stack.getTagElement(ELEMENT_DATA);
            if (elementDataTag != null) {
                // 物品已有元素数据，直接加载
                WeaponElementData data = new WeaponElementData();
                
                // 读取Basic数据
                if (elementDataTag.contains(BASIC_DATA, Tag.TAG_COMPOUND)) {
                    CompoundTag basicTag = elementDataTag.getCompound(BASIC_DATA);
                    data.setBasic(readBasicEntryMap(basicTag));
                }
                
                // 读取Computed数据
                if (elementDataTag.contains(COMPUTED_DATA, Tag.TAG_COMPOUND)) {
                    CompoundTag computedTag = elementDataTag.getCompound(COMPUTED_DATA);
                    data.setComputed(readComputedEntryMap(computedTag));
                }
                
                // 读取Usage数据
                if (elementDataTag.contains(USAGE_DATA, Tag.TAG_COMPOUND)) {
                    CompoundTag usageTag = elementDataTag.getCompound(USAGE_DATA);
                    data.setUsage(readDoubleMap(usageTag));
                }
                
                // 读取Extra数据
                if (elementDataTag.contains(EXTRA_DATA, Tag.TAG_COMPOUND)) {
                    CompoundTag extraTag = elementDataTag.getCompound(EXTRA_DATA);
                    data.setExtra(readExtraEntryMap(extraTag));
                }
                
                // 如果需要重新计算Usage数据
                if (recomputeUsage) {
                    computeUsageData(stack, data);
                }
                
                return data;
            }
        }
        
        // 物品没有元素数据，从配置文件中加载默认数据
        return loadDefaultElementData(stack);
    }
    
    /**
     * 从配置文件加载物品的默认元素数据
     */
    private static WeaponElementData loadDefaultElementData(ItemStack stack) {
        // 从配置中获取武器数据
        WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            return new WeaponElementData();
        }
        
        // 获取元素数据
        WeaponElementData elementData = weaponData.getElementData();
        if (elementData == null) {
            return new WeaponElementData();
        }
        
        // 计算Usage数据
        computeUsageData(stack, elementData);
        
        // 将数据保存到NBT，确保下次直接从NBT读取
        saveElementData(stack, elementData);
        
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        
        // 返回包含默认数据的元素数据对象
        return elementData;
    }
    
    /**
     * 将武器元素数据写入物品NBT
     */
    public static void saveElementData(ItemStack stack, WeaponElementData data) {
        if (stack.isEmpty()) {
            return;
        }
        
        CompoundTag elementDataTag = stack.getOrCreateTagElement(ELEMENT_DATA);
        
        // 写入Basic数据
        if (!data.getAllBasicElements().isEmpty()) {
            CompoundTag basicTag = writeBasicEntryMap(data.getAllBasicElements());
            elementDataTag.put(BASIC_DATA, (Tag)basicTag);
        } else {
            elementDataTag.remove(BASIC_DATA);
        }
        
        // 写入Computed数据
        if (!data.getAllComputedElements().isEmpty()) {
            CompoundTag computedTag = writeComputedEntryMap(data.getAllComputedElements());
            elementDataTag.put(COMPUTED_DATA, (Tag)computedTag);
        } else {
            elementDataTag.remove(COMPUTED_DATA);
        }
        
        // 写入Usage数据
        if (!data.getAllUsageValues().isEmpty()) {
            CompoundTag usageTag = writeDoubleMap(data.getAllUsageValues());
            elementDataTag.put(USAGE_DATA, (Tag)usageTag);
        } else {
            elementDataTag.remove(USAGE_DATA);
        }
        
        // 写入Extra数据
        if (!data.getAllExtraFactions().isEmpty()) {
            CompoundTag extraTag = writeExtraEntryMap(data.getAllExtraFactions());
            elementDataTag.put(EXTRA_DATA, (Tag)extraTag);
        } else {
            elementDataTag.remove(EXTRA_DATA);
        }
    }
    
    /**
     * 添加计算数据元素
     */
    public static void addComputedElement(ItemStack stack, String name, double value, String operation) {
        WeaponElementData data = loadElementData(stack);
        data.addComputedElement(name, value, operation);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 添加带具体来源标识符的计算数据元素
     */
    public static void addComputedElementWithSpecificSource(ItemStack stack, String name, double value, String operation, String specificSource) {
        WeaponElementData data = loadElementData(stack);
        data.addComputedElementWithSpecificSource(name, value, operation, specificSource);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 移除计算数据元素
     */
    public static void removeComputedElement(ItemStack stack, String name, String source) {
        WeaponElementData data = loadElementData(stack);
        data.removeComputedElement(name, source);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 移除指定具体来源标识符的计算数据元素
     */
    public static void removeComputedElementBySpecificSource(ItemStack stack, String name, String specificSource) {
        WeaponElementData data = loadElementData(stack);
        data.removeComputedElementBySpecificSource(name, specificSource);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 添加派系增伤
     */
    public static void addExtraFaction(ItemStack stack, String faction, double value, String operation) {
        WeaponElementData data = loadElementData(stack);
        data.addExtraFaction(faction, value, operation);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 添加带具体来源标识符的派系增伤
     */
    public static void addExtraFactionWithSpecificSource(ItemStack stack, String faction, double value, String operation, String specificSource) {
        WeaponElementData data = loadElementData(stack);
        data.addExtraFactionWithSpecificSource(faction, value, operation, specificSource);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 移除派系增伤
     */
    public static void removeExtraFaction(ItemStack stack, String faction, String source) {
        WeaponElementData data = loadElementData(stack);
        data.removeExtraFaction(faction, source);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 移除指定具体来源标识符的派系增伤
     */
    public static void removeExtraFactionBySpecificSource(ItemStack stack, String faction, String specificSource) {
        WeaponElementData data = loadElementData(stack);
        data.removeExtraFactionBySpecificSource(faction, specificSource);
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 获取最终使用值
     */
    public static double getUsageValue(ItemStack stack, String name) {
        WeaponElementData data = loadElementData(stack, true);
        List<Double> values = data.getUsageValue(name);
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * 获取派系增伤值
     */
    public static double getExtraFactionModifier(ItemStack stack, String faction) {
        WeaponElementData data = loadElementData(stack, true);
        List<ExtraEntry> entries = data.getExtraFaction(faction);
        double total = 0.0;
        for (ExtraEntry entry : entries) {
            if (entry != null) {
                // Extra数据只支持add/sub
                total += "add".equals(entry.getOperation()) ? entry.getValue() : 
                         ("sub".equals(entry.getOperation()) ? -entry.getValue() : 0.0);
            }
        }
        return total;
    }
    
    /**
     * 清除计算数据
     */
    public static void clearComputedData(ItemStack stack) {
        WeaponElementData data = loadElementData(stack);
        data.clearComputed();
        // 重新计算Usage数据以确保一致性
        computeUsageData(stack, data);
        saveElementData(stack, data);
    }
    
    /**
     * 计算Usage数据（核心计算逻辑）
     * 使用专门的modifier处理器进行计算
     */
    public static void computeUsageData(ItemStack stack, WeaponElementData data) {
        // 清空旧的使用数据
        data.clearUsage();
        
        
        // 1. 计算物理元素
        PhysicalElementModifier.computePhysicalElements(data);
        
        // 2. 计算元素复合（基础元素和复合元素）
        ElementCombinationModifier.computeElementCombinations(data);
        
        // 3. 计算暴击率
        CriticalChanceModifier.computeCriticalChance(data);
        
        // 4. 计算暴击伤害
        CriticalDamageModifier.computeCriticalDamage(data);
        
        // 5. 计算触发率
        TriggerChanceModifier.computeTriggerChance(data);
        
        int totalUsageValues = data.getAllUsageValues().size();
    }
    
    
    // NBT读写辅助方法 - Basic层
    private static Map<String, List<BasicEntry>> readBasicEntryMap(CompoundTag tag) {
        Map<String, List<BasicEntry>> map = new HashMap<>();
        if (tag.contains("data", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("data", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag wrapper = (CompoundTag) listTag.get(i);
                ListTag elementArray = wrapper.getList("data", Tag.TAG_STRING);
                String type = elementArray.getString(0);
                double value = Double.parseDouble(elementArray.getString(1));
                String source = elementArray.getString(2);
                
                BasicEntry entry = new BasicEntry();
                entry.setType(type);
                entry.setValue(value);
                entry.setSource(source);
                
                map.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeBasicEntryMap(Map<String, List<BasicEntry>> map) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        
        for (Map.Entry<String, List<BasicEntry>> entry : map.entrySet()) {
            for (BasicEntry basicEntry : entry.getValue()) {
                ListTag elementArray = new ListTag();
                elementArray.add(StringTag.valueOf(basicEntry.getType()));
                elementArray.add(StringTag.valueOf(Double.toString(basicEntry.getValue())));
                elementArray.add(StringTag.valueOf(basicEntry.getSource()));
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("data", elementArray);
                listTag.add(wrapper);
            }
        }
        
        tag.put("data", listTag);
        return tag;
    }
    
    // NBT读写辅助方法 - Computed层
    private static Map<String, List<ComputedEntry>> readComputedEntryMap(CompoundTag tag) {
        Map<String, List<ComputedEntry>> map = new HashMap<>();
        if (tag.contains("data", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("data", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag wrapper = (CompoundTag) listTag.get(i);
                ListTag elementArray = wrapper.getList("data", Tag.TAG_STRING);
                String type = elementArray.getString(0);
                double value = Double.parseDouble(elementArray.getString(1));
                String source = elementArray.getString(2);
                String operation = elementArray.getString(3);
                // 确保specificSource字段正确处理，即使在旧数据中不存在
                String specificSource = "";
                if (elementArray.size() > 4) {
                    specificSource = elementArray.getString(4);
                }
                
                ComputedEntry entry = new ComputedEntry();
                entry.setType(type);
                entry.setValue(value);
                entry.setSource(source);
                entry.setOperation(operation);
                entry.setSpecificSource(specificSource);
                
                map.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeComputedEntryMap(Map<String, List<ComputedEntry>> map) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        
        for (Map.Entry<String, List<ComputedEntry>> entry : map.entrySet()) {
            for (ComputedEntry computedEntry : entry.getValue()) {
                ListTag elementArray = new ListTag();
                elementArray.add(StringTag.valueOf(computedEntry.getType()));
                elementArray.add(StringTag.valueOf(Double.toString(computedEntry.getValue())));
                elementArray.add(StringTag.valueOf(computedEntry.getSource()));
                elementArray.add(StringTag.valueOf(computedEntry.getOperation()));
                // 根据要求，specificSource不能为空，直接保存
                elementArray.add(StringTag.valueOf(computedEntry.getSpecificSource()));
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("data", elementArray);
                listTag.add(wrapper);
            }
        }
        
        tag.put("data", listTag);
        return tag;
    }
    
    // NBT读写辅助方法 - Extra层
    private static Map<String, List<ExtraEntry>> readExtraEntryMap(CompoundTag tag) {
        Map<String, List<ExtraEntry>> map = new HashMap<>();
        if (tag.contains("data", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("data", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag wrapper = (CompoundTag) listTag.get(i);
                ListTag elementArray = wrapper.getList("data", Tag.TAG_STRING);
                String type = elementArray.getString(0);
                double value = Double.parseDouble(elementArray.getString(1));
                String operation = elementArray.getString(2);
                // 确保specificSource字段正确处理，即使在旧数据中不存在
                String specificSource = "";
                if (elementArray.size() > 3) {
                    specificSource = elementArray.getString(3);
                }
                
                ExtraEntry entry = new ExtraEntry();
                entry.setType(type);
                entry.setValue(value);
                entry.setOperation(operation);
                entry.setSpecificSource(specificSource);
                
                map.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeExtraEntryMap(Map<String, List<ExtraEntry>> map) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        
        for (Map.Entry<String, List<ExtraEntry>> entry : map.entrySet()) {
            for (ExtraEntry extraEntry : entry.getValue()) {
                ListTag elementArray = new ListTag();
                elementArray.add(StringTag.valueOf(extraEntry.getType()));
                elementArray.add(StringTag.valueOf(Double.toString(extraEntry.getValue())));
                elementArray.add(StringTag.valueOf(extraEntry.getSource()));
                elementArray.add(StringTag.valueOf(extraEntry.getOperation()));
                // 根据要求，specificSource不能为空，直接保存
                elementArray.add(StringTag.valueOf(extraEntry.getSpecificSource()));
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("data", elementArray);
                listTag.add(wrapper);
            }
        }
        
        tag.put("data", listTag);
        return tag;
    }
    
    private static Map<String, List<Double>> readDoubleMap(CompoundTag tag) {
        Map<String, List<Double>> map = new HashMap<>();
        if (tag.contains("data", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("data", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag wrapper = (CompoundTag) listTag.get(i);
                ListTag elementArray = wrapper.getList("data", Tag.TAG_STRING);
                String type = elementArray.getString(0);
                double value = Double.parseDouble(elementArray.getString(1));
                map.computeIfAbsent(type, k -> new ArrayList<>()).add(value);
            }
        }
        return map;
    }
    
    private static CompoundTag writeDoubleMap(Map<String, List<Double>> map) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        
        for (Map.Entry<String, List<Double>> entry : map.entrySet()) {
            for (Double value : entry.getValue()) {
                ListTag elementArray = new ListTag();
                elementArray.add(StringTag.valueOf(entry.getKey()));
                elementArray.add(StringTag.valueOf(Double.toString(value)));
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("data", elementArray);
                listTag.add(wrapper);
            }
        }
        
        tag.put("data", listTag);
        return tag;
    }
}