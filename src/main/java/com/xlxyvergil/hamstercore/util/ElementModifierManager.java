package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 元素修饰符管理器
 * 参考GunsmithLib实现，使用标准化的数据结构
 */
public class ElementModifierManager {
    
    /**
     * 标准化的元素修饰符数据结构
     */
    public static class ElementModifierData {
        /**
         * 属性标识符（必填）
         * 格式：hamstercore:{元素名}
         */
        private ResourceLocation attribute;
        
        /**
         * 修饰符ID（必填）
         * 使用ElementUUIDManager管理
         */
        private UUID id;
        
        /**
         * 修饰符数值（必填）
         */
        private double amount;
        
        /**
         * 修饰符名称（可选，默认为"Hamster Core Element"）
         */
        private String name = "Hamster Core Element";
        
        /**
         * 运算模式（可选，默认为ADDITION）
         */
        private AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
        
        public ElementModifierData() {}
        
        public ElementModifierData(ResourceLocation attribute, UUID id, double amount) {
            this.attribute = attribute;
            this.id = id;
            this.amount = amount;
        }
        
        public ElementModifierData(ResourceLocation attribute, UUID id, double amount, 
                                   String name, AttributeModifier.Operation operation) {
            this.attribute = attribute;
            this.id = id;
            this.amount = amount;
            this.name = name;
            this.operation = operation;
        }
        
        // Getters and Setters
        public ResourceLocation getAttribute() { return attribute; }
        public void setAttribute(ResourceLocation attribute) { this.attribute = attribute; }
        
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public AttributeModifier.Operation getOperation() { return operation; }
        public void setOperation(AttributeModifier.Operation operation) { this.operation = operation; }
    }
    
