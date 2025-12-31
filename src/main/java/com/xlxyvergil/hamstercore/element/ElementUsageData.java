package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 元素使用数据类
 * 用于存储元素属性的NBT数据，类似Apotheosis的词缀系统
 * 将元素计算结果直接存储到物品的NBT中
 */
public class ElementUsageData {
    
    // NBT标签键名
    private static final String ELEMENT_DATA_KEY = "ElementUsageData";
    private static final String ELEMENT_MODIFIERS_KEY = "ElementModifiers";
    private static final String MODIFIER_NAME_KEY = "name";
    private static final String MODIFIER_AMOUNT_KEY = "amount";
    private static final String MODIFIER_OPERATION_KEY = "operation";
    private static final String MODIFIER_UUID_KEY = "uuid";
    private static final String MODIFIER_SOURCE_KEY = "source";
    private static final String MODIFIER_ELEMENT_TYPE_KEY = "elementType";
    
    /**
     * 将元素数据写入物品的NBT标签中
     * @param stack 物品栈
     * @param modifiers 元素属性修饰符列表
     */
    public static void writeElementDataToItem(ItemStack stack, List<AttributeModifierEntry> modifiers) {
        if (stack.isEmpty()) {
            return;
        }
        
        CompoundTag tag = stack.getOrCreateTag();
        ListTag modifiersList = new ListTag();
        
        // 写入所有属性修饰符
        for (AttributeModifierEntry entry : modifiers) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(MODIFIER_NAME_KEY, entry.getName());
            entryTag.putDouble(MODIFIER_AMOUNT_KEY, entry.getAmount());
            entryTag.putString(MODIFIER_OPERATION_KEY, entry.getOperation());
            entryTag.putUUID(MODIFIER_UUID_KEY, entry.getUuid());
            entryTag.putString(MODIFIER_SOURCE_KEY, entry.getSource());
            entryTag.putString(MODIFIER_ELEMENT_TYPE_KEY, entry.getElementType());
            modifiersList.add(entryTag);
        }
        
        CompoundTag elementTag = new CompoundTag();
        elementTag.put(ELEMENT_MODIFIERS_KEY, modifiersList);
        tag.put(ELEMENT_DATA_KEY, elementTag);
        stack.setTag(tag);
    }
    
    /**
     * 从物品的NBT标签中读取元素数据
     * @param stack 物品栈
     * @return 元素属性修饰符列表
     */
    public static List<AttributeModifierEntry> readElementDataFromItem(ItemStack stack) {
        List<AttributeModifierEntry> modifiers = new ArrayList<>();
        
        if (stack.isEmpty() || !stack.hasTag()) {
            return modifiers;
        }
        
        CompoundTag tag = stack.getTag();
        if (!tag.contains(ELEMENT_DATA_KEY)) {
            return modifiers;
        }
        
        CompoundTag elementTag = tag.getCompound(ELEMENT_DATA_KEY);
        if (!elementTag.contains(ELEMENT_MODIFIERS_KEY)) {
            return modifiers;
        }
        
        ListTag modifiersList = elementTag.getList(ELEMENT_MODIFIERS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag entryTag = modifiersList.getCompound(i);
            AttributeModifierEntry entry = new AttributeModifierEntry(
                entryTag.getString(MODIFIER_NAME_KEY),
                entryTag.getString(MODIFIER_ELEMENT_TYPE_KEY),
                entryTag.getDouble(MODIFIER_AMOUNT_KEY),
                entryTag.getString(MODIFIER_OPERATION_KEY),
                entryTag.getUUID(MODIFIER_UUID_KEY),
                entryTag.getString(MODIFIER_SOURCE_KEY)
            );
            modifiers.add(entry);
        }
        
        return modifiers;
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
        if (!tag.contains(ELEMENT_DATA_KEY)) {
            return false;
        }
        
        CompoundTag elementTag = tag.getCompound(ELEMENT_DATA_KEY);
        return elementTag.contains(ELEMENT_MODIFIERS_KEY);
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
     * 属性修饰符条目类
     * 存储完整的属性修饰符数据
     */
    public static class AttributeModifierEntry {
        private final String name;          // 属性名称
        private final String elementType;   // 元素类型
        private final double amount;        // 数值
        private final String operation;     // 操作类型
        private final UUID uuid;           // UUID
        private final String source;        // 来源
        
        public AttributeModifierEntry(String name, String elementType, double amount, String operation, UUID uuid, String source) {
            this.name = name;
            this.elementType = elementType;
            this.amount = amount;
            this.operation = operation;
            this.uuid = uuid;
            this.source = source;
        }
        
        public String getName() { return name; }
        public String getElementType() { return elementType; }
        public double getAmount() { return amount; }
        public String getOperation() { return operation; }
        public UUID getUuid() { return uuid; }
        public String getSource() { return source; }
    }
}