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
    
    public static final String ELEMENT_TAG_KEY = "ElementProperties";
    public static final String TRIGGER_CHANCE = "TriggerChance";
    
    /**
     * 从物品堆中读取元素列表
     */
    public static List<ElementInstance> readElementsFromItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return new ArrayList<>();
        }
        
        CompoundTag itemTag = stack.getTag();
        if (!itemTag.contains(ELEMENT_TAG_KEY, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        
        ListTag elementListTag = itemTag.getList(ELEMENT_TAG_KEY, Tag.TAG_COMPOUND);
        List<ElementInstance> elements = new ArrayList<>();
        
        for (int i = 0; i < elementListTag.size(); i++) {
            CompoundTag elementTag = elementListTag.getCompound(i);
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
        
        CompoundTag itemTag = stack.getOrCreateTag();
        ListTag elementListTag = new ListTag();
        
        for (ElementInstance element : elements) {
            elementListTag.add(element.toNBT());
        }
        
        itemTag.put(ELEMENT_TAG_KEY, elementListTag);
    }
    
    /**
     * 检查物品是否有任何元素
     */
    public static boolean hasAnyElements(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        
        CompoundTag itemTag = stack.getTag();
        return itemTag.contains(ELEMENT_TAG_KEY, Tag.TAG_LIST) && 
               !itemTag.getList(ELEMENT_TAG_KEY, Tag.TAG_COMPOUND).isEmpty();
    }
    
    /**
     * 获取暴击率
     */
    public static double getCriticalChance(ItemStack stack) {
        List<ElementInstance> elements = readElementsFromItem(stack);
        for (ElementInstance element : elements) {
            if (element.getType() == ElementType.CRITICAL_CHANCE) {
                return element.getValue();
            }
        }
        return 0.0; // 默认值
    }
    
    /**
     * 获取暴击伤害
     */
    public static double getCriticalDamage(ItemStack stack) {
        List<ElementInstance> elements = readElementsFromItem(stack);
        for (ElementInstance element : elements) {
            if (element.getType() == ElementType.CRITICAL_DAMAGE) {
                return element.getValue();
            }
        }
        return 1.0; // 默认值（无暴击时为1.0倍）
    }
    
    /**
     * 获取触发率
     */
    public static double getTriggerChance(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0.0;
        }
        
        CompoundTag itemTag = stack.getTag();
        if (itemTag.contains(TRIGGER_CHANCE, Tag.TAG_DOUBLE)) {
            return itemTag.getDouble(TRIGGER_CHANCE);
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