    /**
     * 应用元素修饰符到事件处理器
     * @param stack 目标物品堆
     * @param modifiers 标准化的修饰符数据列表
     * @param slot 装备槽位
     * @param consumer 修饰符添加回调
     */
    public static void applyElementModifiers(ItemStack stack, 
                                           List<ElementModifierData> modifiers, 
                                           EquipmentSlot slot,
                                           BiConsumer<Attribute, AttributeModifier> consumer) {
        if (modifiers == null || stack == null || consumer == null) {
            return;
        }
        
        // 只在主手槽位时应用修饰符，避免工具提示显示其他槽位信息
        if (slot != EquipmentSlot.MAINHAND) {
            return;
        }
        
        for (ElementModifierData modifierData : modifiers) {
            try {
                // 从属性标识符获取元素类型
                String elementName = modifierData.getAttribute().getPath();
                ElementType elementType = ElementType.byName(elementName);
                if (elementType == null) {
                    System.err.println("Unknown element type: " + elementName);
                    continue;
                }
                
                // 获取对应的元素属性
                ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
                if (elementAttribute == null) {
                    System.err.println("Unregistered element attribute: " + elementName);
                    continue;
                }
                
                // 创建AttributeModifier
                AttributeModifier modifier = new AttributeModifier(
                    modifierData.getId(),
                    modifierData.getName(),
                    modifierData.getAmount(),
                    modifierData.getOperation()
                );
                
                // 通过回调添加修饰符到事件中
                consumer.accept(elementAttribute, modifier);
                
            } catch (Exception e) {
                System.err.println("Error applying element modifier for " + modifierData.getAttribute() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 应用元素修饰符到物品（保留旧方法以兼容现有代码）
     * @param stack 目标物品堆
     * @param modifiers 标准化的修饰符数据列表
     * @param slot 装备槽位
     */
    public static void applyElementModifiers(ItemStack stack, 
                                           List<ElementModifierData> modifiers, 
                                           EquipmentSlot slot) {
        applyElementModifiers(stack, modifiers, slot, (attr, mod) -> stack.addAttributeModifier(attr, mod, slot));
    }
    
    /**
     * 从ElementType创建标准化修饰符数据
     * @param elementType 元素类型
     * @param amount 修饰符数值
     * @param operation 运算模式
     * @param stack 物品堆（用于UUID管理）
     * @param index 索引
     * @return 标准化的修饰符数据
     */
    public static ElementModifierData createModifierData(
        ElementType elementType,
        double amount,
        AttributeModifier.Operation operation,
        ItemStack stack,
        int index
    ) {
        // 属性标识符
        ResourceLocation attribute = new ResourceLocation("hamstercore", elementType.getName());
        
        // UUID管理
        UUID uuid = ElementUUIDManager.getOrCreateUUID(stack, elementType, index);
        
        // 修饰符名称
        String name = elementType.getDisplayName();
        
        return new ElementModifierData(attribute, uuid, amount, name, operation);
    }
    

    
    /**
     * 移除物品上指定元素类型的所有修饰符
     * @param stack 目标物品堆
     * @param elementType 要移除的元素类型
     */
    public static void removeElementModifiers(ItemStack stack, ElementType elementType) {
        if (stack == null || elementType == null) {
            return;
        }
        
        try {
            // 获取对应的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                return;
            }
            
            // 获取属性的ResourceLocation
            ResourceLocation attributeLocation = BuiltInRegistries.ATTRIBUTE.getKey(elementAttribute);
            if (attributeLocation == null) {
                return;
            }
            
            // 通过NBT移除属性修饰符
            removeAttributeModifiersFromNBT(stack, attributeLocation);
            
        } catch (Exception e) {
            System.err.println("Error removing element modifiers for " + elementType.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 移除物品上所有的元素修饰符
     * @param stack 目标物品堆
     */
    public static void removeAllElementModifiers(ItemStack stack) {
        if (stack == null) {
            return;
        }
        
        try {
            // 直接清空AttributeModifiers标签
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("AttributeModifiers", Tag.TAG_LIST)) {
                nbt.remove("AttributeModifiers");
            }
        } catch (Exception e) {
            System.err.println("Error removing all element modifiers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 重新应用物品的元素修饰符（先移除再添加）
     * @param stack 目标物品堆
     * @param modifiers 修饰符数据列表
     * @param slot 装备槽位
     */
    public static void refreshElementModifiers(ItemStack stack, 
                                           List<ElementModifierData> modifiers, 
                                           EquipmentSlot slot) {
        removeAllElementModifiers(stack);
        applyElementModifiers(stack, modifiers, slot);
    }
    
    /**
     * 应用单个元素修饰符到物品
     * @param stack 目标物品堆
     * @param elementType 元素类型
     * @param amount 修饰符数值
     * @param operation 修饰符操作类型
     * @param slot 装备槽位
     */
    public static void applySingleElementModifier(ItemStack stack, 
                                               ElementType elementType,
                                               double amount,
                                               AttributeModifier.Operation operation,
                                               EquipmentSlot slot) {
        if (stack == null || elementType == null) {
            return;
        }
        
        // 只在主手槽位时应用修饰符，避免工具提示显示其他槽位信息
        if (slot != EquipmentSlot.MAINHAND) {
            return;
        }
        
        try {
            // 创建标准化的修饰符数据
            ElementModifierData modifierData = createModifierData(elementType, amount, operation, stack, 0);
            
            // 获取对应的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                return;
            }
            
            // 创建修饰符
            AttributeModifier modifier = new AttributeModifier(
                modifierData.getId(),
                modifierData.getName(),
                amount,
                operation
            );
            
            // 应用修饰符到物品的元素属性上
            stack.addAttributeModifier(elementAttribute, modifier, slot);
            
        } catch (Exception e) {
            System.err.println("Error applying single element modifier for " + elementType.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 批量创建多个元素修饰符数据
     * @param modifiers 元素类型和数值的映射
     * @param operation 运算模式
     * @param stack 物品堆
     * @return 修饰符数据列表
     */
    public static List<ElementModifierData> createBatchModifiers(
        java.util.Map<ElementType, Double> modifiers,
        AttributeModifier.Operation operation,
        ItemStack stack
    ) {
        List<ElementModifierData> result = new ArrayList<>();
        int index = 0;
        
        for (java.util.Map.Entry<ElementType, Double> entry : modifiers.entrySet()) {
            ElementType elementType = entry.getKey();
            double amount = entry.getValue();
            
            ElementModifierData data = createModifierData(elementType, amount, operation, stack, index++);
            result.add(data);
        }
        
        return result;
    }
    
    /**
     * 从物品的NBT中移除指定属性的所有修饰符
     * @param stack 物品堆
     * @param attributeLocation 属性的ResourceLocation
     */
    private static void removeAttributeModifiersFromNBT(ItemStack stack, ResourceLocation attributeLocation) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("AttributeModifiers", Tag.TAG_LIST)) {
            return;
        }
        
        ListTag modifiersList = nbt.getList("AttributeModifiers", Tag.TAG_COMPOUND);
        ListTag newModifiersList = new ListTag();
        
        // 遍历所有修饰符，保留不属于指定属性的修饰符
        for (Tag tag : modifiersList) {
            CompoundTag modifierTag = (CompoundTag) tag;
            String attributeName = modifierTag.getString("AttributeName");
            
            // 如果不是要移除的属性，则保留
            if (!attributeName.equals(attributeLocation.toString())) {
                newModifiersList.add(modifierTag);
            }
        }
        
        // 更新NBT
        if (newModifiersList.isEmpty()) {
            nbt.remove("AttributeModifiers");
        } else {
            nbt.put("AttributeModifiers", newModifiersList);
        }
    }
    
    /**
     * 创建一个新的修饰符合并缓冲区
     * 用于合并相同属性和操作类型的修饰符
     * @return 修饰符合并缓冲区
     */
    public static Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> createMergeBuffer() {
        return new HashMap<>();
    }
    
    /**
     * 使用线程局部存储创建修饰符合并缓冲区
     * 确保线程安全，适用于多线程环境
     * @return 线程安全的修饰符合并缓冲区
     */
    public static ThreadLocal<Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier>> createThreadLocalMergeBuffer() {
        return ThreadLocal.withInitial(HashMap::new);
    }
    
    /**
     * 合并相同属性和操作的修饰符到缓冲区中
     * 参考GunsmithLib的实现模式
     * @param buffer 合并缓冲区
     * @param attribute 要合并的属性
     * @param modifier 要合并的修饰符
     */
    public static void mergeModifier(Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> buffer,
                                    Attribute attribute,
                                    AttributeModifier modifier) {
        var operation = modifier.getOperation();
        var key = Pair.of(attribute, operation);
        var currentModifier = buffer.get(key);
        
        if (currentModifier == null) {
            buffer.put(key, modifier);
            return;
        }
        
        var newAmount = switch (operation) {
            case ADDITION, MULTIPLY_BASE -> currentModifier.getAmount() + modifier.getAmount();
            case MULTIPLY_TOTAL -> (1 + currentModifier.getAmount()) * (1 + modifier.getAmount()) - 1;
            default -> currentModifier.getAmount() + modifier.getAmount(); // 默认使用加法处理未知操作类型
        };
        
        var newModifier = new AttributeModifier(currentModifier.getId(), currentModifier.getName(), newAmount, operation);
        buffer.put(key, newModifier);
    }
    
    /**
     * 批量合并多个修饰符到缓冲区中
     * @param buffer 合并缓冲区
     * @param attribute 属性
     * @param modifiers 要合并的修饰符集合
     */
    public static void mergeModifiers(Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> buffer,
                                     Attribute attribute,
                                     java.util.Collection<AttributeModifier> modifiers) {
        if (modifiers == null) {
            return;
        }
        
        for (AttributeModifier modifier : modifiers) {
            mergeModifier(buffer, attribute, modifier);
        }
    }
    
    /**
     * 将合并后的修饰符应用到目标对象
     * @param buffer 合并后的修饰符缓冲区
     * @param consumer 修饰符应用回调
     */
    public static void applyMergedModifiers(Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> buffer,
                                           BiConsumer<Attribute, AttributeModifier> consumer) {
        if (buffer == null || consumer == null) {
            return;
        }
        
        for (Map.Entry<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> entry : buffer.entrySet()) {
            consumer.accept(entry.getKey().getLeft(), entry.getValue());
        }
    }
}