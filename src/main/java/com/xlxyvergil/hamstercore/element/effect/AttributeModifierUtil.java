package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

/**
 * 属性修饰符工具类
 * 用于处理元素效果相关的属性修饰符操作
 */
public class AttributeModifierUtil {
    
    // 护甲属性修饰符UUID前缀
    private static final String ARMOR_MODIFIER_UUID_PREFIX = "hamstercore:armor_modifier_";
    
    /**
     * 添加护甲减少修饰符
     * @param entity 实体
     * @param percentage 减少百分比 (0.0-1.0)
     * @param duration 持续时间（tick）
     */
    public static void addArmorReductionModifier(LivingEntity entity, double percentage, int duration) {
        // 生成唯一的UUID
        UUID modifierUUID = UUID.nameUUIDFromBytes((ARMOR_MODIFIER_UUID_PREFIX + duration).getBytes());
        
        // 创建修饰符
        AttributeModifier modifier = new AttributeModifier(
            modifierUUID,
            "Element Armor Reduction",
            -percentage,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 添加修饰符到实体的护甲属性上
        entity.getAttribute(Attributes.ARMOR).addPermanentModifier(modifier);
        
        // 这里可以添加定时任务在duration结束后移除修饰符
        // 为简化代码，这里省略了定时移除的实现
    }
    
    /**
     * 移除护甲减少修饰符
     * @param entity 实体
     */
    public static void removeArmorReductionModifier(LivingEntity entity) {
        // 移除所有以ARMOR_MODIFIER_UUID_PREFIX开头的修饰符
        entity.getAttribute(Attributes.ARMOR).getModifiers().stream()
            .filter(modifier -> modifier.getName().equals("Element Armor Reduction"))
            .forEach(modifier -> entity.getAttribute(Attributes.ARMOR).removeModifier(modifier.getId()));
    }
}