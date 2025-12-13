package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器数据管理器
 * 负责处理武器元素数据的NBT读写操作
 * 适配新的两层NBT数据结构
 */
public class WeaponDataManager {
    
    // NBT标签键名
    private static final String ELEMENT_DATA_TAG = "ElementData";
    private static final String BASIC_TAG = "Basic";
    private static final String USAGE_TAG = "Usage";
    
    /**
     * 从物品栈中读取元素数据
     *
     * @param stack 物品栈
     * @return 武器数据对象，如果不存在则返回null
     */
    public static WeaponData loadElementData(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        
        CompoundTag itemTag = stack.getTag();
        if (!itemTag.contains(ELEMENT_DATA_TAG)) {
            return null;
        }
        
        CompoundTag elementDataTag = itemTag.getCompound(ELEMENT_DATA_TAG);
        WeaponData weaponData = new WeaponData();
        
        // 读取Basic层数据
        if (elementDataTag.contains(BASIC_TAG)) {
            ListTag basicList = elementDataTag.getList(BASIC_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < basicList.size(); i++) {
                CompoundTag entryTag = basicList.getCompound(i);
                String type = entryTag.getString("type");
                String source = entryTag.getString("source");
                int order = entryTag.getInt("order");
                
                // 直接添加，保持原有顺序
                weaponData.getBasicElements().computeIfAbsent(type, k -> new ArrayList<>())
                    .add(new WeaponData.BasicEntry(type, source, order));
            }
        }
        
        // 读取Usage层数据
        if (elementDataTag.contains(USAGE_TAG)) {
            CompoundTag usageTag = elementDataTag.getCompound(USAGE_TAG);
            for (String key : usageTag.getAllKeys()) {
                double value = usageTag.getDouble(key);
                weaponData.setUsageElement(key, value);
            }
        }
        
        return weaponData;
    }
    
    /**
     * 将元素数据保存到物品栈中
     *
     * @param stack 物品栈
     * @param weaponData 武器数据对象
     */
    public static void saveElementData(ItemStack stack, WeaponData weaponData) {
        if (stack.isEmpty() || weaponData == null) {
            return;
        }
        
        // 获取或创建物品NBT标签
        CompoundTag itemTag = stack.getOrCreateTag();
        
        // 创建元素数据标签
        CompoundTag elementDataTag = new CompoundTag();
        
        // 保存Basic层数据
        ListTag basicList = new ListTag();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : weaponData.getBasicElements().entrySet()) {
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("type", basicEntry.getType());
                entryTag.putString("source", basicEntry.getSource());
                entryTag.putInt("order", basicEntry.getOrder());
                basicList.add(entryTag);
            }
        }
        elementDataTag.put(BASIC_TAG, basicList);
        
        // 保存Usage层数据
        CompoundTag usageTag = new CompoundTag();
        for (Map.Entry<String, Double> entry : weaponData.getUsageElements().entrySet()) {
            usageTag.putDouble(entry.getKey(), entry.getValue());
        }
        elementDataTag.put(USAGE_TAG, usageTag);
        
        // 将元素数据标签保存到物品标签中
        itemTag.put(ELEMENT_DATA_TAG, elementDataTag);
    }
    
    /**
     * 计算Usage数据
     * 根据Basic层数据计算复合元素的最终数值
     *
     * @param stack 物品栈
     * @param weaponData 武器数据对象
     */
    public static void computeUsageData(ItemStack stack, WeaponData weaponData) {
        // TODO: 实现元素复合计算逻辑
        // 这里应该根据Basic层的元素数据计算Usage层的数据
        // 但现在我们直接使用从配置文件中加载的Usage数据
    }
    
    /**
     * 添加基础元素到物品栈中
     *
     * @param stack 物品栈
     * @param type 元素类型
     * @param source 来源(def或user)
     */
    public static void addBasicElement(ItemStack stack, String type, String source) {
        WeaponData weaponData = loadElementData(stack);
        if (weaponData == null) {
            weaponData = new WeaponData();
        }
        
        weaponData.addBasicElement(type, source);
        saveElementData(stack, weaponData);
    }
    
    /**
     * 从物品栈中移除指定类型的基础元素
     *
     * @param stack 物品栈
     * @param type 元素类型
     */
    public static void removeBasicElement(ItemStack stack, String type) {
        WeaponData weaponData = loadElementData(stack);
        if (weaponData == null) {
            return;
        }
        
        weaponData.removeBasicElement(type);
        saveElementData(stack, weaponData);
    }
}