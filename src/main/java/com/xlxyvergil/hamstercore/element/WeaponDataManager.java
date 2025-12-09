package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.modifier.*;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.nbt.CompoundTag;
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
        // 获取物品的ResourceLocation
        ResourceLocation itemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null) {
            return new WeaponElementData();
        }
        
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
        }
        
        // 写入Computed数据
        if (!data.getAllComputedElements().isEmpty()) {
            CompoundTag computedTag = writeComputedEntryMap(data.getAllComputedElements());
            elementDataTag.put(COMPUTED_DATA, (Tag)computedTag);
        }
        
        // 写入Usage数据
        if (!data.getAllUsageValues().isEmpty()) {
            CompoundTag usageTag = writeDoubleMap(data.getAllUsageValues());
            elementDataTag.put(USAGE_DATA, (Tag)usageTag);
        }
        
        // 写入Extra数据
        if (!data.getAllExtraFactions().isEmpty()) {
            CompoundTag extraTag = writeExtraEntryMap(data.getAllExtraFactions());
            elementDataTag.put(EXTRA_DATA, (Tag)extraTag);
        }
    }
    
    /**
     * 添加计算数据元素
     */
    public static void addComputedElement(ItemStack stack, String name, double value, String operation) {
        WeaponElementData data = loadElementData(stack);
        data.addComputedElement(name, value, operation);
        saveElementData(stack, data);
    }
    
    /**
     * 添加派系增伤
     */
    public static void addExtraFaction(ItemStack stack, String faction, double value, String operation) {
        WeaponElementData data = loadElementData(stack);
        data.addExtraFaction(faction, value, operation);
        saveElementData(stack, data);
    }
    
    /**
     * 获取最终使用值
     */
    public static double getUsageValue(ItemStack stack, String name) {
        WeaponElementData data = loadElementData(stack);
        Double value = data.getUsageValue(name);
        return value != null ? value : 0.0;
    }
    
    /**
     * 获取派系增伤值
     */
    public static double getExtraFactionModifier(ItemStack stack, String faction) {
        WeaponElementData data = loadElementData(stack);
        ExtraEntry entry = data.getExtraFaction(faction);
        if (entry == null) return 0.0;
        
        // Extra数据只支持add/sub
        return "add".equals(entry.getOperation()) ? entry.getValue() : 
               ("sub".equals(entry.getOperation()) ? -entry.getValue() : 0.0);
    }
    
    /**
     * 清除计算数据
     */
    public static void clearComputedData(ItemStack stack) {
        WeaponElementData data = loadElementData(stack);
        data.clearComputed();
        saveElementData(stack, data);
    }
    
    /**
     * 计算Usage数据（核心计算逻辑）
     * 使用专门的modifier处理器进行计算
     */
    public static void computeUsageData(ItemStack stack, WeaponElementData data) {
        // 清空旧的使用数据
        data.clearUsage();
        
        DebugLogger.log("开始计算 %s 的Usage数据...", stack.getItem().toString());
        
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
        DebugLogger.log("Usage数据计算完成，共 %d 个属性", totalUsageValues);
    }
    

    
    // NBT读写辅助方法 - Basic层
    private static Map<String, BasicEntry> readBasicEntryMap(CompoundTag tag) {
        Map<String, BasicEntry> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                CompoundTag entryTag = tag.getCompound(key);
                BasicEntry entry = new BasicEntry();
                entry.setType(entryTag.getString("type"));
                entry.setValue(entryTag.getDouble("value"));
                entry.setSource(entryTag.getString("source"));
                map.put(key, entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeBasicEntryMap(Map<String, BasicEntry> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, BasicEntry> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("type", entry.getValue().getType());
            entryTag.putDouble("value", entry.getValue().getValue());
            entryTag.putString("source", entry.getValue().getSource());
            tag.put(entry.getKey(), entryTag);
        }
        return tag;
    }
    
    // NBT读写辅助方法 - Computed层
    private static Map<String, ComputedEntry> readComputedEntryMap(CompoundTag tag) {
        Map<String, ComputedEntry> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                CompoundTag entryTag = tag.getCompound(key);
                ComputedEntry entry = new ComputedEntry();
                entry.setType(entryTag.getString("type"));
                entry.setValue(entryTag.getDouble("value"));
                entry.setSource(entryTag.getString("source"));
                entry.setOperation(entryTag.getString("operation"));
                map.put(key, entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeComputedEntryMap(Map<String, ComputedEntry> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, ComputedEntry> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("type", entry.getValue().getType());
            entryTag.putDouble("value", entry.getValue().getValue());
            entryTag.putString("source", entry.getValue().getSource());
            entryTag.putString("operation", entry.getValue().getOperation());
            tag.put(entry.getKey(), entryTag);
        }
        return tag;
    }
    
    // NBT读写辅助方法 - Extra层
    private static Map<String, ExtraEntry> readExtraEntryMap(CompoundTag tag) {
        Map<String, ExtraEntry> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                CompoundTag entryTag = tag.getCompound(key);
                ExtraEntry entry = new ExtraEntry();
                entry.setType(entryTag.getString("type"));
                entry.setValue(entryTag.getDouble("value"));
                entry.setOperation(entryTag.getString("operation"));
                map.put(key, entry);
            }
        }
        return map;
    }
    
    private static CompoundTag writeExtraEntryMap(Map<String, ExtraEntry> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, ExtraEntry> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("type", entry.getValue().getType());
            entryTag.putDouble("value", entry.getValue().getValue());
            entryTag.putString("operation", entry.getValue().getOperation());
            tag.put(entry.getKey(), entryTag);
        }
        return tag;
    }
    
    private static Map<String, Double> readDoubleMap(CompoundTag tag) {
        Map<String, Double> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_DOUBLE)) {
                map.put(key, tag.getDouble(key));
            }
        }
        return map;
    }
    
    private static CompoundTag writeDoubleMap(Map<String, Double> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            tag.putDouble(entry.getKey(), entry.getValue());
        }
        return tag;
    }
}