package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import com.xlxyvergil.hamstercore.util.ForgeAttributeValueReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器数据类
 * 使用旧版类结构，但保持新的数据结构和使用方式
 */
public class WeaponData {
    // MOD相关信息字段
    public String modid;
    public String itemId;
    public String gunId;
    public String translationKey;
    
    // 元素数据容器
    public WeaponElementData elementData = new WeaponElementData();
    
    /**
     * 添加Basic层元素
     */
    public void addBasicElement(String type, String source, int order) {
        elementData.addBasicElement(type, source, order);
    }
    
    /**
     * 获取按优先级排序的Basic层元素列表
     * 优先级顺序: Def > Config > User
     */
    public List<BasicEntry> getSortedBasicElements() {
        return elementData.getSortedBasicElements();
    }
    
    /**
     * 设置Usage层元素
     */
    public void setUsageElement(String type, double value) {
        elementData.setUsageElement(type, value);
    }
    
    /**
     * 获取Basic层元素
     */
    public Map<String, List<BasicEntry>> getBasicElements() {
        return elementData.getBasicElements();
    }
    
    /**
     * 获取Usage层元素
     */
    public Map<String, Double> getUsageElements() {
        return elementData.getUsageElements();
    }
    
    /**
     * 获取Usage层指定类型的元素值
     */
    public Double getUsageValue(String type) {
        return elementData.getUsageValue(type);
    }
    
    /**
     * 清空Usage层元素
     */
    public void clearUsageElements() {
        elementData.clearUsageElements();
    }
    
    /**
     * 添加初始修饰符
     */
    public void addInitialModifier(InitialModifierEntry entry) {
        elementData.addInitialModifier(entry);
    }
    
    /**
     * 获取初始修饰符列表
     */
    public List<InitialModifierEntry> getInitialModifiers() {
        return elementData.getInitialModifiers();
    }
    
    /**
     * 计算Usage层数据（基于initialModifiers层和Basic层数据）
     * 这个方法在物品属性被查询时调用，用于动态计算元素值
     */
    public void computeUsageData(ItemStack stack) {
        // 使用ForgeAttributeValueReader获取基础元素和复合元素的预分类计算值
        ForgeAttributeValueReader.ElementCategoryData categoryData = 
            ForgeAttributeValueReader.getAllElementValuesByCategory(stack);
        Map<String, Double> basicAndComplexValues = categoryData.getBasicAndComplexValues();
        
        // 使用ElementCombinationModifier来计算复合元素
        ElementCombinationModifier.computeElementCombinationsWithValues(this, basicAndComplexValues);
    }
    
    /**
     * 将WeaponData转换为NBT标签
     * 直接返回元素数据，不包含MOD相关信息
     */
    public CompoundTag toNBT() {
        // 直接返回元素数据，不嵌套在elementData字段中
        return elementData.toNBT();
    }
    
    /**
     * 从NBT标签创建WeaponData
     * 直接读取元素数据结构
     */
    public static WeaponData fromNBT(CompoundTag tag) {
        WeaponData data = new WeaponData();
        
        // 直接从NBT标签中读取元素数据，不经过嵌套的elementData字段
        data.elementData = WeaponElementData.fromNBT(tag);
        
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
               Objects.equals(elementData, that.elementData);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(modid, itemId, gunId, translationKey, elementData);
    }
}