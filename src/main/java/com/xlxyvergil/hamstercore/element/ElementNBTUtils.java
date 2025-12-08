package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 元素NBT数据工具类
 * 专门用于从物品的NBT中获取元素数据
 * 提供简化的API来访问元素相关NBT数据
 */
public class ElementNBTUtils {
    
    public static final String ELEMENT_DATA = "element_data";
    public static final String ELEMENTS = "elements";
    public static final String TRIGGER_CHANCE = "trigger_chance";
    public static final String LAST_POSITION = "last_position";
    public static final String ELEMENT_TRIGGER_PROBABILITIES = "element_trigger_probabilities";
    
    /**
     * 获取物品的元素数据NBT标签
     * @param stack 物品堆栈
     * @return 元素数据标签，如果没有则返回null
     */
    public static CompoundTag getElementDataTag(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getTagElement(ELEMENT_DATA);
    }
    
    /**
     * 获取物品的元素数据NBT标签，如果不存在则创建
     * @param stack 物品堆栈
     * @return 元素数据标签
     */
    public static CompoundTag getOrCreateElementDataTag(ItemStack stack) {
        return stack.getOrCreateTagElement(ELEMENT_DATA);
    }
    
    /**
     * 获取指定元素的数值
     * @param stack 物品堆栈
     * @param elementType 元素类型
     * @return 元素数值，如果不存在则返回0.0
     */
    public static double getElementValue(ItemStack stack, ElementType elementType) {
        // 使用ElementHelper的方法，确保一致性
        List<ElementInstance> elements = ElementHelper.getElements(stack);
        for (ElementInstance element : elements) {
            if (element.getType() == elementType) {
                return element.getValue();
            }
        }
        return 0.0;
    }
    
    /**
     * 获取武器的暴击率
     * @param stack 物品堆栈
     * @return 暴击率，如果没有设置则返回0.0
     */
    public static double getCriticalChance(ItemStack stack) {
        // 直接使用ElementHelper的方法，确保一致性
        return ElementHelper.getCriticalChance(stack);
    }
    
    /**
     * 获取武器的暴击伤害
     * 以FactionDamageHandler中的公式为准，暴击伤害默认为0，表示无额外加成
     * @param stack 物品堆栈
     * @return 暴击伤害，如果没有设置则返回0.0
     */
    public static double getCriticalDamage(ItemStack stack) {
        // 直接使用ElementHelper的方法，确保一致性
        return ElementHelper.getCriticalDamage(stack);
    }
    
    /**
     * 检查物品是否有暴击率属性
     * @param stack 物品堆栈
     * @return 如果有暴击率属性则返回true
     */
    public static boolean hasCriticalChance(ItemStack stack) {
        return hasElement(stack, ElementType.CRITICAL_CHANCE);
    }
    
    /**
     * 检查物品是否有暴击伤害属性
     * @param stack 物品堆栈
     * @return 如果有暴击伤害属性则返回true
     */
    public static boolean hasCriticalDamage(ItemStack stack) {
        return hasElement(stack, ElementType.CRITICAL_DAMAGE);
    }
    
    /**
     * 获取指定元素的类型
     * @param stack 物品堆栈
     * @param elementName 元素名称
     * @return 元素类型，如果不存在则返回null
     */
    public static ElementType getElementType(ItemStack stack, String elementName) {
        List<ElementInstance> elements = ElementHelper.getElements(stack);
        for (ElementInstance element : elements) {
            if (element.getType().getName().equals(elementName)) {
                return element.getType();
            }
        }
        return null;
    }
    
    /**
     * 检查物品是否有任何元素数据
     * @param stack 物品堆栈
     * @return 如果有元素数据则返回true
     */
    public static boolean hasAnyElements(ItemStack stack) {
        return ElementHelper.hasElementAttributes(stack);
    }
    
