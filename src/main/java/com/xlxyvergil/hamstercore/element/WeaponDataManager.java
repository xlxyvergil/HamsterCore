package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器数据管理器
 * 负责处理武器元素数据的NBT读写操作
 * 适配新的两层NBT数据结构
 * Basic层：存储修饰符的元素类型、排序和是否是CONFIG的信息
 * Usage层：存储复合后的元素以及数值
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
     * 为物品应用配置数据（如果尚未应用）
     * 
     * @param stack 物品栈
     * @return 是否成功应用了配置数据
     */
    public static boolean applyConfigDataIfNeeded(ItemStack stack) {
        if (stack.isEmpty() || stack.hasTag() && stack.getTag().contains(ELEMENT_DATA_TAG)) {
            // 物品为空或已经包含元素数据，无需再次应用
            return false;
        }
        
        // 从配置中获取武器数据
        WeaponData weaponData = com.xlxyvergil.hamstercore.config.WeaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            // 没有找到对应的配置数据
            return false;
        }
        
        // 应用元素修饰符到物品
        com.xlxyvergil.hamstercore.element.ElementApplier.applyElementModifiers(stack, weaponData.getBasicElements());
        
        // 保存元素数据到NBT（只保存Basic层，不保存Usage层）
        saveElementDataWithoutUsage(stack, weaponData);
        
        return true;
    }

    /**
     * 保存元素数据到物品栈中（只保存Basic层）
     *
     * @param stack 物品栈
     * @param weaponData 武器数据对象
     */
    public static void saveElementDataWithoutUsage(ItemStack stack, WeaponData weaponData) {
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
        
        // 不保存Usage层数据
        // Usage层数据会在需要时根据Basic层数据计算得出
        // 配置文件不会生成Usage层的数据
        
        // 将元素数据标签保存到物品标签中
        itemTag.put(ELEMENT_DATA_TAG, elementDataTag);
    }
    
    /**
     * 保存元素数据到物品栈中
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
        // 使用ElementCombinationModifier计算Usage层数据
        com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier.apply(weaponData, stack);
    }
    
    /**
     * 添加基础元素到物品栈中
     *
     * @param stack 物品栈
     * @param type 元素类型
     * @param source 来源(CONFIG或USER)
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