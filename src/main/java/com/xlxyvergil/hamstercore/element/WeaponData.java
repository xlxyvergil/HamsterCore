package com.xlxyvergil.hamstercore.element;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

/**
 * 武器数据类
 * 用于存储武器的元素配置信息，适配新的两层NBT数据结构
 * Basic层：存储修饰符的元素类型、排序和是否是CONFIG的信息
 * Usage层：存储复合后的元素以及数值
 */
public class WeaponData {
    // MOD ID
    public String modid;
    
    // 物品ID
    public String itemId;
    
    // TACZ枪械ID（仅TACZ武器使用）
    public String gunId;
    
    // 拔刀剑translationKey（仅拔刀剑使用）
    public String translationKey;
    
    // 基础元素数据（Basic层）- 记录元素名称、添加顺序和CONFIG标记
    private Map<String, List<BasicEntry>> basicElements = new HashMap<>();
    
    // 使用层数据（Usage层）- 元素复合后的元素类型和数值
    private Map<String, Double> usageElements = new HashMap<>();
    
    /**
     * 获取指定类型的所有使用层元素值
     */
    public List<Double> getUsageValue(String type) {
        List<Double> values = new ArrayList<>();
        if (usageElements.containsKey(type)) {
            values.add(usageElements.get(type));
        }
        return values;
    }
    
    // 初始属性修饰符数据（将在世界加载阶段赋予武器）
    private List<AttributeModifierEntry> initialModifiers = new ArrayList<>();
    
    /**
     * 获取基础元素数据
     */
    public Map<String, List<BasicEntry>> getBasicElements() {
        return basicElements;
    }
    
    /**
     * 获取使用层元素数据
     */
    public Map<String, Double> getUsageElements() {
        return usageElements;
    }
    
    /**
     * 获取初始属性修饰符列表
     */
    public List<AttributeModifierEntry> getInitialModifiers() {
        return initialModifiers;
    }
    
    /**
     * 添加基础元素，指定类型、来源和顺序
     */
    public void addBasicElement(String type, String source, int order) {
        basicElements.computeIfAbsent(type, k -> new ArrayList<>()).add(new BasicEntry(type, source, order));
    }
    
    /**
     * 添加基础元素（默认来源为CONFIG）
     */
    public void addBasicElement(String type) {
        addBasicElement(type, "CONFIG");
    }
    
    /**
     * 添加基础元素，指定类型和来源
     */
    public void addBasicElement(String type, String source) {
        // 计算添加顺序，基于当前该元素类型在整体中的位置
        int order = getNextOrderForType(type);
        basicElements.computeIfAbsent(type, k -> new ArrayList<>()).add(new BasicEntry(type, source, order));
    }
    
    /**
     * 移除指定类型的所有基础元素
     * @param type 元素类型
     */
    public void removeBasicElement(String type) {
        if (basicElements.containsKey(type)) {
            // 获取被删除元素的顺序号
            int removedOrder = basicElements.get(type).get(0).getOrder();
            
            // 移除该类型的元素
            basicElements.remove(type);
            
            // 将所有顺序号大于被删除元素的元素向前移动一位
            for (List<BasicEntry> entries : basicElements.values()) {
                if (!entries.isEmpty() && entries.get(0).getOrder() > removedOrder) {
                    // 更新该类型所有元素的顺序号
                    for (BasicEntry entry : entries) {
                        entry.setOrder(entry.getOrder() - 1);
                    }
                }
            }
        }
    }
    
    /**
     * 获取指定元素类型的下一个顺序号
     * @param type 元素类型
     * @return 顺序号
     */
    private int getNextOrderForType(String type) {
        // 如果该类型已存在，则使用现有顺序
        if (basicElements.containsKey(type) && !basicElements.get(type).isEmpty()) {
            return basicElements.get(type).get(0).getOrder();
        }
        
        // 计算总的元素类型数作为新顺序
        return basicElements.size();
    }
    
    /**
     * 设置使用层元素值
     */
    public void setUsageElement(String type, double value) {
        usageElements.put(type, value);
    }
    
    /**
     * 添加初始属性修饰符
     */
    public void addInitialModifier(AttributeModifierEntry modifier) {
        initialModifiers.add(modifier);
    }
    
    /**
     * 属性修饰符条目类
     */
    public static class AttributeModifierEntry {
        private String attributeName;
        private AttributeModifier modifier;
        
        public AttributeModifierEntry(String attributeName, AttributeModifier modifier) {
            this.attributeName = attributeName;
            this.modifier = modifier;
        }
        
        public String getAttributeName() {
            return attributeName;
        }
        
        public AttributeModifier getModifier() {
            return modifier;
        }
    }
    
    /**
     * BasicEntry内部类，表示基础元素条目
     * 记录元素名称、添加顺序和CONFIG标记
     */
    public static class BasicEntry {
        private String type;
        private String source; // CONFIG 或 USER 标记
        private int order; // 添加顺序
        
        public BasicEntry(String type, String source, int order) {
            this.type = type;
            this.source = source;
            this.order = order;
        }
        
        public String getType() {
            return type;
        }
        
        public String getSource() {
            return source;
        }
        
        public int getOrder() {
            return order;
        }
        
        public void setOrder(int order) {
            this.order = order;
        }
        
        // 添加getOperation方法以修复编译错误
        public net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation getOperation() {
            return net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
        }
    }
}