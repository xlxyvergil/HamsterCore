package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;

/**
 * 元素属性修饰符条目，参考GunsmithLibAttributeModifierEntry实现
 * 提供标准化的元素属性修饰符数据结构
 */
public class ElementAttributeModifierEntry {
    /**
     * 要作用在什么属性上。
     * 必填
     */
    private ElementType elementType;

    /**
     * 属性修饰器 id。
     * 必填
     */
    private UUID id;

    /**
     * 属性修饰器名称，该名称可能被神化模组和一些其他的调试功能看到。
     * 可选
     */
    private String name;

    /**
     * 属性修饰器的值
     * 必填
     */
    private double amount;

    /**
     * 属性修饰器的运算模式
     * 可选，默认为加法
     */
    private AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;

    // 缓存计算结果，提高性能
    private transient Pair<ElementAttribute, AttributeModifier> bakedResult;
    private transient int valid = 0;

    public ElementAttributeModifierEntry() {
        // 空构造方法，用于反序列化
    }

    public ElementAttributeModifierEntry(ElementType elementType, UUID id, double amount) {
        this.elementType = elementType;
        this.id = id;
        this.amount = amount;
        this.name = elementType.getDisplayName();
    }

    public ElementAttributeModifierEntry(ElementType elementType, UUID id, double amount, String name, AttributeModifier.Operation operation) {
        this.elementType = elementType;
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.operation = operation;
    }

    public ElementAttributeModifierEntry(ElementType elementType, UUID id, double amount, AttributeModifier.Operation operation) {
        this(elementType, id, amount, elementType.getDisplayName(), operation);
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
        this.invalidateCache();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
        this.invalidateCache();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.invalidateCache();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.invalidateCache();
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    public void setOperation(AttributeModifier.Operation operation) {
        this.operation = operation;
        this.invalidateCache();
    }

    /**
     * 获取元素类型对应的属性
     * @return 元素属性，如果未注册则返回空的Optional
     */
    public Optional<ElementAttribute> getAttribute() {
        return Optional.ofNullable(ElementRegistry.getAttributeValue(elementType));
    }

    /**
     * 获取完整的修饰符信息（属性+修饰符）
     * @return 包含属性和修饰符的Optional，如果属性未注册则返回空
     */
    public Optional<Pair<ElementAttribute, AttributeModifier>> getModifier() {
        if (valid == -1) {
            return Optional.empty();
        }
        if (valid == 1) {
            return Optional.of(Objects.requireNonNull(bakedResult));
        }
        bakedResult = bake();
        valid = bakedResult != null ? 1 : -1;
        return Optional.ofNullable(bakedResult);
    }

    /**
     * 获取属性修饰符对象
     * @return 属性修饰符，如果属性未注册则返回空的Optional
     */
    public Optional<AttributeModifier> getAttributeModifier() {
        return getModifier().map(Pair::getRight);
    }

    /**
     * 清除缓存的结果
     */
    private void invalidateCache() {
        bakedResult = null;
        valid = 0;
    }

    /**
     * 计算并缓存修饰符结果
     * @return 包含属性和修饰符的Pair，如果属性未注册则返回null
     */
    @Nullable
    private Pair<ElementAttribute, AttributeModifier> bake() {
        if (id == null || elementType == null) {
            return null;
        }
        
        var attribute = ElementRegistry.getAttributeValue(elementType);
        if (attribute == null) {
            return null;
        }
        
        return Pair.of(attribute, new AttributeModifier(getId(), getName(), getAmount(), getOperation()));
    }
    
    /**
     * 应用元素修饰符到事件处理器
     * @param stack 目标物品堆
     * @param modifiers 修饰符条目列表
     * @param slot 装备槽位
     * @param consumer 修饰符添加回调
     */
    public static void applyElementModifiers(ItemStack stack, 
                                           List<ElementAttributeModifierEntry> modifiers, 
                                           EquipmentSlot slot,
                                           BiConsumer<Attribute, AttributeModifier> consumer) {
        if (modifiers == null || stack == null || consumer == null) {
            return;
        }
        
        for (ElementAttributeModifierEntry modifierEntry : modifiers) {
            try {
                // 获取修饰符
                var modifier = modifierEntry.getModifier();
                if (modifier.isEmpty()) {
                    System.err.println("Failed to bake modifier for: " + modifierEntry.getElementType().getName());
                    continue;
                }
                
                // 通过回调添加修饰符到事件中
                var pair = modifier.get();
                consumer.accept((Attribute) pair.getLeft(), pair.getRight());
                
            } catch (Exception e) {
                System.err.println("Error applying element modifier for " + modifierEntry.getElementType().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 从ElementType创建修饰符条目
     * @param elementType 元素类型
     * @param amount 修饰符数值
     * @param operation 运算模式
     * @param stack 物品堆（用于UUID管理）
     * @param index 索引
     * @return 修饰符条目
     */
    public static ElementAttributeModifierEntry createModifierEntry(
        ElementType elementType,
        double amount,
        AttributeModifier.Operation operation,
        ItemStack stack,
        int index
    ) {
        // UUID管理
        UUID uuid = ElementRegistry.getModifierUUID(elementType, index);
        
        // 修饰符名称
        String name = elementType.getDisplayName();
        
        return new ElementAttributeModifierEntry(elementType, uuid, amount, name, operation);
    }
    
    /**
     * 批量创建多个元素修饰符条目
     * @param modifiers 元素类型和数值的映射
     * @param operation 运算模式
     * @param stack 物品堆
     * @return 修饰符条目列表
     */
    public static List<ElementAttributeModifierEntry> createBatchModifiers(
        Map<ElementType, Double> modifiers,
        AttributeModifier.Operation operation,
        ItemStack stack
    ) {
        List<ElementAttributeModifierEntry> result = new ArrayList<>();
        int index = 0;
        
        for (Map.Entry<ElementType, Double> entry : modifiers.entrySet()) {
            ElementType elementType = entry.getKey();
            double amount = entry.getValue();
            
            ElementAttributeModifierEntry data = createModifierEntry(elementType, amount, operation, stack, index++);
            result.add(data);
        }
        
        return result;
    }
    
    /**
     * 创建一个新的修饰符合并缓冲区
     * 用于合并相同属性和操作类型的修饰符
     * @return 修饰符合并缓冲区
     */
    public static Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> createMergeBuffer() {
        return Maps.newHashMap();
    }
    
    /**
     * 使用线程局部存储创建修饰符合并缓冲区
     * 确保线程安全，适用于多线程环境
     * @return 线程安全的修饰符合并缓冲区
     */
    public static ThreadLocal<Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier>> createThreadLocalMergeBuffer() {
        return ThreadLocal.withInitial(Maps::newHashMap);
    }
    
    /**
     * 合并相同属性和操作的修饰符到缓冲区中
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
     * @param attribute 要合并的属性
     * @param modifiers 要合并的修饰符集合
     */
    public static void mergeModifiers(Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> buffer,
                                     Attribute attribute,
                                     Iterable<AttributeModifier> modifiers) {
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