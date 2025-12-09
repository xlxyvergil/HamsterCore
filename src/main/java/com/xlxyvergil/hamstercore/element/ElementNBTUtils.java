package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 元素NBT工具类，提供元素数据与NBT标签之间的转换功能
 */
public class ElementNBTUtils {
    
    public static final String ELEMENT_DATA = "ElementData";
    public static final String ELEMENTS = "Elements";
    public static final String TRIGGER_CHANCE = "TriggerChance";
    
    /**
     * 从物品堆中读取元素列表
     */
    public static List<ElementInstance> readElementsFromItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return new ArrayList<>();
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData == null || !elementData.contains(ELEMENTS, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        
        ListTag elementsList = elementData.getList(ELEMENTS, Tag.TAG_COMPOUND);
        List<ElementInstance> elements = new ArrayList<>();
        
        for (int i = 0; i < elementsList.size(); i++) {
            CompoundTag elementTag = elementsList.getCompound(i);
            ElementInstance element = ElementInstance.fromNBT(elementTag);
            if (element != null) {
                elements.add(element);
            }
        }
        
        return elements;
    }
    
    /**
     * 将元素列表写入物品堆
     */
    public static void writeElementsToItem(ItemStack stack, List<ElementInstance> elements) {
        if (stack.isEmpty()) {
            return;
        }
        
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        ListTag elementsList = new ListTag();
        
        for (ElementInstance element : elements) {
            elementsList.add(element.toNBT());
        }
        
        elementData.put(ELEMENTS, elementsList);
    }
    
    /**
     * 检查物品是否有任何元素
     */
    public static boolean hasAnyElements(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        return elementData != null && elementData.contains(ELEMENTS, Tag.TAG_LIST) && 
               !elementData.getList(ELEMENTS, Tag.TAG_COMPOUND).isEmpty();
    }
    
    /**
     * 获取暴击率
     */
    public static double getCriticalChance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData != null && elementData.contains("CriticalChance", Tag.TAG_DOUBLE)) {
            return elementData.getDouble("CriticalChance");
        }
        
        return 0.0; // 默认值
    }
    
    /**
     * 获取暴击伤害
     */
    public static double getCriticalDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData != null && elementData.contains("CriticalDamage", Tag.TAG_DOUBLE)) {
            return elementData.getDouble("CriticalDamage");
        }
        
        return 0.0; // 默认值
    }
    
    /**
     * 获取触发率
     */
    public static double getTriggerChance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData != null && elementData.contains(TRIGGER_CHANCE, Tag.TAG_DOUBLE)) {
            return elementData.getDouble(TRIGGER_CHANCE);
        }
        
        return 0.0; // 默认值
    }
    
    /**
     * 获取所有元素类型
     */
    public static Set<ElementType> getAllElementTypes(ItemStack stack) {
        List<ElementInstance> elements = readElementsFromItem(stack);
        Set<ElementType> types = new HashSet<>();
        for (ElementInstance element : elements) {
            types.add(element.getType());
        }
        return types;
    }
    
    /**
     * 获取指定元素类型的值
     */
    public static double getElementValue(ItemStack stack, ElementType type) {
        List<ElementInstance> elements = readElementsFromItem(stack);
        for (ElementInstance element : elements) {
            if (element.getType() == type) {
                return element.getValue();
            }
        }
        return 0.0; // 默认值
    }
}