    /**
     * 检查物品是否有指定类型的元素
     * @param stack 物品堆栈
     * @param elementType 元素类型
     * @return 如果有指定元素则返回true
     */
    public static boolean hasElement(ItemStack stack, ElementType elementType) {
        List<ElementInstance> elements = ElementHelper.getElements(stack);
        for (ElementInstance element : elements) {
            if (element.getType() == elementType) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取物品的所有元素类型
     * @param stack 物品堆栈
     * @return 元素类型集合，如果没有元素则返回空集合
     */
    public static Set<ElementType> getAllElementTypes(ItemStack stack) {
        Set<ElementType> types = new HashSet<>();
        List<ElementInstance> elements = ElementHelper.getElements(stack);
        
        for (ElementInstance element : elements) {
            types.add(element.getType());
        }
        
        return types;
    }
    
    /**
     * 获取物品的触发率
     * @param stack 物品堆栈
     * @return 触发率，如果没有设置则返回默认值0.1
     */
    public static double getTriggerChance(ItemStack stack) {
        CompoundTag elementData = getElementDataTag(stack);
        if (elementData != null && elementData.contains(TRIGGER_CHANCE, Tag.TAG_ANY_NUMERIC)) {
            return elementData.getDouble(TRIGGER_CHANCE);
        }
        return 0.1; // 默认触发率 10%
    }
    
    /**
     * 获取元素触发概率数据
     * @param stack 物品堆栈
     * @return 元素触发概率映射，如果没有设置则返回空映射
     */
    public static Map<String, Double> getTriggerProbabilitiesNBT(ItemStack stack) {
        Map<String, Double> probabilities = new HashMap<>();
        CompoundTag elementData = getElementDataTag(stack);
        
        if (elementData != null && elementData.contains(ELEMENT_TRIGGER_PROBABILITIES, Tag.TAG_COMPOUND)) {
            CompoundTag probabilitiesTag = elementData.getCompound(ELEMENT_TRIGGER_PROBABILITIES);
            
            for (String key : probabilitiesTag.getAllKeys()) {
                if (probabilitiesTag.contains(key, Tag.TAG_ANY_NUMERIC)) {
                    probabilities.put(key, probabilitiesTag.getDouble(key));
                }
            }
        }
        
        return probabilities;
    }
    
    /**
     * 获取指定元素类型的触发概率
     * @param stack 物品堆栈
     * @param elementType 元素类型
     * @return 触发概率，如果没有设置则返回0.0
     */
    public static double getElementTriggerProbability(ItemStack stack, ElementType elementType) {
        Map<String, Double> probabilities = getTriggerProbabilitiesNBT(stack);
        return probabilities.getOrDefault(elementType.getName(), 0.0);
    }
    
    /**
     * 检查物品是否有触发概率数据
     * @param stack 物品堆栈
     * @return 如果有触发概率数据则返回true
     */
    public static boolean hasTriggerProbabilities(ItemStack stack) {
        CompoundTag elementData = getElementDataTag(stack);
        return elementData != null && elementData.contains(ELEMENT_TRIGGER_PROBABILITIES, Tag.TAG_COMPOUND);
    }
    
    /**
     * 获取物品元素数据的完整NBT快照
     * @param stack 物品堆栈
     * @return 包含所有元素数据的NBT标签副本，如果没有元素数据则返回空标签
     */
    public static CompoundTag getCompleteElementSnapshot(ItemStack stack) {
        CompoundTag snapshot = new CompoundTag();
        CompoundTag elementData = getElementDataTag(stack);
        
        if (elementData != null) {
            snapshot = elementData.copy();
        }
        
        return snapshot;
    }
    
    /**
     * 验证元素NBT数据的完整性
     * @param stack 物品堆栈
     * @return 验证结果，包含错误信息（如果有）
     */
    public static ValidationResult validateElementData(ItemStack stack) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        CompoundTag elementData = getElementDataTag(stack);
        if (elementData == null) {
            return new ValidationResult(true, errors, warnings);
        }
        
        // 检查元素列表
        if (elementData.contains(ELEMENTS, Tag.TAG_LIST)) {
            ListTag elementsList = elementData.getList(ELEMENTS, Tag.TAG_COMPOUND);
            
            for (int i = 0; i < elementsList.size(); i++) {
                Tag elementTag = elementsList.get(i);
                if (elementTag.getId() != Tag.TAG_COMPOUND) {
                    errors.add("Element at index " + i + " is not a compound tag");
                    continue;
                }
                
                CompoundTag elementCompound = (CompoundTag) elementTag;
                
                // 检查必需的键
                if (!elementCompound.contains("type", Tag.TAG_STRING)) {
                    errors.add("Element at index " + i + " is missing 'type' field");
                }
                
                if (!elementCompound.contains("value", Tag.TAG_ANY_NUMERIC)) {
                    errors.add("Element at index " + i + " is missing 'value' field");
                }
                
                if (!elementCompound.contains("position", Tag.TAG_ANY_NUMERIC)) {
                    errors.add("Element at index " + i + " is missing 'position' field");
                }
                
                if (!elementCompound.contains("is_active", Tag.TAG_BYTE)) {
                    errors.add("Element at index " + i + " is missing 'is_active' field");
                }
                
                if (!elementCompound.contains("source", Tag.TAG_STRING)) {
                    errors.add("Element at index " + i + " is missing 'source' field");
                } else {
                    String sourceStr = elementCompound.getString("source");
                    try {
                        ElementSource.valueOf(sourceStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Element at index " + i + " has invalid source value: " + sourceStr);
                    }
                }
                
                // 检查元素类型是否有效
                if (elementCompound.contains("type", Tag.TAG_STRING)) {
                    String typeName = elementCompound.getString("type");
                    ElementType type = ElementType.byName(typeName);
                    if (type == null) {
                        errors.add("Element at index " + i + " has invalid type: " + typeName);
                    }
                }
            }
        }
        
        // 检查位置连续性（警告）
        if (elementData.contains(ELEMENTS, Tag.TAG_LIST) && elementData.contains(LAST_POSITION, Tag.TAG_ANY_NUMERIC)) {
            ListTag elementsList = elementData.getList(ELEMENTS, Tag.TAG_COMPOUND);
            int lastPosition = elementData.getInt(LAST_POSITION);
            
            Set<Integer> positions = new HashSet<>();
            for (Tag elementTag : elementsList) {
                CompoundTag elementCompound = (CompoundTag) elementTag;
                if (elementCompound.contains("position", Tag.TAG_ANY_NUMERIC)) {
                    positions.add(elementCompound.getInt("position"));
                }
            }
            
            // 检查是否有重复位置
            if (positions.size() < elementsList.size()) {
                warnings.add("Duplicate element positions detected");
            }
            
            // 检查是否有位置超出范围
            for (int pos : positions) {
                if (pos < 0 || pos > lastPosition) {
                    warnings.add("Element position " + pos + " is out of expected range [0, " + lastPosition + "]");
                }
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean isValid, List<String> errors, List<String> warnings) {
            this.isValid = isValid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
    }
}